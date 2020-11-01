package com.tgayle.reddit.models.base

import kotlinx.serialization.SerialName

/**
 * A Reddit [Thing] that can be voted on.
 */
interface Votable {
    @SerialName("ups")
    val upvotes: Int

    @SerialName("downs")
    val downvotes: Int

    /**
     * Whether the current user has liked a [Thing]. True represents an upvote,
     * false a downvote, and null represents no vote.
     */
    @SerialName("likes")
    val liked: Boolean? get() = null
}