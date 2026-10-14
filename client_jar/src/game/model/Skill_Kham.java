package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Skill_Kham extends Effect {
    private LiveActor live;
    public int curTime;
    public int delay;
    public int timeDoc;
    private Image img;
    int[] color;
    private short speedTemp;
    private CharBuff trungDoc;

    public Skill_Kham(short type, int time, LiveActor live) {
        int[] rgbColor;
        this.live = live;
        this.type = type;
        this.delay = time;
        this.curTime = (int) (System.currentTimeMillis() / 1000);
        switch (type) {
            case 0:
                if (live.ID == Canvas.gameScr.mainChar.ID) {
                    Canvas.gameScr.isFillScr = true;
                } else {
                    live.effSkill.removeElement(this);
                }
                break;
            case 1:
                this.timeDoc = ((int) (System.currentTimeMillis() / 1000)) - 1;
                this.trungDoc = new CharBuff(live.x, live.y - 17, 22);
                break;
            case 2:
                this.color = new int[]{7053112, 7517233, 16317187, 8386060, 16453379};
                break;
            case 3:
            case 4:
                if (type == 3) {
                    rgbColor = new int[]{-1, -2950406, -4849665, -5778950, -7354371, -9718017, -9786901, -10969884, -11889954, -12811056};
                } else {
                    rgbColor = new int[]{-1381912, -3092533, -4408651, -5330011, -6185321, -6711666, -7961734, -8817301, -10330540, -11317177};
                }
                this.img = Image.createImage(50, 105);
                Graphics g = this.img.getGraphics();
                g.setColor(-65315);
                g.fillRect(0, 0, 50, 105);
                g.translate((-live.x) + (50 / 2), ((-live.y) + 105) - 5);
                live.paint(g);
                this.img = Res.transparentBG(this.img);
                int[] rgb = new int[50 * 105];
                this.img.getRGB(rgb, 0, 50, 0, 0, 50, 105);
                for (int i = 0; i < rgb.length; i++) {
                    if (rgb[i] != 16777215) {
                        int r = rgb[i] / 1864135;
                        rgb[i] = rgbColor[Util.abs(r)];
                    }
                }
                this.img = Image.createRGBImage(rgb, 50, 105, true);
                live.isFreeze = true;
                break;
            case 5:
                this.speedTemp = live.speed;
                live.speed = (short) (live.speed / 2);
                break;
        }
    }

    @Override
    public void update() {
        if (this.type == 1) {
            this.trungDoc.update();
            this.trungDoc.setXY(this.live.x, this.live.y - 17);
            if ((System.currentTimeMillis() / 1000) - ((long) this.timeDoc) > 1) {
                this.timeDoc = (int) (System.currentTimeMillis() / 1000);
                this.live.hp -= 1000;
                Canvas.gameScr.startFlyText("-1000", 4, this.live.x, this.live.y - 40, 0, -1);
                if (this.live.hp < 0) {
                    this.live.hp = 0;
                }
            }
        }
        if ((System.currentTimeMillis() / 1000) - ((long) this.curTime) > this.delay) {
            switch (this.type) {
                case 0:
                    Canvas.gameScr.isFillScr = false;
                    break;
                case 3:
                case 4:
                    this.live.isFreeze = false;
                    break;
                case 5:
                    this.live.speed = this.speedTemp;
                    break;
            }
            this.live.effSkill.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics g) {
        switch (this.type) {
            case 1:
                this.trungDoc.paint(g);
                break;
            case 2:
                for (int i = 0; i < 12; i++) {
                    int xa = (8 * Util.cos(i * 30)) >> 10;
                    int ya = (-(8 * Util.sin(i * 30))) >> 10;
                    g.setColor(this.color[Res.rnd(5)]);
                    g.fillRect(this.live.x + xa, (this.live.y - this.live.height) + 4 + ya, 1, 5);
                }
                break;
            case 3:
            case 4:
                g.drawImage(this.img, this.live.x, this.live.y + 5, 32 | 1);
                break;
        }
    }
}
