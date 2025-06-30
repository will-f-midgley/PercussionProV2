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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        super.onCreate(savedInstanceState)

        //pass model to vm


        enableEdgeToEdge()
        setContent {
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
                Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Image(
                    painter = painterResource(R.drawable.slap),
                    contentDescription = "length",
                    modifier = Modifier.fillMaxHeight(),
                    //onClick = {println("Hi")}

                )
            }


            //println(prevAttack)

            //peaksArray = waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17] )
            //waveform[3], waveform[10], waveform[11], waveform[12], waveform[13], waveform[14], waveform[15], waveform[16], waveform[17]



        }
    }


}