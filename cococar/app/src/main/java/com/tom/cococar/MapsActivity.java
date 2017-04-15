package com.tom.cococar;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import static android.Manifest.permission.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static String url = "rtmp://140.115.158.81:1935/live/";
    public static String json_url="http://140.115.158.81/cococar/marker";
    private static final int REQUEST_LOCATION = 2;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    String id = "id";
    String rand = Integer.toString((int) (Math.random()*10000));
    boolean talkadd = false;
    boolean voiceopen = false;
    boolean livestart = false;
    private EditText edtalk;
    private EditText e_address;
    private ImageButton submit;
    private ImageButton delete;
    private ImageButton startlive;
    private ImageButton save;
    private ImageButton voice;
    private ImageButton logout;
    private ImageButton search;
    String liverand="";
    String fixrand="";
    String strAddress="";

    //聲控
    private SpeechRecognizer recognizer;
    private Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    final Handler handler = new Handler();

    private static ExecutorService THREAD_POOL_EXECUTOR;
    static {
        THREAD_POOL_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(10);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language= Locale.getDefault().getDisplayLanguage();
        Log.d("language",language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent=getIntent();
        id=intent.getStringExtra("id");
        e_address= (EditText) findViewById(R.id.address);
        edtalk = (EditText) findViewById(R.id.ed_talk);
        startlive= (ImageButton) findViewById(R.id.startlive);
        voice= (ImageButton) findViewById(R.id.voice);
        logout= (ImageButton) findViewById(R.id.logout);
        if(language.equals("中文")){
            startlive.setImageResource(R.drawable.startlivech);
            voice.setImageResource(R.drawable.voicech);
            logout.setImageResource(R.drawable.logoutch);
        }


        //聲控
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new MyRecognizerListener());

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
        LiveTask live = new LiveTask();
        live.executeOnExecutor(THREAD_POOL_EXECUTOR);
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
        //位置權限
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
                if(marker.getTitle().indexOf("user")!=-1) {
                    Intent intent = new Intent(MapsActivity.this, PlayerActivity.class);
                    intent.putExtra("url", "rtmp://140.115.158.81:1935/live/"+marker.getTitle());
                    startActivity(intent);
                }
                return false;
            }
        });
    }

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
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                new LatLng(location.getLatitude(), location.getLongitude())
//                                , 15));
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
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(location.getLatitude(), location.getLongitude())
//                    , 15));
        }
    }

    public void search(View view){
        if(!e_address.getText().toString().isEmpty()) {
            strAddress = e_address.getText().toString();
            try {
                Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
                List<Address> addressLocation = geoCoder.getFromLocationName(strAddress, 1);
                double latitude = addressLocation.get(0).getLatitude();
                double longitude = addressLocation.get(0).getLongitude();
                Log.d("經度", "=" + longitude);
                Log.d("緯度", "=" + latitude);
                LatLng Search = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Search, 18));
            } catch (IOException e) {
            }
            e_address.getText().clear();
        }
    }

    //logout
    public void log_out(View view){
        if(AccessToken.getCurrentAccessToken() != null){
            //登出FB
            id = "id";
            LoginManager.getInstance().logOut();
            Toast toast = Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT);
            toast.show();
            Intent intent;
            intent = new Intent(this, Login.class);
            startActivity(intent);
        }else{
            //跳回登入頁面
            id = "id";
            Toast toast = Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT);
            toast.show();
            Intent intent;
            intent = new Intent(this, Login.class);
            startActivity(intent);
        }
    }

    public void voice(View view){
        if(!voiceopen){
            //開啟聲控
            voiceopen = true;
            Toast toast = Toast.makeText(this, "Open voice control", Toast.LENGTH_SHORT);
            toast.show();
            recognizer.startListening(intent);
        }else{
            //關閉聲控
            voiceopen = false;
            Toast toast = Toast.makeText(this, "Close voice control", Toast.LENGTH_SHORT);
            toast.show();
            recognizer.stopListening();
        }
    }


    public void live_button(View view) {
        live();
    }

    //解析JSON資料
    private ArrayList<Transaction> parseJSON(String s) {
        ArrayList<Transaction> trans = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0; i < array.length(); i++) {
                Log.d("COCO", "COCO3");
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("user_id");
                String rand = obj.getString("marker_id");
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
                Log.d("ncu",tran.latitude);
                Log.d("ncu",tran.longitude);
                LatLng Now = new LatLng(latitude_in, longitude_in);
                if (!markerlist.isEmpty()) {
                    for (Marker m : markerlist) {
                        String mtitle = m.getTitle();
                        Log.d("ProcessUpdate", mtitle);
                        if (mtitle.equals(tran.rand)) {
                            if(rand.indexOf("user") != -1) {
                                m.setPosition(Now);
                                markerlistcopy.add(m);
                                markerlist.remove(m);
                                inList = true;
                                break;
                            }
                            else if(rand.indexOf("marker") != -1){
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
                    if(rand.indexOf("user") != -1) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(Now)
                                .title(tran.rand)
                                .snippet(tran.url));
                        markerlistcopy.add(marker);
                        marker.hideInfoWindow();
                    }
                    else if(rand.indexOf("marker") != -1){
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
        fixrand = "marker-"+rand;
        Log.d("qwers", ""+talkadd);
        if(!talkadd) {
            Toast toast = Toast.makeText(this, "Add a fix marker", Toast.LENGTH_SHORT);
            toast.show();
            String method = "register";
            Log.d("qwerstimmy", ""+talkadd);
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            BackgroundTask backgroundTask = new BackgroundTask(this);
            Log.d("qwer", id);
            Log.d("qwer", longitude);
            Log.d("qwer", latitude);
            if(id.equals(""))
            {
                id="client";
            }
            backgroundTask.executeOnExecutor(THREAD_POOL_EXECUTOR, method, id, fixrand, longitude, latitude, "",edtalk.getText().toString());//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
            talkadd = true;
        }
        String w=edtalk.getText().toString();
        TalkTask talkTask=new TalkTask();
        talkTask.executeOnExecutor(THREAD_POOL_EXECUTOR,w,fixrand);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
        edtalk.getText().clear();
    }

    public void delete (View v)
    {
        talkadd = false;
        DeleteTask  deleteTask=new  DeleteTask();
        String dellive=liverand;
        String delfix="marker-"+rand;
        deleteTask.executeOnExecutor(THREAD_POOL_EXECUTOR,dellive,delfix);//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
        rand = Integer.toString((int) (Math.random()*10000));
    }

    public class LiveTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("COCO","livestart001");
            while (true){
                if(livestart) {
                    Log.d("COCO", "livestart");
                    livestart = false;
                    publishProgress();
                }
            }
        }

        protected void onProgressUpdate(Void... voids){
            Log.d("COCO","livestart2");
            live();
        }
    }

    //聲控
    private class MyRecognizerListener implements RecognitionListener {

        @Override
        public void onResults(Bundle results) {
            List <String>resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(resList.get(0).equals("開啟直播") || resList.get(0).equals("start live")) {
                //TODO
                Log.d("COCO","livestart000");
                livestart = true;
            }
        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recognizer.startListening(intent);
                }
            }, 500);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }

    public void live(){
        Log.d("COCO","livestart3");
        String url1 = "rtmp://140.115.158.81:1935/live/";
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
//        if (location != null) {
//            LatLng Now = new LatLng(location.getLatitude(),location.getLongitude());
//            mMap.addMarker(new MarkerOptions()
//                    .position(Now)
//                    .title("Current Position"));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Now, 12));
//        }
        liverand = "user-"+rand;
        url1=url1+liverand;
        Log.d("bg", url1);
        String method="register";
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        BackgroundTask backgroundTask=new BackgroundTask(this);
        backgroundTask.executeOnExecutor(THREAD_POOL_EXECUTOR,method,id,liverand,longitude,latitude,url1," ");//AsyncTask 提供了 execute 方法來執行(觸發)非同步工作
        Log.d("janices", "in back 2");
        //連結到camera

        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("marker_id", liverand);
        Log.d("janices", "in back 3");
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d("bag", "url1");
        liverand = "user-"+rand;
        StopTask stop = new StopTask();
        stop.executeOnExecutor(THREAD_POOL_EXECUTOR,liverand);
    }

}

