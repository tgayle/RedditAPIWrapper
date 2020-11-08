package com.tgayle.reddit.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(EditedState.EditedStateSerializer::class)
sealed class EditedState {
    @Serializable(EditedStateSerializer::class)
    object Unedited: EditedState()

    @Serializable(EditedStateSerializer::class)
    sealed class Edited: EditedState() {

        @Serializable(EditedStateSerializer::class)
        object EditedWithUnknownTime: Edited()

        @Serializable(EditedStateSerializer::class)
        data class EditedWithTime internal constructor(val editedTimeUtc: Long): Edited()
    }

    object EditedStateSerializer: KSerializer<EditedState> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EditedState", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): EditedState {
            try {
                return if (decoder.decodeBoolean()) {
                    Edited.EditedWithUnknownTime
                } else {
                    Unedited
                }
            } catch (e: Exception) {
                return try {
                    Edited.EditedWithTime(decoder.decodeDouble().toLong())
                } catch (e: Exception) {
                    Edited.EditedWithUnknownTime
                }
            }

        }

        override fun serialize(encoder: Encoder, value: EditedState) {
            when (value) {
                Unedited -> encoder.encodeNull()
                Edited.EditedWithUnknownTime -> encoder.encodeLong(-1)
                is Edited.EditedWithTime -> encoder.encodeLong(value.editedTimeUtc)
            }
        }
    }
}