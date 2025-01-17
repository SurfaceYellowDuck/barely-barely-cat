import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Slider
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.*
import androidx.compose.material.TextField
import androidx.compose.ui.unit.Dp
import com.google.gson.Gson
import java.io.File
import kotlin.random.Random

/**
 * Data class representing constants used in the application.
 *
 * @property sleepProbability Probability of a cat going to sleep.
 * @property w Width of the screen.
 * @property h Height of the screen.
 * @property pc Point count.
 * @property refTime Refresh time.
 * @property r0_small Small radius for interaction.
 * @property r0_big Big radius for interaction.
 * @property R01_big Big radius for detection.
 * @property R01_small Small radius for detection.
 */
data class Consts(
    val sleepProbability: Float,
    val w: Float,
    val h: Float,
    val pc: Int,
    val refTime: Int,
    val r0_small: Float,
    val r0_big: Float,
    val R01_big: Float,
    val R01_small: Float
)

val filePath = "const.json" // Replace with your JSON file path
val file = File(filePath)

val jsonString = file.readText()
val consts = Gson().fromJson(jsonString, Consts::class.java)

val sleepProbability = consts.sleepProbability
val w = consts.w.dp.value
val h = consts.h.dp.value
val pc = consts.pc
val refTime = consts.refTime

/**
 * Data class representing the view state of a cat.
 *
 * @property point The cat being viewed.
 * @property rotation The rotation angle in degrees.
 * @property fieldOfView The field of view angle.
 */
data class ViewState(
    val point: Cat, val rotation: Float, // Угол поворота в градусах
    val fieldOfView: Float // Угол обзора
)

/**
 * Extension function to convert radians to degrees.
 *
 * @return The angle in degrees.
 */
fun Float.toDegrees(): Float = Math.toDegrees(this.toDouble()).toFloat()

/**
 * Updates the state of the cats based on their interactions and environment.
 *
 * @param cats List of cats to update.
 * @param catsKDTree KDTree for nearest neighbor search.
 * @param distance Function to calculate the distance between two cats.
 * @param screenSize The size of the screen.
 * @param obstacles List of obstacles in the environment.
 * @return The updated list of cats.
 */
fun updateCats(
    cats: List<Cat>,
    catsKDTree: KDTree,
    distance: (Cat, Cat) -> Float,
    screenSize: Pair<Dp, Dp>,
    obstacles: List<Obstacle>
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
            distance(cat, nearestCat) <= R0 && Random.nextFloat() < (1 / distance(cat, nearestCat).pow(2)) -> State.HISS

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

        var newX = (cat.x.value + dx).coerceIn(0F, screenSize.first.value)
        var newY = (cat.y.value + dy).coerceIn(0F, screenSize.second.value)

        if (cat.sleepTimer > 0) cat.sleepTimer--

        if (obstacles.any { it.contains(Cat(newX.dp, newY.dp)) }) {
            newX = cat.x.value
            newY = cat.y.value
        }

        Cat(newX.dp, newY.dp, catState, cat.sleepTimer, cat.sleepDuration)
    }.toList()
    return newCats
}

/**
 * Composable function to render the application UI.
 *
 * @param Cats List of cats to display.
 */
