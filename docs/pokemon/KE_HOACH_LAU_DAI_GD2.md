# Kế hoạch giai đoạn 2: đưa thiết kế dài hạn vào code

> Đầu vào: `THIET_KE_LAU_DAI.md` (số liệu), `KIEM_TOAN_KINH_TE.md` (hiện trạng). Bảng số đã có
> trong `ThongSoLauDai.java` và `sql/pokemon_lau_dai.sql`, nhưng **chưa nối vào game**.
>
> **Chỉ bắt đầu sau khi việc bản đồ xong.** Đội bản đồ đang sửa `Map*.java`, `RealController`,
> `MapKhoiTao`, `Tilemap`. Số dòng của `Map.java`, `MapLienDau.java`, `MapClan.java`, `OfflineMap.java`
> ghi ở dưới có thể đã lệch: **grep theo tên hàm**, đừng tin số dòng.
>
> Quy tắc chung:
> - Mọi thay đổi đặt sau cờ mới `sv.lau_dai=1`. Đọc qua một hàm duy nhất `LauDai.bat()`, giống `CheDoPokemon`.
> - Tắt cờ thì game giữ nguyên hành vi cũ, các test characterization cũ vẫn đúng.
> - Mỗi bước có test hàm thuần trong `test/`.
> - Không đổi tên package/class (CODING_CONVENTION §12).
> - Text người chơi thấy phải duyệt trước (§10).

## Thứ tự

Thứ tự được xếp theo rủi ro (thấp trước) và theo phụ thuộc. Mỗi bước có thể merge riêng.

### Bước 0: Hạ tầng (1 ngày, rủi ro thấp)

| # | Việc | File | Ghi chú |
|---|---|---|---|
| 0.1 | Cờ `sv.lau_dai` | `vps/server.ini.tmpl`, `src/server/CauHinh.java` (cạnh khoá `sv.tileExpLv50`, :45-46) | Mặc định 0 |
| 0.2 | `com.htbb.pokemon.LauDai` | mới | `bat()`; bọc `ThongSoLauDai`; đọc đè từ `cau_hinh` các khoá trong `data_kinh_te_lau_dai` |
| 0.3 | Áp `sql/pokemon_lau_dai.sql` | `tools/pokemon/xuat_sql_day_du.sh` | **Chỉ** thêm vào danh sách POKEMON sau khi bước 1 xong. Đã chạy được trên NovaDB (không dùng `CREATE TABLE … LIKE`) |
| 0.4 | `tob_kinh_te_ngay` + job đêm | mới `data/DatabaseKinhTe.java`, gọi từ `BaoTriDaily` | Chỉ số ở THIET_KE mục 2.5; PreparedStatement (§13) |

### Bước 1: Sửa lỗi độc lập với thiết kế (rủi ro thấp, làm ngay cả khi không bật cờ)

| # | Lỗi | Vị trí | Sửa |
|---|---|---|---|
| 1.1 | `Map.random()` tạo `new Random(System.currentTimeMillis())` mỗi lần gọi ⇒ roll thành công và roll bảo hiểm trùng nhau | `src/real/Map.java:3320-3323` (grep `public static int random(int n)`) | Dùng một `Random` tĩnh hoặc `ThreadLocalRandom`. **Ảnh hưởng mọi nơi gọi `Map.random`**: chạy lại toàn bộ test. Cũng sửa `Map.dropItem` (:4366) |
| 1.2 | Trừ 5.000 xu trước khi kiểm tra có đá luyện kim | `src/real/MapEpNgoc.java:639` so với `:641-648` | Đưa trừ tiền xuống sau bước kiểm tra, trong `Char.KHOA_TIEN` (§4) |
| 1.3 | `Pet.MATERIAL_UPDGRADE` thiếu phần tử thứ 6 ⇒ AIOOBE ở bậc 5 | `src/data/Pet.java:69-70, 703` | Thêm phần tử cho đủ, test giống `KiemHoaHinhPet` |
| 1.4 | Ép ngọc dùng `<=` nên tỉ lệ thật là 11% / 21% | `src/real/MapEpNgoc.java:862-863` | Đổi thành `<` |
| 1.5 | x2 vàng bị ghi đè | `src/real/Monster.java:931` | Bỏ dòng ghi đè |
| 1.6 | Tổ đội `nUser` luôn = 5 | `src/real/Region.java:1300-1330, 1561-1590` + bản sao (§3, 7 file ở bước 3) | Dùng số người thật |
| 1.7 | `getValueLua(2)` cộng 2 lần | `src/real/Char.java:3758`, `src/real/CharChiSo.java:775` | Bỏ một chỗ |
| 1.8 | `wModel` null ⇒ boss không nhận sát thương | `src/real/CharChiSo.java:591, 599` | Kiểm null |
| 1.9 | Bảng chiêu của c1 lệch 1 dòng (chiêu 6/7/8) | `src/real/CharManager.java:59-70` (`SKILL_DAM_PERCENT[1]`, `SKILL_COOLDOWN[1]`) | Bỏ dòng thừa ở chỉ số 6. Có test so số dòng giữa các class |
| 1.10 | Giftcode `tt` (2M lượng + 2M lượng khoá) | DB `giftcode` | `UPDATE giftcode SET limit_use=0 WHERE giftcode='tt'` qua file `.sql` (§7) |

