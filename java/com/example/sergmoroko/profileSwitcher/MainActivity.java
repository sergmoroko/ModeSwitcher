package com.example.sergmoroko.profileSwitcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<ListItem> data = new ArrayList<>();
    private ArrayList<Integer> rowIDs = new ArrayList<>();
    private SQLiteDatabase db;
    private ModeSwitcherDbHelper dbHelper;
    private MyListAdapter listAdapter;
    private AlarmReceiver alarm = new AlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView) findViewById(R.id.listView);

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        // Check if it is a first start of activity, or user returned after modifying or deleting alarm
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        // Intent includes extras to perform specified action
        if (extras != null) {
            // ID of row
            int rowId = intent.getIntExtra(Constants.INTENT_EXTRA_ROW_ID, -1);
            switch (extras.getString(Constants.INTENT_EXTRA_STATUS, "")) {
                // Activity started after modifying alarm details
                case Constants.INTENT_EXTRA_TURN_ON:
                    // turn alarm on
                    alarm.setSingleAlarm(getBaseContext(), rowId);
                    break;
                // Activity started after alarm deletion
                case Constants.INTENT_EXTRA_DELETE:
                    // Cancel existing alarms
                    alarm.cancelAlarms(getBaseContext(), rowId);
                    // Delete alarm entry from database
                    deleteDbEntry(rowId);
                    break;
            }
        }
        // get data from database
        getData();

        // setting  listAdapter to listView's
        listAdapter = new MyListAdapter(this, R.layout.list_item_with_button, data);
        if (lv != null) {
            lv.setAdapter(listAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                    // adds row ID to extra
                    intent.putExtra(Constants.INTENT_EXTRA_ID, rowIDs.get(position));

                    startActivity(intent);
                }
            });
        }


        // "Add New Alarm" button
        Button addNew = (Button) findViewById(R.id.main_activity_add_new_button);
        if (addNew != null) {
            addNew.setOnClickListener(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, add settings button to the action bar
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_activity_add_new_button) {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_ID, -1);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh data in list
        getData();
        listAdapter.notifyDataSetChanged();
    }

    // List Adapter class that populates listView with data
    private class MyListAdapter extends ArrayAdapter<ListItem> {
        private int layout;
        private List<ListItem> items;

        private MyListAdapter(Context context, int resource, List<ListItem> objects) {
            super(context, resource, objects);
            items = objects;
            layout = resource;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder mainViewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();

                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_with_button_text1);
                viewHolder.value = (TextView) convertView.findViewById(R.id.list_item_with_button_text2);
                viewHolder.button = (ToggleButton) convertView.findViewById(R.id.toggleButton);
                viewHolder.description = (TextView) convertView.findViewById(R.id.list_item_with_button_text3);

                convertView.setTag(viewHolder);
            }
            final ListItem item = items.get(position);
            mainViewHolder = (ViewHolder) convertView.getTag();

            mainViewHolder.title.setText(item.getTitle());
            mainViewHolder.value.setText(item.getValue());

            // hide description line, if it doesn't exists
            if (item.getDescription() == null) {
                mainViewHolder.description.setVisibility(View.GONE);
            } else {
                mainViewHolder.description.setText(item.getDescription());
            }

            // sets toggleButton state
            if (item.getEnabled()) {
                mainViewHolder.button.setChecked(true);
            } else {
                mainViewHolder.button.setChecked(false);
            }

            mainViewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stateUpdate(item.getEnabled(), position);
                    // turns on/off alarms and updates alarm status in db
                    if (item.getEnabled()) {
                        item.setEnabled(false);
                        alarm.cancelAlarms(getContext(), rowIDs.get(position));
                    } else {
                        item.setEnabled(true);
                        alarm.setSingleAlarm(getContext(), rowIDs.get(position));
                    }
                }
            });
            return convertView;
        }
    }


    // Deletes row from db by its ID
    private void deleteDbEntry(int id) {
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();
        String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(ModeSwitcherDbContract.dataEntry.TABLE_NAME, whereClause, whereArgs);

        db.close();
        dbHelper.close();
    }

    // method that queries data from db
    private void getData() {
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        if (!data.isEmpty()) {
            data.clear();
        }
        if (!rowIDs.isEmpty()) {
            rowIDs.clear();
        }

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, null, null, null, null, null);
        // checks if db is not empty
        if (cursor.getCount() != 0) {

            if (cursor.moveToFirst()) {

                int titleIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_SUMMARY);
                int valueIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_STRING);
                int descriptionIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_DESCRIPTION);
                int IDIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry._ID);
                int enabledID = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_STATE);

                do {
                    String title = cursor.getString(titleIndex);
                    String value = cursor.getString(valueIndex);
                    String description = cursor.getString(descriptionIndex);
                    boolean isEnabled = cursor.getInt(enabledID) == 1;

                    // adds data to list
                    data.add(new ListItem(title, value, description, isEnabled));
                    // adds current ID to list of ID's
                    rowIDs.add(cursor.getInt(IDIndex));

                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();
        dbHelper.close();
    }

    // method updates alarm status in db
    private void stateUpdate(boolean value, int position) {
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_STATE, value ? 0 : 1);

        String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
        String[] whereArgs = new String[]{String.valueOf(rowIDs.get(position))};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        db.close();
        dbHelper.close();

    }
}
