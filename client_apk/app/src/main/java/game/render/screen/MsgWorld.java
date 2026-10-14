package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Command;
import game.model.IAction;
import game.model.MsgInfo;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class MsgWorld extends Screen {
    public static MsgWorld me;
    public int cmdy;
    public int cmtoY;
    public int cmvy;
    public int cmy;
    public int cmyLim;
    public Vector data = new Vector();
    public int limSelect;
    int pa;
    public int selected;
    boolean trans;

    public static MsgWorld gI() {
        MsgWorld msgWorld = me;
        if (msgWorld != null) {
            return msgWorld;
        }
        MsgWorld msgWorld2 = new MsgWorld();
        me = msgWorld2;
        return msgWorld2;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    @Override
    public void init() {
        this.data = new Vector();
        int size = GameScr.savemsgWorld.size();
        for (int i = 0; i < size; i++) {
            MsgInfo msgInfo = (MsgInfo) GameScr.savemsgWorld.elementAt(i);
            msgInfo.arr = Font.arialFont11.splitFontBStrInLine(msgInfo.message, GCanvas.w - 44);
            this.data.addElement(msgInfo);
            this.limSelect += msgInfo.arr.length;
        }
        int i2 = ((this.limSelect * 15) - (GCanvas.h - 60)) + 35;
        this.cmyLim = i2;
        if (i2 < 0) {
            this.cmyLim = 0;
        }
        this.cmtoY = 0;
        this.cmy = 0;
    }

    public MsgWorld() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
                MsgWorld msgWorld = MsgWorld.this;
                msgWorld.limSelect = 0;
                msgWorld.selected = 0;
            }
        });
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.gameScr.paint(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        Res.paintDlgFull(graphics, 20, 20, GCanvas.w - 40, GCanvas.h - 60);
        int i = 2;
        Font.normalFont[0].drawString(graphics, "Kênh thế giới", GCanvas.w / 2, 26, 2);
        graphics.setClip(20, 46, GCanvas.w - 40, GCanvas.h - 90);
        graphics.ClipRec(20, 46, GCanvas.w - 40, GCanvas.h - 90);
        graphics.translate(20, 47);
        graphics.translate(0, -this.cmy);
        int size = this.data.size();
        for (int i2 = 0; i2 < size; i2++) {
            MsgInfo msgInfo = (MsgInfo) this.data.elementAt(i2);
            for (int i3 = 0; i3 < msgInfo.arr.length; i3++) {
                Font.arialFont11.drawString(graphics, msgInfo.arr[i3], 8, i, 0);
                i += 15;
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        super.paint(graphics);
    }

    @Override
    public void updateKey() {
        boolean z = true;
        if (GCanvas.isPointer(20, 20, GCanvas.w - 40, GCanvas.h - 60)) {
            if (GCanvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = this.cmy;
                    this.trans = true;
                }
                if (Math.abs(GCanvas.pyLast - GCanvas.py) != 0) {
                    int i = this.pa + (GCanvas.pyLast - GCanvas.py);
                    this.cmtoY = i;
                    if (i < 0) {
                        this.cmtoY = 0;
                    }
                    int i2 = this.cmtoY;
                    int i3 = this.cmyLim;
                    if (i2 > i3) {
                        this.cmtoY = i3;
                    }
                }
            }
            if (GCanvas.isPointerClick) {
                GCanvas.isPointerClick = false;
                int i4 = ((this.cmtoY + GCanvas.py) - 47) / 15;
                this.selected = i4;
                int i5 = this.limSelect;
                if (i4 > i5 - 1) {
                    this.selected = i5 - 1;
                }
                if (this.selected < 0) {
                    this.selected = 0;
                }
                this.trans = false;
            }
        }
        if (GCanvas.keyPressed[8]) {
            GCanvas.keyPressed[8] = false;
            int i6 = this.selected + 1;
            this.selected = i6;
            int i7 = this.limSelect;
            if (i6 > i7 - 1) {
                this.selected = i7 - 1;
            }
        } else if (GCanvas.keyPressed[2]) {
            GCanvas.keyPressed[2] = false;
            int i8 = this.selected - 1;
            this.selected = i8;
            if (i8 < 0) {
                this.selected = 0;
            }
        } else {
            z = false;
        }
        int i9 = this.cmy;
        int i10 = this.cmtoY;
        if (i9 != i10) {
            int i11 = (i10 - i9) << 2;
            this.cmvy = i11;
            int i12 = this.cmdy + i11;
            int i13 = i9 + (i12 >> 4);
            this.cmy = i13;
            this.cmdy = i12 & 15;
            if (i13 < 0) {
                this.cmy = 0;
            }
            int i14 = this.cmy;
            int i15 = this.cmyLim;
            if (i14 > i15) {
                this.cmy = i15;
            }
        }
        if (z) {
            this.cmtoY = (this.selected * 15) - ((GCanvas.h - 60) / 5);
        }
        super.updateKey();
    }

    @Override
    public void update() {
        GCanvas.gameScr.update();
        super.update();
    }
}
