package data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import io.Session;
import real.Char;
import util.Logger;

/**
 * Ghi cac nghi van gian lan ra hai file text de admin doc duoc ngay tren trang
 * Chan doan, thay vi phai truy van database.
 *
 *     hack.txt     nghi van chung   (dang nhap sai nhan vat, sua goi tin, capcha...)
 *     hackdo.txt   nghi van vat pham (mua ban, hanh trang, trang bi, gem)
 *
 * KHONG thay the viec ghi database. Cac lenh saveOrtherLog(..., "hackc" / "hackR" /
 * "hackbitem" / "hackchonchar" / "ffbug") van giu nguyen — database de thong ke va
 * loc theo nhan vat, con file de doc nhanh theo dong thoi gian.
 *
 * ---------------------------------------------------------------------------
 * VI SAO CO HAN MUC GHI
 *
 * Ngay 29/08/2026, log/game.out tren VPS phinh len 146 MB. Doc ra thi 326.826
 * tren 2.437.569 dong la CUNG MOT vet goi lap lai:
 * ArrayIndexOutOfBoundsException trong Map.deletePotionAndItemOnGround, bi in ra
 * boi khoi catch nam trong vong lap chay cua map (Region.java:186, moi 300ms).
 *
 * Bai hoc: mot cho ghi log nam trong duong chay lien tuc, khong co han muc, som
 * muon se lam day o dia. Cac diem phat hien gian lan duoi day cung nam trong
 * duong xu ly goi tin — neu mot phep kiem nao do bao dong gia lien tuc (do tre
 * mang, du lieu cu), no se ghi khong ngung.
 *
 * Nen o day co hai tran:
 *     - moi nhan vat toi da 20 dong moi gio
 *     - toan server toi da 500 dong moi gio
 * Cham tran thi ghi mot dong bao roi im den khi sang cua so gio moi. Bo dem duoc
 * tach rieng thanh ham thuan de kiem thu duoc — xem test/KiemNghiVanGianLan.java.
 */
public final class NghiVanGianLan {

    public static final String FILE_CHUNG    = "hack.txt";
    public static final String FILE_VAT_PHAM = "hackdo.txt";

    /** Toi da bao nhieu dong cho MOT nhan vat trong mot cua so. */
    public static final int TOI_DA_MOI_NHAN_VAT = 20;
    /** Toi da bao nhieu dong cho CA SERVER trong mot cua so. */
    public static final int TOI_DA_TOAN_SERVER = 500;
    /** Do dai cua so tinh han muc: mot gio. */
    public static final long CUA_SO_MS = 3600000L;

    /** Cat bot chi tiet qua dai — mot dong log khong duoc keo ca man hinh. */
    public static final int CHI_TIET_TOI_DA = 300;

    private static final Hashtable<String, Integer> DEM_THEO_NHAN_VAT = new Hashtable<String, Integer>();
    private static int  demToanServer;
    private static long batDauCuaSo = -1L;

    /** Dinh dang mot dong: SimpleDateFormat khong an toan da luong nen bao trong synchronized. */
    private static final SimpleDateFormat GIO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private NghiVanGianLan() {
    }

    /** Nghi van chung -> hack.txt */
    public static void ghiChung(final Char p, final String loai, final String chiTiet) {
        ghi(FILE_CHUNG, tenNhanVat(p), diaChiIp(p), loai, chiTiet);
    }

    /** Nghi van vat pham -> hackdo.txt */
    public static void ghiVatPham(final Char p, final String loai, final String chiTiet) {
        ghi(FILE_VAT_PHAM, tenNhanVat(p), diaChiIp(p), loai, chiTiet);
    }

    /**
     * Ban dung khi chua co doi tuong Char (vi du luc chon nhan vat, hack dang nhap):
     * truyen thang ten va IP lay tu Session.
     */
    public static void ghiChung(final Session s, final String ten, final String loai, final String chiTiet) {
        ghi(FILE_CHUNG, ten, (s == null) ? "" : s.ip, loai, chiTiet);
    }

