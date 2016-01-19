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

public class FavouriteListFragment extends ListFragment{
    public static final String FM_TAG = "FRAGMENT_LIST_FAVOURITE_OBSERVATION";
    private interactionListener mListener;

    public static FavouriteListFragment newInstanceOf() {
        FavouriteListFragment f = new FavouriteListFragment();
        return f;
    }
    public FavouriteListFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //WeatherApp app = (WeatherApp)(getActivity().getApplicationContext());
        //setListAdapter(new ArrayAdapter<Observation>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, app.getFavourites()));
        //app.registerFavouriteAdapter((ArrayAdapter<Observation>) getListAdapter());
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
    public void onDestroy() {
        Log.d("FavouriteListFragment", "onDestroy()");
        super.onDestroy();
        ((WeatherApp)(getActivity().getApplicationContext())).dropFavouriteAdapater();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mListener != null) {
            mListener.onObservationSelected((Observation)getListAdapter().getItem(position));
        }
    }

    public void registerListAdapter() {
        WeatherApp app = (WeatherApp)(getActivity().getApplicationContext());
        setListAdapter(new ArrayAdapter<Observation>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, app.getFavourites()));
        app.registerObservationAdapter((ArrayAdapter<Observation>) getListAdapter());
    }

    public interface interactionListener {
        void onObservationSelected(Observation obs);
    }
}
