package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Feedback extends AppCompatActivity {

    private ImageView btnBack;
    private RadioButton fully_satisfied,partially_satisfied,not_satisfied;
    private RatingBar rating_bar;
    private String ratingNumber;
    float num_of_rating = 0.0f;


    private EditText feedback_remarks;
    private MaterialButton submitFeedback;

    String feedback_satisfaction_value;

    private LinearLayout feedback_layout,no_internet_layout;
    private TextView dismiss_net_layout;

    ShowNow showNow;
    private AsyncHttpClient client;

    SharedPreferences pref;
    private String token;

    String comp_id;

    private boolean flagmale = false;
    private boolean flagfemale = false;
    private boolean flagfemale3 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initViews();


        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);


        Intent intent = getIntent();
        if (intent != null){
            comp_id = intent.getStringExtra("complaint_id");
        }else {
            Log.e("intent", "null");
        }


        fully_satisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fully_satisfied.isChecked()){
                    if (!flagmale)
                    {
                        feedback_satisfaction_value = "fully-satisfied";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        fully_satisfied.setChecked(true);
                        partially_satisfied.setChecked(false);
                        not_satisfied.setChecked(false);
                        flagmale = true;
                        flagfemale = false;
                        flagfemale3 = false;
                    }
                    else {
                        feedback_satisfaction_value = "";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        flagmale = false;
                        fully_satisfied.setChecked(false);
                        partially_satisfied.setChecked(false);
                        not_satisfied.setChecked(false);
                    }
                }
            }
        });

        partially_satisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (partially_satisfied.isChecked())
                {
                    if (!flagfemale)
                    {
                        feedback_satisfaction_value = "partially-satisfied";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        partially_satisfied.setChecked(true);
                        fully_satisfied.setChecked(false);
                        not_satisfied.setChecked(false);
                        flagmale = false;
                        flagfemale = true;
                        flagfemale3 = false;
                    }
                    else {
                        feedback_satisfaction_value = "";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        flagfemale = false;
                        partially_satisfied.setChecked(false);
                        fully_satisfied.setChecked(false);
                        not_satisfied.setChecked(false);
                    }
                }
            }
        });


        not_satisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_satisfied.isChecked())
                {
                    if (!flagfemale3)
                    {
                        feedback_satisfaction_value = "not-satisfied";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        not_satisfied.setChecked(true);
                        fully_satisfied.setChecked(false);
                        partially_satisfied.setChecked(false);
                        flagmale = false;
                        flagfemale = false;
                        flagfemale3 = true;
                    }
                    else {
                        feedback_satisfaction_value = "";
                        Log.e("satisfaction: ", feedback_satisfaction_value);

                        flagfemale3 = false;
                        not_satisfied.setChecked(false);
                        fully_satisfied.setChecked(false);
                        partially_satisfied.setChecked(false);
                    }
                }
            }
        });


        rating_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                num_of_rating = rating;
                //get rating stars
                ratingNumber = String.valueOf(num_of_rating);
                Log.e("rating: ", String.valueOf(ratingNumber));
            }
        });


        //submit feedback
        submitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remarks = feedback_remarks.getText().toString().trim();

                if (TextUtils.isEmpty(feedback_satisfaction_value)){
                    Toast.makeText(Feedback.this, "Select your satisfaction level",
                            Toast.LENGTH_SHORT).show();
                }
                else if (num_of_rating == 0.0f){
                    Toast.makeText(Feedback.this, "Select rating", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(remarks)){
                    feedback_remarks.setError("Type your remarks");
                    feedback_remarks.requestFocus();
                    return;
                }
                else {
                    if (NetworkUtils.isNetworkConnected(Feedback.this)){

                        submitFeedback(remarks);
                    }
                    else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        feedback_layout.setVisibility(View.GONE);

                        dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtils.isNetworkConnected(Feedback.this))
                                {
                                    no_internet_layout.setVisibility(View.GONE);
                                    feedback_layout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    no_internet_layout.setVisibility(View.VISIBLE);
                                    feedback_layout.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                layoutTransition();
            }
        });
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

    private void submitFeedback(String remarks) {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("token", token);
        jsonParams.put("complaint_id_fk", comp_id);
        jsonParams.put("satisfaction_level", feedback_satisfaction_value);
        jsonParams.put("rating", ratingNumber);
        jsonParams.put("feedback_detail", remarks);

        Log.e("JSONPARAMS", jsonParams.toString());

        getClient().post(API_Utils.FEEDBACK, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Feedback.this);
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
                        String feedback_id = object.getString("feedback_id");
                        Log.e("feedback_id: ",feedback_id);
                        showNow.desplayPositiveToast(Feedback.this,api_res_success_msg);
                        Toast.makeText(Feedback.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
                    }
                    else if (api_res_success == 0)
                    {
                        showNow.desplayErrorToast(Feedback.this,api_res_success_msg);
                        Toast.makeText(Feedback.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
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

                        showNow.desplayErrorToast(Feedback.this,api_res_success_msg);
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
                showNow.desplayErrorToast(Feedback.this,json);
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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        fully_satisfied = findViewById(R.id.fully_satisfied);
        partially_satisfied = findViewById(R.id.partially_satisfied);
        not_satisfied = findViewById(R.id.not_satisfied);
        rating_bar = findViewById(R.id.rating_bar);
        feedback_remarks = findViewById(R.id.feedback_remarks);
        submitFeedback = findViewById(R.id.submitFeedback);
        feedback_layout = findViewById(R.id.feedback_layout);
        no_internet_layout = findViewById(R.id.no_internet_layout);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);

        showNow = new ShowNow(this);
    }
}