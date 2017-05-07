package com.example.barmen.myapplication;

import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsPopup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_popup);

        SharedPreferences load = getSharedPreferences("setting", 0);

        ((Switch)this.findViewById(R.id.switch_rain_notif)).setChecked(load.getBoolean("raise_notif", false));
        ((SeekBar)this.findViewById(R.id.seekBar)).setProgress(load.getInt("distance_notif", 4));
        setSeekBarText(((SeekBar)this.findViewById(R.id.seekBar)).getProgress());

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        HandleSeekBar();

        getWindow().setLayout((int)(dm.widthPixels * 0.8), (int)(dm.heightPixels * 0.5));
    }

    private void HandleSeekBar(){
        SeekBar seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                int Value = (progresValue + 1) * 200;
                ((TextView) findViewById(R.id.seekBarText)).setText(Integer.toString(Value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences save = getSharedPreferences("setting", 0);

                save.edit().putInt("distance_notif", seekBar.getProgress()).commit();
            }
        });

        Switch switch1 = (Switch) findViewById(R.id.switch_rain_notif);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences save = getSharedPreferences("setting", 0);

                if (isChecked) {
                    save.edit().putBoolean("raise_notif", true).commit();
                } else {
                    save.edit().putBoolean("raise_notif", false).commit();
                }
            }
        });
    }

    private void setSeekBarText(int progresValue){
        int Value = (progresValue + 1) * 200;
        ((TextView) findViewById(R.id.seekBarText)).setText(Integer.toString(Value));
    }
}
