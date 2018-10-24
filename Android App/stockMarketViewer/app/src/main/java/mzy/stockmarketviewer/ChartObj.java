package mzy.stockmarketviewer;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

public class ChartObj {
    private Context context;
    private String symbol = "";
    private String indicator = "";
    private String data = null;

    public ChartObj(Context c, String s, String i, String d){
        context = c;
        symbol = s;
        indicator = i;
        data = d;
    }

    @JavascriptInterface
    public String getSymbol() {
        return symbol;
    }
    @JavascriptInterface
    public String getIndicator() {
        return indicator;
    }
    @JavascriptInterface
    public String getData() {
        return data;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }
    public void setData(String data) {
        this.data = data;
    }

    @JavascriptInterface
    public void webLog(String msg){
        Log.i("Web", msg);
    }
}
