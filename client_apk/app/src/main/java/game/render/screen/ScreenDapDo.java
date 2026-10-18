package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Command;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.Scroll;
import game.model.ScrollResult;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.util.Vector;
import network.Message;

public class ScreenDapDo extends Screen {
    public static boolean BHLock = false;
    public static boolean LKDLock = false;
    public static boolean MMLock = false;
    public static final byte TYPE2 = 0;
    public static final byte TYPE4 = 1;
    public static final byte TYPE_ANIMAL = 2;
    public static final byte TYPE_LOCK_ITEM = 3;
    public static final byte TYPE_THEM_DONG = 4;
    public static int hCell = 0;
    public static int hImg = 80;
    public static short itemID = 0;
    static ItemInInventory itemSelect = null;
    public static ScreenDapDo me = null;
    public static int wCell = 0;
    public static int wImg = 40;
    public static int xDr;
    public static int yDr;
    Command cmdClose;
    Command cmdContinue;
    Command cmdEp;
    Command cmdErr;
    Command cmdView;
    int cout;
    public byte currentRow;
    public int h0;
    public int hSh;
    public int index;
    public int indexTabNguyenLieu;
    boolean isEp;
    boolean isShowPopup;
    public boolean isShowText;
    public Screen last;
    public int lastIndex1;
    public int lastIndex2;
    int selectItem;
    public int selectItemInventory;
    public byte timeDelay;
    public byte type;
    public int wSh;
    int[] xItem;
    int xPopup;
    public int xSh;
    int[] xTo;
    int[] yItem;
    int yPopup;
    public int ySh;
    int[] yTo;
    public static Vector vnguyenLieu = new Vector();
    static GemTemplate selectGem = null;
    static GemTemplate selectGemBH = null;
    static GemTemplate selectGemMM = null;
    public static Vector listItem = new Vector();
    public static boolean isFinishImbue = true;
    public static short idLkd = -1;
    public static short idMM = -1;
    public static short idBH = -1;
    public static byte COLOR_WHITE = 0;
    public static byte COLOR_GREEN = 1;
    public static byte COLOR_RED = 2;
    public static byte COLOR_BLUE = 3;
    public static byte COLOR_MAGENTA = 4;
    public static byte COLOR_YELLOW = 5;
    public Scroll mScrollItemInventory = new Scroll();
    public Scroll mScrollShowText = new Scroll();
    public Scroll mScrollNguyenLieu = new Scroll();
    public int wTab = 28;
    public String[] arrShowText = {""};
    public String tile = "Đập đồ";
    String infoPoup = "Thong bao";

    public static boolean isBH(int i) {
        return i >= 0 && i <= 4;
    }

    public static boolean isDaMayMan(int i) {
        return (i >= 5 && i <= 7) || i == 155 || i == 156;
    }

    public static boolean isLKD(int i) {
        return (i >= 8 && i <= 11) || i == 66 || i == 245 || i == 157 || i == 158;
    }

    public void setData(Message message) {
    }

    public static ScreenDapDo gI() {
        ScreenDapDo screenDapDo = me;
        if (screenDapDo != null) {
            return screenDapDo;
        }
        ScreenDapDo screenDapDo2 = new ScreenDapDo();
        me = screenDapDo2;
        return screenDapDo2;
    }

    public void resetData() {
        this.mScrollItemInventory.clear();
        this.mScrollNguyenLieu.clear();
        this.mScrollShowText.clear();
        vnguyenLieu = new Vector();
        this.index = GCanvas.isSmartPhone ? -1 : 0;
        this.selectItemInventory = 0;
        this.indexTabNguyenLieu = 0;
        this.isShowPopup = false;
        setSizeCell(this.type, false);
        this.currentRow = (byte) 0;
        this.lastIndex1 = 0;
        this.lastIndex2 = 0;
        if (GCanvas.isSmartPhone) {
            this.currentRow = (byte) -1;
            this.lastIndex1 = -1;
            this.lastIndex2 = -1;
        }
        this.lastIndex2 = 0;
        if (GCanvas.isSmartPhone) {
            this.lastIndex2 = -1;
        }
    }

    public void setSizeCell(byte b, boolean z) {
        int i = (xDr + (wCell / 2)) - 44;
        int i2 = yDr + (hCell / 2);
        int i3 = i2 - 35;
        if (GCanvas.isSmartPhone) {
            i = (xDr + (wCell / 4)) - 44;
        }
        if (b == 0) {
            int[] iArr = {i, i + 66};
            this.xItem = iArr;
            this.xTo = new int[]{iArr[0], iArr[1]};
            this.yItem = new int[]{i3, i3};
        } else if (b == 1) {
            this.cmdEp.caption = "Đập";
            int[] iArr2 = {i, i - 22, i, i + 88};
            this.xItem = iArr2;
            this.xTo = new int[]{iArr2[0], iArr2[1], iArr2[2], iArr2[3]};
            this.yItem = new int[]{i2 - 53, i, i, i};
            int i4 = i + 33;
            int i5 = i2 - 24;
            this.xPopup = i + 43;
            this.yPopup = i2 + 5;
        } else if (b == 4) {
            this.cmdEp.caption = "Đập";
            int[] iArr3 = {i + 33, i + 64, i + 2};
            this.xItem = iArr3;
            this.xTo = new int[]{iArr3[0], iArr3[1], iArr3[1]};
            this.yItem = new int[]{i2 - 46, i, i};
            int i6 = i2 - 13;
            this.xPopup = i + 43;
            this.yPopup = i2 + 5;
        } else {
            this.cmdEp.caption = "Khóa";
            int[] iArr4 = {i, i};
            this.xItem = iArr4;
            this.xTo = new int[]{iArr4[0], iArr4[1]};
            this.yItem = new int[]{i2 - 53, i2 - 24};
            int i7 = i + 33;
            this.xPopup = i + 43;
            this.yPopup = i2 + 5;
        }
        this.cout = 0;
        if (z) {
            this.currentRow = (byte) 0;
            if (GCanvas.isSmartPhone) {
                this.currentRow = (byte) -1;
            }
            this.left = this.cmdEp;
            this.isShowText = false;
            this.mScrollShowText.clear();
            this.isEp = false;
        } else {
            this.isEp = false;
            this.left = this.cmdEp;
        }
        this.type = b;
        resetSelect();
        getListGem(true, false);
    }

