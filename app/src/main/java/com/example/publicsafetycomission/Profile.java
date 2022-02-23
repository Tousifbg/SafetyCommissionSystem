package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class Profile extends AppCompatActivity {

    private ImageView btnBack;
    private TextView full_name,cnic,gender,guardian,email,district,council,address,contact,username;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ShowNow showNow;
    private AsyncHttpClient client;

    private String token;

    private LinearLayout no_internet_layout,data_layout;
    private TextView dismiss_net_layout;

    private ScrollView scrollbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);

        initVIEWS();

        scrollbar = findViewById(R.id.scrollBar);

        if (NetworkUtils.isNetworkConnected(Profile.this))
        {
            fetchProfileData();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollbar.scrollBy(0, -1);
                            scrollbar.scrollBy(0, 1);
                        }
                    });
                }
            }, 0, 1500);
        }
        else {
            no_internet_layout.setVisibility(View.VISIBLE);
            data_layout.setVisibility(View.GONE);

            dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtils.isNetworkConnected(Profile.this))
                    {
                        no_internet_layout.setVisibility(View.GONE);
                        fetchProfileData();
                        data_layout.setVisibility(View.VISIBLE);
                    }
                    else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        data_layout.setVisibility(View.GONE);
                    }
                }
            });
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                layoutTransition();
            }
        });
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void fetchProfileData() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("token",token);

        getClient().post(API_Utils.GET_PROFILE, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Profile.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    //JSONArray jsonArray = object.getJSONArray("data");
                    JSONObject jsonObject  = object.getJSONObject("user_profile");

                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                    /*for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject jsonObjectNew = jsonObject.getJSONObject(i);*/

                    int size = jsonObject.length();
                    Log.e("LISTSIZE", String.valueOf(size));
                    Log.e("jsonObject", String.valueOf(jsonObject));

                    String complainant_name = jsonObject.optString("complainant_name");
                    String complainant_cnic = jsonObject.optString("complainant_cnic");
                    String complainant_gender = jsonObject.optString("complainant_gender");
                    String complainant_guardian_name = jsonObject.optString("complainant_guardian_name");
                    String complainant_email = jsonObject.optString("complainant_email");
                    String complainant_district_id_fk = jsonObject.optString("complainant_district_id_fk");
                    String complainant_district_name = jsonObject.optString("complainant_district_name");
                    String complainant_council = jsonObject.optString("complainant_council");
                    String complainant_address = jsonObject.optString("complainant_address");
                    String complainant_contact = jsonObject.optString("complainant_contact");
                    String user_name = jsonObject.optString("user_name");

                    Log.d("received data", "complainant_name: " +complainant_name+
                            "\ncomplainant_cnic: " +complainant_cnic+
                            "\ncomplainant_gender: " +complainant_gender+ "\ncomplainant_guardian_name: "
                            +complainant_guardian_name+ "\ncomplainant_email: " +complainant_email+
                            "\ncomplainant_district_id_fk: " +complainant_district_id_fk+
                            "\ncomplainant_district_name: " +complainant_district_name+
                            "\ncomplainant_council: " +complainant_council+
                            "\ncomplainant_address: " +complainant_address+
                            "\ncomplainant_contact: " +complainant_contact+
                            "\nuser_name: " +user_name);

                    showNow.desplayPositiveToast(Profile.this,"User Information");


                    full_name.setText(complainant_name);
                    cnic.setText(complainant_cnic);
                    gender.setText(complainant_gender);
                    guardian.setText(complainant_guardian_name);
                    email.setText(complainant_email);
                    district.setText(complainant_district_name);
                    council.setText(complainant_council);
                    address.setText(complainant_address);
                    contact.setText(complainant_contact);
                    username.setText(user_name);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSONException", "onSuccess: " + e.getMessage());

                    JSONObject object= null;
                    try {
                        object = new JSONObject(json);
                        int api_res_success = object.getInt("response");
                        String api_res_success_msg = object.getString("response_msg");
                        Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                        showNow.desplayErrorToast(Profile.this,api_res_success_msg);
                        showNow.scheduleDismiss();
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("onFailure", "onSuccess: " + json);
                showNow.desplayErrorToast(Profile.this,json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }

    private void initVIEWS() {
        showNow = new ShowNow(this);

        btnBack = findViewById(R.id.btnBack);
        full_name = findViewById(R.id.full_name);
        cnic = findViewById(R.id.cnic);
        gender = findViewById(R.id.gender);
        guardian = findViewById(R.id.guardian);
        email = findViewById(R.id.email);
        district = findViewById(R.id.district);
        council = findViewById(R.id.council);
        address = findViewById(R.id.address);
        contact = findViewById(R.id.contact);
        username = findViewById(R.id.username);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);
        data_layout = findViewById(R.id.data_layout);
        no_internet_layout = findViewById(R.id.no_internet_layout);
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
}