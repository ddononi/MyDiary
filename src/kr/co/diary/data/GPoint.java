package kr.co.diary.data;

import java.io.Serializable;

/**
 *	���� ��ǥ x,y
 */
public class GPoint implements Serializable {

	private static final long serialVersionUID = -1171376646814264988L;

	public double x;

	public double y;

	public double z;

	/**
	 * Constructor
	 */
	public GPoint() {
		super();
	}

	/**
	 * Constructor
	 */
	public GPoint(final double x, final double y) {
		this.x = x;
		this.y = y;
		this.z = 0.0D;
	}

	/**
	 * Constructor
	 */
	public GPoint(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(final double y) {
		this.y = y;
	}

}