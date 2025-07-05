package com.example.percussionapp

import android.content.res.AssetManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import android.util.Log
import androidx.lifecycle.MutableLiveData

class KotlinAudioEngine : PercussionEngineInterface, DefaultLifecycleObserver {

    //DEBUG VALUES
    private val TAG = "tKotlinRecorder"

    private lateinit var assetManager : AssetManager
    private var nativeObjectsExists = false

    private external fun JNICreate(assetManager: AssetManager): Boolean
    private external fun JNIDelete() : Boolean
    private external fun JNIStartPlayer(style: Int) : Boolean
    private external fun JNIStartDetector() : Boolean
    private external fun JNIStopPlayer() : Boolean
    private external fun JNIStopDetector() : Boolean
    private external fun JNIIsRunning() : Boolean
    private external fun JNIIsPlaying() : Boolean
    private external fun JNIIsRecording() : Boolean
    private external fun JNISendNote(type: Int) : Int
    private external fun JNIChangeDifficulty(difficulty: Int) : Boolean
    private external fun JNIChangeLatency(latency: Int) : Boolean
    private external fun JNIChangeTempo(tempo: Int) : Boolean
    private external fun JNIToggleMetronome(metronome: Boolean) : Boolean

    companion object {

        var notesPlayed: Int = 0
        var liveNotes: MutableLiveData<Int> = MutableLiveData(0)
        var currentNoteValue: MutableLiveData<Int> = MutableLiveData(-2)
        var currentBeatValue: MutableLiveData<Int> = MutableLiveData(0)
        var liveBar: MutableLiveData<Int> = MutableLiveData(0)
        var liveWave: MutableLiveData<DoubleArray> = MutableLiveData(DoubleArray(64) {0.0})
        var spectrogram: MutableLiveData<List<List<Double>>> = MutableLiveData(MutableList(20) { List(64) { 0.0 } })

        //METHOD CAN BE CALLED BY C++ CODE AS IT IS IN COMPANION OBJECT
        @JvmStatic
        fun onSound(code: Int) {
            currentNoteValue.postValue(code)
            notesPlayed++
            liveNotes.postValue(notesPlayed)
        }

        //update frequency spectrum
        @JvmStatic
        fun showWave(freq: DoubleArray){
            liveWave.postValue(freq)
        }

        @JvmStatic
        fun newBar(barCount: Int){
            liveBar.postValue(barCount)
        }

        init{
            System.loadLibrary("percussionapp")
        }
    }


    override fun onResume(owner:LifecycleOwner){
        super.onResume(owner)
        createJNIObjects()
    }


    override fun onPause(owner: LifecycleOwner){
            // delete recorder if paused
        if (nativeObjectsExists){
            if (JNIStopPlayer() && JNIStopDetector()) {
                if (JNIDelete()) {
                        nativeObjectsExists = false
                } else {
                    Log.d(TAG, "JNIDelete failed onPause!")
                }
            } else { Log.d(TAG, "JNIStop failed onPause!") }
        }
        super.onPause(owner)
    }

    fun initializeAssets(assets: AssetManager){
        assetManager = assets
    }

    private fun createJNIObjects(){

        if (!nativeObjectsExists) {
            nativeObjectsExists = JNICreate(assetManager)
        }
    }

    override suspend fun sendNote(type: Int): Int {
            createJNIObjects()
            return JNISendNote(type)
    }

    override suspend fun startPlayer(genre: Int) {
            createJNIObjects()
            JNIStartPlayer(genre)
    }
    override suspend fun startDetector() {
            createJNIObjects()
            JNIStartDetector()
    }

    override suspend fun stopPlayer() {
            createJNIObjects()
            JNIStopPlayer()
    }

    override suspend fun stopDetector() {
            createJNIObjects()
            JNIStopDetector()
    }

    override suspend fun isRunning() : Boolean {
        createJNIObjects()
            return JNIIsRunning()
    }
    override suspend fun isRecording() : Boolean {
        createJNIObjects()
            return JNIIsRecording()
    }
    override suspend fun isPlaying() : Boolean {
        createJNIObjects()
        return JNIIsPlaying()
    }

    override suspend fun getNotesPlayed(): Int {
        createJNIObjects()
        return notesPlayed
    }

    override suspend fun changeDifficulty(difficulty: Int) : Boolean{
            createJNIObjects()
            return JNIChangeDifficulty(difficulty)
    }

    override suspend fun changeLatency(latency: Int) : Boolean{
            createJNIObjects()
            return JNIChangeLatency(latency)
    }

    override suspend fun toggleMetronome(metronomeOn: Boolean) : Boolean{
            createJNIObjects()
            return JNIToggleMetronome(metronomeOn)
    }

    override suspend fun changeTempo(tempo: Int) : Boolean{
            createJNIObjects()
            return JNIChangeTempo(tempo)
    }
}