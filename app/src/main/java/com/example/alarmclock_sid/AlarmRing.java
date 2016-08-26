package com.example.alarmclock_sid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmRing extends Activity {

    Thread thread;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);

        Bundle extras = getIntent().getExtras();

        final String alarmTime = extras.getString("timeValue");
        final TextView textViewAlarmTime = (TextView)findViewById(R.id.textView);
        textViewAlarmTime.setText("Alarm will ring at "+alarmTime);

        mp = MediaPlayer.create(this, R.raw.alarm_sound);

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(3000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Calendar c = Calendar.getInstance();

                                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                                String formattedDate = df.format(c.getTime());

                                if(formattedDate.equals(alarmTime)){
                                    mp.start();
                                    textViewAlarmTime.setText("ALARM RINGING!");
                                }
                                else
                                    textViewAlarmTime.setText("Alarm will ring at "+alarmTime);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }

    public void goBackToHomePage(View v){
        Intent intent = new Intent(AlarmRing.this, HomePage.class);
        mp.stop();
        finish();
        startActivity(intent);
    }
}

