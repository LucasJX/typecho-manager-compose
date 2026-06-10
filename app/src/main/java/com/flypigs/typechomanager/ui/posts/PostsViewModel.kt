package com.flypigs.typechomanager.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    /** Initial load — fetches posts from the repository. */
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val posts = postRepository.getRecentPosts()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
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

    /** Pull-to-refresh — forces a network call and updates the cache. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                val posts = postRepository.getRecentPosts(refresh = true)
                _uiState.value = _uiState.value.copy(
                    posts = posts,
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

    /** Delete a post by cid and remove it from the local list immediately. */
    fun deletePost(cid: Int) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(cid)
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts.filter { it.cid != cid }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Delete failed"
                )
            }
        }
    }
}
