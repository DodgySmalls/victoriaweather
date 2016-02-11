package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.List;

public class GoogleMapFragment extends SupportMapFragment implements ObservationDependentUpdatable {
    public static final String FM_TAG = "FRAGMENT_GOOGLE_MAP";
    public static final LatLng DEFAULT_LATLNG = new LatLng(48.4622993, 236.6909943);
    public static final float DEFAULT_ZOOM = 13;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleMap.OnInfoWindowClickListener infoClickListener;

    public GoogleMapFragment(){
    }

    public static GoogleMapFragment newInstance() {
        GoogleMapFragment m = new GoogleMapFragment();
        //TODO: utilize bundle for best practice
        Bundle args = new Bundle();
        return m;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = this.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM));
    }

    public void updateObservations(List<Bundle> observationList) {
        mMap.clear();
        //TODO verify that foreach isn't resolving this on each loop
        for(Observation o : Observation.observationsFromBundleArrayList(observationList)) {
            if(o.hasLatLng()) {
                MarkerOptions m = new MarkerOptions();
                m.position(o.getLatLng());
                m.title(o.getAttribute(Observation.ATTR_NAME));
                m.draggable(false);
                m.flat(false);
                if(o.hasAttribute(Observation.ATTR_TEMPERATURE)) {
                    m.snippet("Temperature: " + o.getAttribute(Observation.ATTR_TEMPERATURE) + o.getAttribute(Observation.ATTR_TEMPERATURE_UNIT));
                }
                mMap.addMarker(m);
            } else {
                Log.d("GoogleMapFragment", "reloadPins(): An observation existed without a valid LatLng");
            }
        }
    }

    public void moveToLatLng(LatLng target) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, DEFAULT_ZOOM));
    }

    //TODO InfoWindow button interactivity http://stackoverflow.com/questions/14123243/google-maps-android-api-v2-interactive-infowindow-like-in-original-android-go/15040761#15040761
    //ala https://play.google.com/store/apps/details?id=com.circlegate.tt.transit.android
}