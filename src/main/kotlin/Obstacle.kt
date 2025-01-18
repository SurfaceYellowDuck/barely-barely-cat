import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Data class representing an obstacle in the environment.
 *
 * @property x The x-coordinate of the obstacle.
 * @property y The y-coordinate of the obstacle.
 * @property width The width of the obstacle.
 * @property height The height of the obstacle.
 */
data class Obstacle(
    val x: Dp, val y: Dp, val width: Dp, val height: Dp
) {
    /**
     * Checks if the obstacle contains the given cat.
     *
     * @param cat The cat to check.
     * @return True if the obstacle contains the cat, false otherwise.
     */
    fun contains(cat: Cat): Boolean {
        return cat.x in x..(x + width) && cat.y in y..(y + height)
    }
}

/**
 * Initializes a list of obstacles.
 *
 * @param count The number of obstacles to create.
 * @param screenSize The size of the screen.
 * @return A list of obstacles.
 */
fun initObstacles(count: Int, screenSize: Pair<Dp, Dp>): List<Obstacle> {
    val obstacles = mutableListOf<Obstacle>()
    for (i in 0 until count) {
        val obstacle = generateRandomObstacle(screenSize, obstacles)
        obstacles.add(obstacle)
    }
    return obstacles
}

/**
 * Checks if this obstacle intersects with another obstacle.
 *
 * @param other The other obstacle to check against.
 * @return True if the obstacles intersect, false otherwise.
 */
fun Obstacle.intersects(other: Obstacle): Boolean {
    return !(x + width < other.x || other.x + other.width < x || y + height < other.y || other.y + other.height < y)
}

/**
 * Generates a random obstacle that does not intersect with existing obstacles.
 *
 * @param screenSize The size of the screen.
 * @param existingObstacles The list of existing obstacles.
 * @return A new random obstacle.
 */
fun generateRandomObstacle(screenSize: Pair<Dp, Dp>, existingObstacles: List<Obstacle>): Obstacle {
    var obstacle: Obstacle
    do {
        val x = getRandomFloatInRange(0F.dp, screenSize.first)
        val y = getRandomFloatInRange(0F.dp, screenSize.second)
        val width = getRandomFloatInRange(20F.dp, 100F.dp)
        val height = getRandomFloatInRange(20F.dp, 100F.dp)
        obstacle = Obstacle(x, y, width, height)
    } while (existingObstacles.any { it.intersects(obstacle) })
    return obstacle
}