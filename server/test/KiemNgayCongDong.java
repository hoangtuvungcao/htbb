import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.LichNam;
import com.htbb.pokemon.NgayCongDong;
import com.htbb.pokemon.SuKienLauDai;
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
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ngay cong dong hang thang (de xuat 8): lich (1 ngay / thang, tu dong theo lich nam, 04:00 -> 04:00), dong noi bat
 * (data_ngay_cong_dong), ti le doi loai / EXP / shiny co tran, danh hieu, tin web.
 */
public final class KiemNgayCongDong {

    private KiemNgayCongDong() {
    }

    public static void chay() {
        lich();
        trongNgay();
        duLieu();
    }

    static Calendar vn(final long ms) {
        final Calendar c = Calendar.getInstance(LichNam.MUI_GIO);
        c.setTimeInMillis(ms);
        return c;
    }

    static void lich() {
        Kiem.nhom("Ngay cong dong: lich nam (1 ngay / thang, Chu nhat, 04:00 -> 04:00)");
        final LichNam.Muc m = LichNam.muc(NgayCongDong.MA_SU_KIEN);
        Kiem.dung("moi:congdong co trong lich nam", m != null);
        Kiem.sai("khong con la tuan Pokemon (7 ngay)", m != null && m.laTuanPokemon());
        Kiem.bang("nhom 'Ngày cộng đồng' (web bao truoc 3 ngay)", "Ngày cộng đồng", m == null ? "" : m.nhom);
        int sai = 0, tong = 0, trungTuan = 0, canTuan = 0;
        for (int nam = 2026; nam <= 2035; nam++) {
            final List<LichNam.CuaSo> ds = LichNam.cuaSo(m, nam);
            final Set<Integer> thang = new HashSet<Integer>();
            for (final LichNam.CuaSo c : ds) {
                tong++;
                final Calendar a = vn(c.mo), b = vn(c.dong);
                thang.add(a.get(Calendar.MONTH) + 1);
                if (a.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY || a.get(Calendar.HOUR_OF_DAY) != LichNam.GIO_CHUYEN
                        || a.get(Calendar.MINUTE) != 0 || c.dong - c.mo != 86400000L || b.get(Calendar.HOUR_OF_DAY) != LichNam.GIO_CHUYEN
                        || a.get(Calendar.YEAR) != nam) {
                    sai++;
                }
            }
            Kiem.bang(nam + ": 12 lan, moi thang mot lan", "12/12", ds.size() + "/" + thang.size());
            // tuan cong dong ISO (chuoi nhiem vu SK-xx) luon co ngay cong dong trong tuan
            for (final int tuan : SuKienLauDai.tuanCoMa(NgayCongDong.MA_SU_KIEN)) {
                canTuan++;
                final long mo = LichNam.thuHaiTuanIso(nam, tuan), dong = mo + 7 * 86400000L;
                for (final LichNam.CuaSo c : ds) {
                    if (c.mo >= mo && c.dong <= dong) {
                        trungTuan++;
                        break;
                    }
                }
            }
        }
        Kiem.bang("2026..2035: moi lan la Chu nhat 04:00 -> thu Hai 04:00 cung nam", 0, sai);
        Kiem.bang("tong 120 lan / 10 nam", 120, tong);
        Kiem.bang("moi tuan cong dong ISO (chuoi nhiem vu) chua ngay cong dong", canTuan, trungTuan);
        // ngay cu the
        Kiem.bang("thang 10/2026: Chu nhat cua tuan 40 (4/10)", "2026-10-4", s(NgayCongDong.ngay(2026, 10)));
        Kiem.bang("thang 11/2026: Chu nhat thu ba (15/11)", "2026-11-15", s(NgayCongDong.ngay(2026, 11)));
        Kiem.bang("thang 2/2027: Chu nhat thu ba (21/2)", "2027-2-21", s(NgayCongDong.ngay(2027, 2)));
        final long[] w = NgayCongDong.cuaSo(2026, 11);
        Kiem.dung("15/11/2026 10:00 dang dien ra", LichNam.dangDienRa(NgayCongDong.MA_SU_KIEN, w[0] + 6 * 3600000L));
        Kiem.sai("16/11/2026 05:00 da dong", LichNam.dangDienRa(NgayCongDong.MA_SU_KIEN, w[1] + 3600000L));
        Kiem.bang("nam xoay 2026 = 0, 2027 = 1, 2028 = 0", "0,1,0",
                NgayCongDong.namXoay(2026) + "," + NgayCongDong.namXoay(2027) + "," + NgayCongDong.namXoay(2028));
        // mo ta rieng tung lan (tin web)
        final LichNam.MoTaLan cu = LichNam.moTaLan;
        LichNam.moTaLan = (mm, nam, lan) -> new String[]{"Dòng Gastly " + (lan + 1), "Danh hiệu Gastly"};
        final LichNam.CuaSo c10 = LichNam.cuaSo(m, 2026).get(9);
        Kiem.bang("mo ta rieng lan thang 10", "Dòng Gastly 10", c10.moTa());
        Kiem.bang("phan thuong rieng", "Danh hiệu Gastly", c10.phanThuong());
        LichNam.moTaLan = null;
        Kiem.bang("khong co nguon rieng: mo ta chung", m.moTa, c10.moTa());
        LichNam.moTaLan = cu;
        final String db = doc("src/data/DatabaseLichNam.java");
        Kiem.dung("ghi lich dung mo ta rieng tung lan", db.contains("c.moTa()") && db.contains("c.phanThuong()"));
        Kiem.dung("SuKien.nap nap dong noi bat truoc lich nam", doc("src/real/SuKien.java").indexOf("NgayCongDongGame.nap()")
                < doc("src/real/SuKien.java").indexOf("LichSuKien.dongBoLichNam()"));
    }

