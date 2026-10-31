import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.DoiLuong;
import com.htbb.pokemon.DoiLuongDb;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.SanLuong;
import data.LenhWeb;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import real.Char;
import real.DoiLuongGame;

/**
 * Doi luong (chu game 31/10/2026, docs/thiet-ke-game.md 9.1 / 9.5): luat thuan com.htbb.pokemon.DoiLuong (ti le khoa,
 * ti gia luong -> xu chung toan may chu theo cap trung vi, tran, phi san, khung gia), so lenh thuan SanLuong.khop (khong
 * sinh xu, gia maker, chan clone), doi luong -> luong khoa tren Char that (mot chieu, nguyen tu khi hai luong cung doi),
 * lenh web (offline cho vao game, chot dong), danh muc 5 khoa, DDL trong sql/htbb.sql, va luat cua so giao dich (khong
 * luong / luong khoa).
 */
public final class KiemDoiLuong {

    private KiemDoiLuong() {
    }

    /** Chay rieng: java KiemDoiLuong (tu server/). "ddl" = in DDL cho khoi SQL them_doi_luong.sql. */
    public static void main(final String[] args) {
        if (args.length > 0 && "ddl".equals(args[0])) {
            for (final String s : DoiLuongDb.DDL) {
                System.out.println(s + ";");
            }
            return;
        }
        try {
            com.htbb.dulieu.DuLieuSql.napTuDumpDayDu();
        } catch (final Throwable ignored) {
        }
        chay();
        System.exit(Kiem.tongKet());
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
    }

    public static void chay() {
        Kiem.nhom("Doi luong: luong -> luong khoa / xu, san luong <-> xu");
        luatThuan();
        san();
        motChieuVaNguyenTu();
        lenhWebOffline();
        try {
            danhMuc();
            noiGame();
            giaoDichKhongCoLuong();
        } catch (final Exception e) {
            Kiem.dung("doi luong: doc tep: " + e, false);
        }
    }

    static void luatThuan() {
        Kiem.bang("1 luong = 1 luong khoa (ti le 1)", 10L, DoiLuong.luongKhoaNhan(10, 1));
        Kiem.bang("ti le 2", 20L, DoiLuong.luongKhoaNhan(10, 2));
        Kiem.bang("so 0 -> khong hop le", -1L, DoiLuong.luongKhoaNhan(0, 1));
        Kiem.bang("ti le 0 = tat", -1L, DoiLuong.luongKhoaNhan(10, 0));
        Kiem.bang("tran int -> tu choi", -1L, DoiLuong.luongKhoaNhan(Integer.MAX_VALUE, 2));
        Kiem.bang("cap trung vi (s[n/2] nhu ChiSoKinhTe)", 30, DoiLuong.capTrungVi(new int[]{50, 10, 30, 20, 40}));
        Kiem.bang("cap trung vi khong ai -> 1", 1, DoiLuong.capTrungVi(new int[0]));
        Kiem.bang("ti gia = round(1% x 12025)", 120L, DoiLuong.tiGia(12025, 1));
        Kiem.bang("ti gia lam tron len tu .5", 121L, DoiLuong.tiGia(12050, 1));
        Kiem.bang("ti gia toi thieu 1", 1L, DoiLuong.tiGia(10, 1));
        Kiem.bang("phan_tram_gio 0 = tat", 0L, DoiLuong.tiGia(12025, 0));
        final long tg = DoiLuong.tiGia(LauDai.xuMoiGio(DoiLuong.capTrungVi(new int[]{27, 27, 50})), 1);
        Kiem.dung("ti gia CHUNG: chi phu thuoc cap trung vi, khong theo cap nguoi doi (" + tg + " xu/luong)",
                tg == DoiLuong.tiGia(LauDai.xuMoiGio(27), 1));
        Kiem.bang("xu nhan = so x ti gia", 12000L, DoiLuong.xuNhan(100, 120));
        Kiem.bang("xu nhan tran long -> -1", -1L, DoiLuong.xuNhan(Long.MAX_VALUE / 2, 3));
        Kiem.bang("con lai tran", 4900L, DoiLuong.conLai(5000, 100));
        Kiem.bang("con lai khong am", 0L, DoiLuong.conLai(5000, 9000));
        Kiem.bang("ngay theo gio Viet Nam: 17:30 UTC 31/10 = 01/11", "2026-11-01",
                DoiLuong.ngay(java.time.Instant.parse("2026-10-31T17:30:00Z").toEpochMilli()).toString());
        Kiem.bang("mac dinh: ti le 1, 1%, tran 5000, phi 5%, tran san 50000", "1/1/5000/5/50000",
                DoiLuong.TI_LE_KHOA + "/" + DoiLuong.XU_PHAN_TRAM_GIO + "/" + DoiLuong.XU_TRAN_NGAY + "/" + DoiLuong.SAN_PHI + "/"
                        + DoiLuong.SAN_TRAN_NGAY);
    }

