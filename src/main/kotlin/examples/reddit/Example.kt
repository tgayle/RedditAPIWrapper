package examples.reddit

import com.tgayle.DesignPatternsRedditAPI.BuildConfig
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.auth.Anonymous
import com.tgayle.reddit.auth.Script
import com.tgayle.reddit.models.ClientId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {

//    val api = RedditAPI()

    runBlocking {
//        frontPageExample(api)

//        commentsExample(api)

//        scriptAuthTest(api)

//        userlessAuthTest()

        val reddit = userlessAuthTest()


        commentsExample(reddit)
    }
}

suspend fun scriptAuthTest(): RedditAPI {
    val auth = Script(BuildConfig.SCRIPT_CLIENT_SECRET, BuildConfig.SCRIPT_USERNAME, BuildConfig.SCRIPT_PASSWORD)
    val api = RedditAPI(RedditClient(
        ClientId(BuildConfig.SCRIPT_CLIENT_ID),
        auth
    ))
    
    api.authenticate().also(::println)
    return api
}

suspend fun userlessAuthTest(): RedditAPI {
    val auth = Anonymous(BuildConfig.SCRIPT_CLIENT_SECRET)
    val client = RedditClient(
        ClientId(BuildConfig.SCRIPT_CLIENT_ID),
        auth
    )

    val reddit = RedditAPI(client)
    return reddit
}

suspend fun subredditTest(reddit: RedditAPI) {
    var numLoaded = 0

    reddit.posts.getSubreddit("programming")
        .flatMapConcat { it.asFlow() }
        .take(30)
        .onEach {
            println(it.title)
        }
        .collect()

//    println("Closed after loading $numLoaded posts.")
}

suspend fun commentsExample(api: RedditAPI) {
    val post = api.posts.getLink("askreddit", "jpfqtc")

    println("""
        ${post.link.title}
        /r/${post.link.subreddit}
        
        Comments:
        \t${post.comments.take(10).joinToString("\n\t") {
            "${it.body}\n\t/u/${it.author} - ${it.upvotes} upvotes"
    }}
    """.trimIndent())
    post.comments.forEach {
        println("${it.author} ${it.parentId} ${it.body}")
    }
}

suspend fun frontPageExample(reddit: RedditAPI) {
    //    reddit.posts.getFrontPage(ListingRequestParams(after = "t3_jon9iz", total = 30, limit = 10))
//        .onEach {
//            numLoaded += it.size
//
//            println("Loaded page! before=${it.first().name} after=${it.last().name} size=${it.size}")
//        }
//        .collect()

}