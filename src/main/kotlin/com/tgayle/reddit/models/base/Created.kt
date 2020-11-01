package com.tgayle.reddit.models.base

import kotlinx.serialization.SerialName

/**
 * A Reddit [Thing] that has a definite time of creation.
 */
interface Created {
    val created: Long

    @SerialName("created_utc")
    val createdUtc: Long
}