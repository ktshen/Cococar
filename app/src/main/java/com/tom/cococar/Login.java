package com.tom.cococar;

import android.content.Intent;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class Login extends AppCompatActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;
    String facebookid="";
    private Button b_enter;
    private EditText e_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        b_enter= (Button) findViewById(R.id.fuckyou);
        e_id= (EditText) findViewById(R.id.enterid);
        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        callbackManager=CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile=Profile.getCurrentProfile();
                facebookid=profile.getName();
                Log.d("facebook","coco" );
                //facebookid=loginResult.getAccessToken().getUserId();
                Log.i("facebook",facebookid );
                goMainScreen();
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
   public void fuck(View v){
       String w =e_id.getText().toString();
       Intent intent;
       intent = new Intent(this, MapsActivity.class);
       intent.putExtra("id",w);
       startActivity(intent);
   }
}
