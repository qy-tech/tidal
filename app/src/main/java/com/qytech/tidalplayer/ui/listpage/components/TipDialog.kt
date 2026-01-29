package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class RequestOrigin {
    NONE, LOG_OUT, DELETE_PLAYLIST
}

data class TipDialogBean(
    val title: String = "Tips",
    val tipText: String = "This is a tips.",
    val confirmBtnText: String = "Confirm",
    val confirmBtnColor: Color = Color(0xff30C4FF),
    val cancelBtnText: String = "Cancel",
    val cancelBtnColor: Color = Color.White.copy(alpha = 0.08f),
    val requestOrigin: RequestOrigin = RequestOrigin.NONE,
    val data: Any? = null
)

@Composable
fun TipDialog(
    bean: TipDialogBean = TipDialogBean(),
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirm: (TipDialogBean) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        TipDialogContent(
            title = bean.title,
            tipText = bean.tipText,
            confirmBtnText = bean.confirmBtnText,
            confirmBtnColor = bean.confirmBtnColor,
            cancelBtnText = bean.cancelBtnText,
            cancelBtnColor = bean.cancelBtnColor,
            onCancel = onCancel,
            onConfirm = { onConfirm.invoke(bean) }
        )
    }
}

//@Preview(
//    showBackground = true,
//    widthDp = 1080,
//    heightDp = 640
//)
@Composable
private fun TipDialogContent(
    title: String = "Tips",
    tipText: String = "This is a tips.",
    confirmBtnText: String = "Confirm",
    confirmBtnColor: Color = Color(0xff30C4FF),
    cancelBtnText: String = "Cancel",
    cancelBtnColor: Color = Color.White.copy(alpha = 0.1f),
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(500.dp)
            .wrapContentHeight()
            .background(
                color = Color(0xff161616),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 30.dp)
            .padding(top = 35.dp, bottom = 30.dp)

    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight(800),
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = tipText,
            color = Color(0xffa0a0a0),
            fontWeight = FontWeight(500),
            fontSize = 24.sp,
            lineHeight = 30.sp
        )
        Spacer(modifier = Modifier.size(20.dp))
        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.End
        ) {
            // 取消
            Box(
                modifier = Modifier
                    .background(
                        color = cancelBtnColor,
                        shape = RoundedCornerShape(100)
                    )
                    .widthIn(
                        min = 140.dp
                    )
                    .padding(vertical = 12.dp, horizontal = 25.dp)
                    .clickable(
                        onClick = onCancel
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cancelBtnText,
                    color = Color.White,
                    fontWeight = FontWeight(600),
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            // 确认
            Box(
                modifier = Modifier
                    .background(
                        color = confirmBtnColor,
                        shape = RoundedCornerShape(100)
                    )
                    .widthIn(
                        min = 140.dp
                    )
                    .padding(vertical = 12.dp, horizontal = 25.dp)
                    .clickable(
                        onClick = onConfirm
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = confirmBtnText,
                    color = Color.White,
                    fontWeight = FontWeight(600),
                    fontSize = 24.sp
                )
            }
        }

    }
}