import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Tao file data frame cho mot sprite moi trong map/d_effect/.
 *
 * Client doc file nay bang DataEffect (Client/DataEffect.cs), dinh dang:
 *
 *   byte  soAnhCon
 *     moi anh con: 5 byte khong dau = id, x, y, w, h   (toa do trong file png)
 *   short soFrame
 *     moi frame  : byte soPhan, roi moi phan = short dx, short dy, byte idAnhCon
 *   short soBuoc  (sequence), moi buoc: short chi so frame
 *   byte  (bo qua)  byte (bo qua)
 *   4 lan: byte doDai, roi doDai byte  -> Animation 0..3
 *
 * dx, dy la do lech khi ve so voi diem neo cua nhan vat: am len tren / sang trai.
 * Mac dinh dat anh can giua theo chieu ngang va ngoi tren mat dat.
 *
 * Cach dung:
 *   javac -d out tools/TaoDataSprite.java
 *   java -cp out TaoDataSprite map/d_effect/300.png 3 doc
 *      tham so 2 = so frame, tham so 3 = "doc" (xep doc) hoac "ngang"
 *   -> ghi ra map/d_effect/300  (cung ten, khong duoi)
 *
 * Kiem tra lai bang cach so voi mot file co san, vi du map/d_effect/222
 * (mat na cao: png 19x66, 3 anh con xep doc).
 */
public class TaoDataSprite {

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java TaoDataSprite <duong dan png> <so frame> [doc|ngang]");
            return;
        }
        final File pngFile = new File(args[0]);
        final int soFrame = Integer.parseInt(args[1]);
        final boolean xepDoc = args.length < 3 || !"ngang".equalsIgnoreCase(args[2]);

        final BufferedImage img = ImageIO.read(pngFile);
        if (img == null) {
            System.out.println("Khong doc duoc anh: " + pngFile);
            return;
        }
        final int w = xepDoc ? img.getWidth() : img.getWidth() / soFrame;
        final int h = xepDoc ? img.getHeight() / soFrame : img.getHeight();
        if (w <= 0 || h <= 0 || w > 255 || h > 255) {
            System.out.println("Kich thuoc mot frame khong hop le: " + w + "x" + h
                    + " (moi canh phai trong khoang 1..255). Kiem tra lai so frame.");
            return;
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);

        // ---- danh sach anh con: moi frame la mot o chu nhat trong png ----
        dos.writeByte(soFrame);
        for (int i = 0; i < soFrame; i++) {
            dos.writeByte(i);                       // id
            dos.writeByte(xepDoc ? 0 : i * w);      // x
            dos.writeByte(xepDoc ? i * h : 0);      // y
            dos.writeByte(w);
            dos.writeByte(h);
        }

        // ---- frame: moi frame ve dung mot anh con, can giua + ngoi tren mat dat ----
        final int dx = -w / 2;
        final int dy = -h;
        dos.writeShort(soFrame);
        for (int i = 0; i < soFrame; i++) {
            dos.writeByte(1);        // so phan
            dos.writeShort(dx);
            dos.writeShort(dy);
            dos.writeByte(i);        // id anh con
        }

        // ---- sequence: chay lan luot 0,1,2,... roi lap lai ----
        dos.writeShort(soFrame);
        for (int i = 0; i < soFrame; i++) {
            dos.writeShort(i);
        }

        // ---- hai byte client doc roi bo qua ----
        dos.writeByte(0);
        dos.writeByte(0);

        // ---- 4 animation: dung chung mot chuoi frame ----
        for (int a = 0; a < 4; a++) {
            dos.writeByte(soFrame);
            for (int i = 0; i < soFrame; i++) {
                dos.writeByte(i);
            }
        }

        dos.flush();
        final String out = pngFile.getPath().substring(0, pngFile.getPath().length() - 4);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(bos.toByteArray());
        }
        System.out.println("Da ghi " + out + " (" + bos.size() + " byte) — "
                + soFrame + " frame " + w + "x" + h + ", neo (" + dx + "," + dy + ")");
    }
}
