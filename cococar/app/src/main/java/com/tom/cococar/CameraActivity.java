package com.tom.cococar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends Activity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {

    private static final String TAG = "Yasea";

    public Date d = new Date();

    boolean voice;

    ImageButton btnPublishstop = null;
    ImageButton btnPublish = null;
    ImageButton btnSwitchCamera = null;
    ImageButton btnSave = null;
    ImageButton btnSaveConti = null;
    ImageButton btnSaveStop = null;

    //聲控
    private SpeechRecognizer recognizer;
    private Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    final Handler handler = new Handler();

//    Button btnSwitchEncoder = null;

    private SharedPreferences sp;
    private String rtmpUrl = "rtmp://ossrs.net/" + getRandomAlphaString(3) + '/' + getRandomAlphaDigitString(5);
    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/" + d.toString() + ".mp4";
    String marker_id="";
    private SrsPublisher mPublisher;

    private static ExecutorService THREAD_POOL_EXECUTOR;
    static {
        THREAD_POOL_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(10);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        sp = getSharedPreferences("Yasea", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);

        Intent intent = getIntent();
        if (intent.hasExtra("marker_id")) {
            marker_id = intent.getStringExtra("marker_id");
            rtmpUrl="rtmp://140.115.158.81:1935/live/" + marker_id;
            Log.d("values", marker_id);
        }else{
            Log.d("values", "asasas");
        }
        if(intent.hasExtra("voice")){
            voice = intent.getBooleanExtra("voice",false);
            Log.d("COCO","voice"+voice);
        }
//        if (intent.hasExtra("save")) {
//            save = intent.getBooleanExtra("save", false);
//        }

        //LocationSync
        LocationSync update = new LocationSync();
        update.executeOnExecutor(THREAD_POOL_EXECUTOR,marker_id);

        // restore data.

        // initialize url.
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);
        btnPublishstop = (ImageButton) findViewById(R.id.publishstop);
        btnPublish = (ImageButton)findViewById(R.id.publish);
        btnSwitchCamera = (ImageButton) findViewById(R.id.swCam);
        btnSave = (ImageButton) findViewById(R.id.save);
        btnSaveConti = (ImageButton) findViewById(R.id.saveconti);
        btnSaveStop = (ImageButton) findViewById(R.id.savestop);
//        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.preview));
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));

        //聲控
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new CameraActivity.MyRecognizerListener());

        //language settings
        String language= Locale.getDefault().getDisplayLanguage();
        if(language.equals("中文")){
            btnPublishstop.setImageResource(R.drawable.livestopch);
            btnPublish.setImageResource(R.drawable.livecontich);
            btnSwitchCamera.setImageResource(R.drawable.switchch);
            btnSave.setImageResource(R.drawable.savech);
            btnSaveConti.setImageResource(R.drawable.savecontich);
            btnSaveStop.setImageResource(R.drawable.savestopch);
        }

        if(voice){
            recognizer.startListening(intent);
            Log.d("COCO","voice start");
        }else{
            recognizer.stopListening();
            Log.d("COCO","voice stop");
        }

        //button onclick control
        btnPublishstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //stop live streaming
                mPublisher.stopPublish();
                mPublisher.stopRecord();
                btnPublishstop.setVisibility(View.INVISIBLE);
                btnPublish.setVisibility(View.VISIBLE);
                btnSaveStop.setVisibility(View.INVISIBLE);
                btnSaveConti.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.VISIBLE);

//                btnRecord.setText("record");
//                btnSwitchEncoder.setEnabled(true);

//                if (btnPublish.getText().toString().contentEquals("publish")) {
//                    rtmpUrl = efu.getText().toString();
//                    SharedPreferences.Editor editor = sp.edit();
//                    editor.putString("rtmpUrl", rtmpUrl);
//                    editor.apply();
//
//                    mPublisher.setPreviewResolution(1280, 720);
//                    mPublisher.setOutputResolution(384, 640);
//                    mPublisher.setVideoSmoothMode();
//                    mPublisher.startPublish(rtmpUrl);
//
//                    if(save){
//                        mPublisher.startRecord(recPath);
//                    }
//
////                    if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
////                        Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
////                    } else {
////                        Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
////                    }
//                    btnPublish.setText("stop");
//                    btnSwitchEncoder.setEnabled(false);
//                }else if(btnPublish.getText().toString().contentEquals("stop")){
//                    mPublisher.stopPublish();
//                    mPublisher.stopRecord();
//                    //StopTask stop = new StopTask();
//                   // stop.executeOnExecutor(THREAD_POOL_EXECUTOR,marker_id);
//                    btnPublish.setText("publish");
//                    btnRecord.setText("record");
//                    btnSwitchEncoder.setEnabled(true);
//                    //回到地圖
//                    //Intent intent;
//                    //intent = new Intent(CameraActivity.this, MapsActivity.class);
//                    //startActivity(intent);
//                }
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtmpUrl = efu.getText().toString();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("rtmpUrl", rtmpUrl);
                editor.apply();

                mPublisher.setPreviewResolution(1280, 720);
                mPublisher.setOutputResolution(384, 640);
                mPublisher.setVideoSmoothMode();
                mPublisher.startPublish(rtmpUrl);

                btnPublish.setVisibility(View.INVISIBLE);
                btnPublishstop.setVisibility(View.VISIBLE);
