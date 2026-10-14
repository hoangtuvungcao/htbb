package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.TField;

public class InputDlg extends Dialog {
    public IAction backAction;
    public boolean isMax = false;
    public IAction okAction;
    public TField tfInput;
    protected String title;
    private int w;

    @Override
    public void updateKey() {
    }

    public InputDlg() {
        TField tField = new TField();
        this.tfInput = tField;
        tField.height = 21;
        this.tfInput.y = GCanvas.h - 76;
        this.tfInput.isFocus = true;
    }

    public void init() {
        this.tfInput.y = GCanvas.h - 76;
        int width = Font.normalFont[0].getWidth(this.title) + 30;
        this.w = width;
        if (width > GCanvas.w) {
            String str = "";
            int i = 0;
            while (i < this.title.length() - 1) {
                int i2 = i + 1;
                str = str + this.title.substring(i, i2);
                if (Font.normalFont[0].getWidth(str) >= GCanvas.w - 50) {
                    this.title = str + "...";
                    this.w = Font.normalFont[0].getWidth(this.title) + 30;
                    break;
                }
                i = i2;
            }
        } else if (!this.isMax) {
            this.w = GCanvas.w - 30;
        }
        if (this.w < 110) {
            this.w = 110;
        }
        this.tfInput.width = this.w - 40;
        this.tfInput.x = GCanvas.hw - (this.tfInput.width / 2);
    }

    public void setInfo(String str, IAction iAction, int i, int i2, boolean z) {
        this.isMax = z;
        this.tfInput.setText("");
        this.tfInput.setIputType(i);
        this.tfInput.setMaxTextLenght(i2);
        this.title = str;
        this.okAction = iAction;
        this.backAction = new IAction() {
            @Override
            public void perform() {
                GCanvas.endDlg();
            }
        };
        this.left = new Command("Đóng", this.backAction);
        this.center = new Command("Ok", this.okAction);
        this.right = this.tfInput.cmdClear;
        init();
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        Res.paintDlgFull(graphics, GCanvas.hw - (this.w / 2), this.tfInput.y - 30, this.w, 69);
        Font.normalFont[0].drawString(graphics, this.title, GCanvas.hw, this.tfInput.y - 24, 2);
        this.tfInput.paint(graphics);
        super.paint(graphics);
    }

    @Override
    public void keyPress(int i) {
        if (this.tfInput.keyPressed(i)) {
            GCanvas.keyPressed[5] = false;
        }
    }

    @Override
    public void update() {
        this.tfInput.update();
        super.updateKey();
    }
}
