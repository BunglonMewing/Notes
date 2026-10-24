package com.circledev.notes;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends Activity {

    private Store store;
    private Theme theme;

    private LinearLayout messages;
    private ScrollView chatScroll;
    private EditText chatInput;
    private TextView chatStatus;
    private TextView btnSend;
    private LinearLayout quickBar;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private boolean typing;

    private final String[] quickPrompts = {
            "Rangkum catatan", "Ide tulisan", "Cek grammar", "Buat to-do",
            "Tips produktivitas", "Jelaskan kode", "Terjemahkan ke English"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        theme = Themer.apply(this);
        store = Store.get(this);

        messages = findViewById(R.id.messages);
        chatScroll = findViewById(R.id.chatScroll);
        chatInput = findViewById(R.id.chatInput);
        chatStatus = findViewById(R.id.chatStatus);
        btnSend = findViewById(R.id.btnSend);
        quickBar = findViewById(R.id.quickBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearChat());
        btnSend.setOnClickListener(v -> send());

        chatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setAlpha(s.toString().trim().isEmpty() ? 0.5f : 1f);
            }
        });

        buildQuickPrompts();

        if (store.chat.isEmpty()) {
            store.chat.add(new ChatMsg("ai",
                    "Halo! Gue NoteFlow AI 🤖 Siap bantu nulis, rangkum catatan, atau diskusi teknologi. Mau ngobrol apa?",
                    Util.time()));
            store.save();
        }
        renderMessages();

        String prefill = getIntent().getStringExtra("prefill");
        if (prefill != null && !prefill.isEmpty()) {
            chatInput.setText(prefill);
            if (getIntent().getBooleanExtra("autoSend", false)) {
                chatInput.postDelayed(this::send, 250);
            }
        }
    }

    private void buildQuickPrompts() {
        quickBar.removeAllViews();
        for (String prompt : quickPrompts) {
            TextView chip = Ui.chip(this, prompt);
            chip.setOnClickListener(v -> {
                chatInput.setText(prompt);
                send();
            });
            quickBar.addView(chip);
        }
    }

    private void renderMessages() {
        messages.removeAllViews();
        for (ChatMsg msg : store.chat) {
            addMessageView(msg);
        }
        if (typing) addTypingView();
        scrollBottom();
    }

    private void addMessageView(ChatMsg msg) {
        boolean ai = "ai".equals(msg.role);
        View row = getLayoutInflater().inflate(
                ai ? R.layout.item_msg_ai : R.layout.item_msg_user, messages, false);
        Themer.walk(row, theme, this);

        TextView text = row.findViewById(R.id.msgText);
        TextView time = row.findViewById(R.id.msgTime);
        time.setText(msg.time == null ? "" : msg.time);

        if (ai) {
            text.setText(RichText.markdown(msg.text));
            TextView save = row.findViewById(R.id.btnSaveNote);
            save.setOnClickListener(v -> saveAiNote(msg.text));
        } else {
            text.setText(msg.text);
        }
        messages.addView(row);
    }

    private void addTypingView() {
        View row = getLayoutInflater().inflate(R.layout.item_msg_ai, messages, false);
        Themer.walk(row, theme, this);
        TextView text = row.findViewById(R.id.msgText);
        text.setText("Mengetik…");
        row.findViewById(R.id.btnSaveNote).setVisibility(View.GONE);
        row.findViewById(R.id.msgTime).setVisibility(View.GONE);
        row.setTag("typing");
        messages.addView(row);
    }

    private void scrollBottom() {
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void send() {
        String message = chatInput.getText().toString().trim();
        if (message.isEmpty() || typing) return;
        chatInput.setText("");

        store.chat.add(new ChatMsg("user", message, Util.time()));
        trimChat();
        store.save();
        typing = true;
        chatStatus.setText("Mengetik...");
        btnSend.setEnabled(false);
        renderMessages();

        final String full = buildPrompt(message);
        executor.execute(() -> {
            String reply = AiClient.ask(full);
            main.post(() -> {
                typing = false;
                btnSend.setEnabled(true);
                chatStatus.setText("Siap membantu ✓");
                store.chat.add(new ChatMsg("ai", reply, Util.time()));
                trimChat();
                store.save();
                renderMessages();
            });
        });
    }

    private String buildPrompt(String message) {
        StringBuilder context = new StringBuilder();
        int count = Math.min(3, store.notes.size());
        for (int i = 0; i < count; i++) {
            Note n = store.notes.get(i);
            String plain = n.body == null ? "" : n.body.replace('\n', ' ');
            if (plain.length() > 80) plain = plain.substring(0, 80);
            if (i > 0) context.append("; ");
            context.append('"').append(n.title).append("\": ").append(plain);
        }
        if (context.length() == 0) return message;
        return "Konteks catatan user:\n" + context + "\n\nPertanyaan: " + message;
    }

    private void trimChat() {
        while (store.chat.size() > 60) store.chat.remove(0);
    }

    private void clearChat() {
        store.chat.clear();
        store.chat.add(new ChatMsg("ai", "Chat bersih! Mau mulai topik baru? 😊", Util.time()));
        store.save();
        renderMessages();
    }

    private void saveAiNote(String text) {
        Note note = new Note();
        note.id = System.currentTimeMillis() ^ (long) (Math.random() * 0xFFFF);
        note.title = "AI – " + Util.today();
        note.body = text;
        note.color = "cb";
        note.tag = "ide";
        note.date = Util.today();
        note.wc = Util.wordCount(text);
        store.notes.add(0, note);
        store.save();
        Util.snack(this, "Disimpan sebagai catatan ✓");
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
