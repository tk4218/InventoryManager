package com.tk4218.model;

/**
 * Created by Tk4218 on 6/14/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "App.db";
    public static final int DATABASE_VERSION = 3;

    /*****************************************************
     *  Table Names
    *******************************************************/
    public static final String TABLE_STYLE = "tableStyle";
    public static final String TABLE_SIZE = "tableSize";
    public static final String TABLE_INVENTORY = "tableInventory";
    public static final String TABLE_SALE_INFO = "tableSaleInfo";

    /********************************************************
     * Table Structures
     ********************************************************/
    public static final String CREATE_TABLE_STYLE =
            "CREATE TABLE " + TABLE_STYLE +
            "( StyleKey          INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "Style             TEXT NOT NULL , "
            + "WholeSalePrice    REAL NOT NULL, "
            + "RetailMinPrice    REAL NOT NULL, "
            + "RetailMaxPrice    REAL NOT NULL, "
            + "MinAdvertisePrice REAL NOT NULL, "
            + "StylePicture      TEXT NOT NULL)";

    public static final String CREATE_TABLE_SIZE =
            "CREATE TABLE " + TABLE_SIZE +
            "( SizeKey      INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "SizeShort    TEXT NOT NULL, "
            + "SizeLong     TEXT NOT NULL )";

    public static final String CREATE_TABLE_INVENTORY =
            "CREATE TABLE " + TABLE_INVENTORY +
            "( InventoryKey     INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "SizeKey          INTEGER NOT NULL, "
            + "StyleKey         INTEGER NOT NULL, "
            + "IsSold           INTEGER NOT NULL, "
            + "ReceivedDate     DATE, "
            + "ReturnedDate     DATE, "
            + "IsDamaged        INTEGER NOT NULL, "
            + "InventoryPicture TEXT NOT NULL)";

    public static final String CREATE_TABLE_SALE_INFO =
            "CREATE TABLE " + TABLE_SALE_INFO +
                    "( InventoryKey     INTEGER PRIMARY KEY NOT NULL, "
                    + "SaleType         TEXT NOT NULL, "
                    + "SalePrice        REAL NOT NULL, "
                    + "SoldTo           TEXT NOT NULL, "
                    + "SoldDate         DATE )";

    public AppDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_STYLE);
        db.execSQL(CREATE_TABLE_SIZE);
        db.execSQL(CREATE_TABLE_INVENTORY);
        db.execSQL(CREATE_TABLE_SALE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STYLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIZE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALE_INFO);
        onCreate(db);
    }
}
