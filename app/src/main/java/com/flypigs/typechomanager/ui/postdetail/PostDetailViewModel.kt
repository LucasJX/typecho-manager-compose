package com.flypigs.typechomanager.ui.postdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.remote.XmlRpcClient
import com.flypigs.typechomanager.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val xmlRpcClient: XmlRpcClient,
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val cid: Int = savedStateHandle.get<Int>("cid") ?: 0

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun publishPost() {
        val currentPost = _post.value ?: return
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val config = configDataStore.getConfig()
                val contentMap = mapOf(
                    "title" to currentPost.title,
                    "description" to currentPost.text,
                    "mt_text_more" to "",
                    "categories" to currentPost.categories,
                    "mt_keywords" to currentPost.tags,
                    "post_status" to "publish",
                )
                xmlRpcClient.editPost(
                    endpoint = config.endpoint,
                    username = config.username,
                    password = config.password,
                    postId = currentPost.cid.toString(),
                    content = contentMap
                )
                _post.value = currentPost.copy(status = "publish")
                _message.value = "文章已发布"
            } catch (e: Exception) {
                _message.value = "发布失败: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val posts = postRepository.getRecentPosts()
                val found = posts.find { it.cid == cid }
                _post.value = found
            } catch (_: Exception) {
                _post.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}
