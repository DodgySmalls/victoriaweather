package ca.victoriaweather.victoriaweather;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.android.gms.maps.model.LatLng;

public class Observation {
    //names derived to coincidence with XML
    public static final String ATTR_ID = "station_id";
    public static final String ATTR_NAME_LONG = "station_long_name";
    public static final String ATTR_NAME = "station_name";
    public static final String ATTR_LATITUDE = "latitude";
    public static final String ATTR_LONGITUDE = "longitude";
    public static final String ATTR_ELEVATION = "elevation";
    public static final String ATTR_TIME = "observation_time";
    public static final String ATTR_TIMEZONE = "elevation";
    public static final String ATTR_TEMPERATURE = "temperature";
    public static final String ATTR_TEMPERATURE_LOW = "temperature_low";
    public static final String ATTR_TEMPERATURE_HIGH = "temperature_high";
    public static final String ATTR_TEMPERATURE_UNIT = "temperature_units";
    public static final String ATTR_HUMIDITY = "humidity";
    public static final String ATTR_HUMIDITY_UNIT = "humidity_units";
    public static final String ATTR_DEWPOINT = "dewpoint";
    public static final String ATTR_DEWPOINT_UNIT = "dewpoint_units";
    public static final String ATTR_PRESSURE = "pressure";
    public static final String ATTR_PRESSURE_UNIT = "pressure_units";
    public static final String ATTR_PRESSURE_TREND = "pressure_trend";
    public static final String ATTR_INSOLATION = "insolation";
    public static final String ATTR_INSOLATION_UNIT = "insolation_units";
    public static final String ATTR_UV_INDEX = "uv_index";
    public static final String ATTR_UV_INDEX_UNIT = "uv_index_units";
    public static final String ATTR_RAIN = "rain";
    public static final String ATTR_RAIN_UNIT = "rain_units";
    public static final String ATTR_RAIN_RATE = "rain_rate";
    public static final String ATTR_RAIN_RATE_UNIT = "rain_rate_units";
    public static final String ATTR_WIND_SPEED = "wind_speed";
    public static final String ATTR_WIND_SPEED_DIRECTION = "wind_speed_direction";
    public static final String ATTR_WIND_SPEED_HEADING = "wind_speed_heading";
    public static final String ATTR_WIND_SPEED_MAX = "wind_speed_max";
    public static final String ATTR_WIND_SPEED_UNIT = "wind_speed_units";
    public static final String ATTR_EVAPOTRANSPIRATION = "evapotranspiration";
    public static final String ATTR_EVAPOTRANSPIRATION_UNIT = "evapotranspiration_units";
    public static final String ATTR_INSOLATION_PREDICTED = "insolation_predicted";
    public static final String ATTR_INSOLATION_PREDICTED_UNIT = "insolation_predicted_unit";
    public static final String HASH_KEYLIST = "observation_keylist";

    private HashMap<String, String> attributes;    //named attributes of an observation
    private String id;

    public Observation(String id) {
        this.id = id;
        attributes = new HashMap<String, String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //Check if this observation has a valid latitude and longitude within its attributes
    public boolean hasLatLng(){
        try {
            Double.parseDouble(attributes.get(ATTR_LATITUDE));
            Double.parseDouble(attributes.get(ATTR_LONGITUDE));
        } catch (NullPointerException e) {
            Log.d("Observation", "hasLatLng(): Observation did not have a LatLng");
            return false;
        }
        return true;
    }

    //Returns the LatLng within attributes
    public LatLng getLatLng() {
        try {
            return new LatLng(Double.parseDouble(attributes.get(ATTR_LATITUDE)), Double.parseDouble(attributes.get(ATTR_LONGITUDE)));
        } catch (NullPointerException e) {
            Log.d("Observation", "getLatLng(): LatLng was requested but could not be retrieved from attributes");
            return null;
        }
    }
    //check if this observation has an entry for a specific attribute name
    public boolean hasAttribute(String attr) {
        for(String key: attributes.keySet()) {
            if(key.equals(attr)) {
                return true;
            }
        }
        return false;
    }

    //Add a new attribute to the observation. If an old attribute existed, overwrite and return false
    public boolean putAttribute(String key, String val) {
        if(this.hasAttribute(key)) {
            attributes.remove(key);
            attributes.put(key, val);
            return false;
        }

        attributes.put(key, val);
        return true;
    }

    //Turn this observation into a bundle
    public Bundle toBundle() {
        Bundle savedInstanceState = new Bundle();
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(attributes.keySet());
        savedInstanceState.putStringArrayList(HASH_KEYLIST, keys);

        for(String key : keys) {
            savedInstanceState.putString(key, attributes.get(key));
        }

        return savedInstanceState;
    }

    //Convert a bundle into an observation
    public static Observation fromBundle(Bundle savedInstanceState) {
        try {
            Observation obs = new Observation((String) savedInstanceState.get(ATTR_ID));
            ArrayList<String> keys = savedInstanceState.getStringArrayList(HASH_KEYLIST);
            for (String key : keys) {
                obs.putAttribute(key, savedInstanceState.getString(key));
            }

            return obs;
        } catch (NullPointerException e) {
            Log.d("Observation", "fromBundle(): FATAL NULL POINTER EXCEPTION");
            return null;
        }
    }

    @Override
    public String toString() {
        if(attributes.containsKey("station_name")) {
            return(attributes.get("station_name"));
        } else {
            return "unhappy";
        }
    }
}
