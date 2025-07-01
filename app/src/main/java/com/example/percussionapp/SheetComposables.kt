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
import android.os.Environment


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import kotlin.io.path.exists
import java.io.File
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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

public var bar1Image = arrayOf("Bass", "Bass", "Bass", "Bass", "Bass", "Bass", "Bass", "Bass")
public var bar2Image = arrayOf("Slap", "Slap", "Slap", "Slap", "Slap", "Slap", "Slap", "Slap")

//get image resources of sheet music
fun getSheetRes(style: Genre, barNum: Int, context: android.content.Context) : Array<String> {
    if (barNum == 1) {
        return when (style) {

            Genre.MERENGUE -> context.resources.getStringArray(R.array.merengue1)
            Genre.GUAGUANCO -> context.resources.getStringArray(R.array.guaguanco1)
            Genre.MOZAMBIQUE -> context.resources.getStringArray(R.array.mozambique1)
            Genre.BOLERO -> context.resources.getStringArray(R.array.merengue1)
            Genre.TUMBAO -> context.resources.getStringArray(R.array.tumbao1)
            // res files cannot be modified so custom rhythms are stored and read from sharedPreferences instead.
            Genre.CUSTOM -> context.resources.getStringArray(R.array.tumbao1)
        }
    } else{
        return when (style) {
            Genre.MERENGUE -> context.resources.getStringArray(R.array.merengue2)
            Genre.GUAGUANCO -> context.resources.getStringArray(R.array.guaguanco2)
            Genre.MOZAMBIQUE -> context.resources.getStringArray(R.array.mozambique2)
            Genre.BOLERO -> context.resources.getStringArray(R.array.merengue1)
            Genre.TUMBAO -> context.resources.getStringArray(R.array.tumbao2)
            Genre.CUSTOM -> context.resources.getStringArray(R.array.tumbao2)
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
    LaunchedEffect(waveform) {
        if (spectrogramOn && wavesToRecord.intValue > 0) {
            val (processedWave, testSet) = getLogFrequencies(waveform,spectrogramResolution)
            currentSpectrogram.add(processedWave.toList())
            wavesToRecord.intValue--

            if (wavesToRecord.intValue == 0){
                lastSpectrogramBitmap.value = currentSpectrogramBitmap.value.asShared()
                currentSpectrogramBitmap.value = createScaledSpectrogramBitmap(currentSpectrogram,canvasWidth / 2.1f,canvasHeight)
            }
        }
    }

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
    val context = LocalContext.current
    //println("inPracticeview")
    val notesPlayed by mutableStateOf(engineVM.notesPlayed.observeAsState().value)
    val waveform by mutableStateOf(engineVM.frequencySpectrum.observeAsState().value)
    val currentBar by mutableStateOf(engineVM.currentBar.observeAsState().value)
    val currentNote by mutableStateOf(engineVM.currentNote.observeAsState().value)
    val playing by mutableStateOf(engineVM.playerLoaded.observeAsState().value)
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

    bar1Image = remember { getSheetRes(style,1,context) }

    if (style == Genre.CUSTOM) {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (externalDir != null) {
            if (!externalDir.exists()) {
                externalDir.mkdirs()
                println("making file")
            }
        } else {println("broken")}
        val eternalFile = File(externalDir, "test_external.txt")
        eternalFile.writeText("Hello world!")
        val testString = eternalFile.readText()
        println(testString)
    }



    bar2Image = remember{ getSheetRes(style,2,context) }
    var settings by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf(false) }
    val spectrogramOn = remember { mutableStateOf(false) }
    val barProgress = remember { Animatable(0f) }

    BarUpdate(currentBar!!,barProgress, style, tempo.intValue)

    if (currentBar == 1) {
        bar1Image = remember { getSheetRes(style,1,context) }
        bar2Image = remember{ getSheetRes(style,2,context) }
    } else {
        bar1Image = remember { getSheetRes(style,2,context) }
        bar2Image = remember{ getSheetRes(style,1,context) }
    }

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
                PercussionStave(barProgress.value, bar1Image,notesPlayed!!,currentNote!!)
                if(!spectrogramOn.value) {
                    Text("NEXT:", Modifier.offset(7.dp, 140.dp))
                    //Text("Place your phone 10cm from your instrument", Modifier.alpha(helpTextAlpha).offset(200.dp, 140.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                        .fillMaxHeight(0.3f)
                        .offset(30.dp, (160).dp)

                    ) {
                        for (i in 0..7) {
                            var note = bar2Image[i]
                            val style = if (note == "Bass") {
                                R.drawable.bass
                            } else {
                                R.drawable.slap
                            }
                            var notesImage = painterResource(style)
                            Image(
                                painter = notesImage,
                                contentDescription = "res$i",
                                modifier = Modifier
                                    //.offset{ IntOffset((notesWidth * i).toInt(),0) }
                                    .aspectRatio(0.5f)
                                    .fillMaxSize()

                                //contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            //
            Column {
                Spacer(Modifier.fillMaxHeight(SHEET_MUSIC_HEIGHT))
                TypeHit(waveform!!)
                //spectrogram display
                FreqCanvas(waveform!!,spectrogramOn.value, currentSpectrogramBitmap,lastSpectrogramBitmap,notesPlayed!!,currentNote!!)

            }
        }
    }
}

@Composable
fun BarUpdate(currentBar: Int, barProgress:Animatable<Float, AnimationVector1D>,
              style: Genre,
              tempo: Int) {
    val context = LocalContext.current
    var bars by remember { mutableStateOf(0) }
    var res1 = getSheetRes(style,1,context)
    var res2 = getSheetRes(style,2,context)


    //when starting, the barline will often move before the song has started - this removes it
    LaunchedEffect(currentBar) {
        println("bars ------ $currentBar")
        bars++
        if (bars > 1) {
            barProgress.animateTo(0f, snap())
            barProgress.animateTo(1f, tween((60 * 990 * 4 / tempo), easing = LinearEasing))
            if (currentBar == 1) {
                bar1Image = res1
                bar2Image = res2

            } else if (currentBar == 2) {
                bar1Image = res2
                bar2Image = res1
            }

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

