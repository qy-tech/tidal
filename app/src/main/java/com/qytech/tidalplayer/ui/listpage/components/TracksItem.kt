package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.listpage.model.SingleSong
import java.time.format.TextStyle

@Composable
fun TracksItem(
    item: SingleSong = SingleSong(),
    isCurrentTrack: Boolean = false,
    isPlaying: Boolean = false,
    isFavourite: Boolean = false,
    onClick: (SingleSong) -> Unit = {},
    onFavourite: (String, Boolean) -> Unit = { _, _ -> },
    onOtherOption: (SingleSong) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(400.dp)
            .height(80.dp)
            .background(
                color = if (isCurrentTrack) Color(0x1400e5ff) else Color(0x10ffffff),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isCurrentTrack) Color(0x4d00e5ff) else Color(0x12ffffff),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 10.dp)
            .padding(start = 10.dp, end = 0.dp)
            .clickable(onClick = { onClick.invoke(item) }),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
        ) {
            Box {
                AsyncImage(
                    model = item.coverUrl ?: "",
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

                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color(0x99000000),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                ImageVector.vectorResource(R.drawable.icon_pause_solid_full)
                            } else {
                                ImageVector.vectorResource(R.drawable.icon_play_solid_full)
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
            ) {
                Text(
                    text = item.getDetailTitle(),
                    color = if (isCurrentTrack) Color(0xff00E5FF) else Color.White,
                    fontWeight = FontWeight(600),
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = item.description ?: "No description",
                    color = Color(0xffA0A0A0),
                    fontWeight = FontWeight(500),
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


            IconButton(
                onClick = { onFavourite.invoke(item.id, isFavourite) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(if (isFavourite) R.drawable.icon_heart_solid_full else R.drawable.icon_heart_regular_full),
                    contentDescription = null,
                    tint = if (isFavourite) Color(0xff00E5FF) else Color(0xffA0A0A0),
                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                text = item.duration,
                color = Color(0xffA0A0A0),
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 12.sp
            )

            IconButton(
                onClick = { onOtherOption.invoke(item) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_ellipsis_solid_full),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color.White
                )
            }
        }
    }
}