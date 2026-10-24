package com.circledev.notes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Single source of truth for notes, to-dos, trash and chat history. */
public class Store {

    private static Store instance;

    private final Context app;
    public final List<Note> notes = new ArrayList<>();
    public final List<Todo> todos = new ArrayList<>();
    public final List<Item> trash = new ArrayList<>();
    public final List<ChatMsg> chat = new ArrayList<>();
    public int widgetStyle = 0;
    public int columns = 2;
    public long lastSave = 0;

    public static synchronized Store get(Context context) {
        if (instance == null) {
            instance = new Store(context.getApplicationContext());
            instance.load();
        }
        return instance;
    }

    private Store(Context context) {
        this.app = context;
    }

    private File internalFile() {
        return new File(new File(app.getFilesDir(), "noteflow"), "native.json");
    }

    private File legacyFile() {
        return new File(new File(app.getFilesDir(), "noteflow"), "data.json");
    }

    private File backupFile() {
        File ext = app.getExternalFilesDir(null);
        if (ext == null) return null;
        return new File(new File(ext, "NoteFlow"), "backup.json");
    }

    private static String read(File f) {
        if (f == null || !f.exists()) return null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void load() {
        File[] candidates = {internalFile(), legacyFile(), backupFile()};
        for (File candidate : candidates) {
            String json = read(candidate);
            if (json == null) continue;
            try {
                parse(json, true);
                return;
            } catch (Exception ignored) {
                // try the next layer
            }
        }
        SharedPreferences prefs = ThemeManager.prefs(app);
        String cached = prefs.getString("cache_json", null);
        if (cached != null) {
            try {
                parse(cached, true);
                return;
            } catch (Exception ignored) {
                // fall through to seed
            }
        }
        seed();
        save();
    }

    private void seed() {
        Note n1 = new Note();
        n1.id = 1;
        n1.title = "AI & Machine Learning 🤖";
        n1.body = "Perkembangan AI 2024\n\n• GPT-4o – multimodal real-time\n• Gemini Ultra – melampaui human expert\n• Llama 3 – open source\n\nAI bukan pengganti manusia, tapi penguat kemampuan.";
        n1.color = "cb";
        n1.tag = "teknologi";
        n1.pinned = true;
        n1.date = Util.today();
        n1.wc = Util.wordCount(n1.body);
        notes.add(n1);

        Note n2 = new Note();
        n2.id = 2;
        n2.title = "Android 15 Features 📱";
        n2.body = "Fitur Utama\n\n1. Adaptive Refresh Rate\n2. Satellite Messaging\n3. Material You v3\n\nRilis: Q3 2024";
        n2.color = "cg";
        n2.tag = "teknologi";
        n2.date = Util.today();
        n2.wc = Util.wordCount(n2.body);
        notes.add(n2);

        Note n3 = new Note();
        n3.id = 3;
        n3.title = "Next.js 14 vs Remix ⚡";
        n3.body = "Fitur | Next.js | Remix\nRendering | SSR/SSG/ISR | SSR\nData | Server Actions | Loaders";
        n3.color = "cy";
        n3.tag = "teknologi";
        n3.date = Util.today();
        n3.wc = Util.wordCount(n3.body);
        notes.add(n3);

        Note n4 = new Note();
        n4.id = 4;
        n4.title = "Sprint Q1 💼";
        n4.body = "✅ Login SSO\n🔄 Optimasi DB\n❌ Dark mode – ditunda";
        n4.color = "cp";
        n4.tag = "kerja";
        n4.date = Util.today();
        n4.wc = Util.wordCount(n4.body);
        notes.add(n4);

        Todo t = new Todo();
        t.id = 10;
        t.title = "🚀 Sprint Dev";
        t.color = "cb";
        t.date = Util.today();
        Todo.TodoItem i1 = new Todo.TodoItem("Setup monorepo");
        i1.id = 101;
        i1.done = true;
        Todo.TodoItem i2 = new Todo.TodoItem("JWT + refresh token");
        i2.id = 102;
        Todo.TodoItem i3 = new Todo.TodoItem("Deploy Vercel");
        i3.id = 103;
        t.items.add(i1);
        t.items.add(i2);
        t.items.add(i3);
        todos.add(t);
    }

    public synchronized String toJson(boolean full) {
        try {
            JSONObject root = new JSONObject();
            JSONArray na = new JSONArray();
            for (Note n : notes) na.put(n.toJson());
            JSONArray ta = new JSONArray();
            for (Todo t : todos) ta.put(t.toJson());
            JSONArray xa = new JSONArray();
            for (Item i : trash) {
                if (i instanceof Note) xa.put(((Note) i).toJson());
                else if (i instanceof Todo) xa.put(((Todo) i).toJson());
            }
            root.put("notes", na);
            root.put("todos", ta);
            root.put("trash", xa);
            if (full) {
                JSONArray ca = new JSONArray();
                for (ChatMsg m : chat) ca.put(m.toJson());
                root.put("chat", ca);
                root.put("widgetStyle", widgetStyle);
                root.put("columns", columns);
                root.put("savedAt", System.currentTimeMillis());
            }
            return root.toString(full ? 2 : 0);
        } catch (Exception e) {
            return "{}";
        }
    }

    public synchronized void save() {
        try {
            String json = toJson(true);
            File internal = internalFile();
            File dir = internal.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            File tmp = new File(internal.getPath() + ".tmp");
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            }
            if (!tmp.renameTo(internal)) {
                try (FileOutputStream out = new FileOutputStream(internal)) {
                    out.write(json.getBytes(StandardCharsets.UTF_8));
                }
                tmp.delete();
            }
            File backup = backupFile();
            if (backup != null) {
                File bdir = backup.getParentFile();
                if (bdir != null && !bdir.exists()) bdir.mkdirs();
                try (FileOutputStream out = new FileOutputStream(backup)) {
                    out.write(json.getBytes(StandardCharsets.UTF_8));
                }
            }
            ThemeManager.prefs(app).edit().putString("cache_json", json).apply();
            lastSave = System.currentTimeMillis();
            NoteWidgetProvider.updateAll(app);
        } catch (Exception ignored) {
        }
    }

