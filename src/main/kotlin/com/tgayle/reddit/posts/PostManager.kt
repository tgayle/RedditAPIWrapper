package com.tgayle.reddit.posts

import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.models.Listing
import com.tgayle.reddit.models.Thing
import com.tgayle.reddit.net.RedditAPIService
import com.tgayle.reddit.net.serialization.encodeToMap
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Integer.min

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

fun <T: Thing> getListing(
    params: ListingRequestParams = ListingRequestParams(),
    fetchListingSegment: suspend (before: String?, after: String?, limit: Int) -> Listing<T>
): Flow<List<T>> = flow {
    require(!(params.before != null && params.after != null)) { "Before and after parameters cannot be both provided at once." }

    val usingBefore = params.before != null
    val usingAfter  = params.after != null || !usingBefore
    var before      = params.before
    var after       = params.after

    var totalLoaded = 0

    while (true) {
        val page = fetchListingSegment(before, after, params.count)// Listing<T>(Thing.Kind.Link.name, Listing.Data(dist = 25, children = emptyList(), before, after))// getPage()

        when {
            usingBefore -> before = page.data.before
            usingAfter -> after = page.data.after
        }

        if (page.data.dist == 0) return@flow

        val subListEndingIndex = min((params.total - totalLoaded), page.data.children.size)
        val takenSection = page.data.children.subList(0, subListEndingIndex)
        totalLoaded += takenSection.size
        println("$totalLoaded $totalLoaded $subListEndingIndex")
        emit(takenSection.map { it.data })

        if (totalLoaded == params.total) {
            return@flow
        }
    }
}

data class ListingRequestParams(
    val before: String? = null, val after: String? = null, val count: Int = 25, val total: Int = Int.MAX_VALUE
)

@Serializable
internal data class ListingQueryParameters(val before: String? = null, val after: String? = null, val count: Int = 25)

class PostManager(val client: RedditClient, private val service: RedditAPIService) {

    suspend fun getFrontPage(params: ListingRequestParams = ListingRequestParams()) = client.ensureAuth {
        getListing(params) { before, after, limit -> service.getFrontPage(encodeToMap(ListingQueryParameters(before, after, limit))) }
    }

    suspend fun getComments(subreddit: String, linkId: String) = client.ensureAuth {
        service.getComments(subreddit, linkId)
    }

    suspend fun Link.comments() = client.ensureAuth {
        service.getComments(subreddit, id)
    }
}