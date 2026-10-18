import com.htbb.chongspam.ChongSpam;
import com.htbb.chongspam.ThongBaoGop;
import com.htbb.log.GioiHanLap;
import com.htbb.log.Nhat;
import io.BoDemGoiTin;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Kiem thu nhat ky (com.htbb.log) + chong spam (com.htbb.chongspam) + nguong tra cuu cua
 * io.BoDemGoiTin.
 *
 * Thoi gian truyen tu ngoai vao o moi cho co the - khong ngu giay nao.
 *
 * Dieu quan trong nhat can chac:
 *   - dong lap bi GOP chu khong MAT: luon co dong tom tat "xN lan";
 *   - dong LOI khong bao gio bi tran loai chan, stack chi in day du lan dau;
 *   - noi dung nguoi choi khong the gia mao dong log (xuong dong bi lam sach);
 *   - nguoi choi binh thuong khong bi chan chat, ke spam bi chan + cam tang dan.
 */
public final class KiemNhatKyChongSpam {

    public static void chay() {
        Kiem.nhom("com.htbb.log + com.htbb.chongspam - nhat ky va chong spam");
        gioiHanLap();
        mauVaLamSach();
        phanLoaiDongCu();
        duongDiNhat();
        stackTrace();
        chatNhip();
        chatTrungLapQuangCao();
        camTangDan();
        theGioiVaThaoTac();
        thongBaoGop();
        boDemTraCuu();
        cauHinhCoTaiLieu();
    }

    private static void gioiHanLap() {
        final GioiHanLap g = new GioiHanLap(5, 60000L, 100);
        int qua = 0;
        for (int i = 0; i < 100; i++) {
            if (g.choPhep("k", 1000L + i)) {
                qua++;
            }
        }
        Kiem.bang("cung khoa: chi 5/100 dong qua trong mot cua so", Integer.valueOf(5), Integer.valueOf(qua));
        Kiem.bang("dem dung 95 dong bi bo", Integer.valueOf(95), Integer.valueOf(g.dangBo("k")));
        Kiem.dung("khoa khac khong bi anh huong", g.choPhep("khac", 1500L));
        Kiem.bang("chua het cua so thi chua co tom tat", Integer.valueOf(0), Integer.valueOf(g.tomTat(30000L).size()));
        final List<GioiHanLap.TomTat> tt = g.tomTat(61001L);
        Kiem.bang("het cua so: dung 1 dong tom tat (khoa 'khac' khong bo dong nao)", Integer.valueOf(1), Integer.valueOf(tt.size()));
        Kiem.dung("tom tat ghi tong 100 lan, bo 95", tt.size() == 1 && tt.get(0).toString().startsWith("x100 lan trong 60s (bo bot 95)"));
        Kiem.dung("sang cua so moi lai duoc ghi", g.choPhep("k", 70000L));
        final GioiHanLap khongGioiHan = new GioiHanLap(0, 60000L, 100);
        boolean tatCa = true;
        for (int i = 0; i < 1000; i++) {
            tatCa &= khongGioiHan.choPhep("k", 0L);
        }
        Kiem.dung("toiDa=0 = khong gioi han", tatCa);
        final GioiHanLap nho = new GioiHanLap(1, 60000L, 16);
        for (int i = 0; i < 40; i++) {
            nho.choPhep("k" + i, 0L);
        }
        Kiem.dung("bang khoa bi chan tren (khong ro ri bo nho)", nho.soKhoa() <= 16);
        boolean coTran = false;
        for (final GioiHanLap.TomTat t : nho.tomTat(61000L)) {
            coTran |= t.khoa.contains("tran bang");
        }
        Kiem.dung("tran bang khoa van de lai dong tom tat", coTran);
    }

