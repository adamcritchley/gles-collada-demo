package com.acritchley.collada;

import java.util.ArrayList;

public class ColladaSceneNode {

	public class Material {
		private ColladaPrimitive primitive;
		private ColladaMaterial material;

		public Material(){
		}

		public void setSymbol(ColladaPrimitive prim) {
			primitive = prim;
		}
	
		public void setTarget(ColladaMaterial mat) {
			material = mat;
		}
	}

	private String id;
	private ColladaVector4f rotX = null;
	private ColladaVector4f rotY = null;
	private ColladaVector4f rotZ = null;
	private ColladaVector3f translate = null;
	private ColladaVector3f scale = null;
	private ColladaMatrix4f matrixTransform = null;
	private ColladaGeometry geometry = null;
	private ColladaController controller = null;
	private ArrayList<Material> materials = new ArrayList<Material>();
	private ColladaSceneNode nodeParent = null;
	private ArrayList<ColladaSceneNode> children = new ArrayList<ColladaSceneNode>();

	public ColladaSceneNode(String value, ColladaSceneNode parent) {
		setId(value);
		nodeParent = parent;
	}

	public void setRotationX(ColladaVector4f vect4f) {
		rotX = vect4f;
	}

	public void setRotationY(ColladaVector4f vect4f) {
		rotY = vect4f;
	}

	public void setRotationZ(ColladaVector4f vect4f) {
		rotZ = vect4f;
	}

	public void setTranslation(ColladaVector3f vect3f) {
		translate = vect3f;
	}

	public void setTransform(ColladaMatrix4f matrix4f) {
		matrixTransform = matrix4f;
	}

	public void setScale(ColladaVector3f vect3f) {
		scale = vect3f;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void useGeometry(ColladaGeometry geo) {
		geometry = geo;
	}

	public void useController(ColladaController ctrl) {
		controller = ctrl;
	}

	public Material createMaterial() {
		Material mat = new Material();
		materials.add(mat);
		return mat;
	}

	public boolean isController(){
		return (controller != null);
	}
	
	public boolean isGeometry(){
		return (geometry != null);
	}
	
	public boolean isCamera(){
		// TODO: Add cameras?
		return false;
	}

	public boolean isLight(){
		// TODO: Add lighting?
		return false;
	}

	public void buildSceneNode(ColladaModel model) {
		if( isGeometry() ){
			if( materials.size() == 0 ){
				geometry.constructMesh(model);

				if( matrixTransform != null ){
					model.transform(matrixTransform);
				}
				if( translate != null ){
					model.translate(translate.x, translate.y, translate.z);
				}
				if( rotZ != null ){
					model.rotate(rotZ.x, rotZ.y, rotZ.z, rotZ.a);
				}
				if( rotY != null ){
					model.rotate(rotY.x, rotY.y, rotY.z, rotY.a);
				}
				if( rotX != null ){
					model.rotate(rotX.x, rotX.y, rotX.z, rotX.a);
				}
				if( scale != null ){
					model.scale(scale.x, scale.y, scale.z);
				}
			}else{
				for( Material mat : materials ){
					ColladaMesh mesh = model.createMesh();
					mat.primitive.constructMesh(mesh);
	
					if( matrixTransform != null ){
						mesh.transform(matrixTransform);
					}
					if( translate != null ){
						mesh.translate(translate.x, translate.y, translate.z);
					}
					if( rotZ != null ){
						mesh.rotate(rotZ.x, rotZ.y, rotZ.z, rotZ.a);
					}
					if( rotY != null ){
						mesh.rotate(rotY.x, rotY.y, rotY.z, rotY.a);
					}
					if( rotX != null ){
						mesh.rotate(rotX.x, rotX.y, rotX.z, rotX.a);
					}
					if( scale != null ){
						mesh.scale(scale.x, scale.y, scale.z);
					}

					mat.material.shadeMesh(mesh);
				}
			}
		}else if( isController() ){
			// Parse it here.
		}
	}

	public ColladaSceneNode getParent() {
		return nodeParent;
	}

	public void addChild(ColladaSceneNode child) {
		children.add(child);
	}
}
