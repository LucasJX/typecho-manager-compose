1|package com.flypigs.typechomanager.data.remote
2|
3|import com.flypigs.typechomanager.data.model.Attachment
4|import kotlinx.coroutines.Dispatchers
5|import kotlinx.coroutines.withContext
6|import kotlinx.serialization.Serializable
7|import kotlinx.serialization.json.Json
8|import okhttp3.MediaType.Companion.toMediaType
9|import okhttp3.MultipartBody
10|import okhttp3.OkHttpClient
11|import okhttp3.Request
12|import okhttp3.RequestBody.Companion.toRequestBody
13|import javax.inject.Inject
14|import javax.inject.Singleton
15|
16|/**
17| * HTTP client for the Typecho companion-api.php plugin.
18| *
19| * All endpoints are relative to a base URL configured per-blog.
20| * Responses are JSON.
21| */
22|@Singleton
23|class CompanionApiClient @Inject constructor(
24|    private val client: OkHttpClient,
25|    private val json: Json = Json { ignoreUnknownKeys = true }
26|) {
27|    // ------------------------------------------------------------------ //
28|    //  Public API                                                         //
29|    // ------------------------------------------------------------------ //
30|
31|    /**
32|     * List media attachments with pagination.
33|     *
34|     * @param companionBase  blog base URL e.g. "https://example.com"
35|     * @param page           1-based page number
36|     * @param pageSize       items per page
37|     * @param mime           optional MIME filter (e.g. "image")
38|     * @return               Pair of (items, totalPages)
39|     */
40|    suspend fun listMedia(
41|        companionBase: String,
42|        page: Int = 1,
43|        pageSize: Int = 20,
44|        mime: String? = null
45|    ): Pair<List<Attachment>, Int> = withContext(Dispatchers.IO) {
46|        val url = buildString {
47|            append(companionBase.trimEnd('/'))
48|            append("/companion-api.php?action=list_media&pg=$page&size=$pageSize")
49|            if (!mime.isNullOrBlank()) {
50|                append("&type=${urlEncode(mime)}")
51|            }
52|        }
53|        val request = Request.Builder()
54|            .url(url)
55|            .get()
56|            .build()
57|
58|        client.newCall(request).execute().use { response ->
59|            if (!response.isSuccessful) {
60|                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
61|            }
62|            val body = response.body?.string()
63|                ?: throw CompanionApiException("Empty response body")
64|            val dto = json.decodeFromString<ListMediaResponse>(body)
65|            val totalPages = if (dto.total <= pageSize) 1 else
66|                (dto.total + pageSize - 1) / pageSize
67|            Pair(dto.items.map { it.toAttachment() }, totalPages)
68|        }
69|    }
70|
71|    /**
72|     * Upload a media file.
73|     *
74|     * @param companionBase  base URL of companion plugin
75|     * @param fileName       original file name (e.g. "photo.jpg")
76|     * @param bytes          raw file bytes
77|     * @param parentCid      parent content CID (0 = unattached)
78|     * @return               the created [Attachment]
79|     */
80|    suspend fun uploadMedia(
81|        companionBase: String,
82|        fileName: String,
83|        bytes: ByteArray,
84|        parentCid: Int = 0
85|    ): Attachment = withContext(Dispatchers.IO) {
86|        val url = "${companionBase.trimEnd('/')}/companion-api.php?action=upload_media"
87|
88|        val fileBody = bytes.toRequestBody(guessMimeType(fileName))
89|        val multipart = MultipartBody.Builder()
90|            .setType(MultipartBody.FORM)
91|            .addFormDataPart("file", fileName, fileBody)
92|            .apply {
93|                if (parentCid > 0) {
94|                    addFormDataPart("parent", parentCid.toString())
95|                }
96|            }
97|            .build()
98|
99|        val request = Request.Builder()
100|            .url(url)
101|            .post(multipart)
102|            .build()
103|
104|        client.newCall(request).execute().use { response ->
105|            if (!response.isSuccessful) {
106|                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
107|            }
108|            val body = response.body?.string()
109|                ?: throw CompanionApiException("Empty response body")
110|            json.decodeFromString<AttachmentDto>(body).toAttachment()
111|        }
112|    }
113|
114|    /**
115|     * Delete a media attachment by CID.
116|     *
117|     * @param companionBase  base URL of companion plugin
118|     * @param cid            attachment CID to delete
119|     * @return               true on success
120|     */
121|    suspend fun deleteMedia(
122|        companionBase: String,
123|        cid: Int
124|    ): Boolean = withContext(Dispatchers.IO) {
125|        val url = "${companionBase.trimEnd('/')}/companion-api.php?action=delete_media&cid=$cid"
126|
127|        val request = Request.Builder()
128|            .url(url)
129|            .delete()
130|            .build()
131|
132|        client.newCall(request).execute().use { response ->
133|            if (!response.isSuccessful) {
134|                throw CompanionApiException("HTTP ${response.code}: ${response.message}")
135|            }
136|            val body = response.body?.string()
137|                ?: throw CompanionApiException("Empty response body")
138|            json.decodeFromString<SimpleResponse>(body).success
139|        }
140|    }
141|
142|    // ------------------------------------------------------------------ //
143|    //  Internal DTOs                                                      //
144|    // ------------------------------------------------------------------ //
145|
146|    @Serializable
147|    private data class ListMediaResponse(
148|        val items: List<AttachmentDto> = emptyList(),
149|        val totalPages: Int = 1,
150|        val total: Int = 0
151|    )
152|
153|    @Serializable
154|    private data class AttachmentDto(
155|        val cid: Int = 0,
156|        val title: String = "",
157|        val created: Long = 0,
158|        val name: String = "",
159|        val path: String = "",
160|        val size: Long = 0,
161|        val type: String = "",
162|        val mime: String = "",
163|        val url: String = ""
164|    ) {
165|        fun toAttachment() = Attachment(
166|            cid = cid,
167|            title = title,
168|            created = created,
169|            name = name,
170|            path = path,
171|            size = size,
172|            type = type,
173|            mime = mime,
174|            url = url
175|        )
176|    }
177|
178|    @Serializable
179|    private data class SimpleResponse(
180|        val success: Boolean = false,
181|        val message: String? = null
182|    )
183|
184|    // ------------------------------------------------------------------ //
185|    //  Helpers                                                            //
186|    // ------------------------------------------------------------------ //
187|
188|    private fun guessMimeType(fileName: String): okhttp3.MediaType {
189|        val ext = fileName.substringAfterLast('.', "").lowercase()
190|        val mime = when (ext) {
191|            "jpg", "jpeg" -> "image/jpeg"
192|            "png" -> "image/png"
193|            "gif" -> "image/gif"
194|            "webp" -> "image/webp"
195|            "svg" -> "image/svg+xml"
196|            "mp4" -> "video/mp4"
197|            "webm" -> "video/webm"
198|            "pdf" -> "application/pdf"
199|            "zip" -> "application/zip"
200|            "txt" -> "text/plain"
201|            "html", "htm" -> "text/html"
202|            "css" -> "text/css"
203|            "js" -> "application/javascript"
204|            "json" -> "application/json"
205|            "xml" -> "application/xml"
206|            else -> "application/octet-stream"
207|        }
208|        return mime.toMediaType()
209|    }
210|
211|    private fun urlEncode(s: String): String =
212|        java.net.URLEncoder.encode(s, "UTF-8")
213|}
214|
215|// ------------------------------------------------------------------ //
216|//  Exception                                                          //
217|// ------------------------------------------------------------------ //
218|
219|class CompanionApiException(message: String, cause: Throwable? = null) :
220|    RuntimeException(message, cause)
221|