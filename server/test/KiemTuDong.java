import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NangLuong;
import com.htbb.pokemon.TuDong;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Tu dong danh chinh thuc (com.htbb.pokemon.TuDong + real.TuDongGame, 18/10/2026): tran gio / ngay (+ the thang),
 * dieu kien bat (bai luyen, PvP, san dau, chet, tat tinh nang), het gio thi tat, ban kinh, nhip nguoi that (cua so
 * truot) + vi pham thi tat, doi ngay, luu ho so (a1/a2), cau hinh trung tam (danh muc + doc gia tri), KHONG cham
 * EXP (khong thuong them), va cac cho noi vao game (MapXuLyLenh -100, RealController 22, MapDanhNhau, BossLauDai).
 */
public final class KiemTuDong {

    private KiemTuDong() {
    }

    static final int NGAY = 20261019;
    static final long T0 = 1_800_000_000_000L;

    public static void chay() {
        Kiem.nhom("Tu dong danh chinh thuc (TuDong) - gioi han + dieu kien + chong bot");
        try {
            tranGio();
            dieuKienBat();
            hetGio();
            banKinh();
            nhip();
            doiNgayVaHoSo();
            hoiSinh();
            cauHinh();
            khongThuongThem();
            noiVaoGame();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static TuDong.CauHinh ch() {
        return new TuDong.CauHinh();
    }

    static TuDong.NguCanh nc() {
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 12;
        n.x = 400;
        n.y = 300;
        return n;
    }

    static void tranGio() {
        final TuDong.CauHinh c = ch();
        Kiem.bang("mac dinh 5 gio / ngay", 5 * 3600, TuDong.giayToiDa(c, false));
        Kiem.bang("the thang +2 gio", 7 * 3600, TuDong.giayToiDa(c, true));
        c.gioNgay = 4;
        c.gioTheThang = 0;
        Kiem.bang("gio_them_the_thang = 0: the thang khong them", 4 * 3600, TuDong.giayToiDa(c, true));
        final TuDong.Phien p = new TuDong.Phien();
        Kiem.bang("phien moi: con du 4 gio", 4 * 3600, TuDong.giayConLai(c, p, false, NGAY));
    }

    static void dieuKienBat() {
        final TuDong.CauHinh c = ch();
        final TuDong.Phien p = new TuDong.Phien();
        Kiem.bang("bai luyen binh thuong: duoc bat", null, TuDong.kiemBat(c, p, nc(), NGAY));
        TuDong.NguCanh n = nc();
        n.baiLuyen = false;
        Kiem.dung("chi_map_da_mo=1: ngoai bai luyen -> tu choi", TuDong.kiemBat(c, p, n, NGAY) != null);
        c.chiMapDaMo = false;
        Kiem.bang("chi_map_da_mo=0: ngoai bai luyen van bat duoc", null, TuDong.kiemBat(c, p, n, NGAY));
        c.chiMapDaMo = true;
        n = nc();
        n.pvp = true;
        Kiem.dung("dang co PK -> tu choi", TuDong.kiemBat(c, p, n, NGAY) != null);
        n = nc();
        n.sanDau = true;
        Kiem.dung("san dau / Gym / chien truong -> tu choi", TuDong.kiemBat(c, p, n, NGAY) != null);
        c.tatKhiPvp = false;
        Kiem.bang("tat_khi_pvp=0: san dau van bat duoc", null, TuDong.kiemBat(c, p, n, NGAY));
        c.tatKhiPvp = true;
        n = nc();
        n.chet = true;
        Kiem.dung("Pokemon dang guc -> tu choi", TuDong.kiemBat(c, p, n, NGAY) != null);
        c.bat = false;
        Kiem.bang("tu_dong.bat=0 -> tu choi voi cau tam khoa", TuDong.lyDo(TuDong.TAT_TAT_TINH_NANG), TuDong.kiemBat(c, p, nc(), NGAY));
        c.bat = true;
        p.ngay = NGAY;
        p.msDung = 5L * 3600000L;
        Kiem.bang("da dung het 5 gio -> tu choi het gio", TuDong.lyDo(TuDong.TAT_HET_GIO), TuDong.kiemBat(c, p, nc(), NGAY));
        n = nc();
        n.theThang = true;
        Kiem.bang("the thang con 2 gio -> van bat duoc", null, TuDong.kiemBat(c, p, n, NGAY));
    }

    static void hetGio() {
        final TuDong.CauHinh c = ch();
        c.gioNgay = 1;
        c.gioTheThang = 0;
        final TuDong.Phien p = new TuDong.Phien();
        p.ngay = NGAY;
        TuDong.bat(c, p, nc(), 0, T0);
        Kiem.dung("bat: dang chay", p.bat);
        Kiem.bang("ban kinh mac dinh = toi da may chu", c.banKinh, p.banKinh);
        long t = T0;
        boolean het = false;
        int buoc = 0;
        // nhip 20 giay mot lan, 1 gio = 180 nhip
        while (!het && buoc < 1000) {
            t += 20000L;
            het = TuDong.tich(c, p, false, t, NGAY);
            buoc++;
        }
        Kiem.bang("het dung 1 gio sau 180 nhip 20s", 180, buoc);
        Kiem.sai("het gio: phien tat", p.bat);
        Kiem.bang("ly do tat = het gio", TuDong.TAT_HET_GIO, p.lyDoTat);
        Kiem.bang("con lai 0", 0, TuDong.giayConLai(c, p, false, NGAY));
        Kiem.dung("bat lai cung ngay -> tu choi", TuDong.kiemBat(c, p, nc(), NGAY) != null);
        // khoang trong lon (client treo / mat mang) chi tinh toi da 90 giay
        final TuDong.Phien q = new TuDong.Phien();
        q.ngay = NGAY;
        TuDong.bat(c, q, nc(), 0, T0);
        TuDong.tich(c, q, false, T0 + 3600000L, NGAY);
        Kiem.bang("khoang 1 gio khong nhip chi tinh 90 giay", 90000L, q.msDung);
        // da tat thi khong cong
        TuDong.tat(q, TuDong.TAT_NGUOI_CHOI);
        TuDong.tich(c, q, false, T0 + 3610000L, NGAY);
        Kiem.bang("da tat: khong cong them", 90000L, q.msDung);
    }

    static void banKinh() {
        final TuDong.CauHinh c = ch();
        final TuDong.Phien p = new TuDong.Phien();
        TuDong.bat(c, p, nc(), 160, T0);
        Kiem.bang("xin 160px (< toi da 240): dung 160", 160, p.banKinh);
        Kiem.sai("trong ban kinh", TuDong.raVung(p, 12, 400 + 150, 300));
        Kiem.sai("trong le 64px", TuDong.raVung(p, 12, 400 + 220, 300));
        Kiem.dung("ra khoi ban kinh + le", TuDong.raVung(p, 12, 400 + 230, 300));
        Kiem.dung("sang map khac = ra vung", TuDong.raVung(p, 13, 400, 300));
        final TuDong.Phien q = new TuDong.Phien();
        TuDong.bat(c, q, nc(), 5000, T0);
        Kiem.bang("xin qua lon: kep ve toi da may chu", 240, q.banKinh);
        final TuDong.Phien r = new TuDong.Phien();
        TuDong.bat(c, r, nc(), 10, T0);
        Kiem.bang("xin qua nho: kep ve 48", TuDong.BAN_KINH_MIN, r.banKinh);
    }

    static void nhip() {
        final TuDong.CauHinh c = ch();
        c.viPhamTat = 5;
        final TuDong.Phien p = new TuDong.Phien();
        TuDong.bat(c, p, nc(), 0, T0);
        int duoc = 0;
        for (int i = 0; i < 12; i++) {
            if (TuDong.choPhep(c, p, TuDong.HD_DANH, T0 + i * 10L)) {
                duoc++;
            }
        }
        Kiem.bang("12 don trong 0,12 giay: nhan du 12 (nhip 12/5)", 12, duoc);
        Kiem.sai("don 13 trong cua so 5 giay bi bo", TuDong.choPhep(c, p, TuDong.HD_DANH, T0 + 200L));
        Kiem.dung("het cua so 5 giay: nhan lai", TuDong.choPhep(c, p, TuDong.HD_DANH, T0 + 5001L));
        Kiem.bang("dem 1 vi pham", 1, p.viPham);
        Kiem.dung("van chay sau 1 vi pham", p.bat);
        for (int i = 0; i < 4; i++) {
            TuDong.choPhep(c, p, TuDong.HD_DANH, T0 + 5002L + i);
        }
        Kiem.sai("5 vi pham (vi_pham_tat=5): tat phien", p.bat);
        Kiem.bang("ly do: vi pham nhip", TuDong.TAT_VI_PHAM, p.lyDoTat);
        Kiem.dung("phien da tat: khong chan nua (thao tac tay)", TuDong.choPhep(c, p, TuDong.HD_DANH, T0 + 5200L));
        // thuoc / nhat co cua so rieng
        final TuDong.CauHinh c2 = ch();
        final TuDong.Phien q = new TuDong.Phien();
        TuDong.bat(c2, q, nc(), 0, T0);
        int thuoc = 0;
        for (int i = 0; i < 10; i++) {
            if (TuDong.choPhep(c2, q, TuDong.HD_THUOC, T0 + i)) {
                thuoc++;
            }
        }
        Kiem.bang("thuoc: 4 lan / 5 giay", 4, thuoc);
        Kiem.dung("thuoc vuot khong lam mat nhip danh", TuDong.choPhep(c2, q, TuDong.HD_DANH, T0 + 20L));
        // nhip nguoi that: chi do
        final TuDong.Phien r = new TuDong.Phien();
        boolean vuot = false;
        for (int i = 0; i < 31; i++) {
            vuot = TuDong.vuotNhipNguoi(c, r, T0 + i);
        }
        Kiem.dung("31 don / 5 giay: vuot nhip nguoi that (30/5)", vuot);
        Kiem.sai("nhip nguoi that khong bat phien", r.bat);
        Kiem.bang("doc nhip '6/2'", "6/2s", TuDong.Nhip.doc("6/2", null).toString());
        Kiem.bang("nhip hong -> mac dinh", TuDong.NHIP_DANH_MAC_DINH, TuDong.Nhip.doc("abc", TuDong.NHIP_DANH_MAC_DINH));
    }

    static void hoiSinh() {
        final TuDong.CauHinh c = ch();
        final TuDong.Phien p = new TuDong.Phien();
        TuDong.bat(c, p, nc(), 0, T0);
        Kiem.sai("khong bat Tu hoi sinh: khong duoc tu dung vat pham", TuDong.choHoiSinh(c, p, T0));
        p.xinHoiSinh = true;
        Kiem.dung("bat Tu hoi sinh: lan 1 duoc", TuDong.choHoiSinh(c, p, T0 + 1000L));
        Kiem.dung("lan 2 trong gio duoc (mac dinh 2/gio)", TuDong.choHoiSinh(c, p, T0 + 2000L));
        Kiem.sai("lan 3 trong 60 phut: khong (phai ve Trung tam)", TuDong.choHoiSinh(c, p, T0 + 3000L));
        Kiem.dung("qua 60 phut: duoc lai", TuDong.choHoiSinh(c, p, T0 + 1000L + 3600000L));
        c.hoiSinhGio = 0;
        Kiem.sai("hoi_sinh_moi_gio=0: khong bao gio", TuDong.choHoiSinh(c, p, T0 + 9000000L));
        c.hoiSinhGio = 2;
        TuDong.tat(p, TuDong.TAT_NGUOI_CHOI);
        Kiem.sai("da tat tu dong: khong", TuDong.choHoiSinh(c, p, T0 + 9900000L));
        final String g = doc("src/real/TuDongGame.java");
        Kiem.dung("hook TuDongGame.dungKhiChet / choHoiSinhTuDong cong khai", g.contains("public static void dungKhiChet(final Char p)") && g.contains("public static boolean choHoiSinhTuDong(final Char p)"));
    }

    static void doiNgayVaHoSo() {
        final TuDong.CauHinh c = ch();
        final TuDong.Phien p = new TuDong.Phien();
        p.ngay = NGAY;
        TuDong.bat(c, p, nc(), 0, T0);
        TuDong.tich(c, p, false, T0 + 60000L, NGAY);
        Kiem.bang("1 phut", 60000L, p.msDung);
        TuDong.tich(c, p, false, T0 + 80000L, NGAY + 1);
        Kiem.bang("qua 00:00: bo dem ngay moi tu 0", 0L, p.msDung);
        Kiem.bang("ngay moi", NGAY + 1, p.ngay);
        Kiem.dung("phien van chay qua ngay", p.bat);
        TuDong.tich(c, p, false, T0 + 100000L, NGAY + 1);
        Kiem.bang("ngay moi cong binh thuong", 20000L, p.msDung);
        final HoSoLauDai h = new HoSoLauDai();
        TuDong.ghiHoSo(p, h);
        Kiem.dung("ghi ho so danh dau ban", h.ban);
        final HoSoLauDai h2 = HoSoLauDai.giaiMa(h.maHoa());
        Kiem.bang("ho so a1 (ngay) qua ma hoa", NGAY + 1, h2.tdNgay);
        Kiem.bang("ho so a2 (ms) qua ma hoa", 20000L, h2.tdMsDung);
        final TuDong.Phien q = new TuDong.Phien();
        TuDong.napTuHoSo(q, h2);
        Kiem.bang("dang nhap lai: nap lai so phut da dung", 20000L, q.msDung);
        Kiem.sai("dang nhap lai: phien moi TAT (khong tu bat lai)", q.bat);
        Kiem.bang("ho so cu khong co a1/a2: 0", 0L, HoSoLauDai.giaiMa("nn=1;nm=5").tdMsDung);
    }

    static void cauHinh() {
        boolean du = true;
        for (final String k : new String[]{TuDong.K_BAT, TuDong.K_GIO_NGAY, TuDong.K_GIO_THE_THANG, TuDong.K_CHI_MAP_DA_MO,
            TuDong.K_TAT_KHI_PVP, TuDong.K_TU_DUNG_THUOC, TuDong.K_BAN_KINH, TuDong.K_NHIP_DANH, TuDong.K_NHIP_THUOC,
            TuDong.K_NHIP_NHAT, TuDong.K_NHIP_NGUOI, TuDong.K_VI_PHAM_TAT, TuDong.K_HOI_SINH_GIO}) {
            du &= CauHinhTrungTam.dinhNghia(k) != null;
        }
        Kiem.dung("13 khoa tu_dong.* co trong danh muc trung tam", du);
        final Map<String, String> db = new HashMap<String, String>();
        db.put(TuDong.K_GIO_NGAY, "4");
        db.put(TuDong.K_TU_DUNG_THUOC, "0");
        db.put(TuDong.K_BAN_KINH, "160");
        db.put(TuDong.K_NHIP_DANH, "6/3");
        CauHinhTrungTam.datChoTest(null, db, false);
        try {
            final TuDong.CauHinh c = TuDong.cauHinh();
            Kiem.bang("doc gio_moi_ngay tu CSDL", 4, c.gioNgay);
            Kiem.sai("doc tu_dung_thuoc=0", c.tuDungThuoc);
            Kiem.bang("doc ban_kinh", 160, c.banKinh);
            Kiem.bang("doc nhip_danh", "6/3s", c.nhipDanh.toString());
            Kiem.dung("mac dinh tu_dong.bat=1", c.bat);
            Kiem.dung("mac dinh tat_khi_pvp=1", c.tatKhiPvp);
            Kiem.dung("mac dinh chi_map_da_mo=1", c.chiMapDaMo);
            Kiem.bang("co gui client: tinh nang + PvP + chi map, khong thuoc", TuDong.CO_TINH_NANG | TuDong.CO_TAT_PVP | TuDong.CO_CHI_MAP, c.co());
        } finally {
            CauHinhTrungTam.datChoTest(null, null, false);
        }
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        Kiem.dung("danh_muc.json co nhom tu_dong", dm.contains("\"ma\": \"tu_dong\""));
    }

    static void khongThuongThem() {
        final String nguon = doc("src/com/htbb/pokemon/TuDong.java") + doc("src/real/TuDongGame.java");
        // tu dong khong co duong EXP / xu / roi rieng: moi phan thuong di qua ha quai thuong (NangLuong)
        Kiem.sai("TuDong / TuDongGame khong cong EXP", nguon.contains("addExp") || nguon.contains("congExp") || nguon.contains(".xp +="));
        Kiem.sai("TuDong / TuDongGame khong doi he so nang luong", nguon.contains("heSo(") || nguon.contains("nlPhut100"));
        // nang luong: cung mot thoi gian danh -> cung he so, khong phu thuoc tu dong
        final HoSoLauDai h = new HoSoLauDai();
        int hs = 100;
        for (int i = 0; i < 190; i++) {
            hs = NangLuong.ghiNhan(h, T0 + i * 55000L, 180, 360);
        }
        Kiem.bang("sau ~3 gio danh lien tuc van 100% (chua qua 180 phut)", 100, hs);
        Kiem.dung("phieu uy thac (treo offline) van rieng trong TangToc", doc("src/com/htbb/pokemon/TangToc.java").contains("UY_THAC"));
    }

    static void noiVaoGame() {
        final String x = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("MapXuLyLenh: lenh -100 -> TuDongGame.xuLy", x.contains("case -100:") && x.contains("TuDongGame.xuLy(player, message)"));
        Kiem.dung("MapXuLyLenh: kiem nhip truoc switch", x.contains("TuDongGame.kiemLenh(player, message.command)"));
        Kiem.dung("RealController: thuoc (22) qua nhip tu dong", doc("src/real/RealController.java").contains("TuDongGame.kiemLenh(p, 22)"));
        Kiem.dung("MapDanhNhau: bi danh -> dung tu dong", doc("src/real/MapDanhNhau.java").contains("TuDongGame.khiPvp(c)"));
        Kiem.dung("BossLauDai: danh boss -> dung tu dong", doc("src/real/BossLauDai.java").contains("TuDongGame.khiGapBoss(p)"));
        Kiem.bang("ma lenh -100", -100, TuDong.LENH);
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
