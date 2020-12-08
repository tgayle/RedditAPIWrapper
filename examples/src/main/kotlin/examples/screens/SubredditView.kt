package examples.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SubredditView(modifier: Modifier = Modifier, subredditName: String) {
    Text(modifier = modifier, text = subredditName)
}