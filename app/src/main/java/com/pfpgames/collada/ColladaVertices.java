package com.pfpgames.collada;

public class ColladaVertices {
	private String vrtxId;
	private ColladaSource vertices = null;
	
	public ColladaVertices(String id){
		vrtxId = id;
	}
	
	public String getId(){
		return vrtxId;
	}

	public void createSource(String id){
		vertices = new ColladaSource(id);
	}
	
	public void useSource(ColladaSource src){
		vertices = src;
	}
	
	public ColladaSource getSource(){
		return vertices;
	}
}
