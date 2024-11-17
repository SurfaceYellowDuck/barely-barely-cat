import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


const val width = 800
const val height = 800

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width=width.dp, height = height.dp)
    ) {
        app()
    }
}
