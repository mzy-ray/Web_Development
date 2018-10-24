package mzy.stockmarketviewer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CusFavAdapter extends BaseAdapter {
    private static ArrayList<StockInfolModel> stockInfolList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CusFavAdapter(Context context, ArrayList<StockInfolModel> favDataList){
        stockInfolList = favDataList;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return stockInfolList.size();
    }

    public Object getItem(int position) {
        return stockInfolList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.favlist_item, null);
            holder = new ViewHolder();
            holder.Symbol = convertView.findViewById(R.id.fav_symbol);
            holder.LastPrice = convertView.findViewById(R.id.fav_lastprice);
            holder.Change = convertView.findViewById(R.id.fav_change);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        StockInfolModel item = stockInfolList.get(position);
        holder.Symbol.setText(item.getSymbol());
        holder.LastPrice.setText(Float.toString(item.getLastPrice()));
        holder.Change.setText(Float.toString(item.getChange()) + " (" + Float.toString(item.getChangePercent()) + "%)");
        if(item.getChange() >= 0){
            holder.Change.setTextColor(Color.rgb(0,255,0));
        }
        else {
            holder.Change.setTextColor(Color.rgb(255,0,0));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView Symbol;
        TextView LastPrice;
        TextView Change;
    }
}
