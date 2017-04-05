package com.tom.cococar;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import static android.Manifest.permission.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static String url = "rtmp://140.115.158.81:1935/live/";
    public static String json_url="http://140.115.158.81/project/getjson.php";

    private static final int REQUEST_LOCATION = 2;
    private static final int REQUEST_CAMERA = 3;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    String id = "id";
    String rand = Integer.toString((int) (Math.random()*10000));
    boolean talkadd = false;
    private EditText edtalk;
    private Button submit;
    private Button delete;
    String liverand="";
    String fixrand="";
    private static ExecutorService THREAD_POOL_EXECUTOR;
    static {
        THREAD_POOL_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(10);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        edtalk = (EditText) findViewById(R.id.ed_talk);
        submit = (Button) findViewById(R.id.submit);
        delete=(Button)findViewById(R.id.delete);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CAMERA},
                    REQUEST_CAMERA
            );

        } else {
            //已有權限，執行程式
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        createLocationRequest();
        UpdateSync update = new UpdateSync();
        update.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            setupMyLocation();
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //連結到撥放器
                Intent intent = getPackageManager().getLaunchIntentForPackage("tcking.github.com.giraffeplayer");
                intent.putExtra("url",marker.getSnippet());
                startActivity(intent);
                return false;
            }
        });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_LOCATION:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 使用者允許權限
