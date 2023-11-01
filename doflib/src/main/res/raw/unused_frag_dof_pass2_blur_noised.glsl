#extension GL_OES_EGL_image_external : require
precision highp float;

uniform sampler2D main_tex;
uniform sampler2D blue_noise;

//uniform samplerExternalOES depth_texture;
uniform float cutoff;
varying vec2 v_TextureCoordinates;
uniform vec2 uPixelSize;

const float uFar = 1.0;
const float GOLDEN_ANGLE = 2.39996323;
const float MAX_BLUR_SIZE2 = 20.0;
const float RAD_SCALE = 6.5;// Smaller = nicer blur, larger = faster



float getBlurSize(float depth, float focusPoint, float focusScale)
{
    float coc = clamp((1.0 / focusPoint - 1.0 / depth)*focusScale, -1.0, 1.0);
    return abs(coc) * MAX_BLUR_SIZE2;
}

float blurAmount(float depth, float focusPoint, float focusScale)
{
    float centerDepth =1.0- depth;

    return centerDepth;
}

vec4 depthOfField(vec2 texCoord, float focusPoint, float focusScale)
{
    vec4 color =  texture2D(main_tex, texCoord);
    float centerDepth = color.a * uFar;
    float centerSize = getBlurSize(centerDepth, focusPoint, focusScale);


    float tot = 1.0;
    float radius = RAD_SCALE;
    for (float ang = 0.0; radius<MAX_BLUR_SIZE2; ang += GOLDEN_ANGLE)
    {
        vec2 tc = texCoord + vec2(cos(ang), sin(ang)) * uPixelSize * radius;
        vec2 screenSize = vec2(1.0/uPixelSize.x, 1.0/uPixelSize.y);
        vec2 r =  texture2D(blue_noise, gl_FragCoord.xy/screenSize.xy * 2.0).xy;
        r.x*=6.28305308;
        vec2 cr = vec2(sin(r.x), cos(r.x))*sqrt(r.y);

        vec4 sampleColor =  texture2D(main_tex, tc+cr*(radius/screenSize.xy));
        float sampleDepth = sampleColor.a * uFar;
        float sampleSize = getBlurSize(sampleDepth, focusPoint, focusScale);
        if (sampleDepth > centerDepth)
        sampleSize = clamp(sampleSize, 0.0, centerSize*2.0);
        float m = smoothstep(radius-0.5, radius+0.5, sampleSize);
        color += mix(color/tot, sampleColor, m);
        tot += 1.0;
        radius += RAD_SCALE/radius;
    }
    return color /= tot;
}

void main()
{
    vec2 textureCoordinates = v_TextureCoordinates;
    vec4 blur = depthOfField(textureCoordinates, 0.5, 1.0);
    gl_FragColor = blur;
}