    public ScreenDapDo() {
        int i;
        this.index = -1;
        wCell = GCanvas.w - 10;
        int i2 = (GCanvas.h - ITEM_HEIGHT) - 10;
        hCell = i2;
        if (wCell > 176) {
            wCell = 176;
        }
        if (i2 > 220) {
            hCell = 220;
        }
        if (GCanvas.w >= 320 && GCanvas.isSmartPhone) {
            wCell = 300;
        }
        xDr = (GCanvas.w / 2) - (wCell / 2);
        int i3 = ((GCanvas.h - ITEM_HEIGHT) / 2) - (hCell / 2);
        yDr = i3;
        if (i3 < 5) {
            yDr = 5;
        }
        if (xDr < 5) {
            xDr = 5;
        }
        this.wSh = wCell;
        if (GCanvas.isSmartPhone && (i = wCell) == 300) {
            this.xSh = xDr + (i / 2);
            this.ySh = yDr + 55;
            this.wSh = (i / 2) - 8;
            this.hSh = hCell - 54;
        }
        this.h0 = hCell - 54;
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (ScreenDapDo.this.isShowText) {
                    ScreenDapDo.this.isShowText = false;
                    ScreenDapDo screenDapDo = ScreenDapDo.this;
                    screenDapDo.center = screenDapDo.cmdView;
                    ScreenDapDo.this.mScrollShowText.clear();
                    ScreenDapDo.this.arrShowText = new String[]{""};
                    if (ScreenDapDo.this.isEp) {
                        ScreenDapDo screenDapDo2 = ScreenDapDo.this;
                        screenDapDo2.left = screenDapDo2.cmdContinue;
                        return;
                    } else {
                        ScreenDapDo screenDapDo3 = ScreenDapDo.this;
                        screenDapDo3.left = screenDapDo3.cmdEp;
                        return;
                    }
                }
                ScreenDapDo.this.resetData();
                ScreenDapDo.this.last.switchToMe();
            }
        });
        boolean z = GCanvas.isSmartPhone;
        this.cmdView = new Command("", new AnonymousClass2());
        this.cmdEp = new Command("Đập", new IAction() {
            @Override
            public void perform() {
                // mod: các việc auto đập/thêm dòng
                if (AutoGuard.dapGuard()) {
                    return;
                }
                ScreenDapDo.this.currentRow = (byte) -1;
                if (ScreenDapDo.vnguyenLieu.size() <= 0) {
                    GCanvas.startOKDlg("Không có nguyên liệu.", new IAction() {
                        @Override
                        public void perform() {
                            ScreenDapDo.this.right.action.perform();
                            GCanvas.endDlg();
                        }
                    });
                    return;
                }
                if (ScreenDapDo.itemSelect != null) {
                    if (ScreenDapDo.this.type == 0 || ScreenDapDo.this.type == 2 || ScreenDapDo.this.type == 3 || ScreenDapDo.this.type == 4) {
                        if (ScreenDapDo.selectGem != null) {
                            if (ScreenDapDo.this.type == 0) {
                                GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 1, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                                ScreenDapDo screenDapDo = ScreenDapDo.this;
                                screenDapDo.left = screenDapDo.cmdContinue;
                                ScreenDapDo.this.doEp();
                                return;
                            }
                            if (ScreenDapDo.this.type == 2) {
                                GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 2, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                                ScreenDapDo screenDapDo2 = ScreenDapDo.this;
                                screenDapDo2.left = screenDapDo2.cmdContinue;
                                GCanvas.startOKDlg("Xin chờ");
                                return;
                            }
                            if (ScreenDapDo.this.type == 4) {
                                if (ScreenDapDo.selectGemBH != null) {
                                    AutoGuard.themDong(GameService.gI(), ScreenDapDo.itemSelect.itemID, 5, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0, ScreenDapDo.selectGemBH.id, ScreenDapDo.selectGemBH.lock ? 1 : 0);
                                    ScreenDapDo screenDapDo3 = ScreenDapDo.this;
                                    screenDapDo3.left = screenDapDo3.cmdContinue;
                                    GCanvas.startOKDlg("Xin chờ");
                                    return;
                                }
                                GCanvas.startOKDlg("Chưa đặt đá.", new IAction() {
                                    @Override
                                    public void perform() {
                                        ScreenDapDo.this.right.action.perform();
                                        GCanvas.endDlg();
                                    }
                                });
                                return;
                            }
                            GameService.gI().doRequestTachNguyenLIeu(ScreenDapDo.itemSelect.itemID, 3, ScreenDapDo.selectGem.id, ScreenDapDo.selectGem.lock ? 1 : 0);
                            ScreenDapDo screenDapDo4 = ScreenDapDo.this;
                            screenDapDo4.left = screenDapDo4.cmdContinue;
                            GCanvas.startOKDlg("Xin chờ");
                            return;
                        }
                        GCanvas.startOKDlg(ScreenDapDo.this.type == 4 ? "Bạn chưa chọn bột" : "Bạn chưa chọn đá thuộc tính.", new IAction() {
                            @Override
                            public void perform() {
                                ScreenDapDo.this.right.action.perform();
                                GCanvas.endDlg();
                            }
                        });
                        return;
                    }
                    if (ScreenDapDo.selectGem == null) {
                        GCanvas.startOKDlg("Bạn chưa chọn luyện kim dược", new IAction() {
                            @Override
                            public void perform() {
                                ScreenDapDo.this.right.action.perform();
                                GCanvas.endDlg();
                            }
                        });
                        return;
                    }
                    final Vector vector = new Vector();
                    final Vector vector2 = new Vector();
                    vector.addElement(((int) ScreenDapDo.selectGem.id) + "");
                    vector2.addElement((ScreenDapDo.selectGem.lock ? 1 : 0) + "");
                    ScreenDapDo.LKDLock = ScreenDapDo.selectGem.lock;
                    ScreenDapDo.idLkd = ScreenDapDo.selectGem.id;
                    if (ScreenDapDo.selectGemBH != null) {
                        vector.addElement(((int) ScreenDapDo.selectGemBH.id) + "");
                        vector2.addElement((ScreenDapDo.selectGemBH.lock ? 1 : 0) + "");
                        ScreenDapDo.BHLock = ScreenDapDo.selectGemBH.lock;
                        ScreenDapDo.idBH = ScreenDapDo.selectGemBH.id;
                    } else {
                        vector.addElement("-1");
                        vector2.addElement("0");
                        ScreenDapDo.BHLock = false;
                        ScreenDapDo.idBH = (short) -1;
                    }
                    if (ScreenDapDo.selectGemMM != null) {
                        vector.addElement(((int) ScreenDapDo.selectGemMM.id) + "");
                        vector2.addElement((ScreenDapDo.selectGemMM.lock ? 1 : 0) + "");
                        ScreenDapDo.MMLock = ScreenDapDo.selectGemMM.lock;
                        ScreenDapDo.idMM = ScreenDapDo.selectGemMM.id;
                    } else {
                        vector.addElement("-1");
                        vector2.addElement("0");
                        ScreenDapDo.MMLock = false;
                        ScreenDapDo.idMM = (short) -1;
                    }
                    ScreenDapDo.itemID = ScreenDapDo.itemSelect.itemID;
                    Vector vector3 = new Vector();
                    for (int i40 = 1; i40 <= 15; i40++) {
                        final int i4 = i40;
                        vector3.addElement(new Command("Cộng " + i4, new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doAutoImbue(ScreenDapDo.itemSelect.itemID, vector, i4, vector2, -1);
                                ScreenDapDo.this.isShowPopup = true;
                                ScreenDapDo.this.infoPoup = "Xin chờ";
                                ScreenDapDo.isFinishImbue = false;
                                GCanvas.startOKDlg("Xin chờ");
                            }
                        }));
                    }
                    AutoGuard.startAtDapDo(GCanvas.menu, vector3, 3);
                    GCanvas.currentDialog = null;
                    return;
                }
                GCanvas.startOKDlg("Chưa đặt vật phẩm.", new IAction() {
                    @Override
                    public void perform() {
                        ScreenDapDo.this.right.action.perform();
                        GCanvas.endDlg();
                    }
                });
            }
        });
        this.cmdContinue = new Command("Tiếp tục", new IAction() {
            @Override
            public void perform() {
                ScreenDapDo screenDapDo = ScreenDapDo.this;
                screenDapDo.setSizeCell(screenDapDo.type, true);
            }
        });
        this.cmdErr = new Command("", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.right = this.cmdClose;
        this.center = this.cmdView;
        if (!GCanvas.isSmartPhone) {
            this.index = 0;
        }
        setData(null);
        this.lastIndex1 = 0;
        if (GCanvas.isSmartPhone) {
            this.lastIndex1 = -1;
            this.currentRow = (byte) -1;
            this.lastIndex2 = -1;
        }
        this.lastIndex2 = 0;
        if (GCanvas.isSmartPhone) {
            this.lastIndex2 = -1;
        }
    }

    class AnonymousClass2 implements IAction {
        AnonymousClass2() {
        }

        @Override
        public void perform() {
            Vector vector = new Vector();
            vector.addElement(new Command("Bỏ vào", new IAction() {
                @Override
                public void perform() {
                    if (GCanvas.isSmartPhone) {
                        ScreenDapDo.this.timeDelay = (byte) 5;
                    }
                    if (ScreenDapDo.this.currentRow == 1) {
                        if (ScreenDapDo.this.selectItemInventory == -1 || ScreenDapDo.this.selectItemInventory > ScreenDapDo.listItem.size() - 1) {
                            return;
                        }
                        if (ScreenDapDo.this.type == 1 && !ScreenDapDo.isFinishImbue) {
                            GCanvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                                @Override
                                public void perform() {
                                    ScreenDapDo.this.right.action.perform();
                                    GCanvas.endDlg();
                                }
                            });
                            return;
                        } else {
                            ScreenDapDo.itemSelect = (ItemInInventory) ScreenDapDo.listItem.elementAt(ScreenDapDo.this.selectItemInventory);
                            return;
                        }
                    }
                    if (ScreenDapDo.this.currentRow != 0 || ScreenDapDo.vnguyenLieu.size() <= 0) {
                        return;
                    }
                    if (ScreenDapDo.this.type == 0 || ScreenDapDo.this.type == 2 || ScreenDapDo.this.type == 3) {
                        ScreenDapDo.selectGem = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                        return;
                    }
                    if (ScreenDapDo.this.type == 4) {
                        GemTemplate gemTemplate = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                        GemTemp gemByID = Res.getGemByID(gemTemplate.id);
                        if (ScreenDapDo.isBot(gemByID.type)) {
                            ScreenDapDo.selectGem = gemTemplate;
                            return;
                        } else {
                            if (ScreenDapDo.this.isDaTienCap(gemByID.type)) {
                                ScreenDapDo.selectGemBH = gemTemplate;
                                return;
                            }
                            return;
                        }
                    }
                    if (!ScreenDapDo.isFinishImbue) {
                        GCanvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                            @Override
                            public void perform() {
                                ScreenDapDo.this.right.action.perform();
                                GCanvas.endDlg();
                            }
                        });
                        return;
                    }
                    GemTemplate gemTemplate2 = (GemTemplate) ScreenDapDo.vnguyenLieu.elementAt(ScreenDapDo.this.indexTabNguyenLieu);
                    if (ScreenDapDo.isDaMayMan(gemTemplate2.id)) {
                        ScreenDapDo.selectGemMM = gemTemplate2;
                    } else if (ScreenDapDo.isBH(gemTemplate2.id)) {
                        ScreenDapDo.selectGemBH = gemTemplate2;
                    } else if (ScreenDapDo.isLKD(gemTemplate2.id)) {
                        ScreenDapDo.selectGem = gemTemplate2;
                    }
                }
            }));
            vector.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    if (GCanvas.isSmartPhone) {
                        ScreenDapDo.this.timeDelay = (byte) 5;
                    }
                    ScreenDapDo.this.doView();
                }
            }));
            GCanvas.menu.startAt(vector, 3);
            GCanvas.currentDialog = null;
        }
    }

    protected void doEp() {
        int i = (xDr + (wCell / 2)) - 44;
        int i2 = hCell / 2;
        if (GCanvas.isSmartPhone) {
            i = (xDr + (wCell / 4)) - 44;
        }
        int[] iArr = this.xTo;
        int i3 = i + 33;
        iArr[1] = i3;
        iArr[0] = i3;
        this.cout = 10;
        this.isEp = true;
    }

    public void updatEp() {
        int i = this.cout - 1;
        this.cout = i;
        if (i < 0) {
            this.cout = 0;
        }
        if (this.cout > 0) {
            return;
        }
        int[] iArr = this.xItem;
        int i2 = iArr[0];
        int[] iArr2 = this.xTo;
        int i3 = iArr2[0];
        if (i2 != i3) {
            if (((i3 - i2) >> 1) == 0) {
                iArr[0] = i3;
            } else {
                iArr[0] = i2 + ((i3 - i2) >> 1);
            }
        }
        int i4 = iArr[1];
        int i5 = iArr2[1];
        if (i4 != i5) {
            if (((i5 - i4) >> 1) == 0) {
                iArr[1] = i5;
            } else {
                iArr[1] = i4 + ((i5 - i4) >> 1);
            }
        }
    }

    protected void doView() {
        int i;
        byte b = this.currentRow;
        if (b == 1) {
            if (Char.inventory.size() == 0 || (i = this.selectItemInventory) < 0 || i > listItem.size() - 1) {
                return;
            }
            ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
            if (!itemInInventory.isEnoughData) {
                itemInInventory.isEnoughData = true;
                GameService.gI().requestItemInfo(itemInInventory.itemID, GCanvas.gameScr.mainChar.ID);
            }
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(itemInInventory.getDescription(false), this.wSh - 10);
        } else if (b == 0) {
            if (vnguyenLieu.size() <= 0) {
                return;
            }
            GemTemp gemByID = Res.getGemByID(((GemTemplate) vnguyenLieu.elementAt(this.indexTabNguyenLieu)).id);
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(ItemInInventory.setTemp(gemByID.name, "0") + ItemInInventory.setTemp(gemByID.decript, "0"), this.wSh - 10);
        }
        if (!GCanvas.isSmartPhone) {
            String[] strArr = this.arrShowText;
            if (strArr.length == 1) {
                this.hSh = 35;
                this.ySh = (GCanvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (strArr.length * ITEM_HEIGHT) + 8;
                this.ySh = (GCanvas.h / 2) - ((this.arrShowText.length * (ITEM_HEIGHT - 3)) / 2);
            }
            int i2 = this.ySh;
            int i3 = yDr;
            if (i2 < i3) {
                this.ySh = i3;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = xDr;
        }
        this.isShowText = true;
        if (!GCanvas.isSmartPhone) {
            this.center = this.cmdErr;
        }
        this.left = this.cmdErr;
    }

    public void paintCell(Graphics graphics) {
        graphics.setColor(-16751521);
        int i = GCanvas.isSmartPhone ? -27 : 10;
        graphics.fillRect(this.xSh + 2, this.ySh + i, this.wSh, this.hSh - 8);
        graphics.setColor(-2496);
        graphics.drawRect(this.xSh + 2, this.ySh + i, this.wSh, this.hSh - 8);
    }

    public void paintShowText(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (wCell >= 300) {
            paintCell(graphics);
            if (!this.isShowText) {
                return;
            }
        } else if (!this.isShowText) {
            return;
        } else {
            paintCell(graphics);
        }
        int i = GCanvas.isSmartPhone ? 24 : 0;
        this.mScrollShowText.setStyle(this.arrShowText.length, ITEM_HEIGHT - 2, this.xSh, this.ySh - i, this.wSh - 2, this.hSh - 10, true, 0);
        graphics.ClipRec(this.xSh, (this.ySh + (GCanvas.isSmartPhone ? 2 : 10)) - i, this.wSh - 2, this.hSh - 15);
        this.mScrollShowText.setClip(graphics, this.xSh, (this.ySh + (GCanvas.isSmartPhone ? 2 : 10)) - i, this.wSh - 2, this.hSh - 15);
        int i2 = 0;
        while (true) {
            String[] strArr = this.arrShowText;
            if (i2 < strArr.length) {
                if (!strArr[i2].equals("")) {
                    byte bCharAt = (byte) (this.arrShowText[i2].charAt(0) - '0');
                    boolean zIsDegit = WindowInfoScr.isDegit(this.arrShowText[i2].charAt(0));
                    if (!zIsDegit) {
                        bCharAt = 0;
                    }
                    Font[] fontArr = Font.normalFont;
                    if (bCharAt >= 5) {
                        bCharAt = 0;
                    }
                    fontArr[bCharAt].drawString(graphics, this.arrShowText[i2].substring(zIsDegit ? 1 : 0), this.xSh + 8, ((this.ySh + ((ITEM_HEIGHT - 3) * i2)) + (GCanvas.isSmartPhone ? 6 : 14)) - ((GCanvas.isSmartPhone ? 2 : 0) + i), 0);
                }
                i2++;
            } else {
                Graphics.resetTransAndroid(graphics);
                graphics.restoreCanvas();
                GCanvas.resetTrans(graphics);
                return;
            }
        }
    }

    @Override
    public void paint(Graphics graphics) {
        int i;
        Screen screen = this.last;
        if (screen != null) {
            screen.paint(graphics);
        }
        if (GCanvas.isSmartPhone && (i = wCell) == 300) {
            int i2 = i / 2;
        }
        Res.paintDlgFull(graphics, xDr, yDr, wCell, hCell, 25, this.tile, true, this.h0 - 28);
        GCanvas.resetTrans(graphics);
        this.mScrollItemInventory.setStyle(Char.inventory.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
        graphics.ClipRec(xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
        this.mScrollItemInventory.setClip(graphics, xDr + 2, this.h0 + yDr + 23, wCell - 4, 31);
        for (int i3 = 0; i3 < listItem.size(); i3++) {
            if (this.selectItemInventory == i3 && this.currentRow == 1) {
                graphics.setColor(-10263709);
                int i4 = xDr;
                int i5 = this.wTab;
                graphics.fillRect(i4 + (i5 * i3), yDr + 23 + this.h0, i5, 28);
            }
            ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(i3);
            int i6 = xDr;
            int i7 = this.wTab;
            itemInInventory.paint(graphics, i6 + (i7 * i3) + (i7 / 2), yDr + 36 + this.h0);
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + (this.wTab * i3), yDr + 22 + this.h0, 1, 29);
            ItemInInventory itemInInventory2 = itemSelect;
            if (itemInInventory2 != null && itemInInventory2.itemID == itemInInventory.itemID) {
                graphics.setColor(-13508609);
                int i8 = xDr;
                int i9 = this.wTab;
                graphics.drawRect(i8 + (i9 * i3) + 1, yDr + 23 + this.h0, i9 - 2, 28);
                int i10 = xDr;
                int i11 = this.wTab;
                graphics.drawRect(i10 + (i11 * i3) + 2, yDr + 24 + this.h0, i11 - 4, 26);
            }
        }
        graphics.setColor(-2249714);
        graphics.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        paintnguyenlieu(graphics);
        GCanvas.resetTrans(graphics);
        paintItemEp(graphics);
        paintShowText(graphics);
        super.paint(graphics);
    }

    public void paintnguyenlieu(Graphics graphics) {
        int i = !GCanvas.isSmartPhone ? wCell - 2 : (wCell - 2) - this.wSh;
        GCanvas.resetTrans(graphics);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        this.mScrollNguyenLieu.setStyle(vnguyenLieu.size(), this.wTab + 2, xDr, yDr + this.h0, i, 27, false, 0);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        int i2 = i - 2;
        graphics.ClipRec2(xDr + 2, ((yDr + 23) + this.h0) - 28, i2, 31);
        this.mScrollNguyenLieu.setClip(graphics, xDr + 2, ((yDr + 23) + this.h0) - 28, i2, 31);
        for (int i3 = 0; i3 < vnguyenLieu.size(); i3++) {
            if (this.indexTabNguyenLieu == i3 && this.currentRow == 0) {
                graphics.setColor(-10263709);
                int i4 = xDr;
                int i5 = this.wTab;
                graphics.fillRect(i4 + (i5 * i3), ((yDr + 24) + this.h0) - 28, i5, 26);
            }
            GemTemplate gemTemplate = (GemTemplate) vnguyenLieu.elementAt(i3);
            if (gemTemplate.lock) {
                graphics.setColor(-13500365);
                int i6 = xDr;
                int i7 = this.wTab;
                graphics.fillRect(i6 + (i7 * i3), ((yDr + 24) + this.h0) - 28, i7, 26);
                if (this.indexTabNguyenLieu == i3 && this.currentRow == 0) {
                    graphics.setColor(-10263709);
                    int i8 = xDr;
                    int i9 = this.wTab;
                    graphics.fillRect(i8 + (i9 * i3), ((yDr + 24) + this.h0) - 28, i9, 26);
                }
            }
            GemTemplate gemTemplate2 = selectGem;
            if (gemTemplate2 != null && gemTemplate2.id == gemTemplate.id && selectGem.lock == gemTemplate.lock) {
                graphics.setColor(-13508609);
                int i10 = xDr;
                int i11 = this.wTab;
                graphics.drawRect(i10 + (i11 * i3) + 1, ((yDr + 24) + this.h0) - 28, i11 - 2, 25);
                int i12 = xDr;
                int i13 = this.wTab;
                graphics.drawRect(i12 + (i13 * i3) + 2, ((yDr + 24) + this.h0) - 27, i13 - 4, 23);
            }
            GemTemplate gemTemplate3 = selectGemBH;
            if (gemTemplate3 != null && gemTemplate3.id == gemTemplate.id && selectGemBH.lock == gemTemplate.lock) {
                graphics.setColor(-1892865);
                int i14 = xDr;
                int i15 = this.wTab;
                graphics.drawRect(i14 + (i15 * i3) + 1, ((yDr + 24) + this.h0) - 28, i15 - 2, 25);
                int i16 = xDr;
                int i17 = this.wTab;
                graphics.drawRect(i16 + (i17 * i3) + 2, ((yDr + 24) + this.h0) - 27, i17 - 4, 23);
            }
            GemTemplate gemTemplate4 = selectGemMM;
            if (gemTemplate4 != null && gemTemplate4.id == gemTemplate.id && selectGemMM.lock == gemTemplate.lock) {
                graphics.setColor(-1892865);
                int i18 = xDr;
                int i19 = this.wTab;
                graphics.drawRect(i18 + (i19 * i3) + 1, ((yDr + 24) + this.h0) - 28, i19 - 2, 25);
                int i20 = xDr;
                int i21 = this.wTab;
                graphics.drawRect(i20 + (i21 * i3) + 2, ((yDr + 24) + this.h0) - 27, i21 - 4, 23);
            }
            short s = Res.getGemByID(gemTemplate.id).idImage;
            int i22 = xDr;
            int i23 = this.wTab;
            Res.paintGem(graphics, s, i22 + (i23 * i3) + (i23 / 2), ((yDr + 24) + this.h0) - 15);
            Font font = Font.smallFontWHITE;
            String str = gemTemplate.number + "";
            int i24 = xDr;
            int i25 = this.wTab;
            font.drawString(graphics, str, ((i24 + (i25 * i3)) + i25) - 1, ((yDr + 22) + this.h0) - Font.smallFontWHITE.getHeight(), 1);
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + (this.wTab * i3), ((yDr + 22) + this.h0) - 28, 1, 29);
        }
        graphics.fillRect(xDr + (this.wTab * vnguyenLieu.size()), ((yDr + 22) + this.h0) - 28, 1, 28);
        Graphics.resetTransAndroid(graphics);
        GCanvas.resetTrans(graphics);
        graphics.restoreCanvas();
    }

    public void paintItemEp(Graphics graphics) {
        GemTemplate gemTemplate;
        for (int i = 0; i < this.xItem.length; i++) {
            graphics.setColor(-1);
            graphics.fillRect(this.xItem[i], this.yItem[i], 22, 22);
            if (this.cout % 2 == 0) {
                graphics.setColor(-10263709);
                graphics.fillRect(this.xItem[i] + 1, this.yItem[i] + 1, 20, 20);
            }
        }
        byte b = this.type;
        if (b != 1) {
            GemTemplate gemTemplate2 = selectGem;
            if (gemTemplate2 != null) {
                Res.paintGem(graphics, Res.getGemByID(gemTemplate2.id).idImage, this.xItem[1] + 10, this.yItem[1] + 10);
            }
            ItemInInventory itemInInventory = itemSelect;
            if (itemInInventory != null) {
                itemInInventory.paint(graphics, this.xItem[0] + 10, this.yItem[0] + 10);
            }
            if (this.type == 4 && (gemTemplate = selectGemBH) != null) {
                Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, this.xItem[2] + 10, this.yItem[2] + 10);
            }
        } else if (b == 1) {
            GemTemplate gemTemplate3 = selectGem;
            if (gemTemplate3 != null) {
                Res.paintGem(graphics, Res.getGemByID(gemTemplate3.id).idImage, this.xItem[2] + 10, this.yItem[2] + 10);
            }
            GemTemplate gemTemplate4 = selectGemBH;
            if (gemTemplate4 != null) {
                Res.paintGem(graphics, Res.getGemByID(gemTemplate4.id).idImage, this.xItem[1] + 10, this.yItem[1] + 10);
            }
            GemTemplate gemTemplate5 = selectGemMM;
            if (gemTemplate5 != null) {
                Res.paintGem(graphics, Res.getGemByID(gemTemplate5.id).idImage, this.xItem[3] + 10, this.yItem[3] + 10);
            }
            ItemInInventory itemInInventory2 = itemSelect;
            if (itemInInventory2 != null) {
                itemInInventory2.paint(graphics, this.xItem[0] + 10, this.yItem[0] + 10);
            }
        }
        if (this.type == 0 || !this.isShowPopup || this.infoPoup.equals("")) {
            return;
        }
        int i2 = yDr + this.h0;
        graphics.setColor(-1);
        graphics.fillRect(this.xPopup - 60, i2 - 27, 120, 20);
        graphics.setColor(-13554904);
        graphics.fillRect(this.xPopup - 59, i2 - 26, 118, 18);
        Font.normalFont[0].drawString(graphics, this.infoPoup, this.xPopup, i2 - 24, 2);
    }

    @Override
    public void update() {
        boolean z;
        boolean z2;
        int i;
        Screen screen = this.last;
        if (screen != null) {
            screen.update();
        }
        updatEp();
        byte b = this.timeDelay;
        if (b > 0) {
            this.timeDelay = (byte) (b - 1);
        }
        if (this.timeDelay <= 0) {
            this.timeDelay = (byte) 0;
        }
        if (GCanvas.currentDialog == null) {
            if (GCanvas.isSmartPhone) {
                if (this.mScrollShowText.FocusNew) {
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                } else if (this.mScrollItemInventory.FocusNew) {
                    this.mScrollShowText.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                } else if (this.mScrollNguyenLieu.FocusNew) {
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollShowText.FocusNew = false;
                }
                if (this.mScrollItemInventory.FocusNew && !GCanvas.menu.showMenu && this.timeDelay == 0) {
                    this.currentRow = (byte) 1;
                    ScrollResult scrollResultUpdateKey = this.mScrollItemInventory.updateKey();
                    if (scrollResultUpdateKey.isDowning || scrollResultUpdateKey.isFinish) {
                        this.selectItemInventory = scrollResultUpdateKey.selected;
                        this.lastIndex1 = scrollResultUpdateKey.selected;
                        if (this.right != null && this.isShowText && GCanvas.canTouch()) {
                            this.right.action.perform();
                        }
                        z = true;
                    } else {
                        z = false;
                    }
                } else {
                    z = false;
                }
                this.mScrollItemInventory.updatecm();
                if (this.mScrollNguyenLieu.FocusNew && !GCanvas.menu.showMenu && this.timeDelay == 0) {
                    this.currentRow = (byte) 0;
                    ScrollResult scrollResultUpdateKey2 = this.mScrollNguyenLieu.updateKey();
                    if (scrollResultUpdateKey2.isDowning || scrollResultUpdateKey2.isFinish) {
                        this.indexTabNguyenLieu = scrollResultUpdateKey2.selected;
                        this.lastIndex2 = scrollResultUpdateKey2.selected;
                        if (this.right != null && this.isShowText && GCanvas.canTouch()) {
                            this.right.action.perform();
                        }
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                } else {
                    z2 = false;
                }
                this.mScrollNguyenLieu.updatecm();
                if (this.mScrollShowText.FocusNew && !GCanvas.menu.showMenu) {
                    this.mScrollShowText.updateKey();
                }
                this.mScrollShowText.updatecm();
                if (!GCanvas.isPointerDown) {
                    if (this.mScrollItemInventory.FocusNew && z) {
                        int i2 = this.selectItemInventory;
                        if (i2 > -1 && i2 <= listItem.size() - 1) {
                            int i3 = this.lastIndex1;
                            if (i3 != -1) {
                                this.selectItemInventory = i3;
                            }
                            this.cmdView.action.perform();
                        }
                    } else if (this.mScrollNguyenLieu.FocusNew && z2 && (i = this.indexTabNguyenLieu) > -1 && i <= vnguyenLieu.size() - 1) {
                        int i4 = this.lastIndex2;
                        if (i4 != -1) {
                            this.indexTabNguyenLieu = i4;
                        }
                        this.cmdView.action.perform();
                    }
                    this.mScrollShowText.FocusNew = false;
                    this.mScrollItemInventory.FocusNew = false;
                    this.mScrollNguyenLieu.FocusNew = false;
                }
            } else if (!this.isShowText) {
                ScrollResult scrollResultUpdateKey3 = this.mScrollItemInventory.updateKey();
                if (scrollResultUpdateKey3.isDowning || scrollResultUpdateKey3.isFinish) {
                    int i5 = scrollResultUpdateKey3.selected;
                    this.selectItemInventory = i5;
                    this.mScrollItemInventory.moveTo(i5 * (this.wTab + 2));
                }
                this.mScrollItemInventory.updatecm();
                ScrollResult scrollResultUpdateKey4 = this.mScrollNguyenLieu.updateKey();
                if (scrollResultUpdateKey4.isDowning || scrollResultUpdateKey4.isFinish) {
                    int i6 = scrollResultUpdateKey4.selected;
                    this.indexTabNguyenLieu = i6;
                    this.mScrollNguyenLieu.moveTo(i6 * (this.wTab + 2));
                }
                this.mScrollNguyenLieu.updatecm();
            } else {
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            }
        }
        super.update();
    }

    public static boolean isPointer(int i, int i2, int i3, int i4) {
        if (GCanvas.isPointerDown) {
            return GCanvas.px < i || GCanvas.px > i + i3 || GCanvas.py < i2 || GCanvas.py > i2 + i4;
        }
        return false;
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (GCanvas.keyPressed[2]) {
                GCanvas.keyPressed[2] = false;
                this.mScrollShowText.cmtoY -= 50;
                if (this.mScrollShowText.cmtoY < 0) {
                    this.mScrollShowText.cmtoY = 0;
                }
            } else if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                this.mScrollShowText.cmtoY += 50;
                if (this.mScrollShowText.cmtoY > this.mScrollShowText.cmyLim) {
                    Scroll scroll = this.mScrollShowText;
                    scroll.cmtoY = scroll.cmyLim;
                }
            }
        } else {
            if (GCanvas.keyPressed[2]) {
                GCanvas.keyPressed[2] = false;
                byte b = (byte) (this.currentRow - 1);
                this.currentRow = b;
                if (b < 0) {
                    this.currentRow = (byte) 0;
                }
            } else if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                byte b2 = (byte) (this.currentRow + 1);
                this.currentRow = b2;
                if (b2 > 1) {
                    this.currentRow = (byte) 1;
                }
            }
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                byte b3 = this.currentRow;
                if (b3 == 1) {
                    int i = this.selectItemInventory - 1;
                    this.selectItemInventory = i;
                    if (i < 0) {
                        this.selectItemInventory = listItem.size() - 1;
                    }
                    this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
                } else if (b3 == 0) {
                    int i2 = this.indexTabNguyenLieu - 1;
                    this.indexTabNguyenLieu = i2;
                    if (i2 < 0) {
                        this.indexTabNguyenLieu = vnguyenLieu.size() - 1;
                    }
                    this.mScrollNguyenLieu.moveTo(this.indexTabNguyenLieu * this.wTab);
                } else {
                    int i3 = this.selectItem - 1;
                    this.selectItem = i3;
                    if (i3 < 0) {
                        this.selectItem = this.xItem.length - 1;
                    }
                }
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                byte b4 = this.currentRow;
                if (b4 == 1) {
                    int i4 = this.selectItemInventory + 1;
                    this.selectItemInventory = i4;
                    if (i4 > listItem.size() - 1) {
                        this.selectItemInventory = 0;
                    }
                    this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
                } else if (b4 == 0) {
                    int i5 = this.indexTabNguyenLieu + 1;
                    this.indexTabNguyenLieu = i5;
                    if (i5 > vnguyenLieu.size() - 1) {
                        this.indexTabNguyenLieu = 0;
                    }
                    this.mScrollNguyenLieu.moveTo(this.indexTabNguyenLieu * this.wTab);
                } else {
                    int i6 = this.selectItem - 1;
                    this.selectItem = i6;
                    if (i6 < 0) {
                        this.selectItem = this.xItem.length - 1;
                    }
                }
            }
        }
        super.updateKey();
    }

    public static boolean isBot(int i) {
        return i == GemTemp.TYPE_BOT;
    }

    public boolean isDaTienCap(int i) {
        return i == GemTemp.TYPE_DA_TIEN_GIAI;
    }

    public void getListGem(boolean z, boolean z2) {
        byte b;
        GemTemplate gemTemplate;
        GemTemplate gemTemplate2;
        GemTemplate gemTemplate3;
        GemTemplate gemTemplate4;
        vnguyenLieu.removeAllElements();
        if (z) {
            listItem.removeAllElements();
        }
        Vector vector = MainChar.gemItem;
        int size = vector.size();
        int i = 0;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        while (true) {
            b = 3;
            if (i >= size) {
                break;
            }
            GemTemplate gemTemplate5 = (GemTemplate) vector.elementAt(i);
            GemTemp gemByID = Res.getGemByID(gemTemplate5.id);
            byte b2 = this.type;
            if (b2 == 0) {
                if (gemByID.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    GemTemplate gemTemplate6 = selectGem;
                    if (gemTemplate6 != null && gemTemplate6.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                        z3 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (b2 == 2) {
                if (gemByID.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    GemTemplate gemTemplate7 = selectGem;
                    if (gemTemplate7 != null && gemTemplate7.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (b2 == 3) {
                if (gemByID.type == GemTemp.TYPE_LOCK_ITEM) {
                    GemTemplate gemTemplate8 = selectGem;
                    if (gemTemplate8 != null && gemTemplate8.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (b2 == 4) {
                if (gemByID.type == GemTemp.TYPE_BOT || gemByID.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                    GemTemplate gemTemplate9 = selectGem;
                    if (gemTemplate9 != null && gemTemplate9.id == gemTemplate5.id && !selectGem.lock && gemByID.type == GemTemp.TYPE_BOT) {
                        selectGem = gemTemplate5;
                        z3 = true;
                    }
                    GemTemplate gemTemplate10 = selectGemBH;
                    if (gemTemplate10 != null && gemTemplate10.id == gemTemplate5.id && !selectGemBH.lock && gemByID.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                        selectGemBH = gemTemplate5;
                        z4 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (gemByID.type == GemTemp.TYPE_IMBUE) {
                GemTemplate gemTemplate11 = selectGem;
                if (gemTemplate11 != null && gemTemplate11.id == gemTemplate5.id && !selectGem.lock) {
                    selectGem = gemTemplate5;
                } else {
                    if (selectGem == null && z2 && idLkd == gemTemplate5.id && !LKDLock) {
                        selectGem = gemTemplate5;
                    }
                    gemTemplate3 = selectGemBH;
                    if (gemTemplate3 == null && gemTemplate3.id == gemTemplate5.id && !selectGemBH.lock) {
                        selectGemBH = gemTemplate5;
                    } else {
                        if (selectGemBH == null && z2 && idBH == gemTemplate5.id && !BHLock) {
                            selectGemBH = gemTemplate5;
                        }
                        gemTemplate4 = selectGemMM;
                        if (gemTemplate4 == null && gemTemplate4.id == gemTemplate5.id && !selectGemMM.lock) {
                            selectGemMM = gemTemplate5;
                        } else {
                            if (selectGemMM == null && z2 && idMM == gemTemplate5.id && !MMLock) {
                                selectGemMM = gemTemplate5;
                            }
                            vnguyenLieu.addElement(gemTemplate5);
                        }
                        z5 = true;
                        vnguyenLieu.addElement(gemTemplate5);
                    }
                    z4 = true;
                    gemTemplate4 = selectGemMM;
                    if (gemTemplate4 == null) {
                        if (selectGemMM == null) {
                            selectGemMM = gemTemplate5;
                            z5 = true;
                        }
                    } else if (selectGemMM == null) {
                        selectGemMM = gemTemplate5;
                        z5 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
                z3 = true;
                gemTemplate3 = selectGemBH;
                if (gemTemplate3 == null) {
                    if (selectGemBH == null) {
                        selectGemBH = gemTemplate5;
                        z4 = true;
                    }
                } else if (selectGemBH == null) {
                    selectGemBH = gemTemplate5;
                    z4 = true;
                }
                gemTemplate4 = selectGemMM;
                if (gemTemplate4 == null) {
                    if (selectGemMM == null) {
                        selectGemMM = gemTemplate5;
                        z5 = true;
                    }
                } else if (selectGemMM == null) {
                    selectGemMM = gemTemplate5;
                    z5 = true;
                }
                vnguyenLieu.addElement(gemTemplate5);
            }
            i++;
        }
        Vector vector2 = MainChar.gemItemKhoa;
        int size2 = vector2.size();
        int i2 = 0;
        boolean z6 = false;
        boolean z7 = false;
        boolean z8 = false;
        while (i2 < size2) {
            GemTemplate gemTemplate12 = (GemTemplate) vector2.elementAt(i2);
            GemTemp gemByID2 = Res.getGemByID(gemTemplate12.id);
            byte b3 = this.type;
            if (b3 == 0) {
                if (gemByID2.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    GemTemplate gemTemplate13 = selectGem;
                    if (gemTemplate13 != null && gemTemplate13.id == gemTemplate12.id && selectGem.lock) {
                        selectGem = gemTemplate12;
                        z6 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate12);
                    gemTemplate12.lock = true;
                }
            } else if (b3 == 2) {
                if (gemByID2.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    GemTemplate gemTemplate14 = selectGem;
                    if (gemTemplate14 != null && gemTemplate14.id == gemTemplate12.id && selectGem.lock) {
                        selectGem = gemTemplate12;
                    }
                    vnguyenLieu.addElement(gemTemplate12);
                    gemTemplate12.lock = true;
                }
            } else if (b3 == b) {
                if (gemByID2.type == GemTemp.TYPE_LOCK_ITEM) {
                    GemTemplate gemTemplate15 = selectGem;
                    if (gemTemplate15 != null && gemTemplate15.id == gemTemplate12.id && selectGem.lock) {
                        selectGem = gemTemplate12;
                    }
                    vnguyenLieu.addElement(gemTemplate12);
                    gemTemplate12.lock = true;
                }
            } else if (b3 == 4) {
                System.out.println("vao day ne");
                if (gemByID2.type == GemTemp.TYPE_BOT || gemByID2.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                    GemTemplate gemTemplate16 = selectGem;
                    if (gemTemplate16 != null && gemTemplate16.id == gemTemplate12.id && selectGem.lock && gemByID2.type == GemTemp.TYPE_BOT) {
                        selectGem = gemTemplate12;
                        z6 = true;
                    }
                    GemTemplate gemTemplate17 = selectGemBH;
                    if (gemTemplate17 != null && gemTemplate17.id == gemTemplate12.id && selectGemBH.lock && gemByID2.type == GemTemp.TYPE_DA_TIEN_GIAI) {
                        selectGemBH = gemTemplate12;
                        z7 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate12);
                    gemTemplate12.lock = true;
                }
            } else if (gemByID2.type == GemTemp.TYPE_IMBUE) {
                GemTemplate gemTemplate18 = selectGem;
                if (gemTemplate18 != null && gemTemplate18.id == gemTemplate12.id && selectGem.lock) {
                    selectGem = gemTemplate12;
                } else {
                    if (selectGem == null && z2 && idLkd == gemTemplate12.id && LKDLock) {
                        selectGem = gemTemplate12;
                    }
                    gemTemplate = selectGemBH;
                    if (gemTemplate == null && gemTemplate.id == gemTemplate12.id && selectGemBH.lock) {
                        selectGemBH = gemTemplate12;
                    } else {
                        if (selectGemBH == null && z2 && idBH == gemTemplate12.id && BHLock) {
                            selectGemBH = gemTemplate12;
                        }
                        gemTemplate2 = selectGemMM;
                        if (gemTemplate2 == null && gemTemplate2.id == gemTemplate12.id && selectGemMM.lock) {
                            selectGemMM = gemTemplate12;
                        } else {
                            if (selectGemMM == null && z2 && idMM == gemTemplate12.id && MMLock) {
                                selectGemMM = gemTemplate12;
                            }
                            vnguyenLieu.addElement(gemTemplate12);
                            gemTemplate12.lock = true;
                        }
                        z8 = true;
                        vnguyenLieu.addElement(gemTemplate12);
                        gemTemplate12.lock = true;
                    }
                    z7 = true;
                    gemTemplate2 = selectGemMM;
                    if (gemTemplate2 == null) {
                        if (selectGemMM == null) {
                            selectGemMM = gemTemplate12;
                            z8 = true;
                        }
                    } else if (selectGemMM == null) {
                        selectGemMM = gemTemplate12;
                        z8 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate12);
                    gemTemplate12.lock = true;
                }
                z6 = true;
                gemTemplate = selectGemBH;
                if (gemTemplate == null) {
                    if (selectGemBH == null) {
                        selectGemBH = gemTemplate12;
                        z7 = true;
                    }
                } else if (selectGemBH == null) {
                    selectGemBH = gemTemplate12;
                    z7 = true;
                }
                gemTemplate2 = selectGemMM;
                if (gemTemplate2 == null) {
                    if (selectGemMM == null) {
                        selectGemMM = gemTemplate12;
                        z8 = true;
                    }
                } else if (selectGemMM == null) {
                    selectGemMM = gemTemplate12;
                    z8 = true;
                }
                vnguyenLieu.addElement(gemTemplate12);
                gemTemplate12.lock = true;
            }
            i2++;
            b = 3;
        }
        byte b4 = this.type;
        if (b4 == 0) {
            if (!z3 && !z6) {
                selectGem = null;
            }
            if (!z4 && !z7) {
                selectGemBH = null;
            }
            if (!z5 && !z8) {
                selectGemMM = null;
            }
        }
        if (b4 == 0 || b4 == 4) {
            listItem = ItemInInventory.getAllItem(COLOR_GREEN, COLOR_MAGENTA);
        } else if (b4 == 2) {
            listItem = ItemInInventory.getAllItemAnimal(COLOR_GREEN, 1);
        } else if (b4 == 3) {
            listItem = ItemInInventory.getAllItemCharExcepColor(COLOR_WHITE);
        } else {
            listItem = ItemInInventory.getAllItem();
        }
        for (int i3 = 0; i3 < listItem.size(); i3++) {
            ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(i3);
            if (itemID == itemInInventory.itemID) {
                itemSelect = itemInInventory;
                return;
            }
        }
    }

    public void setInfoItem(int i) {
        ItemInInventory itemInInventory;
        if (this.selectItemInventory >= listItem.size() || (itemInInventory = (ItemInInventory) listItem.elementAt(this.selectItemInventory)) == null || itemInInventory.itemID != i) {
            return;
        }
        this.arrShowText = Font.normalFont[0].splitFontBStrInLine(itemInInventory.getDescription(false), this.wSh - 10);
    }

    public void doFinishInbue() {
        getListGem(false, true);
        this.isShowPopup = true;
    }

    public void resetSelect() {
        selectGem = null;
        selectGemBH = null;
        selectGemMM = null;
        itemSelect = null;
        getListGem(true, false);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
    }

    @Override
    public void switchToMe(Screen screen) {
        this.last = screen;
        super.switchToMe(screen);
    }
}
