package com.giserpeng.ntripshare.ntrip;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

class MockLocationProvider {
    private Context ctx;
    private String providerName;

    MockLocationProvider(String str, Context context) {
        this.providerName = str;
        this.ctx = context;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                locationManager.addTestProvider(this.providerName, false, false, false, false, false, true, true, 0, 5);
                locationManager.setTestProviderEnabled(this.providerName, true);
            } catch (Exception e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void pushLocation(double d, double d2, float f, float f2, float f3, float f4, int i, int i2, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, float f5) {
        Location location = new Location(this.providerName);
        location.setLatitude(d);
        location.setLongitude(d2);
        location.setAltitude((double) (f + f5));
        location.setSpeed(0.514444f * f2);
        location.setBearing(f3);
        location.setAccuracy(f4);
        location.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT > 16) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", i2);
        bundle.putFloat("mslHeight", f);
        bundle.putString("diffID", str8);
        bundle.putString("utcTime", str);
        bundle.putString("hdop", str2);
        if (str3.length() > 1) {
            bundle.putString("vdop", str3);
        }
        if (str4.length() > 1) {
            bundle.putString("pdop", str4);
        }
        if (str5.length() > 1) {
            bundle.putString("2drms", str5);
        }
        if (str6.length() > 1) {
            bundle.putString("3drms", str6);
        }
        bundle.putString("mockProvider", "LefebureNTRIPClient");
        bundle.putFloat("undulation", f5);
        bundle.putInt("diffStatus", i);
        bundle.putString("diffAge", str7);
        location.setExtras(bundle);
        LocationManager locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                locationManager.setTestProviderLocation(this.providerName, location);
            } catch (SecurityException e) {
                Toast.makeText(this.ctx, "SecurityException on Mock Location Provider", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void shutdown() {
        try {
            LocationManager locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.removeTestProvider(this.providerName);
            }
        } catch (Exception e) {
        }
    }
}
