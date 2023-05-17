package com.example.fileuploader.Custom;

import android.content.Intent;
import android.content.Context;
import androidx.annotation.NonNull;
import com.example.fileuploader.Helper;
import androidx.activity.result.contract.ActivityResultContracts;
//Creating a Custom Contract for Allowing of Selection of {.docx, .xlxs, .txt } documents only
public class CustomDocumentContract extends ActivityResultContracts.OpenDocument {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull String[] input) {
        Intent intent = super.createIntent(context, input);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, Helper.mimeTypes);
        return intent;
    }
}
