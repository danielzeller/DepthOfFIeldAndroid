package no.danielzeller.depthoffield.dof


import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.ViewGroup
import android.widget.FrameLayout
import no.danielzeller.blurbehindlib.renderers.DOFRenderer

class DOFLayout : FrameLayout {

    private lateinit var surfaceView: GLSurfaceView
    private lateinit var renderer: DOFRenderer

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        renderer = DOFRenderer(context)

        surfaceView = GLSurfaceView(context)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setZOrderMediaOverlay(true)
        surfaceView.setRenderer(renderer)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        Choreographer.getInstance().postFrameCallback(value)
    }

    val value = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            drawTextureView()
                Choreographer.getInstance().postFrameCallback(this)

        }
    }

    protected fun drawTextureView() {
        if (renderer.isCreated) {

            val glCanvas = renderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(Color.parseColor("#E0DFE0"))
            if (glCanvas != null) {
                val metaBallContainer = getChildAt(1) as ViewGroup
                setRegPaint(metaBallContainer)
                drawChild(glCanvas, metaBallContainer, drawingTime)
            }
            renderer.surfaceTexture.endDraw(glCanvas)


            val glCanvasDepth = renderer.surfaceDepthTexture.beginDraw()
            glCanvasDepth?.drawColor(Color.BLACK)
            if (glCanvasDepth != null) {
                val metaBallContainer = getChildAt(1) as ViewGroup
                setDepthPaint(metaBallContainer)
                drawChild(glCanvasDepth, metaBallContainer, drawingTime)
            }
            renderer.surfaceDepthTexture.endDraw(glCanvasDepth)
        }
    }

    private fun setRegPaint(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            val v = view.getChildAt(i)
            v.setLayerType(LAYER_TYPE_HARDWARE, null)
        }
    }

    private fun setDepthPaint(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            val v = view.getChildAt(i)
            val paint = (v.tag as Paint?) ?: Paint()
            v.setLayerType(LAYER_TYPE_HARDWARE, createPaint(v.translationZ, paint))
            v.setTag(paint)
        }
    }

    private fun createPaint(depth: Float, paint: Paint): Paint {
        val d = 0.5 + depth / 2f
        val porterDuffColorFilter = PorterDuffColorFilter(
            Color.argb(
                255,
                (d * 255f).toInt(),
                (d * 255f).toInt(),
                (d * 255f).toInt()
            ), PorterDuff.Mode.SRC_ATOP
        )
        paint.colorFilter = porterDuffColorFilter

        return paint
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawChild(canvas, surfaceView, drawingTime)
    }
}
