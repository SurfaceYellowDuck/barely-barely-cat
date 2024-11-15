import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.abs

enum class State {
    WALK,
    FIGHT,
    SLEEP,
    HISS
}

data class Cat(
    var x: Float = 0F,
    var y: Float = 0F,
    var state: State = State.WALK,
    var sleepTimer: Int = 0,
    var sleepDuration: Int = Random.nextInt(5, 10)
) {
    fun distance(other: Cat): Float {
        return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
    }
}
fun getRandomFloatInRange(a: Float, b: Float): Float {
    return Random.nextFloat() * (b - a) + a
}
fun generateRandomPoint(screenSize: Pair<Float, Float>) =
    Cat(getRandomFloatInRange(0F, screenSize.first), getRandomFloatInRange(0F, screenSize.second), State.WALK)

fun initPoints(count: Int, screenSize: Pair<Float, Float>): List<Cat> {
    val points = mutableListOf<Cat>()
    for (i in 0 until count) {
        var point = generateRandomPoint(screenSize)
        while (points.contains(point)) {
            point = generateRandomPoint(screenSize)
        }
        points.add(point)
    }
    return points
}

@Composable
@Preview
fun App() {
    var pointCount by remember { mutableStateOf(TextFieldValue("500")) }
    var width by remember { mutableStateOf(TextFieldValue("800")) }
    var height by remember { mutableStateOf(TextFieldValue("800")) }

    val methods = listOf("euclidean", "manhattan", "chebyshev")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var expanded by remember { mutableStateOf(false) }

    var refreshTime by remember { mutableStateOf(TextFieldValue("500")) }

    val screenSize = Pair(width.text.toFloat(), height.text.toFloat())
    var cats by remember { mutableStateOf(initPoints(pointCount.text.toInt(), screenSize)) }

    val r0 = 2f
    val R0 = 5f
    val sleepProbability = 0.01f

    LaunchedEffect(Unit) {
        while (true) {
            val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod)}
            val catsKDTree = KDTree(cats, dista)
            val newCats = cats.map { cat ->
                val nearestCat = catsKDTree.nearestNeighbor(cat)
                val catState = when {
                    nearestCat?.let { cat.distance(nearestCat) }!! <= r0 -> State.FIGHT
                    cat.distance(nearestCat) <= R0 && Random.nextFloat() <
                            (1 / cat.distance(nearestCat).pow(2)) -> State.HISS

                    cat.sleepTimer > 0 -> State.SLEEP
                    Random.nextFloat() < sleepProbability -> {
                        cat.sleepTimer = cat.sleepDuration
                        State.SLEEP
                    }

                    else -> State.WALK
                }

                val (dx, dy) = when (catState) {
                    State.WALK, State.FIGHT, State.HISS -> Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
                    else -> 0f to 0f
                }

                val newX = (cat.x + dx).coerceIn(0F, screenSize.first)
                val newY = (cat.y + dy).coerceIn(0F, screenSize.second)

                if (cat.sleepTimer > 0) cat.sleepTimer--

                Cat(newX, newY, catState, cat.sleepTimer, cat.sleepDuration)
            }.toList()
            cats = newCats
            kotlinx.coroutines.delay(refreshTime.text.toLong())
        }
    }

    MaterialTheme {
        Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize().background(Color.White),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = androidx.compose.ui.Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = pointCount,
                    onValueChange = { pointCount = it },
                    modifier = androidx.compose.ui.Modifier.width(100.dp),
                    label = { Text("Point Count") }
                )
                TextField(
                    value = refreshTime,
                    onValueChange = { refreshTime = it },
                    modifier = androidx.compose.ui.Modifier.width(100.dp),
                    label = { Text("Refresh Time") }
                )
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedMethod)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(onClick = {
                                selectedMethod = method
                                expanded = false
                            }) {
                                Text(method)
                            }
                        }
                    }
                }
                Button(onClick = {
                    cats = initPoints(pointCount.text.toInt(), screenSize)
                }) {
                    Text("Update")
                }
            }
            Canvas(modifier = androidx.compose.ui.Modifier.weight(1f).fillMaxSize()) {
                val pointRadius = (50.0 / sqrt(cats.size.toFloat())).coerceAtLeast(1.0)

                cats.forEach { point ->
                    val color = when (point.state) {
                        State.WALK -> Color.Green
                        State.FIGHT -> Color.Red
                        State.SLEEP -> Color.Blue
                        State.HISS -> Color.Yellow
                    }
                    drawCircle(
                        color = color,
                        center = Offset(point.x.dp.toPx(), point.y.dp.toPx()),
                        radius = pointRadius.toFloat()
                    )
                }
            }
        }
    }
}

fun distance(cat1: Cat, cat2: Cat, metric: String): Float {
  return when (metric) {
    "euclidean" -> sqrt((cat1.x - cat2.x).pow(2) + (cat1.y - cat2.y).pow(2))
    "manhattan" -> abs(cat1.x - cat2.x) + abs(cat1.y - cat2.y)
    "chebyshev" -> maxOf(abs(cat1.x - cat2.x), abs(cat1.y - cat2.y))
    else -> throw IllegalArgumentException("Invalid distance metric: $metric")
  }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        resizable = false,
        title = "Random Point Drawer",
        state = rememberWindowState(width=800.dp, height = 800.dp)
    ) {
        App()
    }
}
