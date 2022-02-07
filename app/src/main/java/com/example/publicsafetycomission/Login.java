package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.ApiCallback;
import com.example.publicsafetycomission.Helpers.ApiController;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.model.ComplaintModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Login extends AppCompatActivity {

    EditText etloginusername, etloginpassword;
    Button loginsubmit;
    TextView register, forgotpass;
    String loginusername, loginpassword;

    ShowNow showNow;
    private AsyncHttpClient client;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ApiCallback apiCallback;

    private ComplaintModel complaintModel;
    private ApiController apiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String uid = pref.getString("token", "No Data");
        Log.e("SAVED_SHARED_PREF", uid);
        if (uid.equals("No Data")){
            Log.e("PREF","SHARED PREF NOT EXIST");
        }
        else {
            Log.e("PREF","SHARED PREF EXIST");
            goToNextScreen();
        }

        loginsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginusername = etloginusername.getText().toString();
                loginpassword = etloginpassword.getText().toString();
                if (loginusername.isEmpty()){
                    etloginusername.setError("this feild is required to fill");
                }
                else if (loginpassword.isEmpty()){
                    etloginpassword.setError("this feild is required to fill");
                }
                else {
                    if (NetworkUtils.isNetworkConnected(Login.this))
                    {
                        loginUser();
                    }
                    else {
                        Toast.makeText(Login.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registeration.class);
                startActivity(intent);
                layoutTransition();
            }
        });
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    private void goToNextScreen() {
        Intent intent = new Intent(Login.this,Dashboard.class);
        startActivity(intent);
        finish();
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void loginUser() {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("user_name",loginusername);
        jsonParams.put("user_password",loginpassword);

        getClient().post(API_Utils.LOGIN_URL, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Login.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    JSONObject jsonObject  = object.getJSONObject("data");

                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                    String token = jsonObject.getString("token");
                    Log.e("RESPONSE_DATA","token: " +token);

                    editor = pref.edit();
                    editor.putString("token", token);
                    Log.e("SHARED_OK", "ok");
                    editor.commit(); // commit changes

                    goToNextScreen();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSONException", "onSuccess: " + e.getMessage());

                    JSONObject object= null;
                    try {
                        object = new JSONObject(json);
                        int api_res_success = object.getInt("response");
                        String api_res_success_msg = object.getString("response_msg");
                        Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                        showNow.desplayErrorToast(Login.this,api_res_success_msg);
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
                showNow.desplayErrorToast(Login.this,json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }

    private void initViews() {
        etloginusername = findViewById(R.id.loginusername);
        etloginpassword = findViewById(R.id.loginpassword);
        loginsubmit = findViewById(R.id.loginsubmit);
        register = findViewById(R.id.register);
        forgotpass = findViewById(R.id.forgetpassword);

        showNow=new ShowNow(this);
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