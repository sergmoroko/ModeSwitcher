package com.example.sergmoroko.modeswitcher;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ArrayList<String> data = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = (ListView) findViewById(R.id.listview);
           generateListContent();
           lv.setAdapter(new MyListAdaper(this, R.layout.list_item, data));
           lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                          Toast.makeText(MainActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                      }
               });


        Button addNew = (Button) findViewById(R.id.main_activity_add_new_button);
        addNew.setOnClickListener(this);
       }

           private void generateListContent() {
           for(int i = 0; i < 3; i++) {
                   data.add("This is row number " + i);
               }
       }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_activity_add_new_button){
            Intent intent = new Intent(this, DetailsActivity.class);
            startActivity(intent);
        }
    }

    private class MyListAdaper extends ArrayAdapter<String> {
           private int layout;
           private List<String> mObjects;
           private MyListAdaper(Context context, int resource, List<String> objects) {
                   super(context, resource, objects);
                   mObjects = objects;
                   layout = resource;
               }

                  @Override
          public View getView(final int position, View convertView, ViewGroup parent) {
                  ViewHolder mainViewholder = null;
                  if(convertView == null) {
                          LayoutInflater inflater = LayoutInflater.from(getContext());
                         convertView = inflater.inflate(layout, parent, false);
                         ViewHolder viewHolder = new ViewHolder();
                         viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
                         viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text);
                         viewHolder.button = (Button) convertView.findViewById(R.id.list_item_btn);
                         convertView.setTag(viewHolder);
                     }
                  mainViewholder = (ViewHolder) convertView.getTag();
                  mainViewholder.button.setOnClickListener(new View.OnClickListener() {
                             @Override
                          public void onClick(View v) {
                                  Toast.makeText(getContext(), "Button was clicked for list item " + position, Toast.LENGTH_SHORT).show();
                              }
                      });
               mainViewholder.title.setText(getItem(position));

                      return convertView;
             }
        }
    public class ViewHolder {

        ImageView thumbnail;
        TextView title;
        Button button;
        }
}
