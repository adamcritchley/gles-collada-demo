package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.Vector4;

public class MGRotatingText extends MGText {

	public MGRotatingText(RenderedModel[] textList, float[] pitchList, String text,
			float x, float y, float z, float size) {
		super(textList, pitchList, text, x, y, z, size);
	}

	public void draw(Matrix4 projection, float theta, float x, float y, float z) {
		for( MGCharacter c : chars){
			if( c != null ) {
				Vector4 default_color = null;
				if( text_color != null ) {
					default_color = c.getDefaultColor();
					c.setDefaultColor(text_color);
				}
				c.setRotation(theta, x, y, z);
				c.draw(projection);
				if( default_color != null ) {
					c.setDefaultColor(default_color);
				}
			}
		}
	}
}
