<div align="center">

```
╔═══════════════════════════════════╗
║      📝  N O T E S         ║
║   Android · Material You · Java   ║
╚═══════════════════════════════════╝
```

# NoteFlow — Native Java Edition

**Aplikasi catatan Android bergaya Material You, sekarang 100% native Java.**
*Tanpa WebView, tanpa file HTML, tanpa SketchWare.*

<br/>

![Version](https://img.shields.io/badge/versi-8.0-006A6A?style=flat-square)
![Java](https://img.shields.io/badge/Java-Native-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Build](https://img.shields.io/badge/build-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![License](https://img.shields.io/badge/Lisensi-MIT-22C55E?style=flat-square)

</div>

---

## 📖 Tentang Proyek

NoteFlow dulu berupa satu file HTML yang dijalankan di WebView. Versi ini adalah **rewrite
penuh ke Android native (Java)**: seluruh UI dibuat dengan View Android, penyimpanan memakai
file JSON lokal, dan tidak ada satu pun halaman web yang dimuat.

```
Developer  :  Devin Avio Dika Putra (Vio)
Studio     :  BunglonSigma Dev
Package    :  com.circledev.notes
Platform   :  Android native (Java, API 24+)
Repo       :  github.com/BunglonMewing/Notes
```

---

## ✨ Fitur

<table>
<tr>
<td width="50%">

### 📝 Catatan
- Rich text native: **bold, italic, underline, strikethrough**
- Highlight, heading H1/H2/H3, rata kiri/tengah/kanan
- Bullet, nomor, kutipan, blok kode, link, tabel, garis
- Undo / redo, cari & ganti
- **Auto-save tiap 3 detik**
- 7 warna kartu, pin 📌, kunci 🔒, tag, pengingat 🔔
- Mode baca dengan ukuran font 12–24 dan tema gelap

</td>
<td width="50%">

### ☑️ To-Do List
- Banyak daftar to-do
- Progress bar otomatis
- Centang item langsung dari kartu
- Warna kartu dan penghitung selesai

### 🤖 Chat AI
- Riwayat chat tersimpan
- Konteks 3 catatan terbaru otomatis
- Quick prompt siap pakai
- Simpan jawaban AI menjadi catatan

</td>
</tr>
<tr>
<td>

### 🎯 Mode Fokus
- Timer Pomodoro 25 / 15 / 5 menit
- Ring progres native
- Pause, lanjut, reset
- Notifikasi saat selesai

### 🧩 Widget
- 4 gaya: Mini Note, To-Do, Banner, Daftar
- Update otomatis saat data disimpan
- Tap widget membuka aplikasi

</td>
<td>

### 💾 Penyimpanan
```
Layer 1 → cache SharedPreferences
Layer 2 → file internal aplikasi
Layer 3 → backup eksternal aplikasi
```
- Restore dari file JSON pilihan
- Ekspor HTML, CSV, dan backup JSON
- Salin semua teks ke clipboard
- Impor otomatis data `data.json` versi WebView lama

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

### 📋 8 Template
Meeting Notes · Brainstorm · Jurnal Harian · Bug Report
Goals OKR · Book Notes · Project Plan · Expense Tracker

### 📊 Statistik & Kategori
Jumlah catatan, to-do, tugas selesai, sampah, total kata, pengingat, dan chat AI.
Kategori: Teknologi, Kerja, Ide, Personal, Disematkan, Dikunci.

---

## 📂 Struktur Proyek

```
Notes/
├── app/src/main/
│   ├── java/com/circledev/notes/
│   │   ├── MainActivity.java          daftar catatan, pencarian, grid
│   │   ├── EditorActivity.java        editor rich text native
│   │   ├── TodoActivity.java          editor to-do
│   │   ├── ChatActivity.java          chat AI + riwayat
│   │   ├── FocusActivity.java         Pomodoro + RingView
│   │   ├── ReadActivity.java          mode baca
│   │   ├── Store.java                 model & penyimpanan JSON
│   │   ├── RichText.java              span, render, ekspor HTML
│   │   ├── Theme.java / Themer.java   12 tema
│   │   ├── Sheets.java                bottom sheet native
│   │   ├── AiClient.java              HTTP client AI
│   │   └── Reminder*/NoteWidget*      alarm & widget
│   ├── res/layout/                    layout native
│   └── AndroidManifest.xml
├── .github/workflows/android-build.yaml
└── build.gradle
```

---

## 🛠 Build Lokal

Prasyarat: **JDK 17**, **Android SDK 35**, dan Gradle wrapper (sudah disertakan).

```bash
./gradlew assembleDebug
```

APK dihasilkan di:

```
app/build/outputs/apk/debug/app-debug.apk
```

Pasang ke perangkat:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ GitHub Actions

Workflow [`.github/workflows/android-build.yaml`](.github/workflows/android-build.yaml) berjalan
otomatis setiap push/PR ke `main`/`master`, atau manual lewat **Run workflow**:

1. Checkout repository
2. Set up JDK 17 (Temurin)
3. Set up Android SDK + platform 35
4. Build `assembleDebug` dengan Gradle wrapper
5. Upload **APK** sebagai artifact `noteflow-debug-apk`

APK dapat diunduh dari tab **Actions → run terakhir → Artifacts**.

---

## 🔔 Izin

| Izin | Kegunaan |
|------|----------|
| `INTERNET` | Chat AI |
| `POST_NOTIFICATIONS` | Pengingat & timer fokus (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Menjadwalkan ulang pengingat setelah reboot |

Penyimpanan memakai **Storage Access Framework** dan folder aplikasi, jadi tidak memerlukan
izin storage berbahaya.

---

## ⚠️ Catatan AI

Chat AI memakai endpoint yang sama dengan versi HTML:
`https://zoyanz-api.vercel.app/ai/copilot`. Saat pengecekan, endpoint pihak ketiga tersebut
mengembalikan error upstream (`403`/`500`), sehingga balasan akan menampilkan pesan
"Koneksi bermasalah". Ganti konstanta `API` di
[`AiClient.java`](app/src/main/java/com/circledev/notes/AiClient.java) bila endpoint baru tersedia.

---

## 📄 Lisensi

MIT © BunglonSigma Dev
