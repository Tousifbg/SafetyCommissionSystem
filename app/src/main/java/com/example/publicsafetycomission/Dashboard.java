package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.databaseRef.DBHelperClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class Dashboard extends AppCompatActivity {

    CardView complaintregister,viewcomplaints,help_btn,about_btn;
    ImageView buttonLogout,profile;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<HashMap<String,String>> getCategories = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> getDistricts = new ArrayList<HashMap<String, String>>();
    DBHelperClass dbHelperClass;

    private String cat_id, cat_name;
    private String dist_id, dist_name;
    private String token;

    AsyncHttpClient client;
    ShowNow showNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initVIEWs();

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);

        complaintregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, ComplaintArea.class);
                startActivity(intent);
                layoutTransition();

            }
        });

        viewcomplaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, ViewComplaints.class);
                startActivity(intent);
                layoutTransition();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = pref.edit();

                for (String key : pref.getAll().keySet())
                {
                    if (key.contains("token"))
                    {
                        editor.remove(key);
                        editor.commit();
                    }
                }

                Toast.makeText(Dashboard.this, "You are logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Dashboard.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                layoutTransition();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, Profile.class);
                startActivity(intent);
                layoutTransition();
            }
        });
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private AsyncHttpClient getClient(){
        if (client == null)
        {
            client = new AsyncHttpClient();
            client.setTimeout(46000);
            client.setConnectTimeout(40000); // default is 10 seconds, minimum is 1 second
            client.setResponseTimeout(40000);
        }

        return client;
    }

    private void initVIEWs() {
        complaintregister = findViewById(R.id.complaintregister);
        viewcomplaints = findViewById(R.id.viewcomplaints);
        help_btn = findViewById(R.id.help_btn);
        about_btn = findViewById(R.id.about_btn);
        buttonLogout = findViewById(R.id.buttonLogout);
        profile = findViewById(R.id.profile);

        dbHelperClass = new DBHelperClass(this);
        showNow=new ShowNow(this);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Do you want to Exit? ");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Dashboard.super.onBackPressed();
            }
        });
        builder.show();
    }
}