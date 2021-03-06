package com.app.publicsafetycomission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.publicsafetycomission.Constant.API_Utils;
import com.app.publicsafetycomission.Helpers.NetworkUtils;
import com.app.publicsafetycomission.Helpers.ShowNow;
import com.app.publicsafetycomission.adapters.DistrictAdapter;
import com.app.publicsafetycomission.model.DistrictModel;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.app.publicsafetycomission.Helpers.FileUtils;
import com.gne.www.lib.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class Registeration extends AppCompatActivity {
    private FirebaseAnalytics firebaseAnalytics;

    EditText user_username, user_password,confirm_user_password, contact_no, full_name,
            cnic,guardian_name,email_edt,union_council,address_edt,district;
    AutoCompleteTextView gender_acTv;
    Button registerBtn;
    LinearLayout loginBtn;
    String username, password,c_password, phone, code, fullName, user_cnic, g_name, gender, email,
            address,council;
    String selection;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ShowNow showNow;
    private AsyncHttpClient client;

    private LinearLayout register_fields_layout,otp_verification_layout,no_internet_layout,bottomLayout;

    private static final String KEY_VERIFICATION_ID = "key_verification_id";
    private String verificationId;
    private FirebaseAuth mAuth;
    TextInputEditText editText;
    AppCompatButton buttonSignIn;
    PinView pinView;

    TextView resend_code;

    private TextView dismiss_net_layout;

    private String dist_id,dist_name;

    private ScrollView scrollBar;

    ArrayList<DistrictModel> districtMODELS = new ArrayList<>();
    private DistrictAdapter distAdapter;

    private CircleImageView profile_image;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private static final int CAMERA_REQUEST_CODE = 1058;
    private static final int STORAGE_REQUEST_CODE = 1059;

    public static final int PICK_IMG = -1;
    public static final int PICK_CAM_IMG = -50;

    private Uri cam_uri;
    private String imgPath;
    private Bitmap bitmap;
    private FileUtils fileUtils;
    private List<File> filesList = new ArrayList<>();
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        initViews();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();


        if (NetworkUtils.isNetworkConnected(Registeration.this))
        {
            fetchDistrictsFromAPI();
        }else {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }

        scrollBar = findViewById(R.id.scrollBar);
        //HIGHLIGHT SCROLLVIEW
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrollBar.scrollBy(0, -1);
                        scrollBar.scrollBy(0, 1);
                    }
                });
            }
        }, 0, 1500);


        district.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDistrictList();
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = pinView.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {

                    pinView.requestPinFocus();
                    return;
                }

                if (NetworkUtils.isNetworkConnected(Registeration.this))
                {
                    verifyCode(code);
                }
                else {
                    no_internet_layout.setVisibility(View.VISIBLE);
                    otp_verification_layout.setVisibility(View.GONE);
                    register_fields_layout.setVisibility(View.GONE);

                    dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (NetworkUtils.isNetworkConnected(Registeration.this))
                            {
                                otp_verification_layout.setVisibility(View.VISIBLE);
                                no_internet_layout.setVisibility(View.GONE);
                                register_fields_layout.setVisibility(View.GONE);
                            }
                            else {
                                no_internet_layout.setVisibility(View.VISIBLE);
                                otp_verification_layout.setVisibility(View.GONE);
                                register_fields_layout.setVisibility(View.GONE);
                            }
                        }
                    });
                }
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


        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        hasStoragePermission(1);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
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
                fullName = full_name.getText().toString();
                g_name = guardian_name.getText().toString();
                user_cnic = cnic.getText().toString();
                email = email_edt.getText().toString();
                council = union_council.getText().toString();
                address = address_edt.getText().toString();

                if (file == null){
                    Toast.makeText(Registeration.this, "Select your profile image",
                            Toast.LENGTH_SHORT).show();
                    AnimateImage(profile_image);
                }
                else if (TextUtils.isEmpty(fullName))
                {
                    AnimateView(full_name);
                    full_name.setError("Name is required");
                    full_name.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(user_cnic))
                {
                    AnimateView(cnic);
                    cnic.setError("CNIC number is required");
                    cnic.requestFocus();
                    return;
                }
                else if (user_cnic.isEmpty() || user_cnic.length() < 13) {
                    AnimateView(cnic);
                    cnic.setError("Valid CNIC number is required");
                    cnic.requestFocus();
                    return;
                }
                else if (selection == null) {
                    AnimateView(gender_acTv);
                    Toast.makeText(Registeration.this, "Gender is required",
                            Toast.LENGTH_SHORT).show();
                    gender_acTv.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(g_name))
                {
                    AnimateView(guardian_name);
                    guardian_name.setError("Guardian name is required");
                    guardian_name.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(district.getText().toString())) {
                    AnimateView(district);
                    Toast.makeText(Registeration.this, "District Name is required",
                            Toast.LENGTH_SHORT).show();
                    district.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(email))
                {
                    AnimateView(email_edt);
                    email_edt.setError("Email is required");
                    email_edt.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(council))
                {
                    AnimateView(union_council);
                    union_council.setError("Union Council is required");
                    union_council.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(address))
                {
                    AnimateView(address_edt);
                    address_edt.setError("Address is required");
                    address_edt.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(phone))
                {
                    AnimateView(contact_no);
                    contact_no.setError("Phone number is required");
                    contact_no.requestFocus();
                    return;
                }
                else if (phone.isEmpty() || phone.length() < 10) {
                    AnimateView(contact_no);
                    contact_no.setError("Valid phone number is required");
                    contact_no.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(username))
                {
                    AnimateView(user_username);
                    user_username.setError("Username is required");
                    user_username.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(password))
                {
                    AnimateView(user_password);
                    user_password.setError("Password is required");
                    user_password.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(c_password))
                {
                    AnimateView(confirm_user_password);
                    confirm_user_password.setError("Confirm Password is required");
                    confirm_user_password.requestFocus();
                    return;
                }
                else if (!c_password.equals(password))
                {
                    AnimateView(confirm_user_password);
                    confirm_user_password.setError("Password doesn't match");
                    confirm_user_password.requestFocus();
                    return;
                }
                else
                {
                    if (NetworkUtils.isNetworkConnected(Registeration.this)){

                        //registerMe();
                        checkRegisteredUsersFromAPI();

                    } else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        otp_verification_layout.setVisibility(View.GONE);
                        register_fields_layout.setVisibility(View.GONE);

                        dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtils.isNetworkConnected(Registeration.this))
                                {
                                    otp_verification_layout.setVisibility(View.GONE);
                                    no_internet_layout.setVisibility(View.GONE);
                                    register_fields_layout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    no_internet_layout.setVisibility(View.VISIBLE);
                                    otp_verification_layout.setVisibility(View.GONE);
                                    register_fields_layout.setVisibility(View.GONE);
                                }
                            }
                        });
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

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = Registeration.this.getContentResolver().
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

    private boolean hasStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
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
                            imgPath = fileUtils.getPath(Registeration.this, cam_uri);
                            Log.e("FILES",imgPath);

                            if (imgPath != null)
                            {
                                file = new File(imgPath);
                                filesList.add(new File(imgPath));
                                Toast.makeText(Registeration.this, "Image picked", Toast.LENGTH_SHORT).show();
                                bitmap = MediaStore.Images.Media.getBitmap(Registeration.this.
                                        getContentResolver(), cam_uri);

                                profile_image.setImageBitmap(bitmap);

                                //imgToString(bitmap);
                            }
                            else {
                                Toast.makeText(Registeration.this, "Image not picked",
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
                            imgPath = fileUtils.getPath(Registeration.this, image_uri);
                            Log.e("FILES",imgPath);

                            if (imgPath != null){
                                file = new File(imgPath);
                                filesList.add(new File(imgPath));
                                Toast.makeText(Registeration.this, "Image picked", Toast.LENGTH_SHORT).show();
                                bitmap = MediaStore.Images.Media.getBitmap(Registeration.this.
                                        getContentResolver(), image_uri);

                                profile_image.setImageBitmap(bitmap);

                                //imgToString(bitmap);

                            } else {
                                Toast.makeText(Registeration.this, "Image not picked",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    }
                }
            });

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
                    Toast.makeText(Registeration.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(Registeration.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }

    private void checkRegisteredUsersFromAPI() {
        RequestParams jsonParams = new RequestParams();

        jsonParams.put("user_name", username);
        jsonParams.put("user_contact", phone);

        Log.e("JSONPARAMS", jsonParams.toString());

        getClient().post(API_Utils.SIGNUP_VALIDATIONS, jsonParams, new AsyncHttpResponseHandler() {

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
                    int api_res_success = object.getInt("response");
                    String api_res_success_msg = object.getString("response_msg");
                    Log.e("api_res_success_msg",String.valueOf(api_res_success_msg));
                    Log.e("api_res_success",String.valueOf(api_res_success));

                    if (api_res_success == 1)
                    {
                        showNow.desplayPositiveToast(Registeration.this,api_res_success_msg);
                        Toast.makeText(Registeration.this, api_res_success_msg, Toast.LENGTH_SHORT).show();

                        showOTPVerificationLayout();
                    }
                    else if (api_res_success == 0)
                    {
                        showNow.desplayErrorToast(Registeration.this,api_res_success_msg);
                        Toast.makeText(Registeration.this, api_res_success_msg, Toast.LENGTH_SHORT).show();
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

                        showNow.desplayErrorToast(Registeration.this,api_res_success_msg);
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

    private void AnimateView(EditText editText) {
        YoYo.with(Techniques.Tada)
                .duration(800)
                .repeat(1)
                .playOn(editText);
    }

    private void AnimateImage(CircleImageView profile_image) {
        YoYo.with(Techniques.Tada)
                .duration(800)
                .repeat(1)
                .playOn(profile_image);
    }

    private void showDistrictList() {
        district.requestFocus();
        final Dialog districtsDilaog = new Dialog(Registeration.this, R.style.dialog_theme);
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
                    distAdapter = new DistrictAdapter(Registeration.this,
                            districtMODELS,districtsDilaog);
                    districtList.setAdapter(distAdapter);
                    distAdapter.notifyDataSetChanged();
                    //district_id.setText(""+distrct_name.get(i));
                }
            });
        } catch (Exception e) {
            Log.e("ERRR", e.getMessage());
            String error = e.getMessage().toString();
            Toast.makeText(Registeration.this, error,
                    Toast.LENGTH_SHORT).show();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                distAdapter.getFilter().filter(s);
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

    private void showOTPVerificationLayout() {
        register_fields_layout.setVisibility(View.GONE);
        no_internet_layout.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        otp_verification_layout.setVisibility(View.VISIBLE);

        String phoneNumber = code + phone;
        Log.e("phone", phoneNumber);

        sendVerificationCode(phoneNumber);

        resend_code = findViewById(R.id.resend_code);
        resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(phoneNumber);
                Toast.makeText(Registeration.this, "Code has been sent...", Toast.LENGTH_SHORT).show();
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
        if (verificationId != null && code != null)
        {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        }
        else {
            Toast.makeText(Registeration.this, "Some error occurred", Toast.LENGTH_SHORT).show();
        }

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
        jsonParams.put("complainant_cnic",user_cnic);
        jsonParams.put("complainant_gender",selection);
        jsonParams.put("complainant_email",email);
        jsonParams.put("complainant_council",council);
        jsonParams.put("complainant_address",address);
        jsonParams.put("complainant_district_id_fk",Integer.parseInt(dist_id));
        try {
            jsonParams.put("user_avatar",file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        jsonParams.setForceMultipartEntityContentType(true);

        Log.e("JSONPARAMS",jsonParams.toString());
        Log.d("file",file.toString());

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
        no_internet_layout = findViewById(R.id.no_internet_layout);
        bottomLayout = findViewById(R.id.bottomLayout);
        pinView=findViewById(R.id.editTextCode);
        editText = findViewById(R.id.editTextCode1);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);
        confirm_user_password = findViewById(R.id.confirm_user_password);
        district = findViewById(R.id.district_id);

        profile_image = findViewById(R.id.profile_image);

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