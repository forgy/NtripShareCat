package com.giserpeng.ntripshare.ntrip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Calendar;

class Constants {
    static final double FeetPerMeter = 3.2808399d;
    static final String SKU_PREMIUM_FEATURES = "premium_features";
    private static final int Salt = 167199;

    Constants() {
    }

    static boolean IsUnlockCodeValid(Context context) {
        int parseInt;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = defaultSharedPreferences.getString("UUID", "");
        try {
            parseInt = Integer.parseInt(defaultSharedPreferences.getString("UnlockCode", ""));
        } catch (NumberFormatException e) {
            parseInt = 0;
        }
        char[] toCharArray = string.toCharArray();
        int i = 0;
        for (char c : toCharArray) {
            i += c;
        }
        Calendar instance = Calendar.getInstance();
        instance.set(2020, 12, 31);
        return Calendar.getInstance().compareTo(instance) < 0 && parseInt == (i * 119) + Salt;
    }
}
