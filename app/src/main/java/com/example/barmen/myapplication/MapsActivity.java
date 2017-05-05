package com.example.barmen.myapplication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnCameraMoveListener {

    // Constant
    private static final String urlString = "http://193.106.55.45:5000/measurements";
    private static final int mDistanceNotif = 1000;

    // Global variables
    private GoogleMap mMap;
    private Location mUserLoc;
    public static GoogleApiClient mGoogleApiClient;
    private HashMap<Integer, Measurement> visibleMarkers = new HashMap<Integer, Measurement>();
    private View infoWindow;

    private ArrayList<Measurement> Measurements = new ArrayList<>();

    public String newurl;


    Handler h = new Handler();
    Runnable runnable;

    //private JSONArray arrMeasurements = null;
    //private int tmpPrevRainStrength = 0;
    //private HashMap<Integer, JSONObject> mesData = new HashMap<Integer, JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.handlePermission();

//      Create an instance of GoogleAPIClient.
        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        SharedPreferences save = getSharedPreferences("setting", 0);
//        save.edit().remove("raise_notif");

        this.infoWindow = getLayoutInflater().inflate(R.layout.info_window, null);

        // TODO: לבטל את הסרוויס שמתריע ברקע
        stopService(new Intent(this, MyService.class));
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onOpenReportPopup(View view) {
        startActivity(new Intent(MapsActivity.this, ReportPopup.class));
    }

    public void onOpenSettingsPopup(View view) {
        startActivity(new Intent(MapsActivity.this, SettingsPopup.class));
    }

    protected void onStart() {
        this.mGoogleApiClient.connect();

        super.onStart();
    }

    protected void onStop() {
        this.mGoogleApiClient.disconnect();
        h.removeCallbacks(runnable); //stop handler when activity not visible

        // Check if user want push notification.
//        if (getSharedPreferences("setting", 0).getBoolean("raise_notif", false)){
//             // TODO: להפעיל את הסרוויס שמתריע ברגע + לממש אותו כמו שצריך עם קריאה מהשרת
//            Intent serviceIntent = new Intent(this,MyService.class);
//            serviceIntent.putExtra("Loc", mUserLoc);
//            startService(serviceIntent);
//        }

        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Save the map object
        this.mMap = googleMap;

        this.mMap.setInfoWindowAdapter(this);

        this.mMap.setOnCameraMoveListener(this);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        this.mUserLoc = LocationServices.FusedLocationApi.getLastLocation(
                this.mGoogleApiClient);
        if (this.mUserLoc != null) {

            // Zoom in the Camera
            this.mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

            // Move the Camera to the location of the marker
            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(this.mUserLoc.getLatitude(),
                    this.mUserLoc.getLongitude())));

            this.mMap.setMyLocationEnabled(true);
        }

        new ReadServerData().execute(this.urlString);

        // TODO: לשים כאן את הקוד ור לשלוף את הנתונים כל X שניות
        h.postDelayed(new Runnable() {
            public void run() {
                //do something
                new ReadServerData().execute(urlString);

                runnable = this;

                h.postDelayed(runnable, 10000);
            }
        }, 10000);

    }

    @Override
    public void onCameraMove() {
        addHideMarkers(false);
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

                    // Add/Hide the markers on the map
                    handleSevrverData(reader.getJSONArray("Measurements"));

                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleSevrverData(JSONArray arrMeasurements){
        // Conver measurment json to class.
        this.Measurements = MeasurementJsonToArray(arrMeasurements);
        addHideMarkers(false);
    }

    private void addHideMarkers(boolean showNotif)
    {
        boolean dispNotif = false;
        ArrayList<Measurement> arrMeas;
        HashMap<Integer, Measurement> visibleMarkersNew = new HashMap<Integer, Measurement>();

        if(this.mMap == null)
        {
            return;
        }

//        for (int key : visibleMarkers.keySet()) {
//           //visibleMarkers.get(key).remove();
//        }
//
//        visibleMarkers.clear();

        //This is the current user-viewable region of the map
        LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;

        // Conver measurment json to class.
        //arrMeas = MeasurementJsonToArray(arrMeasurements);

        //Loop through all the items that are available to be placed on the map
        for(Measurement currMeas : this.Measurements)
        {
            //JSONObject currMeas = null;
            //try {
            //currMeas = arrMeasurements.getJSONObject(i);

            // Get the measurement id
            //int measID = Integer.parseInt(currMeas.getString("id"));

            // Taking only the last measurement
            //if( i == (arrMeasurements.length() - 1) || currMeas.Id == 428 || currMeas.Id == 429)
            //{
               // int rainPower = currMeas.getInt("rain_power");

                // If we received different rain strength, remove previous markers
               // if( rainPower != tmpPrevRainStrength && measID != 428 && measID != 429)
               // {
               //     mMap.clear();
               //     visibleMarkers.clear();
               //     tmpPrevRainStrength = rainPower;
               // }

                // Getting measurement data
                //double x_coord = Double.parseDouble(currMeas.getString("x_coordinate"));
                //double y_coord = Double.parseDouble(currMeas.getString("y_coordinate"));

                //If the item is within the the bounds of the screen
                if(bounds.contains(new LatLng(currMeas.YCoord, currMeas.XCoord)))
                {
                    //If the item isn't already being displayed
                    if(this.visibleMarkers.containsKey(currMeas.Id)) {
                        visibleMarkersNew.put(currMeas.Id, this.visibleMarkers.get(currMeas.Id));
                        this.visibleMarkers.remove(currMeas.Id);
                    }
                    else {
                        //Add the Marker to the Map and keep track of it with the HashMap
                        //getMarkerForItem just returns a MarkerOptions object
                        currMeas.Marker = this.mMap.addMarker(getMarkerForMeasurement(currMeas));
                        //this.visibleMarkers.put(measID, this.mMap.addMarker(getMarkerForMeasurement(currMeas)));
                        visibleMarkersNew.put(currMeas.Id, currMeas);

                        // Create mesaurment data hash table TODO: לבדוק האם זה הדרך הנכונה
                    }
                }

                // Only if we need to show notification
                if ((showNotif) && (!dispNotif) && (this.CheckDist(mUserLoc, currMeas.XCoord, currMeas.YCoord)))
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

//                //If the marker is off screen
//                else
//                {
//                    //If the course was previously on screen
//                    if(visibleMarkers.containsKey(currMeas.Id))
//                    {
//                        //1. Remove the Marker from the GoogleMap
//                        visibleMarkers.get(currMeas.Id).Marker.remove();
//
//                        //2. Remove the reference to the Marker from the HashMap
//                        visibleMarkers.remove(currMeas.Id);
//                    }
//                }
            //}
            //} catch (JSONException e) {
            //    e.printStackTrace();
           // }
        }

        // Delete all unrelvent marker from screen.
        for (Measurement delMeas : this.visibleMarkers.values()) {
            delMeas.Marker.remove();
        }

        // Save new measurment data.
        this.visibleMarkers = visibleMarkersNew;

        // Execute the thread again because we want realtime data
        //new ReadServerData().execute(urlString);
    }

// TODO:  מה זה?
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

    private MarkerOptions getMarkerForMeasurement(Measurement meas) {
        // Getting measurement data
        //double x_coord = Double.parseDouble(objMeasurement.getString("x_coordinate"));
        //double y_coord = Double.parseDouble(objMeasurement.getString("y_coordinate"));

        // Build the marker title
        //String markerTitle = "Measurement Details";

        // Set snippet according to rain strength
        //String snippet = "";

        // Holds the marker icon to place on the map
        BitmapDescriptor markerIcon = null;

        //String dateTime = objMeasurement.getString("datetime");
        //if(dateTime != "null") {
//             markerTitle = markerTitle + "Date: " + dateTime + '\n';
        //}

        //String MeasId = objMeasurement.getString("id");

        //if (!objMeasurement.isNull("rain_power")) {
            //int rainPower = objMeasurement.getInt("rain_power");

            // Set marker icon according to the rain power
        if (meas.RainPower == 1) {
            markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.weak_rain);
        } else if (meas.RainPower == 2) {
            markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.strong_rain);
        } else {
            markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.sunny);
        }

