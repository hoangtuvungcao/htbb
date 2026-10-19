package game.render.screen;

import effect.DataSkillEff;
import effect.DynamicEffect;
import game.GameMidlet;
import game.model.Actor;
import game.model.DuongDi;
import game.model.AnimalMove;
import game.model.AnimateEffect;
import game.model.AttackResult;
import game.model.BossGyarados;
import game.model.BossSalamence;
import game.model.Boss_Dragon;
import game.model.BossZapdos;
import game.model.BossGengar;
import game.model.BossSerperior;
import game.model.BossAegislash;
import game.model.BossDusknoir;
import game.model.CMLocation;
import game.model.Char;
import game.model.CharBuff;
import game.model.CharCountDown;
import game.model.CharPartInfo;
import game.model.ChatInfo;
import game.model.ChatPopup;
import game.model.ClanInfo;
import game.model.Command;
import game.model.CongGym;
import game.model.Cout;
import game.model.DropGemItem;
import game.model.DropItem;
import game.model.DropPotion;
import game.model.Dropable;
import game.model.DynamicObj;
import game.model.Effect;
import game.model.EffectManager;
import game.model.EffectServerManager;
import game.model.Explosion;
import game.model.GemTemp;
import game.model.GemTemplate;
import game.model.Ground;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.InfoAttributeItem;
import game.model.InputDlg;
import game.model.ItemInInventory;
import game.model.ItemLuckyDraw;
import game.model.ItemQuest;
import game.model.ItemTemplate;
import game.model.Layer;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.Mineral;
import game.model.Monster;
import game.model.MonsterInfo;
import game.model.MonsterNew;
import game.model.MsgInfo;
import game.model.NPC;
import game.model.Npc_Server;
import game.model.PageScr;
import game.model.PaintPopup;
import game.model.PartyInfo;
import game.model.PopupName;
import game.model.Position;
import game.model.Potion;
import game.model.Property;
import game.model.Quest;
import game.model.QuickSlot;
import game.model.RealID;
import game.model.ShopNew;
import game.model.SkillClan;
import game.model.Skill_Kham;
import game.model.Sound;
import game.model.SubEff;
import game.model.Target;
import game.model.PokemonHuyenThoai;
import game.model.Tilemap;
import game.model.TimecountDown;
import game.model.Tree;
import game.model.TruNangLuong;
import game.model.WayPoint;
import game.model.PidgeotBayTemplate;
import game.model.arrow.Arrow;
import game.model.arrow.IArrow;
import game.model.arrow.MagicBeam;
import game.model.arrow.WeaponsAngleBoss;
import game.model.arrow.WeaponsFire;
import game.model.arrow.WeaponsLazer;
import game.model.arrow.WeaponsMango;
import game.model.monster.QuaiYamask;
import game.model.monster.Pet;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.model.skill.Skill_Boss_Halloween;
import game.model.skill.Skill_Da_Roi;
import game.model.skill.Skill_Noel;
import game.networklogic.GameService;
import game.networklogic.ItemDropInfo;
import game.networklogic.MonsterDieInfo;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import network.Message;
import network.Session_ME;

public class GameScr extends Screen {
    public static final byte KILL_MOSTER = 0;
    public static final byte GET_ITEM = 2;
    public static final byte TRANSPORT = 1;
    public int gssx;
    public int gssy;
    public int gssw;
    public int gssh;
    public int gssxe;
    public int gssye;
    public static int cmtoX;
    public static int cmtoY;
    public static int cmvx;
    public static int cmvy;
    public static int cmdx;
    public static int cmdy;
    public static int cmx;
    public static int cmy;
    public static int cmxLim;
    public static int cmyLim;
    public static boolean lockcmx;
    public static boolean lockcmy;
    public Actor focusedActor;
    public boolean onoffInvite;
    long lastUpdateMyPositionTime;
    public static boolean isFight;
    public Position posBoss;
    public int timerung;
    boolean changeSinceLastUpdate;
    public static int gsw;
    public static int gsh;
    public static int gscmdBarY;
    public static int gshw;
    public static int gshh;
    public static int gswd3;
    public static int gshd3;
    public static int gsw2d3;
    public static int gsh2d3;
    public static int gsw3d4;
    public static int gsh3d4;
    public static int gswd6;
    public static int gshd6;
    public String[] flyTextString;
    public int[] flyTextX;
    public int[] flyTextY;
    public int[] flyTextDx;
    public int[] flyTextDy;
    public int[] flyTextState;
    public int[] flyTextColor;
    public boolean isBuffAuto;
    public TField tfChat;
    public static Position posNpcReceiveClan;
    public static Image imgInfo;
    public static Image imgHpMonster;
    public static Image[] imgCong;
    public static Image img12plus;
    public static Image petShadow;
    public Position posNpcRequest;
    int startMsg;
    public static Actor objcountDown;
    public static Actor congThanh;
    public static byte xArena;
    public static byte yArena;
    public static byte wArena;
    public static byte hArena;
    public static int[] timeChiemThanh;
    public static int[] curTimeCT;
    public static short[] idChiemThanh;
    public static Image[] imghealth;
    public static String[] nameChiemThanh;
    public static String[] nameClan;
    public static Vector effManager;
    int HPBARW;
    int HPBARW_MONSTER;
    private Position posMiniMap;
    public static int wMiniMap;
    public static int hMiniMap;
    private Position[] posQuickSlot;
    public long lastTimePing;
    public long pingDelay;
    public static long timeStandWhenAuto;
    int countChang;
    int xMsgServer;
    private static final byte DELAY_BETWEEN_PING = 30;
    public static final byte MAX_INVENTORY = 42;
    public static boolean saveAutoFight;
    public static boolean isBeginAutoBuff;
    public static boolean isAutoBomHp;
    public static boolean isAutoBomMp;
    public static boolean autoBomHMP;
    public boolean isAutoRangeWhenAuto;
    public static long timeDelayHpMp;
    Position posTo;
    int[] SLOTINDEXOFKEY;
    boolean first;
    public static int timeRemovePos;
    public int coutChangeFocusWhenDoing;
    boolean paintMapPopup;
    int countColor;
    int[] colorPaint;
    byte idInfo;
    byte xInfo;
    byte yInfo;
    public Ticker tk;
    public static byte typechat;
    int tickp;
    public short map;
    public boolean isNewVersionAvailable;
    public static short[] idArrMap;
    public static short[][] mapID;
    public short xBeginFrame;
    public short yBeginFrame;
    public short xTheendFrame;
    public short yTheendFrame;
    public Vector decriptNap;
    public Vector syntaxNap;
    public Vector centerNap;
    public static MonsterNew monsternew;
    public static int cmtoYmini;
    public static int cmyMini;
    public static int cmdyMini;
    public static int cmvyMini;
    public static int cmtoXMini;
    public static int cmxMini;
    public static int cmdxMini;
    public static int cmvxMini;
    public static long timeTranMini;
    private Position posCam;
    private Target tg;
    public static byte[][] mapFind;
    public static byte xStart;
    public static byte yStart;
    public static boolean isFixBugAutoQuest;
    public int iTaskAutoLast;
    public Vector objDynamic;
    Vector listMSGServer;
    public SkillClan[] skillClan;
    public SkillClan[] skillClanPrivate;
    public static CharCountDown charcountdonw;
    public Vector VecTime;
    public Vector VecDataEff;
    public static final byte STATE_FINISH = 1;
    public static final byte STATE_WORKING = 2;
    public static final byte STATE_UN_RECEIVE = 0;
    public static final byte STATE_DONE = 3;
    public static Quest mainQuest;
    public static Quest subQuest;
    public static Quest clanQuest;
    public static String nameMap = "";
    public static Vector arrowsUp = new Vector();
    public static Vector infoNewSkill = new Vector();
    public static Vector serverInfo = new Vector();
    public static int tempIDActorParty = -1;
    public static byte idItemClanquest = -1;
    public static String infoQuest = "Hạ 10 con nhím, sau đó quay về nói chuyện với  NPC";
    public static String info_gif_quest = null;
    public static String titleQuest = "";
    public static Vector aeo = new Vector();
    public static long timeBuff = -1;
    public static int indexTabSlot = 0;
    public static int xSlot = 0;
    public static int idMapPK = -1;
    public static int iTaskAuto = 0;
    private static boolean isFindChar = false;
    private static boolean isActorMove = false;
    private static boolean isFindMonster = false;
    public static Vector effAnimate = new Vector();
    public static Vector msgWorld = new Vector();
    public static Vector savemsgWorld = new Vector();
    public static Boss_Dragon[] bDragon = new Boss_Dragon[1];
    static String[] nameCountry = {"Kanto", "Johto"};
    public static byte typeOptionFocus = 0;
    static Random r = new Random(System.currentTimeMillis());
    public static short dtmove = 250;
    static final int[][] C = {new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}};
    static final byte[][] DX = {new byte[]{0, 0, -48, 48}, new byte[]{0, 0, -32, 32}, new byte[]{0, 0, -16, 16}, new byte[4]};
    static final byte[][] DY = {new byte[]{48, -48, 0, 0}, new byte[]{32, -32, 0, 0}, new byte[]{16, -16, 0, 0}, new byte[4]};
    public static long timeMove = System.currentTimeMillis();
    public static boolean autoFight = false;
    public static Quest currQuest = null;
    public static int timeDelayTask = 50;
    public static boolean cheat = false;
    private static int[] colorMini = {0x1E2A44, 0xFFFFFF, 0x3D7DCA};   // pokemon: viền tối / trắng / xanh
    public static byte CHAT_NORMAL = 0;
    public static byte CHAT_TIM_BAN = 1;
    private static int[] colorSlot = {3158321, 88080384, 2122, 2037000, 256};
    static short[] offMapID = {90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 102, 103, 104, 108, 109, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 29, 30, 31, 30, 31, 72, 73, 74, 78, 79, 82, 83, 84, 88, 89};
    static int oldMap = -1;
    public static int paintCay = 0;
    public static int paintChar = 1;
    public static int pTicket = 1000;
    public static byte weather = -1;
    public static Vector allQuestCanReceive = new Vector();
    public static Vector PosNPCQuest = new Vector();
    public static byte[][] TYPE_MP_HP = new byte[2][];
    public static int[][] VALUE_MP_HP = new int[2][];
    int[] CMVIBRATEX = {-1, 2, 1, -2};
    int[] CMVIBRATEY = {-3, 2, -1, 1};
    int currentCmVibrate = -1;
    public Vector actors = new Vector();
    public Vector npc_limit = new Vector();
    public Vector arrowsDown = new Vector();
    public int hideGUI = 0;
    public MainChar mainChar = new MainChar();
    public boolean readyGetGameLogic = true;
    public boolean lockPosition = true;
    public boolean isFillScr = false;
    public Vector shop = new Vector();
    public Vector shopSpecial = new Vector();
    public GameService gameService = GameService.gI();
    public short saveIDViewInfoAnimal = -1;
    public boolean chatMode = false;
    Vector chatHistory = new Vector();
    int CHAT_HISTORY_SIZE = 5;
    public short questID = 1;
    public short current_npc_talk = -1;
    public int indexContent = -1;
    public short idNpcReceive = 5;
    public boolean receiveQuest = false;
    public String[] content_quest = null;
    int yMsgChat = 0;
    int toYchat = -18;
    public int indexBuff = -1;
    private int pingNextIn = 10;
    public int[] DELTACAMERAX = {0, 0, -20, 20};
    public int[] DELTACAMERAY = {20, -20, 0, 0};
    int[] xChange = {4, 0, -4};
    int dira = 1;
    long timeAuto = 0;
    public Vector nearActors = new Vector();
    byte lastDir = 0;
    byte dir = 0;
    byte countStep = 0;
    short moveToX = 0;
    short moveToY = 0;
    public int[] QUICKSLOT_KEY = {1, 3, 5, 7, 9};
    boolean finishQuest = false;
    int dem = 1;
    int t = (int) (System.currentTimeMillis() / 1000);
    private long timeTaskAuto = 0;
    byte[] X_FOWARD = {0, 0, -17, 17};
    byte[] Y_FOWARD = {17, -17, 0, 0};

    @Override
    public void switchToMe() {
        Canvas.keyHold[2] = false;
        super.switchToMe();
        init();
        game.pokemon.ManTai.xong();   // 0.9.5: đã vào thế giới - hết màn tải
    }

    public void addChat(ChatInfo ci) {
        if (this.chatHistory.size() > 50) {
            this.chatHistory.removeElementAt(0);
            this.yMsgChat -= 18;
            this.toYchat -= 18;
        }
        this.chatHistory.addElement(ci);
        this.toYchat += 18;
    }

    public final void loadFlyText() {
        this.flyTextX = new int[15];
        this.flyTextY = new int[15];
        this.flyTextDx = new int[15];
        this.flyTextDy = new int[15];
        this.flyTextState = new int[15];
        this.flyTextColor = new int[15];
        this.flyTextString = new String[15];
        for (int i = 0; i < 15; i++) {
            this.flyTextState[i] = -1;
        }
    }

    public final void startFlyText(String flyString, int flyColor, int x, int y, int dx, int dy) {
        try {
            int max = Integer.parseInt(flyString.substring(1));
            if (max == 2000000) {
                return;
            }
        } catch (Exception e) {
        }
        // pokemon: so/chu bay kieu moi (vien, cong, mau theo loai - game.pokemon.SoBay)
        if (game.pokemon.SoBay.bat(flyString, flyColor, x, y)) {
            return;
        }
        int n = -1;
        for (int i = 0; i < 15; i++) {
            if (this.flyTextState[i] == -1) {
                n = i;
                break;
            }
        }
        if (n == -1) {
            return;
        }
        this.flyTextString[n] = flyString.toLowerCase();
        this.flyTextX[n] = x;
        this.flyTextY[n] = y;
        this.flyTextDx[n] = dx * (r.nextInt(2) == 1 ? -1 : 1);
        this.flyTextDy[n] = dy;
        this.flyTextState[n] = 0;
        this.flyTextColor[n] = flyColor;
    }

    public final void updateFlyText() {
        // pokemon: hat/dan/canh bao/rung man hinh + so bay kieu moi + nhac theo boss
        game.pokemon.HieuUngDanh.capNhat();
        game.pokemon.SoBay.capNhat();
        for (int i = 0; i < 15; i++) {
            if (this.flyTextState[i] != -1) {
                int[] iArr = this.flyTextState;
                int i2 = i;
                iArr[i2] = iArr[i2] + Util.abs(this.flyTextDy[i]);
                if (this.flyTextState[i] > 30) {
                    this.flyTextState[i] = -1;
                }
                int[] iArr2 = this.flyTextX;
                int i3 = i;
                iArr2[i3] = iArr2[i3] + this.flyTextDx[i];
                int[] iArr3 = this.flyTextY;
                int i4 = i;
                iArr3[i4] = iArr3[i4] + this.flyTextDy[i];
            }
        }
    }

    public final void paintFlyText(Graphics g) {
        // pokemon: hieu ung chien dau (toa do the gioi); so bay kieu moi ve o lop giao dien (NhanTheGioi.ve)
        game.pokemon.HieuUngDanh.ve(g);
        for (int i = 0; i < 15; i++) {
            if (this.flyTextState[i] != -1) {
                if (this.flyTextColor[i] != 0) {
                    Font.smallFontColor[this.flyTextColor[i]].drawString(g, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                } else {
                    Font.smallFontYellow.drawString(g, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                }
            }
        }
    }

    @Override
    public void init() {
        this.tfChat.x = 2;
        this.tfChat.y = Canvas.h - 40;
        this.tfChat.width = Canvas.w - 4;
        if (this.posQuickSlot == null) {
            this.posQuickSlot = new Position[5];
            for (int i = 0; i < 5; i++) {
                this.posQuickSlot[i] = new Position();
            }
        }
        this.posQuickSlot[0].x = Canvas.hw - 47;
        this.posQuickSlot[0].y = Canvas.h - 19;
        this.posQuickSlot[1].x = Canvas.hw - 28;
        this.posQuickSlot[1].y = Canvas.h - 19;
        this.posQuickSlot[2].x = Canvas.hw - 8;
        this.posQuickSlot[2].y = Canvas.h - 20;
        this.posQuickSlot[3].x = Canvas.hw + 12;
        this.posQuickSlot[3].y = Canvas.h - 19;
        this.posQuickSlot[4].x = Canvas.hw + 31;
        this.posQuickSlot[4].y = Canvas.h - 19;
        setPosMiniMap();
        loadCamera();
    }

    public GameScr() {
        this.tfChat = new TField();
        this.xMsgServer = 0;
        int[] iArr = new int[10];
        iArr[3] = 1;
        iArr[5] = 2;
        iArr[7] = 3;
        iArr[9] = 4;
        this.SLOTINDEXOFKEY = iArr;
        this.first = false;
        this.paintMapPopup = false;
        this.countColor = 0;
        this.colorPaint = new int[]{16520709, 16499718, 396543, 1101907};
        this.idInfo = (byte) 0;
        this.tk = null;
        this.tickp = 0;
        this.map = (short) -1;
        this.xBeginFrame = (short) 0;
        this.yBeginFrame = (short) 0;
        this.xTheendFrame = (short) 0;
        this.yTheendFrame = (short) 0;
        this.decriptNap = new Vector();
        this.syntaxNap = new Vector();
        this.centerNap = new Vector();
        this.tg = new Target();
        this.objDynamic = new Vector();
        this.VecTime = new Vector();
        this.VecDataEff = new Vector();
        this.tfChat = new TField();
        this.tfChat.height = Screen.ITEM_HEIGHT + 2;
        this.tfChat.isFocus = true;
        this.tfChat.setMaxTextLenght(80);
        init();
        this.isBuffAuto = false;
        loadFlyText();
        setPosMiniMap();
        this.HPBARW = 45;
        this.HPBARW_MONSTER = 50;
        if (this.HPBARW_MONSTER < 30) {
            this.HPBARW_MONSTER = 30;
        }
        this.left = new Command("", new IAction() {
            @Override
            public void perform() {
                if (GameScr.this.mainChar.hp > 0) {
                    PauseMenu.gI().switchToMe();
                }
            }
        });
        this.right = new Command("", new IAction() {
            @Override
            public void perform() {
                GameScr.this.changeToNextFocusActor(false);
            }
        });
        this.xMsgServer = Canvas.w - 20;
    }

    public void setPosMiniMap() {
        if (Tilemap.imgMap == null) {
            return;
        }
        if (HudRong.bat()) {
            int[] o = HudRong.oMiniMap(Tilemap.imgMap.getWidth(), Tilemap.imgMap.getHeight());
            wMiniMap = o[2];
            hMiniMap = o[3];
            this.posMiniMap = new Position(o[0], o[1]);
            return;
        }
        hMiniMap = Canvas.h / 5;
        if (hMiniMap > 100) {
            hMiniMap = 100;
        }
        this.posMiniMap = new Position(Canvas.hw + 50, (Canvas.h - Screen.deltaY) - hMiniMap);
        wMiniMap = (Canvas.w - this.posMiniMap.x) - 1;
        if (wMiniMap > 100) {
            wMiniMap = 100;
        }
        if (Tilemap.imgMap != null) {
            if (wMiniMap > Tilemap.imgMap.getWidth()) {
                wMiniMap = Tilemap.imgMap.getWidth();
            }
            if (hMiniMap > Tilemap.imgMap.getHeight()) {
                hMiniMap = Tilemap.imgMap.getHeight();
            }
        }
        this.posMiniMap.x = (Canvas.w - wMiniMap) - 1;
        this.posMiniMap.y = (Canvas.h - Screen.deltaY) - hMiniMap;
        // pokemon: màn hẹp - hàng ô phím tắt chạm tới bản đồ nhỏ thì đưa bản đồ lên trên hàng ô
        if (Res.imgPanel != null && Canvas.hw + rongThanhO() / 2 > this.posMiniMap.x) {
            this.posMiniMap.y = (Canvas.h - 16) - caoThanhO() - hMiniMap - 2;
        }
    }

    /** pokemon: vị trí bản đồ nhỏ (HudRong). */
    Position posMiniMapHud() {
        return this.posMiniMap;
    }

    /** pokemon: bản đồ nhỏ màn rộng (HudRong) + icon buff / kỹ năng đội dưới bản đồ. */
    private void paintMiniMapRong(Graphics g) {
        if (this.posMiniMap == null || this.posMiniMap.x != Canvas.w - wMiniMap - 4 || this.posMiniMap.y != 45) {
            setPosMiniMap();   // đổi map / đổi cỡ màn
        }
        HudRong.veMiniMap(g, this, this.posMiniMap);
        int y = HudRong.dayMiniMap(this) + 2;
        if (this.indexBuff != -1 && timeBuff - (System.currentTimeMillis() / 1000) > 0) {
            game.pokemon.BangChieu.icon(g, Canvas.w - 24, y, 20, this.indexBuff);
            Font.smallFontColor[0].drawString(g, String.valueOf((timeBuff - System.currentTimeMillis()) / 1000), Canvas.w - 30, y + 5, 1);
            y += 22;
        }
        int nsk = 0;
        if (this.skillClan != null) {
            for (int i5 = 0; i5 < this.skillClan.length; i5++) {
                if (this.skillClan[i5].time > 0) {
                    GameData.paintIcon(g, (short) (this.skillClan[i5].idIcon + 2600), Canvas.w - 9 - 18 * nsk, y + 8);
                    nsk++;
                }
            }
        }
        if (this.skillClanPrivate != null) {
            for (int i6 = 0; i6 < this.skillClanPrivate.length; i6++) {
                if (this.skillClanPrivate[i6].time > 0) {
                    GameData.paintIcon(g, (short) (this.skillClanPrivate[i6].idIcon + 2600), Canvas.w - 9 - 18 * nsk, y + 8);
                    nsk++;
                }
            }
        }
    }

    /** pokemon: bước ngang 1 ô skill nhanh - lớn hơn trên màn rộng (240 / 320+ px). */
    public static int buocO() {
        int b = Res.wKhung;
        if (Canvas.w >= 320) {
            return b < 38 ? 38 : b;
        }
        if (Canvas.w >= 240) {
            return b < 30 ? 30 : b;
        }
        return b;
    }

    public static int rongThanhO() {
        return 5 * buocO();
    }

    /** chiều cao hàng ô skill nhanh (cùng chỗ ảnh panel cũ, cao hơn trên màn rộng). */
    public static int caoThanhO() {
        int h = Res.imgPanel == null ? 24 : Res.imgPanel.getHeight();
        if (Canvas.w >= 320) {
            return h < 40 ? 40 : h;
        }
        if (Canvas.w >= 240) {
            return h < 32 ? 32 : h;
        }
        return h;
    }

    public void loadCamera() {
        gsw = Canvas.w;
        gsh = Canvas.h;
        gshw = gsw >> 1;
        gshh = gsh >> 1;
        gswd3 = gsw / 3;
        gshd3 = gsh / 3;
        gsw2d3 = (2 * gsw) / 3;
        gsh2d3 = (2 * gsh) / 3;
        gsw3d4 = (3 * gsw) / 4;
        gsh3d4 = (3 * gsh) / 4;
        gswd6 = gsw / 6;
        gshd6 = gsh / 6;
        this.gssw = (gsw >> 4) + 2;
        this.gssh = (gsh >> 4) + 2;
        if (gsw % 24 != 0) {
            this.gssw++;
        }
        cmxLim = (Tilemap.w << 4) - gsw;
        cmyLim = (Tilemap.h << 4) - gsh;
        if (this.mainChar != null) {
            int i = this.mainChar.x - gshw;
            cmtoX = i;
            cmx = i;
            int i2 = this.mainChar.y - gshh;
            cmtoY = i2;
            cmy = i2;
        }
        if (cmx < 0) {
            cmx = 0;
        }
        if (cmx > cmxLim) {
            cmx = cmxLim;
        }
        if (cmy < 0) {
            cmy = 0;
        }
        if (cmy > cmyLim) {
            cmy = cmyLim;
        }
        this.gssx = (cmx >> 4) - 1;
        if (this.gssx < 0) {
            this.gssx = 0;
        }
        this.gssy = cmy >> 4;
        this.gssxe = this.gssx + this.gssw;
        this.gssye = this.gssy + this.gssh;
        if (this.gssy < 0) {
            this.gssy = 0;
        }
        if (this.gssye > Tilemap.h - 1) {
            this.gssye = Tilemap.h - 1;
        }
        lockcmy = false;
        lockcmx = false;
        if (Tilemap.pxw < gsw + 32) {
            lockcmx = true;
            cmx = (-(gsw - Tilemap.pxw)) >> 1;
        }
        if (Tilemap.pxh < gsh) {
            lockcmy = true;
            cmy = (-(gsh - Tilemap.pxh)) >> 1;
        }
    }

    public final void updateCamera() {
        if (cmx != cmtoX && !lockcmx) {
            cmvx = (cmtoX - cmx) << 2;
            cmdx += cmvx;
            cmx += cmdx >> 4;
            cmdx &= 15;
            if (cmx < 0) {
                cmx = 0;
            }
            if (cmx > cmxLim) {
                cmx = cmxLim;
            }
        }
        if (cmy != cmtoY && !lockcmy) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
            if (cmy < 0) {
                cmy = 0;
            }
            if (cmy > cmyLim) {
                cmy = cmyLim;
            }
        }
        if (this.currentCmVibrate >= 0) {
            cmx += this.CMVIBRATEX[this.currentCmVibrate];
            cmy += this.CMVIBRATEY[this.currentCmVibrate];
            this.currentCmVibrate++;
            if (this.currentCmVibrate == 4) {
                this.currentCmVibrate = -1;
            }
            if (cmx < 0) {
                cmx = 0;
            }
            if (cmx > cmxLim) {
                cmx = cmxLim;
            }
            if (cmy < 0) {
                cmy = 0;
            }
            if (cmy > cmyLim) {
                cmy = cmyLim;
            }
        }
        this.gssx = (cmx >> 4) - 1;
        if (this.gssx < 0) {
            this.gssx = 0;
        }
        this.gssy = cmy >> 4;
        this.gssxe = this.gssx + this.gssw;
        if (this.gssxe > Tilemap.w) {
            this.gssxe = Tilemap.w;
        }
        if (this.gssye > Tilemap.h) {
            this.gssye = Tilemap.h;
        }
        this.gssye = this.gssy + this.gssh;
        if (this.gssy < 0) {
            this.gssy = 0;
        }
        if (this.gssye > Tilemap.h - 1) {
            this.gssye = Tilemap.h - 1;
        }
    }

    public void updateMainCharWhenAuto() {
        if (!autoFight) {
            this.isAutoRangeWhenAuto = false;
        } else if (timeStandWhenAuto - (System.currentTimeMillis() / 1000) <= 0) {
            sendActorMove(this.mainChar.x, this.mainChar.y);
            timeStandWhenAuto = (System.currentTimeMillis() / 1000) + 10;
        }
    }

    public void updateTime() {
        for (int i = 0; i < this.VecTime.size(); i++) {
            TimecountDown t = (TimecountDown) this.VecTime.elementAt(i);
            if (t != null) {
                t.update();
                if (t.wantdestroy) {
                    this.VecTime.removeElement(t);
                }
            }
        }
    }

    @Override
    public void update() {
        game.pokemon.MenuNguCanh.tiepQuan();   // pokemon 0.9.3: menu chữ trên màn chơi -> menu nút to (máy cảm ứng / chuột)
        AutoGuard.tick(); // Tự động đánh chính thức + Menu nhanh (code mod dùng chung)
        for (int i = 0; i < this.VecDataEff.size(); i++) {
            DataSkillEff data = (DataSkillEff) this.VecDataEff.elementAt(i);
            if (data != null) {
                data.update();
                if (data.wantDetroy) {
                    this.VecDataEff.removeElementAt(i);
                }
            }
        }
        Boss_Dragon boss_Dragon = bDragon[0];
        updateTime();
        timeRemovePos--;
        if (timeRemovePos < 0) {
            timeRemovePos = 0;
        }
        int size0 = aeo.size();
        for (int i2 = 0; i2 < size0; i2++) {
            ((Effect) aeo.elementAt(i2)).update();
            if (((Effect) aeo.elementAt(i2)).wantDetroy) {
                aeo.removeElementAt(i2);
            }
        }
        if (this.tk != null) {
            this.tk.update();
            if (this.tk.end) {
                this.tk = null;
            }
        }
        if (charcountdonw != null) {
            charcountdonw.update();
            if (charcountdonw.WantDestroy) {
                charcountdonw = null;
                objcountDown = null;
            }
        }
        if (this.tg != null) {
            this.tg.update();
        }
        updateCamera();
        cmtoX = (this.mainChar.x - gshw) + this.DELTACAMERAX[this.mainChar.dir];
        cmtoY = ((this.mainChar.y - gshh) + this.DELTACAMERAY[this.mainChar.dir]) - 20;
        updateCmMini();
        for (int i3 = arrowsUp.size() - 1; i3 >= 0; i3--) {
            IArrow a = (IArrow) arrowsUp.elementAt(i3);
            a.update();
            if (a.wantDestroy) {
                arrowsUp.removeElementAt(i3);
            }
        }
        for (int i4 = this.arrowsDown.size() - 1; i4 >= 0; i4--) {
            ((IArrow) this.arrowsDown.elementAt(i4)).update();
        }
        int size3 = this.npc_limit.size();
        for (int i5 = size3 - 1; i5 >= 0; i5--) {
            Actor a2 = (Actor) this.npc_limit.elementAt(i5);
            if (a2 != null && a2.typeLimit == 1) {
                a2.update();
            }
        }
        PosNPCQuest.removeAllElements();
        Vector removeactor = new Vector();
        for (int i6 = this.actors.size() - 1; i6 >= 0; i6--) {
            Actor a3 = (Actor) this.actors.elementAt(i6);
            if (a3.isNPC()) {
                getAllPosNPCMInimap(a3);
            }
            if (a3.ispetTool() && a3.wantDestroy) {
                removeactor.addElement(a3);
            }
            if (a3.catagory == 2 || a3.catagory == 10 || a3.catagory == 11 || a3.catagory == 12) {
                a3.update();
            } else {
                a3.update();
                if (!Util.inDataRange(a3, this.mainChar) && a3 != congThanh && a3.catagory != 127 && a3.catagory != 0 && a3.timeLive == -1) {
                    a3.wantDestroy = true;
                }
                if (a3.wantDestroy && a3.timeLive == -1) {
                    if (this.focusedActor == a3) {
                        this.focusedActor = null;
                    }
                    if (!isFindChar && !isActorMove && !isFindMonster && a3.catagory != 100) {
                        if (a3.catagory == 0) {
                            a3.isDie = true;
                        }
                        removeactor.addElement(a3);
                    }
                }
            }
        }
        if (congThanh != null) {
            congThanh.update();
        }
        Util.quickSort(this.actors);
        if (Canvas.gameTick % 10 == 0 && !game.pokemon.HanhDong.giuMucTieu(this)) {   // pokemon: giữ mục tiêu người chơi chọn
            this.focusedActor = findFocusActor();
        }
        if (this.focusedActor != null && this.focusedActor.catagory == 100) {
            this.focusedActor = null;
        }
        EffectManager.lowEffects.updateAll();
        EffectManager.hiEffects.updateAll();
        updateFlyText();
        if (this.pingNextIn > 0) {
            this.pingNextIn--;
            if (this.pingNextIn == 0) {
                this.lastTimePing = System.currentTimeMillis();
                this.gameService.ping();
            }
        }
        if (this.chatMode) {
            this.tfChat.update();
        }
        if (this.indexBuff != -1 && timeBuff != -1 && System.currentTimeMillis() > timeBuff) {
            this.indexBuff = -1;
            timeBuff = -1L;
        }
        if (xSlot > 0) {
            xSlot -= Res.wKhung / 10;
            if (xSlot < 0) {
                xSlot = 0;
            }
        }
        if ((Tilemap.w >= wMiniMap || Tilemap.h >= hMiniMap) && (System.currentTimeMillis() / 100) - timeTranMini > 20) {
            updateminiMap();
        }
        try {
            if (this.posNpcRequest != null && iTaskAuto == 2) {
                updateTask();
                Canvas.endDlg();
            }
        } catch (Exception e) {
            this.mainChar.posTransRoad = null;
            this.mainChar.countRoad = 0;
        }
        if (effAnimate.size() > 0) {
            AnimateEffect.updateWind();
            for (int i7 = 0; i7 < effAnimate.size(); i7++) {
                AnimateEffect ani = (AnimateEffect) effAnimate.elementAt(i7);
                ani.update();
            }
        }
        if (effManager != null) {
            for (int i8 = 0; i8 < effManager.size(); i8++) {
                EffectServerManager eff = (EffectServerManager) effManager.elementAt(i8);
                eff.update();
            }
        }
        for (int i9 = 0; i9 < removeactor.size(); i9++) {
            this.actors.removeElement(removeactor.elementAt(i9));
        }
        if (Canvas.gameTick % 10 == 0) {
            game.pokemon.PetDuyNhat.donDep(this.actors);   // pokemon: pet của người đã rời cảnh / pet thừa
        }
        updateMsgServer();
        setRemoveIconImg();
        setRemoveClothes();
        if (this.skillClan != null) {
            for (int i10 = 0; i10 < 1; i10++) {
                if (this.skillClan[i10].time > 0) {
                    this.skillClan[i10].updateTime();
                }
            }
        }
        if (this.timerung > 0) {
            this.timerung--;
        } else {
            this.timerung = 0;
        }
        if (Tilemap.isOfflineMap && this.map == 29) {
            if (this.mainChar.x >= this.xBeginFrame - 16 && this.mainChar.x <= this.xTheendFrame + 40 && this.mainChar.y >= this.yBeginFrame - 16 && this.mainChar.y <= this.yTheendFrame + 40) {
                this.focusedActor = isFocusMyGround(this.mainChar);
            } else {
                int size6 = this.actors.size();
                for (int i11 = 0; i11 < size6; i11++) {
                    Actor obj = (Actor) this.actors.elementAt(i11);
                    if (obj.catagory == 10) {
                        obj.isFocus = false;
                    }
                }
            }
        }
        if (msgWorld.size() > 0) {
            MsgInfo ms = (MsgInfo) msgWorld.elementAt(0);
            ms.update();
        }
        if (savemsgWorld.size() > 30) {
            savemsgWorld.removeElementAt(0);
        }
        if (this.mainChar.state != 3 && autoFight && (Canvas.currentScreen instanceof GameScr) && Canvas.currentDialog != null && !this.mainChar.isDie && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80) {
            Canvas.currentDialog = null;
        }
        updateMainCharWhenAuto();
        if (Tilemap.lv == 0 || Tilemap.lv == 70 || Tilemap.lv == 80) {
            autoFight = false;
        }
        if (Canvas.gameTick % 200 == 0) {
            Char.SetRemove();
        }
        if (isFixBugAutoQuest) {
            isFixBugAutoQuest = false;
            this.mainChar.xTo = this.mainChar.x;
            this.mainChar.yTo = this.mainChar.y;
            this.mainChar.xBegin = this.mainChar.x;
            this.mainChar.yBegin = this.mainChar.y;
            this.countChang++;
            if (this.countChang > this.xChange.length - 1) {
                this.countChang = 0;
            }
            int xx = (Tilemap.w / 2) + this.xChange[this.countChang];
            int yy = (Tilemap.h / 2) + this.xChange[this.countChang];
            this.mainChar.posTransRoad = updateFindRoad(this.mainChar.x / 16, this.mainChar.y / 16, xx, yy);
            this.mainChar.countRoad = 0;
            iTaskAuto = this.iTaskAutoLast;
        }
    }

    public Actor isFocusMyGround(LiveActor ac) {
        Actor result = null;
        if (Tilemap.isOfflineMap) {
            for (int i = 0; i < this.actors.size(); i++) {
                Actor obj = (Actor) this.actors.elementAt(i);
                if (obj.catagory == 10) {
                    if (ac.x >= obj.x + 2 && ac.x <= obj.x + 30 && ac.y >= obj.y && ac.y + 2 <= obj.y + 30) {
                        obj.isFocus = true;
                        result = obj;
                    } else {
                        obj.isFocus = false;
                    }
                }
            }
        }
        return result;
    }

    private void updateMsgServer() {
        if (this.listMSGServer != null && this.listMSGServer.size() > 0) {
            this.xMsgServer -= 4;
            String str = (String) this.listMSGServer.elementAt(0);
            if (this.xMsgServer + Font.arialFontW.getWidth(str) < 0) {
                this.listMSGServer.removeElementAt(0);
                this.xMsgServer = Canvas.w - 20;
            }
        }
    }

    private void setRemoveIconImg() {
        if (Canvas.gameTick % 250 != 0) {
            return;   // 0.9.5: dọn ~10 giây một lần (trước: duyệt bảng băm mỗi nhịp)
        }
        GameData.donIcon(System.currentTimeMillis() / 1000);
        Enumeration k = GameData.listImgIcon.keys();
        while (k.hasMoreElements()) {
            String key = (String) k.nextElement();
            ImageIcon img = (ImageIcon) GameData.listImgIcon.get(key);
            if (!img.isLoad && (System.currentTimeMillis() / 1000) - ((long) img.timeRemove) > 120) {
                GameData.listImgIcon.remove(key);
            }
        }
    }

    public void onLoadCloth(byte[] arr, byte type, byte id) {
        Hashtable has = (Hashtable) Res.quanao.elementAt(type);
        CharPartInfo cp = (CharPartInfo) has.get(new StringBuffer(String.valueOf((int) id)).toString());
        if (cp == null) {
            Cout.println("NULL");
        } else {
            cp.load(arr, type, id);
        }
    }

    private void setRemoveClothes() {
        for (int i = Res.quanao.size() - 1; i >= 0; i--) {
            Hashtable has = (Hashtable) Res.quanao.elementAt(i);
            Enumeration k = has.keys();
            while (k.hasMoreElements()) {
                String key = (String) k.nextElement();
                CharPartInfo cp = (CharPartInfo) has.get(key);
                if ((System.currentTimeMillis() / 1000) - ((long) cp.timeRemove) > 60) {
                    has.remove(key);
                }
            }
        }
    }

    private void updateCmMini() {
        if (Tilemap.w >= wMiniMap || Tilemap.h >= hMiniMap) {
            if (cmyMini != cmtoYmini) {
                cmvyMini = (cmtoYmini - cmyMini) << 2;
                cmdyMini += cmvyMini;
                cmyMini += cmdyMini >> 4;
                cmdyMini &= 15;
            }
            if (cmxMini != cmtoXMini) {
                cmvxMini = (cmtoXMini - cmxMini) << 2;
                cmdxMini += cmvxMini;
                cmxMini += cmdxMini >> 4;
                cmdxMini &= 15;
            }
        }
    }

    public Actor findFocusActor() {
        int distance;
        int left = this.mainChar.x + C[this.mainChar.dir][0];
        int right = this.mainChar.x + C[this.mainChar.dir][1];
        int up = this.mainChar.y + C[this.mainChar.dir][2];
        int down = this.mainChar.y + C[this.mainChar.dir][3];
        int shorted = 10000;
        int shortedIndex = -1;
        this.nearActors.removeAllElements();
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.x > left && a.x < right && a.y > up && a.y < down) {
                if (a.catagory == 127 || !a.canfocus || ((a.catagory == 0 && a.ID == this.mainChar.ID) || a.catagory == 10 || a.catagory == 11 || a.catagory == 12 || (autoFight && !Canvas.autoTab.isAutoFire && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80 && !Tilemap.isOfflineMap && (a.isItemCanGet() || a.catagory == 126 || a.catagory == 2 || a.catagory == 0)))) {
                    if ((autoFight || autoBomHMP) && a.catagory == 2) {
                        addChat(this.mainChar, "Bạn hãy tắt auto trước khi focus", 100);
                    }
                } else if ((a.catagory != 1 || a.state != 8) && (!autoFight || (a.state != 3 && !a.isDropItem()))) {
                    if (typeOptionFocus == 1) {
                        if ((a.catagory != 0 || (a.iskiller && !a.isNPC())) && (a.catagory == 1 || a.catagory != 0 || a.iskiller)) {
                            this.nearActors.addElement(a);
                            distance = Util.abs(this.mainChar.x - a.x) + Util.abs(this.mainChar.y - a.y);
                            if (a.catagory != 3 || a.catagory == 4) {
                                distance <<= 1;
                            }
                            if (distance < shorted) {
                                shorted = distance;
                                shortedIndex = i;
                            }
                        }
                    } else if (typeOptionFocus == 2) {
                        if (a.isNPC()) {
                            this.nearActors.addElement(a);
                            distance = Util.abs(this.mainChar.x - a.x) + Util.abs(this.mainChar.y - a.y);
                            if (a.catagory != 3) {
                                distance <<= 1;
                            } else {
                                distance <<= 1;
                            }
                            if (distance < shorted) {
                                shorted = distance;
                                shortedIndex = i;
                            }
                        }
                    } else if (typeOptionFocus == 3) {
                        if ((a.catagory != 0 || a.idClan != this.mainChar.idClan) && !a.isNPC()) {
                            this.nearActors.addElement(a);
                            distance = Util.abs(this.mainChar.x - a.x) + Util.abs(this.mainChar.y - a.y);
                            if (a.catagory != 3) {
                                distance <<= 1;
                            } else {
                                distance <<= 1;
                            }
                            if (distance < shorted) {
                                shorted = distance;
                                shortedIndex = i;
                            }
                        }
                    } else if (typeOptionFocus != 4 || ((a.catagory != 0 || a.nationID != this.mainChar.nationID) && !a.isNPC())) {
                        this.nearActors.addElement(a);
                        distance = Util.abs(this.mainChar.x - a.x) + Util.abs(this.mainChar.y - a.y);
                        if (a.catagory != 3) {
                            distance <<= 1;
                        } else {
                            distance <<= 1;
                        }
                        if (distance < shorted) {
                            shorted = distance;
                            shortedIndex = i;
                        }
                    }
                }
            }
        }
        if (shortedIndex == -1) {
            return null;
        }
        if (autoFight && Canvas.autoTab.isAutoFire) {
            for (int i2 = 0; i2 < this.nearActors.size(); i2++) {
                Actor ac = (Actor) this.nearActors.elementAt(i2);
                if (ac != null && ac.iskiller) {
                    return ac;
                }
            }
        }
        if (this.focusedActor == null && shortedIndex < this.actors.size()) {
            return (Actor) this.actors.elementAt(shortedIndex);
        }
        if (!Canvas.instance.hasPointerEvents() && shortedIndex < this.actors.size()) {
            if (this.nearActors.contains(this.focusedActor)) {
                return this.focusedActor;
            }
            return (Actor) this.actors.elementAt(shortedIndex);
        }
        if (Util.distance(this.mainChar.x, this.mainChar.y, this.focusedActor.x, this.focusedActor.y) > Canvas.w / 2) {
            this.focusedActor = null;
        }
        return this.focusedActor;
    }

    public void onStarDialLucky(int type) {
        DialLuckyScr.gI().switchToMe();
        DialLuckyScr.gI().lastScr = this;
    }

    public Actor getActor(int id) {
        for (int i = 0; i < this.actors.size(); i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac != null && ac.catagory == 0 && ac.ID == id) {
                return ac;
            }
        }
        return null;
    }

    public void changeToNextFocusActor(boolean isBack) {
        if (this.focusedActor == null) {
            this.focusedActor = findFocusActor();
            if (this.focusedActor != null && this.focusedActor.catagory == 100) {
                this.focusedActor = null;
                return;
            }
            return;
        }
        if (isBack && !this.focusedActor.iskiller && autoFight && typeOptionFocus == 1) {
            this.focusedActor = findFocusActor();
            if (this.focusedActor != null && this.focusedActor.iskiller) {
                Cout.println2(" tim thay va thoat rAa");
                return;
            }
        }
        int i = this.nearActors.indexOf(this.focusedActor);
        int nextIndex = i + 1;
        if (nextIndex >= this.nearActors.size() || nextIndex < 0) {
            nextIndex = 0;
        }
        if (this.nearActors.size() > 0) {
            this.focusedActor = (Actor) this.nearActors.elementAt(nextIndex);
        }
        if (this.focusedActor != null && this.focusedActor.catagory == 100) {
            this.focusedActor = null;
        }
    }

    public static void addChat(Actor ac, String str, int time) {
        if (ac == null) {
            // 0.9.4: mất mục tiêu (NPC đã rời / đổi focus) giữa hội thoại: vẫn hiện lời ở hộp đáy, không ném NPE
            game.pokemon.HopThoaiDuoi.dat("", str, time);
            return;
        }
        // pokemon: lời NPC (và lời người chơi đáp lại khi đang trò chuyện) -> hộp thoại đáy màn kiểu GBA
        if (ac instanceof NPC || Canvas.gameScr != null && ac == Canvas.gameScr.mainChar
                && (game.pokemon.HopThoaiDuoi.dangBat() || currQuest != null && currQuest.isShow)) {
            String ten = ac instanceof NPC ? ((NPC) ac).name : ((Char) ac).name;
            game.pokemon.HopThoaiDuoi.dat(ten, str, time);
            ac.chat = null;
            return;
        }
        ac.chat = new ChatPopup(time, str, 1);
        ac.chat.setPos(ac.x, ac.y - ac.height);
    }

    private boolean doTalkWithNPCQuest() {
        if (this.mainChar.receiveQuest && this.mainChar.typeQuest == 1 && this.receiveQuest) {
            if (Canvas.isKeyPressed(5) || Canvas.isKeyPressed(12)) {
                this.indexContent++;
                if (this.indexContent <= this.mainChar.info_npc_quest.length - 1) {
                    this.mainChar.chat = null;
                    this.focusedActor.chat = null;
                    if (this.mainChar.info_npc_quest[this.indexContent].startsWith("1")) {
                        addChat(this.focusedActor, this.mainChar.info_npc_quest[this.indexContent].substring(1), 500);
                        return true;
                    }
                    addChat(this.mainChar, this.mainChar.info_npc_quest[this.indexContent].substring(1), 500);
                    return true;
                }
                this.receiveQuest = false;
                MainChar mainChar = this.mainChar;
                mainChar.nextNpc = (byte) (mainChar.nextNpc + 1);
                if (this.mainChar.nextNpc >= this.mainChar.npc_quest.length) {
                    this.gameService.talk_npc(this.mainChar.npc_quest[this.mainChar.nextNpc - 1], 1, this.questID);
                    dellPotionQuest();
                    return true;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean doTalkWithNPCResponse() {
        return false;
    }

    public void set_npc_request(short type) {
        if (this.idNpcReceive != -1) {
            type = this.idNpcReceive;
        } else {
            this.current_npc_talk = type;
        }
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if ((ac instanceof NPC) && ((NPC) ac).type == type) {
                this.posNpcRequest = new Position(ac.x, ac.y);
                return;
            }
        }
    }

    public void paintPosNPCQuest(Graphics g) {
        for (int i = 0; i < PosNPCQuest.size(); i++) {
            Position p = (Position) PosNPCQuest.elementAt(i);
            paintPoint(g, p, p.indexColor);
        }
    }

    private boolean doReceiveQuest() {
        if (this.receiveQuest) {
            if (Canvas.isKeyPressed(5) || Canvas.isKeyPressed(12)) {
                this.indexContent++;
                if (this.indexContent <= this.content_quest.length - 1) {
                    this.mainChar.chat = null;
                    this.focusedActor.chat = null;
                    if (this.content_quest[this.indexContent].startsWith("1")) {
                        addChat(this.focusedActor, this.content_quest[this.indexContent].substring(1), 500);
                    } else {
                        addChat(this.mainChar, this.content_quest[this.indexContent].substring(1), 500);
                    }
                    if (this.indexContent == this.content_quest.length - 1) {
                        Vector menuItems = new Vector();
                        menuItems.addElement(new Command("Đồng ý", new IAction() {
                            @Override
                            public void perform() {
                                GameService.gI().receiveQuest(GameScr.this.idNpcReceive, GameScr.this.questID, 1);
                                Canvas.startWaitDlg();
                                GameScr.this.receiveQuest = false;
                            }
                        }));
                        if (this.mainChar.typeQuest != 3) {
                            menuItems.addElement(new Command("Không", new IAction() {
                                @Override
                                public void perform() {
                                    GameScr.this.receiveQuest = false;
                                    GameService.gI().receiveQuest(GameScr.this.idNpcReceive, GameScr.this.questID, 2);
                                }
                            }));
                        }
                        Canvas.menu.startAt(menuItems, 3);
                        return true;
                    }
                    return true;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    /** pokemon: đang trò chuyện có bước tiếp (mở đầu map 105 gần ông, nhận / trả nhiệm vụ). */
    public boolean dangTroChuyen() {
        if (Tilemap.lv == 105 && this.focusedActor instanceof NPC && this.mainChar != null
                && this.mainChar.y - this.focusedActor.y <= 32) {
            return true;
        }
        // 0.9.4: hội thoại nhận / trả nhiệm vụ (Quest.isShow) cũng là "đang trò chuyện" - trước đây chỉ xét cờ
        // receiveQuest (không bao giờ bật) nên chạm / bấm bị hộp thoại đáy nuốt (tat) thay vì sang lời, Quest.update
        // chờ phím 5 mãi, updateKey thoát sớm mỗi nhịp -> màn chơi "treo" ngay nhiệm vụ đầu tiên.
        if (currQuest != null && currQuest.isShow) {
            return true;
        }
        return this.receiveQuest;
    }

    private boolean doIntro() {
        if (Tilemap.lv == 105) {
            String[] intro = {"1Cháu của ta", "1Nay con đã lớn", "1đã đến lúc con phải được học hỏi nhiều hơn", "1phải trải nghiệm nhiều hơn từ quê hương đất nước", "1và có nhiều điều để con phải học hỏi nơi biết bao nhà huấn luyện tài ba.", "1Năm xưa ta có người bạn thân là Xatu", "1ông ấy giờ đang là trưởng làng của làng này.", "1con hãy đi xuống phương Nam và tìm gặp người này", "1ông ấy sẽ giúp con gia nhập làng huấn luyện này", "1Con hãy cố gắng để trưởng thành, để có thể giúp được quê hương", "1để có được sự nghiệp riêng và để tìm được nguồn gốc và cha mẹ của con…", "1Hãy lên đường đi", "1..nơi đây ta luôn dõi theo bước chân của con", "0Vâng! thưa nội con đi", "1Uhm, chúc con lên đường bình an."};
            if (!(this.focusedActor instanceof NPC)) {
                // 0.9.4: người chơi khác cùng ở màn mở đầu giành mất mục tiêu (focus = Char) -> trước đây nhân vật đi
                // lên mãi qua ông nội, không bao giờ vào hội thoại. Tự nhắm ông (NPC) khi đã tới ngang / vượt qua.
                for (int i = 0; i < this.actors.size(); i++) {
                    Object o = this.actors.elementAt(i);
                    if (o instanceof NPC && this.mainChar != null && this.mainChar.y - ((NPC) o).y <= 32) {
                        this.focusedActor = (NPC) o;
                        break;
                    }
                }
            }
            if ((this.focusedActor == null || !(this.focusedActor instanceof NPC)) && Canvas.currentDialog == null) {
                Canvas.keyHold[2] = true;
                this.indexContent = -1;
                doActorMove();
                return false;
            }
            Canvas.keyHold[2] = false;
            if (this.mainChar.y - this.focusedActor.y <= 32) {   // 0.9.4: tới ngang HOẶC đã đi quá ông (trước: chỉ |dy| <= 32)
                if (this.indexContent == -1) {
                    Canvas.keyPressed[5] = true;
                }
                if (Canvas.isPointerClick) {
                    // pokemon: chạm BẤT KỲ ĐÂU = nghe tiếp (bản gốc chỉ nhận chạm trúng ông)
                    Canvas.isPointerClick = false;
                    Canvas.keyPressed[5] = true;
                }
                if (Canvas.isKeyPressed(12) || Canvas.isKeyPressed(13)) {
                    Canvas.keyPressed[5] = true;   // phím mềm cũng là "tiếp" (không kẹt khi không biết phím)
                }
                if (Canvas.isKeyPressed(5) && this.indexContent < intro.length) {
                    this.indexContent++;
                    if (this.indexContent >= intro.length) {
                        this.mainChar.state = (byte) 0;
                        Random r2 = new Random(System.currentTimeMillis());
                        int x = 13 + (r2.nextInt() % 5);
                        int y = 11 + (r2.nextInt() % 5);
                        GameService.gI().changeMap(0, x, y);
                        game.pokemon.HopThoaiDuoi.giu(false);
                        game.pokemon.HopThoaiDuoi.tat();
                        Canvas.keyHold[2] = false;
                        Canvas.startWaitDlg("Chuyển màn..", true);
                        return true;
                    }
                    this.mainChar.chat = null;
                    this.focusedActor.chat = null;
                    if (intro[this.indexContent].startsWith("1")) {
                        addChat(this.focusedActor, intro[this.indexContent].substring(1), 500);
                        return true;
                    }
                    addChat(this.mainChar, intro[this.indexContent].substring(1), 700);
                    return true;
                }
                return true;
            }
            Canvas.keyHold[2] = true;
            this.indexContent = -1;
            doActorMove();
            return false;
        }
        return false;
    }

    private void doActorMove() {
        if (Canvas.keyHold[2] || Canvas.keyHold[4] || Canvas.keyHold[6] || Canvas.keyHold[8]) {
            this.duoiMucTieu = null; // tự bấm phím đi: thôi đuổi
        }
        if (Canvas.isKeyPressed(2)) {
            this.dir = (byte) 1;
        }
        if (Canvas.isKeyPressed(4)) {
            this.dir = (byte) 2;
        }
        if (Canvas.isKeyPressed(6)) {
            this.dir = (byte) 3;
        }
        if (Canvas.isKeyPressed(8)) {
            this.dir = (byte) 4;
        }
        boolean move = false;
        boolean getKeyMove = false;
        int oldx = this.mainChar.x;
        int oldy = this.mainChar.y;
        int olddir = this.mainChar.dir;
        int xx = 0;
        int yy = 0;
        int v = this.mainChar.speed;
        if (Canvas.keyHold[2]) {
            this.mainChar.stop = false;
            removeFocusWhenPutKey();
            autoFight = false;
            yy = -16;
            if (congThanh == null || this.mainChar.y > congThanh.y + 40) {
                move = true;
                getKeyMove = true;
            }
            this.mainChar.dir = (short) 1;
            this.moveToX = this.mainChar.x;
            this.moveToY = (short) (this.mainChar.y - v);
            try {
                if (Tilemap.tileTypeAtPixel(this.moveToX, this.moveToY, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                    this.moveToY = this.mainChar.y;
                    if (this.mainChar.setWay(0, -8)) {
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        return;
                    }
                    move = false;
                }
            } catch (Exception e) {
                this.moveToY = this.mainChar.y;
                e.printStackTrace();
            }
        } else if (Canvas.keyHold[8]) {
            this.mainChar.stop = false;
            removeFocusWhenPutKey();
            autoFight = false;
            yy = 16;
            move = true;
            getKeyMove = true;
            this.mainChar.dir = (short) 0;
            this.moveToX = this.mainChar.x;
            this.moveToY = (short) (this.mainChar.y + v);
            try {
                if (Tilemap.tileTypeAtPixel(this.moveToX, this.moveToY, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                    this.moveToY = this.mainChar.y;
                    if (this.mainChar.setWay(0, 8)) {
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        return;
                    }
                    move = false;
                }
            } catch (Exception e2) {
                this.moveToY = this.mainChar.y;
                e2.printStackTrace();
            }
        } else if (Canvas.keyHold[4]) {
            this.mainChar.stop = false;
            removeFocusWhenPutKey();
            autoFight = false;
            xx = -16;
            move = true;
            getKeyMove = true;
            this.mainChar.dir = (short) 2;
            this.moveToX = (short) (this.mainChar.x - v);
            this.moveToY = this.mainChar.y;
            try {
                if (Tilemap.tileTypeAtPixel(this.moveToX, this.moveToY, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                    this.moveToX = this.mainChar.x;
                    if (this.mainChar.setWay(-8, 0)) {
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        return;
                    }
                    move = false;
                }
            } catch (Exception e3) {
                this.moveToX = this.mainChar.x;
                e3.printStackTrace();
            }
        } else if (Canvas.keyHold[6]) {
            this.mainChar.stop = false;
            removeFocusWhenPutKey();
            autoFight = false;
            xx = 16;
            move = true;
            getKeyMove = true;
            this.mainChar.dir = (short) 3;
            this.moveToY = this.mainChar.y;
            this.moveToX = (short) (this.mainChar.x + v);
            try {
                if (Tilemap.tileTypeAtPixel(this.moveToX, this.moveToY, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                    this.moveToX = this.mainChar.x;
                    if (this.mainChar.setWay(8, 0)) {
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        return;
                    }
                    move = false;
                }
            } catch (Exception e4) {
                this.moveToX = this.mainChar.x;
                e4.printStackTrace();
            }
        }
        if (!this.mainChar.FuntioncanMove()) {
            return;
        }
        if (getKeyMove && checkCanChangeMap(oldx + xx, oldy + yy, olddir)) {
            return;
        }
        if (move) {
            if (this.moveToX < 0) {
                this.moveToX = this.mainChar.x;
            }
            if (this.moveToY < 0) {
                this.moveToY = this.mainChar.y;
            }
            if (this.moveToX > Tilemap.w * 16) {
                this.moveToX = this.mainChar.x;
            }
            if (this.moveToY > Tilemap.h * 16) {
                this.moveToX = this.mainChar.y;
            }
            this.mainChar.moveTo(this.moveToX, this.moveToY);
            if (saveAutoFight && typeOptionFocus == 1) {
                Canvas.gameScr.mainChar.lastXAuto = this.moveToX;
                Canvas.gameScr.mainChar.lastYAuto = this.moveToY;
            }
            this.changeSinceLastUpdate = true;
            this.mainChar.comeHome = false;
            this.countStep = (byte) (this.countStep + 1);
        }
        if (System.currentTimeMillis() - timeMove >= 0 && !this.mainChar.stop) {
            timeMove = System.currentTimeMillis() + ((long) dtmove);
            this.gameService.moveChar(this.moveToX, this.moveToY);
        }
    }

    public boolean checkMoveLimit(int xTo, int yTo) {
        if (this.npc_limit.size() == 0) {
            return false;
        }
        int size0 = this.npc_limit.size();
        for (int i = 0; i < size0; i++) {
            Actor a = (Actor) this.npc_limit.elementAt(i);
            if (a != null) {
                Npc_Server npc = (Npc_Server) a;
                if (xTo >= npc.getLimxL() && xTo <= npc.getLimxR() && yTo >= npc.getLimyU() && yTo <= npc.getLimyD()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeFocusWhenPutKey() {
        if (this.map == 17 && this.mainChar.isDoing) {
            this.focusedActor = null;
            this.coutChangeFocusWhenDoing = 0;
        }
    }

    @Override
    public void updateKey() {
        if (this.chatMode && Canvas.isPointerClick) {
            switch (Canvas.collisionCmdBar()) {
                case 0:
                    Canvas.clearKeyHold();
                    Canvas.clearKeyPressed();
                    this.chatMode = false;
                    Canvas.isPointerClick = false;
                    break;
                case 1:
                    String text = this.tfChat.getText();
                    this.tfChat.setText("");
                    doChatFromMe(text);
                    Canvas.isPointerClick = false;
                    break;
                case 2:
                    this.tfChat.clear();
                    Canvas.isPointerClick = false;
                    break;
            }
            return;
        }
        if (Canvas.keyPressed[11]) {
            cheat = !cheat;
        }
        // pokemon: hộp thoại đáy màn - chạm / bấm BẤT KỲ ĐÂU trên màn = tiếp (máy chữ đang chạy: hiện hết)
        boolean dangNoi = dangTroChuyen();
        game.pokemon.HopThoaiDuoi.giu(dangNoi);
        if (game.pokemon.HopThoaiDuoi.dangBat() && Canvas.currentDialog == null && !Canvas.menu.showMenu) {
            boolean cham = Canvas.isPointerClick;
            if (cham || (!dangNoi && Canvas.isKeyPressed(5))) {
                if (!game.pokemon.HopThoaiDuoi.xongChu()) {
                    game.pokemon.HopThoaiDuoi.hienHetChu();
                    Canvas.isPointerClick = false;
                    Canvas.keyPressed[5] = false;
                    return;
                }
                if (dangNoi) {
                    Canvas.isPointerClick = false;
                    Canvas.keyPressed[5] = true;   // tiếp lời (doIntro / nhiệm vụ đọc phím 5)
                } else {
                    game.pokemon.HopThoaiDuoi.tat();   // lời thông tin: chạm = đóng
                    Canvas.isPointerClick = false;
                    Canvas.keyPressed[5] = false;
                    return;
                }
            }
        }
        if (doIntro() || this.chatMode) {
            return;
        }
        if (currQuest != null && currQuest.isShow) {
            // 0.9.4: chạm / bấm chuột bất kỳ đâu = nghe tiếp (cả khi lời đang là bong bóng, không có hộp đáy)
            if (Canvas.isPointerClick) {
                Canvas.isPointerClick = false;
                Canvas.keyPressed[5] = true;
            }
            currQuest.update(this.mainChar, this.focusedActor);
            return;
        }
        if (doTalkWithNPCQuest() || doTalkWithNPCResponse() || doReceiveQuest()) {
            return;
        }
        int aa = 0;
        if (idChiemThanh != null) {
            for (int i = 0; i < idChiemThanh.length; i++) {
                if (idChiemThanh[i] == this.mainChar.ID && ((long) timeChiemThanh[i]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i])) >= 0) {
                    aa = 1;
                }
            }
        }
        if (Canvas.instance.hasPointerEvents() && aa == 0) {
            updatePoint();
        }
        super.updateKey();
        game.pokemon.HanhDong.tick(this);   // pokemon: chạm NPC -> tới gần thì nói chuyện
        if (this.mainChar.justStopFromRun) {
            this.mainChar.justStopFromRun = false;
            this.changeSinceLastUpdate = true;
            this.lastUpdateMyPositionTime = 0L;
            this.mainChar.stop = false;
            sendActorMove(this.mainChar.x, this.mainChar.y);
            this.changeSinceLastUpdate = false;
        }
        if (Canvas.isKeyPressed(10)) {
            doParty();
            Canvas.clearKeyHold();
            return;
        }
        if (Canvas.isKeyPressed(11)) {
            Vector menu = new Vector();
            // pokemon: Tự động đánh chính thức (máy chủ điều khiển) + Menu nhanh (âm thanh, nút) cho máy bấm phím
            menu.addElement(new Command("Tự động đánh", new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                    BangTuDong.mo();
                }
            }));
            menu.addElement(new Command("Menu nhanh", new IAction() {
                @Override
                public void perform() {
                    Canvas.endDlg();
                    MenuNhanh.moBang();
                }
            }));
            menu.addElement(new Command("Chat", new IAction() {
                @Override
                public void perform() {
                    GameScr.typechat = GameScr.CHAT_NORMAL;
                    Canvas.gameScr.chatMode = true;
                    GameScr.this.tfChat.setIputType(0);
                    Canvas.endDlg();
                }
            }));
            menu.addElement(new Command("Thay đồ", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().setConfig(6);
                    Canvas.endDlg();
                }
            }));
            menu.addElement(new Command("Tìm bạn", new IAction() {
                @Override
                public void perform() {
                    GameScr.typechat = GameScr.CHAT_TIM_BAN;
                    Canvas.gameScr.chatMode = true;
                    GameScr.this.tfChat.setIputType(0);
                    Canvas.endDlg();
                }
            }));
            Canvas.menu.startAt(menu, 3);
            return;
        }
        if (autoFight) {
            if (this.indexBuff != -1 && timeBuff - (System.currentTimeMillis() / 1000) > 0) {
                isBeginAutoBuff = true;
            } else {
                isBeginAutoBuff = false;
            }
        }
        if (this.mainChar.state != 3 && ((autoFight || autoBomHMP) && !this.mainChar.isDie && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80 && !Tilemap.isOfflineMap)) {
            if (autoBomHMP && ((Canvas.autoTab.isAutoMHP || Canvas.autoTab.isAll) && timeDelayHpMp - (System.currentTimeMillis() / 1000) <= 0)) {
                int nHp = (this.mainChar.hp * 100) / this.mainChar.maxhp;
                int nMp = (this.mainChar.mp * 100) / this.mainChar.maxmp;
                if (nHp < BangAuto.bomHp && this.mainChar.hp < this.mainChar.maxhp) {
                    isAutoBomHp = true;
                }
                if (nMp < BangAuto.bomMp && this.mainChar.mp < this.mainChar.maxmp) {
                    isAutoBomMp = true;
                }
                if (nHp >= BangAuto.bomHp) {
                    isAutoBomHp = false;
                }
                if (nMp >= BangAuto.bomMp) {
                    isAutoBomMp = false;
                }
                if (isAutoBomHp && !this.mainChar.beStune) {
                    doFire(7, indexTabSlot);
                }
                if (isAutoBomMp && !this.mainChar.beStune) {
                    doFire(9, indexTabSlot);
                }
                timeDelayHpMp = (System.currentTimeMillis() / 1000) + 1;
            }
            if (autoFight && ((Canvas.autoTab.isAutoFire || Canvas.autoTab.isAll) && ((long) BangAuto.tgDelay) - (System.currentTimeMillis() / 100) <= 0)) {
                if (this.mainChar.isDoing) {
                    if (this.focusedActor != null && this.focusedActor.catagory != 2 && this.focusedActor.catagory != 0 && this.focusedActor.catagory != 126) {
                        Canvas.keyPressed[5] = true;
                    } else {
                        changeToNextFocusActor(false);
                    }
                } else {
                    if (this.focusedActor != null && this.focusedActor.catagory != 2 && this.focusedActor.catagory != 126) {
                        boolean isAC = false;
                        if (this.focusedActor.catagory == 0 && !this.focusedActor.iskiller) {
                            changeToNextFocusActor(false);
                            isAC = true;
                        }
                        if (!isAC) {
                            Canvas.keyPressed[this.dem] = true;
                            this.dem += 2;
                            if (this.dem > 5) {
                                this.dem = 1;
                            }
                        }
                    } else {
                        changeToNextFocusActor(false);
                    }
                    if (!isBeginAutoBuff && (this.mainChar.clazz != 2 || !isBuffHoisinh())) {
                        doFire(1, 1);
                        doFire(3, 1);
                        doFire(5, 1);
                    }
                }
                BangAuto.tgDelay = (int) ((System.currentTimeMillis() / 100) + 10);
            }
            if (Canvas.keyPressed[7]) {
                Canvas.keyPressed[7] = false;
                doFire(7, indexTabSlot);
            }
            if (Canvas.keyPressed[9]) {
                Canvas.keyPressed[9] = false;
                doFire(9, indexTabSlot);
            }
        }
        int i2 = 0;
        while (true) {
            if (i2 >= this.QUICKSLOT_KEY.length) {
                break;
            }
            if (Canvas.isKeyPressed(this.QUICKSLOT_KEY[i2])) {
                if (!this.mainChar.beStune) {
                    doFire(this.QUICKSLOT_KEY[i2], indexTabSlot);
                }
                break;
            }
            i2++;
        }
        if (Canvas.menu.showMenu) {
            Canvas.keyPressed[5] = false;
            return;
        }
        if ((Canvas.isKeyPressed(2) || Canvas.isKeyPressed(4) || Canvas.isKeyPressed(6) || Canvas.isKeyPressed(8)) && this.mainChar.state == 3) {
            this.mainChar.stop = false;
            return;
        }
        if ((this.mainChar.state == 1 || this.mainChar.state == 0 || this.mainChar.state == 4) && aa == 0) {
            doActorMove();
        }
    }

    public boolean isBuffHoisinh() {
        byte[] a = {1, 3, 5};
        for (byte b : a) {
            QuickSlot qs = MainChar.quickSlot[1][this.SLOTINDEXOFKEY[b]];
            if (this.mainChar.clazz == 2 && qs.getSkillType() == 6) {
                return true;
            }
        }
        return false;
    }

    private void updateTask() {
        if (this.mainChar.posTransRoad == null) {
            if (Char.skillLevelLearnt[0] == 0) {
                if (this.posNpcRequest != null && this.posNpcRequest.x / 16 == 6 && this.posNpcRequest.y / 16 == 7) {
                    this.posNpcRequest.x = 160;
                }
                if (Util.distance(this.mainChar.x, this.mainChar.y, this.posNpcRequest.x, this.posNpcRequest.y) > 32) {
                    this.mainChar.posTransRoad = updateFindRoad(this.mainChar.x / 16, this.mainChar.y / 16, this.posNpcRequest.x / 16, (this.posNpcRequest.y / 16) + 2);
                    this.mainChar.countRoad = 0;
                    return;
                }
                this.mainChar.dir = (short) 1;
                if ((System.currentTimeMillis() / 100) - this.timeTaskAuto > timeDelayTask) {
                    timeDelayTask = 3;
                    for (int i = 0; i < this.actors.size(); i++) {
                        Actor ac = (Actor) this.actors.elementAt(i);
                        if ((ac instanceof NPC) && ((NPC) ac).type == this.idNpcReceive) {
                            this.focusedActor = ac;
                            break;
                        }
                    }
                    this.timeTaskAuto = System.currentTimeMillis() / 100;
                    if (Canvas.menu.showMenu) {
                        Canvas.menu.center.action.perform();
                        Canvas.menu.showMenu = false;
                        return;
                    } else {
                        Canvas.keyPressed[5] = true;
                        return;
                    }
                }
                return;
            }
            iTaskAuto = 0;
        }
    }

    public boolean checkCanChangeMap(int x, int y, int dir) {
        CMLocation toMap = Tilemap.toOtherMapAt(x + this.X_FOWARD[dir], y + this.Y_FOWARD[dir]);
        if (toMap != null) {
            ChangScr.gI().setMap(this.map, toMap.toX, toMap.toY);
            this.mainChar.state = (byte) 0;
            if (!Tilemap.isOfflineMap) {
                if (!isOffLineMap(toMap.toM)) {
                    int index = getIndexArrMap();
                    final String[] name = Res.getNameMap(index, toMap.toM);
                    final short[] local = Res.getLocation(index, toMap.toM);
                    if (name == null || local == null) {
                        // map khong co trong bang vi tri (goi 105) - vd dau truong 227: truoc day NullPointer moi
                        // khung hinh khi dung canh o cong (BotChucNang cong_tat_ca). Bo qua o cong nay.
                        return false;
                    }
                    Vector menuItem = new Vector();
                    for (int i = 0; i < name.length; i++) {
                        final int k = i;
                        if (!name[i].equals("")) {
                            menuItem.addElement(new Command(game.pokemon.TenMapPokemon.doiHien(name[i]), new IAction() {
                                @Override
                                public void perform() {
                                    GameService.gI().changeMap(local[k * 3], local[(k * 3) + 1], local[(k * 3) + 2]);
                                    ChangScr.gI().switchToMe();
                                    Res.removeMonsterTemplet();
                                    GameScr.nameMap = game.pokemon.TenMapPokemon.doiHien(name[k]);
                                }
                            }));
                        }
                    }
                    Canvas.menu.startAt(menuItem, 3);
                    Canvas.menu.setIndex();
                    return true;
                }
                oldMap = this.map;
                GameService.gI().changeMap(toMap.toM, toMap.toX, toMap.toY);
                ChangScr.gI().switchToMe();
                Res.removeMonsterTemplet();
                return true;
            }
            GameService.gI().changeMap(oldMap, toMap.toX, toMap.toY);
            ChangScr.gI().switchToMe();
            Res.removeMonsterTemplet();
            return true;
        }
        return false;
    }

    private void doFire(int key, int index) {
        if (this.mainChar.state == 3) {
            Canvas.msgdlg.isIcon = false;
            this.indexBuff = -1;
            timeBuff = 0L;
            this.mainChar.resetBuff();
            if (saveAutoFight) {
                autoFight = saveAutoFight;
            }
            if (BangAuto.isAutoComeHome && autoFight) {
                GameService.gI().comeHomeWhenDie();
                ChangScr.gI().switchToMe();
                ChangScr.gI().setMap(this.map, 0, 0);
                saveAutoFight = false;
                autoFight = false;
                return;
            }
            // pokemon: server moi -> hop thoai guc / hoi sinh (HopHoiSinh, lenh -101)
            if (game.pokemon.HopHoiSinh.moLai()) {
                return;
            }
            Canvas.msgdlg.setInfo("Quay về làng.", new Command("Về làng", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().comeHomeWhenDie();
                    ChangScr.gI().switchToMe();
                    ChangScr.gI().setMap(GameScr.this.map, 0, 0);
                    GameScr.saveAutoFight = false;
                    GameScr.autoFight = false;
                }
            }), new Command("Hồi sinh", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doCustomPopup(GameScr.this.mainChar.ID, (byte) -1, "", 1);
                    Canvas.currentDialog = null;
                }
            }), new Command("Đóng", new IAction() {
                @Override
                public void perform() {
                    Canvas.currentDialog = null;
                }
            }));
            Canvas.currentDialog = Canvas.msgdlg;
            return;
        }
        this.mainChar.stand = System.currentTimeMillis();
        if (this.mainChar.state == 3) {
            Canvas.msgdlg.isIcon = false;
            this.indexBuff = -1;
            timeBuff = 0L;
            this.mainChar.resetBuff();
            if (saveAutoFight) {
                autoFight = saveAutoFight;
            }
            if (BangAuto.isAutoComeHome && autoFight) {
                GameService.gI().comeHomeWhenDie();
                ChangScr.gI().switchToMe();
                ChangScr.gI().setMap(this.map, 0, 0);
                saveAutoFight = false;
                autoFight = false;
                return;
            }
            // pokemon: server moi -> hop thoai guc / hoi sinh (HopHoiSinh, lenh -101)
            if (game.pokemon.HopHoiSinh.moLai()) {
                return;
            }
            Canvas.msgdlg.setInfo("Quay về làng.", new Command("Về làng", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().comeHomeWhenDie();
                    ChangScr.gI().switchToMe();
                    ChangScr.gI().setMap(GameScr.this.map, 0, 0);
                    GameScr.saveAutoFight = false;
                    GameScr.autoFight = false;
                }
            }), new Command("Hồi sinh", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doCustomPopup(GameScr.this.mainChar.ID, (byte) -1, "", 1);
                    Canvas.currentDialog = null;
                }
            }), new Command("Đóng", new IAction() {
                @Override
                public void perform() {
                    Canvas.currentDialog = null;
                }
            }));
            Canvas.currentDialog = Canvas.msgdlg;
            return;
        }
        if (this.focusedActor != null && this.focusedActor.isNpcChar()) {
            Vector allquest = getAllQuestNpc(this.focusedActor.getTypeNpc());
            if (allquest.size() > 0 && this.focusedActor.isNpcChar()) {
                allquest.addElement(new Command("Nói chuyện", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestNPCInfo(((Char) GameScr.this.focusedActor).idBoss);
                    }
                }));
                Canvas.menu.startAt(allquest, 3);
                return;
            } else {
                GameService.gI().requestNPCInfo(((Char) this.focusedActor).idBoss);
                return;
            }
        }
        QuickSlot qs = MainChar.quickSlot[index][this.SLOTINDEXOFKEY[key]];
        if (qs.quickslotType != 2) {
            if (qs.isBuff && !(this.focusedActor instanceof NPC)) {
                byte level = Char.skillLevelLearnt[qs.getSkillType()];
                if (level <= 0) {
                    return;
                }
                byte b = SkillManager.EFF_BUFF_SKILL[Canvas.gameScr.mainChar.clazz][qs.getSkillType() - 4];
                int mpLost = SkillManager.getSkillMP(qs.getSkillType(), Char.skillLevelLearnt[qs.getSkillType() - 4], Canvas.gameScr.mainChar.clazz);
                if (mpLost > Canvas.gameScr.mainChar.mp) {
                    if (!this.first) {
                        addChat(new ChatInfo("", "Không đủ MP", 0));
                        this.first = true;
                        return;
                    }
                    return;
                }
                this.first = false;
                if (this.focusedActor != null && this.focusedActor.catagory == 0) {
                    if (qs.getSkillType() == 6) {
                        GameService.gI().useBuff(this.focusedActor.ID, (byte) 0, b, (short) 0);
                        this.mainChar.mp -= mpLost;
                        return;
                    } else {
                        this.mainChar.mp -= mpLost;
                        this.indexBuff = qs.getSkillType();
                        WindowInfoScr.gI().doCastBuffToActor(this.mainChar, this.focusedActor, b, SkillManager.SKILL_CAN_BUFF_TO_USER[this.mainChar.clazz][qs.getSkillType() - 4] == 1);
                        return;
                    }
                }
                if (qs.getSkillType() != 6) {
                    this.mainChar.mp -= mpLost;
                    if (qs.isBuff && (Canvas.gameScr.indexBuff == -1 || Canvas.gameScr.indexBuff == qs.getSkillType())) {
                        Canvas.gameScr.indexBuff = qs.getSkillType();
                    }
                    long timeSinceUseThisSkill = System.currentTimeMillis() - this.mainChar.timeLastUseSkills[qs.getSkillType()];
                    if (autoFight && timeSinceUseThisSkill <= Canvas.gameScr.mainChar.coolDownSkill[qs.getSkillType()] && timeBuff - (System.currentTimeMillis() / 1000) < 0 && this.isBuffAuto) {
                        timeSinceUseThisSkill = Canvas.gameScr.mainChar.coolDownSkill[qs.getSkillType()] + 1;
                        this.isBuffAuto = false;
                    }
                    if (timeSinceUseThisSkill > Canvas.gameScr.mainChar.coolDownSkill[qs.getSkillType()]) {
                        GameService.gI().useBuff(Canvas.gameScr.mainChar.ID, (byte) 0, b, (short) 0);
                        this.mainChar.coolDownSkill[qs.getSkillType()] = SkillManager.getSkillCooldown(qs.getSkillType(), this.mainChar.clazz, Char.skillLevelLearnt[qs.getSkillType()]);
                        this.mainChar.timeLastUseSkills[qs.getSkillType()] = System.currentTimeMillis();
                        return;
                    }
                    return;
                }
                Canvas.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
                return;
            }
            if (this.focusedActor != null && this.focusedActor.catagory == 2 && Char.skillLevelLearnt[qs.getSkillType()] >= 0) {
                doUseSkillToFocusActor(qs.getSkillType());
            }
            if (this.focusedActor == null) {
                return;
            }
            long timeSinceUseThisSkill2 = System.currentTimeMillis() - this.mainChar.timeLastUseSkills[qs.getSkillType()];
            if (this.focusedActor.catagory != 2 && timeSinceUseThisSkill2 > Canvas.gameScr.mainChar.coolDownSkill[qs.getSkillType()]) {
                if (Char.skillLevelLearnt[qs.getSkillType()] > 0 || this.focusedActor.catagory == 10) {
                    doUseSkillToFocusActor(qs.getSkillType());
                    return;
                }
                return;
            }
            return;
        }
        if (qs.quickslotType == 2) {
            doUsePotion(qs.getPotionType());
        }
    }

    public void doUsePotion(int potionType) {
        if (Canvas.gameScr.mainChar.potions[potionType] <= 0) {
            int leght = MainChar.quickSlot[indexTabSlot].length;
            int[] arrHp = {94, 93, 22, 21, 3, 2, 1};
            int[] arrMn = {96, 95, 24, 23, 6, 5, 4};
            for (int i = 0; i < leght; i++) {
                if (MainChar.quickSlot[indexTabSlot][i].quickslotType == 2 && MainChar.quickSlot[indexTabSlot][i].getPotionType() == potionType) {
                    MainChar.quickSlot[indexTabSlot][i].setIsNothing();
                    if (potionType == 1 || potionType == 2 || potionType == 3 || potionType == 21 || potionType == 22 || potionType == 93 || potionType == 94) {
                        boolean exist = false;
                        for (int newPType = 1; newPType <= 3; newPType++) {
                            if (newPType != potionType && Canvas.gameScr.mainChar.potions[newPType] > 0) {
                                MainChar.quickSlot[indexTabSlot][i].setIsPotion(newPType);
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            for (int u = 0; u < arrHp.length; u++) {
                                if (Canvas.gameScr.mainChar.potions[arrHp[u]] > 0) {
                                    MainChar.quickSlot[indexTabSlot][i].setIsPotion(arrHp[u]);
                                    break;
                                }
                            }
                        }
                    }
                    if (potionType == 4 || potionType == 5 || potionType == 6 || potionType == 23 || potionType == 24 || potionType == 95 || potionType == 96) {
                        boolean existMp = false; // bản gốc không bao giờ gán true
                        for (int newPType2 = 4; newPType2 <= 6; newPType2++) {
                            if (newPType2 != potionType && Canvas.gameScr.mainChar.potions[newPType2] > 0) {
                                MainChar.quickSlot[indexTabSlot][i].setIsPotion(newPType2);
                                break;
                            }
                        }
                        if (!existMp) {
                            for (int u2 = 0; u2 < arrMn.length; u2++) {
                                if (Canvas.gameScr.mainChar.potions[arrMn[u2]] > 0) {
                                    MainChar.quickSlot[indexTabSlot][i].setIsPotion(arrMn[u2]);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            RMS.saveQuickSlot();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.mainChar.potionLastTimeUse[potionType] < MainChar.listPotion[potionType].delay) {
            return;
        }
        if (this.mainChar.hp >= this.mainChar.maxhp && (potionType == 1 || potionType == 2 || potionType == 3 || potionType == 21 || potionType == 22 || potionType == 93 || potionType == 94)) {
            game.pokemon.DungDo.loi("HP đã đầy.");   // pokemon: trước im lặng
            return;
        }
        if (this.mainChar.mp >= this.mainChar.maxmp && (potionType == 4 || potionType == 5 || potionType == 6 || potionType == 23 || potionType == 24 || potionType == 95 || potionType == 96)) {
            game.pokemon.DungDo.loi("PP đã đầy.");   // pokemon: trước im lặng
            return;
        }
        if (potionType == 19) {
            // Lang co 6 ban (0/301, 70/1701, 80/1901 - server MapDoMenu mapstart): truoc chi chan map 0
            // nen o lang khac van mat binh ve thanh ma chi bay ve dung lang dang dung.
            if (this.map != 0 && this.map != 301 && this.map != 70 && this.map != 1701 && this.map != 80 && this.map != 1901) {
                Canvas.gameScr.mainChar.timeComeHome = System.currentTimeMillis();
                Canvas.gameScr.mainChar.comeHome = true;
                Canvas.gameScr.mainChar.state = (byte) 4;
            } else {
                Canvas.startOKDlg("Không thể sử dụng trong làng.");
                return;
            }
        }
        if (potionType < 10 || potionType > 20) {
            int[] iArr = this.mainChar.potions;
            iArr[potionType] = iArr[potionType] - 1;
            this.mainChar.potionLastTimeUse[potionType] = now;
        }
        if (this.mainChar.state != 3 && autoFight && !this.mainChar.isDie && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80 && !Tilemap.isOfflineMap) {
            this.mainChar.hp += getMpHpPutKey(0, potionType);
            this.mainChar.mp += getMpHpPutKey(1, potionType);
            if (this.mainChar.hp >= this.mainChar.maxhp) {
                this.mainChar.hp = this.mainChar.maxhp;
            }
            if (this.mainChar.mp >= this.mainChar.maxmp) {
                this.mainChar.mp = this.mainChar.maxmp;
            }
        }
        GameService.gI().usePotion(potionType);
    }

    public int getMpHpPutKey(int type, int idPotion) {
        for (int i = 0; i < TYPE_MP_HP[type].length; i++) {
            if (TYPE_MP_HP[type][i] == idPotion) {
                return VALUE_MP_HP[type][i];
            }
        }
        return 0;
    }

    public void doParty() {
        if (this.focusedActor != null) {
            if (this.focusedActor.catagory == 0) {
                Vector menuItems = new Vector();
                menuItems.addElement(new Command("Mời party", new IAction() {
                    @Override
                    public void perform() {
                        if (((Char) GameScr.this.focusedActor).pk > 0 || ((Char) GameScr.this.focusedActor).iskiller) {
                            Canvas.startOKDlg("Không thể mời người trong trạng thái đánh nhau hoặc phạm tội");
                        } else if (GameScr.this.mainChar.IDParty == -1) {
                            GameScr.this.gameService.requestCreateParty(GameScr.this.mainChar.ID);
                            GameScr.tempIDActorParty = GameScr.this.focusedActor.ID;
                        } else {
                            GameScr.this.gameService.invite2Party(GameScr.this.focusedActor.ID, 0);
                        }
                    }
                }));
                menuItems.addElement(new Command("Trao đổi", new IAction() {
                    @Override
                    public void perform() {
                        if (!GameScr.this.mainChar.isTrade) {
                            GameScr.this.gameService.trade(GameScr.this.focusedActor.ID);
                        }
                    }
                }));
                menuItems.addElement(new Command("Mời đám cưới", new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.gameService.invite2Party(GameScr.this.focusedActor.ID, 3);
                    }
                }));
                if (this.focusedActor instanceof Char) {
                    menuItems.addElement(new Command("Nhắn tin", new IAction() {
                        @Override
                        public void perform() {
                            MessageScr.gI().addTab("", ((Char) GameScr.this.focusedActor).name, 0);
                            MessageScr.gI().setFocusTab(((Char) GameScr.this.focusedActor).name);
                            MessageScr.gI().switchToMe();
                        }
                    }));
                }
                menuItems.addElement(new Command("Kết bạn", new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.gameService.sendInfoAddFriend(((Char) GameScr.this.focusedActor).ID, 0);
                    }
                }));
                if ((Canvas.gameScr.mainChar.isMaster == 0 || Canvas.gameScr.mainChar.isMaster == 1 || Canvas.gameScr.mainChar.isMaster == 2) && ((Char) this.focusedActor).idClan == -1) {
                    menuItems.addElement(new Command("Mời vào đội", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.sendRequestAddClan(GameScr.this.focusedActor.ID, (byte) 0, false);
                        }
                    }));
                }
                if (((Char) this.focusedActor).idClan != -1) {
                    menuItems.addElement(new Command("Xem thông tin đội", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.requestClanInfo(((Char) GameScr.this.focusedActor).idClan);
                        }
                    }));
                }
                menuItems.addElement(new Command("Xem thông tin", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg();
                        GameScr.this.saveIDViewInfoAnimal = GameScr.this.focusedActor.ID;
                        GameScr.this.gameService.requestSomeOneInfo(GameScr.this.focusedActor.ID, (byte) 0);
                    }
                }));
                menuItems.addElement(new Command("Xuống thú cưỡi", new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.unRideHorse();
                    }
                }));
                Canvas.menu.startAt(menuItems, 2);
                return;
            }
            Canvas.keyPressed[5] = false;
            return;
        }
        unRideHorse();
    }

    public void unRideHorse() {
        if (this.mainChar.useHorse != -1 || this.mainChar.idhorse > -1) {
            Canvas.startYesNoDlg("Thú cưỡi sẽ bị mất nếu rương đồ không còn chỗ. Bạn có muốn xuống thú cưỡi không ?", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.unRideHorse((byte) 0);
                    Canvas.currentDialog = null;
                }
            });
        }
    }

    protected Vector getNearMonster(Actor mt, int distance) {
        Vector mst = new Vector();
        mst.addElement(mt);
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac.catagory == 1 && ac.state != 8 && ac.ID != mt.ID && abs(ac.x - mt.x) <= distance && abs(ac.y - mt.y) <= distance) {
                mst.addElement(ac);
                if (mst.size() > 10) {
                    break;
                }
            }
        }
        return mst;
    }

    private boolean isMapPK() {
        if (Tilemap.lv == 112 || Tilemap.lv == 2541 || Tilemap.lv == 2542 || Tilemap.lv == 2543 || Tilemap.lv == 2544) {
            return true;
        }
        return false;
    }

    private boolean isChiemThanh() {
        if (((this.mainChar.x > xArena * 16 && this.mainChar.y > yArena * 16 && this.mainChar.x < wArena * 16 && this.mainChar.y < hArena * 16) || isFight) && this.focusedActor.catagory != 2 && Tilemap.lv == 201) {
            return true;
        }
        return false;
    }

    private boolean isFocusKiller() {
        if (((Char) this.focusedActor).iskiller) {
            return true;
        }
        return false;
    }

    public static void addviewInfo(Actor ac, String str, int time) {
        ac.chat = new ChatPopup(time, str, 0);
        ac.chat.setPos(ac.x, ac.y);
    }

    public void doViewInfoGround(String text, Actor obj) {
        obj.chat = null;
        addviewInfo(obj, text, 50);
    }

    public void addMsgWorld(String info) {
        MsgInfo ms = new MsgInfo(info);
        msgWorld.addElement(ms);
        savemsgWorld.addElement(ms);
    }

    public void sendActorMove(int x, int y) {
        if (System.currentTimeMillis() - timeMove >= 0) {
            timeMove = System.currentTimeMillis() + ((long) dtmove);
            this.gameService.moveChar(x, y);
            this.mainChar.stop = true;
        }
    }

    public void doUseSkillToFocusActor(byte skillType) {
        this.mainChar.getSkillIDFromType(skillType);
        try {
            if (this.focusedActor != null) {
                if (autoFight && (this.focusedActor.isNPC() || this.focusedActor.isNpcChar())) {
                    return;
                }
                if (iTaskAuto != 2 && timeRemovePos <= 0 && !(this.mainChar.posTransRoad != null && laDuongDuoi(this.focusedActor))) {
                    // giữ đường đang ĐUỔI chính mục tiêu này (xoá mỗi lần bấm đánh = khựng giữa đường)
                    this.mainChar.posTransRoad = null;
                    Target target = this.tg;
                    this.tg.y = (short) -1;
                    target.x = (short) -1;
                }
                if (this.focusedActor.isMonster() || ((this.mainChar.nationID != this.focusedActor.nationID && this.focusedActor.isPlayer()) || ((this.focusedActor.isPlayer() && ((idMapPK == Tilemap.lv && this.mainChar.idClan != ((Char) this.focusedActor).idClan) || (this.focusedActor.pk > 0 && this.mainChar.pk > 0 && this.focusedActor.pk != this.mainChar.pk))) || ((this.mainChar.iskiller && this.focusedActor.isPlayer()) || ((this.focusedActor.isPlayer() && (isChiemThanh() || isMapPK() || isFocusKiller())) || ((this.focusedActor.isPlayer() && ((Char) this.focusedActor).isAnimal) || (this.mainChar.isAnimal && (this.focusedActor.isMonster() || this.focusedActor.isPlayer())))))))) {
                    int aa = 0;
                    if (this.focusedActor.isMonster() && this.focusedActor.isMineral()) {
                        aa = 1;
                        this.mainChar.getSkillIDFromType((byte) 0);
                        skillType = 0;
                    }
                    if (!this.focusedActor.isPlayer() || !this.focusedActor.isNPC()) {
                        int mplost = SkillManager.getSkillMP(skillType, Char.skillLevelLearnt[skillType], this.mainChar.clazz);
                        if (mplost > this.mainChar.mp) {
                            if (!this.first) {
                                addChat(new ChatInfo("", "Không đủ MP", 0));
                                this.first = true;
                                return;
                            }
                            return;
                        }
                        if (autoFight) {
                            this.mainChar.idMonster = this.focusedActor.ID;
                        }
                        this.first = false;
                        LiveActor attackedLiveActor = (LiveActor) this.focusedActor;
                        if ((attackedLiveActor.realHP <= 0 || attackedLiveActor.hp <= 0) && attackedLiveActor.catagory == 0) {
                            return;
                        }
                        int maxRange = SkillManager.getSkillRange(skillType, this.mainChar.clazz);
                        if (aa == 1) {
                            maxRange = 30;
                        }
                        if (Util.distance(this.mainChar.x, this.mainChar.y, attackedLiveActor.x, attackedLiveActor.y) > maxRange) {
                            this.coutChangeFocusWhenDoing++;
                            moveToRange(attackedLiveActor, maxRange);
                            this.isAutoRangeWhenAuto = true;
                            // bấm đánh tay: tự đuổi tiếp mỗi khung tới khi vào tầm rồi đánh (capNhatDuoi)
                            if (!autoFight && !TuDong.dang && this.duoiMucTieu != attackedLiveActor) {
                                this.duoiMucTieu = attackedLiveActor;
                                this.duoiBatDau = System.currentTimeMillis();
                            }
                            if (this.duoiMucTieu == attackedLiveActor) {
                                this.duoiChieu = skillType;
                            }
                        } else {
                            this.isAutoRangeWhenAuto = false;
                            this.coutChangeFocusWhenDoing = 0;
                            // vào tầm: dừng hẳn (bỏ đường đuổi) rồi đánh
                            if (this.mainChar.posTransRoad != null && laDuongDuoi(attackedLiveActor)) {
                                this.mainChar.posTransRoad = null;
                                this.mainChar.countRoad = 0;
                            }
                            if (this.mainChar.state == 1) {
                                this.mainChar.moveTo(this.mainChar.x, this.mainChar.y);
                                this.mainChar.justStopFromRun = true; // gửi vị trí dừng ở khung sau
                            }
                            if (this.mainChar.iskiller && this.focusedActor.catagory == 0 && ((Char) this.focusedActor).level < 10 && this.mainChar.nationID == this.focusedActor.nationID) {
                                return;
                            }
                            if (this.focusedActor.catagory == 1 && ((Monster) this.focusedActor).state == 5) {
                                return;
                            }
                            long now = System.currentTimeMillis();
                            if (now - this.mainChar.timeLastUseSkills[skillType] > this.mainChar.coolDownSkill[skillType]) {
                                this.mainChar.dir = Util.findDirection(this.mainChar, attackedLiveActor);
                                AttackResult atr = new AttackResult();
                                atr.dam = 2000000;
                                this.mainChar.mp -= mplost;
                                this.mainChar.startAttack(attackedLiveActor, atr, skillType, Char.skillLevelLearnt[skillType]);
                                if (this.duoiMucTieu == attackedLiveActor) {
                                    this.duoiMucTieu = null; // đuổi xong: đã đánh
                                }
                                boolean isAeo = SkillManager.isSkillAeo(this.mainChar.clazz, skillType);
                                Vector mst = new Vector();
                                if (isAeo) {
                                    if (attackedLiveActor.catagory == 1) {
                                        SkillAnimate skillAnimate = this.mainChar.currentSkillAnimate;
                                        Vector nearMonster = getNearMonster(attackedLiveActor, SkillManager.getRangeSkillAeo(this.mainChar.clazz, skillType));
                                        mst = nearMonster;
                                        skillAnimate.setMonster(nearMonster);
                                    } else if (attackedLiveActor.catagory == 0) {
                                        mst.addElement(attackedLiveActor);
                                        this.mainChar.currentSkillAnimate.setMonster(mst);
                                    }
                                }
                                this.changeSinceLastUpdate = false;
                                switch (attackedLiveActor.catagory) {
                                    case 0:
                                        this.gameService.attackOtherPlayer(attackedLiveActor.ID, skillType, atr.effect, atr.dam, 0, atr.buffEffect);
                                        this.mainChar.downDurableWeapone();
                                        break;
                                    case 1:
                                        if (!isAeo) {
                                            this.gameService.attackMonster(attackedLiveActor.ID, skillType, atr.effect, atr.dam, 0, atr.buffEffect);
                                        } else {
                                            this.gameService.attackMultiMonster(attackedLiveActor.ID, skillType, atr.effect, atr.dam, 0, atr.buffEffect, mst);
                                        }
                                        this.mainChar.downDurableWeapone();
                                        break;
                                }
                                return;
                            }
                        }
                    }
                }
                if (this.focusedActor != null && this.focusedActor.isItemCanGet()) {
                    Dropable dropItem = (Dropable) this.focusedActor;
                    if (dropItem.isSendGet) {
                        return;
                    }
                    if (Util.distance(this.mainChar.x, this.mainChar.y, dropItem.x, dropItem.y) > 35) {
                        moveToRange(dropItem, 35);
                    } else {
                        boolean canTake = true;
                        String reason = "";
                        if (dropItem.catagory == 4) {
                            if (dropItem.item_template_id == 0 && this.mainChar.charXu > 100000000) {
                                canTake = false;
                                reason = "Quá nhiều tiền";
                            }
                        } else if (dropItem.catagory == 3 && Canvas.gameScr.mainChar.isFullInventory()) {
                            reason = "Hành trang đã đầy";
                            canTake = false;
                        }
                        if (!canTake) {
                            addChat(new ChatInfo("", reason, 0));
                            changeToNextFocusActor(false);
                            return;
                        }
                        this.mainChar.dir = Util.findDirection(this.mainChar, dropItem);
                        sendActorMove(this.mainChar.x, this.mainChar.y);
                        this.changeSinceLastUpdate = false;
                        this.gameService.getDropableFromGround(dropItem.catagory, dropItem.ID);
                        return;
                    }
                }
                if (this.focusedActor.isNPC()) {
                    if (this.focusedActor.getNpcType() == 1) {
                        GameService.gI().requestNPCInfo(this.focusedActor.getTypeNpc());
                        return;
                    }
                    if (!Tilemap.isOfflineMap) {
                        Vector allquest = getAllQuestNpc(this.focusedActor.getTypeNpc());
                        if (allquest.size() > 0 && !this.focusedActor.isNpcChar()) {
                            if (posNpcReceiveClan != null && ((NPC) this.focusedActor).type == posNpcReceiveClan.index) {
                                allquest.addElement(new Command("Nhiệm vụ Đội", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameScr.this.gameService.questClan(3);
                                        GameScr.posNpcReceiveClan = null;
                                        Canvas.startWaitDlg();
                                    }
                                }));
                            }
                            allquest.addElement(new Command("Nói chuyện", new IAction() {
                                @Override
                                public void perform() {
                                    GameScr.this.mainChar.chat = null;
                                    GameScr.this.focusedActor.chat = null;
                                    if (!GameScr.this.focusedActor.isNpcChar() && GameScr.this.focusedActor.getTypeNpc() != 7 && GameScr.this.focusedActor.getTypeNpc() != 10 && GameScr.this.focusedActor.getTypeNpc() != 22 && GameScr.this.focusedActor.getTypeNpc() != 31 && GameScr.this.focusedActor.getTypeNpc() != 21 && GameScr.this.focusedActor.getTypeNpc() != 25) {
                                        GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(GameScr.this.focusedActor.getTypeNpc()), 500);
                                    } else {
                                        if (GameScr.this.focusedActor.isNpcChar()) {
                                            return;
                                        }
                                        GameScr.this.showNpcMenu(GameScr.this.focusedActor.getTypeNpc(), new Vector());
                                    }
                                }
                            }));
                            Canvas.menu.startAt(allquest, 3);
                            return;
                        }
                        this.focusedActor.chat = null;
                        if (!this.focusedActor.isNpcChar() && this.focusedActor.getTypeNpc() != 7 && this.focusedActor.getTypeNpc() != 10 && this.focusedActor.getTypeNpc() != 22 && this.focusedActor.getTypeNpc() != 31 && this.focusedActor.getTypeNpc() != 21 && this.focusedActor.getTypeNpc() != 25) {
                            addChat(this.focusedActor, Res.getDefaultChat(this.focusedActor.getTypeNpc()), 500);
                        } else if (!this.focusedActor.isNpcChar()) {
                            showNpcMenu(this.focusedActor.getTypeNpc(), new Vector());
                        }
                    } else if (this.focusedActor.getTypeNpc() == 2 || ((this.focusedActor.getTypeNpc() > 10 && this.focusedActor.getTypeNpc() != 29 && this.focusedActor.getTypeNpc() != 24 && this.focusedActor.getTypeNpc() != 26 && this.focusedActor.getTypeNpc() != 30) || this.focusedActor.getTypeNpc() == 3 || this.focusedActor.getTypeNpc() == 27 || this.focusedActor.getTypeNpc() == 28)) {
                        showNpcMenu(this.focusedActor.getTypeNpc(), new Vector());
                    } else {
                        GameService.gI().requestNPCInfo(this.focusedActor.getTypeNpc());
                    }
                }
                if (this.focusedActor.catagory == 10) {
                    Vector menu = new Vector();
                    if (this.focusedActor != null) {
                        Ground gr = (Ground) this.focusedActor;
                        if (gr.isBuyOk) {
                            menu.addElement(new Command("Xem thông tin", new IAction() {
                                @Override
                                public void perform() {
                                    GameScr.this.doViewInfoGround(GameScr.this.focusedActor.infoGround, GameScr.this.focusedActor);
                                }
                            }));
                            menu.addElement(new Command("Nâng cấp đất", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        Ground gr2 = (Ground) GameScr.this.focusedActor;
                                        GameScr.this.gameService.doPlant((byte) 4, (byte) gr2.ID);
                                    }
                                }
                            }));
                            menu.addElement(new Command("Trồng cây", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        Ground gr2 = (Ground) GameScr.this.focusedActor;
                                        GameScr.this.gameService.doPlant((byte) 0, (byte) gr2.ID);
                                    }
                                }
                            }));
                            menu.addElement(new Command("Thu hoạch", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        Ground gr2 = (Ground) GameScr.this.focusedActor;
                                        GameScr.this.gameService.doPlant((byte) 1, (byte) gr2.ID);
                                    }
                                }
                            }));
                            menu.addElement(new Command("Nhổ bỏ", new AnonymousClass32()));
                        } else {
                            menu.addElement(new Command("Mua đất", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        Ground gr2 = (Ground) GameScr.this.focusedActor;
                                        GameScr.this.gameService.doPlant((byte) 2, (byte) gr2.ID);
                                    }
                                }
                            }));
                        }
                    }
                    Canvas.menu.startAt(menu, 3);
                }
            }
        } catch (Exception e) {
        }
    }

    class AnonymousClass32 implements IAction {
        AnonymousClass32() {
        }

        @Override
        public void perform() {
            if (GameScr.this.focusedActor != null) {
                final Ground gr = (Ground) GameScr.this.focusedActor;
                if (gr.myTree != null) {
                    Canvas.startYesNoDlg("Bạn có muốn nhổ bỏ cây này không? ", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.doPlant((byte) 3, (byte) gr.ID);
                            Canvas.endDlg();
                        }
                    });
                }
            }
        }
    }

    private void moveToRange(Actor attackedLiveActor, int maxRange) {
        // đang đi theo đường ĐUỔI chính mục tiêu này thì vẫn vào (đuổi tiếp / tìm lại đường), đường khác thì như cũ
        boolean dangDuoi = this.mainChar.posTransRoad != null && laDuongDuoi(attackedLiveActor);
        if (!(autoFight && typeOptionFocus == 1 && this.mainChar.posTransRoad != null && !dangDuoi) && (this.mainChar.posTransRoad == null || dangDuoi)) {
            if (isFight && this.map == 201) {
                return;
            }
            int angle = Util.angle(attackedLiveActor.x - this.mainChar.x, -(attackedLiveActor.y - this.mainChar.y));
            int dis = Util.distance(this.mainChar.x, this.mainChar.y, attackedLiveActor.x, attackedLiveActor.y);
            int xa = ((dis - maxRange) * Util.cos(angle)) >> 10;
            int ya = (-((dis - maxRange) * Util.sin(angle))) >> 10;
            boolean isMoveToSkiller = false;
            if (xa > 0) {
                if (xa > 50) {
                    xa = 50;
                }
            } else if (xa < 0 && xa < -50) {
                xa = -50;
            }
            if (ya > 0) {
                if (ya > 50) {
                    ya = 50;
                }
            } else if (ya < 0 && ya < -50) {
                ya = -50;
            }
            if (autoFight && typeOptionFocus == 1 && this.focusedActor != null && this.focusedActor.iskiller) {
                isMoveToSkiller = true;
            }
            if (this.map == 17) {
                if (autoFight && this.mainChar.isDoing && this.coutChangeFocusWhenDoing > 10) {
                    changeToNextFocusActor(false);
                    this.coutChangeFocusWhenDoing = 0;
                }
            } else if (autoFight && !isMoveToSkiller) {
                ya = 0;
                xa = 0;
                changeToNextFocusActor(false);
            }
            if (isMoveToSkiller && timeRemovePos <= 0 && (Math.abs(this.mainChar.x - this.mainChar.lastXAuto) > 120 || Math.abs(this.mainChar.y - this.mainChar.lastYAuto) > 120)) {
                ya = 0;
                xa = 0;
                changeToNextFocusActor(true);
                this.mainChar.timeGoToLastXY = System.currentTimeMillis() - 1;
                findRoad2(this.mainChar.lastXAuto, this.mainChar.lastYAuto);
            }
            if ((xa == 0 && ya == 0) || (autoFight && !isMoveToSkiller)) {
                this.mainChar.moveTo((short) (this.mainChar.x + xa), (short) (this.mainChar.y + ya));
                return;
            }
            duoiToi(attackedLiveActor, maxRange);
        }
    }

    // ---------------------------------------------------------------- đuổi mục tiêu (bao 0.9.2 đánh đi giật)
    // Cũ: moveToRange bước THẲNG tối đa 50 px mỗi trục (xuyên tường), gửi luôn ĐIỂM ĐÍCH cho máy chủ, và mỗi lần
    // bấm đánh lại xoá đường đang đi (doUseSkillToFocusActor) -> gặp tường thì máy chủ trả vị trí cũ (kéo lùi),
    // quái chạy thì đứng khựng chờ lần bấm sau. Mới: đoạn thẳng thông thoáng thì đi thẳng tới điểm đứng trong tầm,
    // không thì tìm đường (DuongDi) tới quái, tìm lại khi quái dời > 24 px (tối đa ~3 lần / giây); vào tầm thì
    // dừng và đánh; quái chết / biến mất / đổi mục tiêu / bấm phím đi / chạm đất thì thôi đuổi.

    /** Mục tiêu đang đuổi theo lệnh bấm đánh tay (tới tầm thì đánh một đòn, như bấm lại). */
    Actor duoiMucTieu;
    byte duoiChieu;
    private long duoiBatDau;
    private long duoiKe;
    /** Đường hiện tại (posTransRoad) là đường đuổi actor này: loại / id / điểm quái lúc tìm / lúc tìm. */
    private int duoiCat = -1;
    private int duoiId = -1;
    private int duoiX;
    private int duoiY;
    private long duoiLuc;
    /** Đo: số lần tìm đường khi đuổi. */
    public static int soTimDuongDuoi;

    boolean laDuongDuoi(Actor a) {
        return a != null && this.duoiCat == a.catagory && this.duoiId == a.ID;
    }

    void huyDuoi() {
        this.duoiMucTieu = null;
    }

    /** Đi tới chỗ đứng trong tầm {@code tam} của {@code a} (đi thẳng nếu thông, không thì theo đường). */
    void duoiToi(Actor a, int tam) {
        MainChar mc = this.mainChar;
        int d = Util.distance(mc.x, mc.y, a.x, a.y);
        int giu = tam - 8;
        if (giu < 8) {
            giu = tam > 1 ? tam - 1 : 1;
        }
        int gx = a.x;
        int gy = a.y;
        if (d > 0) {
            gx = a.x + ((mc.x - a.x) * giu) / d;
            gy = a.y + ((mc.y - a.y) * giu) / d;
        }
        if (!checkMoveLimit(gx, gy) && DuongDi.thongThoang(Tilemap.type, Tilemap.w, Tilemap.h, mc.x, mc.y, gx, gy)) {
            if (mc.posTransRoad != null && laDuongDuoi(a)) {
                mc.posTransRoad = null;
                mc.countRoad = 0;
            }
            mc.moveTo((short) gx, (short) gy);
            mc.stop = false;
            return;
        }
        long bay = System.currentTimeMillis();
        boolean tim = mc.posTransRoad == null || !laDuongDuoi(a)
                || (bay - this.duoiLuc > 350L && (Math.abs(a.x - this.duoiX) > 24 || Math.abs(a.y - this.duoiY) > 24));
        if (!tim) {
            return;
        }
        if (bay - this.duoiLuc < 300L && laDuongDuoi(a)) {
            return; // không tìm lại dồn dập (đường cũ vẫn đi tiếp)
        }
        this.duoiLuc = bay;
        this.duoiX = a.x;
        this.duoiY = a.y;
        this.duoiCat = a.catagory;
        this.duoiId = a.ID;
        soTimDuongDuoi++;
        short[] duong = updateFindRoad(mc.x / 16, mc.y / 16, a.x / 16, a.y / 16);
        if (duong == null) {
            return;
        }
        mc.xTo = mc.x;
        mc.yTo = mc.y;
        mc.xBegin = mc.x;
        mc.yBegin = mc.y;
        mc.posTransRoad = duong;
        mc.countRoad = 0;
    }

    /** Đi theo đường tới điểm pixel (x, y) (tự động đánh khi đoạn thẳng bị chặn); tìm lại khi đích dời > 24 px. */
    public void diTheoDuong(int x, int y) {
        MainChar mc = this.mainChar;
        long bay = System.currentTimeMillis();
        if (mc.posTransRoad != null && this.duoiCat == -2 && Math.abs(x - this.duoiX) <= 24 && Math.abs(y - this.duoiY) <= 24) {
            return;
        }
        if (bay - this.duoiLuc < 300L) {
            return;
        }
        this.duoiLuc = bay;
        this.duoiX = x;
        this.duoiY = y;
        this.duoiCat = -2;
        this.duoiId = -2;
        short[] duong = updateFindRoad(mc.x / 16, mc.y / 16, x / 16, y / 16);
        if (duong == null) {
            return;
        }
        mc.xTo = mc.x;
        mc.yTo = mc.y;
        mc.xBegin = mc.x;
        mc.yBegin = mc.y;
        mc.posTransRoad = duong;
        mc.countRoad = 0;
    }

    /** Bỏ đường do diTheoDuong đặt (đường khác giữ nguyên). */
    public void huyDiTheoDuong() {
        if (this.duoiCat == -2 && this.mainChar.posTransRoad != null) {
            this.mainChar.posTransRoad = null;
            this.mainChar.countRoad = 0;
        }
        if (this.duoiCat == -2) {
            this.duoiCat = -1;
        }
    }

    /** Mỗi khung (MainChar.update): tiếp tục đuổi mục tiêu bấm đánh tay cho tới khi vào tầm và đánh được. */
    public void capNhatDuoi() {
        Actor a = this.duoiMucTieu;
        if (a == null) {
            return;
        }
        long bay = System.currentTimeMillis();
        if (TuDong.dang || autoFight || this.focusedActor != a || a.wantDestroy || this.mainChar.state == 3 || this.mainChar.isDie
                || bay - this.duoiBatDau > 10000L || !this.actors.contains(a)) {
            this.duoiMucTieu = null;
            return;
        }
        if (a instanceof LiveActor) {
            LiveActor la = (LiveActor) a;
            // quái: state 5 bay chết, 8 đang xoá (3 = đang đánh trả, vẫn sống); người: 3 = gục
            boolean chet = a.catagory == 1 ? (la.hp <= 0 || la.state == 5 || la.state == 8)
                    : (la.hp <= 0 || la.realHP <= 0 || la.state == 3);
            if (chet) {
                this.duoiMucTieu = null;
                return;
            }
        }
        if (this.mainChar.isDoing || this.mainChar.state == 2 || bay < this.duoiKe) {
            return;
        }
        this.duoiKe = bay + 120L;
        doUseSkillToFocusActor(this.duoiChieu);
    }

    public void showNpcMenu(final int type, Vector menu) {
        if (type == 3 || type == 27) {
            for (int i = 0; i < Res.nameClazz.length; i++) {
                final int ii = i;
                menu.addElement(new Command(Res.nameClazz[i], new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestNPCInfo((short) type, ii);
                        WindowInfoScr.gI().idClass = ii;
                    }
                }));
            }
            Canvas.menu.startAt(menu, 3);
        } else if (type == 2 || type == 28) {
            menu.addElement(new Command("Mua bán", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().requestNPCInfo((short) type);
                }
            }));
            menu.addElement(new Command("Nghiền bột", new IAction() {
                @Override
                public void perform() {
                    ScreenItemTim sc = new ScreenItemTim();
                    sc.setSizeCell((byte) 5, false);
                    sc.switchToMe(GameScr.this);
                }
            }));
            menu.addElement(new Command("Thêm dòng", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 4, false);
                    ScreenDapDo.gI().tile = "Thêm dòng";
                }
            }));
            menu.addElement(new Command("Luyện đồ", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.haveItemImbue()) {
                        GameScr.this.gameService.requestImbue(0);
                    } else {
                        Canvas.startOKDlg("Bạn chưa có nguyên tố để luyện, hãy đến nhà Persian mua rồi quay lại đây.");
                    }
                }
            }));
            menu.addElement(new Command("Luyện đồ tự động", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 1, false);
                    ScreenDapDo.gI().tile = "Luyện đồ tự động";
                }
            }));
            menu.addElement(new Command("Cộng thuộc tính", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 0, false);
                    ScreenDapDo.gI().tile = "Cộng thuộc tính";
                }
            }));
            menu.addElement(new Command("Khóa đồ thú", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 2, false);
                    ScreenDapDo.gI().tile = "Khóa đồ thú";
                }
            }));
            menu.addElement(new Command("Khóa trang bị", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 3, false);
                    ScreenDapDo.gI().tile = "Khóa trang bị";
                }
            }));
            menu.addElement(new Command("Sửa đồ", new AnonymousClass44()));
            menu.addElement(new Command("Đục lỗ", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestKham(0);
                    WindowInfoScr.gI().name = "Đục lỗ";
                    WindowInfoScr.isKham = false;
                }
            }));
            menu.addElement(new Command("Khảm", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestKham(0);
                    WindowInfoScr.isKham = true;
                    WindowInfoScr.gI().name = "Khảm";
                }
            }));
            menu.addElement(new Command("Hợp thành", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().epNgoc(0, null);
                    Canvas.startWaitDlg();
                }
            }));
        } else if (type == 7) {
            menu.addElement(new Command("Đi đến", new AnonymousClass57()));
            menu.addElement(new Command("Giao tiếp", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(type), 500);
                }
            }));
        } else if (type == 10) {
            menu.addElement(new Command("Lên tàu", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.potions[20] <= 0) {
                        Canvas.startOKDlg("Chưa có vé");
                    } else {
                        GameScr.this.gameService.moveUpBoard();
                        Canvas.startWaitDlg();
                    }
                }
            }));
            menu.addElement(new Command(new StringBuffer("Mua vé (").append(game.pokemon.TienTe.chu(game.pokemon.TienTe.XU, pTicket)).append(")").toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.buyTicket();
                    Canvas.startOKDlg("xin chờ", true);
                }
            }));
            menu.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(type), 500);
                }
            }));
        } else if (type == 22 || type == 31) {
            menu.addElement(new Command("Nạp xu", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.doNapXu();
                }
            }));
            menu.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(type), 500);
                }
            }));
        } else if (type == 25) {
            menu.addElement(new Command("Giao tiếp", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(type), 500);
                }
            }));
            menu.addElement(new Command("Chuyển thẻ", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.giveCard2NPC(GameScr.this.focusedActor.x / 16, GameScr.this.focusedActor.y / 16);
                }
            }));
        } else if (type == 21) {
            menu.addElement(new Command("Học kỹ năng", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.showWindow(0, true, new byte[]{7});
                }
            }));
            menu.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(type), 500);
                }
            }));
        } else if (type > 10 && type != 30) {
            menu.addElement(new Command("Bán", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestNPCInfo(type, 0);
                    Canvas.startWaitDlg();
                }
            }));
            menu.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestNPCInfo(type, 1);
                    Canvas.startWaitDlg();
                }
            }));
        }
        Canvas.menu.startAt(menu, 3);
    }

    class AnonymousClass44 implements IAction {
        AnonymousClass44() {
        }

        @Override
        public void perform() {
            Vector item = new Vector();
            item.addElement(new Command("Đồ cầm tay", new AnonymousClass45(this)));
            item.addElement(new Command("Quần áo", new AnonymousClass48(this)));
            item.addElement(new Command("Tất cả", new AnonymousClass51(this)));
            Canvas.menu.startAt(item, 3);
        }
    }

    class AnonymousClass45 implements IAction {
        final AnonymousClass44 this$1;

        AnonymousClass45(AnonymousClass44 anonymousClass44) {
            this.this$1 = anonymousClass44;
        }

        @Override
        public void perform() {
            if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(1)) {
                Canvas.startOKDlg("Hết tiền");
            } else {
                Canvas.startYesNoDlg(new StringBuffer("Phí sửa chữa là ").append(GameScr.this.mainChar.getPriceRepair(1)).append(" ").append(Res.stMoney).toString(), new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(1);
                        GameScr.this.gameService.repairItem(0);
                        Canvas.startWaitDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    class AnonymousClass48 implements IAction {
        final AnonymousClass44 this$1;

        AnonymousClass48(AnonymousClass44 anonymousClass44) {
            this.this$1 = anonymousClass44;
        }

        @Override
        public void perform() {
            if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(0)) {
                Canvas.startOKDlg("Hết tiền");
            } else {
                Canvas.startYesNoDlg(new StringBuffer("Phí sửa chữa là ").append(GameScr.this.mainChar.getPriceRepair(0)).append(" ").append(Res.stMoney).toString(), new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(0);
                        GameScr.this.gameService.repairItem(1);
                        Canvas.startWaitDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    class AnonymousClass51 implements IAction {
        final AnonymousClass44 this$1;

        AnonymousClass51(AnonymousClass44 anonymousClass44) {
            this.this$1 = anonymousClass44;
        }

        @Override
        public void perform() {
            if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(2)) {
                Canvas.startOKDlg("Hết tiền");
            } else {
                Canvas.startYesNoDlg(new StringBuffer("Phí sửa chữa là ").append(GameScr.this.mainChar.getPriceRepair(2)).append(" ").append(Res.stMoney).toString(), new IAction() {
                    @Override
                    public void perform() {
                        GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(2);
                        GameScr.this.gameService.repairItem(2);
                        Canvas.startWaitDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        Canvas.currentDialog = null;
                    }
                });
            }
        }
    }

    class AnonymousClass57 implements IAction {
        AnonymousClass57() {
        }

        @Override
        public void perform() {
            final PidgeotBayTemplate xp = Res.getXaphu();
            if (xp != null) {
                Vector menuItem = new Vector();
                for (int i = 0; i < xp.mapID.length; i++) {
                    final int aa = i;
                    final int idm = xp.mapID[aa];
                    Command cmd = new Command(game.pokemon.TenBanDoBang.ten(xp.mapID[aa], Res.nameBigMap[xp.mapID[aa]]), new IAction() {
                        @Override
                        public void perform() {
                            if (xp.price[aa] <= GameScr.this.mainChar.charXu) {
                                GameScr.this.doUseXaPhu(idm, aa);
                            } else {
                                Canvas.startOKDlg("Không còn đủ tiền");
                            }
                        }
                    });
                    cmd.idMap = xp.mapID[aa];
                    menuItem.addElement(cmd);
                }
                Canvas.menu.startAt(menuItem, 3);
            }
        }
    }

    public void doUseXaPhu(int mapID2, final int idXphu) {
        int index = getIndexArrMap(mapID2);
        final String[] name = Res.namMap[index];
        final short[] local = Res.localXaphu[index];
        Vector menuItem = new Vector();
        for (int i = 0; i < name.length; i++) {
            final int k = i;
            menuItem.addElement(new Command(game.pokemon.TenMapPokemon.doiHien(name[i]), new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.doMove2Map((byte) idXphu, local[k * 3]);
                    ChangScr.gI().switchToMe();
                    ChangScr.gI().setMap(GameScr.this.map, 0, 0);
                    Res.removeMonsterTemplet();
                    GameScr.nameMap = game.pokemon.TenMapPokemon.doiHien(name[k]);
                }
            }));
        }
        Canvas.menu.startAt(menuItem, 3);
        Canvas.menu.setIndex();
    }

    public void doNapXu() {
        if (this.decriptNap.size() == 0) {
            Canvas.startOKDlg("Chức năng này hiện đang tạm khóa");
            return;
        }
        Vector menuItems = new Vector();
        int size0 = this.decriptNap.size();
        for (int i = 0; i < size0; i++) {
            final String syn = (String) this.syntaxNap.elementAt(i);
            final String center = (String) this.centerNap.elementAt(i);
            final String decription = (String) this.decriptNap.elementAt(i);
            if (center.length() >= 4) {
                menuItems.addElement(new Command((String) this.decriptNap.elementAt(i), new IAction() {
                    @Override
                    public void perform() {
                        GameMidlet.sendSMS(syn, new StringBuffer("sms://").append(center).toString(), new IAction() {
                            @Override
                            public void perform() {
                                Canvas.startOKDlg("Đã gửi tin. Xin chờ trong chốc lát để nhận tin nhắn phản hồi.");
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                Canvas.startOKDlg("Có lỗi khi gửi tin nhắn nạp. Xin hãy thử lại");
                            }
                        });
                    }
                }));
            } else {
                menuItems.addElement(new Command((String) this.decriptNap.elementAt(i), new IAction() {
                    @Override
                    public void perform() {
                        if (!syn.equals("-1")) {
                            final String str = syn;
                            Canvas.startYesNoDlg("Game sẽ tự động thoát. Vui lòng đăng nhập lại sau ít phút sau khi nạp.", new IAction() {
                                @Override
                                public void perform() {
                                    GameMidlet.requestLink(str);
                                }
                            }, new IAction() {
                                @Override
                                public void perform() {
                                    Canvas.currentDialog = null;
                                }
                            });
                        } else {
                            GameScr.this.doNapMoney(decription);
                        }
                    }
                }));
            }
        }
        Canvas.menu.startAt(menuItems, 3);
    }

    public void popupNapTien(final int index, final String decription, String title) {
        final TField tfSeri = new TField(10, 40, 120, Screen.deltaY);
        tfSeri.isFocus = true;
        PageScr.gI().right = tfSeri.cmdClear;
        final TField tfMaso = new TField(10, 75, 120, Screen.deltaY);
        tfMaso.setIputType(3);
        PageScr.gI().setInfo(Canvas.hw - 70, (Canvas.h - Screen.deltaY) - 120, 140, 100, title, new Command("Nạp", new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.onSendNapSMS(index, decription, tfSeri.getText().trim(), tfMaso.getText().trim());
                Canvas.startOKDlg("Đã gửi thông tin nạp");
            }
        }));
        PageScr.gI().left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                if (Canvas.isKeyPressed(2) || Canvas.isKeyPressed(8)) {
                    if (tfSeri.isFocus) {
                        tfSeri.isFocus = false;
                        tfMaso.isFocus = true;
                        PageScr.gI().right = tfMaso.cmdClear;
                    } else {
                        tfSeri.isFocus = true;
                        tfMaso.isFocus = false;
                        PageScr.gI().right = tfSeri.cmdClear;
                    }
                }
                tfSeri.update();
                tfMaso.update();
            }

            @Override
            public void keyPress(int keyCode) {
                tfSeri.keyPressed(keyCode);
                tfMaso.keyPressed(keyCode);
            }

            @Override
            public void paint(Graphics g, int x, int y) {
                Font.borderFont.drawString(g, "Seri", 10, 26, 0);
                Font.borderFont.drawString(g, "Mã thẻ", 10, 61, 0);
                tfSeri.paint(g);
                g.setClip(0, 0, 140, 100);
                tfMaso.paint(g);
            }
        };
        PageScr.gI().switchToMe();
    }

    public void doNapMoney(final String decription) {
        Vector menuItem = new Vector();
        menuItem.addElement(new Command("Nạp lượng", new IAction() {
            @Override
            public void perform() {
                GameScr.this.popupNapTien(0, decription, "NẠP LƯỢNG");
            }
        }));
        menuItem.addElement(new Command("Nạp xu", new IAction() {
            @Override
            public void perform() {
                GameScr.this.popupNapTien(1, decription, "NẠP XU");
            }
        }));
        Canvas.menu.startAt(menuItem, 3);
    }

    private void paintPoint(Graphics g, Position pos, int index) {
        if (pos != null) {
            int x1 = pos.x / 16;
            int y1 = pos.y / 16;
            if (x1 - 4 <= cmxMini) {
                x1 = cmxMini + 4;
            } else if (x1 + 6 > cmxMini + wMiniMap) {
                x1 = (cmxMini + wMiniMap) - 6;
            }
            if (y1 - 4 <= cmyMini) {
                y1 = cmyMini + 4;
            } else if (y1 + 6 > cmyMini + hMiniMap) {
                y1 = (cmyMini + hMiniMap) - 6;
            }
            g.setColor(this.colorPaint[index]);
            g.fillRect(x1, y1, 3, 3);
            g.setColor(16777215);
            g.drawRect(x1 - 1, y1 - 1, 4, 4);
        }
    }

    public void paintMiniMap(Graphics g) {
        if (Tilemap.w < Canvas.w / 16 && Tilemap.h < Canvas.h / 16) {
            return;
        }
        if (HudRong.bat()) {
            paintMiniMapRong(g);   // pokemon: màn rộng - bản đồ nhỏ phải trên dưới hàng nút
            return;
        }
        Canvas.resetTrans(g);
        if (this.indexBuff != -1 && timeBuff - (System.currentTimeMillis() / 1000) > 0) {
            game.pokemon.BangChieu.icon(g, Canvas.w - 20, ((Canvas.h - hMiniMap) - Screen.deltaY) - 20, 20, this.indexBuff);
            Font.smallFontColor[0].drawString(g, new StringBuffer(String.valueOf((timeBuff - System.currentTimeMillis()) / 1000)).toString(), Canvas.w - 10, ((Canvas.h - hMiniMap) - Screen.deltaY) - 28, 2);
        }
        g.translate(this.posMiniMap.x, this.posMiniMap.y);
        int x0 = this.mainChar.x / 16;
        int y0 = this.mainChar.y / 16;
        for (int i = 0; i < 3; i++) {
            g.setColor(colorMini[i]);
            g.drawRect(i, i, wMiniMap - (i * 2), hMiniMap - (i * 2));
        }
        g.setClip(3, 3, wMiniMap - 5, hMiniMap - 5);
        g.translate(-cmxMini, -cmyMini);
        if (Tilemap.imgMap != null) {
            g.drawImage(Tilemap.imgMap, 0, 0, 0);
        }
        g.setColor(16777215);
        g.fillRect(x0, y0 - 2, 5, 5);
        g.setColor(255);
        g.fillRect(x0 + 1, y0 - 1, 3, 3);
        paintPosNPCQuest(g);
        paintPoint(g, posNpcReceiveClan, 1);
        paintPoint(g, this.posBoss, 2);
        game.pokemon.SinhThai.veMini(g);
        g.setColor(16516117);
        for (int i2 = 0; i2 < Char.party.size(); i2++) {
            PartyInfo p1 = (PartyInfo) Char.party.elementAt(i2);
            Actor ac = findCharByID((short) p1.id);
            if (ac != null) {
                g.fillRect(ac.x / 16, ac.y / 16, 2, 2);
            }
        }
        if (this.mainChar.posTransRoad != null) {
            if (this.posCam != null) {
                g.setColor(15198737);
                g.fillRect(this.posCam.x, this.posCam.y, 3, 3);
                this.tg.x = (short) this.posCam.x;
                this.tg.y = (short) this.posCam.y;
            }
            int size1 = this.mainChar.posTransRoad.length;
            for (int i3 = 0; i3 < size1; i3++) {
                g.setColor(15198737);
                short ma = this.mainChar.posTransRoad[i3];
                if (DuongDi.co(ma)) {
                    g.fillRect(DuongDi.maX(ma) + 1, DuongDi.maY(ma) + 1, 1, 1);
                }
            }
        }
        g.setColor(16317005);
        int size2 = Tilemap.pointPop.size();
        for (int i4 = 0; i4 < size2; i4++) {
            PopupName p = (PopupName) Tilemap.pointPop.elementAt(i4);
            int addx = 0;
            if (p.x / 16 >= wMiniMap - 3) {
                addx = -3;
            }
            if (p.x / 16 <= 3) {
                addx = 3;
            }
            int addy = p.y / 16 <= 3 ? 3 : 0;
            if (p.y >= hMiniMap - 3) {
                addy = -3;
            }
            g.fillRect((p.x / 16) + addx, (p.y / 16) + addy, 3, 3);
        }
        Canvas.resetTrans(g);
        // pokemon: tên vùng trong viên xanh ngay trên bản đồ nhỏ (không bị ô phím tắt che)
        if (nameMap != null && nameMap.length() > 0) {
            int wTen = Font.borderFont.getWidth(nameMap) + 10;
            int xTen = Canvas.w - wTen - 1;
            int yTen = this.posMiniMap.y - 16;
            if (Res.imgPanel != null && this.posMiniMap.y + hMiniMap < Canvas.h - Screen.deltaY - 4) {
                // màn hẹp (bản đồ nhỏ đã lên trên hàng ô): tên vùng ở góc trái, ngay trên hàng ô phím tắt
                xTen = 2;
                yTen = (Canvas.h - 16) - caoThanhO() - 17;
            }
            game.pokemon.GiaoDien.vien(g, xTen, yTen, wTen, 15, game.pokemon.GiaoDien.XANH, nameMap);
        }
        Font.smallFont.drawString(g, new StringBuffer(String.valueOf(this.mainChar.x / 16)).append(".").append(this.mainChar.y / 16).toString(), Canvas.w - 2, (Canvas.h - Screen.deltaY) - 7, 1);
        // pokemon: nhãn VÙNG ĐỊA LÝ của map (Pewter = Kanto...), không phải lãnh thổ song song inCountry
        String vungMap = game.pokemon.VungMap.ten(Tilemap.lv);
        if (vungMap != null) {
            Font.normalFontColor.drawString(g, vungMap, Canvas.w - 2, this.posMiniMap.y - 31, 1);
        }
        int nsk = 0;
        if (this.skillClan != null) {
            for (int i5 = 0; i5 < this.skillClan.length; i5++) {
                if (this.skillClan[i5].time > 0) {
                    GameData.paintIcon(g, (short) (this.skillClan[i5].idIcon + 2600), Canvas.w - 9, (this.posMiniMap.y - 85) + (16 * nsk));
                    Font.smallFontColor[0].drawString(g, new StringBuffer(String.valueOf((int) this.skillClan[i5].lv)).toString(), (Canvas.w - 9) + 4, ((this.posMiniMap.y - 85) + (16 * nsk)) - 2, 2);
                    nsk++;
                }
            }
        }
        int nsk2 = 0;
        if (this.skillClanPrivate != null) {
            for (int i6 = 0; i6 < this.skillClanPrivate.length; i6++) {
                if (this.skillClanPrivate[i6].time > 0) {
                    GameData.paintIcon(g, (short) (this.skillClanPrivate[i6].idIcon + 2600), ((Canvas.w - 9) - 6) - 12, (this.posMiniMap.y - 85) + (16 * nsk2));
                    Font.smallFontColor[0].drawString(g, new StringBuffer(String.valueOf((int) this.skillClanPrivate[i6].lv)).toString(), (((Canvas.w - 9) - 6) - 12) + 4, ((this.posMiniMap.y - 85) + (16 * nsk2)) - 2, 2);
                    nsk2++;
                }
            }
        }
    }

    public void paintTime(Graphics g) {
        for (int i = 0; i < this.VecTime.size(); i++) {
            TimecountDown t = (TimecountDown) this.VecTime.elementAt(i);
            if (t != null) {
                t.paint(g, Canvas.w, HudRong.bat() ? HudRong.dayMiniMap(this) + 30 + (i * 18) : hMiniMap + (i * 18) + 16);
            }
        }
    }

    /** Đang vẽ thế giới làm nền cho bảng (ManMenu, Nhân vật, Cửa hàng...): chỉ lớp thế giới. */
    public static boolean laNen;

    /** Bảng gọi: vẽ thế giới phía sau (không HUD). */
    public static void veNen(Graphics g) {
        laNen = true;
        try {
            Canvas.gameScr.paint(g);
        } finally {
            laNen = false;
            Canvas.resetTrans(g);
        }
    }

    @Override
    public void paint(Graphics g) {
        Font.nenSang = false;   // pokemon: HUD / bản đồ: chữ trắng
        if (!this.readyGetGameLogic) {
            return;
        }
        Canvas.resetTrans(g);
        if (this.isFillScr) {
            g.setColor(16777215);
            g.fillRect(0, 0, Canvas.w, Canvas.h);
            return;
        }
        if (lockcmx || lockcmy) {
            g.setColor(0);
            g.fillRect(0, 0, Canvas.w, Canvas.h);
        }
        int rung = 0;
        if (this.timerung > 0) {
            rung = r.nextInt(2);
        }
        // pokemon: rung man hinh khi boss danh manh / chi mang (HieuUngDanh)
        g.translate((-cmx) + rung + game.pokemon.HieuUngDanh.rungX(), (-cmy) - rung + game.pokemon.HieuUngDanh.rungY());
        g.setColor(0);
        g.fillRect(cmx - 5, cmy - 5, Canvas.w + 10, Canvas.h + 10);
        try {
            Tilemap.paintTile(g);
        } catch (Exception e) {
        }
        if (this.mainChar.posTransRoad != null && this.posCam != null) {
            this.tg.paint(g);
        }
        game.pokemon.HanhDong.veGon(g);   // pokemon: gợn sóng điểm chạm đi tới
        if (HudRong.bat() && !this.isAutoRangeWhenAuto) {
            HudRong.veVongChon(g, this.focusedActor);   // pokemon: vòng dưới chân mục tiêu
        }
        EffectManager.lowEffects.paintAll(g);
        try {
            HieuUng.veDuoi(g); // mod: hạt sáng khi di chuyển, nằm dưới nhân vật
        } catch (Throwable th) {
        }
        for (int i = 0; i < this.VecDataEff.size(); i++) {
            DataSkillEff e2 = (DataSkillEff) this.VecDataEff.elementAt(i);
            if (e2 != null) {
                e2.paintTop(g);
            }
        }
        for (int i2 = 0; i2 < aeo.size(); i2++) {
            Effect e3 = (Effect) aeo.elementAt(i2);
            if (e3 != null) {
                e3.paint(g);
            }
        }
        for (int i3 = 0; i3 < this.arrowsDown.size(); i3++) {
            IArrow arrow = (IArrow) this.arrowsDown.elementAt(i3);
            if (arrow != null) {
                arrow.paint(g);
            }
        }
        int treeIndex = 0;
        int actorIndex = 0;
        int npcLimit = 0;
        try {
            if (Tilemap.trees != null) {
                for (int b = this.gssy; b < this.gssye + 5; b++) {
                    if (paintCay == 1) {
                        int sizez = Tilemap.trees.size();
                        while (treeIndex < sizez && ((Tree) Tilemap.trees.elementAt(treeIndex)) != null) {
                            Tree t = (Tree) Tilemap.trees.elementAt(treeIndex);
                            if (t.y == b) {
                                if (t.x - (t.getW() / 32) <= this.gssxe && t.x + (t.getW() / 32) >= this.gssx) {
                                    t.paint(g);
                                }
                            } else if (t.y > b) {
                                break;
                            }
                            treeIndex++;
                        }
                    }
                    int size2 = this.npc_limit.size();
                    while (npcLimit < size2) {
                        Actor npc = (Actor) this.npc_limit.elementAt(npcLimit);
                        if (npc != null && npc.typeLimit == 1 && canPaintNPC_SV(npc)) {
                            npc.paint(g);
                        }
                        npcLimit++;
                    }
                    int size3 = this.actors.size();
                    while (actorIndex < size3) {
                        Actor actor = (Actor) this.actors.elementAt(actorIndex);
                        if (actor.catagory != 2) {
                            if ((actor.y >> 4) > b) {
                                break;
                            }
                            if (((((actor.catagory == 0 && paintChar == 1) || actor.ID == this.mainChar.ID || actor.catagory != 0) && (actor.y >> 4) == b && actor.y <= this.gssye * 16 && actor.y >= this.gssy * 16) || actor.allwayPaint()) && !(actor instanceof CongGym)) {
                                actor.paint(g);
                            }
                        } else if (actor.catagory == 2 && actor.typeLimit != 1 && canPaintNPC_SV(actor)) {
                            actor.paint(g);
                        }
                        actorIndex++;
                        if (actor == this.focusedActor && this.focusedActor.catagory != 10 && this.focusedActor.catagory != 11 && this.focusedActor.catagory != 12) {
                            boolean z = this.focusedActor.catagory == 2 && this.posNpcRequest != null && actor.x / 16 == this.posNpcRequest.x / 16 && actor.y / 16 == this.posNpcRequest.y / 16;
                            boolean npc1 = z;
                            if (!npc1 && !this.isAutoRangeWhenAuto) {
                                g.drawImage(Res.imgSelect, actor.x, actor.y - actor.height, 33);
                            }
                        }
                    }
                }
                Tilemap.paintTileTop(g);
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (congThanh != null) {
            congThanh.paint(g);
        }
        for (int i4 = 0; i4 < arrowsUp.size(); i4++) {
            IArrow arrow2 = (IArrow) arrowsUp.elementAt(i4);
            if (arrow2 != null) {
                arrow2.paint(g);
            }
        }
        int size5 = this.actors.size();
        for (int i5 = 0; i5 < size5; i5++) {
            Actor ac = (Actor) this.actors.elementAt(i5);
            if (ac != null) {
                ac.paintName(g);
            }
        }
        EffectManager.hiEffects.paintAll(g);
        for (int i6 = 0; i6 < this.VecDataEff.size(); i6++) {
            DataSkillEff e5 = (DataSkillEff) this.VecDataEff.elementAt(i6);
            if (e5 != null) {
                e5.paintBot(g);
            }
        }
        try {
            paintFlyText(g);
        } catch (Exception e6) {
        }
        int size6 = Tilemap.pointPop.size();
        for (int i7 = 0; i7 < size6; i7++) {
            PopupName p = (PopupName) Tilemap.pointPop.elementAt(i7);
            if (p != null) {
                if (Tilemap.isOfflineMap) {
                    p.name = "thoát";
                } else {
                    p.name = "vào";
                }
                p.paint(g);
            }
        }
        // pokemon (hệ sinh thái): phao câu, cờ mốc đua, điểm đào
        game.pokemon.SinhThai.veTheGioi(g);
        if (effAnimate.size() > 0 && !Tilemap.isOfflineMap && effAnimate.size() > 0) {
            for (int i8 = 0; i8 < effAnimate.size(); i8++) {
                AnimateEffect ani = (AnimateEffect) effAnimate.elementAt(i8);
                if (ani != null) {
                    ani.paint(g);
                }
            }
        }
        // pokemon (hệ sinh thái): phủ đêm theo giờ
        game.pokemon.SinhThai.veDem(g);
        if (charcountdonw != null) {
            charcountdonw.paint(g, objcountDown.x - 30, (objcountDown.y - 40) - objcountDown.getyHorse());
        }
        game.pokemon.NhipVe.hetTheGioi();
        if (laNen) {
            return;   // 0.9.5: vẽ làm nền cho bảng - bỏ HUD / thẻ / bản đồ nhỏ (bị bảng che, tốn khung)
        }
        try {
            paintFocusActorInfo(g);
            paintMainCharInfo(g);
        } catch (Exception e7) {
            e7.printStackTrace();
        }
        try {
            if (this == Canvas.currentScreen) {
                paintMiniMap(g);
                paintQuickSlot(g);
                paintServerInfo(g);
                HudRong.ve(g, this);   // pokemon: HUD màn rộng (nút trên, cụm chiêu, chat, bản đồ to)
            }
        } catch (Exception e8) {
            e8.printStackTrace();
        }
        super.paint(g);
        try {
            if (Tilemap.lv == 201 && timeChiemThanh != null) {
                paintChiemThanh(g);
            }
        } catch (Exception e9) {
            e9.printStackTrace();
        }
        try {
            if (this == Canvas.currentScreen) {
                paintChat(g);
                if (Canvas.currentDialog == null && !Canvas.menu.showMenu && !this.chatMode && !HudRong.bat()) {
                    g.drawImage(Res.imgSoft[0], 2, Canvas.h - 13, 0);
                    g.drawImage(Res.imgSoft[1], Canvas.w - 15, Canvas.h - 13, 0);
                }
            }
        } catch (Exception e10) {
            e10.printStackTrace();
        }
        if (this.listMSGServer != null && this.listMSGServer.size() > 0) {
            paintMsgServer(g);
        }
        paintTime(g);
        if (msgWorld.size() > 0) {
            MsgInfo ms = (MsgInfo) msgWorld.elementAt(0);
            int width = 0;
            while (true) {
                int i9 = width;
                if (i9 >= Canvas.w) {
                    break;
                }
                if (ms.arr.length == 1) {
                    g.drawImage(Res.imgNotice, i9, (-1) + (this.tk == null ? 0 : 17), 0);
                } else if (ms.arr.length == 2) {
                    g.drawImage(Res.imgNotice, i9, (-1) + (this.tk == null ? 0 : 17), 0);
                    g.drawImage(Res.imgNotice, i9, (-1) + (this.tk == null ? 17 : 34), 0);
                }
                width = i9 + Res.imgNotice.getWidth();
            }
            if (this.tk != null) {
                g.setColor(16777215);
                g.fillRect(0, 18, Canvas.w, 1);
            }
            ms.paint(g);
        }
    }

    public boolean canPaintNPC_SV(Actor npc) {
        if (npc.x + (npc.width / 2) < cmx || npc.x - (npc.width / 2) > cmx + Canvas.w || npc.y - npc.height > cmy + Canvas.h || npc.y < cmy) {
            return false;
        }
        return true;
    }

    private void paintMsgServer(Graphics g) {
        Canvas.resetTrans(g);
        g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36);
        g.setClip(15, Canvas.h - 20, Canvas.w - 30, 20);
        String str = (String) this.listMSGServer.elementAt(0);
        Font.arialFontW.drawString(g, str, this.xMsgServer, (Canvas.h - 10) - (Font.arialFontW.getHeight() / 2), 0);
        g.setClip(0, 0, Canvas.h, Canvas.w);
    }

    private void paintChiemThanh(Graphics g) {
        int y0 = 38;
        for (int i = 0; i < timeChiemThanh.length; i++) {
            if (((long) timeChiemThanh[i]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i])) > 0) {
                Font.arialFontW.drawString(g, new StringBuffer(String.valueOf(i + 1)).append(": ").append(nameChiemThanh[i]).append(" (").append(((long) timeChiemThanh[i]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i]))).append(")").toString(), Canvas.hw + 2, y0 + 1, 3);
                Font.arialFont.drawString(g, new StringBuffer(String.valueOf(i + 1)).append(": ").append(nameChiemThanh[i]).append(" (").append(((long) timeChiemThanh[i]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i]))).append(")").toString(), Canvas.hw, y0, 3);
                y0 += 10;
            } else if (!nameClan[i].equals("")) {
                Font.arialFontW.drawString(g, new StringBuffer(String.valueOf(i + 1)).append(": ").append(nameClan[i]).toString(), Canvas.hw + 2, y0 + 1, 3);
                Font.arialFont.drawString(g, new StringBuffer(String.valueOf(i + 1)).append(": ").append(nameClan[i]).toString(), Canvas.hw, y0, 3);
                y0 += 10;
            }
        }
    }

    public void paintServerInfo(Graphics g) {
        int size = serverInfo.size();
        if (size > 0 && this.tk == null) {
            this.tk = new Ticker((String) serverInfo.elementAt(0), Canvas.w + 10, 2);
            serverInfo.removeElementAt(0);
        }
        if (this.tk != null) {
            this.tk.paint(g);
        }
    }

    public class Ticker {
        int x;
        int y;
        int y1;
        public boolean end = false;
        public String info;

        public Ticker(String info, int x, int y) {
            this.info = "";
            this.info = info;
            this.x = x;
            this.y = y;
        }

        public void paint(Graphics g) {
            int width = 0;
            while (true) {
                int i = width;
                if (i < Canvas.w) {
                    g.drawImage(Res.imgNotice, i, this.y1, 0);
                    width = i + Res.imgNotice.getWidth();
                } else {
                    Font.normalFont[0].drawString(g, this.info, this.x, this.y, 0);
                    return;
                }
            }
        }

        public void update() {
            if (this.x + Font.normalFont[0].getWidth(this.info) + 10 < 0) {
                this.y1 -= 2;
            }
            if (this.y1 < -18) {
                this.end = true;
            }
            this.x -= 2;
        }
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (this.chatMode) {
            this.tfChat.keyPressed(keyCode);
            if (keyCode == -7) {
                this.tfChat.clear();
            }
            if (keyCode == -6) {
                Canvas.clearKeyHold();
                Canvas.clearKeyPressed();
                this.chatMode = false;
            }
            if (keyCode == -5) {
                String text = this.tfChat.getText();
                this.tfChat.setText("");
                doChatFromMe(text);
                return true;
            }
            return true;
        }
        if (PageScr.me != Canvas.currentScreen) {
            switch (keyCode) {
                case 48:
                    if (indexTabSlot == 0) {
                        indexTabSlot = 1;
                    } else {
                        indexTabSlot = 0;
                    }
                    xSlot = Res.wKhung;
                    return false;
                case 98:
                    Canvas.keyPressed[8] = true;
                    return false;
                case 102:
                    Canvas.keyPressed[4] = true;
                    return false;
                case 103:
                    Canvas.keyPressed[5] = true;
                    return false;
                case 104:
                    Canvas.keyPressed[6] = true;
                    return false;
                case 106:
                    Canvas.keyPressed[11] = true;
                    return false;
                case 109:
                    Canvas.keyPressed[0] = true;
                    return false;
                case 110:
                    Canvas.keyPressed[9] = true;
                    return false;
                case 114:
                    Canvas.keyPressed[1] = true;
                    return false;
                case 116:
                    Canvas.keyPressed[2] = true;
                    return false;
                case 117:
                    Canvas.keyPressed[10] = true;
                    return false;
                case 118:
                    Canvas.keyPressed[7] = true;
                    return false;
                case 121:
                    Canvas.keyPressed[3] = true;
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }

    /** tin chat mình vừa gõ (bỏ bản máy chủ gửi lại) */
    private String tinVuaGo;
    private long lucVuaGo;

    private void doChatFromMe(String text) {
        text = GameService.chuChat(text);
        if (text == null) {
            return;   // 0.9.5: tin rỗng / chỉ khoảng trắng: không gửi, không bong bóng (ô chat vẫn mở)
        }
        if ("cauca".equals(text.toLowerCase())) {
            // hệ sinh thái: chỉ câu ở HỒ - xa hồ thì báo, không gửi, không ném cần
            if (!game.pokemon.SinhThai.thuCau()) {
                return;
            }
        }
        if (typechat == CHAT_NORMAL) {
            this.tinVuaGo = text;
            this.lucVuaGo = System.currentTimeMillis();
            this.mainChar.chat = null;
            addChat(this.mainChar, text, 200);
            MessageScr.gI().addTab(new StringBuffer(String.valueOf(this.mainChar.name)).append(": ").append(text).toString(), null, 0);
            GameService.gI().chat(text);
        }
    }

    private void paintChat(Graphics g) {
        if ((this.hideGUI == 2 && !this.chatMode) || Canvas.menu.showMenu) {
            return;
        }
        if (this.hideGUI == 0) {
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            if (Canvas.currentDialog != null || this != Canvas.currentScreen) {
                return;
            }
            if (!HudRong.bat() && ((this.focusedActor != null && this.focusedActor.catagory != 2) || this.focusedActor == null)) {
                int k = Font.borderFont.getWidth("Đóng") + 10;
                g.setClip(k, Canvas.h - 16, (Canvas.w - k) - 13, 20);
                if (this.yMsgChat < this.toYchat) {
                    this.yMsgChat++;
                }
                if (this.listMSGServer != null && this.listMSGServer.size() > 0) {
                    int size0 = this.chatHistory.size();
                    for (int i = 0; i < size0; i++) {
                        ChatInfo ci = (ChatInfo) this.chatHistory.elementAt(i);
                        String info = "";
                        if (!ci.name.equals("")) {
                            info = new StringBuffer(String.valueOf(info)).append(ci.name).append(": ").toString();
                        }
                        String info2 = new StringBuffer(String.valueOf(info)).append(ci.content).toString();
                        int yy = ((Canvas.h - 16) - this.yMsgChat) + (i * 18);
                        if (yy + 13 >= Canvas.h - 20 && yy <= Canvas.h) {
                            Font.arialFontW.drawString(g, info2, k, yy, 0);
                        }
                    }
                }
                g.setClip(0, 0, Canvas.w, Canvas.h);
            }
        }
        if (!this.chatMode) {
            return;
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        this.tfChat.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        Font.borderFont.drawString(g, "Đóng", 5, Canvas.h - Screen.deltaY, 0);
        Font.borderFont.drawString(g, "Xóa", Canvas.w - 5, Canvas.h - Screen.deltaY, 1);
    }

    /** pokemon: bề ngang khung thông tin nhân vật góc trái trên (kiểu khung HP trận đấu Pokémon). */
    static int rongKhungHp() {
        if (HudRong.bat()) {
            return 140;
        }
        if (Canvas.w < 220) {
            return Canvas.w / 2 - 2;
        }
        return 104;
    }

    private void paintMainCharInfo(Graphics g) {
        if (this.hideGUI == 2) {
            return;
        }
        Canvas.resetTrans(g);
        // pokemon: khung trắng viền tối, chân dung trong vòng tròn, Lv + thanh HP (xanh->vàng->đỏ), PP, EXP
        int W = rongKhungHp();
        int H = imgInfo != null ? imgInfo.getHeight() : 34;
        game.pokemon.GiaoDien.khoi(g, 1, 2, W, H - 1, 5, 0x0B1222);
        game.pokemon.GiaoDien.khoi(g, 0, 0, W, H - 1, 5, game.pokemon.GiaoDien.VIEN);
        game.pokemon.GiaoDien.khoiDoc(g, 2, 2, W - 4, H - 5, 3, game.pokemon.GiaoDien.TRANG, 0xE6EEF8);
        g.setColor(game.pokemon.GiaoDien.VIEN);
        g.fillArc(3, 3, 28, 28, 0, 360);
        g.setColor(game.pokemon.GiaoDien.XANH_NHAT);
        g.fillArc(5, 5, 24, 24, 0, 360);
        if (this.mainChar.pokeDex <= 0 || !game.pokemon.PokeBody.paintChanDung(g, this.mainChar.pokeDex, this.mainChar.pokeShiny, 17, 17, 22)) {
            Res.getCharPartInfo(2, this.mainChar.currentHead).paintStatic(g, (short) 20, (short) 18, 0, this.mainChar.frame);
        }
        Canvas.resetTrans(g);
        Font.blackFont.drawString(g, new StringBuffer("Lv").append((int) this.mainChar.level).toString(), 35, 2, 0);
        if (W >= 100) {
            Font.arialFont.drawString(g, this.mainChar.getPercent(), W - 5, 2, 1);
        }
        int xb = 35;
        int wb = W - xb - 5;
        game.pokemon.GiaoDien.thanhHP(g, xb, 16, wb, 6, this.mainChar.hp, this.mainChar.maxhp);
        game.pokemon.GiaoDien.thanh(g, xb, 22, wb, 5, this.mainChar.mp, this.mainChar.maxmp, 1);
        game.pokemon.GiaoDien.thanh(g, xb, 27, wb, 4, this.mainChar.lvpercent, 1000, 2);
        if (this.mainChar.killer > 0) {
            Font.smallFont.drawString(g, new StringBuffer(String.valueOf((int) this.mainChar.killer)).toString(), 5, H + 2, 0);
        }
        if (this.mainChar.delay >= 0) {
            int[] t = this.mainChar.getTime();
            Font.borderFont.drawString(g, new StringBuffer(String.valueOf(t[0])).append(" : ").append(t[1]).toString(), 5, H + 10, 0);
        }
        String tm = this.mainChar.getGoldTime();
        if (!tm.equals("")) {
            Font.borderFont.drawString(g, tm, 5, H + 20, 0);
            Font.smallFontColor[0].drawString(g, Char.goldTime, 5, H + 34, 0);
        }
        if (Canvas.gameTick % 10 > 3) {
            if (this.mainChar.basePointLeft > 0) {
                g.drawImage(imgCong[0], W - 2, H + 5, 24);
            }
            if (this.mainChar.skillPointLeft > 0) {
                g.drawImage(imgCong[1], W - 2, H + 16, 24);
            }
        }
    }

    private void paintQuickSlot(Graphics g) {
        if (this.hideGUI == 2 || Canvas.currentDialog != null || this.chatMode || HudRong.bat()) {
            return;   // màn rộng: cụm nút tròn của HudRong
        }
        if (this.focusedActor != null && this.focusedActor.catagory == 2) {
            Font.borderFont.drawString(g, "Giao tiếp", Canvas.hw, Canvas.h - Screen.deltaY, 2);
            return;
        }
        Canvas.resetTrans(g);
        if (MainChar.quickSlot == null) {
            this.mainChar.loadQuickSlot();
        }
        int buoc = buocO();
        int wSlot = buoc * 5;
        int hSlot = caoThanhO();
        // pokemon: 5 ô chiêu kiểu Pokémon (màu hệ + hình chiêu + hồi chiêu), cùng vị trí cũ
        int xSlot0 = Canvas.hw - (wSlot / 2);
        int ySlot0 = ((Canvas.h - 16) - hSlot) + xSlot;
        int wo = buoc - 3;
        int ho = hSlot - 5;
        int lop = this.mainChar.clazz;
        long now = System.currentTimeMillis();
        int kMoi = -1;
        long tMoi = 0;
        for (int k = 0; k < 5; k++) {
            QuickSlot qs = MainChar.quickSlot[indexTabSlot][k];
            QuickSlot.paintO(g, qs, xSlot0 + (k * buoc) + 2, ySlot0 + 2, wo, ho, null, k == 2);   // không nhãn phím trên ô
            if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL) {
                long t = this.mainChar.timeLastUseSkills[qs.skillType];
                if (t > tMoi && now - t < 1300) {
                    tMoi = t;
                    kMoi = k;
                }
            }
        }
        // bộ ô đang dùng (phím 0 đổi): 2 chấm bên trái thanh
        for (int k = 0; k < 2; k++) {
            int yc = ySlot0 + 3 + (ho / 2) - 5 + (k * 7);
            g.setColor(game.pokemon.GiaoDien.VIEN);
            g.fillArc(xSlot0 - 5, yc - 1, 6, 6, 0, 360);
            g.setColor(k == indexTabSlot ? game.pokemon.GiaoDien.VANG : 0x5E7AA8);
            g.fillArc(xSlot0 - 4, yc, 4, 4, 0, 360);
        }
        // tên chiêu vừa dùng: viên màu hệ ngay trên thanh (không che nhân vật)
        if (kMoi >= 0) {
            QuickSlot qs = MainChar.quickSlot[indexTabSlot][kMoi];
            String ten = game.pokemon.PokeChieu.ten(lop, qs.skillType);
            if (ten != null) {
                // trên nhãn tên map (yTen = ySlot0 - 17), căn giữa màn hình
                Font f = Font.borderFont;
                int wt = f.getWidth(ten) + 14;
                int xt = Canvas.hw - (wt / 2);
                int yt = ySlot0 - 34;
                int mau = game.pokemon.KieuO.mauHe(game.pokemon.PokeChieu.he(lop, qs.skillType));
                game.pokemon.GiaoDien.khoi(g, xt, yt, wt, 16, 7, game.pokemon.GiaoDien.VIEN);
                game.pokemon.GiaoDien.khoiDoc(g, xt + 1, yt + 1, wt - 2, 14, 6, mau, game.pokemon.GiaoDien.pha(mau, 0, 2, 5));
                f.drawString(g, ten, xt + (wt / 2), yt + 1, Font.CENTER);
            }
        }
        if (Canvas.currentScreen == this && !this.chatMode) {
            paintSoftIcon(g);
        }
    }

    private void paintSoftIcon(Graphics g) {
        if (HudRong.bat()) {
            return;
        }
        g.drawImage(Res.imgSoft[0], 2, Canvas.h - 13, 0);
        g.drawImage(Res.imgSoft[1], Canvas.w - 15, Canvas.h - 13, 0);
    }

    private void paintFocusActorInfo(Graphics g) {
        // 0.9.5: nhãn tên / Lv quái + số bay ở lớp giao diện (đã hết lớp thế giới phóng)
        game.pokemon.NhanTheGioi.ve(g);
        game.pokemon.VienTrangThai.macDinh(rongKhungHp());
        if (this.hideGUI == 2) {
            return;
        }
        Canvas.resetTrans(g);
        // pokemon: màn rộng - khung mục tiêu giữa trên (góc phải trên là hàng nút + bản đồ nhỏ)
        int dxRong = HudRong.bat() ? (Canvas.w - rongKhungHp()) / 2 - (Canvas.w - rongKhungHp()) : 0;
        g.translate(dxRong, 0);
        if (!this.isAutoRangeWhenAuto && this.focusedActor != null) {
            // pokemon: khung đối thủ góc phải trên kiểu trận đấu Pokémon - tên, Lv, thanh HP, chân dung
            boolean coMau = false;
            LiveActor la = null;
            if (this.focusedActor.catagory == 0 || this.focusedActor.catagory == 1) {
                la = (LiveActor) this.focusedActor;
                coMau = la.maxhp != 0 && la.idBoss == -1;
            }
            boolean coTen = this.focusedActor.catagory != 10 && this.focusedActor.catagory != 11 && this.focusedActor.catagory != 12;
            if (!coTen && !coMau) {
                return;
            }
            int W = rongKhungHp();
            // 0.9.5: chữ thật - thêm hàng số HP / Lv dưới thanh
            int H = coMau ? (game.pokemon.ChuUI.bo != null ? 37 : 34) : 22;
            int x0 = Canvas.w - W;
            game.pokemon.GiaoDien.khoi(g, x0 + 1, 2, W - 1, H - 1, 5, 0x0B1222);
            game.pokemon.GiaoDien.khoi(g, x0, 0, W, H - 1, 5, game.pokemon.GiaoDien.VIEN);
            game.pokemon.GiaoDien.khoiDoc(g, x0 + 2, 2, W - 4, H - 5, 3, game.pokemon.GiaoDien.TRANG, 0xF8E8E4);
            g.setColor(game.pokemon.GiaoDien.VIEN);
            g.fillArc(Canvas.w - 32, 3, 28, 28, 0, 360);
            g.setColor(0xF8D8D0);
            g.fillArc(Canvas.w - 30, 5, 24, 24, 0, 360);
            if (coTen) {
                String ten = this.focusedActor.getDisplayName();
                if (ten == null) {
                    ten = "";
                }
                int wTen = W - 40;
                if (ten != null && Font.blackFont.getWidth(ten) > wTen) {
                    while (ten.length() > 2 && Font.blackFont.getWidth(new StringBuffer(ten).append("..").toString()) > wTen) {
                        ten = ten.substring(0, ten.length() - 1);
                    }
                    ten = new StringBuffer(ten).append("..").toString();
                }
                Font.blackFont.drawString(g, ten, x0 + 5, 2, 0);
                g.setClip(Canvas.w - 30, 5, 25, 25);
                this.focusedActor.paintCorner(g, Canvas.w - 18, 25);
                Canvas.resetTrans(g);
                g.translate(dxRong, 0);
            }
            if (coMau) {
                long hp = la.hp <= 0 ? 0 : la.hp;
                game.pokemon.GiaoDien.thanhHP(g, x0 + 5, 17, W - 40, 6, hp, la.maxhp);
                if (game.pokemon.ChuUI.bo != null) {
                    // 0.9.5: số HP / Lv chữ thật DƯỚI thanh (không đè thanh)
                    int yS = 23, hS = H - 3 - yS;
                    game.pokemon.ChuDe.chuGiua(g, game.pokemon.NhanTheGioi.hpMax(la.hp > 0 ? la.hp : 1, la.maxhp), x0 + W - 37, yS, hS,
                            game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU, 1);
                    game.pokemon.ChuDe.chuGiua(g, game.pokemon.NhanTheGioi.lv(la.level), x0 + 5, yS, hS,
                            game.pokemon.ChuDe.PHU, game.pokemon.ChuDe.CHU_PHU, 0);
                } else {
                    String so = new StringBuffer(String.valueOf(la.hp > 0 ? la.hp : 1)).append("/").append(la.maxhp).toString();
                    int wSo = Font.smallFont.getWidth(so) + 6;
                    game.pokemon.GiaoDien.khoi(g, x0 + 5, 25, wSo, 7, 2, game.pokemon.GiaoDien.VIEN);
                    Font.smallFont.drawString(g, so, x0 + 8, 26, 0);
                    String sLv = new StringBuffer("lv").append((int) la.level).toString();
                    int xLv = x0 + 8 + wSo;
                    if (xLv + Font.smallFontColor[2].getWidth(sLv) <= Canvas.w - 32) {
                        Font.smallFontColor[2].drawString(g, sLv, xLv, 24, 0);
                    }
                }
            }
        }
    }

    public void startSkill_Noel(int x, int y, int yBg, Vector list) {
        for (int i = 0; i < list.size(); i++) {
            LiveActor a = (LiveActor) list.elementAt(i);
            Skill_Noel sk = new Skill_Noel(x, y, a.x, a.y, a);
            EffectManager.addLowEffect(sk);
        }
    }

    public void startSkill_BossHalloween(int x, int y, int yBg, Vector list) {
        for (int i = 0; i < list.size(); i++) {
            LiveActor a = (LiveActor) list.elementAt(i);
            Skill_Boss_Halloween sk = new Skill_Boss_Halloween(x, y, a);
            EffectManager.addLowEffect(sk);
        }
    }

    public void startSkill_Cau_Lua(int xTo, int yTo, int yBg, Vector list) {
        Skill_Da_Roi skill = new Skill_Da_Roi(xTo, yTo, xTo, yBg, 0);
        skill.type = (short) 1;
        skill.list = list;
        EffectManager.hiEffects.addElement(skill);
    }

    public void startWeapsMango(LiveActor acFr, LiveActor acTo, int vangle) {
        if (acFr != null && acTo != null) {
            WeaponsMango obj = new WeaponsMango(acFr, acTo, vangle);
            arrowsUp.addElement(obj);
        }
    }

    public void startWeapsAngleBoss(int angle, int power, LiveActor acFr, LiveActor acTo, int type, boolean isUpDown, int addw, int addh, int xbg, int ybg, boolean lastActo) {
        if (isUpDown) {
            if (acFr != null && acTo != null) {
                WeaponsAngleBoss a = new WeaponsAngleBoss(angle, power, acFr, acTo, type, addw, addh, xbg, ybg, lastActo);
                arrowsUp.addElement(a);
                return;
            }
            return;
        }
        WeaponsAngleBoss a2 = new WeaponsAngleBoss(angle, power, acFr, acTo, type, addw, addh, xbg, ybg, lastActo);
        this.arrowsDown.addElement(a2);
    }

    public void SkillofBoss_Snake(LiveActor acFr, LiveActor acTo, int w, int h) {
        int i = 0;
        while (i < 12) {
            startWeapsAngleBoss(i * 30, 10, acFr, acTo, 0, true, w, h, 0, 0, i == 11);
            i++;
        }
        this.timerung = 20;
    }

    public void startWeaponsLazer(LiveActor acFrom, LiveActor acTo, int xadd, int yadd, int dir, int power, int idColor) {
        if (acFrom != null && acTo != null) {
            WeaponsLazer obj = new WeaponsLazer(acFrom, acTo, xadd, yadd, power, idColor);
            if (dir == 1) {
                this.arrowsDown.addElement(obj);
            } else {
                arrowsUp.addElement(obj);
            }
        }
    }

    public void startNewMagicBeam(int type, LiveActor from, LiveActor to, int x, int y, int power, byte effect2) {
        MagicBeam magicBeam = new MagicBeam();
        magicBeam.set(type, x, y, power, effect2, from, to);
        arrowsUp.addElement(magicBeam);
    }

    public void startNewMagicBeam(int type, LiveActor from, LiveActor to, int x, int y, int power, byte effect2, int angle) {
        MagicBeam magicBeam = new MagicBeam();
        magicBeam.set(type, x, y, power, effect2, from, to);
        magicBeam.setAngle(angle);
        arrowsUp.addElement(magicBeam);
    }

    public void startNewArrow(int type, LiveActor from, LiveActor to, int x, int y, int power, byte effect2, int imgIndex) {
        Arrow a = new Arrow(imgIndex);
        a.set(type, x, y, power, effect2, from, to);
        arrowsUp.addElement(a);
    }

    public void onActorMove(byte actorCatagory, short type, short actorID, short x2, short y2, byte pk, int charid, byte isnewmonster, byte isThanthu, boolean canfocus) {
        // mod: lệnh 4 (vị trí) cho chính mình = bước lướt bị từ chối -> auto đặt lại vị trí
        if (AutoGuard.viTriGuard(actorCatagory, type, actorID, x2, y2)) {
            return;
        }
        if (!this.readyGetGameLogic) {
        }
        isActorMove = true;
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (actorCatagory == a.catagory && a.ID == actorID) {
                a.canfocus = canfocus;
                a.setPosTo(x2, y2);
                if (a.catagory == 0) {
                    a.pk = pk;
                    if (a.getHp() <= 0 || a.getRealHp() <= 0) {
                        a.setState(3);
                    }
                }
                isActorMove = false;
                return;
            }
        }
        isActorMove = false;
        Actor a1 = CreateActor(actorCatagory, type, actorID, x2, y2, isnewmonster, isThanthu);
        if (!Util.inDataRange(this.mainChar, a1)) {
            return;
        }
        this.actors.addElement(a1);
        a1.canfocus = canfocus;
        if (this.actors.size() < 200) {
            switch (actorCatagory) {
                case 0:
                    if (isThanthu == -1) {
                        ((Char) a1).stand = System.currentTimeMillis();
                        ((Char) a1).pk = pk;
                    }
                    this.gameService.requestCharInfo(actorID);
                    break;
                case 1:
                    ((Monster) a1).idChar = charid;
                    this.gameService.requestMonsterInfo(actorID);
                    break;
            }
        }
    }

    public void onCharInfo(Char cha) {
        int aa;
        if (cha.idBoss != -1) {
            cha.dir = (short) 0;
        }
        if (idChiemThanh != null) {
            int size0 = idChiemThanh.length;
            for (int i = 0; i < size0; i++) {
                if (idChiemThanh[i] == cha.ID) {
                    if (cha != null) {
                        cha.EffFace = null;
                    }
                    if (cha != null && timeChiemThanh[i] > 0) {
                        cha.EffFace = new WayPoint(cha.x, cha.y - 5, (short) 1, (byte) 1, false);
                    }
                }
            }
        }
        if (cha.defend > 0 || cha.defend_magic > 0) {
            if (cha.defend > cha.defend_magic) {
                aa = 21;
            } else {
                aa = 36;
            }
            cha.effPhap = new Effect(cha.x, cha.y, aa);
        }
    }

    public void onPlayerAttackPlayer(short attacker, short attacked, byte skillID, int hpLost, int hpLeft, byte effect2, byte x2, byte buffAttack, byte level) {
        if (!this.readyGetGameLogic) {
            return;
        }
        if (attacker == this.mainChar.ID) {
            Char attakedChar = (Char) findCharByID(attacked);
            if (attakedChar != null) {
                if (this.mainChar.attkPower == 0) {
                    this.mainChar.attkPower = hpLost;
                }
                if (attakedChar != null) {
                    attakedChar.setRealHP(hpLeft);
                    if (attakedChar.hp <= 0 || attakedChar.realHP <= 0) {
                        attakedChar.state = (byte) 3;
                    }
                    AttackResult.startEff(effect2, attakedChar.x, attakedChar.y - 25);
                    if (buffAttack == 0) {
                        AttackResult.startEff(effect2, attakedChar.x, attakedChar.y + 35);
                    }
                }
                if (hpLost > 0) {
                    for (int i = 0; i < x2; i++) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpLost / x2).toString(), 0, attakedChar.x + (i % 2 == 0 ? (short) 0 : (short) -20), attakedChar.y - (i % 2 == 0 ? (short) 20 : (short) 30), 1, -2);
                    }
                    return;
                }
                return;
            }
            return;
        }
        if (attacked == this.mainChar.ID) {
            Char attackerC = (Char) findCharByID(attacker);
            if (attackerC != null) {
                attackerC.dir = Util.findDirection(attackerC, this.mainChar);
                attackerC.startAttack(this.mainChar, new AttackResult(hpLost, effect2), skillID, Char.skillLevelLearnt[skillID]);
                if (SkillManager.isSkillAeo(this.mainChar.clazz, skillID / 10)) {
                    Vector mst = new Vector();
                    mst.addElement(this.mainChar);
                    attackerC.currentSkillAnimate.setActorter(mst);
                }
                if (buffAttack == 0) {
                    Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, attackerC.x + 0, attackerC.y - 35, 1, -2);
                }
            }
            this.mainChar.downDuarable();
            this.mainChar.setRealHP(hpLeft);
            for (int i2 = this.actors.size() - 1; i2 >= 0; i2--) {
                Actor a = (Actor) this.actors.elementAt(i2);
                if (a.ID == attacker) {
                    return;
                }
            }
            return;
        }
        Char attakedChar2 = (Char) findCharByID(attacked);
        if (attakedChar2 != null) {
            attakedChar2.setRealHP(hpLeft);
            if (hpLeft == 0) {
                attakedChar2.state = (byte) 3;
            }
            if (buffAttack == 0) {
                Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, attakedChar2.x + 0, attakedChar2.y - 35, 1, -2);
            }
        }
        Char attackerC2 = (Char) findCharByID(attacker);
        if (attackerC2 != null) {
            attackerC2.dir = Util.findDirection(attackerC2, attakedChar2);
            attackerC2.startAttack(attakedChar2, new AttackResult(hpLost, effect2), skillID, level);
        }
    }

    public void onAttackMultiTarget(Message msg) {
        try {
            short attacker = msg.reader().readShort();
            byte skillID = msg.reader().readByte();
            int hpLost = msg.reader().readInt();
            byte effect2 = msg.reader().readByte();
            byte level = msg.reader().readByte();
            byte buffAttack = msg.reader().readByte();
            Char c = (Char) findCharByID(attacker);
            int len = msg.reader().readByte();
            Vector mst = new Vector();
            for (int i = 0; i < len; i++) {
                try {
                    Monster m = findMonsterByID(msg.reader().readShort());
                    int hpleft = msg.reader().readInt();
                    if (m != null) {
                        mst.addElement(m);
                        int i2 = m.hp - hpleft;
                        m.setRealHP(hpleft);
                        if (hpleft <= 0) {
                            int dx = 0;
                            int dy = 0;
                            if (c != null) {
                                dx = (m.x - c.x) * 2;
                                dy = (m.y - c.y) * 2;
                                while (true) {
                                    if (dx <= 10 && dy <= 10 && dx >= -10 && dy >= -10) {
                                        break;
                                    }
                                    dx >>= 1;
                                    dy >>= 1;
                                }
                            }
                            game.pokemon.HieuUngDanh.quaiChet(m);   // pokemon: guc mo dan + khoi
                            m.startDeadFly(dx, dy);
                        }
                        AttackResult.startEff(effect2, m.x + 0, m.y - 25);
                        if (hpLost > 0) {
                            Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpLost).toString(), 0, m.x + 0, m.y - 15, 1, -2);
                        }
                        if (buffAttack == 0) {
                            Canvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, m.x + 0, m.y - 35, 1, -2);
                        }
                    }
                } catch (Exception e) {
                    Cout.println(new StringBuffer("LOI TRONG HAM onAttackMultiTarget()").append(e.toString()).toString());
                }
            }
            if (this.mainChar.ID == attacker) {
                if (this.mainChar.attkPower == 0) {
                    this.mainChar.attkPower = hpLost;
                }
                if (hpLost > 0) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpLost).toString(), 0, ((Monster) mst.elementAt(0)).x + 0, ((Monster) mst.elementAt(0)).y - 15, 1, -2);
                    return;
                }
                return;
            }
            if (c != null && c.currentSkillAnimate != null) {
                c.startAttack((Monster) mst.elementAt(0), new AttackResult(hpLost, effect2), skillID, level);
                c.currentSkillAnimate.setMonster(mst);
            }
        } catch (Exception e2) {
            Cout.println("LOI TRONG HAM TAN CONG MULTI");
        }
    }

    public void onPlayerAttackMonster(short attacker, short attacked, byte skillID, int hpLost, int hpLeft, byte effect2, byte x2, byte buffAttack, byte level) {
        if (!this.readyGetGameLogic) {
            return;
        }
        LiveActor m = findMonsterByID(attacked);
        if (m != null) {
            int i = m.hp - hpLeft;
            m.setRealHP(hpLeft);
        }
        if (this.mainChar.ID == attacker) {
            if (this.mainChar.attkPower == 0) {
                this.mainChar.attkPower = hpLost;
            }
            // pokemon: mau so theo khac he chieu (uoc luong client) + chop/lac quai
            game.pokemon.HieuUngDanh.nguoiDanh(this.mainChar, m, skillID, effect2, hpLost);
            AttackResult.startEff(effect2, m.x, m.y - 25);
            if (buffAttack == 0) {
                AttackResult.startEff((byte) 3, m.x, m.y + 35);
            }
            if (hpLost > 0) {
                for (int i2 = 0; i2 < x2; i2++) {
                    Canvas.gameScr.startFlyText(new StringBuffer("-").append(hpLost).toString(), 0, m.x + (i2 % 2 == 0 ? (short) 0 : (short) -20), m.y - (i2 % 2 == 0 ? (short) 20 : (short) 30), 1, -2);
                }
                return;
            }
            return;
        }
        try {
            Char c = (Char) findCharByID(attacker);
            if (c != null && m != null) {
                c.dir = Util.findDirection(c, m);
                c.startAttack(m, new AttackResult(hpLost, effect2), skillID, level);
                if (buffAttack == 0) {
                    AttackResult.startEff((byte) 3, m.x, m.y + 35);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDropList(ItemDropInfo[] info) {
        for (int i = 0; i < info.length; i++) {
            try {
                Dropable dropable = (Dropable) CreateActor(info[i].itemCatagory, info[i].itemTemplateID, info[i].itemID, info[i].x, info[i].y, (byte) 0, (byte) -1);
                dropable.startDropFrom(info[i].x, info[i].y, dropable.x, dropable.y);
                this.actors.addElement(dropable);
            } catch (Exception e) {
                Cout.println("LOI TRONG MONSTER DIE 111");
                return;
            }
        }
    }

    public void startDeadFly(Monster m, int attacker) {
        Char c = (Char) findCharByID((short) attacker);
        if (m != null) {
            int dx = 0;
            int dy = 0;
            if (c != null) {
                dx = (m.x - c.x) * 2;
                int i = (m.y - c.y) * 2;
                while (true) {
                    dy = i;
                    if (dx <= 10 && dy <= 10 && dx >= -10 && dy >= -10) {
                        break;
                    }
                    dx >>= 1;
                    i = dy >> 1;
                }
            }
            game.pokemon.HieuUngDanh.quaiChet(m);   // pokemon: guc mo dan + khoi
            m.startDeadFly(dx, dy);
        }
    }

    public void onMonsterDie(MonsterDieInfo mdi) {
        Monster m;
        if (!this.readyGetGameLogic || (m = findMonsterByID(mdi.attacked)) == null) {
            return;
        }
        startDeadFly(m, mdi.attacker);
        if (this.focusedActor == m) {
            this.focusedActor = null;
        }
        if (mdi.itemDrop != null) {
            for (int i = 0; i < mdi.itemDrop.length; i++) {
                Dropable dropable = (Dropable) CreateActor(mdi.itemDrop[i].itemCatagory, mdi.itemDrop[i].itemTemplateID, mdi.itemDrop[i].itemID, mdi.itemDrop[i].x, mdi.itemDrop[i].y, (byte) 0, (byte) -1);
                dropable.startDropFrom(m.x, m.y, dropable.x, dropable.y);
                this.actors.addElement(dropable);
            }
        }
        if (this.mainChar.ID == mdi.attacker) {
            AttackResult.startEff(mdi.effect, m.x + 0, m.y - 25);
            try {
                if (mdi.hpLost > 0) {
                    if (mainQuest != null && mainQuest.isWorking()) {
                        if (mainQuest.isKillMons()) {
                            mainQuest.killMonster(m.monster_type, m.getDisplayName());
                        } else if (mainQuest.isKillMonsByLv()) {
                            mainQuest.killMonsterByLv(abs(this.mainChar.level - m.level));
                        }
                    }
                    if (subQuest != null && subQuest.isWorking()) {
                        if (subQuest.isKillMons()) {
                            subQuest.killMonster(m.monster_type, m.getDisplayName());
                        } else if (subQuest.isKillMonsByLv()) {
                            subQuest.killMonsterByLv(abs(this.mainChar.level - m.level));
                        }
                    }
                    if (clanQuest != null && clanQuest.isWorking()) {
                        if (clanQuest.isKillMons()) {
                            clanQuest.killMonster(m.monster_type, m.getDisplayName());
                        } else if (clanQuest.isKillMonsByLv()) {
                            clanQuest.killMonsterByLv(abs(this.mainChar.level - m.level));
                        }
                    }
                    if (this.mainChar.attkPower == 0) {
                        this.mainChar.attkPower = mdi.hpLost;
                    }
                    if (this.mainChar.kilAllMonsterQuest(m.monster_type)) {
                        addChat(new ChatInfo("", new StringBuffer("Hạ ").append(this.mainChar.infoQuest).append(" ").append(m.getDisplayName()).toString(), 0));
                        if (WindowInfoScr.idMonsterClanQuest != -1) {
                            if (WindowInfoScr.numberQuestGet + 1 >= WindowInfoScr.numberQuestAll) {
                                WindowInfoScr.idMonsterClanQuest = (byte) -1;
                                addChat(new ChatInfo("", "Hoàn thành nhiệm vụ.", 0));
                    game.pokemon.AmThanh.phat(game.pokemon.AmThanh.NHIEM_VU);   // pokemon
                            } else {
                                StringBuffer stringBuffer = new StringBuffer("Hạ được ");
                                short s = (short) (WindowInfoScr.numberQuestGet + 1);
                                WindowInfoScr.numberQuestGet = s;
                                addChat(new ChatInfo("", stringBuffer.append((int) s).append("/").append((int) WindowInfoScr.numberQuestAll).toString(), 0));
                            }
                        }
                    }
                    if (mdi.hpLost > 0) {
                        for (int i2 = 0; i2 < mdi.x2; i2++) {
                            Canvas.gameScr.startFlyText(new StringBuffer("-").append(mdi.hpLost).toString(), 0, m.x + (i2 % 2 == 0 ? (short) 0 : (short) -20), m.y - (i2 % 2 == 0 ? (short) 20 : (short) 30), 1, -2);
                        }
                        return;
                    }
                    return;
                }
                return;
            } catch (Exception e) {
                Cout.println("LOI TRONG MONSTER DIE 222");
                e.printStackTrace();
                return;
            }
        }
        mdi.hpLost = (short) (mdi.hpLost / mdi.x2);
        Char c = (Char) findCharByID(mdi.attacker);
        if (c != null && m != null) {
            c.dir = Util.findDirection(c, m);
            if (!SkillManager.isSkillAeo(c.clazz, mdi.skillID / 10)) {
                c.startAttack(m, new AttackResult(mdi.hpLost, mdi.effect), mdi.skillID, mdi.level);
            }
        }
    }

    public void startExplosionAt(short x, short y) {
        this.actors.addElement(new Explosion(x, y));
    }

    public Actor findCharByID(short id) {
        isFindChar = true;
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == 0 && a.ID == id) {
                isFindChar = false;
                return a;
            }
        }
        isFindChar = false;
        return null;
    }

    private Monster findMonsterByID(short id) {
        isFindMonster = true;
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == 1 && a.ID == id) {
                isFindMonster = false;
                return (Monster) a;
            }
        }
        isFindMonster = false;
        return null;
    }

    public Actor findActorByID(short id) {
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.ID == id) {
                return a;
            }
        }
        return null;
    }

    public Actor findActorByID_Cat(short id, byte cat) {
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.ID == id && a.catagory == cat) {
                return a;
            }
        }
        return null;
    }

    private DropItem findItemByID(short id) {
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == 3 && a.ID == id) {
                return (DropItem) a;
            }
        }
        return null;
    }

    private Dropable findPotionByID(short id, int catagory) {
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == catagory && a.ID == id) {
                return (Dropable) a;
            }
        }
        return null;
    }

    public void onMainCharInfo() {
        game.pokemon.ManTai.toi(game.pokemon.ManTai.BAN_DO);
        int aa;
        // mod: tạo ô skill nhanh trước, lỗi ở đoạn dưới không để quickSlot null (thanh skill thành ô đen)
        this.mainChar.loadQuickSlot();
        GameData.gI().getImage();
        this.mainChar.timeDelayRequestCharInfo = System.currentTimeMillis();
        if (this.mainChar.gender <= 0) {
            this.mainChar.gender = Char.GENDER_OF_CLAZZ[this.mainChar.clazz];
        }
        if (this.mainChar.level == 1 && this.mainChar.lvpercent == 0 && !this.mainChar.first) {
            this.first = true;
            MainChar mainChar = this.mainChar;
            MainChar mainChar2 = this.mainChar;
            this.mainChar.isFirstTimeGetPotion = true;
            mainChar2.isFirstTimeGetMoney = true;
            mainChar.isFirstTimeGetItem = true;
        }
        if (this.mainChar.defend > 0 || this.mainChar.defend_magic > 0) {
            if (this.mainChar.defend > this.mainChar.defend_magic) {
                aa = 21;
            } else {
                aa = 36;
            }
            this.mainChar.effPhap = new Effect(this.mainChar.x, this.mainChar.y, aa);
        }
        this.mainChar.loadQuickSlot();
        PauseMenu.gI().initName();
    }

    public void onMonsterInfo(MonsterInfo monsterInfo) {
        Monster m;
        if (this.readyGetGameLogic && (m = findMonsterByID(monsterInfo.id)) != null) {
            m.setInfo(monsterInfo);
        }
    }

    public void charOutGame(short charID) {
        if (!this.readyGetGameLogic) {
            return;
        }
        for (int i = 0; i < this.actors.size(); i++) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == 0 && a.ID == charID) {
                a.wantDestroy = true;
                return;
            }
        }
    }

    public void onBossAttackPlayer(Message msg) {
        try {
            short idMonster = msg.reader().readShort();
            byte idSkill = msg.reader().readByte();
            Monster m = findMonsterByID(idMonster);
            if (m != null) {
                byte nAttack = msg.reader().readByte();
                Vector list = new Vector();
                for (int i = 0; i < nAttack; i++) {
                    short id = msg.reader().readShort();
                    LiveActor li = (LiveActor) findActorByID(id);
                    if (li != null) {
                        li.damAttack = msg.reader().readInt();
                        int hpLeft = msg.reader().readInt();
                        if (this.mainChar.ID == id) {
                            if (this.mainChar.comeHome) {
                                this.mainChar.state = (byte) 0;
                                this.mainChar.comeHome = false;
                            }
                            MainChar mainChar = this.mainChar;
                            this.mainChar.hp = hpLeft;
                            mainChar.realHP = hpLeft;
                            m.dir = Util.findDirection(m, this.mainChar);
                            if (this.focusedActor == null) {
                                this.focusedActor = m;
                            }
                            list.addElement(this.mainChar);
                        } else {
                            if (m.monster_type != 90) {
                                m.getClass();
                            }
                            if (li != null) {
                                li.hp = hpLeft;
                                li.realHP = hpLeft;
                                if (li.hp <= 0) {
                                    ((Char) li).state = (byte) 3;
                                }
                                m.dir = Util.findDirection(m, li);
                            }
                            list.addElement(li);
                        }
                    } else {
                        msg.reader().readInt();
                        msg.reader().readInt();
                    }
                }
                m.startAttack(list, idSkill);
                // pokemon: canh bao vung + no theo he boss + rung man hinh
                game.pokemon.HieuUngDanh.bossDanh(m, list);
                try {
                    int idSkill2 = msg.reader().readUnsignedByte();
                    m.setIdSkill(idSkill2);
                    int setByMap = msg.reader().readUnsignedByte();
                    m.setByMap(setByMap);
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onMonsterAttackPlayer(short attacker, short attacked, int hpLost, int hpLeft) {
        Monster m;
        if (this.readyGetGameLogic && (m = findMonsterByID(attacker)) != null) {
            if (attacked == 32001) {
                m.setRemoveTarget();
                return;
            }
            if (m.monster_type != 84) {
                m.startAttack();
            }
            if (this.mainChar.ID == attacked) {
                m.setTarget(this.mainChar);
                if (this.mainChar.comeHome) {
                    this.mainChar.state = (byte) 0;
                    this.mainChar.comeHome = false;
                }
                MainChar mainChar = this.mainChar;
                this.mainChar.hp = hpLeft;
                mainChar.realHP = hpLeft;
                if (m.monster_type == 90) {
                    startFlyText(hpLost != 0 ? new StringBuffer("-").append(hpLost).toString() : "MISS", hpLost == 0 ? 0 : 4, this.mainChar.x, this.mainChar.y - 40, 0, -1);
                    m.startAttack(this.mainChar, hpLost, (byte) 8, (byte) 4);
                    if (this.mainChar.hp <= 0) {
                        this.mainChar.state = (byte) 3;
                        return;
                    }
                    return;
                }
                m.dir = Util.findDirection(m, this.mainChar);
                if (this.focusedActor == null) {
                    this.focusedActor = m;
                }
                int dis = Util.abs(this.mainChar.x - m.x) + Util.abs(this.mainChar.y - m.y);
                if (dis < 48) {
                    this.mainChar.smalljump();
                    startFlyText(hpLost != 0 ? new StringBuffer("-").append(hpLost).toString() : "MISS", hpLost == 0 ? 0 : 4, this.mainChar.x, this.mainChar.y - 40, 0, -1);
                    // pokemon: quai Pokemon danh gan -> no theo he quai (thay hieu ung chung)
                    if (m.monster_type != 90 && !game.pokemon.HieuUngDanh.quaiDanh(m, this.mainChar, -1, false)) {
                        EffectManager.addHiEffect(this.mainChar.x, this.mainChar.y - 10, 9);
                    }
                    if (this.mainChar.hp <= 0) {
                        this.mainChar.state = (byte) 3;
                    }
                } else if (m.monster_type != 90) {
                    // pokemon: quai Pokemon danh xa -> dan bay theo he, toi noi moi hien so
                    if (!game.pokemon.HieuUngDanh.quaiDanh(m, this.mainChar, hpLost, true)) {
                        startNewMagicBeam(20, m, this.mainChar, m.x, m.y, hpLost, (byte) 0);
                    }
                }
                this.mainChar.downDuarable();
                if (m.monster_type == 84) {
                    Vector list = new Vector();
                    list.addElement(this.mainChar);
                    m.startAttack(list, (byte) 0);
                    return;
                }
                return;
            }
            Char c = (Char) findCharByID(attacked);
            if (c != null) {
                m.setTarget(c);
                c.hp = hpLeft;
                c.realHP = hpLeft;
                if (c.hp <= 0) {
                    c.state = (byte) 3;
                }
                m.dir = Util.findDirection(m, c);
                c.smalljump();
                startFlyText(hpLost != 0 ? new StringBuffer("-").append(hpLost).toString() : "MISS", hpLost == 0 ? 0 : 4, c.x, c.y - 40, 0, -1);
                if (m.monster_type == 90) {
                    m.startAttack(c, hpLost, (byte) 8, (byte) 4);
                    return;
                }
                // pokemon: no theo he quai
                if (!game.pokemon.HieuUngDanh.quaiDanh(m, c, -1, false)) {
                    EffectManager.hiEffects.addElement(new Effect(c.x, c.y - 10, 9));
                }
                if (m.monster_type == 84) {
                    Vector list2 = new Vector();
                    list2.addElement(c);
                    m.startAttack(list2, (byte) 0);
                }
            }
        }
    }

    public void onPingBack() {
        this.pingDelay = System.currentTimeMillis() - this.lastTimePing;
        this.pingNextIn = 30;
    }

    public void onLoginSuccess() {
    }

    public void onConnectOK() {
        if (game.pokemon.ManTai.dangVao) {
            game.pokemon.ManTai.toi(game.pokemon.ManTai.DANG_NHAP);   // 0.9.5: cùng màn tải, sang bước đăng nhập
        } else {
            Canvas.startWaitDlg("Xin chờ..", true);
        }
    }

    public void onConnectFail() {
        if (game.pokemon.TuNoiLai.dangNoi && Canvas.loginScr != null) {
            // 0.9.4: lần tự nối lại vừa hỏng (mạng chưa về) -> thử lần kế; hết lượt thì hộp mất kết nối như cũ
            final String tk = game.pokemon.TuNoiLai.tk(Canvas.loginScr.tfUser.getText());
            final String mk = game.pokemon.TuNoiLai.mk(Canvas.loginScr.tfPass.getText());
            if (game.pokemon.TuNoiLai.thu(tk, mk, new Runnable() {
                public void run() {
                    GameScr.noiLai(tk, mk);
                }
            })) {
                return;
            }
            hopMatKetNoi(tk, mk);   // hết 3 lần: hộp mất kết nối, "Kết nối lại" vẫn dùng được
            return;
        }
        Canvas.startOKDlg("Không thể kết nối. Vui lòng chọn server khác", new IAction() {
            @Override
            public void perform() {
                Canvas.currentDialog = null;
                ServerListScr.gI().switchToMe();
                Session_ME.gI().close();
            }
        });
    }

    /** Ngắt hẳn phiên cũ rồi đăng nhập lại bằng tk / mk (nút "Kết nối lại" + TuNoiLai). */
    public static void noiLai(String tk, String mk) {
        int sv = ServerListScr.index;
        Canvas.cmdNgatKetNoi.action.perform();
        ServerListScr.index = sv;
        if (sv != -1 && tk.length() > 0 && mk.length() > 0) {
            Canvas.loginScr.tfUser.setText(tk);
            Canvas.loginScr.tfPass.setText(mk);
            Canvas.loginScr.switchToMe();
            Canvas.loginScr.doLogin();
        }
    }

    public void onDisconnect() {
        try {
            if (Sound.me != null) {
                Sound.me.stopMusic(); // Sound.me có thể null (máy không có âm thanh): trước đây NPE -> không hiện hộp thoại, treo
            }
        } catch (Throwable e) {
        }
        if (Canvas.currentScreen != Canvas.loginScr && !DangKyScr.matKetNoi()) {
            if (autoFight) {
                autoFight = false;
                Canvas.cmdNgatKetNoi.action.perform();
            } else {
                autoFight = false;
                // pokemon: hộp thoại thân thiện + "Kết nối lại" (đăng nhập lại bằng tài khoản vừa dùng)
                final String tk = game.pokemon.TuNoiLai.tk(Canvas.loginScr != null && Canvas.loginScr.tfUser != null ? Canvas.loginScr.tfUser.getText() : "");
                final String mk = game.pokemon.TuNoiLai.mk(Canvas.loginScr != null && Canvas.loginScr.tfPass != null ? Canvas.loginScr.tfPass.getText() : "");
                // 0.9.4: rớt thoáng qua -> tự nối lại (tối đa 3 lần, chờ tăng dần) rồi mới hỏi người chơi
                if (game.pokemon.TuNoiLai.thu(tk, mk, new Runnable() {
                    public void run() {
                        GameScr.noiLai(tk, mk);
                    }
                })) {
                    return;
                }
                hopMatKetNoi(tk, mk);
            }
        }
    }

    /** Hộp "Mất kết nối" + nút "Kết nối lại" (đăng nhập lại bằng tài khoản vừa dùng) / "Thoát". */
    static void hopMatKetNoi(final String tk, final String mk) {
        Command ketNoiLai = new Command("Kết nối lại", new IAction() {
            @Override
            public void perform() {
                game.pokemon.TuNoiLai.thuCong();   // vào lại đúng Pokémon; hỏng thì hiện lại hộp này
                GameScr.noiLai(tk, mk);
            }
        });
        Canvas.msgdlg.isIcon = false;
        Canvas.msgdlg.setInfo("Mất kết nối với máy chủ! Kiểm tra mạng rồi bấm Kết nối lại nhé.", (Command) null, ketNoiLai, new Command("Thoát", new IAction() {
            @Override
            public void perform() {
                Canvas.cmdNgatKetNoi.action.perform();
            }
        }));
        Canvas.currentDialog = Canvas.msgdlg;
    }

    public boolean isOffLineMap(int mapID2) {
        int size0 = offMapID.length;
        for (int i = 0; i < size0; i++) {
            if (mapID2 == offMapID[i]) {
                return true;
            }
        }
        return false;
    }

    public short getIndexArrMap() {
        int size0 = mapID.length;
        for (int i = 0; i < size0; i++) {
            for (int j = 0; j < mapID[i].length; j++) {
                if (this.map == mapID[i][j]) {
                    return idArrMap[i];
                }
            }
        }
        return (short) -1;
    }

    public short getIndexArrMap(int idm) {
        int size0 = mapID.length;
        for (int i = 0; i < size0; i++) {
            for (int j = 0; j < mapID[i].length; j++) {
                if (idm == mapID[i][j]) {
                    return idArrMap[i];
                }
            }
        }
        return (short) -1;
    }

    public short getMapLoad(int mapid) {
        int size0 = mapID.length;
        for (int i = 0; i < size0; i++) {
            for (int j = 0; j < mapID[i].length; j++) {
                if (mapid == mapID[i][j]) {
                    return mapID[i][0];
                }
            }
        }
        return (short) -1;
    }

    public void onMap(short map, short toX, short toY, short mapLoad, String mapName, byte[] byteMap) {
        // mod: vào lại đúng map đang đứng: ép nạp lại map để không mất NPC
        if (AutoGuard.mapGuard(map)) {
            return;
        }
        Cout.println(new StringBuffer("onMap: ").append((int) map).toString());
        Canvas.endDlg();
        this.mainChar.posTransRoad = null;
        this.mainChar.EffFace = null;
        this.focusedActor = null;
        this.VecTime.removeAllElements();
        charcountdonw = null;
        objcountDown = null;
        this.nearActors.removeAllElements();
        if (effManager != null) {
            effManager.removeAllElements();
        }
        congThanh = null;
        short loadMap = mapLoad;
        if (loadMap == -1) {
            loadMap = map;
        }
        // pokemon: xoa hieu ung cu, nhac nen theo loai ban do + tieng dich chuyen
        game.pokemon.HieuUngDanh.xoa();
        game.pokemon.AmThanh.vaoMap(loadMap, mapName);
        if (toX != -1 && toY != -1) {
            MainChar mainChar = this.mainChar;
            short s = (short) ((toX * 16) + 8);
            this.mainChar.xTo = s;
            mainChar.x = s;
            MainChar mainChar2 = this.mainChar;
            short s2 = (short) ((toY * 16) + 8);
            this.mainChar.yTo = s2;
            mainChar2.y = s2;
            if (this.mainChar.x / 16 <= 1) {
                MainChar mainChar3 = this.mainChar;
                mainChar3.x = (short) (mainChar3.x + 32);
            }
            if (this.mainChar.y / 16 <= 1) {
                MainChar mainChar4 = this.mainChar;
                mainChar4.y = (short) (mainChar4.y + 32);
            }
            this.mainChar.xTo = this.mainChar.x;
            this.mainChar.yTo = this.mainChar.y;
            if (this.mainChar.myPet != null) {
                setInfoCharPet(this.mainChar.myPet, this.mainChar);
            }
        }
        int isSame = 0;
        this.actors.removeAllElements();
        if (this.mainChar != null) {
            this.actors.addElement(this.mainChar);
        }
        if (this.map != map) {
            isSame = 1;
            this.map = map;
            Res.load.loadMap(loadMap, new IAction() {
                @Override
                public void perform() {
                    Canvas.gameScr.loadCamera();
                    GameScr.this.posNpcRequest = null;
                    if (GameScr.this.current_npc_talk != -1 || GameScr.this.idNpcReceive != -1) {
                        GameScr.this.set_npc_request(GameScr.this.current_npc_talk);
                        Cout.println(new StringBuffer("KIEM TRA = ").append((int) GameScr.this.current_npc_talk).toString());
                    }
                    Canvas.gameScr.switchToMe();
                    GameScr.this.gameService.changeMapOK(-500, 0, 0);
                    try {
                        GameScr.this.setAutoTask();
                        GameScr.this.setObjDynamicType();
                    } catch (Exception e) {
                        Cout.println("ERROR ONMAP");
                    }
                }
            }, byteMap);
        }
        if (loadMap == 107) {
            this.mainChar.delay = 1;
            this.mainChar.realTime = 1;
            this.mainChar.timeWait2Board = System.currentTimeMillis();
        } else if (loadMap == 10) {
            this.mainChar.delay = -1;
            this.mainChar.realTime = -1;
            if (this.mainChar.potions[20] > 0) {
                int[] iArr = this.mainChar.potions;
                iArr[20] = iArr[20] - 1;
            }
        }
        if (loadMap != 201) {
            timeChiemThanh = null;
        }
        this.mainChar.state = (byte) 0;
        this.mainChar.comeHome = false;
        try {
            if (Canvas.loginScr != null) {
                Canvas.loginScr = null;
                CharSelectScr.me = null;
                Canvas.imgLogo = null;
                ServerListScr.close();
            }
            Canvas.currentDialog = null;
            Canvas.clearKeyHold();
            Canvas.clearKeyPressed();
            Tilemap.setNameMap(loadMap);
            // pokemon: tên server gửi đã đổi phần lớn; còn sót (vd "Bạch hạc_1") đổi nốt khi hiện
            nameMap = game.pokemon.TenMapPokemon.doiHien(mapName);
            if (isSame == 0) {
                Canvas.gameScr.switchToMe();
                setAutoTask();
                setObjDynamicType();
            }
        } catch (Exception e) {
            Cout.println("ERROR ONMAP");
        }
        bDragon[0] = new Boss_Dragon();
    }

    public void setObjDynamicType() {
        int size0 = this.objDynamic.size();
        for (int i = 0; i < size0; i++) {
            DynamicObj ac = (DynamicObj) this.objDynamic.elementAt(i);
            if (!this.actors.contains(ac)) {
                this.actors.addElement(ac);
                for (int a = 0; a < ac.w / 2; a++) {
                    int aa = ((ac.y / 16) * Tilemap.w) + ((ac.x / 16) - a);
                    if (aa < Tilemap.type.length) {
                        Tilemap.type[aa] = 2;
                    }
                    int bb = ((ac.y / 16) * Tilemap.w) + (ac.x / 16) + a;
                    if (bb < Tilemap.type.length) {
                        Tilemap.type[bb] = 2;
                    }
                    for (int b = 0; b < ac.h / 2; b++) {
                        int cc = (((ac.y / 16) - b) * Tilemap.w) + ((ac.x / 16) - a);
                        if (cc < Tilemap.type.length) {
                            Tilemap.type[cc] = 2;
                        }
                        int dd = (((ac.y / 16) - b) * Tilemap.w) + (ac.x / 16) + a;
                        if (dd < Tilemap.type.length) {
                            Tilemap.type[dd] = 2;
                        }
                    }
                }
            }
        }
        this.objDynamic.removeAllElements();
    }

    public void setAutoTask() {
        if (Char.skillLevelLearnt[0] == 0) {
            if ((Tilemap.lv != 0 || Tilemap.lv != 101) && Tilemap.lv != 105 && iTaskAuto == 0) {
                iTaskAuto = 1;
                Canvas.startYesNoDlg("Bạn có muốn hệ thống tự động làm nhiệm vụ cho bạn hay không ?", new IAction() {
                    @Override
                    public void perform() {
                        Canvas.startWaitDlg();
                        try {
                            GameScr.mapFind = new byte[Tilemap.w][Tilemap.h];
                            GameScr.iTaskAuto = 2;
                        } catch (Exception e) {
                            Cout.println(new StringBuffer("loi set auto task ").append(e.toString()).toString());
                        }
                        Canvas.endDlg();
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        GameScr.iTaskAuto = 1;
                        Canvas.endDlg();
                        GameScr.this.addChat(new ChatInfo("", "Hãy đi gặp các bậc đàn anh trong làng để học chiêu thức.", 0));
                    }
                });
            }
        }
    }

    public int getPosNameMap(int idm) {
        for (int i = 0; i < mapID.length; i++) {
            for (int j = 0; j < mapID[i].length; j++) {
                if (idm == mapID[i][j]) {
                    return j;
                }
            }
        }
        return -1;
    }

    public void onLoginFail(String reason) {
        if (reason.startsWith("1")) {
            Canvas.startOKDlg(reason.substring(1), new IAction() {
                @Override
                public void perform() {
                    Canvas.currentDialog = null;
                    ServerListScr.gI().switchToMe();
                    Session_ME.gI().close();
                }
            });
        } else {
            Canvas.startOKDlg(reason);
        }
    }

    public void listServer(String reason, final String[] ip, final short[] port, final String[] name) {
        Canvas.endDlg();
        Canvas.startOKDlg(reason, new IAction() {
            @Override
            public void perform() {
                Cout.println("CHUAN BI CAP NHAT ");
                Canvas.currentDialog = null;
                ServerListScr.gI().switchToMe();
                ServerListScr.address = ip;
                ServerListScr.port = port;
                ServerListScr.nameServer = name;
                RMS.saveRMSStringIp(ip, port, name);
                Session_ME.gI().close();
            }
        });
    }

    public void onCharWearingInfo(short charID, Vector items, byte idFashion, byte typePet, String petInfo, AnimalMove myPet, short[] idModel0, byte idIconAnimal) {
        Char c = charID == this.mainChar.ID ? (Char) this.mainChar : (Char) findCharByID(charID);
        if (c == null) {
            return;
        }
        c.idFashion = idFashion;
        c.setWearingInfo(items);
        c.idAnimal = typePet;
        c.infoAnimal = petInfo;
        c.idModel = idModel0;
        c.idPaintIconAnimal = idIconAnimal;
        if (myPet != null) {
            myPet.idSameMyOwnerID = c.ID;
            setCharPet(myPet, c);
        } else {
            game.pokemon.PetDuyNhat.cat(this.actors, c);   // pokemon: cất pet (một pet / người, không sót bản cũ)
        }
        game.pokemon.TrangThaiNguoiChoi.doi(c == this.mainChar ? game.pokemon.TrangThaiNguoiChoi.TRANG_BI : 0);
    }

    /**
     * Đặt pet đi theo của chủ (gói 37 trang bị). pokemon: qua PetDuyNhat - đúng MỘT actor pet / người chơi: đổi pet
     * thay pet cũ (kể cả đổi giữa pet kiểu 5 = Pet và pet AnimalMove), không thêm bản thứ hai.
     */
    public void setCharPet(AnimalMove pet, Char myOwner0) {
        pet.idSameMyOwnerID = myOwner0.ID;
        if (pet.type == 5) {
            AnimalMove cu = myOwner0.petCanh;
            setInfoCharPet(pet, myOwner0);   // mẫu giữ thông tin pet (mô tả, thời hạn) cho bảng trang bị thú
            if (cu instanceof Pet && this.actors.contains(cu) && ((Pet) cu).idTemplate == pet.imgID) {
                game.pokemon.PetDuyNhat.dat(this.actors, myOwner0, cu);
                return;
            }
            Pet p = new Pet(myOwner0, pet.imgID);
            game.pokemon.PetDuyNhat.dat(this.actors, myOwner0, p);
            return;
        }
        AnimalMove pp = game.pokemon.PetDuyNhat.timAnimal(this.actors, myOwner0.ID);
        if (pp != null) {
            pp.type = pet.type;
            pp.imgID = pet.imgID;
            pp.sumFrame = pet.sumFrame;
            pp.infoPet = pet.infoPet;
            pp.totalTime = pet.totalTime;
            pp.timeStart = pet.timeStart;
            pp.idSameMyOwnerID = myOwner0.ID;
        } else {
            pp = pet;
            pp.setFrameType4();
        }
        setInfoCharPet(pp, myOwner0);
        game.pokemon.PetDuyNhat.dat(this.actors, myOwner0, pp);
    }

    public void clearCharPet(short id) {
        for (int i = this.actors.size() - 1; i >= 0; i--) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac != null && ac.catagory == 12 && ac.ID == id && ac.ispetTool()) {
                this.actors.removeElementAt(i);
            }
        }
    }

    public void setInfoCharPet(AnimalMove pp, Char myOwner0) {
        pp.hImg = (short) 1;
        pp.wImg = (short) 1;
        pp.myOwner = myOwner0;
        myOwner0.myPet = pp;
        short s = (myOwner0.dir == 2 || myOwner0.dir == 1) ? (short) (myOwner0.x + 20) : (short) (myOwner0.x - 20);
        pp.xTo = s;
        pp.x = s;
        short s2 = myOwner0.y;
        pp.yTo = s2;
        pp.y = s2;
        if (pp.type != 1) {
            pp.addY = (short) ((-myOwner0.height) + 15);
        } else {
            pp.addY = (short) 0;
        }
        pp.v = (byte) myOwner0.speed;
        pp.status = (short) 0;
        pp.idFrame = (short) 0;
        pp.count = (short) 0;
        pp.setFrameType4();
    }

    public void onViewAnimalInfo(Char ch) {
        WindowInfoScr.gI().setInfo(0, false, new byte[]{31});
        WindowInfoScr.charWearing = ch;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr.gI().name = "Trang bị thú";
        Canvas.endDlg();
    }

    public void onAnimalWearingInfo(short charID, Vector items, Image imgAnimal0, byte numFrame0, int wimg0, int himg0, byte timeChangeFrame0) {
        if (charID == this.mainChar.ID) {
            this.mainChar.imgAnimal = imgAnimal0;
            this.mainChar.numFrame = numFrame0;
            this.mainChar.wimg = wimg0;
            this.mainChar.himg = himg0;
            this.mainChar.timeChangeFrame = timeChangeFrame0;
            this.mainChar.setAnimalWearingInfo(items);
            return;
        }
        Char c = (Char) findCharByID(charID);
        if (c != null) {
            c.imgAnimal = imgAnimal0;
            c.numFrame = numFrame0;
            c.wimg = wimg0;
            c.himg = himg0;
            c.timeChangeFrame = timeChangeFrame0;
            c.setAnimalWearingInfo(items);
            onViewAnimalInfo(c);
        }
    }

    public void onCharInventory(long charXu, int[] potions, Vector items, Vector animals, int type) {
        this.mainChar.setInventory(charXu, potions, items, animals, type);
        cancelTrade();
        if (Canvas.currentScreen == WindowInfoScr.gI() && WindowInfoScr.index[WindowInfoScr.focusTab] == 0) {
            WindowInfoScr.gI().isLoad = true;
            PauseMenu.gI().showMe(0);
            WindowInfoScr.gI().isLoad = false;
        } else if (Canvas.currentScreen == ShopNew.gI() && ShopNew.index != null && ShopNew.focusTab < ShopNew.index.length
                && ShopNew.index[ShopNew.focusTab] == 0) {
            // pokemon: hành trang mở từ Cửa hàng (ShopNew) cũng làm mới ngay sau khi dùng / mua (trước giữ món đã dùng)
            ShopNew.gI().isLoad = true;
            PauseMenu.gI().showShopNew(0);
            ShopNew.gI().isLoad = false;
        }
    }

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public void onGetItemFromGround(short whoGet, ItemInInventory cii) {
        DropItem item = findItemByID(cii.itemOldID);
        if (item != null) {
            Char ch = (Char) findCharByID(whoGet);
            if (ch != null) {
                if (ch == this.mainChar) {
                    addChat(new ChatInfo("", new StringBuffer("Nhặt được ").append(item.getDisplayName()).toString(), 1));
                    game.pokemon.HanhDong.bao(new StringBuffer("Nhặt được ").append(item.getDisplayName()).toString());   // pokemon: thông báo nhanh
                    game.pokemon.AmThanh.phat(game.pokemon.AmThanh.NHAT);   // pokemon
                }
                item.startFlyTo(ch.x, ch.y);
            }
            item.wantDestroy = true;
        }
        if (whoGet == this.mainChar.ID) {
            Char.inventory.addElement(cii);
            if (this.mainChar.isFirstTimeGetItem) {
                Canvas.startOKDlg("Bạn vừa nhặt được một món đồ.", false, 10);
                this.mainChar.isFirstTimeGetItem = false;
            }
        }
    }

    public void onGetPotionFromGround(short whoGet, short potionID, short potionType, short potionQuantity) {
        DropPotion potion = (DropPotion) findPotionByID(potionID, 4);
        if (potion != null) {
            Char ch = (Char) findCharByID(whoGet);
            if (ch != null) {
                if (ch == this.mainChar) {
                    startFlyText(new StringBuffer("+").append((int) potionQuantity).append(potion.getDisplayName2().toLowerCase()).toString(), 2, this.mainChar.x - 10, this.mainChar.y - 30, 1, -1);
                    game.pokemon.HanhDong.bao(new StringBuffer("+").append((int) potionQuantity).append(' ').append(potion.getDisplayName2()).toString());   // pokemon: thông báo nhanh
                    if (this.mainChar.getItemQuest(potion.item_template_id)) {
                        addChat(new ChatInfo("", new StringBuffer("Nhặt ").append(this.mainChar.infoQuest).append(" ").append(potion.getDisplayName2()).toString(), 0));
                    }
                }
                potion.startFlyTo(ch.x, ch.y);
            } else {
                potion.wantDestroy = true;
            }
        }
        if (whoGet == this.mainChar.ID) {
            if (potionType == 0) {
                this.mainChar.charXu += (long) potionQuantity;
            } else {
                int[] iArr = this.mainChar.potions;
                iArr[potionType] = iArr[potionType] + potionQuantity;
            }
            if (potionType == 0) {
                if (this.mainChar.isFirstTimeGetMoney) {
                    Canvas.startOKDlg("Bạn vừa nhặt được xu Bạn có thể dùng xu để mua HP, MP và đồ dùng khác.", false, 1);
                    this.mainChar.isFirstTimeGetMoney = false;
                    return;
                }
                return;
            }
            if (idItemClanquest != -1 && idItemClanquest == potionType) {
                WindowInfoScr.numberQuestGet = (short) (WindowInfoScr.numberQuestGet + 1);
                if (WindowInfoScr.numberQuestGet >= WindowInfoScr.numberQuestAll) {
                    idItemClanquest = (byte) -1;
                    addChat(new ChatInfo("", "Hoàn thành nhiệm vụ.", 0));
                    game.pokemon.AmThanh.phat(game.pokemon.AmThanh.NHIEM_VU);   // pokemon
                } else {
                    addChat(new ChatInfo("", new StringBuffer("Nhặt được: ").append((int) WindowInfoScr.numberQuestGet).append("/").append((int) WindowInfoScr.numberQuestAll).toString(), 0));
                }
            }
            if (this.mainChar.isFirstTimeGetPotion) {
                Canvas.startOKDlg("Bạn vừa nhặt được một vật phẩm dùng được. Xin vào mục hành trang để xem.", false, 1);
                this.mainChar.isFirstTimeGetPotion = false;
            }
        }
    }

    public void onGetGemFromGround(short whoGet3, short gemID, byte catagory) {
        DropGemItem gem = (DropGemItem) findPotionByID(gemID, catagory);
        if (gem != null) {
            Char ch = (Char) findCharByID(whoGet3);
            if (ch != null) {
                gem.startFlyTo(ch.x, ch.y);
            } else {
                gem.wantDestroy = true;
            }
        }
    }

    public void onGetItemQuestFromGround(short whoGet3, short gemID, byte itemQuestType) {
        ItemQuest gem = (ItemQuest) findPotionByID(gemID, 14);
        if (gem != null) {
            Char ch = (Char) findCharByID(whoGet3);
            if (ch != null) {
                gem.startFlyTo(ch.x, ch.y);
            } else {
                gem.wantDestroy = true;
            }
            if (this.mainChar.ID == whoGet3) {
                if (mainQuest != null && mainQuest.isWorking()) {
                    mainQuest.getItemQuest(itemQuestType);
                }
                if (subQuest != null && subQuest.isWorking()) {
                    subQuest.getItemQuest(itemQuestType);
                }
                if (clanQuest != null && clanQuest.isWorking()) {
                    clanQuest.getItemQuest(itemQuestType);
                }
            }
        }
    }

    public void dellPotionQuest() {
        for (int i = 10; i < 14; i++) {
            this.mainChar.potions[i] = 0;
        }
    }

    private void setItemInfo(Vector list, ItemInInventory itemMoreInfo) {
        ItemInInventory itemi = getItemByID(list, itemMoreInfo.itemID);
        if (itemi != null) {
            itemi.isEnoughData = true;
            itemi.clazz = itemMoreInfo.clazz;
            itemi.dayUse = itemMoreInfo.dayUse;
            itemi.property = itemMoreInfo.property;
            itemi.allAttribute = itemMoreInfo.allAttribute;
            itemi.itemAttribute = itemMoreInfo.itemAttribute;
            itemi.durable = itemMoreInfo.durable;
            itemi.lastTime = System.currentTimeMillis();
            itemi.lock = itemMoreInfo.lock;
            if (itemMoreInfo.plusTemplate > -1) {
                itemi.plusTemplate = itemMoreInfo.plusTemplate;
            }
        }
    }

    public void onItemInfo(ItemInInventory itemMoreInfo) {
        Cout.println("onItemInfo");
        setItemInfo(Char.inventory, itemMoreInfo);
        if (WindowInfoScr.charWearing != null) {
            setItemInfo(WindowInfoScr.charWearing.wearingItems, itemMoreInfo);
            if ((WindowInfoScr.charWearing.useHorse != -1 || WindowInfoScr.charWearing.idhorse > -1) && WindowInfoScr.charWearing.wearingAnimal != null && WindowInfoScr.charWearing.wearingAnimal.size() > 0) {
                setItemInfo(WindowInfoScr.charWearing.wearingAnimal, itemMoreInfo);
            }
        }
        setItemInfo(this.mainChar.tItems, itemMoreInfo);
        setItemInfo(this.mainChar.rItems, itemMoreInfo);
        setItemInfo(Char.itembag, itemMoreInfo);
        setItemInfo(WindowInfoScr.gI().sellItem, itemMoreInfo);
        if (WindowInfoScr.gI().listItemAnimal != null && WindowInfoScr.gI().listItemAnimal.size() > 0) {
            setItemInfo(WindowInfoScr.gI().listItemAnimal, itemMoreInfo);
        }
        if (Canvas.currentScreen == WindowInfoScr.gI()) {
            WindowInfoScr.gI().closeText();
            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 21 || WindowInfoScr.index[WindowInfoScr.focusTab] == 25) {
                WindowInfoScr.gI().doShowItemInfoImbue();
                return;
            } else if (WindowInfoScr.index[WindowInfoScr.focusTab] == 32) {
                WindowInfoScr.gI().doshowInfoEpdothu();
                return;
            } else {
                WindowInfoScr.gI().center.action.perform();
                return;
            }
        }
        if (Canvas.currentScreen == ScreenDapDo.gI()) {
            ScreenDapDo.gI().setInfoItem(itemMoreInfo.itemID);
        } else if (Canvas.currentScreen == Canvas.shop) {
            Canvas.shop.onItemInfo();
        }
    }

    public void onUseItemPK(short charID, int use, int potionType) {
        Char c = (Char) findCharByID(charID);
        if (c == null) {
            return;
        }
        if (use == 1) {
            c.pk = (byte) potionType;
        } else {
            c.pk = (byte) 0;
        }
    }

    public void onUsePotion(short userId, byte potionType, short valueAdd, int valueNew, int isHP) {
        // pokemon: một nguồn sự thật HP / PP (MauPP) - đặt giá trị tuyệt đối của server, HUD đọc ngay khung sau
        Char c = (Char) findCharByID(userId);
        if (c == null) {
            return;
        }
        int k = game.pokemon.MauPP.apDung22(c, potionType, valueNew, isHP);
        if (k == game.pokemon.MauPP.HP && valueNew > 0) {
            startFlyText(valueAdd > 0 ? "hp+" : new StringBuffer("hp").append((int) valueAdd).toString(), 4, c.x - 10, c.y - 30, 0, -1);
        } else if (k == game.pokemon.MauPP.PP && valueAdd > 0) {
            startFlyText("mp+", 3, c.x - 10, c.y - 40, 0, -1);
        }
    }

    private ItemInInventory getItemByID(Vector list, short id) {
        int size0 = list.size();
        for (int i = 0; i < size0; i++) {
            ItemInInventory item = (ItemInInventory) list.elementAt(i);
            if (item.itemID == id) {
                return item;
            }
        }
        return null;
    }

    public void onPutItem2Bag(short itemID) {
        ItemInInventory item = getItemByID(Char.inventory, itemID);
        if (item != null) {
            Char.inventory.removeElement(item);
            if (this.mainChar.isExistInvector(Char.itembag, item) == -1) {
                Char.itembag.addElement(item);
                return;
            }
            return;
        }
        if (WindowInfoScr.gI() == Canvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 22) {
            WindowInfoScr.gI().setSizeKeepItem(0);
        }
    }

    public void onGetItemFromBag(short itemID) {
        ItemInInventory item = getItemByID(Char.itembag, itemID);
        if (item != null) {
            Char.itembag.removeElement(item);
            if (this.mainChar.isExistInvector(Char.inventory, item) == -1) {
                Char.inventory.addElement(item);
            }
        }
    }

    public void onNPCKeepItem(Vector itemBag) {
        Char.itembag = itemBag;
        showWindow(0, true, new byte[]{22});
    }

    public void onNPCSellPotion(byte[] potionsSell) {
        this.shop.removeAllElements();
        for (byte b : potionsSell) {
            ItemInInventory item = new ItemInInventory();
            item.catagory = (byte) 4;
            item.potionType = b;
            item.isEnoughData = true;
            item.isShopItem = true;
            this.shop.addElement(item);
        }
        if (game.pokemon.MenuNguCanh.bat()) {
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.THUOC, new byte[]{0, 19});   // pokemon 0.9.3: Poké Mart mới
            return;
        }
        showWindow(1, true, new byte[]{0, 19});
    }

    public void onShopSpecial(Vector v) {
        this.shopSpecial = Res.shopTemplate;
        this.shopSpecial.removeAllElements();
        int size0 = Res.shopTemplate.size();
        for (int i = 0; i < size0; i++) {
            GemTemp ge = (GemTemp) Res.shopTemplate.elementAt(i);
            if (ge.isSell) {
                this.shopSpecial.addElement(ge);
            }
        }
    }

    public void onNPCSellGemItem(Vector gem) {
        this.shop.removeAllElements();
        int size0 = Res.gemTemplate.size();
        for (int i = 0; i < size0; i++) {
            GemTemp ge = (GemTemp) Res.gemTemplate.elementAt(i);
            if (ge.isSell) {
                this.shop.addElement(ge);
            }
        }
        showWindow(1, true, new byte[]{0, 10});
    }

    public void onNPCSellItem(Vector itemsSell) {
        this.shop = itemsSell;
        ItemInInventory item = (ItemInInventory) itemsSell.elementAt(0);
        ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
        byte[] cu = (it.type > 2 && it.type < 8) || (it.type >= 14 && it.type <= 18) ? new byte[]{0, 9}
                : new byte[]{11, 12, 13, 14, 15, 16, 17, 18, 0};
        if (game.pokemon.MenuNguCanh.bat()) {
            game.pokemon.ManCuaHang.mo(game.pokemon.ManCuaHang.DO, cu);   // pokemon 0.9.3: cửa hàng mới
            return;
        }
        WindowInfoScr.gI().setInfo(cu.length == 2 ? 1 : 0, true, cu);
        WindowInfoScr.gI().switchToMe();
    }

    public void onChat(short chatFromID, String text) {
        // 0.9.5.1: máy chủ gửi thông báo trạng thái cho CHÍNH mình bằng tin chat (tự động đánh dừng, không dùng được...):
        // hiện thành thông báo nhanh nhỏ ở giữa trên (DungDo), không phải bong bóng chữ to theo thế giới (bị cắt mép).
        // Tin mình vừa gõ máy chủ gửi lại (bị cấm chat...) thì bỏ - client đã tự hiện bong bóng.
        if (this.mainChar != null && chatFromID == this.mainChar.ID && text != null) {
            if (!text.equals(this.tinVuaGo) || System.currentTimeMillis() - this.lucVuaGo > 5000L) {
                game.pokemon.DungDo.bao(text, game.pokemon.DungDo.TIN);
            }
            return;
        }
        Actor a = findActorByID(chatFromID);
        if (a != null) {
            a.chat = null;
            if (a instanceof Char) {
                if (((Char) a).idBoss != -1) {
                    addChat(a, text, 500);
                    return;
                } else {
                    addChat(a, text, 50);
                    MessageScr.gI().addTab(new StringBuffer(String.valueOf(((Char) a).name)).append(": ").append(text).toString(), null, 0);
                    return;
                }
            }
            addChat(a, text, 50);
        }
    }

    public void onSellItem(short itemID) {
        ItemInInventory item = getItemByID(Char.inventory, itemID);
        if (item != null) {
            // pokemon: server đã bán (lệnh 28 = xác nhận). Xu + thông báo "Đã bán X (+giá)" do server gửi ngay sau
            // (túi 16 / -105 / -104) - trước client tự cộng price/5 (sai giá thật) và hộp "Đã bán thành công." chung
            Char.inventory.removeElement(item);
            game.pokemon.TrangThaiNguoiChoi.doi(game.pokemon.TrangThaiNguoiChoi.TUI);
        }
    }

    public void onSellGemItem(short itemID) {
        GemTemplate gem = GemTemplate.getGemByRealID(itemID);
        if (gem != null) {
            gem.number--;
            if (gem.number <= 0) {
                MainChar.gemItem.removeElement(gem);
                game.pokemon.TrangThaiNguoiChoi.doi(game.pokemon.TrangThaiNguoiChoi.TUI);   // pokemon: xu + thông báo do server
            }
        }
    }

    public void onGiveItemToGround(short charID, short itemID, ItemDropInfo mdi) {
        ItemInInventory item = getItemByID(Char.inventory, itemID);
        if (item != null) {
            Char.inventory.removeElement(item);
            Dropable dropable = (Dropable) CreateActor(mdi.itemCatagory, mdi.itemTemplateID, mdi.itemID, mdi.x, mdi.y, (byte) 0, (byte) -1);
            dropable.startDropFrom(this.mainChar.x, this.mainChar.y, dropable.x, dropable.y);
            this.actors.addElement(dropable);
            if (this.mainChar.ID == charID) {
                Canvas.startOKDlg("Đã bỏ vật phẩm ra đất.");
                return;
            }
            return;
        }
        Dropable dropable2 = (Dropable) CreateActor(mdi.itemCatagory, mdi.itemTemplateID, mdi.itemID, mdi.x, mdi.y, (byte) 0, (byte) -1);
        dropable2.startDropFrom(this.mainChar.x, this.mainChar.y, dropable2.x, dropable2.y);
        this.actors.addElement(dropable2);
    }

    public void onSetXP(short whoSetXP, short newpercent, int dxp) {
        Char c = (Char) findCharByID(whoSetXP);
        if (c != null) {
            c.lvpercent = newpercent;
            startFlyText(new StringBuffer("+").append(dxp).append("xp").toString(), 2, c.x, c.y - 30, -1, -1);
        }
    }

    public void onCharProperties(short id, Property[] ps) {
        Char c = (Char) findCharByID(id);
        if (c != null) {
            for (Property property : ps) {
                c.setProperty(property);
            }
        }
    }

    public void onLevelUp(short idc, byte newlevel, int maxHP, int maxMP) {
        Char c = (Char) findCharByID(idc);
        if (c == null) {
            return;
        }
        c.level = newlevel;
        c.lockUpdate = 30;
        c.dir = (short) 0;
        c.maxhp = maxHP;
        c.maxmp = maxMP;
        c.hp = maxHP;
        c.mp = maxMP;
        c.realHP = c.hp;
        startFlyText("level-up", 3, c.x, c.y - 30, -1, 1);
        startFlyText("level-up", 3, c.x, c.y - 30, 1, -1);
        startFlyText("level-up", 3, c.x, c.y - 30, -1, -1);
        startFlyText("level-up", 3, c.x, c.y - 30, 1, 1);
        if (c != null) {
            EffectManager.addHiEffect(c.x, c.y - 30, 18);
            game.pokemon.HieuUngDanh.lenCap(c);   // pokemon: no sang + tieng len cap
        }
        if (c == this.mainChar) {
            addChat(new ChatInfo("", new StringBuffer("Đạt được level ").append((int) newlevel).toString(), 1));
            if (newlevel == 2) {
                Canvas.startOKDlg("Xin chúc mừng! Bạn vừa lên cấp. Chọn menu, Pokémon để tăng điểm tiềm năng và kỹ năng", false, 20);
            }
        }
    }

    public void onRemoveActor(short actorID, byte cat) {
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor a = (Actor) this.actors.elementAt(i);
            if (a.catagory == cat && a.ID == actorID) {
                this.actors.removeElement(a);
                if (cat == 0) {
                    game.pokemon.PetDuyNhat.bo(this.actors, actorID, null);   // pokemon: người rời cảnh -> gỡ pet
                }
                return;
            }
        }
    }

    public void onCreadtePTOK(short idParty) {
        this.mainChar.IDParty = idParty;
        this.mainChar.IDMasterParty = this.mainChar.ID;
        if (tempIDActorParty != -1) {
            this.gameService.invite2Party(tempIDActorParty, 0);
            tempIDActorParty = -1;
        }
    }

    public void onAdd2Party(final Message m) {
        try {
            byte value = m.reader().readByte();
            if (value == 0) {
                final short partyID = m.reader().readShort();
                final short idInvite = m.reader().readShort();
                final String charName = m.reader().readUTF();
                final int charLV = m.reader().readByte();
                m.reader().readShort();
                final Message msg = new Message((byte) 49);
                if (autoFight && Canvas.autoTab.isAutoParty && !this.onoffInvite) {
                    try {
                        msg.writer().writeByte(1);
                        msg.writer().writeShort(idInvite);
                        this.gameService.session.sendMessage(msg);
                        msg.cleanup();
                        this.mainChar.add2Party(new PartyInfo(idInvite, charName, charLV, m.reader().readByte()));
                        this.mainChar.IDMasterParty = idInvite;
                        this.mainChar.IDParty = partyID;
                        while (m.reader().available() > 0) {
                            PartyInfo infoa = new PartyInfo(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
                            this.mainChar.add2Party(infoa);
                        }
                        addChat(new ChatInfo("", "Đã tham gia nhóm", 0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Canvas.currentDialog = null;
                    return;
                }
                Canvas.startYesNoDlg(new StringBuffer(String.valueOf(charName)).append(" mời bạn tham gia nhóm.").toString(), new IAction() {
                    @Override
                    public void perform() {
                        try {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(idInvite);
                            GameScr.this.gameService.session.sendMessage(msg);
                            msg.cleanup();
                            PartyInfo info = new PartyInfo(idInvite, charName, charLV, m.reader().readByte());
                            GameScr.this.mainChar.add2Party(info);
                            GameScr.this.mainChar.IDMasterParty = idInvite;
                            GameScr.this.mainChar.IDParty = partyID;
                            int size0 = m.reader().available();
                            while (size0 > 0) {
                                PartyInfo infoa2 = new PartyInfo(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
                                GameScr.this.mainChar.add2Party(infoa2);
                            }
                            GameScr.this.addChat(new ChatInfo("", "Đã tham gia nhóm", 0));
                        } catch (Exception e2) {
                            Cout.println("LOI KHI GUI OK VAO PARTY LEN SERVER");
                            e2.printStackTrace();
                        }
                        Canvas.currentDialog = null;
                    }
                }, new IAction() {
                    @Override
                    public void perform() {
                        try {
                            msg.writer().writeByte(2);
                            msg.writer().writeShort(idInvite);
                            GameScr.this.gameService.session.sendMessage(msg);
                            msg.cleanup();
                        } catch (Exception e2) {
                        }
                        Canvas.currentDialog = null;
                    }
                });
                return;
            }
            if (value == 1) {
                short idInvite2 = m.reader().readShort();
                String charName2 = m.reader().readUTF();
                int charLV2 = m.reader().readByte();
                if (Char.party.size() <= 0) {
                    this.mainChar.IDMasterParty = this.mainChar.ID;
                }
                this.mainChar.add2Party(new PartyInfo(idInvite2, charName2, charLV2, m.reader().readByte()));
                addChat(new ChatInfo("", new StringBuffer(String.valueOf(charName2)).append(" đã tham gia nhóm.").toString(), 0));
                return;
            }
            if (value == 2) {
                Canvas.startOKDlg(new StringBuffer(String.valueOf(m.reader().readUTF())).append(" từ chối vào nhóm.").toString());
                return;
            }
            if (value == 3) {
                int size1 = m.reader().available();
                while (size1 > 0) {
                    PartyInfo info = new PartyInfo(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
                    addChat(new ChatInfo("", new StringBuffer(String.valueOf(info.name)).append(" đã vào nhóm.").toString(), 0));
                    this.mainChar.add2Party(info);
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Cout.println("LOI TRONG ONADD PARTY");
        }
    }

    public void onKickOutParty(Message m) {
        try {
            byte value = m.reader().readByte();
            if (value == 0 || value == 2) {
                short idChar = m.reader().readShort();
                if (idChar == this.mainChar.ID) {
                    this.mainChar.IDParty = (short) -1;
                    this.mainChar.IDMasterParty = (short) -1;
                    Char.party.removeAllElements();
                    if (value == 0) {
                        addChat(new ChatInfo("", "Bị đuổi khỏi nhóm", 0));
                        return;
                    } else {
                        addChat(new ChatInfo("", "Rời khỏi nhóm", 0));
                        return;
                    }
                }
                String name = this.mainChar.removeUserParty(idChar);
                if (!name.equals("")) {
                    if (value == 0) {
                        addChat(new ChatInfo("", new StringBuffer(String.valueOf(name)).append(" bị đuổi khỏi nhóm").toString(), 0));
                    } else {
                        addChat(new ChatInfo("", new StringBuffer(String.valueOf(name)).append(" rời khỏi nhóm").toString(), 0));
                    }
                }
                if (Char.party.size() <= 0) {
                    Char.party.removeAllElements();
                    this.mainChar.IDParty = (short) -1;
                    this.mainChar.IDMasterParty = (short) -1;
                    addChat(new ChatInfo("", "Nhóm đã bị giải tán", 0));
                    return;
                }
                return;
            }
            if (value == 1) {
                this.mainChar.IDParty = (short) -1;
                this.mainChar.IDMasterParty = (short) -1;
                Char.party.removeAllElements();
                addChat(new ChatInfo("", "Nhóm đã bị giải tán", 0));
            }
        } catch (Exception e) {
        }
    }

    public void onCharUseBuff(Message m) {
        try {
            int use = m.reader().readByte();
            int charID = m.reader().readShort();
            if (use == 2) {
                for (int i = 0; i < this.actors.size(); i++) {
                    Actor ch = (Actor) this.actors.elementAt(i);
                    if (ch.catagory == 0 && ch.ID == charID) {
                        m.reader().read(((Char) ch).buffType, 0, ((Char) ch).buffType.length);
                        for (int j = 0; j < ((Char) ch).buffType.length; j++) {
                            ((Char) ch).countDown[j] = m.reader().readShort();
                        }
                        ((Char) ch).setBuff(((Char) ch).buffType, ((Char) ch).countDown);
                        m.reader().read(((Char) ch).lvBuff, 0, ((Char) ch).lvBuff.length);
                        return;
                    }
                }
                return;
            }
            int buffID = m.reader().readByte();
            int timeOut = m.reader().readShort();
            if (use == 1) {
                int lvSkill = m.reader().readByte();
                int lvid = 0;
                int size0 = Char.BUFFTYPE.length;
                while (lvid < size0 && Char.BUFFTYPE[lvid] != buffID) {
                    lvid++;
                }
                if (charID == this.mainChar.ID) {
                    if (!this.mainChar.isExistBuff(buffID)) {
                        CharBuff cb = new CharBuff(this.mainChar.x, this.mainChar.y, buffID);
                        cb.setTimeLive(timeOut);
                        timeBuff = cb.tick;
                        this.isBuffAuto = true;
                        this.mainChar.lvBuff[lvid] = (byte) lvSkill;
                        this.mainChar.buffType[lvid] = (byte) buffID;
                        this.mainChar.buff.addElement(cb);
                        // pokemon (K3): particle chieu bo tro tren nguoi Pokemon
                        game.pokemon.PokeHieuUng.khiNhanBuff(this.mainChar, buffID);
                        return;
                    }
                    return;
                }
                int size1 = this.actors.size();
                for (int i2 = 0; i2 < size1; i2++) {
                    Actor ch2 = (Actor) this.actors.elementAt(i2);
                    if (ch2.catagory == 0 && ch2.ID == charID) {
                        if (!((Char) ch2).isExistBuff(buffID)) {
                            CharBuff cb2 = new CharBuff(ch2.x, ch2.y, buffID);
                            cb2.setTimeLive(timeOut);
                            ((Char) ch2).lvBuff[lvid] = (byte) lvSkill;
                            ((Char) ch2).buffType[lvid] = (byte) buffID;
                            ((Char) ch2).buff.addElement(cb2);
                            game.pokemon.PokeHieuUng.khiNhanBuff(ch2, buffID);
                            return;
                        }
                        return;
                    }
                }
                return;
            }
            if (use == 0) {
                int size2 = this.actors.size();
                for (int i3 = 0; i3 < size2; i3++) {
                    Actor ch3 = (Actor) this.actors.elementAt(i3);
                    if (ch3.catagory == 0 && ch3.ID == charID) {
                        ((Char) ch3).removeBuff(buffID);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println("LOI KHI NHAN MSG USER BUFF TRONG ONCHARUSEBUFF ");
        }
    }

    public void onInfoQuestLogin(Message m) {
    }

    public void onInfoGiftQuest(String str) {
        addChat(new ChatInfo("", str, 0));
    }

    public void receiveQuest(Message m) {
        Canvas.endDlg();
    }

    public void onFinishQuest(Message m) {
    }

    public void onNewHPMP(Message msg) {
        try {
            // pokemon: MauPP đọc cả PP ở đuôi gói (server 0.9.3) - HP / max / PP của mình và người khác
            int[] v = game.pokemon.MauPP.doc60(msg.reader());
            int id = v[0];
            if (id == this.mainChar.ID) {
                game.pokemon.MauPP.apDung60(this.mainChar, v);
                if (this.mainChar.state == 3 && this.mainChar.hp > 0) {
                    this.mainChar.state = (byte) 0;
                    addChat(new ChatInfo("", "Đã được hồi sinh.", 0));
                    return;
                }
                return;
            }
            int size0 = this.actors.size();
            for (int i = 0; i < size0; i++) {
                Actor ac = (Actor) this.actors.elementAt(i);
                if (ac.catagory == 0 && ac.ID == id) {
                    game.pokemon.MauPP.apDung60((Char) ac, v);
                    if (((Char) ac).state == 3 && ((Char) ac).hp > 0) {
                        ((Char) ac).state = (byte) 0;
                        addChat(new ChatInfo("", new StringBuffer(String.valueOf(((Char) ac).name)).append(" đã được hồi sinh.").toString(), 0));
                        return;
                    }
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    public void onKiller(int idChar, int check, short pkPoint) {
        if (this.mainChar.ID == idChar) {
            this.mainChar.iskiller = check == 1;
            this.mainChar.killer = pkPoint;
            return;
        }
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac.catagory == 0 && ac.ID == idChar) {
                ((Char) ac).iskiller = check == 1;
                ac.killer = pkPoint;
                return;
            }
        }
    }

    public void onRequestTrade(final short userid) {
        Char p = (Char) findCharByID(userid);
        if (p != null) {
            Canvas.startYesNoDlg(new StringBuffer(String.valueOf(p.name)).append(" đề nghị giao dịch với bạn.").toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendOk_notOkInviteTrafe(userid, 1);
                    Canvas.currentDialog = null;
                }
            }, new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendOk_notOkInviteTrafe(userid, -1);
                    Canvas.currentDialog = null;
                }
            });
        }
    }

    public void onOkTrade(short userID) {
        Char p = (Char) findCharByID(userID);
        String st = "Bắt đầu giao dịch";
        if (userID != this.mainChar.ID && p != null) {
            st = new StringBuffer(String.valueOf(p.name)).append(" đồng ý giao dịch").toString();
        }
        addChat(new ChatInfo("", st, 0));
        cancelTrade();
        if (game.pokemon.MenuNguCanh.bat()) {
            game.pokemon.ManGiaoDich.mo();   // pokemon 0.9.3: giao dịch mới
            return;
        }
        WindowInfoScr.gI().setInfo(0, true, new byte[]{8});
        WindowInfoScr.gI().setSize(108, 90, 6, 6, 18, 18);
        WindowInfoScr.gI().switchToMe();
    }

    public void onDeniedTrade(short userid) {
        Char p = (Char) findCharByID(userid);
        String st = "";
        if (p != null) {
            st = p.name;
        }
        addChat(new ChatInfo("", new StringBuffer(String.valueOf(st)).append(" không đồng ý giao dịch").toString(), 0));
        cancelTrade();
        game.pokemon.ManGiaoDich.ket();
    }

    public void onItemTrade(short userIdTrade, Message msg, byte type) {
        try {
            if (type == 0) {
                ItemInInventory cii = new ItemInInventory();
                byte b = msg.reader().readByte();
                cii.clazz = b;
                cii.charClazz = b;
                cii.itemID = msg.reader().readShort();
                cii.itemOldID = cii.itemID;
                cii.idTemplate = msg.reader().readShort();
                cii.plusTemplate = msg.reader().readByte();
                cii.level = msg.reader().readByte();
                cii.durable = msg.reader().readShort();
                cii.mDurable = msg.reader().readShort();
                byte len4 = msg.reader().readByte();
                for (byte i = 0; i < len4; i = (byte) (i + 1)) {
                    InfoAttributeItem info = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                    cii.allAttribute.addElement(info);
                }
                cii.colorName = msg.reader().readByte();
                cii.heItem = msg.reader().readByte();
                cii.hangItem = msg.reader().readByte();
                cii.magic_physic = msg.reader().readByte();
                cii.isEnoughData = true;
                if (userIdTrade == this.mainChar.ID) {
                    int id = this.mainChar.isExistInvector(this.mainChar.tItems, cii);
                    if (id == -1) {
                        this.mainChar.tItems.addElement(cii);
                        return;
                    } else {
                        this.mainChar.tItems.removeElementAt(id);
                        return;
                    }
                }
                if (((Char) findCharByID(userIdTrade)) != null) {
                    int id2 = this.mainChar.isExistInvector(this.mainChar.rItems, cii);
                    if (id2 == -1) {
                        this.mainChar.rItems.addElement(cii);
                        return;
                    } else {
                        this.mainChar.rItems.removeElementAt(id2);
                        return;
                    }
                }
                return;
            }
            if (type == 1) {
                Potion p = new Potion();
                p.id = (short) msg.reader().readUnsignedByte();
                p.index = MainChar.listPotion[p.id].index;
                p.number = msg.reader().readShort();
                if (userIdTrade == this.mainChar.ID) {
                    MainChar.listPotion[p.id].numTrade += p.number;
                    addPotionTrade(this.mainChar.tItems, p);
                    return;
                }
                addPotionTrade(this.mainChar.rItems, p);
                return;
            }
            if (type == 2) {
                int id3 = msg.reader().readUnsignedByte();
                if (userIdTrade == this.mainChar.ID) {
                    MainChar.listPotion[id3].numTrade = 0;
                    removePotionTrade(this.mainChar.tItems, id3);
                } else {
                    removePotionTrade(this.mainChar.rItems, id3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPotionTrade(Vector list, Potion pa) {
        int size0 = list.size();
        for (int i = 0; i < size0; i++) {
            if (list.elementAt(i) instanceof Potion) {
                Potion p = (Potion) list.elementAt(i);
                if (p.index == MainChar.listPotion[pa.id].index) {
                    p.number += pa.number;
                    return;
                }
            }
        }
        list.addElement(pa);
    }

    private void removePotionTrade(Vector list, int id) {
        int size0 = list.size();
        for (int i = 0; i < size0; i++) {
            if (list.elementAt(i) instanceof Potion) {
                Potion p = (Potion) list.elementAt(i);
                if (p.index == MainChar.listPotion[id].index) {
                    list.removeElement(p);
                    return;
                }
            }
        }
    }

    public void onConfirmTrade() {
        game.pokemon.ManGiaoDich.haiBenKhoa();
        if (WindowInfoScr.index[WindowInfoScr.focusTab] == 8) {
            WindowInfoScr.gI().left = new Command("Chuyển", new IAction() {
                @Override
                public void perform() {
                    Canvas.startYesNoDlg("Bạn thật sự muốn trao đổi", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().sendConfirmTrade();
                            Canvas.gameScr.mainChar.isTrade = false;
                            Canvas.endDlg();
                            WindowInfoScr.gI().left = new Command("", new IAction() {
                                @Override
                                public void perform() {
                                }
                            });
                            WindowInfoScr.gI().center = new Command("Xin chờ", new IAction() {
                                @Override
                                public void perform() {
                                }
                            });
                        }
                    }, new IAction() {
                        @Override
                        public void perform() {
                            Canvas.endDlg();
                        }
                    });
                }
            });
        }
    }

    public void onFinishTrade() {
        cancelTrade();
        game.pokemon.ManGiaoDich.ket();
        Canvas.startOKDlg("Giao dịch hoàn thành", false);
    }

    public void oncancelTrade() {
        cancelTrade();
        game.pokemon.ManGiaoDich.ket();
        Canvas.startOKDlg("Giao dịch bị hủy.");
    }

    public void creatWeaponFire(LiveActor live_From, LiveActor live_To, int type) {
        WeaponsFire w = new WeaponsFire(live_From, live_To, type);
        if (live_From.dir != 1) {
            arrowsUp.addElement(w);
        } else {
            this.arrowsDown.addElement(w);
        }
    }

    private void cancelTrade() {
        this.mainChar.isTrade = false;
        this.mainChar.tItems.removeAllElements();
        this.mainChar.rItems.removeAllElements();
        if (MainChar.listPotion != null) {
            for (int i = 0; i < MainChar.listPotion.length; i++) {
                MainChar.listPotion[i].numTrade = 0;
            }
        }
        if (Canvas.currentScreen == WindowInfoScr.gI() && WindowInfoScr.index[WindowInfoScr.focusTab] == 8) {
            switchToMe();
        }
    }

    public static Vector setListGemInbue(Vector listGem) {
        Vector vtam = new Vector();
        for (int i = 0; i < listGem.size(); i++) {
            GemTemplate gem = (GemTemplate) listGem.elementAt(i);
            GemTemp gg = Res.getGemByID(gem.id);
            if (gg.type == 0) {
                GemTemplate g = new GemTemplate(gem.id);
                GemTemplate.copyData(gem, g);
                vtam.addElement(g);
            }
        }
        return vtam;
    }

    public void onImbue(short priceImbue, byte khoa) {
        WindowInfoScr.gI().prizeImbue = priceImbue;
        Vector vtam = new Vector();
        WindowInfoScr.khoaImbue = khoa;
        if (khoa == 0) {
            vtam = setListGemInbue(MainChar.gemItem);
        } else if (khoa == 1) {
            vtam = setListGemInbue(MainChar.gemItemKhoa);
        } else {
            int aa = MainChar.gemItem.size();
            int bb = MainChar.gemItemKhoa.size();
            for (int i = 0; i < aa; i++) {
                GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemp gg = Res.getGemByID(gem.id);
                if (gg.type == 0) {
                    GemTemplate g = new GemTemplate(gem.id);
                    GemTemplate.copyData(gem, g);
                    vtam.addElement(g);
                }
            }
            for (int i2 = 0; i2 < bb; i2++) {
                GemTemplate gemKhoa = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean tonTai = false;
                for (int j = 0; j < vtam.size(); j++) {
                    GemTemplate g2 = (GemTemplate) vtam.elementAt(j);
                    if (g2.id == gemKhoa.id) {
                        g2.number += gemKhoa.number;
                        tonTai = true;
                    }
                }
                if (!tonTai) {
                    GemTemp gg2 = Res.getGemByID(gemKhoa.id);
                    if (gg2.type == 0) {
                        GemTemplate gk = new GemTemplate(gemKhoa.id);
                        GemTemplate.copyData(gemKhoa, gk);
                        vtam.addElement(gk);
                    }
                }
            }
        }
        for (int a = 0; a < vtam.size(); a++) {
            WindowInfoScr.gI().listTemp.addElement((GemTemplate) vtam.elementAt(a));
        }
        showWindow(0, false, new byte[]{21});
        WindowInfoScr.gI().name = "Luyện đồ";
    }

    public void onKham(short price1, byte khoa) {
        WindowInfoScr.gI().prizeImbue = price1;
        Vector vtam = new Vector();
        WindowInfoScr.khoaKham = khoa;
        if (khoa == 0) {
            vtam = MainChar.gemItem;
        } else if (khoa == 1) {
            vtam = MainChar.gemItemKhoa;
        } else {
            int aa = MainChar.gemItem.size();
            int bb = MainChar.gemItemKhoa.size();
            for (int i = 0; i < aa; i++) {
                GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemplate g = new GemTemplate(gem.id);
                GemTemplate.copyData(gem, g);
                vtam.addElement(g);
            }
            for (int i2 = 0; i2 < bb; i2++) {
                GemTemplate gemKhoa = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean tonTai = false;
                for (int j = 0; j < vtam.size(); j++) {
                    GemTemplate g2 = (GemTemplate) vtam.elementAt(j);
                    if (g2.id == gemKhoa.id) {
                        g2.number += gemKhoa.number;
                        tonTai = true;
                    }
                }
                if (!tonTai) {
                    GemTemplate gk = new GemTemplate(gemKhoa.id);
                    GemTemplate.copyData(gemKhoa, gk);
                    vtam.addElement(gk);
                }
            }
        }
        int size0 = vtam.size();
        for (int a = 0; a < size0; a++) {
            WindowInfoScr.gI().listTemp.addElement((GemTemplate) vtam.elementAt(a));
        }
        showWindow(0, false, new byte[]{25});
    }

    public void onEpDothu(short price, byte idColor, byte lv, short startIDNL, byte slNguyenLieu, byte magic, byte khoa) {
        WindowInfoScr.gI().listTemp.removeAllElements();
        WindowInfoScr.gI().prizeImbue = price;
        WindowInfoScr.idColor = idColor;
        WindowInfoScr.lvThu = lv;
        WindowInfoScr.gI();
        WindowInfoScr.slNLThu = slNguyenLieu;
        WindowInfoScr.magicAnimal = magic;
        Vector vtam = new Vector();
        WindowInfoScr.khoaDoThu = khoa;
        if (khoa == 0) {
            vtam = MainChar.gemItem;
        } else if (khoa == 1) {
            vtam = MainChar.gemItemKhoa;
        } else {
            int aa = MainChar.gemItem.size();
            int bb = MainChar.gemItemKhoa.size();
            for (int i = 0; i < aa; i++) {
                GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemplate g = new GemTemplate(gem.id);
                GemTemplate.copyData(gem, g);
                vtam.addElement(g);
            }
            for (int i2 = 0; i2 < bb; i2++) {
                GemTemplate gemKhoa = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean tonTai = false;
                for (int j = 0; j < vtam.size(); j++) {
                    GemTemplate g2 = (GemTemplate) vtam.elementAt(j);
                    if (g2.id == gemKhoa.id) {
                        g2.number += gemKhoa.number;
                        tonTai = true;
                    }
                }
                if (!tonTai) {
                    GemTemplate gk = new GemTemplate(gemKhoa.id);
                    GemTemplate.copyData(gemKhoa, gk);
                    vtam.addElement(gk);
                }
            }
        }
        int size0 = vtam.size();
        for (int a = 0; a < size0; a++) {
            GemTemplate gem2 = (GemTemplate) vtam.elementAt(a);
            if (gem2.id >= startIDNL && gem2.id <= startIDNL + 5) {
                WindowInfoScr.gI().listTemp.addElement(gem2);
            }
        }
        if (WindowInfoScr.gI().listTemp.size() == 0) {
            Canvas.startOKDlg("Không có nguyên liệu.");
            return;
        }
        showWindow(0, false, new byte[]{32});
        WindowInfoScr.gI().name = new StringBuffer("Đồ ").append(magic == 0 ? "ma" : "vật").append(" ").append((int) lv).toString();
        Canvas.endDlg();
    }

    public void onEpNgoc(short price) {
        WindowInfoScr.gI().prizeImbue = price;
        showWindow(0, false, new byte[]{26});
        WindowInfoScr.gI().name = "Hợp thành";
        Canvas.endDlg();
    }

    public void onSpecialItem(Message msg) {
        MainChar.shopItem.removeAllElements();
        try {
            int len = msg.reader().readByte();
            for (int i = 0; i < len; i++) {
                GemTemplate gem = new GemTemplate();
                gem.rID = msg.reader().readShort();
                gem.id = msg.reader().readByte();
                gem.number = msg.reader().readShort();
                MainChar.shopItem.addElement(gem);
            }
        } catch (Exception e) {
            Cout.println("LOI NHAN SPECIAL_ITEM");
        }
        WindowInfoScr.gI().setCurTab();
    }

    public void onGemItem(Message msg) throws IOException {
        Vector vtam = new Vector();
        vtam.removeAllElements();
        int len = msg.reader().readShort();
        for (int i = 0; i < len; i++) {
            GemTemplate gem = new GemTemplate();
            gem.rID = msg.reader().readShort();
            gem.id = msg.reader().readShort();
            gem.number = msg.reader().readShort();
            vtam.addElement(gem);
        }
        byte khoa = 0;
        try {
            khoa = msg.reader().readByte();
        } catch (Exception e) {
        }
        if (khoa == 0) {
            MainChar.gemItem = vtam;
        } else {
            MainChar.gemItemKhoa = vtam;
        }
        if (Canvas.currentScreen == WindowInfoScr.gI()) {
            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 26) {
                WindowInfoScr.gI().setListImbue(true, 1, khoa);
            }
            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 0) {
                PauseMenu.gI().showMe(0);
            }
        }
    }

    public void onAddItemImbue(short id) {
        this.mainChar.itemImbue = null;
        int size0 = Char.inventory.size();
        for (int i = 0; i < size0; i++) {
            ItemInInventory item = (ItemInInventory) Char.inventory.elementAt(i);
            ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
            if (it.id == id) {
                this.mainChar.itemImbue = item;
                return;
            }
        }
    }

    public void onImbueOK(String info) {
        Canvas.startOKDlg(info, false);
        WindowInfoScr.gI().resetImbue();
    }

    public void onActorDie(Message msg) {
        try {
            int actorID = msg.reader().readShort();
            int cat = msg.reader().readByte();
            int size0 = this.actors.size();
            for (int i = 0; i < size0; i++) {
                Actor ac = (Actor) this.actors.elementAt(i);
                if (ac.ID == actorID) {
                    if (cat == 1) {
                        int dx = (ac.x - 5) * 2;
                        int dy = (ac.y - 5) * 2;
                        while (true) {
                            if (dx > 10 || dy > 10 || dx < -10 || dy < -10) {
                                dx >>= 1;
                                dy >>= 1;
                            } else {
                                ((Monster) ac).startDeadFly(dx, dy);
                                ac.die = true;
                                return;
                            }
                        }
                    } else {
                        if (cat == 0) {
                            ac.actorDie();
                            if (ac.ID == this.mainChar.ID) {
                                game.pokemon.HopHoiSinh.khiGuc(); // pokemon: xin hop thoai guc (-101)
                            }
                            if (ac.ID == this.mainChar.ID && BangAuto.isAutoComeHome && autoFight) {
                                Canvas.currentDialog = null;
                                doFire(0, 0);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Cout.println("LOI TRONG HAM ONACTORDIE TRONG GAMESCR");
        }
    }

    public void onBuffAttack(Message m) {
        try {
            int charID = m.reader().readShort();
            int cat = m.reader().readByte();
            int delay = m.reader().readByte();
            int dam = m.reader().readShort();
            int skillid = m.reader().readByte();
            int clazz = m.reader().readByte();
            int time = -1;
            try {
                time = m.reader().readByte();
            } catch (Exception e) {
            }
            int size0 = this.actors.size();
            for (int i = 0; i < size0; i++) {
                Actor ac = (Actor) this.actors.elementAt(i);
                if (ac.ID == charID) {
                    switch (clazz) {
                        case -1:
                            game.pokemon.HieuUngDanh.biBong(ac);   // pokemon: bieu tuong bong
                            startFlyText(new StringBuffer("-").append(dam).toString(), 1, ac.x, ac.y - 40, 0, -1);
                            if (cat == 1) {
                                ((Monster) ac).setHp(dam);
                                if (((Monster) ac).hp <= 0) {
                                    ((Monster) ac).wantDestroy = true;
                                }
                            } else {
                                ((Char) ac).hp -= dam;
                                if (((Char) ac).hp <= 0) {
                                    ((Char) ac).state = (byte) 3;
                                }
                            }
                            break;
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            if (skillid == 7 && dam > 0) {
                                startFlyText(new StringBuffer("-").append(dam).toString(), 3, ac.x, ac.y - 40, 0, -1);
                                ((Char) ac).mp -= dam;
                                if (((Char) ac).mp <= 0) {
                                    ((Char) ac).mp = 0;
                                }
                                break;
                            }
                            break;
                        case 3:
                            if (skillid == 4) {
                                ac.beStune = true;
                                ac.timeBeStune = System.currentTimeMillis() + ((long) (time > 0 ? time * 1000 : 3000));
                                CharBuff cb = new CharBuff(ac.x, ac.y, 19);
                                cb.setTimeLive(time > 0 ? time : 3);
                                ac.addBuff(cb);
                            }
                            break;
                        case 4:
                            if (skillid == 4) {
                                CharBuff cb2 = new CharBuff(ac.x, ac.y, 22);
                                cb2.setTimeLive(time > 0 ? time : 36);
                                ac.addBuff(cb2);
                                ac.timeOutPoinson = System.currentTimeMillis();
                                ac.poinson = (short) dam;
                                ac.tDelay = (short) delay;
                            }
                            break;
                    }
                    return;
                }
            }
        } catch (Exception e2) {
        }
    }

    public void onListShopOfNPC(final byte[] idShop) {
        Canvas.currentDialog = null;
        Vector menu = new Vector();
        for (int i = 0; i < 3; i++) {
            final int ii = i;
            menu.addElement(new Command(new StringBuffer("Gian hàng ").append(i + 1).toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.this.doActionShop(ii, idShop[idShop.length - 1], idShop[idShop.length - 2]);
                }
            }));
        }
        Canvas.menu.startAt(menu, 3);
    }

    public void doActionShop(int shopID, int type, int ncpType) {
        if (type == 0) {
            WindowInfoScr.gI().idSeller = ncpType;
            WindowInfoScr.gI().shopIdSell = shopID;
            Canvas.startWaitDlg();
            this.gameService.requestGetListItem(ncpType, shopID, this.mainChar.ID);
            return;
        }
        if (type == 1) {
            this.gameService.requestGetListUser(ncpType, shopID);
            Canvas.startWaitDlg();
        }
    }

    public void onFriendList(Message msg, int type, String title) {
        try {
            Vector list = new Vector();
            short num = msg.reader().readShort();
            for (int i = 0; i < num; i++) {
                Char ch = new Char();
                ch.name = msg.reader().readUTF();
                ch.currentHead = msg.reader().readByte();
                ch.dir = (short) 0;
                ch.level = msg.reader().readByte();
                ch.weapon_frame = (short) 0;
                byte numItem = msg.reader().readByte();
                Vector we = new Vector();
                for (int a = 0; a < numItem; a++) {
                    ItemInInventory item = new ItemInInventory();
                    byte b = msg.reader().readByte();
                    item.charClazz = b;
                    ch.clazz = b;
                    item.idTemplate = msg.reader().readShort();
                    item.level = msg.reader().readByte();
                    item.plusTemplate = msg.reader().readByte();
                    we.addElement(item);
                }
                ch.idClan = msg.reader().readShort();
                ch.isMaster = msg.reader().readByte();
                if (type == 3 || type == 4 || type == 7 || type == 8) {
                    ch.charXu = msg.reader().readLong();
                    ch.luong = msg.reader().readInt();
                    ch.nationID = msg.reader().readByte();
                    if (type == 7) {
                        ch.congTrang = msg.reader().readInt();
                        ch.lienTram = msg.reader().readInt();
                    }
                }
                ch.setWearingInfo(we);
                list.addElement(ch);
            }
            if (game.pokemon.ManXepHang.nhan(type, list)) {
                Canvas.endDlg();
                return;   // pokemon 0.9.3: màn Xếp hạng đang chờ kiểu này
            }
            if (list.size() > 0) {
                if (type == 0) {
                    ListScr.gI().freindList = list;
                    return;
                }
                if (type == 1 || type == 3 || type == 4 || type == 6 || type == 7 || type == 8) {
                    ListScr.gI().setInfo(list, type, title);
                    ListScr.gI().switchToMe();
                    Canvas.endDlg();
                }
            }
        } catch (Exception e) {
            Cout.println(new StringBuffer("loi onFriendList >> ").append(e.toString()).toString());
        }
    }

    public void onListInfoDepositeItem(Message msg) {
        try {
            Vector menuItem = new Vector();
            int type = msg.reader().readByte();
            final int npc = msg.reader().readByte();
            final int shopID = msg.reader().readByte();
            switch (type) {
                case 0:
                    int n = msg.reader().readByte();
                    for (int i = 0; i < n; i++) {
                        String name = msg.reader().readUTF();
                        final short charID = msg.reader().readShort();
                        if (charID != this.mainChar.ID) {
                            menuItem.addElement(new Command(name, new IAction() {
                                @Override
                                public void perform() {
                                    GameScr.this.gameService.requestGetListItem(npc, shopID, charID);
                                    Canvas.startWaitDlg();
                                }
                            }));
                        }
                    }
                    Canvas.currentDialog = null;
                    if (menuItem.size() > 0) {
                        Canvas.menu.startAt(menuItem, 3);
                        return;
                    }
                    return;
                case 1:
                    Canvas.currentDialog = null;
                    WindowInfoScr.gI().sellItem.removeAllElements();
                    int nItem = msg.reader().readByte();
                    short charID2 = msg.reader().readShort();
                    for (int i2 = 0; i2 < nItem; i2++) {
                        ItemInInventory item = new ItemInInventory();
                        byte b = msg.reader().readByte();
                        item.charClazz = b;
                        item.clazz = b;
                        item.itemID = msg.reader().readShort();
                        item.idTemplate = msg.reader().readShort();
                        item.plusTemplate = msg.reader().readByte();
                        item.prize = msg.reader().readInt();
                        item.level = msg.reader().readByte();
                        item.durable = msg.reader().readShort();
                        item.numKham = msg.reader().readByte();
                        item.totalKham = msg.reader().readByte();
                        item.allAttribute.removeAllElements();
                        byte len4 = msg.reader().readByte();
                        for (byte j = 0; j < len4; j = (byte) (j + 1)) {
                            InfoAttributeItem info = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                            item.allAttribute.addElement(info);
                        }
                        item.colorName = msg.reader().readByte();
                        item.heItem = msg.reader().readByte();
                        item.beKicked = msg.reader().readByte();
                        item.hangItem = msg.reader().readByte();
                        item.magic_physic = msg.reader().readByte();
                        item.lock = msg.reader().readByte();
                        item.nameCharSeal = msg.reader().readUTF();
                        if (this.mainChar.ID == charID2) {
                            int size0 = Char.inventory.size();
                            for (int j2 = 0; j2 < size0; j2++) {
                                ItemInInventory it = (ItemInInventory) Char.inventory.elementAt(j2);
                                if (it.itemID == item.itemID) {
                                    it.prize = item.prize;
                                    it.isSelling = true;
                                    WindowInfoScr.gI().sellItem.addElement(it);
                                }
                            }
                        } else {
                            WindowInfoScr.gI().sellItem.addElement(item);
                        }
                    }
                    byte numGem = msg.reader().readByte();
                    for (int i3 = 0; i3 < numGem; i3++) {
                        RealID real = new RealID();
                        real.realID = msg.reader().readShort();
                        real.ID = msg.reader().readShort();
                        real.price = msg.reader().readInt();
                        WindowInfoScr.gI().sellItem.addElement(real);
                    }
                    WindowInfoScr.gI().idSeller = npc;
                    WindowInfoScr.gI().shopIdSell = shopID;
                    if (this.mainChar.ID == charID2) {
                        showWindow(0, true, new byte[]{23});
                        return;
                    } else if (WindowInfoScr.gI().sellItem.size() > 0) {
                        WindowInfoScr.gI().idCharSell = charID2;
                        showWindow(0, true, new byte[]{24});
                    } else {
                        return;
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWindow(int focus, boolean isSell, byte[] index) {
        WindowInfoScr.gI().setInfo(focus, isSell, index);
        WindowInfoScr.gI().switchToMe();
    }

    public void onUserSellItem(Message msg) {
        try {
            boolean sell = msg.reader().readBoolean();
            short itemID = msg.reader().readShort();
            int price = 0;
            if (sell) {
                price = msg.reader().readInt();
            }
            byte type = msg.reader().readByte();
            if (type == 0) {
                ItemInInventory item = getItemByID(Char.inventory, itemID);
                if (item != null) {
                    item.isSelling = sell;
                    if (sell) {
                        item.prize = price;
                        WindowInfoScr.gI().sellItem.addElement(item);
                        Canvas.startOKDlg("Đã bỏ vào gian hàng.");
                        return;
                    } else {
                        item.prize = 0;
                        WindowInfoScr.gI().sellItem.removeElement(item);
                        Canvas.startOKDlg("Đã lấy vật phẩm khỏi gian hàng.");
                        return;
                    }
                }
                return;
            }
            short idGem = msg.reader().readShort();
            if (sell) {
                GemTemplate g = GemTemplate.getGemByRealID(itemID);
                if (g != null) {
                    g.number--;
                    if (g.number <= 0) {
                        MainChar.gemItem.removeElement(g);
                    }
                    RealID real = new RealID(g.rID);
                    real.ID = idGem;
                    real.price = price;
                    WindowInfoScr.gI().sellItem.addElement(real);
                }
            } else {
                int size0 = WindowInfoScr.gI().sellItem.size();
                for (int i = 0; i < size0; i++) {
                    RealID real2 = (RealID) WindowInfoScr.gI().sellItem.elementAt(i);
                    if (real2.realID == itemID) {
                        WindowInfoScr.gI().sellItem.removeElement(real2);
                        GemTemplate g2 = GemTemplate.getGemByID(real2.ID);
                        if (g2 != null) {
                            g2.number++;
                            break;
                        }
                        GemTemplate gem = new GemTemplate();
                        gem.rID = real2.realID;
                        gem.id = real2.ID;
                        gem.number = 1;
                        MainChar.gemItem.addElement(gem);
                        break;
                    }
                }
            }
            Canvas.endDlg();
        } catch (Exception e) {
            Cout.println(new StringBuffer("LOI HAM ON_USER_SELL_ITEM").append(e.toString()).toString());
        }
    }

    public void onBuyItemOfUser(short itemID, short charID, byte type) {
        if (this.mainChar.ID == charID) {
            Canvas.startOKDlg("Món đồ đã ở trong hành trang.");
        }
        int size0 = WindowInfoScr.gI().sellItem.size();
        for (int i = 0; i < size0; i++) {
            if (type == 0 && (WindowInfoScr.gI().sellItem.elementAt(i) instanceof ItemInInventory)) {
                ItemInInventory item = (ItemInInventory) WindowInfoScr.gI().sellItem.elementAt(i);
                if (item.itemID == itemID) {
                    WindowInfoScr.gI().sellItem.removeElementAt(i);
                    if (WindowInfoScr.gI() == Canvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 24) {
                        WindowInfoScr.gI().setInfo(WindowInfoScr.gI().getListBuyItem());
                        return;
                    }
                    return;
                }
            } else if (WindowInfoScr.gI().sellItem.elementAt(i) instanceof RealID) {
                RealID item2 = (RealID) WindowInfoScr.gI().sellItem.elementAt(i);
                if (item2.realID == itemID) {
                    WindowInfoScr.gI().sellItem.removeElementAt(i);
                    if (WindowInfoScr.gI() == Canvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 24) {
                        WindowInfoScr.gI().setInfo(WindowInfoScr.gI().getListBuyItem());
                        return;
                    }
                    return;
                }
            } else {
                continue;
            }
        }
    }

    public void onInfoFriend(Message msg) {
        try {
            int ok = msg.reader().readByte();
            String charInvite = msg.reader().readUTF();
            final int charID = msg.reader().readShort();
            switch (ok) {
                case -1:
                default:
                    return;
                case 0:
                    Canvas.startYesNoDlg(new StringBuffer(String.valueOf(charInvite)).append(" muốn kết bạn với bạn").toString(), new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.sendInfoAddFriend(charID, 1);
                            Canvas.currentDialog = null;
                        }
                    }, new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.sendInfoAddFriend(charID, -1);
                            Canvas.currentDialog = null;
                        }
                    });
                    return;
                case 1:
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSMSStruct(Message msg) throws IOException {
        if (msg.reader().available() <= 0) {
            return; // server không có cú pháp nạp: gói rỗng
        }
        int len = msg.reader().readByte();
        for (int i = 0; i < len; i++) {
            this.decriptNap.addElement(msg.reader().readUTF());
            this.syntaxNap.addElement(msg.reader().readUTF());
            this.centerNap.addElement(msg.reader().readUTF());
        }
    }

    public static void loadConfig(int type) {
        switch (type) {
            case 0:
                paintCay = 0;
                paintChar = 1;
                if (Tilemap.trees != null) {
                    int size0 = Tilemap.trees.size();
                    for (int i = 0; i < size0; i++) {
                        ((Tree) Tilemap.trees.elementAt(i)).removeImage();
                    }
                }
                break;
            case 1:
            case 2:
                paintCay = 1;
                paintChar = 1;
                break;
            case 3:
                paintCay = 0;
                paintChar = 0;
                if (Tilemap.trees != null) {
                    int size1 = Tilemap.trees.size();
                    for (int i2 = 0; i2 < size1; i2++) {
                        ((Tree) Tilemap.trees.elementAt(i2)).removeImage();
                    }
                }
                break;
        }
    }

    public void showConfig() {
        Vector menuItem = new Vector();
        String[] str = {"Thấp", "Vừa", "Cao", "Rất thấp"};
        for (int i = 0; i < 4; i++) {
            final int ii = i;
            menuItem.addElement(new Command(str[i], new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.setConfig(ii);
                }
            }));
        }
        Canvas.menu.startAt(menuItem, 3);
    }

    public void onRegClan(final short[] arrayIndex) {
        Vector menu = new Vector();
        int size0 = arrayIndex.length;
        for (int i = 0; i < size0; i++) {
            final int ii = i;
            menu.addElement(new Command("", new AnonymousClass104(arrayIndex, ii)) {
                @Override
                public void paint(Graphics g, int x, int y) {
                    GameData.paintIcon(g, (short) (arrayIndex[ii] + 1000), x, y);
                }
            });
        }
        PaintPopup.gI().setPos(20, 20, Canvas.w - 40, (Canvas.h - 40) - Screen.deltaY, (Canvas.w - 40) / 20);
        PaintPopup.gI().setInfo(menu, "ICON");
        PaintPopup.gI().switchToMe();
    }

    class AnonymousClass104 implements IAction {
        private final short[] val$arrayIndex;
        private final int val$ii;

        AnonymousClass104(short[] sArr, int i) {
            this.val$arrayIndex = sArr;
            this.val$ii = i;
        }

        @Override
        public void perform() {
            InputDlg inputDlg = Canvas.inputDlg;
            final short[] sArr = this.val$arrayIndex;
            final int i = this.val$ii;
            inputDlg.setInfo("Tên đội của bạn", new IAction() {
                @Override
                public void perform() {
                    if (!Canvas.inputDlg.tfInput.getText().equals("")) {
                        Canvas.startWaitDlg();
                        GameScr.this.gameService.chooseIconClan(sArr[i], Canvas.inputDlg.tfInput.getText());
                        PaintPopup.gI().right.action.perform();
                    }
                }
            }, 0, 25, true);
            Canvas.inputDlg.show();
        }
    }

    public void showClanInfo(ClanInfo clan) {
        ViewInfoScr.gI().setInfo(clan);
        ViewInfoScr.gI().switchToMe();
        Canvas.endDlg();
    }

    public void onViewCharInfo(Char ch) {
        // pokemon: the trang bi nguoi khac = Pokemon + ngoai hinh cua nguoi dang chon (goi -22 khong co)
        Actor dangChon = Canvas.gameScr.focusedActor;
        if (dangChon instanceof Char && ch.pokeDex <= 0) {
            Char goc = (Char) dangChon;
            ch.pokeDex = goc.pokeDex;
            ch.pokeShiny = goc.pokeShiny;
            ch.ngoaiHinh = goc.ngoaiHinh;
        }
        WindowInfoScr.gI().setInfo(0, false, new byte[]{1});
        WindowInfoScr.charWearing = ch;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr.gI().name = "Trang bị";
        Canvas.endDlg();
    }

    public void setNPC_server_limit(String name0, short ID0, short idImg0, short x0, short y0, short wimg0, short himg0, byte nFrame0, byte typeLimit) {
        if (typeLimit != 1) {
            return;
        }
        int size0 = this.npc_limit.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.npc_limit.elementAt(i);
            if (ac != null && ac.catagory == 2) {
                NPC npc = (NPC) ac;
                if (npc.npcType == 1) {
                    Npc_Server npc_sever = (Npc_Server) npc;
                    if (npc_sever.ID == ID0) {
                        npc_sever.name = name0;
                        npc_sever.ID = ID0;
                        npc_sever.type = ID0;
                        npc_sever.idImg = idImg0;
                        npc_sever.x = x0;
                        npc_sever.y = y0;
                        npc_sever.width = wimg0;
                        npc_sever.height = himg0;
                        npc_sever.nFrame = nFrame0;
                        npc.typeLimit = typeLimit;
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
        Npc_Server bos = new Npc_Server();
        bos.name = name0;
        bos.ID = ID0;
        bos.type = ID0;
        bos.idImg = idImg0;
        bos.x = x0;
        bos.y = y0;
        bos.width = wimg0;
        bos.height = himg0;
        bos.nFrame = nFrame0;
        bos.typeLimit = typeLimit;
        this.npc_limit.addElement(bos);
    }

    public void setNPC_server(String name0, short ID0, short idImg0, short x0, short y0, short wimg0, short himg0, byte nFrame0, byte typeLimit) {
        setNPC_server_limit(name0, ID0, idImg0, x0, y0, wimg0, himg0, nFrame0, typeLimit);
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac != null && ac.catagory == 2) {
                NPC npc = (NPC) ac;
                if (npc.npcType == 1) {
                    Npc_Server npc_sever = (Npc_Server) npc;
                    if (npc_sever.ID == ID0) {
                        npc_sever.name = name0;
                        npc_sever.ID = ID0;
                        npc_sever.type = ID0;
                        npc_sever.idImg = idImg0;
                        npc_sever.x = x0;
                        npc_sever.y = y0;
                        npc_sever.width = wimg0;
                        npc_sever.height = himg0;
                        npc_sever.nFrame = nFrame0;
                        npc.typeLimit = typeLimit;
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
        Npc_Server bos = new Npc_Server();
        bos.name = name0;
        bos.ID = ID0;
        bos.type = ID0;
        bos.idImg = idImg0;
        bos.x = x0;
        bos.y = y0;
        bos.width = wimg0;
        bos.height = himg0;
        bos.nFrame = nFrame0;
        bos.typeLimit = typeLimit;
        this.actors.addElement(bos);
    }

    public void setMyFarm(int idFarm, int id, int x, int y, String info, String name, int idImgTree, int timeLive, boolean isBuyOk, byte addy, byte colorString) {
        int size0 = this.actors.size();
        for (int i = 0; i < size0; i++) {
            Actor ac = (Actor) this.actors.elementAt(i);
            if (ac.catagory == 10) {
                Ground gr = (Ground) ac;
                if (gr.ID == idFarm) {
                    gr.setInfo(idFarm, id, x, y, info, name, idImgTree, timeLive, isBuyOk, addy, colorString);
                    return;
                }
            }
        }
        this.actors.addElement(new Ground(idFarm, id, x, y, info, name, idImgTree, timeLive, isBuyOk, addy, colorString));
    }

    public static Actor CreateActor(byte catagory, short type, short id, short x, short y, byte isnewmonster, byte isThanthu) {
        Actor a = null;
        switch (catagory) {
            case 0:
                if (isThanthu > -1) {
                    a = new PokemonHuyenThoai();
                    a.setIdThanThu(isThanthu);
                } else {
                    a = new Char();
                }
                break;
            case 1:
                if (type == 91 || type == 92) {
                    a = new BossSerperior();
                } else if (type == 93) {
                    a = new BossGengar();
                } else if (type == 94) {
                    a = new BossDusknoir();
                } else if (type == 46) {
                    a = new BossAegislash();
                } else if (type == 43) {
                    a = new TruNangLuong(0);
                } else if (type == 36) {
                    a = new TruNangLuong(1);
                } else if (type == 37) {
                    a = new TruNangLuong(2);
                } else if (type == 38) {
                    a = new BossGyarados();
                } else if (type == 39) {
                    a = new BossSalamence();
                } else if (type == 82) {
                    a = new BossZapdos();
                } else if (type == 83) {
                    a = new CongGym();
                    congThanh = a;
                } else if (type == 84) {
                    a = new QuaiYamask();
                } else if (type == 113) {
                    a = new MonsterNew(113);
                } else if (type == 115) {
                    a = new MonsterNew(115);
                } else if (type == 116) {
                    a = new MonsterNew(116);
                } else if (type == 117) {
                    a = new MonsterNew(117);
                } else if ((type >= 95 && type <= 112) || isnewmonster == 1) {
                    a = new MonsterNew(type);
                } else {
                    a = new Monster();
                }
                break;
            case 3:
                a = new DropItem();
                break;
            case 4:
                a = new DropPotion();
                break;
            case 6:
            case 7:
                a = new DropGemItem();
                break;
            case 14:
                a = new ItemQuest();
                break;
        }
        a.setNewMonster(isnewmonster);
        if (type >= 85 && type <= 89 && catagory == 1) {
            ((Monster) a).isMineral = true;
        }
        a.x = x;
        a.y = y;
        a.catagory = catagory;
        a.ID = id;
        a.xFirst = x;
        a.yFirst = y;
        a.setType(type);
        return a;
    }

    public void onCapCha(byte[] arrayImg) {
        final Image img = Image.createImage(arrayImg, 0, arrayImg.length);
        final int w = img.getWidth() + 35;
        int h = img.getHeight() + 60;
        final TField tfText = new TField(5, h - 26, w - 10, 20);
        tfText.isFocus = true;
        PageScr.gI().setInfo(Canvas.hw - (w / 2), (Canvas.hh - (h / 2)) - Screen.deltaY, w, h, "NHẬP CHỮ", null);
        PageScr.gI().left = new Command("Ok", new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.doCapCha(tfText.getText());
                Canvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().right = tfText.cmdClear;
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                tfText.update();
            }

            @Override
            public void paint(Graphics g, int x, int y) {
                g.drawImage(img, w / 2, 30, 17);
                tfText.paint(g);
            }

            @Override
            public void keyPress(int keyCode) {
                tfText.keyPressed(keyCode);
            }
        };
        PageScr.gI().switchToMe();
    }

    private void updateminiMap() {
        cmtoXMini = this.mainChar.x / 16;
        cmtoYmini = this.mainChar.y / 16;
        if (cmtoXMini > Tilemap.w - (wMiniMap / 2)) {
            cmtoXMini = Tilemap.w - wMiniMap;
        } else if (cmtoXMini < wMiniMap / 2) {
            cmtoXMini = 0;
        } else {
            cmtoXMini -= wMiniMap / 2;
        }
        if (cmtoYmini < hMiniMap / 2) {
            cmtoYmini = 0;
        } else {
            cmtoYmini -= hMiniMap / 2;
        }
        if (cmtoYmini > Tilemap.h - hMiniMap) {
            cmtoYmini = Tilemap.h - hMiniMap;
        }
    }

    private void updatePoint() {
        if (HudRong.bat() && HudRong.cham(this)) {
            return;   // pokemon: cú chạm thuộc HUD màn rộng
        }
        if (!Canvas.isPointer(Canvas.hw - 40, 0, 80, 20) && Canvas.isPointer(0, 0, Canvas.w, Canvas.h)) {
            // pokemon: chạm ô trống (+) / giữ lâu một ô của thanh ô nhanh -> bảng gán chiêu (ChonChieuNhanh)
            if (!HudRong.bat() && game.pokemon.ChonChieuNhanh.thanhJar(buocO(), caoThanhO())) {   // màn rộng: HudRong gọi chamO
                Canvas.isPointerClick = false;
                Canvas.isPointerDown = false;
                return;
            }
            if (Canvas.isPointerClick) {
                int wSlot = buocO() * 5;
                int hSlot = caoThanhO();
                if (!HudRong.bat() && Canvas.isPointer(Canvas.hw - (wSlot / 2), (Canvas.h - hSlot) - 20, wSlot, hSlot)) {
                    int id = (Canvas.px - (Canvas.hw - (wSlot / 2))) / buocO();
                    if (id > 4) {
                        id = 4;
                    }
                    Canvas.keyPressed[1 + (id * 2)] = true;
                    Canvas.isPointerClick = false;
                    Canvas.isPointerDown = false;
                    return;
                }
            }
            if (Canvas.isPointer(this.posMiniMap.x, this.posMiniMap.y, wMiniMap, hMiniMap) && Canvas.isPointerClick) {
                int dx = Canvas.pxLast - Canvas.px;
                int dy = Canvas.pyLast - Canvas.py;
                if (Util.abs(dy) < 10 && Util.abs(dx) < 10) {
                    if (this.posCam == null) {
                        this.posCam = new Position(0, 0);
                    }
                    this.posCam.x = (short) ((cmtoXMini + Canvas.px) - this.posMiniMap.x);
                    this.posCam.y = (short) ((cmtoYmini + Canvas.py) - this.posMiniMap.y);
                    this.mainChar.posTransRoad = null;
                    if (!Tilemap.tileTypeAtPixel((this.posCam.x * 16) + 2, (this.posCam.y * 16) + 2, 2) && !checkMoveLimit((this.posCam.x * 16) + 2, (this.posCam.y * 16) + 2)) {
                        this.mainChar.xTo = this.mainChar.x;
                        this.mainChar.yTo = this.mainChar.y;
                        this.mainChar.xBegin = this.mainChar.x;
                        this.mainChar.yBegin = this.mainChar.y;
                        this.tg.index = (byte) 8;
                        this.mainChar.posTransRoad = updateFindRoad(this.mainChar.x / 16, this.mainChar.y / 16, this.posCam.x, this.posCam.y);
                        this.mainChar.countRoad = 0;
                        Canvas.isPointerClick = false;
                        return;
                    }
                }
            }
            if (Canvas.isPointerClick) {
                int aa = cmx + Canvas.px;
                int bb = cmy + Canvas.py;
                if (this.focusedActor != null && this.focusedActor.catagory == 10) {
                    if (this.mainChar.x >= this.xBeginFrame && this.mainChar.x <= this.xTheendFrame + 32 && this.mainChar.y >= this.yBeginFrame && this.mainChar.y <= this.yTheendFrame + 32) {
                        int size1 = this.actors.size();
                        for (int i2 = 0; i2 < size1; i2++) {
                            Actor ac2 = (Actor) this.actors.elementAt(i2);
                            if (aa >= ac2.x + 2 && aa <= ac2.x + 30 && bb >= ac2.y && bb <= ac2.y + 30) {
                                Canvas.isPointerClick = false;
                                if (this.focusedActor.ID != -1 && this.focusedActor.ID == ac2.ID) {
                                    if (this.mainChar.state == 0) {
                                        doFire(5, indexTabSlot);
                                        return;
                                    }
                                    return;
                                } else {
                                    if (ac2.ID != this.mainChar.ID) {
                                        findRoad(aa, bb);
                                        return;
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    this.focusedActor = null;
                } else {
                    // pokemon: chạm actor (hộp quanh hình thật) = chọn quái / đánh khi chạm lại, NPC = đi tới + nói chuyện,
                    // người chơi = menu nhỏ, đồ rơi = nhặt (game.pokemon.HanhDong); chạm đất = bỏ chọn + đi tới
                    if (game.pokemon.HanhDong.chamThe(this, aa, bb)) {
                        return;
                    }
                    game.pokemon.HanhDong.boChon();
                    this.focusedActor = null;
                }
                game.pokemon.HanhDong.chamDat(aa, bb);
                findRoad(aa, bb);
            }
        }
    }

    public void findRoad(int aa, int bb) {
        this.duoiMucTieu = null; // đi tới chỗ khác: thôi đuổi
        this.duoiCat = -1;
        int aa2 = aa / 16;
        int bb2 = bb / 16;
        if (!Tilemap.tileTypeAtPixel(cmx + Canvas.px, cmy + Canvas.py, 2) && !checkMoveLimit(cmx + Canvas.px, cmy + Canvas.py)) {
            if (this.posCam == null) {
                this.posCam = new Position(0, 0);
            }
            this.posCam.x = (short) aa2;
            this.posCam.y = (short) bb2;
            Canvas.isPointerClick = false;
            this.mainChar.xTo = this.mainChar.x;
            this.mainChar.yTo = this.mainChar.y;
            this.mainChar.xBegin = this.mainChar.x;
            this.mainChar.yBegin = this.mainChar.y;
            this.tg.index = (byte) 8;
            this.mainChar.posTransRoad = updateFindRoad(this.mainChar.x / 16, this.mainChar.y / 16, aa2, bb2);
            this.mainChar.countRoad = 0;
        }
    }

    public void findRoad2(int aa, int bb) {
        this.duoiMucTieu = null; // đi tới chỗ khác: thôi đuổi
        this.duoiCat = -1;
        int aa2 = aa / 16;
        int bb2 = bb / 16;
        if (this.posCam == null) {
            this.posCam = new Position(0, 0);
        }
        this.posCam.x = (short) aa2;
        this.posCam.y = (short) bb2;
        this.mainChar.xTo = this.mainChar.x;
        this.mainChar.yTo = this.mainChar.y;
        this.mainChar.xBegin = this.mainChar.x;
        this.mainChar.yBegin = this.mainChar.y;
        this.tg.index = (byte) 8;
        this.mainChar.posTransRoad = updateFindRoad(this.mainChar.x / 16, this.mainChar.y / 16, aa2, bb2);
        this.mainChar.countRoad = 0;
        timeRemovePos = 100;
    }

    private int setDis(int x1, int y1, int x2, int y2) {
        return Util.abs(x1 - x2) + Util.abs(y1 - y2);
    }

    /**
     * Tìm đường cho nhân vật chính (chạm đất, tự đi nhiệm vụ, đuổi quái): game.model.DuongDi (BFS trong khung bao,
     * tường = type & 2 như máy chủ). Thay vòng cũ quét cả bản đồ mỗi bước (khựng khung hình) - xem DuongDi.
     */
    private short[] updateFindRoad(int xF, int yF, int xTo, int yTo) {
        if (Util.distance(xF * 16, yF * 16, xTo * 16, yTo * 16) <= 16 || checkMoveLimit(xTo, yTo)) {
            return null;
        }
        xStart = (byte) cmxMini;
        yStart = (byte) cmyMini;
        short[] duong = DuongDi.tim(Tilemap.type, Tilemap.w, Tilemap.h, xF, yF, xTo, yTo);
        if (duong == null && DuongDi.quaXa) {
            // như vòng cũ khi quá 500 bước: tự đi nhiệm vụ thì thử điểm khác (isFixBugAutoQuest)
            if (iTaskAuto == 2) {
                this.iTaskAutoLast = iTaskAuto;
                isFixBugAutoQuest = true;
            }
            iTaskAuto = 1;
        }
        return duong;
    }

    private boolean setContinue(int i, int j, byte[][] mapFind2) {
        if (i + 1 < mapFind2.length && mapFind2[i + 1][j] == 0) {
            return true;
        }
        if (i - 1 >= 0 && mapFind2[i - 1][j] == 0) {
            return true;
        }
        if (j + 1 < mapFind2[i].length && mapFind2[i][j + 1] == 0) {
            return true;
        }
        if (j - 1 >= 0 && mapFind2[i][j - 1] == 0) {
            return true;
        }
        return false;
    }

    public void onMenuOption(final int userId1, final byte idMenu, String[] listStr) {
        if (Canvas.menu.showMenu) {
            Canvas.menu.showMenu = false;
        }
        // pokemon (hệ sinh thái, NPC ảo -528): menu "Túi sinh thái" hiện dạng lưới ô
        if (TuiSinhThaiScr.laMenuTui(userId1, idMenu, listStr)) {
            TuiSinhThaiScr.gI().moi(userId1, idMenu, listStr);
            Canvas.currentDialog = null;
            return;
        }
        Vector menu = new Vector();
        int size0 = listStr.length;
        for (int i = 0; i < size0; i++) {
            final int ii = i;
            // hệ sinh thái: "Câu cá tại đây" chỉ hiện khi đứng sát hồ (luật chỉ câu ở hồ)
            if (userId1 == TuiSinhThaiScr.NPC_ID && idMenu == 0 && listStr[i] != null && listStr[i].startsWith("Câu cá") && !game.pokemon.SinhThai.ganHo()) {
                continue;
            }
            menu.addElement(new Command(listStr[i], new IAction() {
                @Override
                public void perform() {
                    if (userId1 == TuiSinhThaiScr.NPC_ID && idMenu == 0 && ii == 1) {
                        game.pokemon.SinhThai.batDauCau();   // "Câu cá tại đây"
                    }
                    GameScr.this.gameService.doMenuOption(userId1, idMenu, ii);
                }
            }));
        }
        Canvas.menu.startAt(menu, 3);
        Canvas.currentDialog = null;
    }

    public void onTextBox(final int userID, final byte idMenu, String nameText, int typeInput) {
        Canvas.inputDlg.setInfo(nameText, new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.doTextBox(userID, idMenu, Canvas.inputDlg.tfInput.getText());
                Canvas.endDlg();
            }
        }, typeInput, 40, true);
        Canvas.inputDlg.show();
    }

    public void onContentClanQuest(String content) {
        Canvas.startYesNoDlg(content, new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.questClan(1);
                Canvas.endDlg();
            }
        });
    }

    public static void onWeather(byte weather2, int number, int timeLimit) {
        // thời tiết mới thay hẳn thời tiết cũ (trước đây mưa + tuyết chồng nhau)
        for (int i = 0; i < effAnimate.size(); i++) {
            ((AnimateEffect) effAnimate.elementAt(i)).isStop = true;
        }
        if (weather2 != -1) {
            effAnimate.addElement(new AnimateEffect(weather2, true, number, timeLimit));
        } else {
            for (int i = 0; i < effAnimate.size(); i++) {
                AnimateEffect ef = (AnimateEffect) effAnimate.elementAt(i);
                ef.isStop = true;
            }
        }
        weather = weather2;
    }

    public void onSetCharKham(short idChar, byte[] idSkill, byte[] time) {
        LiveActor live = (LiveActor) findActorByID(idChar);
        if (live != null) {
            int size0 = idSkill.length;
            for (int i = 0; i < size0; i++) {
                int aa = 0;
                int size1 = live.effSkill.size();
                for (int a = 0; a < size1; a++) {
                    Skill_Kham eff = (Skill_Kham) live.effSkill.elementAt(a);
                    if (eff.type == idSkill[i]) {
                        eff.delay = time[i];
                        aa = 1;
                        break;
                    }
                }
                if (aa == 0) {
                    live.effSkill.addElement(new Skill_Kham(idSkill[i], time[i], live));
                }
            }
        }
    }

    public void onDynamicObj(DynamicObj obj) {
        this.objDynamic.addElement(obj);
        this.actors.addElement(obj);
    }

    public void onEffect(EffectServerManager effObj) {
        if (effManager == null) {
            effManager = new Vector();
        }
        effManager.addElement(effObj);
    }

    public void onCheDo(String nameItem, short idItem, Vector listMineral, int type, int Khoa) {
        WindowInfoScr.gI().listTemp.removeAllElements();
        WindowInfoScr.gI().listCheDo = listMineral;
        WindowInfoScr.gI().listEp = new Vector();
        WindowInfoScr.idTemp = idItem;
        WindowInfoScr.optionCreateItem = (byte) type;
        if (listMineral.size() == 0) {
            Canvas.startOKDlg("Vật phẩm không thể thăng cấp");
            return;
        }
        WindowInfoScr.khoaCheDo = (byte) Khoa;
        Vector vtam = new Vector();
        if (Khoa == 0) {
            vtam = MainChar.gemItem;
        } else if (Khoa == 1) {
            vtam = MainChar.gemItemKhoa;
        } else {
            int aa = MainChar.gemItem.size();
            int bb = MainChar.gemItemKhoa.size();
            for (int i = 0; i < aa; i++) {
                GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemplate g = new GemTemplate(gem.id);
                GemTemplate.copyData(gem, g);
                vtam.addElement(g);
            }
            for (int i2 = 0; i2 < bb; i2++) {
                GemTemplate gemKhoa = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean tonTai = false;
                for (int j = 0; j < vtam.size(); j++) {
                    GemTemplate g2 = (GemTemplate) vtam.elementAt(j);
                    if (g2.id == gemKhoa.id) {
                        g2.number += gemKhoa.number;
                        tonTai = true;
                    }
                }
                if (!tonTai) {
                    GemTemplate gk = new GemTemplate(gemKhoa.id);
                    GemTemplate.copyData(gemKhoa, gk);
                    vtam.addElement(gk);
                }
            }
        }
        int size0 = listMineral.size();
        for (int i3 = 0; i3 < size0; i3++) {
            Mineral mi = (Mineral) listMineral.elementAt(i3);
            for (int a = 0; a < vtam.size(); a++) {
                GemTemplate gem2 = (GemTemplate) vtam.elementAt(a);
                if ((type <= 2 && (type != 2 || (type == 2 && i3 == 0))) || type > 2) {
                    if (gem2.id >= mi.idTemplate + (type == 0 ? (short) 0 : (short) 3) && gem2.id <= mi.idTemplate + 5) {
                        WindowInfoScr.gI().listTemp.addElement(gem2);
                    }
                } else if (gem2.id == mi.idTemplate + 5) {
                    WindowInfoScr.gI().listTemp.addElement(gem2);
                }
            }
        }
        if (WindowInfoScr.gI().listTemp.size() == 0) {
            Canvas.startOKDlg("Không có nguyên liệu.");
        } else {
            showWindow(0, false, new byte[]{28});
            WindowInfoScr.gI().name = nameItem;
        }
    }

    private void ktGem(GemTemplate g, Vector vTam) {
        for (int i = 0; i < vTam.size(); i++) {
            GemTemplate a = (GemTemplate) vTam.elementAt(i);
            if (a.id == g.id) {
                return;
            }
        }
        vTam.addElement(g);
    }

    public void onMSGServer(String readUTF) {
        MessageScr.gI().addTab(readUTF, null, 0);
        if (game.pokemon.MenuNguCanh.bat()) {
            // 0.9.5: không còn dải tối chạy chữ ở đáy - nhắc sức khoẻ = dòng mờ ngắn, tin khác = biểu ngữ trên
            if (game.pokemon.DungDo.laSucKhoe(readUTF)) {
                game.pokemon.DungDo.sucKhoe(readUTF);
            } else {
                game.pokemon.DungDo.bieuNgu(readUTF);
            }
            return;
        }
        if (this.listMSGServer == null) {
            this.listMSGServer = new Vector();
        }
        this.listMSGServer.addElement(readUTF);
    }

    public void onDropItem2Ground(Message msg) {
        try {
            int size = msg.reader().readByte();
            for (int i = 0; i < size; i++) {
                Dropable dropable = (Dropable) CreateActor(msg.reader().readByte(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), (byte) 0, (byte) -1);
                dropable.startDropFrom(dropable.x, dropable.y, dropable.x, dropable.y);
                this.actors.addElement(dropable);
            }
        } catch (Exception e) {
        }
    }

    public void onSkillClan(SkillClan[] skillclan, SkillClan[] skillClanPrivate) {
        this.skillClan = skillclan;
        this.skillClanPrivate = skillClanPrivate;
    }

    public void onLuckyDraw(Message msg) {
        try {
            byte type = msg.reader().readByte();
            if (type == 0) {
                ItemQsScreen.gI().resetData();
                ItemQsScreen.allItemLucky.removeAllElements();
                ItemQsScreen.totalItemSelect = msg.reader().readByte();
                byte size = msg.reader().readByte();
                for (byte i = 0; i < size; i = (byte) (i + 1)) {
                    ItemLuckyDraw item = new ItemLuckyDraw();
                    item.cat = msg.reader().readByte();
                    item.id = msg.reader().readShort();
                    item.name = msg.reader().readUTF();
                    item.decript = new String[msg.reader().readByte()];
                    for (int j = 0; j < item.decript.length; j++) {
                        item.decript[j] = msg.reader().readUTF();
                    }
                    item.isSelect = msg.reader().readByte();
                    ItemQsScreen.allItemLucky.addElement(item);
                }
                ItemQsScreen.total = (short) ItemQsScreen.allItemLucky.size();
                if (ItemQsScreen.total < 32) {
                    ItemQsScreen.total = (short) 32;
                }
                ItemQsScreen.gI().last = this;
                ItemQsScreen.gI().switchToMe();
                return;
            }
            if (type == 1) {
                DialLuckyScr.gI().lastScr = this;
                DialLuckyScr.gI().switchToMe();
                int i2 = msg.reader().readByte();
                DialLuckyScr.arrayIndex = new byte[i2];
                for (byte i3 = 0; i3 < i2; i3 = (byte) (i3 + 1)) {
                    DialLuckyScr.arrayIndex[i3] = msg.reader().readByte();
                }
                DialLuckyScr.gI().onStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TimecountDown findTime(int id) {
        for (int i = 0; i < this.VecTime.size(); i++) {
            TimecountDown t = (TimecountDown) this.VecTime.elementAt(i);
            if (t != null && t.id == id) {
                return t;
            }
        }
        return null;
    }

    public void onShopNew(Message msg) {
        try {
            short typeshop = msg.reader().readShort();
            ShopNew.gI().typeshop = typeshop;
            int nItem1 = msg.reader().readShort();
            Vector items = new Vector();
            for (int i = 0; i < nItem1; i++) {
                ItemInInventory cii = new ItemInInventory();
                cii.charClazz = msg.reader().readByte();
                cii.itemID = msg.reader().readShort();
                cii.idTemplate = msg.reader().readShort();
                cii.plusTemplate = msg.reader().readByte();
                cii.level = msg.reader().readByte();
                cii.mDurable = msg.reader().readShort();
                cii.durable = msg.reader().readShort();
                cii.clazz = msg.reader().readByte();
                cii.numKham = msg.reader().readByte();
                cii.totalKham = msg.reader().readByte();
                cii.colorName = msg.reader().readByte();
                cii.heItem = msg.reader().readByte();
                cii.beKicked = msg.reader().readByte();
                cii.hangItem = msg.reader().readByte();
                cii.magic_physic = msg.reader().readByte();
                cii.lock = msg.reader().readByte();
                cii.nameCharSeal = msg.reader().readUTF();
                cii.allAttribute.removeAllElements();
                cii.lastTime = System.currentTimeMillis();
                cii.dayUse = msg.reader().readUnsignedShort();
                byte len4 = msg.reader().readByte();
                for (byte ii = 0; ii < len4; ii = (byte) (ii + 1)) {
                    InfoAttributeItem info = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                    cii.allAttribute.addElement(info);
                }
                cii.isItemDrop = msg.reader().readByte();
                cii.isEnoughData = true;
                items.addElement(cii);
            }
            ShopNew.text = msg.reader().readUTF();
            ShopNew.nameshop = msg.reader().readUTF();
            ShopNew.gI().ItemShopNew = items;
            PauseMenu.gI().showShopNew(0);
        } catch (Exception e) {
        }
    }

    public void onCountDown(Message msg) {
        short idicon;
        byte count;
        try {
            byte cat = msg.reader().readByte();
            short id = msg.reader().readShort();
            String tile = msg.reader().readUTF();
            int time_ = msg.reader().readInt();
            try {
                idicon = msg.reader().readShort();
                count = msg.reader().readByte();
            } catch (Exception e) {
                idicon = -1;
                count = -1;
            }
            long time = System.currentTimeMillis() + ((long) (time_ * 1000));
            if (cat != -1) {
                if (time_ > 0) {
                    Actor ac = findActorByID_Cat(id, cat);
                    if (ac != null) {
                        objcountDown = ac;
                    }
                    charcountdonw = new CharCountDown(time, tile);
                }
                if (time_ == 0) {
                    charcountdonw = null;
                    objcountDown = null;
                }
            }
            if (cat == -1 && time_ >= 0) {
                TimecountDown t = findTime(id);
                if (t == null) {
                    t = new TimecountDown(id, idicon, time_, tile, count);
                    this.VecTime.addElement(t);
                }
                if (t != null) {
                    t.tile = tile;
                    t.idIcon = idicon;
                    t.setsecond(time_);
                    if (count == -2) {
                        t.wantdestroy = true;
                    }
                }
            }
        } catch (Exception e2) {
        }
    }

    public static int isNpcQuest(int idNpc) {
        if (mainQuest != null) {
            if ((mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc) {
                return 1;
            }
            if (mainQuest.stateQuest == 0) {
                return 0;
            }
        }
        if (subQuest != null) {
            if ((subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc) {
                return 3;
            }
            if (subQuest.stateQuest == 0) {
                return 2;
            }
        }
        if (clanQuest != null) {
            if ((clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc) {
                return 3;
            }
            if (clanQuest.stateQuest == 0) {
                return 2;
            }
            return -1;
        }
        return -1;
    }

    public void getAllPosNPCMInimap(Actor npc) {
        if (mainQuest != null && ((mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == npc.getTypeNpc())) {
            setPosNPC(npc.getTypeNpc(), mainQuest.stateQuest == 1 ? 3 : 0, npc.x, npc.y);
            return;
        }
        if (subQuest != null && ((subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == npc.getTypeNpc())) {
            setPosNPC(npc.getTypeNpc(), subQuest.stateQuest == 1 ? 3 : 0, npc.x, npc.y);
            return;
        }
        if (clanQuest != null && ((clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == npc.getTypeNpc())) {
            setPosNPC(npc.getTypeNpc(), clanQuest.stateQuest == 1 ? 3 : 0, npc.x, npc.y);
            return;
        }
        for (int i = 0; i < allQuestCanReceive.size(); i++) {
            Quest q = (Quest) allQuestCanReceive.elementAt(i);
            if (q.stateQuest == 0 && q.idNpcReceive == npc.getTypeNpc()) {
                setPosNPC(npc.getTypeNpc(), 0, npc.x, npc.y);
            }
        }
    }

    public static Vector getAllQuestNpc(int idNpc) {
        Vector allquest = new Vector();
        if (mainQuest != null && ((mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc)) {
            allquest.addElement(new Command(new StringBuffer("Trả ").append(mainQuest.name).toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.mainQuest;
                    GameScr.currQuest.startTalk(Canvas.gameScr.mainChar, Canvas.gameScr.focusedActor, GameScr.mainQuest.stateQuest);
                }
            }));
        }
        if (subQuest != null && ((subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc)) {
            allquest.addElement(new Command(new StringBuffer("Trả ").append(subQuest.name).toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.subQuest;
                    GameScr.currQuest.startTalk(Canvas.gameScr.mainChar, Canvas.gameScr.focusedActor, GameScr.subQuest.stateQuest);
                }
            }));
        }
        if (clanQuest != null && ((clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc)) {
            allquest.addElement(new Command(new StringBuffer("Trả ").append(clanQuest.name).toString(), new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.clanQuest;
                    GameScr.currQuest.startTalk(Canvas.gameScr.mainChar, Canvas.gameScr.focusedActor, GameScr.clanQuest.stateQuest);
                }
            }));
        }
        for (int i = 0; i < allQuestCanReceive.size(); i++) {
            final Quest q = (Quest) allQuestCanReceive.elementAt(i);
            if (q.stateQuest == 0 && q.idNpcReceive == idNpc) {
                allquest.addElement(new Command(q.name, new IAction() {
                    @Override
                    public void perform() {
                        GameScr.currQuest = q;
                        GameScr.currQuest.startTalk(Canvas.gameScr.mainChar, Canvas.gameScr.focusedActor, q.stateQuest);
                    }
                }));
            }
        }
        return allquest;
    }

    public static Image getImgQuest(int idNpc) {
        if (mainQuest != null && ((mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc)) {
            return Res.imgSelect2;
        }
        if (subQuest != null && ((subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc)) {
            return Res.imgSelect2;
        }
        if (clanQuest != null && ((clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc)) {
            return Res.imgSelect2;
        }
        for (int i = 0; i < allQuestCanReceive.size(); i++) {
            Quest q = (Quest) allQuestCanReceive.elementAt(i);
            if (q.stateQuest == 0 && q.idNpcReceive == idNpc) {
                if (q.type == 3) {
                    return Res.imgSelect2;
                }
                return Res.imgSelect1;
            }
        }
        return Res.imgSelect;
    }

    public void setPosNPC(int id, int indexColor, int x, int y) {
        for (int i = 0; i < PosNPCQuest.size(); i++) {
            Position p = (Position) PosNPCQuest.elementAt(i);
            if (p != null && p.id == id) {
                return;
            }
        }
        Position pp = new Position();
        pp.id = id;
        pp.indexColor = (short) indexColor;
        pp.x = x;
        pp.y = y;
        PosNPCQuest.addElement(pp);
    }

    public void onQuest(Message m) {
        try {
            byte type_Group_Quest = m.reader().readByte();
            byte size = m.reader().readByte();
            if (type_Group_Quest == 0) {
                allQuestCanReceive.removeAllElements();
            }
            for (int i = 0; i < size; i++) {
                int idQuest = m.reader().readShort();
                Quest q = new Quest(idQuest);
                q.stateQuest = type_Group_Quest;
                q.main_sub = m.reader().readByte();
                q.name = m.reader().readUTF();
                switch (type_Group_Quest) {
                    case 0:
                        q.idNpcReceive = m.reader().readByte();
                        for (String str : Util.split(m.reader().readUTF(), ">")) {
                            q.content.addElement(str);
                        }
                        q.type = m.reader().readByte();
                        q.deltaLv = m.reader().readByte();
                        break;
                    case 1:
                        q.idNpcResponse = m.reader().readByte();
                        String[] data = Util.split(m.reader().readUTF(), ">");
                        for (String str2 : data) {
                            q.rescontent.addElement(str2);
                        }
                        q.decript = m.reader().readUTF();
                        q.infoGift = m.reader().readUTF();
                        break;
                    case 2:
                        q.type = m.reader().readByte();
                        q.decript = m.reader().readUTF();
                        q.idNpcResponse = m.reader().readByte();
                        q.deltaLv = m.reader().readByte();
                        q.infoGift = m.reader().readUTF();
                        if (q.type == 2) {
                            int nitemGet = m.reader().readByte();
                            q.idItemGet = new short[nitemGet];
                            q.totalItemGet = new short[nitemGet];
                            q.allItemGet = new short[nitemGet];
                            for (int j = 0; j < nitemGet; j++) {
                                short iditem = m.reader().readShort();
                                short itemGot = m.reader().readShort();
                                short totalItemGet = m.reader().readShort();
                                q.idItemGet[j] = iditem;
                                q.allItemGet[j] = itemGot;
                                q.totalItemGet[j] = totalItemGet;
                            }
                        } else if (q.type == 0) {
                            int nmonkil = m.reader().readByte();
                            q.idMonsKill = new short[nmonkil];
                            q.totalMonskill = new short[nmonkil];
                            q.allMonsKilled = new short[nmonkil];
                            for (int j2 = 0; j2 < nmonkil; j2++) {
                                short iditem2 = m.reader().readShort();
                                short itemGot2 = m.reader().readShort();
                                short totalItemGet2 = m.reader().readShort();
                                q.idMonsKill[j2] = iditem2;
                                q.allMonsKilled[j2] = itemGot2;
                                q.totalMonskill[j2] = totalItemGet2;
                            }
                        } else if (q.type == 4) {
                            q.totalMonskill = new short[1];
                            q.allMonsKilled = new short[1];
                            q.allMonsKilled[0] = m.reader().readShort();
                            q.totalMonskill[0] = m.reader().readShort();
                        }
                        break;
                }
                if (type_Group_Quest == 0) {
                    allQuestCanReceive.addElement(q);
                    if (q.main_sub == 0 && Char.skillLevelLearnt[0] == 0) {
                        this.idNpcReceive = q.idNpcReceive;
                        set_npc_request(this.idNpcReceive);
                    }
                } else if (q.main_sub == 0) {
                    mainQuest = q;
                } else if (q.main_sub == 1) {
                    subQuest = q;
                } else if (q.main_sub == 2) {
                    clanQuest = q;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFruit(Message msg) {
        try {
            Tree2014.gI().setInfoTree(msg);
        } catch (Exception e) {
        }
    }

    public void onTuBinh(Message msg) {
        TuBinhScr.gI().setData(msg);
    }

    public static void createNewSkill(LiveActor from, LiveActor to, int power, byte eff, int imgIndex) {
        Arrow a = new Arrow(imgIndex);
        a.set(0, from.x, from.y, power, eff, from, to);
        if (imgIndex == 10) {
            a.typeEffEnd = (byte) 46;
        } else if (imgIndex == 11) {
            a.typeEffEnd = (byte) 45;
        } else if (imgIndex == 12) {
            a.typeEffEnd = (byte) 30;
        } else if (imgIndex == 15) {
            a.typeEffEnd = (byte) 58;
        }
        arrowsUp.addElement(a);
    }

    public static Effect creatEff(LiveActor ac, int x, int y, int type) {
        Effect ef = new Effect(x, y, type);
        if (ac != null) {
            ef.myChar = ac;
            if (ac.getHorse() != -1) {
                ef.yadd = (byte) -5;
            }
        }
        switch (type) {
            case 41:
                ef.loop = (byte) 2;
                ef.yadd = (byte) (ef.yadd - 6);
                break;
            case 42:
            case 56:
                ef.loop = (byte) 2;
                ef.yadd = (byte) (ef.yadd - 6);
                break;
            case 43:
                ef.loop = (byte) 3;
                ef.yadd = (byte) (ef.yadd - 6);
                break;
            case 44:
            case 47:
            case 57:
                ef.loop = (byte) 1;
                ef.y -= 6;
                ef.yadd = (byte) (ef.yadd - 6);
                break;
        }
        return ef;
    }

    public void onNewEffect(Message msg) {
        Monster mons;
        int[] ideff = {41, 42, 43, 56};
        int[] idImg = {12, 10, 11, 15};
        int[] idSubeff = {44, 44, 47, 57};
        try {
            short id = msg.reader().readShort();
            LiveActor p = (LiveActor) getActor(id);
            if (p != null) {
                byte cat = msg.reader().readByte();
                byte idEff = msg.reader().readByte();
                byte size = msg.reader().readByte();
                Vector ac = new Vector();
                Vector dam = new Vector();
                Vector hpLeft = new Vector();
                for (int i = 0; i < size; i++) {
                    short idActor = msg.reader().readShort();
                    int power = msg.reader().readInt();
                    int hp = msg.reader().readInt();
                    if (cat == 0) {
                        Char pp = (Char) getActor(idActor);
                        if (pp != null) {
                            dam.addElement(new StringBuffer(String.valueOf(power)).toString());
                            ac.addElement(pp);
                            hpLeft.addElement(new StringBuffer(String.valueOf(hp)).toString());
                            for (int j = 0; j < 3; j++) {
                                Char ppp = new Char();
                                ppp.x = (short) (pp.x + ((r.nextInt() % 10) * 16));
                                ppp.y = (short) (pp.y + ((r.nextInt() % 10) * 16));
                                ac.addElement(ppp);
                                dam.addElement(new StringBuffer(String.valueOf(power)).toString());
                                hpLeft.addElement(new StringBuffer(String.valueOf(hp)).toString());
                            }
                        }
                    } else if (cat == 1 && (mons = findMonsterByID(idActor)) != null) {
                        dam.addElement(new StringBuffer(String.valueOf(power)).toString());
                        ac.addElement(mons);
                        hpLeft.addElement(new StringBuffer(String.valueOf(hp)).toString());
                        for (int j2 = 0; j2 < 3; j2++) {
                            Monster ppp2 = new Monster();
                            ppp2.x = (short) (mons.x + ((r.nextInt() % 10) * 16));
                            ppp2.y = (short) (mons.y + ((r.nextInt() % 10) * 16));
                            ac.addElement(ppp2);
                            dam.addElement(new StringBuffer(String.valueOf(power)).toString());
                            hpLeft.addElement(new StringBuffer(String.valueOf(hp)).toString());
                        }
                    }
                }
                Effect ef = creatEff(p, p.x, p.y, ideff[idEff]);
                ef.subEff = new SubEff();
                ef.subEff.ac = p;
                ef.subEff.target = ac;
                ef.subEff.idEff = idSubeff[idEff];
                ef.subEff.power = dam;
                ef.subEff.hp = hpLeft;
                ef.subEff.eff = 0;
                ef.subEff.indeximg = idImg[idEff];
                EffectManager.hiEffects.addElement(ef);
            }
        } catch (Exception e) {
        }
    }

    public void onAutoImbue(Message msg) {
        try {
            byte type = msg.reader().readByte();
            if (type == 0) {
                ScreenDapDo.gI().switchToMe(this);
                ScreenDapDo.gI().setSizeCell((byte) 1, false);
                return;
            }
            String info = msg.reader().readUTF();
            byte finish = msg.reader().readByte();
            if (Canvas.currentScreen == ScreenDapDo.gI()) {
                ScreenDapDo.gI().infoPoup = info;
                ScreenDapDo.gI().doFinishInbue();
                Canvas.endDlg();
            }
            if (finish == 1) {
                ScreenDapDo.gI().resetSelect();
                ScreenDapDo.isFinishImbue = true;
            }
        } catch (Exception e) {
        }
    }

    public void onTachNguyenLieu(Message msg) {
        try {
            byte type = msg.reader().readByte();
            if (type == 0) {
                ScreenDapDo.gI().switchToMe(this);
                ScreenDapDo.gI().setSizeCell((byte) 0, false);
            } else if (type == 4) {
                Canvas.currentScreen.startXayBot(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
            }
        } catch (Exception e) {
        }
    }

    public void onItemShopOnline(Message msg) {
        try {
            byte type = msg.reader().readByte();
            if (type == -1) {
                Canvas.shop.switchToMe(Canvas.currentScreen);
                return;
            }
            byte index = type;
            if (type == 3) {
                index = 4;
            } else if (type == 4) {
                index = 5;
            }
            ((Vector) ShopOnline.ALL_ITEM.elementAt(index)).removeAllElements();
            byte size = msg.reader().readByte();
            for (int i = 0; i < size; i++) {
                ItemInInventory item = new ItemInInventory();
                item.isEnoughData = true;
                item.clazz = msg.reader().readByte();
                item.idTemplate = msg.reader().readShort();
                item.plusTemplate = msg.reader().readByte();
                item.level = msg.reader().readByte();
                item.mDurable = msg.reader().readShort();
                item.durable = msg.reader().readShort();
                item.numKham = msg.reader().readByte();
                item.totalKham = msg.reader().readByte();
                item.colorName = msg.reader().readByte();
                item.heItem = msg.reader().readByte();
                item.beKicked = msg.reader().readByte();
                item.hangItem = msg.reader().readByte();
                item.magic_physic = msg.reader().readByte();
                item.lock = msg.reader().readByte();
                item.nameCharSeal = msg.reader().readUTF();
                item.nameCharSell = msg.reader().readUTF();
                item.nameCharBet = msg.reader().readUTF();
                item.prize = msg.reader().readInt();
                item.prizeBet = msg.reader().readInt();
                item.timeEnd = msg.reader().readLong();
                item.daySell = msg.reader().readUTF();
                item.allAttribute.removeAllElements();
                byte len4 = msg.reader().readByte();
                for (byte j = 0; j < len4; j = (byte) (j + 1)) {
                    InfoAttributeItem info = new InfoAttributeItem((short) msg.reader().readUnsignedByte(), msg.reader().readShort());
                    item.allAttribute.addElement(info);
                }
                ((Vector) ShopOnline.ALL_ITEM.elementAt(index)).addElement(item);
            }
            if (type == 0) {
                Canvas.shop.setItemInventory();
            }
            if (type == 3) {
                ShopOnline.money = msg.reader().readLong();
            }
            ShopOnline.nPage[index] = msg.reader().readShort();
            if (ShopOnline.indexPage[index] >= ShopOnline.nPage[index]) {
                ShopOnline.indexPage[index] = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTemplateInfo(Message msg) {
        try {
            short type_get = (short) msg.reader().readUnsignedByte();
            short id_template = msg.reader().readShort();
            if (type_get == 0) {
                byte[] infoMons = new byte[msg.reader().readByte()];
                msg.reader().read(infoMons, 0, infoMons.length);
                Res.loadMonster(id_template, infoMons);
                Res.monsterTemplates[id_template].name = msg.reader().readUTF();
                Res.monsterTemplates[id_template].he = msg.reader().readByte();
                Res.monsterTemplates[id_template].maxhp = msg.reader().readInt();
                Res.monsterTemplates[id_template].palate = msg.reader().readByte();
                Res.monsterTemplates[id_template].spalate = msg.reader().readByte();
                byte isnewMonster = msg.reader().readByte();
                Res.monsterTemplates[id_template].isNewMonster = isnewMonster;
                if (isnewMonster == 1) {
                    byte[] data = new byte[msg.reader().readShort()];
                    msg.reader().read(data, 0, data.length);
                    Res.monsterTemplates[id_template].doSetDataMonsterNew(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println("LOI MONSTER TEMPLATE");
        }
    }

    public void onPetAttack(Message msg) {
        try {
            short petID = msg.reader().readShort();
            byte typeattack = msg.reader().readByte();
            if (typeattack == 0) {
                short idSkill = msg.reader().readShort();
                int i = msg.reader().readByte();
                int[] arrdame = new int[i];
                int[] arrHpcon = new int[i];
                Vector nTarget = new Vector();
                Actor ac = Canvas.gameScr.findActorByID_Cat(petID, (byte) 12);
                if (ac != null) {
                    for (int i2 = 0; i2 < i; i2++) {
                        byte cat = msg.reader().readByte();
                        short idtarget = msg.reader().readShort();
                        Actor ac_target = Canvas.gameScr.findActorByID_Cat(idtarget, cat);
                        if (ac_target != null) {
                            nTarget.addElement(ac_target);
                        }
                        arrdame[i2] = msg.reader().readInt();
                        arrHpcon[i2] = msg.reader().readInt();
                    }
                    ac.petAttack(nTarget, typeattack, idSkill, arrdame, arrHpcon);
                }
            }
            if (typeattack == 1) {
                Actor owner = Canvas.gameScr.findActorByID(petID);
                short ideff = msg.reader().readShort();
                long timelive = msg.reader().readLong();
                if (owner != null) {
                    owner.addEffbuff(ideff, owner.x, owner.y, timelive);
                }
            }
        } catch (Exception e) {
        }
    }

    public void onDynamicEffect(Message msg) {
        try {
            byte type = msg.reader().readByte();
            short idEff = msg.reader().readShort();
            short idactor = msg.reader().readShort();
            long timeExist = msg.reader().readLong();
            int x = msg.reader().readByte() * 16;
            int y = msg.reader().readByte() * 16;
            byte cat = msg.reader().readByte();
            byte top_bottom = msg.reader().readByte();
            LiveActor c = (LiveActor) (cat == 0 ? findCharByID(idactor) : findMonsterByID(idactor));
            DynamicEffect d = new DynamicEffect(idEff);
            d.timeExist = timeExist;
            d.x = (short) x;
            d.y = (short) y;
            d.catagory = cat;
            d.stop = msg.reader().readByte();
            int add_remove = msg.reader().readByte();
            try {
                int dam = msg.reader().readInt();
                d.dam = dam;
            } catch (Exception e) {
            }
            if (c != null && type == 1) {
                if (top_bottom == 0) {
                    c.addDynamicEffBuffBottom(d, add_remove);
                    return;
                } else {
                    c.addDynamicEffBuffTop(d, add_remove);
                    return;
                }
            }
            if (idactor > 32000 || type == 0) {
                if (c != null) {
                    d.x = c.x;
                    d.y = c.y;
                }
                this.actors.addElement(d);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onEffData(Message msg) {
        try {
            byte type = msg.reader().readByte();
            switch (type) {
                case 0:
                    byte cat = msg.reader().readByte();
                    short idActor = msg.reader().readShort();
                    short idEff = msg.reader().readShort();
                    int time = msg.reader().readInt();
                    boolean canmove = msg.reader().readBoolean();
                    boolean canpaint = msg.reader().readBoolean();
                    long timelive = 0;
                    if (time > 0) {
                        timelive = ((long) time) + System.currentTimeMillis();
                    }
                    Actor ac = findActorByID_Cat(idActor, cat);
                    byte dyhorse = 0;
                    try {
                        dyhorse = msg.reader().readByte();
                        break;
                    } catch (Exception e) {
                    }
                    if (ac != null) {
                        ac.addEffectSkillTime(idEff, ac.x, ac.y, timelive, canmove, canpaint, true, 0, (byte) Res.random(1, 8), dyhorse);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e2) {
        }
    }

    public static void startSkillEffect(LiveActor c, int idSkill, long timeExist, int top_bottom, int power, int bymap) {
        DynamicEffect d = new DynamicEffect(idSkill);
        d.timeExist = timeExist;
        d.x = c.x;
        d.y = c.y;
        d.catagory = c.catagory;
        d.stop = (byte) 0;
        d.power = power;
        if (bymap == 0) {
            if (top_bottom == 0) {
                c.addDynamicEffBuffBottom(d, 0);
                return;
            } else {
                c.addDynamicEffBuffTop(d, 0);
                return;
            }
        }
        Canvas.gameScr.actors.addElement(d);
    }

    public static void paint12plus(Graphics g) {
        Canvas.resetTrans(g);
        if (img12plus == null) {
            return;
        }
        // 0.9.5: nhãn độ tuổi MỘT chỗ, nhỏ, trọn trong vùng an toàn; ẩn khi có bảng / menu / hộp thoại đang mở
        if (Canvas.currentDialog != null || Canvas.menu.showMenu || Canvas.isLoad) {
            return;
        }
        Screen m = Canvas.currentScreen;
        if (m != Canvas.gameScr && !(m instanceof LoginScr || m instanceof ServerListScr || m instanceof CharSelectScr
                || m instanceof CreateCharScr || m instanceof DangKyScr)) {
            return;   // bảng (Nhân vật, Menu, Cửa hàng...) / màn khác đang mở
        }
        int dy = Canvas.h - 6 - game.pokemon.Bang.anDuoi - 12;
        if (Canvas.currentScreen == Canvas.gameScr) {
            if (Canvas.gameScr.hideGUI == 2 || HudRong.banDoTo) {
                return;
            }
            dy = 40;
            dy += 4;
        }
        int x = 4 + game.pokemon.Bang.anTrai;
        int w = 20, h = 11;
        game.pokemon.GiaoDien.khoi(g, x, dy, w, h, 4, 0xE8412C);
        game.pokemon.ChuDe.chuGiua(g, "12+", x + w / 2, dy, h, game.pokemon.ChuDe.SO, 0xFFFFFF, 2);
    }
}
