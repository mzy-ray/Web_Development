package mzy.stockmarketviewer;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

public class CurrentFragment extends Fragment {
    private ListView curLV;
    private ProgressBar progress;
    private TextView curError;
    private ArrayList<CurStockModel> csModels;
    private CusCurAdapter cAdapter;
    private ImageView star;
    private boolean liked;
    private SharedPreferences favSettings;
    private String favData;
    private WebView curChart;
    private String priceData;
    private HashMap<String, String> indicatorChartData;
    private Spinner indicatorSpin;
    private String indicator;
    private String prevIndicator;
    private TextView change;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.current_fragment, container, false);
        csModels = new ArrayList<CurStockModel>();
        priceData = null;
        indicatorChartData = new HashMap<String, String>();
        indicator = "Price";
        prevIndicator = "Price";
        curLV = rootView.findViewById(R.id.curList);
        progress = rootView.findViewById(R.id.curProgress);
        curError = rootView.findViewById(R.id.curError);

        favSettings = getContext().getSharedPreferences("favSettings", MODE_PRIVATE);
        liked = false;
        favData = StockDetailActivity.symbol + " 0.0 0.0 0.0";
        star = rootView.findViewById(R.id.starImg);
        if (!favSettings.contains(StockDetailActivity.symbol)){
            star.setImageResource(R.drawable.empty);
        }else {
            star.setImageResource(R.drawable.filled);
            liked = true;
        }
        star.setClickable(true);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = favSettings.edit();
                if(!liked){
                    editor.putString(StockDetailActivity.symbol, favData);
                    star.setImageResource(R.drawable.filled);
                    liked = true;

                } else{
                    editor.remove(StockDetailActivity.symbol);
                    star.setImageResource(R.drawable.empty);
                    liked = false;
                }
                editor.commit();
            }
        });

        curChart = rootView.findViewById(R.id.curChart);
        WebSettings wSet = curChart.getSettings();
        wSet.setJavaScriptEnabled(true);
        wSet.setJavaScriptCanOpenWindowsAutomatically(true);
        curChart.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        indicatorSpin = rootView.findViewById(R.id.indicatorSpinner);
        indicatorSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                switch (pos){
                    case 0:
                        indicator = "Price";
                        break;
                    case 1:
                        indicator = "SMA";
                        break;
                    case 2:
                        indicator = "EMA";
                        break;
                    case 3:
                        indicator = "STOCH";
                        break;
                    case 4:
                        indicator = "RSI";
                        break;
                    case 5:
                        indicator = "ADX";
                        break;
                    case 6:
                        indicator = "CCI";
                        break;
                    case 7:
                        indicator = "BBANDS";
                        break;
                    case 8:
                        indicator = "MACD";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        change = rootView.findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (indicator == prevIndicator) return;
                prevIndicator = indicator;
                Log.i("Indicator", indicator);
                try {
                    curChart.addJavascriptInterface(new ChartObj(getContext(), StockDetailActivity.symbol, indicator, indicatorChartData.get(indicator)), "chartObj");
                    curChart.loadUrl("file:///android_asset/Indicators.html");
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }
        });

        new getCurStock().execute();

        new getIndicator().execute();

        return rootView;
    };



    class getCurStock extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            curError.setVisibility(View.GONE);
        }
        @Override
        protected String doInBackground(String... param) {
            String error = "";
            csModels = new ArrayList<CurStockModel>();
            String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/queryStock?symbol=" + StockDetailActivity.symbol;
            String data = HttpRequestUtil.getRequest(url);
            try {
                JSONObject root = new JSONObject(data);
                StockDetailActivity.symbol = root.getJSONObject("Meta Data").getString("2. Symbol");
                JSONObject days = root.getJSONObject("Time Series (Daily)");
                Iterator it = days.keys();
                String today = (String) it.next();
                String yesterday = (String) it.next();
                JSONObject curData = days.getJSONObject(today);
                JSONObject preData = days.getJSONObject(yesterday);
                float lastPriceF = Float.parseFloat(curData.getString("4. close"));
                float openF = Float.parseFloat(curData.getString("1. open"));
                float lowF = Float.parseFloat(curData.getString("3. low"));
                float highF = Float.parseFloat(curData.getString("2. high"));
                float preCloseF = Float.parseFloat(preData.getString("4. close"));
                float changeF = Float.parseFloat(curData.getString("4. close")) - Float.parseFloat(preData.getString("4. close"));
                float changePercentF = changeF / Float.parseFloat(preData.getString("4. close")) * 100;
                DecimalFormat decimalFormat = new DecimalFormat(".00");
                String lastPrice = decimalFormat.format(lastPriceF);
                String open = decimalFormat.format(openF);
                String preClose = decimalFormat.format(preCloseF);
                String change = decimalFormat.format(changeF);
                String changePercent = decimalFormat.format(changePercentF);
                String range = decimalFormat.format(lowF) + " - " + decimalFormat.format(highF);
                String volume = curData.getString("5. volume");
                String timeStamp = root.getJSONObject("Meta Data").getString("3. Last Refreshed");
                if (timeStamp.length() == 10) timeStamp += " 16:00:00 EST";
                else if (!timeStamp.endsWith("T")) timeStamp += " EST";

                CurStockModel row1 = new CurStockModel();
                row1.setName("Stock Symbol");
                row1.setValue(StockDetailActivity.symbol);
                csModels.add(row1);
                CurStockModel row2 = new CurStockModel();
                row2.setName("Last Price");
                row2.setValue(lastPrice);
                csModels.add(row2);
                CurStockModel row3 = new CurStockModel();
                row3.setName("Change");
                row3.setValue(change + " (" + changePercent + "%)");
                row3.setImg("arrow");
                csModels.add(row3);
                CurStockModel row4 = new CurStockModel();
                row4.setName("TimeStamp");
                row4.setValue(timeStamp);
                csModels.add(row4);
                CurStockModel row5 = new CurStockModel();
                row5.setName("Open");
                row5.setValue(open);
                csModels.add(row5);
                CurStockModel row6 = new CurStockModel();
                row6.setName("close");
                row6.setValue(preClose);
                csModels.add(row6);
                CurStockModel row7 = new CurStockModel();
                row7.setName("Day's Range");
                row7.setValue(range);
                csModels.add(row7);
                CurStockModel row8 = new CurStockModel();
                row8.setName("Volume");
                row8.setValue(volume);
                csModels.add(row8);

                favData = StockDetailActivity.symbol + " " + lastPrice + " " + change + " " + changePercent;
                priceData = data;
            } catch (Exception e) {
                Log.e("Json Parse Error", e.getMessage());
                error = "error";
            }
            return error;
        }
        @SuppressLint("JavascriptInterface")
        @Override
        protected void onPostExecute(String error) {
            progress.setVisibility(View.GONE);
            if (error.equals("")) {
                try {
                    cAdapter = new CusCurAdapter(getContext(), csModels);
                    curLV.setAdapter(cAdapter);
                    cAdapter.notifyDataSetChanged();
                    curChart.addJavascriptInterface(new ChartObj(getContext(), StockDetailActivity.symbol, "Price", priceData), "chartObj");
                    curChart.loadUrl("file:///android_asset/Price.html");
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            } else {
                try {
                    curError.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }
        }
    }


    class getIndicator extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... param) {
            indicatorChartData.put("Price", priceData);
            indicatorChartData.put("SMA", "");
            indicatorChartData.put("EMA", "");
            indicatorChartData.put("STOCH", "");
            indicatorChartData.put("RSI", "");
            indicatorChartData.put("ADX", "");
            indicatorChartData.put("CCI", "");
            indicatorChartData.put("BBANDS", "");
            indicatorChartData.put("MACD", "");
            String error = "";
            String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/queryIndicator?symbol=" + StockDetailActivity.symbol;
            String data = HttpRequestUtil.getRequest(url);
            if (data.equals("")) error = "error";
            try{
                JSONObject root = new JSONObject(data);
                indicatorChartData.put("SMA", root.getJSONObject("SMA").toString());
                indicatorChartData.put("EMA", root.getJSONObject("EMA").toString());
                indicatorChartData.put("STOCH", root.getJSONObject("STOCH").toString());
                indicatorChartData.put("RSI", root.getJSONObject("RSI").toString());
                indicatorChartData.put("ADX", root.getJSONObject("ADX").toString());
                indicatorChartData.put("CCI", root.getJSONObject("CCI").toString());
                indicatorChartData.put("BBANDS", root.getJSONObject("BBANDS").toString());
                indicatorChartData.put("MACD", root.getJSONObject("MACD").toString());
            }catch (Exception e){
                Log.e("Data Update Error", e.getMessage());
            }
            return error;
        }
        @SuppressLint("JavascriptInterface")
        @Override
        protected void onPostExecute(String error) {
            if (!error.equals("")) {
                try {
                    Toast.makeText(getContext(), "Failed to get indicators", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }
        }
    }
}
