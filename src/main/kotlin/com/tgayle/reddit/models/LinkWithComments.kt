package com.tgayle.reddit.models

import com.tgayle.reddit.net.serialization.LinkAndCommentSerializer
import kotlinx.serialization.Serializable

@Serializable
data class LinkWithCommentsWrapper(
    @Serializable(LinkAndCommentSerializer::class)
    val linkWithComments: LinkWithComments
)

@Serializable
data class LinkWithComments(
    val link: Link,
    val comments: Listing<Comment>
) {
    constructor(
        link: Listing<Link>,
        comments: Listing<Comment>
    ): this(link.data.children.first().data, comments)
}