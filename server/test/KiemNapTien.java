import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NapTien;
import com.htbb.pokemon.NapTienDb;
import com.htbb.pokemon.NapTienGame;
import com.htbb.pokemon.NapTienLogic;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nap tien khong lam phat / khong tra tien de manh hon (docs/pokemon/NAP_TIEN.md):
 *   - co sv.nap_tien: tat = hanh vi cu, chi bat khi ca sv.lau_dai;
 *   - goi ty gia co dinh, khong khuyen mai %; danh muc SQL khop Java (+ web);
 *   - cong don idempotent (goi lap, 16 luong song song), so cai UNIQUE;
 *   - tran nap tuan/thang, tu dat tran; hoan tien / no; the thang; ve mua (ngan sach vat lieu);
 *   - cua hang KHONG ban do co chi so (whitelist + tu cam), cho trang tri (thue, bien gia);
 *   - moc noi (hook) trong file game co mat va deu re nhanh qua NapTien.bat();
 *   - mo phong: 6 muc tieu nap tien (chenh suc manh <= 5%, xu khong doi, luong bi dot...).
 *
 * Phan CSDL that (cong don song song, goi lap webhook, cho) chi chay khi dat bien moi truong
 * KIEM_NAP_DB = URL JDBC toi mot CSDL NHAP (vd jdbc:mysql://127.0.0.1:3318/htbb_db_nap?useSSL=false,
 * kem KIEM_NAP_DB_USER / KIEM_NAP_DB_PASS): test tao bang tu sql/pokemon_nap_tien.sql roi XOA sau.
 */
public final class KiemNapTien {

    private KiemNapTien() {
    }

    public static void chay() {
        final boolean cuLauDai = LauDai.bat();
        final boolean cuNap = NapTien.coDat();
        try {
            co();
            goi();
            trangThai();
            congIdempotent();
            tran();
            hoanTien();
            theThang();
            veMua();
            cuaHang();
            cho();
            maDon();
            mocNoi();
            sqlVaWeb();
            csdl();
            moPhong();
        } finally {
            LauDai.dat(cuLauDai);
            NapTien.dat(cuNap);
        }
    }

    // ------------------------------------------------------------------ co

    static void co() {
        Kiem.nhom("Nap tien - co sv.nap_tien (tat = hanh vi cu, chi bat khi ca sv.lau_dai)");
        Kiem.sai("thieu dong = tat", NapTien.docGiaTri(null));
        Kiem.sai("'0' = tat", NapTien.docGiaTri("0"));
        Kiem.dung("' 1 ' = bat", NapTien.docGiaTri(" 1 "));
        NapTien.dat(false);
        LauDai.dat(true);
        Kiem.sai("sv.nap_tien=0 -> tat", NapTien.bat());
        NapTien.dat(true);
        LauDai.dat(false);
        Kiem.sai("sv.nap_tien=1 nhung sv.lau_dai=0 -> VAN TAT (fail closed)", NapTien.bat());
        Kiem.dung("canh bao khi thieu sv.lau_dai", NapTien.moTaTrangThai().contains("CANH BAO"));
        LauDai.dat(true);
        Kiem.dung("ca hai co -> bat", NapTien.bat());
        Kiem.bang("bat: gia doi ten = gia cua hang", NapTienLogic.GIA_DOI_TEN, NapTienGame.giaDoiTen(100000));

        // Tat co: moi moc noi tra ve hanh vi cu.
        NapTien.dat(false);
        final String[] menu = {"a", "b"};
        Kiem.dung("tat: menu NPC -10 y nguyen (cung mang)", NapTienGame.noiMenuTongQuan(null, menu) == menu);
        Kiem.sai("tat: MapDoMenu khong bat menu nao", NapTienGame.xuLyMenu(null, -540, 0, 0));
        Kiem.bang("tat: gia doi ten cu", 100000, NapTienGame.giaDoiTen(100000));
        Kiem.bang("tat: khong chan doi ten", null, NapTienGame.kiemDoiTen(null));
        Kiem.bang("tat: khong them ban kinh nhat", 0, NapTienGame.banKinhNhatThemPx(null));
        Kiem.bang("tat: khong luot ham nguc them", null, NapTienGame.luotHamNgucThem(null));
        Kiem.bang("tat: khong cong diem ve mua", 0, NapTienGame.congDiemVeMua(null, 50, "test"));
    }

    // ------------------------------------------------------------------ goi

    static void goi() {
        Kiem.nhom("Nap tien - goi: ty gia co dinh 10 luong / 1.000 d, khong khuyen mai, khong ngau nhien");
        for (final NapTienLogic.Goi g : NapTienLogic.GOI_MAC_DINH) {
            Kiem.bang("goi " + g.ma + " hop le", null, NapTienLogic.kiemGoi(g.loai, g.giaVnd, g.luong));
            if (NapTienLogic.LOAI_LUONG.equals(g.loai)) {
                Kiem.bang("goi " + g.ma + " = gia x 10 / 1000", g.giaVnd / 100, g.luong);
            }
        }
        Kiem.dung("goi luong co khuyen mai (+25%) bi tu choi", NapTienLogic.kiemGoi("luong", 20000, 250) != null);
        Kiem.dung("goi luong thieu (sai ty gia) bi tu choi", NapTienLogic.kiemGoi("luong", 20000, 150) != null);
        Kiem.dung("the thang kem luong tuc thi bi tu choi", NapTienLogic.kiemGoi("the_thang", 50000, 100) != null);
        Kiem.dung("loai la (vd 'vip', 'hop_qua') bi tu choi", NapTienLogic.kiemGoi("vip", 50000, 0) != null);
        Kiem.dung("gia le nghin bi tu choi", NapTienLogic.kiemGoi("luong", 20500, 205) != null);
        Kiem.dung("gia qua cao (> 2 trieu / don) bi tu choi", NapTienLogic.kiemGoi("luong", 3000000, 30000) != null);
        Kiem.bang("5 muc goi luong 20k/50k/100k/200k/500k", 5, demLoai(NapTienLogic.LOAI_LUONG));
        Kiem.bang("1 the thang", 1, demLoai(NapTienLogic.LOAI_THE_THANG));
        Kiem.bang("1 ve mua", 1, demLoai(NapTienLogic.LOAI_VE_MUA));
    }

