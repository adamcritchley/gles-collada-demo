package com.acritchley.collada;

import java.util.ArrayList;

public class ColladaSkin {
	private ColladaMatrix4f bsMatrix = null;
	private ArrayList<ColladaSource> all_sources = new ArrayList<ColladaSource>();
	private ColladaJoints joints = null;
	private ColladaVertexWeights weights = null;
	private ColladaGeometry geometry = null;

	public ColladaSkin(ColladaGeometry geo){
		geometry = geo;
		bsMatrix = new ColladaMatrix4f(1,0,0,0,
				                       0,1,0,0,
				                       0,0,1,0,
				                       0,0,0,1);
	}

	public void setBindShapeMatrix(ColladaMatrix4f matrix) {
		bsMatrix = matrix;
	}

	public ColladaSource createSource(String value) {
		ColladaSource src = new ColladaSource(value);
		all_sources.add(src);
		return src;
	}

	public ColladaJoints createJoints() {
		joints = new ColladaJoints();
		return joints;
	}

	public ColladaVertexWeights createVertexWeights() {
		weights = new ColladaVertexWeights();
		return weights;
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

	public ColladaGeometry getGeometry() {
		return geometry;
	}
}
