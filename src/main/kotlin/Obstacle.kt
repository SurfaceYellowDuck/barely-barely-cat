data class Obstacle(
    val x: Float, val y: Float, val width: Float, val height: Float
) {
    fun contains(cat: Cat): Boolean {
        return cat.x in x..(x + width) && cat.y in y..(y + height)
    }
}

//fun initObstacles(count: Int, screenSize: Pair<Float, Float>): List<Obstacle> {
//    val obstacles = mutableListOf<Obstacle>()
//    for (i in 0 until count) {
//        val x = getRandomFloatInRange(0F, screenSize.first / 2)
//        val y = getRandomFloatInRange(0F, screenSize.second / 2)
//        val width = getRandomFloatInRange(20F, 100F)
//        val height = getRandomFloatInRange(20F, 100F)
//        obstacles.add(Obstacle(x, y, width, height))
//    }
//    return obstacles
//}
fun initObstacles(count: Int, screenSize: Pair<Float, Float>): List<Obstacle> {
    val obstacles = mutableListOf<Obstacle>()
    for (i in 0 until count) {
        var obstacle: Obstacle
        do {
            val x = getRandomFloatInRange(0F, screenSize.first / 2)
            val y = getRandomFloatInRange(0F, screenSize.second / 2)
            val width = getRandomFloatInRange(20F, 100F)
            val height = getRandomFloatInRange(20F, 100F)
            obstacle = Obstacle(x, y, width, height)
        } while (obstacles.any { it.intersects(obstacle) })
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
        val x = getRandomFloatInRange(0F, screenSize.first / 2)
        val y = getRandomFloatInRange(0F, screenSize.second / 2)
        val width = getRandomFloatInRange(20F, 100F)
        val height = getRandomFloatInRange(20F, 100F)
        obstacle = Obstacle(x, y, width, height)
    } while (existingObstacles.any { it.intersects(obstacle) })
    return obstacle
}