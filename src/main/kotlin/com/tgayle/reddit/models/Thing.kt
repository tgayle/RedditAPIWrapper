package com.tgayle.reddit.models

import com.tgayle.reddit.models.base.Created
import com.tgayle.reddit.models.base.Votable
import com.tgayle.reddit.net.DoubleAsLongSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A [Thing] is the base of all Reddit entities.
 */
@Serializable
sealed class Thing {
    abstract val id: String

    /**
     * An identifier identifying the [Thing]'s type.
     *
     * @see Kind
     */
    abstract val kind: Kind

    /**
     * A [Thing]'s full name, comprised of its [Kind] and its id.
     */
    val name get() = "${kind.prefix}_$id"

    /**
     * An identifier representing a [Thing]'s type. Each [Kind] has a string
     * value which when combined with a [Thing]'s id, creates a [Thing]'s name.
     */
    enum class Kind(val prefix: String) {
        Comment("t1"),
        Account("t2"),
        Link("t3"),
        Message("t4"),
        Subreddit("t5"),
        Award("t6");

        companion object {
            fun fromPrefix(prefix: String): Kind {
                return Kind.values().find { kind ->
                    kind.prefix == prefix
                } ?: throw IllegalArgumentException("Tried to get Thing prefix for \"$prefix\" but \"$prefix\" does not exist.")
            }
        }
    }
}

// TODO: Resolve correct sealed class when given a [Listing]

@Serializable
data class Link(
    override val id: String,
    val subreddit: String,

    @SerialName("subreddit_id")
    val subredditId: String,
    val selftext: String,

    @SerialName("ups")
    override val upvotes: Int,

    @SerialName("downs")
    override val downvotes: Int,

    @Serializable(DoubleAsLongSerializer::class)
    override val created: Long,

    @Serializable(DoubleAsLongSerializer::class)
    @SerialName("created_utc")
    override val createdUtc: Long,
    val author: String,
    val title: String
): Thing(), Votable, Created {
    override val kind: Kind = Kind.Link
}

@Serializable
data class Comment(
        override val id: String,
        @SerialName("approved_by")
        val approvedBy: String? = null,
        val author: String,
        @SerialName("author_flair_css_class")
        val authorFlairCssClass: String? = null,
        @SerialName("author_flair_text")
        val authorFlairText: String? = null,
        @SerialName("banned_by")
        val bannedBy: String? = null,
        val body: String, // TODO: Properly deserialize posts/comments, and look into processing stuff via OAuth.ys
        @SerialName("body_html")
        val bodyHtml: String,
        // TODO: special prop
        val edited: EditedState = EditedState.Unedited,
        val gilded: Int,
        @SerialName("likes")
        override val liked: Boolean?,
        @SerialName("ups")
        override val upvotes: Int,
        @SerialName("downs")
        override val downvotes: Int,
        @Serializable(DoubleAsLongSerializer::class)
        @SerialName("created")
        override val created: Long,

        @Serializable(DoubleAsLongSerializer::class)
        @SerialName("created_utc")
        override val createdUtc: Long,

        @SerialName("link_author")
        val linkAuthor: String? = null,
        @SerialName("link_id")
        val linkId: String,
        @SerialName("link_title")
        val linkTitle: String? = null,
        @SerialName("link_url")
        val linkUrl: String?,
        @SerialName("num_reports")
        val numReports: Int? = null,
        @SerialName("parent_id")
        val parentId: String,
        val replies: List<Thing>,
        val saved: Boolean,
        val score: Int,
        @SerialName("score_hidden")
        val scoreHidden: Boolean,
        val subreddit: String,
        @SerialName("subreddit_id")
        val subredditId: String,
        val distinguished: String? = null


): Thing(), Votable, Created {
    override val kind: Kind = Kind.Comment
}

@Serializable(EditedState.EditedStateSerializer::class)
sealed class EditedState {
    @Serializable(EditedState.EditedStateSerializer::class)
    object Unedited: EditedState()

    @Serializable(EditedState.EditedStateSerializer::class)
    sealed class Edited: EditedState() {

        @Serializable(EditedState.EditedStateSerializer::class)
        object EditedWithUnknownTime: Edited()

        @Serializable(EditedState.EditedStateSerializer::class)
        data class EditedWithTime(val editedTimeUtc: Long): Edited()
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