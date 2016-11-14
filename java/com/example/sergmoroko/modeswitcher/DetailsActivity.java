package com.example.sergmoroko.modeswitcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ssss on 04.11.2016.
 */
public class DetailsActivity extends AppCompatActivity{
    private ArrayList<ListItem> data = new ArrayList<>();
    private ArrayList<Integer> timeData = new ArrayList<>();

    static int pickerHour = 0;
    static int pickerMinute = 0;
    static String time;
    //static TextView tv;
    static boolean timeSet;
    ArrayList mSelectedItems;
    ArrayList workdays = new ArrayList();
    static int currentPosition;

    private static Activity currentActivity;

    MyListAdapter listAdapter;

    int startHour = 0;
    int startMinute = 0;
    int breakStartHour = 0;
    int breakStartMinute = 0;
    int endHour = 0;
    int endMinute = 0;
    int breakLength = 0;
    int alarmMode = 0;
    //private ArrayList<Boolean> repeatData = new ArrayList<>();

    boolean[] repeat = {false, false, false, false, false, false, false};

    String descriptionText;

    //ArrayList<String> textValues = new ArrayList<>(Collections.nCopies(7, ""));




    ArrayList titles;
    //ArrayList textValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("ACTIVITY TEST CREATED");

        currentActivity = this;

        titles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.detailsTitles)));
        //textValues = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.detailsDefaultData)));

        setContentView(R.layout.activity_details);
        ListView lv = (ListView) findViewById(R.id.listview_details);
        if(data.isEmpty()){
            generateListContent();
        }
        listAdapter = new MyListAdapter(this, android.R.layout.simple_list_item_2, data);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                switch (position){
                    case 0:
                        showTimePickerDialog(view);
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;
                    case 1:
                        showTimePickerDialog(view);
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;
                    case 2:
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        ShowSeekDialog();
                        currentPosition = position;
                        break;
                    case 3:
                        showTimePickerDialog(view);
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;
                    case 4:
                        showRecurrencePickerDialog();
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;
                    case 5:
                        ShowModeDialog();
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;
                    case 6:
                        ShowNameDialog();
                        //tv = (TextView)view.findViewById(android.R.id.text2);
                        currentPosition = position;
                        break;

                }
            }
        });


        // TODO: 08.11.2016 BACK BUTTON
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("ACTIVITY TEST SAVE INSTANCE STATE CALLED");
        timeData.add(startHour);
        timeData.add(startMinute);
        timeData.add(endHour);
        timeData.add(endMinute);
        timeData.add(breakLength);
        timeData.add(alarmMode);
        timeData.add(breakStartHour);
        timeData.add(breakStartMinute);

        outState.putIntegerArrayList("time", timeData);

        outState.putBooleanArray("repeat", repeat);

        outState.putString("description", descriptionText);



    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        System.out.println("ACTIVITY TEST RESTORE CALLED");
        super.onRestoreInstanceState(savedInstanceState);


        startHour = savedInstanceState.getIntegerArrayList("time").get(0);
        startMinute = savedInstanceState.getIntegerArrayList("time").get(1);
        endHour = savedInstanceState.getIntegerArrayList("time").get(2);
        endMinute = savedInstanceState.getIntegerArrayList("time").get(3);
        breakLength = savedInstanceState.getIntegerArrayList("time").get(4);
        alarmMode = savedInstanceState.getIntegerArrayList("time").get(5);
        breakStartHour = savedInstanceState.getIntegerArrayList("time").get(6);
        breakStartMinute = savedInstanceState.getIntegerArrayList("time").get(7);

        for(int i = 0; i <repeat.length; i++){
            repeat[i] = savedInstanceState.getBooleanArray("repeat")[i];
        }

        descriptionText = savedInstanceState.getString("description");


        data.clear();
        data.add(new ListItem("Start Time", timeToString(startHour, startMinute)));
        data.add(new ListItem("Break Time", timeToString(breakStartHour, breakStartMinute)));
        data.add(new ListItem("Break Length", breakLengthTextToString(breakLength)));
        data.add(new ListItem("End Time", timeToString(endHour, endMinute)));
        data.add(new ListItem("Repeat", repeatTextToString(repeat)));
        data.add(new ListItem("Mode", soundProfileToString(alarmMode)));
        data.add(new ListItem("Name", (descriptionText)));

        listAdapter.notifyDataSetChanged();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ACTIVITY TEST DESTROYED");
    }


    //    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        System.out.println("CONFIG CHANGED");
