package com.android.inventorystore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by trikh on 11-11-2016.
 */

public class ItemContract {
    public static final String CONTENT_AUTHORITY = "com.android.inventorystore";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";

    private ItemContract() {
    }

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_AUTHORITY + PATH_ITEMS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_AUTHORITY + PATH_ITEMS;

        //Table name
        public final static String TABLE_NAME = "inventory";

        // _ID of item
        public final static String _ID = BaseColumns._ID;

        //name of Item
        public final static String COLUMN_ITEM_NAME = "name";

        //Price of item
        public final static String COLUMN_ITEM_PRICE = "price";

        //Stock of the item
        public final static String COLUMN_ITEM_STOCK = "stock";
    }
}
