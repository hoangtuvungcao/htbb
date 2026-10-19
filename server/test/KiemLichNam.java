import com.htbb.pokemon.AmLich;
import com.htbb.pokemon.LichNam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lich su kien nam theo ngay that (chu game duyet 19/10/2026): am lich Viet Nam, cua so moi le, moc 04:00 gio Viet
 * Nam, tu bat / tat, hoan khoi dong lai toi bao tri. Xem com.htbb.pokemon.LichNam / AmLich, data.DatabaseLichNam,
 * real.LichSuKien.dongBoLichNam.
 */
public final class KiemLichNam {

    private KiemLichNam() {
    }

    /**
     * Ngay le am lich Viet Nam da cong bo (lich Vien Han lam / lich van nien Ho Ngoc Duc), {nam, Tet, Gio To 10/3,
     * Vu Lan 15/7, Trung Thu 15/8} dang "thang-ngay" duong lich. 2030: Tet Viet Nam 2/2 (Trung Quoc 3/2 - lech mui gio).
     */
    static final String[][] AM_LICH = {
        {"2026", "2-17", "4-26", "8-27", "9-25"},
        {"2027", "2-6", "4-16", "8-16", "9-15"},
        {"2028", "1-26", "4-4", "9-3", "10-3"},
        {"2029", "2-13", "4-23", "8-24", "9-22"},
        {"2030", "2-2", "4-12", "8-13", "9-12"},
        {"2031", "1-23", "4-1", "9-1", "10-1"},
        {"2032", "2-11", "4-19", "8-20", "9-19"},
        {"2033", "1-31", "4-9", "8-9", "9-8"},
        {"2034", "2-19", "4-28", "8-28", "9-26"},
        {"2035", "2-8", "4-17", "8-18", "9-16"},
    };

    public static void chay() {
        Kiem.nhom("Lich su kien nam - am lich, cua so le, moc 04:00, tu bat / tat, hoan toi bao tri");
        amLich();
        cuaSo();
        danhMuc();
        hoanBaoTri();
        nguon();
    }

    static String md(final int[] d) {
        return d[1] + "-" + d[0];
    }

    static void amLich() {
        final int[][] am = {{1, 1}, {10, 3}, {15, 7}, {15, 8}};
        final String[] ten = {"Tet Nguyen Dan", "Gio To 10/3", "Vu Lan 15/7", "Trung Thu 15/8"};
        for (final String[] r : AM_LICH) {
            final int nam = Integer.parseInt(r[0]);
            for (int i = 0; i < am.length; i++) {
                final int[] d = AmLich.amSangDuong(am[i][0], am[i][1], nam, false);
                Kiem.bang(ten[i] + " " + nam, r[0] + "-" + r[i + 1], d[2] + "-" + md(d));
                final int[] nguoc = AmLich.duongSangAm(d[0], d[1], d[2]);
                Kiem.dung(ten[i] + " " + nam + ": doi nguoc ra dung ngay / thang am",
                        nguoc[0] == am[i][0] && nguoc[1] == am[i][1] && nguoc[3] == 0);
            }
        }
        final int[] hom = AmLich.duongSangAm(19, 10, 2026);
        Kiem.dung("19/10/2026 = 10/9 am lich Binh Ngo", hom[0] == 10 && hom[1] == 9 && hom[2] == 2026);
        final int[] nhuan = AmLich.amSangDuong(1, 5, 2028, true);
        Kiem.dung("2028 co thang 5 nhuan (ton tai ngay 1/5 nhuan)", nhuan[2] == 2028);
        Kiem.bang("2028 nhuan thang 5, khong co thang 3 nhuan", 0, AmLich.amSangDuong(1, 3, 2028, true)[2]);
    }