    private static void mauVaLamSach() {
        Kiem.bang("mau: so -> #, chuoi nhay don -> ?",
                "update tob_char_luong set nPK=# where charname=?",
                Nhat.mau("update tob_char_luong set nPK=17 where charname='abc'"));
        Kiem.dung("mau: hai cau SQL khac ten/so la cung mau",
                Nhat.mau("info VKTT: -1, -1, -1, 1").equals(Nhat.mau("info VKTT: -12, -3, -1, 7")));
        Kiem.dung("mau: tu co chu so (ten nhk001, id) -> # nen moi nguoi choi cung mau",
                Nhat.mau("[CLIENT] nhk001 ip=1.2.3.4 ver=2.7.6").equals(Nhat.mau("[CLIENT] abc9 ip=10.0.0.12 ver=3.0.1")));
        Kiem.bang("mau: giu chu tieng Viet", "Đã nạp lại cấu hình", Nhat.mau("Đã nạp lại cấu hình"));
        Kiem.bang("lam sach: xuong dong thanh chu \\n", "abc\\nFAKE LOG", Nhat.sach("abc\nFAKE LOG"));
        Kiem.bang("lam sach: \\r va ESC (ma mau ANSI)", "a\\rb?[31m", Nhat.sach("a\rb\u001b[31m"));
        Kiem.bang("lam sach: U+2028 (xuong dong unicode)", "a?b", Nhat.sach("a b"));
        Kiem.bang("lam sach: tieng Viet giu nguyen", "Chúc mừng Lão Đại", Nhat.sach("Chúc mừng Lão Đại"));
        final StringBuilder dai = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            dai.append('x');
        }
        Kiem.dung("lam sach: cat chuoi qua dai", Nhat.sach(dai.toString()).length() < 2100);
        Kiem.bang("lam sach: null", "null", Nhat.sach(null));
    }

    private static void phanLoaiDongCu() {
        Kiem.bang("[GOI3] -> loai GOI3", "GOI3/THONG_TIN", Nhat.phanLoai("[GOI3] abc khung=1", false).toString());
        Kiem.bang("[LAU DAI] -> LAU_DAI", "LAU_DAI/THONG_TIN", Nhat.phanLoai("[LAU DAI] engine nhiem vu: 1368 mau", false).toString());
        Kiem.bang("'. [CLIENT]' (tien to print rong) -> CLIENT", "CLIENT/THONG_TIN",
                Nhat.phanLoai(". [CLIENT] abc ip=1.2.3.4", false).toString());
        Kiem.bang("cau SQL tran -> SQL GO_LOI", "SQL/GO_LOI",
                Nhat.phanLoai("update tob_char_luong set nPK=0 where charname='x'", false).toString());
        Kiem.bang("info VKTT -> VKTT GO_LOI", "VKTT/GO_LOI", Nhat.phanLoai("info VKTT: -1, -1, -1, 1", false).toString());
        Kiem.bang("SAVE CHARSELL -> LUU_CHAR", "LUU_CHAR/GO_LOI", Nhat.phanLoai("SAVE CHARSELL", false).toString());
        Kiem.bang("stdout co exception -> CANH_BAO", "CU/CANH_BAO",
                Nhat.phanLoai("Error in doGetItem: java.lang.NullPointerException", false).toString());
        Kiem.bang("stderr co loi -> LOI", "ERR/LOI", Nhat.phanLoai("khong ghi duoc tep x", true).toString());
        Kiem.bang("SQL ERROR -> SQL_LOI (khong bi tat cung SQL go loi)", "SQL_LOI/LOI", Nhat.phanLoai("SQL ERROR=drop x", false).toString());
        Kiem.bang("[LAU DAI] SQLite error -> LOI muc canh bao (stdout)", "LAU_DAI/CANH_BAO",
                Nhat.phanLoai("[LAU DAI] chua doc duoc (SELECT ...): SQLite error: no such table", false).toString());
        Kiem.bang("dong thuong", "CU/THONG_TIN", Nhat.phanLoai("Listen on port 19129", false).toString());
    }

    private static List<String> batGhi() {
        final List<String> ra = new ArrayList<String>();
        Nhat.datDauRaKiemThu(new Consumer<String>() {
            @Override
            public void accept(final String s) {
                synchronized (ra) {
                    ra.add(s);
                }
            }
        });
        return ra;
    }

    private static int dem(final List<String> ds, final String chua) {
        int n = 0;
        for (final String s : ds) {
            if (s.contains(chua)) {
                n++;
            }
        }
        return n;
    }

    private static void duongDiNhat() {
        Nhat.datLaiChoKiemThu(null);
        final List<String> ra = batGhi();
        try {
            for (int i = 0; i < 300; i++) {
                Nhat.giaLapDongCu("info VKTT: -1, -1, -1, " + i, false);
                Nhat.giaLapDongCu("[GOI3] nv" + i + " id=1", false);
            }
            Kiem.bang("loai tat mac dinh (VKTT, GOI3): khong ra dong nao", Integer.valueOf(0), Integer.valueOf(ra.size()));

            for (int i = 0; i < 300; i++) {
                Nhat.giaLapDongCu("[CLIENT] nhk" + i + " ip=127.0.0.1 platf=nokia", false);
            }
            Kiem.bang("300 dong cung mau -> 5 dong", Integer.valueOf(5), Integer.valueOf(dem(ra, "[CLIENT]")));
            ra.clear();
            Nhat.tomTatKiemThu(System.currentTimeMillis() + 61000L);
            Kiem.dung("het cua so -> dong gop 'x300 lan' (khong mat dau vet)", dem(ra, "x300 lan") == 1);

            ra.clear();
            for (int i = 0; i < 400; i++) {
                Nhat.thongTin("BAN_DO", "nhan vat " + (char) ('a' + i % 26) + (char) ('a' + i / 26) + " vao map");
            }
            Kiem.bang("400 dong KHAC mau cung loai -> tran loai 120", Integer.valueOf(120), Integer.valueOf(dem(ra, "[BAN_DO]")));

            ra.clear();
            for (int i = 0; i < 400; i++) {
                Nhat.loi("CSDL", "loi " + (char) ('a' + i % 26) + (char) ('a' + i / 26));
            }
            Kiem.bang("dong LOI khong bi tran loai chan (400 mau khac nhau -> 400)", Integer.valueOf(400),
                    Integer.valueOf(dem(ra, "[CSDL]")));

            ra.clear();
            Nhat.thongTin("CHAT", "ten\n10-18 00:00:00.000 E [ADMIN] gia mao");
            Kiem.bang("noi dung co xuong dong van la MOT dong", Integer.valueOf(1), Integer.valueOf(ra.size()));
            Kiem.dung("xuong dong da bi lam sach", ra.size() == 1 && !ra.get(0).contains("\n") && ra.get(0).contains("\\n"));

            ra.clear();
            final Properties p = new Properties();
            p.setProperty("sv.log.tat", "ADMIN,CU");
            Nhat.datLaiChoKiemThu(p);
            for (int i = 0; i < 50; i++) {
                Nhat.kiemToan("ADMIN", "lenh slv " + i);
            }
            Kiem.bang("kiem toan: 50 dong -> du 50 dong vao tep kiem toan (khong gop, khong tat duoc)",
                    Integer.valueOf(50), Integer.valueOf(dem(ra, "KIEM_TOAN ")));
            ra.clear();
            Nhat.giaLapDongCu("Listen on port 1", false);
            Kiem.bang("sv.log.tat=CU tat dong cu khong tien to", Integer.valueOf(0), Integer.valueOf(ra.size()));

            ra.clear();
            final Properties p2 = new Properties();
            p2.setProperty("sv.log.muc", "CANH_BAO");
            Nhat.datLaiChoKiemThu(p2);
            ra.clear();
            Nhat.thongTin("X", "thong tin");
            Nhat.canhBao("X", "canh bao");
            Nhat.loi("X", "loi");
            Kiem.bang("muc CANH_BAO: bo THONG_TIN, giu CANH_BAO + LOI", Integer.valueOf(2), Integer.valueOf(ra.size()));
            Kiem.dung("muc doc duoc ten tieng Anh", Nhat.Muc.doc("warn", Nhat.Muc.THONG_TIN) == Nhat.Muc.CANH_BAO);
            Kiem.dung("muc sai -> mac dinh", Nhat.Muc.doc("xyz", Nhat.Muc.GO_LOI) == Nhat.Muc.GO_LOI);
        } finally {
            Nhat.datDauRaKiemThu(null);
            Nhat.datLaiChoKiemThu(null);
        }
    }

    private static void stackTrace() {
        Nhat.datLaiChoKiemThu(null);
        final List<String> ra = batGhi();
        try {
            final String[] khoi = {
                "java.lang.NullPointerException",
                "\tat real.Map.doChat(Map.java:100)",
                "\tat real.MapXuLyLenh.processMessage(MapXuLyLenh.java:9)",
                "Caused by: java.io.IOException: x",
                "\t... 3 more",
            };
            for (int i = 0; i < 50; i++) {
                Nhat.giaLapNhieuDongCu(khoi, true);
            }
            Kiem.bang("stack lap 50 lan: chi MOT ban day du", Integer.valueOf(1), Integer.valueOf(dem(ra, "\tat real.Map.doChat")));
            Kiem.dung("ca khoi stack la MOT dong ra (khong vo thanh 5 dong)",
                    dem(ra, "Caused by: java.io.IOException") == 1);
            Kiem.dung("lan lap: mot dong ngan, gop theo mau (<= 5)", dem(ra, "stack da in") <= 5 && dem(ra, "stack da in") >= 1);
            ra.clear();
            Nhat.tomTatKiemThu(System.currentTimeMillis() + 601000L);
            Kiem.dung("cuoi chu ky: dong 'loi lap lai 50 lan'", dem(ra, "loi lap lai 50 lan") == 1);

            ra.clear();
            final Exception e = new IllegalStateException("hong 123");
            for (int i = 0; i < 10; i++) {
                Nhat.loi("XU_LY_GOI", "lenh 27 cua nv" + i, e);
            }
            Kiem.bang("Nhat.loi(..., Throwable): stack day du 1 lan", Integer.valueOf(1),
                    Integer.valueOf(dem(ra, "at KiemNhatKyChongSpam.stackTrace")));

            ra.clear();
            Nhat.giaLapNhieuDongCu(new String[] {"Xin chao", "\tat khong.phai.Stack(X.java:1)"}, false);
            Kiem.bang("dong thuong + dong '\\tat' mo coi: van ra du 2 dong", Integer.valueOf(2), Integer.valueOf(ra.size()));
        } finally {
            Nhat.datDauRaKiemThu(null);
            Nhat.datLaiChoKiemThu(null);
        }
    }

    private static void chatNhip() {
        ChongSpam.datLaiChoKiemThu();
        final ChongSpam.TrangThai t = new ChongSpam.TrangThai();
        long now = 1000000L;
        int qua = 0;
        for (int i = 0; i < 8; i++) {
            if (ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "tin so " + (char) ('a' + i), now + i * 100L) == ChongSpam.KetQua.CHO_PHEP) {
                qua++;
            }
        }
        Kiem.bang("chat gan: 5 tin / 10 giay qua, tin 6+ bi chan", Integer.valueOf(5), Integer.valueOf(qua));
        Kiem.bang("tin thu 9 trong cua so: QUA_NHANH hoac DANG_CAM", Boolean.TRUE,
                Boolean.valueOf(ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "z", now + 900L) != ChongSpam.KetQua.CHO_PHEP));

        final ChongSpam.TrangThai binhThuong = new ChongSpam.TrangThai();
        int chan = 0;
        for (int i = 0; i < 60; i++) {
            // Nguoi choi that: mot tin moi 3 giay, noi dung khac nhau, trong 3 phut.
            if (ChongSpam.kiemChat(binhThuong, ChongSpam.Kenh.GAN, "cau thu " + i + " " + (char) ('a' + i % 26),
                    now + i * 3000L) != ChongSpam.KetQua.CHO_PHEP) {
                chan++;
            }
        }
        Kiem.bang("nguoi choi binh thuong (1 tin / 3 giay): khong bi chan lan nao", Integer.valueOf(0), Integer.valueOf(chan));

        final ChongSpam.TrangThai cuaSo = new ChongSpam.TrangThai();
        for (int i = 0; i < 5; i++) {
            ChongSpam.kiemChat(cuaSo, ChongSpam.Kenh.GAN, "x" + (char) ('a' + i), now);
        }
        Kiem.dung("het 10 giay thi chat lai duoc",
                ChongSpam.kiemChat(cuaSo, ChongSpam.Kenh.GAN, "moi", now + 10000L) == ChongSpam.KetQua.CHO_PHEP);
        Kiem.dung("kenh rieng dem doc lap voi kenh gan",
                ChongSpam.kiemChat(cuaSo, ChongSpam.Kenh.RIENG, "rieng", now + 10001L) == ChongSpam.KetQua.CHO_PHEP);
    }

    private static void chatTrungLapQuangCao() {
        ChongSpam.datLaiChoKiemThu();
        final ChongSpam.TrangThai t = new ChongSpam.TrangThai();
        final long now = 5000000L;
        Kiem.dung("tin dau qua", ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "Mua đồ không?", now) == ChongSpam.KetQua.CHO_PHEP);
        Kiem.bang("cung tin (khac hoa thuong / dau / ky tu lap) trong 30s -> TRUNG_LAP", ChongSpam.KetQua.TRUNG_LAP,
                ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "MUA DO KHONGGGG ???", now + 5000L));
        Kiem.dung("tin khac thi qua", ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "ai di boss", now + 6000L) == ChongSpam.KetQua.CHO_PHEP);
        Kiem.dung("chuan hoa bo dau + ky tu lap", ChongSpam.chuanHoa("Xin  CHÀOOO!!").equals(ChongSpam.chuanHoa("xin chao")));

        final String[] quangCao = {
            "vao http://abc.xyz nhan qua", "www.game-lau.net", "shop ban acc taikhoan.vn gia re",
            "ib zalo 0912 345 678", "web abc (dot) com", "vao xyz chấm com", "server moi 103.45.67.89",
            "lien he 0987654321", "t.me/banacc",
        };
        for (final String q : quangCao) {
            Kiem.dung("quang cao bi bat: " + q, ChongSpam.laQuangCao(q));
        }
        final String[] sach = {
            "chào cả nhà", "lên cấp 10.5 rồi", "boss 3h30 nhé", "đánh 12345 dame", "ai đi Johto không",
            "ok.", "pokemon.go", "x2 exp 20:00",
        };
        for (final String s : sach) {
            Kiem.sai("KHONG bat nham: " + s, ChongSpam.laQuangCao(s));
        }
        final ChongSpam.TrangThai qc = new ChongSpam.TrangThai();
        Kiem.bang("tin quang cao bi chan", ChongSpam.KetQua.QUANG_CAO,
                ChongSpam.kiemChat(qc, ChongSpam.Kenh.THE_GIOI, "vao http://x.com", now));
        final Properties p = new Properties();
        p.setProperty("sv.spam.quangCaoThem", "bán acc,cày thuê");
        ChongSpam.docCauHinh(p);
        Kiem.dung("sv.spam.quangCaoThem: tu them, so khong dau", ChongSpam.laQuangCao("ai can CAY THUE ib"));
        ChongSpam.datLaiChoKiemThu();
        Kiem.sai("dat lai cau hinh thi tu them het tac dung", ChongSpam.laQuangCao("ai can CAY THUE ib"));
    }

    private static void camTangDan() {
        ChongSpam.datLaiChoKiemThu();
        final ChongSpam.TrangThai t = new ChongSpam.TrangThai();
        long now = 9000000L;
        ChongSpam.KetQua kq = null;
        for (int i = 0; i < 4; i++) {
            kq = ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "spam spam", now + i);
        }
        // tin 1 qua; tin 2,3,4 trung lap = 3 vi pham -> chua cam
        Kiem.dung("3 vi pham chua bi cam", t.conCam(now + 10) == 0L);
        ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "spam spam", now + 10);
        Kiem.dung("vi pham thu 4 -> bi cam", t.conCam(now + 10) > 0L);
        Kiem.bang("lan cam dau: 60 giay", Long.valueOf(60000L - 0L), Long.valueOf(t.conCam(now + 10)));
        Kiem.bang("dang cam thi chat bi DANG_CAM", ChongSpam.KetQua.DANG_CAM,
                ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "tin moi", now + 20000L));
        Kiem.dung("ly do bao bang tieng Viet", ChongSpam.lyDo(ChongSpam.KetQua.DANG_CAM, t, now + 20000L).contains("cấm chat"));
        now += 61000L;
        Kiem.dung("het cam thi chat lai duoc", ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "da het cam", now) == ChongSpam.KetQua.CHO_PHEP);
        ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "vao http://a.com", now + 1);
        ChongSpam.kiemChat(t, ChongSpam.Kenh.GAN, "vao http://b.com", now + 2);
        Kiem.bang("lan cam thu hai (2 quang cao = 4 diem): 300 giay", Long.valueOf(300000L), Long.valueOf(t.conCam(now + 2)));
        Kiem.bang("bac tang len 2", Integer.valueOf(2), Integer.valueOf(t.bac()));

        final ChongSpam.TrangThai bao = new ChongSpam.TrangThai();
        Kiem.dung("bao lan dau", ChongSpam.nenBao(bao, 100000L));
        Kiem.sai("bao lai trong 5 giay: khong", ChongSpam.nenBao(bao, 104000L));
        Kiem.dung("sau 5 giay bao lai duoc", ChongSpam.nenBao(bao, 105000L));
        Kiem.dung(ChongSpam.moTa(), ChongSpam.moTa().startsWith("CHONG SPAM -> BAT"));
    }

    private static void theGioiVaThaoTac() {
        ChongSpam.datLaiChoKiemThu();
        final long now = 20000000L;
        final ChongSpam.TrangThai a = new ChongSpam.TrangThai();
        Kiem.dung("loa: tin dau qua", ChongSpam.kiemChat(a, ChongSpam.Kenh.THE_GIOI, "ban x", now) == ChongSpam.KetQua.CHO_PHEP);
        Kiem.bang("loa: tin thu hai trong 30 giay bi chan", ChongSpam.KetQua.QUA_NHANH,
                ChongSpam.kiemChat(a, ChongSpam.Kenh.THE_GIOI, "ban y", now + 1000L));
        int qua = 0;
        for (int i = 0; i < 20; i++) {
            if (ChongSpam.kiemChat(new ChongSpam.TrangThai(), ChongSpam.Kenh.THE_GIOI, "nguoi " + i + (char) ('a' + i), now + 2000L + i)
                    == ChongSpam.KetQua.CHO_PHEP) {
                qua++;
            }
        }
        Kiem.bang("tran chung kenh the gioi: 12/phut ca may chu (1 da dung + 11)", Integer.valueOf(11), Integer.valueOf(qua));
        Kiem.bang("vuot tran chung -> KENH_BAN", ChongSpam.KetQua.KENH_BAN,
                ChongSpam.kiemChat(new ChongSpam.TrangThai(), ChongSpam.Kenh.THE_GIOI, "nua", now + 3000L));

        final ChongSpam.TrangThai t = new ChongSpam.TrangThai();
        int moi = 0;
        for (int i = 0; i < 10; i++) {
            if (ChongSpam.choHanhDong(t, ChongSpam.HanhDong.MOI_DOI, now + i * 100L)) {
                moi++;
            }
        }
        Kiem.bang("moi to doi: 4 / 10 giay", Integer.valueOf(4), Integer.valueOf(moi));
        Kiem.dung("ket ban dem rieng voi moi doi", ChongSpam.choHanhDong(t, ChongSpam.HanhDong.KET_BAN, now + 1000L));
        Kiem.dung("het cua so thi moi lai duoc", ChongSpam.choHanhDong(t, ChongSpam.HanhDong.MOI_DOI, now + 10001L));
        Kiem.dung("lenh 53 la NPC", ChongSpam.hanhDongCuaLenh(53) == ChongSpam.HanhDong.NPC);
        Kiem.dung("lenh 92 la rao ban", ChongSpam.hanhDongCuaLenh(92) == ChongSpam.HanhDong.RAO_BAN);
        Kiem.dung("lenh 4 (di chuyen) khong bi chong spam thao tac", ChongSpam.hanhDongCuaLenh(4) == null);

        final Properties p = new Properties();
        p.setProperty("sv.spam.hd.moiDoi", "1/60");
        p.setProperty("sv.spam.chat.gan", "2/5");
        ChongSpam.docCauHinh(p);
        final ChongSpam.TrangThai t2 = new ChongSpam.TrangThai();
        Kiem.dung("cau hinh moiDoi=1/60: lan dau qua", ChongSpam.choHanhDong(t2, ChongSpam.HanhDong.MOI_DOI, now));
        Kiem.sai("cau hinh moiDoi=1/60: lan hai sau 30 giay van chan", ChongSpam.choHanhDong(t2, ChongSpam.HanhDong.MOI_DOI, now + 30000L));
        Kiem.dung("mo ta cau hinh in nhip moi", ChongSpam.moTa().contains("gan=2/5s"));
        final Properties tat = new Properties();
        tat.setProperty("sv.spam.bat", "0");
        ChongSpam.docCauHinh(tat);
        Kiem.dung("sv.spam.bat=0: khong chan gi", ChongSpam.kiemChat(a, ChongSpam.Kenh.THE_GIOI, "vao http://x.com", now + 1)
                == ChongSpam.KetQua.CHO_PHEP);
        ChongSpam.datLaiChoKiemThu();
    }

    private static void thongBaoGop() {
        final List<String> gui = new ArrayList<String>();
        ThongBaoGop.datLaiChoKiemThu(new Consumer<String>() {
            @Override
            public void accept(final String s) {
                gui.add(s);
            }
        });
        try {
            final long now = 50000000L;
            int ngay = 0;
            for (int i = 0; i < 30; i++) {
                if (ThongBaoGop.nhanKiemThu("nhan_thuong", "Chúc mừng nv" + i + " nhận được nhân sâm", now + i) != null) {
                    ngay++;
                }
            }
            Kiem.bang("30 thong bao cung loai trong 1 giay: chi 3 gui ngay (tran moi loai)", Integer.valueOf(3), Integer.valueOf(ngay));
            Kiem.sai("chua toi gio gop thi khong gop", ThongBaoGop.gopKiemThu(now + 1000L) == null ? false : true);
            Kiem.dung("chua du 15 giay ke tu tin dau vao hang cho: chua gop", ThongBaoGop.gopKiemThu(now + 14000L) == null);
            final String gop = ThongBaoGop.gopKiemThu(now + 16000L);
            Kiem.dung("15 giay sau: MOT tin gop", gop != null && gop.contains(" | ") && gop.contains(" tin khác)"));
            Kiem.dung("hang cho da xa", ThongBaoGop.gopKiemThu(now + 32000L) == null);
            Kiem.bang("trung noi dung trong 120 giay -> bo", null,
                    ThongBaoGop.nhanKiemThu("boss", "Boss X đã xuất hiện!", now + 40000L) == null ? null
                            : ThongBaoGop.nhanKiemThu("boss", "Boss X đã xuất hiện!", now + 41000L));
            int tong = 0;
            for (int i = 0; i < 20; i++) {
                if (ThongBaoGop.nhanKiemThu("loai" + i, "tin rieng " + (char) ('a' + i), now + 200000L + i) != null) {
                    tong++;
                }
            }
            Kiem.bang("tran chung: 6 tin / 60 giay du moi tin mot loai", Integer.valueOf(6), Integer.valueOf(tong));
            Kiem.dung(ThongBaoGop.moTa(), ThongBaoGop.moTa().contains("toi da 6/60s"));
        } finally {
            ThongBaoGop.datLaiChoKiemThu(null);
        }
    }

    private static void boDemTraCuu() {
        final BoDemGoiTin bo = new BoDemGoiTin(0L);
        boolean chan = false;
        for (int i = 0; i < 400; i++) {
            chan |= bo.vuotNguong(7, 0L);
        }
        Kiem.sai("bot quet 400 id quai (lenh 7) trong 5 giay: KHONG cham tran (tra cuu 600)", chan);
        Kiem.sai("lenh thuong van con nguyen 300", bo.vuotNguong(4, 0L));
        for (int i = 0; i < 200; i++) {
            bo.vuotNguong(7, 0L);
        }
        Kiem.dung("lenh 7 thu 601 bi chan", bo.vuotNguong(7, 0L));
        Kiem.bang("ten loai cho log", "tra cuu", BoDemGoiTin.tenLoai(7));
        Kiem.bang("mac dinh tra cuu 600", Integer.valueOf(600), Integer.valueOf(BoDemGoiTin.GOI_TRA_CUU_MAC_DINH));
        BoDemGoiTin.datMienIp("127.0.0.1, 10.0.0.5");
        Kiem.dung("IP mien", BoDemGoiTin.mienIp("10.0.0.5"));
        Kiem.sai("IP khac khong mien", BoDemGoiTin.mienIp("1.2.3.4"));
        BoDemGoiTin.datMienIp("");
        Kiem.sai("mac dinh khong mien IP nao", BoDemGoiTin.mienIp("127.0.0.1"));
    }

    /** Moi khoa sv.log / sv.spam / sv.thongBao ma nguon doc deu co trong tai lieu + ban mau. */
    private static void cauHinhCoTaiLieu() {
        final String tl = doc("../docs/cau-hinh-server-ini.md");
        final String tmpl = doc("../vps/server.ini.tmpl");
        final String ma = doc("src/com/htbb/log/Nhat.java") + doc("src/com/htbb/chongspam/ChongSpam.java")
                + doc("src/com/htbb/chongspam/ThongBaoGop.java");
        final java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"(sv\\.(?:log|spam|thongBao)\\.[A-Za-z.]+)\"").matcher(ma);
        int n = 0;
        while (m.find()) {
            final String k = m.group(1);
            if (k.endsWith(".")) {
                continue; // tien to "sv.spam.hd." + ten
            }
            n++;
            Kiem.dung("tai lieu co " + k, tl.contains("`" + k + "`"));
            Kiem.dung("ban mau co (dang chu thich) " + k, tmpl.contains("#" + k + "="));
        }
        for (final ChongSpam.HanhDong h : ChongSpam.HanhDong.values()) {
            final String k = "sv.spam.hd." + tenHd(h);
            Kiem.dung("tai lieu co " + k, tl.contains("`" + k + "`"));
            Kiem.dung("ban mau co " + k, tmpl.contains("#" + k + "="));
        }
        Kiem.dung("tim duoc khoa cau hinh (tren 30)", n > 30);
        Kiem.dung("ban mau ghim sv.tranGoiTraCuu=600", tmpl.contains("\nsv.tranGoiTraCuu=600"));
        Kiem.dung("ban mau KHONG mien IP nao (dong mien phai la chu thich)",
                !tmpl.contains("\nsv.tranGoiMienIp="));
        Kiem.dung("TeamServer cai bo chan log", doc("src/server/TeamServer.java").contains("com.htbb.log.Nhat.caiDat()"));
        Kiem.sai("Database khong con in cau SQL nPK kem ten",
                doc("src/data/Database.java").contains("set nPK=\" + c.pk + \" where charname='\" + c.name"));
    }

    private static String tenHd(final ChongSpam.HanhDong h) {
        switch (h) {
            case MOI_DOI:
                return "moiDoi";
            case MOI_BANG:
                return "moiBang";
            case KET_BAN:
                return "ketBan";
            case MOI_GIAO_DICH:
                return "moiGiaoDich";
            case NPC:
                return "npc";
            case RAO_BAN:
                return "raoBan";
            case MUA_BAN:
                return "muaBan";
            default:
                return "dangKy";
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemNhatKyChongSpam() {
    }
}
