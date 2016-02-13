package ca.victoriaweather.victoriaweather;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

//Design pattern (nice if you want a more general implementation of asynctask callbacks tied to an activity):
// http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html

public class BitmapRetainerFragment extends Fragment {
    public static final String FM_TAG = "FRAGMENT_BITMAP_RETAINER";
    private PostExecuteCallback mCallbacks;
    private DownloadImageTask mTask;
    private Bitmap mBitmap;

    interface PostExecuteCallback {
        void onPostExecute(Bitmap result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();

        mTask = new DownloadImageTask();
        mTask.execute(args.getString(WebImageActivity.ARG_URL));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (PostExecuteCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interactionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageTask() {}

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
            mBitmap = result;
            mCallbacks.onPostExecute(result);
        }
    }
}
