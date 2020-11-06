package examples

import androidx.compose.animation.animate
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.tgayle.DesignPatternsRedditAPI.BuildConfig
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.auth.Anonymous
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.models.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val api = RedditAPI(
    RedditClient(
        ClientId(BuildConfig.SCRIPT_CLIENT_ID),
        Anonymous(
            BuildConfig.SCRIPT_CLIENT_SECRET,
        )
    )
)

fun main() = Window {
    MaterialTheme {
        val scope = rememberCoroutineScope()

        var loading by remember { mutableStateOf(false) }
        var posts by remember { mutableStateOf(listOf<Link>()) }
        var currentPost by remember { mutableStateOf<Link?>(null) }

        val refreshPosts = {
            scope.launch {
                loading = true
                posts = withContext(Dispatchers.IO) {
                    api.posts.getFrontPage().toList()
                }
                loading = false
            }
        }

        Scaffold(topBar = {
            MyTopAppBar(onRefreshClick = { refreshPosts() })
        }) {
            Column {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth(0.3f).border(1.dp, Color.Black).fillMaxHeight()) {
                        PostList(
                            Modifier.fillMaxSize(),
                            loading = loading,
                            items = posts,
                            onPostClick = {
                                currentPost = it
                            }
                        )
                    }


                    DetailView(Modifier.fillMaxWidth(0.7f).fillMaxHeight(),
                        post = currentPost,
                        onLoadPressed = {
                            refreshPosts()
                        })
                }
            }
        }

    }
}

@Composable
fun MyTopAppBar(onRefreshClick: () -> Unit) {
    TopAppBar(
        title = { Text("Reddit API Example Application (Design Patterns)") },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Image(
                    asset = imageResource("refresh.png"),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                )
            }
        })
}

@Composable
fun PostList(modifier: Modifier = Modifier, loading: Boolean, items: List<Link>, onPostClick: (Link) -> Unit = {}) {
    Stack(modifier) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .align(Alignment.TopCenter)
        ) {
            LazyColumnFor(items) { link ->
                ListItem(
                    secondaryText = { Text("/u/${link.author} - ${link.upvotes} votes") },
                    modifier = Modifier.clickable { onPostClick(link) }) {
                    Text(link.title, maxLines = 1)
                }
                Divider()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White.copy(alpha = animate(if (loading) 0.66f else 0f)),
            content = emptyContent()
        )

        if (loading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                CircularProgressIndicator(Modifier.preferredSize(32.dp))
            }
        }
    }
}

@Composable
fun DetailView(modifier: Modifier = Modifier, post: Link? = null, onLoadPressed: () -> Unit) {
    Column(modifier) {
        Button(onLoadPressed) {
            Text("Load Posts")
        }

        if (post != null) {
            Card {
                Column {
                    Text(post.title, style = MaterialTheme.typography.h3)
                    Text(
                        "/r/${post.subreddit} - /u/${post.author} - ${post.upvotes} upvotes",
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }
}