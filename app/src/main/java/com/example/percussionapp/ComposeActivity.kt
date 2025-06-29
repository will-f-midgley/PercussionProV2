package com.example.percussionapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.percussionapp.ui.theme.VeryLightOrange

class ComposeActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        println("beforetune")
        super.onCreate(savedInstanceState)

        //pass model to vm


        enableEdgeToEdge()
        setContent {
            //println("beforetheme")

            //println("before")
            Box(
                Modifier
                .fillMaxSize()
                .background(VeryLightOrange))

            //println(prevAttack)

            //peaksArray = waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
            //waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17]



        }
    }


}