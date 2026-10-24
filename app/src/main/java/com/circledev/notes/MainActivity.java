package com.circledev.notes;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQ_EXPORT_JSON = 101;
    private static final int REQ_IMPORT_JSON = 102;
    private static final int REQ_EXPORT_HTML = 103;
    private static final int REQ_EXPORT_CSV = 104;

    private Store store;
    private Theme theme;

    private LinearLayout listContainer;
    private LinearLayout chipsBar;
    private LinearLayout bottomNav;
    private EditText searchInput;
    private TextView searchClear;
    private TextView saveBar;
    private LinearLayout emptyBox;
    private TextView emptyIcon;
    private TextView emptyText;
    private LinearLayout fabWrap;
    private LinearLayout fabActions;
    private TextView fab;

    private String page = "notes";
    private String tag = "all";
    private String query = "";
    private int columns = 2;
    private boolean fabOpen;

    private String pendingWrite;
    private String pendingName;
    private String pendingMime;

    private final String[] chipTags = {"all", "teknologi", "kerja", "ide", "personal", "todo"};
    private final String[] chipLabels = {"Semua", "💻 Teknologi", "💼 Kerja", "💡 Ide",
            "👤 Personal", "☑ To-Do"};
    private final String[] navKeys = {"notes", "todo", "pinned", "ai", "focus", "trash"};
    private final String[] navIcons = {"📝", "☑️", "📌", "🤖", "🎯", "🗑️"};
    private final String[] navLabels = {"Catatan", "To-Do", "Pin", "AI", "Fokus", "Sampah"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        theme = Themer.apply(this);
        store = Store.get(this);
        columns = store.columns;

        bindViews();
        buildChips();
        buildNav();
        setupListeners();
        handleIntent(getIntent());
        render();
    }

    private void bindViews() {
        listContainer = findViewById(R.id.listContainer);
        chipsBar = findViewById(R.id.chipsBar);
        bottomNav = findViewById(R.id.bottomNav);
        searchInput = findViewById(R.id.searchInput);
        searchClear = findViewById(R.id.searchClear);
        saveBar = findViewById(R.id.saveBar);
        emptyBox = findViewById(R.id.emptyBox);
        emptyIcon = findViewById(R.id.emptyIcon);
        emptyText = findViewById(R.id.emptyText);
        fabWrap = findViewById(R.id.fabWrap);
        fabActions = findViewById(R.id.fabActions);
        fab = findViewById(R.id.fab);
    }

    private void setupListeners() {
        findViewById(R.id.btnStats).setOnClickListener(v -> Sheets.stats(this, store));
        findViewById(R.id.btnCategories).setOnClickListener(v ->
                Sheets.categories(this, store, this::applyCategory));
        findViewById(R.id.btnMenu).setOnClickListener(v -> showMenu());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.ROOT);
                searchClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                render();
            }
        });

        searchClear.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
            Util.hideKeyboard(this);
        });

        fab.setOnClickListener(v -> toggleFab());
        findViewById(R.id.fabNote).setOnClickListener(v -> {
            closeFab();
            openEditor(0);
        });
        findViewById(R.id.fabTodo).setOnClickListener(v -> {
            closeFab();
            openTodo(0);
        });

        findViewById(R.id.scrollView).setOnScrollChangeListener((v, sx, sy, osx, osy) -> {
            if (Math.abs(sy - osy) > 8) closeFab();
        });
    }

    private void buildChips() {
        chipsBar.removeAllViews();
        for (int i = 0; i < chipLabels.length; i++) {
            final String chipTag = chipTags[i];
            TextView chip = Ui.chip(this, chipLabels[i]);
            chip.setOnClickListener(v -> {
                tag = chipTag;
                page = "notes";
                updateChips();
                render();
            });
            chipsBar.addView(chip);
        }
        updateChips();
    }

    private void updateChips() {
        for (int i = 0; i < chipsBar.getChildCount(); i++) {
            View child = chipsBar.getChildAt(i);
            if (!(child instanceof TextView)) continue;
            Themer.selectChip((TextView) child, chipTags[i].equals(tag), this);
        }
    }

    private void buildNav() {
        bottomNav.removeAllViews();
        for (int i = 0; i < navKeys.length; i++) {
            final String key = navKeys[i];
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, Util.dp(this, 5), 0, Util.dp(this, 5));
            item.setClickable(true);
            item.setFocusable(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            item.setLayoutParams(lp);

            TextView icon = new TextView(this);
            icon.setText(navIcons[i]);
            icon.setTextSize(16);
            icon.setGravity(Gravity.CENTER);
            item.addView(icon);

            TextView label = new TextView(this);
            label.setText(navLabels[i]);
            label.setTextSize(9);
            label.setGravity(Gravity.CENTER);
            item.addView(label);

            item.setOnClickListener(v -> {
                if ("ai".equals(key)) {
                    startActivity(new Intent(this, ChatActivity.class));
                    return;
                }
                if ("focus".equals(key)) {
                    startActivity(new Intent(this, FocusActivity.class));
                    return;
                }
                page = key;
                render();
            });
            bottomNav.addView(item);
        }
    }

    private void updateNav() {
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) bottomNav.getChildAt(i);
            TextView icon = (TextView) item.getChildAt(0);
            TextView label = (TextView) item.getChildAt(1);
            boolean active = page.equals(navKeys[i]);
            int color = active ? theme.primary : theme.textVariant;
            icon.setTextColor(color);
            label.setTextColor(color);
            label.setTypeface(android.graphics.Typeface.create(
                    active ? "sans-serif-medium" : "sans-serif",
                    android.graphics.Typeface.NORMAL));
        }
    }

    private void showMenu() {
        List<Sheets.Action> actions = new ArrayList<>();
        actions.add(new Sheets.Action("📊", "Statistik", "Ringkasan semua aktivitas",
                () -> Sheets.stats(this, store)));
        actions.add(new Sheets.Action("📁", "Kategori", "Filter catatan per kategori",
                () -> Sheets.categories(this, store, this::applyCategory)));
        actions.add(new Sheets.Action("📋", "Template", "8 template siap pakai",
                () -> Sheets.templates(this, this::openTemplate)));
        actions.add(new Sheets.Action("🎨", "Tema", "12 tema Material You",
                () -> Sheets.themes(this, this::recreate)));
        actions.add(new Sheets.Action("🧩", "Widget", "4 gaya widget layar utama",
                () -> Sheets.widgetStyles(this, store, this::render)));
        actions.add(new Sheets.Action("🔔", "Pengingat", "Catatan dengan pengingat",
                () -> Sheets.reminders(this, store, this::openEditor)));
        actions.add(new Sheets.Action("💾", "Simpan & Ekspor", "Backup, restore, HTML, CSV",
                this::openExport));
        actions.add(new Sheets.Action("⊞",
                columns == 2 ? "Tampilan 1 kolom" : "Tampilan 2 kolom",
                "Ubah jumlah kolom daftar", this::toggleColumns));
        Sheets.actions(this, "Menu", actions);
    }

    private void applyCategory(String value) {
        if ("pinned".equals(value)) {
            page = "pinned";
            tag = "all";
        } else if ("locked".equals(value)) {
            page = "notes";
            tag = "locked";
        } else {
            page = "notes";
            tag = value;
        }
        updateChips();
        render();
    }

    private void toggleColumns() {
        columns = columns == 2 ? 1 : 2;
        store.columns = columns;
        store.save();
        render();
        Util.snack(this, columns == 2 ? "Tampilan 2 kolom" : "Tampilan 1 kolom");
    }

    private void toggleFab() {
        fabOpen = !fabOpen;
        if (fabOpen) {
            fabActions.setVisibility(View.VISIBLE);
            fabActions.setAlpha(0f);
            fabActions.animate().alpha(1f).setDuration(140).start();
            fab.setText("✕");
            fab.setTextSize(20);
        } else {
            closeFab();
        }
    }

    private void closeFab() {
        if (!fabOpen) return;
        fabOpen = false;
        fabActions.setVisibility(View.GONE);
        fab.setText("+");
        fab.setTextSize(26);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        long noteId = intent.getLongExtra("noteId", 0);
        if (noteId > 0) {
            intent.removeExtra("noteId");
            openEditor(noteId);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (store == null) return;
        theme = ThemeManager.current(this);
        columns = store.columns;
        render();
    }

    private void openEditor(long id) {
        Intent intent = new Intent(this, EditorActivity.class);
        if (id > 0) intent.putExtra("noteId", id);
        startActivity(intent);
    }

    private void openTemplate(int index) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("template", index);
        startActivity(intent);
    }

    private void openTodo(long id) {
        Intent intent = new Intent(this, TodoActivity.class);
        if (id > 0) intent.putExtra("todoId", id);
        startActivity(intent);
    }

    private String pageTitle() {
        switch (page) {
            case "todo":
                return "To-Do";
            case "pinned":
                return "Disematkan";
            case "trash":
                return "Sampah";
            default:
                return "Catatan";
        }
    }

    private void render() {
        theme = ThemeManager.current(this);
        if (listContainer == null) return;
        listContainer.removeAllViews();
        updateNav();

        TextView titleView = findViewById(R.id.toolbarTitle);
        titleView.setText(pageTitle());
        View chipsParent = (View) chipsBar.getParent();
        if (chipsParent != null) {
            chipsParent.setVisibility("notes".equals(page) ? View.VISIBLE : View.GONE);
        }
        fabWrap.setVisibility("trash".equals(page) ? View.GONE : View.VISIBLE);
        if ("trash".equals(page)) closeFab();

        List<Object> items = collectItems();

        if (items.isEmpty()) {
            emptyBox.setVisibility(View.VISIBLE);
            if ("trash".equals(page)) {
                emptyIcon.setText("🗑️");
                emptyText.setText("Sampah kosong.\nItem yang dihapus muncul di sini.");
            } else if ("todo".equals(page) || "todo".equals(tag)) {
                emptyIcon.setText("☑️");
                emptyText.setText("Belum ada to-do.\nTap + untuk mulai!");
            } else if ("pinned".equals(page)) {
                emptyIcon.setText("📌");
                emptyText.setText("Belum ada item disematkan.");
            } else {
                emptyIcon.setText("📝");
                emptyText.setText("Belum ada item.\nTap + untuk mulai!");
            }
            return;
        }
        emptyBox.setVisibility(View.GONE);

        if ("trash".equals(page)) {
            addTrashBar(items.size());
        }

        if ("notes".equals(page) && !"todo".equals(tag)) {
            List<Object> pinned = new ArrayList<>();
            List<Object> rest = new ArrayList<>();
            for (Object item : items) {
                if (item instanceof Note && ((Note) item).pinned) pinned.add(item);
                else rest.add(item);
            }
            if (!pinned.isEmpty()) {
                listContainer.addView(Ui.sectionLabel(this, "📌 Disematkan"));
                addCards(pinned);
            }
            if (!rest.isEmpty()) {
                if (!pinned.isEmpty()) listContainer.addView(Ui.sectionLabel(this, "Lainnya"));
                addCards(rest);
            }
        } else {
            addCards(items);
        }
    }

    private List<Object> collectItems() {
        List<Object> out = new ArrayList<>();
        boolean hasQuery = !query.isEmpty();

        if ("trash".equals(page)) {
            out.addAll(store.trash);
            return out;
        }
        if ("todo".equals(page)) {
            for (Todo todo : store.todos) {
                if (!hasQuery || todo.title.toLowerCase(Locale.ROOT).contains(query)) out.add(todo);
            }
            return out;
        }
        if ("pinned".equals(page)) {
            for (Note n : store.notes) {
                if (n.pinned && matches(n)) out.add(n);
            }
            for (Todo t : store.todos) {
                if (matches(t)) out.add(t);
            }
            return out;
        }
        if ("todo".equals(tag)) {
            for (Todo t : store.todos) {
                if (matches(t)) out.add(t);
            }
            return out;
        }
        for (Note n : store.notes) {
            if ("locked".equals(tag)) {
                if (!n.locked) continue;
            } else if (!"all".equals(tag) && !tag.equals(n.tag)) {
                continue;
            }
            if (matches(n)) out.add(n);
        }
        if ("all".equals(tag)) {
            for (Todo t : store.todos) {
                if (matches(t)) out.add(t);
            }
        }
        return out;
    }

    private boolean matches(Note n) {
        if (query.isEmpty()) return true;
        String title = n.title == null ? "" : n.title.toLowerCase(Locale.ROOT);
        String body = n.body == null ? "" : n.body.toLowerCase(Locale.ROOT);
        return title.contains(query) || body.contains(query);
    }

    private boolean matches(Todo t) {
        if (query.isEmpty()) return true;
        return t.title.toLowerCase(Locale.ROOT).contains(query);
    }

    private void addTrashBar(int count) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Util.dp(this, 4), Util.dp(this, 6), Util.dp(this, 4), Util.dp(this, 6));

        TextView info = new TextView(this);
        info.setText(count + " item di sampah");
        info.setTextColor(theme.textVariant);
        info.setTextSize(12);
        row.addView(info, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView empty = new TextView(this);
        empty.setText("🗑 Kosongkan");
        empty.setTextColor(theme.error);
        empty.setTextSize(12);
        empty.setPadding(Util.dp(this, 10), Util.dp(this, 6), Util.dp(this, 10), Util.dp(this, 6));
        empty.setOnClickListener(v -> Util.confirm(this, "Kosongkan Sampah?",
                count + " item dihapus permanen!", "Hapus", () -> {
                    store.emptyTrash();
                    showSaveBar("File internal + SDCard");
                    render();
                    Util.snack(this, "Sampah dikosongkan");
                }));
        row.addView(empty);
        listContainer.addView(row);
    }

    private void addCards(List<Object> items) {
        for (int i = 0; i < items.size(); i += columns) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int used = 0;
            for (int j = 0; j < columns && i + j < items.size(); j++) {
                Object item = items.get(i + j);
                View card = item instanceof Note ? noteCard((Note) item) : todoCard((Todo) item);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                int m = Util.dp(this, 4);
                lp.setMargins(m, m, m, m);
                row.addView(card, lp);
                used++;
            }
            while (used < columns) {
                View spacer = new View(this);
                row.addView(spacer, new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                used++;
            }
            listContainer.addView(row);
        }
    }

    private View noteCard(Note note) {
        View card = getLayoutInflater().inflate(R.layout.item_note_card, listContainer, false);
        Themer.walk(card, theme, this);
        card.setBackground(Util.round(theme.cardColor(note.color), 20, this));

        TextView title = card.findViewById(R.id.cardTitle);
        TextView body = card.findViewById(R.id.cardBody);
        TextView tagView = card.findViewById(R.id.cardTag);
        TextView pin = card.findViewById(R.id.cardPin);
        TextView lock = card.findViewById(R.id.cardLock);
        TextView bell = card.findViewById(R.id.cardBell);
        TextView date = card.findViewById(R.id.cardDate);

        if (note.title == null || note.title.trim().isEmpty()) {
            title.setVisibility(View.GONE);
        } else {
            title.setText(note.title);
        }

        if (note.locked) {
            body.setText("🔒 Dikunci");
        } else {
            String preview = note.body == null ? "" : note.body.replace('\n', ' ').trim();
            body.setText(preview);
        }

        if (note.tag != null && !note.tag.isEmpty()) {
            tagView.setVisibility(View.VISIBLE);
            tagView.setText(note.tag);
        } else {
            tagView.setVisibility(View.GONE);
        }
        pin.setVisibility(note.pinned ? View.VISIBLE : View.GONE);
        lock.setVisibility(note.locked ? View.VISIBLE : View.GONE);
        bell.setVisibility(note.remind > 0 ? View.VISIBLE : View.GONE);
        date.setText(note.date == null ? "" : note.date);

        if (!"trash".equals(page)) {
            card.setOnClickListener(v -> openEditor(note.id));
        }
        return card;
    }

    private View todoCard(Todo todo) {
        View card = getLayoutInflater().inflate(R.layout.item_todo_card, listContainer, false);
        Themer.walk(card, theme, this);
        card.setBackground(Util.round(theme.cardColor(todo.color), 20, this));

        TextView title = card.findViewById(R.id.todoTitle);
        TextView count = card.findViewById(R.id.todoCount);
        TextView date = card.findViewById(R.id.todoDate);
        TextView percentView = card.findViewById(R.id.todoPercent);
        ProgressBar progress = card.findViewById(R.id.todoProgress);
        LinearLayout itemsBox = card.findViewById(R.id.todoItems);

        title.setText(todo.title);
        int done = todo.doneCount();
        int total = todo.items.size();
        int percent = todo.percent();
        count.setText(done + "/" + total);
        percentView.setText(percent + "%");
        date.setText(todo.date == null ? "" : todo.date);

        progress.setMax(100);
        progress.setProgress(percent);
        progress.setProgressTintList(ColorStateList.valueOf(theme.primary));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(theme.outlineVariant));

        int limit = Math.min(4, total);
        for (int i = 0; i < limit; i++) {
            final Todo.TodoItem item = todo.items.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, Util.dp(this, 5), 0, Util.dp(this, 5));

            TextView check = new TextView(this);
            check.setTextSize(10);
            check.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable bg = Util.circle(
                    item.done ? theme.primary : 0x00000000);
            bg.setStroke(Util.dp(this, 1.6f), theme.primary);
            check.setBackground(bg);
            check.setTextColor(theme.onPrimary);
            if (item.done) check.setText("✓");
            LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(
                    Util.dp(this, 18), Util.dp(this, 18));
            checkLp.rightMargin = Util.dp(this, 7);
            row.addView(check, checkLp);
            check.setOnClickListener(v -> {
                item.done = !item.done;
                store.save();
                showSaveBar("File internal + SDCard");
                render();
            });

            TextView label = new TextView(this);
            label.setText(item.text);
            label.setTextSize(12);
            label.setTextColor(theme.text);
            label.setMaxLines(1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            if (item.done) {
                label.setPaintFlags(label.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                label.setTextColor(theme.textVariant);
            }
            row.addView(label, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            itemsBox.addView(row);
        }
        if (total > 4) {
            TextView more = new TextView(this);
            more.setText("+" + (total - 4) + " lagi");
            more.setTextSize(10);
            more.setTextColor(theme.textVariant);
            more.setPadding(0, Util.dp(this, 3), 0, 0);
            itemsBox.addView(more);
        }

        if (!"trash".equals(page)) {
            card.setOnClickListener(v -> openTodo(todo.id));
        }
        return card;
    }

    private void showSaveBar(String location) {
        if (saveBar == null) return;
        String time = new SimpleDateFormat("HH:mm", Util.ID).format(new Date());
        saveBar.setText("Tersimpan " + time + "  ·  📁 " + location);
        saveBar.setVisibility(View.VISIBLE);
        saveBar.setAlpha(0f);
        saveBar.animate().alpha(1f).setDuration(150).start();
        saveBar.removeCallbacks(hideSaveBar);
        saveBar.postDelayed(hideSaveBar, 4000);
    }

    private final Runnable hideSaveBar = new Runnable() {
        @Override
        public void run() {
            if (saveBar == null) return;
            saveBar.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> saveBar.setVisibility(View.GONE)).start();
        }
    };

    private void openExport() {
        List<Sheets.Action> actions = new ArrayList<>();
        actions.add(new Sheets.Action("💾", "Simpan ke File HP",
                "Backup JSON ke penyimpanan pilihan", this::exportJsonFile));
        actions.add(new Sheets.Action("🔄", "Restore dari File",
                "Muat ulang data dari file backup", this::importJsonFile));
        actions.add(new Sheets.Action("📋", "Salin Semua Teks", "Salin ke clipboard",
                this::copyAllText));
        actions.add(new Sheets.Action("📄", "Ekspor HTML", "Semua catatan sebagai webpage",
                this::exportHtmlFile));
        actions.add(new Sheets.Action("📊", "Ekspor CSV", "Semua catatan sebagai spreadsheet",
                this::exportCsvFile));
        actions.add(new Sheets.Action("🗑️", "Kosongkan Sampah",
                store.trash.size() + " item di sampah", () -> Util.confirm(this,
                        "Kosongkan Sampah?", store.trash.size() + " item dihapus permanen!",
                        "Hapus", () -> {
                            store.emptyTrash();
                            showSaveBar("File internal + SDCard");
                            render();
                            Util.snack(this, "Sampah dikosongkan");
                        })));
        Sheets.actions(this, "Simpan & Ekspor", actions);
    }

    private String isoDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private void exportJsonFile() {
        pendingWrite = store.exportJson();
        pendingMime = "application/json";
        pendingName = "noteflow-backup-" + isoDate() + ".json";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, pendingName);
        startActivityForResult(intent, REQ_EXPORT_JSON);
    }

    private void importJsonFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_IMPORT_JSON);
    }

    private void exportHtmlFile() {
        StringBuilder body = new StringBuilder();
        for (Note n : store.notes) {
            body.append("<article><h2>").append(RichText.escapeHtml(n.title)).append("</h2>");
            body.append(RichText.toHtml(n));
            body.append("<p style=\"color:#888;font-size:12px\">")
                    .append(RichText.escapeHtml(n.tag)).append(" · ")
                    .append(RichText.escapeHtml(n.date)).append("</p></article><hr>");
        }
        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>NoteFlow Export</title><style>"
                + "body{font-family:sans-serif;max-width:800px;margin:40px auto;padding:0 20px;line-height:1.6}"
                + "h2{color:#006A6A}hr{border-color:#ddd;margin:24px 0}"
                + "code{background:#f0f0f0;padding:2px 5px;border-radius:3px}"
                + "blockquote{border-left:3px solid #006A6A;padding:8px 16px;background:#f0fafa;margin:8px 0}"
                + "mark{padding:0 2px}"
                + "</style></head><body><h1>📝 NoteFlow Export</h1>"
                + "<p style=\"color:#888\">" + RichText.escapeHtml(Util.today()) + " · "
                + store.notes.size() + " catatan</p><hr>" + body + "</body></html>";

        pendingWrite = html;
        pendingMime = "text/html";
        pendingName = "noteflow-export.html";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_TITLE, pendingName);
        startActivityForResult(intent, REQ_EXPORT_HTML);
    }

    private void exportCsvFile() {
        StringBuilder csv = new StringBuilder("\uFEFFID,Judul,Isi,Tag,Tanggal,Disematkan\n");
        for (Note n : store.notes) {
            csv.append(n.id).append(',')
                    .append('"').append(n.title.replace("\"", "\"\"")).append('"').append(',')
                    .append('"').append(n.body.replace("\"", "\"\"")).append('"').append(',')
                    .append(n.tag).append(',')
                    .append(n.date).append(',')
                    .append(n.pinned ? "Ya" : "Tidak").append('\n');
        }
        pendingWrite = csv.toString();
        pendingMime = "text/csv";
        pendingName = "noteflow-export.csv";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, pendingName);
        startActivityForResult(intent, REQ_EXPORT_CSV);
    }

    private void copyAllText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < store.notes.size(); i++) {
            Note n = store.notes.get(i);
            if (i > 0) sb.append("\n\n---\n\n");
            sb.append(n.title).append('\n').append(n.body);
        }
        Util.copy(this, sb.toString());
        Util.snack(this, "Disalin!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        if (requestCode == REQ_EXPORT_JSON || requestCode == REQ_EXPORT_HTML
                || requestCode == REQ_EXPORT_CSV) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new Exception("Tidak bisa membuka file");
                out.write(pendingWrite.getBytes(StandardCharsets.UTF_8));
                showSaveBar("File pilihan: " + pendingName);
                Util.snack(this, "✓ Disimpan: " + pendingName);
            } catch (Exception e) {
                Util.snack(this, "Gagal menyimpan file");
            }
            return;
        }

        if (requestCode == REQ_IMPORT_JSON) {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) throw new Exception("Tidak bisa membaca file");
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;
                while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
                store.importJson(buffer.toString("UTF-8"));
                render();
                showSaveBar("File internal + SDCard");
                Util.snack(this, "✓ Data dipulihkan dari file!");
            } catch (Exception e) {
                Util.snack(this, "File tidak valid!");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fabOpen) {
            closeFab();
            return;
        }
        if (!"notes".equals(page)) {
            page = "notes";
            tag = "all";
            updateChips();
            render();
            return;
        }
        if (!query.isEmpty()) {
            searchInput.setText("");
            return;
        }
        super.onBackPressed();
    }
}
