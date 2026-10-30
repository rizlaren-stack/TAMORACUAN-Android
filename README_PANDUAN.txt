TAMORACUAN - Proyek Aplikasi Android WebView
============================================

ALAMAT WEBSITE: https://tamoracuan.com/
NAMA APLIKASI: TAMORACUAN
PACKAGE PRODUKSI: com.tamoracuan.app
PACKAGE DEBUG: com.tamoracuan.app.debug

PENTING:
File ZIP ini adalah SOURCE PROJECT Android, BUKAN APK siap instal.
Untuk menghasilkan APK, proyek harus di-build dengan Android Studio
atau layanan GitHub Actions. Website asli tidak diubah sama sekali.

CARA 1 - LEWAT ANDROID STUDIO WINDOWS:
1. Instal Android Studio dari https://developer.android.com/studio
2. Ekstrak ZIP, lalu pilih Open dan arahkan ke folder TAMORACUAN_Android.
3. Hubungkan internet; tunggu Gradle Sync (Android SDK 35, Gradle 8.9).
4. Klik menu Build > Build Bundle(s) / APK(s) > Build APK(s).
5. APK debug berada di:
   app\build\outputs\apk\debug\app-debug.apk
6. Pindahkan APK ke Android dan instal untuk uji coba.

CARA 2 - LEWAT GITHUB ACTIONS (TANPA INSTALL ANDROID STUDIO):
1. Buat repository GitHub baru milik sendiri.
2. Upload semua isi folder proyek ini ke root repository tersebut,
   termasuk folder .github/workflows (bukan hanya ZIP-nya).
3. Buka tab Actions pada repo, pilih 'Build Android APK (uji coba)'.
4. Klik 'Run workflow' pada branch main. Jika workflow sudah berjalan
   otomatis karena push, buka run terbarunya.
5. Setelah berhasil, unduh artifact TAMORACUAN-APK-TEST.
6. Ekstrak ZIP artifact -> app-debug.apk.

APK DEBUG HANYA UNTUK UJI COBA.
Untuk APK distribusi resmi, buat file APK RELEASE yang ditandatangani
menggunakan keystore MILIK SENDIRI, dan simpan keystore+password dengan
aman. Jangan sebarkan keystore. Di Android Studio:
Build > Generate Signed Bundle / APK > APK > Create new key store.
Pilih release, bukan debug. Simpan berkas keystore untuk update berikutnya.

CATATAN PENTING:
- Android dapat meminta izin 'Install unknown apps' saat instal APK dari link.
- Aplikasi perlu internet; konten mengikuti perubahan website langsung.
- Login, deposit/QRIS, game provider, pemilih gambar, popup serta download
  harus diuji pada perangkat Android asli sebelum dibagikan kepada member.
- Popup/provider atau sistem keamanan pihak ketiga mungkin menolak WebView.
- Ikon launcher saat ini bergaya teks/warna TAMORACUAN, BUKAN logo asli
  dari website. Sebelum distribusi, ganti PNG ic_launcher* dengan ikon resmi.
- Pembaruan aplikasi membutuhkan keystore signing yang sama, package ID
  yang sama, serta versionCode yang lebih besar.
- Proyek ini tidak otomatis mengunggah APK ke situs atau menghasilkan
  link download publik. Unggah APK release sendiri ke hosting bila selesai.
- Jangan menaruh password/secret website ke kode aplikasi.
