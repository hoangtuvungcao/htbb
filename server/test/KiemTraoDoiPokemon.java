import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.TraoDoiPokemon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KIEM TRAO DOI POKEMON + CA THE + CHUYEN DOI (docs/thiet-ke-game.md 7.7, 7.8): lop thuan com.htbb.pokemon.TraoDoiPokemon,
 * CaThePokemon, HopPc (khong CSDL: "CSDL" la ham ghi gia, co the hong / nem loi / cham). RNG co hat giong.
 * <ul>
 * <li>doi nguyen tu: thanh cong doi dung, giu cap / sac mau / IV; ghi hong / nem loi (rot mang, sap truoc commit) = hai
 *     hop KHONG doi; nap lai tu chuoi da ghi = dung trang thai sau doi (sap sau commit);</li>
 * <li>hai cuoc trao doi dong thoi tren CUNG mot con (2 luong, 200 lan): dung mot cuoc thanh cong, khong nhan ban;</li>
 * <li>cong: con dang theo, khoa 24 gio, nhip, tran ngay, cho trong hop, con doi giua chung, tu doi voi minh;</li>
 * <li>phi: bac hiem / sac mau / 3 sao / tran; tien hoa khi trao doi (Haunter -&gt; Gengar);</li>
 * <li>ca the: IV 0..31, sao, tinh cach, chi so theo cong thuc; chuyen doi: giu con IV cao, tran xu / luong, con nhan
 *     qua trao doi chi ra Keo; SQL them_trade_pokemon.sql + khoa cau hinh.</li>
 * </ul>
 */
public final class KiemTraoDoiPokemon {

    private KiemTraoDoiPokemon() {
    }

    public static void chay() {
        Kiem.nhom("Ca the Pokemon: IV 0..31, sao, tinh cach, chi so (CaThePokemon)");
        kiemCaThe();
        Kiem.nhom("Trao doi Pokemon: doi nguyen tu, ghi hong / rot mang, dong thoi (TraoDoiPokemon)");
        kiemDoi();
        kiemDongThoi();
        Kiem.nhom("Trao doi Pokemon: cong (theo, khoa 24h, nhip, tran ngay, cho), phi, tien hoa khi trao doi");
        kiemCong();
        kiemPhi();
        Kiem.nhom("Chuyen doi con trung loai: giu con tot, tran xu / luong khoa, con nhan qua trao doi");
        kiemChuyen();
        Kiem.nhom("SQL them_trade_pokemon.sql + khoa cau hinh trao_doi.* / bat.chuyen_doi.*");
        kiemDuLieu();
    }

    static final long BAY = 1_800_000_000L;
    static final int NGAY = 20261022;

    static HopPc hop(final int chu, final int... dex) {
        final HopPc h = new HopPc();
        for (final int d : dex) {
            h.them(d, 20, false, NGAY, "poke", CaThePokemon.dongGoi(new int[]{10, 10, 10, 10, 10, 10}), 0, chu);
        }
        h.ban = false;
        return h;
    }

    static List<HopPc.Con> mau(final HopPc h, final int... id) {
        final List<HopPc.Con> v = new ArrayList<HopPc.Con>();
        for (final int i : id) {
            v.add(h.lay(i).saoVoiId(i));
        }
        return v;
    }

    /** "CSDL" gia: luu chuoi da ghi. */
    static final class Csdl implements TraoDoiPokemon.Luu {
        final Map<Integer, String> bang = new HashMap<Integer, String>();
        int lan;
        boolean hong;
        boolean nem;

        public boolean luu(final int idA, final String hopA, final int idB, final String hopB, final List<HopPc.Con> aDua,
                final List<HopPc.Con> bDua) throws Exception {
            this.lan++;
            if (this.nem) {
                throw new java.sql.SQLException("mat ket noi giua giao dich");
            }
            if (this.hong) {
                return false;
            }
            synchronized (this.bang) {
                this.bang.put(Integer.valueOf(idA), hopA);
                this.bang.put(Integer.valueOf(idB), hopB);
            }
            return true;
        }
    }

