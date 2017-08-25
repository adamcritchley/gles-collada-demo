package com.pfpgames.glcolladademo;

import com.pfpgames.glgraphics.MGModel;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glgraphics.gl2es.Gl2Model;

public class MGNebulaSky extends MGModel {
	private final static float depth = -5.25f;
	private final static float scale = 7.10f;
	private final Gl2StarfieldShader starfieldshader;
	private final Gl2Model starfield;
	private Matrix4 ident = new Matrix4();

	public MGNebulaSky(Gl2Model nebulasky, Gl2Model stars, Gl2StarfieldShader starshader)
	{
		super(nebulasky, depth, scale);

		Matrix4.setIdentity(ident);
		starfield = stars;
		starfieldshader = starshader;
	}

	public void updateStarfield(float intensity, float frequency, float phase){
		// Update the intensity
		starfieldshader.updateIntensity(intensity);
		starfieldshader.updateFrequency(frequency);
		starfieldshader.updatePhase(phase);

		// Update our star field texture
		starfield.drawPoints(ident, ident);
	}
	
	@Override
	public boolean hitTest(float vx, float vy) {
		return false;
	}

	@Override
	public boolean update(float time, float special_scale) {
		return false;
	}
}
