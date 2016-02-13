package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * The concept and most implementation of this class is thanks to stack overflow user 'chose007'
 * http://stackoverflow.com/questions/14123243/google-maps-android-api-v2-interactive-infowindow-like-in-original-android-go/15040761#15040761
 */
public class GoogleMapWrapperLayout extends RelativeLayout {
    private Marker mMarker;
    private View mInfoWindowView;
    private GoogleMap mMap;
    private int mBottomOffsetPixels;

    public GoogleMapWrapperLayout(Context context){
        super(context);
    }
    public GoogleMapWrapperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GoogleMapWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }
    /*public GoogleMapWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    } higher API level*/

    /**
     * Must be called before we can route the touch events
     */
    public void init(GoogleMap map, int bottomOffsetPixels) {
        this.mMap = map;
        this.mBottomOffsetPixels = bottomOffsetPixels;
    }

    /**
     * Best to be called from either the InfoWindowAdapter.getInfoContents
     * or InfoWindowAdapter.getInfoWindow.
     */
    public void setMarkerWithInfoWindow(Marker marker, View infoWindow) {
        this.mMarker = marker;
        this.mInfoWindowView = infoWindow;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = false;
        // Make sure that the infoWindow is shown and we have all the needed references
        if (mMarker != null && mMarker.isInfoWindowShown() && mMap != null && mInfoWindowView != null) {
            // Get a marker position on the screen
            Point point = mMap.getProjection().toScreenLocation(mMarker.getPosition());

            // Make a copy of the MotionEvent and adjust it's location
            // so it is relative to the infoWindow left top corner
            MotionEvent copyEv = MotionEvent.obtain(ev);
            copyEv.offsetLocation(
                    -point.x + (mInfoWindowView.getWidth() / 2),
                    -point.y + mInfoWindowView.getHeight() + mBottomOffsetPixels);

            // Dispatch the adjusted MotionEvent to the infoWindow
            ret = mInfoWindowView.dispatchTouchEvent(copyEv);
        }
        // If the infoWindow consumed the touch event, then just return true.
        // Otherwise pass this event to the super class and return it's result
        return ret || super.dispatchTouchEvent(ev);
    }
}
