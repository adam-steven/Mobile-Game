package com.example.cmp309spacegame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this is the start screen, it also checks if the gets a to 500 score and sets an alarm to notify the player
public class MainActivity extends AppCompatActivity implements TopScoreDialogActivity.TopScoreDialogListner {
    Button startButton, leaderboardButton,settingsButton;
    DatabaseReference reff;
    Users user;
    int LAUNCH_SECOND_ACTIVITY = 1;
    int i = 0;
    int lastScore = 0;
    List<Integer> userscores;
    String lowestEntry = "";
    TextView hightscoreText;
    SharedPreferences prefs;
    ConnectivityManager connectivityManager;
    boolean isConnected;
    SoundPool soundPool;
    int buttonSound;
    float volume;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sets the activity to full screen, removing the overhead UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //initialized the sound for the buttons
        initializeSounds();

        //when clicked a sound plays the button is disabled and a GameActivity is started
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound, volume, volume,0,0,1);
                disableButtons();
                Intent intent= new Intent(MainActivity.this, GameActivity.class);
                startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
            }
        });

        //when clicked a sound plays, the connection is tested and if the connection is fine the button is disabled and a LeaderboardActivity is started
        leaderboardButton = findViewById(R.id.leaderboardButton);
        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert connectivityManager != null;
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                isConnected = (networkInfo != null && networkInfo.isConnected());
                soundPool.play(buttonSound, volume, volume,0,0,1);

                if(isConnected) {
                    disableButtons();
                    Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(MainActivity.this, "No Connection", Toast.LENGTH_LONG).show();
            }
        });

        //when clicked a sound plays the button is disabled and a SettingsActivity is started
        settingsButton = findViewById(R.id.optionsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound, volume, volume,0,0,1);
                disableButtons();
                Intent intent= new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //displays the locally stored high score
        hightscoreText = findViewById(R.id.highscoreText);
        prefs = getSharedPreferences("cmp309game", Context.MODE_PRIVATE);
        hightscoreText.setText("HighScore : " + prefs.getInt("highscore", 0));

        //initializes calender for the next day at 12 o'clock
        if(prefs.getBoolean("notifications", true)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            startAlarm(calendar);
        }
        else
            cancleOldAlarm();

        volume = prefs.getInt("volume", 100)/100f;
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

    //disables the buttons so they cant be double tapped
    private void disableButtons()
    {
        startButton.setEnabled(false);
        leaderboardButton.setEnabled(false);
        settingsButton.setEnabled(false);
    }

    //enables the buttons and updates the volume
    @Override
    protected void onResume() {
        super.onResume();
        volume = prefs.getInt("volume", 100)/100f;
        startButton.setEnabled(true);
        leaderboardButton.setEnabled(true);
        settingsButton.setEnabled(true);

    }

    //releases the sound pool as it is no longer being used
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    //sets an alarm notification for the next day at 12 o'clock
    private void startAlarm(Calendar calendar)
    {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        assert alarmManager != null;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    //cancels the set alarm
    private void cancleOldAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

    //gets the results back for the game
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LAUNCH_SECOND_ACTIVITY)
        {
            //checks if the game finish properly (the activity returned a score)
            if(resultCode == Activity.RESULT_OK)
            {
                //update the high score text
                hightscoreText.setText("HighScore : " + prefs.getInt("highscore", 0));
                assert data != null;
                String result=data.getStringExtra("lastScore");
                assert result != null;
                lastScore = Integer.parseInt(result);
                testScore();
            }
            //checks if the game finished early
            if(resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(MainActivity.this, "Game Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    //checks to see if the players last score made it to the top 500 (the firebase scores)
    private void testScore()
    {
        //verifies that there is a connection before continuing
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo != null && networkInfo.isConnected());
        if(!isConnected)
            return;

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

                //check if the stored object stores score first
                boolean scoreFirst = values.get(0).toString().contains("{score");
                if(scoreFirst)
                    scoreFirst(values);
                else
                    nameFirst(values);

                //gets the lowest score from the userscores list
                int minIndex = userscores.indexOf(Collections.min(userscores));

                //checks to see if the games score is better than the lowest score
                if (lastScore > userscores.get(minIndex)) {

                    //gets the document name that holds the lowest score
                    List<String> listOfDocs = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : databaseItems.entrySet()) {
                        listOfDocs.add(entry.getKey());
                    }
                    lowestEntry = listOfDocs.get(minIndex);

                    //shows the TopScoreDialogActivity
                    TopScoreDialogActivity topScoreDialogActivity = new TopScoreDialogActivity(lastScore);
                    topScoreDialogActivity.show(getSupportFragmentManager(), "top score");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });
    }

    //process the information returned form the TopScoreDialogActivity
    @Override
    public void appyText(String username, int scoreGiven) {
        //check to see if the user discarded the score
        if(username.equals("not added"))
        {
            Toast.makeText(MainActivity.this, "Score Not Added", Toast.LENGTH_LONG).show();
            return;
        }

        //check to see if the user did not enter a name, if so show the TopScoreDialogActivity again
        if(username.equals("no value"))
        {
            Toast.makeText(MainActivity.this, "Enter A Name", Toast.LENGTH_LONG).show();
            TopScoreDialogActivity topScoreDialogActivity = new TopScoreDialogActivity(scoreGiven);
            topScoreDialogActivity.show(getSupportFragmentManager(), "top score");
            return;
        }

        user = new Users();
        reff = FirebaseDatabase.getInstance().getReference().child("__");

        user.setName(username);
        user.setScore(scoreGiven);

        //remove the current lowest entry from the firebase
        //IMPROVEMENTS: the lowestEntry should be checked to make sure it still exist
        reff.child(lowestEntry).removeValue();
        //add the score to the firebase
        reff.push().setValue(user);

        Toast.makeText(MainActivity.this, "Score Added", Toast.LENGTH_LONG).show();
    }

    private void scoreFirst(List<Object> values)
    {
        StringBuilder valueStripped = new StringBuilder("null");
        i = 0;

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
        for (String ss : arr) {
            if (i != 0) {
                if (i % 2 != 0) {
                    //add the scores to a list
                    userscores.add(Integer.parseInt(ss));
                }
            }
            i++;
        }
    }

    private void nameFirst(List<Object> values)
    {
        StringBuilder valueStripped = new StringBuilder("null");
        i = 0;
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
        for (String ss : arr) {
            if (i != 0) {
                if (i % 2 == 0) {
                    //add the scores to a list
                    userscores.add(Integer.parseInt(ss));
                }
            }
            i++;
        }
    }

    private static String removeWord(String wordToRemove, String fullWord)
    {
        if (fullWord.contains(wordToRemove))
            fullWord = fullWord.replaceAll(wordToRemove , " ");

        return fullWord;
    }

}
