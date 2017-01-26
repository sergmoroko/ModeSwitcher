package com.example.sergmoroko.profileSwitcher;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;


public class AlarmService extends IntentService {

    private ModeSwitcherDbHelper dbHelper;
    private SQLiteDatabase db;
    private AlarmReceiver alarm = new AlarmReceiver();


    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        String soundProfile = intent.getStringExtra(Constants.INTENT_EXTRA_MODE);
        int id = intent.getIntExtra(Constants.INTENT_EXTRA_ID, 0);
        int repeatType = intent.getIntExtra(Constants.INTENT_EXTRA_REPEAT_TYPE, 0);

        // quantity of alarms left
        int currentAlarmsLeft = getAlarmsLeftQty(id);

        // setting ringer mode to received from intent
        switch (soundProfile) {
            case Constants.SOUND_PROFILE_SILENT:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            case Constants.SOUND_PROFILE_VIBRATE:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            case Constants.SOUND_PROFILE_NORMAL:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
        }

        // if current alarm is not repeated weekly
        if (repeatType == Constants.REPEAT_TYPE_NO_REPEATS) {
            decrementAlarmsQty(id, currentAlarmsLeft);
            currentAlarmsLeft--;
            // cancel alarm if this were last one
            if (currentAlarmsLeft == 0) {
                alarm.cancelAlarms(getBaseContext(), id);
            }
        }

    }

    // this method decrements quantity of alarms left in database
    private void decrementAlarmsQty(int rowId, int alarmsLeft) {

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(ModeSwitcherDbContract.dataEntry.COLUMN_ALARMS_LEFT_QTY, alarmsLeft - 1);

        String whereClause = Constants.WHERE_CLAUSE_ROW_ID;
        String[] whereArgs = new String[]{String.valueOf(rowId)};
        db.update(ModeSwitcherDbContract.dataEntry.TABLE_NAME, cv, whereClause, whereArgs);

        db.close();
        dbHelper.close();
    }


    // method returns quantity of alarms left from database by row ID
    private int getAlarmsLeftQty(int rowId) {

        dbHelper = new ModeSwitcherDbHelper(this);
        db = dbHelper.getWritableDatabase();

        String selection = Constants.WHERE_CLAUSE_ROW_ID;
        String[] selectionArgs = new String[]{String.valueOf(rowId)};

        Cursor cursor = db.query(ModeSwitcherDbContract.dataEntry.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        cursor.moveToFirst();

        int alarmsLeftIndex = cursor.getColumnIndex(ModeSwitcherDbContract.dataEntry.COLUMN_ALARMS_LEFT_QTY);

        int alarmsLeft = cursor.getInt(alarmsLeftIndex);

        cursor.close();
        db.close();
        dbHelper.close();

        return alarmsLeft;
    }
}