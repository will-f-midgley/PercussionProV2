package com.example.percussionapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.percussionapp.ui.theme.LightOrange
import com.example.percussionapp.ui.theme.PercussionAppTheme
import com.example.percussionapp.ui.theme.StrongBrown
import com.example.percussionapp.ui.theme.VeryLightOrange

//get image resources of sheet music
fun getSheetRes(style: Genre, barNum: Int) : MutableIntState {
    if (barNum == 1) {
        return when (style) {
            Genre.MERENGUE -> mutableIntStateOf(R.drawable.merengue)
            Genre.GUAGUANCO -> mutableIntStateOf(R.drawable.guaguanco)
            Genre.MOZAMBIQUE -> mutableIntStateOf(R.drawable.mozambique)
            else -> mutableIntStateOf(R.drawable.tumbao)
        }
    } else{
        return when (style) {
            Genre.MERENGUE -> mutableIntStateOf(R.drawable.merengue2)
            Genre.GUAGUANCO -> mutableIntStateOf(R.drawable.guaguanco2)
            Genre.MOZAMBIQUE -> mutableIntStateOf(R.drawable.mozambique2)
            Genre.BOLERO -> mutableIntStateOf(R.drawable.bolero2)
            else -> mutableIntStateOf(R.drawable.tumbao)
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SpectrogramUpdate(waveform: DoubleArray,
                      spectrogramOn: Boolean,
                      notesPlayed: Int, currentNote: Int,
                      lastSpectrogramBitmap: MutableState<Bitmap>,
                      currentSpectrogramBitmap: MutableState<Bitmap>,
                      canvasWidth: Float, canvasHeight: Float){

    val spectrogramSize = 20
    val spectrogramResolution = waveform.size * 2 - 1
    var lastSpectrogram by remember {
        mutableStateOf(
            List(spectrogramSize) { List(spectrogramResolution) { 0.0 } }
        )
    }
    var currentSpectrogram by remember {
        mutableStateOf(
            MutableList(spectrogramSize) { List(spectrogramResolution) { 0.0 } }
        )
    }

    //captures how many waves to record when showing spectrogram image -
    val wavesToRecord = remember { mutableIntStateOf(0) }
    //when waveform updated, update the current spectrogram to add frequency spectra (if showing)
    """LaunchedEffect(waveform) {
        if (spectrogramOn && wavesToRecord.intValue > 0) {
            val (processedWave, testSet) = getLogFrequencies(waveform,spectrogramResolution)
            currentSpectrogram.add(processedWave.toList())
            wavesToRecord.intValue--

            if (wavesToRecord.intValue == 0){
                lastSpectrogramBitmap.value = currentSpectrogramBitmap.value.asShared()
                currentSpectrogramBitmap.value = createScaledSpectrogramBitmap(currentSpectrogram,canvasWidth / 2.1f,canvasHeight)
            }
        }
    }"""

    //when a note is played, change wavesToRecord to capture the frequencies for the next few milliseconds
    LaunchedEffect(notesPlayed) {
        if( currentNote != -2 && spectrogramOn){
            lastSpectrogram = currentSpectrogram.toList()
            currentSpectrogram = MutableList(0) { List(spectrogramResolution) { 0.0 } }
            wavesToRecord.intValue = spectrogramSize
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("UnrememberedMutableState")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeView(engineVM: AudioEngineViewModel, style: Genre) {
    //println("inPracticeview")
    val notesPlayed by mutableStateOf(engineVM.notesPlayed.observeAsState().value)
    val waveform by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
    val currentBar by mutableStateOf(engineVM.currentBar.observeAsState().value)
    val currentNote by mutableStateOf(engineVM.currentNote.observeAsState().value)
    val playing by mutableStateOf(engineVM.playerLoaded.observeAsState().value)
    println("waveformdefined")
    //these settings should realistically be in the C++ side and accesed via the VM
    //they are here temporarily, but when manipulation has to be done in native code they should be moved.
    val accuracy = remember { mutableIntStateOf(1) }
    LaunchedEffect(accuracy.intValue) { engineVM.changeDifficulty(accuracy.intValue) }
    val latency = remember { mutableIntStateOf(20) }
    LaunchedEffect(latency.intValue) { engineVM.changeLatency(latency.intValue) }
    val metronome = remember { mutableStateOf(false) }
    LaunchedEffect(metronome.value) { engineVM.toggleMetronome(metronome.value) }
    val tempo = remember { mutableIntStateOf(120) }
    LaunchedEffect(tempo.intValue) { engineVM.changeTempo(tempo.intValue) }

    val currentSpectrogramBitmap = remember {
        mutableStateOf(
            createScaledSpectrogramBitmap(
                List(1) { List(1) { 0.0 } },
               5f, 5f
            )
        )
    }

    val lastSpectrogramBitmap = remember {
        mutableStateOf(
            createScaledSpectrogramBitmap(
                List(1) { List(1) { 0.0 } },
                5f, 5f
            )
        )
    }

    val bar1Image = remember { getSheetRes(style,1) }
    val bar2Image = remember{ getSheetRes(style,2) }
    var settings by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf(false) }
    val spectrogramOn = remember { mutableStateOf(false) }
    val barProgress = remember { Animatable(0f) }

    BarUpdate(currentBar!!,barProgress,bar1Image,bar2Image, style, tempo.intValue)

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        containerColor = Color.White,
        topBar = {
            PercussionAppTheme {
                CenterAlignedTopAppBar(
                    title = {
                        Text(style.genreToString(),color = LightOrange)
                    },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { info = !info; settings = false }) {
                            Icon(Icons.Default.Info, "Info", tint = LightOrange)
                        }
                        IconButton(onClick = { settings = !settings; info = false }) {
                            Icon(Icons.Default.Settings, "Settings",tint = LightOrange)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().background(VeryLightOrange))
        //val imageModifier = Modifier.width(100.dp)
        if (settings && !info) {
            Box(
                Modifier
                    .padding(innerPadding).background(VeryLightOrange)
            ) {
                SheetSettings(spectrogramOn, metronome, accuracy, latency,tempo)
            }
        } else if (info && !settings) {
            Box(
                Modifier
                    .padding(innerPadding).background(VeryLightOrange)
            ) {
                NoteInfo()
            }
        }else {
            //main practice interface:
            //
            Box(
                Modifier
                    .padding(innerPadding).padding(top = 20.dp)
            ) {
                //var helpTextAlpha by remember{ mutableFloatStateOf(1f) } //remember{Animatable(1f)};
                StartPracticeButton(engineVM,playing!!,style)

                PercussionStave(barProgress.value, bar1Image.intValue,notesPlayed!!,currentNote!!)
                if(!spectrogramOn.value) {
                    Text("NEXT:", Modifier.offset(7.dp, 140.dp))
                    //Text("Place your phone 10cm from your instrument", Modifier.alpha(helpTextAlpha).offset(200.dp, 140.dp))
                    Image(
                        painter = painterResource(bar2Image.intValue),
                        contentDescription = "res2",
                        modifier = Modifier
                            .fillMaxHeight(0.4f)
                            .offset(30.dp, (160).dp),
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
            //
            Column {
                Spacer(Modifier.fillMaxHeight(SHEET_MUSIC_HEIGHT))
                //spectrogram display
                FreqCanvas(waveform!!,spectrogramOn.value, currentSpectrogramBitmap,lastSpectrogramBitmap,notesPlayed!!,currentNote!!)

            }
        }
    }
}

@Composable
fun BarUpdate(currentBar: Int, barProgress:Animatable<Float, AnimationVector1D>,
              bar1Image: MutableIntState,
              bar2Image: MutableIntState,
              style: Genre,
              tempo: Int) {
    var bars by remember { mutableStateOf(0) }
    val res1 by remember {
        getSheetRes(style,1)
    }
    val res2 by remember {
        getSheetRes(style,2)
    }

    //when starting, the barline will often move before the song has started - this removes it
    LaunchedEffect(currentBar) {
        bars++
        if (bars > 1) {
            if (currentBar == 1) {
                bar1Image.intValue = res1
                bar2Image.intValue = res2

            } else if (currentBar == 2) {
                bar1Image.intValue = res2
                bar2Image.intValue = res1
            }
            barProgress.animateTo(0f, snap())
            barProgress.animateTo(1f, tween((60 * 990 * 4 / tempo), easing = LinearEasing))
        }
    }

}

@Composable
fun StartPracticeButton(engineVM: AudioEngineViewModel,playing:Boolean, style:Genre) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.End){
        Button(
            onClick = { engineVM.togglePlay(style)
                engineVM.toggleRecord()
                /*helpTextAlpha = 0f*/},
            modifier = Modifier.padding(end = 20.dp)
        ) {

            if(!playing) {
                Icon(Icons.Default.PlayArrow, "Play",tint = StrongBrown)
                Text("PLAY", fontSize = 16.sp)
            } else{
                Icon(Icons.Default.Close, "Stop",tint = StrongBrown)
                Text("STOP", fontSize = 18.sp)
            }
        }
    }
}

