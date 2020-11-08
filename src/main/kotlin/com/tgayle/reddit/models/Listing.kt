package com.tgayle.reddit.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*

/**
 * A Reddit structure used for paginating content. The [Data.before] and [Data.after]
 * properties can be used to retrieve content relative to a specific [Thing].
 */
@Serializable
data class Listing<T: Thing>(
    val kind: String,
    val data: Data<T>
): Iterable<T> {

    override operator fun iterator(): Iterator<T> {
        return data.children.asSequence().map { it.data }.iterator()
    }

    @Serializable
    data class Data<T: Thing> internal constructor(
        val dist: Int?,
        @Serializable(ListingChildListSerializer::class)
        val children: List<Child<T>>,
        val before: String?,
        val after: String?
    )

    @Serializable
    data class Child<T: Thing> internal constructor(val data: T)
}

object ListingChildListSerializer: JsonTransformingSerializer<List<Listing.Child<Thing>>>(ListSerializer(Listing.Child.serializer(Thing.serializer()))) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonArray) return element

        return buildJsonArray {
            // Each child should look like { kind: str, data: obj }
            element.jsonArray.forEach { child ->
                val dataWithKind = buildJsonObject {
                    putJsonObject("data") {
                        child.jsonObject["data"]!!.jsonObject.forEach { key, value ->
                            put(key, value)
                        }

                        put("kind", child.jsonObject["kind"]!!)
                    }
                }

                add(dataWithKind)
            }
        }
    }
}