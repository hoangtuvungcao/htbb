import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Dong goi sprite PMD (tools/pokemon/pmd_raw/<khoa>/) thanh tai nguyen client
 * <Assets>/Resources/poke/<khoa>_{anh,dl,cd}.bytes. Assets chinh la client PC htbb-client/pc/Assets
 * (repo canh HTBB/; doi bang bien HTBB_CLIENT_ASSETS); con HTBB/ClientUnity/Assets (ban cu, sap xoa) thi
 * chep sang ca do (bo bang HTBB_CLIENT_CU=0).
 *
 * Dinh dang: docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 7.1 (phien ban 2: them xAtlas). Sau khi ghi, tool DOC LAI
 * tung file .bytes va doi chieu voi AnimData.xml + anh goc (so sanh tung pixel cua
 * khung dau va khung cuoi moi huong) - lech la thoat ma 1.
 *
 * Chay (o thu muc HTBB/, can JDK):
 *   javac -d build/tools tools/pokemon/DongGoiSprite.java
 *   java -cp build/tools DongGoiSprite
 *
 * Thu tu dong tac + uu tien thay the PHAI giong DONG_TAC trong tai_sprite_pmd.py.
 */
public final class DongGoiSprite {

    static final String[][] DONG_TAC = {
        {"Idle", "Walk"},
        {"Walk"},
        {"Attack", "Strike", "QuickStrike", "Swing"},
        {"Shoot", "Charge", "Attack"},
        {"Hurt", "Pain"},
        {"Faint", "Sleep", "Hurt"},
    };

    /** Hang PMD cho huong client DOWN, UP, LEFT, RIGHT. */
    static final int[] HANG_PMD = {0, 4, 6, 2};

    /** Chieu cao toi da moi cot atlas: nhieu may (GPU cu, WebGL1) gioi han texture 2048. */
    static final int CAO_TOI_DA = 2048;

    /** Dong danh dau khoi ghi cong cua tai_hieu_ung_pmd.py trong GHI_CONG.txt. */
    static final String DAU_KHOI_HIEU_UNG = "== HIEU UNG CHIEU (tai_hieu_ung_pmd.py) ==";
    static final String CUOI_KHOI_HIEU_UNG = "== HET HIEU UNG CHIEU ==";

    /** Phien ban dinh dang _dl: 2 = co them u16 xAtlas sau yAtlas (atlas nhieu cot). */
    static final int PHIEN_BAN = 2;

    /** Xep theo cot doc, giu thu tu dong tac. Tra ve false neu vuot gioi han. */
    static boolean xepCot(final DongTac[] dt) {
        int y = 0;
        int xCot = 0;
        int rongCot = 0;
        for (final DongTac t : dt) {
            final int cao = 4 * t.anim.cao;
            final int rong = t.anim.thoiLuong.length * t.anim.rong;
            if (y + cao > CAO_TOI_DA) {
                xCot += rongCot;
                rongCot = 0;
                y = 0;
            }
            t.x = xCot;
            t.y = y;
            y += cao;
            rongCot = Math.max(rongCot, rong);
            if (t.x + rong > CAO_TOI_DA) {
                return false;
            }
        }
        return true;
    }

    /** Xep theo ke ngang, dong tac cao truoc. Tra ve false neu vuot gioi han. */
    static boolean xepKe(final DongTac[] dt) {
        final DongTac[] thuTu = dt.clone();
        Arrays.sort(thuTu, (p, q) -> Integer.compare(q.anim.cao, p.anim.cao));
        int x = 0;
        int yKe = 0;
        int caoKe = 0;
        for (final DongTac t : thuTu) {
            final int cao = 4 * t.anim.cao;
            final int rong = t.anim.thoiLuong.length * t.anim.rong;
            if (x + rong > CAO_TOI_DA) {
                yKe += caoKe;
                caoKe = 0;
                x = 0;
            }
            t.x = x;
            t.y = yKe;
            x += rong;
            caoKe = Math.max(caoKe, cao);
            if (t.y + cao > CAO_TOI_DA) {
                return false;
            }
        }
        return true;
    }

