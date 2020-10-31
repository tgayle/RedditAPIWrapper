package examples.reddit

import com.tgayle.reddit.RedditAPI
import kotlinx.coroutines.runBlocking

fun main() {

    val api = RedditAPI()

    runBlocking {
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