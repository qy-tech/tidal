package com.qytech.tidalplayer.ui.listpage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qytech.tidalplayer.utils.ToastUtils
import com.qytech.tidalplayer.utils.checkDescriptionLegal
import com.qytech.tidalplayer.utils.checkNameLegal

@Composable
fun CreateNewPlaylistDialog(
    onDismiss: () -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirm: (String, String) -> Unit = { _, _ -> }
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        CreateNewPlaylistDialogContent(
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1080,
    heightDp = 640
)
@Composable
private fun CreateNewPlaylistDialogContent(
    title: String = "New Playlist",
    onCancel: () -> Unit = {},
    onConfirm: (String, String) -> Unit = {_, _ ->}
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
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
        CreateNewPlaylistDialogInput(
            title = "NAME",
            currentValue = name,
            placeholder = "e.g. Chill Vibes",
            onValueChange = { newName ->
                name = newName.trim()
            }
        )
        Spacer(modifier = Modifier.size(20.dp))
        CreateNewPlaylistDialogInput(
            inputHeight = 155.dp,
            alignment = Alignment.TopStart,
            title = "DESCRIPTION",
            currentValue = description,
            placeholder = "Optional description...",
            isSingleLine = false,
            onValueChange = { newDescription ->
                description = newDescription.trim()
            }
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
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(100)
                    )
                    .widthIn(
                        min = 140.dp
                    )
                    .clickable(
                        onClick = onCancel
                    )
                    .padding(vertical = 12.dp, horizontal = 25.dp)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
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
                        color = Color.White,
                        shape = RoundedCornerShape(100)
                    )
                    .widthIn(
                        min = 140.dp
                    )
                    .clickable(
                        onClick = {
                            if (name.isBlank()) {
                                ToastUtils.show("名字不能为空")
                                return@clickable
                            }
                            if (!name.checkNameLegal()) {
                                ToastUtils.show("名字不合法")
                                return@clickable
                            }
                            if (description.isNotBlank() && !description.checkDescriptionLegal()) {
                                ToastUtils.show("描述内容不合法")
                                return@clickable
                            }
                            onConfirm.invoke(name, description)
                        }
                    )
                    .padding(vertical = 12.dp, horizontal = 25.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Confirm",
                    color = Color.Black,
                    fontWeight = FontWeight(600),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
private fun CreateNewPlaylistDialogInput(
    title: String = "NAME",
    inputHeight: Dp = 65.dp,
    currentValue: String = "",
    placeholder: String = "asd",
    isSingleLine: Boolean = true,
    alignment: Alignment = Alignment.Center,
    onValueChange: (String) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column() {
        Text(
            text = title,
            color = Color(0xffa0a0a0),
            fontWeight = FontWeight(500),
            fontSize = 24.sp,
            lineHeight = 30.sp
        )
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .background(
                    color = if (isFocused) Color.White.copy(alpha = 0.08f) else Color.White.copy(
                        alpha = 0.05f
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isFocused) Color(0xff00e5ff) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                )
                .height(inputHeight)
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.height(inputHeight - 30.dp),
                contentAlignment = alignment
            ) {
                BasicTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    cursorBrush = SolidColor(Color.White),
                    singleLine = isSingleLine,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        lineHeight = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (currentValue.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color.Gray,
                                    fontSize = 24.sp,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}
