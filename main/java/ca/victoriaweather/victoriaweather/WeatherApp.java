package ca.victoriaweather.victoriaweather;


import android.app.Application;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class WeatherApp extends Application {
    /*public static boolean listContainsAlert(List<Alert> list, Alert alert) {
        // unused
        return true;
    }*/

    private Queue<Observation> observationQueue;

    public Queue<Observation> getObservationQueue() {
        if(observationQueue == null) {
            observationQueue = new ConcurrentLinkedQueue<Observation>();
        }
        return observationQueue;
    }

}
