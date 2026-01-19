package com.qytech.tidalplayer.ui.login.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep

@Suppress("EmptyFunctionBlock")
class JavaScriptInterface {

    @Keep @Suppress("unused") @JavascriptInterface fun triggerFacebookSDKLogin() {}

    @Keep @Suppress("unused") @JavascriptInterface fun triggerTwitterSDKLogin() {}

    @Keep @Suppress("unused") @JavascriptInterface fun triggerResetPassword(url: String) {}

    @Keep
    @Suppress("unused")
    @JavascriptInterface
    fun openInExternalBrowser(url: String, closeWebView: Boolean) {}
}
