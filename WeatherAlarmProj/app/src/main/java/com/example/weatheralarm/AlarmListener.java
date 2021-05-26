package com.example.weatheralarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlarmListener extends BroadcastReceiver {

    static public String st;

    public void setSt(String param)
    {
        st = param;
        System.out.println("setter" + st + " " + param);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:AlarmListener");
        wl.acquire();

        getLocation(context);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TTSManager.getInstance(context).saySomething(st);
                Toast.makeText(context, "Alarm: " + st, Toast.LENGTH_LONG).show(); // For example
                context.sendBroadcast(new Intent("ALARM_NOTIFY"));
            }
        }, 5000);

        //MediaPlayer mp = MediaPlayer.create(context.getApplicationContext(), RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION));
        //mp.start();

        wl.release();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm(Context context, long milliseconds) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmListener.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        System.out.println("setALARM " + milliseconds + "ms");

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + milliseconds, pi);


        System.out.println("response is: " + st);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmListener.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void requestAPI(Context context, double latitude, double longiude)
    {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        //EX: https://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=12d0631370e9393c5d0facb35658c6b3

        final String baseUrl = "https://api.openweathermap.org/data/2.5/weather?lat=";
        final String suffixUrl = "&appid=12d0631370e9393c5d0facb35658c6b3";

        String url = baseUrl + latitude + "&lon=" + longiude + suffixUrl;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            parseJSON(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setSt(error.getLocalizedMessage());
            }
        });
        queue.add(stringRequest);
    }

    public void parseJSON(String s) throws JSONException {
        JSONObject jObject = new JSONObject(s);
        JSONArray jsonArray = jObject.getJSONArray("weather");

        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String generalWeather = jsonObject.getString("description");

        jsonObject = jObject.getJSONObject("main");
        double temperature = Math.ceil(Double.parseDouble(jsonObject.getString("temp"))) - 273;

        jsonObject = jObject.getJSONObject("wind");
        String wind = jsonObject.getString("speed");

        setSt(generalWeather + ", " + temperature + " degrees, wind intensity is " + wind + " meters per second");
    }

    public void getLocation(Context context)
    {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);

        final String provider = lm.getBestProvider(c, true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        System.out.println("location coords: " + location.getLatitude() + " " + location.getLongitude());

        requestAPI(context, location.getLatitude(), location.getLongitude());
    }
}