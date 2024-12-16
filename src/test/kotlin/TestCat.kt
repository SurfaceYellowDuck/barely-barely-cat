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
    fun testSingleCat() {
        val dist = { cat1: Cat, cat2: Cat -> distance(cat1, cat2, "euclidean") }
        val screenSize = Pair(500F, 400F)
        val cats = initCats(1, screenSize)
        val kdTree = KDTree(cats, dist)
        val newCats = updateCats(cats, kdTree, dist, screenSize)
        assertEquals(1, newCats.size)
    }

    @Test
    fun testKDTree() {
        val screenSize = Pair(100F, 100F)
        val cats = initCats(100, screenSize)
        val kdTree = KDTree(cats) { cat1, cat2 -> distance(cat1, cat2, "euclidean") }

        for (target in cats) {
            val kdTreeNearest = kdTree.nearestNeighbor(target)

            val exptectedNearest = cats
                .filter { it != target }
                .minByOrNull { distance(it, target, "euclidean") }

            assertEquals(
                exptectedNearest,
                kdTreeNearest,
                "KDTree's nearest neighbor does not match true nearest neighbor."
            )
        }
    }

    @Test
    fun testCatEqualsHashcodeContracts() {
        val cat1 = Cat(1f, 4f)
        val cat2 = Cat(1f, 4f, State.SLEEP)
        val cat3 = Cat(66f, 59f)
        val cat4 = Cat(1f, 4f, State.HISS, sleepTimer = 5)

        // Reflexivity
        assertTrue(cat1 == cat1)
        assertTrue(cat2 == cat2)
        assertTrue(cat3 == cat3)

        // Symmetry
        assertEquals(cat1 == cat2, cat2 == cat1)
        assertEquals(cat1 == cat3, cat3 == cat1)
        assertEquals(cat2 == cat3, cat3 == cat2)

        // Transitivity
        assertTrue(cat1 == cat2)
        assertTrue(cat1 == cat4)
        assertTrue(cat2 == cat4)

        // Consistency
        assertFalse(cat1 == cat3)
        assertFalse(cat1 == cat3)
        assertFalse(cat1 == cat3)
        assertFalse(cat1 == cat3)
        assertFalse(cat1 == cat3)

        // equal objects must have equal hash codes
        assertTrue(cat1 != cat2 || cat1.hashCode() == cat2.hashCode())
        assertTrue(cat1 != cat3 || cat1.hashCode() == cat3.hashCode())
        assertTrue(cat1 != cat4 || cat1.hashCode() == cat4.hashCode())
        assertTrue(cat2 != cat3 || cat2.hashCode() == cat3.hashCode())
        assertTrue(cat2 != cat4 || cat2.hashCode() == cat4.hashCode())
        assertTrue(cat3 != cat4 || cat3.hashCode() == cat4.hashCode())
    }
}
