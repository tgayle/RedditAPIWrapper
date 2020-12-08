package examples.screens

import androidx.compose.animation.animate
import androidx.compose.desktop.AppWindowAmbient
import androidx.compose.desktop.ComposePanel
import androidx.compose.desktop.Window
import androidx.compose.desktop.initCompose
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.currentTextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientWindowManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sun.net.httpserver.HttpServer
import com.tgayle.DesignPatternsRedditAPI.BuildConfig
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.auth.Anonymous
import com.tgayle.reddit.auth.AuthenticationResult
import com.tgayle.reddit.auth.InstalledApp
import com.tgayle.reddit.models.Account
import com.tgayle.reddit.models.ClientId
import com.tgayle.reddit.models.Link
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import java.awt.Desktop
import java.awt.event.FocusEvent
import java.net.InetSocketAddress
import java.net.URI
import javax.swing.JFrame

val anonymousClient = Anonymous(
    ClientId(BuildConfig.SCRIPT_CLIENT_ID),
    BuildConfig.SCRIPT_CLIENT_SECRET,
).getClient()

@Composable
fun MainScreen(parentFrame: JFrame) {
    var drawerState = rememberDrawerState(DrawerValue.Closed)
    var state = rememberScaffoldState(drawerState)
    var tabs by remember { mutableStateOf(mutableSetOf<RedditTabItem>(RedditTabItem.FrontPage)) }
    var currentTab: RedditTabItem by remember { mutableStateOf(RedditTabItem.FrontPage) }
    var accountSwitcherOpen by remember { mutableStateOf(false) }
    var currentUser: Account? by remember { mutableStateOf(null) }

    var loading by remember { mutableStateOf(false) }
    var api by remember {
        mutableStateOf(RedditAPI(anonymousClient))
    }

    var redditLinks by remember { mutableStateOf(mutableListOf<Link>()) }
    val loadMorePosts = Channel<Unit>(Channel.RENDEZVOUS)

    LaunchedEffect(api) {
        loading = true
        redditLinks.clear()

        currentUser = withContext(Dispatchers.IO) {
            try {
                api.getCurrentUser()
            } catch (exception: Exception) {
                null
            }
        }

        val postLoader = api.posts.getFrontPage()
            .transform {
                loading = false
                emit(it.also { println("Loaded page! first= ${it.firstOrNull()?.title}") })
                loadMorePosts.receive()
                println("Received the okay to load more posts.")
                loading = true
            }

        while (true) {
            postLoader.collect { page ->
                println("received page")
                val snapshot  = redditLinks.toMutableList()
                snapshot.addAll(page)
                redditLinks = snapshot
            }
        }

    }

    Scaffold(
        scaffoldState = state,
        drawerContent = { RedditDrawerContent(
            parentFrame = parentFrame,
            currentUser = currentUser,
            onNewApi = {
                api = it
                drawerState.close()
            }
        ) },
        topBar = {
            val tabList = tabs.toMutableList()
            MainTopBar(
                onRefresh = {},
                currentTab = currentTab,
                tabs = tabList,
                onTabSelected = { currentTab = it },
                onTabRemoved = {
                    println("Remove called.")
                    val currentTabIndex = tabList.indexOf(currentTab)
                    tabList.remove(it)
                    if (currentTab == it) {
                        val newTabToDisplay = tabList.getOrElse(currentTabIndex) { RedditTabItem.FrontPage }
                        currentTab = newTabToDisplay
                    }
                    tabs = tabList.toMutableSet()
                },
                onAccountSwitcherSelected = {
                    accountSwitcherOpen = !accountSwitcherOpen
                    drawerState.open()
                }
            )
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize()) {
            val tabListSize = animate(if (currentTab == RedditTabItem.FrontPage) 1f else 0.3f)
            PostList(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(tabListSize),
                loading = loading,
                links = redditLinks,
                onLinkClicked = {
                    val newTab = RedditTabItem.Post(it)
                    tabs.add(newTab)
                    tabs = tabs
                    currentTab = newTab
                },
                requestMoreLinks = { lastLink ->
                    loadMorePosts.offer(Unit)
                }
            )

            val width = 1 - tabListSize
            val mod = Modifier.fillMaxHeight().fillMaxWidth(1f)
            when (val tab = currentTab) {
                is RedditTabItem.Subreddit -> SubredditView(subredditName = tab.name, modifier = mod)
                is RedditTabItem.Post -> PostView(api = api, link = tab.post, modifier = mod)
            }
        }
    }
}

