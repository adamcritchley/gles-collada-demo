package com.pfpgames.collada;

import com.pfpgames.glgraphics.ProgressCallback;

import java.util.ArrayList;

public class ColladaScene {
	private ArrayList<ColladaSceneNode> nodes = new ArrayList<ColladaSceneNode>();
	private String id;
	
    public ColladaScene(String value) {
    	setId(value);
	}

	public ColladaSceneNode createNode(String value, ColladaSceneNode parent) {
		ColladaSceneNode node = new ColladaSceneNode(value, parent);
		nodes.add(node);
		return node;
	}

	public ColladaSceneNode createJoint(String value, ColladaSceneNode parent) {
		ColladaJointNode node = new ColladaJointNode(value, parent);
		nodes.add(node);
		return node;
	}
	
	public void buildScene(ColladaModel model, ProgressCallback progresscb) {
		for( ColladaSceneNode node : nodes ){
			if( node.isGeometry() ){
				node.buildSceneNode(model, progresscb);
			}
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public ColladaSceneNode getNodeById(String text) {
		for( ColladaSceneNode node : nodes ){
			if( node.getId().equalsIgnoreCase(text) ){
				return node;
			}
		}
		
		return null;
	}
}