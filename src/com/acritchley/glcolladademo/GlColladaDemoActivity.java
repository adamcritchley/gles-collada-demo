package com.acritchley.glcolladademo;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.acritchley.collada.ColladaHandler;
import com.acritchley.glfonts.FontHandler;
import com.acritchley.glfonts.FontLibrary;
import com.acritchley.glfonts.FontObject;
import com.acritchley.glmath.Matrix4;
import com.acritchley.glmath.MatrixStack;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.MotionEvent;

public class GlColladaDemoActivity extends Activity {
	private static final int NUM_MODELS = 5;

	private static final String MODEL_TO_LOAD1 = "seymour.dae";
	private static final String MODEL_TO_LOAD2 = "textured_monkey.dae";
	private static final String MODEL_TO_LOAD3 = "textured_helicopter.dae";
	private static final String MODEL_TO_LOAD4 = "textured_cube.dae";
	private static final String MODEL_TO_LOAD5 = "torus_tris.dae";
	private static final String FONTS_TO_LOAD = "fonts.xml";

	private static final double CHANGE_THRESHOLD = 0.5;
	
	private int curModel = 0;
	private float xrot = 0.0f;
	private float yrot = 0.0f;
	private float firstX = 0.0f;
	private float firstY = 0.0f;
	private double changeDistance = 0.0f;

	private GLSurfaceView view;
    private AssetManager assetMgr;
    private ColladaHandler hcollada;
    private FontHandler hfonts;
    private Gl2Model[] gl2Model;
	private FontLibrary libfonts;

	private Matrix4 modelViewMatrix = new Matrix4();
	private Matrix4 projMatrix = new Matrix4();
	private float oldX = 0.0f;
	private float oldY = 0.0f;
	private final float TOUCH_SCALE = 0.2f;
	private boolean firstMulti = true;
	private float z = 0.5f;
	private double oldDistance;
	private DecimalFormat threeDec;
	private FontObject ofont;
	private String changeString = "(0.000)%";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assetMgr = getResources().getAssets();

        gl2Model = new Gl2Model[NUM_MODELS];
        for(int i=0; i < NUM_MODELS; i++){
        	gl2Model[i] = new Gl2Model();
        }

        hcollada = new ColladaHandler();
        libfonts = new FontLibrary();
        hfonts = new FontHandler();

        Matrix4.setIdentity(modelViewMatrix);
        modelViewMatrix.scaleThis(z, z, z);
        Matrix4.setIdentity(projMatrix);
        
        threeDec = new DecimalFormat("0.000");
    	threeDec.setGroupingUsed(false);

        if( checkGLES20Support() )
        	view = new GLSurfaceView( this );
        else
        	return;

        view.setEGLContextClientVersion(2);
        view.setRenderer( new DemoRenderer() );        
        setContentView(view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
    protected void onPause( )
    {
    	super.onPause();
    	view.onPause();
    }
    
    protected void onResume( )
    {
    	super.onResume();
    	view.onResume();
    }
    
    private boolean checkGLES20Support()
    {
    	final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        return supportsEs2;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float x = event.getX();
        float y = event.getY();
        
        // If a touch is moved on the screen
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
        	if(event.getPointerCount() == 2){
        		float x1 = event.getX(1);
        		float y1 = event.getY(1);

        		// Calculate the distance between pointers
        		double newDist = Math.sqrt(Math.pow(x1-x,2)+Math.pow(y1-y,2))/1500.0;

        		if( !firstMulti )
        		{
		        	// Calculate the change
	        		double dDistance = newDist - oldDistance;
	        		z -= dDistance;
        		}

        		oldDistance = newDist;
        		firstMulti = false;
        	}else{
	        	// Calculate the change
	        	float dx = x - oldX;
		        float dy = y - oldY;
	        	
	        	// Rotate around the axis     		
    	        xrot += dx * TOUCH_SCALE;
    	        yrot += dy * TOUCH_SCALE;
    	        
    	        // Remember the values
    	        oldX = x;
    	        oldY = y;

    	        // See if we moved far enough to signal a change in model
        		changeDistance = Math.sqrt(Math.pow(firstX-x,2)+Math.pow(firstY-y,2))/1500.0;
        		changeString = "(" + threeDec.format(changeDistance / CHANGE_THRESHOLD) + ")%";
    	        if( changeDistance > CHANGE_THRESHOLD ) {
    	        	curModel = (curModel+1) % NUM_MODELS;

    	       		firstX = x;
    	       		firstY = y;
    	        }
        	}
        }else if( event.getAction() == MotionEvent.ACTION_DOWN ){
        	firstMulti = true;
      		oldX = x;
       		oldY = y;
       		
       		firstX = x;
       		firstY = y;
        }

		// Calculate model view matrix.
		MatrixStack stack = new MatrixStack();
		stack.add( Matrix4.rotate(-xrot, 0, 1, 0) );
		stack.add( Matrix4.rotate(-yrot, 1, 0, 0) );
		stack.add( Matrix4.scale(z, z, z) );
		modelViewMatrix = stack.collapse();

		return true;
	}

