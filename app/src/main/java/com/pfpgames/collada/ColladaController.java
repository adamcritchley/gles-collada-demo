package com.pfpgames.collada;

public class ColladaController {
	private String id;
	private ColladaSkin skin;
	private ColladaSceneNode root;

	public ColladaController(String value) {
		id = value;
	}

	public String getId(){
		return id;
	}

	public ColladaSkin createSkin(ColladaGeometry geometry) {
		skin = new ColladaSkin(geometry);
		return skin;
	}

	public ColladaSkin getSkin() {
		return skin;
	}

	public void useSkeleton(ColladaSceneNode node) {
		root = node;
	}
}
