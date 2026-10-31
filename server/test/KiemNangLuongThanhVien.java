import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.TangToc;
import com.htbb.pokemon.TuDong;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Chu game 31/10/2026 - nang luong huan luyen (giu nguyen duong cong 90 phut 100%, bac met 120:20,120:5, roi 0%):
 *   1. Bua the luc (+60 phut BAC BUA: EXP 50%, xu 30%, cong don, KHONG gioi han mua / dung, khong tinh tran diem): Cua hang
 *      luong (1254, 30 luong) + Kecleon (1255, 25 luong khoa). Truoc (cung ngay 31/10): Cua hang luong 1254 + Kecleon (ban khoa
 *      1255, 45 luong khoa, 2 / tuan); dung = 45 diem tang toc (chung tran 60 / ngay, 210 / tuan), 1 / ngay.
 *   2. Thanh vien HTBB / The thang / Ve mua cao cap: moi goi +30 phut 100% / ngay (khoa danh muc), cong don toi da +90,
 *      chot luc khoi tao ngay, KHONG tich sang hom sau, chi EXP (xu / do roi theo phut goc); So tay hien chi tiet.
 *   3. Tu dong danh: nang luong 0% -> KHONG dung, bao MOT lan / ngay; gio mien phi van tinh, kho gio treo KHONG bi tru
 *      (het gio mien phi thi dung, giu kho).
 */
public final class KiemNangLuongThanhVien {

    private KiemNangLuongThanhVien() {
    }

    static final int NGAY = 20261031;
    static final long GIO = 3600000L;
    static final long PHUT = 60000L;

    public static void main(final String[] args) {
        try {
            com.htbb.dulieu.DuLieuSql.napTuDumpDayDu();
        } catch (final Throwable ignored) {
        }
        chay();
        System.exit(Kiem.tongKet());
    }

