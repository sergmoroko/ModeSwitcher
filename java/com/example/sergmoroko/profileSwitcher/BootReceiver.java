package com.example.sergmoroko.profileSwitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// class receives device boot event, and sets alarms
public class BootReceiver extends BroadcastReceiver {

    AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setBulkAlarms(context);
        }
    }
}
