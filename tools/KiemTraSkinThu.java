import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Kiem tra bo anh skin linh thu bang cach LAM Y HET CLIENT.
 *
 * Doc map/skinthu/&lt;n&gt; + &lt;n&gt;.png roi chay lai dung thuat toan cua client:
 *
 *   Client/DataSkillEff.loadData        -> doc anh con + frame + part
 *   Client/Char.getFrameHorse           -> chon frame theo huong va Fhorse
 *   Client/Char.horseMove               -> Fhorse khi di chuyen
 *   Client/DataSkillEff.paintTopHorse   -> ve part onTop == 0
 *   Client/DataSkillEff.paintBottomHorse-> ve part onTop != 0
 *   Client/Char.dy_horse_tool[0]        -> muc nhac nguoi ngoi len
 *
 * Ket qua ghi ra map/skinthu_goc/&lt;n&gt;_nhutrongame.png: moi hang la mot huong, ve lai
 * dung tung khung hinh ma nguoi choi se thay, kem mot hinh nguoi gia lap dat dung cho
 * de biet nguoi se ngoi o dau tren lung con thu.
 *
 * Bat cac loi ma mat thuong khong thay:
 *   - thieu frame (client thoat som -> con thu bien mat mot tick, nhin nhu nhay giat)
 *   - toa do anh con tran ra ngoai atlas
 *   - dat sai onTop (nguoi ngoi bi con thu che het hoac nguoc lai)
 *   - con thu khong cham dat, hoac nguoi ngoi lo lung tren khong
 *
 *   javac -d out_tools tools/KiemTraSkinThu.java
 *   java -cp out_tools KiemTraSkinThu
 */
public class KiemTraSkinThu {

    /** Char.getFrameHorse: DOWN + 0, UP + 7, LEFT/RIGHT + 14. */
    private static final int[] GOC_HUONG = {0, 7, 14};
    private static final String[] TEN_HUONG = {"xuong", "len", "ngang"};
    /** Char.paint nhanh state 0: Fhorse = frame, frame bi chan ve 0..3. */
    private static final int[] DUNG_YEN = {0, 1, 2, 3};
    /** Char.horseMove = {2,2,2,3,3,4,4,4} -> cac gia tri Fhorse khac nhau khi di. */
    private static final int[] DI_CHUYEN = {2, 3, 4};
    /** Char.dy_horse_tool[0] = {-20,-15,-15,-15}: muc nhac nguoi ngoi, theo huong. */
    private static final int[] DY_NGUOI = {-20, -15, -15};
    /** Char.dx_horse_tool[0] = {0,0,7,-7}: le ngang cua nguoi ngoi, theo huong. */
    private static final int[] DX_NGUOI = {0, 0, 7};

    public static void main(final String[] args) throws Exception {
        int loi = 0;
        for (int n = 0; n < 16; n++) {
            final File data = new File("map/skinthu/" + n);
            final File png = new File("map/skinthu/" + n + ".png");
            if (!data.exists() || !png.exists()) {
                continue;
            }
            loi += kiem(n, data, png);
        }
        System.out.println(loi == 0 ? "\nTAT CA DEU DAT." : "\nCO " + loi + " LOI.");
        if (loi > 0) {
            System.exit(1);
        }
    }

