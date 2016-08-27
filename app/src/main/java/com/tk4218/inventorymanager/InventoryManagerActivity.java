package com.tk4218.inventorymanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import com.tk4218.model.DataSource;
import com.tk4218.model.ImageManager;
import com.tk4218.model.TableObject;

import java.io.File;
import java.io.IOException;

import adapters.InventoryGridAdapter;

public class InventoryManagerActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_PERMISSIONS = 100;

    String mCurrentPhotoPath = "";

    DataSource mDbc;
    GridView inventory;
    String selectSize;
    String selectStyle;
    ImageManager imageManager;
    TableObject selectInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_manager_wrapper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageManager = new ImageManager();
        mDbc = new DataSource(this);
        mDbc.open();


        Bundle extras = getIntent().getExtras();
        selectSize = extras.getString("size");
        selectStyle = extras.getString("style");
        selectInventory = mDbc.getStyleInventory(selectSize, selectStyle);
        setTitle(selectSize + " " + selectStyle);

        InventoryGridAdapter adapter = new InventoryGridAdapter(this, selectInventory);
        inventory = (GridView) findViewById(R.id.inventoryGridView);
        inventory.setAdapter(adapter);
    }

    public void takePicture(View view){
        if(selectInventory.findFirst("InventoryKey", Integer.parseInt((String)view.getContentDescription()))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasStoragePermissions = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasStoragePermissions != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSIONS);
                    return;
                }
            }
            capturePicture();
        }
    }

    private void capturePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f;
        try {
            f = imageManager.setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = "";
        }
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    capturePicture();
                } else {
                    Log.d("Permissions", "Write External Storage Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        galleryAddPic();
        mDbc.updateInventoryPicture(mCurrentPhotoPath, selectInventory.getInt("InventoryKey"));
        selectInventory.setString("InventoryPicture", mCurrentPhotoPath);
        inventory.setAdapter(new InventoryGridAdapter(this, selectInventory));
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void soldInventory(View view){
        if(selectInventory.findFirst("InventoryKey", Integer.parseInt((String)view.getContentDescription()))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sold Inventory");
            builder.setMessage("Are you sure you want to mark this item as sold?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDbc.updateInventorySold(1, selectInventory.getInt("InventoryKey"));
                    selectInventory = mDbc.getStyleInventory(selectSize, selectStyle);
                    inventory.setAdapter(new InventoryGridAdapter(InventoryManagerActivity.this, selectInventory));
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  //Do Nothing
                }
            });
            builder.show();
        }
    }
}
