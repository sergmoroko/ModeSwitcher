package com.example.sergmoroko.profileSwitcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;


public class AlarmReceiver extends WakefulBroadcastReceiver {

    private ModeSwitcherDbHelper dbHelper;
    private SQLiteDatabase db;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private int startHour = 0;
    private int startMinute = 0;
    private int breakStartHour = 0;
    private int breakStartMinute = 0;
    private int endHour = 0;
    private int endMinute = 0;
    private int breakLength = 0;
    private int alarmMode = 0;
    private boolean[] repeat = {false, false, false, false, false, false, false};
    private int alarmStartId = 0;
    private int alarmEndId = 0;
    private int repeatType = 0;
    private int alarmsQty = 0;


    @Override
    public void onReceive(final Context context, Intent intent) {

        String mode = intent.getStringExtra(Constants.INTENT_EXTRA_MODE);
        int id = intent.getIntExtra(Constants.INTENT_EXTRA_ID, 0);
        int repeatType = intent.getIntExtra(Constants.INTENT_EXTRA_REPEAT_TYPE, 0);

        Intent service = new Intent(context, AlarmService.class);

        service.putExtra(Constants.INTENT_EXTRA_MODE, mode);
        service.putExtra(Constants.INTENT_EXTRA_ID, id);
        service.putExtra(Constants.INTENT_EXTRA_REPEAT_TYPE, repeatType);

        startWakefulService(context, service);

    }

