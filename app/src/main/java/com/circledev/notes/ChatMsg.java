package com.circledev.notes;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMsg {

    public String role;
    public String text;
    public String time;

    public ChatMsg(String role, String text, String time) {
        this.role = role;
        this.text = text;
        this.time = time;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("role", role);
        o.put("text", text);
        o.put("time", time);
        return o;
    }

    public static ChatMsg fromJson(JSONObject o) {
        return new ChatMsg(o.optString("role", "ai"),
                o.optString("text", ""), o.optString("time", ""));
    }
}
