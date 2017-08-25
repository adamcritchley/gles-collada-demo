package com.pfpgames.glgraphics;

public class MGTextCustom extends MGTextAABB {

	public MGTextCustom(RenderedModel[] textList, float[] customPitch, String text, float x, float y, float z, float size){
		super(textList, null, text, x, y, z, size);
		int idx = 0;
		float cum_pitch = 0.0f;

		for( MGCharacter c : chars){
			c.setPitch(customPitch[idx++]);
			c.setOffset(x + cum_pitch, y);
			cum_pitch += c.getPitch();
		}
		text_width = cum_pitch;
	}
}
