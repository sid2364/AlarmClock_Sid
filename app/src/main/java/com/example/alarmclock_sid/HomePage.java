package com.example.alarmclock_sid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


@SuppressLint({ "SimpleDateFormat", "ValidFragment", "NewApi" }) public class HomePage extends Activity {

	ArrayList<String> list=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	ListView lvAlarms;
	TextView textViewTime;
	AlertDialog alert;
	AlertDialog.Builder builderAddAlarm;
	AlarmManager alarmManager;
	PendingIntent pendingIntent;
	Context context;
	Thread thread;

	static String TIME_FOR_ALARM;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);
		this.context = this;

		/* Thread for showing current time */
		thread = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(500);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Calendar c = Calendar.getInstance();

								SimpleDateFormat df = new SimpleDateFormat("HH:mm");
								String formattedDate = df.format(c.getTime());

								RelativeLayout background= (RelativeLayout)findViewById(R.id.layoutBackground);

								int hoursForBackground = Integer.parseInt(formattedDate.substring(0,2));

								switch(hoursForBackground){ /* background changes color according to time */
									case 21:case 22:case 23:case 0:case 1:case 2:case 3:
									case 4:case 5:case 6:
										background.setBackgroundColor(Color.argb(255, 60, 80 , 95));/* dark blue */
										break;
									case 7:case 8:case 9:case 10:case 11:case 12:case 13:
										background.setBackgroundColor(Color.argb(255, 255, 145, 55));/* orange */
										break;
									case 14:case 15:case 16:case 17:case 18:case 19:case 20:
										background.setBackgroundColor(Color.argb(255, 169, 229, 226));/*sky blue */
										break;
									default:
										background.setBackgroundColor(Color.WHITE);
								}

								textViewTime = (TextView)findViewById(R.id.textViewCurrentTime);
								textViewTime.setText(formattedDate);
							}
						});
					}
				} catch (InterruptedException e) {
				}
			}
		};
		thread.start();

		/* Set up alert dialog for adding alarm */
		builderAddAlarm = new AlertDialog.Builder(this);
		builderAddAlarm.setTitle("Confirm");
		builderAddAlarm.setMessage("Add a new alarm?");
		builderAddAlarm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DialogFragment newFragmentTime = new TimePickerFragment();
				newFragmentTime.show(getFragmentManager(), "timePicker");
				dialog.dismiss();
			}
		});
		builderAddAlarm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert = builderAddAlarm.create();

		/* Set up the list view to show all alarms that are set */
		SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());

		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		lvAlarms = (ListView)findViewById(R.id.listViewAlarms);
		lvAlarms.setAdapter(adapter);

		String[] alarms = db.getAllAlarms();
		if(alarms.length == 0)
			adapter.add("No alarms are set.");
		else
			for(String t : alarms)
				adapter.add(t);

		/* Add click listeners to the list view */
		lvAlarms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
										   int position, long id) {
				String clicked = (String)lvAlarms.getItemAtPosition(position);
				if(clicked.length() > 5){
					Toast.makeText(getApplicationContext(),"Add an alarm by pressing the button above.",Toast.LENGTH_SHORT).show();
					return true;
				}
				final int pos = position;
				final String click = clicked;
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
							case DialogInterface.BUTTON_POSITIVE:
								SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());
								db.deleteAlarm(click);
								Toast.makeText(getApplicationContext(),"Alarm deleted!",Toast.LENGTH_SHORT).show();
								list.remove(pos);
								if(list.size()==0){
									adapter.add("No alarms are set.");
								}
								break;

							case DialogInterface.BUTTON_NEGATIVE:
								/* Do nothing */
								break;
						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Confirm");
				builder.setMessage("Are you sure you want to delete this alarm?").setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener).show();

				return true;
			}
		});

		lvAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				/* Opens the AlarmRing activity and user manually waits for alarm to ring */
				String clicked = (String)lvAlarms.getItemAtPosition(position);
				if(clicked.length() <= 5) {
					Intent intent = new Intent(HomePage.this, AlarmRing.class);
					intent.putExtra("timeValue", clicked);
					startActivity(intent);
					finish();
				}
				else
					Toast.makeText(getApplicationContext(),"Add an alarm by pressing the button above.",Toast.LENGTH_SHORT).show();

			}
		});

	}

	/* Function executes when "Add Alarm" button is pressed */
	public void addAlarm(View v){
		alert.show();
	}


	/* Class creates time picker dialog for user and returns time chosen */
	public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			/* Use the current time as the default values for the picker */
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if(hourOfDay < 10)
				TIME_FOR_ALARM = "0"+hourOfDay+":";
			else
				TIME_FOR_ALARM = hourOfDay+":";
			if(minute < 10)
				TIME_FOR_ALARM += "0"+minute;
			else
				TIME_FOR_ALARM += minute+"";

			SimpleDateFormat format = new SimpleDateFormat("HH:mm");

			Calendar calendar = Calendar.getInstance();
			String alarmTime = hourOfDay+":"+minute, currentTime = format.format(calendar.getTime());

			Date d1 = null;
			Date d2 = null;

			boolean flag = false;
			long diffHours = 0, diffMinutes = 0;

			/* Calculate time difference between now and the alarm */
			try {
				d1 = format.parse(alarmTime);
				d2 = format.parse(currentTime);

				long diff = d1.getTime() - d2.getTime();

				if( diff < 0 ){
					flag = true;
				}

				diffMinutes = diff / (60 * 1000) % 60;
				diffHours = diff / (60 * 60 * 1000) % 24;

				if(flag){
					diffHours += 23;
					diffMinutes = ( 59 + diffMinutes ) % 59;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if(diffHours == 0 && diffMinutes == 0){
				Toast.makeText(getApplicationContext(), "You cannot set an alarm for the current time!", Toast.LENGTH_LONG).show();
				return;
			}

			String toastString = "Alarm will ring in ";
			if(diffHours == 1)
				toastString += diffHours+ " hour and ";
			else if(diffHours > 1)
				toastString += diffHours+ " hours and ";
			if(diffMinutes == 1)
				toastString += diffMinutes+" minute";
			else
				toastString += diffMinutes+" minutes!";
			Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();

			/* Add pending intent for alarm */
			setAlarm(hourOfDay, minute);

			/* Add to db */
			SimpleDatabaseHelper db = new SimpleDatabaseHelper(getApplicationContext());
			db.addAlarm(hourOfDay, minute);

			/* Add to listView */
			if(list.size()==1 && adapter.getItem(0).length()>5){
				adapter.clear();
			}
			adapter.add(TIME_FOR_ALARM);
			adapter.notifyDataSetChanged();

			Intent intent = new Intent(HomePage.this, AlarmRing.class);
			intent.putExtra("timeValue", TIME_FOR_ALARM);
			startActivity(intent);
			finish();
		}
	}

	public void setAlarm(int hourOfDay, int minute){

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);

		/*
		Throwing a "getSlotFromBufferLocked: unknown buffer" error.

		Intent intent = new Intent(HomePage.this, AlarmReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(HomePage.this, 0, intent, 0);
		alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
		*/
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
