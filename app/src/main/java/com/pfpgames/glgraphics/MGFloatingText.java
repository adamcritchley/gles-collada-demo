package com.pfpgames.glgraphics;

public class MGFloatingText extends MGText {
	private final float max_float;
	private final float accumulator;
	private float current_float = 0.0f;

	public MGFloatingText(RenderedModel[] textList, float[] pitchList, String text,
						  float x, float y, float z, float size, float coast, float accum) {
		super(textList, pitchList, text, x, y, z, size);
		max_float = coast;
		accumulator = accum;
	}

	@Override
	public boolean update(float time, float unused) {
		current_float += accumulator;
		if( Math.abs(current_float) > max_float ){
			return true;
		}
		super.setOffset(origin_x, origin_y + current_float);
		return false;
	}
}
