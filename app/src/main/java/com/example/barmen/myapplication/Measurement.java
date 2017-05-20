package com.example.barmen.myapplication;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Barmen on 06/04/2017.
 */

class Measurement {

    public int Id;
    public int SourceType;
    public double XCoord;
    public double YCoord;
    public int RainPower;
    public double Temperature;
    public double Humidity;
    public double SeaLevel;
    public double AirPollution;
    public String Date;
    public String Time;
    public Marker Marker;

    Measurement(int id, int sourceType, double xCoord, double yCoord, int rainPower, double temperature, double humidity,
                double airPollution, double seaLevel, String Date, String Time, Marker marker) {

        this.Id = id;
        this.SourceType = sourceType;
        this.XCoord = xCoord;
        this.YCoord = yCoord;
        this.RainPower = rainPower;
        this.Temperature = temperature;
        this.Humidity = humidity;
        this.AirPollution = airPollution;
        this.SeaLevel = seaLevel;
        this.Date = Date;
        this.Time = Time;
        this.Marker = marker;
    }
}
