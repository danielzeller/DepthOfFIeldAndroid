#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES surface_texture;
uniform samplerExternalOES depth_texture;
varying vec2 v_TextureCoordinates;



void main()
{
    vec2 textureCoordinates = v_TextureCoordinates;
    textureCoordinates.y = 1.0 - textureCoordinates.y;

    vec4 surfaceTextureColor = texture2D(surface_texture, textureCoordinates);
    vec4 depthTextureColor = texture2D(depth_texture, textureCoordinates);
    gl_FragColor = vec4(surfaceTextureColor.rgb, depthTextureColor.r);
}