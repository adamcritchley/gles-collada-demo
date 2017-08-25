package com.pfpgames.collada;

public class ColladaParam {
	private ColladaParameter data = null;

	private String id;

	public ColladaParam(String sid) {
		setId(sid);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ColladaParameter createSurface2D() {
		data = new ColladaFxSurface(this);
		return data;
	}

	public ColladaParameter createSampler2D() {
		data = new ColladaFxSampler(this);
		return data;
	}

	public ColladaParameter getParametric() {
		return data;
	}

	public void dispatchParam(ColladaProfileEvaluator eval) {
		data.evaluate(eval.createParamEvaluator());
	}
}
