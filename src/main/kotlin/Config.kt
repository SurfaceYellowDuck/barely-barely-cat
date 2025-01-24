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
data class Config(
    var sleepProbability: Float,
    var w: Float,
    var h: Float,
    var pc: Int,
    var refTime: Int,
    var r0_small: Float,
    var r0_big: Float,
    var R01_big: Float,
    var R01_small: Float
)

fun DefaultConfig() = Config(0.1f, 800f, 800f, 500, 500, 5f, 30f, 90f, 15f)

/**
 * Loads the configuration constants from a JSON file.
 */
fun GetConfigOrDefault(filePath: String) = File(filePath).let {
    Gson().fromJson(it.readText(), Config::class.java)
} ?: DefaultConfig()
