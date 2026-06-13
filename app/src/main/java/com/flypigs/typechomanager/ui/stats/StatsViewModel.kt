package com.flypigs.typechomanager.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flypigs.typechomanager.data.model.Category
import com.flypigs.typechomanager.data.model.Post
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

data class MonthlyPosts(
    val month: String,  // "1月", "2月", ...
    val count: Int,
)

data class CategoryStat(
    val name: String,
    val count: Int,
)

data class StatsUiState(
    val totalPosts: Int = 0,
    val publishedCount: Int = 0,
    val draftCount: Int = 0,
    val categoryCount: Int = 0,
    val attachmentCount: Int = 0,
    val totalViews: Int = 0,
    val totalComments: Int = 0,
    val monthlyPosts: List<MonthlyPosts> = emptyList(),
    val categoryStats: List<CategoryStat> = emptyList(),
    val recentPosts: List<Post> = emptyList(),
    val avgPostsPerMonth: Float = 0f,
    val longestStreak: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val postRepository: PostRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
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

                val published = posts.filter { it.status == "publish" }
                val drafts = posts.filter { it.status == "draft" || it.status == "private" }

                // 月度文章统计（最近12个月）
                val monthlyPosts = calculateMonthlyPosts(posts)

                // 分类统计
                val categoryStats = categories.map { cat ->
                    CategoryStat(
                        name = cat.name,
                        count = posts.count { post ->
                            post.categories?.contains(cat.name) == true
                        }
                    )
                }.sortedByDescending { it.count }

                // 平均每月文章数
                val monthsWithPosts = monthlyPosts.count { it.count > 0 }
                val avgPosts = if (monthsWithPosts > 0) {
                    posts.size.toFloat() / monthsWithPosts
                } else 0f

                // 最长连续发文天数
                val streak = calculateLongestStreak(posts)

                // 总浏览量和评论数
                val totalViews = posts.sumOf { it.viewsCount }
                val totalComments = posts.sumOf { it.commentCount }

                _uiState.value = _uiState.value.copy(
                    totalPosts = posts.size,
                    publishedCount = published.size,
                    draftCount = drafts.size,
                    categoryCount = categories.size,
                    attachmentCount = attachments.size,
                    totalViews = totalViews,
                    totalComments = totalComments,
                    monthlyPosts = monthlyPosts,
                    categoryStats = categoryStats,
                    recentPosts = posts.sortedByDescending { it.created }.take(10),
                    avgPostsPerMonth = avgPosts,
                    longestStreak = streak,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }

    private fun calculateMonthlyPosts(posts: List<Post>): List<MonthlyPosts> {
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("M月", Locale.CHINA)
        val result = mutableListOf<MonthlyPosts>()

        // 最近12个月
        for (i in 11 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.MONTH, -i)
            val monthLabel = monthFormat.format(tempCal.time)

            val year = tempCal.get(Calendar.YEAR)
            val month = tempCal.get(Calendar.MONTH)

            val count = posts.count { post ->
                val postCal = Calendar.getInstance().apply {
                    timeInMillis = post.created * 1000L
                }
                postCal.get(Calendar.YEAR) == year && postCal.get(Calendar.MONTH) == month
            }
            result.add(MonthlyPosts(monthLabel, count))
        }
        return result
    }

    private fun calculateLongestStreak(posts: List<Post>): Int {
        if (posts.isEmpty()) return 0
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val publishDays = posts
            .filter { it.status == "publish" }
            .map { post ->
                dayFormat.format(Calendar.getInstance().apply {
                    timeInMillis = post.created * 1000L
                }.time)
            }
            .distinct()
            .sorted()

        if (publishDays.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1
        for (i in 1 until publishDays.size) {
            val prev = dayFormat.parse(publishDays[i - 1])!!
            val curr = dayFormat.parse(publishDays[i])!!
            val diff = ((curr.time - prev.time) / (1000 * 60 * 60 * 24)).toInt()
            if (diff == 1) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        return maxStreak
    }
}
