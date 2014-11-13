package com.acritchley.collada;

import com.acritchley.glmath.Matrix4;

public interface ColladaModel{

	public void translate(float x, float y, float z);
	public void rotate(float x, float y, float z, float a);
	public void scale(float x, float y, float z);
	public void transform(ColladaMatrix4f m);

	public boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view);
	public boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view);

	public ColladaMesh createMesh();
}
