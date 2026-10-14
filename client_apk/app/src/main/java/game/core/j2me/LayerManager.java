package game.core.j2me;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;

public class LayerManager {
    private Layer[] component = new Layer[4];
    private int nlayers;
    private int viewHeight;
    private int viewWidth;
    private int viewX;
    private int viewY;

    public LayerManager() {
        setViewWindow(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public void append(Layer layer) {
        removeImpl(layer);
        addImpl(layer, this.nlayers);
    }

    public void insert(Layer layer, int i) {
        if (i < 0 || i > this.nlayers || (exist(layer) && i >= this.nlayers)) {
            throw new IndexOutOfBoundsException();
        }
        removeImpl(layer);
        addImpl(layer, i);
    }

    public Layer getLayerAt(int i) {
        if (i < 0 || i >= this.nlayers) {
            throw new IndexOutOfBoundsException();
        }
        return this.component[i];
    }

    public int getSize() {
        return this.nlayers;
    }

    public void remove(Layer layer) {
        removeImpl(layer);
    }

    public void paint(Canvas canvas, int i, int i2) {
        Rect clipBounds = canvas.getClipBounds();
        canvas.translate(i - this.viewX, i2 - this.viewY);
        int i3 = this.viewX;
        int i4 = this.viewY;
        canvas.clipRect(i3, i4, this.viewWidth + i3, this.viewHeight + i4);
        int i5 = this.nlayers;
        while (true) {
            i5--;
            if (i5 >= 0) {
                Layer layer = this.component[i5];
                if (layer.visible) {
                    layer.paint(canvas);
                }
            } else {
                canvas.translate((-i) + this.viewX, (-i2) + this.viewY);
                canvas.clipRect(clipBounds, Region.Op.REPLACE);
                return;
            }
        }
    }

    public void setViewWindow(int i, int i2, int i3, int i4) {
        if (i3 < 0 || i4 < 0) {
            throw new IllegalArgumentException();
        }
        this.viewX = i;
        this.viewY = i2;
        this.viewWidth = i3;
        this.viewHeight = i4;
    }

    private void addImpl(Layer layer, int i) {
        int i2 = this.nlayers;
        Layer[] layerArr = this.component;
        if (i2 == layerArr.length) {
            Layer[] layerArr2 = new Layer[i2 + 4];
            System.arraycopy(layerArr, 0, layerArr2, 0, i2);
            System.arraycopy(this.component, i, layerArr2, i + 1, this.nlayers - i);
            this.component = layerArr2;
        } else {
            System.arraycopy(layerArr, i, layerArr, i + 1, i2 - i);
        }
        this.component[i] = layer;
        this.nlayers++;
    }

    private void removeImpl(Layer layer) {
        layer.getClass();
        int i = this.nlayers;
        while (true) {
            i--;
            if (i < 0) {
                return;
            }
            if (this.component[i] == layer) {
                remove(i);
            }
        }
    }

    private boolean exist(Layer layer) {
        if (layer == null) {
            return false;
        }
        int i = this.nlayers;
        do {
            i--;
            if (i < 0) {
                return false;
            }
        } while (this.component[i] != layer);
        return true;
    }

    private void remove(int i) {
        Layer[] layerArr = this.component;
        System.arraycopy(layerArr, i + 1, layerArr, i, (this.nlayers - i) - 1);
        Layer[] layerArr2 = this.component;
        int i2 = this.nlayers - 1;
        this.nlayers = i2;
        layerArr2[i2] = null;
    }
}
