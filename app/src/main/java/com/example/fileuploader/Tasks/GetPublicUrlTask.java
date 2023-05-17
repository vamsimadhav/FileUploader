package com.example.fileuploader.Tasks;

import android.util.Log;
import android.os.AsyncTask;
import android.content.Context;
import android.app.ProgressDialog;

import com.example.fileuploader.Helper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.example.fileuploader.Interface.PublicUrlCallback;

public class GetPublicUrlTask extends AsyncTask<String, Void, String> {

    private final Drive mDriveService;
    private final PublicUrlCallback mCallback;
    private ProgressDialog progressDialog;
    private final Context mContext;

    public GetPublicUrlTask(Context mContext,Drive driveService, PublicUrlCallback callback) {
        this.mContext = mContext;
        this.mDriveService = driveService;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = Helper.progressHelper(mContext, "Fetching Public URL",ProgressDialog.STYLE_SPINNER);
    }

    @Override
    protected String doInBackground(String... params) {
        String fileId = params[0];
        String publicUrl = null;
        try {
            //Setting Permission's
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            //Not Visible through Google Search
            permission.setAllowFileDiscovery(false);

            // Insert the permission for the file
            mDriveService.permissions().create(fileId, permission).execute();

            // Get the file with the updated permissions
            File file = mDriveService.files().get(fileId).setFields("webViewLink").execute();

            // Retrieve the public URL
            publicUrl = file.getWebViewLink();

        } catch (Exception e) {
            Log.d("PUBLIC", e.getMessage());
            e.printStackTrace();
        }
        return publicUrl;
    }

    @Override
    protected void onPostExecute(String publicUrl) {
        progressDialog.dismiss();
        // Pass the publicUrl to the callback
        mCallback.onPublicUrlFetched(publicUrl);
    }
}