    class DemoRenderer implements Renderer
    {
    	private boolean firstFrame = true;
    	private int viewportWidth, viewportHeight;
		private long startTime;
		private long framesDrawn = 0;

		public void onDrawFrame(GL10 gl) 
		{
			if( firstFrame ){
				startTime = System.currentTimeMillis();
				firstFrame = false;
			}

			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GLES20.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );	
			GLES20.glViewport ( 0, 0, viewportWidth, viewportHeight  );

			GLES20.glFrontFace( GLES20.GL_CCW );

			gl2Model[curModel].drawTriangles(projMatrix, modelViewMatrix);

			long nowTime = System.currentTimeMillis();
			long diffTime = nowTime - startTime;
			String fpsText = "FPS " + ((long)(framesDrawn / ((float)(diffTime/1000.0f))));
			ofont.draw(-0.75f, -0.75f, 0.0f, 0.05f, fpsText );
			String sizeText = "Vertices " + gl2Model[curModel].size();
			ofont.draw(-0.85f, 0.75f, 0.0f, 0.025f, sizeText);
			String changeText = "Long swipe right to change " + changeString;
			ofont.draw(-0.85f, 0.9f, 0.0f, 0.025f, changeText );

			framesDrawn++;
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) 
		{		
			viewportWidth = width;
			viewportHeight = height;
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) 
		{
			try {
				gl2Model[0].setAssetManager(assetMgr);
				hcollada.parseDae(assetMgr.open(MODEL_TO_LOAD1), gl2Model[0]);
				gl2Model[0].buildModel();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				gl2Model[1].setAssetManager(assetMgr);
				hcollada.parseDae(assetMgr.open(MODEL_TO_LOAD2), gl2Model[1]);
				gl2Model[1].buildModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				gl2Model[2].setAssetManager(assetMgr);
				hcollada.parseDae(assetMgr.open(MODEL_TO_LOAD3), gl2Model[2]);
				gl2Model[2].buildModel();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				gl2Model[3].setAssetManager(assetMgr);
				hcollada.parseDae(assetMgr.open(MODEL_TO_LOAD4), gl2Model[3]);
				gl2Model[3].buildModel();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				gl2Model[4].setAssetManager(assetMgr);
				hcollada.parseDae(assetMgr.open(MODEL_TO_LOAD5), gl2Model[4]);
				gl2Model[4].buildModel();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				hfonts.parseFonts(assetMgr.open(FONTS_TO_LOAD), libfonts);
				ofont = libfonts.createFont("system", assetMgr);
			} catch (IOException e) {
				e.printStackTrace();
			}

			GLES20.glDisable(GLES20.GL_DITHER);				// Disable dithering ( NEW )
			GLES20.glEnable(GLES20.GL_TEXTURE_2D);			// Enable Texture Mapping
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	// Black Background
			GLES20.glClearDepthf(10.0f); 					// Depth Buffer Setup
			GLES20.glEnable(GLES20.GL_DEPTH_TEST); 			// Enables Depth Testing
			GLES20.glDepthFunc(GLES20.GL_LEQUAL); 			// The Type Of Depth Testing To Do
		}
    }
}