### Bước 2: Kinh tế (rủi ro trung bình, cần bật cờ)

| # | Việc | Vị trí | Chi tiết |
|---|---|---|---|
| 2.1 | Bỏ lượng và lượng khoá rơi từ quái | `src/real/Monster.java:1091-1108` (đã có `boolean DropLuong = true`) | `DropLuong = !LauDai.bat()` |
| 2.2 | Xu rơi theo `XU_MOI_QUAI` | `Monster.java:919-945` | Tỉ lệ 30%, số lượng = `XU_MOI_QUAI[lv]/0,3`, hệ số lệch cấp; bỏ `/chiaXuRoi`. Monster nhận `rcvgold` từ `data_monster_lau_dai` |
| 2.3 | Năng lượng huấn luyện | mới `NangLuong` (lớp thuần) + `Char` (field phút đã dùng / tích luỹ) + reset ở `CharCapNhat.java:559-585` (khối đổi ngày, bản sao ở `CharNapDuLieu.java:925-940`) | Áp vào EXP, xu và đá ở mọi bản sao (bước 3). Lưu vào `pInfo` hoặc cột mới `tob_char.nang_luong` |
| 2.4 | Bỏ quà 10M xu ở cấp 35 | `src/real/MapXuLyLenh.java:303` | Thay bằng 1 món bậc vàng |
| 2.5 | Giá nâng cấp và tỉ lệ có pity | `MapEpNgoc.java:600-769` (`doFinishImbue`, mảng tỉ lệ ở `:617`), `CharCapNhat.java:866-1036` (`doAutoImbue`, `:870`) **cả hai bản** | `ThongSoLauDai.CHI_PHI_NC`, `tiLeNangCap(k, truot)`; lưu `truot` trên Item (dùng `otherAtt` chưa dùng: **grep trước**, §1) hoặc cột mới. Bùa bảo hộ là item mới (lượng khoá) thay bảo hiểm 0–4. +7 trở lên trượt thì −1 |
| 2.6 | Chỉ số mỗi cấp + theo % | `MapEpNgoc.java:657-661`, `CharCapNhat.java:926-931` | `CHI_SO_NC_PHAN_TRAM` |
| 2.7 | Thuế chợ 6% + phí đăng 1%; chuyển lượng 5%; trần chuyển xu trực tiếp | `src/real/OfflineMap.java:337-397`, `CharChuyenLuong.java:106-110` | Giữ nhánh thuế đội |
| 2.8 | Giá dịch chuyển, hồi sinh, sửa đồ theo thu nhập | `Map.java:16909` (xa phu), `BaiLuyenCong.java:55-77`, `Map.java:10416-10471` (hồi sinh), `Char.java:4197-4253` (sửa) | `3%/5% × xuMoiGio(cấp)` |
| 2.9 | Bán đồ cho NPC còn 5% + phân rã | `OfflineMap.java:1169-1171`, `MapXuLyLenh.java:392` (tự bán), phân rã: `OfflineMap.java:1865` (đã có "nghiền đồ") | Ra 2 đá + 10% giá |
| 2.10 | Shop: ngừng bán đồ có chỉ số bằng lượng, vé EXP chuyển sang lượng khoá 1/tuần | `data_shop` theo `data_shop_gia_lau_dai`; giới hạn tuần ở `doBuySpecialItem.java:40+` | Cần bảng đếm `tob_mua_tuan` |
| 2.11 | Bỏ hoàn xu khi tiêu lượng, mốc nạp, VIP theo nạp | `Char.java:14789-14797`, `MapQuaTang.java:832-834`, `Char.java:1255` (`up_px_xu_vip`) | Q10 |
| 2.12 | Lượng từ xếp hạng tuần | `SuKienDuaTop.java` / `tob_top_thuong` | Thêm khoá `LUONG:` vào `applyItemString` |
| 2.13 | Ngọc hệ theo giá mới | `MapEpNgoc.doFinishKhamItem` (:926-1022), `PRICE_KHAM` (:33) | Bảng giá `NGOC_HE_*` |

