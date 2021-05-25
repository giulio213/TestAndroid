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
    static int counter = 0;

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
                alarmListener.cancelAlarm(context);

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

                                                alarmListener.cancelAlarm(context);
                                                alarmListener.setAlarm(context, alarmInfo.getTimeInMillis() - System.currentTimeMillis());
                                                //editFile(position);


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

//    public void editFile(int position) {
//        String ret = "";
//
//        try {
//            InputStream inputStream = context.openFileInput("alarm_list.txt");
//
//            if (inputStream != null) {
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                String receiveString = "";
//                StringBuilder stringBuilder = new StringBuilder();
//
//                clearFile();
//
//                while (position > 0 && (receiveString = bufferedReader.readLine()) != null) {
//                    appendToFile(receiveString);
//                    position--;
//                }
//
//                inputStream.close();
//                ret = stringBuilder.toString();
//            }
//        } catch (FileNotFoundException e) {
//            Log.e("login activity", "File not found: " + e.toString());
//        } catch (IOException e) {
//            Log.e("login activity", "Can not read file: " + e.toString());
//        }
//    }

    //    public void appendToFile(String s) {
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("alarm_list.txt", Context.MODE_APPEND));
//            outputStreamWriter.write(s + "\n");
//            outputStreamWriter.close();
//        } catch (Exception e) {
//        }
//    }
//
    protected void SaveFileToInternalStorage(String s) {

        System.out.println(context.getFilesDir());

        File file = new File(context.getFilesDir(), "alarm_list.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file.getName(), Context.MODE_APPEND));
            outputStreamWriter.write(s + "\n");
            outputStreamWriter.close();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    public void clearFile() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("alarm_list.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        } catch (Exception e) {
        }
    }

    int getPosition(Calendar alarmTime)
    {
        if(alarmList.size() == 0)
        {
            return 0;
        }
        else
        {
            for (int idx = 0; idx < alarmList.size(); idx++)
            {
                if (alarmList.get(idx).getTimeInMillis() > alarmTime.getTimeInMillis())
                {
                    return idx;
                }
            }
        }

        return 0;
    }
}