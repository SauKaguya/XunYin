package com.kaguya.music.data

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf

data class Radio (
    val name: String,
    val media: Uri? = null,
    val homepageUrl: String = "",
    val imageUrl: Uri,
    val navidromeID: String? = "",
)

var radioList:MutableList<Radio> = mutableStateListOf()