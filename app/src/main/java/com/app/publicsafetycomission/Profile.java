package com.app.publicsafetycomission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.publicsafetycomission.Constant.API_Utils;
import com.app.publicsafetycomission.Helpers.FileUtils;
import com.app.publicsafetycomission.Helpers.NetworkUtils;
import com.app.publicsafetycomission.Helpers.ShowNow;
import com.app.publicsafetycomission.adapters.DistrictAdapter3;
import com.app.publicsafetycomission.model.DistrictModel;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    private FirebaseAnalytics firebaseAnalytics;
    private ImageView btnBack;
    private EditText full_name,cnic,guardian,email,district,council,address,contact;
    private TextView username,gender;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ShowNow showNow;
    private AsyncHttpClient client;

    private String token;

    private LinearLayout no_internet_layout,data_layout;
    private TextView dismiss_net_layout;

    private ScrollView scrollbar;

    private CircleImageView profile_image;

    private Button updateProfileBtn;
    private String selection;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private static final int CAMERA_REQUEST_CODE = 1058;
    private static final int STORAGE_REQUEST_CODE = 1059;

    public static final int PICK_IMG = -1;
    public static final int PICK_CAM_IMG = -50;

    Uri cam_uri;
    String imgPath;
    Bitmap bitmap;
    FileUtils fileUtils;
    List<File> filesList = new ArrayList<>();
    File file;
    File file1;
    private DistrictAdapter3 districtAdapter3;
    ArrayList<DistrictModel> districtMODELS = new ArrayList<>();
    private String dist_id, dist_name;

    String full_url_img;
    RequestParams jsonParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);

        initVIEWS();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        scrollbar = findViewById(R.id.scrollBar);

        if (NetworkUtils.isNetworkConnected(Profile.this))
        {
            fetchProfileData();
            fetchDistrictsFromAPI();

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
            profile_image.setVisibility(View.GONE);

            dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtils.isNetworkConnected(Profile.this))
                    {
                        no_internet_layout.setVisibility(View.GONE);
                        fetchProfileData();
                        data_layout.setVisibility(View.VISIBLE);
                        profile_image.setVisibility(View.VISIBLE);
                    }
                    else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        data_layout.setVisibility(View.GONE);
                        profile_image.setVisibility(View.GONE);
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

        district.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDistrictList();
            }
        });

        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        hasStoragePermission(1);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });


        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = full_name.getText().toString().trim();
                String guardian_name = guardian.getText().toString().trim();
                String contact_no = contact.getText().toString().trim();
                String cnic_info = cnic.getText().toString().trim();
                String email_info = email.getText().toString().trim();
                String add = address.getText().toString().trim();
                String unioncouncil = council.getText().toString().trim();

                if (NetworkUtils.isNetworkConnected(Profile.this))
                {
                    //connected
                    if (file != null){
                        //Toast.makeText(Profile.this, "not null", Toast.LENGTH_SHORT).show();
                        updateProfileInfo(name, guardian_name, contact_no, cnic_info, email_info,
                                add, unioncouncil, file);
                    }
                    else {
                        //Toast.makeText(Profile.this, "null", Toast.LENGTH_SHORT).show();
                        updateProfileInfo(name, guardian_name, contact_no, cnic_info, email_info,
                                add, unioncouncil);
                    }

                }
                else {
                    Toast.makeText(Profile.this, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                    full_url_img = (base_url_img + user_avatar);
                    Log.e("full_url_img: ", full_url_img);

                    file1 = new File(full_url_img);

                    Glide.with(Profile.this)
                            .load(full_url_img)
                            .centerCrop()
                            .placeholder(R.drawable.placeholderrr)
                            .into(profile_image);

                    showNow.desplayPositiveToast(Profile.this,"User Information");


                    full_name.setText(complainant_name);
                    cnic.setText(complainant_cnic);
                    gender.setText(complainant_gender);
                    //gender.setText(complainant_gender);
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

    private void updateProfileInfo(String name, String guardian_name, String contact_no,
                                   String cnic_info, String email_info, String add,
                                   String unioncouncil,
                                   File file)
    {

        jsonParams = new RequestParams();

            jsonParams.put("token",token);
            jsonParams.put("complainant_name",name);
            jsonParams.put("complainant_guardian_name",guardian_name);
            jsonParams.put("complainant_contact",contact_no);
            jsonParams.put("complainant_cnic",cnic_info);
            jsonParams.put("complainant_gender",selection);
            jsonParams.put("complainant_email",email_info);
            jsonParams.put("complainant_district_id_fk",Integer.parseInt(dist_id));
            jsonParams.put("complainant_address",add);
            jsonParams.put("complainant_council",unioncouncil);
            try {
                jsonParams.put("user_avatar",file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            jsonParams.setForceMultipartEntityContentType(true);

            Log.d("JSONPARAMS",jsonParams.toString());
            Log.d("file",file.toString());

            getClient().post(API_Utils.UPDATE_PROFILE, jsonParams, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    showNow.showLoadingDialog(Profile.this);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String json = new String(responseBody);
                    Log.d("RESPONSE", "onSuccess: " + json);
                    showNow.scheduleDismiss();

                    try {

                        JSONObject object=new JSONObject(json);
                        JSONObject jsonObject  = object.getJSONObject("data");
                        JSONArray jsonArray = jsonObject.getJSONArray("complainant");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                            int size = object.length();
                            Log.d("LISTSIZE", String.valueOf(size));
                            String complaint_id = jsonObjectNew.getString ("complainant_id");
                            String user_id_fk = jsonObjectNew.getString ("user_id_fk");
                            String complainant_district_id_fk = jsonObjectNew.getString ("complainant_district_id_fk");
                            String complainant_council = jsonObjectNew.getString ("complainant_council");
                            String complainant_name = jsonObjectNew.getString ("complainant_name");
                            String complainant_guardian_name = jsonObjectNew.getString ("complainant_guardian_name");
                            String complainant_contact = jsonObjectNew.getString ("complainant_contact");
                            String complainant_cnic = jsonObjectNew.getString ("complainant_cnic");
                            String complainant_gender = jsonObjectNew.getString ("complainant_gender");
                            String complainant_email = jsonObjectNew.getString ("complainant_email");
                            String complainant_address = jsonObjectNew.getString ("complainant_address");
                            String complianant_registration_timestamp = jsonObjectNew.getString ("complianant_registration_timestamp");
                            String complainant_status = jsonObjectNew.getString ("complainant_status");
                            String district_name = jsonObjectNew.getString ("district_name");

                            showNow.desplayPositiveToast(Profile.this,"User Information Updated");

                            Log.d("RESPONSE_DATA", "complaint_id: " +complaint_id+
                                    "\nuser_id_fk: " +user_id_fk+
                                    "\ncomplainant_district_id_fk: " +complainant_district_id_fk+
                                    "\ncomplainant_council: " +complainant_council+
                                    "\ncomplainant_guardian_name: " +complainant_guardian_name+
                                    "\ncomplainant_name: " +complainant_name+
                                    "\ncomplainant_contact: " +complainant_contact+
                                    "\ncomplainant_cnic: " +complainant_cnic+
                                    "\ncomplainant_gender: " +complainant_gender+
                                    "\ncomplainant_email: " +complainant_email+
                                    "\ncomplainant_address: " +complainant_address+
                                    "\ncomplianant_registration_timestamp: " +complianant_registration_timestamp+
                                    "\ncomplainant_status: " +complainant_status+
                                    "\ndistrict_name: " +district_name);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Profile.this, "JSONException: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("JSONException: ",e.getMessage());
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    String json = new String(responseBody);
                    Log.e("REPONSE2", "onSuccess: " + json);
                    Toast.makeText(Profile.this, "ERROR: " + json, Toast.LENGTH_LONG).show();
                    showNow.scheduleDismiss();
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    showNow.scheduleDismiss();
                }
            });

    }


    private void updateProfileInfo(String name, String guardian_name, String contact_no,
                                   String cnic_info, String email_info, String add,
                                   String unioncouncil)
    {

        jsonParams = new RequestParams();

        jsonParams.put("token",token);
        jsonParams.put("complainant_name",name);
        jsonParams.put("complainant_guardian_name",guardian_name);
        jsonParams.put("complainant_contact",contact_no);
        jsonParams.put("complainant_cnic",cnic_info);
        jsonParams.put("complainant_gender",selection);
        jsonParams.put("complainant_email",email_info);
        jsonParams.put("complainant_district_id_fk",Integer.parseInt(dist_id));
        jsonParams.put("complainant_address",add);
        jsonParams.put("complainant_council",unioncouncil);
        try {
            jsonParams.put("user_avatar",file1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        jsonParams.setForceMultipartEntityContentType(true);

        Log.d("JSONPARAMS",jsonParams.toString());
        Log.d("files",file1.toString());

        getClient().post(API_Utils.UPDATE_PROFILE, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(Profile.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.d("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {

                    JSONObject object=new JSONObject(json);
                    JSONObject jsonObject  = object.getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("complainant");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        int size = object.length();
                        Log.d("LISTSIZE", String.valueOf(size));
                        String complaint_id = jsonObjectNew.getString ("complainant_id");
                        String user_id_fk = jsonObjectNew.getString ("user_id_fk");
                        String complainant_district_id_fk = jsonObjectNew.getString ("complainant_district_id_fk");
                        String complainant_council = jsonObjectNew.getString ("complainant_council");
                        String complainant_name = jsonObjectNew.getString ("complainant_name");
                        String complainant_guardian_name = jsonObjectNew.getString ("complainant_guardian_name");
                        String complainant_contact = jsonObjectNew.getString ("complainant_contact");
                        String complainant_cnic = jsonObjectNew.getString ("complainant_cnic");
                        String complainant_gender = jsonObjectNew.getString ("complainant_gender");
                        String complainant_email = jsonObjectNew.getString ("complainant_email");
                        String complainant_address = jsonObjectNew.getString ("complainant_address");
                        String complianant_registration_timestamp = jsonObjectNew.getString ("complianant_registration_timestamp");
                        String complainant_status = jsonObjectNew.getString ("complainant_status");
                        String district_name = jsonObjectNew.getString ("district_name");

                        showNow.desplayPositiveToast(Profile.this,"User Information Updated");

                        Log.d("RESPONSE_DATA", "complaint_id: " +complaint_id+
                                "\nuser_id_fk: " +user_id_fk+
                                "\ncomplainant_district_id_fk: " +complainant_district_id_fk+
                                "\ncomplainant_council: " +complainant_council+
                                "\ncomplainant_guardian_name: " +complainant_guardian_name+
                                "\ncomplainant_name: " +complainant_name+
                                "\ncomplainant_contact: " +complainant_contact+
                                "\ncomplainant_cnic: " +complainant_cnic+
                                "\ncomplainant_gender: " +complainant_gender+
                                "\ncomplainant_email: " +complainant_email+
                                "\ncomplainant_address: " +complainant_address+
                                "\ncomplianant_registration_timestamp: " +complianant_registration_timestamp+
                                "\ncomplainant_status: " +complainant_status+
                                "\ndistrict_name: " +district_name);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Profile.this, "JSONException: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("JSONException: ",e.getMessage());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(Profile.this, "ERROR: " + json, Toast.LENGTH_LONG).show();
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
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

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        dist_id = jsonObjectNew.getString("district_id");
                        dist_name = jsonObjectNew.getString("district_name");
                        String district_status = jsonObjectNew.getString("district_status");
                        Log.d("RESPONSE_DATA","dist_id: " +dist_id+
                                "\ndist_name: " +dist_name+
                                "\ndistrict_status: " +district_status);

                        DistrictModel model = new DistrictModel(dist_id,dist_name);
                        districtMODELS.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(Profile.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }

    private void showDistrictList() {
        district.requestFocus();
        final Dialog districtsDilaog = new Dialog(Profile.this, R.style.dialog_theme);
        districtsDilaog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        districtsDilaog.setCancelable(true);
        districtsDilaog.setContentView(R.layout.dist_dialog);
        districtsDilaog.getWindow().setBackgroundDrawable(new ColorDrawable(0x7f000000));
        EditText searchBar = (EditText) districtsDilaog.findViewById(R.id.searchBar);
        RecyclerView districtList= (RecyclerView) districtsDilaog.findViewById(R.id.districtList);
        districtList.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        districtList.setLayoutManager(layoutManager);
        districtsDilaog.show();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    districtAdapter3 = new DistrictAdapter3(Profile.this,
                            districtMODELS,districtsDilaog);
                    districtList.setAdapter(districtAdapter3);
                    districtAdapter3.notifyDataSetChanged();
                    //district_id.setText(""+distrct_name.get(i));
                }
            });
        } catch (Exception e) {
            Log.e("ERRR", e.getMessage());
            String error = e.getMessage().toString();
            Toast.makeText(Profile.this, error,
                    Toast.LENGTH_SHORT).show();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                districtAdapter3.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void recyclerTouchMethod(String id, String name) {
        district.setText(""+name);
        dist_id = id;
        Log.e("DISTRICT_values","name: "+district.getText().toString()+
                "\nid: "+dist_id);
    }

    private void pickImage() {
        showImagePickDialog();
    }

    private void showImagePickDialog() {

        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //allowed, open camera
                                pickFromCamera();
                            }
                            else {
                                //not allowed, request
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (hasStoragePermission(1)){
                                //allowed, open gallery
                                pickFromGallery();
                            }
                            else {
                                //not allowed, request
                                requestStoragePermission();
                            }
                        }
                    }
                })
                .show();

    }

    private boolean hasStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void initVIEWS() {
        showNow = new ShowNow(this);

        btnBack = findViewById(R.id.btnBack);
        full_name = findViewById(R.id.full_name);
        cnic = findViewById(R.id.cnic);
        guardian = findViewById(R.id.guardian);
        email = findViewById(R.id.email);
        district = findViewById(R.id.district);
        council = findViewById(R.id.council);
        address = findViewById(R.id.address);
        contact = findViewById(R.id.contact);
        gender = findViewById(R.id.gender);
        username = findViewById(R.id.username);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);
        data_layout = findViewById(R.id.data_layout);
        no_internet_layout = findViewById(R.id.no_internet_layout);

        profile_image = findViewById(R.id.profile_image);

        updateProfileBtn = findViewById(R.id.updateProfileBtn);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PICK_IMG)
                pickFromGallery();
            if (requestCode == PICK_CAM_IMG)
                pickFromCamera();
        }
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = Profile.this.getContentResolver().
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
        startCamera.launch(cameraIntent);                // VERY NEW WAY
    }

    private void pickFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launchImageActivity.launch(Intent.createChooser(photoPickerIntent, "Pictures"));
    }


    //new way for onActivityResult
    //For camera
    ActivityResultLauncher<Intent> startCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        //Uri image_uri = cam_uri;
                        Log.e("URI",cam_uri.toString());
                        try {
                            imgPath = fileUtils.getPath(Profile.this, cam_uri);
                            Log.e("FILES",imgPath);

                            if (imgPath != null)
                            {
                                file = new File(imgPath);
                                filesList.add(new File(imgPath));
                                Toast.makeText(Profile.this, "Image picked", Toast.LENGTH_SHORT).show();
                                bitmap = MediaStore.Images.Media.getBitmap(Profile.this.
                                        getContentResolver(), cam_uri);

                                profile_image.setImageBitmap(bitmap);

                                //imgToString(bitmap);
                            }
                            else {
                                Toast.makeText(Profile.this, "Image not picked",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    }
                }
            });


    //new way for onActivityResult
    //For images
    ActivityResultLauncher<Intent> launchImageActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri image_uri = data.getData();
                        try {
                            imgPath = fileUtils.getPath(Profile.this, image_uri);
                            Log.e("FILES",imgPath);

                            if (imgPath != null){
                                file = new File(imgPath);
                                filesList.add(new File(imgPath));
                                Toast.makeText(Profile.this, "Image picked", Toast.LENGTH_SHORT).show();
                                bitmap = MediaStore.Images.Media.getBitmap(Profile.this.
                                        getContentResolver(), image_uri);

                                profile_image.setImageBitmap(bitmap);

                                //imgToString(bitmap);

                            } else {
                                Toast.makeText(Profile.this, "Image not picked",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    }
                }
            });
}