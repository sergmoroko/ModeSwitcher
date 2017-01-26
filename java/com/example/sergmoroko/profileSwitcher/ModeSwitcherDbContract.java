package com.example.sergmoroko.profileSwitcher;

import android.provider.BaseColumns;


class ModeSwitcherDbContract {

    static final class dataEntry implements BaseColumns{
        static final String TABLE_NAME = "settings";
        static final String COLUMN_START_HOUR = "start_hour";
        static final String COLUMN_START_MINUTE = "start_minute";
        static final String COLUMN_BREAK_START_HOUR = "break_start_hour";
        static final String COLUMN_BREAK_START_MINUTE = "break_start_minute";
        static final String COLUMN_END_HOUR = "end_hour";
        static final String COLUMN_END_MINUTE = "end_minute";
        static final String COLUMN_BREAK_LENGTH = "break_length";
        static final String COLUMN_DESCRIPTION = "description";
        static final String COLUMN_ALARM_MODE = "alarm";
        static final String COLUMN_REPEAT_MONDAY = "repeat_mon";
        static final String COLUMN_REPEAT_TUESDAY = "repeat_tue";
        static final String COLUMN_REPEAT_WEDNESDAY = "repeat_wed";
        static final String COLUMN_REPEAT_THURSDAY = "repeat_thu";
        static final String COLUMN_REPEAT_FRIDAY = "repeat_fri";
        static final String COLUMN_REPEAT_SATURDAY = "repeat_sat";
        static final String COLUMN_REPEAT_SUNDAY = "repeat_sun";
        static final String COLUMN_SUMMARY = "summary";
        static final String COLUMN_REPEAT_STRING = "repeat_string";
        static final String COLUMN_STATE = "is_enabled";
        static final String COLUMN_WEEKLY_REPEAT_TYPE = "weekly_repeat_type";
        static final String COLUMN_WEEKLY_REPEAT_BEGINNING = "weekly_repeat_beginning";
        static final String COLUMN_ALARM_START_ID = "alarm_start_id";
        static final String COLUMN_ALARM_END_ID = "alarm_end_id";
        static final String COLUMN_ALARMS_LEFT_QTY = "alarms_left";

    }
}
