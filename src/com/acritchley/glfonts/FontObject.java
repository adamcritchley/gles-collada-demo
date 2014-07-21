package com.acritchley.glfonts;

public class FontObject {
	private String name;
	private FontMapping cmap;
	private RenderedFont fontRenderer;

	public FontObject(FontMapping map) {
		this.name = map.getName();
		this.cmap = map;
	}

	public String getName() {
		return this.name;
	}

	public void buildFont(){
		FontMapping.RendererMapAttachment attachment = fontRenderer.creatMapAttachment(cmap);
		this.cmap.setAttachment(attachment);
	}

	public void setRenderer(RenderedFont rfont) {
		fontRenderer = rfont;
	}
	
	public void draw(float cx, float cy, float cz, float size, String text){
		fontRenderer.draw(cx,cy,cz,size,text);
	}
}
