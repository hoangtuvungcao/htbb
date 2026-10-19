import com.htbb.pokemon.NapTienLogic;
import com.htbb.pokemon.ThanhVien;
import com.htbb.pokemon.ThanhVienLogic;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import real.Char;

/**
 * Thành viên HTBB (docs/van-hanh.md): cấu hình cau_hinh, quyết định coQuyen, hạn / gia hạn /
 * thu hồi, cổng ThanhVien.chan (tắt = không chặn), mọi móc nối có mặt ở đúng file game, SQL mặc định TẮT,
 * hằng số khớp web (thanhVienThuan.ts).
 */
public final class KiemThanhVien {

    private KiemThanhVien() {
    }

    public static void chay() {
        final ThanhVienLogic.CauHinh cu = ThanhVien.cauHinh();
        try {
            cauHinh();
            quyetDinh();
            han();
            cong();
            kichHoat();
            mocNoi();
            sqlVaWeb();
        } finally {
            ThanhVien.datThu(cu);
        }
    }

    static ThanhVienLogic.CauHinh ch(final String... kv) {
        final Properties p = new Properties();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            p.setProperty(kv[i], kv[i + 1]);
        }
        return ThanhVienLogic.docCauHinh(p);
    }

    static void cauHinh() {
        Kiem.nhom("Thanh vien - cau hinh cau_hinh (mac dinh TAT)");
        final ThanhVienLogic.CauHinh t = ThanhVienLogic.TAT;
        Kiem.sai("thieu dong: tat", t.bat);
        Kiem.sai("thieu dong: khong ban", t.banBat);
        Kiem.bang("gia mac dinh 20.000", 20000, t.gia);
        Kiem.bang("han mac dinh 0 = vinh vien", 0, t.thoiHanNgay);
        Kiem.dung("chuc nang mac dinh co giao_dich + chat_the_gioi", t.chucNang.contains("giao_dich") && t.chucNang.contains("chat_the_gioi"));
        final ThanhVienLogic.CauHinh c = ch(ThanhVienLogic.K_BAT, "1", ThanhVienLogic.K_CHUC_NANG, "giao_dich, cho;hack_moi ,CHAT_THE_GIOI",
                ThanhVienLogic.K_GIA, "abc", ThanhVienLogic.K_MIEN_CAP, "10", ThanhVienLogic.K_THOI_HAN, "-5");
        Kiem.dung("bat = 1", c.bat);
        Kiem.bang("ma la bi bo, hoa thuong khong quan trong", "[giao_dich, cho, chat_the_gioi]", c.chucNang.toString());
        Kiem.bang("gia sai -> mac dinh", 20000, c.gia);
        Kiem.bang("han am -> 0", 0, c.thoiHanNgay);
        Kiem.bang("mien cap 10", 10, c.mienCap);
        Kiem.dung("so dang ky >= 11 tinh nang", ThanhVienLogic.CHUC_NANG.length >= 11);
        boolean duMa = true;
        for (final String m : new String[]{"giao_dich", "chat_the_gioi", "cho", "cho_sinh_thai", "chuyen_luong", "bang", "pvp_xep_hang",
            "giftcode", "qua_online", "gui_thu", "nhan_tang"}) {
            duMa &= ThanhVienLogic.laMaHopLe(m);
        }
        Kiem.dung("du 11 ma yeu cau", duMa);
    }

    static void quyetDinh() {
        Kiem.nhom("Thanh vien - coQuyen");
        final ThanhVienLogic.CauHinh tat = ThanhVienLogic.TAT;
        Kiem.dung("tat he thong: khong thanh vien van duoc", ThanhVienLogic.coQuyen(tat, "giao_dich", false, 50));
        final ThanhVienLogic.CauHinh c = ch(ThanhVienLogic.K_BAT, "1", ThanhVienLogic.K_CHUC_NANG, "giao_dich,chat_the_gioi",
                ThanhVienLogic.K_MIEN_CAP, "10");
        Kiem.sai("bat + khoa + khong thanh vien: chan", ThanhVienLogic.coQuyen(c, "giao_dich", false, 30));
        Kiem.dung("bat + khoa + thanh vien: duoc", ThanhVienLogic.coQuyen(c, "giao_dich", true, 30));
        Kiem.dung("tinh nang khong khoa: duoc", ThanhVienLogic.coQuyen(c, "bang", false, 30));
        Kiem.dung("duoi mien_cap (cap 9 < 10): duoc", ThanhVienLogic.coQuyen(c, "giao_dich", false, 9));
        Kiem.sai("dung mien_cap (cap 10): chan", ThanhVienLogic.coQuyen(c, "giao_dich", false, 10));
        Kiem.dung("thong bao co ten tinh nang + duong dan + gia",
                ThanhVienLogic.thongBao(c, "chat_the_gioi", "https://htbb.vn/").contains("Chat thế giới")
                        && ThanhVienLogic.thongBao(c, "chat_the_gioi", "https://htbb.vn/").contains("https://htbb.vn/thanh-vien")
                        && ThanhVienLogic.thongBao(c, "chat_the_gioi", "https://htbb.vn/").contains("20.000 đ"));
        final ThanhVienLogic.CauHinh tb = ch(ThanhVienLogic.K_BAT, "1", ThanhVienLogic.K_THONG_BAO, "Can {ten} - {web}");
        Kiem.bang("thong bao tuy bien", "Can Giftcode - trang web/thanh-vien", ThanhVienLogic.thongBao(tb, "giftcode", ""));
    }

    static void han() {
        Kiem.nhom("Thanh vien - han, gia han, thu hoi");
        final long t = 1000000000000L, d = ThanhVienLogic.NGAY_MS;
        Kiem.dung("vinh vien (0) con han", ThanhVienLogic.conHan(0L, t, true));
        Kiem.sai("thu hoi (-1) het", ThanhVienLogic.conHan(-1L, t, true));
        Kiem.sai("khong co dong: khong phai thanh vien", ThanhVienLogic.conHan(0L, t, false));
        Kiem.dung("con hen", ThanhVienLogic.conHan(t + 1, t, true));
        Kiem.bang("cap moi 30 ngay", t + 30 * d, ThanhVienLogic.hanMoi(false, 0L, t, 30));
        Kiem.bang("gia han noi tiep", t + 40 * d, ThanhVienLogic.hanMoi(true, t + 10 * d, t, 30));
        Kiem.bang("het han roi gia han: tu bay gio", t + 30 * d, ThanhVienLogic.hanMoi(true, t - 5 * d, t, 30));
        Kiem.bang("dang vinh vien giu vinh vien", 0L, ThanhVienLogic.hanMoi(true, 0L, t, 30));
        Kiem.bang("cap vinh vien (ngay 0)", 0L, ThanhVienLogic.hanMoi(false, 0L, t, 0));
        Kiem.bang("thu hoi lan cap 30 ngay", t + 10 * d, ThanhVienLogic.hanSauThuHoi(t + 40 * d, t, 30));
        Kiem.bang("thu hoi het ngay -> -1", -1L, ThanhVienLogic.hanSauThuHoi(t + 10 * d, t, 30));
        Kiem.bang("hoan don vinh vien -> -1", -1L, ThanhVienLogic.hanSauThuHoi(0L, t, 0));
        Kiem.bang("hoan don co han khi dang vinh vien (admin cap) -> giu 0", 0L, ThanhVienLogic.hanSauThuHoi(0L, t, 30));
    }

    static void cong() {
        Kiem.nhom("Thanh vien - cong ThanhVien.chan (tat = khong chan)");
        Char p = null;
        try {
            p = new Char(null);
        } catch (final Throwable e) {
            p = null;
        }
        if (p == null) {
            // Ngoai server (khong co du lieu ban do): tao Char "rong" bang Unsafe qua phan chieu.
            try {
                final Class<?> u = Class.forName("sun.misc.Unsafe");
                final java.lang.reflect.Field f = u.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                p = (Char) u.getMethod("allocateInstance", Class.class).invoke(f.get(null), Char.class);
            } catch (final Throwable e) {
                System.out.println("   (bo qua: khong tao duoc Char - " + e + ")");
            }
        }
        ThanhVien.datThu(ThanhVienLogic.TAT);
        if (p == null) {
            return;
        }
        p.userID = -777001;
        p.isBot = -1;
        Kiem.bang("tat: khong chan", null, ThanhVien.chan(p, "giao_dich"));
        ThanhVien.datThu(ch(ThanhVienLogic.K_BAT, "1", ThanhVienLogic.K_CHUC_NANG, "giao_dich"));
        ThanhVien.datNhoThu(-777001, -1L, false);
        Kiem.dung("bat + khong thanh vien: chan voi loi nhan", ThanhVien.chan(p, "giao_dich") != null);
        Kiem.bang("tinh nang khong khoa: khong chan", null, ThanhVien.chan(p, "bang"));
        ThanhVien.datNhoThu(-777001, 0L, true);
        Kiem.bang("thanh vien vinh vien: khong chan", null, ThanhVien.chan(p, "giao_dich"));
        ThanhVien.datNhoThu(-777001, System.currentTimeMillis() - 1000L, true);
        Kiem.dung("het han: chan", ThanhVien.chan(p, "giao_dich") != null);
        p.isBot = 1;
        Kiem.bang("bot cua server: khong chan", null, ThanhVien.chan(p, "giao_dich"));
        Kiem.bang("null an toan", null, ThanhVien.chan(null, "giao_dich"));
    }

    static void kichHoat() {
        Kiem.nhom("Kich hoat - chua kich hoat VAN choi, chi khoa vai tinh nang (cung cong voi Thanh vien)");
        final ThanhVienLogic.CauHinh t = ThanhVienLogic.TAT;
        Kiem.sai("mac dinh KHONG chan dang nhap", t.khChanDangNhap);
        Kiem.bang("mac dinh khoa 6 tinh nang khi chua kich hoat", "[giao_dich, chat_the_gioi, cho, chuyen_luong, nhan_tang, giftcode]",
                t.khChucNang.toString());
        Kiem.dung("mac dinh thanh vien = da kich hoat", t.khTvLaKh);
        ThanhVien.datThu(t);
        Kiem.sai("ThanhVien.chanDangNhap() mac dinh false", ThanhVien.chanDangNhap());
        ThanhVien.datThu(ch(ThanhVienLogic.K_KH_CHAN_DANG_NHAP, "1"));
        Kiem.dung("kich_hoat.chan_dang_nhap=1 -> chan (hanh vi cu)", ThanhVien.chanDangNhap());
        // Ma tran quyetDinh.
        final ThanhVienLogic.CauHinh c = ch(ThanhVienLogic.K_BAT, "1", ThanhVienLogic.K_CHUC_NANG, "bang,giao_dich",
                ThanhVienLogic.K_KH_CHUC_NANG, "giao_dich,chat_the_gioi");
        Kiem.bang("chua kich hoat + tinh nang can kich hoat -> CAN_KICH_HOAT", ThanhVienLogic.CAN_KICH_HOAT,
                ThanhVienLogic.quyetDinh(c, "chat_the_gioi", false, false, 30));
        Kiem.bang("da kich hoat, khong can thanh vien -> DUOC", ThanhVienLogic.DUOC, ThanhVienLogic.quyetDinh(c, "chat_the_gioi", true, false, 30));
        Kiem.bang("da kich hoat nhung can thanh vien -> CAN_THANH_VIEN", ThanhVienLogic.CAN_THANH_VIEN, ThanhVienLogic.quyetDinh(c, "bang", true, false, 30));
        Kiem.bang("chua kich hoat, tinh nang khong khoa -> DUOC", ThanhVienLogic.DUOC, ThanhVienLogic.quyetDinh(c, "cho", false, false, 30));
        Kiem.bang("thanh vien chua kich hoat (tv = kich hoat) -> DUOC", ThanhVienLogic.DUOC, ThanhVienLogic.quyetDinh(c, "giao_dich", false, true, 30));
        final ThanhVienLogic.CauHinh c2 = ch(ThanhVienLogic.K_KH_CHUC_NANG, "giao_dich", ThanhVienLogic.K_KH_TV_LA_KH, "0");
        Kiem.bang("thanh_vien_la_kich_hoat=0: thanh vien chua kich hoat van bi khoa", ThanhVienLogic.CAN_KICH_HOAT,
                ThanhVienLogic.quyetDinh(c2, "giao_dich", false, true, 30));
        Kiem.dung("loi nhan chi toi /kichhoat", ThanhVienLogic.thongBaoKichHoat(c, "giao_dich", "https://htbb.vn").contains("https://htbb.vn/kichhoat"));
        final ThanhVienLogic.CauHinh rong = ch(ThanhVienLogic.K_KH_CHUC_NANG, "");
        Kiem.dung("danh sach rong = khong khoa gi", rong.khChucNang.isEmpty());
        // Cong that voi Char rong.
        Char p = null;
        try {
            final Class<?> u = Class.forName("sun.misc.Unsafe");
            final java.lang.reflect.Field f = u.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            p = (Char) u.getMethod("allocateInstance", Class.class).invoke(f.get(null), Char.class);
        } catch (final Throwable e) {
            System.out.println("   (bo qua cong: " + e + ")");
        }
        if (p != null) {
            p.userID = -777002;
            p.isBot = -1;
            ThanhVien.datThu(c);
            ThanhVien.datKichHoatThu(-777002, false);
            ThanhVien.datNhoThu(-777002, -1L, false);
            final String m = ThanhVien.chan(p, "chat_the_gioi");
            Kiem.dung("chua kich hoat: bi khoa chat the gioi, loi nhan /kichhoat", m != null && m.contains("/kichhoat"));
            Kiem.bang("chua kich hoat: tinh nang khong khoa van duoc", null, ThanhVien.chan(p, "cho"));
            ThanhVien.datNhoThu(-777002, 0L, true);
            Kiem.bang("la thanh vien (vinh vien): coi nhu da kich hoat", null, ThanhVien.chan(p, "chat_the_gioi"));
            ThanhVien.datKichHoatThu(-777002, true);
            ThanhVien.datNhoThu(-777002, -1L, false);
            final String m2 = ThanhVien.chan(p, "bang");
            Kiem.dung("da kich hoat, khong thanh vien: tinh nang can thanh vien van khoa (loi nhan /thanh-vien)", m2 != null && m2.contains("/thanh-vien"));
            Kiem.bang("da kich hoat: chat the gioi duoc", null, ThanhVien.chan(p, "chat_the_gioi"));
        }
        ThanhVien.datThu(ThanhVienLogic.TAT);
    }

    static void mocNoi() {
        Kiem.nhom("Thanh vien - moc noi o moi cong");
        final String[][] cho = {
            {"src/real/MapMuaBan.java", "ThanhVien.chanVaBao(player, \"giao_dich\")"},
            {"src/real/MapMuaBan.java", "ThanhVien.chanHaiNguoi(player, p, \"giao_dich\", \"nhan_tang\")"},
            {"src/real/MapNhapText.java", "ThanhVien.chanVaBao(player, \"chat_the_gioi\")"},
            {"src/real/MapNhapText.java", "ThanhVien.chanHaiNguoi(player, p, \"gui_thu\", \"nhan_tang\")"},
            {"src/real/OfflineMap.java", "ThanhVien.chanVaBao(player, \"cho\")"},
            {"src/com/htbb/pokemon/NapTienGame.java", "ThanhVien.chanVaBao(p, \"cho\")"},
            {"src/com/htbb/pokemon/HeSinhThaiMenu.java", "ThanhVien.chanVaBao(p, \"cho_sinh_thai\")"},
            {"src/real/CharChuyenLuong.java", "ThanhVien.chanVaBao(player, \"chuyen_luong\")"},
            {"src/real/MapDoi.java", "ThanhVien.chanVaBao(player, \"bang\")"},
            {"src/real/MapDoMenu.java", "ThanhVien.chanVaBao(player, \"pvp_xep_hang\")"},
            {"src/data/DatabaseGiftCode.java", "ThanhVien.chanVaBao(player, \"giftcode\")"},
            {"src/real/QuaHoatDong.java", "ThanhVien.chan(p, \"qua_online\")"},
            {"src/real/RealController.java", "ThanhVien.chanVaBao(player, \"qua_online\")"},
            {"src/real/MapXaHoi.java", "ThanhVien.chanVaBao(player, \"gui_thu\")"},
            {"src/server/CauHinh.java", "com.htbb.pokemon.ThanhVien.apDungCauHinh(p);"},
            {"src/real/cmd/SelectCharHandler.java", "ThanhVien.chanDangNhap(p.userID) && !Database.check_active(p.userID)"},
            {"src/real/cmd/SelectCharHandler.java", "ThanhVien.baoKhiVaoGame(p)"},
        };
        for (final String[] x : cho) {
            Kiem.dung(x[0].replace("src/", "") + ": " + x[1], doc(x[0]).contains(x[1]));
        }
        Kiem.bang("LoginHandler: 3 cua dang nhap chi chan khi kich_hoat.chan_dang_nhap=1", 3,
                dem(doc("src/real/cmd/LoginHandler.java"), "ThanhVien.chanDangNhap(userID) && !Database.check_active("));
        Kiem.bang("LoginHandler: khong con cua chan cung nao", 0, dem(doc("src/real/cmd/LoginHandler.java"), "if (!Database.check_active("));
        Kiem.bang("OfflineMap: 5 cua cho (rao x3, mua x2)", 5, dem(doc("src/real/OfflineMap.java"), "ThanhVien.chanVaBao(player, \"cho\")"));
        Kiem.bang("HeSinhThaiMenu: 5 cua cho sinh thai", 5, dem(doc("src/com/htbb/pokemon/HeSinhThaiMenu.java"), "ThanhVien.chanVaBao(p, \"cho_sinh_thai\")"));
        Kiem.dung("game KHONG cong gi cho don thanh_vien", doc("src/com/htbb/pokemon/NapTienGame.java")
                .contains("NapTienLogic.LOAI_THANH_VIEN.equals(d.loai)"));
        Kiem.bang("goi thanh_vien hop le (khong luong)", null, NapTienLogic.kiemGoi(NapTienLogic.LOAI_THANH_VIEN, 20000, 0));
        Kiem.dung("goi thanh_vien kem luong bi tu choi", NapTienLogic.kiemGoi(NapTienLogic.LOAI_THANH_VIEN, 20000, 200) != null);
        Kiem.sai("ThanhVien khong cong tien te / chi so", doc("src/com/htbb/pokemon/ThanhVien.java").matches("(?s).*(addLuong|addXu|addLuongLock|doAddGemItem).*"));
    }

    static void sqlVaWeb() {
        Kiem.nhom("Thanh vien - bang tv_* / cau_hinh / nap_goi trong sql/htbb_day_du.sql + web");
        Kiem.bang("mac dinh TAT (cau_hinh thanh_vien.bat / ban_bat)", "0|0", DayDu.giaTri("cau_hinh", "khoa", "thanh_vien.bat", "gia_tri")
                + "|" + DayDu.giaTri("cau_hinh", "khoa", "thanh_vien.ban_bat", "gia_tri"));
        Kiem.bang("chuc_nang mac dinh trung Java", ThanhVienLogic.CHUC_NANG_MAC_DINH,
                DayDu.giaTri("cau_hinh", "khoa", "thanh_vien.chuc_nang", "gia_tri"));
        final java.util.Map<String, String> g = DayDu.tim("nap_goi", "loai", "thanh_vien");
        Kiem.dung("goi THANH_VIEN 20.000 d, khong ban mac dinh", g != null && "20000".equals(g.get("gia_vnd")) && "0".equals(g.get("dang_ban")));
        Kiem.dung("bang tv_thanh_vien / tv_nhat_ky co trong CSDL day du (rong)", DayDu.coBang("tv_thanh_vien") && DayDu.coBang("tv_nhat_ky")
                && DayDu.soDong("tv_thanh_vien") <= 0);
        final String ts = doc("../web/server/utils/thanhVienThuan.ts");
        if (ts.isEmpty()) {
            System.out.println("   (bo qua doi chieu web)");
            return;
        }
        boolean du = true;
        for (final String[] c : ThanhVienLogic.CHUC_NANG) {
            du &= ts.contains("'" + c[0] + "'");
        }
        Kiem.dung("web co du ma tinh nang", du);
        Kiem.dung("web chuc nang mac dinh trung", ts.contains("'" + ThanhVienLogic.CHUC_NANG_MAC_DINH + "'"));
        Kiem.dung("web khoa cau_hinh trung", ts.contains("'" + ThanhVienLogic.K_BAN_BAT + "'") && ts.contains("'" + ThanhVienLogic.K_CHUC_NANG + "'"));
        Kiem.dung("web khoa kich hoat trung", ts.contains("'" + ThanhVienLogic.K_KH_CHAN_DANG_NHAP + "'") && ts.contains("'" + ThanhVienLogic.K_KH_TV_LA_KH + "'")
                && ts.contains("'" + ThanhVienLogic.KH_CHUC_NANG_MAC_DINH + "'"));
        final String dm = DayDu.danhMucJson();
        Kiem.dung("danh muc cau hinh trung tam co 4 khoa kich_hoat.*", dm.contains("\"kich_hoat.chan_dang_nhap\"") && dm.contains("\"kich_hoat.chuc_nang\"")
                && dm.contains("\"kich_hoat.thong_bao\"") && dm.contains("\"kich_hoat.thanh_vien_la_kich_hoat\""));
    }

    static int dem(final String s, final String tu) {
        int n = 0;
        for (int i = s.indexOf(tu); i >= 0; i = s.indexOf(tu, i + 1)) {
            n++;
        }
        return n;
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
