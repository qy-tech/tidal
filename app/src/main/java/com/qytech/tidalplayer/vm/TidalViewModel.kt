package com.qytech.tidalplayer.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.qytech.tidal.BuildConfig
import com.qytech.tidal.TidalService
import com.qytech.tidal.cache.TidalCacheManager
import com.qytech.tidal.login.TidalLogin
import com.qytech.tidal.repository.TidalRepository
import com.qytech.tidalplayer.base.BaseViewModel
import com.tidal.sdk.auth.model.AuthConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TidalViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tidalLogin: TidalLogin,
    private val repository: TidalRepository,
    private val tidalCacheManager: TidalCacheManager
) : BaseViewModel() {

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

    val isLoggedIn = tidalLogin.loginUiState
        .map { state ->
            val cacheLoggedIn = !tidalCacheManager.getUserInfo()?.id.isNullOrBlank()
            Timber.Forest.d("isLoggedIn: ${state.isLoggedIn}, cacheLoggedIn: ${cacheLoggedIn}, cacheClear: ${state.cacheClear}")
            state.isLoggedIn || (cacheLoggedIn && !state.cacheClear)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(0),
            initialValue = false
        )


}