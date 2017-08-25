package com.pfpgames.glgraphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;

import com.pfpgames.glfonts.FontMapping;

public class Gl2RendererCharacterAttachment implements FontMapping.RendererCharacterAttachment {
    private FloatBuffer mTexCoordsBuffer = null;
	private int vboTexCoordsHandle = -1;

	public Gl2RendererCharacterAttachment(FontMapping.FontCharacter character) {

    	float texture[] = {
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight(),
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight(),
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight(),
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight(),
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(),
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight(),
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()-character.getHeight(), 
    			character.getCenterX()+character.getWidth(), character.getCenterY()+character.getHeight(), 
    			character.getCenterX()-character.getWidth(), character.getCenterY()+character.getHeight()
			};

    	mTexCoordsBuffer = FloatBuffer.wrap(texture);
    	mTexCoordsBuffer.position(0);

		vboTexCoordsHandle = glGenBuffer();
		
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboTexCoordsHandle );
		GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, mTexCoordsBuffer.capacity()*4, mTexCoordsBuffer, GLES20.GL_STATIC_DRAW  );
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );
	}

	public int getTexVbo() {
		return vboTexCoordsHandle;
	}

    private int glGenBuffer()
    {
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order(ByteOrder.nativeOrder());			
		IntBuffer intbuf = tmp.asIntBuffer();
		intbuf.position(0);
		GLES20.glGenBuffers( 1, intbuf );
		return intbuf.get(0);
    }
}
