package ca.victoriaweather.victoriaweather;


import android.app.Application;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherApp extends Application {
    //returns the index of the matched observation, or -1 if the list does not contain the observation
    public static int listContainsObservation(List<Observation> list, Observation observation) {
        for(int i=0;i<list.size();i++) {
            if(list.get(i).getId().equals(observation.getId())) {
                return i;
            }
        }
        return -1;
    }

    public static boolean listContainsAlert(List<Alert> list, Alert alert) {
        // unused
        return true;
    }

    public static void addObservationLexicographic(List<Observation> list, Observation observation) {
        //TODO Lexicographic order
        list.add(observation);
    }

    private List<Observation> allObservations;
    private List<Alert> allAlerts;
    private List<Observation> favouriteObservations;
    private List<String> favouriteIds;

    private ArrayAdapter<Observation> observationAdapter;
    private ArrayAdapter<Observation> favouriteAdapter;
    private ArrayAdapter<Alert> alertAdapter;

    public List<Observation> getObservations() {
        if(allObservations == null) {
            Log.d("WeatherApp", "getObservations(): creating new List<Observations> -> allObservations");
            allObservations = Collections.synchronizedList(new ArrayList<Observation>());
        }
        return allObservations;
    }

    public List<Observation> getFavourites() {
        if(favouriteObservations == null) {
            Log.d("WeatherApp", "getFavourites(): creating new List<Observations> -> favouriteObservations");
            favouriteObservations = Collections.synchronizedList(new ArrayList<Observation>());
        }
        getFavouriteIds();
        return favouriteObservations;
    }

    public List<Alert> getAlerts() {
        if(allAlerts == null) {
            Log.d("WeatherApp", "getAlerts(): creating new List<Alert> -> allAlerts");
            allAlerts = Collections.synchronizedList(new ArrayList<Alert>());
        }
        return allAlerts;
    }

    private List<String> getFavouriteIds() {
        if(favouriteIds == null) {
            Log.d("WeatherApp", "getFavouriteIds(): creating new List<String> -> favouriteIds");
            favouriteIds = Collections.synchronizedList(new ArrayList<String>());

            //TODO Load favouriteId's from external storage
        }
        return favouriteIds;
    }

    public boolean checkedObservationUpdate(Observation observation) {
        int index;
        if((index = listContainsObservation(getObservations(), observation)) != -1) {
            getObservations().remove(index);
        }
        addObservationLexicographic(getObservations(), observation);

        //if the id appears in favourites, update favouriteObservation
        if(getFavouriteIds().contains(observation.getId())) {
            if((index = listContainsObservation(getFavourites(), observation)) != -1) {
                getFavourites().remove(index);
            } else {
                addObservationLexicographic(getFavourites(), observation);
            }
        }
        return true;
    }

    public boolean checkedAlertUpdate(Alert alert) {

        //TODO Implement alert updates

        return true;
    }

    public void registerAlertAdapter(ArrayAdapter<Alert> a) {
        if(alertAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to register a non-null alertAdapter, overwriting...");
        }
        alertAdapter = a;
    }

    public void dropAlertAdapater() {
        if(alertAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to drop a null alertAdapter");
        }
        alertAdapter = null;
    }

    public void registerObservationAdapter(ArrayAdapter<Observation> o) {
        if(observationAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to register a non-null observationAdapter, overwriting...");
        }
        observationAdapter = o;
    }

    public void dropObservationAdapter() {
        if(observationAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to drop a null observationAdapter");
        }
        observationAdapter = null;
    }

    public void registerFavouriteAdapter(ArrayAdapter<Observation> f) {
        if(favouriteAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to register a non-null favouriteAdapter, overwriting...");
        }
        favouriteAdapter = f;
    }

    public void dropFavouriteAdapater() {
        if(favouriteAdapter != null) {
            Log.d("WeatherApp", "WARNING: Attempted to drop a null favouriteAdapter");
        }
        favouriteAdapter = null;
    }

}
