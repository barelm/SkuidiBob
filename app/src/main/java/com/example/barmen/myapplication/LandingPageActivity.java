package com.example.barmen.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class LandingPageActivity extends Activity {

    // Time to launch the maps activity
    private static int LANDING_PAGE_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // Define the changing pictures
        final int[] imageArray = { R.drawable.webrella_banner_grey_long_1, R.drawable.webrella_banner_grey_long_2, R.drawable.webrella_banner_grey_long_3};

        // Get the image view of the activity
        final ImageView activity_landing_page_imageview = (ImageView) findViewById(R.id.activity_landing_page_imageview);

        // Define a handler for changing pictures
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;
            int iteration_number = 0;

            public void run() {
                int interval;

                activity_landing_page_imageview.setImageResource(imageArray[i]);
                i++;
                iteration_number++;
                if (i > imageArray.length - 1) {
                    i = 0;
                }

                // Make the pictures changing faster during the time
//                if (iteration_number <= 2) {
//                    interval = 350;
//                }
//                else if (iteration_number <= 5) {
//                    interval = 225;
//                }
//                else {
//                    interval = 100;
//                }

                interval = 200;

                // Set timeout for interval between images
                handler.removeCallbacks(this);
                handler.postDelayed(this, interval);
            }
        };

        // Set timeout for initial delay
        handler.postDelayed(runnable, 0);

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
