package com.acritchley.collada;

public class ColladaFxSampler implements ColladaParameter {
	private ColladaParam param = null;
	private ColladaFxSurface surface = null;

	public ColladaFxSampler(ColladaParam p){
		param = p;
	}

	public String getId() {
		return this.param.getId();
	}
	
	public void useSurface(ColladaFxSurface srfc){
		surface = srfc;
	}

	public ColladaFxSurface getSurface(){
		return surface;
	}

	@Override
	public void evaluate(ColladaParamEvaluator cpe) {
		cpe.evaluateParam(this);
	}
}
