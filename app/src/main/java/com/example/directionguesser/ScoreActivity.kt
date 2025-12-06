package com.example.directionguesser

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        val backButton = findViewById<Button>(R.id.backButton)
        val scoresTable = findViewById<TableLayout>(R.id.scoresTable)

        backButton.setOnClickListener {
            finish()
        }
    }
}