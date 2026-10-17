package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

public class CongThanh extends Monster {
    int x0;
    int y0;
    int count;

    @Override
    public void update() {
        switch (this.state) {
            case 5:
                this.frame = (byte) 3;
                this.p1 = (short) (this.p1 + 1);
                this.x = (short) (this.x + this.p2);
                this.y = (short) (this.y + this.p3);
                this.p2 = (short) (this.p2 >> 1);
                this.p3 = (short) (this.p3 >> 1);
                if (this.p1 == 5) {
                    Canvas.gameScr.startExplosionAt(this.x, this.y);
                }
                if (this.p1 > 7) {
                    Canvas.gameScr.actors.removeElement(this);
                    GameScr.congThanh = null;
                }
                break;
        }
        this.x0 = 0;
        this.y0 = 0;
        if (this.count > 0) {
            this.count--;
            this.x0 = Res.rnd(2) - 1;
            this.y0 = Res.rnd(2) - 1;
        }
    }

    @Override
    public void setRealHP(int realHP) {
        super.setRealHP(realHP);
        this.count = 20;
    }

    @Override
    public void jumpVang(Actor causeBy) {
    }

    @Override
    public void paint(Graphics g) {
        // pokemon: template da la Pokemon (lenh -91) hoac do vat PMD: ve bang PokeBody, bo renderer boss cu
        if (paintPokemon(g)) {
            return;
        }
        if (Res.monsterTemplates[this.monster_type] != null && Res.monsterTemplates[this.monster_type].image != null) {
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 32, 0, 48, 89, 0, (this.x - 48) + this.x0, this.y + this.y0, 36);
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 32, 0, 48, 89, 2, this.x + this.x0, this.y + this.y0, 36);
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 0, (this.x - 127) - 48, this.y, 36);
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 2, ((this.x - 127) + 31) - 48, this.y, 36);
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 0, (this.x + 161) - 48, this.y, 36);
            g.drawRegion(Res.monsterTemplates[this.monster_type].image, 0, 9, 32, 76, 2, ((this.x + 161) + 31) - 48, this.y, 36);
        }
    }
}
