import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import androidx.compose.ui.unit.dp

class DistanceTest {
    @Test
    fun `test euclidean distance`() {
        val cat1 = Cat(0.dp, 0.dp)
        val cat2 = Cat(3.dp, 4.dp)
        val dist = distance(cat1, cat2, "euclidean")
        assertEquals(5f, dist, 0.001f)
    }

    @Test
    fun `test manhattan distance`() {
        val cat1 = Cat(0.dp, 0.dp)
        val cat2 = Cat(3.dp, 4.dp)
        val dist = distance(cat1, cat2, "manhattan")
        assertEquals(7f, dist, 0.001f)
    }

    @Test
    fun `test chebyshev distance`() {
        val cat1 = Cat(0.dp, 0.dp)
        val cat2 = Cat(3.dp, 4.dp)
        val dist = distance(cat1, cat2, "chebyshev")
        assertEquals(4f, dist, 0.001f)
    }

    @Test
    fun `test zero distance`() {
        val cat1 = Cat(0.dp, 0.dp)
        val cat2 = Cat(0.dp, 0.dp)
        val dist = distance(cat1, cat2, "euclidean")
        assertEquals(0f, dist, 0.001f)
    }

    @Test
    fun `test cats outside obstacles`() {
        val screenSize = Pair(100.dp, 100.dp)
        val obstacles = initObstacles(5, screenSize)
        val cats = initCats(500, screenSize, obstacles)

        for (cat in cats) {
            for (obstacle in obstacles) {
                assertFalse(obstacle.contains(cat), "Cat $cat is inside obstacle $obstacle")
            }
        }
    }

    @Test
    fun `test nearest neighbor`() {
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val cats = listOf(
            Cat(9.dp, 5.dp),
            Cat(100.dp, 600.dp),
            Cat(15.dp, 15.dp)
        )
        val kdTree = KDTree(cats, dista)

        val target = Cat(9.dp, 6.dp)
        val nearest = kdTree.nearestNeighbor(target)
        
        assertEquals(Cat(9.dp, 5.dp), nearest)
    }

    @Test
    fun `test empty KDTree`() {
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val cats = emptyList<Cat>()
        val kdTree = KDTree(cats, dista)

        assertNull(kdTree.root)
    }
}