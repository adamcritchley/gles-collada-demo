package com.acritchley.collada;

public class ColladaMatrix4f {

	public ColladaMatrix4f(float matrix11, float matrix12, float matrix13, float matrix14,
			float matrix21, float matrix22, float matrix23, float matrix24,
			float matrix31, float matrix32, float matrix33, float matrix34,
			float matrix41, float matrix42, float matrix43, float matrix44) {
		m11 = matrix11;
		m12 = matrix12;
		m13 = matrix13; 
		m14 = matrix14;
	    m21 = matrix21;
	    m22 = matrix22; 
	    m23 = matrix23; 
	    m24 = matrix24;
	    m31 = matrix31; 
	    m32 = matrix32; 
	    m33 = matrix33; 
	    m34 = matrix34;
	    m41 = matrix41; 
	    m42 = matrix42; 
	    m43 = matrix43; 
	    m44 = matrix44;
	}

	float m11, m12, m13, m14,
	      m21, m22, m23, m24,
	      m31, m32, m33, m34,
	      m41, m42, m43, m44;
}
