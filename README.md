<div align="center">
 
<br/>
 
```
╔═══════════════════════════════════╗
║      📝  N O T E S         ║
║   Android 15 · Material You║
╚═══════════════════════════════════╝
```
 
# NoteFlow
 
**Aplikasi catatan modern bergaya Android 15 / Material You**
*Single-file HTML · Bisa dijadikan APK via SketchWare Pro*
 
<br/>
 
![Version](https://img.shields.io/badge/versi-7.0-006A6A?style=flat-square)
![HTML](https://img.shields.io/badge/HTML-Single%20File-E34F26?style=flat-square&logo=html5&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-Vanilla-F7DF1E?style=flat-square&logo=javascript&logoColor=black)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![SketchWare](https://img.shields.io/badge/SketchWare-Pro-006A6A?style=flat-square)
![License](https://img.shields.io/badge/Lisensi-MIT-22C55E?style=flat-square)
 
<br/>
 
> *Capek sama app catatan bawaan yang UInya jadul?*
> *Gue bikin sendiri — dan ini hasilnya.*
 
</div>
 
---
 
## 📖 Tentang Proyek
 
**NoteFlow** adalah aplikasi catatan berbasis **single-file HTML** dengan desain **Android 15 / Material You** yang dibangun dari nol menggunakan HTML, CSS, dan JavaScript vanilla — tanpa framework apapun.
 
Proyek ini lahir dari frustrasi terhadap aplikasi catatan bawaan yang tampilannya membosankan. Dibangun langsung dari **HP Android** menggunakan **Termux** dan **SketchWare Pro**, lalu di-deploy sebagai APK menggunakan WebView.
 
```
Developer  :  Devin Avio Dika Putra (Vio)
Studio     :  BunglonSigma Dev
Package    :  com.circledev.notes
Platform   :  Android (SketchWare Pro + WebView)
Repo       :  github.com/BunglonMewing/Notes
```
 
---
 
## ✨ Fitur
 
<table>
<tr>
<td width="50%">
 
### 📝 Catatan
- Rich Text Editor full screen
- **Bold, Italic, Underline, Strikethrough**
- Highlight teks 🖊
- Heading H1 / H2 / H3
- List bullet & nomor
- Blockquote, Code block
- Insert tabel, link, garis
- Undo / Redo
- **Auto-Save** tiap 3 detik
- 7 warna kartu catatan
- Pin & Kunci catatan (🔒)
- Tag & Pengingat
 
</td>
<td width="50%">
 
### ☑️ To-Do List
- Multiple daftar to-do
- Progress bar otomatis
- Centang item dari kartu
- Warna kartu to-do
 
### 🤖 Chat AI
- Powered by Zoyanz Copilot API
- Konteks catatan otomatis
- Quick prompts siap pakai
- Simpan jawaban AI → catatan
 
</td>
</tr>
<tr>
<td>
 
### 🎯 Mode Fokus (Pomodoro)
- Progress ring SVG animasi
- Durasi 25 / 15 / 5 menit
- Pause & Reset
- Notifikasi selesai
 
### 👁 Mode Baca
- Tampilan bersih tanpa toolbar
- Font adjustable (12–24px)
- Toggle dark/light mode
 
</td>
<td>
 
### 💾 Penyimpanan Triple Layer
```
Layer 1 → localStorage (browser)
Layer 2 → file internal app
Layer 3 → /sdcard/NoteFlow/
```
**Aman saat update APK** ✓
 
### 📋 Template (8 Jenis)
Meeting Notes · Brainstorm · Jurnal
Bug Report · Goals OKR · Book Notes
Project Plan · Expense Tracker
 
</td>
</tr>
</table>
 
### 🎨 12 Tema
 
| ☀️ Terang | 🌙 Gelap |
|-----------|---------|
| Teal Ocean (default) | Dark Teal |
| Violet Dream | Dark Violet |
| Rose Bloom | Dark Rose |
| Amber Glow | Dark Amber |
| Forest Green | Dark Forest |
| Deep Ocean | Dark Ocean |
 
### 📤 Ekspor & Backup
- Simpan ke file HP (`/sdcard/NoteFlow/backup.json`)
- Restore dari file backup
- Ekspor HTML (buka di browser)
- Ekspor CSV (buka di Excel/Sheets)
- Salin semua teks ke clipboard
 
### 🧩 Widget
- 4 gaya: Mini Note, To-Do, Banner, Daftar
- Update otomatis saat catatan disimpan
- Tap widget → buka app langsung
 
---
 
## 📂 Struktur Repo
 
```
Notes/
│
├── noteflow-v7.html          ← File utama (seluruh app ada di sini)
│
├── android/
│   ├── NoteWidgetProvider.java
│   └── res/
│       ├── layout/
│       │   └── widget_layout.xml
│       ├── drawable/
│       │   └── widget_bg.xml
│       └── xml/
│           └── widget_info.xml
│
└── README.md
```
 
> **Catatan:** Seluruh aplikasi ada di 1 file HTML. Folder `android/` berisi file tambahan khusus untuk integrasi widget di SketchWare Pro.
