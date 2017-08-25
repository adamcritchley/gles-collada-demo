package com.pfpgames.collada;

public class ColladaMaterial {
	private String id = null;
	private ColladaEffect srcEffect = null;

	public ColladaMaterial(String value) {
		setId(value);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void useEffect(ColladaEffect effect) {
		srcEffect  = effect;
	}

	public void shadeMesh(ColladaMesh mesh) {
		srcEffect.evaluateEffect(mesh);
	}
}
