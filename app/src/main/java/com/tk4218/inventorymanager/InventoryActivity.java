package com.tk4218.inventorymanager;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tk4218.model.DataSource;
import com.tk4218.model.TableObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import adapters.InventoryListAdapter;

public class InventoryActivity extends AppCompatActivity {
    InventoryListAdapter inventoryListAdapter;
    ExpandableListView expandableListView;
    List<String> styleHeader;
    TableObject styleSizes;
    DataSource mDbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbc = new DataSource(this);
        mDbc.open();

        initializeDatabaseData();
        getInventoryData();
        inventoryListAdapter = new InventoryListAdapter(this, styleHeader, styleSizes);

        expandableListView = (ExpandableListView) findViewById(R.id.inventoryList);
        expandableListView.setAdapter(inventoryListAdapter);
     }


    @Override
    public void onResume(){
        super.onResume();
        getInventoryData();
        inventoryListAdapter = new InventoryListAdapter(this, styleHeader, styleSizes);

        expandableListView = (ExpandableListView) findViewById(R.id.inventoryList);
        expandableListView.setAdapter(inventoryListAdapter);
    }

    public void getInventoryData(){
        styleHeader = new ArrayList<String>();
        styleSizes = mDbc.getInventoryCounts();
        while(!styleSizes.EOF()){
            if(!styleHeader.contains(styleSizes.getString("Style"))){
                styleHeader.add(styleSizes.getString("Style"));
            }
            styleSizes.moveNext();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        //we have a result
        String scanContent = scanningResult.getContents();
        if((scanContent != null) && (scanContent.contains("-"))){
            String [] contents = scanContent.split("-");
            Intent inventoryIntent = new Intent(this, AddInventoryActivity.class);
            inventoryIntent.putExtra("scannedStyle", contents[0].trim());
            inventoryIntent.putExtra("scannedSize", contents[1].trim());
            startActivity(inventoryIntent);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Scanned Barcode Was In Incorrect Format. Please Try Again.",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void addInventory(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick an Option")
                .setItems(new CharSequence[]{"Add from Barcode Scanner","Add Inventory For Existing Style","Add Inventory For New Style"},
                        new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            IntentIntegrator scanIntegrator = new IntentIntegrator(InventoryActivity.this);
                            scanIntegrator.initiateScan();
                        }
                        else if(which == 1){
                            TableObject styles = mDbc.getAllStyleNames();
                            final CharSequence styleNames[] = new CharSequence[styles.getRowCount()];
                            styles.sort("Style", TableObject.SORT_ASCENDING);
                            for(int i = 0; i < styleNames.length; i++){
                                styleNames[i] = styles.getString("Style");
                                styles.moveNext();
                            }
                            AlertDialog.Builder styleDialog = new AlertDialog.Builder(InventoryActivity.this);
                            styleDialog.setTitle("Select a Style")
                                    .setItems(styleNames, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(InventoryActivity.this, AddInventoryActivity.class);
                                            intent.putExtra("scannedStyle", styleNames[which]);
                                            intent.putExtra("scannedSize", "XXS");
                                            startActivity(intent);
                                        }
                                    });
                            styleDialog.show();
                        }
                        else{
                            Intent intent = new Intent(InventoryActivity.this, AddInventoryActivity.class);
                            startActivity(intent);
                        }
                    }
                });
        builder.show();
    }

    public void initializeDatabaseData(){
        if(mDbc.findStyle("Julia").getRowCount() == 0){
            //Insert initial styles
            mDbc.insertStyle("Julia", 18.00, 38.00, 45.00, 45.00, "");
            mDbc.insertStyle("Amelia", 31.00, 60.00, 65.00, 65.00, "");
            mDbc.insertStyle("Maxi", 21.00, 35.00, 42.00, 42.00, "");
            mDbc.insertStyle("Lucy", 23.00, 42.00, 52.00, 52.00, "");
            mDbc.insertStyle("Madison", 23.00, 38.00, 46.00, 46.00, "");
            mDbc.insertStyle("Azure", 14.00, 30.00, 35.00, 35.00, "");
            mDbc.insertStyle("Cassie",14.00, 30.00, 35.00, 35.00, "");
            mDbc.insertStyle("Lola", 21.00, 40.00, 46.00, 46.00, "");
            mDbc.insertStyle("Jill", 25.00, 48.00, 55.00, 55.00, "");
            mDbc.insertStyle("Ana", 27.00, 50.00, 60.00, 60.00, "");
            mDbc.insertStyle("Nicole", 23.00, 40.00, 48.00, 48.00, "");
            mDbc.insertStyle("Jade", 26.00, 50.00, 55.00, 55.00, "");
            mDbc.insertStyle("Irma", 15.00, 30.00, 35.00, 35.00, "");
            mDbc.insertStyle("Randy", 16.00, 30.00, 35.00, 35.00, "");
            mDbc.insertStyle("Classic T", 16.00, 30.00, 35.00, 35.00, "");
            mDbc.insertStyle("Perfect T", 17.00, 30.00, 36.00, 36.00, "");
            mDbc.insertStyle("Monroe Kimono", 21.00, 42.00, 48.00, 48.00, "");
            mDbc.insertStyle("Lindsay Kimono", 21.00, 42.00, 48.00, 48.00, "");
            mDbc.insertStyle("Sarah Cardigan", 30.00, 60.00, 70.00, 70.00, "");
            mDbc.insertStyle("Patrick T", 20.00, 36.00, 40.00, 40.00, "");

            //Insert intial Sizes
            mDbc.insertSize(1, "XXS", "2X Extra Small");
            mDbc.insertSize(2, "XS", "Extra Small");
            mDbc.insertSize(3, "S", "Small");
            mDbc.insertSize(4, "M", "Medium");
            mDbc.insertSize(5, "L", "Large");
            mDbc.insertSize(6, "XL", "Extra Large");
            mDbc.insertSize(7, "XXL", "2X Extra Large");
            mDbc.insertSize(8, "XXXL", "3X Extra Large");
            mDbc.insertSize(9, "OS", "One Size");
            mDbc.insertSize(10, "TC", "Tall and Curvy");
        }
    }

    public void viewInventory(View view){
        Intent intent = new Intent(this, InventoryManagerActivity.class);
        TextView size = (TextView) view.findViewById(R.id.item_size);
        TextView style = (TextView) view.findViewById(R.id.inventory_list_item);
        intent.putExtra("size", size.getText().toString());
        intent.putExtra("style", style.getText().toString());
        startActivity(intent);
    }
}