    private static int kiem(final int n, final File data, final File png) throws Exception {
        System.out.println("===== skin " + n + " =====");
        final BufferedImage atlas = ImageIO.read(png);
        final byte[] b = java.nio.file.Files.readAllBytes(data.toPath());
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
        int loi = 0;

        // ---- doc y het Client/DataSkillEff.loadData ----
        final int soAnh = in.readByte();
        final int[][] anhCon = new int[soAnh][];
        for (int i = 0; i < soAnh; i++) {
            in.readUnsignedByte();                       // id, client doc roi bo qua
            anhCon[i] = new int[]{in.readUnsignedByte(), in.readUnsignedByte(),
                    in.readUnsignedByte(), in.readUnsignedByte()};
            final int[] a = anhCon[i];
            if (a[0] + a[2] > atlas.getWidth() || a[1] + a[3] > atlas.getHeight()) {
                System.out.println("  LOI: anh con " + i + " (" + a[0] + "," + a[1] + " "
                        + a[2] + "x" + a[3] + ") tran ra ngoai atlas "
                        + atlas.getWidth() + "x" + atlas.getHeight());
                loi++;
            }
        }
        final int soFrame = in.readShort();
        final List<List<int[]>> frame = new ArrayList<>();   // {dx, dy, idAnh, flip, onTop}
        for (int i = 0; i < soFrame; i++) {
            final int soPart = in.readByte();
            final List<int[]> parts = new ArrayList<>();
            for (int k = 0; k < soPart; k++) {
                parts.add(new int[]{in.readShort(), in.readShort(),
                        in.readByte(), in.readByte(), in.readByte()});
            }
            frame.add(parts);
        }
        System.out.println("  " + soAnh + " anh con, " + soFrame + " frame");

        // ---- kiem bo cuc 21 frame ----
        if (soFrame % 21 != 0) {
            System.out.println("  LOI: so frame " + soFrame + " khong chia het cho 21."
                    + " Char.getFrameHorse cong 0/7/14 nen thieu frame la con thu bien mat.");
            loi++;
        }
        for (int h = 0; h < 3; h++) {
            for (final int f : DI_CHUYEN) {
                final int idx = GOC_HUONG[h] + f;
                if (idx >= soFrame) {
                    System.out.println("  LOI: huong " + TEN_HUONG[h] + " luc di chuyen can"
                            + " frame " + idx + " nhung chi co " + soFrame);
                    loi++;
                }
            }
        }

        // ---- ve lai dung nhu client ----
        final int oW = 96, oH = 96, le = 26;
        final int soCot = DUNG_YEN.length + DI_CHUYEN.length;
        final BufferedImage ra = new BufferedImage(le + soCot * oW, 18 + 3 * oH,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ra.createGraphics();
        g.setColor(new Color(0x2A, 0x2A, 0x33));
        g.fillRect(0, 0, ra.getWidth(), ra.getHeight());
        g.setColor(Color.YELLOW);
        for (int c = 0; c < soCot; c++) {
            final boolean dung = c < DUNG_YEN.length;
            final int f = dung ? DUNG_YEN[c] : DI_CHUYEN[c - DUNG_YEN.length];
            g.setColor(dung ? new Color(0x90, 0xB0, 0xFF) : new Color(0xFF, 0xD0, 0x60));
            g.drawString((dung ? "dung F" : "di F") + f, le + c * oW + 4, 12);
        }
        for (int h = 0; h < 3; h++) {
            final int oy = 18 + h * oH;
            g.setColor(Color.YELLOW);
            g.drawString(TEN_HUONG[h].substring(0, 2), 3, oy + oH / 2);
            for (int c = 0; c < soCot; c++) {
                final boolean dung = c < DUNG_YEN.length;
                final int f = dung ? DUNG_YEN[c] : DI_CHUYEN[c - DUNG_YEN.length];
                final int idx = GOC_HUONG[h] + f;
                final int ox = le + c * oW;
                // Goc toa do cua nhan vat trong o
                final int cx = ox + oW / 2, cy = oy + oH - 22;
                g.setColor(new Color(0x3A, 0x3A, 0x46));
                g.drawRect(ox, oy, oW - 1, oH - 1);

                // Thu tu ve PHAI GIONG HET Client/Char.paint, khong thi anh kiem tra
                // khong bat duoc loi che khuat:
                //   huong DOWN / LEFT / RIGHT : than nhan vat ve TRUOC, roi den con thu
                //   huong UP                  : con thu ve truoc, than nhan vat ve SAU
                final boolean nguoiVeTruoc = h != 1;   // 1 = huong len (UP)
                if (nguoiVeTruoc) {
                    veNguoi(g, cx, cy, h);
                }
                if (idx < frame.size()) {
                    // Client goi paintTopHorse (onTop == 0) TRUOC roi paintBottomHorse
                    // (onTop != 0) — ten ham bi dao, xem Client/FrameEff.
                    veLop(g, atlas, anhCon, frame.get(idx), cx, cy, 0);
                    veLop(g, atlas, anhCon, frame.get(idx), cx, cy, 1);
                } else {
                    g.setColor(Color.RED);
                    g.drawString("THIEU", ox + 20, cy);
                }
                if (!nguoiVeTruoc) {
                    veNguoi(g, cx, cy, h);
                }
                // Vach dat
                g.setColor(new Color(0xC0, 0x40, 0x40));
                g.drawLine(ox + 2, cy, ox + oW - 3, cy);
            }
        }
        g.dispose();
        final File out = new File("map/skinthu_goc/" + n + "_nhutrongame.png");
        out.getParentFile().mkdirs();
        ImageIO.write(ra, "png", out);
        System.out.println("  -> " + out);
        return loi;
    }

    /**
     * Hinh nguoi gia lap, dat theo Char.dx_horse_tool[0] / dy_horse_tool[0] — dung bang
     * ma SkinLinhThu.INDEX_DXY tro toi. Cao 34px cho gan voi nhan vat that.
     */
    private static void veNguoi(final Graphics2D g, final int cx, final int cy, final int h) {
        g.setColor(new Color(0x50, 0xE0, 0xFF, 200));
        final int nx = cx + DX_NGUOI[h], ny = cy + DY_NGUOI[h];
        g.fillOval(nx - 4, ny - 34, 9, 9);       // dau
        g.fillRect(nx - 3, ny - 25, 7, 25);      // than
    }

    /** Ve cac part co onTop bang {@code lop}, dung phep tinh cua DataSkillEff.paint*Horse. */
    private static void veLop(final Graphics2D g, final BufferedImage atlas,
                              final int[][] anhCon, final List<int[]> parts,
                              final int x, final int y, final int lop) {
        for (final int[] p : parts) {
            final int onTop = p[4];
            if ((onTop == 0 ? 0 : 1) != lop) {
                continue;
            }
            final int[] a = anhCon[p[2]];
            // Client: g.drawRegion(img, sx, sy, w, h, flip, x + dx, y + dy, 0)
            // anchor 0 = goc tren trai.
            final BufferedImage con = atlas.getSubimage(a[0], a[1], a[2], a[3]);
            g.drawImage(con, x + p[0], y + p[1], null);
        }
    }
}
