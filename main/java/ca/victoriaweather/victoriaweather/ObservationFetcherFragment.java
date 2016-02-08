package ca.victoriaweather.victoriaweather;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ObservationFetcherFragment extends Fragment {
    public static final String FM_TAG = "FRAGMENT_OBSERVATION_FETCHER";

    public static ObservationFetcherFragment newInstance() {
        ObservationFetcherFragment off = new ObservationFetcherFragment();
        //setArguments();
        return off;
    }

    private networkListener networkListener;

    public ObservationFetcherFragment() {

    }

    public void execute(WeatherApp app) {
        new FetchObservationListTask().execute(app);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkListener = (networkListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement interactionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        networkListener = null;
    }

    public interface networkListener {
        void onRetrievedList();
    }

    private class FetchObservationListTask extends AsyncTask<WeatherApp, String, List<Observation>> {
        private WeatherApp app;
        private static final String STATION_XML_URL = "http://www.victoriaweather.ca/stations/latest/allcurrent.xml";
        private Exception runtimeException;

        //currently priority isn't used and the whole list is always updated
        protected List<Observation> doInBackground(WeatherApp... app) {
            List<Observation> acq = new ArrayList<Observation>();
            try {
                URL url = new URL(FetchObservationListTask.STATION_XML_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                publishProgress("Input stream opened"); Log.d("FetchStationListTask", "Fetched XML");

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder;
                Document xmlDoc;

                docBuilder = docFactory.newDocumentBuilder();
                xmlDoc = docBuilder.parse(in);
                xmlDoc.getDocumentElement().normalize();

                publishProgress("XML parsed + normalized");

                NodeList stationXMLList = xmlDoc.getElementsByTagName("current_observation");
                Log.d("FetchStationListTask", "Station count: " + Integer.toString(stationXMLList.getLength()));
                for (int i = 0; i < stationXMLList.getLength(); i++) {
                    Node n = stationXMLList.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) n;
                        if (e.getElementsByTagName(Observation.ATTR_ID).item(0) == null) {
                            //Station data is considered unrecoverable if ID is not present in the xml
                            Log.d("FetchStationListTask", "There was an unidentified station within the XML");
                            continue;
                        }

                        Observation o = new Observation(e.getElementsByTagName(Observation.ATTR_ID).item(0).getTextContent());
                        NodeList valuelist = e.getChildNodes();
                        for (int j = 0; j < valuelist.getLength(); j++) {
                            if (valuelist.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                o.putAttribute(((Element) valuelist.item(j)).getTagName(), valuelist.item(j).getTextContent());
                            }
                        }

                        acq.add(o);
                    }
                }

                urlConnection.disconnect();
                in.close();
            } catch (Exception e) {
                runtimeException = e;
                Log.d("FetchStationListTask", "EXCEPTION" + e.getClass().getName());
            }

            //TODO: CLEANUP PARAMETER PASSING -> doInBackground & -> onPostExecute
            try {Thread.sleep(5000);}catch(Exception e) {}

            Queue<Observation> messages = app[0].getObservationQueue();
            for(Observation o : acq) {
                messages.add(o);
            }

            publishProgress("finished");
            return acq;
        }

        protected void onProgressUpdate(String... item) {

        }

        protected void onPostExecute(List<Observation> l) {
            Log.d("FetchStationListTask", "Post Execute.");

            networkListener.onRetrievedList();
            //app.setStationLock(false);

            //TODO handle runtimeException
        }
    }
}
