package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Registeration extends AppCompatActivity {

    EditText usernamereg, passwordreg, editTextCountryCode, phonereg;
    Button btnregister;
    TextView tvlogin;
    String username, password, phone, code;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ShowNow showNow;
    private AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        initViews();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernamereg.getText().toString();
                code = editTextCountryCode.getText().toString().trim();
                phone = phonereg.getText().toString();
                password = passwordreg.getText().toString();

                if (username.isEmpty()) {
                    usernamereg.setError("username is required");
                } else if (password.isEmpty()) {
                    passwordreg.setError("password is required");
                } else if (phone.isEmpty() || phone.length() < 10) {
                    phonereg.setError("Valid number is required");
                }
                else {
                    if (NetworkUtils.isNetworkConnected(Registeration.this))
                    {
                        registerMe();
                    }
                    else {
                        Toast.makeText(Registeration.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(Registeration.this, Login.class);
                startActivity(n);
                layoutTransition();
            }
        });
    }

    private void registerMe() {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("user_name",username);
        jsonParams.put("user_password",password);
        jsonParams.put("user_contact",phone);

        getClient().post(API_Utils.REGISTERATION, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Registeration.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    JSONObject jsonObject  = object.getJSONObject("data");
                    String user_id = jsonObject.getString("user_id");
                    String complainant_id = jsonObject.getString("complainant_id");
                    Log.e("RESPONSE_DATA","user_id: " +user_id+ "/ncomplainant_id: " +complainant_id);

                    showNow.desplayPositiveToast(Registeration.this,"You are registered");
                    Toast.makeText(Registeration.this, "You are registered",
                            Toast.LENGTH_SHORT).show();

                    String phoneNumber = code + phone;
                    // save phone number
                    editor = pref.edit();
                    editor.putString("phoneNumber", phoneNumber);
                    Log.e("SHARED_OK", phoneNumber);
                    editor.commit();

                    Intent intent = new Intent(Registeration.this, VerifyPhoneActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                    Log.d("userdata", "onClick: " + username + password);

                    layoutTransition();

                } catch (JSONException e) {
                    e.printStackTrace();
                    showNow.desplayErrorToast(Registeration.this,
                            "This user_name or user_contact already exists");
                    showNow.scheduleDismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                showNow.desplayErrorToast(Registeration.this,json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
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

    private void initViews() {
        usernamereg = findViewById(R.id.etuserreg);
        passwordreg = findViewById(R.id.etpasswordreg);
        editTextCountryCode = findViewById(R.id.editTextCountryCode);
        phonereg = findViewById(R.id.etphonereg);
        btnregister = findViewById(R.id.btnregister);
        tvlogin = findViewById(R.id.tvlogin);
        showNow=new ShowNow(this);
    }
}