# Sửa lỗi bộ test server (`test/Kiem.java`): 64 → 0

Ngày 18/10/2026, nhánh `pokemon/chuyen-doi`. Người dùng báo "bộ test server có 65 lỗi". Lần chạy
đầu đếm được **64/8565** kiểm tra hỏng, nằm trong 13 nhóm (con số 65 còn tính thêm một lỗi thoáng
qua do agent khác đang sửa). Sau khi sửa: **`DAT: 8579/8579`**, không còn lỗi nào. Tổng số kiểm tra
tăng thêm 14 vì có thêm phép kiểm cho hành vi mới, không phép kiểm nào bị xoá cho qua.

Cách chạy (không đụng `build/classes`, thư mục mà server đang chạy thật dùng):

```
L="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:lib/htbb-core.jar"
javac --release 8 -encoding UTF-8 -cp "$L" -sourcepath src -d <thu_muc_rieng>/src $(find src -name '*.java')
javac --release 8 -encoding UTF-8 -cp "<thu_muc_rieng>/src:$L" -d <thu_muc_rieng>/test $(find test -name '*.java')
java -Dfile.encoding=UTF-8 -cp "<thu_muc_rieng>/test:<thu_muc_rieng>/src:$L" Kiem
```

## Phân loại

| Loại | Nhóm | Số kiểm tra |
|---|---|---|
| Lỗi thật trong `src` → sửa `src` | Nhánh hoa đào giftcode | 6 |
| Hành vi đã đổi có chủ ý (theo commit hoặc chú thích ghi ngày) → cập nhật test cho đúng hành vi mới | 11 nhóm còn lại | 57 |
| Tệp sinh ra bị cũ → sinh lại | Danh mục thú (`web/admin/inc/danh_muc_thu.php`) | 1 (cộng 2 kiểm tra test ở cùng nhóm) |
| Client lệch server → đồng bộ bản sao | `htbb-client/j2me` `res/pk/chu.bin` (không có trong 64 lỗi trên, do `KiemPokeDuLieu` bắt) | 5 |

## Bảng chi tiết

