package com.example.fileuploader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;
    private Context mContext;
    private String mMimeType;

    public DriveServiceHelper(Drive mDriveService, Context context){
        this.mDriveService = mDriveService;
        this.mContext = context;
    }

    public void uploadFileToDrive(final Uri fileUri, final String mimeType) {
        mMimeType = mimeType;
        new UploadFileTask().execute(fileUri);
    }

    private class UploadFileTask extends AsyncTask<Uri, Void, String> {

        @Override
        protected String doInBackground(Uri... params) {
            Uri fileUri = params[0];
            try {
                InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    java.io.File fileContent = new java.io.File(mContext.getCacheDir(), getFileNameFromUri(fileUri));
                    FileOutputStream outputStream = new FileOutputStream(fileContent);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    FileContent mediaContent = new FileContent(mMimeType, fileContent);

                    File body = new File();
                    body.setName(getFileNameFromUri(fileUri));
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
        protected void onPostExecute(String fileId) {
            if (fileId != null) {
                Log.d("TAGSA", "File uploaded: " + fileId);
            } else {
                Log.d("TAGSA", "File upload failed");
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    fileName = cursor.getString(index);
                }
                cursor.close();
            }
        } else if (scheme != null && scheme.equals("file")) {
            fileName = new File().getName();
        }

        return fileName;
    }

}
