package com.example.divinewarriorsmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private Paint paint;
    private SurfaceHolder holder;

    // Battlefield size
    private final int numRows = 10;
    private final int numCols = 10;
    private final int tileSize = 100; // Size of each grid cell

    // Player position
    private int playerRow = 9;
    private int playerCol = 0;
    private final int exitRow = 0; // Top row
    private final int exitCol = numCols - 1; // Rightmost column


    // Police positions
    private List<int[]> policePositions; // Each police is represented as {row, col}
    private int numPolice; // Dynamic number of police officers based on difficulty

    // Obstacles
    private List<Rect> obstacles;

    private int health = 3; // Start with 3 hearts
    private int score = 0;  // Initial score
    private Bitmap heartBitmap;

    // Police movement timer
    private long lastPoliceMoveTime;
    private final long policeMoveDelay = 500; // Police moves every 500ms

    // Bitmaps for background, player, police, and obstacle
    private Bitmap backgroundBitmap;
    private Bitmap playerBitmap;
    private Bitmap policeBitmap;
    private Bitmap rockBitmap;
    private Bitmap exitBitmap;

    public GameView(Context context, int difficultyLevel) {
        super(context);
        holder = getHolder();
        paint = new Paint();

        // Set the number of police based on difficulty
        switch (difficultyLevel) {
            case 1: // Easy
                numPolice = 1;
                break;
            case 2: // Medium
                numPolice = 3;
                break;
            case 3: // Impossible
                numPolice = 4;
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level: " + difficultyLevel);
        }

        // Load bitmaps
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.prison_background);
        playerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.inmate);
        policeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.police);
        rockBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rock);
        exitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exit);
        heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart);


        // Initialize obstacles and police
        obstacles = new ArrayList<>();
        createObstacles();
        initializePolicePositions();
    }

    private void createObstacles() {
        obstacles = new ArrayList<>();

        // Blocking direct path from side to top
        // Add more obstacles to the game
        obstacles.add(new Rect(0 * tileSize, 4 * tileSize, 1 * tileSize, 5 * tileSize)); // Vertical obstacle at the left
        obstacles.add(new Rect(4 * tileSize, 5 * tileSize, 5 * tileSize, 6 * tileSize)); // Horizontal obstacle
        obstacles.add(new Rect(6 * tileSize, 2 * tileSize, 7 * tileSize, 3 * tileSize)); // Small horizontal obstacle
        obstacles.add(new Rect(2 * tileSize, 5 * tileSize, 3 * tileSize, 6 * tileSize)); // Vertical obstacle
        obstacles.add(new Rect(7 * tileSize, 5 * tileSize, 8 * tileSize, 6 * tileSize)); // Another vertical obstacle
        obstacles.add(new Rect(0 * tileSize, 7 * tileSize, 1 * tileSize, 8 * tileSize)); // Vertical obstacle at the bottom left
        obstacles.add(new Rect(4 * tileSize, 8 * tileSize, 5 * tileSize, 9 * tileSize)); // Horizontal obstacle near the bottom
        obstacles.add(new Rect(7 * tileSize, 0 * tileSize, 8 * tileSize, 1 * tileSize)); // Horizontal obstacle at the top right
        obstacles.add(new Rect(2 * tileSize, 6 * tileSize, 3 * tileSize, 7 * tileSize)); // Another vertical obstacle
        obstacles.add(new Rect(5 * tileSize, 1 * tileSize, 6 * tileSize, 2 * tileSize)); // Small horizontal obstacle
        obstacles.add(new Rect(3 * tileSize, 5 * tileSize, 4 * tileSize, 6 * tileSize)); // Another small vertical obstacle
        obstacles.add(new Rect(6 * tileSize, 6 * tileSize, 7 * tileSize, 7 * tileSize)); // Another small obstacle
        obstacles.add(new Rect(8 * tileSize, 6 * tileSize, 9 * tileSize, 7 * tileSize)); // Horizontal obstacle near the bottom right

    }

    private void initializePolicePositions() {
        policePositions = new ArrayList<>();
        for (int i = 0; i < numPolice; i++) {
            // Place police at random initial positions
            int row = (int) (Math.random() * numRows);
            int col = (int) (Math.random() * numCols);

            // Ensure police do not spawn at the player's starting position or on obstacles
            while ((row == playerRow && col == playerCol) || isObstacle(row, col)) {
                row = (int) (Math.random() * numRows);
                col = (int) (Math.random() * numCols);
            }

            policePositions.add(new int[]{row, col});
        }
    }

    private boolean isObstacle(int row, int col) {
        int left = col * tileSize;
        int top = row * tileSize;
        int right = left + tileSize;
        int bottom = top + tileSize;

        Rect position = new Rect(left, top, right, bottom);
        for (Rect obstacle : obstacles) {
            if (Rect.intersects(obstacle, position)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }


    private void update() {
        // Check if the player has reached the exit
        if (playerRow == exitRow && playerCol == exitCol) {
            isPlaying = false;
            saveHighScore();
            showVictoryDialog();
            return; // Exit the update loop
        }

        // Move the police randomly at intervals
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPoliceMoveTime > policeMoveDelay) {
            movePolice();
            lastPoliceMoveTime = currentTime;
        }

        // Check collision between player and any police
        for (int[] police : policePositions) {
            if (playerRow == police[0] && playerCol == police[1]) {
                health--; // Decrease health

                if (health <= 0) {
                    isPlaying = false; // Stop the game
                    saveHighScore();
                    showGameOverDialog(); // Show Game Over dialog
                } else {
                    // Reset positions after losing a heart
                    resetPositions();
                }
                break; // Exit the loop after registering the hit
            }
        }
    }

    private void resetPositions() {
        // Reset player to the starting position
        playerRow = 9;
        playerCol = 0;

        // Reinitialize police positions
        initializePolicePositions();
    }

    private void drawHearts(Canvas canvas) {
        for (int i = 0; i < health; i++) {
            canvas.drawBitmap(heartBitmap, 10 + (i * 50), 10, null);
        }
    }

    private void drawScore(Canvas canvas) {
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        canvas.drawText("Score: " + score, getWidth() - 300, 50, paint);
    }



    private void movePolice() {
        for (int[] police : policePositions) {
            int policeRow = police[0];
            int policeCol = police[1];

            int newPoliceRow = policeRow;
            int newPoliceCol = policeCol;

            // Prefer moving towards the player
            if (Math.random() < 0.7) { // 70% chance to move toward the player
                if (playerRow < policeRow) newPoliceRow--;
                else if (playerRow > policeRow) newPoliceRow++;

                if (playerCol < policeCol) newPoliceCol--;
                else if (playerCol > policeCol) newPoliceCol++;
            } else { // Random movement for unpredictability
                int direction = (int) (Math.random() * 4);
                switch (direction) {
                    case 0: newPoliceRow--; break; // Up
                    case 1: newPoliceRow++; break; // Down
                    case 2: newPoliceCol--; break; // Left
                    case 3: newPoliceCol++; break; // Right
                }
            }

            // Check boundaries
            newPoliceRow = Math.max(0, Math.min(numRows - 1, newPoliceRow));
            newPoliceCol = Math.max(0, Math.min(numCols - 1, newPoliceCol));

            // Check for collisions with obstacles
            if (!isObstacle(newPoliceRow, newPoliceCol)) {
                police[0] = newPoliceRow;
                police[1] = newPoliceCol;
            }
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // Draw the background, restricted to the grid area
            Rect sourceRect = new Rect(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
            Rect destRect = new Rect(0, 0, numCols * tileSize, numRows * tileSize);
            canvas.drawBitmap(backgroundBitmap, sourceRect, destRect, null);

            // Draw obstacles
            for (Rect obstacle : obstacles) {
                canvas.drawBitmap(rockBitmap, null, obstacle, null);
            }
            int exitLeft = exitCol * tileSize;
            int exitTop = exitRow * tileSize;
            canvas.drawBitmap(exitBitmap, null,
                    new Rect(exitLeft, exitTop, exitLeft + tileSize, exitTop + tileSize), null);

            // Draw the player
            int playerLeft = playerCol * tileSize;
            int playerTop = playerRow * tileSize;
            canvas.drawBitmap(playerBitmap, null,
                    new Rect(playerLeft, playerTop, playerLeft + tileSize, playerTop + tileSize), null);

            // Draw the police
            for (int[] police : policePositions) {
                int policeLeft = police[1] * tileSize;
                int policeTop = police[0] * tileSize;
                canvas.drawBitmap(policeBitmap, null,
                        new Rect(policeLeft, policeTop, policeLeft + tileSize, policeTop + tileSize), null);
            }

            drawHearts(canvas);
            drawScore(canvas);



            holder.unlockCanvasAndPost(canvas);
        }
    }
    private void saveHighScore() {
        SharedPreferences prefs = getContext().getSharedPreferences("HighScores", Context.MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        if (score > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", score);
            editor.apply();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(16); // Approx 60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void movePlayer(int dx, int dy) {
        int newRow = playerRow + dy;
        int newCol = playerCol + dx;

        // Check if the new position is valid and not an obstacle
        if (!isObstacle(newRow, newCol)) {
            // Ensure the player stays within bounds
            int boundedRow = Math.max(0, Math.min(numRows - 1, newRow));
            int boundedCol = Math.max(0, Math.min(numCols - 1, newCol));

            // Move the player only if they change position
            if (boundedRow != playerRow || boundedCol != playerCol) {
                playerRow = boundedRow;
                playerCol = boundedCol;
                score += 10; // Increment score only if the move was successful
            }
        }
    }

    private void showGameOverDialog() {
        ((GameActivity) getContext()).runOnUiThread(() -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Game Over")
                    .setMessage("You were caught by the police!")
                    .setPositiveButton("Restart", (dialog, which) -> restartGame())
                    .setCancelable(false)
                    .show();
        });
    }
    private void showVictoryDialog() {
        ((GameActivity) getContext()).runOnUiThread(() -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Victory!")
                    .setMessage("You escaped the prison!")
                    .setPositiveButton("Restart", (dialog, which) -> restartGame())
                    .setCancelable(false)
                    .show();
        });
    }

    private void restartGame() {
        // Stop the current game thread if it's running
        if (gameThread != null && gameThread.isAlive()) {
            isPlaying = false; // Signal the thread to stop
            try {
                gameThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        playerRow = 9;
        playerCol = 0;


        initializePolicePositions();


        score = 0;


        health = 3;

        // Start a new game thread
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}