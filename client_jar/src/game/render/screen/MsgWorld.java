package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.MsgInfo;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class MsgWorld extends Screen {
    public static MsgWorld me;
    public int cmtoY;
    public int cmy;
    public int cmdy;
    public int cmvy;
    public int cmyLim;
    public int selected;
    public int limSelect;
    public Vector data = new Vector();
    boolean trans;
    int pa;

    public static MsgWorld gI() {
        if (me != null) {
            return me;
        }
        MsgWorld msgWorld = new MsgWorld();
        me = msgWorld;
        return msgWorld;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
    }

    @Override
    public void init() {
        this.data = new Vector();
        this.limSelect = 0; // mở lại nhiều lần: trước đây cộng dồn -> cuộn quá danh sách
        for (int i = 0; i < GameScr.savemsgWorld.size(); i++) {
            MsgInfo ms = (MsgInfo) GameScr.savemsgWorld.elementAt(i);
            ms.arr = Font.arialFontW.splitFontBStrInLine(ms.message, Canvas.w - 44);
            this.data.addElement(ms);
            this.limSelect += ms.arr.length;
        }
        this.cmyLim = ((this.limSelect * 15) - (Canvas.h - 60)) + 35;
        if (this.cmyLim < 0) {
            this.cmyLim = 0;
        }
        this.cmtoY = 0;
        this.cmy = 0;
    }

    public MsgWorld() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
                MsgWorld msgWorld = MsgWorld.this;
                MsgWorld.this.limSelect = 0;
                msgWorld.selected = 0;
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        Res.paintDlgFull(g, 20, 20, Canvas.w - 40, Canvas.h - 60);
        Font.borderFont.drawString(g, "Kênh thế giới", Canvas.w / 2, 26, 2);
        g.setClip(20, 46, Canvas.w - 40, Canvas.h - 90);
        g.translate(20, 47);
        g.translate(0, -this.cmy);
        if (this.data.size() > 0) {
            Res.paintSelected(g, 4, (this.selected * 15) + 2, Canvas.w - 49, 15);
        }
        int yy = 2;
        for (int i = 0; i < this.data.size(); i++) {
            MsgInfo ms = (MsgInfo) this.data.elementAt(i);
            for (int j = 0; j < ms.arr.length; j++) {
                Font.arialFontW.drawString(g, ms.arr[j], 8, yy, 0);
                yy += 15;
            }
        }
        Canvas.resetTrans(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        super.paint(g);
    }

    @Override
    public void updateKey() {
        boolean isChange = false;
        if (Canvas.isPointer(20, 20, Canvas.w - 40, Canvas.h - 60)) {
            if (Canvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = this.cmy;
                    this.trans = true;
                }
                if (Math.abs(Canvas.pyLast - Canvas.py) != 0) {
                    this.cmtoY = this.pa + (Canvas.pyLast - Canvas.py);
                    if (this.cmtoY < 0) {
                        this.cmtoY = 0;
                    }
                    if (this.cmtoY > this.cmyLim) {
                        this.cmtoY = this.cmyLim;
                    }
                }
            }
            if (Canvas.isPointerClick) {
                Canvas.isPointerClick = false;
                this.selected = ((this.cmtoY + Canvas.py) - 47) / 15;
                if (this.selected > this.limSelect - 1) {
                    this.selected = this.limSelect - 1;
                }
                if (this.selected < 0) {
                    this.selected = 0;
                }
                this.trans = false;
            }
        }
        if (Canvas.keyPressed[8]) {
            Canvas.keyPressed[8] = false;
            this.selected++;
            if (this.selected > this.limSelect - 1) {
                this.selected = this.limSelect - 1;
            }
            isChange = true;
        } else if (Canvas.keyPressed[2]) {
            Canvas.keyPressed[2] = false;
            this.selected--;
            if (this.selected < 0) {
                this.selected = 0;
            }
            isChange = true;
        }
        if (this.cmy != this.cmtoY) {
            this.cmvy = (this.cmtoY - this.cmy) << 2;
            this.cmdy += this.cmvy;
            this.cmy += this.cmdy >> 4;
            this.cmdy &= 15;
            if (this.cmy < 0) {
                this.cmy = 0;
            }
            if (this.cmy > this.cmyLim) {
                this.cmy = this.cmyLim;
            }
        }
        if (isChange) {
            this.cmtoY = (this.selected * 15) - ((Canvas.h - 60) / 5);
        }
        super.updateKey();
    }

    @Override
    public void update() {
        Canvas.gameScr.update();
        super.update();
    }
}
