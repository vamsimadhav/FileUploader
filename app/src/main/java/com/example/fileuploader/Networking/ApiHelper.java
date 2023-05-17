package com.example.fileuploader.Networking;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import com.example.fileuploader.Helper;
import com.example.fileuploader.Model.UploadResponse;
import com.example.fileuploader.Networking.Interface.ApiCompletionCallback;
import com.example.fileuploader.Networking.Interface.ApiService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHelper {
    private static final String authToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjQzNzA5OTksImlzcyI6Imh0dHBzOi8vYmUxMi5wbGF0Zm9ybS5zaW1wbGlmaWkuY29tL2FwaS92MS9hZG1pbi9hdXRoZW50aWNhdGUiLCJpYXQiOjE2ODQyMTEyMTIsImV4cCI6MTc0NDY5MTIxMiwibmJmIjoxNjg0MjExMjEyLCJqdGkiOiJ3b29HeDFmZ0I2N1FGc0pJIn0.DF__mHMdlHIT6lQgfG76_h_LgrjL4D9u_ivTXGiTlBM";

    public static void uploadDocument(Context mContext, Uri fileUri, ApiCompletionCallback completionCallback){

        ProgressDialog progressDialog = Helper.progressHelper(mContext,"Uploding Document...",ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        // Create API service interface
        ApiService apiService =  ApiClient.getClient().create(ApiService.class);

        // Create form fields
        RequestBody subDir1 = RequestBody.create(MediaType.parse("multipart/form-data"), "android_assignment");
        RequestBody subDir2 = RequestBody.create(MediaType.parse("multipart/form-data"), "prateekg");

        Call<UploadResponse> call = apiService.uploadImages(Helper.getApiHeaders(authToken),Helper.getFile(mContext,fileUri),subDir1,subDir2);

        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if(response.isSuccessful()){
                    progressDialog.dismiss();
                    UploadResponse uploadResponse = response.body();
                    if (uploadResponse != null && uploadResponse.getResponse() != null && !uploadResponse.getResponse().getData().isEmpty()) {
                        String url = uploadResponse.getResponse().getData().get(0).getUrl();
                        completionCallback.onCompletion(url);
                    }
                } else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext,response.message(),Toast.LENGTH_SHORT).show();
                    completionCallback.onCompletion(null);
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                progressDialog.dismiss();
                completionCallback.onCompletion(null);
                Toast.makeText(mContext,"API Call Failed, Try Again",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
