import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loai tien cua MOI vat pham dang ban (chu game 31/10/2026: "phải đúng là bán xu hay lượng hay lượng khoá ... rõ ràng").
 *
 * <ul>
 * <li>Cua hang dac biet (lenh 85): dong nao may chu tru LUONG KHOA (TienShopDacBiet.traLuongKhoa) thi bang gia
 * data_shop_gia_lau_dai phai ghi tien_moi = 2; moi dong dang ban (data_shop.sell = 1) phai co dong gia; dong luong khoa
 * gia &gt; 0.</li>
 * <li>Gioi han tuan (Ve gio vang, Keo tiem nang..., dong Kecleon co gioi han): mua THAT BAI (khong tru tien) thi tra lai
 * luot - truoc day dem luot TRUOC khi tru tien.</li>
 * <li>sv.lau_dai: luong khong mua do / nguyen lieu / da (thiet-ke-game 9.1, 9.5): Persian (dong gia luong), NPC 24 (trang
 * bi chi so 214..263), Shop Dac Biet VIP ngung ban.</li>
 * <li>Doi he do (NPC -15): nhan menu ghi dung loai tien that tru.</li>
 * </ul>
 */
public final class KiemLoaiTienCuaHang {

    private KiemLoaiTienCuaHang() {
    }

    public static void chay() {
        Kiem.nhom("Loai tien cua hang (xu / luong / luong khoa)");
        bangGiaShopDacBiet();
        hoanLuotTuan();
        luongKhongMuaSucManh();
        nhanDoiHe();
    }

    static void bangGiaShopDacBiet() {
        final Map<Integer, Map<String, String>> gia = new HashMap<Integer, Map<String, String>>();
        for (final Map<String, String> r : DayDu.dong("data_shop_gia_lau_dai")) {
            gia.put(Integer.valueOf(r.get("id_shop")), r);
        }
        Kiem.dung("data_shop_gia_lau_dai co du lieu", !gia.isEmpty());
        final List<Map<String, String>> shop = DayDu.dong("data_shop");
        Kiem.dung("data_shop co du lieu", !shop.isEmpty());
        final StringBuilder thieuGia = new StringBuilder();
        for (final Map<String, String> r : shop) {
            if ("1".equals(r.get("sell")) && !gia.containsKey(Integer.valueOf(r.get("id")))) {
                thieuGia.append(r.get("id")).append(' ');
            }
        }
        Kiem.bang("moi dong data_shop dang ban co dong gia (loai tien ro rang)", "", thieuGia.toString().trim());

        final StringBuilder sai = new StringBuilder();
        final StringBuilder giaKhong = new StringBuilder();
        for (final Map.Entry<Integer, Map<String, String>> e : gia.entrySet()) {
            final int id = e.getKey().intValue();
            final int tienMoi = Integer.parseInt(e.getValue().get("tien_moi"));
            if (real.TienShopDacBiet.laDongKhoa(id) && tienMoi != 2) {
                sai.append(id).append(' ');
            }
            if (tienMoi == 2 && Long.parseLong(e.getValue().get("gia_moi")) <= 0) {
                giaKhong.append(id).append(' ');
            }
        }
        Kiem.bang("dong may chu tru luong khoa (laDongKhoa 116..128/150/176) ghi tien_moi = 2", "", sai.toString().trim());
        Kiem.bang("dong luong khoa co gia > 0", "", giaKhong.toString().trim());
        Kiem.dung("phan B nang cap sua tien_moi 116..125 cho CSDL dang chay",
                DayDu.nangCap().contains("UPDATE `data_shop_gia_lau_dai` SET `tien_moi` = 2")
                        && DayDu.nangCap().contains("`id_shop` BETWEEN 116 AND 125"));
    }

