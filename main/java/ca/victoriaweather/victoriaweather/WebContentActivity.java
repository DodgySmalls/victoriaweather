package ca.victoriaweather.victoriaweather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class WebContentActivity extends AppCompatActivity {
    public static final String ARG_TITLE = "ARG_WEB_CONTENT_ACTIVITY_TITLE";
    public static final String ARG_URL = "ARG_WEB_CONTENT_ACTIVITY_URL";

    //TODO compress bitmap to avoid FAILED BINDER TRANSACTION ->  http://stackoverflow.com/questions/3528735/failed-binder-transaction

    //TODO save title modification, url, and bitmap across configuration changes

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_content);
        if(savedInstanceState == null) {
            Bundle args = getIntent().getExtras();
            setTitle(args.getString(ARG_TITLE));

            ImageView imageView = (ImageView) findViewById(R.id.web_content_main_image);
            DownloadImageTask networker = new DownloadImageTask(imageView);
            networker.execute(args.getString(ARG_URL));
        } else {

        }
    }

    //TODO see if meta can be changed based on settings to allow users to avoid dialogs

    //http://stackoverflow.com/questions/25856303/narrow-dialog-when-starting-new-activity-in-theme-holo-dialogwhenlarge-mode
    @Override
    protected void onStart() {
        super.onStart();
        // In order to not be too narrow, set the window size based on the screen resolution:
        final int screen_width = getResources().getDisplayMetrics().widthPixels;
        final int screen_height = getResources().getDisplayMetrics().heightPixels;
        int new_window_width = screen_height;
        int new_window_height = screen_width;
        if(new_window_width < new_window_height) {
            new_window_height = new_window_width;
        } else {
            new_window_width = new_window_height;
        }
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.width = Math.max(layout.width, new_window_width);
        layout.height = Math.max(layout.height, new_window_height);
        getWindow().setAttributes(layout);
    }

    //code thanks to stackoverflow user Kyle Clegg (http://stackoverflow.com/questions/5776851/load-image-from-url)
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
