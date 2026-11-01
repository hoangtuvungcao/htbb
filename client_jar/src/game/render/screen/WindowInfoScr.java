package game.render.screen;

import game.model.Actor;
import game.model.AnimalInfo;
import game.model.Char;
import game.model.ChatInfo;
import game.model.Command;
import game.model.Cout;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.IAction;
import game.model.InfoAttributeItem;
import game.model.InputDlg;
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
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class WindowInfoScr extends Screen {
    public static WindowInfoScr me;
    public int x;
    public int y;
    public int w;
    public int h;
    public int selected;
    public int numH;
    public int xShow;
    public int yShow;
    public int wShow;
    public int hShow;
    public int wSmall;
    public int hSmall;
    public int x0;
    public int y0;
    public int idFrame;
    public int countFrame;
    private String[] showText;
    public Vector newSkill;
    private boolean trans;
    private int pb;
    private int pa;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public static int cmtoYText;
    public static int cmyText;
    public static int cmdyText;
    public static int cmvyText;
    public static int cmyLimText;
    public static int cmtoX;
    public static int cmx;
    public static int cmdx;
    public static int cmvx;
    public static int cmxLim;
    public static int focusTab;
    public static int cmtoXName;
    public static int cmxname;
    public static byte[] index;
    private Vector questInfo;
    public int idClass;
    public int countPop;
    public short idCharSell;
    public Vector listEp;
    public Vector listCheDo;
    private short[][] posEpNgoc;
    public static byte countR;
    public static byte countL;
    public static byte idColor;
    public static byte lvThu;
    public static byte slNLThu;
    public static byte magicAnimal;
    public static short numberQuestAll;
    public static short idTemp;
    public static Char charWearing;
    private byte[] arrayKhamNgoc;
    int limMyBag;
    int excess;
    int maxSize;
    public static byte khoaCheDo;
    public static byte khoaDoThu;
    public static byte khoaImbue;
    public static byte khoaKham;
    byte[] arrSell;
    int saveFocusTab;
    Image imgCharWear;
    public int idSeller;
    public int shopIdSell;
    public int prizeImbue;
    private String[] charInfo;
    boolean isCmyTab3;
    boolean isshowInfo;
    boolean isTab3;
    int transX;
    int dirX;
    public Image imgWeaponAvatar;
    public static final short MAX_POTION = 999;
    public static String[] infoMainCharServer = null;
    public static int dir = 1;
    public static byte idMonsterClanQuest = -1;
    public static short numberQuestGet = 0;
    public static boolean isKham = false;
    public static byte optionCreateItem = 0;
    public static int[] posPaintWearingX = {9, 9, 9, 107, 107, 107, 107, 107, 107, 107, 9, 107, 9, 82, 9, 9, 9, 9, 9, 34, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 107, 107, 107, 107, 107, 34};
    public static int[] posPaintWearingY = {32, 73, 52, 11, 11, 11, 11, 11, 52, 32, 94, 94, 11, 92, 32, 52, 73, 94, 11, 92, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 11, 32, 52, 73, 94, 92};
    private static byte[] idWearChar = {12, -1, 0, 9, 2, 8, 1, 8, 10, 11};
    private static byte[] idWearAnimal = {18, 30, 14, 31, 15, 32, 16, 33, 17, 34};
    private static byte[][] posWearChar = {new byte[]{10, 11}, new byte[]{56, 11}, new byte[]{10, 32}, new byte[]{56, 32}, new byte[]{10, 52}, new byte[]{56, 52}, new byte[]{10, 73}, new byte[]{56, 94}, new byte[]{10, 94}, new byte[]{56, 94}};
    public int numW = 7;
    public int[][] arrFrame = {new int[]{0, 1, 2}, new int[]{0, 1}};
    private String[] title = {"Hành trang", "Trang bị", "Tiềm năng", "Chiêu", "Thông tin", "Nhóm", "Nhiệm vụ", "Học chiêu", "Trao đổi", "Gian hàng", "Gian hàng", "Áo", "Quần", "Nón", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Phiến đá hệ", "Gian hàng", "Gian hàng", "Luyện đồ", "Kho đồ", "Gian hàng", "Gian hàng", "Khảm", "Hợp thành", "Nhiệm vụ", "Chế đồ", "Chiêu đội", "Chiêu cá nhân", "Trang bị thú", "Hợp đồ thú"};
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
    private ItemInInventory[] itemEpdothu = new ItemInInventory[5];
    public boolean isLoad = false;
    int countTabMyitem = 0;
    int xCus = -20;
    private int idImbue = -1;
    boolean first = false;
    int cmyKeep = 0;
    String[] mau = {"Trắng", "Hoàn mỹ", "Đỏ", "Xanh"};
    public Vector potionShop = new Vector();

    public static WindowInfoScr gI() {
        if (me != null) {
            return me;
        }
        WindowInfoScr windowInfoScr = new WindowInfoScr();
        me = windowInfoScr;
        return windowInfoScr;
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
        init();
        this.isChangeTab = true;
    }

    @Override
    public void init() {
        canKhung();
        setLim();
    }

    public WindowInfoScr() {
        this.dirX = 1;
        init();
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.this.isShowText) {
                    WindowInfoScr.this.closeText();
                    return;
                }
                WindowInfoScr.this.listTemp.removeAllElements();
                WindowInfoScr.this.listEp = null;
                WindowInfoScr.focusTab = WindowInfoScr.this.saveFocusTab;
                WindowInfoScr.this.doBuyItem();
                Canvas.gameScr.switchToMe();
                WindowInfoScr.this.list.removeAllElements();
                WindowInfoScr.this.charInfo = null;
                WindowInfoScr.this.questInfo = null;
                WindowInfoScr.this.imgCharWear = null;
                WindowInfoScr.charWearing = null;
                WindowInfoScr.this.transX = 0;
                WindowInfoScr.this.dirX = 1;
                WindowInfoScr.this.isSell = false;
            }
        });
        this.selected = 0;
        focusTab = 0;
        this.transX = 0;
        this.dirX = 1;
    }

    public void setInfo(Vector list) {
        this.list = list;
        this.numW = 7;
        this.numH = list.size() / this.numW;
        if (list.size() % this.numW != 0) {
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
        cmxLim = (this.numW * this.wSmall) - this.w;
        cmyLim = (this.numH * this.hSmall) - this.h;
        if (cmxLim < 0) {
            cmxLim = 0;
        }
        if (cmyLim < 0) {
            cmyLim = 0;
        }
    }

    public void setCurTab() {
        if (Canvas.currentScreen == gI() && focusTab == 0) {
            setSelectTab();
            this.isChangeTab = false;
        }
    }

    public void setInfo(int focus, boolean isSell, byte[] in) {
        this.isSell = isSell;
        if (isSell) {
            this.arrSell = in;
        }
        index = in;
        focusTab = focus;
        this.selected = 0;
        setCmdCenter();
        setSelectTab();
    }

    /** pokemon: bề ngang / cao khung bảng (thay khung cố định 145 px) - lưới đồ dùng ô to hơn ở màn rộng. */
    int rk = 145;
    int hk = 150;

    /** Cạnh ô lưới đồ theo màn: 18 (176), 22 (240), 26 (320+) - giữ vừa chiều cao màn. */
    static int canhO() {
        int k = Canvas.w >= 300 ? 26 : (Canvas.w >= 230 ? 22 : 18);
        while (k > 18 && 5 * k + 60 + 40 > Canvas.h - Screen.deltaY) {
            k -= 2;
        }
        return k;
    }

    /** Dời tâm khung theo bề ngang mới (khung cũ: x = giữa - 64, rộng 145 bắt đầu ở x - 8). */
    void canKhung() {
        this.x = Canvas.hw - this.rk / 2 + 8;
        this.y = Canvas.hh - this.hk / 2 - 2;
    }

    // ------------------------------------------------------------ pokemon: cửa hàng kiểu Poké Mart
    boolean laMart;
    String[] martTen;
    String[] martGia;
    int[] martCo;

    /** Danh sách hàng: biểu tượng + tên + giá canh phải + số đang có (thay lưới ô ở shop thuốc / đặc biệt). */
    void datMart(String[] ten, String[] gia, int[] co) {
        this.martTen = ten;
        this.martGia = gia;
        this.martCo = co;
        int rong = Math.min(Canvas.w - 30, 250);
        if (rong < 126) {
            rong = 126;
        }
        int hang = Canvas.w >= 230 ? 22 : 20;
        this.numW = 1;
        this.wSmall = rong;
        this.hSmall = hang;
        this.numH = Math.max(5, ten.length);
        this.w = rong;
        this.h = 5 * hang;
        this.rk = Math.max(145, rong + 19);
        this.hk = 150 + Math.max(0, 5 * hang - 90);
        canKhung();
        setLim();
        this.laMart = true;
    }

    private void paintMart(Graphics g) {
        int n = this.list == null ? 0 : this.list.size();
        int s = this.hSmall;
        int wr = this.wSmall;
        game.pokemon.GiaoDien.khoi(g, -2, -2, wr + 3, 5 * s + 3, 3, game.pokemon.GiaoDien.XANH);
        g.setClip(-1, -1, wr + 1, 5 * s + 1);
        g.translate(0, -cmy);
        int tu = Math.max(0, cmy / s);
        int den = Math.min(n, tu + 6);
        for (int i = tu; i < den; i++) {
            int y = i * s;
            boolean chon = i == this.selected && !this.isChangeTab;
            if (chon) {
                game.pokemon.GiaoDien.chon(g, 0, y, wr - 1, s - 1);
                game.pokemon.GiaoDien.conTro(g, 2, y + s / 2 - 4, 8);
            } else {
                game.pokemon.GiaoDien.khoi(g, 0, y, wr - 1, s - 1, 2, (i & 1) == 0 ? game.pokemon.GiaoDien.HANG_CHAN : game.pokemon.GiaoDien.HANG_LE);
            }
            ((Command) this.list.elementAt(i)).paint(g, 18, y + s / 2);
            String gia = this.martGia != null && i < this.martGia.length ? this.martGia[i] : "";
            int wGia = Font.arialFontW.getWidth(gia);
            Font.arialFontW.drawString(g, gia, wr - 5, y + (s - 13) / 2, Font.RIGHT);
            String ten = this.martTen != null && i < this.martTen.length ? this.martTen[i] : "";
            int co = this.martCo != null && i < this.martCo.length ? this.martCo[i] : -1;
            int wTen = wr - 30 - wGia - 8;
            if (co >= 0 && s >= 20 && wr >= 180) {
                // có: số đang mang, chữ nhỏ dưới tên
                Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, ten, wTen), 30, y + 1, 0);
                Font.smallFont.drawString(g, "co " + co, 30, y + s - 7, 0);
            } else {
                Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, ten, wTen), 30, y + (s - 13) / 2, 0);
            }
        }
        if (n == 0) {
            Font.arialFontW.drawString(g, "Cửa hàng chưa có hàng", wr / 2, cmy + 2 * s, Font.CENTER);
        }
        g.translate(0, cmy);
        game.pokemon.GiaoDien.moClip(g);
        game.pokemon.KieuO.thanhCuon(g, wr + 2, 0, 5 * s, Math.max(5, n) * s, cmy, 5 * s);
        nhoThanhLuoi(g, wr + 2, 5 * s, Math.max(5, n) * s);
    }

    public void setSize(int wBig, int hBig, int numW, int numH, int wSmall, int hSmall) {
        this.laMart = false;
        // pokemon: lưới đồ (hành trang / cửa hàng / rương: ô 18 x 18, >= 6 cột) dùng ô to hơn ở màn rộng
        if (wSmall == 18 && hSmall == 18 && numW >= 6) {
            int k = canhO();
            wSmall = k;
            hSmall = k;
            wBig = numW * k;
            hBig = 5 * k;
        }
        this.rk = Math.max(145, wBig + 19);
        this.hk = 150 + Math.max(0, (hSmall == wSmall && numW >= 6 ? 5 * hSmall : 90) - 90);
        canKhung();
        this.w = wBig;
        this.h = hBig;
        this.numW = numW;
        this.numH = numH;
        this.wSmall = wSmall;
        this.hSmall = hSmall;
        setLim();
        cmtoY = 0;
        cmtoX = 0;
        cmy = 0;
        cmx = 0;
    }

    @Override
    public void updateKey() {
        if (laCuaSoCham() && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            game.pokemon.ManCu.phimKhongCham(new Runnable() {
                public void run() {
                    WindowInfoScr.this.updateKey();
                }
            });
            return;
        }
        if (this.isShowText) {
            if (Canvas.keyHold[2]) {
                if (this.hShow >= 100) {
                    cmtoYText -= 10;
                    if (cmtoYText < 0) {
                        cmtoYText = 0;
                    }
                } else {
                    closeText();
                }
                Canvas.keyPressed[2] = false;
            } else if (Canvas.keyHold[8]) {
                if (this.hShow >= 100) {
                    cmtoYText += 10;
                    if (cmtoYText > cmyLimText) {
                        cmtoYText = cmyLimText;
                    }
                } else {
                    closeText();
                }
                Canvas.keyPressed[8] = false;
            }
            Canvas.isKeyPressed(2);
            Canvas.isKeyPressed(8);
            if (Canvas.isPointerDown && Canvas.isPointer(this.xShow, this.yShow, this.wShow - 1, (this.hShow - 1) + (this.countPop * 15))) {
                if (!this.trans) {
                    this.pa = cmyText;
                    this.trans = true;
                    game.pokemon.HopChiTiet.batDauKeo(cmyText);   // 0.9.11: nhấn lên thanh cuộn = kéo núm
                }
                cmtoYText = game.pokemon.HopChiTiet.keoNum ? game.pokemon.HopChiTiet.cuonKeoNum() : this.pa + (Canvas.pyLast - Canvas.py);
                if (cmtoYText < 0) {
                    cmtoYText = 0;
                }
                if (cmtoYText > cmyLimText) {
                    cmtoYText = cmyLimText;
                }
            }
            if (Canvas.isPointerClick) {
                this.trans = false;
                Canvas.isPointerClick = false;
                if (!Canvas.isPointer(this.xShow, this.yShow, this.wShow - 1, (this.hShow - 1) + (this.countPop * 15)) && Math.abs(Canvas.pyLast - Canvas.py) < 10 && Math.abs(Canvas.pxLast - Canvas.px) < 10) {
                    closeText();
                }
            }
        }
        if (Canvas.keyPressed[4] || Canvas.keyPressed[6]) {
            closeText();
        }
        if (Canvas.isPointerClick && Math.abs(Canvas.py - Canvas.pyLast) <= 10 && Math.abs(Canvas.px - Canvas.pxLast) <= 10) {
            if (Canvas.isPointer(this.x - 5, this.y, 30, 35)) {
                this.isChangeTab = true;
                Canvas.keyPressed[4] = true;
            }
            if (Canvas.isPointer(this.x + this.rk - 41, this.y, 30, 35)) {
                this.isChangeTab = true;
                Canvas.keyPressed[6] = true;
            }
        }
        if (this.isChangeTab) {
            if (Canvas.isKeyPressed(8)) {
                this.isChangeTab = false;
                setCmdCenter();
                Canvas.keyHold[8] = false;
                if (index[focusTab] == 1) {
                    this.selected = 0;
                }
            } else if (Canvas.isKeyPressed(4)) {
                focusTab--;
                if (focusTab < 0) {
                    focusTab = index.length - 1;
                }
                if (index.length > 1) {
                    countL = (byte) 5;
                    cmxname = 100;
                    dir = 1;
                }
                setSelectTab();
                setCmdCenter();
            } else if (Canvas.isKeyPressed(6)) {
                focusTab++;
                if (focusTab >= index.length) {
                    focusTab = 0;
                }
                if (index.length > 1) {
                    countR = (byte) 5;
                    dir = -1;
                    cmxname = -100;
                }
                setSelectTab();
                setCmdCenter();
            }
        } else if (index[focusTab] != 26 && cmy == 0 && this.numW != 0 && this.selected / this.numW == 0 && Canvas.isKeyPressed(2) && index[focusTab] != 32) {
            this.isChangeTab = true;
            this.center = null;
        }
        updateKeyMain();
        super.updateKey();
    }

    public void getItemAnimalFromList() {
        this.listItemAnimal = new Vector();
        int size0 = Char.inventory.size();
        for (int i = 0; i < size0; i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
            if ((it.type == 14 || it.type == 15 || it.type == 16 || it.type == 17 || it.type == 18) && item.colorName == idColor) {
                this.listItemAnimal.addElement(item);
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
        if (index.length > 1) {
            this.firName = this.name;
            this.name = this.title[index[focusTab]];
        } else if (this.name == null || this.name.length() == 0) {
            // pokemon: cửa sổ 1 tab chưa có tên -> tên tab mặc định
            this.name = this.title[index[focusTab]];
        }
        this.countTabMyitem = 0;
        if (index[focusTab] != 0) {
            this.saveFocusTab = focusTab;
        }
        Cout.println(new StringBuffer("Tab duoc chon la tab : ").append((int) index[focusTab]).toString());
        switch (index[focusTab]) {
            case 0:
                this.isPaintMoney = true;
                setInfo(getInventori());
                setSize(126, 90, 7, this.numH, 18, 18);
                if (Canvas.gameScr.mainChar.limItemBag <= 1) {
                    this.left = new Command("Chọn", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doSelectedInventori();
                        }
                    });
                } else {
                    this.left = new Command("Menu", new AnonymousClass3());
                }
                break;
            case 1:
                this.x0 += 5;
                this.y0 -= 2;
                setSize(117, 104, 3, 5, 97, 21);
                final Vector menu = new Vector();
                if (Canvas.gameScr.mainChar.idFashion != -1) {
                    menu.addElement(new Command("Thay đồ", new IAction() {
                        @Override
                        public void perform() {
                            Canvas.startYesNoDlg("Bạn muốn bỏ đồ thời trang vào rương không ?", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().unRideHorse((byte) 1);
                                    Canvas.endDlg();
                                    Canvas.gameScr.switchToMe();
                                }
                            });
                        }
                    }));
                }
                if (Canvas.gameScr.mainChar.isDoing) {
                    menu.addElement(new Command("Cất cuốc", new IAction() {
                        @Override
                        public void perform() {
                            Canvas.startYesNoDlg("Bạn muốn bỏ đồ thời trang vào rương không ?", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().unRideHorse((byte) 2);
                                    Canvas.endDlg();
                                    Canvas.gameScr.switchToMe();
                                }
                            });
                        }
                    }));
                }
                if (menu.size() == 1) {
                    this.left = (Command) menu.elementAt(0);
                } else if (menu.size() == 2) {
                    this.left = new Command("Chọn", new IAction() {
                        @Override
                        public void perform() {
                            Canvas.menu.startAt(menu, 0);
                        }
                    });
                }
                break;
            case 2:
                this.y0 += 13;
                setSize(124, 94, 1, 5, 125, 19);
                break;
            case 3:
                this.left = null;
                this.x0 += 5;
                this.y0 += 7;
                this.selected = 0;
                setSize(114, 30, Canvas.gameScr.mainChar.getnSkill(), 1, 23, 20);
                break;
            case 4:
                this.center = null;
                this.isPaintMoney = true;
                this.x0 -= 4;
                this.y0 -= 2;
                setSize(134, 95, 1, 10, 50, 12);
                break;
            case 5:
                this.isPaintMoney = true;
                this.x0 -= 4;
                this.y0 -= 2;
                setSize(134, 95, 1, Char.party.size(), 130, 32);
                break;
            case 6:
                this.isPaintMoney = true;
                this.x0 -= 4;
                this.y0 -= 2;
                setQuestInfo();
                setSize(134, 95, 1, this.questInfo.size(), 130, 15);
                this.questInfo.removeAllElements();
                if (Canvas.gameScr.mainChar.receiveQuest) {
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
                this.x0 += 5;
                this.y0 += 7;
                this.left = null;
                setCmdCenter();
                break;
            case 8:
                this.x0 += 9;
                this.y0 -= 5;
                this.name = "Trao đổi";
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
                setSize(126, 90, 1, 1, 18, 18);
                setListSellItem(Canvas.gameScr.shop, -1);
                setInfo(this.list);
                doSetLeftSellWeapon(-1);
                break;
            case 10:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                setListSellGemItem(Canvas.gameScr.shop);
                setInfo(this.list);
                dosetLeftSellGemItem();
                break;
            case 11:
            case 12:
            case 13:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                setListSellItem(Canvas.gameScr.shop, index[focusTab] - 11);
                setInfo(this.list);
                doSetLeftSellWeapon(index[focusTab] - 11);
                break;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                setListSellItem(Canvas.gameScr.shop, index[focusTab] - 6);
                setInfo(this.list);
                doSetLeftSellWeapon(index[focusTab] - 6);
                break;
            case 19:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                setListPotion();
                setInfo(this.list);
                doSetLeftPotion();
                {
                    int n = Canvas.gameScr.shop.size();
                    String[] ten = new String[n];
                    String[] gia = new String[n];
                    int[] co = new int[n];
                    for (int i = 0; i < n; i++) {
                        ItemInInventory it = (ItemInInventory) Canvas.gameScr.shop.elementAt(i);
                        try {
                            ten[i] = MainChar.listPotion[it.potionType].name;
                            gia[i] = Res.potionTemplates[it.potionType].price + " xu";
                            co[i] = Canvas.gameScr.mainChar.potions[it.potionType];
                        } catch (Throwable e) {
                            ten[i] = "";
                            gia[i] = "";
                            co[i] = -1;
                        }
                    }
                    datMart(ten, gia, co);
                }
                break;
            case 20:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                getShopTemplate();
                setInfo(this.list);
                doBuyShopSpecial();
                {
                    Vector ds = new Vector();
                    for (int i = 0; i < Res.shopTemplate.size(); i++) {
                        GemTemp gt = (GemTemp) Res.shopTemplate.elementAt(i);
                        if (gt.shopType == focusTab && gt.isSell) {
                            ds.addElement(gt);
                        }
                    }
                    String[] ten = new String[ds.size()];
                    String[] gia = new String[ds.size()];
                    int[] co = new int[ds.size()];
                    for (int i = 0; i < ds.size(); i++) {
                        GemTemp gt = Res.getShopByID(((GemTemp) ds.elementAt(i)).id);
                        if (gt == null) {
                            gt = (GemTemp) ds.elementAt(i);
                        }
                        ten[i] = gt.name;
                        gia[i] = game.pokemon.TienTe.chu(game.pokemon.TienShop.loai(gt), gt.price);
                        co[i] = -1;
                    }
                    datMart(ten, gia, co);
                }
                this.name = new StringBuffer(String.valueOf(this.name)).append(" ").append(focusTab + 1).toString();
                break;
            case 21:
                this.x0++;
                this.y0 -= 6;
                setInfo(this.list);
                setSize(126, 110, 3, 5, 20, 23);
                setCmdCenter();
                doLeftImbue();
                break;
            case 22:
            case 23:
                // pokemon: cửa sổ 1 tab - đặt tên tab (Rương đồ / Gian hàng) thay vì giữ tên cũ
                if (index.length <= 1) {
                    this.name = index[focusTab] == 22 ? "Kho đồ" : this.title[23];
                }
                this.isPaintMoney = true;
                this.x0 += 9;
                this.y0 += 0;
                this.selected = 0;
                setSizeKeepItem(0);
                setSize(108, 90, 6, this.numH, 18, 18);
                setCmdCenter();
                setLeftKeepAndSellItem();
                break;
            case 24:
                this.isPaintMoney = true;
                setSize(126, 90, 7, this.numH, 18, 18);
                setInfo(getListBuyItem());
                setLeftBuyItem();
                break;
            case 25:
                this.isChangeTab = false;
                this.x0++;
                this.y0 -= 4;
                setInfo(this.list);
                setSize(126, 110, 3, 5, 20, 23);
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
                this.x0++;
                this.y0 -= 4;
                setSize(126, 108, 5, 1, 20, 18);
                this.posEpNgoc = new short[][]{new short[]{(short) ((this.w / 2) - 44), 20}, new short[]{(short) ((this.w / 2) + 26), 20}, new short[]{(short) ((this.w / 2) - 10), 43}, new short[]{(short) ((this.w / 2) - 44), 65}, new short[]{(short) ((this.w / 2) + 26), 65}};
                setListImbue(true, 1, 0);
                this.listEp = new Vector();
                this.center = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedEpNgoc();
                    }
                });
                doLeftEpNgoc();
                break;
            case 27:
                this.isPaintMoney = true;
                this.x0 -= 4;
                this.y0 -= 2;
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
                this.x0++;
                this.y0 -= 4;
                int aa = this.listEp.size() / 2;
                if (aa < 6) {
                    aa = 6;
                }
                setSize(126, 108, aa, 3, 20, 18);
                this.center = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedCheDo();
                    game.pokemon.ManCu.tiepQuanMenu("Chế đồ");
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
                setInfo(setListGuildSkill());
                int sizeaa = 0;
                for (int a = 0; a < Canvas.gameScr.skillClan.length; a++) {
                    if (Canvas.gameScr.skillClan[a].time > 0) {
                        sizeaa++;
                    }
                }
                for (int a2 = 0; a2 < Canvas.gameScr.skillClanPrivate.length; a2++) {
                    if (Canvas.gameScr.skillClanPrivate[a2].time > 0) {
                        sizeaa++;
                    }
                }
                if (sizeaa < 5) {
                    sizeaa = 5;
                }
                setSize(126, 100, 1, sizeaa, 126, 20);
                this.isChangeTab = true;
                break;
            case 30:
                setInfo(setListGuildSkill());
                setSize(126, 100, 1, 6, 126, 20);
                this.isChangeTab = true;
                break;
            case 31:
                this.x0 += 5;
                this.y0 -= 2;
                setSize(117, 104, 2, 5, 97, 21);
                break;
            case 32:
                this.x0++;
                this.y0 -= 4;
                setSize(126, 108, 5, 1, 20, 18);
                this.posEpNgoc = new short[][]{new short[]{(short) ((this.w / 2) - 44), 0}, new short[]{(short) ((this.w / 2) + 26), 0}, new short[]{(short) ((this.w / 2) - 10), 20}, new short[]{(short) ((this.w / 2) - 44), 42}, new short[]{(short) ((this.w / 2) + 26), 42}};
                for (int i = 0; i < this.itemEpdothu.length; i++) {
                    this.itemEpdothu[i] = null;
                }
                getItemAnimalFromList();
                this.listEp = new Vector();
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedEpDothu();
                    game.pokemon.ManCu.tiepQuanMenu("Ép đồ thú");
                    }
                });
                doLeftEpDothu();
                break;
            case 127:
                this.isPaintMoney = true;
                setSize(126, 90, 1, 1, 18, 18);
                setListSellItem(Canvas.gameScr.shop, -1);
                setInfo(this.list);
                doSetLeftPetEat(-1);
                break;
        }
        if (this.selected >= (this.numH * this.numW) - 1) {
            this.selected = 0;
        }
    }

    class AnonymousClass3 implements IAction {
        AnonymousClass3() {
        }

        @Override
        public void perform() {
            Vector menu = new Vector();
            menu.addElement(new Command("Chọn", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.doSelectedInventori();
                }
            }));
            menu.addElement(new Command("Xem thêm", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.countTabMyitem++;
                    if (WindowInfoScr.this.countTabMyitem > WindowInfoScr.this.limMyBag) {
                        WindowInfoScr.this.countTabMyitem = 0;
                    }
                    WindowInfoScr.this.selected = 0;
                    WindowInfoScr.this.closeText();
                    WindowInfoScr.this.setSize(126, 90, 7, WindowInfoScr.this.numH, 18, 18);
                    WindowInfoScr.this.setCmdCenter();
                }
            }));
            Canvas.menu.startAt(menu, 0);
        }
    }

    private Vector setListGuildSkill() {
        Vector menu = new Vector();
        int aa = 0;
        int size0 = Canvas.gameScr.skillClan.length;
        for (int i = 0; i < size0; i++) {
            final int ii = i;
            if (Canvas.gameScr.skillClan[i].time > 0) {
                final int ind = aa;
                menu.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        g.setClip(x - (WindowInfoScr.this.wSmall / 2), y - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall, WindowInfoScr.this.hSmall);
                        GameData.paintIcon(g, (short) (ii + 1600), (x - (WindowInfoScr.this.wSmall / 2)) + 9, y);
                        int aa2 = 0;
                        if (ind == WindowInfoScr.this.selected && !WindowInfoScr.this.isChangeTab) {
                            int ww = Font.arialFont.getWidth(Canvas.gameScr.skillClan[ii].info);
                            if (ww > WindowInfoScr.this.w - ((WindowInfoScr.this.wSmall / 2) + 16)) {
                                WindowInfoScr.this.xCus += 2;
                                if (WindowInfoScr.this.xCus > ww - ((WindowInfoScr.this.wSmall / 2) + 30)) {
                                    WindowInfoScr.this.xCus = -20;
                                }
                            }
                            aa2 = WindowInfoScr.this.xCus;
                            if (WindowInfoScr.this.xCus < 0) {
                                aa2 = 0;
                            }
                        }
                        g.setClip((x - (WindowInfoScr.this.wSmall / 2)) + 18, y - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall - 20, WindowInfoScr.this.hSmall);
                        Font.arialFont.drawString(g, Canvas.gameScr.skillClan[ii].info, ((x - (WindowInfoScr.this.wSmall / 2)) + 18) - aa2, y - (Font.arialFont.getHeight() / 2), 0);
                    }
                });
                aa++;
            }
        }
        int size1 = Canvas.gameScr.skillClanPrivate.length;
        for (int i2 = 0; i2 < size1; i2++) {
            final int ii2 = i2;
            if (Canvas.gameScr.skillClanPrivate[i2].time > 0) {
                final int ind2 = aa;
                menu.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        g.setClip(x - (WindowInfoScr.this.wSmall / 2), y - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall, WindowInfoScr.this.hSmall);
                        GameData.paintIcon(g, (short) (ii2 + 1600), (x - (WindowInfoScr.this.wSmall / 2)) + 9, y);
                        int aa2 = 0;
                        if (ind2 == WindowInfoScr.this.selected && !WindowInfoScr.this.isChangeTab) {
                            int ww = Font.arialFont.getWidth(Canvas.gameScr.skillClan[ii2].info);
                            if (ww > WindowInfoScr.this.w - ((WindowInfoScr.this.wSmall / 2) + 16)) {
                                WindowInfoScr.this.xCus += 2;
                                if (WindowInfoScr.this.xCus > ww - ((WindowInfoScr.this.wSmall / 2) + 30)) {
                                    WindowInfoScr.this.xCus = -20;
                                }
                            }
                            aa2 = WindowInfoScr.this.xCus;
                            if (WindowInfoScr.this.xCus < 0) {
                                aa2 = 0;
                            }
                        }
                        g.setClip((x - (WindowInfoScr.this.wSmall / 2)) + 18, y - (WindowInfoScr.this.hSmall / 2), WindowInfoScr.this.wSmall - 20, WindowInfoScr.this.hSmall);
                        Font.arialFont.drawString(g, Canvas.gameScr.skillClanPrivate[ii2].info, (x - (WindowInfoScr.this.wSmall / 2)) + 18 + aa2, y - (Font.arialFont.getHeight() / 2), 0);
                    }
                });
                aa++;
            }
        }
        return menu;
    }

    protected void doLeftCheDo() {
        if (this.listEp.size() == 0) {
            return;
        }
        final byte[][] arr = new byte[this.listCheDo.size()][6];
        int size0 = this.listEp.size();
        for (int i = 0; i < size0; i++) {
            GemTemplate gem = (GemTemplate) this.listEp.elementAt(i);
            int size1 = this.listCheDo.size();
            for (int a = 0; a < size1; a++) {
                Mineral mi = (Mineral) this.listCheDo.elementAt(a);
                int count = gem.id - mi.idTemplate;
                if (count <= 5) {
                    arr[a][count] = (byte) gem.number;
                    break;
                }
            }
        }
        Canvas.startYesNoDlg("Bạn có muốn hoàn thành không ?", new IAction() {
            @Override
            public void perform() {
                int size2 = WindowInfoScr.this.listEp.size();
                for (int i2 = size2 - 1; i2 >= 0; i2--) {
                    GemTemplate g = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i2);
                    WindowInfoScr.this.removeItemCheDo(g);
                }
                GameService.gI().doCheDo((short) WindowInfoScr.this.listCheDo.size(), WindowInfoScr.idTemp, arr);
                WindowInfoScr.this.right.action.perform();
                Canvas.startWaitDlg();
            }
        });
    }

    public void removeItemCheDo(GemTemplate g) {
        g.number--;
        if (g.number <= 0) {
            this.listEp.removeElement(g);
        }
        boolean aa = false;
        int size0 = this.listTemp.size();
        for (int i = 0; i < size0; i++) {
            GemTemplate ge = (GemTemplate) this.listTemp.elementAt(i);
            if (ge.id == g.id) {
                ge.number++;
                aa = true;
                break;
            }
        }
        if (!aa) {
            GemTemplate ge2 = new GemTemplate(g.id);
            GemTemplate.copyData(g, ge2);
            ge2.number = 1;
            this.listTemp.addElement(g);
        }
    }

    protected void doSelectedCheDo() {
        final boolean is = this.selected / this.numW < 2;
        GemTemplate gem = null;
        if (is) {
            if (this.selected < this.listEp.size()) {
                gem = (GemTemplate) this.listEp.elementAt(this.selected);
            }
        } else if (this.selected % this.numW < this.listTemp.size()) {
            gem = (GemTemplate) this.listTemp.elementAt(this.selected % this.numW);
        }
        if (gem == null) {
            return;
        }
        final GemTemplate g = gem;
        final int xx = ((this.selected % this.numW) * this.wSmall) - cmx;
        final int yy = (this.selected / this.numW) * this.wSmall;
        final short idTem = gem.id;
        Vector menu = new Vector();
        menu.addElement(new Command(!is ? "Bỏ vào" : "Lấy ra", new IAction() {
            @Override
            public void perform() {
                if (is) {
                    WindowInfoScr.this.removeItemCheDo(g);
                } else {
                    int number = 0;
                    int size0 = WindowInfoScr.this.listCheDo.size();
                    for (int a = 0; a < size0; a++) {
                        Mineral mineral = (Mineral) WindowInfoScr.this.listCheDo.elementAt(a);
                        if (g.id - mineral.idTemplate >= 0 && g.id - mineral.idTemplate <= 5) {
                            int size1 = WindowInfoScr.this.listEp.size();
                            for (int i = 0; i < size1; i++) {
                                GemTemplate gem2 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i);
                                if (gem2.id - mineral.idTemplate >= 0 && gem2.id - mineral.idTemplate <= 5) {
                                    number += gem2.number;
                                }
                            }
                            if (number >= mineral.number) {
                                Canvas.startOKDlg("Đã đủ số lượng.");
                                return;
                            }
                        }
                    }
                    boolean aa = false;
                    int size2 = WindowInfoScr.this.listEp.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        GemTemplate gem3 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i2);
                        if (gem3.id == g.id) {
                            gem3.number++;
                            aa = true;
                            break;
                        }
                    }
                    if (!aa) {
                        GemTemplate ge = new GemTemplate(g.id);
                        GemTemplate.copyData(g, ge);
                        ge.number = 1;
                        WindowInfoScr.this.listEp.addElement(ge);
                    }
                    g.number--;
                    if (g.number <= 0) {
                        WindowInfoScr.this.listTemp.removeElement(g);
                    }
                }
                WindowInfoScr.this.setLimitCheDo();
            }
        }));
        menu.addElement(new Command("Thông tin", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.khoaCheDo == 0 || WindowInfoScr.khoaCheDo == 2) {
                    WindowInfoScr.this.showGemItemInfo(idTem, xx, yy);
                } else {
                    WindowInfoScr.this.showGemItemKhoaInfo(idTem, xx, yy);
                }
            }
        }));
        Canvas.menu.startAt(menu, 2);
    }

    public void setImageCharWear() {
        int count = 0;
        int size0 = charWearing.wearingItems.size();
        for (int i = 0; i < size0; i++) {
            ItemInInventory cwi = (ItemInInventory) charWearing.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if (item.plus > 13) {
                count++;
            }
        }
        if (count < 3 || charWearing.idFashion != -1) {
            return;
        }
        this.imgCharWear = Image.createImage(60, 100);
        Graphics g = this.imgCharWear.getGraphics();
        g.setColor(-65315);
        g.fillRect(0, 0, 60, 100);
        if (charWearing.legStyle != -1) {
            Res.getCharPartInfo(0, charWearing.legStyle).paintAvatar(g, (short) 30, (short) 90, 0);
        }
        if (charWearing.bodyStyle != -1) {
            Res.getCharPartInfo(1, charWearing.bodyStyle).paintAvatar(g, (short) 30, (short) 90, 0);
        }
        if (charWearing.hatStyle != -1) {
            Res.getCharPartInfo(3, charWearing.hatStyle).paintAvatar(g, (short) 30, (short) 90, 0);
        }
        if (charWearing.weaponType != -1 && charWearing.weaponStyle != -1 && charWearing.imgWpa != null) {
            g.drawImage(charWearing.imgWpa, 30 + charWearing.dxWear, 90 + charWearing.dyWear, 0);
        }
        this.imgCharWear = Res.transparentBG(this.imgCharWear);
        int[] rgb = new int[6000];
        this.imgCharWear.getRGB(rgb, 0, 60, 0, 0, 60, 100);
        for (int i2 = 0; i2 < rgb.length; i2++) {
            if (rgb[i2] != 16777215) {
                if (i2 - 1 > 0 && rgb[i2 - 1] == 16777215) {
                    rgb[i2] = -1;
                } else if (i2 + 1 < rgb.length && rgb[i2 + 1] == 16777215) {
                    rgb[i2] = -1;
                }
            }
        }
        this.imgCharWear = Image.createRGBImage(rgb, 60, 100, true);
    }

    public void doSelectedEpNgoc() {
        Vector menu = new Vector();
        if (this.numH != 1) {
            if (this.selected % this.numW < this.listTemp.size()) {
                final GemTemplate item = (GemTemplate) this.listTemp.elementAt(this.selected % this.numW);
                menu.addElement(new Command("Bỏ vào", new IAction() {
                    @Override
                    public void perform() {
                        if (WindowInfoScr.this.listEp.size() < 5) {
                            item.number--;
                            if (item.number <= 0) {
                                WindowInfoScr.this.listTemp.removeElement(item);
                            }
                            RealID real = new RealID(item.rID);
                            real.ID = item.id;
                            WindowInfoScr.this.listEp.addElement(real);
                        }
                    }
                }));
                menu.addElement(addGemInfo(item, ((this.selected % this.numW) * 20) + 4, 86));
            }
        } else if (this.selected < this.listEp.size()) {
            final RealID real = (RealID) this.listEp.elementAt(this.selected);
            menu.addElement(new Command("Lấy ra", new IAction() {
                @Override
                public void perform() {
                    GemTemplate gem = GemTemplate.getGemByID(real.ID);
                    gem.number++;
                    WindowInfoScr.this.listEp.removeElement(real);
                }
            }));
            GemTemplate gem = new GemTemplate();
            gem.id = real.ID;
            menu.addElement(addGemInfo(gem, this.posEpNgoc[this.selected][0], this.posEpNgoc[this.selected][1]));
        }
        Canvas.menu.startAt(menu, 2);
    }

    private Command addGemInfo(final GemTemplate item, final int xx, final int yy) {
        return new Command("Thông tin", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.khoaDoThu == 0 || WindowInfoScr.khoaDoThu == 2) {
                    WindowInfoScr.this.showGemItemInfo(item.id, xx, yy);
                } else {
                    WindowInfoScr.this.showGemItemKhoaInfo(item.id, xx, yy);
                }
            }
        });
    }

    class AnonymousClass29 implements IAction {
        AnonymousClass29() {
        }

        @Override
        public void perform() {
            if (WindowInfoScr.this.listEp.size() < 3) {
                Canvas.startOKDlg("Bạn phải ép ít nhất 3 vật phẩm.");
            } else {
                Canvas.startYesNoDlg(new StringBuffer("Bạn phải mất ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, WindowInfoScr.this.prizeImbue)).append(" để hợp.").toString(), new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().epNgoc(1, WindowInfoScr.this.listEp);
                        Canvas.startWaitDlg("Đang hợp..", false);
                        WindowInfoScr.this.listEp.removeAllElements();
                    }
                });
            }
        }
    }

    private void doLeftEpNgoc() {
        this.left = new Command("Hợp", new AnonymousClass29());
    }

    class AnonymousClass31 implements IAction {
        AnonymousClass31() {
        }

        @Override
        public void perform() {
            if (WindowInfoScr.this.selected < WindowInfoScr.this.sellItem.size()) {
                Canvas.startYesNoDlg("Bạn có muốn mua không ?", new IAction() {
                    @Override
                    public void perform() {
                        if (WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected) instanceof ItemInInventory) {
                            ItemInInventory item = (ItemInInventory) WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected);
                            GameService.gI().requestBuyItemUser(WindowInfoScr.this.idCharSell, WindowInfoScr.this.idSeller, WindowInfoScr.this.shopIdSell, item.itemID, 0);
                        } else {
                            RealID real = (RealID) WindowInfoScr.this.sellItem.elementAt(WindowInfoScr.this.selected);
                            GameService.gI().requestBuyItemUser(WindowInfoScr.this.idCharSell, WindowInfoScr.this.idSeller, WindowInfoScr.this.shopIdSell, real.realID, 1);
                        }
                        Canvas.startWaitDlg();
                    }
                });
            }
        }
    }

    public void setLeftBuyItem() {
        this.left = new Command("Mua", new AnonymousClass31());
    }

    public Vector getListBuyItem() {
        Vector menu = new Vector();
        int size0 = this.sellItem.size();
        for (int i = 0; i < size0; i++) {
            final int ii = i;
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    if (!(WindowInfoScr.this.sellItem.elementAt(ii) instanceof ItemInInventory)) {
                        WindowInfoScr.this.showRealIDInfo((RealID) WindowInfoScr.this.sellItem.elementAt(ii), (ii % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, (ii / WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall);
                    } else {
                        WindowInfoScr.this.setShowText(((ItemInInventory) WindowInfoScr.this.sellItem.elementAt(ii)).getInfoItemUserSell(), (ii % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, (ii / WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall);
                    }
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    if (WindowInfoScr.this.sellItem.elementAt(ii) instanceof ItemInInventory) {
                        ItemInInventory item = (ItemInInventory) WindowInfoScr.this.sellItem.elementAt(ii);
                        if (item.isSelling) {
                            game.pokemon.GiaoDien.khoi(g, x - 8, y - 8, 16, 16, 2, 0x3E8A30);
                        }
                        item.paint(g, x, y);
                        return;
                    }
                    Res.paintGem(g, Res.getGemByID(((RealID) WindowInfoScr.this.sellItem.elementAt(ii)).ID).idImage, x, y);
                }
            });
        }
        return menu;
    }

    private void setLeftKeepAndSellItem() {
        this.left = new Command(index[focusTab] == 22 ? "Chuyển" : "Chọn", new IAction() {
            @Override
            public void perform() {
                int inde = ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 3) + (WindowInfoScr.this.selected % WindowInfoScr.this.numW);
                ItemInInventory item = null;
                if (WindowInfoScr.this.selected % WindowInfoScr.this.numW < 3) {
                    if (inde < Char.inventory.size()) {
                        ItemInInventory item2 = (ItemInInventory) Char.inventory.elementAt(inde);
                        if (WindowInfoScr.index[WindowInfoScr.focusTab] == 22) {
                            Canvas.gameScr.gameService.putItem2Bag(item2.itemID);
                            return;
                        } else {
                            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 23) {
                                if (item2.isSelling) {
                                    Canvas.startOKDlg("Vật phẩm đang được bán.");
                                    return;
                                } else {
                                    WindowInfoScr.this.setLeftSellItem(item2, (short) -1);
                                    return;
                                }
                            }
                            return;
                        }
                    }
                    if (inde < Char.inventory.size() + MainChar.gemItem.size() && WindowInfoScr.index[WindowInfoScr.focusTab] == 23) {
                        GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(inde - Char.inventory.size());
                        WindowInfoScr.this.setLeftSellItem(null, gem.rID);
                        return;
                    }
                    return;
                }
                WindowInfoScr.this.closeText();
                if (WindowInfoScr.index[WindowInfoScr.focusTab] == 22 && inde - 3 < Char.itembag.size()) {
                    ItemInInventory item3 = (ItemInInventory) Char.itembag.elementAt(inde - 3);
                    Canvas.gameScr.gameService.getItemFromBag(item3.itemID);
                } else if (WindowInfoScr.index[WindowInfoScr.focusTab] == 23 && inde - 3 < WindowInfoScr.this.sellItem.size()) {
                    RealID gem2 = null;
                    if (WindowInfoScr.this.sellItem.elementAt(inde - 3) instanceof ItemInInventory) {
                        item = (ItemInInventory) WindowInfoScr.this.sellItem.elementAt(inde - 3);
                    } else {
                        gem2 = (RealID) WindowInfoScr.this.sellItem.elementAt(inde - 3);
                    }
                    WindowInfoScr.this.doRequestSellItem(item, gem2 != null ? gem2.realID : (short) -1, 0, false);
                }
            }
        });
    }

    protected void setLeftSellItem(final ItemInInventory item, final short gem) {
        closeText();
        Canvas.inputDlg.setInfo("Nhập giá muốn bán: ", new IAction() {
            @Override
            public void perform() {
                int value;
                try {
                    String str = Canvas.inputDlg.tfInput.getText();
                    if (str.equals("") || (value = Integer.parseInt(str)) < 0) {
                        return;
                    }
                    WindowInfoScr.this.doRequestSellItem(item, gem, value, true);
                } catch (Exception e) {
                    Canvas.startOKDlg("Vui lòng chỉ nhập số.");
                }
            }
        }, 1, 10, true);
        Canvas.inputDlg.show();
    }

    public void doRequestSellItem(ItemInInventory item, short gem, int prize, boolean sell) {
        GameService.gI().requestSellItem(this.idSeller, this.shopIdSell, item, gem, prize, sell);
        Canvas.startWaitDlg();
    }

    private void setSelectedKeepAndSellItem() {
        this.center = new Command("", new IAction() {
            @Override
            public void perform() {
                Vector ite;
                int inde = ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 3) + (WindowInfoScr.this.selected % WindowInfoScr.this.numW);
                if (WindowInfoScr.this.selected % WindowInfoScr.this.numW < 3) {
                    if (inde < Char.inventory.size()) {
                        ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(inde);
                        if (item == null) {
                            return;
                        }
                        WindowInfoScr.this.showItemInventoryInfo(item, WindowInfoScr.this.isSell, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                        return;
                    }
                    if (inde < Char.inventory.size() + MainChar.gemItem.size()) {
                        GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(inde - Char.inventory.size());
                        WindowInfoScr.this.showGemItemInfo(gem.id, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                        return;
                    }
                    return;
                }
                if (WindowInfoScr.index[WindowInfoScr.focusTab] == 22) {
                    ite = Char.itembag;
                } else {
                    ite = WindowInfoScr.this.sellItem;
                }
                if (inde - 3 < ite.size()) {
                    if (ite.elementAt(inde - 3) instanceof ItemInInventory) {
                        WindowInfoScr.this.showItemInventoryInfo((ItemInInventory) ite.elementAt(inde - 3), WindowInfoScr.this.isSell, (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                    } else {
                        WindowInfoScr.this.showRealIDInfo((RealID) ite.elementAt(inde - 3), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * WindowInfoScr.this.wSmall, ((WindowInfoScr.this.selected / WindowInfoScr.this.numW) * WindowInfoScr.this.hSmall) - WindowInfoScr.cmy);
                    }
                }
            }
        });
    }

    public void setListImbue(boolean isEp, int type, int khoa) {
        this.listTemp.removeAllElements();
        Vector v = khoa == 0 ? MainChar.gemItem : MainChar.gemItemKhoa;
        int size0 = v.size();
        for (int i = 0; i < size0; i++) {
            GemTemplate gem = (GemTemplate) v.elementAt(i);
            GemTemp g = Res.getGemByID(gem.id);
            if (!isEp) {
                if (g.type == type) {
                    this.listTemp.addElement(gem);
                }
            } else if (g.typeEp == type) {
                this.listTemp.addElement(gem);
            }
        }
        if (this.listTemp.size() == 0) {
            Canvas.startOKDlg("Không có nguyên liệu.", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.right.action.perform();
                    Canvas.endDlg();
                }
            });
        }
    }

    class AnonymousClass39 implements IAction {
        AnonymousClass39() {
        }

        @Override
        public void perform() {
            if (Canvas.gameScr.mainChar.itemImbue != null) {
                if (Canvas.gameScr.mainChar.hadPutItemImbue()) {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn phải mất ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, WindowInfoScr.this.prizeImbue)).append(" để luyện.").toString(), new IAction() {
                        @Override
                        public void perform() {
                            if (Canvas.gameScr.mainChar.charXu < WindowInfoScr.this.prizeImbue) {
                                Canvas.startOKDlg("Không đủ tiền");
                                return;
                            }
                            Canvas.gameScr.mainChar.charXu -= (long) WindowInfoScr.this.prizeImbue;
                            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 21) {
                                GameService.gI().doImbueItem(1);
                            } else {
                                GameService.gI().doKhamItem(1);
                            }
                            Canvas.startWaitDlg();
                        }
                    }, new IAction() {
                        @Override
                        public void perform() {
                            Canvas.currentDialog = null;
                        }
                    });
                } else {
                    Canvas.startOKDlg(WindowInfoScr.index[WindowInfoScr.focusTab] != 21 ? "Chưa đặt ngọc" : "Chưa đặt luyện kim dược");
                }
            }
        }
    }

    private void doLeftImbue() {
        this.left = new Command(index[focusTab] == 21 ? "Đập" : "Xong", new AnonymousClass39());
    }

    public void setSelectedImbue() {
        Vector menuItem = new Vector();
        final int index2 = this.selected / this.numW;
        final int xx = this.selected % this.numW;
        int siz = (index2 * 2) + (xx / 2);
        if ((index2 == 4 && (this.idImbue == -1 || xx >= Char.inventory.size())) || (index2 != 4 && ((xx == 1 && this.idImbue != -1) || (xx != 1 && siz < MainChar.gemitemImbue.size())))) {
            menuItem.addElement(new Command(index2 != 4 ? "Lấy ra" : "Bỏ vào", new IAction() {
                @Override
                public void perform() {
                    if (index2 == 4) {
                        if (xx < Char.inventory.size()) {
                            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(xx);
                            if (item.dayUse > 0) {
                                Canvas.startOKDlg("Không được đập đồ thuê.");
                                return;
                            }
                            if (Canvas.gameScr.mainChar.itemImbue == null && !item.isSelling) {
                                Canvas.gameScr.mainChar.itemImbue = item;
                                ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                                if (it.type < 3 || it.type > 7) {
                                    WindowInfoScr.this.imgWeaponAvatar = GameData.getImgIcon(it.idIcon).img;
                                } else if (it.type >= 3 && it.type < 8) {
                                    GameService.gI().doRequestWeapone(2, it.type, it.style, (byte) it.index);
                                    Canvas.startWaitDlg();
                                }
                            }
                            WindowInfoScr.this.idImbue = xx;
                            GameService.gI().addItemImbue(item.itemID);
                        } else {
                            if (MainChar.gemitemImbue.size() >= 8) {
                                Canvas.startOKDlg("Không thể thêm");
                                return;
                            }
                            int id = xx - Char.inventory.size();
                            if (id < WindowInfoScr.this.listTemp.size()) {
                                GemTemplate gem = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(id);
                                RealID real = new RealID(gem.rID);
                                real.ID = gem.id;
                                boolean isLKD = false;
                                if (WindowInfoScr.index[WindowInfoScr.focusTab] == 21 && MainChar.gemitemImbue.size() == 0 && (real.ID <= 7 || real.ID == 155 || real.ID == 156)) {
                                    isLKD = true;
                                }
                                if (!isLKD) {
                                    if (Canvas.gameScr.mainChar.countBaoHiem() >= 1 && gem.id < 5) {
                                        Canvas.startOKDlg("Chỉ có thể sử dụng 1 loại bảo hiểm cho 1 lần luyện.");
                                        return;
                                    }
                                    if (Canvas.gameScr.mainChar.countLKD() < 1 || gem.id < 8 || gem.id == 155 || gem.id == 156) {
                                        MainChar.gemitemImbue.addElement(real);
                                        gem.number--;
                                        if (gem.number == 0) {
                                            WindowInfoScr.this.listTemp.removeElement(gem);
                                        }
                                        GameService.gI().addGemItemImbue(real.realID, (byte) 0);
                                    } else {
                                        Canvas.startOKDlg("Không thể sử dụng 2 loại ngọc cho 1 lần luyện.");
                                        return;
                                    }
                                } else {
                                    Canvas.startOKDlg("Phải đặt luyện kim dược trước.");
                                }
                            }
                        }
                    } else if (xx == 1) {
                        if (WindowInfoScr.this.idImbue != -1) {
                            GameService.gI().addItemImbue(((ItemInInventory) Char.inventory.elementAt(WindowInfoScr.this.idImbue)).itemID);
                            WindowInfoScr.this.idImbue = -1;
                            Canvas.gameScr.mainChar.itemImbue = null;
                            WindowInfoScr.this.imgWeaponAvatar = null;
                        }
                    } else {
                        int size = (index2 * 2) + (xx / 2);
                        if (size < MainChar.gemitemImbue.size()) {
                            RealID gem2 = (RealID) MainChar.gemitemImbue.elementAt(size);
                            MainChar.gemitemImbue.removeElement(gem2);
                            int aa = 0;
                            int size1 = WindowInfoScr.this.listTemp.size();
                            for (int i = 0; i < size1; i++) {
                                GemTemplate g = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(i);
                                if (g.id == gem2.ID) {
                                    g.number++;
                                    aa = 1;
                                    break;
                                }
                            }
                            if (aa == 0) {
                                GemTemplate g2 = new GemTemplate(gem2.ID);
                                g2.rID = gem2.realID;
                                WindowInfoScr.this.listTemp.addElement(g2);
                            }
                            GameService.gI().addGemItemImbue(gem2.realID, (byte) 1);
                        }
                    }
                    int aa2 = WindowInfoScr.this.selected % WindowInfoScr.this.numW;
                    int bb = WindowInfoScr.this.selected / WindowInfoScr.this.numW;
                    WindowInfoScr.this.numW = Char.inventory.size() + WindowInfoScr.this.listTemp.size();
                    if (WindowInfoScr.this.numW < 6) {
                        WindowInfoScr.this.numW = 6;
                    }
                    if (aa2 >= WindowInfoScr.this.numW) {
                        aa2 = WindowInfoScr.this.numW - 1;
                    }
                    WindowInfoScr.this.selected = (bb * WindowInfoScr.this.numW) + aa2;
                    WindowInfoScr.cmxLim = ((WindowInfoScr.this.numW * WindowInfoScr.this.wSmall) - WindowInfoScr.this.w) + 6;
                    if (WindowInfoScr.cmxLim < 0) {
                        WindowInfoScr.cmxLim = 0;
                    }
                    if (WindowInfoScr.cmx > WindowInfoScr.cmxLim) {
                        int i2 = WindowInfoScr.cmxLim;
                        WindowInfoScr.cmtoX = i2;
                        WindowInfoScr.cmx = i2;
                    }
                }
            }));
        }
        if (index2 == 4 || ((xx != 1 && siz < MainChar.gemitemImbue.size()) || (this.idImbue != -1 && xx == 1))) {
            menuItem.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.doShowItemInfoImbue();
                }
            }));
        }
        Canvas.menu.startAt(menuItem, 3);
    }

    public void doShowItemInfoImbue() {
        int index2 = this.selected / this.numW;
        int xx = this.selected % this.numW;
        if (index2 == 4) {
            if (xx < Char.inventory.size()) {
                ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(xx);
                showItemInventoryInfo(item, this.isSell, xx * this.wSmall, 4 * this.wSmall);
                return;
            }
            int size0 = this.listTemp.size();
            for (int i = 0; i < size0; i++) {
                if (i == xx - Char.inventory.size()) {
                    GemTemplate gem = (GemTemplate) this.listTemp.elementAt(i);
                    GemTemp g = Res.getGemByID(gem.id);
                    String itemInfo = ItemInInventory.setTemp(g.name, "0");
                    if ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25)) {
                        itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(" - Đã khóa").toString();
                    }
                    setShowText(new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(g.decript, "0")).toString(), (xx * this.wSmall) - cmx, 4 * this.wSmall);
                }
            }
            return;
        }
        if (xx == 1) {
            ItemInInventory item2 = (ItemInInventory) Char.inventory.elementAt(this.idImbue);
            setShowText(item2.getDescription(this.isSell), 50, 2 * this.wSmall);
            return;
        }
        int size = (index2 * 2) + (xx / 2);
        RealID gem2 = (RealID) MainChar.gemitemImbue.elementAt(size);
        GemTemp g2 = Res.getGemByID(gem2.ID);
        String str = ItemInInventory.setTemp(g2.name, "0");
        if ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25)) {
            str = new StringBuffer(String.valueOf(str)).append(" - Đã khóa").toString();
        }
        setShowText(new StringBuffer(String.valueOf(str)).append(ItemInInventory.setTemp(g2.decript, "0")).toString(), xx * 50, index2 * this.wSmall);
    }

    public boolean isBuffSkill() {
        byte[][] skill = {new byte[]{4, 5}, new byte[]{4, 5}, new byte[]{4, 5, 6, 7}, new byte[]{4, 5}, new byte[]{4, 5}};
        int size0 = skill[Canvas.gameScr.mainChar.clazz].length;
        for (int i = 0; i < size0; i++) {
            if (this.selected == skill[Canvas.gameScr.mainChar.clazz][i]) {
                return true;
            }
        }
        return false;
    }

    public void doSelectedSkill() {
        if (Char.skillLevelLearnt[this.selected] == -1) {
            Canvas.startOKDlg("Xin gặp Blaziken để học kỹ năng này");
            return;
        }
        Vector menuItems = new Vector();
        boolean buff2User = false;
        final boolean isBuff = isBuffSkill();
        if (isBuff) {
            buff2User = SkillManager.SKILL_CAN_BUFF_TO_USER[Canvas.gameScr.mainChar.clazz][this.selected - 4] == -1;
        }
        if (isBuff && !buff2User) {
            menuItems.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    if (Char.skillLevelLearnt[WindowInfoScr.this.selected] > 0) {
                        Canvas.menu.showMenu = false;
                        byte b = SkillManager.EFF_BUFF_SKILL[Canvas.gameScr.mainChar.clazz][WindowInfoScr.this.selected - 4];
                        int mpLost = SkillManager.getSkillMP(WindowInfoScr.this.selected, Char.skillLevelLearnt[WindowInfoScr.this.selected - 4], Canvas.gameScr.mainChar.clazz);
                        if (mpLost > Canvas.gameScr.mainChar.mp) {
                            if (!WindowInfoScr.this.first) {
                                Canvas.gameScr.addChat(new ChatInfo("", "Không đủ PP", 0));
                                WindowInfoScr.this.first = true;
                                return;
                            }
                            return;
                        }
                        WindowInfoScr.this.first = false;
                        if (Canvas.gameScr.focusedActor != null && Canvas.gameScr.focusedActor.catagory == 0) {
                            if (WindowInfoScr.this.selected == 6) {
                                GameService.gI().useBuff(Canvas.gameScr.focusedActor.ID, (byte) 0, b, (short) 0);
                                Canvas.gameScr.mainChar.mp -= mpLost;
                                return;
                            }
                            Canvas.gameScr.mainChar.mp -= mpLost;
                            if (isBuff && (Canvas.gameScr.indexBuff == -1 || Canvas.gameScr.indexBuff == WindowInfoScr.this.selected)) {
                                Canvas.gameScr.indexBuff = WindowInfoScr.this.selected;
                            }
                            WindowInfoScr.this.doCastBuffToActor(Canvas.gameScr.mainChar, Canvas.gameScr.focusedActor, b, SkillManager.SKILL_CAN_BUFF_TO_USER[Canvas.gameScr.mainChar.clazz][WindowInfoScr.this.selected - 4] == 1);
                            return;
                        }
                        if (WindowInfoScr.this.selected != 6) {
                            Canvas.gameScr.mainChar.mp -= mpLost;
                            if (isBuff && (Canvas.gameScr.indexBuff == -1 || Canvas.gameScr.indexBuff == WindowInfoScr.this.selected)) {
                                Canvas.gameScr.indexBuff = WindowInfoScr.this.selected;
                            }
                            GameService.gI().useBuff(Canvas.gameScr.mainChar.ID, (byte) 0, b, (short) 0);
                            return;
                        }
                        Canvas.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
                        return;
                    }
                    Canvas.startOKDlg("Chưa học kỹ năng này");
                }
            }));
        }
        if (!buff2User) {
            menuItems.addElement(new Command("Cho vào phím tắt", new IAction() {
                @Override
                public void perform() {
                    if (Char.skillLevelLearnt[WindowInfoScr.this.selected] > 0) {
                        WindowInfoScr.this.doGiveSkillToQuickSlot(WindowInfoScr.this.selected, isBuff);
                    } else {
                        Canvas.startOKDlg("Chưa học kỹ năng này");
                    }
                }
            }));
        }
        menuItems.addElement(new Command("Cộng", new IAction() {
            @Override
            public void perform() {
                if (Canvas.gameScr.mainChar.skillPointLeft == 0) {
                    Canvas.startOKDlg("Đã hết điểm chiêu để cộng. Hãy lên cấp để có thêm điểm chiêu.");
                } else if (Canvas.gameScr.mainChar.level < SkillManager.getLvAddSkill(Canvas.gameScr.mainChar.clazz, WindowInfoScr.this.selected, Char.skillLevelLearnt[WindowInfoScr.this.selected])) {
                    Canvas.startOKDlg("Chưa đủ cấp để cộng. Hãy lên tới cấp yêu cầu.");
                } else {
                    GameService.gI().addSkillPoint(WindowInfoScr.this.selected);
                    Canvas.startWaitDlg("Xin chờ..", true);
                }
            }
        }));
        Canvas.menu.startAt(menuItems, 2);
    }

    protected void doGiveSkillToQuickSlot(final int skillType, final boolean isBuff) {
        Vector menuItems = new Vector();
        for (int i = 0; i < 3; i++) {
            final int ii = i;
            menuItems.addElement(new Command(new StringBuffer("Phím số ").append(1 + (i * 2)).toString(), new IAction() {
                @Override
                public void perform() {
                    MainChar.quickSlot[GameScr.indexTabSlot][ii].setIsSkill(skillType, Canvas.gameScr.mainChar.clazz, isBuff);
                    RMS.saveQuickSlot();
                }
            }));
        }
        Canvas.menu.startAt(menuItems, 2);
    }

    public void doCastBuffToActor(final MainChar actor1, final Actor actor2, final int skillIdEff, boolean buff2Friend) {
        Vector menu = new Vector();
        if (buff2Friend) {
            menu.addElement(new Command("Cho mình", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().useBuff(actor1.ID, (byte) 0, (byte) skillIdEff, (short) 0);
                }
            }));
            menu.addElement(new Command("Cho bạn", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().useBuff(actor2.ID, (byte) 0, (byte) skillIdEff, (short) 0);
                }
            }));
        } else {
            GameService.gI().useBuff(actor1.ID, (byte) 0, (byte) skillIdEff, (short) 0);
        }
        Canvas.menu.startAt(menu, 2);
    }

    public void setCmdCenter() {
        this.charInfo = null;
        switch (index[focusTab]) {
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
                break;
            case 3:
                cmyLim = ((Canvas.gameScr.mainChar.getInfoSkill(this.selected).length + 1) * 12) - 72;
                if (cmyLim < 0) {
                    cmyLim = 0;
                }
                this.center = new Command("", new IAction() {
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
                break;
            case 6:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doSelectedQuest();
                    }
                });
                break;
            case 7:
                this.newSkill = new Vector();
                Vector news = (Vector) GameScr.infoNewSkill.elementAt(Canvas.gameScr.mainChar.clazz);
                byte skillLevel = Char.skillLevelLearnt[((InfoSkillLearn) news.elementAt(this.selected)).idSkill];
                this.newSkill.addElement(new StringBuffer("`").append(((InfoSkillLearn) news.elementAt(this.selected)).name).toString());
                if (skillLevel == -1) {
                    this.newSkill.addElement(new StringBuffer("`Phí: ").append(((InfoSkillLearn) news.elementAt(this.selected)).price).append(" xu").toString());
                }
                String[] info = Font.arialFontW.splitFontBStrInLine(((InfoSkillLearn) news.elementAt(this.selected)).decript, 130);
                for (String str : info) {
                    this.newSkill.addElement(str);
                }
                setSize(114, 30, news.size(), 1, 23, 20);
                cmyLim = (this.newSkill.size() * 12) - 72;
                if (cmyLim < 0) {
                    cmyLim = 0;
                }
                this.center = new Command("", new AnonymousClass55(news));
                break;
            case 8:
            case 26:
            case 27:
            case 29:
            case 30:
            case 32:
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 24:
            default:
                int element = this.selected + (index[focusTab] == 0 ? this.countTabMyitem * 42 : 0);
                this.center = null;
                if (!this.isChangeTab && element < this.list.size()) {
                    this.center = (Command) this.list.elementAt(element);
                    break;
                }
                break;
            case 21:
            case 25:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.setSelectedImbue();
                    }
                });
                break;
            case 22:
            case 23:
                setSelectedKeepAndSellItem();
                break;
            case 28:
                setLimitCheDo();
                break;
            case 31:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        if (WindowInfoScr.charWearing != null) {
                            if (WindowInfoScr.charWearing == Canvas.gameScr.mainChar && !WindowInfoScr.this.isSell) {
                                // 0.9.30: đồ thú của mình - bảng Trang bị thú (Xem / Tháo / Thay theo ô đang chọn)
                                int sel = WindowInfoScr.this.selected;
                                game.pokemon.TrangBiThu.moTaiLoai(sel >= 0 && sel < idWearAnimal.length ? idWearAnimal[sel] : -1);
                                return;
                            }
                            if (WindowInfoScr.charWearing.useHorse == -1 && WindowInfoScr.charWearing.idhorse <= -1) {
                                return;
                            }
                            WindowInfoScr.this.showInfoWearingAnimal();
                        }
                    }
                });
                break;
        }
    }

    class AnonymousClass55 implements IAction {
        private final Vector val$news;

        AnonymousClass55(Vector vector) {
            this.val$news = vector;
        }

        @Override
        public void perform() {
            InfoSkillLearn skill = (InfoSkillLearn) this.val$news.elementAt(WindowInfoScr.this.selected);
            Canvas.startYesNoDlg(new StringBuffer("Bạn muốn học kỹ năng ").append(skill.name).append(" với giá ").append(skill.price).append("xu không? ").toString(), new IAction() {
                @Override
                public void perform() {
                    GameService.gI().learnSkill(Canvas.gameScr.mainChar.clazz, WindowInfoScr.this.selected);
                    Canvas.startWaitDlg();
                }
            });
        }
    }

    public void setLimitCheDo() {
        int bb = this.selected % this.numW;
        if (this.selected / this.numW == 1) {
            int aa = this.listEp.size() / 2;
            if (aa < 6) {
                aa = 6;
            }
            this.numW = aa;
            if (this.numW < 6) {
                this.numW = 6;
            }
            if (bb >= this.numW) {
                bb = this.numW - 1;
            }
            this.selected = this.numW + bb;
        } else if (this.selected / this.numW == 2) {
            this.numW = this.listTemp.size();
            if (this.numW < 6) {
                this.numW = 6;
            }
            if (bb >= this.numW) {
                bb = this.numW - 1;
            }
            this.selected = (2 * this.numW) + bb;
        }
        setLim();
        if (cmx > cmxLim) {
            int i = cmxLim;
            cmtoX = i;
            cmx = i;
        }
    }

    protected void doSelectedTrade() {
        if (this.isShowText) {
            closeText();
            return;
        }
        if ((this.selected / this.numW) + 1 == this.numH) {
            Vector menuitem = new Vector();
            if (this.selected % this.numW < Char.inventory.size()) {
                boolean isCo = false;
                final ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(this.selected % this.numW);
                Vector list = Canvas.gameScr.mainChar.tItems;
                int size0 = list.size();
                for (int i = 0; i < size0; i++) {
                    if (list.elementAt(i) instanceof ItemInInventory) {
                        ItemInInventory itm = (ItemInInventory) list.elementAt(i);
                        if (itm.itemID == item.itemID) {
                            isCo = true;
                            break;
                        }
                    }
                }
                if (!isCo) {
                    menuitem.addElement(new Command("Giao dịch", new IAction() {
                        @Override
                        public void perform() {
                            if (Canvas.gameScr.mainChar.tItems.size() >= 12) {
                                Canvas.startOKDlg("Không thể thêm.");
                            } else {
                                if (item.isSelling) {
                                    return;
                                }
                                Canvas.gameScr.gameService.sendAddItemTrade(item.itemID, 0, 0);
                            }
                        }
                    }));
                }
                menuitem.addElement(new Command("Thông tin", new IAction() {
                    @Override
                    public void perform() {
                        if (!item.isEnoughData) {
                            item.isEnoughData = true;
                            GameService.gI().requestItemInfo(item.itemID, Canvas.gameScr.mainChar.ID);
                        } else {
                            WindowInfoScr.this.setShowText(item.getDescription(false), ((WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18) - WindowInfoScr.cmy, WindowInfoScr.this.h);
                        }
                    }
                }));
            } else {
                int inde = (this.selected % this.numW) - Char.inventory.size();
                int aa = 0;
                int size1 = MainChar.listPotion.length;
                for (int i2 = 0; i2 < size1; i2++) {
                    final int ii = i2;
                    if (MainChar.listPotion[i2].number - MainChar.listPotion[i2].numTrade > 0 && MainChar.listPotion[i2].isTrade) {
                        final int bb = aa;
                        if (aa == inde) {
                            menuitem.addElement(new Command("Giao dịch", new IAction() {
                                @Override
                                public void perform() {
                                    InputDlg inputDlg = Canvas.inputDlg;
                                    final int i3 = ii;
                                    inputDlg.setInfo("Số lượng", new IAction() {
                                        @Override
                                        public void perform() {
                                            try {
                                                int num = Integer.parseInt(Canvas.inputDlg.tfInput.getText());
                                                if (num > 0 && num <= MainChar.listPotion[i3].number - MainChar.listPotion[i3].numTrade) {
                                                    Canvas.gameScr.gameService.sendAddItemTrade((short) i3, 1, num);
                                                    Canvas.endDlg();
                                                }
                                            } catch (Exception e) {
                                            }
                                        }
                                    }, 3, 10, true);
                                    Canvas.inputDlg.show();
                                }
                            }));
                            menuitem.addElement(new Command("Thông tin", new IAction() {
                                @Override
                                public void perform() {
                                    WindowInfoScr.this.setShowText(WindowInfoScr.this.getPotionInfo(ii), (bb % WindowInfoScr.this.numW) * 18, (bb / WindowInfoScr.this.numW) * 18);
                                }
                            }));
                            break;
                        }
                        aa++;
                    }
                }
            }
            Canvas.menu.startAt(menuitem, 3);
            return;
        }
        Vector menu = new Vector();
        Object item2 = null;
        int aa2 = this.selected % this.numW;
        if (aa2 <= 2) {
            // 0.9.27: đưa xu (số tuyệt đối, 0 = rút) - game.pokemon.XuGiaoDich
            menu.addElement(new Command("Đưa xu", new IAction() {
                @Override
                public void perform() {
                    game.pokemon.XuGiaoDich.hoi();
                }
            }));
        }
        if (aa2 <= 2) {
            int bb2 = this.selected / this.numW;
            final int cc = (bb2 * 3) + aa2;
            final Vector tItem = Canvas.gameScr.mainChar.tItems;
            if (cc < tItem.size()) {
                item2 = tItem.elementAt(cc);
                final Object itemHuy = item2;
                menu.addElement(new Command("Hủy", new IAction() {
                    @Override
                    public void perform() {
                        if (tItem.elementAt(cc) instanceof ItemInInventory) {
                            Canvas.gameScr.gameService.sendAddItemTrade(((ItemInInventory) itemHuy).itemID, 0, 0);
                        } else {
                            Canvas.gameScr.gameService.sendAddItemTrade(((Potion) itemHuy).id, 2, 0);
                        }
                    }
                }));
            }
        } else {
            int bb3 = this.selected / this.numW;
            int cc2 = (bb3 * 3) + (aa2 - 3);
            if (cc2 < Canvas.gameScr.mainChar.rItems.size()) {
                item2 = Canvas.gameScr.mainChar.rItems.elementAt(cc2);
            }
        }
        if (item2 != null) {
            final int x1 = aa2 * 18;
            final int y1 = (this.selected / this.numW) * 18;
            final Object it = item2;
            menu.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    if (!(it instanceof ItemInInventory)) {
                        int size2 = MainChar.listPotion.length;
                        for (int i3 = 0; i3 < size2; i3++) {
                            if (MainChar.listPotion[i3].index == ((Potion) it).index) {
                                WindowInfoScr.this.setShowText(WindowInfoScr.this.getPotionInfo(i3), x1, y1);
                                int i4 = 0 + 1;
                                return;
                            }
                        }
                        return;
                    }
                    WindowInfoScr.this.showItemInventoryInfo((ItemInInventory) it, WindowInfoScr.this.isSell, x1, y1);
                }
            }));
        }
        Canvas.menu.startAt(menu, 3);
    }

    protected void doSelectedQuest() {
        if (Canvas.gameScr.mainChar.receiveQuest) {
            Canvas.startYesNoDlg("Bạn thật sự muốn từ bỏ nhiệm vụ này?", new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.mainChar.resetQuest();
                    Canvas.gameScr.dellPotionQuest();
                    GameService.gI().receiveQuest(0, 0, 3);
                    Canvas.startWaitDlg();
                }
            });
        }
    }

    protected void doSelectedParty() {
        if (Char.party.size() > 0) {
            MainChar mc = Canvas.gameScr.mainChar;
            Vector menuItems = new Vector();
            if (mc.ID == mc.IDMasterParty) {
                menuItems.addElement(new Command("Đuổi", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.gameScr.gameService.kickOutParty(((PartyInfo) Char.party.elementAt(WindowInfoScr.this.selected)).id, (byte) 0);
                    }
                }));
                menuItems.addElement(new Command("Giải tán", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.gameScr.gameService.kickOutParty(0, (byte) 1);
                    }
                }));
            } else {
                menuItems.addElement(new Command("Rời nhóm", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.gameScr.gameService.kickOutParty(Canvas.gameScr.mainChar.ID, (byte) 2);
                    }
                }));
            }
            Canvas.menu.startAt(menuItems, 2);
        }
    }

    private void setTextCharInfo() {
        this.charInfo = new String[17];
        MainChar c = Canvas.gameScr.mainChar;
        this.charInfo[0] = new StringBuffer("Pokémon: ").append(c.name).toString();
        this.charInfo[1] = new StringBuffer("Cấp: ").append((int) c.level).append("+").append(c.getPercent()).toString();
        this.charInfo[2] = new StringBuffer("HP: ").append(c.hp).append("/").append(c.maxhp).toString();
        this.charInfo[3] = new StringBuffer("PP: ").append(c.mp).append("/").append(c.maxmp).toString();
        this.charInfo[4] = new StringBuffer("Tấn công: ").append(c.getAttack()).toString();
        this.charInfo[5] = new StringBuffer("Phòng thủ: ").append(c.defend).toString();
        this.charInfo[6] = new StringBuffer("Phòng thủ đặc biệt: ").append(c.defend_magic).toString();
        this.charInfo[7] = new StringBuffer("Chính xác: ").append((int) c.accurate).toString();
        this.charInfo[8] = new StringBuffer("Né tránh: ").append((int) c.dodge).toString();
        this.charInfo[9] = new StringBuffer("Bạo kích: ").append(c.baokich > 0 ? new StringBuffer(String.valueOf(c.baokich / 10)).append(".").append(c.baokich % 10).toString() : "0").append("%").toString();
        this.charInfo[10] = new StringBuffer("Chí mạng: ").append((int) c.critical).toString();
        this.charInfo[11] = new StringBuffer("Cống hiến: ").append(c.dedicationPoint).append(" điểm.").toString();
        this.charInfo[12] = new StringBuffer("Chuỗi thắng: ").append(c.lienTram).append(" điểm.").toString();
        this.charInfo[13] = new StringBuffer("Công trạng: ").append(c.congTrang).append(" điểm.").toString();
        this.charInfo[14] = new StringBuffer("Điểm hoạt động: ").append(c.hoatdong).append(" điểm.").toString();
        this.charInfo[15] = new StringBuffer("Điểm đấu trường: ").append(c.pArena).append(" điểm.").toString();
        this.charInfo[16] = c.nameHusband_wife.equals("") ? "Chưa kết đôi" : "Kết đôi: " + c.nameHusband_wife;
        int soHang = this.charInfo.length - 4 + (infoMainCharServer != null ? infoMainCharServer.length : 0);
        this.numH = (46 + soHang * 13 + 14) / Math.max(1, this.hSmall) + 1;   // pokemon: thẻ đầu + hàng chỉ số
        setLim();
    }

    private void setCamEpDoThu() {
        if (Canvas.isPointerClick) {
            int size0 = this.posEpNgoc.length;
            for (int i = 0; i < size0; i++) {
                if (Canvas.isPointer(((this.x + this.x0) + this.posEpNgoc[i][0]) - 3, ((this.y + this.y0) + this.posEpNgoc[i][1]) - 3, 23, 23)) {
                    Canvas.isPointerClick = false;
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
            }
        }
    }

    private void setCamEpNgoc() {
        if (Canvas.isPointerClick) {
            int size0 = this.posEpNgoc.length;
            for (int i = 0; i < size0; i++) {
                if (Canvas.isPointer(((this.x + this.x0) + this.posEpNgoc[i][0]) - 3, ((this.y + this.y0) + this.posEpNgoc[i][1]) - 3, 23, 23)) {
                    Canvas.isPointerClick = false;
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
            }
        }
    }

    /** 0.9.11: thanh cuộn lưới / danh sách vẽ ở khung trước (toạ độ màn): cột thLuoiX, rãnh thLuoiY..+thLuoiH, núm thLuoiN */
    private int thLuoiX, thLuoiY, thLuoiH, thLuoiN;
    private boolean keoNumLuoi;
    private int numDauLuoi, pyNumLuoi;

    /** Nhớ chỗ thanh cuộn vừa vẽ bằng KieuO.thanhCuon(g, x, 0, h, tong, cmy, h) (để kéo núm). */
    private void nhoThanhLuoi(Graphics g, int x, int h, int tong) {
        if (tong <= h || h < 8 || cmyLim <= 0) {
            return;
        }
        this.thLuoiX = x + g.getTranslateX();
        this.thLuoiY = g.getTranslateY();
        this.thLuoiH = h;
        this.thLuoiN = Math.min(h, Math.max(6, h * h / tong));
    }

    /**
     * 0.9.11: kéo NÚM thanh cuộn lưới - núm theo con trỏ, nội dung theo tỉ lệ cùng chiều; chạm rãnh = nhảy tới chỗ
     * chạm. Trả true khi cú nhấn / thả thuộc về thanh.
     */
    private boolean keoThanhLuoi() {
        if (this.keoNumLuoi) {
            if (Canvas.dangGiu || Canvas.isPointerDown) {
                cmtoY = cmy = game.pokemon.ThanhCuon.keo(this.numDauLuoi, this.pyNumLuoi, Canvas.py, this.thLuoiH, this.thLuoiN, cmyLim);
                return true;
            }
            this.keoNumLuoi = false;
            Canvas.isPointerClick = false;   // cú thả của lượt kéo núm: không chọn ô
            return true;
        }
        if (this.thLuoiH > 0 && cmyLim > 0 && (Canvas.dangGiu || Canvas.isPointerDown)
                && game.pokemon.ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, this.thLuoiX, 3, this.thLuoiY, this.thLuoiH)) {
            int c = Math.max(0, Math.min(cmyLim, cmy));
            if (!game.pokemon.ThanhCuon.trenNum(Canvas.pyLast, this.thLuoiY, this.thLuoiH, this.thLuoiN, c, cmyLim)) {
                c = game.pokemon.ThanhCuon.toi(Canvas.pyLast, this.thLuoiY, this.thLuoiH, this.thLuoiN, cmyLim);
            }
            this.keoNumLuoi = true;
            this.numDauLuoi = c;
            this.pyNumLuoi = Canvas.pyLast;
            cmtoY = cmy = game.pokemon.ThanhCuon.keo(c, this.pyNumLuoi, Canvas.py, this.thLuoiH, this.thLuoiN, cmyLim);
            return true;
        }
        return false;
    }

    private void updateKeyMain() {
        if (!this.isShowText && keoThanhLuoi()) {
            return;
        }
        boolean changeFocus = false;
        if (Canvas.isPointer(this.x + this.x0, this.y + this.y0, this.w, this.h)) {
            if (index[focusTab] == 26) {
                setCamEpNgoc();
            }
            if (index[focusTab] == 32) {
                if (Canvas.py >= this.y + this.y0 + this.posEpNgoc[1][1] && Canvas.py <= this.posEpNgoc[1][1] + 65 + this.y + this.y0) {
                    setCamEpDoThu();
                } else if (Canvas.isPointer(this.x0 + this.x, ((this.y + this.y0) + (4 * this.hSmall)) - 5, 120, 20)) {
                    Canvas.isPointerClick = false;
                    int aa = (Canvas.py - (((this.y + this.y0) + (4 * this.hSmall)) - 5)) / this.hSmall;
                    int bb = (Canvas.px - ((this.x0 + this.x) + 5)) / 20;
                    this.selected = (aa * 6) + bb + 6;
                    if (this.selected < 6) {
                        this.selected = 6;
                    } else if (this.selected > 11) {
                        this.selected = 11;
                    }
                    if (this.numH != 1) {
                        this.numH = 1;
                        this.numW = 6;
                        cmtoY = 0;
                        cmtoX = 0;
                        cmy = 0;
                        cmx = 0;
                    }
                    doSelectedEpDothu();
                }
            }
            int i = Canvas.pyLast - Canvas.py;
            int i2 = Canvas.pxLast - Canvas.px;
            if (Canvas.isPointerDown && index[focusTab] != 1 && !this.isShowText) {
                if (!this.trans) {
                    this.pb = cmy;
                    this.pa = cmx;
                    this.trans = true;
                    int aa2 = ((cmtoY + Canvas.py) - (this.y + this.y0)) / this.hSmall;
                    int bb2 = ((cmtoX + Canvas.px) - (this.x + this.x0)) / this.wSmall;
                    if (index[focusTab] != 3) {
                        if (index[focusTab] != 32) {
                            changeFocus = true;
                        }
                        setCmdCenter();
                        if (aa2 == 5 && this.numH != 5 && index[focusTab] == 26) {
                            this.selected = 3;
                            setSizeEpNgoc(1, false);
                        } else if (aa2 != 4 && (index[focusTab] == 21 || index[focusTab] == 25)) {
                            setUpdatePointerImbue(bb2, aa2);
                        } else if (aa2 == 5 && this.numH != 5 && index[focusTab] == 32) {
                            this.selected = 3;
                            setSizeEpNgoc(1, true);
                        } else {
                            if ((index[focusTab] == 21 || index[focusTab] == 25) && aa2 == 4 && this.selected / this.numW != 4) {
                                this.selected = 10;
                                setSizeImbue(1);
                            }
                            if (index[focusTab] != 32) {
                                this.selected = (aa2 * this.numW) + bb2;
                            }
                        }
                    }
                } else {
                    this.isCmyTab3 = true;
                }
                this.isTab3 = true;
                this.isshowInfo = true;
                cmtoY = this.pb + (Canvas.pyLast - Canvas.py);
                if (index[focusTab] != 32 || Canvas.py >= this.posEpNgoc[1][1] + 70 + this.y + this.y0) {
                    cmtoX = this.pa + (Canvas.pxLast - Canvas.px);
                }
                if (cmtoY < -10) {
                    cmtoY = -10;
                }
                if (cmtoY > cmyLim + 10) {
                    cmtoY = cmyLim + 10;
                }
                if (cmtoX < -10) {
                    cmtoX = -10;
                }
                if (cmtoX > cmxLim + 10) {
                    cmtoX = cmxLim + 10;
                }
            }
            if (Canvas.isPointerClick) {
                this.xCus = -20;
                this.isChangeTab = false;
                Canvas.isPointerClick = false;
                this.trans = false;
                if (index[focusTab] != 1) {
                    if (index[focusTab] != 3) {
                        if (index[focusTab] != 32) {
                            setCmdCenter();
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                        } else if (Canvas.py >= this.posEpNgoc[1][1] + 90 + this.y + this.y0 && Math.abs(Canvas.pxLast - Canvas.px) <= 10) {
                            int aa3 = ((cmtoY + Canvas.py) - (this.y + this.y0)) / this.hSmall;
                            int bb3 = ((cmtoX + Canvas.px) - (this.x + this.x0)) / this.wSmall;
                            this.numW = this.listItemAnimal.size() + this.listTemp.size();
                            if (this.numW < 6) {
                                this.numW = 6;
                            }
                            this.numH = 5;
                            cmxLim = (this.numW * 20) - (this.w - 8);
                            if (cmxLim < 0) {
                                cmxLim = 0;
                            }
                            this.selected = ((aa3 - 1) * this.numW) + bb3;
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                        }
                    } else {
                        int fc = ((cmtoX + Canvas.px) - (this.x + this.x0)) / this.wSmall;
                        if (fc < 0) {
                            fc = 0;
                        } else if (fc > SkillManager.SKILL_NAME[Canvas.gameScr.mainChar.clazz].length - 1) {
                            fc = SkillManager.SKILL_NAME[Canvas.gameScr.mainChar.clazz].length - 1;
                        }
                        if (this.selected != fc) {
                            this.selected = fc;
                        } else {
                            setCmdCenter();
                            if (this.center != null) {
                                this.center.action.perform();
                            }
                        }
                    }
                } else if (Canvas.px >= this.x + 33 && Canvas.px <= this.x + 53) {
                    this.selected = 4;
                    if (Math.abs(Canvas.pyLast - Canvas.py) < 10 && Math.abs(Canvas.pxLast - Canvas.px) < 10) {
                        setCmdCenter();
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                    }
                } else if (Canvas.px > this.x + 80 && Canvas.px <= this.x + 96) {
                    this.selected = 1;
                    if (Math.abs(Canvas.pyLast - Canvas.py) < 10 && Math.abs(Canvas.pxLast - Canvas.px) < 10) {
                        setCmdCenter();
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                    }
                } else if (Canvas.px > this.x && Canvas.px <= this.x + 30) {
                    int yy = (Canvas.py - (this.y + this.y0)) / this.hSmall;
                    int xx = (Canvas.px - (this.x + this.x0)) / this.wSmall;
                    this.selected = (yy * this.numW) + xx;
                    if (this.selected > 12) {
                        this.selected = 12;
                    }
                    if (Math.abs(Canvas.pyLast - Canvas.py) < 10 && Math.abs(Canvas.pxLast - Canvas.px) < 10) {
                        setCmdCenter();
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                    }
                } else if (Canvas.px > this.x + 100 && Canvas.px <= this.x + 120) {
                    int yyy = (Canvas.py - (this.y + this.y0)) / this.hSmall;
                    int xxx = (Canvas.px - (this.x + this.x0)) / this.wSmall;
                    this.selected = (yyy * this.numW) + xxx + 1;
                    if (this.selected > 14) {
                        this.selected = 14;
                    }
                    if (Math.abs(Canvas.pyLast - Canvas.py) < 10 && Math.abs(Canvas.pxLast - Canvas.px) < 10) {
                        setCmdCenter();
                        if (this.center != null) {
                            this.center.action.perform();
                        }
                    }
                }
            }
        } else {
            this.trans = false;
            if (index[focusTab] == 3 && !this.isshowInfo && Canvas.isPointer(this.x + this.x0, this.y + this.y0 + 30, this.w, 65)) {
                this.isCmyTab3 = false;
                if (Canvas.isPointerDown) {
                    if (!this.isTab3) {
                        this.pb = cmy;
                        this.isTab3 = true;
                        setCmdCenter();
                    }
                    cmtoY = this.pb + (Canvas.pyLast - Canvas.py);
                    if (cmtoY < -10) {
                        cmtoY = -10;
                    }
                    if (cmtoY > cmyLim + 10) {
                        cmtoY = cmyLim + 10;
                    }
                }
                if (Canvas.isPointerClick) {
                    Canvas.isPointerClick = false;
                    this.isTab3 = false;
                }
            }
        }
        if (!Canvas.isPointerDown) {
            this.isshowInfo = false;
        }
        if (Canvas.isKeyPressed(2)) {
            this.xCus = -20;
            if (index[focusTab] == 32) {
                setSizeEpNgoc(-1, true);
            } else if (index[focusTab] == 26) {
                setSizeEpNgoc(-1, false);
            } else {
                if (index[focusTab] == 8) {
                    setSizeTrade(-1);
                } else if (index[focusTab] == 21 || index[focusTab] == 25) {
                    setSizeImbue(-1);
                }
                if (index[focusTab] != 1) {
                    if (this.selected / this.numW > 0) {
                        changeFocus = true;
                        this.selected -= this.numW;
                        setCmdCenter();
                    }
                } else {
                    byte[] arr = {0, 4, 2, 0, 12, 2, 3, 2, 5, 6, 6, 8, 9, 9, 11};
                    this.selected = arr[this.selected];
                    changeFocus = true;
                    setCmdCenter();
                }
            }
        } else if (Canvas.isKeyPressed(4)) {
            if (index[focusTab] == 32) {
                this.selected--;
                changeFocus = true;
                if (this.selected < 0) {
                    this.selected = 0;
                }
                if (this.selected == 5) {
                    this.selected = 6;
                }
                if (this.selected > 12 && this.selected < this.selected % this.numW) {
                    this.selected = (this.numH * this.numW) - 1;
                }
            } else if (index[focusTab] != 1) {
                if (this.numW > 1 && this.selected % this.numW > 0) {
                    if ((index[focusTab] != 22 && index[focusTab] != 23) || !setSizeKeepItem(-1)) {
                        changeFocus = true;
                    }
                    this.selected--;
                    setCmdCenter();
                }
            } else {
                byte[] arr4 = {0, 4, 1, 3, 12, 1, 6, 6, 1, 9, 9, 1, 12, 12, 1};
                changeFocus = true;
                this.selected = arr4[this.selected];
                setCmdCenter();
            }
        } else if (Canvas.isKeyPressed(6)) {
            if (index[focusTab] == 32) {
                this.selected++;
                changeFocus = true;
                if (this.selected == 12) {
                    this.selected = 11;
                }
                if (this.selected == 5) {
                    this.selected = 4;
                }
                if (this.selected > 12 && this.selected > (this.numH * this.numW) - 1) {
                    this.selected = (this.numH - 1) * this.numW;
                }
            } else if (index[focusTab] != 1) {
                if (this.numW > 1 && this.selected < (this.numH * this.numW) - 1) {
                    if ((index[focusTab] != 22 && index[focusTab] != 23) || !setSizeKeepItem(1)) {
                        changeFocus = true;
                    }
                    this.selected++;
                    setCmdCenter();
                }
            } else {
                byte[] arr6 = {4, 2, 2, 4, 1, 5, 4, 4, 8, 4, 9, 11, 4, 12, 14};
                changeFocus = true;
                this.selected = arr6[this.selected];
                setCmdCenter();
            }
        } else if (Canvas.isKeyPressed(8)) {
            this.xCus = -20;
            if (index[focusTab] == 32) {
                setSizeEpNgoc(1, true);
            } else if (index[focusTab] == 26) {
                setSizeEpNgoc(1, false);
            } else {
                if (index[focusTab] == 8) {
                    setSizeTrade(1);
                }
                if (index[focusTab] != 1) {
                    if (this.selected / this.numW < this.numH - 1) {
                        if (index[focusTab] == 21 || index[focusTab] == 25) {
                            setSizeImbue(1);
                        }
                        changeFocus = true;
                        this.selected += this.numW;
                        setCmdCenter();
                    }
                } else {
                    byte[] arr2 = {3, 2, 5, 6, 1, 8, 9, 2, 11, 12, 14, 14, 4, 13, 14};
                    this.selected = arr2[this.selected];
                    changeFocus = true;
                    setCmdCenter();
                }
            }
        }
        if (this.list.size() == 0 && index[focusTab] != 22 && index[focusTab] != 23) {
            if (Canvas.keyHold[2]) {
                changeFocus = false;
                cmtoY -= 10;
                if (cmtoY < 0) {
                    cmtoY = 0;
                }
            } else if (Canvas.keyHold[8]) {
                changeFocus = false;
                cmtoY += 10;
                if (cmtoY > cmyLim) {
                    cmtoY = cmyLim;
                }
            }
        }
        if (changeFocus) {
            this.isCmyTab3 = false;
            cmtoY = ((this.selected / this.numW) * this.hSmall) - (this.h / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
            if (index[focusTab] != 32 || this.selected > 4) {
                cmtoX = ((this.selected % this.numW) * this.wSmall) - (this.w / 2);
            }
            if (cmtoX < 0) {
                cmtoX = 0;
            }
            if (cmtoX > cmxLim) {
                cmtoX = cmxLim;
            }
        }
    }

    private void setUpdatePointerImbue(int bb, int aa) {
        if (bb > 0 && bb < 5) {
            this.selected = 1;
            return;
        }
        if (bb == 5) {
            bb = 2;
        }
        this.selected = (aa * this.numW) + bb;
    }

    private void setSizeEpNgoc(int i, boolean isAn) {
        if (i == -1) {
            if (this.numH != 1) {
                this.numH = 1;
                this.selected = 6;
                this.numW = 5;
                cmtoY = 0;
                cmtoX = 0;
                cmy = 0;
                cmx = 0;
                return;
            }
            if (this.selected == 2) {
                this.selected = 0;
                return;
            }
            if (this.selected == 3) {
                this.selected = 2;
                return;
            }
            if (this.selected == 4) {
                this.selected = 1;
                return;
            } else {
                if (this.selected >= 6 && this.selected < 12) {
                    this.selected = 4;
                    return;
                }
                return;
            }
        }
        if (this.selected == 3 || this.selected == 4) {
            if (!isAn) {
                this.numW = this.listTemp.size();
                if (this.numW < 6) {
                    this.numW = 6;
                }
                this.numH = 5;
                this.selected = (this.numH - 1) * this.numW;
                cmxLim = (this.numW * 20) - (this.w - 8);
                if (cmxLim < 0) {
                    cmxLim = 0;
                }
                cmtoY = 0;
                cmtoX = 0;
                cmy = 0;
                cmx = 0;
                return;
            }
            this.selected = 6;
            this.numW = 6;
            cmtoY = 0;
            cmtoX = 0;
            cmy = 0;
            cmx = 0;
            return;
        }
        if (this.selected == 2) {
            this.selected = 3;
            return;
        }
        if (this.selected == 0) {
            this.selected = 2;
            return;
        }
        if (this.selected == 1) {
            this.selected = 4;
        } else if (this.selected >= 6 && this.selected < 12) {
            resetEpdothu(-1);
        }
    }

    public boolean setSizeKeepItem(int i) {
        int size;
        int cm = cmy;
        if (index[focusTab] == 22) {
            size = Char.itembag.size();
        } else {
            size = this.sellItem.size();
        }
        if (Char.inventory.size() + MainChar.gemItem.size() < size) {
            this.numH = (size / 3) + 1;
        } else {
            this.numH = ((Char.inventory.size() + MainChar.gemItem.size()) / 3) + 1;
        }
        if (this.numH < 5) {
            this.numH = 5;
        }
        if ((this.selected % this.numW == 2 && i == 1) || (this.selected % this.numW == 3 && i == -1)) {
            if (i == -1) {
                cmyLim = ((((Char.inventory.size() + MainChar.gemItem.size()) / 3) + 1) * this.wSmall) - this.h;
            } else {
                cmyLim = (((size / 3) + 1) * this.wSmall) - this.h;
            }
            if (cmyLim < 0) {
                cmyLim = 0;
            }
            int dd = (this.selected / this.numW) - (cmy / this.wSmall);
            int i2 = this.cmyKeep;
            cmtoY = i2;
            cmy = i2;
            this.selected = (((cmy / this.wSmall) + dd) * this.numW) + (this.selected % this.numW);
            this.cmyKeep = cm;
            return true;
        }
        cmyLim = (this.numH * this.wSmall) - this.h;
        if (cmyLim < 0) {
            cmyLim = 0;
            return false;
        }
        return false;
    }

    private void setSizeTrade(int dir2) {
        int aa = (this.selected % this.numW) - (cmx / 18);
        if (aa < 0) {
            aa = 0;
        }
        int bb = this.selected / this.numW;
        if ((this.selected / this.numW) + 2 == this.numH && dir2 == 1) {
            int size = 0;
            for (int i = 0; i < MainChar.listPotion.length; i++) {
                if (MainChar.listPotion[i].number - MainChar.listPotion[i].numTrade > 0 && MainChar.listPotion[i].isTrade) {
                    size++;
                }
            }
            this.numW = Char.inventory.size() + size;
            if (this.numW < 6) {
                this.numW = 6;
            }
            cmxLim = (this.numW * 18) - this.w;
        } else {
            this.numW = 6;
            cmxLim = 0;
        }
        if (aa >= this.numW) {
            aa = this.numW - 1;
        }
        this.selected = (bb * this.numW) + aa;
        cmtoY = 0;
        cmtoX = 0;
        cmy = 0;
        cmx = 0;
    }

    private void setSizeImbue(int dir2) {
        int aa = (this.selected % this.numW) - (cmx / 20);
        if (aa < 0) {
            aa = 0;
        }
        int bb = this.selected / this.numW;
        if (bb + 2 == 5 && dir2 == 1) {
            this.numW = Char.inventory.size() + this.listTemp.size();
            if (this.numW < 6) {
                this.numW = 6;
            }
            cmxLim = ((this.numW * 20) - this.w) + 6;
            if (cmxLim < 0) {
                cmxLim = 0;
            }
        } else {
            this.numW = 3;
            cmxLim = 0;
        }
        if (aa >= this.numW) {
            aa = this.numW - 1;
        }
        this.selected = (bb * this.numW) + aa;
        cmtoY = 0;
        cmtoX = 0;
        cmy = 0;
        cmx = 0;
    }

    @Override
    public void update() {
        if (laCuaSoCham() && (Canvas.isPointerClick || Canvas.isPointerDown || Canvas.isPointerRelease)) {
            game.pokemon.ManCu.phimKhongCham(new Runnable() {
                public void run() {
                    WindowInfoScr.this.update();
                }
            });
            return;
        }
        Canvas.gameScr.update();
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        if (cmx != cmtoX) {
            cmvx = (cmtoX - cmx) << 2;
            cmdx += cmvx;
            cmx += cmdx >> 4;
            cmdx &= 15;
        }
        if (Math.abs(cmtoY - cmy) < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10 && cmy > cmyLim) {
            cmtoY = cmyLim;
        }
        if (cmtoXName != cmxname) {
            cmxname += (cmtoXName - cmxname) / 2;
            if (Util.abs(cmxname) <= 1) {
                cmxname = 0;
            }
        }
        if (cmyText != cmtoYText) {
            cmvyText = (cmtoYText - cmyText) << 2;
            cmdyText += cmvyText;
            cmyText += cmdyText >> 4;
            cmdyText &= 15;
        }
        if (index[focusTab] == 31 && charWearing != null && charWearing.imgAnimal != null) {
            if (Canvas.gameTick % charWearing.timeChangeFrame == 0) {
                this.countFrame++;
                if (this.countFrame > this.arrFrame[charWearing.numFrame == 3 ? (char) 0 : (char) 1].length - 1) {
                    this.countFrame = 0;
                }
            }
            this.idFrame = this.arrFrame[charWearing.numFrame == 3 ? (char) 0 : (char) 1][this.countFrame];
        }
        if (Canvas.menu.showMenu) {
            closeText();
        }
        if (index[focusTab] == 0 && this.list != null && this.list.size() > 0) {
            this.limMyBag = Canvas.gameScr.mainChar.limItemBag - 1;
            if (this.limMyBag <= 0) {
                this.limMyBag = 0;
            }
            this.excess = this.list.size() % 42;
            this.maxSize = this.list.size();
        }
    }

    @Override
    public void paint(Graphics g) {
        this.thLuoiH = 0;
        Canvas.gameScr.paint(g);
        if (laCuaSoCham()) {
            veCuaSoCham(g);
            return;
        }
        this.anNutLenh = false;
        g.translate(this.x, this.y);
        // pokemon: khung theo bề ngang / cao của tab (rk x hk), mọi vị trí tính từ khung
        int dl = (this.rk - 145) / 2;
        game.pokemon.GiaoDien.bang(g, -8, 1, this.rk, this.hk);
        if (index[focusTab] == 0) {
            // pokemon: chấm trang hành trang (trang đang xem vàng)
            game.pokemon.KieuO.chamTrang(g, 64 + dl, 29, Canvas.gameScr.mainChar.limItemBag, this.countTabMyitem);
        }
        if (this.isPaintMoney) {
            int hMoney = this.hk - 11;
            if (Canvas.h < 196) {
                hMoney = this.hk - 24;
            }
            // pokemon: biểu tượng tiền (lượng trái, lượng khoá giữa, xu phải) thay "l" / "lk" / "$"; chạm = chú thích. 31/10/2026: xu
            // canh phải cách góc khung 22 px (số gọn "1,25 Tr" bằng arialFont không chui dưới hoa văn góc)
            game.pokemon.TienTe.veHangTien(g, 5, 63 + dl, this.rk - 22, hMoney - 2, Font.smallFontColor[0]);
        }
        // pokemon: mũi tên đổi tab vàng viền tối
        g.setColor(game.pokemon.GiaoDien.VIEN);
        g.fillTriangle(1 - countL, 19, 10 - countL, 13, 10 - countL, 25);
        int xp = this.rk - 145;
        g.fillTriangle(127 + xp + countR, 19, 118 + xp + countR, 13, 118 + xp + countR, 25);
        g.setColor(game.pokemon.GiaoDien.VANG);
        g.fillTriangle(3 - countL, 19, 9 - countL, 15, 9 - countL, 23);
        g.fillTriangle(125 + xp + countR, 19, 119 + xp + countR, 15, 119 + xp + countR, 23);
        if (countL > 0) {
            countL = (byte) (countL - 1);
        }
        if (countR > 0) {
            countR = (byte) (countR - 1);
        }
        // pokemon: tên tab trên viên đỏ (đang chuyển tab: xanh)
        game.pokemon.GiaoDien.khoi(g, 19 + dl, 10, 92, 18, 6, game.pokemon.GiaoDien.VIEN);
        game.pokemon.GiaoDien.khoiDoc(g, 20 + dl, 11, 90, 16, 5, this.isChangeTab ? game.pokemon.GiaoDien.XANH_SANG : game.pokemon.GiaoDien.DO_SANG, this.isChangeTab ? game.pokemon.GiaoDien.XANH : game.pokemon.GiaoDien.DO_TOI);
        g.setClip(21 + dl, 11, 88, 16);
        g.translate(-cmxname, 0);
        boolean sangTab = Font.nenSang;
        Font.nenSang = false;   // tên tab chữ trắng trên viên đỏ / xanh
        String tenTab = game.pokemon.GiaoDien.catChu(Font.normalFont[0], this.name, 86);
        Font.normalFont[0].drawString(g, tenTab, 66 + dl, 12, 2);
        if (cmxname != 0) {
            Font.normalFont[0].drawString(g, game.pokemon.GiaoDien.catChu(Font.normalFont[0], this.firName, 86), 66 + dl + (100 * dir), 12, 2);
        }
        Font.nenSang = sangTab;
        g.translate(cmxname, 0);
        g.translate(this.x0, this.y0);
        g.setClip(-100, -100, 300, 300);
        switch (index[focusTab]) {
            case 1:
                paintCharWearing(g);
                break;
            case 2:
                paintFeatures(g);
                break;
            case 3:
                paintSkill(g);
                break;
            case 4:
                paintCharInfo(g);
                break;
            case 5:
                paintParty(g);
                break;
            case 6:
                paintQuest(g);
                break;
            case 7:
                paintNewSkillLearn(g);
                break;
            case 8:
                paintTrade(g);
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 24:
            case 29:
            case 30:
            default:
                if (this.laMart) {
                    paintMart(g);
                    break;
                }
                // pokemon: lưới ô bo góc có khe, sọc xen kẽ, ô trống mờ, ô chọn viền vàng dày
                game.pokemon.GiaoDien.khoi(g, -2, -2, (this.numW * this.wSmall) + 3, (5 * this.hSmall) + 3, 3, 0x0B1730);
                g.setClip(-2, -2, (this.numW * this.wSmall) + 3, (5 * this.hSmall) + 3);
                g.translate(0, -cmy);
                int xx = cmy / this.hSmall;
                if (xx < 0) {
                    xx = 0;
                }
                int yy = xx + (this.w / this.hSmall);
                if (index[focusTab] == 0 && yy > 42) {
                    yy = 42;
                }
                int soCo = index[focusTab] == 0 ? this.list.size() - (this.countTabMyitem * 42) : this.list.size();
                if (this.isLoad) {
                    soCo = 0;
                }
                int soHang = Math.max(this.numH, 5);
                game.pokemon.KieuO.luoi(g, this.numW, xx, Math.min(yy, soHang), this.wSmall, soCo, this.isChangeTab ? -1 : this.selected);
                if (index[focusTab] != 0) {
                    paintProduct(g);
                } else {
                    paintBag(g);
                }
                if (!this.isChangeTab) {
                    game.pokemon.KieuO.chonO(g, (this.selected % this.numW) * this.wSmall, (this.selected / this.numW) * this.hSmall, this.wSmall);
                }
                g.translate(0, cmy);
                g.setClip(-10, -40, this.rk + 20, this.hk + 40);
                game.pokemon.KieuO.thanhCuon(g, (this.numW * this.wSmall) + 2, 0, 5 * this.hSmall, soHang * this.hSmall, cmy, 5 * this.hSmall);
                nhoThanhLuoi(g, (this.numW * this.wSmall) + 2, 5 * this.hSmall, soHang * this.hSmall);
                break;
            case 21:
            case 25:
                paintImbue(g);
                break;
            case 22:
            case 23:
                paintKeepAndSellItem(g);
                break;
            case 26:
                paintEpNgoc(g);
                break;
            case 27:
                paintQuestClan(g);
                break;
            case 28:
                paintCheDo(g);
                break;
            case 31:
                paintAnimalWearing(g);
                break;
            case 32:
                paintEpDothu(g);
                break;
        }
        if (this.isShowText) {
            Canvas.resetTrans(g);
            paintShowText(g);
        }
        super.paint(g);
    }

    int canPutItem() {
        int count = -1;
        for (int i = 0; i < this.itemEpdothu.length; i++) {
            if (this.itemEpdothu[i] == null) {
                count++;
            }
        }
        return count;
    }

    public void resetEpdothu(int saveIndex) {
        this.numW = this.listItemAnimal.size() + this.listTemp.size();
        if (this.numW < 6) {
            this.numW = 6;
        }
        this.numH = 5;
        if (saveIndex == -1) {
            this.selected = (this.numH - 1) * this.numW;
        } else {
            this.selected -= this.numH;
            if (this.selected > (this.numH * this.numW) - 1) {
                this.selected = (this.numH * this.numW) - 1;
            }
            if (this.selected < (this.numH - 1) * this.numW) {
                this.selected = (this.numH - 1) * this.numW;
            }
            cmtoX = ((this.selected % this.numW) * this.wSmall) - (this.w / 2);
            if (cmtoX < 0) {
                cmtoX = 0;
            }
            if (cmtoX > cmxLim) {
                cmtoX = cmxLim;
            }
            cmx = cmtoX;
        }
        cmxLim = (this.numW * 20) - (this.w - 8);
        if (cmxLim < 0) {
            cmxLim = 0;
        }
        if (saveIndex == -1) {
            cmtoY = 0;
            cmtoX = 0;
            cmy = 0;
            cmx = 0;
        }
    }

    public void doshowInfoEpdothu() {
        ItemInInventory item = (ItemInInventory) this.listItemAnimal.elementAt(this.selected % this.numW);
        showItemInventoryInfo(item, false, ((this.selected % this.numW) * 20) + 4, 86);
    }

    public void doSelectedEpDothu() {
        final ItemInInventory real;
        final GemTemplate g;
        if (this.isShowText) {
            return;
        }
        Vector menu = new Vector();
        if (this.numH != 1) {
            if (this.selected % this.numW >= 0 && this.selected % this.numW < this.listItemAnimal.size()) {
                final ItemInInventory item = (ItemInInventory) this.listItemAnimal.elementAt(this.selected % this.numW);
                if (canPutItem() > -1) {
                    menu.addElement(new Command("Bỏ vào", new IAction() {
                        @Override
                        public void perform() {
                            if (item.level >= 30 && item.level <= WindowInfoScr.lvThu + 5 && item.level >= WindowInfoScr.lvThu - 5 && item.colorName == WindowInfoScr.idColor) {
                                if (WindowInfoScr.this.itemEpdothu[2] == null) {
                                    if (WindowInfoScr.magicAnimal == item.magic_physic) {
                                        WindowInfoScr.this.itemEpdothu[2] = item;
                                        WindowInfoScr.this.listItemAnimal.removeElement(item);
                                    }
                                } else {
                                    for (int i = 0; i < WindowInfoScr.this.itemEpdothu.length; i++) {
                                        if (WindowInfoScr.this.itemEpdothu[i] == null) {
                                            WindowInfoScr.this.itemEpdothu[i] = item;
                                            WindowInfoScr.this.listItemAnimal.removeElement(item);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                Canvas.startOKDlg("Đồ ép phải cùng màu và chênh không quá 5 cấp.");
                            }
                            WindowInfoScr.this.resetEpdothu(1);
                        }
                    }));
                }
                menu.addElement(new Command("Thông tin ", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.doshowInfoEpdothu();
                        Canvas.endDlg();
                    }
                }));
            } else if ((this.selected - this.listItemAnimal.size()) % this.numW >= 0 && (this.selected - this.listItemAnimal.size()) % this.numW < this.listTemp.size()) {
                final GemTemplate item2 = (GemTemplate) this.listTemp.elementAt((this.selected - this.listItemAnimal.size()) % this.numW);
                menu.addElement(new Command("Bỏ vào", new IAction() {
                    @Override
                    public void perform() {
                        if (WindowInfoScr.this.listEp.size() < 6) {
                            if (WindowInfoScr.this.checkItem(false) >= WindowInfoScr.slNLThu) {
                                Canvas.startOKDlg("Nguyên liệu đã đủ số lượng.");
                                return;
                            }
                            item2.number--;
                            if (item2.number <= 0) {
                                WindowInfoScr.this.listTemp.removeElement(item2);
                            }
                            GemTemplate real2 = new GemTemplate(item2.rID);
                            real2.id = item2.id;
                            WindowInfoScr.this.listEp.addElement(real2);
                        }
                        boolean aa = false;
                        int bb = WindowInfoScr.this.listEp.size();
                        for (int i = 0; i < bb; i++) {
                            GemTemplate real3 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i);
                            if (real3.id == item2.id) {
                                real3.number++;
                                aa = true;
                                break;
                            }
                        }
                        for (int i2 = 0; i2 < bb; i2++) {
                            GemTemplate real4 = (GemTemplate) WindowInfoScr.this.listEp.elementAt(i2);
                            if (real4.number == 0) {
                                WindowInfoScr.this.listEp.removeElement(real4);
                            }
                        }
                        if (!aa) {
                            GemTemplate ge = new GemTemplate(item2.id);
                            GemTemplate.copyData(item2, ge);
                            ge.number = 1;
                            WindowInfoScr.this.listEp.addElement(ge);
                        }
                    }
                }));
                menu.addElement(addGemInfo(item2, ((this.selected % this.numW) * 20) + 4, 86));
            }
        } else if (this.selected > 5) {
            if (this.selected % 6 >= 0 && this.selected % 6 < this.listEp.size() && (g = (GemTemplate) this.listEp.elementAt(this.selected % 6)) != null) {
                menu.addElement(new Command("Lấy ra", new IAction() {
                    @Override
                    public void perform() {
                        g.number--;
                        if (g.number <= 0) {
                            WindowInfoScr.this.listEp.removeElement(g);
                        }
                        boolean aa = false;
                        int leght = WindowInfoScr.this.listTemp.size();
                        for (int i = 0; i < leght; i++) {
                            GemTemplate ge = (GemTemplate) WindowInfoScr.this.listTemp.elementAt(i);
                            if (ge.id == g.id) {
                                ge.number++;
                                aa = true;
                                break;
                            }
                        }
                        if (!aa) {
                            new GemTemplate(g.id).number = 1;
                            WindowInfoScr.this.listTemp.addElement(g);
                        }
                        WindowInfoScr.this.resetEpdothu(-1);
                    }
                }));
            }
        } else if (this.selected >= 0 && this.selected < this.itemEpdothu.length && (real = this.itemEpdothu[this.selected]) != null) {
            menu.addElement(new Command("Lấy ra", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.listItemAnimal.addElement(real);
                    WindowInfoScr.this.itemEpdothu[WindowInfoScr.this.selected] = null;
                    WindowInfoScr.this.resetEpdothu(-1);
                }
            }));
        }
        Canvas.menu.startAt(menu, 2);
    }

    int checkItem(boolean isItem) {
        int count = 0;
        if (isItem) {
            for (int i = 0; i < this.itemEpdothu.length; i++) {
                if (this.itemEpdothu[i] != null) {
                    count++;
                }
            }
        } else {
            int size0 = this.listEp.size();
            for (int i2 = 0; i2 < size0; i2++) {
                GemTemplate g = (GemTemplate) this.listEp.elementAt(i2);
                count += g.number;
            }
        }
        return count;
    }

    private void doLeftEpDothu() {
        this.left = new Command("Hợp", new IAction() {
            @Override
            public void perform() {
                if (WindowInfoScr.this.checkItem(true) < 5) {
                    Canvas.startOKDlg("Bạn phải sử dụng đủ 5 vật phẩm.");
                    return;
                }
                if (WindowInfoScr.this.checkItem(false) < WindowInfoScr.slNLThu) {
                    Canvas.startOKDlg(new StringBuffer("Nguyên liệu không đủ. Cần ").append((int) WindowInfoScr.slNLThu).append(" nguyên liệu.").toString());
                    return;
                }
                Vector data = new Vector();
                if (WindowInfoScr.this.itemEpdothu[2] != null) {
                    data.addElement(WindowInfoScr.this.itemEpdothu[2]);
                }
                for (int i = 0; i < WindowInfoScr.this.itemEpdothu.length; i++) {
                    if (i != 2 && WindowInfoScr.this.itemEpdothu[i] != null) {
                        data.addElement(WindowInfoScr.this.itemEpdothu[i]);
                    }
                }
                byte[] data1 = new byte[6];
                int size0 = WindowInfoScr.this.listEp.size();
                for (int j = 0; j < size0; j++) {
                    GemTemplate g = (GemTemplate) WindowInfoScr.this.listEp.elementAt(j);
                    GemTemp ge = Res.getGemByID(g.id);
                    String str = ge.name.substring(ge.name.length() - 1);
                    int ab = Integer.parseInt(str);
                    data1[ab - 1] = (byte) g.number;
                }
                GameService.gI().epDothu(data, data1);
                Canvas.startWaitDlg("Đang hợp..", false);
                for (int i2 = 0; i2 < WindowInfoScr.this.itemEpdothu.length; i2++) {
                    WindowInfoScr.this.itemEpdothu[i2] = null;
                }
                if (WindowInfoScr.this.listEp != null) {
                    WindowInfoScr.this.listEp.removeAllElements();
                }
                WindowInfoScr.this.right.action.perform();
            }
        });
    }

    public void paintListItemAnimal(Graphics g, int count) {
        for (int i = 0; i < this.listItemAnimal.size(); i++) {
            ItemInInventory item = (ItemInInventory) this.listItemAnimal.elementAt(i);
            g.drawImage(Res.imgGrid, (count * 20) + 4, 5 * this.hSmall, 0);
            item.paint(g, (count * 20) + 12, (this.hSmall * 5) + 8);
            count++;
        }
    }

    private void paintGemItemThu(Graphics g, int count, int y) {
        int leght = this.listTemp.size();
        for (int i = 0; i < leght; i++) {
            if (khoaDoThu == 1) {
                g.setColor(3276851);
                g.fillRect(count * 20, y, 17, 17);
            }
            if (this.numH != 1 && this.selected % this.numW == count && khoaDoThu == 1) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillRect((this.selected % this.numW) * 20, (5 * this.hSmall) + 1, 17, 16);
            }
            g.drawImage(Res.imgGrid, count * 20, y, 0);
            GemTemplate item = (GemTemplate) this.listTemp.elementAt(i);
            Res.paintGem(g, Res.getGemByID(item.id).idImage, (count * 20) + 10, y + 10);
            GemTemp ge = Res.getGemByID(item.id);
            String str = ge.name.substring(ge.name.length() - 1);
            try {
                int aa = Integer.parseInt(str);
                Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(aa)).toString(), count * 20, y - 2, 0);
            } catch (Exception e) {
            }
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(item.number)).toString(), (count * 20) + 14, y + 15, 1);
            count++;
        }
    }

    private void paintEpDothu(Graphics g) {
        g.setClip(0, 0, this.w - 4, this.h + 4);
        for (int i = 0; i < 5; i++) {
            g.drawImage(Res.imgGrid, this.posEpNgoc[i][0], this.posEpNgoc[i][1], 0);
        }
        g.setColor(game.pokemon.GiaoDien.VANG);
        if (this.numH == 1 && this.selected < 5) {
            g.fillRect(this.posEpNgoc[this.selected][0] + 1, this.posEpNgoc[this.selected][1] + 1, 16, 16);
        }
        for (int i2 = 0; i2 < this.itemEpdothu.length; i2++) {
            if (this.itemEpdothu[i2] != null) {
                paintItemIcon(g, this.itemEpdothu[i2], this.posEpNgoc[i2][0] + 10, this.posEpNgoc[i2][1] + 10);
            }
        }
        int cc = 3;
        if (idColor == 3) {
            cc = 2;
        } else if (idColor == 2) {
            cc = 1;
        }
        Font.normalFont[cc].drawString(g, new StringBuffer().append(this.mau[cc]).toString(), this.posEpNgoc[2][0] + 10, this.posEpNgoc[1][1], 2);
        Font.normalFont[0].drawString(g, new StringBuffer("Số Đá: ").append((int) slNLThu).toString(), this.posEpNgoc[2][0] + 10, 3 * this.hSmall, 2);
        if (this.numH == 1 && this.selected >= 6 && this.selected < 12) {
            g.fillRect(((this.selected % 6) * 20) + 5, (4 * this.hSmall) - 4, 16, 16);
        }
        for (int i3 = 0; i3 < 6; i3++) {
            g.drawImage(Res.imgGrid, (i3 * 20) + 4, (4 * this.hSmall) - 5, 0);
        }
        int leghtt = this.listEp.size();
        for (int i4 = 0; i4 < leghtt; i4++) {
            GemTemplate real = (GemTemplate) this.listEp.elementAt(i4);
            GemTemp ge = Res.getGemByID(real.id);
            String str = ge.name.substring(ge.name.length() - 1);
            int ab = Integer.parseInt(str);
            Res.paintGem(g, Res.getGemByID(real.id).idImage, ((i4 % 6) * 20) + (this.wSmall / 2) + 3, (4 * this.hSmall) + 3);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(ab)).toString(), ((i4 % 6) * 20) + 5, (4 * this.hSmall) - 7, 0);
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(real.number)).toString(), ((i4 % 6) * 20) + (this.wSmall / 2) + 10, (4 * this.hSmall) + 8, 1);
        }
        g.translate(-cmx, 0);
        if (this.numH != 1) {
            g.fillRect((this.selected % this.numW) * 20, (5 * this.hSmall) + 1, 17, 16);
        }
        int count = 0;
        for (int i5 = 0; i5 < this.listItemAnimal.size(); i5++) {
            ItemInInventory item = (ItemInInventory) this.listItemAnimal.elementAt(i5);
            g.drawImage(Res.imgGrid, count * 20, 5 * this.hSmall, 0);
            item.paint(g, (count * 20) + 8, (this.hSmall * 5) + 8);
            count++;
        }
        paintGemItemThu(g, count, 5 * this.hSmall);
        for (int count2 = count + this.listTemp.size(); count2 < 6; count2++) {
            g.drawImage(Res.imgGrid, count2 * 20, 5 * this.hSmall, 0);
        }
    }

    private void paintGrid(Graphics g, int x, int y) {
        g.setColor(6513507);
        g.drawRect(x, y, this.wSmall, this.hSmall);
        g.setColor(8684162);
        g.drawRect(x + 1, y + 1, this.wSmall - 2, this.hSmall - 2);
    }

    private void paintCheDo(Graphics g) {
        g.setClip(0, 0, this.w, this.h);
        for (int i = 0; i < this.listCheDo.size(); i++) {
            Mineral mi = (Mineral) this.listCheDo.elementAt(i);
            Font.normalFont[0].drawString(g, new StringBuffer(String.valueOf(mi.name)).append(": ").append((int) mi.number).append(" Cái.").toString(), 0, (i * 13) + 2, 0);
        }
        g.setColor(16766498);
        g.fillRect(0, 4 + (this.listCheDo.size() * 13), this.w, 2);
        g.fillRect(0, 4 + (this.hSmall * 3) + (this.listCheDo.size() * 13), this.w, 2);
        g.setColor(game.pokemon.GiaoDien.VANG);
        if (this.selected / this.numW < 2) {
            g.translate(-cmx, 0);
            g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, (this.listCheDo.size() * 13) + ((this.selected / this.numW) * this.wSmall) + 13, 17, 17);
        }
        int cou = 0;
        int wLishEp = this.listEp.size() / 2;
        if (wLishEp < 6) {
            wLishEp = 6;
        }
        for (int i2 = 0; i2 < this.listEp.size(); i2++) {
            GemTemplate gem = (GemTemplate) this.listEp.elementAt(i2);
            g.drawImage(Res.imgGrid, ((i2 % wLishEp) * this.wSmall) + 4, (this.listCheDo.size() * 13) + ((i2 / wLishEp) * this.wSmall) + 13, 0);
            Res.paintGem(g, Res.getGemByID(gem.id).idImage, ((i2 % wLishEp) * this.wSmall) + (this.wSmall / 2) + 4, (this.listCheDo.size() * 13) + 13 + ((i2 / wLishEp) * this.wSmall) + (this.wSmall / 2));
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(gem.number)).toString(), ((i2 % wLishEp) * this.wSmall) + (this.wSmall / 2) + 4, (this.listCheDo.size() * 13) + 13 + ((i2 / wLishEp) * this.wSmall) + (this.wSmall / 2), 1);
            cou++;
        }
        while (cou < 12) {
            g.drawImage(Res.imgGrid, ((cou % 6) * this.wSmall) + 4, (this.listCheDo.size() * 13) + ((cou / 6) * this.wSmall) + 13, 0);
            cou++;
        }
        if (this.selected / this.numW < 2) {
            g.translate(cmx, 0);
        }
        if (this.selected / this.numW >= 2) {
            g.translate(-cmx, 0);
            g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, this.hSmall * 5, 17, 17);
        }
        paintGemItem(g, 0, this.hSmall * 5);
        for (int count = 0 + this.listTemp.size(); count < 6; count++) {
            g.drawImage(Res.imgGrid, (count * 20) + 4, 5 * this.hSmall, 0);
        }
    }

    private void paintSelected(Graphics g, int x, int y) {
        g.setColor(game.pokemon.GiaoDien.VANG);
        g.fillRect(x, y, 16, 16);
    }

    private void paintKeepAndSellItem(Graphics g) {
        // pokemon: 2 lưới 3 cột - trái hành trang (xanh), phải rương / đồ bán (nâu); ô bo góc có khe,
        // ô trống mờ, ô đang chọn viền vàng dày
        Vector li;
        if (index[focusTab] == 22) {
            li = Char.itembag;
        } else {
            li = this.sellItem;
        }
        g.setClip(-2, -2, this.w + 4, this.h + 4);
        boolean benTrai = this.selected % this.numW < 3;
        int nTrai = Char.inventory.size() + MainChar.gemItem.size();
        int selX = (this.selected % this.numW) * 18;
        int selY = (this.selected / this.numW) * 18;
        int dyTrai = benTrai ? cmy : this.cmyKeep;
        g.translate(0, -dyTrai);
        for (int i = 0; i < this.numH; i++) {
            for (int j = 0; j < 3; j++) {
                game.pokemon.KieuO.oDo(g, j * 18, i * 18, 18, (i * 3) + j < nTrai, ((i + j) & 1) == 1);
            }
        }
        if (benTrai && !this.isChangeTab) {
            game.pokemon.KieuO.nenChon(g, selX, selY, 18);
        }
        for (int i2 = 0; i2 < Char.inventory.size(); i2++) {
            int xx = (i2 % 3) * 18;
            int yy = (i2 / 3) * 18;
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i2);
            if (item.isSelling) {
                game.pokemon.GiaoDien.khoi(g, xx + 1, yy + 1, 15, 15, 2, 0x3E8A30);
            }
            item.paint(g, xx + 9, yy + 9);
        }
        int aa = Char.inventory.size();
        for (int i3 = 0; i3 < MainChar.gemItem.size(); i3++) {
            int xx2 = (aa % 3) * 18;
            int yy2 = (aa / 3) * 18;
            GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i3);
            Res.paintGem(g, Res.getGemByID(gem.id).idImage, xx2 + 9, yy2 + 9);
            game.pokemon.KieuO.soLuong(g, xx2 + 17, yy2 + 17, game.pokemon.KieuO.soGon(gem.number));
            aa++;
        }
        if (benTrai && !this.isChangeTab) {
            game.pokemon.KieuO.chonO(g, selX, selY, 18);
        }
        g.translate(0, dyTrai);
        int dyPhai = benTrai ? this.cmyKeep : cmy;
        g.translate(0, -dyPhai);
        for (int i4 = 0; i4 < this.numH; i4++) {
            for (int j2 = 0; j2 < 3; j2++) {
                game.pokemon.KieuO.oRuong(g, (j2 * 18) + 54, i4 * 18, 18, (i4 * 3) + j2 < li.size(), ((i4 + j2) & 1) == 1);
            }
        }
        if (!benTrai && !this.isChangeTab) {
            game.pokemon.KieuO.nenChon(g, selX, selY, 18);
        }
        for (int i5 = 0; i5 < li.size(); i5++) {
            if (li.elementAt(i5) instanceof ItemInInventory) {
                ((ItemInInventory) li.elementAt(i5)).paint(g, ((i5 % 3) * 18) + 54 + 9, ((i5 / 3) * 18) + 9);
            } else {
                Res.paintGem(g, Res.getGemByID(((RealID) li.elementAt(i5)).ID).idImage, ((i5 % 3) * 18) + 54 + 9, ((i5 / 3) * 18) + 9);
            }
        }
        if (!benTrai && !this.isChangeTab) {
            game.pokemon.KieuO.chonO(g, selX, selY, 18);
        }
        g.translate(0, dyPhai);
    }

    private void paintEpNgoc(Graphics g) {
        g.setClip(4, 0, this.w - 8, this.h);
        for (int i = 0; i < 5; i++) {
            g.drawImage(Res.imgGrid, this.posEpNgoc[i][0], this.posEpNgoc[i][1], 0);
        }
        g.setColor(game.pokemon.GiaoDien.VANG);
        if (this.numH == 1 && this.selected < 5) {
            g.fillRect(this.posEpNgoc[this.selected][0] + 1, this.posEpNgoc[this.selected][1] + 1, 16, 16);
        }
        for (int i2 = 0; i2 < this.listEp.size(); i2++) {
            RealID real = (RealID) this.listEp.elementAt(i2);
            Res.paintGem(g, Res.getGemByID(real.ID).idImage, this.posEpNgoc[i2][0] + 10, this.posEpNgoc[i2][1] + 10);
        }
        g.translate(-cmx, 0);
        if (this.numH != 1) {
            g.fillRect(((this.selected % this.numW) * 20) + 4, (5 * this.hSmall) + 1, 16, 16);
        }
        paintGemItem(g, 0, this.hSmall * 5);
        for (int count = 0 + this.listTemp.size(); count < 6; count++) {
            g.drawImage(Res.imgGrid, (count * 20) + 4, 5 * this.hSmall, 0);
        }
    }

    private void paintGemItem(Graphics g, int count, int y) {
        for (int i = 0; i < this.listTemp.size(); i++) {
            if ((khoaCheDo == 1 && index[focusTab] == 28) || ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25))) {
                g.setColor(3276851);
                g.fillRect((count * 20) + 4, y, 17, 17);
            }
            if (khoaCheDo == 1 && this.selected / this.numW >= 2 && this.selected % this.numW == count) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, this.hSmall * 5, 17, 17);
            }
            if (this.selected / this.numW == 4 && khoaImbue == 1 && index[focusTab] == 21 && this.selected % this.numW == count) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, (4 * this.hSmall) + 1, 17, 17);
            }
            if (this.selected / this.numW == 4 && khoaKham == 1 && index[focusTab] == 25 && this.selected % this.numW == count) {
                g.setColor(game.pokemon.GiaoDien.VANG);
                g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, (4 * this.hSmall) + 1, 17, 17);
            }
            g.drawImage(Res.imgGrid, (count * 20) + 4, y, 0);
            GemTemplate item = (GemTemplate) this.listTemp.elementAt(i);
            Res.paintGem(g, Res.getGemByID(item.id).idImage, (count * 20) + 10 + 4, y + 8);
            if (index[focusTab] != 21) {
                GemTemp ge = Res.getGemByID(item.id);
                String str = ge.name.substring(ge.name.length() - 1);
                try {
                    int aa = Integer.parseInt(str);
                    Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(aa)).toString(), (count * 20) + 4, y, 0);
                } catch (Exception e) {
                }
            }
            Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(item.number)).toString(), (count * 20) + 16 + 4, y + 10, 1);
            count++;
        }
    }

    private void paintImbue(Graphics g) {
        paintDapBG(g, 0, 0);
        g.setClip(4, 0, this.w - 8, this.h);
        if (this.imgWeaponAvatar != null) {
            g.drawImage(this.imgWeaponAvatar, 63, 43, 3);
        }
        for (int i = 0; i < MainChar.gemitemImbue.size(); i++) {
            RealID item = (RealID) MainChar.gemitemImbue.elementAt(i);
            Res.paintGem(g, Res.getGemByID(item.ID).idImage, ((i % 2) * 100) + 10 + 3, ((i / 2) * this.hSmall) + (this.hSmall / 2));
        }
        g.translate(-cmx, 0);
        if (this.selected / this.numW == 4) {
            g.fillRect(((this.selected % this.numW) * this.wSmall) + 4, (4 * this.hSmall) + 1, 17, 17);
        }
        int count = 0;
        for (int i2 = 0; i2 < Char.inventory.size(); i2++) {
            g.drawImage(Res.imgGrid, (count * this.wSmall) + 4, 4 * this.hSmall, 0);
            ItemInInventory item2 = (ItemInInventory) Char.inventory.elementAt(i2);
            item2.paint(g, (count * 20) + 10 + 4, (4 * this.hSmall) + 8);
            count++;
        }
        paintGemItem(g, count, 4 * this.hSmall);
        for (int count2 = count + this.listTemp.size(); count2 <= 6; count2++) {
            g.drawImage(Res.imgGrid, (count2 * 20) + 4, 4 * this.hSmall, 0);
        }
    }

    private void paintDapBG(Graphics g, int x, int y) {
        if (!this.isChangeTab && Canvas.gameTick % 10 > 3 && this.selected / this.numW < 4) {
            g.setColor(game.pokemon.GiaoDien.VANG);
            if (this.selected % this.numW == 1 && this.selected / this.numW != 4) {
                g.fillRect(25, 0, 75, 86);
            } else {
                g.fillRect(((this.selected % this.numW) * 50) + 4, ((this.selected / this.numW) * this.hSmall) + 1, 17, 17);
            }
        }
        for (int i = 0; i < 8; i++) {
            g.drawImage(Res.imgGrid, ((i / 4) * 5 * this.wSmall) + 4, (i % 4) * this.hSmall, 0);
        }
        g.setColor(game.pokemon.GiaoDien.VANG);
        g.drawRect(25, 0, 75, 86);
    }

    private void paintGrid(Graphics g) {
        // pokemon: lưới trao đổi - trái của mình (xanh), phải của đối phương (nâu)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                if (j > 2) {
                    game.pokemon.KieuO.oRuong(g, j * 18, i * 18, 18, true, ((i + j) & 1) == 1);
                } else {
                    game.pokemon.KieuO.oDo(g, j * 18, i * 18, 18, true, ((i + j) & 1) == 1);
                }
            }
        }
    }

    private void paintTrade(Graphics g) {
        g.setClip(0, 0, this.w, this.h + 18);
        paintGrid(g);
        g.translate(-cmx, 0);
        if (!this.isChangeTab) {
            game.pokemon.KieuO.nenChon(g, (this.selected % this.numW) * 18, (this.selected / this.numW) * 18, 18);
            game.pokemon.KieuO.chonO(g, (this.selected % this.numW) * 18, (this.selected / this.numW) * 18, 18);
        }
        int xx = 0;
        for (int i = 0; i < Char.inventory.size(); i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
            g.drawImage(Res.imgGrid, xx, 90, 0);
            if (item.isSelling) {
                g.setColor(7706352);
                g.fillRect(xx + 2, 92, 14, 14);
            }
            GameData.paintIcon(g, it.idIcon, 9 + xx, 99);
            xx += 18;
        }
        for (int i2 = 0; i2 < MainChar.listPotion.length; i2++) {
            if (MainChar.listPotion[i2].number - MainChar.listPotion[i2].numTrade > 0 && MainChar.listPotion[i2].isTrade) {
                g.drawImage(Res.imgGrid, xx, 90, 0);
                GameData.paintIcon(g, (short) (MainChar.listPotion[i2].index + 5500), xx + 9, 98);
                Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(MainChar.listPotion[i2].number - MainChar.listPotion[i2].numTrade)).toString(), xx + 9, 99, 1);
                xx += 18;
            }
        }
        if (xx / 18 < 6) {
            for (int i3 = 0; i3 < 6 - (xx / 18); i3++) {
                g.drawImage(Res.imgGrid, xx + (i3 * 18), 90, 0);
            }
        }
        g.translate(cmx, 0);
        Vector tItemTrade = Canvas.gameScr.mainChar.tItems;
        Vector rItemTrade = Canvas.gameScr.mainChar.rItems;
        int size3 = tItemTrade.size();
        for (int i4 = 0; i4 < size3; i4++) {
            paintItemTrade(g, tItemTrade.elementAt(i4), (i4 % 3) * 18, (i4 / 3) * 18);
        }
        int size2 = rItemTrade.size();
        for (int i5 = 0; i5 < size2; i5++) {
            paintItemTrade(g, rItemTrade.elementAt(i5), 54 + ((i5 % 3) * 18), (i5 / 3) * 18);
        }
    }

    private void paintItemTrade(Graphics g, Object item, int x, int y) {
        if (item instanceof ItemInInventory) {
            ((ItemInInventory) item).paint(g, x + 9, y + 9);
            return;
        }
        Potion p = (Potion) item;
        GameData.paintIcon(g, (short) (p.index + 5500), x + 9, y + 9);
        Font.smallFontColor[3].drawString(g, new StringBuffer(String.valueOf(p.number)).toString(), x + 17, y + 9, 1);
    }

    private void paintNewSkillLearn(Graphics g) {
        String[] nameChar = {"Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu"};
        Font.normalFont[0].drawString(g, nameChar[Canvas.gameScr.mainChar.clazz], 60, -14, 2);
        g.setColor(11908533);
        g.fillRect(-8, 0, 133, 1);
        g.fillRect(-8, 30, 133, 1);
        g.setClip(0, 0, this.w, this.h);
        g.translate(-cmx, 0);
        for (int skillType = 0; skillType < this.numW; skillType++) {
            InfoSkillLearn skillearn = (InfoSkillLearn) ((Vector) GameScr.infoNewSkill.elementAt(Canvas.gameScr.mainChar.clazz)).elementAt(skillType);
            game.pokemon.BangChieu.icon(g, ((skillType * 23) + (this.wSmall / 2)) - 10, (this.h / 2) - 10, 20, skillearn.idSkill);
        }
        if (!this.isChangeTab && Canvas.gameTick % 10 > 3) {
            g.setColor(14139920);
            g.drawRect(this.selected * this.wSmall, 5, this.wSmall - 2, 20);
        }
        if (!this.isChangeTab) {
            g.translate(cmx, 0);
            g.setClip(-10, 28, 133, 72);
            g.translate(0, -cmy);
            if (this.newSkill != null) {
                for (int i = 0; i < this.newSkill.size(); i++) {
                    String str = (String) this.newSkill.elementAt(i);
                    if (str.substring(0, 1).equals("`")) {
                        Font.normalFont[0].drawString(g, str.substring(1, str.length()), -5, 30 + (i * 12), 0);
                    } else {
                        Font.arialFontW.drawString(g, str, -5, 30 + (i * 12), 0);
                    }
                }
            }
        }
    }

    private void paintKhung(Graphics g) {
        g.setColor(11908533);
        g.drawRect(0, 0, this.w, this.h + 1);
        g.setClip(0, 0, this.w, this.h + 1);
        g.translate(0, 1 - cmy);
    }

    private void paintQuestClan(Graphics g) {
        paintKhung(g);
        for (int i = 0; i < this.questInfo.size(); i++) {
            String str = (String) this.questInfo.elementAt(i);
            if (str.length() >= 2 && str.substring(0, 1).equals("`")) {
                Font.normalFont[0].drawString(g, str.substring(1, str.length()), 4, i * 15, 0);
            } else {
                Font.arialFontW.drawString(g, str, 4, i * 15, 0);
            }
        }
    }

    public static String getInfoQuestPaint(int type) {
        String info = "";
        if (type == 0 && GameScr.mainQuest != null) {
            try {
                info = GameScr.mainQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
            } catch (Exception e) {
            }
        }
        if (GameScr.subQuest != null && type == 1) {
            try {
                info = GameScr.subQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
            } catch (Exception e) {
            }
        }
        if (GameScr.clanQuest != null && type == 2) {
            try {
                info = GameScr.clanQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return info;
    }

    /**
     * 31/10/2026 (báo lỗi J2ME "sổ nhiệm vụ trống"): dòng của tab Nhiệm vụ cũ - mỗi nhiệm vụ đang làm / đã xong = tên (đầu
     * ký tự 1 = chữ đậm) + các dòng mục tiêu / thưởng, tự xuống dòng theo bề ngang wc (trước cắt ngang mép khung:
     * "Cửa ải: 60 Golem ở Golde"). Nhiệm vụ không có chữ vẫn có dòng tên. Cùng danh sách cho vẽ và cho giới hạn cuộn.
     */
    static Vector dongNhiemVu(int wc) {
        Vector v = new Vector();
        game.model.Quest[] qs = {GameScr.mainQuest, GameScr.subQuest, GameScr.clanQuest};
        for (int t = 0; t < 3; t++) {
            game.model.Quest q = qs[t];
            if (q == null || q.stateQuest != 1 && q.stateQuest != 2) {
                continue;
            }
            String[] ten = Font.normalFont[0].splitFontBStrInLine(q.name, wc);
            for (int i = 0; i < ten.length; i++) {
                v.addElement("\u0001" + ten[i]);
            }
            String info = getInfoQuestPaint(t);
            String[] data = info.equals("") ? new String[0] : Util.split(info, "|");
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null || data[i].length() == 0) {
                    continue;
                }
                String[] d = Font.arialFontW.splitFontBStrInLine(data[i], wc);
                for (int k = 0; k < d.length; k++) {
                    v.addElement(d[k]);
                }
            }
        }
        return v;
    }

    static final int RONG_CHU_NV = 124;

    private void paintQuest(Graphics g) {
        paintKhung(g);
        Vector v = dongNhiemVu(RONG_CHU_NV);
        if (v.size() == 0) {
            Font.arialFontW.drawString(g, "Chưa nhận nhiệm vụ", 8, 1, 0);
            return;
        }
        for (int i = 0; i < v.size(); i++) {
            String d = (String) v.elementAt(i);
            if (d.length() > 0 && d.charAt(0) == '\u0001') {
                Font.normalFont[0].drawString(g, d.substring(1), 4, i * 15, 0);
            } else {
                Font.arialFontW.drawString(g, d, 4, i * 15, 0);
            }
        }
    }

    private void paintParty(Graphics g) {
        paintKhung(g);
        int size = Char.party.size();
        if (size > 0) {
            if (!this.isChangeTab) {
                g.setColor(6448384);
                g.fillRect(2, this.selected * 32, 131, 32);
            }
            for (int i = 0; i < size; i++) {
                PartyInfo ch = (PartyInfo) Char.party.elementAt(i);
                Font.arialFontW.drawString(g, new StringBuffer("Pokémon: ").append(ch.name).toString(), 8, i * 33, 0);
                Font.arialFontW.drawString(g, new StringBuffer("Cấp: ").append(ch.lv).toString(), 8, ((i * 2) + 1) * 16, 0);
                g.setColor(16777215);
                g.fillRect(2, (((i * 2) + 1) * 16) + 15, 131, 1);
            }
            return;
        }
        Font.arialFontW.drawString(g, "Chưa có nhóm", 8, 5, 0);
    }

    private void paintCharInfo(Graphics g) {
        paintKhung(g);
        // pokemon: chữ thông tin không đè hàng tiền ở đáy khung (cuộn để xem tiếp)
        if (this.isPaintMoney) {
            g.clipRect(0, cmy - 1, this.w, this.h - 12);
        }
        // pokemon: trang tóm tắt kiểu Pokémon - thẻ đầu (huy hiệu, tên, Lv, hệ, thanh HP/PP) + bảng chỉ số
        MainChar c = Canvas.gameScr.mainChar;
        int w = this.w;
        game.pokemon.GiaoDien.khoi(g, 1, 1, w - 2, 42, 4, game.pokemon.GiaoDien.XANH);
        game.pokemon.GiaoDien.khoiDoc(g, 2, 2, w - 4, 40, 3, game.pokemon.GiaoDien.TRANG, game.pokemon.GiaoDien.HANG_CHAN);
        int dex = c.pokeDex;
        if (dex <= 0 || !game.pokemon.PokeBody.paintChanDung(g, dex, c.pokeShiny, 19, 22, 32)) {
            game.pokemon.GiaoDien.pokeBall(g, 19, 22, 12, 0);
        }
        String lv = new StringBuffer("Lv").append((int) c.level).toString();
        int wLv = Font.normalFont[0].getWidth(lv);
        Font.normalFont[0].drawString(g, game.pokemon.GiaoDien.catChu(Font.normalFont[0], c.name, w - 44 - wLv - 6), 38, 3, 0);
        Font.normalFont[0].drawString(g, lv, w - 5, 3, 1);
        int he = dex > 0 ? game.pokemon.HuyHieuPoke.he(dex) : 0;
        game.pokemon.KieuO.vienHe(g, 38, 16, he);
        String loai = dex > 0 ? game.pokemon.LoaiPokemonClient.ten(dex) : "";
        if (loai.length() > 0) {
            Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, loai, w - 44 - game.pokemon.KieuO.rongVienHe(he)), 41 + game.pokemon.KieuO.rongVienHe(he), 17, 0);
        }
        game.pokemon.GiaoDien.thanhHP(g, 38, 33, w - 44, 5, c.hp, c.maxhp);
        game.pokemon.GiaoDien.thanh(g, 49, 38, w - 55, 3, c.mp, c.maxmp, 1);
        int y = 46;
        if (this.charInfo != null) {
            for (int i = 4; i < this.charInfo.length; i++) {
                y = hangChiSo(g, this.charInfo[i], y, w, i);
            }
        }
        if (infoMainCharServer != null) {
            for (int i2 = 0; i2 < infoMainCharServer.length; i2++) {
                y = hangChiSo(g, infoMainCharServer[i2], y, w, i2 + 1);
            }
        }
    }

    /** Hàng chỉ số "Nhãn: giá trị": nhãn trái, giá trị phải, nền xen kẽ; trả y hàng sau. */
    private int hangChiSo(Graphics g, String dong, int y, int w, int i) {
        if (dong == null) {
            return y;
        }
        game.pokemon.GiaoDien.khoi(g, 1, y, w - 2, 12, 2, (i & 1) == 0 ? game.pokemon.GiaoDien.HANG_CHAN : game.pokemon.GiaoDien.HANG_LE);
        int p = dong.indexOf(": ");
        if (p > 0) {
            String gt = dong.substring(p + 2);
            int wGt = Font.arialFontW.getWidth(gt);
            Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, dong.substring(0, p), w - wGt - 12), 4, y, 0);
            Font.arialFontW.drawString(g, gt, w - 4, y, 1);
        } else {
            Font.arialFontW.drawString(g, game.pokemon.GiaoDien.catChu(Font.arialFontW, dong, w - 8), 4, y, 0);
        }
        return y + 13;
    }

    private void paintSkill(Graphics g) {
        // pokemon: hàng ô chiêu màu hệ + thẻ chiêu (game.pokemon.BangChieu)
        if (this.selected < 0) {
            this.selected = 0;
        } else if (this.selected > this.numW - 1) {
            this.selected = this.numW - 1;
        }
        game.pokemon.BangChieu.ve(g, this.numW, this.selected, this.isChangeTab, cmx, this.isCmyTab3 ? 0 : cmy, this.w);
    }

    private void paintFeatures(Graphics g) {
        // pokemon: hàng chỉ số bo góc + biểu tượng màu hệ (game.pokemon.BangTiemNang) thay ảnh inv_1
        game.pokemon.BangTiemNang.ve(g, Canvas.gameScr.mainChar, this.selected, this.isChangeTab, this.w);
    }

    private void paintCharWearing(Graphics g) {
        // pokemon: the trang bi - Pokemon xoay 4 huong voi phu kien + dai tom tat (bac, +N, bo, trang phuc)
        if (charWearing.pokeDex > 0 && charWearing.ngoaiHinh != null) {
            charWearing.paintPokemonTinh(g, 58, 90, game.pokemon.NgoaiHinhVe.huongThe(Canvas.gameTick));
            game.pokemon.NgoaiHinhVe.veThe(g, charWearing.ngoaiHinh, this.w / 2, 1);
        } else {
            charWearing.paintAvatar(g, (short) 58, (short) 90);
        }
        if (this.imgCharWear != null && Canvas.gameTick % 16 >= 8) {
            g.drawImage(this.imgCharWear, this.w / 2, 0, 17);
        }
        // pokemon: ô trang bị bo góc + tên ô (game.pokemon.BangTrangBi) thay ảnh khung kiếm hiệp
        game.pokemon.BangTrangBi.ve(g, charWearing, this.selected, this.isChangeTab, posPaintWearingX, posPaintWearingY, this.w);
    }

    private void paintAnimalWearing(Graphics g) {
        ItemTemplate it;
        if (charWearing != null && charWearing.imgAnimal != null && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
            g.drawRegion(charWearing.imgAnimal, 0, charWearing.himg * this.idFrame, charWearing.wimg, charWearing.himg, 0, Res.imgInfoWindow[0].getWidth() / 2, Res.imgInfoWindow[0].getHeight() / 2, 3);
        }
        // pokemon: 10 ô đồ thú cưỡi bo góc, ô chọn viền vàng
        for (int i = 0; i < 10; i++) {
            game.pokemon.KieuO.oDo(g, 2 + ((i % 2) * this.wSmall), 2 + ((i / 2) * 21), 18, false, false);
        }
        if (!this.isChangeTab) {
            int xc = 2 + ((this.selected % this.numW) * this.wSmall);
            int yc = 2 + ((this.selected / this.numW) * 21);
            game.pokemon.KieuO.nenChon(g, xc, yc, 18);
            game.pokemon.KieuO.chonO(g, xc, yc, 18);
        }
        if (charWearing != null) {
            if (charWearing.wearingAnimal != null && (charWearing.useHorse != -1 || charWearing.idhorse > -1)) {
                int size0 = charWearing.wearingAnimal.size();
                for (int i2 = 0; i2 < size0; i2++) {
                    ItemInInventory item = (ItemInInventory) charWearing.wearingAnimal.elementAt(i2);
                    if (item != null && (it = Res.getItem(item.charClazz, item.idTemplate)) != null) {
                        paintItemIcon(g, item, posPaintWearingX[it.type], posPaintWearingY[it.type]);
                    }
                }
            }
            if (charWearing.idAnimal > -1 && charWearing != null) {
                if (charWearing.useHorse != -1 || charWearing.idhorse > -1) {
                    GameData.paintIcon(g, (short) (charWearing.idPaintIconAnimal + 7500), 82, 92);
                }
            }
        }
    }

    private void paintItemIcon(Graphics g, ItemInInventory item, int x, int y) {
        ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
        GameData.paintIcon(g, it.idIcon, x, y);
    }

    private void paintShowText(Graphics g) {
        // 0.9.6.1: thẻ chi tiết chung (tên ngắt cả cụm + biểu tượng, thân đo theo phông thật, giá có biểu tượng tiền,
        // nút ở hàng chân TRONG thẻ - không còn phím mềm đáy màn đè lên chữ)
        int[] r = game.pokemon.HopChiTiet.ve(g, this.textShow, this.arrayKhamNgoc, this.iconShow, this.left, this.center, this.right, cmyText);
        this.xShow = r[0];
        this.yShow = r[1];
        this.wShow = r[2];
        this.hShow = r[3];
        this.countPop = 0;
        cmyLimText = r[4];
        if (cmtoYText > cmyLimText) {
            cmtoYText = cmyLimText;
        }
    }

    /** Chuỗi chi tiết đang hiện (thô, có tiền tố màu) + biểu tượng ô đang chọn; phím mềm trước khi mở thẻ. */
    private String textShow;
    private Command iconShow;
    private boolean anNutCu;

    /** Lệnh ô đang chọn (có paint vẽ biểu tượng) - biểu tượng đầu thẻ chi tiết; null = không. */
    private Command iconChon() {
        try {
            int e = this.selected + (this.countTabMyitem * 42);
            if (e >= 0 && e < this.list.size()) {
                Object o = this.list.elementAt(e);
                if (game.pokemon.HopChiTiet.coHinh(o)) {
                    return (Command) o;
                }
            }
        } catch (Throwable t) {
            // không có biểu tượng
        }
        return null;
    }

    public static boolean isDegit(char c) {
        return c >= '0' && c <= '9';
    }

    private void paintBag(Graphics g) {
        Command cmd;
        try {
            int xx = (cmy / this.hSmall) * this.numW;
            if (xx < 0) {
                xx = 0;
            }
            int yy = xx + (this.numW * this.numH);
            if (yy > 42) {
                yy = 42;
            }
            if ((this.countTabMyitem * 42) + 42 > this.maxSize) {
                yy = this.excess;
            }
            for (int i = xx; i < yy && !this.isLoad; i++) {
                int element = i + (this.countTabMyitem * 42);
                if (element < this.list.size() && (cmd = (Command) this.list.elementAt(element)) != null) {
                    cmd.paint(g, ((i % this.numW) * this.wSmall) + (this.wSmall / 2), ((i / this.numW) * this.hSmall) + (this.hSmall / 2));
                }
            }
        } catch (Exception e) {
            Cout.println("ERROR hanh trang ");
        }
    }

    private void paintProduct(Graphics g) {
        try {
            int xx = (cmy / this.hSmall) * this.numW;
            if (xx < 0) {
                xx = 0;
            }
            int yy = xx + (this.numW * this.numH);
            if (yy > this.list.size()) {
                yy = this.list.size();
            }
            for (int i = xx; i < yy && !this.isLoad; i++) {
                Command cmd = (Command) this.list.elementAt(i);
                cmd.paint(g, ((i % this.numW) * this.wSmall) + (this.wSmall / 2), ((i / this.numW) * this.hSmall) + (this.hSmall / 2));
            }
        } catch (Exception e) {
            Cout.println("ERROR paintProduct");
        }
    }

    private void getShopTemplate() {
        this.list.removeAllElements();
        int size0 = Res.shopTemplate.size();
        for (int i = 0; i < size0; i++) {
            final GemTemp shop = (GemTemp) Res.shopTemplate.elementAt(i);
            final int ii = i;
            if (shop.shopType == focusTab && shop.isSell) {
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        GemTemp g = Res.getShopByID(shop.id);
                        String itemInfo = ItemInInventory.setTemp(g.name, "0");
                        WindowInfoScr.this.setShowText(new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(g.decript, "0")).toString())).append(ItemInInventory.setTemp(new StringBuffer("Giá mua: ").append(game.pokemon.TienTe.chu(game.pokemon.TienShop.loai(g), g.price)).toString(), "0")).toString(), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        GameData.paintIcon(g, (short) (Res.getShopByID(shop.id).idImage + 5500), x, y);
                        game.pokemon.KieuO.soLuong(g, x + 8, y + 9, game.pokemon.KieuO.soGon((int) (shop.id > 1 ? Res.getShopByID(shop.id).value : (short) 1)));
                    }
                });
            }
        }
    }

    public void closeText() {
        if (this.isShowText) {
            this.anNutLenh = this.anNutCu;
        }
        this.isShowText = false;
        this.arrayKhamNgoc = null;
    }

    public void setShowText(String text, int x1, int y1) {
        if (!this.isShowText) {
            this.isShowText = true;
            String text2 = text.replace((char) 194, (char) 226);
            this.textShow = text2;
            this.iconShow = iconChon();
            this.anNutCu = this.anNutLenh;
            this.anNutLenh = true;
            this.wShow = 125;
            this.xShow = (((x1 + this.x) + this.x0) - (this.wShow / 2)) + 9;
            this.yShow = y1 + this.y + this.y0 + 18;
            this.showText = Font.borderFont.splitFontBStrInLine(text2, this.wShow - 10);
            this.hShow = (this.showText.length * 15) + 8 + (this.arrayKhamNgoc != null ? 16 : 0);
            this.xShow -= cmx;
            // pokemon: giữ cả viền hộp (3 px) trong màn hình
            if (this.xShow + this.wShow + 4 > Canvas.w) {
                this.xShow = (Canvas.w - this.wShow) - 4;
            }
            if (this.xShow < 4) {
                this.xShow = 4;
            }
            this.yShow -= cmy;
            if (this.countPop == 0) {
                this.countPop = -(this.showText.length - 1);
            }
            cmtoYText = 0;
            cmyText = 0;
            cmyLimText = 0;
            if (this.hShow > 100) {
                cmyLimText = this.hShow - 100;
                if (cmyLimText < 0) {
                    cmyLimText = 0;
                }
                this.hShow = 100;
            }
            if (this.yShow + this.hShow > Canvas.h - 20) {
                this.yShow = (Canvas.h - 20) - this.hShow;
            }
            if (this.yShow < 0) {
                this.yShow = 10;
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
                if (WindowInfoScr.this.selected >= Res.shopTemplate.size()) {
                    return;
                }
                if (Canvas.gameScr.mainChar.isFullInventory()) {
                    Canvas.startOKDlg("Hành trang đã đầy.");
                    return;
                }
                int aa = 0;
                int size0 = Res.shopTemplate.size();
                for (int i = 0; i < size0; i++) {
                    GemTemp gem = (GemTemp) Res.shopTemplate.elementAt(i);
                    if (gem.shopType == WindowInfoScr.focusTab && gem.isSell) {
                        if (aa == WindowInfoScr.this.selected) {
                            final GemTemp item = (GemTemp) Res.shopTemplate.elementAt(i);
                            GemTemp tg = Res.getShopByID(item.id);
                            String hoi = tg == null ? "Bạn có muốn mua vật phẩm này không ?" : new StringBuffer("Mua ").append(tg.name).append("? Giá: ").append(game.pokemon.TienTe.chu(game.pokemon.TienShop.loai(tg), tg.price)).toString();
                            Canvas.startYesNoDlg(hoi, new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().buyItemShopSpecial(item.id);
                                    Canvas.startWaitDlg();
                                }
                            });
                            return;
                        }
                        aa++;
                    }
                }
            }
        });
    }

    public int getCountPotion(int i) {
        return Canvas.gameScr.mainChar.potions[i];
    }

    public Vector getInventori() {
        Vector menu = new Vector();
        int count = 0;
        int size0 = MainChar.listPotion.length;
        for (int i = 1; i < size0; i++) {
            final int aa = i;
            if (getCountPotion(i) > 0) {
                final int ii = count;
                menu.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.setShowText(WindowInfoScr.this.getPotionInfo(aa), (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        Res.paintPotion(g, MainChar.listPotion[aa].index, x, y, 3);
                        game.pokemon.KieuO.soLuong(g, x + 8, y + 9, game.pokemon.KieuO.soGon(WindowInfoScr.this.getCountPotion(aa)));
                    }
                });
                count++;
            }
        }
        int size1 = MainChar.shopItem.size();
        for (int i2 = 0; i2 < size1; i2++) {
            final GemTemplate item = (GemTemplate) MainChar.shopItem.elementAt(i2);
            final int aa2 = count;
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.showShopItemInfo(item.id, aa2);
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    Res.paintPotion(g, Res.getShopByID(item.id).idImage, x, y, 3);
                    game.pokemon.KieuO.soLuong(g, x + 8, y + 9, game.pokemon.KieuO.soGon(item.number));
                }
            });
            count++;
        }
        int size2 = MainChar.gemItemKhoa.size();
        for (int i3 = 0; i3 < size2; i3++) {
            final int aa3 = count;
            final GemTemplate item2 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i3);
            final GemTemp ge = Res.getGemByID(item2.id);
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.showGemItemKhoaInfo(item2.id, (aa3 % WindowInfoScr.this.numW) * 18, (aa3 / WindowInfoScr.this.numW) * 18);
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    if (Res.getGemByID(item2.id) != null) {
                        if (aa3 - (WindowInfoScr.index[WindowInfoScr.focusTab] == 0 ? WindowInfoScr.this.countTabMyitem * 42 : 0) != WindowInfoScr.this.selected) {
                            g.setColor(3276851);
                            g.fillRect(x - 7, y - 7, 16, 16);
                        }
                        Res.paintGemKhoa(g, Res.getGemByID(item2.id).idImage, x, y);
                        if (ge.isEff != -1) {
                            Res.paintRectColor(g, x - 9, y - 9, 17, 17, ge.count, GemTemp.color[ge.isEff], 16516369);
                            ge.count += 3;
                            if (ge.count > 68) {
                                ge.count = 0;
                            }
                        }
                        game.pokemon.KieuO.soLuong(g, x + 8, y + 9, game.pokemon.KieuO.soGon(item2.number));
                    }
                }
            });
            count++;
        }
        int size3 = MainChar.gemItem.size();
        for (int i4 = 0; i4 < size3; i4++) {
            final int aa4 = count;
            final GemTemplate item3 = (GemTemplate) MainChar.gemItem.elementAt(i4);
            final GemTemp ge2 = Res.getGemByID(item3.id);
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.showGemItemInfo(item3.id, (aa4 % WindowInfoScr.this.numW) * 18, (aa4 / WindowInfoScr.this.numW) * 18);
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    if (Res.getGemByID(item3.id) != null) {
                        Res.paintGem(g, Res.getGemByID(item3.id).idImage, x, y);
                        if (ge2.isEff != -1) {
                            Res.paintRectColor(g, x - 9, y - 9, 17, 17, ge2.count, GemTemp.color[ge2.isEff], 16516369);
                            ge2.count += 3;
                            if (ge2.count > 68) {
                                ge2.count = 0;
                            }
                        }
                        game.pokemon.KieuO.soLuong(g, x + 8, y + 9, game.pokemon.KieuO.soGon(item3.number));
                    }
                }
            });
            count++;
        }
        int size4 = Char.animal.size();
        for (int i5 = 0; i5 < size4; i5++) {
            final int ii2 = count;
            final AnimalInfo item4 = (AnimalInfo) Char.animal.elementAt(i5);
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    if (!item4.info.equals("")) {
                        if (item4.typeAnimal == 0) {
                            WindowInfoScr.this.setShowText(new StringBuffer(String.valueOf(ItemInInventory.setTemp(new StringBuffer(String.valueOf(item4.name)).append(" lv").append((int) item4.level).toString(), "0"))).append(ItemInInventory.setTemp(item4.info, "0")).toString(), (ii2 % WindowInfoScr.this.numW) * 18, (ii2 / WindowInfoScr.this.numW) * 18);
                        } else {
                            long time = ((long) item4.totalTimeCon) - ((System.currentTimeMillis() - item4.timeStart) / 60000);
                            WindowInfoScr.this.setShowText(new StringBuffer(String.valueOf(ItemInInventory.setTemp(item4.name, "0"))).append(ItemInInventory.setTemp(item4.info, "0")).append(ItemInInventory.setTemp(new StringBuffer("Còn lại: ").append(time).append(" phút").toString(), "0")).toString(), (ii2 % WindowInfoScr.this.numW) * 18, (ii2 / WindowInfoScr.this.numW) * 18);
                        }
                    }
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    item4.paint(g, x, y);
                }
            });
            count++;
        }
        int size5 = Char.inventory.size();
        for (int i6 = 0; i6 < size5; i6++) {
            final int ii3 = count;
            final ItemInInventory item5 = (ItemInInventory) Char.inventory.elementAt(i6);
            menu.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.showItemInventoryInfo(item5, WindowInfoScr.this.isSell, (ii3 % WindowInfoScr.this.numW) * 18, (ii3 / WindowInfoScr.this.numW) * 18);
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    if (item5.isSelling) {
                        game.pokemon.GiaoDien.khoi(g, x - 8, y - 8, 16, 16, 2, 0x3E8A30);
                    }
                    item5.paint(g, x, y);
                }
            });
            count++;
        }
        if (MainChar.itemQuest != null) {
            int size6 = MainChar.itemQuest.length;
            for (int i7 = 0; i7 < size6; i7++) {
                final int aa5 = i7;
                if (MainChar.itemQuest[i7] > 0) {
                    final int ii4 = count;
                    final int idImg = ItemQuest.ICON_IMG[i7] + 8000;
                    final String sl = new StringBuffer(String.valueOf((int) MainChar.itemQuest[i7])).toString();
                    menu.addElement(new Command("", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.setShowText(WindowInfoScr.this.getInfoItemQuest(aa5), (ii4 % WindowInfoScr.this.numW) * 18, (ii4 / WindowInfoScr.this.numW) * 18);
                        }
                    }) {
                        @Override
                        public void paint(Graphics g, int x, int y) {
                            GameData.paintIcon(g, (short) idImg, x, y);
                            game.pokemon.KieuO.soLuong(g, x + 8, y + 9, sl);
                        }
                    });
                }
            }
        }
        return menu;
    }

    protected void showGemItemInfo(short id, int xx, int yy) {
        GemTemp g = Res.getGemByID(id);
        String itemInfo = new StringBuffer(String.valueOf(ItemInInventory.setTemp(g.name, "0"))).append(ItemInInventory.setTemp(g.decript, "0")).toString();
        if (this.isSell) {
            itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(new StringBuffer("Giá bán lại: ").append(g.price / 5).toString(), "0")).toString();
        }
        setShowText(itemInfo, xx, yy);
    }

    protected void showGemItemKhoaInfo(short id, int xx, int yy) {
        GemTemp g = Res.getGemByID(id);
        String itemInfo = ItemInInventory.setTemp(g.name, "0");
        String itemInfo2 = new StringBuffer(String.valueOf(new StringBuffer(String.valueOf(itemInfo)).append(" - Đã khóa").toString())).append(ItemInInventory.setTemp(g.decript, "0")).toString();
        if (this.isSell) {
            itemInfo2 = new StringBuffer(String.valueOf(itemInfo2)).append(ItemInInventory.setTemp(new StringBuffer("Giá bán lại: ").append(g.price / 5).toString(), "0")).toString();
        }
        setShowText(itemInfo2, xx, yy);
    }

    protected void showRealIDInfo(RealID real, int xx, int yy) {
        GemTemp g = Res.getGemByID(real.ID);
        String itemInfo = new StringBuffer(String.valueOf(ItemInInventory.setTemp(g.name, "0"))).append(ItemInInventory.setTemp(g.decript, "0")).toString();
        if (this.isSell) {
            itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(new StringBuffer("Giá bán : ").append(real.price).toString(), "0")).toString();
        }
        setShowText(itemInfo, xx, yy);
    }

    protected void showShopItemInfo(short id, int count) {
        GemTemp g = Res.getShopByID(id);
        String itemInfo = new StringBuffer(String.valueOf(ItemInInventory.setTemp(g.name, "0"))).append(ItemInInventory.setTemp(g.decript, "0")).toString();
        if (this.isSell) {
            itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(new StringBuffer("Giá bán lại: ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, g.price / 5)).toString(), "0")).toString();
        }
        setShowText(itemInfo, (count % this.numW) * 18, (count / this.numW) * 18);
    }

    public String getInfoItemQuest(int id) {
        String info = new StringBuffer("0").append(ItemQuest.NAME_ITEM[id]).toString();
        return new StringBuffer(String.valueOf(info)).append("\nSố lượng: ").append((int) MainChar.itemQuest[id]).toString();
    }

    public String getPotionInfo(int aa) {
        String itemInfo;
        if (aa < 7 || aa >= 14) {
            itemInfo = new StringBuffer(String.valueOf(new StringBuffer("0").append(MainChar.listPotion[aa].name).toString())).append("\nSố lượng: ").append(Canvas.gameScr.mainChar.potions[aa]).toString();
            if (this.isSell) {
                itemInfo = new StringBuffer(String.valueOf(itemInfo)).append("\nKhông thể bán lại").toString();
            }
            if (this.potionShop.contains(new StringBuffer(String.valueOf(aa)).toString())) {
                GemTemp gem = null;
                int size0 = Res.shopTemplate.size();
                for (int i = 0; i < size0; i++) {
                    if (((GemTemp) Res.shopTemplate.elementAt(i)).id == aa - 19) {
                        gem = (GemTemp) Res.shopTemplate.elementAt(i);
                        break;
                    }
                }
                if (gem != null && (aa < 69 || aa > 77)) {
                    itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(gem.decript, "0")).toString();
                }
            } else {
                itemInfo = new StringBuffer(String.valueOf(itemInfo)).append(ItemInInventory.setTemp(new StringBuffer("\nHồi phục: ").append((int) (aa - 19 < 0 ? Res.potionTemplates[aa].recovered : Res.potionTemplates[aa - 19].recovered)).append(" ").append(MainChar.listPotion[aa].name2).toString(), "0")).toString();
            }
        } else if (aa >= 7 && aa < 14) {
            itemInfo = new StringBuffer(String.valueOf(new StringBuffer("0").append(MainChar.listPotion[aa].name).toString())).append("\nSố lượng: ").append(Canvas.gameScr.mainChar.potions[aa]).toString();
        } else {
            itemInfo = new StringBuffer(String.valueOf(Res.PORTION_ITEM_NAME_CLAN)).append("\nSố lượng: ").append(Canvas.gameScr.mainChar.potions[aa]).toString();
        }
        return itemInfo;
    }

    public void doSelectedInventori() {
        if (!this.isChangeTab) {
            closeText();
            final int element = this.selected + (index[focusTab] == 0 ? this.countTabMyitem * 42 : 0);
            int count = 0;
            int size0 = Canvas.gameScr.mainChar.potions.length;
            for (int i = 1; i < size0; i++) {
                int num = Canvas.gameScr.mainChar.potions[i];
                if (num > 0) {
                    if (element == count) {
                        showMenuForPotion(i);
                        return;
                    }
                    count++;
                }
            }
            int inde = element - count;
            int size1 = MainChar.shopItem.size();
            if (inde < size1) {
                final GemTemplate shop = (GemTemplate) MainChar.shopItem.elementAt(inde);
                Vector menuItems = new Vector();
                menuItems.addElement(new Command("Sử dụng", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().doUseSpecialItem(shop.rID);
                    }
                }));
                menuItems.addElement(cmdDelItem(shop, 1, MainChar.shopItem, (byte) 0));
                Canvas.menu.startAt(menuItems, 2);
                return;
            }
            int inde2 = inde - MainChar.shopItem.size();
            int size2 = MainChar.gemItemKhoa.size();
            if (inde2 < size2) {
                final GemTemplate item = (GemTemplate) MainChar.gemItemKhoa.elementAt(inde2);
                if (this.isSell) {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn bán vật phẩm này với giá ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, Res.getGemByID(item.id).price / 5)).append(" không?").toString(), new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().sellGemItem(item.rID, (byte) 1);
                            Canvas.startWaitDlg("Đang bán..", true);
                            WindowInfoScr.this.list.removeElementAt(element);
                        }
                    });
                    return;
                }
                Vector menu = new Vector();
                GemTemp g = Res.getGemByID(item.id);
                if (g != null && g.type == 4) {
                    Command cmd = new Command("Sử dụng", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().douseGemItem(item.id, (byte) 1);
                        }
                    });
                    menu.addElement(cmd);
                }
                menu.addElement(cmdDelItem(item, 0, MainChar.gemItemKhoa, (byte) 1));
                Canvas.menu.startAt(menu, 2);
                return;
            }
            int inde3 = inde2 - MainChar.gemItemKhoa.size();
            int size3 = MainChar.gemItem.size();
            if (inde3 < size3) {
                final GemTemplate item2 = (GemTemplate) MainChar.gemItem.elementAt(inde3);
                if (this.isSell) {
                    Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn bán vật phẩm này với giá ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, Res.getGemByID(item2.id).price / 5)).append(" không?").toString(), new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().sellGemItem(item2.rID, (byte) 0);
                            Canvas.startWaitDlg("Đang bán..", true);
                            WindowInfoScr.this.list.removeElementAt(element);
                        }
                    });
                    return;
                }
                Vector menu2 = new Vector();
                GemTemp g2 = Res.getGemByID(item2.id);
                if (g2 != null && g2.type == 4) {
                    Command cmd2 = new Command("Sử dụng", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().douseGemItem(item2.id, (byte) 0);
                        }
                    });
                    menu2.addElement(cmd2);
                }
                menu2.addElement(cmdDelItem(item2, 0, MainChar.gemItem, (byte) 0));
                Canvas.menu.startAt(menu2, 2);
                return;
            }
            int inde4 = inde3 - MainChar.gemItem.size();
            int size4 = Char.animal.size();
            if (inde4 < size4) {
                final AnimalInfo shop2 = (AnimalInfo) Char.animal.elementAt(inde4);
                Vector menuItems2 = new Vector();
                menuItems2.addElement(new Command("Sử dụng", new IAction() {
                    @Override
                    public void perform() {
                        if ((Canvas.gameScr.mainChar.useHorse > -1 || Canvas.gameScr.mainChar.idhorse > -1) && shop2.typeAnimal == 0) {
                            Canvas.startOKDlg("Bạn đang cưỡi một thú khác. Xuống thú đang cưỡi trước rồi mới cưỡi thú này.");
                        } else {
                            GameService.gI().doRideAnimal(shop2.id, shop2.typeAnimal);
                        }
                    }
                }));
                Canvas.menu.startAt(menuItems2, 2);
                return;
            }
            int inde5 = inde4 - Char.animal.size();
            int size5 = Char.inventory.size();
            if (inde5 < size5) {
                final ItemInInventory item3 = (ItemInInventory) Char.inventory.elementAt(inde5);
                if (!this.isSell) {
                    Vector menu3 = new Vector();
                    menu3.addElement(new Command("Sử dụng", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.doUseItem(item3);
                        }
                    }));
                    if (item3.colorName == 1 && item3.level >= 50 && !ItemInInventory.isAnimalArmor(item3.getTemplate().type)) {
                        menu3.addElement(new Command("Tách trang bị", new IAction() {
                            @Override
                            public void perform() {
                                final ItemInInventory itemInInventory = item3;
                                Canvas.startYesNoDlg("Bạn có muốn tách nguyên liệu từ vật phẩm này không?", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameService.gI().doRequestTachNguyenLIeu(itemInInventory.itemID, 0, -1, 0);
                                        Canvas.endDlg();
                                    }
                                });
                            }
                        }));
                    }
                    menu3.addElement(new Command("Bỏ ra đất", new IAction() {
                        @Override
                        public void perform() {
                            final ItemInInventory itemInInventory = item3;
                            Canvas.startYesNoDlg("Vật phẩm sẽ mất khi bạn bỏ ra đất. Bạn có muốn tiếp tục không?", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().giveItemToGround(itemInInventory.itemID);
                                    Canvas.endDlg();
                                }
                            });
                        }
                    }));
                    if (item3.colorName != 0 && item3.colorName != 4) {
                        if (item3.colorName == 1) {
                            menu3.addElement(new Command("Thăng cấp thường", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(10, item3.itemID);
                                }
                            }));
                            menu3.addElement(new Command("Thăng cấp giữ thuộc tính", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(11, item3.itemID);
                                }
                            }));
                        }
                        if (item3.nameCharSeal.equals("")) {
                            menu3.addElement(new Command("Đóng dấu", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(14, item3.itemID);
                                }
                            }));
                        }
                        if (!item3.nameCharSeal.toLowerCase().equals(Canvas.gameScr.mainChar.name.toLowerCase())) {
                            menu3.addElement(new Command("Đóng dấu lại", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(14, item3.itemID);
                                }
                            }));
                        } else {
                            ItemTemplate it = Res.getItem(item3.charClazz, item3.idTemplate);
                            if ((it.type >= 3 && it.type <= 7) || it.type == 8 || it.type == 9) {
                                menu3.addElement(new Command("Sửa thuộc tính kỹ năng", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameService.gI().doRequestActionCheDo(15, item3.itemID);
                                    }
                                }));
                            }
                        }
                        if (item3.lock == 0) {
                            menu3.addElement(new Command("Khoá", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(12, item3.itemID);
                                }
                            }));
                        }
                        if (item3.lock == 1) {
                            menu3.addElement(new Command("Khoá lại", new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().doRequestActionCheDo(12, item3.itemID);
                                }
                            }));
                        }
                        menu3.addElement(new Command("Sửa thuộc tính khác", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().doRequestActionCheDo(13, item3.itemID);
                            }
                        }));
                    }
                    Canvas.menu.startAt(menu3, 2);
                    return;
                }
                ItemTemplate it2 = Res.getItem(item3.charClazz, item3.idTemplate);
                Canvas.startYesNoDlg(new StringBuffer("Bạn có muốn bán vật phẩm này với giá ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, (it2.price / 5) + (it2.plus * 1000))).append(" không?").toString(), new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().sellItem(item3.itemID);
                        Canvas.startWaitDlg("Đang bán..", true);
                        WindowInfoScr.this.list.removeElementAt(element);
                    }
                });
            }
        }
    }

    protected void doSellItem() {
    }

    private Command cmdDelItem(final GemTemplate item, final int index2, Vector lit, final byte khoa) {
        return new Command("Bỏ ra đất", new IAction() {
            @Override
            public void perform() {
                final GemTemplate gemTemplate = item;
                final int i = index2;
                final byte b = khoa;
                Canvas.startYesNoDlg("Bạn có muốn bỏ hết vật phẩm này không ?", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().dellGemItem(gemTemplate.id, i, b);
                        Canvas.endDlg();
                    }
                });
            }
        });
    }

    private void showMenuForPotion(final int potionType) {
        Vector menuItems = new Vector();
        if (game.pokemon.KhanPk.laKhan(potionType)) {
            // 0.9.8: khăn PK - "Đeo khăn (bật PK)" / "Tháo khăn (tắt PK)" + xác nhận; máy chủ quyết (không còn chặn 3 phút ở client)
            menuItems.addElement(new Command(game.pokemon.KhanPk.nhan(), new IAction() {
                @Override
                public void perform() {
                    game.pokemon.KhanPk.hoi(potionType);
                }
            }));
        } else {
            menuItems.addElement(new Command("Sử dụng", new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.doUsePotion(potionType);
                    if (Canvas.gameScr.mainChar.potions[potionType] == 0) {
                        WindowInfoScr.this.list.removeElementAt(WindowInfoScr.this.selected);
                    }
                }
            }));
        }
        if (game.pokemon.MoTaVatPham.ganONhanh(potionType)) {   // 0.9.14: chỉ bình HP / PP (trước: mọi thứ trừ khăn PK)
            menuItems.addElement(new Command("Cho vào phím tắt", new IAction() {
                @Override
                public void perform() {
                    Vector menuItems2 = new Vector();
                    for (int i = 0; i < 2; i++) {
                        final int ii = i;
                        String string = new StringBuffer("Phím số ").append(7 + (2 * i)).toString();
                        final int i2 = potionType;
                        menuItems2.addElement(new Command(string, new IAction() {
                            @Override
                            public void perform() {
                                MainChar.quickSlot[GameScr.indexTabSlot][3 + ii].setIsPotion(i2);
                                RMS.saveQuickSlot();
                            }
                        }));
                    }
                    Canvas.menu.startAt(menuItems2, 2);
                }
            }));
        }
        menuItems.addElement(new Command("Bỏ ra đất", new AnonymousClass120(potionType)));
        Canvas.menu.startAt(menuItems, 2);
    }

    class AnonymousClass120 implements IAction {
        private final int val$potionType;

        AnonymousClass120(int i) {
            this.val$potionType = i;
        }

        @Override
        public void perform() {
            final int i = this.val$potionType;
            Canvas.startYesNoDlg("Bạn muốn bỏ vật phẩm này không ?", new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                    GameService.gI().dellPotion(i);
                    WindowInfoScr.this.list.removeElementAt(WindowInfoScr.this.selected);
                    Canvas.gameScr.mainChar.potions[i] = 0;
                    Canvas.currentDialog = null;
                }
            });
        }
    }

    protected void doUseItem(ItemInInventory item) {
        // pokemon: server kiểm cấp / hệ / điều kiện và LUÔN trả lời (thông báo nhanh -104, DungDo). Bản cũ chặn cấp
        // theo cấp MẪU (khác cấp món) và ném NPE khi thiếu mẫu -> bấm "Sử dụng" không có gì xảy ra.
        game.pokemon.DungDo.dungItem(item);
    }

    private boolean isWeapone(int type) {
        return type >= 3 && type <= 7;
    }

    public void showCharWearingInfo() {
        int aa = 0;
        if (this.selected % 3 == 1) {
            if (charWearing.isDoing && this.selected != 4) {
                int bb = ((this.selected / 3) * 2) + ((this.selected % 3) - (this.selected % 3 > 0 ? 1 : 0));
                int size0 = charWearing.wearingItems.size();
                for (int i = 0; i < size0; i++) {
                    ItemInInventory item = (ItemInInventory) charWearing.wearingItems.elementAt(i);
                    ItemTemplate tc = Res.getItem(item.charClazz, item.idTemplate);
                    if (tc != null && tc.type == 13) {
                        // 0.9.30: bấm lại = menu Tháo ra (như ô trang bị khác)
                        xemMonDangMac(item, tc, posWearChar[bb][0] + 5, posWearChar[bb][1] - 2);
                        return;
                    }
                }
                return;
            }
            if (index.length == 1) {
                if (this.selected != 4) {
                    Vector menu = new Vector();
                    menu.addElement(new Command("Thông tin", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.setShowText(ItemInInventory.setTemp(WindowInfoScr.charWearing.infoAnimal, "0"), 80, 120);
                            Canvas.endDlg();
                        }
                    }));
                    menu.addElement(new Command("Trang bị thú cưỡi", new IAction() {
                        @Override
                        public void perform() {
                            Char c = (Char) Canvas.gameScr.findCharByID(Canvas.gameScr.saveIDViewInfoAnimal);
                            if (c != null) {
                                Canvas.gameScr.gameService.requestSomeOneInfo(c.ID, (byte) 1);
                            } else {
                                WindowInfoScr.this.right.action.perform();
                            }
                            Canvas.endDlg();
                        }
                    }));
                    Canvas.menu.startAt(menu, 3);
                    return;
                }
                int size1 = charWearing.wearingItems.size();
                for (int i2 = 0; i2 < size1; i2++) {
                    ItemInInventory item2 = (ItemInInventory) charWearing.wearingItems.elementAt(i2);
                    if (Res.getItem(item2.charClazz, item2.idTemplate).type == 19) {
                        xemMonDangMac(item2, Res.getItem(item2.charClazz, item2.idTemplate), posWearChar[9][0] + 5, posWearChar[9][1] - 2);
                        return;
                    }
                }
                return;
            }
            if (this.selected != 4) {
                Vector menu1 = new Vector();
                // 0.9.30 (chủ dự án: "APK và JAR không có mấy cái đó"): ô dưới giữa = thú cưỡi + CUỐC (cùng chỗ vẽ, type 13) -
                // thêm lối Trang bị thú (xem / tháo / thay đồ thú) và cuốc đang mặc (xem / tháo)
                menu1.addElement(new Command("Trang bị thú", new IAction() {
                    @Override
                    public void perform() {
                        game.pokemon.TrangBiThu.mo();
                    }
                }));
                final ItemInInventory cuoc = cuocDangMac();
                if (cuoc != null) {
                    menu1.addElement(new Command("Cuốc: xem", new IAction() {
                        @Override
                        public void perform() {
                            WindowInfoScr.this.showItemInventoryInfo(cuoc, false, posWearChar[7][0] + 5, posWearChar[7][1] - 2);
                        }
                    }));
                    menu1.addElement(new Command("Cuốc: tháo ra", new IAction() {
                        @Override
                        public void perform() {
                            game.pokemon.TrangBiThu.thao(cuoc);
                            WindowInfoScr.this.closeText();
                        }
                    }));
                }
                menu1.addElement(new Command("Thú cưỡi", new IAction() {
                    @Override
                    public void perform() {
                        if (!WindowInfoScr.charWearing.infoAnimal.equals("")) {
                            WindowInfoScr.this.setShowText(ItemInInventory.setTemp(WindowInfoScr.charWearing.infoAnimal, "0"), 80, 120);
                        }
                    }
                }));
                menu1.addElement(new Command("Thú cưng", new IAction() {
                    @Override
                    public void perform() {
                        if (WindowInfoScr.charWearing.myPet != null) {
                            WindowInfoScr.this.setShowText(new StringBuffer(String.valueOf(ItemInInventory.setTemp(WindowInfoScr.charWearing.myPet.infoPet, "0"))).append(ItemInInventory.setTemp(new StringBuffer("Còn lại: ").append(((long) WindowInfoScr.charWearing.myPet.totalTime) - ((System.currentTimeMillis() - WindowInfoScr.charWearing.myPet.timeStart) / 60000)).append(" phút").toString(), "0")).toString(), 80, 120);
                        }
                    }
                }));
                Canvas.menu.startAt(menu1, 3);
                return;
            }
            int size2 = charWearing.wearingItems.size();
            for (int i3 = 0; i3 < size2; i3++) {
                ItemInInventory item3 = (ItemInInventory) charWearing.wearingItems.elementAt(i3);
                if (Res.getItem(item3.charClazz, item3.idTemplate).type == 19) {
                    xemMonDangMac(item3, Res.getItem(item3.charClazz, item3.idTemplate), posWearChar[9][0] + 5, posWearChar[9][1] - 2);
                    return;
                }
            }
            return;
        }
        int bb2 = ((this.selected / 3) * 2) + ((this.selected % 3) - (this.selected % 3 > 0 ? 1 : 0));
        int size3 = charWearing.wearingItems.size();
        for (int i4 = 0; i4 < size3; i4++) {
            ItemInInventory item4 = (ItemInInventory) charWearing.wearingItems.elementAt(i4);
            ItemTemplate it = Res.getItem(item4.charClazz, item4.idTemplate);
            if (it == null) {
                continue;
            }
            if (it.type == idWearChar[bb2] || (idWearChar[bb2] == -1 && it.type > 2 && it.type < 8)) {
                aa++;
                if (bb2 != 7 || aa != 1) {
                    xemMonDangMac(item4, it, posWearChar[bb2][0] + 5, posWearChar[bb2][1] - 2);
                    return;
                }
            }
        }
    }

    /** 0.9.30: cuốc (type 13) nhân vật chính đang mặc, null = không. */
    static ItemInInventory cuocDangMac() {
        if (charWearing == null || charWearing.wearingItems == null) {
            return null;
        }
        for (int i = 0; i < charWearing.wearingItems.size(); i++) {
            ItemInInventory it = (ItemInInventory) charWearing.wearingItems.elementAt(i);
            ItemTemplate t = Res.getItem(it.charClazz, it.idTemplate);
            if (t != null && t.type == 13) {
                return it;
            }
        }
        return null;
    }

    /** pokemon: món đang chọn ở tab Trang bị (bấm lần hai cùng món = menu Tháo ra). */
    private ItemInInventory monDangMacXem;
    private long lucXemMonDangMac;

    /**
     * pokemon: bấm một ô trang bị của CHÍNH MÌNH: lần đầu hiện thông tin (như cũ), bấm lại cùng ô trong 6 giây = menu
     * "Tháo ra" (server DungVatPham.thaoTrangBi: lệnh 29 với id món đang mặc). Game gốc không có cách tháo đồ.
     */
    private void xemMonDangMac(final ItemInInventory item, ItemTemplate it, int x, int y) {
        showItemInventoryInfo(item, false, x, y);
        if (charWearing != Canvas.gameScr.mainChar || this.isSell) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.monDangMacXem == item && now - this.lucXemMonDangMac < 6000) {
            Vector menu = new Vector();
            boolean vuKhi = it != null && isWeapone(it.type);
            menu.addElement(new Command(vuKhi ? "Tháo ra (đổi món cầm tay)" : "Tháo ra", new IAction() {
                @Override
                public void perform() {
                    game.pokemon.DungDo.thao(item);
                    WindowInfoScr.this.closeText();
                }
            }));
            Canvas.menu.startAt(menu, 3);
            this.monDangMacXem = null;
            return;
        }
        this.monDangMacXem = item;
        this.lucXemMonDangMac = now;
    }

    public void showInfoWearingAnimal() {
        if (charWearing.wearingAnimal != null && charWearing.wearingAnimal.size() > 0) {
            int size0 = charWearing.wearingAnimal.size();
            for (int i = 0; i < size0; i++) {
                ItemInInventory item = (ItemInInventory) charWearing.wearingAnimal.elementAt(i);
                ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
                if (it.type == idWearAnimal[this.selected]) {
                    int i2 = 0 + 1;
                    showItemInventoryInfo(item, false, posWearChar[0][0] + 5, posWearChar[0][1] - 2);
                    return;
                }
            }
        }
    }

    public void showItemInventoryInfo(ItemInInventory item, boolean isSell, int x, int y) {
        this.arrayKhamNgoc = null;
        if (item.totalKham > 0) {
            this.arrayKhamNgoc = new byte[item.totalKham];
            for (int i = 0; i < item.totalKham; i++) {
                this.arrayKhamNgoc[i] = -1;
            }
            byte k = 0;
            for (int i2 = 0; i2 < item.allAttribute.size(); i2++) {
                InfoAttributeItem name = (InfoAttributeItem) item.allAttribute.elementAt(i2);
                if (name.getColorPaint(false) == 3) {
                    for (int j = 0; j < item.numKham; j++) {
                        byte b = k;
                        k = (byte) (b + 1);
                        this.arrayKhamNgoc[b] = Res.idNgocKham[name.getID() - 10];
                    }
                    break;
                }
            }
        }
        setShowText(item.getDescription(isSell), x, y);
        if (!item.isEnoughData) {
            item.isEnoughData = true;
            GameService.gI().requestItemInfo(item.itemID, charWearing == null ? Canvas.gameScr.mainChar.ID : charWearing.ID);
        }
    }

    public void dofireBasePoint() {
        if (Canvas.gameScr.mainChar.basePointLeft == 0) {
            Canvas.startOKDlg("Đã hết điểm tiềm năng để cộng. Hãy lên cấp để có thêm điểm tiềm năng.");
        } else {
            Canvas.inputDlg.setInfo("Nhập số", new IAction() {
                @Override
                public void perform() {
                    String str = Canvas.inputDlg.tfInput.getText();
                    if (str.equals("")) {
                        return;
                    }
                    try {
                        int value = Integer.parseInt(str);
                        if (value > Canvas.gameScr.mainChar.basePointLeft) {
                            Canvas.startOKDlg(new StringBuffer("Bạn chỉ còn ").append((int) Canvas.gameScr.mainChar.basePointLeft).append(" điểm tiềm năng").toString());
                        } else {
                            GameService.gI().addBasePoint(WindowInfoScr.this.selected, value);
                            Canvas.startWaitDlg();
                        }
                    } catch (Exception e) {
                        Canvas.startOKDlg("Có lỗi xảy ra. Vui lòng chỉ nhập số.");
                    }
                }
            }, 1, 10, true);
            Canvas.inputDlg.show();
        }
    }

    public void setCmdLeftTrade() {
        this.left = new Command("Xong", new IAction() {
            @Override
            public void perform() {
                WindowInfoScr.this.closeText();
                Canvas.gameScr.gameService.sendOkTrade(Canvas.gameScr.mainChar.ID);
                WindowInfoScr.this.left = new Command("Xin chờ", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.gameScr.gameService.sendCancelTrade(Canvas.gameScr.mainChar.ID);
                        Canvas.gameScr.mainChar.isTrade = false;
                        Canvas.currentDialog = null;
                    }
                });
            }
        });
    }

    public void setListSellItem(Vector itemsSell, int type) {
        this.list.removeAllElements();
        int count = 0;
        int size0 = itemsSell.size();
        for (int i = 0; i < size0; i++) {
            final ItemInInventory item = (ItemInInventory) Canvas.gameScr.shop.elementAt(i);
            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
            if (it.type == type || type == -1) {
                final int co = count;
                count++;
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        WindowInfoScr.this.setShowText(item.getTemplateDescription(), (co % WindowInfoScr.this.numW) * 18, (co / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        item.paint(g, x, y);
                    }
                });
            }
        }
    }

    public boolean setMaxxreadyBuy() {
        if (this.readyBuy.size() >= 100) {
            Canvas.startOKDlg("Bạn chỉ được mua 100 vật phẩm một lần.");
            return true;
        }
        return false;
    }

    class AnonymousClass131 implements IAction {
        private final int val$type;

        AnonymousClass131(int i) {
            this.val$type = i;
        }

        @Override
        public void perform() {
            WindowInfoScr.this.closeText();
            if (WindowInfoScr.this.setMaxxreadyBuy()) {
                return;
            }
            ItemInInventory selectedItem = null;
            int count = 0;
            if (this.val$type != -1) {
                int size0 = Canvas.gameScr.shop.size();
                for (int i = 0; i < size0; i++) {
                    ItemInInventory it = (ItemInInventory) Canvas.gameScr.shop.elementAt(i);
                    ItemTemplate iti = Res.getItem(it.charClazz, it.idTemplate);
                    if (iti.type == this.val$type) {
                        if (count == WindowInfoScr.this.selected) {
                            selectedItem = it;
                            break;
                        }
                        count++;
                    }
                }
            } else {
                selectedItem = (ItemInInventory) Canvas.gameScr.shop.elementAt(WindowInfoScr.this.selected);
            }
            final ItemInInventory item = selectedItem;
            final ItemTemplate it2 = Res.getItem(selectedItem.charClazz, selectedItem.idTemplate);
            Canvas.startYesNoDlg("Bạn có muốn cho thú cưng ăn vật phẩm này không?", new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.mainChar.charXu -= (long) it2.price;
                    WindowInfoScr.this.readyBuy.addElement(item);
                    Canvas.startOKDlg("Đã chọn vật phẩm cho thú cưng ăn.", false);
                }
            });
        }
    }

    private void doSetLeftPetEat(int type) {
        this.left = new Command("Mua", new AnonymousClass131(type));
    }

    class AnonymousClass133 implements IAction {
        private final int val$type;

        AnonymousClass133(int i) {
            this.val$type = i;
        }

        @Override
        public void perform() {
            String str;
            WindowInfoScr.this.closeText();
            if (WindowInfoScr.this.setMaxxreadyBuy()) {
                return;
            }
            ItemInInventory selectedItem = null;
            int count = 0;
            if (this.val$type != -1) {
                int size0 = Canvas.gameScr.shop.size();
                for (int i = 0; i < size0; i++) {
                    ItemInInventory it = (ItemInInventory) Canvas.gameScr.shop.elementAt(i);
                    ItemTemplate iti = Res.getItem(it.charClazz, it.idTemplate);
                    if (iti.type == this.val$type) {
                        if (count == WindowInfoScr.this.selected) {
                            selectedItem = it;
                            break;
                        }
                        count++;
                    }
                }
            } else {
                selectedItem = (ItemInInventory) Canvas.gameScr.shop.elementAt(WindowInfoScr.this.selected);
            }
            final ItemInInventory item = selectedItem;
            if (Canvas.gameScr.mainChar.FullInventory() >= 42 * Canvas.gameScr.mainChar.limItemBag) {
                WindowInfoScr.this.setShowText("Hành trang đã đầy", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                return;
            }
            final ItemTemplate it2 = Res.getItem(selectedItem.charClazz, selectedItem.idTemplate);
            if (it2.price > Canvas.gameScr.mainChar.charXu) {
                WindowInfoScr.this.setShowText("Hết tiền", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                return;
            }
            if (it2.ndayLoan == 0) {
                str = new StringBuffer("Bạn có muốn mua ").append(it2.name).append(", Giá: ").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, it2.price)).append(" không?").toString();
            } else {
                str = new StringBuffer("Bạn có muốn thuê ").append(it2.name).append(", Giá: ").append(it2.price).append("Lượng không?").toString();
            }
            Canvas.startYesNoDlg(str, new IAction() {
                @Override
                public void perform() {
                    // pokemon: gửi mua NGAY (trước: trừ xu + báo "Đã mua" ở client, gom tới lúc đóng cửa hàng mới gửi)
                    // - server trả túi / xu (16, -105) và thông báo "Đã mua X (−giá)" hoặc lý do không mua được
                    WindowInfoScr.this.readyBuy.addElement(item);
                    WindowInfoScr.this.doBuyItem();
                }
            });
        }
    }

    private void doSetLeftSellWeapon(int type) {
        this.left = new Command("Mua", new AnonymousClass133(type));
    }

    class AnonymousClass135 implements IAction {
        AnonymousClass135() {
        }

        @Override
        public void perform() {
            WindowInfoScr.this.closeText();
            if (WindowInfoScr.this.setMaxxreadyBuy()) {
                return;
            }
            GemTemp selectedGemItem = (GemTemp) Canvas.gameScr.shop.elementAt(WindowInfoScr.this.selected);
            boolean is = false;
            int num = 0;
            GemTemplate ge = GemTemplate.getGemByID(selectedGemItem.id);
            if (ge != null) {
                num = ge.number;
                is = true;
            }
            if (!is && Canvas.gameScr.mainChar.isFullInventory()) {
                WindowInfoScr.this.setShowText("Hành trang đã đầy", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                return;
            }
            GemTemp g = Res.getGemByID(selectedGemItem.id);
            int numm = num;
            Canvas.inputDlg.setInfo("Số lượng: ", new AnonymousClass136(this, g, selectedGemItem, numm), 1, 4, true);
            Canvas.inputDlg.show();
        }
    }

    private void dosetLeftSellGemItem() {
        this.left = new Command("Mua", new AnonymousClass135());
    }

    class AnonymousClass136 implements IAction {
        final AnonymousClass135 this$1;
        private final GemTemp val$g;
        private final GemTemp val$selectedGemItem;
        private final int val$numm;

        AnonymousClass136(AnonymousClass135 anonymousClass135, GemTemp gemTemp, GemTemp gemTemp2, int i) {
            this.this$1 = anonymousClass135;
            this.val$g = gemTemp;
            this.val$selectedGemItem = gemTemp2;
            this.val$numm = i;
        }

        @Override
        public void perform() {
            try {
                final int number = Integer.parseInt(Canvas.inputDlg.tfInput.getText());
                if (number < 0) {
                    return;
                }
                if ((this.val$g.typeMoney == 0 && this.val$g.price * number > Canvas.gameScr.mainChar.charXu) || (this.val$g.typeMoney == 1 && this.val$g.price * number > Canvas.gameScr.mainChar.luong)) {
                    WindowInfoScr.this.setShowText("Hết tiền", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                    return;
                }
                String string = new StringBuffer("Bạn có muốn mua ").append(number).append(" ").append(this.val$g.name).append(", Giá: ").append(game.pokemon.TienTe.chu(this.val$g.typeMoney == 0 ? game.pokemon.TienTe.XU : game.pokemon.TienTe.LUONG, this.val$g.price * number)).append(" không?").toString();
                final GemTemp gemTemp = this.val$selectedGemItem;
                final int i = this.val$numm;
                final GemTemp gemTemp2 = this.val$g;
                Canvas.startYesNoDlg(string, new IAction() {
                    @Override
                    public void perform() {
                        int aa = 0;
                        int size0 = WindowInfoScr.this.readyBuy.size();
                        for (int i2 = 0; i2 < size0; i2++) {
                            GemTemp gem = (GemTemp) WindowInfoScr.this.readyBuy.elementAt(i2);
                            if (gem.id == gemTemp.id) {
                                if (i + gem.number + number > 300) {
                                    Canvas.startOKDlg("Không được mua nhiều hơn 300 cái.");
                                    return;
                                } else {
                                    gem.number = (short) (gem.number + number);
                                    aa = 1;
                                    break;
                                }
                            }
                        }
                        if (aa == 0) {
                            GemTemp ge = new GemTemp();
                            ge.id = gemTemp.id;
                            ge.number = (short) number;
                            WindowInfoScr.this.readyBuy.addElement(ge);
                        }
                        WindowInfoScr.this.doBuyItem();   // pokemon: gửi ngay, tiền / túi / thông báo do server
                    }
                });
            } catch (Exception e) {
                Canvas.startOKDlg("Chỉ được nhập số.");
            }
        }
    }

    private void setListSellGemItem(Vector shop) {
        this.list.removeAllElements();
        int size0 = shop.size();
        for (int i = 0; i < size0; i++) {
            final GemTemp gem = (GemTemp) shop.elementAt(i);
            final int ii = i;
            if (gem.isSell) {
                this.list.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        String str = new StringBuffer(String.valueOf(ItemInInventory.setTemp(gem.name, "0"))).append(ItemInInventory.setTemp(gem.decript, "0")).append(ItemInInventory.setTemp(new StringBuffer("Giá: ").append(game.pokemon.TienTe.chu(gem.typeMoney == 0 ? game.pokemon.TienTe.XU : game.pokemon.TienTe.LUONG, gem.price)).toString(), "0")).toString();
                        WindowInfoScr.this.setShowText(str, (ii % WindowInfoScr.this.numW) * 18, (ii / WindowInfoScr.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics g, int x, int y) {
                        Res.paintGem(g, gem.idImage, x, y);
                    }
                });
            }
        }
    }

    private void setListPotion() {
        this.list.removeAllElements();
        int size0 = Canvas.gameScr.shop.size();
        for (int i = 0; i < size0; i++) {
            final ItemInInventory item = (ItemInInventory) Canvas.gameScr.shop.elementAt(i);
            this.list.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    WindowInfoScr.this.setShowText(item.getTemplateDescription(), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                }
            }) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    GameData.paintIcon(g, (short) (MainChar.listPotion[item.potionType].index + 5500), x, y);
                }
            });
        }
    }

    class AnonymousClass142 implements IAction {
        AnonymousClass142() {
        }

        @Override
        public void perform() {
            WindowInfoScr.this.isShowText = false;
            if (WindowInfoScr.this.setMaxxreadyBuy()) {
                return;
            }
            final ItemInInventory item = (ItemInInventory) Canvas.gameScr.shop.elementAt(WindowInfoScr.this.selected);
            // pokemon: không cho món của CỬA HÀNG vào readyBuy trước khi chọn số lượng (bản cũ: huỷ hộp số lượng vẫn để
            // món đó trong readyBuy -> đóng cửa hàng là mua; số lượng cộng dồn vào chính món của cửa hàng -> lần mua sau
            // gửi số cũ + mới). Mỗi lần xác nhận gửi NGAY một gói mua đúng N bình.
            if (Canvas.gameScr.mainChar.potions[item.potionType] >= 999) {
                WindowInfoScr.this.setShowText("MAX: 999", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                return;
            }
            final int price = Res.potionTemplates[item.potionType].price;
            if (price > Canvas.gameScr.mainChar.charXu) {
                WindowInfoScr.this.setShowText("Hết tiền", (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                return;
            }
            Canvas.inputDlg.setInfo("Số lượng: ", new IAction() {
                @Override
                public void perform() {
                    try {
                        int number = Integer.parseInt(Canvas.inputDlg.tfInput.getText());
                        if (number <= 0) {
                            Canvas.startOKDlg("Số lượng phải lớn hơn 0.");
                            return;
                        }
                        if (number > 120) {
                            Canvas.startOKDlg("Chỉ được mua nhiều nhất 120 đơn vị một lần.");
                            return;
                        }
                        final int soMua = number;
                        String tenThuoc = "";
                        try {
                            tenThuoc = MainChar.listPotion[item.potionType].name;
                        } catch (Throwable e) {
                        }
                        // pokemon: xác nhận kèm TỔNG tiền kiểu Poké Mart
                        Canvas.startYesNoDlg(new StringBuffer("Mua ").append(soMua).append(" ").append(tenThuoc).append("? Tổng: ").append((long) soMua * price).append(" xu").toString(), new IAction() {
                            @Override
                            public void perform() {
                                ItemInInventory mua = new ItemInInventory();
                                mua.potionType = item.potionType;
                                mua.number = (short) soMua;
                                Vector v = new Vector();
                                v.addElement(mua);
                                GameService.gI().buyPotion(v);   // xu / số bình / "Đã mua X ×N (−giá)" do server trả
                                Canvas.endDlg();
                                WindowInfoScr.this.countPop = 0;
                                WindowInfoScr.this.closeText();
                                WindowInfoScr.this.setShowText(item.getTemplateDescription(), (WindowInfoScr.this.selected % WindowInfoScr.this.numW) * 18, (WindowInfoScr.this.selected / WindowInfoScr.this.numW) * 18);
                            }
                        });
                    } catch (Exception e) {
                        Canvas.startOKDlg("Không được nhập chữ");
                    }
                }
            }, 1, 10, true);
            Canvas.inputDlg.show();
        }
    }

    private void doSetLeftPotion() {
        this.left = new Command("Mua", new AnonymousClass142());
    }

    public void doBuyItem() {
        if (this.readyBuy.size() > 0) {
            if (index[focusTab] == 10) {
                GameService.gI().buyGemItem(this.readyBuy);
            } else if (index[focusTab] == 19) {
                GameService.gI().buyPotion(this.readyBuy);
            } else {
                GameService.gI().buyItem(this.readyBuy);
            }
            this.readyBuy.removeAllElements();
        }
        if (index[focusTab] == 21 || index[focusTab] == 25) {
            if (index[focusTab] == 21) {
                GameService.gI().doImbueItem(-1);
            } else {
                GameService.gI().doKhamItem(-1);
            }
            int size0 = MainChar.gemitemImbue.size();
            for (int i = 0; i < size0; i++) {
                RealID real = (RealID) MainChar.gemitemImbue.elementAt(i);
                GemTemplate item = GemTemplate.getGemByID(real.ID);
                if (item != null) {
                    item.number++;
                    break;
                }
            }
            MainChar.gemitemImbue.removeAllElements();
            Canvas.gameScr.mainChar.itemImbue = null;
            this.imgWeaponAvatar = null;
            this.idImbue = -1;
            return;
        }
        if (index[focusTab] == 22) {
            Canvas.gameScr.gameService.finishPutItem2Bag();
        } else if (index[focusTab] == 8) {
            Canvas.gameScr.gameService.sendCancelTrade(Canvas.gameScr.mainChar.ID);
            Canvas.gameScr.mainChar.isTrade = false;
        }
    }

    public void resetImbue() {
        MainChar.gemitemImbue.removeAllElements();
    }

    private void setQuestInfo() {
        // 31/10/2026: số dòng đã xuống dòng (giới hạn cuộn đúng với chữ vẽ ở paintQuest)
        this.questInfo = dongNhiemVu(RONG_CHU_NV);
    }

    public void questClan(String text1, byte finishQ) {
        this.questInfo = new Vector();
        String[] title = Font.normalFont[0].splitFontBStrInLine(text1, 100);
        for (String str : title) {
            this.questInfo.addElement(str);
        }
        Canvas.endDlg();
    }

    // ------------------------------------------------------------------ 31/10/2026: 4 cửa sổ cũ kiểu chung (máy có con trỏ)
    // Gian hàng của tôi (23), gian hàng người khác (24), Chế đồ (28), Ép đồ thú (32): tấm chung (game.pokemon.ManCu), mọi việc
    // bằng chạm (đặt bán / lấy về / mua / bỏ vào / lấy ra / Xong / Hợp). Luật + gói gửi giữ nguyên: chạm ô = đặt trạng thái
    // chọn như phím rồi gọi đúng việc cũ (thẻ lệnh "Bỏ vào / Lấy ra / Thông tin" -> thẻ lệnh chung). J2ME bấm phím vẽ như cũ.

    /** Tab đang mở là một trong 4 cửa sổ có bản vẽ kiểu chung. */
    public boolean laCuaSoCham() {
        if (!game.pokemon.MenuNguCanh.bat() || index == null || focusTab < 0 || focusTab >= index.length) {
            return false;
        }
        int t = index[focusTab];
        return t == 23 || t == 24 || t == 28 || t == 32;
    }

    /** Gian hàng của tôi ở màn hẹp: 0 = Hành trang, 1 = Đang bán */
    int cotGianHang;

    static String tenMon(Object o) {
        if (o instanceof ItemInInventory) {
            String[] n = ((ItemInInventory) o).getName();
            String t = n == null || n.length == 0 ? "?" : n[0];
            return t.length() > 1 && isDegit(t.charAt(0)) ? t.substring(1) : t;
        }
        int id = o instanceof RealID ? ((RealID) o).ID : o instanceof GemTemplate ? ((GemTemplate) o).id : -1;
        GemTemp t = id < 0 ? null : Res.getGemByID((short) id);
        return t == null ? "?" : t.name;
    }

    /** Ô hiển thị cho món (RealID -> GemTemplate để vẽ icon ngọc / số). */
    static Object oMon(Object o) {
        if (o instanceof RealID) {
            RealID r = (RealID) o;
            GemTemplate g = new GemTemplate(r.ID);
            g.id = r.ID;
            g.number = r.number > 0 ? r.number : (short) 1;
            return g;
        }
        return o;
    }

    static int giaMon(Object o) {
        return o instanceof RealID ? ((RealID) o).price : o instanceof ItemInInventory ? ((ItemInInventory) o).prize : 0;
    }

    void veCuaSoCham(Graphics g) {
        this.anNutLenh = true;
        Canvas.resetTrans(g);
        int t = index[focusTab];
        if (t == 23) {
            veGianHangToi(g);
        } else if (t == 24) {
            veGianHangKhac(g);
        } else if (t == 28) {
            veCheDo(g);
        } else {
            veEpDoThu(g);
        }
    }

    /** Đầu cột: chữ nhãn; trả cao đã dùng. */
    static int dauCot(Graphics g, int[] c, String s) {
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(s, game.pokemon.ChuDe.NHAN, c[2] - 8), c[0] + 4, c[1], game.pokemon.ChuDe.NHAN,
                game.pokemon.ChuDe.CHU, 0);
        return game.pokemon.ChuDe.cao(game.pokemon.ChuDe.NHAN) + game.pokemon.Bang.cach();
    }

    void veGianHangToi(Graphics g) {
        int[] c = game.pokemon.ManCu.tam(g, this, "Gian hàng của tôi", game.pokemon.ManCu.viec(this.right), null);
        int[][] v;
        if (c[2] >= 360) {
            v = game.pokemon.ManCu.chia(c, 50);
        } else {
            // màn hẹp: chip "Hành trang | Đang bán" thay hai cột
            Object[] vc = {new IAction() {
                public void perform() {
                    WindowInfoScr.this.cotGianHang = 0;
                }
            }, new IAction() {
                public void perform() {
                    WindowInfoScr.this.cotGianHang = 1;
                }
            }};
            int hc = game.pokemon.Bang.hangChip(g, c[0], c[1], c[2], new String[]{"Hành trang", "Đang bán"}, this.cotGianHang, vc) + game.pokemon.Bang.cach();
            int[] r = {c[0], c[1] + hc, c[2], c[3] - hc};
            int[] an = {c[0], c[1] + c[3], c[2], 0};
            v = this.cotGianHang == 0 ? new int[][]{r, an} : new int[][]{an, r};
        }
        final Vector tui = new Vector();
        for (int i = 0; i < Char.inventory.size(); i++) {
            Object o = Char.inventory.elementAt(i);
            if (o instanceof ItemInInventory && !((ItemInInventory) o).isSelling) {
                tui.addElement(o);
            }
        }
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            tui.addElement(MainChar.gemItem.elementAt(i));
        }
        int h0 = v[0][3] <= 0 ? 0 : dauCot(g, v[0], "Hành trang - chạm để đặt bán");
        if (v[0][3] > 0) {
        game.pokemon.ManCu.danhSach(g, new int[]{v[0][0], v[0][1] + h0, v[0][2], v[0][3] - h0}, tui.size(), -1, new game.pokemon.ManCu.DongIcon() {
            public Object mon(int i) {
                return tui.elementAt(i);
            }

            public String chinh(int i) {
                return tenMon(tui.elementAt(i));
            }

            public String phu(int i) {
                Object o = tui.elementAt(i);
                return o instanceof GemTemplate ? "Còn " + ((GemTemplate) o).number : "Chạm để đặt giá bán";
            }

            public void cham(int i) {
                Object o = tui.elementAt(i);
                if (o instanceof ItemInInventory) {
                    WindowInfoScr.this.setLeftSellItem((ItemInInventory) o, (short) -1);
                } else {
                    WindowInfoScr.this.setLeftSellItem(null, ((GemTemplate) o).rID);
                }
            }
        }, "Hành trang trống");
        }
        final Vector ban = this.sellItem;
        if (v[1][3] <= 0) {
            return;
        }
        int h1 = dauCot(g, v[1], "Đang bán - chạm để lấy về");
        game.pokemon.ManCu.danhSach(g, new int[]{v[1][0], v[1][1] + h1, v[1][2], v[1][3] - h1}, ban.size(), -1, new game.pokemon.ManCu.DongIcon() {
            public Object mon(int i) {
                return oMon(ban.elementAt(i));
            }

            public String chinh(int i) {
                return tenMon(ban.elementAt(i));
            }

            public String phu(int i) {
                return "Giá " + game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, giaMon(ban.elementAt(i)));
            }

            public void cham(int i) {
                final Object o = ban.elementAt(i);
                game.pokemon.HopXacNhan.hoi("Lấy về", "Thôi bán " + tenMon(o) + " và lấy về hành trang?", "Lấy về", new IAction() {
                    public void perform() {
                        WindowInfoScr.this.doRequestSellItem(o instanceof ItemInInventory ? (ItemInInventory) o : null,
                                o instanceof RealID ? ((RealID) o).realID : (short) -1, 0, false);
                    }
                }, null);
            }
        }, "Chưa bán món nào");
    }

    void veGianHangKhac(Graphics g) {
        int[] c = game.pokemon.ManCu.tam(g, this, "Gian hàng", game.pokemon.ManCu.viec(this.right), null);
        final Vector ban = this.sellItem;
        int h0 = dauCot(g, c, "Chạm món để xem / mua");
        game.pokemon.ManCu.danhSach(g, new int[]{c[0], c[1] + h0, c[2], c[3] - h0}, ban.size(), this.selected, new game.pokemon.ManCu.DongIcon() {
            public Object mon(int i) {
                return oMon(ban.elementAt(i));
            }

            public String chinh(int i) {
                return tenMon(ban.elementAt(i));
            }

            public String phu(int i) {
                return "Giá " + game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, giaMon(ban.elementAt(i)));
            }

            public void cham(final int i) {
                WindowInfoScr.this.selected = i;
                final Object o = ban.elementAt(i);
                Vector lenh = new Vector();
                lenh.addElement(new Command("Mua - " + game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, giaMon(o)), new IAction() {
                    public void perform() {
                        WindowInfoScr.this.selected = i;
                        if (WindowInfoScr.this.left != null && WindowInfoScr.this.left.action != null) {
                            WindowInfoScr.this.left.action.perform();
                        }
                    }
                }));
                lenh.addElement(new Command("Thông tin", new IAction() {
                    public void perform() {
                        if (o instanceof ItemInInventory) {
                            game.pokemon.TheVatPham.moXem(o, "Giá " + game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, giaMon(o)));
                        } else {
                            GemTemp gt = Res.getGemByID(((RealID) o).ID);
                            game.pokemon.HopThongBao.bao(tenMon(o), gt == null ? "" : gt.decript);
                        }
                    }
                }));
                game.pokemon.MenuNguCanh.mo(lenh, tenMon(o));
            }
        }, "Gian hàng trống");
    }

    void veCheDo(Graphics g) {
        int[] c = game.pokemon.ManCu.tam(g, this, "Chế đồ", game.pokemon.ManCu.viec(this.right), new Command[]{this.left});
        int k = game.pokemon.Bang.cach();
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU);
        int y = c[1];
        StringBuffer can = new StringBuffer("Cần: ");
        for (int i = 0; this.listCheDo != null && i < this.listCheDo.size(); i++) {
            Mineral mi = (Mineral) this.listCheDo.elementAt(i);
            can.append(i > 0 ? ", " : "").append(mi.name).append(" x").append((int) mi.number);
        }
        String[] d = game.pokemon.ChuDe.ngat(can.toString(), game.pokemon.ChuDe.PHU, c[2] - 8, 2);
        for (int i = 0; i < d.length; i++) {
            game.pokemon.ChuDe.chu(g, d[i], c[0] + 4, y + i * lh, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
        }
        y += d.length * lh + k;
        final Vector ep = this.listEp == null ? new Vector() : this.listEp;
        y += dauCot(g, new int[]{c[0], y, c[2], 0}, "Đã đặt (chạm = lấy ra)");
        int n = Math.max(6, ep.size());
        int s = game.pokemon.ManCu.hangO(g, c[0], y, c[2], Math.min(n, 8), game.pokemon.Bang.coO(), new game.pokemon.ManCu.O() {
            public String chu(int i) {
                return null;
            }

            public void cham(int i) {
                if (i < ep.size()) {
                    WindowInfoScr.this.numW = 1000;
                    WindowInfoScr.this.selected = i;
                    WindowInfoScr.this.doSelectedCheDo();
                    game.pokemon.ManCu.tiepQuanMenu("Chế đồ");
                }
            }

            public void ve(Graphics g, int i, int x, int y, int s) {
                if (i < ep.size()) {
                    game.pokemon.ManCu.veMon(g, ep.elementAt(i), x, y, s);
                }
            }
        }, "che-dat-");
        y += s + k;
        final Vector tui = this.listTemp;
        int h1 = dauCot(g, new int[]{c[0], y, c[2], 0}, "Nguyên liệu (chạm = bỏ vào)");
        game.pokemon.ManCu.danhSach(g, new int[]{c[0], y + h1, c[2], c[1] + c[3] - y - h1}, tui.size(), -1, new game.pokemon.ManCu.DongIcon() {
            public Object mon(int i) {
                return tui.elementAt(i);
            }

            public String chinh(int i) {
                return tenMon(tui.elementAt(i));
            }

            public String phu(int i) {
                return "Còn " + ((GemTemplate) tui.elementAt(i)).number;
            }

            public void cham(int i) {
                WindowInfoScr.this.numW = 1000;
                WindowInfoScr.this.selected = 2 * 1000 + i;
                WindowInfoScr.this.doSelectedCheDo();
                    game.pokemon.ManCu.tiepQuanMenu("Chế đồ");
            }
        }, "Không có nguyên liệu");
    }

    void veEpDoThu(Graphics g) {
        int[] c0 = game.pokemon.ManCu.tam(g, this, "Ép đồ thú", game.pokemon.ManCu.viec(this.right), new Command[]{this.left});
        int k = game.pokemon.Bang.cach();
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU);
        // màn rộng: ô ép bên trái, hành trang bên phải (danh sách đủ cao); màn hẹp: xếp dọc
        boolean rong = c0[2] >= 400 && c0[2] * 3 >= c0[3] * 4;
        int[] c = rong ? new int[]{c0[0], c0[1], c0[2] * 55 / 100, c0[3]} : c0;
        int[] cl = rong ? new int[]{c0[0] + c[2] + k, c0[1], c0[2] - c[2] - k, c0[3]} : null;
        int y = c[1];
        String yeu = "5 đồ thú cùng màu, cấp " + Math.max(30, lvThu - 5) + "-" + (lvThu + 5) + " (ô giữa: đồ chính) + " + (int) slNLThu + " nguyên liệu.";
        String[] d = game.pokemon.ChuDe.ngat(yeu, game.pokemon.ChuDe.PHU, c[2] - 8, 2);
        for (int i = 0; i < d.length; i++) {
            game.pokemon.ChuDe.chu(g, d[i], c[0] + 4, y + i * lh, game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
        }
        y += d.length * lh + k;
        final ItemInInventory[] dt = this.itemEpdothu;
        // màn thấp (J2ME 320x240): ô nhỏ lại để danh sách hành trang còn >= 2 hàng chạm được
        int sMuon = cl != null ? game.pokemon.Bang.coO() : Math.max(24, Math.min(game.pokemon.Bang.coO(), (c[1] + c[3] - y - lh - 4 * k - 2 * game.pokemon.ManCu.caoHang()) / 2));
        int s = game.pokemon.ManCu.hangO(g, c[0], y, c[2], 5, sMuon, new game.pokemon.ManCu.O() {
            public String chu(int i) {
                return i == 2 ? "Chính" : null;
            }

            public void cham(int i) {
                if (dt[i] != null) {
                    WindowInfoScr.this.numH = 1;
                    WindowInfoScr.this.selected = i;
                    WindowInfoScr.this.doSelectedEpDothu();
                    game.pokemon.ManCu.tiepQuanMenu("Ép đồ thú");
                }
            }

            public void ve(Graphics g, int i, int x, int y, int s) {
                game.pokemon.ManCu.veMon(g, dt[i], x, y, s);
            }
        }, "ep-do-");
        y += s + game.pokemon.ChuDe.cao(game.pokemon.ChuDe.PHU) + k;
        final Vector ep = this.listEp == null ? new Vector() : this.listEp;
        int s2 = game.pokemon.ManCu.hangO(g, c[0], y, c[2], 6, sMuon * 4 / 5, new game.pokemon.ManCu.O() {
            public String chu(int i) {
                return null;
            }

            public void cham(int i) {
                if (i < ep.size()) {
                    WindowInfoScr.this.numH = 1;
                    WindowInfoScr.this.selected = 6 + i;
                    WindowInfoScr.this.doSelectedEpDothu();
                    game.pokemon.ManCu.tiepQuanMenu("Ép đồ thú");
                }
            }

            public void ve(Graphics g, int i, int x, int y, int s) {
                if (i < ep.size()) {
                    game.pokemon.ManCu.veMon(g, ep.elementAt(i), x, y, s);
                }
            }
        }, "ep-nl-");
        y += s2 + k;
        if (cl != null) {
            c = cl;
            y = cl[1];
        }
        final Vector thu = this.listItemAnimal;
        final Vector nl = this.listTemp;
        int h1 = dauCot(g, new int[]{c[0], y, c[2], 0}, "Hành trang (chạm = bỏ vào)");
        game.pokemon.ManCu.danhSach(g, new int[]{c[0], y + h1, c[2], c[1] + c[3] - y - h1}, thu.size() + nl.size(), -1, new game.pokemon.ManCu.DongIcon() {
            Object o(int i) {
                return i < thu.size() ? thu.elementAt(i) : nl.elementAt(i - thu.size());
            }

            public Object mon(int i) {
                return o(i);
            }

            public String chinh(int i) {
                return tenMon(o(i));
            }

            public String phu(int i) {
                Object x = o(i);
                return x instanceof ItemInInventory ? "Cấp " + ((ItemInInventory) x).level : "Còn " + ((GemTemplate) x).number;
            }

            public void cham(int i) {
                WindowInfoScr.this.numH = 2;
                WindowInfoScr.this.numW = 1000;
                WindowInfoScr.this.selected = i;
                WindowInfoScr.this.doSelectedEpDothu();
                    game.pokemon.ManCu.tiepQuanMenu("Ép đồ thú");
            }
        }, "Không có đồ thú cùng màu / nguyên liệu");
    }
}
