package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.adapters.MyRegisteredComplaintsAdapter;
import com.example.publicsafetycomission.model.RegisteredComplaintModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ViewComplaints extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView my_complaint_list;
    private AsyncHttpClient client;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ShowNow showNow;
    String token;

    private MyRegisteredComplaintsAdapter adapter;
    ArrayList<RegisteredComplaintModel> registeredComplaintModels = new ArrayList<>();

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
            Toast.makeText(ViewComplaints.this, "No internet", Toast.LENGTH_SHORT).show();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
                        String complaint_source = jsonObjectNew.getString("complaint_source");
                        String complaint_council = jsonObjectNew.getString("complaint_council");
                        String complaint_detail = jsonObjectNew.getString("complaint_detail");
                        String complaint_entry_timestamp = jsonObjectNew.getString("complaint_entry_timestamp");
                        String complainant_name = jsonObjectNew.getString("complainant_name");
                        String district_name = jsonObjectNew.getString("district_name");
                        String complaint_status_title = jsonObjectNew.getString("complaint_status_title");
                        String complaint_status_color = jsonObjectNew.getString("complaint_status_color");
                        String complaint_category_name = jsonObjectNew.getString("complaint_category_name");

                        RegisteredComplaintModel model = new RegisteredComplaintModel(complaint_source, complaint_council,
                                complaint_detail, complaint_entry_timestamp,complainant_name,district_name,
                                complaint_status_title,complaint_status_color,complaint_category_name);
                        registeredComplaintModels.add(model);

                        Log.d("RESPONSE_DATA", "complaint_source: " +complaint_source+ "\ncomplaint_council: " +complaint_council+
                                "\ncomplaint_detail: " +complaint_detail+ "\ncomplaint_entry_timestamp: "
                                +complaint_entry_timestamp+ "\ncomplainant_name: " +complainant_name+ "\ndistrict_name: " +district_name+
                                "\ncomplaint_status_title: " +complaint_status_title+ "\ncomplaint_status_color: " +complaint_status_color+
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
                                Toast.makeText(ViewComplaints.this, "Your complaints", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("ERRR", e.getMessage());
                        String error = e.getMessage().toString();
                        Toast.makeText(ViewComplaints.this, "TOUSIF : " + error, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewComplaints.this, "JSONException: " + e.getMessage(), Toast.LENGTH_LONG).show();

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
        btnBack = findViewById(R.id.btnBack);
        showNow = new ShowNow(this);
    }
}