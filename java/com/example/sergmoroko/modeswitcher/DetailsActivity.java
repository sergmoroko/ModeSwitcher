package com.example.sergmoroko.modeswitcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.example.sergmoroko.modeswitcher.ModeSwitcherDbContract.dataEntry;

/**
 * Created by ssss on 04.11.2016.
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<ListItem> data = new ArrayList<>();
    private ArrayList<Integer> timeData = new ArrayList<>();
    private long id;

    SQLiteDatabase db;

    static int pickerHour = 0;
    static int pickerMinute = 0;
    static boolean timeSet;


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
    int repeatType = 0;
    long repeatStart = 0;

    boolean[] repeat = {false, false, false, false, false, false, false};

    String descriptionText;

    ModeSwitcherDbHelper dbHelper;

    ArrayList titles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("ACTIVITY TEST CREATED");

        currentActivity = this;

        titles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.detailsTitles)));

        id = this.getIntent().getIntExtra("id", 0);

        setContentView(R.layout.activity_details);
        ListView lv = (ListView) findViewById(R.id.listview_details);
        Button doneBtn = (Button) findViewById(R.id.details_button_done);
        Button deleteBtn = (Button) findViewById(R.id.details_button_delete);
        Button cancelBtn = (Button) findViewById(R.id.details_button_cancel);
        doneBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        if (!isNew()) {

            getDbData();
        } else {
            deleteBtn.setVisibility(View.GONE);
        }

        if (data.isEmpty()) {
            generateListContent();
        }
        listAdapter = new MyListAdapter(this, android.R.layout.simple_list_item_2, data);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                currentPosition = position;
                switch (position) {
                    case 0:
                        showTimePickerDialog(view);
                        break;
                    case 1:
                        showTimePickerDialog(view);
                        break;
                    case 2:
                        ShowSeekDialog();
                        break;
                    case 3:
                        showTimePickerDialog(view);
                        break;
                    case 4:
                        showRecurrencePickerDialog();
                        break;
                    case 5:
                        ShowModeDialog();
                        break;
                    case 6:
                        ShowNameDialog();
                        break;

                }
            }
        });

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

        outState.putInt("repeatType", repeatType);
        outState.putLong("repeatStart", repeatStart);

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

        for (int i = 0; i < repeat.length; i++) {
            repeat[i] = savedInstanceState.getBooleanArray("repeat")[i];
        }

        descriptionText = savedInstanceState.getString("description");

        repeatType = savedInstanceState.getInt("repeatType");
        repeatStart = savedInstanceState.getLong("repeatStart");


        data.clear();
        data.add(new ListItem("Start Time", timeToString(startHour, startMinute)));
        data.add(new ListItem("Break Time", timeToString(breakStartHour, breakStartMinute)));
        data.add(new ListItem("Break Length", breakLengthTextToString(breakLength)));
        data.add(new ListItem("End Time", timeToString(endHour, endMinute)));
        data.add(new ListItem("Repeat", repeatTextToString(repeat)));
        data.add(new ListItem("Sound Profile", soundProfileToString(alarmMode)));
        data.add(new ListItem("Description (optional)", (descriptionText)));

        listAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ACTIVITY TEST DESTROYED");
    }


    private void generateListContent() {

        if (!data.isEmpty()) {
            data.clear();
        }

        data.add(new ListItem("Start Time", timeToString(startHour, startMinute)));
        data.add(new ListItem("Break Time", timeToString(breakStartHour, breakStartMinute)));
        data.add(new ListItem("Break Length", breakLengthTextToString(breakLength)));
        data.add(new ListItem("End Time", timeToString(endHour, endMinute)));
        data.add(new ListItem("Repeat", repeatTextToString(repeat)));
        data.add(new ListItem("Sound Profile", soundProfileToString(alarmMode)));
        data.add(new ListItem("Description (optional)", (descriptionText)));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.details_button_done:
                putDbData();
                doneBack();
                //goBack();
                break;
            case R.id.details_button_delete:
                //deleteDbEntry();
                //deleteBack();
                showDeleteConfirmationDialog();
                //goBack();
                break;
            case R.id.details_button_cancel:
                goBack();
                break;
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

            if (convertView == null) {
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


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = 0;
            int minute = 0;
            if (getInstance().isNew()) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } else {
                switch (currentPosition) {
                    case 0:
                        hour = getInstance().startHour;
                        minute = getInstance().startMinute;
                        break;
                    case 1:
                        hour = getInstance().breakStartHour;
                        minute = getInstance().breakStartMinute;
                        break;
                    case 3:
                        hour = getInstance().endHour;
                        minute = getInstance().endMinute;
                        break;
                }
            }


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

            if (timeSet) {
                getInstance().setTimeText(pickerHour, pickerMinute);
                timeSet = false;
            }
        }


    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public void ShowSeekDialog() {
        BreakLengthDialog breakLengthDialog = new BreakLengthDialog();
        breakLengthDialog.show(getSupportFragmentManager(), "breakLengthDialog");
    }

    /**
     * BREAK LENGTH CHOOSE DIALOG
     **/

    public static class BreakLengthDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            final SeekBar seek = new SeekBar(getActivity());
            seek.setId(R.id.seek_bar);
            seek.setMax(10);
            seek.setProgress(((DetailsActivity) getActivity()).breakLength / 10);

            popDialog.setTitle("Set break length");

            popDialog.setView(seek);
            popDialog.setMessage(((DetailsActivity) getActivity()).breakLengthTextToString(((DetailsActivity) getActivity()).breakLength));

            // OK button
            popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    ((DetailsActivity) getActivity()).setBreakLengthText(seek.getProgress() * 10);
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
                    progress = progressV * 10;
                    dialog.setMessage(((DetailsActivity) getActivity()).breakLengthTextToString(seek.getProgress() * 10));
                }

                public void onStartTrackingTouch(SeekBar arg0) {

                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    dialog.setMessage(((DetailsActivity) getActivity()).setBreakLengthText(progress));
                }
            });


            return dialog;
        }
    }

    /**
     * DELETE CONFIRMATION DIALOG
     * */

    public static class DeleteConfirmationDialog extends DialogFragment{
        AlertDialog.Builder confirmationDialog;
        TextView text;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            confirmationDialog = new AlertDialog.Builder(getActivity());
            text = new TextView(getActivity());
            confirmationDialog.setTitle("Delete entry");
            confirmationDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //delete row
                    getInstance().deleteBack();
                }
            });
            confirmationDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // cancel
                    dialog.dismiss();
                }
            });

            return confirmationDialog.create();
        }
    }

    private void showDeleteConfirmationDialog(){

        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        dialog.show(getSupportFragmentManager(), "deleteConfirmationDialog");
    }

    // show mode name dialog
    private void ShowNameDialog() {
        NameDialog nameDialog = new NameDialog();
        nameDialog.show(getSupportFragmentManager(), "nameDialog");
    }

    /**
     * MODE NAME INPUT DIALOG
     **/
    public static class NameDialog extends DialogFragment {
        AlertDialog.Builder popDialog;
        EditText input;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            popDialog = new AlertDialog.Builder(getActivity());
            input = new EditText(getActivity());
            input.setId(R.id.edittext);

            popDialog.setTitle("Set description");
            if (((DetailsActivity) getActivity()).descriptionText != null) {
                input.setText(((DetailsActivity) getActivity()).descriptionText);
            }

            popDialog.setView(input);

            // OK button
            popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((DetailsActivity) getActivity()).setDescriptionText(input.getText().toString());
                    dialog.dismiss();
                }

            });

            // CANCEL button
            popDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            return popDialog.create();
        }
    }

    // show mode choose dialog
    public void ShowModeDialog() {

        ModeDialog modeDialog = new ModeDialog();
        modeDialog.show(getSupportFragmentManager(), "modeDialog");
    }

    /**
     * MODE CHOOSE DIALOG
     **/
    public static class ModeDialog extends DialogFragment {


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            final Spinner spinner = new Spinner(getActivity());
            spinner.setId(R.id.spinner);

            popDialog.setTitle("Choose mode");

            ArrayAdapter<?> adapter =
                    ArrayAdapter.createFromResource(getActivity(), R.array.modes, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);
            spinner.setSelection(((DetailsActivity) getActivity()).alarmMode);

            popDialog.setView(spinner);


            // OK button
            popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((DetailsActivity) getActivity()).setSoundProfileText(spinner.getSelectedItemPosition());
                    dialog.dismiss();
                }

            });

            // CANCEL button
            popDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            return popDialog.create();

        }


    }

    public void showRecurrencePickerDialog() {
        RecurrencePickerDialog recurrencePickerDialog = new RecurrencePickerDialog();
        recurrencePickerDialog.show(getSupportFragmentManager(), "rec");
    }

    /**
     * RECURRENCE PICKER DIALOG
     **/
    public static class RecurrencePickerDialog extends DialogFragment {
        ArrayList mSelectedItems;
        ArrayList workdays = new ArrayList();
        RadioGroup radioGroup1;
        RadioButton rbWorkDay;
        RadioButton rbEveryDay;
        RadioButton rbNotSelected;
        ListView listView;
        boolean[] repeat;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            repeat = ((DetailsActivity) getActivity()).repeat.clone();

            if (savedInstanceState != null) {
                repeat = savedInstanceState.getBooleanArray("repeat_temp");
            }

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.recurrence_picker, null);

            radioGroup1 = (RadioGroup) v.findViewById(R.id.recurrence_picker_radiogroup);
            rbWorkDay = (RadioButton) v.findViewById(R.id.rb_wd);
            rbEveryDay = (RadioButton) v.findViewById(R.id.rb_ed);
            rbNotSelected = (RadioButton) v.findViewById(R.id.rb_ns);


            //Calendar calendar = Calendar.getInstance();
