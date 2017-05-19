package com.example.barmen.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LandingPageActivity extends Activity {

    // Time to launch the maps activity
    private static int LANDING_PAGE_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // Launch the maps activity after the timeout
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent mainIntent = new Intent(LandingPageActivity.this, MapsActivity.class);
                LandingPageActivity.this.startActivity(mainIntent);
                LandingPageActivity.this.finish();
            }
        }, LANDING_PAGE_TIME_OUT);
    }
}
