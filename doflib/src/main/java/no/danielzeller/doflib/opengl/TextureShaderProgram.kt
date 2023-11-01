package no.danielzeller.depthoffield.opengl


import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.opengl.GLES30

class TextureShaderProgram(vertexShaderResourceId: Int, fragmentShaderResourceId: Int) :
    ShaderProgram(vertexShaderResourceId, fragmentShaderResourceId) {

    private var uMatrixLocation: Int = 0
    private var uTextureUnitLocation: Int = 0
    private var uTextureDepthUnitLocation: Int = 0
    private var uCutoffUnitLocation: Int = 0
    private var uSizeLocation: Int = 0
    override fun load(context: Context) {
        super.load(context)

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)
        uTextureDepthUnitLocation = glGetUniformLocation(program, U_DEPTH_TEXTURE_UNIT)
        positionAttributeLocation = glGetAttribLocation(program, A_POSITION)
        textureCoordinatesAttributeLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES)
        uCutoffUnitLocation = glGetUniformLocation(program, A_CUTOFF_LOCATION)
        uSizeLocation = glGetUniformLocation(program, "uPixelSize")

    }

    fun setUniformsPass1(
        matrix: FloatArray,
        textureId: Int,
        depthTextureId: Int,
        w: Float,
        h: Float,
        blueNoiseTextureId: Int
    ) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        glActiveTexture(GLES30.GL_TEXTURE0)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        glActiveTexture(GLES30.GL_TEXTURE0 + 1)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, depthTextureId)

        glActiveTexture(GLES30.GL_TEXTURE0 + 2)
        glBindTexture(GLES30.GL_TEXTURE_2D, blueNoiseTextureId)

        glUniform1i(uTextureUnitLocation, 0)
        glUniform1i(uTextureDepthUnitLocation, 1)
        glUniform1i(glGetUniformLocation(program, "blue_noise"), 2)
        glUniform2f(uSizeLocation, w, h)
    }

    fun setUniformsPass2(
        matrix: FloatArray,
        textureId: Int,
        w: Float,
        h: Float,
        blueNoiseTextureId: Int
    ) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GLES30.GL_TEXTURE0)
        glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        glUniform1i(glGetUniformLocation(program, "main_tex"), 0)
        glUniform2f(uSizeLocation, w, h)
        glActiveTexture(GLES30.GL_TEXTURE0 + 1)
        glBindTexture(GLES30.GL_TEXTURE_2D, blueNoiseTextureId)
        glUniform1i(glGetUniformLocation(program, "blue_noise"), 1)
    }


    fun setUniformsHexPass2(
        matrix: FloatArray,
        textureId: Int,
        w: Float,
        h: Float,
        dirX: Float,
        dirY: Float
    ) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GLES30.GL_TEXTURE0)
        glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        glUniform1i(glGetUniformLocation(program, "main_tex"), 0)
        glUniform2f(uSizeLocation, w, h)
        glUniform2f(glGetUniformLocation(program, "dir"), dirX, dirY)
    }


    fun setUniformsPass3(matrix: FloatArray, textureId: Int, oroginalTextureId: Int, originalDepthId: Int) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GLES30.GL_TEXTURE0)
        glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        glUniform1i(glGetUniformLocation(program, "main_tex"), 0)

        glActiveTexture(GLES30.GL_TEXTURE0 + 1)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oroginalTextureId)
        glUniform1i(uTextureUnitLocation, 1)

        glActiveTexture(GLES30.GL_TEXTURE0 + 2)
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, originalDepthId)
        glUniform1i(uTextureDepthUnitLocation, 2)
    }
}
