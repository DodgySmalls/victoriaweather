package ca.victoriaweather.victoriaweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ConditionsFragment extends Fragment implements ObservationDependentUpdatable {
    public static final String FM_TAG = "FRAGMENT_OBSERVATION_DISPLAY";
    private static final String OBSERVATION_BUNDLE_STR = "BUNDLE_OBSERVATION";

    private Observation observation;

    public static ConditionsFragment newInstanceOf(Observation o) {
        ConditionsFragment fragment = new ConditionsFragment();
        fragment.setArguments(o.toBundle());
        return fragment;
    }
    public ConditionsFragment() {}

    public Observation getObservation() {
        if(observation == null) {
            observation = Observation.fromBundle(getArguments());
        }
        return observation;
    }

    public void setObservation(Bundle observationBundle) {
        this.observation = Observation.fromBundle(observationBundle); //Object.clone() has issues, this should be bullet proof
    }


    public void updateObservations(List<Bundle> list) {
        for(Bundle b : list) {
            Observation o = Observation.fromBundle(b);
            if(observation.getId().equals(o.getId())) {
                observation = o;
                attemptDisplayUpdate(null);
                return;
            }
        }

        Log.d("ConditionsFraqment", "Tried to update observation but no matching observation was found");
    }

    private void attemptDisplayUpdate(View v) {
        View currentView;

        if(v == null) {
            currentView = getView();
        } else {
            currentView = v;
        }

        try {
            TextView title = (TextView)(currentView.findViewById(R.id.station_title));
            TextView temperature = (TextView)(currentView.findViewById(R.id.condition_temperature));
            TextView windSpeed = (TextView)(currentView.findViewById(R.id.condition_average_wind));
            TextView rain = (TextView)(currentView.findViewById(R.id.condition_rain));
            TextView pressure = (TextView)(currentView.findViewById(R.id.condition_pressure));
            TextView uvindex = (TextView)(currentView.findViewById(R.id.condition_uv_index));
            TextView insolation = (TextView)(currentView.findViewById(R.id.condition_insolation));
            TextView humidity = (TextView)(currentView.findViewById(R.id.condition_humidity));
            TextView temperatureUnit = (TextView)(currentView.findViewById(R.id.condition_temperature_unit));
            TextView windSpeedUnit = (TextView)(currentView.findViewById(R.id.condition_average_wind_unit));
            TextView rainUnit = (TextView)(currentView.findViewById(R.id.condition_rain_unit));
            TextView pressureUnit = (TextView)(currentView.findViewById(R.id.condition_pressure_unit));
            TextView uvindexUnit = (TextView)(currentView.findViewById(R.id.condition_uv_index_unit));
            TextView insolationUnit = (TextView)(currentView.findViewById(R.id.condition_insolation_unit));
            TextView humidityUnit = (TextView)(currentView.findViewById(R.id.condition_humidity_unit));
            Button fav = (Button)(currentView.findViewById(R.id.conditions_toggle_fav));

            if(observation.isFavourite()) {
                fav.setText("-fav");
            } else {
                fav.setText("+fav");
            }

            title.setText(observation.getAttribute(Observation.ATTR_NAME_LONG));

            if(observation.hasAttribute(Observation.ATTR_TEMPERATURE)) {
                temperature.setText(observation.getAttribute(Observation.ATTR_TEMPERATURE));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    temperatureUnit.setText(observation.getAttribute(Observation.ATTR_TEMPERATURE_UNIT));
                }
            }


            if(observation.hasAttribute(Observation.ATTR_WIND_SPEED)) {
                windSpeed.setText(observation.getAttribute(Observation.ATTR_WIND_SPEED));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    windSpeedUnit.setText(observation.getAttribute(Observation.ATTR_WIND_SPEED_UNIT));
                }
            }

            if(observation.hasAttribute(Observation.ATTR_RAIN)) {
                rain.setText(observation.getAttribute(Observation.ATTR_RAIN));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    rainUnit.setText(observation.getAttribute(Observation.ATTR_RAIN_UNIT));
                }
            }

            if(observation.hasAttribute(Observation.ATTR_PRESSURE)) {
                pressure.setText(observation.getAttribute(Observation.ATTR_PRESSURE));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    pressureUnit.setText(observation.getAttribute(Observation.ATTR_PRESSURE_UNIT));
                }
            }

            if(observation.hasAttribute(Observation.ATTR_UV_INDEX)) {
                uvindex.setText(observation.getAttribute(Observation.ATTR_UV_INDEX));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    uvindexUnit.setText(observation.getAttribute(Observation.ATTR_UV_INDEX_UNIT));
                }
            }

            if(observation.hasAttribute(Observation.ATTR_INSOLATION)) {
                insolation.setText(observation.getAttribute(Observation.ATTR_INSOLATION));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    insolationUnit.setText(observation.getAttribute(Observation.ATTR_INSOLATION_UNIT));
                }
            }

            if(observation.hasAttribute(Observation.ATTR_HUMIDITY)) {
                humidity.setText(observation.getAttribute(Observation.ATTR_HUMIDITY));
                if(observation.hasAttribute(Observation.ATTR_TEMPERATURE_UNIT)) {
                    humidityUnit.setText(observation.getAttribute(Observation.ATTR_HUMIDITY_UNIT));
                }
            }

            Button mapBridge = (Button)(currentView.findViewById(R.id.conditions_map_bridge_button));
            if(observation.hasLatLng()) {
                mapBridge.setVisibility(View.VISIBLE);
            } else {
                mapBridge.setVisibility(View.GONE);
            }

            TextView time = (TextView)(currentView.findViewById(R.id.condition_observation_time));
            if(observation.hasAttribute(Observation.ATTR_TIME)) {
                time.setText(observation.getAttribute(Observation.ATTR_TIME));
            }

        } catch (Exception e) {
            Log.d("ConditionsFragment", "attemptDisplayUpdate(): Failed due to exception being thrown");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState != null) {
            observation = Observation.fromBundle(savedInstanceState.getBundle(OBSERVATION_BUNDLE_STR));
            if(observation == null) {
                Log.d("ConditionsFragment", "unhappy");
            }
        } else {
            observation = Observation.fromBundle(getArguments());
            Log.d("ConditionsFragment", "loaded observation from args");
            if(observation == null) {
                Log.d("ConditionsFragment", "didn't actually retrieve observation from bundle");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conditions, container, false);
        attemptDisplayUpdate(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle(OBSERVATION_BUNDLE_STR, observation.toBundle());
    }
}
