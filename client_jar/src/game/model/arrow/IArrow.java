package game.model.arrow;

import game.model.LiveActor;
import javax.microedition.lcdui.Graphics;

public abstract class IArrow {
    public boolean wantDestroy;

    public abstract void set(int i, int i2, int i3, int i4, byte b, LiveActor liveActor, LiveActor liveActor2);

    public abstract void setAngle(int i);

    public abstract void update();

    public abstract void paint(Graphics graphics);

    public abstract void onArrowTouchTarget();
}
