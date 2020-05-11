package com.precious.apps.cameraapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CAPTURE_CODE = 45;
    final int REQUEST_PERMISSIONS_CODE=1;

    ImageView imageView;
    Button deletePhoto;
    Button uploadPhoto;
    Button takePhoto;
    Button retry;
    ProgressBar progressBar;

    String documentPhotoPath;

    boolean photoTaken= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView= findViewById(R.id.imageView);
        deletePhoto=findViewById(R.id.delete_photo);
        uploadPhoto = findViewById(R.id.upload_photo);
        takePhoto=findViewById(R.id.tap_to_take_photo);
        progressBar = findViewById(R.id.progressBar);
        retry = findViewById(R.id.retry);

        if(!photoTaken){
            uploadPhoto.setVisibility(View.GONE);
            deletePhoto.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            retry.setVisibility(View.GONE);
        }

        checkAndRequestPermissions();
        deletePhoto.setOnClickListener(v -> deletePhoto());
        uploadPhoto.setOnClickListener(v -> uploadPhoto());
        takePhoto.setOnClickListener(v -> takePhoto());
        retry.setOnClickListener(v->retry());
    }

    /**
     * Calling the camera application to take photo
     */
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //we need this picture to be stored in external file
            File file;
            try{
                file = createImageFile();
                Uri uri = FileProvider.
                        getUriForFile(this,"com.example.android.fileprovider",file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                startActivityForResult(takePictureIntent, REQUEST_CAPTURE_CODE);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this,"Error occured",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"Phone does not support image capture",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Returning from the camera application with our photo data
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_CODE && resultCode == RESULT_OK) {
            //image has been taken
            //extra checks can be done to make sure the image exists in the storage
           // Toast.makeText(this,documentPhotoPath,Toast.LENGTH_LONG).show();
            setImageFromFilePath();
        }else{
            Toast.makeText(this,"Error taking photo",Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageFromFilePath() {
        takePhoto.setVisibility(View.GONE);
        deletePhoto.setVisibility(View.VISIBLE);
        uploadPhoto.setVisibility(View.VISIBLE);

        File imgFile = new  File(documentPhotoPath);

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
    }


    public void  retry(){
        uploadPhoto();
    }
    private void uploadPhoto() {
        progressBar.setVisibility(View.VISIBLE);
        uploadPhoto.setVisibility(View.GONE);
        deletePhoto.setVisibility(View.GONE);
        takePhoto.setVisibility(View.GONE);

        UploadTask.uploadPhoto(documentPhotoPath,new UploadTask.PhotoTask(){

            @Override
            public void onphotoUploaded() {
                MainActivity.this.runOnUiThread(() -> displayPostUploadButtonsWithMessage("Photo uploaded successfully"));
            }

            @Override
            public void onPhotoUploadFailed() {
                MainActivity.this.runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,"Error uploading photo",Toast.LENGTH_SHORT).show();
                    retry.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    deletePhoto.setVisibility(View.GONE);
                    takePhoto.setVisibility(View.GONE);
                    uploadPhoto.setVisibility(View.GONE);
                });
            }
        });
    }

    public void displayPostUploadButtonsWithMessage(String message){
        progressBar.setVisibility(View.GONE);
        uploadPhoto.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        deletePhoto.setVisibility(View.GONE);
        takePhoto.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        try{
            File file = new File(documentPhotoPath);
            if(file.exists()){
                file.delete();
            }
        }catch (Exception e){}
    }


    private void deletePhoto() {
        takePhoto.setVisibility(View.VISIBLE);
        uploadPhoto.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        deletePhoto.setVisibility(View.GONE);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));

        try{
            File file = new File(documentPhotoPath);
            if(file.exists()){
                file.delete();
            }
        }catch (Exception e){

        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        documentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * request at  Runtime permissions
     * omitted::report to user certain functionalities cannot be done if request not granted
     * @return
     */
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int cameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_PERMISSIONS_CODE);
                return false;
            }
            return true;
        }

        return true;
    }

}
