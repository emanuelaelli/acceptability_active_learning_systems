package com.alesp.feedbackapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;


public class SplashActivity extends Activity {

    //Definisco variabili
    TextView title;
    TextView connection;
    static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.putString("Alice","");
        ed.putString("Bob","");
        ed.putString("Chris","");
        ed.putString("David","");
        ed.commit();





        //Collego cose
        title = (TextView) findViewById(R.id.title);
        connection = (TextView) findViewById(R.id.connection);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();

            }
        }, SPLASH_TIME_OUT);


    }



}
