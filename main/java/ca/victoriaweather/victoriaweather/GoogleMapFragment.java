package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.List;

public class GoogleMapFragment extends Fragment implements ObservationDependentUpdatable, OnMapReadyCallback {
    public static final String FM_TAG = "FRAGMENT_GOOGLE_MAP";
    public static final LatLng DEFAULT_LATLNG = new LatLng(48.4622993, 236.6909943);
    public static final float DEFAULT_ZOOM = 13;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleMapWrapperLayout mMapLayout;
    private MapView mMapView;
    private OnInfoWindowElemTouchListener mInfoButtonListener;
    private ViewGroup mInfoWindow;
    public GoogleMapFragment(){

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        if(mMap == null) {
            mMap = map;
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM));
        }

        /*try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            Log.d("GoogleMapFragment", "onMapReady() MapsInitializer failed");
            return;
        }*/

        Log.d("GoogleMapFragment", "onMapReady()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        mInfoWindow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.layout_infowindow, null);
        Button infoButton = (Button)(mInfoWindow.findViewById(R.id.infowindow_button));
        // Setting custom OnTouchListener which deals with the pressed state
        mInfoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.common_signin_btn_icon_light),
                getResources().getDrawable(R.drawable.common_signin_btn_icon_dark))
        {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Observation parentObs = Observation.fromBundle(((MainActivity)getActivity()).shortNameToObservationBundle(marker.getTitle()));
                ((MainActivity)getActivity()).onObservationSelected(parentObs);
            }
        };
        infoButton.setOnTouchListener(mInfoButtonListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        Log.d("GoogleMapFragment", "onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        Log.d("GoogleMapFragment", "onResume()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
        Log.d("GoogleMapFragment", "onLowMemory()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
        Log.d("GoogleMapFragment", "onSaveInstanceState()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mMapLayout = (GoogleMapWrapperLayout) view.findViewById(R.id.map_wrapper);


        if(mMapView == null) {
            mMapView = new MapView(getActivity());
            mMapView.onCreate(savedInstanceState);
            //mMapView.getMapAsync(this);
            mMap = mMapView.getMap();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM));
        }

        if(mMap != null) {
            mMapLayout.init(mMap, getPixelsFromDp(getActivity().getApplicationContext(), 39 + 20));
            //try {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        Log.d("mMap", "getInfoContents()");
                        // Setting up the infoWindow with current's marker info
                        TextView infoTitle = (TextView) mInfoWindow.findViewById(R.id.infowindow_title);
                        TextView infoSnippet = (TextView) mInfoWindow.findViewById(R.id.infowindow_snippet);
                        infoTitle.setText(marker.getTitle());
                        infoSnippet.setText(marker.getSnippet());
                        mInfoButtonListener.setMarker(marker);

                        // We must call this to set the current marker and infoWindow references
                        // to the GoogleMapWrapperLayout
                        mMapLayout.setMarkerWithInfoWindow(marker, mInfoWindow);
                        return mInfoWindow;
                    }
                });

                //TODO This operation might be dangerous if marker is null (I don't think so but maybe) and this whole thing might need to be in a try catch block?

                mMapLayout.setMarkerWithInfoWindow(mInfoButtonListener.getMarker(), mInfoWindow);

            /*} catch (Exception e) {
                Log.e("GoogleMapFragment", "when trying to set mMap's window adapter (EXCEPTION)", e);
            }*/
        }

        mMapView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mMapLayout.addView(mMapView);

        return view;
    }

    @Override
    public void onDestroyView() {
        //When we break down the view hierarchy to go pause/stop, we decouple the map view to preserve it
        ((ViewGroup)(mMapView.getParent())).removeView(mMapView);
        super.onDestroyView();
        mMapLayout = null;
        Log.d("GoogleMapFragment", "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        mMapView = null;

        //TODO destroy mInfoWindow

        super.onDestroy();
        Log.d("GoogleMapFragment", "onDestroy()");
    }


    public GoogleMap getMap() {
        return mMap;
    }

    public void updateObservations(List<Bundle> observationList) {
        try {
            mMap.clear();

            //TODO verify that foreach isn't resolving this on each loop
            for (Observation o : Observation.observationsFromBundleArrayList(observationList)) {
                if (o.hasLatLng()) {
                    MarkerOptions m = new MarkerOptions();
                    m.position(o.getLatLng());
                    m.title(o.getAttribute(Observation.ATTR_NAME));
                    m.draggable(false);
                    m.flat(false);
                    if (o.hasAttribute(Observation.ATTR_TEMPERATURE)) {
                        m.snippet("Temperature: " + o.getAttribute(Observation.ATTR_TEMPERATURE) + o.getAttribute(Observation.ATTR_TEMPERATURE_UNIT));
                    }
                    mMap.addMarker(m);
                } else {
                    Log.d("GoogleMapFragment", "reloadPins(): An observation existed without a valid LatLng");
                }
            }

        } catch (NullPointerException e) {
            Log.d("GoogleMapFragment", "Couldn't update observations because map was null");
        }
    }

    public void moveToLatLng(LatLng target) {
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, DEFAULT_ZOOM));
        } catch (NullPointerException e) {
            Log.d("GoogleMapFragment", "moveToLatLng() couldn't move map, map was null");
        }
    }

    public void onClickConfirmed(View view, Marker marker) {

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}