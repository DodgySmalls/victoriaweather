package ca.victoriaweather.victoriaweather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AlertsActivity extends AppCompatActivity {

    private AlertListFragment mAlertListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        if(savedInstanceState == null) {
            mAlertListFragment = new AlertListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.alert_body, mAlertListFragment, AlertListFragment.FM_TAG).commit();
        } else {
            mAlertListFragment = (AlertListFragment)getSupportFragmentManager().findFragmentByTag(AlertListFragment.FM_TAG);
        }
    }

    //TODO alert display and fetching
}
