package com.pfpgames.glcolladademo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.opengl.GLES20;
import android.view.MotionEvent;
import com.pfpgames.glgraphics.MGModel;
import com.pfpgames.glgraphics.MGRollingInteger;
import com.pfpgames.glgraphics.MGText;
import com.pfpgames.glgraphics.gl2es.Gl2Model;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.collada.ColladaHandler;
import android.content.res.AssetManager;

public class MiniGame{

	private final float THETA_SCALE = 0.2f;
	private final float TWINKLE_FACTOR = 0.0845569508577407777468194599525f;
    private final float ROLLING_SCORE_FACTOR = 0.05f;
    private final float MAX_SPAWN_RATE = 4.0f;
    private final float MIN_SPAWN_RATE = 0.25f;
    private final float ADJ_SPAWN_RATE = 0.01f;
    private final long PER_OBJ_SCORE = 100;
	private MGModel hitModel = null;
	private Gl2Model badguy = null;
	private ArrayList<MGBadGuy> badguys = new ArrayList<MGBadGuy>();
    private MGEarth earth = null;
    private Gl2Model earth_model = null;
	private int last_step = -1;
	private MGText moreText = null;
	private MGText scoreText = null;
    private MGRollingInteger scoreDisplay = null;
	private float spawn_rate = 3.0f;
	private MGNebulaSky nebula_background;
	private Gl2Model[] textList = null;
	private float[] pitchList = null;
	private Gl2Model starfield = null;
	private Gl2Model nebulasky = null;
	private Gl2StarfieldShader starfieldshader = null;
	private Gl2NebulaShader nebulashader = null;
    private Random badguy_theta_seed = new Random();
    private long score = 0;
    private float rolling_percent = 0.0f;
    private long CLAMP_SIGDIGITS = 10000;
    private long last_percentage = CLAMP_SIGDIGITS;

