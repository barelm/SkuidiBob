package com.example.barmen.myapplication;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Barmen on 21/04/2017.
 */

public class MyService extends Service {

    Handler h = new Handler();
    Runnable runnable;
    Location mUserLoc;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();

        if (extras != null) {
            mUserLoc = (Location)extras.get("Loc");
        }

        CheckRainPower();

//        h.postDelayed(new Runnable() {
//            public void run() {
//                //do something
//                CheckRainPower();
//
//                runnable = this;
//
//                h.postDelayed(runnable, 10000);
//            }
//        }, 10000);

        //we have some options for service
        //start sticky means service will be explicity started and stopped
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        h.removeCallbacks(runnable);
        super.onDestroy();
        //stopping the player when service is destroyed
    }

    public void CheckRainPower(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority("193.106.55.45:5000")
                .appendPath("isRaining")
                .appendQueryParameter("lat",  Double.toString(mUserLoc.getLatitude()))
                .appendQueryParameter("long", Double.toString(mUserLoc.getLongitude()));

        String Url = builder.build().toString();
        new ReadServerData().execute(Url);
    }

    private class ReadServerData extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream) {

            //..........Process JSON DATA................
            if (stream != null) {

                // Get rain power.
                int rainPower = Integer.parseInt(stream);

                if (rainPower > -3) {
                    RaiseNotif(rainPower);
                }
            }
        }
    }

    public void RaiseNotif(int rainPower) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.rain)
                        .setContentTitle("It's raining around you")
                        .setContentText("Don't forget WeBrella!");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());
    }
}
