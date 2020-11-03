package examples.reddit

import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.auth.AuthenticationStrategy
import com.tgayle.reddit.auth.Script
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.models.EditedState
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.models.Listing
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {

    val api = RedditAPI(ClientId("4nQ5Hw_PASBCFw"))

    runBlocking {
        // Example desired use case:
//        val postLoader = flow<Listing<Link>> {  }
//
//        postLoader
//                // Take pages as long as each page has a post from /r/programming
//                .takeWhile { page -> page.data.children.any { it.data.subreddit == "programming"} }
//                // Extract all the posts from each page.
//                .flatMapConcat { page -> page.data.children.asFlow().map { child -> child.data } }
//                // Only take posts with more than 30k upvotes.
//                .filter { it.upvotes > 30000 }
//                .collect()

//        frontPageExample(api)

//        commentsExample(api)

        scriptAuthTest(api)
    }
}

suspend fun scriptAuthTest(api: RedditAPI) {
    val auth = Script("", "", "")

    api.authenticate(auth).also(::println)
}

suspend fun commentsExample(api: RedditAPI) {
    val comments = api.posts.getComments("homeassistant", "jlecaq").forEach { listing ->
        println("${listing.data.children.size} items loaded.")

        for (comment in listing) {
            println("""
                    ${comment.body}
                    
                    /u/${comment.author} - ${comment.created} - ${comment.edited is EditedState.Edited}
                    score: ${comment.score} upvotes: ${comment.upvotes} downvotes: ${comment.downvotes}
                """.trimIndent())
        }
    }

}

suspend fun frontPageExample(api: RedditAPI) {
    val posts = api.posts.getFrontPage()

    for (post in posts) {
        println("""
                ${post.title}
                /u/${post.author} - ${(System.currentTimeMillis() / 1000) - post.created} seconds ago
                /r/${post.subreddit} - ${post.upvotes}/${post.downvotes}
                ${post.created} ${post.createdUtc}
                ${post.liked}
                
                
            """.trimIndent())
    }
}