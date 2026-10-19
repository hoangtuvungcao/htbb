/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;

public class GameCanvas extends Canvas {
    public static String test = "";
    public static int count = 0;

    public GameCanvas() {
    }

    public GameCanvas(GraphicsConfiguration config) {
        super(config);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.red);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (!test.equals("")) {
            g.setColor(Color.black);
            System.out.println("goi paint chu ra man hinh");
            g.drawString(test, 100, 100);
        }
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
    }
}
