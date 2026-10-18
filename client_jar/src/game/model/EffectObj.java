package game.model;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public class EffectObj extends Actor {
    public short ID;
    public short idPlayer;
    public byte style;
    public byte index;
    public short dy = 0;
    public short dx = 0;

    public EffectObj() {
        this.catagory = (byte) 100;
        this.index = (byte) 0;
    }

    @Override
    public void update() {
        EffectData eff = EffectServerManager.getEffect(this.ID);
        if (eff != null) {
            this.index = (byte) (this.index + 1);
            if (this.index >= eff.arrFrame.length) {
                remove();
                return;
            }
            return;
        }
        remove();
    }

    @Override
    public void paint(Graphics g) {
        Actor ava;
        EffectData eff = EffectServerManager.getEffect(this.ID);
        if (eff != null) {
            if (this.style == 0 && (ava = Canvas.gameScr.findActorByID(this.idPlayer)) != null) {
                this.x = (short) (ava.x + this.dx);
                this.y = (short) (ava.y + this.dy);
            }
            eff.paint(g, this.x, this.y, this.index);
        }
    }

    private void remove() {
        Canvas.gameScr.actors.removeElement(this);
    }

    @Override
    public void setPosTo(short x, short y) {
    }
}
