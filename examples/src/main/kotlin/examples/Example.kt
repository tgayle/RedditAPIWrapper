package examples

import com.tgayle.DesignPatternsRedditAPI.BuildConfig
import com.tgayle.reddit.RedditAPI
import com.tgayle.reddit.RedditClient
import com.tgayle.reddit.auth.Anonymous
import com.tgayle.reddit.models.ClientId
import javafx.application.Application
import kotlinx.coroutines.*
import tornadofx.*

class ExampleApplication: App(HelloWorld::class)

class HelloWorld: View() {
    val api = RedditAPI(
        RedditClient(
            ClientId(BuildConfig.SCRIPT_CLIENT_ID),
            Anonymous(
                BuildConfig.SCRIPT_CLIENT_SECRET,
            )
        )
    )

    val coroScope = CoroutineScope(Dispatchers.Main + Job())

    var loading = booleanProperty(false)

    override val root = borderpane {
        val listItems = observableListOf<String>()

        left = listview<String> {
            items = listItems
            prefWidth = 200.0
            prefHeight = 32.0

            cellFormat {
                text = it
            }
        }

        center = vbox {
            button("Click to load") {
                onLeftClick {
                    if (loading.value) return@onLeftClick
                    loading.value = true

                    coroScope.launch {
                        val items = withContext(Dispatchers.IO) {
                            api.posts.getFrontPage().map { it.title }
                        }

                        listItems.clear()
                        listItems.addAll(items)
                        loading.value = false
                    }
                }
            }
            text(stringBinding(loading) { if (value == true) "Loading" else "Not Loading" })
        }
    }
}

fun main() = Application.launch(ExampleApplication::class.java)