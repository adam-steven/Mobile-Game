package com.example.cmp309spacegame;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

//this is a pop up view that takes in  some text
public class TopScoreDialogActivity extends AppCompatDialogFragment {
    private EditText name;
    private TopScoreDialogListner listner;
    private int score;

    TopScoreDialogActivity(int score)
    {
        this.score = score;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.MyDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.activity_top_score_dialog, null);

        name =  view.findViewById(R.id.name);
        TextView scoreText = view.findViewById(R.id.score);
        //sets the score text view the the last games score
        scoreText.setText("YOUR SCORE: " + score);

        //builds the pop up with these attributes
        builder.setView(view)
                .setTitle("NEW TOP 500")
                .setPositiveButton("submit", new DialogInterface.OnClickListener() { //checks to see the submit button was pressed
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = name.getText().toString();
                        if(!username.isEmpty()) //checks to see if the edit text is not empty
                            listner.appyText(username, score);
                        else //if no username was given the pop up will re-open
                            listner.appyText( "no value", score);
                    }
                })
                .setNegativeButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //checks to see the delete button was pressed
                        listner.appyText("not added", 0);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listner = (TopScoreDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement TopScoreDialogListner");
        }
    }

    public interface TopScoreDialogListner
    {
        //this allows the MainActivity to see what information was entered
        void appyText(String username, int scoreGiven);
    }
}