    static int demLoai(final String loai) {
        int n = 0;
        for (final NapTienLogic.Goi g : NapTienLogic.GOI_MAC_DINH) {
            n += loai.equals(g.loai) ? 1 : 0;
        }
        return n;
    }

    // ------------------------------------------------------------------ trang thai

    static void trangThai() {
        Kiem.nhom("Nap tien - may trang thai don");
        final String[][] dung = {
            {"cho_thanh_toan", "da_thanh_toan"}, {"cho_thanh_toan", "that_bai"}, {"that_bai", "da_thanh_toan"},
            {"da_thanh_toan", "da_cong"}, {"da_thanh_toan", "hoan_tien"}, {"da_cong", "cho_thu_hoi"},
            {"cho_thu_hoi", "da_thu_hoi"},
        };
        for (final String[] d : dung) {
            Kiem.dung(d[0] + " -> " + d[1], NapTienLogic.chuyenHopLe(d[0], d[1]));
        }
        final String[][] sai = {
            {"cho_thanh_toan", "da_cong"}, {"da_cong", "da_thanh_toan"}, {"da_cong", "da_cong"},
            {"hoan_tien", "da_thanh_toan"}, {"da_thu_hoi", "da_cong"}, {"da_thanh_toan", "da_thanh_toan"},
            {"that_bai", "da_cong"}, {null, "da_cong"},
        };
        for (final String[] d : sai) {
            Kiem.sai("cam " + d[0] + " -> " + d[1], NapTienLogic.chuyenHopLe(d[0], d[1]));
        }
        Kiem.bang("so cai: khoa cong theo ma don", "cong:HTABCDEFGH", NapTienLogic.thamChieuCong("HTABCDEFGH"));
    }

    // ------------------------------------------------------------------ cong idempotent