@Composable
fun app(Cats: List<Cat>) {
    var viewState by remember {
        mutableStateOf<ViewState?>(null)
    }

    var isDragging by remember { mutableStateOf(false) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }

    var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
    val width by remember { mutableStateOf(TextFieldValue(w.dp.value.toString())) }
    val height by remember { mutableStateOf(TextFieldValue(h.dp.value.toString())) }

    val methods = listOf("euclidean", "manhattan", "chebyshev")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var expanded by remember { mutableStateOf(false) }

    var refreshTime by remember { mutableStateOf(TextFieldValue(refTime.toString())) }

    val screenSize = Pair(width.text.toFloat().dp, height.text.toFloat().dp)
    var cats by remember { mutableStateOf(emptyList<Cat>()) }
    var catss by remember { mutableStateOf(emptyList<Cat>()) }
    var obstacles by remember { mutableStateOf(initObstacles(5, screenSize)) }

    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod) }
            var catsKDTree = KDTree(cats, dista)
            if (cats.size == 1 || cats.size % 1 != 0) {
                catsKDTree = KDTree(catss, dista)
            } else {
                catsKDTree = KDTree(cats, dista)
            }



            val newCats = updateCats(cats, catsKDTree, dista, screenSize, obstacles)
            cats = newCats
            kotlinx.coroutines.delay(refreshTime.text.toLongOrNull() ?: 100)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            run {
                Button(
                    onClick = {
                        cats = initCats(pointCount.text.toInt(), screenSize, obstacles)
                        isRunning = true
                    }, colors = androidx.compose.material.ButtonDefaults.buttonColors(
                        backgroundColor = Color.Green, contentColor = Color.White
                    )
                ) {
                    Text("Start")
                }
                TextField(
                    value = pointCount,
                    onValueChange = { newValue ->
                    val regex = Regex("^[1-9][0-9]*$")
                    if (regex.matches(newValue.text) && newValue.text != "1") {
                        pointCount = newValue
                    } else
                        pointCount = TextFieldValue("0")
                    },
                    modifier = androidx.compose.ui.Modifier.width(100.dp),
                    label = { Text("Point Count") }
                )
                TextField(
                    value = refreshTime,
                    onValueChange = { newValue ->
                        val parsedValue = newValue.text.toLongOrNull()

                        if (parsedValue == null || parsedValue <= 0) {
                            refreshTime = TextFieldValue("1000")
                        } else {
                            refreshTime = newValue
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    label = { Text("Refresh Time") }
                )
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(selectedMethod)
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
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
                Column {
                    Button(
                        onClick = {
                            obstacles = obstacles + generateRandomObstacle(screenSize, obstacles)
                            cats =
                                if (cats.isEmpty()) cats else initCats(pointCount.text.toInt(), screenSize, obstacles)
                        }, colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color.Blue, contentColor = Color.White
                        )
                    ) {
                        Text("Add Obstacle")
                    }
                    Button(
                        onClick = {
                            if (obstacles.isNotEmpty()) {
                                obstacles = obstacles.dropLast(1)
                                cats = if (cats.isEmpty()) cats else initCats(
                                    pointCount.text.toInt(), screenSize, obstacles
                                )
                            }
                        }, colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red, contentColor = Color.White
                        ), modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Remove Obstacle")
                    }
                }
            }
        }

        Canvas(modifier = Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
            detectTapGestures { offset ->
                if (!isDragging) {
                    val tappedPoint = cats.find { point ->
                        val distance = sqrt(
                            ((point.x.toPx() - offset.x.toDp().toPx())).pow(2) + ((point.y.toPx() - offset.y.toDp().toPx())).pow(2)
                        )
                        distance < 20f
                    }

                    if (tappedPoint != null) {
                        viewState = if (viewState?.point == tappedPoint) {
                            null
                        } else {
                            ViewState(tappedPoint, 0f, 30f)
                        }
                    }
                }
            }
        }.pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                isDragging = true
                lastDragPosition = offset
            }, onDragEnd = {
                isDragging = false
                lastDragPosition = null
            }, onDrag = { change, _ ->
                if (viewState != null && lastDragPosition != null) {
                    val currentPosition = change.position

                    // Calculate the rotation angle based on mouse movement
                    val selectedPoint = viewState!!.point
                    val center = Offset(selectedPoint.x.toPx(), selectedPoint.y.toPx())

                    val lastAngle = atan2(
                        lastDragPosition!!.y.toDp().toPx() - center.y.toDp().toPx(), lastDragPosition!!.x.toDp().toPx() - center.x.toDp().toPx()
                    )
                    val currentAngle = atan2(
                        currentPosition.y.toDp().toPx() - center.y.toDp().toPx(), currentPosition.x.toDp().toPx() - center.x.toDp().toPx()
                    )

                    val deltaAngle = (currentAngle.toDp().toPx() - lastAngle.toDp().toPx()).toDegrees()

                    viewState = viewState!!.copy(
                        rotation = (viewState!!.rotation.toDp().toPx() + deltaAngle.toDp().toPx())
                    )

                    lastDragPosition = currentPosition
                }
            })
        }) {
            val pointRadius = (50.0 / sqrt(cats.size.toFloat())).coerceAtLeast(1.0)

            obstacles.forEach { obstacle ->
                val alpha = if (viewState != null) {
                    calculateObstacleAlpha(
                        obstacle, viewState!!.point, viewState!!.fieldOfView, viewState!!.rotation
                    ).toDp().toPx()
                } else 1f.toDp().toPx()

                if (alpha > 0) {
                    drawRect(
                        color = Color.Gray.copy(alpha = alpha),
                        topLeft = Offset(obstacle.x.toPx(), obstacle.y.toPx()),
                        size = androidx.compose.ui.geometry.Size(obstacle.width.toPx(), obstacle.height.toPx())
                    )
                }
            }

            cats.forEach { point ->
                val alpha = if (viewState != null) {
                    calculateAlpha(
                        point, viewState!!.point, viewState!!.fieldOfView.toDp().toPx(), viewState!!.rotation.toDp().toPx(), obstacles
                    ).toDp().toPx()
                } else 1f.toDp().toPx()

                drawCircle(
                    color = if (point == viewState?.point) {
                        Color.Red.copy(alpha = 1f)
                    } else {
                        when (point.state) {
                            State.WALK -> Color.Green.copy(alpha = alpha)
                            State.FIGHT -> Color.Red.copy(alpha = alpha)
                            State.SLEEP -> Color.Blue.copy(alpha = alpha)
                            State.HISS -> Color.Yellow.copy(alpha = alpha)
                        }
                    }, radius = pointRadius.toFloat(), center = Offset(point.x.toPx(), point.y.toPx())
                )

                if (viewState != null) {
                    // Draw view direction lines
                    val baseRotation = viewState!!.rotation
                    val angle = viewState!!.fieldOfView / 2
                    val length = 100f

                    val leftAngle = baseRotation - angle
                    val rightAngle = baseRotation + angle

                    val leftX = viewState!!.point.x.toPx() + length * cos(Math.toRadians(leftAngle.toDouble())).toFloat()
                    val leftY = viewState!!.point.y.toPx() + length * sin(Math.toRadians(leftAngle.toDouble())).toFloat()
                    val rightX = viewState!!.point.x.toPx() + length * cos(Math.toRadians(rightAngle.toDouble())).toFloat()
                    val rightY = viewState!!.point.y.toPx() + length * sin(Math.toRadians(rightAngle.toDouble())).toFloat()

                    // Center view direction line
                    val centerX = viewState!!.point.x.toPx() + length * cos(Math.toRadians(baseRotation.toDouble())).toFloat()
                    val centerY = viewState!!.point.y.toPx() + length * sin(Math.toRadians(baseRotation.toDouble())).toFloat()

                    drawLine(
                        color = Color.Gray,
                        start = Offset(viewState!!.point.x.toPx(), viewState!!.point.y.toPx()),
                        end = Offset(leftX, leftY)
                    )

                    drawLine(
                        color = Color.Gray,
                        start = Offset(viewState!!.point.x.toPx(), viewState!!.point.y.toPx()),
                        end = Offset(rightX, rightY)
                    )

                    drawLine(
                        color = Color.Red,
                        start = Offset(viewState!!.point.x.toPx(), viewState!!.point.y.toPx()),
                        end = Offset(centerX, centerY)
                    )
                }
            }
        }

        if (viewState != null) {
            Text("Угол обзора: ${viewState!!.fieldOfView.toInt()}°")
            Text("Поворот: ${viewState!!.rotation.toInt()}°")
            Slider(
                value = viewState!!.fieldOfView,
                onValueChange = { viewState = viewState!!.copy(fieldOfView = it) },
                valueRange = 10f..180f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Calculates the alpha value for an obstacle based on its visibility.
 *
 * @param obstacle The obstacle to calculate the alpha for.
 * @param selectedPoint The selected cat.
 * @param fieldOfView The field of view angle.
 * @param rotation The rotation angle.
 * @return The alpha value for the obstacle.
 */
fun calculateObstacleAlpha(
    obstacle: Obstacle, selectedPoint: Cat, fieldOfView: Float, rotation: Float
): Float {
    val dx = obstacle.x - selectedPoint.x
    val dy = obstacle.y - selectedPoint.y

    val distance = sqrt(dx.value * dx.value + dy.value * dy.value)
    val angle = atan2(dy.value, dx.value).toDegrees()

    // Normalize the angle relative to the current rotation
    val normalizedAngle = ((angle - rotation + 180) % 360 - 180)

    val halfFOV = fieldOfView / 2

    return if (abs(normalizedAngle) <= halfFOV) {
        // Obstacle is within the field of view
        val maxDistance = 500f // Maximum visibility distance
        (1 - (distance / maxDistance)).coerceIn(0f, 1f)
    } else {
        // Obstacle is outside the field of view
        0f // Not visible
    }
}

/**
 * Calculates the alpha value for a cat based on its visibility.
 *
 * @param point The cat to calculate the alpha for.
 * @param selectedPoint The selected cat.
 * @param fieldOfView The field of view angle.
 * @param rotation The rotation angle.
 * @param obstacles List of obstacles in the environment.
 * @return The alpha value for the cat.
 */
fun calculateAlpha(
    point: Cat, selectedPoint: Cat, fieldOfView: Float, rotation: Float, obstacles: List<Obstacle>
): Float {
    if (point == selectedPoint) return 1f

    val dx = point.x - selectedPoint.x
    val dy = point.y - selectedPoint.y

    val distance = sqrt(dx.value * dx.value + dy.value * dy.value)
    val angle = atan2(dy.value, dx.value).toDegrees()

    // Normalize the angle relative to the current rotation
    val normalizedAngle = ((angle - rotation + 180) % 360 - 180)

    val halfFOV = fieldOfView / 2

    // Check if the point is within any obstacle
    val isInObstacle = obstacles.any { it.contains(point) }

    return if (abs(normalizedAngle) <= halfFOV && !isInObstacle) {
        // Point is within the field of view and not in an obstacle
        val maxDistance = 500f // Maximum visibility distance
        (1 - (distance / maxDistance)).coerceIn(0f, 1f)
    } else {
        // Point is outside the field of view or in an obstacle
        0f
    }
}