//                // If the icon is still initial, give it sunny value
//                if (markerIcon == null) {
//                    markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.sunny);
//                }
        //String temperature = objMeasurement.getString("temperature");
        //if(meas.Temperature != "null") {

            // Convert to numeric value
        //int numericTemperature = (int)(meas.Temperature);
//                    markerTitle = markerTitle + "Temperature: " + numericTemperature + "\n";
        //}

        //String humidity = objMeasurement.getString("humidity");
        //if(humidity != "null") {
//                    markerTitle = markerTitle + "Humidity: " + humidity + "\n";
        //}

        //String sea_level = objMeasurement.getString("sea_level");
        //if(sea_level != "null") {
//                    markerTitle = markerTitle + "Sea Level: " + sea_level + "\n";
        //}

        //String air_pollution = objMeasurement.getString("air_pollution");
        //if(air_pollution != "null") {
//                    markerTitle = markerTitle + "Air Pollution: " + air_pollution + "\n";
        //}
        // TODO: לבדוק שבאמת לא צריך פה כלום
        // Create coordinate object
        LatLng collegeMgmtCoords = new LatLng(meas.YCoord, meas.XCoord);

        // Add a marker in the coordinates
        return (new MarkerOptions().position(collegeMgmtCoords).icon(markerIcon)
                .snippet(Integer.toString(meas.Id)));
        //.title(markerTitle)
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

    private ArrayList<Measurement> MeasurementJsonToArray(JSONArray measJson){
        ArrayList<Measurement> measList = new ArrayList<>();

        int measId = 0, measRainPower = 0, sourceType = 0;
        double measTemperature = 0, measHumidity = 0, xCoord = 0, yCoord = 0, seaLevel = 0, airPollution = 0;

        for(int i = 0; i < measJson.length(); i++)
        {
            JSONObject currMeas = null;

            try {
                currMeas = measJson.getJSONObject(i);

                measId = currMeas.getInt("id");
                sourceType = currMeas.getInt("source_type");
                yCoord = currMeas.getDouble("y_coordinate");
                xCoord = currMeas.getDouble("x_coordinate");

                if (!currMeas.isNull("rain_power"))
                    measRainPower = currMeas.getInt("rain_power");

                if (!currMeas.isNull("temperature"))
                    measTemperature = currMeas.getDouble("temperature");

                if (!currMeas.isNull("humidity"))
                    measHumidity = currMeas.getDouble("humidity");

                if (!currMeas.isNull("sea_level"))
                    seaLevel = currMeas.getDouble("sea_level");

                if (!currMeas.isNull("air_pollution"))
                    airPollution = currMeas.getDouble("air_pollution");
                //TODO: GET ALL MEASURMENT.

            } catch (JSONException e) {
                continue;
            }

            measList.add(new Measurement(measId, sourceType, xCoord, yCoord, measRainPower, measTemperature,
                                         measHumidity, airPollution, seaLevel, null));
        }

        return measList;
    }

    private void handlePermission() {
        // Request location permission if not have yet.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    10);
        }
    }

    @Override
    public View getInfoContents(Marker marker) {

        Measurement selMeas = visibleMarkers.get(Integer.decode(marker.getSnippet()));
        String rain = "";

        if (selMeas.RainPower == 1) {
            rain = "Weak Rain";
        } else if (selMeas.RainPower == 2) {
            rain = "Strong Rain";
        } else {
            rain = "No Rain Detected";
        }

        if (selMeas.SourceType == 1) {
            ((ImageView)this.infoWindow.findViewById(R.id.badge)).setImageResource(R.drawable.ic_new_umbrella);
        } else if (selMeas.SourceType == 2) {
            ((ImageView)this.infoWindow.findViewById(R.id.badge)).setImageResource(R.drawable.ic_vehicle);
        }

        String temp = Double.toString(selMeas.Temperature) + "°";
        String humidity = Double.toString(selMeas.Humidity) + "%";
        String seaLevel = Double.toString(selMeas.SeaLevel);
        ((TextView)this.infoWindow.findViewById(R.id.txtRain)).setText(rain);
        ((TextView)this.infoWindow.findViewById(R.id.txtTemp)).setText(temp);
        ((TextView)this.infoWindow.findViewById(R.id.txtHumidity)).setText(humidity);
        ((TextView)this.infoWindow.findViewById(R.id.txtSeaLevel)).setText(seaLevel);

        return this.infoWindow;
    }

    // TODO: מתושה שאמורה להקפיץ התרעה
//    private void RaiseNotif() {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.rain)
//                        .setContentTitle("It's raining around you")
//                        .setContentText("Don't forget WeBrella!");
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        mNotificationManager.notify(1, mBuilder.build());
//    }
}
