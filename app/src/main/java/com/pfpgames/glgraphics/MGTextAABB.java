package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.Vector3;
import com.pfpgames.glmath.Vector4;

import java.util.Iterator;

public class MGTextAABB extends MGText {

	// Store the min and max bounds for the entire string
	private float minX = 0.0f;
	private float maxX = 0.0f;
	private float minY = 0.0f;
	private float maxY = 0.0f;
	private float minZ = 0.0f;
	private float maxZ = 0.0f;

	public MGTextAABB(RenderedModel[] textList, float[] pitchList, String text, float x, float y, float z, float size){
		super(textList, pitchList, text, x, y, z, size);
	}

	@Override
	public boolean update(float time, float phase_scale) {
		return false;
	}

	public void setColor(final Vector4 color) {
		text_color = color;
	}

	public boolean hitTest(float vx, float vy) {
		computeBounds();
		return hitTestAABB(vx, vy);
	}

	private void computeBounds(){
		Iterator<MGCharacter> iter = chars.iterator();

		// Seed our max and min if we have at least one character
		if( iter.hasNext() ){
			MGCharacter my_char = iter.next();
			Vector4 maxb = my_char.getMaxBounds();
			Vector4 minb = my_char.getMinBounds();
			maxX = maxb.x();
			maxY = maxb.y();
			maxZ = maxb.z();
			minX = minb.x();
			minY = minb.y();
			minZ = minb.z();

            while( iter.hasNext() ) {
                my_char = iter.next();
                maxb = my_char.getMaxBounds();
                minb = my_char.getMinBounds();
                if (maxb.x() > maxX) {
                    maxX = maxb.x();
                }
                if (maxb.y() > maxY) {
                    maxY = maxb.y();
                }
                if (maxb.z() > maxZ) {
                    maxZ = maxb.z();
                }
                if (minb.x() < minX) {
                    minX = minb.x();
                }
                if (minb.y() < minY) {
                    minY = minb.y();
                }
                if (minb.z() < minZ) {
                    minZ = minb.z();
                }
            }
		}
	}

	private boolean hitTestAABB(float vx, float vy) {
		final Vector4 nearPoint = new Vector4(vx, vy, -1, 1);
		final Vector4 farPoint = new Vector4(vx, vy, 1, 1);

		Vector3 ulBounds = new Vector3();
		Vector3 lrBounds = new Vector3();
		Vector3 urBounds = new Vector3();
		Vector3 llBounds = new Vector3();

		final Matrix4 invProj = Matrix4.invert(curProj);

		Vector4 nearLocal = Vector4.Multiply(invProj, nearPoint);
		nearLocal.divideThis(nearLocal.vector[3]);
		Vector4 farLocal = Vector4.Multiply(invProj, farPoint);
		farLocal.divideThis(farLocal.vector[3]);

		// Create a 3D line from our positions
		Vector3 direction = new Vector3(Vector4.Sub(farLocal, nearLocal));
		// Normalize the line's directional vector
		direction.Normalize();
		final Vector3 linePoint = new Vector3(nearLocal);

		// Transform the bounding box into our view
		final Vector4 localMin = new Vector4(minX, minY, minZ, 1);
		final Vector4 localMax = new Vector4(maxX, maxY, maxZ, 1);
		final float wMinX = localMin.vector[0];
		final float wMinY = localMin.vector[1];
		final float wMinZ = localMin.vector[2];
		final float wMaxX = localMax.vector[0];
		final float wMaxY = localMax.vector[1];
		final float wMaxZ = localMax.vector[2];

		// Create 6 faces from the min and max coordinates and test each one for collision.
		boolean intersect = false;

		// Z stays constant (front face)
		// (wMinX != wMaxX && wMinY != wMaxY) protects against plane line intersections.
		// Avoids the case where the X or Y values match which makes a plane
		// as our intersection test will always return true. (repeat for the rest of the faces)
		if( wMinX != wMaxX && wMinY != wMaxY ) {
			ulBounds.setThis(wMinX, wMaxY, wMinZ);
			urBounds.setThis(wMaxX, wMaxY, wMinZ);
			lrBounds.setThis(wMaxX, wMinY, wMinZ);
			llBounds.setThis(wMinX, wMinY, wMinZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint);
		}

		// Z stays constant (back face)
		if( !intersect && wMinX != wMaxX && wMinY != wMaxY ){
			ulBounds.setThis(wMinX, wMaxY, wMaxZ);
			urBounds.setThis(wMaxX, wMaxY, wMaxZ);
			lrBounds.setThis(wMaxX, wMinY, wMaxZ);
			llBounds.setThis(wMinX, wMinY, wMaxZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint );
		}

		// Y stays constant (bottom face)
		if( !intersect && wMinZ != wMaxZ && wMinX != wMaxX){
			ulBounds.setThis(wMinX, wMinY, wMinZ);
			urBounds.setThis(wMaxX, wMinY, wMaxZ);
			lrBounds.setThis(wMaxX, wMinY, wMinZ);
			llBounds.setThis(wMinX, wMinY, wMaxZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint );
		}

		// Y stays constant (top face)
		if( !intersect && wMinZ != wMaxZ && wMinX != wMaxX ){
			ulBounds.setThis(wMinX, wMaxY, wMinZ);
			urBounds.setThis(wMaxX, wMaxY, wMaxZ);
			lrBounds.setThis(wMaxX, wMaxY, wMinZ);
			llBounds.setThis(wMinX, wMaxY, wMaxZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint );
		}

		// X stays constant (left face)
		if( !intersect && wMinZ != wMaxZ && wMinY != wMaxY ){
			ulBounds.setThis(wMinX, wMaxY, wMinZ);
			urBounds.setThis(wMinX, wMaxY, wMaxZ);
			lrBounds.setThis(wMinX, wMinY, wMaxZ);
			llBounds.setThis(wMinX, wMinY, wMinZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint );
		}

		// X stays constant (right face)
		if( !intersect && wMinZ != wMaxZ && wMinY != wMaxY ){
			ulBounds.setThis(wMaxX, wMaxY, wMinZ);
			urBounds.setThis(wMaxX, wMaxY, wMaxZ);
			lrBounds.setThis(wMaxX, wMinY, wMinZ);
			llBounds.setThis(wMaxX, wMinY, wMaxZ);
			intersect = boxLineIntersect(
					ulBounds,
					urBounds,
					lrBounds,
					llBounds,
					direction,
					linePoint );
		}

		return intersect;
	}

