package com.pfpgames.collada;

public interface ColladaModel{

	void translate(float x, float y, float z);
	void rotate(float x, float y, float z, float a);
	void scale(float x, float y, float z);
	void transform(ColladaMatrix4f m);

	ColladaMesh createMesh();

	boolean getTransposeTextureUV();
	boolean getFlipTextureU();
	boolean getFlipTextureV();
}
