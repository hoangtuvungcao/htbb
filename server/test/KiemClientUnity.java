import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu phan dung client Unity CHINH (client_pc_ios/: Assets/, Packages/, ProjectSettings/).
 *
 * Truoc day bai nay soi ban port cu client_pc_ios/ClientUnity/ (da xoa 19/10/2026 - ma nguon
 * nay da gop vao client_pc_ios/Assets). Giu lai nhung tinh chat co nghia tren client that:
 *
 *   1. MyGraphics tao Material tu shader dung san (Unity da bo tao Material tu CHUOI ma shader).
 *   2. Ham native [DllImport] chi duoc khai bao trong khoi #if dung nen tang - IL2CPP sinh tham
 *      chieu ke ca khi khong goi, thieu #if la Android/iOS hong luc lien ket.
 *   3. Tilemap.loadMap hong thi lui ve ban do 0 nhung KHONG tu goi mai khi chinh ban do 0 hong
 *      (StackOverflow), va giu noi dung ngoai le trong nhat ky.
 *   4. Out ghi nhat ky that (LogError luon ghi), nhung KHONG ghi mat khau dang nhap.
 *   5. Kich ban build (Editor/BuildCli.cs) tu thoat voi ma loi - batchmode Debug.LogError KHONG
 *      lam Unity tra ma khac 0.
 *   6. manifest.json khong them goi tu kho tren mang ngoai danh sach da duyet.
 *   7. Bo tai nguyen la ban GOC TeaMobi (inv_1 190x208), khong phai ban J2ME cung ten khac co.
 */
public final class KiemClientUnity {

    private static final String GOC = "../client_pc_ios/";
    private static final String MA = GOC + "Assets/Scripts/Assembly-CSharp/";
    private static final String F_DO_HOA = MA + "MyGraphics.cs";
    private static final String F_BAN_DO = MA + "Tilemap.cs";
    private static final String F_MAIN = MA + "Main.cs";
    private static final String F_NHAT_KY = MA + "Out.cs";
    private static final String F_DANG_NHAP = MA + "LoginScr.cs";
    private static final String F_XAY = GOC + "Assets/Editor/BuildCli.cs";
    private static final String F_MANIFEST = GOC + "Packages/manifest.json";
    private static final String F_PHIEN_BAN = GOC + "ProjectSettings/ProjectVersion.txt";
    private static final String F_BO_QUA = GOC + ".gitignore";

    /** Goi ngoai com.unity.modules.* duoc phep (2d.* di kem ban cai Unity; ide.visualstudio cho .vscode). */
    private static final String[] GOI_DUOC_PHEP = {
        "\"com.unity.2d.sprite\"", "\"com.unity.2d.tilemap\"", "\"com.unity.ide.visualstudio\"",
    };

    public static void chay() {
        kiemShader();
        kiemNative();
        kiemBanDoKhongTuGoiMai();
        kiemToaDoChuot();
        kiemNhatKy();
        kiemKichBanXay();
        kiemManifest();
        kiemTaiNguyenGoc();
        kiemBoQua();
        kiemCauTruc();
    }

    private static void kiemShader() {
        Kiem.nhom("Client Unity - shader ve duong");
        final String ma = boChuThich(doc(F_DO_HOA));
        Kiem.dung("doc duoc " + F_DO_HOA, ma.length() > 0);
        Kiem.sai("KHONG tao Material tu chuoi ma shader", ma.contains("new Material(\"Shader"));
        Kiem.dung("dung shader dung san Hidden/Internal-Colored",
                ma.contains("Shader.Find(\"Hidden/Internal-Colored\")"));
        // Shader dung san mac dinh KHAC shader cu: phai dat lai du ca bon trang thai.
        Kiem.dung("dat lai che do tron nguon", ma.contains("_SrcBlend"));
        Kiem.dung("dat lai che do tron dich", ma.contains("_DstBlend"));
        Kiem.dung("dat lai Cull (shader cu: Cull Off)", ma.contains("_Cull"));
        Kiem.dung("dat lai ZWrite (shader cu: ZWrite Off)", ma.contains("_ZWrite"));
    }

    /** Moi [DllImport] (ngoai chu thich) phai nam trong khoi #if ... #endif. */
    private static void kiemNative() {
        Kiem.nhom("Client Unity - ham native chi trong khoi #if nen tang");
        final File[] ds = new File(MA).listFiles();
        Kiem.dung("co thu muc ma " + MA, ds != null && ds.length > 100);
        int soTep = 0;
        if (ds != null) {
            for (final File f : ds) {
                if (!f.getName().endsWith(".cs")) {
                    continue;
                }
                final String ma = boChuThich(doc(f.getPath()));
                if (!ma.contains("[DllImport(")) {
                    continue;
                }
                soTep++;
                boolean ngoai = false;
                int sau = 0;
                for (final String dong : ma.split("\n")) {
                    final String t = dong.trim();
                    if (t.startsWith("#if")) {
                        sau++;
                    } else if (t.startsWith("#endif")) {
                        sau--;
                    } else if (t.contains("[DllImport(") && sau <= 0) {
                        ngoai = true;
                    }
                }
                Kiem.sai(f.getName() + ": khong co [DllImport] ngoai khoi #if", ngoai);
            }
        }
        System.out.println("    (" + soTep + " tep co [DllImport])");
        Kiem.sai("iOSPlugins.cs khong con khai bao native (ban C# rong)",
                boChuThich(doc(MA + "iOSPlugins.cs")).contains("[DllImport("));
    }

