package com.tk4218.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {
    private SQLiteDatabase db;
    private AppDatabase app;

    public DataSource(Context context){
        app = new AppDatabase(context);
    }

    public void open() throws SQLException {
        db = app.getWritableDatabase();
    }

    public void close() {
        app.close();
    }

    public TableObject getAvailableInventory(){
        Cursor results = db.rawQuery("select i.InventoryKey, sz.SizeShort, st.Style from tableInventory i, tableSize sz, tableStyle st\n" +
                "where i.SizeKey = sz.SizeKey and i.StyleKey = st.StyleKey and i.IsSold = 0", null);
        return new TableObject(results);
    }

    public long insertSize(int sortOrder, String sizeShort, String sizeLong){
        ContentValues values = new ContentValues();
        values.put("SortOrder", sortOrder);
        values.put("SizeShort", sizeShort);
        values.put("SizeLong", sizeLong);
        return db.insert("tableSize", null, values);
    }

    public long insertStyle(String style,
                            double wholeSalePrice,
                            double retailMinPrice,
                            double retailMaxPrice,
                            double minAdvertisePrice,
                            String stylePicture){
        ContentValues values = new ContentValues();
        values.put("Style", style);
        values.put("WholeSalePrice", wholeSalePrice);
        values.put("RetailMinPrice", retailMinPrice);
        values.put("RetailMaxPrice", retailMaxPrice);
        values.put("MinAdvertisePrice", minAdvertisePrice);
        values.put("StylePicture", stylePicture);
        return db.insert("tableStyle", null, values);
    }

    public long insertInventory(int sizeKey, int styleKey, int isSold,
                                String receivedDate, String returnedDate, int isDamaged,
                                String inventoryPicture){
        ContentValues values = new ContentValues();
        values.put("SizeKey", sizeKey);
        values.put("StyleKey", styleKey);
        values.put("IsSold", isSold);
        values.put("ReceivedDate", receivedDate);
        values.put("ReturnedDate", returnedDate);
        values.put("IsDamaged", isDamaged);
        values.put("InventoryPicture", inventoryPicture);
        return db.insert("tableInventory", null, values);
    }

    public TableObject findSizeShort(String sizeShort){
        Cursor results = db.rawQuery("select * from tableSize where SizeShort = ?",  new String [] {sizeShort});
        return new TableObject(results);
    }

    public TableObject findStyle(String style){
        Cursor results = db.rawQuery("Select * from tableStyle where Style = ?", new String [] {style});
        return new TableObject(results);
    }

    public TableObject getInventoryCounts(){
        Cursor results = db.rawQuery("select sz.SortOrder, sz.SizeShort, st.Style, count(1) as StyleCount " +
                                     "from tableInventory i, tableSize sz, tableStyle st " +
                                     "where i.SizeKey = sz.SizeKey and i.StyleKey = st.StyleKey and i.IsSold = 0 " +
                                     "group by sz.SizeShort, st.Style order by st.Style, sz.SortOrder", null);
        return new TableObject(results);
    }

    public TableObject getSizes(){
        Cursor results = db.rawQuery("select SizeShort, SizeLong from tableSize", null);
        return new TableObject(results);
    }

    public TableObject getStyleImage(String style){
        Cursor results = db.rawQuery("select StylePicture from tableStyle where Style = ?", new String [] {style});
        return new TableObject(results);
    }

    public int updateStylePicture(String stylePicture, int styleKey){
        ContentValues values = new ContentValues();
        values.put("StylePicture", stylePicture);
        return db.update("tableStyle", values, "StyleKey=?", new String[] {styleKey+""});
    }

    public TableObject getAllStyleNames(){
        Cursor results = db.rawQuery("select Style from tableStyle" , null);
        return new TableObject(results);
    }

    public TableObject getStyleInventory(String size, String style){
        Cursor results = db.rawQuery("select i.* from tableInventory i, tableSize sz, tableStyle st\n" +
                "where i.SizeKey = sz.SizeKey and i.StyleKey = st.StyleKey and i.IsSold = 0 and sz.SizeShort = ? and st.Style = ?",
                new String [] {size, style});
        return new TableObject(results);
    }

    public int updateInventoryPicture(String inventoryPicture, int inventoryKey){
        ContentValues values = new ContentValues();
        values.put("InventoryPicture", inventoryPicture);
        return db.update("tableInventory", values, "InventoryKey=?", new String[] {inventoryKey+""});
    }

    public int updateInventorySold(int isSold, int inventoryKey){
        ContentValues values = new ContentValues();
        values.put("IsSold", isSold);
        return db.update("tableInventory", values, "InventoryKey=?", new String[] {inventoryKey+""});
    }

    public int updateStylePrices(float wholesalePrice, float minRetailPrice, float maxRetailPrice, float minAdvertisePrice, int styleKey){
        ContentValues values = new ContentValues();
        values.put("WholesalePrice", wholesalePrice);
        values.put("RetailMinPrice", minRetailPrice);
        values.put("RetailMaxPrice", maxRetailPrice);
        values.put("MinAdvertisePrice", minAdvertisePrice);
        return db.update("tableStyle", values, "StyleKey=?", new String[]{styleKey+""});
    }

    public long insertSaleInfo(int inventoryKey, String saleType, double salePrice, String soldTo, String soldDate){
        ContentValues values = new ContentValues();
        values.put("InventoryKey", inventoryKey);
        values.put("SaleType", saleType);
        values.put("SalePrice", salePrice);
        values.put("SoldTo", soldTo);
        values.put("SoldDate", soldDate);
        return db.insert("tableSaleInfo", null, values);
    }

    public TableObject getSaleNames(){
        Cursor results = db.rawQuery("Select Distinct SoldTo from tableSaleInfo", null);
        return new TableObject(results);
    }
}

