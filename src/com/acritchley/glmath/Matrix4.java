package com.acritchley.glmath;

import android.opengl.Matrix;

public class Matrix4 {
	private static final int MATRIX_DIM = 4;
	public float[] matrix = new float[MATRIX_DIM*MATRIX_DIM];

	public Matrix4(){
	}

	public void scaleThis(float x, float y, float z){
		Matrix.scaleM(this.matrix, 0, x, y, z);
	}

	public void rotateThis(float degrees, float x, float y, float z){
		Matrix.rotateM(this.matrix, 0, degrees, x, y, z);
	}

	public void translateThis(float x, float y, float z){
		Matrix.translateM(this.matrix, 0, x, y, z);
	}

	public static void setIdentity(Matrix4 m){
		Matrix.setIdentityM(m.matrix, 0);
	}

	public static Matrix4 add(Matrix4 lhs, Matrix4 rhs){
		Matrix4 result = new Matrix4();
	
		for( int n=0; n<MATRIX_DIM; n++){
			for( int m=0; m<MATRIX_DIM; m++){
				result.matrix[n+(m*MATRIX_DIM)] =
						lhs.matrix[n+(m*MATRIX_DIM)]+
						rhs.matrix[n+(m*MATRIX_DIM)];
			}
		}
		
		return result;
	}

	public static Matrix4 subtract(Matrix4 lhs, Matrix4 rhs){
		Matrix4 result = new Matrix4();
	
		for( int n=0; n<MATRIX_DIM; n++){
			for( int m=0; m<MATRIX_DIM; m++){
				result.matrix[n+(m*MATRIX_DIM)] =
						lhs.matrix[n+(m*MATRIX_DIM)]-
						rhs.matrix[n+(m*MATRIX_DIM)];
			}
		}
		
		return result;
	}

	public static Matrix4 multiply(float a, Matrix4 b){
		Matrix4 result = new Matrix4();
		
		for( int n=0; n<MATRIX_DIM; n++){
			for( int m=0; m<MATRIX_DIM; m++){
				result.matrix[n+(m*MATRIX_DIM)] =
						a*b.matrix[n+(m*MATRIX_DIM)];
			}
		}
		
		return result;
	}

	public static Matrix4 multiply(Matrix4 lhs, Matrix4 rhs){
		Matrix4 result = new Matrix4();

		Matrix.multiplyMM(result.matrix, 0, lhs.matrix, 0, rhs.matrix, 0);
		
		return result;
	}
	
	public static Matrix4 rotate(float degrees, float x, float y, float z){
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		Matrix.rotateM(result.matrix, 0, degrees, x, y, z);

		return result;
	}
	
	public static Matrix4 translate(float x, float y, float z){
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		Matrix.translateM(result.matrix, 0, x, y, z);

		return result;
	}
	
	public static Matrix4 scale(float x, float y, float z){
		Matrix4 result = new Matrix4();
		Matrix4.setIdentity(result);

		Matrix.scaleM(result.matrix, 0, x, y, z);

		return result;
	}
	
	public static Matrix4 frustum(float left, float right, float bottom, float top, float near, float far){
		Matrix4 result = new Matrix4();
		//Matrix4.setIdentity(result);
		
		Matrix.frustumM(result.matrix, 0, left, right, bottom, top, near, far);
/*
		result.matrix[0] = 2.0f*near/(right-left);
		result.matrix[2] = (right+left)/(right-left);
		result.matrix[5] = 2.0f*near/(top-bottom);
		result.matrix[6] = (top+bottom)/(top-bottom);
		result.matrix[10] = (-far-near)/(far-near);
		result.matrix[11] = -2.0f*far*near/(far-near);
		result.matrix[14] = -1.0f;
		result.matrix[15] = 0.0f;
*/
		return result;
	}
	
