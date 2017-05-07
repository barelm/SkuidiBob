package com.example.barmen.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.SeekBar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Barmen on 21/04/2017.
 */

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks {

    private static final int mDbSecondUpdate = 10;

    Handler h = new Handler();
    Runnable runnable;
    Location mUserLoc;

    private double mDistanceNotif = 0;

    private GoogleApiClient mGoogleApiClient;
    private int lastRainMode = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (this.mGoogleApiClient.isConnected() == false) {
            this.mGoogleApiClient.connect();
        }

//        if ( intent != null) {
//            Bundle extras = intent.getExtras();
//
//            if (extras != null) {
//                mUserLoc = (Location)extras.get("Loc");
//            }
//        }

//        if (mUserLoc != null) {
//            CheckRainPower();
//        }

        h.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (mUserLoc != null) {
                    CheckRainPower(mUserLoc);
                }

                runnable = this;

                h.postDelayed(runnable, mDbSecondUpdate * 1000);
            }
        }, mDbSecondUpdate * 1000);

        //we have some options for service
        //start sticky means service will be explicity started and stopped
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Stop reading from db.
        h.removeCallbacks(runnable);

        if (this.mGoogleApiClient.isConnected() == true){
            this.mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    public void CheckRainPower(Location userLoc){

        if (mDistanceNotif == 0) {
            SharedPreferences load = getSharedPreferences("setting", 0);
            mDistanceNotif = ( load.getInt("distance_notif", 4) + 1 ) / 5;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority("193.106.55.45:5000")
                .appendPath("isRaining")
                .appendQueryParameter("lat",  Double.toString(userLoc.getLatitude()))
                .appendQueryParameter("long", Double.toString(userLoc.getLongitude()))
                .appendQueryParameter("radius", Double.toString(mDistanceNotif));

        String Url = builder.build().toString();
        new ReadServerData().execute(Url);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.mUserLoc = LocationServices.FusedLocationApi.getLastLocation(
                this.mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

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

                if (rainPower > 0) {
                    RaiseNotif(rainPower);
                }
            }
        }
    }

    public void DecideIfNotif(int rainPower){

        // If sunny or dont have any measurement - do not send notification.
        if (rainPower == -1 || rainPower == 0){
            lastRainMode = 0;
            return;
        }

        // If rain power dont change - not send notification.
        if (rainPower == lastRainMode){
            return;
        }

        RaiseNotif(rainPower);
    }

    public void RaiseNotif(int rainPower) {
        int color = 0;
        String contentTitile = "";

        if (rainPower == 2){
            color = Color.BLUE;
            contentTitile = "It's raining heavily around you";
        }else if (rainPower == 1){
            color = Color.GREEN;
            contentTitile = "It's raining lightly around you";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.rain)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rain))
                        //.setVibrate(new long[]{ 0, 100, 200, 300 })
                        .setColor(color)
                        .setLights(color, 300, 300)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentTitle(contentTitile)
                        .setContentText("Don't forget WeBrella!");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());
    }
}
