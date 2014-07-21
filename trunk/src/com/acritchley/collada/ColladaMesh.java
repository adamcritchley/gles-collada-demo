package com.acritchley.collada;

public interface ColladaMesh {
	
	public void   setId(String id);
	public String getId();

	public void setIndices(int[] indices);
	public void setVertices(float[] verts);
	public void setTexCoords(float[] coords);
	public void setNormals(float[] normals);

	public ColladaProfileEvaluator createProfileEvaluator();
	
    public void draw();

	public void rotate(float x, float y, float z, float a);
	public void translate(float x, float y, float z);
	public void scale(float x, float y, float z);
}
