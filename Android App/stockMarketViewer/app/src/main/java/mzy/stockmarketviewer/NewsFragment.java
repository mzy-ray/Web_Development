package mzy.stockmarketviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsFragment extends Fragment {
    private ListView newsLV;
    private ProgressBar progress;
    private TextView newsError;
    private ArrayList<NewsModel> nModels;
    private CusNewsAdapter nAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_fragment, container, false);
        nModels = new ArrayList<NewsModel>();
        newsLV = rootView.findViewById(R.id.newsList);
        progress = rootView.findViewById(R.id.newsProgress);
        newsError = rootView.findViewById(R.id.newsError);

        new getNews().execute();
        newsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = newsLV.getItemAtPosition(position);
                NewsModel fullObject = (NewsModel) o;
                String link = fullObject.getLink();
                Uri uri = Uri.parse(fullObject.getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        return rootView;
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



    class getNews extends AsyncTask<String, Void, String>{
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            newsError.setVisibility(View.GONE);
        }
        @Override
        protected String doInBackground(String... param) {
            String error = "";
            nModels = new ArrayList<NewsModel>();
            String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/queryNews?symbol=" + StockDetailActivity.symbol;
            String data = HttpRequestUtil.getRequest(url);
            try{
                JSONObject root = new JSONObject(data);
                JSONArray news = root.getJSONObject("rss").getJSONArray("channel").getJSONObject(0).getJSONArray("item");
                int nums = 0;
                for (int i = 0; i < news.length(); i++){
                    if (news.getJSONObject(i).getJSONArray("link").getString(0).indexOf("article") != -1){
                        NewsModel nModel = new NewsModel();
                        nModel.setLink(news.getJSONObject(i).getJSONArray("link").getString(0));
                        nModel.setTitle(news.getJSONObject(i).getJSONArray("title").getString(0));
                        nModel.setAuthor("Author: " + news.getJSONObject(i).getJSONArray("sa:author_name").getString(0));
                        String date = news.getJSONObject(i).getJSONArray("pubDate").getString(0);
                        date = date.substring(0, date.length() - 6);
                        nModel.setDate("Date: " + date + " EST");
                        nModels.add(nModel);
                        if(++nums == 5) break;
                    }
                }
            }catch (Exception e){
                Log.e("Json Parse Error", e.getMessage());
                error = "error";
            }
            return error;
        }
        @Override
        protected void onPostExecute(String error) {
            progress.setVisibility(View.GONE);
            if (error.equals("")){
                try {
                    nAdapter = new CusNewsAdapter(getContext(), nModels);
                    newsLV.setAdapter(nAdapter);
                    nAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }else{
                try {
                    newsError.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    Log.e("Data Update Error", e.getMessage());
                }
            }
        }
    }

}
