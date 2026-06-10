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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttachmentsUiState(
    val attachments: List<Attachment> = emptyList(),
    val total: Int = 0,
    val totalSize: Long = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    private val apiClient: CompanionApiClient,
    private val configDataStore: ConfigDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttachmentsUiState())
    val uiState: StateFlow<AttachmentsUiState> = _uiState.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        loadAttachments()
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
