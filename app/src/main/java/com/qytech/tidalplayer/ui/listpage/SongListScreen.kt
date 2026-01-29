package com.qytech.tidalplayer.ui.listpage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.listpage.components.DiscoveryMixContent
import com.qytech.tidalplayer.ui.listpage.components.ForYouContent
import com.qytech.tidalplayer.ui.listpage.components.MyMixContent
import com.qytech.tidalplayer.ui.listpage.components.MyPlaylistContent
import com.qytech.tidalplayer.ui.listpage.components.NewArrivalContent
import com.qytech.tidalplayer.ui.listpage.components.TabRowPill
import com.qytech.tidalplayer.ui.listpage.model.DataType
import com.qytech.tidalplayer.ui.listpage.model.ItemType
import com.qytech.tidalplayer.ui.listpage.model.PageItem
import com.qytech.tidalplayer.ui.listpage.model.SubItem
import kotlinx.coroutines.launch

@Composable
fun SongListScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val pageList = remember {
        listOf(
            PageItem(
                "For You",
                arrayListOf(
                    SubItem("Playlists", ItemType.SONG_LIST, DataType.PLAY_LIST),
                    SubItem("Albums", ItemType.SONG_LIST, DataType.ALBUM),
                    SubItem("Tracks", ItemType.TRACK, DataType.TRACK),
                    SubItem("Artists", ItemType.ARTIST, DataType.ARTIST)
                )
            ),
            PageItem("Playlist", emptyList()),
            PageItem(
                title = "Mix", emptyList()
            ),
            PageItem(
                title = "Discovery", emptyList()
            ),
            PageItem(
                title = "New Arrivals", emptyList()
            )
        )
    }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pageList.size })

    BackHandler {
        onBack.invoke()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xff080808)
            )
            .padding(10.dp)
            .padding(start = 15.dp)
    ) {
        // 头部
        ScreenHeader(
            tabs = pageList,
            selected = pageList[pagerState.currentPage],
            onSelect = { item ->
                scope.launch {
                    pagerState.scrollToPage(pageList.indexOf(item))
                }
            },
            labelProvider = { it.title },
            onSearch = {
                navController.navigate(TidalRoute.SEARCH_SONG)
            },
            onUserInfo = {
                navController.navigate(TidalRoute.USER_INFO)
            }
        )

        Spacer(modifier = Modifier.size(10.dp))

        // 内容
        HorizontalPager(
            state = pagerState,
            // 关键点2: 设置页面间距
            pageSpacing = 24.dp,
            // 关键点3: 预加载 (左右各多加载1页，保证滑动时不白屏)
            beyondViewportPageCount = 1,
            userScrollEnabled = false, // 左右滑动取消
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            val item = pageList[page]
            when (page) {
                0 -> {
                    ForYouContent(
                        item = item,
                        navController = navController
                    )
                }
                1 -> {
                    MyPlaylistContent(
                        navController = navController
                    )
                }
                2 -> {
                    MyMixContent(
                        navController = navController
                    )
                }
                3 -> {
                    DiscoveryMixContent(
                        navController = navController
                    )
                }
                4 -> {
                    NewArrivalContent(
                        navController = navController
                    )
                }
            }
        }

    }
}

@Composable
private fun <T> ScreenHeader(
    tabs: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelProvider: (T) -> String = { it.toString() },
    onSearch: () -> Unit = {},
    onUserInfo: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_wave_square_solid_full),
                    contentDescription = null,
                    tint = Color(0xff080808),
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = "MUSIC",
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                color = Color.White,
                fontWeight = FontWeight(800),
                fontSize = 25.sp,
                letterSpacing = 3.sp
            )
        }
        // pill
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TabRowPill(
                tabs = tabs,
                selected = selected,
                onSelect = onSelect,
                labelProvider = labelProvider
            )
        }

        // user
        Row() {
            // 查询
            IconButton(
                onClick = onSearch
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_magnifying_glass_solid_full),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            // 用户信息
            IconButton(
                onClick = onUserInfo
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_user_solid_full),
                    contentDescription = null,
                    tint = Color(0xffEFAF6B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}