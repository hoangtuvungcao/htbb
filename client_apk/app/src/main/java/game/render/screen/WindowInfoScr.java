package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Actor;
import game.model.AnimalInfo;
import game.model.Char;
import game.model.ChatInfo;
import game.model.Command;
import game.model.Cout;
import game.model.FrameImage;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.ItemInInventory;
import game.model.ItemQuest;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.model.Mineral;
import game.model.PartyInfo;
import game.model.Potion;
import game.model.RealID;
import game.model.skill.InfoSkillLearn;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.lang.reflect.Array;
import java.util.Vector;

public class WindowInfoScr extends Screen {
    public static final short MAX_POTION = 999;
    public static Char charWearing = null;
    public static int cmdx = 0;
    public static int cmdy = 0;
    public static int cmdyText = 0;
    public static int cmtoX = 0;
    public static int cmtoXName = 0;
    public static int cmtoY = 0;
    public static int cmtoYText = 0;
    public static int cmvx = 0;
    public static int cmvy = 0;
    public static int cmvyText = 0;
    public static int cmx = 0;
    public static int cmxLim = 0;
    public static int cmxname = 0;
    public static int cmy = 0;
    public static int cmyLim = 0;
    public static int cmyLimText = 0;
    public static int cmyText = 0;
    public static byte countBL = 0;
    public static byte countBR = 0;
    public static byte countL = 0;
    public static byte countR = 0;
    public static int dir = 1;
    public static int focusTab = 0;
    public static byte idColor = 0;
    public static byte idMonsterClanQuest = -1;
    public static short idTemp = 0;
    public static byte[] index = null;
    public static String[] infoMainCharServer = null;
    public static boolean isKham = false;
    public static boolean isWindowInfo;
    public static byte khoaCheDo;
    public static byte khoaDoThu;
    public static byte khoaImbue;
    public static byte khoaKham;
    public static byte lvThu;
    public static byte magicAnimal;
    public static WindowInfoScr me;
    public static short numberQuestAll;
    public static short numberQuestGet;
    public static byte optionCreateItem;
    public static byte slNLThu;
    byte[] arrSell;
    private byte[] arrayKhamNgoc;
    private String[] charInfo;
    private int cmdxTab;
    private int cmtoXTab;
    private int cmvxTab;
    private int cmxLimTab;
    private int cmxTab;
    public int countFrame;
    public int countPop;
    int dirX;
    int excess;
    private int focusTabChinh;
    public int h;
    public int hShow;
    public int hSmall;
    public short idCharSell;
    public int idClass;
    public int idFrame;
    public int idSeller;
    Bitmap imgCharWear;
    public Bitmap imgWeaponAvatar;
    boolean is29;
    private boolean isClick;
    boolean isCmyTab3;
    boolean isDefault;
    boolean isTab3;
    boolean isshowInfo;
    int limMyBag;
    public Vector listCheDo;
    public Vector listEp;
    int maxSize;
    public Vector newSkill;
    public int numH;
    private int pa;
    private int pb;
    private short[][] posEpNgoc;
    public int prizeImbue;
    private Vector questInfo;
    int saveFocusTab;
    public int selected;
    public int selectedGD;
    public int shopIdSell;
    private String[] showText;
    private boolean trans;
    int transX;
    public int w;
    public int wShow;
    public int wSmall;
    public int x;
    public int x0;
    public int xShow;
    public int y;
    public int y0;
    public int yShow;
    public static int[] posPaintWearingX = {9, 9, 9, 107, 107, 107, 107, 107, 107, 107, 9, 107, 9, 82, 9, 9, 9, 9, 9, 34, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 107, 107, 107, 107, 107, 34};
    public static int[] posPaintWearingY = {32, 73, 52, 11, 11, 11, 11, 11, 52, 32, 94, 94, 11, 92, 32, 52, 73, 94, 11, 92, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 11, 32, 52, 73, 94, 92};
    private static byte[] idWearChar = {12, -1, 0, 9, 2, 8, 1, 8, 10, 11};
    private static byte[] idWearAnimal = {18, 30, 14, 31, 15, 32, 16, 33, 17, 34};
    private static byte[][] posWearChar = {new byte[]{10, 11}, new byte[]{56, 11}, new byte[]{10, 32}, new byte[]{56, 32}, new byte[]{10, 52}, new byte[]{56, 52}, new byte[]{10, 73}, new byte[]{56, 94}, new byte[]{10, 94}, new byte[]{56, 94}};
    public int numW = 7;
    public int[][] arrFrame = {new int[]{0, 1, 2}, new int[]{0, 1}};
    private String[] title = {"Hành trang", "Trang bị", "Tiềm năng", "Kỹ năng", "Thông tin", "Nhóm", "Nhiệm vụ", "Học kỹ năng", "Trao đổi", "Gian hàng", "Gian hàng", "Áo", "Quần", "Nón", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Ngọc bội", "Gian hàng", "Gian hàng", "Luyện đồ", "Kho đồ", "Gian hàng", "Gian hàng", "Khảm", "Hợp thành", "Nhiệm vụ", "Chế đồ", "Kỹ năng bang hội", "Kỹ năng cá nhân", "Trang bị thú", "Hợp đồ thú"};
    String[] tabName = {"Áo", "Quần", "Nón", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Ngọc bội"};
    public String name = "";
    public String firName = "";
    public Vector list = new Vector();
    public Vector readyBuy = new Vector();
    public boolean isShowText = false;
    public boolean isSell = false;
    public boolean isChangeTab = false;
    public boolean isPaintMoney = false;
    public Vector sellItem = new Vector();
    public Vector listTemp = new Vector();
    public Vector listItemAnimal = new Vector();
    private ItemInInventory[] itemEpdothu = {null, null, null, null, null};
    public boolean isLoad = false;
    int countTabMyitem = 0;
    int xCus = -20;
    private int idImbue = -1;
    boolean first = false;
    int cmyKeep = 0;
    int tdx = 0;
    String[] mau = {"Trắng", "Hoàn mỹ", "Đỏ", "Xanh"};
    int wSkill = 30;
    public Vector potionShop = new Vector();

    public static boolean isDegit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isWeapone(int i) {
        return i >= 3 && i <= 7;
    }

    protected void doSellItem() {
    }

    public static WindowInfoScr gI() {
        WindowInfoScr windowInfoScr = me;
        if (windowInfoScr != null) {
            return windowInfoScr;
        }
        WindowInfoScr windowInfoScr2 = new WindowInfoScr();
        me = windowInfoScr2;
        return windowInfoScr2;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
        this.isChangeTab = true;
    }

    @Override
    public void init() {
        this.x = GCanvas.hw - 64;
        this.y = GCanvas.hh - 77;
        setLim();
        isWindowInfo = true;
        this.cmtoXTab = 0;
        this.cmxTab = 0;
        this.cmxLimTab = (this.tabName.length * 70) - (GCanvas.w - 20);
    }

    public WindowInfoScr() {
        this.dirX = 1;
        init();
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                WindowInfoScr.this.listTemp.removeAllElements();
                WindowInfoScr.this.listEp = null;
                WindowInfoScr.this.doBuyItem();
                GCanvas.gameScr.switchToMe();
                WindowInfoScr.this.list.removeAllElements();
                WindowInfoScr.this.charInfo = null;
                WindowInfoScr.this.questInfo = null;
                WindowInfoScr.this.imgCharWear = null;
                WindowInfoScr.charWearing = null;
                WindowInfoScr.isWindowInfo = false;
                WindowInfoScr.this.transX = 0;
                WindowInfoScr.this.dirX = 1;
                WindowInfoScr.this.focusTabChinh = 0;
                WindowInfoScr.this.isSell = false;
            }
        });
        this.focusTabChinh = 0;
        this.selected = 0;
        focusTab = 0;
        this.transX = 0;
        this.dirX = 1;
        this.isClick = false;
    }

    public void setInfo(Vector vector) {
        this.list = vector;
        this.numW = 7;
        this.numH = vector.size() / 5;
        if (vector.size() % this.numW != 0) {
            this.numH++;
        }
        if (this.numH < 5) {
            this.numH = 5;
        }
        if (index[focusTab] == 0 && this.numH > 6) {
            this.numH = 6;
        }
        setLim();
        this.isChangeTab = true;
        setCmdCenter();
    }

    public static int getIndex() {
        return index[focusTab];
    }

    private void setLim() {
        if (!this.is29) {
            int i = this.hSmall;
            int i2 = (this.numW * this.wSmall) - this.w;
            cmxLim = i2;
            int i3 = (this.numH * (i + 10)) - this.h;
            cmyLim = i3;
            if (i2 < 0) {
                cmxLim = 0;
            }
            if (i3 < 0) {
                cmyLim = 0;
            }
        } else {
            int i4 = (this.numW * this.wSmall) - this.w;
            cmxLim = i4;
            int i5 = (this.numH * this.hSmall) - this.h;
            cmyLim = i5;
            if (i4 < 0) {
                cmxLim = 0;
            }
            if (i5 < 0) {
                cmyLim = 0;
            }
        }
        this.cmtoXTab = 0;
        this.cmxTab = 0;
        this.cmxLimTab = (this.tabName.length * 70) - (GCanvas.w - 20);
    }

    public void setCurTab() {
        if (GCanvas.currentScreen == gI() && focusTab == 0) {
            setSelectTab();
            this.isChangeTab = false;
        }
    }

    public void setInfo(int i, boolean z, byte[] bArr) {
        try {
            this.isSell = z;
            if (z) {
                this.arrSell = bArr;
            }
            index = bArr;
            focusTab = i;
            if (bArr[i] == 29) {
                this.is29 = true;
            } else {
                this.is29 = false;
            }
            this.selected = 0;
            setCmdCenter();
            setSelectTab();
        } catch (Exception unused) {
        }
    }

    public void setSize(int i, int i2, int i3, int i4, int i5, int i6) {
        this.w = i;
        this.h = i2;
        this.numW = i3;
        this.numH = i4;
        this.wSmall = i5;
        this.hSmall = i6;
        setLim();
        byte b = index[focusTab];
        if (b == 3) {
            cmtoY = 10;
            cmy = 10;
            cmtoX = 10;
            cmx = 10;
        } else {
            cmtoY = -10;
            cmtoX = -10;
            cmy = -10;
            cmx = -10;
        }
        if (this.isDefault) {
            cmtoX = -10;
            cmx = -10;
            cmtoY = 0;
            cmy = 0;
        }
        if (b == 32) {
            cmtoX = 10;
            cmx = 10;
            cmtoY = 0;
            cmy = 0;
        }
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (GCanvas.keyHold[2]) {
                if (this.hShow >= 100) {
                    int i = cmtoYText - 10;
                    cmtoYText = i;
                    if (i < 0) {
                        cmtoYText = 0;
                    }
                } else {
                    closeText();
                }
                GCanvas.keyPressed[2] = false;
            } else if (GCanvas.keyHold[8]) {
                if (this.hShow >= 100) {
                    int i2 = cmtoYText + 10;
                    cmtoYText = i2;
                    int i3 = cmyLimText;
                    if (i2 > i3) {
                        cmtoYText = i3;
                    }
                } else {
                    closeText();
                }
                GCanvas.keyPressed[8] = false;
            }
            GCanvas.isKeyPressed(2);
            GCanvas.isKeyPressed(8);
            if (GCanvas.isPointerDown && GCanvas.isPointer(this.xShow, this.yShow, this.wShow - 1, this.hShow - 1)) {
                if (!this.trans) {
                    this.pa = cmyText;
                    this.trans = true;
                }
                int i4 = this.pa + (GCanvas.pyLast - GCanvas.py);
                cmtoYText = i4;
                if (i4 < -30) {
                    cmtoYText = -30;
                }
                int i5 = cmtoYText;
                int i6 = cmyLimText;
                if (i5 > i6 + 30) {
                    cmtoYText = i6 + 30;
                }
            }
        }
        if (GCanvas.keyPressed[4] || GCanvas.keyPressed[6]) {
            closeText();
        }
        if (GCanvas.isPointerClick && index[focusTab] == 0) {
            if (GCanvas.isPointer(-10, 165, 40, 30)) {
                int i7 = this.countTabMyitem - 1;
                this.countTabMyitem = i7;
                if (i7 < 0) {
                    this.countTabMyitem = this.limMyBag;
                }
                this.selected = 0;
                setSize(140, 112, 7, 9, 18, 18);
                countBL = (byte) 5;
                closeText();
            } else if (GCanvas.isPointer(140, 165, 40, 30)) {
                int i8 = this.countTabMyitem + 1;
                this.countTabMyitem = i8;
                if (i8 > this.limMyBag) {
                    this.countTabMyitem = 0;
                }
                this.selected = 0;
                setSize(140, 112, 7, 9, 18, 18);
                countBR = (byte) 5;
                closeText();
            }
        }
        updateKeyMain();
        super.updateKey();
    }

    public void getItemAnimalFromList() {
        this.listItemAnimal = new Vector();
        int size = Char.inventory.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if ((item.type == 14 || item.type == 15 || item.type == 16 || item.type == 17 || item.type == 18) && itemInInventory.colorName == idColor) {
                this.listItemAnimal.addElement(itemInInventory);
            }
        }
    }

    public void setSelectTab() {
        cmxname = 0;
        this.left = null;
        this.isPaintMoney = false;
        this.newSkill = null;
        this.posEpNgoc = null;
        this.list.removeAllElements();
        this.x0 = 1;
        this.y0 = 35;
        byte[] bArr = index;
        if (bArr.length > 1) {
            this.firName = this.name;
            this.name = this.title[bArr[focusTab]];
        }
        this.countTabMyitem = 0;
        int i = focusTab;
        byte b = bArr[i];
        if (b != 0) {
            this.saveFocusTab = i;
        }
        if (b != 127) {
            switch (b) {
                case 0:
                    this.isDefault = true;
                    this.isPaintMoney = true;
                    setInfo(getInventori());
                    setSize(140, this.isDefault ? 112 : 90, 7, 9, 18, 18);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Chọn", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.doSelectedInventori();
                            }
                        });
                    }
                    break;
                case 1:
                    this.x0 = 1 + 5;
                    this.y0 = 35 - 2;
                    setSize(117, 104, 3, 5, 28, 28);
                    final Vector vector = new Vector();
                    if (GCanvas.gameScr.mainChar.idFashion != -1) {
                        vector.addElement(new Command("Thay áo", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.startYesNoDlg("Bạn muốn bộ áo thời trang vào rương không ?", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameService.gI().unRideHorse((byte) 1);
                                        GCanvas.endDlg();
                                        GCanvas.gameScr.switchToMe();
                                    }
                                });
                            }
                        }));
                    }
                    if (vector.size() == 1) {
                        this.left = (Command) vector.elementAt(0);
                    } else if (vector.size() == 2) {
                        this.left = new Command("Chọn", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.menu.startAt(vector, 0);
                            }
                        });
                    }
                    break;
                case 2:
                    this.y0 = 35 + 13;
                    setSize(124, 94, 1, 5, 125, 19);
                    break;
                case 3:
                    this.left = null;
                    this.x0 += 5;
                    this.y0 += 7;
                    this.selected = 0;
                    setSize(114, 30, GCanvas.gameScr.mainChar.getnSkill(), 1, 30, 20);
                    break;
                case 4:
                    this.center = null;
                    this.isPaintMoney = true;
                    this.x0 -= 4;
                    this.y0 -= 2;
                    setSize(134, 120, 1, 10, 50, 12);
                    break;
                case 5:
                    this.isPaintMoney = true;
                    this.x0 = 1 - 4;
                    this.y0 = 35 - 2;
                    setSize(134, 95, 1, Char.party.size(), 130, 32);
                    break;
                case 6:
                    this.isPaintMoney = true;
                    this.x0 = 1 - 4;
                    this.y0 = 35 - 2;
                    setQuestInfo();
                    setSize(134, 95, 1, this.questInfo.size(), 130, 15);
                    this.questInfo.removeAllElements();
                    if (GCanvas.gameScr.mainChar.receiveQuest) {
                        this.left = new Command("Hủy", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().receiveQuest(0, 0, 3);
                                WindowInfoScr.this.questInfo.removeAllElements();
                            }
                        });
                    }
                    break;
                case 7:
                    this.x0 = 1 + 5;
                    this.y0 = 35 + 7;
                    this.left = null;
                    setCmdCenter();
                    break;
                case 8:
                    this.center = new Command("Chọn", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doSelectedTrade();
                        }
                    });
                    setCmdLeftTrade();
                    break;
                case 9:
                    this.isPaintMoney = true;
                    setSize(126, 112, 1, this.list.size(), 18, 18);
                    setListSellItem(GCanvas.gameScr.shop, -1);
                    setInfo(this.list);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.doSetLeftSellWeapon(-1);
                            }
                        });
                    }
                    break;
                case 10:
                    this.isPaintMoney = true;
                    setSize(126, 84, 1, this.list.size(), 18, 18);
                    setListSellGemItem(GCanvas.gameScr.shop);
                    setInfo(this.list);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.dosetLeftSellGemItem();
                            }
                        });
                    }
                    break;
                case 11:
                case 12:
                case 13:
                    this.isPaintMoney = true;
                    setSize(126, 112, 1, 1, 18, 18);
                    setListSellItem(GCanvas.gameScr.shop, index[focusTab] - 11);
                    setInfo(this.list);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.doSetLeftSellWeapon(WindowInfoScr.index[WindowInfoScr.focusTab] - 11);
                            }
                        });
                    }
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                    this.isPaintMoney = true;
                    setSize(126, 112, 1, 1, 18, 18);
                    setListSellItem(GCanvas.gameScr.shop, index[focusTab] - 6);
                    setInfo(this.list);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.doSetLeftSellWeapon(WindowInfoScr.index[WindowInfoScr.focusTab] - 6);
                            }
                        });
                    }
                    break;
                case 19:
                    this.isPaintMoney = true;
                    setSize(126, 112, 1, 1, 18, 18);
                    setListPotion();
                    setInfo(this.list);
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Mua", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.doSetLeftPotion();
                            }
                        });
                    }
                    break;
                case 20:
                    this.isPaintMoney = true;
                    setSize(126, 112, 1, 1, 18, 18);
                    getShopTemplate();
                    setInfo(this.list);
                    doBuyShopSpecial();
                    this.name += " " + (focusTab + 1);
                    break;
                case 21:
                    this.x0 = 1 + 1;
                    this.y0 = 35 - 6;
                    setInfo(this.list);
                    setSize(144, 110, 3, 5, 24, 24);
                    setCmdCenter();
                    doLeftImbue();
                    break;
                case 22:
                    this.isPaintMoney = true;
                    this.x0 = 1 + 9;
                    this.y0 = 35;
                    this.selected = 0;
                    setSizeKeepItem(0);
                    setSize(132, 110, 6, this.numH, 22, 22);
                    setCmdCenter();
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Chuyển", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.setLeftKeepAndSellItem();
                            }
                        });
                    }
                    break;
                case 23:
                    this.isPaintMoney = true;
                    this.x0 = 1 + 9;
                    this.y0 = 35;
                    this.selected = 0;
                    setSizeKeepItem(0);
                    setSize(132, 110, 6, this.numH, 22, 22);
                    setCmdCenter();
                    if (GCanvas.screenlevel == 1) {
                        this.left = new Command("Chọn", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.setLeftKeepAndSellItem();
                            }
                        });
                    }
                    break;
                case 24:
                    this.isPaintMoney = true;
                    setSize(126, 112, 7, this.numH, 18, 18);
                    setInfo(getListBuyItem());
                    this.left = new Command("Mua", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.setLeftBuyItem();
                        }
                    });
                    break;
                case 25:
                    this.isChangeTab = false;
                    this.x0 = 1 + 1;
                    this.y0 = 35 - 4;
                    setInfo(this.list);
                    setSize(144, 110, 3, 5, 24, 24);
                    if (isKham) {
                        setListImbue(false, 1, khoaKham);
                    } else {
                        this.name = "Đục lỗ";
                        setListImbue(false, 2, khoaKham);
                    }
                    this.center = new Command("", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.setSelectedImbue();
                        }
                    });
                    doLeftImbue();
                    break;
                case 26:
                    this.x0 = 1 + 1;
                    this.y0 = 35 - 4;
                    setSize(126, 108, 5, 1, 20, 18);
                    int i2 = this.w;
                    this.posEpNgoc = new short[][]{new short[]{(short) ((i2 / 2) - 44), 20}, new short[]{(short) ((i2 / 2) + 26), 20}, new short[]{(short) ((i2 / 2) - 10), 43}, new short[]{(short) ((i2 / 2) - 44), 65}, new short[]{(short) ((i2 / 2) + 26), 65}};
                    setListImbue(true, 1, 0);
                    this.listEp = new Vector();
                    this.center = new Command("", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doSelectedEpNgoc();
                        }
                    });
                    doLeftEpNgoc();
                    break;
                case 27:
                    this.isPaintMoney = false;
                    this.x0 = 1 - 4;
                    this.y0 = 35 - 2;
                    if (this.questInfo == null) {
                        this.questInfo = new Vector();
                    }
                    setSize(134, 95, 1, this.questInfo.size(), 130, 15);
                    this.left = new Command("Hủy", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().questClan(2);
                            WindowInfoScr.this.right.action.perform();
                        }
                    });
                    break;
                case 28:
                    int size = this.listEp.size() / 2;
                    setSize(144, 144, size < 6 ? 6 : size, 3, 24, 24);
                    this.center = new Command("", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doSelectedCheDo();
                        }
                    });
                    this.left = new Command("Xong", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doLeftCheDo();
                        }
                    });
                    break;
                case 29:
                    try {
                        setInfo(setListGuildSkill());
                        setSize(126, 100, 1, 6, 126, 20);
                        for (int i3 = 0; i3 < GCanvas.gameScr.skillClan.length; i3++) {
                            short s = GCanvas.gameScr.skillClan[i3].time;
                        }
                        for (int i4 = 0; i4 < GCanvas.gameScr.skillClanPrivate.length; i4++) {
                            short s2 = GCanvas.gameScr.skillClanPrivate[i4].time;
                        }
                        this.isChangeTab = true;
                    } catch (Exception unused) {
                        Cout.println("Loi goi skill ban hoi");
                    }
                    break;
                case 30:
                    setInfo(setListGuildSkill());
                    setSize(126, 100, 1, 6, 126, 20);
                    this.isChangeTab = true;
                    break;
                case 31:
                    this.x0 = 1 + 5;
                    this.y0 = 35 - 2;
                    setSize(117, 104, 3, 5, 28, 28);
                    break;
                case 32:
                    this.x0 = 1 + 1;
                    this.y0 = 35 - 4;
                    setSize(126, 108, 5, 1, 20, 18);
                    int i5 = this.w;
                    this.posEpNgoc = new short[][]{new short[]{(short) ((i5 / 2) - 44), -10}, new short[]{(short) ((i5 / 2) + 26), -10}, new short[]{(short) ((i5 / 2) - 10), 13}, new short[]{(short) ((i5 / 2) - 44), 35}, new short[]{(short) ((i5 / 2) + 26), 35}};
                    int i6 = 0;
                    while (true) {
                        ItemInInventory[] itemInInventoryArr = this.itemEpdothu;
                        if (i6 < itemInInventoryArr.length) {
                            itemInInventoryArr[i6] = null;
                            i6++;
                        } else {
                            getItemAnimalFromList();
                            this.listEp = new Vector();
                            this.center = new Command("", new IAction() {
                                @Override
                                public void perform() {
                                    WindowInfoScr.this.doSelectedEpDothu();
                                }
                            });
                            doLeftEpDothu();
                        }
                        break;
                    }
                    break;
            }
        } else {
            this.isPaintMoney = true;
            setSize(126, 90, 1, 1, 18, 18);
            setListSellItem(GCanvas.gameScr.shop, -1);
            setInfo(this.list);
            doSetLeftPetEat(-1);
        }
        if (this.selected >= (this.numH * this.numW) - 1) {
            this.selected = 0;
        }
    }

    private Vector setListGuildSkill() {
        Vector vector = new Vector();
        int i = 0;
        for (int i2 = 0; i2 < GCanvas.gameScr.skillClan.length; i2++) {
            try {
                if (GCanvas.gameScr.skillClan[i2].time > 0) {
                    final int i3 = i2;
                    final int i4 = i;
                    vector.addElement(new Command("", new IAction() {
                        @Override
                        public void perform() {
                        }
                    }) {
                        @Override
                        public void paint(Graphics graphics, int i5, int i6) {
                            graphics.setClip(i5 - (WindowInfoScr.this.wSmall / 2), i6 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall, WindowInfoScr.this.hSmall);
                            graphics.ClipRec(i5 - (WindowInfoScr.this.wSmall / 2), i6 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall, WindowInfoScr.this.hSmall);
                            GameData.paintIcon(graphics, (short) (i3 + 1600), (i5 - (WindowInfoScr.this.wSmall / 2)) + 9, i6);
                            int i7 = 0;
                            if (i4 == WindowInfoScr.this.selected && !WindowInfoScr.this.isChangeTab) {
                                int width = Font.arialFontBlack.getWidth(GCanvas.gameScr.skillClan[i3].info);
                                if (width > WindowInfoScr.this.w - ((WindowInfoScr.this.wSmall / 2) + 16)) {
                                    WindowInfoScr.this.xCus += 2;
                                    if (WindowInfoScr.this.xCus > width - ((WindowInfoScr.this.wSmall / 2) + 30)) {
                                        WindowInfoScr.this.xCus = -20;
                                    }
                                }
                                int i8 = WindowInfoScr.this.xCus;
                                if (WindowInfoScr.this.xCus >= 0) {
                                    i7 = i8;
                                }
                            }
                            graphics.restoreCanvas();
                            graphics.setClip((i5 - (WindowInfoScr.this.wSmall / 2)) + 18, i6 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall - 20, WindowInfoScr.this.hSmall);
                            graphics.ClipRec((i5 - (WindowInfoScr.this.wSmall / 2)) + 18, i6 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall - 20, WindowInfoScr.this.hSmall);
                            Font.arialFontBlack.drawString(graphics, GCanvas.gameScr.skillClan[i3].info, ((i5 - (WindowInfoScr.this.wSmall / 2)) + 18) - i7, i6 - (Font.arialFontBlack.getHeight() / 2), 0);
                            graphics.restoreCanvas();
                        }
                    });
                    i++;
                }
            } catch (Exception unused) {
                Cout.println("null skill bang hoi setListGuildSkill");
            }
        }
        for (int i5 = 0; i5 < GCanvas.gameScr.skillClanPrivate.length; i5++) {
            if (GCanvas.gameScr.skillClanPrivate[i5].time > 0) {
                final int i6 = i5;
                final int i7 = i;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i8, int i9) {
                        graphics.setClip(i8 - (WindowInfoScr.this.wSmall / 2), i9 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall, WindowInfoScr.this.hSmall);
                        GameData.paintIcon(graphics, (short) (i6 + 1600), (i8 - (WindowInfoScr.this.wSmall / 2)) + 9, i9);
                        int i10 = 0;
                        if (i7 == WindowInfoScr.this.selected && !WindowInfoScr.this.isChangeTab) {
                            int width = Font.arialFontBlack.getWidth(GCanvas.gameScr.skillClan[i6].info);
                            if (width > WindowInfoScr.this.w - ((WindowInfoScr.this.wSmall / 2) + 16)) {
                                WindowInfoScr.this.xCus += 2;
                                if (WindowInfoScr.this.xCus > width - ((WindowInfoScr.this.wSmall / 2) + 30)) {
                                    WindowInfoScr.this.xCus = -20;
                                }
                            }
                            int i11 = WindowInfoScr.this.xCus;
                            if (WindowInfoScr.this.xCus >= 0) {
                                i10 = i11;
                            }
                        }
                        graphics.setClip((i8 - (WindowInfoScr.this.wSmall / 2)) + 18, i9 - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall - 20, WindowInfoScr.this.hSmall);
                        Font.arialFontBlack.drawString(graphics, GCanvas.gameScr.skillClanPrivate[i6].info, (i8 - (WindowInfoScr.this.wSmall / 2)) + 18 + i10, i9 - (Font.arialFontBlack.getHeight() / 2), 0);
                    }
                });
                i++;
            }
        }
        return vector;
    }

    protected void doLeftCheDo() {
        int size = this.listEp.size();
        if (size == 0) {
            return;
        }
        final byte[][] bArr = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, size, 6);
        for (int i = 0; i < size; i++) {
            GemTemplate gemTemplate = (GemTemplate) this.listEp.elementAt(i);
            for (int i2 = 0; i2 < size; i2++) {
                int i3 = gemTemplate.id - ((Mineral) this.listCheDo.elementAt(i2)).idTemplate;
                if (i3 <= 5) {
                    bArr[i2][i3] = (byte) gemTemplate.number;
                    break;
                }
            }
        }
        GCanvas.startYesNoDlg("Bạn có muốn hoàn thành không ?", new IAction() {
            @Override
            public void perform() {
                for (int size2 = WindowInfoScr.this.listEp.size() - 1; size2 >= 0; size2--) {
                    WindowInfoScr.this.removeItemCheDo((GemTemplate) WindowInfoScr.this.listEp.elementAt(size2));
                }
                GameService.gI().doCheDo((short) WindowInfoScr.this.listCheDo.size(), WindowInfoScr.idTemp, bArr);
                WindowInfoScr.this.right.action.perform();
                GCanvas.startWaitDlg();
            }
        });
    }

    public void removeItemCheDo(GemTemplate gemTemplate) {
        gemTemplate.number--;
        if (gemTemplate.number <= 0) {
            this.listEp.removeElement(gemTemplate);
        }
        int size = this.listTemp.size();
        for (int i = 0; i < size; i++) {
            GemTemplate gemTemplate2 = (GemTemplate) this.listTemp.elementAt(i);
            if (gemTemplate2.id == gemTemplate.id) {
                gemTemplate2.number++;
                return;
            }
        }
        GemTemplate gemTemplate3 = new GemTemplate(gemTemplate.id);
        GemTemplate.copyData(gemTemplate, gemTemplate3);
        gemTemplate3.number = 1;
        this.listTemp.addElement(gemTemplate);
    }

    protected void doSelectedCheDo() {
        final GemTemplate gemTemplate;
        try {
            int i = this.selected;
            int i2 = this.numW;
            final boolean z = i / i2 < 2;
            if (z) {
                if (i < this.listEp.size()) {
                    gemTemplate = (GemTemplate) this.listEp.elementAt(this.selected);
                } else {
                    gemTemplate = null;
                }
            } else if (i % i2 < this.listTemp.size()) {
                gemTemplate = (GemTemplate) this.listTemp.elementAt(this.selected % this.numW);
            } else {
                gemTemplate = null;
            }
            if (gemTemplate == null) {
                return;
            }
            int i3 = this.selected;
            int i4 = this.numW;
            int i5 = this.wSmall;
            final int i6 = ((i3 % i4) * i5) + cmx;
            final int i7 = (i3 / i4) * i5;
            final short s = gemTemplate.id;
            Vector vector = new Vector();
            vector.addElement(new Command(!z ? "Bỏ vào" : "Lấy ra", new IAction() {
                @Override
                public void perform() {
                    if (z) {
                        WindowInfoScr.this.removeItemCheDo(gemTemplate);
                    } else {
                        int size = WindowInfoScr.this.listCheDo.size();
                        int i8 = 0;
                        int i9 = 0;
                        for (int i10 = 0; i10 < size; i10++) {
                            Mineral mineral = (Mineral) WindowInfoScr.this.listCheDo.elementAt(i10);
                            if (gemTemplate.id - mineral.idTemplate >= 0 && gemTemplate.id - mineral.idTemplate <= 5) {
                                int size2 = WindowInfoScr.this.listEp.size();
                                for (int i11 = 0; i11 < size2; i11++) {
                                    GemTemplate gemTemplate2 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i11);
                                    if (gemTemplate2.id - mineral.idTemplate >= 0 && gemTemplate2.id - mineral.idTemplate <= 5) {
                                        i9 += gemTemplate2.number;
                                    }
                                }
                                if (i9 >= mineral.number) {
                                    GCanvas.startOKDlg("Đã đủ số lượng.");
                                    return;
                                }
                            }
                        }
                        int size3 = WindowInfoScr.this.listEp.size();
                        while (true) {
                            if (i8 < size3) {
                                GemTemplate gemTemplate3 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i8);
                                if (gemTemplate3.id == gemTemplate.id) {
                                    gemTemplate3.number++;
                                    break;
                                }
                                i8++;
                            } else {
                                GemTemplate gemTemplate4 = new GemTemplate(gemTemplate.id);
                                GemTemplate.copyData(gemTemplate, gemTemplate4);
                                gemTemplate4.number = 1;
                                WindowInfoScr.this.listEp.addElement(gemTemplate4);
                                break;
                            }
                        }
                        gemTemplate.number--;
                        if (gemTemplate.number <= 0) {
                            WindowInfoScr.this.listTemp.removeElement(gemTemplate);
                        }
                    }
                    WindowInfoScr.this.setLimitCheDo();
                }
            }));
            vector.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    if (WindowInfoScr.khoaCheDo == 0 || WindowInfoScr.khoaCheDo == 2) {
                        WindowInfoScr.this.showGemItemInfo(s, i6, i7);
                    } else {
                        WindowInfoScr.this.showGemItemKhoaInfo(s, i6, i7);
                    }
                }
            }));
            GCanvas.menu.startAt(vector, 2);
        } catch (Exception unused) {
        }
    }

    public void setImageCharWear() {
        int size = charWearing.wearingItems.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingItems.elementAt(i2);
            if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).plus > 13) {
                i++;
            }
        }
        if (i < 3 || charWearing.idFashion != -1) {
            return;
        }
        this.imgCharWear = Bitmap.createBitmap(60, 100, Bitmap.Config.RGB_565);
        int[] iArr = new int[6000];
        for (int i3 = 0; i3 < 6000; i3++) {
            if (iArr[i3] != 16777215) {
                int i4 = i3 - 1;
                if (i4 > 0 && iArr[i4] == 16777215) {
                    iArr[i3] = -1;
                } else {
                    int i5 = i3 + 1;
                    if (i5 < 6000 && iArr[i5] == 16777215) {
                        iArr[i3] = -1;
                    }
                }
            }
        }
        this.imgCharWear = Bitmap.createBitmap(iArr, 60, 100, Bitmap.Config.RGB_565);
    }

    public void doSelectedEpNgoc() {
        try {
            Vector vector = new Vector();
            if (this.numH != 1) {
                if (this.selected % this.numW < this.listTemp.size()) {
                    final GemTemplate gemTemplate = (GemTemplate) this.listTemp.elementAt(this.selected % this.numW);
                    vector.addElement(new Command("Bỏ vào", new IAction() {
                        @Override
                        public void perform() {
                            if (WindowInfoScr.this.listEp.size() < 5) {
                                gemTemplate.number--;
                                if (gemTemplate.number <= 0) {
                                    WindowInfoScr.this.listTemp.removeElement(gemTemplate);
                                }
                                RealID realID = new RealID(gemTemplate.rID);
                                realID.ID = gemTemplate.id;
                                WindowInfoScr.this.listEp.addElement(realID);
                            }
                        }
                    }));
                    vector.addElement(addGemInfo(gemTemplate, ((this.selected % this.numW) * 20) + 4, 86));
                }
            } else if (this.selected < this.listEp.size()) {
                final RealID realID = (RealID) this.listEp.elementAt(this.selected);
                vector.addElement(new Command("Lấy ra", new IAction() {
                    @Override
                    public void perform() {
                        GemTemplate.getGemByID(realID.ID).number++;
                        WindowInfoScr.this.listEp.removeElement(realID);
                    }
                }));
                GemTemplate gemTemplate2 = new GemTemplate();
                gemTemplate2.id = realID.ID;
                short[] sArr = this.posEpNgoc[this.selected];
                vector.addElement(addGemInfo(gemTemplate2, sArr[0], sArr[1]));
            }
            GCanvas.menu.startAt(vector, 2);
        } catch (Exception unused) {
        }
    }

    private Command addGemInfo(final GemTemplate gemTemplate, int i, int i2) {
        return new Command("Thông tin", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.khoaDoThu == 0 || WindowInfoScr.khoaDoThu == 2) {
                    WindowInfoScr.this.showGemItemInfo(gemTemplate.id, 145, -10);
                } else {
                    WindowInfoScr.this.showGemItemKhoaInfo(gemTemplate.id, 145, -10);
                }
            }
        });
    }

    private void doLeftEpNgoc() {
        this.left = new Command("Hợp", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.this.listEp.size() < 3) {
                    GCanvas.startOKDlg("Bạn phải ép ít nhất 3 vật phẩm.");
                } else {
                    GCanvas.startYesNoDlg("Bạn phải mất " + GCanvas.getMoneys(WindowInfoScr.this.prizeImbue) + "Xu để hợp.", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().epNgoc(1, WindowInfoScr.this.listEp);
                            GCanvas.startWaitDlg("Đang hợp..", false);
                            WindowInfoScr.this.listEp.removeAllElements();
                        }
                    });
                }
            }
        });
    }

    public void setLeftBuyItem() {
        if (this.selected < this.sellItem.size()) {
            GCanvas.startYesNoDlg("Bạn có muốn mua không ?", new IAction() {
                @Override
                public void perform() {
                    if (WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected) instanceof ItemInInventory) {
                        GameService.gI().requestBuyItemUser(WindowInfoScr.this.idCharSell, WindowInfoScr.this.idSeller, WindowInfoScr.this.shopIdSell, ((ItemInInventory) WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected)).itemID, 0);
                    } else {
                        GameService.gI().requestBuyItemUser(WindowInfoScr.this.idCharSell, WindowInfoScr.this.idSeller, WindowInfoScr.this.shopIdSell, ((RealID) WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected)).realID, 1);
                    }
                    GCanvas.startWaitDlg();
                }
            });
        }
    }

    public Vector getListBuyItem() {
        Vector vector = new Vector();
        int size = this.sellItem.size();
        for (int i0 = 0; i0 < size; i0++) {
            final int i = i0;
            vector.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    if (WindowInfoScr.this.sellItem.elementAt(i) instanceof ItemInInventory) {
                        WindowInfoScr windowInfoScr = WindowInfoScr.this;
                        windowInfoScr.setShowTextNew(((ItemInInventory) windowInfoScr.sellItem.elementAt(i)).getInfoItemUserSell(), (i % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, (i / WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall);
                    } else {
                        WindowInfoScr windowInfoScr2 = WindowInfoScr.this;
                        windowInfoScr2.showRealIDInfo((RealID) windowInfoScr2.sellItem.elementAt(i), (i % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, (i / WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall);
                    }
                }
            }) {
                @Override
                public void paint(Graphics graphics, int i2, int i3) {
                    if (WindowInfoScr.this.sellItem.elementAt(i) instanceof ItemInInventory) {
                        ItemInInventory itemInInventory = (ItemInInventory) WindowInfoScr.this.sellItem.elementAt(i);
                        if (itemInInventory.isSelling) {
                            graphics.setColor(-9070864);
                            graphics.fillRect(i2 - 7, i3 - 7, 14, 14);
                        }
                        itemInInventory.paint(graphics, i2, i3);
                        return;
                    }
                    Res.paintGem(graphics, Res.getGemByID(((RealID) WindowInfoScr.this.sellItem.elementAt(i)).ID).idImage, i2, i3);
                }
            });
        }
        return vector;
    }

    public void setLeftKeepAndSellItem() {
        int i;
        RealID realID;
        int i2;
        int i3 = this.selected;
        int i4 = this.numW;
        int i5 = ((i3 / i4) * 3) + (i3 % i4);
        ItemInInventory itemInInventory = null;
        if (i3 % i4 < 3) {
            if (i5 < Char.inventory.size()) {
                ItemInInventory itemInInventory2 = (ItemInInventory) Char.inventory.elementAt(i5);
                byte b = index[focusTab];
                if (b == 22) {
                    GCanvas.gameScr.gameService.putItem2Bag(itemInInventory2.itemID);
                    return;
                } else {
                    if (b == 23) {
                        if (itemInInventory2.isSelling) {
                            GCanvas.startOKDlg("Vật phẩm đang được bán.");
                            return;
                        } else {
                            setLeftSellItem(itemInInventory2, (short) -1);
                            return;
                        }
                    }
                    return;
                }
            }
            if (i5 >= MainChar.inventory.size() + MainChar.gemItem.size() || index[focusTab] != 23) {
                return;
            }
            setLeftSellItem(null, ((GemTemplate) MainChar.gemItem.elementAt(i5 - Char.inventory.size())).rID);
            return;
        }
        closeText();
        if (index[focusTab] == 22 && (i2 = i5 - 3) < Char.itembag.size()) {
            GCanvas.gameScr.gameService.getItemFromBag(((ItemInInventory) Char.itembag.elementAt(i2)).itemID);
            return;
        }
        if (index[focusTab] != 23 || (i = i5 - 3) >= this.sellItem.size()) {
            return;
        }
        if (this.sellItem.elementAt(i) instanceof ItemInInventory) {
            itemInInventory = (ItemInInventory) this.sellItem.elementAt(i);
            realID = null;
        } else {
            realID = (RealID) this.sellItem.elementAt(i);
        }
        doRequestSellItem(itemInInventory, realID != null ? realID.realID : (short) -1, 0, false);
    }

    protected void setLeftSellItem(final ItemInInventory itemInInventory, final short s) {
        closeText();
        GCanvas.inputDlg.setInfo("Nhập giá muốn bán: ", new IAction() {
            @Override
            public void perform() {
                int i;
                try {
                    String text = GCanvas.inputDlg.tfInput.getText();
                    if (!text.equals("") && (i = Integer.parseInt(text)) >= 0) {
                        WindowInfoScr.this.doRequestSellItem(itemInInventory, s, i, true);
                    }
                } catch (Exception unused) {
                    GCanvas.startOKDlg("Vui lòng chỉ nhập số.");
                }
            }
        }, 1, 10, true);
        GCanvas.inputDlg.show();
    }

    public void doRequestSellItem(ItemInInventory itemInInventory, short s, int i, boolean z) {
        GameService.gI().requestSellItem(this.idSeller, this.shopIdSell, itemInInventory, s, i, z);
        GCanvas.startWaitDlg();
    }

    private void setSelectedKeepAndSellItem() {
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                Vector vector;
                int i = ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 3) + (WindowInfoScr.this.selected % WindowInfoScr.this.numW);
                if (WindowInfoScr.this.selected % WindowInfoScr.this.numW < 3) {
                    if (i < Char.inventory.size()) {
                        ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
                        if (itemInInventory != null) {
                            WindowInfoScr windowInfoScr = WindowInfoScr.this;
                            windowInfoScr.showItemInventoryInfo(itemInInventory, windowInfoScr.isSell, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                            return;
                        }
                        return;
                    }
                    if (i < Char.inventory.size() + MainChar.gemItem.size()) {
                        WindowInfoScr.this.showGemItemInfo(((GemTemplate) MainChar.gemItem.elementAt(i - Char.inventory.size())).id, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                        return;
                    }
                    return;
                }
                if (WindowInfoScr.index[WindowInfoScr.focusTab] == 22) {
                    vector = Char.itembag;
                } else {
                    vector = WindowInfoScr.this.sellItem;
                }
                int i2 = i - 3;
                if (i2 < vector.size()) {
                    if (vector.elementAt(i2) instanceof ItemInInventory) {
                        ItemInInventory itemInInventory2 = (ItemInInventory) vector.elementAt(i2);
                        WindowInfoScr windowInfoScr2 = WindowInfoScr.this;
                        windowInfoScr2.showItemInventoryInfo(itemInInventory2, windowInfoScr2.isSell, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                        return;
                    }
                    WindowInfoScr.this.showRealIDInfo((RealID) vector.elementAt(i2), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                }
            }
        });
    }

    public void setListImbue(boolean z, int i, int i2) {
        this.listTemp.removeAllElements();
        Vector vector = i2 == 0 ? MainChar.gemItem : MainChar.gemItemKhoa;
        for (int i3 = 0; i3 < vector.size(); i3++) {
            GemTemplate gemTemplate = (GemTemplate) vector.elementAt(i3);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            if (!z) {
                if (gemByID.type == i) {
                    this.listTemp.addElement(gemTemplate);
                }
            } else if (gemByID.typeEp == i) {
                this.listTemp.addElement(gemTemplate);
            }
        }
        if (this.listTemp.size() == 0) {
            GCanvas.startOKDlg("Không có nguyên liệu.", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.right.action.perform();
                    GCanvas.endDlg();
                }
            });
        }
    }

    private void doLeftImbue() {
        this.left = new Command(index[focusTab] == 21 ? "Đập" : "Xong", new IAction() {
            @Override
            public void perform() {
                if (GCanvas.gameScr.mainChar.itemImbue != null) {
                    if (GCanvas.gameScr.mainChar.hadPutItemImbue()) {
                        GCanvas.startYesNoDlg("Bạn phải mất " + WindowInfoScr.this.prizeImbue + " xu để luyện.", new IAction() {
                            @Override
                            public void perform() {
                                if (GCanvas.gameScr.mainChar.charXu < WindowInfoScr.this.prizeImbue) {
                                    GCanvas.startOKDlg("Không đủ tiền");
                                    return;
                                }
                                GCanvas.gameScr.mainChar.charXu -= (long) WindowInfoScr.this.prizeImbue;
                                if (WindowInfoScr.index[WindowInfoScr.focusTab] == 21) {
                                    GameService.gI().doImbueItem(1);
                                } else {
                                    GameService.gI().doKhamItem(1);
                                }
                                GCanvas.startWaitDlg();
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    } else {
                        GCanvas.startOKDlg(WindowInfoScr.index[WindowInfoScr.focusTab] != 21 ? "Chưa đặt ngọc" : "Chưa đặt luyện kim dược.");
                    }
                }
            }
        });
    }

    public void setSelectedImbue() {
        Vector vector = new Vector();
        int i = this.selected;
        int i2 = this.numW;
        final int i3 = i / i2;
        final int i4 = i % i2;
        int i5 = (i3 * 2) + (i4 / 2);
        if ((i3 == 4 && (this.idImbue == -1 || i4 >= MainChar.inventory.size())) || (i3 != 4 && ((i4 == 1 && this.idImbue != -1) || (i4 != 1 && i5 < MainChar.gemitemImbue.size())))) {
            vector.addElement(new Command(i3 != 4 ? "Lấy ra" : "Bỏ vào", new IAction() {
                @Override
                public void perform() {
                    int i6 = i3;
                    if (i6 == 4) {
                        if (i4 < Char.inventory.size()) {
                            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i4);
                            if (itemInInventory.dayUse > 0) {
                                GCanvas.startOKDlg("Không được đập đồ thuê.");
                                return;
                            }
                            if (GCanvas.gameScr.mainChar.itemImbue == null && !itemInInventory.isSelling) {
                                GCanvas.gameScr.mainChar.itemImbue = itemInInventory;
                                ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                                if (item.type < 3 || item.type > 7) {
                                    WindowInfoScr.this.imgWeaponAvatar = GameData.getImgIcon(item.idIcon).img;
                                } else if (item.type >= 3 && item.type < 8) {
                                    GameService.gI().doRequestWeapone(2, item.type, item.style, (byte) item.index);
                                    GCanvas.startWaitDlg();
                                }
                            }
                            WindowInfoScr.this.idImbue = i4;
                            GameService.gI().addItemImbue(itemInInventory.itemID);
                        } else {
                            try {
                                if (MainChar.gemitemImbue.size() >= 8) {
                                    GCanvas.startOKDlg("Không thể thêm");
                                    return;
                                }
                                GemTemplate gemTemplate = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(i4 - Char.inventory.size());
                                RealID realID = new RealID(gemTemplate.rID);
                                realID.ID = gemTemplate.id;
                                if (WindowInfoScr.index[WindowInfoScr.focusTab] != 21 || MainChar.gemitemImbue.size() != 0 || (realID.ID > 7 && realID.ID != 155 && realID.ID != 156)) {
                                    if (GCanvas.gameScr.mainChar.countBaoHiem() >= 1 && gemTemplate.id < 5) {
                                        GCanvas.startOKDlg("Chỉ có thể sử dụng 1 loại bảo hiểm cho 1 lần luyện.");
                                        return;
                                    }
                                    if (GCanvas.gameScr.mainChar.countLKD() >= 1 && gemTemplate.id >= 8 && gemTemplate.id != 155 && gemTemplate.id != 156) {
                                        GCanvas.startOKDlg("Không thể sử dụng 2 loại ngọc cho 1 lần luyện.");
                                        return;
                                    }
                                    MainChar.gemitemImbue.addElement(realID);
                                    gemTemplate.number--;
                                    if (gemTemplate.number == 0) {
                                        WindowInfoScr.this.listTemp.removeElement(gemTemplate);
                                    }
                                    GameService.gI().addGemItemImbue(realID.realID, (byte) 0);
                                } else {
                                    GCanvas.startOKDlg("Phải đặt luyện kim dược trước.");
                                }
                            } catch (Exception unused) {
                            }
                        }
                    } else {
                        int i7 = i4;
                        if (i7 == 1) {
                            if (WindowInfoScr.this.idImbue != -1) {
                                GameService.gI().addItemImbue(((ItemInInventory) Char.inventory.elementAt(WindowInfoScr.this.idImbue)).itemID);
                                WindowInfoScr.this.idImbue = -1;
                                GCanvas.gameScr.mainChar.itemImbue = null;
                                WindowInfoScr.this.imgWeaponAvatar = null;
                            }
                        } else {
                            int i8 = (i6 * 2) + (i7 / 2);
                            if (i8 < MainChar.gemitemImbue.size()) {
                                RealID realID2 = (RealID) MainChar.gemitemImbue.elementAt(i8);
                                MainChar.gemitemImbue.removeElement(realID2);
                                int size = WindowInfoScr.this.listTemp.size();
                                int i9 = 0;
                                while (true) {
                                    if (i9 < size) {
                                        GemTemplate gemTemplate2 = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(i9);
                                        if (gemTemplate2.id == realID2.ID) {
                                            gemTemplate2.number++;
                                            break;
                                        }
                                        i9++;
                                    } else {
                                        GemTemplate gemTemplate3 = new GemTemplate(realID2.ID);
                                        gemTemplate3.rID = realID2.realID;
                                        WindowInfoScr.this.listTemp.addElement(gemTemplate3);
                                        break;
                                    }
                                }
                                GameService.gI().addGemItemImbue(realID2.realID, (byte) 1);
                            }
                        }
                    }
                    int i10 = WindowInfoScr.this.selected % WindowInfoScr.this.numW;
                    int i11 = WindowInfoScr.this.selected / WindowInfoScr.this.numW;
                    WindowInfoScr.this.numW = Char.inventory.size() + WindowInfoScr.this.listTemp.size();
                    if (WindowInfoScr.this.numW < 6) {
                        WindowInfoScr.this.numW = 6;
                    }
                    if (i10 >= WindowInfoScr.this.numW) {
                        i10 = WindowInfoScr.this.numW - 1;
                    }
                    WindowInfoScr windowInfoScr = WindowInfoScr.this;
                    windowInfoScr.selected = (i11 * windowInfoScr.numW) + i10;
                    WindowInfoScr.cmxLim = ((WindowInfoScr.this.numW * WindowInfoScr.this.wSmall) - WindowInfoScr.this.w) + 6;
                    if (WindowInfoScr.cmxLim < 0) {
                        WindowInfoScr.cmxLim = 0;
                    }
                    if (WindowInfoScr.cmx > WindowInfoScr.cmxLim) {
                        int i12 = WindowInfoScr.cmxLim;
                        WindowInfoScr.cmtoX = i12;
                        WindowInfoScr.cmx = i12;
                    }
                }
            }));
        }
        if (i3 == 4 || ((i4 != 1 && i5 < MainChar.gemitemImbue.size()) || (this.idImbue != -1 && i4 == 1))) {
            vector.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.doShowItemInfoImbue();
                }
            }));
        }
        GCanvas.menu.startAt(vector, 3);
    }

    public void doShowItemInfoImbue() {
        int i = this.selected;
        int i2 = this.numW;
        int i3 = i / i2;
        int i4 = i % i2;
        if (i3 != 4) {
            if (i4 == 1) {
                setShowTextNew(((ItemInInventory) Char.inventory.elementAt(this.idImbue)).getDescription(this.isSell), 145, -10);
                return;
            }
            GemTemp gemByID = Res.getGemByID(((RealID) MainChar.gemitemImbue.elementAt((i3 * 2) + (i4 / 2))).ID);
            String temp = ItemInInventory.setTemp(gemByID.name, "0");
            if ((khoaImbue != 1 || index[focusTab] != 21) && khoaKham == 1) {
                byte b = index[focusTab];
            }
            setShowTextNew(temp + ItemInInventory.setTemp(gemByID.decript, "0"), 145, -10);
            return;
        }
        if (i4 < Char.inventory.size()) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i4);
            boolean z = this.isSell;
            int i5 = this.wSmall;
            showItemInventoryInfo(itemInInventory, z, i4 * i5, i5 * 4);
            return;
        }
        int size = this.listTemp.size();
        for (int i6 = 0; i6 < size; i6++) {
            if (i6 == i4 - Char.inventory.size()) {
                GemTemp gemByID2 = Res.getGemByID(((GemTemplate) this.listTemp.elementAt(i6)).id);
                String temp2 = ItemInInventory.setTemp(gemByID2.name, "0");
                if ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25)) {
                    temp2 = temp2 + " - Đã khóa";
                }
                setShowTextNew(temp2 + ItemInInventory.setTemp(gemByID2.decript, "0"), 145, -10);
            }
        }
    }

    public boolean isBuffSkill() {
        byte[][] bArr = {new byte[]{4, 5}, new byte[]{4, 5}, new byte[]{4, 5, 6, 7}, new byte[]{4, 5}, new byte[]{4, 5}};
        for (int i = 0; i < bArr[GCanvas.gameScr.mainChar.clazz].length; i++) {
            if (this.selected == bArr[GCanvas.gameScr.mainChar.clazz][i]) {
                return true;
            }
        }
        return false;
    }

    public void doSelectedSkill() {
        if (Char.skillLevelLearnt[this.selected] == -1) {
            GCanvas.startOKDlg("Xin gặp Lâm tướng quân để học kỹ năng này");
            return;
        }
        Vector vector = new Vector();
        final boolean zIsBuffSkill = isBuffSkill();
        boolean z = false;
        if (zIsBuffSkill && SkillManager.SKILL_CAN_BUFF_TO_USER[GCanvas.gameScr.mainChar.clazz][this.selected - 4] == -1) {
            z = true;
        }
        if (zIsBuffSkill && !z) {
            vector.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    if (Char.skillLevelLearnt[WindowInfoScr.this.selected] > 0) {
                        GCanvas.menu.showMenu = false;
                        byte b = SkillManager.EFF_BUFF_SKILL[GCanvas.gameScr.mainChar.clazz][WindowInfoScr.this.selected - 4];
                        int skillMP = SkillManager.getSkillMP(WindowInfoScr.this.selected, Char.skillLevelLearnt[WindowInfoScr.this.selected - 4], GCanvas.gameScr.mainChar.clazz);
                        if (skillMP > GCanvas.gameScr.mainChar.mp) {
                            if (WindowInfoScr.this.first) {
                                return;
                            }
                            GCanvas.gameScr.addChat(new ChatInfo("", "Không đủ MP", 0));
                            WindowInfoScr.this.first = true;
                            return;
                        }
                        WindowInfoScr.this.first = false;
                        if (GCanvas.gameScr.focusedActor != null && GCanvas.gameScr.focusedActor.catagory == 0) {
                            if (WindowInfoScr.this.selected == 6) {
                                GameService.gI().useBuff(GCanvas.gameScr.focusedActor.ID, (byte) 0, b, (short) 0);
                                GCanvas.gameScr.mainChar.mp -= skillMP;
                                return;
                            }
                            GCanvas.gameScr.mainChar.mp -= skillMP;
                            if (zIsBuffSkill && (GCanvas.gameScr.indexBuff == -1 || GCanvas.gameScr.indexBuff == WindowInfoScr.this.selected)) {
                                GCanvas.gameScr.indexBuff = WindowInfoScr.this.selected;
                            }
                            WindowInfoScr.this.doCastBuffToActor(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, b, SkillManager.SKILL_CAN_BUFF_TO_USER[GCanvas.gameScr.mainChar.clazz][WindowInfoScr.this.selected + (-4)] == 1);
                            return;
                        }
                        if (WindowInfoScr.this.selected != 6) {
                            GCanvas.gameScr.mainChar.mp -= skillMP;
                            if (zIsBuffSkill && (GCanvas.gameScr.indexBuff == -1 || GCanvas.gameScr.indexBuff == WindowInfoScr.this.selected)) {
                                GCanvas.gameScr.indexBuff = WindowInfoScr.this.selected;
                            }
                            GameService.gI().useBuff(GCanvas.gameScr.mainChar.ID, (byte) 0, b, (short) 0);
                            return;
                        }
                        GCanvas.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
                        return;
                    }
                    GCanvas.startOKDlg("Chưa học kỹ năng này");
                }
            }));
        }
        if (!z) {
            vector.addElement(new Command("Cho vào phím tắt", new IAction() {
                @Override
                public void perform() {
                    if (Char.skillLevelLearnt[WindowInfoScr.this.selected] > 0) {
                        WindowInfoScr windowInfoScr = WindowInfoScr.this;
                        windowInfoScr.doGiveSkillToQuickSlot(windowInfoScr.selected, zIsBuffSkill);
                    } else {
                        GCanvas.startOKDlg("Chưa học kỹ năng này");
                    }
                }
            }));
        }
        vector.addElement(new Command("Cộng", new IAction() {
            @Override
            public void perform() {
                if (GCanvas.gameScr.mainChar.skillPointLeft == 0) {
                    GCanvas.startOKDlg("Đã hết điểm kỹ năng để cộng. Xin đánh lên level để có điểm kỹ năng.");
                } else if (GCanvas.gameScr.mainChar.level < SkillManager.getLvAddSkill(GCanvas.gameScr.mainChar.clazz, WindowInfoScr.this.selected, Char.skillLevelLearnt[WindowInfoScr.this.selected])) {
                    GCanvas.startOKDlg("Chưa đủ cấp độ để cộng. Xin đánh lên level yêu cầu để có thể cộng.");
                } else {
                    GameService.gI().addSkillPoint(WindowInfoScr.this.selected);
                    GCanvas.startWaitDlg("Xin chờ..", true);
                }
            }
        }));
        GCanvas.menu.startAt(vector, 2);
    }

    protected void doGiveSkillToQuickSlot(final int i, final boolean z) {
        Vector vector = new Vector();
        for (int i20 = 0; i20 < 3; i20++) {
            final int i2 = i20;
            vector.addElement(new Command("Phím số " + ((i2 * 2) + 1), new IAction() {
                @Override
                public void perform() {
                    MainChar.quickSlot[GameScr.indexTabSlot][i2].setIsSkill(i, GCanvas.gameScr.mainChar.clazz, z);
                    RMS.saveQuickSlot();
                }
            }));
        }
        GCanvas.menu.startAt(vector, 2);
    }

    public void doCastBuffToActor(final MainChar mainChar, final Actor actor, final int i, boolean z) {
        Vector vector = new Vector();
        if (z) {
            vector.addElement(new Command("Cho mình", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().useBuff(mainChar.ID, (byte) 0, (byte) i, (short) 0);
                }
            }));
            vector.addElement(new Command("Cho bạn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().useBuff(actor.ID, (byte) 0, (byte) i, (short) 0);
                }
            }));
        } else {
            GameService.gI().useBuff(mainChar.ID, (byte) 0, (byte) i, (short) 0);
        }
        GCanvas.menu.startAt(vector, 2);
    }

    public void setCmdCenter() {
        this.charInfo = null;
        byte b = index[focusTab];
        switch (b) {
            case 1:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.showCharWearingInfo();
                    }
                });
                break;
            case 2:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.dofireBasePoint();
                    }
                });
                this.left = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.dofireBasePoint();
                    }
                });
                break;
            case 3:
                int length = ((GCanvas.gameScr.mainChar.getInfoSkill(this.selected).length + 1) * 12) - 72;
                cmyLim = length;
                if (length < 0) {
                    cmyLim = 0;
                }
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedSkill();
                    }
                });
                this.left = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedSkill();
                    }
                });
                break;
            case 4:
                setTextCharInfo();
                break;
            case 5:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedParty();
                    }
                });
                this.left = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedParty();
                    }
                });
                break;
            case 6:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedQuest();
                    }
                });
                this.left = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedQuest();
                    }
                });
                break;
            case 7:
                this.newSkill = new Vector();
                final Vector vector = (Vector) GameScr.infoNewSkill.elementAt(GCanvas.gameScr.mainChar.clazz);
                byte b2 = Char.skillLevelLearnt[((InfoSkillLearn) vector.elementAt(this.selected)).idSkill];
                this.newSkill.addElement("`" + ((InfoSkillLearn) vector.elementAt(this.selected)).name);
                if (b2 == -1) {
                    this.newSkill.addElement("`Phí: " + ((InfoSkillLearn) vector.elementAt(this.selected)).price + " xu");
                }
                for (String str : Font.arialFont11.splitFontBStrInLine(((InfoSkillLearn) vector.elementAt(this.selected)).decript, 110)) {
                    this.newSkill.addElement(str);
                }
                setSize(114, 30, vector.size(), 2, 30, 20);
                int size = (this.newSkill.size() * 12) - 72;
                cmyLim = size;
                if (size < 0) {
                    cmyLim = 0;
                }
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        InfoSkillLearn infoSkillLearn = (InfoSkillLearn) vector.elementAt(WindowInfoScr.this.selected);
                        GCanvas.startYesNoDlg("Bạn muốn học kỹ năng " + infoSkillLearn.name + " với giá " + infoSkillLearn.price + "xu không? ", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().learnSkill(GCanvas.gameScr.mainChar.clazz, WindowInfoScr.this.selected);
                                GCanvas.startWaitDlg();
                            }
                        });
                    }
                });
                break;
            case 8:
                break;
            default:
                switch (b) {
                    case 21:
                        break;
                    case 22:
                    case 23:
                        setSelectedKeepAndSellItem();
                        break;
                    default:
                        switch (b) {
                            case 25:
                                break;
                            case 26:
                            case 27:
                            case 29:
                            case 30:
                            case 32:
                                break;
                            case 28:
                                setLimitCheDo();
                                break;
                            case 31:
                                this.center = new Command("", new IAction() {
                                    @Override
                                    public void perform() {
                                        if (WindowInfoScr.charWearing != null) {
                                            if (WindowInfoScr.charWearing.useHorse != -1 || WindowInfoScr.charWearing.idhorse > -1) {
                                                WindowInfoScr.this.showInfoWearingAnimal();
                                            }
                                        }
                                    }
                                });
                                break;
                            default:
                                int i = this.selected + (b == 0 ? this.countTabMyitem * 42 : 0);
                                this.center = null;
                                if (i < this.list.size()) {
                                    this.center = (Command) this.list.elementAt(i);
                                    this.center.action.perform();
                                }
                                break;
                        }
                        break;
                }
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.setSelectedImbue();
                    }
                });
                break;
        }
    }

    public void setLimitCheDo() {
        int i = this.selected;
        int i2 = this.numW;
        int i3 = i % i2;
        if (i / i2 == 1) {
            int size = this.listEp.size() / 2;
            if (size < 6) {
                size = 6;
            }
            this.numW = size;
            if (size < 6) {
                this.numW = 6;
            }
            int i4 = this.numW;
            if (i3 >= i4) {
                i3 = i4 - 1;
            }
            this.selected = i4 + i3;
            cmx = -10;
        } else if (i / i2 == 2) {
            int size2 = this.listTemp.size();
            this.numW = size2;
            if (size2 < 6) {
                this.numW = 6;
            }
            int i5 = this.numW;
            if (i3 >= i5) {
                i3 = i5 - 1;
            }
            this.selected = (i5 * 2) + i3;
        }
        setLim();
        int i6 = this.numW * this.wSmall;
        cmxLim = i6;
        if (cmx < 0) {
            cmx = -10;
        }
        if (cmx > i6) {
            int i7 = i6 + 30;
            cmtoX = i7;
            cmx = i7;
        }
    }

    protected void doSelectedTrade() {
        Vector vector = new Vector();
        if (this.selected < Char.inventory.size()) {
            final ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(this.selected);
            Vector tItems = GCanvas.gameScr.mainChar.tItems;
            int size = tItems.size();
            boolean daCo = false;
            for (int k = 0; k < size; k++) {
                if ((tItems.elementAt(k) instanceof ItemInInventory) && ((ItemInInventory) tItems.elementAt(k)).itemID == itemInInventory.itemID) {
                    daCo = true;
                    break;
                }
            }
            if (!daCo) {
                vector.addElement(new Command("Giao dịch", new IAction() {
                    @Override
                    public void perform() {
                        if (GCanvas.gameScr.mainChar.tItems.size() >= 12) {
                            GCanvas.startOKDlg("Không thể thêm.");
                        } else {
                            if (itemInInventory.isSelling) {
                                return;
                            }
                            GCanvas.gameScr.gameService.sendAddItemTrade(itemInInventory.itemID, 0, 0);
                        }
                    }
                }));
            }
        } else {
            int target = this.selected - Char.inventory.size();
            int count = 0;
            for (int k = 0; k < MainChar.listPotion.length; k++) {
                if (MainChar.listPotion[k].number - MainChar.listPotion[k].numTrade > 0 && MainChar.listPotion[k].isTrade) {
                    if (count == target) {
                        final int ii = k;
                        final int co = count;
                        vector.addElement(new Command("Giao dịch", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.inputDlg.setInfo("Số lượng", new IAction() {
                                    @Override
                                    public void perform() {
                                        try {
                                            int i3 = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                                            if (i3 <= 0 || i3 > MainChar.listPotion[ii].number - MainChar.listPotion[ii].numTrade) {
                                                return;
                                            }
                                            GCanvas.gameScr.gameService.sendAddItemTrade((short) ii, 1, i3);
                                            GCanvas.endDlg();
                                        } catch (Exception unused) {
                                        }
                                    }
                                }, 3, 10, true);
                                GCanvas.inputDlg.show();
                            }
                        }));
                        vector.addElement(new Command("Thông tin", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr self = WindowInfoScr.this;
                                self.setShowTextNew(self.getPotionInfo(ii), (co % WindowInfoScr.this.numW) * 18, (co / WindowInfoScr.this.numW) * 18);
                            }
                        }));
                        break;
                    }
                    count++;
                }
            }
        }
        GCanvas.menu.startAt(vector, 3);
        if (GCanvas.isPointer(25, 55, 132, 110)) {
            Vector vector3 = new Vector();
            int i3 = this.selectedGD;
            int i4 = i3 % 6;
            Object it = null;
            if (i4 <= 2) {
                final int cc = ((i3 / 6) * 3) + i4;
                final Vector tItem = GCanvas.gameScr.mainChar.tItems;
                if (cc < tItem.size()) {
                    it = tItem.elementAt(cc);
                    final Object huy = it;
                    vector3.addElement(new Command("Hủy", new IAction() {
                        @Override
                        public void perform() {
                            if (tItem.elementAt(cc) instanceof ItemInInventory) {
                                GCanvas.gameScr.gameService.sendAddItemTrade(((ItemInInventory) huy).itemID, 0, 0);
                            } else {
                                GCanvas.gameScr.gameService.sendAddItemTrade(((Potion) huy).id, 2, 0);
                            }
                        }
                    }));
                }
            } else {
                int i6 = ((i3 / 6) * 3) + (i4 - 3);
                if (i6 < GCanvas.gameScr.mainChar.rItems.size()) {
                    it = GCanvas.gameScr.mainChar.rItems.elementAt(i6);
                }
            }
            if (it != null) {
                final Object info = it;
                final int x1 = i4 * 22;
                final int y1 = (this.selectedGD / 6) * 22;
                vector3.addElement(new Command("Thông tin", new IAction() {
                    @Override
                    public void perform() {
                        Object obj = info;
                        if (obj instanceof ItemInInventory) {
                            WindowInfoScr self = WindowInfoScr.this;
                            self.showItemInventoryInfo((ItemInInventory) obj, self.isSell, x1, y1);
                            return;
                        }
                        for (int i9 = 0; i9 < MainChar.listPotion.length; i9++) {
                            if (MainChar.listPotion[i9].index == ((Potion) info).index) {
                                WindowInfoScr self2 = WindowInfoScr.this;
                                self2.setShowTextNew(self2.getPotionInfo(0), x1, y1);
                                return;
                            }
                        }
                    }
                }));
            }
            GCanvas.menu.startAt(vector3, 3);
        }
    }

    protected void doSelectedQuest() {
        if (GCanvas.gameScr.mainChar.receiveQuest) {
            GCanvas.startYesNoDlg("Bạn thật sự muốn từ bỏ nhiệm vụ này?", new IAction() {
                @Override
                public void perform() {
                    GCanvas.gameScr.mainChar.resetQuest();
                    GCanvas.gameScr.dellPotionQuest();
                    GameService.gI().receiveQuest(0, 0, 3);
                    GCanvas.startWaitDlg();
                }
            });
        }
    }

    protected void doSelectedParty() {
        if (Char.party.size() > 0) {
            MainChar mainChar = GCanvas.gameScr.mainChar;
            Vector vector = new Vector();
            if (mainChar.ID == mainChar.IDMasterParty) {
                vector.addElement(new Command("Đuổi", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.gameService.kickOutParty(((PartyInfo) Char.party.elementAt(WindowInfoScr.this.selected)).id, (byte) 0);
                    }
                }));
                vector.addElement(new Command("Giải tán", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.gameService.kickOutParty(0, (byte) 1);
                    }
                }));
            } else {
                vector.addElement(new Command("Rời nhóm", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.gameService.kickOutParty(GCanvas.gameScr.mainChar.ID, (byte) 2);
                    }
                }));
            }
            GCanvas.menu.startAt(vector, 2);
        }
    }

    private void setTextCharInfo() {
        this.charInfo = new String[15];
        MainChar mainChar = GCanvas.gameScr.mainChar;
        this.charInfo[0] = "Nhân vật: " + mainChar.name;
        this.charInfo[1] = "Level: " + ((int) mainChar.level) + "+" + mainChar.getPercent();
        this.charInfo[2] = "HP: " + mainChar.hp + "/" + mainChar.maxhp;
        this.charInfo[3] = "MP: " + mainChar.mp + "/" + mainChar.maxmp;
        this.charInfo[4] = "Tấn công: " + mainChar.getAttack();
        this.charInfo[5] = "Thủ vật lý: " + mainChar.defend;
        this.charInfo[6] = "Thủ ma pháp: " + mainChar.defend_magic;
        this.charInfo[7] = "Chính xác: " + ((int) mainChar.accurate);
        this.charInfo[8] = "Né tránh: " + ((int) mainChar.dodge);
        this.charInfo[9] = "Bạo kích: " + (mainChar.baokich > 0 ? (mainChar.baokich / 10) + "." + (mainChar.baokich % 10) : "0") + "%";
        this.charInfo[10] = "Chí mạng: " + ((int) mainChar.critical);
        this.charInfo[11] = "Cống hiến: " + mainChar.dedicationPoint + " điểm.";
        this.charInfo[12] = "Liên trảm: " + mainChar.lienTram + " điểm.";
        this.charInfo[13] = "Công trạng: " + mainChar.congTrang + " điểm.";
        int length = this.charInfo.length + 1;
        this.numH = length;
        String[] strArr = infoMainCharServer;
        if (strArr != null) {
            this.numH = length + strArr.length;
        }
        setLim();
    }

    private void setCamEpDoThu() {
        if (!GCanvas.isPointerClick) {
            return;
        }
        int i = 0;
        while (true) {
            short[][] sArr = this.posEpNgoc;
            if (i >= sArr.length) {
                return;
            }
            short[] sArr2 = sArr[i];
            if (GCanvas.isPointer(sArr2[0] + 21, sArr2[1] + 62, 23, 23)) {
                GCanvas.isPointerClick = false;
                this.selected = i;
                if (this.numH != 1) {
                    this.numH = 1;
                    this.numW = 5;
                    cmtoY = 0;
                    cmtoX = 0;
                    cmy = 0;
                    cmx = 0;
                }
                doSelectedEpDothu();
            }
            i++;
        }
    }

    private void setCamEpNgoc() {
        if (!GCanvas.isPointerClick) {
            return;
        }
        int i = 0;
        while (true) {
            short[][] sArr = this.posEpNgoc;
            if (i >= sArr.length) {
                return;
            }
            short[] sArr2 = sArr[i];
            if (GCanvas.isPointer(sArr2[0] + 21, sArr2[1] + 62, 23, 23)) {
                GCanvas.isPointerClick = false;
                this.selected = i;
                if (this.numH != 1) {
                    this.numH = 1;
                    this.numW = 5;
                    cmtoY = 0;
                    cmtoX = 0;
                    cmy = 0;
                    cmx = 0;
                }
            }
            i++;
        }
    }

    private void updateKeyMain() {
        byte b = index[focusTab];
        boolean z;
        if (b == 1) {
            z = false;
            if (GCanvas.isPointerClick && GCanvas.px > 17) {
                int ws = this.wSmall;
                if (GCanvas.px <= (ws * 5) + 17) {
                    if (GCanvas.isPointer(ws + 21, (this.hSmall * 4) + 57, 28, 28)) {
                        this.selected = 4;
                        this.isClick = true;
                        if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                            this.center.action.perform();
                        }
                    } else if (GCanvas.isPointer((this.wSmall * 3) + 15, (this.hSmall * 4) + 57, 28, 28)) {
                        this.selected = 1;
                        this.isClick = true;
                        if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                            this.center.action.perform();
                        }
                    } else if (GCanvas.px > 17 && GCanvas.px <= 47) {
                        int row = (GCanvas.py - 57) / this.hSmall;
                        int col = (GCanvas.px - 17) / this.wSmall;
                        this.selected = (row * this.numW) + col;
                        this.isClick = true;
                        if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                            this.center.action.perform();
                        }
                    } else if (GCanvas.isPointer((this.wSmall * 4) + 15, 57, 28, this.hSmall * 5)) {
                        this.selected = (((GCanvas.py - 57) / this.hSmall) * this.numW) + 2;
                        this.isClick = true;
                        if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                            this.center.action.perform();
                        }
                    }
                }
            }
        } else if (b == 28) {
            if (GCanvas.isPointerDown && GCanvas.isPointer(15, 89, this.w + 10, 113)) {
                if (!this.trans) {
                    this.pa = cmx;
                    this.trans = true;
                }
                if (Math.abs(GCanvas.pxLast - GCanvas.px) > 10) {
                    int i6 = this.pa + (GCanvas.pxLast - GCanvas.px);
                    cmtoX = i6;
                    if (i6 < -10) {
                        cmtoX = -10;
                    }
                    if (cmtoX > cmxLim) {
                        cmtoX = cmxLim;
                    }
                }
                int i9 = (((GCanvas.py - 89) / this.hSmall) * this.numW) + (((cmtoX + GCanvas.px) - 15) / this.wSmall);
                this.selected = i9;
                if (i9 < 0) {
                    this.selected = 0;
                }
            }
            if (GCanvas.isPointerClick && GCanvas.isPointer(15, 89, this.w + 10, 113)) {
                this.xCus = -20;
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
            z = false;
        } else if (b == 7) {
            if (GCanvas.isPointerClick && GCanvas.isPointer(15, 55, 150, 140) && !GCanvas.menu.showMenu) {
                int i10 = (GCanvas.py - 65) / (this.hSmall + 10);
                int i11 = (GCanvas.px - 14) / this.wSmall;
                int i13 = this.numW;
                if (this.selected == (i10 * i13) + i11) {
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                        setCmdCenter();
                    }
                } else {
                    int i14 = (i10 * i13) + i11;
                    this.selected = i14;
                    if (i14 > i13 - 1) {
                        this.selected = i13 - 1;
                    }
                    setCmdCenter();
                }
                int i16 = this.numW;
                if (this.selected > i16 - 1) {
                    this.selected = i16 - 1;
                }
            }
            z = false;
        } else if (b == 8) {
            if (GCanvas.isPointerClick && GCanvas.isPointer(25, 55, 132, 110) && !GCanvas.menu.showMenu) {
                int sel = (((GCanvas.py - 65) / this.hSmall) * 6) + ((GCanvas.px - 35) / this.wSmall);
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.selectedGD == sel) {
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                } else {
                    this.selectedGD = sel;
                }
                z = true;
            } else if (GCanvas.isPointer(25, 165, 132, 30) && !GCanvas.menu.showMenu) {
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pa = cmx;
                        this.trans = true;
                    }
                    if (Math.abs(GCanvas.pxLast - GCanvas.px) > 10) {
                        int x1 = this.pa + (GCanvas.pxLast - GCanvas.px);
                        cmtoX = x1;
                        if (x1 < -10) {
                            cmtoX = -10;
                        }
                        if (cmtoX > cmxLim) {
                            cmtoX = cmxLim;
                        }
                    }
                    this.selected = ((cmtoX + GCanvas.px) - 25) / this.wSmall;
                    z = true;
                } else {
                    if (GCanvas.isPointerClick) {
                        this.xCus = -20;
                        this.isChangeTab = false;
                        GCanvas.isPointerClick = false;
                        this.trans = false;
                        if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                            setCmdCenter();
                        }
                    }
                    z = false;
                }
            } else {
                z = false;
            }
        } else if (b == 25 || b == 21) {
            if (GCanvas.isPointerDown && GCanvas.isPointer(15, 65, this.w + 10, 137)) {
                boolean r;
                if (!this.trans) {
                    this.pa = cmx;
                    this.trans = true;
                    int row = (GCanvas.py - 65) / this.hSmall;
                    int col = ((cmtoX + GCanvas.px) - 24) / this.wSmall;
                    if (row == 5) {
                        row = 4;
                    }
                    if (row < 4) {
                        this.numW = 3;
                        this.wSmall = 20;
                        col = (GCanvas.px - 24) / this.wSmall;
                    } else if (row == 4) {
                        this.wSmall = 24;
                    }
                    byte t = index[focusTab];
                    if (t == 3) {
                        r = false;
                    } else {
                        r = t != 32;
                        setCmdCenter();
                        byte t2;
                        if (row != 4 && row != 5 && ((t2 = index[focusTab]) == 21 || t2 == 25)) {
                            setUpdatePointerImbue(col, row);
                        } else {
                            byte t3 = index[focusTab];
                            if ((t3 == 21 || t3 == 25) && row == 4 && this.selected / this.numW != 4) {
                                this.selected = 10;
                                setSizeImbue(1);
                            }
                            if (index[focusTab] != 32) {
                                this.selected = (row * this.numW) + col;
                            }
                        }
                    }
                } else {
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) > 10) {
                        cmtoY = this.pb + (GCanvas.pyLast - GCanvas.py);
                    }
                    r = false;
                }
                int x1 = cmx + (GCanvas.pxLast - GCanvas.px);
                cmtoX = x1;
                if (cmtoY < -10) {
                    cmtoY = -10;
                }
                int yl = cmyLim;
                if (cmtoY > yl + 10) {
                    cmtoY = yl + 10;
                }
                if (x1 < -10) {
                    cmtoX = -10;
                }
                int xl = cmxLim;
                if (cmtoX > xl + 10) {
                    cmtoX = xl + 10;
                }
                z = r;
            } else {
                z = false;
            }
            if (GCanvas.isPointerClick && GCanvas.isPointer(15, 65, this.w + 10, 137)) {
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        } else if (b == 26) {
            if (b == 26) {
                setCamEpNgoc();
            }
            if (GCanvas.isPointerDown && GCanvas.isPointer(15, 65, this.w + 10, 137)) {
                boolean r;
                if (this.trans) {
                    r = false;
                } else {
                    this.pa = cmx;
                    this.trans = true;
                    int i19 = (GCanvas.py - 65) / this.hSmall;
                    int i20 = ((cmtoX + GCanvas.px) - 24) / this.wSmall;
                    r = index[focusTab] != 32;
                    setCmdCenter();
                    if (i19 == 5 && this.numH != 5 && index[focusTab] == 26) {
                        this.selected = 3;
                        setSizeEpNgoc(1, false);
                    } else if (i19 == 5 && this.numH != 5 && index[focusTab] == 32) {
                        this.selected = 3;
                        setSizeEpNgoc(1, true);
                    } else {
                        this.selected = (i19 * this.numW) + i20;
                    }
                }
                int i21 = this.pa + (GCanvas.pxLast - GCanvas.px);
                cmtoX = i21;
                if (i21 < 10) {
                    cmtoX = 10;
                }
                int i23 = cmxLim;
                if (cmtoX > i23) {
                    cmtoX = i23 - 10;
                }
                z = r;
            } else {
                z = false;
            }
            if (GCanvas.isPointerClick && GCanvas.isPointer(15, 65, this.w + 10, 137)) {
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        } else if (b == 31) {
            if (GCanvas.isPointerClick) {
                if (GCanvas.px > 17 && GCanvas.px <= 47) {
                    int sel = ((GCanvas.py - 57) / this.hSmall) * 2;
                    this.selected = sel;
                    if (sel < 0) {
                        this.selected = 0;
                    }
                    if (this.selected > 8) {
                        this.selected = 8;
                    }
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && charWearing != null
                            && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
                        showInfoWearingAnimal();
                    }
                } else if (GCanvas.px > (this.wSmall * 4) + 17 && GCanvas.px <= (this.wSmall * 4) + 47) {
                    int row = (GCanvas.py - 57) / this.hSmall;
                    this.isClick = true;
                    int sel = (row * 2) + 1;
                    this.selected = sel;
                    if (sel > 9) {
                        this.selected = 9;
                    }
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && charWearing != null
                            && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
                        showInfoWearingAnimal();
                    }
                }
            }
            z = false;
        } else if (b == 32) {
            if (b == 32) {
                if (GCanvas.py >= this.posEpNgoc[1][1] + 65 && GCanvas.py <= this.posEpNgoc[3][1] + 95) {
                    setCamEpDoThu();
                } else if (GCanvas.isPointer(25, this.posEpNgoc[3][1] + 95, 120, 30)) {
                    GCanvas.isPointerClick = false;
                    int row = (GCanvas.py - (this.posEpNgoc[3][1] + 103)) / this.hSmall;
                    int col = (GCanvas.px - 25) / 20;
                    int sel = (row * 6) + col + 6;
                    this.selected = sel;
                    if (sel < 6) {
                        this.selected = 6;
                    } else if (sel > 11) {
                        this.selected = 11;
                    }
                    if (this.numH != 1) {
                        this.numH = 1;
                        this.numW = 6;
                    }
                    doSelectedEpDothu();
                }
            }
            if (GCanvas.isPointerDown && GCanvas.isPointer(14, 170, this.w + 30, 30)) {
                if (!this.trans) {
                    this.pa = cmx;
                    this.trans = true;
                    int row = (GCanvas.py - 75) / this.hSmall;
                    int col = ((cmtoX + GCanvas.px) - 24) / this.wSmall;
                    setCmdCenter();
                    if (row == 5 && this.numH != 5 && index[focusTab] == 32) {
                        this.selected = 3;
                        setSizeEpNgoc(1, true);
                    }
                }
                if (index[focusTab] == 32) {
                    cmtoX = this.pa + (GCanvas.pxLast - GCanvas.px);
                }
                if (cmtoX < 0) {
                    cmtoX = 10;
                }
                int xl = cmxLim;
                if (cmtoX > xl) {
                    cmtoX = xl - 20;
                }
            }
            if (GCanvas.isPointerClick && GCanvas.isPointer(14, 170, this.w + 30, 30)) {
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    int row = (GCanvas.py - 75) / this.hSmall;
                    int col = ((cmtoX + GCanvas.px) - 24) / this.wSmall;
                    this.selected = ((row - 1) * this.numW) + col;
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
            z = false;
        } else if (b == 22 || b == 23) {
            if (GCanvas.isPointerDown && GCanvas.isPointer(15, 50, 140, 140)) {
                if (!this.trans) {
                    this.pb = cmy;
                    this.trans = true;
                }
                int i24 = this.pb + (GCanvas.pyLast - GCanvas.py);
                cmtoY = i24;
                if (i24 < -10) {
                    cmtoY = -10;
                }
                int i26 = cmyLim;
                if (cmtoY > i26 + 10) {
                    cmtoY = i26 + 10;
                }
            }
            if (GCanvas.isPointerClick && GCanvas.isPointer(15, 50, 140, 140)) {
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                int i27 = ((cmtoY + GCanvas.py) - 55) / this.hSmall;
                int i28 = (GCanvas.px - 20) / this.wSmall;
                if (this.selected == (this.numW * i27) + i28 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    setLeftKeepAndSellItem();
                } else {
                    this.selected = (i27 * this.numW) + i28;
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
                z = true;
            } else {
                z = false;
            }
        } else if (this.isDefault) {
            if (GCanvas.isPointer(10, 55, this.w + 20, 113)) {
                int i29 = GCanvas.pyLast - GCanvas.py;
                int i30 = GCanvas.pxLast - GCanvas.px;
                boolean r;
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pb = cmy;
                        this.trans = true;
                    }
                    if (Math.abs(i29) > 10) {
                        int i31 = this.pb + (GCanvas.pyLast - GCanvas.py);
                        cmtoY = i31;
                        if (i31 < -10) {
                            cmtoY = -10;
                        }
                        int i33 = cmyLim;
                        if (cmtoY > i33) {
                            cmtoY = i33;
                        }
                    }
                    r = true;
                } else {
                    r = false;
                }
                if (GCanvas.isPointerClick) {
                    this.isClick = true;
                    this.xCus = -20;
                    this.isChangeTab = false;
                    GCanvas.isPointerClick = false;
                    this.trans = false;
                    int i34 = ((((cmtoY + GCanvas.py) - 65) / (this.hSmall + 10)) * 5) + ((GCanvas.px - 24) / (this.wSmall + 10));
                    if (this.selected == i34 && Math.abs(i29) < 10 && Math.abs(i30) < 10) {
                        byte b6 = index[focusTab];
                        if (b6 == 0) {
                            doSelectedInventori();
                        } else if (b6 == 24) {
                            setLeftBuyItem();
                        } else {
                            switch (b6) {
                                case 9:
                                    doSetLeftSellWeapon(-1);
                                    break;
                                case 10:
                                    dosetLeftSellGemItem();
                                    break;
                                case 11:
                                case 12:
                                case 13:
                                    doSetLeftSellWeapon(b6 - 11);
                                    break;
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                    doSetLeftSellWeapon(b6 - 6);
                                    break;
                                case 19:
                                    doSetLeftPotion();
                                    break;
                            }
                        }
                    } else {
                        this.selected = i34;
                        if (i34 > 41 && index[focusTab] == 0) {
                            this.selected = 41;
                        }
                        setCmdCenter();
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                    }
                }
                int i35 = cmy;
                int i36 = cmtoY;
                if (i35 != i36) {
                    int i37 = (i36 - i35) << 2;
                    cmvy = i37;
                    int i38 = cmdy + i37;
                    cmy = i35 + (i38 >> 4);
                    cmdy = i38 & 15;
                }
                z = r;
            } else {
                z = false;
            }
        } else if (GCanvas.isPointer((this.x + this.x0) - 7, (this.y + this.y0) - 10, this.w + 20, this.h + 20)) {
            int i39 = GCanvas.pyLast - GCanvas.py;
            int i40 = GCanvas.pxLast - GCanvas.px;
            if (!GCanvas.isPointerDown || index[focusTab] == 1) {
                z = false;
            } else {
                boolean r;
                if (this.trans) {
                    r = false;
                } else {
                    this.pb = cmy;
                    this.pa = cmx;
                    this.trans = true;
                    int i41 = ((cmtoY + GCanvas.py) - (this.y + this.y0)) / this.hSmall;
                    int i42 = ((cmtoX + GCanvas.px) - (this.x + this.x0)) / this.wSmall;
                    r = index[focusTab] != 32;
                    setCmdCenter();
                    byte b7 = index[focusTab];
                    if (b7 != 3 && b7 != 32) {
                        this.selected = (i41 * this.numW) + i42;
                    }
                }
                this.isTab3 = true;
                this.isshowInfo = true;
                cmtoY = this.pb + (GCanvas.pyLast - GCanvas.py);
                int i43 = this.pa + (GCanvas.pxLast - GCanvas.px);
                cmtoX = i43;
                if (cmtoY < -10) {
                    cmtoY = -10;
                }
                int i45 = cmyLim;
                if (cmtoY > i45 + 10) {
                    cmtoY = i45 + 10;
                }
                if (i43 < 10) {
                    cmtoX = 10;
                }
                int i47 = cmxLim;
                if (cmtoX > i47) {
                    cmtoX = i47 - 10;
                }
                z = r;
            }
            if (GCanvas.isPointerClick) {
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                if (Math.abs(i39) < 10 && Math.abs(i40) < 10 && index[focusTab] != 3) {
                    setCmdCenter();
                    if (this.center != null) {
                        this.center.action.perform();
                    }
                }
            }
        } else {
            z = false;
        }
        if (!GCanvas.isPointerDown) {
            this.isshowInfo = false;
        }
        if (z && !this.isDefault) {
            byte t = index[focusTab];
            if (t != 3) {
                int sel = this.selected;
                int nw = this.numW;
                int ty = ((sel / nw) * this.hSmall) - (this.h / 2);
                cmtoY = ty;
                if (ty < 0) {
                    cmtoY = 0;
                }
                int yl = cmyLim;
                if (cmtoY > yl) {
                    cmtoY = yl;
                }
                if (t != 32) {
                    cmtoX = ((sel % nw) * this.wSmall) - (this.w / 2);
                } else if (sel > 4) {
                    cmtoX = ((sel % nw) * this.wSmall) - (this.w / 2);
                }
                if (cmtoX < 0) {
                    cmtoX = 0;
                }
                int xl = cmxLim;
                if (cmtoX > xl) {
                    cmtoX = xl;
                }
            }
        }
    }

    private void setUpdatePointerImbue(int i, int i2) {
        if (i > 0 && i < 5) {
            this.selected = 1;
            return;
        }
        if (i == 5) {
            i = 2;
        }
        this.selected = (i2 * this.numW) + i;
    }

    private void setSizeEpNgoc(int i, boolean z) {
        if (i == -1) {
            if (this.numH != 1) {
                this.numH = 1;
                this.selected = 4;
                this.numW = 5;
                cmtoY = 0;
                cmtoX = 0;
                cmy = 0;
                cmx = 0;
                return;
            }
            int i2 = this.selected;
            if (i2 == 2) {
                this.selected = 0;
                return;
            } else if (i2 == 3) {
                this.selected = 2;
                return;
            } else {
                if (i2 == 4) {
                    this.selected = 1;
                    return;
                }
                return;
            }
        }
        int i3 = this.selected;
        if (i3 != 3 && i3 != 4) {
            if (i3 == 2) {
                this.selected = 3;
                return;
            } else if (i3 == 0) {
                this.selected = 2;
                return;
            } else {
                if (i3 == 1) {
                    this.selected = 4;
                    return;
                }
                return;
            }
        }
        if (!z) {
            int size = this.listTemp.size();
            this.numW = size;
            if (size < 6) {
                this.numW = 6;
            }
            this.numH = 5;
            int i4 = this.numW;
            this.selected = (5 - 1) * i4;
            int i5 = (i4 * 20) - (this.w - 8);
            cmxLim = i5;
            if (i5 < 0) {
                cmxLim = 0;
            }
            cmtoY = 0;
            cmtoX = 0;
            cmy = 0;
            cmx = 0;
            return;
        }
        resetEpdothu(-1);
    }

    public boolean setSizeKeepItem(int i) {
        int size;
        int i2 = cmy;
        if (index[focusTab] == 22) {
            size = Char.itembag.size();
        } else {
            size = this.sellItem.size();
        }
        if (MainChar.inventory.size() + MainChar.gemItem.size() < size) {
            this.numH = (size / 3) + 1;
        } else {
            this.numH = ((MainChar.inventory.size() + MainChar.gemItem.size()) / 3) + 1;
        }
        if (this.numH < 5) {
            this.numH = 5;
        }
        int i3 = this.selected;
        int i4 = this.numW;
        if ((i3 % i4 == 2 && i == 1) || (i3 % i4 == 3 && i == -1)) {
            if (i == -1) {
                cmyLim = ((((MainChar.inventory.size() + MainChar.gemItem.size()) / 3) + 1) * this.wSmall) - index[focusTab] == 22 ? this.h : this.h + 22;
            } else {
                int i5 = (((size / 3) + 1) * this.wSmall) - index[focusTab];
                int i6 = this.h;
                if (i5 != 22) {
                    i6 += 22;
                }
                cmyLim = i6;
            }
            if (cmyLim < 0) {
                cmyLim = 0;
            }
            int i7 = this.selected;
            int i8 = this.numW;
            int i9 = cmy;
            int i10 = this.wSmall;
            int i11 = (i7 / i8) - (i9 / i10);
            int i12 = this.cmyKeep;
            cmtoY = i12;
            cmy = i12;
            this.selected = (((i12 / i10) + i11) * i8) + (i7 % i8);
            this.cmyKeep = i2;
            return true;
        }
        int i13 = (this.numH * this.wSmall) - index[focusTab] == 22 ? this.h : this.h + 22;
        cmyLim = i13;
        if (i13 < 0) {
            cmyLim = 0;
        }
        return false;
    }

    private void setSizeTrade(int i) {
        int i2 = this.selected;
        int i3 = this.numW;
        int i4 = (i2 % i3) - (cmx / 22);
        if (i4 < 0) {
            i4 = 0;
        }
        int i5 = i2 / i3;
        if ((i2 / i3) + 2 == this.numH && i == 1) {
            int i6 = 0;
            for (int i7 = 0; i7 < MainChar.listPotion.length; i7++) {
                if (MainChar.listPotion[i7].number - MainChar.listPotion[i7].numTrade > 0 && MainChar.listPotion[i7].isTrade) {
                    i6++;
                }
            }
            int size = Char.inventory.size() + i6;
            this.numW = size;
            if (size < 6) {
                this.numW = 6;
            }
            cmxLim = (this.numW * 22) - this.w;
        } else {
            this.numW = 6;
            cmxLim = 0;
        }
        int i8 = this.numW;
        if (i4 >= i8) {
            i4 = i8 - 1;
        }
        this.selected = (i5 * i8) + i4;
    }

    private void setSizeImbue(int i) {
        int i2 = this.selected;
        int i3 = this.numW;
        int i4 = (i2 % i3) - (cmx / 24);
        if (i4 < 0) {
            i4 = 0;
        }
        int i5 = i2 / i3;
        if (i5 + 2 == 5 && i == 1) {
            int size = MainChar.inventory.size() + this.listTemp.size();
            this.numW = size;
            if (size < 6) {
                this.numW = 6;
            }
            int i6 = ((this.numW * 24) - this.w) + 6;
            cmxLim = i6;
            if (i6 < 0) {
                cmxLim = 0;
            }
        } else {
            this.numW = 3;
            cmxLim = 0;
        }
        int i7 = this.numW;
        if (i4 >= i7) {
            i4 = i7 - 1;
        }
        this.selected = (i5 * i7) + i4;
        cmtoY = 0;
        cmtoX = 0;
        cmy = 0;
        cmx = 0;
    }

    @Override
    public void update() {
        Vector vector;
        Char r0;
        GCanvas.gameScr.update();
        int i = this.cmxTab;
        int i2 = this.cmtoXTab;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            this.cmvxTab = i3;
            int i4 = this.cmdxTab + i3;
            this.cmxTab = i + (i4 >> 4);
            this.cmdxTab = i4 & 15;
        }
        if (Math.abs(i2 - this.cmxTab) < 15 && this.cmxTab < 0) {
            this.cmtoXTab = 0;
        }
        if (Math.abs(this.cmtoXTab - this.cmxTab) < 10) {
            int i5 = this.cmxTab;
            int i6 = this.cmxLimTab;
            if (i5 > i6) {
                this.cmtoXTab = i6;
            }
        }
        int i7 = cmy;
        int i8 = cmtoY;
        if (i7 != i8) {
            int i9 = (i8 - i7) << 2;
            cmvy = i9;
            int i10 = cmdy + i9;
            cmy = i7 + (i10 >> 4);
            cmdy = i10 & 15;
        }
        int i11 = cmx;
        int i12 = cmtoX;
        if (i11 != i12) {
            int i13 = (i12 - i11) << 2;
            cmvx = i13;
            int i14 = cmdx + i13;
            cmx = i11 + (i14 >> 4);
            cmdx = i14 & 15;
        }
        if (Math.abs(i8 - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10) {
            int i15 = cmy;
            int i16 = cmyLim;
            if (i15 > i16) {
                cmtoY = i16;
            }
        }
        int i17 = cmtoXName;
        int i18 = cmxname;
        if (i17 != i18) {
            int i19 = i18 + ((i17 - i18) / 2);
            cmxname = i19;
            if (Util.abs(i19) <= 1) {
                cmxname = 0;
            }
        }
        int i20 = cmyText;
        int i21 = cmtoYText;
        if (i20 != i21) {
            int i22 = (i21 - i20) << 2;
            cmvyText = i22;
            int i23 = cmdyText + i22;
            cmyText = i20 + (i23 >> 4);
            cmdyText = i23 & 15;
        }
        if (Math.abs(i21 - cmyText) < 15 && cmyText < 0) {
            cmtoYText = 0;
        }
        if (Math.abs(cmtoYText - cmyText) < 10) {
            int i24 = cmyText;
            int i25 = cmyLimText;
            if (i24 > i25) {
                cmtoYText = i25;
            }
        }
        if (index[focusTab] == 31 && (r0 = charWearing) != null && r0.imgAnimal != null) {
            if (GCanvas.gameTick % charWearing.timeChangeFrame == 0) {
                int i26 = this.countFrame + 1;
                this.countFrame = i26;
                if (i26 > this.arrFrame[charWearing.numFrame == 3 ? (char) 0 : (char) 1].length - 1) {
                    this.countFrame = 0;
                }
            }
            try {
                this.idFrame = this.arrFrame[charWearing.numFrame == 3 ? (char) 0 : (char) 1][this.countFrame];
            } catch (Exception unused) {
            }
        }
        if (index[focusTab] == 0 && (vector = this.list) != null && vector.size() > 0) {
            int i27 = GCanvas.gameScr.mainChar.limItemBag - 1;
            this.limMyBag = i27;
            if (i27 <= 0) {
                this.limMyBag = 0;
            }
            this.excess = this.list.size() % 42;
            this.maxSize = this.list.size();
        }
        if (GCanvas.menu.showMenu) {
            closeText();
        }
        updateTab();
        if (!GCanvas.isPointerClick || !GCanvas.isPointer(15, 23, GCanvas.w - 20, 20) || Math.abs(GCanvas.pyLast - GCanvas.py) >= 10 || Math.abs(GCanvas.pxLast - GCanvas.px) >= 10) {
            return;
        }
        moveCameraTab();
    }

    private void updateTab() {
        if (GCanvas.isPointerDown && GCanvas.isPointer(15, 23, GCanvas.w - 20, 20) && !GCanvas.menu.showMenu) {
            if (!this.trans) {
                this.pa = this.cmxTab;
                this.trans = true;
            }
            int i = this.pa + (GCanvas.pxLast - GCanvas.px);
            this.cmtoXTab = i;
            if (i < -100) {
                this.cmtoXTab = -100;
            }
            int i2 = this.cmtoXTab;
            int i3 = this.cmxLimTab;
            if (i2 > i3 + 100) {
                this.cmtoXTab = i3 + 100;
            }
        }
        if (GCanvas.isPointerClick) {
            this.trans = false;
        }
        if (GCanvas.isPointer(15, 23, GCanvas.w - 20, 20) && !GCanvas.menu.showMenu && GCanvas.isPointerClick && this.isDefault && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && GCanvas.isPointer(15, 23, GCanvas.w - 20, 20)) {
            byte b = index[focusTab];
            switch (b) {
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                    int i4 = ((this.cmtoXTab + GCanvas.px) - 24) / 70;
                    this.tdx = i4;
                    this.selected = 0;
                    this.focusTabChinh = i4;
                    focusTab = i4;
                    setSelectTab();
                    setCmdCenter();
                    break;
                default:
                    if (b != 24 && GCanvas.isPointer(15, 23, GCanvas.hw - 15, 20)) {
                        this.focusTabChinh = 0;
                        focusTab = 1;
                        setSelectTab();
                        setCmdCenter();
                        break;
                    } else if (index[focusTab] != 24 && GCanvas.isPointer(GCanvas.hw + 15, 23, GCanvas.hw - 15, 20)) {
                        this.focusTabChinh = 1;
                        focusTab = 0;
                        setSelectTab();
                        setCmdCenter();
                        break;
                    }
                    break;
            }
        }
    }

    private void moveCameraTab() {
        int i = this.focusTabChinh;
        if (i == 0 || i == 1) {
            this.cmtoXTab = 0;
        } else if (i == this.tabName.length) {
            this.cmtoXTab = this.cmxLimTab;
        } else {
            this.cmtoXTab = (i * 70) - GCanvas.hw;
        }
        if (this.cmtoXTab < 0) {
            this.cmtoXTab = 0;
        }
        int i2 = this.cmtoXTab;
        int i3 = this.cmxLimTab;
        if (i2 > i3) {
            this.cmtoXTab = i3;
        }
    }

    @Override
    public void paint(Graphics g) {
        try {
            GCanvas.gameScr.paint(g);
            g.translate(this.x, this.y);
            int tab = index[focusTab];
            if (tab != 28 && tab != 25 && tab != 21 && tab != 26 && tab != 32 && !this.isDefault
                    && tab != 7 && tab != 22 && tab != 23 && tab != 1 && tab != 31 && tab != 8 && tab != 27) {
                if (tab == 29) {
                    paintKhungClan(g, -10, -10);
                    Font.normalFont[0].drawString(g, this.name, 66, 10, 2);
                } else {
                    Res.paintDlgFull(g, -10, -10);
                }
            }
            if (this.isPaintMoney) {
                Font.smallFontWHITE.drawString(g, GCanvas.gameScr.mainChar.charXu + "$", 132, 153, 1);
                Font.smallFont1.drawString(g, GCanvas.gameScr.mainChar.luong + "L", 5, 153, 0);
            }
            tab = index[focusTab];
            if (tab != 27 && tab != 29) {
                g.drawImage(Res.imgBar, countL, 3, 3);
                g.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, countR + 128, 4, 3);
                if (countL > 0) {
                    countL = (byte) (countL - 1);
                }
                if (countR > 0) {
                    countR = (byte) (countR - 1);
                }
                g.setColor(0xFF797B79);
                g.fillRect(21, -4, 88, 16);
                g.fillRect(20, -3, 90, 14);
                g.setColor(this.isChangeTab ? 0xFF007793 : 0xFF242424);
                g.fillRect(21, -3, 88, 14);
                g.setClip(21, -4, 88, 16);
                g.translate(-cmxname, 0);
                int nameWidth = Font.normalFont[0].getWidth(this.name);
                if (nameWidth > 88) {
                    this.transX -= this.dirX;
                    if (Util.abs(this.transX) > ((nameWidth - 88) / 2) + 5) {
                        this.dirX *= -1;
                    }
                }
                Font.normalFont[0].drawString(g, this.name, this.transX + 66, -3, 2);
            }
            if (cmxname != 0) {
                Font.normalFont[0].drawString(g, this.firName, (dir * 100) + 66, -3, 2);
            }
            g.translate(-cmxname, 0);
            g.translate(this.x0, this.y0);
            g.setClip(-100, -100, 300, 300);
            switch (index[focusTab]) {
                case 1:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    g.setColor(0);
                    g.setAlpha(180);
                    g.fillRect(0, 0, GCanvas.w, GCanvas.h);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 97);
                    paintCharWearing(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, "Trang bị", GCanvas.hw, 25, 2);
                    break;
                case 2:
                    this.isDefault = false;
                    paintFeatures(g);
                    break;
                case 3:
                    this.isDefault = false;
                    paintSkill(g);
                    break;
                case 4:
                    this.isDefault = false;
                    paintCharInfo(g);
                    break;
                case 5:
                    this.isDefault = false;
                    paintParty(g);
                    break;
                case 6:
                    this.isDefault = false;
                    paintQuest(g);
                    break;
                case 7:
                case 8:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    g.setColor(0);
                    g.setAlpha(180);
                    g.fillRect(0, 0, GCanvas.w, GCanvas.h);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    if (index[focusTab] == 7) {
                        paintNewSkillLearn(g);
                        GCanvas.resetTrans(g);
                        Font.normalFont[0].drawString(g, "Học kỹ năng", GCanvas.hw, 25, 2);
                    } else {
                        GCanvas.resetTrans(g);
                        g.translate(35, 65);
                        paintTrade(g);
                        GCanvas.resetTrans(g);
                        Font.normalFont[0].drawString(g, "Giao dịch", GCanvas.hw, 25, 2);
                    }
                    break;
                case 21:
                case 25:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    paintImbue(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, this.name, GCanvas.hw, 25, 2);
                    break;
                case 22:
                case 23:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    Graphics.resetTransAndroid(g);
                    g.saveCanvas();
                    g.ClipRec2(0, 0, GCanvas.w, GCanvas.h);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, 145);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, 144);
                    g.drawRect(-9, -16, 139, 144);
                    GCanvas.resetTrans(g);
                    g.translate(20, 55);
                    paintKeepItem(g);
                    GCanvas.resetTrans(g);
                    Graphics.resetTransAndroid(g);
                    g.restoreCanvas();
                    if (index[focusTab] == 22) {
                        Font.normalFont[0].drawString(g, "Kho đồ", GCanvas.hw, 25, 2);
                    } else {
                        Font.normalFont[0].drawString(g, "Bán đồ", GCanvas.hw, 25, 2);
                    }
                    break;
                case 26:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    paintEpNgoc(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, this.name, GCanvas.hw, 25, 2);
                    break;
                case 27:
                    this.isDefault = false;
                    paintQuestClan(g);
                    break;
                case 28:
                    this.isDefault = false;
                    GCanvas.resetTrans(g);
                    g.setColor(0);
                    g.setAlpha(180);
                    g.fillRect(0, 0, GCanvas.w, GCanvas.h);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    paintCheDo(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, this.name, GCanvas.hw, 25, 2);
                    break;
                case 29: {
                    this.isDefault = false;
                    int tx = g.getTranslateX();
                    int ty = g.getTranslateY();
                    Graphics.resetTransAndroid(g);
                    g.saveCanvas();
                    g.translateAndroid(tx, ty);
                    g.ClipRec2(0, 0, (this.numW * this.wSmall) + 1, (this.hSmall * 5) + 1);
                    g.setClip(0, 0, (this.numW * this.wSmall) + 1, (this.hSmall * 5) + 1);
                    int rowStart = cmy / this.hSmall;
                    if (rowStart < 0) {
                        rowStart = 0;
                    }
                    int rowEnd = (this.w / this.hSmall) + rowStart;
                    for (int row = rowStart; row < rowEnd; row++) {
                        for (int col = 0; col < this.numW; col++) {
                            paintGrid29(g, this.wSmall * col, this.hSmall * row);
                        }
                    }
                    paintProduct(g);
                    Graphics.resetTransAndroid(g);
                    g.restoreCanvas();
                    break;
                }
                case 30: {
                    this.isDefault = false;
                    g.setClip(0, 0, (this.numW * this.wSmall) + 1, (this.hSmall * 5) + 1);
                    g.translate(0, -cmy);
                    int rowStart = cmy / this.hSmall;
                    if (rowStart < 0) {
                        rowStart = 0;
                    }
                    int rowEnd = (this.w / this.hSmall) + rowStart;
                    for (int row = rowStart; row < rowEnd; row++) {
                        for (int col = 0; col < this.numW; col++) {
                            paintGrid(g, this.wSmall * col, this.hSmall * row);
                        }
                    }
                    if (!this.isChangeTab) {
                        g.setColor(0xFFA1ADCE);
                        g.fillRect(((this.selected % this.numW) * this.wSmall) + 1, ((this.selected / this.numW) * this.hSmall) + 1, this.wSmall - 2, this.hSmall - 2);
                    }
                    paintProduct(g);
                    break;
                }
                case 31:
                    GCanvas.resetTrans(g);
                    g.setColor(0);
                    g.setAlpha(130);
                    g.fillRect(0, 0, GCanvas.w, GCanvas.h);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    paintAnimalWearing(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, "Trang bị thú", GCanvas.hw, 25, 2);
                    break;
                case 32:
                    GCanvas.resetTrans(g);
                    paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                    g.translate(25, 65);
                    g.setColor(0xFF00645F);
                    g.fillRect(140, -16, GCanvas.w - 180, GCanvas.h - 98);
                    g.setColor(0xFFFFF640);
                    g.drawRect(140, -16, GCanvas.w - 181, GCanvas.h - 98);
                    paintEpDothu(g);
                    GCanvas.resetTrans(g);
                    Font.normalFont[0].drawString(g, this.name, GCanvas.hw, 25, 2);
                    break;
                default:
                    try {
                        this.isDefault = true;
                        GCanvas.resetTrans(g);
                        paintBG(g, 10, 20, GCanvas.w - 20, GCanvas.h - 60);
                        g.translate(25, 65);
                        g.setColor(0xFF00645F);
                        g.fillRect(140, -16, GCanvas.w - 180, 145);
                        g.setColor(0xFFFFF640);
                        g.drawRect(140, -16, GCanvas.w - 181, 144);
                        g.drawRect(-10, -16, 147, 144);
                        int tx = g.getTranslateX();
                        int ty = g.getTranslateY();
                        Graphics.resetTransAndroid(g);
                        g.setClip(-10, -10, (this.numW * this.wSmall) + 20, (this.hSmall * 5) + 22);
                        g.saveCanvas();
                        g.translateAndroid(tx, ty);
                        g.ClipRec2(-10, -10, (this.numW * this.wSmall) + 20, (this.hSmall * 5) + 22);
                        g.translate(0, -cmy);
                        int rowStart = cmy / (this.hSmall + 10);
                        if (rowStart < 0) {
                            rowStart = 0;
                        }
                        int rowEnd = ((this.w + 20) / (this.hSmall + 10)) + rowStart;
                        if (index[focusTab] == 0 && rowEnd > 42) {
                            rowEnd = 42;
                        }
                        for (int row = rowStart; row < rowEnd; row++) {
                            for (int col = 0; col < 5; col++) {
                                paintGrid(g, ((this.wSmall + 10) * col) - 7, ((this.hSmall + 10) * row) - 10);
                            }
                        }
                        if (!GCanvas.isPointerDown && this.isClick && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                            g.setColor(0xFFA1ADCE);
                            g.fillRect(((this.selected % 5) * (this.wSmall + 10)) - 5, ((this.selected / 5) * (this.hSmall + 10)) - 8, this.wSmall + 7, this.hSmall + 7);
                        }
                        if (index[focusTab] != 0) {
                            paintProduct(g);
                        } else {
                            paintBag(g);
                        }
                        Graphics.resetTransAndroid(g);
                        g.restoreCanvas();
                        GCanvas.resetTrans(g);
                        if (index[focusTab] == 0) {
                            Font.smallFont1.drawString(g, (this.countTabMyitem + 1) + "/" + GCanvas.gameScr.mainChar.limItemBag, 85, 173, 2);
                            g.drawImage(Res.imgBar, 25, 173, 3);
                            g.drawRegion(Res.imgBar, 0, 0, 11, 7, 2, 150, 173, 3);
                        }
                        switch (index[focusTab]) {
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                                g.setClip(15, 23, GCanvas.w - 30, 25);
                                g.translate(-this.cmxTab, 0);
                                g.setColor(0xFF94D9F0);
                                g.setAlpha(70);
                                g.fillRect((this.focusTabChinh * 70) + 15, 23, 70, 20);
                                for (int i = 0; i < this.tabName.length; i++) {
                                    Font.normalFont[0].drawString(g, "" + this.tabName[i], (i * 70) + 50, 25, 2);
                                }
                                for (int i = 0; i < this.tabName.length - 1; i++) {
                                    g.setColor(0xFFA19CA0);
                                    g.fillRect((i * 70) + 83, 23, 1, 20);
                                    g.setColor(0xFF7B7A7B);
                                    g.fillRect((i * 70) + 84, 23, 1, 20);
                                }
                                g.translate(this.cmxTab, 0);
                                g.setClip(0, 0, GCanvas.w, GCanvas.h);
                                break;
                            default:
                                g.setColor(0xFF94D9F0);
                                g.setAlpha(70);
                                g.fillRect((this.focusTabChinh * (GCanvas.hw - 15)) + 15, 23, GCanvas.hw - 15, 20);
                                Font.normalFont[0].drawString(g, "Gian hàng", (GCanvas.w / 4) + 10, 25, 2);
                                Font.normalFont[0].drawString(g, "Hành trang", GCanvas.hw + (GCanvas.w / 4), 25, 2);
                                g.setColor(0xFFA19CA0);
                                g.fillRect(GCanvas.hw, 23, 1, 19);
                                g.setColor(0xFF7B7A7B);
                                g.fillRect(GCanvas.hw + 1, 23, 1, 19);
                                break;
                        }
                        if (this.isPaintMoney) {
                            Font.smallFontWHITE.drawString(g, GCanvas.gameScr.mainChar.charXu + "$", 155, 185, 1);
                            Font.smallFont1.drawString(g, GCanvas.gameScr.mainChar.luong + "L", 20, 185, 0);
                            if (index[focusTab] != 0 && GCanvas.gameScr.mainChar.luongKhoa > -1) {
                                Font.smallFont1.drawString(g, GCanvas.gameScr.mainChar.luongKhoa + " LK", 84, 173, 2);
                            }
                        }
                    } catch (Exception e) {
                    }
                    break;
            }
            if (this.isShowText) {
                GCanvas.resetTrans(g);
                Graphics.resetTransAndroid(g);
                g.saveCanvas();
                g.ClipRec2(0, 0, GCanvas.w, GCanvas.h);
                g.translate(25, 65);
                paintShowTextNew(g);
                Graphics.resetTransAndroid(g);
                g.restoreCanvas();
            }
            super.paint(g);
        } catch (Exception e) {
        }
    }

    int canPutItem() {
        int i = -1;
        int i2 = 0;
        while (true) {
            ItemInInventory[] itemInInventoryArr = this.itemEpdothu;
            if (i2 >= itemInInventoryArr.length) {
                return i;
            }
            if (itemInInventoryArr[i2] == null) {
                i++;
            }
            i2++;
        }
    }

    public void resetEpdothu(int i) {
        int size = this.listItemAnimal.size() + this.listTemp.size();
        this.numW = size;
        if (size < 6) {
            this.numW = 6;
        }
        this.numH = 5;
        if (i == -1) {
            this.selected = (5 - 1) * this.numW;
        } else {
            int i2 = this.selected - 5;
            this.selected = i2;
            int i3 = this.numW;
            if (i2 > (5 * i3) - 1) {
                this.selected = (5 * i3) - 1;
            }
            if (this.selected < (5 - 1) * i3) {
                this.selected = (5 - 1) * i3;
            }
            int i4 = ((this.selected % i3) * this.wSmall) - (this.w / 2);
            cmtoX = i4;
            if (i4 < 0) {
                cmtoX = -10;
            }
            int i5 = cmtoX;
            int i6 = cmxLim;
            if (i5 > i6) {
                cmtoX = i6;
            }
            cmx = cmtoX;
        }
        int i7 = (this.numW * 20) - (this.w - 8);
        cmxLim = i7;
        if (i7 < 0) {
            cmxLim = 0;
        }
        if (i == -1) {
            cmtoY = 0;
            cmtoX = 0;
            cmy = 0;
            cmx = 0;
        }
    }

    public void doshowInfoEpdothu() {
        showItemInventoryInfo((ItemInInventory) this.listItemAnimal.elementAt(this.selected % this.numW), false, (this.selected % this.numW) * 20, 86);
    }

    public void doSelectedEpDothu() {
        final ItemInInventory itemInInventory;
        final GemTemplate gemTemplate;
        Vector vector = new Vector();
        if (this.numH != 1) {
            int i = this.selected;
            int i2 = this.numW;
            if (i % i2 >= 0 && i % i2 < this.listItemAnimal.size()) {
                final ItemInInventory itemInInventory2 = (ItemInInventory) this.listItemAnimal.elementAt(this.selected % this.numW);
                if (canPutItem() > -1) {
                    vector.addElement(new Command("Bỏ vào", new IAction() {
                        @Override
                        public void perform() {
                            if (itemInInventory2.level >= 30 && itemInInventory2.level <= WindowInfoScr.lvThu + 5 && itemInInventory2.level >= WindowInfoScr.lvThu - 5 && itemInInventory2.colorName == WindowInfoScr.idColor) {
                                if (WindowInfoScr.this.itemEpdothu[2] != null) {
                                    for (int i3 = 0; i3 < WindowInfoScr.this.itemEpdothu.length; i3++) {
                                        if (WindowInfoScr.this.itemEpdothu[i3] == null) {
                                            WindowInfoScr.this.itemEpdothu[i3] = itemInInventory2;
                                            WindowInfoScr.this.listItemAnimal.removeElement(itemInInventory2);
                                            return;
                                        }
                                    }
                                    return;
                                }
                                if (WindowInfoScr.magicAnimal == itemInInventory2.magic_physic) {
                                    WindowInfoScr.this.itemEpdothu[2] = itemInInventory2;
                                    WindowInfoScr.this.listItemAnimal.removeElement(itemInInventory2);
                                    return;
                                }
                                return;
                            }
                            GCanvas.startOKDlg("Đồ ép phải cùng màu và chênh lệch +-5 level.");
                        }
                    }));
                }
                vector.addElement(new Command("Thông tin ", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doshowInfoEpdothu();
                        GCanvas.endDlg();
                    }
                }));
            } else if ((this.selected - this.listItemAnimal.size()) % this.numW >= 0 && (this.selected - this.listItemAnimal.size()) % this.numW < this.listTemp.size()) {
                final GemTemplate gemTemplate2 = (GemTemplate) this.listTemp.elementAt((this.selected - this.listItemAnimal.size()) % this.numW);
                vector.addElement(new Command("Bỏ vào", new IAction() {
                    @Override
                    public void perform() {
                        boolean z;
                        if (WindowInfoScr.this.listEp.size() < 6) {
                            if (WindowInfoScr.this.checkItem(false) >= WindowInfoScr.slNLThu) {
                                GCanvas.startOKDlg("Nguyên liệu đã đủ số lượng.");
                                return;
                            }
                            gemTemplate2.number--;
                            if (gemTemplate2.number <= 0) {
                                WindowInfoScr.this.listTemp.removeElement(gemTemplate2);
                            }
                            GemTemplate gemTemplate3 = new GemTemplate(gemTemplate2.rID);
                            gemTemplate3.id = gemTemplate2.id;
                            WindowInfoScr.this.listEp.addElement(gemTemplate3);
                        }
                        int size = WindowInfoScr.this.listEp.size();
                        int i3 = 0;
                        while (true) {
                            if (i3 >= size) {
                                z = false;
                                break;
                            }
                            GemTemplate gemTemplate4 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i3);
                            if (gemTemplate4.id == gemTemplate2.id) {
                                gemTemplate4.number++;
                                z = true;
                                break;
                            }
                            i3++;
                        }
                        for (int i4 = 0; i4 < size; i4++) {
                            GemTemplate gemTemplate5 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i4);
                            if (gemTemplate5.number == 0) {
                                WindowInfoScr.this.listEp.removeElement(gemTemplate5);
                            }
                        }
                        if (z) {
                            return;
                        }
                        GemTemplate gemTemplate6 = new GemTemplate(gemTemplate2.id);
                        GemTemplate.copyData(gemTemplate2, gemTemplate6);
                        gemTemplate6.number = 1;
                        WindowInfoScr.this.listEp.addElement(gemTemplate6);
                    }
                }));
                vector.addElement(addGemInfo(gemTemplate2, ((this.selected % this.numW) * 20) + 4, 86));
            }
        } else {
            int i3 = this.selected;
            if (i3 > 5) {
                if (i3 % 6 >= 0 && i3 % 6 < this.listEp.size() && (gemTemplate = (GemTemplate) this.listEp.elementAt(this.selected % 6)) != null) {
                    vector.addElement(new Command("Lấy ra", new IAction() {
                        @Override
                        public void perform() {
                            gemTemplate.number--;
                            if (gemTemplate.number <= 0) {
                                WindowInfoScr.this.listEp.removeElement(gemTemplate);
                            }
                            int size = WindowInfoScr.this.listTemp.size();
                            for (int i4 = 0; i4 < size; i4++) {
                                GemTemplate gemTemplate3 = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(i4);
                                if (gemTemplate3.id == gemTemplate.id) {
                                    gemTemplate3.number++;
                                    WindowInfoScr.this.resetEpdothu(-1);
                                }
                            }
                            new GemTemplate(gemTemplate.id).number = 1;
                            WindowInfoScr.this.listTemp.addElement(gemTemplate);
                            WindowInfoScr.this.resetEpdothu(-1);
                        }
                    }));
                }
            } else if (i3 >= 0) {
                ItemInInventory[] itemInInventoryArr = this.itemEpdothu;
                if (i3 < itemInInventoryArr.length && (itemInInventory = itemInInventoryArr[i3]) != null) {
                    vector.addElement(new Command("Lấy ra", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.listItemAnimal.addElement(itemInInventory);
                            WindowInfoScr.this.itemEpdothu[WindowInfoScr.this.selected] = null;
                            WindowInfoScr.this.resetEpdothu(-1);
                        }
                    }));
                }
            }
        }
        GCanvas.menu.startAt(vector, 2);
    }

    int checkItem(boolean z) {
        int i;
        int i2 = 0;
        if (z) {
            i = 0;
            while (true) {
                ItemInInventory[] itemInInventoryArr = this.itemEpdothu;
                if (i2 >= itemInInventoryArr.length) {
                    break;
                }
                if (itemInInventoryArr[i2] != null) {
                    i++;
                }
                i2++;
            }
        } else {
            i = 0;
            while (i2 < this.listEp.size()) {
                i += ((GemTemplate) this.listEp.elementAt(i2)).number;
                i2++;
            }
        }
        return i;
    }

    private void doLeftEpDothu() {
        this.left = new Command("Hợp", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.this.checkItem(true) < 5) {
                    GCanvas.startOKDlg("Bạn phải sử dụng đủ 5 vật phẩm.");
                    return;
                }
                if (WindowInfoScr.this.checkItem(false) < WindowInfoScr.slNLThu) {
                    GCanvas.startOKDlg("Nguyên liệu không đủ. Cần " + ((int) WindowInfoScr.slNLThu) + " nguyên liệu.");
                    return;
                }
                Vector vector = new Vector();
                if (WindowInfoScr.this.itemEpdothu[2] != null) {
                    vector.addElement(WindowInfoScr.this.itemEpdothu[2]);
                }
                for (int i = 0; i < WindowInfoScr.this.itemEpdothu.length; i++) {
                    if (i != 2 && WindowInfoScr.this.itemEpdothu[i] != null) {
                        vector.addElement(WindowInfoScr.this.itemEpdothu[i]);
                    }
                }
                byte[] bArr = new byte[6];
                for (int i2 = 0; i2 < WindowInfoScr.this.listEp.size(); i2++) {
                    GemTemplate gemTemplate = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i2);
                    GemTemp gemByID = Res.getGemByID(gemTemplate.id);
                    bArr[Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1)) - 1] = (byte) gemTemplate.number;
                }
                GameService.gI().epDothu(vector, bArr);
                GCanvas.startWaitDlg("Đang hợp..", false);
                for (int i3 = 0; i3 < WindowInfoScr.this.itemEpdothu.length; i3++) {
                    WindowInfoScr.this.itemEpdothu[i3] = null;
                }
                if (WindowInfoScr.this.listEp != null) {
                    WindowInfoScr.this.listEp.removeAllElements();
                }
                WindowInfoScr.this.right.action.perform();
            }
        });
    }

    public void paintListItemAnimal(Graphics graphics, int i) {
        int size = this.listItemAnimal.size();
        for (int i2 = 0; i2 < size; i2++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.listItemAnimal.elementAt(i2);
            int i3 = i * 20;
            graphics.drawImage(Res.imgGrid, i3, this.hSmall * 6, 0);
            itemInInventory.paint(graphics, i3 + 8, (this.hSmall * 6) + 8);
            i++;
        }
    }

    private void paintEpDothu(Graphics graphics) {
        int i;
        int i2;
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.setClip(-10, -10, this.w + 20, this.h + 35);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(-10, -10, this.w + 20, this.h + 35);
        for (int i3 = 0; i3 < 5; i3++) {
            Bitmap bitmap = Res.imgGrid;
            short[] sArr = this.posEpNgoc[i3];
            graphics.drawImage(bitmap, sArr[0], sArr[1], 0);
        }
        graphics.setColor(-6181426);
        if (this.numH == 1 && (i2 = this.selected) < 5) {
            short[] sArr2 = this.posEpNgoc[i2];
            graphics.fillRect(sArr2[0] + 1, sArr2[1] + 1, 16, 16);
        }
        int i4 = 0;
        while (true) {
            ItemInInventory[] itemInInventoryArr = this.itemEpdothu;
            if (i4 >= itemInInventoryArr.length) {
                break;
            }
            ItemInInventory itemInInventory = itemInInventoryArr[i4];
            if (itemInInventory != null) {
                short[] sArr3 = this.posEpNgoc[i4];
                paintItemIcon(graphics, itemInInventory, sArr3[0] + 10, sArr3[1] + 10);
            }
            i4++;
        }
        byte b = idColor;
        int i5 = 3;
        char c = b == 3 ? (char) 2 : b == 2 ? (char) 1 : (char) 3;
        Font font = Font.normalFont[c];
        String str = "" + this.mau[c];
        short[][] sArr4 = this.posEpNgoc;
        font.drawString(graphics, str, sArr4[2][0] + 10, sArr4[1][1], 2);
        Font.normalFont[0].drawString(graphics, "Số Đá: " + ((int) slNLThu), this.posEpNgoc[2][0] + 10, this.hSmall * 3, 2);
        if (this.numH == 1 && (i = this.selected) >= 6 && i < 12) {
            graphics.fillRect(((i % 6) * 20) + 5, (this.hSmall * 4) + 1, 16, 16);
        }
        for (int i6 = 0; i6 < 6; i6++) {
            graphics.drawImage(Res.imgGrid, (i6 * 20) + 4, this.hSmall * 4, 0);
        }
        int size = this.listEp.size();
        int i7 = 0;
        while (i7 < size) {
            GemTemplate gemTemplate = (GemTemplate) this.listEp.elementAt(i7);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            int i8 = Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1));
            int i9 = (i7 % 6) * 20;
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i9 + (this.wSmall / 2) + i5, (this.hSmall * 4) + 8);
            Font.smallFontBLUE.drawString(graphics, i8 + "", i9 + 5, (this.hSmall * 4) - 5, 0);
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i9 + (this.wSmall / 2) + 10, (this.hSmall * 4) + 15, 1);
            i7++;
            size = size;
            i5 = 3;
        }
        graphics.translate(-cmx, 0);
        if (this.numH != 1) {
            graphics.fillRect((this.selected % this.numW) * 20, (this.hSmall * 6) + 1, 17, 16);
        }
        int size2 = this.listItemAnimal.size();
        int i10 = 0;
        for (int i11 = 0; i11 < size2; i11++) {
            ItemInInventory itemInInventory2 = (ItemInInventory) this.listItemAnimal.elementAt(i11);
            int i12 = i10 * 20;
            graphics.drawImage(Res.imgGrid, i12, this.hSmall * 6, 0);
            itemInInventory2.paint(graphics, i12 + 8, (this.hSmall * 6) + 8);
            i10++;
        }
        paintGemItemThu(graphics, i10, this.hSmall * 6);
        for (int size3 = i10 + this.listTemp.size(); size3 < 6; size3++) {
            graphics.drawImage(Res.imgGrid, size3 * 20, this.hSmall * 6, 0);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
    }

    private void paintBG(Graphics graphics, int i, int i2, int i3, int i4) {
        graphics.setColor(-16500172);
        graphics.fillRect(i, i2, i3, i4);
        int width = i - 7;
        int i5 = i3 + 5;
        graphics.setClip(width, i2, i5, 25);
        graphics.ClipRec(width, i2, i5, 25);
        while (width < i3) {
            graphics.drawImage(Res.imgBgPopup, i + width, i2 + 3, 0);
            width += Res.imgBgPopup.getWidth();
        }
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        for (int i6 = 0; i6 < 3; i6++) {
            graphics.setColor(Res.color[i6]);
            int i7 = i + i6;
            int i8 = i6 * 2;
            int i9 = i3 - i8;
            graphics.fillRect(i7 - 1, (25 - i6) + i2, i9 + 2, 1);
            graphics.setColor(Res.color[i6]);
            int i10 = i2 + i6;
            int i11 = i4 - i8;
            graphics.fillRect(i7, i10, 1, i11);
            graphics.fillRect(i + (i3 - i6), i10, 1, i11);
            graphics.fillRect(i7, i10, i9, 1);
            graphics.fillRect(i7, (i4 - i6) + i2, i9, 1);
        }
        int i12 = i - 2;
        int i13 = i2 - 2;
        graphics.drawImage(Res.imgPopupGoc, i12, i13, 0);
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 2, i + 3 + i3, i13, Graphics.TOP | Graphics.RIGHT);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        int i14 = i2 + i4;
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 6, i12, i14 + 4, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 3, i + i3 + 3, i14 + 3, Graphics.BOTTOM | Graphics.RIGHT);
    }

    private void paintGrid(Graphics graphics, int i, int i2) {
        graphics.setColor(-10263709);
        boolean z = this.isDefault;
        int i3 = this.wSmall;
        if (z) {
            i3 += 10;
        }
        graphics.drawRect(i, i2, i3, z ? this.hSmall + 10 : this.hSmall);
        graphics.setColor(-8093054);
        int i4 = i + 1;
        int i5 = i2 + 1;
        boolean z2 = this.isDefault;
        graphics.drawRect(i4, i5, z2 ? this.wSmall + 8 : this.wSmall - 2, z2 ? this.hSmall + 8 : this.hSmall - 2);
    }

    private void paintCheDo(Graphics graphics) {
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.setClip(-10, -10, this.w + 3, this.h + 20);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(-10, -10, this.w + 3, this.h + 20);
        int size = this.listCheDo.size();
        for (int i = 0; i < size; i++) {
            Mineral mineral = (Mineral) this.listCheDo.elementAt(i);
            Font.normalFont[0].drawString(graphics, mineral.name + ": " + ((int) mineral.number) + " Cái.", -10, (i * 13) - 8, 0);
        }
        graphics.setColor(-10718);
        graphics.fillRect(-10, (this.listCheDo.size() * 13) - 6, this.w + 2, 2);
        graphics.fillRect(-10, (((this.hSmall * 3) + 2) + (this.listCheDo.size() * 13)) - 15, this.w + 2, 2);
        int size2 = this.listEp.size() / 2;
        int i2 = size2 < 6 ? 6 : size2;
        graphics.translate(0, 0);
        graphics.setColor(-6181426);
        int i3 = this.selected;
        int i4 = this.numW;
        int i5 = 24;
        if (i3 / i4 < 2) {
            graphics.fillRect(((i3 % i4) * 24) - 9, (this.listCheDo.size() * 13) + ((this.selected / this.numW) * 24) + 3, 23, 23);
        }
        graphics.translate(0, 0);
        int size3 = this.listEp.size();
        int i6 = 0;
        while (i6 < size3) {
            GemTemplate gemTemplate = (GemTemplate) this.listEp.elementAt(i6);
            int i7 = (i6 % i2) * i5;
            int i8 = (i6 / i2) * 24;
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, ((this.wSmall / 2) + i7) - 6, ((((this.listCheDo.size() * 13) + 13) + i8) + (this.wSmall / 2)) - 10);
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", (i7 + (this.wSmall / 2)) - 10, ((((this.listCheDo.size() * 13) + 13) + i8) + (this.wSmall / 2)) - 10, 1);
            i6++;
            size3 = size3;
            i5 = 24;
        }
        int i9 = i5;
        for (int i10 = 0; i10 < 3; i10++) {
            graphics.setColor(-8685189);
            graphics.fillRect(-10, (i10 * 24) + 27, 146, 2);
        }
        for (int i11 = 0; i11 < 7; i11++) {
            graphics.setColor(-8685189);
            graphics.fillRect((i11 * 24) - 10, 27, 2, 48);
        }
        if (this.selected / this.numW >= 2) {
            graphics.translate(-cmx, 0);
            graphics.setColor(-6181426);
            graphics.fillRect(((this.selected % this.numW) * i9) + 1, (this.hSmall * 4) + 1, 23, 23);
        } else {
            graphics.translate(-10, 0);
        }
        paintGemItemCheDo(graphics, 0, this.hSmall * 4);
        for (int size4 = this.listTemp.size(); size4 < 6; size4++) {
            int i12 = this.hSmall * 4;
            for (int i13 = 0; i13 < 2; i13++) {
                graphics.setColor(-8685189);
                graphics.fillRect(0, (i13 * 24) + i12, (size4 * 24) + 26, 2);
            }
            for (int i14 = 0; i14 < size4 + 2; i14++) {
                graphics.setColor(-8685189);
                graphics.fillRect(i14 * 24, i12, 2, i9);
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintSelected(Graphics graphics, int i, int i2) {
        graphics.setColor(-6181426);
        graphics.fillRect(i, i2, 21, 21);
    }

    private void paintKeepItem(Graphics graphics) {
        Vector vector;
        graphics.setClip(0, 0, this.w + 5, this.h + 23);
        graphics.ClipRec(0, 0, this.w + 5, this.h + 23);
        if (this.selected % this.numW < 3) {
            graphics.translate(0, -cmy);
            int i = this.selected;
            int i2 = this.numW;
            paintSelected(graphics, ((i % i2) * 22) + 1, ((i / i2) * 22) + 1);
        } else {
            graphics.translate(0, -this.cmyKeep);
        }
        for (int i3 = 0; i3 < this.numH; i3++) {
            for (int i4 = 0; i4 < 3; i4++) {
                graphics.setColor(-6579813);
                graphics.drawRect(i4 * 22, i3 * 22, this.wSmall, this.hSmall);
            }
        }
        int size = MainChar.inventory.size();
        for (int i5 = 0; i5 < size; i5++) {
            int i6 = (i5 % 3) * 22;
            int i7 = (i5 / 3) * 22;
            ItemInInventory itemInInventory = (ItemInInventory) MainChar.inventory.elementAt(i5);
            if (itemInInventory.isSelling) {
                graphics.setColor(-9070864);
                graphics.fillRect(i6 + 2, i7 + 2, 14, 14);
            }
            itemInInventory.paint(graphics, i6 + 9, i7 + 9);
        }
        int size2 = MainChar.inventory.size();
        for (int i8 = 0; i8 < MainChar.gemItem.size(); i8++) {
            int i9 = (size2 % 3) * 22;
            int i10 = (size2 / 3) * 22;
            GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i8);
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i9 + 9, i10 + 9);
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i9 + 22, (i10 + 22) - Font.smallFontBLUE.getHeight(), 1);
            size2++;
        }
        if (this.selected % this.numW > 2) {
            graphics.translate(0, this.cmyKeep);
            graphics.translate(0, -cmy);
        } else {
            graphics.translate(0, cmy);
            graphics.translate(0, -this.cmyKeep);
        }
        for (int i11 = 0; i11 < this.numH; i11++) {
            for (int i12 = 0; i12 < 3; i12++) {
                graphics.setColor(-10208767);
                int i13 = (i12 * 22) + 66;
                int i14 = i11 * 22;
                graphics.fillRect(i13, i14, this.wSmall, this.hSmall);
                graphics.setColor(-6579813);
                graphics.drawRect(i13, i14, this.wSmall, this.hSmall);
            }
        }
        int i15 = this.selected;
        int i16 = this.numW;
        if (i15 % i16 > 2) {
            paintSelected(graphics, ((i15 % i16) * 22) + 1, ((i15 / i16) * 22) + 1);
        }
        if (index[focusTab] == 22) {
            vector = Char.itembag;
        } else {
            vector = this.sellItem;
        }
        int size3 = vector.size();
        for (int i17 = 0; i17 < size3; i17++) {
            if (vector.elementAt(i17) instanceof ItemInInventory) {
                ((ItemInInventory) vector.elementAt(i17)).paint(graphics, ((i17 % 3) * 22) + 75, ((i17 / 3) * 22) + 9);
            } else {
                Res.paintGem(graphics, Res.getGemByID(((RealID) vector.elementAt(i17)).ID).idImage, ((i17 % 3) * 22) + 75, ((i17 / 3) * 22) + 9);
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintKeepAndSellItem(Graphics graphics) {
        Vector vector;
        graphics.setClip(0, 0, this.w, this.h);
        if (this.selected % this.numW < 3) {
            graphics.translate(0, -cmy);
            int i = this.selected;
            int i2 = this.numW;
            paintSelected(graphics, ((i % i2) * 18) + 1, ((i / i2) * 18) + 1);
        } else {
            graphics.translate(0, -this.cmyKeep);
        }
        for (int i3 = 0; i3 < this.numH; i3++) {
            for (int i4 = 0; i4 < 3; i4++) {
                graphics.drawImage(Res.imgGrid, i4 * 18, i3 * 18, 0);
            }
        }
        int size = MainChar.inventory.size();
        for (int i5 = 0; i5 < size; i5++) {
            int i6 = (i5 % 3) * 18;
            int i7 = (i5 / 3) * 18;
            ItemInInventory itemInInventory = (ItemInInventory) MainChar.inventory.elementAt(i5);
            if (itemInInventory.isSelling) {
                graphics.setColor(-9070864);
                graphics.fillRect(i6 + 2, i7 + 2, 14, 14);
            }
            itemInInventory.paint(graphics, i6 + 9, i7 + 9);
        }
        int size2 = MainChar.inventory.size();
        int size3 = MainChar.gemItem.size();
        int i8 = size2;
        for (int i9 = 0; i9 < size3; i9++) {
            int i10 = (i8 % 3) * 18;
            int i11 = (i8 / 3) * 18;
            GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i9);
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i10 + 9, i11 + 9);
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i10 + 18, (i11 + 18) - Font.smallFontBLUE.getHeight(), 1);
            i8++;
        }
        if (this.selected % this.numW > 2) {
            graphics.translate(0, this.cmyKeep);
            graphics.translate(0, -cmy);
        } else {
            graphics.translate(0, cmy);
            graphics.translate(0, -this.cmyKeep);
        }
        for (int i12 = 0; i12 < this.numH; i12++) {
            for (int i13 = 0; i13 < 3; i13++) {
                graphics.setColor(-10208767);
                int i14 = (i13 * 18) + 54;
                int i15 = i12 * 18;
                graphics.fillRect(i14, i15, 18, 18);
                graphics.drawImage(Res.imgGrid, i14, i15, 0);
            }
        }
        int i16 = this.selected;
        int i17 = this.numW;
        if (i16 % i17 > 2) {
            paintSelected(graphics, ((i16 % i17) * 18) + 1, ((i16 / i17) * 18) + 1);
        }
        if (index[focusTab] == 22) {
            vector = Char.itembag;
        } else {
            vector = this.sellItem;
        }
        int size4 = vector.size();
        for (int i18 = 0; i18 < size4; i18++) {
            if (vector.elementAt(i18) instanceof ItemInInventory) {
                ((ItemInInventory) vector.elementAt(i18)).paint(graphics, ((i18 % 3) * 18) + 63, ((i18 / 3) * 18) + 9);
            } else {
                Res.paintGem(graphics, Res.getGemByID(((RealID) vector.elementAt(i18)).ID).idImage, ((i18 % 3) * 18) + 63, ((i18 / 3) * 18) + 9);
            }
        }
    }

    private void paintEpNgoc(Graphics graphics) {
        int i;
        graphics.setColor(-8815751);
        graphics.fillRect(16, -1, 94, 18);
        graphics.fillRect(15, 0, 96, 16);
        graphics.setColor(-14408668);
        graphics.fillRect(16, 0, 94, 16);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.setClip(-10, 0, this.w + 20, this.h + 10);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(-10, 0, this.w + 20, this.h + 10);
        for (int i2 = 0; i2 < 5; i2++) {
            Bitmap bitmap = Res.imgGrid;
            short[] sArr = this.posEpNgoc[i2];
            graphics.drawImage(bitmap, sArr[0], sArr[1], 0);
        }
        graphics.setColor(-6181426);
        if (this.numH == 1 && (i = this.selected) < 5) {
            short[] sArr2 = this.posEpNgoc[i];
            graphics.fillRect(sArr2[0] + 1, sArr2[1] + 1, 16, 16);
        }
        int size = this.listEp.size();
        for (int i3 = 0; i3 < size; i3++) {
            short s = Res.getGemByID(((RealID) this.listEp.elementAt(i3)).ID).idImage;
            short[] sArr3 = this.posEpNgoc[i3];
            Res.paintGem(graphics, s, sArr3[0] + 10, sArr3[1] + 10);
        }
        graphics.translate(-cmx, 0);
        if (this.numH != 1) {
            graphics.fillRect(((this.selected % this.numW) * 20) + 4, (this.hSmall * 5) + 1, 17, 16);
        }
        paintGemItemHopThanh(graphics, 0, this.hSmall * 5);
        for (int size2 = this.listTemp.size(); size2 < 6; size2++) {
            graphics.drawImage(Res.imgGrid, (size2 * 20) + 4, this.hSmall * 5, 0);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintGemItemCheDo(Graphics graphics, int i, int i2) {
        int size = this.listTemp.size();
        int i3 = i;
        for (int i4 = 0; i4 < size; i4++) {
            if (khoaCheDo == 1) {
                graphics.setColor(-13500365);
                graphics.fillRect(i3 * 24, i2, 24, 24);
            }
            int i5 = this.selected;
            int i6 = this.numW;
            if (i5 / i6 >= 2 && khoaCheDo == 1 && i5 % i6 == i3) {
                graphics.setColor(-6181426);
                graphics.fillRect(((this.selected % this.numW) * 24) + 1, (this.hSmall * 4) + 1, 23, 23);
            }
            for (int i7 = 0; i7 < 2; i7++) {
                graphics.setColor(-8685189);
                graphics.fillRect(0, (i7 * 24) + i2, (i3 * 24) + 26, 2);
            }
            for (int i8 = 0; i8 < i3 + 2; i8++) {
                graphics.setColor(-8685189);
                graphics.fillRect(i8 * 24, i2, 2, 24);
            }
            int i9 = this.selected;
            int i10 = this.numW;
            if (i9 / i10 >= 2 && i9 % i10 == i3) {
                graphics.setColor(-6181426);
                graphics.fillRect(((this.selected % this.numW) * 24) + 1, (this.hSmall * 4) + 1, 23, 23);
            }
            GemTemplate gemTemplate = (GemTemplate) this.listTemp.elementAt(i4);
            int i11 = i3 * 24;
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i11 + 12, i2 + 12);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            try {
                Font.smallFontBLUE.drawString(graphics, Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1)) + "", i11, i2 + 2, 0);
            } catch (Exception unused) {
            }
            Font.smallFontWHITE.drawString(graphics, gemTemplate.number + "", i11 + 20, i2 + 17, 1);
            i3++;
        }
    }

    private void paintGemItemHopThanh(Graphics graphics, int i, int i2) {
        int size = this.listTemp.size();
        int i3 = i;
        for (int i4 = 0; i4 < size; i4++) {
            int i5 = i3 * 20;
            int i6 = i5 + 4;
            graphics.drawImage(Res.imgGrid, i6, i2, 0);
            GemTemplate gemTemplate = (GemTemplate) this.listTemp.elementAt(i4);
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i5 + 14, i2 + 12);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            try {
                Font.smallFontBLUE.drawString(graphics, Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1)) + "", i6, i2 - 2, 0);
            } catch (Exception unused) {
            }
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i5 + 18, i2 + 15, 1);
            i3++;
        }
    }

    private void paintGemItemThu(Graphics graphics, int i, int i2) {
        int size = this.listTemp.size();
        int i3 = i;
        for (int i4 = 0; i4 < size; i4++) {
            if (khoaDoThu == 1) {
                graphics.setColor(-13500365);
                graphics.fillRect(i3 * 20, i2, 17, 17);
            }
            if (this.numH != 1 && khoaDoThu == 1 && this.selected % this.numW == i3) {
                graphics.setColor(-6181426);
                graphics.fillRect((this.selected % this.numW) * 20, (this.hSmall * 6) + 1, 17, 16);
            }
            int i5 = i3 * 20;
            graphics.drawImage(Res.imgGrid, i5, i2, 0);
            GemTemplate gemTemplate = (GemTemplate) this.listTemp.elementAt(i4);
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i5 + 10, i2 + 10);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            try {
                Font.smallFontBLUE.drawString(graphics, Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1)) + "", i5, i2 - 2, 0);
            } catch (Exception unused) {
            }
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i5 + 14, i2 + 15, 1);
            i3++;
        }
    }

    private void paintGemItem(Graphics graphics, int i, int i2) {
        int size = this.listTemp.size();
        int i3 = i;
        for (int i4 = 0; i4 < size; i4++) {
            if ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25)) {
                graphics.setColor(-13500365);
                graphics.fillRect(i3 * 24, i2, 24, 24);
            }
            if (khoaImbue == 1 && index[focusTab] == 21) {
                int i5 = this.selected;
                int i6 = this.numW;
                if (i5 / i6 == 4 && i5 % i6 == i3) {
                    graphics.setColor(-6181426);
                    graphics.fillRect((this.selected % this.numW) * this.wSmall, (this.hSmall * 4) + 1, 24, 23);
                }
            }
            if (khoaKham == 1 && index[focusTab] == 25) {
                int i7 = this.selected;
                int i8 = this.numW;
                if (i7 / i8 == 4 && i7 % i8 == i3) {
                    graphics.setColor(-6181426);
                    graphics.fillRect((this.selected % this.numW) * this.wSmall, (this.hSmall * 4) + 1, 24, 23);
                }
            }
            for (int i9 = 0; i9 < 2; i9++) {
                graphics.setColor(-8685189);
                graphics.fillRect(0, (i9 * 24) + i2, (i3 * 24) + 26, 2);
            }
            for (int i10 = 0; i10 < i3 + 2; i10++) {
                graphics.setColor(-8685189);
                graphics.fillRect(i10 * 24, i2, 2, 24);
            }
            GemTemplate gemTemplate = (GemTemplate) this.listTemp.elementAt(i4);
            int i11 = i3 * 24;
            Res.paintGem(graphics, Res.getGemByID(gemTemplate.id).idImage, i11 + 14, i2 + 12);
            GemTemp gemByID = Res.getGemByID(gemTemplate.id);
            try {
                Font.smallFontBLUE.drawString(graphics, Integer.parseInt(gemByID.name.substring(gemByID.name.length() - 1)) + "", i11, i2 + 2, 0);
            } catch (Exception unused) {
            }
            Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i11 + 20, i2 + 15, 1);
            i3++;
        }
    }

    private void paintImbue(Graphics graphics) {
        int i;
        paintDapBG(graphics, 0, 0);
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        graphics.setClip(-10, 0, this.w, this.h + 20);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(-10, 0, this.w, this.h + 20);
        Bitmap bitmap = this.imgWeaponAvatar;
        if (bitmap != null) {
            graphics.drawImage(bitmap, 63, 43, 3);
        }
        int size = MainChar.gemitemImbue.size();
        for (int i2 = 0; i2 < size; i2++) {
            int i3 = this.hSmall;
            Res.paintGem(graphics, Res.getGemByID(((RealID) MainChar.gemitemImbue.elementAt(i2)).ID).idImage, ((i2 % 2) * 100) + 13, (((i2 / 2) * i3) + (i3 / 2)) - 4);
        }
        graphics.translate(-cmx, 0);
        int i4 = this.selected;
        int i5 = this.numW;
        if (i4 / i5 == 4) {
            graphics.fillRect((i4 % i5) * this.wSmall, (this.hSmall * 4) + 1, 24, 23);
        }
        int size2 = Char.inventory.size();
        int i6 = 0;
        for (int i7 = 0; i7 < size2; i7++) {
            graphics.setColor(-3621303);
            int i8 = i6 * 24;
            graphics.drawRect(i8, this.hSmall * 4, 24, 24);
            graphics.drawRect(i8 + 1, (this.hSmall * 4) + 1, 24, 24);
            ((ItemInInventory) Char.inventory.elementAt(i7)).paint(graphics, i8 + 12, (this.hSmall * 4) + 10);
            i6++;
        }
        paintGemItem(graphics, i6, this.hSmall * 4);
        int size3 = i6 + this.listTemp.size();
        while (size3 <= 6) {
            int i9 = this.hSmall * 4;
            for (int i10 = 0; i10 < 2; i10++) {
                graphics.setColor(-8685189);
                graphics.fillRect(0, (i10 * 24) + i9, (size3 * 24) + 2, 2);
            }
            int i11 = 0;
            while (true) {
                i = size3 + 1;
                if (i11 < i) {
                    graphics.setColor(-8685189);
                    graphics.fillRect(i11 * 24, i9, 2, 24);
                    i11++;
                } else {
                    break;
                }
            }
            size3 = i;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintDapBG(Graphics graphics, int i, int i2) {
        if (GCanvas.gameTick % 10 > 3 && this.selected / this.numW < 4) {
            graphics.setColor(-6181426);
            int i3 = this.selected;
            int i4 = this.numW;
            if (i3 % i4 == 1 && i3 / i4 != 4) {
                graphics.fillRect(25, 0, 75, 86);
            } else {
                graphics.fillRect(((i3 % i4) * 50) + 4, ((i3 / i4) * this.hSmall) + 1, 17, 17);
            }
        }
        for (int i5 = 0; i5 < 8; i5++) {
            graphics.drawImage(Res.imgGrid, ((i5 / 4) * 100) + 4, (i5 % 4) * this.hSmall, 0);
        }
        graphics.setColor(-6181426);
        graphics.drawRect(25, 0, 75, 86);
    }

    private void paintGrid(Graphics graphics) {
        for (int i = 0; i < 5; i++) {
            for (int i2 = 0; i2 < 6; i2++) {
                if (i2 > 2) {
                    graphics.setColor(-10208767);
                    graphics.fillRect((i2 * 22) - 12, (i * 22) - 8, 22, 22);
                }
                graphics.setColor(-10263709);
                graphics.drawRect((i2 * 22) - 12, (i * 22) - 8, 22, 22);
            }
        }
    }

    private void paintTrade(Graphics graphics) {
        int translateX = graphics.getTranslateX();
        int translateY = graphics.getTranslateY();
        Graphics.resetTransAndroid(graphics);
        int i = -12;
        graphics.setClip(-12, -8, 133, 133);
        graphics.saveCanvas();
        graphics.translateAndroid(translateX, translateY);
        graphics.ClipRec2(-12, -8, 133, 133);
        paintGrid(graphics);
        graphics.setColor(-6181426);
        int i2 = this.selectedGD;
        int i3 = this.wSmall;
        int i4 = this.hSmall;
        graphics.fillRect(((i2 % 6) * i3) - 11, ((i2 / 6) * i4) - 7, i3, i4);
        Vector vector = GCanvas.gameScr.mainChar.tItems;
        Vector vector2 = GCanvas.gameScr.mainChar.rItems;
        int size = vector.size();
        for (int i5 = 0; i5 < size; i5++) {
            paintItemTrade(graphics, vector.elementAt(i5), ((i5 % 3) * this.wSmall) - 12, ((i5 / 3) * this.hSmall) - 8);
        }
        int size2 = vector2.size();
        for (int i6 = 0; i6 < size2; i6++) {
            Object objElementAt = vector2.elementAt(i6);
            int i7 = this.wSmall;
            paintItemTrade(graphics, objElementAt, ((i7 * 3) + ((i6 % 3) * i7)) - 12, ((i6 / 3) * this.hSmall) - 8);
        }
        graphics.translate(-cmx, 0);
        graphics.setColor(-6181426);
        int i8 = this.selected;
        int i9 = this.wSmall;
        int i10 = this.hSmall;
        graphics.fillRect((i8 * i9) - 11, (i10 * 5) - 7, i9, i10);
        int size3 = Char.inventory.size();
        for (int i11 = 0; i11 < size3; i11++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i11);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            graphics.setColor(-10263709);
            graphics.drawRect(i, this.h - 8, this.wSmall, this.hSmall);
            if (itemInInventory.isSelling) {
                graphics.setColor(-9070864);
                graphics.fillRect(i + 2, this.h - 6, 18, 18);
            }
            GameData.paintIcon(graphics, item.idIcon, i + 11, this.h + 3);
            i += this.wSmall;
        }
        for (int i12 = 0; i12 < MainChar.listPotion.length; i12++) {
            if (MainChar.listPotion[i12].number - MainChar.listPotion[i12].numTrade > 0 && MainChar.listPotion[i12].isTrade) {
                graphics.setColor(-10263709);
                graphics.drawRect(i, this.h - 8, this.wSmall, this.hSmall);
                GameData.paintIcon(graphics, (short) (MainChar.listPotion[i12].index + 5500), i + 11, this.h + 3);
                Font.smallFontBLUE.drawString(graphics, (MainChar.listPotion[i12].number - MainChar.listPotion[i12].numTrade) + "", i + 20, this.h + 3, 1);
                i += this.wSmall;
            }
        }
        if (i / this.wSmall < 6) {
            for (int i13 = 0; i13 < 6 - (i / this.wSmall); i13++) {
                graphics.setColor(-10263709);
                int i14 = this.wSmall;
                graphics.drawRect((i13 * i14) + i, this.h - 8, i14, this.hSmall);
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintItemTrade(Graphics graphics, Object obj, int i, int i2) {
        if (obj instanceof ItemInInventory) {
            ((ItemInInventory) obj).paint(graphics, i + 9, i2 + 9);
            return;
        }
        Potion potion = (Potion) obj;
        int i3 = i2 + 9;
        GameData.paintIcon(graphics, (short) (potion.index + 5500), i + 9, i3);
        Font.smallFontBLUE.drawString(graphics, potion.number + "", i + 17, i3, 1);
    }

    private void paintNewSkillLearn(Graphics graphics) {
        Font.normalFont[0].drawString(graphics, new String[]{"Kiếm sỹ", "Chiến binh", "Pháp sư", "Đấu sỹ", "Cung thủ"}[GCanvas.gameScr.mainChar.clazz], 60, -14, 2);
        graphics.setColor(-4868683);
        graphics.fillRect(-5, -2, 138, 1);
        if (GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-2637296);
            int i = this.selected;
            if (i < 5) {
                int i2 = (i * this.wSkill) - 9;
                int i3 = this.h;
                graphics.fillRect(i2, (i3 / 2) - 12, this.wSmall - 6, (this.hSmall + (i3 / 4)) - 3);
            } else if (i >= 5 && i < 10) {
                int i4 = this.wSkill;
                int i5 = this.h;
                graphics.fillRect(((i - 5) * i4) - 9, ((i5 / 2) - 12) + i4, this.wSmall - 6, (this.hSmall + (i5 / 4)) - 3);
            } else if (i >= 10) {
                int i6 = this.wSkill;
                int i7 = this.h;
                graphics.fillRect(((i - 10) * i6) - 9, ((i7 / 2) - 12) + (i6 * 2), this.wSmall - 6, (this.hSmall + (i7 / 4)) - 3);
            }
        }
        for (int i8 = 0; i8 < this.numW; i8++) {
            InfoSkillLearn infoSkillLearn = (InfoSkillLearn) ((Vector) GameScr.infoNewSkill.elementAt(GCanvas.gameScr.mainChar.clazz)).elementAt(i8);
            if (i8 < 5) {
                GameData.imgSkillIcon.drawFrame(infoSkillLearn.idSkill, (this.wSkill * i8) + 3, this.h / 2, 0, 3, graphics);
                graphics.drawImage(Res.imgKhung, (this.wSkill * i8) + 3, this.h / 2, 3);
            } else if (i8 >= 5 && i8 < 10) {
                FrameImage frameImage = GameData.imgSkillIcon;
                int i9 = i8 - 5;
                int i10 = this.wSkill;
                frameImage.drawFrame(i8, (i9 * i10) + 3, (this.h / 2) + i10, 0, 3, graphics);
                Bitmap bitmap = Res.imgKhung;
                int i11 = this.wSkill;
                graphics.drawImage(bitmap, (i9 * i11) + 3, (this.h / 2) + i11, 3);
            } else if (i8 >= 10) {
                FrameImage frameImage2 = GameData.imgSkillIcon;
                int i12 = i8 - 10;
                int i13 = this.wSkill;
                frameImage2.drawFrame(i8, (i12 * i13) + 3, (this.h / 2) + (i13 * 2), 0, 3, graphics);
                Bitmap bitmap2 = Res.imgKhung;
                int i14 = this.wSkill;
                graphics.drawImage(bitmap2, (i12 * i14) + 3, (this.h / 2) + (i14 * 2), 3);
            }
        }
        graphics.setClip(140, -15, GCanvas.w - 180, 140);
        graphics.ClipRec(140, -15, GCanvas.w - 180, 140);
        Vector vector = this.newSkill;
        if (vector != null) {
            int size = vector.size();
            for (int i15 = 0; i15 < size; i15++) {
                String str = (String) this.newSkill.elementAt(i15);
                if (str.substring(0, 1).equals("`")) {
                    Font.normalFont[0].drawString(graphics, str.substring(1, str.length()), 145, (i15 * 12) - 10, 0);
                } else {
                    Font.arialFont11.drawString(graphics, str, 145, (i15 * 12) - 10, 0);
                }
            }
        }
        graphics.restoreCanvas();
    }

    private void paintKhung(Graphics graphics) {
        int i = this.h;
        int i2 = i + 26;
        if (index[focusTab] != 4) {
            i = i2;
        }
        graphics.setColor(-4868683);
        graphics.drawRect(0, -10, this.w, i);
        graphics.setClip(0, -10, this.w, i);
        graphics.translate(0, 1 - cmy);
    }

    private void paintQuestClan(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        int i = (GCanvas.w - 170) / 2;
        int i2 = (GCanvas.h - 130) / 2;
        Res.paintDlgDragonFull(graphics, i - 5, i2 - 10, 180, 150);
        graphics.setColor(-4868683);
        int i3 = i + 2;
        int i4 = i2 + 15;
        graphics.drawRect(i3, i4, 166, 100);
        Font.normalFont[0].drawString(graphics, "Nhiệm vụ bang hội", i + 90, i2 - 5, 2);
        graphics.setClip(i3, i4, 166, 100);
        graphics.ClipRec(i3, i4, 166, 100);
        graphics.translate(i + 5, i2 + 20);
        graphics.translate(0, -cmy);
        int size = this.questInfo.size();
        for (int i5 = 0; i5 < size; i5++) {
            String str = (String) this.questInfo.elementAt(i5);
            if (str.length() >= 2 && str.substring(0, 1).equals("`")) {
                Font.normalFont[0].drawString(graphics, str.substring(1, str.length() - 1), 5, (i5 * 15) + 2, 0);
            } else {
                Font.arialFont11.drawString(graphics, str, 5, (i5 * 15) + 2, 0);
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
    }

    private void paintGrid29(Graphics graphics, int i, int i2) {
        graphics.setColor(-10263709);
        graphics.drawRect(i, i2, this.wSmall, this.hSmall);
        graphics.setColor(-8093054);
        graphics.drawRect(i + 1, i2 + 1, this.wSmall - 2, this.hSmall - 2);
    }

    public void paintKhungClan(Graphics graphics, int i, int i2) {
        int i3 = i2 + 14;
        int width = Res.imgInv[2].getWidth();
        int height = Res.imgInv[2].getHeight();
        int i4 = i - 18;
        int i5 = i2 + 11;
        int i6 = i - 18;
        graphics.setClip(i6, i3, 184, 142);
        graphics.ClipRec(i6, i3, 184, 142);
        for (int i7 = 184 % width; i7 < 184 + width; i7 += width) {
            for (int i8 = 144 % height; i8 < 144 + height; i8 += height) {
                graphics.drawImage(Res.imgInv[2], (i + 169) - i7, (i2 + 158) - i8, Graphics.TOP | Graphics.LEFT);
            }
        }
        graphics.restoreCanvas();
        int i9 = i - 60;
        int i10 = i2 - 36;
        graphics.setClip(i9, i10, GCanvas.w, GCanvas.h);
        graphics.ClipRec(i9, i10, GCanvas.w, GCanvas.h);
        for (int i11 = 0; i11 < 3; i11++) {
            graphics.setColor(Res.color[i11]);
            int i12 = i11 * 2;
            graphics.drawRect(i4 + i11, i5 + i11, 184 - i12, 144 - i12);
        }
        graphics.fillRect(i - 17, i2 + 39, 182, 1);
        int i13 = i - 20;
        int i14 = i2 + 9;
        graphics.drawImage(Res.imgInv[0], i13, i14, 20);
        int i15 = i + 169;
        graphics.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 2, i15, i14, 24);
        int i16 = i2 + 158;
        graphics.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 6, i13, i16, 36);
        graphics.drawRegion(Res.imgInv[0], 0, 0, 18, 19, 3, i15, i16, 40);
        graphics.restoreCanvas();
    }

    public static String getInfoQuestPaint(int i) {
        String infoPaintScr;
        if (i == 0 && GameScr.mainQuest != null) {
            infoPaintScr = GameScr.mainQuest.getInfoPaintScr();
        } else {
            infoPaintScr = "";
        }
        if (GameScr.subQuest != null && i == 1) {
            try {
                infoPaintScr = GameScr.subQuest.getInfoPaintScr();
            } catch (Exception unused) {
            }
        }
        if (GameScr.clanQuest == null || i != 2) {
            return infoPaintScr;
        }
        try {
            return GameScr.clanQuest.getInfoPaintScr();
        } catch (Exception unused2) {
            return infoPaintScr;
        }
    }

    private void paintQuest(Graphics graphics) {
        String[] strArr;
        int length;
        String[] strArr2;
        paintKhung(graphics);
        String infoQuestPaint = getInfoQuestPaint(0);
        String infoQuestPaint2 = getInfoQuestPaint(1);
        String infoQuestPaint3 = getInfoQuestPaint(2);
        if (infoQuestPaint.equals("") && infoQuestPaint2.equals("")) {
            Font.arialFont11.drawString(graphics, "Chưa nhận nhiệm vụ", 8, -7, 0);
            return;
        }
        String[] strArr3 = null;
        if (infoQuestPaint.equals("")) {
            strArr = null;
        } else {
            String[] strArrSplit = Util.split(infoQuestPaint, "|");
            strArr = new String[strArrSplit.length + 1];
            strArr[0] = GameScr.mainQuest.name;
            int i = 0;
            while (i < strArrSplit.length) {
                int i2 = i + 1;
                strArr[i2] = strArrSplit[i];
                i = i2;
            }
        }
        if (strArr != null) {
            Font.normalFont[0].drawString(graphics, strArr[0], 4, 0, 0);
            for (int i3 = 1; i3 < strArr.length; i3++) {
                Font.arialFont11.drawString(graphics, strArr[i3], 4, i3 * 15, 0);
            }
            length = strArr.length;
        } else {
            length = 0;
        }
        if (infoQuestPaint2.equals("")) {
            strArr2 = null;
        } else {
            String[] strArrSplit2 = Util.split(infoQuestPaint2, "|");
            strArr2 = new String[strArrSplit2.length + 1];
            strArr2[0] = GameScr.subQuest.name;
            int i4 = 0;
            while (i4 < strArrSplit2.length) {
                int i5 = i4 + 1;
                strArr2[i5] = strArrSplit2[i4];
                i4 = i5;
            }
        }
        if (strArr2 != null) {
            Font.normalFont[0].drawString(graphics, strArr2[0], 4, length * 15, 0);
            for (int i6 = 1; i6 < strArr2.length; i6++) {
                Font.arialFont11.drawString(graphics, strArr2[i6], 4, (i6 + length) * 15, 0);
            }
            length += strArr2.length;
        }
        if (!infoQuestPaint3.equals("")) {
            String[] strArrSplit3 = Util.split(infoQuestPaint3, "|");
            strArr3 = new String[strArrSplit3.length + 1];
            strArr3[0] = GameScr.clanQuest.name;
            int i7 = 0;
            while (i7 < strArrSplit3.length) {
                int i8 = i7 + 1;
                strArr3[i8] = strArrSplit3[i7];
                i7 = i8;
            }
        }
        if (strArr3 != null) {
            Font.normalFont[0].drawString(graphics, strArr3[0], 4, length * 15, 0);
            for (int i9 = 1; i9 < strArr3.length; i9++) {
                Font.arialFont11.drawString(graphics, strArr3[i9], 4, (i9 + length) * 15, 0);
            }
            int length2 = strArr3.length;
        }
    }

    private void paintParty(Graphics graphics) {
        paintKhung(graphics);
        int size = Char.party.size();
        if (size > 0) {
            graphics.setColor(-10328832);
            graphics.fillRect(2, this.selected * 32, 131, 32);
            for (int i = 0; i < size; i++) {
                PartyInfo partyInfo = (PartyInfo) Char.party.elementAt(i);
                Font.arialFont11.drawString(graphics, "NV:" + partyInfo.name, 8, i * 33, 0);
                int i2 = ((i * 2) + 1) * 16;
                Font.arialFont11.drawString(graphics, "LV: " + partyInfo.lv, 8, i2, 0);
                graphics.setColor(-1);
                graphics.fillRect(2, i2 + 15, 131, 1);
            }
            return;
        }
        Font.arialFont11.drawString(graphics, "Chưa có nhóm", 8, -3, 0);
    }

    private void paintCharInfo(Graphics graphics) {
        paintKhung(graphics);
        if (this.charInfo != null) {
            for (int i = 0; i < this.charInfo.length; i++) {
                Font.arialFont11.drawString(graphics, this.charInfo[i], 4, (i * 12) - 3, 0);
            }
        }
        if (infoMainCharServer != null) {
            for (int i2 = 0; i2 < infoMainCharServer.length; i2++) {
                Font.arialFont11.drawString(graphics, infoMainCharServer[i2], 4, ((this.charInfo.length + i2 + 1) * 12) + 5, 0);
            }
        }
    }

    private void paintSkill(Graphics graphics) {
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.skillPointLeft) + "", 105, -16, 2);
        Font.normalFont[0].drawString(graphics, "Điểm kỹ năng: ", 60, -22, 2);
        graphics.setColor(-4868683);
        graphics.fillRect(-8, -8, 133, 1);
        graphics.fillRect(-8, 26, 133, 1);
        graphics.setClip(-8, -8, this.w + 20, this.h + 8);
        graphics.ClipRec(-8, -8, this.w + 20, this.h + 8);
        graphics.translate(-cmx, 0);
        if (GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-16187921);
            int i = this.selected;
            int i2 = this.wSmall;
            int i3 = this.h;
            graphics.fillRect((i * i2) + 3, 5 - (i3 / 4), i2 - 6, (this.hSmall + (i3 / 4)) - 3);
        }
        for (int i4 = 0; i4 < this.numW; i4++) {
            FrameImage frameImage = GameData.imgSkillIcon;
            int i5 = (this.wSmall / 2) + (this.wSkill * i4);
            int i6 = this.h;
            frameImage.drawFrame(i4, i5, ((i6 / 2) - (i6 / 4)) + 2, 0, 3, graphics);
            Bitmap bitmap = Res.imgKhung;
            int i7 = (this.wSkill * i4) + (this.wSmall / 2);
            int i8 = this.h;
            graphics.drawImage(bitmap, i7, ((i8 / 2) - (i8 / 4)) + 2, 3);
            Font.smallFont1.drawString(graphics, ((int) Char.skillLevelLearnt[i4]) + "", ((this.wSkill * i4) + this.wSmall) - 4, (this.h / 2) - 1, 1);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        if (this.isChangeTab) {
            return;
        }
        int i9 = this.selected;
        if (i9 < 0) {
            this.selected = 0;
        } else if (i9 > SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz].length - 1) {
            this.selected = SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz].length - 1;
        }
        graphics.translate(cmx, 0);
        graphics.setClip(-11, 28, 138, 72);
        graphics.ClipRec(-11, 28, 138, 72);
        String str = SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz][this.selected];
        byte b = Char.skillLevelLearnt[this.selected];
        if (b == 9) {
            str = str + " MAX" + ((int) b);
        } else if (b >= 1) {
            str = str + " lv " + ((int) b);
        }
        String[] infoSkill = GCanvas.gameScr.mainChar.getInfoSkill(this.selected);
        Font.normalFont[0].drawString(graphics, str, -5, 30, 0);
        int i10 = 40;
        for (String str2 : infoSkill) {
            Font.arialFont11.drawString(graphics, str2, -5, i10, 0);
            i10 += 12;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintFeatures(Graphics graphics) {
        if (this.selected > 4) {
            this.selected = 4;
        }
        graphics.setColor(-9498350);
        graphics.fillRect(3, this.selected * 19, 121, 18);
        graphics.drawImage(Res.imgBGTiemNang, 0, -20, 0);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.basePointLeft) + "", 107, -11, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.strength) + "", 107, 8, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.agility) + "", 107, 27, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.spirit) + "", 107, 46, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.health) + "", 107, 65, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.luck) + "", 107, 84, 2);
    }

    private void paintCharWearing(Graphics graphics) {
        if (this.selected > 14) {
            this.selected = 14;
        }
        if (this.selected < 0) {
            this.selected = 0;
        }
        charWearing.paintAvatar(graphics, (short) 63, (short) 90);
        if (this.imgCharWear != null && GCanvas.gameTick % 16 >= 8) {
            graphics.drawImage(this.imgCharWear, (this.w / 2) + 5, 5, Graphics.TOP | Graphics.HCENTER);
        }
        graphics.setColor(0);
        int i = this.wSmall;
        graphics.fillRect(-7, -13, i, i * 5);
        int i2 = this.wSmall;
        graphics.fillRect((i2 * 4) - 7, -13, i2, i2 * 5);
        int i3 = this.wSmall;
        graphics.fillRect((i3 * 3) - 10, (this.hSmall * 4) - 13, i3, i3);
        int i4 = this.wSmall;
        graphics.fillRect(i4 - 3, (this.hSmall * 4) - 13, i4, i4);
        for (int i5 = 0; i5 < 5; i5++) {
            paintGrid(graphics, -7, (this.hSmall * i5) - 13);
            paintGrid(graphics, (this.wSmall * 4) - 7, (this.hSmall * i5) - 13);
        }
        paintGrid(graphics, (this.wSmall * 3) - 10, (this.hSmall * 4) - 13);
        int i6 = this.wSmall;
        graphics.fillRect(i6 - 4, -13, (i6 * 3) - 6, 1);
        int i7 = this.wSmall;
        graphics.fillRect(i7 - 4, (i7 * 5) - 13, (i7 * 3) - 6, 1);
        int i8 = this.wSmall;
        graphics.fillRect(i8 - 4, -13, 1, i8 * 5);
        int i9 = this.wSmall;
        graphics.fillRect((i9 * 4) - 10, -13, 1, i9 * 5);
        paintGrid(graphics, this.wSmall - 3, (this.hSmall * 4) - 13);
        if (GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-6181426);
            int i10 = this.selected;
            if (i10 % 3 != 1) {
                int i11 = this.numW;
                int i12 = i10 % i11 <= 0 ? 0 : 4;
                int i13 = this.wSmall;
                graphics.fillRect((i12 * i13) - 6, ((i10 / i11) * i13) - 12, 26, 26);
            } else if (i10 != 4) {
                graphics.fillRect((this.wSmall * 3) - 9, (this.hSmall * 4) - 12, 26, 26);
            } else {
                graphics.fillRect(this.wSmall - 2, (this.hSmall * 4) - 12, 26, 26);
            }
        }
        if (charWearing.wearingItems != null) {
            int size = charWearing.wearingItems.size();
            for (int i14 = 0; i14 < size; i14++) {
                ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingItems.elementAt(i14);
                ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                if (item.type == 8) {
                    if (itemInInventory.viTriVe == 1) {
                        paintItemIcon(graphics, itemInInventory, 119, 57);
                    } else {
                        paintItemIcon(graphics, itemInInventory, 119, 83);
                    }
                } else {
                    paintItemIcon(graphics, itemInInventory, posPaintWearingX[item.type], posPaintWearingY[item.type]);
                }
            }
        }
        if (charWearing.idAnimal > -1) {
            GameData.paintIcon(graphics, (short) (charWearing.idPaintIconAnimal + 7500), 87, 112);
        }
    }

    private void paintAnimalWearing(Graphics graphics) {
        Char r0;
        ItemTemplate item;
        Char r1 = charWearing;
        if (r1 != null && r1.imgAnimal != null && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
            graphics.drawRegion(charWearing.imgAnimal, 0, charWearing.himg * this.idFrame, charWearing.wimg, charWearing.himg, 0, (Res.imgInfoWindow[0].getWidth() / 2) + 5, (Res.imgInfoWindow[0].getHeight() / 2) + 5, 3);
        }
        graphics.setColor(0);
        int i = this.wSmall;
        graphics.fillRect(-7, -13, i, i * 5);
        int i2 = this.wSmall;
        graphics.fillRect((i2 * 4) - 7, -13, i2, i2 * 5);
        graphics.fillRect((this.wSmall * 3) - 8, (this.hSmall * 4) - 11, 26, 26);
        for (int i3 = 0; i3 < 5; i3++) {
            paintGrid(graphics, -7, (this.hSmall * i3) - 13);
            paintGrid(graphics, (this.wSmall * 4) - 7, (this.hSmall * i3) - 13);
        }
        paintGrid(graphics, (this.wSmall * 3) - 10, (this.hSmall * 4) - 13);
        int i4 = this.wSmall;
        graphics.fillRect(i4 - 4, -13, (i4 * 3) - 6, 1);
        int i5 = this.wSmall;
        graphics.fillRect(i5 - 4, (i5 * 5) - 13, (i5 * 3) - 6, 1);
        int i6 = this.wSmall;
        graphics.fillRect(i6 - 4, -13, 1, i6 * 5);
        int i7 = this.wSmall;
        graphics.fillRect((i7 * 4) - 10, -13, 1, i7 * 5);
        if (this.isClick && GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-6181426);
            int i8 = this.selected;
            if (i8 % 2 == 0) {
                graphics.fillRect(-6, ((i8 / 2) * this.hSmall) - 11, 26, 26);
            } else {
                graphics.fillRect((this.wSmall * 4) - 6, ((i8 / 2) * this.hSmall) - 11, 26, 26);
            }
        }
        Char r2 = charWearing;
        if (r2 != null) {
            if (r2.wearingAnimal != null && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
                int size = charWearing.wearingAnimal.size();
                for (int i9 = 0; i9 < size; i9++) {
                    ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingAnimal.elementAt(i9);
                    if (itemInInventory != null && (item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate)) != null) {
                        paintItemIcon(graphics, itemInInventory, posPaintWearingX[item.type], posPaintWearingY[item.type]);
                    }
                }
            }
            if (charWearing.idAnimal <= -1 || (r0 = charWearing) == null) {
                return;
            }
            if (r0.useHorse != -1 || charWearing.idhorse > -1) {
                GameData.paintIcon(graphics, (short) (charWearing.idPaintIconAnimal + 7500), 87, 112);
            }
        }
    }

    private void paintItemIcon(Graphics graphics, ItemInInventory itemInInventory, int i, int i2) {
        GameData.paintIcon(graphics, Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).idIcon, i, i2);
    }

    private void paintShowText(Graphics graphics) {
        int i;
        byte[] bArr;
        graphics.setColor(-16751521);
        graphics.fillRect(this.xShow, this.yShow, this.wShow + 25, this.hShow);
        graphics.setColor(-2496);
        graphics.drawRect(this.xShow, this.yShow, this.wShow + 24, this.hShow - 1);
        graphics.setClip(this.xShow, this.yShow, this.wShow + 25, (this.hShow + (this.countPop * 15)) - 2);
        graphics.ClipRec(this.xShow, this.yShow, this.wShow + 25, (this.hShow + (this.countPop * 15)) - 2);
        graphics.translate(0, -cmyText);
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int length = this.showText.length;
            i = this.countPop;
            if (i2 >= length + i) {
                break;
            }
            if (i2 == 1 && (bArr = this.arrayKhamNgoc) != null && bArr.length > 0) {
                for (int i4 = 0; i4 < this.arrayKhamNgoc.length; i4++) {
                    int i5 = i4 * 20;
                    graphics.drawImage(Res.imgKham, this.xShow + 12 + i5, this.yShow + 12 + i3, 3);
                    byte b = this.arrayKhamNgoc[i4];
                    if (b != -1) {
                        Res.paintGem(graphics, b, this.xShow + 12 + i5, this.yShow + 12 + i3);
                    }
                }
                i3 += 18;
            }
            if (!this.showText[i2].equals("")) {
                byte bCharAt = (byte) (this.showText[i2].charAt(0) - '0');
                boolean zIsDegit = isDegit(this.showText[i2].charAt(0));
                if (!zIsDegit) {
                    bCharAt = 0;
                }
                Font[] fontArr = Font.normalFont;
                if (bCharAt >= 6) {
                    bCharAt = 0;
                }
                fontArr[bCharAt].drawString(graphics, this.showText[i2].substring(zIsDegit ? 1 : 0), this.xShow + 4, this.yShow + 4 + i3, 0);
                i3 += 15;
            }
            i2++;
        }
        if (i < 0) {
            this.countPop = i + 1;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintBag(Graphics graphics) {
        Command command;
        try {
            int i = (cmy / (this.hSmall + 10)) * 5;
            if (i < 0) {
                i = 0;
            }
            int i2 = i + 45;
            if (i2 > 42) {
                i2 = 42;
            }
            if ((this.countTabMyitem * 42) + 42 > this.maxSize) {
                i2 = this.excess;
            }
            while (i < i2 && !this.isLoad) {
                int i3 = (this.countTabMyitem * 42) + i;
                if (i3 < this.list.size() && (command = (Command) this.list.elementAt(i3)) != null) {
                    int i4 = this.wSmall;
                    int i5 = this.hSmall;
                    command.paint(graphics, (((i % 5) * (i4 + 10)) + ((i4 + 10) / 2)) - 7, (((i / 5) * (i5 + 10)) + ((i5 + 10) / 2)) - 10);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println("ERROR hanh trang ");
        }
    }

    private void paintProduct(Graphics graphics) {
        try {
            int i = 0;
            if (this.isDefault) {
                int i2 = (cmy / (this.hSmall + 10)) * 5;
                if (i2 >= 0) {
                    i = i2;
                }
                int size = i + 45;
                if (size > this.list.size()) {
                    size = this.list.size();
                }
                while (i < size && !this.isLoad) {
                    Command command = (Command) this.list.elementAt(i);
                    int i3 = this.wSmall;
                    int i4 = this.hSmall;
                    command.paint(graphics, (((i % 5) * (i3 + 10)) + ((i3 + 10) / 2)) - 7, (((i / 5) * (i4 + 10)) + ((i4 + 10) / 2)) - 10);
                    i++;
                }
                return;
            }
            int i5 = cmy / this.hSmall;
            int i6 = this.numW;
            int i7 = i5 * i6;
            if (i7 >= 0) {
                i = i7;
            }
            int size2 = (i6 * this.numH) + i;
            if (size2 > this.list.size()) {
                size2 = this.list.size();
            }
            while (i < size2 && !this.isLoad) {
                Command command2 = (Command) this.list.elementAt(i);
                int i8 = this.numW;
                int i9 = this.wSmall;
                int i10 = ((i % i8) * i9) + (i9 / 2);
                int i11 = this.hSmall;
                command2.paint(graphics, i10, ((i / i8) * i11) + (i11 / 2));
                i++;
            }
        } catch (Exception unused) {
            Cout.println("ERROR paintProduct");
        }
    }

    private void getShopTemplate() {
        this.list.removeAllElements();
        int size = Res.shopTemplate.size();
        for (int i0 = 0; i0 < size; i0++) {
            final int i = i0;
            final GemTemp gemTemp = (GemTemp) Res.shopTemplate.elementAt(i);
            if (gemTemp.shopType == focusTab && gemTemp.isSell) {
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        GemTemp shopByID = Res.getShopByID(gemTemp.id);
                        String str = (ItemInInventory.setTemp(shopByID.name, "0") + ItemInInventory.setTemp(shopByID.decript, "0")) + ItemInInventory.setTemp("Giá mua: " + shopByID.price + " " + (shopByID.typeMoney == 0 ? "lượng" : "xu"), "0");
                        WindowInfoScr windowInfoScr = WindowInfoScr.this;
                        windowInfoScr.setShowTextNew(str, (i % windowInfoScr.numW) * 18, (i / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i2, int i3) {
                        GameData.paintIcon(graphics, (short) (Res.getShopByID(gemTemp.id).idImage + 5500), i2, i3);
                        Font.smallFontBLUE.drawString(graphics, ((int) (gemTemp.id > 1 ? Res.getShopByID(gemTemp.id).value : (short) 1)) + "", i2 + 8, i3 + 1, 1);
                    }
                });
            }
        }
    }

    public void closeText() {
        this.isShowText = false;
        this.arrayKhamNgoc = null;
    }

    public void setShowText(String str, int i, int i2) {
        Cout.println("ITEM ITEM =" + str);
        if (!this.isShowText) {
            this.isShowText = true;
            this.wShow = 125;
            this.xShow = (((i + this.x) + this.x0) - (125 / 2)) + 9;
            this.yShow = i2 + this.y + this.y0 + 18;
            String[] strArrSplitFontBStrInLine = Font.arialFont11.splitFontBStrInLine(str, this.wShow - 10);
            this.showText = strArrSplitFontBStrInLine;
            this.hShow = (strArrSplitFontBStrInLine.length * 15) + 8 + (this.arrayKhamNgoc != null ? 16 : 0);
            int i3 = this.xShow - cmx;
            this.xShow = i3;
            if (i3 + 1 + this.wShow > GCanvas.w) {
                this.xShow = (GCanvas.w - this.wShow) - 1;
            } else if (this.xShow + 1 < 0) {
                this.xShow = -1;
            }
            int i4 = this.yShow - cmy;
            this.yShow = i4;
            if (this.countPop == 0) {
                this.countPop = -(this.showText.length - 1);
            }
            cmyLimText = 0;
            cmtoYText = 0;
            cmyText = 0;
            int i5 = this.hShow;
            if (i5 > 100) {
                int i6 = i5 - 100;
                cmyLimText = i6;
                if (i6 < 0) {
                    cmyLimText = 0;
                }
                this.hShow = 100;
            }
            if (i4 + this.hShow > GCanvas.h - 20) {
                this.yShow = (GCanvas.h - 20) - this.hShow;
                return;
            }
            return;
        }
        this.countPop = 0;
        closeText();
    }

    public void doBuyShopSpecial() {
        this.left = new Command("Mua", new IAction() {
            @Override
            public void perform() {
                WindowInfoScr.this.closeText();
                int size = Res.shopTemplate.size();
                if (WindowInfoScr.this.selected >= size) {
                    return;
                }
                if (GCanvas.gameScr.mainChar.isFullInventory()) {
                    GCanvas.startOKDlg("Hành trang đã đầy.");
                    return;
                }
                int i = 0;
                for (int i2 = 0; i2 < size; i2++) {
                    GemTemp gemTemp = (GemTemp) Res.shopTemplate.elementAt(i2);
                    if (gemTemp.shopType == WindowInfoScr.focusTab && gemTemp.isSell) {
                        if (i == WindowInfoScr.this.selected) {
                            final GemTemp gemTemp2 = (GemTemp) Res.shopTemplate.elementAt(i2);
                            GCanvas.startYesNoDlg("Bạn có muốn mua vật phẩm này không ?", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().buyItemShopSpecial(gemTemp2.id);
                                    GCanvas.startWaitDlg();
                                }
                            });
                            return;
                        }
                        i++;
                    }
                }
            }
        });
    }

    public int getCountPotion(int i) {
        return GCanvas.gameScr.mainChar.potions[i];
    }

    public Vector getInventori() {
        Vector vector = new Vector();
        try {
            int slot = 0;
            for (int p = 1; p < MainChar.listPotion.length; p++) {
                if (getCountPotion(p) > 0) {
                    final int potion = p;
                    final int ii = slot;
                    vector.addElement(new Command("", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr windowInfoScr = WindowInfoScr.this;
                            windowInfoScr.setShowTextNew(windowInfoScr.getPotionInfo(potion), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                        }
                    }) {
                        @Override
                        public void paint(Graphics graphics, int i3, int i4) {
                            Res.paintPotion(graphics, MainChar.listPotion[potion].index, i3, i4, 3);
                            Font.smallFontBLUE.drawString(graphics, WindowInfoScr.this.getCountPotion(potion) + "", i3 + 11, i4 + 4, 1);
                        }
                    });
                    slot++;
                }
            }
            int size = MainChar.shopItem.size();
            for (int i3 = 0; i3 < size; i3++) {
                final GemTemplate gemTemplate = (GemTemplate) MainChar.shopItem.elementAt(i3);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.showShopItemInfo(gemTemplate.id, ii);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i4, int i5) {
                        Res.paintPotion(graphics, Res.getShopByID(gemTemplate.id).idImage, i4, i5, 3);
                        Font.smallFontBLUE.drawString(graphics, gemTemplate.number + "", i4 + 11, i5 + 4, 1);
                    }
                });
                slot++;
            }
            for (int i4 = 0; i4 < MainChar.gemItemKhoa.size(); i4++) {
                final GemTemplate gemTemplate2 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i4);
                final GemTemp gemByID = Res.getGemByID(gemTemplate2.id);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.showGemItemKhoaInfo(gemTemplate2.id, (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i6, int i7) {
                        if (Res.getGemByID(gemTemplate2.id) != null) {
                            if (ii - (WindowInfoScr.index[WindowInfoScr.focusTab] == 0 ? WindowInfoScr.this.countTabMyitem * 42 : 0) != WindowInfoScr.this.selected || Math.abs(GCanvas.px - GCanvas.pxLast) > 10 || Math.abs(GCanvas.py - GCanvas.pyLast) > 10) {
                                graphics.setColor(-13500365);
                                graphics.fillRect(i6 - 12, i7 - 12, 25, 25);
                            }
                            Res.paintGemKhoa(graphics, Res.getGemByID(gemTemplate2.id).idImage, i6, i7);
                            if (gemByID.isEff != -1) {
                                Res.paintRectColor(graphics, i6 - 9, i7 - 9, 17, 17, gemByID.count, GemTemp.color[gemByID.isEff], -260847);
                                gemByID.count += 3;
                                if (gemByID.count > 68) {
                                    gemByID.count = 0;
                                }
                            }
                            Font.smallFontBLUE.drawString(graphics, gemTemplate2.number + "", i6 + 11, i7 + 4, 1);
                        }
                    }
                });
                slot++;
            }
            int size2 = MainChar.gemItem.size();
            for (int i6 = 0; i6 < size2; i6++) {
                final GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItem.elementAt(i6);
                final GemTemp gemByID2 = Res.getGemByID(gemTemplate3.id);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.showGemItemInfo(gemTemplate3.id, (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i7, int i8) {
                        if (Res.getGemByID(gemTemplate3.id) != null) {
                            Res.paintGem(graphics, Res.getGemByID(gemTemplate3.id).idImage, i7, i8);
                            if (gemByID2.isEff != -1) {
                                Res.paintRectColor(graphics, i7 - 9, i8 - 9, 17, 17, gemByID2.count, GemTemp.color[gemByID2.isEff], -260847);
                                gemByID2.count += 3;
                                if (gemByID2.count > 68) {
                                    gemByID2.count = 0;
                                }
                            }
                            Font.smallFontBLUE.drawString(graphics, gemTemplate3.number + "", i7 + 11, i8 + 4, 1);
                        }
                    }
                });
                slot++;
            }
            int size3 = MainChar.animal.size();
            for (int i7 = 0; i7 < size3; i7++) {
                final AnimalInfo animalInfo = (AnimalInfo) Char.animal.elementAt(i7);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        if (animalInfo.info.equals("")) {
                            return;
                        }
                        if (animalInfo.typeAnimal == 0) {
                            WindowInfoScr.this.setShowText(ItemInInventory.setTemp(animalInfo.name + " lv" + ((int) animalInfo.level), "0") + ItemInInventory.setTemp(animalInfo.info, "0"), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                        } else {
                            WindowInfoScr.this.setShowText(ItemInInventory.setTemp(animalInfo.name, "0") + ItemInInventory.setTemp(animalInfo.info, "0") + ItemInInventory.setTemp("Còn lại: " + (((long) animalInfo.totalTimeCon) - ((System.currentTimeMillis() - animalInfo.timeStart) / 60000)) + " phút", "0"), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                        }
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i8, int i9) {
                        animalInfo.paint(graphics, i8, i9);
                    }
                });
                slot++;
            }
            int size4 = Char.inventory.size();
            for (int i8 = 0; i8 < size4; i8++) {
                final ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i8);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr windowInfoScr = WindowInfoScr.this;
                        windowInfoScr.showItemInventoryInfo(itemInInventory, windowInfoScr.isSell, (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i9, int i10) {
                        if (itemInInventory.isSelling) {
                            graphics.setColor(-9070864);
                            graphics.fillRect(i9 - 8, i10 - 8, 16, 16);
                        }
                        itemInInventory.paint(graphics, i9, i10);
                    }
                });
                slot++;
            }
            if (MainChar.itemQuest != null) {
                int length = MainChar.itemQuest.length;
                final int ii = slot;
                for (int q = 0; q < length; q++) {
                    final int quest = q;
                    if (MainChar.itemQuest[quest] > 0) {
                        final int idImg = ItemQuest.ICON_IMG[quest] + 8000;
                        final String str2 = ((int) MainChar.itemQuest[quest]) + "";
                        vector.addElement(new Command("", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr windowInfoScr = WindowInfoScr.this;
                                windowInfoScr.setShowTextNew(windowInfoScr.getInfoItemQuest(quest), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                            }
                        }) {
                            @Override
                            public void paint(Graphics graphics, int i11, int i12) {
                                GameData.paintIcon(graphics, (short) idImg, i11, i12);
                                Font.smallFontBLUE.drawString(graphics, str2, i11 + 8, i12 + 1, 1);
                            }
                        });
                    }
                }
            }
        } catch (Exception unused) {
        }
        return vector;
    }

    protected void showGemItemInfo(short s, int i, int i2) {
        GemTemp gemByID = Res.getGemByID(s);
        String str = ItemInInventory.setTemp(gemByID.name, "0") + ItemInInventory.setTemp(gemByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + (gemByID.price / 5), "0");
        }
        setShowTextNew(str, i, i2);
    }

    protected void showGemItemKhoaInfo(short s, int i, int i2) {
        GemTemp gemByID = Res.getGemByID(s);
        String str = (ItemInInventory.setTemp(gemByID.name, "0") + " - Đã khóa") + ItemInInventory.setTemp(gemByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + (gemByID.price / 5), "0");
        }
        setShowTextNew(str, i, i2);
    }

    protected void showRealIDInfo(RealID realID, int i, int i2) {
        GemTemp gemByID = Res.getGemByID(realID.ID);
        String str = ItemInInventory.setTemp(gemByID.name, "0") + ItemInInventory.setTemp(gemByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán : " + realID.price, "0");
        }
        setShowTextNew(str, i, i2);
    }

    protected void showShopItemInfo(short s, int i) {
        GemTemp shopByID = Res.getShopByID(s);
        String str = ItemInInventory.setTemp(shopByID.name, "0") + ItemInInventory.setTemp(shopByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + GCanvas.getMoneys(shopByID.price / 5), "0");
        }
        int i2 = this.numW;
        setShowTextNew(str, (i % i2) * 18, (i / i2) * 18);
    }

    public String getInfoItemQuest(int i) {
        return ("0" + ItemQuest.NAME_ITEM[i]) + "\nSố lượng: " + ((int) MainChar.itemQuest[i]);
    }

    public String getPotionInfo(int i) {
        short s;
        GemTemp gemTemp;
        if (i >= 7 && i < 14) {
            if (i < 7 || i >= 14) {
                return Res.PORTION_ITEM_NAME_CLAN + "\n Số lượng: " + GCanvas.gameScr.mainChar.potions[i];
            }
            return ("0" + MainChar.listPotion[i].name) + "\nSố lượng: " + GCanvas.gameScr.mainChar.potions[i];
        }
        String str = ("0" + MainChar.listPotion[i].name) + "\n Số lượng: " + GCanvas.gameScr.mainChar.potions[i];
        if (this.isSell) {
            str = str + "\n Không thể bán lại";
        }
        if (this.potionShop.contains(i + "")) {
            int size = Res.shopTemplate.size();
            int i2 = 0;
            while (true) {
                if (i2 >= size) {
                    gemTemp = null;
                    break;
                }
                if (((GemTemp) Res.shopTemplate.elementAt(i2)).id == i - 19) {
                    gemTemp = (GemTemp) Res.shopTemplate.elementAt(i2);
                    break;
                }
                i2++;
            }
            return (gemTemp == null || (i >= 69 && i <= 77)) ? str : str + ItemInInventory.setTemp(gemTemp.decript, "0");
        }
        StringBuilder sbAppend = new StringBuilder().append(str);
        StringBuilder sb = new StringBuilder("\nHồi phục: ");
        int i3 = i - 19;
        if (i3 < 0) {
            s = Res.potionTemplates[i].recovered;
        } else {
            s = Res.potionTemplates[i3].recovered;
        }
        return sbAppend.append(ItemInInventory.setTemp(sb.append((int) s).append(" ").append(MainChar.listPotion[i].name2).toString(), "0")).toString();
    }

    public void doSelectedInventori() {
        final int i = this.selected + (index[focusTab] == 0 ? this.countTabMyitem * 42 : 0);
        int i2 = 0;
        for (int i3 = 1; i3 < GCanvas.gameScr.mainChar.potions.length; i3++) {
            if (GCanvas.gameScr.mainChar.potions[i3] > 0) {
                if (i == i2) {
                    showMenuForPotion(i3);
                    return;
                }
                i2++;
            }
        }
        int i4 = i - i2;
        if (i4 < MainChar.shopItem.size()) {
            final GemTemplate gemTemplate = (GemTemplate) MainChar.shopItem.elementAt(i4);
            Vector vector = new Vector();
            vector.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doUseSpecialItem(gemTemplate.rID);
                }
            }));
            vector.addElement(cmdDelItem(gemTemplate, 1, MainChar.shopItem, (byte) 0));
            GCanvas.menu.startAt(vector, 2);
            return;
        }
        int size = i4 - MainChar.shopItem.size();
        if (size < MainChar.gemItemKhoa.size()) {
            final GemTemplate gemTemplate2 = (GemTemplate) MainChar.gemItemKhoa.elementAt(size);
            if (this.isSell) {
                GCanvas.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + (Res.getGemByID(gemTemplate2.id).price / 5) + "$ không?", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().sellGemItem(gemTemplate2.rID, (byte) 1);
                        GCanvas.startWaitDlg("Đang bán..", true);
                        WindowInfoScr.this.list.removeElementAt(i);
                    }
                });
                return;
            }
            Vector vector2 = new Vector();
            GemTemp gemByID = Res.getGemByID(gemTemplate2.id);
            if (gemByID != null && gemByID.type == 4) {
                vector2.addElement(new Command("Sử dụng", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().douseGemItem(gemTemplate2.id, (byte) 1);
                    }
                }));
            }
            vector2.addElement(cmdDelItem(gemTemplate2, 0, MainChar.gemItemKhoa, (byte) 1));
            GCanvas.menu.startAt(vector2, 2);
            return;
        }
        int size2 = size - MainChar.gemItemKhoa.size();
        if (size2 < MainChar.gemItem.size()) {
            final GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItem.elementAt(size2);
            if (this.isSell) {
                GCanvas.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + (Res.getGemByID(gemTemplate3.id).price / 5) + "$ không?", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().sellGemItem(gemTemplate3.rID, (byte) 0);
                        GCanvas.startWaitDlg("Đang bán..", true);
                        WindowInfoScr.this.list.removeElementAt(i);
                    }
                });
                return;
            }
            Vector vector3 = new Vector();
            GemTemp gemByID2 = Res.getGemByID(gemTemplate3.id);
            if (gemByID2 != null && gemByID2.type == 4) {
                vector3.addElement(new Command("Sử dụng", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().douseGemItem(gemTemplate3.id, (byte) 0);
                    }
                }));
            }
            vector3.addElement(cmdDelItem(gemTemplate3, 0, MainChar.gemItem, (byte) 0));
            GCanvas.menu.startAt(vector3, 2);
            return;
        }
        int size3 = size2 - MainChar.gemItem.size();
        if (size3 < Char.animal.size()) {
            final AnimalInfo animalInfo = (AnimalInfo) MainChar.animal.elementAt(size3);
            Vector vector4 = new Vector();
            vector4.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    if ((GCanvas.gameScr.mainChar.useHorse > -1 || GCanvas.gameScr.mainChar.idhorse > -1) && animalInfo.typeAnimal == 0) {
                        GCanvas.startOKDlg("Không thể cưỡi.");
                    } else {
                        GameService.gI().doRideAnimal(animalInfo.id, animalInfo.typeAnimal);
                    }
                }
            }));
            GCanvas.menu.startAt(vector4, 2);
            return;
        }
        int size4 = size3 - MainChar.animal.size();
        if (size4 < Char.inventory.size()) {
            final ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(size4);
            if (!this.isSell) {
                Vector vector5 = new Vector();
                vector5.addElement(new Command("Sử dụng", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doUseItem(itemInInventory);
                    }
                }));
                if (itemInInventory.colorName == 1 && itemInInventory.level >= 50 && !ItemInInventory.isAnimalArmor(itemInInventory.getTemplate().type)) {
                    vector5.addElement(new Command("Tách trang bị", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.startYesNoDlg("Bạn có muốn tách nguyên liệu từ vật phẩm này không?", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestTachNguyenLIeu(itemInInventory.itemID, 0, -1, 0);
                                    GCanvas.endDlg();
                                }
                            });
                        }
                    }));
                }
                vector5.addElement(new Command("Bỏ ra đất", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.startYesNoDlg("Vật phẩm sẽ mất khi bạn bỏ ra đất. Bạn có muốn tiếp tục không?", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().giveItemToGround(itemInInventory.itemID);
                                GCanvas.endDlg();
                            }
                        });
                    }
                }));
                if (itemInInventory.colorName != 0 && itemInInventory.colorName != 4) {
                    if (itemInInventory.colorName == 1) {
                        vector5.addElement(new Command("Thăng cấp thường", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(10, itemInInventory.itemID);
                            }
                        }));
                        vector5.addElement(new Command("Thăng cấp giữ thuộc tính", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(11, itemInInventory.itemID);
                            }
                        }));
                    }
                    if (itemInInventory.nameCharSeal.equals("")) {
                        vector5.addElement(new Command("Đóng dấu", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(14, itemInInventory.itemID);
                            }
                        }));
                    }
                    if (!itemInInventory.nameCharSeal.toLowerCase().equals(GCanvas.gameScr.mainChar.name.toLowerCase())) {
                        vector5.addElement(new Command("Đóng dấu lại", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(14, itemInInventory.itemID);
                            }
                        }));
                    } else {
                        ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                        if ((item.type >= 3 && item.type <= 7) || item.type == 8 || item.type == 9) {
                            vector5.addElement(new Command("Sửa thuộc tính kỹ năng", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(15, itemInInventory.itemID);
                                }
                            }));
                        }
                    }
                    if (itemInInventory.lock == 0) {
                        vector5.addElement(new Command("Khoá", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(12, itemInInventory.itemID);
                            }
                        }));
                    }
                    if (itemInInventory.lock == 1) {
                        vector5.addElement(new Command("Khoá lại", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(12, itemInInventory.itemID);
                            }
                        }));
                    }
                    vector5.addElement(new Command("Sửa thuộc tính khác", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().doRequestActionCheDo(13, itemInInventory.itemID);
                        }
                    }));
                }
                GCanvas.menu.startAt(vector5, 2);
                return;
            }
            ItemTemplate item2 = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            GCanvas.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + ((item2.price / 5) + (item2.plus * 1000)) + "$ không?", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().sellItem(itemInInventory.itemID);
                    GCanvas.startWaitDlg("Đang bán..", true);
                    WindowInfoScr.this.list.removeElementAt(i);
                }
            });
        }
    }

    private Command cmdDelItem(final GemTemplate gemTemplate, final int i, Vector vector, final byte b) {
        return new Command("Bỏ ra đất", new IAction() {
            @Override
            public void perform() {
                GCanvas.startYesNoDlg("Bạn có muốn bỏ hết vật phẩm này không ?", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().dellGemItem(gemTemplate.id, i, b);
                        GCanvas.endDlg();
                    }
                });
            }
        });
    }

    private void showMenuForPotion(final int i) {
        Vector vector = new Vector();
        if (i >= 14 && i <= 18 && GCanvas.gameScr.mainChar.pk > 0) {
            vector.addElement(new Command("Tháo khăn", new IAction() {
                @Override
                public void perform() {
                    if (System.currentTimeMillis() - GCanvas.gameScr.mainChar.timeWearScarves < 180000) {
                        GCanvas.startOKDlg("Sau 3 phút tính từ thời điểm đeo khăn mới được tháo khăn.");
                    } else {
                        GCanvas.gameScr.doUsePotion(GCanvas.gameScr.mainChar.pk);
                        GCanvas.gameScr.mainChar.pk = (byte) 0;
                    }
                }
            }));
        } else {
            vector.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    int i2;
                    if (GCanvas.gameScr.mainChar.iskiller && (i2 = i) >= 14 && i2 <= 18) {
                        GCanvas.startOKDlg("Không thể đeo khăn khi đang phạm tội.");
                        return;
                    }
                    GCanvas.gameScr.doUsePotion(i);
                    if (GCanvas.gameScr.mainChar.potions[i] == 0) {
                        WindowInfoScr.this.list.removeElementAt(WindowInfoScr.this.selected);
                    }
                    int i3 = i;
                    if (i3 < 14 || i3 > 18) {
                        return;
                    }
                    GCanvas.gameScr.mainChar.timeWearScarves = System.currentTimeMillis();
                    GCanvas.gameScr.mainChar.pk = (byte) i;
                }
            }));
        }
        if (i < 14 || i >= 21) {
            vector.addElement(new Command("Cho vào phím tắt", new IAction() {
                @Override
                public void perform() {
                    Vector vector2 = new Vector();
                    for (int i20 = 0; i20 < 2; i20++) {
                        final int i2 = i20;
                        vector2.addElement(new Command("Phím số " + ((i2 * 2) + 7), new IAction() {
                            @Override
                            public void perform() {
                                MainChar.quickSlot[GameScr.indexTabSlot][i2 + 3].setIsPotion(i);
                                RMS.saveQuickSlot();
                            }
                        }));
                    }
                    GCanvas.menu.startAt(vector2, 2);
                }
            }));
        }
        vector.addElement(new Command("Bỏ ra đất", new IAction() {
            @Override
            public void perform() {
                GCanvas.startYesNoDlg("Bạn muốn bỏ vật phẩm này không ?", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.endDlg();
                        GameService.gI().dellPotion(i);
                        WindowInfoScr.this.list.removeElementAt(WindowInfoScr.this.selected);
                        GCanvas.gameScr.mainChar.potions[i] = 0;
                        GCanvas.currentDialog = null;
                    }
                });
            }
        }));
        GCanvas.menu.startAt(vector, 2);
    }

    protected void doUseItem(ItemInInventory itemInInventory) {
        ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        if (item.gender != 0 && item.gender != GCanvas.gameScr.mainChar.gender) {
            GCanvas.startOKDlg("Vật phẩm này chỉ dành cho " + Res.GENDERNAME[item.gender] + ".");
            return;
        }
        if (item.clazz != -1 && item.clazz != GCanvas.gameScr.mainChar.clazz && isWeapone(item.type)) {
            GCanvas.startOKDlg("Vật phẩm này chỉ dành cho " + CreateCharScr.clazz[item.clazz][CreateCharScr.selectGender].name + ".");
        } else if (item.level > GCanvas.gameScr.mainChar.level) {
            GCanvas.startOKDlg("Bạn phải đạt cấp " + ((int) item.level) + " để có thể dùng.");
        } else {
            GameService.gI().useItem(itemInInventory.itemID);
        }
    }

    public void showCharWearingInfo() {
        try {
            int i = this.selected;
            if (i % 3 == 1) {
                if (charWearing.isDoing) {
                    int i2 = this.selected;
                    if (i2 != 4) {
                        int i3 = ((i2 / 3) * 2) + ((i2 % 3) - (i2 % 3 > 0 ? 1 : 0));
                        int size = charWearing.wearingItems.size();
                        for (int i4 = 0; i4 < size; i4++) {
                            ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingItems.elementAt(i4);
                            if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type == 13) {
                                byte[] bArr = posWearChar[i3];
                                showItemInventoryInfo(itemInInventory, false, bArr[0] + 5, bArr[1] - 2);
                            }
                        }
                        return;
                    }
                    return;
                }
                if (index.length == 1) {
                    if (this.selected != 4) {
                        Vector vector = new Vector();
                        vector.addElement(new Command("Thông tin", new IAction() {
                            @Override
                            public void perform() {
                                WindowInfoScr.this.setShowTextNew(WindowInfoScr.charWearing.infoAnimal, 80, 120);
                                GCanvas.endDlg();
                            }
                        }));
                        vector.addElement(new Command("Trang bị linh thú", new IAction() {
                            @Override
                            public void perform() {
                                Char r0 = (Char) GCanvas.gameScr.findCharByID(GCanvas.gameScr.saveIDViewInfoAnimal);
                                if (r0 != null) {
                                    GCanvas.gameScr.gameService.requestSomeOneInfo(r0.ID, (byte) 1);
                                } else {
                                    WindowInfoScr.this.right.action.perform();
                                }
                                GCanvas.endDlg();
                            }
                        }));
                        GCanvas.menu.startAt(vector, 3);
                        return;
                    }
                    int size2 = charWearing.wearingItems.size();
                    for (int i5 = 0; i5 < size2; i5++) {
                        ItemInInventory itemInInventory2 = (ItemInInventory) charWearing.wearingItems.elementAt(i5);
                        if (Res.getItem(itemInInventory2.charClazz, itemInInventory2.idTemplate).type == 19) {
                            byte[] bArr2 = posWearChar[9];
                            showItemInventoryInfo(itemInInventory2, false, bArr2[0] + 5, bArr2[1] - 2);
                            return;
                        }
                    }
                    return;
                }
                if (this.selected != 4) {
                    Vector vector2 = new Vector();
                    vector2.addElement(new Command("Linh thú", new IAction() {
                        @Override
                        public void perform() {
                            if (WindowInfoScr.charWearing.infoAnimal.equals("")) {
                                return;
                            }
                            WindowInfoScr.this.setShowTextNew(ItemInInventory.setTemp(WindowInfoScr.charWearing.infoAnimal, "0"), 80, 120);
                        }
                    }));
                    vector2.addElement(new Command("Thú cưng", new IAction() {
                        @Override
                        public void perform() {
                            if (WindowInfoScr.charWearing.myPet != null) {
                                WindowInfoScr.this.setShowTextNew(ItemInInventory.setTemp(WindowInfoScr.charWearing.myPet.infoPet, "0") + ItemInInventory.setTemp("Còn lại: " + (((long) WindowInfoScr.charWearing.myPet.totalTime) - ((System.currentTimeMillis() - WindowInfoScr.charWearing.myPet.timeStart) / 60000)) + " phút", "0"), 80, 120);
                            }
                        }
                    }));
                    GCanvas.menu.startAt(vector2, 3);
                    return;
                }
                int size3 = charWearing.wearingItems.size();
                for (int i6 = 0; i6 < size3; i6++) {
                    ItemInInventory itemInInventory3 = (ItemInInventory) charWearing.wearingItems.elementAt(i6);
                    if (Res.getItem(itemInInventory3.charClazz, itemInInventory3.idTemplate).type == 19) {
                        byte[] bArr3 = posWearChar[9];
                        showItemInventoryInfo(itemInInventory3, false, bArr3[0] + 5, bArr3[1] - 2);
                        return;
                    }
                }
                return;
            }
            int i7 = ((i / 3) * 2) + ((i % 3) - (i % 3 > 0 ? 1 : 0));
            int size4 = charWearing.wearingItems.size();
            int i8 = 0;
            for (int i9 = 0; i9 < size4; i9++) {
                ItemInInventory itemInInventory4 = (ItemInInventory) charWearing.wearingItems.elementAt(i9);
                ItemTemplate item = Res.getItem(itemInInventory4.charClazz, itemInInventory4.idTemplate);
                short s = item.type;
                byte b = idWearChar[i7];
                if (s == b || (b == -1 && item.type > 2 && item.type < 8)) {
                    i8++;
                    if (i7 != 7 || i8 != 1) {
                        byte[] bArr4 = posWearChar[i7];
                        showItemInventoryInfo(itemInInventory4, false, bArr4[0] + 5, bArr4[1] - 2);
                        return;
                    }
                }
            }
        } catch (Exception unused) {
            Cout.println("Ngoai mang");
        }
    }

    public void showInfoWearingAnimal() {
        try {
            if (charWearing.wearingAnimal == null || charWearing.wearingAnimal.size() <= 0) {
                return;
            }
            int size = charWearing.wearingAnimal.size();
            for (int i = 0; i < size; i++) {
                ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingAnimal.elementAt(i);
                if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type == idWearAnimal[this.selected]) {
                    byte[] bArr = posWearChar[0];
                    showItemInventoryInfo(itemInInventory, false, bArr[0] + 5, bArr[1] - 2);
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }

    public void showItemInventoryInfo(ItemInInventory itemInInventory, boolean z, int i, int i2) {
        this.arrayKhamNgoc = null;
        if (itemInInventory.totalKham > 0) {
            this.arrayKhamNgoc = new byte[itemInInventory.totalKham];
            int i3 = 0;
            for (int i4 = 0; i4 < itemInInventory.totalKham; i4++) {
                this.arrayKhamNgoc[i4] = -1;
            }
            for (int i5 = 0; i5 < itemInInventory.allAttribute.size(); i5++) {
                InfoAttributeItem infoAttributeItem = (InfoAttributeItem) itemInInventory.allAttribute.elementAt(i5);
                if (infoAttributeItem.getColorPaint(false) == 3) {
                    byte b = 0;
                    while (i3 < itemInInventory.numKham) {
                        this.arrayKhamNgoc[b] = Res.idNgocKham[infoAttributeItem.getID() - 10];
                        i3++;
                        b = (byte) (b + 1);
                    }
                    break;
                }
            }
        }
        setShowTextNew(itemInInventory.getDescription(z), 145, -10);
        if (itemInInventory.isEnoughData) {
            return;
        }
        itemInInventory.isEnoughData = true;
        GameService gameServiceGI = GameService.gI();
        short s = itemInInventory.itemID;
        Char r8 = charWearing;
        gameServiceGI.requestItemInfo(s, r8 == null ? GCanvas.gameScr.mainChar.ID : r8.ID);
    }

    public void dofireBasePoint() {
        if (GCanvas.gameScr.mainChar.basePointLeft == 0) {
            GCanvas.startOKDlg("Đã hết điểm tiềm năng để cộng. Xin đánh lên level để có điểm tiềm năng.");
        } else {
            GCanvas.inputDlg.setInfo("Nhập số", new IAction() {
                @Override
                public void perform() {
                    String text = GCanvas.inputDlg.tfInput.getText();
                    if (text.equals("")) {
                        return;
                    }
                    try {
                        int i = Integer.parseInt(text);
                        if (i > GCanvas.gameScr.mainChar.basePointLeft) {
                            GCanvas.startOKDlg("Bạn chỉ còn " + ((int) GCanvas.gameScr.mainChar.basePointLeft) + " điểm tiềm năng");
                        } else {
                            GameService.gI().addBasePoint(WindowInfoScr.this.selected, i);
                            GCanvas.startWaitDlg();
                        }
                    } catch (Exception unused) {
                        GCanvas.startOKDlg("Có lỗi xảy ra. Vui lòng chỉ nhập số.");
                    }
                }
            }, 1, 10, true);
            GCanvas.inputDlg.show();
        }
    }

    public void setCmdLeftTrade() {
        this.left = new Command("Xong", new IAction() {
            @Override
            public void perform() {
                WindowInfoScr.this.closeText();
                GCanvas.gameScr.gameService.sendOkTrade(GCanvas.gameScr.mainChar.ID);
                WindowInfoScr.this.left = new Command("Xin chờ", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.gameService.sendCancelTrade(GCanvas.gameScr.mainChar.ID);
                        GCanvas.gameScr.mainChar.isTrade = false;
                        GCanvas.currentDialog = null;
                    }
                });
            }
        });
    }

    public void setListSellItem(Vector vector, int i) {
        this.list.removeAllElements();
        int size = vector.size();
        int co = 0;
        for (int i3 = 0; i3 < size; i3++) {
            final ItemInInventory itemInInventory = (ItemInInventory) GCanvas.gameScr.shop.elementAt(i3);
            if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type == i || i == -1) {
                final int ii = co;
                co++;
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.setShowTextNew(itemInInventory.getTemplateDescription(), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i4, int i5) {
                        itemInInventory.paint(graphics, i4, i5);
                    }
                });
            }
        }
    }

    public boolean setMaxxreadyBuy() {
        if (this.readyBuy.size() < 100) {
            return false;
        }
        GCanvas.startOKDlg("Bạn chỉ được mua 100 vật phẩm một lần.");
        return true;
    }

    public void doSetLeftSellWeapon(int i) {
        String str;
        closeText();
        if (setMaxxreadyBuy()) {
            return;
        }
        ItemInInventory chon = null;
        if (i != -1) {
            int size = GCanvas.gameScr.shop.size();
            int i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                ItemInInventory itemInInventory2 = (ItemInInventory) GCanvas.gameScr.shop.elementAt(i3);
                if (Res.getItem(itemInInventory2.charClazz, itemInInventory2.idTemplate).type == i) {
                    if (i2 == this.selected) {
                        chon = itemInInventory2;
                        break;
                    }
                    i2++;
                }
            }
        } else {
            try {
                if (this.selected >= GCanvas.gameScr.shop.size()) {
                    return;
                }
                chon = (ItemInInventory) GCanvas.gameScr.shop.elementAt(this.selected);
            } catch (Exception unused) {
            }
        }
        if (GCanvas.gameScr.mainChar.FullInventory() >= GCanvas.gameScr.mainChar.limItemBag * 42) {
            GCanvas.startOKDlg("Hành trang đã đầy");
            return;
        }
        final ItemInInventory itemInInventory = chon;
        final ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        if (item.price > GCanvas.gameScr.mainChar.charXu) {
            GCanvas.startOKDlg("Hết tiền");
            return;
        }
        if (item.ndayLoan == 0) {
            str = "Bạn có muốn mua " + item.name + ", Giá: " + item.price + "$ không?";
        } else {
            str = "Bạn có muốn thuê " + item.name + ", Giá: " + item.price + "Lượng không?";
        }
        GCanvas.startYesNoDlg(str, new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.mainChar.charXu -= (long) item.price;
                WindowInfoScr.this.readyBuy.addElement(itemInInventory);
                GCanvas.startOKDlg("Đã mua. Món đồ đang ở trong hành trang.", false);
            }
        });
    }

    public void dosetLeftSellGemItem() {
        final int i;
        boolean z;
        closeText();
        if (!setMaxxreadyBuy() && this.selected < GCanvas.gameScr.shop.size()) {
            final GemTemp gemTemp = (GemTemp) GCanvas.gameScr.shop.elementAt(this.selected);
            GemTemplate gemByID = GemTemplate.getGemByID(gemTemp.id);
            if (gemByID != null) {
                i = gemByID.number;
                z = true;
            } else {
                i = 0;
                z = false;
            }
            if (!z && GCanvas.gameScr.mainChar.isFullInventory()) {
                GCanvas.startOKDlg("Hành trang đã đầy");
                return;
            }
            final GemTemp gemByID2 = Res.getGemByID(gemTemp.id);
            GCanvas.inputDlg.setInfo("Số lượng: ", new IAction() {
                @Override
                public void perform() {
                    try {
                        final int i2 = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                        if (i2 < 0) {
                            return;
                        }
                        if ((gemByID2.typeMoney == 0 && gemByID2.price * i2 > GCanvas.gameScr.mainChar.charXu) || (gemByID2.typeMoney == 1 && gemByID2.price * i2 > GCanvas.gameScr.mainChar.luong)) {
                            GCanvas.startOKDlg("Hết tiền");
                        } else {
                            GCanvas.startYesNoDlg("Bạn có muốn mua " + i2 + " " + gemByID2.name + ", Giá: " + (gemByID2.price * i2) + (gemByID2.typeMoney == 0 ? "$" : "lượng") + " không?", new IAction() {
                                @Override
                                public void perform() {
                                    int size = WindowInfoScr.this.readyBuy.size();
                                    int i3 = 0;
                                    while (true) {
                                        if (i3 < size) {
                                            GemTemp gemTemp2 = (GemTemp) WindowInfoScr.this.readyBuy.elementAt(i3);
                                            if (gemTemp2.id == gemTemp.id) {
                                                if (i + gemTemp2.number + i2 > 300) {
                                                    GCanvas.startOKDlg("Không được mua nhiều hơn 300 cái.");
                                                    return;
                                                } else {
                                                    gemTemp2.number = (short) (gemTemp2.number + i2);
                                                    break;
                                                }
                                            }
                                            i3++;
                                        } else {
                                            GemTemp gemTemp3 = new GemTemp();
                                            gemTemp3.id = gemTemp.id;
                                            gemTemp3.number = (short) i2;
                                            WindowInfoScr.this.readyBuy.addElement(gemTemp3);
                                            break;
                                        }
                                    }
                                    if (gemByID2.typeMoney == 0) {
                                        GCanvas.gameScr.mainChar.charXu -= (long) (gemByID2.price * i2);
                                    } else {
                                        GCanvas.gameScr.mainChar.luong -= gemByID2.price * i2;
                                    }
                                    GCanvas.startOKDlg("Đã mua. Món đồ đang ở trong hành trang.");
                                }
                            });
                        }
                    } catch (Exception unused) {
                        GCanvas.startOKDlg("Chỉ được nhập số.");
                    }
                }
            }, 1, 4, true);
            GCanvas.inputDlg.show();
        }
    }

    private void setListSellGemItem(Vector vector) {
        this.list.removeAllElements();
        int size = vector.size();
        for (int i0 = 0; i0 < size; i0++) {
            final int i = i0;
            final GemTemp gemTemp = (GemTemp) vector.elementAt(i);
            if (gemTemp.isSell) {
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        String str = ItemInInventory.setTemp(gemTemp.name, "0") + ItemInInventory.setTemp(gemTemp.decript, "0") + ItemInInventory.setTemp("Giá: " + gemTemp.price + (gemTemp.typeMoney == 0 ? " xu" : " lượng"), "0");
                        WindowInfoScr windowInfoScr = WindowInfoScr.this;
                        windowInfoScr.setShowTextNew(str, (i % windowInfoScr.numW) * 18, (i / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i2, int i3) {
                        Res.paintGem(graphics, gemTemp.idImage, i2, i3);
                    }
                });
            }
        }
    }

    private void setListPotion() {
        this.list.removeAllElements();
        int size = GCanvas.gameScr.shop.size();
        for (int i = 0; i < size; i++) {
            final ItemInInventory itemInInventory = (ItemInInventory) GCanvas.gameScr.shop.elementAt(i);
            this.list.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.setShowTextNew(itemInInventory.getTemplateDescription(), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                }
            }) {
                @Override
                public void paint(Graphics graphics, int i2, int i3) {
                    GameData.paintIcon(graphics, (short) (MainChar.listPotion[itemInInventory.potionType].index + 5500), i2, i3);
                }
            });
        }
    }

    public void doSetLeftPotion() {
        int s = 0;
        this.isShowText = false;
        if (setMaxxreadyBuy()) {
            return;
        }
        ItemInInventory itemInInventory = (ItemInInventory) GCanvas.gameScr.shop.elementAt(this.selected);
        int size = this.readyBuy.size();
        int found = -1;
        if (size > 0) {
            for (int k = 0; k < size; k++) {
                if (((ItemInInventory) this.readyBuy.elementAt(k)).potionType == itemInInventory.potionType) {
                    s = itemInInventory.number;
                    found = k;
                    break;
                }
            }
        }
        int bbIndex;
        if (found == -1) {
            this.readyBuy.addElement(itemInInventory);
            bbIndex = size;
        } else {
            bbIndex = found;
        }
        if (s + GCanvas.gameScr.mainChar.potions[itemInInventory.potionType] >= 999) {
            int i2 = this.selected;
            int i3 = this.numW;
            setShowText("MAX: 999", (i2 % i3) * 18, (i2 / i3) * 18);
            return;
        }
        final int price = Res.potionTemplates[itemInInventory.potionType].price;
        if (price > GCanvas.gameScr.mainChar.charXu) {
            GCanvas.startOKDlg("Hết tiền");
            return;
        }
        final int bb = bbIndex;
        GCanvas.inputDlg.setInfo("Số lượng: ", new IAction() {
            @Override
            public void perform() {
                try {
                    int i4 = Integer.parseInt(GCanvas.inputDlg.tfInput.getText());
                    if (i4 < 0) {
                        GCanvas.startOKDlg("Không được nhập số âm.");
                        return;
                    }
                    if (i4 > 120) {
                        GCanvas.startOKDlg("Chỉ được mua nhiều nhất 120 đơn vị một lần.");
                        return;
                    }
                    ItemInInventory itemInInventory2 = (ItemInInventory) WindowInfoScr.this.readyBuy.elementAt(bb);
                    itemInInventory2.number = (short) (itemInInventory2.number + i4);
                    GCanvas.gameScr.mainChar.charXu -= (long) price;
                    GCanvas.endDlg();
                    WindowInfoScr.this.countPop = 0;
                    WindowInfoScr.this.closeText();
                    WindowInfoScr.this.setShowTextNew(itemInInventory2.getTemplateDescription(), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                } catch (Exception unused) {
                    GCanvas.startOKDlg("Không được nhập chữ");
                }
            }
        }, 1, 10, true);
        GCanvas.inputDlg.show();
    }

    public void doBuyItem() {
        if (this.readyBuy.size() > 0) {
            focusTab = this.saveFocusTab;
            GCanvas.startWaitDlg("Xin chờ", true);
            byte b = index[focusTab];
            if (b == 10) {
                GameService.gI().buyGemItem(this.readyBuy);
            } else if (b == 19) {
                GameService.gI().buyPotion(this.readyBuy);
            } else {
                GameService.gI().buyItem(this.readyBuy);
            }
            this.readyBuy.removeAllElements();
        }
        byte b2 = index[focusTab];
        if (b2 != 21 && b2 != 25) {
            if (b2 == 22) {
                GCanvas.gameScr.gameService.finishPutItem2Bag();
                return;
            } else {
                if (b2 == 8) {
                    GCanvas.gameScr.gameService.sendCancelTrade(GCanvas.gameScr.mainChar.ID);
                    GCanvas.gameScr.mainChar.isTrade = false;
                    return;
                }
                return;
            }
        }
        if (b2 == 21) {
            GameService.gI().doImbueItem(-1);
        } else {
            GameService.gI().doKhamItem(-1);
        }
        int size = MainChar.gemitemImbue.size();
        for (int i = 0; i < size; i++) {
            GemTemplate gemByID = GemTemplate.getGemByID(((RealID) MainChar.gemitemImbue.elementAt(i)).ID);
            if (gemByID != null) {
                gemByID.number++;
                break;
            }
        }
        MainChar.gemitemImbue.removeAllElements();
        GCanvas.gameScr.mainChar.itemImbue = null;
        this.imgWeaponAvatar = null;
        this.idImbue = -1;
    }

    public void resetImbue() {
        MainChar.gemitemImbue.removeAllElements();
    }

    private void setQuestInfo() {
        try {
            this.questInfo = new Vector();
            String infoQuestPaint = getInfoQuestPaint(0);
            String infoQuestPaint2 = getInfoQuestPaint(1);
            String infoQuestPaint3 = getInfoQuestPaint(2);
            if (!infoQuestPaint.equals("")) {
                String[] strArrSplit = Util.split(infoQuestPaint, "|");
                this.questInfo.addElement(GameScr.mainQuest.name);
                for (String str : strArrSplit) {
                    this.questInfo.addElement(str);
                }
            }
            if (!infoQuestPaint2.equals("")) {
                String[] strArrSplit2 = Util.split(infoQuestPaint2, "|");
                this.questInfo.addElement(GameScr.subQuest.name);
                for (String str2 : strArrSplit2) {
                    this.questInfo.addElement(str2);
                }
            }
            if (infoQuestPaint3.equals("")) {
                return;
            }
            String[] strArrSplit3 = Util.split(infoQuestPaint3, "|");
            this.questInfo.addElement(GameScr.clanQuest.name);
            for (String str3 : strArrSplit3) {
                this.questInfo.addElement(str3);
            }
        } catch (Exception unused) {
        }
    }

    public void questClan(String str, byte b) {
        this.questInfo = new Vector();
        for (String str2 : Font.normalFont[0].splitFontBStrInLine(str, 100)) {
            this.questInfo.addElement(str2);
        }
        GCanvas.endDlg();
    }

    private void paintShowTextNew(Graphics graphics) {
        int i;
        byte[] bArr;
        graphics.setClip(this.xShow, this.yShow + 1, this.wShow - 20, GCanvas.h - 108);
        graphics.ClipRec(this.xShow, this.yShow + 1, this.wShow - 20, GCanvas.h - 108);
        graphics.translate(0, -cmyText);
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int length = this.showText.length;
            i = this.countPop;
            if (i2 >= length + i) {
                break;
            }
            if (i2 == 1 && (bArr = this.arrayKhamNgoc) != null && bArr.length > 0) {
                for (int i4 = 0; i4 < this.arrayKhamNgoc.length; i4++) {
                    int i5 = i4 * 20;
                    graphics.drawImage(Res.imgKham, this.xShow + 12 + i5, this.yShow + 12 + i3, 3);
                    byte b = this.arrayKhamNgoc[i4];
                    if (b != -1) {
                        Res.paintGem(graphics, b, this.xShow + 12 + i5, this.yShow + 12 + i3);
                    }
                }
                i3 += 18;
            }
            if (!this.showText[i2].equals("")) {
                byte bCharAt = (byte) (this.showText[i2].charAt(0) - '0');
                boolean zIsDegit = isDegit(this.showText[i2].charAt(0));
                if (!zIsDegit) {
                    bCharAt = 0;
                }
                Font[] fontArr = Font.normalFont;
                if (bCharAt >= 6) {
                    bCharAt = 0;
                }
                fontArr[bCharAt].drawString(graphics, this.showText[i2].substring(zIsDegit ? 1 : 0), this.xShow + 4, this.yShow + 4 + i3, 0);
                i3 += 15;
                Cout.println("tt " + this.showText[i2]);
            }
            i2++;
        }
        if (i < 0) {
            this.countPop = i + 1;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void doSetLeftPetEat(final int i) {
        this.left = new Command("Mua", new IAction() {
            @Override
            public void perform() {
                ItemInInventory found;
                WindowInfoScr.this.closeText();
                if (WindowInfoScr.this.setMaxxreadyBuy()) {
                    return;
                }
                if (i != -1) {
                    int size = GCanvas.gameScr.shop.size();
                    int i2 = 0;
                    found = null;
                    for (int i3 = 0; i3 < size; i3++) {
                        ItemInInventory it = (ItemInInventory) GCanvas.gameScr.shop.elementAt(i3);
                        if (Res.getItem(it.charClazz, it.idTemplate).type == i) {
                            if (i2 == WindowInfoScr.this.selected) {
                                found = it;
                                break;
                            }
                            i2++;
                        }
                    }
                } else {
                    found = (ItemInInventory) GCanvas.gameScr.shop.elementAt(WindowInfoScr.this.selected);
                }
                final ItemInInventory itemInInventory = found;
                final ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                GCanvas.startYesNoDlg("Bạn có muốn cho thú cưng ăn vật phẩm này không?", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.gameScr.mainChar.charXu -= (long) item.price;
                        WindowInfoScr.this.readyBuy.addElement(itemInInventory);
                        GCanvas.startOKDlg("Đã chọn vật phẩm cho thú cưng ăn.", false);
                    }
                });
            }
        });
    }

    public void setShowTextNew(String str, int i, int i2) {
        this.isShowText = true;
        this.wShow = (GCanvas.w - 20) - (this.x0 + 138);
        this.xShow = 140;
        this.yShow = -10;
        String[] strArrSplitFontBStrInLine = Font.arialFont11.splitFontBStrInLine(str, this.wShow - 10);
        this.showText = strArrSplitFontBStrInLine;
        int length = (strArrSplitFontBStrInLine.length * 15) + 8 + (this.arrayKhamNgoc != null ? 16 : 0);
        this.hShow = length;
        if (this.countPop == 0) {
            this.countPop = -(strArrSplitFontBStrInLine.length - 1);
        }
        cmyLimText = 0;
        cmtoYText = 0;
        cmyText = 0;
        if (length > 130) {
            int i3 = length - 130;
            cmyLimText = i3;
            int i4 = this.x0;
            if (i3 < i4) {
                cmyLimText = i4;
            }
        }
    }
}
