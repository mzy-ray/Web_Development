package mzy.stockmarketviewer;

import java.util.Comparator;

public class StockInfolModel {
    private String symbol = "";
    private float lastPrice = 0;
    private float change = 0;
    private float changePercent = 0;
    private String timeStamp = "";
    private float open = 0;
    private float close = 0;
    private String range = "";
    private float volume = 0;


    public String getSymbol() {
        return symbol;
    }
    public float getLastPrice() {
        return lastPrice;
    }
    public float getChange() {
        return change;
    }
    public float getChangePercent() {
        return changePercent;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public float getOpen() {
        return open;
    }
    public float getClose() {
        return close;
    }
    public String getRange() {
        return range;
    }
    public float getVolume() {
        return volume;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public void setLastPrice(float lastPrice) {
        this.lastPrice = lastPrice;
    }
    public void setChange(float change) {
        this.change = change;
    }
    public void setChangePercent(float changePercent) {
        this.changePercent = changePercent;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    public void setOpen(float open) {
        this.open = open;
    }
    public void setClose(float close) {
        this.close = close;
    }
    public void setRange(String range) {
        this.range = range;
    }
    public void setVolume(float volume) {
        this.volume = volume;
    }


    public static Comparator<StockInfolModel> bySymbol = new Comparator<StockInfolModel>(){
        public int compare(StockInfolModel s1, StockInfolModel s2) {
            return s1.getSymbol().compareToIgnoreCase(s2.getSymbol());
        }
    };
    public static Comparator<StockInfolModel> byLastPrice = new Comparator<StockInfolModel>(){
        public int compare(StockInfolModel s1, StockInfolModel s2) {
            return Float.compare(s1.getLastPrice(), s2.getLastPrice());
        }
    };
    public static Comparator<StockInfolModel> byChange = new Comparator<StockInfolModel>(){
        public int compare(StockInfolModel s1, StockInfolModel s2) {
            return Float.compare(s1.getChange(), s2.getChange());
        }
    };
    public static Comparator<StockInfolModel> byChangePercent = new Comparator<StockInfolModel>(){
        public int compare(StockInfolModel s1, StockInfolModel s2) {
            return Float.compare(s1.getChangePercent(), s2.getChangePercent());
        }
    };

}
