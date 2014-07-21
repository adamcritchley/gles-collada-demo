package com.acritchley.collada;

import java.util.ArrayList;

public class ColladaProfileCommon {

    private ColladaTechniqueFx technique = null;
    private ArrayList<ColladaParam> params = new ArrayList<ColladaParam>();
    
    public ColladaProfileCommon(){
    	
    }

	public ColladaTechniqueFx createTechnique(String sid) {
		technique = new ColladaTechniqueFx(sid);
		return technique;
	}

	public ColladaParam createParam(String sid) {
		ColladaParam param = new ColladaParam(sid);
		params.add(param);
		return param;
	}

	public ColladaParam getParamById(String value) {
		for( ColladaParam param : params ){
			if( param.getId().equalsIgnoreCase(value) ){
				return param;
			}
		}
		
		return null;
	}

	public void startEvaluation(ColladaMesh mesh) {
		ColladaProfileEvaluator eval = mesh.createProfileEvaluator();

		try {
			eval.evaluateProfileCommon(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for( ColladaParam param : params ){
			param.dispatchParam(eval);
		}

		technique.dispatchTechnique(eval);
	}
}
