package com.example.percussionapp

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

//val pathPrefix = Environment.getExternalStorageDirectory().absolutePath + "/recording"

class PracticeActivity : ComponentActivity() {
    val recorderViewModel = AudioEngineViewModel()
    val realRecorder = KotlinAudioEngine()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        realRecorder.initializeAssets(this.assets)
        super.onCreate(savedInstanceState)

        //pass model to vm
        recorderViewModel.audioHandler = realRecorder

        lifecycle.addObserver(realRecorder)

        enableEdgeToEdge()
        setContent {
            Practice(recorderViewModel)
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        lifecycle.removeObserver(realRecorder)
    }

    override fun onResume() {
        super.onResume()
        recorderViewModel.applyParameters()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Practice(recorderView: AudioEngineViewModel){

    PercussionAppTheme {
        var x = 0
        println("practice")
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val navController = rememberNavController()

        val recording by recorderView.detectorLoaded.observeAsState()
        val playing by recorderView.playerLoaded.observeAsState()

        //navhost moves between screens - useful for quick navigation. settings and info should also
        //be moved to using navhost system, currently not implemented because of some audio issues that
        //arise
        NavHost(navController = navController,
            startDestination = SelectScreen
        ){
            composable<SelectScreen> {

                val context = LocalContext.current
                BackHandler {
                    val intent = Intent(context, MainActivity::class.java)
                    //activityContext.packageManager.getLaunchIntentForPackage(
                    //                                activityContext.packageName)
                    context.startActivity(intent)
                }
                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Scaffold(
                    containerColor = Color.White,
                    topBar = {
                        PercussionAppTheme {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text("Select Style", color = LightOrange)
                                },
                                scrollBehavior = scrollBehavior
                            )
                        }
                    }
                ) { innerPadding ->
                    PercussionAppTheme {
                        Box(Modifier.fillMaxSize().background(VeryLightOrange))
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),

                            ) {
                            GenreButton({navController.navigate(SheetScreen(Genre.TUMBAO))},Genre.TUMBAO)
                            GenreButton({navController.navigate(SheetScreen(Genre.MOZAMBIQUE))},Genre.MOZAMBIQUE)
                            GenreButton({navController.navigate(SheetScreen(Genre.GUAGUANCO))},Genre.GUAGUANCO)
                            GenreButton({navController.navigate(SheetScreen(Genre.MERENGUE))},Genre.MERENGUE)
                            GenreButton({navController.navigate(SheetScreen(Genre.BOLERO))},Genre.BOLERO)
                        }
                    }
                }
            }

            composable<SheetScreen> {
                BackHandler {
                    //stop playing when back button is pressed
                    if (recording!!) {
                        recorderView.toggleRecord()
                    }
                    if (playing!!) {
                        recorderView.togglePlay(Genre.TUMBAO)
                    }
                    navController.navigate(SelectScreen)
                }
                val args = it.toRoute<SheetScreen>()
                val context = LocalContext.current
                (context as? Activity)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Box(Modifier.fillMaxSize().background(VeryLightOrange))
                x = x+1
                println("practice bouta run")
                PracticeView(recorderView, args.genre)
                println(x)
                Column {
                    Spacer(Modifier.fillMaxHeight(0.8f))
                }
            }
        }
    }
}

@Serializable
object SelectScreen

@Serializable
data class SheetScreen(
    val genre: Genre
)

@Composable
fun ColumnScope.GenreButton(genrePressed: () -> Unit, genre:Genre){
    Button(onClick = {
        genrePressed()
    }, Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(10.dp)
        .padding(top = 20.dp, bottom = 10.dp)
    ) {
        Text(genre.genreToString(),fontSize = 20.sp)
    }
}
