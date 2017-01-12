package com.example.sergmoroko.modeswitcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ArrayList<ListItem> data = new ArrayList<>();
    private ArrayList<Integer> rowIDs = new ArrayList<>();
    SQLiteDatabase db;
    ModeSwitcherDbHelper dbHelper;
    MyListAdapter listAdapter;
    AlarmReceiver alarm = new AlarmReceiver();
    //boolean isEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = (ListView) findViewById(R.id.listview);
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras!= null){
            Log.d("test1", "intent catched, extras != null");
            int rowId = intent.getIntExtra("rowId", -1);
            switch (extras.getString("status", "")){
                case "turn_on":
                    alarm.setSingleAlarm(getBaseContext(), rowId);
                    Log.d("test1", "turn on alarm");
                    break;
                case "delete":
                    alarm.cancelAlarms(getBaseContext(), rowId);
                    deleteDbEntry(rowId);
                    Log.d("test1", "alarms cancelled, row deleted");
                    break;
            }
        }



          // generateListContent();
            getData();
        listAdapter = new MyListAdapter(this, R.layout.list_item_with_button, data);
           lv.setAdapter(listAdapter);
           lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                          //Toast.makeText(MainActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                       Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                       if(!rowIDs.isEmpty()){
                           intent.putExtra("id", rowIDs.get(position));
                       }
                       else{
                           intent.putExtra("id", -1);
                       }
                       startActivity(intent);
                      }
               });


//        if(intent!= null){
//            Log.d("test1", "intent catched, its != null");
//            int rowId = intent.getIntExtra("rowId", -1);
//            if(rowId != -1){
//                Log.d("test1", "intent catched, alarm fired");
//                alarm.setSingleAlarm(getBaseContext(), rowId);
//            }
//            // if goback caused by exit or done
//        }


        Button addNew = (Button) findViewById(R.id.main_activity_add_new_button);
        addNew.setOnClickListener(this);
       }


    //           private void generateListContent() {
//           for(int i = 0; i < 3; i++) {
//                   data.add("This is row number " + i);
//               }
//       }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
            intent.putExtra("id", -1);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
        listAdapter.notifyDataSetChanged();
    }

    private class MyListAdapter extends ArrayAdapter<ListItem> {
        private int layout;
        private List<ListItem> items;
        private MyListAdapter(Context context, int resource, List<ListItem> objects) {
            super(context, resource, objects);
            items = objects;
            layout = resource;
        }

                  @Override
          public View getView(final int position, View convertView, ViewGroup parent) {
                      ViewHolder mainViewHolder;

                      if(convertView == null) {
                          LayoutInflater inflater = LayoutInflater.from(getContext());
                          convertView = inflater.inflate(layout, parent, false);
                          ViewHolder viewHolder = new ViewHolder();

                         viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_with_button_text1);
                         viewHolder.value = (TextView) convertView.findViewById(R.id.list_item_with_button_text2);
                         //viewHolder.button = (ImageButton) convertView.findViewById(R.id.image_button);
                          viewHolder.button = (ToggleButton) convertView.findViewById(R.id.toggleButton);
                          viewHolder.description = (TextView) convertView.findViewById(R.id.list_item_with_button_text3);

                         convertView.setTag(viewHolder);
                     }
                      final ListItem item = items.get(position);
                      mainViewHolder = (ViewHolder) convertView.getTag();

                      mainViewHolder.title.setText(item.getTitle());
                      mainViewHolder.value.setText(item.getValue());
                      if(item.getDescription() == null){
                      mainViewHolder.description.setVisibility(View.GONE);
                      }
                      else {
                          mainViewHolder.description.setText(item.getDescription());
                      }

                      if (item.getEnabled()) {
                          //mainViewHolder.button.setImageResource(R.drawable.modeswitcher_switch_button_on_72);
                          mainViewHolder.button.setChecked(true);
                      } else {
                          //mainViewHolder.button.setImageResource(R.drawable.modeswitcher_switch_button_off_72);
                          mainViewHolder.button.setChecked(false);

                      }
                      mainViewHolder.button.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              stateUpdate(item.getEnabled(), position);
                              System.out.println("BUTTON TEST - CLICKED");
                              if(item.getEnabled()){
                                  item.setEnabled(false);
                                  //alarm.setAlarms(getContext());
                                  alarm.cancelAlarms(getContext(), rowIDs.get(position));
                                  System.out.println("BUTTON TEST DISABLED");
                              }
                              else{
                                  item.setEnabled(true);
                                  System.out.println("BUTTON TEST ENABLED");
                                  alarm.setSingleAlarm(getContext(), rowIDs.get(position));
                              }
                          }
                      });

                      return convertView;
             }
        }
    public class ViewHolder {

        //ImageView thumbnail;
        TextView title;
        TextView value;
        TextView description;
        //ImageButton button;
        ToggleButton button;
        }

    private void deleteDbEntry(int id) {

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();
        String whereClause = "_ID=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(ModeSwitcherDbContract.dataEntry.TABLE_NAME, whereClause, whereArgs);

        dbHelper.close();
    }

    private void getData(){
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        if(!data.isEmpty()){
            data.clear();
        }
        if(!rowIDs.isEmpty()){
            rowIDs.clear();
        }

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, null, null, null, null, null);
        if(cursor.getCount()!= 0) {

            if (cursor.moveToFirst()) {

                int titleIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_SUMMARY);
                int valueIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_STRING);
                int descriptionIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_DESCRIPTION);
                int IDIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry._ID);
                int enabledID = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_STATE);
                //int lastAlarmID = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_LAST_ALARM_TIME);

                do {
                    String title = cursor.getString(titleIndex);
                    String value = cursor.getString(valueIndex);
                    String description = cursor.getString(descriptionIndex);
                    //long lastAlarmTime = cursor.getLong(lastAlarmID);
                    int sw = cursor.getInt(enabledID);
                    boolean isEnabled = false;

//                    if(sw == 1 && lastAlarmTime!= 0 && lastAlarmTime > Calendar.getInstance().getTimeInMillis()){
//                        isEnabled = true;
//                    }
//                    else{
//                        stateUpdate(false, cursor.getInt(IDIndex));
//                    }

                    if(sw == 1){
                        isEnabled = true;
                    }

                    data.add(new ListItem(title, value, description, isEnabled));
                    rowIDs.add(cursor.getInt(IDIndex));

                }
                while (cursor.moveToNext());

            }

        }

        dbHelper.close();
    }

    private void stateUpdate(boolean value, int position){
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        if(value){
            cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_STATE, 0);
        }
        else {
            cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_STATE, 1);
        }

        String whereClause = "_ID=?";
        String[] whereArgs = new String[] { String.valueOf(rowIDs.get(position)) };
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        dbHelper.close();

    }
}
