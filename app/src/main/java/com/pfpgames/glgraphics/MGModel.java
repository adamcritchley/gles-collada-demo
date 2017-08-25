package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.MatrixStack;
import com.pfpgames.glmath.Vector2;
import com.pfpgames.glmath.Vector4;

public abstract class MGModel implements MGObject {
	private transient Matrix4 translateMatrix = null;
    private transient Matrix4 scaleMatrix = null;
    private transient Matrix4 rotateMatrix = null;
    private transient Matrix4 curProj = null;
	private transient Matrix4 curView = null;
    private transient MatrixStack ms = new MatrixStack();
	protected transient RenderedModel model = null;

	protected float curX = 0.0f;
	protected float curY = 0.0f;
	private boolean dirtyView = false;
	private float curDepth = 0.0f;
	private float curScale = 0.0f;
	private float curRotateAngle = 0.0f;
	private float curRotateX = 0.0f;
	private float curRotateY = 0.0f;
	private float curRotateZ = 0.0f;
	protected float vDot = 0.0f;
	protected float vAngle = 0.0f;
	protected Vector2 vDirection;
	private boolean model_active;

	public MGModel(RenderedModel m, float depth, float scale)
	{
		model = m;
		ms = new MatrixStack();
		curDepth = depth;
		curScale = scale;
		model_active = false;

		translateMatrix = Matrix4.translate(0, 0, depth);
		scaleMatrix = Matrix4.scale(scale, scale, scale);
		rotateMatrix = Matrix4.identity();
		curProj = Matrix4.identity();
		curView = Matrix4.identity();
		dirtyView = true;
	}

    private void bakeView(){
        if( dirtyView ) {
            ms.add(translateMatrix);
            ms.add(scaleMatrix);
            ms.add(rotateMatrix);
            curView = ms.collapse();
            ms.clear();
            dirtyView = false;
        }
    }

	@Override
	public void setOffset(float x, float y){
		curX = x;
		curY = y;
		Matrix4.setIdentity(translateMatrix);
        translateMatrix.translateThis(x, y, curDepth);
        dirtyView = true;
	}

	@Override
    public void setDepth(float depth){
        curDepth = depth;
        Matrix4.setIdentity(translateMatrix);
        translateMatrix.translateThis(curX, curY, depth);
        dirtyView = true;
    }

	@Override
    public void setScale(float scale){
        curScale = scale;
        Matrix4.setIdentity(scaleMatrix);
        scaleMatrix.scaleThis(scale, scale, scale);
        dirtyView = true;
    }

    public void setRotation(float degrees, float x, float y, float z){
		curRotateAngle = degrees;
		curRotateX = x;
		curRotateY = y;
		curRotateZ = z;
        Matrix4.setIdentity(rotateMatrix);
        rotateMatrix.rotateThis(degrees, x, y, z);
        dirtyView = true;
    }

	@Override
	public float getX(){
		return curX;
	}

	@Override
	public float getY(){
		return curY;
	}

	@Override
	public float getDepth(){
		return curDepth;
	}

	@Override
	public float getScale(){
		return curScale;
	}

    public float getVelocity(){
        return vDot;
    }

	@Override
	public void draw(Matrix4 projection) {
        bakeView();
		curProj = projection;

		model.drawTriangles(projection, curView);
	}

	protected boolean hitTestBox(float vx, float vy) {
        bakeView();
		return model.hitTestBox(vx, vy, curProj, curView);
	}
	
	protected boolean hitTestSphere(float vx, float vy) {
        bakeView();
		return model.hitTestSphere(vx, vy, curProj, curView);
	}

	public abstract boolean hitTest(float vx, float vy);

	public void setVelocity(float velocity, Vector2 direction){
		vDot = velocity;
		vDirection = direction;
		if( direction != null ) {
			vAngle = (float) (Math.atan2(direction.x(), -direction.y()) - Math.PI / 2.0f);
			if (vAngle < 0.0f) {
				vAngle += (Math.PI * 2.0f);
			}
		}else{
			vAngle = 0.0f;
		}
	}

	public Vector4 getDefaultColor() {
		return model.getDefaultColor();
	}

	public void setDefaultColor(Vector4 color) {
		model.setDefaultColor(color);
	}

	public Vector4 getMaxBounds(){
		bakeView();
		return Vector4.Multiply(curView, model.getMaxBounds());
	}

	public Vector4 getMinBounds(){
		bakeView();
		return Vector4.Multiply(curView, model.getMinBounds());
	}

	@Override
	public void activate(float time)
	{
		model_active = true;
	}

	@Override
	public void deactivate(float time)
	{
		model_active = false;
	}

	@Override
	public boolean isActive()
	{
		return model_active;
	}
}
