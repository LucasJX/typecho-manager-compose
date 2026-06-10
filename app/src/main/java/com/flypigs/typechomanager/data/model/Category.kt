package com.flypigs.typechomanager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val mid: Int = 0,
    val name: String = "",
    val slug: String = "",
    val count: Int = 0
)
