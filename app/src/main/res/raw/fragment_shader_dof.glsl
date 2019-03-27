#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES surface_texture;
uniform samplerExternalOES depth_texture;
uniform float cutoff;
varying vec2 v_TextureCoordinates;

float remap(float value, float inputMin, float inputMax, float outputMin, float outputMax)
{
    return (value - inputMin) * ((outputMax - outputMin) / (inputMax - inputMin)) + outputMin;
}

void main()                    		
{
    vec2 textureCoordinates = v_TextureCoordinates;
    textureCoordinates.y = 1.0 - textureCoordinates.y;

 	vec4 surfaceTextureColor = texture2D(surface_texture, textureCoordinates);
 	vec4 surfaceDepthTextureColor = texture2D(depth_texture, textureCoordinates);

    gl_FragColor =  surfaceTextureColor * surfaceDepthTextureColor;
}