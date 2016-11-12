package com.android.inventorystore;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.inventorystore.data.ItemContract.ItemEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by trikh on 10-11-2016.
 */

public class ItemCursorAdapter extends CursorAdapter {
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_inventory_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder listItemView = new ViewHolder(view);

        /*Set buttons with the tags to fetch position later*/
        listItemView.sellButton.setTag(cursor.getInt(cursor.getColumnIndex(ItemEntry._ID)));
        listItemView.addButton.setTag(cursor.getInt(cursor.getColumnIndex(ItemEntry._ID)));

        /*fetch details from the cursor*/
        String nameString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        String priceString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
        final String stockString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK));
        final int currentStock = Integer.parseInt(stockString);

        /* if stock is less or 0 set color sccordingly*/
        if (currentStock == 0) {
            view.setBackgroundColor(context.getResources().getColor(R.color.empty_stock));
        } else if (currentStock <= 5) {
            view.setBackgroundColor(context.getResources().getColor(R.color.stock_less));
        } else
            view.setBackgroundColor(context.getResources().getColor(R.color.background));

        /*set the view for the list item*/
        listItemView.item_name.setText(nameString);
        listItemView.item_price.setText(priceString);
        listItemView.item_stock.setText(stockString);

        /*setting on click for current item sell button*/
        listItemView.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.prompt_view, null);
                TextView headingPrompt = (TextView) promptView.findViewById(R.id.heading_prompt);
                headingPrompt.setText(context.getString(R.string.order));

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
                                Uri currentItemUri = Uri.withAppendedPath(ItemEntry.CONTENT_URI, "" + listItemView.addButton.getTag().toString());
                                int rowsAffected = context.getContentResolver().update(currentItemUri, values, null, null);
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

        listItemView.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                if (currentStock > 0) {
                    values.put(ItemEntry.COLUMN_ITEM_STOCK, currentStock - 1);
                    Uri currentItemUri = Uri.withAppendedPath(ItemEntry.CONTENT_URI, "" + listItemView.addButton.getTag().toString());
                    int rowsAffected = context.getContentResolver().update(currentItemUri, values, null, null);
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
    }

    static class ViewHolder {
        @BindView(R.id.name)
        TextView item_name;
        @BindView(R.id.price)
        TextView item_price;
        @BindView(R.id.stock)
        TextView item_stock;
        @BindView(R.id.sell_button)
        Button sellButton;
        @BindView(R.id.add_button)
        Button addButton;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
