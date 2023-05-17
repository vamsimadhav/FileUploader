package com.example.fileuploader.Networking.Interface;

import com.example.fileuploader.Model.UploadResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("s3/uploadimages")
    Call<UploadResponse> uploadImages(
            @HeaderMap Map<String, String> headers,
            @Part MultipartBody.Part file,
            @Part("sub_dir1") RequestBody subDir1,
            @Part("sub_dir2") RequestBody subDir2
    );
}
