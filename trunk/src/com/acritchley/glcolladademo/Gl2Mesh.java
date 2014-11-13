package com.acritchley.glcolladademo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.acritchley.collada.ColladaFxSampler;
import com.acritchley.collada.ColladaFxSurface;
import com.acritchley.collada.ColladaMatrix4f;
import com.acritchley.collada.ColladaMesh;
import com.acritchley.collada.ColladaParamEvaluator;
import com.acritchley.collada.ColladaProfileCommon;
import com.acritchley.collada.ColladaProfileEvaluator;
import com.acritchley.collada.ColladaShader;
import com.acritchley.collada.ColladaTechniqueFxEvaluator;
import com.acritchley.collada.ColladaTechniqueFxPhong;
import com.acritchley.glmath.DirectMatrix4f;
import com.acritchley.glmath.Matrix4;
import com.acritchley.glmath.MatrixStack;

public class Gl2Mesh implements ColladaMesh {
	private String meshId = null;
    private FloatBuffer mVertexBuffer = null;
    private FloatBuffer mNormalBuffer = null;
    private FloatBuffer mTexCoordsBuffer = null;
    private int numVertices = 0;
    private int numNormals = 0;
    private int numTexCoords = 0;
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
	private ColladaShader shaderObject;
	private AssetManager assetMgr;
	private Matrix4 worldViewMatrix;
	private DirectMatrix4f worldProjViewBuffer = new DirectMatrix4f();
	
	// Bounds used for hit testing
	private float minX; 
	private float maxX;
	private float minY; 
	private float maxY;
	private float minZ; 
	private float maxZ;

    public Gl2Mesh(AssetManager assetManager) {
    	assetMgr = assetManager;
	}
    
    public int size(){
    	return mIndexBuffer.capacity();
    }
    
    public ColladaProfileEvaluator createProfileEvaluator(){
    	return new GLES20ProfileEvaluator();
    }
    