### Bước 3: EXP và cấp (rủi ro cao: đụng bảng cấp đang chạy)

| # | Việc | Vị trí |
|---|---|---|
| 3.1 | Nạp bảng EXP mới khi bật cờ (giữ bảng cũ khi tắt) | `src/real/LevelDetail.java:11-14` (`expMain`/`expRequire`/`expTemplate`), `setExpNew` :87-105, `getLevelFromExp` :50-66. **Chuyển đổi nhân vật cũ**: giữ **cấp**, đặt exp = 0 của cấp đó (script SQL một lần, có bản sao lưu) |
| 3.2 | Trần cấp theo mùa | `LevelDetail.setExpNew` (cắt theo `LauDai.tranCap(ngàyKhaiTruong)`) + `doUsePotion.java:229` ("lv max sv"). Khoá `sv.ngay_khai_truong` |
| 3.3 | Bỏ các hệ số EXP chồng nhau | `CharCapNhat.java:127` (`×3·TangEXP`), `:133-137` (×0,65/0,6), `Monster.java:775-818` (`apDungGiamExpTheoCapNV`), `MapXuLyLenh.java:191-194` và `:31-60` (chia 3) |
| 3.4 | Hệ số lệch cấp mới | **7 bản sao** (§3, đếm lại bằng `grep -c apDungGiamExpTheoCapNV`): `Region.java` 1163-1175 + 1544-1559 (hooks 1321/1582/1690), `RegionVantienTran.java` 1003/1279/1405, `MapTown.java` 817/1067/1197, `MapLienDau.java` 1692/1952/2083, `MapDanhNhau.java` 840/979/1566, `MapClan.java` 726/862, `CharCopy.java` 383/496/620, `CharTrangBi.java` 402. **Gom vào một hàm** `LauDai.expHaQuai(p, mt, raw)` rồi gọi từ mọi bản sao |
| 3.5 | Tinh thông | `Char` (field + lưu `pInfo`), `CharChiSo` (+% HP/tấn công), menu NPC -90 |
| 3.6 | Bắt kịp | trong `expHaQuai`: trung vị cấp tính 1 lần/giờ (`BaoTriDaily`) |
| 3.7 | Vận tiêu: EXP theo `expRequire` | `CharSuKien.java:67-83`: sau 3.1 tự đổi theo bảng mới. **Kiểm lại** permille |
| 3.8 | Cấp > 91 (năm 2) | client đọc cấp 1 byte (OK tới 127); `LevelDetail` mở 100; map và quái Hoenn mới (đội map); item bậc thần thoại / tối thượng (`data_item`, id mới **grep trước**, hiện max 967) |

### Bước 4: Nhiệm vụ (rủi ro trung bình)