//        setContentView(R.layout.activity_details);
//        generateListContent();
//    }

    private void generateListContent() {
            //data.add(new ListItem("Type", "Workday"));
//            data.add(new ListItem("Start Time", "8:30"));
//            data.add(new ListItem("Break Time", "13:00"));
//            data.add(new ListItem("Break Length", "1 h"));
//            data.add(new ListItem("End Time", "17:30"));
//            data.add(new ListItem("Repeat", "every day"));
//            data.add(new ListItem("Mode", "mode name"));
//            data.add(new ListItem("Name", "day shift"));

        data.add(new ListItem("Start Time", timeToString(startHour, startMinute)));
        data.add(new ListItem("Break Time", timeToString(breakStartHour, breakStartMinute)));
        data.add(new ListItem("Break Length", breakLengthTextToString(breakLength)));
        data.add(new ListItem("End Time", timeToString(endHour, endMinute)));
        data.add(new ListItem("Repeat", repeatTextToString(repeat)));
        data.add(new ListItem("Mode", soundProfileToString(alarmMode)));
        data.add(new ListItem("Name", (descriptionText)));



//        for(int i = 0; i < 7; i++){
//            data.add(new ListItem(titles.get(i).toString(), textValues.get(i).toString()));
//        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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


                viewHolder.title = (TextView) convertView.findViewById(android.R.id.text1);
                viewHolder.value = (TextView) convertView.findViewById(android.R.id.text2);

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
        TextView title;
        TextView value;
    }

    public class ListItem{

        private String title = null;
        private String value = null;

        ListItem() {
        }

        ListItem(String title, String value) {
            setTitle(title);
            setValue(value);
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeSet = true;
            pickerHour = hourOfDay;
            pickerMinute = minute;
        }


        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);

            if(timeSet) {
//                String hour;
//                String minute;
//                if(pickerHour < 10){
//                    hour = "0" + pickerHour;
//                }
//                else{
//                    hour = Integer.toString(pickerHour);
//                }
//                if(pickerMinute <10){
//                    minute = "0" + pickerMinute;
//                }
//                else{
//                    minute = Integer.toString(pickerMinute);
//                }
//
//                time = hour + ":" + minute;

                //tv.setText(setTimeText(pickerHour, pickerMinute));
                //data.get(currentPosition).setValue(profile);
                getInstance().setTimeText(pickerHour, pickerMinute);

                //tv.setText(time);


                timeSet = false;
            }

        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    /** BREAK LENGTH CHOOSE DIALOG **/

    public void ShowSeekDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(10);
        seek.setProgress(6);



        popDialog.setTitle("Set break length");
        popDialog.setMessage("60m");
        popDialog.setView(seek);

        // OK button
        popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //tv.setText((seek.getProgress() *10) + "m");
                //tv.setText(setBreakLengthText(seek.getProgress() *10));
                setBreakLengthText(seek.getProgress() *10);
                dialog.dismiss();
            }

        });
        // CANCEL button
        popDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        final AlertDialog dialog = popDialog.create();
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                progress = progressV *10;
            }

            public void onStartTrackingTouch(SeekBar arg0) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                dialog.setMessage(setBreakLengthText(progress));
            }
        });

        dialog.show();
    }

    /** MODE NAME INPUT DIALOG **/
    public void ShowNameDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        popDialog.setTitle("Set description");
        input.setText("sss");

        popDialog.setView(input);

        // OK button
        popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //tv.setText(input.getText());
                //tv.setText(setDescriptionText(input.getText().toString()));
                setDescriptionText(input.getText().toString());
                dialog.dismiss();
            }

        });

        // CANCEL button
        popDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        final AlertDialog dialog = popDialog.create();
        dialog.show();
    }

    /** MODE CHOOSE DIALOG **/
    public void ShowModeDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final Spinner spinner = new Spinner(this);

        popDialog.setTitle("Choose mode");

        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        popDialog.setView(spinner);


        // OK button
        popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //String[] choose = getResources().getStringArray(R.array.modes);
                //tv.setText(choose[spinner.getSelectedItemPosition()]);

                setSoundProfileText(spinner.getSelectedItemPosition());
                //tv.setText(setSoundProfileText(spinner.getSelectedItemPosition()));
                dialog.dismiss();
            }

        });

        // CANCEL button
        popDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        final AlertDialog dialog = popDialog.create();
        dialog.show();
    }


    /** RECURRENCE PICKER DIALOG**/

    public void showRecurrencePickerDialog(){

        mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        workdays.add(0);
        workdays.add(1);
        workdays.add(2);
        workdays.add(3);
        workdays.add(4);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


//        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
//        View v = inflater.inflate(R.layout.recurrence_picker, null);
//
//
//        final RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.recurrence_picker_radiogroup);
//        final RadioButton rbWorkDay = (RadioButton) v.findViewById(R.id.rb_wd);
//        final RadioButton rbEveryDay = (RadioButton) v.findViewById(R.id.rb_ed);
//        final RadioButton rbNotSelected = (RadioButton) v.findViewById(R.id.rb_ns);
//
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//                switch (checkedId){
//                    case R.id.rb_wd:
//
//                       break;
//                    case R.id.rb_ed:
//
//                        break;
//                    case R.id.rb_ns:
//
//                        break;
//                }
//
//            }
//        });


        // Set the dialog title
        builder.setTitle("Repeat")
  //              .setView(v)
                //.setView(R.layout.recurrence_picker)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.days, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }

//                                boolean noneOfThis = true;
//
//                                if(mSelectedItems.isEmpty()){
//                                    rbNotSelected.setChecked(true);
//                                    noneOfThis = false;
//                                }
//                                if(mSelectedItems.size() == 7){
//                                    rbEveryDay.setChecked(true);
//                                    noneOfThis = false;
//                                }
//                                if(mSelectedItems.containsAll(workdays) && mSelectedItems.size() == 5){
//                                    rbWorkDay.setChecked(true);
//                                    noneOfThis = false;
//                                }
//                                if(noneOfThis){
//                                    rbNotSelected.setChecked(false);
//                                    rbWorkDay.setChecked(false);
//                                    rbEveryDay.setChecked(false);
//                                }
                            }

                        })

                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
