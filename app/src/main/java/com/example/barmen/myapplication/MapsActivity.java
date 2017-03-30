package com.example.barmen.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String urlString = "http://193.106.55.45:5000/measurements";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private class ReadServerData extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings){
            String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream){

            //..........Process JSON DATA................
            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader= new JSONObject(stream);

                    // Get jsonArrray of the measurements
                    placeCoordsOnMap(reader.getJSONArray("Measurements"));

                }catch(JSONException e){
                    e.printStackTrace();
                }

            } // if statement end
        } // onPostExecute() end
    } // ProcessJSON class end

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Save the map object
        mMap = googleMap;

        // Execute async task to read measurements data from the server
        // This will happen every 5 seconds
        setRepeatingServerReadTask();
    }

    public void setRepeatingServerReadTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            // Execute async task to read measurements data from the server
                            new ReadServerData().execute(urlString);
                        } catch (Exception e) {
                            // error, do something
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 5000);  // interval of 5 seconds
    }

    public void placeCoordsOnMap(JSONArray arrMeasurements){
        Marker locationMarker;

        // Notification code - start
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.rain)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());
        // Notification code - End

        // Go through every measurement and add an appropriate marker
        for(int i = 0; i < arrMeasurements.length(); i++)
        {
            // Getting the measurement json object
            JSONObject currMeas = null;
            try {
                currMeas = arrMeasurements.getJSONObject(i);

                // Getting X and Y coordinates
                double x_coord = Double.parseDouble(currMeas.getString("x_coordinate"));
                double y_coord = Double.parseDouble(currMeas.getString("y_coordinate"));

                // Create coordinate object
                LatLng collegeMgmtCoords = new LatLng(y_coord, x_coord);

                // Add a marker in the coordinates
                locationMarker = mMap.addMarker(new MarkerOptions().position(collegeMgmtCoords).title("It's raining men, hallelujah")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.rain)));

                // Move the Camera to the location of the marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(collegeMgmtCoords));

                // Zoom in the Camera
                mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

                // Show the information window
                locationMarker.showInfoWindow();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
