package com.markomilosavljevic.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> tempList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private StockAdapter mAdapter;
    private String choice;
    private static final String MARKET_WATCH_URL = "http://www.marketwatch.com/investing/stock/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        swipeRefresh = findViewById(R.id.swiper);
        mAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefresh.setOnRefreshListener(this);

        onStartupConnection();

        SymbolNameDownloader rd = new SymbolNameDownloader();
        new Thread(rd).start();

        loadStocks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_stocks_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_stock_button) {
            makeStockDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeStockDialog() {
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
            builder.setIcon(R.drawable.warning);
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        if (SymbolNameDownloader.symbolNameMap.size() < 1) {
            SymbolNameDownloader rd = new SymbolNameDownloader();
            new Thread(rd).start();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                choice = et.getText().toString().trim();

                final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

                if (results.size() == 0) {
                    doNoAnswer(choice);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));
                } else {
                    String[] array = results.toArray(new String[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a Selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String symbol = results.get(which);
                            doSelection(symbol);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setMessage("Please Enter a Symbol or Name:");
        builder.setTitle("Stock Selection");
        builder.setIcon(R.drawable.input);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void doNoAnswer(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No Data for Specified Symbol/Name");
        builder.setTitle("No Data Found: " + symbol);
        builder.setIcon(R.drawable.error);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void doSelection(String sym) {
        String[] data = sym.split("-");
        FinancialDataDownloader stockDownloader = new FinancialDataDownloader(this, data[0].trim());
        new Thread(stockDownloader).start();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        String symbol = stockList.get(pos).getStockSymbol();
        String urlWithSymbol = MARKET_WATCH_URL + symbol;

        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Cannot Reach External URL ");
            builder.setIcon(R.drawable.warning);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(urlWithSymbol));
            startActivity(i);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock stock = stockList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock Symbol");
        builder.setMessage("Delete Stock Symbol " + stock.getStockSymbol() + "?");
        builder.setIcon(R.drawable.delete);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, stock.getStockSymbol() + " Deleted", Toast.LENGTH_LONG).show();
                stockList.remove(pos);
                tempList.remove(pos);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    @Override
    public void onRefresh() {
        if (checkNetworkConnection()) {
            stockList.clear();
            for (int i = 0; i < tempList.size(); i++) {
                String stockSymbol = tempList.get(i).getStockSymbol();
                Log.d(TAG, "onRefresh symbol: " + stockSymbol);
                FinancialDataDownloader fdd = new FinancialDataDownloader(this, stockSymbol);
                new Thread(fdd).start();

                Log.d(TAG, "onRefresh: finished index " + i);
            }
            Collections.sort(stockList);
            tempList.clear();
            mAdapter.notifyDataSetChanged();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Refresh Is Currently Not Available");
            builder.setIcon(R.drawable.warning);
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        swipeRefresh.setRefreshing(false);
    }


    public void onStartupConnection() {
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Unable to Update The Content");
            builder.setIcon(R.drawable.warning);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void addStock(Stock stock) {
        if (stock == null) {
            badDataAlert(choice);
            return;
        }

        if (stockList.contains(stock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(stock.getStockSymbol() + " is Already Displayed");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        stockList.add(stock);
        tempList.add(stock);
        Collections.sort(stockList);
        Collections.sort(tempList);
        mAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String sym) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No Data for Selection");
        builder.setTitle("Symbol Not Found: " + sym);
        builder.setIcon(R.drawable.error);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveStocks();
    }

    // Read JSON data
    private void loadStocks() {
        try{
            InputStream inputStream = getApplicationContext().
                    openFileInput(getString(R.string.file_name));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray stockArr = new JSONArray(sb.toString());

            if (!checkNetworkConnection()) {
                for (int i = 0; i < stockArr.length(); i++) {
                    JSONObject nObj = stockArr.getJSONObject(i);
                    String stockSymbol = nObj.getString("stockSymbol");
                    String companyName = nObj.getString("companyName");
                    double latestPrice = 0.0;
                    double priceChange = 0.0;
                    double changePercent = 0.0;

                    Stock s = new Stock(stockSymbol, companyName, latestPrice, priceChange, changePercent);
                    stockList.add(s);
                    tempList.add(s);
                    Collections.sort(stockList);
                    Collections.sort(tempList);
                }

            } else {
                for (int i = 0; i < stockArr.length(); i++) {
                    JSONObject nObj = stockArr.getJSONObject(i);
                    String stockSymbol = nObj.getString("stockSymbol");
                    String companyName = nObj.getString("companyName");
                    double latestPrice = nObj.getDouble("latestPrice");
                    double priceChange = nObj.getDouble("priceChange");
                    double changePercent = nObj.getDouble("changePercent");

                    Stock s = new Stock(stockSymbol, companyName, latestPrice, priceChange, changePercent);
                    stockList.add(s);
                    tempList.add(s);
                    Collections.sort(stockList);
                    Collections.sort(tempList);
                }
            }
            mAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save JSON data
    private void saveStocks() {
        try {
            FileOutputStream fileOutputStream = getApplicationContext()
                    .openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            jsonWriter.setIndent("  ");
            jsonWriter.beginArray();
            for (Stock stock: stockList) {
                jsonWriter.beginObject();
                jsonWriter.name("stockSymbol").value(stock.getStockSymbol());
                jsonWriter.name("companyName").value(stock.getCompanyName());
                jsonWriter.name("latestPrice").value(stock.getLatestPrice());
                jsonWriter.name("priceChange").value(stock.getPriceChange());
                jsonWriter.name("changePercent").value(stock.getChangePercent());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}