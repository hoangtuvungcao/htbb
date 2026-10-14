package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.model.Main;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Menu extends Main {
    public boolean showMenu;
    public Vector menuItems;
    public int selected;
    public int menuX;
    public int menuY;
    public int menuW;
    public int menuH;
    public int menuTemY;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int xc;
    public static Image img = null;
    boolean trans;
    int cvyb;
    int cdyb;
    int ctoyBag;
    int pa = 0;
    boolean transX = false;
    int cyBag = 0;
    int cylim = 0;

    public Menu() {
        this.left = new Command("Chọn", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
                if (Menu.this.selected != -1) {
                    ((Command) Menu.this.menuItems.elementAt(Menu.this.selected)).action.perform();
                }
            }
        });
        this.center = new Command("", this.left.action);
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Menu.this.showMenu = false;
            }
        });
    }

    public void setIndex() {
        if (this.menuItems != null) {
            this.selected = GameScr.r.nextInt(this.menuItems.size());
            this.ctoyBag = (this.selected * Screen.ITEM_HEIGHT) - (Screen.ITEM_HEIGHT * 2);
            if (this.ctoyBag < 0) {
                this.ctoyBag = 0;
            }
            if (this.ctoyBag > this.cylim) {
                this.ctoyBag = this.cylim;
            }
        }
    }

    public void startAt(Vector menuItems, int pos) {
        if (menuItems.size() <= 0) {
            return;
        }
        img = null;
        this.menuItems = menuItems;
        this.menuW = 0;
        this.menuH = 0;
        for (int i = 0; i < menuItems.size(); i++) {
            Command c = (Command) menuItems.elementAt(i);
            int w = Font.normalFont[0].getWidth(c.caption);
            if (w > this.menuW) {
                this.menuW = w;
            }
            this.menuH += Screen.ITEM_HEIGHT;
        }
        this.menuW += 10;
        if (this.menuW < 100) {
            this.menuW = 100;
        }
        if (this.menuH > Screen.ITEM_HEIGHT * 4) {
            this.menuH = Screen.ITEM_HEIGHT * 4;
        }
        this.menuH += 4;
        this.menuY = (Canvas.h - 27) - this.menuH;
        if (pos == 0) {
            this.menuX = 2;
        } else if (pos == 1) {
            this.menuX = (Canvas.w - this.menuW) - 2;
        } else if (pos == 2) {
            this.menuX = (Canvas.w >> 1) - (this.menuW >> 1);
            this.menuY = (Canvas.h - this.menuH) >> 1;
        } else {
            this.menuX = (Canvas.w >> 1) - (this.menuW >> 1);
        }
        if (Canvas.h < 200) {
            this.menuY += 10;
        }
        this.menuTemY = Canvas.h - Screen.ITEM_HEIGHT;
        this.showMenu = true;
        this.selected = 0;
        this.cylim = (this.menuItems.size() - 4) * Screen.ITEM_HEIGHT;
        if (this.cylim < 0) {
            this.cylim = 0;
        }
        this.selected = 0;
        this.cyBag = 0;
        this.ctoyBag = 0;
        if (Canvas.isBB) {
            Canvas.clearKeyPressed();
        }
    }

    private void setKey(int dir) {
        this.selected += dir;
        if (this.selected < 0) {
            this.selected = this.menuItems.size() - 1;
        }
        if (this.selected > this.menuItems.size() - 1) {
            this.selected = 0;
        }
        this.ctoyBag = (this.selected * Screen.ITEM_HEIGHT) - (Screen.ITEM_HEIGHT * 2);
        if (this.ctoyBag < 0) {
            this.ctoyBag = 0;
        }
        if (this.ctoyBag > this.cylim) {
            this.ctoyBag = this.cylim;
        }
    }

    public void updateMenuKey() {
        if (Canvas.isKeyPressed(2)) {
            setKey(-1);
        } else if (Canvas.isKeyPressed(8)) {
            setKey(1);
        }
        if (Canvas.isPointerDown && Canvas.isPointer(this.menuX, this.menuY, this.menuW, this.menuH)) {
            if (!this.trans) {
                this.pa = this.cyBag;
                this.trans = true;
                this.cyBag = this.ctoyBag;
            }
            int b = this.menuTemY + 2;
            int a = ((this.ctoyBag + Canvas.py) - b) / Screen.ITEM_HEIGHT;
            if (a < 0) {
                a = 0;
            }
            if (a > this.menuItems.size() - 1) {
                a = this.menuItems.size() - 1;
            }
            this.selected = a;
            if (Math.abs(Canvas.pyLast - Canvas.py) != 0) {
                this.ctoyBag = this.pa + (Canvas.pyLast - Canvas.py);
                if (this.ctoyBag < 0) {
                    this.ctoyBag = 0;
                }
                if (this.ctoyBag > this.cylim) {
                    this.ctoyBag = this.cylim;
                }
                this.selected = -1;
            }
        }
        if (Canvas.isPointerClick) {
            this.trans = false;
            if (Canvas.isPointer(this.menuX, this.menuY, this.menuW, this.menuH)) {
                Canvas.isPointerClick = false;
                int b2 = this.menuTemY + 2;
                int a2 = ((this.ctoyBag + Canvas.py) - b2) / Screen.ITEM_HEIGHT;
                if (a2 < 0) {
                    a2 = 0;
                }
                if (a2 > this.menuItems.size() - 1) {
                    a2 = this.menuItems.size() - 1;
                }
                if (Math.abs(Canvas.pyLast - Canvas.py) <= 10 && this.cyBag == this.ctoyBag) {
                    this.selected = a2;
                    if (this.selected != -1 && this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        }
        super.updateKey();
    }

    public void paintMenu(Graphics g) {
        Canvas.resetTrans(g);
        if (this.cyBag != this.ctoyBag) {
            this.cvyb = (this.ctoyBag - this.cyBag) << 2;
            this.cdyb += this.cvyb;
            this.cyBag += this.cdyb >> 4;
            this.cdyb &= 15;
            if (this.cyBag < 0) {
                this.cyBag = 0;
            }
        }
        g.setColor(11908533);
        g.fillRect(this.menuX - 2, this.menuY - 2, this.menuW + 4, this.menuH + 5);
        g.setColor(2181450);
        g.fillRect(this.menuX, this.menuTemY, this.menuW, this.menuH);
        g.setClip(this.menuX - 2, this.menuY, this.menuW + 5, this.menuH + 3);
        g.translate(this.menuX + 5, this.menuTemY + 2);
        for (int i = 0; i < this.menuItems.size(); i++) {
            Font font = Font.arialFontW;
            if (this.selected != -1 && i == this.selected) {
                g.setColor(11495168);
                g.fillRect(-3, (i * Screen.ITEM_HEIGHT) - this.cyBag, this.menuW - 4, Screen.ITEM_HEIGHT);
                font = Font.normalFont[0];
            }
            if (Canvas.isBB) {
                Font.borderFont.drawString(g, ((Command) this.menuItems.elementAt(i)).caption, Canvas.hw, (3 + (i * Screen.ITEM_HEIGHT)) - this.cyBag, 2);
            } else {
                font.drawString(g, ((Command) this.menuItems.elementAt(i)).caption, 0, (3 + (i * Screen.ITEM_HEIGHT)) - this.cyBag, 0);
            }
        }
        super.paint(g);
    }

    public void updateMenu() {
        if (this.menuTemY > this.menuY) {
            int delta = (this.menuTemY - this.menuY) >> 1;
            if (delta < 1) {
                delta = 1;
            }
            this.menuTemY -= delta;
        }
        this.menuTemY = this.menuY;
    }
}