	public static Matrix4 orthographic(float left, float right, float bottom, float top, float near, float far){
		Matrix4 result = new Matrix4();
		//Matrix4.setIdentity(result);

		Matrix.orthoM(result.matrix, 0, left, right, bottom, top, near, far);
/*
		result.matrix[0] = 2.0f/(right-left);
		result.matrix[3] = -((right+left)/(right-left));
		result.matrix[5] = 2.0f/(top-bottom);
		result.matrix[7] = -((top+bottom)/(top-bottom));
		result.matrix[10] = -2.0f/(far-near);
		result.matrix[11] = -((far+near)/(far-near));
*/
		
		return result;
	}
	
	public static Matrix4 invert(Matrix4 m){
		Matrix4 result = new Matrix4();
		Matrix.invertM(result.matrix, 0, m.matrix, 0);
		return result;
	}
	/*
	static Ray convert_normalized_2D_point_to_ray(float normalized_x, float normalized_y) {
	    // We'll convert these normalized device coordinates into world-space
	    // coordinates. We'll pick a point on the near and far planes, and draw a
	    // line between them. To do this transform, we need to first multiply by
	    // the inverse matrix, and then we need to undo the perspective divide.
	    vec4 near_point_ndc = {normalized_x, normalized_y, -1, 1};
	    vec4 far_point_ndc = {normalized_x, normalized_y,  1, 1};
	 
	    vec4 near_point_world, far_point_world;
	    mat4x4_mul_vec4(near_point_world, inverted_view_projection_matrix, near_point_ndc);
	    mat4x4_mul_vec4(far_point_world, inverted_view_projection_matrix, far_point_ndc);
	 
	    // Why are we dividing by W? We multiplied our vector by an inverse
	    // matrix, so the W value that we end up is actually the *inverse* of
	    // what the projection matrix would create. By dividing all 3 components
	    // by W, we effectively undo the hardware perspective divide.
	    divide_by_w(near_point_world);
	    divide_by_w(far_point_world);
	 
	    // We don't care about the W value anymore, because our points are now
	    // in world coordinates.
	    vec3 near_point_ray = {near_point_world[0], near_point_world[1], near_point_world[2]};
	    vec3 far_point_ray = {far_point_world[0], far_point_world[1], far_point_world[2]};
	    vec3 vector_between;
	    vec3_sub(vector_between, far_point_ray, near_point_ray);
	    return (Ray) {
	        {near_point_ray[0], near_point_ray[1], near_point_ray[2]},
	        {vector_between[0], vector_between[1], vector_between[2]}};
	}*/
	/*
	 * static inline int sphere_intersects_ray(Sphere sphere, Ray ray) {
    if (distance_between(sphere.center, ray) < sphere.radius)
        return 1;
    return 0;
}
 
static inline float distance_between(vec3 point, Ray ray) {
    vec3 p1_to_point;
    vec3_sub(p1_to_point, point, ray.point);
    vec3 p2_to_point;
    vec3 translated_ray_point;
    vec3_add(translated_ray_point, ray.point, ray.vector);
    vec3_sub(p2_to_point, point, translated_ray_point);
 
    // The length of the cross product gives the area of an imaginary
    // parallelogram having the two vectors as sides. A parallelogram can be
    // thought of as consisting of two triangles, so this is the same as
    // twice the area of the triangle defined by the two vectors.
    // http://en.wikipedia.org/wiki/Cross_product#Geometric_meaning
    vec3 cross_product;
    vec3_mul_cross(cross_product, p1_to_point, p2_to_point);
    float area_of_triangle_times_two = vec3_len(cross_product);
    float length_of_base = vec3_len(ray.vector);
 
    // The area of a triangle is also equal to (base * height) / 2. In
    // other words, the height is equal to (area * 2) / base. The height
    // of this triangle is the distance from the point to the ray.
    float distance_from_point_to_ray = area_of_triangle_times_two / length_of_base;
    return distance_from_point_to_ray;
}*/
}
