package com.acritchley.collada;

public interface ColladaModel {
	public ColladaMesh createMesh();

	public void translate(float x, float y, float z);
	public void rotate(float x, float y, float z, float a);
	public void scale(float x, float y, float z);
}
