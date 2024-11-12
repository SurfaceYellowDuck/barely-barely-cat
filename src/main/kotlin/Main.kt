import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class State {
    WALK,
    FIGHT,
    SLEEP,
    HISS
}

data class Cat(
    var x: Float = 0f,
    var y: Float = 0f,
    var state: State = State.WALK,
    var sleepTimer: Int = 0, // Время сна
    var sleepDuration: Int = Random.nextInt(5, 10) // Продолжительность сна
)

fun generateRandomPoint(screenSize: Pair<Float, Float>) =
    Cat(Random.nextFloat() * screenSize.first, Random.nextFloat() * screenSize.second, State.WALK)

fun initPoints(count: Int, screenSize: Pair<Float, Float>): Set<Cat> {
    val points = mutableSetOf<Cat>()
    for (i in 0 until count) {
        var point = generateRandomPoint(screenSize)
        while (points.contains(point)) {
            point = generateRandomPoint(screenSize)
        }
        points.add(point)
    }
    return points
}

val pointCount: Int = 10000
val width = 800.dp
val height = 800.dp

@Composable
@Preview
fun App() {
    val screenSize = Pair(width.value, height.value)
    var points by remember { mutableStateOf(initPoints(pointCount, screenSize)) }

    val r0 = 10f // Радиус драки
    val R0 = 50f // Радиус шипения
    val sleepProbability = 0.01f // Вероятность уснуть

    LaunchedEffect(Unit) {
        while (true) {
            val newPoints = points.map { p ->
                val neighbors = points.filter { other ->
                    other != p && distance(p, other) <= R0
                }

                val catState = when {
                    neighbors.any { distance(p, it) <= r0 } -> State.FIGHT // В драке коты продолжают двигаться
                    neighbors.any { distance(p, it) <= R0 && Random.nextFloat() < (1 / distance(p, it).pow(2)) } -> State.HISS
                    p.sleepTimer > 0 -> State.SLEEP
                    Random.nextFloat() < sleepProbability -> {
                        p.sleepTimer = p.sleepDuration
                        State.SLEEP
                    }
          else -> State.WALK
        }

        val (dx, dy) = when (catState) {
          State.WALK, State.FIGHT -> Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1 // Движение в драке
          State.SLEEP -> 0f to 0f
          else -> 0f to 0f
        }

        val newX = (p.x + dx).coerceIn(0f, screenSize.first)
        val newY = (p.y + dy).coerceIn(0f, screenSize.second)

        // Обновляем таймер сна
        if (p.sleepTimer > 0) p.sleepTimer--

        Cat(newX, newY, catState, p.sleepTimer, p.sleepDuration)
      }.toSet()
      points = newPoints
      kotlinx.coroutines.delay(10L) // Задержка обновления
    }
  }

  MaterialTheme {
    Column(
      modifier = androidx.compose.ui.Modifier.fillMaxSize().background(Color.White),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Canvas(modifier = androidx.compose.ui.Modifier.weight(1f).fillMaxSize()) {
        points.forEach { point ->
          val color = when (point.state) {
            State.WALK -> Color.Green
            State.FIGHT -> Color.Red // Коты красные во время драки
            State.SLEEP -> Color.Blue
            State.HISS -> Color.Yellow
          }
          drawCircle(
            color = color,
            center = Offset(point.x, point.y),
            radius = 5f // Размер кота
          )
        }
      }
    }
  }
}

fun distance(cat1: Cat, cat2: Cat): Float {
  return sqrt((cat1.x - cat2.x).pow(2) + (cat1.y - cat2.y).pow(2))
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
