package com.circledev.notes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Todo extends Item {

    public static class TodoItem {
        public long id;
        public String text = "";
        public boolean done;

        public TodoItem() {
        }

        public TodoItem(String text) {
            this.id = System.currentTimeMillis() ^ (long) (Math.random() * 0xFFFF);
            this.text = text;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("text", text);
            o.put("done", done);
            return o;
        }

        public static TodoItem fromJson(JSONObject o) {
            TodoItem i = new TodoItem();
            i.id = o.optLong("id", System.currentTimeMillis());
            i.text = o.optString("text", "");
            i.done = o.optBoolean("done", false);
            return i;
        }
    }

    public List<TodoItem> items = new ArrayList<>();

    public Todo() {
        type = "todo";
    }

    public int doneCount() {
        int done = 0;
        for (TodoItem i : items) if (i.done) done++;
        return done;
    }

    public int percent() {
        return items.isEmpty() ? 0 : Math.round(doneCount() * 100f / items.size());
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = baseJson();
        JSONArray arr = new JSONArray();
        for (TodoItem i : items) arr.put(i.toJson());
        o.put("items", arr);
        return o;
    }

    public static Todo fromJson(JSONObject o) {
        Todo t = new Todo();
        t.id = o.optLong("id", System.currentTimeMillis());
        t.title = o.optString("title", "To-Do");
        t.color = o.optString("color", "");
        t.date = o.optString("date", Util.today());
        JSONArray arr = o.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject io = arr.optJSONObject(i);
                if (io != null) t.items.add(TodoItem.fromJson(io));
            }
        }
        return t;
    }
}