    static void san() {
        Kiem.bang("phi moi luong ceil(100 x 5%)", 5L, DoiLuong.phiMoiLuong(100, 5));
        Kiem.bang("phi moi luong ceil(101 x 5%)", 6L, DoiLuong.phiMoiLuong(101, 5));
        Kiem.bang("ky quy mua 10 x (101 + 6)", 1070L, DoiLuong.kyQuyMua(10, 101, 5));
        final long[] k = DoiLuong.khungGia(120);
        Kiem.bang("khung gia 50%..500% ti gia", "60..600", k[0] + ".." + k[1]);
        Kiem.dung("chua co ti gia -> san dong", DoiLuong.khungGia(0) == null);
        Kiem.dung("gia ngoai khung bi tu choi", DoiLuong.loiLenh(10, 601, 120, 50000, 0) != null
                && DoiLuong.loiLenh(10, 59, 120, 50000, 0) != null);
        Kiem.dung("vuot tran ngay bi tu choi", DoiLuong.loiLenh(101, 120, 120, 100, 0) != null);
        Kiem.dung("qua 10 lenh mo bi tu choi", DoiLuong.loiLenh(1, 120, 120, 100, DoiLuong.SAN_LENH_MO_TOI_DA) != null);
        Kiem.dung("lenh hop le", DoiLuong.loiLenh(100, 120, 120, 100, 0) == null);

        // Lenh BAN 20 gia 100 vao so MUA: 120 x5 (id 1), 110 x10 (id 2, bi chan clone), 105 x10 (id 3), 90 x50 (id 4)
        final List<SanLuong.Lenh> mua = new ArrayList<SanLuong.Lenh>();
        mua.add(new SanLuong.Lenh(1, DoiLuongDb.LOAI_MUA, 11, 101, "a", 120, 5, DoiLuong.kyQuyMua(5, 120, 5)));
        mua.add(new SanLuong.Lenh(2, DoiLuongDb.LOAI_MUA, 12, 102, "clone", 110, 10, DoiLuong.kyQuyMua(10, 110, 5)));
        mua.add(new SanLuong.Lenh(3, DoiLuongDb.LOAI_MUA, 13, 103, "c", 105, 10, DoiLuong.kyQuyMua(10, 105, 5)));
        mua.add(new SanLuong.Lenh(4, DoiLuongDb.LOAI_MUA, 14, 104, "d", 90, 50, DoiLuong.kyQuyMua(50, 90, 5)));
        final List<SanLuong.Khop> ks = SanLuong.khop(true, 100, 20, mua, 5, (l, xu) -> "clone".equals(l.ten));
        Kiem.bang("khop: 5 @120 (id1), bo id2 (clone), 10 @105 (id3), dung truoc 90", "1:5@120,3:10@105", chuoi(ks));
        long xuBan = 0L;
        long xuMua = 0L;
        long phi = 0L;
        for (final SanLuong.Khop x : ks) {
            xuBan += x.xuBan;
            xuMua += x.xuMua;
            phi += x.phi;
            Kiem.dung("gia khop = gia lenh dang cho (maker) va >= gia ban", x.gia == x.cho.gia && x.gia >= 100);
            Kiem.dung("ky quy nguoi mua du cho moi lan khop", x.xuMua <= x.cho.xuKyQuy);
        }
        Kiem.bang("khong sinh xu: xu nguoi mua tra = xu nguoi ban nhan + phi dot", xuMua, xuBan + phi);
        Kiem.bang("phi = 5 x 6 + 10 x 6", 90L, phi);
        // Lenh MUA 8 gia 115 vao so BAN: 100 x3, 112 x10, 116 x5
        final List<SanLuong.Lenh> ban = new ArrayList<SanLuong.Lenh>();
        ban.add(new SanLuong.Lenh(7, DoiLuongDb.LOAI_BAN, 21, 201, "e", 100, 3, 0));
        ban.add(new SanLuong.Lenh(8, DoiLuongDb.LOAI_BAN, 22, 202, "f", 112, 10, 0));
        ban.add(new SanLuong.Lenh(9, DoiLuongDb.LOAI_BAN, 23, 203, "g", 116, 5, 0));
        final List<SanLuong.Khop> km = SanLuong.khop(false, 115, 8, ban, 5, null);
        Kiem.bang("mua: re nhat truoc, dung o gia > gia mua", "7:3@100,8:5@112", chuoi(km));
        long tru = 0L;
        for (final SanLuong.Khop x : km) {
            tru += x.xuMua;
        }
        Kiem.dung("mua khop re hon gia toi da: tru < ky quy (phan du hoan lai)", tru < DoiLuong.kyQuyMua(8, 115, 5));
    }