    @Override
    public void buildMesh(){
    	shaderObject = null;
    	createModel();
    	shaderProgram = createDefaultShader();
		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, texName );
    }

    @Override
    public void buildMesh(ColladaShader shader){
    	shaderObject = shader;
    	createModel();
    	shaderProgram = shader.buildShader(assetMgr, texName, "modelViewMatrix");
		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, texName );
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

	public int createDefaultShader() {
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
		return createProgram( vertexShader, fragmentShader );
	}

    public void drawPoints(Matrix4 proj, Matrix4 view){
    	drawPrimitive(GLES20.GL_POINTS, proj, view);
    }

    public void drawTriangles(Matrix4 proj, Matrix4 view){
    	drawPrimitive(GLES20.GL_TRIANGLES, proj, view);
    }

    public void drawPrimitive(int drawType, Matrix4 proj, Matrix4 view){

    	Matrix4 worldModelView = Matrix4.multiply(view, worldViewMatrix);
		Matrix4 worldProjModelView = Matrix4.multiply(proj, worldModelView);
		worldProjViewBuffer.set( worldProjModelView );
   
		GLES20.glUseProgram ( shaderProgram );

		GLES20.glUniformMatrix4fv( modelViewLocation, 1, false, worldProjViewBuffer.getBuffer() );

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

    	if( shaderObject != null){
    		shaderObject.begin(worldModelView, proj, numVertices, numNormals, numTexCoords);
    	}

		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
		GLES20.glDrawElements( drawType, mIndexBuffer.capacity(), GLES20.GL_UNSIGNED_INT, 0 );

    	if( shaderObject != null){
    		shaderObject.end();
    	}

		if( useTexCoords ){
			GLES20.glDisableVertexAttribArray( 2 );
			GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, 0 );
		}

		if( useNormals ){
			GLES20.glDisableVertexAttribArray( 1 );
		}

		GLES20.glDisableVertexAttribArray( 0 );
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, 0 );
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
		numVertices = mVertexBuffer.capacity();
	}

	@Override
	public void setNormals(float[] normals) {
		mNormalBuffer = FloatBuffer.wrap( normals );
		mNormalBuffer.position(0);
		useNormals = true;
		numNormals = mNormalBuffer.capacity();
	}

	@Override
	public void setTexCoords(float[] coords) {
		mTexCoordsBuffer = FloatBuffer.wrap( coords );
		mTexCoordsBuffer.position(0);
		useTexCoords = true;
		numTexCoords = mTexCoordsBuffer.capacity();
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
    public static int glGenBuffer()
    {
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order(ByteOrder.nativeOrder());			
		IntBuffer intbuf = tmp.asIntBuffer();
		intbuf.position(0);
		GLES20.glGenBuffers( 1, intbuf );
		return intbuf.get(0);
    }
    
	public static int glGenFramebuffer()
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order(ByteOrder.nativeOrder());			
		IntBuffer intbuf = tmp.asIntBuffer();
		intbuf.position(0);
		GLES20.glGenFramebuffers( 1, intbuf );
		return intbuf.get(0);
	}

	public static int glGenRenderbuffer()
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order(ByteOrder.nativeOrder());			
		IntBuffer intbuf = tmp.asIntBuffer();
		intbuf.position(0);
		GLES20.glGenRenderbuffers( 1, intbuf );
		return intbuf.get(0);
	}

	public static int glGenTexture()
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order(ByteOrder.nativeOrder());			
		IntBuffer intbuf = tmp.asIntBuffer();
		intbuf.position(0);
		GLES20.glGenTextures( 1, intbuf );
		return intbuf.get(0);
	}

    public static String readFile(AssetManager assets, String filename){
        StringBuffer contents = new StringBuffer();
    	
		try {
			InputStream inputStream = assets.open(filename);
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			try {
				String read = in.readLine();
			    while (read != null) {
			    	contents.append(read + "\n");
			        read = in.readLine();
			    } 
			} catch (IOException e) {
				e.printStackTrace();
			}

		    contents.deleteCharAt(contents.length() - 1);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    
	    return contents.toString();
    }
    
	public static int loadShader( int type, String source )
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
	
	public static int createProgram( int vertexShader, int fragmentShader )
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
			is = assetMgr.open(assetName);
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
		boolean heightPowerOf2 = (bitmap.getHeight() & (bitmap.getHeight() - 1)) == 0;
		boolean widthPowerOf2 = (bitmap.getWidth() & (bitmap.getWidth() - 1)) == 0;
		if( heightPowerOf2 && widthPowerOf2 ){
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		}else{
			// If the texture is not a power of two then use clamp to edge...
			// otherwise npot with GL_REPEAT fails on the Shield.
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		}

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

	@Override
	public void transform(ColladaMatrix4f m) {
		Matrix4 result = new Matrix4();
		
		result.matrix[0] = m.m11;
		result.matrix[1] = m.m21;
		result.matrix[2] = m.m31;
		result.matrix[3] = m.m41;
		result.matrix[4] = m.m12;
		result.matrix[5] = m.m22;
		result.matrix[6] = m.m32;
		result.matrix[7] = m.m42;
		result.matrix[8] = m.m13;
		result.matrix[9] = m.m23;
		result.matrix[10] = m.m33;
		result.matrix[11] = m.m43;
		result.matrix[12] = m.m14;
		result.matrix[13] = m.m24;
		result.matrix[14] = m.m34;
		result.matrix[15] = m.m44;

		worldViewStack.add( result );
	}

	@Override
	public void setBounds(float minx, float maxx,
			float miny, float maxy,
			float minz, float maxz) {
		minX = minx;
		maxX = maxx;
		minY = miny;
		maxY = maxy;
		minZ = minz;
		maxZ = maxz;		
	}

	@Override
	public boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view) {
		return false;
	}

	@Override
	public boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view) {
		return false;
	}
}
