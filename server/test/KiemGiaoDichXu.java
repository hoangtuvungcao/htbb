import io.Message;
import io.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import real.Char;
import real.GiaoDichXu;
import real.GiaoDichXuKiem;

/**
 * GIAO DICH XU 1-1 (0.9.27, real/GiaoDichXu + MapMuaBan goi 66/2 kieu 3).
 *
 * Kiem: luat dat / chot (thuan); tron luong goi 66 that (dat xu -> khoa -> xac nhan) qua MapMuaBan.doTrade voi hai
 * nhan vat gia: xu doi chu dung, tong xu bao toan, khong nhan doi khi xac nhan lai / huy giua chung / tieu xu truoc khi
 * chot; tran ngay theo TAI KHOAN (thue.tran_chuyen_xu_gio) ke ca sau "khoi dong lai" (nap lai tu nhat ky); chong clone
 * chan ben dua xu cap thap; luong / luong khoa khong giao dich duoc (khong co kieu goi, so du khong doi); client cu
 * (BAN < 927, PC Unity -1) khong nhan goi 66/2/3 ma nhan dong chu, giao dich thuoc cua ho khong doi.
 */
public final class KiemGiaoDichXu {

    private KiemGiaoDichXu() {
    }

    static final HashMap<Char, List<byte[]>> GOI = new HashMap<Char, List<byte[]>>();
    static real.Map map;
    static short idKe = 31000;
    static int tkKe = 7700000;

    public static void chay() {
        Kiem.nhom("Giao dich xu 1-1 (GiaoDichXu, goi 66/2/3) - nguyen tu, khong nhan doi, tran ngay, luong khong giao dich");
        final Properties cu = new Properties();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        final int[] quaiCu = com.htbb.pokemon.ThongSoLauDai.QUAI_MOI_GIO, xuQuaiCu = com.htbb.pokemon.ThongSoLauDai.XU_MOI_QUAI;
        try {
            if (quaiCu.length < 101 || xuQuaiCu.length < 101) {
                // bang data_cap_do_lau_dai chua nap (chay rieng nhom nay): so lieu gia, 1 gio o cap L = 100 * L * 10 xu
                final int[] q = new int[101], x = new int[101];
                for (int i = 0; i <= 100; i++) {
                    q[i] = 100;
                    x[i] = 10 * Math.max(1, i);
                }
                com.htbb.pokemon.ThongSoLauDai.QUAI_MOI_GIO = q;
                com.htbb.pokemon.ThongSoLauDai.XU_MOI_QUAI = x;
            }
            if (real.Map.gemTemplate == null) {
                real.Map.gemTemplate = new real.GemTemplate[0];
            }
            GiaoDichXu.datNguonThu(null);
            com.htbb.pokemon.ChongClone.boQuaNapSo();
            map = new real.Map();
            luatThuan();
            giaoDichHaiChieu();
            tieuXuTruocKhiChot();
            xacNhanLaiKhongNhanDoi();
            huyGiuaChungKhongSot();
            giamSauKhiKhoa();
            tranNgayTheoTaiKhoan();
            tranNapLaiSauKhoiDong();
            chongCloneChanBenDuaXu();
            luongKhongGiaoDichDuoc();
            clientCu();
            botMienTran();
            maNguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            com.htbb.pokemon.LauDai.apDungCauHinh(cu);
            GiaoDichXu.datNguonThu(null);
            real.Map.gemTemplate = gemCu;
            com.htbb.pokemon.ThongSoLauDai.QUAI_MOI_GIO = quaiCu;
            com.htbb.pokemon.ThongSoLauDai.XU_MOI_QUAI = xuQuaiCu;
        }
    }

    // ------------------------------------------------------------------ dung cu

    static Char nv(final String ten, final long xu, final int ban) {
        final Char p = new Char(null);
        final List<byte[]> ds = new ArrayList<byte[]>();
        p.setSession(new Session(null, null, true) {
            @Override
            public void sendMessage(final Message m) {
                ds.add(m.toByteArray());
            }
        });
        GOI.put(p, ds);
        p.getSession().banClient = ban;
        p.charname = ten;
        p.id = idKe++;
        p.charDBID = 900000 + p.id;
        p.userID = tkKe++;
        p.isBot = -1;
        p.lvDetail.lv = 50;
        p.hp = 1000;
        p.maxhp = 1000;
        p.MAX_INVENTORY = 42;
        p.maxBag = 1;
        p.map = map;
        p.x = 500;
        p.y = 500;
        p.passWord = "";
        p.setxu(xu);
        GiaoDichXuKiem.datLuong(p, 700, 300);
        // chong clone: tai khoan tao lau, choi nhieu gio (qua cong cap / tuoi / gio choi)
        com.htbb.pokemon.ChongClone.datThu(p.userID, System.currentTimeMillis() - 30L * 86400000L, p.charDBID, 100000L);
        return p;
    }

