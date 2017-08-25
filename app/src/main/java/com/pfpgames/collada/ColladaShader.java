package com.pfpgames.collada;

import com.pfpgames.glmath.Matrix4;

import android.content.res.AssetManager;

public interface ColladaShader {

	public abstract int buildShader(AssetManager assetMgr, String texName, String mvpMatrixName);
	public abstract void begin(Matrix4 worldModelView, Matrix4 proj, int numVertices, int numNormals, int numTexCoords);
	public abstract void end();

}
