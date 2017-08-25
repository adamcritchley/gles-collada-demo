package com.pfpgames.collada;

public class ColladaFxSurface implements ColladaParameter {
	private ColladaParam param = null;
	private ColladaImage image = null;

	public ColladaFxSurface(ColladaParam p){
		param = p;
	}

	public String getId() {
		return this.param.getId();
	}
	
	public void useImage(ColladaImage img){
		this.image = img;
	}

	public ColladaImage getImage() {
		return image;
	}

	@Override
	public void evaluate(ColladaParamEvaluator cpe) {
		cpe.evaluateParam(this);
	}
}
