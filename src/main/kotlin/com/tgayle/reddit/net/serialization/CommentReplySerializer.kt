package com.tgayle.reddit.net.serialization

import com.tgayle.reddit.models.Listing
import com.tgayle.reddit.models.Thing
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

object CommentReplySerializer: KSerializer<Listing<Thing>?> {
    override fun deserialize(decoder: Decoder): Listing<Thing>? {
        try {
            decoder.decodeString()
            return null
        } catch (e: Exception) {
            try {
                decoder.decodeNull()
                return null

            } catch (e: Exception) {
                return decoder.decodeNullableSerializableValue(serializer())
            }
        }
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CommentReplies")

    override fun serialize(encoder: Encoder, value: Listing<Thing>?) {
        TODO("This should not be serialized.")
    }
}