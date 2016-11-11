package com.android.inventorystore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.inventorystore.data.ItemContract.ItemEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.name_edit)
    EditText nameField;
    @BindView(R.id.price_input)
    EditText priceField;

    Uri mCurrentItemUri;
    private boolean hasItemChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasItemChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this);

        /*Check for previous data in intent*/
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.add_item));
        } else {
            setTitle(getString(R.string.edit_item));
        }

        nameField.setOnTouchListener(mTouchListener);
        priceField.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!hasItemChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!hasItemChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.keep, discardButtonClickListener);
        builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    finish();
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE
        };
        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            nameField.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME)));
            priceField.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameField.setText("");
        priceField.setText("");
    }

    private void saveItem() {
        String nameString = nameField.getText().toString().trim();
        String priceString = priceField.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            nameField.setError(getString(R.string.name_error));
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            priceField.setError(getString(R.string.price_error));
            return;
        } else {
            int price = Integer.parseInt(priceString);
            if (price == 0) {
                priceField.setError(getString(R.string.price_error));
            } else {
                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
                values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
                //If item is new, use insert function
                if (mCurrentItemUri == null) {
                    Uri newItemUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

                    //If no new item inserted
                    if (newItemUri == null) {
                        Toast.makeText(this, getString(R.string.failed_insert), Toast.LENGTH_SHORT).show();
                    }
                    //If item has been inserted
                    else {
                        Toast.makeText(this, getString(R.string.success_insert), Toast.LENGTH_SHORT).show();
                    }
                }
                //If item is previously available, use update function
                else {
                    int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                    //If no row was updated
                    if (rowsAffected == 0) {
                        Toast.makeText(this, getString(R.string.failed_update), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(this, getString(R.string.success_update), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete1_confirmation);
        builder.setPositiveButton(R.string.confirmation_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.confirmation_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.failed_delete), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete1_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
