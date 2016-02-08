package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements ObservationListFragment.interactionListener, FavouriteListFragment.interactionListener, ObservationFetcherFragment.networkListener {
    private static final String BUNDLE_FIRST_LOAD = "BUNDLE_FIRST_LOAD";
    private static final String BUNDLE_OBSERVATIONS = "BUNDLE_ALL_OBSERVATIONS";
    private static final String FAVOURITES_FILENAME = "VICTORIAWEATHER_FAVOURITES_LIST";
    private ArrayList<Observation> observations;
    private boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            observations = new ArrayList<Observation>();

            checkQueue((WeatherApp) getApplication());

            ObservationListFragment tempref;
            getSupportFragmentManager().beginTransaction().add(R.id.main_display_frame, tempref = ObservationListFragment.newInstanceOf(), ObservationListFragment.FM_TAG).commit();

            loadFavourites();

            ObservationFetcherFragment f = new ObservationFetcherFragment();
            f.execute((WeatherApp)getApplication());
            getSupportFragmentManager().beginTransaction().add(R.id.actionpanel_reload_pane, f, ObservationFetcherFragment.FM_TAG).commit();
        } else {
            observations = Observation.observationsFromBundleArrayList(savedInstanceState.getParcelableArrayList(BUNDLE_OBSERVATIONS));
            firstLoad = savedInstanceState.getBoolean(BUNDLE_FIRST_LOAD);

            checkQueue((WeatherApp)getApplication());

            try {
                getSupportFragmentManager().beginTransaction().attach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame)).commit();
            } catch (NullPointerException e) {
                Log.d("MainActivity", "onCreate(): Had instance state but no fragment associated with 'main_display_frame'");
            }
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        try {
            savedInstanceState.putBoolean(BUNDLE_FIRST_LOAD, firstLoad);
            savedInstanceState.putParcelableArrayList(BUNDLE_OBSERVATIONS, Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onSaveInstanceState() NullPointerException @observations or @favouriteStrings");
        }
    }

    public void onObservationSelected(Observation observation) {

        //TODO Display conditions fragment with observation
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

    public void onFavouritesListSelected(View v) {
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

    public void onObservationListSelected(View v) {
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

    public void onMapSelected(View v) {
        GoogleMapFragment fragment = (GoogleMapFragment)getSupportFragmentManager().findFragmentByTag(GoogleMapFragment.FM_TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(fragment == null) {
            Log.d("MainActivity", "onMapSelected(): NO FRAGMENT with tag \"" + GoogleMapFragment.FM_TAG + "\"");
            fragment = new GoogleMapFragment();
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.add(R.id.main_display_frame, fragment, GoogleMapFragment.FM_TAG);
        } else {

            //TODO don't detach + attach if mapFragment is already in main_display_frame (maybe for other frags as well, but most important since mapFragment glitches graphically when you do this)
            Log.d("MainActivity", "onMapSelected(): Found a fragment with tag \"" + GoogleMapFragment.FM_TAG + "\"");
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
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
        try {
            fragment = (ObservationDependentUpdatable)getSupportFragmentManager().findFragmentById(R.id.main_display_frame);
            fragment.updateObservations(Observation.observationsToBundleArrayList(observations));
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException");
        } catch (ClassCastException e) {
            Log.d("MainActivity", "onRetrievedList(): ClassCastException (ObservationDependentUpdatable)");
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

    public void toggleFavouriteFromConditionsFragment(View v) {
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

    //TODO @override ondestroy to null references to old lists (several activities/fragments need to stop leaking memory)
}
