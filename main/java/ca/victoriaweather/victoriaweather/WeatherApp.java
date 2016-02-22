package ca.victoriaweather.victoriaweather;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class WeatherApp extends Application {
    private Queue<Observation> observationQueue;

    public Queue<Observation> getObservationQueue() {
        if(observationQueue == null) {
            observationQueue = new ConcurrentLinkedQueue<Observation>();
        }
        return observationQueue;
    }

    public static boolean isWifiNetworkAvailable(Activity callingActivity) {
        try {
            NetworkInfo activeInfo = ((ConnectivityManager) callingActivity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isConnected()) {
                return activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isMobileNetworkAvailable(Activity callingActivity) {
        try {
            NetworkInfo activeInfo = ((ConnectivityManager) callingActivity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isConnected()) {
                return activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }
}
