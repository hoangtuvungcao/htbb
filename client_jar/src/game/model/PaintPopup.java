package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class PaintPopup extends Screen {
    private static PaintPopup me;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    private int x;
    private int y;
    private int x0;
    private int y0;
    private int w;
    private int h;
    private int numW;
    private int wOne;
    public int selected;
    private Screen lastScr;
    public Vector cmd;
    private String title;

    public static PaintPopup gI() {
        if (me != null) {
            return me;
        }
        PaintPopup paintPopup = new PaintPopup();
        me = paintPopup;
        return paintPopup;
    }

    @Override
    public void switchToMe() {
        this.lastScr = Canvas.currentScreen;
        super.switchToMe();
    }

    public PaintPopup() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                PaintPopup.this.lastScr.switchToMe();
                PaintPopup.this.cmd.removeAllElements();
                PaintPopup.this.title = null;
                PaintPopup.this.lastScr = null;
            }
        });
        this.center = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                ((Command) PaintPopup.this.cmd.elementAt(PaintPopup.this.selected)).action.perform();
            }
        });
    }

    public void setPos(int x0, int y0, int w0, int h0, int numW) {
        this.x = x0;
        this.y = y0;
        this.w = w0;
        this.h = h0;
        this.numW = numW;
        this.wOne = (this.w - 6) / numW;
        int i = ((this.w - 6) - (this.wOne * numW)) / 2;
        this.y0 = i;
        this.x0 = i;
    }

    public void setInfo(Vector cmd, String title) {
        this.title = title;
        this.cmd = cmd;
        int aa = 0;
        if (cmd.size() % this.numW != 0) {
            aa = 1;
        }
        cmyLim = (((cmd.size() / this.numW) + aa) * this.wOne) - (this.h - 32);
        if (cmyLim < 0) {
            cmyLim = 0;
        }
    }

    @Override
    public void update() {
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
    }

    @Override
    public void updateKey() {
        boolean changeFocus = false;
        if (Canvas.isKeyPressed(2)) {
            changeFocus = true;
            if (this.selected / this.numW > 0) {
                this.selected -= this.numW;
            }
        } else if (Canvas.isKeyPressed(4)) {
            changeFocus = true;
            this.selected--;
            if (this.selected < 0) {
                this.selected = 0;
            }
        } else if (Canvas.isKeyPressed(6)) {
            changeFocus = true;
            this.selected++;
            if (this.selected >= this.cmd.size()) {
                this.selected = this.cmd.size() - 1;
            }
        } else if (Canvas.isKeyPressed(8)) {
            changeFocus = true;
            if (this.selected / this.numW < this.cmd.size() / this.numW && this.selected + this.numW < this.cmd.size()) {
                this.selected += this.numW;
            }
        }
        if (changeFocus) {
            cmtoY = ((this.selected / this.numW) * this.wOne) - ((this.h - 31) / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        super.updateKey();
    }

    @Override
    public void paint(Graphics g) {
        this.lastScr.paint(g);
        Canvas.resetTrans(g);
        Res.paintDlgFull(g, this.x, this.y, this.w, this.h);
        Font.borderFont.drawString(g, game.pokemon.GiaoDien.tieuDe(this.title), this.x + (this.w / 2) + 6, this.y + 6, 2); // pokemon: chữ trắng viền giữa dải đỏ
        g.translate(this.x + 3 + this.x0, this.y + 26 + this.y0);
        g.setClip(0, 0, this.w - (this.x0 * 2), (this.h - 30) - this.y0);
        g.translate(0, -cmy);
        Res.paintSelected(g, (this.selected % this.numW) * this.wOne, (this.selected / this.numW) * this.wOne, this.wOne, this.wOne);
        int aa = (cmy / this.wOne) * this.numW;
        if (aa <= 0) {
            aa = 0;
        }
        int bb = aa + ((((this.h - 31) / this.wOne) + 2) * this.numW);
        if (bb > this.cmd.size()) {
            bb = this.cmd.size();
        }
        for (int i = aa; i < bb; i++) {
            Command c = (Command) this.cmd.elementAt(i);
            if (c != null) {
                g.setColor(Res.color[0]);
                g.drawRect((i % this.numW) * this.wOne, (i / this.numW) * this.wOne, this.wOne, this.wOne);
                c.paint(g, ((i % this.numW) * this.wOne) + (this.wOne / 2), ((i / this.numW) * this.wOne) + (this.wOne / 2));
            }
        }
        super.paint(g);
    }
}
