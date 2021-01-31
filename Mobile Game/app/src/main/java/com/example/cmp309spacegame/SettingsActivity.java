package com.example.cmp309spacegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

//this view allows the user to edit some of the devices stored data related to the app
public class SettingsActivity extends AppCompatActivity {
    SharedPreferences prefs;
    Switch controlsSwitch, notificationSwitch;
    SeekBar sensitivity, volume;
    SoundPool soundPool;
    int buttonSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //sets the activity to full screen, removing the overhead UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        prefs = getSharedPreferences("cmp309game", Context.MODE_PRIVATE);
        volume =findViewById(R.id.volumeBar);
        controlsSwitch =  findViewById(R.id.controlsSwitch);
        sensitivity =findViewById(R.id.controlsBar);
        notificationSwitch =  findViewById(R.id.notificationSwitch);

        //initializes the game sounds
        initializeSounds();

        setSettings();

        //when pressed, make a sound and reset all the locally stored settings
        Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound, 1, 1,0,0,1);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putInt("volume", volume.getMax());
                editor.putBoolean("motionControls", false);
                editor.putInt("motionSensitivity", sensitivity.getMax()/2);
                editor.putBoolean("notifications", true);

                editor.apply();

                setSettings();
            }
        });

        //when pressed, make a sound save all the changes to the locally stored settings and end the activity
        Button saveButton = findViewById(R.id.leave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound, volume.getProgress()/100f, volume.getProgress()/100f,0,0,1);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putInt("volume", volume.getProgress());
                editor.putBoolean("motionControls", controlsSwitch.isChecked());
                editor.putInt("motionSensitivity", sensitivity.getProgress());
                editor.putBoolean("notifications", notificationSwitch.isChecked());

                editor.apply();

                finish();
            }
        });
    }

    //update the activity's UI
    private void setSettings()
    {
        volume.setProgress(prefs.getInt("volume", volume.getMax()));
        controlsSwitch.setChecked(prefs.getBoolean("motionControls", false));
        sensitivity.setProgress(prefs.getInt("motionSensitivity", sensitivity.getMax()/2));
        notificationSwitch.setChecked(prefs.getBoolean("notifications", true));
    }

    //creates a new sound pool instance a initializes two audio files for later use
    private void initializeSounds()
    {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        buttonSound = soundPool.load(this, R.raw.buttonclick, 1);
    }

    //releases the sound pool as it is no longer being used
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}
