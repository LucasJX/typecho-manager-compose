package com.flypigs.typechomanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsUiState(
    val blogName: String = "",
    val blogUrl: String = "",
    val endpoint: String = "",
    val username: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isDark: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            val config = configDataStore.getConfig()
            val themeMode = configDataStore.getThemeMode()
            _uiState.value = _uiState.value.copy(
                blogName = config.blogName ?: "",
                blogUrl = config.blogUrl.ifEmpty {
                    // Derive blog URL from XML-RPC endpoint
                    config.endpoint
                        .substringBefore("/index.php")
                        .substringBefore("/xmlrpc")
                        .trimEnd('/')
                },
                endpoint = config.endpoint,
                username = config.username,
                themeMode = themeMode,
                isDark = themeMode == ThemeMode.DARK,
            )
        }
    }

    fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            configDataStore.saveThemeMode(mode)
            _uiState.value = _uiState.value.copy(
                themeMode = mode,
                isDark = mode == ThemeMode.DARK,
            )
        }
    }

    fun toggleDark() {
        val newMode = if (_uiState.value.isDark) ThemeMode.SYSTEM else ThemeMode.DARK
        saveThemeMode(newMode)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            configDataStore.clearConfig()
            onLoggedOut()
        }
    }
}
