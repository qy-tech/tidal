package com.qytech.tidalplayer.ui.listpage

import android.text.format.DateUtils
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.components.CustomThinSlider
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import com.qytech.tidalplayer.utils.ToastUtils
import com.tidal.sdk.player.playbackengine.model.PlaybackState
import timber.log.Timber

@Composable
fun ListStartScreen(
    parentNavController: NavHostController,
    onBack: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: ListPageViewModel = hiltViewModel()
    val checkAuth by viewModel.checkAuth.collectAsState()
    val controllerUiState by viewModel.controllerUiState.collectAsState()
    val showController by remember {
        derivedStateOf {
            controllerUiState.showController
        }
    }
    val isPlaying by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.PLAYING
        }
    }
    val isBuffering by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.STALLED
        }
    }
    val isPlayerIdle by remember {
        derivedStateOf {
            controllerUiState.playbackState == PlaybackState.IDLE
        }
    }
    val currentSong by remember {
        derivedStateOf {
            controllerUiState.currentSong
        }
    }
    val favouriteTracks by viewModel.collectionTrackIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAuth()
    }

    LaunchedEffect(checkAuth) {
        if (!checkAuth) {
            ToastUtils.show("权限不足，重新登录")
            viewModel.logout()
            parentNavController.popBackStack(
                route = TidalRoute.LOGIN_START,
                inclusive = false
            )
        }
    }

    ConstraintLayout() {
        val (nav, controller, floatButton) = createRefs()

        NavHost(
            navController = navController,
            startDestination = TidalRoute.SONG_LIST,
            modifier = Modifier.constrainAs(nav) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            composable(TidalRoute.SONG_LIST) {
                SongListScreen(
                    navController = navController,
                    onBack = onBack
                )
            }
            composable(TidalRoute.SEARCH_SONG) {

            }
            composable(TidalRoute.USER_INFO) {

            }
            composable(
                route = TidalRoute.ITEM_TRACK_LIST,
                arguments = listOf(
                    navArgument("listId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("dataType") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("coverUrl") {
                        type = NavType.StringType
                    },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("description") {
                        type = NavType.StringType
                    }
                )
            ) {
                val listId = it.arguments?.getString("listId") ?: ""
                val dataType = it.arguments?.getInt("dataType") ?: -1
                val coverUrl = it.arguments?.getString("coverUrl")
                val title = it.arguments?.getString("title") ?: ""
                val description = it.arguments?.getString("description")
                ItemTrackListScreen(
                    navController = navController,
                    listId = listId,
                    dataType = dataType,
                    coverUrl = coverUrl,
                    title = title,
                    description = description
                )
            }
            composable(
                route = TidalRoute.PLAYLIST_ALBUM_LIST,
                arguments = listOf(
                    navArgument("artistId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("dataType") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                )
            ) {
                val artistId = it.arguments?.getString("artistId") ?: ""
                val dataType = it.arguments?.getInt("dataType") ?: -1
                val title = it.arguments?.getString("title") ?: ""
                PlaylistAlbumScreen(
                    navController = navController,
                    artistId = artistId,
                    title = title,
                    dataType = dataType
                )
            }
            composable(
                route = TidalRoute.ARTIST_LIST,
                arguments = listOf(
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                )
            ) {
                val title = it.arguments?.getString("title") ?: ""
                ArtistScreen(
                    navController = navController,
                    title = title
                )
            }
        }

        if (showController) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .constrainAs(controller) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
//                    .background(
//                        color = Color.Red
//                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {}
            ) {
                val content = createRef()
                MusicController(
                    currentSong = currentSong,
                    isIdle = isPlayerIdle,
                    isPlaying = isPlaying,
                    isFavourite = { id -> favouriteTracks.contains(id) },
                    isBuffering = isBuffering,
                    sliderEnabled = !isPlayerIdle,
                    progress = controllerUiState.currentProgress,
                    totalProgress = controllerUiState.totalProgress,
                    sliderValueRange = 0f..controllerUiState.totalProgress.coerceAtLeast(0f),
                    modifier = Modifier
                        .constrainAs(content) {
                            top.linkTo(parent.top, 30.dp)
                            bottom.linkTo(parent.bottom, 15.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .padding(horizontal = 40.dp),
                    onSliderValueChanged = { value ->
                        viewModel.setDragProgress(value)
                    },
                    onSliderValueChangedFinish = {
                        viewModel.setDragProgress(null)
                    },
                    onPlayOrPause = {
                        val state = controllerUiState.playbackState
                        if (state != null && state != PlaybackState.IDLE) {
                            if (isPlaying || isBuffering) {
                                viewModel.pauseSong()
                            } else {
                                viewModel.playSong()
                            }
                        }
                    },
                    onForward = {
                        val state = controllerUiState.playbackState
                        if (state != null && state != PlaybackState.IDLE) {
                            viewModel.beforeSong()
                        }
                    },
                    onBackward = {
                        val state = controllerUiState.playbackState
                        if (state != null && state != PlaybackState.IDLE) {
                            viewModel.nextSong()
                        }
                    },
                    onHidden = {
                        viewModel.setControllerShow(false)
                    },
                    onFavourite = { trackId, isFavourite ->
                        val state = controllerUiState.playbackState
                        if (state != null && state != PlaybackState.IDLE) {
                            if (isFavourite) {
                                viewModel.removeTrackToCollection(trackId)
                            } else {
                                viewModel.addTrackToCollection(trackId)
                            }
                        }

                    }
                )
            }
        } else {
            PulsingMusicButton(
                modifier = Modifier.constrainAs(floatButton) {
                    end.linkTo(parent.end, 50.dp)
                    bottom.linkTo(parent.bottom, 50.dp)
                },
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                onClick = {
                    viewModel.setControllerShow(true)
                }
            )
        }

    }
}

@Preview
@Composable
private fun MusicController(
    modifier: Modifier = Modifier,
    currentSong: SingleSong = SingleSong(),
    isIdle: Boolean = true,
    isPlaying: Boolean = false,
    isFavourite: (String) -> Boolean = {false},
    isBuffering: Boolean = false,
    progress: Float = 0f,
    totalProgress: Float = 0f,
    sliderEnabled: Boolean = false,
    sliderValueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onSliderValueChanged: (Float) -> Unit = {},
    onSliderValueChangedFinish: () -> Unit = {},
    onPlayOrPause: () -> Unit = {},
    onForward: () -> Unit = {},
    onBackward: () -> Unit = {},
    onHidden: () -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = {_, _ ->}
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .border(
                width = 1.dp,
                color = Color(0x30ffffff),
                shape = RoundedCornerShape(15.dp)
            )
            .background(
                color = Color(0xff1a1a1a),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 5.dp),
    ) {
        val (controller, slider) = createRefs()

        CustomThinSlider(
            enabled = sliderEnabled,
            currentValue = progress,
            valueRange = sliderValueRange,
            onValueChange = onSliderValueChanged,
            onValueChangeFinished = onSliderValueChangedFinish,
            modifier = Modifier
                .constrainAs(slider) {
                    top.linkTo(parent.top, (-28).dp)
                    start.linkTo(parent.start)
                }
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )

        Row(
            modifier = Modifier
                .constrainAs(controller) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isIdle) {
                AsyncImage(
                    model = currentSong.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(
                            shape = RoundedCornerShape(10.dp)
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp) // 你的封面尺寸
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Black,       // 左上：黑 (阴影)
                                    Color(0xFF111111),          // 中间：基础色
                                    Color(0xFF333333)       // 右下：亮 (反光)
                                ),
                                start = Offset.Zero,
                                end = Offset.Infinite // 对角线方向
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(
                            shape = RoundedCornerShape(10.dp)
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = false
                        )
                        // 加一个极细的边框，增强边界感
                        .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                ) {
                    // 中间的图标
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_layer_group_solid_full), // 这里可以用你的叠叠乐图标
                        contentDescription = null,
                        tint = Color(0xFF444444), // 图标颜色要暗淡一点，像嵌在里面
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            ) {
                Text(
                    text = if (isIdle) "TIDAL" else currentSong.getDetailTitle(),
                    color = Color.White,
                    fontWeight = FontWeight(600),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = if (isIdle) "Select a track to play" else currentSong.description
                        ?: "No description",
                    color = Color(0xffA0A0A0),
                    fontWeight = FontWeight(500),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row() {
                Text(
                    text = DateUtils.formatElapsedTime(progress.toLong()),
                    color = Color(0xffA0A0A0),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 12.sp
                )
                Text(
                    text = " / ",
                    color = Color(0xffA0A0A0),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 12.sp
                )
                Text(
                    text = DateUtils.formatElapsedTime(totalProgress.toLong()),
                    color = Color(0xffA0A0A0),
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(50.dp)
            )
            Controller(
                isPlaying = isPlaying,
                isFavourite = isFavourite.invoke(currentSong.id),
                isBuffering = isBuffering,
                onPlayOrPause = onPlayOrPause,
                onForward = onForward,
                onBackward = onBackward,
                onFavourite = {
                    onFavourite.invoke(currentSong.id, isFavourite.invoke(currentSong.id))
                }
            )
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(50.dp)
            )

            Box(
                modifier = Modifier
                    .background(
                        color = Color(0x0dffffff),
                        shape = CircleShape
                    )
                    .size(35.dp)
                    .clickable(onClick = onHidden),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_chevron_down_solid_full),
                    contentDescription = null,
                    tint = Color(0xffA0A0A0),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

}

@Composable
private fun Controller(
    isPlaying: Boolean = false,
    isFavourite: Boolean = false,
    isBuffering: Boolean = false,
    onForward: () -> Unit = {},
    onPlayOrPause: () -> Unit = {},
    onBackward: () -> Unit = {},
    onFavourite: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.background(
            color = Color.Transparent
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .clickable(onClick = onForward),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_backward_step_solid_full),
                contentDescription = null,
                tint = Color(0xffA0A0A0),
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(10.dp)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xffA0A0A0),
                    shape = CircleShape
                )
                .clickable(onClick = onPlayOrPause),
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                val infiniteTransition = rememberInfiniteTransition()
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing)
                    )
                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_spinner_solid_full),
                    contentDescription = null,
                    tint = Color(0xff080808),
                    modifier = Modifier
                        .size(25.dp)
                        .rotate(angle)
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(if (isPlaying) R.drawable.icon_pause_solid_full else R.drawable.icon_play_solid_full),
                    contentDescription = null,
                    tint = Color(0xff080808),
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(10.dp)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .clickable(onClick = onBackward),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_forward_step_solid_full),
                contentDescription = null,
                tint = Color(0xffA0A0A0),
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(10.dp)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .clickable(onClick = { onFavourite.invoke(isFavourite) }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(if (isFavourite) R.drawable.icon_heart_solid_full else R.drawable.icon_heart_regular_full),
                contentDescription = null,
                tint = if (isFavourite) TidalCyan else Color(0xffA0A0A0),
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

// 定义你的主题色
val TidalCyan = Color(0xFF00E5FF)
val DarkBg = Color(0xFF1A1A1A)

//@Preview
@Composable
private fun PulsingMusicButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    onClick: () -> Unit = {}
) {
    // 1. 创建无限动画控制器
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    // 2. 定义缩放动画 (Scale): 1.0 -> 1.15 -> 1.0
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.15f else 1f, // 不播放时保持 1f
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing), // 1秒一次呼吸
            repeatMode = RepeatMode.Reverse // 往复运动
        ),
        label = "scale_anim"
    )

    // 3. 定义光晕半径动画 (Glow Radius): 0px -> 扩大
    // 使用 Float 而不是 Dp，方便在 drawBehind 中计算像素
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 0.5f else 0f, // 不播放时透明
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_anim"
    )

