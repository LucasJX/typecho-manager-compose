package com.flypigs.typechomanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.repository.PostRepository
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
    val xmlRpcUrl: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isDark: Boolean = false,
    val isLoading: Boolean = false,
    val postCount: Int = 0,
    val draftCount: Int = 0,
    val categoryCount: Int = 0,
    val attachmentCount: Int = 0,
    val isLoadingStats: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configDataStore: ConfigDataStore,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
        loadStats()
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
                xmlRpcUrl = config.endpoint,
                themeMode = themeMode,
                isDark = themeMode == ThemeMode.DARK,
            )
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true)
            try {
                val posts = postRepository.getRecentPosts()
                val categories = try {
                    postRepository.getCategories()
                } catch (_: Exception) {
                    emptyList()
                }
                val attachments = try {
                    postRepository.getAttachments()
                } catch (_: Exception) {
                    emptyList()
                }
                _uiState.value = _uiState.value.copy(
                    postCount = posts.count { it.status == "publish" },
                    draftCount = posts.count { it.status == "draft" },
                    categoryCount = categories.size,
                    attachmentCount = attachments.size,
                    isLoadingStats = false,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingStats = false)
            }
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

    fun clearCache(onCleared: () -> Unit) {
        viewModelScope.launch {
            try {
                // Clear image cache by reloading stats
                loadStats()
                onCleared()
            } catch (_: Exception) {
                onCleared()
            }
        }
    }
}
