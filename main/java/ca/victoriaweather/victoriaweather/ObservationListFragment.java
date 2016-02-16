package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ObservationListFragment extends ListFragment implements ObservationDependentUpdatable {
    public static final String FM_TAG = "FRAGMENT_LIST_OBSERVATION";
    private static final String BUNDLE_OBSERVATIONS = "BUNDLE_OBSERVATIONS";
    private interactionListener mListener;
    private List<Observation> observations;

    public static ObservationListFragment newInstanceOf() {
        ObservationListFragment f = new ObservationListFragment();
        return f;
    }
    public ObservationListFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            observations = new ArrayList<Observation>();
            setListAdapter(new ArrayAdapter<Observation>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, observations));
        } else {
            observations = Observation.observationsFromBundleArrayList(savedInstanceState.getParcelableArrayList(BUNDLE_OBSERVATIONS));
            setListAdapter(new ArrayAdapter<Observation>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, observations));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        try {
            savedInstanceState.putParcelableArrayList(BUNDLE_OBSERVATIONS, Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("ObservationListFragment", "onSaveInstanceState() NullPointerException @observations");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (interactionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interactionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mListener != null) {
            mListener.onObservationSelected((Observation)getListAdapter().getItem(position));
        }
    }

    public void updateObservations(List<Bundle> list) {
        int index;

        for(Bundle b: list) {
            Observation o = Observation.fromBundle(b);
            if((index  = Observation.listContainsObservation(observations, o)) != -1) {
                observations.remove(index);
            }
            Observation.addObservationLexicographic(observations, o);
        }

        setListShown(true);
        ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
    }


    public interface interactionListener {
        void onObservationSelected(Observation obs);
    }
}
