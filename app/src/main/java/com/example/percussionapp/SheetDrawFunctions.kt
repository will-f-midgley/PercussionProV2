package com.example.percussionapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.log

val queue = mutableListOf<Int>()
val recentHits = mutableListOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
val recentAccuracy = mutableListOf<Int>()
var totalHit = 0

@Composable
fun TypeHit(waveform: DoubleArray) {
    val activityContext = LocalContext.current
    //println(bar1Image[totalHit%8])
    val sharedPreference = activityContext.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    var noteHit by remember { mutableStateOf("None") }
    if (waveform[0] > 10) {
        val rawSlap = sharedPreference.getString("Slap", "0")
        val slapArray = rawSlap?.split(",")
        val rawBass = sharedPreference.getString("Bass", "0")
        val bassArray = rawBass?.split(",")
        val rawTone = sharedPreference.getString("Tone", "0")
        val toneArray = rawTone?.split(",")
        val peaksArray = Normalise(waveform)

        var diffSlap = 0.0
        var diffTone = 0.0
        var diffBass = 0.0
        if (slapArray != null && toneArray != null && bassArray != null) {
            for (i in 0..(slapArray.size - 1)) {
                //println(slapArray[i].toDouble())
                diffSlap = diffSlap + (slapArray[i].toDouble() - peaksArray[i]) * (slapArray[i].toDouble() - peaksArray[i])
                diffBass = diffBass + (bassArray[i].toDouble() - peaksArray[i]) * (bassArray[i].toDouble() - peaksArray[i])
                diffTone = diffTone + (toneArray[i].toDouble() - peaksArray[i]) * (toneArray[i].toDouble() - peaksArray[i])
            }
        }
        if (diffBass < diffSlap && diffBass < diffTone) {
            //println("diffBass = $diffBass")
            noteHit = "Bass"
            if (bar1Image[totalHit%8] == "Bass") {
                recentAccuracy.add(100)
            } else {recentAccuracy.add(0)}
        } else if (diffTone < diffBass && diffTone < diffSlap) {
            //("diffTone = $diffTone")
            noteHit = "Tone"
            if (bar1Image[totalHit%8] == "Tone") {
                recentAccuracy.add(100)
            } else {recentAccuracy.add(0)}
        } else if (diffSlap < diffBass && diffSlap < diffTone) {
            //println("diffSlap = $diffSlap")
            noteHit = "Slap"
            if (bar1Image[totalHit%8] == "Slap") {
                recentAccuracy.add(100)
            } else {recentAccuracy.add(0)}
        }
        if (recentAccuracy.size > 19) {
            recentAccuracy.removeAt(0)
        }
    }



}

@Composable
fun calculatePercentage(last20: MutableList<Int>): Int {
    if (last20.size == 0) {return 100}
    var sum = 0
    for (i in 0..(last20.size-1)) {
        sum += last20[i]
    }
    return sum/last20.size

}

