package examples.screens

import androidx.compose.animation.animate
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tgayle.reddit.models.Link
import kotlin.math.min

sealed class RedditTabItem(val tabHint: String) {
    object FrontPage: RedditTabItem("Front Page")
    data class Subreddit(val name: String): RedditTabItem("/r/$name")
    data class Post(val post: Link): RedditTabItem(post.title)
}

@Composable
fun RedditTabList(
    modifier: Modifier = Modifier,
    selectedTab: RedditTabItem,
    tabs: List<RedditTabItem>,
    onTabSelected: (tab: RedditTabItem) -> Unit,
    onTabRemoved: (RedditTabItem) -> Unit
) {
    ScrollableTabRow(modifier = modifier, selectedTabIndex = tabs.indexOf(selectedTab), ) {
        tabs.map { item ->
            Tab(
                selected = selectedTab == item,
                onClick = { onTabSelected(item) },
                modifier = Modifier.padding(end = 4.dp).height(32.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.tabHint.substring(0, min(40, item.tabHint.length)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item !is RedditTabItem.FrontPage) {
                        IconButton(
                            onClick = { onTabRemoved(item) },
                            modifier = Modifier.padding(start = 2.dp).size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}