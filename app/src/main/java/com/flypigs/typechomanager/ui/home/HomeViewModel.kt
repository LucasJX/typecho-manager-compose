package com.flypigs.typechomanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.Category
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val allPosts: List<Post> = emptyList(),
    val categories: List<Category> = emptyList(),
    val attachmentCount: Int = 0,
    val blogName: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedCategorySlug: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val configDataStore: ConfigDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBlogName()
        loadPosts()
    }

    private fun loadBlogName() {
        viewModelScope.launch {
            try {
                val config = configDataStore.getConfig()
                _uiState.value = _uiState.value.copy(blogName = config.blogName ?: "")
            } catch (_: Exception) {}
        }
    }

    /** Initial load — fetches posts and categories in parallel. */
    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val posts = postRepository.getRecentPosts()
                val categories = try {
                    postRepository.getCategories()
                } catch (_: Exception) {
                    emptyList()
                }
                val attachmentCount = try {
                    postRepository.getAttachments().size
                } catch (_: Exception) {
                    0
                }
                _uiState.value = _uiState.value.copy(
                    allPosts = posts,
                    posts = applyFilter(posts, _uiState.value.selectedCategorySlug),
                    categories = categories,
                    attachmentCount = attachmentCount,
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
                val categories = try {
                    postRepository.getCategories()
                } catch (_: Exception) {
                    _uiState.value.categories
                }
                val attachmentCount = try {
                    postRepository.getAttachments().size
                } catch (_: Exception) {
                    _uiState.value.attachmentCount
                }
                _uiState.value = _uiState.value.copy(
                    allPosts = posts,
                    posts = applyFilter(posts, _uiState.value.selectedCategorySlug),
                    categories = categories,
                    attachmentCount = attachmentCount,
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
                val updatedAll = _uiState.value.allPosts.filter { it.cid != cid }
                _uiState.value = _uiState.value.copy(
                    allPosts = updatedAll,
                    posts = applyFilter(updatedAll, _uiState.value.selectedCategorySlug)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Delete failed"
                )
            }
        }
    }

    /**
     * Filter displayed posts by category slug.
     *
     * Pass `null` to show all posts.
     */
    fun filterByCategory(slug: String?) {
        val current = _uiState.value
        // Tapping the already-selected chip clears the filter
        val newSlug = if (slug == current.selectedCategorySlug) null else slug
        _uiState.value = current.copy(
            selectedCategorySlug = newSlug,
            posts = applyFilter(current.allPosts, newSlug)
        )
    }

    private fun applyFilter(posts: List<Post>, categorySlug: String?): List<Post> {
        if (categorySlug.isNullOrBlank()) return posts
        return posts.filter { post ->
            post.categories.any { it.equals(categorySlug, ignoreCase = true) }
        }
    }
}
