package com.pfpgames.collada;

import com.pfpgames.glmath.Matrix4;

public interface ColladaMesh {

	void   setId(String id);
	String getId();

	void setIndices(int[] indices);
	void setVertices(float[] verts);
	void setTexCoords(float[] coords);
	void setNormals(float[] normals);
	void setBounds(float minx, float maxx, float miny, float maxy, float minz, float maxz);

	ColladaProfileEvaluator createProfileEvaluator();
	
    void drawTriangles(Matrix4 proj, Matrix4 view);
    void drawPoints(Matrix4 proj, Matrix4 view);

	void rotate(float x, float y, float z, float a);
	void translate(float x, float y, float z);
	void scale(float x, float y, float z);
	void transform(ColladaMatrix4f m);
	
	boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view);
	boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view);

	int size();
	void buildMesh();
	void buildMesh(ColladaShader shader);
}
