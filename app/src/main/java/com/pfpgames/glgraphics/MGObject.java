package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;

public interface MGObject {
	void draw(Matrix4 projection);
	boolean update(float time, float special_scale);
	void activate(float time);
	void deactivate(float time);
	boolean isActive();
	void setOffset(float x, float y);
	void setDepth(float z);
	void setScale(float scale);
	float getX();
	float getY();
	float getDepth();
	float getScale();
}