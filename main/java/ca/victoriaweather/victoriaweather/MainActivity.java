package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity
        implements ObservationListFragment.interactionListener,
                   FavouriteListFragment.interactionListener,
                   ObservationFetcherFragment.networkListener,
                   GraphSelectionDialogFragment.interactionListener {

    private static final String BUNDLE_FIRST_LOAD = "BUNDLE_FIRST_LOAD";
    private static final String BUNDLE_OBSERVATIONS = "BUNDLE_ALL_OBSERVATIONS";
    private static final String BUNDLE_PERCEIVED_NETWORKING = "BUNDLE_NETWORK_BOOL";
    private static final String FAVOURITES_FILENAME = "VICTORIAWEATHER_FAVOURITES_LIST";

    private ArrayList<Observation> observations;
    private boolean firstLoad = true;
    private boolean perceivedNetworking = false;


    //TODO avoid FAILED BINDER TRANSACTION
    // Upon further investigation the cause of this failure is partially uncertain but is correlated to the onSaveInstanceState() of main activity
    // (Doesn't occur with a small observationlist)
    // My guess is that activities which are created infront of this do not receive complete information about this activity's instancestate, but that is entirely irrelevant
    // No negative consequences have been observed
    // (Pending further investigation)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            observations = new ArrayList<Observation>();

            checkQueue((WeatherApp) getApplication());

            getSupportFragmentManager().beginTransaction().add(R.id.main_display_frame, ObservationListFragment.newInstanceOf(), ObservationListFragment.FM_TAG).commit();

            loadFavourites();

            getSupportFragmentManager().beginTransaction().add(R.id.actionpanel_reload_pane, new ObservationFetcherFragment(), ObservationFetcherFragment.FM_TAG).commit();
            getSupportFragmentManager().executePendingTransactions();
            attemptRefresh(null);
        } else {
            observations = Observation.observationsFromBundleArrayList(savedInstanceState.getParcelableArrayList(BUNDLE_OBSERVATIONS));
            firstLoad = savedInstanceState.getBoolean(BUNDLE_FIRST_LOAD);
            perceivedNetworking = savedInstanceState.getBoolean(BUNDLE_PERCEIVED_NETWORKING);

            if(perceivedNetworking) {
                updateNetworkProgress(true);
            } else {
                updateNetworkProgress(false);
            }
            checkQueue((WeatherApp)getApplication());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent newIntent;
        if (id == R.id.action_settings) {
            newIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(newIntent);
            return true;
        } else if (id == R.id.action_alerts) {
            newIntent = new Intent(getApplicationContext(), AlertsActivity.class);
            startActivity(newIntent);
            return true;
        } else if (id == R.id.action_about) {
            newIntent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(newIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MainActivity", "onSaveInstanceState()");
        try {
            outState.putBoolean(BUNDLE_FIRST_LOAD, firstLoad);
            outState.putBoolean(BUNDLE_PERCEIVED_NETWORKING, perceivedNetworking);
            outState.putParcelableArrayList(BUNDLE_OBSERVATIONS, Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onSaveInstanceState() NullPointerException @observations or @favouriteStrings");
        }
    }

    public void onObservationSelected(Observation observation) {
        ConditionsFragment fragment = (ConditionsFragment)getSupportFragmentManager().findFragmentByTag(ConditionsFragment.FM_TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(fragment == null) {
            Log.d("MainActivity", "onObservationSelected(): NO FRAGMENT with tag \"" + ConditionsFragment.FM_TAG + "\"");
            fragment = ConditionsFragment.newInstanceOf(observation);
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.add(R.id.main_display_frame, fragment, ConditionsFragment.FM_TAG);
        } else {
            Log.d("MainActivity", "onObservationSelected(): Found a fragment with tag \"" + ConditionsFragment.FM_TAG + "\"");
            fragment.setObservation(observation.toBundle());
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
        }
        transaction.commit();

        //TODO does this have to happen since we are always setting the fragment? just call updateObservations() in MainActivity.onCreate() (whatever fragment is in display_frame) to ensure it works across configuration changes
        if(observations.size() > 0 && !firstLoad) {
            getSupportFragmentManager().executePendingTransactions();
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        }
    }

    public void onFavouritesListSelected(View callingView) {
        FavouriteListFragment fragment = (FavouriteListFragment)getSupportFragmentManager().findFragmentByTag(FavouriteListFragment.FM_TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(fragment == null) {
            Log.d("MainActivity", "onFavouriteListSelected(): NO FRAGMENT with tag \"" + FavouriteListFragment.FM_TAG + "\"");
            fragment = FavouriteListFragment.newInstanceOf();
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.add(R.id.main_display_frame, fragment, FavouriteListFragment.FM_TAG);

        } else {
            Log.d("MainActivity", "onFavouriteListSelected(): Found a fragment with tag \"" + FavouriteListFragment.FM_TAG + "\"");
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
        }
        transaction.commit();
        if(observations.size() > 0 || !firstLoad) {
            getSupportFragmentManager().executePendingTransactions();
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        }
    }

    public void onObservationListSelected(View callingView) {
        ObservationListFragment fragment = (ObservationListFragment)getSupportFragmentManager().findFragmentByTag(ObservationListFragment.FM_TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(fragment == null) {
            Log.d("MainActivity", "onObservationListSelected(): NO FRAGMENT with tag \"" + ObservationListFragment.FM_TAG + "\"");
            fragment = ObservationListFragment.newInstanceOf();
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.add(R.id.main_display_frame, fragment, ObservationListFragment.FM_TAG);
        } else {
            Log.d("MainActivity", "onObservationListSelected(): Found a fragment with tag \"" + ObservationListFragment.FM_TAG + "\"");
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
        }
        transaction.commit();
        if(observations.size() > 0 && !firstLoad) {
            getSupportFragmentManager().executePendingTransactions();
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        }
    }

    public void onMapSelected(View callingView) {
        GoogleMapFragment fragment = (GoogleMapFragment)getSupportFragmentManager().findFragmentByTag(GoogleMapFragment.FM_TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(fragment == null) {
            Log.d("MainActivity", "onMapSelected(): NO FRAGMENT with tag \"" + GoogleMapFragment.FM_TAG + "\"");
            fragment = new GoogleMapFragment();
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.add(R.id.main_display_frame, fragment, GoogleMapFragment.FM_TAG);
        } else {
            Log.d("MainActivity", "onMapSelected(): Found a fragment with tag \"" + GoogleMapFragment.FM_TAG + "\"");
            if(!(GoogleMapFragment.FM_TAG.equals(getSupportFragmentManager().findFragmentById(R.id.main_display_frame).getTag()))) {
                transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
                transaction.attach(fragment);
            }
        }
        transaction.commit();
        if(observations.size() > 0 && !firstLoad) {
            getSupportFragmentManager().executePendingTransactions();
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        }
    }

    public void onRetrievedList() {
        ObservationDependentUpdatable fragment;

        try {
            ((WeatherApp)getApplication()).getObservationQueue();
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException (Application was not found, returning)");
            return;
        } catch (ClassCastException e) {
            Log.d("MainActivity", "onRetrievedList(): ClassCastException (Application was not found, returning)");
            return;
        }

        firstLoad = false;
        checkQueue((WeatherApp) getApplication());
        updateNetworkProgress(false);
        try {
            fragment = (ObservationDependentUpdatable)getSupportFragmentManager().findFragmentById(R.id.main_display_frame);
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException");
        } catch (ClassCastException e) {
            Log.d("MainActivity", "onRetrievedList(): ClassCastException (ObservationDependentUpdatable)");
        }

    }

    public void onFailedToRetrieveList() {
        //TODO user notification toast ( ^ better args probably after resolving networking error)
        updateNetworkProgress(false);
    }

    private void updateNetworkProgress(boolean attempt) {
        try {
            ProgressBar networkProgress = (ProgressBar) findViewById(R.id.actionpanel_networking_progressbar);
            TextView onClickRefresh = (TextView) findViewById(R.id.action_panel_refresh);

            //Gross that we set the tint every time we update the ui but on older API versions xml cannot be used to specify parameters, cleaner than loads of resource-vXX files
            //This also applies the accent to the drawable meaning all other progress bars which reference this drawable will receive this tint
            networkProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.theme_gold), PorterDuff.Mode.MULTIPLY);

            //TODO not sure if this is wasteful (might still be drawing to a view that just isn't displayed?)
            if(!attempt) {
                perceivedNetworking = false;
                networkProgress.setVisibility(View.GONE);
                onClickRefresh.setVisibility(View.VISIBLE);
            } else {
                perceivedNetworking = true;
                networkProgress.setVisibility(View.VISIBLE);
                onClickRefresh.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            Log.d("MainActivity", "updateNetworkProgress(): NullPointerException");
        } catch (ClassCastException e) {
            Log.d("MainActivity", "updateNetworkProgress(): ClassCastException (ObservationDependentUpdatable)");
        }

    }

    //takes WeatherApp as an argument to ensure that we note when the activity is trying to find it's associated application
    public void checkQueue(WeatherApp app) {
        Queue<Observation> q = app.getObservationQueue();
        Observation o;
        int index;
        while ((o = q.poll()) != null) {
            if((index = Observation.listContainsObservation(observations, o)) != -1) {
                if(observations.get(index).isFavourite()) {
                    o.setFavourite(true);                   //incoming web data doesn't know about local application variables, so we flag incoming stations that are already favourites
                }
                observations.remove(index);
            }
            Observation.addObservationLexicographic(observations, o);
        }
    }

    public void toggleFavouriteFromConditionsFragment(View callingView) {
        try {
            ConditionsFragment fragment = (ConditionsFragment) getSupportFragmentManager().findFragmentByTag(ConditionsFragment.FM_TAG);
            fragment.getObservation();

            for(Observation o : observations) {
                if(o.getId().equals(fragment.getObservation().getId())) {
                    if(fragment.getObservation().isFavourite()) {
                        o.setFavourite(false);
                        break;
                    } else {
                        o.setFavourite(true);
                        break;
                    }
                }
            }

            ((ObservationDependentUpdatable)getSupportFragmentManager().findFragmentById(R.id.main_display_frame)).updateObservations(Observation.observationsToBundleArrayList(observations));
            pushCurrentFavourites();
        }   catch (NullPointerException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): NullPointerException");
            return;
        } catch (ClassCastException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): ClassCastException (ConditionsFragment)");
            return;
        }
    }

    public void setFavouriteStatus(Bundle bundledObservation, boolean b) {
        int index;
        if((index = Observation.listContainsObservation(observations, Observation.fromBundle(bundledObservation))) != -1) {
            ((Observation)observations.get(index)).setFavourite(b);
        } else {
            Log.d("MainActivity", "setFavouriteStatus(): Retrieved an observation favourite change request for a station which was not currently in observations");
        }

        pushCurrentFavourites();

        try {
            ((ObservationDependentUpdatable) getSupportFragmentManager().findFragmentById(R.id.main_display_frame)).updateObservations(Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("MainActivity", "setFavouriteStatus(): NullPointerException");
        } catch (ClassCastException e) {
            Log.d("MainActivity", "setFavouriteStatus(): ClassCastException (ObservationDependentUpdatable)");
        }
    }

    //writes the current set of favourited stations into an external file
    public void pushCurrentFavourites() {
        Log.d("MainActivity", "Writing favourites to file");
        try {
            FileOutputStream fos = openFileOutput(FAVOURITES_FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for(Observation o : observations) {
                if(o.isFavourite()) {
                    try {
                        osw.append(o.getId());
                        osw.append("\r\n");
                        try {
                            osw.append(o.getAttribute(Observation.ATTR_NAME));
                        } catch (NullPointerException e) {
                            osw.append("Savename_error");
                        }
                        osw.append("\r\n");
                        try {
                            osw.append(o.getAttribute(Observation.ATTR_NAME_LONG));
                        } catch (NullPointerException e) {
                            osw.append("Savelongname_error");
                        }
                        osw.append("\r\n");
                    } catch (IOException e) {
                        Log.d("MainActivity", "pushCurrentFavourites(): error while writing \" " + o.toString() + "\" to favourites file");
                    }
                }
            }
            try {
                osw.flush();
                osw.close();
                fos.flush();
                fos.close();
            }catch (IOException e) {
                Log.d("MainActivity", "pushCurrentFavourites(): failed to close FileOutputStream or OutputStreamWriter");
            }
        } catch(FileNotFoundException e) {
            //should be unreachable due to Context.MODE_PRIVATE unless the OS denies us the ability to generate a file
            Log.d("MainActivity", "pushCurrentFavourites(): File not found");
        }
    }

    public void loadFavourites() {
        Log.d("MainActivity", "loadFavourites() attempting to load favourites from file.");
        Observation o;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(FAVOURITES_FILENAME)));
            for(String line; (line = br.readLine()) != null; ) {
                o = new Observation(line);
                o.putAttribute(Observation.ATTR_ID, o.getId());
                Log.d("MainActivity", "loadFavourites() got obs, id:" + o.getId());
                line = br.readLine();
                if(line == null) {
                    Log.d("MainActivity", "loadFavourites(): Bad file (buffer was null when a name should've been supplied");
                } else {
                    o.putAttribute(Observation.ATTR_NAME, line);
                }
                line = br.readLine();
                if(line == null) {
                    Log.d("MainActivity", "loadFavourites(): Bad file (buffer was null when a long name should've been supplied");
                } else {
                    o.putAttribute(Observation.ATTR_NAME_LONG, line);
                }

                o.setFavourite(true);
                o.putAttribute(Observation.ATTR_STATION_FAULT, "Unfortunately, this station could not be reached.");
                //if an observation already exists in the list, mark it as favourite, otherwise add the (empty) observation into the list with the correct names and id
                int index;
                if((index = Observation.listContainsObservation(observations, o)) != -1) {
                    observations.get(index).setFavourite(true);
                } else {
                    Observation.addObservationLexicographic(observations, o);
                }
            }
        } catch (FileNotFoundException e) {
            Log.d("MainActivity", "loadFavourites(): File not found");
            try {
                FileInputStream fos = openFileInput(FAVOURITES_FILENAME);
            } catch (Exception e2) {
                Log.d("MainActivity", "fos also didn't work");
            }
        } catch (IOException e) {
            Log.d("MainActivity", "loadFavourites(): io exception occurred");
        }
    }

    //passes appropriate arguments to a new dialog, which may then call MainActivity.launchWebContent() to put a new activity in the foreground
    public void showGraphSelectionDialog(View callingView) {
        Observation targetObs;
        GraphSelectionDialogFragment dialogFragment;
        try {
            ConditionsFragment fragment = (ConditionsFragment) getSupportFragmentManager().findFragmentByTag(ConditionsFragment.FM_TAG);
            targetObs = fragment.getObservation();

            Bundle dialogArgs = new Bundle();
            dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_STATION_NAME_TAG, targetObs.getAttribute(Observation.ATTR_NAME));

            switch(callingView.getId()) {
                case R.id.condition_temperature_row: dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_TEMPERATURE);break;
                case R.id.condition_average_wind_row:  dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_WIND);break;
                case R.id.condition_rain_row: dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_RAIN);break;
                case R.id.condition_pressure_row: dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_PRESSURE);break;
                case R.id.condition_uv_index_row:  dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_UV_INDEX);break;
                case R.id.condition_insolation_row:  dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_INSOLATION);break;
                case R.id.condition_humidity_row:  dialogArgs.putString(GraphSelectionDialogFragment.BUNDLE_CONDITION_TAG, GraphSelectionDialogFragment.CONDITION_HUMIDITY);break;
                default: Log.d("MainActivity", "showGraphSelectionDialog() could not parse calling view into a condition [" + callingView.getId() + "]");
                    return;
            }

            DialogFragment dialog = new GraphSelectionDialogFragment();
            dialog.setArguments(dialogArgs);
            dialog.show(getSupportFragmentManager(), GraphSelectionDialogFragment.FM_TAG);

        } catch (NullPointerException e) {
            Log.d("MainActivity", "showGraphSelectionDialog() null pointer when retrieving");
        } catch (ClassCastException e) {
            Log.d("MainActivity", "showGraphSelectionDialog() called but ConditionsFragment could not be retrieved");
        }
    }

    public void launchWebContent(String url, String title) {
        Intent newIntent;
        newIntent = new Intent(getApplicationContext(), WebImageActivity.class);

        Bundle args = new Bundle();
        args.putString(WebImageActivity.ARG_TITLE, title);
        args.putString(WebImageActivity.ARG_URL, url);
        newIntent.putExtras(args);

        startActivity(newIntent);
    }

    public void zoomToCurrentStation(View callingView) {
        try {
            ConditionsFragment fragment = (ConditionsFragment) getSupportFragmentManager().findFragmentByTag(ConditionsFragment.FM_TAG);
            Observation currentObs = fragment.getObservation();

            if(currentObs.hasLatLng()) {
                onMapSelected(null);
                GoogleMapFragment f = (GoogleMapFragment)getSupportFragmentManager().findFragmentByTag(GoogleMapFragment.FM_TAG);
                f.moveToLatLng(currentObs.getLatLng());
            }

        }   catch (NullPointerException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): NullPointerException");
            return;
        } catch (ClassCastException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): ClassCastException (ConditionsFragment)");
            return;
        }
    }

    public void openCurrentStationBrowser(View v) {
        try {
            ConditionsFragment fragment = (ConditionsFragment) getSupportFragmentManager().findFragmentByTag(ConditionsFragment.FM_TAG);
            Observation currentObs = fragment.getObservation();

            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Observation.STATION_DETAILS_URL + currentObs.getId())));

        }   catch (NullPointerException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): NullPointerException");
            return;
        } catch (ClassCastException e) {
            Log.d("MainActivity", "toggleFavouriteFromConditionsFragment(): ClassCastException (ConditionsFragment)");
            return;
        }
    }


    public void attemptRefresh(View callingView) {
        try {
            ObservationFetcherFragment fragment = (ObservationFetcherFragment) getSupportFragmentManager().findFragmentByTag(ObservationFetcherFragment.FM_TAG);
            if(fragment.execute((WeatherApp) getApplication())) {
                updateNetworkProgress(true);
            } else {
                Log.d("MainActivity", "attemptRefresh() did not fire due to internal logic");
            }
        } catch (Exception e) {
            Log.d("MainActivity", "attemptRefresh() refresh attempt failed due to exception");
        }
    }

    public Bundle shortNameToObservationBundle(String shortName) {
        for(Observation o : observations) {
            if(o.hasAttribute(Observation.ATTR_NAME)) {
                if(o.getAttribute(Observation.ATTR_NAME).equals(shortName)) {
                    return o.toBundle();
                }
            }
        }
        return null;
    }
    //TODO @override ondestroy to null references to old lists (several activities/fragments need to stop leaking memory)
}
