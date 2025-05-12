package com.example.percussionapp

import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import androidx.core.graphics.rotationMatrix
import kotlin.math.log

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
fun getLogFrequencies(waveform: DoubleArray, listSize : Int) : List<Double>{
    val logScaleFactor = (listSize) / log(waveform.size.toDouble(), 10.0)
    var currentIndex = 1
    var currentLogIndex : Double = log((currentIndex + 1.0), 10.0) * logScaleFactor
    val processedWave = MutableList(listSize){0.0}
    for (i in 0..<processedWave.size - 1){
        processedWave[i] = waveform[currentIndex - 1]
        if (currentLogIndex <= i) {
            currentIndex++
            currentLogIndex = log(
                (currentIndex.toDouble()),
                10.0
            ) * logScaleFactor
        }
    }
    return processedWave
}