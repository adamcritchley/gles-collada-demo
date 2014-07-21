package com.acritchley.collada;

public class ColladaTechniqueFx {

	private String id = null;
	private ColladaTechnique technique = null;

	public ColladaTechniqueFx(String sid) {
		setId(sid);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ColladaTechniqueFxPhong createPhongShader(){
		ColladaTechniqueFxPhong phong = new ColladaTechniqueFxPhong(this);
		technique = phong;
		return phong;
	}

	public void dispatchTechnique(ColladaProfileEvaluator eval) {
		technique.evaluate(eval.createTechniqueEvaluator());
	}
}
