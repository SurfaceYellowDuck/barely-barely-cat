import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TestStates {

    @Test
    fun `test WalkingState`() {
        val walkingState = WalkingState()

        val (dx, dy) = walkingState.nextMove()
        assertTrue(dx in -1f..1f)
        assertTrue(dy in -1f..1f)

        assertEquals(Color.Green, walkingState.color)
    }

    @Test
    fun `test FightingState`() {
        val fightingState = FightingState()

        assertEquals(Color.Red, fightingState.color)
    }

    @Test
    fun `test SleepingState`() {
        val sleepingState = SleepingState()

        val (dx, dy) = sleepingState.nextMove()
        assertEquals(0f, dx)
        assertEquals(0f, dy)

        assertEquals(Color.Blue, sleepingState.color)
    }

    @Test
    fun `test HissingState`() {
        val hissingState = HissingState()

        assertEquals(Color.Yellow, hissingState.color)
    }
}