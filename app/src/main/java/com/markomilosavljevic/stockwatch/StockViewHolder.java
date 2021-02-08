package com.markomilosavljevic.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView stockSymbol;
    public TextView companyName;
    public TextView stockPrice;
    public TextView priceChange;
    public TextView changePercent;

    public StockViewHolder(View itemView) {
        super(itemView);

        stockSymbol = itemView.findViewById(R.id.stockSymbol);
        companyName = itemView.findViewById(R.id.companyName);
        stockPrice = itemView.findViewById(R.id.stockPrice);
        priceChange = itemView.findViewById(R.id.priceChange);
        changePercent = itemView.findViewById(R.id.changePercent);

    }
}
