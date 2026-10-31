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
 * <li>Chu game 31/10/2026: luong (nap) la tien TOT NHAT - Persian / NPC 24 / Shop VIP van ban bang luong; moi thu gia
 * luong khoa (shop dac biet, Kecleon, bua nang cap, doi he, hoi sinh) mua duoc bang luong cung gia khi thieu luong khoa,
 * do mua bang luong khong khoa.</li>
 * <li>Menu luong cu: kiem-va-tru nguyen tu (PhiLuong / Char.truNeuDu); Gay Anh Trang chi trong Trung Thu; hop banh dac
 * biet mot cong thuc; Keo thu cung gia 0 khong ban; ao hoa Hoa Ky Lan hien gia.</li>
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
        luongLaTienTotNhat();
        nguyenTu();
        suKien();
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

    /**
     * Chu game 31/10/2026 (dao lai de xuat chan): luong (nap) la tien TOT NHAT - Persian dong gia luong, NPC 24 trang bi
     * chi so, Shop Dac Biet VIP van ban bang luong; dong gia luong khoa mua duoc bang luong cung gia, do khong khoa.
     */
    static void luongLaTienTotNhat() {
        final String offline = DayDu.doc("src/real/OfflineMap.java");
        Kiem.dung("Persian: khong chan dong gia luong", !offline.contains("banDuoc()") && offline.contains("if (g.typeMoney == 1) {"));
        Kiem.dung("NPC 24: trang bi 214..263 van ban bang luong", !offline.contains("doChiSoBangLuongNgungBan")
                && DayDu.doc("src/real/cmd/RequestNPCInfoHandler.java").contains("for (int i = 214; i <= 263; ++i) {"));
        Kiem.dung("Shop Dac Biet VIP van ban", !DayDu.doc("src/real/Map.java").contains("shopVipNgungBan")
                && !DayDu.doc("src/real/MapDoMenu.java").contains("shopVipNgungBan"));
        Kiem.dung("Persian: Da tach 5x-7x mua bang luong -> KHONG khoa",
                offline.contains("if (id != 175 && id != 176 && id != 177 || g.typeMoney == 1) {"));
        Kiem.dung("thiet-ke 9.5: bo muc tieu 'chenh suc manh <= 5%', xu / cay van la duong tien chinh",
                DayDu.doc("../docs/thiet-ke-game.md").contains("xu / cày quái vẫn là đường tiến chính"));
        Kiem.dung("goi 25 gui sell goc cua ngoc", DayDu.doc("src/real/MessageCreator.java").contains("m.dos.writeByte(Map.gemTemplate[l].sell);"));

        // luong khoa truoc, thieu thi luong cung gia, thieu ca hai thi khong tru
        Kiem.bang("du luong khoa -> tra luong khoa", real.Char.TRA_LUONG_KHOA, real.Char.chonTraLuongKhoaRoiLuong(30, 100, 30, 30));
        Kiem.bang("thieu luong khoa, du luong -> tra luong", real.Char.TRA_LUONG, real.Char.chonTraLuongKhoaRoiLuong(29, 30, 30, 30));
        Kiem.bang("thieu ca hai -> khong tru", real.Char.TRA_KHONG, real.Char.chonTraLuongKhoaRoiLuong(29, 29, 30, 30));
        Kiem.bang("gia am -> khong tru", real.Char.TRA_KHONG, real.Char.chonTraLuongKhoaRoiLuong(100, 100, -1, -1));
        final real.Char p = new real.Char(null);
        p.setLuong(100);
        p.lockLuong = 10;
        Kiem.bang("Char.truLuongKhoaRoiLuong: thieu khoa -> tru luong", real.Char.TRA_LUONG, p.truLuongKhoaRoiLuong(30, 30));
        Kiem.bang("  luong con 70", 70L, (long) p.getLuong());
        Kiem.bang("  luong khoa giu 10", 10L, (long) p.getLuongLock());
        Kiem.bang("Char.truLuongKhoaRoiLuong: du khoa -> tru khoa", real.Char.TRA_LUONG_KHOA, p.truLuongKhoaRoiLuong(10, 10));
        Kiem.bang("  luong khoa con 0", 0L, (long) p.getLuongLock());
        Kiem.bang("  luong giu 70", 70L, (long) p.getLuong());
        Kiem.bang("Char.truLuongKhoaRoiLuong: thieu ca hai -> khong doi", real.Char.TRA_KHONG, p.truLuongKhoaRoiLuong(80, 80));
        Kiem.bang("  luong giu 70 (khong tru)", 70L, (long) p.getLuong());

        // Kecleon (Cua hang lau dai): chi phi luong khoa tra bang luong khi thieu, do KHONG khoa
        final java.util.List<long[]> chi = real.CuaHangLauDaiKiem.docChiPhi("luong_khoa:15");
        final real.Char k = new real.Char(null);
        k.setLuong(20);
        k.lockLuong = 5;
        Kiem.dung("Kecleon: thieu luong khoa", !real.CuaHangLauDaiKiem.du(k, chi, false));
        Kiem.dung("Kecleon: du luong", real.CuaHangLauDaiKiem.du(k, chi, true));
        real.CuaHangLauDaiKiem.tru(k, chi, true);
        Kiem.bang("Kecleon: tru 15 luong", 5L, (long) k.getLuong());
        Kiem.bang("Kecleon: luong khoa giu", 5L, (long) k.getLuongLock());
        Kiem.dung("Kecleon: nhan menu ghi 'lượng khoá (hoặc lượng)'",
                DayDu.doc("src/real/CuaHangLauDai.java").contains("b.append(\"lượng khoá (hoặc lượng)\");"));
        Kiem.dung("Kecleon: mua bang luong -> phat(p, thuong, !traLuong) (do khong khoa)",
                DayDu.doc("src/real/CuaHangLauDai.java").contains("phat(p, thuong, !traLuong);"));
        Kiem.dung("createItemSoLuong(..., khoa): ban sao trong vong lap cung khoa voi ban goc",
                DayDu.doc("src/real/Char.java").contains("themItem.lock = newItem.lock;"));

        // Bua nang cap / hoi sinh: luong khoa truoc roi luong
        final String nc = DayDu.doc("src/real/NangCapLauDaiGame.java");
        Kiem.dung("Bua nang cap: thieu luong khoa thi tru luong", nc.contains("p.subLuong(bua);")
                && nc.contains("Char.chonTraLuongKhoaRoiLuong(p.lockLuong, p.getLuong(), bua, bua)"));
        Kiem.bang("Bua nang cap: ghi ro tien that tru", ", bùa trừ 5 lượng", real.NangCapLauDaiGame.ghiBua(5, true));
        Kiem.bang("Bua nang cap: ghi ro tien that tru (khoa)", ", bùa trừ 5 lượng khoá", real.NangCapLauDaiGame.ghiBua(5, false));
        Kiem.dung("Hoi sinh: tra luong khoa hoac luong", DayDu.doc("src/real/HoiSinhGame.java").contains("p.truLuongKhoaRoiLuong(pl, pl)"));
    }

    /** Menu luong cu (thu cuoi, cong hien, thoi trang, tinh luyen, than binh, ao hoa): kiem-va-tru nguyen tu. */
    static void nguyenTu() {
        final real.Char p = new real.Char(null);
        p.setLuong(50);
        final int[] lam = {0};
        Kiem.bang("PhiLuong.lamRoiTru: du + viec thanh cong -> XONG", real.PhiLuong.XONG,
                real.PhiLuong.lamRoiTru(p, 20, () -> { lam[0]++; return true; }));
        Kiem.bang("  tru 20", 30L, (long) p.getLuong());
        Kiem.bang("PhiLuong.lamRoiTru: viec that bai -> khong tru", real.PhiLuong.KHONG_LAM_DUOC,
                real.PhiLuong.lamRoiTru(p, 20, () -> false));
        Kiem.bang("  giu 30", 30L, (long) p.getLuong());
        Kiem.bang("PhiLuong.lamRoiTru: thieu -> khong lam viec", real.PhiLuong.THIEU,
                real.PhiLuong.lamRoiTru(p, 31, () -> { lam[0] += 100; return true; }));
        Kiem.bang("  viec chi chay 1 lan", 1, lam[0]);
        Kiem.dung("PhiLuong.tru: thieu -> false", !real.PhiLuong.tru(p, 31));
        Kiem.dung("PhiLuong.tru: am -> false", !real.PhiLuong.tru(p, -1));

        final String menu = DayDu.doc("src/real/MapDoMenu.java");
        Kiem.dung("thu cuoi: khong con 'getLuong() < Animal.price'", !menu.contains("player.getLuong() < Animal.price"));
        Kiem.dung("thu cuoi: khong con subLuong rieng (200 khang)", !menu.contains("player.subLuong(200L);"));
        Kiem.dung("thu cuoi: muc Tiem nang hien price[1] (gia that tru)", menu.contains("\"Tiềm năng(\" + Animal.price[1]"));
        Kiem.dung("cong hien: tru nguyen tu", menu.contains("if (!PhiLuong.tru(player, 100L))"));
        final String map = DayDu.doc("src/real/Map.java");
        Kiem.dung("nang cap thoi trang: tru trong truNeuDu", map.contains("player.getLuong() >= requiredGold && nguyenLieuNc.otherAtt[65] >= requiredFragments"));
        Kiem.dung("tinh luyen: 1 luong + 3tr xu trong truNeuDu", map.contains("player.getLuong() >= 1 && player.getxu() >= 3000000L"));
        Kiem.dung("than binh: tru truoc khi gia han", map.contains("if (!PhiLuong.tru(player, giaLuong)) {"));
        Kiem.dung("ao hoa Hoa Ky Lan: tru nguyen tu", DayDu.doc("src/real/Char.java").contains("if (!this.truLuongNeuDu((long) GIA_LUONG_AO_HOA_HOA_KY_LAN)) {"));
        Kiem.bang("ao hoa Hoa Ky Lan: menu hien gia", "ảo hóa (5000 lượng + 10 nham thạch C2)", real.Char.mucMenuAoHoaHoaKyLan());
        Kiem.dung("menu Hoa Ky Lan dung mucMenuAoHoaHoaKyLan", map.contains("Char.mucMenuAoHoaHoaKyLan()"));
    }

    /** Su kien: Gay Anh Trang chi trong Trung Thu; hop banh dac biet mot cong thuc 5+5, khong doi hai lan. */
    static void suKien() {
        final String rc = DayDu.doc("src/real/RealController.java");
        Kiem.dung("Gay Anh Trang / nen: kiem su kien phia may chu", rc.contains("(idOptionMenu == 3 || idOptionMenu == 4) && !denTrungThuDangMo()")
                && rc.contains("idActor == -65 && !denTrungThuDangMo()"));
        Kiem.bang("hop banh: 5 banh moi loai", 5, real.Map.BANH_MOI_HOP_DAC_BIET);
        Kiem.bang("hop banh: 5 luong", 5, real.Map.LUONG_MOI_HOP_DAC_BIET);
        final String map = DayDu.doc("src/real/Map.java");
        final int h = map.indexOf("public void doProcessInputTextHalloWeen2016(");
        final int h2 = map.indexOf("} else if (idActor == -32017) {", h);
        Kiem.dung("Halloween: khong con ban sao hop banh Trung Thu (-32018)", h > 0 && h2 > h
                && !map.substring(h, h2).contains("potions[134]"));
        Kiem.dung("MapNhapText dung chung cong thuc", DayDu.doc("src/real/MapNhapText.java").contains("Map.doiHopBanhDacBiet(player, sl)"));
        Kiem.dung("Keo thu cung gia 0: khong ban", !real.doBuySpecialItemKiem.banKeoThuCung(true, 0));
        Kiem.dung("Keo thu cung het ky: khong ban", !real.doBuySpecialItemKiem.banKeoThuCung(false, 100));
        Kiem.dung("Keo thu cung co gia + dang ky: ban (tru xu)", real.doBuySpecialItemKiem.banKeoThuCung(true, 100));
    }

    static void nhanDoiHe() {
        final String menu = DayDu.doc("src/real/MapDoMenu.java");
        Kiem.dung("doi he: nhan ghi '200 lượng khoá / 100 lượng' khi chon luong khoa",
                menu.contains("(idOptionMenu == 1 ? \" (200 lượng khoá / 100 lượng)\" : \" (100 lượng)\")"));
        Kiem.dung("doi he: menu co ca hai lua chon", menu.contains("new String[]{\"Dùng lượng (100)\", \"Dùng lượng khoá (200)\"}"));
        Kiem.dung("doi he: tru nguyen tu, luong khoa thieu thi luong", menu.contains("player.truLuongKhoaRoiLuong(giaLock, gia)"));
        Kiem.dung("doi he bang luong khoa -> do khoa", menu.contains("it.khoaVinhVien(); // doi bang luong khoa"));
    }
}