    public static void chay() {
        Kiem.nhom("Nang luong huan luyen: Bua the luc o cua hang, thuong Thanh vien / The thang / Ve mua, Tu dong luc 0%");
        try {
            bua();
            thuongThanhVien();
            danhMuc();
            tuDong();
            noiVaoGame();
            taiLieu();
        } catch (final Throwable e) {
            e.printStackTrace(System.out);
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static long luc(final int y, final int m, final int d, final int h) {
        final java.util.Calendar c = com.htbb.pokemon.DongHo.lich();
        c.clear();
        c.set(y, m - 1, d, h, 0, 0);
        return c.getTimeInMillis();
    }

    // ------------------------------------------------------------------ 1. bua the luc

    static void bua() {
        final int b = TangToc.BUA;
        Kiem.bang("bua: ban cao cap 1254 / ban khoa 1255", "1254/1255", TangToc.ID_CAO_CAP[b] + "/" + TangToc.ID_KHOA[b]);
        Kiem.bang("bua: gia cao cap 30 luong", 30, TangToc.GIA_LUONG[b]);
        Kiem.bang("bua: khong gioi han mua / tuan (0)", 0, TangToc.MUA_TUAN_TOI_DA[b]);
        Kiem.sai("bua: khong tinh tran diem chung", TangToc.TINH_TRAN[b]);
        Kiem.bang("bua: khong gioi han / ngay (mac dinh 0)", 0, TangToc.BUA_NGAY_TOI_DA);
        Kiem.bang("bac bua: EXP 50%, xu 30%", "50/30", TangToc.BUA_HE_SO_EXP + "/" + TangToc.BUA_HE_SO_XU);
        // xu / bua (0,3 gio thu nhap) khong re hon ty gia he thong 1 luong = 1% thu nhap 1 gio
        final double gioXu = TangToc.BUA_PHUT / 60.0 * TangToc.BUA_HE_SO_XU / 100.0;
        Kiem.dung("xu tu 1 bua (" + gioXu + " gio thu nhap) <= 1% x " + TangToc.GIA_LUONG[b] + " luong",
                gioXu <= 0.01 * TangToc.GIA_LUONG[b] + 1e-9);
        // cua hang luong (nap_cua_hang): dang ban, 30 luong, gioi_han 0 (khong gioi han)
        final Map<String, String> r = DayDu.tim("nap_cua_hang", "ma", "bua_the_luc");
        Kiem.dung("nap_cua_hang bua_the_luc: dang ban, trao tang_toc, 30 luong, gioi_han 0, khong giao dich",
                r != null && "1".equals(r.get("dang_ban")) && "tang_toc".equals(r.get("trao")) && "30".equals(r.get("gia_luong"))
                        && "0".equals(r.get("gioi_han")) && "0".equals(r.get("giao_dich_duoc")) && "0".equals(r.get("han_ngay")));
        Kiem.dung("NapTienLogic: bua_the_luc 30 luong, 0 / tuan",
                doc("src/com/htbb/pokemon/NapTienLogic.java").contains("new Mon(\"bua_the_luc\", \"Bùa thể lực (+60 phút)\", \"tang_toc\", TRAO_TANG_TOC, 30, 0, 0, 0, false)"));
        Kiem.dung("web napTienThuan: bua_the_luc { gia: 30, tuanToiDa: 0 }",
                doc("../web/server/utils/napTienThuan.ts").contains("bua_the_luc: { gia: 30, tuanToiDa: 0, diem: 45 }"));
        Kiem.bang("NapTienLogic.kiemMon: bua gioi_han 0 hop le", null, com.htbb.pokemon.NapTienLogic.kiemMon(new com.htbb.pokemon.NapTienLogic.Mon(
                "bua_the_luc", "Bùa thể lực (+60 phút)", "tang_toc", com.htbb.pokemon.NapTienLogic.TRAO_TANG_TOC, 30, 0, 0, 0, false)));
        Kiem.bang("NapTienLogic.kiemMon: admin dat lai tran 3 / tuan hop le", null, com.htbb.pokemon.NapTienLogic.kiemMon(new com.htbb.pokemon.NapTienLogic.Mon(
                "bua_the_luc", "Bùa thể lực (+60 phút)", "tang_toc", com.htbb.pokemon.NapTienLogic.TRAO_TANG_TOC, 30, 0, 3, 0, false)));
        Kiem.dung("phan B: UPDATE nap_cua_hang bua co dieu kien gia / gioi han cu", DayDu.nangCap().contains(
                "UPDATE `nap_cua_hang` SET `gia_luong` = 30, `gioi_han` = 0 WHERE `ma` = 'bua_the_luc' AND `gia_luong` = 60 AND `gioi_han` = 4;"));
        // Kecleon (luong khoa): ban khoa 1255, 25 luong khoa, khong gioi han tuan
        Map<String, String> kec = null;
        for (final Map<String, String> d : DayDu.dong("data_cua_hang_lau_dai")) {
            if ("ITEM:1255:1".equals(d.get("thuong"))) {
                kec = d;
            }
        }
        Kiem.dung("Kecleon: Bua the luc khoa (1255) 25 luong khoa, gioi_han_tuan 0, nhom Luong khoa (Kecleon)", kec != null
                && "luong_khoa:25".equals(kec.get("chi_phi")) && "0".equals(kec.get("gioi_han_tuan"))
                && "Lượng khoá (Kecleon)".equals(kec.get("nhom")) && "130".equals(kec.get("id")));
        final String b2 = DayDu.nangCap();
        Kiem.dung("phan B: INSERT IGNORE dong Kecleon 130",
                b2.contains("(130,'Lượng khoá (Kecleon)','Bùa thể lực (+60 phút) - khoá','luong_khoa:25','ITEM:1255:1',1,0,-1,0);"));
        Kiem.dung("data_vat_pham_lau_dai 1254 / 1255: ghi khong gioi han", "không giới hạn, không tính điểm tăng tốc".equals(
                DayDu.giaTri("data_vat_pham_lau_dai", "id_item", "1255", "gioi_han")));
        // dung: cong don, khong an diem, khong chan diem cua mon khac
        final HoSoLauDai h = new HoSoLauDai();
        final int tuan = 2043;
        Kiem.bang("ban khoa 1255 cung loai bua", b, TangToc.loaiCuaItem(1255));
        for (int i = 0; i < 10; i++) {
            Kiem.bang("bua thu " + (i + 1) + " dung duoc", null, TangToc.kiemDung(h, b, 50, 100, false, NGAY, tuan));
            TangToc.ghiDung(h, b, NGAY, tuan);
        }
        Kiem.bang("10 bua: 0 diem ngay / tuan", "0/0", h.tgDiemNgay + "/" + h.tgDiemTuan);
        Kiem.bang("10 bua: con du 60 diem cho mon khac", 60, TangToc.diemConLai(h, NGAY, tuan));
        final HoSoLauDai h3 = new HoSoLauDai();
        h3.tgNgay = NGAY;
        h3.tgTuan = tuan;
        h3.tgDiemTuan = 210;
        h3.tgDiemNgay = 60;
        Kiem.bang("het tran 60 / 210 diem: bua van dung duoc", null, TangToc.kiemDung(h3, b, 50, 100, false, NGAY, tuan));
        Kiem.dung("het tran: ve x1,5 bi chan (tran khong doi)", TangToc.kiemDung(h3, TangToc.VE_15, 50, 100, false, NGAY, tuan) != null);
        Kiem.dung("cap toi da: bua van khong dung duoc (EXP vuot cap = tinh thong)", TangToc.kiemDung(new HoSoLauDai(), b, 100, 100, false, NGAY, tuan) != null);
        Kiem.dung("TangTocGame doc tran bua / ngay tu danh muc", doc("src/real/TangTocGame.java").contains("TangToc.K_BUA_NGAY_TOI_DA"));
        // bac bua: 3 bua = 180 phut o 50% EXP / 30% xu, sau phut 100%
        final HoSoLauDai n = new HoSoLauDai();
        final long t0 = luc(2026, 10, 31, 8);
        NangLuong.doiNgay(n, t0, 90, 180, null);
        for (int i = 0; i < 3; i++) {
            TangToc.ghiDung(n, b, NGAY, tuan);
        }
        Kiem.bang("3 bua: 90 phut 100% + 180 phut bac bua", "90/180", n.nlPhut100 + "/" + TangToc.buaPhutHomNay(n));
        n.nlMsDung = 200 * PHUT;
        Kiem.bang("phut 200: EXP 50%", 50, NangLuong.heSoHienTai(n, t0 + 4 * GIO));
        Kiem.bang("phut 200: xu 30%", 30, NangLuong.heSoXuHienTai(n, t0 + 4 * GIO));
        n.nlMsDung = 270 * PHUT;
        Kiem.bang("phut 270: het bac bua -> bac met 20% (EXP)", 20, NangLuong.heSoHienTai(n, t0 + 5 * GIO));
        Kiem.bang("phut 270: xu 20%", 20, NangLuong.heSoXuHienTai(n, t0 + 5 * GIO));
        NangLuong.doiNgay(n, t0 + 24 * GIO, 90, 180, null);
        Kiem.bang("hom sau: bua khong tich (90 phut, bac bua 0)", "90/0", n.nlPhut100 + "/" + TangToc.buaPhutHomNay(n));
        final String mt = NangLuong.moTaPhut100(HoSoLauDai.giaiMa(nMa(t0)));
        Kiem.dung("So tay: dong Bua the luc '3 bùa, +180 phút ở mức 50% EXP, 30% xu'", mt.contains("Bùa thể lực hôm nay: 3 bùa, +180 phút ở mức 50% EXP, 30% xu/đồ rơi"));
    }

    static String nMa(final long t0) {
        final HoSoLauDai n = new HoSoLauDai();
        NangLuong.doiNgay(n, t0, 90, 180, null);
        for (int i = 0; i < 3; i++) {
            TangToc.ghiDung(n, TangToc.BUA, NGAY, 2043);
        }
        return n.maHoa();
    }

    // ------------------------------------------------------------------ 2. thuong thanh vien

    static void thuongThanhVien() {
        final long t0 = luc(2026, 10, 31, 8);
        final HoSoLauDai h = new HoSoLauDai();
        NangLuong.ghiNhan(h, luc(2026, 10, 30, 8), 90, 180); // hom qua: chua co goi
        h.nlMsDung = 200 * PHUT; // hom qua dung het 90 phut 100% -> khong tich
        Kiem.dung("doi ngay voi 3 goi: khoi tao ngay moi", NangLuong.doiNgay(h, t0, 90, 180, new int[]{30, 30, 30}));
        Kiem.bang("3 goi cong don: 90 + 30 + 30 + 30 = 180 phut 100%", 180, h.nlPhut100);
        Kiem.bang("tong thuong toi da +90", 90, NangLuong.thuongTong(h));
        Kiem.bang("phut goc (cho xu / tich luy) = 90", 90, NangLuong.phut100Goc(h));
        Kiem.sai("goi lai cung ngay: khong cong lan nua", NangLuong.doiNgay(h, t0 + GIO, 90, 180, new int[]{30, 30, 30}));
        Kiem.bang("ghiNhan cung ngay khong cong lai thuong", 100, NangLuong.ghiNhan(h, t0 + 2 * GIO, 90, 180, new int[]{30, 30, 30}));
        Kiem.bang("van 180 phut", 180, h.nlPhut100);
        // phut 150: EXP 100% (co thuong), xu 20% (phut goc 90 -> qua 60 phut met)
        h.nlMsDung = 150 * PHUT;
        Kiem.bang("phut 150: EXP 100% nho thuong", 100, NangLuong.heSoHienTai(h, t0 + 3 * GIO));
        Kiem.bang("phut 150: xu / do roi 20% (thuong chi EXP)", 20, NangLuong.heSoXuHienTai(h, t0 + 3 * GIO));
        h.nlMsDung = 180 * PHUT + 120 * PHUT + 120 * PHUT;
        Kiem.bang("sau 180 + 120 + 120 phut: 0% (duong cong giu nguyen, chi doi diem bat dau)", 0, NangLuong.heSoHienTai(h, t0 + 4 * GIO));
        // khong tich sang hom sau: dung 100 phut (< 180) -> con 80 phut, nhung 90 phut la thuong -> tich toi da 0
        final HoSoLauDai k = new HoSoLauDai();
        NangLuong.doiNgay(k, luc(2026, 10, 30, 8), 90, 180, new int[]{30, 30, 30});
        k.nlMsDung = 100 * PHUT;
        NangLuong.doiNgay(k, t0, 90, 180, null);
        Kiem.bang("hom qua dung 100 / 180 phut (90 la thuong): hom nay 90 + tich 0 (thuong khong tich)", 90, k.nlPhut100);
        final HoSoLauDai k2 = new HoSoLauDai();
        NangLuong.doiNgay(k2, luc(2026, 10, 30, 8), 90, 180, new int[]{30, 0, 0});
        k2.nlMsDung = 40 * PHUT;
        NangLuong.doiNgay(k2, t0, 90, 180, new int[]{30, 0, 0});
        Kiem.bang("hom qua dung 40 phut co thuong 30: tich 90 - 40 = 50, hom nay 90 + 50 + 30", 170, k2.nlPhut100);
        // bua + thuong cung ngay: ca hai deu khong tich
        final HoSoLauDai bt = new HoSoLauDai();
        NangLuong.doiNgay(bt, luc(2026, 10, 30, 8), 90, 180, new int[]{0, 30, 0});
        TangToc.ghiDung(bt, TangToc.BUA, 20261030, 2043);
        Kiem.bang("bua + the thang: 90 + 30 phut 100%, bua o bac rieng 60", "120/60", bt.nlPhut100 + "/" + TangToc.buaPhutHomNay(bt));
        Kiem.bang("phut goc khong tinh bua lan thuong", 90, NangLuong.phut100Goc(bt));
        bt.nlMsDung = 10 * PHUT;
        NangLuong.doiNgay(bt, t0, 90, 180, null);
        Kiem.bang("hom sau: tich 80 (90 - 10), khong tich bua / thuong", 170, bt.nlPhut100);
        // ho so luu / doc lai giu thuong
        final HoSoLauDai luu = HoSoLauDai.giaiMa(h.maHoa());
        Kiem.bang("HoSoLauDai n1/n2/n3 luu / doc lai", "30/30/30/180", luu.nlThuongTv + "/" + luu.nlThuongThe + "/" + luu.nlThuongVe + "/" + luu.nlPhut100);
        Kiem.bang("ho so cu (khong co n1..n3): thuong 0", 0, NangLuong.thuongTong(HoSoLauDai.giaiMa("nn=20261030;np=90")));
        // So tay: chi tiet
        final String mt = NangLuong.moTaPhut100(h);
        Kiem.dung("So tay: 'Phút 100% hôm nay: 180 phút' + co ban 90 + 3 dong thuong",
                mt.startsWith("Phút 100% hôm nay: 180 phút (cơ bản + tích luỹ 90") && mt.contains("Thành viên HTBB +30")
                        && mt.contains("Thẻ tháng +30") && mt.contains("Vé mùa cao cấp +30") && mt.contains("chỉ tính EXP"));
        final String mtBua = NangLuong.moTaPhut100(bt);
        Kiem.dung("So tay: khong goi -> goi y moi goi +30 (cau hinh)", mtBua.contains("cơ bản + tích luỹ 170")
                && mtBua.contains("Thành viên HTBB +30, Thẻ tháng +30, Vé mùa cao cấp +30"));
        final HoSoLauDai cb = new HoSoLauDai();
        NangLuong.doiNgay(cb, t0, 90, 180, new int[]{0, 30, 0});
        TangToc.ghiDung(cb, TangToc.BUA, 20261031, 2043);
        Kiem.dung("So tay: bua + the thang hien ca hai", NangLuong.moTaPhut100(cb).contains("Thẻ tháng +30")
                && NangLuong.moTaPhut100(cb).contains("Bùa thể lực hôm nay: 1 bùa, +60 phút"));
        // cau hinh: mac dinh 30, kep 0..180, 0 = tat
        Kiem.bang("thuongTheoCo mac dinh 30/30/30", "30,30,30", s(NangLuong.thuongTheoCo(new boolean[]{true, true, true})));
        Kiem.bang("chi the thang", "0,30,0", s(NangLuong.thuongTheoCo(new boolean[]{false, true, false})));
        Kiem.bang("khong goi / null", "0,0,0", s(NangLuong.thuongTheoCo(null)));
        final Map<String, String> db = new HashMap<String, String>();
        db.put(NangLuong.K_THUONG_THANH_VIEN, "0");
        db.put(NangLuong.K_THUONG_THE_THANG, "45");
        db.put(NangLuong.K_THUONG_VE_MUA, "999");
        CauHinhTrungTam.datChoTest(null, db, false);
        try {
            Kiem.bang("cau hinh CSDL: 0 / 45 / sai khoang -> giu mac dinh 30", "0,45,30",
                    s(NangLuong.thuongTheoCo(new boolean[]{true, true, true})));
        } finally {
            CauHinhTrungTam.datChoTest(null, null, false);
        }
    }

    static String s(final int[] a) {
        return a[0] + "," + a[1] + "," + a[2];
    }

    // ------------------------------------------------------------------ danh muc

    static void danhMuc() {
        int thay = 0;
        for (final DinhNghiaKhoa k : CauHinhTrungTam.danhMuc()) {
            for (final String khoa : NangLuong.K_THUONG) {
                if (khoa.equals(k.khoa)) {
                    thay++;
                    Kiem.dung(khoa + ": nhom kinh_te, so, mac dinh 30, 0..180, chinh tren web, ap dung ngay",
                            "kinh_te".equals(k.nhom) && "so".equals(k.kieu) && "30".equals(k.macDinh) && k.min() == 0 && k.max() == 180
                                    && DinhNghiaKhoa.LOP_CHINH.equals(k.lop()) && k.apDungNgay && "cau_hinh".equals(k.bang));
                }
            }
        }
        Kiem.bang("danh muc co du 3 khoa thuong nang luong", 3, thay);
        boolean coBua = false;
        for (final DinhNghiaKhoa k : CauHinhTrungTam.danhMuc()) {
            if (TangToc.K_BUA_NGAY_TOI_DA.equals(k.khoa)) {
                coBua = "kinh_te".equals(k.nhom) && "0".equals(k.macDinh) && k.min() == 0 && k.max() == 100
                        && DinhNghiaKhoa.LOP_CHINH.equals(k.lop());
            }
        }
        Kiem.dung("danh muc: tang_toc.bua_ngay_toi_da (0 = khong gioi han, chinh tren web)", coBua);
        Kiem.dung("gen.ts / docs co tang_toc.bua_ngay_toi_da", doc("../web/shared/utils/cauHinhTrungTam.gen.ts").contains("khoa: \"tang_toc.bua_ngay_toi_da\"")
                && doc("../docs/cau-hinh.md").contains("| `tang_toc.bua_ngay_toi_da` |"));
        final String b = DayDu.nangCap();
        for (final String khoa : NangLuong.K_THUONG) {
            Kiem.dung("phan B muc 2 co " + khoa, b.contains("VALUES ('" + khoa + "', 'kinh_te'"));
            Kiem.dung("web gen.ts co " + khoa, doc("../web/shared/utils/cauHinhTrungTam.gen.ts").contains("khoa: \"" + khoa + "\""));
            Kiem.dung("docs/cau-hinh.md co " + khoa, doc("../docs/cau-hinh.md").contains("| `" + khoa + "` |"));
        }
        Kiem.dung("bac met giu nguyen 120:20,120:5", java.util.Arrays.deepToString(com.htbb.pokemon.ThongSoLauDai.BAC_MET).equals("[[120, 20], [120, 5]]"));
        Kiem.bang("phut 100% / ngay giu nguyen 90", 90, com.htbb.pokemon.ThongSoLauDai.NANG_LUONG_PHUT_NGAY);
        // mo ta goi nap (web /naptien) ghi quyen loi moi
        for (final String ma : new String[]{"THANH_VIEN", "THE_THANG", "VE_MUA"}) {
            final Map<String, String> g = DayDu.tim("nap_goi", "ma", ma);
            Kiem.dung("nap_goi " + ma + ": mo ta co +30 phut nang luong", g != null && String.valueOf(g.get("mo_ta"))
                    .contains("+30 phút năng lượng huấn luyện 100% mỗi ngày"));
            Kiem.dung("phan B: UPDATE nap_goi " + ma + " co dieu kien mo ta cu", b.contains("WHERE `ma` = '" + ma + "' AND `mo_ta` = '"));
        }
    }

    // ------------------------------------------------------------------ 3. tu dong danh luc 0%

    static void tuDong() {
        final TuDong.CauHinh c = new TuDong.CauHinh(); // 5 gio mien phi
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 12;
        n.x = 400;
        n.y = 300;
        final long t0 = 1_800_000_000_000L;
        // con gio mien phi + nang luong 0%: van chay, gio mien phi van tinh, kho khong tru
        final TuDong.Phien p = new TuDong.Phien();
        p.ngay = NGAY;
        TuDong.themKho(p, 10 * GIO);
        TuDong.bat(c, p, n, 0, t0);
        Kiem.sai("0% + con mien phi: KHONG dung", TuDong.tich(c, p, false, t0 + PHUT, NGAY, false));
        Kiem.bang("0%: gio mien phi van tinh (1 phut)", Long.valueOf(PHUT), Long.valueOf(p.msDung));
        Kiem.bang("0%: kho nguyen 10 gio", Long.valueOf(10 * GIO), Long.valueOf(p.msKho));
        // vat qua moc mien phi luc 0%: dung, giu kho, ly do rieng
        p.msDung = 5 * GIO - 30000L;
        Kiem.dung("0% + het mien phi: dung (khong dot ve treo)", TuDong.tich(c, p, false, t0 + 2 * PHUT, NGAY, false));
        Kiem.bang("kho giu nguyen 10 gio", Long.valueOf(10 * GIO), Long.valueOf(p.msKho));
        Kiem.bang("mien phi chot o 5 gio", Long.valueOf(5 * GIO), Long.valueOf(p.msDung));
        Kiem.bang("ly do: het gio mien phi, giu kho", TuDong.TAT_HET_GIO_GIU_KHO, p.lyDoTat);
        Kiem.dung("cau bao giu kho nhac Bua the luc / 0 gio", TuDong.lyDo(TuDong.TAT_HET_GIO_GIU_KHO).contains("kho giờ treo không bị trừ")
                && TuDong.lyDo(TuDong.TAT_HET_GIO_GIU_KHO).contains("Bùa thể lực"));
        // bat lai luc 0%: tu choi (chi con kho); con nang luong: bat duoc va tru kho nhu cu
        n.hetNangLuong = true;
        Kiem.bang("bat luc 0% chi con kho: tu choi", TuDong.lyDo(TuDong.TAT_HET_GIO_GIU_KHO), TuDong.kiemBat(c, p, n, NGAY));
        n.hetNangLuong = false;
        Kiem.bang("bat khi con nang luong: duoc (dung kho)", null, TuDong.kiemBat(c, p, n, NGAY));
        TuDong.bat(c, p, n, 0, t0 + 3 * PHUT);
        Kiem.sai("con nang luong: chay tren kho", TuDong.tich(c, p, false, t0 + 4 * PHUT, NGAY, true));
        Kiem.bang("con nang luong: kho bi tru 1 phut", Long.valueOf(10 * GIO - PHUT), Long.valueOf(p.msKho));
        Kiem.sai("tich 5 tham so cu = tru kho (giu hanh vi cu)", TuDong.tich(c, p, false, t0 + 5 * PHUT, NGAY));
        Kiem.bang("tich cu: kho bi tru them 1 phut", Long.valueOf(10 * GIO - 2 * PHUT), Long.valueOf(p.msKho));
        // 0% + con mien phi + khong kho: bat duoc (gio mien phi van dung)
        final TuDong.Phien q = new TuDong.Phien();
        q.ngay = NGAY;
        n.hetNangLuong = true;
        Kiem.bang("0% nhung con gio mien phi: bat duoc (khong tu dung)", null, TuDong.kiemBat(c, q, n, NGAY));
        n.hetNangLuong = false;
        // het ca mien phi lan kho luc 0%: ly do het gio thuong
        final TuDong.Phien r = new TuDong.Phien();
        r.ngay = NGAY;
        TuDong.bat(c, r, n, 0, t0);
        r.msDung = 5 * GIO - 1000L;
        Kiem.dung("0% + khong kho: het gio thuong", TuDong.tich(c, r, false, t0 + PHUT, NGAY, false) && r.lyDoTat == TuDong.TAT_HET_GIO);
        // bao mot lan / ngay
        Kiem.dung("bao het nang luong: lan dau", TuDong.nenBaoHetNangLuong(q, NGAY));
        Kiem.sai("bao het nang luong: khong lap cung ngay", TuDong.nenBaoHetNangLuong(q, NGAY));
        Kiem.dung("bao het nang luong: ngay moi bao lai", TuDong.nenBaoHetNangLuong(q, NGAY + 1));
        Kiem.bang("cau bao chu game", "Đã hết năng lượng huấn luyện hôm nay — đánh quái không còn EXP/xu. Dùng Bùa thể lực hoặc nghỉ tới 0 giờ.",
                TuDong.BAO_HET_NANG_LUONG);
    }

    // ------------------------------------------------------------------ noi vao game (nguon)

    static void noiVaoGame() {
        final String ld = doc("src/real/LauDaiNhanVat.java");
        Kiem.dung("LauDaiNhanVat.nangLuong: thuong chi khi khoi tao ngay (doc CSDL 1 lan / ngay)",
                ld.contains("NangLuong.daKhoiTao(h, bay) ? null : thuongNangLuong(p)") && ld.contains("NangLuong.ghiNhan(h, bay, phutNgay(), phutTich(), thuong)"));
        Kiem.dung("LauDaiNhanVat: 3 goi (Thanh vien, The thang, Ve mua cao cap)", ld.contains("ThanhVien.coGoiConHan(p.userID)")
                && ld.contains("TangTocGame.coTheThang(p)") && ld.contains("NapTienGame.coVeMuaCaoCap(p)"));
        Kiem.dung("LauDaiNhanVat: bot AI khong co thuong", ld.contains("HlvAiGame.laBot(p)"));
        Kiem.dung("So tay: chot ngay + hien chi tiet", doc("src/real/SoTayLauDai.java").contains("LauDaiNhanVat.chuanBiNgay(p)")
                && doc("src/real/SoTayLauDai.java").contains("NangLuong.moTaPhut100(h)"));
        Kiem.dung("Keo hiem: khoi tao ngay (co thuong) truoc khi tru phut 100%",
                doc("src/real/VatPhamDungLauDai.java").contains("final HoSoLauDai h = LauDaiNhanVat.chuanBiNgay(p);"));
        final String td = doc("src/real/TuDongGame.java");
        Kiem.dung("TuDongGame: tich khong tru kho khi het nang luong", td.contains("homNay(bay), !hetNangLuong(p))")
                && td.contains("!(hetNL = hetNangLuong(p))"));
        Kiem.dung("TuDongGame: bao het nang luong (khong tat)", td.contains("baoHetNangLuong(p, ph, bay)")
                && td.contains("ThongBao.rieng(p, TuDong.BAO_HET_NANG_LUONG)"));
        Kiem.dung("TuDongGame: ngu canh bat co hetNangLuong", td.contains("n.hetNangLuong = hetNangLuong(p);"));
        Kiem.dung("ThanhVien.coGoiConHan: loi CSDL = khong tang", doc("src/com/htbb/pokemon/ThanhVien.java").contains("public static boolean coGoiConHan"));
        Kiem.dung("NapTienGame.coVeMuaCaoCap: tat nap = khong", doc("src/com/htbb/pokemon/NapTienGame.java").contains("public static boolean coVeMuaCaoCap"));
    }

    static void taiLieu() {
        final String tk = doc("../docs/thiet-ke-game.md");
        Kiem.dung("thiet-ke-game.md 3.3: thuong Thanh vien / The thang / Ve mua", tk.contains("exp.nang_luong_thanh_vien_phut"));
        Kiem.dung("thiet-ke-game.md 3.4: Bua the luc 30 luong / Kecleon 25 luong khoa, khong gioi han", tk.contains("Bùa thể lực khoá 25 lượng khoá")
                && tk.contains("30 lượng, không giới hạn"));
        Kiem.dung("thiet-ke-game.md 8.2: 0% khong tru kho gio treo", tk.contains("năng lượng huấn luyện 0%") && tk.contains("không trừ kho"));
        Kiem.dung("web cam nang cap-do: thuong goi", doc("../web/app/pages/cam-nang/cap-do.vue").contains("phút 100% mỗi ngày"));
    }
}
