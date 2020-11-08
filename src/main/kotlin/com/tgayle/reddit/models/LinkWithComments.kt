package com.tgayle.reddit.models

import kotlinx.serialization.Serializable

@Serializable
data class LinkWithComments(
    val link: Link,
    val comments: Listing<Reply>
)