package game.model;

import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

public class PopupName extends Tree {
    public String name;
    public byte num;
    public byte iPrivate;
    public static FrameImage imgArrow;

    public PopupName(String name, int x, int y) {
        super(x, y, 0);
        this.iPrivate = (byte) 0;
        this.x = (short) x;
        this.y = (short) y;
        this.name = name;
        this.num = (byte) Res.rnd(8);
    }

    @Override
    public void update() {
    }

    @Override
    public void paint(Graphics g) {
        this.num = (byte) (this.num + 1);
        if (this.num >= 8) {
            this.num = (byte) 0;
        }
        g.drawImage(Res.imgShadow, this.x, this.y, 3);
        // 0.9.7: cổng sát mép bản đồ -> mũi tên + chữ "vào" kẹp trọn trong vùng nhìn (không bị cắt ở mép màn)
        int wc = Font.normalFontColor.getWidth(this.name);
        int xa = kep(this.x, 9), xc = kep(this.x, wc / 2 + 2);
        if (imgArrow != null) {
            imgArrow.drawFrame(0, xa, (this.y - 10) + (this.num / 2), 0, 33, g);
        }
        // 0.9.7: chữ biển = nhãn lớp giao diện (NhanTheGioi, ưu tiên thấp nhất: nhường chỗ tên người / NPC); J2ME vẽ ở đây
        if (game.pokemon.NhanTheGioi.cua(this, this.name, this.x, this.y - 14)) {
            return;
        }
        Font.normalFontColor.drawString(g, this.name, xc, (this.y - 32) + (this.num / 2), 2);
    }

    /** x kẹp sao cho [x - nua, x + nua] nằm trong vùng nhìn thế giới (chỉ khi cổng đang ở gần vùng nhìn). */
    static int kep(int x, int nua) {
        int l = game.render.screen.GameScr.cmx, r = l + game.render.screen.GameScr.gsw;
        if (x < l - nua || x > r + nua) {
            return x;
        }
        return Math.max(l + nua, Math.min(r - nua, x));
    }
}
