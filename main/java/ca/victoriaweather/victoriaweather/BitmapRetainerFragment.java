package ca.victoriaweather.victoriaweather;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

//Design pattern (nice if you want a more general implementation of asynctask callbacks tied to an activity):
// http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html

public class BitmapRetainerFragment extends Fragment {
    public static final String FM_TAG = "FRAGMENT_BITMAP_RETAINER";
    private static final String LRU_BMP_KEY = "MY_BITMAP";
    private PostExecuteCallback mCallbacks;
    private DownloadImageTask mTask;
    private LruCache<String, Bitmap> mMemoryCache;


    interface PostExecuteCallback {
        void onPostExecute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

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
        return mMemoryCache.get(LRU_BMP_KEY);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        public DownloadImageTask() {}

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                //BitmapFactory.Options bmfopts = new BitmapFactory.Options();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            try{Thread.sleep(2000);}catch(Exception e) {}
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            mMemoryCache.put(LRU_BMP_KEY, result);
            mCallbacks.onPostExecute();
        }
    }
}
