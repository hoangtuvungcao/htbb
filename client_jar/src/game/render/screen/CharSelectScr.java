package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import network.Session_ME;

public class CharSelectScr extends Screen {
    public static CharSelectScr me;
    public MainChar[] charInfos;
    MainChar[] chars;
    public int selected = 0;
    public Command cmdSelect;
    public Command cmdNew;
    public Command cmdDel;
    public Command cmdClose;
    public static Image imgBg;

    public static CharSelectScr gI() {
        if (me != null) {
            return me;
        }
        CharSelectScr charSelectScr = new CharSelectScr();
        me = charSelectScr;
        return charSelectScr;
    }

    public void setCharList(MainChar[] charInfos) {
        if (!Session_ME.gI().connected) {
            return;
        }
        Canvas.loginScr.savePass();
        if (!Canvas.gameScr.isNewVersionAvailable) {
            Canvas.endDlg();
        }
        this.charInfos = charInfos;
        this.chars = new MainChar[charInfos.length];
        this.chars = charInfos;
        this.cmdSelect = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                Canvas.startWaitDlg("Xin chờ..", true);
                GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 1);
                Canvas.gameScr.mainChar.imgWpa = CharSelectScr.this.chars[CharSelectScr.this.selected].imgWpa;
                Canvas.gameScr.mainChar.dxWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dxWear;
                Canvas.gameScr.mainChar.dyWear = CharSelectScr.this.chars[CharSelectScr.this.selected].dyWear;
                ChangScr.gI().switchToMe();
                CharSelectScr.this.chars = null;
                CharSelectScr.me = null;
            }
        });
        this.cmdNew = new Command("Tạo mới", new IAction() {
            @Override
            public void perform() {
                CreateCharScr.gI().switchToMe();
            }
        });
        this.cmdDel = new Command("Xóa", new AnonymousClass3());
        this.cmdClose = new Command("Thoát", new IAction() {
            @Override
            public void perform() {
                Session_ME.gI().close();
                Canvas.loginScr.switchToMe();
            }
        });
        if (this.selected < this.chars.length) {
            this.center = this.cmdSelect;
            this.left = this.cmdDel;
        } else {
            this.center = this.cmdNew;
            this.left = null;
        }
        this.right = this.cmdClose;
        if (charInfos.length > 0) {
            if (charInfos[this.selected].using == 1) {
                this.cmdDel.caption = "Xóa";
            } else {
                this.cmdDel.caption = "Khôi phục";
            }
        }
    }

    class AnonymousClass3 implements IAction {
        AnonymousClass3() {
        }

        @Override
        public void perform() {
            if (CharSelectScr.this.cmdDel.caption.equals("Xóa")) {
                Canvas.startYesNoDlg("Bạn thật sự muốn xóa nhân vật này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 2);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            } else if (CharSelectScr.this.cmdDel.caption.equals("Khôi phục")) {
                Canvas.startYesNoDlg("Bạn muốn khôi phục nhân vật này?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg("Xin chờ..", true);
                        GameService.gI().selectChar(CharSelectScr.this.charInfos[CharSelectScr.this.selected].IDDB, 3);
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        ServerListScr.gI().paintBg(g);
        Canvas.resetTrans(g);
        Font.borderFont.drawString(g, "Chọn nhân vật:", Canvas.hw, Canvas.hh - 65, 2);
        int xStart = Canvas.hw - 60;
        int yStart = Canvas.hh - 30;
        int i = 0;
        int x = xStart;
        while (i < 3) {
            if (i == this.selected) {
                g.setColor(15769344);
                if (i < this.charInfos.length) {
                    int i2 = (this.charInfos.length <= 0 || this.charInfos[i].using == 1) ? 15769344 : 2205861;
                    g.setColor(i2);
                }
            } else {
                g.setColor(10644224);
                if (i < this.charInfos.length) {
                    int i3 = (this.charInfos.length <= 0 || this.charInfos[i].using == 1) ? 10644224 : 34949;
                    g.setColor(i3);
                }
            }
            g.fillRect(x + 3, yStart - 10, 34, 84);
            g.setColor(1379840);
            g.drawRect(x + 3, yStart - 10, 34, 84);
            if (i < this.chars.length) {
                this.chars[i].x = (short) (x + 20);
                this.chars[i].y = (short) (yStart + 64);
                this.chars[i].paintAvatar(g, this.chars[i].x, this.chars[i].y);
            }
            i++;
            x += 40;
        }
        if (this.selected < this.chars.length) {
            Font.borderFont.drawString(g, new StringBuffer(String.valueOf(this.charInfos[this.selected].name)).append(" lv: ").append((int) (this.charInfos[this.selected].lastLv > 0 ? this.charInfos[this.selected].lastLv : (short) 1)).toString(), Canvas.hw, Canvas.hh + 45, 2);
            if (this.charInfos[this.selected].using == 0) {
                Font.borderFont.drawString(g, new StringBuffer("Còn lại: ").append((int) this.charInfos[this.selected].day).append(" ngày").toString(), Canvas.hw, Canvas.hh + 58, 2);
            }
            if (this.charInfos[this.selected].nationID != -1) {
                int ww = Font.borderFont.getWidth(new StringBuffer(String.valueOf(this.charInfos[this.selected].name)).append(" lv: ").append((int) (this.charInfos[this.selected].lastLv > 0 ? this.charInfos[this.selected].lastLv : (short) 1)).toString());
                g.drawRegion(Res.imgNation, 0, this.charInfos[this.selected].nationID * 11, 11, 11, 0, (Canvas.hw - (ww / 2)) - 15, Canvas.hh + 48, 20);
            }
        }
        super.paint(g);
    }

    @Override
    public void update() {
        Canvas.loginScr.updateCamera();
        for (int i = 0; i < this.chars.length; i++) {
            this.chars[i].update();
        }
        boolean isChangeFocus = false;
        if (Canvas.isKeyPressed(4)) {
            this.selected--;
            isChangeFocus = true;
            if (this.selected < 0) {
                this.selected = 2;
            }
            if (this.charInfos.length > 0) {
                if (this.selected < this.charInfos.length) {
                    if (this.charInfos[this.selected].using == 1) {
                        this.cmdDel.caption = "Xóa";
                    } else {
                        this.cmdDel.caption = "Khôi phục";
                    }
                } else {
                    this.cmdDel.caption = "";
                }
            }
        } else if (Canvas.isKeyPressed(6)) {
            this.selected++;
            isChangeFocus = true;
            if (this.selected > 2) {
                this.selected = 0;
            }
            if (this.charInfos.length > 0) {
                if (this.selected < this.charInfos.length) {
                    if (this.charInfos[this.selected].using == 1) {
                        this.cmdDel.caption = "Xóa";
                    } else {
                        this.cmdDel.caption = "Khôi phục";
                    }
                } else {
                    this.cmdDel.caption = "";
                }
            }
        }
        int pa = -1;
        if (Canvas.isPointerClick && Canvas.isPointer(Canvas.hw - 58, Canvas.hh - 40, 115, 84)) {
            pa = (Canvas.px - (Canvas.hw - 58)) / 38;
            isChangeFocus = true;
        }
        if (isChangeFocus) {
            if (this.selected < this.chars.length) {
                this.center = this.cmdSelect;
                this.left = this.cmdDel;
            } else {
                this.center = this.cmdNew;
                this.left = null;
            }
            if (pa == this.selected) {
                this.center.action.perform();
            }
            if (pa < 3 && pa >= 0) {
                this.selected = pa;
            }
        }
        super.update();
    }
}
