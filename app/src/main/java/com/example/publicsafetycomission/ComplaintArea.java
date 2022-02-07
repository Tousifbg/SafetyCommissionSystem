package com.example.publicsafetycomission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Helpers.ApiCallback;
import com.example.publicsafetycomission.Helpers.ApiController;
import com.example.publicsafetycomission.Helpers.FileUtils;
import com.example.publicsafetycomission.Helpers.NetworkUtils;
import com.example.publicsafetycomission.Helpers.ShowNow;
import com.example.publicsafetycomission.adapters.CategoryAdapter;
import com.example.publicsafetycomission.adapters.DistrictAdapter;
import com.example.publicsafetycomission.databaseRef.DBHelperClass;
import com.example.publicsafetycomission.databaseRef.DataBaseConstant;
import com.example.publicsafetycomission.model.ComplaintModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ComplaintArea extends AppCompatActivity {

    private static final String TAG = "ComplaintArea";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    AppCompatButton submitComplaint;

    EditText complaint_detail,complaint_council,category_id,district_id;
    String comp_council,comp_detail;
    String token;

    private String cat_id, cat_name;
    private String dist_id, dist_name;

    ArrayList<HashMap<String,String>> getCategories = new ArrayList<HashMap<String, String>>();
    ArrayList<String> categ_id = new ArrayList<String>();
    ArrayList<String> categ_name = new ArrayList<String>();

    ArrayList<HashMap<String,String>> getDistricts = new ArrayList<HashMap<String, String>>();
    ArrayList<String> distrct_id = new ArrayList<String>();
    ArrayList<String> distrct_name = new ArrayList<String>();

    String temp[];
    String temp1[];

    DBHelperClass dbHelperClass;

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
    public static final int PICK_AUDIO = -2;
    public static final int PICK_VIDEO = -3;
    public static final int PICK_DOCS = -4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_area);

        initViews();

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        token = pref.getString("token", "No Data");
        Log.e("token",token);

        dbHelperClass = new DBHelperClass(this);
        try {
            getCategories = dbHelperClass.getCategoriesData();
            Log.e("CATEGORIES_Size", String.valueOf(getCategories.size()));
            if (getCategories.size() > 0) {
                temp1 = new String[getCategories.size()];
                for (int i = 0; i < getCategories.size(); i++) {
                    HashMap<String, String> map = getCategories.get(i);
                    categ_id.add( map.get(DataBaseConstant.TAG_CATEG_ID));
                    categ_name.add(map.get(DataBaseConstant.TAG_CATEG_NAME));
                    temp1[i]=map.get("project_name");
                }
            }
            else {
                Toast.makeText(ComplaintArea.this, "Categories data missing from API", Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
        }

        try {
            getDistricts = dbHelperClass.getDistrictData();
            Log.e("DIST_Size", String.valueOf(getDistricts.size()));
            if (getDistricts.size() > 0) {
                temp = new String[getDistricts.size()];
                for (int i = 0; i < getDistricts.size(); i++) {
                    HashMap<String, String> map = getDistricts.get(i);
                    distrct_id.add( map.get(DataBaseConstant.TAG_DIST_ID));
                    distrct_name.add(map.get(DataBaseConstant.TAG_DIST_NAME));
                    temp[i]=map.get("project_name2");
                }
            }
            else {
                Toast.makeText(ComplaintArea.this, "District data missing from API", Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());

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
                onBackPressed();
                layoutTransition();
            }
        });

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
                    category_id.setError("Select category");
                }
                else if (TextUtils.isEmpty(dist_id))
                {
                    district_id.setError("Select district");
                }
                else if (TextUtils.isEmpty(comp_council))
                {
                    complaint_council.setError("This field is required");
                }
                else if (TextUtils.isEmpty(comp_detail))
                {
                    complaint_detail.setError("This field is required");
                }
                else {
                    if (NetworkUtils.isNetworkConnected(ComplaintArea.this))
                    {
                        postComplaint();
                    }
                    else {
                        Toast.makeText(ComplaintArea.this, "No internet",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launchImageActivity.launch(Intent.createChooser(photoPickerIntent, "Pictures"));
        //startActivityForResult(Intent.createChooser(photoPickerIntent,"Pictures"), GALLERY_REQUEST);
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
                            Toast.makeText(ComplaintArea.this, "Image picked", Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            image_tick.setVisibility(View.VISIBLE);
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
                            Toast.makeText(ComplaintArea.this, "Video picked", Toast.LENGTH_SHORT).show();
                            //imageEdt.setText("Image Picked !");
                            layout_tick.setVisibility(View.VISIBLE);
                            videos_tick.setVisibility(View.VISIBLE);
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
                            Toast.makeText(ComplaintArea.this, "Audio picked",
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
                pickImage();
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
        ListView cattlist= (ListView) categoryDilaog.findViewById(R.id.countrylist);
        CategoryAdapter categoryAdapter= new CategoryAdapter(ComplaintArea.this,
                categ_id,categ_name,temp1);
        cattlist.setAdapter(categoryAdapter);
        categoryDilaog.show();

        cattlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                categoryDilaog.dismiss();
                category_id.setText(""+categ_name.get(i));
                cat_id = categ_id.get(i);
                Log.e("cat_id", cat_id);
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
        ListView distlist= (ListView) districtsDilaog.findViewById(R.id.countrylist);
        DistrictAdapter districtAdapter= new DistrictAdapter(ComplaintArea.this,
                distrct_id,distrct_name,temp);
        distlist.setAdapter(districtAdapter);
        districtsDilaog.show();

        distlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                districtsDilaog.dismiss();
                district_id.setText(""+distrct_name.get(i));
                dist_id = distrct_id.get(i);
                Log.e("dist_id", dist_id);
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

        complaintModel = new ComplaintModel();
        apiController = new ApiController(this);
        showNow = new ShowNow(this);
    }

    private void layoutTransition() {
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}