    static void congIdempotent() {
        Kiem.nhom("Nap tien - cong don idempotent (goi lap / song song)");
        final String[] tt = {NapTienLogic.DA_THANH_TOAN};
        final long[] du = {0L};
        Kiem.bang("lan 1 cong 1000", 1000, NapTienLogic.congDonThuan(tt, du, 1000));
        Kiem.bang("lan 2 (callback lap) cong 0", 0, NapTienLogic.congDonThuan(tt, du, 1000));
        Kiem.bang("so du 1000", 1000L, du[0]);
        Kiem.bang("trang thai da_cong", NapTienLogic.DA_CONG, tt[0]);
        final String[] tt2 = {NapTienLogic.CHO_THANH_TOAN};
        Kiem.bang("don chua thanh toan khong cong", 0, NapTienLogic.congDonThuan(tt2, du, 500));
        // 16 luong cung luc.
        final String[] tt3 = {NapTienLogic.DA_THANH_TOAN};
        final long[] du3 = {0L};
        final AtomicInteger lanCong = new AtomicInteger();
        final CountDownLatch bd = new CountDownLatch(1);
        final List<Thread> ds = new ArrayList<Thread>();
        for (int i = 0; i < 16; i++) {
            final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bd.await();
                    } catch (final InterruptedException ignored) {
                    }
                    if (NapTienLogic.congDonThuan(tt3, du3, 2000) > 0) {
                        lanCong.incrementAndGet();
                    }
                }
            });
            t.start();
            ds.add(t);
        }
        bd.countDown();
        for (final Thread t : ds) {
            try {
                t.join();
            } catch (final InterruptedException ignored) {
            }
        }
        Kiem.bang("16 luong song song: dung 1 lan cong", 1, lanCong.get());
        Kiem.bang("16 luong song song: so du 2000", 2000L, du3[0]);
    }

    // ------------------------------------------------------------------ tran

    static void tran() {
        Kiem.nhom("Nap tien - tran nap tuan/thang (bao ve nguoi choi)");
        Kiem.bang("duoi tran", null, NapTienLogic.kiemTran(0, 0, 500000, 0, 0, 0));
        Kiem.dung("vuot tran tuan 1 trieu", NapTienLogic.kiemTran(600000, 600000, 500000, 0, 0, 0) != null);
        Kiem.dung("vuot tran thang 2 trieu (tuan moi)", NapTienLogic.kiemTran(0, 1800000, 500000, 0, 0, 0) != null);
        Kiem.bang("dung bang tran thang", null, NapTienLogic.kiemTran(0, 1500000, 500000, 0, 0, 0));
        Kiem.dung("tu dat tran 100k: don 200k bi chan", NapTienLogic.kiemTran(0, 0, 200000, 0, 0, 100000) != null);
        Kiem.dung("tu dat tran thap hon ca tran tuan", NapTienLogic.kiemTran(0, 50000, 60000, 0, 0, 100000) != null);
        Kiem.bang("cau hinh 99 trieu bi kep ve tran cung 5 trieu", NapTienLogic.TRAN_CUNG_THANG_VND,
                NapTienLogic.kepTran(99000000L, NapTienLogic.TRAN_THANG_VND));
        Kiem.bang("cau hinh 0 -> mac dinh", NapTienLogic.TRAN_THANG_VND, NapTienLogic.kepTran(0, NapTienLogic.TRAN_THANG_VND));
        Kiem.dung("so tien am / 0 bi tu choi", NapTienLogic.kiemTran(0, 0, 0, 0, 0, 0) != null);
    }

    // ------------------------------------------------------------------ hoan tien / no

    static void hoanTien() {
        Kiem.nhom("Nap tien - hoan tien / chargeback: thu hoi + no");
        Kiem.bang("thu hoi khi du: tru het, khong no", Arrays.toString(new long[]{1000, 0}),
                Arrays.toString(NapTienLogic.thuHoi(5000, 1000)));
        Kiem.bang("thu hoi khi thieu (da tieu): tru 300, no 700", Arrays.toString(new long[]{300, 700}),
                Arrays.toString(NapTienLogic.thuHoi(300, 1000)));
        Kiem.bang("nap moi khi dang no 700: tra no 700, cong 300", Arrays.toString(new long[]{700, 300}),
                Arrays.toString(NapTienLogic.congCoNo(700, 1000)));
        Kiem.bang("khong no: cong du", Arrays.toString(new long[]{0, 1000}), Arrays.toString(NapTienLogic.congCoNo(0, 1000)));
        Kiem.bang("so am khong lam am so du", Arrays.toString(new long[]{0, 0}), Arrays.toString(NapTienLogic.thuHoi(-5, -5)));
        final long t = 1000000000000L, ngay = 86400000L;
        Kiem.bang("thu hoi the 50 ngay: con 20 ngay", t + 20 * ngay, NapTienLogic.thuHoiTheThang(t + 50 * ngay, t));
        Kiem.bang("thu hoi the 10 ngay: het ngay bay gio (khong am)", t, NapTienLogic.thuHoiTheThang(t + 10 * ngay, t));
        Kiem.bang("the da het: giu nguyen", t - ngay, NapTienLogic.thuHoiTheThang(t - ngay, t));
    }

    // ------------------------------------------------------------------ the thang

    static void theThang() {
        Kiem.nhom("Nap tien - the thang: 30 ngay, 20 luong/ngay, khong nhan bu, toi da 2 thang chong");
        final long t = 1000000000000L, ngay = 86400000L;
        Kiem.bang("mua moi: han = +30 ngay", t + 30 * ngay, NapTienLogic.giaHanTheThang(0, t));
        Kiem.bang("mua them khi con 30 ngay: +30 (=60)", t + 60 * ngay, NapTienLogic.giaHanTheThang(t + 30 * ngay, t));
        Kiem.bang("mua them khi con 60 ngay: tu choi", -1L, NapTienLogic.giaHanTheThang(t + 60 * ngay, t));
        Kiem.dung("con han, chua nhan hom nay -> nhan", NapTienLogic.coTheNhanNgay(t + ngay, t, "2026-10-16", "2026-10-17"));
        Kiem.sai("da nhan hom nay -> khong", NapTienLogic.coTheNhanNgay(t + ngay, t, "2026-10-17", "2026-10-17"));
        Kiem.sai("het han -> khong", NapTienLogic.coTheNhanNgay(t - 1, t, "", "2026-10-17"));
        Kiem.bang("tong luong the = 600 (ngang goi 50k + 20%, chi mua trang tri)", 600,
                NapTienLogic.THE_THANG_SO_NGAY * NapTienLogic.THE_THANG_LUONG_NGAY);
        Kiem.bang("luot ham nguc them chi cho diem ve mua", 20, NapTienLogic.THE_THANG_DIEM_VE_MUA_LUOT_THEM);
    }

    // ------------------------------------------------------------------ ve mua

    static void veMua() {
        Kiem.nhom("Nap tien - ve mua: tien do chi bang choi, vat lieu cao cap = free, khong chi so");
        Kiem.bang("mua ve 0 ngay 0", 0, NapTienLogic.muaVe(0));
        Kiem.bang("mua ve 1 ngay 56", 1, NapTienLogic.muaVe(56));
        Kiem.bang("con 56 ngay ngay 0", 56, NapTienLogic.ngayConLaiMuaVe(0));
        Kiem.bang("bac 0 voi 99 diem", 0, NapTienLogic.bacTuDiem(99));
        Kiem.bang("bac 30 toi da", 30, NapTienLogic.bacTuDiem(999999));
        Kiem.bang("tran diem ngay: 100 da co + 50 -> 20", 20, NapTienLogic.diemDuocCong(100, 50, 120));
        Kiem.bang("da het tran -> 0", 0, NapTienLogic.diemDuocCong(120, 50, 120));
        final int free = NapTienLogic.tongDaTrack(false), cao = NapTienLogic.tongDaTrack(true);
        Kiem.bang("da nang cap track cao cap = track free (cung ngan sach)", free, cao);
        Kiem.dung("da/mua <= 30 vien (~3,5% da cua nguoi choi vua trong 56 ngay)", free <= 30);
        boolean sach = true;
        int diemFree = 0, diemCao = 0;
        for (final boolean caoCap : new boolean[]{false, true}) {
            for (int b = 1; b <= NapTienLogic.VE_MUA_SO_BAC; b++) {
                final String t = NapTienLogic.thuongBac(caoCap, b);
                boolean tg = false;
                if (t.matches("TG:[a-z0-9_]+:[0-9]+")) {
                    // tang toc: CHI ma trong TangToc.MA, trao ban KHOA (ID_KHOA) - chung tran diem ngay/tuan
                    final int loai = com.htbb.pokemon.TangToc.loaiCuaMa(t.split(":")[1]);
                    tg = loai >= 0 && Integer.parseInt(t.split(":")[2]) <= 2;
                    if (caoCap) {
                        diemCao += loai >= 0 ? com.htbb.pokemon.TangToc.DIEM[loai] * Integer.parseInt(t.split(":")[2]) : 0;
                    } else {
                        diemFree += loai >= 0 ? com.htbb.pokemon.TangToc.DIEM[loai] * Integer.parseInt(t.split(":")[2]) : 0;
                    }
                }
                sach &= t.isEmpty() || t.matches("DA:[0-9]+") || t.matches("TT:[a-z0-9_]+") || tg;
            }
        }
        Kiem.dung("thuong ve mua chi la DA (khoa), TT (trang tri) hoac TG (tang toc ban KHOA, chung tran) - khong xu/luong/EXP truc tiep/do chi so", sach);
        Kiem.dung("track mien phi co tang toc ban khoa >= track cao cap (nguoi khong nap kiem duoc tuong duong): free "
                + diemFree + " / cao cap " + diemCao + " diem", diemFree >= diemCao && diemFree > 0);
        Kiem.dung("tong diem tang toc ca 2 track / mua (56 ngay) << tran tuan (khong vuot tran)",
                diemFree + diemCao <= com.htbb.pokemon.TangToc.DIEM_TUAN_TOI_DA);
        Kiem.dung("ca 30 bac cao cap deu co thuong", NapTienLogic.thuongBac(true, 1).length() > 0
                && NapTienLogic.thuongBac(true, 30).length() > 0);
    }

    // ------------------------------------------------------------------ cua hang

    static void cuaHang() {
        Kiem.nhom("Nap tien - cua hang luong KHONG ban do co chi so");
        for (final NapTienLogic.Mon m : NapTienLogic.MON_MAC_DINH) {
            Kiem.bang("mon " + m.ma + " hop le", null, NapTienLogic.kiemMon(m));
        }
        final String[][] xau = {
            {"ao_chi_so", "Áo choàng +10% tấn công", "skin", "trang_tri"},
            {"ve_x2", "Vé x2 EXP 60 phút", "tien_ich", "trang_tri"},
            {"bua", "Bùa bảo hộ cường hoá", "tien_ich", "trang_tri"},
            {"da_may_man", "Đá may mắn", "tien_ich", "trang_tri"},
            {"ruong", "Rương bí ẩn", "skin", "trang_tri"},
            {"do_that", "Kiếm huyền thoại", "skin", "item"},
            {"exp", "Kẹo", "tien_ich", "exp"},
            {"xu", "Túi xu", "tien_ich", "xu"},
        };
        for (final String[] x : xau) {
            final NapTienLogic.Mon m = new NapTienLogic.Mon(x[0], x[1], x[2], x[3], 100, 0, 0, 0, false);
            Kiem.dung("tu choi: " + x[1] + " (trao " + x[3] + ")", NapTienLogic.kiemMon(m) != null);
        }
        Kiem.dung("tu choi co_chi_so = 1 du ten vo hai", NapTienLogic.kiemMon(
                new NapTienLogic.Mon("mu", "Mũ đẹp", "skin", "trang_tri", 100, 0, 0, 0, false, true)) != null);
        Kiem.dung("tu choi mon han ngay ma giao dich duoc", NapTienLogic.kiemMon(
                new NapTienLogic.Mon("mau_x", "Màu tên", "mau_ten", "trang_tri", 100, 30, 0, 0, true)) != null);
        Kiem.dung("tu choi tien ich giao dich duoc", NapTienLogic.kiemMon(
                new NapTienLogic.Mon("tui", "Túi", "tien_ich", "mo_rong_tui", 300, 0, 0, 0, true)) != null);
        Kiem.dung("tu choi gia 1 luong / 99999 luong", NapTienLogic.kiemMon(
                new NapTienLogic.Mon("re", "Rẻ", "skin", "trang_tri", 1, 0, 0, 0, false)) != null
                && NapTienLogic.kiemMon(new NapTienLogic.Mon("dat", "Đắt", "skin", "trang_tri", 99999, 0, 0, 0, false)) != null);
        Kiem.dung("'hp' trong 'Hà Phương' khong bi nham la tu cam (khop ca tu)",
                NapTienLogic.kiemMon(new NapTienLogic.Mon("hp_ten", "Khung tên Hà Phương", "khung", "trang_tri", 100, 0, 0, 0, false)) == null);
        // Xoay vong 4 tuan.
        final List<NapTienLogic.Mon> ds = Arrays.asList(NapTienLogic.MON_MAC_DINH);
        int moiTuanItNhat = Integer.MAX_VALUE;
        for (int tuan = 0; tuan < 4; tuan++) {
            moiTuanItNhat = Math.min(moiTuanItNhat, NapTienLogic.monDangBan(ds, tuan).size());
        }
        Kiem.dung("moi tuan co hang (luon ban + 1 nhom xoay)", moiTuanItNhat >= 6);
        Kiem.dung("tuan 0 va tuan 1 khac hang (xoay vong)",
                !names(NapTienLogic.monDangBan(ds, 0)).equals(names(NapTienLogic.monDangBan(ds, 1))));
        Kiem.bang("xoay vong lap lai sau 4 tuan", names(NapTienLogic.monDangBan(ds, 1)), names(NapTienLogic.monDangBan(ds, 5)));
        Kiem.bang("doi ten: chua doi bao gio -> 0", 0, NapTienLogic.ngayChoDoiTen(0, 1000));
        Kiem.bang("doi ten: vua doi -> cho 30 ngay", 30, NapTienLogic.ngayChoDoiTen(1000, 1000));
        Kiem.bang("tuan doi luc thu Hai (ngay epoch 20745 = thu Hai 2026-10-19)", NapTienLogic.tuanTuNgayEpoch(20744) + 1,
                NapTienLogic.tuanTuNgayEpoch(20745));
        Kiem.bang("thu Hai -> Chu nhat cung tuan", NapTienLogic.tuanTuNgayEpoch(20745), NapTienLogic.tuanTuNgayEpoch(20751));
    }

    static String names(final List<NapTienLogic.Mon> ds) {
        final StringBuilder b = new StringBuilder();
        for (final NapTienLogic.Mon m : ds) {
            b.append(m.ma).append(',');
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ cho

    static void cho() {
        Kiem.nhom("Nap tien - cho trang tri: thue 6% + phi dang 1% (dot), bien gia 50-200% trung vi");
        Kiem.bang("thue 6% cua 10.000", 600L, NapTienLogic.thueBan(10000));
        Kiem.bang("phi dang 1% cua 10.000", 100L, NapTienLogic.phiDang(10000));
        Kiem.bang("phi dang toi thieu 1 xu", 1L, NapTienLogic.phiDang(10));
        Kiem.bang("chua du 3 giao dich: gia nao cung duoc", null, NapTienLogic.kiemGiaRao(999999, Arrays.asList(1000L, 1000L)));
        final List<Long> gd = Arrays.asList(1000L, 1200L, 900L, 1100L, 1000L);
        Kiem.bang("trung vi 1000", 1000L, NapTienLogic.trungVi(gd));
        Kiem.bang("gia 1500 trong bien", null, NapTienLogic.kiemGiaRao(1500, gd));
        Kiem.dung("gia 2500 (> 200%) bi chan - chong rua tien / RMT gia ao", NapTienLogic.kiemGiaRao(2500, gd) != null);
        Kiem.dung("gia 400 (< 50%) bi chan", NapTienLogic.kiemGiaRao(400, gd) != null);
        Kiem.dung("gia 0 bi chan", NapTienLogic.kiemGiaRao(0, null) != null);
    }

    // ------------------------------------------------------------------ ma don

    static void maDon() {
        Kiem.nhom("Nap tien - tim ma don trong noi dung chuyen khoan");
        Kiem.bang("noi dung chuan", "HTABCDEFGH", NapTienLogic.timMaDon("HTABCDEFGH"));
        Kiem.bang("ngan hang chen chu truoc/sau, chu thuong", "HT7F3AQ2BX",
                NapTienLogic.timMaDon("MBVCB.123 ht7f3aq2bx chuyen tien"));
        Kiem.bang("ky tu de nham (0/O/1/I) khong phai ma", null, NapTienLogic.timMaDon("HTO0I1ABCD"));
        Kiem.bang("dinh lien chu khac khong nhan", null, NapTienLogic.timMaDon("XHTABCDEFGH"));
        Kiem.bang("rong", null, NapTienLogic.timMaDon(null));
    }

    // ------------------------------------------------------------------ moc noi

    static void mocNoi() {
        Kiem.nhom("Nap tien - moc noi trong file game (deu qua NapTien.bat / NapTienGame)");
        final String ch = doc("src/real/Char.java");
        Kiem.bang("Char: subLuong + subLuongTang dot luong khi bat (2 cho)", 2, dem(ch, "com.htbb.pokemon.NapTien.ghiTieu(l);"));
        Kiem.dung("TeamServer doc sv.nap_tien", doc("src/server/TeamServer.java").contains("p.getProperty(\"sv.nap_tien\")"));
        Kiem.dung("TeamServer khoi dong vong nap", doc("src/server/TeamServer.java").contains("NapTienGame.batDau()"));
        Kiem.dung("NPC -10 noi muc Cua hang & Nap",
                doc("src/real/cmd/RequestNPCInfoHandler.java").contains("NapTienGame.noiMenuTongQuan(p, menu)"));
        Kiem.dung("MapDoMenu re nhanh menu nap", doc("src/real/MapDoMenu.java").contains("NapTienGame.xuLyMenu(player, idNpc, idMenu, idOptionMenu)"));
        final String bn = doc("src/data/DatabaseXepHang.java");
        Kiem.dung("TileNap bi bo qua khi bat (ty gia co dinh)", bn.contains("if (com.htbb.pokemon.NapTien.bat()) {\n                    tile = 1;"));
        Kiem.dung("khong tich diem nap VIP khi bat", bn.contains("if (!com.htbb.pokemon.NapTien.bat()) {\n                player.luongNap"));
        Kiem.bang("chuyen luong truc tiep bi chan khi bat (3 cua vao)", 3,
                dem(doc("src/real/CharChuyenLuong.java"), "if (chanViNapTien(player)) {"));
        Kiem.dung("doi ten dung gia + gioi han cua nap", doc("src/real/CharDoiTenNhanVat.java").contains("NapTienGame.giaDoiTen(COST_LUONG)"));
        Kiem.dung("ham nguc: luot them chi diem ve mua", doc("src/real/HamNgucGame.java").contains("NapTienGame.luotHamNgucThem(p)"));
        Kiem.dung("nhat do: ban kinh them cua the thang", doc("src/real/Map.java").contains("NapTienGame.banKinhNhatThemPx(player)"));
        // Co che cu lam phat da tat qua sv.lau_dai (NapTien.bat() doi ca lau_dai).
        Kiem.dung("moc nap cu (luong + trung/phuong hoang) chan khi lau_dai", doc("src/real/MapQuaTang.java")
                .contains("static void grantMocNapReward(Map map, Char player, int moc, int phoenixChoice) {\n"
                        + "        // GD2 2.11 (Q10): khong co nap -> khong co qua moc nap khi bat co sv.lau_dai.\n"
                        + "        if (com.htbb.pokemon.LauDai.bat()) {"));
        Kiem.dung("doi diem xai luong -> xu (MapSuKien) chan khi lau_dai",
                doc("src/real/MapSuKien.java").contains("Bat co sv.lau_dai: tat (THIET_KE 2.1 bo hoan xu khi tieu luong)."));
        Kiem.dung("luong roi tu quai tat khi lau_dai", doc("src/real/Monster.java").contains("boolean DropLuong = !com.htbb.pokemon.LauDai.bat();"));
        final String tmpl = doc("../vps/server.ini.tmpl");
        Kiem.dung("server.ini.tmpl: sv.nap_tien=0 (mac dinh tat)", tmpl.contains("\nsv.nap_tien=0\n"));
        // Moi mon ma code trao chi qua whitelist: khong goi applyItemString / AddItem (do co chi so).
        final String game = doc("src/com/htbb/pokemon/NapTienGame.java");
        Kiem.sai("NapTienGame khong trao do co chi so (applyItemString / GiftCode.AddItem / createVuKhi)",
                game.contains("applyItemString") || game.contains("AddItem(") || game.contains("createVuKhi"));
        Kiem.sai("NapTienGame khong cong xu tu luong (addXu chi o cho/hoan phi)",
                Pattern.compile("addXu\\([^)]*luong").matcher(game).find());
        Kiem.bang("NapTienGame chi addLuong o 2 cho (cong don, the thang)", 2, dem(game, "p.addLuong((int) tn[1])")
                + dem(game, "p.addLuong(cong)"));
    }

    static int dem(final String s, final String tu) {
        int n = 0;
        int i = s.indexOf(tu);
        while (i >= 0) {
            n++;
            i = s.indexOf(tu, i + 1);
        }
        return n;
    }

    // ------------------------------------------------------------------ sql + web

    static void sqlVaWeb() {
        Kiem.nhom("Nap tien - sql/pokemon_nap_tien.sql + web khop NapTienLogic");
        final String s = doc("../sql/pokemon_nap_tien.sql");
        Kiem.dung("SET NAMES utf8", s.contains("SET NAMES utf8;"));
        final String lenh = s.replaceAll("(?m)--.*$", "");
        Kiem.sai("khong ON DUPLICATE KEY (NovaDB)", lenh.contains("ON DUPLICATE KEY"));
        Kiem.sai("khong CREATE TABLE .. LIKE", Pattern.compile("(?i)CREATE\\s+TABLE[^(]*\\bLIKE\\b").matcher(lenh).find());
        Kiem.sai("khong NOW()/CURDATE() (moc thoi gian do ung dung ghi)", lenh.contains("NOW()") || lenh.contains("CURDATE()"));
        Kiem.sai("khong ghi de chinh sua admin (khong REPLACE/DELETE)", Pattern.compile("(?m)^(REPLACE|DELETE|UPDATE)").matcher(lenh).find());
        final Matcher g = Pattern.compile("\\('(\\w+)','([^']*)','(\\w+)',(\\d+),(\\d+),").matcher(s.substring(s.indexOf("INSERT IGNORE INTO `nap_goi`")));
        final List<String> sqlGoi = new ArrayList<String>();
        while (g.find() && sqlGoi.size() < 20) {
            sqlGoi.add(g.group(1) + "|" + g.group(2) + "|" + g.group(3) + "|" + g.group(4) + "|" + g.group(5));
        }
        final List<String> javaGoi = new ArrayList<String>();
        for (final NapTienLogic.Goi x : NapTienLogic.GOI_MAC_DINH) {
            javaGoi.add(x.ma + "|" + x.ten + "|" + x.loai + "|" + x.giaVnd + "|" + x.luong);
        }
        Kiem.bang("nap_goi trong SQL = GOI_MAC_DINH", javaGoi.toString(), sqlGoi.subList(0, Math.min(sqlGoi.size(), javaGoi.size())).toString());
        final Matcher m = Pattern.compile("\\('([a-z0-9_]+)','([^']*)','(\\w+)','(\\w+)',(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\)")
                .matcher(s);
        final List<String> sqlMon = new ArrayList<String>();
        while (m.find()) {
            sqlMon.add(m.group(1) + "|" + m.group(2) + "|" + m.group(3) + "|" + m.group(4) + "|" + m.group(5) + "|" + m.group(6)
                    + "|" + m.group(7) + "|" + m.group(8) + "|" + m.group(9) + "|" + m.group(10));
        }
        final List<String> javaMon = new ArrayList<String>();
        for (final NapTienLogic.Mon x : NapTienLogic.MON_MAC_DINH) {
            javaMon.add(x.ma + "|" + x.ten + "|" + x.nhom + "|" + x.trao + "|" + x.giaLuong + "|" + x.hanNgay + "|" + x.gioiHan
                    + "|" + x.nhomXoay + "|" + (x.giaoDichDuoc ? 1 : 0) + "|" + (x.coChiSo ? 1 : 0));
        }
        Kiem.bang("nap_cua_hang trong SQL = MON_MAC_DINH", javaMon.toString(), sqlMon.toString());
        Kiem.dung("xuat_sql_day_du.sh gom pokemon_nap_tien", doc("../tools/pokemon/xuat_sql_day_du.sh").contains("pokemon_nap_tien"));

        final File web = new File("../web/server/utils/napTienThuan.ts");
        if (!web.isFile()) {
            System.out.println("   (bo qua doi chieu web: khong thay ../HTBB-Web)");
            return;
        }
        final String ts = doc(web.getPath());
        Kiem.dung("web: ty gia 10 luong / 1.000 d", ts.contains("LUONG_MOI_1000_VND = " + NapTienLogic.LUONG_MOI_1000_VND));
        Kiem.dung("web: tran tuan", ts.contains("TRAN_TUAN_VND = " + NapTienLogic.TRAN_TUAN_VND));
        Kiem.dung("web: tran thang", ts.contains("TRAN_THANG_VND = " + NapTienLogic.TRAN_THANG_VND));
        Kiem.dung("web: tran cung", ts.contains("TRAN_CUNG_THANG_VND = " + NapTienLogic.TRAN_CUNG_THANG_VND));
        Kiem.dung("web: whitelist cach trao giong server", ts.contains("'trang_tri', 'mo_rong_tui', 'doi_ten', 'tang_toc'"));
        Kiem.dung("web: bang chu ma don giong server", ts.contains("'" + NapTienLogic.KY_TU_MA + "'"));
        Kiem.dung("web: trang thai don giong server", ts.contains("'cho_thu_hoi'") && ts.contains("'da_thu_hoi'") && ts.contains("'da_cong'"));
    }

    // ------------------------------------------------------------------ csdl that

    static void csdl() {
        final String url = System.getenv("KIEM_NAP_DB");
        if (url == null || url.trim().isEmpty()) {
            System.out.println();
            System.out.println("== Nap tien - CSDL that (bo qua: dat KIEM_NAP_DB de chay)");
            return;
        }
        Kiem.nhom("Nap tien - CSDL that (" + url.replaceAll("password=[^&]*", "password=***") + ")");
        final String user = System.getenv("KIEM_NAP_DB_USER") == null ? "root" : System.getenv("KIEM_NAP_DB_USER");
        final String pass = System.getenv("KIEM_NAP_DB_PASS") == null ? "" : System.getenv("KIEM_NAP_DB_PASS");
        NapTienDb.datNguon(new NapTienDb.Nguon() {
            @Override
            public Connection lay() throws SQLException {
                return DriverManager.getConnection(url, user, pass);
            }

            @Override
            public void tra(final Connection c) {
                try {
                    if (c != null) {
                        c.close();
                    }
                } catch (final SQLException ignored) {
                }
            }
        });
        try {
            chayKichBan(url, user, pass);
        } catch (final Exception e) {
            Kiem.bang("CSDL khong loi", "", String.valueOf(e));
        } finally {
            try {
                xoaBang(url, user, pass);
            } catch (final Exception ignored) {
            }
        }
    }

    static final String[] BANG = {"nap_goi", "nap_don", "nap_vi", "nap_so_cai", "nap_nhat_ky", "nap_webhook", "nap_cau_hinh",
        "nap_cua_hang", "nap_so_huu", "nap_cho", "nap_the_thang", "nap_ve_mua", "nap_mua_dem"};

    static long dem1(final Statement st, final String sql) throws SQLException {
        try (java.sql.ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : -1L;
        }
    }

    static void xoaBang(final String url, final String u, final String p) throws Exception {
        try (Connection c = DriverManager.getConnection(url, u, p); Statement st = c.createStatement()) {
            for (final String b : BANG) {
                st.execute("DROP TABLE IF EXISTS `" + b + "`");
            }
        }
    }

    static void chayKichBan(final String url, final String u, final String p) throws Exception {
        xoaBang(url, u, p);
        final String sql = doc("../sql/pokemon_nap_tien.sql").replaceAll("(?m)^--.*$", "");
        try (Connection c = DriverManager.getConnection(url, u, p); Statement st = c.createStatement()) {
            for (final String cau : sql.split(";\\s*\\n")) {
                if (!cau.trim().isEmpty()) {
                    st.execute(cau);
                }
            }
            // chay lai lan 2: khong loi, khong nhan doi du lieu.
            for (final String cau : sql.split(";\\s*\\n")) {
                if (!cau.trim().isEmpty()) {
                    st.execute(cau);
                }
            }
            Kiem.bang("chay SQL 2 lan: 7 goi (INSERT IGNORE)", 7L, dem1(st, "SELECT COUNT(*) FROM nap_goi"));
            Kiem.bang("chay SQL 2 lan: 22 mon cua hang", 22L, dem1(st, "SELECT COUNT(*) FROM nap_cua_hang"));
            Kiem.bang("13 bang", 13, BANG.length);
            st.execute("INSERT INTO nap_don (ma_don, user_id, char_id, ma_goi, loai, so_tien, luong, trang_thai, tao_luc)"
                    + " VALUES ('HTAAAAAAAA', 7, 70, 'L100', 'luong', 100000, 1000, 'da_thanh_toan', '2026-10-17 10:00:00')");
            st.execute("INSERT INTO nap_don (ma_don, user_id, char_id, ma_goi, loai, so_tien, luong, trang_thai, tao_luc)"
                    + " VALUES ('HTBBBBBBBB', 7, 70, 'L20', 'luong', 20000, 200, 'cho_thanh_toan', '2026-10-17 10:00:00')");
            st.execute("INSERT INTO nap_cua_hang (ma, ten, nhom, trao, gia_luong, co_chi_so) VALUES"
                    + " ('lau_chi_so','Áo đẹp','skin','trang_tri',100,1), ('lau_item','Kiếm','skin','item',100,0)");
        }
        Kiem.dung("coBang", NapTienDb.coBang());
        final List<NapTienDb.Don> don = NapTienDb.donChoCong(10);
        Kiem.bang("chi don da_thanh_toan cho cong", "HTAAAAAAAA", don.isEmpty() ? "" : don.get(0).maDon);
        final long id = don.get(0).id;
        // 8 luong cung chot da_thanh_toan -> da_cong.
        final AtomicInteger thang = new AtomicInteger();
        final CountDownLatch bd = new CountDownLatch(1);
        final List<Thread> ds = new ArrayList<Thread>();
        for (int i = 0; i < 8; i++) {
            final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bd.await();
                        if (NapTienDb.danhDauDaCong(id, 70)) {
                            thang.incrementAndGet();
                        }
                    } catch (final Exception ignored) {
                    }
                }
            });
            t.start();
            ds.add(t);
        }
        bd.countDown();
        for (final Thread t : ds) {
            t.join();
        }
        Kiem.bang("8 luong chot cong cung luc: dung 1 thang", 1, thang.get());
        Kiem.sai("chot lai lan nua: khong", NapTienDb.danhDauDaCong(id, 70));
        Kiem.dung("so cai lan 1 ghi", NapTienDb.ghiSoCai(7, 70, "cong", 1000, 100000, "HTAAAAAAAA", "cong:HTAAAAAAAA", "t"));
        Kiem.sai("so cai lan 2 (cung tham chieu) bi chan", NapTienDb.ghiSoCai(7, 70, "cong", 1000, 100000, "HTAAAAAAAA",
                "cong:HTAAAAAAAA", "t"));
        Kiem.bang("khong con don cho cong", 0, NapTienDb.donChoCong(10).size());
        // Goi lap webhook: cung ma giao dich ben cong -> UNIQUE.
        boolean trung = false;
        try (Connection c = DriverManager.getConnection(url, u, p); Statement st = c.createStatement()) {
            st.execute("INSERT INTO nap_webhook (nha_cung_cap, ma_gd, tao_luc) VALUES ('sepay', 'GD1', '2026-10-17 10:00:00')");
            try {
                st.execute("INSERT INTO nap_webhook (nha_cung_cap, ma_gd, tao_luc) VALUES ('sepay', 'GD1', '2026-10-17 10:00:01')");
            } catch (final SQLException e) {
                trung = true;
            }
            st.execute("UPDATE nap_don SET nha_cung_cap='sepay', ma_gd_ncc='GD9' WHERE ma_don='HTAAAAAAAA'");
            boolean trungDon = false;
            try {
                st.execute("UPDATE nap_don SET nha_cung_cap='sepay', ma_gd_ncc='GD9' WHERE ma_don='HTBBBBBBBB'");
            } catch (final SQLException e) {
                trungDon = true;
            }
            Kiem.dung("mot giao dich ngan hang khong xac nhan duoc 2 don (UNIQUE ncc+ma_gd)", trungDon);
        }
        Kiem.dung("webhook goi lap (cung ma giao dich) bi UNIQUE chan", trung);
        // Thuong lan dau.
        Kiem.dung("thuong lan dau lan 1", NapTienDb.nhanThuongDau(7));
        Kiem.sai("thuong lan dau lan 2", NapTienDb.nhanThuongDau(7));
        // No / thu hoi.
        NapTienDb.congNo(7, 300);
        Kiem.bang("no 300", 300L, NapTienDb.noLuong(7));
        Kiem.sai("tru no 500 (> no) bi tu choi", NapTienDb.truNo(7, 500));
        Kiem.dung("tru no 300", NapTienDb.truNo(7, 300));
        Kiem.bang("het no", 0L, NapTienDb.noLuong(7));
        try (Connection c = DriverManager.getConnection(url, u, p); Statement st = c.createStatement()) {
            st.execute("UPDATE nap_don SET trang_thai='cho_thu_hoi' WHERE ma_don='HTAAAAAAAA' AND trang_thai='da_cong'");
        }
        Kiem.bang("don cho thu hoi", 1, NapTienDb.donChoThuHoi(10).size());
        Kiem.dung("chot thu hoi lan 1", NapTienDb.danhDauDaThuHoi(id));
        Kiem.sai("chot thu hoi lan 2", NapTienDb.danhDauDaThuHoi(id));
        // The thang.
        final long t = System.currentTimeMillis();
        Kiem.dung("gia han the", NapTienDb.doiHanThe(70, 7, 0L, t + 30L * 86400000L));
        Kiem.sai("gia han voi han cu sai (2 don cung luc) bi tu choi", NapTienDb.doiHanThe(70, 7, 0L, t + 60L * 86400000L));
        Kiem.dung("nhan luong the hom nay", NapTienDb.nhanNgay(70, "2026-10-17", t));
        Kiem.sai("nhan lan 2 cung ngay", NapTienDb.nhanNgay(70, "2026-10-17", t));
        Kiem.sai("het han thi khong nhan", NapTienDb.nhanNgay(70, "2026-10-18", t + 31L * 86400000L));
        // Ve mua.
        Kiem.bang("diem ve: tran ngay 120", 120, NapTienDb.congDiem(70, 0, 200, "2026-10-17", 120));
        Kiem.bang("diem ve: het tran", 0, NapTienDb.congDiem(70, 0, 5, "2026-10-17", 120));
        Kiem.bang("diem ve: ngay moi", 5, NapTienDb.congDiem(70, 0, 5, "2026-10-18", 120));
        Kiem.dung("nhan bac 0 -> 1", NapTienDb.nhanBac(70, 0, false, 0, 1));
        Kiem.sai("nhan bac lap (cu sai)", NapTienDb.nhanBac(70, 0, false, 0, 1));
        // Cua hang: bo dong co chi so / cach trao la.
        boolean coXau = false;
        for (final NapTienLogic.Mon m : NapTienDb.cuaHang()) {
            coXau |= m.ma.startsWith("lau_");
        }
        Kiem.sai("cua hang bo qua dong co_chi_so=1 va trao='item' admin chen vao", coXau);
        Kiem.dung("dem mua lan 1", NapTienDb.ghiMua(70, "skin_mu_rom", 0, t));
        Kiem.sai("dem mua bam dup (so lan cu sai)", NapTienDb.ghiMua(70, "skin_mu_rom", 0, t));
        // Cho.
        final long sh = NapTienDb.themSoHuu(70, "skin_mu_rom", "mua", true, 0L);
        final long shKhoa = NapTienDb.themSoHuu(70, "khung_nguoi_ung_ho", "lan_dau", false, 0L);
        Kiem.dung("id so huu tra ve", sh > 0 && shKhoa > sh);
        Kiem.sai("mon khong giao dich duoc khong rao duoc", NapTienDb.rao(shKhoa, 70, "a", "khung_nguoi_ung_ho", 1000, 10));
        Kiem.dung("rao mon giao dich duoc", NapTienDb.rao(sh, 70, "a", "skin_mu_rom", 1000, 10));
        Kiem.sai("rao lai mon dang rao", NapTienDb.rao(sh, 70, "a", "skin_mu_rom", 1000, 10));
        final List<NapTienDb.Rao> r = NapTienDb.raoTheoMa("skin_mu_rom");
        Kiem.bang("1 dong rao", 1, r.size());
        Kiem.sai("nguoi ban tu mua cua minh", NapTienDb.chotMua(r.get(0).id, 70, 60));
        Kiem.dung("nguoi mua 1 thang", NapTienDb.chotMua(r.get(0).id, 80, 60));
        Kiem.sai("nguoi mua 2 cham chan", NapTienDb.chotMua(r.get(0).id, 90, 60));
        NapTienDb.chuyenSoHuu(r.get(0).soHuuId, 80);
        Kiem.bang("so huu sang nguoi mua", 1, NapTienDb.soHuu(80, t).size());
        Kiem.bang("nguoi ban cho nhan xu", 1, NapTienDb.chuaTra(70).size());
        Kiem.dung("tra xu nguoi ban lan 1", NapTienDb.chotTra(r.get(0).id));
        Kiem.sai("tra xu lan 2", NapTienDb.chotTra(r.get(0).id));
        Kiem.bang("gia gan day", "[1000]", NapTienDb.giaGanDay("skin_mu_rom").toString());
    }

    // ------------------------------------------------------------------ mo phong

    static void moPhong() {
        Kiem.nhom("Nap tien - mo phong (tools/pokemon/MoPhongKinhTe.java, mo-dun NAP TIEN)");
        final javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.out.println("   (bo qua mo phong: JVM khong co javac)");
            return;
        }
        try {
            final File ra = Files.createTempDirectory("mophongnap").toFile();
            final int kq = jc.run(null, null, null, "-encoding", "UTF-8", "-nowarn", "-cp",
                    System.getProperty("java.class.path"), "-d", ra.getPath(), "../tools/pokemon/MoPhongKinhTe.java");
            Kiem.bang("bien dich MoPhongKinhTe", 0, kq);
            if (kq != 0) {
                return;
            }
            try (URLClassLoader cl = new URLClassLoader(new URL[]{ra.toURI().toURL()}, KiemNapTien.class.getClassLoader())) {
                final Method m = cl.loadClass("MoPhongKinhTe").getMethod("chayKiemNapTien", String.class, int.class, int.class);
                final String[][] r = (String[][]) m.invoke(null, "..", 730, 25);
                Kiem.bang("6 muc tieu nap tien", 6, r.length);
                for (final String[] x : r) {
                    Kiem.bang("mo phong: " + x[0] + " (" + x[2] + ")", "1", x[1]);
                }
            }
        } catch (final Exception e) {
            Kiem.bang("mo phong chay khong loi", "", String.valueOf(e));
        }
    }

    // ------------------------------------------------------------------ tien ich

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
