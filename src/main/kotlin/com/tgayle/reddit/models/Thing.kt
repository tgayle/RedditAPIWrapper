package com.tgayle.reddit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Thing {
    abstract val id: String
    abstract val kind: Kind

    val fullName get() = "${kind.prefix}_$id"

    enum class Kind(val prefix: String) {
        Comment("t1"),
        Account("t2"),
        Link("t3"),
        Message("t4"),
        Subreddit("t5"),
        Award("t6");

        companion object {
            fun fromPrefix(prefix: String): Kind {
                return Kind.values().find { kind ->
                    kind.prefix == prefix
                } ?: throw IllegalArgumentException("Tried to get Thing prefix for \"$prefix\" but \"$prefix\" does not exist.")
            }
        }
    }
}

@Serializable
data class Link(
    override val id: String,
    val subreddit: String,

    @SerialName("subreddit_id")
    val subredditId: String,
    val selftext: String,

    @SerialName("ups")
    val upvotes: Int,
    val created: Double,
    val author: String,
    val title: String
): Thing() {
    override val kind: Kind = Kind.Link
}