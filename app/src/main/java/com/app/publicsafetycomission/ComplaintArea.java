package com.app.publicsafetycomission;

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
import android.media.ThumbnailUtils;
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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.app.publicsafetycomission.model.DistrictModel;
import com.app.publicsafetycomission.Constant.API_Utils;
import com.app.publicsafetycomission.Helpers.ApiCallback;
import com.app.publicsafetycomission.Helpers.ApiController;
import com.app.publicsafetycomission.Helpers.FileUtils;
import com.app.publicsafetycomission.Helpers.NetworkUtils;
import com.app.publicsafetycomission.Helpers.ShowNow;
import com.app.publicsafetycomission.adapters.CategoryAdapter2;
import com.app.publicsafetycomission.adapters.DistrictAdapter2;
import com.app.publicsafetycomission.model.CategoryModel;
import com.app.publicsafetycomission.model.ComplaintModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class ComplaintArea extends AppCompatActivity {
    private FirebaseAnalytics firebaseAnalytics;

    private static final String TAG = "ComplaintArea";
    private static final int CAMERA_REQUEST_CODE = 1058;
    private static final int STORAGE_REQUEST_CODE = 1059;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Button submitComplaint;

    EditText complaint_detail,complaint_council,category_id,district_id;
    String comp_council,comp_detail;
    String token;

    private String cat_id, cat_name;
    private String dist_id, dist_name;

    ArrayList<DistrictModel> districtMODELS = new ArrayList<>();
    private DistrictAdapter2 districtAdapter2;

    ArrayList<CategoryModel> categoryModels = new ArrayList<>();
    private CategoryAdapter2 categoryAdapter2;

    AsyncHttpClient client;

    ImageView image_picker,videos_picker,doc_picker,audios_picker,
            image_tick,videos_tick,doc_tick,audios_tick,btnBack;
    LinearLayout layout_tick;

    private ArrayList<Uri> uri=new ArrayList<>();
    FileUtils fileUtils;
    List<File> filesList = new ArrayList<>();

    private ComplaintModel complaintModel;
    private ApiController apiController;
    private ShowNow showNow;

    public static final int PICK_IMG = -1;
    public static final int PICK_CAM_IMG = -50;
    public static final int PICK_AUDIO = -2;
    public static final int PICK_VIDEO = -3;
    public static final int PICK_DOCS = -4;

    private LinearLayout no_internet_layout,complaint_layout;
    private TextView dismiss_net_layout;

    private ScrollView scrollBar;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private static final int pic_id = 123;
    Uri cam_uri;

    private RotateAnimation rotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_area);

        initViews();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
         pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);


        if (NetworkUtils.isNetworkConnected(ComplaintArea.this))
        {
            fetchDistrictsFromAPI();
            fetchCategoriesFromAPI();
        }else {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
        }

        district_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDistrictList();
            }
        });

        category_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoriesList();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                layoutTransition();
            }
        });

        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        hasStoragePermission(1);
        image_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        videos_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideo();
            }
        });

        doc_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDocs();
            }
        });

        audios_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAudio();
            }
        });

        submitComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comp_council = complaint_council.getText().toString();
                comp_detail = complaint_detail.getText().toString();
                if (TextUtils.isEmpty(cat_id))
                {
                    Toast.makeText(ComplaintArea.this, "Select category first", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(dist_id))
                {
                    Toast.makeText(ComplaintArea.this, "Select district first", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(comp_council))
                {
                    complaint_council.setError("This field is required");
                    complaint_council.requestFocus();
                    return;
                }
                else if (TextUtils.isEmpty(comp_detail))
                {
                    complaint_detail.setError("This field is required");
                    complaint_detail.requestFocus();
                    return;
                }
                else {
                    if (NetworkUtils.isNetworkConnected(ComplaintArea.this))
                    {
                        postComplaint();
                    }
                    else {
                        no_internet_layout.setVisibility(View.VISIBLE);
                        complaint_layout.setVisibility(View.GONE);

                        dismiss_net_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (NetworkUtils.isNetworkConnected(ComplaintArea.this))
                                {
                                    no_internet_layout.setVisibility(View.GONE);
                                    complaint_layout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    no_internet_layout.setVisibility(View.VISIBLE);
                                    complaint_layout.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            }
        });

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


        //rotate animation
        rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1500);
        rotateAnimation.setRepeatCount(Animation.ABSOLUTE);
        image_picker.startAnimation(rotateAnimation);
        videos_picker.startAnimation(rotateAnimation);
        doc_picker.startAnimation(rotateAnimation);
        audios_picker.startAnimation(rotateAnimation);
    }

    private void postComplaint() {
        complaintModel.setToken(token);
        complaintModel.setComplaint_category_id_fk(Integer.parseInt(cat_id));
        complaintModel.setDistrict_id_fk(Integer.parseInt(dist_id));
        complaintModel.setComplaint_council(comp_council);
        complaintModel.setComplaint_detail(comp_detail);
        complaintModel.setUpload_file(filesList);
        Log.d("SIZE_OF_LIST",filesList.size()+"");

        apiController.addComplaint(complaintModel, filesList, new ApiCallback() {
            @Override
            public void responseCallback(boolean success, String message, String s) {
            }

            @Override
            public void responseCallback(int success, String message, String s) {
                Log.d("SUCCESS_MSG", String.valueOf(success));
                if (success == 1)
                {

                }
                else {
                    showNow.desplayErrorToast(ComplaintArea.this, message);
                }
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

    private void pickFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launchImageActivity.launch(Intent.createChooser(photoPickerIntent, "Pictures"));
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cam_uri = ComplaintArea.this.getContentResolver().
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri);

        //startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE); // OLD WAY
        startCamera.launch(cameraIntent);                // VERY NEW WAY
    }

    private void pickDocs() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        launchDocActivity.launch(Intent.createChooser(intent, "Select PDF"));
        //startActivityForResult(Intent.createChooser(intent, "Select Pdf"), DOCS_FILE_REQUEST);
    }

    private void pickVideo() {
        Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        videoIntent.setType("video/*");
        videoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        launchVideoActivity.launch(Intent.createChooser(videoIntent, "Select Your Videos"));
        //startActivityForResult(Intent.createChooser(videoIntent,"Select Your Videos"), VIDEO_FILE_REQUEST);
    }

    private void pickAudio() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("audio/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        launchAudiosActivity.launch(Intent.createChooser(galleryIntent, "Select your Audio"));
        //startActivityForResult(Intent.createChooser(galleryIntent,"Select Your Audio"), AUDIO_FILE_REQUEST);
    }


    //new way for onActivityResult
    //For images
    ActivityResultLauncher<Intent> launchImageActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            // Get a list of picked images

                            for (int i = 0; i < count; i++) {
                                uri.add(data.getClipData().getItemAt(i).getUri());
                                String imgPath = fileUtils.getPath(ComplaintArea.this,
                                        data.getClipData().getItemAt(i).getUri());

                                filesList.add(new File(imgPath));

                            }
                            for (File f : filesList) {
                                if (f.exists()) {
                                    Log.i(TAG, "exist");
                                }
                            }
                            Toast.makeText(ComplaintArea.this, "Multiple images picked", Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            image_tick.setVisibility(View.VISIBLE);
                            image_tick.setImageDrawable(getResources().getDrawable(R.drawable.basic_tick));
                            Log.d("lenght",uri.size()+"");
                        }
                        else{
                            Uri image_uri = data.getData();
                            try {
                                String imgPath = fileUtils.getPath(ComplaintArea.this, image_uri);
                                Log.e("FILES",imgPath);

                                if (imgPath != null){
                                    filesList.add(new File(imgPath));

                                    Toast.makeText(ComplaintArea.this, "Image picked", Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.VISIBLE);
                                    image_tick.setVisibility(View.VISIBLE);

                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ComplaintArea.this.
                                            getContentResolver(), image_uri);

                                    image_tick.setImageBitmap(bitmap);

                                } else {
                                    Toast.makeText(ComplaintArea.this, "Image not picked",
                                            Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image");
                                    layout_tick.setVisibility(View.GONE);
                                    image_tick.setVisibility(View.INVISIBLE);
                                }
                            } catch (Exception e) {
                                Log.i("TAG", "Some exception " + e);
                            }
                        }
                    }
                }
            });

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
                            String imgPath = fileUtils.getPath(ComplaintArea.this, cam_uri);
                            Log.e("FILES",imgPath);

                            if (imgPath != null){
                                filesList.add(new File(imgPath));

                                Toast.makeText(ComplaintArea.this, "Image picked", Toast.LENGTH_SHORT).show();
                                //imageEdt.setText("Image Picked !");
                                layout_tick.setVisibility(View.VISIBLE);
                                image_tick.setVisibility(View.VISIBLE);

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ComplaintArea.this.
                                        getContentResolver(), cam_uri);

                                image_tick.setImageBitmap(bitmap);

                            } else {
                                Toast.makeText(ComplaintArea.this, "Image not picked",
                                        Toast.LENGTH_SHORT).show();
                                //imageEdt.setText("Image");
                                layout_tick.setVisibility(View.GONE);
                                image_tick.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    }
                }
            });


    //new way for onActivityResult
    //For videos
    ActivityResultLauncher<Intent> launchVideoActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            // Get a list of picked images

                            for (int i = 0; i < count; i++) {
                                uri.add(data.getClipData().getItemAt(i).getUri());
                                String videoPath = fileUtils.getPath(ComplaintArea.this,
                                        data.getClipData().getItemAt(i).getUri());
                                filesList.add(new File(videoPath));

                            }
                            for (File f : filesList) {
                                if (f.exists()) {
                                    Log.i(TAG, "exist");
                                }
                            }

                            Toast.makeText(ComplaintArea.this, "Multiple videos picked", Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            videos_tick.setVisibility(View.VISIBLE);
                            videos_tick.setImageDrawable(getResources().getDrawable(R.drawable.basic_tick));

                            Log.d("lenght",uri.size()+"");
                        }
                        else
                        {
                            try {
                                Uri video_uri = data.getData();
                                String videoPath = fileUtils.getPath(ComplaintArea.this, video_uri);
                                File file = FileUtils.getFile(ComplaintArea.this,video_uri);

                                Log.e("FILES",videoPath);
                                if (videoPath != null){
                                    //videoEdt.setText("Video Picked");
                                    filesList.add(new File(videoPath));
                                    Toast.makeText(ComplaintArea.this, "Video picked", Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.VISIBLE);
                                    videos_tick.setVisibility(View.VISIBLE);

                                    Bitmap bmThumbnail = ThumbnailUtils.
                                            createVideoThumbnail(videoPath,
                                                    MediaStore.Images.Thumbnails.MINI_KIND);
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ComplaintArea.this.
                                            getContentResolver(), video_uri);

                                    videos_tick.setImageBitmap(bmThumbnail);
                                } else {
                                    //videoEdt.setText("Video");
                                    Toast.makeText(ComplaintArea.this, "Video not picked", Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.GONE);
                                    videos_tick.setVisibility(View.INVISIBLE);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });


    //new way for onActivityResult
    //For documents
    ActivityResultLauncher<Intent> launchDocActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            // Get a list of picked images
                            Log.e("count", String.valueOf(count));
                            for (int i = 0; i < count; i++) {
                                uri.add(data.getClipData().getItemAt(i).getUri());
                                String attachmentPath = fileUtils.getPath(ComplaintArea.this,
                                        data.getClipData().getItemAt(i).getUri());
                                filesList.add(new File(attachmentPath));
                            }
                            for (File f : filesList) {
                                if (f.exists()) {
                                    Log.i(TAG, "exist");
                                }
                            }
                            Toast.makeText(ComplaintArea.this, "Doc picked",
                                    Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            doc_tick.setVisibility(View.VISIBLE);
                            Log.d("lenght", uri.size() + "");
                        } else {
                            try {
                                Uri attachmenturi = data.getData();
                                Log.e("URI", String.valueOf(attachmenturi));
                                String attachmentPath = fileUtils.getPath(ComplaintArea.this,
                                        attachmenturi);
                                File file = FileUtils.getFile(ComplaintArea.this, attachmenturi);

                                Log.e("FILES", attachmentPath);
                                if (attachmentPath != null) {
                                    //attachmentEdt.setText("Attachment Picked");
                                    filesList.add(new File(attachmentPath));
                                    Toast.makeText(ComplaintArea.this, "Doc picked",
                                            Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.VISIBLE);
                                    doc_tick.setVisibility(View.VISIBLE);
                                } else {
                                    //attachmentEdt.setText("Attachment");
                                    Toast.makeText(ComplaintArea.this, "Doc not picked",
                                            Toast.LENGTH_SHORT).show();
                                    layout_tick.setVisibility(View.GONE);
                                    doc_tick.setVisibility(View.INVISIBLE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });


    //new way for onActivityResult
    //For audios
    ActivityResultLauncher<Intent> launchAudiosActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            // Get a list of picked images

                            for (int i = 0; i < count; i++) {
                                uri.add(data.getClipData().getItemAt(i).getUri());
                                String audioPath = fileUtils.getPath(ComplaintArea.this,
                                        data.getClipData().getItemAt(i).getUri());
                                filesList.add(new File(audioPath));
                            }
                            for (File f : filesList) {
                                if (f.exists()) {
                                    Log.i(TAG, "exist");
                                }
                            }
                            Toast.makeText(ComplaintArea.this, "Multiple audios picked",
                                    Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            audios_tick.setVisibility(View.VISIBLE);
                            Log.d("lenght",uri.size()+"");
                        }
                        else{
                            try {
                                Uri audio_uri = data.getData();
                                String audioPath = fileUtils.getPath(ComplaintArea.this, audio_uri);
                                Log.e("FILES",audioPath);
                                if (audioPath != null){
                                    //audioEdt.setText("Audio Picked");
                                    filesList.add(new File(audioPath));
                                    Toast.makeText(ComplaintArea.this, "Audio picked",
                                            Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.VISIBLE);
                                    audios_tick.setVisibility(View.VISIBLE);

                                } else {
                                    //audioEdt.setText("Audio");
                                    Toast.makeText(ComplaintArea.this, "Audio not picked",
                                            Toast.LENGTH_SHORT).show();
                                    //imageEdt.setText("Image Picked !");
                                    layout_tick.setVisibility(View.GONE);
                                    audios_tick.setVisibility(View.INVISIBLE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

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
            if (requestCode == PICK_AUDIO)
                pickAudio();
            if (requestCode == PICK_VIDEO)
                pickVideo();
        } if (requestCode == PICK_DOCS)
            pickDocs();
    }

    private void showCategoriesList() {
        category_id.requestFocus();
        final Dialog categoryDilaog = new Dialog(ComplaintArea.this, R.style.dialog_theme);
        categoryDilaog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        categoryDilaog.setCancelable(true);
        categoryDilaog.setContentView(R.layout.cat_dialog);
        categoryDilaog.getWindow().setBackgroundDrawable(new ColorDrawable(0x7f000000));
        EditText searchBar = (EditText) categoryDilaog.findViewById(R.id.searchBar);
        RecyclerView catList= (RecyclerView) categoryDilaog.findViewById(R.id.districtList);
        catList.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        catList.setLayoutManager(layoutManager);
        categoryDilaog.show();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    categoryAdapter2 = new CategoryAdapter2(ComplaintArea.this,
                            categoryModels,categoryDilaog);
                    catList.setAdapter(categoryAdapter2);
                    categoryAdapter2.notifyDataSetChanged();
                    //district_id.setText(""+distrct_name.get(i));
                }
            });
        } catch (Exception e) {
            Log.e("ERRR", e.getMessage());
            String error = e.getMessage().toString();
            Toast.makeText(ComplaintArea.this, error,
                    Toast.LENGTH_SHORT).show();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                categoryAdapter2.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showDistrictList() {
        district_id.requestFocus();
        final Dialog districtsDilaog = new Dialog(ComplaintArea.this, R.style.dialog_theme);
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
                    districtAdapter2 = new DistrictAdapter2(ComplaintArea.this,
                            districtMODELS,districtsDilaog);
                    districtList.setAdapter(districtAdapter2);
                    districtAdapter2.notifyDataSetChanged();
                    //district_id.setText(""+distrct_name.get(i));
                }
            });
        } catch (Exception e) {
            Log.e("ERRR", e.getMessage());
            String error = e.getMessage().toString();
            Toast.makeText(ComplaintArea.this, error,
                    Toast.LENGTH_SHORT).show();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                districtAdapter2.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void recyclerTouchMethod(String id, String name) {
        district_id.setText(""+name);
        dist_id = id;
        Log.e("DISTRICT_values","name: "+district_id.getText().toString()+
                "\nid: "+dist_id);
    }

    public void recyclerTouchMethod2(String id, String name) {
        category_id.setText(""+name);
        cat_id = id;
        Log.e("CATEGORY_values","name: "+category_id.getText().toString()+
                "\nid: "+cat_id);
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
        submitComplaint = findViewById(R.id.submitComplaint);

        image_picker = findViewById(R.id.image_picker);
        videos_picker = findViewById(R.id.videos_picker);
        doc_picker = findViewById(R.id.doc_picker);
        audios_picker = findViewById(R.id.audios_picker);
        image_tick = findViewById(R.id.image_tick);
        videos_tick = findViewById(R.id.videos_tick);
        doc_tick = findViewById(R.id.doc_tick);
        audios_tick = findViewById(R.id.audios_tick);

        layout_tick = findViewById(R.id.layout_tick);

        btnBack = findViewById(R.id.btnBack);
        complaint_detail = findViewById(R.id.complaint_detail);
        complaint_council = findViewById(R.id.complaint_council);
        category_id = findViewById(R.id.category_id);
        district_id = findViewById(R.id.district_id);

        no_internet_layout = findViewById(R.id.no_internet_layout);
        complaint_layout = findViewById(R.id.complaint_layout);
        dismiss_net_layout = findViewById(R.id.dismiss_net_layout);

        complaintModel = new ComplaintModel();
        apiController = new ApiController(this);
        showNow = new ShowNow(this);

        scrollBar = findViewById(R.id.scrollBar);
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
                    Toast.makeText(ComplaintArea.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(ComplaintArea.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
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

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);

                        cat_id = jsonObjectNew.getString("complaint_category_id");
                        cat_name = jsonObjectNew.getString("complaint_category_name");
                        Log.d("RESPONSE_DATA","cat_id: " +cat_id+ "\ncat_name: " +cat_name);


                        CategoryModel model = new CategoryModel(cat_id,cat_name);
                        categoryModels.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ComplaintArea.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String json = new String(responseBody);
                Log.e("REPONSE2", "onSuccess: " + json);
                Toast.makeText(ComplaintArea.this, json, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                super.onCancel();
            }
        });
    }
}