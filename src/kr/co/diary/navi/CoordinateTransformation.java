package kr.co.diary.navi;


/**
 *	��ǥ ��ȯ Ŭ����
 *	TM, TM128, WGS84
 *
 *	<h3>��뿹</3>
 *	<pre>
 * 	GPoint pnt = new GPoint(127.299732, 36.4432564);
 * 	GPoint outPoint = CoordinateTransformation.convert(CoordinateTransformation.TM128, CoordinateTransformation.WGS84, pnt);
 * 	printf --> outPoint.getX(), outPoint.getY()
 *  </pre>
 *
 */
public class CoordinateTransformation {

    public static final int WGS84 = 0;
    public static final int TM128 = 1;
    public static final int TM = 2;


    private static double[] m_Ind = new double[3];
    private static double[] m_Es = new double[3];
    private static double[] m_Esp = new double[3];
    private static double[] src_m = new double[3];
    private static double[] dst_m = new double[3];

    private static double EPSLN = 0.0000000001;
    private static double[] m_arMajor = new double[3];
    private static double[] m_arMinor = new double[3];

    private static double[] m_arScaleFactor = new double[3];
    private static double[] m_arLonCenter = new double[3];
    private static double[] m_arLatCenter = new double[3];
    private static double[] m_arFalseNorthing = new double[3];
    private static double[] m_arFalseEasting = new double[3];

    private static double[] datum_params = new double[3];

    static {
        m_arScaleFactor[WGS84] = 1;
        m_arLonCenter[WGS84] = 0.0;
        m_arLatCenter[WGS84] = 0.0;
        m_arFalseNorthing[WGS84] = 0.0;
        m_arFalseEasting[WGS84] = 0.0;
        m_arMajor[WGS84] = 6378137.0;
        m_arMinor[WGS84] = 6356752.3142;


        m_arScaleFactor[TM128] = 0.9996;//0.9999;
        m_arLonCenter[TM128] = 2.22529479629277; // 127.5
        //m_arLonCenter[KATEC] = 2.23402144255274; // 128
        m_arLatCenter[TM128] = 0.663225115757845;
        m_arFalseNorthing[TM128] = 600000.0;
        m_arFalseEasting[TM128] = 400000.0;
        m_arMajor[TM128] = 6377397.155;
        m_arMinor[TM128] = 6356078.9633422494;


        m_arScaleFactor[TM] = 1.0;
        //this.m_arLonCenter[TM] = 2.21656815003280; // 127
        m_arLonCenter[TM] = 2.21661859489671; // 127.+10.485 minute
        m_arLatCenter[TM] = 0.663225115757845;
        m_arFalseNorthing[TM] = 500000.0;
        m_arFalseEasting[TM] = 200000.0;
        m_arMajor[TM] = 6377397.155;
        m_arMinor[TM] = 6356078.9633422494;

        datum_params[0] = -146.43;
        datum_params[1] = 507.89;
        datum_params[2] = 681.46;

        double tmp = m_arMinor[WGS84] / m_arMajor[WGS84];
        m_Es[WGS84] = 1.0 - tmp * tmp;
        m_Esp[WGS84] = m_Es[WGS84] / (1.0 - m_Es[WGS84]);

        if (m_Es[WGS84] < 0.00001) {
            m_Ind[WGS84] = 1.0;
        } else {
            m_Ind[WGS84] = 0.0;
        }

        tmp = m_arMinor[TM128] / m_arMajor[TM128];
        m_Es[TM128] = 1.0 - tmp * tmp;
        m_Esp[TM128] = m_Es[TM128] / (1.0 - m_Es[TM128]);

        if (m_Es[TM128] < 0.00001) {
            m_Ind[TM128] = 1.0;
        } else {
            m_Ind[TM128] = 0.0;
        }

        tmp = m_arMinor[TM] / m_arMajor[TM];
        m_Es[TM] = 1.0 - tmp * tmp;
        m_Esp[TM] = m_Es[TM] / (1.0 - m_Es[TM]);

        if (m_Es[TM] < 0.00001) {
            m_Ind[TM] = 1.0;
        } else {
            m_Ind[TM] = 0.0;
        }

        src_m[WGS84] = m_arMajor[WGS84] * mlfn(e0fn(m_Es[WGS84]), e1fn(m_Es[WGS84]), e2fn(m_Es[WGS84]), e3fn(m_Es[WGS84]), m_arLatCenter[WGS84]);
        dst_m[WGS84] = m_arMajor[WGS84] * mlfn(e0fn(m_Es[WGS84]), e1fn(m_Es[WGS84]), e2fn(m_Es[WGS84]), e3fn(m_Es[WGS84]), m_arLatCenter[WGS84]);
        src_m[TM128] = m_arMajor[TM128] * mlfn(e0fn(m_Es[TM128]), e1fn(m_Es[TM128]), e2fn(m_Es[TM128]), e3fn(m_Es[TM128]), m_arLatCenter[TM128]);
        dst_m[TM128] = m_arMajor[TM128] * mlfn(e0fn(m_Es[TM128]), e1fn(m_Es[TM128]), e2fn(m_Es[TM128]), e3fn(m_Es[TM128]), m_arLatCenter[TM128]);
        src_m[TM] = m_arMajor[TM] * mlfn(e0fn(m_Es[TM]), e1fn(m_Es[TM]), e2fn(m_Es[TM]), e3fn(m_Es[TM]), m_arLatCenter[TM]);
        dst_m[TM] = m_arMajor[TM] * mlfn(e0fn(m_Es[TM]), e1fn(m_Es[TM]), e2fn(m_Es[TM]), e3fn(m_Es[TM]), m_arLatCenter[TM]);
    }

