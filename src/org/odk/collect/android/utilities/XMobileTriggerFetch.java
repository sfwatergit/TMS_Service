package org.odk.collect.android.utilities;

import android.os.AsyncTask;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Purpose:
 * - Instantiated by TriggerManagerIntentService to get Triggers.
 * - Get xml upon instantiation
 * - Passes downloading of xml to DownloadXmlTask
 * - Parses xml using xmlPullParse
 * - A call to getTrigger will return the Trigger for the form id and question
 * <p/>
 * TODO: Will probably need to instantiate as Task.
 *
 * @author sid.feygin @berkeley.edu
 * @date: 4/20/13
 * @time: 12:40 PM
 */
public class XMobileTriggerFetch {

    //Eventually, this should be based on preference in user info (i.e., it will retrieve the appropriate URL from a
    // REST interface at xMobile.org.


    //Use shared preferences to maintain state
    public static final String PREFS_NAME = "xmobilePrefs";

    //parser stuff
    private static final String ns = null;


    /**
     * Instantiates a new X mobile trigger fetch.
     *
     * @param urlString the feed url
     */
    public String fetchTriggers(String urlString) throws ExecutionException{

        String triggerString = null;

        DownloadXmlTask xmlTask = new DownloadXmlTask();
        xmlTask.execute(urlString);
        try {
            triggerString = xmlTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        return triggerString;
    }


    private List<Trigger> parseTriggers(InputStream xmlStream) throws XmlPullParserException, IOException {

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(xmlStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            xmlStream.close();
        }


    }

    private List<Trigger> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Trigger> triggers = new ArrayList<Trigger>();
        parser.require(XmlPullParser.START_TAG, ns, "triggers");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            //Starts by looking for the trigger tag
            if (name.equals("trigger")) {
                triggers.add(readTrigger(parser));
            } else {
                skip(parser);
            }
        }
        return triggers;
    }

    private Trigger readTrigger(XmlPullParser parser) throws IOException, XmlPullParserException {
        String type = null;
        String qid = null;
        String tDef = null;

        parser.require(XmlPullParser.START_TAG, ns, "trigger");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tag = parser.getName();
            type = parser.getAttributeValue(null, "type");
            qid = parser.getAttributeValue(null, "qid");
            if (tag.equals("trigger")) {
                tDef = readText(parser);
            }
            parser.require(XmlPullParser.END_TAG, ns, "trigger");
        }
        return new Trigger(qid, type, tDef);

    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    /**
     * The type Trigger.
     * Consists of a key,value pair of question id and trigger type.
     */


    public static class Trigger {
        private final String TRIGGER_QID;
        private final String TRIGGER_TYPE;
        private final String TRIGGER_DEF;

        private Trigger(String qid, String type, String tDef) {
            TRIGGER_QID = qid;
            TRIGGER_TYPE = type;
            TRIGGER_DEF = tDef;
        }

    }

    //Subclass to download xml and return parsed triggers as string
    class DownloadXmlTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            try {
                return String.valueOf(getXmlFromUrl(urls[0]));
            } catch (IOException e) {
                return "error";
            } catch (XmlPullParserException e) {
                return "error";
            }
        }

        private List<Trigger> getXmlFromUrl(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            List<Trigger> triggers = null;

            try {
                stream = downloadUrl(urlString);
                triggers = parseTriggers(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return triggers;
        }

        /**
         * Download xml from url.
         *
         * @param urlString the urlString
         * @return the xml from url
         */
        public InputStream downloadUrl(String urlString) throws IOException {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /*milliseconds*/);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            return conn.getInputStream();


        }
    }


}
