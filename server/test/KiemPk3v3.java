import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.Pk3v3;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PK 3 VS 3 BUOI TOI (chu game 05/11/2026) - phan THUAN com.htbb.pokemon.Pk3v3 (gio mo, bac cap, ghep doi + chong clone /
 * gap lai, xet thuong + chong cay / treo / bot, bang xep hang) + du lieu (khoa danh muc pk3v3.* o SQL phan A + B / gen.ts /
 * docs, khoi them_pk3v3.sql, danh hieu tuan, tai lieu, client). Luong game (hang cho, tran, bo cuoc, rot mang, luot / tai
 * khoan, chot ngay / tuan): test/real/KiemPk3v3Game.
 */
public final class KiemPk3v3 {

    private KiemPk3v3() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    /** ms cua gio Viet Nam ngay 05/11/2026 (thu Nam) {@code gio}:{@code phut}. */
    static long luc(final int ngay, final int gio, final int phut) {
        final Calendar c = Calendar.getInstance(com.htbb.pokemon.LichNam.MUI_GIO);
        c.clear();
        c.set(2026, Calendar.NOVEMBER, ngay, gio, phut, 0);
        return c.getTimeInMillis();
    }

    public static void chay() {
        Kiem.nhom("PK 3 vs 3 buoi toi (Pk3v3): gio mo, bac cap, ghep 3v3 + chong clone, xet thuong / chong cay, du lieu");
        try {
            gioMo();
            bacCap();
            ghep();
            xet();
            bangXepHang();
            duLieu();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.bang("KiemPk3v3 khong nem loi", "", String.valueOf(e));
        }
    }

    // ------------------------------------------------------------------ gio mo

    static void gioMo() {
        Kiem.sai("18:59 chua mo (19-23)", Pk3v3.dangMo(luc(5, 18, 59), 19, 23));
        Kiem.dung("19:00 mo", Pk3v3.dangMo(luc(5, 19, 0), 19, 23));
        Kiem.dung("22:59 con mo", Pk3v3.dangMo(luc(5, 22, 59), 19, 23));
        Kiem.sai("23:00 da dong", Pk3v3.dangMo(luc(5, 23, 0), 19, 23));
        Kiem.sai("buoi sang khong mo", Pk3v3.dangMo(luc(5, 9, 0), 19, 23));
        Kiem.dung("dong 24 = nua dem: 23:30 con mo", Pk3v3.dangMo(luc(5, 23, 30), 19, 24));
        Kiem.dung("qua nua dem (22 -> 2): 01:00 mo", Pk3v3.dangMo(luc(6, 1, 0), 22, 2));
        Kiem.sai("qua nua dem (22 -> 2): 03:00 dong", Pk3v3.dangMo(luc(6, 3, 0), 22, 2));
        Kiem.bang("khung gio chu", "19:00–23:00", Pk3v3.khungGio(19, 23));
        Kiem.bang("khung gio 24 = 00:00", "19:00–00:00", Pk3v3.khungGio(19, 24));
        Kiem.bang("ngay (gio VN) 05/11/2026 22:00", Integer.valueOf(20261105), Integer.valueOf(Pk3v3.ngay(luc(5, 22, 0))));
        Kiem.bang("tuan = tuan cup (thu Hai 05:00)", Integer.valueOf(CupLogic.tuan(luc(5, 20, 0))), Integer.valueOf(Pk3v3.tuan(luc(5, 20, 0))));
        Kiem.bang("mac dinh 19-23 gio, 500 tran", "19/23/500", Pk3v3.GIO_MO + "/" + Pk3v3.GIO_DONG + "/" + Pk3v3.TRAN_NGAY);
    }

    // ------------------------------------------------------------------ bac cap

    static void bacCap() {
        Kiem.bang("cap 19 < toi thieu: -1", Integer.valueOf(-1), Integer.valueOf(Pk3v3.bac(19, 20)));
        Kiem.bang("cap 20: bac 0", Integer.valueOf(0), Integer.valueOf(Pk3v3.bac(20, 20)));
        Kiem.bang("cap 39: bac 0", Integer.valueOf(0), Integer.valueOf(Pk3v3.bac(39, 20)));
        Kiem.bang("cap 40: bac 1", Integer.valueOf(1), Integer.valueOf(Pk3v3.bac(40, 20)));
        Kiem.bang("cap 120: bac cuoi", Integer.valueOf(Pk3v3.SO_BAC - 1), Integer.valueOf(Pk3v3.bac(120, 20)));
        Kiem.bang("cap 140: van bac cuoi", Integer.valueOf(Pk3v3.SO_BAC - 1), Integer.valueOf(Pk3v3.bac(140, 20)));
        Kiem.bang("ten bac 1", "cấp 40–59", Pk3v3.tenBac(1, 20));
        Kiem.bang("ten bac cuoi", "cấp 120+", Pk3v3.tenBac(5, 20));
    }

