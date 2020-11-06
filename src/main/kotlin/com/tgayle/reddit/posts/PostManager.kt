package com.tgayle.reddit.posts

import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.net.RedditAPIService
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

data class ListingParameters(
    val after: String? = null,
    val before: String? = null,
    val count: Int = 25,
)

class ListingLoader<T>(val route: String, val params: ListingParameters) {
    init {
        val query = with(params) {
            "?count=$count${if (after != null)  "&after=$after&" else ""}${if (before != null) "&before=$before" else ""}&"
        }

        val request = Request.Builder().get().url(route + query)

        val client = OkHttpClient()
        val call = client.newCall(request.build())
    }
}

class PostManager(val client: RedditClient, private val service: RedditAPIService) {

    suspend fun getFrontPage() = client.ensureAuth {
        service.getFrontPage()
    }

    suspend fun getComments(subreddit: String, linkId: String) = client.ensureAuth {
        service.getComments(subreddit, linkId)
    }

    suspend fun Link.comments() = client.ensureAuth {
        service.getComments(subreddit, id)
    }
}