import com.google.gson.Gson
import java.io.File

/**
 * Data class representing the configuration constants used in the application.
 *
 * @property sleepProbability Probability of a cat going to sleep.
 * @property w Width of the screen.
 * @property h Height of the screen.
 * @property pc Point count.
 * @property refTime Refresh time.
 * @property r0_small Small radius for interaction.
 * @property r0_big Big radius for interaction.
 * @property R01_big Big radius for detection.
 * @property R01_small Small radius for detection.
 */
data class Consts(
    val sleepProbability: Float, val w: Float, val h: Float, val pc: Int,
    val refTime: Int, val r0_small: Float, val r0_big: Float,
    val R01_big: Float, val R01_small: Float
)

/**
 * Loads the configuration constants from a JSON file.
 */
val config = File("const.json").let {
     Gson().fromJson(it.readText(), Consts::class.java)
} ?: Consts(0.1f, 800f, 800f, 1000, 100, 0.1f, 0.5f, 1f, 5f)

val width = config.w
val height = config.h
