import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Dong goi nguyen PNG 866/867 vao bo phan nhan vat; khong ve them huong chuyen dong. */
public final class TaoThoiTrangTrungThu2026 {
    public static void main(String[] args) throws Exception {
        for (int sex = 0; sex < 2; sex++) {
            Path source = Paths.get("map/icon/" + (866 + sex) + ".png");
            byte[] png = Files.readAllBytes(source);
            BufferedImage image = ImageIO.read(source.toFile());
            int w = image.getWidth(), h = image.getHeight() / 2;
            if (image.getHeight() % 2 != 0 || w > 127 || h > 127 || png.length > 32767)
                throw new IOException("Sprite phai co 2 khung doc, kich thuoc vua giao thuc");
            // Chia khung theo chieu cao: dau 0..45%, than 45..75%, chan 75..100%.
            int neck = h * 45 / 100, waist = h * 75 / 100;
            pack(png, w, h, 0, neck, 1, Paths.get("map/c/head/" + (84 + sex)));
            pack(png, w, h, neck, waist - neck, 5, Paths.get("map/c/body/" + (81 + sex)));
            pack(png, w, h, waist, h - waist, 5, Paths.get("map/c/leg/" + (81 + sex)));
            BufferedImage icon = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = icon.createGraphics();
            // Icon hanh trang lay phan dau cua khung dau, giu nguyen anh avatar goc.
            int side = Math.min(w, neck);
            int left = (w - side) / 2;
            int top = Math.max(0, neck - side);
            g.drawImage(image, 0, 0, 18, 18, left, top, left + side, top + side, null);
            g.dispose();
            ImageIO.write(icon, "png", new File("map/icon/" + (868 + sex) + ".png"));
            System.out.println("Trung Thu " + (912 + sex) + ": " + w + "x" + h + ", 2 frames");
        }
    }
    private static void pack(byte[] png, int w, int frameH, int y, int h, int count, Path out) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(buffer);
        data.writeByte(0); data.writeByte(y); data.writeByte(w); data.writeByte(h);
        data.writeByte(-w / 2); data.writeByte(y - frameH);
        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < count; frame++) {
                data.writeByte(0);
                data.writeByte(y + (frame % 2) * frameH);
                data.writeByte(w); data.writeByte(h);
            }
            for (int frame = 0; frame < 6; frame++) {
                data.writeByte(-w / 2); data.writeByte(y - frameH);
            }
        }
        data.flush();
        try (DataOutputStream file = new DataOutputStream(Files.newOutputStream(out))) {
            file.writeShort(png.length); file.write(png);
            file.writeShort(buffer.size()); file.write(buffer.toByteArray());
        }
    }
}
