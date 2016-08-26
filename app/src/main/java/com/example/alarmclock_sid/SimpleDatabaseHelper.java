package com.example.alarmclock_sid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDatabaseHelper {
	private SQLiteOpenHelper _openHelper;
    
	private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "AlarmClock_Alarms";

    private static final String TABLE_NAME_ALARMS = "alarms";

    private static final String KEY_TIME = "time";

    public SimpleDatabaseHelper(Context context) {
        _openHelper = new SimpleSQLiteOpenHelper(context);
    }

    /* This is an internal class that handles the creation of all database tables */
    class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {
        SimpleSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME+".db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	String CREATE_TABLE_ALARMS = "create table if not exists " + TABLE_NAME_ALARMS + "("
                    + KEY_TIME + " text)";
            db.execSQL(CREATE_TABLE_ALARMS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public void addAlarm(int hour, int minute){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        String time;
        if(hour < 10){
            time = "0"+hour;
        }
        time = hour+":";
        if(minute < 10){
            time = "0"+minute;
        }
        time = time.concat(minute+"");

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);

        String sql = "INSERT INTO "+TABLE_NAME_ALARMS+" VALUES('"+time+"')";
        db.execSQL(sql);
        db.close();
    }
    public void deleteAlarm(String time){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        db.execSQL("delete from "+TABLE_NAME_ALARMS+" where "+KEY_TIME+"='"+time+"';");
        db.close();
    }
    public String[] getAllAlarms() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select distinct " + KEY_TIME + " from " + TABLE_NAME_ALARMS, null);
        if (cursor.getCount() == 0) return new String[0];

        String[] alarms = new String[cursor.getCount()];

        cursor = db.rawQuery("select distinct " + KEY_TIME + " from " + TABLE_NAME_ALARMS, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            alarms[i++] = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            cursor.moveToNext();
        }
        db.close();
        return alarms;
    }
}