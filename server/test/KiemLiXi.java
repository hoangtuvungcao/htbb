import com.htbb.pokemon.GioiHanQua;
import com.htbb.pokemon.LiXiLogic;
import com.htbb.pokemon.LiXiLogic.CauHinh;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Li xi Tet (chu game duyet 19/10/2026): bao li xi co tran ngay / ca dot, xu theo gio thu nhap (het giai 1 - 5 trieu),
 * phan xu qua cong chong clone; ma li xi = luong KHOA, moi tai khoan / thiet bi / IP mot lan, tran tong.
 */
public final class KiemLiXi {

    private KiemLiXi() {
    }

    public static void chay() {
        Kiem.nhom("Li xi Tet - tran nhan / mo bao, xu theo gio thu nhap, ma li xi luong khoa theo tai khoan / thiet bi");
        boDem();
        tui();
        ma();
        quetMa();
    }

    static void boDem() {
        final GioiHanQua.BoNho k = new GioiHanQua.BoNho();
        Kiem.dung("bo dem: cong trong tran", k.thu("m", "a", 1, 2, 3, "20260101") && k.thu("m", "a", 1, 2, 3, "20260101"));
        Kiem.sai("bo dem: vuot tran ngay", k.thu("m", "a", 1, 2, 3, "20260101"));
        Kiem.dung("bo dem: ngay moi duoc tiep", k.thu("m", "a", 1, 2, 3, "20260102"));
        Kiem.sai("bo dem: vuot tran ca dot", k.thu("m", "a", 1, 2, 3, "20260103"));
        Kiem.bang("bo dem: tong", 3L, k.tong("m", "a"));
        k.hoan("m", "a", 1, "20260102");
        Kiem.dung("bo dem: hoan thi cong lai duoc", k.thu("m", "a", 1, 2, 3, "20260102"));
        Kiem.sai("bo dem: them <= 0 khong cong", k.thu("m", "b", 0, 2, 3, "20260101"));
    }

    static void tui() {
        final CauHinh c = CauHinh.macDinh();
        final GioiHanQua.BoNho k = new GioiHanQua.BoNho();
        final String dot = "2026";
        Kiem.bang("nhan bao 1", null, LiXiLogic.nhanTui(c, k, dot, "20260201", "A"));
        Kiem.bang("nhan bao 2", null, LiXiLogic.nhanTui(c, k, dot, "20260201", "a"));
        Kiem.dung("nhan bao 3 trong ngay: chan (2 / ngay, khong con 'nhat ca 100 bao')", LiXiLogic.nhanTui(c, k, dot, "20260201", "a") != null);
        Kiem.bang("ngay sau nhan tiep", null, LiXiLogic.nhanTui(c, k, dot, "20260202", "a"));
        int mo = 0;
        for (int ngay = 1; ngay <= 10; ngay++) {
            for (int i = 0; i < 5; i++) {
                if (LiXiLogic.moTui(c, k, dot, "202602" + (ngay < 10 ? "0" : "") + ngay, "a") == null) {
                    mo++;
                }
            }
        }
        Kiem.bang("10 ngay x 5 lan: mo duoc dung 15 bao ca dot (3 / ngay)", 15, mo);
        Kiem.dung("het dot: bao loi ca dot", LiXiLogic.moTui(c, k, dot, "20260215", "a").contains("đợt"));
        Kiem.bang("dot moi (nam sau) mo lai duoc", null, LiXiLogic.moTui(c, k, "2027", "20270201", "a"));

        final long g20 = com.htbb.pokemon.LauDai.xuMoiGio(20);
        final Random r = new Random(5);
        boolean trongKhoang = true;
        for (int i = 0; i < 2000; i++) {
            final long x = LiXiLogic.xuTui(c, g20, r);
            trongKhoang &= x >= g20 * 5 / 100 && x <= g20 * 10 / 100;
        }
        Kiem.dung("xu bao cap 20 trong 5 - 10% gio (" + g20 * 5 / 100 + " - " + g20 / 10 + " xu)", trongKhoang);
        Kiem.bang("giai lon = 1 gio thu nhap cap 20", g20, LiXiLogic.giaiLon(c, g20));
        Kiem.dung("giai lon cap 90 << 1.000.000 xu cu", LiXiLogic.giaiLon(c, com.htbb.pokemon.LauDai.xuMoiGio(90)) < 1000000L);
        Kiem.bang("giai lon cau hinh 10000% bi kep 300%", 300, new CauHinh(2, 3, 15, 5, 10, 10000, 5, 15, 1, 20000).giaiLonPhanTramGio);
        final long caDot = c.tuiMoDot * g20 * c.xuPhanTramGioMax / 100 + 5 * LiXiLogic.giaiLon(c, g20);
        Kiem.dung("tran xu ca dot / Pokemon cap 20 (15 bao + ca 5 giai) <= 7 gio thu nhap", caDot <= 7 * g20);
    }