    static String s(final int[] d) {
        return d[0] + "-" + d[1] + "-" + d[2];
    }

    static NgayCongDong.Dong dong(final int tiLe, final int exp, final int soGio) {
        return new NgayCongDong.Dong(10, 0, "4", "Gastly", new int[]{25, 52}, new int[]{999}, 14, soGio, tiLe, exp, 30, 9804, "");
    }

    static void trongNgay() {
        Kiem.nhom("Ngay cong dong: gio vang, doi loai, EXP, shiny (co tran)");
        final NgayCongDong.Dong d = dong(40, 20, 3);
        Kiem.sai("13h chua gio vang", d.trongGioVang(13));
        Kiem.dung("14h gio vang", d.trongGioVang(14));
        Kiem.dung("16h gio vang", d.trongGioVang(16));
        Kiem.sai("17h het gio vang", d.trongGioVang(17));
        Kiem.bang("khung gio hien thi", "14:00–17:00", d.khungGio());
        final List<int[]> tuNhien = new ArrayList<int[]>(Arrays.asList(new int[]{110, 25}, new int[]{113, 52}, new int[]{113, 47}));
        Kiem.bang("ban do 110: Gastly", 25, NgayCongDong.loaiTrenMap(d, 110, tuNhien));
        Kiem.bang("ban do 113: Haunter (cung dong)", 52, NgayCongDong.loaiTrenMap(d, 113, tuNhien));
        Kiem.bang("ban do them 999: loai dau dong", 25, NgayCongDong.loaiTrenMap(d, 999, tuNhien));
        Kiem.bang("ban do khac: khong thuoc", -1, NgayCongDong.loaiTrenMap(d, 5, tuNhien));
        // ti le doi loai ~ ti_le
        final Random r = new Random(7);
        int doi = 0;
        for (int i = 0; i < 100000; i++) {
            doi += NgayCongDong.doiThanhNoiBat(d, 47, 52, r.nextInt(100)) ? 1 : 0;
        }
        Kiem.dung("doi loai ~40% (" + doi / 1000 + "%)", doi > 38500 && doi < 41500);
        Kiem.sai("loai goc da la loai noi bat: khong doi", NgayCongDong.doiThanhNoiBat(d, 52, 52, 0));
        // tran du CSDL ghi gi
        final NgayCongDong.Dong lo = dong(95, 300, 12);
        Kiem.bang("ti le kep <= 50%", NgayCongDong.TRAN_TI_LE_PHAN_TRAM, lo.tiLe);
        Kiem.bang("EXP kep <= 25%", NgayCongDong.TRAN_EXP_PHAN_TRAM, lo.exp);
        Kiem.bang("so gio kep <= 4", NgayCongDong.TRAN_SO_GIO, lo.soGio);
        Kiem.bang("EXP them khi ha loai noi bat trong gio vang", 20, NgayCongDong.expThem(d, true, 52));
        Kiem.bang("ngoai gio vang: 0", 0, NgayCongDong.expThem(d, false, 52));
        Kiem.bang("loai khac: 0", 0, NgayCongDong.expThem(d, true, 47));
        Kiem.dung("shiny chi trong gio vang", NgayCongDong.shinyTang(true, true) && !NgayCongDong.shinyTang(true, false)
                && !NgayCongDong.shinyTang(false, true));
        Kiem.bang("shiny x5 (SuKienLauDai.nhanShiny)", 5, SuKienLauDai.nhanShiny(false, true));
        // EXP cong vao buff chung: van chiu tran +100% cua CapDoLauDai
        final long goc = CapDoLauDai.expHaQuai(1000L, 100, 0, 100, 100, 100);
        final long max = CapDoLauDai.expHaQuai(1000L, 100, 100, 100, 100, 100);
        final long vuot = CapDoLauDai.expHaQuai(1000L, 100, 100 + NgayCongDong.TRAN_EXP_PHAN_TRAM, 100, 100, 100);
        Kiem.dung("buff EXP + ngay cong dong van <= tran +100% (" + goc + " / " + max + " / " + vuot + ")", vuot == max && max <= goc * 2);
        final String ex = doc("src/real/ExpLauDai.java");
        Kiem.dung("ExpLauDai cong EXP ngay cong dong vao buff (truoc tran)", ex.contains("buffPhanTram(c, ds) + com.htbb.pokemon.NgayCongDongGame.expThem(mt)"));
        Kiem.dung("shiny x5 chi gio vang (SuKienLauDaiGame)", doc("src/real/SuKienLauDaiGame.java").contains("NgayCongDongGame.gioVang()"));
        Kiem.dung("hoi sinh doi loai noi bat (HeSinhThaiGame)", doc("src/com/htbb/pokemon/HeSinhThaiGame.java").contains("NgayCongDongGame.loaiNoiBat(mapId, goc)"));
        Kiem.dung("mo ta co ten dong + gio vang", NgayCongDong.moTa(d).contains("Gastly") && NgayCongDong.moTa(d).contains("14:00"));
        final Pattern tien = Pattern.compile("(^|[\\s,(])(xu|lượng)([\\s,.)]|$)");
        Kiem.sai("mo ta / thuong khong hua xu / luong", tien.matcher(NgayCongDong.moTa(d)).find() || tien.matcher(NgayCongDong.phanThuong(d)).find());
        Kiem.bang("chon dong: thang khong co -> null", null, NgayCongDong.chon(new NgayCongDong.Dong[]{d}, 2026, 3));
        Kiem.bang("chon dong: nam le thieu nam xoay 1 -> dung dong nam xoay 0", d, NgayCongDong.chon(new NgayCongDong.Dong[]{d}, 2027, 10));
    }

