package com.pfpgames.glcolladademo;


import com.pfpgames.glgraphics.MGModel;
import com.pfpgames.glgraphics.RenderedModel;

public class MGEarth extends MGModel {
	private final static float depth = -4.0f;
	private final static float scale = 0.6f;

	public MGEarth(RenderedModel earth)
	{
		super(earth, depth, scale);
	}

	@Override
	public boolean update(float time, float phase_scale)
	{
		return false;
	}

	@Override
	public boolean hitTest(float vx, float vy) {
		return super.hitTestSphere(vx, vy);
	}

}