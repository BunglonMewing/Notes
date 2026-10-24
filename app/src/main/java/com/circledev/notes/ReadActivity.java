package com.circledev.notes;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.widget.TextView;

public class ReadActivity extends Activity {

    private Store store;
    private Theme theme;
    private Note note;

    private TextView readTitle;
    private TextView readMeta;
    private TextView readContent;
    private TextView fontLabel;

    private int fontSize = 17;
    private boolean darkRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        theme = Themer.apply(this);
        store = Store.get(this);

        long noteId = getIntent().getLongExtra("noteId", 0);
        note = noteId > 0 ? store.noteById(noteId) : null;
        if (note == null) {
            finish();
            return;
        }

        readTitle = findViewById(R.id.readTitle);
        readMeta = findViewById(R.id.readMeta);
        readContent = findViewById(R.id.readContent);
        fontLabel = findViewById(R.id.fontLabel);

        readTitle.setText(note.title == null || note.title.isEmpty() ? "Tanpa Judul" : note.title);
        readMeta.setText((note.tag == null ? "" : note.tag) + "  ·  " + note.wc
                + " kata  ·  " + note.date);

        Spannable content = RichText.render(note.body, note.spans);
        readContent.setText(content);
        readContent.setTextSize(fontSize);
        fontLabel.setText(fontSize + "px");

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnShare).setOnClickListener(v ->
                Util.share(this, note.title, note.title + "\n\n" + note.body));
        findViewById(R.id.btnFontDown).setOnClickListener(v -> changeFont(-1));
        findViewById(R.id.btnFontUp).setOnClickListener(v -> changeFont(1));
        findViewById(R.id.btnTheme).setOnClickListener(v -> toggleTheme());
    }

    private void changeFont(int delta) {
        fontSize = Math.max(12, Math.min(24, fontSize + delta));
        readContent.setTextSize(fontSize);
        fontLabel.setText(fontSize + "px");
    }

    private void toggleTheme() {
        darkRead = !darkRead;
        android.view.View root = findViewById(R.id.root);
        if (darkRead) {
            root.setBackgroundColor(0xFF1A1A1A);
            readTitle.setTextColor(Color.WHITE);
            readContent.setTextColor(0xFFCCCCCC);
            readMeta.setTextColor(0xFF9E9E9E);
        } else {
            root.setBackgroundColor(theme.bg);
            readTitle.setTextColor(theme.text);
            readContent.setTextColor(theme.text);
            readMeta.setTextColor(theme.textVariant);
        }
    }
}
