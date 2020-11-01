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

    @SerialName("likes")
    val liked: Boolean get() = false
}