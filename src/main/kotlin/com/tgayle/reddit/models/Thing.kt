package com.tgayle.reddit.models

import com.tgayle.reddit.models.base.Created
import com.tgayle.reddit.models.base.Votable
import com.tgayle.reddit.net.serialization.CommentReplySerializer
import com.tgayle.reddit.net.serialization.DoubleAsLongSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
//    abstract val kind: Kind

    abstract fun kind(): Kind

    /**
     * A [Thing]'s full name, comprised of its [Kind] and its id.
     */
    val name get() = "${kind().prefix}_$id"

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
        Award("t6"),
        More("");

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
@SerialName("t3")
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
    override fun kind(): Kind = Kind.Link
}

@Serializable
sealed class Reply: Thing() {
    @Serializable
    @SerialName("t1")
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
        val linkUrl: String? = null,
        @SerialName("num_reports")
        val numReports: Int? = null,
        @SerialName("parent_id")
        val parentId: String? = null,
        @Serializable(CommentReplySerializer::class)
        val replies: Listing<Reply>?,
        val saved: Boolean,
        val score: Int,
        @SerialName("score_hidden")
        val scoreHidden: Boolean,
        val subreddit: String,
        @SerialName("subreddit_id")
        val subredditId: String,
        val distinguished: String? = null
    ): Reply(), Votable, Created {
        override fun kind(): Kind = Kind.Comment
    }


    @Serializable
    @SerialName("more")
    data class MoreComments(
        val count: Int,
        @SerialName("parent_id")
        val parentId: String,
        val depth: Int,
        val children: List<String>,
        override val id: String
    ): Reply() {
        override fun kind(): Kind = Kind.More
    }
}

@Serializable
@SerialName("t2")
data class Account(
    override val id: String,

    @SerialName("comment_karma")
    val commentKarma: Int,
    @SerialName("has_name")
    val hasName: Boolean? = null,
    @SerialName("has_mod_mail")
    val hasModMail: Boolean,
    @SerialName("has_verified_email")
    val hasVerifiedEmail: Boolean,
    @SerialName("inbox_count")
    val inboxCount: Int?,
    @SerialName("is_friend")
    val isFriend: Boolean? = null,
    @SerialName("is_gold")
    val isGold: Boolean,
    @SerialName("is_mod")
    val isMod: Boolean,
    @SerialName("link_karma")
    val linkKarma: Int,
    @SerialName("name")
    val username: String,
    @SerialName("over_18")
    val over18: Boolean,

): Thing() {
    override fun kind(): Kind = Kind.Account

}