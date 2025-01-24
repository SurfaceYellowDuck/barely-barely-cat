import kotlin.random.Random
import androidx.compose.ui.graphics.Color

/**
 * Interface representing the state of a cat.
 */
interface CatState {
    /**
     * Determines the next move of the cat based on its state.
     *
     * @return A pair of floats representing the change in x and y coordinates.
     */
    fun nextMove(): Pair<Float, Float> {
        return Random.nextFloat() * 2 - 1 to Random.nextFloat() * 2 - 1
    }

    /**
     * The color associated with the cat's state.
     */
    val color: Color
}

/**
 * Represents the walking state of a cat.
 */
class WalkingState : CatState {
    override val color: Color
        get() = Color.Green
}

/**
 * Represents the fighting state of a cat.
 */
class FightingState : CatState {
    override val color: Color
        get() = Color.Red
}

/**
 * Represents the sleeping state of a cat.
 */
class SleepingState : CatState {
    override fun nextMove(): Pair<Float, Float> {
        return 0f to 0f
    }

    override val color: Color
        get() = Color.Blue
}

/**
 * Represents the hissing state of a cat.
 */
class HissingState : CatState {
    override val color: Color
        get() = Color.Black
}