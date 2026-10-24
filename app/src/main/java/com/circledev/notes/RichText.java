package com.circledev.notes;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.text.style.URLSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Converts between editable spans, stored span data and export formats. */
public final class RichText {

    private RichText() {
    }

    public static Spanned fromLegacyHtml(String html) {
        try {
            return Html.fromHtml(html == null ? "" : html, Html.FROM_HTML_MODE_LEGACY);
        } catch (Exception e) {
            return new SpannableStringBuilder(html == null ? "" : html);
        }
    }

    public static List<SpanData> extract(Spanned text) {
        List<SpanData> out = new ArrayList<>();
        if (text == null) return out;
        int len = text.length();
        if (len == 0) return out;

        for (StyleSpan span : text.getSpans(0, len, StyleSpan.class)) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            if (start < 0 || end <= start) continue;
            if ((span.getStyle() & Typeface.BOLD) != 0) {
                out.add(new SpanData(start, end, SpanData.BOLD, null));
            }
            if ((span.getStyle() & Typeface.ITALIC) != 0) {
                out.add(new SpanData(start, end, SpanData.ITALIC, null));
            }
        }
        for (UnderlineSpan span : text.getSpans(0, len, UnderlineSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.UNDERLINE, null);
        }
        for (StrikethroughSpan span : text.getSpans(0, len, StrikethroughSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.STRIKE, null);
        }
        for (BackgroundColorSpan span : text.getSpans(0, len, BackgroundColorSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.HIGHLIGHT,
                    String.format(Locale.US, "#%06X", 0xFFFFFF & span.getBackgroundColor()));
        }
        for (RelativeSizeSpan span : text.getSpans(0, len, RelativeSizeSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.SIZE,
                    String.valueOf(span.getSizeChange()));
        }
        for (TypefaceSpan span : text.getSpans(0, len, TypefaceSpan.class)) {
            String family = span.getFamily();
            if (family != null && family.toLowerCase(Locale.US).contains("mono")) {
                add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.MONO, null);
            }
        }
        for (URLSpan span : text.getSpans(0, len, URLSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.LINK, span.getURL());
        }
        for (AlignmentSpan span : text.getSpans(0, len, AlignmentSpan.class)) {
            add(out, text.getSpanStart(span), text.getSpanEnd(span), SpanData.ALIGN,
                    alignmentValue(span.getAlignment()));
        }
        return out;
    }

    private static void add(List<SpanData> out, int start, int end, String type, String value) {
        if (start >= 0 && end > start) out.add(new SpanData(start, end, type, value));
    }

    private static String alignmentValue(Layout.Alignment alignment) {
        if (alignment == Layout.Alignment.ALIGN_CENTER) return "center";
        if (alignment == Layout.Alignment.ALIGN_OPPOSITE) return "right";
        return "normal";
    }

    public static void apply(Spannable dest, List<SpanData> spans) {
        if (dest == null || spans == null) return;
        int len = dest.length();
        for (SpanData d : spans) {
            if (d == null || d.type == null) continue;
            int start = Math.max(0, Math.min(d.start, len));
            int end = Math.max(0, Math.min(d.end, len));
            if (start >= end) continue;
            Object span = null;
            switch (d.type) {
                case SpanData.BOLD:
                    span = new StyleSpan(Typeface.BOLD);
                    break;
                case SpanData.ITALIC:
                    span = new StyleSpan(Typeface.ITALIC);
                    break;
                case SpanData.UNDERLINE:
                    span = new UnderlineSpan();
                    break;
                case SpanData.STRIKE:
                    span = new StrikethroughSpan();
                    break;
                case SpanData.HIGHLIGHT:
                    span = new BackgroundColorSpan(parseColor(d.value, 0xFFFFF59D));
                    break;
                case SpanData.SIZE:
                    span = new RelativeSizeSpan(parseFloat(d.value, 1f));
                    break;
                case SpanData.MONO:
                    span = new TypefaceSpan("monospace");
                    break;
                case SpanData.LINK:
                    if (d.value != null && !d.value.isEmpty()) span = new URLSpan(d.value);
                    break;
                case SpanData.ALIGN:
                    Layout.Alignment alignment = "center".equals(d.value)
                            ? Layout.Alignment.ALIGN_CENTER
                            : "right".equals(d.value)
                            ? Layout.Alignment.ALIGN_OPPOSITE
                            : Layout.Alignment.ALIGN_NORMAL;
                    span = new AlignmentSpan.Standard(alignment);
                    break;
                default:
                    break;
            }
            if (span != null) {
                dest.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static int parseColor(String value, int fallback) {
        try {
            return value == null ? fallback : Color.parseColor(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return value == null ? fallback : Float.parseFloat(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static Spannable render(String body, List<SpanData> spans) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(body == null ? "" : body);
        apply(ssb, spans);
        return ssb;
    }

    /** Minimal markdown formatter for AI replies. */
    public static Spannable markdown(String text) {
        String src = text == null ? "" : text;
        SpannableStringBuilder out = new SpannableStringBuilder();
        int i = 0;
        while (i < src.length()) {
            if (src.startsWith("**", i)) {
                int end = src.indexOf("**", i + 2);
                if (end > i + 2) {
                    int start = out.length();
                    out.append(src, i + 2, end);
                    out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 2;
                    continue;
                }
            }
            if (src.charAt(i) == '`') {
                int end = src.indexOf('`', i + 1);
                if (end > i + 1) {
                    int start = out.length();
                    out.append(src, i + 1, end);
                    out.setSpan(new TypefaceSpan("monospace"), start, out.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    out.setSpan(new BackgroundColorSpan(0x22808080), start, out.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 1;
                    continue;
                }
            }
            if (src.charAt(i) == '*') {
                int end = src.indexOf('*', i + 1);
                if (end > i + 1) {
                    int start = out.length();
                    out.append(src, i + 1, end);
                    out.setSpan(new StyleSpan(Typeface.ITALIC), start, out.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 1;
                    continue;
                }
            }
            out.append(src.charAt(i));
            i++;
        }
        return out;
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Export one note to simple, readable HTML using the stored spans. */
    public static String toHtml(Note note) {
        String body = note.body == null ? "" : note.body;
        StringBuilder out = new StringBuilder();
        out.append("<p>");
        String currentKey = "";
        List<String> openTags = new ArrayList<>();
        List<String> closeTags = new ArrayList<>();

        for (int i = 0; i < body.length(); i++) {
            boolean bold = covers(note.spans, i, SpanData.BOLD);
            boolean italic = covers(note.spans, i, SpanData.ITALIC);
            boolean underline = covers(note.spans, i, SpanData.UNDERLINE);
            boolean strike = covers(note.spans, i, SpanData.STRIKE);
            String highlight = valueAt(note.spans, i, SpanData.HIGHLIGHT);
            String size = valueAt(note.spans, i, SpanData.SIZE);
            String link = valueAt(note.spans, i, SpanData.LINK);
            boolean mono = covers(note.spans, i, SpanData.MONO);

            StringBuilder key = new StringBuilder();
            key.append(bold).append('|').append(italic).append('|').append(underline)
                    .append('|').append(strike).append('|').append(highlight).append('|')
                    .append(size).append('|').append(link).append('|').append(mono);

            if (!key.toString().equals(currentKey)) {
                for (int c = openTags.size() - 1; c >= 0; c--) {
                    out.append("</").append(closeTags.get(c)).append(">");
                }
                openTags.clear();
                closeTags.clear();
                if (link != null && !link.isEmpty()) {
                    out.append("<a href=\"").append(escapeHtml(link)).append("\">");
                    openTags.add("a");
                    closeTags.add("a");
                }
                if (mono) {
                    out.append("<code>");
                    openTags.add("code");
                    closeTags.add("code");
                }
                if (highlight != null) {
                    out.append("<mark style=\"background-color:").append(highlight).append("\">");
                    openTags.add("mark");
                    closeTags.add("mark");
                }
                if (size != null) {
                    out.append("<span style=\"font-size:").append(size).append("em\">");
                    openTags.add("span");
                    closeTags.add("span");
                }
                if (bold) {
                    out.append("<b>");
                    openTags.add("b");
                    closeTags.add("b");
                }
                if (italic) {
                    out.append("<i>");
                    openTags.add("i");
                    closeTags.add("i");
                }
                if (underline) {
                    out.append("<u>");
                    openTags.add("u");
                    closeTags.add("u");
                }
                if (strike) {
                    out.append("<s>");
                    openTags.add("s");
                    closeTags.add("s");
                }
                currentKey = key.toString();
            }

            char c = body.charAt(i);
            if (c == '\n') out.append("<br>");
            else out.append(escapeHtml(String.valueOf(c)));
        }
        for (int c = openTags.size() - 1; c >= 0; c--) {
            out.append("</").append(closeTags.get(c)).append(">");
        }
        out.append("</p>");
        return out.toString();
    }

    private static boolean covers(List<SpanData> spans, int index, String type) {
        if (spans == null) return false;
        for (SpanData s : spans) {
            if (s != null && type.equals(s.type) && index >= s.start && index < s.end) return true;
        }
        return false;
    }

    private static String valueAt(List<SpanData> spans, int index, String type) {
        if (spans == null) return null;
        for (SpanData s : spans) {
            if (s != null && type.equals(s.type) && index >= s.start && index < s.end) {
                return s.value;
            }
        }
        return null;
    }
}
