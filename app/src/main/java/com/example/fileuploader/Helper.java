package com.example.fileuploader;

import android.net.Uri;
import java.util.Collections;
import android.content.Context;
import android.database.Cursor;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.provider.OpenableColumns;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class Helper {

    public static final String[] mimeTypes = new String[]{
            "text/*",                                 // Text Document MIME
            "application/pdf" ,                       // PDF MIME
            "application/msword",                     // MS Word MIME
            "application/vnd.ms-excel",               // MS EXCEL MIME
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" //.xlxs MIME,
    };
    public static Drive getDriveService(Context mContext){
        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(mContext);
        if(mAccount != null){
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(mAccount.getAccount());
            return new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(mContext.getString(R.string.app_name))
                    .build();
        }
        return null;
    }

    public static String getMimeTypeFromUri(Uri uri,Context mContext) {
        ContentResolver contentResolver = mContext.getContentResolver();
        String mimeType = contentResolver.getType(uri);
        return mimeType;
    }

    public static String getFileNameFromUri(Uri uri, Context mContext) {
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

    public static ProgressDialog progressHelper(Context mContext,String message ,int style){
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(style);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }
}
