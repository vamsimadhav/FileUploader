package com.example.fileuploader.UI.Fragments;

import java.util.List;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.TextView;
import com.example.fileuploader.R;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import android.content.pm.ResolveInfo;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.content.pm.PackageManager;
import androidx.navigation.NavController;

public class ShareFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_share,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Args to get public url
        ShareFragmentArgs args = ShareFragmentArgs.fromBundle(getArguments());
        String publicUrl = args.getPublicUrl();

        TextView textView = getActivity().findViewById(R.id.public_url);
        textView.setText(publicUrl);

        textView.setOnClickListener(view12 -> {
            NavController controller = Navigation.findNavController(getView());
            Bundle args2 = new WebViewFragmentArgs.Builder()
                    .setPublicUrl(publicUrl)
                    .build()
                    .toBundle();
            controller.navigate(R.id.webViewFragment,args2);
        });

        Button shareButton = getActivity().findViewById(R.id.shareButton);
        shareButton.setOnClickListener(view1 -> shareUrl(publicUrl));
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
