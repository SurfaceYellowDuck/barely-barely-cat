import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    val config = GetConfigOrDefault("const.json")
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width = (config.w * 0.8).dp, height = (config.h * 0.8).dp)
    ) {
        val app = App(config)
        app.run()
    }
}
