package game.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import game.core.j2me.Graphics;
import game.core.j2me.Images2;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class Skill_Kham extends Effect {
    public static int curTime;
    public static int delay;
    public static long t;
    public static int timeDoc;
    private int[] FROM_COLOR;
    Bitmap IMG;
    private int THRESHOLD;
    int[] color;
    public boolean destroy = false;
    public Images2 img;
    private LiveActor live;
    private short speedTemp;
    private CharBuff trungDoc;

    public Skill_Kham(short s, int i, LiveActor liveActor) {
        if (liveActor == null) {
            return;
        }
        this.live = liveActor;
        this.type = s;
        delay = i;
        curTime = (int) (System.currentTimeMillis() / 1000);
        if (s == 0) {
            if (liveActor.ID == GCanvas.gameScr.mainChar.ID) {
                GCanvas.gameScr.isFillScr = true;
                return;
            } else {
                liveActor.effSkill.removeElement(this);
                return;
            }
        }
        if (s == 1) {
            timeDoc = ((int) (System.currentTimeMillis() / 1000)) - 1;
            this.trungDoc = new CharBuff(liveActor.x, liveActor.y - 17, 22);
            return;
        }
        if (s == 2) {
            this.color = new int[]{-9724104, -9259983, -460029, -8391156, -323837};
            return;
        }
        if (s == 3) {
            FilePack.init("/other");
            this.IMG = FilePack.getImg("skill_ice");
            FilePack.reset();
            liveActor.isFreeze = true;
            return;
        }
        if (s != 4) {
            if (s != 5) {
                return;
            }
            this.speedTemp = liveActor.speed;
            liveActor.speed = (short) (liveActor.speed / 2);
            return;
        }
        FilePack.init("/other");
        this.IMG = FilePack.getImg("skill_da");
        FilePack.reset();
        liveActor.isFreeze = true;
    }

    private boolean match(int i) {
        return Math.abs(Color.red(i) - this.FROM_COLOR[0]) < this.THRESHOLD && Math.abs(Color.green(i) - this.FROM_COLOR[1]) < this.THRESHOLD && Math.abs(Color.blue(i) - this.FROM_COLOR[2]) < this.THRESHOLD;
    }

    @Override
    public void update() {
        if (this.type == 1) {
            this.trungDoc.update();
            this.trungDoc.setXY(this.live.x, this.live.y - 17);
            if ((System.currentTimeMillis() / 1000) - ((long) timeDoc) > 1) {
                timeDoc = (int) (System.currentTimeMillis() / 1000);
                this.live.hp -= 1000;
                GCanvas.gameScr.startFlyText("-1000", 4, this.live.x, this.live.y - 40, 0, -1);
                if (this.live.hp < 0) {
                    this.live.hp = 0;
                }
            }
        }
        t = ((long) delay) - ((System.currentTimeMillis() / 1000) - ((long) curTime));
        if ((System.currentTimeMillis() / 1000) - ((long) curTime) > delay) {
            short s = this.type;
            if (s == 0) {
                GCanvas.gameScr.isFillScr = false;
            } else if (s == 3 || s == 4) {
                this.live.isFreeze = false;
            } else if (s == 5) {
                this.live.speed = this.speedTemp;
            }
            this.live.effSkill.removeElement(this);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            short s = this.type;
            if (s == 1) {
                this.trungDoc.paint(graphics);
                return;
            }
            if (s != 2) {
                if (s == 3 || s == 4) {
                    try {
                        graphics.drawImage(this.IMG, this.live.x, this.live.y + 10, Graphics.BOTTOM | Graphics.HCENTER);
                        return;
                    } catch (Exception unused) {
                        Cout.println("Loi paint skill kham");
                        return;
                    }
                }
                return;
            }
            for (int i = 0; i < 12; i++) {
                int i2 = i * 30;
                int iCos = (Util.cos(i2) * 8) >> 10;
                int i3 = (-(Util.sin(i2) * 8)) >> 10;
                graphics.setColor(this.color[Res.rnd(5)]);
                graphics.fillRect(this.live.x + iCos, (this.live.y - this.live.height) + 4 + i3, 1, 5);
            }
        } catch (Exception unused2) {
            Cout.println("Loi paint skill kham");
        }
    }
}
