package com.kaguya.music.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kaguya.music.R
import com.kaguya.music.data.Playlist
import com.kaguya.music.data.Screen
import com.kaguya.music.data.playlistList
import com.kaguya.music.providers.local.localPlaylistImageGenerator
import com.kaguya.music.saveManager
import com.kaguya.music.ui.elements.HorizontalLineWithNavidromeCheck
import com.kaguya.music.ui.elements.PlaylistGrid
import com.kaguya.music.ui.elements.dialogs.DeletePlaylist
import com.kaguya.music.ui.elements.dialogs.showDeletePlaylistDialog
import kotlinx.coroutines.delay

var selectedPlaylist by mutableStateOf<Playlist?>(Playlist("My Very Awesome Playlist With A Long Name", Uri.EMPTY))
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Preview(showBackground = true, showSystemUi = false)
@Composable
fun PlaylistScreen(navHostController: NavHostController = rememberNavController()) {
    val leftPadding = if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) 0.dp else 80.dp

    val state = rememberPullToRefreshState()

    val context = LocalContext.current

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            //saveManager(context).saveSettings()
            //delay(500)
            playlistList.clear()

            saveManager(context).loadPlaylists()

            delay(500)
            for (playlist in playlistList){
                if (playlist.navidromeID == "Local"){
                    val playlistImage = localPlaylistImageGenerator(playlist.songs, context) ?: Uri.EMPTY
                    playlist.coverArt = playlistImage
                }
            }
            state.endRefresh()
        }
    }



    /* RADIO ICON + TEXT */
    Box(modifier = Modifier
        .nestedScroll(state.nestedScrollConnection)
        .fillMaxWidth()
        .fillMaxHeight()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(
                start = leftPadding,
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )) {
            /* HEADER */
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.placeholder),
                    contentDescription = "Songs Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(48.dp))
                Text(
                    text = stringResource(R.string.playlists),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )
            }

            HorizontalLineWithNavidromeCheck()

            PlaylistGrid(playlistList , onPlaylistSelected = { playlist ->
                navHostController.navigate(Screen.PlaylistDetails.route) {
                    launchSingleTop = true
                }
                selectedPlaylist = playlist})

            if(showDeletePlaylistDialog.value)
                DeletePlaylist(setShowDialog =  { showDeletePlaylistDialog.value = it } )
        }
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = state,
        )
    }
}