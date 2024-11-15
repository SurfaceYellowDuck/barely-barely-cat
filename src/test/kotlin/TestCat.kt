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
}