# Ghi công âm thanh (bản jar J2ME)

Câu hỏi mở **M4** (nhạc, âm thanh) trong `KE_HOACH_CHUYEN_DOI.md`: bản jar đã có âm thanh, **chỉ dùng CC0**,
không dùng nhạc/tiếng Pokémon. Bản sao trong jar: `htbb-client/j2me/res/LICENSE-audio.txt` (`/LICENSE-audio.txt`).

| Tệp trong jar (`/am/`) | Nguồn | Giấy phép |
|---|---|---|
| `s_click`, `s_xacnhan`, `s_huy`, `s_mo`, `s_nhat` | Kenney "Interface Sounds" (click_001, confirmation_001, back_001, open_001, pluck_001) | CC0 1.0 |
| `s_danh`, `s_chimang`, `s_bidanh`, `s_ithieuqua` | Kenney "Impact Sounds" (impactPunch_medium_000, impactPunch_heavy_001, impactSoft_heavy_001, impactGeneric_light_000) | CC0 1.0 |
| `s_truot`, `s_vang` | Kenney "RPG Audio" (knifeSlice2, handleCoins2) | CC0 1.0 |
| `s_phep`, `s_hieuqua`, `s_chet`, `s_dichchuyen`, `s_hoimau`, `s_canhbao` | Kenney "Digital Audio" (zapTwoTone, phaserUp3, phaserDown2, phaseJump1, powerUp2, lowThreeTone) | CC0 1.0 |
| `s_lencap`, `s_tienhoa`, `s_nhiemvu` | Kenney "Music Jingles", 8-Bit (jingles_NES12, NES00, NES04) | CC0 1.0 |
| `s_boss` | Tự tổng hợp (răng cưa quét xuống + nhiễu lọc) | CC0 1.0 |
| `n_*.mid` (9 bài: tiêu đề, thị trấn, đường, rừng, hang, tuyết, sa mạc, đấu chiến, boss) | Nhạc chiptune gốc soạn bằng thuật toán (`j2me/tools/lam_am_thanh.py`) | CC0 1.0 |

- Gói Kenney tải từ https://kenney.nl/assets (rpg-audio, impact-sounds, interface-sounds, digital-audio,
  music-jingles), mỗi gói có `License.txt` ghi "Creative Commons Zero, CC0". Kenney không bắt buộc ghi công.
- Gói tải về KHÔNG đưa vào git; chỉ đưa tệp đã chuyển đổi (WAV 8 bit mono 11–16 kHz, bản nhẹ 8 kHz; MIDI).
- Sinh lại: `python3 -P j2me/tools/lam_am_thanh.py <thư mục giải nén> j2me/res/am j2me/res_nhe/am`.
- Dung lượng: bản thường ~118 KB tệp (~72 KB nén trong jar); bản nhẹ (`GOI=nhe`) chỉ 16 tiếng hiệu ứng 8 kHz,
  ~33 KB (~28 KB nén), không có nhạc.
