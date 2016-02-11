package ca.victoriaweather.victoriaweather;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;


public class GraphSelectionDialogFragment extends DialogFragment {
    public static final String FM_TAG = "FRAGMENT_DIALOG_GRAPH_SELECTOR";
    public static final String BASE_URL_STRING = "http://www.victoriaweather.ca/stations/";
    public static final String BUNDLE_STATION_NAME_TAG = "GRAPH_SELECTION_NAME_ARG";
    public static final String BUNDLE_CONDITION_TAG = "GRAPH_SELECTION_CONDITION_ARG";
    public static final String BUNDLE_URL_TAG = "GRAPH_SELECTION_INSTANCE_URL";
    public static final String CONDITION_TEMPERATURE = "temperature";
    public static final String CONDITION_RAIN = "raintotal";
    public static final String CONDITION_WIND = "wind";
    public static final String CONDITION_PRESSURE = "console.barometer";
    public static final String CONDITION_UV_INDEX = "uv";
    public static final String CONDITION_INSOLATION = "solar";
    public static final String CONDITION_HUMIDITY = "humidity";

    private static final String UI_TITLE = "Select graph period";
    private static final String UI_CANCEL = "cancel";
    private static final String UI_PERIOD_DAY = "day";
    private static final String UI_PERIOD_WEEK = "week";
    private static final String UI_PERIOD_MONTH = "month";
    private static final String[] UI_ITEMS = {UI_PERIOD_DAY, UI_PERIOD_WEEK, UI_PERIOD_MONTH};

    private String URLstr;
    private String conditionstr;

    private interactionListener selectListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            selectListener = (interactionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interactionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            Bundle args = getArguments();
            conditionstr = args.getString(BUNDLE_CONDITION_TAG);
            URLstr = BASE_URL_STRING;
            URLstr += args.getString(BUNDLE_STATION_NAME_TAG) + "/" + args.getString(BUNDLE_STATION_NAME_TAG) + ".";
        } else {
            URLstr = savedInstanceState.getString(BUNDLE_URL_TAG);
            conditionstr = savedInstanceState.getString(BUNDLE_CONDITION_TAG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_URL_TAG, URLstr);
        outState.putString(BUNDLE_CONDITION_TAG, conditionstr);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());   //compatibility
        b.setTitle(UI_TITLE);

        //right out of dev tutorials - http://developer.android.com/guide/topics/ui/dialogs.html
        b.setItems(UI_ITEMS, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d("GraphSelection", "click: " + which);
                switch(which) {
                    case 0: selectListener.launchWebContent(URLstr + "day." + conditionstr + ".png", "Last 24 hours");
                        break;
                    case 1: selectListener.launchWebContent(URLstr + "week." + conditionstr + ".png", "Last week");
                        break;
                    case 2: selectListener.launchWebContent(URLstr + "month." + conditionstr + ".png", "Last month");
                        break;
                    default: selectListener.launchWebContent(URLstr + "day." + conditionstr + ".png", "Last 24 hours");
                        break;
                }
            }
        });

        return b.create();
    }


    public interface interactionListener {
        void launchWebContent(String url, String title);
    }
}
