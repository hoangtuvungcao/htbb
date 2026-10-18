package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Char;
import game.model.Command;
import game.model.Cout;
import game.model.Effect;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.ItemInInventory;
import game.model.MainChar;
import game.model.Scroll;
import game.model.ScrollResult;
import game.model.arrow.Arrow;
import game.model.arrow.WeaponsLazer;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import network.Message;

public class ScreenItemTim extends Screen {
    public static boolean BHLock = false;
    public static boolean LKDLock = false;
    public static boolean MMLock = false;
    public static final byte TYPE2 = 0;
    public static final byte TYPE4 = 1;
    public static final byte TYPE_ANIMAL = 2;
    public static final byte TYPE_EP_ITEM_TIM = 6;
    public static final byte TYPE_LOCK_ITEM = 3;
    public static final byte TypeXayBot = 5;
    public static int hCell = 0;
    public static int hImg = 80;
    public static short itemID = 0;
    static ItemInInventory itemSelect = null;
    public static ScreenItemTim me = null;
    public static int wCell = 0;
    public static int wImg = 40;
    public static int xDr;
    public static int yDr;
    boolean DropItem;
    Command cmdClose;
    Command cmdContinue;
    Command cmdDapItemTim;
    Command cmdErr;
    Command cmdView;
    Command cmdWaiting;
    Command cmdXay;
    int cout;
    public boolean createBot;
    public byte currentRow;
    public byte frame;
    public int h0;
    public int hSh;
    public int hclip;
    public int hclip1;
    public int index;
    public int indexTabNguyenLieu;
    public boolean isDap;
    boolean isEp;
    public boolean isShot;
    boolean isShowPopup;
    public boolean isShowText;
    public boolean isXayBot;
    public boolean isdestroyitem;
    public Screen last;
    public int lastIndex1;
    public int lastIndex2;
    public ItemInInventory selecItemNL1;
    public ItemInInventory selecItemNL3;
    public ItemInInventory selecItemNL4;
    public ItemInInventory selecItemNL6;
    int selectItem;
    public int selectItemInventory;
    public byte selectMenuItem;
    public int tick;
    public byte timeDelay;
    public byte type;
    public int wSh;
    int[] xItem;
    int xPopup;
    public int xSh;
    int[] xTo;
    public int xto;
    int[] yItem;
    int yPopup;
    public int ySh;
    int[] yTo;
    public int yto;
    public static Vector vnguyenLieu = new Vector();
    static GemTemplate selectGem = null;
    static GemTemplate selectGemBH = null;
    static GemTemplate selectGemMM = null;
    static GemTemplate selecItemNL2 = null;
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
    public String tile = "Cộng thuộc tính";
    public Vector mShot = new Vector();
    public int size = 22;
    public Vector ListTtem = new Vector();
    public boolean[] CheckHaveItemCell = {false, false, false, false, false, false};
    public boolean[] CheckPaintItem = {false, false, false, false, false, false};
    public Vector LisItemSelect = new Vector();
    public short id_bot = 0;
    public byte typeResult = 0;
    public byte soluong = 1;
    public Vector mEffect = new Vector();
    public Vector lazer = new Vector();
    String infoPoup = "Thong bao";
    Vector mDestroy = new Vector();
    public int timeDlay = 15;
    public int dem = -1;
    public byte currentCell = 5;

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

    public static ScreenItemTim gI() {
        ScreenItemTim screenItemTim = me;
        if (screenItemTim != null) {
            return screenItemTim;
        }
        ScreenItemTim screenItemTim2 = new ScreenItemTim();
        me = screenItemTim2;
        return screenItemTim2;
    }

    public void resetData() {
        this.mScrollItemInventory.clear();
        this.LisItemSelect.removeAllElements();
        this.mScrollNguyenLieu.clear();
        this.mScrollShowText.clear();
        this.mEffect.removeAllElements();
        this.lazer.removeAllElements();
        this.mShot.removeAllElements();
        vnguyenLieu.removeAllElements();
        this.index = GCanvas.isSmartPhone ? -1 : 0;
        this.selectItemInventory = 0;
        this.indexTabNguyenLieu = 0;
        this.isShowPopup = false;
        this.createBot = false;
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
        this.type = b;
        int i = (xDr + (wCell / 2)) - 44;
        int i2 = yDr + (hCell / 2);
        if (GCanvas.isSmartPhone) {
            i = (xDr + (wCell / 4)) - 44;
        }
        if (b == 6) {
            this.currentRow = (byte) 1;
            this.cmdDapItemTim.caption = "Đập";
            this.tile = "Đập đồ tím";
            int i3 = yDr + 27 + ((this.h0 - 28) / 2);
            int i4 = hCell / 4;
            int[] iArr = new int[7];
            this.xItem = iArr;
            int[] iArr2 = new int[7];
            this.yItem = iArr2;
            iArr[0] = i + 34;
            iArr2[0] = (i3 - (this.size / 2)) + 20;
            this.currentRow = (byte) -1;
            int i5 = 30;
            for (int i6 = 1; i6 < 7; i6++) {
                this.xItem[i6] = ((Util.cos(i5) * i4) / 1024) + this.xItem[0];
                this.yItem[i6] = ((Util.sin(i5) * i4) / 1024) + this.yItem[0];
                i5 += 60;
            }
        } else if (b == 5) {
            int i7 = yDr + 27 + ((this.h0 - 28) / 2);
            this.cmdXay.caption = "Nghiền";
            this.tile = "Nghiền Bột";
            this.currentRow = (byte) 1;
            int[] iArr3 = {i, i - 15, i, i + 81, 0};
            this.xItem = iArr3;
            int[] iArr4 = new int[4];
            this.xTo = iArr4;
            int[] iArr5 = new int[4];
            this.yTo = iArr5;
            int[] iArr6 = new int[5];
            this.yItem = iArr6;
            int i8 = i + 33;
            int i9 = this.size;
            int i10 = (i7 - (i9 / 2)) + 3;
            iArr6[0] = i10;
            int i11 = i10 - (i9 * 2);
            int i12 = yDr;
            if (i11 < i12 + 27) {
                iArr6[0] = i12 + 27 + (i9 * 2);
            }
            int i13 = iArr6[0];
            iArr6[2] = i13 - (i9 * 2);
            iArr6[1] = i13 + 28;
            iArr6[3] = i13 + 28;
            iArr4[0] = iArr3[0];
            iArr4[1] = iArr3[1];
            iArr4[2] = iArr3[2];
            iArr4[3] = iArr3[3];
            iArr5[0] = iArr6[0];
            iArr5[1] = iArr6[1];
            iArr5[2] = iArr6[2];
            iArr5[3] = iArr6[3];
            this.xPopup = i + 43;
            this.yPopup = i2 + 5;
            if (GCanvas.isSmallScreen) {
                int[] iArr7 = this.yItem;
                int i14 = iArr7[0];
                iArr7[2] = (i14 - (this.size * 2)) + 4;
                int[] iArr8 = this.xItem;
                iArr8[4] = iArr8[0];
                iArr7[4] = i14 + 40;
            } else {
                int[] iArr9 = this.xItem;
                iArr9[4] = iArr9[0];
                int[] iArr10 = this.yItem;
                iArr10[4] = iArr10[0] + 50;
            }
            this.xto = this.xTo[0];
            this.yto = this.yTo[0];
            this.hclip = this.size;
            this.hclip1 = 0;
        }
        Cout.println("TYPE SCREENDAPDO " + ((int) b));
        this.cout = 0;
        if (b == 5) {
            if (z) {
                this.currentRow = (byte) 0;
                if (GCanvas.isSmartPhone) {
                    this.currentRow = (byte) -1;
                }
                this.left = this.cmdXay;
                this.isShowText = false;
                this.mScrollShowText.clear();
                this.isEp = false;
            } else {
                this.isEp = false;
                this.left = this.cmdXay;
            }
        } else if (b == 6) {
            if (z) {
                this.currentRow = (byte) 0;
                if (GCanvas.isSmartPhone) {
                    this.currentRow = (byte) -1;
                }
                this.left = this.cmdDapItemTim;
                this.isShowText = false;
                this.mScrollShowText.clear();
                this.isEp = false;
            } else {
                this.isEp = false;
                this.left = this.cmdDapItemTim;
            }
        }
        resetSelect();
        getListGem(true, false);
        if (b == 6) {
            addNguyenlieu();
        }
    }

