package com.example.fileuploader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.List;

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

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),WebViewActivity.class);
                intent.putExtra("publicUrl",textView.getText());
                startActivity(intent);
            }
        });

        Button shareButton = getActivity().findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareUrl(textView.getText().toString());
            }
        });
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

    private void shareUrl(String url){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);

// Check if there are any apps that can handle the sharing Intent
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(shareIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

// Start the sharing Intent if there are suitable apps available
        if (isIntentSafe) {
            startActivity(Intent.createChooser(shareIntent, "Share URL via"));
        } else {
            // Handle the case when no apps can handle the sharing Intent
            Toast.makeText(getContext(), "No apps available to handle sharing", Toast.LENGTH_SHORT).show();
        }
    }
}
