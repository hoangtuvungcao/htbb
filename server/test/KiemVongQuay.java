import com.htbb.pokemon.VongQuayLogic;
import com.htbb.pokemon.VongQuayLogic.CauHinh;
import com.htbb.pokemon.VongQuayLogic.KetQua;
import com.htbb.pokemon.VongQuayLogic.NguoiCuoc;
import com.htbb.pokemon.VongQuayLogic.NguoiDat;
import com.htbb.pokemon.VongQuayLogic.SoNgay;
import com.htbb.pokemon.VongQuayLogic.Vong;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vong quay gop hu (chu game 19/10/2026: "bam Dong y khong co gi xay ra" + "sao gioi han xu cao vay? chung ta theo co
 * che khong lam phat").
 *
 * Phan 1-6: ham thuan com.htbb.pokemon.VongQuayLogic (muc cuoc, tran, khoa cuoi luot, dong ho, hoan khi 1 nguoi, phi,
 * bao toan hu, chon theo trong so, gioi han thiet bi / IP). Phan 7: cau hinh mac dinh khop tools/cauhinh/danh_muc.json
 * va sql/sua_vong_quay.sql. Phan 8: quet ma - nut Dong y cua client gui goi -32 (ok = 1) va server dinh tuyen goi do
 * sang real.VongQuay; he cu (doXoso, popup 100, VongQuayLuong) da bo; cong chong clone duoc goi.
 */
public final class KiemVongQuay {

    private KiemVongQuay() {
    }

    static final long T0 = 1700000000000L;

    public static void chay() {
        Kiem.nhom("Vong quay gop hu - muc cuoc, tran, phi (ho hut), bao toan hu, hoan 1 nguoi, chong clone, nut Dong y");
        mucCuoc();
        dongHo();
        hoanMotNguoi();
        baoToanVaPhi();
        trongSo();
        soNgay();
        cauHinhMacDinh();
        quetMa();
    }

    static NguoiDat nd(final String ten, final int cap, final long soDu, final int luot, final long tong) {
        return new NguoiDat(ten, cap, soDu, luot, tong);
    }

    static NguoiCuoc nc(final String ten, final long so) {
        return new NguoiCuoc(ten, ten.hashCode() & 0xffff, so, "", "");
    }

    // ------------------------------------------------------------------ 1. kiem cuoc

    static void mucCuoc() {
        final CauHinh c = VongQuayLogic.macDinh(0);
        final Vong v = new Vong();
        Kiem.bang("thuong: dat 1.000 xu (toi thieu) duoc", null, VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 5000, 0, 0), 1000, "xu"));
        Kiem.bang("thuong: dat 10.000 xu (toi da) duoc", null, VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 50000, 0, 0), 10000, "xu"));
        final String it = VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 50000, 0, 0), 999, "xu");
        Kiem.dung("thuong: 999 xu bi chan (duoi toi thieu)", it != null && it.contains("tối thiểu 1.000"));
        final String nhieu = VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 99999999, 0, 0), 10001, "xu");
        Kiem.dung("thuong: 10.001 xu bi chan (tren toi da)", nhieu != null && nhieu.contains("tối đa 10.000"));
        Kiem.dung("so am / 0 bi chan", VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 5000, 0, 0), 0, "xu") != null
                && VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 5000, 0, 0), -5000, "xu") != null);
        Kiem.dung("so du khong du bi chan", VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 999, 0, 0), 1000, "xu") != null);
        final String cap = VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 19, 50000, 0, 0), 1000, "xu");
        Kiem.dung("thuong: cap 19 bi chan", cap != null && cap.contains("cấp 20"));
        final String capVip = VongQuayLogic.kiemCuoc(VongQuayLogic.macDinh(1), v, T0, nd("a", 49, 999999, 0, 0), 10000, "xu");
        Kiem.dung("VIP: cap 49 bi chan (can 50)", capVip != null && capVip.contains("cấp 50"));
        final String luot = VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 50000, 10, 0), 1000, "xu");
        Kiem.dung("tran 10 luot / ngay", luot != null && luot.contains("10 lượt"));
        final String ngay = VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 50000, 3, 25000), 6000, "xu");
        Kiem.dung("tran 30.000 xu / ngay (da dat 25.000, them 6.000 bi chan, bao con 5.000)",
                ngay != null && ngay.contains("30.000") && ngay.contains("còn 5.000"));
        Kiem.bang("tran ngay: them dung 5.000 thi duoc", null, VongQuayLogic.kiemCuoc(c, v, T0, nd("a", 20, 50000, 3, 25000), 5000, "xu"));

        v.them(nc("a", 1000), T0);
        final String hai = VongQuayLogic.kiemCuoc(c, v, T0 + 1000, nd("A", 20, 50000, 1, 1000), 1000, "xu");
        Kiem.dung("moi luot dat 1 lan (khong phan biet hoa thuong)", hai != null && hai.contains("1 lần"));

        final Vong day = new Vong();
        for (int i = 0; i < c.soNguoiToiDa; i++) {
            day.them(nc("n" + i, 1000), T0);
        }
        final String du = VongQuayLogic.kiemCuoc(c, day, T0 + 1000, nd("x", 20, 50000, 0, 0), 1000, "xu");
        Kiem.dung("du 20 nguoi bi chan", du != null && du.contains("20 người"));
        Kiem.bang("tran hu thuong = 20 x 10.000", 200000L, c.tranHu());
        Kiem.bang("tran hu VIP = 20 x 50.000", 1000000L, VongQuayLogic.macDinh(1).tranHu());

        final CauHinh dong = new CauHinh(false, 20, 1000, 10000, 30000, 10, 20, 10, 120, 10);
        Kiem.dung("vong quay tat: bi chan", VongQuayLogic.kiemCuoc(dong, new Vong(), T0, nd("a", 50, 50000, 0, 0), 1000, "xu") != null);

        final long[] g = VongQuayLogic.mucGoiY(c);
        Kiem.bang("muc goi y thuong", "[1000, 5500, 10000]", java.util.Arrays.toString(g));
        Kiem.bang("muc goi y VIP", "[10000, 30000, 50000]", java.util.Arrays.toString(VongQuayLogic.mucGoiY(VongQuayLogic.macDinh(1))));
        Kiem.bang("muc goi y luong", "[10, 55, 100]", java.util.Arrays.toString(VongQuayLogic.mucGoiY(VongQuayLogic.macDinh(2))));
        boolean hopLe = true;
        for (final int loai : new int[]{0, 1, 2}) {
            final CauHinh k = VongQuayLogic.macDinh(loai);
            for (final long m : VongQuayLogic.mucGoiY(k)) {
                hopLe &= VongQuayLogic.kiemCuoc(k, new Vong(), T0, nd("z", 100, Long.MAX_VALUE / 4, 0, 0), m, "xu") == null;
            }
        }
        Kiem.dung("moi muc goi y deu qua kiem cuoc", hopLe);
        Kiem.bang("dinh dang so", "1.234.567", VongQuayLogic.so(1234567));
        Kiem.bang("ti le 1/3", "33,3", VongQuayLogic.tiLe(1, 3));
        Kiem.bang("ti le 100%", "100", VongQuayLogic.tiLe(5, 5));
    }

    // ------------------------------------------------------------------ 2. dong ho

    static void dongHo() {
        final CauHinh c = VongQuayLogic.macDinh(0);
        final Vong v = new Vong();
        Kiem.bang("luot chua bat dau: hien ca luot 120 giay", 120, VongQuayLogic.conLaiGiay(c, v, T0));
        Kiem.sai("luot rong khong bao gio 'den gio'", VongQuayLogic.denGio(c, v, T0 + 999999999L));
        Kiem.bang("luot rong: khong ket thuc", null, VongQuayLogic.ketThucNeuDenGio(c, v, T0 + 999999999L, new Random(1)));
        v.them(nc("a", 1000), T0);
        Kiem.bang("dong ho chay tu nguoi dat dau tien (khong dung o 02:00)", 90, VongQuayLogic.conLaiGiay(c, v, T0 + 30000));
        v.them(nc("b", 1000), T0 + 50000);
        Kiem.bang("nguoi thu hai KHONG dat lai dong ho", 60, VongQuayLogic.conLaiGiay(c, v, T0 + 60000));
        Kiem.sai("con 11 giay: chua khoa", VongQuayLogic.dangKhoa(c, v, T0 + 109000));
        Kiem.dung("con 10 giay: khoa dat", VongQuayLogic.dangKhoa(c, v, T0 + 110000));
        final String k = VongQuayLogic.kiemCuoc(c, v, T0 + 111000, nd("c", 20, 50000, 0, 0), 1000, "xu");
        Kiem.dung("10 giay cuoi: nguoi moi bi chan", k != null && k.contains("khoá"));
        Kiem.bang("119,9 giay: chua ket thuc", null, VongQuayLogic.ketThucNeuDenGio(c, v, T0 + 119900, new Random(1)));
        final KetQua kq = VongQuayLogic.ketThucNeuDenGio(c, v, T0 + 120000, new Random(1));
        Kiem.dung("120 giay: ket thuc, co nguoi thang", kq != null && !kq.hoan && kq.nguoiThang != null);
        Kiem.bang("ket thuc xong: luot moi rong", 0, v.soNguoi());
        Kiem.bang("ket thuc xong: dong ho luot moi chua chay", 0L, v.batDau());
        Kiem.bang("phut:giay", "01:05", VongQuayLogic.phutGiay(65));
    }

    // ------------------------------------------------------------------ 3. hoan khi 1 nguoi

    static void hoanMotNguoi() {
        final CauHinh c = VongQuayLogic.macDinh(1);
        final Vong v = new Vong();
        v.them(nc("chu_game", 50000), T0);
        final KetQua kq = VongQuayLogic.ketThucNeuDenGio(c, v, T0 + 120000, new Random(7));
        Kiem.dung("1 nguoi het gio: HOAN, khong ai 'thang 100%'", kq != null && kq.hoan && kq.nguoiThang == null);
        Kiem.bang("1 nguoi: khong thu phi", 0L, kq == null ? -1L : kq.phi);
        Kiem.bang("1 nguoi: hoan du so da dat", 50000L, kq == null ? -1L : kq.tongTra());
        Kiem.bang("1 nguoi: hoan cho dung nguoi", "chu_game", kq == null || kq.nguoi.isEmpty() ? "" : kq.nguoi.get(0).khoa);
    }

    // ------------------------------------------------------------------ 4. bao toan + phi

    static void baoToanVaPhi() {
        final Random r = new Random(20261019L);
        boolean baoToan = true;
        boolean thangTrongDs = true;
        boolean phiDung = true;
        long tongVao = 0L;
        long tongRa = 0L;
        long tongPhi = 0L;
        for (int vong = 0; vong < 5000; vong++) {
            final int loai = vong % 3;
            final CauHinh c = VongQuayLogic.macDinh(loai);
            final Vong v = new Vong();
            final int n = 1 + r.nextInt(c.soNguoiToiDa);
            long vao = 0L;
            for (int i = 0; i < n; i++) {
                final long so = c.cuocToiThieu + (long) (r.nextDouble() * (c.cuocToiDa - c.cuocToiThieu + 1));
                if (VongQuayLogic.kiemCuoc(c, v, T0 + i, nd("p" + i, 100, so, 0, 0), so, "xu") != null) {
                    baoToan = false;
                    continue;
                }
                v.them(nc("p" + i, so), T0 + i);
                vao += so;
            }
            final KetQua kq = VongQuayLogic.ketThucNeuDenGio(c, v, T0 + c.giayMoiVong * 1000L + 50, r);
            if (kq == null) {
                baoToan = false;
                continue;
            }
            baoToan &= kq.hu == vao && kq.tongTra() + kq.phi == vao;
            if (!kq.hoan) {
                thangTrongDs &= kq.nguoi.contains(kq.nguoiThang);
                phiDung &= kq.phi == vao * c.phiPhanTram / 100L && kq.phi > 0L;
            } else {
                phiDung &= kq.phi == 0L && n == 1;
            }
            tongVao += vao;
            tongRa += kq.tongTra();
            tongPhi += kq.phi;
        }
        Kiem.dung("5.000 luot ngau nhien: moi luot hu = tong cuoc, tong vao = tong tra + phi (khong in tien)", baoToan);
        Kiem.bang("tong ca 5.000 luot: vao = ra + phi", tongVao, tongRa + tongPhi);
        Kiem.dung("phi > 0 (ho hut) va ra < vao", tongPhi > 0L && tongRa < tongVao);
        Kiem.dung("phi dung % moi loai (10 / 10 / 15), hoan thi phi 0", phiDung);
        Kiem.dung("nguoi thang luon la nguoi da dat", thangTrongDs);
        Kiem.bang("phi lam tron xuong: 10% cua 19.999 = 1.999", 1999L, VongQuayLogic.phi(VongQuayLogic.macDinh(0), 19999L));
        final CauHinh re = new CauHinh(true, 20, 1000, 10000, 30000, 10, 20, 0, 120, 10);
        Kiem.bang("phi cau hinh 0% bi kep len 5% (khong thanh duong chuyen xu mien phi)", 5, re.phiPhanTram);
        final CauHinh cao = new CauHinh(true, 20, 1000, 10000, 30000, 10, 20, 99, 120, 10);
        Kiem.bang("phi cau hinh 99% bi kep xuong 50%", 50, cao.phiPhanTram);
    }

    // ------------------------------------------------------------------ 5. trong so

    static void trongSo() {
        final Random r = new Random(42L);
        int aThang = 0;
        final int lan = 20000;
        for (int i = 0; i < lan; i++) {
            final Vong v = new Vong();
            v.them(nc("a", 30000), T0);
            v.them(nc("b", 10000), T0);
            final KetQua kq = VongQuayLogic.ketThuc(VongQuayLogic.macDinh(1), v, r);
            if ("a".equals(kq.nguoiThang.khoa)) {
                aThang++;
            }
        }
        final double tl = aThang * 100.0 / lan;
        Kiem.dung("dat 3/4 hu thang ~75% (" + Math.round(tl * 10) / 10.0 + "%)", tl > 73.5 && tl < 76.5);
        Kiem.bang("trong so: chi 1 phan tu > 0", 2, VongQuayLogic.chonTheoTrongSo(new long[]{0, 0, 5}, new Random(3)));
    }

    // ------------------------------------------------------------------ 6. so ngay (thiet bi / IP, tran ngay)

    static void soNgay() {
        final SoNgay s = new SoNgay();
        final String n = "20261019";
        Kiem.bang("nick 1 cung may: duoc", null, s.kiemMay(n, "a", "MAY1", "1.2.3.4", 2));
        s.ghi(n, 0, "a", "MAY1", "1.2.3.4", 1000);
        Kiem.bang("nick 2 cung may: duoc", null, s.kiemMay(n, "b", "MAY1", "9.9.9.9", 2));
        s.ghi(n, 1, "b", "MAY1", "9.9.9.9", 10000);
        final String ba = s.kiemMay(n, "c", "MAY1", "5.5.5.5", 2);
        Kiem.dung("nick 3 cung thiet bi / ngay: bi chan (gioi han chong clone 2)", ba != null && ba.contains("tối đa 2"));
        final String ip = s.kiemMay(n, "d", "MAY9", "9.9.9.9", 1);
        Kiem.dung("cung IP (gioi han 1): nick khac bi chan", ip != null);
        Kiem.bang("nick da tham gia van quay tiep duoc", null, s.kiemMay(n, "A", "MAY1", "1.2.3.4", 2));
        Kiem.bang("may / IP rong: khong chan theo khoa do", null, s.kiemMay(n, "e", "", "", 1));
        Kiem.bang("ngay moi: so dem lai", null, s.kiemMay("20261020", "c", "MAY1", "5.5.5.5", 2));
        s.ghi(n, 0, "x", "", "", 1000);
        s.ghi(n, 0, "x", "", "", 2000);
        Kiem.bang("luot / ngay dem theo loai", 2, s.luot(n, 0, "X"));
        Kiem.bang("tong / ngay", 3000L, s.tong(n, 0, "x"));
        Kiem.bang("loai khac dem rieng", 0, s.luot(n, 1, "x"));
        s.hoan(n, 0, "x", 2000);
        Kiem.bang("hoan (1 nguoi) tra lai luot", 1, s.luot(n, 0, "x"));
        Kiem.bang("hoan tra lai tong", 1000L, s.tong(n, 0, "x"));
        s.nap(n, 2, "y", "MAY7", "", 4, 400);
        Kiem.bang("nap lai tu CSDL: luot", 4, s.luot(n, 2, "y"));
        Kiem.dung("nap lai tu CSDL: thiet bi duoc tinh", s.kiemMay(n, "z", "MAY7", "", 1) != null);
    }

    // ------------------------------------------------------------------ 7. cau hinh mac dinh = danh muc + sql

    static void cauHinhMacDinh() {
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        final String sql = doc("../sql/sua_vong_quay.sql");
        Kiem.dung("doc duoc danh_muc.json va sql/sua_vong_quay.sql", !dm.isEmpty() && !sql.isEmpty());
        final String[] ma = {"thuong", "vip", "luong"};
        for (int loai = 0; loai < 3; loai++) {
            final CauHinh c = VongQuayLogic.macDinh(loai);
            final String k = "vong_quay." + ma[loai] + ".";
            Kiem.bang(k + "cap_toi_thieu", String.valueOf(c.capToiThieu), macDinh(dm, k + "cap_toi_thieu"));
            Kiem.bang(k + "cuoc_toi_thieu", String.valueOf(c.cuocToiThieu), macDinh(dm, k + "cuoc_toi_thieu"));
            Kiem.bang(k + "cuoc_toi_da", String.valueOf(c.cuocToiDa), macDinh(dm, k + "cuoc_toi_da"));
            Kiem.bang(k + "cuoc_ngay_toi_da", String.valueOf(c.cuocNgayToiDa), macDinh(dm, k + "cuoc_ngay_toi_da"));
            Kiem.bang(k + "luot_ngay_toi_da", String.valueOf(c.luotNgayToiDa), macDinh(dm, k + "luot_ngay_toi_da"));
            Kiem.bang(k + "so_nguoi_toi_da", String.valueOf(c.soNguoiToiDa), macDinh(dm, k + "so_nguoi_toi_da"));
            Kiem.bang(k + "phi_phan_tram", String.valueOf(c.phiPhanTram), macDinh(dm, k + "phi_phan_tram"));
            Kiem.dung("sql/sua_vong_quay.sql co khoa " + k + "cuoc_toi_da", sql.contains("'" + k + "cuoc_toi_da'"));
        }
        Kiem.bang("vong_quay.giay_moi_vong", "120", macDinh(dm, "vong_quay.giay_moi_vong"));
        Kiem.bang("vong_quay.giay_khoa", "10", macDinh(dm, "vong_quay.giay_khoa"));

        // Theo dai cap (thiet_ke_can_bang.md 1.6 / LauDai.xuMoiGio): cuoc toi da <= ~1,5 gio thu nhap o cap mo,
        // tran ngay <= ~3,5 gio - nguoi choi khong the dat ca "kho" vai tram gio nhu muc cu (VIP 1 trieu - 1 ty).
        final CauHinh th = VongQuayLogic.macDinh(0);
        final CauHinh vip = VongQuayLogic.macDinh(1);
        final long gio20 = com.htbb.pokemon.LauDai.xuMoiGio(20);
        final long gio50 = com.htbb.pokemon.LauDai.xuMoiGio(50);
        Kiem.dung("thuong: cuoc toi da <= 1,5 gio thu nhap cap 20 (" + gio20 + " xu/gio)", th.cuocToiDa * 2 <= gio20 * 3);
        Kiem.dung("thuong: tran ngay <= 3,5 gio thu nhap cap 20", th.cuocNgayToiDa * 2 <= gio20 * 7);
        Kiem.dung("VIP: cuoc toi da <= 1,5 gio thu nhap cap 50 (" + gio50 + " xu/gio)", vip.cuocToiDa * 2 <= gio50 * 3);
        Kiem.dung("VIP: tran ngay <= 3,5 gio thu nhap cap 50", vip.cuocNgayToiDa * 2 <= gio50 * 7);
        Kiem.dung("muc toi thieu thuong <= 1/5 so xu khoi dau 5.000 (nguoi cap 20 dat duoc)", th.cuocToiThieu * 5 <= 5000);
        Kiem.dung("phi xu >= thue cho 7% (khong re hon giao dich)", th.phiPhanTram >= 7 && vip.phiPhanTram >= 7);
        Kiem.dung("phi luong >= phi chuyen luong 5%", VongQuayLogic.macDinh(2).phiPhanTram >= 5);
    }

    static String macDinh(final String dm, final String khoa) {
        final Matcher m = Pattern.compile("\"khoa\": \"" + Pattern.quote(khoa) + "\"[^}]*\"macDinh\": \"([^\"]*)\"").matcher(dm);
        return m.find() ? m.group(1) : "(khong co)";
    }

    // ------------------------------------------------------------------ 8. quet ma: nut Dong y, dinh tuyen, cong

    static void quetMa() {
        final String mh = doc("../client_jar/src/game/networklogic/MessageHandler.java");
        final String gs = doc("../client_jar/src/game/networklogic/GameService.java");
        final String hx = doc("../client_jar/src/game/pokemon/HopXacNhan.java");
        final int a = mh.indexOf("case -32:");
        final String c32 = a < 0 ? "" : mh.substring(a, Math.min(mh.length(), a + 900));
        Kiem.dung("client: goi -32 mo hop xac nhan (Huy / Dong y)", c32.contains("Canvas.startYesNoDlg(text5"));
        Kiem.dung("client: Dong y gui doCustomPopup(..., 1)", c32.matches("(?s).*doCustomPopup\\(idB, idPopup, text5, 1\\).*"));
        Kiem.dung("client: Huy gui doCustomPopup(..., 0)", c32.contains("doCustomPopup(idB, idPopup, text5, 0)"));
        final int g = gs.indexOf("public void doCustomPopup(");
        final String dc = g < 0 ? "" : gs.substring(g, Math.min(gs.length(), g + 500));
        Kiem.dung("client: doCustomPopup ghi short id, byte popup, UTF, byte ok va GUI",
                dc.matches("(?s).*init\\(\\(byte\\) -32\\).*writeShort\\(idB\\).*writeByte\\(idPopup\\).*writeUTF\\(text5\\).*writeByte\\(ok\\).*sendMessage\\(m\\).*"));
        Kiem.dung("client: nut chinh cua HopXacNhan goi hanh dong 'co'", hx.contains("HopXacNhan.this.xong(HopXacNhan.this.co)")
                && hx.contains("this.bam[1] = this.bamCo"));

        final String map = doc("src/real/Map.java");
        final int p = map.indexOf("void doPopup(Char player, Message message)");
        final String pop = p < 0 ? "" : map.substring(p, Math.min(map.length(), p + 4000));
        Kiem.dung("server doPopup doc dung thu tu short / byte / UTF / byte",
                pop.matches("(?s).*readShort\\(\\).*readByte\\(\\).*readUTF\\(\\).*readByte\\(\\).*"));
        Kiem.dung("server: popup vong quay -> VongQuay.theoPopup(idpopup).dongY(player)",
                map.contains("VongQuay.theoPopup(idpopup).dongY(player)"));
        Kiem.sai("server: khong con nhanh popup 100 lam moi dung hop cu (nut chet)", map.contains("player.infoXoso(0)"));

        final String vq = doc("src/real/VongQuay.java");
        Kiem.dung("ba popup rieng 97 / 98 / 99 (khong dung chung 100)", vq.contains("97, -32040") && vq.contains("98, -32041") && vq.contains("99, -32056"));
        Kiem.dung("Dong y: chua dat -> mo bang chon muc (moChonMuc), da dat -> lam moi", vq.matches("(?s).*public void dongY\\(.*guiThongTin\\(p\\).*moChonMuc\\(p, NPC\\).*"));
        Kiem.dung("cong chong clone: ChongClone.lyDo + gioi han cung thiet bi / IP", vq.contains("ChongClone.lyDo(p, true)")
                && vq.contains("SO_NGAY.kiemMay(") && vq.contains("nvCungMayToiDa"));
        Kiem.dung("moi lan thua -> thang ghi nhat ky chuyen gia tri", vq.contains("ChongClone.KIEU_VONG_QUAY"));
        Kiem.dung("luong: tru luong MO bang truLuongMoChuyen (khong dung luong khoa, khong cong diem xai)",
                vq.contains("p.truLuongMoChuyen(so)") && !vq.contains("subLuong(") && !vq.contains("LuongKhoa") && !vq.contains("addLuongLock"));
        Kiem.dung("xu: tru bang truXuNeuDu (khong tru am)", vq.contains("p.truXuNeuDu(so, false,"));
        Kiem.dung("nguoi offline: tra qua tob_vong_quay_cho_nhan, nhan khi dang nhap",
                vq.contains("DatabaseVongQuay.congChoNhan(") && doc("src/real/CharNapDuLieu.java").contains("VongQuay.khiDangNhap(self)"));

        final String menu = doc("src/real/MapDoMenu.java");
        Kiem.dung("MapDoMenu: NPC -15 menu vong quay -> VongQuay.doMenu", menu.contains("VongQuay.doMenu(player, idNpc, idMenu, idOptionMenu)"));
        Kiem.sai("MapDoMenu: het muc cu 1.000.000 - 50.000.000 xu", menu.contains("1.000.000 xu\\nGiá trị nhập xu cao nhất"));
        final String nt = doc("src/real/MapNhapText.java");
        Kiem.dung("MapNhapText: o nhap so -> VongQuay.theoO(idActor).nhapSo", nt.contains("VongQuay.theoO(idActor).nhapSo(player, text)"));

        final String ch = doc("src/real/Char.java");
        Kiem.sai("Char: he cu doXoso / doXosoLow / doXosoLuong da bo", ch.contains("void doXoso(") || ch.contains("void doXosoLow(") || ch.contains("void doXosoLuong("));
        Kiem.sai("Char: het muc VIP 1 ty xu / luot", ch.contains("MAX_BID_XOSO"));
        Kiem.sai("VongQuayLuong.java da bo", Files.exists(Paths.get("src/real/VongQuayLuong.java")));
        Kiem.sai("CharVongLap: het luong vong quay VIP cu", doc("src/real/CharVongLap.java").contains("Char.ALL_PLAYER_XOSO.size() > 1"));

        // Xo so (ra soat cung dot): goi -63 tho phai qua gio ban + chot ve cu, nhu menu NPC.
        final int x = map.indexOf("void doBuyXoso(Char player, Message message)");
        final String bx = x < 0 ? "" : map.substring(x, Math.min(map.length(), x + 2000));
        Kiem.dung("xo so: goi -63 kiem so 0..99, chot ve cu, gio ban, ve ky da quay",
                bx.contains("number > 99") && bx.contains("player.doOpenNumber()") && bx.contains("Char.timeBuy()")
                        && bx.contains("player.coVeKyDaQuay()"));
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
