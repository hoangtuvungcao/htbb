package game.render.screen;

import game.model.ChatTab;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class MessageScr extends Screen {
    public static MessageScr me;
    protected String[] info;
    private Vector list;
    private int focusTab;
    private Screen lastScr;
    private int w;
    private int h;
    private int x;
    private int y;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int lastSe;
    public static int limit;
    public static int chatDelay;
    private ChatTab focusT;
    private ChatTab publicTab;
    private TField tfInput;
    public static int selected = 0;
    public static byte nMSG = 0;
    private int wTab = 60;
    private int hTab = 20;
    private int du = 10;
    int a = 0;

    public static MessageScr gI() {
        if (me != null) {
            return me;
        }
        MessageScr messageScr = new MessageScr();
        me = messageScr;
        return messageScr;
    }

    @Override
    public void switchToMe() {
        this.lastScr = Canvas.currentScreen;
        changTab();
        nMSG = (byte) 0;
        chatDelay = ((int) System.currentTimeMillis()) / 100;
        super.switchToMe();
        init();
    }

    class AnonymousClass1 implements IAction {
        AnonymousClass1() {
        }

        @Override
        public void perform() {
            Vector menu = new Vector();
            if (MessageScr.this.focusTab != 0) {
                menu.addElement(new Command("Đóng tab", new IAction() {
                    @Override
                    public void perform() {
                        MessageScr.this.list.removeElementAt(MessageScr.this.focusTab);
                        if (MessageScr.this.focusTab >= MessageScr.this.list.size()) {
                            MessageScr.this.focusTab = MessageScr.this.list.size() - 1;
                        }
                        MessageScr.this.setWTab();
                        MessageScr.this.changTab();
                    }
                }));
            }
            menu.addElement(new Command("Đóng", new IAction() {
                @Override
                public void perform() {
                    MessageScr.this.lastScr.switchToMe();
                }
            }));
            Canvas.menu.startAt(menu, 0);
        }
    }

    public MessageScr() {
        this.focusTab = 1;
        this.left = new Command("Menu", new AnonymousClass1());
        this.tfInput = new TField();
        init();
        this.tfInput.isFocus = true;
        this.list = new Vector();
        this.publicTab = new ChatTab("Tin đến", null, null, false);
        this.focusTab = 0;
        this.publicTab.text = new Vector();
        this.list.addElement(this.publicTab);
        changTab();
        setWTab();
    }

    @Override
    public void init() {
        this.w = Canvas.w - 48;
        this.h = Canvas.h - 64;
        this.y = 24;
        this.x = 24;
        this.tfInput.x = this.x + 4;
        this.tfInput.y = (this.y + this.h) - 25;
        this.tfInput.height = 20;
        this.tfInput.width = this.w - 8;
        setWTab();
    }

    public void setWTab() {
        if (this.list != null && this.focusT != null) {
            this.du = 20;
            this.wTab = ((this.w - (this.du * this.list.size())) + this.du) - 1;
            if (this.wTab < Font.borderFont.getWidth(this.focusT.name) + 15) {
                this.wTab = Font.borderFont.getWidth(this.focusT.name) + 15;
                this.du = (this.w - this.wTab) / (this.list.size() - 1);
            }
            if (this.list.size() == 1) {
                this.wTab = this.w - 1;
            }
        }
    }

    public void setFocusTab(String name) {
        for (int i = 0; i < this.list.size(); i++) {
            ChatTab tab = (ChatTab) this.list.elementAt(i);
            if (tab.name.equals(name)) {
                tab.isOpen = false;
                this.focusT = tab;
                this.focusTab = i;
            }
        }
    }

    public void addTab(String te, String name, int type) {
        ChatTab tab;
        if (name != null && !name.equals(this.publicTab.name)) {
            tab = getTab(name);
            if (this != Canvas.currentScreen) {
                nMSG = (byte) 1;
            }
        } else {
            tab = this.publicTab;
        }
        if (tab == null) {
            tab = new ChatTab(name, new Command("Chat", new IAction() {
                @Override
                public void perform() {
                    MessageScr.this.doChat();
                }
            }), this.tfInput.cmdClear, true);
            tab.text = new Vector();
            this.list.addElement(tab);
            setWTab();
        }
        if (!te.equals("")) {
            String[] tex = Font.arialFontW.splitFontBStrInLine(te, this.w - 20);
            for (String str : tex) {
                tab.text.addElement(str);
                if (tab.text.size() > 50) {
                    tab.text.removeElementAt(0);
                }
            }
        }
        if (tab == this.focusT) {
            if (selected + 1 == this.focusT.text.size() - lastSe) {
                selected = this.focusT.text.size() - lastSe;
                cmtoY = (selected * 14) - (limit / 2);
            }
            cmyLim = ((this.focusT.text.size() * 14) - limit) + 50;
            if (cmyLim < 0) {
                cmyLim = 0;
            }
        }
    }

    protected void doChat() {
        if (this.tfInput.getText().equals("")) {
            return;
        }
        chatDelay = (int) (System.currentTimeMillis() / 100);
        if (this.focusT.name.equals("Bang hội")) {
            GameService.gI().chatToClan(this.tfInput.getText());
        } else {
            addTab(new StringBuffer(String.valueOf(Canvas.gameScr.mainChar.name)).append(": ").append(this.tfInput.getText()).toString(), this.focusT.name, 0);
            GameService.gI().sendMsgPrivate(this.focusT.name, this.tfInput.getText());
        }
        this.tfInput.setText("");
    }

    private ChatTab getTab(String name) {
        for (int i = 0; i < this.list.size(); i++) {
            ChatTab tab = (ChatTab) this.list.elementAt(i);
            if (tab.name.equals(name)) {
                return tab;
            }
        }
        return null;
    }

    public void changTab() {
        this.focusT = (ChatTab) this.list.elementAt(this.focusTab);
        this.focusT.isOpen = false;
        limit = this.h - this.hTab;
        if (this.focusT.isInput) {
            limit -= 21;
        }
        cmyLim = ((this.focusT.text.size() * 14) - limit) + 50;
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        lastSe = ((limit - 10) / 2) / 14;
        selected = lastSe;
        this.center = this.focusT.center;
        this.right = this.focusT.right;
    }

    @Override
    public void paint(Graphics g) {
        g.translate(-g.getTranslateX(), -g.getTranslateY());
        this.lastScr.paint(g);
        Canvas.resetTrans(g);
        super.paint(g);
        paintTab(g, this.x, this.y, this.w, this.h);
        paintText(g);
    }

    private void paintText(Graphics g) {
        Font.borderFont.drawString(g, this.focusT.name, this.x + (this.wTab / 2) + (this.focusTab * this.du), (this.y + (this.hTab / 2)) - 5, 2);
        if (this.focusT.isInput && !Canvas.menu.showMenu) {
            this.tfInput.paint(g);
        }
        g.setClip(this.x, this.y + this.hTab + 5, this.w - 3, limit - 9);
        g.translate(0, -cmy);
        if (this.focusT.text.size() > 0) {
            int aa = cmy / 14;
            int bb = aa + (limit / 14) + 1;
            if (bb >= this.focusT.text.size()) {
                bb = this.focusT.text.size();
            }
            for (int i = aa; i < bb; i++) {
                String str = (String) this.focusT.text.elementAt(i);
                Font.arialFontW.drawString(g, str, this.x + 8, this.y + (i * 14) + this.hTab + 3, 0);
            }
        }
    }

    private void paintTab(Graphics g, int x, int y, int w, int h) {
        g.setColor(277044);
        g.fillRect(x, y + this.hTab, w, h - this.hTab);
        for (int i = 0; i < 3; i++) {
            g.setColor(Res.color[i]);
            g.drawRect(x + i, y + i + this.hTab, (w - (i * 2)) - 1, ((h - (i * 2)) - this.hTab) - 1);
        }
        for (int a = this.list.size() - 1; a > this.focusTab; a--) {
            ChatTab tab = (ChatTab) this.list.elementAt(a);
            for (int i2 = 0; i2 < 3; i2++) {
                g.setColor(Res.color[i2]);
                g.drawRect(x + i2 + (a * this.du), y + i2, this.wTab - (i2 * 2), this.hTab - (i2 * 2));
            }
            tab.count++;
            if (tab.count > 10) {
                tab.count = 0;
            }
            if (tab.isOpen && tab.count >= 5) {
                g.setColor(573098);
                g.fillRect(x + 3 + (this.du * a), y + 3, this.wTab - 5, this.hTab - 3);
            } else {
                g.setClip(((x + (this.du * a)) + this.wTab) - this.du, y, this.du - 2, this.hTab);
                g.drawImage(Res.imgInv[2], (((x + (this.du * a)) + 3) + this.wTab) - 70, y + 3, 0);
                g.setClip(x, y, w, h);
            }
        }
        int a2 = 0;
        while (a2 <= this.focusTab) {
            for (int i3 = 0; i3 < 3; i3++) {
                g.setColor(Res.color[i3]);
                g.drawRect(x + i3 + (a2 * this.du), y + i3, this.wTab - (i3 * 2), this.hTab - (i3 * 2));
            }
            ChatTab tab2 = (ChatTab) this.list.elementAt(a2);
            tab2.count++;
            if (tab2.count > 10) {
                tab2.count = 0;
            }
            if (tab2.isOpen && tab2.count >= 5) {
                g.setColor(573098);
                g.fillRect(x + 3 + (this.du * a2), y + 3, this.wTab - 5, (this.hTab - 3) + (a2 == this.focusTab ? 4 : 0));
            } else if (a2 == this.focusTab) {
                g.setClip(x + (this.du * a2) + 3, y, this.wTab - 5, this.hTab + 3);
                for (int b = 0; b < (this.wTab / 70) + 1; b++) {
                    g.drawImage(Res.imgInv[2], x + 3 + (this.du * a2) + (70 * b), y + 3, 0);
                }
            } else {
                g.setClip(x + (this.du * a2), y, this.du, this.hTab);
                g.drawImage(Res.imgInv[2], x + (this.du * a2) + 3, y + 3, 0);
            }
            g.setClip(x - 4, y - 4, w + 8, h + 9);
            a2++;
        }
        g.drawImage(Res.imgInv[0], (x - 2) + (this.du * this.focusTab), y - 2, 0);
        g.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 2, x + 3 + this.wTab + (this.du * this.focusTab), y - 2, 24);
        int a3 = 0;
        while (a3 < 2) {
            for (int i4 = 0; i4 < 2; i4++) {
                g.setColor(Res.color[a3 == 0 ? 1 + i4 : 2 - i4]);
                g.fillRect(x + (this.focusTab * this.du) + i4 + 1 + ((this.wTab - 3) * a3), ((y + this.hTab) - 8) + 6 + (a3 == 0 ? i4 : 1 - i4), 1, 3);
            }
            a3++;
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        g.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 6, x - 2, y + h + 2, 36);
        g.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 3, x + w + 2, y + h + 2, 40);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (this.focusT.isInput && this.tfInput.isFocus && !Canvas.menu.showMenu) {
            this.tfInput.keyPressed(keyCode);
        }
        return super.keyPress(keyCode);
    }

    @Override
    public void update() {
        this.lastScr.update();
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
    }

    @Override
    public void updateKey() {
        if (this.focusT.isInput && !Canvas.menu.showMenu) {
            this.tfInput.update();
        }
        boolean changeFocus = false;
        if (Canvas.isKeyPressed(4)) {
            this.focusTab--;
            if (this.focusTab < 0) {
                this.focusTab = this.list.size() - 1;
            }
            changTab();
        } else if (Canvas.isKeyPressed(6)) {
            this.focusTab++;
            if (this.focusTab >= this.list.size()) {
                this.focusTab = 0;
            }
            changTab();
        }
        if (Canvas.keyHold[2]) {
            selected--;
            if (selected < lastSe) {
                selected = lastSe;
            }
            changeFocus = true;
        } else if (Canvas.keyHold[8]) {
            if (cmy < cmyLim) {
                selected++;
            }
            if (selected > this.focusT.text.size() - lastSe) {
                selected = this.focusT.text.size() - lastSe;
            }
            changeFocus = true;
        }
        if (changeFocus) {
            cmtoY = (selected * 14) - (limit / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        super.updateKey();
    }
}
