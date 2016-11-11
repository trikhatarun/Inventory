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
        listItemView.sellButton.setTag(cursor.getPosition());
        listItemView.addButton.setTag(cursor.getPosition());

        /*fetch details from the cursor*/
        String nameString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        String priceString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
        final String stockString = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK));

        /*set the view for the list item*/
        listItemView.item_name.setText(nameString);
        listItemView.item_price.setText(priceString);
        listItemView.item_stock.setText(stockString);

        /*setting on click for current item sell button*/
        listItemView.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater li = LayoutInflater.from(context);

                View promptView = li.inflate(R.layout.prompt_view, null);
                TextView headingPrompt = (TextView) promptView.findViewById(R.id.heading_prompt);
                headingPrompt.setText(context.getString(R.string.sold));

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptView);

                final EditText userInput = (EditText) promptView.findViewById(R.id.input);

                alertDialogBuilder.setCancelable(false).setPositiveButton(context.getText(R.string.sell_string), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Integer quantitySold = Integer.parseInt(userInput.getText().toString().trim());
                        ContentValues values = new ContentValues();
                        values.put(ItemEntry.COLUMN_ITEM_STOCK, Integer.parseInt(stockString) - quantitySold);
                        Uri currentItemUri = Uri.withAppendedPath(ItemEntry.CONTENT_URI, "" + listItemView.sellButton.getTag() + 1);
                        context.getContentResolver().update(currentItemUri, values, null, null);
                    }
                });
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
