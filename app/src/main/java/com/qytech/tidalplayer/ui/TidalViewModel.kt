package com.qytech.tidalplayer.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qytech.tidal.BuildConfig
import com.qytech.tidal.TidalService
import com.qytech.tidal.cache.TidalCacheManager
import com.qytech.tidal.data.UserInfo
import com.qytech.tidal.login.TidalLogin
import com.qytech.tidal.repository.TidalRepository
import com.tidal.sdk.auth.model.AuthConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TidalViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tidalLogin: TidalLogin,
    private val repository: TidalRepository,
    private val tidalCacheManager: TidalCacheManager
) : ViewModel() {

    init {
        TidalService.init(
            context, AuthConfig(
                clientId = BuildConfig.TIDAL_CLIENT_ID,
                clientSecret = BuildConfig.TIDAL_CLIENT_SECRET,
                credentialsKey = "storage"
            )
        )
    }

    fun getTidalLogin() = tidalLogin

    private val tokenLoginUser = flow<UserInfo?> {
        try {
            repository.getUser().collectLatest { userInfo ->
                emit(userInfo)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    val isLoggedIn = tidalLogin.loginUiState
        .map { state ->
            val cacheLoggedIn = !tidalCacheManager.getUserInfo()?.id.isNullOrBlank()
            state.isLoggedIn || cacheLoggedIn
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )


}