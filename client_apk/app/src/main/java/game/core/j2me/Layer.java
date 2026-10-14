package game.core.j2me;

import android.graphics.Canvas;

public abstract class Layer {
    int height;
    boolean visible = true;
    int width;
    int x;
    int y;

    public abstract void paint(Canvas canvas);

    Layer(int i, int i2) {
        setWidthImpl(i);
        setHeightImpl(i2);
    }

    public void setPosition(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public void move(int i, int i2) {
        this.x += i;
        this.y += i2;
    }

    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public void setVisible(boolean z) {
        this.visible = z;
    }

    public final boolean isVisible() {
        return this.visible;
    }

    void setWidthImpl(int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        this.width = i;
    }

    void setHeightImpl(int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        this.height = i;
    }
}
