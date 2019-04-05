package no.danielzeller.depthoffield

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.REVERSE
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.animation.PathInterpolatorCompat
import kotlinx.android.synthetic.main.activity_main3.*
import no.danielzeller.depthoffield.animation.interpolate
import no.danielzeller.depthoffield.animation.onUpdate

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        rootView.childCount
        for (i in 0 until rootView.childCount) {
            val child = rootView.getChildAt(i)
            child.clipToOutline = true
        }

        ObjectAnimator.ofFloat(brawski, View.TRANSLATION_Y, 0f, 300f).setDuration(2000).start()
        val scale = ObjectAnimator.ofFloat(brawski, View.TRANSLATION_Z, 0f, -1f).setDuration(1700)
        scale.repeatCount = 100
        scale.repeatMode = REVERSE
        scale.interpolate(PathInterpolatorCompat.create(.88f, 0f, .13f, .99f))
        scale.onUpdate { value ->
            brawski.scaleX = 1f + (value as Float) * 0.5f
            brawski.scaleY = 1f + (value as Float) * 0.5f
        }
        scale.start()
    }
}
