package com.flypigs.typechomanager.data.remote

import com.flypigs.typechomanager.data.model.Category
import com.flypigs.typechomanager.data.model.Post
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * XML-RPC client for Typecho blogs (MetaWeblog / WordPress API).
 *
 * Handles Typecho-specific quirks:
 * - Passwords containing '+' must be URL-encoded to '%2B' to avoid
 *   the XML-RPC layer interpreting it as a space character.
 * - Typecho returns integer values as `<int>` (not `<i4>`).
 */
@Singleton
class XmlRpcClient @Inject constructor(
    private val client: OkHttpClient
) {
    companion object {
        private val XML_MEDIA_TYPE = "text/xml; charset=utf-8".toMediaType()

        // MetaWeblog method names
        private const val METHOD_GET_RECENT_POSTS = "metaWeblog.getRecentPosts"
        private const val METHOD_GET_POST = "metaWeblog.getPost"
        private const val METHOD_NEW_POST = "metaWeblog.newPost"
        private const val METHOD_EDIT_POST = "metaWeblog.editPost"
        private const val METHOD_DELETE_POST = "blogger.deletePost"
        private const val METHOD_GET_CATEGORIES = "wp.getCategories"
    }

    // ------------------------------------------------------------------ //
    //  Public API                                                         //
    // ------------------------------------------------------------------ //

    suspend fun getRecentPosts(
        endpoint: String,
        username: String,
        password: String,
        numberOfPosts: Int = 20
    ): List<Post> {
        val xml = buildMethodCall(
            METHOD_GET_RECENT_POSTS,
            listOf(
                valueString("1"), // blogId (Typecho default)
                valueString(username),
                valueString(encodePassword(password)),
                valueInt(numberOfPosts)
            )
        )
        val response = execute(endpoint, xml)
        return parseArray(response) { parsePostStruct(it) }
    }

    suspend fun getPost(
        endpoint: String,
        username: String,
        password: String,
        postId: String
    ): Post {
        val xml = buildMethodCall(
            METHOD_GET_POST,
            listOf(
                valueString(postId),
                valueString(username),
                valueString(encodePassword(password))
            )
        )
        val response = execute(endpoint, xml)
        return parseSingleStruct(response) { parsePostStruct(it) }
    }

    suspend fun newPost(
        endpoint: String,
        username: String,
        password: String,
        content: Map<String, Any?>
    ): String {
        val xml = buildMethodCall(
            METHOD_NEW_POST,
            listOf(
                valueString("1"), // blogId
                valueString(username),
                valueString(encodePassword(password)),
                buildStruct(content),
                valueBoolean(true) // publish flag
            )
        )
        val response = execute(endpoint, xml)
        return parseSingleValue(response) // returns post ID as string
    }

    suspend fun editPost(
        endpoint: String,
        username: String,
        password: String,
        postId: String,
        content: Map<String, Any?>
    ): Boolean {
        val xml = buildMethodCall(
            METHOD_EDIT_POST,
            listOf(
                valueString(postId),
                valueString(username),
                valueString(encodePassword(password)),
                buildStruct(content),
                valueBoolean(true)
            )
        )
        val response = execute(endpoint, xml)
        return parseSingleValue(response).toBoolean()
    }

    suspend fun deletePost(
        endpoint: String,
        username: String,
        password: String,
        postId: String
    ): Boolean {
        val xml = buildMethodCall(
            METHOD_DELETE_POST,
            listOf(
                valueString("appkey"), // unused by Typecho, but required by spec
                valueString(postId),
                valueString(username),
                valueString(encodePassword(password)),
                valueBoolean(true)
            )
        )
        val response = execute(endpoint, xml)
        return parseSingleValue(response).toBoolean()
    }

    suspend fun getCategories(
        endpoint: String,
        username: String,
        password: String
    ): List<Category> {
        val xml = buildMethodCall(
            METHOD_GET_CATEGORIES,
            listOf(
                valueString("1"), // blogId (Typecho default)
                valueString(username),
                valueString(encodePassword(password))
            )
        )
        val response = execute(endpoint, xml)
        return parseArray(response) { parseCategoryStruct(it) }
    }

    // ------------------------------------------------------------------ //
    //  Password encoding (Typecho quirk)                                  //
    // ------------------------------------------------------------------ //

    /**
     * URL-encode the password so that '+' becomes '%2B'.
     * Typecho's XML-RPC handler decodes the password before comparing it
     * to the stored hash. If we send a raw '+', the server-side URL-decoder
     * turns it into a space, causing authentication to fail.
     */
    private fun encodePassword(password: String): String =
        URLEncoder.encode(password, "UTF-8")

    // ------------------------------------------------------------------ //
    //  HTTP execution                                                     //
    // ------------------------------------------------------------------ //

    private suspend fun execute(endpoint: String, xmlBody: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(endpoint)
            .post(xmlBody.toRequestBody(XML_MEDIA_TYPE))
            .header("Content-Type", "text/xml; charset=utf-8")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw XmlRpcException("HTTP ${response.code}: ${response.message}")
            }
            return@withContext response.body?.string()
                ?: throw XmlRpcException("Empty response body")
        }
    }

    // ------------------------------------------------------------------ //
    //  XML-RPC request builder                                            //
    // ------------------------------------------------------------------ //

    private fun buildMethodCall(methodName: String, params: List<String>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("<methodCall>")
        sb.appendLine("  <methodName>$methodName</methodName>")
        sb.appendLine("  <params>")
        for (param in params) {
            sb.appendLine("    <param><value>$param</value></param>")
        }
        sb.appendLine("  </params>")
        sb.appendLine("</methodCall>")
        return sb.toString()
    }

    private fun valueString(v: String): String = "<string>${escapeXml(v)}</string>"

    private fun valueInt(v: Int): String = "<int>$v</int>"

    private fun valueBoolean(v: Boolean): String = "<boolean>${if (v) 1 else 0}</boolean>"

    private fun buildStruct(map: Map<String, Any?>): String {
        val sb = StringBuilder()
        sb.append("<struct>")
        for ((key, value) in map) {
            sb.append("<member>")
            sb.append("<name>${escapeXml(key)}</name>")
            sb.append("<value>")
            sb.append(when (value) {
                is String -> "<string>${escapeXml(value)}</string>"
                is Int -> "<int>$value</int>"
                is Long -> "<int>$value</int>"
                is Boolean -> "<boolean>${if (value) 1 else 0}</boolean>"
                is List<*> -> buildArray(value)
                is Map<*, *> -> buildStruct(value.filterKeys { it is String }.mapKeys { it.key as String }.mapValues { it.value })
                null -> "<string/>"
                else -> "<string>${escapeXml(value.toString())}</string>"
            })
            sb.append("</value>")
            sb.append("</member>")
        }
        sb.append("</struct>")
        return sb.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildArray(items: List<*>): String {
        val sb = StringBuilder()
        sb.append("<array><data>")
        for (item in items) {
            sb.append("<value>")
            sb.append(when (item) {
                is String -> "<string>${escapeXml(item)}</string>"
                is Int -> "<int>$item</int>"
                is Map<*, *> -> buildStruct(item.filterKeys { it is String }.mapKeys { it.key as String }.mapValues { it.value })
                else -> "<string>${escapeXml(item.toString())}</string>"
            })
            sb.append("</value>")
        }
        sb.append("</data></array>")
        return sb.toString()
    }

    private fun escapeXml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    // ------------------------------------------------------------------ //
    //  XML-RPC response parser (XmlPullParser)                            //
    // ------------------------------------------------------------------ //

    /**
     * Parse a `<methodResponse><params>` into a list of [T], one per `<param>`.
     * Each `<param>` is expected to contain a `<struct>`.
     */
    private fun <T> parseArray(xml: String, structParser: (XmlPullParser) -> T): List<T> {
        checkForFault(xml)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val results = mutableListOf<T>()
        var inParams = false

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "params" -> inParams = true
                        "value" -> {
                            if (inParams) {
                                // Drill into the struct inside <value>
                                val struct = findChildTag(parser, "struct")
                                if (struct != null) {
                                    results.add(structParser(struct))
                                }
                            }
                        }
                    }
                }
            }
            parser.next()
        }
        return results
    }

    /**
     * Parse a single `<struct>` from the first `<param>` in the response.
     */
    private fun <T> parseSingleStruct(xml: String, structParser: (XmlPullParser) -> T): T {
        checkForFault(xml)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "struct") {
                return structParser(parser)
            }
            parser.next()
        }
        throw XmlRpcException("No struct found in response")
    }

    /**
     * Parse a single scalar value (string / int / boolean) from the response.
     */
    private fun parseSingleValue(xml: String): String {
        checkForFault(xml)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var insideValue = false
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "value") insideValue = true
                    else if (insideValue && parser.name in listOf("string", "int", "i4", "boolean")) {
                        return parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "value") insideValue = false
                }
            }
            parser.next()
        }
        throw XmlRpcException("No value found in response")
    }

    // ------------------------------------------------------------------ //
    //  Struct parsers                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Parse a MetaWeblog post struct into a [Post].
     *
     * Expected members:
     *  - postid (string)
     *  - title (string)
     *  - link / permalink (string) — ignored for slug, we derive slug from content
     *  - dateCreated (dateTime.iso8601) — mapped to `created`
     *  - mt_text_more / description (string) — mapped to `text`
     *  - post_status (string)
     *  - userid (string) — mapped to `author`
     *  - categories (array of strings)
     *  - mt_keywords (array of strings) — mapped to `tags`
     *  - mt_allow_comments (int) — ignored for now
     *  - wp_slug (string)
     */
    private fun parsePostStruct(parser: XmlPullParser): Post {
        val members = parseStructMembers(parser)
        val cid = (members["postid"] as? String)?.toIntOrNull() ?: 0
        val title = (members["title"] as? String) ?: ""
        val slug = (members["wp_slug"] as? String) ?: ""
        val text = (members["description"] as? String) ?: ""
        val status = (members["post_status"] as? String) ?: Post.Companion.Status.DRAFT.value
        val author = (members["userid"] as? String)?.toIntOrNull() ?: 0
        val created = parseIso8601(members["dateCreated"] as? String)
        val modified = parseIso8601(members["date_modified_gmt"] as? String)
            ?: parseIso8601(members["dateCreated"] as? String)
        val categories = (members["categories"] as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
        val tags = (members["mt_keywords"] as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
        val commentCount = (members["mt_allow_comments"] as? String)?.toIntOrNull() ?: 0

        return Post(
            cid = cid,
            title = title,
            slug = slug,
            created = created,
            modified = modified,
            text = text,
            status = status,
            author = author,
            categories = categories,
            tags = tags,
            commentCount = commentCount
        )
    }

    /**
     * Parse a WordPress category struct into a [Category].
     *
     * Expected members:
     *  - categoryId (string)
     *  - categoryName (string)
     *  - categoryNiceName (string) — mapped to `slug`
     *  - count (int or string)
     */
    private fun parseCategoryStruct(parser: XmlPullParser): Category {
        val members = parseStructMembers(parser)
        val mid = (members["categoryId"] as? String)?.toIntOrNull() ?: 0
        val name = (members["categoryName"] as? String) ?: ""
        val slug = (members["categoryNiceName"] as? String) ?: ""
        val count = (members["count"] as? String)?.toIntOrNull()
            ?: (members["count"] as? Int) ?: 0

        return Category(mid = mid, name = name, slug = slug, count = count)
    }

    /**
     * Generic struct parser: reads `<struct>…</struct>` and returns a map of
     * member name -> value.  Values may be String, Int, List<*>, or nested Map.
     */
    private fun parseStructMembers(parser: XmlPullParser): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        var depth = 0
        var inStruct = false
        var currentName: String? = null
        var inMember = false
        var inName = false
        var inValue = false

        while (true) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "struct" -> {
                            if (inStruct) depth++
                            inStruct = true
                        }
                        "member" -> {
                            if (inStruct) inMember = true
                        }
                        "name" -> {
                            if (inMember) {
                                inName = true
                                currentName = parser.nextText()
                                inName = false
                            }
                        }
                        "value" -> {
                            if (inMember && currentName != null) {
                                inValue = true
                                val v = readValue(parser)
                                result[currentName!!] = v
                                currentName = null
                                inValue = false
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "member" -> inMember = false
                        "struct" -> {
                            if (depth > 0) {
                                depth--
                            } else {
                                return result
                            }
                        }
                    }
                }
                XmlPullParser.END_DOCUMENT -> return result
            }
            parser.next()
        }
    }

    /**
     * Read the contents of a `<value>` element and return a typed Kotlin value.
     * Handles: string, int, i4, boolean, dateTime.iso8601, array, struct.
     */
    private fun readValue(parser: XmlPullParser): Any? {
        // If the next START_TAG is a typed tag, consume it; otherwise the text
        // content is a bare string (XML-RPC allows <value>text</value> without wrapper).
        val outerDepth = parser.depth
        var text: String? = null

        // Peek at next event without consuming — XmlPullParser doesn't have peek,
        // so we just read and branch.
        parser.next()
        when (parser.eventType) {
            XmlPullParser.TEXT -> {
                text = parser.text.trim()
            }
            XmlPullParser.START_TAG -> {
                return when (parser.name) {
                    "string" -> parser.nextText()
                    "int", "i4" -> parser.nextText()
                    "boolean" -> parser.nextText().trim().let { it == "1" || it.equals("true", true) }
                    "dateTime.iso8601" -> parser.nextText()
                    "array" -> parseArrayValues(parser)
                    "struct" -> parseStructMembers(parser)
                    else -> parser.nextText()
                }
            }
        }
        return text ?: ""
    }

    /**
     * Parse `<array><data><value>…</value></data></array>`.
     */
    private fun parseArrayValues(parser: XmlPullParser): List<Any?> {
        val items = mutableListOf<Any?>()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "value") {
                        items.add(readValue(parser))
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "array") return items
                }
            }
            parser.next()
        }
        return items
    }

    // ------------------------------------------------------------------ //
    //  Fault handling                                                     //
    // ------------------------------------------------------------------ //

    private fun checkForFault(xml: String) {
        if (!xml.contains("<fault>")) return

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var inFault = false
        var faultString = ""
        var faultCode = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "fault") inFault = true
                    if (inFault) {
                        when (parser.name) {
                            "name" -> {
                                val n = parser.nextText()
                                // Read the value that follows
                                // We rely on parseStructMembers-style reading
                            }
                        }
                    }
                }
            }
            parser.next()
        }

        // Simpler extraction via regex for fault details
        val codeMatch = Regex("""<name>faultCode</name>\s*<value>\s*<(?:int|i4|string)>(\d+)</""").find(xml)
        val msgMatch = Regex("""<name>faultString</name>\s*<value>\s*<string>(.*?)</string>""").find(xml)
        faultCode = codeMatch?.groupValues?.get(1) ?: "unknown"
        faultString = msgMatch?.groupValues?.get(1) ?: "Unknown XML-RPC fault"

        throw XmlRpcFaultException(faultCode.toIntOrNull() ?: -1, faultString)
    }

    // ------------------------------------------------------------------ //
    //  Utilities                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Navigate the parser forward until a START_TAG named [tagName] is found
     * at the current depth.  Returns the parser positioned at that tag, or null.
     */
    private fun findChildTag(parser: XmlPullParser, tagName: String): XmlPullParser? {
        val startDepth = parser.depth
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == tagName && parser.depth == startDepth + 1) {
                        return parser
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.depth < startDepth) return null
                }
            }
        }
        return null
    }

    /**
     * Parse ISO 8601 / XML-RPC dateTime.iso8601 format to epoch millis.
     * Typecho sends: "20060102T15:04:05" or "2006-01-02T15:04:05".
     */
    private fun parseIso8601(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        return try {
            // Try java.time first (API 26+ guaranteed, or desugared)
            val cleaned = dateStr.replace("Z", "").trim()
            val temporal = java.time.LocalDateTime.parse(
                cleaned,
                java.time.format.DateTimeFormatter.ofPattern(
                    if (cleaned.contains("-")) "yyyy-MM-dd'T'HH:mm:ss" else "yyyyMMdd'T'HH:mm:ss"
                )
            )
            temporal.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }
}

// ------------------------------------------------------------------ //
//  Exceptions                                                         //
// ------------------------------------------------------------------ //

open class XmlRpcException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class XmlRpcFaultException(val faultCode: Int, val faultString: String) :
    XmlRpcException("XML-RPC fault $faultCode: $faultString")
