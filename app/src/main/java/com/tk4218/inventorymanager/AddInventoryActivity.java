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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tk4218.model.DataSource;
import com.tk4218.model.ImageManager;
import com.tk4218.model.TableObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AddInventoryActivity extends AppCompatActivity {
    /**************************************************************
     * Global Variables
     ***************************************************************/
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_PERMISSIONS = 100;

    String mCurrentPhotoPath = "";

    DataSource mDbc;
    EditText styleName;
    Spinner sizeSpinner;
    Spinner amountSpinner;
    EditText wholesalePrice;
    EditText minRetailPrice;
    EditText maxRetailPrice;
    EditText minAdvertisePrice;
    ImageView styleImage;
    List<String> sizes;
    TableObject style;
    TableObject size;
    ImageManager imageManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /******************************************************************
     * Overridable methods
     *******************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory_wrapper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbc = new DataSource(this);
        mDbc.open();

        initializeActivityFields();
        setSizeSpinnerValues();
        setExistingStyleValues();

        styleImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddInventoryActivity.this);
                builder.setTitle("Delete Style Picture");
                builder.setMessage("Would you like to remove this picture as the default picture?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File f = new File(mCurrentPhotoPath);
                        if(!f.delete()){Log.e("Error", "Unable to Delete File");}
                        AddInventoryActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mCurrentPhotoPath))));
                        mCurrentPhotoPath = "";
                        if (style.getRowCount() > 0) {
                            mDbc.updateStylePicture("", style.getInt("StyleKey"));
                        }
                        styleImage.setImageResource(R.drawable.dress_example);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return false;
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            size = mDbc.findSizeShort(sizeSpinner.getSelectedItem().toString());
            if (style == null) {
                style = mDbc.findStyle(styleName.getText().toString());
            }

            if (style.getRowCount() == 0) {
                long numInsert = mDbc.insertStyle(styleName.getText().toString(),
                        Double.parseDouble(wholesalePrice.getText().toString()),
                        Double.parseDouble(minRetailPrice.getText().toString()),
                        Double.parseDouble(maxRetailPrice.getText().toString()),
                        Double.parseDouble(minAdvertisePrice.getText().toString()),
                        mCurrentPhotoPath);

                if (numInsert != 0) {
                    style = mDbc.findStyle(styleName.getText().toString());
                }
            }

            if (style.getRowCount() != 0) {
                if (style.getString("StylePicture").equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Set Style Picture");
                    builder.setMessage("Would you like to use this image as the default image for this style?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            insertInventory("Yes");
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            insertInventory("No");
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    insertInventory("No");
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        imageManager.setPic(mCurrentPhotoPath, styleImage, 1);
        galleryAddPic();
    }

    /************************************************************************
     * Event Handlers
     ************************************************************************/
    public void changePicture(View view) {
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

    /*************************************************************************
     * Private Methods
     *************************************************************************/

    private void initializeActivityFields() {
        styleName = (EditText) findViewById(R.id.StyleName);
        sizeSpinner = (Spinner) findViewById(R.id.SizeSpinner);
        amountSpinner = (Spinner) findViewById(R.id.AmountSpinner);
        wholesalePrice = (EditText) findViewById(R.id.WholesalePrice);
        minRetailPrice = (EditText) findViewById(R.id.MinRetailPrice);
        maxRetailPrice = (EditText) findViewById(R.id.MaxRetailPrice);
        minAdvertisePrice = (EditText) findViewById(R.id.MinAdvertisePrice);
        styleImage = (ImageView) findViewById(R.id.imageView);
        imageManager = new ImageManager();
    }

    private void setSizeSpinnerValues() {
        TableObject tableSizes = mDbc.getSizes();
        sizes = new ArrayList<>();
        while (!tableSizes.EOF()) {
            sizes.add(tableSizes.getString("SizeShort"));
            tableSizes.moveNext();
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(spinnerAdapter);
    }

    private void setExistingStyleValues() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String scannedStyle = extras.getString("scannedStyle");
            String scannedSize = extras.getString("scannedSize");
            style = mDbc.findStyle(scannedStyle);
            sizeSpinner.setSelection(sizes.indexOf(scannedSize));
            styleName.setText(scannedStyle);
            if (style.getRowCount() > 0) {
                wholesalePrice.setText(String.format(Locale.US, "%1$.2f", style.getFloat("WholeSalePrice")));
                minRetailPrice.setText(String.format(Locale.US, "%1$.2f", style.getFloat("RetailMinPrice")));
                maxRetailPrice.setText(String.format(Locale.US, "%1$.2f", style.getFloat("RetailMaxPrice")));
                minAdvertisePrice.setText(String.format(Locale.US, "%1$.2f", style.getFloat("MinAdvertisePrice")));
                mCurrentPhotoPath = style.getString("StylePicture");
                if (!mCurrentPhotoPath.equals("")) {
                    imageManager.setPic(mCurrentPhotoPath, styleImage, 1);
                } else {
                    styleImage.setImageResource(R.drawable.default_image);
                }
            } else {
                styleImage.setImageResource(R.drawable.default_image);
            }
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

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void insertInventory(String confirmation) {
        if (confirmation.equals("Yes")) {
            mDbc.updateStylePicture(mCurrentPhotoPath, style.getInt("StyleKey"));
        }
        int numOfItems = Integer.parseInt(amountSpinner.getSelectedItem().toString());
        for (int i = 0; i < numOfItems; i++) {
            mDbc.insertInventory(size.getInt("SizeKey"), style.getInt("StyleKey"), 0, new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(new Date()), "", 0, "");
        }
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddInventory Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.tk4218.inventorymanager/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "AddInventory Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.tk4218.inventorymanager/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
