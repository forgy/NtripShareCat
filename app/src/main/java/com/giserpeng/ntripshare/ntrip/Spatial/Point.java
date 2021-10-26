package com.giserpeng.ntripshare.ntrip.Spatial;

import java.math.BigDecimal;

public class Point {
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal z;

    public Point(String wkt) {
        String clear = wkt.substring(wkt.indexOf("(")+1, wkt.indexOf(")"));
        x =  new BigDecimal(clear.split(" ")[0]);
        y =  new BigDecimal(clear.split(" ")[1]);
    }

    public BigDecimal getX() {
        if (x == null)
            x = new BigDecimal(0);
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        if (y == null)
            y = new BigDecimal(0);
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public BigDecimal getZ() {
        if (z == null){
            x = new BigDecimal(0);
        }
        return z;
    }

    public void setZ(BigDecimal z) {
        this.z = z;
    }

    public String getWKT(){
        if (x == null)
            x = new BigDecimal(0);

        if (y == null)
            y = new BigDecimal(0);

        return "POINT(" + x + " " + y + ")";
    }
}
