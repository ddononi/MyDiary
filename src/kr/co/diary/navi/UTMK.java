package kr.co.diary.navi;

import android.util.Log;

/**
 * UTMK transformation info
 *
 */
public class UTMK {
	public static String name = "UTMK";
	public static String datum = "GRS80";
	public static double lat0 = 38;
	public static double lng0 = 127.5;
	public static double a = 6378137;
	public static double b = 6356752.314140356;
	public static double falseNorthing = 2000000;
	public static double falseEasting = 1000000;
	public static double scaleFactor = 0.9996;

	public static double q = 3.141592653589793;
	public static double c = 1.5707963267948966;
	public static double r = 6.283185307179586;
	public static double u = 180 / q;
	public static double s = q / 180;
	public static double d = 1.0026;
	public static double n = 0.3826834323650898;
	public static double o = 1e-10;
	public static double f = 6378137;
	public static double m = 0.00000484813681109536;

	public static double a2;
	public static double b2;
	public static double es;
	public static double e;
	public static double ep2;

	public static double e0;
	public static double e1;
	public static double e2;
	public static double e3;
	public static double ml0;

	static {
		init();
	}

	public static void init() {

		Log.d("UTMK", "UTMK is initialized.");

		lat0 *= s;
		lng0 *= s;

		a2 = a * a;
		b2 = b * b;
		es = (a2 - b2) / a2;
		e = Math.sqrt(es);
		ep2 = (a2 - b2) / b2;

		// if(datum_params&&datum_params.length>3){
		// datum_params[3]*=m;
		// datum_params[4]*=m;
		// datum_params[5]*=m;
		// datum_params[6]=(datum_params[6]/1000000)+1;
		// }

		e0 = v(es);
		e1 = l(es);
		e2 = b(es);
		e3 = p(es);
		ml0 = a * e(e0, e1, e2, e3, lat0);

		Log.d("UTMK", "u value : " + u);

	}

	public static double v(final double z) {
		return (1 - 0.25 * z * (1 + z / 16 * (3 + 1.25 * z)));
	}

	public static double l(final double z) {
		return (0.375 * z * (1 + 0.25 * z * (1 + 0.46875 * z)));
	}

	public static double b(final double z) {
		return (0.05859375 * z * z * (1 + 0.75 * z));
	}

	public static double p(final double z) {
		return (z * z * z * (35 / 3072));
	}

	public static double e(final double D, final double C, final double B, final double A, final double z) {
		return (D * z - C * Math.sin(2 * z) + B * Math.sin(4 * z) - A
				* Math.sin(6 * z));
	}

	public static double t(final double z) {
		if (z < 0) {
			return (-1);
		} else {
			return (1);
		}
	}

	public static double g(final double z) {
		if (Math.abs(z) < q) {
			return z;
		} else {
			return z - (t(z) * r);
		}
	}

}