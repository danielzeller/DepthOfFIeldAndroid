package no.danielzeller.depthoffield.animation

import android.os.SystemClock

class FrameRateCounter {

    private var lastFrameTime: Long = 0

    fun timeStep(): Float {
        val time = SystemClock.uptimeMillis()
        val timeDelta = time - lastFrameTime
        val timeDeltaSeconds = if (lastFrameTime > 0.0f) timeDelta / 1000.0f else 0.0f
        lastFrameTime = time
        return Math.min(0.021f, timeDeltaSeconds)
    }
}

