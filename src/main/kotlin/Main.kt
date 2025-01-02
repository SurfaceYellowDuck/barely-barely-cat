import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

val width = consts.w.dp
val height = consts.h.dp
val screenSize = Pair(width, height)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width = (screenSize.first), height = (screenSize.second))
    ) {
        var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
        var obstacles by remember { mutableStateOf(initObstacles(5, screenSize)) }

        var cats by remember { mutableStateOf(initCats(pointCount.text.toInt(), screenSize, obstacles)) }
        app(cats)
    }
}
