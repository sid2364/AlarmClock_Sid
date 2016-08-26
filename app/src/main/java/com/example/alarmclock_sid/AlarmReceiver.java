package com.example.alarmclock_sid;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        System.out.println("IN THE RECEIVER");
        String state = intent.getExtras().getString("extra");
        Log.e("SID_Reciever", "In the receiver with " + state);

        Intent serviceIntent = new Intent(context,AlarmRing.class);
        serviceIntent.putExtra("extra", state);

        context.startService(serviceIntent);
    }

}