package com.precious.apps.cameraapplication;

import android.util.Log;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Task that will be used to upload photo to the server.
 */
public class UploadTask {
   public static String TAG = "UploadTask";
    OkHttpClient client = new OkHttpClient();

    public interface PhotoTask{
        void onphotoUploaded();
        void onPhotoUploadFailed();
    }



    public static void uploadPhoto(String potoPath, PhotoTask task){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

                    RequestBody req = new MultipartBody.Builder().
                            setType(MultipartBody.FORM).
                            addFormDataPart("user_id", Utils.generateUniqueUserID())
                            .addFormDataPart("document",
                                    new File(potoPath).getName(),
                                    RequestBody.create(MEDIA_TYPE_PNG, new File(potoPath))).
                                    build();

                    Request request = new Request.Builder()
                            .url(Constants.API_ENDPOINT)
                            .post(req)
                            .build();

                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    Log.d("response", response.body().string());

                    task.onphotoUploaded();
                } catch (Exception e) {
                    e.printStackTrace();
                    task.onPhotoUploadFailed();
                }
            }
        });
        thread.start();

    }

}
