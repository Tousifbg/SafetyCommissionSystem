package com.example.publicsafetycomission.Helpers;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.publicsafetycomission.ComplaintArea;
import com.example.publicsafetycomission.Constant.API_Utils;
import com.example.publicsafetycomission.Dashboard;
import com.example.publicsafetycomission.Login;
import com.example.publicsafetycomission.ViewComplaints;
import com.example.publicsafetycomission.model.ComplaintModel;
import com.example.publicsafetycomission.model.LoginModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ApiController {
    private Context mContext;
    private AsyncHttpClient client;
    private ShowNow helper;
    private final String TAG = "ApiController";

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public ApiController(Context context) {
        this.mContext = context;
        helper = new ShowNow(mContext);

        //SHARED PREFERENCES TO CHECK IF USER ID IS ALREADY SAVED
        pref = mContext.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
    }

    private AsyncHttpClient getClient() {
        if (client == null) {

            final int DEFAULT_TIMEOUT = 20 * 1000;
            client = new AsyncHttpClient();
            client.setTimeout(1000000);
            client.setConnectTimeout(400000); // default is 10 seconds, minimum is 1 second
            client.setResponseTimeout(400000);
        }
        return client;
    }

    public void addComplaint(ComplaintModel complaintModel, List<File> imgList ,
                             final ApiCallback callBack) {

        File[] files = new File[imgList.size()];
        for (int i=0;i<imgList.size();i++){
            files[i]=imgList.get(i);
            Log.d("see files",files[i].toString());

        }
        final RequestParams jsonParams = new RequestParams();

        jsonParams.put("token",complaintModel.getToken());
        jsonParams.put("complaint_category_id_fk",complaintModel.getComplaint_category_id_fk());
        jsonParams.put("district_id_fk",complaintModel.getDistrict_id_fk());
        jsonParams.put("complaint_council",complaintModel.getComplaint_council());
        jsonParams.put("complaint_detail",complaintModel.getComplaint_detail());
        try {
            jsonParams.put("attachments[]",files);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        jsonParams.setForceMultipartEntityContentType(true);

        getClient().post(API_Utils.COMPLAINT_REGISTER, jsonParams,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.d("JSON_PARAMETERS",jsonParams.toString());
                        helper.showLoadingDialog(mContext);
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        int percent= (int) ((bytesWritten/totalSize)*100);
                        Log.d("check_progress",percent+ "here");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        helper.scheduleDismiss();
                        String json = new String(responseBody);
                        Log.e("SUCCESS", "onSuccess: " + json);
                        try {
                            JSONObject object=new JSONObject(json);
                            if(callBack!=null){
                                int api_res_success = object.getInt("response");
                                String api_res_success_msg = object.getString("response_msg");
                                if (api_res_success == 1){
                                    callBack.responseCallback(object.getInt("response"),
                                            object.getString("response_msg"),"");
                                    helper.desplayPositiveToast(mContext,api_res_success_msg);
                                    Toast.makeText(mContext, "Your complaint has been submitted",
                                            Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(mContext, Dashboard.class);
                                    mContext.startActivity(intent);
                                    onFinish();
                                }
                                else if (api_res_success == 0)
                                {
                                    helper.desplayErrorToast(mContext,api_res_success_msg);
                                    Toast.makeText(mContext, "FAILED", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        helper.scheduleDismiss();
                        String json = new String(responseBody);
                        Log.d("FAILURE", "onSuccess: " + json);
                        if(callBack!=null){
                            Toast.makeText(mContext, "FAILED", Toast.LENGTH_SHORT).show();
                        }

                    }
                    @Override
                    public void onCancel() {
                        super.onCancel();
                        if(callBack!=null){

                            callBack.responseCallback(false,"Request Cancelled","");
                        }

                    }
                });
    }

    public void login(LoginModel loginModel,
                      final ApiCallback callBack) {

        final RequestParams jsonParams = new RequestParams();

        jsonParams.put("user_name",loginModel.getUsername());
        jsonParams.put("user_password",loginModel.getPassword());

        getClient().post(API_Utils.LOGIN_URL, jsonParams,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.d("JSON_PARAMETERS",jsonParams.toString());
                        helper.showLoadingDialog(mContext);
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        int percent= (int) ((bytesWritten/totalSize)*100);
                        Log.d("check_progress",percent+ "here");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        helper.scheduleDismiss();
                        String json = new String(responseBody);
                        Log.d("SUCCESS", "onSuccess: " + json);
                        try {

                            JSONObject object=new JSONObject(json);
                            JSONObject jsonObject  = object.getJSONObject("data");

                            if (callBack != null)
                            {
                                int api_res_success = object.getInt("response");
                                String api_res_success_msg = object.getString("response_msg");
                                Log.d("api_res_success","response: " +api_res_success);
                                if (api_res_success == 1){
                                    callBack.responseCallback(object.getInt("response"),
                                            object.getString("response_msg"),"");
                                    helper.desplayPositiveToast(mContext,api_res_success_msg);

                                    String token = jsonObject.getString("token");
                                    Log.e("RESPONSE_DATA","token: " +token);

                                    editor = pref.edit();
                                    editor.putString("token", token);
                                    Log.e("SHARED_OK", "ok");
                                    editor.commit(); // commit changes

                                    goToNextScreen();
                                }
                                else if (api_res_success == 0)
                                {
                                    helper.desplayErrorToast(mContext,api_res_success_msg);
                                    Toast.makeText(mContext, "FAILED", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        helper.scheduleDismiss();
                        String json = new String(responseBody);
                        Log.d("FAILURE", "onSuccess: " + json);
                        if(callBack!=null){
                            Toast.makeText(mContext, "FAILED", Toast.LENGTH_SHORT).show();
                        }

                    }
                    @Override
                    public void onCancel() {
                        super.onCancel();
                        if(callBack!=null){

                            callBack.responseCallback(false,"Request Cancelled","");
                        }

                    }
                });
    }

    private void goToNextScreen() {
        Intent intent = new Intent(mContext,Dashboard.class);
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
