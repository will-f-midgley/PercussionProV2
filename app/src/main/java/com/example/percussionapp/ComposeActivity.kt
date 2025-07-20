package com.example.percussionapp

import android.content.Context
import android.widget.EditText
import android.content.SharedPreferences
import android.app.Activity
import android.media.MediaPlayer
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.SoundPool
import android.os.Build
import kotlin.io.path.exists
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
import com.example.percussionapp.ui.theme.StrongBrown
import com.example.percussionapp.ui.theme.VeryLightOrange
import kotlinx.serialization.Serializable
import java.io.File
import java.util.Arrays

// Function to return appropriate drawable based on String input

fun updateComposeIcon(note : String) : Int {
    return when (note) {
        "Bass" -> R.drawable.bass;
        "Slap" -> R.drawable.slap;
        "Tone" -> R.drawable.toneicon;
        else -> R.drawable.none;
    }
}


class ComposeActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    //private var soundPoolBuilder: SoundPool.Builder = SoundPool.Builder()
    override fun onCreate(savedInstanceState: Bundle?) {
        //soundPoolBuilder.setMaxStreams(5)
        //val player = soundPoolBuilder.build()
        println("beforetune")
        //player.stop(stream)
        val tempIcon = Icons.Default.Info
        super.onCreate(savedInstanceState)

        //pass model to vm


        enableEdgeToEdge()
        setContent {
            PercussionAppTheme {
                val context = LocalContext.current
                val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val eternalFile = File(externalDir, "custom.txt")
                if (externalDir != null && !eternalFile.exists()) {
                    println("making file")
                    externalDir.mkdirs()

                    eternalFile.writeText("None,None,None,None,None,None,None,None\nNone,None,None,None,None,None,None,None")
                } else {
                }
                println("after")
                //val eternalFile = File(externalDir, "custom.txt")

                val testString = eternalFile.readLines()

                val testIcon = painterResource(R.drawable.slap)
                val bar1ImageArray = remember { mutableStateListOf(testString[0].split(",")) }
                val bar1ImageIcons = remember {
                    mutableStateListOf(
                        updateComposeIcon(bar1ImageArray[0][0]),
                        updateComposeIcon(bar1ImageArray[0][1]),
                        updateComposeIcon(bar1ImageArray[0][2]),
                        updateComposeIcon(bar1ImageArray[0][3]),
                        updateComposeIcon(bar1ImageArray[0][4]),
                        updateComposeIcon(bar1ImageArray[0][5]),
                        updateComposeIcon(bar1ImageArray[0][6]),
                        updateComposeIcon(bar1ImageArray[0][7])
                    )
                }
                val bar2ImageArray = remember { mutableStateListOf(testString[1].split(",")) }
                val bar2ImageIcons = remember {
                    mutableStateListOf(
                        updateComposeIcon(bar2ImageArray[0][0]),
                        updateComposeIcon(bar2ImageArray[0][1]),
                        updateComposeIcon(bar2ImageArray[0][2]),
                        updateComposeIcon(bar2ImageArray[0][3]),
                        updateComposeIcon(bar2ImageArray[0][4]),
                        updateComposeIcon(bar2ImageArray[0][5]),
                        updateComposeIcon(bar2ImageArray[0][6]),
                        updateComposeIcon(bar2ImageArray[0][7])
                    )
                }
                //println("beforetheme")

                //println("before")
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(VeryLightOrange)
                )

                Column(Modifier.padding(30.dp).padding(top = 30.dp))
                 {
                    Button(
                        onClick = {
                            for (i in 0..7) {

                                bar1Image[i] = "None"
                                bar2Image[i] = "None"
                                bar1ImageIcons[i] = R.drawable.none
                                bar2ImageIcons[i] = R.drawable.none
                            }
                        }, Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(text = "Reset", fontSize = 25.sp)
                    }
                    Row(
                        Modifier.fillMaxWidth(),

                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        for (i in 0..7) {
                            IconButton(
                                onClick = {
                                    val mp = MediaPlayer.create(context, R.raw.clave_short)
                                    mp.start()
                                    //val stream = player.play(R.raw.clave_short, 0.5f, 0.5f, 1, -1, 1.0f)
                                    if (bar1Image[i] == "Bass") {
                                        bar1Image[i] = "Slap"
                                    } else if (bar1Image[i] == "Slap") {
                                        bar1Image[i] = "Tone"
                                    } else if (bar1Image[i] == "Tone") {
                                        bar1Image[i] = "None"
                                    } else if (bar1Image[i] == "None") {
                                        bar1Image[i] = "Bass"
                                    }
                                    bar1ImageIcons[i] = updateComposeIcon(bar1Image[i])
                                },
                            ) {
                                Icon(painterResource(bar1ImageIcons[i]), "Info", tint = StrongBrown)
                            }
                        }
                        //val num1 = findViewById<EditText>(R.id.num1)
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        for (i in 0..7) {
                            IconButton(
                                onClick = {

                                    if (bar2Image[i] == "Bass") {
                                        bar2Image[i] = "Slap"
                                    } else if (bar2Image[i] == "Slap") {
                                        bar2Image[i] = "Tone"
                                    } else if (bar2Image[i] == "Tone") {
                                        bar2Image[i] = "None"
                                    } else if (bar2Image[i] == "None") {
                                        bar2Image[i] = "Bass"
                                    }
                                    bar2ImageIcons[i] = updateComposeIcon(bar2Image[i])
                                },
                            ) {
                                Icon(painterResource(bar2ImageIcons[i]), "Info", tint = StrongBrown)
                            }
                        }
                        //val num1 = findViewById<EditText>(R.id.num1)
                    }

                    Button(
                        onClick = {
                            var bar1ImageString = bar1Image[0].toString()
                            var bar2ImageString = bar2Image[0].toString()
                            for (i in 1..7) {
                                bar1ImageString += ",${bar1Image[i]}"
                                bar2ImageString += ",${bar2Image[i]}"
                            }
                            eternalFile.writeText("$bar1ImageString\n")
                            eternalFile.appendText(bar2ImageString)
                            val testString = eternalFile.readLines()
                            println(testString)
                            val test2 = testString[0].split(",")
                        }, Modifier
                            .fillMaxWidth()

                    ) {
                        Text(text = "Save", fontSize = 25.sp)
                    }
                }
                //println(prevAttack)

                //peaksArray = waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
                //waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17]


            }
        }
    }


}