package com.flypigs.typechomanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val cid: Int = 0,
    val title: String = "",
    val created: Long = 0L,
    val name: String = "",
    val path: String = "",
    val size: Long = 0L,
    val type: String = "",
    val mime: String = "",
    val url: String = ""
)
