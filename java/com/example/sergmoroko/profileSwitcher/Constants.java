package com.example.sergmoroko.profileSwitcher;


 class Constants {
    /**
     * Intent extras names
     */
    static final String INTENT_EXTRA_ROW_ID = "rowId";
    static final String INTENT_EXTRA_ID = "id";
    static final String INTENT_EXTRA_STATUS = "status";
    static final String INTENT_EXTRA_TURN_ON = "turn_on";
    static final String INTENT_EXTRA_DELETE = "delete";
    static final String INTENT_EXTRA_MODE = "mode";
    static final String INTENT_EXTRA_REPEAT_TYPE = "repeatType";

    /**
     * DB strings
     */
    static final String WHERE_CLAUSE_ROW_ID = "_ID=?";

    /**
     * Sound profile string extras
     */
    static final String SOUND_PROFILE_NORMAL = "normal";
    static final String SOUND_PROFILE_VIBRATE = "vibrate";
    static final String SOUND_PROFILE_SILENT = "silent";

    /**
     * Sound profile id's
     */
    static final int SOUND_PROFILE_NORMAL_ID = 0;
    static final int SOUND_PROFILE_SILENT_ID = 1;
    static final int SOUND_PROFILE_VIBRATE_ID = 2;

    /**
     * Alarm repeat types
     */
    static final int REPEAT_TYPE_NO_REPEATS = 0;
    static final int REPEAT_TYPE_EVERY_WEEK = 1;
    static final int REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_THIS = 2;
    static final int REPEAT_TYPE_EVERY_TWO_WEEKS_FROM_NEXT = 3;

    /**
     * Settings strings
     */
    static final String SETTINGS_PRE_DELAY = "pre_delay_enabled";
    static final String SETTINGS_POST_DELAY = "post_delay_enabled";

    /**
     * Random number constants
     */

    static final int WEEK_LENGTH_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;
    static final int HOUR_LENGTH_IN_MINUTES = 60;
    static final int DAY_LENGTH_IN_HOURS = 24;
    static final int DELAY_IN_MILLIS = 1000 * 60 * 5;
    static final int DAYS_IN_WEEK_QTY = 7;
    static final int WORKING_DAYS_IN_WEEK_QTY = 5;

    /**
     * Day numbers
     */
    static final int MONDAY = 0;
    static final int TUESDAY = 1;
    static final int WEDNESDAY = 2;
    static final int THURSDAY = 3;
    static final int FRIDAY = 4;
    static final int SATURDAY = 5;
    static final int SUNDAY = 6;

    /**
     * Details Activity listView positions
     */
    static final int START_TIME_POSITION = 0;
    static final int BREAK_TIME_POSITION = 1;
    static final int BREAK_LENGTH_POSITION = 2;
    static final int END_TIME_POSITION = 3;
    static final int REPEAT_POSITION = 4;
    static final int SOUND_PROFILE_POSITION = 5;
    static final int DESCRIPTION_POSITION = 6;

    /**
     * DetailsActivity other
     */

    static final int BREAK_LENGTH_SEEK_BAR_MULTIPLIER = 10;

    /**
     * SavedInstanceState strings
     */

    static final String REPEAT_TEMPORARY_ARRAY = "repeat_temp";
    static final String DESCRIPTION_SAVE = "description";
    static final String REPEAT_TYPE_SAVE = "repeatType";
    static final String REPEAT_START_SAVE = "repeatStart";
    static final String START_HOUR_SAVE = "startHour";
    static final String START_MINUTE_SAVE = "startMinute";
    static final String END_HOUR_SAVE = "endHour";
    static final String END_MINUTE_SAVE = "endMinute";
    static final String BREAK_LENGTH_SAVE = "breakLength";
    static final String ALARM_MODE_SAVE = "alarmMode";
    static final String BREAK_START_HOUR_SAVE = "breakStartHour";
    static final String BREAK_START_MINUTE_SAVE = "breakStartMinute";
    static final String REPEAT_ARRAY_SAVE = "repeat";

}
