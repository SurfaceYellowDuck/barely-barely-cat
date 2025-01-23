import kotlin.random.Random


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
    var x: Float = 0F,
    var y: Float = 0F,
    var state: CatState = WalkingState(),
    var sleepTimer: Int = 0,
    var sleepDuration: Int = Random.nextInt(5, 10)
) : Comparable<Cat> {

    /**
     * Compares this Cat object with another based on their x and y coordinates.
     *
     * @param other The other Cat object to be compared.
     * @return A negative integer, zero, or a positive integer as this Cat is less than, equal to,
     * or greater than the specified Cat.
     */
    override fun compareTo(other: Cat) = compareValuesBy(
        this, other,
        { it.x },
        { it.y }
    )

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

        return x == other.x &&
                y == other.y
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of
     * hash tables such as those provided by `HashMap`.
     *
     * @return A hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        val poly_coeff = 31
        result = poly_coeff * result + y.hashCode()
        result = poly_coeff * result + state.hashCode()
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
fun getRandomFloatInRange(a: Float, b: Float): Float {
    return Random.nextFloat() * (b - a) + a
}

/**
 * Generates a random cat positioned within the given screen size.
 *
 * @param screenSize The dimensions of the screen, where the first element is the width and the second is the height.
 * @return A randomly positioned `Cat` instance within the bounds of the given screen size.
 */
fun generateRandomCat(screenSize: Pair<Float, Float>) =
    Cat(getRandomFloatInRange(0F, screenSize.first), getRandomFloatInRange(0F, screenSize.second), WalkingState())

/**
 * Initializes a list of cats with randomly generated positions within the given screen size.
 *
 * @param count The number of cats to initialize.
 * @param screenSize A pair representing the width and height of the screen.
 * @return A list of cats with unique positions within the specified screen size.
 */
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