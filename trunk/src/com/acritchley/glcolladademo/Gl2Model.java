package com.acritchley.glcolladademo;

import java.util.ArrayList;

import android.content.res.AssetManager;

import com.acritchley.collada.ColladaMesh;
import com.acritchley.collada.ColladaModel;
import com.acritchley.glmath.Matrix4;

public class Gl2Model implements ColladaModel {
	private AssetManager assetManager;
	private Matrix4 modelViewMatrix;
	private ArrayList<Gl2Mesh> meshes = new ArrayList<Gl2Mesh>();

	@Override
	public ColladaMesh createMesh() {
		Gl2Mesh model = new Gl2Mesh(this);
		meshes.add(model);
		return model;
	}

	public int size() {
		int msize = 0;
		for( Gl2Mesh mesh : meshes ){
			msize += mesh.size();
		}
		return msize;
	}

	public void draw() {
		for( Gl2Mesh mesh : meshes ){
			mesh.draw();
		}
	}

	public void buildModel() {
		for( Gl2Mesh mesh : meshes ){
			mesh.createModel();
			mesh.createShader();
		}
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setModelViewMatrix(Matrix4 m) {
		this.modelViewMatrix = m;
	}

	public Matrix4 getModelViewMatrix() {
		return modelViewMatrix;
	}

	@Override
	public void translate(float x, float y, float z) {
		for( Gl2Mesh mesh : meshes ){
			mesh.translate(x, y, z);
		}
	}

	@Override
	public void rotate(float x, float y, float z, float a) {
		for( Gl2Mesh mesh : meshes ){
			mesh.rotate(x, y, z, a);
		}
	}

	@Override
	public void scale(float x, float y, float z) {
		for( Gl2Mesh mesh : meshes ){
			mesh.scale(x, y, z);
		}
	}

}
