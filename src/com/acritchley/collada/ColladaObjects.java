package com.acritchley.collada;

import java.util.ArrayList;

public class ColladaObjects {
	private String author = "";
	private String authoringTool = "";
	private String units = "";
	private String meter = "";
	private String created = "";
	private String modified = "";
    private ArrayList<ColladaGeometry> geometries = new ArrayList<ColladaGeometry>();
    private ArrayList<ColladaImage> images = new ArrayList<ColladaImage>();
    private ArrayList<ColladaEffect> effects = new ArrayList<ColladaEffect>();
    private ArrayList<ColladaMaterial> materials = new ArrayList<ColladaMaterial>();
    private ArrayList<ColladaScene> scenes = new ArrayList<ColladaScene>();
    private ArrayList<ColladaController> controllers = new ArrayList<ColladaController>();
    private ColladaScene sceneInstance;

    public ColladaObjects(){
    }

	public ColladaImage createImagery(String value, String name) {
		ColladaImage img = new ColladaImage(value, name);
		images.add(img);
		return img;
	}
	
    public ColladaGeometry createGeometry(String gid){
    	ColladaGeometry geo = new ColladaGeometry(gid);
    	geometries.add(geo);
		return geo;
    }

	public ColladaEffect createEffect(String value) {
		ColladaEffect ce = new ColladaEffect(value);
		effects.add(ce);
		return ce;
	}

	public ColladaMaterial createMaterial(String value) {
		ColladaMaterial cm = new ColladaMaterial(value);
		materials.add(cm);
		return cm;
	}

	public ColladaScene createScene(String value) {
		ColladaScene sn = new ColladaScene(value);
		scenes.add(sn);
		return sn;
	}

	public void buildModel(ColladaModel model){
		sceneInstance.buildScene(model);
	}

	public void setAuthoringTool(String authoringTool) {
		this.authoringTool = authoringTool;
	}

	public String getAuthoringTool() {
		return authoringTool;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getUnits() {
		return units;
	}

	public void setMeter(String meter) {
		this.meter = meter;
	}

	public String getMeter() {
		return meter;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public String getModified() {
		return modified;
	}

	public ColladaImage getImageryById(String text) {
		for( ColladaImage img : images ){
			if( img.getId().equalsIgnoreCase(text) ){
				return img;
			}
		}
		
		return null;
	}

	public ColladaEffect getEffectById(String text) {
		for( ColladaEffect effect : effects ){
			if( effect.getId().equalsIgnoreCase(text) ){
				return effect;
			}
		}
		
		return null;
	}

	public ColladaGeometry getGeometryById(String text) {
		for( ColladaGeometry geometry : geometries ){
			if( geometry.getId().equalsIgnoreCase(text) ){
				return geometry;
			}
		}
		
		return null;
	}

	public ColladaMaterial getMaterialById(String text) {
		for( ColladaMaterial material : materials ){
			if( material.getId().equalsIgnoreCase(text) ){
				return material;
			}
		}

		return null;
	}

	public ColladaScene getSceneById(String text) {
		for( ColladaScene scene : scenes ){
			if( scene.getId().equalsIgnoreCase(text) ){
				return scene;
			}
		}

		return null;
	}

	public void useScene(ColladaScene sceneById) {
		sceneInstance = sceneById;
	}

	public ColladaController createController(String value) {
		ColladaController ctrl = new ColladaController(value);
		controllers.add(ctrl);
		return ctrl;
	}

	public ColladaController getControllerById(String text) {
		for( ColladaController ctrl : controllers ){
			if( ctrl.getId().equalsIgnoreCase(text) ){
				return ctrl;
			}
		}

		return null;
	}
}
