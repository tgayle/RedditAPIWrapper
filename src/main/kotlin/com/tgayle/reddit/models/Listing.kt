package com.tgayle.reddit.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

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
    data class Data<T: Thing> constructor(
        val dist: Int?,
        @Serializable(ChildListSerializer::class)
        val children: List<Child<T>>,
        val before: String?,
        val after: String?
    )

    @Serializable
    data class Child<T: Thing>(val data: T)
}

object ChildListSerializer: JsonTransformingSerializer<List<Listing.Child<Thing>>>(ListSerializer(Listing.Child.serializer(Thing.serializer()))) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
//        return buildJsonObject {
//            putJsonObject("data") {
//                element.jsonObject["data"]!!.jsonObject.forEach { key, value ->
//                    put(key, value)
//                }
//
//                put("kind", element.jsonObject["kind"]!!)
//            }
//        }
        if (element !is JsonArray) return element

        val array = buildJsonArray {
            // Each child should look like { kind: str, data: obj }
            element.jsonArray.map { child ->

                val data = buildJsonObject {
                    child.jsonObject["data"]!!.jsonObject.forEach { key, value ->
                        put(key, value)
                    }

                    put("kind", child.jsonObject["kind"]!!)
                }

                val parent = buildJsonObject { put("data", data) }
                parent
//                add(buildJsonArray { add(parent) })
            }.forEach(this::add)
        }

        return array
    }
}