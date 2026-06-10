package com.flypigs.typechomanager.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.BlogConfig
import com.flypigs.typechomanager.data.remote.XmlRpcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val endpoint: String = "",
    val username: String = "",
    val password: String = "",
    val blogId: String = "1",
    val isLoading: Boolean = false,
    val error: String? = null,
    val connected: Boolean = false,
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val configDataStore: ConfigDataStore,
    private val xmlRpcClient: XmlRpcClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun updateField(
        endpoint: String? = null,
        username: String? = null,
        password: String? = null,
        blogId: String? = null,
    ) {
        _uiState.value = _uiState.value.copy(
            endpoint = endpoint ?: _uiState.value.endpoint,
            username = username ?: _uiState.value.username,
            password = password ?: _uiState.value.password,
            blogId = blogId ?: _uiState.value.blogId,
            error = null,
        )
    }

    fun connect() {
        val state = _uiState.value
        if (state.endpoint.isBlank()) {
            _uiState.value = state.copy(error = "请输入博客地址")
            return
        }
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "请输入用户名")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "请输入密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Test connection by fetching recent posts
                val posts = xmlRpcClient.getRecentPosts(
                    endpoint = state.endpoint.trimEnd('/'),
                    username = state.username,
                    password = state.password,
                    blogId = state.blogId.ifBlank { "1" },
                    numberOfPosts = 1,
                )

                // Save config on success
                val config = BlogConfig(
                    endpoint = state.endpoint.trimEnd('/'),
                    username = state.username,
                    password = state.password,
                    blogId = state.blogId.ifBlank { "1" },
                    blogName = "",
                )
                configDataStore.saveConfig(config)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    connected = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "连接失败，请检查配置",
                )
            }
        }
    }
}