    public ScreenItemTim() {
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
        this.cmdDapItemTim = new Command("Đập", new IAction() {
            @Override
            public void perform() {
                if (ScreenItemTim.this.CheckHaveItemcell()) {
                    if (!ScreenItemTim.this.CheckHaveItemcell() || ScreenItemTim.this.isDap) {
                        return;
                    }
                    ScreenItemTim.this.addlazeEpItemTim();
                    ScreenItemTim.this.isShot = true;
                    ScreenItemTim.this.isDap = true;
                    return;
                }
                GCanvas.startOKDlg("Bạn chưa bỏ nguyên liệu vào ô!");
            }
        });
        this.cmdWaiting = new Command("Xin chờ", new IAction() {
            @Override
            public void perform() {
            }
        });
        this.cmdXay = new Command("Nghiền", new IAction() {
            @Override
            public void perform() {
                if (ScreenItemTim.itemSelect == null) {
                    GCanvas.startOKDlg("Bạn chưa bỏ trang bị vào");
                } else {
                    if (ScreenItemTim.this.isXayBot) {
                        return;
                    }
                    GameService.gI().doRequestTachNguyenLIeu(ScreenItemTim.itemSelect.itemID, 4, 0, 0);
                    GCanvas.startOKDlg("Xin chờ");
                }
            }
        });
        this.cmdClose = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (GCanvas.isPointerClick) {
                    GCanvas.isPointerClick = false;
                }
                if (ScreenItemTim.this.isShowText) {
                    ScreenItemTim.this.isShowText = false;
                    ScreenItemTim screenItemTim = ScreenItemTim.this;
                    screenItemTim.center = screenItemTim.cmdView;
                    ScreenItemTim.this.mScrollShowText.clear();
                    ScreenItemTim.this.arrShowText = new String[]{""};
                    if (ScreenItemTim.this.isEp) {
                        ScreenItemTim screenItemTim2 = ScreenItemTim.this;
                        screenItemTim2.left = screenItemTim2.cmdContinue;
                        return;
                    } else if (ScreenItemTim.this.type == 5) {
                        ScreenItemTim screenItemTim3 = ScreenItemTim.this;
                        screenItemTim3.left = screenItemTim3.cmdXay;
                        return;
                    } else {
                        if (ScreenItemTim.this.type == 6) {
                            ScreenItemTim screenItemTim4 = ScreenItemTim.this;
                            screenItemTim4.left = screenItemTim4.cmdDapItemTim;
                            return;
                        }
                        return;
                    }
                }
                ScreenItemTim.this.resetData();
                ScreenItemTim.this.last.switchToMe();
            }
        });
        boolean z = GCanvas.isSmartPhone;
        this.cmdView = new Command("", new AnonymousClass5());
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

    class AnonymousClass5 implements IAction {
        AnonymousClass5() {
        }

        @Override
        public void perform() {
            Vector vector = new Vector();
            vector.addElement(new Command("Bỏ vào", new IAction() {
                @Override
                public void perform() {
                    if (ScreenItemTim.this.type == 5 && ScreenItemTim.this.isXayBot) {
                        GCanvas.startOKDlg("Không thể chọn trong quá trình luyện");
                        return;
                    }
                    if (ScreenItemTim.this.type == 6 && ScreenItemTim.this.isDap) {
                        GCanvas.startOKDlg("Không thể chọn trong quá trình luyện");
                        return;
                    }
                    if (ScreenItemTim.this.type == 6 && ScreenItemTim.listItem.size() > 0) {
                        if (ScreenItemTim.this.currentCell == 1) {
                            ScreenItemTim.this.selecItemNL1 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL1)) {
                                GCanvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL1 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 2 && ScreenItemTim.selecItemNL2 != null) {
                            ScreenItemTim.this.CheckPaintItem[1] = true;
                        }
                        if (ScreenItemTim.this.currentCell == 3) {
                            ScreenItemTim.this.selecItemNL3 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL3)) {
                                GCanvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL3 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 4) {
                            ScreenItemTim.this.selecItemNL4 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL4)) {
                                GCanvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL4 = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 5) {
                            ScreenItemTim.itemSelect = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.itemSelect)) {
                                GCanvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.itemSelect = null;
                            }
                        }
                        if (ScreenItemTim.this.currentCell == 6) {
                            ScreenItemTim.this.selecItemNL6 = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                            if (ScreenItemTim.this.checKselecitem(ScreenItemTim.this.selecItemNL6)) {
                                GCanvas.startOKDlg("Item Da duoc chon");
                                ScreenItemTim.this.selecItemNL6 = null;
                            }
                        }
                    }
                    if (ScreenItemTim.this.type == 5) {
                        if (ScreenItemTim.this.createBot) {
                            ScreenItemTim.this.createBot = false;
                        }
                        if (ScreenItemTim.this.hclip <= 0) {
                            ScreenItemTim.this.hclip = ScreenItemTim.this.size;
                        }
                        if (ScreenItemTim.this.hclip1 > 0) {
                            ScreenItemTim.this.hclip1 = 0;
                        }
                    }
                    if (GCanvas.isSmartPhone) {
                        ScreenItemTim.this.timeDelay = (byte) 5;
                    }
                    if (ScreenItemTim.this.currentRow != 1 || ScreenItemTim.this.type == 6 || ScreenItemTim.listItem.size() <= 0 || ScreenItemTim.this.selectItemInventory == -1 || ScreenItemTim.this.selectItemInventory > ScreenItemTim.listItem.size() - 1 || ScreenItemTim.this.isDap) {
                        return;
                    }
                    if (ScreenItemTim.this.type == 1 && !ScreenItemTim.isFinishImbue) {
                        GCanvas.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", new IAction() {
                            @Override
                            public void perform() {
                                ScreenItemTim.this.right.action.perform();
                                GCanvas.endDlg();
                            }
                        });
                    } else {
                        ScreenItemTim.itemSelect = (ItemInInventory) ScreenItemTim.listItem.elementAt(ScreenItemTim.this.selectItemInventory);
                    }
                }
            }));
            if (!GCanvas.isSmartPhone) {
                vector.addElement(new Command("Thông tin", new IAction() {
                    @Override
                    public void perform() {
                        if (GCanvas.isSmartPhone) {
                            ScreenItemTim.this.timeDelay = (byte) 5;
                        }
                        if (ScreenItemTim.listItem.size() > 0 && ScreenItemTim.this.currentRow == 1 && ScreenItemTim.this.type != 6) {
                            ScreenItemTim.this.doView();
                        } else {
                            if (ScreenItemTim.listItem.size() <= 0 || ScreenItemTim.this.type != 6) {
                                return;
                            }
                            ScreenItemTim.this.doView();
                        }
                    }
                }));
            }
            GCanvas.menu.startAt(vector, 3);
            GCanvas.currentDialog = null;
        }
    }

    private void paintListItem(Graphics graphics) {
        Res.paintDlgFull(graphics, xDr, yDr, wCell, hCell, 25, this.tile, false, this.h0 - 28);
        GCanvas.resetTrans(graphics);
        if (this.currentCell != 2) {
            this.mScrollItemInventory.setStyle(listItem.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
            graphics.ClipRec(xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
            this.mScrollItemInventory.setClip(graphics, xDr + 2, this.h0 + yDr + 23, wCell - 4, 31);
            for (int i = 0; i < listItem.size(); i++) {
                if (this.selectItemInventory == i && this.currentRow == 1) {
                    graphics.setColor(-10263709);
                    int i2 = xDr;
                    int i3 = this.wTab;
                    graphics.fillRect(i2 + (i3 * i), yDr + 23 + this.h0, i3, 28);
                }
                ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(i);
                int i4 = xDr;
                int i5 = this.wTab;
                itemInInventory.paint(graphics, i4 + (i5 * i) + (i5 / 2), yDr + 36 + this.h0);
                graphics.setColor(-2249714);
                graphics.fillRect(xDr + (this.wTab * i), yDr + 22 + this.h0, 1, 29);
            }
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
        }
        if (selecItemNL2 != null && this.currentCell == 2) {
            graphics.setColor(-10263709);
            graphics.fillRect(xDr + 3, yDr + 23 + this.h0, this.wTab - 3, 28);
            Res.paintGem(graphics, Res.getGemByID(selecItemNL2.id).idImage, xDr + (this.wTab / 2), yDr + 36 + this.h0);
            Font.verySmallFont.drawString(graphics, selecItemNL2.number + "", (this.size / 2) + xDr + (this.wTab / 2), ((yDr + 50) + this.h0) - Font.verySmallFont.getHeight(), 1);
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + this.wTab, yDr + 22 + this.h0, 1, 29);
        }
        GCanvas.resetTrans(graphics);
        graphics.setColor(-1438451);
        int[] iArr = this.xItem;
        byte b = this.currentCell;
        int i6 = iArr[b] - 1;
        int i7 = this.yItem[b] - 11;
        int i8 = this.size;
        graphics.fillRect(i6, i7, i8 + 2, i8 + 2);
    }

    @Override
    public void startXayBot(int i, int i2, int i3) {
        GCanvas.endDlg();
        this.id_bot = (short) i;
        this.typeResult = (byte) i2;
        this.soluong = (byte) i3;
        addlazer();
        this.isXayBot = true;
    }

    private void paintResult(Graphics graphics) {
        int i = this.xItem[4] + 4;
        int i2 = (this.yItem[4] - 8) + this.hclip;
        int i3 = this.size;
        graphics.setClip(i, i2, i3, i3);
        int i4 = this.xItem[4] + 4;
        int i5 = (this.yItem[4] - 8) + this.hclip;
        int i6 = this.size;
        graphics.ClipRec(i4, i5, i6, i6);
        if (this.typeResult == 0) {
            Res.paintPotion(graphics, MainChar.listPotion[this.id_bot].index, this.xItem[4] + 4, this.yItem[4] - 6, 0);
        } else {
            GameData.paintIcon(graphics, (short) (this.id_bot + 6500), this.xItem[4] + 12, this.yItem[4] + 2, 3);
            Font.normalFont[3].drawString(graphics, ((int) this.soluong) + "", this.xItem[4] + 18, this.yItem[4] + 2, 1);
        }
        graphics.restoreCanvas();
    }

    private void addEffectBot() {
        int i = Arrow.ARROWSIZE[0][10] / 2;
        for (int i2 = 0; i2 < 2; i2++) {
            int i3 = (this.xItem[2] + i) - 4;
            int i4 = this.yto;
            int i5 = this.size;
            this.mEffect.addElement(new Effect_XayBot(i3, (i4 + (i5 / 2)) - 10, i2 * 2, (this.yItem[4] + (i5 / 2)) - 10, 0));
        }
    }

    private void updatelazer() {
        int i = Arrow.ARROWSIZE[0][10] / 2;
        for (int i2 = 0; i2 < this.lazer.size(); i2++) {
            WeaponsLazer weaponsLazer = (WeaponsLazer) this.lazer.elementAt(i2);
            if (weaponsLazer != null && this.type == 5) {
                if (this.hclip > 4) {
                    addEffectBot();
                }
                if (GCanvas.gameTick % 2 == 0) {
                    this.mEffect.addElement(new Effect((this.xItem[2] + i) - 4, (this.yto + (this.size / 2)) - 10, 54));
                }
            }
            weaponsLazer.update2();
            if (GCanvas.gameTick % 2 == 0) {
                this.mEffect.addElement(new Effect(weaponsLazer.xlim, weaponsLazer.ylim, 54));
            }
            if (weaponsLazer.wantDestroy) {
                this.lazer.removeElement(weaponsLazer);
                this.isdestroyitem = true;
                this.isShot = true;
            }
        }
    }

    private void paintlazer(Graphics graphics) {
        for (int i = 0; i < this.lazer.size(); i++) {
            ((WeaponsLazer) this.lazer.elementAt(i)).paint(graphics);
        }
    }

    public void addlazer() {
        int i = Arrow.ARROWSIZE[0][10] / 2;
        this.center = this.cmdWaiting;
        this.cmdXay.caption = "";
        this.mEffect.addElement(new Effect((this.xItem[2] + i) - 4, (this.yto + (this.size / 2)) - 10, 54));
        for (int i2 = 0; i2 < this.xItem.length; i2++) {
            if (i2 % 4 != 0) {
                int[] iArr = this.xItem;
                this.lazer.addElement(new WeaponsLazer((iArr[i2] + i) - 4, (this.yItem[i2] + i) - 10, (iArr[2] + i) - 4, (this.yto + (this.size / 2)) - 10, 40, 2));
            }
        }
    }

    private void updateEffect() {
        for (int i = 0; i < this.mEffect.size(); i++) {
            Effect effect2 = (Effect) this.mEffect.elementAt(i);
            if (effect2 != null) {
                effect2.update();
            }
            if (effect2.wantDetroy && effect2.type == -1) {
                this.createBot = true;
                this.mEffect.removeElement(effect2);
            }
            if (effect2.wantDetroy) {
                this.mEffect.removeElement(effect2);
            }
        }
    }

    private void paintEffect(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        for (int i = 0; i < this.mEffect.size(); i++) {
            ((Effect) this.mEffect.elementAt(i)).paint(graphics);
        }
    }

    protected void doView() {
        int i;
        int i2;
        if (this.type == 6) {
            if (Char.inventory.size() == 0 || (i2 = this.selectItemInventory) < 0 || i2 > listItem.size() - 1) {
                return;
            }
            if (this.currentCell == 2) {
                GemTemplate gemTemplate = selecItemNL2;
                if (gemTemplate == null) {
                    return;
                }
                GemTemp gemByID = Res.getGemByID(gemTemplate.id);
                this.arrShowText = Font.normalFont[0].splitFontBStrInLine(ItemInInventory.setTemp(gemByID.name, "0") + ItemInInventory.setTemp(gemByID.decript, "0"), this.wSh - 10);
            }
            if (this.currentCell != 2) {
                ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
                if (!itemInInventory.isEnoughData) {
                    itemInInventory.isEnoughData = true;
                    GameService.gI().requestItemInfo(itemInInventory.itemID, GCanvas.gameScr.mainChar.ID);
                }
                this.arrShowText = Font.normalFont[0].splitFontBStrInLine(itemInInventory.getDescription(false), this.wSh - 10);
            }
        }
        if (this.type == 5) {
            if (Char.inventory.size() == 0 || (i = this.selectItemInventory) < 0 || i > listItem.size() - 1) {
                return;
            }
            ItemInInventory itemInInventory2 = (ItemInInventory) listItem.elementAt(this.selectItemInventory);
            if (!itemInInventory2.isEnoughData) {
                itemInInventory2.isEnoughData = true;
                GameService.gI().requestItemInfo(itemInInventory2.itemID, GCanvas.gameScr.mainChar.ID);
            }
            this.arrShowText = Font.normalFont[0].splitFontBStrInLine(itemInInventory2.getDescription(false), this.wSh - 10);
        }
        if (!GCanvas.isSmartPhone && listItem.size() > 0) {
            String[] strArr = this.arrShowText;
            if (strArr.length == 1) {
                this.hSh = 35;
                this.ySh = (GCanvas.h / 2) - (Font.normalFont[0].getHeight() / 2);
            } else {
                this.hSh = (strArr.length * ITEM_HEIGHT) + 8;
                this.ySh = (GCanvas.h / 2) - ((this.arrShowText.length * (ITEM_HEIGHT - 3)) / 2);
            }
            int i3 = this.ySh;
            int i4 = yDr;
            if (i3 < i4) {
                this.ySh = i4;
            }
            if (this.hSh > 150) {
                this.hSh = 150;
            }
            this.xSh = xDr;
        }
        if (GCanvas.isSmartPhone) {
            return;
        }
        this.isShowText = true;
        this.center = this.cmdErr;
        this.left = this.cmdErr;
    }

    public void paintCell(Graphics graphics) {
        graphics.setColor(-16751521);
        int i = GCanvas.isSmartPhone ? -27 : 10;
        graphics.fillRect(this.xSh + 2, this.ySh + i, this.wSh, this.hSh - 8);
        graphics.setColor(-2496);
        graphics.drawRect(this.xSh + 2, this.ySh + i, this.wSh, this.hSh - 8);
    }

    private void addNguyenlieu() {
        for (int i = 0; i < vnguyenLieu.size(); i++) {
        }
    }

    public void paintShowText(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (wCell >= 300) {
            paintCell(graphics);
            if (!this.isShowText && !GCanvas.isSmartPhone) {
                return;
            }
        } else if (!this.isShowText && !GCanvas.isSmartPhone) {
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

    private void paintScreenXayBot(Graphics graphics) {
        Res.paintDlgFull(graphics, xDr, yDr, wCell, hCell, 25, this.tile, false, this.h0 - 28);
        GCanvas.resetTrans(graphics);
        this.mScrollItemInventory.setStyle(listItem.size(), this.wTab, xDr, yDr + 28 + this.h0, wCell - 2, 27, false, 0);
        graphics.ClipRec(xDr + 2, yDr + 23 + this.h0, wCell - 4, 31);
        this.mScrollItemInventory.setClip(graphics, xDr + 2, this.h0 + yDr + 23, wCell - 4, 31);
        for (int i = 0; i < listItem.size(); i++) {
            if (this.selectItemInventory == i && this.currentRow == 1) {
                graphics.setColor(-10263709);
                int i2 = xDr;
                int i3 = this.wTab;
                graphics.fillRect(i2 + (i3 * i), yDr + 23 + this.h0, i3, 28);
            }
            ItemInInventory itemInInventory = (ItemInInventory) listItem.elementAt(i);
            int i4 = xDr;
            int i5 = this.wTab;
            itemInInventory.paint(graphics, i4 + (i5 * i) + (i5 / 2), yDr + 36 + this.h0);
            graphics.setColor(-2249714);
            graphics.fillRect(xDr + (this.wTab * i), yDr + 22 + this.h0, 1, 29);
            ItemInInventory itemInInventory2 = itemSelect;
            if (itemInInventory2 != null && itemInInventory2.itemID == itemInInventory.itemID) {
                graphics.setColor(-13508609);
                int i6 = xDr;
                int i7 = this.wTab;
                graphics.drawRect(i6 + (i7 * i) + 1, yDr + 23 + this.h0, i7 - 2, 28);
                int i8 = xDr;
                int i9 = this.wTab;
                graphics.drawRect(i8 + (i9 * i) + 2, yDr + 24 + this.h0, i9 - 4, 26);
            }
        }
        graphics.setColor(-2249714);
        graphics.fillRect(xDr + (this.wTab * listItem.size()), yDr + 22 + this.h0, 1, 29);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        Screen screen = this.last;
        if (screen != null) {
            screen.paint(graphics);
        }
        byte b = this.type;
        if (b == 5) {
            paintScreenXayBot(graphics);
        } else if (b == 6) {
            paintListItem(graphics);
        }
        GCanvas.resetTrans(graphics);
        paintItemEp(graphics);
        paintlazer(graphics);
        paintEffect(graphics);
        if (this.type == 5) {
            paintlazer(graphics);
            paintResult(graphics);
        }
        for (int i = 0; i < this.mShot.size(); i++) {
            ((Effect) this.mShot.elementAt(i)).paint(graphics);
        }
        paintShowText(graphics);
        super.paint(graphics);
    }

    public boolean CheckHaveItemcell() {
        int i = 0;
        while (true) {
            boolean[] zArr = this.CheckHaveItemCell;
            if (i >= zArr.length) {
                return true;
            }
            if (!zArr[i]) {
                return false;
            }
            i++;
        }
    }

    public void paintItemEp(Graphics graphics) {
        if (this.type == 5) {
            for (int i = 0; i < this.xItem.length; i++) {
                if (i % 4 == 0) {
                    graphics.setColor(-1);
                    graphics.fillRect(this.xItem[i], this.yItem[i] - 10, this.size, this.size);
                    graphics.setColor(-10263709);
                    graphics.fillRect(this.xItem[i] + 1, this.yItem[i] - 9, this.size - 2, this.size - 2);
                } else {
                    graphics.drawRegion(Res.getImgArrow(10), 0, this.frame * Arrow.ARROWSIZE[1][10], Arrow.ARROWSIZE[0][10], Arrow.ARROWSIZE[1][10], 0, this.xItem[i] - 3, this.yItem[i] - 10, 0);
                }
            }
        } else if (this.type == 6) {
            for (int i = 0; i < this.xItem.length; i++) {
                graphics.setColor(-1);
                graphics.fillRect(this.xItem[i], this.yItem[i] - 10, this.size, this.size);
                graphics.setColor(-10263709);
                graphics.fillRect(this.xItem[i] + 1, this.yItem[i] - 9, this.size - 2, this.size - 2);
            }
        }
        byte b = this.type;
        if (b != 1) {
            ItemInInventory item = itemSelect;
            if (item != null && b != 5 && b != 6) {
                item.paint(graphics, this.xItem[0] + 10, this.yItem[0]);
            }
            if (this.type == 6) {
                ItemInInventory item0 = itemSelect;
                if (item0 != null) {
                    item0.paint(graphics, this.xItem[5] + (this.size / 2), this.yItem[5] + 1);
                    this.CheckHaveItemCell[5] = true;
                }
                ItemInInventory item1 = this.selecItemNL1;
                if (item1 != null) {
                    item1.paint(graphics, this.xItem[1] + (this.size / 2), this.yItem[1] + 1);
                    this.CheckHaveItemCell[0] = true;
                }
                if (this.CheckPaintItem[1]) {
                    Res.paintGem(graphics, Res.getGemByID(selecItemNL2.id).idImage, this.xItem[2] + (this.size / 2), this.yItem[2]);
                    this.CheckHaveItemCell[1] = true;
                }
                ItemInInventory item3 = this.selecItemNL3;
                if (item3 != null) {
                    item3.paint(graphics, this.xItem[3] + (this.size / 2), this.yItem[3] + 1);
                    this.CheckHaveItemCell[2] = true;
                }
                ItemInInventory item4 = this.selecItemNL4;
                if (item4 != null) {
                    item4.paint(graphics, this.xItem[4] + (this.size / 2), this.yItem[4] + 1);
                    this.CheckHaveItemCell[3] = true;
                }
                ItemInInventory item6 = this.selecItemNL6;
                if (item6 != null) {
                    item6.paint(graphics, this.xItem[6] + (this.size / 2), this.yItem[6] + 1);
                    this.CheckHaveItemCell[4] = true;
                }
            }
            if (itemSelect != null && this.type == 5) {
                GCanvas.resetTrans(graphics);
                graphics.setClip(this.xItem[0], ((this.yItem[0] - (this.size / 4)) + this.hclip1) - 5, this.size, this.size);
                graphics.ClipRec(this.xItem[0], ((this.yItem[0] - (this.size / 4)) + this.hclip1) - 5, this.size, this.size);
                itemSelect.paint(graphics, this.xItem[0] + 11, this.yItem[0] + 1);
                graphics.restoreCanvas();
            }
        }
        if (this.type == 0 || !this.isShowPopup || this.infoPoup.equals("")) {
            return;
        }
        int y = yDr + this.h0;
        graphics.setColor(-1);
        graphics.fillRect(this.xPopup - 60, y - 27, 120, 20);
        graphics.setColor(-13554904);
        graphics.fillRect(this.xPopup - 59, y - 26, 118, 18);
        Font.normalFont[0].drawString(graphics, this.infoPoup, this.xPopup, y - 24, 2);
        paintlazer(graphics);
    }

    private void updateResult() {
        int i;
        if (this.createBot && GCanvas.gameTick % 2 == 0 && (i = this.hclip) > 0) {
            this.hclip = i - 1;
            this.hclip1++;
        }
    }

    @Override
    public void update() {
        boolean z;
        boolean z2;
        int i;
        if (this.right != null) {
            System.out.println("khong null------------");
        }
        updateResult();
        updatelazer();
        updateEffect();
        if (GCanvas.isSmartPhone && listItem.size() > 0) {
            doView();
        }
        if (this.type == 6 && this.isDap) {
            this.center = this.cmdWaiting;
            this.cmdDapItemTim.caption = "";
            int i2 = this.timeDlay;
            if (i2 >= 0) {
                this.timeDlay = i2 - 1;
            }
            if (this.timeDlay < 0 && this.lazer.size() <= 0) {
                this.timeDlay = 15;
                resetCheck();
                this.isDap = false;
            }
        }
        int i3 = this.dem;
        if (i3 >= 0) {
            this.dem = i3 - 1;
        }
        if (this.dem < 0 && this.isShot && GCanvas.gameTick % 15 == 0 && this.tick < 3 && this.type == 6) {
            for (int i4 = 1; i4 < this.xItem.length; i4++) {
                int[] iArr = this.xItem;
                int i5 = iArr[i4];
                int i6 = this.size;
                int[] iArr2 = this.yItem;
                this.mShot.addElement(new EffectDapDoTim(i5 + (i6 / 2), iArr2[i4], iArr[0] + (i6 / 2), iArr2[0]));
            }
            this.tick++;
        }
        int i7 = 0;
        while (true) {
            if (i7 >= this.mShot.size()) {
                break;
            }
            Effect effect2 = (Effect) this.mShot.elementAt(i7);
            effect2.update();
            if (effect2.wantDetroy) {
                int i8 = 0;
                for (int i9 = 10; i8 < i9; i9 = 10) {
                    int i10 = (this.size / 2) + this.xItem[0];
                    int i11 = this.yItem[0];
                    this.mEffect.addElement(new Effect_XayBot(i10, i11, 0, i11 + 80, -1));
                    i8++;
                }
                this.mEffect.addElement(new Effect(this.xItem[0] + (this.size / 2), this.yItem[0], 50));
                this.mDestroy.addElement(effect2);
            }
            i7++;
        }
        for (int i12 = 0; i12 < this.mDestroy.size(); i12++) {
            this.mShot.removeElement(this.mDestroy.elementAt(i12));
        }
        this.mDestroy.removeAllElements();
        byte b = this.type;
        if (b == 5 || b == 6) {
            this.currentRow = (byte) 1;
        }
        byte b2 = (byte) (this.frame + 1);
        this.frame = b2;
        if (b2 >= 3) {
            this.frame = (byte) 0;
        }
        if (this.isdestroyitem && this.mEffect.size() <= 0 && this.type == 5) {
            int i13 = this.timeDlay;
            if (i13 >= 0) {
                this.timeDlay = i13 - 1;
            }
            if (this.timeDlay < 0) {
                itemSelect = null;
                this.isdestroyitem = false;
                this.isXayBot = false;
                this.timeDlay = 15;
                this.cmdXay.caption = "Nghiền";
                this.center = this.cmdView;
                resetSelect();
            }
        }
        Screen screen = this.last;
        if (screen != null) {
            screen.update();
        }
        byte b3 = this.timeDelay;
        if (b3 > 0) {
            this.timeDelay = (byte) (b3 - 1);
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
                        int i14 = this.selectItemInventory;
                        if (i14 > -1 && i14 <= listItem.size() - 1) {
                            int i15 = this.lastIndex1;
                            if (i15 != -1) {
                                this.selectItemInventory = i15;
                            }
                            this.cmdView.action.perform();
                        }
                    } else if (this.mScrollNguyenLieu.FocusNew && z2 && (i = this.indexTabNguyenLieu) > -1 && i <= vnguyenLieu.size() - 1) {
                        int i16 = this.lastIndex2;
                        if (i16 != -1) {
                            this.indexTabNguyenLieu = i16;
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
                    int i17 = scrollResultUpdateKey3.selected;
                    this.selectItemInventory = i17;
                    this.mScrollItemInventory.moveTo(i17 * (this.wTab + 2));
                }
                this.mScrollItemInventory.updatecm();
                ScrollResult scrollResultUpdateKey4 = this.mScrollNguyenLieu.updateKey();
                if (scrollResultUpdateKey4.isDowning || scrollResultUpdateKey4.isFinish) {
                    int i18 = scrollResultUpdateKey4.selected;
                    this.indexTabNguyenLieu = i18;
                    this.mScrollNguyenLieu.moveTo(i18 * (this.wTab + 2));
                }
                this.mScrollNguyenLieu.updatecm();
            } else {
                this.mScrollShowText.updateKey();
                this.mScrollShowText.updatecm();
            }
        }
        if (GCanvas.isPointerDownItem && this.type == 6) {
            for (int i19 = 1; i19 < this.xItem.length; i19++) {
                if (GCanvas.px >= this.xItem[i19] && GCanvas.px <= this.xItem[i19] + this.size) {
                    if (GCanvas.py >= this.yItem[i19] - 10 && GCanvas.py <= (this.yItem[i19] - 10) + this.size) {
                        this.currentCell = (byte) i19;
                        this.selectItemInventory = 0;
                        this.mScrollItemInventory.moveTo(this.wTab);
                    }
                }
            }
            GCanvas.isPointerDownItem = false;
        }
        super.update();
    }

    public boolean checKselecitem(ItemInInventory itemInInventory) {
        ItemInInventory itemInInventory2;
        ItemInInventory itemInInventory3;
        ItemInInventory itemInInventory4;
        ItemInInventory itemInInventory5;
        ItemInInventory itemInInventory6;
        ItemInInventory itemInInventory7;
        ItemInInventory itemInInventory8;
        ItemInInventory itemInInventory9;
        ItemInInventory itemInInventory10;
        ItemInInventory itemInInventory11;
        ItemInInventory itemInInventory12;
        ItemInInventory itemInInventory13;
        ItemInInventory itemInInventory14;
        ItemInInventory itemInInventory15;
        ItemInInventory itemInInventory16;
        byte b = this.currentCell;
        if (b == 1) {
            ItemInInventory itemInInventory17 = itemSelect;
            return (itemInInventory17 != null && itemInInventory17.itemID == itemInInventory.itemID) || ((itemInInventory2 = this.selecItemNL3) != null && itemInInventory2.itemID == itemInInventory.itemID) || (((itemInInventory3 = this.selecItemNL4) != null && itemInInventory3.itemID == itemInInventory.itemID) || ((itemInInventory4 = this.selecItemNL6) != null && itemInInventory4.itemID == itemInInventory.itemID));
        }
        if (b == 3) {
            ItemInInventory itemInInventory18 = itemSelect;
            return (itemInInventory18 != null && itemInInventory18.itemID == itemInInventory.itemID) || ((itemInInventory5 = this.selecItemNL1) != null && itemInInventory5.itemID == itemInInventory.itemID) || (((itemInInventory6 = this.selecItemNL4) != null && itemInInventory6.itemID == itemInInventory.itemID) || ((itemInInventory7 = this.selecItemNL6) != null && itemInInventory7.itemID == itemInInventory.itemID));
        }
        if (b == 4) {
            ItemInInventory itemInInventory19 = itemSelect;
            return (itemInInventory19 != null && itemInInventory19.itemID == itemInInventory.itemID) || ((itemInInventory8 = this.selecItemNL3) != null && itemInInventory8.itemID == itemInInventory.itemID) || (((itemInInventory9 = this.selecItemNL1) != null && itemInInventory9.itemID == itemInInventory.itemID) || ((itemInInventory10 = this.selecItemNL6) != null && itemInInventory10.itemID == itemInInventory.itemID));
        }
        if (b == 5) {
            ItemInInventory itemInInventory20 = this.selecItemNL1;
            return (itemInInventory20 != null && itemInInventory20.itemID == itemInInventory.itemID) || ((itemInInventory11 = this.selecItemNL3) != null && itemInInventory11.itemID == itemInInventory.itemID) || (((itemInInventory12 = this.selecItemNL4) != null && itemInInventory12.itemID == itemInInventory.itemID) || ((itemInInventory13 = this.selecItemNL6) != null && itemInInventory13.itemID == itemInInventory.itemID));
        }
        if (b != 6) {
            return false;
        }
        ItemInInventory itemInInventory21 = itemSelect;
        return (itemInInventory21 != null && itemInInventory21.itemID == itemInInventory.itemID) || ((itemInInventory14 = this.selecItemNL3) != null && itemInInventory14.itemID == itemInInventory.itemID) || (((itemInInventory15 = this.selecItemNL4) != null && itemInInventory15.itemID == itemInInventory.itemID) || ((itemInInventory16 = this.selecItemNL1) != null && itemInInventory16.itemID == itemInInventory.itemID));
    }

    public static boolean isPointer(int i, int i2, int i3, int i4) {
        if (GCanvas.isPointerDown) {
            return GCanvas.px < i || GCanvas.px > i + i3 || GCanvas.py < i2 || GCanvas.py > i2 + i4;
        }
        return false;
    }

    public void addlazeEpItemTim() {
        int[] iArr = this.xItem;
        int i = iArr[6];
        int i2 = this.size;
        int[] iArr2 = this.yItem;
        this.lazer.addElement(new WeaponsLazer(i + (i2 / 2), iArr2[6], iArr[1] + (i2 / 2), iArr2[1], 100, 3, 3));
        int i3 = 1;
        while (i3 < this.xItem.length - 1) {
            int[] iArr3 = this.xItem;
            int i4 = iArr3[i3];
            int i5 = this.size;
            int i6 = i4 + (i5 / 2);
            int[] iArr4 = this.yItem;
            int i7 = i3 + 1;
            int i8 = (i3 * 2) + 6;
            this.lazer.addElement(new WeaponsLazer(i6, iArr4[i3], iArr3[i7] + (i5 / 2), iArr4[i7], 100, 3, i8));
            this.dem = i8;
            i3 = i7;
        }
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (GCanvas.keyPressed[2] && this.type != 5) {
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
            if (GCanvas.keyPressed[2] && this.type != 5) {
                GCanvas.keyPressed[2] = false;
                this.currentCell = (byte) (this.currentCell - 1);
                ItemInInventory itemInInventory = this.selecItemNL1;
                if (itemInInventory != null) {
                    this.LisItemSelect.addElement(itemInInventory);
                }
                ItemInInventory itemInInventory2 = this.selecItemNL3;
                if (itemInInventory2 != null) {
                    this.LisItemSelect.addElement(itemInInventory2);
                }
                ItemInInventory itemInInventory3 = this.selecItemNL4;
                if (itemInInventory3 != null) {
                    this.LisItemSelect.addElement(itemInInventory3);
                }
                ItemInInventory itemInInventory4 = itemSelect;
                if (itemInInventory4 != null) {
                    this.LisItemSelect.addElement(itemInInventory4);
                }
                ItemInInventory itemInInventory5 = this.selecItemNL6;
                if (itemInInventory5 != null) {
                    this.LisItemSelect.addElement(itemInInventory5);
                }
                this.DropItem = false;
                if (this.currentCell < 1) {
                    this.currentCell = (byte) 6;
                }
                if (this.type == 6) {
                    this.selectItemInventory = 0;
                    this.mScrollItemInventory.moveTo(this.wTab);
                }
            } else if (GCanvas.keyPressed[8]) {
                GCanvas.keyPressed[8] = false;
                this.DropItem = false;
                ItemInInventory itemInInventory6 = this.selecItemNL1;
                if (itemInInventory6 != null) {
                    this.LisItemSelect.addElement(itemInInventory6);
                }
                ItemInInventory itemInInventory7 = this.selecItemNL3;
                if (itemInInventory7 != null) {
                    this.LisItemSelect.addElement(itemInInventory7);
                }
                ItemInInventory itemInInventory8 = this.selecItemNL4;
                if (itemInInventory8 != null) {
                    this.LisItemSelect.addElement(itemInInventory8);
                }
                ItemInInventory itemInInventory9 = itemSelect;
                if (itemInInventory9 != null) {
                    this.LisItemSelect.addElement(itemInInventory9);
                }
                ItemInInventory itemInInventory10 = this.selecItemNL6;
                if (itemInInventory10 != null) {
                    this.LisItemSelect.addElement(itemInInventory10);
                }
                byte b = (byte) (this.currentCell + 1);
                this.currentCell = b;
                if (b > 6) {
                    this.currentCell = (byte) 1;
                }
                if (this.type == 6) {
                    this.selectItemInventory = 0;
                    this.mScrollItemInventory.moveTo(this.wTab);
                }
            }
            if (GCanvas.keyPressed[4]) {
                GCanvas.keyPressed[4] = false;
                int i = this.selectItemInventory - 1;
                this.selectItemInventory = i;
                if (i < 0) {
                    this.selectItemInventory = listItem.size() - 1;
                }
                this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
            } else if (GCanvas.keyPressed[6]) {
                GCanvas.keyPressed[6] = false;
                int i2 = this.selectItemInventory + 1;
                this.selectItemInventory = i2;
                if (i2 > listItem.size() - 1) {
                    this.selectItemInventory = 0;
                }
                this.mScrollItemInventory.moveTo(this.selectItemInventory * this.wTab);
            }
        }
        super.updateKey();
    }

    public void getListGem(boolean z, boolean z2) {
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
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        for (int i = 0; i < size; i++) {
            GemTemplate gemTemplate5 = (GemTemplate) vector.elementAt(i);
            GemTemp gemByID = Res.getGemByID(gemTemplate5.id);
            byte b = this.type;
            if (b == 0) {
                if (gemByID.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    GemTemplate gemTemplate6 = selectGem;
                    if (gemTemplate6 != null && gemTemplate6.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                        z3 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (b == 2) {
                if (gemByID.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    GemTemplate gemTemplate7 = selectGem;
                    if (gemTemplate7 != null && gemTemplate7.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (b == 3) {
                if (gemByID.type == GemTemp.TYPE_LOCK_ITEM) {
                    GemTemplate gemTemplate8 = selectGem;
                    if (gemTemplate8 != null && gemTemplate8.id == gemTemplate5.id && !selectGem.lock) {
                        selectGem = gemTemplate5;
                    }
                    vnguyenLieu.addElement(gemTemplate5);
                }
            } else if (gemByID.type == GemTemp.TYPE_IMBUE) {
                GemTemplate gemTemplate9 = selectGem;
                if (gemTemplate9 != null && gemTemplate9.id == gemTemplate5.id && !selectGem.lock) {
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
                            if (this.type == 6) {
                                selecItemNL2 = gemTemplate5;
                            }
                            vnguyenLieu.addElement(gemTemplate5);
                        }
                        z5 = true;
                        if (this.type == 6) {
                            selecItemNL2 = gemTemplate5;
                        }
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
                    if (this.type == 6) {
                        selecItemNL2 = gemTemplate5;
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
                if (this.type == 6) {
                    selecItemNL2 = gemTemplate5;
                }
                vnguyenLieu.addElement(gemTemplate5);
            }
        }
        Vector vector2 = MainChar.gemItemKhoa;
        int size2 = vector2.size();
        boolean z6 = false;
        boolean z7 = false;
        boolean z8 = false;
        for (int i2 = 0; i2 < size2; i2++) {
            GemTemplate gemTemplate10 = (GemTemplate) vector2.elementAt(i2);
            GemTemp gemByID2 = Res.getGemByID(gemTemplate10.id);
            byte b2 = this.type;
            if (b2 == 0) {
                if (gemByID2.type == GemTemp.TYPE_ADD_NEW_ATTRIBUTE) {
                    GemTemplate gemTemplate11 = selectGem;
                    if (gemTemplate11 != null && gemTemplate11.id == gemTemplate10.id && selectGem.lock) {
                        selectGem = gemTemplate10;
                        z6 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate10);
                    gemTemplate10.lock = true;
                }
            } else if (b2 == 2) {
                if (gemByID2.type == GemTemp.TYPE_LOCK_ANIMAL) {
                    GemTemplate gemTemplate12 = selectGem;
                    if (gemTemplate12 != null && gemTemplate12.id == gemTemplate10.id && selectGem.lock) {
                        selectGem = gemTemplate10;
                    }
                    vnguyenLieu.addElement(gemTemplate10);
                    gemTemplate10.lock = true;
                }
            } else if (b2 == 3) {
                if (gemByID2.type == GemTemp.TYPE_LOCK_ITEM) {
                    GemTemplate gemTemplate13 = selectGem;
                    if (gemTemplate13 != null && gemTemplate13.id == gemTemplate10.id && selectGem.lock) {
                        selectGem = gemTemplate10;
                    }
                    vnguyenLieu.addElement(gemTemplate10);
                    gemTemplate10.lock = true;
                }
            } else if (gemByID2.type == GemTemp.TYPE_IMBUE) {
                GemTemplate gemTemplate14 = selectGem;
                if (gemTemplate14 != null && gemTemplate14.id == gemTemplate10.id && selectGem.lock) {
                    selectGem = gemTemplate10;
                } else {
                    if (selectGem == null && z2 && idLkd == gemTemplate10.id && LKDLock) {
                        selectGem = gemTemplate10;
                    }
                    gemTemplate = selectGemBH;
                    if (gemTemplate == null && gemTemplate.id == gemTemplate10.id && selectGemBH.lock) {
                        selectGemBH = gemTemplate10;
                    } else {
                        if (selectGemBH == null && z2 && idBH == gemTemplate10.id && BHLock) {
                            selectGemBH = gemTemplate10;
                        }
                        gemTemplate2 = selectGemMM;
                        if (gemTemplate2 == null && gemTemplate2.id == gemTemplate10.id && selectGemMM.lock) {
                            selectGemMM = gemTemplate10;
                        } else {
                            if (selectGemMM == null && z2 && idMM == gemTemplate10.id && MMLock) {
                                selectGemMM = gemTemplate10;
                            }
                            vnguyenLieu.addElement(gemTemplate10);
                            gemTemplate10.lock = true;
                        }
                        z8 = true;
                        vnguyenLieu.addElement(gemTemplate10);
                        gemTemplate10.lock = true;
                    }
                    z7 = true;
                    gemTemplate2 = selectGemMM;
                    if (gemTemplate2 == null) {
                        if (selectGemMM == null) {
                            selectGemMM = gemTemplate10;
                            z8 = true;
                        }
                    } else if (selectGemMM == null) {
                        selectGemMM = gemTemplate10;
                        z8 = true;
                    }
                    vnguyenLieu.addElement(gemTemplate10);
                    gemTemplate10.lock = true;
                }
                z6 = true;
                gemTemplate = selectGemBH;
                if (gemTemplate == null) {
                    if (selectGemBH == null) {
                        selectGemBH = gemTemplate10;
                        z7 = true;
                    }
                } else if (selectGemBH == null) {
                    selectGemBH = gemTemplate10;
                    z7 = true;
                }
                gemTemplate2 = selectGemMM;
                if (gemTemplate2 == null) {
                    if (selectGemMM == null) {
                        selectGemMM = gemTemplate10;
                        z8 = true;
                    }
                } else if (selectGemMM == null) {
                    selectGemMM = gemTemplate10;
                    z8 = true;
                }
                vnguyenLieu.addElement(gemTemplate10);
                gemTemplate10.lock = true;
            }
        }
        byte b3 = this.type;
        if (b3 == 0) {
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
        if (b3 == 0) {
            listItem = ItemInInventory.getAllItem(COLOR_GREEN);
        } else if (b3 == 2) {
            listItem = ItemInInventory.getAllItemAnimal(COLOR_GREEN, 1);
        } else if (b3 == 3) {
            listItem = ItemInInventory.getAllItemCharExcepColor(COLOR_WHITE);
        } else {
            listItem = ItemInInventory.getAllItemCharDrop();
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

    private void resetCheck() {
        selectGem = null;
        this.cmdDapItemTim.caption = "Đập";
        this.center = this.cmdView;
        this.isShot = false;
        this.tick = 0;
        this.selecItemNL1 = null;
        selecItemNL2 = null;
        this.selecItemNL3 = null;
        this.selecItemNL4 = null;
        this.selecItemNL6 = null;
        this.LisItemSelect.removeAllElements();
        int i = 0;
        while (true) {
            boolean[] zArr = this.CheckHaveItemCell;
            if (i < zArr.length) {
                zArr[i] = false;
                this.CheckPaintItem[i] = false;
                i++;
            } else {
                itemSelect = null;
                getListGem(true, false);
                return;
            }
        }
    }

    public void resetSelect() {
        if (this.type == 5) {
            this.center = this.cmdView;
        }
        this.isShot = false;
        this.tick = 0;
        this.isDap = false;
        resetCheck();
        this.currentCell = (byte) 5;
        this.isXayBot = false;
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