    private void getDbData(Context context, int rowId) {

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        String selection = Constants.WHERE_CLAUSE_ROW_ID;
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int startHourIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_START_HOUR);
        int startMinuteIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_START_MINUTE);
        int breakStartHourIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_BREAK_START_HOUR);
        int breakStartMinuteIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_BREAK_START_MINUTE);
        int endHourIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_END_HOUR);
        int endMinuteIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_END_MINUTE);
        int breakLengthIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_BREAK_LENGTH);
        int alarmModeIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_MODE);
        int repeatMondayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_MONDAY);
        int repeatTuesdayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_TUESDAY);
        int repeatWednesdayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_WEDNESDAY);
        int repeatThursdayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_THURSDAY);
        int repeatFridayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_FRIDAY);
        int repeatSaturdayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_SATURDAY);
        int repeatSundayIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_REPEAT_SUNDAY);
        int repeatTypeIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_WEEKLY_REPEAT_TYPE);


        startHour = cursor.getInt(startHourIndex);
        startMinute = cursor.getInt(startMinuteIndex);
        breakStartHour = cursor.getInt(breakStartHourIndex);
        breakStartMinute = cursor.getInt(breakStartMinuteIndex);
        endHour = cursor.getInt(endHourIndex);
        endMinute = cursor.getInt(endMinuteIndex);
        breakLength = cursor.getInt(breakLengthIndex);
        alarmMode = cursor.getInt(alarmModeIndex);
        repeatType = cursor.getInt(repeatTypeIndex);

        int[] repeatArray = {cursor.getInt(repeatMondayIndex), cursor.getInt(repeatTuesdayIndex),
                cursor.getInt(repeatWednesdayIndex), cursor.getInt(repeatThursdayIndex),
                cursor.getInt(repeatFridayIndex), cursor.getInt(repeatSaturdayIndex),
                cursor.getInt(repeatSundayIndex)};
        repeat = intToBooleanArray(repeatArray);


        cursor.close();
        db.close();
        dbHelper.close();
    }

    private void getAlarmsIds(Context context, int rowId) {
        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        String selection = Constants.WHERE_CLAUSE_ROW_ID;
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int alarmStartIdIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_START_ID);
        int alarmEndIdIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_END_ID);

        alarmStartId = cursor.getInt(alarmStartIdIndex);
        alarmEndId = cursor.getInt(alarmEndIdIndex);

        cursor.close();
        db.close();
        dbHelper.close();

    }

    private void setAlarmsIds(Context context, int rowId) {

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_START_ID, alarmStartId);
        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_END_ID, alarmEndId);
        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARMS_LEFT_QTY, alarmsQty);
        String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);
        db.close();
        dbHelper.close();

    }

    public void setBulkAlarms(Context context) {
        ArrayList<Integer> ids = getIds(context);
        for (int rowId : ids) {
            setAlarm(context, rowId);
        }
        enableBootReceiver(context);
    }

    public void setSingleAlarm(Context context, int rowId) {
        setAlarm(context, rowId);
        enableBootReceiver(context);
    }


    private void enableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void setAlarm(Context context, int rowId) {

        getDbData(context, rowId);

        getAlarmsIds(context, rowId);

        if (alarmEndId != 0) {
            cancelAlarms(context, rowId);
        }

        alarmStartId = rowId * 100;
        alarmEndId = alarmStartId;

        for (int i = 0; i < repeat.length; i++) {
            if (repeat[i]) {
                // i+2 because monday in calendar class == 2
                setDailyAlarms(context, i + 2, repeatType, rowId);
            }
        }

        setAlarmsIds(context, rowId);

    }

    private ArrayList<Integer> getIds(Context context) {
        ArrayList<Integer> ids = new ArrayList<>();

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();


        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, null, null, null, null, null);
        // if db is not empty
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                int IDIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry._ID);
                int enabledID = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_STATE);

                do {
                    if (cursor.getInt(enabledID) == 1) {
                        ids.add(cursor.getInt(IDIndex));
                    }
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        db.close();
        dbHelper.close();
        return ids;
    }

    // converts int array from db to boolean
    // 1 = true, 0 = false
    private boolean[] intToBooleanArray(int[] array) {
        boolean[] boolArray = {false, false, false, false, false, false, false};

        for (int i = 0; i < boolArray.length; i++) {
            if (array[i] == 1) {
                boolArray[i] = true;
            }
        }
        return boolArray;
    }


    private void setDailyAlarms(Context context, int dayNumber, int repeatType, int rowId) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean preDelay = sharedPref.getBoolean(Constants.SETTINGS_PRE_DELAY, false);
        boolean postDelay = sharedPref.getBoolean(Constants.SETTINGS_POST_DELAY, false);


        // 5m delay, if it's enabled in settings
        long preDelayValue = preDelay ? Constants.DELAY_IN_MILLIS : 0;
        long postDelayValue = postDelay ? Constants.DELAY_IN_MILLIS : 0;

        String normalMode = Constants.SOUND_PROFILE_NORMAL;
        String chosenMode = "";

        switch (alarmMode) {
            case Constants.SOUND_PROFILE_NORMAL_ID:
                chosenMode = Constants.SOUND_PROFILE_NORMAL;
                break;
            case Constants.SOUND_PROFILE_SILENT_ID:
                chosenMode = Constants.SOUND_PROFILE_SILENT;
                break;
            case Constants.SOUND_PROFILE_VIBRATE_ID:
                chosenMode = Constants.SOUND_PROFILE_VIBRATE;
                break;
        }

        // in case some of alarms time are set after 00:00
        // by increasing hour value instead of day we can avoid problems with weeks end
        breakStartHour = (breakStartHour < startHour) ? breakStartHour + Constants.DAY_LENGTH_IN_HOURS : breakStartHour;
        int breakEndHour = breakStartHour + (breakLength / Constants.HOUR_LENGTH_IN_MINUTES);
        endHour = (endHour < startHour) ? endHour + Constants.DAY_LENGTH_IN_HOURS : endHour;

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        // start of work time
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);

        Intent myIntent = new Intent(context, AlarmReceiver.class);
        setIntentExtras(myIntent, repeatType, rowId, chosenMode);

        pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() - preDelayValue, pendingIntent, repeatType);

        alarmEndId++;

        // sets break start and end only if its length not equal to 0
        if (breakLength > preDelayValue + postDelayValue) {

            // start of the break
            calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
            calendar.set(Calendar.HOUR_OF_DAY, breakStartHour);
            calendar.set(Calendar.MINUTE, breakStartMinute);

            myIntent = new Intent(context, AlarmReceiver.class);
            setIntentExtras(myIntent, repeatType, rowId, normalMode);

            pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() + postDelayValue, pendingIntent, repeatType);

            alarmEndId++;

            // end of the break
            calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
            calendar.set(Calendar.HOUR_OF_DAY, breakEndHour);
            calendar.set(Calendar.MINUTE, breakStartMinute + (breakLength % Constants.HOUR_LENGTH_IN_MINUTES));

            myIntent = new Intent(context, AlarmReceiver.class);
            setIntentExtras(myIntent, repeatType, rowId, chosenMode);


            pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() - preDelayValue, pendingIntent, repeatType);

            alarmEndId++;
        }

        // end of the work day
        calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMinute);

        myIntent = new Intent(context, AlarmReceiver.class);
        setIntentExtras(myIntent, repeatType, rowId, normalMode);


        pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() + postDelayValue, pendingIntent, repeatType);

        alarmEndId++;
    }

    // sets alarms to alarm manager
    private void set(AlarmManager alarmManager, int type, long triggerAt, PendingIntent pendingIntent, int repeatType) {

        long weekLength = Constants.WEEK_LENGTH_IN_MILLIS;

        switch (repeatType) {
            case Constants.REPEAT_TYPE_NO_REPEATS:
                alarmManager.set(type, triggerAt, pendingIntent);
                alarmsQty++;
                break;
            case Constants.REPEAT_TYPE_EVERY_WEEK:
                alarmManager.setRepeating(type, triggerAt, weekLength, pendingIntent);
                break;
            case Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS:
                alarmManager.setRepeating(type, triggerAt, weekLength * 2, pendingIntent);
                break;
            case Constants.REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_NEXT:
                alarmManager.setRepeating(type, triggerAt + weekLength, weekLength * 2, pendingIntent);
                break;
        }

    }

    // cancels alarms for specified row id
    public void cancelAlarms(Context context, int rowId) {
        getAlarmsIds(context, rowId);
        Intent myIntent;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int id = alarmStartId; id < alarmEndId; id++) {
            myIntent = new Intent(context, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
            alarmManager.cancel(pendingIntent);
        }

        alarmEndId = 0;

        setAlarmsIds(context, rowId);
        disableAlarm(context, rowId);
    }

    // changes status of alarm in database
    private void disableAlarm(Context context, int rowId) {
        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_STATE, 0);

        String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);
        db.close();
        dbHelper.close();
    }

    // puts provided values to intent extras
    private void setIntentExtras(Intent intent, int repeatType, int rowId, String mode) {
        intent.putExtra(Constants.INTENT_EXTRA_MODE, mode);
        intent.putExtra(Constants.INTENT_EXTRA_ID, rowId);
        intent.putExtra(Constants.INTENT_EXTRA_REPEAT_TYPE, repeatType);
    }
}
