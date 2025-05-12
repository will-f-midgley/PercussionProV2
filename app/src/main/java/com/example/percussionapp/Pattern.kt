package com.example.percussionapp

import androidx.compose.runtime.mutableStateOf

class Pattern(val name: String) {
    var stream: Int? = null
    var audioID: Int? = null
    var playing = mutableStateOf(false)
}