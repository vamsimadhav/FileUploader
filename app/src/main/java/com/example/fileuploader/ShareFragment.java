package com.example.fileuploader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

public class ShareFragment extends Fragment {
    private String fileId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_share,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        TextView textView = getActivity().findViewById(R.id.public_url);
        ShareFragmentArgs args = ShareFragmentArgs.fromBundle(getArguments());
        fileId = args.getFileId();
        Drive mDriveService = Helper.getDriveService(mAccount,getContext());
        GetPublicUrlTask task = new GetPublicUrlTask(mDriveService, publicUrl -> {
            if(publicUrl != null){
                textView.setText(publicUrl);
            }
        });
        task.execute(fileId);
    }

    public interface GetPublicUrlCallback {
        void onPublicUrlFetched(String publicUrl);
    }

    private class GetPublicUrlTask extends AsyncTask<String, Void, String> {

        private Drive mDriveService;
        private GetPublicUrlCallback mCallback;

        public GetPublicUrlTask(Drive driveService, GetPublicUrlCallback callback) {
            this.mDriveService = driveService;
            this.mCallback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String fileId = params[0];
            String publicUrl = null;
            try {
                File file = mDriveService.files().get(fileId).setFields("webViewLink").execute();
                publicUrl = file.getWebViewLink();
            } catch (Exception e) {
                Log.d("PUBLIC", e.getMessage());
                e.printStackTrace();
            }
            return publicUrl;
        }

        @Override
        protected void onPostExecute(String publicUrl) {
            // Pass the publicUrl to the callback
            mCallback.onPublicUrlFetched(publicUrl);
        }
    }
}
