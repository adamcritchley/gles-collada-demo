package com.pfpgames.glcolladademo;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.pfpgames.collada.ColladaHandler;
import com.pfpgames.glfonts.FontHandler;
import com.pfpgames.glfonts.FontLibrary;
import com.pfpgames.glfonts.FontObject;
import com.pfpgames.glgraphics.MGText;
import com.pfpgames.glgraphics.MGTextAABB;
import com.pfpgames.glgraphics.ProgressCallback;
import com.pfpgames.glgraphics.gl2es.Gl2Model;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.MatrixStack;
import com.pfpgames.glmath.Vector4;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class GlColladaDemoActivity extends Activity {
    private static final int NUM_MODELS = 5;

    private static final String MODELS_TO_LOAD[] = {"seymour.dae", "x-fighter.dae", "guitar.dae", "tree.dae", "blender.dae"};
    private static final String FONTS_TO_LOAD = "fonts.xml";

    private static final double CHANGE_THRESHOLD = 0.7;

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
    private Gl2Model[] gl2Model = null;
    private float[] modelSizeBias;
    private FontLibrary libfonts;
    private Gl2Model[] textList = null;
    private float[] pitchList = null;

    private Matrix4 modelViewMatrix = new Matrix4();
    private Matrix4 projMatrix = new Matrix4();
    private float oldX = 0.0f;
    private float oldY = 0.0f;
    private final float TOUCH_SCALE = 0.2f;
    private boolean firstMulti = true;
    private float z = 4.0f;
    private double oldDistance;
    private DecimalFormat threeDec;
    private FontObject ofont;
    private String changeString = "(0.000)%";
    private long currentScreen = 0;
    private MGTextAABB models_view = null;
    private MGTextAABB mini_game = null;
    private MGText label_view = null;
    private float viewportWidthDiv2 = 0.0f;
    private float viewportHeightDiv2 = 0.0f;
    private MiniGame minigame_level = null;
    private DemoRenderer glrenderer = null;
    private Gl2Model pflogo = null;
    private Matrix4 pflogoTForm = null;
    private boolean firstFrame = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assetMgr = getResources().getAssets();

        modelSizeBias = new float[NUM_MODELS];
        modelSizeBias[0] = 0.0f;
        modelSizeBias[1] = -3.4f;
        modelSizeBias[2] = -3.5f;
        modelSizeBias[3] = -3.5f;
        modelSizeBias[4] = -2.5f;

        gl2Model = new Gl2Model[NUM_MODELS];
        for(int i=0; i < NUM_MODELS; i++){
            gl2Model[i] = null;
        }

        textList = new Gl2Model[255];
        for(int i=0; i<255; i++){
            textList[i] = null;
        }

        pitchList = new float[255];
        for(int i=0; i<255; i++){
            pitchList[i] = 0.2f;
        }

        pitchList[' '] = 0.3f;

        hcollada = new ColladaHandler();
        libfonts = new FontLibrary();
        hfonts = new FontHandler();

        Matrix4.setIdentity(modelViewMatrix);
        modelViewMatrix.scaleThis(z, z, z);
        Matrix4.setIdentity(projMatrix);

        pflogoTForm = Matrix4.translate(0.0f, 0.0f, -3.5f);
        pflogoTForm.rotateThis(90.0f, 0.0f, 1.0f, 0.0f);
        pflogoTForm.scaleThis(3.7f, 3.7f, 3.7f);

        threeDec = new DecimalFormat("0.000");
        threeDec.setGroupingUsed(false);

        if( checkGLES20Support() )
            view = new GLSurfaceView( this );
        else
            return;

        view.setEGLContextClientVersion(2);
        glrenderer = new DemoRenderer();
        view.setRenderer( glrenderer );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

            final float x = event.getX();
            final float y = event.getY();

            // Convert to normalized screen coordinates (-1.0f to 1.0f)
            final float screen_x = (x - viewportWidthDiv2) / viewportWidthDiv2;
            final float screen_y = -(y - viewportHeightDiv2) / viewportHeightDiv2;

            // Do the screen checks first...
            if( currentScreen == 0 ) {
            }else if ( currentScreen == 1 ){
                // If a touch is moved on the screen
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // Do nothing...
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    if (models_view != null && models_view.hitTest(screen_x, screen_y)) {
                        firstFrame = true;
                        currentScreen = 2;
                    } else if (mini_game != null && mini_game.hitTest(screen_x, screen_y)) {
                        currentScreen = 3;
                    }
                }
            }else if (currentScreen == 2 ){
                // If a touch is moved on the screen
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getPointerCount() == 2) {
                        float x1 = event.getX(1);
                        float y1 = event.getY(1);

                        // Calculate the distance between pointers
                        double newDist = Math.sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2)) / 1500.0;

                        if (!firstMulti) {
                            // Calculate the change
                            double dDistance = newDist - oldDistance;
                            z -= dDistance;
                        }

                        oldDistance = newDist;
                        firstMulti = false;
                    } else {
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
                        changeDistance = Math.sqrt(Math.pow(firstX - x, 2) + Math.pow(firstY - y, 2)) / 1500.0;
                        changeString = "(" + threeDec.format(changeDistance / CHANGE_THRESHOLD) + ")%";
                        if (changeDistance > CHANGE_THRESHOLD) {
                            curModel = (curModel + 1) % NUM_MODELS;

                            firstX = x;
                            firstY = y;
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    firstMulti = true;
                    oldX = x;
                    oldY = y;

                    firstX = x;
                    firstY = y;
                }

                // Calculate model view matrix.
                final float biased_z = z + modelSizeBias[curModel];
                MatrixStack stack = new MatrixStack();
                stack.add( Matrix4.translate(0.0f, 0.0f, -4.0f) );
                stack.add( Matrix4.rotate(-xrot, 0.0f, 1.0f, 0.0f) );
                stack.add( Matrix4.rotate(-yrot, 1.0f, 0.0f, 0.0f) );
                stack.add( Matrix4.scale(biased_z, biased_z, biased_z) );
                modelViewMatrix = stack.collapse();
            }else if( currentScreen == 3 ){
                // The event object is re-used by Android every touch event
                // so we'll need to make our own copy to avoid having the
                // pass by ref copy of MotionEvent changed from under us.
                final TouchEvent te = new TouchEvent(screen_x, screen_y, event.getAction());
                view.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                        glrenderer.onTouchEvent(te);
                        }
                        });
            }

            return true;
        }

    @Override
        public void onBackPressed() {
            if( currentScreen > 1 && currentScreen != 0 ) {
                currentScreen = 1;
            }else{
                finish();
            }
        }

    class TouchEvent{
        public float x;
        public float y;
        public int action;
        TouchEvent(float a, float b, int c){
            x = a;
            y = b;
            action = c;
        }
    }

    class DemoRenderer implements Renderer
    {
        private int viewportWidth, viewportHeight;
        private long startTime;
        private long framesDrawn = 0;
        private long diffTime = 1;
        private float totalLoaded = 0.0f;
        private float percentLoaded = 0.0f;
        private final int CALLBACK_FREQUENCY = 10;
        private final int MODEL_WEIGHT = 20;
        private final int TOTAL_MODELS = 79 + NUM_MODELS * MODEL_WEIGHT;
        private final float LOAD_STEP = 1.0F / TOTAL_MODELS;

        AsyncTask<Void, Void, Void> task = null;
        private int model_loading = 0;

        public void onDrawFrame(GL10 unused)
        {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT );

            GLES20.glFrontFace( GLES20.GL_CCW );
            GLES20.glViewport ( 0, 0, viewportWidth, viewportHeight  );

            if( currentScreen == 0 ) {
                if( loadNext() ){
                    totalLoaded++;
                    percentLoaded += LOAD_STEP;
                }

                // Draw the progress indicator
                ofont.draw(-0.75f, -0.75f, 0.0f, 0.05f, Integer.toString((int)(percentLoaded*100.0f)) + "%" );

                // Draw the splash image
                pflogo.drawTriangles(projMatrix, pflogoTForm);

                if( totalLoaded == TOTAL_MODELS ) {
                    currentScreen = 1;
                }
            }else if( currentScreen == 1 ) {
                // Draw the demo selection screen
                label_view.draw(projMatrix);
                models_view.draw(projMatrix);
                mini_game.draw(projMatrix);
            }else if( currentScreen == 2) {
                if( firstFrame ){
                    startTime = System.currentTimeMillis();
                    firstFrame = false;
                }

                // Draw the model preview screen
                gl2Model[curModel].drawTriangles(projMatrix, modelViewMatrix);

                String fpsText = "FPS " + ((long)(framesDrawn / (diffTime/1000.0f)));
                ofont.draw(-0.75f, -0.75f, 0.0f, 0.05f, fpsText );
                String sizeText = "Vertices " + gl2Model[curModel].size();
                ofont.draw(-0.85f, 0.75f, 0.0f, 0.025f, sizeText);
                String changeText = "Long swipe right to change " + changeString;
                ofont.draw(-0.85f, 0.9f, 0.0f, 0.025f, changeText );

                long nowTime = System.currentTimeMillis();
                diffTime = nowTime - startTime;

                framesDrawn++;
            }else if( currentScreen == 3) {
                // Play our mini game!
                float draw_time = ((float)(System.currentTimeMillis() - startTime)) / 1000.0f;
                minigame_level.drawStage(projMatrix, viewportWidth, viewportHeight, draw_time);
            }
        }

        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            final float w = width;
            final float h = height;
            viewportWidth = width;
            viewportHeight = height;
            viewportWidthDiv2 = w / 2.0f;
            viewportHeightDiv2 = h / 2.0f;
            final float ratio = w / h;
            projMatrix = Matrix4.frustum(-ratio, ratio, -1, 1, 1, 10);
        }

        private boolean loadNext()
        {
            // Load the basic requirements for the loading screen
            try {
                hfonts.parseFonts(assetMgr.open(FONTS_TO_LOAD), libfonts);
                ofont = libfonts.createFont("system", assetMgr);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if( pflogo == null ) {
                try {
                    pflogo = new Gl2Model();
                    pflogo.setAssetManager(assetMgr);
                    pflogo.setFlipTextureV(true);
                    hcollada.parseDae(assetMgr.open("pflogo.dae"), pflogo);
                    pflogo.buildModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Some of the models are large so build them in an AsyncTask to avoid an ANR.
            if( task == null ) {
                task = new AsyncTask<Void, Void, Void>() {
                    private float iteration_weight = 0.0f;
                    private float accumulative_weight = 0.0f;
                    private float prevLoaded = 0.0f;
                    private int iteration = 1;

                    @Override
                        protected Void doInBackground(Void... params) {
                            ProgressCallback loadingProgress = new ProgressCallback(CALLBACK_FREQUENCY) {
                                @Override
                                    public boolean reportProgress(PROGRESS_STAGE stage, int progress, int step, int total) {
                                        if( total != 0 ) {
                                            if (progress == 0) {
                                                // "iteration" is now 2, which means the "iteration_weight" starts at 50% of MODEL_WEIGHT
                                                iteration++;
                                                // Perform a logarithmic operation which approaches MODEL_WEIGHT with each iteration
                                                iteration_weight = (MODEL_WEIGHT * (1.0f - (1.0f / iteration))) - accumulative_weight;
                                            }

                                            // Calculate the progress for this step of the iteration's weight
                                            final float current_weight = (step / (float)total) * iteration_weight;
                                            accumulative_weight += current_weight;
                                            // Add the weight to to the initial loading progress for this model
                                            update(prevLoaded + accumulative_weight);
                                        }

                                        return false;
                                    }
                            };

                            for( int i = 0; i < NUM_MODELS; i++) {
                                if (gl2Model[i] == null) {
                                    prevLoaded = totalLoaded;

                                    try {
                                        // No OpenGL calls should occur here!
                                        gl2Model[i] = new Gl2Model();
                                        gl2Model[i].setAssetManager(assetMgr);
                                        gl2Model[i].setFlipTextureV(true);
                                        hcollada.parseDae(assetMgr.open(MODELS_TO_LOAD[i]),
                                                gl2Model[i],
                                                loadingProgress);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // Finish by adding the total weight
                                    // Adding the difference in weight will introduce floating precision errors
                                    update(prevLoaded + MODEL_WEIGHT);
                                    accumulative_weight = 0.0f;
                                    iteration = 1;
                                }
                            }

                            return null;
                        }

                    private void update(float progress) {
                        totalLoaded = progress;
                        percentLoaded = progress * LOAD_STEP;
                    }
                };
                task.execute();
            }

            // Wait for the async task to finish
            if( task.getStatus() != AsyncTask.Status.FINISHED) {
                // Don't update our splash screen loading progress
                // as the AsyncTask will be updating it for us
                return false;
            }

            // These need to be built in the OpenGL context instead of the AsyncTask.
            if( model_loading < NUM_MODELS ){
                gl2Model[model_loading].buildModel();
                model_loading++;

                return true;
            }

            if( textList['a'] == null ) {
                buildCharacter(hcollada, assetMgr, "a.dae", 'a', 0.41f);

                return true;
            }

            if( textList['b'] == null ) {
                buildCharacter(hcollada, assetMgr, "b.dae", 'b', 0.44f);

                return true;
            }

            if( textList['c'] == null ) {
                buildCharacter(hcollada, assetMgr, "c.dae", 'c', 0.42f);

                return true;
            }

            if( textList['d'] == null ) {
                buildCharacter(hcollada, assetMgr, "d.dae", 'd', 0.46f);

                return true;
            }

            if( textList['e'] == null ) {
                buildCharacter(hcollada, assetMgr, "e.dae", 'e', 0.52f);

                return true;
            }

            if( textList['f'] == null ) {
                buildCharacter(hcollada, assetMgr, "f.dae", 'f', 0.36f);

                return true;
            }

            if( textList['g'] == null ) {
                buildCharacter(hcollada, assetMgr, "g.dae", 'g', 0.44f);

                return true;
            }

            if( textList['h'] == null ) {
                buildCharacter(hcollada, assetMgr, "h.dae", 'h', 0.46f);

                return true;
            }

            if( textList['i'] == null ) {
                buildCharacter(hcollada, assetMgr, "i.dae", 'i', 0.22f);

                return true;
            }

            if( textList['j'] == null ) {
                buildCharacter(hcollada, assetMgr, "j.dae", 'j', 0.22f);

                return true;
            }

            if( textList['k'] == null ) {
                buildCharacter(hcollada, assetMgr, "k.dae", 'k', 0.41f);

                return true;
            }

            if( textList['l'] == null ) {
                buildCharacter(hcollada, assetMgr, "l.dae", 'l', 0.24f);

                return true;
            }

            if( textList['m'] == null ) {
                buildCharacter(hcollada, assetMgr, "m.dae", 'm', 0.72f);

                return true;
            }

            if( textList['n'] == null ) {
                buildCharacter(hcollada, assetMgr, "n.dae", 'n', 0.52f);

                return true;
            }

            if( textList['o'] == null ) {
                buildCharacter(hcollada, assetMgr, "o.dae", 'o', 0.54f);

                return true;
            }

            if( textList['p'] == null ) {
                buildCharacter(hcollada, assetMgr, "p.dae", 'p', 0.48f);

                return true;
            }

            if( textList['q'] == null ) {
                buildCharacter(hcollada, assetMgr, "q.dae", 'q', 0.44f);

                return true;
            }

            if( textList['r'] == null ) {
                buildCharacter(hcollada, assetMgr, "r.dae", 'r', 0.40f);

                return true;
            }

            if( textList['s'] == null ) {
                buildCharacter(hcollada, assetMgr, "s.dae", 's', 0.36f);

                return true;
            }

            if( textList['t'] == null ) {
                buildCharacter(hcollada, assetMgr, "t.dae", 't', 0.36f);

                return true;
            }

            if( textList['u'] == null ) {
                buildCharacter(hcollada, assetMgr, "u.dae", 'u', 0.45f);

                return true;
            }

            if( textList['v'] == null ) {
                buildCharacter(hcollada, assetMgr, "v.dae", 'v', 0.42f);

                return true;
            }

            if( textList['w'] == null ) {
                buildCharacter(hcollada, assetMgr, "w.dae", 'w', 0.68f);

                return true;
            }

            if( textList['x'] == null ) {
                buildCharacter(hcollada, assetMgr, "x.dae", 'x', 0.48f);

                return true;
            }

            if( textList['y'] == null ) {
                buildCharacter(hcollada, assetMgr, "y.dae", 'y', 0.41f);

                return true;
            }

            if( textList['z'] == null ) {
                buildCharacter(hcollada, assetMgr, "z.dae", 'z', 0.41f);

                return true;
            }

            if( textList['A'] == null ) {
                buildCharacter(hcollada, assetMgr, "A_up.dae", 'A', 0.63f);

                return true;
            }

            if( textList['B'] == null ) {
                buildCharacter(hcollada, assetMgr, "B_up.dae", 'B', 0.55f);

                return true;
            }

            if( textList['C'] == null ) {
                buildCharacter(hcollada, assetMgr, "C_up.dae", 'C', 0.66f);

                return true;
            }

            if( textList['D'] == null ) {
                buildCharacter(hcollada, assetMgr, "D_up.dae", 'D', 0.68f);

                return true;
            }

            if( textList['E'] == null ) {
                buildCharacter(hcollada, assetMgr, "E_up.dae", 'E', 0.52f);

                return true;
            }

            if( textList['F'] == null ) {
                buildCharacter(hcollada, assetMgr, "F_up.dae", 'F', 0.52f);

                return true;
            }

            if( textList['G'] == null ) {
                buildCharacter(hcollada, assetMgr, "G_up.dae", 'G', 0.68f);

                return true;
            }

            if( textList['H'] == null ) {
                buildCharacter(hcollada, assetMgr, "H_up.dae", 'H', 0.63f);

                return true;
            }

            if( textList['I'] == null ) {
                buildCharacter(hcollada, assetMgr, "I_up.dae", 'I', 0.30f);

                return true;
            }

            if( textList['J'] == null ) {
                buildCharacter(hcollada, assetMgr, "J_up.dae", 'J', 0.32f);

                return true;
            }

            if( textList['K'] == null ) {
                buildCharacter(hcollada, assetMgr, "K_up.dae", 'K', 0.65f);

                return true;
            }

            if( textList['L'] == null ) {
                buildCharacter(hcollada, assetMgr, "L_up.dae", 'L', 0.55f);

                return true;
            }

            if( textList['M'] == null ) {
                buildCharacter(hcollada, assetMgr, "M_up.dae", 'M', 0.78f);

                return true;
            }

            if( textList['N'] == null ) {
                buildCharacter(hcollada, assetMgr, "N_up.dae", 'N', 0.74f);

                return true;
            }

            if( textList['O'] == null ) {
                buildCharacter(hcollada, assetMgr, "O_up.dae", 'O', 0.78f);

                return true;
            }

            if( textList['P'] == null ) {
                buildCharacter(hcollada, assetMgr, "P_up.dae", 'P', 0.52f);

                return true;
            }

            if( textList['Q'] == null ) {
                buildCharacter(hcollada, assetMgr, "Q_up.dae", 'Q', 0.74f);

                return true;
            }

            if( textList['R'] == null ) {
                buildCharacter(hcollada, assetMgr, "R_up.dae", 'R', 0.58f);

                return true;
            }

            if( textList['S'] == null ) {
                buildCharacter(hcollada, assetMgr, "S_up.dae", 'S', 0.48f);

                return true;
            }

            if( textList['T'] == null ) {
                buildCharacter(hcollada, assetMgr, "T_up.dae", 'T', 0.56f);

                return true;
            }

            if( textList['U'] == null ) {
                buildCharacter(hcollada, assetMgr, "U_up.dae", 'U', 0.68f);

                return true;
            }

            if( textList['V'] == null ) {
                buildCharacter(hcollada, assetMgr, "V_up.dae", 'V', 0.62f);

                return true;
            }

            if( textList['W'] == null ) {
                buildCharacter(hcollada, assetMgr, "W_up.dae", 'W', 0.92f);

                return true;
            }

            if( textList['X'] == null ) {
                buildCharacter(hcollada, assetMgr, "X_up.dae", 'X', 0.66f);

                return true;
            }

            if( textList['Y'] == null ) {
                buildCharacter(hcollada, assetMgr, "Y_up.dae", 'Y', 0.58f);

                return true;
            }

            if( textList['Z'] == null ) {
                buildCharacter(hcollada, assetMgr, "Z_up.dae", 'Z', 0.58f);

                return true;
            }

            if( textList['1'] == null ) {
                buildCharacter(hcollada, assetMgr, "1.dae", '1', 0.50f);

                return true;
            }

            if( textList['2'] == null ) {
                buildCharacter(hcollada, assetMgr, "2.dae", '2', 0.60f);

                return true;
            }

            if( textList['3'] == null ) {
                buildCharacter(hcollada, assetMgr, "3.dae", '3', 0.58f);

                return true;
            }

            if( textList['4'] == null ) {
                buildCharacter(hcollada, assetMgr, "4.dae", '4', 0.58f);

                return true;
            }

            if( textList['5'] == null ) {
                buildCharacter(hcollada, assetMgr, "5.dae", '5', 0.58f);

                return true;
            }

            if( textList['6'] == null ) {
                buildCharacter(hcollada, assetMgr, "6.dae", '6', 0.58f);

                return true;
            }

            if( textList['7'] == null ) {
                buildCharacter(hcollada, assetMgr, "7.dae", '7', 0.58f);

                return true;
            }

            if( textList['8'] == null ) {
                buildCharacter(hcollada, assetMgr, "8.dae", '8', 0.58f);

                return true;
            }

            if( textList['9'] == null ) {
                buildCharacter(hcollada, assetMgr, "9.dae", '9', 0.58f);

                return true;
            }

            if( textList['0'] == null ) {
                buildCharacter(hcollada, assetMgr, "0.dae", '0', 0.58f);

                return true;
            }

            if( textList['\''] == null ) {
                buildCharacter(hcollada, assetMgr, "apostrophe.dae", '\'', 0.30f);

                return true;
            }

            if( textList['!'] == null ) {
                buildCharacter(hcollada, assetMgr, "bang.dae", '!', 0.52f);

                return true;
            }

            if( textList[':'] == null ) {
                buildCharacter(hcollada, assetMgr, "colon.dae", ':', 0.52f);

                return true;
            }

            if( textList[','] == null ) {
                buildCharacter(hcollada, assetMgr, "comma.dae", ',', 0.44f);

                return true;
            }

            if( textList['.'] == null ) {
                buildCharacter(hcollada, assetMgr, "period.dae", '.', 0.30f);

                return true;
            }

            if( textList['?'] == null ) {
                buildCharacter(hcollada, assetMgr, "question.dae", '?', 0.52f);

                return true;
            }

            if( textList['-'] == null ) {
                buildCharacter(hcollada, assetMgr, "hyphen.dae", '-', 0.38f);

                return true;
            }

            if( textList['%'] == null ) {
                buildCharacter(hcollada, assetMgr, "percent.dae", '%', 0.38f);

                return true;
            }

            if( label_view == null ) {
                label_view = new MGText(textList, pitchList, "Choose a demo:", 0.0f, 2.0f, -4.0f, 1.0f);
                label_view.setOffset(-label_view.getWidth() / 2.0f, label_view.getY());
                label_view.setColor(new Vector4(1.0f, 1.0f, 1.0f, 0.0f));

                return true;
            }

            if( models_view == null ) {
                // Create an Axis Aligned Bounding Box for our text
                models_view = new MGTextAABB(textList, pitchList, "Models", 0.0f, 0.5f, -4.0f, 0.75f);
                // Center the model
                models_view.setOffset(-models_view.getWidth() / 2.0f, models_view.getY());
                models_view.setColor(new Vector4(1.0f, 1.0f, 1.0f, 0.0f));

                return true;
            }

            if( mini_game == null ) {
                // Create an Axis Aligned Bounding Box for our text
                mini_game = new MGTextAABB(textList, pitchList, "Minigame", 0.0f, -0.5f, -4.0f, 0.75f);
                // Center the model
                mini_game.setOffset(-mini_game.getWidth() / 2.0f, mini_game.getY());
                mini_game.setColor(new Vector4(1.0f, 1.0f, 1.0f, 0.0f));

                return true;
            }

            if( minigame_level == null ) {
                minigame_level = new MiniGame(hcollada, assetMgr, pitchList, textList);
            }

            return true;
        }

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            GLES20.glDisable(GLES20.GL_DITHER);				// Disable dithering ( NEW )
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);			// Enable Texture Mapping
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 	// Black Background
            GLES20.glClearDepthf(10.0f); 					// Depth Buffer Setup
            GLES20.glEnable(GLES20.GL_DEPTH_TEST); 			// Enables Depth Testing
            GLES20.glDepthFunc(GLES20.GL_LEQUAL); 			// The Type Of Depth Testing To Do
        }


        private void buildCharacter(ColladaHandler hcollada, AssetManager assetMgr, String file, char cval, float pitch)
        {
            pitchList[cval] = pitch;
            textList[cval] = new Gl2Model();

            try {
                textList[cval].setAssetManager(assetMgr);
                hcollada.parseDae(assetMgr.open(file), textList[cval] );
                textList[cval].buildModel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onTouchEvent(final TouchEvent te) {
            minigame_level.touchInput(te.x, te.y, te.action);
        }
    }
}
