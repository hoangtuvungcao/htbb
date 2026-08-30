import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cong bat/tat su kien (real/SuKien) - moi su kien phai bat duoc bat ky luc nao.
 *
 * VAN DE DA SUA (30/08/2026). Truoc do moi ham Char.isSuKien* so ngay he thong voi mot
 * moc GHI CUNG trong khoang 2015..2024:
 *
 *     return getDayTime(0L).compareTo("2020-11-01 00:00:00") &lt; 0;   // Halloween
 *
 * Moc nao cung da qua, nen moi su kien tat vinh vien va khong the mo lai neu khong sua
 * ma nguon roi build lai jar. Test nay chot lai ba dieu:
 *
 *   1. KHONG CON MOC NGAY NAO. Quet ma nguon Char.java: khong ham su kien nao duoc
 *      phep goi getDayTime/getDayOpen nua. Day la phep kiem quan trong nhat - de sot
 *      mot ham la su kien do lai chet cung nhu cu, va khong ai phat hien duoc bang mat
 *      vi trang quan tri van hien no nhu binh thuong.
 *
 *   2. MOI HAM DEU BAT/TAT DUOC THAT. Goi tung ham qua phan chieu, dat cong bat roi
 *      tat, doi chieu ket qua. Bat duoc bang phan chieu nghia la them mot ham su kien
 *      moi ma quen noi vao SuKien se do ngay tai day.
 *
 *   3. CAP is* / isFinishAll* NGUOC NHAU. Rat nhieu menu viet "!isFinishAllSuKienX()",
 *      nen mot cap lech nhau la bat su kien len ma menu van dong.
 *
 * HAI CAI BAY KHI DOC TEST NAY:
 *   - isSuKienNoel() luon false tren server lien dau / server local, va isSuKienSnNgude()
 *     luon false khi sv.me=1. Do la dieu kien ve LOAI MAY CHU, khong phai ngay thang,
 *     nen co y giu lai. Test chay ngoai server (khong nap server.ini) nen ca hai co deu
 *     tat, tuc hai ham nay bat duoc binh thuong.
 *   - Char co khoi static cham CSDL. Test chi goi ham tinh nen khong sao, nhung dung
 *     them phep kiem nao can du lieu that vao day.
 */
public final class KiemSuKien {

