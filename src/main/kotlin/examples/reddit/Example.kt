package examples.reddit

import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.models.Listing
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {

    val api = RedditAPI()

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

        val posts = api.posts.getFrontPage()

        for (post in posts) {
            println("""
                ${post.title}
                /u/${post.author} - ${(System.currentTimeMillis() / 1000) - post.created} seconds ago
                /r/${post.subreddit}
                
                
            """.trimIndent())
        }
    }

}