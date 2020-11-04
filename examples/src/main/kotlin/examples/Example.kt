package examples

import javafx.application.Application
import tornadofx.*

class ExampleApplication: App(HelloWorld::class)

class HelloWorld: View() {
    override val root = borderpane {
        left = listview<String> {
            items = ('a'..'z').map { it.toString() }.toObservable()
            prefWidth = 100.0
            prefHeight = 32.0

            cellFormat {
                text = it
            }
        }

        center = text {
            text = "Test Message"
        }
    }
}

fun main() = Application.launch(ExampleApplication::class.java)