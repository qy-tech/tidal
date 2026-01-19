package com.qytech.tidalplayer.ui.login

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.qytech.tidalplayer.R
import com.qytech.tidalplayer.ui.TidalRoute
import com.qytech.tidalplayer.ui.TidalViewModel

@Composable
fun LoginStartScreen(
    viewModel: TidalViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff080808))
    ) {
        val (quit, login) = createRefs()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(login) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoggedIn) {
//             todo 自动登录，或者直接跳转到歌曲列表
                navController.navigate(TidalRoute.SONG_LIST_START)
            } else {
                NotLoggedInUI(
                    onClickLogin = {
                        navController.navigate(TidalRoute.LOGIN_WEB)
                    },
                    onClickRegister = {
                        navController.navigate(TidalRoute.LOGIN_WEB)
                    }
                )
            }
        }

        Text(
            text = "退出",
            color = Color.White,
            fontWeight = FontWeight(500),
            letterSpacing = 3.sp,
            modifier = Modifier.constrainAs(quit) {
                top.linkTo(parent.top, )
                start.linkTo(parent.start, )
            }
                .padding(10.dp)
                .clickable(onClick = {
                    activity?.moveTaskToBack(true)
                }),
            fontSize = 25.sp
        )
    }

}

@Preview(showBackground = true)
@Composable
private fun NotLoggedInUI(
    onClickLogin: () -> Unit = {},
    onClickRegister: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .background(Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TIDAL",
                color = Color.White,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.size(10.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_tidal_connect),
                    contentDescription = null,
                    modifier = Modifier.size(65.dp),
                    tint = Color.Black
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(15.dp))
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(color = Color(0x5dffffff))
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "使用浏览器登录TIDAL",
                color = Color.White,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.size(15.dp))
            Button(
                onClick = onClickLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .width(150.dp)
                    .height(35.dp)
            ) {
                Text(
                    text = "登录",
                    color = Color.White,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Button(
                onClick = onClickRegister,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x5effffff),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .height(35.dp)
                    .width(150.dp)
            ) {
                Text(
                    text = "注册",
                    fontSize = 14.sp,
                    color = Color.White,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
    }
}