    /** Mo giao dich giua a va b nhu sau goi 66/1 (khong can ban do that cho buoc moi). */
    static void mo(final Char a, final Char b) {
        a.datLaiTrangThaiGiaoDich();
        b.datLaiTrangThaiGiaoDich();
        a.userTrade.add(b);
        b.userTrade.add(a);
    }

    static void gui(final Char p, final byte[] du) {
        GiaoDichXuKiem.goi66(map, p, new Message(66, du));
    }

    static byte[] lenh(final int cmd) throws Exception {
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(o);
        d.writeByte(cmd);
        if (cmd == 3 || cmd == 4) {
            d.writeShort(0);
        }
        return o.toByteArray();
    }

    static byte[] datXu(final long xu) throws Exception {
        return kieu(3, xu);
    }

    static byte[] kieu(final int kieu, final long xu) throws Exception {
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(o);
        d.writeByte(2);
        d.writeByte(kieu);
        d.writeLong(xu);
        return o.toByteArray();
    }

    /** Ca hai khoa (66/4) roi ca hai xac nhan (66/5). */
    static void khoaVaChot(final Char a, final Char b) throws Exception {
        gui(a, lenh(4));
        gui(b, lenh(4));
        gui(a, lenh(5));
        gui(b, lenh(5));
    }

    /** So goi 66/2/3 p nhan duoc; xuCuoi[0] = so xu trong goi cuoi. */
    static int goiXu(final Char p, final long[] xuCuoi) throws Exception {
        int n = 0;
        for (final byte[] b : GOI.get(p)) {
            if (b.length >= 3 + 2 + 2 + 8 && (b[0] & 0xFF) == 66 && b[3] == 2 && b[4] == 3) {
                n++;
                if (xuCuoi != null) {
                    final DataInputStream d = new DataInputStream(new ByteArrayInputStream(b, 5, b.length - 5));
                    d.readShort();
                    xuCuoi[0] = d.readLong();
                }
            }
        }
        return n;
    }

    static String chu(final Char p) {
        final StringBuilder sb = new StringBuilder();
        for (final byte[] b : GOI.get(p)) {
            sb.append(new String(b, StandardCharsets.UTF_8)).append('\n');
        }
        return sb.toString();
    }

    static void datTran(final String gio) {
        final Properties p = new Properties();
        if (gio != null) {
            p.setProperty("thue.tran_chuyen_xu_gio", gio);
        }
        com.htbb.pokemon.LauDai.apDungCauHinh(p);
    }

    // ------------------------------------------------------------------ kiem

    static void luatThuan() {
        final long M = Long.MAX_VALUE;
        Kiem.bang("dat 1000 khi co 5000, chua ai khoa", GiaoDichXu.HOP_LE, GiaoDichXu.kiemDat(1000, 0, false, false, false, 5000, M, false));
        Kiem.bang("dat so am", GiaoDichXu.LOI_SO_AM, GiaoDichXu.kiemDat(-1, 0, false, false, false, 5000, M, false));
        Kiem.bang("dat vuot so du", GiaoDichXu.LOI_KHONG_DU, GiaoDichXu.kiemDat(6000, 0, false, false, false, 5000, M, false));
        Kiem.bang("dat vuot tran con lai", GiaoDichXu.LOI_VUOT_TRAN, GiaoDichXu.kiemDat(3000, 0, false, false, false, 5000, 2000, false));
        Kiem.bang("giam sau khi doi phuong khoa", GiaoDichXu.LOI_DA_KHOA, GiaoDichXu.kiemDat(100, 1000, false, true, false, 5000, M, false));
        Kiem.bang("tang sau khi doi phuong khoa (chi co loi cho ho)", GiaoDichXu.HOP_LE, GiaoDichXu.kiemDat(2000, 1000, false, true, false, 5000, M, false));
        Kiem.bang("minh da khoa -> khong doi", GiaoDichXu.LOI_DA_KHOA, GiaoDichXu.kiemDat(2000, 1000, true, false, false, 5000, M, false));
        Kiem.bang("tran = 0 (tat) -> khong dat duoc", GiaoDichXu.LOI_TAT, GiaoDichXu.kiemDat(1, 0, false, false, false, 5000, M, true));
        Kiem.bang("tat nhung rut ve 0 van duoc", GiaoDichXu.HOP_LE, GiaoDichXu.kiemDat(0, 0, false, false, false, 5000, M, true));
        Kiem.bang("chot hop le", GiaoDichXu.HOP_LE, GiaoDichXu.kiemTraChot(100, 50, 50, 100, 100, 50, M, M));
        Kiem.bang("chot: rXu con sot (tB=0 nhung rA=999)", GiaoDichXu.LOI_LECH_HAI_BEN, GiaoDichXu.kiemTraChot(0, 999, 0, 0, 0, 0, M, M));
        Kiem.bang("chot: ben dua da tieu bot", GiaoDichXu.LOI_KHONG_DU, GiaoDichXu.kiemTraChot(100, 0, 0, 100, 99, 0, M, M));
        Kiem.bang("chot: vuot tran", GiaoDichXu.LOI_VUOT_TRAN, GiaoDichXu.kiemTraChot(100, 0, 0, 100, 1000, 0, 99, M));
        Kiem.bang("chot: so am", GiaoDichXu.LOI_SO_AM, GiaoDichXu.kiemTraChot(-5, 0, 0, -5, 0, 0, M, M));
    }

