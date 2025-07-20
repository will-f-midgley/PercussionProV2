package com.example.percussionapp

import android.content.Context
import android.widget.EditText
import android.content.SharedPreferences
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.percussionapp.ui.theme.LightOrange
import com.example.percussionapp.ui.theme.PercussionAppTheme
import com.example.percussionapp.ui.theme.VeryLightOrange
import kotlinx.serialization.Serializable
import java.util.Arrays


public var prevAttack = 0.0
public var selectedTune = "Null"


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
            Tuner(recorderViewModel, waveform!!, recording!!, {recorderViewModel.toggleRecord()})
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

//fun checkFreq2(engineVM: AudioEngineViewModel) {
 //   val waveform2 by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
//}

@Preview
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
        val parts = highscore?.split(",")
        val highscore2 = sharedPreference.getString("Slap", "0")
        val parts2 = highscore2?.split(",")
        val highscore3 = sharedPreference.getString("Tone", "0")
        val parts3 = highscore3?.split(",")
    }

    var diffSlap = 0.0
    var diffTone = 0.0
    var diffBass = 0.0
    """for (i in 1..(slapArray.size-1)) {
        diffSlap = diffSlap + (slapArray[i] - peaksArray[i])*(slapArray[i] - peaksArray[i])
        diffTone = diffTone + (toneArray[i] - peaksArray[i])*(toneArray[i] - peaksArray[i])
        diffBass = diffBass + (bassArray[i] - peaksArray[i])*(bassArray[i] - peaksArray[i])
    }"""
    if (currentAttack > 200 && currentAttack > prevAttack + 40 && currentAttack < 999999999999) {
        //println(Arrays.toString(peaksArray))
        with (sharedPreference.edit()) {
            //putInt("bass", slapArray)
            putString(selectedTune, Arrays.toString(peaksArray).replace("[","").replace("]",""))
            apply()
        }
        println("retuned $selectedTune")
    }

    val bass = sharedPreference.getString("Bass", "0")
    val parts = bass?.split(",")
    //println("Bass - $parts")
    val slap = sharedPreference.getString("Slap", "0")
    val parts2 = slap?.split(",")
    //println("Slap - $parts2")
    val tone = sharedPreference.getString("Tone", "0")
    val parts3 = tone?.split(",")

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
                checkFreq(waveform!!)
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
                checkFreq(waveform!!)
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
                checkFreq(waveform!!)
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