//                        String repDays ="";
//
//                        for(int i =0; i <mSelectedItems.size(); i++){
//                            String day = getResources().getStringArray(R.array.days)[i];
//                            if(i < mSelectedItems.size() - 1){
//                                repDays = repDays + day.substring(0,3) + ", ";
//                            }
//                            else{
//                                repDays = repDays + day.substring(0,3);
//                            }
//                        }

                        //tv.setText(setRepeatText(mSelectedItems));
                        setRepeatText(mSelectedItems);

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });


        builder.create();
        builder.show();
    }

    public String setTimeText(int hour, int minute){
        String h;
        String m;
        String time;
        if(hour < 10){
            h = "0" + hour;
        }
        else{
            h = Integer.toString(hour);
        }
        if(minute <10){
            m = "0" + minute;
        }
        else{
            m = Integer.toString(minute);
        }

        time = h + ":" + m;

        //textValues.set(currentPosition, time);
        data.get(currentPosition).setValue(time);
        switch (currentPosition){
            case 0:
                startHour = hour;
                startMinute = minute;
                break;
            case 1:
                breakStartHour = hour;
                breakStartMinute = minute;
                break;
            case 3:
                endHour = hour;
                endMinute = minute;
                break;
        }
        listAdapter.notifyDataSetChanged();

        return time;
    }

    public String setBreakLengthText(int length){
        String l;
        if(length < 60){
            l = length + "m";
        }
        else{
            l = length / 60 + "h" + " " + length % 60 + "m";
        }

        //textValues.set(currentPosition, l);
        data.get(currentPosition).setValue(l);
        breakLength = length;
        listAdapter.notifyDataSetChanged();
        return l;
    }

    public String setRepeatText(ArrayList<Integer> days){

        String d ="";

        for(int i =0; i <days.size(); i++){

            String day = getResources().getStringArray(R.array.days)[days.get(i)];

            if(i < days.size() - 1){
                d = d + day.substring(0,3) + ", ";
            }
            else{
                d = d + day.substring(0,3);
            }
        }
        //textValues.set(currentPosition, d);
        data.get(currentPosition).setValue(d);

        for(int i: days){
            repeat[i] = true;
        }

        listAdapter.notifyDataSetChanged();

        return d;
    }

    public String setSoundProfileText(int i){

        String[] profiles = getResources().getStringArray(R.array.modes);
        String profile = profiles[i];
        //textValues.set(currentPosition, profile);
        data.get(currentPosition).setValue(profile);
        alarmMode = i;
        listAdapter.notifyDataSetChanged();


        return profile;
    }

    public String setDescriptionText(String description){
        //textValues.set(currentPosition, description);
        data.get(currentPosition).setValue(description);
        descriptionText = description;
        listAdapter.notifyDataSetChanged();

        return description;
    }

    public static DetailsActivity getInstance(){
        return (DetailsActivity) currentActivity;
    }

    private String timeToString(int hour, int minute){
        String h;
        String m;
        String time;
        if(hour < 10){
            h = "0" + hour;
        }
        else{
            h = Integer.toString(hour);
        }
        if(minute <10){
            m = "0" + minute;
        }
        else{
            m = Integer.toString(minute);
        }

        time = h + ":" + m;

        return time;
    }

    private String soundProfileToString(int i){
        String[] profiles = getResources().getStringArray(R.array.modes);
        String profile = profiles[i];
        return profile;
    }

    private String repeatTextToString(boolean[] days){

        String d ="";

        for(int i =0; i <days.length; i++){
            if(days[i]) {

                String day = getResources().getStringArray(R.array.days)[i];

                if (i < days.length - 1) {
                    d = d + day.substring(0, 3) + ", ";
                } else {
                    d = d + day.substring(0, 3);
                }
            }
        }

        return d;
    }

    private String breakLengthTextToString(int length){
        String l;
        if(length < 60){
            l = length + "m";
        }
        else{
            l = length / 60 + "h" + " " + length % 60 + "m";
        }
        return l;
    }



}
