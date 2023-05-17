package com.example.fileuploader.UI.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.fileuploader.DriveServiceHelper;
import com.example.fileuploader.Helper;
import com.example.fileuploader.Interface.PublicUrlCallback;
import com.example.fileuploader.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

public class UploadFragment extends Fragment {
    private ActivityResultLauncher<String[]> documentPicker;
    private GoogleSignInAccount mAccount;
    private String publicUrl;
    private final String[] mimeTypes = new String[]{
            "text/*",                                 // Text Document MIME
            "application/pdf" ,                       // PDF MIME
            "application/msword",                     // MS Word MIME
            "application/vnd.ms-excel",               // MS EXCEL MIME
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" //.xlxs MIME,
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button button = getActivity().findViewById(R.id.button);

        documentPicker = registerForActivityResult(new CustomDocumentContract(), uri -> {
            if (uri != null) {
                Drive mDriveService = Helper.getDriveService(mAccount,getContext());
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(mDriveService,getContext());
                String mimeType = Helper.getMimeTypeFromUri(uri,getContext());
                driveServiceHelper.uploadFileToDrive(uri, mimeType, (success, fileId) -> {
                    if(success && fileId != null){
                       try {
                           GetPublicUrlTask task = new GetPublicUrlTask(mDriveService, publicUrl -> {
                               if(publicUrl != null){
                                   this.publicUrl = publicUrl;
                                   NavController navController = Navigation.findNavController(getView());
                                   Bundle args = new ShareFragmentArgs.Builder()
                                           .setPublicUrl(publicUrl)
                                           .build()
                                           .toBundle();
                                   navController.navigate(R.id.shareFragment,args);
                               }
                           });
                           task.execute(fileId);
                       } catch (Exception e){
                           Log.d("Navigation",e.getMessage());
                           e.printStackTrace();
                       }
                    }
                });
            }
        });

        button.setOnClickListener(view1 -> documentPicker.launch(mimeTypes));
    }

    //Creating a Custom Contract for Allowing of Selection of {.docx, .xlxs, .txt } documents only
    private class CustomDocumentContract extends ActivityResultContracts.OpenDocument {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String[] input) {
            Intent intent = super.createIntent(context, input);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            return intent;
        }
    }

    private class GetPublicUrlTask extends AsyncTask<String, Void, String> {

        private Drive mDriveService;
        private PublicUrlCallback mCallback;

        public GetPublicUrlTask(Drive driveService, PublicUrlCallback callback) {
            this.mDriveService = driveService;
            this.mCallback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String fileId = params[0];
            String publicUrl = null;
            try {
                Permission permission = new Permission();
                permission.setType("anyone");
                permission.setRole("reader");
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
            // Pass the publicUrl to the callback
            mCallback.onPublicUrlFetched(publicUrl);
        }
    }
}
