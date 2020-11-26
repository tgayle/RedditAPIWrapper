package com.tgayle.reddit.models

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            // TODO: It would be better if we could somehow specify where the type discriminator is on the JSON rather than merging the kind into the object itself?
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

data class ListingBuilderParams(
    val before: String? = null,
    val after: String? = null,
    val limit: Int = 25,
    val total: Int = Int.MAX_VALUE
)

@Serializable
internal data class ListingQueryParameters(
    val before: String? = null,
    val after: String? = null,
    val limit: Int = 25
)

internal fun <T: Thing> buildListing(
    params: ListingBuilderParams = ListingBuilderParams(),
    fetchListingSegment: suspend (before: String?, after: String?, limit: Int) -> Listing<T>
): Flow<List<T>> = flow {
    require(!(params.before != null && params.after != null)) { "Before and after parameters cannot be both provided at once." }

    val usingBefore = params.before != null
    val usingAfter = params.after != null || !usingBefore
    var before = params.before
    var after = params.after

    var totalLoaded = 0

    while (true) {
        val page = fetchListingSegment(before, after, params.limit)

        when {
            usingBefore -> before = page.data.before
            usingAfter -> after = page.data.after
        }

        if (page.data.dist == 0) return@flow

        val subListEndingIndex = Integer.min((params.total - totalLoaded), page.data.children.size)
        val takenSection = page.data.children.subList(0, subListEndingIndex)
        totalLoaded += takenSection.size

        emit(takenSection.map { it.data })

        if (totalLoaded == params.total) {
            return@flow
        }
    }
}