    private static void kiemBanDoKhongTuGoiMai() {
        Kiem.nhom("Client Unity - nap ban do khong tu goi minh mai");
        final String ma = boChuThich(doc(F_BAN_DO));
        Kiem.dung("doc duoc " + F_BAN_DO, ma.length() > 0);
        final int viTriCatch = ma.lastIndexOf("LOAD TILE ERR");
        final String khoiCatch = (viTriCatch < 0) ? ""
                : ma.substring(viTriCatch, Math.min(ma.length(), viTriCatch + 400));
        Kiem.dung("tim thay khoi bat loi cua loadMap", viTriCatch >= 0);
        Kiem.dung("loi goi lai loadMap(0) nam trong dieu kien level != 0",
                khoiCatch.indexOf("if (level != 0)") >= 0
                        && khoiCatch.indexOf("if (level != 0)") < khoiCatch.indexOf("loadMap(0, null)"));
        Kiem.dung("khoi bat loi giu noi dung ngoai le",
                ma.contains("ex.Message") && ma.contains("ex.StackTrace"));
    }

    /** Main.checkInput doi toa do chuot: truc y phai dung scaleY (truoc day dung scaleX cho ca hai truc). */
    private static void kiemToaDoChuot() {
        Kiem.nhom("Client Unity - toa do chuot");
        final String main = boChuThich(doc(F_MAIN));
        Kiem.dung("doc duoc " + F_MAIN, main.length() > 0);
        Kiem.bang("ca 3 cho doi toa do y deu dung scaleY", 3,
                dem(main, "ScaleGUI.scaleY((float)Screen.height"));
        Kiem.sai("KHONG dung scaleX cho toa do y", main.contains("ScaleGUI.scaleX((float)Screen.height"));
    }

    private static void kiemNhatKy() {
        Kiem.nhom("Client Unity - kenh ghi nhat ky");
        final String out = boChuThich(doc(F_NHAT_KY));
        Kiem.dung("doc duoc " + F_NHAT_KY, out.length() > 0);
        for (final String ham : new String[] { "println", "Log", "LogError", "LogWarning" }) {
            final int vt = out.indexOf("public static void " + ham + "(string str)");
            final String than = (vt < 0) ? "" : out.substring(vt, Math.min(out.length(), vt + 160));
            Kiem.dung("Out." + ham + " co ghi ra that", than.contains("Debug.Log"));
        }
        final int vt = out.indexOf("public static void LogError(string str)");
        final String than = (vt < 0) ? "" : out.substring(vt, Math.min(out.length(), vt + 80));
        Kiem.sai("Out.LogError ghi ca ban phat hanh (khong chan isDebugBuild)", than.contains("isDebugBuild"));
        final String dn = boChuThich(doc(F_DANG_NHAP));
        Kiem.dung("doc duoc " + F_DANG_NHAP, dn.length() > 0);
        Kiem.sai("LoginScr KHONG ghi mat khau ra nhat ky", dn.contains("\"  pass \""));
    }

    private static void kiemKichBanXay() {
        Kiem.nhom("Client Unity - kich ban build (BuildCli)");
        final String ma = boChuThich(doc(F_XAY));
        Kiem.dung("doc duoc " + F_XAY, ma.length() > 0);
        Kiem.dung("co build Windows", ma.contains("public static void Win64()"));
        Kiem.dung("co build Android", ma.contains("public static void Android()"));
        Kiem.dung("co build iOS", ma.contains("public static void iOS()"));
        Kiem.dung("tu thoat voi ma loi khi that bai", ma.contains("EditorApplication.Exit(1)"));
        Kiem.dung("ma thoat theo ket qua build", ma.contains("BuildResult.Succeeded ? 0 : 1"));
    }

    private static void kiemManifest() {
        Kiem.nhom("Client Unity - manifest goi");
        final String js = doc(F_MANIFEST);
        Kiem.dung("doc duoc " + F_MANIFEST, js.length() > 0);
        int soPhuThuoc = 0;
        boolean chiGoiDaDuyet = true;
        for (final String dong : js.split("\n")) {
            final String t = dong.trim();
            if (!t.startsWith("\"com.") && !t.startsWith("\"unity")) {
                continue;
            }
            soPhuThuoc++;
            if (t.startsWith("\"com.unity.modules.")) {
                continue;
            }
            boolean duyet = false;
            for (final String g : GOI_DUOC_PHEP) {
                duyet |= t.startsWith(g);
            }
            if (!duyet) {
                chiGoiDaDuyet = false;
                System.out.println("    goi ngoai danh sach: " + t);
            }
        }
        Kiem.dung("co khai bao phu thuoc", soPhuThuoc > 0);
        Kiem.dung("chi dung module dung san + goi da duyet", chiGoiDaDuyet);
    }

