package com.circledev.notes;

import org.json.JSONException;
import org.json.JSONObject;

/** A single formatting span stored with a note body. */
public class SpanData {

    public static final String BOLD = "b";
    public static final String ITALIC = "i";
    public static final String UNDERLINE = "u";
    public static final String STRIKE = "s";
    public static final String HIGHLIGHT = "hl";
    public static final String SIZE = "size";
    public static final String MONO = "mono";
    public static final String LINK = "link";
    public static final String ALIGN = "align";

    public int start;
    public int end;
    public String type;
    public String value;

    public SpanData() {
    }

    public SpanData(int start, int end, String type, String value) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.value = value;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("s", start);
        o.put("e", end);
        o.put("t", type);
        if (value != null) o.put("v", value);
        return o;
    }

    public static SpanData fromJson(JSONObject o) {
        SpanData s = new SpanData();
        s.start = o.optInt("s");
        s.end = o.optInt("e");
        s.type = o.optString("t");
        s.value = o.optString("v", null);
        return s;
    }
}