| # | Việc | Vị trí |
|---|---|---|
| 4.1 | Kiểm `lv` cho nhiệm vụ chính + vé chương 2/tuần | `Char.createMainQuest` (`Char.java:12191`), `getAllQuestCanReceive` (:12274), `OnQuest` (:12429). Vé lưu ở bảng mới `tob_nhiem_vu_lau_dai` |
| 4.2 | **Lọc nhiệm vụ phụ theo cấp trước khi gửi** (danh sách ghi số lượng bằng 1 byte, tối đa 127) | `Char.getAllQuestCanReceive` (:12278-12289), `MessageCreator.createMsgCharQuest` (:2882, `writeByte(allQuest.size())`). Chỉ gửi nhiệm vụ có `lv ≤ cấp < lv+10` và tối đa 20 cái |
| 4.3 | Đổi `data_quest` sang `data_quest_moi` | `RENAME TABLE` (cuối `sql/pokemon_lau_dai.sql`, đang để comment). Loader `DatabaseXepHang.loadAllQuestTemplate` (:471): thêm `ORDER BY id`, vì hiện thứ tự phụ thuộc thứ tự chèn. **Chuyển tiến độ cũ**: người đang ở nhiệm vụ chính i (cũ) → `idMainQuest` mới theo cấp (script) |
| 4.4 | Bảng tiến độ cho loại mới (tuần, Pokédex, sư phạm, Hầm ngục, thành tựu, quay lại) | mới `tob_nhiem_vu_lau_dai(id_char, ma, tien_do, trang_thai, ngay)` + `NhiemVuLauDai` (lớp thuần) + menu NPC (`MapDoMenu`, **grep case trống**) |
| 4.5 | Vật phẩm nhiệm vụ: thêm tên mới | `data.ItemQuest` nằm trong jar (10 tên). Phương án: thêm lớp `data/ItemQuest.java` vào `src/` cùng chữ ký (javap), để class trong `src/` đè class trong jar. **Kiểm** `LoginHandler.java:173`, `DataGame.java:674`, `CharKhoiTao.java:115` (mảng `itemquest` theo độ dài) |
| 4.6 | Huy hiệu Gym và danh hiệu | `DanhHieu`, `tob_danh_hieu`; boss Gym (bước 6) |

### Bước 5: Sự kiện

| # | Việc | Vị trí |
|---|---|---|
| 5.1 | Thêm `Muc` cho mã `moi:*` (congdong, shiny, dotkich, tuanhe, pokedex, bungphat, chienvung, muapvp, valentine, 3004, thieunhi, quockhanh, 2010, hamngucmua, tongket) | `SuKien.java:142-205` (`DANH_MUC`), trang admin `web/admin/sukien.php` |
| 5.2 | Nạp lịch 52 tuần vào `tob_su_kien` (`mo_luc/dong_luc`) | script từ `data_su_kien_nam`; lịch gom thay đổi lúc 05:00 thứ Hai (`LichSuKien`) |
| 5.3 | Thưởng sự kiện là vật phẩm khoá, **hết hạn**; xu ≤ 25% ngân sách | `MapSuKien.java` (60 chỗ `subLuong`/`truNeuDu`), `Map.java:8321/8387, 11031-11408` |
| 5.4 | Giftcode: chỉ vật phẩm khoá, ≤ 2/tháng | `DatabaseGiftCode.java:430-495` (kiểm ở **nơi xử lý**, §11) |

### Bước 6: Chiến đấu, quái, boss (rủi ro cao; cần client)

