package com.kaguya.music.data

import androidx.compose.runtime.mutableStateListOf
import com.kaguya.music.R

data class BottomNavItem(
    var title: String,
    var icon: Int,
    val screenRoute: String,
    val enabled: Boolean = true
)

var bottomNavigationItems = mutableStateListOf<BottomNavItem>(
    BottomNavItem(
        "Home",
        R.drawable.rounded_home_24,
        "home_screen"
    ),
    BottomNavItem(
        "Albums",
        R.drawable.rounded_library_music_24,
        "album_screen"
    ),
    BottomNavItem(
        "Songs",
        R.drawable.round_music_note_24,
        "songs_screen"
    ),
    BottomNavItem(
        "Artists",
        R.drawable.rounded_artist_24,
        "artists_screen"
    ),
    BottomNavItem(
        "Radios",
        R.drawable.rounded_radio,
        "radio_screen"
    ),
    BottomNavItem(
        "Playlists",
        R.drawable.placeholder,
        "playlist_screen"
    )
)