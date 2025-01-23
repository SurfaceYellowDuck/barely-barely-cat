import com.google.gson.Gson
import java.io.File

data class Consts(
    val sleepProbability: Float, val w: Float, val h: Float, val pc: Int,
    val refTime: Int, val r0_small: Float, val r0_big: Float,
    val R01_big: Float, val R01_small: Float
)

val config = File("const.json").let {
     Gson().fromJson(it.readText(), Consts::class.java)
} ?: Consts(0.1f, 800f, 800f, 1000, 100, 0.1f, 0.5f, 1f, 5f)

val width = config.w
val height = config.h
