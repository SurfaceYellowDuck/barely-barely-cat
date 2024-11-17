import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow

const val r0 = 2f
const val R0 = 5f
const val sleepProbability = 0.01f
const val w = 800f
const val h = 800f
const val pc = 500
const val refTime = 500


fun updateCats(
    cats: List<Cat>,
    catsKDTree: KDTree,
    distance: (Cat, Cat) -> Float,
    screenSize: Pair<Float, Float>
): List<Cat> {
    val newCats = cats.map { cat ->
        val nearestCat = catsKDTree.nearestNeighbor(cat)
        val catState = when {
            nearestCat?.let { distance(cat, it) }!! <= r0 -> State.FIGHT
            distance(cat, nearestCat) <= R0 && Random.nextFloat() <
                    (1 / distance(cat, nearestCat).pow(2)) -> State.HISS

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
    return newCats
}

@Composable
@Preview
fun app() {
    var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
    val width by remember { mutableStateOf(TextFieldValue(w.toString())) }
    val height by remember { mutableStateOf(TextFieldValue(h.toString())) }

    val methods = listOf("euclidean", "manhattan", "chebyshev")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var expanded by remember { mutableStateOf(false) }

    var refreshTime by remember { mutableStateOf(TextFieldValue(refTime.toString())) }

    val screenSize = Pair(width.text.toFloat(), height.text.toFloat())
    var cats by remember { mutableStateOf(initCats(pointCount.text.toInt(), screenSize)) }


    LaunchedEffect(Unit) {
        while (true) {
            val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod) }
            val catsKDTree = KDTree(cats, dista)
            val newCats = updateCats(cats, catsKDTree, dista, screenSize)
            cats = newCats
            kotlinx.coroutines.delay(refreshTime.text.toLongOrNull() ?: 100)
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
                    cats = initCats(pointCount.text.toInt(), screenSize)
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