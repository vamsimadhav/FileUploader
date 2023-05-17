package com.example.fileuploader.UI.Fragments;

import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;
import android.view.ViewGroup;
import android.content.Intent;
import android.content.Context;
import com.example.fileuploader.R;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.Scope;
import androidx.activity.result.ActivityResult;
import com.google.api.services.drive.DriveScopes;
import com.google.android.gms.common.api.ApiException;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class LoginFragment extends Fragment {
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button loginButton = getActivity().findViewById(R.id.login);

        mGoogleSignInClient = getGoogleSignInClient(getContext());

        loginButton.setOnClickListener(view1 -> {
            signIn();

        });
    }

    private ActivityResultLauncher<Intent> googleSignIn = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            handleSignInResult(task);
        }
    });

    private GoogleSignInClient getGoogleSignInClient(Context context){
       GoogleSignInOptions signInOptions =  new GoogleSignInOptions
                                               .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                               .requestEmail()
                                               .requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE))
                                               .build();

        return GoogleSignIn.getClient(context,signInOptions);
    }

    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            // Signed in successfully, show authenticated U
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            // Signed out, show unauthenticated UI.
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }

    private void signIn() {
        googleSignIn.launch(mGoogleSignInClient.getSignInIntent());
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_uploadFragment);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            updateUI(account);
        } else {
            updateUI(null);
        }
    }
}
