package effect;

import java.util.Vector;

public class FrameEff {
    public Vector listPart;
    public Vector listPartBottom;
    public Vector listPartTop;
    public byte xShadow;
    public byte yShadow;

    public FrameEff(Vector list) {
        this.listPart = new Vector();
        this.listPartBottom = new Vector();
        this.listPartTop = new Vector();
        this.xShadow = (byte) 0;
        this.yShadow = (byte) 0;
        this.listPart = list;
    }

    public FrameEff(Vector listtop, Vector listbottom) {
        this.listPart = new Vector();
        this.listPartBottom = new Vector();
        this.listPartTop = new Vector();
        this.xShadow = (byte) 0;
        this.yShadow = (byte) 0;
        this.listPartTop = listtop;
        this.listPartBottom = listbottom;
        this.listPart = listtop;
    }

    public Vector getListPartPaint() {
        Vector a = new Vector();
        for (int i = 0; i < this.listPartBottom.size(); i++) {
            a.addElement(this.listPartBottom.elementAt(i));
        }
        for (int i2 = 0; i2 < this.listPart.size(); i2++) {
            a.addElement(this.listPart.elementAt(i2));
        }
        return a;
    }
}
