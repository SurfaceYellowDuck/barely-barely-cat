import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow
import kotlinx.coroutines.delay

**
 * Main application class that manages the simulation of cats.
 *
 * @property consts The configuration constants used in the application.
 */
class App(private val consts: Consts) {
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
                nearestCat?.let { distance(cat, it) }!! <= r0 -> FightingState()
                distance(cat, nearestCat) <= R0 && Random.nextFloat() <
                        (1 / distance(cat, nearestCat).pow(2)) -> HissingState()

                cat.sleepTimer > 0 -> SleepingState()
                Random.nextFloat() < consts.sleepProbability -> {
                    cat.sleepTimer = cat.sleepDuration
                    SleepingState()
                }

                else -> WalkingState()
            }

            val (dx, dy) = catState.nextMove()

            val newX = (cat.x + dx).coerceIn(0F, screenSize.first)
            val newY = (cat.y + dy).coerceIn(0F, screenSize.second)

            if (cat.sleepTimer > 0) cat.sleepTimer--

            Cat(newX, newY, catState, cat.sleepTimer, cat.sleepDuration)
        }.toList()
        return newCats
    }

    /**
     * Composable function to draw a text field.
     *
     * @param text The current text value of the text field.
     * @param onValueChange The callback to be invoked when the text value changes.
     * @param label The label to be displayed for the text field.
     */
    @Composable
    fun drawTextField(text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, label: String) {
        TextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.width(100.dp),
            label = { Text(label) }
        )
    }

    /**
     * Composable function to draw a button.
     *
     * @param onClick The callback to be invoked when the button is clicked.
     * @param buttonText The text to be displayed on the button.
     */
    @Composable
    fun drawButton(onClick: () -> Unit, buttonText: String) {
        Button(
            onClick = onClick,
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = Color.Green,
                contentColor = Color.White
            )
        ) {
            Text(buttonText)
        }
    }

    /**
     * Composable function to run the main application.
     */
    @Composable
    fun run() {
        var refreshTime by remember { mutableStateOf(TextFieldValue(consts.refTime.toString())) }
        var selectedMethod by remember { mutableStateOf(DistanceMetric.EUCLIDEAN) }
        val cats = remember { mutableStateOf(emptyList<Cat>()) }

        LaunchedEffect(Unit) {
            while (true) {
                if (cats.value.isNotEmpty()) {
                    val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod) }
                    val catsKDTree = KDTree(cats.value, dista)
                    cats.value = updateCats(cats.value, catsKDTree, dista, (consts.w to consts.h))
                    delay(refreshTime.text.toLongOrNull() ?: 100)
                } else {
                    delay(100)
                }
            }
        }

        drawAll(
            cats = cats,
            selectedMethod = { newMethod -> selectedMethod = newMethod },
            refreshTime = { newTime -> refreshTime = newTime },
            currentMethod = selectedMethod,
            currentRefreshTime = refreshTime
        )
    }

    /**
     * Composable function to draw all UI components.
     *
     * @param cats The list of cats to be displayed.
     * @param selectedMethod The callback to be invoked when the distance metric method changes.
     * @param refreshTime The callback to be invoked when the refresh time changes.
     * @param currentMethod The current distance metric method.
     * @param currentRefreshTime The current refresh time.
     */
    @Composable
    fun drawAll(
        cats: MutableState<List<Cat>>,
        selectedMethod: (DistanceMetric) -> Unit,
        refreshTime: (TextFieldValue) -> Unit,
        currentMethod: DistanceMetric,
        currentRefreshTime: TextFieldValue
    ) {
        var pointCount by remember { mutableStateOf(TextFieldValue(consts.pc.toString())) }
        val width by remember { mutableStateOf(TextFieldValue(consts.w.toString())) }
        val height by remember { mutableStateOf(TextFieldValue(consts.h.toString())) }
        var expanded by remember { mutableStateOf(false) }
        val screenSize = Pair(width.text.toFloat(), height.text.toFloat())

        MaterialTheme {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.White),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    drawTextField(pointCount, { pointCount = it }, "Point Count")
                    drawTextField(currentRefreshTime, { refreshTime(it) }, "Refresh Time")

                    Box {
                        drawButton({ expanded = true }, currentMethod.name)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DistanceMetric.entries.forEach { method ->
                                DropdownMenuItem(onClick = {
                                    selectedMethod(method)
                                    expanded = false
                                }) {
                                    Text(method.name)
                                }
                            }
                        }
                    }

                    drawButton({
                        cats.value = initCats(pointCount.text.toInt(), screenSize)
                    }, "Start")
                }

                Canvas(modifier = Modifier.weight(1f).fillMaxSize()) {
                    val pointRadius = (50.0 / sqrt(cats.value.size.toFloat())).coerceAtLeast(1.0)
                    cats.value.forEach { point ->
                        drawCircle(
                            color = point.state.color,
                            center = Offset(point.x.dp.toPx(), point.y.dp.toPx()),
                            radius = pointRadius.toFloat()
                        )
                    }
                }
            }
        }
    }
}
