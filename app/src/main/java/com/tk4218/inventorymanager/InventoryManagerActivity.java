package com.tk4218.inventorymanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.tk4218.model.DataSource;
import com.tk4218.model.ImageManager;
import com.tk4218.model.TableObject;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import adapters.InventoryGridAdapter;

public class InventoryManagerActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private static int REQUEST_LOAD_IMAGE = 2;

    static final int REQUEST_PERMISSIONS = 100;

    String mCurrentPhotoPath = "";
    float discount = 0, additionalCost = 0;
    double total = 0;


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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Load Image").setItems(new CharSequence[]{"Take Picture", "Load Image from Gallery"},
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
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
                if(which == 1){
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(intent, REQUEST_LOAD_IMAGE);
                }
            }
        });

        builder.show();
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
        if(requestCode == REQUEST_TAKE_PHOTO) {
            galleryAddPic();
        }
        if(requestCode == REQUEST_LOAD_IMAGE) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
        }
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
        Log.d("InventoryKey", view.getContentDescription().toString());
        final int inventoryKey = Integer.parseInt((String)view.getContentDescription());
        if(selectInventory.findFirst("InventoryKey", inventoryKey)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sold Inventory");
            builder.setIcon(R.drawable.dollar_sign);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_sale_dialog, null);
            builder.setView(dialogView);


            TextView sizeStyle = (TextView) dialogView.findViewById(R.id.size_style);
            sizeStyle.setText(selectSize + " " + selectStyle);

            TextView salePrice = (TextView) dialogView.findViewById(R.id.sale_price);
            final TableObject stylePrice = mDbc.findStyle(selectStyle);
            total = stylePrice.getFloat("MinAdvertisePrice");
            discount = 0;
            additionalCost = 0;
            salePrice.setText("Sale Price: $" + String.format(Locale.US, "%1$.2f", total));

            final TextView totalPrice = (TextView) dialogView.findViewById(R.id.total_price);
            totalPrice.setText("Total Price: $" + String.format(Locale.US, "%1$.2f", total));

            EditText editDiscount = (EditText) dialogView.findViewById(R.id.discount);
            editDiscount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        discount = Float.parseFloat(s.toString());
                    } catch(Exception e){
                        discount = 0;
                    }
                    total = stylePrice.getFloat("MinAdvertisePrice") - discount + additionalCost;
                    totalPrice.setText("Total Price: $" + String.format(Locale.US, "%1$.2f", total));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            EditText editAddlCost = (EditText) dialogView.findViewById(R.id.additional_cost);
            editAddlCost.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        additionalCost = Float.parseFloat(s.toString());
                    } catch(Exception e){
                        additionalCost = 0;
                    }
                    total = stylePrice.getFloat("MinAdvertisePrice") - discount + additionalCost;
                    totalPrice.setText("Total Price: $" + String.format(Locale.US, "%1$.2f", total));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });


            final AutoCompleteTextView soldTo = (AutoCompleteTextView) dialogView.findViewById(R.id.sold_to);
            TableObject saleNames = mDbc.getSaleNames();
            if(saleNames.getRowCount() > 0){
                String [] names = new String[saleNames.getRowCount()];
                for(int i = 0; i < saleNames.getRowCount(); i++){
                    Log.d("Names", saleNames.getString("SoldTo"));
                    names[i] = saleNames.getString("SoldTo");
                    saleNames.moveNext();
                }
                soldTo.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, names));
            }

            //builder.setMessage("Are you sure you want to mark this item as sold?");
            builder.setPositiveButton("Sold!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("Sold", "Inventory Key:" + inventoryKey+"");
                    mDbc.updateInventorySold(1, inventoryKey);
                    mDbc.insertSaleInfo(inventoryKey, "", total, soldTo.getText().toString(), new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(new Date()));
                    selectInventory = mDbc.getStyleInventory(selectSize, selectStyle);
                    while(!selectInventory.EOF()){
                        Log.d("Inventory", selectInventory.getInt("InventoryKey") + " " + selectInventory.getString("InventoryPicture"));
                        selectInventory.moveNext();
                    }
                    selectInventory.moveFirst();
                    inventory.setAdapter(new InventoryGridAdapter(InventoryManagerActivity.this, selectInventory));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("InventoryKey", selectInventory.getInt("InventoryKey") +"");
                  //Do Nothing
                }
            });
            builder.show();
        }
    }
}
