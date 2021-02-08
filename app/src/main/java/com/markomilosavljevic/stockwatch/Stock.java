package com.markomilosavljevic.stockwatch;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    private String stockSymbol;
    private String companyName;
    private double latestPrice;
    private double priceChange;
    private double changePercent;

    public Stock(String stockSymbol, String companyName, double latestPrice,
                 double priceChange, double changePercent) {

        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.latestPrice = latestPrice;
        this.priceChange = priceChange;
        this.changePercent = changePercent;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
       return companyName;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getChangePercent() {
        return changePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return companyName.equals(stock.companyName) &&
                stockSymbol.equals(stock.stockSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, stockSymbol);
    }

    @Override
    public int compareTo(Stock stock) {
        return companyName.compareTo(stock.getCompanyName());
    }
}
