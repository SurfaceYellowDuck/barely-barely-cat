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
import kotlinx.coroutines.delay

/**
 * Main application class that manages the simulation of cats.
 *
 * @property config The configuration constants used in the application.
 */
class App(private val config: Config) {

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
        var refreshTime by remember { mutableStateOf(TextFieldValue(config.refTime.toString())) }
        var selectedMethod by remember { mutableStateOf(DistanceMetric.EUCLIDEAN) }
        val cats = remember { mutableStateOf(emptyList<Cat>()) }

        LaunchedEffect(Unit) {
            while (true) {
                if (cats.value.isNotEmpty()) {
                    val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod) }
                    val catsKDTree = KDTree(cats.value, dista)
                    cats.value = updateCats(cats.value, catsKDTree, dista, config)
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
        var pointCount by remember { mutableStateOf(TextFieldValue(config.pc.toString())) }
        val width by remember { mutableStateOf(TextFieldValue(config.w.toString())) }
        val height by remember { mutableStateOf(TextFieldValue(config.h.toString())) }
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
