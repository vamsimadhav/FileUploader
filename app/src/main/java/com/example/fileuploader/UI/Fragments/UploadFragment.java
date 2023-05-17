package com.example.fileuploader.UI.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import com.example.fileuploader.R;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.fileuploader.Helper;
import androidx.navigation.NavController;
import com.google.api.services.drive.Drive;
import com.example.fileuploader.DriveServiceTask;
import com.example.fileuploader.GetPublicUrlTask;
import androidx.activity.result.ActivityResultLauncher;
import com.example.fileuploader.Custom.CustomDocumentContract;

public class UploadFragment extends Fragment {

      @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button button = getActivity().findViewById(R.id.button);

        button.setOnClickListener(view1 -> documentPicker.launch(Helper.mimeTypes));
    }

    private final ActivityResultLauncher<String[]> documentPicker = registerForActivityResult(new CustomDocumentContract(), uri -> {
        if (uri != null) {
            Drive mDriveService = Helper.getDriveService(getContext());
            DriveServiceTask driveServiceTask = new DriveServiceTask(mDriveService,getContext());
            String mimeType = Helper.getMimeTypeFromUri(uri,getContext());
            driveServiceTask.uploadFileToDrive(uri, mimeType, (success, fileId) -> {
                if(success && fileId != null){
                    try {
                        GetPublicUrlTask task = new GetPublicUrlTask(getContext(),mDriveService, publicUrl -> {
                            if(publicUrl != null){
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
}
