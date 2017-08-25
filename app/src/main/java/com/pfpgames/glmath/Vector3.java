package com.pfpgames.glmath;

public class Vector3 {
	private static final int VECTOR_DIM = 3;
	public float[] vector = new float[VECTOR_DIM];
	
	public Vector3(){
		
	}

	public Vector3(float vx, float vy, float vz) {
		vector[0] = vx;
		vector[1] = vy;
		vector[2] = vz;
	}

	public Vector3(Vector4 v) {
		vector[0] = v.vector[0];
		vector[1] = v.vector[1];
		vector[2] = v.vector[2];
	}
	
	public float Length(){
		return (float) Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2]);
	}
	
	public void Normalize(){
		float length = Length();
		divideThis(length);
	}
	
	public static float Dot(Vector3 a, Vector3 b){
		float result = a.vector[0]*b.vector[0]+a.vector[1]*b.vector[1]+a.vector[2]*b.vector[2];
		return result;
	}
	
	public static Vector3 Cross(Vector3 a, Vector3 b){
		Vector3 result = new Vector3();
		result.vector[0] = a.vector[1]*b.vector[2] - a.vector[2]*b.vector[1];
		result.vector[1] = a.vector[2]*b.vector[0] - a.vector[0]*b.vector[2];
		result.vector[2] = a.vector[0]*b.vector[1] - a.vector[1]*b.vector[0];
		return result;
	}
	
	public static Vector3 Sub(Vector3 a, Vector3 b){
		Vector3 result = new Vector3();
		result.vector[0] = a.vector[0] - b.vector[0];
		result.vector[1] = a.vector[1] - b.vector[1];
		result.vector[2] = a.vector[2] - b.vector[2];
		return result;
	}
	
	public static Vector3 Add(Vector3 a, Vector3 b){
		Vector3 result = new Vector3();
		result.vector[0] = a.vector[0] + b.vector[0];
		result.vector[1] = a.vector[1] + b.vector[1];
		result.vector[2] = a.vector[2] + b.vector[2];
		return result;
	}
	
	public void divideThis(float scalar){
		vector[0] /= scalar;
		vector[1] /= scalar;
		vector[2] /= scalar;
	}

	public void setThis(float a, float b, float c){
		vector[0] = a;
		vector[1] = b;
		vector[2] = c;
	}

	public float x(){
		return vector[0];
	}

	public float y(){
		return vector[1];
	}

	public float z(){
		return vector[2];
	}
}
