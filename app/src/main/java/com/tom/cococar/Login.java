package com.tom.cococar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import static android.Manifest.permission.CAMERA;

public class Login extends AppCompatActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;
    String facebookid="";
    private Button b_enter;
    private EditText e_id;

    //權限變數
    private static final int REQUEST_LOCATION = 2;
    private static final int REQUEST_CAMERA = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //相機權限
        int permission_1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        //位置權限
        int permission_2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission_3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission_1 != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CAMERA},
                    REQUEST_CAMERA
            );
        } else {
            //已有權限，執行程式
        }

        //位置權限
        if (permission_2 != PackageManager.PERMISSION_GRANTED && permission_3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            //有權限
        }

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        b_enter= (Button) findViewById(R.id.fuckyou);
        e_id= (EditText) findViewById(R.id.enterid);
        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        callbackManager=CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            private ProfileTracker mProfileTracker;
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            // profile2 is the new profile
                            facebookid = profile2.getName();
                            Log.v("facebook - profile2", facebookid);
                            mProfileTracker.stopTracking();
                            goMainScreen();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                }
                else {
                    Profile profile1 = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile1.getName());
                    facebookid = profile1.getName();
                    goMainScreen();
                }
//                Profile profile = Profile.getCurrentProfile();
//                facebookid = profile1.getName();
                Log.d("facebook","coco" );
//                facebookid=loginResult.getAccessToken().getUserId();
                Log.i("facebook",facebookid );
//                goMainScreen();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }
    private void goMainScreen(){
        Intent intent;
        intent = new Intent(this, MapsActivity.class);
        intent.putExtra("id",facebookid);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if you don't add following block,
        // your registered `FacebookCallback` won't be called
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }
   public void enter(View v){
       String w =e_id.getText().toString();
       Intent intent;
       intent = new Intent(this, MapsActivity.class);
       intent.putExtra("id",w);
       startActivity(intent);
   }
}
