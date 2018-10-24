package mzy.stockmarketviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CusCurAdapter extends BaseAdapter {
    private static ArrayList<CurStockModel> curList;
    private Context mContext;
    private LayoutInflater mInflater;

    public CusCurAdapter(Context context, ArrayList<CurStockModel> curDataList){
        curList = curDataList;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int getCount() {
        return curList.size();
    }

    public Object getItem(int position) {
        return curList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CurStockModel item = curList.get(position);
        if (item.getImg().equals("")) {
            CusCurAdapter.ViewHolder1 holder;
            convertView = mInflater.inflate(R.layout.curlist_item1, null);
            holder = new CusCurAdapter.ViewHolder1();
            holder.Name = convertView.findViewById(R.id.cur_name);
            holder.Value = convertView.findViewById(R.id.cur_value);
            holder.Name.setText(item.getName());
            holder.Value.setText(item.getValue());
        }else{
            CusCurAdapter.ViewHolder2 holder;
            convertView = mInflater.inflate(R.layout.curlist_item2, null);
            holder = new CusCurAdapter.ViewHolder2();
            holder.Name = convertView.findViewById(R.id.cur_name2);
            holder.Value = convertView.findViewById(R.id.cur_value2);
            holder.Image = convertView.findViewById(R.id.cur_image);
            holder.Name.setText(item.getName());
            holder.Value.setText(item.getValue());
            if (item.getValue().startsWith("-")){
                holder.Image.setImageResource(R.drawable.down);
            }else{
                holder.Image.setImageResource(R.drawable.up);
            }
        }

        return convertView;
    }

    static class ViewHolder1 {
        TextView Name;
        TextView Value;
    }

    static class ViewHolder2 {
        TextView Name;
        TextView Value;
        ImageView Image;
    }
}
