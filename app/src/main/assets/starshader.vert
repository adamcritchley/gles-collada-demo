attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;
uniform mat4 modelViewMatrix;
uniform float fIntensity;
uniform float fPhase;
uniform float fFrequency;
float fDensity;
float fRand;

highp float rand(vec2 co)
{
    highp float a = 12.9898;
    highp float b = 78.233;
    highp float c = 43758.5453;
    highp float dt= dot(co.xy ,vec2(a,b));
    highp float sn= mod(dt,3.14);
    return fract(sin(sn) * c);
}

void main()
{
   vec4 pos = modelViewMatrix * aPosition;
   gl_Position = pos;
   vTexCoord = aTexCoord;

   float fRand = rand(pos.xy);
   float fDensity = (1.0 / (length(pos.xy) + 1.0));
   gl_PointSize = fDensity * fRand * fIntensity * sin(pos.y * fFrequency + pos.x * fPhase) * cos(pos.x * fFrequency + pos.y * fPhase);
}
