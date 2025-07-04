package com.example.percussionapp


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

class AudioEngineViewModel : ViewModel() {
    var audioHandler : PercussionEngineInterface? = null
        set (value){
            field = value
            applyParameters()
        }

    private val _notesPlayed by mutableStateOf(KotlinAudioEngine.liveNotes)
    private val _waveform by mutableStateOf(KotlinAudioEngine.liveWave)
    private val _currentBar by mutableStateOf(KotlinAudioEngine.liveBar)
    private val _currentNote by mutableStateOf(KotlinAudioEngine.currentNoteValue)
    private val _currentBeat by mutableStateOf(KotlinAudioEngine.currentBeatValue)
    private val _isRunning = MutableLiveData(false)
    private var _detectorLoaded = MutableLiveData(false)
    private val _playerLoaded = MutableLiveData(false)

    private val _songStarted = MutableLiveData(false)
    //private val _notesPlayed = MutableLiveData(0)

    val isRecording : LiveData<Boolean>
        get(){
            return _isRunning
        }
    val detectorLoaded : LiveData<Boolean>
        get(){
            return _detectorLoaded
        }
    val playerLoaded : LiveData<Boolean>
        get(){
            return _playerLoaded
        }
    val songStarted : LiveData<Boolean>
        get(){
            return _songStarted
        }
    val notesPlayed : MutableLiveData<Int>
        get(){
            return _notesPlayed
        }
    val frequencySpectrum : MutableLiveData<DoubleArray>
        get(){
            return _waveform
        }
    val currentBar : MutableLiveData<Int>
        get(){
            return _currentBar
        }

    val currentNote : LiveData<Int>
        get(){
            return _currentNote
        }

    fun changeDifficulty(difficulty: Int) {
        viewModelScope.launch{
            audioHandler?.changeDifficulty(difficulty)
        }
    }
    fun changeLatency(latency: Int) {
        viewModelScope.launch{
            audioHandler?.changeLatency(latency)
        }
    }
    fun changeTempo(tempo: Int) {
        viewModelScope.launch{
            audioHandler?.changeTempo(tempo)
        }
    }
    fun toggleMetronome(m: Boolean) {
        viewModelScope.launch{
            audioHandler?.toggleMetronome(m)
        }
    }

    fun toggleRecord() {
        viewModelScope.launch {
            if (audioHandler?.isRecording() == true) {
                Log.d("vm","is recording - stopping")
                audioHandler?.stopDetector()
            }
            else{
                audioHandler?.startDetector()
            }
            updateRecordState()
        }
    }

    fun togglePlay(genre: Genre) {
        viewModelScope.launch {
            if (audioHandler?.isPlaying() == true) {
                audioHandler?.stopPlayer()
            }
            else{
                audioHandler?.startPlayer(genre.ordinal)
            }
            updateRecordState()
        }
    }

    fun sendNote(type: Int){
        viewModelScope.launch {
            val timing = audioHandler?.sendNote(type)
        }
    }

    suspend fun soundDetected(){
        _notesPlayed.value = audioHandler?.getNotesPlayed()
    }

    private fun updateRecordState(){
        viewModelScope.launch {
            // if statements ensure that recomposition wont occur when the value is changed to the same as previous value
            if (_isRunning.value != audioHandler?.isRunning()) {
                _isRunning.postValue(audioHandler?.isRunning())
            }
            if (_detectorLoaded.value != audioHandler?.isRecording()) {
                _detectorLoaded.postValue(audioHandler?.isRecording())
            }
            if (_playerLoaded.value != audioHandler?.isPlaying()) {
                _playerLoaded.postValue(audioHandler?.isPlaying())
            }
        }
    }

    fun applyParameters() {
        viewModelScope.launch {
            _isRunning.value = false
            _notesPlayed.value = audioHandler?.getNotesPlayed()
        }
    }

    fun startPlaying(genre: Genre) {
        viewModelScope.launch{
            audioHandler?.startPlayer(genre.ordinal)
        }
    }
}