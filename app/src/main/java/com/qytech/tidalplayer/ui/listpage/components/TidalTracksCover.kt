package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qytech.tidalplayer.R

// 定义 Tidal 风格色值
val TidalAccent = Color(0xFF00E5FF)
val DeepBlueStart = Color(0xFF004E92)
val DeepBlueEnd = Color(0xFF000428)

@Composable
fun TidalTracksCover(
    modifier: Modifier = Modifier,
    size: Dp = 280.dp, // 默认大图尺寸
    subtitle: String = "COLLECTION",
    title: String = "TRACKS",
    icon: ImageVector = ImageVector.vectorResource(R.drawable.icon_music_solid_full)
) {
    Box(
        modifier = modifier
            .size(size)
            // 1. 圆角裁剪
            .clip(RoundedCornerShape(16.dp))
            // 2. 核心渐变背景 (对应 CSS: linear-gradient(135deg, #004e92, #000428))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DeepBlueStart, DeepBlueEnd),
                    start = Offset.Zero, // 左上
                    end = Offset.Infinite // 右下
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 3. 背景装饰圆圈 (对应 CSS 中的 absolute div border-radius:50%)
        Box(
            modifier = Modifier
                .size(size * 0.7f) // 圆圈大小约为封面的 70%
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.05f), // 极淡的白色描边
                    shape = CircleShape
                )
        )

        // 4. 中间内容层
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TidalAccent.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(size * 0.22f) // 图标大小随封面缩放
                    .padding(bottom = 12.dp)
            )

            // 副标题 (COLLECTION)
            Text(
                text = subtitle.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (size.value * 0.05).sp, // 动态字号 14sp @ 280dp
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp // 宽字间距
            )
            
            // 主标题 (TRACKS)
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = (size.value * 0.12).sp, // 动态字号 32sp @ 280dp
                fontWeight = FontWeight.Black, // 最粗字体
                letterSpacing = 2.sp,
                lineHeight = (size.value * 0.12).sp
            )
        }
    }
}

// --- 预览 ---
@Preview
@Composable
fun PreviewTidalCover() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. 大图模式 (详情页用)
        TidalTracksCover(
            size = 280.dp,
            subtitle = "MY COLLECTION",
            title = "TRACKS"
        )
        
        // 2. 列表小图模式 (Quick Picks 列表用)
        // 只需要改 size，字体和图标会自动按比例缩小
        TidalTracksCover(
            size = 70.dp,
            subtitle = "MY",
            title = "LIKED"
        )
    }
}