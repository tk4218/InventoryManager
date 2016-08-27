package com.tk4218.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.tk4218.inventorymanager.AlbumStorageDirFactory;
import com.tk4218.inventorymanager.BaseAlbumDirFactory;
import com.tk4218.inventorymanager.FroyoAlbumDirFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tk4218 on 8/15/2016.
 */
public class ImageManager {

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    public ImageManager(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        else
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
    }

    public File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("LuLaRoe");

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("LuLaRoe", "failed to create directory");
                        return null;
                    }
                }
            }

        }

        return storageDir;
    }

    public void setPic(String photoPath, ImageView image, int scaleFactor) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        //int targetW = image.getWidth()/imageScale;
        //int targetH = image.getHeight()/imageScale;

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        //int photoW = bmOptions.outWidth;
        //int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        //if ((targetW > 0) || (targetH > 0)) {
          //  scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        //}

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        image.setImageBitmap(bitmap);

    }

}
