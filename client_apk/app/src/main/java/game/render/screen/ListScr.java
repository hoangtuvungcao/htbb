package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.ChatInfo;
import game.model.ClanInfo;
import game.model.Command;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.ViewTextDlg;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;

public class ListScr extends Screen {
    public static final byte CLAN = 1;
    public static final byte FRIEND = 0;
    public static final byte MANAGER_LIST = 6;
    public static final byte MSG_CLAN = 2;
    public static final byte RIGHER = 3;
    public static final byte STRONGER = 4;
    public static final byte TOP = 7;
    public static final byte TOP_CLAN = 5;
    public static final byte TOP_CUC_BO = 8;
    public static int cmdy;
    public static int cmtoY;
    public static int cmvy;
    public static int cmy;
    public static int cmyLim;
    public static ListScr me;
    public static int selected;
    public Vector freindList;
    private int h;
    private int height;
    private Screen lastScr;
    public Vector list;
    private String[] nameManager;
    private String title;
    private int type;
    private int w;
    public byte page = 0;
    int pa = 0;
    boolean trans = false;
    int x0 = 0;
    int dir0 = 1;
    int a0 = 20;
    int x1 = 0;
    int dir1 = 1;
    int x2 = 0;
    int dir2 = 1;

    public static ListScr gI() {
        ListScr listScr = me;
        if (listScr != null) {
            return listScr;
        }
        ListScr listScr2 = new ListScr();
        me = listScr2;
        return listScr2;
    }

    @Override
    public void switchToMe() {
        if (GCanvas.currentScreen == this) {
            return;
        }
        selected = 0;
        this.page = (byte) 0;
        cmtoY = 0;
        cmy = 0;
        if (this.lastScr == null) {
            this.lastScr = GCanvas.gameScr;
        }
        super.switchToMe();
        init();
    }

    @Override
    public void init() {
        this.w = GCanvas.w - 41;
        this.h = GCanvas.h - 56;
        int size = (this.list.size() * this.height) - (this.h - 32);
        cmyLim = size;
        if (size < 0) {
            cmyLim = 0;
        }
    }

