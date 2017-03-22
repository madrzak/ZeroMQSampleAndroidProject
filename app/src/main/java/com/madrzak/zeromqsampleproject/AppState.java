package com.madrzak.zeromqsampleproject;

/**
 * Created by Łukasz on 21/03/2017.
 */

public class AppState {

    private final String TAG = AppState.class.getSimpleName();
    private static AppState ourInstance = new AppState();

    public static AppState getInstance() {
        return ourInstance;
    }

    private AppState() {
    }

    private String currentTenant;

    public String getCurrentTenant() {
        return currentTenant;
    }

    public void setCurrentTenant(String currentTenant) {
        this.currentTenant = currentTenant;
    }
}