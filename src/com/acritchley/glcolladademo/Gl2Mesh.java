package com.acritchley.glcolladademo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.acritchley.collada.ColladaFxSampler;
import com.acritchley.collada.ColladaFxSurface;
import com.acritchley.collada.ColladaMesh;
import com.acritchley.collada.ColladaParamEvaluator;
import com.acritchley.collada.ColladaProfileCommon;
import com.acritchley.collada.ColladaProfileEvaluator;
import com.acritchley.collada.ColladaTechniqueFxEvaluator;
import com.acritchley.collada.ColladaTechniqueFxPhong;
import com.acritchley.glmath.DirectMatrix;
import com.acritchley.glmath.Matrix4;
import com.acritchley.glmath.MatrixStack;

public class Gl2Mesh implements ColladaMesh {
	private String meshId = null;
    private FloatBuffer mVertexBuffer = null;
    private FloatBuffer mNormalBuffer = null;
    private FloatBuffer mTexCoordsBuffer = null;
    private IntBuffer mIndexBuffer = null;
    private int vboVertexHandle = -1;
	private int vboNormalsHandle = -1;
	private int vboTexCoordsHandle = -1;
    private int vboIndexHandle = -1;
	private int textureHandle = -1;
    private boolean useNormals = false;
    private boolean useTexCoords = false;
    private MatrixStack worldViewStack = new MatrixStack();
	
	// Variables for shader program
	private String texName = "texDefault";
	private int texLocation;
	private int modelViewLocation;
	private int shaderProgram;
	private Gl2Model gl2Model;
	private Matrix4 worldViewMatrix;
	private DirectMatrix worldViewMatrixBuffer = new DirectMatrix();

    public Gl2Mesh(Gl2Model model) {
    	gl2Model = model;
	}
    
    public int size(){
    	return mIndexBuffer.capacity();
    }
    
    public ColladaProfileEvaluator createProfileEvaluator(){
    	return new GLES20ProfileEvaluator();
    }
 
