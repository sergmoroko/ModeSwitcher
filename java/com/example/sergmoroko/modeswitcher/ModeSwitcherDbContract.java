package com.example.sergmoroko.modeswitcher;

import android.provider.BaseColumns;

/**
 * Created by ssss on 14.11.2016.
 */

public class ModeSwitcherDbContract {

    public static final class dataEntry implements BaseColumns{
        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_START_HOUR = "start_hour";
        public static final String COLUMN_START_MINUTE = "start_minute";
        public static final String COLUMN_BREAK_START_HOUR = "break_start_hour";
        public static final String COLUMN_BREAK_START_MINUTE = "break_start_minute";
        public static final String COLUMN_END_HOUR = "end_hour";
        public static final String COLUMN_END_MINUTE = "end_minute";
        public static final String COLUMN_BREAK_LENGTH = "break_length";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ALARM_MODE = "alarm";
        public static final String COLUMN_REPEAT_MONDAY = "repeat_mon";
        public static final String COLUMN_REPEAT_TUESDAY = "repeat_tue";
        public static final String COLUMN_REPEAT_WEDNESDAY = "repeat_wed";
        public static final String COLUMN_REPEAT_THURSDAY = "repeat_thu";
        public static final String COLUMN_REPEAT_FRIDAY = "repeat_fri";
        public static final String COLUMN_REPEAT_SATURDAY = "repeat_sat";
        public static final String COLUMN_REPEAT_SUNDAY = "repeat_sun";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_REPEAT_STRING = "repeat_string";
        public static final String COLUMN_STATE = "is_enabled";
        public static final String COLUMN_WEEKLY_REPEAT_TYPE = "weekly_repeat_type";
        public static final String COLUMN_WEEKLY_REPEAT_BEGINNING = "weekly_repeat_beginning";
        public static final String COLUMN_ALARM_START_ID = "alarm_start_id";
        public static final String COLUMN_ALARM_END_ID = "alarm_end_id";
        public static final String COLUMN_ALRMS_LEFT_QTY = "alarms_left";


    }
}
