package com.pfpgames.glmath;

import android.opengl.Matrix;

public class Matrix4 {
	private static final int MATRIX_DIM = 4;
	public float[] matrix = new float[MATRIX_DIM * MATRIX_DIM];

	public Matrix4() {
	}

	public void scaleThis(float x, float y, float z) {
		Matrix.scaleM(this.matrix, 0, x, y, z);
	}

	public void rotateThis(float degrees, float x, float y, float z) {
		Matrix.rotateM(this.matrix, 0, degrees, x, y, z);
	}

	public void translateThis(float x, float y, float z) {
		Matrix.translateM(this.matrix, 0, x, y, z);
	}

	public static void setIdentity(Matrix4 m) {
		Matrix.setIdentityM(m.matrix, 0);
	}

	public static Matrix4 add(Matrix4 lhs, Matrix4 rhs) {
		Matrix4 result = new Matrix4();

		for (int n = 0; n < MATRIX_DIM; n++) {
			for (int m = 0; m < MATRIX_DIM; m++) {
				result.matrix[n + (m * MATRIX_DIM)] =
						lhs.matrix[n + (m * MATRIX_DIM)] +
								rhs.matrix[n + (m * MATRIX_DIM)];
			}
		}

		return result;
	}

	public static Matrix4 subtract(Matrix4 lhs, Matrix4 rhs) {
		Matrix4 result = new Matrix4();

		for (int n = 0; n < MATRIX_DIM; n++) {
			for (int m = 0; m < MATRIX_DIM; m++) {
				result.matrix[n + (m * MATRIX_DIM)] =
						lhs.matrix[n + (m * MATRIX_DIM)] -
								rhs.matrix[n + (m * MATRIX_DIM)];
			}
		}

		return result;
	}

	public static Matrix4 multiply(float a, Matrix4 b) {
		Matrix4 result = new Matrix4();

		for (int n = 0; n < MATRIX_DIM; n++) {
			for (int m = 0; m < MATRIX_DIM; m++) {
				result.matrix[n + (m * MATRIX_DIM)] =
						a * b.matrix[n + (m * MATRIX_DIM)];
			}
		}

		return result;
	}

	public static Matrix4 multiply(Matrix4 lhs, Matrix4 rhs) {
		Matrix4 result = new Matrix4();

		Matrix.multiplyMM(result.matrix, 0, lhs.matrix, 0, rhs.matrix, 0);

		return result;
	}

	public static Matrix4 rotate(float degrees, float x, float y, float z) {
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		if( degrees != 0.0f ) {
			Matrix.rotateM(result.matrix, 0, degrees, x, y, z);
		}

		return result;
	}

	public static Matrix4 translate(float x, float y, float z) {
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		Matrix.translateM(result.matrix, 0, x, y, z);

		return result;
	}

	public static Matrix4 scale(float x, float y, float z) {
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		Matrix.scaleM(result.matrix, 0, x, y, z);

		return result;
	}

	public static Matrix4 frustum(float left, float right, float bottom, float top, float near, float far) {
		Matrix4 result = new Matrix4();
		//Matrix4.setIdentity(result);

		Matrix.frustumM(result.matrix, 0, left, right, bottom, top, near, far);
/*
		result.matrix[0] = 2.0f*near/(right-left);
		result.matrix[2] = (right+left)/(right-left);
		result.matrix[5] = 2.0f*near/(top-bottom);
		result.matrix[6] = (top+bottom)/(top-bottom);
		result.matrix[10] = (-far-near)/(far-near);
		result.matrix[11] = -2.0f*far*near/(far-near);
		result.matrix[14] = -1.0f;
		result.matrix[15] = 0.0f;
*/
		return result;
	}

	public static Matrix4 orthographic(float left, float right, float bottom, float top, float near, float far) {
		Matrix4 result = new Matrix4();
		//Matrix4.setIdentity(result);

		Matrix.orthoM(result.matrix, 0, left, right, bottom, top, near, far);
/*
		result.matrix[0] = 2.0f/(right-left);
		result.matrix[3] = -((right+left)/(right-left));
		result.matrix[5] = 2.0f/(top-bottom);
		result.matrix[7] = -((top+bottom)/(top-bottom));
		result.matrix[10] = -2.0f/(far-near);
		result.matrix[11] = -((far+near)/(far-near));
*/

		return result;
	}

	public static Matrix4 invert(Matrix4 m) {
		Matrix4 result = new Matrix4();
		Matrix.invertM(result.matrix, 0, m.matrix, 0);
		return result;
	}

	public static Matrix4 identity() {
		Matrix4 result = new Matrix4();
		Matrix.setIdentityM(result.matrix, 0);
		return result;
	}
}
