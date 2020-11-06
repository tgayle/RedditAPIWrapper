package com.tgayle.reddit.net.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

@OptIn(InternalSerializationApi::class)
class MapEncoder: NamedValueEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule
    val map = mutableMapOf<String, String>()

    override fun encodeTaggedValue(tag: String, value: Any) {
        map[tag] = value.toString()
    }

    /* no-op, null fields are not added to map. */
    override fun encodeTaggedNull(tag: String) {
    }
}

/**
 * Converts a Serializable to a Map<String, String> where each key is a property of the Serializable.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> encodeToMap(serializable: T): Map<String, String> {
    val encoder = MapEncoder()
    serializer(typeOf<T>()).serialize(encoder, serializable)
    return encoder.map

}