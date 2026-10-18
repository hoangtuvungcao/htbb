package game.render.screen;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.model.Actor;
import game.model.AnimalInfo;
import game.model.Char;
import game.model.CharPartInfo;
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
import game.model.Layer;
import game.model.MainChar;
import game.model.Mineral;
import game.model.PageScr;
import game.model.PartyInfo;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.lang.reflect.Array;
import java.util.Vector;

public class MenuWindows extends Screen {
    public static Char charWearing = null;
    public static int cmdx = 0;
    public static int cmdxSubTab = 0;
    public static int cmdxTab = 0;
    public static int cmdy = 0;
    public static int cmdyParty = 0;
    public static int cmdyText = 0;
    public static int cmtoX = 0;
    public static int cmtoXSubTab = 0;
    public static int cmtoXTab = 0;
    public static int cmtoY = 0;
    public static int cmtoYParty = 0;
    public static int cmtoYText = 0;
    public static int cmvx = 0;
    public static int cmvxSubTab = 0;
    public static int cmvxTab = 0;
    public static int cmvy = 0;
    public static int cmvyParty = 0;
    public static int cmvyText = 0;
    public static int cmx = 0;
    public static int cmxLim = 0;
    public static int cmxLimSubTab = 0;
    public static int cmxLimTab = 0;
    public static int cmxSubTab = 0;
    public static int cmxTab = 0;
    public static int cmy = 0;
    public static int cmyLim = 0;
    public static int cmyLimParty = 0;
    public static int cmyLimText = 0;
    public static int cmyParty = 0;
    public static int cmyText = 0;
    public static int focusTab = 0;
    public static int focusTabChinh = 2;
    public static int focusTabCuaHang = 1;
    public static int focusTabHanhTrang = 1;
    public static byte[] index;
    public static MenuWindows me;
    private byte[] arrayKhamNgoc;
    private String[] charInfo;
    public int countFrame;
    public int countPop;
    int countTabMyitem;
    int excess;
    public int h;
    public int hKhung;
    public int hShow;
    public int hSmall;
    public int idFrame;
    Bitmap imgCharWear;
    public boolean isClick;
    public boolean isDefault;
    public boolean isPaintMoney;
    public boolean isSell;
    public boolean isShowText;
    private Screen lastScr;
    int limMyBag;
    int maxSize;
    String[] nameClan;
    public int numH;
    private int pa;
    private int paTab;
    private int pb;
    private Vector questInfo;
    public int selected;
    private String[] showText;
    int tdx;
    int tdy;
    public boolean trans;
    public int w;
    public int wKhung;
    public int wShow;
    public int wSmall;
    public int wSubTab;
    public int wTab;
    public int x0;
    public int xKhung;
    public int xShow;
    public int y0;
    public int yKhung;
    public int yShow;
    private static byte[] idWearChar = {12, -1, 0, 9, 2, 8, 1, 8, 10, 11};
    private static byte[] idWearAnimal = {18, 19, 14, 20, 15, 21, 16, 22, 17, 23};
    private static byte[][] posWearChar = {new byte[]{10, 11}, new byte[]{56, 11}, new byte[]{10, 32}, new byte[]{56, 32}, new byte[]{10, 52}, new byte[]{56, 52}, new byte[]{10, 73}, new byte[]{56, 94}, new byte[]{10, 94}, new byte[]{56, 94}};
    public static int[] posPaintWearingX = {7, 7, 7, 119, 119, 119, 119, 119, 119, 119, 7, 119, 7, 90, 7, 7, 7, 7, 7, 40};
    public static int[] posPaintWearingY = {27, 83, 57, 0, 0, 0, 0, 0, 57, 27, 114, 114, 0, 97, 27, 57, 85, 112, 0, 112};
    public int hTab = 25;
    public int numW = 7;
    // mod: thêm tab \"Auto\" đứng đầu (NqshMod.themTabAuto)
    String[] tabName = NqshMod.themTabAuto(new String[]{"Cửa hàng", "Hành trang", "Trang bị", "T.bị thú", "Kỹ năng", "Tiềm năng", "Nhiệm vụ", "Cài đặt", "Bang hội", "Khác"});
    String[] tabCuaHang = {"Gian hàng 1", "Gian hàng 2", "Gian hàng 3"};
    String[] tabSkill = {"Level 1", "Level 2", "Level 3"};
    String[] tabCaiDat = {"Bản đồ lớn", "Hướng dẫn", "Bật/tắt lời mời", "Bật/tắt giao diện", "Bật/tắt auto đánh", "Chế độ focus"};
    String[] tabKhac = {"Tin nhắn", "Kênh thế giới", "Bạn bè", "Top cao thủ", "Top đại gia", "Bảng top", "Cây thần", "Cổ vật", "Khác"};
    public boolean isLoad = false;
    public boolean isChangeTab = true;
    public Vector list = new Vector();
    public int[][] arrFrame = {new int[]{0, 1, 2}, new int[]{0, 1}};
    int xCus = -20;
    int dem = 0;
    int selectConfig = -1;
    int selectSetting = -1;
    int selectClan = -1;
    int wSkill = 30;
    boolean first = false;
    public Vector potionShop = new Vector();

    public static boolean isDegit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isWeapone(int i) {
        return i >= 3 && i <= 7;
    }

    public void initName() {
        if (GCanvas.gameScr.mainChar.idClan == -1) {
            this.nameClan = new String[]{"Top bang hội", "Đăng ký bang hội"};
            return;
        }
        if (GCanvas.gameScr.mainChar.isMaster == 0) {
            this.nameClan = new String[]{"Top bang hội", "Nhiệm vụ", "Thành viên bang hội", "Thông tin bang hội", "Tin nhắn bang hội", "Kỹ năng", "Chat toàn bang", "Quyên góp", "Giải tán bang hội"};
            if (MainChar.dissolved) {
                this.nameClan[8] = "Phục hồi bang hội";
                return;
            }
            return;
        }
        this.nameClan = new String[]{"Top bang hội", "Nhiệm vụ", "Thành viên bang hội", "Thông tin bang hội", "Tin nhắn bang hội", "Kỹ năng", "Chat toàn bang", "Quyên góp", "Rời bang"};
    }

    public static MenuWindows gI() {
        MenuWindows menuWindows = me;
        if (menuWindows != null) {
            return menuWindows;
        }
        MenuWindows menuWindows2 = new MenuWindows();
        me = menuWindows2;
        return menuWindows2;
    }

    @Override
    public void switchToMe() {
        this.lastScr = GCanvas.currentScreen;
        super.switchToMe();
        init();
        this.isChangeTab = true;
        focusTabChinh = 2;
        Khoitao();
    }

