package com.pfpgames.glmath;

public class Vector2 {
	private static final int VECTOR_DIM = 2;
	public float[] vector = new float[VECTOR_DIM];

	public Vector2(){

	}

	public Vector2(float vx, float vy) {
		vector[0] = vx;
		vector[1] = vy;
	}

	public Vector2(Vector4 v) {
		vector[0] = v.vector[0];
		vector[1] = v.vector[1];
	}
	
	public float Length(){
		return (float) Math.sqrt(vector[0]*vector[0] + vector[1]*vector[1]);
	}
	
	public void Normalize(){
		float length = Length();
		divideThis(length);
	}
	
	public static float Dot(Vector2 a, Vector2 b){
		float result = a.vector[0]*b.vector[0]+a.vector[1]*b.vector[1];
		return result;
	}
	
	public static Vector2 Sub(Vector2 a, Vector2 b){
		Vector2 result = new Vector2();
		result.vector[0] = a.vector[0] - b.vector[0];
		result.vector[1] = a.vector[1] - b.vector[1];
		return result;
	}
	
	public static Vector2 Add(Vector2 a, Vector2 b){
		Vector2 result = new Vector2();
		result.vector[0] = a.vector[0] + b.vector[0];
		result.vector[1] = a.vector[1] + b.vector[1];
		return result;
	}

	public void divideThis(float scalar){
		vector[0] /= scalar;
		vector[1] /= scalar;
	}

	public void setThis(float a, float b){
		vector[0] = a;
		vector[1] = b;
	}

	public float x(){
		return vector[0];
	}

	public float y(){
		return vector[1];
	}
}