    static void giaoDichHaiChieu() throws Exception {
        datTran(null);
        final Char a = nv("gdxuA", 100000, 927), b = nv("gdxuB", 50000, 927);
        mo(a, b);
        gui(a, datXu(30000));
        gui(b, datXu(10000));
        final long[] x = new long[1];
        Kiem.dung("B (client moi) nhan goi 66/2/3 voi so xu A dua", goiXu(b, x) >= 1);
        Kiem.bang("B thay 10000 (goi cuoi = chinh B dat)", Long.valueOf(10000L), Long.valueOf(x[0]));
        Kiem.bang("A.tXu / B.rXu = 30000", "30000/30000", a.tXu + "/" + b.rXu);
        Kiem.bang("dat xu CHUA tru gi (A van 100000)", Long.valueOf(100000L), Long.valueOf(a.getxu()));
        khoaVaChot(a, b);
        Kiem.bang("A sau: 100000 - 30000 + 10000", Long.valueOf(80000L), Long.valueOf(a.getxu()));
        Kiem.bang("B sau: 50000 - 10000 + 30000", Long.valueOf(70000L), Long.valueOf(b.getxu()));
        Kiem.bang("tong xu bao toan", Long.valueOf(150000L), Long.valueOf(a.getxu() + b.getxu()));
        Kiem.bang("luong / luong khoa khong doi", "700/300/700/300", GiaoDichXuKiem.luong(a) + "/" + GiaoDichXuKiem.luongKhoa(a)
                + "/" + GiaoDichXuKiem.luong(b) + "/" + GiaoDichXuKiem.luongKhoa(b));
        Kiem.bang("tran ngay ghi theo tai khoan A", Long.valueOf(30000L), Long.valueOf(GiaoDichXu.daChuyenHomNay(a.userID)));
        Kiem.bang("tran ngay ghi theo tai khoan B", Long.valueOf(10000L), Long.valueOf(GiaoDichXu.daChuyenHomNay(b.userID)));
    }

    static void tieuXuTruocKhiChot() throws Exception {
        final Char a = nv("gdxuC", 100000, 927), b = nv("gdxuD", 50000, 927);
        mo(a, b);
        gui(a, datXu(30000));
        gui(b, datXu(5000));
        a.subXu(80000, false, "kiem: tieu o NPC giua chung");
        khoaVaChot(a, b);
        Kiem.bang("A tieu con 20000 < 30000 da dat -> huy, A khong bi tru", Long.valueOf(20000L), Long.valueOf(a.getxu()));
        Kiem.bang("B khong nhan, khong mat", Long.valueOf(50000L), Long.valueOf(b.getxu()));
        Kiem.bang("trang thai xoa sach", "0/0/0/0", a.tXu + "/" + a.rXu + "/" + b.tXu + "/" + b.rXu);
        Kiem.bang("tran ngay khong tinh lan huy", Long.valueOf(0L), Long.valueOf(GiaoDichXu.daChuyenHomNay(a.userID)));

        // ben THU HAI thieu: truHaiBen tru A xong, B thieu -> A duoc hoan, khong ai doi
        final Char c = nv("gdxuC2", 1000, 927), d = nv("gdxuD2", 1000, 927);
        mo(c, d);
        c.tXu = 500;
        d.rXu = 500;
        d.tXu = 2000;
        c.rXu = 2000;
        Kiem.sai("truHaiBen: ben B thieu -> false", GiaoDichXu.truHaiBen(c, d));
        Kiem.bang("truHaiBen: A duoc hoan du", "1000/1000", c.getxu() + "/" + d.getxu());
    }

