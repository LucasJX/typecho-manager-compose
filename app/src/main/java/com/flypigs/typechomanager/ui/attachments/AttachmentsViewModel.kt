package com.flypigs.typechomanager.ui.attachments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.Attachment
import com.flypigs.typechomanager.data.remote.CompanionApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 素材库视图模式 */
enum class ViewMode { GRID, LIST }

data class AttachmentsUiState(
    val attachments: List<Attachment> = emptyList(),
    val total: Int = 0,
    val totalSize: Long = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.GRID,
)

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    private val apiClient: CompanionApiClient,
    private val configDataStore: ConfigDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttachmentsUiState())
    val uiState: StateFlow<AttachmentsUiState> = _uiState.asStateFlow()

    /** 搜索后的过滤结果 */
    val filteredAttachments: StateFlow<List<Attachment>> = _uiState
        .map { state -> filterAttachments(state) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        loadAttachments()
    }

    /** 更新搜索关键词 */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /** 切换视图模式 */
    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }

    private fun filterAttachments(state: AttachmentsUiState): List<Attachment> {
        val query = state.searchQuery.trim().lowercase()
        if (query.isEmpty()) return state.attachments
        return state.attachments.filter { attachment ->
            attachment.name.lowercase().contains(query) ||
            attachment.type.lowercase().contains(query)
        }
    }

    /**
     * Initial load — fetches the first page of attachments.
     */
    fun loadAttachments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val config = configDataStore.getConfig()
                val companionBase = config.blogUrl.ifEmpty {
                    config.endpoint.substringBefore("/index.php")
                }.trimEnd('/')
                val (items, totalPages, totalSize) = apiClient.listMedia(
                    companionBase = companionBase,
                    username = config.username,
                    password = config.password,
                    page = 1,
                    pageSize = PAGE_SIZE
                )
                _uiState.value = _uiState.value.copy(
                    attachments = items,
                    total = items.size,
                    totalSize = totalSize,
                    page = 1,
                    totalPages = totalPages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Load the next page of attachments (infinite scroll).
     */
    fun loadMore() {
        val state = _uiState.value
        val nextPage = state.page + 1
        if (state.isLoadingMore || nextPage > state.totalPages) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            try {
                val config = configDataStore.getConfig()
                val companionBase = config.blogUrl.ifEmpty {
                    config.endpoint.substringBefore("/index.php")
                }.trimEnd('/')
                val (newItems, totalPages, _) = apiClient.listMedia(
                    companionBase = companionBase,
                    username = config.username,
                    password = config.password,
                    page = nextPage,
                    pageSize = PAGE_SIZE
                )
                _uiState.value = _uiState.value.copy(
                    attachments = state.attachments + newItems,
                    total = state.attachments.size + newItems.size,
                    page = nextPage,
                    totalPages = totalPages,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Load more failed"
                )
            }
        }
    }

    /**
     * Delete an attachment by CID and remove it from the local list immediately.
     */
    fun deleteAttachment(cid: Int) {
        viewModelScope.launch {
            try {
                val config = configDataStore.getConfig()
                val companionBase = config.blogUrl.ifEmpty {
                    config.endpoint.substringBefore("/index.php")
                }.trimEnd('/')
                apiClient.deleteMedia(companionBase, config.username, config.password, cid)
                val updated = _uiState.value.attachments.filter { it.cid != cid }
                _uiState.value = _uiState.value.copy(
                    attachments = updated,
                    total = updated.size,
                    totalSize = updated.sumOf { it.size }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Delete failed"
                )
            }
        }
    }

    /**
     * Pull-to-refresh — reload from page 1.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                val config = configDataStore.getConfig()
                val companionBase = config.blogUrl.ifEmpty {
                    config.endpoint.substringBefore("/index.php")
                }.trimEnd('/')
                val (items, totalPages, totalSize) = apiClient.listMedia(
                    companionBase = companionBase,
                    username = config.username,
                    password = config.password,
                    page = 1,
                    pageSize = PAGE_SIZE
                )
                _uiState.value = _uiState.value.copy(
                    attachments = items,
                    total = items.size,
                    totalSize = totalSize,
                    page = 1,
                    totalPages = totalPages,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Refresh failed"
                )
            }
        }
    }
}
