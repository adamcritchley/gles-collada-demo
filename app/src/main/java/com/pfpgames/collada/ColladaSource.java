package com.pfpgames.collada;

import java.util.ArrayList;

public class ColladaSource {
	public class NameArray {
		
		private ArrayList<String> names = new ArrayList<String>();

		public String getName(int index) {
			return names.get(index);
		}
		
		public int size(){
			return names.size();
		}

		public void setNameData(String[] namesArray) {
			for( String name : namesArray ){
				names.add(name);
			}
		}
	}

	public class FloatArray {

		private float[] data = null;

		public void setFloatData(float[] array) {
			data = array;
		}

		public float[] getFloatData(){
			return data;
		}

		public int size(){
			return data.length;
		}
	}

	private String srcId;
	private FloatArray floatArray = null;
	private NameArray nameArray = null;

	public ColladaSource(String id){
		srcId = id;
	}
	
	public String getId(){
		return srcId;
	}

	public FloatArray getFloatArray() {
		if( floatArray == null ){
			floatArray = new FloatArray();
		}

		return floatArray;
	}

	public NameArray getNameArray() {
		if( nameArray == null ){
			nameArray = new NameArray();
		}
		
		return nameArray;
	}
}
