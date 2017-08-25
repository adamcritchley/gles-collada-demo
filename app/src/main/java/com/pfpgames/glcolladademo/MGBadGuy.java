package com.pfpgames.glcolladademo;

import com.pfpgames.glgraphics.MGModel;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glgraphics.gl2es.Gl2Model;

public class MGBadGuy extends MGModel {
	private static final float depth = -3.5f;
	private static final float scale = 0.30f;
	private float t;
	private float ti;

	public MGBadGuy(Gl2Model logo)
	{
		super(logo, depth, scale);
	}

	void setTheta(float theta){
		t = theta;
	}
	
	float getTheta(){
		return t;
	}

	float getTime(){
		return ti;
	}
	
	@Override
	public void draw(Matrix4 projection) {
		super.draw(projection);
	}

	@Override
	public boolean hitTest(float vx, float vy) {
		return super.hitTestSphere(vx, vy);
	}

	public void setTime(float time) {
		ti = time;
	}

	@Override
	public boolean update(float time, float special_scale) {
		return false;
	}
}