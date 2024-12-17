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
import java.io.File
import com.google.gson.Gson

data class Consts(val sleepProbability: Float, val w: Float, val h: Float, val pc: Int,
    val refTime: Int, val r0_small: Float, val r0_big: Float,
                 val R01_big: Float, val R01_small: Float)

/**
 * Constant value representing a distance threshold used to determine the proximity between cats.
 *
 * If the distance between two cats is less than or equal to `r0`, certain state changes like `FIGHT` or `HISS` can be triggered.
 */


val filePath = "const.json" // Replace with your JSON file path
val file = File(filePath)

val jsonString = file.readText()
val consts = Gson().fromJson(jsonString, Consts::class.java)

val sleepProbability = consts.sleepProbability
val w = consts.w
val h = consts.h
val pc = consts.pc
val refTime = consts.refTime

/**
 * Updates the list of cats based on their current state and position.
 *
 * @param cats The list of cats to be updated.
 * @param catsKDTree A KDTree that helps to find the nearest neighbor of a cat.
 * @param distance A function that calculates the distance between two cats.
 * @param screenSize A pair representing the width and height of the screen.
 * @return A new list of cats with updated positions and states.
 */
fun updateCats(
    cats: List<Cat>,
    catsKDTree: KDTree,
    distance: (Cat, Cat) -> Float,
    screenSize: Pair<Float, Float>
): List<Cat> {
    val r0 = when {
        cats.size.toFloat() < 100 -> consts.r0_big
        cats.size.toFloat() < 10000 -> consts.r0_small
        else -> 1f
    }

    val R0 = when {
        cats.size.toFloat() < 100 -> consts.R01_big
        cats.size.toFloat() < 10000 -> consts.R01_small
        else -> 5f
    }

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

/**
 * Composable function representing the main app layout and behavior.
 *
 * This function contains various UI elements and logic to manage an interactive
 * simulation of cats with different states, running in a continuous loop with a
 * specified refresh rate. The UI allows customization of point count, refresh time, and
 * distance metric calculation method.
 *
 * UI Components:
 * - TextFields for point count and refresh time input.
 * - Dropdown menu to select the distance metric method.
 * - Button to update the cat positions.
 * - Canvas to render the cat positions and states visually.
 *
 * The internal state is managed using Jetpack Compose's state management
 * functions like `remember` and `mutableStateOf`. The cats' positions
 * are updated periodically based on a KDTree structure for efficient distance
 * calculations.
 *
 * The simulation loop runs continuously in a coroutine, updating the cat
 * positions and UI at the specified refresh interval.
 *
 * Distance Metrics:
 * - "euclidean" calculates straight-line distance.
 * - "manhattan" calculates grid-based distance.
 * - "chebyshev" calculates maximum of horizontal and vertical distances.
 *
 * The function utilizes various Compose UI components and functions like
 * `MaterialTheme`, `Column`, `Row`, `TextField`, `Button`, `DropdownMenu`,
 * `Canvas`, and drawing functions from the `androidx.compose.ui.graphics` package.
 */
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
    var cats by remember { mutableStateOf(emptyList<Cat>()) }

    var isRunning by remember { mutableStateOf(false) }


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
                Button(
                    onClick = {
                        cats = initCats(pointCount.text.toInt(), screenSize)
                        isRunning = true
                    },
                    colors = androidx.compose.material.ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Start")
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