    static long ms(final int y, final int m, final int d, final int h) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.clear();
        c.set(y, m - 1, d, h, 0, 0);
        return c.getTimeInMillis();
    }

    static boolean trong(final String ma, final int y, final int m, final int d, final int h) {
        return LichNam.dangDienRa(ma, ms(y, m, d, h));
    }

    static void cuaSo() {
        for (int y = 2026; y <= 2030; y++) {
            Kiem.dung("Tet Duong lich " + y + ": dang dien ra 1/1", trong("tetduonglich", y, 1, 1, 12));
            Kiem.dung("Valentine " + y + ": 14/2", trong("moi:valentine", y, 2, 14, 20));
            Kiem.dung("8/3 " + y, trong("quocte83", y, 3, 8, 12));
            Kiem.dung("30/4 va 1/5 " + y, trong("moi:3004", y, 4, 30, 12) && trong("moi:3004", y, 5, 1, 20));
            Kiem.dung("Thieu nhi 1/6 " + y, trong("moi:thieunhi", y, 6, 1, 12));
            Kiem.dung("He " + y + ": 1/7", trong("he2017", y, 7, 1, 12));
            Kiem.dung("Quoc khanh 2/9 " + y, trong("moi:quockhanh", y, 9, 2, 12));
            Kiem.dung("20/10 " + y, trong("moi:2010", y, 10, 20, 12));
            Kiem.dung("Halloween " + y + ": 31/10 toi", trong("halloween", y, 10, 31, 22));
            Kiem.dung("Dem Halloween " + y + ": 31/10", trong("demhalloween", y, 10, 31, 20));
            Kiem.sai("Dem Halloween " + y + ": khong bat 30/10", trong("demhalloween", y, 10, 30, 20));
            Kiem.sai("Halloween " + y + ": tat sau 3/11 04:00", trong("halloween", y, 11, 3, 5));
            Kiem.dung("20/11 " + y, trong("sinhnhat2011", y, 11, 20, 12));
            Kiem.dung("Noel " + y + ": 24/12 va 25/12", trong("noel", y, 12, 24, 22) && trong("noel", y, 12, 25, 12));
            Kiem.dung("Dem Noel " + y + ": toi 24/12", trong("demnoel", y, 12, 24, 23));
            for (final String[] r : AM_LICH) {
                if (Integer.parseInt(r[0]) != y) {
                    continue;
                }
                final String[] tet = r[1].split("-");
                final int tm = Integer.parseInt(tet[0]);
                final int td = Integer.parseInt(tet[1]);
                Kiem.dung("Tet Nguyen Dan " + y + ": mung 1", trong("tet2017", y, tm, td, 12));
                Kiem.dung("Ba ngay Tet " + y + ": mung 1", trong("tet123", y, tm, td, 12));
                final long mung4 = ms(y, tm, td, 12) + 3 * 86400000L;
                Kiem.sai("Ba ngay Tet " + y + ": tat mung 4", LichNam.dangDienRa("tet123", mung4));
                final String[] tt = r[4].split("-");
                Kiem.dung("Trung Thu " + y + ": 15/8 am", trong("trungthu", y, Integer.parseInt(tt[0]), Integer.parseInt(tt[1]), 20));
                final String[] gt = r[2].split("-");
                Kiem.dung("Gio To " + y + ": 10/3 am", trong("gioto", y, Integer.parseInt(gt[0]), Integer.parseInt(gt[1]), 12));
                final String[] vl = r[3].split("-");
                Kiem.dung("Vu Lan " + y + ": 15/7 am", trong("vulan", y, Integer.parseInt(vl[0]), Integer.parseInt(vl[1]), 12));
            }
        }
        // Black Friday: thu Sau thu 4 cua thang 11.
        Kiem.dung("Black Friday 2026 = 27/11", trong("blackfriday", 2026, 11, 27, 12));
        Kiem.dung("Black Friday 2027 = 26/11", trong("blackfriday", 2027, 11, 26, 12));
        Kiem.sai("Black Friday 2026: khong bat 20/11", trong("blackfriday", 2026, 11, 20, 12));

        // Moi moc mo / dong luc 04:00 gio Viet Nam (truoc bao tri 04:30-05:30).
        boolean dung04 = true;
        String sai = "";
        for (final LichNam.CuaSo c : LichNam.tatCa(2026, 2030)) {
            for (final long t : new long[]{c.mo, c.dong}) {
                final Calendar k = Calendar.getInstance(LichNam.MUI_GIO);
                k.setTimeInMillis(t);
                if (k.get(Calendar.HOUR_OF_DAY) != LichNam.GIO_CHUYEN || k.get(Calendar.MINUTE) != 0) {
                    dung04 = false;
                    sai = c.muc.ma + " " + c.nam;
                }
            }
            if (c.dong <= c.mo) {
                dung04 = false;
                sai = c.muc.ma + " " + c.nam + " dong <= mo";
            }
        }
        Kiem.dung("moi moc mo / dong 2026-2030 luc 04:00 gio Viet Nam, dong sau mo " + sai, dung04);
        Kiem.bang("tuan ISO 43/2026: thu Hai 19/10 04:00", ms(2026, 10, 19, 4), LichNam.thuHaiTuanIso(2026, 43));

        // Hom nay (19/10/2026, 15h): Halloween + 20/10 bat, dem Halloween chua, Noel chua.
        final long homNay = ms(2026, 10, 19, 15);
        Kiem.dung("19/10/2026: Halloween dang dien ra (giu nguyen tren may that)", LichNam.dangDienRa("halloween", homNay));
        Kiem.dung("19/10/2026: 20/10 dang dien ra", LichNam.dangDienRa("moi:2010", homNay));
        Kiem.sai("19/10/2026: dem Halloween chua bat", LichNam.dangDienRa("demhalloween", homNay));
        Kiem.sai("19/10/2026: Noel chua bat", LichNam.dangDienRa("noel", homNay));
        Kiem.sai("19/10/2026: tuan he 42 vua het luc 04:00", LichNam.dangDienRa("moi:tuanhe", homNay));
    }

    static void danhMuc() {
        final Set<String> suKien = new HashSet<String>();
        for (final real.SuKien.Muc m : real.SuKien.danhMuc()) {
            suKien.add(m.ma);
        }
        final Set<String> daCo = new HashSet<String>();
        for (final LichNam.Muc m : LichNam.DANH_MUC) {
            Kiem.dung("lich " + m.ma + " co trong SuKien.DANH_MUC", suKien.contains(m.ma));
            Kiem.dung("lich " + m.ma + " khong trung", daCo.add(m.ma));
            Kiem.dung("lich " + m.ma + " co ten / mo ta / phan thuong / ngay le",
                    !m.ten.isEmpty() && !m.moTa.isEmpty() && !m.phanThuong.isEmpty() && !m.ngayLe.isEmpty());
            final List<LichNam.CuaSo> c = LichNam.cuaSo(m, 2027);
            Kiem.dung("lich " + m.ma + " co it nhat mot lan trong nam", !c.isEmpty());
        }
        // Ma KHONG theo lich = bat tay (ban cu / mini / cai dat bang top).
        final String[] thuCong = {"noel2023", "halloween2015", "he2015", "mini", "minichucnu", "mininuichaubau", "anxuluongtop"};
        int soThuCong = 0;
        for (final String ma : suKien) {
            if (!daCo.contains(ma)) {
                boolean coTrongDs = false;
                for (final String t : thuCong) {
                    coTrongDs |= t.equals(ma);
                }
                Kiem.dung("ma " + ma + " khong co lich phai nam trong danh sach thu cong", coTrongDs);
                soThuCong++;
            }
        }
        Kiem.bang("so ma thu cong", thuCong.length, soThuCong);
        for (final String bo : new String[]{"worldcup", "worldcupnhanqua", "worldcup2017", "snngude", "duatophoalu"}) {
            Kiem.sai("su kien kiem hiep " + bo + " da bo khoi danh muc", suKien.contains(bo));
            Kiem.sai("su kien kiem hiep " + bo + " khong co lich", daCo.contains(bo));
        }
        Kiem.dung("le Viet Nam du: Tet, Valentine, 8/3, Gio To, 30/4, 1/6, he, Trung Thu, Halloween, 20/11, Noel, Tet DL",
                daCo.containsAll(java.util.Arrays.asList("tet2017", "moi:valentine", "quocte83", "gioto", "moi:3004",
                        "moi:thieunhi", "he2017", "trungthu", "halloween", "sinhnhat2011", "noel", "tetduonglich")));
        Kiem.dung("dieu kien SQL 'khong theo lich' liet ke ma lich", data.DatabaseLichNam.KHONG_THEO_LICH.contains("'halloween'")
                && data.DatabaseLichNam.KHONG_THEO_LICH.startsWith("NOT (tu_dong = 1"));
    }

    static void hoanBaoTri() {
        Kiem.bang("bao tri 05:10, bay gio 04:00 -> con 70 phut", 70, real.BaoTriDaily.phutDen(240, 310));
        Kiem.bang("bao tri 04:40, bay gio 23:00 -> con 340 phut", 340, real.BaoTriDaily.phutDen(1380, 280));
        Kiem.bang("lich doi luc 04:00, bao tri con 70 phut: KHONG tu khoi dong lai", 0, real.LichSuKien.soChoTuKhoiDong(2, 2, 70));
        Kiem.bang("lich + admin cung doi: chi tinh dong cua admin", 1, real.LichSuKien.soChoTuKhoiDong(3, 2, 70));
        Kiem.bang("bao tri con 10 gio (admin bat som): khoi dong lai nhu cu", 2, real.LichSuKien.soChoTuKhoiDong(2, 2, 600));
        Kiem.bang("bao tri tu dong da tat (-1): khoi dong lai nhu cu", 2, real.LichSuKien.soChoTuKhoiDong(2, 2, -1));
    }

    static void nguon() {
        final String lich = doc("src/real/LichSuKien.java");
        Kiem.dung("LichSuKien moi nhip dong bo lich nam truoc khi chay lich hen tay",
                lich.indexOf("dongBoLichNam();") > 0 && lich.indexOf("dongBoLichNam();") < lich.indexOf("DatabaseSuKien.chayLichDenHan();"));
        Kiem.dung("LichSuKien tru dong lich khi bao tri sap toi", lich.contains("soDongChoKhoiDongLaiTheoLich()"));
        final String sk = doc("src/real/SuKien.java");
        Kiem.dung("SuKien.nap dong bo lich nam TRUOC khi doc bat",
                sk.indexOf("LichSuKien.dongBoLichNam();") > 0 && sk.indexOf("LichSuKien.dongBoLichNam();") < sk.indexOf("data.DatabaseSuKien.nap();"));
        Kiem.sai("bo hen lich 52 tuan ISO cu (henLichNam)", doc("src/real/LauDaiJob.java").contains("henLichNam()"));
        final String db = doc("src/data/DatabaseLichNam.java");
        Kiem.dung("dong bo bat so sanh bang NOW() cua CSDL", db.contains("l.mo_luc <= NOW() AND l.dong_luc > NOW()"));
        Kiem.dung("dong ghi de 'tat nam nay' (ghi_de = 1) khong bat", db.contains("l.ghi_de <> 1"));
        Kiem.dung("ghi lich giu gio admin da doi (ghi_de <> 0)", db.contains("IF(ghi_de = 0, VALUES(mo_luc), mo_luc)"));
        final String sql = DayDu.nangCap();
        Kiem.dung("htbb_day_du.sql co tob_su_kien_lich + cot tob_su_kien.tu_dong", DayDu.coBang("tob_su_kien_lich")
                && DayDu.cot("tob_su_kien").contains("tu_dong"));
        Kiem.dung("sql/nang_cap.sql: bang + cot tu_dong + xoa ma kiem hiep",
                sql.contains("CREATE TABLE IF NOT EXISTS tob_su_kien_lich") && sql.contains("ADD COLUMN IF NOT EXISTS tu_dong")
                && sql.contains("'worldcup'"));
        boolean du = true;
        for (final String ma : LichNam.dsMa()) {
            du &= sql.contains("'" + ma + "'");
        }
        Kiem.dung("sql xoa moc ISO cu cho MOI ma co lich", du);
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
