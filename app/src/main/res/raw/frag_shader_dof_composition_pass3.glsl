#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES surface_texture;

uniform sampler2D  main_tex;
varying vec2 v_TextureCoordinates;



void main()                    		
{
    vec2 textureCoordinates = v_TextureCoordinates;
    vec2 textureCoordinatesFlipped = v_TextureCoordinates;
    textureCoordinatesFlipped.y = 1.0-textureCoordinatesFlipped.y;

    vec4 downSampled = texture2D(main_tex, textureCoordinates);
    vec4 original = texture2D(surface_texture, textureCoordinatesFlipped);
    gl_FragColor = vec4(mix(original.rgb,downSampled.rgb, clamp(downSampled.a*1.0,0.0,1.0)),1.0);
}