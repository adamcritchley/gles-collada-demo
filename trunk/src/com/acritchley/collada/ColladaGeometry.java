package com.acritchley.collada;

import java.util.ArrayList;

public class ColladaGeometry {
	private String id = null;
	private ArrayList<ColladaSource> all_sources = new ArrayList<ColladaSource>();
	private ArrayList<ColladaVertices> all_vertices = new ArrayList<ColladaVertices>();
	private ArrayList<ColladaPrimitive> all_primitives = new ArrayList<ColladaPrimitive>();

    public ColladaGeometry( String gid ){
    	setId(gid);
    }

    public ColladaVertices createVertices(String id){
    	ColladaVertices vertices = new ColladaVertices(id);
    	all_vertices.add(vertices);
    	return vertices;
    }
	
    public ColladaSource createSource(String id){
    	ColladaSource src = new ColladaSource(id);
    	all_sources.add(src);
		return src;
    }
    
    public ColladaTriangles createTriangles(String id){
    	ColladaTriangles prims = new ColladaTriangles(id);
    	all_primitives.add(prims);
    	return prims;
    }

    public ColladaPolylist createPolylist(String id){
    	ColladaPolylist prims = new ColladaPolylist(id);
    	all_primitives.add(prims);
    	return prims;
    }

	public ColladaSource getSourceById(String value) {
		for( ColladaSource src : all_sources)
		{
			if( src.getId().equalsIgnoreCase(value) )
			{
				return src;
			}
		}

		return null;
	}

	public ColladaVertices getVerticesById(String value) {
		for( ColladaVertices vrtx : all_vertices)
		{
			if( vrtx.getId().equalsIgnoreCase(value) )
			{
				return vrtx;
			}
		}

		return null;
	}

	public ColladaPrimitive getPrimitiveById(String value) {
		for( ColladaPrimitive prim : all_primitives){
			if( prim.getId().equalsIgnoreCase(value) )
			{
				return prim;
			}
		}

		return null;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void constructMesh(ColladaModel model) {
		for( ColladaPrimitive prim : all_primitives){
			ColladaMesh mesh = model.createMesh();
			prim.constructMesh(mesh);
		}
	}
}
