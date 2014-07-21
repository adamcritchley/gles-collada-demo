package com.acritchley.collada;

public class ColladaTexture {
	private ColladaParam image;
	private String texCoords;

	public ColladaTexture(ColladaParam param, String map) {
		image = param;
		texCoords = map;
	}
}
