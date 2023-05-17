package com.example.fileuploader;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.fileuploader.Interface.UploadListener;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DriveServiceHelper {
    private final Drive mDriveService;
    private final Context mContext;
    private String mMimeType;
    private UploadListener uploadListener;

    public DriveServiceHelper(Drive mDriveService, Context context){
        this.mDriveService = mDriveService;
        this.mContext = context;
    }

    public void uploadFileToDrive(final Uri fileUri, final String mimeType,UploadListener uploadListener) {
        this.uploadListener = uploadListener;
        mMimeType = mimeType;
        new UploadFileTask().execute(fileUri);
    }

    private class UploadFileTask extends AsyncTask<Uri, Integer, String> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Helper.progressHelper(mContext);
        }

        @Override
        protected String doInBackground(Uri... params) {
            Uri fileUri = params[0];
            try {
                InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    java.io.File fileContent = new java.io.File(mContext.getCacheDir(),Helper.getFileNameFromUri(fileUri,mContext));
                    FileOutputStream outputStream = new FileOutputStream(fileContent);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;
                    long totalBytes = inputStream.available();
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int progress = (int) ((totalBytesRead * 100) / totalBytes);
                        publishProgress(progress);
                    }
                    outputStream.close();
                    inputStream.close();

                    FileContent mediaContent = new FileContent(mMimeType, fileContent);

                    File body = new File();
                    body.setName(Helper.getFileNameFromUri(fileUri,mContext));
                    body.setMimeType(mMimeType);

                    File uploadedFile = mDriveService.files().create(body, mediaContent).execute();
                    Log.d("MSD", "File uploaded: " + uploadedFile.getId());

                    return uploadedFile.getId();
                } else {
                    Log.d("TAGSA", "Input stream is null");
                    return null;
                }
            } catch (IOException e) {
                Log.d("AAA", e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String fileId) {
            progressDialog.dismiss();
            if (fileId != null) {
                uploadListener.onCompletion(true,fileId);
                Toast.makeText(mContext, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                Log.d("TAGSA", "File uploaded: " + fileId);
            } else {
                uploadListener.onCompletion(false,null);
                Toast.makeText(mContext, "File upload failed", Toast.LENGTH_SHORT).show();
                Log.d("TAGSA", "File upload failed");
            }
        }
    }
}

