package server;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DOC / GHI server.ini LUC DANG CHAY
 *
 * Muc dich: bang setting cua admin trong game va file server.ini la CUNG MOT
 * nguon du lieu. Admin sua trong game -> ghi thang vao server.ini nen con
 * nguyen sau khi restart. Sua tay trong server.ini -> go lenh nap lai (hoac
 * restart) la co hieu luc.
 *
 * Cach ghi: doc file thanh tung dong, thay dung dong "key=..." roi ghi lai.
 * Khong dung Properties.store() vi ham do xoa sach comment va dao lon thu tu.
 * Dong nao khong lien quan duoc giu nguyen tung byte (doc/ghi bang ISO-8859-1,
 * la bang ma Properties dung, nen khong lam hong ky tu nao).
 */
public final class CauHinh {

    public static final String FILE = "server.ini";
    private static final Charset MA_HOA = Charset.forName("ISO-8859-1");

    /** Cac khoa server.ini dung cho bang setting. */
    public static final String K_GIO_CT = "sv.gioChiemThanh";
    public static final String K_PHUT_CT = "sv.phutChiemThanh";
    public static final String K_NGAY_CT = "sv.ngayChiemThanh";
    public static final String K_TILE_XU = "sv.tileXu";
    public static final String K_TILE_LUONG = "sv.tileLuong";
    public static final String K_TILE_SUKIEN = "sv.tileItemSuKien";
    public static final String K_TILE_POTION = "sv.tilePotion";
    public static final String K_TILE_NGOC = "sv.tileNgoc";
    public static final String K_TILE_ITEM = "sv.tileItem";
    public static final String K_CHIA_XU = "sv.chiaXuRoi";
    public static final String K_TAO_TEN_GM = "sv.choPhepTaoTenGM";

    private CauHinh() {
    }

    // ============================ GHI ============================

    /** Ghi mot khoa. Tra ve true neu da ghi xuong file thanh cong. */
    public static boolean luu(final String key, final String value) {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(key, value);
        return luuNhieu(m);
    }

    public static boolean luu(final String key, final int value) {
        return luu(key, String.valueOf(value));
    }

