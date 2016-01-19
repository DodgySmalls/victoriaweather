package ca.victoriaweather.victoriaweather;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity implements ObservationListFragment.interactionListener, FavouriteListFragment.interactionListener, ObservationFetcherFragment.networkListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().add(R.id.main_display_frame, FavouriteListFragment.newInstanceOf(), FavouriteListFragment.FM_TAG).commit();
            //getSupportFragmentManager().beginTransaction().detach(getSupportFragmentManager().findFragmentByTag(FavouriteListFragment.FM_TAG)).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.main_display_frame, ObservationListFragment.newInstanceOf(), ObservationListFragment.FM_TAG).commit();
            ObservationFetcherFragment f = new ObservationFetcherFragment();
            f.execute();
            getSupportFragmentManager().beginTransaction().add(R.id.actionpanel_reload_pane, f, ObservationFetcherFragment.FM_TAG).commit();
        } else {
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
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
        }
        transaction.commit();
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
            Log.d("MainActivity", "onMapSelected(): Found a fragment with tag \"" + GoogleMapFragment.FM_TAG + "\"");
            transaction.detach(getSupportFragmentManager().findFragmentById(R.id.main_display_frame));
            transaction.attach(fragment);
        }
        transaction.commit();
    }

    public void onRetrievedList() {
        Log.d("MainActivity", "onRetrievedList()");
        try {
            ObservationListFragment f = (ObservationListFragment)getSupportFragmentManager().findFragmentByTag(ObservationListFragment.FM_TAG);
            f.registerListAdapter();
            ((ArrayAdapter<Observation>)f.getListAdapter()).notifyDataSetChanged();
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException: -> ObservationListFragment or ObservationListFragment.getListAdapter()");
        }

        try {
            FavouriteListFragment f = (FavouriteListFragment)getSupportFragmentManager().findFragmentByTag(FavouriteListFragment.FM_TAG);
            f.registerListAdapter();
            ((ArrayAdapter<Observation>)f.getListAdapter()).notifyDataSetChanged();
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException: -> FavouriteListFragment or FavouriteListFragment.getListAdapter()");
        }

        try {
            GoogleMapFragment f = (GoogleMapFragment)getSupportFragmentManager().findFragmentByTag(GoogleMapFragment.FM_TAG);
            f.reloadPins();
        } catch (NullPointerException e) {
            Log.d("MainActivity", "onRetrievedList(): NullPointerException: -> FavouriteListFragment or FavouriteListFragment.getListAdapter()");
        }
    }
}
