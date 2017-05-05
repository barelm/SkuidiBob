package com.example.barmen.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.android.gms.location.LocationServices;

/**
 * Created by Barmen on 03/04/2017.
 */

public class ReportPopup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels * 0.8), (int)(dm.heightPixels * 0.3));
    }

    public void onReportSunny(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);
    }

    public void onReportWeakRain(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);

    }

    public void onReportStrongRain(View view) {

        // Get the current user location
        Location userLoc = LocationServices.FusedLocationApi.getLastLocation(
                MapsActivity.mGoogleApiClient);

    }
}