    private void Khoitao() {
        setInfo(0, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
        charWearing = GCanvas.gameScr.mainChar;
        setImageCharWear();
        setSelectTab();
        setCmdCenter();
    }

    public MenuWindows() {
        init();
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                MenuWindows.focusTabChinh = 2;
                MenuWindows.focusTabCuaHang = 1;
                MenuWindows.focusTabHanhTrang = 1;
                MenuWindows.cmxTab = 0;
                MenuWindows.cmxSubTab = 0;
                MenuWindows.cmtoXTab = 0;
                MenuWindows.cmtoXSubTab = 0;
                MenuWindows.this.selected = 0;
                MenuWindows.index = null;
                MenuWindows.this.charInfo = null;
                MenuWindows.this.closeText();
                GCanvas.gameScr.switchToMe();
                MenuWindows.this.list.removeAllElements();
            }
        });
    }

    @Override
    public void init() {
        this.selected = 0;
        this.wKhung = GCanvas.w - 20;
        this.hKhung = GCanvas.h - 60;
        this.xKhung = 10;
        this.yKhung = 20;
        int i = GCanvas.w / 4;
        this.wTab = i;
        int i2 = this.wKhung;
        this.wSubTab = i2 / 3;
        cmx = 0;
        cmtoXTab = 0;
        cmxTab = 0;
        if (this.countTabMyitem != 0) {
            this.countTabMyitem = 0;
        }
        cmxLimTab = (this.tabName.length * i) - i2;
    }

    @Override
    public void paint(Graphics graphics) {
        try {
            graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
            this.lastScr.paint(graphics);
            graphics.setColor(0);
            graphics.setAlpha(180);
            graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
            graphics.ClipRec(0, 0, GCanvas.w, GCanvas.h);
            paintTab(graphics, this.xKhung, this.yKhung, this.wKhung, this.hKhung);
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
            GCanvas.resetTrans(graphics);
            int i = this.xKhung;
            int i2 = this.yKhung;
            int i3 = this.wKhung;
            int i4 = this.hKhung;
            int i5 = i - 2;
            int i6 = i2 - 2;
            graphics.drawImage(Res.imgPopupGoc, i5, i6, 0);
            graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 2, i + 3 + i3, i6, Graphics.TOP | Graphics.RIGHT);
            graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
            int i7 = i2 + i4;
            graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 6, i5, i7 + 4, Graphics.BOTTOM | Graphics.LEFT);
            graphics.drawRegion(Res.imgPopupGoc, 0, 0, 18, 19, 3, i + i3 + 3, i7 + 3, Graphics.BOTTOM | Graphics.RIGHT);
            super.paint(graphics);
        } catch (Exception unused) {
        }
    }

    @Override
    public void update() {
        Char r0;
        this.lastScr.update();
        int i = cmxTab;
        int i2 = cmtoXTab;
        if (i != i2) {
            int i3 = (i2 - i) << 2;
            cmvxTab = i3;
            int i4 = cmdxTab + i3;
            cmxTab = i + (i4 >> 4);
            cmdxTab = i4 & 15;
        }
        int i5 = cmxSubTab;
        int i6 = cmtoXSubTab;
        if (i5 != i6) {
            int i7 = (i6 - i5) << 2;
            cmvxSubTab = i7;
            int i8 = cmdxSubTab + i7;
            cmxSubTab = i5 + (i8 >> 4);
            cmdxSubTab = i8 & 15;
        }
        int i9 = cmyParty;
        int i10 = cmtoYParty;
        if (i9 != i10) {
            int i11 = (i10 - i9) << 2;
            cmvyParty = i11;
            int i12 = cmdyParty + i11;
            cmyParty = i9 + (i12 >> 4);
            cmdyParty = i12 & 15;
        }
        int i13 = cmy;
        int i14 = cmtoY;
        if (i13 != i14) {
            int i15 = (i14 - i13) << 2;
            cmvy = i15;
            int i16 = cmdy + i15;
            cmy = i13 + (i16 >> 4);
            cmdy = i16 & 15;
        }
        int i17 = cmx;
        int i18 = cmtoX;
        if (i17 != i18) {
            int i19 = (i18 - i17) << 2;
            cmvx = i19;
            int i20 = cmdx + i19;
            cmx = i17 + (i20 >> 4);
            cmdx = i20 & 15;
        }
        int i21 = cmyText;
        int i22 = cmtoYText;
        if (i21 != i22) {
            int i23 = (i22 - i21) << 2;
            cmvyText = i23;
            int i24 = cmdyText + i23;
            cmyText = i21 + (i24 >> 4);
            cmdyText = i24 & 15;
        }
        int iAbs = Math.abs(i14 - cmy);
        char c = 0;
        if (iAbs < 15 && cmy < 0) {
            cmtoY = 0;
        }
        if (Math.abs(cmtoY - cmy) < 10) {
            int i25 = cmy;
            int i26 = cmyLim;
            if (i25 > i26) {
                cmtoY = i26;
            }
        }
        if (Math.abs(cmtoYParty - cmyParty) < 15 && cmyParty < 0) {
            cmtoYParty = 0;
        }
        int i27 = focusTabChinh == 6 ? 30 : 0;
        if (Math.abs(cmtoYParty - cmyParty) < 10) {
            int i28 = cmyParty;
            int i29 = cmyLimParty;
            if (i28 > i29 + i27) {
                cmtoYParty = i29 + i27;
            }
        }
        if (Math.abs(cmtoYText - cmyText) < 15 && cmyText < 0) {
            cmtoYText = 0;
        }
        if (Math.abs(cmtoYText - cmyText) < 10) {
            int i30 = cmyText;
            int i31 = cmyLimText;
            if (i30 > i31) {
                cmtoYText = i31;
            }
        }
        if (focusTabChinh == 4 && (r0 = charWearing) != null && r0.imgAnimal != null) {
            if (GCanvas.gameTick % charWearing.timeChangeFrame == 0) {
                int i32 = this.countFrame + 1;
                this.countFrame = i32;
                if (i32 > this.arrFrame[charWearing.numFrame == 3 ? (char) 0 : (char) 1].length - 1) {
                    this.countFrame = 0;
                }
            }
            try {
                int[][] iArr = this.arrFrame;
                if (charWearing.numFrame != 3) {
                    c = 1;
                }
                this.idFrame = iArr[c][this.countFrame];
            } catch (Exception unused) {
            }
        }
        updateTab();
        moveCameraTab();
    }

    @Override
    public void updateKey() {
        if (this.isShowText) {
            if (GCanvas.isPointerDown) {
                int i = this.x0;
                if (GCanvas.isPointer(i + 140, this.y0, this.wKhung - (i + 138), 140)) {
                    if (!this.trans) {
                        this.pa = cmyText;
                        this.trans = true;
                    }
                    int i2 = this.pa + (GCanvas.pyLast - GCanvas.py);
                    cmtoYText = i2;
                    if (i2 < -30) {
                        cmtoYText = -30;
                    }
                    int i3 = cmtoYText;
                    int i4 = cmyLimText;
                    if (i3 > i4 + 30) {
                        cmtoYText = i4 + 30;
                    }
                }
            }
            if (GCanvas.isPointerClick) {
                this.trans = false;
            }
        }
        updateKeyMain();
        super.updateKey();
    }

    private void updateKeyMain() {
        Char r1;
        Char r2;
        int i;
        if (GCanvas.isPointer(this.x0 - 10, this.y0 - 10, 140, 112) && (((i = focusTabChinh) == 1 || i == 2) && !GCanvas.menu.showMenu)) {
            int i2 = GCanvas.pyLast - GCanvas.py;
            int i3 = GCanvas.pxLast - GCanvas.px;
            if (GCanvas.isPointerDown) {
                if (!this.trans) {
                    this.pb = cmy;
                    this.trans = true;
                }
                if (Math.abs(i2) > 10) {
                    int i4 = this.pb + (GCanvas.pyLast - GCanvas.py);
                    cmtoY = i4;
                    if (i4 < -20) {
                        cmtoY = -20;
                    }
                    int i5 = cmtoY;
                    int i6 = cmyLim;
                    if (i5 > i6) {
                        cmtoY = i6;
                    }
                }
            }
            if (GCanvas.isPointerClick) {
                this.isClick = true;
                this.xCus = -20;
                this.isChangeTab = false;
                GCanvas.isPointerClick = false;
                this.trans = false;
                int i7 = ((((cmtoY + GCanvas.py) - (this.y0 - 10)) / (this.hSmall + 10)) * 5) + (((cmtoX + GCanvas.px) - (this.x0 - 10)) / (this.wSmall + 10));
                if (this.selected == i7 && Math.abs(i2) < 10 && Math.abs(i3) < 10) {
                    if (index[focusTab] == 20) {
                        doBuyShopSpecial();
                    } else {
                        doSelectedInventori();
                    }
                } else {
                    this.selected = i7;
                    if (i7 > 41) {
                        this.selected = 41;
                    }
                    setCmdCenter();
                }
            }
            int i8 = cmy;
            int i9 = cmtoY;
            if (i8 != i9) {
                int i10 = (i9 - i8) << 2;
                cmvy = i10;
                int i11 = cmdy + i10;
                cmy = i8 + (i11 >> 4);
                cmdy = i11 & 15;
            }
        }
        if (focusTabChinh == 3 && GCanvas.isPointerClick && GCanvas.px > this.x0 - 7) {
            int i12 = GCanvas.px;
            int i13 = this.x0;
            int i14 = this.wSmall;
            if (i12 <= (i13 - 7) + ((i14 + 10) * 5)) {
                if (GCanvas.isPointer((i13 - 2) + i14 + 10, (this.y0 - 13) + ((this.hSmall + 10) * 4), 28, 28)) {
                    this.selected = 4;
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                        this.center.action.perform();
                        Cout.println("");
                    }
                } else if (GCanvas.isPointer((this.x0 - 9) + ((this.wSmall + 10) * 3), (this.y0 - 13) + ((this.hSmall + 10) * 4), 28, 28)) {
                    this.selected = 1;
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                        this.center.action.perform();
                        Cout.println("");
                    }
                } else if (GCanvas.px > this.x0 - 7 && GCanvas.px <= this.x0 + 23) {
                    this.selected = (((GCanvas.py - (this.y0 - 13)) / (this.hSmall + 10)) * this.numW) + ((GCanvas.px - (this.x0 - 7)) / (this.wSmall + 10));
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                        this.center.action.perform();
                        Cout.println("");
                    }
                } else if (GCanvas.isPointer((this.x0 - 9) + ((this.wSmall + 10) * 4), this.y0 - 13, 28, (this.hSmall + 10) * 5)) {
                    this.selected = (((GCanvas.py - (this.y0 - 13)) / (this.hSmall + 10)) * this.numW) + 2;
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.center != null) {
                        this.center.action.perform();
                        Cout.println("");
                    }
                }
            }
        }
        if (focusTabChinh == 4 && GCanvas.isPointerClick) {
            if (GCanvas.px > this.x0 - 7 && GCanvas.px <= this.x0 + 23) {
                int i15 = ((GCanvas.py - (this.y0 - 13)) / (this.hSmall + 10)) * 2;
                this.selected = i15;
                if (i15 < 0) {
                    this.selected = 0;
                }
                if (this.selected > 8) {
                    this.selected = 8;
                }
                this.isClick = true;
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && (r2 = charWearing) != null && (r2.useHorse != -1 || charWearing.idhorse > -1)) {
                    showInfoWearingAnimal();
                    Cout.println("");
                }
            } else if (GCanvas.px > (this.x0 - 7) + ((this.wSmall + 10) * 4) && GCanvas.px <= this.x0 + 23 + ((this.wSmall + 10) * 4)) {
                int i16 = (GCanvas.py - (this.y0 - 13)) / (this.hSmall + 10);
                this.isClick = true;
                int i17 = (i16 * 2) + 1;
                this.selected = i17;
                if (i17 > 9) {
                    this.selected = 9;
                }
                if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && (r1 = charWearing) != null && (r1.useHorse != -1 || charWearing.idhorse > -1)) {
                    showInfoWearingAnimal();
                    Cout.println("");
                }
            }
        }
        if (focusTabChinh == 5) {
            if (GCanvas.isPointerClick && GCanvas.isPointer(this.x0 - 10, this.y0 - 10, 150, 140) && !GCanvas.menu.showMenu) {
                this.isClick = true;
                int i18 = (GCanvas.py - this.y0) / (this.hSmall + 10);
                int i19 = (GCanvas.px - (this.x0 - 10)) / this.wSmall;
                int i20 = this.selected;
                int i21 = this.numW;
                if (i20 == (i18 * i21) + i19) {
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                        doSelectedSkill();
                        Cout.println("");
                    }
                } else {
                    this.selected = (i18 * i21) + i19;
                    Cout.println("");
                }
                if (this.selected > GCanvas.gameScr.mainChar.getnSkill() - 1) {
                    this.selected = GCanvas.gameScr.mainChar.getnSkill() - 1;
                }
            }
            int i22 = this.x0;
            if (GCanvas.isPointer(i22 + 140, this.y0, this.wKhung - (i22 + 138), 140)) {
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pb = cmyParty;
                        this.trans = true;
                    }
                    int i23 = this.pb + (GCanvas.pyLast - GCanvas.py);
                    cmtoYParty = i23;
                    if (i23 < -30) {
                        cmtoYParty = -30;
                    }
                    int i24 = cmtoYParty;
                    int i25 = cmyLimParty;
                    if (i24 > i25 + 60) {
                        cmtoYParty = i25 + 60;
                    }
                }
                if (GCanvas.isPointerClick) {
                    this.trans = false;
                }
            }
        }
        if (focusTabChinh == 6) {
            if (GCanvas.isPointer(this.x0, this.y0 + 2, Res.imgBGTiemNang.getWidth(), Res.imgBGTiemNang.getHeight())) {
                int i26 = ((cmtoY + GCanvas.py) - (this.y0 + 19)) / this.hSmall;
                int i27 = ((cmtoX + GCanvas.px) - (this.x0 + 2)) / this.wSmall;
                int i28 = this.selected;
                int i29 = this.numW;
                if (i28 == (i26 * i29) + i27) {
                    this.isClick = true;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                        if (this.center != null) {
                            this.center.action.perform();
                            Cout.println("");
                        }
                        setCmdCenter();
                    }
                } else {
                    this.selected = (i26 * i29) + i27;
                }
            }
            int i30 = this.x0;
            if (GCanvas.isPointer(i30 + 140, this.y0, this.wKhung - (i30 + 138), 140)) {
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pb = cmyParty;
                        this.trans = true;
                    }
                    int i31 = this.pb + (GCanvas.pyLast - GCanvas.py);
                    cmtoYParty = i31;
                    if (i31 < -30) {
                        cmtoYParty = -30;
                    }
                    int i32 = cmtoYParty;
                    int i33 = cmyLimParty;
                    if (i32 > i33 + 30) {
                        cmtoYParty = i33 + 30;
                    }
                }
                if (GCanvas.isPointerClick) {
                    this.trans = false;
                }
            }
        }
        if (focusTabChinh == 7) {
            if (GCanvas.isPointer(this.x0 - 10, this.y0 - 10, 140, 130) && !GCanvas.menu.showMenu) {
                int i34 = GCanvas.pyLast - GCanvas.py;
                int i35 = GCanvas.pxLast;
                int i36 = GCanvas.px;
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pb = cmy;
                        this.trans = true;
                    }
                    if (Math.abs(i34) > 10) {
                        int i37 = this.pb + (GCanvas.pyLast - GCanvas.py);
                        cmtoY = i37;
                        if (i37 < -20) {
                            cmtoY = -20;
                        }
                        int i38 = cmtoY;
                        int i39 = cmyLim;
                        if (i38 > i39 + 20) {
                            cmtoY = i39 + 20;
                        }
                    }
                    this.selected = ((((cmtoY + GCanvas.py) - (this.y0 - 5)) / this.hSmall) * this.numW) + (((cmtoX + GCanvas.px) - (this.x0 - 5)) / this.wSmall);
                }
                if (GCanvas.isPointerClick) {
                    this.xCus = -20;
                    this.isChangeTab = false;
                    GCanvas.isPointerClick = false;
                    this.trans = false;
                }
                int i40 = cmy;
                int i41 = cmtoY;
                if (i40 != i41) {
                    int i42 = (i41 - i40) << 2;
                    cmvy = i42;
                    int i43 = cmdy + i42;
                    cmy = i40 + (i43 >> 4);
                    cmdy = i43 & 15;
                }
            }
            int i44 = this.x0;
            if (GCanvas.isPointer(i44 + 140, this.y0, this.wKhung - (i44 + 138), 140) && !GCanvas.menu.showMenu) {
                int i45 = GCanvas.pyLast - GCanvas.py;
                int i46 = GCanvas.pxLast - GCanvas.px;
                if (GCanvas.isPointerDown) {
                    if (!this.trans) {
                        this.pb = cmyParty;
                        this.trans = true;
                    }
                    if (Math.abs(i45) > 10) {
                        int i47 = this.pb + (GCanvas.pyLast - GCanvas.py);
                        cmtoYParty = i47;
                        if (i47 < -20) {
                            cmtoYParty = -20;
                        }
                        int i48 = cmtoYParty;
                        int i49 = cmyLim;
                        if (i48 > i49 + 20) {
                            cmtoYParty = i49 + 20;
                        }
                    }
                    int i50 = ((cmtoYParty + GCanvas.py) - this.y0) / 32;
                    if (Math.abs(i45) < 10 && Math.abs(i46) < 10) {
                        this.selected = i50;
                    }
                    if (this.selected > Char.party.size() - 1) {
                        this.selected = Char.party.size() - 1;
                    }
                }
                if (GCanvas.isPointerClick) {
                    this.xCus = -20;
                    this.isChangeTab = false;
                    GCanvas.isPointerClick = false;
                    this.trans = false;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                        doSelectedParty();
                    }
                }
                int i51 = cmyParty;
                int i52 = cmtoYParty;
                if (i51 != i52) {
                    int i53 = (i52 - i51) << 2;
                    cmvyParty = i53;
                    int i54 = cmdyParty + i53;
                    cmyParty = i51 + (i54 >> 4);
                    cmdyParty = i54 & 15;
                }
            }
        }
        if (focusTabChinh == 8) {
            if (GCanvas.isPointer(this.x0, this.y0, 140, 140)) {
                if (GCanvas.isPointerDown) {
                    this.dem++;
                    int i55 = (GCanvas.py - (this.y0 + 15)) / 25;
                    if (i55 > 3) {
                        i55 = 3;
                    }
                    this.selectConfig = i55;
                    if (Math.abs(GCanvas.pyLast - GCanvas.py) != 0) {
                        this.selectConfig = -1;
                    }
                }
                if (GCanvas.isPointerClick && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                    this.dem = 0;
                    int i56 = this.selectConfig;
                    GCanvas.gameScr.gameService.setConfig(i56 == 0 ? 3 : i56 - 1);
                    GCanvas.gameScr.switchToMe();
                    Cout.println("");
                }
            }
            int i57 = this.x0;
            if (GCanvas.isPointer(i57 + 140, this.y0, this.wKhung - (i57 + 138), 140)) {
                int i58 = GCanvas.pyLast - GCanvas.py;
                int i59 = GCanvas.pxLast - GCanvas.px;
                if (GCanvas.isPointerDown) {
                    this.dem++;
                    if (!this.trans) {
                        this.pb = cmy;
                        this.trans = true;
                    }
                    int i60 = ((cmtoY + GCanvas.py) - this.y0) / 30;
                    if (Math.abs(i58) < 10 && Math.abs(i59) < 10) {
                        this.selectSetting = i60;
                    }
                    if (Math.abs(i58) > 10) {
                        int i61 = this.pb + (GCanvas.pyLast - GCanvas.py);
                        cmtoY = i61;
                        if (i61 < -20) {
                            cmtoY = -20;
                        }
                        int i62 = cmtoY;
                        int i63 = cmyLim;
                        if (i62 > i63 + 20) {
                            cmtoY = i63 + 20;
                        }
                        this.selectSetting = -1;
                    }
                }
                if (GCanvas.isPointerClick) {
                    this.dem = 0;
                    this.xCus = -20;
                    this.isChangeTab = false;
                    GCanvas.isPointerClick = false;
                    this.trans = false;
                    if (Math.abs(i58) < 10 && Math.abs(i59) < 10) {
                        int i64 = this.selectSetting;
                        if (i64 == 0) {
                            GCanvas.gameScr.switchToMe();
                            doBigMap();
                            Cout.println("");
                        } else if (i64 == 1) {
                            GCanvas.gameScr.switchToMe();
                            HelpScr.gI().switchToMe();
                            Cout.println("");
                        } else if (i64 == 2) {
                            GCanvas.gameScr.onoffInvite = !GCanvas.gameScr.onoffInvite;
                            GameService.gI().setConfig(!GCanvas.gameScr.onoffInvite ? 4 : 5);
                            GCanvas.gameScr.switchToMe();
                            GCanvas.startOKDlg(!GCanvas.gameScr.onoffInvite ? "Đã bật chức năng nhận lời mời." : "Đã tắt chức năng nhận lời mời.");
                            Cout.println("");
                        } else if (i64 == 3) {
                            GCanvas.gameScr.switchToMe();
                            GCanvas.gameScr.isBatMiniMap = true;
                            GCanvas.gameScr.hideGUI += 2;
                            if (GCanvas.gameScr.hideGUI > 2) {
                                GCanvas.gameScr.hideGUI = 0;
                            }
                            Cout.println("");
                        } else if (i64 == 4) {
                            GCanvas.autoTab.switchToMe();
                        } else if (i64 == 5) {
                            GCanvas.autoTab.switchToMe();
                            GCanvas.autoTab.isOptionFocus = true;
                            GCanvas.autoTab.currentIndex = GameScr.typeOptionFocus;
                            GCanvas.autoTab.setInfo();
                        }
                    }
                }
                int i65 = cmy;
                int i66 = cmtoY;
                if (i65 != i66) {
                    int i67 = (i66 - i65) << 2;
                    cmvy = i67;
                    int i68 = cmdy + i67;
                    cmy = i65 + (i68 >> 4);
                    cmdy = i68 & 15;
                }
            }
        }
        if (focusTabChinh == 9 && GCanvas.isPointer(this.x0, this.y0, this.wKhung, 140)) {
            int i69 = (GCanvas.py - (this.y0 + 5)) / 25;
            int i70 = (GCanvas.px - (this.x0 - 5)) / (this.wKhung / 2);
            if (GCanvas.isPointerDown) {
                int i71 = (i69 * 2) + i70;
                this.selectClan = i71;
                String[] strArr = this.nameClan;
                if (i71 > strArr.length - 1) {
                    this.selectClan = strArr.length - 1;
                }
                if (Math.abs(GCanvas.pyLast - GCanvas.py) != 0) {
                    this.selectClan = -1;
                }
            }
            if (GCanvas.isPointerClick && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10) {
                doClan(this.selectClan);
                Cout.println("");
            }
        }
        if (focusTabChinh == 10 && GCanvas.isPointer(this.x0, this.y0, this.wKhung, 140)) {
            int i72 = (GCanvas.py - (this.y0 + 5)) / 25;
            int i73 = (GCanvas.px - (this.x0 - 5)) / (this.wKhung / 2);
            if (GCanvas.isPointerDown) {
                int i74 = (i72 * 2) + i73;
                this.selectClan = i74;
                String[] strArr2 = this.tabKhac;
                if (i74 > strArr2.length - 1) {
                    this.selectClan = strArr2.length - 1;
                }
                if (Math.abs(GCanvas.pyLast - GCanvas.py) != 0) {
                    this.selectClan = -1;
                }
            }
            if (!GCanvas.isPointerClick || Math.abs(GCanvas.pxLast - GCanvas.px) >= 10 || Math.abs(GCanvas.pyLast - GCanvas.py) >= 10) {
                return;
            }
            switch (this.selectClan) {
                case 0:
                    GCanvas.gameScr.switchToMe();
                    MessageScr.gI().switchToMe();
                    Cout.println("");
                    break;
                case 1:
                    MsgWorld.gI().switchToMe();
                    Cout.println("");
                    break;
                case 2:
                    GCanvas.gameScr.switchToMe();
                    Cout.println("");
                    try {
                        if (ListScr.gI().freindList != null && ListScr.gI().freindList.size() != 0) {
                            Vector vector = new Vector();
                            int size = ListScr.gI().freindList.size();
                            for (int i75 = 0; i75 < size; i75++) {
                                vector.addElement((Char) ListScr.gI().freindList.elementAt(i75));
                            }
                            ListScr.gI().setInfo(vector, 0, "BẠN BÈ");
                            ListScr.gI().switchToMe();
                        }
                        GCanvas.startOKDlg("Chưa có bạn");
                    } catch (Exception unused) {
                        Cout.println("Loi chua co ban be!");
                        return;
                    }
                    break;
                case 3:
                    GameService.gI().requestTopStronger_Righer(4, 0);
                    Cout.println("");
                    break;
                case 4:
                    GCanvas.gameScr.switchToMe();
                    GameService.gI().requestTopStronger_Righer(3, 0);
                    Cout.println("");
                    break;
                case 5:
                    GCanvas.gameScr.switchToMe();
                    GameService.gI().requestTopStronger_Righer(7, 0);
                    Cout.println("");
                    break;
                case 6:
                    Tree2014.gI().switchToMe(GCanvas.gameScr);
                    break;
                case 7:
                    TuBinhScr.gI().switchToMe(GCanvas.gameScr);
                    break;
                case 8:
                    GameService.gI().dosendKhacMenu();
                    break;
            }
        }
    }

    private void doClan(int i) {
        switch (i) {
            case 0:
                GCanvas.gameScr.switchToMe();
                GameService.gI().requestTopStronger_Righer(5, 0);
                break;
            case 1:
                GCanvas.gameScr.switchToMe();
                GCanvas.startWaitDlg();
                if (GCanvas.gameScr.mainChar.idClan == -1) {
                    GameService.gI().registerClan();
                } else {
                    GameService.gI().questClan(0);
                    GCanvas.gameScr.showWindow(0, false, new byte[]{27});
                    GCanvas.startWaitDlg();
                }
                break;
            case 2:
                GCanvas.gameScr.switchToMe();
                GCanvas.startWaitDlg();
                GameService.gI().requestClanList(GCanvas.gameScr.mainChar.idClan, (byte) 0);
                break;
            case 3:
                GCanvas.gameScr.switchToMe();
                GCanvas.startWaitDlg();
                GameService.gI().requestClanInfo(GCanvas.gameScr.mainChar.idClan);
                break;
            case 4:
                GCanvas.gameScr.switchToMe();
                GameService.gI().msgClanAll(null, (byte) 1, 0);
                break;
            case 5:
                GCanvas.gameScr.showWindow(0, true, new byte[]{29, 30});
                break;
            case 6:
                GCanvas.gameScr.switchToMe();
                MessageScr.gI().addTab("", "Bang hội", 1);
                MessageScr.gI().setFocusTab("Bang hội");
                MessageScr.gI().switchToMe();
                break;
            case 7:
                GCanvas.inputDlg.setInfo("Số lượng", new IAction() {
                    @Override
                    public void perform() {
                        String text = GCanvas.inputDlg.tfInput.getText();
                        if (text.equals("")) {
                            return;
                        }
                        try {
                            GameService.gI().transMoney(Integer.parseInt(text));
                            GCanvas.startOKDlg("Đã chuyển tiền thành công");
                            GCanvas.gameScr.switchToMe();
                        } catch (Exception unused) {
                            GCanvas.startOKDlg("Bạn phải nhập số");
                        }
                    }
                }, 1, 100, false);
                GCanvas.inputDlg.show();
                break;
            case 8:
                if (GCanvas.gameScr.mainChar.isMaster == 0) {
                    GCanvas.startYesNoDlg(MainChar.dissolved ? "Bạn có chắc muốn phục hồi lại bang hội không ?" : "Bạn có chắc muốn giải tán bang hội không ?", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.gameScr.switchToMe();
                            GameService.gI().dissolveCLan();
                            GCanvas.startOKDlg(MainChar.dissolved ? "Đã khôi phục lại bang hội" : "Đã giãi tán bang hội");
                            MainChar.dissolved = !MainChar.dissolved;
                            MenuWindows.this.initName();
                        }
                    });
                } else {
                    GCanvas.startYesNoDlg("Bạn có chắc muốn rời bang không ?", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().outClan();
                            GCanvas.gameScr.mainChar.idClan = (short) -1;
                            GCanvas.gameScr.switchToMe();
                            MenuWindows.this.initName();
                            GCanvas.startOKDlg("Rời bang thành công");
                        }
                    });
                }
                break;
        }
    }

    private void doBigMap() {
        final short[][] location = {new short[]{28, 105}, new short[]{55, 100}, new short[]{24, 138}, new short[]{61, 148}, new short[]{105, 76}, new short[]{80, 128}, new short[]{112, 132}, new short[]{136, 111}, new short[]{76, 60}, new short[]{129, 158}, new short[]{173, 155}, new short[]{153, 160}, new short[]{145, 60}, new short[]{127, 87}, new short[]{126, 62}, new short[]{35, 60}, new short[]{35, 60}};
        short[] mapIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 110, 111, 112, 202, 104};
        int found = 0;
        for (int k = 0; k < 17; k++) {
            if (GCanvas.gameScr.map == mapIds[k]) {
                found = k;
                break;
            }
        }
        final int id = found;
        PageScr.gI().setInfo(GCanvas.hw - 94, (GCanvas.hh - 84) - deltaY, 197, 168, Res.nameBigMap[id], null);
        PageScr.gI().indexFont = 1;
        PageScr.gI().isBigMap = true;
        PageScr.gI().left = new Command("Trở lại", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().layer = new Layer() {
            @Override
            public void paint(Graphics graphics, int i3, int i4) {
                GameData.paintIconCayThan(graphics, (short) 800, 98, 95);
                CharPartInfo charPartInfo = Res.getCharPartInfo(2, GCanvas.gameScr.mainChar.currentHead);
                short[] pos = location[id];
                charPartInfo.paint(graphics, pos[0], pos[1], 0, 1);
            }
        };
        PageScr.gI().switchToMe();
    }

    private void moveCameraTab() {
        cmxLimTab = (this.tabName.length * this.wTab) - this.wKhung;
        if (GCanvas.isPointerClick && !GCanvas.menu.showMenu && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10) {
            int i = focusTabChinh;
            if (i == 1) {
                cmtoXTab = 0;
                String[] strArr = this.tabCuaHang;
                int length = strArr.length;
                int i2 = this.wSubTab;
                int i3 = (length * i2) - this.wKhung;
                cmxLimSubTab = i3;
                int i4 = focusTabCuaHang;
                if (i4 == 1 || i4 == 2) {
                    cmtoXSubTab = 0;
                } else if (i4 == strArr.length) {
                    cmtoXSubTab = i3;
                } else {
                    cmtoXSubTab = ((i4 - 1) * i2) - (i2 + 20);
                }
            } else if (i == 2) {
                cmtoXTab = 0;
                if (GCanvas.gameScr.mainChar.limItemBag - 1 > 3) {
                    cmxLimSubTab = (GCanvas.gameScr.mainChar.limItemBag * this.wSubTab) - this.wKhung;
                } else {
                    cmxLimSubTab = GCanvas.gameScr.mainChar.limItemBag * this.wSubTab;
                }
                int i5 = focusTabHanhTrang;
                if (i5 == 1 || i5 == 2) {
                    cmtoXSubTab = 0;
                } else if (i5 == GCanvas.gameScr.mainChar.limItemBag - 1) {
                    cmtoXSubTab = cmxLimSubTab;
                } else {
                    int i6 = focusTabHanhTrang - 1;
                    int i7 = this.wSubTab;
                    cmtoXSubTab = (i6 * i7) - (i7 + 20);
                }
            } else if (i == this.tabName.length) {
                cmtoXTab = cmxLimTab;
            } else {
                int i8 = this.wTab;
                cmtoXTab = ((i - 1) * i8) - (i8 * 2);
            }
        }
        if (cmtoXTab < 0) {
            cmtoXTab = 0;
        }
        int i9 = cmtoXTab;
        int i10 = cmxLimTab;
        if (i9 > i10) {
            cmtoXTab = i10;
        }
        if (cmtoXSubTab < 0) {
            cmtoXSubTab = 0;
        }
        int i11 = cmtoXSubTab;
        int i12 = cmxLimSubTab;
        if (i11 > i12) {
            cmtoXSubTab = i12;
        }
    }

    private void updateTab() {
        int i;
        if (GCanvas.currentDialog != null || GCanvas.menu.showMenu) {
            return;
        }
        if (GCanvas.isPointerDown && GCanvas.isPointer(this.xKhung, this.yKhung, this.wKhung, this.hTab) && !GCanvas.menu.showMenu) {
            if (!this.trans) {
                this.pa = cmxTab;
                this.trans = true;
            }
            int i2 = this.pa + (GCanvas.pxLast - GCanvas.px);
            cmtoXTab = i2;
            if (i2 < -100) {
                cmtoXTab = -100;
            }
            int i3 = cmtoXTab;
            int i4 = cmxLimTab;
            if (i3 > i4 + 100) {
                cmtoXTab = i4 + 100;
            }
        }
        if (GCanvas.isPointerClick) {
            this.trans = false;
        }
        if (GCanvas.isPointer(this.xKhung, this.yKhung - 5, this.wKhung, this.hTab + 10) && GCanvas.isPointerClick && !GCanvas.menu.showMenu && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10) {
            this.selected = 0;
            this.isClick = false;
            int i5 = ((cmtoXTab + GCanvas.px) - this.xKhung) / this.wTab;
            // mod: ô 0 = tab "Auto" mở menu mod (trả -1); ô khác lùi 1 về số ô của tab gốc
            i5 = NqshMod.tabAuto(i5);
            if (i5 < 0) {
                return;
            }
            this.tdx = i5;
            focusTabChinh = i5 + 1;
            this.isChangeTab = true;
            GCanvas.endDlg();
            closeText();
            setCmdCenter();
            cmyLimParty = 0;
            Cout.println("");
            switch (focusTabChinh) {
                case 1:
                    int size = Res.shopTemplate.size();
                    int i6 = 0;
                    for (int i7 = 0; i7 < size; i7++) {
                        GemTemp gemTemp = (GemTemp) Res.shopTemplate.elementAt(i7);
                        if (gemTemp.shopType + 1 > i6) {
                            i6 = gemTemp.shopType + 1;
                        }
                    }
                    byte[] bArr = new byte[i6];
                    for (int i8 = 0; i8 < i6; i8++) {
                        bArr[i8] = 20;
                    }
                    setInfo(0, true, bArr);
                    focusTab = focusTabCuaHang - 1;
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 2:
                    focusTabHanhTrang = 1;
                    setInfo(0, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 3:
                    setInfo(1, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 4:
                    setInfo(7, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 5:
                    focusTabCuaHang = 1;
                    setInfo(3, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 6:
                    cmyLimParty = 90;
                    if (GCanvas.currentDialog != null || GCanvas.menu.showMenu) {
                        return;
                    }
                    setInfo(2, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    setInfo(4, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 7:
                    setInfo(6, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                    charWearing = GCanvas.gameScr.mainChar;
                    setImageCharWear();
                    setSelectTab();
                    setCmdCenter();
                    break;
                case 8:
                    cmyLim = (this.tabCaiDat.length * 30) - 120;
                    break;
            }
        }
        int i9 = this.xKhung;
        int i10 = this.yKhung + this.hKhung;
        int i11 = this.hTab;
        if (GCanvas.isPointer(i9, i10 - i11, this.wKhung, i11 + 5) && GCanvas.isPointerClick && (((i = focusTabChinh) == 1 || i == 5 || i == 2) && !GCanvas.menu.showMenu && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10)) {
            this.isChangeTab = true;
            GCanvas.endDlg();
            closeText();
            setCmdCenter();
            int i12 = focusTabChinh;
            if (i12 == 1) {
                focusTabCuaHang = (((cmtoXSubTab + GCanvas.px) - this.xKhung) / this.wSubTab) + 1;
                int size2 = Res.shopTemplate.size();
                int i13 = 0;
                for (int i14 = 0; i14 < size2; i14++) {
                    GemTemp gemTemp2 = (GemTemp) Res.shopTemplate.elementAt(i14);
                    if (gemTemp2.shopType + 1 > i13) {
                        i13 = gemTemp2.shopType + 1;
                    }
                }
                byte[] bArr2 = new byte[i13];
                for (int i15 = 0; i15 < i13; i15++) {
                    bArr2[i15] = 20;
                }
                setInfo(0, true, bArr2);
                focusTab = focusTabCuaHang - 1;
                setSelectTab();
                setCmdCenter();
            } else if (i12 == 2) {
                int i16 = ((cmtoXSubTab + GCanvas.px) - this.xKhung) / this.wSubTab;
                focusTabHanhTrang = i16 + 1;
                this.countTabMyitem = i16;
                if (i16 > this.limMyBag) {
                    this.countTabMyitem = 0;
                    focusTabHanhTrang = 1;
                }
                setSize(140, 112, 7, 9, 18, 18);
                this.selected = 0;
                closeText();
            } else if (i12 == 5) {
                focusTabCuaHang = (((cmtoXSubTab + GCanvas.px) - this.xKhung) / this.wSubTab) + 1;
                setInfo(3, false, new byte[]{0, 1, 2, 3, 4, 5, 6, 31});
                charWearing = GCanvas.gameScr.mainChar;
                setImageCharWear();
                setSelectTab();
                setCmdCenter();
            }
        }
        int i17 = cmxTab;
        int i18 = cmtoXTab;
        if (i17 != i18) {
            int i19 = (i18 - i17) << 2;
            cmvxTab = i19;
            int i20 = cmdxTab + i19;
            cmxTab = i17 + (i20 >> 4);
            cmdxTab = i20 & 15;
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

    private void paintCharInfo(Graphics graphics) {
        graphics.setClip(this.xShow, this.yShow + 1, this.wShow, (this.hKhung - this.y0) + 15);
        graphics.ClipRec(this.xShow, this.yShow + 1, this.wShow, (this.hKhung - this.y0) + 15);
        graphics.translate(0, -cmyParty);
        if (this.charInfo != null) {
            for (int i = 0; i < this.charInfo.length; i++) {
                Font.arialFont11.drawString(graphics, this.charInfo[i], 150, (i * 12) - 11, 0);
            }
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintTab(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        graphics.setColor(-16500172);
        graphics.fillRect(i, i2, i3, i4);
        int width = i - 7;
        int i14 = i3 + 5;
        graphics.setClip(width, i2, i14, this.hTab);
        graphics.ClipRec(width, i2, i14, this.hTab);
        while (width < i3) {
            graphics.drawImage(Res.imgBgPopup, i + width, i2 + 3, 0);
            width += Res.imgBgPopup.getWidth();
        }
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        graphics.setClip(i, i2, i3, this.hTab);
        graphics.ClipRec(i, i2, i3, this.hTab);
        graphics.translate(-cmxTab, 0);
        for (int i15 = 0; i15 < this.tabName.length; i15++) {
            if (i15 == focusTabChinh) { // mod: +1 cho tab "Auto"
                Font font = Font.normalFont[0];
                String str = "" + this.tabName[i15];
                int i16 = this.wTab;
                font.drawString(graphics, str, (((i16 / 2) + i) + (i16 * i15)) - 1, i2 + 6, 2);
            } else {
                Font font2 = Font.arialFont11;
                String str2 = "" + this.tabName[i15];
                int i17 = this.wTab;
                font2.drawString(graphics, str2, (((i17 / 2) + i) + (i17 * i15)) - 1, i2 + 6, 2);
            }
        }
        int i18 = 0;
        while (true) {
            if (i18 >= this.tabName.length - 1) {
                break;
            }
            graphics.setColor(-6185824);
            int i19 = this.wTab;
            graphics.fillRect(((i + i19) + (i19 * i18)) - 2, i2, 1, this.hTab);
            graphics.setColor(-8684933);
            int i20 = this.wTab;
            graphics.fillRect(((i + i20) + (i20 * i18)) - 1, i2, 1, this.hTab);
            i18++;
        }
        graphics.setColor(-7022096);
        graphics.setAlpha(70);
        int i21 = this.wTab;
        graphics.fillRect((((focusTabChinh + 1) * i21) + i) - i21, i2, i21, this.hTab);
        graphics.translate(cmxTab, 0);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        for (int i22 = 0; i22 < 3; i22++) {
            graphics.setColor(Res.color[i22]);
            int i23 = i + i22;
            int i24 = i22 * 2;
            int i25 = i3 - i24;
            graphics.fillRect(i23 - 1, (this.hTab - i22) + i2, i25 + 2, 1);
            graphics.setColor(Res.color[i22]);
            int i26 = i2 + i22;
            int i27 = i4 - i24;
            graphics.fillRect(i23, i26, 1, i27);
            graphics.fillRect((i3 - i22) + i, i26, 1, i27);
            graphics.fillRect(i23, i26, i25, 1);
            graphics.fillRect(i23, (i4 - i22) + i2, i25, 1);
        }
        switch (focusTabChinh) {
            case 1:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -10, this.wKhung - (this.x0 + 138), GCanvas.screenlevel == 3 ? this.hKhung - (this.y0 + 3) : this.hKhung - (this.y0 + 10));
                graphics.setColor(-2496);
                graphics.drawRect(140, -10, (this.wKhung - (this.x0 + 138)) - 1, GCanvas.screenlevel == 3 ? this.hKhung - (this.y0 + 3) : this.hKhung - (this.y0 + 10));
                if (this.isPaintMoney) {
                    Font font3 = Font.smallFontWHITE;
                    String str3 = GCanvas.gameScr.mainChar.charXu + "$";
                    int i28 = GCanvas.screenlevel == 3 ? 127 : this.wKhung - 20;
                    if (GCanvas.screenlevel == 3) {
                        i5 = this.hKhung;
                        i6 = this.y0 + 12;
                    } else {
                        i5 = this.hKhung;
                        i6 = this.y0 + 15;
                    }
                    font3.drawString(graphics, str3, i28, i5 - i6, 1);
                    Font font4 = Font.smallFont1;
                    String str4 = GCanvas.gameScr.mainChar.luong + "l";
                    i7 = GCanvas.screenlevel != 3 ? this.x0 + 117 : 2;
                    if (GCanvas.screenlevel == 3) {
                        i8 = this.hKhung;
                        i9 = this.y0 + 12;
                    } else {
                        i8 = this.hKhung;
                        i9 = this.y0 + 15;
                    }
                    font4.drawString(graphics, str4, i7, i8 - i9, 0);
                    if (GCanvas.gameScr.mainChar.luongKhoa > -1) {
                        Font.smallFont1.drawString(graphics, GCanvas.gameScr.mainChar.luongKhoa + "lk", GCanvas.screenlevel == 3 ? 56 : this.wKhung - 89, GCanvas.screenlevel == 3 ? (this.hKhung - 25) - (this.y0 + 3) : -16, 2);
                    }
                }
                GCanvas.resetTrans(graphics);
                Graphics.resetTransAndroid(graphics);
                graphics.ClipRec(0, 0, GCanvas.w, GCanvas.h);
                paintTabCuaHang(graphics, i, i2, i3, i4);
                graphics.restoreCanvas();
                graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
                break;
            case 2:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -10, this.wKhung - (this.x0 + 138), GCanvas.screenlevel == 3 ? (this.hKhung - this.y0) - 3 : (this.hKhung - this.y0) - 15);
                graphics.setColor(-2496);
                graphics.drawRect(140, -10, (this.wKhung - (this.x0 + 138)) - 1, GCanvas.screenlevel == 3 ? (this.hKhung - this.y0) - 3 : (this.hKhung - this.y0) - 16);
                Font.smallFont1.drawString(graphics, (this.countTabMyitem + 1) + "/" + ((int) GCanvas.gameScr.mainChar.limItemBag), 64, -16, 2);
                graphics.setColor(0);
                if (this.isPaintMoney) {
                    Font font5 = Font.smallFontWHITE;
                    String str5 = GCanvas.gameScr.mainChar.charXu + "$";
                    int i29 = GCanvas.screenlevel == 3 ? 127 : this.wKhung - 20;
                    if (GCanvas.screenlevel == 3) {
                        i10 = this.hKhung;
                        i11 = this.y0 + 11;
                    } else {
                        i10 = this.hKhung;
                        i11 = this.y0 + 15;
                    }
                    font5.drawString(graphics, str5, i29, i10 - i11, 1);
                    Font font6 = Font.smallFont1;
                    String str6 = GCanvas.gameScr.mainChar.luong + "l";
                    i7 = GCanvas.screenlevel != 3 ? this.x0 + 117 : 2;
                    if (GCanvas.screenlevel == 3) {
                        i12 = this.hKhung;
                        i13 = this.y0 + 12;
                    } else {
                        i12 = this.hKhung;
                        i13 = this.y0 + 15;
                    }
                    font6.drawString(graphics, str6, i7, i12 - i13, 0);
                    if (GCanvas.gameScr.mainChar.luongKhoa > -1) {
                        Font.smallFont1.drawString(graphics, GCanvas.gameScr.mainChar.luongKhoa + "lk", GCanvas.screenlevel == 3 ? 56 : this.wKhung - 89, GCanvas.screenlevel == 3 ? (this.hKhung - 25) - (this.y0 + 3) : -16, 2);
                    }
                }
                GCanvas.resetTrans(graphics);
                graphics.ClipRec(0, 0, GCanvas.w, GCanvas.h);
                paintTabHanhTrang(graphics, i, i2, i3, i4);
                graphics.restoreCanvas();
                GCanvas.resetTrans(graphics);
                break;
            case 3:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -11, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 20);
                graphics.setColor(-2496);
                graphics.drawRect(140, -11, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) + 19);
                paintCharWearing(graphics);
                GCanvas.resetTrans(graphics);
                break;
            case 4:
                GCanvas.resetTrans(graphics);
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -11, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 20);
                graphics.setColor(-2496);
                graphics.drawRect(140, -11, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) + 19);
                paintAnimalWearing(graphics);
                GCanvas.resetTrans(graphics);
                break;
            case 5:
                GCanvas.resetTrans(graphics);
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -11, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) - 2);
                graphics.setColor(-2496);
                graphics.drawRect(140, -11, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) - 3);
                GCanvas.resetTrans(graphics);
                paintTabSkill(graphics, i, i2, i3, i4);
                GCanvas.resetTrans(graphics);
                break;
            case 6:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -14, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 25);
                graphics.setColor(-2496);
                graphics.drawRect(140, -14, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) + 24);
                paintFeatures(graphics);
                paintCharInfo(graphics);
                GCanvas.resetTrans(graphics);
                break;
            case 7:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-16751521);
                graphics.fillRect(-5, -14, 140, (this.hKhung - this.y0) + 15);
                graphics.setColor(-2496);
                graphics.drawRect(-5, -14, 139, (this.hKhung - this.y0) + 14);
                graphics.setColor(-16751521);
                graphics.fillRect(140, -14, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 15);
                graphics.setColor(-2496);
                graphics.drawRect(140, -14, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) + 14);
                if (this.isPaintMoney) {
                    Font.smallFontWHITE.drawString(graphics, GCanvas.gameScr.mainChar.charXu + "$", 130, GCanvas.screenlevel + (-14) == 3 ? (this.hKhung - this.y0) + 40 : (this.hKhung - this.y0) + 10, 1);
                    Font.smallFont1.drawString(graphics, GCanvas.gameScr.mainChar.luong + "L", 2, GCanvas.screenlevel + (-14) == 3 ? (this.hKhung - this.y0) + 40 : (this.hKhung - this.y0) + 10, 0);
                    if (GCanvas.gameScr.mainChar.luongKhoa > -1) {
                        Font.smallFont1.drawString(graphics, GCanvas.gameScr.mainChar.luongKhoa + " LK", 145, GCanvas.screenlevel + (-14) == 3 ? (this.hKhung - this.y0) + 40 : (this.hKhung - this.y0) + 10, 0);
                    }
                }
                paintQuest(graphics);
                GCanvas.resetTrans(graphics);
                graphics.translate(this.x0, this.y0);
                paintParty(graphics);
                GCanvas.resetTrans(graphics);
                break;
            case 8:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-2496);
                graphics.drawRect(-5, -13, 139, (this.hKhung - this.y0) + 24);
                graphics.setColor(-2496);
                graphics.drawRect(140, -13, (this.wKhung - (this.x0 + 138)) - 1, (this.hKhung - this.y0) + 24);
                Font.normalFont[0].drawString(graphics, "Cấu hình", 0, -7, 0);
                graphics.fillRect(-5, 10, 139, 2);
                if (GCanvas.isPointerDown && this.dem > 3 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.selectConfig != -1) {
                    int i30 = this.x0 - 5;
                    int i31 = this.y0;
                    if (GCanvas.isPointer(i30, i31 - 13, 139, (this.hKhung - i31) + 24)) {
                        graphics.setColor(-12668977);
                        graphics.fillRect(0, (this.selectConfig * 25) + 20, 130, 25);
                    }
                }
                Font.arialFont11.drawString(graphics, "Rất thấp", 60, 25, 2);
                Font.arialFont11.drawString(graphics, "Thấp", 60, 50, 2);
                Font.arialFont11.drawString(graphics, "Vừa", 60, 75, 2);
                Font.arialFont11.drawString(graphics, "Cao", 60, 100, 2);
                graphics.setClip(140, -10, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 18);
                graphics.ClipRec(140, -10, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 18);
                graphics.translate(0, -cmy);
                if (GCanvas.isPointerDown && this.dem > 3 && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.selectSetting != -1) {
                    int i32 = this.x0;
                    int i33 = this.y0;
                    if (GCanvas.isPointer(i32 + 140, i33 - 10, this.wKhung - (i32 + 138), (this.hKhung - i33) + 18)) {
                        graphics.setColor(-12668977);
                        graphics.fillRect(143, (this.selectSetting * 30) - 7, (this.wKhung - (this.x0 + 138)) - 6, 30);
                    }
                }
                for (int i34 = 0; i34 < this.tabCaiDat.length; i34++) {
                    Font.normalFont[0].drawString(graphics, "" + this.tabCaiDat[i34], ((this.wKhung - (this.x0 + 138)) / 2) + 140, i34 * 30, 2);
                }
                Graphics.resetTransAndroid(graphics);
                graphics.restoreCanvas();
                GCanvas.resetTrans(graphics);
                break;
            case 9:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-2496);
                graphics.drawRect(-5, -13, (this.wKhung / 2) - 10, (this.hKhung - this.y0) + 24);
                int i35 = this.wKhung;
                graphics.drawRect((i35 / 2) - 5, -13, (i35 - (this.x0 + (i35 / 2))) + 5, (this.hKhung - this.y0) + 24);
                if (GCanvas.isPointerDown && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.selectClan != -1) {
                    int i36 = this.x0;
                    int i37 = this.y0;
                    if (GCanvas.isPointer(i36 - 5, i37 - 13, this.wKhung - i36, (this.hKhung - i37) + 24)) {
                        graphics.setColor(-12668977);
                        int i38 = this.selectClan;
                        int i39 = i38 % 2 == 0 ? -4 : -5;
                        int i40 = this.wKhung;
                        graphics.fillRect(i39 + ((i38 % 2) * (i40 / 2)), ((i38 / 2) * 25) - 4, i38 % 2 == 0 ? (i40 / 2) - 11 : (i40 - (this.x0 + (i40 / 2))) + 5, 24);
                    }
                }
                graphics.setColor(-6185824);
                for (int i41 = 0; i41 < 6; i41++) {
                    int i42 = (i41 * 25) - 5;
                    graphics.fillRect(-5, i42, (this.wKhung / 2) - 10, 1);
                    int i43 = this.wKhung;
                    graphics.fillRect((i43 / 2) - 5, i42, (i43 - (this.x0 + (i43 / 2))) + 5, 1);
                }
                for (int i44 = 0; i44 < this.nameClan.length; i44++) {
                    Font font7 = Font.normalFont[0];
                    String str7 = "" + this.nameClan[i44];
                    int i45 = this.wKhung;
                    font7.drawString(graphics, str7, (((i45 / 2) - 10) / 2) + ((i44 % 2) * ((i45 / 4) + ((i45 - (this.x0 + (i45 / 2))) / 2))), (i44 / 2) * 25, 2);
                }
                GCanvas.resetTrans(graphics);
                break;
            case 10:
                graphics.translate(this.x0, this.y0);
                graphics.setColor(-2496);
                graphics.drawRect(-5, -13, (this.wKhung / 2) - 10, (this.hKhung - this.y0) + 24);
                int i46 = this.wKhung;
                graphics.drawRect((i46 / 2) - 5, -13, (i46 - (this.x0 + (i46 / 2))) + 5, (this.hKhung - this.y0) + 24);
                if (GCanvas.isPointerDown && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10 && this.selectClan != -1) {
                    int i47 = this.x0;
                    int i48 = this.y0;
                    if (GCanvas.isPointer(i47 - 5, i48 - 13, this.wKhung - i47, (this.hKhung - i48) + 24)) {
                        graphics.setColor(-12668977);
                        int i49 = this.selectClan;
                        int i50 = i49 % 2 == 0 ? -4 : -5;
                        int i51 = this.wKhung;
                        graphics.fillRect(i50 + ((i49 % 2) * (i51 / 2)), ((i49 / 2) * 25) - 4, i49 % 2 == 0 ? (i51 / 2) - 11 : (i51 - (this.x0 + (i51 / 2))) + 5, 24);
                    }
                }
                graphics.setColor(-6185824);
                for (int i52 = 0; i52 < 5; i52++) {
                    int i53 = (i52 * 25) - 5;
                    graphics.fillRect(-5, i53, (this.wKhung / 2) - 10, 1);
                    int i54 = this.wKhung;
                    graphics.fillRect((i54 / 2) - 5, i53, (i54 - (this.x0 + (i54 / 2))) + 5, 1);
                }
                for (int i55 = 0; i55 < this.tabKhac.length; i55++) {
                    Font font8 = Font.normalFont[0];
                    String str8 = "" + this.tabKhac[i55];
                    int i56 = this.wKhung;
                    font8.drawString(graphics, str8, (((i56 / 2) - 10) / 2) + ((i55 % 2) * ((i56 / 4) + ((i56 - (this.x0 + (i56 / 2))) / 2))), (i55 / 2) * 25, 2);
                }
                GCanvas.resetTrans(graphics);
                break;
        }
        if (this.isShowText) {
            graphics.translate(this.x0, this.y0);
            paintShowText(graphics);
            GCanvas.resetTrans(graphics);
        }
    }

    private void paintTabHanhTrang(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        int i6 = i + 5;
        int i7 = i2 + i4;
        int i8 = this.hTab;
        int i9 = i3 - 10;
        graphics.setClip(i6, i7 - i8, i9, i8 - 3);
        int i10 = this.hTab;
        graphics.ClipRec(i6, i7 - i10, i9, i10 - 3);
        int width = i - 7;
        while (true) {
            if (width >= i3) {
                break;
            }
            graphics.drawImage(Res.imgBgPopup, i + width, (i7 - this.hTab) + 3, 0);
            width += Res.imgBgPopup.getWidth();
        }
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        int i11 = this.hTab;
        graphics.setClip(i6, i7 - i11, i9, i11 - 3);
        int i12 = this.hTab;
        graphics.ClipRec(i6, i7 - i12, i9, i12 - 3);
        graphics.translate(-cmxSubTab, 0);
        graphics.setColor(-7022096);
        graphics.setAlpha(70);
        int i13 = this.wSubTab;
        int i14 = ((focusTabHanhTrang * i13) + i) - i13;
        int i15 = this.hTab;
        graphics.fillRect(i14, i7 - i15, i13, i15 - 2);
        for (int i16 = 0; i16 <= this.limMyBag; i16++) {
            if (i16 == focusTabHanhTrang - 1) {
                int i17 = this.wSubTab;
                Font.normalFont[0].drawString(graphics, "Hành trang " + (i16 + 1), (((i17 / 2) + i) + (i17 * i16)) - 1, (i7 - this.hTab) + 6, 2);
            } else {
                int i18 = this.wSubTab;
                Font.arialFont11.drawString(graphics, "Hành trang " + (i16 + 1), (((i18 / 2) + i) + (i18 * i16)) - 1, (i7 - this.hTab) + 6, 2);
            }
        }
        for (int i19 = 1; i19 <= this.limMyBag + 1; i19++) {
            graphics.setColor(-6185824);
            int i20 = (this.wSubTab * i19) + i;
            int i21 = this.hTab;
            graphics.fillRect(i20, i7 - i21, 1, i21);
            graphics.setColor(-8684933);
            int i22 = (this.wSubTab * i19) + i + 1;
            int i23 = this.hTab;
            graphics.fillRect(i22, i7 - i23, 1, i23);
        }
        graphics.translate(cmxSubTab, 0);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        for (i5 = 0; i5 < 3; i5++) {
            graphics.setColor(Res.color[i5]);
            int i24 = this.hTab;
            graphics.fillRect((i + i5) - 1, i2 + (i24 - i5) + (i4 - (i24 * 2)) + 2, (i3 - (i5 * 2)) + 2, 1);
        }
        graphics.translate(this.x0, this.y0);
        switch (focusTabHanhTrang) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                paintHanhTrang(graphics, i3);
                break;
        }
        GCanvas.resetTrans(graphics);
    }

    private void paintParty(Graphics graphics) {
        int size = Char.party.size();
        graphics.setClip(140, -10, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 10);
        graphics.ClipRec(140, -10, this.wKhung - (this.x0 + 138), (this.hKhung - this.y0) + 10);
        graphics.translate(0, -cmyParty);
        if (size > 0) {
            graphics.setColor(-10328832);
            graphics.fillRect(142, this.selected * 32, this.wKhung - (this.x0 + 142), 32);
            for (int i = 0; i < size; i++) {
                PartyInfo partyInfo = (PartyInfo) Char.party.elementAt(i);
                Font.arialFont11.drawString(graphics, "NV:" + partyInfo.name, 148, i * 33, 0);
                int i2 = ((i * 2) + 1) * 16;
                Font.arialFont11.drawString(graphics, "LV: " + partyInfo.lv, 148, i2, 0);
                graphics.setColor(-1);
                graphics.fillRect(142, i2 + 15, this.wKhung - (this.x0 + 138), 1);
            }
        } else {
            Font.arialFont11.drawString(graphics, "Chưa có nhóm", 148, -7, 0);
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    public static String getInfoQuestPaint(int i) {
        String strReplace;
        if (i == 0 && GameScr.mainQuest != null) {
            strReplace = GameScr.mainQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
        } else {
            strReplace = "";
        }
        if (GameScr.subQuest != null && i == 1) {
            try {
                strReplace = GameScr.subQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
            } catch (Exception unused) {
            }
        }
        if (GameScr.clanQuest == null || i != 2) {
            return strReplace;
        }
        try {
            return GameScr.clanQuest.getInfoPaintScr().replace((char) 194, (char) 226).replace((char) 212, (char) 244);
        } catch (Exception unused2) {
            return strReplace;
        }
    }

    private void paintQuest(Graphics graphics) {
        String[] strArr;
        int length;
        String[] strArr2;
        String infoQuestPaint = getInfoQuestPaint(0);
        String infoQuestPaint2 = getInfoQuestPaint(1);
        String infoQuestPaint3 = getInfoQuestPaint(2);
        if (infoQuestPaint.equals("") && infoQuestPaint2.equals("") && infoQuestPaint3.equals("")) {
            Font.arialFont11.drawString(graphics, "Chưa nhận nhiệm vụ", 8, -7, 0);
            return;
        }
        graphics.setClip(-5, -10, 140, (this.hKhung - this.y0) + 10);
        graphics.ClipRec(-5, -10, 140, (this.hKhung - this.y0) + 10);
        graphics.translate(0, -cmy);
        String[] strArr3 = null;
        if (infoQuestPaint.equals("")) {
            strArr = null;
        } else {
            String[] strArrSplit = Util.split(infoQuestPaint, "|");
            String[] strArr4 = new String[strArrSplit.length + 1];
            strArr4[0] = GameScr.mainQuest.name;
            int i = 0;
            while (i < strArrSplit.length) {
                int i2 = i + 1;
                strArr4[i2] = strArrSplit[i];
                i = i2;
            }
            strArr = strArr4;
        }
        if (strArr != null) {
            Font.normalFont[1].drawString(graphics, strArr[0], 4, -8, 0);
            for (int i3 = 1; i3 < strArr.length; i3++) {
                Font.arialFont11.drawString(graphics, strArr[i3], 4, (i3 * 15) - 8, 0);
            }
            length = strArr.length;
        } else {
            length = 0;
        }
        if (infoQuestPaint2.equals("")) {
            strArr2 = null;
        } else {
            String[] strArrSplit2 = Util.split(infoQuestPaint2, "|");
            String[] strArr5 = new String[strArrSplit2.length + 1];
            strArr5[0] = GameScr.subQuest.name;
            int i4 = 0;
            while (i4 < strArrSplit2.length) {
                int i5 = i4 + 1;
                strArr5[i5] = strArrSplit2[i4];
                i4 = i5;
            }
            strArr2 = strArr5;
        }
        if (strArr2 != null) {
            Font.normalFont[1].drawString(graphics, strArr2[0], 4, (length * 15) - 8, 0);
            for (int i6 = 1; i6 < strArr2.length; i6++) {
                Font.arialFont11.drawString(graphics, strArr2[i6], 4, ((i6 + length) * 15) - 8, 0);
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
            Font.normalFont[1].drawString(graphics, strArr3[0], 4, (length * 15) - 8, 0);
            for (int i9 = 1; i9 < strArr3.length; i9++) {
                Font.arialFont11.drawString(graphics, strArr3[i9], 4, ((i9 + length) * 15) - 8, 0);
            }
            int length2 = strArr3.length;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintFeatures(Graphics graphics) {
        if (this.isClick) {
            if (this.selected > 4) {
                this.selected = 4;
            }
            graphics.setColor(-9498350);
            graphics.fillRect(3, (this.selected * 19) + 20, 121, 18);
        }
        graphics.drawImage(Res.imgBGTiemNang, 2, 0, 0);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.basePointLeft) + "", 107, 10, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.strength) + "", 107, 28, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.agility) + "", 107, 47, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.spirit) + "", 107, 66, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.health) + "", 107, 86, 2);
        Font.verySmallFont.drawString(graphics, ((int) GCanvas.gameScr.mainChar.luck) + "", 107, 105, 2);
    }

    private void paintSkill(Graphics graphics) {
        int i;
        Font.normalFont[0].drawString(graphics, ((int) GCanvas.gameScr.mainChar.skillPointLeft) + "", 105, -15, 2);
        if (this.isClick && GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-16187921);
            int i2 = this.selected;
            if (i2 < 5) {
                int i3 = (i2 * this.wSkill) - 9;
                int i4 = this.h;
                graphics.fillRect(i3, (i4 / 2) - 12, this.wSmall - 6, (this.hSmall + (i4 / 4)) - 3);
            } else if (i2 >= 5 && i2 < 10) {
                int i5 = this.wSkill;
                int i6 = this.h;
                graphics.fillRect(((i2 - 5) * i5) - 9, ((i6 / 2) - 12) + i5, this.wSmall - 6, (this.hSmall + (i6 / 4)) - 3);
            } else if (i2 >= 10) {
                int i7 = this.wSkill;
                int i8 = this.h;
                graphics.fillRect(((i2 - 10) * i7) - 9, ((i8 / 2) - 12) + (i7 * 2), this.wSmall - 6, (this.hSmall + (i8 / 4)) - 3);
            }
        }
        Font font = Font.smallFontWHITEShow;
        if (GCanvas.screenlevel == 1) {
            font = Font.smallFontWHITE;
        }
        Font font2 = font;
        int i9 = 0;
        while (i9 < GCanvas.gameScr.mainChar.getnSkill()) {
            if (i9 < 5) {
                if (Res.imgKhung != null) {
                    graphics.drawImage(Res.imgKhung, (this.wSkill * i9) + 3, this.h / 2, 3);
                } else {
                    graphics.setColor(-5856863);
                    graphics.fillRect((this.wSkill * i9) - 7, (this.h / 2) - 10, this.wSmall - 10, this.hSmall);
                    graphics.setColor(-10263709);
                    graphics.fillRect((this.wSkill * i9) - 6, (this.h / 2) - 9, this.wSmall - 12, this.hSmall - 2);
                }
                i = i9;
                GameData.imgSkillIcon.drawFrame(i9, (this.wSkill * i9) + 3, this.h / 2, 0, 3, graphics);
                font2.drawString(graphics, ((int) Char.skillLevelLearnt[i]) + "", (i * this.wSkill) + 11, (this.h / 2) + 4, 1);
            } else {
                i = i9;
                if (i >= 5 && i < 10) {
                    if (Res.imgKhung != null) {
                        Bitmap bitmap = Res.imgKhung;
                        int i10 = this.wSkill;
                        graphics.drawImage(bitmap, ((i - 5) * i10) + 3, (this.h / 2) + i10, 3);
                    } else {
                        graphics.setColor(-5856863);
                        int i11 = i - 5;
                        int i12 = this.wSkill;
                        graphics.fillRect((i11 * i12) - 7, ((this.h / 2) - 10) + i12, this.wSmall - 10, this.hSmall);
                        graphics.setColor(-10263709);
                        int i13 = this.wSkill;
                        graphics.fillRect((i11 * i13) - 6, ((this.h / 2) - 9) + i13, this.wSmall - 12, this.hSmall - 2);
                    }
                    FrameImage frameImage = GameData.imgSkillIcon;
                    int i14 = i - 5;
                    int i15 = this.wSkill;
                    frameImage.drawFrame(i, (i14 * i15) + 3, (this.h / 2) + i15, 0, 3, graphics);
                    String str = ((int) Char.skillLevelLearnt[i]) + "";
                    int i16 = this.wSkill;
                    font2.drawString(graphics, str, (i14 * i16) + 11, (this.h / 2) + 4 + i16, 1);
                } else if (i >= 10) {
                    if (Res.imgKhung != null) {
                        Bitmap bitmap2 = Res.imgKhung;
                        int i17 = this.wSkill;
                        graphics.drawImage(bitmap2, ((i - 10) * i17) + 3, (this.h / 2) + (i17 * 2), 3);
                    } else {
                        graphics.setColor(-5856863);
                        int i18 = i - 10;
                        int i19 = this.wSkill;
                        graphics.fillRect((i18 * i19) - 7, ((this.h / 2) - 10) + (i19 * 2), this.wSmall - 10, this.hSmall);
                        graphics.setColor(-10263709);
                        int i20 = this.wSkill;
                        graphics.fillRect((i18 * i20) - 6, ((this.h / 2) - 9) + (i20 * 2), this.wSmall - 12, this.hSmall - 2);
                    }
                    FrameImage frameImage2 = GameData.imgSkillIcon;
                    int i21 = i - 10;
                    int i22 = this.wSkill;
                    frameImage2.drawFrame(i, (i21 * i22) + 3, (this.h / 2) + (i22 * 2) + 1, 0, 3, graphics);
                    String str2 = ((int) Char.skillLevelLearnt[i]) + "";
                    int i23 = this.wSkill;
                    font2.drawString(graphics, str2, (i21 * i23) + 11, (this.h / 2) + 4 + (i23 * 2), 1);
                }
            }
            i9 = i + 1;
        }
        int i24 = this.selected;
        if (i24 < 0) {
            this.selected = 0;
        } else if (i24 > SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz].length - 1) {
            this.selected = SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz].length - 1;
        }
        String str3 = SkillManager.SKILL_NAME[GCanvas.gameScr.mainChar.clazz][this.selected];
        byte b = Char.skillLevelLearnt[this.selected];
        if (b == 9) {
            str3 = str3 + " MAX" + ((int) b);
        } else if (b >= 1) {
            str3 = str3 + " lv " + ((int) b);
        }
        String[] infoSkill = GCanvas.gameScr.mainChar.getInfoSkill(this.selected);
        graphics.setClip(this.xShow, this.yShow + 1, this.wShow, (this.hKhung - this.y0) + 15);
        graphics.ClipRec(this.xShow, this.yShow + 1, this.wShow, (this.hKhung - this.y0) + 15);
        graphics.translate(0, -cmyParty);
        String[] strArrSplitFontBStrInLine = Font.normalFont[1].splitFontBStrInLine(str3, (this.wKhung - (this.x0 + 138)) - 4);
        for (int i25 = 0; i25 < strArrSplitFontBStrInLine.length; i25++) {
            Font.normalFont[1].drawString(graphics, strArrSplitFontBStrInLine[i25], 145, (i25 * 12) - 10, 0);
        }
        int length = strArrSplitFontBStrInLine.length > 1 ? 3 + ((strArrSplitFontBStrInLine.length - 1) * 12) : 3;
        for (String str4 : infoSkill) {
            Font.arialFont11.drawString(graphics, str4, 145, length, 0);
            length += 12;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintShowText(Graphics graphics) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        byte[] bArr;
        if (focusTabChinh == 2) {
            graphics.setClip(this.xShow, this.yShow + 1, this.wShow, GCanvas.screenlevel == 3 ? (this.hKhung - this.y0) - 5 : (this.hKhung - this.y0) - 17);
            i = this.xShow;
            i2 = this.yShow + 1;
            i3 = this.wShow;
            i4 = GCanvas.screenlevel == 3 ? (this.hKhung - this.y0) - 5 : (this.hKhung - this.y0) - 17;
        } else {
            graphics.setClip(this.xShow, this.yShow + 1, this.wShow, (this.hKhung - this.y0) + 15);
            i = this.xShow;
            i2 = this.yShow + 1;
            i3 = this.wShow;
            i4 = (this.hKhung - this.y0) + 15;
        }
        graphics.ClipRec(i, i2, i3, i4);
        graphics.translate(0, -cmyText);
        int i6 = 0;
        int i7 = 0;
        while (true) {
            int length = this.showText.length;
            i5 = this.countPop;
            if (i6 >= length + i5) {
                break;
            }
            if (i6 == 1 && (bArr = this.arrayKhamNgoc) != null && bArr.length > 0) {
                for (int i8 = 0; i8 < this.arrayKhamNgoc.length; i8++) {
                    int i9 = i8 * 20;
                    graphics.drawImage(Res.imgKham, this.xShow + 12 + i9, this.yShow + 12 + i7, 3);
                    byte b = this.arrayKhamNgoc[i8];
                    if (b != -1) {
                        Res.paintGem(graphics, b, this.xShow + 12 + i9, this.yShow + 12 + i7);
                    }
                }
                i7 += 18;
            }
            if (!this.showText[i6].equals("")) {
                byte bCharAt = (byte) (this.showText[i6].charAt(0) - '0');
                boolean zIsDegit = isDegit(this.showText[i6].charAt(0));
                if (!zIsDegit) {
                    bCharAt = 0;
                }
                Font[] fontArr = Font.normalFont;
                if (bCharAt >= 6) {
                    bCharAt = 0;
                }
                fontArr[bCharAt].drawString(graphics, this.showText[i6].substring(zIsDegit ? 1 : 0), this.xShow + 4, this.yShow + 4 + i7, 0);
                i7 += 15;
            }
            i6++;
        }
        if (i5 < 0) {
            this.countPop = i5 + 1;
        }
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
    }

    private void paintTabCuaHang(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5;
        int i6 = i + 5;
        int i7 = i2 + i4;
        int i8 = this.hTab;
        int i9 = i3 - 10;
        graphics.setClip(i6, i7 - i8, i9, i8 - 3);
        int i10 = this.hTab;
        graphics.ClipRec(i6, i7 - i10, i9, i10 - 3);
        int width = i - 7;
        while (true) {
            if (width >= i3) {
                break;
            }
            graphics.drawImage(Res.imgBgPopup, i + width, (i7 - this.hTab) + 3, 0);
            width += Res.imgBgPopup.getWidth();
        }
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        int i11 = this.hTab;
        graphics.setClip(i6, i7 - i11, i9, i11 - 3);
        int i12 = this.hTab;
        graphics.ClipRec(i6, i7 - i12, i9, i12 - 3);
        graphics.translate(-cmxSubTab, 0);
        graphics.setColor(-7022096);
        graphics.setAlpha(70);
        int i13 = this.wSubTab;
        int i14 = (i + (focusTabCuaHang * i13)) - i13;
        int i15 = this.hTab;
        graphics.fillRect(i14, i7 - i15, i13, i15 - 2);
        for (int i16 = 0; i16 < this.tabCuaHang.length; i16++) {
            if (i16 == focusTabCuaHang - 1) {
                Font font = Font.normalFont[0];
                String str = "" + this.tabCuaHang[i16];
                int i17 = this.wSubTab;
                font.drawString(graphics, str, ((i + (i17 / 2)) + (i17 * i16)) - 1, (i7 - this.hTab) + 6, 2);
            } else {
                Font font2 = Font.arialFont11;
                String str2 = "" + this.tabCuaHang[i16];
                int i18 = this.wSubTab;
                font2.drawString(graphics, str2, ((i + (i18 / 2)) + (i18 * i16)) - 1, (i7 - this.hTab) + 6, 2);
            }
        }
        for (int i19 = 1; i19 < this.tabCuaHang.length; i19++) {
            graphics.setColor(-6185824);
            int i20 = i + (this.wSubTab * i19);
            int i21 = this.hTab;
            graphics.fillRect(i20, i7 - i21, 1, i21);
            graphics.setColor(-8684933);
            int i22 = i + (this.wSubTab * i19) + 1;
            int i23 = this.hTab;
            graphics.fillRect(i22, i7 - i23, 1, i23);
        }
        graphics.translate(cmxSubTab, 0);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        for (i5 = 0; i5 < 3; i5++) {
            graphics.setColor(Res.color[i5]);
            int i24 = this.hTab;
            graphics.fillRect((i + i5) - 1, i2 + (i24 - i5) + (i4 - (i24 * 2)) + 2, (i3 - (i5 * 2)) + 2, 1);
        }
        graphics.translate(this.x0, this.y0);
        int i25 = focusTabCuaHang;
        if (i25 == 1 || i25 == 2 || i25 == 3) {
            paintHanhTrang(graphics, i3);
        }
        GCanvas.resetTrans(graphics);
    }

    private void paintTabSkill(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5 = i + 5;
        int i6 = i2 + i4;
        int i7 = this.hTab;
        int i8 = i3 - 10;
        graphics.setClip(i5, i6 - i7, i8, i7 - 3);
        int i9 = this.hTab;
        graphics.ClipRec(i5, i6 - i9, i8, i9 - 3);
        for (int width = i - 7; width < i3; width += Res.imgBgPopup.getWidth()) {
            graphics.drawImage(Res.imgBgPopup, i + width, (i6 - this.hTab) + 3, 0);
        }
        graphics.restoreCanvas();
        GCanvas.resetTrans(graphics);
        int i10 = this.hTab;
        graphics.setClip(i5, i6 - i10, i8, i10 - 3);
        int i11 = this.hTab;
        graphics.ClipRec(i5, i6 - i11, i8, i11 - 3);
        graphics.translate(-cmxSubTab, 0);
        graphics.setColor(-7022096);
        graphics.setAlpha(70);
        int i12 = this.wSubTab;
        int i13 = (i + (focusTabCuaHang * i12)) - i12;
        int i14 = this.hTab;
        graphics.fillRect(i13, i6 - i14, i12, i14 - 2);
        for (int i15 = 0; i15 < this.tabSkill.length; i15++) {
            if (i15 == focusTabCuaHang - 1) {
                Font font = Font.normalFont[0];
                String str = "" + this.tabSkill[i15];
                int i16 = this.wSubTab;
                font.drawString(graphics, str, ((i + (i16 / 2)) + (i16 * i15)) - 1, (i6 - this.hTab) + 6, 2);
            } else {
                Font font2 = Font.arialFont11;
                String str2 = "" + this.tabSkill[i15];
                int i17 = this.wSubTab;
                font2.drawString(graphics, str2, ((i + (i17 / 2)) + (i17 * i15)) - 1, (i6 - this.hTab) + 6, 2);
            }
        }
        for (int i18 = 1; i18 < this.tabSkill.length; i18++) {
            graphics.setColor(-6185824);
            int i19 = i + (this.wSubTab * i18);
            int i20 = this.hTab;
            graphics.fillRect(i19, i6 - i20, 1, i20);
            graphics.setColor(-8684933);
            int i21 = i + (this.wSubTab * i18) + 1;
            int i22 = this.hTab;
            graphics.fillRect(i21, i6 - i22, 1, i22);
        }
        graphics.translate(cmxSubTab, 0);
        Graphics.resetTransAndroid(graphics);
        graphics.restoreCanvas();
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        for (int i23 = 0; i23 < 3; i23++) {
            graphics.setColor(Res.color[i23]);
            int i24 = this.hTab;
            graphics.fillRect((i + i23) - 1, i2 + (i24 - i23) + (i4 - (i24 * 2)) + 2, (i3 - (i23 * 2)) + 2, 1);
        }
        graphics.translate(this.x0, this.y0);
        Font.normalFont[0].drawString(graphics, "Điểm kỹ năng: ", 60, -15, 2);
        graphics.setColor(-4868683);
        graphics.fillRect(-5, -2, 138, 1);
        if (focusTabCuaHang == 1) {
            paintSkill(graphics);
        }
        GCanvas.resetTrans(graphics);
    }

    private void paintHanhTrang(Graphics graphics, int i) {
        try {
            this.isDefault = true;
            graphics.setClip(-10, -10, ((this.wSmall + 10) * 5) + 10, (this.hSmall + 10) * 4);
            graphics.ClipRec(-10, -10, ((this.wSmall + 10) * 5) + 10, (this.hSmall + 10) * 4);
            graphics.translate(0, -cmy);
            int i2 = cmy;
            int i3 = this.hSmall;
            int i4 = i2 / (i3 + 10);
            if (i4 < 0) {
                i4 = 0;
            }
            int i5 = ((i + 20) / (i3 + 10)) + i4;
            if (index[focusTab] == 0 && i5 > 42) {
                i5 = 42;
            }
            while (i4 < i5) {
                for (int i6 = 0; i6 < 5; i6++) {
                    paintGrid(graphics, ((this.wSmall + 10) * i6) - 7, ((this.hSmall + 10) * i4) - 10);
                }
                i4++;
            }
            if (!GCanvas.isPointerDown && this.isClick && Math.abs(GCanvas.pyLast - GCanvas.py) < 10 && Math.abs(GCanvas.pxLast - GCanvas.px) < 10) {
                graphics.setColor(-6181426);
                int i7 = this.selected;
                int i8 = this.wSmall;
                int i9 = this.hSmall;
                graphics.fillRect(((i7 % 5) * (i8 + 10)) - 5, ((i7 / 5) * (i9 + 10)) - 8, i8 + 7, i9 + 7);
            }
            if (index[focusTab] != 0) {
                paintProduct(graphics);
            } else {
                paintBag(graphics);
            }
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
        } catch (Exception unused) {
        }
    }

    public void setCurTab() {
        if (GCanvas.currentScreen == gI() && focusTabChinh == 2) {
            setSelectTab();
            this.isChangeTab = false;
        }
    }

    public void setSelectTab() {
        this.left = null;
        this.isPaintMoney = false;
        this.list.removeAllElements();
        this.x0 = this.xKhung + 15;
        this.y0 = this.yKhung + this.hTab + 20;
        byte b = index[focusTab];
        if (b == 0) {
            this.isDefault = true;
            this.isPaintMoney = true;
            setInfo(getInventori());
            setSize(140, this.isDefault ? 112 : 90, 7, 9, 18, 18);
            if (GCanvas.screenlevel == 1) {
                this.left = new Command("Chọn", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.doSelectedInventori();
                    }
                });
                return;
            }
            return;
        }
        if (b == 1) {
            setSize(117, 104, 3, 5, 18, 18);
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
            if (GCanvas.gameScr.mainChar.isDoing) {
                vector.addElement(new Command("Cất cuốc", new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.startYesNoDlg("Bạn muốn cất cuốc vào rương không ?", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().unRideHorse((byte) 2);
                                GCanvas.endDlg();
                                GCanvas.gameScr.switchToMe();
                            }
                        });
                    }
                }));
            }
            if (vector.size() == 1) {
                this.left = (Command) vector.elementAt(0);
                return;
            } else {
                if (vector.size() == 2) {
                    this.left = new Command("Chọn", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.menu.startAt(vector, 0);
                        }
                    });
                    return;
                }
                return;
            }
        }
        if (b == 2) {
            setSize(124, 94, 1, 5, 125, 19);
            this.isPaintMoney = true;
            return;
        }
        if (b == 3) {
            this.left = null;
            setSize(114, 30, 5, 2, 30, 20);
            return;
        }
        if (b == 6) {
            this.isPaintMoney = true;
            setQuestInfo();
            setSize(134, 190, 1, this.questInfo.size(), 130, 15);
            this.questInfo.removeAllElements();
            if (GCanvas.gameScr.mainChar.receiveQuest) {
                this.left = new Command("Hủy", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().receiveQuest(0, 0, 3);
                        MenuWindows.this.questInfo.removeAllElements();
                    }
                });
            }
            Cout.println("day ==============================");
            return;
        }
        if (b == 20) {
            this.isPaintMoney = true;
            setSize(140, 112, 1, 1, 18, 18);
            getShopTemplate();
            setInfo(this.list);
            if (GCanvas.screenlevel == 1) {
                this.left = new Command("Mua", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.doBuyShopSpecial();
                    }
                });
                return;
            }
            return;
        }
        if (b != 28) {
            if (b != 31) {
                return;
            }
            setSize(117, 104, 3, 5, 18, 18);
        } else {
            int size = WindowInfoScr.gI().listEp.size() / 2;
            setSize(144, 144, size < 6 ? 6 : size, 3, 24, 45);
            this.center = new Command("Chọn", new IAction() {
                @Override
                public void perform() {
                    MenuWindows.this.doSelectedCheDo();
                }
            });
            this.left = new Command("Xong", new IAction() {
                @Override
                public void perform() {
                    MenuWindows.this.doLeftCheDo();
                }
            });
        }
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

    public static int getIndex() {
        return index[focusTab];
    }

    public void setCmdCenter() {
        this.charInfo = null;
        switch (focusTabChinh) {
            case 1:
            case 2:
                int i = this.selected + (index[focusTab] == 0 ? this.countTabMyitem * 42 : 0);
                this.center = null;
                if (i < this.list.size()) {
                    this.center = (Command) this.list.elementAt(i);
                    this.center.action.perform();
                }
                break;
            case 3:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.showCharWearingInfo();
                    }
                });
                break;
            case 6:
                this.center = new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.dofireBasePoint();
                    }
                });
                setTextCharInfo();
                break;
            case 7:
                this.left = new Command("Hủy Nv", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.doSelectedQuest();
                    }
                });
                break;
            case 8:
            case 9:
                this.left = null;
                break;
        }
    }

    protected void doSelectedParty() {
        try {
            if (Char.party.size() > 0) {
                MainChar mainChar = GCanvas.gameScr.mainChar;
                Vector vector = new Vector();
                if (mainChar.ID == mainChar.IDMasterParty) {
                    vector.addElement(new Command("Đuổi", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.gameScr.gameService.kickOutParty(((PartyInfo) Char.party.elementAt(MenuWindows.this.selected)).id, (byte) 0);
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
        } catch (Exception unused) {
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

    private void setTextCharInfo() {
        this.charInfo = new String[17];
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
        this.charInfo[14] = "Điểm hoạt động: " + mainChar.hoatdong + " điểm.";
        this.charInfo[15] = "Điểm đấu trường: " + mainChar.pArena + " điểm.";
        this.charInfo[16] = mainChar.nameHusband_wife.equals("") ? "Độc thân" : mainChar.nameHusband_wife;
        this.numH = this.charInfo.length + 1;
        setLim();
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
                            GameService.gI().addBasePoint(MenuWindows.this.selected, i);
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

    public boolean isBuffSkill() {
        byte[][] bArr = {new byte[]{4, 5}, new byte[]{4, 5}, new byte[]{4, 5, 6, 7}, new byte[]{4, 5}, new byte[]{4, 5}};
        for (int i = 0; i < bArr[GCanvas.gameScr.mainChar.clazz].length; i++) {
            if (this.selected == bArr[GCanvas.gameScr.mainChar.clazz][i]) {
                return true;
            }
        }
        return false;
    }

    private void doSelectedSkill() {
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
                    if (Char.skillLevelLearnt[MenuWindows.this.selected] > 0) {
                        GCanvas.menu.showMenu = false;
                        byte b = SkillManager.EFF_BUFF_SKILL[GCanvas.gameScr.mainChar.clazz][MenuWindows.this.selected - 4];
                        int skillMP = SkillManager.getSkillMP(MenuWindows.this.selected, Char.skillLevelLearnt[MenuWindows.this.selected - 4], GCanvas.gameScr.mainChar.clazz);
                        if (skillMP > GCanvas.gameScr.mainChar.mp) {
                            if (MenuWindows.this.first) {
                                return;
                            }
                            GCanvas.gameScr.addChat(new ChatInfo("", "Không đủ MP", 0));
                            MenuWindows.this.first = true;
                            return;
                        }
                        MenuWindows.this.first = false;
                        if (GCanvas.gameScr.focusedActor != null && GCanvas.gameScr.focusedActor.catagory == 0) {
                            if (MenuWindows.this.selected == 6) {
                                GameService.gI().useBuff(GCanvas.gameScr.focusedActor.ID, (byte) 0, b, (short) 0);
                                GCanvas.gameScr.mainChar.mp -= skillMP;
                                return;
                            }
                            GCanvas.gameScr.mainChar.mp -= skillMP;
                            if (zIsBuffSkill && (GCanvas.gameScr.indexBuff == -1 || GCanvas.gameScr.indexBuff == MenuWindows.this.selected)) {
                                GCanvas.gameScr.indexBuff = MenuWindows.this.selected;
                            }
                            MenuWindows.this.doCastBuffToActor(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, b, SkillManager.SKILL_CAN_BUFF_TO_USER[GCanvas.gameScr.mainChar.clazz][MenuWindows.this.selected + (-4)] == 1);
                            return;
                        }
                        if (MenuWindows.this.selected != 6) {
                            GCanvas.gameScr.mainChar.mp -= skillMP;
                            if (zIsBuffSkill && (GCanvas.gameScr.indexBuff == -1 || GCanvas.gameScr.indexBuff == MenuWindows.this.selected)) {
                                GCanvas.gameScr.indexBuff = MenuWindows.this.selected;
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
                    if (Char.skillLevelLearnt[MenuWindows.this.selected] > 0) {
                        MenuWindows menuWindows = MenuWindows.this;
                        menuWindows.doGiveSkillToQuickSlot(menuWindows.selected, zIsBuffSkill);
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
                } else if (GCanvas.gameScr.mainChar.level < SkillManager.getLvAddSkill(GCanvas.gameScr.mainChar.clazz, MenuWindows.this.selected, Char.skillLevelLearnt[MenuWindows.this.selected])) {
                    GCanvas.startOKDlg("Chưa đủ cấp độ để cộng. Xin đánh lên level yêu cầu để có thể cộng.");
                } else {
                    GameService.gI().addSkillPoint(MenuWindows.this.selected);
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

    private void showInfoWearingAnimal() {
        try {
            int size = charWearing.wearingAnimal.size();
            if (charWearing.wearingAnimal == null || size <= 0) {
                return;
            }
            for (int i = 0; i < size; i++) {
                ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingAnimal.elementAt(i);
                if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).type == idWearAnimal[this.selected]) {
                    byte[] bArr = posWearChar[0];
                    showItemInventoryAnimalInfo(itemInInventory, false, bArr[0] + 5, bArr[1] - 2);
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }

    public void showCharWearingInfo() {
        try {
            if (this.selected % 3 != 1 && !charWearing.isDoing) {
                int i = this.selected;
                int i2 = ((i / 3) * 2) + ((i % 3) - (i % 3 > 0 ? 1 : 0));
                int size = charWearing.wearingItems.size();
                int i3 = 0;
                for (int i4 = 0; i4 < size; i4++) {
                    ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingItems.elementAt(i4);
                    ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
                    short s = item.type;
                    byte b = idWearChar[i2];
                    if (s == b || (b == -1 && item.type > 2 && item.type < 8)) {
                        i3++;
                        if (i2 != 7 || i3 != 1) {
                            byte[] bArr = posWearChar[i2];
                            showItemInventoryInfo(itemInInventory, false, bArr[0] + 5, bArr[1] - 2);
                            return;
                        }
                    }
                }
                return;
            }
            if (charWearing.isDoing) {
                int i5 = this.selected;
                if (i5 != 4) {
                    int i6 = ((i5 / 3) * 2) + ((i5 % 3) - (i5 % 3 > 0 ? 1 : 0));
                    int size2 = charWearing.wearingItems.size();
                    for (int i7 = 0; i7 < size2; i7++) {
                        ItemInInventory itemInInventory2 = (ItemInInventory) charWearing.wearingItems.elementAt(i7);
                        if (Res.getItem(itemInInventory2.charClazz, itemInInventory2.idTemplate).type == 13) {
                            byte[] bArr2 = posWearChar[i6];
                            showItemInventoryInfo(itemInInventory2, false, bArr2[0] + 5, bArr2[1] - 2);
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
                            MenuWindows.this.setShowText(ItemInInventory.setTemp(MenuWindows.charWearing.infoAnimal, "0"), 80, 120);
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
                                MenuWindows.this.right.action.perform();
                            }
                            GCanvas.endDlg();
                        }
                    }));
                    GCanvas.menu.startAt(vector, 3);
                    return;
                }
                int size3 = charWearing.wearingItems.size();
                for (int i8 = 0; i8 < size3; i8++) {
                    ItemInInventory itemInInventory3 = (ItemInInventory) charWearing.wearingItems.elementAt(i8);
                    if (Res.getItem(itemInInventory3.charClazz, itemInInventory3.idTemplate).type == 19) {
                        byte[] bArr3 = posWearChar[9];
                        showItemInventoryInfo(itemInInventory3, false, bArr3[0] + 5, bArr3[1] - 2);
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
                        if (MenuWindows.charWearing.infoAnimal.equals("")) {
                            return;
                        }
                        MenuWindows.this.setShowText(ItemInInventory.setTemp(MenuWindows.charWearing.infoAnimal, "0"), 80, 120);
                    }
                }));
                vector2.addElement(new Command("Thú cưng", new IAction() {
                    @Override
                    public void perform() {
                        if (MenuWindows.charWearing.myPet != null) {
                            MenuWindows.this.setShowText(ItemInInventory.setTemp(MenuWindows.charWearing.myPet.infoPet, "0") + ItemInInventory.setTemp("Còn lại: " + (((long) MenuWindows.charWearing.myPet.totalTime) - ((System.currentTimeMillis() - MenuWindows.charWearing.myPet.timeStart) / 60000)) + " phút", "0"), 80, 120);
                        }
                    }
                }));
                GCanvas.menu.startAt(vector2, 3);
                return;
            }
            int size4 = charWearing.wearingItems.size();
            for (int i9 = 0; i9 < size4; i9++) {
                ItemInInventory itemInInventory4 = (ItemInInventory) charWearing.wearingItems.elementAt(i9);
                if (Res.getItem(itemInInventory4.charClazz, itemInInventory4.idTemplate).type == 19) {
                    byte[] bArr4 = posWearChar[9];
                    showItemInventoryInfo(itemInInventory4, false, bArr4[0] + 5, bArr4[1] - 2);
                    return;
                }
            }
        } catch (Exception unused) {
            Cout.println("Ngoai mang");
        }
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
                        MenuWindows.this.list.removeElementAt(MenuWindows.this.selected);
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
                        MenuWindows.this.list.removeElementAt(MenuWindows.this.selected);
                        GCanvas.gameScr.mainChar.potions[i] = 0;
                        GCanvas.currentDialog = null;
                    }
                });
            }
        }));
        GCanvas.menu.startAt(vector, 2);
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
            focusTabChinh = 2;
            updateTab();
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
                        MenuWindows.this.list.removeElementAt(i);
                    }
                });
            } else {
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
            }
            updateTab();
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
                        MenuWindows.this.list.removeElementAt(i);
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
                        MenuWindows.this.doUseItem(itemInInventory);
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
                                MenuWindows.this.list.removeElementAt(i);
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
                    MenuWindows.this.list.removeElementAt(i);
                }
            });
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
                        MenuWindows menuWindows = MenuWindows.this;
                        menuWindows.setShowText(str, (i % menuWindows.numW) * 18, (i / MenuWindows.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i2, int i3) {
                        GameData.paintIcon(graphics, (short) (Res.getShopByID(gemTemp.id).idImage + 5500), i2, i3);
                        Font.smallFontWHITE.drawString(graphics, ((int) (gemTemp.id > 1 ? Res.getShopByID(gemTemp.id).value : (short) 1)) + "", i2 + 13, i3 + 6, 1);
                    }
                });
            }
        }
    }

    public void doBuyShopSpecial() {
        closeText();
        int size = Res.shopTemplate.size();
        if (this.selected >= size) {
            return;
        }
        if (GCanvas.gameScr.mainChar.isFullInventory()) {
            GCanvas.startOKDlg("Hành trang đã đầy.");
            return;
        }
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            GemTemp gemTemp = (GemTemp) Res.shopTemplate.elementAt(i2);
            if (gemTemp.shopType == focusTab && gemTemp.isSell) {
                if (i == this.selected) {
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

    private void setLim() {
        int i = this.hSmall;
        int i2 = (this.numW * this.wSmall) - this.w;
        cmxLim = i2;
        if (this.isDefault) {
            i += 10;
        }
        int i3 = (this.numH * i) - this.h;
        cmyLim = i3;
        if (i2 < 0) {
            cmxLim = 0;
        }
        if (i3 < 0) {
            cmyLim = 0;
        }
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

    public void setInfo(int i, boolean z, byte[] bArr) {
        try {
            this.isSell = z;
            index = bArr;
            focusTab = i;
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
        if (index[focusTab] == 3) {
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

    private void paintBag(Graphics graphics) {
        Command command;
        try {
            int i = (cmy / (this.hSmall + 10)) * 5;
            if (i < 0) {
                i = 0;
            }
            int i2 = i + 45;
            Vector vector = this.list;
            if (vector != null && vector.size() > 0) {
                int i3 = GCanvas.gameScr.mainChar.limItemBag - 1;
                this.limMyBag = i3;
                if (i3 <= 0) {
                    this.limMyBag = 0;
                }
                this.excess = this.list.size() % 42;
                this.maxSize = this.list.size();
            }
            if (i2 > 42) {
                i2 = 42;
            }
            if ((this.countTabMyitem * 42) + 42 > this.maxSize) {
                i2 = this.excess;
            }
            while (i < i2 && !this.isLoad) {
                int i4 = (this.countTabMyitem * 42) + i;
                if (i4 < this.list.size() && (command = (Command) this.list.elementAt(i4)) != null) {
                    int i5 = this.wSmall;
                    int i6 = this.hSmall;
                    command.paint(graphics, (((i % 5) * (i5 + 10)) + ((i5 + 10) / 2)) - 7, (((i / 5) * (i6 + 10)) + ((i6 + 10) / 2)) - 10);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println("ERROR hanh trang ");
        }
    }

    protected void showShopItemInfo(short s, int i) {
        GemTemp shopByID = Res.getShopByID(s);
        String str = ItemInInventory.setTemp(shopByID.name, "0") + ItemInInventory.setTemp(shopByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + GCanvas.getMoneys(shopByID.price / 5), "0");
        }
        int i2 = this.numW;
        setShowText(str, (i % i2) * 18, (i / i2) * 18);
    }

    protected void showGemItemInfo(short s, int i, int i2) {
        GemTemp gemByID = Res.getGemByID(s);
        String str = ItemInInventory.setTemp(gemByID.name, "0") + ItemInInventory.setTemp(gemByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + (gemByID.price / 5), "0");
        }
        setShowText(str, i, i2);
    }

    protected void showGemItemKhoaInfo(short s, int i, int i2) {
        GemTemp gemByID = Res.getGemByID(s);
        String str = (ItemInInventory.setTemp(gemByID.name, "0") + " - Đã khóa") + ItemInInventory.setTemp(gemByID.decript, "0");
        if (this.isSell) {
            str = str + ItemInInventory.setTemp("Giá bán lại: " + (gemByID.price / 5), "0");
        }
        setShowText(str, i, i2);
    }

    public String getInfoItemQuest(int i) {
        return ("0" + ItemQuest.NAME_ITEM[i]) + "\nSố lượng: " + ((int) MainChar.itemQuest[i]);
    }

    public String getPotionInfo(int i) {
        short s;
        GemTemp gemTemp;
        if (i >= 7 && i < 14) {
            if (i >= 7 && i < 14) {
                return ("0" + MainChar.listPotion[i].name) + "\nSố lượng: " + GCanvas.gameScr.mainChar.potions[i];
            }
            return Res.PORTION_ITEM_NAME_CLAN + "\n Số lượng: " + GCanvas.gameScr.mainChar.potions[i];
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
            if (gemTemp != null && (i < 69 || i > 77)) {
                return str + ItemInInventory.setTemp(gemTemp.decript, "0");
            }
        } else {
            try {
                StringBuilder sbAppend = new StringBuilder().append(str);
                StringBuilder sb = new StringBuilder("\nHồi phục: ");
                int i3 = i - 19;
                if (i3 < 0) {
                    s = Res.potionTemplates[i].recovered;
                } else {
                    s = Res.potionTemplates[i3].recovered;
                }
                return sbAppend.append(ItemInInventory.setTemp(sb.append((int) s).append(" ").append(MainChar.listPotion[i].name2).toString(), "0")).toString();
            } catch (Exception unused) {
            }
        }
        return str;
    }

    public int getCountPotion(int i) {
        return GCanvas.gameScr.mainChar.potions[i];
    }

    public Vector getInventori() {
        Vector vector = new Vector();
        try {
            final Font font = Font.smallFontWHITE;
            int slot = 0;
            for (int p = 1; p < MainChar.listPotion.length; p++) {
                if (getCountPotion(p) > 0) {
                    final int potion = p;
                    final int ii = slot;
                    vector.addElement(new Command("", new IAction() {
                        @Override
                        public void perform() {
                            MenuWindows menuWindows = MenuWindows.this;
                            menuWindows.setShowText(menuWindows.getPotionInfo(potion), (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                        }
                    }) {
                        @Override
                        public void paint(Graphics graphics, int i4, int i5) {
                            Res.paintPotion(graphics, MainChar.listPotion[potion].index, i4, i5, 3);
                            font.drawString(graphics, MenuWindows.this.getCountPotion(potion) + "", i4 + 13, i5 + 6, 1);
                        }
                    });
                    slot++;
                }
            }
            int size = MainChar.shopItem.size();
            for (int i4 = 0; i4 < size; i4++) {
                final GemTemplate gemTemplate = (GemTemplate) MainChar.shopItem.elementAt(i4);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.showShopItemInfo(gemTemplate.id, ii);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i5, int i6) {
                        Res.paintPotion(graphics, Res.getShopByID(gemTemplate.id).idImage, i5, i6, 3);
                        font.drawString(graphics, gemTemplate.number + "", i5 + 13, i6 + 6, 1);
                    }
                });
                slot++;
            }
            for (int i6 = 0; i6 < MainChar.gemItemKhoa.size(); i6++) {
                final GemTemplate gemTemplate2 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i6);
                final GemTemp gemByID = Res.getGemByID(gemTemplate2.id);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.showGemItemKhoaInfo(gemTemplate2.id, (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i8, int i9) {
                        if (Res.getGemByID(gemTemplate2.id) != null) {
                            if (ii - (MenuWindows.index[MenuWindows.focusTab] == 0 ? MenuWindows.this.countTabMyitem * 42 : 0) != MenuWindows.this.selected || Math.abs(GCanvas.px - GCanvas.pxLast) > 10 || Math.abs(GCanvas.py - GCanvas.pyLast) > 10) {
                                graphics.setColor(-13500365);
                                graphics.fillRect(i8 - 12, i9 - 12, 25, 25);
                            }
                            Res.paintGemKhoa(graphics, Res.getGemByID(gemTemplate2.id).idImage, i8, i9);
                            if (gemByID.isEff != -1) {
                                Res.paintRectColor(graphics, i8 - 9, i9 - 9, 17, 17, gemByID.count, GemTemp.color[gemByID.isEff], -260847);
                                gemByID.count += 3;
                                if (gemByID.count > 68) {
                                    gemByID.count = 0;
                                }
                            }
                            font.drawString(graphics, gemTemplate2.number + "", i8 + 13, i9 + 6, 1);
                        }
                    }
                });
                slot++;
            }
            int size2 = MainChar.gemItem.size();
            for (int i8 = 0; i8 < size2; i8++) {
                final GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItem.elementAt(i8);
                final GemTemp gemByID2 = Res.getGemByID(gemTemplate3.id);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows.this.showGemItemInfo(gemTemplate3.id, (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i9, int i10) {
                        if (Res.getGemByID(gemTemplate3.id) != null) {
                            Res.paintGem(graphics, Res.getGemByID(gemTemplate3.id).idImage, i9, i10);
                            if (gemByID2.isEff != -1) {
                                Res.paintRectColor(graphics, i9 - 9, i10 - 9, 17, 17, gemByID2.count, GemTemp.color[gemByID2.isEff], -260847);
                                gemByID2.count += 3;
                                if (gemByID2.count > 68) {
                                    gemByID2.count = 0;
                                }
                            }
                            font.drawString(graphics, gemTemplate3.number + "", i9 + 13, i10 + 6, 1);
                        }
                    }
                });
                slot++;
            }
            int size3 = MainChar.animal.size();
            for (int i9 = 0; i9 < size3; i9++) {
                final AnimalInfo animalInfo = (AnimalInfo) Char.animal.elementAt(i9);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        if (animalInfo.info.equals("")) {
                            return;
                        }
                        if (animalInfo.typeAnimal == 0) {
                            MenuWindows.this.setShowText(ItemInInventory.setTemp(animalInfo.name + " lv" + ((int) animalInfo.level), "0") + ItemInInventory.setTemp(animalInfo.info, "0"), (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                        } else {
                            MenuWindows.this.setShowText(ItemInInventory.setTemp(animalInfo.name, "0") + ItemInInventory.setTemp(animalInfo.info, "0") + ItemInInventory.setTemp("Còn lại: " + (((long) animalInfo.totalTimeCon) - ((System.currentTimeMillis() - animalInfo.timeStart) / 60000)) + " phút", "0"), (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                        }
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i10, int i11) {
                        animalInfo.paint(graphics, i10, i11);
                    }
                });
                slot++;
            }
            int size4 = Char.inventory.size();
            for (int i10 = 0; i10 < size4; i10++) {
                final ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i10);
                final int ii = slot;
                vector.addElement(new Command("", new IAction() {
                    @Override
                    public void perform() {
                        MenuWindows menuWindows = MenuWindows.this;
                        menuWindows.showItemInventoryInfo(itemInInventory, menuWindows.isSell, (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                    }
                }) {
                    @Override
                    public void paint(Graphics graphics, int i11, int i12) {
                        if (itemInInventory.isSelling) {
                            graphics.setColor(-9070864);
                            graphics.fillRect(i11 - 8, i12 - 8, 16, 16);
                        }
                        itemInInventory.paint(graphics, i11, i12);
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
                                MenuWindows menuWindows = MenuWindows.this;
                                menuWindows.setShowText(menuWindows.getInfoItemQuest(quest), (ii % MenuWindows.this.numW) * 18, (ii / MenuWindows.this.numW) * 18);
                            }
                        }) {
                            @Override
                            public void paint(Graphics graphics, int i13, int i14) {
                                GameData.paintIcon(graphics, (short) idImg, i13, i14);
                                font.drawString(graphics, str2, i13 + 8, i14 + 1, 1);
                            }
                        });
                    }
                }
            }
        } catch (Exception unused) {
        }
        return vector;
    }

    private void showItemInventoryAnimalInfo(ItemInInventory itemInInventory, boolean z, int i, int i2) {
        this.arrayKhamNgoc = null;
        if (itemInInventory.totalKham > 0) {
            this.arrayKhamNgoc = new byte[itemInInventory.totalKham];
            for (int i3 = 0; i3 < itemInInventory.totalKham; i3++) {
                this.arrayKhamNgoc[i3] = -1;
            }
            for (int i4 = 0; i4 < itemInInventory.property.length; i4++) {
                if (itemInInventory.property[i4] > 0) {
                    for (int i5 = 0; i5 < itemInInventory.numKham; i5++) {
                        this.arrayKhamNgoc[i5] = Res.idNgocKham[i4];
                    }
                    break;
                }
            }
        }
        setShowText(itemInInventory.getDescription(z), i, i2);
        if (itemInInventory.isEnoughData) {
            return;
        }
        itemInInventory.isEnoughData = true;
        GameService gameServiceGI = GameService.gI();
        short s = itemInInventory.itemID;
        Char r7 = charWearing;
        gameServiceGI.requestItemInfo(s, r7 == null ? GCanvas.gameScr.mainChar.ID : r7.ID);
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
        if (!itemInInventory.isEnoughData) {
            GameService gameServiceGI = GameService.gI();
            short s = itemInInventory.itemID;
            Char r10 = charWearing;
            gameServiceGI.requestItemInfo(s, r10 == null ? GCanvas.gameScr.mainChar.ID : r10.ID);
            return;
        }
        setShowText(itemInInventory.getDescription(z), i, i2);
    }

    public void closeText() {
        this.isShowText = false;
        this.arrayKhamNgoc = null;
    }

    public void setShowText(String str, int i, int i2) {
        if (GCanvas.screenlevel == 1) {
            str = str.replace("Â", "â");
        }
        this.isShowText = true;
        this.wShow = this.wKhung - (this.x0 + 138);
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
            if (focusTabChinh == 2) {
                cmyLimText = length - 100;
            } else {
                cmyLimText = length - 130;
            }
        }
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
        graphics.fillRect(-7, -13, i + 10, (i + 10) * 5);
        int i2 = this.wSmall;
        graphics.fillRect(((i2 + 10) * 4) - 7, -13, i2 + 10, (i2 + 10) * 5);
        graphics.fillRect(((this.wSmall + 10) * 3) - 8, ((this.hSmall + 10) * 4) - 11, 26, 26);
        int i3 = this.wSmall;
        graphics.fillRect(i3 + 7, ((((i3 + 10) * 5) - 14) - i3) - 10, i3 + 10, i3 + 10);
        for (int i4 = 0; i4 < 5; i4++) {
            paintGrid(graphics, -7, ((this.hSmall + 10) * i4) - 13);
            paintGrid(graphics, ((this.wSmall + 10) * 4) - 7, ((this.hSmall + 10) * i4) - 13);
        }
        paintGrid(graphics, ((this.wSmall + 10) * 3) - 10, ((this.hSmall + 10) * 4) - 13);
        int i5 = this.wSmall;
        graphics.fillRect(i5 + 6, -13, ((i5 + 10) * 3) - 6, 1);
        int i6 = this.wSmall;
        graphics.fillRect(i6 + 6, ((i6 + 10) * 5) - 13, ((i6 + 10) * 3) - 6, 1);
        int i7 = this.wSmall;
        graphics.fillRect(i7 + 6, -13, 1, (i7 + 10) * 5);
        int i8 = this.wSmall;
        graphics.fillRect(((i8 + 10) * 4) - 10, -13, 1, (i8 + 10) * 5);
        int i9 = this.wSmall;
        paintGrid(graphics, i9 + 7, ((((i9 + 10) * 5) - 14) - i9) - 10);
        if (this.isClick && GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-6181426);
            int i10 = this.selected;
            if (i10 % 3 != 1) {
                int i11 = this.numW;
                int i12 = i10 % i11 <= 0 ? 0 : 4;
                int i13 = this.wSmall;
                graphics.fillRect((i12 * (i13 + 10)) - 6, ((i10 / i11) * (i13 + 10)) - 12, 26, 26);
            } else if (i10 != 4) {
                graphics.fillRect(((this.wSmall + 10) * 3) - 9, ((this.hSmall + 10) * 4) - 12, 26, 26);
            } else {
                int i14 = this.wSmall;
                graphics.fillRect(i14 + 8, ((((i14 + 10) * 5) - 13) - i14) - 10, i14 + 8, i14 + 8);
            }
        }
        if (charWearing.wearingItems != null) {
            int size = charWearing.wearingItems.size();
            for (int i15 = 0; i15 < size; i15++) {
                ItemInInventory itemInInventory = (ItemInInventory) charWearing.wearingItems.elementAt(i15);
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
        if (charWearing.idAnimal <= -1 || GCanvas.gameScr.mainChar.isDoing) {
            return;
        }
        GameData.paintIcon(graphics, (short) (charWearing.idPaintIconAnimal + 7500), 87, 112);
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
        graphics.fillRect(-7, -13, i + 10, (i + 10) * 5);
        int i2 = this.wSmall;
        graphics.fillRect(((i2 + 10) * 4) - 7, -13, i2 + 10, (i2 + 10) * 5);
        graphics.fillRect(((this.wSmall + 10) * 3) - 8, ((this.hSmall + 10) * 4) - 11, 26, 26);
        for (int i3 = 0; i3 < 5; i3++) {
            paintGrid(graphics, -7, ((this.hSmall + 10) * i3) - 13);
            paintGrid(graphics, ((this.wSmall + 10) * 4) - 7, ((this.hSmall + 10) * i3) - 13);
        }
        paintGrid(graphics, ((this.wSmall + 10) * 3) - 10, ((this.hSmall + 10) * 4) - 13);
        int i4 = this.wSmall;
        graphics.fillRect(i4 + 6, -13, ((i4 + 10) * 3) - 6, 1);
        int i5 = this.wSmall;
        graphics.fillRect(i5 + 6, ((i5 + 10) * 5) - 13, ((i5 + 10) * 3) - 6, 1);
        int i6 = this.wSmall;
        graphics.fillRect(i6 + 6, -13, 1, (i6 + 10) * 5);
        int i7 = this.wSmall;
        graphics.fillRect(((i7 + 10) * 4) - 10, -13, 1, (i7 + 10) * 5);
        if (this.isClick && GCanvas.gameTick % 10 > 3) {
            graphics.setColor(-6181426);
            int i8 = this.selected;
            if (i8 % 2 == 0) {
                graphics.fillRect(-6, ((i8 / 2) * (this.hSmall + 10)) - 11, 26, 26);
            } else {
                graphics.fillRect(((this.wSmall + 10) * 4) - 6, ((i8 / 2) * (this.hSmall + 10)) - 11, 26, 26);
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

    protected void doSelectedCheDo() {
        final GemTemplate gemTemplate;
        try {
            int i = this.selected;
            int i2 = this.numW;
            final boolean z = i / i2 < 2;
            if (z) {
                if (i < WindowInfoScr.gI().listEp.size()) {
                    gemTemplate = (GemTemplate) WindowInfoScr.gI().listEp.elementAt(this.selected);
                } else {
                    gemTemplate = null;
                }
            } else if (i % i2 < WindowInfoScr.gI().listTemp.size()) {
                gemTemplate = (GemTemplate) WindowInfoScr.gI().listTemp.elementAt(this.selected % this.numW);
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
                        MenuWindows.this.removeItemCheDo(gemTemplate);
                    } else {
                        int i8 = 0;
                        int i9 = 0;
                        for (int i10 = 0; i10 < WindowInfoScr.gI().listCheDo.size(); i10++) {
                            Mineral mineral = (Mineral) WindowInfoScr.gI().listCheDo.elementAt(i10);
                            if (gemTemplate.id - mineral.idTemplate >= 0 && gemTemplate.id - mineral.idTemplate <= 5) {
                                for (int i11 = 0; i11 < WindowInfoScr.gI().listEp.size(); i11++) {
                                    GemTemplate gemTemplate2 = (GemTemplate) WindowInfoScr.gI().listEp.elementAt(i11);
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
                        while (true) {
                            if (i8 < WindowInfoScr.gI().listEp.size()) {
                                GemTemplate gemTemplate3 = (GemTemplate) WindowInfoScr.gI().listEp.elementAt(i8);
                                if (gemTemplate3.id == gemTemplate.id) {
                                    gemTemplate3.number++;
                                    break;
                                }
                                i8++;
                            } else {
                                GemTemplate gemTemplate4 = new GemTemplate(gemTemplate.id);
                                GemTemplate.copyData(gemTemplate, gemTemplate4);
                                gemTemplate4.number = 1;
                                WindowInfoScr.gI().listEp.addElement(gemTemplate4);
                                break;
                            }
                        }
                        gemTemplate.number--;
                        if (gemTemplate.number <= 0) {
                            WindowInfoScr.gI().listTemp.removeElement(gemTemplate);
                        }
                    }
                    MenuWindows.this.setLimitCheDo();
                }
            }));
            vector.addElement(new Command("Thông tin", new IAction() {
                @Override
                public void perform() {
                    MenuWindows.this.showGemItemInfo(s, i6, i7);
                }
            }));
            GCanvas.menu.startAt(vector, 2);
        } catch (Exception unused) {
        }
    }

    public void removeItemCheDo(GemTemplate gemTemplate) {
        gemTemplate.number--;
        if (gemTemplate.number <= 0) {
            WindowInfoScr.gI().listEp.removeElement(gemTemplate);
        }
        for (int i = 0; i < WindowInfoScr.gI().listTemp.size(); i++) {
            GemTemplate gemTemplate2 = (GemTemplate) WindowInfoScr.gI().listTemp.elementAt(i);
            if (gemTemplate2.id == gemTemplate.id) {
                gemTemplate2.number++;
                return;
            }
        }
        GemTemplate gemTemplate3 = new GemTemplate(gemTemplate.id);
        GemTemplate.copyData(gemTemplate, gemTemplate3);
        gemTemplate3.number = 1;
        WindowInfoScr.gI().listTemp.addElement(gemTemplate);
    }

    public void setLimitCheDo() {
        int i = this.selected;
        int i2 = this.numW;
        int i3 = i % i2;
        if (i / i2 == 1) {
            int size = WindowInfoScr.gI().listEp.size() / 2;
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
        } else if (i / i2 == 2) {
            int size2 = WindowInfoScr.gI().listTemp.size();
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
        int i7 = i6 - 124;
        cmxLim = i7;
        if (cmx < 0) {
            cmx = -10;
        }
        if (cmx > i7) {
            int i8 = i6 - 94;
            cmtoX = i8;
            cmx = i8;
        }
    }

    protected void doLeftCheDo() {
        if (WindowInfoScr.gI().listEp.size() == 0) {
            return;
        }
        final byte[][] bArr = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, WindowInfoScr.gI().listCheDo.size(), 6);
        for (int i = 0; i < WindowInfoScr.gI().listEp.size(); i++) {
            GemTemplate gemTemplate = (GemTemplate) WindowInfoScr.gI().listEp.elementAt(i);
            for (int i2 = 0; i2 < WindowInfoScr.gI().listCheDo.size(); i2++) {
                int i3 = gemTemplate.id - ((Mineral) WindowInfoScr.gI().listCheDo.elementAt(i2)).idTemplate;
                if (i3 <= 5) {
                    bArr[i2][i3] = (byte) gemTemplate.number;
                    break;
                }
            }
        }
        GCanvas.startYesNoDlg("Bạn có muốn hoàn thành không ?", new IAction() {
            @Override
            public void perform() {
                for (int size = WindowInfoScr.gI().listEp.size() - 1; size >= 0; size--) {
                    MenuWindows.this.removeItemCheDo((GemTemplate) WindowInfoScr.gI().listEp.elementAt(size));
                }
                GameService gameServiceGI = GameService.gI();
                short size2 = (short) WindowInfoScr.gI().listCheDo.size();
                WindowInfoScr.gI();
                gameServiceGI.doCheDo(size2, WindowInfoScr.idTemp, bArr);
                MenuWindows.this.right.action.perform();
                GCanvas.startWaitDlg();
            }
        });
    }
}