    static String chuoi(final List<SanLuong.Khop> ks) {
        final StringBuilder b = new StringBuilder();
        for (final SanLuong.Khop k : ks) {
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(k.cho.id).append(':').append(k.so).append('@').append(k.gia);
        }
        return b.toString();
    }

    static void motChieuVaNguyenTu() {
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0]; // Char(null) can mang ngoc (giong KiemCmeTai)
        }
        try {
            motChieuVaNguyenTu0();
        } finally {
            real.Map.gemTemplate = gemCu;
        }
    }

    static void motChieuVaNguyenTu0() {
        try {
            final Char p = new Char(null);
            p.charname = "kiemdoiluong";
            p.setLuong(100);
            Kiem.dung("doi 30 luong -> luong khoa", DoiLuongGame.doiKhoa(p, 30, 1, "kiem", 0L) == null);
            Kiem.bang("luong con 70, luong khoa +30", "70/30", p.getLuong() + "/" + p.getLuongLock());
            Kiem.dung("ti le khac cau hinh hien tai -> tu choi (khong doi ngam)", DoiLuongGame.doiKhoa(p, 1, 2, "kiem", 0L) != null);
            final Char q = new Char(null);
            q.charname = "kiemdoiluong2";
            q.addLuongLock(500);
            Kiem.dung("MOT CHIEU: luong khoa khong phai dau vao (0 luong, 500 luong khoa -> tu choi)",
                    DoiLuongGame.doiKhoa(q, 10, 1, "kiem", 0L) != null && q.getLuongLock() == 500 && q.getLuong() == 0);
            Kiem.dung("thieu luong -> khong doi gi", DoiLuongGame.doiKhoa(p, 71, 1, "kiem", 0L) != null
                    && p.getLuong() == 70 && p.getLuongLock() == 30);

            // Nguyen tu: 8 luong cung doi 20 tu 70 luong -> dung 3 lan thanh cong, khong am, tong bao toan.
            final int soLuong = 8;
            final CountDownLatch bd = new CountDownLatch(1);
            final CountDownLatch xong = new CountDownLatch(soLuong);
            final AtomicInteger ok = new AtomicInteger();
            for (int i = 0; i < soLuong; i++) {
                new Thread(() -> {
                    try {
                        bd.await();
                        if (DoiLuongGame.doiKhoa(p, 20, 1, "kiem", 0L) == null) {
                            ok.incrementAndGet();
                        }
                    } catch (final Exception ignored) {
                    } finally {
                        xong.countDown();
                    }
                }).start();
            }
            bd.countDown();
            xong.await();
            Kiem.bang("nguyen tu: 8 luong doi 20 tu 70 -> 3 lan", 3, ok.get());
            Kiem.bang("bao toan: luong + luong khoa = 100", 100, p.getLuong() + p.getLuongLock());
            Kiem.bang("con 10 luong", 10, p.getLuong());
        } catch (final Throwable e) {
            Kiem.dung("doi khoa tren Char: " + e, false);
        }
    }

    static void lenhWebOffline() {
        try {
            final int[] tt = {LenhWeb.CHO};
            LenhWeb.KetQua kq = DoiLuongGame.xuLyLenhWeb(1L, DoiLuongGame.LENH_KHOA, 987654321, 10, tt);
            Kiem.bang("web, offline: cho vao game (khong ghi thang tob_char)", LenhWeb.CHO_DANG_NHAP, kq.trangThai);
            Kiem.dung("web, offline: co mo ta", !kq.moTa.isEmpty());
            tt[0] = LenhWeb.CHO_DANG_NHAP;
            kq = DoiLuongGame.xuLyLenhWeb(1L, DoiLuongGame.LENH_XU, 987654321, 10, tt);
            Kiem.dung("web, van offline: giu nguyen, khong ghi lai", kq.trangThai == LenhWeb.CHO_DANG_NHAP && kq.moTa.isEmpty());
            boolean nem = false;
            try {
                DoiLuongGame.xuLyLenhWeb(1L, DoiLuongGame.LENH_KHOA, 987654321, 0, tt);
            } catch (final IllegalArgumentException e) {
                nem = true;
            }
            Kiem.dung("web: so luong 0 -> loi", nem);
            Kiem.dung("chot DANG_XU_LY chi tu CHO / CHO_DANG_NHAP", !LenhWeb.chotDangXuLy(1L, LenhWeb.XONG_ONLINE)
                    && !LenhWeb.chotDangXuLy(1L, LenhWeb.DANG_XU_LY) && !LenhWeb.chotDangXuLy(1L, LenhWeb.HUY));
        } catch (final Throwable e) {
            Kiem.dung("lenh web doi luong: " + e, false);
        }
    }

    static void danhMuc() throws Exception {
        final String[][] k = {
            {DoiLuong.K_TI_LE_KHOA, "1"}, {DoiLuong.K_XU_PHAN_TRAM_GIO, "1"}, {DoiLuong.K_XU_TRAN_NGAY, "5000"},
            {DoiLuong.K_SAN_PHI, "5"}, {DoiLuong.K_SAN_TRAN_NGAY, "50000"},
        };
        for (final String[] x : k) {
            final DinhNghiaKhoa d = CauHinhTrungTam.dinhNghia(x[0]);
            Kiem.dung("danh muc co " + x[0], d != null);
            if (d != null) {
                Kiem.bang(x[0] + " mac dinh", x[1], d.macDinh);
                Kiem.bang(x[0] + " nhom", "kinh_te", d.nhom);
            }
        }
        final String sql = doc("../sql/htbb.sql");
        for (final String ddl : DoiLuongDb.DDL) {
            Kiem.dung("sql/htbb.sql co DDL " + ddl.substring(27, ddl.indexOf('(')).trim(), sql.contains(ddl + ";"));
        }
        for (final String[] x : k) {
            Kiem.dung("phan A + B sql co " + x[0], sql.contains("('" + x[0] + "','kinh_te'") && sql.contains("('" + x[0] + "', 'kinh_te'"));
            Kiem.dung("gen.ts co " + x[0], doc("../web/shared/utils/cauHinhTrungTam.gen.ts").contains("khoa: \"" + x[0] + "\""));
            Kiem.dung("docs/cau-hinh.md co " + x[0], doc("../docs/cau-hinh.md").contains("| `" + x[0] + "` |"));
        }
    }

    static void noiGame() throws Exception {
        final String g = doc("src/real/DoiLuongGame.java");
        final String s = doc("src/com/htbb/pokemon/SanLuong.java");
        Kiem.dung("luong khoa khong bao gio bi tru (khong subLuongLock / truLuongKhoaNeuDu)",
                !g.contains("subLuongLock") && !g.contains("truLuongKhoaNeuDu") && !s.contains("LuongLock"));
        Kiem.dung("moi duong doi tru luong THUONG bang truLuongMoChuyen", g.contains("p.truLuongMoChuyen(n)")
                && !g.contains("subLuong(") && !g.contains("truLuongNeuDu("));
        Kiem.dung("khong duong nao doi ra luong tu he thong: addLuong chi o hoan / tra tien san",
                g.split("p\\.addLuong\\(", -1).length - 1 == 2);
        Kiem.dung("doi khoa / doi xu kiem-va-tru trong Char.truNeuDu", g.split("p\\.truNeuDu\\(", -1).length - 1 == 2);
        Kiem.dung("doi xu: xu sinh ra dem vao kinh te (XU_DEM)", g.contains("p.addXu(xu, DoiLuong.LY_DO_XU, Char.XU_DEM)"));
        Kiem.dung("san: ky quy / tra tien la xu CHUYEN (khong dem sinh / dot)", g.contains("Char.XU_CHUYEN") && g.contains("LauDai.ghiXuRa(kq.phi)"));
        Kiem.dung("san: cong chong clone mot ben + tung doi tac", g.contains("ChongClone.chanMotBen(p, ChongClone.KIEU_KY_GUI)")
                && g.contains("ChongClone.chanChuyen(p, l.charId, l.ten, ChongClone.KIEU_KY_GUI, xu)"));
        Kiem.dung("bot (huan luyen vien AI) khong dung doi luong / san", g.contains("HlvAiGame.laBot(p)"));
        Kiem.dung("san: tu khop bi loai (user_id <> nguoi dat)", s.contains("user_id <> ?"));
        Kiem.dung("san: khoa dong doi dien FOR UPDATE trong giao dich", s.contains("FOR UPDATE") && s.contains("DoiLuongDb.giaoDich"));
        Kiem.dung("san: luu nhan vat SAU khi tru ky quy, TRUOC khi ghi lenh", g.indexOf("daLuu = Database.instance.saveCharAuto(p)")
                < g.indexOf("SanLuong.datLenh(") && g.indexOf("p.truLuongMoChuyen(n) //") < g.indexOf("daLuu = Database"));
        Kiem.dung("tra tien: chot 0 -> 1 roi moi cong", doc("src/com/htbb/pokemon/DoiLuongDb.java")
                .contains("SET trang_thai = 1, tra_luc = ? WHERE id = ? AND trang_thai = 0"));
        Kiem.dung("ti gia dong bang ca ngay (INSERT IGNORE)", doc("src/com/htbb/pokemon/DoiLuongDb.java")
                .contains("INSERT IGNORE INTO tob_ti_gia_luong_xu"));
        Kiem.dung("ti gia loai bot (team_user.la_bot)", doc("src/com/htbb/pokemon/DoiLuongDb.java").contains("la_bot = 1"));
        Kiem.dung("NPC -10 noi muc Doi luong", doc("src/real/cmd/RequestNPCInfoHandler.java").contains("real.DoiLuongGame.noiMenuTongQuan(p, menu)"));
        Kiem.dung("MapDoMenu / MapNhapText / TeamServer noi DoiLuongGame", doc("src/real/MapDoMenu.java").contains("DoiLuongGame.xuLyMenu(")
                && doc("src/real/MapNhapText.java").contains("DoiLuongGame.xuLyNhap(")
                && doc("src/server/TeamServer.java").contains("real.DoiLuongGame.batDau()"));
        final String lw = doc("src/data/LenhWeb.java");
        Kiem.dung("LenhWeb chuyen lenh doi luong sang DoiLuongGame, chot co dieu kien", lw.contains("real.DoiLuongGame.xuLyLenhWeb(")
                && lw.contains("WHERE id = ? AND trang_thai = ?"));
        final String web = doc("../web/server/utils/lenhNhanVat.ts");
        Kiem.dung("web chi huy dong CHO / CHO_DANG_NHAP (dong da chot khong huy duoc)", web.contains("trang_thai IN (?, ?)"));
        Kiem.dung("web /account khong ghi thang tob_char", !doc("../web/server/api/doi-luong/index.post.ts").contains("UPDATE tob_char"));
    }

    /** Cua so giao dich 1-1 (goi 66): khong co o xu / luong / luong khoa; luong chuyen chi qua NPC (CharChuyenLuong) + san. */
    static void giaoDichKhongCoLuong() throws Exception {
        final String m = doc("src/real/MapMuaBan.java");
        final int a = m.indexOf("private static void doTradeDuoiKhoa(");
        final int b = m.indexOf("static void doUseSpecialITem(");
        final String gd = m.substring(a, b);
        Kiem.dung("giao dich: khong cong / tru luong", !gd.contains("addLuong") && !gd.contains("subLuong")
                && !gd.contains("truLuong") && !gd.contains("setLuong"));
        Kiem.dung("giao dich: khong cong / tru luong khoa", !gd.contains("LuongLock") && !gd.contains("lockLuong"));
        Kiem.dung("giao dich: hien chi cho thuoc (o vat pham tu choi, khong co o xu)", gd.contains("Không thể trao đổi vật phẩm này.")
                && !gd.contains("addXu") && !gd.contains("subXu"));
        Kiem.dung("chuyen luong giua nguoi choi qua NPC (CharChuyenLuong, phi)", Files.exists(Paths.get("src/real/CharChuyenLuong.java")));
    }
}
