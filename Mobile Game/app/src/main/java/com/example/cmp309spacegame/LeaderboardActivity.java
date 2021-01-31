package com.example.cmp309spacegame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this shows all the stored firebase data in an organised table
public class LeaderboardActivity extends AppCompatActivity {
    TextView rank, name, score;
    Button back;
    List<String> usernames;
    List<Integer> userscores;
    DatabaseReference reff;
    int i = 0;
    SoundPool soundPool;
    int buttonSound;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        //sets the activity to full screen, removing the overhead UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rank= findViewById(R.id.r1);
        name= findViewById(R.id.u1);
        score= findViewById(R.id.s1);

        usernames = new ArrayList<>();
        userscores = new ArrayList<>();

        //get the firebase collection "users"
        reff = FirebaseDatabase.getInstance().getReference().child("__");
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //adds all current data in the collection to a hash map
                Map<String, Object> databaseItems = (HashMap<String,Object>) dataSnapshot.getValue();
                assert databaseItems != null;
                List<Object> values = new ArrayList<>(databaseItems.values());
                i = 0;

                //check if the stored object stores score first
                boolean scoreFirst = values.get(0).toString().contains("{score");
                if(scoreFirst)
                    scoreFirst(values);
                else
                    nameFirst(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

        //initialized the sound for the back button (prefs is to get the volume)
        prefs = getSharedPreferences("cmp309game", Context.MODE_PRIVATE);
        initializeSounds();

        //if the back button is pressed play a sound and close the activity
        back= findViewById(R.id.leave);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound, prefs.getInt("volume", 100)/100f, prefs.getInt("volume", 100)/100f,0,0,1);
                finish();
            }
        });
    }

    //process the data based on the score being first
    private void scoreFirst(List<Object> values)
    {
        StringBuilder valueStripped = new StringBuilder("null");

        //go through the list of objects stripped unnecessary character and adding them to a string ({score=0, name=NAME} to 0 NAME)
        for(Object value : values) {
            String currentValue = value.toString();

            currentValue = removeWord(", name=", currentValue );
            currentValue = removeWord("score=", currentValue );
            currentValue = currentValue.substring(1);
            currentValue = currentValue.substring(0, currentValue.length() - 1);

            valueStripped.append(currentValue);
        }

        //split the string into an array using spaces
        String[] arr = valueStripped.toString().split(" ");
        int indexPosition = 0;

        for (String ss : arr) {
            if(i != 0) {
                if (i % 2 != 0) { //check if the current entry is odd (score)
                    indexPosition = 0;
                    for (Integer scores : userscores) { //organize the scores with the highest number first
                        if (Integer.parseInt(ss) < scores)
                            indexPosition++;
                    }
                    //add the score to a list based on were it should go
                    userscores.add(indexPosition, Integer.parseInt(ss));
                } else
                    //add the username to a list based on were the score went in its list
                    usernames.add(indexPosition, ss);
            }
            i++;
        }

        sendToView();
    }

    //process the data based on the name being first
    private void nameFirst(List<Object> values)
    {
        StringBuilder valueStripped = new StringBuilder("null");
        String currentname = "";

        //go through the list of objects stripped unnecessary character and adding them to a string ({name=NAME, score=0} to NAME 0)
        for(Object value : values) {
            String currentValue = value.toString();

            currentValue = removeWord(", score=", currentValue );
            currentValue = removeWord("name=", currentValue );
            currentValue = currentValue.substring(1);
            currentValue = currentValue.substring(0, currentValue.length() - 1);

            valueStripped.append(currentValue);
        }

        //split the string into an array using spaces
        String[] arr = valueStripped.toString().split(" ");
        int indexPosition;

        for (String ss : arr) {
            if(i != 0) {
                if (i % 2 == 0) { //check if the current entry is ever (score)
                    indexPosition = 0;
                    for (Integer scores : userscores) {//organize the scores with the highest number first
                        if (Integer.parseInt(ss) < scores)
                            indexPosition++;
                    }
                    //add the score to a list based on were it should go
                    userscores.add(indexPosition, Integer.parseInt(ss));
                    //add the username to a list based on were the score went in its list
                    usernames.add(indexPosition, currentname);
                } else
                    //store the name until it corresponding score is processed
                    currentname = ss;
            }
            i++;
        }

        sendToView();
    }

    //set the userscores and usernames lists to the activity's text views
    private void sendToView()
    {
        i = 1;
        StringBuilder userranksString = new StringBuilder();
        StringBuilder usernamesString = new StringBuilder();
        StringBuilder userscoresString = new StringBuilder();

        for(Integer scores: userscores) {
            userranksString.append("\n").append(i).append(".\n");
            userscoresString.append("\n").append(scores).append("\n");
            i++;
        }

        for(String names: usernames)
            usernamesString.append("\n").append(names).append("\n");

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        rank.setText(userranksString.toString());
        name.setText(usernamesString.toString());
        score.setText(userscoresString.toString());
    }

    private static String removeWord(String wordToRemove, String fullWord)
    {
        if (fullWord.contains(wordToRemove))
            fullWord = fullWord.replaceAll(wordToRemove , " ");

        return fullWord;
    }

    //creates a new sound pool instance a initializes one audio file for later use
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
