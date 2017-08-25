
precision mediump float;

varying vec2 vTexCoord;
uniform sampler2D starField;

void main()
{
  vec4 starIntensity = texture2D(starField, vTexCoord);

  // Insert your own sky creation code here...

  // Pass through
  gl_FragColor = starIntensity;
}
