#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform sampler2D main_tex;
//uniform samplerExternalOES depth_texture;
uniform float cutoff;
varying vec2 v_TextureCoordinates;

float remap(float value, float inputMin, float inputMax, float outputMin, float outputMax)
{
    return (value - inputMin) * ((outputMax - outputMin) / (inputMax - inputMin)) + outputMin;
}


uniform vec2 uPixelSize;
const float uFar = 1.0;
const float GOLDEN_ANGLE = 2.39996323;
const float MAX_BLUR_SIZE = 8.0;
const float RAD_SCALE = 0.6; // Smaller = nicer blur, larger = faster

float getBlurSize(float depth, float focusPoint, float focusScale)
{
	float coc = clamp((1.0 / focusPoint - 1.0 / depth)*focusScale, -1.0, 1.0);
	return abs(coc) * MAX_BLUR_SIZE;
}

vec4 depthOfField(vec2 texCoord, float focusPoint, float focusScale)
{
    vec4 combined = texture2D(main_tex, texCoord);
	float centerDepth = combined.a;
	float centerSize = getBlurSize(centerDepth, focusPoint, focusScale);
	vec3 color = combined.rgb;
	float tot = 1.0;
	float radius = RAD_SCALE;

	float sampleSum =centerSize;
	for (float ang = 0.0; radius<MAX_BLUR_SIZE; ang += GOLDEN_ANGLE*4.0)
	{
		vec2 tc = texCoord + vec2(cos(ang), sin(ang)) * uPixelSize * radius;

		vec4 sampleCombined = texture2D(main_tex, tc);
		vec3 sampleColor = sampleCombined.rgb;
		float sampleDepth = sampleCombined.a;
		float sampleSize = getBlurSize(sampleDepth, focusPoint, focusScale);
		if (sampleDepth > centerDepth){
			sampleSize = clamp(sampleSize, 0.0, centerSize*2.0);
		}
		sampleSum+=sampleSize;
		float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
		color += mix(color/tot, sampleColor, m);
		tot += 1.0;
		radius += RAD_SCALE/radius;
	}
	return vec4(color /= tot,((sampleSum/MAX_BLUR_SIZE)/tot));
}


void main()
{
    vec2 textureCoordinates = v_TextureCoordinates;
    vec4 blur = depthOfField(textureCoordinates,0.5, 1.0);
    gl_FragColor = blur;
}