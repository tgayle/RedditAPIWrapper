package com.tgayle.reddit.posts

import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.*
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.net.serialization.encodeToMap

class PostManager(private val client: RedditClient, private val service: RedditAPIService) {

    suspend fun getFrontPage(params: ListingBuilderParams = ListingBuilderParams()) = client.ensureAuth {
        buildListing(params) { before, after, limit -> service.getFrontPage(encodeToMap(ListingQueryParameters(before, after, limit))) }
    }

    suspend fun getSubreddit(subreddit: String, params: ListingBuilderParams = ListingBuilderParams()) = client.ensureAuth {
        buildListing(params) { before, after, limit ->
            service.getSubreddit(subreddit, encodeToMap(ListingQueryParameters(before, after, limit)))
        }
    }

    suspend fun getLink(subreddit: String, linkId: String) = client.ensureAuth {
        service.getLink(subreddit, linkId).let { LinkWithComments(it.first().data.children.first().data as Link, it[1] as Listing<Reply>) }
    }
}