    static void xacNhanLaiKhongNhanDoi() throws Exception {
        final Char a = nv("gdxuE", 100000, 927), b = nv("gdxuF", 0, 927);
        mo(a, b);
        gui(a, datXu(40000));
        khoaVaChot(a, b);
        gui(a, lenh(5));
        gui(b, lenh(5));
        gui(a, lenh(4));
        gui(b, lenh(4));
        gui(a, lenh(5));
        gui(b, lenh(5));
        Kiem.bang("gui lai 66/5, 66/4 sau khi xong -> khong chuyen them", "60000/40000", a.getxu() + "/" + b.getxu());
        // hai luong cung chot: KHOA_GIAO_DICH tuan tu hoa -> chi mot lan
        final Char c = nv("gdxuE2", 100000, 927), d = nv("gdxuF2", 0, 927);
        mo(c, d);
        gui(c, datXu(10000));
        gui(c, lenh(4));
        gui(d, lenh(4));
        gui(c, lenh(5));
        final Thread t1 = new Thread(() -> {
            try {
                gui(d, lenh(5));
            } catch (final Exception e) {
            }
        });
        final Thread t2 = new Thread(() -> {
            try {
                gui(c, lenh(5));
            } catch (final Exception e) {
            }
        });
        t1.start();
        t2.start();
        t1.join(5000);
        t2.join(5000);
        Kiem.bang("hai luong cung xac nhan -> xu chuyen dung mot lan", "90000/10000", c.getxu() + "/" + d.getxu());
    }

    static void huyGiuaChungKhongSot() throws Exception {
        final Char a = nv("gdxuG", 100000, 927), b = nv("gdxuH", 100000, 927), c = nv("gdxuI", 0, 927);
        mo(a, b);
        gui(b, datXu(50000));
        gui(a, lenh(4));
        a.huyGiaoDich(false, true); // thoat / rot mang / doi map giua chung
        Kiem.bang("huy: rXu cua A va tXu cua B xoa", "0/0", a.rXu + "/" + b.tXu);
        Kiem.bang("huy: khong ai mat xu", "100000/100000", a.getxu() + "/" + b.getxu());
        // giao dich moi voi nguoi thu ba, khong ai dua gi: A KHONG duoc cong 50000 sot lai
        mo(a, c);
        khoaVaChot(a, c);
        Kiem.bang("giao dich sau khong nhan xu sot tu lan huy", "100000/0", a.getxu() + "/" + c.getxu());
    }

    static void giamSauKhiKhoa() throws Exception {
        final Char a = nv("gdxuJ", 100000, 927), b = nv("gdxuK", 100000, 927);
        mo(a, b);
        gui(a, datXu(20000));
        gui(b, lenh(4));
        gui(a, datXu(1000));
        Kiem.bang("doi phuong da khoa -> A khong giam duoc", Long.valueOf(20000L), Long.valueOf(a.tXu));
        gui(a, datXu(25000));
        Kiem.bang("tang van duoc", Long.valueOf(25000L), Long.valueOf(b.rXu));
        gui(a, lenh(4));
        gui(a, datXu(30000));
        Kiem.bang("minh da khoa -> khong doi nua", Long.valueOf(25000L), Long.valueOf(a.tXu));
        a.huyGiaoDich(false, true);
    }

