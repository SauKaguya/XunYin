package com.kaguya.music.data

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateListOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.text.Subtitle

data class Song
    (
    val imageUrl: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int,
    val isRadio: Boolean? = false,
    val media: Uri? = null,
    var timesPlayed: Int? = 0,
    val dateAdded: String? = "",
    val year: String? = "",
    val format: String? = "MP3",
    val bitrate: String? = "320",
    val navidromeID: String? = "",
    val lastPlayed: String? = "",
    val trackIndex: Int? = 0
)

val songsList: MutableList<Song> = mutableStateListOf()