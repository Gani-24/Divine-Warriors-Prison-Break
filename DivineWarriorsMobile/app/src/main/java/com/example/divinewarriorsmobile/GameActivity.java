package com.example.divinewarriorsmobile;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private MediaPlayer backgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music);
        backgroundMusic.setLooping(true); // Loop the music continuously// Ensure the layout is linked correctly

        // Retrieve the number of police based on difficulty
        int numPolice = getIntent().getIntExtra("numPolice", 3); // Default to 3 police if not passed

        // Reference to the FrameLayout container for GameView
        FrameLayout gameViewContainer = findViewById(R.id.gameViewContainer);

        // Initialize and add the GameView with the specified number of police
        gameView = new GameView(this, numPolice);
        gameViewContainer.addView(gameView);

        // Reference to the directional buttons
        Button buttonUp = findViewById(R.id.buttonUp);
        Button buttonDown = findViewById(R.id.buttonDown);
        Button buttonLeft = findViewById(R.id.buttonLeft);
        Button buttonRight = findViewById(R.id.buttonRight);

        // Set up button listeners for movement
        buttonUp.setOnClickListener(v -> movePlayer(0, -1));
        buttonDown.setOnClickListener(v -> movePlayer(0, 1));
        buttonLeft.setOnClickListener(v -> movePlayer(-1, 0));
        buttonRight.setOnClickListener(v -> movePlayer(1, 0));
    }

    private void movePlayer(int dx, int dy) {
        if (gameView != null) {
            gameView.movePlayer(dx, dy);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }
}
