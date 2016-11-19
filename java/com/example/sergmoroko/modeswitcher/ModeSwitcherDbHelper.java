package com.example.sergmoroko.modeswitcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sergmoroko.modeswitcher.ModeSwitcherDbContract.dataEntry;

/**
 * Created by ssss on 14.11.2016.
 */

public class ModeSwitcherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "settings.db";

    public ModeSwitcherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE "+
                        dataEntry.TABLE_NAME + " (" +
                        dataEntry._ID + " INTEGER PRIMARY KEY," +
                        dataEntry.COLUMN_START_HOUR + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_START_MINUTE + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_BREAK_START_HOUR + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_BREAK_START_MINUTE + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_BREAK_LENGTH + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_END_HOUR + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_END_MINUTE + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_ALARM_MODE + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_MONDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_TUESDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_WEDNESDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_THURSDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_FRIDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_SATURDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_REPEAT_SUNDAY + " INTEGER NOT NULL, " +
                        dataEntry.COLUMN_SUMMARY + " TEXT, " +
                        dataEntry.COLUMN_REPEAT_STRING + " TEXT, " +
                        dataEntry.COLUMN_DESCRIPTION + " TEXT" + ");";

        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + dataEntry.TABLE_NAME);
        onCreate(db);
    }
}
