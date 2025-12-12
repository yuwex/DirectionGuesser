package com.example.directionguesser

import android.location.Location
import android.util.Log

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.round

class Game {
    private var maxRounds = 0
    private var currentRound = 0
    private var landmarks = ArrayList<Landmark>()

    private var defaultLandmarks = ArrayList<Landmark>()
    private var gameState = GameState.NEW

    constructor() {
        this.defaultLandmarks = arrayListOf(
            Landmark("Statue of Liberty", "New York", LatLng(40.68947165945934, -74.04474627898335), R.drawable.statue_liberty),
            Landmark("Iribe Center", "College Park", LatLng(38.98912699513576, -76.93637753412865), R.drawable.iribe),
            Landmark("Golden Gate Bridge", "San Francisco", LatLng(37.818496726, -122.473831438), R.drawable.gate_bridge),
            Landmark("Grand Canyon", "Canyons Place", LatLng(36.2190382, -113.1609597), R.drawable.grand_canyon),
            Landmark("Hoover Dam", "Boulder City", LatLng(36.0160633, -114.7378049), R.drawable.hoover_dam),
            Landmark("Venice Beach", "Venice", LatLng(33.977100, -118.467575), R.drawable.venice_beach),
            Landmark("Monument Valley", "Navajo County", LatLng(36.9833294, -110.0999996), R.drawable.monument_valley))
    }

    fun newGame(rounds: Int) {
        maxRounds = rounds
        currentRound = 0
        this.landmarks.addAll(defaultLandmarks)

        if (landmarks.size < maxRounds) {
            throw Exception("The number of passed locations must be less")
        }

        this.landmarks.shuffle()

        gameState = GameState.NEW
    }

    fun getCurrentLandmark(): Landmark? {
        if (gameState == GameState.OVER) {
            return null
        }

        return landmarks.get(currentRound)
    }

    fun guess(direction: Float, location: Location): Float? {
        // Returns the accuracy percent (0-1, 1 being completely correct)of the guess
        // Takes the current user direction (in degrees, 0-360) and location

        if (gameState == GameState.OVER) {
            return null
        }

        val targetLoc = getCurrentLandmark()!!.location

        Log.w("MainActivity", "Src: ${location.latitude}, ${location.longitude}, Dest: $targetLoc")

        var targetDirection = SphericalUtil.computeHeading(
            LatLng(location.latitude, location.longitude),
            targetLoc
        ).toFloat()

        if (targetDirection < 0) {
            targetDirection += 360f
        }

//        Log.w("MainActivity", "Direction: $direction Target Direction: $targetDirection")

        val diff = ((targetDirection - direction + 540f) % 360f - 180f).absoluteValue

//        Log.w("MainActivity", "Diff: $diff, score: ${1 - diff / 180f}")

        currentRound += 1

        if (currentRound >= maxRounds) {
            gameState = GameState.OVER
        }

        return round((1 - diff / 180f) * 100) / 100
    }

    fun getLandmarks() : ArrayList<Landmark> {
        return this.landmarks
    }


    fun formatScore(score: Float) : String {
        return "${(score * 10000).toInt()/100}%"
    }

    fun getDefaultLandmarks() : ArrayList<Landmark> {
        return this.defaultLandmarks
    }

    private fun circleMod(a: Float): Float {
        return a - floor(a / 360f) * 360f
    }
}

enum class GameState { NEW, PLAYING, OVER }
data class Landmark(val name: String, val city: String, val location: LatLng, val photo: Int)
data class Score(val username: String? = null, val score : Float? = null)