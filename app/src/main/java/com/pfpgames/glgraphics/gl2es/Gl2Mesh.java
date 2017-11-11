package com.pfpgames.glgraphics.gl2es;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.pfpgames.collada.ColladaFxSampler;
import com.pfpgames.collada.ColladaFxSurface;
import com.pfpgames.collada.ColladaMatrix4f;
import com.pfpgames.collada.ColladaMesh;
import com.pfpgames.collada.ColladaParamEvaluator;
import com.pfpgames.collada.ColladaProfileCommon;
import com.pfpgames.collada.ColladaProfileEvaluator;
import com.pfpgames.collada.ColladaShader;
import com.pfpgames.collada.ColladaTechniqueFxEvaluator;
import com.pfpgames.collada.ColladaTechniqueFxPhong;
import com.pfpgames.glmath.DirectMatrix4f;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.MatrixStack;
import com.pfpgames.glmath.Vector3;
import com.pfpgames.glmath.Vector4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
	private int colorLocation;
	private int shaderProgram;
	private ColladaShader shaderObject;
	private AssetManager assetMgr;
	private Matrix4 worldViewMatrix = null;
	private DirectMatrix4f worldProjViewBuffer = new DirectMatrix4f();
    private float shader_red = 0.75f;
    private float shader_blue = 0.75f;
    private float shader_green = 0.75f;
    private float shader_alpha = 0.75f;
	
	// Bounds used for hit testing
	private float minX; 
	private float maxX;
	private float minY; 
	private float maxY;
	private float minZ; 
	private float maxZ;

    // Original bounds of the mesh
    private float oMinX;
    private float oMaxX;
    private float oMinY;
    private float oMaxY;
    private float oMinZ;
    private float oMaxZ;
	private Vector4 meshMaxBounds = null;
	private Vector4 meshMinBounds = null;
	private String texSource = null;

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
		computeBounds();
    	shaderProgram = createDefaultShader();
		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		colorLocation = GLES20.glGetUniformLocation(shaderProgram, "aColor");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, texName );
		if( texSource != null ) {
			textureHandle = loadGLTexture(texSource);
		}
    }

    @Override
    public void buildMesh(ColladaShader shader){
    	shaderObject = shader;
    	createModel();
		computeBounds();
    	shaderProgram = shader.buildShader(assetMgr, texName, "modelViewMatrix");
		modelViewLocation = GLES20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
		texLocation = GLES20.glGetUniformLocation(shaderProgram, texName );
    }

    public void setDefaultColor(Vector4 color){
        shader_red = color.x();
        shader_blue = color.y();
        shader_green = color.z();
        shader_alpha = color.w();
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
            "#version 100\n" +
			"attribute vec4 aPosition;                     \n" +
			"attribute vec3 aNormal;                       \n" +
			"attribute vec2 aTexCoord;                     \n" +
			"varying vec2 vTexCoord;                       \n" +
			"uniform mat4 modelViewMatrix;                 \n" +
			"void main()                                   \n" +
			"{                                             \n" +
			"   gl_Position = modelViewMatrix * aPosition; \n" +
			"   vTexCoord = aTexCoord;                     \n" +
			"}                                             \n";

		String fragmentShaderSrc = "";

		if( texName.equalsIgnoreCase("texDefault")){
			fragmentShaderSrc =
                "#version 100\n" +
                "precision mediump float;                            \n" +
				"uniform vec4 aColor;                                \n" +
				"void main()                                         \n" +
				"{                                                   \n" +
				"  gl_FragColor = aColor;                            \n" +
				"}                                                   \n";
		}else{
			fragmentShaderSrc =
                "#version 100\n" +
                "precision mediump float;                            \n" +
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

		if( colorLocation != -1 ) {
			GLES20.glUniform4f(colorLocation, shader_red, shader_blue, shader_green, shader_alpha);
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

	public void setHitMargin(float margin) {
		minX = oMinX - (oMaxX - oMinX) * margin;
		maxX = oMaxX + (oMaxX - oMinX) * margin;
		minY = oMinY - (oMaxY - oMinY) * margin;
		maxY = oMaxY + (oMaxY - oMinY) * margin;
		minZ = oMinZ - (oMaxZ - oMinZ) * margin;
		maxZ = oMaxZ + (oMaxZ - oMinZ) * margin;

		if( worldViewMatrix != null ) {
			computeBounds();
		}
	}

	private void computeBounds(){
		final Vector4 localMax = new Vector4(maxX, maxY, maxZ, 1.0f);
		meshMaxBounds = Vector4.Multiply(worldViewMatrix, localMax);

		final Vector4 localMin = new Vector4(minX, minY, minZ, 1.0f);
		meshMinBounds = Vector4.Multiply(worldViewMatrix, localMin);
	}

	public Vector4 getMaxBounds(){
		return meshMaxBounds;
	}

	public Vector4 getMinBounds(){
		return meshMinBounds;
	}

	private interface ProfileEvaluation{
		ColladaParamEvaluator paramEval();
		ColladaTechniqueFxEvaluator techEval();
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
					texSource = tech.getImage().getSource();
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
        oMinX = minx;
        oMaxX = maxx;
        oMinY = miny;
        oMaxY = maxy;
        oMinZ = minz;
        oMaxZ = maxx;
        setHitMargin(0.0f);
	}

	@Override
	public boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view) {
		final Vector4 nearPoint = new Vector4(vx, vy, -1, 1);
		final Vector4 farPoint = new Vector4(vx, vy, 1, 1);

		Vector3 ulBounds = new Vector3();
		Vector3 lrBounds = new Vector3();
		Vector3 urBounds = new Vector3();
		Vector3 llBounds = new Vector3();

		final Matrix4 invProj = Matrix4.invert(proj);
		final Matrix4 curView = Matrix4.multiply(view, worldViewMatrix);

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
		final Vector3 worldMin = new Vector3(Vector4.Multiply(curView, localMin));
		final Vector3 worldMax = new Vector3(Vector4.Multiply(curView, localMax));
		final float wMinX = worldMin.vector[0];
		final float wMinY = worldMin.vector[1];
		final float wMinZ = worldMin.vector[2];
		final float wMaxX = worldMax.vector[0];
		final float wMaxY = worldMax.vector[1];
		final float wMaxZ = worldMax.vector[2];

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

	@SuppressWarnings("unused")
	private boolean triangleLineIntersectBarycentric(
			Vector3 origin, Vector3 direction,
			Vector3 v0, Vector3 v1, Vector3 v2) {

		Vector3 e1,e2,h,s,q;
		float a,f,u,v;
		e1 = Vector3.Sub(v1, v0);
		e2 = Vector3.Sub(v2, v0);

		h = Vector3.Cross(direction, e2);
		a = Vector3.Dot(e1, h);

		if (a > -0.00001 && a < 0.00001)
			return false;

		f = 1.0f / a;
		s = Vector3.Sub(origin, v0);
		u = f * Vector3.Dot(s, h);

		if (u < 0.0 || u > 1.0)
			return false;

		q = Vector3.Cross(s, e1);
		v = f * Vector3.Dot(direction, q);

		if (v < 0.0 || (u + v) > 1.0)
			return false;

		return true;
	}

	@Override
	public boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view) {
		Vector4 nearPoint = new Vector4(vx, vy, -1, 1);
		Vector4 farPoint = new Vector4(vx, vy, 1, 1);

		Matrix4 invProj = Matrix4.invert(proj);
		Matrix4 curView = Matrix4.multiply(view, worldViewMatrix);

		Vector4 nearLocal = Vector4.Multiply(invProj, nearPoint);
		nearLocal.divideThis(nearLocal.vector[3]);
		Vector4 farLocal = Vector4.Multiply(invProj, farPoint);
		farLocal.divideThis(farLocal.vector[3]);

		// Create a 3D line from our positions
		Vector3 direction = new Vector3(Vector4.Sub(farLocal, nearLocal));
		// Normalize the line's directional vector
		direction.Normalize();
		Vector3 linePoint = new Vector3(nearLocal);

		Vector4 localMax = new Vector4(maxX, maxY, maxZ, 1);
		Vector4 test = Vector4.Multiply(curView, localMax);
		Vector3 worldMax = new Vector3(test);
		Vector4 localMin = new Vector4(minX, minY, minZ, 1);
		Vector3 worldMin = new Vector3(Vector4.Multiply(curView, localMin));
		Vector3 worldDiff = Vector3.Sub(worldMax, worldMin);
		float maxRadius = Math.max(Math.max(worldDiff.vector[0], worldDiff.vector[1]), worldDiff.vector[2]);
		float radius = maxRadius/2.0f;

		Vector4 midPoint = new Vector4((maxX - minX)/2.0f+minX, (maxY - minY)/2.0f+minY, (maxZ - minZ)/2.0f+minZ, 1.0f);
		Vector3 midPointWorld = new Vector3(Vector4.Multiply(curView, midPoint));

		return sphereLineIntersect(linePoint, direction, midPointWorld, radius);
	}

	public boolean sphereLineIntersect(
			Vector3 origin,
			Vector3 direction,
			Vector3 center,
			float radius) {
		Vector3 p1_to_point = Vector3.Sub(center, origin);
		Vector3 translated_ray_point = Vector3.Add(origin, direction);
		Vector3 p2_to_point = Vector3.Sub(center, translated_ray_point);

		Vector3 cross_product = Vector3.Cross(p1_to_point, p2_to_point);
		float area_of_triangle_times_two = cross_product.Length();
		float length_of_base = direction.Length();
		float distance_from_point_to_ray = area_of_triangle_times_two / length_of_base;
	
		if (distance_from_point_to_ray < radius)
			return true;

		return false;
	}

	public void renderOffscreen() {
		// TODO Auto-generated method stub
	
	}

	public boolean IsOffscreen() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getOffscreenTexture() {
		// TODO Auto-generated method stub
		return null;
	}

}
