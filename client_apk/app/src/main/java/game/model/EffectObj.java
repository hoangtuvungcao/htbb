package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;

public class EffectObj extends Actor {
    public short ID;
    public short idPlayer;
    public byte index;
    public byte style;
    public short dy = 0;
    public short dx = 0;

    @Override
    public void setPosTo(short s, short s2) {
    }

    public EffectObj() {
        this.catagory = (byte) 100;
        this.index = (byte) 0;
    }

    @Override
    public void update() {
        EffectData effect2 = EffectServerManager.getEffect(this.ID);
        if (effect2 != null) {
            byte b = (byte) (this.index + 1);
            this.index = b;
            if (b >= effect2.arrFrame.length) {
                remove();
                return;
            }
            return;
        }
        remove();
    }

    @Override
    public void paint(Graphics graphics) {
        Actor actorFindActorByID;
        EffectData effect2 = EffectServerManager.getEffect(this.ID);
        if (effect2 != null) {
            if (this.style == 0 && (actorFindActorByID = GCanvas.gameScr.findActorByID(this.idPlayer)) != null) {
                this.x = (short) (actorFindActorByID.x + this.dx);
                this.y = (short) (actorFindActorByID.y + this.dy);
            }
            effect2.paint(graphics, this.x, this.y, this.index);
        }
    }

    private void remove() {
        GCanvas.gameScr.actors.removeElement(this);
    }
}
