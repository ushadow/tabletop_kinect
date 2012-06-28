package edu.mit.yingyin.util;

import java.awt.Point;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class Geometry {
	/**
	 * Calculates the euler angles from the quaternion, positive direction of the 
	 * rotaion follows the right hand rule: if the thumb of the right hand is 
	 * pointed in the direction of the axis, the positive direction of rotation is 
	 * given by the curl of the fingers.
	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm
	 * @param q1 quaternion has to be normalized
	 * @return Tuple3f.x = roll (rotation about x axis)
	 * 		   Tuple3f.y = yaw (rotation about y axis)
	 * 		   Tuple3f.z = pitch (rotation about z axis)
	 */
	static public Tuple3f getEulerAngles(Quat4f q1) {
		/** assumes q1 is a normalised quaternion */
		Tuple3f tuple3f = new Vector3f();
		float heading, attitude, bank;
		double test = q1.x * q1.y + q1.z * q1.w;
		if (test > 0.499) { // singularity at north pole
			heading = (float)(2 * Math.atan2(q1.x, q1.w));
			attitude = (float)Math.PI / 2;
			bank = 0;
		} else if (test < -0.499) { // singularity at south pole
			heading = (float)(-2 * Math.atan2(q1.x, q1.w));
			attitude = -(float)Math.PI / 2;
			bank = 0;
		} else {
			double sqx = q1.x * q1.x;
	    double sqy = q1.y * q1.y;
	    double sqz = q1.z * q1.z;
	    heading = (float)Math.atan2(2 * q1.y * q1.w - 2 * q1.x * q1.z, 
	                                1 - 2 * sqy - 2 * sqz);
			attitude = (float)Math.asin(2 * test);
			bank = (float)Math.atan2(2 * q1.x * q1.w - 2 * q1.y * q1.z, 
			                         1 - 2 * sqx - 2 * sqz);
		}
		tuple3f.x = bank;
		tuple3f.y = heading;
		tuple3f.z = attitude;
		return tuple3f;
	}
	
	/** 
	 * Calculates angle C of triangle ABC.
	 * 
	 * @param A Point A of the triangle.
	 * @param B Point B of the triangle.
	 * @param C Point C of the triangle.
	 * @return angle of C.
	 */
	static public double getAngleC(Point A, Point B, Point C) {
	  Point vCA = new Point(A.x - C.x, A.y - C.y);
	  double distSqCA = vCA.distanceSq(0, 0);
	  if (distSqCA == 0)
	    return 0;
	  Point vCB = new Point(B.x - C.x, B.y - C.y);
	  double distSqCB = vCB.distanceSq(0, 0);
	  if (distSqCB == 0)
	    return 0;
	  // Cosine rule: c^2 = a^2 + b^2 - 2abcosC
	  return Math.acos((vCA.x * vCB.x + vCA.y * vCB.y) / 
	      Math.sqrt(distSqCA * distSqCB));
	}
}
