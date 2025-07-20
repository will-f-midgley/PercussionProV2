package com.example.percussionapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.percussionapp.ui.theme.VeryLightOrange
import java.util.Arrays


var prevAttack = 0.0
var selectedTune = "Null"


class TuningActivity : ComponentActivity() {
    val recorderViewModel = AudioEngineViewModel()
    val realRecorder = KotlinAudioEngine()
    val recording = true

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        realRecorder.initializeAssets(this.assets)
        super.onCreate(savedInstanceState)

        //pass model to vm
        recorderViewModel.audioHandler = realRecorder

        lifecycle.addObserver(realRecorder)

        enableEdgeToEdge()
        setContent {
            val waveform by mutableStateOf(recorderViewModel.frequencySpectrum.observeAsState().value)
            Box(Modifier
                .fillMaxSize()
                .background(VeryLightOrange))
            Tuner(recorderViewModel, waveform!!, recording, {recorderViewModel.toggleRecord()})
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        lifecycle.removeObserver(realRecorder)
    }

    override fun onResume() {
        super.onResume()
        recorderViewModel.applyParameters()
    }
}

fun Magnitude(ar : DoubleArray) : Double {
    var mag : Double = 0.0
    for (i in 0..(ar.size-1)) {
        mag = mag + ar[i]
    }
    return mag
}


fun checkFreq(waveform : DoubleArray) {
    //println(waveform)
}

@Composable
fun Tuner(engineVM: AudioEngineViewModel, waveform : DoubleArray, recording : Boolean, startRecord : ()->Unit ) {
    //if (waveform[4] > 50 && waveform[4] > prevAttack + 10 && waveform[4] < 999) {println(waveform[4])}
    val activityContext = LocalContext.current
    val currentAttack = Magnitude(waveform)
    //println(Arrays.toString(peaksArray))
    println(waveform[0])
    val peaksArray = Normalise(waveform)

    //val sharedPreference = getSharedPreferences("Preference_name", MODE_PRIVATE)


    val sharedPreference = activityContext.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)

    //val highscore = sharedPreference.getString("bass", "0")
    //val parts = highscore?.split(",")
    //if (parts != null) {
    //    for (i in 0..(parts.size-1)) {
    //        println(parts[i].toDouble())
    //    }
    //}
    if (selectedTune == "Tune") {
        val highscore = sharedPreference.getString("Bass", "0")
        val highscore2 = sharedPreference.getString("Slap", "0")
        val highscore3 = sharedPreference.getString("Tone", "0")
    }

    if (currentAttack > 200 && currentAttack > prevAttack + 40 && currentAttack < 999999999999) {
        //println(Arrays.toString(peaksArray))
        with (sharedPreference.edit()) {
            //putInt("bass", slapArray)
            putString(selectedTune, Arrays.toString(peaksArray).replace("[","").replace("]",""))
            apply()
        }
        println("retuned $selectedTune")
    }


    //println("prev - $prevAttack , current - $currentAttack")
    //val waveform2 by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(50. dp, 200.dp, 50.dp, 200.dp)
    ) {
        Button(
            onClick = {
                selectedTune = "Tune"
                startRecord()
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Tunes", fontSize = 25.sp)
        }
        Button(
            onClick = {
                selectedTune = "Bass"
                checkFreq(waveform)
                startRecord()
                //checkFreq2(engineVM)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Bass", fontSize = 25.sp)
        }
        Button(
            onClick = {
                selectedTune = "Slap"
                checkFreq(waveform)
                startRecord()
                //checkFreq2(engineVM)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Slap", fontSize = 25.sp)
        }
        Button(
            onClick = {
                selectedTune = "Tone"
                checkFreq(waveform)
                startRecord()
                //checkFreq2(engineVM)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Tone", fontSize = 25.sp)
        }
        //Text("Bass: $bass", Modifier.offset(7.dp, 140.dp))
        //Text("Slap: $slap", Modifier.offset(7.dp, 140.dp))
        //Text("Tone: $tone", Modifier.offset(7.dp, 140.dp))
    }
    //println(currentAttack)
    prevAttack = currentAttack

}