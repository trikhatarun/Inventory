package com.android.inventorystore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.inventorystore.data.ItemContract.ItemEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.inventory_list)
    ListView listView;
    @BindView(R.id.empty_view)
    View emptyView;

    private ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Bind the views to the activity
        ButterKnife.bind(this);

        listView.setEmptyView(emptyView);
        mCursorAdapter = new ItemCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        //On fab click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newItem = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(newItem);
            }
        });

        //On list item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                Intent editItem = new Intent(MainActivity.this, EditorActivity.class);
                editItem.setData(currentItemUri);

                startActivity(editItem);
            }
        });

        //Initiate loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all_items) {
            deleteAllItems();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_STOCK};
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
