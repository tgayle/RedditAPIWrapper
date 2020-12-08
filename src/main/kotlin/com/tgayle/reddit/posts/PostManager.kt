package com.tgayle.reddit.posts

import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.*
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.net.defaultJsonConverter
import com.tgayle.reddit.net.serialization.encodeToMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class PostManager(private val client: RedditClient, private val service: RedditAPIService) {
    private val json = defaultJsonConverter()


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

    suspend fun Link.comment(body: String) = client.ensureAuth {
        comment(name, body)
    }

    suspend fun Reply.Comment.reply(body: String) = client.ensureAuth {
        comment(name, body)
    }

    suspend fun Link.vote(liked: Boolean?) {
        service.vote(mapOf(
            "dir" to (if (liked == true) 1 else if (liked == false) -1 else 0).toString(),
            "id" to this.name,
        ))
    }

    private suspend fun comment(fullName: String, body: String): CommentSubmissionBody {
        val response = service.comment(mapOf(
            "api_type" to "json",
            "text" to body,
            "thing_id" to fullName
        ))

        return CommentSubmissionBody(
            // TODO: Move this deserialization into a serializer maybe?
            errors = json.decodeFromJsonElement(response["json"]!!.jsonObject["errors"]!!.jsonArray),
            data = json.decodeFromJsonElement(response["json"]!!.jsonObject["data"]!!.jsonObject["things"]!!.jsonArray.let { originalArray ->
                buildJsonArray {
                    originalArray.forEach { thing ->
                        val thingObj = thing.jsonObject

                        val data = thingObj["data"]!!.jsonObject
                        buildJsonObject {
                            this.put("kind", thingObj["kind"]!!.jsonPrimitive)
                            data.forEach { (key, value) ->
                                this.put(key, value)
                            }
                        }.let { this.add(it) }
                    }
                }
            })
        )
    }
}

@Serializable
data class CommentSubmissionBody(val errors: List<String>, val data: List<Thing>)