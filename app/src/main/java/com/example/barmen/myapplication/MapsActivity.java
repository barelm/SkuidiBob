package com.example.barmen.myapplication;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String urlString = "http://193.106.55.45:5000/measurements";
    private GoogleMap mMap;

    private Location mUserLoc;
    private float mDistanceNotif = 1000;
    private GoogleApiClient mGoogleApiClient;

    private JSONArray arrMeasurements = null;

    private int tmpPrevRainStrength = 0;

    private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//      Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Alarm alarm = new Alarm();
        //alarm.setAlarm(this);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mUserLoc = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mUserLoc != null) {

            // Zoom in the Camera
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

            // Move the Camera to the location of the marker
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mUserLoc.getLatitude(),
                    mUserLoc.getLongitude())));

            mMap.setMyLocationEnabled(true);

            new ReadServerData().execute(urlString);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
                    arrMeasurements = reader.getJSONArray("Measurements");

                    // Add/Hide the markers on the map
                    addHideMarkers(true);

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
    }

    private void addHideMarkers(boolean showNotif)
    {
        boolean dispNotif = false;

        if(this.mMap != null)
        {
            //This is the current user-viewable region of the map
            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            for(int i = 0; i < arrMeasurements.length(); i++)
            {
                JSONObject currMeas = null;
                try {

                    currMeas = arrMeasurements.getJSONObject(i);

                    // Get the measurement id
                    int measID = Integer.parseInt(currMeas.getString("id"));

                    // Taking only the last measurement
                    if( i == (arrMeasurements.length() - 1) || measID == 428 || measID == 429)
                    {
                        int rainPower = currMeas.getInt("rain_power");

                        // If we received different rain strength, remove previous markers
                        if( rainPower != tmpPrevRainStrength && measID != 428 && measID != 429)
                        {
                            mMap.clear();
                            visibleMarkers.clear();
                            tmpPrevRainStrength = rainPower;
                        }

                        // Getting measurement data
                        double x_coord = Double.parseDouble(currMeas.getString("x_coordinate"));
                        double y_coord = Double.parseDouble(currMeas.getString("y_coordinate"));

                        //If the item is within the the bounds of the screen
                        if(bounds.contains(new LatLng(y_coord, x_coord)))
                        {
                            //If the item isn't already being displayed
                            if(!visibleMarkers.containsKey(measID))
                            {
                                //Add the Marker to the Map and keep track of it with the HashMap
                                //getMarkerForItem just returns a MarkerOptions object
                                this.visibleMarkers.put(measID, this.mMap.addMarker(getMarkerForMeasurement(currMeas)));

                                // Only if we need to show notification
                                if ((showNotif) && (!dispNotif) && (this.CheckDist(mUserLoc, x_coord, y_coord)))
                                {
                                    NotificationCompat.Builder mBuilder =
                                            new NotificationCompat.Builder(this)
                                                    .setSmallIcon(R.drawable.rain)
                                                    .setContentTitle("It's raining around you")
                                                    .setContentText("Don't forget WeBrella!");

                                    NotificationManager mNotificationManager =
                                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                    mNotificationManager.notify(1, mBuilder.build());

                                    dispNotif = true;
                                }
                            }
                        }

                        //If the marker is off screen
                        else
                        {
                            //If the course was previously on screen
                            if(visibleMarkers.containsKey(measID))
                            {
                                //1. Remove the Marker from the GoogleMap
                                visibleMarkers.get(measID).remove();

                                //2. Remove the reference to the Marker from the HashMap
                                visibleMarkers.remove(measID);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Execute the thread again because we want realtime data
            new ReadServerData().execute(urlString);
        }
    }

//    public void setRepeatingServerReadTask() {
//
//        final Handler handler = new Handler();
//        Timer timer = new Timer();
//
//        // timer task
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    public void run() {
//                        try {
//                            // Execute async task to read measurements data from the server
//                            new ReadServerData().execute(urlString);
//                        } catch (Exception e) {
//                            // error, do something
//                        }
//                    }
//                });
//            }
//        };
//
//        timer.schedule(task, 0, 5000);  // run every minute
//    }

    private MarkerOptions getMarkerForMeasurement(JSONObject objMeasurement) {
        try {
            // Getting measurement data
            double x_coord = Double.parseDouble(objMeasurement.getString("x_coordinate"));
            double y_coord = Double.parseDouble(objMeasurement.getString("y_coordinate"));

            // Build the marker title
            String markerTitle = "Measurement Details";

            // Set snippet according to rain strength
            String snippet = "";

            // Holds the marker icon to place on the map
            BitmapDescriptor markerIcon = null;

            String dateTime = objMeasurement.getString("datetime");
            if(dateTime != "null") {
//             markerTitle = markerTitle + "Date: " + dateTime + '\n';
            }

            if (!objMeasurement.isNull("rain_power")) {
                int rainPower = objMeasurement.getInt("rain_power");

                // Set marker icon according to the rain power
                if (rainPower == 1) {
//                        markerTitle = markerTitle + "Rain Strength: Low" + "\n";
                    snippet = "Rain Strength: Low";
                    markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.weak_rain);
                } else if (rainPower == 2) {
//                        markerTitle = markerTitle + "Rain Strength: High" + "\n";
                    snippet = "Rain Strength: High";
                    markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.strong_rain);
                } else {
                    snippet = "No Rain Detected";
                    markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.sunny);
                }
            } else {
               // markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.sunny);
            }

//                // If the icon is still initial, give it sunny value
//                if (markerIcon == null) {
//                    markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.sunny);
//                }
            String temperature = objMeasurement.getString("temperature");
            if(temperature != "null") {

                // Convert to numeric value
                int numericTemperature = (int)Double.parseDouble(temperature);
//                    markerTitle = markerTitle + "Temperature: " + numericTemperature + "\n";
            }

            String humidity = objMeasurement.getString("humidity");
            if(humidity != "null") {
//                    markerTitle = markerTitle + "Humidity: " + humidity + "\n";
            }

            String sea_level = objMeasurement.getString("sea_level");
            if(sea_level != "null") {
//                    markerTitle = markerTitle + "Sea Level: " + sea_level + "\n";
            }

            String air_pollution = objMeasurement.getString("air_pollution");
            if(air_pollution != "null") {
//                    markerTitle = markerTitle + "Air Pollution: " + air_pollution + "\n";
            }

            // Create coordinate object
            LatLng collegeMgmtCoords = new LatLng(y_coord, x_coord);

            // Add a marker in the coordinates
            return (new MarkerOptions().position(collegeMgmtCoords).title(markerTitle)
                    .icon(markerIcon).snippet(snippet));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean CheckDist(Location currLoc, double XDest, double YDest) {
        if (currLoc == null)
            return false;

        Location targetLocation = new Location("");
        targetLocation.setLatitude(YDest);
        targetLocation.setLongitude(XDest);

        float dist = currLoc.distanceTo(targetLocation);

        if (dist < mDistanceNotif)
            return true;
        else
            return false;
    }

//    private void RaiseNotif() {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.rain)
//                        .setContentTitle("Rain around you")
//                        .setContentText("muhahahahahahaha");
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        mNotificationManager.notify(1, mBuilder.build());
//    }

//    private class Alarm extends BroadcastReceiver
//    {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            RaiseNotif();
//        }
//
//        public void setAlarm(Context context)
//        {
//            AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//            Intent i = new Intent(context, Alarm.class);
//            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
//            am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 5, pi); // Millisec * Second * Minute
//        }
//
//        public void cancelAlarm(Context context)
//        {
//            Intent intent = new Intent(context, Alarm.class);
//            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            alarmManager.cancel(sender);
//        }
//    }
}
