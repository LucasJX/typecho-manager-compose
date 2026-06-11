package com.flypigs.typechomanager.ui.postdetail

import androidx.lifecycle.SavedStateHandle
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

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val cid: Int = savedStateHandle.get<Int>("cid") ?: 0

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
