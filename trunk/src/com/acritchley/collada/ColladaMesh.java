package com.acritchley.collada;

import com.acritchley.glmath.Matrix4;

public interface ColladaMesh {

	public void   setId(String id);
	public String getId();

	public void setIndices(int[] indices);
	public void setVertices(float[] verts);
	public void setTexCoords(float[] coords);
	public void setNormals(float[] normals);
	public void setBounds(float minx, float maxx, float miny, float maxy, float minz, float maxz);

	public ColladaProfileEvaluator createProfileEvaluator();
	
    public void drawTriangles(Matrix4 proj, Matrix4 view);
    public void drawPoints(Matrix4 proj, Matrix4 view);

	public void rotate(float x, float y, float z, float a);
	public void translate(float x, float y, float z);
	public void scale(float x, float y, float z);
	public void transform(ColladaMatrix4f m);
	
	boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view);
	boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view);

	public int size();
	public void buildMesh();
	public void buildMesh(ColladaShader shader);
}
