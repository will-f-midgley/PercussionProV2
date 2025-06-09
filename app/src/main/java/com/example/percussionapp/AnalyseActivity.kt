package com.example.percussionapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.percussionapp.ui.theme.PercussionAppTheme
import java.io.File

class AnalyseActivity : ComponentActivity() {
    private val recorderView = AudioEngineViewModel()
    private val realRecorder = KotlinAudioEngine()

    @SuppressLint("UnrememberedMutableState", "MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {

        realRecorder.initializeAssets(this.assets)

        super.onCreate(savedInstanceState)

        recorderView.audioHandler = realRecorder

        lifecycle.addObserver(realRecorder)

        enableEdgeToEdge()
        setContent {
            PercussionAppTheme {
                val frequencySpectrum by mutableStateOf(recorderView.frequencySpectrum.observeAsState().value)
                val recording by mutableStateOf(recorderView.detectorLoaded.observeAsState().value)

                Analysis(frequencySpectrum!!,recording!!,{recorderView.toggleRecord()})

            }
        }
    }
}

@Composable
fun Analysis(frequencySpectrum: DoubleArray, recording: Boolean, startRecord: ()->Unit){

    val arraySize = frequencySpectrum.size  - 1

    val spectrogram by remember{
        mutableStateOf(
            //200 columns visible on the screen at one time. 100 is a placeholder value
            MutableList(200) { List(arraySize) { 200.0 } }
        )
    }

    //when the spectrum is updated, remove the leftmost column from the spectrogram and add the newest one
    LaunchedEffect(frequencySpectrum){
        val processedWave = getLogFrequencies(frequencySpectrum ,arraySize)

        //val fileContent = File("res/raw/wave.txt").readText()
        //println(fileContent)
        spectrogram.removeAt(0)
        spectrogram.add(processedWave.toList())
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            var buttonText = "START"
            var iconRes = Icons.Default.PlayArrow
            if (recording){
                buttonText = "STOP"
                iconRes = Icons.Default.Close
            }
            Button(onClick = {startRecord()},
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(10.dp)){
                Icon(iconRes, "Play")
                Text(buttonText, fontSize = 20.sp)
            }
            Spectrogram(spectrogram)
        }
    }
}

@Composable
fun Spectrogram(spectrogram: MutableList<List<Double>>){
    Canvas(modifier = Modifier.fillMaxSize().padding(20.dp).background(Color.hsv(300f,0.1f,0.85f))) {
        val bitmap = createScaledSpectrogramBitmap(spectrogram,size.width,size.height)
        drawImage(image=bitmap.asImageBitmap())
    }
}