	private boolean boxLineIntersect(
			Vector3 ulBounds,
			Vector3 urBounds,
			Vector3 lrBounds,
			Vector3 llBounds,
			Vector3 dir,
			Vector3 orig)
	{
		boolean hit = triangleLineIntersectGeometric(orig, dir, ulBounds, urBounds, lrBounds);
		hit |= !hit && triangleLineIntersectGeometric(orig, dir, ulBounds, lrBounds, llBounds);

		return hit;
	}

	private boolean triangleLineIntersectGeometric(
			Vector3 origin, Vector3 direction,
			Vector3 v0, Vector3 v1, Vector3 v2) {
		Vector3 v0v1 = Vector3.Sub(v1, v0);
		Vector3 v0v2 = Vector3.Sub(v2, v0);
		Vector3 N = Vector3.Cross(v0v1, v0v2);
		N.Normalize();
		float nDotRay = Vector3.Dot(N, direction);
		if (Vector3.Dot(N, direction) == 0) return false; // ray parallel to triangle
		float d = Vector3.Dot(N, v0);
		float t = -(Vector3.Dot(N, origin) - d) / nDotRay;

		// inside-out test
		Vector3 Phit = new Vector3();
		Phit.vector[0] = origin.vector[0] + direction.vector[0] * t;
		Phit.vector[1] = origin.vector[1] + direction.vector[1] * t;
		Phit.vector[2] = origin.vector[2] + direction.vector[2] * t;

		// inside-out test edge0
		Vector3 v0p = Vector3.Sub(Phit, v0);
		float v = Vector3.Dot(N, Vector3.Cross(v0v1, v0p));
		if (v < 0) return false; // P outside triangle

		// inside-out test edge1
		Vector3 v1p = Vector3.Sub(Phit, v1);
		Vector3 v1v2 = Vector3.Sub(v2, v1);
		float w = Vector3.Dot(N, Vector3.Cross(v1v2, v1p));
		if (w < 0) return false; // P outside triangle

		// inside-out test edge2
		Vector3 v2p = Vector3.Sub(Phit, v2);
		Vector3 v2v0 = Vector3.Sub(v0, v2);
		float u = Vector3.Dot(N,  Vector3.Cross(v2v0, v2p));
		if (u < 0) return false; // P outside triangle

		return true;
	}
}