    /**
     * KHONG BAO GIO nem ngoai le ra ngoai. Day la duong ghi log nam trong luong xu
     * ly goi tin cua nguoi choi — mot loi ghi file khong duoc phep lam hong nuoc di
     * cua ho.
     */
    private static void ghi(final String tenFile, final String charname, final String ip,
                            final String loai, final String chiTiet) {
        try {
            final long bayGio = System.currentTimeMillis();
            final int cho = xinPhepGhi(charname, bayGio);
            if (cho == KHONG_CHO) {
                return;
            }
            final String gio;
            synchronized (GIO) {
                gio = GIO.format(new Date(bayGio));
            }
            if (cho == CHO_VA_BAO_CHAM_TRAN) {
                Logger.logString(taoDong(gio, charname, ip, loai,
                        "... cham tran ghi log, tam ngung den gio sau"), tenFile);
                return;
            }
            Logger.logString(taoDong(gio, charname, ip, loai, chiTiet), tenFile);
        } catch (final Throwable boQua) {
        }
    }

    // ---------------------------------------------------------------- han muc

    public static final int CHO                  = 0;
    public static final int CHO_VA_BAO_CHAM_TRAN = 1;
    public static final int KHONG_CHO            = 2;

    /**
     * Quyet dinh co ghi dong nay khong. Thoi gian truyen tu ngoai vao de kiem thu
     * duoc ma khong phai cho het mot gio.
     *
     * Tra ve CHO_VA_BAO_CHAM_TRAN dung MOT lan tai dung dong vuot tran, de nguoi
     * doc file biet la co bi cat bot chu khong tuong la het chuyen.
     */
    public static synchronized int xinPhepGhi(final String charname, final long bayGio) {
        if (batDauCuaSo < 0L || bayGio - batDauCuaSo >= CUA_SO_MS) {
            batDauCuaSo = bayGio;
            demToanServer = 0;
            DEM_THEO_NHAN_VAT.clear();
        }

        final String khoa = (charname == null || charname.length() == 0) ? "(khong ten)" : charname;
        final Integer daCo = DEM_THEO_NHAN_VAT.get(khoa);
        final int soCu = (daCo == null) ? 0 : daCo.intValue();

        // Tran toan server tinh truoc: mot dot bao dong gia tren nhieu nhan vat
        // van phai bi chan.
        if (demToanServer >= TOI_DA_TOAN_SERVER) {
            return (demToanServer == TOI_DA_TOAN_SERVER) ? danhDauChamTran() : KHONG_CHO;
        }
        if (soCu >= TOI_DA_MOI_NHAN_VAT) {
            if (soCu == TOI_DA_MOI_NHAN_VAT) {
                DEM_THEO_NHAN_VAT.put(khoa, Integer.valueOf(soCu + 1));
                ++demToanServer;
                return CHO_VA_BAO_CHAM_TRAN;
            }
            return KHONG_CHO;
        }

        DEM_THEO_NHAN_VAT.put(khoa, Integer.valueOf(soCu + 1));
        ++demToanServer;
        return CHO;
    }

    private static int danhDauChamTran() {
        ++demToanServer;
        return CHO_VA_BAO_CHAM_TRAN;
    }

    /** Chi dung cho kiem thu: xoa sach bo dem. */
    public static synchronized void datLai() {
        batDauCuaSo = -1L;
        demToanServer = 0;
        DEM_THEO_NHAN_VAT.clear();
    }

    // ---------------------------------------------------------------- dinh dang

    /**
     * Mot su kien = MOT dong, cac cot ngan cach bang " | ".
     *
     * Moi truong bi thay ky tu xuong dong va dau ngan cot, de mot ten nhan vat
     * hay chi tiet co ky tu la khong the day noi dung sang dong khac — nhu vay
     * "grep" theo ten nhan vat luon dem dung so su kien.
     */
    public static String taoDong(final String gio, final String charname, final String ip,
                          final String loai, final String chiTiet) {
        return locO(gio) + " | " + locO(charname) + " | " + locO(ip)
             + " | " + locO(loai) + " | " + cat(locO(chiTiet));
    }

    private static String locO(final String s) {
        if (s == null || s.length() == 0) {
            return "-";
        }
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                sb.append(' ');
            } else if (c == '|') {
                sb.append('/');
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private static String cat(final String s) {
        if (s.length() <= CHI_TIET_TOI_DA) {
            return s;
        }
        return s.substring(0, CHI_TIET_TOI_DA) + "...";
    }

    // ---------------------------------------------------------------- tien ich

    private static String tenNhanVat(final Char p) {
        try {
            return (p == null) ? null : p.charname;
        } catch (final Throwable boQua) {
            return null;
        }
    }

    private static String diaChiIp(final Char p) {
        try {
            if (p == null) {
                return null;
            }
            final Session s = p.getSession();
            return (s == null) ? null : s.ip;
        } catch (final Throwable boQua) {
            return null;
        }
    }
}