    static void ma() {
        final CauHinh c = CauHinh.macDinh();
        final GioiHanQua.BoNho k = new GioiHanQua.BoNho();
        final String dot = "2026";
        final String n = "20260201";
        final Random r = new Random(1);
        boolean khoang = true;
        for (int i = 0; i < 500; i++) {
            final int l = LiXiLogic.luongKhoaMa(c, r);
            khoang &= l >= 5 && l <= 15;
        }
        Kiem.dung("ma: 5 - 15 luong khoa", khoang);
        Kiem.bang("tai khoan 1, may 1: duoc", null, LiXiLogic.giuMa(c, k, dot, n, 1, "MAY1", "1.1.1.1", 10));
        Kiem.dung("tai khoan 1 lan hai (Pokemon khac cung tai khoan): chan", LiXiLogic.giuMa(c, k, dot, n, 1, "MAY9", "9.9.9.9", 10) != null);
        Kiem.dung("tai khoan 2 cung thiet bi: chan", LiXiLogic.giuMa(c, k, dot, n, 2, "MAY1", "2.2.2.2", 10) != null);
        Kiem.dung("tai khoan 3 cung IP: chan", LiXiLogic.giuMa(c, k, dot, n, 3, "MAY3", "1.1.1.1", 10) != null);
        Kiem.bang("tai khoan 2 bi chan thi KHONG mat luot (thiet bi khac van nhap duoc)", null,
                LiXiLogic.giuMa(c, k, dot, n, 2, "MAY2", "2.2.2.2", 10));
        LiXiLogic.hoanMa(c, k, dot, n, 2, "MAY2", "2.2.2.2", 10);
        Kiem.bang("ma sai -> hoan: tai khoan nhap lai duoc", null, LiXiLogic.giuMa(c, k, dot, n, 2, "MAY2", "2.2.2.2", 10));
        final CauHinh quyNho = new CauHinh(2, 3, 15, 5, 10, 100, 5, 15, 1, 25);
        final GioiHanQua.BoNho k2 = new GioiHanQua.BoNho();
        Kiem.bang("quy 25: ma 15 duoc", null, LiXiLogic.giuMa(quyNho, k2, dot, n, 10, "A", "", 15));
        final String het = LiXiLogic.giuMa(quyNho, k2, dot, n, 11, "B", "", 15);
        Kiem.dung("quy 25: ma 15 thu hai het quy", het != null && het.contains("hết"));
        Kiem.bang("het quy: tai khoan 11 khong bi tinh da nhap", 0L, k2.tong(LiXiLogic.maNhapMa(dot), "u11"));
        Kiem.bang("tong luong khoa phat ra <= quy", 15L, k2.tong(LiXiLogic.maTongLuong(dot), "*"));
    }

    static void quetMa() {
        final String ch = doc("src/real/Char.java");
        final int a = ch.indexOf("public static synchronized void doOpenLixi2021(");
        final String mo = a < 0 ? "" : ch.substring(a, Math.min(ch.length(), a + 7000));
        Kiem.dung("mo bao: kiem tran truoc (LiXiLogic.moTui), vuot thi tra lai bao", mo.contains("LiXiLogic.moTui(") && mo.contains("p.potions[159]++"));
        Kiem.sai("mo bao: het addXu(Char.XU_LI_XI[j]) 1 - 5 trieu", mo.contains("addXu(Char.XU_LI_XI"));
        Kiem.sai("mo bao: het 10.000 - 20.000 / 5.000 - 105.000 co dinh", mo.contains("getRandomMinMax(10000, 20000)") || mo.contains("5000 + Map.r.nextInt(100000)"));
        Kiem.dung("mo bao: moi phan xu qua traXuLiXi (cong chong clone + thiet bi)", mo.contains("traXuLiXi(p,")
                && ch.matches("(?s).*static long traXuLiXi\\(.*ChongClone.lyDo\\(p, true\\).*SO_NGAY.kiemMay\\(.*"));
        Kiem.dung("giai lon: toi da so giai / dot (o dem *)", mo.contains("LiXiLogic.maGiaiLon(dotLiXi()), \"*\""));
        Kiem.dung("nhan bao: tran ngay (LiXiLogic.nhanTui)", doc("src/real/Map.java").contains("LiXiLogic.nhanTui(Char.cauHinhLiXi()"));
        final String nt = doc("src/real/MapNhapText.java");
        final int b = nt.indexOf("if (idActor == -32051) {");
        final String m = b < 0 ? "" : nt.substring(b, Math.min(nt.length(), b + 4000));
        Kiem.dung("ma: cong chong clone", m.contains("ChongClone.lyDo(player, true)"));
        Kiem.dung("ma: tai khoan / thiet bi / quy (LiXiLogic.giuMa) roi chot ma nguyen tu", m.contains("LiXiLogic.giuMa(")
                && m.contains("DatabaseGioiHanQua.chotMaLiXi(") && m.indexOf("giuMa(") < m.indexOf("chotMaLiXi("));
        Kiem.dung("ma: tra LUONG KHOA (addLuongLock), khong tra luong mo", m.contains("player.addLuongLock(luong)") && !m.contains("player.addLuong(luong)"));
        final String dm = DayDu.danhMucJson(); // danh muc trong CSDL (ban chup sql/htbb.sql)
        Kiem.dung("danh muc li_xi.* (giai lon 100% gio, ma 5-15 luong khoa, quy 20.000)",
                dm.matches("(?s).*\"li_xi.tui_giai_lon_phan_tram_gio\"[^}]*\"macDinh\": \"100\".*")
                        && dm.matches("(?s).*\"li_xi.ma_luong_khoa_max\"[^}]*\"macDinh\": \"15\".*")
                        && dm.matches("(?s).*\"li_xi.ma_tong_luong_khoa_dot\"[^}]*\"macDinh\": \"20000\".*"));
        Kiem.dung("sql/htbb.sql (phan 9): bang tob_gioi_han_qua + khoa li_xi", DayDu.nangCap().contains("tob_gioi_han_qua")
                && DayDu.nangCap().contains("'li_xi.tui_mo_ngay_toi_da'"));
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
