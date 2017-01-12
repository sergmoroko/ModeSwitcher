package com.example.sergmoroko.modeswitcher;

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
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ssss on 28.11.2016.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private ModeSwitcherDbHelper dbHelper;
    private SQLiteDatabase db;

    AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    int startHour = 0;
    int startMinute = 0;
    int breakStartHour = 0;
    int breakStartMinute = 0;
    int endHour = 0;
    int endMinute = 0;
    int breakLength = 0;
    int alarmMode = 0;
    boolean[] repeat = {false, false, false, false, false, false, false};
    int alarmStartId = 0;
    int alarmEndId = 0;
    int repeatType =0;
    private long currentLast;
    private int alarmsQty =0;


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("test1" , "alarmReceiver onReceive called");


        String mode = intent.getStringExtra("mode");
        int id = intent.getIntExtra("_id", 0);
        int repeatType = intent.getIntExtra("repeatType", 0);

        System.out.println("test1 INTENT TEST IN ONRECEIVE " + mode);

        Intent service = new Intent(context, AlarmService.class);

        service.putExtra("mode", mode);
        service.putExtra("_id", id);
        service.putExtra("repeatType", repeatType);

        startWakefulService(context, service);

    }

    private void getDbData(Context context, int rowId){

        Log.d("test1" , "getDBdata called");

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        String selection = "_ID" + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();


        //Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, null, null, null, null, null);

        //cursor.moveToFirst();

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
        dbHelper.close();
    }

    private void getAlarmsIds(Context context, int rowId){
        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        String selection = "_ID" + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int alarmStartIdIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_START_ID);
        int alarmEndIdIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_END_ID);

        alarmStartId = cursor.getInt(alarmStartIdIndex);
        alarmEndId = cursor.getInt(alarmEndIdIndex);

        cursor.close();
        dbHelper.close();

    }

    private void setAlarmsIds(Context context, int rowId) {

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_START_ID, alarmStartId);
        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARM_END_ID, alarmEndId);
        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALRMS_LEFT_QTY, alarmsQty);
        String whereClause = "_ID=?";
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        dbHelper.close();

    }

    public void setBulkAlarms(Context context){
        ArrayList<Integer> ids = getIds(context);
        for(int rowId: ids){
            setAlarm(context, rowId);
        }

        enableBootReceiver(context);
    }

    public void setSingleAlarm(Context context, int rowId){
        setAlarm(context, rowId);
        enableBootReceiver(context);
    }


    private void enableBootReceiver(Context context){
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }



    private void setAlarm(Context context, int rowId){

        Log.d("test1" , "setAlarms called");

        getDbData(context, rowId);

        getAlarmsIds(context, rowId);

        if(alarmEndId != 0){
            cancelAlarms(context, rowId);
        }

        alarmStartId = rowId * 100;
        alarmEndId = alarmStartId;

//        int id =0;
//        String mode = "normal";
//        String mode1 ="";
//
//        switch (alarmMode){
//            case 0:
//                mode1 = "normal";
//                break;
//            case 1:
//                mode1 = "silent";
//                break;
//            case 2:
//                mode1 = "vibrate";
//                break;
//        }
//
//
//
//        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        //for(int i = 1; i <= repeat.length; i++){
//            Calendar calendar = Calendar.getInstance();
//            //calendar.set(Calendar.DAY_OF_WEEK, i);
//            calendar.set(Calendar.HOUR_OF_DAY, startHour);
//            calendar.set(Calendar.MINUTE, startMinute);
//
//            Intent myIntent = new Intent(context, AlarmReceiver.class);
//            myIntent.putExtra("mode", mode1);
//
//
//            pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
//
//            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
//
//            id++;
//
//            //calendar.set(Calendar.DAY_OF_WEEK, i);
//            calendar.set(Calendar.HOUR_OF_DAY, breakStartHour);
//            calendar.set(Calendar.MINUTE, breakStartMinute);
//
//            myIntent = new Intent(context, AlarmReceiver.class);
//            myIntent.putExtra("mode", mode);
//
//
//            pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
//            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
//
//            id++;
//
//            //calendar.set(Calendar.DAY_OF_WEEK, i);
//            calendar.set(Calendar.HOUR_OF_DAY, breakStartHour + (breakLength / 60));
//            calendar.set(Calendar.MINUTE, breakStartMinute + (breakLength % 60));
//
//            myIntent = new Intent(context, AlarmReceiver.class);
//            myIntent.putExtra("mode", mode1);
//
//
//            pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
//            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
//
//            id++;
//
//            //calendar.set(Calendar.DAY_OF_WEEK, i);
//            calendar.set(Calendar.HOUR_OF_DAY, endHour);
//            calendar.set(Calendar.MINUTE, endMinute);
//
//            myIntent = new Intent(context, AlarmReceiver.class);
//            myIntent.putExtra("mode", mode);
//
//
//            pendingIntent = PendingIntent.getBroadcast(context, id, myIntent, 0);
//            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        for(int i = 0; i < repeat.length; i++){
            if(repeat[i]){
                // i+2 because monday in calendar class == 2
                setDailyAlarms(context, i+2, repeatType, rowId);
            }
        }

        setAlarmsIds(context, rowId);

        }

    private ArrayList<Integer> getIds(Context context) {
        ArrayList<Integer> ids = new ArrayList<>();

        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();


        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() != 0) {

            if (cursor.moveToFirst()) {
                int IDIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry._ID);
                int enabledID = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_STATE);

                do {
                    int sw = cursor.getInt(enabledID);
                    if (sw == 1) {
                        ids.add(cursor.getInt(IDIndex));
                    }
                }
                while (cursor.moveToNext());
            }
        }

        cursor.close();
        dbHelper.close();
        return ids;
    }

   // }

    private boolean[] intToBooleanArray(int[] array){
        boolean[] boolArray = {false, false, false, false, false, false, false};

        for (int i = 0; i < boolArray.length; i++){
            if(array[i] == 1){
                boolArray[i] = true;
            }
        }
        return boolArray;
    }


    private void setDailyAlarms(Context context, int dayNumber, int repeatType, int rowId){

        //int id = alarmStartId;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean preDelay = sharedPref.getBoolean("pre_delay_enabled", false);
        boolean postDelay = sharedPref.getBoolean("post_delay_enabled", false);


        // 5m delay, if it's enabled in settings
        long preDelayValue = preDelay ? 1000 * 60 * 5 : 0;
        long postDelayValue = postDelay ? 1000 * 60 * 5 : 0;


        String mode = "normal";
        String mode1 = "";


        switch (alarmMode){
            case 0:
                mode1 = "normal";
                break;
            case 1:
                mode1 = "silent";
                break;
            case 2:
                mode1 = "vibrate";
                break;
        }


//        int breakStartDayNumber = (breakStartHour < startHour) ? dayNumber + 1 : dayNumber;
//        int breakEndDayNumber = (breakStartHour + (breakLength / 60) < startHour) ? dayNumber + 1 : dayNumber;
//        int endDayNumber = (endHour < startHour) ? dayNumber + 1 : dayNumber;


        // in case some of alarms time are set after 00:00
        // by increasing hour value instead of day we can avoid problems with weeks end
        breakStartHour = (breakStartHour < startHour) ? breakStartHour + 24 : breakStartHour;
        int breakEndHour = breakStartHour + (breakLength / 60);
        endHour = (endHour < startHour) ? endHour + 24 : endHour;

//        if (breakStartHour < startHour) {
//            breakStartDayNumber = dayNumber + 1;
//        }
//        if (breakStartHour + (breakLength / 60) < startHour) {
//            breakEndDayNumber = dayNumber + 1;
//        }
//        if (endHour < startHour) {
//            endDayNumber = dayNumber + 1;
//        }

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        // start of work time
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);

        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("mode", mode1);
        myIntent.putExtra("_id", rowId);
        myIntent.putExtra("repeatType", repeatType);

        pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() - preDelayValue, pendingIntent, repeatType);

        alarmEndId++;

        // sets break start and end only if its length not equal to 0
        if(breakLength > preDelayValue + postDelayValue) {

            // start of the break
            calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
            calendar.set(Calendar.HOUR_OF_DAY, breakStartHour);
            calendar.set(Calendar.MINUTE, breakStartMinute);

            myIntent = new Intent(context, AlarmReceiver.class);
            myIntent.putExtra("mode", mode);
            myIntent.putExtra("_id", rowId);
            myIntent.putExtra("repeatType", repeatType);




            pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
            set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() + postDelayValue, pendingIntent, repeatType);

            alarmEndId++;

            // end of the break
            calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
            calendar.set(Calendar.HOUR_OF_DAY, breakEndHour);
            calendar.set(Calendar.MINUTE, breakStartMinute + (breakLength % 60));

            myIntent = new Intent(context, AlarmReceiver.class);
            myIntent.putExtra("mode", mode1);
            myIntent.putExtra("_id", rowId);
            myIntent.putExtra("repeatType", repeatType);


            pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
            set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() - preDelayValue, pendingIntent, repeatType);

            alarmEndId++;
        }

        // end of the work day
        calendar.set(Calendar.DAY_OF_WEEK, dayNumber);
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMinute);

        myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("mode", mode);
        myIntent.putExtra("_id", rowId);
        myIntent.putExtra("repeatType", repeatType);


        pendingIntent = PendingIntent.getBroadcast(context, alarmEndId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        set(alarmManager, AlarmManager.RTC, calendar.getTimeInMillis() + postDelayValue, pendingIntent, repeatType);

        alarmEndId++;
    }

    private void set(AlarmManager alarmManager, int type, long triggerAt, PendingIntent pendingIntent, int repeatType) {

        long weekLength = 1000 * 60 * 60 * 24 * 7;

        switch (repeatType) {
            case 0:
                alarmManager.set(type, triggerAt, pendingIntent);
                //currentLast = triggerAt;
                alarmsQty++;

                break;
            case 1:
                alarmManager.setRepeating(type, triggerAt, weekLength, pendingIntent);
                break;
            case 2:
                alarmManager.setRepeating(type, triggerAt, weekLength * 2, pendingIntent);
                break;
            case 3:
                alarmManager.setRepeating(type, triggerAt + weekLength, weekLength * 2, pendingIntent);
                break;
        }

        long val = triggerAt;

        Date date=new Date(val);
        SimpleDateFormat df2 = new SimpleDateFormat("hh:mm/dd/MM/yy");
        String dateText = df2.format(date);
        System.out.println("DATE TEST: " + dateText + " ID = " + alarmEndId);

    }

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

    private void disableAlarm(Context context, int rowId){
        dbHelper = new ModeSwitcherDbHelper(context);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_STATE, 0);

        String whereClause = "_ID=?";
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        dbHelper.close();
    }
}
