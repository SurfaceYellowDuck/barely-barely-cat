import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class KDTreeTest {

    @Test
    fun `test build tree with empty list`() {
        val kdTree = KDTree(emptyList()) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.EUCLIDEAN) }
        assertNull(kdTree.root)
    }

    @Test
    fun `test build tree with single element`() {
        val cat = Cat(1f, 2f)
        val kdTree = KDTree(listOf(cat)) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.EUCLIDEAN) }
        assertEquals(cat, kdTree.root?.cat)
        assertNull(kdTree.root?.left)
        assertNull(kdTree.root?.right)
    }

    @Test
    fun `test nearest neighbor with single element`() {
        val cat = Cat(1f, 2f)
        val kdTree = KDTree(listOf(cat)) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.EUCLIDEAN) }
        val nearest = kdTree.nearestNeighbor(Cat(3f, 4f))
        assertEquals(cat, nearest)
    }

    @Test
    fun `test nearest neighbor with multiple elements`() {
        val cats = listOf(
            Cat(1f, 2f),
            Cat(3f, 4f),
            Cat(-1f, -2f),
            Cat(5f, 6f),
            Cat(0f, 0f)
        )
        val kdTree = KDTree(cats) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.EUCLIDEAN) }

        val target = Cat(1.1f, 2.1f)
        val nearest = kdTree.nearestNeighbor(target)
        assertEquals(Cat(1f, 2f), nearest)
    }

    @Test
    fun `test nearest neighbor with far target` () {
        val cats = listOf(
            Cat(1f, 2f),
            Cat(3f, 4f),
            Cat(-1f, -2f),
            Cat(5f, 6f),
            Cat(0f, 0f)
        )
        val kdTree = KDTree(cats) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.EUCLIDEAN) }

        val target = Cat(100f, 100f)
        val nearest = kdTree.nearestNeighbor(target)
        assertEquals(Cat(5f, 6f), nearest)
    }

    @Test
    fun `test nearest neighbor with Manhattan distance`() {
        val cats = listOf(
            Cat(1f, 2f),
            Cat(3f, 4f),
            Cat(-1f, -2f),
            Cat(5f, 6f),
            Cat(0f, 0f)
        )
        val kdTree = KDTree(cats) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.MANHATTAN) }

        val target = Cat(1.5f, 2.5f)
        val nearest = kdTree.nearestNeighbor(target)
        assertEquals(Cat(1f, 2f), nearest)
    }

    @Test
    fun `test nearest neighbor with Chebyshev distance` () {
        val cats = listOf(
            Cat(1f, 2f),
            Cat(3f, 4f),
            Cat(-1f, -2f),
            Cat(5f, 6f),
            Cat(0f, 0f)
        )
        val kdTree = KDTree(cats) { cat1, cat2 -> distance(cat1, cat2, DistanceMetric.CHEBYSHEV) }

        val target = Cat(1.5f, 2.5f)
        val nearest = kdTree.nearestNeighbor(target)
        assertEquals(Cat(1f, 2f), nearest)
    }
}