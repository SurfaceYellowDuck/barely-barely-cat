import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.gson.Gson
import java.io.File

val filePath = "const.json" // Replace with your JSON file path
val file = File(filePath)

val jsonString = file.readText()
val consts = Gson().fromJson(jsonString, Consts::class.java)
val width = consts.w
val height = consts.h

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width = (width * 0.8).dp, height = (height * 0.8).dp)
    ) {
        app()
    }
}
