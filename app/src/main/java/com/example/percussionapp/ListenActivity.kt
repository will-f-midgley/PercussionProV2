package com.example.percussionapp

import android.content.pm.ActivityInfo
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.percussionapp.ui.theme.LightOrange
import com.example.percussionapp.ui.theme.PercussionAppTheme
import com.example.percussionapp.ui.theme.StrongBrown
import com.example.percussionapp.ui.theme.VeryLightOrange


val patterns: Set<Pattern> =
    setOf(
        Pattern("Tumbao"),
        Pattern("Merengue"),
        Pattern("Mozambique"),
        Pattern("Merengue"),
        Pattern("Guaguancó"),
        Pattern("Bolero"))

val claves: Set<Pattern> =
    setOf(Pattern("Son clave"),
        Pattern("Rumba clave"))

class ListenActivity : ComponentActivity() {


    private var soundPoolBuilder: SoundPool.Builder = SoundPool.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        soundPoolBuilder.setMaxStreams(5)
        val player = soundPoolBuilder.build()
        for (pattern in patterns) {
            var resourceID = 0
            when(pattern.name){
                "Son clave" -> resourceID = R.raw.clave_short
                "Tumbao" -> resourceID = R.raw.tumbao
                "Merengue" -> resourceID = R.raw.merengue
                "Mozambique" -> resourceID = R.raw.mozambique
                "Guaguancó" -> resourceID = R.raw.guaguanco
                "Bolero" -> resourceID = R.raw.bolero

            }
            pattern.audioID = player.load(this, resourceID, 1)
        }

        for (clave in claves){
            var resourceID = 0
            when(clave.name){
                "Son clave" -> resourceID = R.raw.clave_short
                "Rumba clave" -> resourceID = R.raw.rumba_clave
            }
            clave.audioID = player.load(this, resourceID, 1)
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PercussionAppTheme {
                Box(Modifier.fillMaxSize().background(VeryLightOrange))
                Column(modifier = Modifier.padding(30.dp).padding(top=30.dp)) {

                    ListenUI(player)

                }
            }
        }
    }
}

fun playMusic(pattern: Pattern, clave: Pattern, togglePattern: Boolean, player:SoundPool){

    //due to sync issues, clave and pattern streams have to be changed at the same time.
    // neither can be closed unless both are stopped to preserve sync
    //instead, the volume is adjusted if one is playing but not the other. this maintains sync.

    var patternToToggle = clave
    var otherPattern = pattern
    if (togglePattern) {
        patternToToggle = pattern
        otherPattern = clave
    }
    //neither playing (start both, but keep otherPattern silent for now)
    if (!patternToToggle.playing.value && !otherPattern.playing.value){
        patternToToggle.stream = player.play(patternToToggle.audioID!!, 0.5f, 0.5f, 1, -1, 1.0f)
        otherPattern.stream = player.play(otherPattern.audioID!!, 0.0f, 0.0f, 1, -1, 1.0f)
    }
    //patternToToggle not playing, otherPattern playing
    else if (!patternToToggle.playing.value && otherPattern.playing.value) {
        player.setVolume(patternToToggle.stream!!,0.5f,0.5f)
    }
    //patternToToggle playing, otherPattern playing
    else if (patternToToggle.playing.value && otherPattern.playing.value) {
        player.setVolume(patternToToggle.stream!!,0.0f,0.0f)
    }
    //patternToToggle playing, otherPattern not playing (stop both)
    else if (patternToToggle.playing.value && !otherPattern.playing.value) {
        player.stop(patternToToggle.stream!!)
        player.stop(otherPattern.stream!!)
    }
    patternToToggle.playing.value = !patternToToggle.playing.value
}

