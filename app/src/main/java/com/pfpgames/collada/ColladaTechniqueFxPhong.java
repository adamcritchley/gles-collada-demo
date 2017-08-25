package com.pfpgames.collada;

public class ColladaTechniqueFxPhong implements ColladaTechnique{
	private ColladaTechniqueFx technique = null;
	private ColladaVector4f emission = null;
	private ColladaVector4f ambient = null;
	private ColladaTexture diffuse = null;
	private ColladaVector4f specular = null;
	private float shininess = 0.0f;
	private ColladaVector4f reflective = null;
	private float reflectivity = 0.0f;
	private ColladaVector4f transparent = null;
	private float transparency = 0.0f;
	private float index_of_refraction = 0.0f;

	public ColladaTechniqueFxPhong(ColladaTechniqueFx t){
		technique = t;
	}

	public String getId() {
		return this.technique.getId();
	}
	
	public void setDiffuse(ColladaParam diffuse, String map) {
		this.diffuse = new ColladaTexture(diffuse, map);
	}

	public ColladaTexture getDiffuse() {
		return diffuse;
	}

	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

	public float getShininess() {
		return shininess;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setIndexOfRefraction(float index_of_refraction) {
		this.index_of_refraction = index_of_refraction;
	}

	public float getIndexOfRefraction() {
		return index_of_refraction;
	}

	public void setEmission(ColladaVector4f emission) {
		this.emission = emission;
	}

	public ColladaVector4f getEmission() {
		return emission;
	}

	public void setAmbient(ColladaVector4f ambient) {
		this.ambient = ambient;
	}

	public ColladaVector4f getAmbient() {
		return ambient;
	}

	public void setSpecular(ColladaVector4f specular) {
		this.specular = specular;
	}

	public ColladaVector4f getSpecular() {
		return specular;
	}

	public void setReflective(ColladaVector4f reflective) {
		this.reflective = reflective;
	}

	public ColladaVector4f getReflective() {
		return reflective;
	}

	public void setTransparent(ColladaVector4f transparent) {
		this.transparent = transparent;
	}

	public ColladaVector4f getTransparent() {
		return transparent;
	}

	@Override
	public void evaluate(ColladaTechniqueFxEvaluator cte) {
		cte.evaluateTechnique(this);
	}
}
