package com.flypigs.typechomanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BlogConfig(
    val endpoint: String = "",
    val username: String = "",
    val password: String = "",
    val blogId: String = "1",
    val blogName: String? = null
)
