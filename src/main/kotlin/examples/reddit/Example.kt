package examples.reddit

import com.tgayle.DesignPatternsRedditAPI.BuildConfig
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.auth.*
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.models.Reply
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {

    runBlocking {
        val reddit =
//            userlessAuthTest()
//            installedAuthTest()
//            scriptAuthTest()
            webappAuthTest()

        frontPageExample(reddit)
//        commentsExample(reddit)
    }
}

suspend fun scriptAuthTest(): RedditAPI {
    val auth = Script(ClientId(BuildConfig.SCRIPT_CLIENT_ID), BuildConfig.SCRIPT_CLIENT_SECRET, BuildConfig.SCRIPT_USERNAME, BuildConfig.SCRIPT_PASSWORD)
    val api = RedditAPI(auth.getClient())
    
    api.authenticate().also(::println)
    return api
}

suspend fun userlessAuthTest(): RedditAPI {
    val auth = Anonymous(ClientId(BuildConfig.SCRIPT_CLIENT_ID), BuildConfig.SCRIPT_CLIENT_SECRET)
    val reddit = RedditAPI(auth.getClient())
    return reddit
}

suspend fun installedAuthTest(): RedditAPI {
    val id = ClientId(BuildConfig.INSTALLED_CLIENT_ID)
    val redirectUri = BuildConfig.INSTALLED_REDIRECT_URI
    val strategy = InstalledApp(id, redirectUri)

    val url = strategy.getAuthorizationUrl(redirectUri, duration = OAuthStrategy.TokenDuration.Permanent, compact = true)

    println("Please login at this account and paste in the URL you're redirected to:")
    println(url)

    val authorizationUrl = readLine()!!

    when (val result = strategy.onAuthorizationComplete(authorizationUrl)) {
        is AuthenticationResult.Success -> {
            return RedditAPI(result.client)
        }
        else -> {
            error(result)
        }
    }
}

suspend fun webappAuthTest(): RedditAPI {
    val id = ClientId(BuildConfig.WEB_CLIENT_ID)
    val redirectUri = BuildConfig.WEB_REDIRECT_URI
    val strategy = WebApp(id, BuildConfig.WEB_CLIENT_SECRET, redirectUri)

    val url = strategy.getAuthorizationUrl(redirectUri, duration = OAuthStrategy.TokenDuration.Permanent, compact = false)

    println("Please login at this account and paste in the URL you're redirected to:")
    println(url)

    val authorizationUrl = readLine()!!

    when (val result = strategy.onAuthorizationComplete(authorizationUrl)) {
        is AuthenticationResult.Success -> {
            return RedditAPI(result.client)
        }
        else -> {
            error(result)
        }
    }
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
    ${"\t"}${post.comments.joinToString("\n\n\t") {
    when (it) {
        is Reply.Comment -> "${it.body}\n\t\t/u/${it.author} - ${it.upvotes} upvotes"
        is Reply.MoreComments -> "${it.count} more comments, ${it.depth} deep"
    }
    }}
    """)
}

suspend fun idiomaticCommentsExample(api: RedditAPI) {
    val (post, comments) = api.posts.getLink("askreddit", "jpfqtc")

    with(api) {
        val hint = comments.data.children.filterIsInstance<Reply.MoreComments>().first()
//        hint.load()
    }

    println("""
    ${post.title}
    /r/${post.subreddit}
        
    Comments:
    ${"\t"}${comments.joinToString("\n\n\t") {
        when (it) {
            is Reply.Comment -> "${it.body}\n\t\t/u/${it.author} - ${it.upvotes} upvotes"
            is Reply.MoreComments -> "${it.count} more comments, ${it.depth} deep"
        }
    }}
    """)
}

suspend fun frontPageExample(reddit: RedditAPI) {
        reddit.posts.getFrontPage()
            .take(1)
            .collect { page ->
                page.forEach { link -> println(link.title) }
            }
//        .onEach {
//            println("Loaded page! before=${it.first().name} after=${it.last().name} size=${it.size}")
//        }
//        .collect()

}