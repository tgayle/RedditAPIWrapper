package com.tgayle.reddit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A [Thing] is the base of all Reddit entities.
 */
@Serializable
sealed class Thing {
    abstract val id: String

    /**
     * An identifier identifying the [Thing]'s type.
     *
     * @see Kind
     */
    abstract val kind: Kind

    /**
     * A [Thing]'s full name, comprised of its [Kind] and its id.
     */
    val name get() = "${kind.prefix}_$id"

    /**
     * An identifier representing a [Thing]'s type. Each [Kind] has a string
     * value which when combined with a [Thing]'s id, creates a [Thing]'s name.
     */
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

// TODO: Resolve correct sealed class when given a [Listing]

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