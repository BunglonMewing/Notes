package com.circledev.notes;

import android.graphics.Color;

/** The 12 Material You inspired palettes from NoteFlow v7. */
public final class Theme {

    public final String id;
    public final String name;
    public final String sub;
    public final boolean dark;

    public final int primary;
    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;
    public final int secondaryContainer;
    public final int onSecondaryContainer;
    public final int surface1;
    public final int surface2;
    public final int surface3;
    public final int text;
    public final int textVariant;
    public final int outline;
    public final int outlineVariant;
    public final int error;
    public final int bg;

    private Theme(String id, String name, String sub, boolean dark,
                  String primary, String onPrimary, String primaryContainer,
                  String onPrimaryContainer, String secondaryContainer,
                  String surface1, String surface2, String surface3,
                  String text, String textVariant, String outline, String outlineVariant,
                  String error, String bg) {
        this.id = id;
        this.name = name;
        this.sub = sub;
        this.dark = dark;
        this.primary = Color.parseColor(primary);
        this.onPrimary = Color.parseColor(onPrimary);
        this.primaryContainer = Color.parseColor(primaryContainer);
        this.onPrimaryContainer = Color.parseColor(onPrimaryContainer);
        this.secondaryContainer = Color.parseColor(secondaryContainer);
        this.onSecondaryContainer = Color.parseColor(textVariant);
        this.surface1 = Color.parseColor(surface1);
        this.surface2 = Color.parseColor(surface2);
        this.surface3 = Color.parseColor(surface3);
        this.text = Color.parseColor(text);
        this.textVariant = Color.parseColor(textVariant);
        this.outline = Color.parseColor(outline);
        this.outlineVariant = Color.parseColor(outlineVariant);
        this.error = Color.parseColor(error);
        this.bg = Color.parseColor(bg);
    }

