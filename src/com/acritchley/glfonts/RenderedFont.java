package com.acritchley.glfonts;

public interface RenderedFont {

	void draw(float cx, float cy, float cz, float size, String text);

	FontMapping.RendererMapAttachment creatMapAttachment(FontMapping cmap);
}
