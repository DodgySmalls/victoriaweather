package ca.victoriaweather.victoriaweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConditionsFragment extends Fragment {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conditions, container, false);

        //display observation

        return view;
    }
}
