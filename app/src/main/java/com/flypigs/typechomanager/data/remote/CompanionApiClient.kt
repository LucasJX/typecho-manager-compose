package com.flypigs.typechomanager.data.remote

import com.flypigs.typechomanager.data.model.Attachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP client for the Typecho companion-api.php plugin.
 *
 * All endpoints are relative to a base URL configured per-blog.
 * Responses are JSON.
 */
@Singleton
class CompanionApiClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    // ------------------------------------------------------------------ //
    //  Public API                                                         //
    // ------------------------------------------------------------------ //

    /**
     * List media attachments with pagination.
     *
     * @param companionBase  e.g. "https://example.com/usr/plugins/Companion"
     * @param page           1-based page number
     * @param pageSize       items per page
     * @param mime           optional MIME filter (e.g. "image")
     * @return               Pair of (items, totalPages)
     */
    suspend fun listMedia(
        companionBase: String,
        page: Int = 1,
        pageSize: Int = 20,
        mime: String? = null
    ): Pair<List<Attachment>, Int> = withContext(Dispatchers.IO) {
        val url = buildString {
            append(companionBase.trimEnd('/'))
            append("/api.php?action=list&page=$page&pageSize=$pageSize")
            if (!mime.isNullOrBlank()) {
                append("&mime=${urlEncode(mime)}")
            }
        }
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
            }
            val body = response.body?.string()
                ?: throw CompanionApiException("Empty response body")
            val dto = json.decodeFromString<ListMediaResponse>(body)
            Pair(dto.items.map { it.toAttachment() }, dto.totalPages)
        }
    }

    /**
     * Upload a media file.
     *
     * @param companionBase  base URL of companion plugin
     * @param fileName       original file name (e.g. "photo.jpg")
     * @param bytes          raw file bytes
     * @param parentCid      parent content CID (0 = unattached)
     * @return               the created [Attachment]
     */
    suspend fun uploadMedia(
        companionBase: String,
        fileName: String,
        bytes: ByteArray,
        parentCid: Int = 0
    ): Attachment = withContext(Dispatchers.IO) {
        val url = "${companionBase.trimEnd('/')}/api.php?action=upload"

        val fileBody = bytes.toRequestBody(guessMimeType(fileName))
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, fileBody)
            .apply {
                if (parentCid > 0) {
                    addFormDataPart("parent", parentCid.toString())
                }
            }
            .build()

        val request = Request.Builder()
            .url(url)
            .post(multipart)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
            }
            val body = response.body?.string()
                ?: throw CompanionApiException("Empty response body")
            json.decodeFromString<AttachmentDto>(body).toAttachment()
        }
    }

    /**
     * Delete a media attachment by CID.
     *
     * @param companionBase  base URL of companion plugin
     * @param cid            attachment CID to delete
     * @return               true on success
     */
    suspend fun deleteMedia(
        companionBase: String,
        cid: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "${companionBase.trimEnd('/')}/api.php?action=delete&cid=$cid"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
            }
            val body = response.body?.string()
                ?: throw CompanionApiException("Empty response body")
            json.decodeFromString<SimpleResponse>(body).success
        }
    }

    // ------------------------------------------------------------------ //
    //  Internal DTOs                                                      //
    // ------------------------------------------------------------------ //

    @Serializable
    private data class ListMediaResponse(
        val items: List<AttachmentDto> = emptyList(),
        val totalPages: Int = 1,
        val total: Int = 0
    )

    @Serializable
    private data class AttachmentDto(
        val cid: Int = 0,
        val title: String = "",
        val created: Long = 0,
        val name: String = "",
        val path: String = "",
        val size: Long = 0,
        val type: String = "",
        val mime: String = "",
        val url: String = ""
    ) {
        fun toAttachment() = Attachment(
            cid = cid,
            title = title,
            created = created,
            name = name,
            path = path,
            size = size,
            type = type,
            mime = mime,
            url = url
        )
    }

    @Serializable
    private data class SimpleResponse(
        val success: Boolean = false,
        val message: String? = null
    )

    // ------------------------------------------------------------------ //
    //  Helpers                                                            //
    // ------------------------------------------------------------------ //

    private fun guessMimeType(fileName: String): okhttp3.MediaType {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val mime = when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "application/xml"
            else -> "application/octet-stream"
        }
        return mime.toMediaType()
    }

    private fun urlEncode(s: String): String =
        java.net.URLEncoder.encode(s, "UTF-8")
}

// ------------------------------------------------------------------ //
//  Exception                                                          //
// ------------------------------------------------------------------ //

class CompanionApiException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
