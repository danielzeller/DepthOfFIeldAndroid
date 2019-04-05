package no.danielzeller.doflib

import android.opengl.GLSurfaceView
import no.danielzeller.depthoffield.opengl.ViewSurfaceTexture

interface DOFRenderer : GLSurfaceView.Renderer {
    fun destroy()
    var isCreated: Boolean
    val surfaceTexture: ViewSurfaceTexture
    val surfaceDepthTexture: ViewSurfaceTexture
}