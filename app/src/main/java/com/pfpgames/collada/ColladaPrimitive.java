package com.pfpgames.collada;

public interface ColladaPrimitive {
    public void useVertices(ColladaVertices verts, int offset, int stride);
    public void useNormals(ColladaSource norms, int offset, int stride);
    public void useTexCoords(ColladaSource texcoords, int offset, int stride);
    public ColladaMesh constructMesh(ColladaMesh mesh, boolean trans_uv, boolean flip_u, boolean flip_v);
	public ColladaIndices createIndices();
	public String getId();
}
