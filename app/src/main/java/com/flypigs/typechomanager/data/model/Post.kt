package com.flypigs.typechomanager.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val cid: Int = 0,
    val title: String = "",
    val slug: String = "",
    val created: Long = 0L,
    val modified: Long = 0L,
    @SerialName("text")
    val text: String = "",
    val status: String = Status.DRAFT.value,
    val author: Int = 0,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val commentCount: Int = 0,
    val viewsCount: Int = 0
) {
    companion object {
        enum class Status(val value: String) {
            PUBLISH("publish"),
            DRAFT("draft"),
            PRIVATE("private");

            companion object {
                fun fromValue(value: String): Status =
                    entries.firstOrNull { it.value == value } ?: DRAFT
            }
        }
    }
}
