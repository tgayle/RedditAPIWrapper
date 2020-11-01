package com.tgayle.reddit.models.base

import com.tgayle.reddit.net.DoubleAsLongSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Reddit [Thing] that has a definite time of creation.
 */
interface Created {
    @Serializable(DoubleAsLongSerializer::class)
    @SerialName("created")
    val created: Long

    @Serializable(DoubleAsLongSerializer::class)
    @SerialName("created_utc")
    val createdUtc: Long
}