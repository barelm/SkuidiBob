package com.example.barmen.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;

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

        getWindow().setLayout((int)(dm.widthPixels * 0.8), (int)(dm.heightPixels * 0.6));
    }

    public void onWeakRain(View view) {
        int x = 200;

        x = 300;
    }

    public void onHeavyRain(View view) {
        int x = 200;

        x = 300;
    }

    public void onSnow(View view) {
        int x = 200;

        x = 300;
    }
}


