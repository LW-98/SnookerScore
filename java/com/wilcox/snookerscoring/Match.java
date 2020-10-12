package com.wilcox.snookerscoring;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Match extends AppCompatActivity implements DialogInterface.OnClickListener {

    private String userID;
    private ArrayList<String> nameList = new ArrayList<>();

    private Button undo, redo, endGame, foul, miss, switchPlayer, red, yellow, green, brown, blue, pink, black;
    private TextView playerOneName, playerTwoName, playerOneScore, playerTwoScore, playerOnePointer, playerTwoPointer;
    private int currentPlayer = 1, missesPlayerOne, missesPlayerTwo, shotsPlayerOne, shotsPlayerTwo, foulsPlayerOne = 0, foulsPlayerTwo = 0, redBallsPotted, playerPottedRed = 0, lastPot = 0, endShot = 0, pointsLeft = 147, playerOnePots = 0, playerTwoPots = 0, playerOneBigBreak = 0, playerTwoBigBreak = 0;
    private double playerOnePotPercentage = 0, playerTwoPotPercentage = 0, playerOneSafetyPercentage = 0, playerTwoSafetyPercentage = 0;
    private boolean lastStretch = false;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private Stack<Pair> shotStack, redoStack;
    private ArrayList<Integer> playerOneShotHistory = new ArrayList<>(), playerTwoShotHistory = new ArrayList<>(), shotHistory = new ArrayList<>();
    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_screen);

        // Initialise link to objects from xml file
        playerOneName = findViewById(R.id.playerOneName);
        playerTwoName = findViewById(R.id.playerTwoName);
        playerOneScore = findViewById(R.id.playerOneScore);
        playerTwoScore = findViewById(R.id.playerTwoScore);
        red = findViewById(R.id.redBall);
        yellow = findViewById(R.id.yellowBall);
        green = findViewById(R.id.greenBall);
        brown = findViewById(R.id.brownBall);
        blue = findViewById(R.id.blueBall);
        pink = findViewById(R.id.pinkBall);
        black = findViewById(R.id.blackBall);
        switchPlayer = findViewById(R.id.switchPlayerButton);
        miss = findViewById(R.id.missButton);
        undo = findViewById(R.id.undoButton);
        redo = findViewById(R.id.redoButton);
        foul = findViewById(R.id.foulButton);
        endGame = findViewById(R.id.endGameButton);
        playerOnePointer = findViewById(R.id.playerOnePointer);
        playerTwoPointer = findViewById(R.id.playerTwoPointer);
        mainToolbar = findViewById(R.id.accountToolbar);

        redoStack = new Stack();
        shotStack = new Stack();
        missesPlayerOne = 0;
        missesPlayerTwo = 0;
        redBallsPotted = 0;

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Match");

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userID = firebaseAuth.getUid();

        disableColours();

        // Gets current list of players for user to choose from and sets the name of both players
        DocumentReference docRef = firestore.collection("Users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get map from database and store names in an ArrayList
                        Map tempMap = document.getData();
                        Map newMap = (Map) tempMap.get("players");
                        if (newMap == null)
                            newMap = new HashMap();
                        for (Object key : newMap.keySet()) {
                            nameList.add((String) key);
                            System.out.println((String) key);
                        }
                        // Convert ArrayList to Array
                        String[] arr = nameList.toArray(new String[nameList.size()]);

                        AlertDialog.Builder playerSelect = new AlertDialog.Builder(Match.this);
                        playerSelect.setCancelable(false);
                        playerSelect.setTitle("Select Opponent");
                        playerSelect.setSingleChoiceItems(arr, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String mPlayerOneName = task.getResult().getString("name");
                                playerOneName.setText(mPlayerOneName.substring(0, mPlayerOneName.indexOf(" ")));
                                playerTwoName.setText(nameList.get(which));
                                dialog.dismiss();
                            }
                        });

                        playerSelect.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Match.this, Dashboard.class));
                            }
                        });

                        AlertDialog playerSelectDialog = playerSelect.create();
                        playerSelectDialog.show();
                    } else {
                    }
                } else {
                    Toast.makeText(Match.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Undo function
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shotStack.empty()) {
                    Toast.makeText(Match.this, "Can't undo further", Toast.LENGTH_LONG).show();
                } else {
                    Pair<Integer, Integer> pair = shotStack.pop();
                    if (pair.second == 0 || pair.second == 8) {
                        if (endShot > 0) {
                            if (endShot == 2 && (Integer) shotStack.peek().second == 1) {
                                endShot = 1;
                                lastStretch = false;
                            } else {
                            }
                        }
                        if (pair.first == 1) {
                            if (pair.second == 0)
                                missesPlayerOne--;
                            shotsPlayerOne--;
                            changePointer();
                            currentPlayer = 1;
                        } else {
                            if (pair.second == 0)
                                missesPlayerTwo--;
                            shotsPlayerTwo--;
                            changePointer();
                            currentPlayer = 2;
                        }
                    } else {
                        if (endShot != 0 && pair.second > 0)
                            endShot--;
                        else if (endShot == 2 && pair.second < 0 && (Integer) shotStack.peek().second == 1)
                            endShot--;
                        if (pair.second == 1) {
                            if (redBallsPotted == 15) {
                                lastStretch = false;
                                endShot = 0;
                                playerPottedRed = 0;
                            }
                            redBallsPotted--;
                        }
                        if (pair.first == 1) {
                            shotsPlayerOne--;
                            if (pair.second < 0) {
                                missesPlayerOne--;
                                playerTwoScore.setText(String.valueOf(Integer.parseInt(playerTwoScore.getText().toString()) + pair.second));
                                changePointer();
                                currentPlayer = 1;
                            } else
                                playerOneScore.setText(String.valueOf(Integer.parseInt(playerOneScore.getText().toString()) - pair.second));
                            playerOneShotHistory.remove(playerOneShotHistory.size() - 1);
                            if (currentPlayer == 2) {
                                changePointer();
                            }
                            initRecyclerView(1);
                        } else {
                            shotsPlayerTwo--;
                            if (pair.second < 0) {
                                missesPlayerTwo--;
                                playerOneScore.setText(String.valueOf(Integer.parseInt(playerOneScore.getText().toString()) + pair.second));
                                changePointer();
                                currentPlayer = 2;
                            } else
                                playerTwoScore.setText(String.valueOf(Integer.parseInt(playerTwoScore.getText().toString()) - pair.second));
                            playerTwoShotHistory.remove(playerTwoShotHistory.size() - 1);
                            if (currentPlayer == 1) {
                                changePointer();
                            }
                            initRecyclerView(2);
                        }
                        shotHistory.remove(shotHistory.size() - 1);
                    }
                    redoStack.push(pair);
                }
                updateColours();
            }
        });

        // Redo function
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (redoStack.empty()) {
                    Toast.makeText(Match.this, "Can't redo further", Toast.LENGTH_LONG).show();
                } else {
                    Pair<Integer, Integer> pair = redoStack.pop();
                    if (pair.second == 0 || pair.second == 8) {
                        if (endShot == 1)
                            endShot++;
                        if (pair.second == 0)
                            switchPlayer(true);
                        else
                            switchPlayer(false);
                    } else {
                        if (pair.second == 1)
                            scoreRed();
                        if (pair.first == 1) {
                            if (pair.second < 0) {
                                foulScore(pair.second);
                            } else {
                                currentPlayer = 1;
                                score(pair.second);
                            }
                        } else {
                            if (pair.second < 0) {
                                foulScore(pair.second);
                            } else {
                                currentPlayer = 2;
                                score(pair.second);
                            }
                        }
                        shotHistory.add(pair.second);
                    }
                }
            }
        });

        // This is called when foul button is pressed
        foul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                foul();
            }
        });

        // Functions to change the player whose turn it is
        switchPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (endShot == 1)
                    endShot++;
                switchPlayer(false);
            }
        });
        miss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (endShot == 1)
                    endShot++;
                switchPlayer(true);
            }
        });

        // Functions that call the score() method with its corresponding value i.e. red = 1 point
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                scoreRed();
                score(1);
            }
        });
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    yellow.setEnabled(false);
                score(2);
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    green.setEnabled(false);
                score(3);
            }
        });
        brown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    brown.setEnabled(false);
                score(4);
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    blue.setEnabled(false);
                score(5);
            }
        });
        pink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    pink.setEnabled(false);
                score(6);
            }
        });
        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoStack.clear();
                if (lastStretch)
                    black.setEnabled(false);
                score(7);
            }
        });

        // This is called when 'End Game' button is pressed
        endGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog();

            }
        });
    }

    // This is called when menu icon is selected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.match_menu, menu);
        return true;
    }

    public void scoreRed() {
        redBallsPotted++;
        if (redBallsPotted == 15) {
            endShot = 1;
            disableRed();
            playerPottedRed = currentPlayer;
        }
    }

    // This is called when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.basics:
                new AlertDialog.Builder(this)
                        .setNegativeButton("Back to Game", null)
                        .setTitle("Basics")
                        .setMessage(R.string.rules_basics)
                        .show();
                return true;
            case R.id.scoring_rules:
                new AlertDialog.Builder(this)
                        .setNegativeButton("Back to Game", null)
                        .setTitle("Scoring")
                        .setMessage(R.string.rules_scoring)
                        .show();
                return true;
            case R.id.fouls_rules:
                new AlertDialog.Builder(this)
                        .setNegativeButton("Back to Game", null)
                        .setTitle("Fouls")
                        .setMessage(R.string.rules_fouls)
                        .show();
                return true;
            case R.id.all_rules:
                Uri uriUrl = Uri.parse("https://www.wpbsa.com/wp-content/uploads/2016/03/official-rules-of-the-game.pdf");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
                return true;
            case R.id.stats:
                updateStats();
                return true;
            default:
                return false;
        }
    }

    public void saveDialog() {
        new AlertDialog.Builder(this).setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Save match")
                .setMessage("Do you want to save this game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveGame();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    // Create dialog when user tries to quit, decreases user error
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exiting match")
                .setMessage("Are you sure you want to go to the dashboard? Match data will be lost")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Changes where the pointer is pointing, only needed for undo() method
    public void changePointer() {
        if (currentPlayer == 1) {
            playerOnePointer.setTextColor(ContextCompat.getColor(Match.this, R.color.colorPrimary));
            playerTwoPointer.setTextColor(Color.WHITE);
        } else {
            playerTwoPointer.setTextColor(ContextCompat.getColor(Match.this, R.color.colorPrimary));
            playerOnePointer.setTextColor(Color.WHITE);
        }
    }

    // Method that switches whose turn it is, the value miss keeps track of if it was intentional or not (for statistical purposes)
    // 0 on shotStack means miss, 8 on shotStack means safety
    public void switchPlayer(boolean miss) {
        if (currentPlayer == 1) {
            if (miss) {
                missesPlayerOne++;
                shotStack.add(new Pair<>(1, 0));
            } else {
                shotStack.add(new Pair<>(1, 8));
            }
            changePointer();
            currentPlayer = 2;
            shotsPlayerOne++;
        } else if (currentPlayer == 2) {
            if (miss) {
                missesPlayerTwo++;
                shotStack.add(new Pair<>(2, 0));
            } else {
                shotStack.add(new Pair<>(2, 8));
            }
            changePointer();
            currentPlayer = 1;
            shotsPlayerTwo++;
        }
        if (redBallsPotted != 15)
            disableColours();
        else
            updateColours();
    }

    // Score method that adds to the score of whoever currentPlayer is set as (1 or 2)
    public void score(int x) {
        lastPot = x;
        if (currentPlayer == 1) {
            if (x > 0) {
                shotsPlayerOne++;
                playerOneScore.setText(String.valueOf(Integer.parseInt(playerOneScore.getText().toString()) + x));
                shotStack.push(new Pair<>(1, x));
                playerOneShotHistory.add(x);
            } else {
                playerOneScore.setText(String.valueOf(Integer.parseInt(playerOneScore.getText().toString()) - x));
                shotStack.push(new Pair<>(2, x));
            }
            initRecyclerView(1);
        } else {
            if (x > 0) {
                shotsPlayerTwo++;
                playerTwoScore.setText(String.valueOf(Integer.parseInt(playerTwoScore.getText().toString()) + x));
                shotStack.push(new Pair<>(2, x));
                playerTwoShotHistory.add(x);
            } else {
                playerTwoScore.setText(String.valueOf(Integer.parseInt(playerTwoScore.getText().toString()) - x));
                shotStack.push(new Pair<>(1, x));
            }
            initRecyclerView(2);
        }
        shotHistory.add(x);
        if ((lastStretch || endShot == 1) && shotHistory.get(shotHistory.size() - 1) != 1) {
            endShot++;
        }
        updateColours();
    }

    // This method is called when a user selects a foul through the popup menu i.e. foul(), or when a foul is redone
    private void foulScore(int x) {
        shotStack.add(new Pair(currentPlayer, x));
        shotHistory.add(x);
        if (!lastStretch)
            disableColours();
        else if (endShot == 1)
            endShot++;
        switch (currentPlayer) {
            case 1:
                playerOneShotHistory.add(x);
                playerTwoScore.setText(String.valueOf(Integer.parseInt(playerTwoScore.getText().toString()) - x));
                shotsPlayerOne++;
                missesPlayerOne++;
                initRecyclerView(1);
                changePointer();
                currentPlayer = 2;
                break;
            case 2:
                playerTwoShotHistory.add(x);
                playerOneScore.setText(String.valueOf(Integer.parseInt(playerOneScore.getText().toString()) - x));
                shotsPlayerTwo++;
                missesPlayerTwo++;
                initRecyclerView(2);
                changePointer();
                currentPlayer = 1;
                break;
        }
        updateColours();
    }

    // This method presents the popup dialog when the foul button is pressed
    private void foul() {
        final String[] arr = {"4", "5", "6", "7"};
        AlertDialog.Builder foulSelect = new AlertDialog.Builder(Match.this);
        foulSelect.setCancelable(true);
        foulSelect.setTitle("Select Foul");
        foulSelect.setSingleChoiceItems(arr, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                foulScore(Integer.parseInt(arr[which]) * -1);
                dialog.dismiss();
            }
        });
        foulSelect.setNegativeButton("Cancel", null);

        AlertDialog playerSelectDialog = foulSelect.create();
        playerSelectDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    // Update live shot history for whatever player is passed in (1 or 2)
    private void initRecyclerView(int player) {
        RecyclerView recyclerView;
        LiveShotHistoryAdapter adapter;
        if (player == 1) {
            recyclerView = findViewById(R.id.playerOneShotHistory);
            adapter = new LiveShotHistoryAdapter(playerOneShotHistory, this);
        } else {
            recyclerView = findViewById(R.id.playerTwoShotHistory);
            adapter = new LiveShotHistoryAdapter(playerTwoShotHistory, this);
        }
        recyclerView.setAdapter(adapter);
        // Set the layout equal to a grid that is 6 elements wide
        recyclerView.setLayoutManager(new GridLayoutManager(this, 6));
    }

    // Grey out colours
    private void disableColours() {
        yellow.setEnabled(false);
        green.setEnabled(false);
        brown.setEnabled(false);
        blue.setEnabled(false);
        pink.setEnabled(false);
        black.setEnabled(false);
    }

    // Make colours available
    private void enableColours() {
        yellow.setEnabled(true);
        green.setEnabled(true);
        brown.setEnabled(true);
        blue.setEnabled(true);
        pink.setEnabled(true);
        black.setEnabled(true);
    }

    private void disableRed() {
        red.setEnabled(false);
    }

    private void updateColours() {

        // If stack is empty, enable only red as it means its the start of the game
        if (shotStack.isEmpty()) {
            disableColours();
            red.setEnabled(true);
        } else {
            if (redBallsPotted == 15) {
                if (playerPottedRed == currentPlayer && (Integer) shotStack.peek().second != 1) {
                    lastStretch = true;
                }
                disableRed();
            } else {
                lastStretch = false;
                endShot = 0;
                red.setEnabled(true);
            }

            // Handles greying out of balls when in last stage of game
            if (endShot == 1) {
                enableColours();
                lastStretch = true;
            } else {
                if (lastStretch && endShot != 0) {
                    disableColours();
                    switch (endShot) {
                        case 9:
                            saveDialog();
                            break;
                        case 8:
                            if (playerOneScore.getText().equals(playerTwoScore.getText())) {
                                black.setEnabled(true);
                            } else {
                                saveDialog();
                            }
                            break;
                        case 7:
                            black.setEnabled(true);
                            break;
                        case 6:
                            pink.setEnabled(true);
                            break;
                        case 5:
                            blue.setEnabled(true);
                            break;
                        case 4:
                            brown.setEnabled(true);
                            break;
                        case 3:
                            green.setEnabled(true);
                            break;
                        case 2:
                            yellow.setEnabled(true);
                            break;
                    }
                }
            }

            // Handles greying out of balls in the main stage of the game
            if (!shotStack.isEmpty() && lastStretch == false) {
                if ((Integer) shotStack.peek().second != 1) {
                    if (currentPlayer == playerPottedRed)
                        enableColours();
                    else
                        disableColours();
                } else {
                    enableColours();
                }
            }
        }
    }

    // Save game to the database
    private void saveGame() {
        calculateStats();
        Map map = new HashMap();
        map.put("name", playerTwoName.getText().toString());
        map.put("fouls", foulsPlayerOne+":"+foulsPlayerTwo);
        map.put("score", playerOneScore.getText().toString() + ":" + playerTwoScore.getText().toString());
        map.put("break", playerOneBigBreak + ":" + playerTwoBigBreak);
        map.put("misses", missesPlayerOne + ":" + missesPlayerTwo);
        map.put("pots", playerOnePots + ":" + playerTwoPots);
        map.put("shots", shotsPlayerOne + ":" + shotsPlayerTwo);
        map.put("potPercentage", playerOnePotPercentage + ":" + playerTwoPotPercentage);
        map.put("safetyPercentage", playerOneSafetyPercentage + ":" + playerTwoSafetyPercentage);
        // Gets reference to database
        DocumentReference docRef = firestore.collection("Users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map tempMap = document.getData();
                        Map newMap = (Map) tempMap.get("players");
                        // Get the map that is currently in the database so I can update it
                        Map temp = (Map) newMap.get(playerTwoName.getText());
                        // Check if win or loss
                        if (Integer.parseInt((String) playerOneScore.getText()) > Integer.parseInt((String) playerTwoScore.getText()))
                            temp.put("Wins", ((Long) temp.get("Wins")) + 1);
                        else
                            temp.put("Losses", ((Long) temp.get("Losses")) + 1);

                        newMap.put(playerTwoName.getText(), temp);
                        tempMap.put("players", newMap);
                        firestore.collection("Users").document(userID)
                                .set(tempMap);
                        startActivity(new Intent(Match.this, Dashboard.class));
                    } else {
                    }
                } else {
                    Toast.makeText(Match.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                }
            }
        });
        docRef.update("MatchHistory", FieldValue.arrayUnion(map));
    }

    private void calculateStats() {

        // Calculate biggest break
        int playerOneMax = 0;
        int playerTwoMax = 0;
        int playerOneTemp = 0;
        int playerTwoTemp = 0;
        Stack<Pair> tempStack = (Stack<Pair>) shotStack.clone();

        if (!shotStack.isEmpty()) {
            do {
                Pair<Integer, Integer> x = tempStack.pop();
                if (x.first == 1) {
                    if (x.second <= 0 || x.second == 8 || tempStack.isEmpty() || (Integer) tempStack.peek().first == 2) {
                        if (tempStack.isEmpty() || (Integer) tempStack.peek().first == 2) {
                            playerOneTemp += x.second;
                        }
                        if (playerOneTemp > playerOneMax) {
                            playerOneMax = playerOneTemp;
                            playerOneTemp = 0;
                        }
                    } else {
                        playerOneTemp += x.second;
                    }
                } else {
                    if (x.second <= 0 || x.second == 8 || tempStack.isEmpty() || (Integer) tempStack.peek().first == 1) {
                        if (tempStack.isEmpty() || (Integer) tempStack.peek().first == 1) {
                            playerTwoTemp += x.second;
                        }
                        if (playerTwoTemp > playerTwoMax) {
                            playerTwoMax = playerTwoTemp;
                            playerTwoTemp = 0;
                        }
                    } else {
                        playerTwoTemp += x.second;
                    }
                }
            } while (!tempStack.isEmpty());
        }
        playerOneBigBreak = playerOneMax;
        playerTwoBigBreak = playerTwoMax;

        // Calculate points left
        pointsLeft = 147;
        if (lastStretch) {
            pointsLeft = 27;
            switch (endShot) {
                case 8:
                    pointsLeft -= 7;
                case 7:
                    pointsLeft -= 6;
                case 6:
                    pointsLeft -= 5;
                case 5:
                    pointsLeft -= 4;
                case 4:
                    pointsLeft -= 3;
                case 3:
                    pointsLeft -= 2;
            }
        } else {
            for (int x : shotHistory) {
                pointsLeft = (8 * (15 - redBallsPotted)) + 27;
            }
        }

        // Calculate number of balls potted
        playerOnePots = 0;
        playerTwoPots = 0;
        foulsPlayerOne = 0;
        foulsPlayerTwo = 0;
        for (Pair<Integer, Integer> x : shotStack) {
            if (x.second > 0 && x.second < 8) {
                switch (x.first) {
                    case 1:
                        playerOnePots++;
                        break;
                    case 2:
                        playerTwoPots++;
                        break;
                }
            }
            if (x.second < 0) {
                switch (x.first) {
                    case 1:
                        foulsPlayerOne++;
                        break;
                    case 2:
                        foulsPlayerTwo++;
                        break;
                }
            }
        }

        // Calculate safety percentage
        if (shotStack.size() >= 2) {
            int playerOneSafeties = shotsPlayerOne - (missesPlayerOne + playerOnePots);
            int playerTwoSafeties = shotsPlayerTwo - (missesPlayerTwo + playerTwoPots);
            int playerOneSuccessfulSafeties = 0;
            int playerTwoSuccessfulSafeties = 0;

            tempStack = (Stack<Pair>) shotStack.clone();
            Pair<Integer, Integer> pair = tempStack.pop();
            Pair<Integer, Integer> firstpair = tempStack.pop();
            if (pair.second == 8) {
                switch (pair.first) {
                    case 1:
                        playerOneSafeties--;
                        break;
                    case 2:
                        playerTwoSafeties--;
                        break;
                }
            }
            while (true) {
                if (firstpair.second == 8) {
                    if (pair.second < 1) {
                        switch (firstpair.first) {
                            case 1:
                                playerOneSuccessfulSafeties++;
                                break;
                            case 2:
                                playerTwoSuccessfulSafeties++;
                                break;
                        }
                    } else {
                    }
                } else {
                }
                if (tempStack.isEmpty()) {
                    break;
                }
                pair = firstpair;
                firstpair = tempStack.pop();
            }
            if (playerOneSafeties != 0)
                playerOneSafetyPercentage = ((double) playerOneSuccessfulSafeties / playerOneSafeties) * 100;
            else
                playerOneSafetyPercentage = 0;
            if (playerTwoSafeties != 0)
                playerTwoSafetyPercentage = ((double) playerTwoSuccessfulSafeties / playerTwoSafeties) * 100;
            else
                playerTwoSafetyPercentage = 0;
        }

        // Calculate pot percentages
        if (shotsPlayerOne != 0)
            //playerOnePotPercentage = (1 - ((double) missesPlayerOne / shotsPlayerOne)) * 100;
            playerOnePotPercentage = ((double) playerOnePots / shotsPlayerOne) * 100;
        else
            playerOnePotPercentage = 0;

        if (shotsPlayerTwo != 0)
            //playerTwoPotPercentage = (1 - ((double) missesPlayerTwo / shotsPlayerTwo)) * 100;
            playerTwoPotPercentage = ((double) playerTwoPots / shotsPlayerTwo) * 100;
        else
            playerTwoPotPercentage = 0;
    }

    // Method to add stats to a list so they can be shown to the user
    private void updateStats() {
        calculateStats();
        List<String> list = new ArrayList();
        for (int i = 1; i < 28; i++) {
            switch (i) {
                case 2:
                    list.add("Points Left: " + pointsLeft);
                    break;
                case 4:
                    list.add(playerOneName.getText().toString());
                    break;
                case 6:
                    list.add(playerTwoName.getText().toString());
                    break;
                case 7:
                    list.add(String.valueOf(playerOneBigBreak));
                    break;
                case 8:
                    list.add("Max Break");
                    break;
                case 9:
                    list.add(String.valueOf(playerTwoBigBreak));
                    break;
                case 10:
                    list.add(String.valueOf(foulsPlayerOne));
                    break;
                case 11:
                    list.add("Fouls");
                    break;
                case 12:
                    list.add(String.valueOf(foulsPlayerTwo));
                    break;
                case 13:
                    list.add(String.valueOf(missesPlayerOne));
                    break;
                case 14:
                    list.add("Misses");
                    break;
                case 15:
                    list.add(String.valueOf(missesPlayerTwo));
                    break;
                case 16:
                    list.add(String.valueOf(playerOnePots));
                    break;
                case 17:
                    list.add("Pots");
                    break;
                case 18:
                    list.add(String.valueOf(playerTwoPots));
                    break;
                case 19:
                    list.add(String.valueOf(shotsPlayerOne));
                    break;
                case 20:
                    list.add("Shots");
                    break;
                case 21:
                    list.add(String.valueOf(shotsPlayerTwo));
                    break;
                case 22:
                    list.add(String.format("%.1f", playerOnePotPercentage) + "%");
                    break;
                case 23:
                    list.add("Pot Percentage");
                    break;
                case 24:
                    list.add(String.format("%.1f", playerTwoPotPercentage) + "%");
                    break;
                case 25:
                    list.add(String.format("%.1f", playerOneSafetyPercentage) + "%");
                    break;
                case 26:
                    list.add("Safety Percentage");
                    break;
                case 27:
                    list.add(String.format("%.1f", playerTwoSafetyPercentage) + "%");
                    break;
                default:
                    list.add("");
            }
        }

        GridView grid = new GridView(this);
        grid.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
        grid.setNumColumns(3);

        new AlertDialog.Builder(this)
                .setNegativeButton("Back to Game", null)
                .setTitle("Match Statistics")
                .setView(grid)
                .show();
    }
}
