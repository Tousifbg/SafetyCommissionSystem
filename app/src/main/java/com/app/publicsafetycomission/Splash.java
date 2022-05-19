package com.app.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;


public class Splash extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String uid = pref.getString("token", "No Data");
        Log.e("SAVED_SHARED_PREF", uid);
        if (uid.equals("No Data")){
            Log.e("PREF","SHARED PREF NOT EXIST");
        }
        else {
            Log.e("PREF","SHARED PREF EXIST");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Splash.this,Dashboard.class);
                    startActivity(intent);
                    finish();
                    layoutTransition();
                }
            },4000);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash.this,Login.class);
                startActivity(intent);
                finish();
                layoutTransition();
            }
        },4000);
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }
}