    private void parse(String json, boolean replace) throws Exception {
        JSONObject root = new JSONObject(json);
        if (!root.has("notes") && !root.has("todos")) {
            throw new Exception("Format tidak dikenal");
        }
        if (replace) {
            notes.clear();
            todos.clear();
            trash.clear();
            chat.clear();
        }
        JSONArray na = root.optJSONArray("notes");
        if (na != null) {
            for (int i = 0; i < na.length(); i++) {
                JSONObject o = na.optJSONObject(i);
                if (o != null) notes.add(Note.fromJson(o));
            }
        }
        JSONArray ta = root.optJSONArray("todos");
        if (ta != null) {
            for (int i = 0; i < ta.length(); i++) {
                JSONObject o = ta.optJSONObject(i);
                if (o != null) todos.add(Todo.fromJson(o));
            }
        }
        JSONArray xa = root.optJSONArray("trash");
        if (xa != null) {
            for (int i = 0; i < xa.length(); i++) {
                Item item = parseItem(xa.optJSONObject(i));
                if (item != null) trash.add(item);
            }
        }
        JSONArray ca = root.optJSONArray("chat");
        if (ca != null) {
            for (int i = 0; i < ca.length(); i++) {
                JSONObject o = ca.optJSONObject(i);
                if (o != null) chat.add(ChatMsg.fromJson(o));
            }
        }
        widgetStyle = root.optInt("widgetStyle", widgetStyle);
        columns = root.optInt("columns", 2) == 1 ? 1 : 2;
    }

    private static Item parseItem(JSONObject o) {
        if (o == null) return null;
        if ("todo".equals(o.optString("type")) || o.has("items")) {
            return Todo.fromJson(o);
        }
        return Note.fromJson(o);
    }

    public synchronized void importJson(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        if (!root.has("notes") && !root.has("todos")) {
            throw new Exception("File bukan backup NoteFlow");
        }
        notes.clear();
        todos.clear();
        trash.clear();
        parse(json, false);
        save();
    }

    public String exportJson() {
        try {
            JSONObject root = new JSONObject();
            JSONArray na = new JSONArray();
            for (Note n : notes) na.put(n.toJson());
            JSONArray ta = new JSONArray();
            for (Todo t : todos) ta.put(t.toJson());
            JSONArray xa = new JSONArray();
            for (Item i : trash) {
                if (i instanceof Note) xa.put(((Note) i).toJson());
                else if (i instanceof Todo) xa.put(((Todo) i).toJson());
            }
            root.put("notes", na);
            root.put("todos", ta);
            root.put("trash", xa);
            root.put("savedAt", System.currentTimeMillis());
            return root.toString(2);
        } catch (Exception e) {
            return "{}";
        }
    }

    public Note noteById(long id) {
        for (Note n : notes) if (n.id == id) return n;
        return null;
    }

    public Todo todoById(long id) {
        for (Todo t : todos) if (t.id == id) return t;
        return null;
    }

    public void moveNoteToTrash(Note n) {
        notes.remove(n);
        n.deletedAt = System.currentTimeMillis();
        trash.add(0, n);
        save();
    }

    public void moveTodoToTrash(Todo t) {
        todos.remove(t);
        t.deletedAt = System.currentTimeMillis();
        trash.add(0, t);
        save();
    }

    public void emptyTrash() {
        trash.clear();
        save();
    }

    public int totalTasks() {
        int total = 0;
        for (Todo t : todos) total += t.items.size();
        return total;
    }

    public int doneTasks() {
        int done = 0;
        for (Todo t : todos) done += t.doneCount();
        return done;
    }
}
