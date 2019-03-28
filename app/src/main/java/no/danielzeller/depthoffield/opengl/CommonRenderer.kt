package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glDisable
import android.opengl.GLES30
import android.opengl.Matrix
import no.danielzeller.depthoffield.R
import no.danielzeller.depthoffield.opengl.*


class CommonRenderer(
    private val context: Context,
    internal val scale: Float,
    private val paddingVertical: Float
) {

    val surfaceTexture = ViewSurfaceTexture()
    val surfaceDepthTexture = ViewSurfaceTexture()

    var isCreated = false
    var blurRadius = 40f

    private val projectionMatrixOrtho = FloatArray(16)
    private lateinit var spriteMesh: SpriteMesh

    private val fullscreenTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_frag)
    private val fullscreenMaskTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_and_mask_frag)

    private val gauss2PassHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_horizontal)
    private val gauss2PassVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_vertical)
    private var renderTextureHorizontal = RenderTexture()
    private var renderTextureVertical = RenderTexture()
    private var width = 0
    private var height = 0

    fun onSurfaceCreated() {
        glDisable(GL_DEPTH_TEST)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        spriteMesh = SpriteMesh()
        fullscreenTextureShader.load(context)
        fullscreenMaskTextureShader.load(context)
        gauss2PassHorizontal.load(context)
        gauss2PassVertical.load(context)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        renderTextureHorizontal.initiateFrameBuffer(
            (width * scale).toInt(), ((height + paddingVertical) * scale).toInt()
        )
        renderTextureVertical.initiateFrameBuffer((width * scale).toInt(), ((height + paddingVertical) * scale).toInt())
        surfaceTexture.createSurface(width, height)
        surfaceDepthTexture.createSurface(width, height)

        isCreated = true
    }


    private fun setupViewPort(width: Int, height: Int, offset: Float) {
        GLES20.glViewport(0, -(offset * 0.5f).toInt(), width, (height + offset).toInt())
        val left = -1.0f
        val right = 1.0f
        val bottom = 1f
        val top = -1f
        val near = -1.0f
        val far = 1.0f
        Matrix.setIdentityM(projectionMatrixOrtho, 0)
        Matrix.orthoM(projectionMatrixOrtho, 0, left, right, bottom, top, near, far)
    }

    internal fun onDrawFrame() {

        surfaceTexture.updateTexture()
        surfaceDepthTexture.updateTexture()
        blurPass(renderTextureHorizontal, gauss2PassHorizontal, false, surfaceTexture.getTextureID())
        blurPass(renderTextureVertical, gauss2PassVertical, true, renderTextureHorizontal.fboTex)
        renderFullscreenTexture(fullscreenTextureShader)
        GLES20.glClearColor(1f,0f,0f,1f)
    }


    private fun renderFullscreenTexture(shader: TextureShaderProgram) {
        setupViewPort(width, height, paddingVertical)
        shader.useProgram()
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(shader.program, ShaderProgram.U_MATRIX),
            1,
            false,
            projectionMatrixOrtho,
            0
        )

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureVertical.fboTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(shader.program, "mainTexture"), 0)

        spriteMesh.bindData(shader)
        spriteMesh.draw()
    }


    private fun blurPass(
        renderTexture: RenderTexture,
        blurShader: TextureShaderProgram,
        isVerticalPass: Boolean,
        bindTextureID: Int
    ) {
        setupViewPort((width * scale).toInt(), ((height + paddingVertical) * scale).toInt(), 0f)
        renderTexture.bindRenderTexture()
        blurShader.useProgram()

        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(blurShader.program, ShaderProgram.U_MATRIX),
            1,
            false,
            projectionMatrixOrtho,
            0
        )

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        if (bindTextureID != surfaceTexture.getTextureID()) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bindTextureID)
        } else {
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, bindTextureID)
        }
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "mainTexture"), 0)

        if (isVerticalPass) {
            GLES20.glUniform1f(
                GLES20.glGetUniformLocation(blurShader.program, "textureWidth"),
                1f / width.toFloat() / scale
            )
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureHeight"), 0f)
        } else {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureWidth"), 0f)
            GLES20.glUniform1f(
                GLES20.glGetUniformLocation(blurShader.program, "textureHeight"),
                1f / height.toFloat() / scale
            )
        }

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0 + 1)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceDepthTexture.getTextureID())
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "depth_texture"), 1)


        GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "scale"), scale)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "blurRadius"), (blurRadius * scale).toInt())

        spriteMesh.bindData(blurShader)
        spriteMesh.draw()
        renderTexture.unbindRenderTexture()
    }

    fun destroyResources() {
        GLES20.glDeleteProgram(fullscreenTextureShader.program)
        GLES20.glDeleteProgram(fullscreenMaskTextureShader.program)
        GLES20.glDeleteProgram(gauss2PassHorizontal.program)
        GLES20.glDeleteProgram(gauss2PassVertical.program)

        surfaceTexture.releaseSurface()
        surfaceDepthTexture.releaseSurface()
        renderTextureHorizontal.deleteAllTextures()
        renderTextureVertical.deleteAllTextures()
    }
}