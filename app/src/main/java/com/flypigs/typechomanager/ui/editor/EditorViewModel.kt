package com.flypigs.typechomanager.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.remote.XmlRpcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val title: String = "",
    val content: String = "",
    val isPublishing: Boolean = false,
    val isPublished: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val xmlRpcClient: XmlRpcClient,
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun publish() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "请输入标题")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPublishing = true, error = null)
            try {
                val config = configDataStore.getConfig()
                val contentMap = mapOf(
                    "title" to state.title,
                    "description" to state.content,
                    "mt_text_more" to "",
                    "categories" to listOf("未分类"),
                )
                xmlRpcClient.newPost(
                    endpoint = config.endpoint,
                    username = config.username,
                    password = config.password,
                    content = contentMap
                )
                _uiState.value = _uiState.value.copy(isPublishing = false, isPublished = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    error = e.message ?: "发布失败"
                )
            }
        }
    }
}
