package effect;

import java.util.Vector;

public class FrameEff {
    public Vector listPart;
    public Vector listPartBottom;
    public Vector listPartTop;
    public byte xShadow;
    public byte yShadow;

    public FrameEff(Vector vector) {
        this.listPart = new Vector();
        this.listPartBottom = new Vector();
        this.listPartTop = new Vector();
        this.xShadow = (byte) 0;
        this.yShadow = (byte) 0;
        this.listPart = vector;
    }

    public FrameEff(Vector vector, Vector vector2) {
        this.listPart = new Vector();
        this.listPartBottom = new Vector();
        new Vector();
        this.xShadow = (byte) 0;
        this.yShadow = (byte) 0;
        this.listPartTop = vector;
        this.listPartBottom = vector2;
        this.listPart = vector;
    }

    public Vector getListPartPaint() {
        Vector vector = new Vector();
        for (int i = 0; i < this.listPartBottom.size(); i++) {
            vector.addElement(this.listPartBottom.elementAt(i));
        }
        for (int i2 = 0; i2 < this.listPart.size(); i2++) {
            vector.addElement(this.listPart.elementAt(i2));
        }
        return vector;
    }
}
