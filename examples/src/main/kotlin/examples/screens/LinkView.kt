package examples.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.models.EditedState
import com.tgayle.reddit.models.Link
import com.tgayle.reddit.models.Reply
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skija.Image
import java.util.*

val client = HttpClient()

@Composable
fun PostView(
    modifier: Modifier = Modifier,
    api: RedditAPI,
    link: Link,
) {
    var comments: List<Reply> by remember(link) { mutableStateOf(emptyList()) }

    LaunchedEffect(api, link) {
        println("Loading comments")
        comments = api.posts.getLink(link.subreddit, link.id, commentDepth = 4).comments.toList()
        println("Comments loaded")
    }

    Column(modifier = modifier) {
        LinkHeader(link = link)
        Divider()

        var currentComment: Reply? = remember { comments.firstOrNull() }
        var depth = remember { 0 }

        LazyColumnFor(comments) {
            when (it) {
                is Reply.Comment -> CommentItem(it)
                is Reply.MoreComments -> LoadMoreCommentButton(it)
            }
        }
    }
}

@Composable
fun LinkHeader(link: Link) {
    var loading by remember(link) { mutableStateOf(false) }
    var image: ImageBitmap? by remember(link) { mutableStateOf(null) }

    Column(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
        Text(text = link.title, style = MaterialTheme.typography.h4)
        Text(text = "/r/${link.subreddit}", style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.height(16.dp))

        LaunchedEffect(link) {
            loading = true
            println("Thumbnail is ${link.thumbnail}")

            if (link.thumbnail == "self" || link.thumbnail == "default") {
                loading = false
                image = null
                return@LaunchedEffect
            }

            image = try {
                val bytes = withContext(Dispatchers.IO) {
                    client.get<ByteArray>(link.thumbnail)
                }
                Image.makeFromEncoded(bytes).asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                loading = false
            }
        }

        image.let { image ->
            when {
                loading -> Text("Loading")
                image == null -> {}
                else -> {
                    Image(image, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun CommentItem(comment: Reply.Comment, depth: Int = 0) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(start = 8.dp * depth)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconToggleButton(
                    checked = expanded,
                    onCheckedChange = { expanded = it },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add
                    )

                }

                val textStyle = AmbientTextStyle.current

                Text(modifier = Modifier, text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.DarkGray)) {
                        append("/u/${comment.author}  ")
                    }

                    withStyle(SpanStyle(background = Color.Gray)) {
                        append(comment.authorFlairText ?: "")
                    }

                    append("  ")

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(comment.upvotes.toString())
                    }

                    append(" ")

                    withStyle(SpanStyle(color = textStyle.color.copy(alpha = 0.66f))) {
                        append(Date(comment.createdUtc * 1000).toLocaleString())
                        if (comment.edited is EditedState.Edited) {
                            append("*")
                        }
                    }
                })

            }

            AnimatedVisibility(expanded) {
                Text(comment.body)

                Column {
                    comment.replies?.forEach {
                        when (it) {
                            is Reply.Comment -> CommentItem(it, depth + 1)
                            is Reply.MoreComments -> LoadMoreCommentButton(it)
                        }
                    }
                }

            }

        }
        Divider()
    }
}

@Composable
fun LoadMoreCommentButton(moreComments: Reply.MoreComments) {
    Button(onClick = {}) {
        Text("${moreComments.count} more replies")
    }
}