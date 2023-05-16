package com.example.fileuploader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class UploadFragment extends Fragment {

    private static final String FILE_NAME_BACKUPP = "File_Uploader";
    private ActivityResultLauncher<String[]> documentPicker;
    private GoogleSignInAccount mAccount;
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

        documentPicker = registerForActivityResult(new CustomDocumentContract(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    DriveServiceHelper driveServiceHelper = new DriveServiceHelper(getDriveService(),getContext());
                    driveServiceHelper.uploadFileToDrive(uri, "application/pdf");
                }
            }
        });

        button.setOnClickListener(view1 -> documentPicker.launch(mimeTypes));
    }

    private Drive getDriveService(){
        mAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if(mAccount != null){
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(mAccount.getAccount());
            return new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(getString(R.string.app_name))
                    .build();
        }
        return null;
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
}