    /**
     * Ham su kien -> ma cong, va chieu.
     * true  = ham tra ve DUNG khi su kien BAT (isSuKienX)
     * false = ham tra ve DUNG khi su kien TAT (isFinishAllSuKienX)
     */
    private static final Object[][] BANG = {
        {"isFinishAllSuKien", real.SuKien.TAT_CA, Boolean.FALSE},

        {"isSuKienNoel", real.SuKien.NOEL, Boolean.TRUE},
        {"isFinishAllSuKienNoel", real.SuKien.NOEL, Boolean.FALSE},
        {"isFinishSuKien", real.SuKien.NOEL, Boolean.FALSE},
        {"isSuKienNoel2023", real.SuKien.NOEL_2023, Boolean.TRUE},
        {"isDemNoel", real.SuKien.DEM_NOEL, Boolean.TRUE},

        {"isSuKienTet2017", real.SuKien.TET_2017, Boolean.TRUE},
        {"isFinishAllSuKienTet2017", real.SuKien.TET_2017, Boolean.FALSE},
        {"isSuKienTetduonglich2024", real.SuKien.TET_DUONG_LICH, Boolean.TRUE},
        {"isSuKienTet2016", real.SuKien.TET_2016, Boolean.TRUE},
        {"isFinishAllSuKienTet2016", real.SuKien.TET_2016, Boolean.FALSE},
        {"is123Tet", real.SuKien.TET_123, Boolean.TRUE},

        {"isSuKienBlackFriday", real.SuKien.BLACK_FRIDAY, Boolean.TRUE},
        {"isOpenDauGiaChoiBlackFriday", real.SuKien.BLACK_FRIDAY_DAU_GIA, Boolean.TRUE},
        {"isOpenSellBlackFriday", real.SuKien.BLACK_FRIDAY_BAN, Boolean.TRUE},

        {"isSuKienTrungThul2016", real.SuKien.TRUNG_THU, Boolean.TRUE},
        {"isFinishAllSuKienTrungThul2016", real.SuKien.TRUNG_THU, Boolean.FALSE},
        {"isDemTrungThu", real.SuKien.DEM_TRUNG_THU, Boolean.TRUE},
        {"isSuKienTrungThu2015", real.SuKien.TRUNG_THU_2015, Boolean.TRUE},
        {"isFinishAllSuKienTrungthu2015", real.SuKien.TRUNG_THU_2015, Boolean.FALSE},
        {"isBanLongDenHoaSen", real.SuKien.LONG_DEN_HOA_SEN, Boolean.TRUE},

        {"isSuKienHaloween2016", real.SuKien.HALLOWEEN, Boolean.TRUE},
        {"isFinishAllSuKienHaloween2016", real.SuKien.HALLOWEEN, Boolean.FALSE},
        {"isDemHaloween", real.SuKien.DEM_HALLOWEEN, Boolean.TRUE},
        {"isDemHaloween2023", real.SuKien.DEM_HALLOWEEN, Boolean.TRUE},
        {"isSuKienHalowwen2015", real.SuKien.HALLOWEEN_2015, Boolean.TRUE},
        {"isFinishAllSuKienHaloween2015", real.SuKien.HALLOWEEN_2015, Boolean.FALSE},

        {"isSuKienHe2017", real.SuKien.HE_2017, Boolean.TRUE},
        {"isFinishAllSuKienHe2017", real.SuKien.HE_2017, Boolean.FALSE},
        {"isFinishAllSuKienHe2015", real.SuKien.HE_2015, Boolean.FALSE},

        {"isSuKienVuLan2015", real.SuKien.VU_LAN, Boolean.TRUE},
        {"isFinishAllSuKienVulan2015", real.SuKien.VU_LAN, Boolean.FALSE},

        {"isSuKienGioTo2016", real.SuKien.GIO_TO, Boolean.TRUE},
        {"isFinishAllSuKienGioTo2016", real.SuKien.GIO_TO, Boolean.FALSE},

        {"isSuKienMini", real.SuKien.MINI, Boolean.TRUE},
        {"isFinishAllSuKienMini", real.SuKien.MINI, Boolean.FALSE},
        {"isSuKienMiniChucNu", real.SuKien.MINI_CHUC_NU, Boolean.TRUE},
        {"isFinishSuKienMiniChucNu", real.SuKien.MINI_CHUC_NU, Boolean.FALSE},
        {"isSuKienMiniNuiChauBau", real.SuKien.MINI_NUI_CHAU_BAU, Boolean.TRUE},

        {"isSuKien83", real.SuKien.QUOC_TE_8_3, Boolean.TRUE},
        {"isSuKien2011l2016", real.SuKien.SINH_NHAT_2011, Boolean.TRUE},
        {"isSuKienSnNgude", real.SuKien.SINH_NHAT_NGUDE, Boolean.TRUE},
        {"isFinishAllSuKienSnNgude", real.SuKien.SINH_NHAT_NGUDE, Boolean.FALSE},
        {"isSinhNhatNgude", real.SuKien.SINH_NHAT_NGUDE_QUA, Boolean.TRUE},
        {"canReceiveGiftSNNgude", real.SuKien.SINH_NHAT_NGUDE_QUA, Boolean.TRUE},
        {"isSuKienDuaTopHoaLu", real.SuKien.DUA_TOP_HOA_LU, Boolean.TRUE},

        {"isSuKienWordcup2017", real.SuKien.WORLD_CUP_2017, Boolean.TRUE},
    };

    public static void chay() {
        Kiem.nhom("Su kien - moi su kien phai bat duoc bat ky luc nao");

        // Map.openLog KHOI TAO BANG true trong ma nguon, con server.ini that dat sv.me=0
        // nen tren may chu no la false. Test khong nap server.ini, phai tu dat - neu
        // khong thi hai ham Sinh nhat Ngude luon tra false va phep kiem ben duoi do nham.
        final boolean openLogCu = real.Map.openLog;
        real.Map.openLog = false;
        try {
            chayThat();
        } finally {
            real.Map.openLog = openLogCu;
        }

        // Chot rieng cai chan sv.me: day la dieu kien ve LOAI MAY CHU chu khong phai
        // ngay thang, nen co y giu lai khi bo cac moc ngay.
        real.Map.openLog = true;
        try {
            dat(real.SuKien.SINH_NHAT_NGUDE, true);
            Kiem.sai("sv.me=1 van chan Sinh nhat Ngude du da bat cong",
                    real.Char.isSuKienSnNgude());
        } finally {
            real.SuKien.dat(new HashMap<String, Boolean>());
            real.Map.openLog = openLogCu;
        }
    }

