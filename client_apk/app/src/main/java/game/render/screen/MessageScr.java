package game.render.screen;

import game.core.j2me.Graphics;
import game.model.ChatTab;
import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class MessageScr extends Screen {
    public static int chatDelay;
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static int lastSe;
    public static int limit;
    public static MessageScr me;
    public static byte nMSG;
    public static int selected;
    private ChatTab focusT;
    private int focusTab;
    private int h;
    protected String[] info;
    private Screen lastScr;
    private Vector list;
    private int pa;
    private ChatTab publicTab;
    private TField tfInput;
    private boolean trans;
    private int w;
    private int x;
    private int y;
    private int wTab = 60;
    private int hTab = 20;
    private int du = 10;
    int a = 0;

    public static MessageScr gI() {
        MessageScr messageScr = me;
        if (messageScr != null) {
            return messageScr;
        }
        MessageScr messageScr2 = new MessageScr();
        me = messageScr2;
        return messageScr2;
    }

    @Override
    public void switchToMe() {
        this.lastScr = GCanvas.currentScreen;
        changTab();
        nMSG = (byte) 0;
        chatDelay = ((int) System.currentTimeMillis()) / 100;
        super.switchToMe();
        init();
    }

    public MessageScr() {
        this.focusTab = 1;
        this.left = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                Vector vector = new Vector();
                if (MessageScr.this.focusTab != 0) {
                    vector.addElement(new Command("Đóng tab", new IAction() {
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
                vector.addElement(new Command("Đóng", new IAction() {
                    @Override
                    public void perform() {
                        MessageScr.this.lastScr.switchToMe();
                    }
                }));
                GCanvas.menu.startAt(vector, 0);
            }
        });
        this.tfInput = new TField();
        init();
        this.tfInput.isFocus = true;
        this.list = new Vector();
        ChatTab chatTab = new ChatTab("TIN ĐẾN", null, null, false);
        this.publicTab = chatTab;
        this.focusTab = 0;
        chatTab.text = new Vector();
        this.list.addElement(this.publicTab);
        changTab();
        setWTab();
    }

    @Override
    public void init() {
        this.w = GCanvas.w - 48;
        this.h = GCanvas.h - 64;
        this.y = 24;
        this.x = 24;
        this.tfInput.x = 24 + 4;
        this.tfInput.y = (this.y + this.h) - 25;
        this.tfInput.height = 20;
        this.tfInput.width = this.w - 8;
        setWTab();
    }

    public void setWTab() {
        Vector vector = this.list;
        if (vector == null || this.focusT == null) {
            return;
        }
        this.du = 20;
        int size = ((this.w - (20 * vector.size())) + this.du) - 1;
        this.wTab = size;
        if (size < Font.normalFont[0].getWidth(this.focusT.name) + 15) {
            int width = Font.normalFont[0].getWidth(this.focusT.name) + 15;
            this.wTab = width;
            this.du = (this.w - width) / (this.list.size() - 1);
        }
        if (this.list.size() == 1) {
            this.wTab = this.w - 1;
        }
    }

    public void setFocusTab(String str) {
        for (int i = 0; i < this.list.size(); i++) {
            ChatTab chatTab = (ChatTab) this.list.elementAt(i);
            if (chatTab.name.equals(str)) {
                chatTab.isOpen = false;
                this.focusT = chatTab;
                this.focusTab = i;
            }
        }
    }

    public void addTab(String str, String str2, int i) {
        ChatTab chatTab;
        if (str2 != null && !str2.equals(this.publicTab.name)) {
            chatTab = getTab(str2);
            if (this != GCanvas.currentScreen) {
                nMSG = (byte) 1;
            }
        } else {
            chatTab = this.publicTab;
        }
        if (chatTab == null) {
            chatTab = new ChatTab(str2, new Command("Chat", new IAction() {
                @Override
                public void perform() {
                    MessageScr.this.doChat();
                }
            }), this.tfInput.cmdClear, true);
            chatTab.text = new Vector();
            this.list.addElement(chatTab);
            setWTab();
        }
        if (!str.equals("")) {
            for (String str3 : Font.arialFont11.splitFontBStrInLine(str, this.w - 20)) {
                chatTab.text.addElement(str3);
                if (chatTab.text.size() > 50) {
                    chatTab.text.removeElementAt(0);
                }
            }
        }
        ChatTab chatTab2 = this.focusT;
        if (chatTab == chatTab2) {
            if (selected + 1 == chatTab2.text.size() - lastSe) {
                int size = this.focusT.text.size() - lastSe;
                selected = size;
                cmtoY = (size * 14) - (limit / 2);
            }
            int size2 = ((this.focusT.text.size() * 14) - limit) + 50;
            cmyLim = size2;
            if (size2 < 0) {
                cmyLim = 0;
            }
        }
    }

    protected void doChat() {
        if (this.tfInput.getText().equals("")) {
            return;
        }
        chatDelay = (int) (System.currentTimeMillis() / 100);
        if (this.focusT.name.equals("Đội")) {
            GameService.gI().chatToClan(this.tfInput.getText());
        } else {
            addTab(GCanvas.gameScr.mainChar.name + ": " + this.tfInput.getText(), this.focusT.name, 0);
            GameService.gI().sendMsgPrivate(this.focusT.name, this.tfInput.getText());
        }
        this.tfInput.setText("");
    }

    private ChatTab getTab(String str) {
        int size = this.list.size();
        for (int i = 0; i < size; i++) {
            ChatTab chatTab = (ChatTab) this.list.elementAt(i);
            if (chatTab.name.equals(str)) {
                return chatTab;
            }
        }
        return null;
    }

    public void changTab() {
        ChatTab chatTab = (ChatTab) this.list.elementAt(this.focusTab);
        this.focusT = chatTab;
        chatTab.isOpen = false;
        limit = this.h - this.hTab;
        if (this.focusT.isInput) {
            limit -= 21;
        }
        int size = this.focusT.text.size() * 14;
        int i = limit;
        int i2 = (size - i) + 50;
        cmyLim = i2;
        if (i2 < 0) {
            cmyLim = 0;
        }
        cmtoY = 0;
        cmy = 0;
        int i3 = ((i - 10) / 2) / 14;
        lastSe = i3;
        selected = i3;
        this.center = this.focusT.center;
        this.right = this.focusT.right;
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
            this.lastScr.paint(graphics);
            graphics.setColor(0);
            graphics.setAlpha(180);
            graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
            paintTab(graphics, this.x, this.y, this.w, this.h);
            paintText(graphics);
            GCanvas.resetTrans(graphics);
            super.paint(graphics);
        } catch (Exception unused) {
        }
    }

    private void paintText(Graphics graphics) {
        try {
            Font font = Font.normalFont[0];
            if (GCanvas.screenlevel == 1) {
                font = Font.bigFont1;
            }
            font.drawString(graphics, this.focusT.name.toUpperCase(), this.x + (this.wTab / 2) + (this.focusTab * this.du), ((this.y + (this.hTab / 2)) - 5) + (GCanvas.screenlevel == 1 ? 4 : 0), 2);
            if (this.focusT.isInput && !GCanvas.menu.showMenu) {
                this.tfInput.paint(graphics);
            }
            graphics.setClip(this.x, this.y + this.hTab + 5, this.w - 3, limit - 9);
            graphics.ClipRec(this.x, this.y + this.hTab + 5, this.w - 3, limit - 9);
            graphics.translate(0, -cmy);
            if (this.focusT.text.size() > 0) {
                int i = cmy / 14;
                int size = (limit / 14) + i + 1;
                if (size >= this.focusT.text.size()) {
                    size = this.focusT.text.size();
                }
                while (i < size) {
                    if (i < 0) {
                        i = 0;
                    }
                    Font.arialFont11.drawString(graphics, (String) this.focusT.text.elementAt(i), this.x + 8, this.y + (i * 14) + this.hTab + 3, 0);
                    i++;
                }
            }
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
        } catch (Exception unused) {
        }
    }

    private void paintTab(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        graphics.setColor(-16500172);
        int i6 = this.hTab;
        graphics.fillRect(i, i2 + i6, i3, i4 - i6);
        int i7 = 0;
        while (true) {
            i5 = 3;
            if (i7 >= 3) {
                break;
            }
            graphics.setColor(Res.color[i7]);
            int i8 = this.hTab;
            int i9 = i7 * 2;
            graphics.drawRect(i + i7, i2 + i7 + i8, (i3 - i9) - 1, ((i4 - i9) - i8) - 1);
            i7++;
        }
        int size = this.list.size() - 1;
        while (size > this.focusTab) {
            ChatTab chatTab = (ChatTab) this.list.elementAt(size);
            int i10 = 0;
            while (i10 < i5) {
                graphics.setColor(Res.color[i10]);
                int i11 = i10 * 2;
                graphics.drawRect(i + i10 + (this.du * size), i2 + i10, this.wTab - i11, this.hTab - i11);
                i10++;
                i5 = 3;
            }
            chatTab.count++;
            if (chatTab.count > 10) {
                chatTab.count = 0;
            }
            if (chatTab.isOpen && chatTab.count >= 5) {
                graphics.setColor(-16204118);
                graphics.fillRect(i + 3 + (this.du * size), i2 + 3, this.wTab - 5, this.hTab - 3);
            } else {
                int i12 = this.du;
                graphics.setClip((((i12 * size) + i) + this.wTab) - i12, i2, i12 - 2, this.hTab);
                int i13 = this.du;
                graphics.ClipRec((((i13 * size) + i) + this.wTab) - i13, i2, i13 - 2, this.hTab);
                graphics.drawImage(Res.imgBgPopup, ((((this.du * size) + i) + 3) + this.wTab) - 70, i2 + 3, 0);
                graphics.restoreCanvas();
                graphics.setClip(i, i2, i3, i4);
            }
            size--;
            i5 = 3;
        }
        int i14 = 0;
        while (i14 <= this.focusTab) {
            int i15 = 0;
            for (int i16 = 3; i15 < i16; i16 = 3) {
                graphics.setColor(Res.color[i15]);
                int i17 = i15 * 2;
                graphics.drawRect(i + i15 + (this.du * i14), i2 + i15, this.wTab - i17, this.hTab - i17);
                i15++;
            }
            ChatTab chatTab2 = (ChatTab) this.list.elementAt(i14);
            chatTab2.count++;
            if (chatTab2.count > 10) {
                chatTab2.count = 0;
            }
            if (chatTab2.isOpen && chatTab2.count >= 5) {
                graphics.setColor(-16204118);
                graphics.fillRect(i + 3 + (this.du * i14), i2 + 3, this.wTab - 5, (this.hTab - 3) + (i14 == this.focusTab ? 4 : 0));
            } else {
                if (i14 == this.focusTab) {
                    graphics.setClip((this.du * i14) + i + 3, i2, this.wTab - 5, this.hTab + 3);
                    graphics.ClipRec((this.du * i14) + i + 3, i2, this.wTab - 5, this.hTab + 3);
                    for (int i18 = 0; i18 < (this.wTab / 70) + 1; i18++) {
                        graphics.drawImage(Res.imgBgPopup, i + 3 + (this.du * i14) + (i18 * 70), i2 + 3, 0);
                    }
                    graphics.restoreCanvas();
                } else {
                    int i19 = this.du;
                    graphics.setClip((i19 * i14) + i, i2, i19, this.hTab);
                    int i20 = this.du;
                    graphics.ClipRec((i20 * i14) + i, i2, i20, this.hTab);
                    graphics.drawImage(Res.imgBgPopup, (this.du * i14) + i + 3, i2 + 3, 0);
                    graphics.restoreCanvas();
                }
                graphics.setClip(i - 4, i2 - 4, i3 + 8, i4 + 9);
                i14++;
            }
            graphics.setClip(i - 4, i2 - 4, i3 + 8, i4 + 9);
            i14++;
        }
        int i21 = i - 2;
        int i22 = i2 - 2;
        graphics.drawImage(Res.imgPopupGoc, i21 + (this.du * this.focusTab), i22, 0);
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 2, i + 3 + this.wTab + (this.du * this.focusTab), i22, Graphics.TOP | Graphics.RIGHT);
        int i23 = 0;
        while (i23 < 2) {
            int i24 = 0;
            while (i24 < 2) {
                graphics.setColor(Res.color[i23 == 0 ? i24 + 1 : 2 - i24]);
                int i25 = i24 + 1;
                int i26 = (this.focusTab * this.du) + i + i25 + ((this.wTab - 3) * i23);
                int i27 = (this.hTab + i2) - 2;
                if (i23 != 0) {
                    i24 = 1 - i24;
                }
                graphics.fillRect(i26, i27 + i24, 1, 3);
                i24 = i25;
            }
            i23++;
        }
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        int i28 = i2 + i4;
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 6, i21, i28 + 3, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 3, i + i3 + 2, i28 + 2, Graphics.BOTTOM | Graphics.RIGHT);
    }

    @Override
    public boolean keyPress(int i) {
        if (this.focusT.isInput && this.tfInput.isFocus && !GCanvas.menu.showMenu) {
            this.tfInput.keyPressed(i);
        }
        return super.keyPress(i);
    }

    @Override
    public void update() {
        this.lastScr.update();
        int i = cmy;
        int i2 = cmtoY;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            cmvy = i3;
            int i4 = cmdy + i3;
            cmy = i + (i4 >> 4);
            cmdy = i4 & 15;
        }
        if (Math.abs(i2 - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10) {
            int i5 = cmy;
            int i6 = cmyLim;
            if (i5 > i6) {
                cmtoY = i6;
            }
        }
    }

    @Override
    public void updateKey() {
        int i;
        if (this.focusT.isInput && !GCanvas.menu.showMenu) {
            this.tfInput.update();
        }
        if (GCanvas.isPointerDown) {
            if (GCanvas.pyLast - GCanvas.py > 20) {
                GCanvas.keyHold[8] = true;
            } else if (GCanvas.py - GCanvas.pyLast > 20) {
                GCanvas.keyHold[2] = true;
            }
        }
        if (GCanvas.isPointerClick) {
            pointer(GCanvas.px, GCanvas.py);
        }
        if (GCanvas.isKeyPressed(4)) {
            int i2 = this.focusTab - 1;
            this.focusTab = i2;
            if (i2 < 0) {
                this.focusTab = this.list.size() - 1;
            }
            changTab();
        } else if (GCanvas.isKeyPressed(6)) {
            int i3 = this.focusTab + 1;
            this.focusTab = i3;
            if (i3 >= this.list.size()) {
                this.focusTab = 0;
            }
            changTab();
        }
        if (GCanvas.keyHold[2]) {
            GCanvas.keyHold[2] = false;
            int i4 = selected - 3;
            selected = i4;
            int i5 = lastSe;
            if (i4 < i5) {
                selected = i5;
            }
        } else {
            if (GCanvas.keyHold[8]) {
                GCanvas.keyHold[8] = false;
                if (cmy < cmyLim) {
                    selected += 3;
                }
                if (selected > this.focusT.text.size() - lastSe) {
                    selected = this.focusT.text.size() - lastSe;
                }
            }
            if (GCanvas.isPointerDown && GCanvas.isPointer(this.x, this.y, this.w, this.h)) {
                if (!this.trans) {
                    this.pa = cmy;
                    this.trans = true;
                }
                i = this.pa + (GCanvas.pyLast - GCanvas.py);
                cmtoY = i;
                if (i < (-GCanvas.pyLast)) {
                    cmtoY = -GCanvas.pyLast;
                }
                if (cmtoY > cmyLim + (GCanvas.pyLast / 2)) {
                    cmtoY = cmyLim + (GCanvas.pyLast / 2);
                }
            }
            if (GCanvas.isPointerClick) {
                this.trans = false;
            }
            super.updateKey();
        }
        int i6 = (selected * 14) - (limit / 2);
        cmtoY = i6;
        if (i6 < 0) {
            cmtoY = 0;
        }
        int i7 = cmtoY;
        int i8 = cmyLim;
        if (i7 > i8) {
            cmtoY = i8;
        }
        if (GCanvas.isPointerDown) {
            if (!this.trans) {
                this.pa = cmy;
                this.trans = true;
            }
            i = this.pa + (GCanvas.pyLast - GCanvas.py);
            cmtoY = i;
            if (i < (-GCanvas.pyLast)) {
                cmtoY = -GCanvas.pyLast;
            }
            if (cmtoY > cmyLim + (GCanvas.pyLast / 2)) {
                cmtoY = cmyLim + (GCanvas.pyLast / 2);
            }
        }
        if (GCanvas.isPointerClick) {
            this.trans = false;
        }
        super.updateKey();
    }

    private void pointer(int i, int i2) {
        if (Math.abs(GCanvas.pyLast - GCanvas.py) <= 10) {
            if (xacDinhToaDo(i, i2, this.x + 10, this.y + 10, 100, 50)) {
                GCanvas.keyPressed[4] = true;
            } else if (xacDinhToaDo(i, i2, this.w - 20, this.y + 10, 100, 50)) {
                GCanvas.keyPressed[6] = true;
            }
        }
    }
}
