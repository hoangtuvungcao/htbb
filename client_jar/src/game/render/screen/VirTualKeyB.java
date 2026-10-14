package game.render.screen;

import game.model.FrameImage;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public class VirTualKeyB {
    public int x;
    public int y;
    public int w;
    public int h;
    public int wO;
    public int hO;
    public int xA;
    public int yA;
    public int hA;
    public int wA;
    public int numW;
    public int numH;
    public int numKeyW;
    public int numHeyH;
    private String[] printA;
    private String[] printabc;
    private String[] printABC;
    private String[] print123;
    private String[] print;
    private byte[] keyMapReleased;
    public static boolean isABC = false;
    public static boolean isChangeMode = false;
    long timeChangeMode;
    public static FrameImage arrow2;
    int indexClick = -1;
    String text = "";
    int delay = 0;

    public VirTualKeyB() {
        if (Canvas.isVirTualKey) {
            Canvas.isVirHorizontal = false;
            this.h = (Canvas.h / 6) * 2;
            Canvas.h -= this.h;
            this.x = 0;
            this.y = Canvas.h;
            this.w = Canvas.w;
            this.wO = this.w / 4;
            this.hO = this.h / 2;
            this.xA = this.x;
            this.yA = this.y;
            this.hA = this.h / 3;
            this.wA = this.w / 4;
            this.numW = 4;
            this.numH = 2;
            this.numKeyW = 4;
            this.numHeyH = 3;
            this.printA = new String[]{"-", "Top", "ABC", "-", "Left", "Down", "Right", "OK"};
            this.printabc = new String[]{".,?!1", "abc2", "def3", "Xóa", "ghi4", "jkl5", "mno6", "Xong", "pqrs7", "tuv8", "wxyz9", "0"};
            this.printABC = new String[]{".,?!1", "ABC2", "DEF3", "Xóa", "GHI4", "JKL5", "MNO6", "Xong", "PQRS7", "TUV8", "WXYZ9", "0"};
            this.print123 = new String[]{"1", "2", "3", "Xóa", "4", "5", "6", "Xong", "7", "8", "9", "0"};
            this.keyMapReleased = new byte[]{-6, -1, 0, -7, -3, -2, -4, -5};
            this.timeChangeMode = -1L;
            setTextMode();
        }
    }

    private void setKeyPressedABC(int indexClick2) {
        if (Canvas.isVirHorizontal && indexClick2 < 9) {
            if (Canvas.currentScreen == Canvas.gameScr && Canvas.currentDialog == null && !Canvas.menu.showMenu && !Canvas.gameScr.chatMode) {
                Canvas.gameScr.chatMode = true;
                Canvas.gameScr.tfChat.setText("");
            } else {
                Canvas.instance.keyPressed(49 + indexClick2);
                return;
            }
        }
        if (!Canvas.isVirHorizontal && indexClick2 % 4 != 3) {
            if (Canvas.currentScreen == Canvas.gameScr && Canvas.currentDialog == null && !Canvas.menu.showMenu && !Canvas.gameScr.chatMode) {
                Canvas.gameScr.chatMode = true;
                Canvas.gameScr.tfChat.setText("");
                return;
            } else {
                Canvas.instance.keyPressed((49 + indexClick2) - (indexClick2 / 4));
                return;
            }
        }
        switch (indexClick2) {
            case 3:
                if (Canvas.gameScr.chatMode) {
                    Canvas.gameScr.tfChat.clear();
                    break;
                } else if (Canvas.currentScreen.right != null && Canvas.currentScreen.right.caption.equals("Xóa")) {
                    Canvas.currentScreen.right.action.perform();
                    break;
                }
                break;
            case 7:
            case 9:
                isABC = false;
                break;
            case 10:
                Canvas.instance.keyPressed(48);
                break;
            case 11:
                if (Canvas.isVirHorizontal) {
                    if (Canvas.gameScr.chatMode) {
                        Canvas.gameScr.tfChat.clear();
                        break;
                    } else if (Canvas.currentScreen.right != null && Canvas.currentScreen.right.caption.equals("Xóa")) {
                        Canvas.currentScreen.right.action.perform();
                        break;
                    }
                } else {
                    Canvas.instance.keyPressed(48);
                    break;
                }
                break;
        }
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$PrimitiveArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    private void setKeyPressed(int index) {
        if (index == 2) {
            this.timeChangeMode = System.currentTimeMillis() / 10;
            isChangeMode = true;
        } else {
            Canvas.instance.keyPressed(this.keyMapReleased[index]);
        }
    }

    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$PrimitiveArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    private void setKeyReleased(int index) {
        if (index != 2 && index < this.keyMapReleased.length) {
            Canvas.instance.keyReleased(this.keyMapReleased[index]);
        }
    }

    public void update() {
        if (isABC) {
            updateKeyABC();
        } else {
            updateKey();
        }
    }

    private void updateKeyABC() {
        if (Canvas.isPointer(this.xA, this.yA, this.w, this.h)) {
            if (Canvas.isPointerDown) {
                int px = (Canvas.px - this.xA) / this.wA;
                int py = (Canvas.py - this.yA) / this.hA;
                this.indexClick = (py * this.numKeyW) + px;
                setKeyPressedABC(this.indexClick);
                Canvas.isPointerDown = false;
                Canvas.isPointerClick = false;
            }
            if (Canvas.isPointerClick && this.indexClick != -1) {
                setKeyReleasedABC(this.indexClick);
                this.indexClick = -1;
                Canvas.isPointerClick = false;
            }
        }
    }

    private void updateKey() {
        if (isChangeMode && Canvas.isPointerClick) {
            isChangeMode = false;
            if ((System.currentTimeMillis() / 10) - this.timeChangeMode > 40) {
                TField.setMode();
                setTextMode();
            } else {
                this.indexClick = -1;
                isABC = true;
            }
        }
        if (Canvas.isPointer(this.x, this.y, this.w, this.h)) {
            if (Canvas.isPointerDown) {
                int px = (Canvas.px - this.x) / this.wO;
                int py = (Canvas.py - this.y) / this.hO;
                this.indexClick = (py * this.numW) + px;
                setKeyPressed(this.indexClick);
                Canvas.isPointerDown = false;
                Canvas.isPointerClick = false;
            }
            if (Canvas.isPointerClick && this.indexClick != -1) {
                setKeyReleased(this.indexClick);
                this.indexClick = -1;
                Canvas.isPointerClick = false;
            }
        }
    }

    private void setTextMode() {
        switch (TField.mode) {
            case 0:
            case 1:
                this.print = this.printabc;
                break;
            case 2:
                this.print = this.printABC;
                break;
            case 3:
                this.print = this.print123;
                break;
        }
    }

    private void setKeyReleasedABC(int indexClick2) {
    }

    public void paint(Graphics g) {
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        if (isABC) {
            paintABC(g);
        } else {
            paintKey(g);
        }
    }

    private void paintABC(Graphics g) {
        g.setClip(this.xA, this.yA, this.w, this.h);
        g.setColor(15070459);
        g.fillRect(this.xA, this.yA, this.w, this.h);
        g.setColor(1);
        g.drawRect(this.xA, this.yA, this.w - 1, this.h - 1);
        for (int i = 1; i < this.numKeyW; i++) {
            g.fillRect(this.xA + (i * this.wA), this.yA, 1, this.h);
        }
        for (int i2 = 1; i2 < this.numHeyH; i2++) {
            g.fillRect(this.xA, this.yA + (i2 * this.hA), this.w, 1);
        }
        for (int i3 = 0; i3 < this.printABC.length; i3++) {
            g.setClip(this.xA + ((i3 % this.numKeyW) * this.wA), (this.yA + ((i3 / this.numKeyW) * this.hA)) - 5, this.wA, this.hA + 5);
            if (this.indexClick == i3) {
                g.setColor(16514186);
                g.fillRect(this.xA + ((i3 % this.numKeyW) * this.wA) + 1, this.yA + ((i3 / this.numKeyW) * this.hA) + 1, this.wA - 2, this.hA - 2);
            }
            Font.borderFont.drawString(g, this.print[i3], this.xA + ((i3 % this.numKeyW) * this.wA) + (this.wA / 2), ((this.yA + ((i3 / this.numKeyW) * this.hA)) - 5) + (this.hA / 2), 2);
        }
    }

    public void paintKey(Graphics g) {
        g.setClip(this.x, this.y, this.w, this.h);
        g.setColor(15070459);
        g.fillRect(this.x, this.y, this.w, this.h);
        g.setColor(1);
        g.drawRect(this.x, this.y, this.w - 1, this.h - 1);
        for (int i = 1; i < this.numW; i++) {
            g.fillRect(this.x + (i * this.wO), this.y, 1, this.h);
        }
        for (int i2 = 1; i2 < this.numH; i2++) {
            g.fillRect(this.x, this.y + (i2 * this.hO), this.w, 1);
        }
        for (int i3 = 0; i3 < this.printA.length; i3++) {
            if (this.indexClick == i3) {
                g.setColor(16514186);
                g.fillRect(this.x + ((i3 % this.numW) * this.wO) + 1, this.y + ((i3 / this.numW) * this.hO) + 1, this.wO - 2, this.hO - 2);
            }
            if (this.printA[i3].equals("ABC")) {
                Font.borderFont.drawString(g, TField.modeNotify[TField.mode], this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), ((this.y + ((i3 / this.numW) * this.hO)) - 5) + (this.hO / 2), 2);
            } else if (this.printA[i3].equals("Top")) {
                arrow2.drawFrame(0, this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), this.y + ((i3 / this.numW) * this.hO) + (this.hO / 2), 4, 3, g);
            } else if (this.printA[i3].equals("Down")) {
                arrow2.drawFrame(0, this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), this.y + ((i3 / this.numW) * this.hO) + (this.hO / 2), 7, 3, g);
            } else if (this.printA[i3].equals("Left")) {
                arrow2.drawFrame(0, this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), this.y + ((i3 / this.numW) * this.hO) + (this.hO / 2), 0, 3, g);
            } else if (this.printA[i3].equals("Right")) {
                arrow2.drawFrame(0, this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), this.y + ((i3 / this.numW) * this.hO) + (this.hO / 2), 2, 3, g);
            } else {
                Font.borderFont.drawString(g, this.printA[i3], this.x + ((i3 % this.numW) * this.wO) + (this.wO / 2), ((this.y + ((i3 / this.numW) * this.hO)) - 5) + (this.hO / 2), 2);
            }
        }
    }
}
