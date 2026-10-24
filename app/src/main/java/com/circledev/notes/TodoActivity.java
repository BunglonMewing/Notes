package com.circledev.notes;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TodoActivity extends Activity {

    private Store store;
    private Theme theme;

    private EditText titleInput;
    private EditText newItemInput;
    private LinearLayout itemsContainer;
    private LinearLayout colorDots;

    private Todo editing;
    private long todoId;
    private String color = "";
    private final List<Todo.TodoItem> items = new ArrayList<>();

    private final String[] colors = {"", "cy", "cp", "cb", "cg", "cv"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        theme = Themer.apply(this);
        store = Store.get(this);

        todoId = getIntent().getLongExtra("todoId", 0);
        if (todoId > 0) {
            editing = store.todoById(todoId);
            if (editing == null) {
                finish();
                return;
            }
            for (Todo.TodoItem item : editing.items) {
                Todo.TodoItem copy = new Todo.TodoItem();
                copy.id = item.id;
                copy.text = item.text;
                copy.done = item.done;
                items.add(copy);
            }
            color = editing.color == null ? "" : editing.color;
        }

        titleInput = findViewById(R.id.titleInput);
        newItemInput = findViewById(R.id.newItemInput);
        itemsContainer = findViewById(R.id.itemsContainer);
        colorDots = findViewById(R.id.colorDots);

        if (editing != null) titleInput.setText(editing.title);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnDelete).setOnClickListener(v -> delete());
        findViewById(R.id.btnAddItem).setOnClickListener(v -> addItem());
        newItemInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addItem();
                return true;
            }
            return false;
        });

        renderColorDots();
        renderItems();

        if (editing == null) {
            titleInput.requestFocus();
        }
    }

    private void renderColorDots() {
        colorDots.removeAllViews();
        for (String key : colors) {
            int fill = key.isEmpty() ? theme.bg : Theme.swatch(key, theme.dark);
            View dot = Ui.colorDot(this, fill, key.equals(color));
            dot.setOnClickListener(v -> {
                color = key;
                renderColorDots();
            });
            colorDots.addView(dot);
        }
    }

    private void renderItems() {
        itemsContainer.removeAllViews();
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Belum ada item.\nTambahkan di kolom bawah.");
            empty.setTextColor(theme.textVariant);
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, Util.dp(this, 30), 0, Util.dp(this, 30));
            itemsContainer.addView(empty);
            return;
        }
        for (Todo.TodoItem item : items) {
            View row = getLayoutInflater().inflate(R.layout.item_todo_edit_row,
                    itemsContainer, false);
            TextView check = row.findViewById(R.id.rowCheck);
            EditText text = row.findViewById(R.id.rowText);
            TextView delete = row.findViewById(R.id.rowDelete);

            Themer.walk(row, theme, this);
            text.setText(item.text);
            bindRowState(check, text, item);

            check.setOnClickListener(v -> {
                item.done = !item.done;
                bindRowState(check, text, item);
            });
            delete.setOnClickListener(v -> {
                items.remove(item);
                renderItems();
            });
            text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    item.text = s.toString();
                }
            });
            itemsContainer.addView(row);
        }
    }

    private void bindRowState(TextView check, TextView text, Todo.TodoItem item) {
        check.setText(item.done ? "✓" : "○");
        check.setTextColor(item.done ? theme.onPrimary : theme.primary);
        check.setBackground(item.done
                ? Util.round(theme.primary, 100, this)
                : Util.round(0x00000000, 100, this));
        if (item.done) {
            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            text.setTextColor(theme.textVariant);
        } else {
            text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            text.setTextColor(theme.text);
        }
    }

    private void addItem() {
        String value = newItemInput.getText().toString().trim();
        if (value.isEmpty()) return;
        items.add(new Todo.TodoItem(value));
        newItemInput.setText("");
        renderItems();
        newItemInput.requestFocus();
    }

    private void save() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty() && items.isEmpty()) {
            Util.snack(this, "Isi nama daftar atau tambah item");
            return;
        }
        if (title.isEmpty()) title = "To-Do";
        Todo target = editing;
        if (target == null) {
            target = new Todo();
            target.id = System.currentTimeMillis() ^ (long) (Math.random() * 0xFFFF);
            store.todos.add(0, target);
            editing = target;
        }
        target.title = title;
        target.color = color;
        target.items = new ArrayList<>(items);
        target.date = Util.today();
        store.save();
        Util.snack(this, "To-Do disimpan ✓");
        finish();
    }

    private void delete() {
        if (editing == null) {
            finish();
            return;
        }
        Util.confirm(this, "Hapus to-do?", "Dipindahkan ke Sampah.", "Hapus", () -> {
            store.moveTodoToTrash(editing);
            editing = null;
            Util.snack(this, "Dipindahkan ke Sampah");
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (editing == null && titleInput.getText().toString().trim().isEmpty()
                && items.isEmpty()) {
            finish();
            return;
        }
        save();
    }
}
