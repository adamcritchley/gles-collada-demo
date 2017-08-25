precision mediump float;
uniform sampler2D starTexture;

void main()
{
   vec4 vColor = texture2D(starTexture, gl_PointCoord);
   gl_FragColor = vColor;
}
