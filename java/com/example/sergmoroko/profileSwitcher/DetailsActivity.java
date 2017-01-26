package com.example.sergmoroko.profileSwitcher;

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
import java.util.Calendar;
import java.util.List;

import com.example.sergmoroko.profileSwitcher.ModeSwitcherDbContract.dataEntry;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<ListItem> data = new ArrayList<>();
    private long id;

    private SQLiteDatabase db;

    static int pickerHour = 0;
    static int pickerMinute = 0;
    static boolean timeSet;


    private static int currentPosition;

    private static Activity currentActivity;

    private MyListAdapter listAdapter;

    private int startHour = 0;
    private int startMinute = 0;
    private int breakStartHour = 0;
    private int breakStartMinute = 0;
    private int endHour = 0;
    private int endMinute = 0;
    private int breakLength = 0;
    private int alarmMode = 0;
    private int repeatType = 0;
    private long repeatStart = 0;

    private boolean[] repeat = {false, false, false, false, false, false, false};

    private String descriptionText;

    private ModeSwitcherDbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        id = this.getIntent().getIntExtra(Constants.INTENT_EXTRA_ID, 0);

        setContentView(R.layout.activity_details);
        ListView lv = (ListView) findViewById(R.id.listView_details);
        Button doneBtn = (Button) findViewById(R.id.details_button_done);
        Button deleteBtn = (Button) findViewById(R.id.details_button_delete);
        Button cancelBtn = (Button) findViewById(R.id.details_button_cancel);
        if (doneBtn != null) {
            doneBtn.setOnClickListener(this);
        }
        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(this);
        }
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(this);
        }
        // if data for current ID exists in db, get it
        if (!isNew()) {
            getDbData();
            // otherwise hide delete button
        } else {
            if (deleteBtn != null) {
                deleteBtn.setVisibility(View.GONE);
            }
        }

        if (data.isEmpty()) {
            generateListContent();
        }
        listAdapter = new MyListAdapter(this, android.R.layout.simple_list_item_2, data);

        if (lv != null) {
            lv.setAdapter(listAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    currentPosition = position;
                    switch (position) {
                        case Constants.START_TIME_POSITION:
                            showTimePickerDialog(view);
                            break;
                        case Constants.BREAK_TIME_POSITION:
                            showTimePickerDialog(view);
                            break;
                        case Constants.BREAK_LENGTH_POSITION:
                            ShowSeekDialog();
                            break;
                        case Constants.END_TIME_POSITION:
                            showTimePickerDialog(view);
                            break;
                        case Constants.REPEAT_POSITION:
                            showRecurrencePickerDialog();
                            break;
                        case Constants.SOUND_PROFILE_POSITION:
                            ShowModeDialog();
                            break;
                        case Constants.DESCRIPTION_POSITION:
                            ShowNameDialog();
                            break;
                    }
                }
            });
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBooleanArray(Constants.REPEAT_ARRAY_SAVE, repeat);

        outState.putInt(Constants.START_HOUR_SAVE, startHour);
        outState.putInt(Constants.START_MINUTE_SAVE, startMinute);
        outState.putInt(Constants.END_HOUR_SAVE, endHour);
        outState.putInt(Constants.END_MINUTE_SAVE, endMinute);
        outState.putInt(Constants.BREAK_LENGTH_SAVE, breakLength);
        outState.putInt(Constants.ALARM_MODE_SAVE, alarmMode);
        outState.putInt(Constants.BREAK_START_HOUR_SAVE, breakStartHour);
        outState.putInt(Constants.BREAK_START_MINUTE_SAVE, breakStartMinute);

        outState.putString(Constants.DESCRIPTION_SAVE, descriptionText);
        outState.putInt(Constants.REPEAT_TYPE_SAVE, repeatType);
        outState.putLong(Constants.REPEAT_START_SAVE, repeatStart);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        startHour = savedInstanceState.getInt(Constants.START_HOUR_SAVE);
        startMinute = savedInstanceState.getInt(Constants.START_MINUTE_SAVE);
        endHour = savedInstanceState.getInt(Constants.END_HOUR_SAVE);
        endMinute = savedInstanceState.getInt(Constants.END_MINUTE_SAVE);
        breakLength = savedInstanceState.getInt(Constants.BREAK_LENGTH_SAVE);
        alarmMode = savedInstanceState.getInt(Constants.ALARM_MODE_SAVE);
        breakStartHour = savedInstanceState.getInt(Constants.BREAK_START_HOUR_SAVE);
        breakStartMinute = savedInstanceState.getInt(Constants.BREAK_START_MINUTE_SAVE);

        repeat = savedInstanceState.getBooleanArray(Constants.REPEAT_ARRAY_SAVE);

        descriptionText = savedInstanceState.getString(Constants.DESCRIPTION_SAVE);
        repeatType = savedInstanceState.getInt(Constants.REPEAT_TYPE_SAVE);
        repeatStart = savedInstanceState.getLong(Constants.REPEAT_START_SAVE);

        generateListContent();

        listAdapter.notifyDataSetChanged();
    }

    private void generateListContent() {
        if (!data.isEmpty()) {
            data.clear();
        }

        data.add(new ListItem(getResources().getString(R.string.start_time_title), timeToString(startHour, startMinute)));
        data.add(new ListItem(getResources().getString(R.string.break_time_title), timeToString(breakStartHour, breakStartMinute)));
        data.add(new ListItem(getResources().getString(R.string.break_length_title), breakLengthTextToString(breakLength)));
        data.add(new ListItem(getResources().getString(R.string.end_time_title), timeToString(endHour, endMinute)));
        data.add(new ListItem(getResources().getString(R.string.repeat_title), repeatTextToString(repeat)));
        data.add(new ListItem(getResources().getString(R.string.sound_profile_title), soundProfileToString(alarmMode)));
        data.add(new ListItem(getResources().getString(R.string.description_title), (descriptionText)));
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
                break;
            case R.id.details_button_delete:
                showDeleteConfirmationDialog();
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

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
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

    /**
     * TIME PICKING DIALOG
     */

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = 0;
            int minute = 0;
            // dialog title
            String dialogTitle = "";
            switch (currentPosition) {
                case Constants.START_TIME_POSITION:
                    dialogTitle = getResources().getString(R.string.start_time_dialog_title);
                    break;
                case Constants.BREAK_TIME_POSITION:
                    dialogTitle = getResources().getString(R.string.break_time_dialog_title);
                    break;
                case Constants.END_TIME_POSITION:
                    dialogTitle = getResources().getString(R.string.end_time_dialog_title);
                    break;
            }

            if (getInstance().isNew()) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } else {
                switch (currentPosition) {
                    case Constants.START_TIME_POSITION:
                        hour = getInstance().startHour;
                        minute = getInstance().startMinute;
                        break;
                    case Constants.BREAK_TIME_POSITION:
                        hour = getInstance().breakStartHour;
                        minute = getInstance().breakStartMinute;
                        break;
                    case Constants.END_TIME_POSITION:
                        hour = getInstance().endHour;
                        minute = getInstance().endMinute;
                        break;
                }
            }


            // Create a new instance of TimePickerDialog and return it
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            dialog.setTitle(dialogTitle);
            return dialog;
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

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            final SeekBar seek = new SeekBar(getActivity());
            seek.setId(R.id.seek_bar);
            seek.setMax(Constants.BREAK_LENGTH_SEEK_BAR_MULTIPLIER);
            seek.setProgress(((DetailsActivity) getActivity()).breakLength / Constants.BREAK_LENGTH_SEEK_BAR_MULTIPLIER);

            popDialog.setTitle(R.string.break_length_dialog_title);

            popDialog.setView(seek);
            popDialog.setMessage(((DetailsActivity) getActivity()).breakLengthTextToString(((DetailsActivity) getActivity()).breakLength));

            // OK button
            popDialog.setPositiveButton(R.string.break_length_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    ((DetailsActivity) getActivity()).setBreakLengthText(seek.getProgress() * Constants.BREAK_LENGTH_SEEK_BAR_MULTIPLIER);
                    dialog.dismiss();
                }

            });
            // CANCEL button
            popDialog.setNeutralButton(R.string.break_length_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            final AlertDialog dialog = popDialog.create();

            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                    progress = progressV * Constants.BREAK_LENGTH_SEEK_BAR_MULTIPLIER;
                    dialog.setMessage(((DetailsActivity) getActivity()).breakLengthTextToString(seek.getProgress() * Constants.BREAK_LENGTH_SEEK_BAR_MULTIPLIER));
                }

                public void onStartTrackingTouch(SeekBar arg0) {

                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    dialog.setMessage(((DetailsActivity) getActivity()).breakLengthTextToString(progress));
                }
            });

            return dialog;
        }
    }

    /**
     * DELETE CONFIRMATION DIALOG
     */

    public static class DeleteConfirmationDialog extends DialogFragment {
        AlertDialog.Builder confirmationDialog;
        TextView text;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            confirmationDialog = new AlertDialog.Builder(getActivity());
            text = new TextView(getActivity());
            confirmationDialog.setTitle(R.string.delete_confirmation_dialog_title);
            confirmationDialog.setPositiveButton(R.string.delete_confirmation_dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //delete row
                    getInstance().deleteBack();
                }
            });
            confirmationDialog.setNegativeButton(R.string.delete_confirmation_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // cancel
                    dialog.dismiss();
                }
            });

            return confirmationDialog.create();
        }
    }

    private void showDeleteConfirmationDialog() {

        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog();
        dialog.show(getSupportFragmentManager(), "deleteConfirmationDialog");
    }

    // show mode name dialog
    private void ShowNameDialog() {
        NameDialog nameDialog = new NameDialog();
        nameDialog.show(getSupportFragmentManager(), "nameDialog");
    }

    /**
     * DESCRIPTION INPUT DIALOG
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

            popDialog.setTitle(R.string.description_dialog_title);
            if (((DetailsActivity) getActivity()).descriptionText != null) {
                input.setText(((DetailsActivity) getActivity()).descriptionText);
            }

            popDialog.setView(input);

            // OK button
            popDialog.setPositiveButton(R.string.description_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((DetailsActivity) getActivity()).setDescriptionText(input.getText().toString());
                    dialog.dismiss();
                }

            });

            // CANCEL button
            popDialog.setNeutralButton(R.string.description_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            return popDialog.create();
        }
    }

    // show sound profile choose dialog
    public void ShowModeDialog() {
        RingerModeDialog modeDialog = new RingerModeDialog();
        modeDialog.show(getSupportFragmentManager(), "modeDialog");
    }

    /**
     * SOUND PROFILE CHOOSE DIALOG
     **/
    public static class RingerModeDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
            final Spinner spinner = new Spinner(getActivity());
            spinner.setId(R.id.spinner);

            popDialog.setTitle(R.string.ringer_mode_dialog_title);

            ArrayAdapter<?> adapter =
                    ArrayAdapter.createFromResource(getActivity(), R.array.modes, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);
            spinner.setSelection(((DetailsActivity) getActivity()).alarmMode);

            popDialog.setView(spinner);

            // OK button
            popDialog.setPositiveButton(R.string.ringer_mode_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ((DetailsActivity) getActivity()).setSoundProfileText(spinner.getSelectedItemPosition());
                    dialog.dismiss();
                }

            });

            // CANCEL button
            popDialog.setNeutralButton(R.string.ringer_mode_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            return popDialog.create();
        }
    }

    // show repeat dialog
    public void showRecurrencePickerDialog() {
        RecurrencePickerDialog recurrencePickerDialog = new RecurrencePickerDialog();
        recurrencePickerDialog.show(getSupportFragmentManager(), "rec");
    }

    /**
     * RECURRENCE PICKER DIALOG
     **/
    public static class RecurrencePickerDialog extends DialogFragment {
        ArrayList<Integer> mSelectedItems;
        ArrayList<Integer> workdays = new ArrayList<>();
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
                repeat = savedInstanceState.getBooleanArray(Constants.REPEAT_TEMPORARY_ARRAY);
            }

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.recurrence_picker, null);

            // radioButtons
            radioGroup1 = (RadioGroup) v.findViewById(R.id.recurrence_picker_radio_group);
            rbWorkDay = (RadioButton) v.findViewById(R.id.rb_wd);
            rbEveryDay = (RadioButton) v.findViewById(R.id.rb_ed);
            rbNotSelected = (RadioButton) v.findViewById(R.id.rb_ns);


            int repeatPos = ((DetailsActivity) getActivity()).repeatType;
            long thisMonday = getThisMonday();

            long preSavedTime = ((DetailsActivity) getActivity()).repeatStart;

            int weeksElapsed = (int) ((thisMonday - preSavedTime) / Constants.WEEK_LENGTH_IN_MILLIS);


            final Spinner repeatSpinner = (Spinner) v.findViewById(R.id.repeat_spinner);
            ArrayAdapter<?> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.repeat,
                    android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            repeatSpinner.setAdapter(spinnerAdapter);

            // if saved
            if (repeatPos == Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS ||
                    repeatPos == Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_NEXT) {

                repeatPos = (weeksElapsed % 2 == 0) ? Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS :
                        Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS;
            }
            repeatSpinner.setSelection(repeatPos);

            // Set the dialog title
            builder.setTitle(R.string.recurrence_dialog_title)
                    .setView(v)
                    .setMultiChoiceItems(R.array.days, repeat, null)

                    // Set the action buttons
                    .setPositiveButton(R.string.recurrence_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            listCheck(listView);
                            ((DetailsActivity) getActivity()).data.get(currentPosition)
                                    .setValue(((DetailsActivity) getActivity()).repeatTextToString(((DetailsActivity) getActivity()).repeat));

                            int spinnerPos = repeatSpinner.getSelectedItemPosition();

                            // saving a beginning of the week time
                            switch (spinnerPos) {
                                case Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS:
                                    // save time of current week beginning
                                    setRepeatStartTime(true);

                                    break;
                                case Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_NEXT:
                                    // save time of next week beginning
                                    setRepeatStartTime(false);
                                    break;

                            }
                            ((DetailsActivity) getActivity()).repeatType = spinnerPos;

                            ((DetailsActivity) getActivity()).listAdapter.notifyDataSetChanged();

                        }
                    })

                    .setNegativeButton(R.string.recurrence_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });


            AlertDialog dialog = builder.create();

            listView = dialog.getListView();

            rbWorkDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < Constants.WORKING_DAYS_IN_WEEK_QTY; i++) {
                        if (!listView.isItemChecked(i)) {
                            listView.performItemClick(listView, i, 0);
                        }
                    }
                    for (int y = Constants.WORKING_DAYS_IN_WEEK_QTY; y < Constants.DAYS_IN_WEEK_QTY; y++) {
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
                    for (int i = 0; i < Constants.DAYS_IN_WEEK_QTY; i++) {
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
                    for (int i = 0; i < Constants.DAYS_IN_WEEK_QTY; i++) {
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
            outState.putBooleanArray(Constants.REPEAT_TEMPORARY_ARRAY, repeat);
            super.onSaveInstanceState(outState);
        }

        // writes checked days to array
        private void listCheck(ListView listView) {
            for (int i = 0; i < listView.getCount(); i++) {
                ((DetailsActivity) getActivity()).repeat[i] = listView.isItemChecked(i);
            }
        }

        private void setRepeatStartTime(boolean thisWeek) {
            if (thisWeek) {
                ((DetailsActivity) getActivity()).repeatStart = getThisMonday();
            } else {
                ((DetailsActivity) getActivity()).repeatStart = getThisMonday() + Constants.WEEK_LENGTH_IN_MILLIS;
            }
        }

        // method returns long containing a moment of current week beginning
        private long getThisMonday() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }

        // check radioButtons accordingly to current checkBoxes state
        private void checkRadioButtons() {
            boolean noneOfThis = true;
            if (mSelectedItems.isEmpty()) {
                rbNotSelected.setChecked(true);
                noneOfThis = false;
            }
            if (mSelectedItems.size() == Constants.DAYS_IN_WEEK_QTY) {
                rbEveryDay.setChecked(true);
                noneOfThis = false;
            }
            if (mSelectedItems.containsAll(workdays)
                    && mSelectedItems.size() == Constants.WORKING_DAYS_IN_WEEK_QTY) {
                rbWorkDay.setChecked(true);
                noneOfThis = false;
            }
            if (noneOfThis) {
                radioGroup1.clearCheck();
            }
        }
    }

    private void setTimeText(int hour, int minute) {

        String time = timeToString(hour, minute);

        data.get(currentPosition).setValue(time);
        switch (currentPosition) {
            case Constants.START_TIME_POSITION:
                startHour = hour;
                startMinute = minute;
                break;
            case Constants.BREAK_TIME_POSITION:
                breakStartHour = hour;
                breakStartMinute = minute;
                break;
            case Constants.END_TIME_POSITION:
                endHour = hour;
                endMinute = minute;
                break;
        }
        listAdapter.notifyDataSetChanged();
    }

    private void setBreakLengthText(int length) {
        String breakText = breakLengthTextToString(length);
        data.get(currentPosition).setValue(breakText);
        breakLength = length;
        listAdapter.notifyDataSetChanged();
    }


    private void setSoundProfileText(int i) {
        String[] profiles = getResources().getStringArray(R.array.modes);
        String profile = profiles[i];
        data.get(currentPosition).setValue(profile);
        alarmMode = i;
        listAdapter.notifyDataSetChanged();
    }

    private void setDescriptionText(String description) {
        data.get(currentPosition).setValue(description);
        descriptionText = description;
        listAdapter.notifyDataSetChanged();
    }

    public static DetailsActivity getInstance() {
        return (DetailsActivity) currentActivity;
    }

    private String timeToString(int hour, int minute) {
        // adding "0" to hours and minutes if their values are less than 10
        String h = (hour < 10) ? "0" + hour : Integer.toString(hour);
        String m = (minute < 10) ? "0" + minute : Integer.toString(minute);
        return h + getResources().getString(R.string.time_delimiter) + m;
    }

    private String soundProfileToString(int i) {
        String[] profiles = getResources().getStringArray(R.array.modes);
        return profiles[i];
    }

    private String repeatTextToString(boolean[] days) {

        String resultString = "";

        for (int i = 0; i < days.length; i++) {
            String day = getResources().getStringArray(R.array.days_short)[i];
            resultString = (days[i]) ? resultString + day + ", " : resultString;
        }
        // if resulting string is not empty
        if (resultString.length() != 0) {
            // remove the last comma and whitespace
            resultString = resultString.substring(0, resultString.length() - 2);
        }

        return resultString;
    }


    private String breakLengthTextToString(int length) {
        String l = "";
        if (length < Constants.HOUR_LENGTH_IN_MINUTES) {
            l = length + getResources().getString(R.string.minute);
        }
        if (length == Constants.HOUR_LENGTH_IN_MINUTES) {
            l = getResources().getString(R.string.one_hour);
        }
        if (length > Constants.HOUR_LENGTH_IN_MINUTES) {
            l = length / Constants.HOUR_LENGTH_IN_MINUTES + getResources().getString(R.string.hour) +
                    getResources().getString(R.string.break_length_delimiter) +
                    length % Constants.HOUR_LENGTH_IN_MINUTES + getResources().getString(R.string.minute);
        }
        return l;

    }

    private String timeSummaryString() {
        return timeToString(startHour, startMinute) +
                getResources().getString(R.string.summary_time_delimiter) + timeToString(endHour, endMinute);
    }

    private boolean[] intToBooleanArray(int[] array) {
        boolean[] boolArray = {false, false, false, false, false, false, false};

        for (int i = 0; i < boolArray.length; i++) {
            boolArray[i] = (array[i] == 1);
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

        String selection = Constants.WHERE_CLAUSE_ROW_ID;
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
        db.close();
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

        cv.put(dataEntry.COLUMN_REPEAT_MONDAY, booleanToInt(repeat[Constants.MONDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_TUESDAY, booleanToInt(repeat[Constants.TUESDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_WEDNESDAY, booleanToInt(repeat[Constants.WEDNESDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_THURSDAY, booleanToInt(repeat[Constants.THURSDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_FRIDAY, booleanToInt(repeat[Constants.FRIDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_SATURDAY, booleanToInt(repeat[Constants.SATURDAY]));
        cv.put(dataEntry.COLUMN_REPEAT_SUNDAY, booleanToInt(repeat[Constants.SUNDAY]));

        cv.put(dataEntry.COLUMN_DESCRIPTION, descriptionText);
        cv.put(dataEntry.COLUMN_SUMMARY, timeSummaryString());
        cv.put(dataEntry.COLUMN_REPEAT_STRING, repeatTextToString(repeat));

        cv.put(dataEntry.COLUMN_WEEKLY_REPEAT_TYPE, repeatType);
        cv.put(dataEntry.COLUMN_WEEKLY_REPEAT_BEGINNING, repeatStart);

        // COLUMN_STATE = 1 means that alarms for that entry are active
        cv.put(dataEntry.COLUMN_STATE, 1);

        // insert new row if it's a new entry
        if (isNew()) {
            id = db.insertOrThrow(dataEntry.TABLE_NAME, null, cv);
            // or update currently existing
        } else {
            String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
            String[] whereArgs = new String[]{String.valueOf(id)};
            db.update(dataEntry.TABLE_NAME, cv, whereClause, whereArgs);
        }
        db.close();
        dbHelper.close();
    }

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
    private void doneBack() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.INTENT_EXTRA_STATUS, Constants.INTENT_EXTRA_TURN_ON);
        intent.putExtra(Constants.INTENT_EXTRA_ROW_ID, (int) id);
        startActivity(intent);
    }

    // go back and delete row from db
    private void deleteBack() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.INTENT_EXTRA_ROW_ID, (int) id);
        intent.putExtra(Constants.INTENT_EXTRA_STATUS, Constants.INTENT_EXTRA_DELETE);
        startActivity(intent);
    }

}
