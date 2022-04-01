package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends AppCompatActivity {

    CardView complaintregister,viewcomplaints,help_btn,about_btn;

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

    private CircleImageView profile_image;
    private TextView profile_name,profile_contact,profile_address;

    private ImageView ic_homeimg,ic_settingsimg,ic_exitimg;

    private LinearLayout linearLayout10,linearLayout4;

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


        ic_settingsimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, Profile.class);
                startActivity(intent);
                layoutTransition();
            }
        });

        ic_exitimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogNow();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchProfileData();
    }

    private void fetchProfileData() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("token",token);

        getClient().post(API_Utils.GET_PROFILE, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Dashboard.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    JSONObject jsonObject  = object.getJSONObject("user_profile");

                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));


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
                    String user_avatar = jsonObject.optString("user_avatar");


                    Log.d("received data", "complainant_name: " +complainant_name+
                            "\ncomplainant_cnic: " +complainant_cnic+
                            "\ncomplainant_gender: " +complainant_gender+ "\ncomplainant_guardian_name: "
                            +complainant_guardian_name+ "\ncomplainant_email: " +complainant_email+
                            "\ncomplainant_district_id_fk: " +complainant_district_id_fk+
                            "\ncomplainant_district_name: " +complainant_district_name+
                            "\ncomplainant_council: " +complainant_council+
                            "\ncomplainant_address: " +complainant_address+
                            "\ncomplainant_contact: " +complainant_contact+
                            "\nuser_name: " +user_name+
                            "\nuser_avatar: " +user_avatar);

                    String base_url_img = "https://ppsc.kp.gov.pk/assets/images/";

                    String full_url_img = (base_url_img + user_avatar);
                    Log.d("full_url_img: ", full_url_img);


                    profile_name.setText(complainant_name);
                    profile_contact.setText(complainant_contact);
                    profile_address.setText(complainant_address);

                    Glide.with(Dashboard.this)
                            .load(full_url_img)
                            .centerCrop()
                            .placeholder(R.drawable.placeholderrr)
                            .into(profile_image);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSONException", "onSuccess: " + e.getMessage());

                    JSONObject object= null;
                    try {
                        object = new JSONObject(json);
                        int api_res_success = object.getInt("response");
                        String api_res_success_msg = object.getString("response_msg");
                        Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                        showNow.desplayErrorToast(Dashboard.this,api_res_success_msg);
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
                showNow.desplayErrorToast(Dashboard.this,json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }

    private void showDialogNow() {
        final android.app.AlertDialog alert_dialog;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.setting_item, null);
        builder.setView(view);

        TextView btnCancelAction = view.findViewById(R.id.btnCancelAction);
        TextView btnLogoutNow = view.findViewById(R.id.btnLogoutNow);

        alert_dialog = builder.create();
        alert_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alert_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alert_dialog.show();


        btnLogoutNow.setOnClickListener(new View.OnClickListener() {
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

        btnCancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert_dialog.dismiss();
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
        profile_image = findViewById(R.id.profile_image);
        //logout = findViewById(R.id.buttonLogout);
        ic_homeimg = findViewById(R.id.home_icon);
        ic_settingsimg = findViewById(R.id.setting_icon);
        ic_exitimg = findViewById(R.id.exit_icon);

        profile_name = findViewById(R.id.profile_name);
        profile_address = findViewById(R.id.profile_address);
        profile_contact = findViewById(R.id.profile_contact);

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