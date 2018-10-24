package mzy.stockmarketviewer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

public class HistoricalFragment extends Fragment {
    private ProgressBar progress;
    private TextView hisError;
    private WebView hisChart;
    private String chartData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.historical_fragment, container, false);
        chartData = null;
        progress = rootView.findViewById(R.id.hisProgress);
        hisError = rootView.findViewById(R.id.hisError);
        hisChart = rootView.findViewById(R.id.hisChart);
        WebSettings wSet = hisChart.getSettings();
        wSet.setJavaScriptEnabled(true);
        hisChart.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        new getHisStock().execute();

        return rootView;
    };



    class getHisStock extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            hisError.setVisibility(View.GONE);
        }
        @Override
        protected String doInBackground(String... param) {
            String error = "";
            String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/queryStock?symbol=" + StockDetailActivity.symbol;
            chartData = HttpRequestUtil.getRequest(url);
            if (chartData.equals("")) error = "error";
            return error;
        }
        @SuppressLint("JavascriptInterface")
        @Override
        protected void onPostExecute(String error) {
            progress.setVisibility(View.GONE);
            if (error.equals("")) {
                try {
                    hisChart.addJavascriptInterface(new ChartObj(getContext(), StockDetailActivity.symbol, "Historical", chartData.toString()), "chartObj");
                    hisChart.loadUrl("file:///android_asset/Historical.html");
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            } else {
                try {
                    hisError.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }
        }
    }

}