    /** Hang nguon cho huong client h: anh 1 hang thi moi huong dung hang 0. */
    static int hangNguon(final DongTac t, final int h) {
        return t.soHang == 1 ? 0 : HANG_PMD[h];
    }

    static final class Anim {
        String ten;
        int rong;
        int cao;
        int[] thoiLuong;
    }

    static final class DongTac {
        Anim anim;
        boolean that;
        int y;
        BufferedImage nguon;
        int x;
        int soHang;
        byte[] dx = new byte[4];
        byte[] dy = new byte[4];
    }

    public static void main(final String[] args) throws Exception {
        final File goc = new File(".").getCanonicalFile();
        final File raw = new File(goc, "tools/pokemon/pmd_raw");
        final List<File> cacRa = thuMucRa(goc);
        final File ra = cacRa.get(0);
        if (!raw.isDirectory()) {
            System.err.println("Khong co " + raw + " - chay tools/pokemon/tai_sprite_pmd.py truoc");
            System.exit(1);
        }
        ra.mkdirs();
        final String[] khoa = raw.list();
        Arrays.sort(khoa);
        int loi = 0;
        int so = 0;
        int caoMax = 0;
        String khoaCaoMax = "";
        final StringBuilder ghiCong = new StringBuilder();
        ghiCong.append("Sprite Pokemon: PMDCollab SpriteCollab - https://github.com/PMDCollab/SpriteCollab\n");
        ghiCong.append("Giay phep: CC BY-NC 4.0 (KHONG thuong mai). Nguoi ve theo tung loai ben duoi\n");
        ghiCong.append("(cot: thoi diem, nguoi ve, trang thai, giay phep, dong tac). Sinh boi DongGoiSprite.\n\n");
        for (final String k : khoa) {
            final File tm = new File(raw, k);
            if (!new File(tm, "AnimData.xml").isFile()) {
                continue;
            }
            try {
                final int cao = dongGoi(tm, k, ra);
                if (cao > caoMax) {
                    caoMax = cao;
                    khoaCaoMax = k;
                }
                so++;
            } catch (final Exception e) {
                loi++;
                System.out.println("LOI " + k + ": " + e.getMessage());
            }
            final File cr = new File(tm, "credits.txt");
            ghiCong.append("== ").append(k).append('\n');
            if (cr.isFile()) {
                ghiCong.append(new String(Files.readAllBytes(cr.toPath()), StandardCharsets.UTF_8).trim()).append('\n');
            } else {
                loi++;
                System.out.println("LOI " + k + ": thieu credits.txt");
            }
        }
        // Giu nguyen khoi ghi cong hieu ung chieu do tools/pokemon/tai_hieu_ung_pmd.py ghi them
        // (giua hai dong danh dau), neu khong chay lai tool nay se xoa mat ghi cong particle.
        final File fGhiCong = new File(ra, "GHI_CONG.txt");
        if (fGhiCong.isFile()) {
            final String cu = new String(Files.readAllBytes(fGhiCong.toPath()), StandardCharsets.UTF_8);
            final int a = cu.indexOf(DAU_KHOI_HIEU_UNG);
            final int b = cu.indexOf(CUOI_KHOI_HIEU_UNG);
            if (a >= 0 && b > a) {
                ghiCong.append('\n').append(cu, a, b + CUOI_KHOI_HIEU_UNG.length()).append('\n');
            }
        }
        Files.write(fGhiCong.toPath(), ghiCong.toString().getBytes(StandardCharsets.UTF_8));
        for (int i = 1; i < cacRa.size(); i++) {
            chepSang(ra, cacRa.get(i));
        }
        System.out.println("Da dong goi " + so + " loai -> " + cacRa + " (atlas cao nhat " + caoMax + " px: " + khoaCaoMax + "), loi: " + loi);
        System.exit(loi == 0 ? 0 : 1);
    }

