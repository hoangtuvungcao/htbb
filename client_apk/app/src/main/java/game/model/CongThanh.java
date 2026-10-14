package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.GameScr;

public class CongThanh extends Monster {
    int count;
    int x0;
    int y0;

    @Override
    public void jumpVang(Actor actor) {
    }

    @Override
    public void update() {
        if (this.state == 5) {
            this.frame = (byte) 3;
            this.p1 = (short) (this.p1 + 1);
            this.x = (short) (this.x + this.p2);
            this.y = (short) (this.y + this.p3);
            this.p2 = (short) (this.p2 >> 1);
            this.p3 = (short) (this.p3 >> 1);
            if (this.p1 == 5) {
                GCanvas.gameScr.startExplosionAt(this.x, this.y);
            }
            if (this.p1 > 7) {
                GCanvas.gameScr.actors.removeElement(this);
                GameScr.congThanh = null;
            }
        }
        this.x0 = 0;
        this.y0 = 0;
        int i = this.count;
        if (i > 0) {
            this.count = i - 1;
            this.x0 = Res.rnd(2) - 1;
            this.y0 = Res.rnd(2) - 1;
        }
    }

    @Override
    public void setRealHP(int i) {
        super.setRealHP(i);
        this.count = 20;
    }

    @Override
    public void paint(Graphics graphics) {
        if (Res.monsterTemplates[this.monster_type] == null || Res.monsterTemplates[this.monster_type].image == null) {
            return;
        }
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 32, 0, 48, 89, 0, (this.x - 48) + this.x0, this.y + this.y0, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 32, 0, 48, 89, 2, this.x + this.x0, this.y + this.y0, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 0, this.x - 175, this.y, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 2, this.x - 144, this.y, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 0, this.x + 113, this.y, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 2, this.x + 144, this.y, Graphics.BOTTOM | Graphics.LEFT);
    }
}
