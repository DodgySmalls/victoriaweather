package ca.victoriaweather.victoriaweather;


import java.util.HashMap;
import java.util.Set;

public class Alert {
    public static final String ATTR_ID = "id";
    public static final String ATTR_START = "start_date";
    public static final String ATTR_END = "end_date";
    public static final String ATTR_TITLE = "title";
    public static final String ATTR_DISPLAY_OVERRIDE = "always_alert";
    public static final String ATTR_CONTENT = "body";

    private HashMap<String, String> attributes;

    public Alert(String id) {
        attributes = new HashMap<String, String>();
        attributes.put(ATTR_ID, id);
    }

    public boolean hasAttribute(String key) {
        if(attributes.containsKey(key)) {
            return true;
        }
        return false;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void putAttribute(String key, String val) {
        attributes.put(key, val);
    }

    @Override
    public String toString() {
        String output = "";
        Set<String> attrsof = attributes.keySet();
        for(String s : attrsof) {
            output += "   " + s;
        }
        return output;
    }
}
