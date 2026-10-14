package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.TField;
import javax.microedition.lcdui.Graphics;

public class InputDlg extends Dialog {
    protected String title;
    public IAction okAction;
    public IAction backAction;
    private int w;
    public boolean isMax = false;
    public TField tfInput = new TField();

    public InputDlg() {
        this.tfInput.height = 21;
        this.tfInput.y = Canvas.h - 56;
        this.tfInput.isFocus = true;
    }

    public void init() {
        this.tfInput.y = Canvas.h - 56;
        this.w = Font.borderFont.getWidth(this.title) + 30;
        if (this.w > Canvas.w) {
            String str = "";
            for (int i = 0; i < this.title.length() - 1; i++) {
                str = new StringBuffer(String.valueOf(str)).append(this.title.substring(i, i + 1)).toString();
                if (Font.borderFont.getWidth(str) >= Canvas.w - 50) {
                    this.title = new StringBuffer(String.valueOf(str)).append("...").toString();
                    this.w = Font.borderFont.getWidth(this.title) + 30;
                    break;
                }
            }
        } else if (!this.isMax) {
            this.w = Canvas.w - 30;
        }
        if (this.w < 110) {
            this.w = 110;
        }
        this.tfInput.width = this.w - 40;
        this.tfInput.x = Canvas.hw - (this.tfInput.width / 2);
    }

    public void setInfo(String title, IAction ok, int type, int num, boolean isMaxW) {
        this.isMax = isMaxW;
        this.tfInput.setText("");
        this.tfInput.setIputType(type);
        this.tfInput.setMaxTextLenght(num);
        this.title = title;
        this.okAction = ok;
        this.backAction = new IAction() {
            @Override
            public void perform() {
                Canvas.endDlg();
            }
        };
        this.left = new Command("Đóng", this.backAction);
        this.center = new Command("Ok", this.okAction);
        this.right = this.tfInput.cmdClear;
        init();
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        Res.paintDlgFull(g, Canvas.hw - (this.w / 2), Canvas.h - 90, this.w, 69);
        Font.borderFont.drawString(g, this.title, Canvas.hw, Canvas.h - 86, 2);
        this.tfInput.paint(g);
        super.paint(g);
    }

    @Override
    public void keyPress(int keyCode) {
        boolean swallow = this.tfInput.keyPressed(keyCode);
        if (swallow) {
            Canvas.keyPressed[5] = false;
        }
    }

    @Override
    public void update() {
        this.tfInput.update();
        super.updateKey();
    }

    @Override
    public void updateKey() {
    }
}
