package com.giserpeng.ntripshare.gnss.utils;

import java.util.Arrays;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-04 17:23
 */
public class RTKUtils {

    private static final double PI = Math.PI;
    private static final double RE = 6378137.0;
    private static final double FE = 1.0 / 298.257223563;
    private static final double EE = FE * (2 - FE);

    /**
     * ECEF (Earth-Centered Earth-Fixed) ==> WGS84 (World Geodetic System)
     *
     * @return double[]
     */
    public static double[] ecef2pos(final double x, final double y, final double z) {

        final double[] e = {x, y, z};
        double r = dot(e, e, 2);
        double j = z;
        double k = 0.0;
        double v = RE;
        double s;

        for (; Math.abs(j - k) >= 1E-4; ) {
            k = j;
            s = j / Math.sqrt(r + j * j);
            v = RE / Math.sqrt(1.0 - EE * s * s);
            j = z + EE * v * s;
        }

        double lat = r > 1E-12 ? Math.atan(j / Math.sqrt(r)) : (z > 0.0 ? PI / 2.0 : -PI / 2.0);
        double lon = r > 1E-12 ? Math.atan2(y, x) : 0.0;
        double alt = Math.sqrt(r + j * j) - v;

        double[] geo = new double[3];
        geo[0] = lat * 180 / PI;
        geo[1] = lon * 180 / PI;
        geo[2] = alt;

        System.out.println("args = [" + Arrays.toString(geo) + "]");
        return geo;
    }

    private static double dot(final double[] a, final double[] b, int n) {
        double c = 0.0;
        while (--n >= 0) c += a[n] * b[n];
        return c;
    }
}
