/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Random;
import javax.imageio.ImageIO;

public class Capcha {
    static String st = "adgjmptw";
    byte[] byteImage;
    String text = "";
    int size = 20;
    int delta = 0;
    String typeImage = "png";

    public Capcha(boolean isWeb) {
        String h = "map/capcha.png";
        this.text = "";
        this.createImage(h);
    }

    static int abs(int x) {
        return x > 0 ? x : -x;
    }

    private void createText() {
        Random rd = new Random();
        rd.setSeed(System.currentTimeMillis());

        for (int i = 0; i < 2; i++) {
            int c = abs(rd.nextInt() % st.length());
            this.text = this.text + st.charAt(c);
        }
    }

    public void createImage(String stBkg) {
        try {
            this.createText();
            FileInputStream file = new FileInputStream(stBkg);
            BufferedImage img = ImageIO.read(file);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Graphics g = img.getGraphics();
            g.setColor(Color.black);
            Font f = new Font("Tahoma", 2, this.size);
            g.setFont(f);
            int w = img.getWidth();
            int h = img.getHeight();
            g.setColor(Color.RED);
            g.drawString(this.text, 10, 20 + this.delta);
            g.drawLine(0, 0, w >> 1, h);
            g.drawLine(w, 0, 48, h);
            g.drawLine(w, 0, 49, h);
            g.drawLine(w, 0, 50, h);
            g.drawLine(w, 0, 0, h);
            g.drawLine(0, h >> 1, w, h >> 1);
            g.drawLine(0, h >> 1, w + 5, h >> 1);
            g.drawLine(0, h >> 1, w + 10, h >> 1);
            ImageIO.write(img, this.typeImage, os);
            this.byteImage = os.toByteArray();
        } catch (Exception var9) {
            System.out.println("loi roi ne " + var9.toString());
        }
    }

    public byte[] getByteImage() {
        return this.byteImage;
    }

    public String getText() {
        return this.text;
    }
}