//display showing early, late, miss etc.
@Composable
fun NoteFeedback(barProgress: Float, notesPlayed:Int, notesWidth: Int, currentNote:Int, currentNotes: Array<String>, noteAudio: MutableState<Boolean>){
    val context = LocalContext.current
    val noteAlpha = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(1f) }
    val noteSize = remember { Animatable(120f) }

    var noteColour by remember { mutableStateOf(Color.Red) }
    var textColour by remember { mutableStateOf(Color.Red) }
    var timeText by remember { mutableStateOf("GOOD") }
    val textMeasurer = rememberTextMeasurer()


    LaunchedEffect(notesPlayed) {
        noteSize.animateTo(20f, snap())
        noteSize.animateTo(120f, tween(400))
    }
    LaunchedEffect(notesPlayed) {
        noteAlpha.animateTo(1f, snap())
        noteAlpha.animateTo(0f, tween(300))
    }
    LaunchedEffect(notesPlayed) {
        textAlpha.animateTo(1f, snap())
        textAlpha.animateTo(0f, tween(1000))
    }
    LaunchedEffect(notesPlayed) {
        if (queue.size > 19) {
            queue.removeAt(0)
        }
        if (recentHits.size > 19) {
            recentHits.removeAt(0)
        }
        if (currentNote == 0) {
            noteColour = Color.Green
            textColour = noteColour
            timeText = "GOOD"
            queue.add(100)
            recentHits.add((-100..100).random())
        } else if (currentNote == 1) {
            noteColour = Color.hsv(35f, 1f, 1f)
            textColour = noteColour
            timeText = "EARLY"
            queue.add(50)
            recentHits.add((-300..-100).random())
        } else if (currentNote == 2) {
            noteColour = Color.Magenta
            textColour = noteColour
            timeText = "LATE"
            queue.add(50)
            recentHits.add((100..300).random())
        } else if (currentNote == -1) {
            noteColour = Color.Transparent
            textColour = Color.Red
            timeText = "MISS"
            queue.add(0)
        } else if (currentNote == -2){
            noteColour = Color.Blue
            textColour = Color.Blue
            timeText = "SKIP"
            queue.add(100)
        }

        val currentStroke = currentNotes[totalHit%8]
        if (noteAudio.value) {
            if (currentStroke == "Slap") {
                val playNote = MediaPlayer.create(context, R.raw.slap)
                playNote.start()
            } else if (currentStroke == "Bass") {
                val playNote = MediaPlayer.create(context, R.raw.bass)
                playNote.start()
            } else if (currentStroke == "Tone") {
                val playNote = MediaPlayer.create(context, R.raw.tone)
                playNote.start()
            }
        }
        totalHit += 1
        //println(totalHit)


    }
    val percentText = calculatePercentage(queue).toString() + "%"
    val accuracyText = calculatePercentage(recentAccuracy).toString() + "%"

    // Canvas in charge of creating the live timing bar.
    val config = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidth = config.screenWidthDp
    val scaledWidth = screenWidth * density
    //println(screenWidth)
    Canvas (Modifier.zIndex(0.90f).offset{ IntOffset((900f).toInt(),(400f).toInt()) }) {
        drawRect(color = Color.hsv(0f, 0f, 0f), topLeft = Offset(0f, 0f), size = Size(600f,50f))
        drawRect(color = Color.hsv(180f, 1f, 1f), topLeft = Offset(295f, 0f), size = Size(10f,50f))
        for (i in 0..(recentHits.size-1)) {
         //i * 5f is used to have the alpha value decrease over time so that newer hits appear fresher.
            drawRect(color = Color.hsv(180f, 1f, 1f, (i * .05f)), topLeft = Offset((297f + recentHits[i]), 0f), size = Size(6f,50f))
        }
    }

    Canvas(
        Modifier
            .width((notesWidth * 0.364).dp).then(Modifier.fillMaxHeight())
            .zIndex(0.9f)
    ) {



        drawCircle(
            //Color.hsv(22f, .9f, .9f)
            color = noteColour,
            radius = noteSize.value,
            alpha = noteAlpha.value,
            center = Offset(
                //(screenWidth.value * -0.12f)
                scaledWidth * 0.1f + (scaledWidth * 0.7f * barProgress),
                size.height * 0.5f
            )
        )

        val measuredPercent =
            textMeasurer.measure(
                percentText,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ),
            )

        val measuredAccuracy =
            textMeasurer.measure(
                accuracyText,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ),
            )

        // Timing Text
        //val accuracyColour = colour


        drawText(
            measuredPercent,
            topLeft = Offset(1600f, 600f),
            color = textColour
        )
        // Accuracy Text


        drawText(
            measuredAccuracy,
            topLeft = Offset(2000f, 600f),
            color = Color.hsv(0f, .50f, .40f)
        )



    }
}

@Composable
fun Notes(barProgress: Float, currentNotes: Array<String>, notesPlayed: Int,currentNote:Int, noteAudio: MutableState<Boolean>){
    val config = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidth = config.screenWidthDp
    val scaledWidth = screenWidth * density
    var notesWidth by remember{ mutableIntStateOf(0) }
    val style by remember{ mutableIntStateOf(R.drawable.bass)}
    //val style2 = style.intValue
    //val test = mutableIntStateOf(R.drawable.bass)
    //val test2 = test.intValue
    Box(contentAlignment = Alignment.CenterStart) {

        Image(
            painter = painterResource(R.drawable.note_line2),
            contentDescription = "Bar",
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .offset{ val noteLineOffset = ((scaledWidth*0.7 * barProgress) + (scaledWidth*0.1)).toInt()
                    IntOffset(noteLineOffset,0) }
                .zIndex(1f),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color = Color.hsv(210f,1f,0.8f,0.7f))
        )

        var notesImage = painterResource(style)
        val imageSize = notesImage.intrinsicSize
        //notesImage is actual image. Change to index thing
        notesWidth = (imageSize.width*16).toInt()

        Row(
            Modifier.fillMaxWidth(0.7f).offset{ IntOffset((scaledWidth*0.1).toInt(),0) },
            horizontalArrangement = Arrangement.Center,
        ) {
            for (i in 0..7) {
                val note = currentNotes[i]
                //println(note)
                val currentStyle = if (note == "Bass") {
                    R.drawable.bass
                } else if (note == "Slap") {
                    R.drawable.slap
                } else if (note == "Tone") {
                    R.drawable.tone
                } else (R.drawable.none)
                notesImage = painterResource(currentStyle)
                Image(
                    painter = notesImage,
                    contentDescription = "res$i",
                    modifier = Modifier
                        //.offset{ IntOffset((notesWidth * i).toInt(),0) }
                        .weight(1f)
                        .aspectRatio(0.5f)
                        //.fillMaxSize()

                    //contentScale = ContentScale.Crop
                )
            }
        }


        NoteFeedback(barProgress,notesPlayed, notesWidth,currentNote, currentNotes, noteAudio)

    }
}

