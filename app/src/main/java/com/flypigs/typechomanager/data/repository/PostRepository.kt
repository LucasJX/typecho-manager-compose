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
    private var cachedCategories: List<Category>? = null
    private var cachedAttachments: List<Attachment>? = null

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
     * Uses companion-api (绕开 Typecho 1.3.0 XMLRPC deletePost bug).
     * On success the post is also evicted from the in-memory cache.
     */
    suspend fun deletePost(cid: Int): Boolean {
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        val companionBase = config.blogUrl.ifEmpty {
            config.endpoint.substringBefore("/index.php")
        }.trimEnd('/')

        val result = companionApiClient.deletePost(
            companionBase = companionBase,
            username = config.username,
            password = config.password,
            cid = cid
        )
        if (result) {
            cacheMutex.withLock {
                cachedPosts = cachedPosts?.filter { it.cid != cid }
            }
        }
        return result
    }

    /** Update a post's status (e.g. "draft", "publish", "private"). */
    suspend fun updatePostStatus(cid: Int, newStatus: String): Boolean {
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        val result = xmlRpcClient.editPost(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password,
            postId = cid.toString(),
            content = mapOf("post_status" to newStatus)
        )
        if (result) {
            cacheMutex.withLock {
                cachedPosts = cachedPosts?.map { post ->
                    if (post.cid == cid) post.copy(status = newStatus) else post
                }
            }
        }
        return result
    }

    /** Fetch all categories for the blog. */
    suspend fun getCategories(refresh: Boolean = false): List<Category> = cacheMutex.withLock {
        if (!refresh && cachedCategories != null) {
            return@withLock cachedCategories!!
        }
        val config = configDataStore.getConfig()
        require(config.endpoint.isNotBlank()) { "Blog endpoint is not configured" }

        val categories = xmlRpcClient.getCategories(
            endpoint = config.endpoint,
            username = config.username,
            password = config.password
        )
        cachedCategories = categories
        categories
    }

    /** Fetch all attachments for the blog. */
    suspend fun getAttachments(refresh: Boolean = false): List<Attachment> = cacheMutex.withLock {
        if (!refresh && cachedAttachments != null) {
            return@withLock cachedAttachments!!
        }
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
        cachedAttachments = items
        items
    }

    /** Clear the in-memory cache (e.g. on logout). */
    fun invalidateCache() {
        cachedPosts = null
        cachedCategories = null
        cachedAttachments = null
    }
}
