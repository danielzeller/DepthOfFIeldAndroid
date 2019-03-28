//package no.danielzeller.depthoffield.opengl
//
//import android.content.Context
//import android.graphics.*
//import android.opengl.GLSurfaceView
//import android.util.AttributeSet
//import android.view.Choreographer
//import android.view.SurfaceView
//import android.view.TextureView
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import no.danielzeller.blurbehindlib.renderers.CommonRenderer
//import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer
//
//
//class CompBatMBLayoutSV : FrameLayout {
//
//    private lateinit var surfaceView: SurfaceView
//    private lateinit var textureViewRenderer: TextureViewRenderer
//
//    constructor(context: Context) : super(context, null)
//
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        commonRenderer = CommonRenderer(context, 0.5f, 0f)
//        commonRenderer.blurRadius = 40f
//        val openGLRenderer = GLSurfaceViewRenderer()
//
//        surfaceView= GLSurfaceView(context)
//        surfaceView.setEGLContextClientVersion(2)
//        surfaceView.setZOrderMediaOverlay(true)
//        surfaceView.setRenderer(openGLRenderer)
//        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//
//        addView(surfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
//        openGLRenderer.commonRenderer = commonRenderer
//
//        Choreographer.getInstance().postFrameCallback(value)
//    }
//
//    val value = object : Choreographer.FrameCallback {
//        override fun doFrame(frameTimeNanos: Long) {
//            drawTextureView()
//            Choreographer.getInstance().postFrameCallback(this)
//        }
//    }
//
//    protected fun drawTextureView() {
//        if (textureViewRenderer.isCreated) {
//            textureViewRenderer.cutoffFactor = getCutoffFactor()
//            val glCanvas = textureViewRenderer.surfaceTexture.beginDraw()
//            glCanvas?.drawColor(Color.WHITE)
//            if (glCanvas != null) {
//                val metaBallContainer = getChildAt(1) as ViewGroup
//                setRegPaint(metaBallContainer)
//                drawChild(glCanvas, metaBallContainer, drawingTime)
//            }
//            textureViewRenderer.updateForMilliSeconds(1000)
//            textureViewRenderer.surfaceTexture.endDraw(glCanvas)
//
//
//            val glCanvasDepth = textureViewRenderer.surfaceDepthTexture.beginDraw()
//            glCanvasDepth?.drawColor(Color.BLACK)
//            if (glCanvasDepth != null) {
//                val metaBallContainer = getChildAt(1) as ViewGroup
//                setDepthPaint(metaBallContainer)
//                drawChild(glCanvasDepth, metaBallContainer, drawingTime)
//            }
//            textureViewRenderer.surfaceDepthTexture.endDraw(glCanvasDepth)
//        }
//    }
//
//    private fun setRegPaint(view: ViewGroup) {
//        for (i in 0 until view.childCount) {
//            val v = view.getChildAt(i)
//            v.setLayerType(LAYER_TYPE_HARDWARE, null)
//        }
//    }
//
//    private fun setDepthPaint(view: ViewGroup) {
//        for (i in 0 until view.childCount) {
//            val v = view.getChildAt(i)
//            v.setLayerType(LAYER_TYPE_HARDWARE, createPaint(v.translationZ))
//        }
//    }
//
//    private fun createPaint(depth: Float): Paint {
//        val d = 0.5 + depth / 2f
//        val paint = Paint()
//        paint.colorFilter = PorterDuffColorFilter(
//            Color.argb(
//                255,
//                (d * 255f).toInt(),
//                (d * 255f).toInt(),
//                (d * 255f).toInt()
//            ), PorterDuff.Mode.SRC_ATOP
//        )
//
//        return paint
//    }
//
//    open fun getCutoffFactor(): Float {
//        return 0.65f
//    }
//
//    override fun dispatchDraw(canvas: Canvas) {
//        drawChild(canvas, textureView, drawingTime)
//    }
//}