    private static double D2R(final double degree) {
        return degree* Math.PI / 180.0;
    }

    private static double R2D(final double radian) {
        return radian * 180.0 / Math.PI;
    }

    private static double e0fn(final double x) {
        return 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x));
    }

    private static double e1fn(final double x) {
        return 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x));
    }

    private static double e2fn(final double x) {
        return 0.05859375 * x * x * (1.0 + 0.75 * x);
    }

    private static double e3fn(final double x) {
        return x * x * x * (35.0 / 3072.0);
    }

    private static double mlfn(final double e0, final double e1, final double e2, final double e3, final double phi) {
        return e0 * phi - e1 * Math.sin(2.0 * phi) + e2 * Math.sin(4.0 * phi) - e3 * Math.sin(6.0 * phi);
    }

    private static double asinz(double value) {
        if (Math.abs(value) > 1.0) {
			value = (value > 0 ? 1: -1);
		}
        return Math.asin(value);
    }

    public static GPoint convert(final int srctype, final int dsttype, final GPoint in_pt) {
        GPoint tmpPt = new GPoint();
        GPoint out_pt = new GPoint();

        if (srctype == WGS84) {
            tmpPt.x = D2R(in_pt.x);
            tmpPt.y = D2R(in_pt.y);
        } else {
            tm2geo(srctype, in_pt, tmpPt);
        }

        if (dsttype == WGS84) {
            out_pt.x = R2D(tmpPt.x);
            out_pt.y = R2D(tmpPt.y);
        } else {
            geo2tm(dsttype, tmpPt, out_pt);
        }

        return out_pt;
    }

    public static void geo2tm(final int dsttype, final GPoint in_pt, final GPoint out_pt) {
        double x, y;

        transform(WGS84, dsttype, in_pt);
        double delta_lon = in_pt.x - m_arLonCenter[dsttype];
        double sin_phi = Math.sin(in_pt.y);
        double cos_phi = Math.cos(in_pt.y);

        if (m_Ind[dsttype] != 0) {
            double b = cos_phi * Math.sin(delta_lon);

            if ((Math.abs(Math.abs(b) - 1.0)) < EPSLN) {

            }
        } else {
            double b = 0;
            x = 0.5 * m_arMajor[dsttype] * m_arScaleFactor[dsttype] * Math.log((1.0 + b) / (1.0 - b));
            double con = Math.acos(cos_phi * Math.cos(delta_lon) / Math.sqrt(1.0 - b * b));

            if (in_pt.y < 0) {
                con = con * -1;
                y = m_arMajor[dsttype] * m_arScaleFactor[dsttype] * (con - m_arLatCenter[dsttype]);
            }
        }

        double al = cos_phi * delta_lon;
        double als = al * al;
        double c = m_Esp[dsttype] * cos_phi * cos_phi;
        double tq = Math.tan(in_pt.y);
        double t = tq * tq;
        double con = 1.0 - m_Es[dsttype] * sin_phi * sin_phi;
        double n = m_arMajor[dsttype] / Math.sqrt(con);
        double ml = m_arMajor[dsttype] * mlfn(e0fn(m_Es[dsttype]), e1fn(m_Es[dsttype]), e2fn(m_Es[dsttype]), e3fn(m_Es[dsttype]), in_pt.y);

        out_pt.x = m_arScaleFactor[dsttype] * n * al * (1.0 + als / 6.0 * (1.0 - t + c + als / 20.0 * (5.0 - 18.0 * t + t * t + 72.0 * c - 58.0 * m_Esp[dsttype]))) + m_arFalseEasting[dsttype];
        out_pt.y = m_arScaleFactor[dsttype] * (ml - dst_m[dsttype] + n * tq * (als * (0.5 + als / 24.0 * (5.0 - t + 9.0 * c + 4.0 * c * c + als / 30.0 * (61.0 - 58.0 * t + t * t + 600.0 * c - 330.0 * m_Esp[dsttype]))))) + m_arFalseNorthing[dsttype];
    }


    public static void tm2geo(final int srctype, final GPoint in_pt, final GPoint out_pt) {
        GPoint tmpPt = new GPoint(in_pt.getX(), in_pt.getY());
        int max_iter = 6;

        if (m_Ind[srctype] != 0) {
            double f = Math.exp(in_pt.x / (m_arMajor[srctype] * m_arScaleFactor[srctype]));
            double g = 0.5 * (f - 1.0 / f);
            double temp = m_arLatCenter[srctype] + tmpPt.y / (m_arMajor[srctype] * m_arScaleFactor[srctype]);
            double h = Math.cos(temp);
            double con = Math.sqrt((1.0 - h * h) / (1.0 + g * g));
            out_pt.y = asinz(con);

            if (temp < 0) {
				out_pt.y *= -1;
			}

            if ((g == 0) && (h == 0)) {
                out_pt.x = m_arLonCenter[srctype];
            } else {
                out_pt.x = Math.atan(g / h) + m_arLonCenter[srctype];
            }
        }

        tmpPt.x -= m_arFalseEasting[srctype];
        tmpPt.y -= m_arFalseNorthing[srctype];

        double con = (src_m[srctype] + tmpPt.y / m_arScaleFactor[srctype]) / m_arMajor[srctype];
        double phi = con;

        int i = 0;

        while (true) {
            double delta_Phi = ((con + e1fn(m_Es[srctype]) * Math.sin(2.0 * phi) - e2fn(m_Es[srctype]) * Math.sin(4.0 * phi) + e3fn(m_Es[srctype]) * Math.sin(6.0 * phi)) / e0fn(m_Es[srctype])) - phi;
            phi = phi + delta_Phi;

            if (Math.abs(delta_Phi) <= EPSLN) {
				break;
			}

            if (i >= max_iter) {
                break;
            }

            i++;
        }

        if (Math.abs(phi) < (Math.PI / 2)) {
            double sin_phi = Math.sin(phi);
            double cos_phi = Math.cos(phi);
            double tan_phi = Math.tan(phi);
            double c = m_Esp[srctype] * cos_phi * cos_phi;
            double cs = c * c;
            double t = tan_phi * tan_phi;
            double ts = t * t;
            double cont = 1.0 - m_Es[srctype] * sin_phi * sin_phi;
            double n = m_arMajor[srctype] / Math.sqrt(cont);
            double r = n * (1.0 - m_Es[srctype]) / cont;
            double d = tmpPt.x / (n * m_arScaleFactor[srctype]);
            double ds = d * d;
            out_pt.y = phi - (n * tan_phi * ds / r) * (0.5 - ds / 24.0 * (5.0 + 3.0 * t + 10.0 * c - 4.0 * cs - 9.0 * m_Esp[srctype] - ds / 30.0 * (61.0 + 90.0 * t + 298.0 * c + 45.0 * ts - 252.0 * m_Esp[srctype] - 3.0 * cs)));
            out_pt.x = m_arLonCenter[srctype] + (d * (1.0 - ds / 6.0 * (1.0 + 2.0 * t + c - ds / 20.0 * (5.0 - 2.0 * c + 28.0 * t - 3.0 * cs + 8.0 * m_Esp[srctype] + 24.0 * ts))) / cos_phi);
        } else {
            out_pt.y = Math.PI * 0.5 * Math.sin(tmpPt.y);
            out_pt.x = m_arLonCenter[srctype];
        }
        transform(srctype, WGS84, out_pt);
    }

    public static double getDistancebyGeo(final GPoint pt1, final GPoint pt2) {
        double lat1 = D2R(pt1.y);
        double lon1 = D2R(pt1.x);
        double lat2 = D2R(pt2.y);
        double lon2 = D2R(pt2.x);

        double longitude = lon2 - lon1;
        double latitude = lat2 - lat1;

        double a = Math.pow(Math.sin(latitude / 2.0), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(longitude / 2.0), 2);
        return 6376.5 * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }

    public static double getDistancebyKatec(GPoint pt1, GPoint pt2) {
        pt1 = convert(TM128, WGS84, pt1);
        pt2 = convert(TM128, WGS84, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    public static double getDistancebyTm(GPoint pt1, GPoint pt2) {
        pt1 = convert(TM, WGS84, pt1);
        pt2 = convert(TM, WGS84, pt2);

        return getDistancebyGeo(pt1, pt2);
    }

    private static long getTimebySec(final double distance) {
        return Math.round(3600 * distance / 4);
    }

    public static long getTimebyMin(final double distance) {
        return (long)(Math.ceil(getTimebySec(distance) / 60));
    }

    /*
    Author:       Richard Greenwood rich@greenwoodmap.com
    License:      LGPL as per: http://www.gnu.org/copyleft/lesser.html
    */

    /**
     * convert between geodetic coordinates (longitude, latitude, height)
     * and gecentric coordinates (X, Y, Z)
     * ported from Proj 4.9.9 geocent.c
    */

    // following constants from geocent.c
    private static final double HALF_PI = 0.5 * Math.PI;
    private static final double COS_67P5  = 0.38268343236508977;  /* cosine of 67.5 degrees */
    private static final double AD_C      = 1.0026000 ;
    /* Toms region 1 constant */

    private static void transform(final int srctype, final int dsttype, final GPoint point) {
        if (srctype == dsttype) {
			return;
		}

        if (srctype != 0 || dsttype != 0) {
            // Convert to geocentric coordinates.
            geodetic_to_geocentric(srctype, point);

            // Convert between datums
            if (srctype != 0) {
                geocentric_to_wgs84(point);
            }

            if (dsttype != 0) {
                geocentric_from_wgs84(point);
            }

            // Convert back to geodetic coordinates
            geocentric_to_geodetic(dsttype, point);
        }
    }

    private static boolean geodetic_to_geocentric (final int type, final GPoint p) {

    /*
     * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
     * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
     * according to the current ellipsoid parameters.
     *
     *    Latitude  : Geodetic latitude in radians                     (input)
     *    Longitude : Geodetic longitude in radians                    (input)
     *    Height    : Geodetic height, in meters                       (input)
     *    X         : Calculated Geocentric X coordinate, in meters    (output)
     *    Y         : Calculated Geocentric Y coordinate, in meters    (output)
     *    Z         : Calculated Geocentric Z coordinate, in meters    (output)
     *
     */

      double Longitude = p.x;
      double Latitude = p.y;
      double Height = p.z;
      double X;  // output
      double Y;
      double Z;

      double Rn;            /*  Earth radius at location  */
      double Sin_Lat;       /*  Math.sin(Latitude)  */
      double Sin2_Lat;      /*  Square of Math.sin(Latitude)  */
      double Cos_Lat;       /*  Math.cos(Latitude)  */

      /*
      ** Don't blow up if Latitude is just a little out of the value
      ** range as it may just be a rounding issue.  Also removed longitude
      ** test, it should be wrapped by Math.cos() and Math.sin().  NFW for PROJ.4, Sep/2001.
      */
      if (Latitude < -HALF_PI && Latitude > -1.001 * HALF_PI ) {
		Latitude = -HALF_PI ;
	} else if (Latitude > HALF_PI && Latitude < 1.001 * HALF_PI ) {
		Latitude = HALF_PI;
	} else if ((Latitude < -HALF_PI) || (Latitude > HALF_PI)) { /* Latitude out of range */
          return true;
      }

      /* no errors */
      if (Longitude > Math.PI) {
		Longitude -= (2*Math.PI);
	}
      Sin_Lat = Math.sin(Latitude);
      Cos_Lat = Math.cos(Latitude);
      Sin2_Lat = Sin_Lat * Sin_Lat;
      Rn = m_arMajor[type] / (Math.sqrt(1.0e0 - m_Es[type] * Sin2_Lat));
      X = (Rn + Height) * Cos_Lat * Math.cos(Longitude);
      Y = (Rn + Height) * Cos_Lat * Math.sin(Longitude);
      Z = ((Rn * (1 - m_Es[type])) + Height) * Sin_Lat;

      p.x = X;
      p.y = Y;
      p.z = Z;
      return false;
    } // cs_geodetic_to_geocentric()


    /** Convert_Geocentric_To_Geodetic
     * The method used here is derived from 'An Improved Algorithm for
     * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
     */
    public static void geocentric_to_geodetic (final int type, final GPoint p) {

      double X = p.x;
      double Y = p.y;
      double Z = p.z;
      double Longitude;
      double Latitude = 0.;
      double Height;

      double W;        /* distance from Z axis */
      double W2;       /* square of distance from Z axis */
      double T0;       /* initial estimate of vertical component */
      double T1;       /* corrected estimate of vertical component */
      double S0;       /* initial estimate of horizontal component */
      double S1;       /* corrected estimate of horizontal component */
      double Sin_B0;   /* Math.sin(B0), B0 is estimate of Bowring aux doubleiable */
      double Sin3_B0;  /* cube of Math.sin(B0) */
      double Cos_B0;   /* Math.cos(B0) */
      double Sin_p1;   /* Math.sin(phi1), phi1 is estimated latitude */
      double Cos_p1;   /* Math.cos(phi1) */
      double Rn;       /* Earth radius at location */
      double Sum;      /* numerator of Math.cos(phi1) */
      boolean At_Pole;  /* indicates location is in polar region */

      At_Pole = false;
      if (X != 0.0) {
          Longitude = Math.atan2(Y,X);
      }
      else {
          if (Y > 0) {
              Longitude = HALF_PI;
          }
          else if (Y < 0) {
              Longitude = -HALF_PI;
          }
          else {
              At_Pole = true;
              Longitude = 0.0;
              if (Z > 0.0) {  /* north pole */
                  Latitude = HALF_PI;
              }
              else if (Z < 0.0) {  /* south pole */
                  Latitude = -HALF_PI;
              }
              else {  /* center of earth */
                  Latitude = HALF_PI;
                  Height = -m_arMinor[type];
                  return;
              }
          }
      }
      W2 = X*X + Y*Y;
      W = Math.sqrt(W2);
      T0 = Z * AD_C;
      S0 = Math.sqrt(T0 * T0 + W2);
      Sin_B0 = T0 / S0;
      Cos_B0 = W / S0;
      Sin3_B0 = Sin_B0 * Sin_B0 * Sin_B0;
      T1 = Z + m_arMinor[type] * m_Esp[type] * Sin3_B0;
      Sum = W - m_arMajor[type] * m_Es[type] * Cos_B0 * Cos_B0 * Cos_B0;
      S1 = Math.sqrt(T1*T1 + Sum * Sum);
      Sin_p1 = T1 / S1;
      Cos_p1 = Sum / S1;
      Rn = m_arMajor[type] / Math.sqrt(1.0 - m_Es[type] * Sin_p1 * Sin_p1);
      if (Cos_p1 >= COS_67P5) {
          Height = W / Cos_p1 - Rn;
      }
      else if (Cos_p1 <= -COS_67P5) {
          Height = W / -Cos_p1 - Rn;
      }
      else {
          Height = Z / Sin_p1 + Rn * (m_Es[type] - 1.0);
      }
      if (At_Pole == false) {
          Latitude = Math.atan(Sin_p1 / Cos_p1);
      }

      p.x = Longitude;
      p.y = Latitude;
      p.z = Height;
      return;
    } // geocentric_to_geodetic()



    /****************************************************************/
    // geocentic_to_wgs84(defn, p )
   //  defn = coordinate system definition,
    //  p = point to transform in geocentric coordinates (x,y,z)
    private static void geocentric_to_wgs84(final GPoint p) {

      //if( defn.datum_type == PJD_3PARAM )
      {
        // if( x[io] == HUGE_VAL )
        //    continue;
        p.x += datum_params[0];
        p.y += datum_params[1];
        p.z += datum_params[2];
      }
    } // geocentric_to_wgs84

    /****************************************************************/
    // geocentic_from_wgs84()
    //  coordinate system definition,
    //  point to transform in geocentric coordinates (x,y,z)
    private static void geocentric_from_wgs84(final GPoint p) {

      //if( defn.datum_type == PJD_3PARAM )
      {
        //if( x[io] == HUGE_VAL )
        //    continue;
        p.x -= datum_params[0];
        p.y -= datum_params[1];
        p.z -= datum_params[2];

      }
    } //geocentric_from_wgs84()



    public static GPoint fromRectangularToGeodetic(final GPoint pnt) {
        double C,B;
        double Q;
        double M;
        double F = 6.0D;
        double E,S;

        pnt.x -= UTMK.falseEasting;
        pnt.y -= UTMK.falseNorthing;
        C = (UTMK.ml0+pnt.y/UTMK.scaleFactor)/UTMK.a;
        B = C;
        for(M=0;;M++){
            Q=((C+UTMK.e1*Math.sin(2*B)-UTMK.e2*Math.sin(4*B)+UTMK.e3*Math.sin(6*B))/UTMK.e0)-B;
            B+=Q;
            if(Math.abs(Q) <= UTMK.o){
                break;
            }
            if(M>=F){
                return null;
            }
        }
        if(Math.abs(B) < UTMK.c){
            double A=Math.sin(B);
            double R=Math.cos(B);
            double G=Math.tan(B);
            double O=UTMK.ep2*Math.pow(R,2);
            double D=Math.pow(O,2);
            double H=Math.pow(G,2);
            double z=Math.pow(H,2);
            C=1-UTMK.es*Math.pow(A,2);
            double L=UTMK.a/Math.sqrt(C);
            double I=L*(1-UTMK.es)/C;
            double N=pnt.x/(L*UTMK.scaleFactor);
            double K=Math.pow(N,2);
            E=B-(L*G*K/I)*(0.5-K/24*(5+3*H+10*O-4*D-9*UTMK.ep2-K/30*(61+90*H+298*O+45*z-252*UTMK.ep2-3*D)));
            S=UTMK.g(UTMK.lng0+(N*(1-K/6*(1+2*H+O-K/20*(5-2*O+28*H-3*D+8*UTMK.ep2+24*z)))/R));
        }else{
            E=UTMK.c*UTMK.t(pnt.y);
            S=UTMK.lng0;
        }

        //pnt.x=S;
        //pnt.y=E;


        GPoint newPnt = new GPoint(S, E);

        return newPnt;
    }



}