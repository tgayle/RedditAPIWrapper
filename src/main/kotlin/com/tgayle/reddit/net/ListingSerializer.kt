package com.tgayle.reddit.net

import com.tgayle.reddit.models.Listing
import com.tgayle.reddit.models.Thing
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ListingSerializer<T: Thing>(private val serializer: KSerializer<T>): KSerializer<Listing<T>> {
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun deserialize(decoder: Decoder): Listing<T> {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Listing<T>) {
        TODO("Not yet implemented")
    }

}