import kotlin.random.Random
import androidx.compose.ui.graphics.Color

interface CatState {
    fun nextMove(): Pair<Float, Float> {
        return Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
    }
    val color: Color
}

class WalkingState : CatState {
    override val color: Color
        get() = Color.Green
}

class FightingState : CatState {
    override val color: Color
        get() = Color.Red
}

class SleepingState : CatState {
    override fun nextMove(): Pair<Float, Float> {
        return 0f to 0f
    }

    override val color: Color
        get() = Color.Blue
}

class HissingState : CatState {
    override val color: Color
        get() = Color(0xFFFFC0CB)
}