package com.example.percussionapp

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


fun checkFreq(waveform : DoubleArray) {
    println(waveform)
}

//fun checkFreq2(engineVM: AudioEngineViewModel) {
 //   val waveform2 by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
//}

@Preview
@Composable
fun Tuner(engineVM: AudioEngineViewModel, waveform2 : DoubleArray, recording : Boolean, startRecord : ()->Unit ) {
    if (waveform2[4] > 50 && waveform2[4] > prevAttack + 10 && waveform2[4] < 999) {println(waveform2[4])}
    val activityContext = LocalContext.current
    val currentAttack = waveform2[3]
    //println("prev - $prevAttack , current - $currentAttack")
    val waveform by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
    LaunchedEffect(waveform){
        //println("launcedeffect")
    }
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
            Text(text = "Yippee", fontSize = 25.sp)
        }
    }
    if (waveform2[4] < 999) {prevAttack = waveform2[4]}

}