package com.example.percussionapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.percussionapp.ui.theme.PercussionAppTheme
import com.example.percussionapp.ui.theme.VeryLightOrange

const val SHEET_MUSIC_HEIGHT = 0.6f

enum class Genre(var genre: String) {
    TUMBAO("Tumbao"),
    MOZAMBIQUE("Mozambique"),
    GUAGUANCO("Guaguancó"),
    MERENGUE("Merengue"),
    BOLERO("Bolero");

    fun genreToString(): String {
        return genre
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PercussionAppTheme {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Box(Modifier
                    .fillMaxSize()
                    .background(VeryLightOrange))
                Menu()
            }
        }

        //get permissions to record audio - this is necessary for the app to function!
        //write and read storage permissions aren't necessary yet but will likely be needed when adding further features
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                (this as Activity?)!!,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }
}

@Composable
fun Menu() {
    val activityContext = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp, 200.dp, 50.dp, 200.dp)
    ) {
        Text(
            text = "PercussionPro",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .weight(2f)
                .wrapContentSize()
        )
        Button(
            onClick = {
                val intent = Intent(activityContext, PracticeActivity::class.java)
                activityContext.startActivity(intent)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Practice", fontSize = 25.sp)
        }

        Button(
            onClick = {
                val intent = Intent(activityContext, ListenActivity::class.java)
                activityContext.startActivity(intent)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Listen", fontSize = 25.sp)
        }

        Button(
            onClick = {
                val intent = Intent(activityContext, AnalyseActivity::class.java)
                activityContext.startActivity(intent)
            }, Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Analyse", fontSize = 25.sp)
        }
    }
}