    public void createModel(){

    	vboVertexHandle = glGenBuffer();

    	GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboVertexHandle );
    	GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, mVertexBuffer.capacity()*4, mVertexBuffer, GLES20.GL_STATIC_DRAW  );
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );

		if( useNormals ){
	    	vboNormalsHandle = glGenBuffer();
		
	    	GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboNormalsHandle );
	    	GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, mNormalBuffer.capacity()*4, mNormalBuffer, GLES20.GL_STATIC_DRAW  );
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );
		}

		if( useTexCoords ){
			vboTexCoordsHandle = glGenBuffer();
						
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboTexCoordsHandle );
			GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER, mTexCoordsBuffer.capacity()*4, mTexCoordsBuffer, GLES20.GL_STATIC_DRAW  );
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );
		}

		vboIndexHandle = glGenBuffer();
    			
		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		GLES20.glBufferData( GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity()*4, mIndexBuffer, GLES20.GL_STATIC_DRAW );
		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, 0 );
		
		worldViewMatrix = worldViewStack.collapse();
    }

	public void createShader() {
		String vertexShaderSrc =
			"attribute vec4 aPosition;                     \n" +
			"attribute vec3 aNormal;                       \n" +
			"attribute vec2 aTexCoord;                     \n" +
			"varying vec2 vTexCoord;                       \n" +
			"uniform mat4 modelViewMatrix;                 \n" +
			"void main()                                   \n" +
			"{                                             \n" +
			"   gl_Position = modelViewMatrix * aPosition; \n" +
//			"   gl_Normal = aNormal;                       \n" +
			"   vTexCoord = aTexCoord;                     \n" +
			"}                                             \n";

		String fragmentShaderSrc = "";

		if( texName.equalsIgnoreCase("texDefault")){
			fragmentShaderSrc = "precision mediump float;           \n" +
			   "void main()                                         \n" +
			   "{                                                   \n" +
			   "  gl_FragColor = vec4 ( 0.0, 1.0, 0.0, 1.0 );       \n" +
			   "}                                                   \n";
		}else{
			fragmentShaderSrc = "precision mediump float;           \n" +
			   "varying vec2 vTexCoord;                             \n" +
			   "uniform sampler2D "+texName+";                      \n" +
			   "void main()                                         \n" +
			   "{                                                   \n" +
			   "  gl_FragColor = texture2D("+texName+", vTexCoord); \n" +
			   "}                                                   \n";
		}

		int vertexShader = loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderSrc );
		int fragmentShader = loadShader( GLES20.GL_FRAGMENT_SHADER, fragmentShaderSrc );			
		shaderProgram = createProgram( vertexShader, fragmentShader );

		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, texName );
	}

    public void draw(){
    	MatrixStack worldModelView = new MatrixStack();
		worldModelView.add(gl2Model.getModelViewMatrix());
		worldModelView.add(worldViewMatrix);
		worldViewMatrixBuffer.set( worldModelView.collapse() );

		GLES20.glUseProgram ( shaderProgram );

		GLES20.glUniformMatrix4fv( modelViewLocation, 1, false, worldViewMatrixBuffer.getBuffer() );

		GLES20.glEnableVertexAttribArray( 0 );
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboVertexHandle );
		GLES20.glVertexAttribPointer ( 0, 3, GLES20.GL_FLOAT, false, 0, 0 );
		
		if( useNormals ){
			GLES20.glEnableVertexAttribArray( 1 );
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboNormalsHandle );
			GLES20.glVertexAttribPointer ( 1, 3, GLES20.GL_FLOAT, false, 0, 0 );
		}
		
		if( useTexCoords ){
		    GLES20.glEnableVertexAttribArray( 2 );
			GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboTexCoordsHandle );
			GLES20.glVertexAttribPointer ( 2, 2, GLES20.GL_FLOAT, false, 0, 0 );

			GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
			GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureHandle );
			GLES20.glUniform1f( texLocation, 0 );
		}

		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		GLES20.glDrawElements( GLES20.GL_TRIANGLES, mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_INT, 0 );
   	}

	@Override
	public void setIndices(int[] indices) {
        mIndexBuffer = IntBuffer.wrap( indices );
        mIndexBuffer.position(0);
	}

	@Override
	public void setVertices(float[] verts) {
    	mVertexBuffer = FloatBuffer.wrap( verts );
    	mVertexBuffer.position(0);
	}

	@Override
	public void setNormals(float[] normals) {
		mNormalBuffer = FloatBuffer.wrap( normals );
		mNormalBuffer.position(0);
		useNormals = true;
	}

	@Override
	public void setTexCoords(float[] coords) {
		mTexCoordsBuffer = FloatBuffer.wrap( coords );
		mTexCoordsBuffer.position(0);
		useTexCoords = true;
	}

	@Override
	public void setId(String id) {
		meshId = id;
	}

	@Override
	public String getId() {
		return meshId;
	}
	
	private interface ProfileEvaluation{
		public ColladaParamEvaluator paramEval();
		public ColladaTechniqueFxEvaluator techEval();
	}
	
	private class GLES20ProfileEvaluator implements ColladaProfileEvaluator{
		private ProfileEvaluation eval = null;

		@Override
		public void evaluateProfileCommon(ColladaProfileCommon prof) throws Exception {
			eval = new CommonEvaluation();
		}

		@Override
		public void evaluateProfileCg(ColladaProfileCommon prof) throws Exception {
			throw new Exception("Cg Not Supported");
		}

		@Override
		public void evaluateProfileGLES(ColladaProfileCommon prof) throws Exception {
			throw new Exception("GLES Not Supported");
		}

		@Override
		public void evaluateProfileGLSL(ColladaProfileCommon prof) throws Exception {
			throw new Exception("GLSL Not Supported");
		}

		@Override
		public ColladaParamEvaluator createParamEvaluator() {
			return eval.paramEval();
		}

		@Override
		public ColladaTechniqueFxEvaluator createTechniqueEvaluator() {
			return eval.techEval();
		}
		
		private class CommonEvaluation implements ProfileEvaluation{

			@Override
			public ColladaTechniqueFxEvaluator techEval() {
				return new CommonTechniqueEvaluator();
			}

			@Override
			public ColladaParamEvaluator paramEval() {
				return new CommonParamEvaluator();
			}

			private class CommonTechniqueEvaluator implements ColladaTechniqueFxEvaluator{

				@Override
				public void evaluateTechnique(ColladaTechniqueFxPhong tech) {
					// setup properties of the shader
				}
			}

			private class CommonParamEvaluator implements ColladaParamEvaluator{

				@Override
				public void evaluateParam(ColladaFxSampler tech) {
					// assign it to a shader location
					texName = tech.getSurface().getImage().getName();
				}

				@Override
				public void evaluateParam(ColladaFxSurface tech) {
					// load the image
					textureHandle = loadGLTexture(tech.getImage().getSource());
				}
			}
		}
	}

	// Helper methods
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
		GLES20.glBindAttribLocation( program, 1, "aNormal" );
		GLES20.glBindAttribLocation( program, 2, "aTexCoord" );
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
	
	public int loadGLTexture(String assetName) {
		// Get the texture from the Android resource directory
		InputStream is = null;
		
		try {
			is = gl2Model.getAssetManager().open(assetName);
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

	@Override
	public void rotate(float x, float y, float z, float a) {
		worldViewStack.add( Matrix4.rotate(a, x, y, z) );
	}

	@Override
	public void translate(float x, float y, float z) {
		worldViewStack.add( Matrix4.translate(x, y, z) );
	}

	@Override
	public void scale(float x, float y, float z) {
		worldViewStack.add( Matrix4.scale(x, y, z) );
	}
}
