package com.tgayle.reddit.net.serialization

import com.tgayle.reddit.models.LinkWithComments
import com.tgayle.reddit.models.Listing
import com.tgayle.reddit.models.Thing
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

object LinkAndCommentSerializer: JsonTransformingSerializer<LinkWithComments>(LinkWithComments.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonArray && element.size == 2) {
            return buildJsonObject {
                put("link", element.first())
                put("comments", element[1])
            }
        }

        return super.transformDeserialize(element)
    }
}