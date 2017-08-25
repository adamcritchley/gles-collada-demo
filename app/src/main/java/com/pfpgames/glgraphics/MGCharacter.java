package com.pfpgames.glgraphics;

public class MGCharacter extends MGModel {
	private final static float scale = 1.3f;
	private float pitch_width;

	public MGCharacter(RenderedModel charModel, float pitch, float depth) {
		super(charModel, depth, scale);
		pitch_width = pitch;
	}

	public void setPitch(float pitch){
		pitch_width = pitch;
	}
	
	public float getPitch(){
		return pitch_width;
	}
	
	@Override
	public boolean hitTest(float vx, float vy){
		return super.hitTestBox(vx, vy);
	}

	@Override
	public boolean update(float time, float phase_scale) {

		return false;
	}

}
