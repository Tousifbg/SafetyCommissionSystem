package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.ApiCallback;
import com.example.publicsafetycomission.Helpers.ApiController;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.databaseRef.DBHelperClass;
import com.example.publicsafetycomission.model.ComplaintModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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

    ArrayList<HashMap<String,String>> getCategories = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> getDistricts = new ArrayList<HashMap<String, String>>();
    DBHelperClass dbHelperClass;

    String token;
    private String cat_id, cat_name;
    private String dist_id, dist_name;

    private LinearLayout no_internet_layout,login_layout;
    private TextView dismiss_net_layout;

    private String email,verifyCode, newPass;

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

        //get districts from API
        getDistricts = dbHelperClass.getDistrictData();
        if (getDistricts.size() < 2){
            fetchDistrictsFromAPI();
        }  else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("DATA","ALL districts preloaded successfully");
                }
            });
        }

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog();
                //Toast.makeText(Login.this, "Coming soon...", Toast.LENGTH_SHORT).show();
            }
        });

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
                        no_internet_layout.setVisibility(View.VISIBLE);
                        login_layout.setVisibility(View.GONE);

                        dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtils.isNetworkConnected(Login.this))
                                {
                                    no_internet_layout.setVisibility(View.GONE);
                                    login_layout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    no_internet_layout.setVisibility(View.VISIBLE);
                                    login_layout.setVisibility(View.GONE);
                                }
                            }
                        });
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

    private void forgotPasswordDialog() {
        final android.app.AlertDialog alert_dialog;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.forgotpassword_dialog, null);
        builder.setView(view);
        final LinearLayout password_layout = view.findViewById(R.id.password_layout);
        final LinearLayout verification_layout = view.findViewById(R.id.verification_layout);
        final LinearLayout email_layout = view.findViewById(R.id.email_layout);
        final EditText email_address = view.findViewById(R.id.email_address);
        final EditText verification_code = view.findViewById(R.id.verification_code);
        final EditText new_password = view.findViewById(R.id.new_password);
        TextView btn_get_Code = view.findViewById(R.id.btn_get_Code);
        TextView btn_change_pass = view.findViewById(R.id.change_pass);
        TextView tv_no = view.findViewById(R.id.tv_no);

        alert_dialog = builder.create();
        alert_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alert_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alert_dialog.show();

        btn_get_Code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = email_address.getText().toString();
                if (TextUtils.isEmpty(email))
                {
                    email_address.setError("Email is required");
                    Toast.makeText(Login.this, "Email is required", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (NetworkUtils.isNetworkConnected(Login.this)){
                        getCodeFromAPI(btn_get_Code,btn_change_pass,
                                verification_layout,password_layout);
                    }
                    else {
                        alert_dialog.dismiss();
                        no_internet_layout.setVisibility(View.VISIBLE);
                        login_layout.setVisibility(View.GONE);

                        dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtils.isNetworkConnected(Login.this))
                                {
                                    no_internet_layout.setVisibility(View.GONE);
                                    login_layout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    no_internet_layout.setVisibility(View.VISIBLE);
                                    login_layout.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            }
        });

            btn_change_pass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = email_address.getText().toString();
                    verifyCode = verification_code.getText().toString();
                    newPass = new_password.getText().toString();
                    if (TextUtils.isEmpty(email))
                    {
                        email_address.setError("Email is required");
                        Toast.makeText(Login.this, "Email is required", Toast.LENGTH_SHORT).show();
                    }
                    else if (TextUtils.isEmpty(verifyCode))
                    {
                        verification_code.setError("Verification Code is required");
                        Toast.makeText(Login.this, "Verification Code is required", Toast.LENGTH_SHORT).show();
                    }
                    else if (TextUtils.isEmpty(newPass))
                    {
                        new_password.setError("Password is required");
                        Toast.makeText(Login.this, "Password is required", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (NetworkUtils.isNetworkConnected(Login.this))
                        {
                            resetPassword(alert_dialog,email,verifyCode,newPass);
                        }
                        else {
                            alert_dialog.dismiss();
                            no_internet_layout.setVisibility(View.VISIBLE);
                            login_layout.setVisibility(View.GONE);

                            dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (NetworkUtils.isNetworkConnected(Login.this))
                                    {
                                        no_internet_layout.setVisibility(View.GONE);
                                        login_layout.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        no_internet_layout.setVisibility(View.VISIBLE);
                                        login_layout.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                }
            });

        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert_dialog.dismiss();
            }
        });
    }

    private void resetPassword(AlertDialog alert_dialogg, String emaill, String verifyCodee, String newPasss) {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("verification_source", "email");
        jsonParams.put("user_email", emaill);
        jsonParams.put("vcode", verifyCodee);
        jsonParams.put("new_password", newPasss);

        Log.e("JSONPARAMS", jsonParams.toString());

        getClient().post(API_Utils.FORGOT_PASSWORD_RESET, jsonParams, new AsyncHttpResponseHandler() {

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
                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));
                    Log.e("api_res_success",String.valueOf(api_res_success));

                    if (api_res_success == 1)
                    {
                        alert_dialogg.dismiss();
                        showNow.desplayPositiveToast(Login.this,api_res_success_msg);
                        Toast.makeText(Login.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
                    }
                    else if (api_res_success == 0)
                    {
                        showNow.desplayErrorToast(Login.this,api_res_success_msg);
                        Toast.makeText(Login.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
                    }

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

    private void getCodeFromAPI(TextView btn_get_Code, TextView btn_change_pass,
                                LinearLayout verification_layout, LinearLayout password_layout) {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("verification_source", "email");
        jsonParams.put("user_email", email);

        Log.e("JSONPARAMS", jsonParams.toString());

        getClient().post(API_Utils.FORGOT_PASSWORD_VERIFY, jsonParams, new AsyncHttpResponseHandler() {

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
                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));
                    Log.e("api_res_success",String.valueOf(api_res_success));

                    if (api_res_success == 1)
                    {
                        showNow.desplayPositiveToast(Login.this,api_res_success_msg);
                        Toast.makeText(Login.this, api_res_success_msg, Toast.LENGTH_SHORT).show();

                        verification_layout.setVisibility(View.VISIBLE);
                        password_layout.setVisibility(View.VISIBLE);
                        btn_get_Code.setVisibility(View.GONE);
                        btn_change_pass.setVisibility(View.VISIBLE);
                    }
                    else if (api_res_success == 0)
                    {
                        showNow.desplayErrorToast(Login.this,api_res_success_msg);
                        Toast.makeText(Login.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
                    }

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

                    token = jsonObject.getString("token");
                    Log.e("RESPONSE_DATA","token: " +token);

                    editor = pref.edit();
                    editor.putString("token", token);
                    Log.e("SHARED_OK", "ok");
                    editor.commit(); // commit changes

                    //get categories from API
                    getCategories = dbHelperClass.getCategoriesData();
                    if (getCategories.size() < 2){
                        fetchCategoriesFromAPI();
                    }  else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("DATA","ALL categories preloaded successfully");
                            }
                        });
                    }

                    showNow.desplayPositiveToast(Login.this,"You are logged in");
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

        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);
        no_internet_layout = findViewById(R.id.no_internet_layout);
        login_layout = findViewById(R.id.login_layout);

        showNow=new ShowNow(this);
        dbHelperClass = new DBHelperClass(this);
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

    private void fetchCategoriesFromAPI() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("token",token);


        Log.e("JSON_DATA_POST", String.valueOf(jsonParams));

        getClient().post(API_Utils.CATEGORIES, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.d("RESPONSE", "onSuccess: " + json);

                try {
                    JSONObject object=new JSONObject(json);
                    JSONArray jsonArray = object.getJSONArray("complaint_categories");
                    dbHelperClass.deleteCategoriesTables();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        cat_id = jsonObjectNew.getString("complaint_category_id");
                        cat_name = jsonObjectNew.getString("complaint_category_name");
                        dbHelperClass.addCategories(cat_id, cat_name);
                        Log.d("RESPONSE_DATA","cat_id: " +cat_id+ "\ncat_name: " +cat_name);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(Login.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }

    private void fetchDistrictsFromAPI() {
        RequestParams jsonParams = new RequestParams();

        getClient().post(API_Utils.DISTRICTS, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.d("RESPONSE", "onSuccess: " + json);

                try {
                    JSONObject object=new JSONObject(json);
                    JSONArray jsonArray = object.getJSONArray("districts");
                    dbHelperClass.deleteDistrictTables();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        dist_id = jsonObjectNew.getString("district_id");
                        dist_name = jsonObjectNew.getString("district_name");
                        String district_status = jsonObjectNew.getString("district_status");
                        dbHelperClass.addDistricts(dist_id, dist_name);
                        Log.d("RESPONSE_DATA","dist_id: " +dist_id+ "\ndist_name: " +dist_name+ "\ndistrict_status: " +district_status);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(Login.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }
}