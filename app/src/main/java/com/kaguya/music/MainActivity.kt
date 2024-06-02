package com.kaguya.music

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaguya.music.data.bottomNavigationItems
import com.kaguya.music.player.MediaLibraryService
import com.kaguya.music.player.SongHelper
import com.kaguya.music.player.rememberManagedMediaController
import com.kaguya.music.ui.NowPlayingContent
import com.kaguya.music.ui.dpToPx
import com.kaguya.music.ui.elements.bounceClick
import com.kaguya.music.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 用于保存当前播放的歌曲的索引
 */
var sliderPos = mutableIntStateOf(0)

/**
 * 重复播放
 */
var repeatSong = mutableStateOf(false)

/**
 * 随机播放
 */
var shuffleSongs = mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    lateinit var navController: NavHostController

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        /**
         * 开启 MediaLibraryService
         */
        val serviceIntent = Intent(applicationContext, MediaLibraryService::class.java)
        this.startService(serviceIntent)

        super.onCreate(savedInstanceState)

        /**
         * 设置窗口是否适应系统窗口，例如状态栏或导航栏
         * 如果为true，则内容将延伸到系统窗口的边缘。如果为false，则内容将延伸到屏幕的边缘。
         */
        WindowCompat.setDecorFitsSystemWindows(window, false)

        /**
         * BottomSheetScaffoldState 是一个包含底部抽屉的状态的类。
         * 它包含一个 BottomSheetState 和一个 SnackbarHostState。
         * BottomSheetState 是一个包含底部抽屉状态的类。
         * 它包含一个 targetValue 和一个 currentValue。
         */
        val scaffoldState = BottomSheetScaffoldState(
            bottomSheetState = SheetState(
                skipPartiallyExpanded = false,
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = true
            ),
            snackbarHostState = SnackbarHostState()
        )

        /**
         * SnackbarHostState 这是一个包含 Snackbar 状态的类。
         * 用来管理 Snackbar 的显示和隐藏。
         * Snackbar 是一个显示短消息的界面元素。
         */
        val snackbarHostState = SnackbarHostState()

        setContent {

            MusicPlayerTheme {
                /**
                 * 底部导航 + NOW-PLAYING UI
                 */
                navController = rememberNavController()

                /**
                 * 用于管理媒体控制器
                 */
                val mediaController = rememberManagedMediaController()

                /**
                 * 用于保存当前选中的底部导航项的索引
                 */
                var selectedItemIndex by rememberSaveable{ mutableIntStateOf(0) }


                /**
                 * 用于处理协程
                 */
                val coroutineScope = rememberCoroutineScope()

                /**
                 * 用于处理全屏时的位移
                 * 当底部抽屉处于展开状态时，将其向上移动以避免与底部导航栏重叠
                 */
                val yTrans by animateIntAsState(
                    targetValue =
                    if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded)
                        dpToPx(-80 - WindowInsets.navigationBars
                                                     .asPaddingValues()
                                                     .calculateBottomPadding()
                                                     .value.toInt())
                    else 0,
                    label = "Fullscreen Translation"
                )

                /**
                 * 主界面
                 */
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    //color = MaterialTheme.colorScheme.background
                    color = Color.Transparent
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            val backStackEntry = navController.currentBackStackEntryAsState()
                            if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE){
                                NavigationBar (
                                    modifier = Modifier
                                    .offset{ IntOffset(x=0, y= -yTrans) },
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Transparent,
                                ) {
                                    bottomNavigationItems.forEachIndexed { index, item ->
                                        if (!item.enabled) return@forEachIndexed
                                        NavigationBarItem(
                                            selected = item.screenRoute == backStackEntry.value?.destination?.route,
                                            modifier = Modifier.bounceClick(),
                                            onClick = {
                                                selectedItemIndex = index
                                                navController.navigate(item.screenRoute) {
                                                    launchSingleTop = true
                                                }
                                                coroutineScope.launch {
                                                    scaffoldState.bottomSheetState.partialExpand()
                                                } },
                                            label = { Text(text = item.title) },
                                            alwaysShowLabel = false,
                                            icon = {
                                                Icon(ImageVector.vectorResource(item.icon),contentDescription = item.title)
                                            }
                                        )
                                    }
                                }
                            }
                            else{
                                NavigationRail {
                                    bottomNavigationItems.forEachIndexed { index, item ->
                                        if (!item.enabled) return@forEachIndexed
                                        NavigationRailItem(
                                            selected = item.screenRoute == backStackEntry.value?.destination?.route,
                                            onClick = {
                                                selectedItemIndex = index
                                                navController.navigate(item.screenRoute) {
                                                    launchSingleTop = true
                                                }
                                                coroutineScope.launch {
                                                    scaffoldState.bottomSheetState.partialExpand()
                                                } },
                                            label = { Text(text = item.title) },
                                            alwaysShowLabel = false,
                                            icon = {
                                                Icon(ImageVector.vectorResource(item.icon),contentDescription = item.title)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        paddingValues -> SetupNavGraph(navController = navController, paddingValues, mediaController.value)
                        BottomSheetScaffold(
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredWidth(LocalConfiguration.current.screenWidthDp.dp),
                            sheetContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                            sheetPeekHeight =
                            if (SongHelper.currentSong.title == "" &&
                                SongHelper.currentSong.duration == 0 &&
                                SongHelper.currentSong.imageUrl == Uri.EMPTY)
                                0.dp // Hide Mini-player if empty
                            else if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE){
                                72.dp + 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            }
                            else {
                                72.dp
                            },
                            sheetShadowElevation = 4.dp,
                            sheetShape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                            sheetDragHandle = { },
                            scaffoldState = scaffoldState,
                            sheetContent = {
                                NowPlayingContent(
                                    context = this@MainActivity,
                                    scaffoldState = scaffoldState,
                                    snackbarHostState = snackbarHostState,
                                    navHostController = navController,
                                    mediaController = mediaController.value
                                )
                            }) {
                        }
                    }
                }
            }
        }

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                Log.d("PERMISSIONS", "Is 'READ_MEDIA_AUDIO' permission granted? $isGranted")
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        }
        else {
            requestPermissionLauncher.launch(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        // SAVE SETTINGS ON APP EXIT
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityPreStopped(activity: Activity) {
                saveManager(this@MainActivity).saveSettings()
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                MediaLibraryService().onDestroy()
                println("Destroyed, Goodbye :(")
            }
        })

    }
}

fun formatMilliseconds(milliseconds: Float): String {
    val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    return format.format(Date(milliseconds.toLong()))
}
fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