@Composable
fun SheetView(currentPattern: Pattern) {
    var patternBar1 by remember { mutableIntStateOf(0) }
    var patternBar2 by remember { mutableIntStateOf(0) }

    when(currentPattern.name){
        "Son clave" -> {patternBar1 = R.drawable.clave1; patternBar2 = R.drawable.clave2}
        "Rumba clave" -> {patternBar1 = R.drawable.rumba_clave1; patternBar2 = R.drawable.clave2}
        "Tumbao" -> {patternBar1 = R.drawable.tumbao; patternBar2 = R.drawable.tumbao2}
        "Merengue" -> {patternBar1 = R.drawable.merengue; patternBar2 = R.drawable.merengue2}
        "Mozambique" -> {patternBar1 = R.drawable.mozambique; patternBar2 = R.drawable.mozambique2}
        "Guaguancó" -> {patternBar1 = R.drawable.guaguanco; patternBar2 = R.drawable.guaguanco2}
        "Bolero" -> {patternBar1 = R.drawable.tumbao; patternBar2 = R.drawable.bolero2}
    }
    Box(Modifier.padding(top = 0.dp).fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(patternBar1),
                contentDescription = "Sheet music",
                modifier = Modifier
                    .weight(1f),
                contentScale = ContentScale.FillWidth
                    //.graphicsLayer(2f, 2f)
            )
            Image(
                painter = painterResource(patternBar2),
                contentDescription = "Sheet music",
                modifier = Modifier
                    .weight(1f),
                contentScale = ContentScale.FillWidth
                    //.graphicsLayer(2f, 2f)
            )
        }
    }
}

@Composable
fun PlayButton(currentPattern: Pattern, currentClave: Pattern, player: SoundPool, patternButton: Boolean){
    Button(
        onClick = {playMusic(currentPattern,currentClave,patternButton,player)}
    ){
        if (patternButton) {
            if (!currentPattern.playing.value) {
                Icon(Icons.Default.PlayArrow, "Play", Modifier.size(30.dp),tint = StrongBrown)
            } else {
                Icon(Icons.Default.Close, "Stop", Modifier.size(30.dp),tint = StrongBrown)
            }
        } else{
            if (!currentClave.playing.value) {
                Icon(Icons.Default.PlayArrow, "Play", Modifier.size(30.dp),tint = StrongBrown)
            } else {
                Icon(Icons.Default.Close, "Stop", Modifier.size(30.dp),tint = StrongBrown)
            }
        }
    }
}

@Composable
fun PatternControl(pattern: MutableState<Pattern>, clave: MutableState<Pattern>, player: SoundPool){
    Row(horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp)) {
        var expanded by remember{ mutableStateOf(false) }
        OutlinedButton(
            onClick = {expanded = !expanded},
            modifier = Modifier.padding(end=10.dp)
        ) {
            Text(
                pattern.value.name, color = Color.Black, fontSize = 25.sp,
                lineHeight = 35.sp
            )
            DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false}) {
                patterns.forEach { p ->
                    DropdownMenuItem(
                        text = {Text(p.name, color = LightOrange)},
                        onClick = {
                            //stop playing when changing value to avoid sync issues
                            if (pattern.value.playing.value){
                                playMusic(pattern.value,clave.value,true,player)
                            }
                            if (clave.value.playing.value){
                                playMusic(pattern.value,clave.value,false,player)
                            }
                            //switch clave if guaguanco
                            val newClave: Pattern?
                            if (p.name == "Guaguancó"){
                                newClave = claves.find{it.name == "Rumba clave"}
                            } else{
                                newClave = claves.find{it.name == "Son clave"}
                            }
                            if (newClave != null){
                                clave.value = newClave
                            }
                            pattern.value = p}
                    )
                }
            }
        }
        PlayButton(pattern.value, clave.value, player,true)
    }
}

@Composable
fun ClaveControl(pattern: MutableState<Pattern>, clave: MutableState<Pattern>, player: SoundPool){
    Row(horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp)) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier.padding(end=10.dp)
        ) {
            Text(
                clave.value.name, color = Color.Black, fontSize = 25.sp,
                lineHeight = 35.sp
            )
        }
        PlayButton(pattern.value,clave.value,player,false)
    }

}

@Composable
fun ListenUI(player:SoundPool) {
    val currentPattern = remember{mutableStateOf(patterns.elementAt(0))}
    val currentClave = remember{mutableStateOf(claves.elementAt(0))}

    //switch to rumba clave if pattern is guaguancó
    LaunchedEffect(currentPattern) {
        val clave: Pattern?
        if (currentPattern.value.name == "Guaguancó"){
            clave = claves.find { it.name == "Rumba clave"}

        } else{
            clave = claves.find { it.name == "Son clave"}
        }
        if (clave != null){
            currentClave.value = clave
        }
    }

    Box {
        Column {
            PatternControl(currentPattern, currentClave, player)
            Box(Modifier.padding(top=20.dp)) {
                SheetView(currentPattern.value)
            }
            Box(Modifier.offset(y = -50.dp)) {
                SheetView(currentClave.value)
            }
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom){
            ClaveControl(currentPattern, currentClave, player)
        }
    }
}