    /**
     * Ghi nhieu khoa trong mot lan mo file. Khoa da co thi thay gia tri (giu
     * nguyen vi tri trong file), khoa chua co thi them vao cuoi.
     */
    public static synchronized boolean luuNhieu(final LinkedHashMap<String, String> capGiaTri) {
        if (capGiaTri == null || capGiaTri.isEmpty()) {
            return true;
        }
        try {
            final File f = new File(FILE);
            final String noiDung = f.exists() ? new String(Files.readAllBytes(f.toPath()), MA_HOA) : "";
            // Giu nguyen kieu xuong dong cua file (LF hay CRLF) va viec file co
            // ket thuc bang xuong dong hay khong, de ghi lai khong lam doi ca file.
            final String xuongDong = noiDung.contains("\r\n") ? "\r\n" : "\n";
            final boolean hetBangXuongDong = noiDung.endsWith("\n") || noiDung.endsWith("\r");
            final List<String> dong = new ArrayList<String>();
            if (!noiDung.isEmpty()) {
                final String[] tach = noiDung.split("\r\n|\n|\r", -1);
                for (int i = 0; i < tach.length; i++) {
                    // Phan tu rong cuoi cung chi la he qua cua dau xuong dong cuoi file
                    if (i == tach.length - 1 && tach[i].isEmpty() && hetBangXuongDong) {
                        break;
                    }
                    dong.add(tach[i]);
                }
            }

            final LinkedHashMap<String, String> conLai = new LinkedHashMap<String, String>(capGiaTri);
            for (int i = 0; i < dong.size(); i++) {
                final String l = dong.get(i);
                final String trim = l.trim();
                // Bo qua dong trong va dong chu thich (#, !, //)
                if (trim.isEmpty() || trim.startsWith("#") || trim.startsWith("!") || trim.startsWith("//")) {
                    continue;
                }
                final int dau = l.indexOf('=');
                if (dau < 0) {
                    continue;
                }
                final String key = l.substring(0, dau).trim();
                if (conLai.containsKey(key)) {
                    dong.set(i, key + "=" + conLai.remove(key));
                }
            }
            if (!conLai.isEmpty()) {
                dong.add("");
                dong.add("# ==== Cac dong duoi day do bang setting trong game ghi ra ====");
                for (final Map.Entry<String, String> e : conLai.entrySet()) {
                    dong.add(e.getKey() + "=" + e.getValue());
                }
            }
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dong.size(); i++) {
                if (i > 0) {
                    sb.append(xuongDong);
                }
                sb.append(dong.get(i));
            }
            if (hetBangXuongDong || noiDung.isEmpty()) {
                sb.append(xuongDong);
            }
            final byte[] raBytes = sb.toString().getBytes(MA_HOA);

            // Ghi ra file tam roi doi ten: mat dien giua luc ghi cung khong lam
            // mat server.ini dang dung.
            final File tam = new File(FILE + ".tmp");
            Files.write(tam.toPath(), raBytes);
            final File bak = new File(FILE + ".bak");
            if (bak.exists()) {
                bak.delete();
            }
            if (f.exists() && !f.renameTo(bak)) {
                // Doi ten that bai (file dang bi mo) -> ghi truc tiep
                Files.write(f.toPath(), raBytes);
                tam.delete();
                return true;
            }
            if (!tam.renameTo(f)) {
                // Khong doi ten duoc thi tra lai ban cu
                bak.renameTo(f);
                return false;
            }
            return true;
        } catch (final Exception e) {
            System.out.println("LOI GHI " + FILE + ": " + e);
            return false;
        }
    }

    // =========================== NAP LAI ===========================

    /**
     * Nap lai tu server.ini nhung THAM SO CO THE DOI LUC DANG CHAY.
     *
     * CO Y KHONG nap lai: cong, ket noi DB, duong dan log, gioi han CCU...
     * doi cac thu do luc server dang chay se lam dut ket noi hoac tao pool DB
     * thu hai. Muon doi chung thi restart.
     *
     * @return mo ta ket qua de hien cho admin
     */
    public static synchronized String napLai() {
        try {
            final Properties p = new Properties();
            p.load(Files.newInputStream(Paths.get(FILE)));

            real.Monster.PC_TILE_XU = kep(p.getProperty(K_TILE_XU), real.Monster.PC_TILE_XU);
            real.Monster.PC_TILE_LUONG = kep(p.getProperty(K_TILE_LUONG), real.Monster.PC_TILE_LUONG);
            real.Monster.PC_TILE_ITEM_SUKIEN = kep(p.getProperty(K_TILE_SUKIEN), real.Monster.PC_TILE_ITEM_SUKIEN);
            real.Monster.PC_TILE_POTION = kep(p.getProperty(K_TILE_POTION), real.Monster.PC_TILE_POTION);
            real.Monster.PC_TILE_NGOC = kep(p.getProperty(K_TILE_NGOC), real.Monster.PC_TILE_NGOC);
            real.Monster.PC_TILE_ITEM = kep(p.getProperty(K_TILE_ITEM), real.Monster.PC_TILE_ITEM);
            real.Map.DIV_DROP_XU = Math.max(1, so(p.getProperty(K_CHIA_XU), real.Map.DIV_DROP_XU));
            TeamServer.tileGoc = new int[]{real.Monster.PC_TILE_XU, real.Monster.PC_TILE_LUONG,
                real.Monster.PC_TILE_ITEM_SUKIEN, real.Monster.PC_TILE_POTION,
                real.Monster.PC_TILE_NGOC, real.Monster.PC_TILE_ITEM, real.Map.DIV_DROP_XU};

            apDungGioChiemThanh(p);
            data.Database.CHO_PHEP_TAO_TEN_GM = "1".equals(nhan(p.getProperty(K_TAO_TEN_GM), "0"));

            int max = so(p.getProperty("sv.maxCharPerAccount"), data.Database.MAX_CHAR_PER_ACCOUNT);
            if (max < 1) {
                max = 1;
            }
            if (max > 3) {
                max = 3;
            }
            data.Database.MAX_CHAR_PER_ACCOUNT = max;

            TeamServer.adminAccounts.clear();
            final String ds = nhan(p.getProperty("sv.adminAccounts"), "");
            if (!ds.isEmpty()) {
                for (final String a : ds.split(",")) {
                    final String an = a.trim().toLowerCase();
                    if (!an.isEmpty()) {
                        TeamServer.adminAccounts.add(an);
                    }
                }
            }
            TeamServer.website = nhan(p.getProperty("sv.website"), "");
            TeamServer.linkTaiGame = nhan(p.getProperty("sv.linkTaiGame"), "");
            TeamServer.testMode = "1".equals(nhan(p.getProperty("sv.testMode"), "0"));
            TeamServer.testCode = nhan(p.getProperty("sv.testCode"), "").toLowerCase();

            return "Đã nạp lại " + FILE + ".";
        } catch (final Exception e) {
            return "Không nạp được " + FILE + ": " + e;
        }
    }

    /** Ap dung gio chiem thanh trong file vao MapTown. Dung ca luc khoi dong. */
    public static void apDungGioChiemThanh(final Properties p) {
        real.MapTown.TimeAdminCheat = so(p.getProperty(K_GIO_CT), -1);
        real.MapTown.minuteAdminCheat = so(p.getProperty(K_PHUT_CT), -1);
        real.MapTown.dayAdminCheat = nhan(p.getProperty(K_NGAY_CT), "");
        if (real.MapTown.TimeAdminCheat < 0 || real.MapTown.TimeAdminCheat > 23) {
            real.MapTown.TimeAdminCheat = -1;
            real.MapTown.minuteAdminCheat = -1;
        }
        if (real.MapTown.minuteAdminCheat < 0 || real.MapTown.minuteAdminCheat > 59) {
            real.MapTown.minuteAdminCheat = 0;
        }
    }

    /** Ghi gio chiem thanh dang dat trong bo nho xuong file. */
    public static boolean luuGioChiemThanh() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_GIO_CT, String.valueOf(real.MapTown.TimeAdminCheat));
        m.put(K_PHUT_CT, String.valueOf(real.MapTown.minuteAdminCheat));
        m.put(K_NGAY_CT, real.MapTown.dayAdminCheat == null ? "" : real.MapTown.dayAdminCheat);
        return luuNhieu(m);
    }

    /** Ghi ti le drop dang dat trong bo nho xuong file. */
    public static boolean luuTiLeDrop() {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(K_TILE_XU, String.valueOf(real.Monster.PC_TILE_XU));
        m.put(K_TILE_LUONG, String.valueOf(real.Monster.PC_TILE_LUONG));
        m.put(K_TILE_SUKIEN, String.valueOf(real.Monster.PC_TILE_ITEM_SUKIEN));
        m.put(K_TILE_POTION, String.valueOf(real.Monster.PC_TILE_POTION));
        m.put(K_TILE_NGOC, String.valueOf(real.Monster.PC_TILE_NGOC));
        m.put(K_TILE_ITEM, String.valueOf(real.Monster.PC_TILE_ITEM));
        m.put(K_CHIA_XU, String.valueOf(real.Map.DIV_DROP_XU));
        return luuNhieu(m);
    }

    // =========================== TIEN ICH ===========================

    private static String nhan(final String v, final String macDinh) {
        return (v == null) ? macDinh : v.trim();
    }

    private static int so(final String v, final int macDinh) {
        try {
            return Integer.parseInt(v.trim());
        } catch (final Exception e) {
            return macDinh;
        }
    }

    private static int kep(final String v, final int macDinh) {
        int n = so(v, macDinh);
        if (n < 0) {
            n = 0;
        }
        if (n > 100) {
            n = 100;
        }
        return n;
    }
}
