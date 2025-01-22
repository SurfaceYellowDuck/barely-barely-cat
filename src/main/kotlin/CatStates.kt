import kotlin.random.Random
import androidx.compose.ui.graphics.Color

interface CatState {
    fun nextMove(): Pair<Float, Float>
    val color: Color
}

class WalkingState : CatState {
    override fun nextMove(): Pair<Float, Float> {
        return Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
    }

    override val color: Color
        get() = Color.Green
}

class FightingState : CatState {
    override fun nextMove(): Pair<Float, Float> {
        return Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
    }

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
    override fun nextMove(): Pair<Float, Float> {
        return Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
    }

    override val color: Color
        get() = Color.Yellow
}