import com.htbb.pokemon.BanHuong;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 0.9.8 NGAM TAY PvP (com.htbb.pokemon.BanHuong + real.BanHuongGame): hinh hoc duong dan (dan / xuyen / non / vung),
 * tam / be rong, tuong chan, bu tre 150 ms, dan bay theo thoi gian (ne duoc), chong spam, huong 4 phia client cu; phan
 * game o real.KiemNgamTayGame (tu dong bi tu choi trong PvP, khoa muc tieu bi tu choi, hoi chieu / PP, client cu).
 */
public final class KiemNgamTay {

    private KiemNgamTay() {
    }

    static String doc(final String p) {
        try {
            return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("Ngam tay PvP: hinh hoc ban theo huong (0.9.8)");
        thongSo();
        dan();
        xuyen();
        non();
        vung();
        tuong();
        buTre();
        danBay();
        kiemBan();
        spam();
        huongCu();
        Kiem.nhom("Ngam tay PvP: game (tu dong, khoa muc tieu, hoi chieu / PP, client cu)");
        try {
            real.KiemNgamTayGame.BAO = new real.KiemNgamTayGame.Bao() {
                @Override
                public void bang(final String moTa, final Object mongDoi, final Object thucTe) {
                    Kiem.bang(moTa, mongDoi, thucTe);
                }
            };
            real.KiemNgamTayGame.chay();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemNgamTayGame khong nem loi: " + e, false);
        }
        nguon();
    }

    static BanHuong.MucTieu mt(final int id, final int x, final int y) {
        return new BanHuong.MucTieu(id, 0, x, y, null);
    }

    static List<BanHuong.MucTieu> ds(final BanHuong.MucTieu... a) {
        final List<BanHuong.MucTieu> l = new ArrayList<BanHuong.MucTieu>();
        for (final BanHuong.MucTieu m : a) {
            l.add(m);
        }
        return l;
    }

    static String ids(final BanHuong.KetQua k) {
        final StringBuilder b = new StringBuilder();
        for (final BanHuong.Trung t : k.trung) {
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(t.mt.id);
        }
        return b.toString();
    }

    static final BanHuong.ThongSo DAN = new BanHuong.ThongSo(BanHuong.K_DAN, 120, 20, 0, 30, 1);
    static final BanHuong.ThongSo XUYEN = new BanHuong.ThongSo(BanHuong.K_XUYEN, 120, 24, 0, 30, 3);
    static final BanHuong.ThongSo NON = new BanHuong.ThongSo(BanHuong.K_NON, 80, 24, 0, 60, 4);
    static final BanHuong.ThongSo VUNG = new BanHuong.ThongSo(BanHuong.K_VUNG, 100, 64, 0, 360, 5);

    static void thongSo() {
        Kiem.dung("buff (lop 2 chieu 6) = tu than", BanHuong.laBuff(2, 6) && BanHuong.macDinh(2, 6, 100).kieu == BanHuong.K_TU_THAN);
        Kiem.sai("lop 0 chieu 6 khong la buff", BanHuong.laBuff(0, 6));
        Kiem.bang("lop danh xa, chieu 0 = dan", Integer.valueOf(BanHuong.K_DAN), Integer.valueOf(BanHuong.macDinh(4, 0, 90).kieu));
        Kiem.bang("lop can chien, chieu 0 = non", Integer.valueOf(BanHuong.K_NON), Integer.valueOf(BanHuong.macDinh(0, 0, 30).kieu));
        Kiem.bang("chieu 2 = tia xuyen", Integer.valueOf(BanHuong.K_XUYEN), Integer.valueOf(BanHuong.macDinh(1, 2, 60).kieu));
        Kiem.bang("chieu 3 = non", Integer.valueOf(BanHuong.K_NON), Integer.valueOf(BanHuong.macDinh(1, 3, 70).kieu));
        Kiem.bang("chieu 8 = vung", Integer.valueOf(BanHuong.K_VUNG), Integer.valueOf(BanHuong.macDinh(1, 8, 80).kieu));
        Kiem.bang("tam goc nho (20) duoc nang toi thieu 24", Integer.valueOf(24), Integer.valueOf(BanHuong.macDinh(3, 0, 20).tam));
        Kiem.bang("dan: toi da luon 1", Integer.valueOf(1), Integer.valueOf(new BanHuong.ThongSo(BanHuong.K_DAN, 100, 20, 400, 30, 9).toiDa));
        Kiem.bang("kep tam > 640", Integer.valueOf(640), Integer.valueOf(new BanHuong.ThongSo(BanHuong.K_DAN, 9999, 20, 400, 30, 1).tam));
        Kiem.bang("kieu sai -> dan", Integer.valueOf(BanHuong.K_DAN), Integer.valueOf(new BanHuong.ThongSo(77, 100, 20, 0, 30, 1).kieu));
        Kiem.bang("thoi gian bay 120 px / 480 px/s = 250 ms", Long.valueOf(250), Long.valueOf(new BanHuong.ThongSo(BanHuong.K_DAN, 120, 20, 480, 30, 1).msBay(120)));
        Kiem.bang("tuc thi: 0 ms", Long.valueOf(0), Long.valueOf(XUYEN.msBay(120)));
        Kiem.bang("non khong bay (0 ms)", Long.valueOf(0), Long.valueOf(new BanHuong.ThongSo(BanHuong.K_NON, 120, 20, 480, 30, 1).msBay(120)));
        Kiem.dung("huong 0 -> null", BanHuong.chuanHoa(0, 0) == null);
        Kiem.dung("huong NaN -> null", BanHuong.chuanHoa(Double.NaN, 1) == null);
    }

    static void dan() {
        // ban sang phai tu (100,100)
        final BanHuong.KetQua a = BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 100), mt(2, 160, 100)), null);
        Kiem.bang("dan: trung nguoi GAN nhat tren duong (2 truoc 1)", "2", ids(a));
        Kiem.bang("dan: khong co tuong -> tam that = tam", Integer.valueOf(120), Integer.valueOf(a.tamThat));
        Kiem.bang("dan: lech doc 19 px (nua rong 10 + than 10 = 20) -> trung", "1",
                ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 180, 119)), null)));
        Kiem.bang("dan: lech doc 21 px -> truot", "", ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 180, 121)), null)));
        Kiem.bang("dan: dung cuoi tam + than (230) -> trung", "1", ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 229, 100)), null)));
        Kiem.bang("dan: qua tam (232) -> truot", "", ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 232, 100)), null)));
        Kiem.bang("dan: phia SAU nguoi ban -> truot", "", ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 60, 100)), null)));
        Kiem.bang("dan: huong cheo (1,1) trung (170,170)", "1", ids(BanHuong.moPhong(DAN, 100, 100, 5, 5, 0, 0, ds(mt(1, 170, 170)), null)));
        Kiem.bang("dan: huong cheo khong trung muc tieu ngang (180,100)", "", ids(BanHuong.moPhong(DAN, 100, 100, 1, 1, 0, 0, ds(mt(1, 180, 100)), null)));
        Kiem.bang("dan: do dai vector khong doi ket qua (1000,0)", "2",
                ids(BanHuong.moPhong(DAN, 100, 100, 1000, 0, 0, 0, ds(mt(1, 200, 100), mt(2, 160, 100)), null)));
        Kiem.bang("dan: huong 0 -> khong ban", "", ids(BanHuong.moPhong(DAN, 100, 100, 0, 0, 0, 0, ds(mt(1, 150, 100)), null)));
        Kiem.bang("tu than: khong ban", "", ids(BanHuong.moPhong(new BanHuong.ThongSo(BanHuong.K_TU_THAN, 100, 20, 0, 30, 1), 100, 100, 1, 0, 0, 0,
                ds(mt(1, 150, 100)), null)));
    }

    static void xuyen() {
        final BanHuong.KetQua a = BanHuong.moPhong(XUYEN, 100, 100, 1, 0, 0, 0,
                ds(mt(1, 200, 100), mt(2, 130, 100), mt(3, 160, 105), mt(4, 190, 99), mt(5, 150, 160)), null);
        Kiem.bang("xuyen: trung moi nguoi tren duong theo thu tu, toi da 3", "2,3,4", ids(a));
        Kiem.bang("xuyen: nguoi lech xa (150,160) khong trung", Boolean.FALSE, Boolean.valueOf(a.trungId(5)));
    }

    static void non() {
        // non 60 do, tam 80, huong phai
        Kiem.bang("non: chinh giua", "1", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 160, 100)), null)));
        Kiem.bang("non: 25 do lech (trong 30) trung", "1", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0,
                ds(mt(1, 100 + (int) (60 * Math.cos(Math.toRadians(25))), 100 + (int) (60 * Math.sin(Math.toRadians(25))))), null)));
        Kiem.bang("non: 50 do lech (ngoai 30, xa mep > than) truot", "", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0,
                ds(mt(1, 100 + (int) (70 * Math.cos(Math.toRadians(50))), 100 + (int) (70 * Math.sin(Math.toRadians(50))))), null)));
        Kiem.bang("non: qua tam (95) truot", "", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 195, 100)), null)));
        Kiem.bang("non: sat nguoi ban (phia sau 5 px) trung", "1", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 95, 100)), null)));
        Kiem.bang("non: phia sau xa truot", "", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 50, 100)), null)));
        Kiem.bang("non: nhieu muc tieu, gan truoc", "2,1", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 170, 100), mt(2, 130, 110)), null)));
    }

    static void vung() {
        // vung o (200,100), duong kinh 64 -> ban kinh 32 + than 10 = 42
        Kiem.bang("vung: tam vung", "1", ids(BanHuong.moPhong(VUNG, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 100)), null)));
        Kiem.bang("vung: cach tam 41 trung", "1", ids(BanHuong.moPhong(VUNG, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 141)), null)));
        Kiem.bang("vung: cach tam 43 truot", "", ids(BanHuong.moPhong(VUNG, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 143)), null)));
        Kiem.bang("vung: nguoi giua duong (khong trong vung) truot", "", ids(BanHuong.moPhong(VUNG, 100, 100, 1, 0, 0, 0, ds(mt(1, 140, 100)), null)));
    }

    /** Tuong doc o cot x = 160..175 (o 10). */
    static final BanHuong.Tuong TUONG = new BanHuong.Tuong() {
        @Override
        public boolean chan(final int x, final int y) {
            return x >= 160 && x < 176;
        }
    };

    static void tuong() {
        final BanHuong.KetQua a = BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 100)), TUONG);
        Kiem.bang("tuong: dan dung truoc tuong (tam that 56)", Integer.valueOf(56), Integer.valueOf(a.tamThat));
        Kiem.bang("tuong: nguoi sau tuong khong trung", "", ids(a));
        Kiem.bang("tuong: nguoi truoc tuong van trung", "1", ids(BanHuong.moPhong(DAN, 100, 100, 1, 0, 0, 0, ds(mt(1, 140, 100)), TUONG)));
        Kiem.bang("tuong: tia xuyen cung khong xuyen tuong", "1",
                ids(BanHuong.moPhong(XUYEN, 100, 100, 1, 0, 0, 0, ds(mt(1, 140, 100), mt(2, 200, 100)), TUONG)));
        Kiem.bang("tuong: non khong trung nguoi sau tuong", "", ids(BanHuong.moPhong(NON, 100, 100, 1, 0, 0, 0, ds(mt(1, 175, 100)), TUONG)));
        Kiem.bang("tuong: vung roi truoc tuong (tam 56) -> khong trung sau tuong", "",
                ids(BanHuong.moPhong(VUNG, 100, 100, 1, 0, 0, 0, ds(mt(1, 200, 100)), TUONG)));
        Kiem.bang("tuong: ban nguoc chieu tuong khong bi chan", Integer.valueOf(120),
                Integer.valueOf(BanHuong.moPhong(DAN, 100, 100, -1, 0, 0, 0, ds(), TUONG).tamThat));
        Kiem.sai("thongDuong qua tuong", BanHuong.thongDuong(TUONG, 100, 100, 200, 100));
        Kiem.dung("thongDuong song song tuong", BanHuong.thongDuong(TUONG, 100, 100, 150, 140));
    }

    static void buTre() {
        // muc tieu dung o (200,100) toi t=1000, roi chay xuong (200,160) luc t=1100 (vi tri hien tai)
        final BanHuong.LichSu ls = new BanHuong.LichSu();
        ls.ghi(900, 200, 100);
        ls.ghi(1000, 200, 100);
        ls.ghi(1100, 200, 160);
        final BanHuong.ThongSo d = new BanHuong.ThongSo(BanHuong.K_DAN, 120, 20, 0, 30, 1);
        Kiem.bang("bu tre 150 ms: vi tri 1000 (trong cua so) -> trung", "1",
                ids(BanHuong.moPhong(d, 100, 100, 1, 0, 1100, 150, ds(new BanHuong.MucTieu(1, 0, 200, 160, ls)), null)));
        Kiem.bang("bu tre 0 ms: chi vi tri hien tai -> truot", "",
                ids(BanHuong.moPhong(d, 100, 100, 1, 0, 1100, 0, ds(new BanHuong.MucTieu(1, 0, 200, 160, ls)), null)));
        Kiem.bang("bu tre: ban luc 1300 (vi tri cu da qua 150 ms) -> truot", "",
                ids(BanHuong.moPhong(d, 100, 100, 1, 0, 1300, 150, ds(new BanHuong.MucTieu(1, 0, 200, 160, ls)), null)));
        Kiem.bang("bu tre kep tran 300: xin 5000 ms van khong lui toi 900", "",
                ids(BanHuong.moPhong(d, 100, 100, 1, 0, 1500, 5000, ds(new BanHuong.MucTieu(1, 0, 200, 160, ls)), null)));
        final int[] g = ls.luc(1050, 200, 160);
        Kiem.bang("noi suy giua hai mau", "200,130", g[0] + "," + g[1]);
        final BanHuong.LichSu l2 = new BanHuong.LichSu();
        l2.ghi(0, 100, 100);
        l2.ghi(5000, 110, 100); // dung yen lau roi moi buoc
        final int[] g2 = l2.luc(2500, 110, 100);
        Kiem.bang("dung yen lau: khong noi suy keo dai (vi tri 2500 = cho cu)", "100,100", g2[0] + "," + g2[1]);
        final BanHuong.LichSu l3 = new BanHuong.LichSu();
        l3.ghi(0, 100, 100);
        l3.ghi(100, 900, 100); // dich chuyen
        Kiem.bang("dich chuyen: xoa lich su (1 mau)", Integer.valueOf(1), Integer.valueOf(l3.soMau()));
        final BanHuong.LichSu l4 = new BanHuong.LichSu();
        for (int i = 0; i < 100; i++) {
            l4.ghi(i * 50, 100 + i, 100);
        }
        Kiem.bang("lich su vong 32 mau", Integer.valueOf(BanHuong.LichSu.SO_MAU), Integer.valueOf(l4.soMau()));
        Kiem.bang("huong di cuoi: sang phai", "1,0", l4.huongX + "," + l4.huongY);
    }

    static void danBay() {
        // dan 200 px/s, tam 120: toi x=200 (cach 100, than 10) sau ~460 ms.
        final BanHuong.ThongSo cham = new BanHuong.ThongSo(BanHuong.K_DAN, 120, 20, 200, 30, 1);
        final BanHuong.LichSu ne = new BanHuong.LichSu();
        ne.ghi(0, 200, 100);
        ne.ghi(150, 200, 100);
        ne.ghi(200, 200, 160); // ne luc 200 ms, dan toi ~460 ms: ca cua so bu tre [310, 460] da o cho moi
        Kiem.bang("dan cham: muc tieu ne kip (truoc khi dan toi) -> truot", "",
                ids(BanHuong.moPhong(cham, 100, 100, 1, 0, 0, 150, ds(new BanHuong.MucTieu(1, 0, 200, 160, ne)), null)));
        final BanHuong.LichSu dung = new BanHuong.LichSu();
        dung.ghi(0, 200, 100);
        Kiem.bang("dan cham: muc tieu dung yen -> trung", "1",
                ids(BanHuong.moPhong(cham, 100, 100, 1, 0, 0, 150, ds(new BanHuong.MucTieu(1, 0, 200, 100, dung)), null)));
        final BanHuong.KetQua k = BanHuong.moPhong(cham, 100, 100, 1, 0, 0, 150, ds(new BanHuong.MucTieu(1, 0, 200, 100, dung)), null);
        Kiem.dung("dan cham: thoi diem trung ~ 450-500 ms", k.trung.size() == 1 && k.trung.get(0).ms >= 440 && k.trung.get(0).ms <= 520);
        // muc tieu chay VAO duong dan sau khi ban (luc dau o ngoai)
        final BanHuong.LichSu vao = new BanHuong.LichSu();
        vao.ghi(0, 200, 160);
        vao.ghi(200, 200, 160);
        vao.ghi(260, 200, 100);
        Kiem.bang("dan cham: muc tieu chay vao duong dan truoc khi dan toi -> trung", "1",
                ids(BanHuong.moPhong(cham, 100, 100, 1, 0, 0, 0, ds(new BanHuong.MucTieu(1, 0, 200, 100, vao)), null)));
        // hai muc tieu: xa dung yen, gan chay vao sau khi dan da bay qua cho do -> trung xa
        final BanHuong.LichSu muon = new BanHuong.LichSu();
        muon.ghi(0, 130, 160);
        muon.ghi(400, 130, 160);
        muon.ghi(450, 130, 100);
        Kiem.bang("dan cham: chay vao sau luc dan da qua -> dan trung nguoi xa", "2",
                ids(BanHuong.moPhong(cham, 100, 100, 1, 0, 0, 0, ds(new BanHuong.MucTieu(1, 0, 130, 100, muon),
                        new BanHuong.MucTieu(2, 0, 200, 100, dung)), null)));
    }

    static void kiemBan() {
        Kiem.bang("hop le", null, BanHuong.kiemBan(1, 16, 3, false, 5000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("chieu ngoai mang", "chieu_sai", BanHuong.kiemBan(16, 16, 3, false, 5000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("chieu am", "chieu_sai", BanHuong.kiemBan(-1, 16, 3, false, 5000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("chua hoc", "chua_hoc", BanHuong.kiemBan(1, 16, 0, false, 5000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("buff khong ban", "buff", BanHuong.kiemBan(4, 16, 3, true, 5000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("da guc", "da_guc", BanHuong.kiemBan(1, 16, 3, false, 5000, 1000, 50, 10, false, false, 1000, 0));
        Kiem.bang("bi khong che", "bi_khong_che", BanHuong.kiemBan(1, 16, 3, false, 5000, 1000, 50, 10, true, true, 1000, 0));
        Kiem.bang("huong 0", "huong_0", BanHuong.kiemBan(1, 16, 3, false, 5000, 1000, 50, 10, true, false, 0, 0));
        Kiem.bang("con hoi chieu", "hoi_chieu", BanHuong.kiemBan(1, 16, 3, false, 999, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("vua het hoi chieu", null, BanHuong.kiemBan(1, 16, 3, false, 1000, 1000, 50, 10, true, false, 1000, 0));
        Kiem.bang("thieu PP", "thieu_pp", BanHuong.kiemBan(1, 16, 3, false, 5000, 1000, 9, 10, true, false, 1000, 0));
        Kiem.dung("goc lech 32 px dung duoc", BanHuong.gocHopLe(132, 100, 100, 100));
        Kiem.sai("goc lech 33 px khong dung", BanHuong.gocHopLe(133, 100, 100, 100));
    }

    static void spam() {
        final BanHuong.ChongSpam c = new BanHuong.ChongSpam();
        boolean bao = false;
        for (int i = 0; i < BanHuong.SPAM_SO - 1; i++) {
            bao |= c.tuChoi(1000 + i * 10);
        }
        Kiem.sai("24 goi bi tu choi trong 5 giay: chua nghi van", bao);
        Kiem.dung("goi thu 25: nghi van", c.tuChoi(1300));
        final BanHuong.ChongSpam c2 = new BanHuong.ChongSpam();
        boolean bao2 = false;
        for (int i = 0; i < 100; i++) {
            bao2 |= c2.tuChoi(i * 300L); // 1 goi / 300 ms = 17 trong 5 giay
        }
        Kiem.sai("goi tu choi thua (1 / 300 ms): khong nghi van", bao2);
        final BanHuong.ChongSpam c3 = new BanHuong.ChongSpam();
        Kiem.dung("phat dau cho qua", c3.choPhat(10_000));
        Kiem.sai("phat thu hai sau 50 ms (doi huong lien tuc): chan", c3.choPhat(10_050));
        Kiem.dung("phat sau 80 ms: cho", c3.choPhat(10_080));
    }

    static void huongCu() {
        Kiem.bang("client cu: muc tieu ben phai -> ban phai", "1000,0", s(BanHuong.huongBonPhia(100, 100, 150, 110)));
        Kiem.bang("client cu: muc tieu ben trai", "-1000,0", s(BanHuong.huongBonPhia(100, 100, 40, 90)));
        Kiem.bang("client cu: muc tieu phia duoi", "0,1000", s(BanHuong.huongBonPhia(100, 100, 110, 160)));
        Kiem.bang("client cu: muc tieu phia tren", "0,-1000", s(BanHuong.huongBonPhia(100, 100, 90, 20)));
        // cheo 45 do + lech: ban 4 phia khong khoa -> co the truot
        final BanHuong.MucTieu cheo = mt(1, 170, 160);
        final int[] h = BanHuong.huongBonPhia(100, 100, 170, 160);
        Kiem.bang("client cu: muc tieu cheo xa duong 4 phia -> truot (khong khoa)", "",
                ids(BanHuong.moPhong(DAN, 100, 100, h[0], h[1], 0, 0, ds(cheo), null)));
    }

    static String s(final int[] a) {
        return a[0] + "," + a[1];
    }

    static void nguon() {
        final String x = doc("src/real/MapXuLyLenh.java");
        final int iTd = x.indexOf("TuDongGame.kiemLenh(player, message.command)");
        final int iBh = x.indexOf("BanHuongGame.xuLy(map, player, message)");
        final int iSw = x.indexOf("switch (message.command)");
        Kiem.dung("MapXuLyLenh: BanHuongGame.xuLy truoc switch (sau nhip tu dong)", iTd > 0 && iBh > iTd && iBh < iSw);
        Kiem.bang("MapXuLyLenh: lenh danh quai 9 / 106 cung khoa nhan vat voi dan ngam tay", Integer.valueOf(2),
                Integer.valueOf(x.split("synchronized \\(BanHuongGame.khoa\\(player\\)\\)", -1).length - 1));
        Kiem.dung("CharCapNhat: ghi lich su vi tri sau khi nhan vi tri", doc("src/real/CharCapNhat.java").contains("BanHuongGame.ghiViTri(self, now)"));
        Kiem.dung("CharVongLap.update: nhip che do PvP", doc("src/real/CharVongLap.java").contains("BanHuongGame.nhip(self)"));
        Kiem.dung("NoiDungLauDai: nap data_chieu_ban", doc("src/real/NoiDungLauDai.java").contains("BanHuongGame.nap()"));
        Kiem.dung("RealController: -113 trong privateCmd (luong phien nhu lenh danh)", doc("src/real/RealController.java").contains("49, 50, 22, -113}"));
        final String t = doc("src/real/TuDongGame.java");
        Kiem.dung("TuDongGame: bat tu dong hoi BanHuongGame truoc", t.contains("BanHuongGame.lyDoCamTuDong(p); // 0.9.8"));
        Kiem.dung("TuDongGame: dang chay ma vao PvP thi dung", t.contains("} else if (BanHuongGame.lyDoCamTuDong(p) != null) {"));
        final String sql = doc("../sql/them_ngam_tay_pvp.sql");
        Kiem.dung("sql/them_ngam_tay_pvp.sql: CREATE TABLE IF NOT EXISTS + INSERT IGNORE, khong DROP",
                sql.contains("CREATE TABLE IF NOT EXISTS `data_chieu_ban`") && sql.contains("INSERT IGNORE") && !sql.toUpperCase().contains("DROP "));
        Kiem.bang("sql: 50 dong (5 lop x chieu 0..9)", Integer.valueOf(50), Integer.valueOf(sql.split("\n  \\(").length - 1));
        Kiem.dung("lenh -113 khong trung danh sach tranh", BanHuong.LENH == -113);
        final String dm = doc("../tools/cauhinh/danh_muc.json");
        Kiem.dung("danh muc cau hinh co pvp.ngam_tay*", dm.contains("\"pvp.ngam_tay\"") && dm.contains("\"pvp.ngam_tay.bu_tre_ms\"")
                && dm.contains("\"pvp.ngam_tay.giay_giao_chien\"") && dm.contains("\"pvp.ngam_tay.map\""));
    }
}