| # | Việc | Vị trí |
|---|---|---|
| 6.1 | Hệ áp **sau** khi trừ thủ; sàn sát thương theo %; chí mạng ×1,5 | `CharChiSo.attackDam` :554-1017 (khối hệ :753-758, khối dam :810-829); PvP `Region.java:748-764`, `MapDanhNhau.java:263-288`, `MapLienDau.java:964-988` (trần 8% HP) |
| 6.2 | Chỉ số quái theo `data_monster_lau_dai` | `UPDATE … JOIN` (comment cuối SQL); `Database.loadMonsterTemplate` (:5632, cột ở :5734-5774) |
| 6.3 | Chiêu 3: 5 đòn × 220%; cân bằng dòng ±5% | `CharManager.java:59-70` (`SKILL_DAM_PERCENT`, `UP_DAMGE_SKILL`, `SKILL_COOLDOWN`), test tích HP×Tấn công×Thủ |
| 6.4 | Trạng thái (bỏng / độc / tê / ngủ / băng) | mới `TrangThaiPokemon` (lớp thuần); tick ở `LiveActor`/`Char` update; gói -97 |
| 6.5 | Gói đòn đánh -95 (cờ chí mạng / khắc / STAB) | kèm sau `Message(9)` ở `Region.java:1601` + bản sao (grep `new Message(9)`), đòn quái → người (`Monster.attack` :317-420) |
| 6.6 | Boss theo pha, báo trước (-96), nộ, đệ (-98), đóng góp (-99) | mới `GiaiDoanBoss`; `Boss.java` (lớp cha) + 16 file `Boss*.java`; 3 boss chưa gọi lớp cha `onDropItem` (BossTruRong, BossTruRongMoba, BossNguoiTuyet), sửa luôn |
| 6.7 | Quái alpha | `MapNapTaiNguyen` (spawn, :1187-1265), `Monster` (cờ alpha, HP×4); thông báo bản đồ |
| 6.8 | Họ quái theo vùng sinh thái | `thiet_ke_map.csv` (đội map) + `map/pokemon_spawn/` |
| 6.9 | Client (nhóm hiệu ứng) | đọc -95..-99; **số lệnh phải grep lại** cả `src/` và `ClientUnity/` (hiện -90..-94 đã dùng) và **thống nhất với nhóm hiệu ứng/âm thanh** trước khi chốt |

### Bước 7: Cuốn hút

| # | Việc | Vị trí |
|---|---|---|
| 7.1 | Lịch đăng nhập 7 ngày | `CharCapNhat.java:559` (đổi ngày) |
| 7.2 | Thang PvP mùa (Elo, bậc Poké → Master, ghép trận theo dải 10 cấp) | `MapLienDau`, `tob_char_thach_dau` (cột `pointrank` đã có) |
| 7.3 | Shiny + pity | `tob_pokedex` thêm cột `so_shiny`, `dem_pity`; `Pokedex.java` |
| 7.4 | Hầm ngục bí ẩn | map mới (đội map), hạt giống theo ngày; bảng `tob_ham_nguc` |
| 7.5 | Sư phạm, bạn bè +5% | `tob_friendlist`, `Char` |
| 7.6 | Thời tiết theo ngày, bùng phát, huyền thoại lang thang | `BaoTriDaily` (hạt giống ngày), spawn |

## Rủi ro chính

| Rủi ro | Giảm thiểu |
|---|---|
| Đổi bảng EXP làm nhân vật cũ nhảy cấp | Giữ cấp, đặt lại exp; sao lưu `tob_char`; chỉ áp khi mở máy chủ mới hoặc mùa mới |
| Sửa `Map.random()` đổi hành vi ở hàng trăm chỗ | Bước riêng; chạy toàn bộ test + 50 bot (`tools/pokemon/kiem_tai`) |
| Bỏ sót bản sao cộng EXP / đánh | Gom vào 1 hàm, `grep -c` trước và sau (§3) |
| Gói -95..-99 trùng với việc client khác đang làm | Đăng ký số trong `KE_HOACH_CHUYEN_DOI.md` mục 7.3 trước khi code |
| Danh sách nhiệm vụ > 127 làm hỏng client | Bước 4.2 phải xong **trước** 4.3 |
| `ItemQuest` trong jar | Đè class phải giữ đúng chữ ký; test `javap` |
| Thay đổi số liệu tác động qua `cau_hinh` | Mọi khoá mới có giá trị mặc định = hành vi cũ khi `sv.lau_dai=0` |
| Mô phỏng ≠ thực tế | Bảng `tob_kinh_te_ngay` + ngưỡng đỏ (THIET_KE mục 2.5) từ ngày đầu; chỉnh qua `cau_hinh` |

## Tiêu chí "xong" cho cả giai đoạn

- `sv.lau_dai=0`: test cũ y nguyên (65 lỗi cũ, không tăng).
- `sv.lau_dai=1`: các test mới đạt.
- 50 bot × 1 giờ, không exception.
- Mô phỏng chạy lại với số **đọc từ DB đã áp** (không phải từ script) vẫn 12/12.
- 7 ngày chạy thử: cung tiền thực ≤ 10 giờ; ra/vào xu 90–105%.
