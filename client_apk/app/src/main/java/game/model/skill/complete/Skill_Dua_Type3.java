package game.model.skill.complete;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Cout;
import game.model.Effect;
import game.model.EffectManager;
import game.model.LiveActor;
import game.model.Monster;
import game.model.skill.SkillAnimate;
import game.render.GCanvas;
import game.render.Res;

public class Skill_Dua_Type3 extends SkillAnimate {
    int ID;
    byte lap;
    byte loop;
    int lvSkill;
    byte nDragon;
    int pmonster;

    @Override
    public void updateSkill(Monster monster) {
    }

    public Skill_Dua_Type3(int i) {
        super(-1);
        this.pmonster = 0;
        this.nDragon = (byte) 1;
        this.ID = i;
    }

    @Override
    public void setLvSkill(int i) {
        this.lvSkill = i;
    }

    @Override
    public int getX2() {
        this.nDragon = (byte) nDra[this.lvSkill];
        return nDra[this.lvSkill];
    }

    @Override
    public void updateSkill(Char r16) {
        try {
            super.updateSkill(r16);
            if (r16.state == 0) {
                return;
            }
            if (r16.p1 == 8) {
                this.loop = (byte) 0;
                this.lap = (byte) 0;
            }
            try {
                updateSkillDua(r16);
            } catch (Exception unused) {
            }
            try {
                if (r16.p1 == 3 && this.ID == 0) {
                    short[][] sArr = {new short[]{90, 315, 225, 270, 315, 225, 90, 315, 225, 315, 225}, new short[]{270, 135, 45, 90, 135, 45, 270, 135, 45, 135, 45}, new short[]{180, 45, 315, 180, 45, 315, 180, 45, 315, 45, 315}, new short[]{0, 135, 225, 0, 135, 225, 0, 135, 225, 135, 225}};
                    if (r16.dir <= splashDuaX.length - 1 && r16.dir <= splashDuaY.length - 1 && r16.dir <= 3 && r16.dir <= sArr[r16.dir].length - 1) {
                        GCanvas.gameScr.startNewMagicBeam(3, r16, r16.attackTarget, splashDuaX[r16.dir] + r16.x, splashDuaY[r16.dir] + r16.y, r16.attkPower != 2000000 ? r16.attkPower / this.nDragon : r16.attkPower, r16.attkEffect, sArr[r16.dir][this.loop]);
                    }
                    byte b = (byte) (this.loop + 1);
                    this.loop = b;
                    if (b < this.nDragon) {
                        r16.p1 = (short) 0;
                    }
                }
                int i = 2;
                if (this.lap % 2 == 0 && this.ID == 1) {
                    int i2 = this.lvSkill;
                    if (i2 >= 7 || i2 <= 3) {
                        i = i2 >= 7 ? 4 : 1;
                    }
                    for (int i3 = 0; i3 < i; i3++) {
                        Eff_DauRong_Down eff_DauRong_Down = new Eff_DauRong_Down(r16.attackTarget.x, r16.attackTarget.y - 200, r16.attackTarget);
                        if (r16.attkPower != 2000000) {
                            eff_DauRong_Down.power = r16.attkPower / this.nDragon;
                        } else {
                            eff_DauRong_Down.power = r16.attkPower;
                        }
                        EffectManager.hiEffects.addElement(eff_DauRong_Down);
                        EffectManager.addHiEffect(r16.x, r16.y - 15, 23);
                    }
                }
            } catch (Exception unused2) {
            }
            r16.p1 = (short) (r16.p1 + 1);
            if (this.ID == 1) {
                this.lap = (byte) (this.lap + 1);
            }
        } catch (Exception unused3) {
            Cout.println("Loi skill type dua 3");
        }
    }

    class Eff_DauRong_Down extends Effect {
        int power;
        public LiveActor to;
        int vy = 5;
        int[] wImg = {0, 0, 24, 24};
        int x;
        int xTo;
        int y;
        int yTo;

        public Eff_DauRong_Down(int i, int i2, LiveActor liveActor) {
            this.x = GCanvas.random(i - 15, i + 15);
            this.y = GCanvas.random(i2 - 20, i2 + 20);
            this.to = liveActor;
        }

        @Override
        public void update() {
            int i = this.y;
            int i2 = this.vy;
            int i3 = i + i2;
            this.y = i3;
            this.vy = i2 + 2;
            if (i3 > this.to.y - 15) {
                EffectManager.hiEffects.removeElement(this);
                EffectManager.addHiEffect(this.x, this.y - 5, 30);
            }
            if (this.power != 0) {
                GCanvas.gameScr.startFlyText("-" + this.power, 0, this.x, this.y - 15, 1, -2);
            } else {
                GCanvas.gameScr.startFlyText("MISS", 0, this.x, this.y - 15, 1, -2);
            }
        }

        @Override
        public void paint(Graphics graphics) {
            if (Res.getImgArrow(4) != null) {
                Bitmap bitmap = Res.imgArrow[4];
                int[] iArr = this.wImg;
                graphics.drawRegion(bitmap, iArr[0], iArr[1], iArr[2], iArr[3], 5, this.x, this.y, 3);
            }
        }
    }
}
