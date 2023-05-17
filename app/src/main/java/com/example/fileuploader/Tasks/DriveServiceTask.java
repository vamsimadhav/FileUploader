package com.example.fileuploader.Tasks;

import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import android.widget.Toast;
import android.os.AsyncTask;
import android.content.Context;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import android.app.ProgressDialog;
import com.example.fileuploader.Helper;
import com.google.api.services.drive.Drive;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.example.fileuploader.Interface.UploadListener;
import com.google.api.services.drive.model.FileList;

public class DriveServiceTask {
    private final Drive mDriveService;
    private final Context mContext;
    private String mMimeType;
    private UploadListener uploadListener;

    public DriveServiceTask(Drive mDriveService, Context context){
        this.mDriveService = mDriveService;
        this.mContext = context;
    }

    public void uploadFileToDrive(final Uri fileUri, final String mimeType,UploadListener uploadListener) {
        this.uploadListener = uploadListener;
        mMimeType = mimeType;
        new UploadFileTask().execute(fileUri);
    }

    private class UploadFileTask extends AsyncTask<Uri, Integer, String> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Helper.progressHelper(mContext,"Uploading File",ProgressDialog.STYLE_HORIZONTAL);
        }

        @Override
        protected String doInBackground(Uri... params) {
            Uri fileUri = params[0];
            try {
                // Check if the folder exists
                String folderName = "FileUploader";
                String folderId = getFolderIdByName(folderName);

                // If the folder doesn't exist, create it
                if (folderId == null) {
                    folderId = createFolder(folderName, null);
                }
                InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    //Fetching the Folder Name using the helper
                    String fileName = Helper.getFileNameFromUri(fileUri,mContext);
                    java.io.File fileContent = new java.io.File(mContext.getCacheDir(), fileName);
                    FileOutputStream outputStream = new FileOutputStream(fileContent);

                    //Reading and Outputting the contents of the file
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;
                    long totalBytes = inputStream.available();
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int progress = (int) ((totalBytesRead * 100) / totalBytes);
                        publishProgress(progress);
                    }
                    outputStream.close();
                    inputStream.close();
                    //Setting the type of filecontent to be saved on drive
                    FileContent mediaContent = new FileContent(mMimeType, fileContent);

                    //Creating the file to be uploaded and setting its parameters
                    File body = new File();
                    List<String> parents = Collections.singletonList(folderId);
                    body.setParents(parents); // Setting that it should be inside a particular folder
                    body.setName(fileName); //Setting folder name
                    body.setMimeType(mMimeType); //Setting the File Type ie.. .docx, .xlxs, .txt

                    //Using drive service to upload the file to cloud
                    File uploadedFile = mDriveService.files().create(body, mediaContent).execute();
                    Log.d("SUCCESS", "File uploaded: " + uploadedFile.getId());

                    return uploadedFile.getId();
                } else {
                    Log.d("FILE ISSUE FAILURE", "Input stream is null");
                    return null;
                }
            } catch (IOException e) {
                Log.d("FAILURE", e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String fileId) {
            progressDialog.dismiss();
            if (fileId != null) {
                uploadListener.onCompletion(true,fileId);
                Toast.makeText(mContext, "File uploaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                uploadListener.onCompletion(false,null);
                Toast.makeText(mContext, "File upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFolderIdByName(String folderName) {
        try {
            //Using inbuilt file search and querying for the file if present
            FileList fileList = mDriveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'")
                    .execute();
            List<File> folders = fileList.getFiles();
            if (folders != null && !folders.isEmpty()) {
                return folders.get(0).getId();
            }
        } catch (IOException e) {
            Log.d("FolderName Issue", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String createFolder(String folderName, String parentFolderId) {
        try {
            //Creating the folder in local
            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            if (parentFolderId != null) {
                List<String> parents = Collections.singletonList(parentFolderId);
                folderMetadata.setParents(parents);
            }
            //Telling the drive to create in cloud
            File createdFolder = mDriveService.files().create(folderMetadata).execute();
            return createdFolder.getId();
        } catch (IOException e) {
            Log.d("FolderId Issue", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

