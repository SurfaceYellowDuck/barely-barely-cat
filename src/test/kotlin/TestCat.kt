import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TestCat {
   @Test
   fun `test distance function`() {
       val cat1 = Cat(0f, 0f)
       val cat2 = Cat(3f, 4f)

       assertEquals(5f, distance(cat1, cat2, "euclidean"))
       assertEquals(7f, distance(cat1, cat2, "manhattan"))
       assertEquals(4f, distance(cat1, cat2, "chebyshev"))
   }

    @Test
    fun testNearestNeighbor() {
        val dista = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val cats = listOf(
            Cat(9.0f, 5.0f),
            Cat(100.0f, 600.0f),
            Cat(15.0f, 15.0f)
        )
        val kdTree = KDTree(cats, dista)

        val target = Cat(9.0f, 6.0f)
        val nearest = kdTree.nearestNeighbor(target)
        
        assertEquals(Cat(9.0f, 5.0f), nearest)
    }

    @Test
    fun testEmptyKDTree() {
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