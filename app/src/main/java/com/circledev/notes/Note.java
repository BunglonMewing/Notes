package com.circledev.notes;

import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note extends Item {

    public String body = "";
    public List<SpanData> spans = new ArrayList<>();
    public String tag = "teknologi";
    public boolean pinned;
    public boolean locked;
    public long remind;
    public int wc;

    public Note() {
        type = "note";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = baseJson();
        o.put("body", body);
        o.put("tag", tag);
        o.put("pinned", pinned);
        o.put("locked", locked);
        o.put("remind", remind);
        o.put("wc", wc);
        JSONArray arr = new JSONArray();
        for (SpanData s : spans) {
            if (s != null && s.start >= 0 && s.end > s.start && s.type != null) {
                arr.put(s.toJson());
            }
        }
        o.put("spans", arr);
        return o;
    }

    public static Note fromJson(JSONObject o) {
        Note n = new Note();
        n.id = o.optLong("id", System.currentTimeMillis());
        n.title = o.optString("title", "");
        n.color = o.optString("color", "");
        n.date = o.optString("date", Util.today());
        n.tag = o.optString("tag", "teknologi");
        n.pinned = o.optBoolean("pinned", false);
        n.locked = o.optBoolean("locked", false);
        n.wc = o.optInt("wc", 0);
        n.remind = parseRemind(o.opt("remind"));

        if (o.has("body")) {
            n.body = o.optString("body", "");
            JSONArray arr = o.optJSONArray("spans");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject so = arr.optJSONObject(i);
                    if (so != null) n.spans.add(SpanData.fromJson(so));
                }
            }
        } else if (o.has("html")) {
            // Data from the old WebView version.
            Spanned sp = RichText.fromLegacyHtml(o.optString("html", ""));
            n.body = sp.toString();
            n.spans = RichText.extract(sp);
        }
        if (n.wc == 0) n.wc = Util.wordCount(n.body);
        return n;
    }

    private static long parseRemind(Object raw) {
        if (raw == null) return 0;
        if (raw instanceof Number) return ((Number) raw).longValue();
        String s = String.valueOf(raw);
        if (s.isEmpty() || s.equals("null")) return 0;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {
        }
        String[] patterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(pattern, java.util.Locale.US);
                f.setLenient(true);
                java.util.Date d = f.parse(s);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {
            }
        }
        return 0;
    }
}
