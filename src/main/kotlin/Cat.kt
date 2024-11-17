import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow
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
) : Comparable<Cat> {

    override fun compareTo(other: Cat) = compareValuesBy(this, other,
        { it.x },
        { it.y }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cat) return false

        return x == other.x &&
                y == other.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + sleepTimer
        result = 31 * result + sleepDuration
        return result
    }
}

fun getRandomFloatInRange(a: Float, b: Float): Float {
    return Random.nextFloat() * (b - a) + a
}

fun generateRandomCat(screenSize: Pair<Float, Float>) =
    Cat(getRandomFloatInRange(0F, screenSize.first), getRandomFloatInRange(0F, screenSize.second), State.WALK)

fun initCats(count: Int, screenSize: Pair<Float, Float>): List<Cat> {
    val points = mutableListOf<Cat>()
    for (i in 0 until count) {
        var point = generateRandomCat(screenSize)
        while (points.contains(point)) {
            point = generateRandomCat(screenSize)
        }
        points.add(point)
    }
    return points
}

fun distance(cat1: Cat, cat2: Cat, metric: String): Float {
    return when (metric) {
        "euclidean" -> sqrt((cat1.x - cat2.x).pow(2) + (cat1.y - cat2.y).pow(2))
        "manhattan" -> abs(cat1.x - cat2.x) + abs(cat1.y - cat2.y)
        "chebyshev" -> maxOf(abs(cat1.x - cat2.x), abs(cat1.y - cat2.y))
        else -> throw IllegalArgumentException("Invalid distance metric: $metric")
    }
}