    // ------------------------------------------------------------------ ghep

    static Pk3v3.Nhom solo(final int id, final int bac, final long vao, final String may) {
        return nhom(new int[]{id}, bac, vao, new String[]{may});
    }

    static Pk3v3.Nhom nhom(final int[] ids, final int bac, final long vao, final String[] may) {
        final Set<String> tr = new HashSet<String>();
        final Set<String> ng = new HashSet<String>();
        for (int i = 0; i < ids.length; i++) {
            tr.addAll(CupLogic.khoaClone(may[i], "", 1000 + ids[i]));
            ng.addAll(CupLogic.khoaClone(may[i], "ip" + ids[i], 1000 + ids[i]));
        }
        return new Pk3v3.Nhom(ids, bac, vao, tr, ng);
    }

    static String tran(final List<Pk3v3.Doi[]> kq) {
        final StringBuilder b = new StringBuilder();
        for (final Pk3v3.Doi[] t : kq) {
            b.append(b.length() == 0 ? "" : " | ").append(t[0].ids()).append(" vs ").append(t[1].ids());
        }
        return b.toString();
    }

    static void ghep() {
        final List<Pk3v3.Nhom> h = new ArrayList<Pk3v3.Nhom>();
        for (int i = 1; i <= 6; i++) {
            h.add(solo(i, 0, i, "m" + i));
        }
        Kiem.bang("6 nguoi le cung bac: 1 tran 3 dau 3 theo thu tu vao hang", "[1, 2, 3] vs [4, 5, 6]", tran(Pk3v3.ghep(h, null, 3)));
        Kiem.bang("5 nguoi le: chua du, khong ghep", "", tran(Pk3v3.ghep(h.subList(0, 5), null, 3)));
        final List<Pk3v3.Nhom> lech = new ArrayList<Pk3v3.Nhom>();
        for (int i = 1; i <= 6; i++) {
            lech.add(solo(i, i <= 3 ? 0 : 1, i, "m" + i));
        }
        Kiem.bang("bac cap khac nhau khong ghep chung (3 bac 0 + 3 bac 1)", "", tran(Pk3v3.ghep(lech, null, 3)));
        final List<Pk3v3.Nhom> doi = new ArrayList<Pk3v3.Nhom>();
        doi.add(nhom(new int[]{1, 2, 3}, 2, 1, new String[]{"a", "b", "c"}));
        doi.add(solo(4, 2, 2, "d"));
        doi.add(solo(5, 2, 3, "e"));
        doi.add(solo(6, 2, 4, "f"));
        Kiem.bang("to doi 3 nguoi + 3 nguoi le", "[1, 2, 3] vs [4, 5, 6]", tran(Pk3v3.ghep(doi, null, 3)));
        final List<Pk3v3.Nhom> hai = new ArrayList<Pk3v3.Nhom>();
        hai.add(nhom(new int[]{1, 2}, 0, 1, new String[]{"a", "b"}));
        hai.add(nhom(new int[]{3, 4}, 0, 2, new String[]{"c", "d"}));
        hai.add(solo(5, 0, 3, "e"));
        hai.add(solo(6, 0, 4, "f"));
        Kiem.bang("hai to doi 2 nguoi + 2 nguoi le: to doi luon chung doi", "[1, 2, 5] vs [3, 4, 6]", tran(Pk3v3.ghep(hai, null, 3)));
        // cung thiet bi: 1 va 4 cung may -> khong bao gio o hai ben (cung doi cung khong: khoa trong)
        final List<Pk3v3.Nhom> clone = new ArrayList<Pk3v3.Nhom>();
        for (int i = 1; i <= 6; i++) {
            clone.add(solo(i, 0, i, i == 4 ? "m1" : "m" + i));
        }
        Kiem.bang("cung thiet bi (1 va 4): khong ghep vao cung tran", "", tran(Pk3v3.ghep(clone, null, 3)));
        for (int i = 7; i <= 9; i++) {
            clone.add(solo(i, 0, i, "m" + i));
        }
        final String k = tran(Pk3v3.ghep(clone, null, 3));
        Kiem.dung("cung thiet bi: doi cua 1 gap doi khac (khong co 4): " + k, k.equals("[1, 2, 3] vs [7, 8, 9]"));
        // gap lai: [1,2,3] da gap [4,5,6] 3 lan -> uu tien doi [7,8,9]
        final List<Pk3v3.Nhom> gl = new ArrayList<Pk3v3.Nhom>();
        for (int i = 1; i <= 9; i++) {
            gl.add(solo(i, 0, i, "m" + i));
        }
        final Pk3v3.GapLai g = (a, b) -> (a <= 3 && b >= 4 && b <= 6) || (b <= 3 && a >= 4 && a <= 6) ? 3 : 0;
        Kiem.bang("gap lai qua 3 lan: ghep doi thu khac neu co", "[1, 2, 3] vs [7, 8, 9]", tran(Pk3v3.ghep(gl, g, 3)));
        Kiem.bang("gap lai nhung khong con doi khac: van ghep (chi khong thuong)", "[1, 2, 3] vs [4, 5, 6]",
                tran(Pk3v3.ghep(gl.subList(0, 6), g, 3)));
        Kiem.bang("gap nhieu nhat giua hai doi", Integer.valueOf(3), Integer.valueOf(Pk3v3.gapNhieuNhat(Arrays.asList(1, 2), Arrays.asList(5, 9), g)));
    }

