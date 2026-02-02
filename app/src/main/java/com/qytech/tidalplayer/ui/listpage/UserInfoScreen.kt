package com.qytech.tidalplayer.ui.listpage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.qytech.tidal.data.UserInfo
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.components.CreateNewPlaylistDialog
import com.qytech.tidalplayer.ui.listpage.components.FullScreenLoading
import com.qytech.tidalplayer.ui.listpage.components.HandlePagingError
import com.qytech.tidalplayer.ui.listpage.components.RequestOrigin
import com.qytech.tidalplayer.ui.listpage.components.TipDialog
import com.qytech.tidalplayer.ui.listpage.components.TipDialogBean
import com.qytech.tidalplayer.ui.listpage.model.ChannelType
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.SongList
import com.qytech.tidalplayer.utils.ToastUtils
import com.qytech.tidalplayer.utils.popBackSafely
import kotlinx.coroutines.launch

@Composable
fun UserInfoScreen(
    parentNavController: NavController,
    navController: NavController
) {
    val viewModel: ListPageViewModel = hiltViewModel()
    val userInfo by remember { mutableStateOf(viewModel.getUserInfo()) }
    val userPlaylist = viewModel.myPlaylistPagingData.collectAsLazyPagingItems()
    var playlistCount by remember { mutableIntStateOf(userPlaylist.itemCount) }
    var showCreateNewDialog by remember { mutableStateOf(false) }
    var showTipDialog by remember { mutableStateOf(false) }
    val showLoading by viewModel.showLoading.collectAsState()
    var tipDialogBean by remember { mutableStateOf(TipDialogBean()) }
    val scope = rememberCoroutineScope()
    val deletePlaylistIds by viewModel.deletePlaylistIds.collectAsState()
    val createPlaylist by viewModel.createPlaylist.collectAsState()

    HandlePagingError(userPlaylist)

    LaunchedEffect(Unit) {
        viewModel.operationChannel.collect { type ->
            when (type) {
                is ChannelType.DeletePlaylist -> {
                    if (type.result) {
                        playlistCount--
                    }
                }
                is ChannelType.CreatePlaylist -> {
                    if (type.result) {
                        playlistCount++
                    }
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(userPlaylist.itemCount) {
        playlistCount = userPlaylist.itemCount + createPlaylist.size - deletePlaylistIds.size
        if (userPlaylist.loadState.refresh is LoadState.NotLoading
            && !userPlaylist.loadState.append.endOfPaginationReached
            && userPlaylist.itemCount > 0
        ) {
            userPlaylist[userPlaylist.itemCount - 1]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black
            )
            .padding(horizontal = 25.dp)
    ) {
        // 退出按钮
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.size(20.dp))
            // 退出按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .clickable(onClick = {
                        navController.popBackSafely()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_xmark_solid_full),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
        LazyColumn() {
            // 用户信息
            item {
                UserInfoContent(
                    userInfo = userInfo,
                    playlistCount = playlistCount,
                    onLogout = {
                        tipDialogBean = tipDialogBean.copy(
                            title = "Log Out",
                            tipText = "Are you sure you want to log out of your account?",
                            confirmBtnText = "Log Out",
                            confirmBtnColor = Color(0xffff3b30),
                            requestOrigin = RequestOrigin.LOG_OUT
                        )
                        showTipDialog = true
                    }
                )
            }
            // 提示
            item {
                // 间隔线
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                )
                Spacer(modifier = Modifier.size(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Your Playlists",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight(600),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                            .background(
                                color = Color(0xff00E5FF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                onClick = {
                                    showCreateNewDialog = true
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.icon_plus_solid_full),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Create New",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight(800)
                        )
                    }
                }
                // 间隔线
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                )
                Spacer(modifier = Modifier.size(10.dp))
                val textColor = Color(0xffA0A0A0)
                val spec = 15.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
                    Text(
                        text = "COVER",
                        color = textColor,
                        modifier = Modifier
                            .width(55.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .width(spec)
                    )
                    Text(
                        text = "NAME",
                        color = textColor,
                        modifier = Modifier
                            .weight(1.5f)
                    )
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .width(spec)
                    )
                    Text(
                        text = "TYPE",
                        color = textColor,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .width(spec)
                    )
                    Text(
                        text = "ACTION",
                        color = textColor,
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            // 主要内容
            items(
                count = createPlaylist.size,
                key = { index -> createPlaylist[index].id }
            ) { index ->
                val item = createPlaylist[index]
                item.apply {
                    val isDeleted = deletePlaylistIds.contains(item.id)
                    if (isDeleted) return@apply
                    CustomPlaylistItem(
                        playlist = item,
                        onSeeAll = { list ->
                            navController.navigate(
                                TidalRoute.getItemTrackListRoute(
                                    listId = list.id,
                                    dataType = list.dataType,
                                    coverUrl = list.coverUrl,
                                    title = list.title,
                                    description = list.description
                                )
                            )
                        },
                        onDelete = { list ->
                            tipDialogBean = tipDialogBean.copy(
                                title = "Delete Playlist",
                                tipText = "Are you sure you want to delete '${list.title}' playlist? This action cannot be undone.",
                                confirmBtnText = "Delete",
                                confirmBtnColor = Color(0xffff3b30),
                                requestOrigin = RequestOrigin.DELETE_PLAYLIST,
                                data = list.id
                            )
                            showTipDialog = true
                        }
                    )
                }

            }

            items(
                count = userPlaylist.itemCount,
                key = { index -> userPlaylist[index]?.id ?: "_${index}_" }
            ) { index ->
                val item = userPlaylist[index]
                item?.apply {
                    val isDeleted = deletePlaylistIds.contains(item.id)
                    if (isDeleted) return@apply
                    CustomPlaylistItem(
                        playlist = item,
                        onSeeAll = { list ->
                            navController.navigate(
                                TidalRoute.getItemTrackListRoute(
                                    listId = list.id,
                                    dataType = list.dataType,
                                    coverUrl = list.coverUrl,
                                    title = list.title,
                                    description = list.description
                                )
                            )
                        },
                        onDelete = { list ->
                            tipDialogBean = tipDialogBean.copy(
                                title = "Delete Playlist",
                                tipText = "Are you sure you want to delete '${list.title}' playlist? This action cannot be undone.",
                                confirmBtnText = "Delete",
                                confirmBtnColor = Color(0xffff3b30),
                                requestOrigin = RequestOrigin.DELETE_PLAYLIST,
                                data = list.id
                            )
                            showTipDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showTipDialog) {
        TipDialog(
            bean = tipDialogBean,
            onDismiss = {
                showTipDialog = false
            },
            onCancel = {
                showTipDialog = false
            },
            onConfirm = { bean ->
                showTipDialog = false
                when (bean.requestOrigin) {
                    RequestOrigin.LOG_OUT -> {
                        scope.launch {
                            ToastUtils.show("登出成功")
                            viewModel.logout()
                            parentNavController.popBackStack(
                                route = TidalRoute.LOGIN_START,
                                inclusive = false
                            )
                        }
                    }

                    RequestOrigin.DELETE_PLAYLIST -> {
                        val listId = bean.data
                        if (listId is String && listId.isNotEmpty()) {
                            viewModel.deletePlaylist(listId)
                        } else {
                            ToastUtils.show("删除失败，歌单id为空")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    CreateNewPlaylistDialog(
        showState = showCreateNewDialog,
        onDismiss = {
            showCreateNewDialog = false
        },
        onCancel = {
            showCreateNewDialog = false
        },
        onConfirm = { name, description ->
            showCreateNewDialog = false
            viewModel.createPlaylist(name, description)
        }
    )

    FullScreenLoading(showLoading)
}

@Composable
private fun CustomPlaylistItem(
    playlist: SongList = SongList(),
    onSeeAll: (SongList) -> Unit = {},
    onDelete: (SongList) -> Unit = {}
) {
    val spec = 15.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(55.dp),
            contentAlignment = Alignment.Center
        ) {
            if (playlist.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.default_playlist_cover_160x160)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(
                            shape = RoundedCornerShape(5.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = playlist.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(
                            shape = RoundedCornerShape(5.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.5f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playlist.title,
                fontSize = 15.sp,
                fontWeight = FontWeight(700),
                color = Color.White
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Custom",
                fontSize = 15.sp,
                fontWeight = FontWeight(500),
                color = Color(0xffA0a0a0)
            )
        }
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .width(spec)
        )
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onSeeAll.invoke(playlist) },
                modifier = Modifier
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_eye_solid_full),
                    contentDescription = null,
                    tint = Color(0xffA0a0a0),
                )
            }
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .width(10.dp)
            )
            IconButton(
                onClick = { onDelete.invoke(playlist) },
                modifier = Modifier
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_trash_solid_full),
                    contentDescription = null,
                    tint = Color(0xffA0a0a0),
                )
            }
        }
    }
    Spacer(modifier = Modifier.size(10.dp))

}

@Composable
fun UserInfoContent(
    userInfo: UserInfo?,
    playlistCount: Int = 0,
    onLogout: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(shape = CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xffff0080),
                            Color(0xff7928CA)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .border(
                    width = 4.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        ) {}
        Spacer(modifier = Modifier.size(25.dp))
        Column(
            modifier = Modifier
        ) {
            Text(
                text = userInfo?.userName ?: "Unknown",
                color = Color.White,
                fontWeight = FontWeight(800),
                fontSize = 50.sp
            )
            Spacer(modifier = Modifier.size(5.dp))
            // 图标
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_envelope_solid_full),
                        contentDescription = null,
                        tint = Color(0xff00E5FF),
                        modifier = Modifier
                            .size(25.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = userInfo?.email ?: "Unknown",
                        color = Color(0xffA0A0A0),
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.size(30.dp))
                Row {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_list_solid_full),
                        contentDescription = null,
                        tint = Color(0xff00E5FF),
                        modifier = Modifier
                            .size(25.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "$playlistCount Custom Playlist",
                        color = Color(0xffA0A0A0),
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.size(30.dp))
                Row {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_hashtag_solid_full),
                        contentDescription = null,
                        tint = Color(0xff00E5FF),
                        modifier = Modifier
                            .size(25.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "ID: ${userInfo?.id ?: "******"}",
                        color = Color(0xffA0A0A0),
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(15.dp))
            Row() {
                // 设置按钮
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(60.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(60.dp)
                        )
                        .clickable(onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    Row() {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.icon_gear_solid_full),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Settings",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight(800)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                // 登出按钮
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(50.dp)
                        .background(
                            color = Color(0xffff3b30).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(60.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xffff3b30),
                            shape = RoundedCornerShape(60.dp)
                        )
                        .clickable(onClick = onLogout),
                    contentAlignment = Alignment.Center
                ) {
                    Row() {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.icon_right_from_bracket_solid_full),
                            contentDescription = null,
                            tint = Color(0xffff3b30),
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Log Out",
                            color = Color(0xffff3b30),
                            fontSize = 20.sp,
                            fontWeight = FontWeight(800)
                        )
                    }
                }
            }

        }
    }
}