// a lot of adaptive sizing is used to keep the view consistent between different screen sizes
@Composable
fun PercussionStave(barProgress: Float, bar1Image: Array<String>,notesPlayed:Int,currentNote:Int,noteAudio: MutableState<Boolean>){

    Box(Modifier
        .fillMaxWidth()
        .fillMaxHeight(SHEET_MUSIC_HEIGHT), contentAlignment = Alignment.CenterStart) {

        Row(Modifier.fillMaxSize()/*.background(Color.hsv(100f,0.9f,0.8f,0.5f))*/, verticalAlignment = Alignment.CenterVertically) {

            Box(contentAlignment = Alignment.CenterStart) {
                Notes(barProgress, bar1Image,notesPlayed,currentNote, noteAudio)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.fillMaxWidth(0.1f))
            //ScrollingNotes(modifier = Modifier.fillMaxWidth())
        }
    }
}


fun Normalise(ar : DoubleArray) : DoubleArray {
    var max = 0.0
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

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun FreqCanvas(waveform: DoubleArray, spectrogramOn: Boolean,
               currentSpectrogramBitmap: MutableState<Bitmap>, lastSpectrogramBitmap: MutableState<Bitmap>,
               notesPlayed: Int, currentNote: Int){
    var canvasWidth by remember { mutableFloatStateOf(10f) }
    var canvasHeight by remember { mutableFloatStateOf(10f) }
    val activityContext = LocalContext.current
    val sharedPreference = activityContext.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    SpectrogramUpdate(waveform,spectrogramOn,notesPlayed,currentNote,lastSpectrogramBitmap,currentSpectrogramBitmap, canvasWidth, canvasHeight)

    Canvas(
        modifier = Modifier.fillMaxSize().padding(20.dp).padding(top = 50.dp)
            /*.background(Color.hsv(300f, 0.1f, 0.85f))*/.graphicsLayer()
    ) {
        canvasWidth = size.width
        canvasHeight = size.height

        val waveformSize = waveform.size

        if (!spectrogramOn) {
            val leftOffset = size.width/2

            val windowSize = size.width / (log((waveformSize - 1).toDouble(), 10.0) * 2)
            //draw small frequency display in bottom left, each value is increased by log scale
            val peaks = mutableSetOf<Int>()
            var prev1 = 0.0
            var prev2 = 0.0
            var biggestPeak = 0.0
            var biggestBin = -1
            var biggestPeak2 = 0.0
            var biggestBin2 = -1
            for (i in 1..(waveform.size - 1)) {
                //println(i)
                if (prev2 > prev1 && prev2 > waveform[i] && prev2 > 90 && prev2 < 9999) {
                    peaks.add(i-1)
                    if (prev2 > biggestPeak) {
                        biggestPeak = prev2
                        biggestBin = i-1
                    } else if (prev2 > biggestPeak2) {
                        biggestPeak2 = prev2
                        biggestBin2 = i-1
                    }
                }
                prev1 = prev2
                prev2 = waveform[i]
            }
            // Function to analyse and print out predicted stroke
            if (peaks.size > 0) {
                val rawSlap = sharedPreference.getString("Slap", "0")
                val slapArray = rawSlap?.split(",")
                val rawBass = sharedPreference.getString("Bass", "0")
                val bassArray = rawBass?.split(",")
                val rawTone = sharedPreference.getString("Tone", "0")
                val toneArray = rawTone?.split(",")
                val peaksArray = Normalise(waveform)

                var diffSlap = 0.0
                var diffTone = 0.0
                var diffBass = 0.0
                if (slapArray != null && toneArray != null && bassArray != null) {
                    for (i in 0..(slapArray.size - 1)) {
                        //println(slapArray[i].toDouble())
                        diffSlap = diffSlap + (slapArray[i].toDouble() - peaksArray[i]) * (slapArray[i].toDouble() - peaksArray[i])
                        diffBass = diffBass + (bassArray[i].toDouble() - peaksArray[i]) * (bassArray[i].toDouble() - peaksArray[i])
                        diffTone = diffTone + (toneArray[i].toDouble() - peaksArray[i]) * (toneArray[i].toDouble() - peaksArray[i])
                    }
                }




            }

            for (i in 1..<waveformSize - 1) {
                var tempColor = Color.Red
                if (peaks.contains(i)) {
                    tempColor = Color.Green
                }
                drawLine(
                    //start = Offset(x = (canvasWidth/(waveformSize - 1)) * i, y = (canvasHeight - (waveform[i] * 10)).toFloat()),
                    start = Offset(
                        x = (windowSize * log(i.toFloat(), 10.0f)).toFloat() +leftOffset,
                        y = (size.height - (waveform[i] * 1.5)).toFloat()
                    ),
                    end = Offset(
                        x = (windowSize * log(
                            (i + 1).toFloat(),
                            10.0f
                        )).toFloat() + leftOffset,
                        y = (size.height - (waveform[i + 1] * 1.5)).toFloat()
                    ),

                    strokeWidth = 4.0f,
                    color = tempColor
                )
            }

        } else {
            //FIX THE RECOMPOSITION HERE! currently too slow!!
            drawImage(image=currentSpectrogramBitmap.value.asImageBitmap())
            drawImage(image=lastSpectrogramBitmap.value.asImageBitmap(), topLeft = Offset(x = (size.width / 2), y = -10.0f))

        }
    }
}