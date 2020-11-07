package examples

import androidx.compose.animation.animate
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyColumnForIndexed
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
import com.tgayle.reddit.posts.ListingRequestParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private class Controller(val scope: CoroutineScope) {
    var loading by mutableStateOf(false)
        private set
    var posts by mutableStateOf(listOf<Link>())
        private set
    var currentPost by mutableStateOf<Link?>(null)

    var currentSubreddit: String? by mutableStateOf(null)

    val api = RedditAPI(
        RedditClient(
            ClientId(BuildConfig.SCRIPT_CLIENT_ID),
            Anonymous(
                BuildConfig.SCRIPT_CLIENT_SECRET,
            )
        )
    )

    private suspend fun linkLoader(params: ListingRequestParams = ListingRequestParams()): Flow<List<Link>> {
        return currentSubreddit.let {
            if (it == null) {
                api.posts.getFrontPage()
            } else {
                api.posts.getSubreddit(it, params)
            }
        }
    }

    fun refreshPosts() {
        scope.launch {
            if (loading) {
                println("Didn't try to load page since we're already loading.")
                return@launch
            }
            loading = true
            posts = withContext(Dispatchers.IO) {
                linkLoader()
                    .catch {
                        println(it)
                        loading = false
                    }
                    .firstOrNull() ?: listOf()
            }
            loading = false
        }
    }

    fun loadNextPage(lastItem: Link) {
        scope.launch {
            if (loading) {
                return@launch
            }

            println("Loading next page with after = ${lastItem.name}")
            loading = true

            posts = posts + withContext(Dispatchers.IO) {
                linkLoader(ListingRequestParams(after = lastItem.name)).firstOrNull() ?: listOf()
            }

            loading = false
        }
    }

    fun loadSubreddit(subreddit: String) {
        currentSubreddit = (if (subreddit.isBlank()) null else subreddit.replace(" ", ""))
        refreshPosts()
    }
}

fun main() = Window {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val controller = remember { Controller(scope) }

        onActive {
            controller.refreshPosts()
        }

        Scaffold(topBar = {
            MyTopAppBar(
                onRefreshClick = { controller.refreshPosts() },
                currentSubreddit = controller.currentSubreddit
            )
        }) {
            Column {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth(0.3f).border(1.dp, Color.Black).fillMaxHeight()) {
                        PostList(
                            Modifier.fillMaxSize(),
                            loading = controller.loading,
                            items = controller.posts,
                            currentSubreddit = controller.currentSubreddit,
                            onPostClick = {
                                controller.currentPost = it
                            },
                            onEndOfPageReached = { lastItem -> controller.loadNextPage(lastItem) },
                            onSubredditChanged = { controller.loadSubreddit(it) }
                        )
                    }


                    DetailView(Modifier.fillMaxWidth(animate(if (controller.currentPost != null) 0.7f else 0.3f))
                        .fillMaxHeight(),
                        post = controller.currentPost,
                        onLoadPressed = { controller.refreshPosts() }
                    )
                }
            }
        }

    }
}

@Composable
private fun MyTopAppBar(
    onRefreshClick: () -> Unit,
    currentSubreddit: String?
) {
    TopAppBar(
        title = { Text(if (currentSubreddit == null) "Front Page" else "/r/$currentSubreddit") },
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
private fun PostList(
    modifier: Modifier = Modifier,
    loading: Boolean,
    items: List<Link>,
    currentSubreddit: String? = null,
    onSubredditChanged: (String) -> Unit = {},
    onPostClick: (Link) -> Unit = {},
    onEndOfPageReached: (lastItem: Link) -> Unit = {}
) {

    Stack(modifier) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .align(Alignment.TopCenter)
        ) {

            Card(modifier = Modifier.padding(8.dp), elevation = 8.dp) {
                PostListSearchBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onSubredditChanged,
                    currentSubreddit
                )
            }

            PostListItems(items, onPostClick, onEndOfPageReached)
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
private fun PostListSearchBar(
    modifier: Modifier = Modifier,
    onSubredditChanged: (String) -> Unit,
    currentSubreddit: String?
) {
    var subredditText by remember { mutableStateOf(currentSubreddit ?: "") }

    ListItem() {
        OutlinedTextField(
            modifier = modifier,
            value = subredditText,
            onValueChange = {
                if ("\n" in it) {
                    onSubredditChanged(subredditText)
                } else {
                    subredditText = it
                }
            },
            placeholder = { Text(if (currentSubreddit.isNullOrBlank()) "Front Page" else "/r/$currentSubreddit") }
        )
    }
}

@Composable
private fun PostListItems(
    items: List<Link>,
    onPostClick: (Link) -> Unit,
    onEndOfPageReached: (lastItem: Link) -> Unit
) {
    LazyColumnForIndexed(items) { index, link ->
        ListItem(
            secondaryText = { Text("/u/${link.author} - ${link.upvotes} votes") },
            modifier = Modifier.clickable { onPostClick(link) }) {
            Text(link.title, maxLines = 1)
        }
        Divider()

        if (index == items.size - 1) {
            ListItem() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onEndOfPageReached(link) }) {
                        Text("Load next page.")
                    }
                }
            }
        }

    }
}

@Composable
private fun DetailView(modifier: Modifier = Modifier, post: Link? = null, onLoadPressed: () -> Unit) {
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