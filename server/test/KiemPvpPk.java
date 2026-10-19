import com.htbb.pokemon.DanhHieuLogic;
import com.htbb.pokemon.LuatPk;
import com.htbb.pokemon.ThangPvp;
import com.htbb.pokemon.XepHangPvp;
import io.Message;
import io.Session;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import real.Char;
import real.DanhHieuGame;
import real.DiemMocNhiemVu;
import real.XepHangPvpGame;

/**
 * 0.9.7 sua loi tu ra soat loi choi:
 * (1) thuong cuoi mua PvP (ThangPvp.LUONG_BAC duoc dung, khong phat hai lan),
 * (2) Elo chi trong tran xep hang (ghep theo dai cap + Elo, khong cung tai khoan / may / IP, giam dan khi gap lai, huy
 *     tran qua ngan / khong danh tra),
 * (3) luat PK (bo mien VIP, khong tut cap, nguong khoa PK toi duoc, nghiep phai theo gio online, chenh cap 10, thi tran
 *     an toan),
 * (4) diem moc cho moi kieu muc tieu nhiem vu (data_nhiem_vu_kieu).
 */
public final class KiemPvpPk {

    private KiemPvpPk() {
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("PvP xep hang / thuong mua / luat PK / diem moc nhiem vu (0.9.7)");
        luatPk();
        xepHang();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        try {
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            tranGame();
            diemMoc();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            XepHangPvpGame.xoaHet();
            DanhHieuGame.datMau(new ArrayList<DanhHieuLogic.Mau>(), null);
            com.htbb.pokemon.LauDai.dat(false);
        }
        nguon();
    }

    // ------------------------------------------------------------------ (3) luat PK

    static void luatPk() {
        // cap 30: moc dau cap 1.000.000, can 200.000 de len 31
        Kiem.bang("ten do mat 1% EXP can cua cap", Long.valueOf(2000), Long.valueOf(LuatPk.expMat(1_100_000L, 200_000L, 1_000_000L, true)));
        Kiem.bang("vo toi mat 0,1%", Long.valueOf(200), Long.valueOf(LuatPk.expMat(1_100_000L, 200_000L, 1_000_000L, false)));
        Kiem.bang("sat moc dau cap: chi mat phan tren moc (khong tut cap)", Long.valueOf(500),
                Long.valueOf(LuatPk.expMat(1_000_500L, 200_000L, 1_000_000L, true)));
        Kiem.bang("dung moc dau cap: mat 0", Long.valueOf(0), Long.valueOf(LuatPk.expMat(1_000_000L, 200_000L, 1_000_000L, true)));
        Kiem.bang("EXP 0: mat 0", Long.valueOf(0), Long.valueOf(LuatPk.expMat(0L, 200_000L, 0L, true)));
        Kiem.dung("nguong khoa PK nam trong tam short (truoc 319000 > tran 32000)", LuatPk.NGUONG_KHOA_PK > 0
                && LuatPk.NGUONG_KHOA_PK < LuatPk.TRAN_NGHIEP && LuatPk.TRAN_NGHIEP <= Short.MAX_VALUE);
        Kiem.dung("khoaPk toi duoc", LuatPk.khoaPk(LuatPk.NGUONG_KHOA_PK) && !LuatPk.khoaPk(LuatPk.NGUONG_KHOA_PK - 1));
        Kiem.bang("nghiep phai 5 / phut online", Integer.valueOf(95), Integer.valueOf(LuatPk.phaiNghiep(100, 1)));
        Kiem.bang("mot lan giet (+100) phai het sau 20 phut online", Integer.valueOf(0), Integer.valueOf(LuatPk.phaiNghiep(100, 20)));
        Kiem.bang("nghiep khong am", Integer.valueOf(0), Integer.valueOf(LuatPk.phaiNghiep(3, 10)));
        Kiem.dung("thi tran: chan PK", LuatPk.chan(true, 50, 50, false, 0) != null);
        Kiem.dung("thi tran: chan ca khi muc tieu ten do", LuatPk.chan(true, 50, 50, true, 0) != null);
        Kiem.dung("chenh 11 cap (thap hon): chan", LuatPk.chan(false, 50, 39, false, 0) != null);
        Kiem.dung("chenh dung 10 cap: cho", LuatPk.chan(false, 50, 40, false, 0) == null);
        Kiem.dung("muc tieu thap hon nhung dang ten do / bat PK: cho (tu chiu)", LuatPk.chan(false, 50, 20, true, 0) == null);
        Kiem.dung("danh nguoi CAO hon: cho", LuatPk.chan(false, 20, 60, false, 0) == null);
        Kiem.dung("nghiep qua nang: chan danh nguoi vo toi", LuatPk.chan(false, 50, 50, false, LuatPk.NGUONG_KHOA_PK) != null);
    }