    public ListScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                ListScr.this.lastScr.switchToMe();
                ListScr.this.list.removeAllElements();
                ListScr.this.lastScr = null;
            }
        });
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                if (ListScr.selected < ListScr.this.list.size()) {
                    ListScr listScr = ListScr.this;
                    listScr.doSelected(listScr.list.elementAt(ListScr.selected));
                }
            }
        });
        this.left = new Command("Menu", new IAction() {
            @Override
            public void perform() {
                ListScr.this.doMenu();
            }
        });
    }

    protected void doMenu() {
        Vector vector = new Vector();
        int i = this.type;
        if (i == 4 || i == 3 || i == 5 || i == 1) {
            if (i == 5) {
                vector.addElement(new Command("Danh sách quản lý đội.", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestQuanLyBang(((ClanInfo) ListScr.this.list.elementAt(ListScr.selected)).id);
                    }
                }));
            }
            vector.addElement(new Command("Xem thêm", new IAction() {
                @Override
                public void perform() {
                    GCanvas.startWaitDlg();
                    if (ListScr.this.type == 1) {
                        GameService gameServiceGI = GameService.gI();
                        short s = ((Char) ListScr.this.list.elementAt(ListScr.selected)).idClan;
                        ListScr listScr = ListScr.this;
                        byte b = (byte) (listScr.page + 1);
                        listScr.page = b;
                        gameServiceGI.requestClanList(s, b);
                        return;
                    }
                    GameService gameServiceGI2 = GameService.gI();
                    int i2 = ListScr.this.type;
                    ListScr listScr2 = ListScr.this;
                    byte b2 = (byte) (listScr2.page + 1);
                    listScr2.page = b2;
                    gameServiceGI2.requestTopStronger_Righer(i2, b2);
                }
            }));
        }
        if (this.type == 0) {
            vector.addElement(new Command("Xóa bạn", new IAction() {
                @Override
                public void perform() {
                    if (ListScr.this.freindList.size() == 0) {
                        GCanvas.startOKDlg("Danh sách trống");
                    } else {
                        GCanvas.startYesNoDlg("Bạn có muốn xóa người này không ?", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().removeFriend(((Char) ListScr.this.list.elementAt(ListScr.selected)).name);
                                ListScr.this.list.removeElementAt(ListScr.selected);
                                ListScr.this.freindList.removeElementAt(ListScr.selected);
                                ListScr.selected--;
                                ListScr.cmyLim = (ListScr.this.list.size() * ListScr.this.height) - (ListScr.this.h - 32);
                                if (ListScr.cmyLim < 0) {
                                    ListScr.cmyLim = 0;
                                }
                                GCanvas.startOKDlg("Đã xóa bạn");
                                ListScr.this.setPos();
                            }
                        });
                    }
                }
            }));
        }
        int i2 = this.type;
        if (i2 == 1) {
            if (((Char) this.list.elementAt(0)).idClan == GCanvas.gameScr.mainChar.idClan) {
                vector.addElement(new Command("Nhắn tin toàn đội", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.inputDlg.setInfo("Nội dung", new IAction() {
                            @Override
                            public void perform() {
                                String text = GCanvas.inputDlg.tfInput.getText();
                                if (text.equals("")) {
                                    return;
                                }
                                GCanvas.startOKDlg("Đã gửi tin");
                                GameService.gI().msgClanAll(text, (byte) 0, 0);
                            }
                        }, 0, 100, false);
                        GCanvas.inputDlg.show();
                    }
                }));
            }
            if (((Char) this.list.elementAt(selected)).idClan == GCanvas.gameScr.mainChar.idClan && ((GCanvas.gameScr.mainChar.isMaster == 0 || GCanvas.gameScr.mainChar.isMaster == 1) && !((Char) this.list.elementAt(selected)).name.equals(GCanvas.gameScr.mainChar.name))) {
                vector.addElement(new Command("Mời khỏi đội", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.startYesNoDlg("Bạn có chắc muốn mời " + ((Char) ListScr.this.list.elementAt(ListScr.selected)).name + " khỏi đội không ?", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().requestEvictionClan(((Char) ListScr.this.list.elementAt(ListScr.selected)).name);
                                ListScr.this.list.removeElementAt(ListScr.selected);
                                ListScr.selected--;
                                GCanvas.endDlg();
                                ListScr.this.setPos();
                            }
                        });
                    }
                }));
            }
        } else if (i2 == 2 && GCanvas.gameScr.mainChar.isMaster == 0) {
            vector.addElement(new Command("Xóa tin nhắn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().msgClanAll(null, (byte) 2, ((ChatInfo) ListScr.this.list.elementAt(ListScr.selected)).ID);
                    ListScr.this.list.removeElementAt(ListScr.selected);
                    ListScr.selected--;
                    if (ListScr.selected < 0) {
                        ListScr.selected = 0;
                    }
                }
            }));
        }
        if (vector.size() > 0) {
            GCanvas.menu.startAt(vector, 0);
        }
    }

    public void doSelected(final Object obj) {
        int i = this.type;
        if (i == 2) {
            ViewTextDlg.gI().setInfo(((ChatInfo) obj).content, "TIN NHẮN");
        } else if (i == 5) {
            GameService.gI().requestClanInfo(((ClanInfo) obj).id);
        } else {
            GCanvas.inputDlg.setInfo("Nội dung tin nhắn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().sendMsgPrivate(((Char) obj).name, GCanvas.inputDlg.tfInput.getText());
                    GCanvas.endDlg();
                }
            }, 0, 100, false);
            GCanvas.inputDlg.show();
        }
    }

    public void setInfo(Vector vector, int i, String str) {
        selected = 0;
        if (vector.size() == 0) {
            this.page = (byte) -1;
        } else {
            this.list = vector;
        }
        this.type = i;
        if (i == 2) {
            this.height = 30;
        } else {
            this.height = 40;
        }
        this.title = str;
        setPos();
        Vector vector2 = this.list;
        if (vector2 != null) {
            cmyLim = (vector2.size() * this.height) - (this.h - 32);
        }
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        if (i == 6 && this.nameManager == null) {
            this.nameManager = new String[]{"Đội trưởng", "Đội phó", "Cố vấn"};
        }
    }

    public void setPos() {
        int i = this.type;
        if (i == 2 || i == 5) {
            return;
        }
        int size = this.list.size();
        for (int i2 = 0; i2 < size; i2++) {
            Char r4 = (Char) this.list.elementAt(i2);
            r4.x = (short) 25;
            int i3 = this.height;
            r4.y = (short) ((((i2 * i3) + (i3 / 2)) + (r4.height / 2)) - 5);
        }
    }

    @Override
    public void update() {
        if (Math.abs(cmtoY - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10) {
            int i = cmy;
            int i2 = cmyLim;
            if (i > i2) {
                cmtoY = i2;
            }
        }
    }

    @Override
    public void updateKey() {
        boolean z;
        boolean z2 = true;
        if (GCanvas.isPointer(20, 46, this.w, this.h - 24)) {
            if (GCanvas.isPointerDown) {
                if (!this.trans) {
                    this.pa = cmy;
                    this.trans = true;
                }
                if (GCanvas.pyLast - GCanvas.py != 0) {
                    int i = this.pa + (GCanvas.pyLast - GCanvas.py);
                    cmtoY = i;
                    if (i < -30) {
                        cmtoY = -30;
                    }
                    int i2 = cmtoY;
                    int i3 = cmyLim;
                    if (i2 > i3 + 30) {
                        cmtoY = i3 + 30;
                    }
                }
            }
            if (GCanvas.isPointerClick) {
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10) {
                    int i4 = ((cmtoY + GCanvas.py) - 46) / this.height;
                    if (i4 == selected) {
                        this.center.action.perform();
                    } else {
                        selected = i4;
                    }
                    if (selected < 0) {
                        selected = 0;
                    }
                    if (selected >= this.list.size()) {
                        selected = this.list.size() - 1;
                    }
                    this.x0 = 0;
                    z = true;
                } else {
                    z = false;
                }
            } else {
                z = false;
            }
        } else {
            z = false;
        }
        if (GCanvas.isKeyPressed(2)) {
            int i5 = selected - 1;
            selected = i5;
            if (i5 < 0) {
                selected = this.list.size() - 1;
            }
            this.x0 = 0;
        } else if (GCanvas.isKeyPressed(8)) {
            int i6 = selected + 1;
            selected = i6;
            if (i6 >= this.list.size()) {
                selected = 0;
            }
            this.x0 = 0;
        } else {
            z2 = z;
        }
        if (z2) {
            int i7 = (selected * this.height) - ((this.h - 31) / 2);
            cmtoY = i7;
            if (i7 < 0) {
                cmtoY = 0;
            }
            int i8 = cmtoY;
            int i9 = cmyLim;
            if (i8 > i9) {
                cmtoY = i9;
            }
        }
        int i10 = cmy;
        int i11 = cmtoY;
        if (i10 != i11) {
            int i12 = (i11 - i10) << 2;
            cmvy = i12;
            int i13 = cmdy + i12;
            cmy = i10 + (i13 >> 4);
            cmdy = i13 & 15;
        }
        super.updateKey();
        Screen screen = this.lastScr;
        if (screen != null) {
            screen.update();
        }
    }

    @Override
    public void paint(Graphics graphics) {
        Screen screen = this.lastScr;
        if (screen != null) {
            screen.paint(graphics);
        }
        GCanvas.resetTrans(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        Res.paintDlgFull(graphics, 20, 20, this.w, this.h);
        graphics.setClip(25, 22, this.w - 10, 30);
        graphics.ClipRec(25, 22, this.w - 10, 30);
        Font font = Font.normalFont[0];
        if (GCanvas.screenlevel == 1) {
            font = Font.bigFont1;
        }
        font.drawString(graphics, this.title, this.a0 + (this.w / 2), 30, 2);
        GCanvas.resetTrans(graphics);
        graphics.restoreCanvas();
        graphics.translate(20, 47);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(4, 0, this.w - 8, this.h - 31);
        graphics.setClip(4, 0, this.w - 8, this.h - 31);
        graphics.translate(0, -cmy);
        if (this.type != 2) {
            int i = selected;
            int i2 = this.height;
            Res.paintSelected(graphics, 4, i * i2, this.w - 9, i2);
        }
        int i3 = this.type;
        if (i3 == 5) {
            paintTopClan(graphics);
        } else if (i3 == 2) {
            paintMsgClan(graphics);
        } else {
            paintChar(graphics);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        super.paint(graphics);
    }

    private void paintTopClan(Graphics graphics) {
        int i;
        int i2 = cmy;
        int i3 = this.height;
        int i4 = i2 / i3;
        int size = (this.h / i3) + i4 + 1;
        if (size >= this.list.size()) {
            size = this.list.size();
        }
        while (i4 < size) {
            ClanInfo clanInfo = (ClanInfo) this.list.elementAt(i4);
            int i5 = 0;
            if (clanInfo.id == -1 || GameData.getImgIcon(clanInfo.id) == null) {
                i = 0;
            } else {
                i = 12;
                GameData.paintIcon(graphics, (short) (clanInfo.id + 1000), 16, (this.height * i4) + 12);
            }
            Font.normalFont[0].drawString(graphics, clanInfo.name + "        - Cấp độ: " + (clanInfo.level > -1 ? ((int) clanInfo.level) + "" : "?"), i + 11, (this.height * i4) + 4, 0);
            if (clanInfo.nation > -1) {
                graphics.drawRegion(Res.imgNation, 0, clanInfo.nation * 11, 11, 11, 0, i + 15 + Font.normalFont[0].getWidth(clanInfo.name) + 3, (this.height * i4) + 6, 0);
            }
            String str = "Đội trưởng: " + clanInfo.master + " - Tiền: " + clanInfo.money + "xu - Thành viên: " + ((int) clanInfo.members);
            if (i4 == selected) {
                int width = Font.arialFont11.getWidth(str);
                int i6 = this.w;
                if (width > i6 - 65 && selected == i4) {
                    if (this.dir1 == 1) {
                        int i7 = this.x1 - 1;
                        this.x1 = i7;
                        if (i7 < (i6 - 75) - width) {
                            this.dir1 = -1;
                        }
                    } else {
                        int i8 = this.x1 + 1;
                        this.x1 = i8;
                        if (i8 > 5) {
                            this.dir1 = 1;
                        }
                    }
                }
            }
            Font font = Font.arialFont11;
            if (i4 == selected) {
                i5 = this.x1;
            }
            font.drawString(graphics, str, i5 + 11, (this.height * i4) + 20, 0);
            i4++;
        }
    }

    private void paintMsgClan(Graphics graphics) {
        String str;
        int i = cmy;
        int i2 = this.height;
        int i3 = i / i2;
        int size = (this.h / i2) + i3 + 1;
        if (size >= this.list.size()) {
            size = this.list.size();
        }
        for (int i4 = size - 1; i4 >= i3; i4--) {
            ChatInfo chatInfo = (ChatInfo) this.list.elementAt(i4);
            Font.normalFont[0].drawString(graphics, chatInfo.name, 7, i4 * this.height, 0);
            if (Font.arialFont11.getWidth(chatInfo.content) > this.w - 10) {
                str = Font.arialFontBlack.splitFontBStrInLine(chatInfo.content, this.w - 10)[0] + "...";
            } else {
                str = chatInfo.content;
            }
            Font.arialFont11.drawString(graphics, str, 7, (this.height * i4) + 15, 0);
        }
    }

    private void paintChar(Graphics graphics) {
        int i;
        try {
            int i2 = cmy;
            int i3 = this.height;
            int i4 = i2 / i3;
            int size = (this.h / i3) + i4 + 1;
            if (size >= this.list.size()) {
                size = this.list.size();
            }
            int i5 = size;
            int i6 = i4;
            while (i6 < i5) {
                Char r7 = (Char) this.list.elementAt(i6);
                r7.paintCharList(graphics);
                String str = "Cấp độ: " + (r7.level > -1 ? ((int) r7.level) + "" : "?");
                int size2 = r7.wearingItems.size();
                String str2 = str;
                for (int i7 = 0; i7 < size2; i7++) {
                    ItemInInventory itemInInventory = (ItemInInventory) r7.wearingItems.elementAt(i7);
                    String str3 = str2 + " - " + ItemTemplate.nameItem[Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type] + ": " + ((int) itemInInventory.level);
                    if (itemInInventory.plusTemplate > 0) {
                        str3 = str3 + "+" + ((int) itemInInventory.plusTemplate);
                    }
                    str2 = str3;
                }
                if (r7.idClan == -1 || GameData.getImgIcon(r7.idClan) == null) {
                    i = 0;
                } else {
                    GameData.paintIcon(graphics, (short) (r7.idClan + 1000), 45, (this.height * i6) + 12);
                    i = 8;
                }
                String priceMoney = GCanvas.getPriceMoney(r7.charXu, r7.luong);
                if (!priceMoney.equals("")) {
                    priceMoney = "  (" + priceMoney + ")";
                }
                graphics.setClip(45, cmy, this.w - 49, this.h - 31);
                String str4 = r7.name + "   " + priceMoney + ((this.type != 6 || r7.isMaster >= 3) ? "" : " - " + this.nameManager[r7.isMaster]) + (this.type == 7 ? " -  CT: " + r7.congTrang + " - LT: " + r7.lienTram : "");
                int width = Font.normalFont[0].getWidth(str4);
                int i8 = this.w;
                if (width > i8 - 55 && selected == i6) {
                    if (this.dir2 == 1) {
                        int i9 = this.x2 - 1;
                        this.x2 = i9;
                        if (i9 < (i8 - (width / 2)) - width) {
                            this.dir2 = -1;
                        }
                    } else {
                        int i10 = this.x2 + 1;
                        this.x2 = i10;
                        if (i10 > 5) {
                            this.dir2 = 1;
                        }
                    }
                }
                Font.normalFont[0].drawString(graphics, str4, (i6 == selected ? this.x2 : 0) + i + 45, (this.height * i6) + 5, 0);
                if (r7.nationID > -1) {
                    graphics.drawRegion(Res.imgNation, 0, r7.nationID * 11, 11, 11, 0, i + 50 + Font.normalFont[0].getWidth(r7.name) + (i6 == selected ? this.x2 : 0), (this.height * i6) + 6, 0);
                }
                int width2 = Font.arialFont11.getWidth(str2);
                int i11 = this.w;
                if (width2 > i11 - 55 && selected == i6) {
                    if (this.dir0 == 1) {
                        int i12 = this.x0 - 1;
                        this.x0 = i12;
                        if (i12 < (i11 - (width2 / 2)) - width2) {
                            this.dir0 = -1;
                        }
                    } else {
                        int i13 = this.x0 + 1;
                        this.x0 = i13;
                        if (i13 > 5) {
                            this.dir0 = 1;
                        }
                    }
                }
                Font.arialFont11.drawString(graphics, str2, 45 + (i6 == selected ? this.x0 : 0), (this.height * i6) + 20, 0);
                graphics.setClip(0, cmy, this.w - 4, this.h - 31);
                i6++;
            }
        } catch (Exception unused) {
        }
    }
}
