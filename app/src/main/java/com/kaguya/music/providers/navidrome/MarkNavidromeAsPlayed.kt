package com.kaguya.music.providers.navidrome

import com.kaguya.music.data.Song
import com.kaguya.music.data.navidromeServersList
import com.kaguya.music.data.selectedNavidromeServerIndex
import com.kaguya.music.data.useNavidromeServer
import com.kaguya.music.player.SongHelper
import com.kaguya.music.sliderPos

fun markNavidromeSongAsPlayed(song: Song){
    if (SongHelper.currentSong.isRadio == true || !useNavidromeServer.value) return

    println("Scrobble Percentage: ${(sliderPos.intValue.toFloat() / SongHelper.currentSong.duration.toFloat()) * 100f}, with sliderPos = ${sliderPos.intValue} | songDuration = ${SongHelper.currentSong.duration} | minPercentage = ${SongHelper.minPercentageScrobble}")
    if ((sliderPos.intValue.toFloat() / SongHelper.currentSong.duration.toFloat()) * 100f < SongHelper.minPercentageScrobble.intValue) return

    sendNavidromeGETRequest(
        navidromeServersList[selectedNavidromeServerIndex.intValue].url,
        navidromeServersList[selectedNavidromeServerIndex.intValue].username,
        navidromeServersList[selectedNavidromeServerIndex.intValue].password,
        "scrobble.view?id=${song.navidromeID}&submission=true"
    )
}