    // ------------------------------------------------------------------ (2) xep hang thuan

    static final class LsGia implements XepHangPvp.LichSu {
        long gan;
        int so;

        public long ganNhat(final int a, final int b) {
            return gan;
        }

        public int soTran24h(final int a, final int b) {
            return so;
        }
    }

    static XepHangPvp.Cho cho(final int id, final int cap, final int elo, final long vao, final int user, final String may, final String ip) {
        return new XepHangPvp.Cho(id, cap, elo, vao, new XepHangPvp.Cum(user, may, ip));
    }

    static void xepHang() {
        final long bay = 10_000_000L;
        final LsGia ls = new LsGia();
        Kiem.bang("cua so Elo luc vao hang", Integer.valueOf(100), Integer.valueOf(XepHangPvp.cuaSo(0)));
        Kiem.bang("cua so rong dan (60 giay)", Integer.valueOf(300), Integer.valueOf(XepHangPvp.cuaSo(60_000)));
        Kiem.bang("cua so toi da 400", Integer.valueOf(400), Integer.valueOf(XepHangPvp.cuaSo(3_600_000)));
        final XepHangPvp.Cho a = cho(1, 40, 1000, bay, 1, "m1", "1.1.1.1");
        Kiem.dung("cung dai cap, Elo gan, khac tai khoan: ghep", XepHangPvp.ghepDuoc(a, cho(2, 45, 1050, bay, 2, "m2", "2.2.2.2"), bay, ls));
        Kiem.sai("lech 15 cap: khong ghep", XepHangPvp.ghepDuoc(a, cho(2, 55, 1000, bay, 2, "m2", "2.2.2.2"), bay, ls));
        Kiem.dung("cap 40 gap cap 50 (lech dung 10): ghep", XepHangPvp.ghepDuoc(a, cho(2, 50, 1000, bay, 2, "m2", "2.2.2.2"), bay, ls));
        Kiem.sai("Elo lech 300 luc moi vao: khong ghep", XepHangPvp.ghepDuoc(a, cho(2, 40, 1300, bay, 2, "m2", "2.2.2.2"), bay, ls));
        Kiem.dung("Elo lech 300 sau 1 phut cho ca hai: ghep", XepHangPvp.ghepDuoc(cho(1, 40, 1000, bay - 60_000, 1, "m1", "1.1.1.1"),
                cho(2, 40, 1300, bay - 60_000, 2, "m2", "2.2.2.2"), bay, ls));
        Kiem.sai("cung tai khoan: khong ghep", XepHangPvp.ghepDuoc(a, cho(2, 40, 1000, bay, 1, "m2", "2.2.2.2"), bay, ls));
        Kiem.sai("cung thiet bi: khong ghep", XepHangPvp.ghepDuoc(a, cho(2, 40, 1000, bay, 2, "m1", "2.2.2.2"), bay, ls));
        Kiem.sai("cung IP: khong ghep", XepHangPvp.ghepDuoc(a, cho(2, 40, 1000, bay, 2, "m2", "1.1.1.1"), bay, ls));
        Kiem.dung("IP rong (proxy tin cay) khong tinh trung", new XepHangPvp.Cum(1, "a", "").trung(new XepHangPvp.Cum(2, "b", "")) == false);
        ls.gan = bay - 60_000L;
        Kiem.sai("vua dau voi nhau 1 phut truoc: khong ghep lai", XepHangPvp.ghepDuoc(a, cho(2, 40, 1000, bay, 2, "m2", "2.2.2.2"), bay, ls));
        ls.gan = bay - XepHangPvp.CHAN_LAP_MS - 1;
        ls.so = 3;
        Kiem.sai("da 3 tran cung doi thu trong 24 gio: khong ghep nua (he so 0)", XepHangPvp.ghepDuoc(a, cho(2, 40, 1000, bay, 2, "m2", "2.2.2.2"), bay, ls));
        ls.so = 0;
        ls.gan = 0;
        final List<XepHangPvp.Cho> hang = new ArrayList<XepHangPvp.Cho>();
        hang.add(cho(1, 40, 1000, bay - 3000, 1, "m1", "ip1"));
        hang.add(cho(2, 40, 1090, bay - 2000, 2, "m2", "ip2"));
        hang.add(cho(3, 41, 1010, bay - 1000, 3, "m3", "ip3"));
        hang.add(cho(4, 80, 1000, bay, 4, "m4", "ip4"));
        final List<XepHangPvp.Cho[]> cap = XepHangPvp.ghep(hang, bay, ls);
        Kiem.bang("ghep: 1 cap (nguoi cap 80 cho tiep)", Integer.valueOf(1), Integer.valueOf(cap.size()));
        Kiem.dung("ghep nguoi cho lau nhat voi Elo gan nhat (1 voi 3)", cap.size() == 1 && cap.get(0)[0].id == 1 && cap.get(0)[1].id == 3);
        Kiem.bang("he so lan 1", Integer.valueOf(1000), Integer.valueOf(XepHangPvp.heSoLap(0)));
        Kiem.bang("he so lan 2", Integer.valueOf(500), Integer.valueOf(XepHangPvp.heSoLap(1)));
        Kiem.bang("he so lan 3", Integer.valueOf(250), Integer.valueOf(XepHangPvp.heSoLap(2)));
        Kiem.bang("he so lan 4+", Integer.valueOf(0), Integer.valueOf(XepHangPvp.heSoLap(3)));
        final int goc = ThangPvp.eloMoi(1000, 1000, 1) - 1000;
        Kiem.bang("Elo he so 1000 = goc", Integer.valueOf(1000 + goc), Integer.valueOf(XepHangPvp.eloMoi(1000, 1000, 1, 1000)));
        Kiem.bang("Elo he so 500 = nua", Integer.valueOf(1000 + goc / 2), Integer.valueOf(XepHangPvp.eloMoi(1000, 1000, 1, 500)));
        Kiem.bang("Elo he so 0 = khong doi", Integer.valueOf(1000), Integer.valueOf(XepHangPvp.eloMoi(1000, 1000, 1, 0)));
        Kiem.bang("huy: tran qua ngan", "qua_ngan", XepHangPvp.lyDoHuy(5, 500, 1000, false));
        Kiem.bang("huy: ben thua khong danh tra (< 10% HP)", "khong_danh_tra", XepHangPvp.lyDoHuy(60, 50, 1000, false));
        Kiem.bang("huy: cung may", "cung_tai_khoan_thiet_bi_ip", XepHangPvp.lyDoHuy(60, 500, 1000, true));
        Kiem.dung("hop le: 60 giay, ben thua gay 20%", XepHangPvp.lyDoHuy(60, 200, 1000, false) == null);
        // (1) thuong mua
        Kiem.bang("thuong mua: duoi 10 tran = khong", "0/0", s(XepHangPvp.thuongMua(1700, 9)));
        Kiem.bang("thuong mua: Poke Ball = 0", "0/0", s(XepHangPvp.thuongMua(1100, 30)));
        Kiem.bang("thuong mua: Great 15 luong khoa", "1/" + ThangPvp.LUONG_BAC[1], s(XepHangPvp.thuongMua(1250, 10)));
        Kiem.bang("thuong mua: Ultra 40", "2/" + ThangPvp.LUONG_BAC[2], s(XepHangPvp.thuongMua(1450, 10)));
        Kiem.bang("thuong mua: Master 100", "3/" + ThangPvp.LUONG_BAC[3], s(XepHangPvp.thuongMua(1650, 10)));
        Kiem.dung("thuong mua toi da 100 luong khoa (khong lam phat)", ThangPvp.LUONG_BAC[ThangPvp.LUONG_BAC.length - 1] <= 100);
    }

