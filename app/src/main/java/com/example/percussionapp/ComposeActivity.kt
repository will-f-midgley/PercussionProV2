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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.percussionapp.ui.theme.LightOrange
import com.example.percussionapp.ui.theme.PercussionAppTheme
import com.example.percussionapp.ui.theme.VeryLightOrange
import kotlinx.serialization.Serializable
import java.util.Arrays


class ComposeActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)

    override fun onCreate(savedInstanceState: Bundle?) {
        println("beforetune")
        val tempIcon = Icons.Default.Info

        super.onCreate(savedInstanceState)

        //pass model to vm


        enableEdgeToEdge()
        setContent {
            val testIcon = painterResource(R.drawable.slap)
            val bar1ImageArray = remember { mutableStateListOf("Bass", "Bass", "Bass", "Bass", "Bass", "Bass", "Bass", "Bass")}
            val bar1ImageIcons = remember { mutableStateListOf(R.drawable.bass, R.drawable.bass, R.drawable.bass, R.drawable.bass, R.drawable.bass, R.drawable.bass, R.drawable.bass, R.drawable.bass) }
            //println("beforetheme")

            //println("before")
            Box(
                Modifier
                .fillMaxSize()
                .background(VeryLightOrange)
            )

            Button(
                onClick = {
                }, Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(text = "Tone", fontSize = 25.sp)
            }

            Row(
                //Modifier.fillMaxWidth().padding(100.dp),
                Modifier.offset(7.dp, 140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly) {

                for (i in 0..7) {
                    IconButton(
                        onClick = {

                            if (bar1Image[i] == "Bass") {
                                bar1Image[i] = "Slap"
                                bar1ImageIcons[i] = R.drawable.slap
                            } else if (bar1Image[i] == "Slap") {
                                bar1Image[i] = "Bass"
                                bar1ImageIcons[i] = R.drawable.bass
                            }
                            val printed = bar1Image[i]
                            println("pressed 1 - $i -- $printed")

                                  },
                    ) {
                        Icon(painterResource(bar1ImageIcons[i]), "Info", tint = LightOrange)
                    }
                }
                //val num1 = findViewById<EditText>(R.id.num1)
            }

            Row(
                Modifier.offset(7.dp, 280.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly) {

                for (i in 0..7) {
                    IconButton(
                        onClick = {println("pressed 2 - $i")},
                    ) {
                        Icon(testIcon, "Info", tint = LightOrange)
                    }
                }
                //val num1 = findViewById<EditText>(R.id.num1)
            }


            //println(prevAttack)

            //peaksArray = waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
            //waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17]



        }
    }


}