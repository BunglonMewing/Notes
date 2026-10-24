package com.circledev.notes;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class Sheets {

    public interface OnTag {
        void onTag(String tag);
    }

    public interface OnTemplate {
        void onTemplate(int index);
    }

    public interface OnId {
        void onId(long id);
    }

    public interface OnChanged {
        void onChanged();
    }

    public static class Action {
        public final String icon;
        public final String title;
        public final String subtitle;
        public final Runnable run;

        public Action(String icon, String title, String subtitle, Runnable run) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.run = run;
        }
    }

    private Sheets() {
    }

    private static Dialog base(Activity a, String title, View content) {
        Theme t = ThemeManager.current(a);
        Dialog d = new Dialog(a, R.style.DialogTheme);
        d.setContentView(R.layout.dialog_sheet);
        TextView titleView = d.findViewById(R.id.sheetTitle);
        titleView.setText(title);
        TextView close = d.findViewById(R.id.sheetClose);
        close.setOnClickListener(v -> d.dismiss());
        LinearLayout holder = d.findViewById(R.id.sheetContent);
        holder.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View sheetRoot = d.findViewById(R.id.sheetRoot);
        sheetRoot.setBackground(Util.topRound(t.surface1, 28, a));
        Themer.walk(sheetRoot, t, a);

        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setGravity(Gravity.BOTTOM);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            lp.dimAmount = 0.45f;
            w.setAttributes(lp);
        }

        ScrollView scroll = d.findViewById(R.id.sheetScroll);
        int maxHeight = (int) (a.getResources().getDisplayMetrics().heightPixels * 0.72f);
        scroll.post(() -> {
            int width = scroll.getWidth();
            if (width <= 0) return;
            content.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int wanted = content.getMeasuredHeight() + Util.dp(a, 8);
            ViewGroup.LayoutParams lp = scroll.getLayoutParams();
            lp.height = Math.min(wanted, maxHeight);
            scroll.setLayoutParams(lp);
        });

        d.show();
        return d;
    }

    public static void actions(Activity a, String title, List<Action> actions) {
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);
        final Dialog[] holder = new Dialog[1];
        for (Action action : actions) {
            content.addView(actionRow(a, action.icon, action.title, action.subtitle, v -> {
                if (holder[0] != null) holder[0].dismiss();
                action.run.run();
            }));
        }
        holder[0] = base(a, title, content);
    }

    private static View actionRow(Activity a, String icon, String title, String subtitle,
                                  View.OnClickListener listener) {
        Theme t = ThemeManager.current(a);
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Util.dp(a, 6), Util.dp(a, 9), Util.dp(a, 6), Util.dp(a, 9));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(listener);

        TextView ic = new TextView(a);
        ic.setText(icon);
        ic.setTextSize(17);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(Util.round(t.surface2, 14, a));
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(Util.dp(a, 38), Util.dp(a, 38));
        row.addView(ic, icLp);

        LinearLayout info = new LinearLayout(a);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoLp.leftMargin = Util.dp(a, 12);

        TextView tvTitle = new TextView(a);
        tvTitle.setText(title);
        tvTitle.setTextColor(t.text);
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        info.addView(tvTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(a);
            tvSub.setText(subtitle);
            tvSub.setTextColor(t.textVariant);
            tvSub.setTextSize(11);
            tvSub.setLineSpacing(Util.dp(a, 1), 1f);
            LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subLp.topMargin = Util.dp(a, 1);
            info.addView(tvSub, subLp);
        }
        row.addView(info, infoLp);
        return row;
    }

    public static void themes(Activity a, OnChanged changed) {
        Theme current = ThemeManager.current(a);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);

        List<Theme> light = new ArrayList<>();
        List<Theme> dark = new ArrayList<>();
        for (Theme t : Theme.ALL) {
            (t.dark ? dark : light).add(t);
        }

        final Dialog[] holder = new Dialog[1];
        addThemeSection(a, content, "☀️ Terang", light, current, holder, changed);
        addThemeSection(a, content, "🌙 Gelap", dark, current, holder, changed);
        holder[0] = base(a, "Pilih Tema", content);
    }

    private static void addThemeSection(Activity a, LinearLayout parent, String label,
                                        List<Theme> themes, Theme current,
                                        Dialog[] holder, OnChanged changed) {
        Theme t = ThemeManager.current(a);
        TextView header = new TextView(a);
        header.setText(label);
        header.setTextColor(t.textVariant);
        header.setTextSize(11);
        header.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        header.setPadding(Util.dp(a, 4), Util.dp(a, 12), Util.dp(a, 4), Util.dp(a, 4));
        parent.addView(header);

        for (int i = 0; i < themes.size(); i += 3) {
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 3; j++) {
                int index = i + j;
                if (index < themes.size()) {
                    row.addView(themeCard(a, themes.get(index), current, holder, changed));
                } else {
                    View spacer = new View(a);
                    row.addView(spacer, new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                }
            }
            parent.addView(row);
        }
    }

    private static View themeCard(Activity a, Theme theme, Theme current, Dialog[] holder,
                                  OnChanged changed) {
        Theme t = ThemeManager.current(a);
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Util.dp(a, 10), Util.dp(a, 9), Util.dp(a, 10), Util.dp(a, 9));
        android.graphics.drawable.GradientDrawable bg = Util.round(theme.bg, 14, a);
        bg.setStroke(Util.dp(a, theme.id.equals(current.id) ? 2f : 1f),
                theme.id.equals(current.id) ? t.primary : t.outlineVariant);
        card.setBackground(bg);
        card.setClickable(true);
        card.setFocusable(true);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cardLp.setMargins(Util.dp(a, 4), Util.dp(a, 4), Util.dp(a, 4), Util.dp(a, 4));
        card.setLayoutParams(cardLp);

        LinearLayout swatches = new LinearLayout(a);
        swatches.setOrientation(LinearLayout.HORIZONTAL);
        int[] colors = {theme.primary, theme.primaryContainer, theme.bg};
        for (int color : colors) {
            View sw = new View(a);
            android.graphics.drawable.GradientDrawable shape = Util.circle(color);
            shape.setStroke(Util.dp(a, 1), theme.outlineVariant);
            sw.setBackground(shape);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    Util.dp(a, 13), Util.dp(a, 13));
            lp.rightMargin = Util.dp(a, 3);
            swatches.addView(sw, lp);
        }
        card.addView(swatches);

        TextView name = new TextView(a);
        name.setText(theme.name);
        name.setTextColor(theme.primary);
        name.setTextSize(11);
        name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLp.topMargin = Util.dp(a, 6);
        card.addView(name, nameLp);

        TextView sub = new TextView(a);
        sub.setText(theme.sub);
        sub.setTextColor(theme.textVariant);
        sub.setTextSize(9);
        card.addView(sub);

        card.setOnClickListener(v -> {
            ThemeManager.set(a, theme.id);
            if (holder[0] != null) holder[0].dismiss();
            Util.snack(a, "Tema " + theme.name + " ✓");
            if (changed != null) changed.onChanged();
        });
        return card;
    }

    public static void templates(Activity a, OnTemplate callback) {
        Templates.Template[] items = Templates.all();
        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            Templates.Template tpl = items[i];
            final int index = i;
            actions.add(new Action(tpl.icon, tpl.name, tpl.desc, () -> callback.onTemplate(index)));
        }
        actions(a, "Template Catatan", actions);
    }

    public static void stats(Activity a, Store store) {
        Theme t = ThemeManager.current(a);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);

        int totalWords = 0;
        for (Note n : store.notes) totalWords += n.wc;
        int reminders = 0;
        for (Note n : store.notes) if (n.remind > 0) reminders++;
        int chats = 0;
        for (ChatMsg m : store.chat) if ("user".equals(m.role)) chats++;
        String lastSave = store.lastSave > 0
                ? new java.text.SimpleDateFormat("HH:mm", Util.ID).format(new java.util.Date(store.lastSave))
                : "—";

        Object[][] cards = {
                {store.notes.size(), "Catatan", "📝"},
                {store.todos.size(), "To-Do", "☑️"},
                {countPinned(store), "Disematkan", "📌"},
                {countLocked(store), "Dikunci", "🔒"},
                {store.totalTasks(), "Total Tugas", "📋"},
                {store.doneTasks(), "Selesai", "✅"},
                {store.trash.size(), "Sampah", "🗑️"},
                {totalWords, "Total Kata", "✍️"},
                {reminders, "Pengingat", "🔔"},
                {chats, "Chat AI", "🤖"},
                {lastSave, "Last Save", "💾"},
        };

        for (int i = 0; i < cards.length; i += 2) {
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 2; j++) {
                int index = i + j;
                if (index < cards.length) {
                    row.addView(statCard(a, t, String.valueOf(cards[index][0]),
                            String.valueOf(cards[index][1]), String.valueOf(cards[index][2])));
                } else {
                    row.addView(new View(a), new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                }
            }
            content.addView(row);
        }
        base(a, "Statistik", content);
    }

    private static int countPinned(Store store) {
        int c = 0;
        for (Note n : store.notes) if (n.pinned) c++;
        return c;
    }

    private static int countLocked(Store store) {
        int c = 0;
        for (Note n : store.notes) if (n.locked) c++;
        return c;
    }

    private static View statCard(Activity a, Theme t, String number, String label, String icon) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Util.dp(a, 13), Util.dp(a, 11), Util.dp(a, 13), Util.dp(a, 11));
        card.setBackground(Util.round(t.surface2, 16, a));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(Util.dp(a, 4), Util.dp(a, 4), Util.dp(a, 4), Util.dp(a, 4));
        card.setLayoutParams(lp);

        TextView ic = new TextView(a);
        ic.setText(icon);
        ic.setTextSize(17);
        card.addView(ic);

        TextView num = new TextView(a);
        num.setText(number);
        num.setTextColor(t.text);
        num.setTextSize(number.length() > 4 ? 15 : 25);
        num.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        card.addView(num);

        TextView lbl = new TextView(a);
        lbl.setText(label);
        lbl.setTextColor(t.textVariant);
        lbl.setTextSize(10);
        card.addView(lbl);
        return card;
    }

    public static void categories(Activity a, Store store, OnTag callback) {
        Theme t = ThemeManager.current(a);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);

        String[][] cats = {
                {"💻", "Teknologi", "teknologi"},
                {"💼", "Kerja", "kerja"},
                {"💡", "Ide", "ide"},
                {"👤", "Personal", "personal"},
                {"📌", "Disematkan", "pinned"},
                {"🔒", "Dikunci", "locked"},
        };
        final Dialog[] holder = new Dialog[1];
        for (String[] cat : cats) {
            int count = countCategory(store, cat[2]);
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Util.dp(a, 4), Util.dp(a, 10), Util.dp(a, 4), Util.dp(a, 10));
            row.setClickable(true);
            row.setFocusable(true);

            TextView ic = new TextView(a);
            ic.setText(cat[0]);
            ic.setTextSize(17);
            ic.setGravity(Gravity.CENTER);
            ic.setBackground(Util.round(t.surface2, 12, a));
            row.addView(ic, new LinearLayout.LayoutParams(Util.dp(a, 38), Util.dp(a, 38)));

            TextView name = new TextView(a);
            name.setText(cat[1]);
            name.setTextColor(t.text);
            name.setTextSize(14);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            nameLp.leftMargin = Util.dp(a, 12);
            row.addView(name, nameLp);

            TextView countView = new TextView(a);
            countView.setText(String.valueOf(count));
            countView.setTextColor(t.textVariant);
            countView.setTextSize(11);
            countView.setPadding(Util.dp(a, 8), Util.dp(a, 2), Util.dp(a, 8), Util.dp(a, 2));
            countView.setBackground(Util.round(t.surface2, 100, a));
            row.addView(countView);

            row.setOnClickListener(v -> {
                if (holder[0] != null) holder[0].dismiss();
                callback.onTag(cat[2]);
            });
            content.addView(row);
        }
        holder[0] = base(a, "Kategori", content);
    }

    private static int countCategory(Store store, String tag) {
        int count = 0;
        for (Note n : store.notes) {
            if ("pinned".equals(tag)) {
                if (n.pinned) count++;
            } else if ("locked".equals(tag)) {
                if (n.locked) count++;
            } else if (tag.equals(n.tag)) {
                count++;
            }
        }
        return count;
    }

    public static void widgetStyles(Activity a, Store store, OnChanged changed) {
        Theme t = ThemeManager.current(a);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);
        String[] labels = {"Mini Note", "To-Do", "Banner Lebar", "Daftar"};
        String[] descs = {
                "Judul dan isi catatan terbaru",
                "Daftar to-do dengan progres",
                "Catatan terbaru versi lebar",
                "Tiga catatan teratas",
        };
        Note first = store.notes.isEmpty() ? null : store.notes.get(0);
        Todo firstTodo = store.todos.isEmpty() ? null : store.todos.get(0);
        String[] previews = {
                first == null ? "Belum ada catatan" : first.title + " — " + preview(first),
                firstTodo == null ? "Belum ada to-do" : firstTodo.title + " · "
                        + firstTodo.doneCount() + "/" + firstTodo.items.size(),
                first == null ? "Belum ada catatan" : first.title + " — " + preview(first),
                store.notes.size() + " catatan tersimpan",
        };
        final Dialog[] holder = new Dialog[1];
        for (int i = 0; i < labels.length; i++) {
            final int style = i;
            LinearLayout card = new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(Util.dp(a, 15), Util.dp(a, 12), Util.dp(a, 15), Util.dp(a, 12));
            android.graphics.drawable.GradientDrawable bg = Util.round(t.surface2, 16, a);
            if (store.widgetStyle == i) bg.setStroke(Util.dp(a, 2), t.primary);
            card.setBackground(bg);
            card.setClickable(true);
            card.setFocusable(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, Util.dp(a, 4), 0, Util.dp(a, 4));
            card.setLayoutParams(lp);

            TextView label = new TextView(a);
            label.setText(labels[i]);
            label.setTextColor(t.text);
            label.setTextSize(14);
            label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            card.addView(label);

            TextView preview = new TextView(a);
            preview.setText(previews[i]);
            preview.setTextColor(t.textVariant);
            preview.setTextSize(11);
            preview.setMaxLines(3);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pLp.topMargin = Util.dp(a, 3);
            card.addView(preview, pLp);

            TextView desc = new TextView(a);
            desc.setText(descs[i] + (store.widgetStyle == i ? "  •  Dipakai" : ""));
            desc.setTextColor(t.primary);
            desc.setTextSize(10);
            card.addView(desc);

            card.setOnClickListener(v -> {
                store.widgetStyle = style;
                store.save();
                if (holder[0] != null) holder[0].dismiss();
                Util.snack(a, "Gaya widget dipakai ✓");
                if (changed != null) changed.onChanged();
            });
            content.addView(card);
        }
        holder[0] = base(a, "Widget", content);
    }

    private static String preview(Note n) {
        String body = n.body == null ? "" : n.body.replace('\n', ' ').trim();
        return body.length() > 70 ? body.substring(0, 70) : body;
    }

    public static void reminders(Activity a, Store store, OnId onOpen) {
        Theme t = ThemeManager.current(a);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);

        boolean any = false;
        final Dialog[] holder = new Dialog[1];
        for (Note n : store.notes) {
            if (n.remind <= 0) continue;
            any = true;
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Util.dp(a, 4), Util.dp(a, 10), Util.dp(a, 4), Util.dp(a, 10));
            row.setClickable(true);
            row.setFocusable(true);

            TextView ic = new TextView(a);
            ic.setText("🔔");
            ic.setTextSize(16);
            ic.setGravity(Gravity.CENTER);
            ic.setBackground(Util.round(t.surface2, 100, a));
            row.addView(ic, new LinearLayout.LayoutParams(Util.dp(a, 38), Util.dp(a, 38)));

            LinearLayout info = new LinearLayout(a);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            infoLp.leftMargin = Util.dp(a, 12);

            TextView title = new TextView(a);
            title.setText(n.title == null || n.title.isEmpty() ? "Catatan" : n.title);
            title.setTextColor(t.text);
            title.setTextSize(14);
            title.setMaxLines(1);
            title.setEllipsize(android.text.TextUtils.TruncateAt.END);
            info.addView(title);

            TextView when = new TextView(a);
            when.setText(Util.fmtRemind(n.remind));
            when.setTextColor(t.primary);
            when.setTextSize(11);
            info.addView(when);
            row.addView(info, infoLp);

            TextView open = new TextView(a);
            open.setText("Buka");
            open.setTextColor(t.primary);
            open.setTextSize(12);
            open.setPadding(Util.dp(a, 8), Util.dp(a, 4), Util.dp(a, 8), Util.dp(a, 4));
            row.addView(open);

            final long id = n.id;
            row.setOnClickListener(v -> {
                if (holder[0] != null) holder[0].dismiss();
                onOpen.onId(id);
            });
            content.addView(row);
        }

        if (!any) {
            TextView empty = new TextView(a);
            empty.setText("Belum ada pengingat 🔔");
            empty.setTextColor(t.textVariant);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(Util.dp(a, 20), Util.dp(a, 28), Util.dp(a, 20), Util.dp(a, 28));
            content.addView(empty);
        }
        holder[0] = base(a, "Pengingat", content);
    }
}
