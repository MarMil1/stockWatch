package com.markomilosavljevic.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FinancialDataDownloader implements Runnable {

    private MainActivity mainActivity;
    private String stockSymbol;
    private static final String TAG = "FinancialDataDownloader";

    private static final String STOCKS_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String STOCKS_API = StockAPI.api();


    FinancialDataDownloader(MainActivity mainActivity, String stockSymbol) {
        this.mainActivity = mainActivity;
        this.stockSymbol = stockSymbol;
    }

    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(STOCKS_URL + stockSymbol + "/quote?token=" + STOCKS_API).buildUpon();
        String urlToUse = uriBuilder.toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            return;
        }

        process(sb.toString());

    }

    private void process(String s) {
        s = s.replaceAll("null", "0.0");
        try {
            JSONObject jStock = new JSONObject(s);
            final String stockSymbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");
            double latestPrice = jStock.getDouble("latestPrice");
            double priceChange = jStock.getDouble("change");
            double changePercent = jStock.getDouble("changePercent");


            final Stock stock = new Stock(stockSymbol, companyName, latestPrice, priceChange, changePercent);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(stock);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
