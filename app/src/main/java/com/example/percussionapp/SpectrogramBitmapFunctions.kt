package com.example.percussionapp

import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import androidx.core.graphics.rotationMatrix
//import kotlin.math.log
import kotlin.math.*

//converts 2d array to spectrogram, encoding values as colours
fun createScaledSpectrogramBitmap(spectrogram: List<List<Double>>,canvasWidth: Float, canvasHeight: Float): Bitmap {
    return createScaledBitmap(
        rotateBitmap(createBitmap(spectrogram,
            //there seems to be something wrong with how colour values are packed - need to change configuration
            {(0 and 0xff) shr 24 or //??>?red???
                    (255 and 0xff) shl 16 or //ALPHA
                    ((255 - (it * 5)).toInt() and 0xff) shl 8 or //GREEN
                    ((255 - (it * 5)).toInt() and 0xff)} //GREEN
            , Bitmap.Config.ARGB_8888), -90f),
        (canvasWidth).toInt(),canvasHeight.toInt(),false)
}


//creates bitmap based on two dimensional list - first flattens it, transforms and calls standard createBitmap function
fun createBitmap(spectrogram: List<List<Double>>, transform: (Double) -> Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val intColors = spectrogram
        .flatten()
        .map(transform)
        .toIntArray()
    val width = spectrogram . firstOrNull ()?.size ?: 0
    val height = spectrogram.size
    return Bitmap.createBitmap(
        intColors,
        width,
        height,
        config,
    )
}

fun rotateBitmap(original : Bitmap, degrees: Float) : Bitmap {
    val matrix = rotationMatrix(degrees)
    val rotatedBitmap = Bitmap.createBitmap(original , 0, 0, original.getWidth(), original.getHeight(), matrix, true)
    return rotatedBitmap
}

//stretches array by logarithmic scale
fun getLogFrequencies(waveform: DoubleArray, listSize : Int) : Pair<List<Double>, MutableSet<String>>{
    val logScaleFactor = ((listSize) / log(waveform.size.toDouble(), 10.0)) * 2
    //println(waveform.size)
    val setTest = mutableSetOf<String>()
    var currentIndex = 1
    var currentLogIndex : Double = log((currentIndex + 1.0), 10.0) * logScaleFactor
    val processedWave = MutableList(listSize){0.0}
    var notePlayed : Boolean = false

    // Used to filter unnecessary noise
    var biggestFreq : Double = 10.00
    var biggestIndex : Int = 0
    val base: Double = 60.00
    //60.00
    var temp : Double = 0.0
    for (i in 0..<processedWave.size - 1){


        processedWave[i] = max(base, waveform[currentIndex -1])-base
        temp = processedWave[i]
        if (temp > 0.0) {notePlayed = true}
        //if (temp > 0.0 && temp < 99999) {println("$i\t $currentIndex - $temp")}
        //if (temp > 200 && temp < 999999) {
        //    println("$i - $temp")
        //}
        if (biggestFreq < waveform[currentIndex -1]) {
            biggestFreq = waveform[currentIndex -1]
            biggestIndex = currentIndex
        }
        //if (processedWave[i] > 4E30) {
        //    println(processedWave[i])
        //}
        if (currentLogIndex <= i) {
            if (temp < 99999 && temp > 100) {
                //println("$currentIndex - $temp")
                setTest.add("$currentIndex - $temp")
            }
            currentIndex++
            currentLogIndex = log(
                (currentIndex.toDouble()),
                10.0
            ) * logScaleFactor
        }
    }
    println(setTest.size)
    if (!(setTest.size == 0)) {println(setTest)}
    //for (i in 0..28) {
        //println(waveform[i])
    //}
    if (biggestIndex > 1 && biggestIndex < 7 && biggestFreq > 400 && biggestFreq < 99999999) {
        //println(biggestIndex)
        //println(biggestFreq)
        //println("bass - $biggestIndex of $biggestFreq")
    //} else if ((biggestIndex == 18 || biggestIndex == 19) && biggestFreq > 350 && biggestFreq < 999999999) {
    //    println("Slap - $biggestIndex of $biggestFreq")
    } else if (biggestFreq > 20 && biggestFreq < 999999999) {
        //println("Unknown - $biggestIndex of $biggestFreq")
    }

    return processedWave to setTest
}