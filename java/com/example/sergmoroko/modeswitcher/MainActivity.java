package com.example.sergmoroko.modeswitcher;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ArrayList<ListItem> data = new ArrayList<>();
    private ArrayList<Integer> rowIDs = new ArrayList<>();
    SQLiteDatabase db;
    ModeSwitcherDbHelper dbHelper;
    MyListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = (ListView) findViewById(R.id.listview);
        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();


          // generateListContent();
            getData();
        listAdapter = new MyListAdapter(this, R.layout.list_item_with_button, data);
           lv.setAdapter(listAdapter);
           lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                          Toast.makeText(MainActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
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
                         viewHolder.button = (ImageButton) convertView.findViewById(R.id.image_button);
                         convertView.setTag(viewHolder);
                     }
                      ListItem item = items.get(position);
                      mainViewHolder = (ViewHolder) convertView.getTag();

                      mainViewHolder.title.setText(item.getTitle());
                      mainViewHolder.value.setText(item.getValue());

                      return convertView;
             }
        }
    public class ViewHolder {

        //ImageView thumbnail;
        TextView title;
        TextView value;
        ImageButton button;
        }

    private void getData(){

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
                int IDIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry._ID);

                do {
                    String title = cursor.getString(titleIndex);
                    String value = cursor.getString(valueIndex);

                    data.add(new ListItem(title, value));
                    rowIDs.add(cursor.getInt(IDIndex));

                }
                while (cursor.moveToNext());

            }

        }

    }
}
