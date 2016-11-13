package com.android.inventorystore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.inventorystore.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String STATE_URI = "STATE_URI";
    private final int EXISTING_PET_LOADER = 1;
    @BindView(R.id.name_edit)
    EditText nameField;
    @BindView(R.id.price_input)
    EditText priceField;
    @BindView(R.id.stock)
    TextView stock;
    @BindView(R.id.sell_button_editor)
    TextView sellButton;
    @BindView(R.id.add_button_editor)
    TextView addButton;
    @BindView(R.id.buttonPanel)
    View buttonPanel;
    @BindView(R.id.stockPanel)
    View stockPanel;
    @BindView(R.id.order_supplier)
    Button order;
    @BindView(R.id.image)
    ImageView image;

    Uri mCurrentItemUri, mUri;
    private boolean hasItemChanged = false;
    private int currentStock;

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
        final Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.add_item));
        } else {
            setTitle(getString(R.string.edit_item));
            stockPanel.setVisibility(View.VISIBLE);
            buttonPanel.setVisibility(View.VISIBLE);
            order.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        nameField.setOnTouchListener(mTouchListener);
        priceField.setOnTouchListener(mTouchListener);
        image.setOnTouchListener(mTouchListener);

        final Context context = EditorActivity.this;

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.prompt_view, null);
                TextView headingPrompt = (TextView) promptView.findViewById(R.id.heading_prompt);
                headingPrompt.setText(getString(R.string.order));

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptView);

                final EditText userInput = (EditText) promptView.findViewById(R.id.input);

                alertDialogBuilder.setCancelable(false).setPositiveButton(context.getText(R.string.order_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String quantityString = userInput.getText().toString().trim();
                        if (quantityString.isEmpty() || quantityString == "" || quantityString.length() == 0 || quantityString == null) {
                            return;
                        } else {
                            Integer quantityAdded = Integer.parseInt(quantityString);
                            if (quantityAdded != 0) {
                                ContentValues values = new ContentValues();
                                values.put(ItemEntry.COLUMN_ITEM_STOCK, currentStock + quantityAdded);
                                int rowsAffected = context.getContentResolver().update(mCurrentItemUri, values, null, null);
                                if (rowsAffected == 0) {
                                    Toast.makeText(context, context.getString(R.string.failed_order), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, context.getString(R.string.success_order), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                alertDialogBuilder.show();
            }
        });
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                if (currentStock > 0) {
                    values.put(ItemEntry.COLUMN_ITEM_STOCK, currentStock - 1);
                    int rowsAffected = context.getContentResolver().update(mCurrentItemUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(context, context.getString(R.string.sold_failed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.sold_success), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, "order@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Monthly order");
                intent.putExtra(Intent.EXTRA_STREAM, "Please send the next batch of order");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }

                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                image.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = image.getWidth();
        int targetH = image.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
            return null;
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
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
                boolean confirmation = saveItem();
                if (confirmation) {
                    finish();
                }
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
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_STOCK,
                ItemEntry.COLUMN_ITEM_IMAGE
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
            String imageLink = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE));

            if (imageLink != null) {
                image.setImageBitmap(getBitmapFromUri(Uri.parse(imageLink)));
            }

            String stockString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK));
            stock.setText(stockString);

            currentStock = Integer.parseInt(stockString);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameField.setText("");
        priceField.setText("");
        stock.setText("");
    }

    private boolean saveItem() {
        String nameString = nameField.getText().toString().trim();
        String priceString = priceField.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            nameField.setError(getString(R.string.name_error));
            return false;
        }
        if (TextUtils.isEmpty(priceString)) {
            priceField.setError(getString(R.string.price_error));
            return false;
        } else {
            int price = Integer.parseInt(priceString);
            if (price == 0) {
                priceField.setError(getString(R.string.price_error));
                return false;
            } else {
                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
                values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
                if (mUri != null) {
                    values.put(ItemEntry.COLUMN_ITEM_IMAGE, mUri.toString());
                }
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
                    return true;
                }
                //If item is previously available, use update function
                else {
                    int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                    //If no row was updated
                    if (rowsAffected == 0) {
                        Toast.makeText(this, getString(R.string.failed_update), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(this, getString(R.string.success_update), Toast.LENGTH_SHORT).show();
                    return true;
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
        builder.show();
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
