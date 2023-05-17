package com.example.fileuploader.UI.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.fileuploader.DriveServiceHelper;
import com.example.fileuploader.Helper;
import com.example.fileuploader.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class UploadFragment extends Fragment {
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

        documentPicker = registerForActivityResult(new CustomDocumentContract(), uri -> {
            if (uri != null) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(Helper.getDriveService(mAccount,getContext()),getContext());
                String mimeType = Helper.getMimeTypeFromUri(uri,getContext());
                driveServiceHelper.uploadFileToDrive(uri, mimeType, (success, fileId) -> {
                    if(success && fileId != null){
                       try {
                           NavController navController = Navigation.findNavController(getView());
                           Bundle args = new ShareFragmentArgs.Builder()
                                   .setFileId(fileId)
                                   .build()
                                   .toBundle();
                           navController.navigate(R.id.shareFragment,args);
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
}
