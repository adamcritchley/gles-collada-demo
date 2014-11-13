package com.acritchley.glmath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DirectMatrix4f {

	private static final int MATRIX_DIM = 4;
	private FloatBuffer matrix = null;

	public DirectMatrix4f(){
		ByteBuffer b = ByteBuffer.allocateDirect(MATRIX_DIM*MATRIX_DIM*4);
		b.order(ByteOrder.nativeOrder());
		matrix = b.asFloatBuffer();

	}

	public void set( Matrix4 m ){
		matrix.put(m.matrix);
		matrix.position(0);
	}

	public FloatBuffer getBuffer(){
		return matrix;
	}
}
