package no.danielzeller.depthoffield.opengl

import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.ViewGroup
import android.widget.FrameLayout
import no.danielzeller.blurbehindlib.renderers.CommonRenderer
import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer


class CompBatMBLayoutGauss : FrameLayout {

    private lateinit var surfaceView: GLSurfaceView

    constructor(context: Context) : super(context, null)

    private lateinit var commonRenderer: CommonRenderer

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        commonRenderer = CommonRenderer(context, 0.5f, 0f)
        commonRenderer.blurRadius = 40f
        val openGLRenderer = GLSurfaceViewRenderer()

        surfaceView= GLSurfaceView(context)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setZOrderMediaOverlay(true)
        surfaceView.setRenderer(openGLRenderer)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        openGLRenderer.commonRenderer = commonRenderer

        Choreographer.getInstance().postFrameCallback(value)
    }

    val value = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            drawTextureView()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    protected fun drawTextureView() {
        if (commonRenderer.isCreated) {
            val glCanvas = commonRenderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(Color.WHITE)
            if (glCanvas != null) {
                val metaBallContainer = getChildAt(1) as ViewGroup
                setRegPaint(metaBallContainer)
                drawChild(glCanvas, metaBallContainer, drawingTime)
            }
            commonRenderer.surfaceTexture.endDraw(glCanvas)


            val glCanvasDepth = commonRenderer.surfaceDepthTexture.beginDraw()
            glCanvasDepth?.drawColor(Color.WHITE)
            if (glCanvasDepth != null) {
                val metaBallContainer = getChildAt(1) as ViewGroup
                setDepthPaint(metaBallContainer)
                drawChild(glCanvasDepth, metaBallContainer, drawingTime)
            }
            commonRenderer.surfaceDepthTexture.endDraw(glCanvasDepth)
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
            v.setLayerType(LAYER_TYPE_HARDWARE, createPaint(v.translationZ))
        }
    }

    private fun createPaint(depth: Float): Paint {
        val d =Math.abs(depth)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(
            Color.argb(
                255,
                (d * 255f).toInt(),
                (d * 255f).toInt(),
                (d * 255f).toInt()
            ), PorterDuff.Mode.SRC_ATOP
        )

        return paint
    }


    override fun dispatchDraw(canvas: Canvas) {
        drawChild(canvas, surfaceView, drawingTime)
    }
}
