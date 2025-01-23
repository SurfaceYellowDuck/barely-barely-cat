import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * The main entry point of the application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width = (width * 0.8).dp, height = (height * 0.8).dp)
    ) {
        val app = App(config)
        app.run()
    }
}
