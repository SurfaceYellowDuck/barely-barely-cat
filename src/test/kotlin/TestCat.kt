import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TestCat {
    @Test
    fun `test Cat equality`() {
        val cat1 = Cat(x = 1.0f, y = 2.0f)
        val cat2 = Cat(x = 1.0f, y = 2.0f)
        val cat3 = Cat(x = 3.0f, y = 4.0f)

        assertEquals(cat1, cat2)
        assertNotEquals(cat1, cat3)
    }

    @Test
    fun `test Cat comparison`() {
        val cat1 = Cat(x = 1.0f, y = 2.0f)
        val cat2 = Cat(x = 3.0f, y = 4.0f)

        assertTrue(cat1 < cat2)
        assertTrue(cat2 > cat1)
    }

    @Test
    fun `test getRandomFloatInRange`() {
        val min = 1.0f
        val max = 5.0f
        val randomValue = getRandomFloatInRange(min, max)

        assertTrue(randomValue >= min && randomValue < max)
    }

    @Test
    fun `test generateRandomCat`() {
        val screenSize = Pair(100f, 200f)
        val cat = generateRandomCat(screenSize)

        assertTrue(cat.x in 0f..screenSize.first)
        assertTrue(cat.y in 0f..screenSize.second)
    }

    @Test
    fun `test initCats unique positions`() {
        val screenSize = Pair(100f, 200f)
        val cats = initCats(10, screenSize)

        assertEquals(10, cats.size)
        assertTrue(cats.distinctBy { it.x to it.y }.size == cats.size)
    }

    @Test
    fun `test initCats positions within screen size`() {
        val screenSize = Pair(100f, 200f)
        val cats = initCats(10, screenSize)

        cats.forEach { cat ->
            assertTrue(cat.x in 0f..screenSize.first)
            assertTrue(cat.y in 0f..screenSize.second)
        }
    }

    @Test
    fun `test cat equals hashcode contracts`() {
        val cat1 = Cat(1f, 4f)
        val cat2 = Cat(1f, 4f, SleepingState())
        val cat3 = Cat(66f, 59f)
        val cat4 = Cat(1f, 4f, HissingState(), sleepTimer = 5)

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

