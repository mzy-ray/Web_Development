package mzy.stockmarketviewer;

import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by zhengyangma on 2017/11/28.
 */

public class test {
    public static void main(String[] args) {
        String s = "[{\"S";
        System.out.println(s);
    }

}

//        this.registerForContextMenu(LV);
//
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        menu.setHeaderTitle("Remove from Favorites?");
//        menu.add(0, 0, 0, "No");
//        menu.add(0, 1, 0, "Yes");
//    }
//
//
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        switch (item.getItemId()) {
//            case 0:
//                break;
//            case 1:
//                Object o = LV.getItemAtPosition(menuInfo.position);
//                StockInfolModel fullObject = (StockInfolModel) o;
//                SharedPreferences.Editor editor = favSettings.edit();
//                editor.remove(fullObject.getSymbol());
//                editor.commit();
//                generateFav();
//        }
//        return super.onContextItemSelected(item);
//    }