//    // 3. 【新增】旋转动画 (唱片感): 0度 -> 360度
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            // 旋转慢一点比较优雅，6秒转一圈
            // 关键：必须用 LinearEasing (线性)，否则转一圈会卡顿一下
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart // 这里用 Restart，让它一直转下去，不要倒着转
        ),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(60.dp) // 给外层留够空间，防止光晕被裁剪
            .border(
                width = 3.dp,
                color = Color(0x30ffffff),
                shape = CircleShape
            )
            .background(
                color = Color(0xff1a1a1a),
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                // 应用缩放动画
                .scale(scale)
                // 绘制霓虹光晕 (关键步骤)
                .drawBehind {
                    // 如果不播放，完全不绘制光晕，节省性能
                    if (glowAlpha > 0) {
                        val radius = size.maxDimension / 2f * 1.5f // 光晕比按钮大 1.5 倍

                        // 使用径向渐变模拟发光：中心是主题色，向外渐变到透明
                        val brush = Brush.radialGradient(
                            colors = listOf(
                                TidalCyan.copy(alpha = glowAlpha), // 中心亮
                                TidalCyan.copy(alpha = 0f)         // 边缘透明
                            ),
                            radius = radius
                        )

                        drawCircle(
                            brush = brush,
                            radius = radius
                        )
                    }
                }
                .graphicsLayer {
                    rotationZ = rotation
                }
                // 按钮本体背景
                .size(42.dp)
                .clip(CircleShape)
                .background(DarkBg)
                .padding(9.dp) // 图标内边距
        ) {
            if (isBuffering) {
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing)
                    )
                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_spinner_solid_full),
                    contentDescription = null,
                    tint = TidalCyan,
                    modifier = Modifier
                        .matchParentSize()
                        .rotate(angle)
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_music_solid_full),
                    contentDescription = "Show Player",
                    tint = TidalCyan,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}