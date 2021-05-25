package com.example.weatheralarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ListActivity implements AdapterView.OnItemClickListener {

    boolean databaseWritten = false;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayList<Calendar> alarmList = new ArrayList<Calendar>();
    int alarmCount = 0;
    TimePickerDialog timePicker;
    DatePickerDialog datePicker;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy    HH:mm", Locale.ENGLISH);
    //ArrayList<AlarmListener> alarmListenerList = new ArrayList<AlarmListener>();

    AlarmListener alarmListener = new AlarmListener();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    //ArrayAdapter<String> adapter;
    ListItemComponent adapter;

    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED
    int clickCounter = 0;

    protected void SaveFileToInternalStorage(String s) {

        System.out.println(getFilesDir());

        File file = new File(getFilesDir(), "alarm_list.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file.getName(), Context.MODE_APPEND));
            outputStreamWriter.write(s + "\n");
            outputStreamWriter.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    @Override
    public void onCreate(Bundle icicle) {

        registerReceiver(alarmListener, new IntentFilter());
        TTSManager.getInstance(this);
        databaseWritten = false;

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        readFromFile();
        clearFile();

        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        adapter = new ListItemComponent(listItems, alarmList, alarmListener, this);
//        adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                listItems);
        setListAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItems(view);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        if(!databaseWritten) {
            System.out.println("onDestroy");
            for (int idx = 0; idx < listItems.size(); idx++) {
                SaveFileToInternalStorage(listItems.get(idx));
            }
            databaseWritten = true;
        }
        super.onDestroy();
    }

    @Override
    public void onStop()
    {
        if(!databaseWritten) {
            System.out.println("onStop");
            for (int idx = 0; idx < listItems.size(); idx++) {
                SaveFileToInternalStorage(listItems.get(idx));
            }
            databaseWritten = true;
        }
        super.onStop();
    }

    // Create a message handling object as an anonymous class.
    public void onItemClick(AdapterView parent, View v, int position, long id) {
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    public void addItems(View view) {

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minute = cldr.get(Calendar.MINUTE);

        datePicker = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        timePicker = new TimePickerDialog(MainActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void onTimeSet(TimePicker view, int hour, int minute) {

                                        Calendar alarmInfo = Calendar.getInstance();
                                        alarmInfo.set(year, monthOfYear, dayOfMonth, hour, minute, 0);

                                        int pos = getPosition(alarmInfo);

                                        alarmList.add(pos, alarmInfo);
                                        String timeString = "";
                                        timeString = timeString + (dayOfMonth < 10 ? "0" + dayOfMonth + "/" : dayOfMonth + "/");
                                        timeString = timeString + (monthOfYear < 10 ? "0" + monthOfYear + "/" : monthOfYear + "/");
                                        timeString = timeString + year + "    ";
                                        timeString = timeString + (hour < 10 ? "0" + hour + ":" : hour + ":");
                                        timeString = timeString + (minute < 10 ? "0" + minute : minute);

                                        listItems.add(pos, timeString);
                                        //listItems.add(pos, dayOfMonth + "/" + monthOfYear + "/" + year + "    " + hour + ":" + minute);
                                        //SaveFileToInternalStorage(listItems.get(listItems.size() - 1));

                                        //AlarmListener al = new AlarmListener();
                                        alarmListener.setAlarm(MainActivity.this, alarmInfo.getTimeInMillis() - System.currentTimeMillis());

                                        System.out.println(alarmInfo.getTimeInMillis() - System.currentTimeMillis());

                                        //alarmListenerList.add(al);

                                        adapter.notifyDataSetChanged();
                                    }
                                }, hour, minute, false);

                        timePicker.show();
                    }
                }, year, month, day);
        datePicker.show();
    }

    public void readFromFile()
    {
        String ret = "";

        try {
            InputStream inputStream = openFileInput("alarm_list.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    listItems.add(receiveString);
                    Calendar c = Calendar.getInstance();
                    c.setTime(sdf.parse(receiveString));
                    alarmList.add(c);

                    System.out.println("ziua " + c.getTimeInMillis());
                }

                System.out.println("nr iteme in lista " + alarmList.size());

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void clearFile() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("alarm_list.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        } catch (Exception e) {
        }
    }

    int getPosition(Calendar alarmTime)
    {
        int pos = 0;

        if(alarmList.size() == 0)
        {
            return pos;
        }
        else
        {
            for (int idx = 0; idx < alarmList.size(); idx++)
            {
                if (alarmTime.getTimeInMillis() > alarmList.get(idx).getTimeInMillis())
                {
                    System.out.println("index " + idx);
                    pos = idx + 1;
                }
            }
        }

        return pos;
    }
}