    static void tranNgayTheoTaiKhoan() throws Exception {
        datTran("1");
        final Char a = nv("gdxuL", 100000000L, 927), b = nv("gdxuM", 0, 927);
        final long tran = GiaoDichXu.tranNgay(a);
        Kiem.bang("tran = 1 gio thu nhap o cap 50", Long.valueOf(com.htbb.pokemon.LauDai.xuMoiGio(50)), Long.valueOf(tran));
        mo(a, b);
        gui(a, datXu(tran + 1));
        Kiem.bang("dat vuot tran -> bi tu choi", Long.valueOf(0L), Long.valueOf(a.tXu));
        Kiem.dung("bao con lai bao nhieu", chu(a).contains("chỉ còn đưa được"));
        gui(a, datXu(tran));
        khoaVaChot(a, b);
        Kiem.bang("dua dung tran -> thanh cong", Long.valueOf(tran), Long.valueOf(b.getxu()));
        mo(a, b);
        gui(a, datXu(1));
        Kiem.bang("het tran -> dat 1 xu bi tu choi", Long.valueOf(0L), Long.valueOf(a.tXu));
        // nhan vat khac CUNG tai khoan: tran tinh chung
        final Char a2 = nv("gdxuL2", 100000000L, 927);
        a2.userID = a.userID;
        mo(a2, b);
        gui(a2, datXu(1));
        Kiem.bang("nhan vat khac cung tai khoan cung het tran", Long.valueOf(0L), Long.valueOf(a2.tXu));
        // chen trang thai bang tay (goi tu che) -> luc chot van chan
        a2.tXu = 5;
        b.rXu = 5;
        gui(a2, lenh(4));
        gui(b, lenh(4));
        gui(a2, lenh(5));
        gui(b, lenh(5));
        Kiem.bang("vuot tran luc chot -> huy, khong chuyen", Long.valueOf(tran), Long.valueOf(b.getxu()));
        // nguoi NHAN van dua duoc trong tran cua ho
        Kiem.bang("con lai cua B (chua dua gi) = tran cua B", Long.valueOf(GiaoDichXu.tranNgay(b)), Long.valueOf(GiaoDichXu.conLai(b)));
        datTran("0");
        mo(b, a);
        b.addXu(10, "kiem");
        gui(b, datXu(5));
        Kiem.bang("tran = 0 -> giao dich xu tat", Long.valueOf(0L), Long.valueOf(b.tXu));
        b.huyGiaoDich(false, true);
        datTran("-1");
        Kiem.bang("tran < 0 -> khong gioi han", Long.valueOf(Long.MAX_VALUE), Long.valueOf(GiaoDichXu.tranNgay(a)));
        datTran(null);
        Kiem.bang("mac dinh 5 gio (thue.tran_chuyen_xu_gio)", Long.valueOf(com.htbb.pokemon.LauDai.xuMoiGio(50) * 5),
                Long.valueOf(GiaoDichXu.tranNgay(a)));
    }

    static void tranNapLaiSauKhoiDong() throws Exception {
        datTran("1");
        final Char a = nv("gdxuN", 100000000L, 927), b = nv("gdxuO", 0, 927);
        final long tran = GiaoDichXu.tranNgay(a);
        final java.util.Map<Integer, Long> nhatKy = new HashMap<Integer, Long>();
        nhatKy.put(Integer.valueOf(a.userID), Long.valueOf(tran - 100));
        GiaoDichXu.datNguonThu(() -> nhatKy); // "khoi dong lai": bo dem trong bo nho rong, nap tu tob_log_chuyen_gia_tri
        Kiem.bang("nap lai: da dua hom nay tu nhat ky", Long.valueOf(tran - 100), Long.valueOf(GiaoDichXu.daChuyenHomNay(a.userID)));
        mo(a, b);
        gui(a, datXu(101));
        Kiem.bang("sau khoi dong van con tran cu -> 101 bi chan", Long.valueOf(0L), Long.valueOf(a.tXu));
        gui(a, datXu(100));
        Kiem.bang("100 con lai thi duoc", Long.valueOf(100L), Long.valueOf(a.tXu));
        a.huyGiaoDich(false, true);
        GiaoDichXu.datNguonThu(null);
        datTran(null);
    }

    static void chongCloneChanBenDuaXu() throws Exception {
        final Char a = nv("gdxuP", 100000, 927), b = nv("gdxuQ", 0, 927);
        a.lvDetail.lv = 5; // duoi chong_clone.cap_toi_thieu
        mo(a, b);
        gui(a, datXu(1000));
        khoaVaChot(a, b);
        Kiem.bang("ben dua xu duoi cap chong clone -> chan, khong chuyen", "100000/0", a.getxu() + "/" + b.getxu());
    }

    static void luongKhongGiaoDichDuoc() throws Exception {
        final Char a = nv("gdxuR", 1000, 927), b = nv("gdxuS", 1000, 927);
        mo(a, b);
        // thu moi kieu khac 1/2/3 (vd client sua goi de "dua luong"): may chu bo qua
        for (int k = 4; k <= 9; k++) {
            gui(a, kieu(k, 200));
        }
        gui(a, kieu(-1, 200));
        khoaVaChot(a, b);
        Kiem.bang("kieu la (4..9, -1) khong chuyen luong / luong khoa / xu", "700/300/700/300/1000/1000",
                GiaoDichXuKiem.luong(a) + "/" + GiaoDichXuKiem.luongKhoa(a) + "/" + GiaoDichXuKiem.luong(b) + "/"
                + GiaoDichXuKiem.luongKhoa(b) + "/" + a.getxu() + "/" + b.getxu());
    }

