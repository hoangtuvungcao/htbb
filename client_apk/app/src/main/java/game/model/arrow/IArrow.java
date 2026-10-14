package game.model.arrow;

import game.core.j2me.Graphics;
import game.model.LiveActor;

public abstract class IArrow {
    public boolean wantDestroy;

    public abstract void onArrowTouchTarget();

    public abstract void paint(Graphics graphics);

    public abstract void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2);

    public abstract void setAngle(int i);

    public abstract void update();
}
