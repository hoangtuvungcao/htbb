package game.render.screen;

import game.core.j2me.Graphics;
import game.model.ClanInfo;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class ViewInfoScr extends Screen {
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static int lastSe;
    private static ViewInfoScr me;
    public static int selected;
    private short h;
    private Screen lastScr;
    private Vector text;
    private String title;
    private short w;
    private int type = 0;
    private short idClan = -1;
    private String status = "";
    int xa = 0;
    int dir = 1;

    public static ViewInfoScr gI() {
        ViewInfoScr viewInfoScr = me;
        if (viewInfoScr != null) {
            return viewInfoScr;
        }
        ViewInfoScr viewInfoScr2 = new ViewInfoScr();
        me = viewInfoScr2;
        return viewInfoScr2;
    }

    @Override
    public void switchToMe() {
        if (this.lastScr == null) {
            this.lastScr = GCanvas.currentScreen;
        }
        super.switchToMe();
    }

    public ViewInfoScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                ViewInfoScr.this.lastScr.switchToMe();
                ViewInfoScr.this.lastScr = null;
                ViewInfoScr.this.text.removeAllElements();
                ViewInfoScr unused = ViewInfoScr.me = null;
            }
        });
        this.left = new Command("Menu", new AnonymousClass2());
        this.w = (short) 150;
        this.h = (short) 170;
        lastSe = ((170 - 30) / 2) / 14;
    }

    class AnonymousClass2 implements IAction {
        AnonymousClass2() {
        }

        @Override
        public void perform() {
            Vector vector = new Vector();
            if (ViewInfoScr.this.type == 0) {
                if (GCanvas.gameScr.mainChar.isMaster == 0) {
                    vector.addElement(new Command("Đặt slogan", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.inputDlg.setInfo("Slogan", new IAction() {
                                @Override
                                public void perform() {
                                    String text = GCanvas.inputDlg.tfInput.getText();
                                    if (text.equals("")) {
                                        return;
                                    }
                                    GameService.gI().setSologan(text);
                                    ViewInfoScr.this.status = text;
                                    GCanvas.endDlg();
                                    ViewInfoScr.this.xa = 0;
                                    ViewInfoScr.this.dir = 1;
                                }
                            }, 0, 50, false);
                            GCanvas.inputDlg.show();
                        }
                    }));
                }
                vector.addElement(new Command("Danh sách thành viên", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.startWaitDlg();
                        GameService.gI().requestClanList(ViewInfoScr.this.idClan, (byte) 0);
                    }
                }));
            }
            GCanvas.menu.startAt(vector, 0);
        }
    }

    public void setInfo(String str, String str2) {
        this.title = str;
        this.type = 2;
        this.text = new Vector();
        for (String str3 : Font.normalFont[0].splitFontBStrInLine(str2, this.w - 15)) {
            this.text.addElement(str3);
        }
        setLim();
    }

    private void setLim() {
        int size = ((this.text.size() * 14) - (this.h - 25)) + 20;
        cmyLim = size;
        if (size < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        selected = lastSe;
    }

    public void setInfo(ClanInfo clanInfo) {
        this.title = clanInfo.name.toUpperCase();
        this.type = 0;
        this.idClan = clanInfo.id;
        this.text = new Vector();
        this.status = clanInfo.status;
        this.text.addElement("Đội trưởng: " + clanInfo.master);
        this.text.addElement("Cấp độ: " + ((int) clanInfo.level));
        this.text.addElement("Xu: " + GCanvas.getMoneys(clanInfo.money));
        this.text.addElement("Thành viên: " + ((int) clanInfo.members));
        this.text.addElement("Điểm cống hiến: " + clanInfo.dedicationPoint);
        this.text.addElement("Điểm kinh ngiệm: " + clanInfo.xp);
        this.text.addElement("Thành lập: " + clanInfo.date);
        if (clanInfo.dissolved) {
            for (String str : Font.normalFont[0].splitFontBStrInLine(clanInfo.strDissolved, this.w - 15)) {
                this.text.addElement(str);
            }
        }
        setLim();
    }

    @Override
    public void updateKey() {
        int i;
        int i2;
        Screen screen;
        if (GCanvas.keyHold[2]) {
            int i3 = selected - 1;
            selected = i3;
            int i4 = lastSe;
            if (i3 < i4) {
                selected = i4;
            }
        } else {
            if (GCanvas.keyHold[8]) {
                if (cmy < cmyLim) {
                    selected++;
                }
                if (selected > (this.text.size() - lastSe) + 1) {
                    selected = (this.text.size() - lastSe) + 1;
                }
            }
            i = cmy;
            i2 = cmtoY;
            if (i != i2) {
                int i5 = (i2 - i) << 2;
                cmvy = i5;
                int i6 = cmdy + i5;
                cmy = i + (i6 >> 4);
                cmdy = i6 & 15;
            }
            super.updateKey();
            screen = this.lastScr;
            if (screen != null) {
                screen.update();
            }
        }
        int i7 = (selected * 14) - ((this.h - 25) / 2);
        cmtoY = i7;
        if (i7 < 0) {
            cmtoY = 0;
        }
        int i8 = cmtoY;
        int i9 = cmyLim;
        if (i8 > i9) {
            cmtoY = i9;
        }
        i = cmy;
        i2 = cmtoY;
        if (i != i2) {
            int i10 = (i2 - i) << 2;
            cmvy = i10;
            int i11 = cmdy + i10;
            cmy = i + (i11 >> 4);
            cmdy = i11 & 15;
        }
        super.updateKey();
        screen = this.lastScr;
        if (screen != null) {
            screen.update();
        }
    }

    @Override
    public void paint(Graphics graphics) {
        Screen screen = this.lastScr;
        if (screen != null) {
            screen.paint(graphics);
        }
        GCanvas.resetTrans(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        graphics.translate(GCanvas.hw - (this.w / 2), GCanvas.hh - (this.h / 2));
        Res.paintDlgFull(graphics, 0, 0, this.w, this.h);
        graphics.setClip(0, 0, this.w, this.h);
        Font.bigFont1.drawString(graphics, this.title, this.w >> 1, (GCanvas.screenlevel == 1 ? 4 : 0) + 4, 2);
        graphics.setClip(0, 27, this.w, this.h - 30);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(0, 27, this.w, this.h - 30);
        graphics.translate(0, -cmy);
        if (this.type == 2) {
            paintText(graphics);
        } else {
            paintTextNormal(graphics);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    private void paintTextNormal(Graphics graphics) {
        int size = this.text.size();
        int i = 34;
        for (int i2 = 0; i2 < size; i2++) {
            Font.arialFont11.drawString(graphics, (String) this.text.elementAt(i2), 5, i, 0);
            i += 14;
        }
    }

    private void paintText(Graphics graphics) {
        short s = this.idClan;
        int i = 34;
        if (s != -1) {
            GameData.paintIcon(graphics, (short) (s + 1000), this.w / 2, 34);
            i = 40;
        }
        if (this.type == 0 && !this.status.equals("")) {
            int width = Font.arialFont11.getWidth(this.status);
            if (width > this.w) {
                if (this.dir == 1) {
                    this.xa--;
                } else {
                    this.xa++;
                }
                if (Math.abs(this.xa) > ((width / 2) - (this.w / 2)) + 10) {
                    this.dir *= -1;
                }
            }
            graphics.setClip(3, 0, this.w - 6, this.h);
            graphics.ClipRec(3, 0, this.w - 6, this.h);
            Font.normalFont[0].drawString(graphics, this.status, (this.w >> 1) + this.xa, i, 2);
            graphics.restoreCanvas();
            i += 14;
        }
        int size = this.text.size();
        for (int i2 = 0; i2 < size; i2++) {
            Font.arialFont11.drawString(graphics, (String) this.text.elementAt(i2), 5, i, 0);
            i += 14;
        }
    }
}
