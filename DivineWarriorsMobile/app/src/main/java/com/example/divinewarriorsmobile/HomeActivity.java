package com.example.divinewarriorsmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Button easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> startGame(1));


        Button mediumButton = findViewById(R.id.mediumButton);
        mediumButton.setOnClickListener(v -> startGame(2));


        Button impossibleButton = findViewById(R.id.impossibleButton);
        impossibleButton.setOnClickListener(v -> startGame(3));
    }

    private void startGame(int numPolice) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("numPolice", numPolice); // Pass number of police to the game
        startActivity(intent);
    }
}
