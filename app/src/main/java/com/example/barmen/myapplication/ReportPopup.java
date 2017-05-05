package com.example.barmen.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by Barmen on 03/04/2017.
 */

public class ReportPopup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.report_popup);

        SharedPreferences load = getSharedPreferences("setting", 0);
        ((CheckBox)this.findViewById(R.id.cb_raise_notif)).setChecked(load.getBoolean("raise_notif", false));

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels * 0.8), (int)(dm.heightPixels * 0.6));
    }

    public void onNotifClicked(View view) {
        SharedPreferences save = getSharedPreferences("setting", 0);



        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cb_raise_notif:
                if (((CheckBox) view).isChecked()) {
                    save.edit().putBoolean("raise_notif", true).commit();
                }else {
                    save.edit().putBoolean("raise_notif", false).commit();
                }
        }

    }

}


