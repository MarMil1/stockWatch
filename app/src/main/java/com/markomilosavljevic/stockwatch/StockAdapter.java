package com.markomilosavljevic.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainAct;

    StockAdapter(List<Stock> stcList, MainActivity ma) {
        this.stockList = stcList;
        this.mainAct = ma;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_entry, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        String finalPriceChange = String.format(Locale.getDefault(),"%.2f", stock.getPriceChange());
        double value = Double.parseDouble(finalPriceChange);

        if (value < 0) {
            holder.priceChange.setText(String.format("▼  %s", finalPriceChange));
            holder.stockSymbol.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.stockPrice.setTextColor(Color.RED);
            holder.priceChange.setTextColor(Color.RED);
            holder.changePercent.setTextColor(Color.RED);

        } else if (value == 0.00) {
            holder.priceChange.setText(String.format("%s", finalPriceChange));
            holder.stockSymbol.setTextColor(Color.WHITE);
            holder.companyName.setTextColor(Color.WHITE);
            holder.stockPrice.setTextColor(Color.WHITE);
            holder.priceChange.setTextColor(Color.WHITE);
            holder.changePercent.setTextColor(Color.WHITE);
        }

        else {
            holder.priceChange.setText(String.format("▲  %s", finalPriceChange));
            holder.stockSymbol.setTextColor(Color.parseColor("#00d800"));
            holder.companyName.setTextColor(Color.parseColor("#00d800"));
            holder.stockPrice.setTextColor(Color.parseColor("#00d800"));
            holder.priceChange.setTextColor(Color.parseColor("#00d800"));
            holder.changePercent.setTextColor(Color.parseColor("#00d800"));
        }

        holder.stockSymbol.setText(String.format("%s",stock.getStockSymbol()));
        holder.companyName.setText(String.format("%s", stock.getCompanyName()));
        holder.stockPrice.setText(String.format(Locale.getDefault(),"%.2f", stock.getLatestPrice()));
        holder.changePercent.setText(String.format(Locale.getDefault(),"(%.2f%%)",stock.getChangePercent()));

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