//                btnSwitchEncoder.setEnabled(false);
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
                }
            }
        });

//        btnRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (btnRecord.getText().toString().contentEquals("record")) {
//                    mPublisher.startRecord(recPath);
//                    btnRecord.setText("pause");
//                } else if (btnRecord.getText().toString().contentEquals("pause")) {
//                    mPublisher.pauseRecord();
//                    btnRecord.setText("resume");
//                } else if (btnRecord.getText().toString().contentEquals("resume")) {
//                    mPublisher.resumeRecord();
//                    btnRecord.setText("pause");
//                }
//            }
//        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.startRecord(recPath);
                btnSave.setVisibility(View.INVISIBLE);
                btnSaveStop.setVisibility(View.VISIBLE);
            }
        });

        btnSaveStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.pauseRecord();
                btnSaveStop.setVisibility(View.INVISIBLE);
                btnSaveConti.setVisibility(View.VISIBLE);
            }
        });

        btnSaveConti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.resumeRecord();
                btnSaveConti.setVisibility(View.INVISIBLE);
                btnSaveStop.setVisibility(View.VISIBLE);
            }
        });

//        btnSwitchEncoder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
//                    mPublisher.swithToSoftEncoder();
//                    btnSwitchEncoder.setText("hard encoder");
//                } else if (btnSwitchEncoder.getText().toString().contentEquals("hard encoder")) {
//                    mPublisher.swithToHardEncoder();
//                    btnSwitchEncoder.setText("soft encoder");
//                }
//            }
//        });

        //publish
        rtmpUrl = efu.getText().toString();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("rtmpUrl", rtmpUrl);
        editor.apply();

        mPublisher.setPreviewResolution(1280, 720);
        mPublisher.setOutputResolution(384, 640);
        mPublisher.setVideoSmoothMode();
        mPublisher.startPublish(rtmpUrl);

//        if(save){
//            mPublisher.startRecord(recPath);
//        }

//        if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
//            Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
//        }

//        btnSwitchEncoder.setEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ImageButton btn = (ImageButton) findViewById(R.id.publish);
        btn.setEnabled(true);
        mPublisher.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
//        btnRecord.setText("record");
        btnSaveConti.setVisibility(View.INVISIBLE);
        btnSaveStop.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (btnPublishstop.getVisibility() == View.VISIBLE) {
            mPublisher.startEncode();
        }
    }

    private static String getRandomAlphaString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private static String getRandomAlphaDigitString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            btnPublishstop.setVisibility(View.INVISIBLE);
            btnPublish.setVisibility(View.VISIBLE);

            btnSaveConti.setVisibility(View.INVISIBLE);
            btnSaveStop.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.VISIBLE);
