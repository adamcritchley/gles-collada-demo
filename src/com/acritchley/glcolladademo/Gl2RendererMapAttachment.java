package com.acritchley.glcolladademo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.acritchley.glfonts.FontMapping;
import com.acritchley.glmath.DirectMatrix;
import com.acritchley.glmath.Matrix4;

public class Gl2RendererMapAttachment implements FontMapping.RendererMapAttachment {
    private FloatBuffer mVertexBuffer = null;
    private IntBuffer mIndexBuffer = null;
    private int vboVertexHandle = -1;
    private int vboIndexHandle = -1;
	private int textureHandle = -1;

	private int texLocation;
	private int modelViewLocation;
	private int shaderProgram;
	private AssetManager assetMgr;
	private FontMapping charMap;
	private DirectMatrix worldViewMatrixBuffer = new DirectMatrix();

	private float vertices[] = {
			1, 1, -1, 
			1, -1, -1, 
			-1, -1, -1, 
			1, 1, -1, 
			-1, -1, -1, 
			-1, 1, -1, 
			1, 1, 1, 
			-1, 1, 1, 
			-1, -1, 1,
			1, 1, 1, 
			-1, -1, 1,
			1, -1, 1,
			1, 1, -1,
			1, 1, 1,
			1, -1, 1,
			1, 1, -1, 
			1, -1, 1,
			1, -1, -1, 
			1, -1, -1, 
			1, -1, 1, 
			-1, -1, 1, 
			1, -1, -1, 
			-1, -1, 1, 
			-1, -1, -1,
			-1, -1, -1,
			-1, -1, 1,
			-1, 1, 1,
			-1, -1, -1, 
			-1, 1, 1, 
			-1, 1, -1,
			1, 1, 1,
			1, 1, -1, 
			-1, 1, -1,
			1, 1, 1,
			-1, 1, -1, 
			-1, 1, 1 
											};

	private int[] indices = {
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
			12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
			32, 33, 34, 35 
												};
	
	public Gl2RendererMapAttachment(AssetManager mgr, FontMapping cmap){
		this.assetMgr = mgr;
		this.charMap = cmap;
		
		this.createModel(this.charMap.getFileName());
		this.createShader();
	}

	@Override
	public FontMapping.RendererCharacterAttachment createCharacterAttachment(FontMapping.FontCharacter fchar) {
		Gl2RendererCharacterAttachment renderedChar = new Gl2RendererCharacterAttachment(fchar);
		return renderedChar;
	}


	public void draw(float cx, float cy, float cz, float size, String text) {
		Matrix4 scaleView = Matrix4.scale(size, size, size);
		Matrix4 localView = Matrix4.translate(cx, cy, cz);
		localView.rotateThis(180, 0, 1, 0);

		GLES20.glUseProgram ( shaderProgram );

		for( char code : text.toCharArray() ){
			FontMapping.FontCharacter fchar = this.charMap.getCharacterByCode((int)code);
			
			if( fchar != null ){
				Gl2RendererCharacterAttachment rchar = (Gl2RendererCharacterAttachment)fchar.getAttachment();

				worldViewMatrixBuffer.set( Matrix4.multiply(localView, scaleView) );

				GLES20.glUniformMatrix4fv( modelViewLocation, 1, false, worldViewMatrixBuffer.getBuffer() );

				GLES20.glEnableVertexAttribArray( 0 );
				GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboVertexHandle );
				GLES20.glVertexAttribPointer ( 0, 3, GLES20.GL_FLOAT, false, 0, 0 );

			    GLES20.glEnableVertexAttribArray( 1 );
				GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, rchar.getTexVbo() );
				GLES20.glVertexAttribPointer ( 1, 2, GLES20.GL_FLOAT, false, 0, 0 );

				GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
				GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureHandle );
				GLES20.glUniform1f( texLocation, 0 );

				GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
				GLES20.glDrawElements( GLES20.GL_TRIANGLES, mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_INT, 0 );