//                    //noinspection MissingPermission
//                    setupMyLocation();
//                } else {
//                    // 使用者拒絕授權 , 停用 MyLocation 功能
//                }
//                break;
//            case REQUEST_CAMERA:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 使用者允許權限
//                    //noinspection MissingPermission
//                } else {
//                    // 使用者拒絕授權 , 停用 MyLocation 功能
//                }
//                break;
//        }
//    }

    private void setupMyLocation() {
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    // 透過位置服務，取得目前裝置所在
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    // 設定標準為存取精確
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
                    String provider = locationManager.getBestProvider(criteria, true);
                    //noinspection MissingPermission
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        Log.i("LOCATION", location.getLatitude() + "/" +
                                location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude())
                                , 15));
                    }
                    return false;
                }
            }
        );
    }


    //手機旋轉不重開
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission

        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (location != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 15));
        }
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude())
                    , 15));
        }
    }

    public void live_button(View view) {
        Toast toast = Toast.makeText(this, "Add a new marker", Toast.LENGTH_SHORT);
        toast.show();
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // 設定標準為存取精確
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
        String provider = locationManager.getBestProvider(criteria, true);
        //noinspection MissingPermission
        Location location = locationManager.getLastKnownLocation("network");
        if (location != null) {
            LatLng Now = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(Now)
                    .title("Current Position"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Now, 12));
        }
        liverand = "marker"+rand;
        url=url+liverand;
        Log.d("bg", url);
        String method="register";
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        BackgroundTask backgroundTask=new BackgroundTask(this);
        backgroundTask.executeOnExecutor(THREAD_POOL_EXECUTOR,method,id,liverand,longitude,latitude,url);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
        Log.d("janices", "in back 2");
        //連結到camera

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }


    //解析JSON資料
    private ArrayList<Transaction> parseJSON(String s) {
        ArrayList<Transaction> trans = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0; i < array.length(); i++) {
                Log.d("COCO", "COCO3");
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("id");
                String rand = obj.getString("rand");
                String latitude = obj.getString("latitude");
                String longitude = obj.getString("longitude");
                String url = obj.getString("url");
                String talk = obj.getString("talk");
                Log.d("COCO", id + "/" + rand + "/" + latitude + "/" + longitude + "/" + url);
                Transaction t = new Transaction(id, rand, latitude, longitude, url, talk);
                trans.add(t);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trans;
    }


    public static String getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
        char[] buffer = new char[1024];
        String jsonString = new String();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
            Log.d("HIHI", line);
        }
        br.close();

        return sb.toString();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public class UpdateSync extends AsyncTask<Void, Void, String> {
        private ArrayList<Marker> markerlist = new ArrayList<Marker>();
        private ArrayList<Marker> markerlistcopy = new ArrayList<Marker>();
        private ArrayList<Transaction> trans;

        @Override
        protected String doInBackground(Void... voids) {
            // Do something here on the main thread
            int i = 0;
            while (true) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mMap.clear();
//                    }
//                });
                String result = null;
                try {
                    result = getJSONObjectFromURL(json_url);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                markerlistcopy = new ArrayList<Marker>();
                if (!result.isEmpty()) {
                    trans = parseJSON(result);
                    publishProgress();

                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onProgressUpdate(Void... voids) {
            //plot the point to the map
            Log.d("ProcessUpdate", "Enter");
            for (Transaction tran : trans) {
                boolean inList = false;
                double latitude_in = Double.parseDouble(tran.latitude);
                double longitude_in = Double.parseDouble(tran.longitude);
                LatLng Now = new LatLng(latitude_in, longitude_in);
                if (!markerlist.isEmpty()) {
                    for (Marker m : markerlist) {
                        String mtitle = m.getTitle();
                        Log.d("ProcessUpdate", mtitle);
                        if (mtitle.equals(tran.rand)) {
                            if(rand.indexOf("marker") != -1) {
                                m.setPosition(Now);
                                markerlistcopy.add(m);
                                markerlist.remove(m);
                                inList = true;
                                break;
                            }
                            else if(rand.indexOf("user") != -1){
                                m.setSnippet(tran.talk);
                                markerlistcopy.add(m);
                                markerlist.remove(m);
                                inList = true;
                                break;
                            }
                        }
                    }
                }
                if (!inList) {
                    String rand = tran.rand;
                    if(rand.indexOf("marker") != -1) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(Now)
                                .title(tran.rand)
                                .snippet(tran.url));
                        markerlistcopy.add(marker);
                    }
                    else if(rand.indexOf("user") != -1){
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(Now)
                                .title(tran.id)
                                .snippet(tran.talk)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bubble2)));
                        markerlistcopy.add(marker);
                        marker.showInfoWindow();
                    }

                }
            }

            for (Marker m : markerlist) {
                m.remove();
            }
            markerlist.clear();
            markerlist = markerlistcopy;
        }
    }

    public void submit(View v) {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // 設定標準為存取精確
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
        String provider = locationManager.getBestProvider(criteria, true);
        //noinspection MissingPermission
        Location location = locationManager.getLastKnownLocation("network");
        fixrand = "user"+rand;
        if(!talkadd) {
            Toast toast = Toast.makeText(this, "Add a fix marker", Toast.LENGTH_SHORT);
            toast.show();
            String method = "register";
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.executeOnExecutor(THREAD_POOL_EXECUTOR, method, id, fixrand, longitude, latitude, "");//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
            talkadd = true;
        }
        String w=edtalk.getText().toString();
        TalkTask talkTask=new TalkTask();
        talkTask.executeOnExecutor(THREAD_POOL_EXECUTOR,w,fixrand);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作

    }

    public class TalkTask extends AsyncTask<String, Void, Void>
    {
        protected Void doInBackground(String... params) //背景中做的事
        {
            Log.d("timmy", "in back");
            String reg_url = "http://140.115.158.81/project/talk.php";
            String talk_get = params[0];
            String rand_get=params[1];
            try {
                Log.d("COCO", "in back 2");
                URL url = new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                //　先取得HttpURLConnection urlConn = new URL("http://www.google.com").openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);//post的情況下需要設置DoOutput為true
                OutputStream os = httpURLConnection.getOutputStream();//java.io.OutputStream是以byte為單位的輸出串流（stream）類別，用來處理出的資料通道
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                Log.d("timmy", "in back 3");
                String data =  URLEncoder.encode("rand","UTF-8")+"="+URLEncoder.encode(rand_get,"UTF-8")+"&"+URLEncoder.encode("talk", "UTF-8") + "=" + URLEncoder.encode(talk_get, "UTF-8");
                Log.d("timmy", "in back 4");
                //&在php中表示下一個表單欄位的開始
                bufferedWriter.write(data);// //使用缓冲区中的方法将数据写入到缓冲区中。
                bufferedWriter.flush();//flush();將緩衝數據寫到文件去
                bufferedWriter.close();
                os.close();
                InputStream IS = httpURLConnection.getInputStream();
                IS.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void  onPreExecute() //AsyncTask 執行時會 第一個被呼叫的
        {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values)//會以 Void 的型態回報進度
        {
            super.onProgressUpdate(values);
        }
    }
   public void delete (View v)
    {
        talkadd = false;
        DeleteTask  deleteTask=new  DeleteTask();
        String dellive=liverand;
        String delfix=fixrand;
        deleteTask.executeOnExecutor(THREAD_POOL_EXECUTOR,dellive,delfix);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
    }
    public class DeleteTask extends AsyncTask<String, Void, Void>
    {
        protected Void doInBackground(String... params) //背景中做的事
        {
            Log.d("janice", "in back");
            String reg_url = "http://140.115.158.81/project/delete.php";
            String liverand_get = params[0];
            String fixrand_get=params[1];
            try {
                Log.d("janice", "in back 2");
                URL url = new URL(reg_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                //　先取得HttpURLConnection urlConn = new URL("http://www.google.com").openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);//post的情況下需要設置DoOutput為true
                OutputStream os = httpURLConnection.getOutputStream();//java.io.OutputStream是以byte為單位的輸出串流（stream）類別，用來處理出的資料通道
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                Log.d("timmy", "in back 3");
                String data =  URLEncoder.encode("liverand","UTF-8")+"="+URLEncoder.encode(liverand_get,"UTF-8")+"&"+URLEncoder.encode("fixrand", "UTF-8") + "=" + URLEncoder.encode(fixrand_get, "UTF-8");
                Log.d("timmy", "in back 4");
                //&在php中表示下一個表單欄位的開始
                bufferedWriter.write(data);// //使用缓冲区中的方法将数据写入到缓冲区中。
                bufferedWriter.flush();//flush();將緩衝數據寫到文件去
                bufferedWriter.close();
                os.close();
                InputStream IS = httpURLConnection.getInputStream();
                IS.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void  onPreExecute() //AsyncTask 執行時會 第一個被呼叫的
        {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values)//會以 Void 的型態回報進度
        {
            super.onProgressUpdate(values);
        }
    }
}