    private static void chayThat() {
        // ================= 1. KHONG CON MOC NGAY GHI CUNG =================
        final String nguon = doc("src/real/Char.java");
        Kiem.dung("doc duoc src/real/Char.java", nguon.length() > 1000);
        final List<String> conNgay = hamSuKienConSoNgay(nguon);
        Kiem.bang("so ham su kien con so ngay he thong", Integer.valueOf(0),
                Integer.valueOf(conNgay.size()));
        for (final String h : conNgay) {
            Kiem.dung(h + " khong con moc ngay ghi cung", false);
        }

        // ================= 2. MOI CONG DEU CO TRONG DANH MUC =================
        final Set<String> maCoThat = new HashSet<String>();
        for (final real.SuKien.Muc m : real.SuKien.danhMuc()) {
            maCoThat.add(m.ma);
            Kiem.dung("muc " + m.ma + " co ten", m.ten != null && !m.ten.isEmpty());
            Kiem.dung("muc " + m.ma + " co nhom", m.nhom != null && !m.nhom.isEmpty());
            Kiem.dung("muc " + m.ma + " co mo ta", m.moTa != null && !m.moTa.isEmpty());
        }
        Kiem.dung("danh muc khong co ma trung",
                maCoThat.size() == real.SuKien.danhMuc().length);

        for (final Object[] d : BANG) {
            Kiem.dung("ma " + d[1] + " co trong danh muc", maCoThat.contains(d[1]));
        }

        // ================= 3. BAT / TAT THAT SU DOI KET QUA =================
        for (final Object[] d : BANG) {
            final String tenHam = (String) d[0];
            final String ma = (String) d[1];
            final boolean thuan = ((Boolean) d[2]).booleanValue();

            final Boolean khiBat = goi(tenHam, ma, true);
            final Boolean khiTat = goi(tenHam, ma, false);
            if (khiBat == null || khiTat == null) {
                Kiem.dung("goi duoc Char." + tenHam + "()", false);
                continue;
            }
            Kiem.bang(tenHam + " khi bat " + ma,
                    Boolean.valueOf(thuan), khiBat);
            Kiem.bang(tenHam + " khi tat " + ma,
                    Boolean.valueOf(!thuan), khiTat);
        }

        // ================= 4. KHONG SOT HAM NAO =================
        // Moi ham public static boolean ten kieu isSuKien*/isFinishAll*SuKien* trong Char
        // deu phai co mat trong BANG o tren. Them ham moi ma quen la do ngay.
        final Set<String> daPhu = new HashSet<String>();
        for (final Object[] d : BANG) {
            daPhu.add((String) d[0]);
        }
        for (final Method m : real.Char.class.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) || m.getParameterTypes().length != 0
                    || m.getReturnType() != boolean.class) {
                continue;
            }
            final String t = m.getName();
            if (t.contains("SuKien") || t.contains("Sukien")) {
                Kiem.dung("ham " + t + " co trong bang doi chieu cua test", daPhu.contains(t));
            }
        }

        // ================= 5. WORLD CUP THEO CUNG MOT CONG =================
        try {
            dat(real.SuKien.WORLD_CUP, true);
            Kiem.dung("bat cong worldcup -> dang binh chon", real.SuKienWorldCup.dangDienRa());
            Kiem.sai("dang binh chon thi chua phat qua", real.SuKienWorldCup.dangHanNhanQua());

            dat(real.SuKien.WORLD_CUP, false);
            dat2(real.SuKien.WORLD_CUP, false, real.SuKien.WORLD_CUP_NHAN_QUA, true);
            Kiem.sai("tat binh chon -> quai khong roi bong vang", real.SuKienWorldCup.dangDienRa());
            Kiem.dung("bat cong phat qua -> dang phat qua", real.SuKienWorldCup.dangHanNhanQua());
            Kiem.dung("dang phat qua thi menu van hien", real.SuKienWorldCup.hienTrongMenu());

            dat2(real.SuKien.WORLD_CUP, false, real.SuKien.WORLD_CUP_NHAN_QUA, false);
            Kiem.sai("tat ca hai -> menu khong hien", real.SuKienWorldCup.hienTrongMenu());
        } finally {
            real.SuKien.dat(new HashMap<String, Boolean>());
        }

        // ================= 6. MA NGUON: NAP TRUOC KHI DUNG TEMPLATE =================
        final String sv = doc("src/server/TeamServer.java");
        final int viTriNap = sv.indexOf("real.SuKien.nap();");
        final int viTriInit = sv.indexOf("RealController.intance.initData();");
        Kiem.dung("TeamServer co goi SuKien.nap()", viTriNap > 0);
        Kiem.dung("nap cong su kien TRUOC khi dung bang template/shop",
                viTriNap > 0 && viTriInit > 0 && viTriNap < viTriInit);

        // ================= 7. TRANG QUAN TRI =================
        final String php = doc("web/admin/sukien.php");
        Kiem.dung("co trang web/admin/sukien.php", !php.isEmpty());
        Kiem.dung("trang quan tri dung dung ten bang", php.contains("'tob_su_kien'"));
        // May chu so huu cac cot mo ta; web chi duoc ghi cot bat/sua_*. Ghi de len cot cua
        // may chu thi lan khoi dong sau bi xoa sach, va nguoc lai may chu ghi vao cot `bat`
        // se nuot mat thiet lap cua admin.
        Kiem.sai("trang quan tri KHONG ghi de cot cua may chu",
                php.contains("SET ten =") || php.contains("SET dang_bat =")
                        || php.contains("SET nhom ="));
        final String dbSuKien = doc("src/data/DatabaseSuKien.java");
        Kiem.dung("may chu KHONG ghi de cot `bat` khi cap nhat anh chup",
                dbSuKien.contains("ON DUPLICATE KEY UPDATE")
                        && !dbSuKien.contains("bat = VALUES(bat)"));

        final String layout = doc("web/admin/inc/layout.php");
        Kiem.dung("menu quan tri co muc su kien",
                layout.contains("'sukien.php'") && layout.contains("'Sự kiện'"));

        // ================= 8. SQL MIGRATE KHOP VOI CREATE TABLE TRONG JAVA =================
        // May chu tu tao bang, con tep sql/ la ban chep tay de chay truoc. Hai ban lech
        // cot thi ai chay tep sql se co mot bang ma may chu khong ghi duoc - va loi do chi
        // lo ra thanh mot dong log lang le luc khoi dong.
        final String sql = doc("sql/them_bang_su_kien.sql");
        Kiem.dung("co tep sql/them_bang_su_kien.sql", !sql.isEmpty());
        final String[] cot = {"ma", "bat", "ten", "nhom", "mo_ta", "dang_bat",
            "sua_boi", "sua_luc", "may_chu_luc", "mo_luc", "dong_luc"};
        for (final String c : cot) {
            Kiem.dung("cot " + c + " co trong CREATE TABLE cua Java",
                    dbSuKien.contains("\"" + c + " ") || dbSuKien.contains("+ \"" + c + " ")
                            || dbSuKien.contains(c + " VARCHAR") || dbSuKien.contains(c + " TINYINT")
                            || dbSuKien.contains(c + " DATETIME"));
            Kiem.dung("cot " + c + " co trong tep sql", sql.contains("`" + c + "`"));
        }
    }

    // ---------------------------------------------------------------------------

    /** Dat mot cong roi goi ham, tra ve null neu khong goi duoc. */
    private static Boolean goi(final String tenHam, final String ma, final boolean bat) {
        try {
            dat(ma, bat);
            final Method m = real.Char.class.getMethod(tenHam);
            return (Boolean) m.invoke(null);
        } catch (final Exception e) {
            return null;
        } finally {
            real.SuKien.dat(new HashMap<String, Boolean>());
        }
    }

    private static void dat(final String ma, final boolean bat) {
        final Map<String, Boolean> m = new HashMap<String, Boolean>();
        m.put(ma, Boolean.valueOf(bat));
        real.SuKien.dat(m);
    }

    private static void dat2(final String ma1, final boolean bat1,
            final String ma2, final boolean bat2) {
        final Map<String, Boolean> m = new HashMap<String, Boolean>();
        m.put(ma1, Boolean.valueOf(bat1));
        m.put(ma2, Boolean.valueOf(bat2));
        real.SuKien.dat(m);
    }

    /**
     * Ham su kien nao trong Char.java con so ngay he thong.
     *
     * Quet tho: tim tung "public static boolean is...(" co ten dinh toi su kien roi doc
     * than ham den dau ngoac dong cung cap. Chi can biet co goi getDayTime/getDayOpen
     * hay khong, khong can hieu bieu thuc.
     */
    private static List<String> hamSuKienConSoNgay(final String nguon) {
        final List<String> ra = new ArrayList<String>();
        final java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("public static boolean (is[A-Za-z0-9_]*|can[A-Za-z0-9_]*)\\s*\\(\\s*\\)")
                .matcher(nguon);
        while (m.find()) {
            final String ten = m.group(1);
            if (!laHamSuKien(ten)) {
                continue;
            }
            final String than = layThan(nguon, m.end());
            if (than.contains("getDayTime") || than.contains("getDayOpen")) {
                ra.add(ten);
            }
        }
        return ra;
    }

    private static boolean laHamSuKien(final String ten) {
        return ten.contains("SuKien") || ten.contains("Sukien") || ten.contains("Dem")
                || ten.contains("123Tet") || ten.contains("BlackFriday")
                || ten.contains("SNNgude") || ten.contains("SinhNhatNgude")
                || ten.contains("LongDenHoaSen");
    }

    /** Than ham bat dau tu dau ngoac nhon dau tien sau viTri. */
    private static String layThan(final String nguon, final int viTri) {
        final int mo = nguon.indexOf('{', viTri);
        if (mo < 0) {
            return "";
        }
        int sau = 1;
        for (int i = mo + 1; i < nguon.length(); i++) {
            final char c = nguon.charAt(i);
            if (c == '{') {
                sau++;
            } else if (c == '}') {
                sau--;
                if (sau == 0) {
                    return nguon.substring(mo, i);
                }
            }
        }
        return nguon.substring(mo);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemSuKien() {
    }
}
