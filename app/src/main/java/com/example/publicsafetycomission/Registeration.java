package com.example.publicsafetycomission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.ApiCallback;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.gne.www.lib.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class Registeration extends AppCompatActivity {

    EditText user_username, user_password,confirm_user_password, contact_no, full_name,
            cnic,guardian_name,email_edt,union_council,address_edt;
    AutoCompleteTextView gender_acTv;
    Button registerBtn;
    TextView loginBtn;
    String username, password,c_password, phone, code, fullName, user_cnic, g_name, gender, email, address,council;
    String selection;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ShowNow showNow;
    private AsyncHttpClient client;

    private LinearLayout register_fields_layout,otp_verification_layout;

    private static final String KEY_VERIFICATION_ID = "key_verification_id";
    private String verificationId;
    private FirebaseAuth mAuth;
    TextInputEditText editText;
    AppCompatButton buttonSignIn;
    PinView pinView;

    TextView resend_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        initViews();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = pinView.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {

                    pinView.requestPinFocus();
                    return;
                }
                verifyCode(code);
            }
        });
        if (verificationId == null && savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
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

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = user_username.getText().toString();
                code = "+92";       //for pakistan
                phone = contact_no.getText().toString();
                password = user_password.getText().toString();
                c_password = confirm_user_password.getText().toString();
                fullName = full_name.getText().toString().trim();
                g_name = guardian_name.getText().toString().trim();
                user_cnic = cnic.getText().toString().trim();
                email = email_edt.getText().toString().trim();
                council = union_council.getText().toString().trim();
                address = address_edt.getText().toString().trim();

                if (TextUtils.isEmpty(username))
                {
                    user_username.setError("Username is required");
                }
                else if (TextUtils.isEmpty(password))
                {
                    user_password.setError("Password is required");
                }
                else if (TextUtils.isEmpty(c_password))
                {
                    confirm_user_password.setError("Confirm Password is required");
                }
                else if (!c_password.equals(password))
                {
                    confirm_user_password.setError("Password doesn't match");
                }
                else if (TextUtils.isEmpty(phone))
                {
                    contact_no.setError("Phone number is required");
                }
                else if (phone.isEmpty() || phone.length() < 10) {
                    contact_no.setError("Valid phone number is required");
                }
                else if (TextUtils.isEmpty(fullName))
                {
                    full_name.setError("Name is required");
                }
                else if (TextUtils.isEmpty(g_name))
                {
                    guardian_name.setError("Guardian name is required");
                }
                else if (TextUtils.isEmpty(user_cnic))
                {
                    cnic.setError("CNIC number is required");
                }
                else if (user_cnic.isEmpty() || user_cnic.length() < 13) {
                    cnic.setError("Valid CNIC number is required");
                }
                else if (TextUtils.isEmpty(address))
                {
                    address_edt.setError("Address is required");
                }
                else if (selection == null) {
                    Toast.makeText(Registeration.this, "Gender is required",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(email))
                {
                    email_edt.setError("Email is required");
                }
                else if (TextUtils.isEmpty(council))
                {
                    union_council.setError("Union Council is required");
                }
                else
                {
                    if (NetworkUtils.isNetworkConnected(Registeration.this)){

                        //registerMe();
                        showOTPVerificationLayout();

                    } else {
                        Toast.makeText(Registeration.this, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(Registeration.this, Login.class);
                startActivity(n);
                layoutTransition();
            }
        });
    }

    private void showOTPVerificationLayout() {
        register_fields_layout.setVisibility(View.GONE);
        otp_verification_layout.setVisibility(View.VISIBLE);

        String phoneNumber = code + phone;
        Log.e("phone", phoneNumber);

        sendVerificationCode(phoneNumber);

        resend_code = findViewById(R.id.resend_code);
        resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(phoneNumber);
            }
        });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS,
                Registeration.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String code = phoneAuthCredential.getSmsCode();
                        if (code != null) {
                            editText.setText(code);
                            verifyCode(code);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(Registeration.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                    }
                });
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(Registeration.this, "Code verified",
                                    Toast.LENGTH_SHORT).show();
                            registerMe();
                            /*Intent intent = new Intent(Registeration.this,
                                    Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            layoutTransition();*/

                        } else {
                            Toast.makeText(Registeration.this,
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void registerMe() {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("user_name",username);
        jsonParams.put("user_password",password);
        jsonParams.put("user_contact",phone);
        jsonParams.put("complainant_name",fullName);
        jsonParams.put("complainant_guardian_name",g_name);
        jsonParams.put("complainant_cnic",cnic);
        jsonParams.put("complainant_gender",selection);
        jsonParams.put("complainant_email",email);
        jsonParams.put("complainant_council",council);
        jsonParams.put("complainant_address",address);

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

                    /*Intent intent = new Intent(Registeration.this, VerifyPhoneActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                    Log.d("userdata", "onClick: " + username + password);*/
                    Intent intent = new Intent(Registeration.this, Login.class);
                    startActivity(intent);
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
        user_username = findViewById(R.id.user_username);
        user_password = findViewById(R.id.user_password);
        contact_no = findViewById(R.id.contact_no);
        full_name = findViewById(R.id.full_name);
        cnic = findViewById(R.id.cnic);
        guardian_name = findViewById(R.id.guardian_name);
        email_edt = findViewById(R.id.email_edt);
        union_council = findViewById(R.id.union_council);
        address_edt = findViewById(R.id.address_edt);
        registerBtn = findViewById(R.id.registerBtn);
        gender_acTv = findViewById(R.id.gender_acTv);
        loginBtn = findViewById(R.id.loginBtn);
        register_fields_layout = findViewById(R.id.register_fields_layout);
        otp_verification_layout = findViewById(R.id.otp_verification_layout);
        pinView=findViewById(R.id.editTextCode);
        editText = findViewById(R.id.editTextCode1);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        confirm_user_password = findViewById(R.id.confirm_user_password);
        //tvlogin = findViewById(R.id.tvlogin);
        showNow=new ShowNow(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_VERIFICATION_ID,verificationId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationId = savedInstanceState.getString(KEY_VERIFICATION_ID);
    }
}