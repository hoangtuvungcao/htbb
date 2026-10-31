package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

public class MTickets extends Screen {
    public Vector vVs = new Vector();
    byte numHp;
    byte index;
    int x0;
    int y0;
    public static MTickets me;

    public static MTickets gi() {
        if (me != null) {
            return me;
        }
        MTickets mTickets = new MTickets();
        me = mTickets;
        return mTickets;
    }

    public void onHappy(Message msg) {
        try {
            this.vVs = new Vector();
            byte sum = msg.reader().readByte();
            for (int i = 0; i < sum; i++) {
                byte a = msg.reader().readByte();
                this.vVs.addElement(new StringBuffer(String.valueOf(a < 10 ? "0" : "")).append((int) a).toString());
            }
        } catch (Exception e) {
        }
    }

    public void sendSelectHp() {
        if (this.index >= 0 && this.index < this.vVs.size()) {
            String s = (String) this.vVs.elementAt(this.index);
            int a = Integer.parseInt(s);
            GameService.gI().sendTickets((byte) a);
        }
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        Canvas.resetTrans(g);
        if (game.pokemon.ManCu.bat()) {
            // 31/10/2026: tấm chung - lưới số còn bán (ô cỡ chạm), chạm = chọn, nút "Mua số NN"
            this.anNutLenh = true;
            boolean co = this.index >= 0 && this.index < this.vVs.size();
            Command mua = co && this.center != null ? new Command("Mua số " + this.vVs.elementAt(this.index), this.center.action) : null;
            int[] c = game.pokemon.ManCu.tam(g, this, "Vé số", game.pokemon.ManCu.viec(this.right), new Command[]{mua});
            String goi = "Chọn một số còn bán rồi bấm Mua.";
            game.pokemon.ChuDe.chu(g, goi, c[0] + c[2] / 2, c[1], game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 2);
            int hg = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + game.pokemon.Bang.cach();
            if (this.vVs.size() == 0) {
                game.pokemon.Bang.trong(g, c[0], c[1] + hg, c[2], c[3] - hg, "Hết số để bán", "Quay lại kỳ sau nhé.");
                return;
            }
            final Vector v = this.vVs;
            game.pokemon.ManCu.luoi(g, new int[]{c[0], c[1] + hg, c[2], c[3] - hg}, v.size(), this.index, new game.pokemon.ManCu.O() {
                public String chu(int i) {
                    return (String) v.elementAt(i);
                }

                public void cham(int i) {
                    MTickets.this.index = (byte) i;
                }

                public void ve(Graphics g, int i, int x, int y, int s) {
                }
            }, 0);
            return;
        }
        int xbg = this.x0;
        int ybg = this.y0;
        Res.paintDlgDragonFull(g, xbg - 5, ybg - 5, 171, 171);
        g.setColor(16777215);
        g.fillRect(xbg + ((this.index % 10) * 16), ybg + ((this.index / 10) * 16), 16, 16);
        for (int i = 0; i < 100; i++) {
            g.drawImage(Res.imgGrid, xbg + ((i % 10) * 16), ybg + ((i / 10) * 16), 0);
        }
        for (int i2 = 0; i2 < this.vVs.size(); i2++) {
            String n = (String) this.vVs.elementAt(i2);
            Font.normalFont[1].drawString(g, n, xbg + 3 + ((i2 % 10) * 16), ybg + 2 + ((i2 / 10) * 16), 0);
        }
        super.paint(g);
    }

    @Override
    public void update() {
        Canvas.gameScr.update();
        super.update();
    }

    @Override
    public void updateKey() {
        if (game.pokemon.ManCu.bat()) {
            Canvas.isPointerClick = false;   // vùng chạm kiểu chung đã xử lý (lưới cũ 16 px không dùng)
            if (Canvas.keyPressed[8] || Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6]) {
                int d = Canvas.keyPressed[8] ? 5 : Canvas.keyPressed[2] ? -5 : Canvas.keyPressed[4] ? -1 : 1;
                Canvas.keyPressed[8] = Canvas.keyPressed[2] = Canvas.keyPressed[4] = Canvas.keyPressed[6] = false;
                int n = Math.max(1, this.vVs.size());
                this.index = (byte) (((this.index + d) % n + n) % n);
            }
            super.updateKey();
            return;
        }
        if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            this.index = (byte) (this.index + 10);
            if (this.index > 99) {
                this.index = (byte) 0;
            }
        } else if (Canvas.keyPressed[4]) {
            Canvas.keyPressed[4] = false;
            this.index = (byte) (this.index - 1);
            if (this.index < 0) {
                this.index = (byte) 99;
            }
        } else if (Canvas.keyPressed[6]) {
            Canvas.keyPressed[6] = false;
            this.index = (byte) (this.index + 1);
            if (this.index > 99) {
                this.index = (byte) 0;
            }
        } else if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            this.index = (byte) (this.index - 10);
            if (this.index < 0) {
                this.index = (byte) 99;
            }
        }
        if (Canvas.isPointer(this.x0, this.y0, 160, 160) && Canvas.isPointerClick) {
            int aa = (Canvas.pxLast - this.x0) / 16;
            int bb = (Canvas.pyLast - this.y0) / 16;
            this.index = (byte) ((bb * 10) + aa);
            if (this.index <= 0) {
                this.index = (byte) 0;
            }
            if (this.index > 99) {
                this.index = (byte) 99;
            }
            Canvas.isPointerClick = false;
            if (this.center != null) {
                this.center.action.perform();
            }
        }
        super.updateKey();
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    class AnonymousClass1 implements IAction {
        AnonymousClass1() {
        }

        @Override
        public void perform() {
            if (MTickets.this.index >= 0 && MTickets.this.index < MTickets.this.vVs.size()) {
                Canvas.startYesNoDlg("Bạn có muốn mua số này không ?", new IAction() {
                    @Override
                    public void perform() {
                        MTickets.this.sendSelectHp();
                        Canvas.gameScr.switchToMe();
                        MTickets.this.index = (byte) 0;
                        MTickets.this.vVs.removeAllElements();
                    }
                });
            } else {
                Canvas.startOKDlg("Chọn sai. Vui lòng chọn lại");
            }
        }
    }

    @Override
    public void init() {
        super.init();
        this.center = new Command("Chọn", new AnonymousClass1());
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
                MTickets.this.index = (byte) 0;
                MTickets.this.vVs.removeAllElements();
            }
        });
        this.x0 = Canvas.hw - 80;
        this.y0 = ((Canvas.h - 35) / 2) - 80;
        if (this.y0 < 10) {
            this.y0 = 10;
        }
    }
}
