package mzy.stockmarketviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CusNewsAdapter extends BaseAdapter {
    private static ArrayList<NewsModel> newsList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CusNewsAdapter(Context context, ArrayList<NewsModel> newsDataList){
        newsList = newsDataList;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return newsList.size();
    }

    public Object getItem(int position) {
        return newsList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.newslist_item, null);
            holder = new ViewHolder();
            holder.Title = convertView.findViewById(R.id.news_title);
            holder.Author = convertView.findViewById(R.id.news_author);
            holder.Date = convertView.findViewById(R.id.news_date);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        NewsModel item = newsList.get(position);
        holder.Title.setText(item.getTitle());
        holder.Author.setText(item.getAuthor());
        holder.Date.setText(item.getDate());

        return convertView;
    }

    static class ViewHolder {
        TextView Title;
        TextView Author;
        TextView Date;
    }
}
