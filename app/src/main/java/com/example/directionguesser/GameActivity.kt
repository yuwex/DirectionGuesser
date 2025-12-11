package com.example.directionguesser

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.internal.ads.zzads
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import java.text.NumberFormat
import kotlin.math.max
import kotlin.math.min


class GameActivity : AppCompatActivity(), SensorEventListener  {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var currentLocation: Location? = null

    lateinit var sensorManager: SensorManager
    var accelerometer: Sensor? = null
    var magnetometer: Sensor? = null

    var accelerometerCoordinates = FloatArray(3)
    var magnetometerCoordinates = FloatArray(3)

    lateinit var guessButton: Button
    lateinit var locationText: TextView
    lateinit var locationImg: ImageView

    var game = Game()

    lateinit var compassImage: ImageView
    var compassDegree = 0f;
    private lateinit var adView : AdView


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

//        testGame()

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager


        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        compassImage = findViewById(R.id.compass)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.w("MainActivity", "Missing Permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                1001
            )

            // TODO: Have to handle if the user says "NO"
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { l -> currentLocation = l }

        handleAds()

        val homeButton = findViewById<Button>(R.id.home)
        val scoreButton = findViewById<Button>(R.id.scores)
        guessButton = findViewById<Button>(R.id.guess)
        locationImg = findViewById<ImageView>(R.id.locationPicture)
        locationText = findViewById<TextView>(R.id.locationText)

        scoreButton.setOnClickListener {
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)
        }

        game.newGame(3)
        updateView()
        guessButton.setOnClickListener {
            var direction = Math.toDegrees(compassDegree.toDouble()).toFloat()
            if (direction < 0) direction += 360f

            val landmark = game.getCurrentLandmark()
            val score = game.guess(direction, currentLocation!!)

            Log.w("MainActivity", "Got click with landmark ${landmark?.name}")

            if (score != null && landmark != null) {
                Log.w("MainActivity", "Processing click. Accuracy: ${game.formatScore(score)}")

                Toast.makeText(this, "Accuracy: ${game.formatScore(score)}", Toast.LENGTH_SHORT).show()

                val progressBar = findViewById<ProgressBar>(R.id.scoreBar)
                val animation = ObjectAnimator.ofInt(progressBar,
                    "progress", progressBar.progress, (score * 100).toInt())
                animation.duration = 100
                animation.interpolator = AccelerateInterpolator()
                animation.start()

                // Toast.makeText(this, "Accuracy: ${game.formatScore(score)}", Toast.LENGTH_LONG).show()

                // Local saving
                val keyLandmarkHS = "hs_${landmark.name}"
                val keyLandmarkPlays = "plays_${landmark.name}"
                val pref = this.getSharedPreferences("${this.packageName}_preferences", MODE_PRIVATE)
                val bestLevelScore = pref.getFloat(keyLandmarkHS, 0f)
                pref.edit(commit=true) {
                    if (score > bestLevelScore) {
                        putFloat(keyLandmarkHS, score)
                    }
                    putInt(keyLandmarkPlays, pref.getInt(keyLandmarkPlays, 0) + 1)
                }

                // Firebase saving
                val firebase = FirebaseDatabase.getInstance()
                val reference = firebase.getReference(landmark.name)
                reference.get().addOnSuccessListener { snapshot ->
                    val highScore = snapshot.getValue<Score>()

                    if (!snapshot.exists() || score > highScore!!.score!!) {
                        val newHighScore = Score(MainActivity.username, score)
                        reference.setValue(newHighScore)
                    }
                }
            }

            updateView()
        }

        // we can save persistent data by keeping game state even after closing the app maybe?
        homeButton.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateView() {
        val landmark = game.getCurrentLandmark()
        if (landmark != null) {
            locationImg.setImageResource(landmark.photo)
            locationText.text = "${landmark.name}, ${landmark.city}"
        } else {
            guessButton.isVisible = false
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {

        // Get the sensor, and set the appropriate phone-specific coordinates
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerCoordinates = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerCoordinates = event.values.clone()
        }

        val rotationMatrix = FloatArray(9)

        // Get globe rotation matrix from phone orientation data
        if (SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerCoordinates,
                magnetometerCoordinates
            )
        ) {
            val orientation = FloatArray(3)

            // Get orientation from rotation matrix
            SensorManager.getOrientation(rotationMatrix, orientation)

            val rotateAnimation =
                RotateAnimation(
                    Math.toDegrees(-compassDegree.toDouble()).toFloat(),
                    Math.toDegrees(orientation[0].toDouble()).toFloat(),
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )

            rotateAnimation.duration = 200
            compassDegree = orientation[0]

            compassImage.startAnimation(rotateAnimation)

//            Log.w(
//                "MainActivity",
//                "Azimuth: ${orientation[0]}, Pitch: ${orientation[1]}, Roll: ${orientation[2]}"
//            )

//            valw direction = Math.toDegrees(compassDegree.toDouble()).toFloat()
//            Log.w("MainActivity", "Direction: $direction")
        }
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

//    fun testGame() {
//
//        val statueLiberty = Location("dummyprovider")
//        statueLiberty.latitude = 40.68947165945934
//        statueLiberty.longitude = -74.04474627898335
//
//        val iribeCenter = Location("dummyprovider")
//        iribeCenter.latitude = 38.98912699513576
//        iribeCenter.longitude = -76.93637753412865
//
//        game.newGame(1, arrayListOf(Landmark("Statue of Liberty", "New York", iribeCenter, "")))
//
//
//
//        Log.w("MainActivity", "" + game.getCurrentLandmark())
//
//        val acc = game.guess(50f, statueLiberty)
//
//        Log.w("MainActivity", "Accuracy: $acc")
//    }

    fun handleAds() {
        adView = AdView( this )
        var adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT )
        adView.setAdSize( adSize )
        var adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        var builder = AdRequest.Builder( )
        builder.addKeyword( "locations" ).addKeyword( "travel" ).addKeyword( "fun")
        var adRequest = builder.build()

        var adLayout = findViewById<LinearLayout>( R.id.ad_view )
        adLayout.addView( adView )
        adView.loadAd( adRequest )

    }
}