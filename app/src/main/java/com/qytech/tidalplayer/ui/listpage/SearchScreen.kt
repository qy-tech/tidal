package com.qytech.tidalplayer.ui.listpage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.listpage.components.SearchResult
import com.qytech.tidalplayer.utils.ToastUtils
import com.qytech.tidalplayer.utils.checkDescriptionLegal
import com.qytech.tidalplayer.utils.popBackSafely

@Composable
fun SearchScreen(
    navController: NavController? = null
) {
    val focusManager = LocalFocusManager.current
    val viewModel: ListPageViewModel = hiltViewModel()
    val searchHistory by viewModel.searchHistory.collectAsState()
//    val searchHistory = remember { listOf("asdasd", "564", "wqee21",
//        "c4a4d6as45") }
    val focusRequester = remember { FocusRequester() }
    var searchText by rememberSaveable { mutableStateOf("") }
    var showSearchResult by rememberSaveable { mutableStateOf(false) }
    val inputTextIs = remember { MutableInteractionSource() }
    val inputTextFocus by inputTextIs.collectIsFocusedAsState()

    LaunchedEffect(inputTextFocus) {
        if (inputTextFocus) {
            showSearchResult = false
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
            SearchBar(
                searchText = searchText,
                focusRequester = focusRequester,
                inputTextIs = inputTextIs,
                onValueChange = { newSearchText ->
                    searchText = newSearchText
                },
                onBack = {
                    navController?.popBackSafely()
                },
                onSearch = { search ->
                    viewModel.addSearchHistory(search)
                    focusManager.clearFocus()
                    showSearchResult = true
                },
                onClear = {
                    searchText = ""
                    focusRequester.requestFocus()
                }
            )
            Spacer(modifier = Modifier.size(20.dp))
        }

        // 往下部分
        Spacer(modifier = Modifier.size(30.dp))

        if (showSearchResult) {
            SearchResult(
                navController = navController,
                searchText = searchText
            )
        } else {
            SearchHistory(
                history = searchHistory,
                onClick = { search ->
                    if (search != searchText) {
                        searchText = search
                    }
                    focusManager.clearFocus()
                    viewModel.addSearchHistory(search)
                    showSearchResult = true
                },
                onDel = { text ->
                    viewModel.removeSearchHistory(text)
                },
                onClear = {
                    viewModel.clearSearchHistory()
                }
            )
        }
    }
}

@Composable
private fun SearchHistory(
    history: List<String> = emptyList(),
    onClick: (String) -> Unit = {},
    onClear: () -> Unit = {},
    onDel: (String) -> Unit = {}
) {
    Column() {
        Row() {
            Text(
                text = "RECENT SEARCHES",
                color = Color(0xffA0A0A0),
                fontSize = 24.sp,
                fontWeight = FontWeight(600)
            )
            Spacer(
                modifier = Modifier
                    .width(20.dp)
                    .height(1.dp)
            )

            if (!history.isEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(25.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.icon_trash_solid_full),
                        contentDescription = null,
                        tint = Color(0xffa0a0a0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(20.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (index in history.size - 1 downTo 0) {
                HistoryChip(
                    text = history[index],
                    onClick = onClick,
                    onDel = onDel
                )
            }
        }
    }
}

@Composable
private fun HistoryChip(
    text: String,
    onClick: (String) -> Unit = {},
    onDel: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .widthIn(
                min = 150.dp
            )
            .background(
                color = Color.White.copy(0.2f),
                shape = RoundedCornerShape(100)
            )
            .clickable(
                onClick = { onClick.invoke(text) }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Spacer(modifier = Modifier.size(15.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .clickable(
                    onClick = { onDel.invoke(text) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_xmark_solid_full),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = Color(0xffa0a0a0)
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    focusRequester: FocusRequester,
    inputTextIs: MutableInteractionSource,
    onValueChange: (String) -> Unit,
    onBack: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_arrow_left_solid_full),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.size(20.dp))
        SearchInput(
            modifier = Modifier.weight(1f),
            focusRequester = focusRequester,
            inputTextIs = inputTextIs,
            currentValue = searchText,
            placeholder = "Search for playlist, or artists...",
            onValueChange = onValueChange,
            onClear = onClear
        )
        Spacer(modifier = Modifier.size(20.dp))
        // 搜索按钮
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(50.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(
                    onClick = {
                        if (searchText.isBlank()) {
                            ToastUtils.show("搜索内容不能为空")
                            return@clickable
                        }
                        if (!searchText.checkDescriptionLegal()) {
                            ToastUtils.show("搜索内容不合法")
                            return@clickable
                        }
                        onSearch.invoke(searchText)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Search",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight(600)
            )
        }
    }
}

@Composable
private fun SearchInput(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    inputTextIs: MutableInteractionSource,
    inputHeight: Dp = 65.dp,
    currentValue: String = "",
    placeholder: String = "asd",
    isSingleLine: Boolean = true,
    alignment: Alignment = Alignment.Center,
    onValueChange: (String) -> Unit = {},
    onClear: () -> Unit = {}
) {
    val isFocused by inputTextIs.collectIsFocusedAsState()
    var showClearBtn by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.08f) else Color.White.copy(
                    alpha = 0.12f
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFocused) Color(0xff00e5ff) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .height(inputHeight)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.icon_magnifying_glass_solid_full),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = Color(0xff555555)

        )
        Spacer(modifier = Modifier.size(10.dp))
        Box(
            modifier = Modifier
                .height(inputHeight - 30.dp)
                .weight(1f),
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
                interactionSource = inputTextIs,
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
                            showClearBtn = false
                        } else {
                            showClearBtn = true
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
            )
        }

        // 清除按钮
        if (showClearBtn) {
            Spacer(modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = Color(0xffa0a0a0),
                        shape = CircleShape
                    )
                    .clickable(
                        onClick = onClear
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_xmark_solid_full),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

}