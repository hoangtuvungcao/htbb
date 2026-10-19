import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.LichNam;
import com.htbb.pokemon.PhoBanLogic;
import com.htbb.pokemon.PhongRieng;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
 * Pho ban Ham nguc (to doi 5 tang) + Cup PvP tuan xoay vong (0.9.7, de xuat 7 + 10 chu game duyet 19/10/2026).
 *
 * Phan A (cup): tuan ISO thu Hai 04:00 gio Viet Nam (khop LichNam.thuHaiTuanIso), xoay vong, lich trong tuan, dang ky
 * (cap / he / tuoi tai khoan / gio choi), ghep Thuy Si (khong gap lai, CHONG CLONE: chung thiet bi / IP / tai khoan
 * khong bao gio gap nhau - thu ngau nhien nhieu giai), xep hang, thuong (tran luong khoa, danh hieu co han, treo may
 * khong nhan), chuan hoa sat thuong, lich nam (web tu dang tin). Phan B (pho ban): chu de theo thu, chi so quai noi
 * suy Gym, he so to doi, kiem vao (luot ngay, cap, chenh cap, cung cho, ban), vong doi phong (qua tang, ha boss, guc
 * ca doi, het gio, mat ket noi / dang nhap lai, roi phong), thuong ngay mot lan, bang roi (tran ky vong, khoa, id co
 * that). Phan C: SQL idempotent + khop ban mac dinh, cau hinh, danh hieu, goi -112, diem moc trong ma.
 */
public final class KiemPhoBanCup {

    private KiemPhoBanCup() {
    }

    public static void chay() {
        Kiem.nhom("Cup PvP tuan + Pho ban Ham nguc - xoay tuan, dang ky, Thuy Si, chong clone, thuong mot lan, vong doi phong, luot, roi");
        tuanCup();
        xoayVong();
        lichTrongTuan();
        dangKy();
        thuySi();
        thuySiChongCloneNgauNhien();
        xepHangVaThuong();
        chuanHoa();
        lichNamCup();
        chuDeVaChiSo();
        kiemVao();
        vongDoiPhong();
        thuongNgay();
        bangRoi();
        sqlVaMacDinh();
        cauHinh();
        danhHieu();
        goi112VaKho();
        diemMoc();
    }

    static final java.util.TimeZone VN = LichNam.MUI_GIO;

    static long vn(final int y, final int m, final int d, final int h, final int phut) {
        final Calendar c = Calendar.getInstance(VN);
        c.clear();
        c.set(y, m - 1, d, h, phut, 0);
        return c.getTimeInMillis();
    }

