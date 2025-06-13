package com.example.percussionapp

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
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

//display showing early, late, miss etc.
@Composable
fun NoteFeedback(barProgress: Float, notesPlayed:Int, notesWidth: Int, currentNote:Int){

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
        if (currentNote == 0) {
            noteColour = Color.Green
            textColour = noteColour
            timeText = "GOOD"
        } else if (currentNote == 1) {
            noteColour = Color.hsv(35f, 1f, 1f)
            textColour = noteColour
            timeText = "EARLY"
        } else if (currentNote == 2) {
            noteColour = Color.Magenta
            textColour = noteColour
            timeText = "LATE"
        } else if (currentNote == -1) {
            noteColour = Color.Transparent
            textColour = Color.Red
            timeText = "MISS"
        } else if (currentNote == -2){
            noteColour = Color.Blue
            textColour = Color.Blue
            timeText = "SKIP"
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
                size.width * 0.03f + ((notesWidth * 0.92f * barProgress)),
                size.height * 0.5f
            )
        )

        val measuredText =
            textMeasurer.measure(
                timeText,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ),
            )

        drawText(
            measuredText,
            topLeft = Offset(
                notesWidth * -0.02f + (notesWidth * 0.92f * (barProgress)),
                size.height * 0.8f
            ),
            color = textColour,
            alpha = textAlpha.value
        )
    }
}

@Composable
fun Notes(barProgress: Float, currentNotes: Int, notesPlayed: Int,currentNote:Int){
    var notesWidth by remember{ mutableIntStateOf(0) }
    Box(contentAlignment = Alignment.CenterStart) {
        Image(
            painter = painterResource(R.drawable.note_line),
            contentDescription = "Start indicator",
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .offset{ IntOffset((notesWidth * -0.06f).toInt(),0) },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color = Color.Black)
        )
        Image(
            painter = painterResource(R.drawable.note_line),
            contentDescription = "Bar",
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .offset{ val noteLineOffset = ((notesWidth * 0.92f * barProgress) - (notesWidth * 0.06f)).toInt()
                    IntOffset(noteLineOffset,0) }
                .zIndex(1f),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color = Color.hsv(210f,1f,0.8f,0.7f))
        )
        Image(
            painter = painterResource(R.drawable.note_line),
            contentDescription = "End indicator",
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .offset{ IntOffset((notesWidth * 0.86f).toInt(),0) },
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color = Color.Black)
        )

        val notesImage = painterResource(currentNotes)
        val imageSize = notesImage.intrinsicSize

        Image(
            painter = notesImage,
            contentDescription = "res1",
            modifier = Modifier

                .aspectRatio(imageSize.width/imageSize.height)
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    notesWidth = coordinates.size.width
                },
            contentScale = ContentScale.Fit
        )

        NoteFeedback(barProgress,notesPlayed, notesWidth,currentNote)

    }
}

// a lot of adaptive sizing is used to keep the view consistent between different screen sizes
@Composable
fun PercussionStave(barProgress: Float, bar1Image: Int,notesPlayed:Int,currentNote:Int){
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(Modifier
        .fillMaxWidth()
        .fillMaxHeight(SHEET_MUSIC_HEIGHT), contentAlignment = Alignment.CenterStart) {

        Image(
            painter = painterResource(R.drawable.stave_line),
            contentDescription = "Stave",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            contentScale = ContentScale.FillBounds,
        )
        Row(Modifier.fillMaxSize()/*.background(Color.hsv(100f,0.9f,0.8f,0.5f))*/, verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.clef),
                contentDescription = "Clef",
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth(0.07f)
                    .padding(start = screenWidth * 0.02f),
                contentScale = ContentScale.Fit,
            )
            Image(
                painter = painterResource(R.drawable.cut_time),
                contentDescription = "TimeSignature",
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth(0.07f)
                    .padding(start = screenWidth * 0.02f),
                contentScale = ContentScale.Fit,
            )
            Box(contentAlignment = Alignment.CenterStart) {
                Notes(barProgress, bar1Image,notesPlayed,currentNote)
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

@Composable
fun WaveFormPeaks(waveform: DoubleArray) {
    var prev1 : Double = 0.0
    var prev2 : Double = 0.0
    for (i in 1..<waveform.size - 1) {
        if (prev2 > prev1 && prev2 > waveform[i] && prev2 > 350 && prev2 < 9999999 && i < 40) {
            println(prev2)
        }
        prev1 = prev2
        prev2 = waveform[i]
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun FreqCanvas(waveform: DoubleArray, spectrogramOn: Boolean,
               currentSpectrogramBitmap: MutableState<Bitmap>, lastSpectrogramBitmap: MutableState<Bitmap>,
               notesPlayed: Int, currentNote: Int){
    var canvasWidth by remember { mutableFloatStateOf(10f) }
    var canvasHeight by remember { mutableFloatStateOf(10f) }
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
            var prev1 : Double = 0.0
            var prev2 : Double = 0.0
            var biggestPeak : Double = 0.0
            var biggestBin = -1
            var biggestPeak2 : Double = 0.0
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
            if (peaks.size > 0) {
                if (biggestBin < 7) {
                    println("Bass")
                } else if (waveform[12] > 250 || waveform[11] > 250) {
                    println("Tone")
                } else if (waveform[18] > 350 || waveform[17] > 350) {
                    println("Slap")
                }
                println("$peaks - Biggest bin = $biggestBin at $biggestPeak, second biggest = $biggestBin2 at $biggestPeak2")


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
            drawImage(image=lastSpectrogramBitmap.value.asImageBitmap(), topLeft = Offset(x = (size.width / 2), y = 0.0f))

        }
    }
}