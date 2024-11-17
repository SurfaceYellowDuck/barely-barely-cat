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
}