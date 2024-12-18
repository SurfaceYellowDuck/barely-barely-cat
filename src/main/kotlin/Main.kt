import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

val width = consts.w
val height = consts.h

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "Random Point Drawer",
        state = rememberWindowState(width = (width * 0.8).dp, height = (height * 0.8).dp)
//        resizable = false,
//        title = "Random Point Drawer",
//        state = rememberWindowState(width = width.dp, height = height.dp)

    ) {
        val width by remember { mutableStateOf(TextFieldValue(w.toString())) }
        val height by remember { mutableStateOf(TextFieldValue(h.toString())) }
        var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
        val screenSize = Pair(width.text.toFloat(), height.text.toFloat())
        var obstacles by remember { mutableStateOf(initObstacles(5, screenSize)) }

        var cats by remember { mutableStateOf(initCats(pointCount.text.toInt(), screenSize, obstacles)) }
        app(cats)
    }
}
