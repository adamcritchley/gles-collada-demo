package com.acritchley.glfonts;

import java.util.ArrayList;

public class FontMapping {

	private ArrayList<FontCharacter> characterList = new ArrayList<FontCharacter>();
	private FontCharacter[] characterArray = null;

	private String name;
	private String file;
	private String fullFile;
	private RendererMapAttachment rendererAttachment;
	private int minCode = -1;
	private int maxCode = -1;

	public FontMapping(String n, String fn, String ffn) {
		name = n;
		file = fn;
		fullFile = ffn;
	}

	public String getName(){
		return name;
	}
	
	public String getFileName(){
		return file;
	}
	
	public String getFilePath(){
		return fullFile;
	}

	public void addCharacter(int code, float centerX, float centerY,
			float width, float height){
		if( minCode == -1 ){
			minCode = maxCode = code;
		}else{
			minCode = Math.min(minCode, code);
			maxCode = Math.max(maxCode, code);
		}

		FontCharacter character = new FontCharacter(code);
		character.setCenterX(centerX);
		character.setCenterY(centerY);
		character.setHeight(height);
		character.setWidth(width);
		characterList.add(character);
	}

	public void finishFont() {
		characterArray = new FontCharacter[(maxCode-minCode)+1];
		for(FontCharacter fchar : characterList){
			characterArray[fchar.code-minCode] = fchar;
		}
	}

	public FontCharacter getCharacterByCode( int code ) {

		if( code < minCode ) return null;
		if( code > maxCode ) return null;

		return characterArray[code-minCode];
	}

	public void setAttachment(RendererMapAttachment renderedMap) {
		this.rendererAttachment = renderedMap;

		for(FontCharacter fchar : characterList){
			RendererCharacterAttachment attachment = this.rendererAttachment.createCharacterAttachment(fchar);
			fchar.setAttachment(attachment);
		}
	}

	public RendererMapAttachment getAttachment() {
		return rendererAttachment;
	}
	

	public interface RendererMapAttachment {
		public RendererCharacterAttachment createCharacterAttachment(FontCharacter fchar);
	}

	public interface RendererCharacterAttachment {

	}

	public class FontCharacter {
		private int code;
		private float centerX;
		private float centerY;
		private float width;
		private float height;
		private RendererCharacterAttachment rendererAttachment;
		
		public FontCharacter(int c){	
			this.setCode(c);
		}

		public void setCode(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
		public void setCenterX(float centerX) {
			this.centerX = centerX;
		}
		public float getCenterX() {
			return centerX;
		}
		public void setCenterY(float centerY) {
			this.centerY = centerY;
		}
		public float getCenterY() {
			return centerY;
		}
		public void setWidth(float width) {
			this.width = width;
		}
		public float getWidth() {
			return width;
		}
		public void setHeight(float height) {
			this.height = height;
		}
		public float getHeight() {
			return height;
		}

		public void setAttachment(RendererCharacterAttachment attachment) {
			this.rendererAttachment = attachment;
		}

		public RendererCharacterAttachment getAttachment() {
			return rendererAttachment;
		}
	}
}
