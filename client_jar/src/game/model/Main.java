package game.model;

import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameScr;
import game.render.screen.MessageScr;
import game.render.screen.Screen;
import game.render.screen.TField;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Main {
    public Command left;
    public Command center;
    public Command right;
    public static Image ImgBpa;
    boolean isQwerty = false;

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        if (this.left != null) {
            Font.borderFont.drawString(g, this.left.caption, 5, Canvas.h - Screen.deltaY, 0);
        }
        if (this.center != null) {
            Font.borderFont.drawString(g, this.center.caption, Canvas.hw, Canvas.h - Screen.deltaY, 2);
        }
        if (this.right != null) {
            Font.borderFont.drawString(g, this.right.caption, Canvas.w - 5, Canvas.h - Screen.deltaY, 1);
        }
        if (MessageScr.nMSG > 0) {
            g.drawImage(Res.imgMSG, Canvas.w - 7, 7, 3);
        }
        if (GameScr.msgWorld.size() <= 0 && ImgBpa != null) {
            g.drawImage(ImgBpa, Canvas.hw, 0, 17);
        }
    }

    public void updateKey() {
        if (Canvas.isPointerClick) {
            if (ImgBpa != null && Canvas.isPointer(Canvas.hw - 40, 0, 80, 20)) {
                if (Canvas.currentScreen == Canvas.gameScr) {
                    if (!Canvas.isVirTualKey) {
                        Vector menu = new Vector();
                        menu.addElement(new Command("Bàn phím", new IAction() {
                            @Override
                            public void perform() {
                                Canvas.endDlg();
                                if (!Canvas.isVirTualKey && TField.isQwerty) {
                                    Main.this.isQwerty = true;
                                    TField.isQwerty = false;
                                }
                                GameScr.typechat = GameScr.CHAT_NORMAL;
                                Canvas.isVirTualKey = !Canvas.isVirTualKey;
                                Canvas.instance.sizeChanged(0, 0);
                                Canvas.isPointerClick = false;
                            }
                        }));
                        menu.addElement(new Command("Thay đồ", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().setConfig(6);
                                Canvas.endDlg();
                            }
                        }));
                        menu.addElement(new Command("Tìm bạn", new IAction() {
                            @Override
                            public void perform() {
                                Canvas.endDlg();
                                if (!Canvas.isVirTualKey && TField.isQwerty) {
                                    Main.this.isQwerty = true;
                                    TField.isQwerty = false;
                                }
                                GameScr.typechat = GameScr.CHAT_TIM_BAN;
                                Canvas.isVirTualKey = !Canvas.isVirTualKey;
                                Canvas.instance.sizeChanged(0, 0);
                                Canvas.isPointerClick = false;
                            }
                        }));
                        Canvas.menu.startAt(menu, 3);
                    } else {
                        if (this.isQwerty) {
                            TField.isQwerty = true;
                        }
                        Canvas.isVirTualKey = !Canvas.isVirTualKey;
                        Canvas.instance.sizeChanged(0, 0);
                        Canvas.isPointerClick = false;
                    }
                } else {
                    if (!Canvas.isVirTualKey) {
                        if (TField.isQwerty) {
                            this.isQwerty = true;
                            TField.isQwerty = false;
                        }
                    } else if (this.isQwerty) {
                        TField.isQwerty = true;
                    }
                    Canvas.isVirTualKey = !Canvas.isVirTualKey;
                    Canvas.instance.sizeChanged(0, 0);
                    Canvas.isPointerClick = false;
                }
            }
            if (Math.abs(Canvas.pyLast - Canvas.py) <= 10 && Math.abs(Canvas.pxLast - Canvas.px) <= 10) {
                switch (Canvas.collisionCmdBar()) {
                    case 0:
                        if (this.left != null && this.left.action != null) {
                            this.left.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                    case 1:
                        if (this.center != null && this.center.action != null) {
                            Canvas.endDlg();
                            this.center.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                    case 2:
                        if (this.right != null && this.right.action != null) {
                            this.right.action.perform();
                            Canvas.isPointerClick = false;
                        }
                        break;
                }
            }
        }
        if (Canvas.keyPressed[5]) {
            if (this.center != null && this.center.action != null) {
                this.center.action.perform();
                Canvas.keyPressed[5] = false;
                return;
            }
            return;
        }
        if (Canvas.keyPressed[12]) {
            if (this.left != null && this.left.action != null) {
                this.left.action.perform();
                Canvas.keyPressed[12] = false;
                return;
            }
            return;
        }
        if (Canvas.isBB) {
            if (Canvas.keyReleased[13] && this.right != null && this.right.action != null) {
                this.right.action.perform();
                Canvas.keyReleased[13] = false;
                return;
            }
            return;
        }
        if (Canvas.keyPressed[13] && this.right != null && this.right.action != null) {
            this.right.action.perform();
            Canvas.keyPressed[13] = false;
        }
    }
}
