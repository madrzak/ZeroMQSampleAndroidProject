package com.madrzak.zeromqsampleproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Łukasz on 21/03/2017.
 */

public class LocalPreferences {
    private static String TAG = LocalPreferences.class.getSimpleName();

    private static final String KEY_TENANT = "currentTenant";

    private static Context context;
    private static LocalPreferences instance;
    private static final Object MUTEX = new Object();
    private SharedPreferences prefs;

    private LocalPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static LocalPreferences getInstance() {
        if (instance == null) {
            synchronized (MUTEX) {
                if (instance == null) {
                    instance = new LocalPreferences();
                }
            }
        }
        return instance;
    }

    public static LocalPreferences getInstance(Context c) {
        context = c;
        return getInstance();
    }

    public void setCurrentTenant(String tenant) {
        writeString(KEY_TENANT, tenant);
    }
    public String getCurrentTenant() {
        return getString(KEY_TENANT, null);
    }


    // HELPER METHODS

    private void writeString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    private String getString(String key, String def) {
        return prefs.getString(key, def);
    }



}
