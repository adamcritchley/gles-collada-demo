package com.pfpgames.glgraphics.gl2es;

import android.content.res.AssetManager;
import android.opengl.GLES20;

import com.pfpgames.collada.ColladaMatrix4f;
import com.pfpgames.collada.ColladaModel;
import com.pfpgames.collada.ColladaShader;
import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.Vector4;
import com.pfpgames.glgraphics.RenderedModel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class Gl2Model implements ColladaModel, RenderedModel {
	private AssetManager assetManager;
	private ArrayList<Gl2Mesh> meshes = new ArrayList<Gl2Mesh>();
	private boolean offscreen = false;
	private int frameBuffer;
	private int depthBuffer;
	private int renderTex;
	private int offscreenWidth;
	private int offscreenHeight;
	private boolean tuv = false;
	private boolean flipU = false;
	private boolean flipV = false;
    private Vector4 default_color = new Vector4(0.75f, 0.75f, 0.75f, 1.0f);

    // Store the min and max bounds for the entire model in world space
	private Vector4 modelMinBounds = new Vector4();
	private Vector4 modelMaxBounds = new Vector4();

    @Override
	public Gl2Mesh createMesh(){
		Gl2Mesh newMesh = new Gl2Mesh(assetManager);
		meshes.add(newMesh);
		return newMesh;
	}

	public void setTransposeTextureUV(boolean transpose_uv){
		tuv = transpose_uv;
	}
	public void setFlipTextureU(boolean flip_u){
		flipU = flip_u;
	}
	public void setFlipTextureV(boolean flip_v){
		flipV = flip_v;
	}

	@Override
	public boolean getTransposeTextureUV(){
		return tuv;
	}

	@Override
	public boolean getFlipTextureU(){
		return flipU;
	}

	@Override
	public boolean getFlipTextureV(){
		return flipV;
	}

	public int size() {
		int msize = 0;
		for( Gl2Mesh mesh : meshes ){
			msize += mesh.size();
		}
		return msize;
	}

	public void drawTriangles(Matrix4 proj, Matrix4 view) {
		if( offscreen  ){
			enableOffscreen();
		}
		
		for( Gl2Mesh mesh : meshes ){
			mesh.drawTriangles(proj, view);
		}
		
		if( offscreen  ){
			disableOffscreen();
		}
	}

	public void drawPoints(Matrix4 proj, Matrix4 view) {
		if( offscreen  ){
			enableOffscreen();
		}
		
		for( Gl2Mesh mesh : meshes ){
			mesh.drawPoints(proj, view);
		}
		
		if( offscreen  ){
			disableOffscreen();
		}
	}

	public void buildModel() {
		for( Gl2Mesh mesh : meshes ){
			mesh.buildMesh();
            mesh.setDefaultColor(default_color);
		}

		computeBounds();
	}

	public void buildModel(ColladaShader shader) {
		for( Gl2Mesh mesh : meshes ){
			mesh.buildMesh(shader);
            mesh.setDefaultColor(default_color);
		}

		computeBounds();
	}

	public Vector4 getMaxBounds(){
		return modelMaxBounds;
	}

	public Vector4 getMinBounds(){
		return modelMinBounds;
	}

	private void computeBounds(){
		float minX;
		float maxX;
		float minY;
		float maxY;
		float minZ;
		float maxZ;
		Iterator<Gl2Mesh> iter = meshes.iterator();

		// Is there at least on mesh?
		if( iter.hasNext() ){
			Gl2Mesh mesh = iter.next();
			Vector4 maxb = mesh.getMaxBounds();
			Vector4 minb = mesh.getMinBounds();
			maxX = maxb.x();
			maxY = maxb.y();
			maxZ = maxb.z();
			minX = minb.x();
			minY = minb.y();
			minZ = minb.z();

			while( iter.hasNext() ) {
				mesh = iter.next();
				maxb = mesh.getMaxBounds();
				minb = mesh.getMinBounds();
				if (maxb.x() > maxX) {
					maxX = maxb.x();
				}
				if (maxb.y() > maxY) {
					maxY = maxb.y();
				}
				if (maxb.z() > maxZ) {
					maxZ = maxb.z();
				}
				if (minb.x() < minX) {
					minX = minb.x();
				}
				if (minb.y() < minY) {
					minY = minb.y();
				}
				if (minb.z() < minZ) {
					minZ = minb.z();
				}
			}

			modelMaxBounds.setThis(maxX, maxY, maxZ, 1.0f);
			modelMinBounds.setThis(minX, minY, minZ, 1.0f);
		}
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	@Override
	public void translate(float x, float y, float z) {
		for( Gl2Mesh mesh : meshes ){
			mesh.translate(x, y, z);
		}
	}

	@Override
	public void rotate(float x, float y, float z, float a) {
		for( Gl2Mesh mesh : meshes ){
			mesh.rotate(x, y, z, a);
		}
	}

	@Override
	public void scale(float x, float y, float z) {
		for( Gl2Mesh mesh : meshes ){
			mesh.scale(x, y, z);
		}
	}

	@Override
	public void transform(ColladaMatrix4f m) {
		for( Gl2Mesh mesh : meshes ){
			mesh.transform(m);
		}
	}

	public boolean hitTestBox(float vx, float vy, Matrix4 proj, Matrix4 view){
		for( Gl2Mesh mesh : meshes ){
			if( mesh.hitTestBox(vx, vy, proj, view) ){
				return true;
			}
		}

		return false;
	}

	public boolean hitTestSphere(float vx, float vy, Matrix4 proj, Matrix4 view){
		for( Gl2Mesh mesh : meshes ){
			if( mesh.hitTestSphere(vx, vy, proj, view) ){
				return true;
			}
		}

		return false;
	}

    @Override
    public void setDefaultColor(Vector4 color) {
        default_color = color;

        for( Gl2Mesh mesh : meshes ){
            mesh.setDefaultColor(color);
        }
    }

    @Override
    public Vector4 getDefaultColor() {
        return default_color;
    }

	public int getOffscreenTexture() {
		return renderTex;
	}

	public void renderOffscreen(int renderWidth, int renderHeight) {
		offscreenWidth = renderWidth;
		offscreenHeight = renderHeight;

		depthBuffer = Gl2Mesh.glGenRenderbuffer();
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, renderWidth, renderHeight);

		renderTex = Gl2Mesh.glGenTexture();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, renderWidth, renderHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (ByteBuffer)null);

		frameBuffer = Gl2Mesh.glGenFramebuffer();
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex, 0);

		if( GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) == GLES20.GL_FRAMEBUFFER_COMPLETE){
			offscreen = true;
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

	private void enableOffscreen() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glViewport(0, 0, offscreenWidth, offscreenHeight);
	}

	private void disableOffscreen() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

    public void setHitMargin(float margin) {
        for( Gl2Mesh mesh : meshes ){
            mesh.setHitMargin(margin);
        }
    }
}
