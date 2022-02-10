package com.example.publicsafetycomission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.adapters.DistrictAdapter;
import com.example.publicsafetycomission.adapters.DistrictAdapter2;
import com.example.publicsafetycomission.databaseRef.DBHelperClass;
import com.example.publicsafetycomission.databaseRef.DataBaseConstant;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class ProfileUpdate extends AppCompatActivity {

    private EditText full_name,guardian_name,phone_no,cnic_Edt,email_edt,home_district,address_edt;
    private AppCompatButton updateBtn;
    private AutoCompleteTextView gender_acTv;

    String name, g_name, p_number, gender, cnic, email, dist_id, address;

    private ShowNow showNow;

    ArrayList<HashMap<String,String>> getDistricts = new ArrayList<HashMap<String, String>>();
    ArrayList<String> distrct_id = new ArrayList<String>();
    ArrayList<String> distrct_name = new ArrayList<String>();
    String temp3[];
    DBHelperClass dbHelperClass;
    String token;
    String selection;
    String complainant_email;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private AsyncHttpClient client;

    private static Pattern CNIC_PATTERN,AFG_CNIC_PATTERN,PASSWORD_PATTERN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        initViews();

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        complainant_email = pref.getString("complainant_email", "No Data");
        Log.e("token",token);
        Log.e("complainant_email",complainant_email);
        if (complainant_email.equals("No Data")){
            Log.e("PREFF","SHARED PREF NOT EXIST");
        }
        else {
            Log.e("PREFF","SHARED PREF EXIST");
            goToNextScreen();
        }

        dbHelperClass = new DBHelperClass(this);
        showNow = new ShowNow(this);

        try {
            getDistricts = dbHelperClass.getDistrictData();
            Log.e("DIST_Size", String.valueOf(getDistricts.size()));
            if (getDistricts.size() > 0) {
                temp3 = new String[getDistricts.size()];
                for (int i = 0; i < getDistricts.size(); i++) {
                    HashMap<String, String> map = getDistricts.get(i);
                    distrct_id.add( map.get(DataBaseConstant.TAG_DIST_ID));
                    distrct_name.add(map.get(DataBaseConstant.TAG_DIST_NAME));
                    temp3[i]=map.get("project_name3");
                }
            }
            else {
                Toast.makeText(ProfileUpdate.this, "District data missing from API",
                        Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
        }


        // CUSTOM AUTOCOMPLETE TEXTVIEW FOR GENDER SPINNER
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.Gender_Names));
        gender_acTv.setAdapter(arrayAdapter);
        gender_acTv.setCursorVisible(false);
        gender_acTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                gender_acTv.showDropDown();
                selection = (String) adapterView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), selection,
                        Toast.LENGTH_SHORT);
            }
        });
        gender_acTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender_acTv.showDropDown();
            }
        });

        home_district.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDistrictList();
            }
        });

        //cnic_Edt.addTextChangedListener(new PatternedTextWatcher("#####-#######-#"));

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = full_name.getText().toString().trim();
                g_name = guardian_name.getText().toString().trim();
                p_number = phone_no.getText().toString().trim();
                cnic = cnic_Edt.getText().toString();
                email = email_edt.getText().toString().trim();
                dist_id = home_district.getText().toString().trim();
                address = address_edt.getText().toString().trim();

                if (TextUtils.isEmpty(name))
                {
                    full_name.setError("Name is required");
                }
                else if (TextUtils.isEmpty(g_name))
                {
                    guardian_name.setError("Guardian name is required");
                }
                else if (TextUtils.isEmpty(p_number))
                {
                    phone_no.setError("Phone number is required");
                }
                else if(p_number.length() < 11 && p_number.length() > 11)
                {
                    phone_no.setError("Invalid Mobile Number");
                }
                else if (TextUtils.isEmpty(cnic))
                {
                    cnic_Edt.setError("CNIC is required");
                }

                else if (TextUtils.isEmpty(email))
                {
                    email_edt.setError("Email is required");
                }
                else if (TextUtils.isEmpty(address))
                {
                    address_edt.setError("Address is required");
                }
                else if (selection == null) {
                    Toast.makeText(ProfileUpdate.this, "Gender is required",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(dist_id))
                {
                    home_district.setError("Enter your home district name");
                }
                else
                {
                    if (NetworkUtils.isNetworkConnected(ProfileUpdate.this)){

                        updateProfile();

                    } else {
                        Toast.makeText(ProfileUpdate.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void goToNextScreen() {
        Intent intent = new Intent(ProfileUpdate.this,Dashboard.class);
        startActivity(intent);
        finish();
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void showDistrictList() {
        home_district.requestFocus();
        final Dialog districtsDilaog = new Dialog(ProfileUpdate.this, R.style.dialog_theme);
        districtsDilaog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        districtsDilaog.setCancelable(true);
        districtsDilaog.setContentView(R.layout.dist_dialog2);
        districtsDilaog.getWindow().setBackgroundDrawable(new ColorDrawable(0x7f000000));
        ListView distlist= (ListView) districtsDilaog.findViewById(R.id.countrylist);
        DistrictAdapter2 districtAdapter2= new DistrictAdapter2(ProfileUpdate.this,
                distrct_id,distrct_name,temp3);
        distlist.setAdapter(districtAdapter2);
        districtsDilaog.show();

        distlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                districtsDilaog.dismiss();
                home_district.setText(""+distrct_name.get(i));
                dist_id = distrct_id.get(i);
                Log.e("dist_id", dist_id);
            }
        });
    }

    private void updateProfile() {

        RequestParams jsonParams = new RequestParams();
        jsonParams.put("token",token);
        jsonParams.put("complainant_name",name);
        jsonParams.put("complainant_guardian_name",g_name);
        jsonParams.put("complainant_contact",p_number);
        jsonParams.put("complainant_cnic",cnic);
        jsonParams.put("complainant_gender",selection);
        jsonParams.put("complainant_email",email);
        jsonParams.put("complainant_district_id_fk", dist_id);
        jsonParams.put("complainant_address",address);

        getClient().post(API_Utils.UPDATE_PROFILE, jsonParams, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showNow.showLoadingDialog(ProfileUpdate.this);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String json = new String(responseBody);
                Log.e("RESPONSE", "onSuccess: " + json);
                showNow.scheduleDismiss();

                try {
                    JSONObject object=new JSONObject(json);
                    //JSONArray jsonArray = object.getJSONArray("data");
                    JSONObject jsonObject  = object.getJSONObject("data");

                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                    /*for (int i = 0; i < jsonObject.length(); i++) {
                        JSONObject jsonObjectNew = jsonObject.getJSONObject(i);*/

                        int size = jsonObject.length();
                        Log.e("LISTSIZE", String.valueOf(size));
                        Log.e("jsonObject", String.valueOf(jsonObject));

                        String complainant_id = jsonObject.optString("complainant_id");
                        String user_id_fk = jsonObject.optString("user_id_fk");
                        String district_id_fk = jsonObject.optString("district_id_fk");
                        String complainant_district_id_fk = jsonObject.optString("complainant_district_id_fk");
                        String complainant_council = jsonObject.optString("complainant_council");
                        String complainant_name = jsonObject.optString("complainant_name");
                        String complainant_guardian_name = jsonObject.optString("complainant_guardian_name");
                        String complainant_contact = jsonObject.optString("complainant_contact");
                        String complainant_cnic = jsonObject.optString("complainant_cnic");
                        String complainant_gender = jsonObject.optString("complainant_gender");
                        String complainant_email = jsonObject.optString("complainant_email");
                        String complainant_address = jsonObject.optString("complainant_address");
                        String complianant_registration_timestamp = jsonObject.optString("complianant_registration_timestamp");
                        String complainant_status = jsonObject.optString("complainant_status");

                        Log.d("send data", "complainant_id: " +complainant_id+
                                "\nuser_id_fk: " +user_id_fk+
                                "\ndistrict_id_fk: " +district_id_fk+ "\ncomplainant_district_id_fk: "
                                +complainant_district_id_fk+ "\ncomplainant_council: " +complainant_council+
                                "\ncomplainant_name: " +complainant_name+
                                "\ncomplainant_guardian_name: " +complainant_guardian_name+
                                "\ncomplainant_contact: " +complainant_contact+
                                "\ncomplainant_cnic: " +complainant_cnic+
                                "\ncomplainant_gender: " +complainant_gender+
                                "\ncomplainant_email: " +complainant_email+
                                "\ncomplainant_address: " +complainant_address+
                                "\ncomplianant_registration_timestamp: " +complianant_registration_timestamp+
                                "\ncomplainant_status: " +complainant_status);

                        Toast.makeText(ProfileUpdate.this, "Profile is updated",
                                Toast.LENGTH_SHORT).show();

                        editor = pref.edit();
                        editor.putString("complainant_email", complainant_email);
                        Log.e("SHARED_OK", "ok");
                        editor.commit(); // commit changes

                        goToNextScreen();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSONException", "onSuccess: " + e.getMessage());
/*
                    JSONObject object= null;
                    try {
                        object = new JSONObject(json);
                        int api_res_success = object.getInt("response");
                        String api_res_success_msg = object.getString("response_msg");
                        Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));

                        showNow.desplayErrorToast(ProfileUpdate.this,api_res_success_msg);
                        showNow.scheduleDismiss();
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }*/
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("onFailure", "onSuccess: " + json);
                showNow.desplayErrorToast(ProfileUpdate.this,json);
                showNow.scheduleDismiss();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                showNow.scheduleDismiss();
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

    private void initViews() {
        updateBtn = findViewById(R.id.updateBtn);
        full_name = findViewById(R.id.full_name);
        guardian_name = findViewById(R.id.guardian_name);
        phone_no = findViewById(R.id.phone_no);
        cnic_Edt = findViewById(R.id.cnic_Edt);
        email_edt = findViewById(R.id.email_edt);
        home_district = findViewById(R.id.home_district);
        address_edt = findViewById(R.id.address_edt);
        gender_acTv = findViewById(R.id.gender_acTv);
    }
}