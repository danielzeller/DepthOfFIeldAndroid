#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES surface_texture;
uniform samplerExternalOES depth_texture;
varying vec2 v_TextureCoordinates;
const float uFar = 1.0;
const float MAX_BLUR_SIZE = 20.0;

float getBlurSize(float depth, float focusPoint, float focusScale)
{
	float coc = clamp((1.0 / focusPoint - 1.0 / depth)*focusScale, -1.0, 1.0);
	return abs(coc) * MAX_BLUR_SIZE;
}

float blurAmount(vec2 texCoord, float focusPoint, float focusScale)
{
	float centerDepth =1.0- texture2D(depth_texture, texCoord).r;

	return centerDepth;
}

void main()                    		
{
    vec2 textureCoordinates = v_TextureCoordinates;
    textureCoordinates.y = 1.0 - textureCoordinates.y;

 	vec4 surfaceTextureColor = texture2D(surface_texture, textureCoordinates);
    float blur = blurAmount(textureCoordinates,0.5, 1.0);
    gl_FragColor = vec4(surfaceTextureColor.rgb, blur);
}