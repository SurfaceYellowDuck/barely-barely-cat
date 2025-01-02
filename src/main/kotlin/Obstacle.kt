import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Obstacle(
    val x: Dp, val y: Dp, val width: Dp, val height: Dp
) {
    fun contains(cat: Cat): Boolean {
        return cat.x in x..(x + width) && cat.y in y..(y + height)
    }
}

fun initObstacles(count: Int, screenSize: Pair<Float, Float>): List<Obstacle> {
    val obstacles = mutableListOf<Obstacle>()
    for (i in 0 until count) {
        val obstacle = generateRandomObstacle(screenSize, obstacles)
        obstacles.add(obstacle)
    }
    return obstacles
}

fun Obstacle.intersects(other: Obstacle): Boolean {
    return !(x + width < other.x || other.x + other.width < x || y + height < other.y || other.y + other.height < y)
}

fun generateRandomObstacle(screenSize: Pair<Float, Float>, existingObstacles: List<Obstacle>): Obstacle {
    var obstacle: Obstacle
    do {
        val x = getRandomFloatInRange(0F, screenSize.first)
        val y = getRandomFloatInRange(0F, screenSize.second)
        val width = getRandomFloatInRange(20F, 100F)
        val height = getRandomFloatInRange(20F, 100F)
        obstacle = Obstacle(x.dp, y.dp, width.dp, height.dp)
    } while (existingObstacles.any { it.intersects(obstacle) })
    return obstacle
}