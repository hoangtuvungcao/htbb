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
            // pokemon: ngắt dòng theo lòng bảng (bảng có viền kép ~5 px mỗi bên; w có thể chưa khởi tạo)
            int rongChu = (this.w > 0 ? this.w : Canvas.w - 48) - 22;
            String[] tex = Font.arialFontW.splitFontBStrInLine(te, rongChu);
            for (String str0 : tex) {
                // dòng vẫn dài hơn lòng bảng (ngắt dòng gốc trượt): ngắt tiếp theo dấu cách
                String str = str0;
                while (Font.arialFontW.getWidthNguyenVan(str) > rongChu && str.indexOf(' ') > 0) {
                    int cat = str.length();
                    do {
                        cat = str.lastIndexOf(' ', cat - 1);
                    } while (cat > 0 && Font.arialFontW.getWidthNguyenVan(str.substring(0, cat)) > rongChu);
                    if (cat <= 0) {
                        break;
                    }
                    tab.text.addElement(str.substring(0, cat));
                    str = str.substring(cat + 1);
                }
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
        String t = game.networklogic.GameService.chuChat(this.tfInput.getText());
        if (t == null) {
            return;   // 0.9.5: rỗng / chỉ khoảng trắng: không gửi
        }
        this.tfInput.setText(t);
        chatDelay = (int) (System.currentTimeMillis() / 100);
        if (this.focusT.name.equals("Đội")) {
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
        g.setClip(this.x + 5, this.y + this.hTab + 5, this.w - 10, limit - 9);
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
        // pokemon: thân bảng xanh viền trắng; thẻ (tab) bo góc - đang xem: đỏ, khác: xanh đậm, có tin mới: vàng nhấp nháy
        game.pokemon.GiaoDien.bang(g, x, y + this.hTab, w, h - this.hTab);
        for (int a = this.list.size() - 1; a >= 0; a--) {
            if (a == this.focusTab) {
                continue;
            }
            paintMotTab(g, x + (a * this.du), y, (ChatTab) this.list.elementAt(a), false);
        }
        if (this.focusTab >= 0 && this.focusTab < this.list.size()) {
            paintMotTab(g, x + (this.focusTab * this.du), y, (ChatTab) this.list.elementAt(this.focusTab), true);
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
    }

    private void paintMotTab(Graphics g, int xt, int y, ChatTab tab, boolean dangXem) {
        tab.count++;
        if (tab.count > 10) {
            tab.count = 0;
        }
        boolean nhay = tab.isOpen && tab.count >= 5;
        int tren = nhay ? game.pokemon.GiaoDien.VANG : (dangXem ? game.pokemon.GiaoDien.DO_SANG : game.pokemon.GiaoDien.THAN_TREN);
        int duoi = nhay ? game.pokemon.GiaoDien.VANG_TOI : (dangXem ? game.pokemon.GiaoDien.DO_TOI : game.pokemon.GiaoDien.THAN_DUOI);
        g.setClip(xt - 1, y - 1, this.wTab + 3, this.hTab + (dangXem ? 5 : 2));
        game.pokemon.GiaoDien.khoi(g, xt, y, this.wTab + 1, this.hTab + 8, 4, game.pokemon.GiaoDien.VIEN);
        game.pokemon.GiaoDien.khoi(g, xt + 2, y + 2, this.wTab - 3, this.hTab + 6, 3, game.pokemon.GiaoDien.TRANG);
        game.pokemon.GiaoDien.khoiDoc(g, xt + 3, y + 3, this.wTab - 5, this.hTab + 4, 2, tren, duoi);
        g.setClip(0, 0, Canvas.w, Canvas.h);
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