    public static final Theme[] ALL = new Theme[]{
            new Theme("teal", "Teal Ocean", "Default", false,
                    "#006A6A", "#FFFFFF", "#9DF0EF", "#002020", "#CCE8E8",
                    "#EAF4F4", "#DFF0F0", "#D3ECEC", "#161D1D", "#3F4949",
                    "#6F7979", "#BEC9C9", "#BA1A1A", "#F4FBFB"),
            new Theme("violet", "Violet Dream", "Elegan", false,
                    "#6750A4", "#FFFFFF", "#EADDFF", "#21005D", "#E8DEF8",
                    "#F7F2FA", "#F0EBF5", "#E8E3F0", "#1C1B1F", "#49454F",
                    "#79747E", "#CAC4D0", "#BA1A1A", "#FFFBFE"),
            new Theme("rose", "Rose Bloom", "Romantis", false,
                    "#9B1B30", "#FFFFFF", "#FFDAD8", "#3B0009", "#FFD8DC",
                    "#FFF0F1", "#FFE5E7", "#FFDADC", "#1C0002", "#534042",
                    "#857373", "#D8C2C2", "#BA1A1A", "#FFFBFF"),
            new Theme("amber", "Amber Glow", "Cozy", false,
                    "#7A5900", "#FFFFFF", "#FFDEA8", "#271900", "#F5E0B0",
                    "#FFF8EE", "#FCEFD8", "#F8E4C4", "#261A00", "#534B32",
                    "#7A7061", "#D0C4A8", "#BA1A1A", "#FFFBF5"),
            new Theme("forest", "Forest Green", "Segar", false,
                    "#1D6B3A", "#FFFFFF", "#A8F5C0", "#002110", "#C0EDD0",
                    "#EEF7F1", "#E2F2E8", "#D4ECDC", "#0D1F14", "#3A4D3F",
                    "#5C7062", "#BACFC0", "#BA1A1A", "#F4FBF6"),
            new Theme("ocean", "Deep Ocean", "Tenang", false,
                    "#0061A4", "#FFFFFF", "#D1E4FF", "#001D36", "#C8E6FF",
                    "#EEF4FF", "#E2EDFF", "#D5E6FF", "#001D36", "#3A4D5E",
                    "#4E6075", "#B8C8D8", "#BA1A1A", "#F5F9FF"),
            new Theme("dark", "Dark Teal", "Gelap", true,
                    "#9DF0EF", "#003737", "#004F4F", "#9DF0EF", "#324B4B",
                    "#141D1D", "#1A2525", "#1F2D2D", "#E1E9E9", "#BEC9C9",
                    "#899393", "#3F4949", "#FFB4AB", "#0E1414"),
            new Theme("dark-violet", "Dark Violet", "Gelap", true,
                    "#D0BCFF", "#381E72", "#4F378B", "#EADDFF", "#4A4458",
                    "#211F26", "#282530", "#302D38", "#E6E1E5", "#CAC4D0",
                    "#938F99", "#49454F", "#F2B8B5", "#1C1B1F"),
            new Theme("dark-rose", "Dark Rose", "Gelap", true,
                    "#FFB3B4", "#680016", "#930025", "#FFDAD8", "#5C3737",
                    "#231718", "#2A1D1E", "#322323", "#F5DDDD", "#D8C2C2",
                    "#A48C8C", "#534042", "#FFB4AB", "#1C0002"),
            new Theme("dark-amber", "Dark Amber", "Gelap", true,
                    "#F5BF48", "#3F2D00", "#564200", "#FFDEA8", "#4A3B00",
                    "#201A00", "#272100", "#2E2700", "#EEE0BB", "#D0C4A8",
                    "#B0A58A", "#4A3B00", "#FFB4AB", "#181400"),
            new Theme("dark-forest", "Dark Forest", "Gelap", true,
                    "#8DD9A4", "#003919", "#004D28", "#A8F5C0", "#1E3D28",
                    "#101E14", "#162519", "#1C2C1F", "#CDE9D5", "#A0C8AC",
                    "#6A9878", "#1E3D28", "#FFB4AB", "#0A140D"),
            new Theme("dark-ocean", "Dark Ocean", "Gelap", true,
                    "#9ECAFF", "#003258", "#004882", "#D1E4FF", "#1A3A5C",
                    "#0D1929", "#122030", "#172838", "#D1E4FF", "#A8C8E8",
                    "#7096B4", "#1A3A5C", "#FFB4AB", "#080F1A"),
    };

    public static Theme byId(String id) {
        if (id != null) {
            for (Theme t : ALL) {
                if (t.id.equals(id)) return t;
            }
        }
        return ALL[0];
    }

    /** Card background for a stored color key (cy, cp, cb, cg, cv, co). */
    public int cardColor(String key) {
        if (key == null || key.isEmpty()) return surface1;
        switch (key) {
            case "cy": return Color.parseColor(dark ? "#3D3500" : "#FFF9C4");
            case "cp": return Color.parseColor(dark ? "#3D0017" : "#FCE4EC");
            case "cb": return Color.parseColor(dark ? "#001D36" : "#E3F2FD");
            case "cg": return Color.parseColor(dark ? "#002105" : "#E8F5E9");
            case "cv": return Color.parseColor(dark ? "#2A0041" : "#F3E5F5");
            case "co": return Color.parseColor(dark ? "#331200" : "#FFF3E0");
            default: return surface1;
        }
    }

    /** Swatch shown in the color picker. */
    public static int swatch(String key, boolean dark) {
        if (key == null || key.isEmpty()) return 0;
        switch (key) {
            case "cy": return Color.parseColor(dark ? "#3D3500" : "#FFF9C4");
            case "cp": return Color.parseColor(dark ? "#3D0017" : "#FCE4EC");
            case "cb": return Color.parseColor(dark ? "#001D36" : "#E3F2FD");
            case "cg": return Color.parseColor(dark ? "#002105" : "#E8F5E9");
            case "cv": return Color.parseColor(dark ? "#2A0041" : "#F3E5F5");
            case "co": return Color.parseColor(dark ? "#331200" : "#FFF3E0");
            default: return 0;
        }
    }

    public static final String[] CARD_KEYS = {"", "cy", "cp", "cb", "cg", "cv"};
}
