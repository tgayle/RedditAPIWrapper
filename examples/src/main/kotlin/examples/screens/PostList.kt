package examples.screens

import androidx.compose.animation.animate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.tgayle.reddit.models.Link

@Composable
fun PostList(
    modifier: Modifier = Modifier,
    loading: Boolean,
    links: List<Link>,
    requestMoreLinks: (lastLink: Link) -> Unit,
    onLinkClicked: (Link) -> Unit
) {
    Box(modifier = modifier) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).alpha(animate(if (loading) 1f else 0f)))

        LazyColumnForIndexed(links) { index, link ->
            LinkItem(
                link=link,
                onLinkClicked = onLinkClicked,
            )

            Divider()

            if (index == links.size - 1) {
                TextButton(onClick = {requestMoreLinks(link)}) {
                    Text("Load more...")
                }

                Divider()
            }
        }
    }
}

@Composable
fun LinkItem(link: Link, onLinkClicked: (Link) -> Unit) {
    ListItem(
        text = { Text(link.title) },
        secondaryText = {
            Row {
                Text("/u/${link.author}")
                Spacer(modifier = Modifier.width(4.dp))
                Text("/r/${link.subreddit}")
            }
        },
        modifier = Modifier.clickable(onClick = { onLinkClicked(link) })
    )
}