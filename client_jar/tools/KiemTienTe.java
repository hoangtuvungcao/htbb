import game.pokemon.TienTe;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm TIỀN TỆ (0.9.3):
 *  1. game.pokemon.TienTe: so() "1.250.000", gon() "1,25 Tr" / "12,5 N", chu() "20 lượng khoá", chú thích, ảnh
 *     biểu tượng 16/24/32 đủ (client_jar/res/tien) + bản web 32/64 (web/public/tien).
 *  2. KHÔNG còn chữ viết tắt tiền tệ khó hiểu trong chuỗi giao diện: "$" (xu), "l" / "L" / "lk" / "LK" / "lg" (lượng,
 *     lượng khoá), tiền tố "xu:" / "lg:" / "lk:" - quét chuỗi trong client_jar/src, client_jar/shared/mod, server/src
 *     (bỏ nhật ký / SQL), lớp nền PC / APK, client_pc_ios (C#) và web/app (Vue / TS). Chỗ có chữ thuần dùng tên đầy đủ (TienTe.ten).
 *
 *   sh client_pc/build.sh    # lớp game + MIDP cho Java SE (client_pc/out/build/cls)
 *   O=$(mktemp -d); C=client_pc/out/build/cls
 *   javac -cp $C -d $O client_jar/src/game/pokemon/TienTe.java client_jar/tools/KiemTienTe.java
 *   java -Djava.awt.headless=true -cp $O:$C:client_pc/out/build/res KiemTienTe .    # (gốc monorepo) thoát 1 = hỏng
 */
public final class KiemTienTe {
    static int dat, hong;

    static void bang(String mo, Object mong, Object that) {
        if (mong.equals(that)) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG " + mo + ": mong [" + mong + "] thuc te [" + that + "]");
        }
    }

    static void dung(String mo, boolean v) {
        bang(mo, Boolean.TRUE, Boolean.valueOf(v));
    }

    public static void main(String[] a) throws Exception {
        File goc = new File(a.length > 0 ? a[0] : ".");
        kiemSo();
        kiemAnh(goc);
        kiemVietTat(goc);
        System.out.println("KiemTienTe: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }

    static void kiemSo() {
        bang("so 0", "0", TienTe.so(0));
        bang("so 999", "999", TienTe.so(999));
        bang("so 1000", "1.000", TienTe.so(1000));
        bang("so 1250000", "1.250.000", TienTe.so(1250000));
        bang("so am", "-5.300", TienTe.so(-5300));
        bang("so long", "9.223.372.036.854.775.807", TienTe.so(Long.MAX_VALUE));
        bang("gon 999", "999", TienTe.gon(999));
        bang("gon 9999", "9.999", TienTe.gon(9999));
        bang("gon 12500", "12,5 N", TienTe.gon(12500));
        bang("gon 12000", "12 N", TienTe.gon(12000));
        bang("gon 250000", "250 N", TienTe.gon(250000));
        bang("gon 1250000", "1,25 Tr", TienTe.gon(1250000));
        bang("gon 1050000", "1,05 Tr", TienTe.gon(1050000));
        bang("gon 1999999 (cat, khong lam tron len)", "1,99 Tr", TienTe.gon(1999999));
        bang("gon 250000000", "250 Tr", TienTe.gon(250000000));
        bang("gon 3200000000", "3,2 Tỉ", TienTe.gon(3200000000L));
        bang("gon am", "-12,5 N", TienTe.gon(-12500));
        bang("ten xu", "xu", TienTe.ten(TienTe.XU));
        bang("ten luong", "lượng", TienTe.ten(TienTe.LUONG));
        bang("ten luong khoa", "lượng khoá", TienTe.ten(TienTe.LUONG_KHOA));
        bang("chu", "20 lượng khoá", TienTe.chu(TienTe.LUONG_KHOA, 20));
        bang("chuGia", "1.000 xu - 5 lượng", TienTe.chuGia(1000, 5));
        bang("chuGia chi xu", "1.000 xu", TienTe.chuGia(1000, 0));
        bang("goi y xu", "Xu: tiền chơi, kiếm từ quái / nhiệm vụ / bán đồ", TienTe.goiY(TienTe.XU));
        dung("goi y luong khoa noi khong giao dich", TienTe.goiY(TienTe.LUONG_KHOA).indexOf("không giao dịch") > 0);
        TienTe.hienGoiY(TienTe.XU, 1250000, 10, 10);
        dung("dang hien chu thich", TienTe.dangGoiY());
        bang("chu thich kem so day du", "Xu: 1.250.000 - tiền chơi, kiếm từ quái / nhiệm vụ / bán đồ", TienTe.chuGoiY());
        for (int l = 0; l < TienTe.SO_LOAI; l++) {
            dung("loai " + l + " co ten + chu thich", TienTe.ten(l).length() > 0 && TienTe.goiY(l).length() > 0);
        }
    }

    static final String[] ANH = {"xu", "luong", "luong_khoa", "su_kien", "vinh_du"};

    static void kiemAnh(File goc) throws Exception {
        for (int i = 0; i < ANH.length; i++) {
            for (int c : new int[]{16, 24, 32}) {
                File f = new File(goc, "client_jar/res/tien/" + ANH[i] + "_" + c + ".png");
                dung("anh " + f.getPath(), f.isFile());
                if (f.isFile()) {
                    java.awt.image.BufferedImage b = javax.imageio.ImageIO.read(f);
                    dung("anh " + f.getName() + " dung co", b != null && b.getWidth() == c && b.getHeight() == c);
                }
            }
            for (int c : new int[]{32, 64}) {
                dung("anh web " + ANH[i] + "_" + c, new File(goc, "web/public/tien/" + ANH[i] + "_" + c + ".png").isFile());
            }
        }
    }

    // ------------------------------------------------------------------ quét chữ viết tắt

    /** chuỗi trong mã Java / C# / TS ("..." hoặc '...') */
    static final Pattern CHUOI = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"|'((?:[^'\\\\\\n]|\\\\.)*)'");
    /** "$" như đơn vị tiền: sau số / cách / đầu chuỗi, trước cách / hết chuỗi / ")" (không bắt $1 của regex, ${} của JS) */
    static final Pattern DO_LA = Pattern.compile("(^|[\\s\\d])\\$(?=$|[\\s),.!?])");
    /** lk / LK / lg / Lg như đơn vị (từ đứng riêng); web: chỉ lk (lg là cỡ / mốc màn hình của Tailwind / Nuxt UI) */
    static final Pattern VIET_TAT = Pattern.compile("(^|[\\s,\\d(])(lk|LK|Lk|lg|LG|Lg)(?=$|[\\s:.,)!?])");
    static final Pattern VIET_TAT_WEB = Pattern.compile("(^|[\\s,\\d(])(lk|LK|Lk)(?=$|[\\s:.,)!?])");
    /** tiền tố "xu:" / "lg:" / "lk:" ngay trước số (kiểu "Xu: 1.000"); "Số lượng:" = số lượng món, không phải tiền */
    static final Pattern TIEN_TO = Pattern.compile("(?i)(^|[\\s,(])(xu|lg|lk):\\s*$");
    /** đuôi tên miền (danh sách lọc tên miền trong chat) */
    static final Pattern TEN_MIEN = Pattern.compile("^\\. ?[a-z]{2,3}$");
    /** dòng không phải giao diện (nhật ký, SQL, regex, thông số) */
    static final Pattern KHONG_UI = Pattern.compile("saveOrtherLog|System\\.out|System\\.err|Cout\\.|Nhat\\.|NhatKy|Log\\.[deiw]|console\\.|"
            + "executeQuery|executeUpdate|prepareStatement|SELECT |UPDATE |INSERT |DELETE |Pattern\\.|\\.matches\\(|replaceAll|\\.mau\\(|"
            + "log\\(|logger|ghiLog|Database\\.instance\\.save|getProperty\\(");

    static final List<String> LOI = new ArrayList<String>();

    static void kiemVietTat(File goc) throws Exception {
        quet(new File(goc, "client_jar/src"), ".java", false);
        quet(new File(goc, "client_jar/shared/mod"), ".java", false);
        quet(new File(goc, "server/src"), ".java", true);
        quet(new File(goc, "client_pc/src/htbb"), ".java", false);
        quet(new File(goc, "client_apk/pokemon/src/main/java/com"), ".java", false);
        quet(new File(goc, "client_pc_ios/Assets/Scripts/Assembly-CSharp"), ".cs", false);
        quet(new File(goc, "web/app"), ".vue", false);
        quet(new File(goc, "web/app"), ".ts", false);
        for (int i = 0; i < LOI.size() && i < 60; i++) {
            System.out.println("  viet tat: " + LOI.get(i));
        }
        bang("so chuoi giao dien con chu viet tat tien te", Integer.valueOf(0), Integer.valueOf(LOI.size()));
    }

    static void quet(File d, String duoi, boolean mayChu) throws Exception {
        File[] ds = d.listFiles();
        if (ds == null) {
            return;
        }
        for (File f : ds) {
            if (f.isDirectory()) {
                quet(f, duoi, mayChu);
            } else if (f.getName().endsWith(duoi)) {
                quetTep(f, mayChu);
            }
        }
    }

    static void quetTep(File f, boolean mayChu) throws Exception {
        String ten = f.getPath().replace('\\', '/');
        // tệp không phải giao diện: script PowerShell tự cập nhật (C#), bảng font (danh sách ký tự), bàn phím số
        if (ten.endsWith("/CapNhat.cs") || ten.endsWith("/Font.java") || ten.endsWith("/MFont.cs") || ten.endsWith("/TField.java")
                || ten.endsWith("/TField.cs") || ten.contains("/server/Panel/") || ten.contains("/quantri/") || ten.contains("/pages/admin/")
                || ten.contains("/components/admin/")) {
            return;
        }
        List<String> dong = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        boolean vue = ten.endsWith(".vue");
        boolean web = ten.contains("/web/");
        for (int i = 0; i < dong.size(); i++) {
            String s = dong.get(i);
            String t = s.trim();
            if (t.startsWith("//") || t.startsWith("*") || t.startsWith("/*") || t.startsWith("#") || t.startsWith("<!--")) {
                continue;
            }
            if (KHONG_UI.matcher(s).find()) {
                continue;
            }
            int cm = s.indexOf("// ");
            if (cm >= 0) {
                s = s.substring(0, cm);   // chú thích cuối dòng
            }
            List<String> ds = new ArrayList<String>();
            Matcher m = CHUOI.matcher(s);
            while (m.find()) {
                ds.add(m.group(1) != null ? m.group(1) : m.group(2));
            }
            if (vue && !t.startsWith("const ") && !t.startsWith("import ")) {
                ds.add(s.replaceAll("\\{\\{[^}]*\\}\\}", " 0 ").replaceAll("<[^>]*>", " "));   // chữ trong template
            }
            boolean coTien = s.toLowerCase().contains("luong") || s.toLowerCase().contains("xu") || s.contains("lượng");
            for (String c : ds) {
                String lydo = null;
                if (DO_LA.matcher(c).find() && !c.contains("${") && !c.startsWith("^")) {
                    lydo = "\"$\" thay cho xu";
                } else if (TEN_MIEN.matcher(c).find()) {
                    lydo = null;
                } else if ((web ? VIET_TAT_WEB : VIET_TAT).matcher(c).find()) {
                    lydo = "viết tắt lk / lg";
                } else if (!mayChu && coTien && (c.equals("l") || c.equals("L") || c.equals("LK") || c.equals("lk"))) {
                    lydo = "hậu tố \"" + c + "\" thay cho lượng";
                } else if (!mayChu && TIEN_TO.matcher(c).find()) {
                    lydo = "tiền tố \"" + c.trim() + "\"";
                }
                if (lydo != null) {
                    LOI.add(ten + ":" + (i + 1) + " " + lydo + " -> " + t.substring(0, Math.min(140, t.length())));
                }
            }
        }
    }
}