    static void hoanLuotTuan() {
        Kiem.dung("khong loai tien nao giam -> hoan luot", real.TienShopDacBiet.khongTruTien(30, 30, 5, 5, 100, 100));
        Kiem.dung("tien tang (khong tru) -> hoan luot", real.TienShopDacBiet.khongTruTien(30, 31, 5, 5, 100, 100));
        Kiem.dung("tru luong khoa -> KHONG hoan", !real.TienShopDacBiet.khongTruTien(30, 0, 5, 5, 100, 100));
        Kiem.dung("tru luong -> KHONG hoan", !real.TienShopDacBiet.khongTruTien(30, 30, 5, 4, 100, 100));
        Kiem.dung("tru xu -> KHONG hoan", !real.TienShopDacBiet.khongTruTien(30, 30, 5, 5, 100, 1));
        final String mua = DayDu.doc("src/real/doBuySpecialItem.java");
        Kiem.dung("lenh 85 hoan luot tuan khi khong tru tien", mua.contains("LUOT_TUAN.set(")
                && mua.contains("TienShopDacBiet.khongTruTien(") && mua.contains("DatabaseLauDai.hoanMuaTuan("));
        final String ch = DayDu.doc("src/real/CuaHangLauDai.java");
        final int iKhongDu = ch.indexOf("if (!ok) {");
        Kiem.dung("Cua hang lau dai: thieu tien -> hoan luot tuan",
                iKhongDu > 0 && ch.indexOf("DatabaseLauDai.hoanMuaTuan(", iKhongDu) > iKhongDu
                        && ch.indexOf("DatabaseLauDai.hoanMuaTuan(", iKhongDu) < ch.indexOf("return \"Không đủ: \"", iKhongDu));
        Kiem.dung("hoanMuaTuan khong xuong duoi 0", DayDu.doc("src/data/DatabaseLauDai.java")
                .contains("SET so_luong = so_luong - 1 WHERE id_char = ? AND id_shop = ? AND tuan = ? AND so_luong > 0"));
    }

    static void luongKhongMuaSucManh() {
        Kiem.dung("Persian: dong gia luong ngung ban khi lau dai", !real.GemTemplate.banDuoc(1, 1, true));
        Kiem.dung("Persian: dong gia xu van ban khi lau dai", real.GemTemplate.banDuoc(1, 0, true));
        Kiem.dung("Persian: tat lau dai giu nhu cu", real.GemTemplate.banDuoc(1, 1, false));
        Kiem.dung("Persian: sell = 0 khong ban", !real.GemTemplate.banDuoc(0, 0, false));
        Kiem.dung("goi 25 gui sell theo banDuoc", DayDu.doc("src/real/MessageCreator.java")
                .contains("Map.gemTemplate[l].banDuoc() ? Map.gemTemplate[l].sell : 0"));
        Kiem.dung("lenh 74 bo qua dong ngung ban", DayDu.doc("src/real/OfflineMap.java").contains("if (!g.banDuoc()) {"));

        for (int id = 214; id <= 263; id++) {
            if (!real.OfflineMap.doChiSoBangLuongNgungBan(id, true)) {
                Kiem.dung("NPC 24: trang bi " + id + " (luong) ngung ban khi lau dai", false);
            }
        }
        Kiem.dung("NPC 24: 214..263 ngung ban khi lau dai", real.OfflineMap.doChiSoBangLuongNgungBan(214, true)
                && real.OfflineMap.doChiSoBangLuongNgungBan(263, true));
        Kiem.dung("giap xu 213 / cuoc 465 / the mua ban 675 khong bi chan", !real.OfflineMap.doChiSoBangLuongNgungBan(213, true)
                && !real.OfflineMap.doChiSoBangLuongNgungBan(465, true) && !real.OfflineMap.doChiSoBangLuongNgungBan(675, true));
        Kiem.dung("NPC 24: tat lau dai giu nhu cu", !real.OfflineMap.doChiSoBangLuongNgungBan(214, false));

        Kiem.dung("Shop Dac Biet VIP ngung ban khi lau dai", real.Map.shopVipNgungBan(true) && !real.Map.shopVipNgungBan(false));
        final String menu = DayDu.doc("src/real/MapDoMenu.java");
        Kiem.dung("menu Shop Dac Biet VIP bao ngung ban", menu.contains("Map.shopVipNgungBan(com.htbb.pokemon.LauDai.bat())"));
    }

    static void nhanDoiHe() {
        Kiem.dung("doi he: nhan ghi 'lượng khoá' khi chon luong khoa", DayDu.doc("src/real/MapDoMenu.java")
                .contains("(idOptionMenu == 1 ? \" lượng khoá)\" : \" lượng)\")"));
    }
}
