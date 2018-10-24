package mzy.stockmarketviewer;

import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequestUtil {
    public static String getRequest(String address){
        String data = "";
        try{
            URL url = new URL(address);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            data = out.toString();
            return data;
        }catch (Exception e){
            Log.e("Http Error", e.getMessage());
            return data;
        }
    }
}
