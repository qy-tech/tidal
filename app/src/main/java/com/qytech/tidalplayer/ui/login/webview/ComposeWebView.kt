package com.qytech.tidalplayer.ui.login.webview

import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.qytech.tidal.BuildConfig
import com.qytech.tidalplayer.ui.TidalViewModel

@Composable
fun ComposeWebView(
    viewModel: TidalViewModel,
    onPageFinish: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize()
            .imePadding(),
        factory = { context ->
            WebView.setWebContentsDebuggingEnabled(true)

            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                setBackgroundColor(Color.TRANSPARENT) // 1. 背景透明
                alpha = 0f // 2. 初始透明度设为 0 (完全隐形)

                settings.apply {
                    loadWithOverviewMode = false
                    useWideViewPort = false
                    javaScriptEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }

                webChromeClient = ExtendedChromeClient()
                webViewClient = ExtendedWebClient(
                    onRedirectUriReceived = { it ->
                        viewModel.getTidalLogin().finalizeLogin(it.toString())
                    },
                    onPageFinish = onPageFinish
                )

                addJavascriptInterface(JavaScriptInterface(), "javascriptObject")
                val url =
                    viewModel.getTidalLogin().getLoginUrl(BuildConfig.TIDAL_CLIENT_REDIRECT_URI)
                loadUrl(
                    url
                )
            }
        }
    )
}