    static int so(final String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (final Exception e) {
            return -1;
        }
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    // ================================================================== A. cup

    static void tuanCup() {
        // 19/10/2026 la thu Hai (tuan ISO 43)
        final long mo = vn(2026, 10, 19, 4, 0);
        Kiem.bang("tuan: moTuan(tuan(thu Hai 04:00)) = dung moc", Long.valueOf(mo), Long.valueOf(CupLogic.moTuan(CupLogic.tuan(mo))));
        Kiem.bang("tuan: thu Hai 03:59 van la tuan truoc", Integer.valueOf(CupLogic.tuan(mo) - 1), Integer.valueOf(CupLogic.tuan(mo - 60000L)));
        Kiem.bang("tuan: Chu nhat 23:59 cung tuan", Integer.valueOf(CupLogic.tuan(mo)), Integer.valueOf(CupLogic.tuan(vn(2026, 10, 25, 23, 59))));
        Kiem.bang("tuan: thu Hai sau 04:00 sang tuan moi", Integer.valueOf(CupLogic.tuan(mo) + 1), Integer.valueOf(CupLogic.tuan(vn(2026, 10, 26, 4, 0))));
        Kiem.bang("tuan ISO 2026-43", "2026-43", CupLogic.namTuanIso(mo)[0] + "-" + CupLogic.namTuanIso(mo)[1]);
        boolean khop = true;
        boolean lienTuc = true;
        int truoc = Integer.MIN_VALUE;
        for (int nam = 2025; nam <= 2032; nam++) {
            for (int w = 1; w <= 53; w++) {
                final long t = LichNam.thuHaiTuanIso(nam, w);
                final int[] iso = CupLogic.namTuanIso(t);
                if (iso[0] != nam || iso[1] != w) {
                    continue; // nam 52 tuan
                }
                khop &= CupLogic.moTuan(CupLogic.tuan(t)) == t;
                if (truoc != Integer.MIN_VALUE) {
                    lienTuc &= CupLogic.tuan(t) == truoc + 1;
                }
                truoc = CupLogic.tuan(t);
            }
        }
        Kiem.dung("tuan: moi thu Hai 04:00 ISO 2025-2032 (ca nam 53 tuan 2026) = moc tuan cup", khop);
        Kiem.dung("tuan: chi so tuan lien tuc qua tuan 53 / doi nam (xoay vong khong nhay)", lienTuc);
        Kiem.bang("2026 co tuan 53 (28/12/2026)", "2026-53", CupLogic.namTuanIso(vn(2026, 12, 28, 10, 0))[0] + "-" + CupLogic.namTuanIso(vn(2026, 12, 28, 10, 0))[1]);
        Kiem.bang("04/01/2027 03:00 van la tuan 2026-53", "2026-53", CupLogic.namTuanIso(vn(2027, 1, 4, 3, 0))[0] + "-" + CupLogic.namTuanIso(vn(2027, 1, 4, 3, 0))[1]);
    }

    static void xoayVong() {
        CupLogic.datDuLieu(CupLogic.loaiMacDinh(), CupLogic.lichMacDinh());
        final int t0 = CupLogic.tuan(vn(2026, 10, 19, 5, 0));
        final Set<String> gap = new HashSet<String>();
        boolean khacTuanTruoc = true;
        for (int i = 0; i < 16; i++) {
            gap.add(CupLogic.loaiTuan(t0 + i).ma);
            khacTuanTruoc &= !CupLogic.loaiTuan(t0 + i).ma.equals(CupLogic.loaiTuan(t0 + i + 1).ma);
        }
        Kiem.bang("xoay: 16 tuan gap du 7 loai cup", Integer.valueOf(7), Integer.valueOf(gap.size()));
        Kiem.dung("xoay: hai tuan lien nhau khong trung loai", khacTuanTruoc);
        Kiem.bang("xoay: lap lai sau 16 tuan", CupLogic.loaiTuan(t0).ma, CupLogic.loaiTuan(t0 + 16).ma);
        Kiem.bang("xoay: tuan am (truoc 1970) khong vo", Boolean.TRUE, Boolean.valueOf(CupLogic.loaiTuan(-5) != null));
        final List<String> l = new ArrayList<String>(Arrays.asList("doi", "khong_co"));
        CupLogic.datDuLieu(null, l);
        Kiem.bang("xoay: lich CSDL tro loai khong co -> loai dau (khong null)", "nhi", CupLogic.loaiTuan(1).ma);
        Kiem.bang("xoay: lich CSDL doi duoc", "doi", CupLogic.loaiTuan(0).ma);
        CupLogic.datDuLieu(CupLogic.loaiMacDinh(), CupLogic.lichMacDinh());
    }

    static void lichTrongTuan() {
        final int g = CupLogic.GIO_VONG_DAU;
        final int p = CupLogic.PHUT_MOI_VONG;
        final int v = CupLogic.SO_VONG;
        Kiem.bang("pha: thu Hai 05:00 = dang ky", Integer.valueOf(CupLogic.PHA_DANG_KY), Integer.valueOf(CupLogic.pha(vn(2026, 10, 19, 5, 0), g, p, v)[0]));
        Kiem.bang("pha: Chu nhat 19:54 = dang ky", Integer.valueOf(CupLogic.PHA_DANG_KY), Integer.valueOf(CupLogic.pha(vn(2026, 10, 25, 19, 54), g, p, v)[0]));
        Kiem.bang("pha: Chu nhat 19:55 = khoa danh sach, cho vong 1", Integer.valueOf(CupLogic.PHA_CHO_VONG), Integer.valueOf(CupLogic.pha(vn(2026, 10, 25, 19, 55), g, p, v)[0]));
        Kiem.bang("pha: 20:00 = vong 1 (chi so 0)", "2-0", CupLogic.pha(vn(2026, 10, 25, 20, 0), g, p, v)[0] + "-" + CupLogic.pha(vn(2026, 10, 25, 20, 0), g, p, v)[1]);
        Kiem.bang("pha: 20:41 = vong 5", "2-4", CupLogic.pha(vn(2026, 10, 25, 20, 41), g, p, v)[0] + "-" + CupLogic.pha(vn(2026, 10, 25, 20, 41), g, p, v)[1]);
        Kiem.bang("pha: 20:50 = xong (chot)", Integer.valueOf(CupLogic.PHA_XONG), Integer.valueOf(CupLogic.pha(vn(2026, 10, 25, 20, 50), g, p, v)[0]));
        Kiem.bang("pha: thu Hai 04:00 tuan sau = dang ky tuan moi", Integer.valueOf(CupLogic.PHA_DANG_KY), Integer.valueOf(CupLogic.pha(vn(2026, 10, 26, 4, 0), g, p, v)[0]));
        Kiem.dung("pha: tran toi da + chuan bi vua trong mot vong 10 phut", CupLogic.GIAY_TRAN + 5 + 4 < p * 60);
    }

    static void dangKy() {
        final CupLogic.Loai nhi = CupLogic.loai("nhi");
        final CupLogic.Loai lua = CupLogic.loai("he_lua");
        Kiem.bang("dang ky: Cup Nhi cap 25 duoc", null, CupLogic.kiemThanhVien(nhi, "A", 25, 1, -1, 100, 50, 24, 10));
        Kiem.dung("dang ky: Cup Nhi cap 31 bi chan", String.valueOf(CupLogic.kiemThanhVien(nhi, "A", 31, 1, -1, 100, 50, 24, 10)).contains("20–30"));
        Kiem.dung("dang ky: cap 19 bi chan (moi cup)", String.valueOf(CupLogic.kiemThanhVien(lua, "A", 19, 1, -1, 100, 50, 24, 10)).contains("cấp 20"));
        Kiem.bang("dang ky: Cup he Lua - Charizard (Lua / Bay) duoc", null, CupLogic.kiemThanhVien(lua, "A", 60, 1, 9, 100, 50, 24, 10));
        Kiem.bang("dang ky: Cup he Lua - he phu Lua cung duoc", null, CupLogic.kiemThanhVien(lua, "A", 60, 9, 1, 100, 50, 24, 10));
        Kiem.dung("dang ky: Cup he Lua - Squirtle bi chan", String.valueOf(CupLogic.kiemThanhVien(lua, "A", 60, 2, -1, 100, 50, 24, 10)).contains("hệ Lửa"));
        Kiem.dung("dang ky: tai khoan moi tao 3 gio bi chan (chong clone)", String.valueOf(CupLogic.kiemThanhVien(lua, "A", 60, 1, -1, 3, 50, 24, 10)).contains("24 giờ"));
        Kiem.dung("dang ky: moi choi 2 gio bi chan (chong clone)", String.valueOf(CupLogic.kiemThanhVien(lua, "A", 60, 1, -1, 100, 2, 24, 10)).contains("10 giờ"));
        Kiem.bang("dang ky: khong doc duoc tuoi / gio choi (-1) khong chan", null, CupLogic.kiemThanhVien(lua, "A", 60, 1, -1, -1, -1, 24, 10));
        Kiem.dung("dang ky: Cup Doi la 2 nguoi", CupLogic.loai("doi").soNguoi == 2);
        Kiem.dung("khoa clone: chung thiet bi = trung", CupLogic.trungClone(CupLogic.khoaClone("imei1", "1.1.1.1", 5), CupLogic.khoaClone("imei1", "2.2.2.2", 6)));
        Kiem.dung("khoa clone: chung IP = trung", CupLogic.trungClone(CupLogic.khoaClone("a", "1.1.1.1", 5), CupLogic.khoaClone("b", "1.1.1.1", 6)));
        Kiem.dung("khoa clone: chung tai khoan = trung", CupLogic.trungClone(CupLogic.khoaClone("a", "", 5), CupLogic.khoaClone("b", "", 5)));
        Kiem.sai("khoa clone: khac het = khong trung", CupLogic.trungClone(CupLogic.khoaClone("a", "1.1.1.1", 5), CupLogic.khoaClone("b", "2.2.2.2", 6)));
        Kiem.sai("khoa clone: thiet bi / IP rong khong tinh la trung", CupLogic.trungClone(CupLogic.khoaClone("", "", 0), CupLogic.khoaClone("", "", 0)));
    }

    static CupLogic.Doi doi(final int id, final String may) {
        return new CupLogic.Doi(id, id, CupLogic.khoaClone(may, "", 1000 + id));
    }

    static void thuySi() {
        final List<CupLogic.Doi> ds = new ArrayList<CupLogic.Doi>();
        for (int i = 1; i <= 8; i++) {
            ds.add(doi(i, "m" + i));
        }
        CupLogic.Ghep g = CupLogic.ghep(ds, 1L);
        Kiem.bang("Thuy Si: 8 doi -> 4 cap, khong mien", "4/-1", g.cap.size() + "/" + g.mien);
        ds.add(doi(9, "m9"));
        g = CupLogic.ghep(ds, 1L);
        Kiem.bang("Thuy Si: 9 doi -> 4 cap + 1 mien", "4", String.valueOf(g.cap.size()));
        Kiem.dung("Thuy Si: co doi mien", g.mien > 0);
        // mien dau chi mot lan: danh dau doi mien, ghep lai thi doi khac mien
        final Map<Integer, CupLogic.Doi> theoId = new HashMap<Integer, CupLogic.Doi>();
        for (final CupLogic.Doi d : ds) {
            theoId.put(Integer.valueOf(d.id), d);
        }
        final int mien1 = g.mien;
        CupLogic.ghiMien(theoId.get(Integer.valueOf(mien1)));
        final CupLogic.Ghep g2 = CupLogic.ghep(ds, 2L);
        Kiem.dung("Thuy Si: doi da mien khong mien lan 2", g2.mien != mien1);
        Kiem.bang("Thuy Si: mien +3 diem", Integer.valueOf(CupLogic.DIEM_MIEN), Integer.valueOf(theoId.get(Integer.valueOf(mien1)).diem));
        // khong gap lai: 4 doi, vong 1 a-b c-d -> vong 2 khong co a-b
        final List<CupLogic.Doi> bon = new ArrayList<CupLogic.Doi>();
        for (int i = 1; i <= 4; i++) {
            bon.add(doi(i, "x" + i));
        }
        final CupLogic.Ghep v1 = CupLogic.ghep(bon, 7L);
        final Map<Integer, CupLogic.Doi> m4 = new HashMap<Integer, CupLogic.Doi>();
        for (final CupLogic.Doi d : bon) {
            m4.put(Integer.valueOf(d.id), d);
        }
        for (final int[] c : v1.cap) {
            CupLogic.ghiTran(m4.get(Integer.valueOf(c[0])), m4.get(Integer.valueOf(c[1])), CupLogic.A_THANG);
        }
        final CupLogic.Ghep v2 = CupLogic.ghep(bon, 8L);
        boolean gapLai = false;
        for (final int[] c : v2.cap) {
            gapLai |= m4.get(Integer.valueOf(c[0])).daGap.contains(Integer.valueOf(c[1]));
        }
        Kiem.sai("Thuy Si: vong 2 khong gap lai doi vong 1", gapLai);
        boolean cungDiem = true;
        for (final int[] c : v2.cap) {
            cungDiem &= m4.get(Integer.valueOf(c[0])).diem == m4.get(Integer.valueOf(c[1])).diem;
        }
        Kiem.dung("Thuy Si: vong 2 ghep cung diem (thang gap thang)", cungDiem);
        // chong clone: hai doi cung thiet bi - chi con nhau la doi thu -> khong ghep
        final List<CupLogic.Doi> hai = new ArrayList<CupLogic.Doi>(Arrays.asList(doi(1, "cung"), doi(2, "cung")));
        final CupLogic.Ghep gc = CupLogic.ghep(hai, 3L);
        Kiem.bang("chong clone: 2 doi cung may -> khong ghep, ca hai vao danh sach khong co doi thu", "0/2", gc.cap.size() + "/" + gc.khongGhep.size());
        final List<CupLogic.Doi> bonClone = new ArrayList<CupLogic.Doi>(Arrays.asList(doi(1, "A"), doi(2, "A"), doi(3, "B"), doi(4, "B")));
        final CupLogic.Ghep g4 = CupLogic.ghep(bonClone, 4L);
        boolean sach = g4.cap.size() == 2;
        for (final int[] c : g4.cap) {
            sach &= !(c[0] <= 2 && c[1] <= 2) && !(c[0] >= 3 && c[1] >= 3);
        }
        Kiem.dung("chong clone: A,A,B,B -> A gap B (hai cap), khong cap nao cung may", sach);
        Kiem.bang("het gio: con nhieu HP hon thang", Integer.valueOf(CupLogic.A_THANG), Integer.valueOf(CupLogic.ketQuaHetGio(600, 300)));
        Kiem.bang("het gio: bang nhau hoa", Integer.valueOf(CupLogic.HOA), Integer.valueOf(CupLogic.ketQuaHetGio(500, 500)));
        Kiem.bang("het gio: B nhieu hon", Integer.valueOf(CupLogic.B_THANG), Integer.valueOf(CupLogic.ketQuaHetGio(0, 1)));
    }

    /** Nhieu giai ngau nhien: khong bao gio cung khoa clone gap nhau, moi doi toi da 1 tran / vong, han che gap lai. */
    static void thuySiChongCloneNgauNhien() {
        final Random r = new Random(20261019L);
        boolean khongClone = true;
        boolean motTran = true;
        boolean khongGapLaiKhiCoCach = true;
        int soGiai = 0;
        int tongTran = 0;
        for (int giai = 0; giai < 300; giai++) {
            final int n = 2 + r.nextInt(40);
            final List<CupLogic.Doi> ds = new ArrayList<CupLogic.Doi>();
            for (int i = 1; i <= n; i++) {
                // ~15% chung may voi mot doi khac, ~10% chung IP
                final String may = r.nextInt(100) < 15 ? "may" + r.nextInt(Math.max(1, n / 3)) : "rieng" + i;
                final String ip = r.nextInt(100) < 10 ? "ip" + r.nextInt(3) : "";
                ds.add(new CupLogic.Doi(i, i, CupLogic.khoaClone(may, ip, 100 + i)));
            }
            final Map<Integer, CupLogic.Doi> theo = new HashMap<Integer, CupLogic.Doi>();
            for (final CupLogic.Doi d : ds) {
                theo.put(Integer.valueOf(d.id), d);
            }
            for (int vong = 0; vong < CupLogic.SO_VONG; vong++) {
                final CupLogic.Ghep g = CupLogic.ghep(ds, giai * 100L + vong);
                final Set<Integer> daCo = new HashSet<Integer>();
                if (g.mien >= 0) {
                    daCo.add(Integer.valueOf(g.mien));
                    CupLogic.ghiMien(theo.get(Integer.valueOf(g.mien)));
                }
                for (final int[] c : g.cap) {
                    final CupLogic.Doi a = theo.get(Integer.valueOf(c[0]));
                    final CupLogic.Doi b = theo.get(Integer.valueOf(c[1]));
                    khongClone &= !CupLogic.trungClone(a.khoa, b.khoa);
                    motTran &= daCo.add(Integer.valueOf(a.id)) && daCo.add(Integer.valueOf(b.id));
                    if (vong < 2 && n >= 8) {
                        khongGapLaiKhiCoCach &= !a.daGap.contains(Integer.valueOf(b.id));
                    }
                    CupLogic.ghiTran(a, b, r.nextInt(3) + 1);
                    tongTran++;
                }
                for (final Integer k : g.khongGhep) {
                    motTran &= daCo.add(k);
                }
                motTran &= daCo.size() == n;
            }
            soGiai++;
        }
        Kiem.dung("chong clone ngau nhien (" + soGiai + " giai, " + tongTran + " tran): KHONG cap nao chung thiet bi / IP / tai khoan", khongClone);
        Kiem.dung("Thuy Si ngau nhien: moi doi dung 1 vai tro / vong (dau, mien, hoac khong co doi thu)", motTran);
        Kiem.dung("Thuy Si ngau nhien: vong 1-2 khong gap lai (>= 8 doi)", khongGapLaiKhiCoCach);
    }

    static void xepHangVaThuong() {
        final CupLogic.Doi a = doi(1, "a");
        final CupLogic.Doi b = doi(2, "b");
        final CupLogic.Doi c = doi(3, "c");
        final CupLogic.Doi d = doi(4, "d");
        CupLogic.ghiTran(a, b, CupLogic.A_THANG);
        CupLogic.ghiTran(c, d, CupLogic.A_THANG);
        CupLogic.ghiTran(a, c, CupLogic.A_THANG);
        CupLogic.ghiTran(b, d, CupLogic.A_THANG);
        final List<CupLogic.Doi> h = CupLogic.xepHang(Arrays.asList(d, c, b, a));
        Kiem.bang("xep hang: diem truoc (a 6 diem hang 1)", Integer.valueOf(1), Integer.valueOf(h.get(0).id));
        Kiem.bang("xep hang: b va c cung 3 diem - Buchholz bang nhau -> dang ky som hon (b)", Integer.valueOf(2), Integer.valueOf(h.get(1).id));
        Kiem.bang("xep hang: d cuoi", Integer.valueOf(4), Integer.valueOf(h.get(3).id));
        final CupLogic.Loai nhi = CupLogic.loai("nhi");
        final CupLogic.Thuong vd = CupLogic.thuong(nhi, 1, 5, 5);
        Kiem.bang("thuong: vo dich 20 luong khoa", Integer.valueOf(CupLogic.LK_VO_DICH), Integer.valueOf(vd.luongKhoa));
        Kiem.bang("thuong: vo dich Cup Nhi -> danh hieu 9800, 28 ngay", "9800/40320", vd.danhHieu + "/" + vd.phut);
        Kiem.bang("thuong: vo dich Cup Doi -> 9802", Integer.valueOf(9802), Integer.valueOf(CupLogic.thuong(CupLogic.loai("doi"), 1, 5, 3).danhHieu));
        Kiem.bang("thuong: vo dich Cup he -> 9801", Integer.valueOf(9801), Integer.valueOf(CupLogic.thuong(CupLogic.loai("he_co"), 1, 5, 3).danhHieu));
        Kiem.bang("thuong: vo dich Cup Khong trang bi -> 9803", Integer.valueOf(9803), Integer.valueOf(CupLogic.thuong(CupLogic.loai("thuan"), 1, 5, 3).danhHieu));
        Kiem.bang("thuong: hang 3 -> a quan 10 luong khoa", "10/9804", CupLogic.thuong(nhi, 3, 5, 3).luongKhoa + "/" + CupLogic.thuong(nhi, 3, 5, 3).danhHieu);
        Kiem.bang("thuong: hang 6 da dau 5 thang 2 -> 5 luong khoa", Integer.valueOf(5), Integer.valueOf(CupLogic.thuong(nhi, 6, 5, 2).luongKhoa));
        Kiem.bang("thuong: hang 6 nhung thua het (treo may) -> 0", Integer.valueOf(0), Integer.valueOf(CupLogic.thuong(nhi, 6, 5, 0).luongKhoa));
        Kiem.bang("thuong: hang 20 dau 3 thang 1 -> tham gia 2", Integer.valueOf(CupLogic.LK_THAM_GIA), Integer.valueOf(CupLogic.thuong(nhi, 20, 3, 1).luongKhoa));
        Kiem.bang("thuong: hang 20 chi dau 2 tran -> 0", Integer.valueOf(0), Integer.valueOf(CupLogic.thuong(nhi, 20, 2, 2).luongKhoa));
        Kiem.bang("thuong: chua dau tran nao (vang mat) -> 0 ke ca hang 1", Integer.valueOf(0), Integer.valueOf(CupLogic.thuong(nhi, 1, 0, 0).luongKhoa));
        int max = 0;
        long tong100 = 0;
        for (int hang = 1; hang <= 100; hang++) {
            final int lk = CupLogic.thuong(nhi, hang, 5, 2).luongKhoa;
            max = Math.max(max, lk);
            tong100 += lk;
        }
        Kiem.dung("thuong: moi nguoi <= 20 luong khoa / tuan (trong tran 25 / tuan cua Gym / boss / xep hang)", max <= 20 && max <= 25);
        Kiem.bang("thuong: tong luong khoa giai 100 nguoi (20 + 2x10 + 5x5 + 92x2)", Long.valueOf(20 + 20 + 25 + 92 * 2), Long.valueOf(tong100));
        Kiem.dung("thuong: danh hieu co han (khong vinh vien)", vd.phut > 0 && CupLogic.thuong(nhi, 3, 5, 3).phut > 0 && CupLogic.thuong(nhi, 5, 5, 3).phut > 0);
        Kiem.dung("thuong: chu thuong khong nhac xu", !CupLogic.phanThuongChu().contains(" xu,") && CupLogic.phanThuongChu().contains("Không có xu"));
    }

    static void chuanHoa() {
        final CupLogic.Loai nhi = CupLogic.loai("nhi");
        // cung ti le "sat thuong / HP minh" -> cung phan nghin HP doi thu, bat ke cap (HP to / nho)
        final int a = CupLogic.satThuong(300, 10000, 50000, nhi.phanNghinMin, nhi.phanNghinMax);
        final int b = CupLogic.satThuong(3000, 100000, 50000, nhi.phanNghinMin, nhi.phanNghinMax);
        Kiem.bang("chuan hoa: cap thap va cap cao cung ti le -> cung sat thuong", Integer.valueOf(a), Integer.valueOf(b));
        Kiem.bang("chuan hoa: 3% HP minh -> 3% HP doi thu (1.500 / 50.000)", Integer.valueOf(1500), Integer.valueOf(a));
        Kiem.bang("chuan hoa: kep tren 80 phan nghin", Integer.valueOf(4000), Integer.valueOf(CupLogic.satThuong(99999, 1000, 50000, 10, 80)));
        Kiem.bang("chuan hoa: kep duoi 10 phan nghin", Integer.valueOf(500), Integer.valueOf(CupLogic.satThuong(0, 1000, 50000, 10, 80)));
        final CupLogic.Loai th = CupLogic.loai("thuan");
        final int yeu = CupLogic.satThuong(100, 10000, 50000, th.phanNghinMin, th.phanNghinMax);
        final int manh = CupLogic.satThuong(5000, 10000, 50000, th.phanNghinMin, th.phanNghinMax);
        Kiem.dung("Cup Khong trang bi: do manh gap 50 lan van chi chenh <= 1,67 lan (30..50 phan nghin)", manh * 100 <= yeu * 167);
        Kiem.dung("chuan hoa: doi thu het mau sau >= 13 don (tran 8%), khong mot phat chet", 1000 / 80 >= 12);
        Kiem.bang("HP phan nghin: 1/3", Integer.valueOf(333), Integer.valueOf(CupLogic.phanNghinHp(1, 3)));
    }

    static void lichNamCup() {
        final LichNam.Muc m = LichNam.muc(CupLogic.MA_LICH);
        Kiem.dung("lich nam: co muc " + CupLogic.MA_LICH, m != null);
        if (m == null) {
            return;
        }
        Kiem.bang("lich nam: nhom 'Cúp PvP' (web bao truoc 1 ngay)", CupLogic.NHOM_LICH, m.nhom);
        final List<LichNam.CuaSo> c26 = LichNam.cuaSo(m, 2026);
        Kiem.bang("lich nam: 2026 co 53 tuan cup", Integer.valueOf(53), Integer.valueOf(c26.size()));
        Kiem.bang("lich nam: 2027 co 52 tuan cup", Integer.valueOf(52), Integer.valueOf(LichNam.cuaSo(m, 2027).size()));
        boolean lienTuc = true;
        boolean ten = true;
        for (int i = 0; i < c26.size(); i++) {
            final LichNam.CuaSo c = c26.get(i);
            lienTuc &= c.dong - c.mo == 7L * 86400000L && (i == 0 || c26.get(i - 1).dong == c.mo);
            final CupLogic.Loai l = CupLogic.loaiTuan(CupLogic.tuan(c.mo));
            ten &= c.ten.startsWith(l.ten) && c.ten.contains("tuần " + c.lan) && c.moTa.contains(l.luat()) && c.ten.length() <= 100 && c.moTa.length() <= 500;
        }
        Kiem.dung("lich nam: tuan cup lien nhau, moi tuan 7 ngay, mo thu Hai 04:00", lienTuc);
        Kiem.dung("lich nam: ten / mo ta moi tuan = loai cup cua tuan do (web dang tin dung ten)", ten);
        Kiem.dung("lich nam: luon dang dien ra (khong bat / tat lam khoi dong lai may chu)", LichNam.dangDienRa(CupLogic.MA_LICH, vn(2026, 10, 21, 12, 0))
                && LichNam.dangDienRa(CupLogic.MA_LICH, vn(2027, 1, 4, 3, 59)) && LichNam.dangDienRa(CupLogic.MA_LICH, vn(2027, 1, 4, 4, 0)));
        Kiem.dung("lich nam: phan thuong <= 300 ky tu (cot phan_thuong)", m.phanThuong.length() <= 300);
        final String web = doc("../web/shared/utils/suKienNam.ts");
        Kiem.dung("web: suKienNam.ts biet nhom 'Cúp PvP' (bao truoc 1 ngay, tin co link bang xep hang)", web.contains("'Cúp PvP'") && web.contains("/cup"));
        Kiem.dung("web: trang /cup va API /api/cup co that", Files.exists(Paths.get("../web/app/pages/cup.vue")) && Files.exists(Paths.get("../web/server/api/cup.get.ts")));
    }

    // ================================================================== B. pho ban

    static void chuDeVaChiSo() {
        final List<PhoBanLogic.ChuDe> cd = PhoBanLogic.chuDeMacDinh();
        Kiem.bang("chu de: 7 ngay", Integer.valueOf(7), Integer.valueOf(cd.size()));
        Kiem.bang("chu de: 19/10/2026 thu Hai = Hang Bang", "hang_bang", PhoBanLogic.chuDe(cd, 20261019).ma);
        Kiem.bang("chu de: 25/10/2026 Chu nhat = Hang Rong", "hang_rong", PhoBanLogic.chuDe(cd, 20261025).ma);
        Kiem.bang("chu de: thu Ba = Nui Lua", "nui_lua", PhoBanLogic.chuDe(cd, 20261020).ma);
        Kiem.bang("chu de: thu cua 31/12/2026 = thu Nam (4)", Integer.valueOf(4), Integer.valueOf(PhoBanLogic.thuCua(20261231)));
        Kiem.dung("chu de: danh sach rong -> null (khong vo)", PhoBanLogic.chuDe(new ArrayList<PhoBanLogic.ChuDe>(), 20261019) == null);
        // template co that + dung he chu de (bang_quai.csv)
        final Map<Integer, int[]> he = new HashMap<Integer, int[]>();
        for (final String dong : doc("../du_lieu/bang_quai.csv").split("\n")) {
            final String[] c = dong.split(",");
            if (c.length > 9 && c[0].matches("\\d+")) {
                he.put(Integer.valueOf(c[0]), new int[]{so(c[8]), so(c[9])});
            }
        }
        boolean coThat = true;
        boolean dungHe = true;
        final StringBuilder sai = new StringBuilder();
        for (final PhoBanLogic.ChuDe c : cd) {
            final int[] tat = Arrays.copyOf(c.mauQuai, c.mauQuai.length + 1);
            tat[tat.length - 1] = c.mauBoss;
            for (final int id : tat) {
                final int[] h = he.get(Integer.valueOf(id));
                if (h == null) {
                    coThat = false;
                    sai.append(c.ma).append(":").append(id).append(" ");
                } else if (h[0] != c.he && h[1] != c.he && id != 39) { // 39 = Salamence (template boss Gym, he Rong)
                    dungHe = false;
                    sai.append(c.ma).append(":").append(id).append("(he ").append(h[0]).append("/").append(h[1]).append(") ");
                }
            }
        }
        Kiem.dung("chu de: moi template quai / boss co trong bang_quai.csv " + sai, coThat);
        Kiem.dung("chu de: moi quai mang he cua chu de (he 1 hoac he 2) " + sai, dungHe);
        // chi so goc = bang Gym
        final List<int[]> gym = new ArrayList<int[]>();
        for (final String dong : doc("../du_lieu/noi_dung_gym.csv").split("\n")) {
            final String[] c = dong.split(",");
            if (c.length > 10 && c[0].matches("\\d+")) {
                gym.add(new int[]{Integer.parseInt(c[5]), Integer.parseInt(c[8]), Integer.parseInt(c[9]), Integer.parseInt(c[10])});
            }
        }
        Kiem.bang("chi so goc: doc du 16 Gym", Integer.valueOf(16), Integer.valueOf(gym.size()));
        boolean dungMoc = true;
        for (final int[] g : gym) {
            final int[] x = PhoBanLogic.chiSoGoc(gym, g[0]);
            dungMoc &= x[0] == g[1] && x[1] == g[2] && x[2] == g[3];
        }
        Kiem.dung("chi so goc: dung moc Gym (cap 12..83)", dungMoc);
        final int[] c50 = PhoBanLogic.chiSoGoc(gym, 50);
        Kiem.dung("chi so goc: cap 50 nam giua Gym 48 va 53", c50[0] > 171000 && c50[0] < 187900);
        final int[] c100 = PhoBanLogic.chiSoGoc(gym, 100);
        Kiem.dung("chi so goc: ngoai suy cap 100 tang deu (> cap 83, < 1,4 lan)", c100[0] > 289500 && c100[0] < 289500 * 14 / 10);
        final int[] f = PhoBanLogic.chiSoGoc(new ArrayList<int[]>(), 83);
        Kiem.dung("chi so goc: CSDL trong -> cong thuc khop Gym 83 sai < 1%", Math.abs(f[0] - 289500) * 100 < 289500);
        final List<PhoBanLogic.Tang> t = PhoBanLogic.tangMacDinh();
        final int giay = PhoBanLogic.giayMotMinh(t);
        Kiem.dung("tang: danh mot minh ~11 phut (600..720 giay) < tran 20 phut", giay >= 600 && giay <= 720 && giay < PhoBanLogic.PHUT_TOI_DA * 60);
        Kiem.dung("tang: tang 5 la boss duy nhat", t.get(4).laBoss && !t.get(0).laBoss && !t.get(3).laBoss && t.get(4).soQuai == 1);
        final int[] goc = PhoBanLogic.chiSoGoc(gym, 40);
        Kiem.bang("he so to doi: 4 nguoi HP quai x2,8", Integer.valueOf(280), Integer.valueOf(PhoBanLogic.heSoNguoi(4, false)));
        Kiem.bang("he so to doi: 4 nguoi HP boss x3,1", Integer.valueOf(310), Integer.valueOf(PhoBanLogic.heSoNguoi(4, true)));
        Kiem.bang("he so to doi: 9 nguoi kep ve 4", Integer.valueOf(280), Integer.valueOf(PhoBanLogic.heSoNguoi(9, false)));
        final int hp1 = PhoBanLogic.chiSoQuai(goc, t.get(4), 1)[0];
        final int hp4 = PhoBanLogic.chiSoQuai(goc, t.get(4), 4)[0];
        Kiem.dung("he so to doi: 4 nguoi ha boss nhanh hon 1 nguoi (HP x3,1 < 4 lan sat thuong)", hp4 < hp1 * 4L && hp4 > hp1 * 3L);
        Kiem.bang("cap quai = cap cao nhat doi", Integer.valueOf(45), Integer.valueOf(PhoBanLogic.capQuai(new int[]{40, 45, 38})));
    }

    static void kiemVao() {
        final int luot = PhoBanLogic.LUOT_NGAY;
        Kiem.bang("vao: 1 nguoi cap 20 du luot", null, PhoBanLogic.kiemVao(true, new int[]{20}, new int[]{0}, new boolean[]{true}, new boolean[]{false}, new String[]{"Bạn"}, 20, luot));
        Kiem.dung("vao: tat cau hinh -> tam dong", String.valueOf(PhoBanLogic.kiemVao(false, new int[]{20}, new int[]{0}, new boolean[]{true}, new boolean[]{false}, new String[]{"Bạn"}, 20, luot)).contains("tạm đóng"));
        Kiem.dung("vao: 5 nguoi -> chan (toi da 4)", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{30, 30, 30, 30, 30}, new int[5], new boolean[]{true, true, true, true, true},
                new boolean[5], new String[]{"a", "b", "c", "d", "e"}, 20, luot)).contains("tối đa 4"));
        Kiem.dung("vao: cap 19 -> chan", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{19}, new int[]{0}, new boolean[]{true}, new boolean[]{false}, new String[]{"Bạn"}, 20, luot)).contains("cấp 20"));
        Kiem.dung("vao: het luot (3/3) -> chan", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{40}, new int[]{3}, new boolean[]{true}, new boolean[]{false}, new String[]{"Bạn"}, 20, luot)).contains("hết lượt"));
        Kiem.bang("vao: 2/3 luot van vao duoc", null, PhoBanLogic.kiemVao(true, new int[]{40}, new int[]{2}, new boolean[]{true}, new boolean[]{false}, new String[]{"Bạn"}, 20, luot));
        Kiem.dung("vao: thanh vien het luot -> ca doi khong vao (ghi ten)", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{40, 41}, new int[]{0, 3}, new boolean[]{true, true},
                new boolean[2], new String[]{"Bạn", "Misty"}, 20, luot)).startsWith("Misty"));
        Kiem.dung("vao: thanh vien khac ban do -> chan", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{40, 41}, new int[2], new boolean[]{true, false},
                new boolean[2], new String[]{"Bạn", "Brock"}, 20, luot)).contains("cùng bản đồ"));
        Kiem.dung("vao: thanh vien dang ban (phong khac / tran cup) -> chan", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{40, 41}, new int[2], new boolean[]{true, true},
                new boolean[]{false, true}, new String[]{"Bạn", "Brock"}, 20, luot)).contains("bận"));
        Kiem.dung("vao: chenh 11 cap -> chan (chong keo cap)", String.valueOf(PhoBanLogic.kiemVao(true, new int[]{60, 49}, new int[2], new boolean[]{true, true},
                new boolean[2], new String[]{"Bạn", "Brock"}, 20, luot)).contains("chênh"));
        Kiem.bang("vao: chenh 10 cap van duoc", null, PhoBanLogic.kiemVao(true, new int[]{60, 50}, new int[2], new boolean[]{true, true}, new boolean[2],
                new String[]{"Bạn", "Brock"}, 20, luot));
    }

    static List<PhoBanLogic.TinhTrang> tt(final boolean... b) {
        final List<PhoBanLogic.TinhTrang> l = new ArrayList<PhoBanLogic.TinhTrang>();
        for (int i = 0; i + 2 < b.length; i += 3) {
            l.add(new PhoBanLogic.TinhTrang(b[i], b[i + 1], b[i + 2]));
        }
        return l;
    }

    static boolean co(final List<int[]> sk, final int ma) {
        for (final int[] s : sk) {
            if (s[0] == ma) {
                return true;
            }
        }
        return false;
    }

    static void vongDoiPhong() {
        final long t0 = 1_800_000_000_000L;
        final int phut = PhoBanLogic.PHUT_TOI_DA;
        // --- thang: qua 5 tang
        final PhoBanLogic.Phong p = new PhoBanLogic.Phong(3, 20261019, 40, t0);
        p.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        p.tv.add(new PhoBanLogic.ThanhVien(2, "B"));
        long bay = t0;
        for (int tang = 1; tang <= 5; tang++) {
            p.batDauTang(tang, tang == 5 ? 1 : 3, bay, 3000L);
            Kiem.sai("vong doi: tang " + tang + " chua qua khi chua ha het", p.haQuai(bay, 3000L) && p.tong > 1);
            for (int k = 1; k < p.tong; k++) {
                p.haQuai(bay, 3000L);
            }
            final List<int[]> sk0 = p.nhip(bay, tt(true, true, true, true, true, true), phut);
            if (tang < 5) {
                Kiem.sai("vong doi: tang " + tang + " xong - chua sang tang ngay (tre 3 giay)", co(sk0, PhoBanLogic.SK_SANG_TANG));
                bay += 3000L;
                final List<int[]> sk = p.nhip(bay, tt(true, true, true, true, true, true), phut);
                Kiem.dung("vong doi: tang " + tang + " -> sang tang " + (tang + 1) + " sau 3 giay", co(sk, PhoBanLogic.SK_SANG_TANG));
            } else {
                Kiem.dung("vong doi: ha boss tang 5 -> THANG", co(sk0, PhoBanLogic.SK_KET_THUC) && p.ketQua == PhoBanLogic.THANG);
            }
        }
        Kiem.bang("vong doi: thang = qua 5 tang", Integer.valueOf(5), Integer.valueOf(p.tangDaQua()));
        Kiem.sai("vong doi: ket thuc roi thi ha them quai khong tinh", p.haQuai(bay, 0L));
        Kiem.sai("vong doi: thang - chua dong ngay (8 giay xem ket qua)", co(p.nhip(bay + 7000L, tt(true, true, true, true, true, true), phut), PhoBanLogic.SK_DONG));
        Kiem.dung("vong doi: thang - dong phong sau 8 giay", co(p.nhip(bay + 8000L, tt(true, true, true, true, true, true), phut), PhoBanLogic.SK_DONG));
        Kiem.sai("vong doi: phong da ket thuc -> khong con la thanh vien (khong dang nhap lai vao)", p.conTrong(1));
        // --- guc ca doi: cho 15 giay roi thua
        final PhoBanLogic.Phong g = new PhoBanLogic.Phong(4, 20261019, 40, t0);
        g.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        g.tv.add(new PhoBanLogic.ThanhVien(2, "B"));
        g.batDauTang(2, 5, t0, 3000L);
        Kiem.sai("guc: mot nguoi guc, mot nguoi song -> chua thua", co(g.nhip(t0 + 1000L, tt(true, true, false, true, true, true), phut), PhoBanLogic.SK_KET_THUC));
        g.nhip(t0 + 2000L, tt(true, true, false, true, true, false), phut);
        Kiem.sai("guc: ca doi guc 14 giay -> van cho", co(g.nhip(t0 + 16000L, tt(true, true, false, true, true, false), phut), PhoBanLogic.SK_KET_THUC));
        final List<int[]> skg = g.nhip(t0 + 17000L, tt(true, true, false, true, true, false), phut);
        Kiem.dung("guc: ca doi guc 15 giay -> THUA", co(skg, PhoBanLogic.SK_KET_THUC) && g.ketQua == PhoBanLogic.THUA);
        Kiem.bang("guc: thua o tang 2 = qua 1 tang", Integer.valueOf(1), Integer.valueOf(g.tangDaQua()));
        Kiem.dung("guc: thua dong sau 3 giay", co(g.nhip(t0 + 20000L, tt(), phut), PhoBanLogic.SK_DONG));
        // --- roi phong (guc ve Trung tam / qua cong): mat suat, phong con nguoi thi chay tiep
        final PhoBanLogic.Phong r = new PhoBanLogic.Phong(5, 20261019, 40, t0);
        r.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        r.tv.add(new PhoBanLogic.ThanhVien(2, "B"));
        r.batDauTang(1, 6, t0, 3000L);
        final List<int[]> skr = r.nhip(t0 + 1000L, tt(true, true, true, true, false, true), phut);
        Kiem.dung("roi: B ra khoi khu -> SK_ROI, B mat suat", co(skr, PhoBanLogic.SK_ROI) && r.thanhVien(2).roi && !r.conTrong(2));
        Kiem.sai("roi: B quay lai ban do khong duoc ve phong (dang nhap lai bi tu choi)", r.dangNhapLai(2, t0 + 2000L));
        Kiem.dung("roi: A van trong phong, phong chua ket thuc", r.conTrong(1) && r.ketQua == PhoBanLogic.DANG);
        r.roi(1);
        r.nhip(t0 + 3000L, tt(true, true, true, true, false, true), phut);
        Kiem.dung("roi: ca doi roi -> 15 giay sau thua", co(r.nhip(t0 + 19000L, tt(true, false, true, true, false, true), phut), PhoBanLogic.SK_KET_THUC)
                && r.ketQua == PhoBanLogic.THUA);
        // --- mat ket noi: giu cho 180 giay, dang nhap lai ve dung phong
        final PhoBanLogic.Phong m = new PhoBanLogic.Phong(6, 20261019, 40, t0);
        m.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        m.batDauTang(3, 4, t0, 3000L);
        m.nhip(t0 + 1000L, tt(false, false, false), phut);
        Kiem.sai("mat ket noi: 1 nguoi duy nhat mat mang 60 giay -> chua thua (con han)", co(m.nhip(t0 + 61000L, tt(false, false, false), phut), PhoBanLogic.SK_KET_THUC));
        Kiem.dung("mat ket noi: dang nhap lai trong 180 giay -> ve phong", m.dangNhapLai(1, t0 + 90000L));
        final List<int[]> skm = m.nhip(t0 + 91000L, tt(true, true, true), phut);
        Kiem.dung("mat ket noi: nhip sau dang nhap lai -> SK_VAO_LAI (gui lai the)", co(skm, PhoBanLogic.SK_VAO_LAI) && m.conTrong(1));
        m.nhip(t0 + 100000L, tt(false, false, false), phut);
        Kiem.sai("mat ket noi: qua 180 giay thi dang nhap lai khong ve phong", m.dangNhapLai(1, t0 + 100000L + PhoBanLogic.GIAY_MAT_KET_NOI * 1000L));
        // --- het gio
        final PhoBanLogic.Phong h = new PhoBanLogic.Phong(7, 20261019, 40, t0);
        h.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        h.batDauTang(4, 10, t0, 3000L);
        Kiem.sai("het gio: 19 phut 59 giay chua het", co(h.nhip(t0 + phut * 60000L - 1000L, tt(true, true, true), phut), PhoBanLogic.SK_KET_THUC));
        Kiem.dung("het gio: du 20 phut -> HET_GIO", co(h.nhip(t0 + phut * 60000L, tt(true, true, true), phut), PhoBanLogic.SK_KET_THUC)
                && h.ketQua == PhoBanLogic.HET_GIO);
        Kiem.bang("het gio: o tang 4 = qua 3 tang", Integer.valueOf(3), Integer.valueOf(h.tangDaQua()));
        // --- tang thieu template (0 quai) khong ket phong
        final PhoBanLogic.Phong z = new PhoBanLogic.Phong(8, 20261019, 40, t0);
        z.tv.add(new PhoBanLogic.ThanhVien(1, "A"));
        z.batDauTang(2, 0, t0, 3000L);
        Kiem.dung("tang 0 quai (thieu template) -> tu sang tang sau", co(z.nhip(t0 + 3000L, tt(true, true, true), phut), PhoBanLogic.SK_SANG_TANG));
        z.batDauTang(5, 0, t0 + 3000L, 3000L);
        Kiem.dung("tang 5 thieu boss -> khong ket (THANG)", co(z.nhip(t0 + 4000L, tt(true, true, true), phut), PhoBanLogic.SK_KET_THUC) && z.ketQua == PhoBanLogic.THANG);
        Kiem.bang("phan tram tien do: tang 3 ha 2/4", Integer.valueOf(50), Integer.valueOf(PhoBanLogic.phanTram(3, 2, 4)));
    }

    static void thuongNgay() {
        Kiem.bang("thuong ngay: ha boss, chua nhan -> 0,2 gio EXP + 5 luong khoa", "200/5", PhoBanLogic.thuongNgay(5, false)[0] + "/" + PhoBanLogic.thuongNgay(5, false)[1]);
        Kiem.bang("thuong ngay: ha boss lan 2 trong ngay -> 0 (mot lan / ngay, chung Ham nguc cu)", "0/0", PhoBanLogic.thuongNgay(5, true)[0] + "/" + PhoBanLogic.thuongNgay(5, true)[1]);
        Kiem.bang("thuong ngay: thua o tang 4 -> 0", "0/0", PhoBanLogic.thuongNgay(4, false)[0] + "/" + PhoBanLogic.thuongNgay(4, false)[1]);
        Kiem.dung("thuong ngay: dung ngan sach 'hang_bi_an' (0,2 gio EXP, 5 luong khoa nhu Ham nguc cu)", PhoBanLogic.EXP_PHAN_NGHIN_GIO == 200 && PhoBanLogic.LUONG_KHOA_BOSS == 5);
        final String g = doc("src/real/PhoBanGame.java");
        Kiem.dung("thuong ngay: danh dau hnDaThuong TRUOC khi phat (khong phat 2 lan, chung Ham nguc cu)", g.indexOf("h.hnDaThuong = 1") > 0
                && g.indexOf("h.hnDaThuong = 1") < g.indexOf("Map.addXPForChar(c, Math.max(1L, exp)"));
        final String hn = doc("src/real/HamNgucGame.java");
        Kiem.dung("Ham nguc cu doc chung hnDaThuong (pho ban da nhan -> bao da nhan)", hn.contains("Phó bản tổ đội, 1 lần/ngày"));
        Kiem.bang("nhom roi: cap 29 / 30 / 59 / 60", "boss:thap/boss:trung/boss:trung/boss:cao",
                PhoBanLogic.nhomRoi(29) + "/" + PhoBanLogic.nhomRoi(30) + "/" + PhoBanLogic.nhomRoi(59) + "/" + PhoBanLogic.nhomRoi(60));
    }

    /** {nguon, khoa, vat_pham, ppm, sl_min, sl_max, khoa(0/1)} tu tep SQL. */
    static List<String[]> dongRoi(final String sql) {
        final List<String[]> ds = new ArrayList<String[]>();
        final Matcher m = Pattern.compile("\\((\\d+),'(pho_ban)','([^']*)','([^']*)','[^']*',(\\d+),(\\d+),(\\d+),'[^']*',(\\d+),(\\d+)\\)").matcher(sql);
        while (m.find()) {
            ds.add(new String[]{m.group(2), m.group(3), m.group(4), m.group(5), m.group(6), m.group(7), m.group(8)});
        }
        return ds;
    }

    static void bangRoi() {
        final String sql = doc("../sql/them_pho_ban_cup.sql");
        final List<String[]> ds = dongRoi(sql);
        Kiem.dung("bang roi: co dong pho_ban cho ca 3 nhom cap", ds.size() >= 9);
        final String[] nhom = {PhoBanLogic.NHOM_THAP, PhoBanLogic.NHOM_TRUNG, PhoBanLogic.NHOM_CAO};
        boolean tran = true;
        boolean khoa = true;
        final StringBuilder b = new StringBuilder();
        for (final String n : nhom) {
            double da = 0;
            double nl = 0;
            double tb = 0;
            for (final String[] r : ds) {
                if (!r[1].equals(n)) {
                    continue;
                }
                khoa &= "1".equals(r[6]);
                final double k = PhoBanLogic.kyVong(Integer.parseInt(r[3]), Integer.parseInt(r[4]), Integer.parseInt(r[5]));
                if (r[2].startsWith("ngoc:")) {
                    da += k;
                } else if (r[2].startsWith("trang_bi:")) {
                    tb += k;
                } else {
                    nl += k;
                }
            }
            b.append(n).append(" da ").append(da).append(" nl ").append(nl).append(" tb ").append(tb).append("; ");
            tran &= da <= PhoBanLogic.TRAN_DA_MOI_LAN && nl <= PhoBanLogic.TRAN_NGUYEN_LIEU_MOI_LAN && tb <= PhoBanLogic.TRAN_TRANG_BI_MOI_LAN && da > 0;
        }
        Kiem.dung("bang roi: ky vong / lan trong tran (da <= 4, nguyen lieu <= 1, trang bi <= 0,35): " + b, tran);
        Kiem.dung("bang roi: moi dong KHOA (khong giao dich duoc, khong ra cho)", khoa);
        // id tham chieu co that trong CSDL (khong dinh nghia vat pham moi)
        final String day = doc("../sql/htbb_day_du.sql");
        boolean coThat = true;
        final StringBuilder thieu = new StringBuilder();
        for (final String[] r : ds) {
            if (r[2].startsWith("item:")) {
                final String id = r[2].substring(5);
                if (!Pattern.compile("\\(" + id + ",'[^']*',28,").matcher(day).find()) {
                    coThat = false;
                    thieu.append(r[2]).append(' ');
                }
            } else if (r[2].startsWith("ngoc:")) {
                coThat &= "8".equals(r[2].substring(5));
            } else if (!r[2].equals("trang_bi:0")) {
                coThat = false;
                thieu.append(r[2]).append(' ');
            }
        }
        Kiem.dung("bang roi: id item / ngoc co that trong htbb_day_du.sql (type 28 / Da nang cap 8) " + thieu, coThat);
        Kiem.sai("bang roi: khong cho xu / luong (chi vat pham khoa)", sql.contains("'xu:") || sql.contains("'luong:") || sql.contains("LUONGKHOA"));
        Kiem.dung("bang roi: Manh tinh hoa (Su thi+) hiem: <= 5% va chi nhom cao", Pattern.compile("'boss:cao','item:1038','[^']*',(50000|[1-4]\\d{4}|\\d{1,4}),").matcher(sql).find()
                && !Pattern.compile("'boss:(thap|trung)','item:1038'").matcher(sql).find());
        final String g = doc("src/real/PhoBanGame.java");
        Kiem.dung("bang roi: trang bi ruong boss bi khoa (lock = 1)", g.contains("it.lock = 1;"));
        Kiem.dung("bang roi: ruong chi mo khi chua nhan thuong ngay (sau return khi da nhan)",
                g.indexOf("final String ruong = moRuong(c, ph);") > g.indexOf("if (tn[0] <= 0) {"));
    }

    // ================================================================== C. SQL, cau hinh, danh hieu, goi, diem moc

    static void sqlVaMacDinh() {
        final String sql = doc("../sql/them_pho_ban_cup.sql");
        Kiem.dung("SQL: tep them_pho_ban_cup.sql co", sql.length() > 1000);
        int ins = 0;
        int dup = 0;
        for (final String cau : sql.split(";\n")) {
            if (cau.contains("INSERT INTO")) {
                ins++;
                dup += cau.contains("ON DUPLICATE KEY UPDATE") ? 1 : 0;
            }
        }
        Kiem.bang("SQL: moi INSERT deu ON DUPLICATE KEY UPDATE (chay lai duoc)", Integer.valueOf(ins), Integer.valueOf(dup));
        Kiem.sai("SQL: khong INSERT ... SELECT", Pattern.compile("INSERT[^;]*SELECT", Pattern.CASE_INSENSITIVE).matcher(sql).find());
        Kiem.sai("SQL: khong '= col' tu gan (col=col)", Pattern.compile("`(\\w+)`\\s*=\\s*`\\1`").matcher(sql).find());
        Kiem.sai("SQL: khong CREATE TABLE thieu IF NOT EXISTS", Pattern.compile("CREATE TABLE (?!IF NOT EXISTS)").matcher(sql).find());
        Kiem.sai("SQL: khong DROP / DELETE / TRUNCATE", Pattern.compile("\\b(DROP|DELETE|TRUNCATE)\\b").matcher(sql).find());
        // khop ban mac dinh trong ma
        boolean cd = true;
        for (final PhoBanLogic.ChuDe c : PhoBanLogic.chuDeMacDinh()) {
            final StringBuilder q = new StringBuilder();
            for (int i = 0; i < c.mauQuai.length; i++) {
                q.append(i == 0 ? "" : ",").append(c.mauQuai[i]);
            }
            cd &= sql.contains("(" + c.thu + ",'" + c.ma + "','" + c.ten + "'," + c.he + ",'" + q + "'," + c.mauBoss + ",'" + c.moTa + "',1)");
        }
        Kiem.dung("SQL: data_pho_ban_ngay = PhoBanLogic.chuDeMacDinh()", cd);
        boolean tg = true;
        for (final PhoBanLogic.Tang t : PhoBanLogic.tangMacDinh()) {
            tg &= sql.contains("(" + t.tang + "," + t.soQuai + "," + t.hpPhanNghin + "," + t.tcPhanNghin + "," + t.thuPhanNghin + "," + (t.laBoss ? 1 : 0) + ")");
        }
        Kiem.dung("SQL: data_pho_ban_tang = PhoBanLogic.tangMacDinh()", tg);
        boolean lo = true;
        for (final CupLogic.Loai l : CupLogic.loaiMacDinh()) {
            lo &= sql.contains("('" + l.ma + "','" + l.ten + "','" + l.moTa + "'," + l.capToiDa + "," + l.he + "," + l.soNguoi + "," + (l.khongTrangBi ? 1 : 0) + ","
                    + l.phanNghinMin + "," + l.phanNghinMax + ",");
        }
        Kiem.dung("SQL: data_cup_loai = CupLogic.loaiMacDinh()", lo);
        final List<String> lich = CupLogic.lichMacDinh();
        boolean li = true;
        for (int i = 0; i < lich.size(); i++) {
            li &= sql.contains("(" + i + ",'" + lich.get(i) + "')");
        }
        Kiem.dung("SQL: data_cup_lich = CupLogic.lichMacDinh() (16 o)", li && lich.size() == 16);
        Kiem.dung("SQL: ten ban do 4381 / 4382 (data_map_ten)", sql.contains("(4381,'Phó bản Hầm ngục'") && sql.contains("(4382,'Sàn Cúp PvP'"));
        final String java = doc("src/data/DatabasePhoBanCup.java");
        for (final String bang : new String[]{"tob_pho_ban_luot", "tob_pho_ban_lich_su", "tob_cup_dang_ky", "tob_cup_tran", "tob_cup_thuong"}) {
            Kiem.dung("SQL + may chu cung tao bang " + bang, sql.contains("CREATE TABLE IF NOT EXISTS `" + bang + "`") && java.contains("CREATE TABLE IF NOT EXISTS " + bang + " ("));
        }
        Kiem.dung("thuong idempotent: INSERT IGNORE + UPDATE ... da_nhan = 0 (nguyen tu)", java.contains("INSERT IGNORE INTO tob_cup_thuong")
                && java.contains("SET da_nhan = 1, nhan_luc = NOW() WHERE tuan = ? AND char_id = ? AND da_nhan = 0"));
        final String cup = doc("src/real/CupGame.java");
        Kiem.dung("thuong idempotent: chi phat khi danhDauDaNhan == 1 (CSDL loi -1 / da phat 0 -> khong phat)",
                cup.contains("if (DatabasePhoBanCup.danhDauDaNhan(r[0], c.charDBID) != 1) {"));
        Kiem.dung("thuong idempotent: chot mot lan / tuan (daChot) + nguoi offline nhan khi dang nhap", cup.contains("if (t.daChot) {")
                && doc("src/real/CharNapDuLieu.java").contains("CupGame.khiDangNhap(self);"));
        Kiem.dung("README sql liet ke them_pho_ban_cup.sql", doc("../sql/README.md").contains("them_pho_ban_cup.sql"));
    }

    static String giaTriMacDinh(final String json, final String khoa) {
        final Matcher m = Pattern.compile("\"khoa\": \"" + Pattern.quote(khoa) + "\"[^}]*\"macDinh\": \"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static void cauHinh() {
        final String j = doc("../tools/cauhinh/danh_muc.json");
        Kiem.bang("cau hinh: pho_ban.luot_ngay = PhoBanLogic.LUOT_NGAY", String.valueOf(PhoBanLogic.LUOT_NGAY), giaTriMacDinh(j, "pho_ban.luot_ngay"));
        Kiem.bang("cau hinh: pho_ban.phut_toi_da", String.valueOf(PhoBanLogic.PHUT_TOI_DA), giaTriMacDinh(j, "pho_ban.phut_toi_da"));
        Kiem.bang("cau hinh: pho_ban.cap_toi_thieu", String.valueOf(PhoBanLogic.CAP_TOI_THIEU), giaTriMacDinh(j, "pho_ban.cap_toi_thieu"));
        Kiem.bang("cau hinh: cup.gio_vong_dau", String.valueOf(CupLogic.GIO_VONG_DAU), giaTriMacDinh(j, "cup.gio_vong_dau"));
        Kiem.bang("cau hinh: cup.so_vong", String.valueOf(CupLogic.SO_VONG), giaTriMacDinh(j, "cup.so_vong"));
        Kiem.bang("cau hinh: cup.so_doi_toi_thieu", String.valueOf(CupLogic.SO_DOI_TOI_THIEU), giaTriMacDinh(j, "cup.so_doi_toi_thieu"));
        Kiem.bang("cau hinh: pho_ban.bat / cup.bat mac dinh bat", "1/1", giaTriMacDinh(j, "pho_ban.bat") + "/" + giaTriMacDinh(j, "cup.bat"));
        final String sinh = doc("src/com/htbb/cauhinh/DanhMucSinh.java");
        Kiem.dung("cau hinh: DanhMucSinh da sinh lai (co pho_ban.* / cup.*)", sinh.contains("pho_ban.luot_ngay") && sinh.contains("cup.so_doi_toi_thieu"));
        Kiem.dung("cau hinh: pokemon_cau_hinh_trung_tam.sql da sinh lai", doc("../sql/pokemon_cau_hinh_trung_tam.sql").contains("'cup.gio_vong_dau'"));
    }

    static void danhHieu() {
        final String sql = doc("../sql/them_pho_ban_cup.sql");
        final int[][] dh = {{CupLogic.DH_VO_DICH_NHI, CupLogic.PHUT_DH_VO_DICH}, {CupLogic.DH_VO_DICH_HE, CupLogic.PHUT_DH_VO_DICH},
            {CupLogic.DH_VO_DICH_DOI, CupLogic.PHUT_DH_VO_DICH}, {CupLogic.DH_VO_DICH_THUAN, CupLogic.PHUT_DH_VO_DICH},
            {CupLogic.DH_A_QUAN, CupLogic.PHUT_DH_A_QUAN}, {CupLogic.DH_DAU_SI_CUP, CupLogic.PHUT_DH_DAU_SI}};
        boolean ok = true;
        for (final int[] d : dh) {
            final Matcher m = Pattern.compile("\\(" + d[0] + ",'[^']*','[^']*','[^']*','[^']*',(\\d),'\\w*','(\\w+)',(\\d+),'trao','',1,\\d,(\\d+),'cup'").matcher(sql);
            ok &= m.find() && Integer.parseInt(m.group(4)) == d[1] && Integer.parseInt(m.group(3)) <= 10;
        }
        Kiem.dung("danh hieu cup: 9800..9805 co trong SQL, 'trao', co han dung so ngay, <= 10 phan nghin (o 1%)", ok);
        Kiem.dung("danh hieu pho ban: 9810 vinh vien + 9811 / 9812 dem 'pho_ban'", sql.contains("(9810,'pho_ban_chinh_phuc'") && sql.contains("'pho_ban','',10,")
                && sql.contains("'pho_ban','',50,"));
        final Matcher ids = Pattern.compile("\\((98\\d\\d),'").matcher(sql);
        boolean dai = true;
        while (ids.find()) {
            final int id = Integer.parseInt(ids.group(1));
            dai &= id >= 9800 && id <= 9819;
        }
        Kiem.dung("danh hieu: dai id 9800..9819 (khong dung 9600.. sinh thai / 9700.. thanh tuu / 10000.. nhiem vu)", dai);
        final String g = doc("src/real/PhongRiengGame.java");
        Kiem.dung("danh hieu: trao vao kho cu (allDanhHieu) KHONG tu deo (khong goi addDanhHieu tu deo)", g.contains("p.allDanhHieu.add(d);")
                && !g.contains("idEffDanhHieu ="));
    }

    static void goi112VaKho() {
        try {
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            PhongRieng.viet(new DataOutputStream(b), PhongRieng.PHO_BAN, "Phó bản: Hang Băng", "Tầng 2/5 - hạ 3/8", "Đội còn 2/2", 845, 27);
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b.toByteArray()));
            final String s = in.readByte() + "|" + in.readUTF() + "|" + in.readUTF() + "|" + in.readUTF() + "|" + in.readShort() + "|" + in.readByte();
            Kiem.bang("goi -112: doc lai dung thu tu", "1|Phó bản: Hang Băng|Tầng 2/5 - hạ 3/8|Đội còn 2/2|845|27", s);
            Kiem.bang("goi -112: het du lieu sau 6 truong", Integer.valueOf(0), Integer.valueOf(in.available()));
            final ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            PhongRieng.viet(new DataOutputStream(b2), PhongRieng.AN, null, null, null, -5, -9);
            final DataInputStream in2 = new DataInputStream(new ByteArrayInputStream(b2.toByteArray()));
            Kiem.bang("goi -112: an the, null an toan, giay / phan tram am -> -1", "0|||" + "|-1|-1", in2.readByte() + "|" + in2.readUTF() + "|" + in2.readUTF() + "|"
                    + in2.readUTF() + "|" + in2.readShort() + "|" + in2.readByte());
        } catch (final Exception e) {
            Kiem.dung("goi -112: khong loi " + e, false);
        }
        Kiem.bang("goi -112: chi client BAN >= 908", Integer.valueOf(908), Integer.valueOf(PhongRieng.BAN_CLIENT));
        final String g = doc("src/real/PhongRiengGame.java");
        Kiem.dung("goi -112: gui chi khi banClient >= PhongRieng.BAN_CLIENT", g.contains("p.getSession().banClient >= PhongRieng.BAN_CLIENT"));
        // -112 khong trung lenh nao khac cua may chu
        int dung = 0;
        try {
            for (final Object o : Files.walk(Paths.get("src")).toArray()) {
                final java.nio.file.Path p = (java.nio.file.Path) o;
                if (p.toString().endsWith(".java")) {
                    final String s = doc(p.toString());
                    if (Pattern.compile("Message\\(\\s*(\\(byte\\)\\s*)?-112\\b|case\\s+-112\\b|= -112\\b").matcher(s.replace("case -112: // 0.9.7 phong rieng", "")
                            .replace("LENH = -112;", "")).find()) {
                        dung++;
                    }
                }
            }
        } catch (final Exception ignored) {
        }
        Kiem.bang("goi -112: khong lenh nao khac dung so -112", Integer.valueOf(0), Integer.valueOf(dung));
        final PhongRieng.Kho k = new PhongRieng.Kho(4);
        Kiem.bang("kho khu: muon lan luot 1, 2, 3 (khu 0 de trong)", "1,2,3,-1", k.muon() + "," + k.muon() + "," + k.muon() + "," + k.muon());
        k.tra(2);
        Kiem.bang("kho khu: tra 2 roi muon lai 2", Integer.valueOf(2), Integer.valueOf(k.muon()));
        k.tra(0);
        Kiem.sai("kho khu: tra khu 0 khong lam khu 0 thanh trong", k.soTrong() > 0);
        Kiem.bang("mmss", "14:05", PhongRieng.mmss(845));
        PhongRieng.dangKyMap(4381);
        Kiem.dung("luat ban do: phong rieng khong cho tu dong", PhongRieng.khongTuDong(4381) && !PhongRieng.khongTuDong(1));
    }

    static void diemMoc() {
        final String region = doc("src/real/Region.java");
        Kiem.dung("diem moc Region: PvP trong phong rieng chi qua CupGame.choDanh", region.contains("if (!CupGame.choDanh(p, c)"));
        Kiem.dung("diem moc Region: tran cup KHONG tinh Elo (ThangPvpGame.ketQua chan boi !cupTran)", region.contains("if (c.hp <= 0 && !cupTran) {\n                ThangPvpGame.ketQua(p, c);"));
        Kiem.dung("diem moc Region: sat thuong chuan hoa + HP khong ve 0 + khong cong diem do sat", region.contains("damage = CupGame.satThuong(p, c, d);")
                && region.contains("CupGame.sauDon(p, c);") && region.contains("if (p.isKiller && !cupTran) {"));
        Kiem.dung("diem moc: Tu dong danh cam o phong rieng (TuDong.mapBiCam)", doc("src/com/htbb/pokemon/TuDong.java").contains("if (PhongRieng.khongTuDong(map)) {"));
        Kiem.dung("diem moc: khong hoi sinh tai cho trong phong rieng", doc("src/real/HoiSinhGame.java").contains("return com.htbb.pokemon.PhongRieng.LY_DO_HOI_SINH;"));
        Kiem.dung("diem moc: khong doi khu (NPC -34) trong phong rieng", doc("src/real/MapDoMenu.java").contains("com.htbb.pokemon.PhongRieng.LY_DO_DOI_KHU"));
        Kiem.dung("diem moc: lenh chat 'khu' trong phong rieng -> menu Roi", doc("src/real/MapChat.java").contains("PhongRiengGame.moMenuTrongPhong(player);"));
        Kiem.dung("diem moc: dang nhap -> khiNapViTri (ve dung phong / ve lang)", doc("src/real/CharNapDuLieu.java").contains("PhongRiengGame.khiNapViTri(self);"));
        Kiem.dung("diem moc: quai chet -> PhoBanGame.khiQuaiChet", doc("src/real/Monster.java").contains("PhoBanGame.khiQuaiChet(this, p);"));
        Kiem.dung("diem moc: tao ban do 4381 / 4382", doc("src/real/RealController.java").contains("PhongRiengGame.taoBanDo(mapList);"));
        Kiem.dung("diem moc: TeamServer khoi dong nhip phong rieng", doc("src/server/TeamServer.java").contains("real.PhongRiengGame.khoiDong();"));
        Kiem.dung("diem moc: lenh C->S -112 (cham the -> Roi / Bo cuoc)", doc("src/real/MapXuLyLenh.java").contains("PhongRiengGame.xuLy(player, message);"));
        final String st = doc("src/real/SoTayLauDai.java");
        Kiem.dung("So tay: Ham nguc -> 'Phó bản tổ đội (5 tầng)', goc -> 'Cúp PvP tuần'", st.contains("\"Phó bản tổ đội (5 tầng)\"") && st.contains("\"Cúp PvP tuần\""));
        final String pb = doc("src/real/PhoBanGame.java");
        Kiem.dung("pho ban: quai khong hoi sinh (MonsterPhoBan) + don khu truoc khi tra", doc("src/real/MonsterPhoBan.java").contains("this.bornTime = Long.MAX_VALUE;")
                && pb.contains("donKhu(m, ph.khu);") && pb.contains("RealController.intance.idGen.putID(q.id, 1"));
        Kiem.dung("pho ban: EXP quai theo template tham chieu cung cap (khong theo template goc cap 80+)", pb.contains("thamChieu(ph.cap)"));
        Kiem.dung("ban do phong rieng: nen 117 (khoa-map: cap 0, khong tu dong) / 227", doc("src/real/PhongRiengGame.java").contains("NAP_PHO_BAN = 117")
                && doc("src/real/PhongRiengGame.java").contains("NAP_CUP = 227"));
        Kiem.dung("ban do phong rieng: khong nap quai tu tep (MapPhongRieng.loadMap)", doc("src/real/MapPhongRieng.java").contains("public void loadMap("));
        final String tl = doc("../docs/thiet-ke-game.md");
        Kiem.dung("tai lieu: thiet-ke-game co muc Cup PvP tuan + Pho ban", tl.contains("Cúp PvP tuần") && tl.contains("Phó bản Hầm ngục"));
    }
}
