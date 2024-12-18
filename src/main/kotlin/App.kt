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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
import com.google.gson.Gson
import java.io.File
import kotlin.random.Random

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
val w = consts.w
val h = consts.h
val pc = consts.pc
val refTime = consts.refTime

data class ViewState(
    val point: Cat,
    val rotation: Float, // Угол поворота в градусах
    val fieldOfView: Float // Угол обзора
)

// Extension функция для преобразования радиан в градусы
fun Float.toDegrees(): Float = Math.toDegrees(this.toDouble()).toFloat()
fun updateCats(
    cats: List<Cat>,
    catsKDTree: KDTree,
    distance: (Cat, Cat) -> Float,
    screenSize: Pair<Float, Float>,
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

        var newX = (cat.x + dx).coerceIn(0F, screenSize.first)
        var newY = (cat.y + dy).coerceIn(0F, screenSize.second)

        if (cat.sleepTimer > 0) cat.sleepTimer--

        if (obstacles.any { it.contains(Cat(newX, newY)) }) {
            newX = cat.x
            newY = cat.y
        }

        Cat(newX, newY, catState, cat.sleepTimer, cat.sleepDuration)
    }.toList()
    return newCats
}
@Composable
fun app(Cats: List<Cat>) {
//    var cats by remember {
//        mutableStateOf(Cats)
//    }

    var viewState by remember {
        mutableStateOf<ViewState?>(null)
    }

    var isDragging by remember { mutableStateOf(false) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }

    var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
    val width by remember { mutableStateOf(TextFieldValue(w.toString())) }
    val height by remember { mutableStateOf(TextFieldValue(h.toString())) }

    val methods = listOf("euclidean", "manhattan", "chebyshev")
    var selectedMethod by remember { mutableStateOf(methods[0]) }
    var expanded by remember { mutableStateOf(false) }

    var refreshTime by remember { mutableStateOf(TextFieldValue(refTime.toString())) }

    val screenSize = Pair(width.text.toFloat(), height.text.toFloat())
    var cats by remember { mutableStateOf(emptyList<Cat>()) }
    var obstacles by remember { mutableStateOf(initObstacles(5, screenSize)) }

    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, selectedMethod) }
            val catsKDTree = KDTree(cats, dista)
            val newCats = updateCats(cats, catsKDTree, dista, screenSize, obstacles)
            cats = newCats
            kotlinx.coroutines.delay(refreshTime.text.toLongOrNull() ?: 100)
        }
    }

    Column( modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
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
                    onValueChange = { pointCount = it },
                    modifier = androidx.compose.ui.Modifier.width(100.dp),
                    label = { Text("Point Count") })
                TextField(
                    value = refreshTime,
                    onValueChange = { refreshTime = it },
                    modifier = androidx.compose.ui.Modifier.width(100.dp),
                    label = { Text("Refresh Time") })
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
                            cats = initCats(pointCount.text.toInt(), screenSize, obstacles)
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
                                cats = initCats(pointCount.text.toInt(), screenSize, obstacles)
                            }
                        }, colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red, contentColor = Color.White
                        ), modifier = androidx.compose.ui.Modifier.padding(top = 8.dp)
                    ) {
                        Text("Remove Obstacle")
                    }
                }
            }
        }
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!isDragging) {
                            val tappedPoint = cats.find { point ->
                                val distance = sqrt(
                                    (point.x - offset.x).pow(2) +
                                            (point.y - offset.y).pow(2)
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
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            lastDragPosition = offset
                        },
                        onDragEnd = {
                            isDragging = false
                            lastDragPosition = null
                        },
                        onDrag = { change, _ ->
                            if (viewState != null && lastDragPosition != null) {
                                val currentPosition = change.position

                                // Вычисляем угол поворота на основе движения мыши
                                val selectedPoint = viewState!!.point
                                val center = Offset(selectedPoint.x, selectedPoint.y)

                                val lastAngle = atan2(
                                    lastDragPosition!!.y - center.y,
                                    lastDragPosition!!.x - center.x
                                )
                                val currentAngle = atan2(
                                    currentPosition.y - center.y,
                                    currentPosition.x - center.x
                                )

                                val deltaAngle = (currentAngle - lastAngle).toDegrees()

                                viewState = viewState!!.copy(
                                    rotation = (viewState!!.rotation + deltaAngle)
                                )

                                lastDragPosition = currentPosition
                            }
                        }
                    )
                }
        ) {
            val pointRadius = (50.0 / sqrt(cats.size.toFloat())).coerceAtLeast(1.0)

            obstacles.forEach { obstacle ->
                drawRect(
                    color = Color.Gray,
                    topLeft = Offset(obstacle.x.dp.toPx(), obstacle.y.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(obstacle.width.dp.toPx(), obstacle.height.dp.toPx())
                )
            }
            cats.forEach { point ->
                val alpha = if (viewState != null) {
                    calculateAlpha(
                        point,
                        viewState!!.point,
                        viewState!!.fieldOfView,
                        viewState!!.rotation
                    )
                } else 1f

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
//                        Color.Blue.copy(alpha = alpha)
                    },
                    radius =  pointRadius.toFloat(),
                    center = Offset(point.x, point.y)
                )

                if (viewState != null) {
                    // Рисуем линии направления обзора
                    val baseRotation = viewState!!.rotation
                    val angle = viewState!!.fieldOfView / 2
                    val length = 100f

                    val leftAngle = baseRotation - angle
                    val rightAngle = baseRotation + angle

                    val leftX = viewState!!.point.x + length * cos(Math.toRadians(leftAngle.toDouble())).toFloat()
                    val leftY = viewState!!.point.y + length * sin(Math.toRadians(leftAngle.toDouble())).toFloat()
                    val rightX = viewState!!.point.x + length * cos(Math.toRadians(rightAngle.toDouble())).toFloat()
                    val rightY = viewState!!.point.y + length * sin(Math.toRadians(rightAngle.toDouble())).toFloat()

                    // Центральная линия направления взгляда
                    val centerX = viewState!!.point.x + length * cos(Math.toRadians(baseRotation.toDouble())).toFloat()
                    val centerY = viewState!!.point.y + length * sin(Math.toRadians(baseRotation.toDouble())).toFloat()

                    drawLine(
                        color = Color.Gray,
                        start = Offset(viewState!!.point.x, viewState!!.point.y),
                        end = Offset(leftX, leftY)
                    )

                    drawLine(
                        color = Color.Gray,
                        start = Offset(viewState!!.point.x, viewState!!.point.y),
                        end = Offset(rightX, rightY)
                    )

                    drawLine(
                        color = Color.Red,
                        start = Offset(viewState!!.point.x, viewState!!.point.y),
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

fun calculateAlpha(
    point: Cat,
    selectedPoint: Cat,
    fieldOfView: Float,
    rotation: Float
): Float {
    if (point == selectedPoint) return 1f

    val dx = point.x - selectedPoint.x
    val dy = point.y - selectedPoint.y

    val distance = sqrt(dx * dx + dy * dy)
    val angle = atan2(dy, dx).toDegrees()

    // Нормализуем угол относительно текущего поворота
    val normalizedAngle = ((angle - rotation + 180) % 360 - 180)

    val halfFOV = fieldOfView / 2

    return if (abs(normalizedAngle) <= halfFOV) {
        // Точка находится в поле зрения
        val maxDistance = 500f // Максимальная дистанция видимости
        (1 - (distance / maxDistance)).coerceIn(0f, 1f)
    } else {
        // Точка вне поля зрения
        0f
    }
}

//fun main() = application {
//    Window(
//        onCloseRequest = ::exitApplication,
//        resizable = false,
//        title = "Random Point Drawer",
//        state = rememberWindowState(width = width.dp, height = height.dp)
//    ) {
//        val width by remember { mutableStateOf(TextFieldValue(w.toString())) }
//        val height by remember { mutableStateOf(TextFieldValue(h.toString())) }
//        var pointCount by remember { mutableStateOf(TextFieldValue(pc.toString())) }
//        val screenSize = Pair(width.text.toFloat(), height.text.toFloat())
//        var obstacles by remember { mutableStateOf(initObstacles(5, screenSize)) }
//
//        var cats by remember { mutableStateOf(initCats(pointCount.text.toInt(), screenSize, obstacles)) }
//        FirstPersonView(cats)
//    }
//}
