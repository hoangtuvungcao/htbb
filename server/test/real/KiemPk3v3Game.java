package real;

import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.PhongRieng;
import com.htbb.pokemon.Pk3v3;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PK 3 VS 3 BUOI TOI - luong GAME real.Pk3v3Game voi nhan vat gia (KiemVeDichChuyen.NV), san gia, kho bo nho, loa gia, dong ho
 * co dinh: gio mo / dong + loa, hang cho (mot minh / to doi, cap, ban), ghep 3v3 + dua vao san, luot / TAI KHOAN (nick phu
 * cung tai khoan het luot), danh / loai / het nguoi, bo cuoc, rot mang (doi danh thieu nguoi), het gio, thuong + chong cay
 * (nhanh, treo, gap lai, het thang co thuong), bot HLV AI, chot top ngay / danh hieu tuan mot lan, thuong khi dang nhap,
 * menu So tay. Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemPk3v3Game {

    private KiemPk3v3Game() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void dung(final String m, final boolean v) {
        BAO.bang(m, Boolean.TRUE, Boolean.valueOf(v));
    }

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    // ================================================================== gia

    static final int MAP_SAN = 4382, MAP_NHA = 9;
    static final Map<Integer, Char> ONLINE = new HashMap<Integer, Char>();
    static final PhongRieng.Kho KHU = new PhongRieng.Kho(20);
    static final List<String> LOA = new ArrayList<String>();
    static long gio;

    static final Pk3v3Game.San SAN = new Pk3v3Game.San() {
        public boolean co() {
            return true;
        }

        public int mapId() {
            return MAP_SAN;
        }

        public int muon() {
            return KHU.muon();
        }

        public void tra(final int khu) {
            KHU.tra(khu);
        }

        public void dua(final Char c, final int khu, final int ben, final int thuTu) {
            c.mapID = MAP_SAN;
            c.region = khu;
        }

        public void ve(final Char c, final PhongRiengGame.ViTri v) {
            c.mapID = (short) (v == null ? MAP_NHA : v.mapId);
            c.region = v == null ? 0 : v.khu;
        }

        public Char online(final int charId) {
            return ONLINE.get(Integer.valueOf(charId));
        }
    };

    /** Kho bo nho (thay CSDL). */
    static final class KhoNho implements Pk3v3Game.Kho {
        final Map<String, int[]> luot = new HashMap<String, int[]>();
        final Map<String, int[]> diem = new LinkedHashMap<String, int[]>();
        final Map<String, Integer> gap = new HashMap<String, Integer>();
        final List<String> tran = new ArrayList<String>();
        final java.util.Set<String> chot = new java.util.HashSet<String>();
        final Map<String, Object[]> thuong = new LinkedHashMap<String, Object[]>();

        public void taoBang() {
        }

        public int[] napLuot(final int ngay, final int userId) {
            final int[] r = luot.get(ngay + ":" + userId);
            return r == null ? new int[3] : new int[]{r[0], r[1], r[2]};
        }

        public int napLuongTuan(final int tuan, final int userId) {
            int s = 0;
            for (final Map.Entry<String, int[]> e : luot.entrySet()) {
                if (e.getKey().endsWith(":" + userId) && e.getValue()[3] == tuan) {
                    s += e.getValue()[2];
                }
            }
            return s;
        }

        public void congLuot(final int ngay, final int tuan, final int userId, final int dTran, final int dThang, final int dLuong) {
            final int[] r = luot.computeIfAbsent(ngay + ":" + userId, k -> new int[]{0, 0, 0, tuan});
            r[0] += dTran;
            r[1] += dThang;
            r[2] += dLuong;
        }

        public List<Pk3v3Game.DongDiem> napDiem(final int tuTuan) {
            return new ArrayList<Pk3v3Game.DongDiem>();
        }

        public void congDiem(final int ngay, final int tuan, final Pk3v3.Diem d) {
            final int[] r = diem.computeIfAbsent(ngay + ":" + d.charId, k -> new int[5]);
            r[0] += d.diem;
            r[1] += d.thang;
            r[2] += d.hoa;
            r[3] += d.thua;
            r[4] += d.hopLe;
        }

        public Map<String, Integer> napGap(final int ngay) {
            return new HashMap<String, Integer>();
        }

        public void congGap(final int ngay, final int a, final int b) {
            gap.merge(ngay + ":" + Pk3v3Game.cap(a, b), Integer.valueOf(1), Integer::sum);
        }

        public void ghiTran(final int ngay, final int tuan, final int bac, final String doiA, final String doiB, final int ketQua,
                final int giay, final String lyDo) {
            tran.add(doiA + "|" + doiB + "|" + ketQua + "|" + giay + "|" + lyDo);
        }

        public boolean chot(final String loai, final int ky) {
            return chot.add(loai + ":" + ky);
        }

        public void ghiThuong(final String loai, final int ky, final int charId, final int hang, final int luongKhoa, final int danhHieu,
                final int phut) {
            thuong.putIfAbsent(loai + ":" + ky + ":" + charId, new Object[]{loai, Integer.valueOf(ky), Integer.valueOf(hang),
                Integer.valueOf(luongKhoa), Integer.valueOf(danhHieu), Integer.valueOf(phut), Integer.valueOf(charId), Boolean.FALSE});
        }

        public List<Object[]> thuongCho(final int charId) {
            final List<Object[]> ds = new ArrayList<Object[]>();
            for (final Object[] r : thuong.values()) {
                if (((Integer) r[6]).intValue() == charId && !((Boolean) r[7]).booleanValue()) {
                    ds.add(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5]});
                }
            }
            return ds;
        }

        public boolean daNhan(final String loai, final int ky, final int charId) {
            final Object[] r = thuong.get(loai + ":" + ky + ":" + charId);
            if (r == null || ((Boolean) r[7]).booleanValue()) {
                return false;
            }
            r[7] = Boolean.TRUE;
            return true;
        }
    }

    static KhoNho kho;

    static long luc(final int ngay, final int h, final int m, final int s) {
        final Calendar c = Calendar.getInstance(com.htbb.pokemon.LichNam.MUI_GIO);
        c.clear();
        c.set(2026, Calendar.NOVEMBER, ngay, h, m, s);
        return c.getTimeInMillis();
    }

    static int idKe = 777100;

    static KiemVeDichChuyen.NV nv(final int cap, final int userId) {
        final KiemVeDichChuyen.NV p = new KiemVeDichChuyen.NV(idKe++, cap);
        p.userID = userId;
        p.partyID = -1;
        p.mapID = MAP_NHA;
        p.map = KiemVeDichChuyen.banDo(MAP_NHA, MAP_NHA);
        p.x = 100;
        p.y = 100;
        p.allDanhHieu = new java.util.Vector<data.DanhHieu>();
        ONLINE.put(Integer.valueOf(p.charDBID), p);
        return p;
    }

    static List<KiemVeDichChuyen.NV> sau(final int cap) {
        final List<KiemVeDichChuyen.NV> l = new ArrayList<KiemVeDichChuyen.NV>();
        for (int i = 0; i < 6; i++) {
            l.add(nv(cap, 50000 + idKe));
        }
        return l;
    }

    static void moi() {
        Pk3v3Game.xoaHet();
        ONLINE.clear();
        LOA.clear();
        for (int k = 1; k < KHU.soKhu(); k++) {
            KHU.tra(k);
        }
        kho = new KhoNho();
        Pk3v3Game.KHO = kho;
    }

    /** Xoa trang thai hoat dong + kho, giu nhan vat online. */
    static void lamMoiTrangThai() {
        Pk3v3Game.xoaHet();
        for (int k = 1; k < KHU.soKhu(); k++) {
            KHU.tra(k);
        }
        kho = new KhoNho();
        Pk3v3Game.KHO = kho;
    }

    static void cauHinh(final String... kv) {
        final Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, m, true);
    }

    static Pk3v3Game.Tran tranDauTien() {
        return Pk3v3Game.TRAN.isEmpty() ? null : Pk3v3Game.TRAN.values().iterator().next();
    }

    // ================================================================== chay

    public static void chay() {
        final Pk3v3Game.San sanCu = Pk3v3Game.SAN;
        final Pk3v3Game.Kho khoCu = Pk3v3Game.KHO;
        final java.util.function.Consumer<String> loaCu = Pk3v3Game.LOA;
        final java.util.function.LongSupplier gioCu = Pk3v3Game.GIO;
        final java.util.function.Function<Char, List<Char>> doiCu = Pk3v3Game.DOI;
        final java.util.function.Predicate<Char> truongCu = Pk3v3Game.TRUONG;
        final boolean lauDaiCu = LauDai.bat();
        final GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new GemTemplate[0]; // new Char() can (nhu KiemChongVatPham)
        }
        LauDai.dat(true);
        Pk3v3Game.SAN = SAN;
        Pk3v3Game.LOA = s -> LOA.add(s);
        Pk3v3Game.GIO = () -> gio;
        try {
            cauHinh();
            gioMoVaLoa();
            vaoHang();
            toDoi();
            ghepVaTran();
            boCuocRotMang();
            hetGio();
            chongCay();
            luotTaiKhoan();
            bot();
            chotNgayTuan();
            menuSoTay();
        } catch (final Throwable e) {
            e.printStackTrace();
            bang("KiemPk3v3Game khong nem loi", "", String.valueOf(e));
        } finally {
            Pk3v3Game.xoaHet();
            Pk3v3Game.SAN = sanCu;
            Pk3v3Game.KHO = khoCu;
            Pk3v3Game.LOA = loaCu;
            Pk3v3Game.GIO = gioCu;
            Pk3v3Game.DOI = doiCu;
            Pk3v3Game.TRUONG = truongCu;
            LauDai.dat(lauDaiCu);
            real.Map.gemTemplate = gemCu;
            com.htbb.cauhinh.CauHinhTrungTam.datChoTest(null, null, false);
            ONLINE.clear();
        }
    }

    // ------------------------------------------------------------------ gio mo + loa

    static void gioMoVaLoa() {
        moi();
        final KiemVeDichChuyen.NV p = nv(30, 9001);
        gio = luc(5, 18, 30, 0);
        String loi = Pk3v3Game.vaoHang(p, false, gio);
        dung("18:30 chua mo: khong vao hang (" + loi + ")", loi != null && loi.contains("19:00–23:00"));
        Pk3v3Game.nhip(gio); // lan dau sau khoi dong: khong loa
        bang("lan nhip dau: khong loa", Integer.valueOf(0), Integer.valueOf(LOA.size()));
        gio = luc(5, 19, 0, 1);
        Pk3v3Game.nhip(gio);
        dung("19:00 loa toan may chu: da mo", LOA.size() == 1 && LOA.get(0).contains("đã mở") && LOA.get(0).contains("19:00–23:00"));
        bang("19:00 vao hang duoc", null, Pk3v3Game.vaoHang(p, false, gio));
        dung("dang cho", Pk3v3Game.dangCho(p));
        gio = luc(5, 23, 0, 0);
        Pk3v3Game.nhip(gio);
        dung("23:00 loa da dong", LOA.get(LOA.size() - 1).contains("đã đóng"));
        dung("23:00 hang cho bi xoa, bao nguoi cho", !Pk3v3Game.dangCho(p) && p.baoCuoi().contains("Đã rời hàng chờ"));
        cauHinh(Pk3v3.K_GIO_MO, "8", Pk3v3.K_GIO_DONG, "10");
        dung("admin doi gio (8-10): 9:00 mo", Pk3v3Game.dangMo(luc(5, 9, 0, 0)));
        dung("admin doi gio: 20:00 dong", !Pk3v3Game.dangMo(luc(5, 20, 0, 0)));
        cauHinh(Pk3v3.K_BAT, "0");
        dung("pk3v3.bat = 0: tam dong", !Pk3v3Game.dangMo(luc(5, 20, 0, 0)) && Pk3v3Game.vaoHang(p, false, luc(5, 20, 0, 0)).contains("tạm đóng"));
        cauHinh();
    }

    // ------------------------------------------------------------------ hang cho

    static void vaoHang() {
        moi();
        gio = luc(5, 20, 0, 0);
        final KiemVeDichChuyen.NV thap = nv(15, 9101);
        String loi = Pk3v3Game.vaoHang(thap, false, gio);
        dung("cap 15 < 20: khong vao (" + loi + ")", loi != null && loi.contains("cần cấp 20"));
        final KiemVeDichChuyen.NV gucc = nv(30, 9102);
        gucc.hp = 0;
        loi = Pk3v3Game.vaoHang(gucc, false, gio);
        dung("dang guc: khong vao", loi != null && loi.contains("gục"));
        final KiemVeDichChuyen.NV p = nv(30, 9103);
        bang("vao hang", null, Pk3v3Game.vaoHang(p, false, gio));
        loi = Pk3v3Game.vaoHang(p, false, gio);
        dung("vao hai lan: tu choi", loi != null && loi.contains("đã ở trong hàng chờ"));
        final KiemVeDichChuyen.NV ma = nv(30, 9104);
        ma.isBot = 0;
        dung("nhan vat may (isBot) khong vao", Pk3v3Game.vaoHang(ma, false, gio) != null);
        bang("roi hang", Boolean.TRUE, Boolean.valueOf(Pk3v3Game.roiHang(p.charDBID, null)));
        dung("da roi", !Pk3v3Game.dangCho(p));
        // cho qua lau
        bang("vao lai", null, Pk3v3Game.vaoHang(p, false, gio));
        Pk3v3Game.nhip(gio + (Pk3v3.GIAY_CHO_TOI_DA + 1) * 1000L);
        dung("cho qua 10 phut: tu roi hang + bao", !Pk3v3Game.dangCho(p) && p.baoCuoi().contains("Đã rời hàng chờ"));
        // thoat game khi dang cho
        bang("vao lai lan nua", null, Pk3v3Game.vaoHang(p, false, gio + 700000L));
        ONLINE.remove(Integer.valueOf(p.charDBID));
        Pk3v3Game.nhip(gio + 701000L);
        dung("thoat game: roi hang", !Pk3v3Game.dangCho(p));
    }

    static void toDoi() {
        moi();
        gio = luc(5, 20, 0, 0);
        final KiemVeDichChuyen.NV a = nv(45, 9201), b = nv(41, 9202), c = nv(30, 9203), d = nv(30, 9204);
        for (final Char x : new Char[]{a, b, c, d}) {
            x.partyID = 7;
        }
        final List<Char> doi3 = new ArrayList<Char>();
        doi3.add(a);
        doi3.add(b);
        doi3.add(c);
        Pk3v3Game.DOI = p -> doi3;
        Pk3v3Game.TRUONG = p -> p == a;
        String loi = Pk3v3Game.vaoHang(b, true, gio);
        dung("khong phai doi truong: tu choi (" + loi + ")", loi != null && loi.contains("đội trưởng"));
        bang("doi truong cho ca to doi 3 nguoi vao", null, Pk3v3Game.vaoHang(a, true, gio));
        final Pk3v3Game.Cho cho = Pk3v3Game.choCua(c.charDBID);
        dung("ca 3 trong cung mot nhom", cho != null && cho.nhom.so() == 3 && cho == Pk3v3Game.choCua(a.charDBID));
        bang("bac cua to doi = cap cao nhat (45 -> bac 1)", Integer.valueOf(1), Integer.valueOf(cho == null ? -9 : cho.nhom.bac));
        dung("moi thanh vien duoc bao", c.baoCuoi().contains("cả tổ đội 3 người"));
        Pk3v3Game.roiHang(b.charDBID, null);
        dung("mot nguoi roi = ca nhom roi", !Pk3v3Game.dangCho(a) && !Pk3v3Game.dangCho(c));
        final List<Char> doi4 = new ArrayList<Char>(doi3);
        doi4.add(d);
        Pk3v3Game.DOI = p -> doi4;
        loi = Pk3v3Game.vaoHang(a, true, gio);
        dung("to doi 4 nguoi: tu choi", loi != null && loi.contains("tối đa 3"));
        Pk3v3Game.DOI = p -> java.util.Collections.<Char>singletonList(p);
        loi = Pk3v3Game.vaoHang(a, true, gio);
        dung("to doi chi con 1 nguoi online: tu choi", loi != null && loi.contains("ít nhất 2"));
        final KiemVeDichChuyen.NV khongDoi = nv(30, 9205);
        loi = Pk3v3Game.vaoHang(khongDoi, true, gio);
        dung("chua co to doi: goi y vao mot minh", loi != null && loi.contains("chưa có tổ đội"));
    }

    // ------------------------------------------------------------------ ghep + tran

    /** Vao 6 nguoi, ghep ngay; tra tran (null neu khong ghep). */
    static Pk3v3Game.Tran mo6(final List<KiemVeDichChuyen.NV> l, final long t) {
        for (final KiemVeDichChuyen.NV p : l) {
            final String loi = Pk3v3Game.vaoHang(p, false, t);
            if (loi != null) {
                bang("vao hang " + p.charname, null, loi);
            }
        }
        Pk3v3Game.ghepNgay(t);
        return Pk3v3Game.tranCua(l.get(0).charDBID);
    }

    /** p gay {@code lan} don chuan ~50 phan nghin cho c (qua hook Region). */
    static void danh(final Char p, final Char c, final int lan) {
        for (int i = 0; i < lan && c.hp > 0; i++) {
            if (!Pk3v3Game.choDanh(p, c)) {
                return;
            }
            final int dam = Pk3v3Game.satThuong(p, c, (long) p.maxhp * 50L / 1000L * 100L / com.htbb.pokemon.SatThuongLauDai.PVP_PHAN_TRAM);
            c.hp -= dam;
            Pk3v3Game.sauDon(p, c);
        }
    }

    static void ghepVaTran() {
        moi();
        gio = luc(5, 20, 0, 0);
        final List<KiemVeDichChuyen.NV> l = sau(30);
        final Pk3v3Game.Tran t = mo6(l, gio);
        dung("6 nguoi cung bac: ghep 1 tran 3 dau 3", t != null && t.a.size() == 3 && t.b.size() == 3);
        if (t == null) {
            return;
        }
        dung("hang cho trong sau khi ghep", Pk3v3Game.HANG.isEmpty());
        boolean vao = true;
        for (final Char c : l) {
            vao &= c.mapID == MAP_SAN && c.region == t.khu && c.hp == c.maxhp;
        }
        dung("ca 6 dua vao cung khu san, day HP", vao);
        bang("luot tai khoan +1 moi nguoi", Integer.valueOf(1), Integer.valueOf(Pk3v3Game.luot(t.ngay, l.get(0).userID)[0]));
        final KiemVeDichChuyen.NV a1 = l.get(0), a2 = l.get(1), a3 = l.get(2), b1 = l.get(3), b2 = l.get(4), b3 = l.get(5);
        dung("trong 5 giay chuan bi: chua danh duoc", !Pk3v3Game.choDanh(a1, b1));
        gio += 6000L;
        dung("het chuan bi: danh doi thu duoc", Pk3v3Game.choDanh(a1, b1));
        dung("khong danh dong doi", !Pk3v3Game.choDanh(a1, a2));
        // ben B danh tra mot it (khong phai tran nhuong), roi A ha het B
        danh(b1, a1, 3);
        danh(b2, a2, 2);
        gio += 30000L;
        danh(a1, b1, 40);
        dung("het HP: bi loai, HP giu 1 (khong guc)", t.loai.contains(Integer.valueOf(b1.charDBID)) && b1.hp == 1);
        dung("bi loai khong danh / bi danh nua", !Pk3v3Game.choDanh(b1, a1) && !Pk3v3Game.choDanh(a2, b1));
        danh(a2, b2, 40);
        dung("con 1 nguoi: tran chua ket thuc", t.ketQua == 0);
        danh(a1, b3, 40);
        bang("doi B het nguoi: doi A thang", Integer.valueOf(Pk3v3.A_THANG), Integer.valueOf(t.ketQua));
        bang("thang + co danh: +1 luong khoa", Integer.valueOf(1), Integer.valueOf(a1.lockLuong));
        bang("thang nhung khong danh (treo may): 0", Integer.valueOf(0), Integer.valueOf(a3.lockLuong));
        dung("treo may: bao ly do (" + a3.baoCuoi() + ")", a3.baoCuoi().contains("không gây sát thương"));
        bang("thua: 0 luong", Integer.valueOf(0), Integer.valueOf(b1.lockLuong));
        final List<Pk3v3.Diem> bang = Pk3v3Game.xepNgay(t.ngay);
        dung("bang ngay: thang 3 diem dung dau, nguoi treo khong co ten", !bang.isEmpty() && bang.get(0).diem == 3
                && Pk3v3Game.DIEM.get(Integer.valueOf(t.ngay)).get(Integer.valueOf(a3.charDBID)).hopLe == 0);
        dung("nhat ky tran: A thang, hop le (" + kho.tran + ")", kho.tran.size() == 1 && kho.tran.get(0).contains("|1|") && kho.tran.get(0).endsWith("|"));
        Pk3v3Game.nhip(gio + 5000L);
        dung("4 giay sau: ve cho cu, tra khu", a1.mapID == MAP_NHA && b3.mapID == MAP_NHA && Pk3v3Game.TRAN.isEmpty()
                && !KHU.dangDung(t.khu));
    }

    static void boCuocRotMang() {
        moi();
        gio = luc(5, 20, 10, 0);
        final List<KiemVeDichChuyen.NV> l = sau(30);
        final Pk3v3Game.Tran t = mo6(l, gio);
        if (t == null) {
            bang("bo cuoc: co tran", "co", "khong");
            return;
        }
        gio += 6000L;
        final KiemVeDichChuyen.NV a1 = l.get(0), a2 = l.get(1), a3 = l.get(2), b1 = l.get(3);
        ONLINE.remove(Integer.valueOf(a2.charDBID)); // rot mang
        Pk3v3Game.nhip(gio);
        dung("rot mang: bi loai, doi danh tiep thieu nguoi", t.loai.contains(Integer.valueOf(a2.charDBID)) && t.ketQua == 0);
        dung("dong doi duoc bao", a1.baoCuoi().contains("thiếu người"));
        Pk3v3Game.doMenu(a3, Pk3v3Game.MENU_BO, 0); // bo cuoc qua menu (the -112 / So tay)
        dung("bo cuoc: bi loai", t.loai.contains(Integer.valueOf(a3.charDBID)) && t.ketQua == 0);
        a1.mapID = MAP_NHA; // roi san
        Pk3v3Game.nhip(gio + 1000L);
        bang("ca doi A het nguoi: B thang", Integer.valueOf(Pk3v3.B_THANG), Integer.valueOf(t.ketQua));
        bang("thang vi doi thu bo / roi (khong danh, tran nhanh): khong luong", Integer.valueOf(0), Integer.valueOf(b1.lockLuong));
        dung("ly do tran ghi nhat ky (nhanh)", kho.tran.get(0).endsWith("|nhanh"));
    }

    static void hetGio() {
        moi();
        gio = luc(5, 20, 20, 0);
        final List<KiemVeDichChuyen.NV> l = sau(30);
        final Pk3v3Game.Tran t = mo6(l, gio);
        if (t == null) {
            bang("het gio: co tran", "co", "khong");
            return;
        }
        gio += 6000L;
        danh(l.get(0), l.get(3), 4);
        danh(l.get(4), l.get(1), 3);
        Pk3v3Game.nhip(t.batDau + (Pk3v3.GIAY_TRAN + Pk3v3.GIAY_CHUAN_BI) * 1000L + 10L);
        bang("het gio: doi con nhieu % HP hon thang", Integer.valueOf(Pk3v3.A_THANG), Integer.valueOf(t.ketQua));
        bang("het gio, du giay, ben thua co danh: thang co danh +1", Integer.valueOf(1), Integer.valueOf(l.get(0).lockLuong));
    }

    // ------------------------------------------------------------------ chong cay

    /** Danh mot tran tron ven: doi A ha doi B (ca hai ben deu danh), A tu id 0..2. */
    static Pk3v3Game.Tran tranThang(final List<KiemVeDichChuyen.NV> l, final List<KiemVeDichChuyen.NV> b, final long t0) {
        gio = t0;
        for (final KiemVeDichChuyen.NV x : l) {
            Pk3v3Game.vaoHang(x, false, gio);
        }
        for (final KiemVeDichChuyen.NV x : b) {
            Pk3v3Game.vaoHang(x, false, gio);
        }
        Pk3v3Game.ghepNgay(gio);
        final Pk3v3Game.Tran t = Pk3v3Game.tranCua(l.get(0).charDBID);
        if (t == null) {
            return null;
        }
        final List<Char> ben = new ArrayList<Char>();
        final List<Char> thu = new ArrayList<Char>();
        for (final Integer id : t.a) {
            ben.add(ONLINE.get(id));
        }
        for (final Integer id : t.b) {
            thu.add(ONLINE.get(id));
        }
        final boolean aLaL = t.a.contains(Integer.valueOf(l.get(0).charDBID));
        final List<Char> thang = aLaL ? ben : thu;
        final List<Char> thua = aLaL ? thu : ben;
        gio += 6000L;
        for (int i = 0; i < 3; i++) {
            danh(thua.get(i), thang.get(i), 1);
        }
        gio += 25000L;
        for (int i = 0; i < 3; i++) {
            danh(thang.get(i), thua.get(i), 40);
        }
        Pk3v3Game.nhip(gio + 5000L);
        return t;
    }

    static void chongCay() {
        moi();
        final List<KiemVeDichChuyen.NV> a = new ArrayList<KiemVeDichChuyen.NV>(), b = new ArrayList<KiemVeDichChuyen.NV>();
        for (int i = 0; i < 3; i++) {
            a.add(nv(30, 60000 + i));
            b.add(nv(30, 61000 + i));
        }
        final long t0 = luc(5, 20, 30, 0);
        for (int lan = 0; lan < 4; lan++) {
            final Pk3v3Game.Tran t = tranThang(a, b, t0 + lan * 300000L);
            dung("tran cung doi thu lan " + (lan + 1) + " ghep duoc", t != null);
        }
        bang("3 tran thang dau co thuong, lan 4 gap lai qua 3 lan: khong luong", Integer.valueOf(3), Integer.valueOf(a.get(0).lockLuong));
        final Pk3v3.Diem d = Pk3v3Game.DIEM.get(Integer.valueOf(Pk3v3.ngay(t0))).get(Integer.valueOf(a.get(0).charDBID));
        bang("gap lai: tran 4 khong tinh diem (3 tran x 3 diem)", Integer.valueOf(9), Integer.valueOf(d == null ? -1 : d.diem));
        // het tran thang co thuong / ngay (gap lai noi rong de chi con luat 3 thang / ngay)
        lamMoiTrangThai();
        cauHinh(Pk3v3.K_GAP_LAI, "50");
        for (int lan = 0; lan < 4; lan++) {
            tranThang(a, b, t0 + lan * 300000L);
        }
        bang("chi 3 tran thang dau moi ngay co luong khoa (tai khoan)", Integer.valueOf(3 + 3), Integer.valueOf(a.get(0).lockLuong));
        final Pk3v3.Diem d2 = Pk3v3Game.DIEM.get(Integer.valueOf(Pk3v3.ngay(t0))).get(Integer.valueOf(a.get(0).charDBID));
        bang("het thuong ngay: van tinh diem xep hang", Integer.valueOf(12), Integer.valueOf(d2 == null ? -1 : d2.diem));
        bang("kho: 3 thang co thuong, 3 luong", "4/3/3", kho.luot.get(Pk3v3.ngay(t0) + ":" + a.get(0).userID)[0] + "/"
                + kho.luot.get(Pk3v3.ngay(t0) + ":" + a.get(0).userID)[1] + "/" + kho.luot.get(Pk3v3.ngay(t0) + ":" + a.get(0).userID)[2]);
        cauHinh();
    }

    // ------------------------------------------------------------------ luot / tai khoan

    static void luotTaiKhoan() {
        moi();
        cauHinh(Pk3v3.K_TRAN_NGAY, "1");
        gio = luc(5, 21, 0, 0);
        final List<KiemVeDichChuyen.NV> l = sau(30);
        final Pk3v3Game.Tran t = mo6(l, gio);
        dung("tran dau trong ngay ghep duoc", t != null);
        if (t != null) {
            gio += 6000L;
            for (int i = 0; i < 3; i++) {
                Pk3v3Game.boCuoc(l.get(3 + i), gio);
            }
            Pk3v3Game.nhip(gio + 5000L);
        }
        // nick phu: nhan vat khac cung tai khoan voi l[0]
        final KiemVeDichChuyen.NV phu = nv(30, l.get(0).userID);
        ONLINE.remove(Integer.valueOf(l.get(0).charDBID));
        final String loi = Pk3v3Game.vaoHang(phu, false, gio + 10000L);
        dung("tai khoan het luot (1/ngay): nhan vat khac cung tai khoan cung het (" + loi + ")", loi != null && loi.contains("tính theo tài khoản"));
        dung("ngay hom sau: lai co luot", Pk3v3Game.vaoHang(phu, false, luc(6, 20, 0, 0)) == null);
        cauHinh();
        bang("mac dinh 500 tran / ngay / tai khoan", Integer.valueOf(500), Integer.valueOf(Pk3v3Game.tranNgay()));
    }

    // ------------------------------------------------------------------ bot

    static void bot() {
        moi();
        final List<KiemVeDichChuyen.NV> a = new ArrayList<KiemVeDichChuyen.NV>(), b = new ArrayList<KiemVeDichChuyen.NV>();
        for (int i = 0; i < 3; i++) {
            a.add(nv(30, 70000 + i));
            b.add(nv(30, 71000 + i));
        }
        a.get(1).laHlvAi = true;
        final Pk3v3Game.Tran t = tranThang(a, b, luc(5, 21, 30, 0));
        dung("bot (HLV AI) duoc vao hang / ghep nhu nguoi", t != null);
        bang("bot thang: khong luong khoa", Integer.valueOf(0), Integer.valueOf(a.get(1).lockLuong));
        bang("nguoi that cung doi: +1", Integer.valueOf(1), Integer.valueOf(a.get(0).lockLuong));
        boolean coBot = false;
        for (final Pk3v3.Diem d : Pk3v3Game.xepNgay(Pk3v3.ngay(luc(5, 21, 30, 0)))) {
            coBot |= d.charId == a.get(1).charDBID;
        }
        dung("bot khong vao bang xep hang", !coBot);
    }

    // ------------------------------------------------------------------ chot ngay / tuan + dang nhap

    static void chotNgayTuan() {
        moi();
        final int ngay = Pk3v3.ngay(luc(5, 20, 0, 0));
        final int tuan = Pk3v3.tuan(luc(5, 20, 0, 0));
        final List<KiemVeDichChuyen.NV> l = new ArrayList<KiemVeDichChuyen.NV>();
        for (int i = 0; i < 4; i++) {
            final KiemVeDichChuyen.NV p = nv(30, 80000 + i);
            l.add(p);
            final Pk3v3.Diem d = new Pk3v3.Diem(p.charDBID);
            d.userId = p.userID;
            d.ten = p.charname;
            d.diem = 30 - i * 3;
            d.hopLe = 10 + i;
            d.luc = i;
            Pk3v3Game.bangNgay(ngay, tuan).put(Integer.valueOf(p.charDBID), d);
        }
        Pk3v3Game.bangNgay(ngay, tuan).get(Integer.valueOf(l.get(2).charDBID)).hopLe = 4; // chua du 5 tran: khong thuong top
        Pk3v3Game.ngayTruoc = ngay;
        Pk3v3Game.daNapDiem = true;
        Pk3v3Game.moTruoc = Boolean.TRUE;
        gio = luc(5, 23, 0, 5);
        Pk3v3Game.nhip(gio);
        bang("chot ngay: hang 1 +3, hang 2 +2, hang 3 chua du tran 0", "3/2/0/0", l.get(0).lockLuong + "/" + l.get(1).lockLuong + "/"
                + l.get(2).lockLuong + "/" + l.get(3).lockLuong);
        dung("loa top ngay", LOA.get(LOA.size() - 1).startsWith("Top " + Pk3v3.TEN));
        Pk3v3Game.DA_CHOT.clear(); // nhu khoi dong lai: moc CSDL chan phat lan hai
        Pk3v3Game.chotNgay(ngay);
        bang("chot lai (khoi dong lai): khong phat lan hai", Integer.valueOf(3), Integer.valueOf(l.get(0).lockLuong));
        // tuan: thuong cho nguoi offline, nhan khi dang nhap mot lan
        ONLINE.remove(Integer.valueOf(l.get(0).charDBID));
        Pk3v3Game.chotTuan(tuan);
        bang("chot tuan: nguoi offline chua nhan danh hieu", Integer.valueOf(0), Integer.valueOf(l.get(0).allDanhHieu.size()));
        dung("chot tuan: hang 2 online nhan Dau si", l.get(1).allDanhHieu.size() == 1 && l.get(1).allDanhHieu.get(0).ideff == Pk3v3.DH_DAU_SI);
        Pk3v3Game.khiDangNhap(l.get(0));
        dung("dang nhap: nhan Vua PK 3 vs 3 (7 ngay)", l.get(0).allDanhHieu.size() == 1 && l.get(0).allDanhHieu.get(0).ideff == Pk3v3.DH_VUA
                && l.get(0).allDanhHieu.get(0).timeExpire > 0L);
        Pk3v3Game.khiDangNhap(l.get(0));
        bang("dang nhap lan hai: khong nhan lai", Integer.valueOf(1), Integer.valueOf(l.get(0).allDanhHieu.size()));
        dung("loa vua tuan", LOA.get(LOA.size() - 1).contains(l.get(0).charname));
        // tran luong tuan: luong top ngay tinh vao tran 25
        bang("luong tuan tai khoan hang 1 = 3", Integer.valueOf(3), Integer.valueOf(Pk3v3Game.luongTuan(tuan, l.get(0).userID)[0]));
    }

    // ------------------------------------------------------------------ menu

    static void menuSoTay() throws Exception {
        moi();
        gio = luc(5, 20, 0, 0);
        final KiemVeDichChuyen.NV p = nv(30, 90001);
        GymKiem.datHoSo(p, new com.htbb.pokemon.HoSoLauDai());
        Pk3v3Game.moMenu(p);
        Object[] m = p.menuCuoi();
        dung("dang mo: menu vao hang (menu -527/" + Pk3v3Game.MENU_MO + ")", m != null && ((Integer) m[1]).intValue() == Pk3v3Game.MENU_MO
                && "Vào hàng chờ một mình".equals(((String[]) m[2])[0]));
        SoTayLauDai.doMenu(p, Pk3v3Game.MENU_MO, 0);
        dung("So tay -> vao hang mot minh", Pk3v3Game.dangCho(p));
        Pk3v3Game.moMenu(p);
        m = p.menuCuoi();
        dung("dang cho: menu Roi hang", m != null && ((Integer) m[1]).intValue() == Pk3v3Game.MENU_CHO && "Rời hàng chờ".equals(((String[]) m[2])[0]));
        SoTayLauDai.doMenu(p, Pk3v3Game.MENU_CHO, 0);
        dung("roi hang qua menu", !Pk3v3Game.dangCho(p));
        gio = luc(5, 12, 0, 0);
        Pk3v3Game.moMenu(p);
        m = p.menuCuoi();
        dung("ban ngay: chi xem bang / luat", m != null && ((Integer) m[1]).intValue() == Pk3v3Game.MENU_DONG);
        SoTayLauDai.moMenu(p);
        m = p.menuCuoi();
        dung("So tay goc: muc 12 = PK 3 vs 3 buổi tối", m != null && ((String[]) m[2]).length > SoTayLauDai.MUC_PK3V3
                && Pk3v3.TEN.equals(((String[]) m[2])[SoTayLauDai.MUC_PK3V3]));
        final int truoc = p.goi.size();
        SoTayLauDai.doMenu(p, 0, SoTayLauDai.MUC_PK3V3);
        dung("So tay muc 12 mo PK 3 vs 3", p.goi.size() > truoc && ((Integer) p.menuCuoi()[1]).intValue() == Pk3v3Game.MENU_DONG);
        dung("NPC Dau truong hien muc khi bat", Pk3v3Game.hienNpc());
    }
}
