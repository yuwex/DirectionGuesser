package com.example.directionguesser

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // view setup
        val scoresGrid = findViewById<GridLayout>(R.id.scoreGrid)
        var idx = 0
        var inflater = LayoutInflater.from(this)

        val game = Game()
        var firebase = FirebaseDatabase.getInstance()

        var landmarks = game.getLandmarks()
        for (landmark in landmarks) {
            Log.d("MainActivity", "$landmark")
            val gridElement = inflater.inflate(R.layout.grid_location, scoresGrid, false)
            val landmarkTitle = gridElement.findViewById<TextView>(R.id.landmarkTitle)
            val landmarkHighScore = gridElement.findViewById<TextView>(R.id.landmarkHighScore)

            val reference = firebase.getReference(landmark.name)
            reference.get().addOnSuccessListener {
                snapshot ->
                if (!snapshot.exists()) {
                    landmarkHighScore.text = "No scores yet!"
                } else {
                    val scoreData = snapshot.getValue<Score>()
                    val username = scoreData?.username
                    val score = scoreData?.score

                    if (username == null || score == null) {
                        landmarkHighScore.text = "Error retrieving score."
                    } else {
                        landmarkHighScore.text = "$username: $score"
                    }
                }
            }
            landmarkTitle.text = landmark.name

            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(idx / 4)
                columnSpec = GridLayout.spec(idx % 4)
            }
            idx++
            scoresGrid.addView(gridElement, params)
        }

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }
}