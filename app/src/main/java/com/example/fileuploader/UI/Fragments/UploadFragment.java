package com.example.fileuploader.UI.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import com.example.fileuploader.Networking.Interface.ApiCompletionCallback;
import com.example.fileuploader.Networking.ApiHelper;
import com.example.fileuploader.R;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fileuploader.Helper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
            uploadFile(uri);
        }
    });

      private void uploadFile(Uri fileUri){
          ApiHelper.uploadDocument(getContext(), fileUri, new ApiCompletionCallback() {
              @Override
              public void onCompletion(String publicUrl) {
                  if(publicUrl != null){
                      NavController navController = Navigation.findNavController(getView());
                      Bundle args = new ShareFragmentArgs.Builder()
                              .setPublicUrl(publicUrl)
                              .build()
                              .toBundle();
                      navController.navigate(R.id.shareFragment,args);
                  }
              }
          });
      }
}
