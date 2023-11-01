package no.danielzeller.depthoffield

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import no.danielzeller.depthoffield.animation.BrownianMotion
import no.danielzeller.depthoffield.animation.Vector3
import no.danielzeller.depthoffield.animation.onUpdate
import no.danielzeller.depthoffield.animation.start
import no.danielzeller.doflib.DOFLayout
import kotlin.random.Random

private const val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/960x540?"

class FloatingImagesActivity : AppCompatActivity() {

    private lateinit var picasso: Picasso
    private val runningAnims = ArrayList<Animator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picasso = Picasso.Builder(applicationContext).memoryCache(LruCache(100000000)).build()

        val dofLayout = DOFLayout(this)
        val container = FrameLayout(this)
        dofLayout.addView(container)

        setContentView(dofLayout)

        val searchTerm = resources.getStringArray(R.array.image_search_term)
        for (i in 0 until 8) {
            val view = addView(container, searchTerm[i])
            animateView(view)
        }


        val focusView = addView(container, searchTerm[8])
        val marginLayoutParams = focusView.layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.leftMargin = resources.displayMetrics.widthPixels / 2 - marginLayoutParams.width / 2
        marginLayoutParams.topMargin = resources.displayMetrics.heightPixels / 2 - marginLayoutParams.height / 2
    }

    override fun onStop() {
        super.onStop()
        for (anim in runningAnims) {
            anim.cancel()
        }
        runningAnims.clear()
    }

    private fun animateView(view: View) {
        val moveXY = resources.displayMetrics.widthPixels.toFloat()
        val motion = BrownianMotion(Vector3(moveXY, moveXY, 1f))
        motion.positionFrequency = 0.15f
        ValueAnimator.ofFloat(0f, 1f).setDuration(10000000000).onUpdate {
            motion.update()
            view.translationX = motion.position.x
            view.translationY = motion.position.y
            val zPos = motion.position.z
            view.translationZ = zPos
            val scale = 1f + zPos
            view.scaleX = scale
            view.scaleY = scale
        }.start(runningAnims)
    }

    private fun addView(container: FrameLayout, searchTerm: String): View {
        val view = LayoutInflater.from(this).inflate(R.layout.image_card_fullscreen, container, false)
        view.clipToOutline = true
        picasso.load(UNSPLASH_RANDOM_URL + searchTerm).config(Bitmap.Config.ARGB_8888).into(view.findViewById<ImageView>(R.id.imageView))

        val size = (resources.displayMetrics.widthPixels * 0.4f).toInt()
        val layoutParams = FrameLayout.LayoutParams(size, size)
        layoutParams.topMargin = Random.nextInt(resources.displayMetrics.heightPixels - size / 2)
        layoutParams.leftMargin = Random.nextInt(resources.displayMetrics.widthPixels - size / 2)

        container.addView(view, layoutParams)
        return view
    }
}