| # | Nhóm / kiểm tra hỏng | Nguyên nhân gốc | Cách sửa | Bằng chứng |
|---|---|---|---|---|
| 1 | **Nhánh hoa đào giftcode** – `true damage 24.1%` (241 → thực tế 80), `true damage chance 29.0%` (290 → 100), ×3 bản sao = 6 | **Lỗi thật (hồi quy).** Commit `ffcb3ed` (20–21/09) cho `Item.createAttributeVuKhiThoiTrangMax()` co giãn trần theo **chục level của trang bị** (`tranSTCTiLeTheoLevel(this.level)`). Nhánh hoa đào (835) chỉ có một template level 10, nên bị kéo về sàn 6x (8%/10%), trong khi đặc tả ghi tại `Char.createItemSoLuong` vẫn là "24.1% ST chuẩn, 50.1% vũ khí, 29.0% tỷ lệ". Thay đổi đó nhắm vào vũ khí thời trang tinh luyện 6x/7x/8x. Chính Nguyệt Linh Trượng (cũng chỉ một template level 10) đã phải được sửa riêng để lấy level người chơi. | Thêm `Item.createAttributeNhanhHoaDaoGiftcode()`: gọi bản Max rồi đặt `otherAtt[54]=TRAN_STC_CU` (241) và `[56]=TRAN_TILE_CU` (290); `[55]` vẫn là 501. Đổi 2 chỗ gọi cho 835 trong `Char.createItemSoLuong` sang hàm này. Không sửa test. | `src/real/Char.java` (chú thích của nhánh 835), `src/real/Item.java` (`TRAN_STC_CU`/`TRAN_TILE_CU`), `docs/danh_muc_item.md:854` (835 level 10). **Cần người dùng xác nhận:** nếu muốn nhánh hoa đào cũng co giãn theo level thì đổi lại 2 dòng gọi đó và cập nhật `test/KiemNhanhHoaDao`. |
| 2 | **Skin linh thú** – `charDBID chuyen sang tham so 30` | Test cũ. Ngày 21/09, `saveChar` có thêm 3 cột `kho_xu`/`kho_luong`/`kho_luong_khoa` nên `charDBID` chuyển sang tham số 33. Bản thân code đúng: có 33 dấu `?` và `setInt(33, p.charDBID)`. | Test không còn chốt cứng một con số mà đếm số `?` trong câu UPDATE của `saveChar` và đòi `charDBID` nằm đúng ở `?` cuối (hàm `KiemTatCaiTrang.chiSoCharDBIDSaveChar`, dùng chung). Lần sau thêm cột thì test không phải sửa nữa, mà vẫn bắt được lỗi lệch chỉ số. | `Database.themCotKhoXuLuong()` (chú thích "them 21/09/2026"), `src/data/DatabaseNhanVat.java` saveChar |
| 3 | **Tắt cải trang** – `tham so 30`, `khong con o tham so 27`, `cot moi mac dinh 0` | (a) Cùng nguyên nhân với #2. (b) Kiểm tra "không có `setInt(27, p.charDBID)`" quét cả tệp, mà `saveCharAuto` có câu UPDATE ngắn hơn và dùng tham số 27 là **đúng**. (c) Ngày 21/09, `themCotNeuThieu` được tách thành `themCotNeuThieu(tenCot, kieuCot, moTa)` để dùng chung cho `bigint` của `kho_xu`. Câu ALTER giờ ghép chuỗi `" " + kieuCot + " NOT NULL DEFAULT 0"` nên chuỗi liền `int(11) NOT NULL DEFAULT 0` không còn. Mặc định vẫn là 0. | (a, b) Kiểm tra theo thân `saveChar`: `charDBID` ở `?` cuối và chỉ được đặt đúng 1 lần. (c) Kiểm tra các cột công tắc đi qua `themCotNeuThieu(tenCot, "int(11)", moTa)` và nhánh ALTER của `tob_char` có `" NOT NULL DEFAULT 0");`. | `src/data/Database.java` `themCotNeuThieu` / `themCotKhoXuLuong` |
| 4 | **Ảo hoá Hoả Kỳ Lân** – `gia 100 luong`, `dat moc het han`, `ghi log aohoahkl`, `dat moc TRUOC doTrangBi` | Giá đổi có chủ ý ngày 21/09 (chú thích trên hằng số: "truoc chi mat 100 luong, gio them 10 hoa thach C2"): `GIA_LUONG_AO_HOA_HOA_KY_LAN = 5000` và `SO_HOA_THACH_C2_... = 10`. Bước kiểm và trừ hoả thạch làm thân hàm dài hơn cửa sổ cố định 2300 ký tự của test, nên 3 kiểm tra kia (code vẫn đúng) rơi ra ngoài cửa sổ. | Test: giá 5000 lượng + 10 hoả thạch C2. Cửa sổ quét giờ chạy đến hết hàm (tới `checkExpireAoHoaHoaKyLan`). Thêm kiểm tra "kiểm đủ hoả thạch TRƯỚC khi trừ lượng". | `src/real/Char.java` `doAoHoaHoaKyLan` (committed ở HEAD với giá 5000) |
| 5 | **Danh mục thú** – `dung 30 con thu`, `so dong khop`, `khong chi so nao thieu` | Ngày 19/09 thêm mẫu pet 30 "Capybara" (Trung Thu 2026, theo yêu cầu người dùng, chú thích trong `Map.loadPetTemplate`) nhưng chưa sinh lại `web/admin/inc/danh_muc_thu.php`. | Chạy lại `tools/SinhDanhMucThu` (cho ra 31 thú, 0..30). Test chuyển sang 31. | `src/real/Map.java` `loadPetTemplate` (mẫu 30) |
| 6 | **Mẫu pet có ảnh** – `nap duoc Map.petTemplate (30 mau)` | Cùng nguyên nhân với #5. | Đổi thành 31. Thêm cặp `{30, 9}` (Capybara mượn ảnh mẫu 9) vào `CAP_MUON` để kiểm bộ ảnh/khung/kiểu di chuyển, đúng như hướng dẫn trong chú thích tại `Map.java`. | như #5 |
| 7 | **Lệnh web** – `vong doc lay ca CHO va CHO_DANG_NHAP` | Commit `a8c6644` (phân trang hàng đợi, "khong de 50 lenh offline dau tien chan ca hang doi") thêm điều kiện con trỏ `AND id > ?` vào giữa `IN (?, ?)` và `ORDER BY`. | Test khớp câu SQL mới và kiểm thêm `setInt(1, CHO)` / `setInt(2, CHO_DANG_NHAP)`. | `src/data/LenhWeb.java` `docLenhCho` |
| 8 | **Sự kiện** – 18 kiểm tra: `ma tatca/tet2016/demtrungthu/trungthu2015/longdenhoasen/snngudequa co trong danh muc` và `isFinishAllSuKien`, `isSuKienTet2016`, `isDemTrungThu`, `isBanLongDenHoaSen`, `isSinhNhatNgude`, `canReceiveGiftSNNgude`… `khi bat` | Commit `ba7df7b` (21/09, người dùng yêu cầu "xoá sự kiện trùng lặp + xoá hết Trung Thu, chỉ giữ bản đã sửa") cố ý bỏ 6 mã khỏi `SuKien.DANH_MUC`. Lý do từng mã ghi ở chú thích trên `DANH_MUC`, kèm `sql/xoa_su_kien_trung_lap.sql`. `SuKien.dat()` bỏ qua mã không có trong danh mục, nên bật/tắt các mã này không còn tác dụng. | Tách 9 hàm sang bảng mới `DA_BO_21_09` trong test. Với mỗi hàm: kiểm mã **không** còn trong danh mục, hàm vẫn gọi được, và bật/tắt cổng cho cùng một kết quả. Phần "không sót hàm nào" vẫn tính cả hai bảng. | `src/real/SuKien.java` (chú thích trên `DANH_MUC`) |
| 9 | **Hoá hình Pet** – 11 kiểm tra `case 12: …` | Ngày 19/09, menu NPC -8 chèn "Đổi tên nhân vật" ở vị trí 8 (chú thích trong `RequestNPCInfoHandler` case -8). Mọi mục từ đó trở xuống dịch 1 bậc: Hoá hình Pet chuyển từ `case 12` sang `case 13` trong `RealController.doCreateItem`, còn `case 12` giờ là Bộ Sưu Tập. Logic không đổi gì. | Test lấy đoạn `case 13:`…`case 14:`, đổi nhãn các phép kiểm sang "case 13". | `src/real/cmd/RequestNPCInfoHandler.java` case -8, `src/real/RealController.java` ~2005 |
| 10 | **Tinh Luyện Thần Binh** – 3 kiểm tra ở RealController | Cùng lý do dịch menu như #9: Tinh Luyện chuyển từ `case 8` sang `case 9`. Test lấy `indexOf("case 8:")` nên trúng nhánh Đổi tên. | Lấy đoạn `case 9:`…`case 10:`. | như #9 |
| 11 | **Tinh Luyện Thần Binh** – 4 kiểm tra công dồn khi đạt 100% (`atb[] … att55, Short.MAX_VALUE`, `att55, Byte.MAX_VALUE`, `otherAtt[55] = att55`, `otherAtt[56] = att56`) | Đổi có chủ ý ngày 20/09, chỉnh lại 21/09 ("người dùng xác nhận làm đúng theo nội dung đã gửi", chú thích "Cong don VKTB" trong `Map.java`). Chỉ số mới = (chỉ số hiện có của thần binh + chỉ số vũ khí chế), nhân ưu đãi theo **chục level** của trang bị (phần nghìn = level), không nhân theo att55 nữa. `[55]`/`[56]` cũng công dồn và nhân theo level; chỉ `[54]` giữ nguyên. | Test khớp công thức mới: `nhanTheoChiSoVuKhiTang(tongAtb, phanNghinTheoLevel, Short.MAX_VALUE)`, newAtb/lockAtb với `Byte.MAX_VALUE`, và `[55]`/`[56]` = công dồn rồi nhân theo level. Giữ kiểm tra `[54] = att54`. Thêm kiểm tra "KHÔNG còn dùng att55 làm hệ số nhân", để chặn lại đúng lỗi ăn hai lần ngày 05/09. | `src/real/Map.java` ~9780–9870 |
| 12 | **Cải Tạo Cải Trang** – `case 10 mo thang danh sach` | Cùng lý do dịch menu như #9: chuyển từ case 10 sang case 11. | Lấy đoạn `// cải tạo cải trang\n case 11:`…`case 12:`. | như #9 |
| 13 | **Nâng cấp vũ khí lv86** – 4 kiểm tra `tim thay doUserUpLevelItem[Special]` | Commit `4645536` đổi hai hàm từ `void` sang `boolean`, để `MapCheTao` chỉ trừ 5000 xu khi hàm trả về true (sửa lỗi mất xu oan). Test tìm theo chữ ký `void …`. | Tìm theo `boolean doUserUpLevelItem…`. | `src/real/Map.java:6563/6703`, `src/real/MapCheTao.java:445` |
| 14 | **Công thành** – 4 kiểm tra `<70` / thông báo '70' | Ngày 22/09 người dùng hạ ngưỡng **vào thành** từ 70 xuống 60: commit `5fde7ad` (MapXuLyLenh) và `d280070` (MapLienDau.doChangeMap, Char.kickOutTown). Ngưỡng **giao thẻ** (`MapTown.playerGiveCard`) không đổi, vẫn 70. | Test: 3 chỗ vào thành là `<60` và thông báo "60". Kiểm tra giao thẻ vẫn giữ `<70`. Đổi tên nhóm và chú thích đầu tệp cho đúng. | `git show 5fde7ad`, `git show d280070` |
| 15 | *(ngoài 64)* **j2me `KiemPokeDuLieu`**: `ChuPokemon khac server` ×5 | Một agent khác (kiểm item mồ côi, 17:23 hôm nay) đổi `huyết linh thảo` → "Kẹo thú cưng" (để không trùng "Kẹo hiếm" là kẹo EXP 1214) ở server `ChuPokemon.java` và cả hai `ChuPokemonDuLieu.cs` (ClientUnity và pc), nhưng chưa sinh lại từ điển của bản jar `res/pk/chu.bin`. | Sinh lại **chỉ** `chu.bin` bằng chính `DongGoiPokeJ2me.ghiTuDien(tepTuDien(../pc/Assets))`. So bản giải mã cũ và mới thì chỉ khác đúng 1 dòng. Chép vào `res/pk/` và `res_nhe/pk/` (hai tệp vốn giống hệt nhau). | `KiemPokeDuLieu`: `ChuPokemon: 3461 chuoi, khac ban server: 0` … `OK` |

## Tệp đã đổi

- `src/real/Item.java`: thêm `createAttributeNhanhHoaDaoGiftcode()`.
- `src/real/Char.java`: 2 chỗ gọi cho template 835.
- `web/admin/inc/danh_muc_thu.php`: sinh lại (31 thú).
- `htbb-client/j2me/res/pk/chu.bin`, `htbb-client/j2me/res_nhe/pk/chu.bin`: sinh lại từ điển.
- Test: `KiemTatCaiTrang`, `KiemSkinThuCuoi`, `KiemAoHoaPonyta`, `KiemDanhMucThu`, `KiemMauPetCoAnh`,
  `KiemLenhWeb`, `KiemSuKien`, `KiemHoaHinhPet`, `KiemTinhLuyenBaoVat`, `KiemCaiTaoCaiTrang`,
  `KiemNangCapDoCamTayLv86`, `KiemChiemGymDoiDiemGiaoHang`.

Không commit git, không khởi động lại server, không đụng `build/classes`.
