# Phát hành, Checksum & Tư liệu Truyền thông HTBB

## 1. Bản phát hành Open Test (`v0.9.33-opentest`, BAN `933`)

Các tệp cài đặt chính thức cho Android, PC (Windows / Linux) và J2ME được đính kèm tại GitHub Release **`v0.9.33-opentest`** kèm mã kiểm toàn vẹn SHA-256:

```text
c6596f926c26a8570fd80fb0de5060065b1073dbf43a39ddbec88d717b772cd8  HTBB-0.9.33-opentest-android.apk
a3e9a3d21fae4a914caca82bed61a83608fc682a5e64ed9a4d931f384724b1b6  HTBB-0.9.33-opentest.jar
70ffe97eb6e62af1b4b67487928214540780de31e3eae59ca06875245d26ea8c  HTBB-0.9.33-opentest-nhe.jar
0de1f0550304ae8c64ff5dff4a0e960024d51c38770bfbb6a844bc7ce00265e8  HTBB-PC-Linux-0.9.33.AppImage
4e69143001276836d03479db6f93b5d5184555dc87b67ac30a2705be096e3dd7  HTBB-PC-Linux-0.9.33.tar.gz
f71437823aaf3bcc7f975975f0f8a43245407c89b185aac50981ab99eadc93d1  HTBB-PC-Setup-0.9.33.exe
65cdcc9d19ae04c5811cd428ca94a495a6e0014bddcae5afc24d49a286c1ec42  HTBB-PC-Windows-0.9.33.zip
```

## 2. Tư liệu giới thiệu & Chế độ chơi

### Tổng quan Open Test
- Nhập vai nhà huấn luyện, chọn 1 trong 5 Pokémon khởi đầu (`Charmander`, `Bulbasaur`, `Squirtle`, `Pichu`, `Eevee`).
- Theo dấu nhiệm vụ ở làng tân thủ, đánh quái, tự động đánh, lên cấp, tiến hoá, săn Pokémon hiếm, làm yếu rồi ném Poké Ball và xem sao IV trong Hộp PC.
- Hệ sinh thái & tính năng cộng đồng: câu cá ở hồ, hành trang dạng lưới, luyện đồ ở lò rèn, tủ trang phục, Chat Thế giới / Bạn bè, phó bản Hầm ngục, thời tiết – màu mùa, Ngày cộng đồng.
- PvP kéo để ngắm (không auto, không khoá mục tiêu), đấu trường xếp hạng theo Elo, Cúp PvP tuần và Đấu trường Bảo Bối 3v3 (tốc độ ×2).

### Ghi công âm thanh & tài nguyên
- **Pokémon**: © Nintendo / Game Freak / Creatures (dự án fan phi thương mại — xem [giay-phep.md](giay-phep.md)).
- **Sprite & chân dung Pokémon**: PMDCollab (`CC BY-NC 4.0`).
- **Nhạc nền video & Open Test**: *"Chiptune Adventures"* (`1. Stage 1`, `2. Stage2`, `3. Boss Fight`) — Juhani Junkala (`OpenGameArt.org`, **CC0 1.0 Public Domain**).

## 3. Nhánh P2P (`p2p` · `v0.9.30-p2p`)

Mã nguồn tích hợp Peerova P2P tunnel cho server Java (Windows/Linux glibc 2.17+) và các client Android / PC / Unity nằm trên nhánh **`p2p`** (`docs/p2p-win2012.md`). Khi cấu hình trên máy chủ, điền khoá `sv.p2p_license=` vào tệp `server.ini` cục bộ (không đưa khoá thật vào Git).
