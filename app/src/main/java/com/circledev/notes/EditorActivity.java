package com.circledev.notes;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditorActivity extends Activity {

    private Store store;
    private Theme theme;

    private EditText titleInput;
    private EditText bodyInput;
    private LinearLayout tagBar;
    private LinearLayout colorDots;
    private LinearLayout fmtBar;
    private TextView btnPin;
    private TextView btnLock;
    private TextView reminderButton;
    private TextView wordCount;
    private TextView saveStatus;
    private LinearLayout findBar;
    private EditText findInput;
    private EditText replaceInput;
    private TextView findCount;

    private Note editing;
    private long noteId;
    private int templateIndex = -1;
    private String color = "";
    private String tag = "teknologi";
    private boolean pinned;
    private boolean locked;
    private long remind;
    private boolean deleted;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayDeque<Snapshot> undoStack = new ArrayDeque<>();
    private final ArrayDeque<Snapshot> redoStack = new ArrayDeque<>();
    private boolean suppressUndo;
    private long lastTypingPush;
    private boolean applyingHistory;

    private final Runnable autoSaveTask = () -> commit(false);

    private static class Snapshot {
        String text;
        List<SpanData> spans;
        int start;
        int end;
    }

    private final String[] tags = {"teknologi", "kerja", "ide", "personal"};
    private final String[] colors = {"", "cy", "cp", "cb", "cg", "cv", "co"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        theme = Themer.apply(this);
        store = Store.get(this);

        noteId = getIntent().getLongExtra("noteId", 0);
        templateIndex = getIntent().getIntExtra("template", -1);
        if (noteId > 0) {
            editing = store.noteById(noteId);
            if (editing == null) {
                finish();
                return;
            }
        }

        bindViews();
        setupTopBar();
        buildFormatBar();
        setupEditing();
        populate();
    }

    private void bindViews() {
        titleInput = findViewById(R.id.titleInput);
        bodyInput = findViewById(R.id.bodyInput);
        tagBar = findViewById(R.id.tagBar);
        colorDots = findViewById(R.id.colorDots);
        fmtBar = findViewById(R.id.fmtBar);
        btnPin = findViewById(R.id.btnPin);
        btnLock = findViewById(R.id.btnLock);
        reminderButton = findViewById(R.id.reminderButton);
        wordCount = findViewById(R.id.wordCount);
        saveStatus = findViewById(R.id.saveStatus);
        findBar = findViewById(R.id.findBar);
        findInput = findViewById(R.id.findInput);
        replaceInput = findViewById(R.id.replaceInput);
        findCount = findViewById(R.id.findCount);
    }

    private void setupTopBar() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            commit(false);
            finish();
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (commit(false)) {
                Util.snack(this, "✓ Catatan tersimpan");
            }
            finish();
        });
        btnPin.setOnClickListener(v -> {
            pinned = !pinned;
            updatePinLock();
            scheduleAutoSave();
        });
        btnLock.setOnClickListener(v -> {
            locked = !locked;
            updatePinLock();
            scheduleAutoSave();
        });
        findViewById(R.id.btnRead).setOnClickListener(v -> openReadMode());
        findViewById(R.id.btnShare).setOnClickListener(v -> shareNote());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteNote());

        reminderButton.setOnClickListener(v -> pickReminder());
        findViewById(R.id.btnReplace).setOnClickListener(v -> replaceAll());
        findViewById(R.id.btnFindClose).setOnClickListener(v -> {
            findBar.setVisibility(View.GONE);
            Util.hideKeyboard(this);
        });
        findInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateFindCount();
            }
        });
    }

    private void populate() {
        if (editing != null) {
            titleInput.setText(editing.title);
            bodyInput.setText(editing.body);
            RichText.apply(bodyInput.getText(), editing.spans);
            color = editing.color == null ? "" : editing.color;
            tag = editing.tag == null ? "teknologi" : editing.tag;
            pinned = editing.pinned;
            locked = editing.locked;
            remind = editing.remind;
        } else if (templateIndex >= 0 && templateIndex < Templates.all().length) {
            Templates.Template tpl = Templates.all()[templateIndex];
            titleInput.setText(tpl.title);
            bodyInput.setText(tpl.body);
            color = tpl.color;
            tag = tpl.tag;
            bodyInput.setSelection(bodyInput.getText().length());
        }
        updatePinLock();
        renderTagChips();
        renderColorDots();
        updateWordCount();
        updateReminderButton();
        saveStatus.setVisibility(View.GONE);
        if (editing == null) {
            bodyInput.postDelayed(() -> {
                bodyInput.requestFocus();
                bodyInput.setSelection(bodyInput.getText().length());
            }, 220);
        }
    }

    private void setupEditing() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (suppressUndo || applyingHistory) return;
                long now = System.currentTimeMillis();
                if (now - lastTypingPush > 2000) {
                    pushUndo(snapshot());
                    lastTypingPush = now;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (applyingHistory) return;
                updateWordCount();
                scheduleAutoSave();
            }
        };
        titleInput.addTextChangedListener(watcher);
        bodyInput.addTextChangedListener(watcher);
    }

    private void updatePinLock() {
        btnPin.setText(pinned ? "📌" : "📍");
        btnPin.setTextColor(pinned ? theme.primary : theme.textVariant);
        btnLock.setText(locked ? "🔒" : "🔓");
        btnLock.setTextColor(locked ? theme.primary : theme.textVariant);
    }

    private void renderTagChips() {
        tagBar.removeAllViews();
        for (String value : tags) {
            TextView chip = Ui.chip(this, value);
            Themer.selectChip(chip, value.equals(tag), this);
            chip.setOnClickListener(v -> {
                tag = value;
                renderTagChips();
                scheduleAutoSave();
            });
            tagBar.addView(chip);
        }
    }

    private void renderColorDots() {
        colorDots.removeAllViews();
        for (String key : colors) {
            int fill = key.isEmpty() ? theme.bg : Theme.swatch(key, theme.dark);
            boolean selected = key.equals(color);
            View dot = Ui.colorDot(this, fill, selected);
            dot.setOnClickListener(v -> {
                color = key;
                renderColorDots();
                scheduleAutoSave();
            });
            colorDots.addView(dot);
        }
    }

    private void updateReminderButton() {
        if (remind > 0) {
            reminderButton.setText("🔔 " + Util.fmtRemind(remind));
            Themer.selectPill(reminderButton, true, this);
        } else {
            reminderButton.setText("🔔 Tambah pengingat");
            Themer.selectPill(reminderButton, false, this);
        }
    }

    private void pickReminder() {
        if (remind > 0) {
            java.util.List<Sheets.Action> actions = new java.util.ArrayList<>();
            actions.add(new Sheets.Action("🕒", "Ubah jadwal", Util.fmtRemind(remind),
                    this::showDatePicker));
            actions.add(new Sheets.Action("🗑", "Hapus pengingat", "Matikan pengingat catatan ini",
                    () -> {
                        remind = 0;
                        updateReminderButton();
                        scheduleAutoSave();
                    }));
            Sheets.actions(this, "Pengingat", actions);
            return;
        }
        showDatePicker();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (remind > 0) cal.setTimeInMillis(remind);
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.YEAR, year);
            picked.set(Calendar.MONTH, month);
            picked.set(Calendar.DAY_OF_MONTH, day);
            if (remind > 0) {
                Calendar old = Calendar.getInstance();
                old.setTimeInMillis(remind);
                picked.set(Calendar.HOUR_OF_DAY, old.get(Calendar.HOUR_OF_DAY));
                picked.set(Calendar.MINUTE, old.get(Calendar.MINUTE));
            }
            new TimePickerDialog(this, (timeView, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                remind = picked.getTimeInMillis();
                updateReminderButton();
                Util.requestNotificationPermission(this);
                scheduleAutoSave();
            }, picked.get(Calendar.HOUR_OF_DAY), picked.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateWordCount() {
        wordCount.setText(Util.wordCount(bodyInput.getText().toString()) + " kata");
    }

    private void scheduleAutoSave() {
        handler.removeCallbacks(autoSaveTask);
        handler.postDelayed(autoSaveTask, 3000);
    }

    private boolean commit(boolean quiet) {
        if (deleted) return false;
        String title = titleInput.getText().toString().trim();
        String body = bodyInput.getText().toString();
        if (title.isEmpty() && body.trim().isEmpty()) {
            return false;
        }
        Note target = editing;
        if (target == null) {
            target = new Note();
            target.id = System.currentTimeMillis() ^ (long) (Math.random() * 0xFFFF);
            store.notes.add(0, target);
            editing = target;
            noteId = target.id;
        }
        target.title = title;
        target.body = body;
        target.spans = RichText.extract(bodyInput.getText());
        target.color = color;
        target.tag = tag;
        target.pinned = pinned;
        target.locked = locked;
        target.remind = remind;
        target.date = Util.today();
        target.wc = Util.wordCount(body);
        store.save();
        ReminderScheduler.schedule(this, target);
        showSaveStatus();
        return true;
    }

    private void showSaveStatus() {
        saveStatus.setText("Tersimpan " + Util.time() + "  ·  📁 File internal + SDCard");
        saveStatus.setVisibility(View.VISIBLE);
        saveStatus.setAlpha(0f);
        saveStatus.animate().alpha(1f).setDuration(150).start();
        saveStatus.removeCallbacks(hideStatus);
        saveStatus.postDelayed(hideStatus, 3500);
    }

    private final Runnable hideStatus = new Runnable() {
        @Override
        public void run() {
            if (saveStatus == null) return;
            saveStatus.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> saveStatus.setVisibility(View.GONE)).start();
        }
    };

    private void deleteNote() {
        if (editing == null && bodyInput.getText().toString().trim().isEmpty()
                && titleInput.getText().toString().trim().isEmpty()) {
            finish();
            return;
        }
        Util.confirm(this, "Hapus catatan?", "Dipindahkan ke Sampah.", "Hapus", () -> {
            if (editing == null) commit(true);
            if (editing != null) {
                ReminderScheduler.cancel(this, editing.id);
                store.moveNoteToTrash(editing);
                editing = null;
            }
            deleted = true;
            Util.snack(this, "Dipindahkan ke Sampah");
            finish();
        });
    }

    private void openReadMode() {
        commit(true);
        if (editing == null) {
            Util.snack(this, "Tulis catatan dulu!");
            return;
        }
        Intent intent = new Intent(this, ReadActivity.class);
        intent.putExtra("noteId", editing.id);
        startActivity(intent);
    }

    private void shareNote() {
        commit(true);
        String title = titleInput.getText().toString();
        Util.share(this, title, title + "\n\n" + bodyInput.getText().toString());
    }

    private void openAi() {
        commit(true);
        String title = titleInput.getText().toString();
        String body = bodyInput.getText().toString();
        if (title.isEmpty() && body.isEmpty()) {
            Util.snack(this, "Tulis catatan dulu!");
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("prefill", "Analisis dan berikan saran untuk catatan ini:\n\""
                + title + "\"\n" + (body.length() > 500 ? body.substring(0, 500) : body));
        intent.putExtra("autoSend", true);
        startActivity(intent);
    }

    // ── Formatting toolbar ─────────────────────────────────────────────

    private void buildFormatBar() {
        addFmt("H1", () -> applyHeading(1.6f), false);
        addFmt("H2", () -> applyHeading(1.35f), false);
        addFmt("H3", () -> applyHeading(1.15f), false);
        addFmt("¶", () -> applyHeading(0f), false);
        addSeparator();
        addFmt("B", () -> toggleStyle(Typeface.BOLD), true);
        addFmt("I", () -> toggleStyle(Typeface.ITALIC), false);
        addFmt("U", () -> toggleUnderline(), false);
        addFmt("S", () -> toggleStrike(), false);
        addFmt("🖊", this::toggleHighlight, false);
        addSeparator();
        addFmt("•", () -> togglePrefix("• ", false), false);
        addFmt("1.", () -> togglePrefix("", true), false);
        addFmt("❝", this::toggleQuote, false);
        addFmt("</>", this::toggleCode, false);
        addSeparator();
        addFmt("⬅", () -> applyAlignment(Layout.Alignment.ALIGN_NORMAL), false);
        addFmt("↔", () -> applyAlignment(Layout.Alignment.ALIGN_CENTER), false);
        addFmt("➡", () -> applyAlignment(Layout.Alignment.ALIGN_OPPOSITE), false);
        addSeparator();
        addFmt("🔗", this::insertLink, false);
        addFmt("⊞", () -> insertText("\n| Kolom 1 | Kolom 2 |\n|---------|---------|\n| Data    | Data    |\n"), false);
        addFmt("—", () -> insertText("\n──────────\n"), false);
        addSeparator();
        addFmt("↩", this::undo, false);
        addFmt("↪", this::redo, false);
        addFmt("🔍", this::toggleFind, false);
        addSeparator();
        addFmt("🤖 AI", this::openAi, false);
    }

    private void addFmt(String label, Runnable action, boolean bold) {
        TextView button = new TextView(this);
        button.setText(label);
        button.setTextSize(13);
        button.setTextColor(theme.textVariant);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(Util.dp(this, 36));
        button.setPadding(Util.dp(this, 8), Util.dp(this, 8), Util.dp(this, 8), Util.dp(this, 8));
        button.setClickable(true);
        button.setFocusable(true);
        if (bold) button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setOnClickListener(v -> action.run());
        fmtBar.addView(button);
    }

    private void addSeparator() {
        View sep = new View(this);
        sep.setBackgroundColor(theme.outlineVariant);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                Util.dp(this, 1), Util.dp(this, 20));
        lp.setMargins(Util.dp(this, 3), 0, Util.dp(this, 3), 0);
        fmtBar.addView(sep, lp);
    }

    private int[] selection() {
        int start = bodyInput.getSelectionStart();
        int end = bodyInput.getSelectionEnd();
        if (start < 0) start = 0;
        if (end < 0) end = bodyInput.getText().length();
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        return new int[]{start, end};
    }

    private int[] paragraph() {
        String text = bodyInput.getText().toString();
        int[] sel = selection();
        int start = text.lastIndexOf('\n', Math.max(0, sel[0] - 1)) + 1;
        int end = text.indexOf('\n', sel[1]);
        if (end < 0) end = text.length();
        return new int[]{start, end};
    }

    private boolean hasSelection() {
        int[] sel = selection();
        if (sel[0] == sel[1]) {
            Util.snack(this, "Pilih teks dulu untuk format ini");
            return false;
        }
        return true;
    }

    private void toggleStyle(int style) {
        if (!hasSelection()) return;
        Editable e = bodyInput.getText();
        int[] sel = selection();
        pushUndo(snapshot());
        boolean removed = false;
        for (StyleSpan span : e.getSpans(sel[0], sel[1], StyleSpan.class)) {
            if ((span.getStyle() & style) != 0
                    && e.getSpanStart(span) <= sel[0] && e.getSpanEnd(span) >= sel[1]) {
                e.removeSpan(span);
                removed = true;
            }
        }
        if (!removed) {
            e.setSpan(new StyleSpan(style), sel[0], sel[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        scheduleAutoSave();
    }

    private void toggleUnderline() {
        if (!hasSelection()) return;
        Editable e = bodyInput.getText();
        int[] sel = selection();
        pushUndo(snapshot());
        UnderlineSpan[] spans = e.getSpans(sel[0], sel[1], UnderlineSpan.class);
        boolean removed = false;
        for (UnderlineSpan span : spans) {
            e.removeSpan(span);
            removed = true;
        }
        if (!removed) {
            e.setSpan(new UnderlineSpan(), sel[0], sel[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleStrike() {
        if (!hasSelection()) return;
        Editable e = bodyInput.getText();
        int[] sel = selection();
        pushUndo(snapshot());
        StrikethroughSpan[] spans = e.getSpans(sel[0], sel[1], StrikethroughSpan.class);
        boolean removed = false;
        for (StrikethroughSpan span : spans) {
            e.removeSpan(span);
            removed = true;
        }
        if (!removed) {
            e.setSpan(new StrikethroughSpan(), sel[0], sel[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleHighlight() {
        if (!hasSelection()) return;
        Editable e = bodyInput.getText();
        int[] sel = selection();
        pushUndo(snapshot());
        BackgroundColorSpan[] spans = e.getSpans(sel[0], sel[1], BackgroundColorSpan.class);
        if (spans.length > 0) {
            for (BackgroundColorSpan span : spans) e.removeSpan(span);
        } else {
            e.setSpan(new BackgroundColorSpan(0xFFFFEB3B), sel[0], sel[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applyHeading(float size) {
        Editable e = bodyInput.getText();
        int[] para = paragraph();
        if (para[0] == para[1]) return;
        pushUndo(snapshot());
        for (RelativeSizeSpan span : e.getSpans(para[0], para[1], RelativeSizeSpan.class)) {
            e.removeSpan(span);
        }
        for (StyleSpan span : e.getSpans(para[0], para[1], StyleSpan.class)) {
            if ((span.getStyle() & Typeface.BOLD) != 0) e.removeSpan(span);
        }
        if (size > 0) {
            e.setSpan(new RelativeSizeSpan(size), para[0], para[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            e.setSpan(new StyleSpan(Typeface.BOLD), para[0], para[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        scheduleAutoSave();
    }

    private void applyAlignment(Layout.Alignment alignment) {
        Editable e = bodyInput.getText();
        int[] para = paragraph();
        if (para[0] == para[1]) return;
        pushUndo(snapshot());
        for (AlignmentSpan span : e.getSpans(para[0], para[1], AlignmentSpan.class)) {
            e.removeSpan(span);
        }
        e.setSpan(new AlignmentSpan.Standard(alignment), para[0], para[1],
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        scheduleAutoSave();
    }

    private void togglePrefix(String prefix, boolean numbered) {
        Editable e = bodyInput.getText();
        int[] para = paragraph();
        if (para[0] == para[1]) return;
        pushUndo(snapshot());
        String block = e.subSequence(para[0], para[1]).toString();
        String[] lines = block.split("\n", -1);
        boolean allHave = true;
        for (String line : lines) {
            if (numbered) {
                if (!line.matches("^\\d+\\.\\s.*")) {
                    allHave = false;
                    break;
                }
            } else if (!line.startsWith(prefix)) {
                allHave = false;
                break;
            }
        }
        StringBuilder out = new StringBuilder();
        int number = 1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i > 0) out.append('\n');
            if (allHave) {
                if (numbered) out.append(line.replaceFirst("^\\d+\\.\\s", ""));
                else out.append(line.substring(prefix.length()));
            } else {
                String clean = numbered ? line.replaceFirst("^\\d+\\.\\s", "") : line;
                if (!numbered && clean.startsWith(prefix)) clean = clean.substring(prefix.length());
                if (numbered) out.append(number++).append(". ").append(clean);
                else out.append(prefix).append(clean);
            }
        }
        e.replace(para[0], para[1], out.toString());
        bodyInput.setSelection(Math.min(para[0] + out.length(), e.length()));
        scheduleAutoSave();
    }

    private void toggleQuote() {
        togglePrefix("▎ ", false);
    }

    private void toggleCode() {
        if (!hasSelection()) return;
        Editable e = bodyInput.getText();
        int[] sel = selection();
        pushUndo(snapshot());
        TypefaceSpan[] spans = e.getSpans(sel[0], sel[1], TypefaceSpan.class);
        boolean removed = false;
        for (TypefaceSpan span : spans) {
            if (span.getFamily() != null && span.getFamily().contains("mono")) {
                e.removeSpan(span);
                removed = true;
            }
        }
        if (!removed) {
            e.setSpan(new TypefaceSpan("monospace"), sel[0], sel[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            e.setSpan(new BackgroundColorSpan(0x22808080), sel[0], sel[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        scheduleAutoSave();
    }

    private void insertLink() {
        Util.prompt(this, "Sisipkan link", "https://", "https://", url -> {
            if (url == null || url.isEmpty()) return;
            int[] sel = selection();
            if (sel[0] == sel[1]) {
                insertText(url);
                int end = bodyInput.getSelectionEnd();
                bodyInput.getText().setSpan(new URLSpan(url), end - url.length(), end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                pushUndo(snapshot());
                bodyInput.getText().setSpan(new URLSpan(url), sel[0], sel[1],
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                scheduleAutoSave();
            }
        });
    }

    private void insertText(String value) {
        pushUndo(snapshot());
        int start = bodyInput.getSelectionStart();
        int end = bodyInput.getSelectionEnd();
        if (start < 0) start = bodyInput.getText().length();
        if (end < 0) end = start;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        bodyInput.getText().replace(start, end, value);
        bodyInput.setSelection(start + value.length());
        scheduleAutoSave();
    }

    private void toggleFind() {
        boolean show = findBar.getVisibility() != View.VISIBLE;
        findBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            findInput.requestFocus();
            updateFindCount();
        } else {
            Util.hideKeyboard(this);
        }
    }

    private void updateFindCount() {
        String needle = findInput.getText().toString();
        if (needle.isEmpty()) {
            findCount.setText("0");
            return;
        }
        String hay = bodyInput.getText().toString().toLowerCase(Locale.ROOT);
        String low = needle.toLowerCase(Locale.ROOT);
        int count = 0;
        int index = 0;
        while ((index = hay.indexOf(low, index)) != -1) {
            count++;
            index += low.length();
        }
        findCount.setText(String.valueOf(count));
    }

    private void replaceAll() {
        String find = findInput.getText().toString();
        if (find.isEmpty()) return;
        String replace = replaceInput.getText().toString();
        pushUndo(snapshot());
        Editable e = bodyInput.getText();
        String text = e.toString();
        int index = 0;
        int count = 0;
        while ((index = text.indexOf(find, index)) != -1) {
            e.replace(index, index + find.length(), replace);
            text = e.toString();
            index += replace.length();
            count++;
            if (count > 2000) break;
        }
        updateFindCount();
        scheduleAutoSave();
        Util.snack(this, "Diganti " + count + " kali");
    }

    // ── Undo / redo ────────────────────────────────────────────────────

    private Snapshot snapshot() {
        Snapshot s = new Snapshot();
        s.text = bodyInput.getText().toString();
        s.spans = RichText.extract(bodyInput.getText());
        s.start = bodyInput.getSelectionStart();
        s.end = bodyInput.getSelectionEnd();
        return s;
    }

    private void pushUndo(Snapshot s) {
        undoStack.push(s);
        while (undoStack.size() > 40) undoStack.removeLast();
        redoStack.clear();
    }

    private void restore(Snapshot s) {
        applyingHistory = true;
        bodyInput.setText(s.text);
        RichText.apply(bodyInput.getText(), s.spans);
        int len = bodyInput.getText().length();
        bodyInput.setSelection(Math.max(0, Math.min(s.start, len)),
                Math.max(0, Math.min(s.end, len)));
        applyingHistory = false;
        updateWordCount();
    }

    private void undo() {
        if (undoStack.isEmpty()) {
            Util.snack(this, "Tidak ada yang bisa dibatalkan");
            return;
        }
        redoStack.push(snapshot());
        restore(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) {
            Util.snack(this, "Tidak ada yang bisa diulang");
            return;
        }
        undoStack.push(snapshot());
        restore(redoStack.pop());
    }

    @Override
    public void onBackPressed() {
        commit(false);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoSaveTask);
        if (!isFinishing()) commit(false);
        else commit(false);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(autoSaveTask);
        super.onDestroy();
    }
}
