package com.example.cmp309spacegame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.WindowManager;

//this initializes the custom game view and the games sound pull
public class GameActivity extends AppCompatActivity {

    private GameController gameController;
    SoundPool soundPool;
    int pointSound,deathSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //sets the activity to full screen, removing the overhead UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //gets the size of the screen
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        //initializes the game sounds
        initializeSounds();
        //initializes the game view
        gameController = new GameController(this, point.x, point.y, soundPool, pointSound,deathSound);
        setContentView(gameController);
    }

    //since the game controller is not an activity onPause() and onResume() need to update the view here.
    @Override
    protected void onPause() {
        super.onPause();
        gameController.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameController.resume();
    }

    //releases the sound pool as it is no longer being used
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    //creates a new sound pool instance a initializes two audio files for later use
    private void initializeSounds()
    {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(20)
                .setAudioAttributes(audioAttributes)
                .build();

        pointSound = soundPool.load(this, R.raw.point, 1);
        deathSound = soundPool.load(this, R.raw.death, 1);
    }

}
