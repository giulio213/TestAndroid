package com.example.weatheralarm;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class ListItemComponent extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<Calendar> alarmList = new ArrayList<Calendar>();
    private Context context;
    private AlarmListener alarmListener;

    TimePickerDialog timePicker;
    DatePickerDialog datePicker;

    public ListItemComponent(ArrayList<String> list, ArrayList<Calendar> alarm_list, AlarmListener alarmListener, Context context) {
        this.list = list;
        this.alarmList = alarm_list;
        this.context = context;
        this.alarmListener = alarmListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return list.indexOf(pos);
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, null);
        }

        //Handle TextView and display string from your list
        TextView tvContact = (TextView) view.findViewById(R.id.tvContact);
        tvContact.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button) view.findViewById(R.id.delete);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);

                if(position == 0)
                {
                    alarmListener.cancelAlarm(context);
                }

                notifyDataSetChanged();
            }
        });

        Button editBtn = (Button) view.findViewById(R.id.edit);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minute = cldr.get(Calendar.MINUTE);

                datePicker = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                timePicker = new TimePickerDialog(context,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hour, int minute) {

                                                Calendar alarmInfo = Calendar.getInstance();
                                                alarmInfo.set(year, monthOfYear, dayOfMonth, hour, minute, 0);
                                                alarmList.set(position, alarmInfo);
                                                list.set(position, dayOfMonth + "/" + monthOfYear + "/" + year + "    " + hour + ":" + minute);

//                                                alarmListener.cancelAlarm(context);
//                                                alarmListener.setAlarm(context, alarmInfo.getTimeInMillis() - System.currentTimeMillis());

                                                notifyDataSetChanged();
                                            }
                                        }, hour, minute, false);

                                timePicker.show();
                            }
                        }, year, month, day);
                datePicker.show();

            }
        });

        return view;
    }
}