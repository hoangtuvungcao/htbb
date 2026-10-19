import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.NapTienLogic;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.TangToc;
import com.htbb.pokemon.ThongSoLauDai;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Vat pham tang toc + bo tran cap theo mua (18/10/2026, docs/thiet-ke-game.md / 1.8,
 * docs/van-hanh.md): tran diem ngay/tuan chung ban cao cap + ban khoa, hieu ung ve / bua / uy thac, cap
 * toi da, whitelist cua hang luong, khoa noi dung theo cap + cot truyen thay vi theo ngay.
 */
public final class KiemTangToc {

    private KiemTangToc() {
    }

    static final int NGAY = 20261019, TUAN = 2042;

    public static void chay() {
        Kiem.nhom("Tang toc luyen cap (TangToc) + khong tran cap theo mua");
        try {
            tran();
            hieuUng();
            cuaHang();
            khoaNoiDung();
            duLieu();
            doiPhien();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static HoSoLauDai moi() {
        return new HoSoLauDai();
    }

    static void tran() {
        Kiem.bang("tran ngay 60 diem", 60, TangToc.DIEM_NGAY_TOI_DA);
        Kiem.bang("tran tuan 210 diem (3,5 gio EXP)", 210, TangToc.DIEM_TUAN_TOI_DA);
        boolean idDung = true;
        for (int i = 0; i < 4; i++) {
            idDung &= TangToc.ID_CAO_CAP[i] >= 1250 && TangToc.ID_KHOA[i] <= 1257 && TangToc.ID_CAO_CAP[i] != TangToc.ID_KHOA[i];
            idDung &= TangToc.loaiCuaItem(TangToc.ID_CAO_CAP[i]) == i && TangToc.loaiCuaItem(TangToc.ID_KHOA[i]) == i;
        }
        Kiem.dung("ban cao cap / ban khoa: id rieng 1250..1257, cung loai (cung tran)", idDung);
        Kiem.bang("khong phai tang toc -> -1", -1, TangToc.loaiCuaItem(1201));

        final HoSoLauDai h = moi();
        Kiem.bang("ve x1,5 dung duoc", null, TangToc.kiemDung(h, TangToc.VE_15, 50, 100, false, NGAY, TUAN));
        TangToc.ghiDung(h, TangToc.VE_15, NGAY, TUAN);
        TangToc.ghiDung(h, TangToc.VE_15, NGAY, TUAN);
        Kiem.dung("qua 60 phut ve/ngay -> chan", TangToc.kiemDung(h, TangToc.VE_15, 50, 100, false, NGAY, TUAN) != null);
        Kiem.dung("dang chay ve x1,5 -> khong dung x2", TangToc.kiemDung(h, TangToc.VE_2, 50, 100, false, NGAY, TUAN) != null);
        Kiem.bang("diem con lai hom nay = 60 - 30", 30, TangToc.diemConLai(h, NGAY, TUAN));
        Kiem.bang("uy thac dung duoc", null, TangToc.kiemDung(h, TangToc.UY_THAC, 50, 100, false, NGAY, TUAN));
        TangToc.ghiDung(h, TangToc.UY_THAC, NGAY, TUAN);
        TangToc.ghiDung(h, TangToc.UY_THAC, NGAY, TUAN);
        Kiem.dung("54/60 diem: bua (45) bi chan tran ngay", TangToc.kiemDung(h, TangToc.BUA, 50, 100, false, NGAY, TUAN) != null);
        Kiem.bang("ngay moi: bua dung duoc", null, TangToc.kiemDung(h, TangToc.BUA, 50, 100, false, NGAY + 1, TUAN));

        // tran tuan: 7 ngay x 60 diem > 210
        final HoSoLauDai w = moi();
        int dung = 0;
        for (int d = 0; d < 7; d++) {
            for (int k = 0; k < 5; k++) {
                if (TangToc.kiemDung(w, TangToc.UY_THAC, 50, 100, true, NGAY + d, TUAN) == null) {
                    TangToc.ghiDung(w, TangToc.UY_THAC, NGAY + d, TUAN);
                    w.tgUtMs = 0L; // da chay het (gia lap dang nhap lai)
                    w.tgUtMsNgay += 3600000L;
                    dung++;
                }
            }
        }
        Kiem.bang("uy thac co the thang: 4 gio/ngay, tran tuan 210 -> 17 lan (204 diem)", 17, dung);
        Kiem.bang("diem tuan khong vuot tran", 204, w.tgDiemTuan);
        Kiem.bang("tuan moi: dat lai", 60, TangToc.diemConLai(w, NGAY + 7, TUAN + 1));
        final HoSoLauDai u = moi();
        int gio = 0;
        for (int k = 0; k < 5; k++) {
            if (TangToc.kiemDung(u, TangToc.UY_THAC, 50, 100, false, NGAY, TUAN) == null) {
                TangToc.ghiDung(u, TangToc.UY_THAC, NGAY, TUAN);
                gio++;
            }
        }
        Kiem.bang("uy thac khong the thang: 3 gio/ngay", 3, gio);

        final HoSoLauDai b = moi();
        TangToc.ghiDung(b, TangToc.BUA, NGAY, TUAN);
        Kiem.dung("bua the luc 1/ngay", TangToc.kiemDung(b, TangToc.BUA, 50, 100, false, NGAY, TUAN) != null);
        final HoSoLauDai k = moi();
        k.khTuan = TUAN;
        k.khSo = 2;
        Kiem.dung("keo hiem 2/tuan", TangToc.kiemDung(k, TangToc.KEO, 50, 100, false, NGAY, TUAN) != null);
        Kiem.dung("cap toi da: moi vat pham tang toc bi tu choi (khong ban tinh thong)",
                TangToc.kiemDung(moi(), TangToc.VE_2, 100, 100, false, NGAY, TUAN) != null
                        && TangToc.kiemDung(moi(), TangToc.UY_THAC, 100, 100, false, NGAY, TUAN) != null);
        // ban sao luu / doc lai
        final HoSoLauDai h2 = HoSoLauDai.giaiMa(h.maHoa());
        Kiem.bang("ho so: diem tuan luu duoc", h.tgDiemTuan, h2.tgDiemTuan);
        Kiem.bang("ho so: ve dang chay luu duoc", Long.valueOf(h.tgVeMs), Long.valueOf(h2.tgVeMs));
        Kiem.bang("ho so: uy thac cho luu duoc", Long.valueOf(h.tgUtMs), Long.valueOf(h2.tgUtMs));
    }

    static void hieuUng() {
        final HoSoLauDai h = moi();
        TangToc.ghiDung(h, TangToc.VE_2, NGAY, TUAN);
        Kiem.bang("ve x2: 30 phut danh quai", Long.valueOf(30L * 60000L), Long.valueOf(h.tgVeMs));
        Kiem.bang("ve x2: he so 200", 200, TangToc.heSoVeVaTru(h, 60000L));
        Kiem.bang("ve tru theo thoi gian danh quai", Long.valueOf(29L * 60000L), Long.valueOf(h.tgVeMs));
        Kiem.bang("dung yen (0 ms) khong mat ve", 200, TangToc.heSoVeVaTru(h, 0L));
        Kiem.bang("con 29 phut", Long.valueOf(29L * 60000L), Long.valueOf(h.tgVeMs));
        TangToc.heSoVeVaTru(h, 40L * 60000L);
        Kiem.bang("het ve -> 100", 100, TangToc.heSoVeVaTru(h, 60000L));
        Kiem.bang("nhanVe x1,5", 150, TangToc.nhanVe(100, 150));
        Kiem.bang("nhanVe khong ve", 100, TangToc.nhanVe(100, 100));
        Kiem.bang("ve nhan SAU nang luong: 200 x met 5% = 10, x ve x2 = 20", 20,
                TangToc.nhanVe(CapDoLauDai.expHaQuai(200, 100, 0, 5, 100, 100), 200));

        // bua the luc: +60 phut 100% chi EXP, khong tich sang hom sau, he so xu khong tinh bua
        final HoSoLauDai b = moi();
        final long t0 = ngay(2026, 10, 19, 8);
        NangLuong.ghiNhan(b, t0, 90, 180);
        final int hom = NangLuong.ngay(t0);
        TangToc.ghiDung(b, TangToc.BUA, hom, TUAN);
        Kiem.bang("bua: phut 100% = 90 + 60", 150, b.nlPhut100);
        int heSo = 0;
        for (int i = 1; i <= 120; i++) {
            heSo = NangLuong.ghiNhan(b, t0 + i * 60000L, 90, 180);
        }
        Kiem.bang("phut 120 co bua: EXP 100%", 100, heSo);
        Kiem.bang("phut 120 co bua: xu/do roi van 20% (tang toc chi EXP)", 20, NangLuong.heSoXuHienTai(b, t0 + 120 * 60000L));
        NangLuong.ghiNhan(b, ngay(2026, 10, 20, 8), 90, 180);
        Kiem.bang("hom sau: khong tich phan bua (dung 120 > 90 -> chi 90)", 90, b.nlPhut100);
        final HoSoLauDai c = moi();
        TangToc.ghiDung(c, TangToc.BUA, NangLuong.ngay(ngay(2026, 10, 21, 8)), TUAN);
        NangLuong.ghiNhan(c, ngay(2026, 10, 21, 8), 90, 180);
        Kiem.bang("bua dung truoc lan danh quai dau tien: cong khi doi ngay", 90 + 60, c.nlPhut100);

        // uy thac: offline, 20% EXP 1 gio, chi phan da dat truoc
        final HoSoLauDai u = moi();
        TangToc.ghiDung(u, TangToc.UY_THAC, NGAY, TUAN);
        TangToc.ghiDung(u, TangToc.UY_THAC, NGAY, TUAN);
        final long ra = 1000000000000L;
        Kiem.bang("offline 30 phut: tinh 30 phut", Long.valueOf(30L * 60000L), Long.valueOf(TangToc.tinhUyThac(u, ra, ra + 30L * 60000L, NGAY, TUAN)));
        Kiem.bang("con lai 90 phut cho lan sau", Long.valueOf(90L * 60000L), Long.valueOf(u.tgUtMs));
        Kiem.bang("offline 5 gio: chi phan con lai", Long.valueOf(90L * 60000L), Long.valueOf(TangToc.tinhUyThac(u, ra, ra + 5L * 3600000L, NGAY, TUAN)));
        Kiem.bang("EXP uy thac 1 gio = 20% EXP 1 gio danh quai", Long.valueOf(ThongSoLauDai.expMoiGio(60) / 5),
                Long.valueOf(TangToc.expUyThac(3600000L, ThongSoLauDai.expMoiGio(60))));
        Kiem.bang("chua thoat (moc 0) -> khong tinh", Long.valueOf(0L), Long.valueOf(TangToc.tinhUyThac(moi(), 0L, ra, NGAY, TUAN)));
    }

    static void cuaHang() {
        int soTangToc = 0;
        for (final NapTienLogic.Mon m : NapTienLogic.MON_MAC_DINH) {
            if (NapTienLogic.TRAO_TANG_TOC.equals(m.trao)) {
                soTangToc++;
                final int loai = TangToc.loaiCuaMa(m.ma);
                Kiem.dung("cua hang " + m.ma + ": gia + gioi han tuan = TangToc", loai >= 0 && m.giaLuong == TangToc.GIA_LUONG[loai]
                        && m.gioiHan == TangToc.MUA_TUAN_TOI_DA[loai] && !m.giaoDichDuoc && m.hanNgay == 0);
                Kiem.bang("cua hang " + m.ma + " hop le", null, NapTienLogic.kiemMon(m));
            }
        }
        Kiem.bang("5 mon tang toc trong cua hang luong", 5, soTangToc);
        Kiem.dung("tang toc: ma la (khong trong TangToc.MA) bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("ve_exp_10",
                "Vé EXP x10", "tang_toc", NapTienLogic.TRAO_TANG_TOC, 20, 0, 5, 0, false)) != null);
        Kiem.dung("tang toc: sai gia bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("ve_exp_2",
                "Vé EXP x2", "tang_toc", NapTienLogic.TRAO_TANG_TOC, 10, 0, 7, 0, false)) != null);
        Kiem.dung("tang toc: gioi han tuan vuot tran bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("ve_exp_2",
                "Vé EXP x2", "tang_toc", NapTienLogic.TRAO_TANG_TOC, 40, 0, 50, 0, false)) != null);
        Kiem.dung("tang toc: giao dich duoc bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("uy_thac",
                "Phiếu ủy thác", "tang_toc", NapTienLogic.TRAO_TANG_TOC, 15, 0, 17, 0, true)) != null);
        Kiem.dung("ten EXP voi trao trang_tri van bi tu choi", NapTienLogic.kiemMon(new NapTienLogic.Mon("ve_exp_15",
                "Vé EXP x1,5", "tien_ich", NapTienLogic.TRAO_TRANG_TRI, 20, 0, 0, 0, false)) != null);
        Kiem.dung("tang toc khong co chi so (co_chi_so = 1 bi tu choi)", NapTienLogic.kiemMon(new NapTienLogic.Mon("ve_exp_15",
                "Vé EXP x1,5", "tang_toc", NapTienLogic.TRAO_TANG_TOC, 20, 0, 14, 0, false, true)) != null);
        int muaToiDa = 0;
        for (int i = 0; i < TangToc.SO_LOAI; i++) {
            muaToiDa = Math.max(muaToiDa, TangToc.MUA_TUAN_TOI_DA[i] * TangToc.DIEM[i]);
        }
        Kiem.dung("gioi han mua/tuan moi mon <= ~1 tran tuan (khong mua thua)", muaToiDa <= TangToc.DIEM_TUAN_TOI_DA + 30);
        Kiem.bang("the thang: +1 gio uy thac", 1, NapTienLogic.THE_THANG_GIO_UY_THAC_THEM);
    }

    static void khoaNoiDung() {
        final boolean cu = LauDai.tranTheoMua();
        try {
            LauDai.datTranTheoMua(false);
            Kiem.bang("tran theo mua tat: tran = 100 ngay 0", 100, CapDoLauDai.tranCap(0, 100));
            Kiem.dung("tran theo mua tat: ngay_mo khong chan", LauDai.ngayMoNoiDung() > 545);
            Kiem.bang("chuong nam 2 (ngay_mo 365) nhan duoc theo cap", null,
                    NhiemVuLauDai.kiemChinh(91, 91, 365, LauDai.ngayMoNoiDung(), true, 1));
            Kiem.dung("chuong nam 2 van can cap", NhiemVuLauDai.kiemChinh(91, 90, 365, LauDai.ngayMoNoiDung(), true, 1) != null);
            Kiem.dung("Hoenn: cap 84 + chua xong C52.7 -> chan (cot truyen)", BanDoHoenn.lyDoChan(true, 100, 90, false) != null);
            Kiem.bang("Hoenn: cap 90 + xong C52.7 -> qua", null, BanDoHoenn.lyDoChan(true, 100, 90, true));
            Kiem.dung("Hoenn: cap 83 -> chan", BanDoHoenn.lyDoChan(true, 100, 83, true) != null);
            Kiem.bang("Hoenn: ma ket nam 1", "C52.7", BanDoHoenn.MA_KET_NAM_1);
            LauDai.datTranTheoMua(true);
            Kiem.bang("bat lai tran theo mua: ngay 0 -> 60", 60, CapDoLauDai.tranCap(0, 100));
            Kiem.dung("bat lai tran theo mua: Hoenn chan khi tran < 91", BanDoHoenn.lyDoChan(true, 80, 90, true) != null);
        } finally {
            LauDai.datTranTheoMua(cu);
        }
    }

    static void duLieu() throws Exception {
        final java.util.List<java.util.Map<String, String>> nvDs = DayDu.bangThietKe("nhiem_vu_moi.csv");
        final String nv = nvDs.toString();
        Kiem.sai("nhiem vu: het khoa 'cach buoc truoc 7 ngay' (thay bang cap)", nv.contains("cach_buoc_truoc_ngay"));
        Kiem.sai("nhiem vu: khong con chu 'chờ tới ngày thứ'", nv.contains("chờ tới ngày thứ") || nv.contains("Mở vào ngày thứ"));
        final java.util.Map<String, String> zap = DayDu.tim("data_quest_lau_dai", "ma", "HT-ZAP.2");
        Kiem.dung("huyen thoai: buoc sau can cap cao hon (HT-ZAP.2 cap 42)", zap != null && "5502".equals(zap.get("id_so"))
                && "huyen_thoai".equals(zap.get("loai")) && "42".equals(zap.get("cap_toi_thieu")));
        boolean du = true;
        for (int i = 0; i < 4; i++) {
            du &= TangToc.MA[i].equals(DayDu.giaTri("data_vat_pham_lau_dai", "id_item", String.valueOf(TangToc.ID_CAO_CAP[i]), "ma"))
                    && (TangToc.MA[i] + "_khoa").equals(DayDu.giaTri("data_vat_pham_lau_dai", "id_item", String.valueOf(TangToc.ID_KHOA[i]), "ma"));
        }
        Kiem.dung("data_vat_pham_lau_dai: du 8 mau tang toc", du);
        boolean kec = false;
        for (final java.util.Map<String, String> r : DayDu.dong("data_cua_hang_lau_dai")) {
            kec |= "luong_khoa:15".equals(r.get("chi_phi")) && "ITEM:1251:1".equals(r.get("thuong"));
        }
        Kiem.dung("Kecleon ban ban khoa (luong khoa, gioi han tuan)", kec);
        for (final String ma : new String[]{"ve_exp_15", "ve_exp_2", "bua_the_luc", "uy_thac", "keo_hiem"}) {
            final java.util.Map<String, String> r = DayDu.tim("nap_cua_hang", "ma", ma);
            Kiem.dung("nap_cua_hang co " + ma + " (trao tang_toc)", r != null && "tang_toc".equals(r.get("trao")) && "tang_toc".equals(r.get("nhom")));
        }
        Kiem.bang("bang EXP: tong 1->100 ~ 880 gio (x1000)", Boolean.TRUE,
                Boolean.valueOf(Math.abs(ThongSoLauDai.GIO_TICH_LUY_PHAN_NGHIN[100] - 882389) < 20000));
    }

    /** Nhan vat dang o bang EXP phien 1 (co tran mua) -> phien 2: giu cap, giu % (ChuyenDoiLauDai.doiPhienBang). */
    static void doiPhien() {
        final int n = 100;
        final long[] m1 = CapDoLauDai.expMain(n, 1), r1 = CapDoLauDai.expRequire(n, 1);
        final long[] m2 = CapDoLauDai.expMain(n, CapDoLauDai.PHIEN_BANG), r2 = CapDoLauDai.expRequire(n, CapDoLauDai.PHIEN_BANG);
        Kiem.dung("phien 1 khac phien hien tai (bang da doi)", m1[90] != m2[90]);
        boolean giu = true;
        for (final int L : new int[]{1, 16, 39, 40, 60, 85, 91, 99, 100}) {
            final long xp = L >= n ? m1[n - 1] : m1[L - 1] + r1[L] / 2;
            final long moi = CapDoLauDai.chuyenExp(xp, m1, r1, m2, r2);
            giu &= CapDoLauDai.capTuExp(moi, m2) == L;
        }
        Kiem.dung("doi phien bang EXP: giu cap (1, 16, 39, 40, 60, 85, 91, 99, 100)", giu);
        Kiem.bang("cap 1-30 giong nhau o hai phien (tien hoa dau khong doi)", Long.valueOf(m1[29]), Long.valueOf(m2[29]));
    }

    static long ngay(final int y, final int m, final int d, final int h) {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        c.clear();
        c.set(y, m - 1, d, h, 0, 0);
        return c.getTimeInMillis();
    }
}
