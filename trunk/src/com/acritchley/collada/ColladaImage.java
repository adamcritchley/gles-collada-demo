package com.acritchley.collada;

public class ColladaImage {

	private String id;
	private String imgSource;
	private String imgName;

	public ColladaImage(String value, String name) {
		setId(value);
		setName(name);
	}
	
	public void setSource(String imgSource) {
		this.imgSource = imgSource;
	}

	public String getSource() {
		return imgSource;
	}

	public void setName(String imgName) {
		this.imgName = imgName;
	}

	public String getName() {
		return imgName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
