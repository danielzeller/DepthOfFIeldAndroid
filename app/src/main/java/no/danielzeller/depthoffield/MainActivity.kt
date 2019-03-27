package no.danielzeller.depthoffield

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView.childCount
        for (i in 0 until rootView.childCount) {
            val child = rootView.getChildAt(i)
            child.clipToOutline = true
//            child.setLayerType(View.LAYER_TYPE_HARDWARE, createPaint(child.translationZ))
        }
        ObjectAnimator.ofFloat(brawski, View.TRANSLATION_Y,0f,500f).setDuration(2000).start()
    }
}
