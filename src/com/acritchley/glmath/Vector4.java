package com.acritchley.glmath;

import android.opengl.Matrix;

public class Vector4 {
	private static final int VECTOR_DIM = 4;
	public float[] vector = new float[VECTOR_DIM];
	
	public Vector4(){
		
	}

	public Vector4(float vx, float vy, float vz, float vw) {
		vector[0] = vx;
		vector[1] = vy;
		vector[2] = vz;
		vector[3] = vw;
	}
	
	public static Vector4 Multiply(Matrix4 m, Vector4 v){
		Vector4 result = new Vector4();
		Matrix.multiplyMV(result.vector, 0, m.matrix, 0, v.vector, 0);
		return result;
	}
	
	public static Vector4 Sub(Vector4 a, Vector4 b){
		Vector4 result = new Vector4();
		result.vector[0] = a.vector[0] - b.vector[0];
		result.vector[1] = a.vector[1] - b.vector[1];
		result.vector[2] = a.vector[2] - b.vector[2];
		result.vector[3] = a.vector[3] - b.vector[3];
		return result;
	}
	
	public void divideThis(float scalar){
		vector[0] /= scalar;
		vector[1] /= scalar;
		vector[2] /= scalar;
		vector[3] /= scalar;
	}

	public void setThis(float a, float b, float c, float d){
		vector[0] = a;
		vector[1] = b;
		vector[2] = c;
		vector[3] = d;
	}
}
