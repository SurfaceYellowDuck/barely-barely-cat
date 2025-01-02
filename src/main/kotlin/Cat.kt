import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.abs

/**
 * Represents the different states that an entity (like a cat) can be in.
 *
 * WALK - The entity is in a walking state.
 * FIGHT - The entity is in a fighting state.
 * SLEEP - The entity is in a sleeping state.
 * HISS - The entity is in a hising state.
 */
enum class State {
    WALK, FIGHT, SLEEP, HISS
}

/**
 * A data class representing a cat with specific coordinates, state, and sleeping behavior.
 *
 * @property x The x-coordinate of the cat.
 * @property y The y-coordinate of the cat.
 * @property state The current state of the cat.
 * @property sleepTimer The remaining time the cat needs to sleep.
 * @property sleepDuration The duration for which the cat sleeps.
 */
data class Cat(
    var x: Dp = 0F.dp,
    var y: Dp = 0F.dp,
    var state: State = State.WALK,
    var sleepTimer: Int = 0,
    var sleepDuration: Int = Random.nextInt(5, 10),
    var isSelected: Boolean = false,
) : Comparable<Cat> {

    /**
     * Compares this Cat object with another based on their x and y coordinates.
     *
     * @param other The other Cat object to be compared.
     * @return A negative integer, zero, or a positive integer as this Cat is less than, equal to,
     * or greater than the specified Cat.
     */
    override fun compareTo(other: Cat) = compareValuesBy(this, other, { it.x }, { it.y })

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other The reference object with which to compare.
     * @return true if this object is the same as the other object;
     * false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cat) return false

        return x == other.x && y == other.y
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of
     * hash tables such as those provided by `HashMap`.
     *
     * @return A hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}

/**
 * Generates a random float value within the specified range.
 *
 * @param a The lower bound of the range, inclusive.
 * @param b The upper bound of the range, exclusive.
 * @return A random float value between the specified range [a, b].
 */
fun getRandomFloatInRange(a: Dp, b: Dp): Dp {
    return (Random.nextFloat() * (b.value - a.value) + a.value).dp
}

/**
 * Generates a random cat positioned within the given screen size.
 *
 * @param screenSize The dimensions of the screen, where the first element is the width and the second is the height.
 * @return A randomly positioned `Cat` instance within the bounds of the given screen size.
 */
fun generateRandomCat(screenSize: Pair<Dp, Dp>) =
    Cat(getRandomFloatInRange(0F.dp, screenSize.first), getRandomFloatInRange(0F.dp, screenSize.second), State.WALK)

/**
 * Initializes a list of cats with randomly generated positions within the given screen size.
 *
 * @param count The number of cats to initialize.
 * @param screenSize A pair representing the width and height of the screen.
 * @return A list of cats with unique positions within the specified screen size.
 */
fun initCats(count: Int, screenSize: Pair<Dp, Dp>, obstacles: List<Obstacle>): List<Cat> {
    val points = mutableListOf<Cat>()
    for (i in 0 until count) {
        var point = generateRandomCat(screenSize)
        while (points.contains(point) || obstacles.any { it.contains(point) }) {
            point = generateRandomCat(screenSize)
        }
        points.add(point)
    }
    return points
}

/**
 * Calculates the distance between two cats using a specified metric.
 *
 * @param cat1 The first cat.
 * @param cat2 The second cat.
 * @param metric The distance metric to use ("euclidean", "manhattan", or "chebyshev").
 * @return The distance between the two cats as a Float.
 * @throws IllegalArgumentException If an invalid metric is provided.
 */
fun distance(cat1: Cat, cat2: Cat, metric: String): Float {
    return when (metric) {
        "euclidean" -> sqrt((cat1.x.value - cat2.x.value).pow(2) + (cat1.y.value - cat2.y.value).pow(2))
        "manhattan" -> abs(cat1.x.value - cat2.x.value) + abs(cat1.y.value - cat2.y.value)
        "chebyshev" -> maxOf(abs(cat1.x.value - cat2.x.value), abs(cat1.y.value - cat2.y.value))
        else -> throw IllegalArgumentException("Invalid distance metric: $metric")
    }
}