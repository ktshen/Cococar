package com.tom.cococar;

/**
 * Created by timmy on 2017/4/20.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
public class Directions {

    private GoogleMap map;
    private Context context;
    private String mode;
    public final static String MODE_DRIVING = "driving";
    public final static String MODE_WALKING = "walking";
    public final static String MODE_BICYCLING = "bicycling";
    public final static String MODE_TRANSIT = "transit";

    private Directions() {}
    private static Directions _instance = new Directions();
    public static Directions getInstance() {
        return _instance;
    }
    public void draw(Context context, LatLng origin, LatLng dest,
                     GoogleMap map, String mode) {
        this.context = context;
        this.map = map;
        this.mode = mode;
        // 取得 Google Directions API URL
        String url = getDirectionsUrl(origin, dest, mode);
        System.out.println(url);
        DownloadTask downloadTask = new DownloadTask();
        // 開始下載json經由Google Directions
        downloadTask.execute(url);
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest,
                                    String mode) {

        // 起點位置
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // 終點位置
        String str_dest = "destination=" + dest.latitude + ","
                + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";

        // 參數接合
        String parameters = str_origin + "&" + str_dest + "&"
                + sensor + "&mode=" + mode;

        // 輸出格式
        String output = "json";

        // 建立完整URL
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        return url;
    }
    // 從URL下載JSON資料的方法
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // 建立 http connection
            urlConnection = (HttpURLConnection) url.openConnection();
            // 啟動連線
            urlConnection.connect();
            // 讀取資料
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    // 下載並解析JSON
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                // 取得所下載的資料
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // 解析JSON
            parserTask.execute(result);
        }
    }
    // 解析JSON格式
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // 解析JSON資料
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // 走訪所有 result
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                // 得到每一個位置(經緯度)資料
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    // 放置折線點經緯度集合
                    points.add(position);
                }
                // 繪製折線點經緯度集合
                lineOptions.addAll(points);
                lineOptions.width(5); // 導航路徑寬度
                lineOptions.color(Color.BLUE); // 導航路徑顏色
            }
            if(lineOptions != null) {
                map.addPolyline(lineOptions);
            } else {
                Toast.makeText(context, mode + "模式下無導航路徑 !",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

