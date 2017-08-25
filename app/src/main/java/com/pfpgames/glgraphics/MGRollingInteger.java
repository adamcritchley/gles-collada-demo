package com.pfpgames.glgraphics;

import com.pfpgames.glmath.Matrix4;

public class MGRollingInteger implements MGObject {
	private static final int MAX_DIGITS = 10;
	private final float[] text_pitch;
	private long max_number;
	private float origin_x;
	private float origin_y;
	private float origin_z;
	private float scale_size;
	private long cur_value;
	private float max_width;
	private MGCharacter[] digits = new MGCharacter[MAX_DIGITS];

	public MGRollingInteger(RenderedModel[] textList, float[] pitchList, long number,
						 float x, float y, float z, float size) {
		text_pitch = pitchList;
		origin_x = x;
		origin_y = y;
		origin_z = z;
		scale_size = size;
		cur_value = 0;
		max_number = number;
		for(int i=0; i<MAX_DIGITS; i++){
			final int cval = '0' + i;
			final float this_pitch = text_pitch[cval] * scale_size;
			MGCharacter new_char = new MGCharacter(textList[cval], this_pitch, z);
			digits[i] = new_char;
		}

        calcMaxWidth();
	}

	public long getMaxNumber(){
		return max_number;
	}

	public void setMaxNumber(long number){
		max_number = number;
		calcMaxWidth();
	}

    private void calcMaxWidth(){
        long drawn_value = max_number;
		max_width = 0.0f;
        do{
            int this_value = 0;
            if( drawn_value > 0 ){
                this_value = (int)(drawn_value % 10);
            }
            final int cval = '0' + this_value;
            final float this_pitch = text_pitch[cval] * scale_size;
            max_width += this_pitch;
            digits[this_value].setPitch(this_pitch);
            drawn_value /= 10;
        }while( drawn_value > 0 );
    }

	public float getWidth(){ return max_width; }

	@Override
	public void draw(Matrix4 projection) {
		float cum_pitch = 0.0f;
		long drawn_value = cur_value;
		do{
			int this_value = 0;
            if( drawn_value > 0 ){
                this_value = (int)(drawn_value % 10);
            }
			cum_pitch += digits[this_value].getPitch() * scale_size;
			digits[this_value].setOffset(origin_x + max_width - cum_pitch, origin_y);
			digits[this_value].setDepth(origin_z);
			digits[this_value].setScale(scale_size);
			digits[this_value].draw(projection);
			drawn_value /= 10;
		}while( drawn_value > 0 );
	}

	@Override
	public boolean update(float time, float max_time) {
		cur_value = (long)((time/max_time) * max_number);
		return false;
	}

	public void setPercentage(float percent) {
		cur_value = (long)(percent * max_number);
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

	@Override
	public void setOffset(float x, float y) {
		origin_x = x;
		origin_y = y;
	}

	@Override
	public void setDepth(float z) {
		origin_z = z;
	}

	@Override
	public void setScale(float scale) {
		scale_size = scale;
        calcMaxWidth();
	}

	@Override
	public float getX() {
		return origin_x;
	}

	@Override
	public float getY() {
		return origin_y;
	}

	@Override
	public float getDepth() {
		return origin_z;
	}

	@Override
	public float getScale() {
		return scale_size;
	}

}
