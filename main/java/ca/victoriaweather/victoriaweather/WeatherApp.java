package ca.victoriaweather.victoriaweather;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

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

    public boolean isNetworkingAvailable() throws Exception {
        //code snippet from android dev tutorials (http://developer.android.com/training/basics/network-ops/managing.html)
        //modified for compatibility as per (http://stackoverflow.com/questions/32547006/connectivitymanager-getnetworkinfoint-deprecated)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String networkPreference = preferences.getString("pref_syncConnectionType", "none");

        ConnectivityManager connMgr = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        /*boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();*/

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // connected to wifi
            Toast.makeText(this, "Using " + networkInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            return true;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            // connected to the mobile provider's data plan
            // only proceed if user has specified that they allow mobile data
            if ("all".equals(networkPreference)) {
                Toast.makeText(this, "Using " + networkInfo.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        Toast.makeText(this, "Couldn't refresh.\r\nPlease check your network preference and connectivity.", Toast.LENGTH_LONG).show();
        return false;
    }
}
