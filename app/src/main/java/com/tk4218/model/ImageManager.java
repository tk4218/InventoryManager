package com.tk4218.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.tk4218.inventorymanager.AlbumStorageDirFactory;
import com.tk4218.inventorymanager.BaseAlbumDirFactory;
import com.tk4218.inventorymanager.FroyoAlbumDirFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tk4218 on 8/15/2016.
 */
public class ImageManager {

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private LruCache<String, Bitmap> mMemoryCache;

    public ImageManager(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        else
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 6;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
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

    public Bitmap setPic(String photoPath, int reqWidth, int reqHeight) {
        if(photoPath.equals(""))
            return null;

        Matrix matrix = new Matrix();

        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case 6:
                    matrix.postRotate(90);
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 8:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
        }catch(Exception e){

            }

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);

		/* Set bitmap options to scale the image decode target */
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions, reqWidth, reqHeight);
		/* Decode the JPEG file into a Bitmap */
        bmOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        if (bitmap != null)
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return bitmap;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final  int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }

        return inSampleSize * 4;
    }


    public void loadBitmap(String photoPath, ImageView imageView, int width, int height, Resources resources){
        final Bitmap bitmap = getBitmapFromMemCache(photoPath);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        } else if(cancelPotentialWork(photoPath, imageView)){
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, width, height);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(resources,
                    BitmapFactory.decodeResource(resources, android.R.color.darker_gray), task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(photoPath);
        }
    }

    public static boolean cancelPotentialWork(String photo, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask != null){
            final String bitmapPhoto = bitmapWorkerTask.photoPath;
            if(bitmapPhoto.equals("") || !bitmapPhoto.equals(photo)){
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if(imageView != null){
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof  AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private  int width, height;
        private String photoPath = "";

        public BitmapWorkerTask(ImageView imageView, int width, int height){
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            photoPath = params[0];
            final Bitmap bitmap = setPic(photoPath, width, height);
            addBitmapToMemoryCache(photoPath, bitmap);
            return bitmap;
        }

        @Override
        protected  void onPostExecute(Bitmap bitmap){
            if(isCancelled())
                bitmap = null;

            if (imageViewReference != null && bitmap != null){
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable{
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

         public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
             super(res, bitmap);
             bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
         }

        public BitmapWorkerTask getBitmapWorkerTask(){
            return bitmapWorkerTaskReference.get();
        }
    }

}
