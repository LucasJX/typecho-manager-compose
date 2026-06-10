package com.flypigs.typechomanager.data.repository

import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.model.Attachment
import com.flypigs.typechomanager.data.model.Category
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.data.remote.CompanionApiClient
import com.flypigs.typechomanager.data.remote.XmlRpcClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for blog posts and categories.
 *
 * Wraps [XmlRpcClient] for the remote API and [ConfigDataStore] for
 * reading the current blog configuration.  An in-memory cache avoids
 * redundant network calls when the user navigates back to the home screen.
 */
@Singleton
class PostRepository @Inject constructor(
    private val xmlRpcClient: XmlRpcClient,
    private val companionApiClient: CompanionApiClient,
    private val configDataStore: ConfigDataStore
) {
    private val cacheMutex = Mutex()
    private var cachedPosts: List<Post>? = null

    /**
     * Fetch recent posts from the blog.
     *
     * @param refresh when `true` the in-memory cache is bypassed.
     * @param numberOfPosts maximum number of posts to fetch (default 20).
     */
    suspend fun getRecentPosts(
        refresh: Boolean = false,
        numberOfPosts: Int = 20
    ): List<Post> = cacheMutex.withLock {
        if (!refresh && cachedPosts != null) {
            return@withLock cachedPosts!!
        }
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        val posts = xmlRpcClient.getRecentPosts(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password,
            numberOfPosts = numberOfPosts
        )
        cachedPosts = posts
        posts
    }

    /** Fetch a single post by its content-id. */
    suspend fun getPost(cid: Int): Post {
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        return xmlRpcClient.getPost(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password,
            postId = cid.toString()
        )
    }

    /**
     * Delete a post by its content-id.
     *
     * On success the post is also evicted from the in-memory cache.
     */
    suspend fun deletePost(cid: Int): Boolean {
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        val result = xmlRpcClient.deletePost(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password,
            postId = cid.toString()
        )
        if (result) {
            cacheMutex.withLock {
                cachedPosts = cachedPosts?.filter { it.cid != cid }
            }
        }
        return result
    }

    /** Fetch all categories for the blog. */
    suspend fun getCategories(): List<Category> {
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        return xmlRpcClient.getCategories(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password
        )
    }

    /** Fetch all attachments for the blog. */
    suspend fun getAttachments(): List<Attachment> {
        val config = configDataStore.getConfig()
        val companionBase = config.blogUrl.ifEmpty {
            config.endpoint.substringBefore("/index.php")
        }.trimEnd('/')
        val (items, _, _) = companionApiClient.listMedia(
            companionBase = companionBase,
            username = config.username,
            password = config.password,
            page = 1,
            pageSize = 1000
        )
        return items
    }

    /** Clear the in-memory cache (e.g. on logout). */
    fun invalidateCache() {
        cachedPosts = null
    }
}