    static Map<String, Anim> docAnimData(final File xml) throws Exception {
        final Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        final NodeList ds = d.getElementsByTagName("Anim");
        final Map<String, Element> tho = new LinkedHashMap<String, Element>();
        for (int i = 0; i < ds.getLength(); i++) {
            final Element e = (Element) ds.item(i);
            tho.put(text(e, "Name"), e);
        }
        final Map<String, Anim> kq = new LinkedHashMap<String, Anim>();
        for (final String ten : tho.keySet()) {
            Element e = tho.get(ten);
            String tenAnh = ten;
            for (int i = 0; i < 10 && e != null && text(e, "CopyOf") != null; i++) {
                tenAnh = text(e, "CopyOf");
                e = tho.get(tenAnh);
            }
            if (e == null) {
                continue;
            }
            final Anim a = new Anim();
            a.ten = tenAnh;
            a.rong = Integer.parseInt(text(e, "FrameWidth"));
            a.cao = Integer.parseInt(text(e, "FrameHeight"));
            final NodeList du = e.getElementsByTagName("Duration");
            a.thoiLuong = new int[du.getLength()];
            for (int j = 0; j < du.getLength(); j++) {
                a.thoiLuong[j] = Integer.parseInt(du.item(j).getTextContent().trim());
            }
            kq.put(ten, a);
        }
        return kq;
    }

    static String text(final Element e, final String tag) {
        final NodeList n = e.getElementsByTagName(tag);
        return n.getLength() == 0 ? null : n.item(0).getTextContent().trim();
    }

