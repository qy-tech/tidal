package com.qytech.tidalplayer.ui.login.webview

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.qytech.tidal.BuildConfig

class ExtendedWebClient(private val onRedirectUriReceived: (Uri) -> Unit, private val onPageFinish: () -> Unit) : WebViewClient() {

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
    }

    // android 7.0 及以上
    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        // 返回 false 表示由 WebView 处理这个 URL
        return hasRedirectUri(request.url)
    }

    // android 7.0 以下
    @SuppressWarnings("deprecation")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val uri = Uri.parse(url)
        return hasRedirectUri(uri)
    }

    private fun hasRedirectUri(uri: Uri): Boolean {
        return if (uri.toString().startsWith(BuildConfig.TIDAL_CLIENT_REDIRECT_URI)) {
            onRedirectUriReceived(uri)
            true
        } else {
            false
        }
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        // 处理加载错误
        onReceivedError(view, error.errorCode, error.description.toString(), request.url.toString())
    }

    @SuppressWarnings("deprecation")
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String,
    ) {
        // TODO handle error
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinish.invoke()
        view?.animate()?.alpha(1f)?.setDuration(300)?.start()
    }
}
