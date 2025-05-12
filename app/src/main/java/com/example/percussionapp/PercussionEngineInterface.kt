package com.example.percussionapp

import android.content.res.AssetManager

interface PercussionEngineInterface {
    suspend fun startPlayer(genre: Int)
    suspend fun startDetector()
    suspend fun stopPlayer()
    suspend fun stopDetector()
    suspend fun isRunning() : Boolean
    suspend fun isRecording() : Boolean
    suspend fun isPlaying() : Boolean
    suspend fun getNotesPlayed() : Int
    suspend fun sendNote(type: Int) : Int
    suspend fun changeDifficulty(difficulty: Int) : Boolean
    suspend fun changeLatency(latency: Int) : Boolean
    suspend fun toggleMetronome(metronomeOn: Boolean) : Boolean
    suspend fun changeTempo(tempo: Int) : Boolean
}