    /**
     * Bo J2ME co anh CUNG TEN ma KHAC KICH THUOC voi ban Unity goc (inv_1: goc 190x208, J2ME 125x116):
     * lay nham bo la so tiem nang lech khoi ten thuoc tinh. Client that phai giu anh GOC.
     */
    private static void kiemTaiNguyenGoc() {
        Kiem.nhom("Client Unity - tai nguyen ban goc");
        final File inv = new File(GOC + "Assets/Resources/res/inv_1.bytes");
        Kiem.dung("co res/inv_1", inv.isFile());
        final int[] co = doCoPng(inv);
        Kiem.bang("inv_1 la ban GOC 190 rong", 190, co[0]);
        Kiem.bang("inv_1 la ban GOC 208 cao", 208, co[1]);
        Kiem.dung("co anh nut dang bam res/bar1", new File(GOC + "Assets/Resources/res/bar1.bytes").isFile());
    }

    private static void kiemBoQua() {
        Kiem.nhom("Client Unity - khong commit rac");
        final String bq = doc(F_BO_QUA);
        Kiem.dung("doc duoc " + F_BO_QUA, bq.length() > 0);
        // Library/ mot minh nang hang tram MB; Build/ chua ban .exe.
        for (final String d : new String[] { "/Library/", "/Temp/", "/Logs/", "/UserSettings/", "/obj/", "/Build/", "/Builds/" }) {
            Kiem.dung("bo qua " + d, bq.contains(d));
        }
    }

    private static void kiemCauTruc() {
        Kiem.nhom("Client Unity - cau truc thu muc");
        Kiem.dung("ma game nam duoi Assets/", new File(MA + "Main.cs").isFile());
        Kiem.dung("kich ban build nam trong Assets/Editor/ (bat buoc voi ma Editor)", new File(F_XAY).isFile());
        Kiem.dung("co manifest goi", new File(F_MANIFEST).isFile());
        final String pb = doc(F_PHIEN_BAN);
        Kiem.dung("Unity 6000.x (ProjectVersion.txt)", pb.contains("m_EditorVersion: 6000."));
        // Mot ban ma nguon duy nhat: ban sao thu hai la sua mot ben khong sang ben kia.
        Kiem.sai("khong con ban port cu client_pc_ios/ClientUnity/", new File(GOC + "ClientUnity").exists());
        Kiem.sai("khong con thu muc Client/ cu", new File("Client").isDirectory());
    }

    /** Doc rong/cao tu phan IHDR cua tep PNG. Tra {-1,-1} neu khong phai PNG. */
    private static int[] doCoPng(final File tep) {
        try {
            final byte[] d = new byte[33];
            final FileInputStream in = new FileInputStream(tep);
            try {
                int da = 0;
                while (da < d.length) {
                    final int n = in.read(d, da, d.length - da);
                    if (n < 0) {
                        return new int[] { -1, -1 };
                    }
                    da += n;
                }
            } finally {
                in.close();
            }
            if ((d[0] & 0xFF) != 0x89 || d[1] != 'P' || d[2] != 'N' || d[3] != 'G') {
                return new int[] { -1, -1 };
            }
            return new int[] { doI32(d, 16), doI32(d, 20) };
        } catch (final Exception e) {
            return new int[] { -1, -1 };
        }
    }

    private static int doI32(final byte[] d, final int i) {
        return ((d[i] & 0xFF) << 24) | ((d[i + 1] & 0xFF) << 16)
                | ((d[i + 2] & 0xFF) << 8) | (d[i + 3] & 0xFF);
    }

    private static int dem(final String trong, final String mau) {
        int n = 0;
        int i = trong.indexOf(mau);
        while (i >= 0) {
            n++;
            i = trong.indexOf(mau, i + mau.length());
        }
        return n;
    }

    // -----------------------------------------------------------------------
    private static String boChuThich(final String ma) {
        final StringBuilder sb = new StringBuilder(ma.length());
        boolean trongKhoi = false;
        for (final String dong : ma.split("\n", -1)) {
            String t = dong;
            if (trongKhoi) {
                final int het = t.indexOf("*/");
                if (het < 0) {
                    continue;
                }
                t = t.substring(het + 2);
                trongKhoi = false;
            }
            final int mo = t.indexOf("/*");
            if (mo >= 0) {
                final int het = t.indexOf("*/", mo + 2);
                if (het >= 0) {
                    t = t.substring(0, mo) + t.substring(het + 2);
                } else {
                    t = t.substring(0, mo);
                    trongKhoi = true;
                }
            }
            final int hai = t.indexOf("//");
            if (hai >= 0) {
                t = t.substring(0, hai);
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder((int) f.length() + 16);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
        } catch (final Exception ex) {
            return "";
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    private KiemClientUnity() {
    }
}
