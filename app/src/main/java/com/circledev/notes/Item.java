package com.circledev.notes;

import org.json.JSONException;
import org.json.JSONObject;

public class Item {

    public long id;
    public String type = "note";
    public String title = "";
    public String color = "";
    public String date = "";
    public long deletedAt;

    public JSONObject baseJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("type", type);
        o.put("title", title);
        o.put("color", color);
        o.put("date", date);
        if (deletedAt > 0) o.put("deletedAt", deletedAt);
        return o;
    }
}
