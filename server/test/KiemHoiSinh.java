import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.BanDoPokemon;
import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.HoiSinh;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThongSoLauDai;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Guc / hoi sinh (com.htbb.pokemon.HoiSinh + real.HoiSinhGame, 18/10/2026): phi theo cap + he so guc lap, luong khoa
 * co tran, phat ve Trung tam (KHONG BAO GIO tut cap, do ben, met moi), tan thu, han che PvP / Gym / boss / Me cung,
 * cho + hoi chieu, dong doi, tu dong danh (dung tu dong, tu dung vat pham chi khi bat), vung Hoenn hoi sinh O HOENN
 * (o di duoc that tren cMap), cau hinh trung tam va cac cho noi vao game.
 */
public final class KiemHoiSinh {

    private KiemHoiSinh() {
    }

    static final long T0 = 1_800_000_000_000L;

    public static void chay() {
        Kiem.nhom("Guc / hoi sinh (HoiSinh) - phi, phat, vung, han che, tu dong");
        try {
            phi();
            phat();
            luaChon();
            hanChe();
            tuDong();
            giaVaTranThuoc();
            vungHoenn();
            cauHinh();
            noiVaoGame();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void phi() {
        final HoiSinh.CauHinh c = new HoiSinh.CauHinh();
        final long gio50 = LauDai.xuMoiGio(50);
        Kiem.bang("phi xu cap 50 lan 1 = 40% gio thu nhap", gio50 * 40 / 100, HoiSinh.phiXu(c, 50, 1));
        Kiem.bang("lan 2 trong cua so x1,5", gio50 * 40 / 100 * 150 / 100, HoiSinh.phiXu(c, 50, 2));
        Kiem.bang("lan 3 x2", gio50 * 40 / 100 * 2, HoiSinh.phiXu(c, 50, 3));
        Kiem.bang("lan 4 x3 (tran)", gio50 * 40 / 100 * 3, HoiSinh.phiXu(c, 50, 4));
        Kiem.bang("lan 9 van x3 (tran)", HoiSinh.phiXu(c, 50, 4), HoiSinh.phiXu(c, 50, 9));
        Kiem.dung("phi tang theo cap", HoiSinh.phiXu(c, 90, 1) > HoiSinh.phiXu(c, 50, 1) && HoiSinh.phiXu(c, 50, 1) > HoiSinh.phiXu(c, 20, 1));
        final double gioPhi = (double) HoiSinh.phiXu(c, 70, 1) / LauDai.xuMoiGio(70);
        Kiem.dung("phi ~0,3-0,5 gio thu nhap (" + gioPhi + ")", gioPhi >= 0.3 && gioPhi <= 0.5);
        c.xuToiThieu = 5000;
        Kiem.bang("phi toi thieu (xu_toi_thieu) o cap thap", 5000L, HoiSinh.phiXu(c, 1, 1));
        c.xuToiThieu = HoiSinh.XU_TOI_THIEU;
        Kiem.bang("luong khoa lan 1 = 2", 2, HoiSinh.phiLuongKhoa(c, 1));
        Kiem.bang("luong khoa lan 2 = 3 (lam tron len)", 3, HoiSinh.phiLuongKhoa(c, 2));
        c.luongKhoa = 5;
        Kiem.bang("luong khoa co tran 10", 10, HoiSinh.phiLuongKhoa(c, 4));
        c.luongKhoa = 0;
        Kiem.bang("luong khoa 0 = tat", 0, HoiSinh.phiLuongKhoa(c, 1));
        final HoiSinh.LichSu ls = new HoiSinh.LichSu();
        final long cs = 30 * 60000L;
        Kiem.bang("guc 1", 1, ls.ghiGuc(T0, cs));
        Kiem.bang("guc 2 sau 10 phut", 2, ls.ghiGuc(T0 + 600000L, cs));
        Kiem.bang("guc 3 sau 25 phut", 3, ls.ghiGuc(T0 + 1500000L, cs));
        Kiem.bang("guc sau 40 phut: lan 0p va 10p da ra cua so 30 phut", 2, ls.ghiGuc(T0 + 2400000L, cs));
        Kiem.bang("2 gio sau: ve lai lan 1", 1, ls.ghiGuc(T0 + 9000000L, cs));
        Kiem.bang("he so chu 1,5", "1,5", HoiSinh.heSoChu(150));
        Kiem.bang("phan nghin chu 2%", "2%", HoiSinh.phanTramChu(20));
        Kiem.bang("so co dau cham", "1.234.567", HoiSinh.so(1234567L));
    }

    static void phat() {
        final HoiSinh.CauHinh c = new HoiSinh.CauHinh();
        final int cap = 50;
        final long dau = 1_000_000L, len = ThongSoLauDai.EXP_LEN_CAP[cap];
        final long moiGio = ThongSoLauDai.expMoiGio(cap);
        final long mat = HoiSinh.expMat(c, cap, dau + len / 2, dau, len, moiGio);
        Kiem.bang("mat 2% EXP cap (chan 15 phut cay)", Math.min(len * 20 / 1000, moiGio * 15 / 60), mat);
        Kiem.bang("tien do it hon muc mat: chi tru toi dau cap (KHONG tut cap)", 100L, HoiSinh.expMat(c, cap, dau + 100L, dau, len, moiGio));
        Kiem.bang("vua len cap (tien do 0): khong mat", 0L, HoiSinh.expMat(c, cap, dau, dau, len, moiGio));
        Kiem.bang("tan thu (cap 14): khong mat EXP", 0L, HoiSinh.expMat(c, 14, dau + len / 2, dau, len, moiGio));
        // cap cao: tran theo phut cay quan trong hon 2%
        final int cao = 90;
        final long lc = ThongSoLauDai.EXP_LEN_CAP[cao], mg = ThongSoLauDai.expMoiGio(cao);
        Kiem.bang("cap 90: chan 15 phut cay", Math.min(lc * 20 / 1000, mg * 15 / 60), HoiSinh.expMat(c, cao, lc, 0L, lc, mg));
        // ngau nhien: khong bao gio am, khong vuot tien do
        boolean ok = true;
        final java.util.Random r = new java.util.Random(7);
        for (int i = 0; i < 2000; i++) {
            final int cp = 1 + r.nextInt(99);
            final long l = 1 + (long) (r.nextDouble() * 1e9);
            final long td = (long) (r.nextDouble() * l);
            final long m = HoiSinh.expMat(c, cp, 5000L + td, 5000L, l, r.nextBoolean() ? 0L : l / 3);
            ok &= m >= 0 && m <= td;
        }
        Kiem.dung("2000 ca ngau nhien: 0 <= mat <= tien do trong cap", ok);
        Kiem.bang("do ben 5% cua 1000 = -50", 950, HoiSinh.doBenSau(1000, 1000, 5));
        Kiem.bang("do ben khong xuong duoi 0", 0, HoiSinh.doBenSau(20, 1000, 5));
        Kiem.bang("do ben 0 giu 0 (khong vo do them)", 0, HoiSinh.doBenSau(0, 1000, 5));
        Kiem.bang("do ben it nhat -1", 9, HoiSinh.doBenSau(10, 10, 1));
        Kiem.bang("met moi -20% EXP", 80L, HoiSinh.apGiam(100L, 20));
        Kiem.bang("met moi 0% = giu nguyen", 100L, HoiSinh.apGiam(100L, 0));
    }

    static HoiSinh.TinhHuong th(final int cap) {
        final HoiSinh.TinhHuong t = new HoiSinh.TinhHuong();
        t.cap = cap;
        t.gucLuc = T0;
        t.xu = 1_000_000_000L;
        t.luongKhoa = 100;
        t.soThuoc = 2;
        t.soThuocToiDa = 1;
        return t;
    }

    static HoiSinh.LuaChon tim(final List<HoiSinh.LuaChon> ds, final int ma) {
        for (final HoiSinh.LuaChon l : ds) {
            if (l.ma == ma) {
                return l;
            }
        }
        return null;
    }

    static void luaChon() {
        final HoiSinh.CauHinh c = new HoiSinh.CauHinh();
        final HoiSinh.TinhHuong t = th(40);
        List<HoiSinh.LuaChon> ds = HoiSinh.dsLuaChon(c, t, T0 + 1000L);
        Kiem.dung("ngay sau guc: tai cho phai cho (dem nguoc 4 giay)", tim(ds, HoiSinh.LC_THUOC).choGiay == 4 && !tim(ds, HoiSinh.LC_THUOC).bamDuoc());
        Kiem.dung("ve Trung tam bam duoc ngay", tim(ds, HoiSinh.LC_TRUNG_TAM).bamDuoc());
        Kiem.sai("cap 40 khong co dong tan thu", tim(ds, HoiSinh.LC_TAN_THU) != null);
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.dung("sau 5 giay: Thuoc hoi sinh bam duoc", tim(ds, HoiSinh.LC_THUOC).bamDuoc());
        Kiem.dung("tra xu bam duoc khi du xu", tim(ds, HoiSinh.LC_XU).bamDuoc());
        t.xu = 10;
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.dung("thieu xu: khoa kem ly do", tim(ds, HoiSinh.LC_XU).khoa != null && tim(ds, HoiSinh.LC_XU).khoa.startsWith("Thiếu"));
        t.soThuoc = 0;
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.dung("het thuoc: khoa", tim(ds, HoiSinh.LC_THUOC).khoa != null);
        t.lanTaiChoCuoi = T0 - 10000L;
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.bang("hoi chieu 60 giay giua hai lan tai cho", 45, tim(ds, HoiSinh.LC_THUOC_TOI_DA).choGiay);
        Kiem.dung("hoi chieu khong chan ve Trung tam", tim(ds, HoiSinh.LC_TRUNG_TAM).bamDuoc());
        Kiem.dung("khong co dong doi: cho dong doi khoa", tim(ds, HoiSinh.LC_DONG_DOI).khoa != null);
        t.coDongDoi = true;
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.dung("co dong doi mang thuoc: bam duoc", tim(ds, HoiSinh.LC_DONG_DOI).bamDuoc());
        t.daChoDongDoi = true;
        t.choDongDoiDen = T0 + 65000L;
        ds = HoiSinh.dsLuaChon(c, t, T0 + 5000L);
        Kiem.bang("dang cho dong doi: dem nguoc 60 giay", 60, tim(ds, HoiSinh.LC_DONG_DOI).choGiay);
        Kiem.dung("... va khong bam lai duoc", !tim(ds, HoiSinh.LC_DONG_DOI).bamDuoc());
        t.lan = 2;
        Kiem.dung("mo ta lan 2: x1,5", HoiSinh.moTa(c, t).contains("×1,5"));
        // tan thu
        final HoiSinh.TinhHuong n = th(10);
        ds = HoiSinh.dsLuaChon(c, n, T0 + 6000L);
        Kiem.dung("tan thu: co hoi sinh mien phi", tim(ds, HoiSinh.LC_TAN_THU) != null && tim(ds, HoiSinh.LC_TAN_THU).bamDuoc()
                && tim(ds, HoiSinh.LC_TAN_THU).phi == 0L);
        Kiem.sai("tan thu: khong hien tra xu", tim(ds, HoiSinh.LC_XU) != null);
        n.lanTaiChoCuoi = T0;
        ds = HoiSinh.dsLuaChon(c, n, T0 + 6000L);
        Kiem.bang("tan thu: hoi chieu 30 giay", 24, tim(ds, HoiSinh.LC_TAN_THU).choGiay);
        Kiem.dung("tan thu: ve Trung tam khong phat", tim(ds, HoiSinh.LC_TRUNG_TAM).chiTiet.contains("không bị phạt"));
        c.dongDoiBat = false;
        Kiem.bang("tat dong doi: khong hien dong", null, tim(HoiSinh.dsLuaChon(c, t, T0), HoiSinh.LC_DONG_DOI));
    }

    static void hanChe() {
        final HoiSinh.CauHinh c = new HoiSinh.CauHinh();
        Kiem.bang("map thuong: duoc", null, HoiSinh.lyDoCam(c, 12, false, false, false, false));
        Kiem.dung("PvP: cam", HoiSinh.lyDoCam(c, 12, true, false, false, false) != null);
        Kiem.dung("dau truong 202: cam (map_pvp)", HoiSinh.lyDoCam(c, 202, false, false, false, false) != null);
        Kiem.dung("Gym: cam", HoiSinh.lyDoCam(c, 12, false, true, false, false) != null);
        Kiem.dung("pha boss: cam", HoiSinh.lyDoCam(c, 12, false, false, true, false) != null);
        Kiem.dung("Me cung: cam", HoiSinh.lyDoCam(c, 12, false, false, false, true) != null);
        c.camBoss = false;
        Kiem.bang("tat cam_boss: boss duoc", null, HoiSinh.lyDoCam(c, 12, false, false, true, false));
        c.mapCamThem = new int[]{44};
        Kiem.dung("map_cam_them", HoiSinh.lyDoCam(c, 44, false, false, false, false) != null);
        final HoiSinh.TinhHuong t = th(60);
        t.camTaiCho = HoiSinh.lyDoCam(c, 12, true, false, false, false);
        final List<HoiSinh.LuaChon> ds = HoiSinh.dsLuaChon(new HoiSinh.CauHinh(), t, T0 + 99000L);
        boolean taiChoKhoa = true;
        for (final HoiSinh.LuaChon l : ds) {
            if (l.ma != HoiSinh.LC_TRUNG_TAM && l.ma != HoiSinh.LC_DONG_DOI) {
                taiChoKhoa &= l.khoa != null && l.khoa.equals(t.camTaiCho);
            }
        }
        Kiem.dung("bi cam: MOI lua chon tai cho khoa cung ly do", taiChoKhoa);
        Kiem.dung("bi cam: ve Trung tam van duoc", tim(ds, HoiSinh.LC_TRUNG_TAM).bamDuoc());
    }

    static void tuDong() {
        final HoiSinh.TinhHuong t = th(50);
        Kiem.bang("tu dong, khong bat Tu hoi sinh: ve Trung tam (dung)", HoiSinh.LC_TRUNG_TAM, HoiSinh.chonTuDong(false, t));
        Kiem.bang("bat Tu hoi sinh + co thuoc: dung Thuoc hoi sinh (re hon)", HoiSinh.LC_THUOC, HoiSinh.chonTuDong(true, t));
        t.soThuoc = 0;
        Kiem.bang("chi co toi da: dung toi da", HoiSinh.LC_THUOC_TOI_DA, HoiSinh.chonTuDong(true, t));
        t.soThuocToiDa = 0;
        Kiem.bang("het vat pham: ve Trung tam (dung, khong treo vo han)", HoiSinh.LC_TRUNG_TAM, HoiSinh.chonTuDong(true, t));
        t.soThuoc = 5;
        t.camTaiCho = "x";
        Kiem.bang("ban do cam tai cho: ve Trung tam", HoiSinh.LC_TRUNG_TAM, HoiSinh.chonTuDong(true, t));
        final String g = doc("src/real/HoiSinhGame.java");
        Kiem.dung("tu dong: chi dung vat pham khi TuDongGame.choHoiSinhTuDong (tran / gio + nguoi choi bat)",
                g.contains("!TuDongGame.choHoiSinhTuDong(p)"));
        Kiem.dung("tu dong: con lai TuDongGame.dungKhiChet + ve Trung tam", g.contains("TuDongGame.dungKhiChet(p)")
                && g.contains("t.tuDongLam = lam;") && g.contains("veTrungTam(p)"));
    }

    static void giaVaTranThuoc() {
        final HoiSinh.CauHinh c = new HoiSinh.CauHinh();
        Kiem.bang("gia Thuoc hoi sinh cap 1: khong duoi gia goc 407", 1077L, HoiSinh.giaThuoc(c, 1)); // 3590*30%
        Kiem.bang("gia Thuoc hoi sinh cap 90 = 30% gio thu nhap", LauDai.xuMoiGio(90) * 30 / 100, HoiSinh.giaThuoc(c, 90));
        final double ti = (double) HoiSinh.giaThuoc(c, 70) / HoiSinh.phiXu(c, 70, 1);
        Kiem.dung("gia thuoc cung bac phi xu tai cho (" + ti + ")", ti >= 0.5 && ti <= 1.0);
        c.giaThuocPhanTramGio = 0;
        Kiem.bang("0% = gia toi thieu (goc)", 407L, HoiSinh.giaThuoc(c, 90));
        final HoiSinh.CauHinh d = new HoiSinh.CauHinh();
        final HoiSinh.LichSu ls = new HoiSinh.LichSu();
        for (int i = 0; i < 6; i++) {
            ls.ghiThuoc(T0 + i * 60000L);
        }
        Kiem.bang("6 lan dung thuoc trong gio", 6, ls.thuocTrongGio(T0 + 400000L));
        Kiem.sai("het luot (tran 6/gio)", HoiSinh.conLuotThuoc(d, 6));
        Kiem.bang("qua 60 phut: lan dau ra khoi cua so", 5, ls.thuocTrongGio(T0 + 3600000L + 1L));
        final HoiSinh.TinhHuong t = th(60);
        t.thuocTrongGio = 6;
        t.conLuotThuoc = false;
        final List<HoiSinh.LuaChon> ds = HoiSinh.dsLuaChon(d, t, T0 + 99000L);
        Kiem.dung("het luot: Thuoc hoi sinh khoa", tim(ds, HoiSinh.LC_THUOC).khoa != null && tim(ds, HoiSinh.LC_THUOC).khoa.contains("60 phút"));
        Kiem.dung("het luot: Thuoc toi da khoa", tim(ds, HoiSinh.LC_THUOC_TOI_DA).khoa != null);
        Kiem.dung("het luot: tra xu van duoc", tim(ds, HoiSinh.LC_XU).bamDuoc());
        Kiem.bang("het luot: tu dong ve Trung tam", HoiSinh.LC_TRUNG_TAM, HoiSinh.chonTuDong(true, t));
        d.thuocMoiGio = 0;
        Kiem.dung("0 = khong gioi han", HoiSinh.conLuotThuoc(d, 999));
        Kiem.dung("cua so thuoc NPC khong ban Thuoc hoi sinh (gia khong hien theo nguoi mua)",
                doc("src/real/OfflineMap.java").contains("var32 == com.htbb.pokemon.HoiSinh.THUOC_HOI_SINH && HoiSinhGame.bat()"));
        final String g = doc("src/real/HoiSinhGame.java");
        Kiem.dung("So tay -> Cua hang: dong Thuoc hoi sinh gia theo nguoi xem", doc("src/real/CuaHangLauDai.java").contains("HoiSinhGame.dongCuaHang(p)")
                && doc("src/real/CuaHangLauDai.java").contains("HoiSinhGame.moMuaThuoc(p)"));
        Kiem.dung("hop xac nhan ghi dung so xu tru + chot cap (doi cap -> hien lai)", g.contains("\"Mua \" + SO_MUA[i] + \" Thuốc hồi sinh: trả \" + HoiSinh.so(gia * SO_MUA[i])")
                && g.contains("p.lvDetail.lv != capHien") && g.contains("HoiSinh.giaThuoc(ch(), capHien) * n"));
        Kiem.dung("ghi luot thuoc khi dung (tay / tu dong / tu tui)", doc("src/real/HoiSinhGame.java").split("ls.ghiThuoc\\(", -1).length - 1 >= 3);
    }

    static boolean[] diDuoc(final int id) throws Exception {
        final DinhDangBanDo m = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + id).toPath()));
        final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + DinhDangBanDo.boTile(id) + ".type").toPath());
        final boolean[] di = new boolean[m.w * m.h];
        for (int i = 0; i < di.length; i++) {
            final int t = m.tile[i];
            di[i] = !DinhDangBanDo.trong(t) && t < k.length && (k[t] & 2) == 0;
        }
        return di;
    }

    static void vungHoenn() throws Exception {
        Kiem.bang("map cu (Pewter 0) la vung cu", HoiSinh.VUNG_CU, HoiSinh.vung(0));
        Kiem.bang("Mauville 49 la Hoenn", HoiSinh.VUNG_HOENN, HoiSinh.vung(49));
        Kiem.bang("khong phai Hoenn: khong co diem Hoenn", null, HoiSinh.diemTrungTamHoenn(9, null));
        Kiem.bang("Rung Petalburg 44 -> Littleroot 43 / Rustboro 45 (1 cong)", true,
                HoiSinh.trungTamHoennGanNhat(44) == 43 || HoiSinh.trungTamHoennGanNhat(44) == 45);
        Kiem.bang("Route 110 48 -> Slateport 47 hoac Mauville 49", true,
                HoiSinh.trungTamHoennGanNhat(48) == 47 || HoiSinh.trungTamHoennGanNhat(48) == 49);
        Kiem.bang("Sanh Nha vo dich 282 -> Ever Grande 62", 62, HoiSinh.trungTamHoennGanNhat(282));
        Kiem.bang("thi tran co Trung tam: chinh no", 59, HoiSinh.trungTamHoennGanNhat(59));
        final boolean coFile = new File(BanDoPokemon.THU_MUC_CMAP + "43").isFile();
        if (!coFile) {
            System.out.println("   (bo qua kiem o di duoc: khong co cMap)");
        }
        final Map<Integer, boolean[]> di = new HashMap<Integer, boolean[]>();
        int sai = 0, ngoai = 0;
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            final int[] d = coFile ? HoiSinh.diemTrungTamHoenn(b.id, (map, x, y) -> {
                try {
                    boolean[] a = di.get(map);
                    if (a == null) {
                        a = diDuoc(map);
                        di.put(map, a);
                    }
                    return a[y * BanDoHoenn.lay(map).w + x];
                } catch (final Exception e) {
                    return false;
                }
            }) : HoiSinh.diemTrungTamHoenn(b.id, null);
            if (d == null || !BanDoHoenn.laHoenn(d[0]) || !HoiSinh.coTrungTam(BanDoHoenn.lay(d[0]))) {
                ngoai++;
                continue;
            }
            if (coFile) {
                final boolean[] a = di.containsKey(d[0]) ? di.get(d[0]) : diDuoc(d[0]);
                if (!a[d[2] * BanDoHoenn.lay(d[0]).w + d[1]]) {
                    sai++;
                }
            }
        }
        Kiem.bang("guc o MOI ban do Hoenn (29) -> hoi sinh o thi tran HOENN co Trung tam", 0, ngoai);
        Kiem.bang("diem hoi sinh Hoenn la o di duoc (cMap)", 0, sai);
    }

    static void cauHinh() throws Exception {
        final String j = doc("../tools/cauhinh/danh_muc.json");
        for (final String k : new String[]{HoiSinh.K_BAT, HoiSinh.K_XU_PHAN_TRAM_GIO, HoiSinh.K_HE_SO_LAP, HoiSinh.K_CUA_SO_PHUT,
                HoiSinh.K_LUONG_KHOA, HoiSinh.K_LUONG_KHOA_TOI_DA, HoiSinh.K_CHO_SAU_GUC, HoiSinh.K_HOI_CHIEU, HoiSinh.K_EXP_MAT,
                HoiSinh.K_EXP_MAT_TOI_DA_PHUT, HoiSinh.K_DO_BEN, HoiSinh.K_MET_PHUT, HoiSinh.K_MET_EXP, HoiSinh.K_MET_TAN_CONG,
                HoiSinh.K_TAN_THU_CAP, HoiSinh.K_TAN_THU_HOI_CHIEU, HoiSinh.K_DONG_DOI_BAT, HoiSinh.K_DONG_DOI_GIAY, HoiSinh.K_CAM_PVP,
                HoiSinh.K_CAM_GYM, HoiSinh.K_CAM_BOSS, HoiSinh.K_CAM_ME_CUNG, HoiSinh.K_MAP_PVP, HoiSinh.K_MAP_CAM_THEM,
                HoiSinh.K_AUTO_VE_MAP_UP, HoiSinh.K_GIA_THUOC, HoiSinh.K_GIA_THUOC_TOI_THIEU, HoiSinh.K_THUOC_MOI_GIO, HoiSinh.K_HP_THUOC, HoiSinh.K_HP_TRA_PHI, HoiSinh.K_XU_TOI_THIEU}) {
            // Gia hoi sinh chinh tren web nam o nhom tu_dong ("Tu dong danh & hoi sinh"), luat co dinh o hoi_sinh.
            Kiem.dung("danh muc cau hinh co " + k + " (nhom hoi_sinh / tu_dong, ap dung ngay)", j.contains("\"khoa\": \"" + k + "\", \"nhom\": \"hoi_sinh\"")
                    || j.contains("\"khoa\": \"" + k + "\", \"nhom\": \"tu_dong\""));
        }
        Kiem.dung("nhom \"Hồi sinh\"", j.contains("\"ma\": \"hoi_sinh\", \"ten\": \"Hồi sinh"));
        // mac dinh trong danh muc = mac dinh trong code
        final HoiSinh.CauHinh c = HoiSinh.cauHinh();
        final HoiSinh.CauHinh m = new HoiSinh.CauHinh();
        Kiem.dung("doc tu cau hinh trung tam = mac dinh code", c.bat == m.bat && c.xuPhanTramGio == m.xuPhanTramGio
                && c.expMatPhanNghin == m.expMatPhanNghin && c.tanThuCap == m.tanThuCap && c.hoiChieu == m.hoiChieu
                && java.util.Arrays.equals(c.heSoLap, m.heSoLap) && java.util.Arrays.equals(c.mapPvp, m.mapPvp)
                && c.metPhut == m.metPhut && c.thuocMoiGio == m.thuocMoiGio && c.giaThuocPhanTramGio == m.giaThuocPhanTramGio && c.doBen == m.doBen && c.autoVeMapUp == m.autoVeMapUp && c.luongKhoa == m.luongKhoa);
    }

    static void noiVaoGame() {
        Kiem.dung("Char.actorDie -> HoiSinhGame.khiGuc", doc("src/real/Char.java").contains("HoiSinhGame.khiGuc(this)"));
        Kiem.dung("Char: khong tu hoi sinh theo gio duoi cap 50 khi co che bat", doc("src/real/Char.java").contains("!HoiSinhGame.apDung(this)"));
        Kiem.dung("Char: Auto ve Map Up sau guc chi khi cho phep", doc("src/real/Char.java").contains("HoiSinhGame.choAutoVeMapUp()"));
        Kiem.dung("Char.getAttack: Met moi", doc("src/real/Char.java").contains("HoiSinhGame.apMetTanCong(this, att)"));
        Kiem.dung("ExpLauDai: Met moi -EXP", doc("src/real/ExpLauDai.java").contains("HoiSinhGame.apMetExp(c,"));
        final String v = doc("src/real/CharVongLap.java");
        Kiem.dung("CharVongLap: hop thoai moi thay popup cu + nhip", v.contains("HoiSinhGame.sauMotGiay(self)") && v.contains("HoiSinhGame.nhip(self)"));
        final String m = doc("src/real/Map.java");
        Kiem.dung("Map: popup -1 / 1 cu -> chanCu", m.split("HoiSinhGame.chanCu\\(player\\)", -1).length - 1 >= 2);
        Kiem.dung("Map.doComeHomeWhenDie: Hoenn + phat sau khi ve", m.contains("HoiSinhGame.diemVeTrungTam(player)") && m.contains("HoiSinhGame.sauVeTrungTam(player)"));
        Kiem.dung("MapDoMenu: menu -803/-804 + -802 cu", doc("src/real/MapDoMenu.java").contains("HoiSinhGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)")
                && doc("src/real/MapDoMenu.java").contains("HoiSinhGame.chanCu(player)"));
        Kiem.dung("MapXuLyLenh: lenh -101", doc("src/real/MapXuLyLenh.java").contains("case -101:") && doc("src/real/MapXuLyLenh.java").contains("HoiSinhGame.xuLy(player, message)"));
        Kiem.dung("VatPhamDungLauDai: dung thuoc tu tui qua luat hoi sinh", doc("src/real/VatPhamDungLauDai.java").contains("HoiSinhGame.chanDungTuTui(p)"));
        Kiem.dung("ChiSoMayChu: chi so hoiSinh", doc("src/server/ChiSoMayChu.java").contains("real.HoiSinhGame.json()"));
        Kiem.dung("BossLauDai.dangDanhBoss", doc("src/real/BossLauDai.java").contains("static synchronized boolean dangDanhBoss(final Char p)"));
        Kiem.dung("MoPhongKinhTe: mo-dun hoi sinh", doc("../tools/pokemon/MoPhongKinhTe.java").contains("HoiSinhMoPhong"));
        Kiem.bang("ma lenh -101", -101, real.HoiSinhGame.LENH);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
