package no.danielzeller.doflib


import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.ViewGroup
import android.widget.FrameLayout

private const val HEX_BLUR = 0
private const val CIRCULAR_BLUR = 1

class DOFLayout : FrameLayout {

    private lateinit var surfaceView: GLSurfaceView
    private lateinit var renderer: DOFRenderer
    private var scale = 0.4f
    private var blurType = HEX_BLUR
    private var backgroundFillColor = Color.BLACK

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        loadAttributesFromXML(attrs)
        setupView(context)
    }

    private fun loadAttributesFromXML(attrs: AttributeSet?) {

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.DepthOfField, 0, 0)
        try {
            scale = typedArray.getFloat(R.styleable.DepthOfField_downSample, scale)
            blurType = typedArray.getInteger(R.styleable.DepthOfField_burType, HEX_BLUR)
            backgroundFillColor = typedArray.getColor(R.styleable.DepthOfField_backgroundFillColor, backgroundFillColor)
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupView(context: Context) {
        renderer = createRenderer(context)

        surfaceView = GLSurfaceView(context)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setZOrderMediaOverlay(true)
        surfaceView.setRenderer(renderer)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun createRenderer(context: Context): DOFRenderer {
        if (blurType == HEX_BLUR) {
            return DOFRendererHex(context, scale)
        }
        return DOFRendererCircular(context, scale)
    }

    val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            drawTextureView()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        renderer.destroy()
    }

    protected fun drawTextureView() {
        if (renderer.isCreated) {

            val glCanvas = renderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(backgroundFillColor)
            if (glCanvas != null) {
                val metaBallContainer = getChildAt(1) as ViewGroup
                setRegPaint(metaBallContainer)
                drawChild(glCanvas, metaBallContainer, drawingTime)
            }
            renderer.surfaceTexture.endDraw(glCanvas)

            val glCanvasDepth = renderer.surfaceDepthTexture.beginDraw()
            glCanvasDepth?.drawColor(Color.WHITE)
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
        val d = 0.5 - depth / 2f
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
