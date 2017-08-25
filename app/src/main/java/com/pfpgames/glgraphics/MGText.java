package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;
import com.pfpgames.glmath.Vector4;

import java.util.ArrayList;

public class MGText implements MGObject{
	protected final float[] text_pitch;
	protected final RenderedModel[] text_models;
	protected String displayed_text;
	protected Vector4 text_color = null;
	protected ArrayList<MGCharacter> chars = null;
	protected Matrix4 curProj = null;
	protected float text_width;
	protected float origin_x = 0.0f;
	protected float origin_y = 0.0f;
	protected float origin_z = 0.0f;
	protected float scale_size = 0.0f;

	public MGText(RenderedModel[] textList, float[] pitchList, String text, float x, float y, float z, float size){
		displayed_text = text;
		text_models = textList;
		text_pitch = pitchList;
		origin_x = x;
		origin_y = y;
		origin_z = z;
		scale_size = size;
		curProj = Matrix4.identity();
		buildString();
	}

	private void buildString(){
		float cum_pitch = 0.0f;
		chars = new ArrayList<>(displayed_text.length());
		for(int i=0; i<displayed_text.length(); i++){
			int cval = displayed_text.charAt(i);
			float this_pitch = 0.0f;
			if( text_pitch != null ){
				this_pitch = text_pitch[cval] * scale_size;
			}
			if( cval == ' ' ) {
				cum_pitch += this_pitch;
			}else {
				MGCharacter new_char = new MGCharacter(text_models[cval], this_pitch, origin_z);
				new_char.setOffset(origin_x + cum_pitch, origin_y);
				new_char.setScale(scale_size);
				cum_pitch += new_char.getPitch();
				chars.add(new_char);
			}
		}
		text_width = cum_pitch;
	}

	private void refreshString(){
		float cum_pitch = 0.0f;
		int str_idx = 0;
		for(int i=0; i<displayed_text.length(); i++) {
			int cval = displayed_text.charAt(i);
			float this_pitch = 0.0f;
			if (text_pitch != null) {
				this_pitch = text_pitch[cval] * scale_size;
			}
			if (cval == ' ') {
				cum_pitch += this_pitch;
			} else {
				MGCharacter c = chars.get(str_idx);
				c.setPitch(this_pitch);
				c.setOffset(origin_x + cum_pitch, origin_y);
				c.setScale(scale_size);
				cum_pitch += c.getPitch();
				str_idx++;
			}
		}
		text_width = cum_pitch;
	}

	public float getX(){
		return origin_x;
	}

	public float getY(){
		return origin_y;
	}

	public float getDepth(){
		return origin_z;
	}

	@Override
	public float getScale() {
		return scale_size;
	}

	public boolean hitTest(float vx, float vy){
		return (this.hitTestCharacter(vx, vy) != null);
	}

	public MGCharacter hitTestCharacter(float vx, float vy){
		for( MGCharacter c : chars){
			if (c.hitTest(vx, vy)) {
				return c;
			}
		}

		return null;
	}

	public float getWidth(){
		return text_width;
	}

	@Override
	public void setOffset(float x, float y) {
		origin_x = x;
		origin_y = y;
		refreshString();
	}

	@Override
	public void setDepth(float z) {
		origin_z = z;
		for( MGCharacter c : chars){
			c.setDepth(z);
		}
		refreshString();
	}

	@Override
	public void setScale(float scale) {
		scale_size = scale;
		refreshString();
	}

	@Override
	public void draw(Matrix4 projection) {
		curProj = projection;

		for( MGCharacter c : chars){
			Vector4 default_color = null;
			if( text_color != null ) {
				default_color = c.getDefaultColor();
				c.setDefaultColor(text_color);
			}
			c.draw(projection);
			if( default_color != null ) {
				c.setDefaultColor(default_color);
			}
		}
	}

	@Override
	public boolean update(float time, float phase_scale) {
		return false;
	}

	@Override
	public void activate(float time) {

	}

	@Override
	public void deactivate(float time) {

	}

	@Override
	public boolean isActive() {
		return false;
	}

	public void setColor(final Vector4 color) {
		text_color = color;
	}

	public String getText() {
		return displayed_text;
	}

	public void setText(String text){
		displayed_text = text;
		buildString();
	}
}
