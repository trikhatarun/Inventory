package com.android.inventorystore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.inventorystore.data.ItemContract.ItemEntry;

/**
 * Created by trikh on 12-11-2016.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "storage.db";

    private static final int DATABASE_VERSION = 1;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + "( "
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL, "
                + ItemEntry.COLUMN_ITEM_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_ITEM_IMAGE + " TEXT DEFAULT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
