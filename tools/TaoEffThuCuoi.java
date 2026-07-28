import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Dung sprite THU CUOI theo he thong "thu cuoi tool" — chinh la he thong dang ve
 * con Rong Bang, Poro, Lan, Su Tu... va ca hoa hinh.
 *
 * Duong di: Animal.getIdImageTool() tra ve id dang 8700+N -> server gui trong goi
 * thong tin nhan vat -> client Char.setHorse(idhorse,...) -> Char.getPartEff(idhorse)
 * -> DataSkillEff. Du lieu frame duoc gui SAN o goi login (message 26, danh sach
 * MessageCreator.id_eff_tool_ngu_long) con anh thi client xin theo id luc can
 * (DataGame.sendByteImg). Vi vay he nay KHONG phu thuoc bo anh cache — khac han
 * duong imgHorse.
 *
 * Dinh dang file data (Client/DataSkillEff.loadData), khac DataEffect o cho moi
 * part co them 2 byte flip/onTop va so buoc sequence la 1 byte:
 *
 *   byte  soAnhCon
 *     moi anh con: 5 byte KHONG DAU = id, x, y, w, h   (nen moi so phai <= 255)
 *   short soFrame
 *     moi frame: byte soPart, roi moi part = short dx, short dy, byte idAnhCon,
 *                byte flip, byte onTop
 *   byte  soBuoc, roi moi buoc = short chi so frame
 *   byte  indexStartSkill
 *   byte n0 + n0 byte  -> frameChar[0]
 *   byte n1 + n1 byte  -> frameChar[1]
 *   byte n3 + n3 byte  -> frameChar[3]
 *
 * Client chon frame theo huong trong Char.getFrameBody():
 *   DOWN -> frame            (0..5)
 *   UP   -> 6 + frame        (6..11)
 *   LEFT/RIGHT -> 12 + frame (12..17), RIGHT duoc lat ngang
 * => can dung 18 frame: 6 xuong, 6 len, 6 ngang.
 *
 *   javac -d out tools/CatSpriteSheet.java tools/TaoEffThuCuoi.java
 *   java -cp out TaoEffThuCuoi <bang.png> <id d_effect> --xuong=N --len=N --ngang=N
 *        [--botrai=0] [--nguong=48] [--o=60]
 *
 * Ghi ra map/d_effect/<id>.png + map/d_effect/<id>. Id phai nam trong
 * id_eff_tool_ngu_long va khong duoc trung id nao dang dung.
 */
public class TaoEffThuCuoi {

