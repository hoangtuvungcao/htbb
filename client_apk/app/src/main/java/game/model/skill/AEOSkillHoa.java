package game.model.skill;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Cout;
import game.model.Effect;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;

public class AEOSkillHoa extends Effect {
    private static byte[][] frame = {new byte[]{0, 0, 1, 1, 2, 2}, new byte[]{1, 1, 2, 2, 2}};
    private static byte[] typeEff = {15, 15};
    Char attacker;
    short d;
    public int degree;
    int delay;
    int f;
    int idSkill;
    int startX;
    int startY;
    long time;

    public AEOSkillHoa(int i, int i2, int i3) {
        super(i, i2, i3);
        this.idSkill = 0;
        this.degree = 0;
        this.d = (short) 20;
        this.f = 0;
    }

    public AEOSkillHoa(int i) {
        super(i);
        this.idSkill = 0;
        this.d = (short) 20;
        this.f = 0;
        this.degree = i;
    }

    public AEOSkillHoa(int i, int i2, byte b) {
        super(i);
        this.idSkill = 0;
        this.degree = 0;
        this.d = (short) 20;
        this.f = 0;
        this.x = i;
        this.y = i2;
        this.idSkill = b;
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            int i = this.idSkill;
            if (i == 0) {
                Res.paintImgEff(graphics, typeEff[i], 0, frame[i][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], this.x + this.startX, this.y + this.startY, Graphics.BOTTOM | Graphics.HCENTER);
                byte[] bArr = typeEff;
                int i2 = this.idSkill;
                Res.paintImgEff(graphics, bArr[i2], 0, frame[i2][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], (this.x - 3) + this.startX, this.y + this.startY, Graphics.BOTTOM | Graphics.HCENTER);
            } else if (i == 1) {
                Res.paintImgEff(graphics, typeEff[i], 0, frame[i][this.f] * Effect.HEIGHT[typeEff[this.idSkill]], Effect.WIDTH[typeEff[this.idSkill]], Effect.HEIGHT[typeEff[this.idSkill]], this.x, this.y, Graphics.BOTTOM | Graphics.HCENTER);
            }
        } catch (Exception unused) {
            Cout.println("skill AEOSKILLHOA");
        }
    }

    public void setPos(int i, int i2) {
        this.startX = i;
        this.startY = i2;
    }

    public void setCharAttack(Char r1) {
        this.attacker = r1;
    }

    @Override
    public void update() {
        int i = this.idSkill;
        if (i != 0) {
            if (i != 1) {
                return;
            }
            if (GCanvas.gameTick % 2 == 0) {
                this.f = (this.f + 1) % frame[this.idSkill].length;
            }
            if (this.delay <= 0) {
                this.wantDetroy = true;
                return;
            } else {
                if (System.currentTimeMillis() - this.time >= 1000) {
                    this.time = System.currentTimeMillis();
                    this.delay--;
                    return;
                }
                return;
            }
        }
        if (GCanvas.gameTick % 2 == 0) {
            this.f = (this.f + 1) % frame[this.idSkill].length;
        }
        int i2 = this.degree;
        if (i2 > 360) {
            this.degree = i2 - 360;
        }
        short s = this.d;
        if (s < 80) {
            this.d = (short) (s + 10);
        } else {
            this.d = (short) 20;
        }
        int i3 = this.degree;
        if (i3 == 0) {
            this.x = this.d;
        } else if (i3 == 180) {
            this.x = (short) (-this.d);
        } else {
            this.x = (short) ((Util.cos(i3) * this.d) / 1024);
        }
        int i4 = this.degree;
        if (i4 == 90) {
            this.y = this.d;
        } else if (i4 == 270) {
            this.y = (short) (-this.d);
        } else {
            this.y = (short) ((Util.sin(i4) * this.d) / 1024);
        }
    }

    public void setTime(int i) {
        this.time = System.currentTimeMillis();
        this.delay = i;
    }
}
