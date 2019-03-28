#extension GL_OES_EGL_image_external : require
precision mediump float;


varying vec2 v_TextureCoordinates;
uniform sampler2D mainTexture;
uniform samplerExternalOES depth_texture;
uniform int blurRadius;


uniform float textureWidth;
uniform float textureHeight;
uniform float scale;

mediump float getGaussWeight(mediump float currentPos, mediump float sigma)
{
    return 1.0 / sigma * exp(-(currentPos * currentPos) / (2.0 * sigma * sigma));
}

void main() {
    int diameter = 2 * blurRadius + 1;
    vec4 sampleTex;
    vec4 sampleDepthTex;
    vec4 col;
    float weightSum = 0.0;
    vec2 flippedYUV = v_TextureCoordinates;
    flippedYUV.y = 1.0 - flippedYUV.y;
    int step = int(float( blurRadius/25)*scale)+1;

    float originalDetph = texture2D(depth_texture,flippedYUV.st).r;

    for(int i = 0; i < diameter; i+=step) {
        vec2 offset = vec2(float(i - blurRadius) * textureWidth,  float(i - blurRadius) * textureHeight);
        sampleDepthTex = texture2D(depth_texture, flippedYUV.st+offset);
        sampleTex = vec4(texture2D(mainTexture, v_TextureCoordinates.st+offset*sampleDepthTex.r));
        float index = float(i);
        float gaussWeight = getGaussWeight(index - float(diameter - 1)/2.0,  (float(diameter - 1)/2.0 + 1.0) / 2.0);
        if(originalDetph<sampleDepthTex.r){
             gaussWeight=0.0;
        }
        col += sampleTex * gaussWeight;
        weightSum += gaussWeight;
    }

     gl_FragColor = col / weightSum;
//    gl_FragColor =  vec4(texture2D(mainTexture, v_TextureCoordinates.st));
}