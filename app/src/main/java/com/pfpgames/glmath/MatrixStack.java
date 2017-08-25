package com.pfpgames.glmath;

import java.util.Stack;

public class MatrixStack extends Stack<Matrix4> {

	private static final long serialVersionUID = 6790770286780743223L;

	public Matrix4 collapse(){
		Matrix4 result = null;

		if( this.elementCount == 0 ){
			result = new Matrix4();
			Matrix4.setIdentity(result);
		}else{
			result = (Matrix4) this.elementData[0];
			if( this.elementCount > 1 ){
				for(int i=1; i<this.elementCount;i++){
					result = Matrix4.multiply(result, (Matrix4) this.elementData[i]);
				}
			}
		}

		return result;
	}
}
