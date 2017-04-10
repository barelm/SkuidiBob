package com.example.barmen.myapplication;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Barmen on 06/04/2017.
 */

class Measurement {

    public int Id;
    public double XCoord;
    public double YCoord;
    public int RainPower;
    public double Temperature;
    public double Humidity;
    public double SeaLevel;
    public double AirPollution;
    public Marker Marker;

    Measurement(int id, double xCoord, double yCoord, int rainPower, double temperature, double humidity,
                double airPollution, double seaLevel, Marker marker) {

        this.Id = id;
        this.XCoord = xCoord;
        this.YCoord = yCoord;
        this.RainPower = rainPower;
        this.Temperature = temperature;
        this.Humidity = humidity;
        this.AirPollution = airPollution;
        this.SeaLevel = seaLevel;
        this.Marker = marker;
    }
}
