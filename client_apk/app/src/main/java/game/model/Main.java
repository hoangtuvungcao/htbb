package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;

public abstract class Main {
    public static int ITEM_HEIGHT = 30;
    public static Bitmap ImgBpa = null;
    public static int hTab = 35;
    public Command center;
    public Command left;
    public Command right;
    boolean isQwerty = false;
    int index = -1;

    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        Command command = this.left;
        if (command != null) {
            if (command.caption != "") {
                if (GCanvas.instance.focusBtnBar() == 1) {
                    graphics.drawImage(Res.imgBtnBarFocus, 0, GCanvas.h - 35);
                } else {
                    graphics.drawImage(Res.imgButtonBar, 5, GCanvas.h - 27);
                }
            }
            Font.arialFont11.drawString(graphics, this.left.caption, 39, (GCanvas.h - Screen.deltaY) - 4, 2);
        }
        Command command2 = this.center;
        if (command2 != null) {
            if (command2.caption != "") {
                if (GCanvas.instance.focusBtnBar() == 2) {
                    graphics.drawImage(Res.imgBtnBarFocus, GCanvas.hw - 39, GCanvas.h - 35);
                } else {
                    graphics.drawImage(Res.imgButtonBar, GCanvas.hw - 34, GCanvas.h - 27);
                }
            }
            Font.arialFont11.drawString(graphics, this.center.caption, GCanvas.hw + 1, (GCanvas.h - Screen.deltaY) - 4, 2);
        }
        Command command3 = this.right;
        if (command3 != null) {
            if (command3.caption != "") {
                if (GCanvas.instance.focusBtnBar() == 3) {
                    graphics.drawImage(Res.imgBtnBarFocus, GCanvas.w - 78, GCanvas.h - 35);
                } else {
                    graphics.drawImage(Res.imgButtonBar, GCanvas.w - 73, GCanvas.h - 27);
                }
            }
            Font.arialFont11.drawString(graphics, this.right.caption, GCanvas.w - 39, (GCanvas.h - Screen.deltaY) - 4, 2);
        }
        Bitmap bitmap = ImgBpa;
        if (bitmap != null) {
            graphics.drawImage(bitmap, GCanvas.hw, 0, Graphics.TOP | Graphics.HCENTER);
        }
    }

    public void updateKey() {
        Command command;
        if (GCanvas.isPointerClick && Math.abs(GCanvas.pyLast - GCanvas.py) <= 10 && Math.abs(GCanvas.pxLast - GCanvas.px) <= 10) {
            int iCollisionCmdBar = GCanvas.collisionCmdBar();
            if (iCollisionCmdBar == 0) {
                this.index = 0;
                Command command2 = this.left;
                if (command2 != null && command2.action != null) {
                    this.left.action.perform();
                    GCanvas.isPointerClick = false;
                }
            } else if (iCollisionCmdBar == 1) {
                this.index = 1;
                Command command3 = this.center;
                if (command3 != null && command3.action != null) {
                    GCanvas.endDlg();
                    this.center.action.perform();
                    GCanvas.isPointerClick = false;
                }
            } else if (iCollisionCmdBar == 2) {
                this.index = 2;
                Command command4 = this.right;
                if (command4 != null && command4.action != null) {
                    this.right.action.perform();
                    GCanvas.isPointerClick = false;
                }
            }
        }
        if (GCanvas.keyPressed[5]) {
            Command command5 = this.center;
            if (command5 == null || command5.action == null) {
                return;
            }
            this.center.action.perform();
            GCanvas.keyPressed[5] = false;
            return;
        }
        if (GCanvas.keyPressed[12]) {
            Command command6 = this.left;
            if (command6 == null || command6.action == null) {
                return;
            }
            this.left.action.perform();
            GCanvas.keyPressed[12] = false;
            return;
        }
        if (!GCanvas.keyPressed[13] || (command = this.right) == null || command.action == null) {
            return;
        }
        this.right.action.perform();
        GCanvas.keyPressed[13] = false;
    }
}
