package ca.victoriaweather.victoriaweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import java.util.ArrayList;
import java.util.List;

public class AlertListFragment extends ListFragment {
    public static final String FM_TAG = "FRAGMENT_ALERT_LIST";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private class AlertFetcherTask extends AsyncTask<String, Void, List<Alert>> {
        protected List<Alert> doInBackground(String... params) {
            ArrayList<Alert> aList = new ArrayList<Alert>();



            return aList;
        }

        protected void onPostExecute() {

        }
    }
}


