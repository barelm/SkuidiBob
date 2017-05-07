package com.example.barmen.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by Barmen on 03/04/2017.
 */

public class ReportPopup extends Activity {

    public Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels * 0.8), (int)(dm.heightPixels * 0.3));

        this.myIntent = getIntent();
    }

    public void onReportSunny(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);

        new AsyncPostRequest().execute(userLoc.getLatitude(), userLoc.getLongitude(), 0.0);
    }

    public void onReportWeakRain(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);

        new AsyncPostRequest().execute(userLoc.getLatitude(), userLoc.getLongitude(), 1.0);
    }

    public void onReportStrongRain(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);

        new AsyncPostRequest().execute(userLoc.getLatitude(), userLoc.getLongitude(), 2.0);
    }

    private class AsyncPostRequest extends AsyncTask<Double, Void, Integer> {

        @Override
        protected Integer doInBackground(Double... params) {

            Integer responseCode = 0;

            try {
                URL url = new URL(MapsActivity.urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.addRequestProperty("Accept", "application/json");
                httpURLConnection.addRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                // Get X and Y coordinates
                double y_coord = params[0];
                double x_coord = params[1];
                int rain_power = params[2].intValue();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("source_id", 2);
                jsonObject.put("x_coordinate", x_coord);
                jsonObject.put("y_coordinate", y_coord);
                jsonObject.put("rain_power", rain_power);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(jsonObject.toString());
                wr.flush();
                wr.close();

                Log.d("MSG", httpURLConnection.getResponseMessage());

                responseCode = httpURLConnection.getResponseCode();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode){

            String result;

            // Return to the main activity with the appropriate result code
            if(responseCode == 200)
            {
                setResult(Activity.RESULT_OK, myIntent);
            }
            else
            {
                setResult(404, myIntent);
            }

            // Close the report popup
            finish();
        }
    }
}





