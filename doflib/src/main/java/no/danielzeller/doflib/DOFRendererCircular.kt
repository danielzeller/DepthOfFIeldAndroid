package no.danielzeller.doflib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES20
import android.opengl.Matrix
import no.danielzeller.depthoffield.opengl.RenderTexture
import no.danielzeller.depthoffield.opengl.SpriteMesh
import no.danielzeller.depthoffield.opengl.TextureShaderProgram
import no.danielzeller.depthoffield.opengl.ViewSurfaceTexture
import no.opengl.danielzeller.opengltesting.opengl.util.TextureHelper.loadTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class DOFRendererCircular(private val context: Context, val scale: Float) : DOFRenderer {

    override val surfaceTexture = ViewSurfaceTexture()
    override val surfaceDepthTexture = ViewSurfaceTexture()

    private val projectionMatrixOrtho = FloatArray(16)
    private lateinit var spriteMesh: SpriteMesh

    private val pass1DownsampleAndDepth = TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_dof_pass1_downscale)
    private val pass2Blur = TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_dof_pass2_blur)
    private val pass3FinalComposition = TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_dof_pass3_composition)

    private var downsampledTexture = RenderTexture()
    private var downsampledTextureBlurred = RenderTexture()

    private var width = 0
    private var height = 0

    override var isCreated = false
    private var blueNoiseTextureId = 0

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        clearViewSurfaceTexture()
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        setupViewPort(width, height)
        createRenderingObjects()
        clearViewSurfaceTexture()
        isCreated = true
    }

    override fun onDrawFrame(glUnused: GL10) {
        doRenderFrame()
    }

    private fun clearViewSurfaceTexture() {
        val canvas = surfaceTexture.beginDraw()
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        surfaceTexture.endDraw(canvas)
    }

    private fun createRenderingObjects() {
        spriteMesh = SpriteMesh()
        pass1DownsampleAndDepth.load(context)
        pass2Blur.load(context)
        pass3FinalComposition.load(context)
        surfaceTexture.createSurface(width, height)
        surfaceDepthTexture.createSurface(width, height)
        downsampledTexture.initiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        downsampledTextureBlurred.initiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        blueNoiseTextureId = loadTexture(context, R.drawable.blue_noise)
    }


    private fun setupViewPort(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val left = -1.0f
        val right = 1.0f
        val bottom = 1f
        val top = -1f
        val near = -1.0f
        val far = 1.0f
        Matrix.setIdentityM(projectionMatrixOrtho, 0)
        Matrix.orthoM(projectionMatrixOrtho, 0, left, right, bottom, top, near, far)
    }

    private fun pass1DownsampleAndDepth() {
        setupViewPort((width.toFloat() * scale).toInt(), (height.toFloat() * scale).toInt())
        downsampledTexture.bindRenderTexture()
        surfaceTexture.updateTexture()
        surfaceDepthTexture.updateTexture()
        pass1DownsampleAndDepth.useProgram()
        pass1DownsampleAndDepth.setUniformsPass1(
            projectionMatrixOrtho,
            surfaceTexture.getTextureID(),
            surfaceDepthTexture.getTextureID(),
            width.toFloat() * scale,
            height.toFloat() * scale,
            blueNoiseTextureId
        )
        spriteMesh.bindData(pass1DownsampleAndDepth)
        spriteMesh.draw()
        downsampledTexture.unbindRenderTexture()
    }

    private fun pass2Blur() {
        setupViewPort((width.toFloat() * scale).toInt(), (height.toFloat() * scale).toInt())
        downsampledTextureBlurred.bindRenderTexture()
        pass2Blur.useProgram()
        pass2Blur.setUniformsPass2(
            projectionMatrixOrtho,
            downsampledTexture.fboTex,
            1f / (width.toFloat() * scale),
            1f / (height.toFloat() * scale),
            blueNoiseTextureId
        )
        spriteMesh.bindData(pass2Blur)
        spriteMesh.draw()
        downsampledTextureBlurred.unbindRenderTexture()
    }

    private fun pass3Composition() {
        setupViewPort(width, height)

        pass3FinalComposition.useProgram()
        pass3FinalComposition.setUniformsPass3(
            projectionMatrixOrtho,
            downsampledTextureBlurred.fboTex,
            surfaceTexture.getTextureID(),
            surfaceDepthTexture.getTextureID()
        )
        spriteMesh.bindData(pass3FinalComposition)
        spriteMesh.draw()
    }

    private fun doRenderFrame() {
        pass1DownsampleAndDepth()
        pass2Blur()
        pass3Composition()
    }

    override fun destroy() {
        //TODO: delete resources
    }
}
