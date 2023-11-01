package no.danielzeller.depthoffield.animation

import java.util.*

/**
 * Brownian motion is an algorithm for moving to a random position in a dynamic and random way,
 * just like pieces of dust floating in the air.
 *
 * See this video for a detailed info about how it works:
 * https://www.youtube.com/watch?v=4m5JnJBq2AU
 *
 * The algorithm is derived from MetaBalls LIB:
 * https://github.com/danielzeller/MetaBalls-LIB-Android/blob/master/metaballslib/src/main/java/no/danielzeller/metaballslib/progressbar/BrownianMotion.kt
 *
 * Which again is a port from this C# Unity version:
 * https://github.com/keijiro/Klak/blob/master/Assets/Klak/Motion/Runtime/BrownianMotion.cs
 *
 */
class BrownianMotion(private var positionScale: Vector3) {

    /**
     * How often should the motion change direction? Higher values gives more turbulence.
     */
    var positionFrequency = 0.25f

    /**
     * Gives the actual position of the motion. Ranges +- positionScale
     */
    val position = Vector3(0f, 0f, 0f)

    /**
     * The amount of time since the last frame was drawn.
     */
    var deltaTime =0f

    private var positionAmplitude = 0.5f
    private var positionFractalLevel = 3
    private val fbmNorm = 1 / 0.75f
    private val time: FloatArray = FloatArray(6)
    private val frameRate = FrameRateCounter()

    init {
        rehash()
        frameRate.timeStep()
    }

    private fun rehash() {
        for (i in 0..5) {
            time[i] = MathHelper.randomRange(-10000.0f, 0.0f)
        }
    }


    fun update() {

        deltaTime = frameRate.timeStep()
        for (i in 0..2) {
            time[i] += positionFrequency * deltaTime
        }
        var n = Vector3(
                Perlin.fbm(time[0], positionFractalLevel),
                Perlin.fbm(time[1], positionFractalLevel),
                Perlin.fbm(time[2], positionFractalLevel))

        n = Vector3.scale(n, positionScale)
        n.x *= positionAmplitude * fbmNorm
        n.y *= positionAmplitude * fbmNorm
        n.z *= positionAmplitude * fbmNorm

        position.x = n.x
        position.y = n.y
        position.z = n.z
    }

    object Perlin {

        private var perm = intArrayOf(151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180, 151)

        private fun noise(x: Float): Float {
            var xVar = x
            val x1 = Math.floor(xVar.toDouble()).toInt() and 0xff
            xVar -= Math.floor(xVar.toDouble()).toFloat()
            val u = fade(xVar)
            return lerp(u, grad(perm[x1], xVar), grad(perm[x1 + 1], xVar - 1)) * 2
        }

        fun fbm(x: Float, octave: Int): Float {
            var xVar = x
            var f = 0.0f
            var w = 0.5f
            for (i in 0 until octave) {
                f += w * noise(xVar)
                xVar *= 2.0f
                w *= 0.5f
            }
            return f
        }

        private fun fade(t: Float): Float {
            return t * t * t * (t * (t * 6 - 15) + 10)
        }

        private fun lerp(t: Float, a: Float, b: Float): Float {
            return a + t * (b - a)
        }

        private fun grad(hash: Int, x: Float): Float {
            return if (hash and 1 == 0) x else -x
        }
    }
}

class Vector3(var x: Float, var y: Float, var z: Float) {
    companion object {
        fun scale(inVector: Vector3, scaleVector: Vector3): Vector3 {
            inVector.x *= scaleVector.x
            inVector.y *= scaleVector.y
            inVector.z *= scaleVector.z
            return inVector
        }
    }
}

object MathHelper {
    private var rand = Random()
    fun randomRange(min: Float, max: Float): Float {

        val randomNum = rand.nextInt(max.toInt() - min.toInt() + 1) + min.toInt()

        return randomNum.toFloat()
    }
}
