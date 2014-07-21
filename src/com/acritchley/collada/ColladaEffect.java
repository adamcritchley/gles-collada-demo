package com.acritchley.collada;

public class ColladaEffect {

	private String id;
	private ColladaProfileCommon profile;

	public ColladaEffect(String value) {
		setId(value);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ColladaProfileCommon createColladaProfile() {
		profile = new ColladaProfileCommon();
		return profile;
	}

	public void evaluateEffect(ColladaMesh mesh) {
		profile.startEvaluation(mesh);
	}
}