//            int week = calendar.get(Calendar.WEEK_OF_YEAR);
//            //week = calendar.get(Calendar.WEEK_OF_MONTH);
//            int date = calendar.get(Calendar.DATE);
//
//            System.out.println("WEEK NUMBER" + date);
//
//            String[] weeklyRepeatItems = getResources().getStringArray(R.array.repeat).clone();
//            String nowIs;
//
//            if (week % 2 == 0) {
//                nowIs = "(this week is even)";
//                weeklyRepeatItems[3] = weeklyRepeatItems[3] + " " + nowIs;
//            } else {
//                nowIs = "(this week is odd)";
//                weeklyRepeatItems[2] = weeklyRepeatItems[2] + " " + nowIs;
//            }


            int repeatPos = ((DetailsActivity) getActivity()).repeatType;
            long thisMonday = getThisMonday();

            //calendar.set(Calendar.)
            long presavedTime = ((DetailsActivity) getActivity()).repeatStart;

            System.out.println("TEST123 " + thisMonday + " - now" );
            System.out.println("TEST123 " + presavedTime + " - start time" );

            int weeksElapsed = (int) ((thisMonday - presavedTime) / (1000*60*60*24*7));

            //int spinnerPos;

//            if (weeksElapsed % 2 == 0) {
//                spinnerPos = 4;
//
//            } else {
//                spinnerPos = 3;
//            }



            final Spinner repeatSpinner = (Spinner) v.findViewById(R.id.repeat_spinner);
            ArrayAdapter<?> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.repeat, android.R.layout.simple_spinner_item);
            //ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, weeklyRepeatItems);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            repeatSpinner.setAdapter(spinnerAdapter);
            //repeatSpinner.setSelection();

            // if saved
            if(repeatPos == 2 || repeatPos == 3){
                if (weeksElapsed % 2 == 0) {
                    repeatPos = 2;
                    System.out.println("TEST123 " + weeksElapsed + " repeatPos = " + repeatPos);

                } else {
                    repeatPos = 3;
                    System.out.println("TEST123 " + weeksElapsed + " repeatPos = " + repeatPos);
                }
            }
            repeatSpinner.setSelection(repeatPos);




            // Set the dialog title
            builder.setTitle("Repeat")
                    .setView(v)
                    .setMultiChoiceItems(R.array.days, repeat, null)

                    // Set the action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            listCheck(listView);
                            ((DetailsActivity) getActivity()).data.get(currentPosition)
                                    .setValue(((DetailsActivity) getActivity()).repeatTextToString(((DetailsActivity) getActivity()).repeat));

                            //setRepeatStartTime();
                            int spinnerPos = repeatSpinner.getSelectedItemPosition();

                            System.out.println();


                            switch (spinnerPos){
                                case 2:
                                    // save time of current week beginning
                                    // spinnerPos = 3
                                    setRepeatStartTime(true);

                                    break;
                                case 3:
                                    // save time of next week beginning
                                    // spinnerPos = 4
                                    setRepeatStartTime(false);
                                    break;

                            }
                            ((DetailsActivity) getActivity()).repeatType = spinnerPos;

                            ((DetailsActivity) getActivity()).listAdapter.notifyDataSetChanged();

                        }
                    })

                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });


            AlertDialog dialog = builder.create();

            listView = dialog.getListView();
            //listView.setId(R.id.list_view);


            rbWorkDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 5; i++) {
                        if (!listView.isItemChecked(i)) {
                            listView.performItemClick(listView, i, 0);
                        }
                    }
                    for (int y = 5; y < 7; y++) {
                        if (listView.isItemChecked(y)) {
                            listView.performItemClick(listView, y, 0);
                        }
                    }
                    radioGroup1.check(R.id.rb_wd);
                }
            });

            rbEveryDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 7; i++) {
                        if (!listView.isItemChecked(i)) {
                            listView.performItemClick(listView, i, 0);
                        }
                    }
                    radioGroup1.check(R.id.rb_ed);
                }
            });

            rbNotSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 7; i++) {
                        if (listView.isItemChecked(i)) {
                            listView.performItemClick(listView, i, 0);
                        }
                    }
                    radioGroup1.check(R.id.rb_ns);
                }
            });

            workdays.add(0);
            workdays.add(1);
            workdays.add(2);
            workdays.add(3);
            workdays.add(4);

            mSelectedItems = new ArrayList<>();
            for (int i = 0; i < repeat.length; i++) {
                if (repeat[i]) {
                    mSelectedItems.add(i);
                }
            }
            checkRadioButtons();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    boolean isChecked = listView.isItemChecked(position);
                    if (isChecked) {

                        // If the user checked the item, add it to the selected items
                        repeat[position] = true;
                        mSelectedItems.add(position);

                    } else {

                        if (mSelectedItems.contains(position)) {
                            // Else, if the item is already in the array, remove it
                            repeat[position] = false;
                            mSelectedItems.remove(Integer.valueOf(position));
                        }
                    }

                    checkRadioButtons();
                }
            });

            return dialog;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putBooleanArray("repeat_temp", repeat);
            super.onSaveInstanceState(outState);
        }

        private void listCheck(ListView listView) {
            for (int i = 0; i < listView.getCount(); i++) {
                ((DetailsActivity) getActivity()).repeat[i] = listView.isItemChecked(i);
            }
        }

        private void setRepeatStartTime(boolean thisWeek){

            if(thisWeek) {
                ((DetailsActivity) getActivity()).repeatStart = getThisMonday();
            }
            else{
                ((DetailsActivity) getActivity()).repeatStart = getThisMonday() + (1000*60*60*24*7);
            }
            //System.out.println( "TEST123 " + calendar.getTimeInMillis());
        }

        private long getThisMonday(){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }

        private void checkRadioButtons() {
            boolean noneOfThis = true;
            if (mSelectedItems.isEmpty()) {
                rbNotSelected.setChecked(true);
                noneOfThis = false;
            }
            if (mSelectedItems.size() == 7) {
                rbEveryDay.setChecked(true);
                noneOfThis = false;
            }
            if (mSelectedItems.containsAll(workdays)
                    && mSelectedItems.size() == 5) {
                rbWorkDay.setChecked(true);
                noneOfThis = false;
            }
            if (noneOfThis) {
                radioGroup1.clearCheck();
            }
        }
    }


    public String setTimeText(int hour, int minute) {
        String h;
        String m;
        String time;
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = Integer.toString(hour);
        }
        if (minute < 10) {
            m = "0" + minute;
        } else {
            m = Integer.toString(minute);
        }

        time = h + ":" + m;

        data.get(currentPosition).setValue(time);
        switch (currentPosition) {
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

    public String setBreakLengthText(int length) {
        String l;
        if (length < 60) {
            l = length + "m";
        } else {
            l = length / 60 + "h" + " " + length % 60 + "m";
        }

        data.get(currentPosition).setValue(l);
        breakLength = length;
        listAdapter.notifyDataSetChanged();
        return l;
    }


    public String setSoundProfileText(int i) {

        String[] profiles = getResources().getStringArray(R.array.modes);
        String profile = profiles[i];
        data.get(currentPosition).setValue(profile);
        alarmMode = i;
        listAdapter.notifyDataSetChanged();


        return profile;
    }

    public String setDescriptionText(String description) {
        data.get(currentPosition).setValue(description);
        descriptionText = description;
        listAdapter.notifyDataSetChanged();

        return description;
    }

    public static DetailsActivity getInstance() {
        return (DetailsActivity) currentActivity;
    }

    private String timeToString(int hour, int minute) {
        String h;
        String m;
        String time;
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = Integer.toString(hour);
        }
        if (minute < 10) {
            m = "0" + minute;
        } else {
            m = Integer.toString(minute);
        }

        time = h + ":" + m;

        return time;
    }

    private String soundProfileToString(int i) {
        String[] profiles = getResources().getStringArray(R.array.modes);
        return profiles[i];
    }

    private String repeatTextToString(boolean[] days) {

        String d = "";

        for (int i = 0; i < days.length; i++) {
            if (days[i]) {

                String day = getResources().getStringArray(R.array.days)[i];

                if (i < days.length - 1) {
                    d = d + day.substring(0, 3) + ", ";
                } else {
                    d = d + day.substring(0, 3);
                }
            }
        }

        if (d.lastIndexOf(',') == d.length() - 2 && d.length() != 0) {

            d = d.substring(0, d.length() - 2);
        }

        return d;
    }

    private String breakLengthTextToString(int length) {
        String l = "";
        if (length < 60) {
            l = length + "m";
        }
        if (length == 60) {
            l = "1h";
        }
        if (length > 60) {
            l = length / 60 + "h" + " " + length % 60 + "m";
        }
        return l;
    }

    private String timeSummaryString() {
        return timeToString(startHour, startMinute) + " - " + timeToString(endHour, endMinute);
    }


    private boolean[] intToBooleanArray(int[] array) {
        boolean[] boolArray = {false, false, false, false, false, false, false};

        for (int i = 0; i < boolArray.length; i++) {
            if (array[i] == 1) {
                boolArray[i] = true;
            }
        }
        return boolArray;
    }

    private int booleanToInt(boolean b) {
        if (b) {
            return 1;
        }
        return 0;
    }

    private void getDbData() {

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        String selection = "_ID" + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = db.query(dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int startHourIndex = cursor.getColumnIndex(dataEntry.COLUMN_START_HOUR);
        int startMinuteIndex = cursor.getColumnIndex(dataEntry.COLUMN_START_MINUTE);
        int breakStartHourIndex = cursor.getColumnIndex(dataEntry.COLUMN_BREAK_START_HOUR);
        int breakStartMinuteIndex = cursor.getColumnIndex(dataEntry.COLUMN_BREAK_START_MINUTE);
        int endHourIndex = cursor.getColumnIndex(dataEntry.COLUMN_END_HOUR);
        int endMinuteIndex = cursor.getColumnIndex(dataEntry.COLUMN_END_MINUTE);
        int breakLengthIndex = cursor.getColumnIndex(dataEntry.COLUMN_BREAK_LENGTH);
        int alarmModeIndex = cursor.getColumnIndex(dataEntry.COLUMN_ALARM_MODE);
        int descriptionIndex = cursor.getColumnIndex(dataEntry.COLUMN_DESCRIPTION);
        int repeatMondayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_MONDAY);
        int repeatTuesdayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_TUESDAY);
        int repeatWednesdayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_WEDNESDAY);
        int repeatThursdayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_THURSDAY);
        int repeatFridayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_FRIDAY);
        int repeatSaturdayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_SATURDAY);
        int repeatSundayIndex = cursor.getColumnIndex(dataEntry.COLUMN_REPEAT_SUNDAY);
        int repeatTypeIndex = cursor.getColumnIndex(dataEntry.COLUMN_WEEKLY_REPEAT_TYPE);
        int repeatTimeIndex = cursor.getColumnIndex(dataEntry.COLUMN_WEEKLY_REPEAT_BEGINNING);



        startHour = cursor.getInt(startHourIndex);
        startMinute = cursor.getInt(startMinuteIndex);
        breakStartHour = cursor.getInt(breakStartHourIndex);
        breakStartMinute = cursor.getInt(breakStartMinuteIndex);
        endHour = cursor.getInt(endHourIndex);
        endMinute = cursor.getInt(endMinuteIndex);
        breakLength = cursor.getInt(breakLengthIndex);
        alarmMode = cursor.getInt(alarmModeIndex);
        descriptionText = cursor.getString(descriptionIndex);
        repeatType = cursor.getInt(repeatTypeIndex);
        repeatStart = cursor.getLong(repeatTimeIndex);


        int[] repeatArray = {cursor.getInt(repeatMondayIndex), cursor.getInt(repeatTuesdayIndex),
                cursor.getInt(repeatWednesdayIndex), cursor.getInt(repeatThursdayIndex),
                cursor.getInt(repeatFridayIndex), cursor.getInt(repeatSaturdayIndex),
                cursor.getInt(repeatSundayIndex)};
        repeat = intToBooleanArray(repeatArray);


        cursor.close();
        dbHelper.close();
    }

    private void putDbData() {

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(dataEntry.COLUMN_START_HOUR, startHour);
        cv.put(dataEntry.COLUMN_START_MINUTE, startMinute);
        cv.put(dataEntry.COLUMN_BREAK_START_HOUR, breakStartHour);
        cv.put(dataEntry.COLUMN_BREAK_START_MINUTE, breakStartMinute);
        cv.put(dataEntry.COLUMN_BREAK_LENGTH, breakLength);
        cv.put(dataEntry.COLUMN_END_HOUR, endHour);
        cv.put(dataEntry.COLUMN_END_MINUTE, endMinute);
        cv.put(dataEntry.COLUMN_ALARM_MODE, alarmMode);

        cv.put(dataEntry.COLUMN_REPEAT_MONDAY, booleanToInt(repeat[0]));
        cv.put(dataEntry.COLUMN_REPEAT_TUESDAY, booleanToInt(repeat[1]));
        cv.put(dataEntry.COLUMN_REPEAT_WEDNESDAY, booleanToInt(repeat[2]));
        cv.put(dataEntry.COLUMN_REPEAT_THURSDAY, booleanToInt(repeat[3]));
        cv.put(dataEntry.COLUMN_REPEAT_FRIDAY, booleanToInt(repeat[4]));
        cv.put(dataEntry.COLUMN_REPEAT_SATURDAY, booleanToInt(repeat[5]));
        cv.put(dataEntry.COLUMN_REPEAT_SUNDAY, booleanToInt(repeat[6]));

        cv.put(dataEntry.COLUMN_DESCRIPTION, descriptionText);
        cv.put(dataEntry.COLUMN_SUMMARY, timeSummaryString());
        cv.put(dataEntry.COLUMN_REPEAT_STRING, repeatTextToString(repeat));

        cv.put(dataEntry.COLUMN_WEEKLY_REPEAT_TYPE, repeatType);
        cv.put(dataEntry.COLUMN_WEEKLY_REPEAT_BEGINNING, repeatStart);
//        cv.put(dataEntry.COLUMN_ALARM_START_ID, 0);
//        cv.put(dataEntry.COLUMN_ALARM_END_ID, 0);

        cv.put(dataEntry.COLUMN_STATE, 1);

        //cv.put(dataEntry.COLUMN_STATE, 1);

        if (isNew()) {
            id = db.insertOrThrow(dataEntry.TABLE_NAME, null, cv);
        } else {
            String whereClause = "_ID=?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            db.update(dataEntry.TABLE_NAME, cv, whereClause, whereArgs);
        }

        dbHelper.close();
    }

//    private void deleteDbEntry() {
//        dbHelper = new ModeSwitcherDbHelper(this);
//        db = dbHelper.getWritableDatabase();
//        String whereClause = "_ID=?";
//        String[] whereArgs = new String[]{String.valueOf(id)};
//        db.delete(dataEntry.TABLE_NAME, whereClause, whereArgs);
//        dbHelper.close();
//    }


    // return true if entry id == -1 to check if it is a new entry
    private boolean isNew() {
        return id == -1;
    }


    // go to previous activity without saving
    private void goBack() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // save entry to db, and go back
    private void doneBack(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("status", "turn_on");
        intent.putExtra("rowId", (int)id);
        startActivity(intent);
    }

    // go back and delete row from db
    private void deleteBack(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("rowId", (int)id);
        intent.putExtra("status", "delete");
        startActivity(intent);
    }

}
