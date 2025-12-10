package com.example.directionguesser

import android.location.Location
import android.util.Log

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.absoluteValue
import kotlin.math.floor

class Game {
    private var maxRounds = 0
    private var currentRound = 0
    private var landmarks = ArrayList<Landmark>()
    private var gameState = GameState.NEW

    constructor() {
        val iribeCenter = Location("dummyprovider")
        iribeCenter.latitude = 38.98912699513576
        iribeCenter.longitude = -76.93637753412865

        val statueLiberty = Location("dummyprovider")
        statueLiberty.latitude = 40.68947165945934
        statueLiberty.longitude = -74.04474627898335


        this.landmarks = arrayListOf(
            Landmark("Statue of Liberty", "New York", statueLiberty, ""),
            Landmark("Iribe", "College Park", iribeCenter, ""))
    }

    fun newGame(rounds: Int, landmarks: ArrayList<Landmark>) {
        maxRounds = rounds
        currentRound = 0
        this.landmarks = ArrayList()
        this.landmarks.addAll(landmarks)

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

        var targetDirection = SphericalUtil.computeHeading(
            LatLng(location.latitude, location.longitude),
            LatLng(targetLoc.latitude, targetLoc.longitude)
        ).toFloat()

        if (targetDirection < 0) {
            targetDirection += 360f
        }

        Log.w("MainActivity", "Direction: $direction Target Direction: $targetDirection")

        val diff = ((targetDirection - direction + 540f) % 360f - 180f).absoluteValue

        Log.w("MainActivity", "Diff: $diff")

        return 1 - diff / 180f
    }

    fun getLandmarks() : ArrayList<Landmark> {
        return this.landmarks
    }

    private fun circleMod(a: Float): Float {
        return a - floor(a / 360f) * 360f
    }
}

enum class GameState { NEW, PLAYING, OVER }
data class Landmark(val name: String, val city: String, val location: Location, val imageSrc: String)
data class Score(val username: String? = null, val score : Float? = null)