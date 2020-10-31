package com.tgayle.reddit.models

import kotlinx.serialization.Serializable

@Serializable
data class Listing<T: Thing>(
    val kind: String,
    val data: Data<T>
) {

    operator fun iterator(): Iterator<T> {
        return data.children.asSequence().map { it.data }.iterator()
    }

    @Serializable
    data class Data<T: Thing> constructor(
        val dist: Int,
        val children: List<Child<T>>,
        val before: String?,
        val after: String?
    )

    @Serializable
    data class Child<T: Thing>(val kind: String, val data: T)
}