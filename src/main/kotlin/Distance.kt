import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

enum class DistanceMetric {
    EUCLIDEAN,
    MANHATTAN,
    CHEBYSHEV
}

/**
 * Calculates the distance between two cats using a specified metric.
 *
 * @param cat1 The first cat.
 * @param cat2 The second cat.
 * @param metric The distance metric to use (EUCLIDEAN, MANHATTAN, or CHEBYSHEV).
 * @return The distance between the two cats as a Float.
 */
fun distance(cat1: Cat, cat2: Cat, metric: DistanceMetric): Float {
    return when (metric) {
        DistanceMetric.EUCLIDEAN -> sqrt((cat1.x - cat2.x).pow(2) + (cat1.y - cat2.y).pow(2))
        DistanceMetric.MANHATTAN -> abs(cat1.x - cat2.x) + abs(cat1.y - cat2.y)
        DistanceMetric.CHEBYSHEV -> maxOf(abs(cat1.x - cat2.x), abs(cat1.y - cat2.y))
    }
}
