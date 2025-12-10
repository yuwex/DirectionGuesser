package com.example.directionguesser

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // View setup
        val scoresGrid = findViewById<GridLayout>(R.id.scoreGrid)
        val spinner: Spinner = findViewById(R.id.landmarkSpinner)
        val backButton = findViewById<Button>(R.id.backButton)

        val inflater = LayoutInflater.from(this)
        val game = Game()
        val firebase = FirebaseDatabase.getInstance()
        val defaultLandmarks = game.getDefaultLandmarks()

        val names = ArrayList<String>()
        defaultLandmarks.forEach { (name, _, _) -> names.add(name) }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        fun loadSingleScore(selectedName: String) {
            scoresGrid.removeAllViews()

            val gridElement = inflater.inflate(R.layout.grid_location, scoresGrid, false)
            val landmarkTitle = gridElement.findViewById<TextView>(R.id.landmarkTitle)
            val landmarkHighScore = gridElement.findViewById<TextView>(R.id.landmarkHighScore)

            landmarkTitle.text = selectedName
            landmarkHighScore.text = "Loading..."

            // Fetch data from Firebase
            val reference = firebase.getReference(selectedName)
            reference.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    landmarkHighScore.text = "No scores yet!"
                } else {
                    val scoreData = snapshot.getValue<Score>()
                    val username = scoreData?.username
                    val score = scoreData?.score

                    if (username == null || score == null) {
                        landmarkHighScore.text = "Error retrieving score."
                    } else {
                        // Display the score (format: "User: 100")
                        landmarkHighScore.text = "$username: $score"
                    }
                }
            }.addOnFailureListener {
                landmarkHighScore.text = "Connection Error"
            }

            scoresGrid.addView(gridElement)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLandmarkName = names[position]
                loadSingleScore(selectedLandmarkName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}