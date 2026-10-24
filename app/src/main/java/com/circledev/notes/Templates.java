package com.circledev.notes;

public final class Templates {

    public static class Template {
        public final String icon;
        public final String name;
        public final String desc;
        public final String tag;
        public final String color;
        public final String title;
        public final String body;

        Template(String icon, String name, String desc, String tag, String color,
                 String title, String body) {
            this.icon = icon;
            this.name = name;
            this.desc = desc;
            this.tag = tag;
            this.color = color;
            this.title = title;
            this.body = body;
        }
    }

    private Templates() {
    }

    public static Template[] all() {
        String date = Util.today();
        String longDate = Util.fmtDateLong();
        return new Template[]{
                new Template("📋", "Meeting Notes", "Agenda, diskusi, action items", "kerja", "cb",
                        "Meeting Notes",
                        "📋 Meeting Notes\nTanggal: " + date + "\nPeserta: \n\nAgenda\n• \n\nDiskusi\n• \n\nAction Items\n• [ ] "),
                new Template("💡", "Ide & Brainstorm", "Tampung semua ide kreatif", "ide", "cy",
                        "Ide & Brainstorm",
                        "💡 Brainstorm\nTopik: \n\nIde Utama\n• \n• \n• \n\nPro & Kontra\n✅ Pro: \n❌ Kontra: "),
                new Template("📖", "Jurnal Harian", "Catatan harian & refleksi", "personal", "cv",
                        "Jurnal — " + longDate,
                        "📖 Jurnal — " + longDate + "\n\nHari ini saya...\n\n\nYang saya pelajari\n\n\nRencana besok\n• "),
                new Template("🐛", "Bug Report", "Dokumentasi bug & fix", "teknologi", "co",
                        "Bug Report",
                        "🐛 Bug Report\nTanggal: " + date + "\n\nDeskripsi Bug\n\n\nSteps to Reproduce\n1. \n2. \n\nExpected vs Actual\nExpected: \nActual: \n\nRoot Cause\n\n\nFix\n"),
                new Template("🎯", "Goals & OKR", "Target dan key results", "personal", "cg",
                        "Goals & OKR",
                        "🎯 Goals\nPeriode: \n\nObjective\n\n\nKey Results\n• KR1: \n• KR2: \n• KR3: \n\nProgress\n0%"),
                new Template("📚", "Book Notes", "Ringkasan buku & insight", "personal", "cp",
                        "Book Notes",
                        "📚 Book Notes\nJudul: \nPenulis: \n\nMain Ideas\n• \n\nFavorite Quotes\n\"\"\n\nKey Takeaway\n"),
                new Template("🚀", "Project Plan", "Rencana project & milestone", "teknologi", "cb",
                        "Project Plan",
                        "🚀 Project Plan\nProject: \nDeadline: \n\nScope\n\n\nMilestones\n• [ ] Fase 1: \n• [ ] Fase 2: \n• [ ] Launch: \n\nTech Stack\n"),
                new Template("💰", "Expense Tracker", "Catat pengeluaran harian", "personal", "cy",
                        "Expense Tracker",
                        "💰 Pengeluaran — " + date + "\n\nItem | Jumlah | Kategori\n-----|--------|---------\n | Rp | \n\nTotal: Rp"),
        };
    }
}