    static void clientCu() throws Exception {
        final Char cu = nv("gdxuCu", 1000, 926), unity = nv("gdxuUnity", 1000, -1), moi = nv("gdxuMoi", 100000, 927);
        mo(moi, cu);
        gui(moi, datXu(3000));
        Kiem.bang("client cu (926) KHONG nhan goi 66/2/3", Integer.valueOf(0), Integer.valueOf(goiXu(cu, null)));
        Kiem.dung("client cu nhan dong chu thay the co so xu", chu(cu).contains("3.000 xu"));
        khoaVaChot(moi, cu);
        Kiem.bang("client cu van nhan xu khi hoan tat", Long.valueOf(4000L), Long.valueOf(cu.getxu()));
        mo(moi, unity);
        gui(moi, datXu(2000));
        Kiem.bang("PC Unity (ban -1) KHONG nhan goi 66/2/3", Integer.valueOf(0), Integer.valueOf(goiXu(unity, null)));
        moi.huyGiaoDich(false, true);
        // client cu giao dich nhu truoc: khong gui kieu 3 -> tXu/rXu = 0, khong ai mat xu
        final Char c1 = nv("gdxuCu1", 5000, 926), c2 = nv("gdxuCu2", 5000, 926);
        mo(c1, c2);
        khoaVaChot(c1, c2);
        Kiem.bang("hai client cu giao dich khong co xu -> so du giu nguyen", "5000/5000", c1.getxu() + "/" + c2.getxu());
        Kiem.bang("hai client cu khong nhan goi 66/2/3 nao", Integer.valueOf(0), Integer.valueOf(goiXu(c1, null) + goiXu(c2, null)));
    }

    static void botMienTran() {
        final Char bot = nv("gdxuBot", 1000, 927);
        bot.laHlvAi = true;
        Kiem.bang("huan luyen vien AI khong tinh tran ngay (bot -> nguoi co han muc rieng HlvAiGame)", Long.valueOf(Long.MAX_VALUE),
                Long.valueOf(GiaoDichXu.tranNgay(bot)));
        bot.laHlvAi = false;
        final Char a = nv("gdxuT", 0, 927), b = nv("gdxuU", 0, 927);
        a.tXu = 100;
        b.tXu = 40;
        Kiem.dung("ben dua rong (A) luu truoc", GiaoDichXu.luuATruoc(a, b));
        Kiem.sai("ben nhan rong (B) luu sau", GiaoDichXu.luuATruoc(b, a));
    }

    static void maNguon() throws Exception {
        final String x = new String(Files.readAllBytes(Paths.get("src/real/GiaoDichXu.java")), StandardCharsets.UTF_8);
        final String mb = new String(Files.readAllBytes(Paths.get("src/real/MapMuaBan.java")), StandardCharsets.UTF_8);
        Kiem.sai("GiaoDichXu khong dong vao luong / luong khoa", x.contains("Luong(") || x.contains("lockLuong") || x.contains(".luong"));
        final int tru = mb.indexOf("GiaoDichXu.truHaiBen(player, dp)");
        final int thuoc = mb.indexOf("player.doAddItemTrad2Inventory()");
        final int cong = mb.indexOf("GiaoDichXu.congHaiBen(player, dp)");
        Kiem.dung("MapMuaBan: tru xu -> doi thuoc -> cong xu (theo thu tu)", tru > 0 && tru < thuoc && thuoc < cong);
        Kiem.dung("thuoc that bai -> hoan xu", mb.indexOf("GiaoDichXu.hoanHaiBen(player, dp)", thuoc) > thuoc
                && mb.indexOf("GiaoDichXu.hoanHaiBen(player, dp)", thuoc) < cong);
        Kiem.dung("kiem chot xu truoc chong clone", mb.indexOf("GiaoDichXu.kiemChot(player, dp)") < mb.indexOf("ChongClone.chanChuyen(player, dp"));
        Kiem.dung("nhat ky gdxu nam trong LocLogVatPham.GIU", data.LocLogVatPham.GIU.contains("gdxu"));
    }
}
