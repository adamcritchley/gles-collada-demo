package com.acritchley.collada;

public interface ColladaProfileEvaluator {
	public void evaluateProfileCommon(ColladaProfileCommon prof) throws Exception;
	public void evaluateProfileCg(ColladaProfileCommon prof) throws Exception;
	public void evaluateProfileGLES(ColladaProfileCommon prof) throws Exception;
	public void evaluateProfileGLSL(ColladaProfileCommon prof) throws Exception;
	
	public ColladaParamEvaluator createParamEvaluator();
	public ColladaTechniqueFxEvaluator createTechniqueEvaluator();
}
