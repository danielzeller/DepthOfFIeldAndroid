package no.danielzeller.depthoffield.animation

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.AppCompatImageView
import no.danielzeller.depthoffield.R

const val CLIP_ANIM_DURATION = 800L
private const val ROTATE_SPEED = 0.3f
private const val IMAGE_SCALE_AMOUNT = 0.25f
private const val CIRCLES_SCALE_AMOUNT = 0.15f
val scaleProgressBarInterpolator = PathInterpolator(.57f, .31f, .01f, .88f)

class LoaderImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var introAnim: Animator? = null
    private var alphaAnim: ValueAnimator? = null
    private var scale = 1f

    var isLoaderVisible = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint()
    private val color1: Int
    private val color2: Int
    private val color3: Int
    private val dotSize: Float

    private val circlePath1 = Path()
    private val circlePath2 = Path()
    private val circlePath3 = Path()

    private lateinit var pm1: PathMeasure
    private lateinit var pm2: PathMeasure
    private lateinit var pm3: PathMeasure

    private var clipPathScale = 1f
    private var rotateAmount = 0f

    private val fc = FrameRateCounter()
    private val pathPosition = floatArrayOf(0f, 0f)
    private var loaderAlpha = 0

    init {
        paint.isAntiAlias = true
        color1 = resources.getColor(R.color.progressColor1, null)
        color2 = resources.getColor(R.color.progressColor2, null)
        color3 = resources.getColor(R.color.progressColor3, null)
        paint.strokeWidth = resources.getDimension(R.dimen.progressCircleStrokeWidth)
        dotSize = resources.getDimension(R.dimen.progressDotRadius)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        buildCirclePaths()
    }

    private fun buildCirclePaths() {
        circlePath1.reset()
        circlePath2.reset()
        circlePath3.reset()
        circlePath1.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius1), Path.Direction.CW)
        circlePath2.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius2), Path.Direction.CW)
        circlePath3.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius3), Path.Direction.CW)
        pm1 = PathMeasure(circlePath1, true)
        pm2 = PathMeasure(circlePath2, true)
        pm3 = PathMeasure(circlePath3, true)
    }

    fun introAnimate() {
        scale = 1f + IMAGE_SCALE_AMOUNT
        loaderAlpha = 255
        setHasTransientState(true)
        introAnim = ValueAnimator.ofFloat(1f, 0f).setDuration(CLIP_ANIM_DURATION).onUpdate { value ->
            val animatedValue = value as Float
            scale = 1f + animatedValue * IMAGE_SCALE_AMOUNT
            clipPathScale = animatedValue
        }.interpolate(scaleProgressBarInterpolator).onEnd {
            isLoaderVisible = false
            setHasTransientState(false)
        }
        introAnim!!.start()
        alphaAnim = ValueAnimator.ofInt(255, 0).setDuration(CLIP_ANIM_DURATION).onUpdate { value ->
            loaderAlpha = value as Int
        }
        alphaAnim?.start()

        isLoaderVisible = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        val count = canvas.save()
        if (scale != 1f) {
            canvas.scale(scale, scale, (width / 2).toFloat(), (height / 2).toFloat())
        }
        super.onDraw(canvas)
        canvas.restoreToCount(count)

        drawLoader(canvas)
    }

    private fun drawLoader(canvas: Canvas) {
        if (isLoaderVisible) {
            val count = canvas.save()

            increaseRotation()
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.alpha = loaderAlpha
            canvas.drawPaint(paint)
            drawCircles(canvas)

            canvas.restoreToCount(count)

            invalidate()
        }
    }

    private fun drawCircles(canvas: Canvas) {
        canvas.rotate(-90f, width / 2f, height / 2f)
        val clipScaleParallax = (1f - clipPathScale) * CIRCLES_SCALE_AMOUNT
        canvas.scale(clipPathScale + clipScaleParallax, clipPathScale + clipScaleParallax, width / 2f, height / 2f)
        drawPath(canvas, circlePath1, color1, rotateAmount * 3f, pm1)
        drawPath(canvas, circlePath2, color2, rotateAmount * 2f, pm2)
        drawPath(canvas, circlePath3, color3, rotateAmount, pm3)
    }

    private fun increaseRotation() {
        rotateAmount += fc.timeStep() * ROTATE_SPEED
        if (rotateAmount > 1f) rotateAmount = 0f
    }


    private fun drawPath(canvas: Canvas, path: Path, color: Int, rotationAmount: Float, pm: PathMeasure) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.alpha = loaderAlpha
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        pm.getPosTan(pm.length * (rotationAmount % 1f), pathPosition, null)
        canvas.drawCircle(pathPosition[0], pathPosition[1], dotSize, paint)
    }

    fun cancelIntroAnim() {
        introAnim?.cancel()
        alphaAnim?.cancel()
        loaderAlpha = 255
        scale = 1f
        clipPathScale = 1f
        if (hasTransientState()) {
            setHasTransientState(false)
        }
    }
}