package com.flypigs.typechomanager.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.Category
import com.flypigs.typechomanager.data.model.Post
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
    val categories: List<Category> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val tags: String = "",
    val status: Post.Companion.Status = Post.Companion.Status.DRAFT,
    val allowComment: Boolean = true,
    val allowPing: Boolean = true,
    val isPreview: Boolean = false,
    val isPublishing: Boolean = false,
    val isPublished: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val xmlRpcClient: XmlRpcClient,
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // 编辑模式：null = 新建，非 null = 编辑已有文章
    private var editingPostId: String? = null

    init {
        loadCategories()
    }

    fun loadPost(postId: String) {
        editingPostId = postId
        viewModelScope.launch {
            try {
                val config = configDataStore.getConfig()
                val post = xmlRpcClient.getPost(
                    endpoint = config.endpoint,
                    username = config.username,
                    password = config.password,
                    postId = postId
                )
                _uiState.value = _uiState.value.copy(
                    title = post.title,
                    content = post.text,
                    selectedCategories = post.categories,
                    tags = post.tags.joinToString(", "),
                    status = Post.Companion.Status.fromValue(post.status),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "加载文章失败: ${e.message}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true)
            try {
                val config = configDataStore.getConfig()
                val categories = xmlRpcClient.getCategories(
                    endpoint = config.endpoint,
                    username = config.username,
                    password = config.password
                )
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoadingCategories = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingCategories = false)
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun updateTags(tags: String) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    fun toggleCategory(categoryName: String) {
        val current = _uiState.value.selectedCategories.toMutableList()
        if (current.contains(categoryName)) {
            current.remove(categoryName)
        } else {
            current.add(categoryName)
        }
        _uiState.value = _uiState.value.copy(selectedCategories = current)
    }

    fun setStatus(status: Post.Companion.Status) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun toggleAllowComment() {
        _uiState.value = _uiState.value.copy(allowComment = !_uiState.value.allowComment)
    }

    fun toggleAllowPing() {
        _uiState.value = _uiState.value.copy(allowPing = !_uiState.value.allowPing)
    }

    fun togglePreview() {
        _uiState.value = _uiState.value.copy(isPreview = !_uiState.value.isPreview)
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
                val tagsList = state.tags
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                val contentMap = mapOf(
                    "title" to state.title,
                    "description" to state.content,
                    "mt_text_more" to "",
                    "categories" to state.selectedCategories,
                    "mt_keywords" to tagsList,
                    "post_status" to state.status.value,
                    "mt_allow_comments" to if (state.allowComment) 1 else 0,
                    "mt_allow_ping" to if (state.allowPing) 1 else 0,
                )

                if (editingPostId != null) {
                    xmlRpcClient.editPost(
                        endpoint = config.endpoint,
                        username = config.username,
                        password = config.password,
                        postId = editingPostId!!,
                        content = contentMap
                    )
                } else {
                    xmlRpcClient.newPost(
                        endpoint = config.endpoint,
                        username = config.username,
                        password = config.password,
                        content = contentMap
                    )
                }
                _uiState.value = _uiState.value.copy(isPublishing = false, isPublished = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    error = e.message ?: "发布失败"
                )
            }
        }
    }

    fun saveDraft() {
        setStatus(Post.Companion.Status.DRAFT)
        publish()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
