package com.example.asus.final_two.helperclasses;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateFile {
    public String getImageFilePath() {
        return imageFilePath;
    }

    String imageFilePath;
    Context context;
    public CreateFile(Context context) {
        this.context=context;
    }

    public File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir((Environment.DIRECTORY_PICTURES));
        if(!storageDir.exists())
        {
            Log.e("CreateFileTag","the directory doesn't exist");
        }
        else Log.e("CreateFileTag",Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }
}
