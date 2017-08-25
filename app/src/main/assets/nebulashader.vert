
attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
uniform mat4 modelViewMatrix;

void main()
{
   vec4 pos = modelViewMatrix * aPosition;
   gl_Position = pos;
   vTexCoord = aTexCoord;

  // Insert your own sky creation code here...
}
