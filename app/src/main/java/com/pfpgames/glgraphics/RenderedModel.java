package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.Vector4;

public interface RenderedModel {
	void drawTriangles(Matrix4 proj, Matrix4 view);
	void drawPoints(Matrix4 proj, Matrix4 view);
	boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view);
	boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view);
	void setDefaultColor(Vector4 color);
	Vector4 getDefaultColor();
	Vector4 getMaxBounds();
	Vector4 getMinBounds();
}