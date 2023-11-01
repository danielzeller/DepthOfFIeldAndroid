#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES surface_texture;
uniform samplerExternalOES depth_texture;
uniform sampler2D blue_noise;
varying vec2 v_TextureCoordinates;
uniform vec2 uPixelSize;


void main()
{
    vec2 textureCoordinates = v_TextureCoordinates;
    textureCoordinates.y = 1.0 - textureCoordinates.y;
    float scale = 512.0/uPixelSize.y*3.0;
    vec2 uv = vec2(gl_FragCoord.x/uPixelSize.x *scale, gl_FragCoord.y/uPixelSize.x*scale);
    vec4 blueNoise = texture2D(blue_noise, uv);

    vec4 surfaceTextureColor = texture2D(surface_texture, textureCoordinates);
    vec4 depthTextureColor = texture2D(depth_texture, textureCoordinates);

    float blendMaount=  (abs(0.5- depthTextureColor.r)*2.0);

    float blurRadius=30.0*blendMaount;

    vec3 result = vec3(0.0f, 0.0f, 0.0f);

    for (int i = 0; i < 1; ++i)
    {
        vec2 blueNoiseRand;
        if (i == 0)
        blueNoiseRand = blueNoise.xy;
        else
        blueNoiseRand = blueNoise.yz;

        vec2 r= blueNoiseRand;

        r.x*=6.28305308;


        // uniform sample the circle
        vec2 cr = vec2(sin(r.x), cos(r.x))*sqrt(r.y);


        vec3 color = texture2D(surface_texture, textureCoordinates-cr*(blurRadius/uPixelSize.xy)).rgb;
        result = mix(result, color, 1.0 / float(i+1));
    }

    gl_FragColor = vec4(result, depthTextureColor.r);

}