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

    @Test
    fun testMultipleCatsWithSameCoordinates() {
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val targetCat = Cat(10f, 20f, State.SLEEP, 2, 7)
        val nearestNeighbor = Cat(10f, 20f, State.WALK, 0, 10)
        val cats = listOf(
            Cat(10f, 20f, State.SLEEP, 2, 7),
            Cat(10f, 20f, State.WALK, 0, 10),
            Cat(30f, 40f, State.WALK, 2, 10)
        )
        val kdTree = KDTree(cats, dista)

        val nearest = kdTree.nearestNeighbor(targetCat)

        assertEquals(nearestNeighbor, nearest)
    }

    // precondition: set "sleepProbability" : 1 in json
    @Test
    fun testCatsEnterSleepState() {
        val screenSize = Pair(100F, 100F)
        val cats = initCats(5, screenSize)
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val kdTree = KDTree(cats, dista)
        val newCats = updateCats(cats, kdTree, dista, screenSize)

        assertTrue(newCats.all { it.state == State.SLEEP })
    }

    // precondition: set "sleepProbability" : 0 in json
    @Test
    fun testCatsWithoutNeighborsWalk() {
        val screenSize = Pair(100F, 100F)
        val cat1 = Cat(10F, 10F)
        val cat2 = Cat(90F, 90F)
        val cats = listOf(cat1, cat2)
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val kdTree = KDTree(cats, dista)
        val newCats = updateCats(cats, kdTree, dista, screenSize)

        assertTrue(newCats.all { it.state == State.WALK })
    }

}