//            btnPublish.setText("publish");
//            btnRecord.setText("record");
//            btnSwitchEncoder.setEnabled(true);
        } catch (Exception e1) {
            // Ignore
        }
    }

    // Implementation of SrsRtmpListener.

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    // Implementation of SrsRecordHandler.

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    // Implementation of SrsEncodeHandler.

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    public class LocationSync extends AsyncTask<String, Void, Void> {

        LocationRequest locationRequest;
        Location location;
        String latitude;
        String longitude;

        String reg_url="http://140.115.158.81/cococar/marker";

        private void createLocationRequest() {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        private Location setupMyLocation() {
            //noinspection MissingPermission

            // 透過位置服務，取得目前裝置所在
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            // 設定標準為存取精確
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            // 向系統查詢最合適的服務提供者名稱 ( 通常也是 "gps")
            String provider = locationManager.getBestProvider(criteria, true);
            //noinspection MissingPermission
            location = locationManager.getLastKnownLocation(provider);

            return location;
        }

        @Override
        protected Void doInBackground(String... s) {
            // Do something here on the main thread
            createLocationRequest();
            while (true){
                location = setupMyLocation();
                if(location != null) {
                    latitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());

                    try {
                        URL url = new URL(reg_url);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        //　先取得HttpURLConnection urlConn = new URL("http://www.google.com").openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);//post的情況下需要設置DoOutput為true
                        OutputStream os = httpURLConnection.getOutputStream();//java.io.OutputStream是以byte為單位的輸出串流（stream）類別，用來處理出的資料通道
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        Log.d("COCO", "in back 3");
                        //InputStreamReader 與 OutputStreamWriter 類別屬於「由 byte 轉成 char」的
                        // 中繼接頭。舉例來說，在處理資料輸入時，資料輸出的 outputStream 像是小口徑的水管
                        // ，而我們希望能將小水管轉換為大口徑的 Bufferedwriter 這類大水管時，可利用 outputStreamWriter 這類轉換器，將 outStream 轉換為 Writer，
                        //更多http解說:  https://litotom.com/2016/05/11/java%E7%9A%84%E7%B6%B2%E8%B7%AF%E7%A8%8B%E5%BC%8F%E8%A8%AD%E8%A8%88/

                        String data = "{\"marker_id\":\"" + s[0] +
                                "\",  \"longitude\": \""+ longitude +
                                "\", \"latitude\": \"" + latitude +
                                "\"}";

                        Log.d("aaaaaaa",data);

//                        String data =
//                                URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longitude, "UTF-8") + "&" +
//                                        URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(latitude, "UTF-8") + "&" +
//                                        URLEncoder.encode("marker_id", "UTF-8") + "=" + URLEncoder.encode(s[0], "UTF-8");
                        Log.d("COCO", "in back 4");
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

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    //聲控
    private class MyRecognizerListener implements RecognitionListener {

        @Override
        public void onResults(Bundle results) {
            List<String> resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(resList.get(0).equals("繼續直播") || resList.get(0).equals("Resume")) {
                //TODO
                Log.d("COCO","voice resume");

                mPublisher.setPreviewResolution(1280, 720);
                mPublisher.setOutputResolution(384, 640);
                mPublisher.setVideoSmoothMode();
                mPublisher.startPublish(rtmpUrl);

                btnPublish.setVisibility(View.INVISIBLE);
                btnPublishstop.setVisibility(View.VISIBLE);

            }
            else if(resList.get(0).equals("暫停直播") || resList.get(0).equals("Stop")) {
                //TODO
                Log.d("COCO","voice Stop");
                //stop live streaming
                mPublisher.stopPublish();
                mPublisher.stopRecord();
                btnPublishstop.setVisibility(View.INVISIBLE);
                btnPublish.setVisibility(View.VISIBLE);
                btnSaveStop.setVisibility(View.INVISIBLE);
                btnSaveConti.setVisibility(View.INVISIBLE);
                btnSave.setVisibility(View.VISIBLE);

            }
            else if(resList.get(0).equals("儲存影片") || resList.get(0).equals("Save")) {
                //TODO
                Log.d("COCO","voice Save");
                mPublisher.startRecord(recPath);
                btnSave.setVisibility(View.INVISIBLE);
                btnSaveStop.setVisibility(View.VISIBLE);

            }
            else if(resList.get(0).equals("繼續儲存") || resList.get(0).equals("Resume Saving")) {
                //TODO
                Log.d("COCO","voice Resume Saving");
                mPublisher.resumeRecord();
                btnSaveConti.setVisibility(View.INVISIBLE);
                btnSaveStop.setVisibility(View.VISIBLE);
            }
            else if(resList.get(0).equals("暫停儲存") || resList.get(0).equals("Pause Saving")) {
                //TODO
                Log.d("COCO","voice Pause Saving");
                mPublisher.pauseRecord();
                btnSaveStop.setVisibility(View.INVISIBLE);
                btnSaveConti.setVisibility(View.VISIBLE);

            }
            else if(resList.get(0).equals("轉換鏡頭") || resList.get(0).equals("Switch")) {
                //TODO
                Log.d("COCO","voice Switch");
                if (Camera.getNumberOfCameras() > 0) {
                    mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
                }

            }
            else
                Log.d("COCO","voice nnnnnnnnnn");
        }

        @Override
        public void onError(int error) {
            Log.d("COCO",error+" voice ");
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
            Log.d("COCO","voice runnable");
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

}

