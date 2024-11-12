import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

enum class State {
    WALK,
    FIGHT
}
class Point {
    var x: Float = 0f
    var y: Float = 0f
    var state: State = State.WALK
    constructor(x: Float, y: Float, state: State) {
        this.x = x
        this.y = y
        this.state = state
    }
}

fun generateRandomPoint(screenSize: Pair<Float, Float>) = Point(Random.nextFloat() * screenSize.first, Random.nextFloat() * screenSize.second, State.WALK)

fun initPoints(count: Int, screenSize: Pair<Float, Float>): Set<Point> {
    val points = mutableSetOf<Point>()
    for (i in 0 until count) {
       var point = generateRandomPoint(screenSize)
        while (points.contains(point)) {
            point = generateRandomPoint(screenSize)
        }
        points.add(point)
    }
    return points
}

val pointCount: Int = 50_000
val width = 800.dp
val height = 800.dp

@Composable
@Preview
fun App() {
    val screenSize = Pair(width.value, height.value)
    var points by remember { mutableStateOf(initPoints(pointCount, screenSize)) }

    var R = 10000f
    var T = 100L

    LaunchedEffect(Unit) {
        while (true) {
            val newPoints = points.map { p ->
                val dx = Random.nextFloat() * 2 - 1
                val dy = Random.nextFloat() * 2 - 1
                val newX = (p.x + dx).coerceIn(0f, screenSize.first)
                val newY = (p.y + dy).coerceIn(0f, screenSize.second)
                Point(newX, newY, State.WALK)
            }.toSet()
            points = newPoints
            kotlinx.coroutines.delay(T)
        }
    }

    MaterialTheme {
        Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().background(Color.White),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Canvas(modifier = androidx.compose.ui.Modifier.weight(1f).fillMaxSize()) {
                points.forEach { point ->
                    drawCircle(
                        color = Color.Red,
                        center = Offset(point.x, point.y),
                        radius = 1f
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width = width, height = height)
    ) {
        App()
    }
}