    /** Ghi 3 file cho mot loai, doc lai doi chieu. Tra ve chieu cao atlas. */
    static int dongGoi(final File tm, final String k, final File ra) throws Exception {
        final Map<String, Anim> anims = docAnimData(new File(tm, "AnimData.xml"));
        final DongTac[] dt = new DongTac[DONG_TAC.length];
        int rongAtlas = 1;
        int caoAtlas = 1;
        for (int i = 0; i < DONG_TAC.length; i++) {
            DongTac t = null;
            for (int j = 0; j < DONG_TAC[i].length; j++) {
                final Anim a = anims.get(DONG_TAC[i][j]);
                if (a != null) {
                    t = new DongTac();
                    t.anim = a;
                    t.that = j == 0;
                    break;
                }
            }
            if (t == null) {
                throw new IOException("thieu dong tac " + i);
            }
            if (t.anim.thoiLuong.length > 255) {
                throw new IOException("qua 255 khung: " + t.anim.ten);
            }
            t.nguon = ImageIO.read(new File(tm, t.anim.ten + "-Anim.png"));
            if (t.nguon == null) {
                throw new IOException("khong doc duoc " + t.anim.ten + "-Anim.png");
            }
            final int n = t.anim.thoiLuong.length;
            // PMD: dong tac thuong co 8 hang huong, mot so (Sleep, EventSleep...) chi 1 hang
            t.soHang = t.nguon.getHeight() / t.anim.cao;
            if (t.nguon.getWidth() < n * t.anim.rong || (t.soHang != 8 && t.soHang != 1)) {
                throw new IOException(t.anim.ten + ": anh " + t.nguon.getWidth() + "x" + t.nguon.getHeight()
                        + " khong khop " + n + " khung " + t.anim.rong + "x" + t.anim.cao + " (can 1 hoac 8 hang)");
            }
            tinhChan(t, new File(tm, t.anim.ten + "-Shadow.png"));
            if (4 * t.anim.cao > CAO_TOI_DA || n * t.anim.rong > CAO_TOI_DA) {
                throw new IOException(t.anim.ten + ": mot dong tac da vuot " + CAO_TOI_DA + " px");
            }
            dt[i] = t;
        }
        // Xep atlas: thu xep theo cot (giu thu tu, de doc) roi xep ke (cao giam dan);
        // lay cach dau tien ma ca hai chieu <= CAO_TOI_DA.
        if (!xepCot(dt) && !xepKe(dt)) {
            throw new IOException("khong xep duoc atlas trong " + CAO_TOI_DA + "x" + CAO_TOI_DA);
        }
        for (final DongTac t : dt) {
            rongAtlas = Math.max(rongAtlas, t.x + t.anim.thoiLuong.length * t.anim.rong);
            caoAtlas = Math.max(caoAtlas, t.y + 4 * t.anim.cao);
        }
        final BufferedImage atlas = new BufferedImage(rongAtlas, caoAtlas, BufferedImage.TYPE_INT_ARGB);
        for (final DongTac t : dt) {
            final int n = t.anim.thoiLuong.length;
            for (int h = 0; h < 4; h++) {
                for (int f = 0; f < n; f++) {
                    for (int py = 0; py < t.anim.cao; py++) {
                        for (int px = 0; px < t.anim.rong; px++) {
                            final int argb = t.nguon.getRGB(f * t.anim.rong + px, hangNguon(t, h) * t.anim.cao + py);
                            atlas.setRGB(t.x + f * t.anim.rong + px, t.y + h * t.anim.cao + py, argb);
                        }
                    }
                }
            }
        }
        final ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(atlas, "png", png);
        final byte[] dl = ghiDuLieu(dt);
        Files.write(new File(ra, k + "_anh.bytes").toPath(), png.toByteArray());
        Files.write(new File(ra, k + "_dl.bytes").toPath(), dl);
        final File cd = new File(tm, "portrait.png");
        if (!cd.isFile()) {
            throw new IOException("thieu portrait.png");
        }
        Files.copy(cd.toPath(), new File(ra, k + "_cd.bytes").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        kiemLai(k, ra, dt);
        return caoAtlas;
    }

    /**
     * Diem chan moi huong = tam diem anh khac trong suot trong khung 0 cua *-Shadow.png
     * (PMD ve bong dung cho chan dat xuong), tinh so voi tam khung. Khong co bong -> (0, cao/4).
     */
    static void tinhChan(final DongTac t, final File bong) throws IOException {
        final BufferedImage b = bong.isFile() ? ImageIO.read(bong) : null;
        for (int h = 0; h < 4; h++) {
            long sx = 0;
            long sy = 0;
            int dem = 0;
            if (b != null) {
                final int y0 = hangNguon(t, h) * t.anim.cao;
                for (int py = 0; py < t.anim.cao && y0 + py < b.getHeight(); py++) {
                    for (int px = 0; px < t.anim.rong && px < b.getWidth(); px++) {
                        if ((b.getRGB(px, y0 + py) >>> 24) != 0) {
                            sx += px;
                            sy += py;
                            dem++;
                        }
                    }
                }
            }
            int dx = 0;
            int dy = t.anim.cao / 4;
            if (dem > 0) {
                dx = (int) Math.round((double) sx / dem - t.anim.rong / 2.0);
                dy = (int) Math.round((double) sy / dem - t.anim.cao / 2.0);
            }
            t.dx[h] = (byte) Math.max(-128, Math.min(127, dx));
            t.dy[h] = (byte) Math.max(-128, Math.min(127, dy));
        }
    }

    static byte[] ghiDuLieu(final DongTac[] dt) throws IOException {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream o = new DataOutputStream(bo);
        o.writeByte(PHIEN_BAN);
        o.writeByte(dt.length);
        for (final DongTac t : dt) {
            o.writeShort(t.anim.rong);
            o.writeShort(t.anim.cao);
            o.writeByte(t.anim.thoiLuong.length);
            for (final int d : t.anim.thoiLuong) {
                o.writeByte(Math.max(1, Math.min(255, d)));
            }
            o.writeShort(t.y);
            o.writeShort(t.x);
            o.write(t.dx);
            o.write(t.dy);
        }
        for (final DongTac t : dt) {
            o.writeByte(t.that ? 1 : 0);
        }
        o.flush();
        return bo.toByteArray();
    }

    /** Doc lai file vua ghi theo DUNG dac ta 7.1, so voi du lieu goc. */
    /** Resources/poke cua cac client can ghi: HTBB_CLIENT_ASSETS, hoac htbb-client/pc (+ ClientUnity cu neu con). */
    static List<File> thuMucRa(final File goc) throws IOException {
        final List<File> ds = new ArrayList<>();
        final String rieng = System.getenv("HTBB_CLIENT_ASSETS");
        if (rieng != null && !rieng.isEmpty()) {
            ds.add(new File(rieng, "Resources/poke"));
            return ds;
        }
        final File pc = new File(goc, "../htbb-client/pc/Assets").getCanonicalFile();
        if (pc.isDirectory()) {
            ds.add(new File(pc, "Resources/poke"));
        }
        final File cu = new File(goc, "client_pc_ios/ClientUnity/Assets");
        if (cu.isDirectory() && !"0".equals(System.getenv("HTBB_CLIENT_CU"))) {
            ds.add(new File(cu, "Resources/poke"));
        }
        if (ds.isEmpty()) {
            ds.add(new File(cu, "Resources/poke"));
        }
        return ds;
    }

    /** Chep cac tep .bytes + GHI_CONG.txt sang thu muc client khac (giu dong bo trong luc chuyen tiep). */
    static void chepSang(final File tu, final File den) throws IOException {
        den.mkdirs();
        final File[] tep = tu.listFiles();
        if (tep == null) {
            return;
        }
        for (final File t : tep) {
            final String ten = t.getName();
            if (t.isFile() && (ten.endsWith(".bytes") || ten.startsWith("GHI_CONG"))) {
                Files.copy(t.toPath(), new File(den, ten).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    static void kiemLai(final String k, final File ra, final DongTac[] dt) throws IOException {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                Files.readAllBytes(new File(ra, k + "_dl.bytes").toPath())));
        if (in.readUnsignedByte() != PHIEN_BAN) {
            throw new IOException("kiem lai: sai phien ban");
        }
        final int n = in.readUnsignedByte();
        if (n != dt.length) {
            throw new IOException("kiem lai: so dong tac " + n);
        }
        final BufferedImage atlas = ImageIO.read(new File(ra, k + "_anh.bytes"));
        for (int i = 0; i < n; i++) {
            final int w = in.readUnsignedShort();
            final int h = in.readUnsignedShort();
            final int f = in.readUnsignedByte();
            final DongTac t = dt[i];
            if (w != t.anim.rong || h != t.anim.cao || f != t.anim.thoiLuong.length) {
                throw new IOException("kiem lai: dong tac " + i + " lech kich thuoc");
            }
            for (int j = 0; j < f; j++) {
                in.readUnsignedByte();
            }
            final int y = in.readUnsignedShort();
            final int x = in.readUnsignedShort();
            if (x != t.x || y != t.y) {
                throw new IOException("kiem lai: dong tac " + i + " lech vi tri atlas");
            }
            if (y + 4 * h > atlas.getHeight() || x + f * w > atlas.getWidth() || atlas.getHeight() > CAO_TOI_DA) {
                throw new IOException("kiem lai: dong tac " + i + " tran atlas");
            }
            in.skipBytes(8);
            for (int huong = 0; huong < 4; huong++) {
                for (final int khung : new int[] {0, f - 1}) {
                    for (int py = 0; py < h; py += 3) {
                        for (int px = 0; px < w; px += 3) {
                            final int a = atlas.getRGB(x + khung * w + px, y + huong * h + py);
                            final int b = t.nguon.getRGB(khung * w + px, hangNguon(t, huong) * h + py);
                            if (a != b && ((a >>> 24) != 0 || (b >>> 24) != 0)) {
                                throw new IOException("kiem lai: lech pixel dong tac " + i + " huong " + huong);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            in.readUnsignedByte();
        }
        if (in.available() != 0) {
            throw new IOException("kiem lai: thua " + in.available() + " byte");
        }
    }
}
