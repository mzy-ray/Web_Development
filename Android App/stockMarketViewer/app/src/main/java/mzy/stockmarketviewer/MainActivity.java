package mzy.stockmarketviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView autoComplete;
    private ArrayAdapter<String> compAdapter;
    private ArrayList<String> compSymbols;
    private TextView getQuoteTxt;
    private TextView clearTxt;
    private Toast errToast;
    private ListView favLV;
    private ProgressBar progress;
    private CusFavAdapter sIAdapter;
    private ArrayList<StockInfolModel> sIModels;
    private SharedPreferences favSettings;
    private ImageView refreshImg;
    private Switch autoSw;
    private Spinner sortSpin;
    private Spinner orderSpin;
    private int sortBy;
    private int orderBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compSymbols = new ArrayList<String>();
        sIModels = new ArrayList<StockInfolModel>();
        sortBy = 0;
        orderBy = 0;
        favSettings = getSharedPreferences("favSettings", MODE_PRIVATE);

        autoComplete = (AutoCompleteTextView)findViewById(R.id.autoCompleteText);
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                String selection = (String)parent.getItemAtPosition(position);
                selection  = selection.split(" ")[0];
                autoComplete.setText(selection);
            }
        });
        autoComplete.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable editable) {
                // TODO Auto-generated method stub
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = s.toString();
                new getAutoComplete().execute(newText);
            }
        });

        getQuoteTxt = findViewById(R.id.getQuote);
        getQuoteTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String symbol = autoComplete.getText().toString().trim();
                if(symbol.equals("")) {
                    errToast = Toast.makeText(getApplicationContext(), "Please enter a stock name or symbol", Toast.LENGTH_SHORT);
                    errToast.show();
                } else{
                    Intent it = new Intent(MainActivity.this, StockDetailActivity.class);
                    it.putExtra("symbol", symbol);
                    MainActivity.this.startActivity(it);
                }
            }
        });

        clearTxt = findViewById(R.id.clear);
        clearTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoComplete.setText("");
                if (errToast != null){
                    errToast.cancel();
                }
            }
        });

        refreshImg = findViewById(R.id.refresh);
        refreshImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new updateFav().execute();
            }
        });

        final java.util.Timer timer = new java.util.Timer(true);
        final TimerTask timerTask = new TimerTask() {
            public void run() {
                autoRefresh();
            }
        };
        autoSw = findViewById(R.id.autoSwitch);
        autoSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    timer.scheduleAtFixedRate(timerTask, 0, 5000);
                }else {
                    timer.cancel();
                }
            }
        });

        sortSpin = findViewById(R.id.sortSpinner);
        orderSpin = findViewById(R.id.orderSpinner);
        sortSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sortBy = pos;
                sortFav();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        orderSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                orderBy = pos;
                if (sortBy != 0) {
                    sortFav();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        favLV = findViewById(R.id.favList);
        progress = findViewById(R.id.favProgress);

//        SharedPreferences.Editor editor = favSettings.edit();
//        editor.putString("MSFT","MSFT 1.0 -1.0 -1.0");
//        editor.commit();

        generateFav();
        new updateFav().execute();

        favLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = favLV.getItemAtPosition(position);
                StockInfolModel fullObject = (StockInfolModel) o;
                String symbol = fullObject.getSymbol();
                Intent it = new Intent(MainActivity.this, StockDetailActivity.class);
                it.putExtra("symbol", symbol);
                MainActivity.this.startActivity(it);
            }
        });
        favLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, final int position, long id) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.remove_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuNo:
                                break;
                            case R.id.menuYes:
                                Object o = favLV.getItemAtPosition(position);
                                StockInfolModel fullObject = (StockInfolModel) o;
                                SharedPreferences.Editor editor = favSettings.edit();
                                editor.remove(fullObject.getSymbol());
                                editor.commit();
                                generateFav();
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });


    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        generateFav();
        new updateFav().execute();
    }



    private void generateFav(){
        Map<String, ?> allEntries = favSettings.getAll();
        sIModels = new ArrayList<StockInfolModel>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String[] favData = entry.getValue().toString().split(" ");
            StockInfolModel sIModel = new StockInfolModel();
            sIModel.setSymbol(favData[0]);
            sIModel.setLastPrice(Float.parseFloat(favData[1]));
            sIModel.setChange(Float.parseFloat(favData[2]));
            sIModel.setChangePercent(Float.parseFloat(favData[3]));
            sIModels.add(sIModel);
        }
        sIAdapter = new CusFavAdapter(getApplicationContext(), sIModels);
        favLV.setAdapter(sIAdapter);
        sIAdapter.notifyDataSetChanged();
    }


    private void autoRefresh(){

    }



    private void sortFav(){
        if (sortBy == 0) {
            generateFav();
            return;
        }
        switch (sortBy){
            case 1:
                Collections.sort(sIModels, StockInfolModel.bySymbol);
                break;
            case 2:
                Collections.sort(sIModels, StockInfolModel.byLastPrice);
                break;
            case 3:
                Collections.sort(sIModels, StockInfolModel.byChange);
                break;
            case 4:
                Collections.sort(sIModels, StockInfolModel.byChangePercent);
                break;
        }
        if (orderBy == 1) Collections.reverse(sIModels);
        sIAdapter = new CusFavAdapter(getApplicationContext(), sIModels);
        favLV.setAdapter(sIAdapter);
        sIAdapter.notifyDataSetChanged();
    }



    class getAutoComplete extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... key) {
            compSymbols = new ArrayList<String>();
            String symbol = key[0].trim();
            if (symbol.equals("")) return "";
            String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/complete?symbol=" + symbol;
            String data = HttpRequestUtil.getRequest(url);
            Log.i("AutoComplete Data", data);
            if (data.equals("")) return "";

            try {
                JSONArray jsArray = new JSONArray(data);
                for (int i = 0; i < jsArray.length() && i < 5; i++) {
                    JSONObject jsonobject = jsArray.getJSONObject(i);
                    String display = jsonobject.getString("Symbol") + " - " + jsonobject.getString("Name") + " (" + jsonobject.getString("Exchange") + ")";
                    compSymbols.add(i, display);
                }
            }catch (Exception e){
                Log.e("Json Parse Error", e.getMessage());
                return "";
            }
            return "";
        }
        @Override
        protected void onPostExecute(String data) {
            compAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.comp_item, compSymbols);
            autoComplete.setAdapter(compAdapter);
            compAdapter.notifyDataSetChanged();
        }
    }


    class updateFav extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }
        @Override
        protected ArrayList<String> doInBackground(String... param) {
            ArrayList<String> errors = new ArrayList<String>();
            Map<String, ?> allEntries = favSettings.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String symbol = entry.getValue().toString().split(" ")[0];
                String url = "http://default-environment.wgjpy8yryt.us-east-2.elasticbeanstalk.com/queryLatest?symbol=" + symbol;
                String data = HttpRequestUtil.getRequest(url);
                try {
                    JSONObject root = new JSONObject(data);
                    JSONObject days = root.getJSONObject("Time Series (Daily)");
                    Iterator it = days.keys();
                    String today = (String) it.next();
                    String yesterday = (String) it.next();
                    JSONObject curData = days.getJSONObject(today);
                    JSONObject preData = days.getJSONObject(yesterday);
                    float lastPriceF = Float.parseFloat(curData.getString("4. close"));
                    float changeF = Float.parseFloat(curData.getString("4. close")) - Float.parseFloat(preData.getString("4. close"));
                    float changePercentF = changeF / Float.parseFloat(preData.getString("4. close")) * 100;
                    DecimalFormat decimalFormat = new DecimalFormat(".00");
                    String lastPrice = decimalFormat.format(lastPriceF);
                    String change = decimalFormat.format(changeF);
                    String changePercent = decimalFormat.format(changePercentF);
                    String favData = symbol + " " + lastPrice + " " + change + " " + changePercent;

                    SharedPreferences.Editor editor = favSettings.edit();
                    editor.putString(symbol, favData);
                    editor.commit();
                    Log.i("Fav Updated", favData);
                }catch (Exception e){
                    Log.e("Json Parse Error", e.getMessage());
                    errors.add(symbol);
                }
            }
            return errors;
        }
        @Override
        protected void onPostExecute(ArrayList<String> errors) {
            generateFav();
            progress.setVisibility(View.GONE);
            for (String e : errors){
                errToast = Toast.makeText(getApplicationContext(), "Failed to update " + e, Toast.LENGTH_SHORT);
                errToast.show();
            }
        }
    }



}
