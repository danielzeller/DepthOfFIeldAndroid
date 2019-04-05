//package no.danielzeller.depthoffield.opengl
//
//
//import android.content.Context
//import android.graphics.Color
//import android.graphics.PorterDuff
//import android.graphics.SurfaceTexture
//import android.opengl.EGL14
//import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
//import android.opengl.GLES20
//import android.opengl.Matrix
//import android.os.Handler
//import android.view.TextureView
//import no.danielzeller.depthoffield.R
//import javax.microedition.khronos.egl.EGL10
//import javax.microedition.khronos.egl.EGL10.*
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.egl.EGLContext
//import javax.microedition.khronos.egl.EGLDisplay
//
//class SVRenderer(val context: Context) : TextureView.SurfaceTextureListener {
//
//    var isCreated = false
//    var cutoffFactor = 0.69f
//    var onSurfaceTextureCreated: (() -> Unit)? = null
//    var onSurfaceTextureRender: (() -> Unit)? = null
//
//    val surfaceTexture = ViewSurfaceTexture()
//    val surfaceDepthTexture = ViewSurfaceTexture()
//
//
//    private var updateViewUntil = -1L
//    private lateinit var renderer: RendererThread
//
//    var scale = 0.5f
//
//    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
//
//    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
//
//    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
//        renderer.isStopped = true
//        return false
//    }
//
//    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
//        renderer = RendererThread(surface, width, height)
//        renderer.start()
//        updateForMilliSeconds(2000)
//    }
//
//    fun updateForMilliSeconds(milliSeconds: Long) {
//        updateViewUntil = System.currentTimeMillis() + milliSeconds
//    }
//
//    inner class RendererThread(private val surface: SurfaceTexture, private val width: Int, private val height: Int) :
//        Thread() {
//
//        var isStopped = false
//        private val projectionMatrixOrtho = FloatArray(16)
//        private lateinit var spriteMesh: SpriteMesh
//        private val pass1DownsampleAndDepth = TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_dof_downscale_pass1)
//        private val pass2Blur = TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_shader_dof_blur_pass2)
//        private val pass3FinalComposition =
//            TextureShaderProgram(R.raw.vertex_shader, R.raw.frag_shader_dof_composition_pass3)
//        private var downsampledTexture = RenderTexture()
//        private var downsampledTextureBlurred = RenderTexture()
//        private val handler = Handler()
//
//
//        override fun run() {
//            super.run()
//            val egl = EGLContext.getEGL() as EGL10
//            val eglDisplay = egl.eglGetDisplay(EGL_DEFAULT_DISPLAY)
//            egl.eglInitialize(eglDisplay, intArrayOf(0, 0))   // getting OpenGL ES 2
//            val eglConfig = chooseEglConfig(egl, eglDisplay)
//            val eglContext = egl.eglCreateContext(
//                eglDisplay,
//                eglConfig,
//                EGL_NO_CONTEXT,
//                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE)
//            )
//            val eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)
//
//
//            while (!isStopped && egl.eglGetError() == EGL_SUCCESS) {
//                if (true) {
//                    egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
//
//                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//                    if (!isCreated) {
//                        onSurfaceCreated()
//                        isCreated = true
//                    }
//                    doRenderFrame()
//                    egl.eglSwapBuffers(eglDisplay, eglSurface)
//                }
//                Thread.sleep((1f / 60f * 1000f).toLong())
//            }
//
//            surface.release()
//            surfaceTexture.releaseSurface()
//            surfaceDepthTexture.releaseSurface()
//            egl.eglDestroyContext(eglDisplay, eglContext)
//            egl.eglDestroySurface(eglDisplay, eglSurface)
//        }
//
//        private val config = intArrayOf(
//            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
//            EGL_RED_SIZE, 8,
//            EGL_GREEN_SIZE, 8,
//            EGL_BLUE_SIZE, 8,
//            EGL_ALPHA_SIZE, 8,
//            EGL_DEPTH_SIZE, 0,
//            EGL_STENCIL_SIZE, 0,
//            EGL_NONE
//        )
//
//        private fun chooseEglConfig(egl: EGL10, eglDisplay: EGLDisplay): EGLConfig {
//            val configsCount = intArrayOf(0)
//            val configs = arrayOfNulls<EGLConfig>(1)
//            egl.eglChooseConfig(eglDisplay, config, configs, 1, configsCount)
//            return configs[0]!!
//        }
//
//        private fun onSurfaceCreated() {
//            setupViewPort(width, height)
//            createRenderingObjects()
//            clearViewSurfaceTexture()
//            handler.post { onSurfaceTextureCreated?.invoke() }
//        }
//
//        private fun clearViewSurfaceTexture() {
//            val canvas = surfaceTexture.beginDraw()
//            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//            surfaceTexture.endDraw(canvas)
//        }
//
//        private fun createRenderingObjects() {
//            spriteMesh = SpriteMesh()
//            pass1DownsampleAndDepth.load(context)
//            pass2Blur.load(context)
//            pass3FinalComposition.load(context)
//            surfaceTexture.createSurface(width, height)
//            surfaceDepthTexture.createSurface(width, height)
//            downsampledTexture.initiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
//            downsampledTextureBlurred.initiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
//        }
//
//
//        private fun setupViewPort(width: Int, height: Int) {
//            GLES20.glViewport(0, 0, width, height)
//            val left = -1.0f
//            val right = 1.0f
//            val bottom = 1f
//            val top = -1f
//            val near = -1.0f
//            val far = 1.0f
//            Matrix.setIdentityM(projectionMatrixOrtho, 0)
//            Matrix.orthoM(projectionMatrixOrtho, 0, left, right, bottom, top, near, far)
//        }
//
//        private fun pass1DownsampleAndDepth() {
//            setupViewPort((width.toFloat() * scale).toInt(), (height.toFloat() * scale).toInt())
//            downsampledTexture.bindRenderTexture()
//            surfaceTexture.updateTexture()
//            surfaceDepthTexture.updateTexture()
//            pass1DownsampleAndDepth.useProgram()
//            pass1DownsampleAndDepth.setUniformsPass1(
//                projectionMatrixOrtho,
//                surfaceTexture.getTextureID(),
//                surfaceDepthTexture.getTextureID(),
//                1f / (width.toFloat() * scale),
//                1f / (height.toFloat() * scale)
//            )
//            spriteMesh.bindData(pass1DownsampleAndDepth)
//            spriteMesh.draw()
//            downsampledTexture.unbindRenderTexture()
//        }
//        private fun pass2DownsampleAndDepth() {
//            setupViewPort((width.toFloat() * scale).toInt(), (height.toFloat() * scale).toInt())
//            downsampledTextureBlurred.bindRenderTexture()
//            pass2Blur.useProgram()
//            pass2Blur.setUniformsPass2(
//                projectionMatrixOrtho,
//                downsampledTexture.fboTex,
//                1f / (width.toFloat() * scale),
//                1f / (height.toFloat() * scale)
//            )
//            spriteMesh.bindData(pass2Blur)
//            spriteMesh.draw()
//            downsampledTextureBlurred.unbindRenderTexture()
//        }
//        private fun pass3Composition() {
//            setupViewPort(width, height)
//
//            pass3FinalComposition.useProgram()
//            pass3FinalComposition.setUniformsPass3(projectionMatrixOrtho, downsampledTextureBlurred.fboTex, surfaceTexture.getTextureID())
//            spriteMesh.bindData(pass3FinalComposition)
//            spriteMesh.draw()
//        }
//
//        private fun doRenderFrame() {
//            onSurfaceTextureRender?.invoke()
//            pass1DownsampleAndDepth()
//            pass2DownsampleAndDepth()
//            pass3Composition()
//        }
////        private fun renderFullscreenRenderTexture() {
////            surfaceTexture.updateTexture()
////            surfaceDepthTexture.updateTexture()
////            fullscreenTextureShader.useProgram()
////            fullscreenTextureShader.setUniformsPass1(
////                projectionMatrixOrtho,
////                surfaceTexture.getTextureID(),
////                surfaceDepthTexture.getTextureID(),
////                cutoffFactor,
////                1f / width.toFloat(),
////                1f / height.toFloat()
////            )
////            spriteMesh.bindData(fullscreenTextureShader)
////            spriteMesh.draw()
////        }
//    }
//
//
//}