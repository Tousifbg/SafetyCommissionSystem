package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.adapters.MyRegisteredComplaintsAdapter;
import com.example.publicsafetycomission.model.RegisteredComplaintModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class ViewComplaints extends AppCompatActivity {

    private RecyclerView my_complaint_list;
    private AsyncHttpClient client;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ShowNow showNow;
    String token;

    private MyRegisteredComplaintsAdapter adapter;
    ArrayList<RegisteredComplaintModel> registeredComplaintModels = new ArrayList<>();

    private LinearLayout no_internet_layout;
    private TextView dismiss_net_layout;
    private LottieAnimationView animationView2,animationView;

    private FloatingActionButton add_complaints;
    private ImageView btnBack;
    private TextView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_complaints);

        initViews();

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token", token);


        my_complaint_list.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        my_complaint_list.setLayoutManager(layoutManager);

        if (NetworkUtils.isNetworkConnected(ViewComplaints.this))
        {
            fetchMyComplaints();
        }
        else {
            no_internet_layout.setVisibility(View.VISIBLE);
            my_complaint_list.setVisibility(View.GONE);

            dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtils.isNetworkConnected(ViewComplaints.this))
                    {
                        no_internet_layout.setVisibility(View.GONE);
                        my_complaint_list.setVisibility(View.VISIBLE);
                    }
                    else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        animationView.setVisibility(View.VISIBLE);
                        dismiss_net_layout.setVisibility(View.VISIBLE);
                        animationView2.setVisibility(View.GONE);
                        my_complaint_list.setVisibility(View.GONE);
                    }
                }
            });
        }

     /*   add_complaints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewComplaints.this, ComplaintArea.class);
                startActivity(intent);
                layoutTransition();
            }
        });*/

   /*     logout.setOnClickListener(new View.OnClickListener() {
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

                Toast.makeText(ViewComplaints.this, "You are logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewComplaints.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                layoutTransition();
            }
        });*/

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                layoutTransition();
            }
        });
    }

    private void fetchMyComplaints() {
        RequestParams jsonParams = new RequestParams();
        jsonParams.put("token", token);
        Log.d("PARAMS", jsonParams.toString());

        getClient().post(API_Utils.GET_COMPLAINTS, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(ViewComplaints.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.d("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object = new JSONObject(json);
                    JSONArray jsonArray = object.getJSONArray("complaints");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        int size = jsonArray.length();
                        Log.d("LISTSIZE", String.valueOf(size));
                        String complaint_id = jsonObjectNew.getString("complaint_id");
                        String complaint_source = jsonObjectNew.getString("complaint_source");
                        String complaint_council = jsonObjectNew.getString("complaint_council");
                        String complaint_detail = jsonObjectNew.getString("complaint_detail");
                        String complaint_entry_timestamp = jsonObjectNew.getString("complaint_entry_timestamp");
                        String complainant_name = jsonObjectNew.getString("complainant_name");
                        String district_name = jsonObjectNew.getString("district_name");
                        String complaint_status_title = jsonObjectNew.getString("complaint_status_title");
                        String complaint_status_color = jsonObjectNew.getString("complaint_status_color");
                        String complaint_category_name = jsonObjectNew.getString("complaint_category_name");

                        RegisteredComplaintModel model = new RegisteredComplaintModel(complaint_id,complaint_source,
                                complaint_council, complaint_detail, complaint_entry_timestamp,complainant_name,
                                district_name, complaint_status_title,complaint_status_color,complaint_category_name);
                        registeredComplaintModels.add(model);

                        Log.d("RESPONSE_DATA", "complaint_id: " +complaint_id+
                                "\ncomplaint_source: " +complaint_source+
                                "\ncomplaint_council: " +complaint_council+
                                "\ncomplaint_detail: " +complaint_detail+
                                "\ncomplaint_entry_timestamp: " +complaint_entry_timestamp+
                                "\ncomplainant_name: " +complainant_name+
                                "\ndistrict_name: " +district_name+
                                "\ncomplaint_status_title: " +complaint_status_title+
                                "\ncomplaint_status_color: " +complaint_status_color+
                                "\ncomplaint_category_name: " +complaint_category_name);
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new MyRegisteredComplaintsAdapter(ViewComplaints.this,
                                        registeredComplaintModels);
                                my_complaint_list.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                showNow.desplayPositiveToast(ViewComplaints.this,"Your Complaints");
                            }
                        });
                    } catch (Exception e) {
                        Log.e("ERRR", e.getMessage());
                        String error = e.getMessage().toString();
                        Toast.makeText(ViewComplaints.this,error,
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewComplaints.this, "JSONException: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    no_internet_layout.setVisibility(View.VISIBLE);
                    my_complaint_list.setVisibility(View.GONE);
                    animationView.setVisibility(View.GONE);
                    dismiss_net_layout.setVisibility(View.GONE);
                    animationView2.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(ViewComplaints.this, "ERROR: " + json, Toast.LENGTH_LONG).show();
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
            }
        });
    }

    private AsyncHttpClient getClient() {
        if (client == null) {
            client = new AsyncHttpClient();
            client.setTimeout(46000);
            client.setConnectTimeout(40000); // default is 10 seconds, minimum is 1 second
            client.setResponseTimeout(40000);
        }
        return client;
    }

    private void initViews() {
        my_complaint_list = findViewById(R.id.my_complaint_list);
        no_internet_layout = findViewById(R.id.no_internet_layout);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);
        animationView2 = findViewById(R.id.animationView2);
        animationView = findViewById(R.id.animationView);
        add_complaints = findViewById(R.id.add_complaints);
        btnBack = findViewById(R.id.btnBack);
        logout = findViewById(R.id.buttonLogout);
        showNow = new ShowNow(this);
    }

    public void withDrawComplaintDialog(String comp_id){

        final android.app.AlertDialog alert_dialog;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.complaint_withdrawn_dialog, null);
        builder.setView(view);
        final EditText withdraw_edt = view.findViewById(R.id.withdraw_edt);
        TextView btn_withdraw = view.findViewById(R.id.btn_withdraw);
        TextView tv_no = view.findViewById(R.id.tv_no);

        alert_dialog = builder.create();
        alert_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alert_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alert_dialog.show();

        btn_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remarks = withdraw_edt.getText().toString().trim();
                if (TextUtils.isEmpty(remarks)){
                    withdraw_edt.setError("Enter your remarks");
                    withdraw_edt.requestFocus();
                    return;
                }
                else {
                    if (NetworkUtils.isNetworkConnected(ViewComplaints.this))
                    {
                        //call withdraw complaint API
                        RequestParams jsonParams = new RequestParams();

                        jsonParams.put("token", token);
                        jsonParams.put("complaint_id_fk", comp_id);
                        jsonParams.put("complaint_remarks_detail", remarks);

                        Log.e("JSONPARAMS", jsonParams.toString());

                        getClient().post(API_Utils.WITHDRAW_COMPLAINT, jsonParams,
                                new AsyncHttpResponseHandler() {

                            @Override
                            public void onStart() {
                                super.onStart();
                                showNow.showLoadingDialog(ViewComplaints.this);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                String json = new String(responseBody);
                                Log.e("RESPONSE", "onSuccess: " + json);
                                showNow.scheduleDismiss();

                                try {

                                    JSONObject object = new JSONObject(json);
                                    int api_res_success = object.getInt("response");
                                    String api_res_success_msg = object.getString("response_msg");
                                    Log.e("api_res_success_msg", String.valueOf(api_res_success_msg));
                                    Log.e("api_res_success", String.valueOf(api_res_success));

                                    if (api_res_success == 1) {
                                        alert_dialog.dismiss();
                                        showNow.desplayPositiveToast(ViewComplaints.this, api_res_success_msg);
                                        registeredComplaintModels.clear();
                                        fetchMyComplaints();
                                    } else if (api_res_success == 0) {
                                        alert_dialog.dismiss();
                                        showNow.desplayErrorToast(ViewComplaints.this, api_res_success_msg);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("JSONException", "onSuccess: " + e.getMessage());

                                    JSONObject object = null;
                                    try {
                                        object = new JSONObject(json);
                                        int api_res_success = object.getInt("response");
                                        String api_res_success_msg = object.getString("response_msg");
                                        Log.e("api_res_success_msg", String.valueOf(api_res_success_msg));

                                        showNow.desplayErrorToast(ViewComplaints.this, api_res_success_msg);
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
                                showNow.desplayErrorToast(ViewComplaints.this, json);
                                showNow.scheduleDismiss();
                            }

                            @Override
                            public void onCancel() {
                                super.onCancel();
                                showNow.scheduleDismiss();
                            }
                        });
                    }
                    else {
                        Toast.makeText(ViewComplaints.this, "No internet",
                                Toast.LENGTH_SHORT).show();
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

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}