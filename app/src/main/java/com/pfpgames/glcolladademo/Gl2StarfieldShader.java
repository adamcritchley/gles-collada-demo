package com.pfpgames.glcolladademo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.pfpgames.collada.ColladaShader;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glgraphics.gl2es.Gl2Mesh;

public class Gl2StarfieldShader implements ColladaShader{
	private final int STAR_WIDTH = 8;
	private final int STAR_HEIGHT = 8;
	private int starTexLocation;
	private int starTexHandle;
	private int intensityLocation;
	private float starsIntensity = 20.0f;
	private float starsPhase = 0.0f;
	private float starsFrequency = 0.0f;
	private int phaseLocation;
	private int frequencyLocation;

	@Override
	public int buildShader(AssetManager assets, String texName, String mvpMatrixName) {

		String vertexStarShaderSrc = "#version 100\n" + Gl2Mesh.readFile(assets, "starshader.vert");
		String fragmentStarShaderSrc = "#version 100\n" + Gl2Mesh.readFile(assets, "starshader.frag");
		
		int vertexShader = Gl2Mesh.loadShader( GLES20.GL_VERTEX_SHADER, vertexStarShaderSrc );
		int fragmentShader = Gl2Mesh.loadShader( GLES20.GL_FRAGMENT_SHADER, fragmentStarShaderSrc );			
		int starShaderProgram = Gl2Mesh.createProgram( vertexShader, fragmentShader );
		
		starTexLocation = GLES20.glGetUniformLocation(starShaderProgram, "starTexture" );
		intensityLocation = GLES20.glGetUniformLocation(starShaderProgram, "fIntensity" );
		phaseLocation = GLES20.glGetUniformLocation(starShaderProgram, "fPhase" );
		frequencyLocation = GLES20.glGetUniformLocation(starShaderProgram, "fFrequency" );
		
		buildStarTexture();

		return starShaderProgram;
	}

	@Override
	public void begin(Matrix4 worldModelView, Matrix4 proj, int numVertices, int numNormals, int numTexCoords) {

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_DST_COLOR);

		GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, starTexHandle );
		GLES20.glUniform1f( starTexLocation, 0 );

		GLES20.glUniform1f(intensityLocation, starsIntensity);
		GLES20.glUniform1f(phaseLocation, starsPhase);
		GLES20.glUniform1f(frequencyLocation, starsFrequency);
	}

	@Override
	public void end() {
		GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, 0 );
		
		GLES20.glDisable(GLES20.GL_BLEND);
	}

	private void buildStarTexture() {

		Bitmap bitmap = Bitmap.createBitmap(STAR_WIDTH, STAR_HEIGHT, Bitmap.Config.ARGB_8888);

		double shade_min = 0;
		double shade_max = 0;
		double gaussian_mean = STAR_WIDTH/2.0;
		double gaussian_sigma = 1.6;
		double[][] gaussian = new double[STAR_WIDTH][STAR_HEIGHT];
		for(int y = 0; y < STAR_HEIGHT; y++)
		{
			for(int x = 0; x < STAR_WIDTH; x++)
			{
				double x_term = (x-gaussian_mean)*(x-gaussian_mean);
				double y_term = (y-gaussian_mean)*(y-gaussian_mean);
				double shade = Math.exp(-(x_term + y_term) / (2.0 * gaussian_sigma * gaussian_sigma));
				gaussian[x][y] = shade;
				if( x == 0 && y == 0 ){
					shade_min = shade;
					shade_max = shade;
				}
				shade_min = Math.min(shade_min, shade);
				shade_max = Math.max(shade_max, shade);
			}
		}

		for(int y = 0; y < STAR_HEIGHT; y++)
		{
			for(int x = 0; x < STAR_WIDTH; x++)
			{
				double shade = (gaussian[x][y] - shade_min) / (shade_max - shade_min);
				int brightness = (int)Math.max(0.0, Math.min(255.0, shade * 255.0));
				int color = android.graphics.Color.argb(brightness, brightness, brightness, brightness);
				bitmap.setPixel(x, y, color);
			}
		}

		// Generate one texture pointer...
		starTexHandle = Gl2Mesh.glGenBuffer();
		// ...and bind it to our array
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, starTexHandle);

		// Create Nearest Filtered Texture
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Clean up
		bitmap.recycle();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public void updateIntensity(float intensity) {
		starsIntensity = intensity;
	}
	
	public void updatePhase(float phase) {
		starsPhase = phase;
	}

	public void updateFrequency(float frequency) {
		starsFrequency = frequency;
	}
}
