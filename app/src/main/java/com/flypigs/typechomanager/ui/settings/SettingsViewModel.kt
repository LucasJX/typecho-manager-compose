package com.flypigs.typechomanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.ThemeMode
import com.flypigs.typechomanager.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class HeatmapDay(
    val date: String,
    val count: Int,
)

data class SettingsUiState(
    val blogName: String = "",
    val blogUrl: String = "",
    val endpoint: String = "",
    val username: String = "",
    val xmlRpcUrl: String = "",
    val apiUrl: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isDark: Boolean = false,
    val isLoading: Boolean = false,
    val postCount: Int = 0,
    val draftCount: Int = 0,
    val categoryCount: Int = 0,
    val attachmentCount: Int = 0,
    val isLoadingStats: Boolean = false,
    val cacheSize: String = "0 KB",
    val pullToRefreshEnabled: Boolean = true,
    val imageQuality: String = "高质量",
    val versionName: String = "1.7.0",
    val heatmapData: List<HeatmapDay> = emptyList(),
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
        loadHeatmapData()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val config = configDataStore.getConfig()
                val themeMode = configDataStore.getThemeMode()
                _uiState.value = _uiState.value.copy(
                    blogName = config.blogName,
                    blogUrl = config.blogUrl,
                    endpoint = config.endpoint,
                    username = config.username,
                    xmlRpcUrl = config.xmlRpcUrl,
                    apiUrl = config.endpoint,
                    themeMode = themeMode,
                    isDark = themeMode == ThemeMode.DARK,
                    isLoading = false,
                    pullToRefreshEnabled = configDataStore.getPullToRefreshEnabled(),
                    imageQuality = configDataStore.getImageQuality(),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
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
                    postCount = posts.size,
                    draftCount = posts.count { it.status == "draft" },
                    categoryCount = categories.size,
                    attachmentCount = attachments.size,
                    isLoadingStats = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingStats = false)
            }
        }
    }

    private fun loadHeatmapData() {
        viewModelScope.launch {
            try {
                val posts = postRepository.getRecentPosts()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // 统计过去 12 周每天的文章操作次数
                val calendar = Calendar.getInstance()
                val heatmapData = mutableListOf<HeatmapDay>()

                // 从 12 周前开始
                calendar.add(Calendar.WEEK_OF_YEAR, -12)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                val postDates = posts.groupBy { dateFormat.format(it.date) }

                repeat(84) { // 12 周 * 7 天
                    val dateStr = dateFormat.format(calendar.time)
                    val count = postDates[dateStr]?.size ?: 0
                    heatmapData.add(HeatmapDay(date = dateStr, count = count))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                _uiState.value = _uiState.value.copy(heatmapData = heatmapData)
            } catch (_: Exception) {
                // 忽略错误
            }
        }
    }

    fun toggleThemeMode() {
        val newMode = if (_uiState.value.isDark) ThemeMode.SYSTEM else ThemeMode.DARK
        viewModelScope.launch {
            configDataStore.setThemeMode(newMode)
            _uiState.value = _uiState.value.copy(
                themeMode = newMode,
                isDark = newMode == ThemeMode.DARK,
            )
        }
    }

    fun togglePullToRefresh() {
        val newValue = !_uiState.value.pullToRefreshEnabled
        viewModelScope.launch {
            configDataStore.setPullToRefreshEnabled(newValue)
            _uiState.value = _uiState.value.copy(pullToRefreshEnabled = newValue)
        }
    }

    fun cycleImageQuality() {
        val qualities = listOf("高质量", "中等", "低质量")
        val currentIndex = qualities.indexOf(_uiState.value.imageQuality)
        val nextIndex = (currentIndex + 1) % qualities.size
        val newQuality = qualities[nextIndex]
        viewModelScope.launch {
            configDataStore.setImageQuality(newQuality)
            _uiState.value = _uiState.value.copy(imageQuality = newQuality)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: 实现缓存清理
            _uiState.value = _uiState.value.copy(cacheSize = "0 KB")
        }
    }
}
