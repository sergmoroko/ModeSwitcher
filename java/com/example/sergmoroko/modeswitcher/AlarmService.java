package com.example.sergmoroko.modeswitcher;

/**
 * Created by ssss on 28.11.2016.
 */

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;

import android.util.Log;

public class AlarmService extends IntentService {

    private ModeSwitcherDbHelper dbHelper;
    private SQLiteDatabase db;
    AlarmReceiver alarm = new AlarmReceiver();


    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d("test1" , "alarmService onHandleIntent called");

        final AudioManager mobilemode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        String mode = intent.getStringExtra("mode");
        int id = intent.getIntExtra("_id", 0);
        int repeatType = intent.getIntExtra("repeatType", 0);

        System.out.println("test1 INTENT TEST IN ONHANDLEINTENT mode " + mode);
        System.out.println("test1 INTENT TEST IN ONHANDLEINTENT id " + id);
        System.out.println("test1 INTENT TEST IN ONHANDLEINTENT repeat type " + repeatType);

        int currentAlarmsLeft = getAlarmsLeftQty(id);


        switch (mode){
            case "silent":
                mobilemode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Log.d("test1", "silent");
                break;
            case "vibrate":
                mobilemode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Log.d("test1", "vibrate");
                break;
            case "normal":
                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Log.d("test1", "normal");
                break;
        }

        if(repeatType == 0) {
            decrementAlarmsQty(id, currentAlarmsLeft);
            currentAlarmsLeft--;
            if (currentAlarmsLeft == 0) {
                alarm.cancelAlarms(getBaseContext(), id);
            }
        }

    }

    private void decrementAlarmsQty(int rowId, int alarmsLeft){

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALRMS_LEFT_QTY, alarmsLeft - 1);

        String whereClause = "_ID=?";
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        dbHelper.close();
    }


    private int getAlarmsLeftQty(int rowId){

        Log.d("rowid", Integer.toString(rowId));

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        String selection = "_ID" + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        cursor.moveToFirst();

        int alarmsLeftIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALRMS_LEFT_QTY);

        int alarmsLeft = cursor.getInt(alarmsLeftIndex);

        cursor.close();
        dbHelper.close();

        return alarmsLeft;
    }
}