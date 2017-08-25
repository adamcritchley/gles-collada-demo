package com.pfpgames.glcolladademo;

import java.nio.FloatBuffer;

import com.pfpgames.collada.ColladaShader;
import com.pfpgames.glgraphics.gl2es.Gl2Mesh;
import com.pfpgames.glgraphics.gl2es.Gl2Model;
import com.pfpgames.glmath.Matrix4;

import android.content.res.AssetManager;
import android.opengl.GLES20;

public class Gl2NebulaShader implements ColladaShader{

	private final int STARFIELD_WIDTH = 512;
	private final int STARFIELD_HEIGHT = 512;
	private int starFieldHandle = 0;
	private int starFieldLocation;

	public Gl2NebulaShader(Gl2Model starfield) {
		starfield.renderOffscreen(STARFIELD_WIDTH, STARFIELD_HEIGHT);
		starFieldHandle = starfield.getOffscreenTexture();
	}

	@Override
	public void begin(Matrix4 worldModelView, Matrix4 proj, int numVertices, int numNormals, int numTexCoords){
		GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, starFieldHandle );
		GLES20.glUniform1f( starFieldLocation, 0 );
	}

	@Override
	public void end(){
		GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, 0 );
	}
	
	@Override
	public int buildShader(AssetManager assets, String texName, String mvpMatrixName){
		String vertexShaderSrc =
				"#version 100\n" +
				Gl2Mesh.readFile(assets, "nebulashader.vert");

		String fragmentShaderSrc =
			"#version 100\n" +
			Gl2Mesh.readFile(assets, "nebulashader.frag");

		int vertexShader = Gl2Mesh.loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderSrc );
		int fragmentShader = Gl2Mesh.loadShader( GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc );			
		int shaderProgram = Gl2Mesh.createProgram( vertexShader, fragmentShader );

		starFieldLocation = GLES20.glGetUniformLocation(shaderProgram, "starField" );
		
		return shaderProgram;
	}
}