    // ------------------------------------------------------------------ xet thuong

    static String x(final Pk3v3.Xet r) {
        return r.diem + "/" + r.luong + "/" + r.thangTinhThuong + "/" + r.lyDo + "/" + r.hopLe();
    }

    static void xet() {
        Kiem.bang("tran 10 giay: khong hop le (nhanh)", "nhanh", Pk3v3.lyDoTran(Pk3v3.A_THANG, 10, 500, 20));
        Kiem.bang("ben thua gay 50 phan nghin: nhuong", "nhuong", Pk3v3.lyDoTran(Pk3v3.A_THANG, 60, 50, 20));
        Kiem.bang("hoa khong xet nhuong", null, Pk3v3.lyDoTran(Pk3v3.HOA, 60, 0, 20));
        Kiem.bang("tran binh thuong hop le", null, Pk3v3.lyDoTran(Pk3v3.B_THANG, 60, 300, 20));
        final int lt = Pk3v3.LUONG_THANG, tn = Pk3v3.THANG_THUONG_NGAY, tt = Pk3v3.TRAN_LUONG_TUAN, gl = Pk3v3.GAP_LAI_TOI_DA;
        Kiem.bang("thang hop le: 3 diem + 1 luong khoa", "3/1/true//true", x(Pk3v3.xet(1, null, 400, false, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("hoa: 1 diem, khong luong", "1/0/false//true", x(Pk3v3.xet(0, null, 400, false, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("thua: 0 diem (van tinh tran hop le)", "0/0/false//true", x(Pk3v3.xet(-1, null, 400, false, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("tran nhanh: khong gi ca", "0/0/false/nhanh/false", x(Pk3v3.xet(1, "nhanh", 400, false, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("treo may (gay 40 phan nghin): khong gi ca", "0/0/false/treo/false", x(Pk3v3.xet(1, null, 40, false, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("gap lai 3 lan roi: khong diem, khong thuong", "0/0/false/gap_lai/false", x(Pk3v3.xet(1, null, 400, false, 3, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("bot (HLV AI) thang: diem nhung khong luong khoa", "3/0/false/bot/true", x(Pk3v3.xet(1, null, 400, true, 0, 0, 0, lt, tn, tt, gl)));
        Kiem.bang("da 3 tran thang co thuong hom nay: chi diem", "3/0/false/het_thang/true", x(Pk3v3.xet(1, null, 400, false, 0, 3, 0, lt, tn, tt, gl)));
        Kiem.bang("du tran tuan 25: thang van tinh luot, 0 luong", "3/0/true/het_tuan/true", x(Pk3v3.xet(1, null, 400, false, 0, 0, 25, lt, tn, tt, gl)));
        Kiem.bang("top ngay hang 1: 3 luong khoa", Integer.valueOf(3), Integer.valueOf(Pk3v3.luongTopNgay(1, 5, 0, 25)));
        Kiem.bang("top ngay hang 3: 1", Integer.valueOf(1), Integer.valueOf(Pk3v3.luongTopNgay(3, 9, 0, 25)));
        Kiem.bang("top ngay chua du 5 tran: 0", Integer.valueOf(0), Integer.valueOf(Pk3v3.luongTopNgay(1, 4, 0, 25)));
        Kiem.bang("top ngay kep tran tuan (con 1)", Integer.valueOf(1), Integer.valueOf(Pk3v3.luongTopNgay(1, 9, 24, 25)));
        Kiem.bang("hang 4 khong co thuong ngay", Integer.valueOf(0), Integer.valueOf(Pk3v3.luongTopNgay(4, 9, 0, 25)));
        Kiem.bang("danh hieu tuan hang 1", Integer.valueOf(Pk3v3.DH_VUA), Integer.valueOf(Pk3v3.danhHieuTuan(1, 10)));
        Kiem.bang("danh hieu tuan hang 10", Integer.valueOf(Pk3v3.DH_DAU_SI), Integer.valueOf(Pk3v3.danhHieuTuan(10, 12)));
        Kiem.bang("hang 11 / chua du 10 tran: khong danh hieu", "0/0", Pk3v3.danhHieuTuan(11, 20) + "/" + Pk3v3.danhHieuTuan(1, 9));
        // tran luong khoa / tuan cua nguoi cay toi da: 3 thang x 7 ngay + top 1 moi ngay 21 -> van <= 25
        final int toiDa = Math.min(Pk3v3.TRAN_LUONG_TUAN, 7 * (Pk3v3.THANG_THUONG_NGAY * Pk3v3.LUONG_THANG + Pk3v3.LK_TOP_NGAY[0]));
        Kiem.dung("khong lam phat: toi da 25 luong khoa / tuan / tai khoan (ngang Dau truong Bao Boi)", toiDa <= 25);
        Kiem.dung("luat chu co gio + 500 tran + thuong", Pk3v3.luatChu(19, 23, 500, 20, 1, 3, 25, 3, 20).contains("19:00–23:00")
                && Pk3v3.luatChu(19, 23, 500, 20, 1, 3, 25, 3, 20).contains("500 trận / ngày / tài khoản"));
    }

    // ------------------------------------------------------------------ bang xep hang

    static void bangXepHang() {
        final List<Pk3v3.Diem> ds = new ArrayList<Pk3v3.Diem>();
        final int[][] v = {{1, 9, 5, 100}, {2, 9, 4, 200}, {3, 12, 8, 50}, {4, 15, 6, 10}, {5, 0, 0, 0}};
        for (final int[] r : v) {
            final Pk3v3.Diem d = new Pk3v3.Diem(r[0]);
            d.diem = r[1];
            d.hopLe = r[2];
            d.luc = r[3];
            d.ten = "n" + r[0];
            ds.add(d);
        }
        ds.get(3).bot = true;
        final List<Pk3v3.Diem> b = Pk3v3.xepHang(ds);
        final StringBuilder s = new StringBuilder();
        for (final Pk3v3.Diem d : b) {
            s.append(d.charId).append(' ');
        }
        Kiem.bang("xep hang: diem, it tran hon, bo bot, bo nguoi chua co tran hop le", "3 2 1 ", s.toString());
    }

    // ------------------------------------------------------------------ du lieu

    static void duLieu() throws Exception {
        final Map<String, String[]> mong = new HashMap<String, String[]>();
        mong.put(Pk3v3.K_BAT, new String[]{"1", "chinh"});
        mong.put(Pk3v3.K_GIO_MO, new String[]{String.valueOf(Pk3v3.GIO_MO), "chinh"});
        mong.put(Pk3v3.K_GIO_DONG, new String[]{String.valueOf(Pk3v3.GIO_DONG), "chinh"});
        mong.put(Pk3v3.K_TRAN_NGAY, new String[]{String.valueOf(Pk3v3.TRAN_NGAY), "chinh"});
        mong.put(Pk3v3.K_LUONG_THANG, new String[]{String.valueOf(Pk3v3.LUONG_THANG), "chinh"});
        mong.put(Pk3v3.K_THANG_THUONG, new String[]{String.valueOf(Pk3v3.THANG_THUONG_NGAY), "co_dinh"});
        mong.put(Pk3v3.K_TRAN_LUONG_TUAN, new String[]{String.valueOf(Pk3v3.TRAN_LUONG_TUAN), "co_dinh"});
        mong.put(Pk3v3.K_CAP, new String[]{String.valueOf(Pk3v3.CAP_TOI_THIEU), "co_dinh"});
        mong.put(Pk3v3.K_GAP_LAI, new String[]{String.valueOf(Pk3v3.GAP_LAI_TOI_DA), "co_dinh"});
        mong.put(Pk3v3.K_GIAY_TOI_THIEU, new String[]{String.valueOf(Pk3v3.GIAY_TOI_THIEU), "co_dinh"});
        Kiem.bang("moi khoa pk3v3.* co mac dinh mong doi", Integer.valueOf(Pk3v3.KHOA.length), Integer.valueOf(mong.size()));
        final String sql = doc("../sql/htbb.sql");
        final String gen = doc("../web/shared/utils/cauHinhTrungTam.gen.ts");
        final String md = doc("../docs/cau-hinh.md");
        for (final String k : Pk3v3.KHOA) {
            final DinhNghiaKhoa d = CauHinhTrungTam.dinhNghia(k);
            Kiem.dung("danh muc co " + k, d != null);
            if (d != null) {
                Kiem.bang(k + " mac dinh = Pk3v3", mong.get(k)[0], d.macDinh);
                Kiem.bang(k + " nhom su_kien (hien tren trang quan tri)", "su_kien", d.nhom);
            }
            Kiem.dung("phan A + B sql co " + k + " (" + mong.get(k)[1] + ")", sql.contains("('" + k + "','su_kien'")
                    && sql.contains("('" + k + "', 'su_kien'") && sql.contains("'real.Pk3v3Game'") && sql.matches("(?s).*\\('" + java.util.regex.Pattern.quote(k)
                    + "', 'su_kien'[^\\n]*'" + mong.get(k)[1] + "', [01]\\);.*"));
            Kiem.dung("gen.ts co " + k, gen.contains("khoa: \"" + k + "\""));
            Kiem.dung("docs/cau-hinh.md co " + k, md.contains("| `" + k + "` |"));
        }
        // khoi SQL
        final String s = DayDu.phanNangCap("them_pk3v3.sql");
        Kiem.dung("sql/htbb.sql co khoi them_pk3v3.sql", s.length() > 1000);
        Kiem.sai("khoi pk3v3: khong DROP / DELETE / TRUNCATE", s.contains("DROP ") || s.contains("DELETE ") || s.contains("TRUNCATE"));
        Kiem.dung("khoi pk3v3: luat bang nguoi choi (KiemDuLieuSql.khongPhaNguoiChoi)", KiemDuLieuSql.khongPhaNguoiChoi(s));
        final String g = doc("src/real/Pk3v3Game.java");
        for (final String bang : new String[]{"tob_pk3v3_luot", "tob_pk3v3_diem", "tob_pk3v3_gap", "tob_pk3v3_tran", "tob_pk3v3_chot",
            "tob_pk3v3_thuong"}) {
            final int a = s.indexOf("CREATE TABLE IF NOT EXISTS `" + bang + "`");
            Kiem.dung("khoi pk3v3: CREATE TABLE IF NOT EXISTS `" + bang + "`", a >= 0);
            if (a < 0) {
                continue;
            }
            final String ddl = s.substring(a, s.indexOf(") ENGINE", a));
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^  `(\\w+)` ").matcher(ddl);
            final List<String> cot = new ArrayList<String>();
            while (m.find()) {
                cot.add(m.group(1));
            }
            final int ga = g.indexOf("CREATE TABLE IF NOT EXISTS " + bang + " (");
            final String gd = ga < 0 ? "" : g.substring(ga, g.indexOf("DEFAULT CHARSET", ga));
            int thieu = 0;
            for (final String c : cot) {
                thieu += gd.contains(" " + c + " ") || gd.contains("(" + c + " ") ? 0 : 1;
            }
            Kiem.bang(bang + ": Pk3v3Game.SQL_TAO_BANG co du cot SQL (" + cot.size() + ")", Integer.valueOf(0), Integer.valueOf(thieu));
        }
        final Map<String, List<Map<String, Object>>> b = KiemLoaiDayDu.docSql(s);
        final List<Map<String, Object>> dh = b.get("data_danh_hieu");
        Kiem.bang("2 danh hieu tuan 9880 / 9881", Integer.valueOf(2), Integer.valueOf(dh == null ? 0 : dh.size()));
        int ok = 0;
        for (final Map<String, Object> r : dh == null ? new ArrayList<Map<String, Object>>() : dh) {
            final int id = KiemLoaiDayDu.so(r, "id");
            ok += KiemLoaiDayDu.chu(r, "ma").equals(Pk3v3.maDanhHieu(id)) && KiemLoaiDayDu.so(r, "thoi_han_phut") == Pk3v3.PHUT_DH
                    && KiemLoaiDayDu.so(r, "phan_nghin") <= 10 && "trao".equals(KiemLoaiDayDu.chu(r, "dieu_kien")) ? 1 : 0;
        }
        Kiem.bang("danh hieu tuan: ma khop Pk3v3, 7 ngay, chi so <= 1% (ngan sach o), dieu kien trao", Integer.valueOf(2), Integer.valueOf(ok));
        Kiem.dung("phan A cung co danh hieu 9880 / 9881", DayDu.toanVan().indexOf("(9880,'pk3v3_vua'") < DayDu.toanVan().indexOf("-- >>> them_pk3v3.sql")
                && DayDu.toanVan().contains("(9881,'pk3v3_tinh_anh'"));
        Kiem.dung("sql/README.md liet ke them_pk3v3.sql", doc("../sql/README.md").contains("them_pk3v3.sql"));
        // tai lieu + duong vao
        final String tk = doc("../docs/thiet-ke-game.md");
        Kiem.dung("thiet-ke-game.md: muc PK 3 vs 3 buoi toi co so lieu", tk.contains("PK 3 vs 3 buổi tối") && tk.contains("500 trận")
                && tk.contains("19:00–23:00") && tk.contains("25 lượng khoá / tuần"));
        final String st = doc("src/real/SoTayLauDai.java");
        Kiem.dung("So tay: muc 12 = PK 3 vs 3 (sau Ve lang, khong chen vao MUC_GOC)", st.contains("MUC_PK3V3 = SO_MUC_GOC + 1")
                && st.contains("SO_MUC_DONG = SO_MUC_GOC + 2") && st.contains("goc[MUC_PK3V3] = com.htbb.pokemon.Pk3v3.TEN"));
        final String rg = doc("src/real/Region.java");
        Kiem.dung("Region: 3v3 dung lai duong danh nguoi cua cup (choDanh / satThuong / sauDon)", rg.contains("Pk3v3Game.choDanh(p, c) || CupGame.choDanh(p, c)")
                && rg.contains("Pk3v3Game.laKhu(p) ? Pk3v3Game.satThuong(p, c, d)") && rg.contains("if (!Pk3v3Game.sauDon(p, c)) {"));
        Kiem.dung("Cup PvP tuan: muc cuoi mo PK 3 vs 3 (duong client cu Menu -> Su kien -> Cup)", doc("src/real/CupGame.java").contains("MUC_PK3V3}, SoTayLauDai.NPC_ID, MENU_XEM")
                && doc("src/real/CupGame.java").contains("\"Bảng xếp hạng tuần này\", MUC_PK3V3}, SoTayLauDai.NPC_ID, MENU));"));
        Kiem.dung("NPC Dau truong (-31): muc PK 3 vs 3", doc("src/real/cmd/RequestNPCInfoHandler.java").contains("p.idxPk3v3DauTruong = menu.length;")
                && doc("src/real/MapDoMenu.java").contains("idOptionMenu == player.idxPk3v3DauTruong"));
        Kiem.dung("dang nhap: nhan thuong PK 3 vs 3 cho", doc("src/real/CharNapDuLieu.java").contains("Pk3v3Game.khiDangNhap(self);"));
        Kiem.dung("TeamServer tao bang pk3v3", doc("src/server/TeamServer.java").contains("real.Pk3v3Game.taoBang();"));
        final String cl = doc("../client_jar/src/game/render/screen/GameScr.java");
        if (!cl.isEmpty()) {
            Kiem.dung("client: the Su kien co dong PK 3 vs 3 gui -527/0/12", cl.contains("SU_KIEN_PK3V3 = \"" + Pk3v3.TEN + "\"")
                    && cl.contains("MUC_PK3V3 = 12;"));
        }
    }
}