    static String s(final int[] a) {
        return a[0] + "/" + a[1];
    }

    // ------------------------------------------------------------------ (2) tran trong game (nhan vat gia)

    static int idKe = 71000;

    static Char nv(final String may, final String ip) {
        final Char p = new Char(null);
        final Session se = new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
            }
        };
        se.IMEI = may;
        se.ip = ip;
        p.setSession(se);
        p.getSession().banClient = 907;
        p.charname = "kiempvp" + idKe;
        p.charDBID = idKe++;
        p.userID = p.charDBID;
        p.lvDetail.lv = 40;
        p.maxhp = 1000;
        p.hp = 1000;
        p.isBot = -1;
        return p;
    }

    static void tranGame() {
        XepHangPvpGame.xoaHet();
        final Char a = nv("mA", "10.0.0.1"), b = nv("mB", "10.0.0.2"), c = nv("mC", "10.0.0.3");
        Kiem.bang("giet ngoai tran xep hang: khong Elo (-1)", Integer.valueOf(-1), Integer.valueOf(XepHangPvpGame.ketQua(a, c)));
        final long bay = System.currentTimeMillis();
        XepHangPvpGame.datTran(a.charDBID, b.charDBID, bay - 3000L);
        Kiem.dung("cung tran", XepHangPvpGame.cungTran(a, b) && !XepHangPvpGame.cungTran(a, c));
        XepHangPvpGame.khiSatThuong(b, a, 500);
        Kiem.bang("tran 3 giay: huy (0)", Integer.valueOf(0), Integer.valueOf(XepHangPvpGame.ketQua(a, b)));
        Kiem.sai("huy xong: het tran", XepHangPvpGame.cungTran(a, b));
        XepHangPvpGame.xoaHet();
        XepHangPvpGame.datTran(a.charDBID, b.charDBID, bay - 60_000L);
        XepHangPvpGame.khiSatThuong(b, a, 20);
        XepHangPvpGame.khiSatThuong(c, a, 900); // nguoi ngoai tran: khong tinh
        Kiem.bang("ben thua gay 2% HP (nguoi ngoai danh khong tinh): huy", Integer.valueOf(0), Integer.valueOf(XepHangPvpGame.ketQua(a, b)));
        XepHangPvpGame.xoaHet();
        XepHangPvpGame.datTran(a.charDBID, b.charDBID, bay - 60_000L);
        XepHangPvpGame.khiSatThuong(b, a, 150);
        XepHangPvpGame.khiSatThuong(b, a, 100);
        Kiem.bang("tran hop le lan dau: he so 1000", Integer.valueOf(1000), Integer.valueOf(XepHangPvpGame.ketQua(a, b)));
        XepHangPvpGame.datTran(a.charDBID, b.charDBID, bay - 60_000L);
        XepHangPvpGame.khiSatThuong(b, a, 300);
        Kiem.bang("gap lai cung doi thu: he so 500", Integer.valueOf(500), Integer.valueOf(XepHangPvpGame.ketQua(a, b)));
        final Char d = nv("mA", "10.0.0.9"); // cung thiet bi voi a (nick phu)
        XepHangPvpGame.datTran(a.charDBID, d.charDBID, bay - 60_000L);
        XepHangPvpGame.khiSatThuong(d, a, 300);
        Kiem.bang("nick phu cung thiet bi (lot vao tran): huy", Integer.valueOf(0), Integer.valueOf(XepHangPvpGame.ketQua(a, d)));
        XepHangPvpGame.xoaHet();
    }

    // ------------------------------------------------------------------ (4) diem moc

    static void diemMoc() {
        Kiem.bang("vung loai Bulbasaur", Integer.valueOf(0), Integer.valueOf(DiemMocNhiemVu.vungLoai(1)));
        Kiem.bang("vung loai Chikorita", Integer.valueOf(1), Integer.valueOf(DiemMocNhiemVu.vungLoai(152)));
        Kiem.bang("vung loai Treecko", Integer.valueOf(2), Integer.valueOf(DiemMocNhiemVu.vungLoai(252)));
        Kiem.bang("vung loai ngoai", Integer.valueOf(-1), Integer.valueOf(DiemMocNhiemVu.vungLoai(0)));
        // diem moc -> NhiemVuEngine.suKien -> DanhHieuGame (bo dem) -> mo danh hieu: chuoi mot su kien that
        final List<DanhHieuLogic.Mau> ds = new ArrayList<DanhHieuLogic.Mau>();
        final String[] kieu = {"dao_dat", "nau_an", "lam_vuon", "ap_trung", "do_vui", "dua_pokemon", "cho_ban", "nang_cap", "pokedex_vung",
            "giao_hang", "dua_thu", "ho_tong"};
        for (int i = 0; i < kieu.length; i++) {
            ds.add(new DanhHieuLogic.Mau(9900 + i, "kiem_" + kieu[i], "Kiểm " + kieu[i], "", "", 0, "sao", "", 0, kieu[i], "", 1, 0, 0,
                    "thanh_tuu", i));
        }
        ds.add(new DanhHieuLogic.Mau(9950, "kiem_bo", "Kiểm bộ", "", "", 0, "sao", "", 0, "bo_trang_bi", "", 1, 0, 0, "thanh_tuu", 99));
        DanhHieuGame.datMau(ds, null);
        final Char p = nv("mX", "10.0.0.50");
        p.allDanhHieu = new java.util.Vector<data.DanhHieu>();
        p.idEffDanhHieu = -1;
        DiemMocNhiemVu.khiDao(p, 3);
        DiemMocNhiemVu.khiNau(p, 1, true);
        DiemMocNhiemVu.khiNau(p, 2, false); // che thuoc khong tinh nau_an
        DiemMocNhiemVu.khiThuHoach(p, 5);
        DiemMocNhiemVu.khiNoTrung(p, 25);
        DiemMocNhiemVu.khiDoVui(p);
        DiemMocNhiemVu.khiDua(p, 2);
        DiemMocNhiemVu.khiBan(p);
        DiemMocNhiemVu.khiLoaiMoi(p, 25);
        DiemMocNhiemVu.khiGiaoVatPham(p, new short[]{9});
        DiemMocNhiemVu.khiHoTong(p);
        int du = 0;
        final StringBuilder thieu = new StringBuilder();
        for (int i = 0; i < kieu.length; i++) {
            if ("nang_cap".equals(kieu[i])) {
                continue; // mac do can trang bi that (Map.doUseItemGoc) - kiem bang nguon ben duoi
            }
            boolean co = false;
            for (final data.DanhHieu d : p.allDanhHieu) {
                co |= d.ideff == 9900 + i;
            }
            if (co) {
                du++;
            } else {
                thieu.append(kieu[i]).append(' ');
            }
        }
        Kiem.bang("11 diem moc ban dung kieu vao NhiemVuEngine.suKien (thieu: " + thieu + ")", Integer.valueOf(11), Integer.valueOf(du));
        DanhHieuGame.xoaTrangThai(p);
        suKienVatPham(p);
    }

    /** Bang data_su_kien_vat_pham doc tu sql/them_moc_nhiem_vu.sql: {ma, id}. */
    static List<Object[]> bangSuKien() {
        final List<Object[]> ds = new ArrayList<Object[]>();
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\('([a-z0-9]+)', (\\d+), '", java.util.regex.Pattern.MULTILINE)
                .matcher(DayDu.phanNangCap("them_moc_nhiem_vu.sql"));
        while (m.find()) {
            ds.add(new Object[]{m.group(1), Integer.valueOf(m.group(2))});
        }
        return ds;
    }

    /** su_kien_vat_pham: chi vat pham cua su kien DANG BAT, tham so = id vat pham, dem dung so luong. */
    static void suKienVatPham(final Char p) {
        final List<Object[]> bang = bangSuKien();
        Kiem.dung("doc duoc data_su_kien_vat_pham (" + bang.size() + " dong)", bang.size() >= 15);
        real.SuKienVatPham.datBang(bang);
        final java.util.Map<String, Boolean> cu = new java.util.HashMap<String, Boolean>();
        cu.put("halloween", Boolean.TRUE);
        cu.put("trungthu", Boolean.FALSE);
        cu.put("tet2017", Boolean.FALSE);
        real.SuKien.dat(cu);
        try {
            Kiem.bang("Keo (137) thuoc Halloween dang bat", "halloween", real.SuKienVatPham.suKienDangBat(137, real.SuKien::bat));
            Kiem.dung("Dau xanh (122) cua Trung Thu / Tet dang tat: khong tinh", real.SuKienVatPham.suKienDangBat(122, real.SuKien::bat) == null);
            Kiem.dung("binh HP (1) khong phai vat pham su kien", real.SuKienVatPham.suKienDangBat(1, real.SuKien::bat) == null);
            // chuoi that: nhat vat pham -> NhiemVuEngine.suKien -> bo dem danh hieu (mau nghe su_kien_vat_pham tham so 137, can 3)
            final List<DanhHieuLogic.Mau> ds = new ArrayList<DanhHieuLogic.Mau>();
            ds.add(new DanhHieuLogic.Mau(9960, "kiem_keo", "Kiểm kẹo", "", "", 0, "sao", "", 0, "su_kien_vat_pham", "137", 3, 0, 0, "su_kien", 1));
            ds.add(new DanhHieuLogic.Mau(9961, "kiem_bat_ky", "Kiểm bất kỳ", "", "", 0, "sao", "", 0, "su_kien_vat_pham", "", 1, 0, 0, "su_kien", 2));
            DanhHieuGame.datMau(ds, null);
            final Char q = nv("mY", "10.0.0.51");
            q.allDanhHieu = new java.util.Vector<data.DanhHieu>();
            q.idEffDanhHieu = -1;
            DiemMocNhiemVu.khiNhanVatPhamSuKien(q, 122, 5); // su kien tat: khong tinh
            Kiem.dung("vat pham cua su kien dang tat: khong tinh", !coDh(q, 9961));
            DiemMocNhiemVu.khiNhanVatPhamSuKien(q, 138, 1); // Bi ngo Halloween
            Kiem.dung("vat pham bat ky cua su kien dang bat: tinh", coDh(q, 9961));
            Kiem.sai("Bi ngo khong tinh cho muc tieu Keo (137)", coDh(q, 9960));
            DiemMocNhiemVu.khiNhanVatPhamSuKien(q, 137, 2);
            Kiem.sai("2/3 Keo: chua du", coDh(q, 9960));
            DiemMocNhiemVu.khiNhanVatPhamSuKien(q, 137, 1);
            Kiem.dung("3/3 Keo (dem dung so luong nhat): du", coDh(q, 9960));
            DanhHieuGame.xoaTrangThai(q);
        } finally {
            real.SuKien.dat(new java.util.HashMap<String, Boolean>());
            real.SuKienVatPham.datBang(new ArrayList<Object[]>());
        }
        // bang du lieu khop code roi: moi id trong cac mang idpt cua khoi roi su kien (Monster.java) co dong
        final java.util.Set<Integer> coDong = new java.util.HashSet<Integer>();
        for (final Object[] d : bang) {
            coDong.add((Integer) d[1]);
        }
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile("^\\s*final (?:byte|short)\\[\\] idpt\\d* = \\{([^}]*)\\}",
                java.util.regex.Pattern.MULTILINE).matcher(doc("src/real/Monster.java"));
        final StringBuilder thieu = new StringBuilder();
        int n = 0;
        while (m.find()) {
            for (final String x : m.group(1).replace("(byte)", "").split(",")) {
                n++;
                if (!coDong.contains(Integer.valueOf(Integer.parseInt(x.trim())))) {
                    thieu.append(x.trim()).append(' ');
                }
            }
        }
        Kiem.dung("doc duoc id roi su kien trong Monster.java (" + n + ")", n >= 10);
        Kiem.bang("moi vat pham roi su kien co trong data_su_kien_vat_pham (thieu: " + thieu + ")", "", thieu.toString());
        Kiem.dung("Vu Lan (106) va World Cup (161) co dong", coDong.contains(Integer.valueOf(106)) && coDong.contains(Integer.valueOf(161)));
    }

    static boolean coDh(final Char p, final int id) {
        for (final data.DanhHieu d : p.allDanhHieu) {
            if (d.ideff == id) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ nguon (diem goi that)

    static void nguon() {
        final String ch = doc("src/real/Char.java");
        final int i = ch.indexOf("public boolean isNotDownExp()");
        Kiem.dung("VIP 3 khong con mien mat EXP (isNotDownExp = false)", i > 0 && ch.substring(i, i + 80).contains("return false;"));
        Kiem.sai("het nguong 319000 khong toi duoc", doc("src/real/MapDanhNhau.java").contains("319000"));
        Kiem.sai("nghiep khong con -200 moi ngay dang nhap", doc("src/real/CharNapDuLieu.java").contains("self.killer -= 200"));
        Kiem.dung("getXPLost qua LuatPkGame (khong tut cap)", doc("src/real/LevelDetail.java").contains("LuatPkGame.expMat(this, killer)"));
        Kiem.dung("nghiep phai moi phut (CharVongLap)", doc("src/real/CharVongLap.java").contains("LuatPkGame.nhipPhut(self)"));
        Kiem.dung("luat PK o ca hai duong danh nguoi", doc("src/real/MapDanhNhau.java").contains("LuatPkGame.chan(map, p, c)")
                && doc("src/real/Region.java").contains("LuatPkGame.chan(this.map, p, c)"));
        Kiem.dung("sat thuong tran xep hang ghi o ca hai duong", doc("src/real/MapDanhNhau.java").contains("XepHangPvpGame.khiSatThuong")
                && doc("src/real/Region.java").contains("XepHangPvpGame.khiSatThuong"));
        final String tp = doc("src/real/ThangPvpGame.java");
        Kiem.dung("ThangPvpGame: Elo chi khi XepHangPvpGame.ketQua >= 1", tp.contains("XepHangPvpGame.ketQua(thang, thua)") && tp.contains("if (heSo < 0)"));
        final String xh = doc("src/real/XepHangPvpGame.java");
        Kiem.dung("thuong mua: INSERT IGNORE truoc, chi phat khi chen duoc (khong phat hai lan)",
                xh.contains("INSERT IGNORE INTO tob_pvp_thuong") && xh.indexOf("chen != 1") > xh.indexOf("INSERT IGNORE INTO tob_pvp_thuong")
                        && xh.indexOf("chen != 1") < xh.indexOf("LUONGKHOA:"));
        Kiem.dung("ThangPvp.LUONG_BAC duoc dung", doc("src/com/htbb/pokemon/XepHangPvp.java").contains("ThangPvp.LUONG_BAC"));
        Kiem.dung("NPC Dau truong co muc Dau xep hang", doc("src/real/cmd/RequestNPCInfoHandler.java").contains("\"Đấu xếp hạng\"")
                && doc("src/real/MapDoMenu.java").contains("XepHangPvpGame.dangKy(player)"));
        final String sql = DayDu.phanNangCap("sua_pvp_pk.sql");
        Kiem.dung("sql/sua_pvp_pk.sql idempotent (2 bang IF NOT EXISTS, khong DROP)", sql.split("CREATE TABLE IF NOT EXISTS").length - 1 == 2
                && !sql.contains("DROP"));
        // (4) moi kieu muc tieu trong data_nhiem_vu_kieu (tru dan_y khong con dung) co diem goi trong ma
        final String nvSql = DayDu.sql();
        final int a = nvSql.indexOf("INSERT INTO `data_nhiem_vu_kieu` VALUES");
        final StringBuilder ma = new StringBuilder();
        try (java.util.stream.Stream<java.nio.file.Path> s = Files.walk(Paths.get("src"))) {
            s.filter(x -> x.toString().endsWith(".java")).forEach(x -> ma.append(doc(x.toString())));
        } catch (final Exception e) {
            Kiem.dung("duyet src: " + e, false);
        }
        final String toanBo = ma.toString();
        int soKieu = 0;
        final StringBuilder khong = new StringBuilder();
        if (a > 0) {
            // ban chup CSDL: mot dong INSERT cho ca bang -> doc theo bang (cot dau = ma kieu)
            final String cot0 = DayDu.cot("data_nhiem_vu_kieu").get(0);
            for (final java.util.Map<String, String> r : DayDu.dong("data_nhiem_vu_kieu")) {
                final String k = r.get(cot0);
                if ("dan_y".equals(k)) {
                    continue;
                }
                soKieu++;
                if (!toanBo.contains("\"" + k + "\"")) {
                    khong.append(k).append(' ');
                }
            }
        }
        Kiem.dung("doc duoc danh muc kieu muc tieu (" + soKieu + ")", soKieu >= 40);
        Kiem.bang("moi kieu muc tieu co diem goi trong ma (thieu: " + khong + ")", "", khong.toString());
        Kiem.dung("su_kien_vat_pham: khong con ti le uoc luong 35%", !toanBo.contains("TI_LE_SU_KIEN") && !toanBo.contains("khiHaQuaiSuKien"));
        Kiem.dung("nhat vat pham (ca hai duong doGetPotion) qua DiemMocNhiemVu.khiNhanVatPhamSuKien",
                doc("src/real/Map.java").split("DiemMocNhiemVu.khiNhanVatPhamSuKien\\(player, pt.getType\\(\\), pt.quantity\\)").length - 1 == 2);
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        Kiem.dung("cho_ban: nguoi ban offline ghi tob_nhiem_vu_cho", hst.contains("real.DiemMocNhiemVu.ghiChoNguoiBan(idBan, ghiChu)"));
        final String dm = doc("src/real/DiemMocNhiemVu.java");
        Kiem.dung("cho_ban: ap mot lan (UPDATE da_ap 0 -> 1 doi dung 1 dong moi cong)",
                dm.contains("WHERE id = ? AND da_ap = 0") && dm.contains("Long.valueOf(d[0])) == 1"));
        Kiem.dung("cho_ban: ap luc vao ban do", doc("src/real/DanhHieuGame.java").contains("DiemMocNhiemVu.apChoCho(p)"));
        final String sq = DayDu.phanNangCap("them_moc_nhiem_vu.sql");
        Kiem.dung("sql/them_moc_nhiem_vu.sql idempotent", sq.split("CREATE TABLE IF NOT EXISTS").length - 1 == 2 && !sq.contains("DROP")
                && sq.contains("ON DUPLICATE KEY UPDATE"));
        int nCauCa = 0;
        int k = 0;
        while ((k = toanBo.indexOf("suKien(p, \"cau_ca\"", k)) >= 0) {
            nCauCa++;
            k++;
        }
        Kiem.bang("diem moc cau_ca chi MOT cho (khong dem hai lan)", Integer.valueOf(1), Integer.valueOf(nCauCa));
    }
}
