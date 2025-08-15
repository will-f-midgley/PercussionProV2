package com.example.percussionapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//for on/off controls
@Composable
fun ColumnScope.Setting(text: String, value: MutableState<Boolean>){
    var buttonText = "ON"
    var buttonColour =  ButtonDefaults.buttonColors().containerColor
    if (!value.value){
        buttonText = "OFF"
        buttonColour = Color.LightGray
    }
    Row(
        Modifier.weight(1f).fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(modifier = Modifier.weight(2f),text = text, fontSize = 25.sp)
        Button(
            modifier = Modifier.weight(1f),
            onClick = { value.value = !value.value },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColour)) {
            Text(text = buttonText, fontSize = 25.sp)
        }
    }
}


//for slider controls
@Composable
fun ColumnScope.Setting(text: String, secondaryText: String, value: MutableState<Int>, range: IntRange){
    Row(
        Modifier.weight(1f).fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(Modifier.weight(2f)) {
            Text(text = text, fontSize = 25.sp)
            Text(text = secondaryText, fontSize = 18.sp)
        }
        Column(Modifier.weight(1f)) {
            Slider(
                value = value.value.toFloat(),
                onValueChange = { value.value = it.toInt() },
                colors = SliderDefaults.colors(),
                steps = 9,
                valueRange = range.first.toFloat()..range.last.toFloat()
            )
            Text(text = value.value.toString())
        }
    }
}

@Composable
fun NoteInfo(){
    Row(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.note_length),
            contentDescription = "length",
            modifier = Modifier.fillMaxHeight()
        )
        Image(
            painter = painterResource(R.drawable.note_type),
            contentDescription = "type",
            modifier = Modifier.fillMaxHeight()
        )
        Image(
            painter = painterResource(R.drawable.cut_time_info),
            contentDescription = "cuttime",
            modifier = Modifier.fillMaxHeight(1f)
        )
    }
}
@Composable
fun SheetSettings(spectrogramOn: MutableState<Boolean>, metronome: MutableState<Boolean>, accuracy: MutableState<Int>, latency: MutableState<Int>, tempo: MutableState<Int>, noteAudio: MutableState<Boolean>){
    Row(
        Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize().weight(1f)
        ) {
            Setting("Show spectrogram:", spectrogramOn)
            Setting("Note Audio:", noteAudio)
            Setting("Metronome:",metronome)
            Setting("Tempo:", "BPM of the backing track",tempo,IntRange(50,250))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize().weight(1f)
        ) {
            val range = IntRange(1,3)
            Setting("Difficulty:", "The higher this is, the shorter the window to play a note on time.",accuracy,range)
            Setting("Latency:", "If you are getting 'LATE' when you're playing on time, increase this",latency,IntRange(0,200))
        }
    }
}

