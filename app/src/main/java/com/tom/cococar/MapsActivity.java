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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static ExecutorService THREAD_POOL_EXECUTOR;
    static {
        THREAD_POOL_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(10);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
        int random = (int) (Math.random()*10000);
        String rand = Integer.toString(random);
        url=url+rand;
        Log.d("bg", url);
        String method="register";
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        BackgroundTask backgroundTask=new BackgroundTask(this);
        backgroundTask.executeOnExecutor(THREAD_POOL_EXECUTOR,method,id,rand,longitude,latitude,url);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作

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
                Log.d("COCO", id + "/" + rand + "/" + latitude + "/" + longitude + "/" + url);
                Transaction t = new Transaction(id, rand, latitude, longitude, url);
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
                            m.setPosition(Now);
                            markerlistcopy.add(m);
                            markerlist.remove(m);
                            inList = true;
                            break;
                        }
                    }
                }
                if (!inList) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(Now)
                            .title(tran.rand)
                            .snippet(tran.url));
                    markerlistcopy.add(marker);
                }
            }

            for (Marker m : markerlist) {
                m.remove();
            }
            markerlist.clear();
            markerlist = markerlistcopy;
        }
    }
}

