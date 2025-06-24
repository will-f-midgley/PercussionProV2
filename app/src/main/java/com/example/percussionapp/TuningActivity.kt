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


class TuningActivity : ComponentActivity() {
    val recorderViewModel = AudioEngineViewModel()
    val realRecorder = KotlinAudioEngine()
    val recording = true

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        println("beforetune")

        var peaksArray = arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        realRecorder.initializeAssets(this.assets)
        super.onCreate(savedInstanceState)

        //pass model to vm
        recorderViewModel.audioHandler = realRecorder

        lifecycle.addObserver(realRecorder)

        enableEdgeToEdge()
        setContent {
            //println("beforetheme")

                //println("before")
            val waveform by mutableStateOf(recorderViewModel.frequencySpectrum.observeAsState().value)
            Box(Modifier
                .fillMaxSize()
                .background(VeryLightOrange))
            Tuner(recorderViewModel, waveform!!, recording!!, {recorderViewModel.toggleRecord()})

            //println(prevAttack)

                //peaksArray = waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
                //waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17]



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

fun Normalise2(ar : Array<Double>) : Array<Double> {
    var max : Double = 0.0
    for (i in 0..(ar.size-1)) {
        if (ar[i] > max) {
            max = ar[i]
        }
    }
    for (i in 0..(ar.size-1)) {
        ar[i] = ar[i]/max
    }
    return ar
}

fun Magnitude(ar : DoubleArray) : Double {
    var mag : Double = 0.0
    for (i in 0..(ar.size-1)) {
        mag = mag + ar[i]
    }
    return mag
}


fun checkFreq(waveform : DoubleArray) {
    println(waveform)
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
    var slapArray = arrayOf(86.42892712234213, 72.263845004136, 150.31081161915716, 139.4460845929765, 44.01599347251757, 21.41825511070141, 7.0265391374781485, 148.34470227831065, 358.4993505323191)
    var toneArray = arrayOf(44.187432006412315, 25.670839837506115, 76.37721080917086, 124.24048435877741, 129.61980257519912, 82.1310829865887, 44.854661399239426, 47.13147251497897, 121.88675171923278)
    var bassArray = arrayOf(669.9019151426718, 86.66255200799405, 35.66802710733727, 25.286240113354612, 33.21913862862645, 26.54039860150233, 19.62759959905264, 19.584165599083956, 31.610588147541467)
    var peaksArray = arrayOf(waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
    //println(Arrays.toString(peaksArray))
    slapArray = Normalise2(slapArray)
    toneArray = Normalise2(toneArray)
    peaksArray = Normalise2(peaksArray)
    bassArray = Normalise2(bassArray)
    //val sharedPreference = getSharedPreferences("Preference_name", MODE_PRIVATE)


    val sharedPreference = activityContext.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)

    val highscore = sharedPreference.getString("bass", "0")
    val parts = highscore?.split(",")
    //if (parts != null) {
    //    for (i in 0..(parts.size-1)) {
    //        println(parts[i].toDouble())
    //    }
    //}

    with (sharedPreference.edit()) {
        //putInt("bass", slapArray)
        putString("bass", Arrays.toString(bassArray).replace("[","").replace("]",""))
        apply()
    }
    var diffSlap = 0.0
    var diffTone = 0.0
    var diffBass = 0.0
    for (i in 1..(slapArray.size-1)) {
        diffSlap = diffSlap + (slapArray[i] - peaksArray[i])*(slapArray[i] - peaksArray[i])
        diffTone = diffTone + (toneArray[i] - peaksArray[i])*(toneArray[i] - peaksArray[i])
        diffBass = diffBass + (bassArray[i] - peaksArray[i])*(bassArray[i] - peaksArray[i])
    }
    if (currentAttack > 100 && currentAttack > prevAttack + 40 && currentAttack < 999999999999) {
        //println(Arrays.toString(peaksArray))
        if (diffBass < diffSlap && diffBass < diffTone && diffBass < 0.2) {
            println("diffBass = $diffBass")
        } else if (diffTone < diffBass && diffTone < diffSlap && diffTone < 0.2) {
            println("diffTone = $diffTone")
        } else if (diffSlap < diffBass && diffSlap < diffTone && diffSlap < 0.2) {
            println("diffSlap = $diffSlap")}
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
    }
    //println(currentAttack)
    prevAttack = currentAttack

}