    /** So frame moi huong ma client mong doi. */
    private static final int SO_FRAME_HUONG = 6;

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java TaoEffThuCuoi <bang.png> <id> --xuong=N --len=N "
                    + "--ngang=N [--botrai=0] [--nguong=48] [--o=60]");
            return;
        }
        final File sheet = new File(args[0]);
        final int id = Integer.parseInt(args[1]);
        int hXuong = -1, hLen = -1, hNgang = -1, boTrai = 0, nguong = 48, o = 51;
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--xuong=")) hXuong = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--len=")) hLen = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--ngang=")) hNgang = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--botrai=")) boTrai = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--nguong=")) nguong = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--o=")) o = Integer.parseInt(a.substring(4));
        }
        if (hXuong < 0 || hLen < 0 || hNgang < 0) {
            System.out.println("Thieu --xuong / --len / --ngang");
            return;
        }
        if (o > 51) {
            System.out.println("O qua lon: 5 cot x " + o + " > 255. Dung --o <= 51.");
            return;
        }

        final BufferedImage goc = ImageIO.read(sheet);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + sheet);
            return;
        }
        final boolean[][] coHinh = CatSpriteSheet.locNen(goc, nguong, boTrai, 0);
        final List<int[]> hangs = CatSpriteSheet.timDai(coHinh, true);

        // Lay khung bao cua tung frame cua 3 hang, roi mot ti le CHUNG cho tat ca.
        final int[] hang = {hXuong, hLen, hNgang};
        final int[][][] khung = new int[3][SO_FRAME_HUONG][];
        double tiLe = Double.MAX_VALUE;
        for (int nhom = 0; nhom < 3; nhom++) {
            if (hang[nhom] >= hangs.size()) {
                System.out.println("Khong co hang " + hang[nhom] + " (bang chi co "
                        + hangs.size() + " hang)");
                return;
            }
            final int[] h = hangs.get(hang[nhom]);
            final List<int[]> cot = CatSpriteSheet.timCot(coHinh, h[0], h[1], -1, -1);
            if (cot.isEmpty()) {
                System.out.println("Hang " + hang[nhom] + " khong co frame");
                return;
            }
            for (int f = 0; f < SO_FRAME_HUONG; f++) {
                // Thieu frame thi lap lai vong tron cho du 6.
                final int[] c = cot.get(f % cot.size());
                final int[] k = CatSpriteSheet.khungBao(coHinh, c[0], c[1], h[0], h[1]);
                if (k == null) continue;
                khung[nhom][f] = k;
                tiLe = Math.min(tiLe, Math.min((double) o / (k[3] - k[1] + 1),
                        (double) o / (k[2] - k[0] + 1)));
            }
            System.out.println("  nhom " + nhom + " (hang " + hang[nhom] + "): "
                    + cot.size() + " frame nguon");
        }
        if (tiLe == Double.MAX_VALUE) {
            System.out.println("Khong lay duoc frame nao");
            return;
        }

        // Atlas 4 cot x 5 hang o vuong canh o, phai nam gon trong 255x255 vi toa do
        // anh con luu bang byte khong dau.
        // 18 frame phai nam gon trong 255x255: 5 cot x 4 dong, o toi da 51 (255/5).
        final int cot = 5;
        final int dong = (18 + cot - 1) / cot;
        final BufferedImage atlas = new BufferedImage(cot * o, dong * o,
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = atlas.createGraphics();
        final int[][] oAnh = new int[18][]; // x, y, w, h cua tung frame trong atlas
        for (int i = 0; i < 18; i++) {
            final int nhom = i / SO_FRAME_HUONG;   // 0 xuong, 1 len, 2 ngang
            final int f = i % SO_FRAME_HUONG;
            final int[] k = khung[nhom][f];
            if (k == null) {
                oAnh[i] = new int[]{0, 0, 1, 1};
                continue;
            }
            final BufferedImage nho = CatSpriteSheet.thuNho(goc, coHinh, k, tiLe);
            final int ox = (i % cot) * o, oy = (i / cot) * o;
            // Can giua o va sat day o.
            final int dx = ox + (o - nho.getWidth()) / 2;
            final int dy = oy + (o - nho.getHeight());
            g.drawImage(nho, dx, dy, null);
            oAnh[i] = new int[]{dx, dy, nho.getWidth(), nho.getHeight()};
        }
        g.dispose();
        if (atlas.getWidth() > 255 || atlas.getHeight() > 255) {
            System.out.println("Atlas " + atlas.getWidth() + "x" + atlas.getHeight()
                    + " vuot 255 — giam --o");
            return;
        }

        final File png = new File("map/d_effect/" + id + ".png");
        ImageIO.write(atlas, "png", png);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(18);
        for (int i = 0; i < 18; i++) {
            dos.writeByte(i);
            dos.writeByte(oAnh[i][0]);
            dos.writeByte(oAnh[i][1]);
            dos.writeByte(oAnh[i][2]);
            dos.writeByte(oAnh[i][3]);
        }
        dos.writeShort(18);
        for (int i = 0; i < 18; i++) {
            dos.writeByte(1);                       // mot part
            dos.writeShort(-oAnh[i][2] / 2);        // dx: can giua
            dos.writeShort(-oAnh[i][3]);            // dy: ngoi tren mat dat
            dos.writeByte(i);                       // id anh con
            dos.writeByte(0);                       // flip
            dos.writeByte(0);                       // onTop = 0 -> ve duoi nhan vat
        }
        dos.writeByte(18);
        for (int i = 0; i < 18; i++) {
            dos.writeShort(i);
        }
        dos.writeByte(0);   // indexStartSkill
        dos.writeByte(0);   // frameChar[0] rong
        dos.writeByte(0);   // frameChar[1] rong
        dos.writeByte(0);   // frameChar[3] rong
        dos.flush();
        try (FileOutputStream fo = new FileOutputStream(new File("map/d_effect/" + id))) {
            fo.write(bos.toByteArray());
        }
        System.out.println("Xong: d_effect/" + id + ".png (" + atlas.getWidth() + "x"
                + atlas.getHeight() + ") + d_effect/" + id + " (" + bos.size() + " byte)"
                + ", ti le " + String.format("%.3f", tiLe) + " -> id gui client = "
                + (8700 + id));
    }
}
