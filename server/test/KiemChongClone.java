import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.ChongCloneLogic.Ben;
import com.htbb.pokemon.ChongCloneLogic.CauHinh;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Chong clone + luong khoa khong chuyen duoc (chu game 19/10/2026: "tao nhieu clone de giao dich xu qua thi sao?
 * luong khoa thi khong giao dich duoc dung khong?").
 *
 * Phan 1-4: ham thuan com.htbb.pokemon.ChongCloneLogic (cap, tuoi tai khoan, gio choi, gioi han cung may, nghi clone,
 * gia ky gui). Phan 5: thuoc khoa (real.ThuocKhoa + HoSoLauDai "x<id>"). Phan 6-7: quet ma nguon - moi duong chuyen
 * xu / gia tri giua hai nguoi choi deu goi cong, va KHONG duong nao chuyen luong khoa.
 */
public final class KiemChongClone {

    private KiemChongClone() {
    }

    private static final long GIO = 3600000L;

    static Ben ben(final int id, final int cap, final String may, final String ip, final long tuoiMs, final long phut) {
        return new Ben(id, "nv" + id, id * 10, cap, may, ip, tuoiMs, phut);
    }

    public static void chay() {
        Kiem.nhom("Chong clone - cap / tuoi tai khoan / gio choi / cung may / gia ky gui; luong khoa khong chuyen duoc");
        capVaTuoi();
        cungMay();
        nghiClone();
        giaKyGui();
        thuocKhoa();
        quetCong();
        quetLuongKhoa();
        cauHinh();
    }

    // ------------------------------------------------------------------ 1. cap / tuoi / gio choi

    static void capVaTuoi() {
        final CauHinh c = CauHinh.macDinh();
        Kiem.bang("mac dinh: cap toi thieu 20", 20, c.capToiThieu);
        Kiem.bang("mac dinh: tuoi tai khoan 24 gio", 24, c.tuoiTaiKhoanGio);
        Kiem.bang("mac dinh: gio choi 10", 10, c.gioChoiToiThieu);
        Kiem.bang("mac dinh: 2 nhan vat cung may / nguoi nhan / ngay", 2, c.nvCungMayToiDa);

        final Ben du = ben(1, 20, "m1", "1.1.1.1", 24 * GIO, 600);
        Kiem.bang("cap 20, 24 gio, 10 gio choi: duoc", null, ChongCloneLogic.lyDoChan(c, du, true));
        final String c19 = ChongCloneLogic.lyDoChan(c, ben(1, 19, "m1", "", 48 * GIO, 999), true);
        Kiem.dung("cap 19: bi chan, bao can cap 20", c19 != null && c19.contains("cấp 20"));
        final String t = ChongCloneLogic.lyDoChan(c, ben(1, 30, "m1", "", 23 * GIO, 999), true);
        Kiem.dung("tai khoan 23 gio: bi chan, bao con 1 gio", t != null && t.contains("24 giờ") && t.contains("còn 1 giờ"));
        final String g = ChongCloneLogic.lyDoChan(c, ben(1, 30, "m1", "", 99 * GIO, 599), true);
        Kiem.dung("599 phut choi: bi chan", g != null && g.contains("chơi đủ 10 giờ"));
        Kiem.bang("khong doc duoc tuoi / gio choi (-1): khong chan vi ly do do", null,
                ChongCloneLogic.lyDoChan(c, ben(1, 30, "", "", -1L, -1L), true));
        final String nk = ChongCloneLogic.lyDoChan(c, ben(7, 5, "", "", -1L, -1L), false);
        Kiem.dung("loi nhan ve nguoi kia neu ten", nk != null && nk.startsWith("Người chơi nv7"));
        final CauHinh tat = new CauHinh(false, 20, 24, 10, 2, 3, 10, 200, 5, false);
        Kiem.bang("tat chong clone: khong chan", null, ChongCloneLogic.lyDoChan(tat, ben(1, 1, "", "", 0L, 0L), true));
        final CauHinh khongTuoi = new CauHinh(true, 20, 0, 0, 2, 3, 10, 200, 5, false);
        Kiem.bang("tuoi = 0 va gio choi = 0: chi con cap", null,
                ChongCloneLogic.lyDoChan(khongTuoi, ben(1, 20, "", "", 0L, 0L), true));
    }

    // ------------------------------------------------------------------ 2. gioi han cung thiet bi / IP

    static void cungMay() {
        final CauHinh c = CauHinh.macDinh();
        final ChongCloneLogic.SoNgay so = new ChongCloneLogic.SoNgay();
        final Ben nhan = ben(100, 50, "mayChinh", "9.9.9.9", 99 * GIO, 9999);
        final Ben a = ben(1, 20, "mayClone", "5.5.5.5", 99 * GIO, 9999);
        final Ben b = ben(2, 20, "mayClone", "5.5.5.6", 99 * GIO, 9999);
        final Ben cc = ben(3, 20, "mayClone", "8.8.8.8", 99 * GIO, 9999); // CUNG may voi a, b (doi mang)
        final Ben d = ben(4, 20, "mayD", "7.7.7.7", 99 * GIO, 9999);
        Kiem.bang("clone 1 gui: duoc", null, so.kiem(c, "20261019", a, nhan));
        so.ghi(c, "20261019", a, nhan);
        Kiem.bang("clone 2 cung may gui: duoc (toi da 2)", null, so.kiem(c, "20261019", b, nhan));
        so.ghi(c, "20261019", b, nhan);
        final String m3 = so.kiem(c, "20261019", cc, nhan);
        Kiem.dung("clone 3 cung may (khac IP): bi chan", m3 != null && m3.contains("thiết bị / mạng"));
        Kiem.bang("nguoi gui da co trong so (clone 1) gui them: duoc", null, so.kiem(c, "20261019", a, nhan));
        Kiem.bang("nguoi khac may, khac IP: duoc", null, so.kiem(c, "20261019", d, nhan));
        Kiem.bang("nguoi nhan khac: duoc", null, so.kiem(c, "20261019", cc, ben(101, 50, "x", "y", 0, 0)));
        Kiem.bang("sang ngay moi: so xoa, clone 3 duoc", null, so.kiem(c, "20261020", cc, nhan));
        Kiem.bang("ngay moi: so nguoi gui cua nguoi nhan = 0", 0, so.soNguoiGui("20261020", 100));
        final ChongCloneLogic.SoNgay soIp = new ChongCloneLogic.SoNgay();
        soIp.ghi(c, "d", ben(11, 20, "m11", "6.6.6.6", 0, 0), nhan);
        soIp.ghi(c, "d", ben(12, 20, "m12", "6.6.6.6", 0, 0), nhan);
        Kiem.dung("3 may khac nhau CUNG IP: nguoi thu 3 bi chan", soIp.kiem(c, "d", ben(13, 20, "m13", "6.6.6.6", 0, 0), nhan) != null);
        Kiem.bang("may khac, IP khac sau 2 nguoi cung IP: duoc", null, soIp.kiem(c, "d", ben(14, 20, "m14", "6.6.6.7", 0, 0), nhan));
        final ChongCloneLogic.SoNgay so2 = new ChongCloneLogic.SoNgay();
        final Ben khongMay = ben(5, 20, "", "", 0, 0);
        so2.ghi(c, "1", khongMay, nhan);
        so2.ghi(c, "1", ben(6, 20, "", "", 0, 0), nhan);
        Kiem.bang("thiet bi / IP rong khong bi coi la trung nhau", null, so2.kiem(c, "1", ben(7, 20, "", "", 0, 0), nhan));
    }

    // ------------------------------------------------------------------ 3. nghi clone

    static void nghiClone() {
        final CauHinh c = CauHinh.macDinh();
        final ChongCloneLogic.SoNgay so = new ChongCloneLogic.SoNgay();
        final Ben nhan = ben(100, 50, "mayChinh", "9.9.9.9", 0, 0);
        ChongCloneLogic.KetQuaGhi k = so.ghi(c, "d", ben(1, 20, "a", "1", 0, 0), nhan);
        Kiem.sai("1 nguoi gui: khong nghi", k.nghi);
        k = so.ghi(c, "d", ben(2, 20, "b", "2", 0, 0), nhan);
        Kiem.sai("2 nguoi gui khac may: khong nghi", k.nghi);
        k = so.ghi(c, "d", ben(3, 20, "c", "3", 0, 0), nhan);
        Kiem.dung("3 nguoi gui trong ngay: nghi", k.nghi && k.lyDo.contains("3 người gửi"));
        final ChongCloneLogic.SoNgay so2 = new ChongCloneLogic.SoNgay();
        so2.ghi(c, "d", ben(1, 20, "chung", "1", 0, 0), nhan);
        k = so2.ghi(c, "d", ben(2, 20, "chung", "2", 0, 0), nhan);
        Kiem.dung("2 nguoi gui chung may: nghi", k.nghi && k.lyDo.contains("chung thiết bị"));
        final ChongCloneLogic.SoNgay so3 = new ChongCloneLogic.SoNgay();
        k = so3.ghi(c, "d", ben(1, 20, "mayChinh", "1", 0, 0), nhan);
        Kiem.dung("nguoi gui cung may voi nguoi nhan: nghi", k.nghi && k.lyDo.contains("cùng thiết bị với người nhận"));
    }

    // ------------------------------------------------------------------ 4. gia ky gui

    static void giaKyGui() {
        final CauHinh c = CauHinh.macDinh();
        Kiem.bang("tran = 10 x gia goc khi lon hon 200 gio thu nhap", 100000000L, ChongCloneLogic.giaToiDa(c, 10000000L, 1000L));
        Kiem.bang("tran = 200 gio thu nhap khi gia goc nho", 3454000L, ChongCloneLogic.giaToiDa(c, 1000L, 17270L));
        Kiem.bang("khong co gia goc: 200 gio thu nhap", 200000L, ChongCloneLogic.giaToiDa(c, 0L, 1000L));
        Kiem.bang("tran khong vuot int (giao thuc ky gui dung int)", (long) Integer.MAX_VALUE,
                ChongCloneLogic.giaToiDa(c, Long.MAX_VALUE / 2, 1000L));
        Kiem.bang("san = 5% gia goc", 500L, ChongCloneLogic.giaToiThieu(c, 10000L));
        Kiem.bang("gia hop le", null, ChongCloneLogic.kiemGiaKyGui(c, 50000L, 10000L, 1000L));
        final String cao = ChongCloneLogic.kiemGiaKyGui(c, 300001L, 1000L, 1500L);
        Kiem.dung("rac gia 1.000 rao 300.001 xu (nguoi ban thu nhap 1.500/gio): bi chan", cao != null && cao.contains("quá cao"));
        final String thap = ChongCloneLogic.kiemGiaKyGui(c, 100L, 10000L, 1000L);
        Kiem.dung("rao 1% gia goc: bi chan (ban pha gia cho clone)", thap != null && thap.contains("quá thấp"));
        Kiem.dung("gia <= 0: bi chan", ChongCloneLogic.kiemGiaKyGui(c, 0L, 1000L, 1000L) != null);
        final CauHinh tat = new CauHinh(false, 20, 24, 10, 2, 3, 10, 200, 5, false);
        Kiem.bang("tat chong clone: gia nao cung duoc (tru <= 0)", null, ChongCloneLogic.kiemGiaKyGui(tat, 999999999L, 1L, 1L));
    }

    // ------------------------------------------------------------------ 5. thuoc khoa

    static void thuocKhoa() {
        Kiem.bang("10 binh, 4 khoa -> giao dich 6", 6, real.ThuocKhoa.giaoDichDuoc(10, 4));
        Kiem.bang("3 binh, 5 khoa (da dung bot) -> 0", 0, real.ThuocKhoa.giaoDichDuoc(3, 5));
        Kiem.bang("10 binh, 0 khoa -> 10", 10, real.ThuocKhoa.giaoDichDuoc(10, 0));
        Kiem.bang("so am -> 0", 0, real.ThuocKhoa.giaoDichDuoc(-2, 0));
        final com.htbb.pokemon.HoSoLauDai h = new com.htbb.pokemon.HoSoLauDai();
        h.datThuocKhoa(94, 12);
        h.datThuocKhoa(96, 3);
        h.datTruot(5, 2);
        final String ma = h.maHoa();
        Kiem.dung("ho so ghi khoa x94 / x96", ma.contains("x94=12") && ma.contains("x96=3"));
        final com.htbb.pokemon.HoSoLauDai h2 = com.htbb.pokemon.HoSoLauDai.giaiMa(ma);
        Kiem.bang("doc lai x94", 12, h2.thuocKhoa(94));
        Kiem.bang("doc lai x96", 3, h2.thuocKhoa(96));
        Kiem.bang("khoa 'x' khong lan sang pity 't'", 2, h2.truot(5));
        h2.datThuocKhoa(94, 0);
        Kiem.sai("dat 0 thi xoa khoa", h2.maHoa().contains("x94"));
    }

    // ------------------------------------------------------------------ 6. moi duong chuyen deu goi cong

    static void quetCong() {
        final String gd = doc("src/real/MapMuaBan.java");
        Kiem.dung("giao dich: moi giao dich kiem cap/tuoi ca hai ben", gd.contains("ChongClone.chanCaHai(player, p, player"));
        Kiem.dung("giao dich: chot kiem gioi han cung may moi chieu", gd.contains("ChongClone.chanChuyen(player, dp, player")
                && gd.contains("ChongClone.chanChuyen(dp, player, player"));
        Kiem.dung("giao dich: chot ghi nhat ky", gd.contains("ChongClone.ghi(player, dp"));
        Kiem.dung("giao dich: thuoc khoa khong lot (luc bo vao va luc chot)",
                gd.contains("ThuocKhoa.giaoDichDuoc(player, idPotion)") && gd.contains("ThuocKhoa.viPham(player, player.tPotions)")
                && gd.contains("ThuocKhoa.viPham(dp, dp.tPotions)"));

        final String kg = doc("src/real/OfflineMap.java");
        Kiem.bang("ky gui: rao vat pham / ngoc kiem gia + cap (3 cho)", 3, dem(kg, "ChongClone.giaKyGuiSai(player, prize_sell"));
        Kiem.dung("ky gui: mua kiem hai ben + cung may", kg.contains("ChongClone.chanChuyen(player, p, player, com.htbb.pokemon.ChongClone.KIEU_KY_GUI"));
        Kiem.dung("ky gui: mua ghi nhat ky", kg.contains("ChongClone.ghi(player, p, com.htbb.pokemon.ChongClone.KIEU_KY_GUI"));
        final int viTriCong = kg.indexOf("ChongClone.chanChuyen(player, p, player");
        final int viTriTru = kg.indexOf("player.subXu(player.sellerInfo.priceSell");
        Kiem.dung("ky gui: cong chay TRUOC khi tru xu", viTriCong > 0 && viTriTru > viTriCong);

        final String map = doc("src/real/Map.java");
        Kiem.dung("thu cung lay xu: chot kiem kich hoat + chong clone", map.contains("ChongClone.chanChuyen(player, p, player, com.htbb.pokemon.ChongClone.KIEU_THU_CUNG"));
        Kiem.dung("thu cung lay xu: chot ghi nhat ky", map.contains("ChongClone.ghi(player, p, com.htbb.pokemon.ChongClone.KIEU_THU_CUNG"));
        final String nt = doc("src/real/MapNhapText.java");
        Kiem.dung("thu cung: hoi gia da kiem cap/tuoi", nt.contains("ChongClone.chanCaHai(player, p, player, com.htbb.pokemon.ChongClone.KIEU_THU_CUNG)"));
        Kiem.dung("thach dau 1-1 cuoc xu: kiem ca hai ben", nt.contains("KIEU_THACH_DAU"));
        Kiem.dung("thach dau nhom cuoc xu: kiem moi thanh vien", doc("src/real/MapXaHoi.java").contains("KIEU_THACH_DAU"));
        final String tt = doc("src/com/htbb/pokemon/NapTienGame.java");
        Kiem.dung("cho trang tri: rao kiem cap/tuoi, mua kiem cung may + ghi",
                tt.contains("ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_TRANG_TRI)")
                && tt.contains("ChongClone.chanChuyen(p, r.nguoiBan") && tt.contains("ChongClone.ghi(p, r.nguoiBan"));
        final String st = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.bang("cho sinh thai: rao / rao trung / giao don / tao don kiem cap", 4,
                dem(st, "ChongClone.chanMotBen(p, ChongClone.KIEU_CHO_SINH_THAI)"));
        Kiem.dung("cho sinh thai: mua kiem cung may + ghi, giao don ghi",
                st.contains("ChongClone.chanChuyen(p, t.idBan") && st.contains("ChongClone.ghi(p, t.idBan")
                && st.contains("ChongClone.ghiNhan(don.idMua"));
        final String nv = doc("src/data/DatabaseNhanVat.java");
        Kiem.bang("gio choi khong con cong lai ca phien moi lan luu (2 ham luu tob_char)", 2,
                dem(nv, "p.layPhutChoiChuaLuu()"));
    }

    // ------------------------------------------------------------------ 7. luong khoa khong chuyen duoc

    static void quetLuongKhoa() {
        // Duong chuyen giua hai nguoi: khong duong nao tru luong khoa ben nay roi cong cho ben kia.
        final String[][] duong = {
            {"src/real/MapMuaBan.java", "giao dich truc tiep"},
            {"src/real/OfflineMap.java", "cho ky gui"},
            {"src/real/CharChuyenLuong.java", "chuyen luong"},
            {"src/com/htbb/pokemon/HeSinhThaiMenu.java", "cho sinh thai"},
            {"src/real/MapDoi.java", "doi / bang (quy, luong bang)"},
        };
        for (final String[] d : duong) {
            final String s = doc(d[0]);
            Kiem.dung("doc duoc " + d[0], s.length() > 100);
            Kiem.sai(d[1] + ": khong cong luong khoa cho nguoi khac (addLuongLock)", s.contains("addLuongLock"));
            Kiem.sai(d[1] + ": khong tru luong khoa (subLuongLock / truLuongKhoaNeuDu)",
                    s.contains("subLuongLock") || s.contains("truLuongKhoaNeuDu"));
        }
        final String tt = doc("src/com/htbb/pokemon/NapTienGame.java");
        final int muaCho = tt.indexOf("static String muaCho(");
        final String thanMuaCho = muaCho < 0 ? "" : tt.substring(muaCho, tt.indexOf("\n    }\n", muaCho));
        Kiem.dung("cho trang tri: mua bang xu, khong dung luong khoa",
                thanMuaCho.contains("truXuNeuDu") && !thanMuaCho.contains("LuongLock") && !thanMuaCho.contains("LuongKhoa"));
        // Chuyen luong chi dung luong MO.
        final String ch = doc("src/real/Char.java");
        final String nhanChuyen = than(ch, "public int addLuongNhanChuyen(");
        final String truChuyen = than(ch, "truLuongChuyenKhoan(");
        Kiem.dung("chuyen luong: cong vao luong mo (khong dong lockLuong)",
                !nhanChuyen.isEmpty() && !nhanChuyen.contains("lockLuong") && !nhanChuyen.contains("LuongLock"));
        Kiem.dung("chuyen luong: tru luong mo (khong dong lockLuong)",
                !truChuyen.isEmpty() && !truChuyen.contains("lockLuong") && !truChuyen.contains("LuongLock"));
        // Vat pham khoa: cho ky gui tu choi, vut ra dat thi huy, ban NPC khong ra xu.
        final String kg = doc("src/real/OfflineMap.java");
        Kiem.dung("ky gui tu choi vat pham khoa", kg.contains("((Item)item).lock == 1"));
        Kiem.dung("ban NPC: vat pham khoa khong doi ra xu", kg.contains("item.lock == 1 && !com.htbb.pokemon.ChongClone.banNpcDoKhoa()"));
        final String map = doc("src/real/Map.java");
        Kiem.dung("ban NPC: nguyen lieu khoa khong doi ra xu", map.contains("typeMaterial == 1 && !com.htbb.pokemon.ChongClone.banNpcDoKhoa()"));
        Kiem.dung("vut ra dat: vat pham khoa bi chan / huy, khong ai nhat duoc", map.contains("lock != 0"));
        // Shop: mua bang luong khoa -> thuoc khoa + vat pham lock 1.
        final String shop = doc("src/real/doBuySpecialItem.java");
        Kiem.dung("shop luong khoa: thuoc moi tang thanh thuoc khoa", shop.contains("ThuocKhoa.sauKhiMua(player, thuocTruoc, luongKhoaTruoc)"));
        Kiem.dung("shop luong khoa: vat pham moi vao tui bi khoa", shop.contains("((Item) o).lock = 1;"));
        // Giftcode an toan chi phat (khong chuyen) luong khoa / do khoa.
        final String gc = doc("src/com/htbb/pokemon/GiftcodeAnToanLogic.java");
        Kiem.dung("giftcode an toan: chi luong khoa / do khoa", gc.length() > 100 && !gc.contains("addXu") && !gc.contains("addLuong("));
        // Giao dich truc tiep: chi thuoc - khong co truong xu / luong nao.
        final String gd = doc("src/real/MapMuaBan.java");
        Kiem.sai("giao dich truc tiep khong chuyen xu", gd.contains("addXu(") || gd.contains("subXu("));
        Kiem.sai("giao dich truc tiep khong chuyen luong", gd.contains("addLuong(") || gd.contains("subLuong("));
    }

    // ------------------------------------------------------------------ 8. cau hinh trung tam

    static void cauHinh() {
        final String[][] khoa = {
            {ChongCloneLogic.K_BAT, "1"}, {ChongCloneLogic.K_CAP, "20"}, {ChongCloneLogic.K_TUOI_TK_GIO, "24"},
            {ChongCloneLogic.K_GIO_CHOI, "10"}, {ChongCloneLogic.K_CUNG_MAY, "2"}, {ChongCloneLogic.K_NGHI_SO_GUI, "3"},
            {ChongCloneLogic.K_GIA_LAN, "10"}, {ChongCloneLogic.K_GIA_GIO, "200"}, {ChongCloneLogic.K_GIA_MIN_PT, "5"},
            {ChongCloneLogic.K_BAN_NPC_DO_KHOA, "0"},
        };
        for (final String[] k : khoa) {
            final com.htbb.cauhinh.DinhNghiaKhoa d = com.htbb.cauhinh.CauHinhTrungTam.dinhNghia(k[0]);
            Kiem.dung("khoa " + k[0] + " co trong danh muc trung tam", d != null);
            if (d != null) {
                Kiem.bang("khoa " + k[0] + " mac dinh", k[1], d.macDinh);
            }
        }
        final CauHinh c = com.htbb.pokemon.ChongClone.cauHinh();
        Kiem.bang("doc cau hinh trung tam = mac dinh logic (cap)", CauHinh.macDinh().capToiThieu, c.capToiThieu);
        Kiem.bang("doc cau hinh trung tam = mac dinh logic (cung may)", CauHinh.macDinh().nvCungMayToiDa, c.nvCungMayToiDa);
        Kiem.sai("mac dinh: cam ban do khoa cho NPC", c.banNpcDoKhoa);
        final String sql = DayDu.nangCap();
        Kiem.dung("htbb_day_du.sql co bang tob_log_chuyen_gia_tri (rong)", DayDu.coBang("tob_log_chuyen_gia_tri")
                && DayDu.soDong("tob_log_chuyen_gia_tri") <= 0);
        Kiem.dung("sql/nang_cap.sql tao bang idempotent",
                sql.contains("CREATE TABLE IF NOT EXISTS tob_log_chuyen_gia_tri"));
        Kiem.dung("bang SQL khop ma Java (cot nghi, bi_chan)", sql.contains("bi_chan") && sql.contains("nghi TINYINT")
                && data.DatabaseChongClone.SQL_TAO_BANG.contains("bi_chan"));
    }

    // ------------------------------------------------------------------ tien ich

    static int dem(final String s, final String x) {
        int n = 0;
        int i = 0;
        while ((i = s.indexOf(x, i)) >= 0) {
            n++;
            i += x.length();
        }
        return n;
    }

    static String than(final String s, final String dau) {
        final int i = s.indexOf(dau);
        if (i < 0) {
            return "";
        }
        final int mo = s.indexOf('{', i);
        int sau = 0;
        for (int j = mo; j < s.length(); j++) {
            if (s.charAt(j) == '{') {
                sau++;
            } else if (s.charAt(j) == '}' && --sau == 0) {
                return s.substring(mo, j);
            }
        }
        return "";
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
