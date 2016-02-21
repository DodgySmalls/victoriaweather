package ca.victoriaweather.victoriaweather;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutURL = (TextView) findViewById(R.id.about_network_about_details);
        TextView aboutVariablesURL = (TextView) findViewById(R.id.about_network_variables);

        aboutURL.setOnClickListener(this);
        aboutVariablesURL.setOnClickListener(this);

    }

    public void onClick(View view) {
        String urlStr = "http://www.victoriaweather.ca";
        switch(view.getId()) {
            case R.id.about_network_about_details: urlStr = "http://www.victoriaweather.ca/about.php";
                                                   break;
            case R.id.about_network_variables:     urlStr = "http://www.victoriaweather.ca/information.php";
                                                   break;
            default:
                break;
        }
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlStr)));
    }
}
