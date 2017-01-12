package com.example.sergmoroko.modeswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ssss on 28.11.2016.
 */

public class BootReceiver extends BroadcastReceiver{

    AlarmReceiver alarm = new AlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            //// TODO: 20.12.2016 bulk alarms set
            alarm.setBulkAlarms(context);
        }
    }
}
