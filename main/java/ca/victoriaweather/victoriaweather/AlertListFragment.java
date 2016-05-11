package ca.victoriaweather.victoriaweather;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AlertListFragment extends ListFragment {
    public static final String FM_TAG = "FRAGMENT_ALERT_LIST";

    private List<Alert> mAlertList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState == null) {
            mAlertList = new ArrayList<Alert>();
            setListAdapter(new ArrayAdapter<Alert>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mAlertList));
            new AlertFetcherTask().execute("null");
        }
    }

    private class AlertFetcherTask extends AsyncTask<String, Void, List<Alert>> {
        //TODO SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        private static final String ALERT_XML_URL = "http://victoriaweather.ca/stations/latest/app_alerts.xml";
        private static final String ALERT_OBJ_STR = "alert";
        private Exception mRuntimeException;
        private boolean mProceed = false;

        protected void onPreExecute() {
            try {
                mProceed = ((WeatherApp)getActivity().getApplication()).isNetworkingAvailable();
            } catch (Exception e) {
                //TODO better handling
                //Internet is not connected if NPE is thrown
                Log.e("AlertFetcherFrag", "Network exception stack trace", e);
                Toast.makeText(getContext(), "Couldn't refresh.\r\nPlease check your network preference and connectivity.", Toast.LENGTH_LONG).show();
                Log.d("AlertFetcherFrag", "No network found.");
                mProceed = false;
            }
        }

        protected List<Alert> doInBackground(String... params) {
            ArrayList<Alert> aList = new ArrayList<Alert>();
            if(mProceed) {
                try {
                    URL url = new URL(AlertFetcherTask.ALERT_XML_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder;
                    Document xmlDoc;
                    docBuilder = docFactory.newDocumentBuilder();
                    xmlDoc = docBuilder.parse(in);
                    xmlDoc.getDocumentElement().normalize();

                    NodeList stationXMLList = xmlDoc.getElementsByTagName(ALERT_OBJ_STR);
                    for (int i = 0; i < stationXMLList.getLength(); i++) {
                        Node n = stationXMLList.item(i);
                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            Element e = (Element) n;
                            if (e.getElementsByTagName(Alert.ATTR_ID).item(0) == null) {
                                Log.d("AlertFetcherTask", "There was an unidentified alert within the XML");
                                continue;
                            }

                            Alert a = new Alert(e.getElementsByTagName(Alert.ATTR_ID).item(0).getTextContent());
                            NodeList valuelist = e.getChildNodes();
                            for (int j = 0; j < valuelist.getLength(); j++) {
                                if (valuelist.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                    a.putAttribute(((Element) valuelist.item(j)).getTagName(), valuelist.item(j).getTextContent());
                                }
                            }

                            aList.add(a);
                        }
                    }
                } catch (Exception e) {

                /*} catch (MalformedURLException e) {
                    //should be unreachable
                } catch (IOException e) {

                } catch (ParserConfigurationException e) {
                    //should be unreachable
                } catch (SAXException e) {*/

                }
            }
            return aList;
        }

        protected void onPostExecute(List<Alert> list) {
            for(Alert a : list) {
                mAlertList.add(a);
            }

            ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
        }
    }
}