    static void duLieu() {
        Kiem.nhom("data_ngay_cong_dong + danh hieu (sql/htbb.sql phan them_thoi_tiet_mua.sql)");
        final String s = DayDu.phanNangCap("them_thoi_tiet_mua.sql"); // tep cu gop vao sql/htbb.sql phan 11
        final Matcher m = Pattern.compile("^\\((\\d+),(\\d),(\\d+),'(\\w+)','([\\d,]+)','([\\d,]*)',(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),'([^']*)'\\)",
                Pattern.MULTILINE).matcher(s);
        final Map<String, String> theoKhoa = new HashMap<String, String>();
        final Set<Integer> idQuai = new HashSet<Integer>();
        final Set<Integer> dh = new HashSet<Integer>();
        int n = 0, vuot = 0;
        while (m.find()) {
            n++;
            theoKhoa.put(m.group(1) + "/" + m.group(2), m.group(4));
            for (final String q : m.group(5).split(",")) {
                idQuai.add(Integer.parseInt(q));
            }
            vuot += Integer.parseInt(m.group(8)) > NgayCongDong.TRAN_SO_GIO || Integer.parseInt(m.group(9)) > NgayCongDong.TRAN_TI_LE_PHAN_TRAM
                    || Integer.parseInt(m.group(10)) > NgayCongDong.TRAN_EXP_PHAN_TRAM ? 1 : 0;
            dh.add(Integer.parseInt(m.group(12)));
        }
        Kiem.bang("24 dong (12 thang x 2 nam xoay)", 24, n);
        Kiem.bang("du 24 khoa thang / nam xoay", 24, theoKhoa.size());
        Kiem.bang("khong dong nao vuot tran", 0, vuot);
        // id quai la quai thuong co ban do tu nhien (data_hst_bung_phat)
        final Set<Integer> coMap = new HashSet<Integer>();
        for (final Map<String, String> r : DayDu.dong("data_hst_bung_phat")) { // CSDL day du (sql/htbb.sql)
            coMap.add(Integer.parseInt(r.get("id_quai")));
        }
        final Set<Integer> thieu = new HashSet<Integer>(idQuai);
        thieu.removeAll(coMap);
        Kiem.bang("moi loai cua dong co ban do tu nhien (data_hst_bung_phat)", "[]", thieu.toString());
        // danh hieu: moi dong mot danh hieu 98xx, dieu kien cong_dong, tham so = so dong; ngan sach 1%
        final Matcher t = Pattern.compile("^\\((98\\d\\d),'(\\w+)','([^']*)','([^']*)','([^']*)',(\\d),'(\\w+)','(\\w+)',(\\d+),'([\\w:]+)','(\\w*)',(\\d+),(\\d),(\\d+),'(\\w+)',(\\d+),(\\d)\\)",
                Pattern.MULTILINE).matcher(s);
        int nt = 0, saiDk = 0, vuotNs = 0;
        boolean hoaCoDau = false;
        final Set<Integer> idDh = new HashSet<Integer>();
        while (t.find()) {
            nt++;
            idDh.add(Integer.parseInt(t.group(1)));
            saiDk += "cong_dong".equals(t.group(10)) || "tap:cong_dong".equals(t.group(10)) ? 0 : 1;
            vuotNs += Integer.parseInt(t.group(9)) > com.htbb.pokemon.DanhHieuLogic.TRAN_MAC_DINH ? 1 : 0;
            for (final String chu : new String[]{t.group(3), t.group(4)}) {
                for (int k = 0; k < chu.length(); k++) {
                    hoaCoDau |= chu.charAt(k) >= 'Ạ' && chu.charAt(k) <= 'Ỹ' && Character.isUpperCase(chu.charAt(k));
                }
            }
        }
        Kiem.bang("17 danh hieu (16 dong + tru cot)", 17, nt);
        Kiem.bang("dieu kien cong_dong / tap:cong_dong", 0, saiDk);
        Kiem.bang("chi so <= 10 phan nghin (ngan sach o 1%)", 0, vuotNs);
        Kiem.sai("khong chu HOA co dau (font J2ME)", hoaCoDau);
        final Set<Integer> dhThieu = new HashSet<Integer>(dh);
        dhThieu.removeAll(idDh);
        Kiem.bang("moi id_danh_hieu cua dong co dong danh hieu", "[]", dhThieu.toString());
        Kiem.sai("danh hieu 98xx khong trung them_danh_hieu.sql", doc("../sql/them_danh_hieu.sql").contains("(9801,"));
        Kiem.sai("ON DUPLICATE khong ghi de chi_so / phan_nghin / bat", s.contains("`chi_so`=VALUES") || s.contains("`phan_nghin`=VALUES")
                || s.contains("`bat`=VALUES"));
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