    public MiniGame(ColladaHandler hcollada, AssetManager assets, float[] pitch, Gl2Model[] text){
        // Save off the text info
		textList = text;
		pitchList = pitch;

        // Create our assets
		moreText = createText("more!", 4.5f, 3.0f, -4.0f, 0.6f);
        scoreDisplay = createRollingInteger(0, 4.4f, -3.2f, -4.0f, 0.8f);
		scoreText = createText("Score", 2.6f, -3.2f, -4.0f, 0.7f);

        try {
            // Load the Earth model
            earth_model = new Gl2Model();
            earth_model.setAssetManager(assets);
            hcollada.parseDae(assets.open("earth.dae"), earth_model);
            earth_model.buildModel();
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
            // Load the badguy model (the model is shared amongst all the MGBadGuy instances)
			badguy = new Gl2Model();
			badguy.setAssetManager(assets);
			hcollada.parseDae(assets.open("badguy.dae"), badguy);
			badguy.buildModel();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
            // Load the canvas to draw the stars offscreen
			starfield = new Gl2Model();
			starfield.setAssetManager(assets);
			hcollada.parseDae(assets.open("boxcanvas.dae"), starfield);
			starfieldshader = new Gl2StarfieldShader();
			starfield.buildModel(starfieldshader);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
            // Load the canvas to draw the stars background
			nebulasky = new Gl2Model();
			nebulasky.setAssetManager(assets);
			hcollada.parseDae(assets.open("nebulacanvas.dae"), nebulasky);
			nebulashader = new Gl2NebulaShader(starfield);
			nebulasky.buildModel(nebulashader);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create our star renderer
		nebula_background = createNebulaShader();

        // Create the Earth model
        earth = createEarth();
	}

	public void touchInput(float x, float y, int action) {
		
		if( action == MotionEvent.ACTION_DOWN ){
            if( moreText.hitTest(x, y) ){
                // Increase spawn rate and instantly reflect new rate
                last_step = -1;
                spawn_rate -= ADJ_SPAWN_RATE;
                if( spawn_rate < MIN_SPAWN_RATE ){
                    spawn_rate = MIN_SPAWN_RATE;
                }
            }

    		for(Iterator<MGBadGuy> iterator=badguys.iterator(); iterator.hasNext(); ){
    			MGBadGuy badguy = iterator.next();
    			
				if( badguy.hitTest(x, y) ){
                    // Increase the score
                    final long new_score = score + PER_OBJ_SCORE;
                    // Adjust our current percentage to reflect the new maximum
                    rolling_percent = ((rolling_percent * score) / new_score);
                    // Set the new maximum and percentage
                    scoreDisplay.setMaxNumber(new_score);
                    scoreDisplay.setPercentage(rolling_percent);
                    score = new_score;
                    // Increase the spawn rate
                    spawn_rate -= ADJ_SPAWN_RATE;
                    if( spawn_rate < MIN_SPAWN_RATE ){
                        spawn_rate = MIN_SPAWN_RATE;
                    }
					iterator.remove();
				}
    		}

        }else if( action == MotionEvent.ACTION_MOVE && hitModel != null ) {

        }else if( action == MotionEvent.ACTION_UP && hitModel != null ){

        }
	}

	public void drawStage(Matrix4 projection, int width, int height, float time) {
		float theta = (time * THETA_SCALE ) % 360.0f;
		float twinkle = (time * TWINKLE_FACTOR ) % 360.0f + 12;

        // Render the stars to an offscreen texture
		nebula_background.updateStarfield( 7.0f, twinkle, 5.0f );

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT );

		GLES20.glFrontFace( GLES20.GL_CCW );
		GLES20.glViewport ( 0, 0, width, height  );

        // Draw the stars to our background
		nebula_background.draw(projection);

        // Draw the Earth
        earth.setRotation(theta * 40.0f, 1.0f, 0.0f, 0.0f);
        earth.draw(projection);

        // Calculate our rolling score adjustment, adding more the farther away we are
        rolling_percent += (ROLLING_SCORE_FACTOR*((1.0f-rolling_percent)/1.0f));
        // Clamp to 100%, remove all digits of our mantissa after the significant digits
        final long current_percentage = (long)(rolling_percent*CLAMP_SIGDIGITS);
        if( last_percentage != CLAMP_SIGDIGITS && last_percentage == current_percentage ) {
            rolling_percent = 1.0f;
        }
        last_percentage = current_percentage;
        // Finally update our score
        scoreDisplay.setPercentage(rolling_percent);

        // Do we need to spawn a new bad guy?
		int step = (int)(time / spawn_rate);
		if(step != last_step) {
			MGBadGuy new_badguy = createBadGuy();
			new_badguy.setOffset((float) Math.cos(theta) * 4.0f, (float) Math.sin(theta) * 4.0f);
			new_badguy.setTheta(badguy_theta_seed.nextInt(360));
			new_badguy.setTime(time);
			badguys.add(new_badguy);
			last_step = step;
		}

		// Propagate the bad guys!
		for(Iterator<MGBadGuy> iterator=badguys.iterator(); iterator.hasNext(); ){
			MGBadGuy this_badguy = iterator.next();

            // Get the custom rotation bias for this model
			final float mytheta = this_badguy.getTheta();
            // Reduce the speed as the object gets closer to the center
			final float mytime = (1.0f/(time-this_badguy.getTime()+1));

			if( mytime < 0.075f ){
				// Crashed into the Earth... whoops!
                final long new_score = score - PER_OBJ_SCORE;
                // Adjust our rolling score accordingly
                if( new_score >= 0 ) {
                    // Adjust our current percentage to reflect the new maximum
                    rolling_percent = ((rolling_percent * score) / new_score);
                    // Set the new maximum and percentage
                    scoreDisplay.setMaxNumber(new_score);
                    scoreDisplay.setPercentage(rolling_percent);
                    score = new_score;
                }
                // Decrease our spawn rate
                spawn_rate += ADJ_SPAWN_RATE;
                if (spawn_rate > MAX_SPAWN_RATE) {
                    spawn_rate = MAX_SPAWN_RATE;
                }
				iterator.remove();
			}else{
                // And our bad guy gets closer!
				final float x = (float)Math.cos(mytheta)*4.0f*mytime;
				final float y = (float)Math.sin(mytheta)*4.0f*mytime;
                this_badguy.setOffset(x, y);
                this_badguy.setRotation(theta * 100.0f + mytheta, 0.0f, 1.0f, 0.0f);
                this_badguy.setScale(0.8f*mytime);
                this_badguy.draw(projection);
			}
		}

		// Draw the HUD, what little one we do have...
		moreText.draw(projection);
		scoreText.draw(projection);
        scoreDisplay.draw(projection);
	}

	private MGEarth createEarth()
    {
        return new MGEarth(earth_model);
    }

	private MGText createText(String txt, float x, float y, float z, float size)
	{
		return new MGText(textList, pitchList, txt, x, y, z, size);
	}

	private MGBadGuy createBadGuy()
	{
		return new MGBadGuy(badguy);
	}

	private MGNebulaSky createNebulaShader()
	{
		return new MGNebulaSky(nebulasky, starfield, starfieldshader);
	}

    public MGRollingInteger createRollingInteger(long number, float x, float y, float z, float size) {
        return new MGRollingInteger(textList, pitchList, number, x, y, z, size);
    }
}