sealed class LoginStatus {
    object None: LoginStatus()
    object AwaitingAuthentication: LoginStatus()
    object ProcessingLogin: LoginStatus()
}

@Composable
fun RedditDrawerContent(
    parentFrame: JFrame,
    currentUser: Account?,
    onNewApi: (RedditAPI) -> Unit
) {
    var loggingIn: LoginStatus by remember { mutableStateOf(LoginStatus.None) }
    val strategy = InstalledApp(ClientId(BuildConfig.INSTALLED_CLIENT_ID), BuildConfig.INSTALLED_REDIRECT_URI)
    var runningServer: HttpServer? = remember { null }
    val scope = rememberCoroutineScope()

    if (loggingIn == LoginStatus.AwaitingAuthentication && runningServer == null) {
        Desktop.getDesktop().browse(URI.create(strategy.getAuthorizationUrl()))
        LaunchedEffect(loggingIn) {
            val server = HttpServer.create(InetSocketAddress("localhost", 8080), 0)
            server.createContext("/reddit/auth") { request ->
                parentFrame.requestFocus()
                server.stop(0)
                scope.launch {
                    loggingIn = LoginStatus.ProcessingLogin
                    when (val result = strategy.onAuthorizationComplete(request.requestURI.toString())) {
                        is AuthenticationResult.Success -> {
                            loggingIn = LoginStatus.None
                            onNewApi(RedditAPI(result.client))
                        }
                        else -> {
                            println(result)
                        }
                    }
                }
            }
            server.executor = Dispatchers.Default.asExecutor()
            server.start()
            runningServer = server
            println("Server started...")

        }
    } else if (loggingIn == LoginStatus.ProcessingLogin) {
        Text("Processing your login....")
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(modifier = Modifier.padding(8.dp), elevation = 8.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                    if (currentUser == null) {
                        Text("You are not logged in.")
                        Spacer(Modifier.height(4.dp))
                        Button(onClick = {
                            loggingIn = LoginStatus.AwaitingAuthentication
                        }) {
                            Text("Login")
                        }
                    } else {
                        Text("Logged in as ${currentUser.username}")
                        Button(onClick = {
                            onNewApi(RedditAPI(anonymousClient))
                        }) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTopBar(
    currentTab: RedditTabItem,
    tabs: List<RedditTabItem>,
    onAccountSwitcherSelected: () -> Unit,
    onRefresh: () -> Unit,
    onTabSelected: (RedditTabItem) -> Unit,
    onTabRemoved: (RedditTabItem) -> Unit
) {
    Column {
        TopAppBar(
            title = { TopAppBarTitle(currentTab) },
            actions = {
                Button(onClick = { nightMode.value = !nightMode.value }) {
                    Text(text = if (nightMode.value) "Light" else "Dark")
                }

                IconButton(onClick = onAccountSwitcherSelected) {
                    Icon(Icons.Default.AccountCircle, modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp))
                }

                IconButton(onClick = onRefresh) {
                    Image(
                        bitmap = imageResource("refresh.png"),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                    )
                }
            }
        )

        RedditTabList(
            selectedTab = currentTab,
            tabs = tabs,
            onTabSelected = onTabSelected,
            onTabRemoved = onTabRemoved
        )
    }
}

@Composable
private fun TopAppBarTitle(currentTab: RedditTabItem) {
    Column {
        Text(
            text = when (currentTab) {
                RedditTabItem.FrontPage -> "Front Page"
                is RedditTabItem.Subreddit -> "/r/${currentTab.name}"
                is RedditTabItem.Post -> currentTab.post.title
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (currentTab is RedditTabItem.Post) {
            ProvideTextStyle(MaterialTheme.typography.subtitle1) {
                Row {
                    Text("/u/${currentTab.post.author}")
                    Spacer(Modifier.width(8.dp))
                    Text("/r/${currentTab.post.subreddit}")
                }
            }
        }
    }
}

val nightMode = mutableStateOf(false)

fun main() {
    val frame = JFrame()
    frame.title = "Reddit API Example"
    frame.contentPane.add(ComposePanel().apply {
        setContent {
            MaterialTheme(colors = if (nightMode.value) darkColors() else lightColors()) {
                MainScreen(frame)
            }
        }
    })

    frame.isVisible = true
}