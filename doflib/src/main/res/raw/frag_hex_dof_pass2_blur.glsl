precision highp float;

uniform sampler2D main_tex;
varying vec2 v_TextureCoordinates;
uniform vec2 uPixelSize;

uniform vec2 dir;// = vec2(0.02,0.02);//blur direction
const float thresh = 1.0;//depth threshold
uniform float yMultiplier;

float weight(float x){
    return 1.0-x*x*x*x;
}

float convertDepth(float depth){
    return abs(depth-0.5)*2.0;
}
void main()
{
    vec2 uv = gl_FragCoord.xy / uPixelSize.xy;
    float dOriginal=texture2D(main_tex, uv).a;
    float dist = convertDepth(dOriginal);
    float totalw = 1.0;

    vec4 color = texture2D(main_tex, uv);
    for (int i=0; i<=10; i++){
        vec2 p = uv;
        float fi = float(i-5)/5.0;
        p.xy+=dir*fi*dist;

        float w = weight(fi);

        vec4 c = texture2D(main_tex, p);
        float sampleDepth = convertDepth(c.a);
        if (dOriginal>c.a){
            w*=max(.0, 1.0-(dist-sampleDepth)/thresh);
        }
        color += c*w;
        totalw+=w;
    }
    color/=totalw;
    gl_FragColor = vec4(color.rgb, texture2D(main_tex, uv).a);
}