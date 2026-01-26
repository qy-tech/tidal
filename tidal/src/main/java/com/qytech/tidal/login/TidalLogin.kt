package com.qytech.tidal.login

import com.qytech.tidal.TidalService
import com.qytech.tidal.cache.TidalCacheManager
import com.qytech.tidal.data.UserInfo
import com.tidal.sdk.auth.model.AuthorizationError
import com.tidal.sdk.auth.model.CredentialsUpdatedMessage
import com.tidal.sdk.auth.model.LoginConfig
import com.tidal.sdk.auth.model.QueryParameter
import com.tidal.sdk.auth.model.TokenResponseError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LoginUiState(
    val isLoggedIn: Boolean = false,
    val cacheClear: Boolean = false,
    val handleState: LoginHandleState? = null,
    val userCode: String? = null,
    val deviceCode: String? = null,
    val qrCode: String? = null
)

sealed class LoginHandleState {
    object Loading : LoginHandleState()
    data class Success(val msg: String?) : LoginHandleState()
    data class Error(val msg: String?) : LoginHandleState()
}

@Singleton
class TidalLogin @Inject constructor(
    private val tidalCacheManager: TidalCacheManager
) {
    private val loginScope = CoroutineScope(Dispatchers.IO + Job())

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState = _loginUiState.asStateFlow()

    init {
        onCredentialsUpdates()
    }

    /**
     * 拿到登录的URL，用来跳转过去
     */
    fun getLoginUrl(redirectUri: String): String {
        val loginConfig = LoginConfig(
            customParams = setOf(
                QueryParameter(
                    key = "appMode",
                    value = "android"
                )
            )
        )
        return TidalService.tidalAuth?.auth?.initializeLogin(
            redirectUri = redirectUri,
            loginConfig = loginConfig
        ).toString()
    }

    /**
     * 处理最终登录逻辑，这个方法是在登录且拦截最后一个uri时，使用的
     */
    fun finalizeLogin(loginResponseUri: String) {
        loginScope.launch {
            try {
                // 加载中
                _loginUiState.update {
                    it.copy(
                        handleState = LoginHandleState.Loading
                    )
                }

                val res = TidalService.tidalAuth?.auth?.finalizeLogin(loginResponseUri)
                if (res?.isSuccess == true) {
                    _loginUiState.update {
                        it.copy(
                            handleState = LoginHandleState.Success(null)
                        )
                    }
                    saveToken()
                } else {
                    _loginUiState.update {
                        it.copy(
                            handleState = LoginHandleState.Error("登录失败")
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginUiState.update {
                    it.copy(
                        handleState = LoginHandleState.Error("登录失败")
                    )
                }
            }
        }
    }

    /**
     * 设备登录，其实就是二维码登录
     */
    fun useQrCodeLogin() {
        loginScope.launch {
            val res = TidalService.tidalAuth?.auth?.initializeDeviceLogin()
            if (res?.isSuccess == true) {
                _loginUiState.update {
                    it.copy(
                        userCode = res.successData?.userCode,
                        deviceCode = res.successData?.deviceCode,
                        qrCode = res.successData?.verificationUri
                    )
                }

                val deviceLoginRes = TidalService.tidalAuth?.auth?.finalizeDeviceLogin(
                    res.successData?.deviceCode ?: ""
                )
                if (deviceLoginRes?.isSuccess == true) {
                    saveToken()
                }
            }
        }
    }

    private fun saveToken() {
        loginScope.launch {
            val userToken = TidalService.credentialsProvider?.getCredentials()?.successData?.token
            val userId = TidalService.credentialsProvider?.getCredentials()?.successData?.userId
            val isLoggedIn = TidalService.credentialsProvider?.isUserLoggedIn() == true
            if (userToken != null) {
                tidalCacheManager.saveUserInfo(userInfo = UserInfo(
                    id = userId ?: "",
                    token = userToken
                ))
                _loginUiState.update {
                    it.copy(
                        isLoggedIn = isLoggedIn,
                        cacheClear = false
                    )
                }
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            TidalService.tidalAuth?.auth?.logout()
            // 需要缓存清除
            tidalCacheManager.clearUserInfo()
            _loginUiState.update {
                it.copy(
                    isLoggedIn = false,
                    cacheClear = true
                )
            }
        }
    }

    private fun onCredentialsUpdates() {
        loginScope.launch {
            TidalService.credentialsProvider?.bus?.collectLatest {
                when (it) {
                    is AuthorizationError -> {
                        logout()
                    }

                    is TokenResponseError -> {
                        logout()
                    }

                    is CredentialsUpdatedMessage -> {
                        saveToken()
                    }
                }
            }
        }
    }

}