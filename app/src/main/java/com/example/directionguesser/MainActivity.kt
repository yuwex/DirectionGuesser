package com.example.directionguesser

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {

    // store per-username permissions as local persistent data?

    var username = "guest"
    lateinit var usernameEditText : EditText
    var firebase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.play)
        val scoreButton = findViewById<Button>(R.id.scores)
        val usernameEditText = findViewById<EditText>(R.id.username)

        playButton.setOnClickListener {
            username = usernameEditText.text.toString()
            if (username != "guest") {
                var reference: DatabaseReference = firebase.getReference(username)

                // do something, or maybe do this in gameactivity
            }

            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        scoreButton.setOnClickListener {
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)
        }
    }


}