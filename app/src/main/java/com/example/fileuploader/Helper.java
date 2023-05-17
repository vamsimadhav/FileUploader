package com.example.fileuploader;

import android.net.Uri;
import android.content.Context;
import android.database.Cursor;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Helper {

    public static final String[] mimeTypes = new String[]{
            "application/pdf" ,                       // PDF MIME
            "application/msword",                     // MS Word MIME
            "application/vnd.ms-excel",               // MS EXCEL MIME
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",//.xlxs MIME,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    };

    public static String getFileNameFromUri(Uri uri, ContentResolver contentResolver) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    fileName = cursor.getString(index);
                }
                cursor.close();
            }
        }
        return fileName;
    }

    public static byte[] getBytesFromInputStream(InputStream inputStream) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer.toByteArray();
    }

    public  static Map<String,String> getApiHeaders(String authToken){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-IN,en-GB;q=0.9,en;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("X-Requested-With", "XMLHttpRequest");
        return  headers;
    }

    public static MultipartBody.Part getFile(Context mContext, Uri fileUri){
        // Create file part
        ContentResolver contentResolver = mContext.getContentResolver();
        String fileName = Helper.getFileNameFromUri(fileUri, contentResolver);
        String mimeType = contentResolver.getType(fileUri);
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), Helper.getBytesFromInputStream(inputStream));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("files[0]", fileName, requestBody);
        return filePart;
    }

    public static ProgressDialog progressHelper(Context mContext,String message ,int style){
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(style);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }
}