    static String dex(final HopPc h) {
        final StringBuilder sb = new StringBuilder();
        for (final HopPc.Con c : h.danhSach()) {
            sb.append(c.dex).append(c.shiny ? "s" : "").append('/').append(c.cap).append(' ');
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------ ca the

    static void kiemCaThe() {
        final int[] iv = {31, 0, 15, 31, 7, 20};
        final int g = CaThePokemon.dongGoi(iv);
        Kiem.bang("dong goi / mo goi IV", Arrays.toString(iv), Arrays.toString(CaThePokemon.moGoi(g)));
        Kiem.bang("tong / % IV", "104,55", CaThePokemon.tong(g) + "," + CaThePokemon.phanTram(g));
        final int full = CaThePokemon.dongGoi(new int[]{31, 31, 31, 31, 31, 31});
        Kiem.dung("100% = Hoan hao 3 sao", CaThePokemon.hoanHao(full) && CaThePokemon.sao(full) == 3
                && "Hoàn hảo".equals(CaThePokemon.chuDanhGia(full)));
        Kiem.bang("sao theo %: 0 / 50 / 51 / 67 / 81 / 82", "0,0,1,2,2,3", CaThePokemon.sao(0) + "," + sao(93) + "," + sao(95) + ","
                + sao(125) + "," + sao(151) + "," + sao(153));
        final Random r = new Random(20261022L);
        int min = 99;
        int max = -1;
        final int[] demSao = new int[4];
        final int[] demTc = new int[25];
        for (int i = 0; i < 50000; i++) {
            final int x = CaThePokemon.ivNgauNhien(r);
            for (int k = 0; k < 6; k++) {
                min = Math.min(min, CaThePokemon.iv(x, k));
                max = Math.max(max, CaThePokemon.iv(x, k));
            }
            demSao[CaThePokemon.sao(x)]++;
            demTc[CaThePokemon.tinhCachNgauNhien(r)]++;
        }
        Kiem.bang("IV ngau nhien trong 0..31 (50.000 con, hat giong)", "0,31", min + "," + max);
        Kiem.dung("3 sao rat hiem (~0,4%): " + demSao[3] * 100.0 / 50000 + "%, 0 sao pho bien (~50%): " + demSao[0] * 100.0 / 50000 + "%",
                demSao[3] > 100 && demSao[3] < 600 && demSao[0] > 22000 && demSao[0] < 28000);
        int tcMin = Integer.MAX_VALUE;
        for (final int d : demTc) {
            tcMin = Math.min(tcMin, d);
        }
        Kiem.dung("25 tinh cach deu xuat hien (it nhat " + tcMin + " lan)", tcMin > 1500);
        Kiem.bang("tinh cach: Kiên quyết +Tấn công -Tấn công ĐB; Cứng cỏi trung tính", "Kiên quyết (+Tấn công, -Tấn công đặc biệt)|-1",
                CaThePokemon.chuTinhCach(3) + "|" + CaThePokemon.oTang(0));
        Kiem.bang("Nhút nhát +Tốc độ -Tấn công; Khiêm tốn +Tấn công ĐB -Tấn công", "5,1,3,1", CaThePokemon.oTang(10) + ","
                + CaThePokemon.oGiam(10) + "," + CaThePokemon.oTang(15) + "," + CaThePokemon.oGiam(15));
        // Garchomp-kieu cong thuc game chinh ban: Blaziken cap 50, IV 31, tinh cach Kiên quyết (+Tấn công -Tấn công ĐB)
        final int[] goc = {80, 120, 70, 110, 70, 80};
        final int[] cs = CaThePokemon.chiSo(goc, full, 3, 50);
        Kiem.bang("chi so Blaziken cap 50 IV 31 Kiên quyết (khong EV)", "[155, 154, 90, 117, 90, 100]", Arrays.toString(cs));
        Kiem.dung("hai con cung loai cung cap khac IV -> Suc manh khac", CaThePokemon.sucManh(goc, full, 0, 50)
                > CaThePokemon.sucManh(goc, 0, 0, 50));
        Kiem.dung("IV tat dinh cho con cu: on dinh, trong 30 bit", CaThePokemon.ivTatDinh(5, 19, NGAY) == CaThePokemon.ivTatDinh(5, 19, NGAY)
                && CaThePokemon.ivTatDinh(5, 19, NGAY) >= 0 && CaThePokemon.ivTatDinh(5, 19, NGAY) < (1 << 30));
        // luu / nap giu ca the, chu goc, lan doi; chuoi cu (5 truong) van nap duoc
        final HopPc h = new HopPc();
        final HopPc.Con c = h.them(257, 40, true, NGAY, "ultra", g, 17, 77);
        c.doiLuc = BAY;
        final HopPc h2 = HopPc.giaiMa(h.maHoa());
        final HopPc.Con c2 = h2.lay(c.id);
        Kiem.dung("luu / nap: IV, tinh cach, chu goc, lan doi", c2 != null && c2.iv == g && c2.tinhCach == 17 && c2.chuGoc == 77
                && c2.doiLuc == BAY && c2.shiny);
        final HopPc cu = HopPc.giaiMa("v=1;ik=3;c1=19.5.0.20261020.poke;c2=255.7.1.20261020.");
        Kiem.dung("chuoi Hop PC cu (truoc ca the): van nap, IV tat dinh", cu.soCon() == 2 && cu.lay(1).iv == CaThePokemon.ivTatDinh(1, 19, 20261020)
                && cu.lay(2).shiny && cu.lay(2).chuGoc == 0);
    }

    static int sao(final int tong) {
        final int[] v = new int[6];
        int con = tong;
        for (int i = 0; i < 6; i++) {
            v[i] = Math.min(31, con);
            con -= v[i];
        }
        return CaThePokemon.sao(CaThePokemon.dongGoi(v));
    }

    // ------------------------------------------------------------------ doi

    static void kiemDoi() {
        final TraoDoiPokemon.Luat luat = TraoDoiPokemon.Luat.macDinh();
        final HopPc a = hop(1, 19, 93);
        final HopPc b = hop(2, 255);
        final HopPc.Con shiny = b.them(371, 33, true, NGAY, "ultra", CaThePokemon.dongGoi(new int[]{31, 31, 30, 31, 31, 29}), 3, 2);
        final Map<Integer, Integer> thd = new HashMap<Integer, Integer>();
        thd.put(Integer.valueOf(93), Integer.valueOf(94));
        final Csdl db = new Csdl();
        final TraoDoiPokemon.KetQua kq = TraoDoiPokemon.thucHien(1, a, mau(a, 2), 2, b, mau(b, shiny.id), BAY, NGAY, luat, thd, db);
        Kiem.dung("doi thanh cong: " + kq.loi, kq.ok);
        Kiem.bang("A: mat Haunter, nhan Bagon sac mau cap 33", "19/20 371s/33", dex(a));
        Kiem.bang("B: Haunter tien hoa thanh Gengar khi duoc trao doi", "255/20 94/20", dex(b));
        final HopPc.Con nhan = a.danhSach().get(1);
        Kiem.dung("con nhan giu IV / tinh cach / chu goc, danh dau lan doi", nhan.iv == shiny.iv && nhan.tinhCach == 3 && nhan.chuGoc == 2
                && nhan.doiLuc == BAY);
        Kiem.dung("ket qua bao tien hoa 93 -> 94", kq.tienHoa.size() == 1 && kq.tienHoa.get(0)[0] == 93 && kq.tienHoa.get(0)[1] == 94);
        Kiem.bang("bo dem: moi ben nhan 1, nhip, tong doi", "1,1," + BAY + ",1", a.doiNgay + "," + b.doiNgay + "," + a.doiCuoi + "," + b.tongDoi);
        Kiem.dung("CSDL ghi dung mot lan, chuoi = hop that (sap SAU commit: nap lai la dung)", db.lan == 1
                && HopPc.giaiMa(db.bang.get(Integer.valueOf(1))).maHoa().equals(a.maHoa())
                && HopPc.giaiMa(db.bang.get(Integer.valueOf(2))).maHoa().equals(b.maHoa()));
        Kiem.dung("da ghi CSDL -> hop khong con 'ban'", !a.ban && !b.ban);

        // ghi hong / rot mang giua giao dich: KHONG doi gi
        final HopPc x = hop(10, 19, 43);
        final HopPc y = hop(11, 255, 304);
        final String x0 = x.maHoa();
        final String y0 = y.maHoa();
        final Csdl hong = new Csdl();
        hong.hong = true;
        final TraoDoiPokemon.KetQua k1 = TraoDoiPokemon.thucHien(10, x, mau(x, 1), 11, y, mau(y, 2), BAY, NGAY, luat, thd, hong);
        Kiem.dung("CSDL tra false: huy, hai hop nguyen ven", !k1.ok && x.maHoa().equals(x0) && y.maHoa().equals(y0));
        hong.hong = false;
        hong.nem = true;
        final TraoDoiPokemon.KetQua k2 = TraoDoiPokemon.thucHien(10, x, mau(x, 1), 11, y, mau(y, 2), BAY, NGAY, luat, thd, hong);
        Kiem.dung("rot ket noi / sap TRUOC commit (nem loi): hai hop nguyen ven, khong ai mat", !k2.ok && x.maHoa().equals(x0)
                && y.maHoa().equals(y0) && x.soCon() == 2 && y.soCon() == 2 && k2.loi.contains("không ai mất"));
        Kiem.dung("khong co CSDL (null): khong doi", !TraoDoiPokemon.thucHien(10, x, mau(x, 1), 11, y, mau(y, 2), BAY, NGAY, luat, thd, null).ok
                && x.maHoa().equals(x0));
        // con doi giua luc khoa va luc chot (vd luyen cap): huy
        final List<HopPc.Con> mx = mau(x, 1);
        x.lay(1).cap++;
        final TraoDoiPokemon.KetQua k3 = TraoDoiPokemon.thucHien(10, x, mx, 11, y, mau(y, 2), BAY, NGAY, luat, thd, new Csdl());
        Kiem.dung("con doi cap sau khi khoa -> huy", !k3.ok && k3.loi.contains("thay đổi"));
        Kiem.dung("tu doi voi chinh minh -> tu choi", !TraoDoiPokemon.thucHien(10, x, mau(x, 2), 10, x, mau(x, 2), BAY, NGAY, luat, thd, new Csdl()).ok);
    }

    /** Hai cuoc trao doi DONG THOI tren cung mot con cua A (A-B va A-C): dung mot cuoc qua, khong nhan ban. */
    static void kiemDongThoi() {
        final TraoDoiPokemon.Luat luat = new TraoDoiPokemon.Luat(1000, 0L, 0L);
        int loi = 0;
        int haiCung = 0;
        for (int vong = 0; vong < 200; vong++) {
            final HopPc a = hop(1, 371);
            final HopPc b = hop(2, 19);
            final HopPc c = hop(3, 43);
            final List<HopPc.Con> ma = mau(a, 1);
            final List<HopPc.Con> mb = mau(b, 1);
            final List<HopPc.Con> mc = mau(c, 1);
            final CountDownLatch bat = new CountDownLatch(1);
            final AtomicInteger thanh = new AtomicInteger();
            final Csdl db = new Csdl();
            final Thread t1 = new Thread(() -> {
                try {
                    bat.await();
                } catch (final InterruptedException ignored) {
                }
                if (TraoDoiPokemon.thucHien(1, a, ma, 2, b, mb, BAY, NGAY, luat, null, db).ok) {
                    thanh.incrementAndGet();
                }
            });
            final Thread t2 = new Thread(() -> {
                try {
                    bat.await();
                } catch (final InterruptedException ignored) {
                }
                // khoa nguoc thu tu (C id 3 &gt; A id 1): kiem khong deadlock
                if (TraoDoiPokemon.thucHien(3, c, mc, 1, a, ma, BAY, NGAY, luat, null, db).ok) {
                    thanh.incrementAndGet();
                }
            });
            t1.start();
            t2.start();
            bat.countDown();
            try {
                t1.join(5000);
                t2.join(5000);
            } catch (final InterruptedException ignored) {
            }
            if (t1.isAlive() || t2.isAlive()) {
                loi++;
                continue;
            }
            final int bagon = a.soCungLoai(371, false) + b.soCungLoai(371, false) + c.soCungLoai(371, false);
            final int tong = a.soCon() + b.soCon() + c.soCon();
            if (thanh.get() != 1 || bagon != 1 || tong != 3) {
                haiCung++;
            }
        }
        Kiem.bang("200 vong x 2 luong cung tranh 1 con: khong deadlock", Integer.valueOf(0), Integer.valueOf(loi));
        Kiem.bang("200 vong: dung 1 cuoc thanh cong, Bagon van la 1 con, tong 3 con", Integer.valueOf(0), Integer.valueOf(haiCung));
    }

    // ------------------------------------------------------------------ cong

    static void kiemCong() {
        final TraoDoiPokemon.Luat luat = TraoDoiPokemon.Luat.macDinh();
        Kiem.bang("luat mac dinh: 10 / ngay, khoa 24 gio, nhip 60 giay", "10,86400,60", luat.tranNgay + "," + luat.khoaGiay + "," + luat.cachGiay);
        final HopPc a = hop(1, 19, 43, 92, 255);
        a.datTheo(1);
        Kiem.dung("con dang di theo: phai thoi theo truoc", TraoDoiPokemon.kiemBen(a, ids(1), 1, BAY, NGAY, luat).contains("Thôi đi theo"));
        Kiem.dung("khong dua con nao -> tu choi", TraoDoiPokemon.kiemBen(a, ids(), 1, BAY, NGAY, luat) != null);
        Kiem.dung("qua 3 con moi ben -> tu choi", TraoDoiPokemon.kiemBen(a, ids(1, 2, 3, 4), 1, BAY, NGAY, luat).contains("tối đa 3"));
        Kiem.dung("chon trung id -> tu choi", TraoDoiPokemon.kiemBen(a, ids(2, 2), 1, BAY, NGAY, luat) != null);
        Kiem.dung("con hop le -> duoc", TraoDoiPokemon.kiemBen(a, ids(2, 3), 2, BAY, NGAY, luat) == null);
        a.lay(2).doiLuc = BAY - 3600L;
        Kiem.dung("con vua nhan qua trao doi 1 gio truoc: khoa (con 23 gio)", TraoDoiPokemon.kiemBen(a, ids(2), 1, BAY, NGAY, luat).contains("23 giờ"));
        a.lay(2).doiLuc = BAY - 24L * 3600L;
        Kiem.dung("du 24 gio: trao doi tiep duoc", TraoDoiPokemon.kiemBen(a, ids(2), 1, BAY, NGAY, luat) == null);
        a.doiCuoi = BAY - 10L;
        Kiem.dung("nhip: vua trao doi 10 giay truoc -> cho 50 giay", TraoDoiPokemon.kiemBen(a, ids(3), 1, BAY, NGAY, luat).contains("50 giây"));
        a.doiCuoi = 0L;
        a.doiNgay(NGAY);
        a.doiNgay = 9;
        Kiem.dung("tran ngay: da nhan 9, nhan them 2 > 10 -> tu choi", TraoDoiPokemon.kiemBen(a, ids(3, 4), 2, BAY, NGAY, luat).contains("tối đa 10"));
        Kiem.dung("tran ngay: nhan them 1 = 10 -> duoc", TraoDoiPokemon.kiemBen(a, ids(3), 1, BAY, NGAY, luat) == null);
        a.doiNgay(NGAY + 1);
        Kiem.bang("sang ngay moi: bo dem nhan dat lai", Integer.valueOf(0), Integer.valueOf(a.doiNgay));
        final HopPc day = new HopPc();
        for (int i = 0; i < 30; i++) {
            day.them(19, 5, false, NGAY, "poke");
        }
        Kiem.dung("hop day: dua 1 nhan 2 -> khong du cho", TraoDoiPokemon.kiemBen(day, ids(1), 2, BAY, NGAY, luat).contains("không đủ chỗ"));
        Kiem.dung("hop day: dua 1 nhan 1 -> duoc", TraoDoiPokemon.kiemBen(day, ids(1), 1, BAY, NGAY, luat) == null);
    }

    static List<Integer> ids(final int... v) {
        final List<Integer> l = new ArrayList<Integer>();
        for (final int x : v) {
            l.add(Integer.valueOf(x));
        }
        return l;
    }

    static void kiemPhi() {
        // 10.000 xu / gio thu nhap, 50 phan nghin
        Kiem.bang("phi thuong / it gap / hiem / rat hiem = 5 / 10 / 20 / 40% gio", "500,1000,2000,4000", TraoDoiPokemon.phi(10000, 50, 2000, 0, false, 0)
                + "," + TraoDoiPokemon.phi(10000, 50, 2000, 1, false, 0) + "," + TraoDoiPokemon.phi(10000, 50, 2000, 2, false, 0) + ","
                + TraoDoiPokemon.phi(10000, 50, 2000, 3, false, 0));
        Kiem.bang("sac mau x4, 3 sao x2", "2000,1000", TraoDoiPokemon.phi(10000, 50, 2000, 0, true, 0) + "," + TraoDoiPokemon.phi(10000, 50, 2000, 0, false, 3));
        Kiem.bang("rat hiem sac mau 3 sao: kep tran 2 gio thu nhap", Long.valueOf(20000), Long.valueOf(TraoDoiPokemon.phi(10000, 50, 2000, 3, true, 3)));
        Kiem.bang("phi 0 khi tat", Long.valueOf(0), Long.valueOf(TraoDoiPokemon.phi(10000, 0, 2000, 3, true, 3)));
    }

    // ------------------------------------------------------------------ chuyen doi

    static void kiemChuyen() {
        final TraoDoiPokemon.ThuongChuyen[] t = TraoDoiPokemon.chuyenMacDinh();
        Kiem.bang("thuong chuyen doi mac dinh keo 2/3/5/8, xu 1/3/10/0%, luong 0/0/0/1", "2,3,5,8|10,30,100,0|0,0,0,1",
                t[0].keo + "," + t[1].keo + "," + t[2].keo + "," + t[3].keo + "|" + t[0].xuPhanNghinGio + "," + t[1].xuPhanNghinGio + ","
                        + t[2].xuPhanNghinGio + "," + t[3].xuPhanNghinGio + "|" + t[0].luongKhoa + "," + t[1].luongKhoa + "," + t[2].luongKhoa + ","
                        + t[3].luongKhoa);
        final HopPc h = new HopPc();
        final int thap = CaThePokemon.dongGoi(new int[]{3, 3, 3, 3, 3, 3});
        final int giua = CaThePokemon.dongGoi(new int[]{15, 15, 15, 15, 15, 15});
        final int cao = CaThePokemon.dongGoi(new int[]{30, 30, 30, 30, 30, 30});
        Kiem.bang("chua du 2 con cung loai: khong chuyen", Integer.valueOf(-1), Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, false, thap, 2, 1)));
        final HopPc.Con c1 = h.them(19, 5, false, NGAY, "poke", thap, 0, 1);
        final HopPc.Con c2 = h.them(19, 5, false, NGAY, "poke", giua, 0, 1);
        Kiem.bang("du 2 con, con moi IV cao: chuyen con cu IV thap nhat", Integer.valueOf(c1.id),
                Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, false, cao, 2, 1)));
        Kiem.bang("du 2 con, con moi IV thap hon: chuyen chinh con moi (0)", Integer.valueOf(0),
                Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, false, CaThePokemon.dongGoi(new int[]{1, 1, 1, 1, 1, 1}), 2, 1)));
        Kiem.bang("sac mau khong bao gio tu chuyen", Integer.valueOf(-1), Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, true, thap, 2, 1)));
        Kiem.bang("giu 0 = tat tu chuyen", Integer.valueOf(-1), Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, false, thap, 0, 1)));
        h.datTheo(c1.id);
        Kiem.bang("con dang theo khong bi tu chuyen (chon con con lai)", Integer.valueOf(c2.id),
                Integer.valueOf(TraoDoiPokemon.chonChuyenKhiBat(h, 19, false, cao, 2, 1)));
        // thuong + tran ngay: 10.000 xu / gio, tran 300 phan nghin = 3.000 xu / ngay
        final HopPc.Con hiem = new HopPc.Con(0, 123, 30, false, NGAY, "great");
        hiem.chuGoc = 1;
        long xu = 0;
        for (int i = 0; i < 5; i++) {
            xu += TraoDoiPokemon.tinhChuyen(h, hiem, 1, t[2], 10000, 300, 1, NGAY).xu;
        }
        Kiem.bang("Hiem: 1.000 xu / con, 5 con nhung tran ngay 3.000 xu", Long.valueOf(3000), Long.valueOf(xu));
        final TraoDoiPokemon.DaChuyen k = TraoDoiPokemon.tinhChuyen(h, hiem, 1, t[2], 10000, 300, 1, NGAY);
        Kiem.dung("qua tran: van ra Keo, 0 xu", k.keo == 5 && k.xu == 0);
        final HopPc.Con ratHiem = new HopPc.Con(0, 371, 30, true, NGAY, "ultra");
        ratHiem.chuGoc = 1;
        final TraoDoiPokemon.DaChuyen r1 = TraoDoiPokemon.tinhChuyen(h, ratHiem, 1, t[3], 10000, 300, 1, NGAY);
        final TraoDoiPokemon.DaChuyen r2 = TraoDoiPokemon.tinhChuyen(h, ratHiem, 1, t[3], 10000, 300, 1, NGAY);
        Kiem.dung("Rat hiem sac mau: 16 Keo + 1 luong khoa, lan 2 trong ngay 0 luong", r1.keo == 16 && r1.luongKhoa == 1 && r2.luongKhoa == 0);
        h.doiNgay(NGAY + 1);
        Kiem.dung("sang ngay moi: tran xu / luong dat lai", TraoDoiPokemon.tinhChuyen(h, hiem, 1, t[2], 10000, 300, 1, NGAY + 1).xu == 1000);
        final HopPc.Con nhan = new HopPc.Con(0, 123, 30, false, NGAY, "great");
        nhan.chuGoc = 2;
        final TraoDoiPokemon.DaChuyen kn = TraoDoiPokemon.tinhChuyen(h, nhan, 1, t[2], 10000, 300, 1, NGAY + 1);
        Kiem.dung("con nhan qua trao doi (chu goc khac): chi ra Keo, 0 xu (chan nuoi clone)", kn.keo == 5 && kn.xu == 0 && kn.luongKhoa == 0);
        final HopPc.Con doiVe = new HopPc.Con(0, 123, 30, false, NGAY, "great");
        doiVe.chuGoc = 1;
        doiVe.doiLuc = BAY;
        Kiem.dung("con da tung trao doi (ke ca doi ve tay chu): chi ra Keo", TraoDoiPokemon.tinhChuyen(h, doiVe, 1, t[2], 10000, 300, 1, NGAY + 1).xu == 0);
        // ngan sach: xu chuyen doi / ngay < xu bong toi thieu de bat du tran ngay (30 con x >= 1 Poke Ball 2% gio)
        Kiem.dung("ngan sach: tran xu chuyen doi 30% gio < 30 bong x 2% = 60% gio", 300 < 30 * 20);
    }

    // ------------------------------------------------------------------ du lieu

    static void kiemDuLieu() {
        final String sql = KiemBoNgocCheTao.doc("../sql/them_trade_pokemon.sql");
        Kiem.dung("doc duoc sql/them_trade_pokemon.sql", !sql.isEmpty());
        Kiem.dung("idempotent: CREATE TABLE IF NOT EXISTS / ADD COLUMN IF NOT EXISTS", sql.contains("ADD COLUMN IF NOT EXISTS `tien_hoa_trao_doi`")
                && sql.contains("CREATE TABLE IF NOT EXISTS `tob_log_trao_doi_pokemon`") && sql.contains("CREATE TABLE IF NOT EXISTS `data_bat_chi_so_loai`"));
        Kiem.dung("khong INSERT...SELECT...ON DUPLICATE KEY UPDATE col=col", !Pattern.compile("(?is)INSERT[^;]*SELECT[^;]*ON DUPLICATE KEY UPDATE")
                .matcher(sql).find() && !Pattern.compile("`(\\w+)` = `\\1`").matcher(sql).find());
        final String bat = KiemBoNgocCheTao.doc("../sql/them_bo_ngoc_che_tao_bat.sql");
        final Matcher m = Pattern.compile("^\\((\\d+),'", Pattern.MULTILINE).matcher(bat);
        final java.util.Set<String> loai = new java.util.HashSet<String>();
        while (m.find()) {
            loai.add(m.group(1));
        }
        final Matcher m2 = Pattern.compile("^\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\)", Pattern.MULTILINE).matcher(sql);
        final java.util.Set<String> cs = new java.util.HashSet<String>();
        int sai = 0;
        while (m2.find()) {
            cs.add(m2.group(1));
            for (int i = 2; i <= 7; i++) {
                final int v = Integer.parseInt(m2.group(i));
                if (v < 1 || v > 255) {
                    sai++;
                }
            }
        }
        Kiem.bang("moi loai thu phuc duoc co chi so goc (" + cs.size() + ")", loai.toString().length() > 0 ? Integer.valueOf(loai.size()) : null,
                Integer.valueOf(cs.size()));
        cs.removeAll(loai);
        Kiem.bang("chi so goc khong thua loai la", Integer.valueOf(0), Integer.valueOf(cs.size()));
        Kiem.bang("chi so goc trong 1..255", Integer.valueOf(0), Integer.valueOf(sai));
        Kiem.dung("Blaziken 80/120/70/110/70/80, Metagross 80/135/130/95/90/70", sql.contains("(257,80,120,70,110,70,80)")
                && sql.contains("(376,80,135,130,95,90,70)"));
        Kiem.dung("tien hoa khi trao doi: Haunter (93), Dusclops (356)", sql.contains("SET `tien_hoa_trao_doi` = 1 WHERE `dex` IN (93, 356)"));
        Kiem.dung("rat hiem: Bagon / Shelgon / Salamence / Metang / Metagross / Ursaluna", sql.contains("SET `do_hiem` = 3 WHERE `dex` IN (371, 372, 373, 375, 376, 901)"));
        Kiem.dung("thuong chuyen doi 4 bac", sql.contains("(0, 'Thường', 2, 10, 0)") && sql.contains("(3, 'Rất hiếm', 8, 0, 1)"));
        final String dm = KiemBoNgocCheTao.doc("../tools/cauhinh/danh_muc.json");
        final String[][] khoa = {
            {"bat.nguong_hp_phan_tram", "35"}, {"bat.so_lan_bo_chay", "4"}, {"bat.giu_moi_loai", "2"},
            {"bat.chuyen_doi.tran_xu_phan_nghin_gio", "300"}, {"bat.chuyen_doi.tran_luong_ngay", "1"},
            {"trao_doi.bat", "1"}, {"trao_doi.cap_toi_thieu", "10"}, {"trao_doi.tran_ngay", "10"}, {"trao_doi.khoa_gio", "24"},
            {"trao_doi.cach_giay", "60"}, {"trao_doi.phi_phan_nghin_gio", "50"}, {"trao_doi.phi_tran_phan_nghin_gio", "2000"},
        };
        for (final String[] k : khoa) {
            Kiem.dung("danh muc cau hinh: " + k[0] + " mac dinh " + k[1],
                    dm.matches("(?s).*\"khoa\": \"" + k[0].replace(".", "\\.") + "\"[^}]*\"macDinh\": \"" + k[1] + "\".*"));
        }
        final String sh = KiemBoNgocCheTao.doc("../tools/pokemon/xuat_sql_day_du.sh");
        Kiem.dung("xuat_sql_day_du.sh nap them_trade_pokemon sau them_bo_ngoc_che_tao_bat", sh.contains("BO_NGOC_BAT=\"them_bo_ngoc_che_tao_bat them_trade_pokemon\""));
    }
}