				// Translate the drawing origin by the size of the letter.
				localView.translateThis(-size*2, 0, 0);
			}
		}
	}

	private void createModel(String assetName){
		mVertexBuffer = FloatBuffer.wrap(vertices);
		mVertexBuffer.position(0);
		mIndexBuffer = IntBuffer.wrap(indices);
		mIndexBuffer.position(0);

		vboVertexHandle = glGenBuffer();

    	GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboVertexHandle );
    	GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, mVertexBuffer.capacity()*4, mVertexBuffer, GLES20.GL_STATIC_DRAW  );
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );

		vboIndexHandle = glGenBuffer();
    			
		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		GLES20.glBufferData( GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity()*4, mIndexBuffer, GLES20.GL_STATIC_DRAW );
		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, 0 );

		textureHandle = loadGLTexture(assetName);
    }

	private void createShader() {
		String vertexShaderSrc =
			"attribute vec4 aPosition;                     \n" +
			"attribute vec2 aTexCoord;                     \n" +
			"varying vec2 vTexCoord;                       \n" +
			"uniform mat4 modelViewMatrix;                 \n" +
			"void main()                                   \n" +
			"{                                             \n" +
			"   gl_Position = modelViewMatrix * aPosition; \n" +
			"   vTexCoord = aTexCoord;                     \n" +
			"}                                             \n";

		String fragmentShaderSrc = "precision mediump float;    \n" +
		   "varying vec2 vTexCoord;                             \n" +
		   "uniform sampler2D texFont;                          \n" +
		   "void main()                                         \n" +
		   "{                                                   \n" +
		   "  gl_FragColor = texture2D(texFont, vTexCoord);     \n" +
		   "}                                                   \n";

		int vertexShader = loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderSrc );
		int fragmentShader = loadShader( GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc );			
		shaderProgram = createProgram( vertexShader, fragmentShader );

		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, "texFont" );
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

	private int loadShader( int type, String source )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		int shader = GLES20.glCreateShader( type );
		if( shader == 0 )
			throw new RuntimeException( "creating the shader didn't work" );
		GLES20.glShaderSource( shader, source );
		GLES20.glCompileShader( shader );
		GLES20.glGetShaderiv( shader, GLES20.GL_COMPILE_STATUS, intbuf );
		int compiled = intbuf.get(0);
		if( compiled == 0 )
		{					
			GLES20.glGetShaderiv( shader, GLES20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )
			{
				String infoLog = GLES20.glGetShaderInfoLog( shader );
				Log.d( "GLES20", "shader info: " + infoLog );
			}
			throw new RuntimeException( "creating the shader didn't work" );
		}

		return shader;
	}
	
	private int createProgram( int vertexShader, int fragmentShader )
	{
		int program = GLES20.glCreateProgram();
		if( program == 0 )
			throw new RuntimeException( "creating program didn't work" );
		
		GLES20.glAttachShader( program, vertexShader );
		GLES20.glAttachShader( program, fragmentShader );

		GLES20.glBindAttribLocation( program, 0, "aPosition" );
		GLES20.glBindAttribLocation( program, 1, "aTexCoord" );
		GLES20.glLinkProgram( program );

		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intbuf = tmp.asIntBuffer();
		
		GLES20.glGetProgramiv( program, GLES20.GL_LINK_STATUS, intbuf );
		int linked = intbuf.get(0);
		if( linked == 0 )
		{
			GLES20.glGetProgramiv( program, GLES20.GL_INFO_LOG_LENGTH, intbuf );
			int infoLogLength = intbuf.get(0);
			if( infoLogLength > 1 )
			{
				Log.d( "GLES20", "couldn't link program: " + GLES20.glGetProgramInfoLog( program ) );					
			}
			
			throw new RuntimeException( "Creating program didn't work" );
		}

		return program;
	}
	
	private int loadGLTexture(String assetName) {
		// Get the texture from the Android resource directory
		InputStream is = null;
		
		try {
			is = this.assetMgr.open(assetName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Bitmap bitmap = null;

		try {
			// BitmapFactory is an Android graphics utility for images
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			// Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Generate one texture pointer...
		int tex = glGenBuffer();
		// ...and bind it to our array
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);

		// Create Nearest Filtered Texture
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

		// Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Clean up
		bitmap.recycle();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		return tex;
	}
}
