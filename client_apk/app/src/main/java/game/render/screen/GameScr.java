package game.render.screen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.htbb.client.GameMidlet;
import effect.DynamicEffect;
import game.core.j2me.FontSys;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.Actor;
import game.model.AnimalMove;
import game.model.AnimateEffect;
import game.model.AttackResult;
import game.model.BossTL;
import game.model.BossThanLan;
import game.model.Boss_Dragon;
import game.model.Boss_Eagle;
import game.model.Boss_Eyes;
import game.model.Boss_Snake;
import game.model.Boss_TuongQuan;
import game.model.Boss_Xuong;
import game.model.CMLocation;
import game.model.Char;
import game.model.CharBuff;
import game.model.CharCountDown;
import game.model.CharPartInfo;
import game.model.ChatInfo;
import game.model.ChatPopup;
import game.model.ClanInfo;
import game.model.Command;
import game.model.CongThanh;
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
import game.model.SkillClan;
import game.model.Skill_Kham;
import game.model.SubEff;
import game.model.ThanThu;
import game.model.Tilemap;
import game.model.TimecountDown;
import game.model.Tree;
import game.model.TruTranThanh;
import game.model.WayPoint;
import game.model.XaphuTemplate;
import game.model.arrow.Arrow;
import game.model.arrow.IArrow;
import game.model.arrow.MagicBeam;
import game.model.arrow.WeaponsAngleBoss;
import game.model.arrow.WeaponsFire;
import game.model.arrow.WeaponsLazer;
import game.model.arrow.WeaponsMango;
import game.model.monster.CuongThi;
import game.model.monster.Pet;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.model.skill.Skill_Boss_Halloween;
import game.model.skill.Skill_Da_Roi;
import game.model.skill.Skill_Noel;
import game.networklogic.GameService;
import game.networklogic.ItemDropInfo;
import game.networklogic.MonsterDieInfo;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import network.Message;
import network.Session_ME;

public class GameScr extends Screen {
    private static final byte DELAY_BETWEEN_PING = 30;
    public static final byte GET_ITEM = 2;
    public static final byte KILL_MOSTER = 0;
    public static final byte MAX_INVENTORY = 42;
    public static final byte STATE_DONE = 3;
    public static final byte STATE_FINISH = 1;
    public static final byte STATE_UN_RECEIVE = 0;
    public static final byte STATE_WORKING = 2;
    public static final byte TRANSPORT = 1;
    public static boolean autoBomHMP = false;
    public static CharCountDown charcountdonw = null;
    public static Quest clanQuest = null;
    public static int cmdx = 0;
    public static int cmdxMini = 0;
    public static int cmdy = 0;
    public static int cmdyMini = 0;
    public static int cmtoX = 0;
    public static int cmtoXMini = 0;
    public static int cmtoY = 0;
    public static int cmtoYmini = 0;
    public static int cmvx = 0;
    public static int cmvxMini = 0;
    public static int cmvy = 0;
    public static int cmvyMini = 0;
    public static int cmx = 0;
    public static int cmxLim = 0;
    public static int cmxMini = 0;
    public static int cmy = 0;
    public static int cmyLim = 0;
    public static int cmyMini = 0;
    public static Actor congThanh = null;
    public static int[] curTimeCT = null;
    public static Vector effManager = null;
    public static int gscmdBarY = 0;
    public static int gsh = 0;
    public static int gsh2d3 = 0;
    public static int gsh3d4 = 0;
    public static int gshd3 = 0;
    public static int gshd6 = 0;
    public static int gshh = 0;
    public static int gshw = 0;
    public static int gsw = 0;
    public static int gsw2d3 = 0;
    public static int gsw3d4 = 0;
    public static int gswd3 = 0;
    public static int gswd6 = 0;
    public static byte hArena = 0;
    public static int hMiniMap = 0;
    public static short[] idArrMap = null;
    public static short[] idChiemThanh = null;
    public static Bitmap img12plus = null;
    public static Bitmap imgBtn1 = null;
    public static Bitmap imgBtn2 = null;
    public static Bitmap imgBtn5 = null;
    public static Bitmap imgBtn6 = null;
    public static Bitmap imgChat = null;
    public static Bitmap[] imgChatIcon = null;
    public static Bitmap[] imgCong = null;
    public static Bitmap imgFocusActor = null;
    public static Bitmap imgFunctionLight = null;
    public static Bitmap imgHpMonster = null;
    public static Bitmap imgInfo = null;
    public static Bitmap imgMapMini = null;
    public static Bitmap[] imgMenu = null;
    public static Bitmap imgMoveFocus = null;
    public static Bitmap imgParty = null;
    public static Bitmap imgRoundInfo = null;
    public static Bitmap imgSwapClothes = null;
    public static Bitmap imgSwapQuickSlot = null;
    public static Bitmap imgTarget = null;
    public static Bitmap[] imghealth = null;
    public static boolean isAutoBomHp = false;
    public static boolean isAutoBomMp = false;
    public static boolean isBeginAutoBuff = false;
    public static boolean isFight = false;
    public static boolean isFixBugAutoQuest = false;
    public static boolean isTarget = false;
    public static boolean lockcmx = false;
    public static boolean lockcmy = false;
    public static Quest mainQuest = null;
    public static byte[][] mapFind = null;
    public static short[][] mapID = null;
    public static String[] nameChiemThanh = null;
    public static String[] nameClan = null;
    public static String nameMap = "";
    public static Actor objcountDown;
    public static Bitmap petShadow;
    public static Position posNpcReceiveClan;
    public static boolean saveAutoFight;
    public static Quest subQuest;
    public static int[] timeChiemThanh;
    public static long timeDelayHpMp;
    public static int timeRemovePos;
    public static long timeStandWhenAuto;
    public static long timeTranMini;
    public static byte typechat;
    public static byte wArena;
    public static int wMiniMap;
    public static byte xArena;
    public static byte xStart;
    public static byte yArena;
    public static byte yStart;
    int HPBARW;
    int HPBARW_MONSTER;
    int MPBARW;
    public int btnFocus;
    public int btnFocusPotion;
    boolean changeSinceLastUpdate;
    int countChang;
    public int coutChangeFocusWhenDoing;
    int demA;
    public int[] flyTextColor;
    public int[] flyTextDx;
    public int[] flyTextDy;
    public int[] flyTextState;
    public String[] flyTextString;
    public int[] flyTextX;
    public int[] flyTextY;
    public Actor focusedActor;
    public int gssh;
    public int gssw;
    public int gssx;
    public int gssxe;
    public int gssy;
    public int gssye;
    public int iTaskAutoLast;
    public boolean isAutoRangeWhenAuto;
    public boolean isMini;
    public boolean isNewVersionAvailable;
    public boolean isPaint;
    public boolean isTouchChat;
    public boolean isTouchMenu;
    public boolean isViewMsgWorld;
    public int lapTarget;
    public long lastTimePing;
    long lastUpdateMyPositionTime;
    public Vector listMSGServer;
    public boolean onoffInvite;
    public long pingDelay;
    public Position posBoss;
    private Position posCam;
    private Position posMiniMap;
    public Position posNpcRequest;
    private Position[] posQuickSlot;
    Position posTo;
    public SkillClan[] skillClan;
    public SkillClan[] skillClanPrivate;
    int startMsg;
    public TField tfChat;
    TField tfMaso;
    TField tfSeri;
    public int timerung;
    byte xInfo;
    int xMsgServer;
    public int xT;
    byte yInfo;
    public int yT;
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
    public static String[] nameCountry = {"Kanto", "Johto"};
    public static byte typeOptionFocus = 0;
    static Random r = new Random(System.currentTimeMillis());
    public static short dtmove = 250;
    static final int[][] C = {new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}, new int[]{-90, 90, -90, 90}};
    static final byte[][] DX = {new byte[]{0, 0, -48, 48}, new byte[]{0, 0, -32, 32}, new byte[]{0, 0, -16, 16}, new byte[]{0, 0, 0, 0}};
    static final byte[][] DY = {new byte[]{48, -48, 0, 0}, new byte[]{32, -32, 0, 0}, new byte[]{16, -16, 0, 0}, new byte[]{0, 0, 0, 0}};
    public static long timeMove = System.currentTimeMillis();
    public static boolean autoFight = false;
    public static Quest currQuest = null;
    public static int timeDelayTask = 50;
    public static boolean cheat = false;
    private static int[] colorMini = {-9879000, -4879786, -2307918};
    public static boolean isNhanPhim = false;
    public static byte CHAT_NORMAL = 0;
    public static byte CHAT_TIM_BAN = 1;
    private static int[] colorSlot = {-13618895, -11272192, -16775094, -14740216, -16776960};
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
    public static byte paint18plush = 1;
    int[] CMVIBRATEX = {-1, 2, 1, -2};
    int[] CMVIBRATEY = {-3, 2, -1, 1};
    int currentCmVibrate = -1;
    public Vector actors = new Vector();
    public Vector arrowsDown = new Vector();
    public int hideGUI = 0;
    public MainChar mainChar = new MainChar();
    public boolean readyGetGameLogic = true;
    public boolean lockPosition = true;
    public boolean isFillScr = false;
    public Vector shop = new Vector();
    public Vector shopSpecial = new Vector();
    public Vector npc_limit = new Vector();
    public GameService gameService = GameService.gI();
    public short saveIDViewInfoAnimal = -1;
    public boolean chatMode = false;
    public boolean isBuffAuto = false;
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
    boolean isBatMiniMap = true;
    private int pingNextIn = 10;
    public int[] DELTACAMERAX = {0, 0, -20, 20};
    public int[] DELTACAMERAY = {20, -20, 0, 0};
    int leght = aeo.size();
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
    public boolean isChuyenMap = false;
    private long timeTaskAuto = 0;
    byte[] X_FOWARD = {0, 0, -17, 17};
    byte[] Y_FOWARD = {17, -17, 0, 0};
    int[] SLOTINDEXOFKEY = {0, 0, 0, 1, 0, 2, 0, 3, 0, 4};
    boolean first = false;
    int a = 0;
    int b = 0;
    boolean paintMapPopup = false;
    int countColor = 0;
    int[] colorPaint = {-256507, -277498, -16380673, -15675309};
    int xChat = 4;
    int yChat = 58;
    byte idInfo = 0;
    public Ticker tk = null;
    short test = 3;
    public short map = -1;
    public short xBeginFrame = 0;
    public short yBeginFrame = 0;
    public short xTheendFrame = 0;
    public short yTheendFrame = 0;
    public Vector decriptNap = new Vector();
    public Vector syntaxNap = new Vector();
    public Vector centerNap = new Vector();
    public Vector objDynamic = new Vector();
    public Vector VecTime = new Vector();

    public static int abs(int i) {
        return i > 0 ? i : -i;
    }

    public void onFinishQuest(Message message) {
    }

    public void onInfoQuestLogin(Message message) {
    }

    public void onLoginSuccess() {
    }

    @Override
    public void switchToMe() {
        GCanvas.keyHold[2] = false;
        super.switchToMe();
        init();
        this.isBuffAuto = false;
        this.isTouchMenu = false;
    }

    public void addChat(ChatInfo chatInfo) {
        if (this.chatHistory.size() > 50) {
            this.chatHistory.removeElementAt(0);
            this.yMsgChat -= 18;
            this.toYchat -= 18;
        }
        this.chatHistory.addElement(chatInfo);
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
            if (Integer.parseInt(flyString.substring(1)) == 2000000) {
                return;
            }
        } catch (Exception e) {
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
        for (int i = 0; i < 15; i++) {
            int[] iArr = this.flyTextState;
            int i2 = iArr[i];
            if (i2 != -1) {
                iArr[i] = i2 + Util.abs(this.flyTextDy[i]);
                int[] iArr2 = this.flyTextState;
                if (iArr2[i] > 30) {
                    iArr2[i] = -1;
                }
                int[] iArr3 = this.flyTextX;
                iArr3[i] = iArr3[i] + this.flyTextDx[i];
                int[] iArr4 = this.flyTextY;
                iArr4[i] = iArr4[i] + this.flyTextDy[i];
            }
        }
    }

    public final void paintFlyText(Graphics graphics) {
        int i = 0;
        if (GCanvas.screenlevel == 1) {
            while (i < 15) {
                if (this.flyTextState[i] != -1) {
                    if (this.flyTextColor[i] != 0) {
                        FontBitMap.smallFontColor[this.flyTextColor[i]].drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                    } else {
                        FontBitMap.smallFontYellow.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                    }
                }
                i++;
            }
            return;
        }
        while (i < 15) {
            if (this.flyTextState[i] != -1) {
                int i2 = this.flyTextColor[i];
                if (i2 == 0) {
                    FontSys.big.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                } else if (i2 == 1) {
                    Font.smallFont1.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                } else if (i2 == 2) {
                    Font.smallFontGREEN.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                } else if (i2 == 3) {
                    Font.smallFontBLUE.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                } else if (i2 == 4) {
                    Font.smallFontRED.drawString(graphics, this.flyTextString[i], this.flyTextX[i], this.flyTextY[i], 0);
                }
            }
            i++;
        }
    }

    @Override
    public void init() {
        this.tfChat.x = 10;
        this.tfChat.y = GCanvas.h - 60;
        this.tfChat.width = GCanvas.w - 20;
        if (this.posQuickSlot == null) {
            this.posQuickSlot = new Position[5];
            for (int i = 0; i < 5; i++) {
                this.posQuickSlot[i] = new Position();
            }
        }
        this.posQuickSlot[0].x = GCanvas.hw - 47;
        this.posQuickSlot[0].y = GCanvas.h - 19;
        this.posQuickSlot[1].x = GCanvas.hw - 28;
        this.posQuickSlot[1].y = GCanvas.h - 19;
        this.posQuickSlot[2].x = GCanvas.hw - 8;
        this.posQuickSlot[2].y = GCanvas.h - 20;
        this.posQuickSlot[3].x = GCanvas.hw + 12;
        this.posQuickSlot[3].y = GCanvas.h - 19;
        this.posQuickSlot[4].x = GCanvas.hw + 31;
        this.posQuickSlot[4].y = GCanvas.h - 19;
        setPosMiniMap();
        loadCamera();
        imgTarget = Image.loadImageFromAsset("target.png");
        imgSwapClothes = Image.loadImageFromAsset("swapclothes.png");
    }

    public GameScr() {
        this.tfChat = new TField();
        this.xMsgServer = 0;
        TField tField = new TField();
        this.tfChat = tField;
        tField.height = 21;
        this.tfChat.isFocus = true;
        this.tfChat.setMaxTextLenght(80);
        init();
        loadFlyText();
        setPosMiniMap();
        this.HPBARW = 45;
        this.MPBARW = 45;
        this.HPBARW_MONSTER = 50;
        this.xMsgServer = GCanvas.w - 20;
        this.isPaint = true;
    }

    public void setPosMiniMap() {
        if (Tilemap.imgMap == null) {
            return;
        }
        int i = GCanvas.h / 5;
        hMiniMap = i;
        if (i > 100) {
            hMiniMap = 100;
        }
        this.posMiniMap = new Position(GCanvas.hw + 50, 0);
        int i2 = (GCanvas.w - this.posMiniMap.x) - 1;
        wMiniMap = i2;
        if (i2 > 100) {
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
        this.posMiniMap.x = (GCanvas.w - wMiniMap) - 1;
        this.posMiniMap.y = 0;
    }

    public void loadCamera() {
        gsw = GCanvas.w;
        int i = GCanvas.h;
        gsh = i;
        int i2 = gsw;
        gshw = i2 >> 1;
        gshh = i >> 1;
        gswd3 = i2 / 3;
        gshd3 = i / 3;
        gsw2d3 = (i2 * 2) / 3;
        gsh2d3 = (i * 2) / 3;
        gsw3d4 = (i2 * 3) / 4;
        gsh3d4 = (i * 3) / 4;
        gswd6 = i2 / 6;
        gshd6 = i / 6;
        int i3 = i2 >> 4;
        this.gssw = i3 + 2;
        this.gssh = (i >> 4) + 2;
        if (i2 % 24 != 0) {
            this.gssw = i3 + 3;
        }
        cmxLim = ((Tilemap.w << 4) - gsw) - 10;
        cmyLim = ((Tilemap.h << 4) - gsh) - 30;
        MainChar mainChar = this.mainChar;
        if (mainChar != null) {
            int i4 = mainChar.x - gshw;
            cmtoX = i4;
            cmx = i4;
            int i5 = this.mainChar.y - gshh;
            cmtoY = i5;
            cmy = i5;
        }
        if (cmx < 0) {
            cmx = 0;
        }
        int i6 = cmx;
        int i7 = cmxLim;
        if (i6 > i7) {
            cmx = i7;
        }
        if (cmy < 0) {
            cmy = 0;
        }
        int i8 = cmy;
        int i9 = cmyLim;
        if (i8 > i9) {
            cmy = i9;
        }
        int i10 = (cmx >> 4) - 1;
        this.gssx = i10;
        if (i10 < 0) {
            this.gssx = 0;
        }
        int i11 = cmy >> 4;
        this.gssy = i11;
        this.gssxe = this.gssx + this.gssw + 1;
        int i12 = this.gssh + i11 + 1;
        this.gssye = i12;
        if (i11 < 0) {
            this.gssy = 0;
        }
        if (i12 > Tilemap.h - 1) {
            this.gssye = Tilemap.h - 1;
        }
        lockcmy = false;
        lockcmx = false;
        int i13 = Tilemap.pxw;
        int i14 = gsw;
        if (i13 < i14 + 32) {
            lockcmx = true;
            cmx = (-(i14 - Tilemap.pxw)) >> 1;
        }
        int i15 = Tilemap.pxh;
        int i16 = gsh;
        if (i15 < i16) {
            lockcmy = true;
            cmy = (-(i16 - Tilemap.pxh)) >> 1;
        }
    }

    public final void updateCamera() {
        int i = cmx;
        int i2 = cmtoX;
        if (i != i2 && !lockcmx) {
            int i3 = (i2 - i) << 2;
            cmvx = i3;
            int i4 = cmdx + i3;
            int i5 = i + (i4 >> 4);
            cmx = i5;
            cmdx = i4 & 15;
            if (i5 < 0) {
                cmx = 0;
            }
            int i6 = cmx;
            int i7 = cmxLim;
            if (i6 > i7) {
                cmx = i7;
            }
        }
        int i8 = cmy;
        int i9 = cmtoY;
        if (i8 != i9 && !lockcmy) {
            int i10 = (i9 - i8) << 2;
            cmvy = i10;
            int i11 = cmdy + i10;
            int i12 = i8 + (i11 >> 4);
            cmy = i12;
            cmdy = i11 & 15;
            if (i12 < 0) {
                cmy = 0;
            }
            int i13 = cmy;
            int i14 = cmyLim;
            if (i13 > i14) {
                cmy = i14;
            }
        }
        int i15 = this.currentCmVibrate;
        if (i15 >= 0) {
            int i16 = cmx + this.CMVIBRATEX[i15];
            cmx = i16;
            int i17 = cmy + this.CMVIBRATEY[i15];
            cmy = i17;
            int i18 = i15 + 1;
            this.currentCmVibrate = i18;
            if (i18 == 4) {
                this.currentCmVibrate = -1;
            }
            if (i16 < 0) {
                cmx = 0;
            }
            int i19 = cmx;
            int i20 = cmxLim;
            if (i19 > i20) {
                cmx = i20;
            }
            if (i17 < 0) {
                cmy = 0;
            }
            int i21 = cmy;
            int i22 = cmyLim;
            if (i21 > i22) {
                cmy = i22;
            }
        }
        int i23 = (cmx >> 4) - 1;
        this.gssx = i23;
        if (i23 < 0) {
            this.gssx = 0;
        }
        this.gssy = cmy >> 4;
        int i24 = this.gssx + this.gssw + 1;
        this.gssxe = i24;
        if (i24 > Tilemap.w) {
            this.gssxe = Tilemap.w;
        }
        if (this.gssye > Tilemap.h) {
            this.gssye = Tilemap.h;
        }
        int i25 = this.gssy;
        int i26 = this.gssh + i25 + 1;
        this.gssye = i26;
        if (i25 < 0) {
            this.gssy = 0;
        }
        if (i26 > Tilemap.h - 1) {
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
            TimecountDown timecountDown = (TimecountDown) this.VecTime.elementAt(i);
            if (timecountDown != null) {
                timecountDown.update();
                if (timecountDown.wantdestroy) {
                    this.VecTime.removeElement(timecountDown);
                }
            }
        }
    }

    @Override
    public void update() {
        // mod: mỗi khung hình chạy các auto (Auto up, Vận tiêu, Vận may, Lên thành, Tiện ích, chế đồ...)
        PauseMenu.autoCheDoTick();
        int i = timeRemovePos - 1;
        timeRemovePos = i;
        if (i < 0) {
            timeRemovePos = 0;
        }
        for (int i2 = 0; i2 < this.leght; i2++) {
            ((Effect) aeo.elementAt(i2)).update();
            if (((Effect) aeo.elementAt(i2)).wantDetroy) {
                aeo.removeElementAt(i2);
            }
        }
        Ticker ticker = this.tk;
        if (ticker != null) {
            ticker.update();
            if (this.tk.end) {
                this.tk = null;
            }
        }
        updateTime();
        CharCountDown charCountDown = charcountdonw;
        if (charCountDown != null) {
            charCountDown.update();
            if (charcountdonw.WantDestroy) {
                charcountdonw = null;
                objcountDown = null;
            }
        }
        updateCamera();
        cmtoX = (this.mainChar.x - gshw) + this.DELTACAMERAX[this.mainChar.dir];
        cmtoY = ((this.mainChar.y - gshh) + this.DELTACAMERAY[this.mainChar.dir]) - 20;
        updateCmMini();
        for (int size = arrowsUp.size() - 1; size >= 0; size--) {
            IArrow iArrow = (IArrow) arrowsUp.elementAt(size);
            iArrow.update();
            if (iArrow.wantDestroy) {
                arrowsUp.removeElementAt(size);
            }
        }
        for (int size2 = this.arrowsDown.size() - 1; size2 >= 0; size2--) {
            ((IArrow) this.arrowsDown.elementAt(size2)).update();
        }
        for (int size3 = this.npc_limit.size() - 1; size3 >= 0; size3--) {
            Actor actor = (Actor) this.npc_limit.elementAt(size3);
            if (actor != null && actor.typeLimit == 1) {
                actor.update();
            }
        }
        PosNPCQuest.removeAllElements();
        Vector vector = new Vector();
        for (int size4 = this.actors.size() - 1; size4 >= 0; size4--) {
            Actor actor2 = (Actor) this.actors.elementAt(size4);
            if (actor2.isNPC()) {
                getAllPosNPCMInimap(actor2);
            }
            if (actor2.ispetTool() && actor2.wantDestroy) {
                vector.addElement(actor2);
            }
            if (actor2.catagory == 2 || actor2.catagory == 10 || actor2.catagory == 11 || actor2.catagory == 12) {
                actor2.update();
            } else {
                actor2.update();
                if (!Util.inDataRange(actor2, this.mainChar) && actor2 != congThanh && actor2.catagory != 127 && actor2.catagory != 0 && actor2.timeLive == -1) {
                    actor2.wantDestroy = true;
                }
                if (actor2.wantDestroy && actor2.timeLive == -1) {
                    if (this.focusedActor == actor2) {
                        this.focusedActor = null;
                    }
                    if (!isFindChar && !isActorMove && !isFindMonster && actor2.catagory != 100) {
                        if (actor2.catagory == 0) {
                            actor2.isDie = true;
                        }
                        vector.addElement(actor2);
                    }
                }
            }
        }
        Actor actor3 = congThanh;
        if (actor3 != null) {
            actor3.update();
        }
        Util.quickSort(this.actors);
        if (GCanvas.gameTick % 10 == 0) {
            this.focusedActor = findFocusActor();
        }
        Actor actor4 = this.focusedActor;
        if (actor4 != null && actor4.catagory == 100) {
            this.focusedActor = null;
        }
        EffectManager.lowEffects.updateAll();
        EffectManager.hiEffects.updateAll();
        updateFlyText();
        int i3 = this.pingNextIn;
        if (i3 > 0) {
            int i4 = i3 - 1;
            this.pingNextIn = i4;
            if (i4 == 0) {
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
        int i5 = xSlot;
        if (i5 > 0) {
            int i6 = i5 - (Res.wKhung / 10);
            xSlot = i6;
            if (i6 < 0) {
                xSlot = 0;
            }
        }
        if ((Tilemap.w >= wMiniMap || Tilemap.h >= hMiniMap) && (System.currentTimeMillis() / 100) - timeTranMini > 20) {
            updateminiMap();
        }
        try {
            if (this.posNpcRequest != null && iTaskAuto == 2) {
                updateTask();
                GCanvas.endDlg();
            }
        } catch (Exception unused) {
            this.mainChar.posTransRoad = null;
            this.mainChar.countRoad = 0;
        }
        try {
            if (effAnimate.size() > 0) {
                AnimateEffect.updateWind();
                int size5 = effAnimate.size();
                for (int i7 = 0; i7 < size5; i7++) {
                    ((AnimateEffect) effAnimate.elementAt(i7)).update();
                }
            }
        } catch (Exception unused2) {
        }
        if (effManager != null) {
            for (int i8 = 0; i8 < effManager.size(); i8++) {
                ((EffectServerManager) effManager.elementAt(i8)).update();
            }
        }
        for (int i9 = 0; i9 < vector.size(); i9++) {
            this.actors.removeElement(vector.elementAt(i9));
        }
        updateMsgServer();
        setRemoveIconImg();
        setRemoveClothes();
        SkillClan[] skillClanArr = this.skillClan;
        if (skillClanArr != null && skillClanArr[0].time > 0) {
            this.skillClan[0].updateTime();
        }
        int i10 = this.timerung;
        if (i10 > 0) {
            this.timerung = i10 - 1;
        } else {
            this.timerung = 0;
        }
        if (Tilemap.isOfflineMap && this.map == 29) {
            if (this.mainChar.x >= this.xBeginFrame - 16 && this.mainChar.x <= this.xTheendFrame + 40 && this.mainChar.y >= this.yBeginFrame - 16 && this.mainChar.y <= this.yTheendFrame + 40) {
                this.focusedActor = isFocusMyGround(this.mainChar);
            } else {
                int size6 = this.actors.size();
                for (int i11 = 0; i11 < size6; i11++) {
                    Actor actor5 = (Actor) this.actors.elementAt(i11);
                    if (actor5.catagory == 10) {
                        actor5.isFocus = false;
                    }
                }
            }
        }
        if (msgWorld.size() > 0) {
            ((MsgInfo) msgWorld.elementAt(0)).update();
        }
        if (savemsgWorld.size() > 30) {
            savemsgWorld.removeElementAt(0);
        }
        int i12 = this.lapTarget + 1;
        this.lapTarget = i12;
        if (i12 > 100) {
            this.lapTarget = 0;
        }
        if (Math.abs(this.mainChar.x - this.xT) < 10 && Math.abs(this.mainChar.y - this.yT) < 10) {
            isTarget = false;
        }
        if (this.mainChar.state != 3 && autoFight && (GCanvas.currentScreen instanceof GameScr) && GCanvas.currentDialog != null && !this.mainChar.isDie && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80) {
            GCanvas.currentDialog = null;
        }
        updateMainCharWhenAuto();
        if (Tilemap.lv == 0 || Tilemap.lv == 70 || Tilemap.lv == 80) {
            autoFight = false;
        }
        if (GCanvas.gameTick % 200 == 0) {
            Char.SetRemove();
        }
        if (isFixBugAutoQuest) {
            isFixBugAutoQuest = false;
            MainChar mainChar = this.mainChar;
            mainChar.xTo = mainChar.x;
            MainChar mainChar2 = this.mainChar;
            mainChar2.yTo = mainChar2.y;
            MainChar mainChar3 = this.mainChar;
            mainChar3.xBegin = mainChar3.x;
            MainChar mainChar4 = this.mainChar;
            mainChar4.yBegin = mainChar4.y;
            int i13 = this.countChang + 1;
            this.countChang = i13;
            if (i13 > this.xChange.length - 1) {
                this.countChang = 0;
            }
            int i14 = (Tilemap.w / 2) + this.xChange[this.countChang];
            int i15 = (Tilemap.h / 2) + this.xChange[this.countChang];
            MainChar mainChar5 = this.mainChar;
            mainChar5.posTransRoad = updateFindRoad(mainChar5.x / 16, this.mainChar.y / 16, i14, i15);
            this.mainChar.countRoad = 0;
            iTaskAuto = this.iTaskAutoLast;
        }
    }

    public Actor isFocusMyGround(LiveActor liveActor) {
        Actor actor = null;
        if (Tilemap.isOfflineMap) {
            int size = this.actors.size();
            for (int i = 0; i < size; i++) {
                Actor actor2 = (Actor) this.actors.elementAt(i);
                if (actor2.catagory == 10) {
                    if (liveActor.x >= actor2.x + 2 && liveActor.x <= actor2.x + 30 && liveActor.y >= actor2.y && liveActor.y + 2 <= actor2.y + 30) {
                        actor2.isFocus = true;
                        actor = actor2;
                    } else {
                        actor2.isFocus = false;
                    }
                }
            }
        }
        return actor;
    }

    private void updateMsgServer() {
        Vector vector = this.listMSGServer;
        if (vector == null || vector.size() <= 0) {
            return;
        }
        this.xMsgServer -= 4;
        if (this.xMsgServer + Font.arialFont11.getWidth((String) this.listMSGServer.elementAt(0)) < 0) {
            this.listMSGServer.removeElementAt(0);
            this.xMsgServer = GCanvas.w - 20;
            this.isPaint = false;
        }
    }

    private void setRemoveIconImg() {
        Enumeration enumerationKeys = GameData.listImgIcon.keys();
        while (enumerationKeys.hasMoreElements()) {
            String str = (String) enumerationKeys.nextElement();
            ImageIcon imageIcon = (ImageIcon) GameData.listImgIcon.get(str);
            if (!imageIcon.isLoad && (System.currentTimeMillis() / 1000) - ((long) imageIcon.timeRemove) > 120) {
                GameData.listImgIcon.remove(str);
            }
        }
    }

    public void onLoadCloth(byte[] bArr, byte b, byte b2) {
        ((CharPartInfo) ((Hashtable) Res.quanao.elementAt(b)).get(((int) b2) + "")).load(bArr, b, b2);
    }

    private void setRemoveClothes() {
        for (int size = Res.quanao.size() - 1; size >= 0; size--) {
            Hashtable hashtable = (Hashtable) Res.quanao.elementAt(size);
            Enumeration enumerationKeys = hashtable.keys();
            while (enumerationKeys.hasMoreElements()) {
                String str = (String) enumerationKeys.nextElement();
                if ((System.currentTimeMillis() / 1000) - ((long) ((CharPartInfo) hashtable.get(str + "")).timeRemove) > 60) {
                    hashtable.remove(str);
                }
            }
        }
    }

    private void updateCmMini() {
        if (Tilemap.w >= wMiniMap || Tilemap.h >= hMiniMap) {
            int i = cmyMini;
            int i2 = cmtoYmini;
            if (i != i2) {
                int i3 = (i2 - i) << 2;
                cmvyMini = i3;
                int i4 = cmdyMini + i3;
                cmyMini = i + (i4 >> 4);
                cmdyMini = i4 & 15;
            }
            int i5 = cmxMini;
            int i6 = cmtoXMini;
            if (i5 != i6) {
                int i7 = (i6 - i5) << 2;
                cmvxMini = i7;
                int i8 = cmdxMini + i7;
                cmxMini = i5 + (i8 >> 4);
                cmdxMini = i8 & 15;
            }
        }
    }

    public Actor findFocusActor() {
        int iAbs;
        short s = this.mainChar.x;
        int[][] iArr = C;
        int i = s + iArr[this.mainChar.dir][0];
        byte b = 1;
        int i2 = this.mainChar.x + iArr[this.mainChar.dir][1];
        int i3 = this.mainChar.y + iArr[this.mainChar.dir][2];
        int i4 = this.mainChar.y + iArr[this.mainChar.dir][3];
        this.nearActors.removeAllElements();
        int size = this.actors.size();
        int i5 = 10000;
        int i6 = 0;
        int i7 = -1;
        while (i6 < size) {
            Actor actor = (Actor) this.actors.elementAt(i6);
            if (actor.x > i && actor.x < i2 && actor.y > i3 && actor.y < i4) {
                if (actor.catagory == 127 || !actor.canfocus || ((actor.catagory == 0 && actor.ID == this.mainChar.ID) || actor.catagory == 10 || actor.catagory == 11 || actor.catagory == 12 || (autoFight && !GCanvas.autoTab.isAutoFire && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80 && !Tilemap.isOfflineMap && (actor.isItemCanGet() || actor.catagory == 126 || actor.catagory == 2 || actor.catagory == 0)))) {
                    if ((autoFight || autoBomHMP) && actor.catagory == 2) {
                        addChat(this.mainChar, "Bạn hãy tắt auto trước khi focus", 100);
                    }
                } else if ((actor.catagory != b || actor.state != 8) && (!autoFight || (actor.state != 3 && !actor.isDropItem()))) {
                    byte b2 = typeOptionFocus;
                    if (b2 == b) {
                        if ((actor.catagory != 0 || (actor.iskiller && !actor.isNPC())) && (actor.catagory == b || actor.catagory != 0 || actor.iskiller)) {
                            this.nearActors.addElement(actor);
                            iAbs = Util.abs(this.mainChar.x - actor.x) + Util.abs(this.mainChar.y - actor.y);
                            if (actor.catagory != 3 || actor.catagory == 4) {
                                iAbs <<= 1;
                            }
                            if (iAbs < i5) {
                                i5 = iAbs;
                                i7 = i6;
                            }
                        }
                    } else if (b2 == 2) {
                        if (actor.isNPC()) {
                            this.nearActors.addElement(actor);
                            iAbs = Util.abs(this.mainChar.x - actor.x) + Util.abs(this.mainChar.y - actor.y);
                            if (actor.catagory != 3) {
                                iAbs <<= 1;
                            } else {
                                iAbs <<= 1;
                            }
                            if (iAbs < i5) {
                                i5 = iAbs;
                                i7 = i6;
                            }
                        }
                    } else if (b2 == 3) {
                        if ((actor.catagory != 0 || actor.idClan != this.mainChar.idClan) && !actor.isNPC()) {
                            this.nearActors.addElement(actor);
                            iAbs = Util.abs(this.mainChar.x - actor.x) + Util.abs(this.mainChar.y - actor.y);
                            if (actor.catagory != 3) {
                                iAbs <<= 1;
                            } else {
                                iAbs <<= 1;
                            }
                            if (iAbs < i5) {
                                i5 = iAbs;
                                i7 = i6;
                            }
                        }
                    } else if (b2 != 4 || ((actor.catagory != 0 || actor.nationID != this.mainChar.nationID) && !actor.isNPC())) {
                        this.nearActors.addElement(actor);
                        iAbs = Util.abs(this.mainChar.x - actor.x) + Util.abs(this.mainChar.y - actor.y);
                        if (actor.catagory != 3) {
                            iAbs <<= 1;
                        } else {
                            iAbs <<= 1;
                        }
                        if (iAbs < i5) {
                            i5 = iAbs;
                            i7 = i6;
                        }
                    }
                }
            }
            i6++;
            b = 1;
        }
        if (i7 == -1) {
            return null;
        }
        if (autoFight && GCanvas.autoTab.isAutoFire) {
            for (int i8 = 0; i8 < this.nearActors.size(); i8++) {
                Actor actor2 = (Actor) this.nearActors.elementAt(i8);
                if (actor2 != null && actor2.iskiller) {
                    return actor2;
                }
            }
        }
        if (this.focusedActor == null && i7 < this.actors.size()) {
            return (Actor) this.actors.elementAt(i7);
        }
        if (!GCanvas.instance.hasPointerEvents() && i7 < this.actors.size()) {
            if (this.nearActors.contains(this.focusedActor)) {
                return this.focusedActor;
            }
            return (Actor) this.actors.elementAt(i7);
        }
        if (Util.distance(this.mainChar.x, this.mainChar.y, this.focusedActor.x, this.focusedActor.y) > GCanvas.w / 2) {
            this.focusedActor = null;
        }
        return this.focusedActor;
    }

    public void onStarDialLucky(int i) {
        DialLuckyScr.gI().switchToMe();
        DialLuckyScr.gI().lastScr = this;
    }

    public Actor getActor(int i) {
        for (int i2 = 0; i2 < this.actors.size(); i2++) {
            Actor actor = (Actor) this.actors.elementAt(i2);
            if (actor != null && actor.catagory == 0 && actor.ID == i) {
                return actor;
            }
        }
        return null;
    }

    public void changeToNextFocusActor(boolean z) {
        Actor actor = this.focusedActor;
        if (actor == null) {
            Actor actorFindFocusActor = findFocusActor();
            this.focusedActor = actorFindFocusActor;
            if (actorFindFocusActor == null || actorFindFocusActor.catagory != 100) {
                return;
            }
            this.focusedActor = null;
            return;
        }
        if (z && !actor.iskiller && autoFight && typeOptionFocus == 1) {
            Actor actorFindFocusActor2 = findFocusActor();
            this.focusedActor = actorFindFocusActor2;
            if (actorFindFocusActor2 != null && actorFindFocusActor2.iskiller) {
                return;
            }
        }
        int iIndexOf = this.nearActors.indexOf(this.focusedActor) + 1;
        if (iIndexOf >= this.nearActors.size() || iIndexOf < 0) {
            iIndexOf = 0;
        }
        if (this.nearActors.size() > 0) {
            this.focusedActor = (Actor) this.nearActors.elementAt(iIndexOf);
        }
        Actor actor2 = this.focusedActor;
        if (actor2 == null || actor2.catagory != 100) {
            return;
        }
        this.focusedActor = null;
    }

    public static void addChat(Actor actor, String str, int i) {
        actor.chat = new ChatPopup(i, str, 1);
        actor.chat.setPos(actor.x, actor.y - actor.height);
    }

    private boolean doTalkWithNPCQuest() {
        if (!this.mainChar.receiveQuest || this.mainChar.typeQuest != 1 || !this.receiveQuest) {
            return false;
        }
        if (GCanvas.isKeyPressed(5) || GCanvas.isKeyPressed(12)) {
            int i = this.indexContent + 1;
            this.indexContent = i;
            if (i <= this.mainChar.info_npc_quest.length - 1) {
                this.mainChar.chat = null;
                this.focusedActor.chat = null;
                if (this.mainChar.info_npc_quest[this.indexContent].startsWith("1")) {
                    addChat(this.focusedActor, this.mainChar.info_npc_quest[this.indexContent].substring(1), 500);
                } else {
                    MainChar mainChar = this.mainChar;
                    addChat(mainChar, mainChar.info_npc_quest[this.indexContent].substring(1), 500);
                }
            } else {
                this.receiveQuest = false;
                MainChar mainChar2 = this.mainChar;
                mainChar2.nextNpc = (byte) (mainChar2.nextNpc + 1);
                if (this.mainChar.nextNpc >= this.mainChar.npc_quest.length) {
                    this.gameService.talk_npc(this.mainChar.npc_quest[this.mainChar.nextNpc - 1], 1, this.questID);
                    dellPotionQuest();
                }
            }
        }
        return true;
    }

    private boolean doTalkWithNPCResponse() {
        if (!this.finishQuest || !this.mainChar.finishQuest || this.mainChar.nextNpc >= this.mainChar.npc_response.length) {
            return false;
        }
        if (GCanvas.isKeyPressed(5) || GCanvas.isKeyPressed(12)) {
            int i = this.indexContent + 1;
            this.indexContent = i;
            if (i <= this.mainChar.info_response[this.mainChar.nextNpc].length - 1) {
                this.mainChar.chat = null;
                this.focusedActor.chat = null;
                if (this.mainChar.info_response[this.mainChar.nextNpc][this.indexContent].startsWith("1")) {
                    addChat(this.focusedActor, this.mainChar.info_response[this.mainChar.nextNpc][this.indexContent].substring(1), 500);
                } else {
                    MainChar mainChar = this.mainChar;
                    addChat(mainChar, mainChar.info_response[this.mainChar.nextNpc][this.indexContent].substring(1), 500);
                }
            } else {
                this.indexContent = 0;
                MainChar mainChar2 = this.mainChar;
                mainChar2.nextNpc = (byte) (mainChar2.nextNpc + 1);
                if (this.mainChar.nextNpc < this.mainChar.npc_response.length) {
                    this.mainChar.chat = null;
                    this.focusedActor.chat = null;
                    this.finishQuest = false;
                    set_npc_request(this.mainChar.npc_response[this.mainChar.nextNpc]);
                } else {
                    this.gameService.talk_npc_response_quest(this.mainChar.npc_response[this.mainChar.nextNpc - 1], 1, this.questID);
                    this.finishQuest = false;
                }
            }
        }
        return true;
    }

    public void set_npc_request(short s) {
        short s2 = this.idNpcReceive;
        if (s2 != -1) {
            s = s2;
        } else {
            this.current_npc_talk = s;
        }
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if ((actor instanceof NPC) && ((NPC) actor).type == s) {
                this.posNpcRequest = new Position(actor.x, actor.y);
                return;
            }
        }
    }

    public void paintPosNPCQuest(Graphics graphics) {
        for (int i = 0; i < PosNPCQuest.size(); i++) {
            Position position = (Position) PosNPCQuest.elementAt(i);
            paintPoint(graphics, position, position.indexColor);
        }
    }

    private boolean doReceiveQuest() {
        if (!this.receiveQuest) {
            return false;
        }
        if (GCanvas.isKeyPressed(5) || GCanvas.isKeyPressed(12)) {
            int i = this.indexContent + 1;
            this.indexContent = i;
            if (i <= this.content_quest.length - 1) {
                this.mainChar.chat = null;
                this.focusedActor.chat = null;
                if (this.content_quest[this.indexContent].startsWith("1")) {
                    addChat(this.focusedActor, this.content_quest[this.indexContent].substring(1), 500);
                } else {
                    addChat(this.mainChar, this.content_quest[this.indexContent].substring(1), 500);
                }
                if (this.indexContent == this.content_quest.length - 1) {
                    Vector vector = new Vector();
                    vector.addElement(new Command("Đồng ý", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().receiveQuest(GameScr.this.idNpcReceive, GameScr.this.questID, 1);
                            GameScr.this.receiveQuest = false;
                        }
                    }));
                    if (this.mainChar.typeQuest != 3) {
                        vector.addElement(new Command("Không", new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.receiveQuest = false;
                                GameService.gI().receiveQuest(GameScr.this.idNpcReceive, GameScr.this.questID, 2);
                            }
                        }));
                    }
                    GCanvas.menu.startAt(vector, 3);
                }
            }
        }
        return true;
    }

    private boolean doIntro() {
        int i;
        if (Tilemap.lv == 105) {
            String[] strArr = {"1Cháu của ta", "1Nay con đã lớn", "1đã đến lúc con phải được học hỏi nhiều hơn", "1phải trải nghiệm nhiều hơn từ quê hương đất nước", "1và có nhiều điều để con phải học hỏi nơi biết bao nhà huấn luyện tài ba.", "1Năm xưa ta có người bạn thân là Xatu", "1ông ấy giờ đang là trưởng làng của làng này.", "1con hãy đi xuống phương Nam và tìm gặp người này", "1ông ấy sẽ giúp con gia nhập làng huấn luyện.", "1Con hãy cố gắng để trưởng thành, để có thể giúp được quê hương", "1để có được sự nghiệp riêng và để tìm được nguồn gốc và cha mẹ của con…", "1Hãy lên đường đi", "1..nơi đây ta luôn dõi theo bước chân của con", "0Vâng! thưa nội con đi", "1Uhm, chúc con lên đường bình an."};
            Actor actor = this.focusedActor;
            if ((actor == null || !(actor instanceof NPC)) && GCanvas.currentDialog == null) {
                GCanvas.keyHold[2] = true;
                this.indexContent = -1;
                doActorMove();
            } else {
                GCanvas.keyHold[2] = false;
                if (Util.abs(this.mainChar.y - this.focusedActor.y) <= 32) {
                    if (this.indexContent == -1) {
                        GCanvas.keyPressed[5] = true;
                    }
                    if (GCanvas.isPointerClick) {
                        GCanvas.isPointerClick = false;
                        int i2 = cmx + GCanvas.px;
                        int i3 = cmy + GCanvas.py;
                        if (Util.abs(this.focusedActor.x - i2) < 20 && Util.abs((this.focusedActor.y - 20) - i3) < 40) {
                            GCanvas.keyPressed[5] = true;
                        }
                    }
                    if (GCanvas.isKeyPressed(5) && (i = this.indexContent) < 15) {
                        int i4 = i + 1;
                        this.indexContent = i4;
                        if (i4 >= 15) {
                            this.mainChar.state = (byte) 0;
                            Random random = new Random(System.currentTimeMillis());
                            GameService.gI().changeMap(0, (random.nextInt() % 5) + 13, (random.nextInt() % 5) + 11);
                            GCanvas.keyHold[2] = false;
                            GCanvas.startWaitDlg("Chuyển màn..", true);
                        } else {
                            this.mainChar.chat = null;
                            this.focusedActor.chat = null;
                            if (strArr[this.indexContent].startsWith("1")) {
                                addChat(this.focusedActor, strArr[this.indexContent].substring(1), 500);
                            } else {
                                addChat(this.mainChar, strArr[this.indexContent].substring(1), 700);
                            }
                        }
                    }
                    return true;
                }
                GCanvas.keyHold[2] = true;
                this.indexContent = -1;
                doActorMove();
            }
        }
        return false;
    }

    private void doActorMove() {
        short s;
        boolean z;
        boolean z2;
        if (GCanvas.isKeyPressed(2)) {
            this.dir = (byte) 1;
        }
        if (GCanvas.isKeyPressed(4)) {
            this.dir = (byte) 2;
        }
        if (GCanvas.isKeyPressed(6)) {
            this.dir = (byte) 3;
        }
        if (GCanvas.isKeyPressed(8)) {
            this.dir = (byte) 4;
        }
        short s2 = this.mainChar.x;
        short s3 = this.mainChar.y;
        short s4 = this.mainChar.dir;
        short s5 = this.mainChar.speed;
        short s6 = -16;
        if (GCanvas.keyHold[2]) {
            this.mainChar.stop = false;
            removeFocusWhenPutKey();
            this.btnFocus = 2;
            autoFight = false;
            z = congThanh == null || this.mainChar.y > congThanh.y + 40;
            z2 = z;
            this.mainChar.dir = (short) 1;
            this.moveToX = this.mainChar.x;
            short s7 = (short) (this.mainChar.y - s5);
            this.moveToY = s7;
            try {
                if (Tilemap.tileTypeAtPixel(this.moveToX, s7, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                    this.moveToY = this.mainChar.y;
                    if (this.mainChar.setWay(0, -8)) {
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        return;
                    }
                    z = false;
                }
            } catch (Exception e) {
                this.moveToY = this.mainChar.y;
                e.printStackTrace();
            }
            s = -16;
        } else {
            if (GCanvas.keyHold[8]) {
                this.mainChar.stop = false;
                removeFocusWhenPutKey();
                this.btnFocus = 8;
                autoFight = false;
                this.mainChar.dir = (short) 0;
                this.moveToX = this.mainChar.x;
                short s8 = (short) (this.mainChar.y + s5);
                this.moveToY = s8;
                try {
                    if (Tilemap.tileTypeAtPixel(this.moveToX, s8, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                        this.moveToY = this.mainChar.y;
                        if (this.mainChar.setWay(0, 8)) {
                            this.changeSinceLastUpdate = true;
                            this.mainChar.comeHome = false;
                            return;
                        }
                        z = false;
                    } else {
                        z = true;
                    }
                    z2 = true;
                } catch (Exception e2) {
                    this.moveToY = this.mainChar.y;
                    e2.printStackTrace();
                    z = true;
                    z2 = true;
                }
                s = 16;
            } else {
                if (GCanvas.keyHold[4]) {
                    this.mainChar.stop = false;
                    removeFocusWhenPutKey();
                    this.btnFocus = 4;
                    autoFight = false;
                    this.mainChar.dir = (short) 2;
                    this.moveToX = (short) (this.mainChar.x - s5);
                    short s9 = this.mainChar.y;
                    this.moveToY = s9;
                    try {
                        if (Tilemap.tileTypeAtPixel(this.moveToX, s9, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                            this.moveToX = this.mainChar.x;
                            if (this.mainChar.setWay(-8, 0)) {
                                this.changeSinceLastUpdate = true;
                                this.mainChar.comeHome = false;
                                return;
                            }
                            z = false;
                        } else {
                            z = true;
                        }
                        z2 = true;
                    } catch (Exception e3) {
                        this.moveToX = this.mainChar.x;
                        e3.printStackTrace();
                        z = true;
                        z2 = true;
                    }
                } else if (GCanvas.keyHold[6]) {
                    this.mainChar.stop = false;
                    removeFocusWhenPutKey();
                    this.btnFocus = 6;
                    autoFight = false;
                    this.mainChar.dir = (short) 3;
                    this.moveToY = this.mainChar.y;
                    short s10 = (short) (this.mainChar.x + s5);
                    this.moveToX = s10;
                    try {
                        if (Tilemap.tileTypeAtPixel(s10, this.moveToY, 2) || checkMoveLimit(this.moveToX, this.moveToY)) {
                            this.moveToX = this.mainChar.x;
                            if (this.mainChar.setWay(8, 0)) {
                                this.changeSinceLastUpdate = true;
                                this.mainChar.comeHome = false;
                                return;
                            }
                            z = false;
                        } else {
                            z = true;
                        }
                        z2 = true;
                    } catch (Exception e4) {
                        this.moveToX = this.mainChar.x;
                        e4.printStackTrace();
                        z = true;
                        z2 = true;
                    }
                    s6 = 16;
                } else {
                    s = 0;
                    z = false;
                    z2 = false;
                    s6 = 0;
                }
                s = 0;
            }
            if (this.mainChar.canMove()) {
                if (z2 || !checkCanChangeMap(s2 + s6, s3 + s, s4)) {
                    if (z) {
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
                            GCanvas.gameScr.mainChar.lastXAuto = this.moveToX;
                            GCanvas.gameScr.mainChar.lastYAuto = this.moveToY;
                        }
                        this.changeSinceLastUpdate = true;
                        this.mainChar.comeHome = false;
                        this.countStep = (byte) (this.countStep + 1);
                    }
                    if (System.currentTimeMillis() - timeMove >= 0 || this.mainChar.stop) {
                    }
                    timeMove = System.currentTimeMillis() + ((long) dtmove);
                    this.gameService.moveChar(this.moveToX, this.moveToY);
                    return;
                }
                return;
            }
            return;
        }
        s6 = 0;
        if (this.mainChar.canMove()) {
            return;
        }
        if (z2) {
        }
        if (z) {
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
            if (saveAutoFight) {
                GCanvas.gameScr.mainChar.lastXAuto = this.moveToX;
                GCanvas.gameScr.mainChar.lastYAuto = this.moveToY;
            }
            this.changeSinceLastUpdate = true;
            this.mainChar.comeHome = false;
            this.countStep = (byte) (this.countStep + 1);
        }
        if (System.currentTimeMillis() - timeMove >= 0) {
        }
    }

    public boolean checkMoveLimit(int i, int i2) {
        if (this.npc_limit.size() == 0) {
            return false;
        }
        int size = this.npc_limit.size();
        for (int i3 = 0; i3 < size; i3++) {
            Actor actor = (Actor) this.npc_limit.elementAt(i3);
            if (actor != null) {
                Npc_Server npc_Server = (Npc_Server) actor;
                if (i >= npc_Server.getLimxL() && i <= npc_Server.getLimxR() && i2 >= npc_Server.getLimyU() && i2 <= npc_Server.getLimyD()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void chatFast() {
        String strTrim = this.tfChat.getText().trim();
        this.tfChat.setText("");
        if (strTrim.trim() != "") {
            doChatFromMe(strTrim);
        }
        GCanvas.clearKeyHold();
        GCanvas.clearKeyPressed();
        this.chatMode = false;
        GCanvas.isPointerClick = false;
        this.isTouchChat = false;
    }

    public void removeFocusWhenPutKey() {
        if (this.map == 17 && this.mainChar.isDoing) {
            this.focusedActor = null;
            this.coutChangeFocusWhenDoing = 0;
        }
    }

    @Override
    public void updateKey() {
        boolean z;
        if (this.chatMode && GCanvas.isPointerClick) {
            int iCollisionCmdBar = GCanvas.collisionCmdBar();
            if (iCollisionCmdBar == 0 || iCollisionCmdBar == 1) {
                return;
            }
            if (iCollisionCmdBar == 2) {
                GCanvas.clearKeyHold();
                GCanvas.clearKeyPressed();
                this.chatMode = false;
                GCanvas.isPointerClick = false;
                return;
            }
        }
        if (GCanvas.isPointerClick && GCanvas.isPointer(GCanvas.w - 55, (GCanvas.h - (GCanvas.hh / 2)) - 11, 50, 70)) {
            this.btnFocus = 5;
            GCanvas.keyPressed[5] = true;
        }
        if (GCanvas.isHpUp && xacDinhToaDo(GCanvas.px, GCanvas.py, GCanvas.w - 62, (GCanvas.h - (GCanvas.hh / 2)) - 27, 40, 40)) {
            GCanvas.isHpUp = false;
            this.btnFocusPotion = 7;
            GCanvas.keyPressed[7] = true;
        }
        if (GCanvas.isHpUp && GCanvas.isPointer(GCanvas.w - 45, (GCanvas.h - (GCanvas.hh / 2)) - 55, 40, 40)) {
            GCanvas.isHpUp = false;
            this.btnFocusPotion = 9;
            GCanvas.keyPressed[9] = true;
        }
        if (GCanvas.keyPressed[11]) {
            cheat = !cheat;
        }
        if (doIntro() || this.chatMode) {
            return;
        }
        Quest quest = currQuest;
        if (quest != null && quest.isShow) {
            currQuest.update(this.mainChar, this.focusedActor);
            return;
        }
        if (doTalkWithNPCQuest() || doTalkWithNPCResponse() || doReceiveQuest()) {
            return;
        }
        long j = 1000;
        if (idChiemThanh != null) {
            int i = 0;
            z = false;
            while (true) {
                short[] sArr = idChiemThanh;
                if (i >= sArr.length) {
                    break;
                }
                if (sArr[i] == this.mainChar.ID && ((long) timeChiemThanh[i]) - ((System.currentTimeMillis() / j) - ((long) curTimeCT[i])) >= 0) {
                    z = true;
                }
                i++;
                j = 1000;
            }
        } else {
            z = false;
        }
        if (GCanvas.isPointerClick) {
            if (GCanvas.isPoint(this.xChat, this.yChat, 30, 30)) {
                GCanvas.gameScr.chatMode = true;
                GCanvas.gameScr.tfChat.doChangeToTextBox();
                GCanvas.isPointerClick = false;
            }
            if (GCanvas.isPoint((GCanvas.w / 2) - (imgMenu[0].getWidth() / 2), (GCanvas.h - imgMenu[0].getHeight()) - 5, imgMenu[0].getWidth(), imgMenu[0].getHeight() + 10)) {
                MenuSelectItem.gI().switchToMe();
                this.isTouchMenu = true;
                GCanvas.isPointerClick = false;
            }
        }
        if (!z) {
            updatePoint();
        }
        super.updateKey();
        if (this.mainChar.justStopFromRun) {
            this.mainChar.justStopFromRun = false;
            this.changeSinceLastUpdate = true;
            this.lastUpdateMyPositionTime = 0L;
            this.mainChar.stop = false;
            sendActorMove(this.mainChar.x, this.mainChar.y);
            this.changeSinceLastUpdate = false;
        }
        if (GCanvas.isKeyPressed(10)) {
            doParty();
            GCanvas.clearKeyHold();
            return;
        }
        if (GCanvas.isKeyPressed(11)) {
            Vector vector = new Vector();
            vector.addElement(new Command("Chat", new IAction() {
                @Override
                public void perform() {
                    GameScr.typechat = GameScr.CHAT_NORMAL;
                    GCanvas.gameScr.chatMode = true;
                    GameScr.this.tfChat.setIputType(0);
                    GCanvas.endDlg();
                }
            }));
            vector.addElement(new Command("Thay đồ", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().setConfig(6);
                    GCanvas.endDlg();
                }
            }));
            GCanvas.menu.startAt(vector, 3);
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
            Actor actor = this.focusedActor;
            if (actor == null || actor.catagory != 1 || this.focusedActor.ID != this.mainChar.idMonster) {
                this.mainChar.autoFight = false;
                this.mainChar.idMonster = 40000;
            } else if (this.mainChar.state == 0) {
                GCanvas.keyPressed[this.QUICKSLOT_KEY[this.mainChar.idSkill]] = true;
                MainChar mainChar = this.mainChar;
                mainChar.idSkill = (byte) ((mainChar.idSkill + 1) % 3);
            }
            if (autoBomHMP && ((GCanvas.autoTab.isAutoMHP || GCanvas.autoTab.isAll) && timeDelayHpMp - (System.currentTimeMillis() / 1000) <= 0)) {
                int i2 = (this.mainChar.hp * 100) / this.mainChar.maxhp;
                int i3 = (this.mainChar.mp * 100) / this.mainChar.maxmp;
                if (i2 < BangAuto.bomHp && this.mainChar.hp < this.mainChar.maxhp) {
                    isAutoBomHp = true;
                }
                if (i3 < BangAuto.bomMp && this.mainChar.mp < this.mainChar.maxmp) {
                    isAutoBomMp = true;
                }
                if (i2 >= BangAuto.bomHp) {
                    isAutoBomHp = false;
                }
                if (i3 >= BangAuto.bomMp) {
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
            if (autoFight && ((GCanvas.autoTab.isAutoFire || GCanvas.autoTab.isAll) && ((long) BangAuto.tgDelay) - (System.currentTimeMillis() / 100) <= 0)) {
                if (this.mainChar.isDoing) {
                    Actor actor2 = this.focusedActor;
                    if (actor2 != null && actor2.catagory != 2 && this.focusedActor.catagory != 0 && this.focusedActor.catagory != 126) {
                        GCanvas.keyPressed[5] = true;
                    } else {
                        changeToNextFocusActor(false);
                    }
                } else {
                    Actor actor3 = this.focusedActor;
                    if (actor3 != null && actor3.catagory != 2 && this.focusedActor.catagory != 126) {
                        if (this.focusedActor.catagory == 0 && !this.focusedActor.iskiller) {
                            changeToNextFocusActor(false);
                        } else {
                            boolean[] zArr = GCanvas.keyPressed;
                            int i4 = this.dem;
                            zArr[i4] = true;
                            int i5 = i4 + 2;
                            this.dem = i5;
                            if (i5 > 5) {
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
        }
        int i6 = 0;
        while (true) {
            int[] iArr = this.QUICKSLOT_KEY;
            if (i6 >= iArr.length) {
                break;
            }
            if (GCanvas.isKeyPressed(iArr[i6])) {
                if (!this.mainChar.beStune) {
                    doFire(this.QUICKSLOT_KEY[i6], indexTabSlot);
                    break;
                }
                break;
            }
            i6++;
        }
        if (GCanvas.menu.showMenu) {
            GCanvas.keyPressed[5] = false;
            return;
        }
        if ((GCanvas.isKeyPressed(2) || GCanvas.isKeyPressed(4) || GCanvas.isKeyPressed(6) || GCanvas.isKeyPressed(8)) && this.mainChar.state == 3) {
            this.mainChar.stop = false;
        } else if (this.mainChar.state == 1 || this.mainChar.state == 0 || this.mainChar.state == 4) {
            doActorMove();
        }
    }

    public boolean isBuffHoisinh() {
        byte[] bArr = {1, 3, 5};
        for (int i = 0; i < 3; i++) {
            QuickSlot quickSlot = MainChar.quickSlot[1][this.SLOTINDEXOFKEY[bArr[i]]];
            if (this.mainChar.clazz == 2 && quickSlot.getSkillType() == 6) {
                return true;
            }
        }
        return false;
    }

    private void updateTask() {
        if (this.mainChar.posTransRoad == null) {
            if (Char.skillLevelLearnt[0] == 0) {
                Position position = this.posNpcRequest;
                if (position != null && position.x / 16 == 6 && this.posNpcRequest.y / 16 == 7) {
                    this.posNpcRequest.x = 160;
                }
                if (Util.distance(this.mainChar.x, this.mainChar.y, this.posNpcRequest.x, this.posNpcRequest.y) > 32) {
                    MainChar mainChar = this.mainChar;
                    mainChar.posTransRoad = updateFindRoad(mainChar.x / 16, this.mainChar.y / 16, this.posNpcRequest.x / 16, (this.posNpcRequest.y / 16) + 2);
                    this.mainChar.countRoad = 0;
                    return;
                }
                this.mainChar.dir = (short) 1;
                if ((System.currentTimeMillis() / 100) - this.timeTaskAuto > timeDelayTask) {
                    timeDelayTask = 3;
                    int size = this.actors.size();
                    for (int i = 0; i < size; i++) {
                        Actor actor = (Actor) this.actors.elementAt(i);
                        if ((actor instanceof NPC) && ((NPC) actor).type == this.idNpcReceive) {
                            this.focusedActor = actor;
                            break;
                        }
                    }
                    this.timeTaskAuto = System.currentTimeMillis() / 100;
                    if (GCanvas.menu.showMenu) {
                        GCanvas.menu.selected = 0;
                        GCanvas.menu.center.action.perform();
                        GCanvas.menu.showMenu = false;
                        return;
                    }
                    GCanvas.keyPressed[5] = true;
                    return;
                }
                return;
            }
            iTaskAuto = 0;
        }
    }

    public boolean checkCanChangeMap(int i, int i2, int i3) {
        CMLocation otherMapAt;
        try {
            otherMapAt = Tilemap.toOtherMapAt(i + this.X_FOWARD[i3], i2 + this.Y_FOWARD[i3]);
        } catch (Exception unused) {
            otherMapAt = null;
        }
        if (otherMapAt == null) {
            return false;
        }
        ChangScr.gI().setMap(this.map, otherMapAt.toX, otherMapAt.toY);
        this.mainChar.state = (byte) 0;
        if (!Tilemap.isOfflineMap) {
            if (!isOffLineMap(otherMapAt.toM)) {
                short indexArrMap = getIndexArrMap();
                final String[] nameMap2 = Res.getNameMap(indexArrMap, otherMapAt.toM);
                final short[] location = Res.getLocation(indexArrMap, otherMapAt.toM);
                Vector vector = new Vector();
                for (int i40 = 0; i40 < nameMap2.length; i40++) {
                    final int i4 = i40;
                    if (!nameMap2[i4].equals("")) {
                        vector.addElement(new Command(nameMap2[i4], new IAction() {
                            @Override
                            public void perform() {
                                short[] sArr = location;
                                int i5 = i4;
                                GameService.gI().changeMap(location[i4 * 3], sArr[(i5 * 3) + 1], sArr[(i5 * 3) + 2]);
                                ChangScr.gI().switchToMe();
                                Res.removeMonsterTemplet();
                                GameScr.nameMap = nameMap2[i4];
                            }
                        }));
                    }
                }
                GCanvas.menu.startAt(vector, 3);
                GCanvas.menu.setIndex();
                return true;
            }
            oldMap = this.map;
            GameService.gI().changeMap(otherMapAt.toM, otherMapAt.toX, otherMapAt.toY);
            ChangScr.gI().switchToMe();
            Res.removeMonsterTemplet();
            return true;
        }
        GameService.gI().changeMap(oldMap, otherMapAt.toX, otherMapAt.toY);
        ChangScr.gI().switchToMe();
        Res.removeMonsterTemplet();
        return true;
    }

    private void doFire(int i, int i2) {
        if (this.mainChar.state == 3) {
            GCanvas.msgdlg.isIcon = false;
            this.indexBuff = -1;
            timeBuff = 0L;
            this.mainChar.resetBuff();
            boolean z = saveAutoFight;
            if (z) {
                autoFight = z;
            }
            if (BangAuto.isAutoComeHome && autoFight) {
                GameService.gI().comeHomeWhenDie();
                ChangScr.gI().switchToMe();
                ChangScr.gI().setMap(this.map, 0, 0);
                saveAutoFight = false;
                autoFight = false;
                return;
            }
            GCanvas.msgdlg.setInfo("Quay về làng.", new Command("Về làng", new IAction() {
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
                    GCanvas.currentDialog = null;
                }
            }), new Command("Đóng", new IAction() {
                @Override
                public void perform() {
                    GCanvas.currentDialog = null;
                }
            }));
            GCanvas.currentDialog = GCanvas.msgdlg;
            return;
        }
        this.mainChar.stand = System.currentTimeMillis();
        if (this.mainChar.state == 3) {
            GCanvas.msgdlg.isIcon = false;
            this.indexBuff = -1;
            timeBuff = 0L;
            this.mainChar.resetBuff();
            boolean z2 = saveAutoFight;
            if (z2) {
                autoFight = z2;
            }
            if (BangAuto.isAutoComeHome && autoFight) {
                GameService.gI().comeHomeWhenDie();
                ChangScr.gI().switchToMe();
                ChangScr.gI().setMap(this.map, 0, 0);
                saveAutoFight = false;
                autoFight = false;
                return;
            }
            GCanvas.msgdlg.setInfo("Quay về làng.", new Command("Về làng", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().comeHomeWhenDie();
                    ChangScr.gI().switchToMe();
                    ChangScr.gI().setMap(GameScr.this.map, 0, 0);
                    GameScr.saveAutoFight = false;
                    GameScr.autoFight = false;
                }
            }), (Command) null, new Command("Hồi sinh", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().doCustomPopup(GameScr.this.mainChar.ID, (byte) -1, "", 1);
                    GCanvas.currentDialog = null;
                }
            }));
            GCanvas.currentDialog = GCanvas.msgdlg;
            return;
        }
        Actor actor = this.focusedActor;
        if (actor != null && actor.isNpcChar()) {
            Vector allQuestNpc = getAllQuestNpc(this.focusedActor.getTypeNpc());
            if (allQuestNpc.size() > 0 && this.focusedActor.isNpcChar()) {
                allQuestNpc.addElement(new Command("Nói chuyện", new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestNPCInfo(((Char) GameScr.this.focusedActor).idBoss);
                    }
                }));
                GCanvas.menu.startAt(allQuestNpc, 3);
                return;
            } else {
                GameService.gI().requestNPCInfo(((Char) this.focusedActor).idBoss);
                return;
            }
        }
        QuickSlot quickSlot = MainChar.quickSlot[i2][this.SLOTINDEXOFKEY[i]];
        if (quickSlot.quickslotType != 2) {
            if (quickSlot.isBuff && !(this.focusedActor instanceof NPC)) {
                if (MainChar.skillLevelLearnt[quickSlot.getSkillType()] <= 0) {
                    return;
                }
                byte b = SkillManager.EFF_BUFF_SKILL[GCanvas.gameScr.mainChar.clazz][quickSlot.getSkillType() - 4];
                int skillMP = SkillManager.getSkillMP(quickSlot.getSkillType(), Char.skillLevelLearnt[quickSlot.getSkillType() - 4], GCanvas.gameScr.mainChar.clazz);
                if (skillMP > GCanvas.gameScr.mainChar.mp) {
                    if (this.first) {
                        return;
                    }
                    addChat(new ChatInfo("", "Không đủ MP", 0));
                    this.first = true;
                    return;
                }
                this.first = false;
                Actor actor2 = this.focusedActor;
                if (actor2 != null && actor2.catagory == 0) {
                    if (quickSlot.getSkillType() == 6) {
                        GameService.gI().useBuff(this.focusedActor.ID, (byte) 0, b, (short) 0);
                        this.mainChar.mp -= skillMP;
                        return;
                    } else {
                        this.mainChar.mp -= skillMP;
                        this.indexBuff = quickSlot.getSkillType();
                        WindowInfoScr.gI().doCastBuffToActor(this.mainChar, this.focusedActor, b, SkillManager.SKILL_CAN_BUFF_TO_USER[this.mainChar.clazz][quickSlot.getSkillType() + (-4)] == 1);
                        return;
                    }
                }
                if (quickSlot.getSkillType() != 6) {
                    this.mainChar.mp -= skillMP;
                    if (quickSlot.isBuff && (GCanvas.gameScr.indexBuff == -1 || GCanvas.gameScr.indexBuff == quickSlot.getSkillType())) {
                        GCanvas.gameScr.indexBuff = quickSlot.getSkillType();
                    }
                    long jCurrentTimeMillis = System.currentTimeMillis() - this.mainChar.timeLastUseSkills[quickSlot.getSkillType()];
                    if (autoFight && jCurrentTimeMillis <= GCanvas.gameScr.mainChar.coolDownSkill[quickSlot.getSkillType()] && timeBuff - (System.currentTimeMillis() / 1000) < 0 && this.isBuffAuto) {
                        jCurrentTimeMillis = GCanvas.gameScr.mainChar.coolDownSkill[quickSlot.getSkillType()] + 1;
                        this.isBuffAuto = false;
                    }
                    if (jCurrentTimeMillis > GCanvas.gameScr.mainChar.coolDownSkill[quickSlot.getSkillType()]) {
                        GameService.gI().useBuff(GCanvas.gameScr.mainChar.ID, (byte) 0, b, (short) 0);
                        this.mainChar.coolDownSkill[quickSlot.getSkillType()] = SkillManager.getSkillCooldown(quickSlot.getSkillType(), this.mainChar.clazz, MainChar.skillLevelLearnt[quickSlot.getSkillType()]);
                        this.mainChar.timeLastUseSkills[quickSlot.getSkillType()] = System.currentTimeMillis();
                        return;
                    }
                    return;
                }
                GCanvas.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
                return;
            }
            Actor actor3 = this.focusedActor;
            if (actor3 != null && actor3.catagory == 2 && MainChar.skillLevelLearnt[quickSlot.getSkillType()] >= 0) {
                doUseSkillToFocusActor(quickSlot.getSkillType());
            }
            if (this.focusedActor == null) {
                return;
            }
            long jCurrentTimeMillis2 = System.currentTimeMillis() - this.mainChar.timeLastUseSkills[quickSlot.getSkillType()];
            if (this.focusedActor.catagory == 2 || jCurrentTimeMillis2 <= GCanvas.gameScr.mainChar.coolDownSkill[quickSlot.getSkillType()]) {
                return;
            }
            if (MainChar.skillLevelLearnt[quickSlot.getSkillType()] > 0 || this.focusedActor.catagory == 10) {
                doUseSkillToFocusActor(quickSlot.getSkillType());
                return;
            }
            return;
        }
        if (quickSlot.quickslotType == 2) {
            doUsePotion(quickSlot.getPotionType());
        }
    }

    public void doUsePotion(int i) {
        int i2 = 94;
        if (GCanvas.gameScr.mainChar.potions[i] <= 0) {
            int[] iArr = {94, 93, 22, 21, 3, 2, 1};
            int[] iArr2 = {96, 95, 24, 23, 6, 5, 4};
            int i3 = 0;
            while (i3 < MainChar.quickSlot[indexTabSlot].length) {
                if (MainChar.quickSlot[indexTabSlot][i3].quickslotType == 2 && MainChar.quickSlot[indexTabSlot][i3].getPotionType() == i) {
                    MainChar.quickSlot[indexTabSlot][i3].setIsNothing();
                    if (i == 1 || i == 2 || i == 3 || i == 21 || i == 22 || i == 93 || i == i2) {
                        int i4 = 1;
                        while (true) {
                            if (i4 > 3) {
                                for (int i5 = 0; i5 < 7; i5++) {
                                    if (GCanvas.gameScr.mainChar.potions[iArr[i5]] > 0) {
                                        MainChar.quickSlot[indexTabSlot][i3].setIsPotion(iArr[i5]);
                                        break;
                                    }
                                }
                                break;
                            }
                            if (i4 != i && GCanvas.gameScr.mainChar.potions[i4] > 0) {
                                MainChar.quickSlot[indexTabSlot][i3].setIsPotion(i4);
                                break;
                            }
                            i4++;
                        }
                    }
                    if (i == 4 || i == 5 || i == 6 || i == 23 || i == 24 || i == 95 || i == 96) {
                        for (int i6 = 4; i6 <= 6; i6++) {
                            if (i6 != i && GCanvas.gameScr.mainChar.potions[i6] > 0) {
                                MainChar.quickSlot[indexTabSlot][i3].setIsPotion(i6);
                                break;
                            }
                        }
                        for (int i7 = 0; i7 < 7; i7++) {
                            if (GCanvas.gameScr.mainChar.potions[iArr2[i7]] > 0) {
                                MainChar.quickSlot[indexTabSlot][i3].setIsPotion(iArr2[i7]);
                                break;
                            }
                        }
                    }
                }
                i3++;
                i2 = 94;
            }
            RMS.saveQuickSlot();
            return;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (jCurrentTimeMillis - this.mainChar.potionLastTimeUse[i] < MainChar.listPotion[i].delay) {
            return;
        }
        if (this.mainChar.hp < this.mainChar.maxhp || !(i == 1 || i == 2 || i == 3 || i == 21 || i == 22 || i == 93 || i == 94)) {
            if (this.mainChar.mp < this.mainChar.maxmp || !(i == 4 || i == 5 || i == 6 || i == 23 || i == 24 || i == 95 || i == 96)) {
                if (i == 19) {
                    if (this.map != 0) {
                        GCanvas.gameScr.mainChar.timeComeHome = System.currentTimeMillis();
                        GCanvas.gameScr.mainChar.comeHome = true;
                        GCanvas.gameScr.mainChar.state = (byte) 4;
                    } else {
                        GCanvas.startOKDlg("Không thể sử dụng trong làng.");
                        return;
                    }
                }
                if (i < 10 || i > 20) {
                    int[] iArr3 = this.mainChar.potions;
                    iArr3[i] = iArr3[i] - 1;
                    this.mainChar.potionLastTimeUse[i] = jCurrentTimeMillis;
                }
                if (this.mainChar.state != 3 && autoFight && !this.mainChar.isDie && Tilemap.lv != 0 && Tilemap.lv != 201 && Tilemap.lv != 70 && Tilemap.lv != 80 && !Tilemap.isOfflineMap) {
                    this.mainChar.hp += getMpHpPutKey(0, i);
                    this.mainChar.mp += getMpHpPutKey(1, i);
                    if (this.mainChar.hp >= this.mainChar.maxhp) {
                        MainChar mainChar = this.mainChar;
                        mainChar.hp = mainChar.maxhp;
                    }
                    if (this.mainChar.mp >= this.mainChar.maxmp) {
                        MainChar mainChar2 = this.mainChar;
                        mainChar2.mp = mainChar2.maxmp;
                    }
                }
                GameService.gI().usePotion(i);
            }
        }
    }

    public int getMpHpPutKey(int i, int i2) {
        int i3 = 0;
        while (true) {
            byte[] bArr = TYPE_MP_HP[i];
            if (i3 >= bArr.length) {
                return 0;
            }
            if (bArr[i3] == i2) {
                return VALUE_MP_HP[i][i3];
            }
            i3++;
        }
    }

    public void doParty() {
        try {
            Actor actor = this.focusedActor;
            if (actor != null) {
                if (actor.catagory == 0) {
                    Vector vector = new Vector();
                    vector.addElement(new Command("Mời party", new IAction() {
                        @Override
                        public void perform() {
                            try {
                                if (((Char) GameScr.this.focusedActor).pk <= 0 && !((Char) GameScr.this.focusedActor).iskiller) {
                                    if (GameScr.this.mainChar.IDParty == -1) {
                                        GameScr.this.gameService.requestCreateParty(GameScr.this.mainChar.ID);
                                        GameScr.tempIDActorParty = GameScr.this.focusedActor.ID;
                                        return;
                                    } else {
                                        GameScr.this.gameService.invite2Party(GameScr.this.focusedActor.ID, 0);
                                        return;
                                    }
                                }
                                GCanvas.startOKDlg("Không thể mời người trong trạng thái đánh nhau hoặc phạm tội");
                            } catch (Exception unused) {
                                Cout.println("Loi moi party");
                            }
                        }
                    }));
                    vector.addElement(new Command("Trao đổi", new IAction() {
                        @Override
                        public void perform() {
                            if (GameScr.this.mainChar.isTrade) {
                                return;
                            }
                            GameScr.this.gameService.trade(GameScr.this.focusedActor.ID);
                        }
                    }));
                    vector.addElement(new Command("Mời đám cưới", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.invite2Party(GameScr.this.focusedActor.ID, 3);
                        }
                    }));
                    if (this.focusedActor instanceof Char) {
                        vector.addElement(new Command("Nhắn tin", new IAction() {
                            @Override
                            public void perform() {
                                MessageScr.gI().addTab("", ((Char) GameScr.this.focusedActor).name, 0);
                                MessageScr.gI().setFocusTab(((Char) GameScr.this.focusedActor).name);
                                MessageScr.gI().switchToMe();
                            }
                        }));
                    }
                    vector.addElement(new Command("Kết bạn", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.gameService.sendInfoAddFriend(((Char) GameScr.this.focusedActor).ID, 0);
                        }
                    }));
                    if ((GCanvas.gameScr.mainChar.isMaster == 0 || GCanvas.gameScr.mainChar.isMaster == 1 || GCanvas.gameScr.mainChar.isMaster == 2) && ((Char) this.focusedActor).idClan == -1) {
                        vector.addElement(new Command("Mời vào đội", new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.gameService.sendRequestAddClan(GameScr.this.focusedActor.ID, (byte) 0, false);
                            }
                        }));
                    }
                    if (((Char) this.focusedActor).idClan != -1) {
                        vector.addElement(new Command("Xem thông tin đội", new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.gameService.requestClanInfo(((Char) GameScr.this.focusedActor).idClan);
                            }
                        }));
                    }
                    vector.addElement(new Command("Xem thông tin", new IAction() {
                        @Override
                        public void perform() {
                            GCanvas.startWaitDlg();
                            if (GameScr.this.focusedActor != null) {
                                GameScr gameScr = GameScr.this;
                                gameScr.saveIDViewInfoAnimal = gameScr.focusedActor.ID;
                                GameScr.this.gameService.requestSomeOneInfo(GameScr.this.focusedActor.ID, (byte) 0);
                            }
                        }
                    }));
                    vector.addElement(new Command("Xuống thú cưỡi", new IAction() {
                        @Override
                        public void perform() {
                            GameScr.this.unRideHorse();
                        }
                    }));
                    GCanvas.menu.startAt(vector, 2);
                    return;
                }
                GCanvas.keyPressed[5] = false;
                return;
            }
            unRideHorse();
        } catch (Exception unused) {
            Cout.println("Loi party");
        }
    }

    public void unRideHorse() {
        if (this.mainChar.useHorse != -1 || this.mainChar.idhorse > -1) {
            GCanvas.startYesNoDlg("Thú cưỡi sẽ bị mất nếu rương đồ không còn chỗ. Bạn có muốn xuống thú cưỡi không ?", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.unRideHorse((byte) 0);
                    GCanvas.currentDialog = null;
                }
            });
        }
    }

    protected Vector getNearMonster(Actor actor, int i) {
        Vector vector = new Vector();
        vector.addElement(actor);
        int size = this.actors.size();
        for (int i2 = 0; i2 < size; i2++) {
            Actor actor2 = (Actor) this.actors.elementAt(i2);
            if (actor2.catagory == 1 && actor2.state != 8 && actor2.ID != actor.ID && abs(actor2.x - actor.x) <= i && abs(actor2.y - actor.y) <= i) {
                vector.addElement(actor2);
                if (vector.size() > 10) {
                    break;
                }
            }
        }
        return vector;
    }

    private boolean isMapPK() {
        return Tilemap.lv == 112 || Tilemap.lv == 2541 || Tilemap.lv == 2542 || Tilemap.lv == 2543 || Tilemap.lv == 2544;
    }

    private boolean isChiemThanh() {
        return ((this.mainChar.x > xArena * 16 && this.mainChar.y > yArena * 16 && this.mainChar.x < wArena * 16 && this.mainChar.y < hArena * 16) || isFight) && this.focusedActor.catagory != 2 && Tilemap.lv == 201;
    }

    private boolean isFocusKiller() {
        return ((Char) this.focusedActor).iskiller;
    }

    public static void addviewInfo(Actor actor, String str, int i) {
        actor.chat = new ChatPopup(i, str, 0);
        actor.chat.setPos(actor.x, actor.y);
    }

    public void doViewInfoGround(String str, Actor actor) {
        actor.chat = null;
        addviewInfo(actor, str, 50);
    }

    public void addMsgWorld(String str) {
        MsgInfo msgInfo = new MsgInfo(str);
        msgWorld.addElement(msgInfo);
        savemsgWorld.addElement(msgInfo);
    }

    public void sendActorMove(int i, int i2) {
        this.mainChar.stop = false;
        if (System.currentTimeMillis() - timeMove >= 0) {
            timeMove = System.currentTimeMillis() + ((long) dtmove);
            this.gameService.moveChar(i, i2);
        }
    }

    public void doUseSkillToFocusActor(byte b) {
        byte b2;
        boolean z;
        Vector vector;
        String str;
        this.mainChar.getSkillIDFromType(b);
        try {
            Actor actor = this.focusedActor;
            if (actor != null) {
                if (autoFight && (actor.isNPC() || this.focusedActor.isNpcChar())) {
                    return;
                }
                GameScr gameScr = GCanvas.gameScr;
                boolean z2 = true;
                if (iTaskAuto != 2 && timeRemovePos <= 0 && this.mainChar.posTransRoad != null && autoFight && typeOptionFocus == 1) {
                    timeRemovePos = 100;
                    changeToNextFocusActor(true);
                    findRoad2(this.mainChar.lastXAuto, this.mainChar.lastYAuto);
                }
                if (this.focusedActor.isMonster() || ((this.mainChar.nationID != this.focusedActor.nationID && this.focusedActor.isPlayer()) || ((this.focusedActor.isPlayer() && ((idMapPK == Tilemap.lv && this.mainChar.idClan != ((Char) this.focusedActor).idClan) || (this.focusedActor.pk > 0 && this.mainChar.pk > 0 && this.focusedActor.pk != this.mainChar.pk))) || ((this.mainChar.iskiller && this.focusedActor.isPlayer()) || ((this.focusedActor.isPlayer() && (isChiemThanh() || isMapPK() || isFocusKiller())) || ((this.focusedActor.isPlayer() && ((Char) this.focusedActor).isAnimal) || (this.mainChar.isAnimal && (this.focusedActor.isMonster() || this.focusedActor.isPlayer())))))))) {
                    if (this.focusedActor.isMonster() && this.focusedActor.isMineral()) {
                        this.mainChar.getSkillIDFromType((byte) 0);
                        z = true;
                        b2 = 0;
                    } else {
                        b2 = b;
                        z = false;
                    }
                    if (!this.focusedActor.isPlayer() || !this.focusedActor.isNPC()) {
                        int skillMP = SkillManager.getSkillMP(b2, MainChar.skillLevelLearnt[b2], this.mainChar.clazz);
                        if (skillMP > this.mainChar.mp) {
                            if (this.first) {
                                return;
                            }
                            addChat(new ChatInfo("", "Không đủ MP", 0));
                            this.first = true;
                            return;
                        }
                        if (autoFight) {
                            this.mainChar.idMonster = this.focusedActor.ID;
                            this.mainChar.autoFight = true;
                        }
                        this.first = false;
                        LiveActor liveActor = (LiveActor) this.focusedActor;
                        if ((liveActor.realHP <= 0 || liveActor.hp <= 0) && liveActor.catagory == 0) {
                            return;
                        }
                        int skillRange = SkillManager.getSkillRange(b2, this.mainChar.clazz);
                        if (z) {
                            skillRange = 30;
                        }
                        if (Util.distance(this.mainChar.x, this.mainChar.y, liveActor.x, liveActor.y) > skillRange) {
                            this.coutChangeFocusWhenDoing++;
                            moveToRange(liveActor, skillRange);
                        } else {
                            this.isAutoRangeWhenAuto = false;
                            this.coutChangeFocusWhenDoing = 0;
                            if (this.mainChar.iskiller && this.focusedActor.catagory == 0 && ((Char) this.focusedActor).level < 10 && this.mainChar.nationID == this.focusedActor.nationID) {
                                return;
                            }
                            if (this.focusedActor.catagory == 1 && ((Monster) this.focusedActor).state == 5) {
                                return;
                            }
                            if (System.currentTimeMillis() - this.mainChar.timeLastUseSkills[b2] > this.mainChar.coolDownSkill[b2]) {
                                MainChar mainChar = this.mainChar;
                                mainChar.dir = Util.findDirection(mainChar, liveActor);
                                AttackResult attackResult = new AttackResult();
                                attackResult.dam = 2000000;
                                this.mainChar.mp -= skillMP;
                                this.mainChar.startAttack(liveActor, attackResult, b2, MainChar.skillLevelLearnt[b2]);
                                boolean zIsSkillAeo = SkillManager.isSkillAeo(this.mainChar.clazz, b2);
                                Vector vector2 = new Vector();
                                if (!zIsSkillAeo) {
                                    vector = vector2;
                                } else if (liveActor.catagory == 1) {
                                    SkillAnimate skillAnimate = this.mainChar.currentSkillAnimate;
                                    Vector nearMonster = getNearMonster(liveActor, SkillManager.getRangeSkillAeo(this.mainChar.clazz, b2));
                                    skillAnimate.setMonster(nearMonster);
                                    vector = nearMonster;
                                } else {
                                    if (liveActor.catagory == 0) {
                                        vector2.addElement(liveActor);
                                        this.mainChar.currentSkillAnimate.setMonster(vector2);
                                    }
                                    vector = vector2;
                                }
                                this.changeSinceLastUpdate = false;
                                byte b3 = liveActor.catagory;
                                if (b3 == 0) {
                                    this.gameService.attackOtherPlayer(liveActor.ID, b2, attackResult.effect, attackResult.dam, 0, attackResult.buffEffect);
                                    this.mainChar.downDurableWeapone();
                                    return;
                                } else {
                                    if (b3 != 1) {
                                        return;
                                    }
                                    if (!zIsSkillAeo) {
                                        this.gameService.attackMonster(liveActor.ID, b2, attackResult.effect, attackResult.dam, 0, attackResult.buffEffect);
                                    } else {
                                        this.gameService.attackMultiMonster(liveActor.ID, b2, attackResult.effect, attackResult.dam, 0, attackResult.buffEffect, vector);
                                    }
                                    this.mainChar.downDurableWeapone();
                                    return;
                                }
                            }
                        }
                    }
                }
                Actor actor2 = this.focusedActor;
                if (actor2 != null && actor2.isItemCanGet()) {
                    Dropable dropable = (Dropable) this.focusedActor;
                    if (dropable.isSendGet) {
                        return;
                    }
                    if (Util.distance(this.mainChar.x, this.mainChar.y, dropable.x, dropable.y) > 35) {
                        moveToRange(dropable, 35);
                    } else {
                        if (dropable.catagory == 4) {
                            if (dropable.item_template_id != 0 || this.mainChar.charXu <= 100000000) {
                                str = "";
                            } else {
                                str = "Quá nhiều tiền";
                                z2 = false;
                            }
                        } else if (dropable.catagory == 3 && GCanvas.gameScr.mainChar.isFullInventory()) {
                            str = "Hành trang đã đầy";
                            z2 = false;
                        } else {
                            str = "";
                        }
                        if (!z2) {
                            addChat(new ChatInfo("", str, 0));
                            changeToNextFocusActor(false);
                            return;
                        } else {
                            MainChar mainChar2 = this.mainChar;
                            mainChar2.dir = Util.findDirection(mainChar2, dropable);
                            sendActorMove(this.mainChar.x, this.mainChar.y);
                            this.gameService.getDropableFromGround(dropable.catagory, dropable.ID);
                            return;
                        }
                    }
                }
                if (this.focusedActor.isNPC()) {
                    if (this.focusedActor.getNpcType() == 1) {
                        GameService.gI().requestNPCInfo(this.focusedActor.getTypeNpc());
                        return;
                    }
                    if (!Tilemap.isOfflineMap) {
                        Vector allQuestNpc = getAllQuestNpc(this.focusedActor.getTypeNpc());
                        if (allQuestNpc.size() > 0 && !this.focusedActor.isNpcChar()) {
                            if (posNpcReceiveClan != null && ((NPC) this.focusedActor).type == posNpcReceiveClan.index) {
                                allQuestNpc.addElement(new Command("Nhiệm vụ Đội", new IAction() {
                                    @Override
                                    public void perform() {
                                        GameScr.this.gameService.questClan(3);
                                        GameScr.posNpcReceiveClan = null;
                                        GCanvas.startWaitDlg();
                                    }
                                }));
                            }
                            allQuestNpc.addElement(new Command("Nói chuyện", new IAction() {
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
                                        GameScr gameScr2 = GameScr.this;
                                        gameScr2.showNpcMenu(gameScr2.focusedActor.getTypeNpc(), new Vector());
                                    }
                                }
                            }));
                            GCanvas.menu.startAt(allQuestNpc, 3);
                            return;
                        }
                        this.focusedActor.chat = null;
                        if (!this.focusedActor.isNpcChar() && this.focusedActor.getTypeNpc() != 7 && this.focusedActor.getTypeNpc() != 10 && this.focusedActor.getTypeNpc() != 22 && this.focusedActor.getTypeNpc() != 31 && this.focusedActor.getTypeNpc() != 21 && this.focusedActor.getTypeNpc() != 25) {
                            Actor actor3 = this.focusedActor;
                            addChat(actor3, Res.getDefaultChat(actor3.getTypeNpc()), 500);
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
                    Vector vector3 = new Vector();
                    Actor actor4 = this.focusedActor;
                    if (actor4 != null) {
                        if (((Ground) actor4).isBuyOk) {
                            vector3.addElement(new Command("Xem thông tin", new IAction() {
                                @Override
                                public void perform() {
                                    GameScr gameScr2 = GameScr.this;
                                    gameScr2.doViewInfoGround(gameScr2.focusedActor.infoGround, GameScr.this.focusedActor);
                                }
                            }));
                            vector3.addElement(new Command("Nâng cấp đất", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        GameScr.this.gameService.doPlant((byte) 4, (byte) ((Ground) GameScr.this.focusedActor).ID);
                                    }
                                }
                            }));
                            vector3.addElement(new Command("Trồng cây", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        GameScr.this.gameService.doPlant((byte) 0, (byte) ((Ground) GameScr.this.focusedActor).ID);
                                    }
                                }
                            }));
                            vector3.addElement(new Command("Thu hoạch", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        GameScr.this.gameService.doPlant((byte) 1, (byte) ((Ground) GameScr.this.focusedActor).ID);
                                    }
                                }
                            }));
                            vector3.addElement(new Command("Nhổ bỏ\u008f", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        final Ground ground = (Ground) GameScr.this.focusedActor;
                                        if (ground.myTree != null) {
                                            GCanvas.startYesNoDlg("Bạn có muốn nhổ bỏ cây này không? ", new IAction() {
                                                @Override
                                                public void perform() {
                                                    GameScr.this.gameService.doPlant((byte) 3, (byte) ground.ID);
                                                    GCanvas.endDlg();
                                                }
                                            });
                                        }
                                    }
                                }
                            }));
                        } else {
                            vector3.addElement(new Command("Mua đất", new IAction() {
                                @Override
                                public void perform() {
                                    if (GameScr.this.focusedActor != null) {
                                        GameScr.this.gameService.doPlant((byte) 2, (byte) ((Ground) GameScr.this.focusedActor).ID);
                                    }
                                }
                            }));
                        }
                    }
                    GCanvas.menu.startAt(vector3, 3);
                }
            }
        } catch (Exception unused) {
        }
    }

    private void moveToRange(Actor actor, int i) {
        Actor actor2;
        if (!(autoFight && typeOptionFocus == 1 && this.mainChar.posTransRoad != null) && this.mainChar.posTransRoad == null) {
            if (isFight && this.map == 201) {
                return;
            }
            int iAngle = Util.angle(actor.x - this.mainChar.x, -(actor.y - this.mainChar.y));
            int iDistance = Util.distance(this.mainChar.x, this.mainChar.y, actor.x, actor.y) - i;
            int iCos = (Util.cos(iAngle) * iDistance) >> 10;
            int i2 = (-(iDistance * Util.sin(iAngle))) >> 10;
            if (iCos > 0) {
                if (iCos > 40) {
                    iCos = 40;
                }
            } else if (iCos < 0 && iCos < -40) {
                iCos = -40;
            }
            if (i2 > 0) {
                if (i2 > 40) {
                    i2 = 40;
                }
            } else if (i2 < 0 && i2 < -40) {
                i2 = -40;
            }
            int i3 = 0;
            boolean z = autoFight && typeOptionFocus == 1 && (actor2 = this.focusedActor) != null && actor2.iskiller;
            if (this.map == 17) {
                if (autoFight && this.mainChar.isDoing && this.coutChangeFocusWhenDoing > 10) {
                    changeToNextFocusActor(false);
                    this.coutChangeFocusWhenDoing = 0;
                }
            } else if (autoFight && !z) {
                changeToNextFocusActor(false);
                i2 = 0;
                iCos = 0;
            }
            if (!z || timeRemovePos > 0 || (Math.abs(this.mainChar.x - this.mainChar.lastXAuto) <= 120 && Math.abs(this.mainChar.y - this.mainChar.lastYAuto) <= 120)) {
                i3 = iCos;
            } else {
                changeToNextFocusActor(true);
                findRoad2(this.mainChar.lastXAuto, this.mainChar.lastYAuto);
                i2 = 0;
            }
            MainChar mainChar = this.mainChar;
            mainChar.moveTo((short) (mainChar.x + i3), (short) (this.mainChar.y + i2));
            if (!autoFight || z) {
                sendActorMove((short) (this.mainChar.x + i3), (short) (this.mainChar.y + i2));
            }
        }
    }

    class AnonymousClass39 implements IAction {
        AnonymousClass39() {
        }

        @Override
        public void perform() {
            Vector vector = new Vector();
            vector.addElement(new Command("Đồ cầm tay", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(1)) {
                        GCanvas.startOKDlg("Hết tiền");
                    } else {
                        GCanvas.startYesNoDlg("Phí sửa chữa là " + GameScr.this.mainChar.getPriceRepair(1) + " " + Res.stMoney, new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(1);
                                GameScr.this.gameService.repairItem(0);
                                GCanvas.startWaitDlg();
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    }
                }
            }));
            vector.addElement(new Command("Quần áo", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(0)) {
                        GCanvas.startOKDlg("Hết tiền");
                    } else {
                        GCanvas.startYesNoDlg("Phí sửa chữa là " + GameScr.this.mainChar.getPriceRepair(0) + " " + Res.stMoney, new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(0);
                                GameScr.this.gameService.repairItem(1);
                                GCanvas.startWaitDlg();
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    }
                }
            }));
            vector.addElement(new Command("Tất cả", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.charXu < GameScr.this.mainChar.getPriceRepair(2)) {
                        GCanvas.startOKDlg("Hết tiền");
                    } else {
                        GCanvas.startYesNoDlg("Phí sửa chữa là " + GameScr.this.mainChar.getPriceRepair(2) + " " + Res.stMoney, new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.mainChar.charXu -= (long) GameScr.this.mainChar.getPriceRepair(2);
                                GameScr.this.gameService.repairItem(2);
                                GCanvas.startWaitDlg();
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.currentDialog = null;
                            }
                        });
                    }
                }
            }));
            GCanvas.menu.startAt(vector, 3);
        }
    }

    public void showNpcMenu(final int i, Vector vector) {
        if (i == 3 || i == 27) {
            for (int i20 = 0; i20 < Res.nameClazz.length; i20++) {
                final int i2 = i20;
                vector.addElement(new Command(Res.nameClazz[i2], new IAction() {
                    @Override
                    public void perform() {
                        GameService.gI().requestNPCInfo((short) i, i2);
                        WindowInfoScr.gI().idClass = i2;
                    }
                }));
            }
            GCanvas.menu.startAt(vector, 3);
        } else if (i == 2 || i == 28) {
            vector.addElement(new Command("Mua bán", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().requestNPCInfo((short) i);
                }
            }));
            vector.addElement(new Command("Nghiền bột", new IAction() {
                @Override
                public void perform() {
                    ScreenItemTim screenItemTim = new ScreenItemTim();
                    screenItemTim.setSizeCell((byte) 5, false);
                    screenItemTim.switchToMe(GameScr.this);
                }
            }));
            vector.addElement(new Command("Thêm dòng", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 4, false);
                    ScreenDapDo.gI().tile = "Thêm dòng";
                }
            }));
            vector.addElement(new Command("Luyện đồ", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.haveItemImbue()) {
                        GameScr.this.gameService.requestImbue(0);
                    } else {
                        GCanvas.startOKDlg("Bạn chưa có nguyên tố để luyện, hãy đến nhà Persian mua rồi quay lại đây.");
                    }
                }
            }));
            vector.addElement(new Command("Luyện đồ tự động", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 1, false);
                    ScreenDapDo.gI().tile = "Luyện đồ tự động";
                }
            }));
            vector.addElement(new Command("Cộng thuộc tính", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 0, false);
                    ScreenDapDo.gI().tile = "Cộng thuộc tính";
                }
            }));
            vector.addElement(new Command("Khóa đồ thú", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 2, false);
                    ScreenDapDo.gI().tile = "Khóa đồ thú";
                }
            }));
            vector.addElement(new Command("Khóa trang bị", new IAction() {
                @Override
                public void perform() {
                    ScreenDapDo.gI().switchToMe(GameScr.this);
                    ScreenDapDo.gI().setSizeCell((byte) 3, false);
                    ScreenDapDo.gI().tile = "Khóa trang bị";
                }
            }));
            vector.addElement(new Command("Sửa đồ", new AnonymousClass39()));
            vector.addElement(new Command("Đục lỗ", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestKham(0);
                    WindowInfoScr.isKham = false;
                    WindowInfoScr.gI().name = "Đục lỗ";
                }
            }));
            vector.addElement(new Command("Khảm", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestKham(0);
                    WindowInfoScr.isKham = true;
                    WindowInfoScr.gI().name = "Khảm";
                }
            }));
            vector.addElement(new Command("Hợp thành", new IAction() {
                @Override
                public void perform() {
                    GameService.gI().epNgoc(0, null);
                    GCanvas.startWaitDlg();
                }
            }));
        } else if (i == 7) {
            vector.addElement(new Command("Đi đến", new IAction() {
                @Override
                public void perform() {
                    final XaphuTemplate xaphu = Res.getXaphu();
                    if (xaphu != null) {
                        Vector vector2 = new Vector();
                        for (int i30 = 0; i30 < xaphu.mapID.length; i30++) {
                            final int i3 = i30;
                            final int i4 = xaphu.mapID[i3];
                            Command command = new Command(Res.nameBigMap[xaphu.mapID[i3]], new IAction() {
                                @Override
                                public void perform() {
                                    if (xaphu.price[i3] > GameScr.this.mainChar.charXu) {
                                        GCanvas.startOKDlg("Không còn đủ tiền");
                                    } else {
                                        GameScr.this.doUseXaPhu(i4, i3);
                                    }
                                }
                            });
                            command.idMap = xaphu.mapID[i3];
                            vector2.addElement(command);
                        }
                        GCanvas.menu.startAt(vector2, 3);
                    }
                }
            }));
            vector.addElement(new Command("Giao tiếp", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(i), 500);
                }
            }));
        } else if (i == 10) {
            vector.addElement(new Command("Lên tàu", new IAction() {
                @Override
                public void perform() {
                    if (GameScr.this.mainChar.potions[20] <= 0) {
                        GCanvas.startOKDlg("Chưa có vé");
                    } else {
                        GameScr.this.gameService.moveUpBoard();
                        GCanvas.startWaitDlg();
                    }
                }
            }));
            vector.addElement(new Command("Mua vé (" + pTicket + " xu)", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.buyTicket();
                    GCanvas.startOKDlg("xin chờ", true);
                }
            }));
            vector.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(i), 500);
                }
            }));
        } else if (i == 22 || i == 31) {
            vector.addElement(new Command("Nạp xu", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.doNapXu();
                }
            }));
            vector.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(i), 500);
                }
            }));
        } else if (i == 25) {
            vector.addElement(new Command("Giao tiếp", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(i), 500);
                }
            }));
            vector.addElement(new Command("Chuyển thẻ", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.giveCard2NPC(GameScr.this.focusedActor.x / 16, GameScr.this.focusedActor.y / 16);
                }
            }));
        } else if (i == 21) {
            vector.addElement(new Command("Học kỹ năng", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.showWindow(0, true, new byte[]{7});
                }
            }));
            vector.addElement(new Command("Nói chuyện", new IAction() {
                @Override
                public void perform() {
                    GameScr.addChat(GameScr.this.focusedActor, Res.getDefaultChat(i), 500);
                }
            }));
        } else if (i > 10 && i != 30) {
            vector.addElement(new Command("Bán", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestNPCInfo(i, 0);
                    GCanvas.startWaitDlg();
                }
            }));
            vector.addElement(new Command("Mua", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.requestNPCInfo(i, 1);
                    GCanvas.startWaitDlg();
                }
            }));
        }
        GCanvas.menu.startAt(vector, 3);
    }

    public void doUseXaPhu(int i, final int i2) {
        short indexArrMap = getIndexArrMap(i);
        final String[] strArr = Res.namMap[indexArrMap];
        final short[] sArr = Res.localXaphu[indexArrMap];
        Vector vector = new Vector();
        for (int i3 = 0; i3 < strArr.length; i3++) {
            final int i4 = i3;
            vector.addElement(new Command(strArr[i3], new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.doMove2Map((byte) i2, sArr[i4 * 3]);
                    ChangScr.gI().switchToMe();
                    ChangScr.gI().setMap(GameScr.this.map, 0, 0);
                    Res.removeMonsterTemplet();
                    GameScr.nameMap = strArr[i4];
                }
            }));
        }
        GCanvas.menu.startAt(vector, 3);
        GCanvas.menu.setIndex();
    }

    public boolean isVTC() {
        return !GameMidlet.isHTBB && GameMidlet.agent.endsWith("88");
    }

    public void doNapXuVTC() {
        if (this.decriptNap.size() == 0) {
            GCanvas.startOKDlg("Chức năng này hiện đang tạm khóa");
        } else {
            GCanvas.startYesNoDlg("Game sẽ tự động thoát. Vui lòng đăng nhập lại sau ít phút sau khi nạp.", new IAction() {
                @Override
                public void perform() {
                    GameMidlet.requestLink("http://pay.mobiplay.vn/ngude");
                }
            }, new IAction() {
                @Override
                public void perform() {
                    GCanvas.currentDialog = null;
                }
            });
        }
    }

    public void doNapXu() {
        if (isVTC()) {
            doNapXuVTC();
            return;
        }
        if (this.decriptNap.size() == 0 || GameMidlet.VERSION == 2) {
            GCanvas.startOKDlg("Chức năng này hiện đang tạm khóa");
            return;
        }
        Vector vector = new Vector();
        int size = this.decriptNap.size();
        for (int i = 0; i < size; i++) {
            final String str = (String) this.syntaxNap.elementAt(i);
            final String str2 = (String) this.centerNap.elementAt(i);
            final String str3 = (String) this.decriptNap.elementAt(i);
            if (str2.length() >= 4) {
                vector.addElement(new Command((String) this.decriptNap.elementAt(i), new IAction() {
                    @Override
                    public void perform() {
                        GCanvas.startYesNoDlg("Bạn có muốn gửi tin nhắn không?", new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.startWaitDlg();
                                GameMidlet.sendSMS(str, "sms://" + str2, new IAction() {
                                    @Override
                                    public void perform() {
                                        GCanvas.startOKDlg("Đã gửi tin. Xin chờ trong chốc lát để nhận tin nhắn phản hồi.");
                                    }
                                }, new IAction() {
                                    @Override
                                    public void perform() {
                                        GCanvas.startOKDlg("Có lỗi khi gửi tin nhắn nạp. Xin hãy thử lại");
                                    }
                                });
                            }
                        }, new IAction() {
                            @Override
                            public void perform() {
                                GCanvas.endDlg();
                            }
                        });
                    }
                }));
            } else {
                vector.addElement(new Command((String) this.decriptNap.elementAt(i), new IAction() {
                    @Override
                    public void perform() {
                        if (!str.equals("-1")) {
                            GCanvas.startYesNoDlg("Game sẽ tự động thoát. Vui lòng đăng nhập lại sau ít phút sau khi nạp.", new IAction() {
                                @Override
                                public void perform() {
                                    GameMidlet.requestLink(str);
                                }
                            }, new IAction() {
                                @Override
                                public void perform() {
                                    GCanvas.currentDialog = null;
                                }
                            });
                        } else {
                            GameScr.this.doNapMoney(str3);
                        }
                    }
                }));
            }
        }
        GCanvas.menu.startAt(vector, 3);
    }

    public void popupNapTien(final int i, final String str, String str2) {
        TField tField = new TField();
        this.tfSeri = tField;
        tField.x = GCanvas.hw - 60;
        this.tfSeri.y = (GCanvas.h - deltaY) - 80;
        this.tfSeri.width = 120;
        this.tfSeri.height = deltaY;
        this.tfSeri.isFocus = true;
        this.tfSeri.setIputType(3);
        PageScr.gI().right = this.tfSeri.cmdClear;
        TField tField2 = new TField();
        this.tfMaso = tField2;
        tField2.x = GCanvas.hw - 60;
        this.tfMaso.y = (GCanvas.h - deltaY) - 45;
        this.tfMaso.width = 120;
        this.tfMaso.height = deltaY;
        this.tfMaso.isFocus = false;
        this.tfMaso.setIputType(3);
        PageScr.gI().setInfo(GCanvas.hw - 70, (GCanvas.h - deltaY) - 120, 140, 100, str2, new Command("Nạp", new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.onSendNapSMS(i, str, GameScr.this.tfSeri.getText().trim(), GameScr.this.tfMaso.getText().trim());
                GCanvas.startOKDlg("Đã gửi thông tin nạp");
            }
        }));
        PageScr.gI().left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                GCanvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().isBigMap = false;
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                GameScr.this.tfSeri.update();
                GameScr.this.tfMaso.update();
                if (GCanvas.isKeyPressed(2) || GCanvas.isKeyPressed(8)) {
                    if (GameScr.this.tfSeri.isFocus) {
                        GameScr.this.tfSeri.isFocus = false;
                        GameScr.this.tfMaso.isFocus = true;
                        PageScr.gI().right = GameScr.this.tfMaso.cmdClear;
                        return;
                    }
                    GameScr.this.tfSeri.isFocus = true;
                    GameScr.this.tfMaso.isFocus = false;
                    PageScr.gI().right = GameScr.this.tfSeri.cmdClear;
                }
            }

            @Override
            public void keyPress(int i2) {
                GameScr.this.tfSeri.keyPressed(i2);
                GameScr.this.tfMaso.keyPressed(i2);
            }

            @Override
            public void paint(Graphics graphics, int i2, int i3) {
                Font.normalFont[0].drawString(graphics, "Seri", GCanvas.hw - 60, (GCanvas.h - Screen.deltaY) - 94, 0);
                Font.normalFont[0].drawString(graphics, "Mã thẻ", GCanvas.hw - 60, (GCanvas.h - Screen.deltaY) - 59, 0);
                GameScr.this.tfSeri.paint(graphics);
                graphics.setClip(GCanvas.hw - 70, (GCanvas.h - Screen.deltaY) - 120, 140, 100);
                graphics.ClipRec(GCanvas.hw - 70, (GCanvas.h - Screen.deltaY) - 120, 140, 100);
                GameScr.this.tfMaso.paint(graphics);
                graphics.restoreCanvas();
            }
        };
        PageScr.gI().switchToMe();
    }

    public void doNapMoney(final String str) {
        Vector vector = new Vector();
        vector.addElement(new Command("Nạp lượng", new IAction() {
            @Override
            public void perform() {
                GameScr.this.popupNapTien(0, str, "NAP LƯỢNG");
            }
        }));
        vector.addElement(new Command("Nạp xu", new IAction() {
            @Override
            public void perform() {
                GameScr.this.popupNapTien(1, str, "NẠP XU");
            }
        }));
        GCanvas.menu.startAt(vector, 3);
    }

    private void paintPoint(Graphics graphics, Position position, int i) {
        if (position != null) {
            int i2 = position.x / 16;
            int i3 = position.y / 16;
            int i4 = i2 - 4;
            int i5 = cmxMini;
            if (i4 <= i5) {
                i2 = i5 + 4;
            } else if (i2 + 6 > wMiniMap + i5) {
                i2 = i5 + 54;
            }
            int i6 = i3 - 4;
            int i7 = cmyMini;
            if (i6 <= i7) {
                i3 = i7 + 4;
            } else if (i3 + 6 > hMiniMap + i7) {
                i3 = i7 + 44;
            }
            graphics.setColor(this.colorPaint[i]);
            graphics.fillRect(i2, i3, 3, 3);
            graphics.setColor(-1);
            graphics.drawRect(i2 - 1, i3 - 1, 4, 4);
        }
    }

    public void paintMiniMap(Graphics graphics) {
        if (Tilemap.w >= GCanvas.w / 16 || Tilemap.h >= GCanvas.h / 16) {
            GCanvas.resetTrans(graphics);
            if (this.posMiniMap.x < GCanvas.w - 62) {
                graphics.translate(GCanvas.w - 62, 0);
            } else {
                graphics.translate(this.posMiniMap.x, 0);
            }
            int i = this.mainChar.x / 16;
            int i2 = this.mainChar.y / 16;
            int i3 = 0;
            while (true) {
                int i4 = 60;
                if (i3 >= 3) {
                    break;
                }
                graphics.setColor(colorMini[i3]);
                if (this.posMiniMap.x >= GCanvas.w - 62) {
                    i4 = wMiniMap;
                }
                graphics.fillRect(i3, i3, i4 - (i3 * 2), hMiniMap - (i3 * 2));
                i3++;
            }
            int translateX = graphics.getTranslateX();
            int translateY = graphics.getTranslateY();
            Graphics.resetTransAndroid(graphics);
            int i5 = wMiniMap;
            if (i5 > 60) {
                i5 = 60;
            }
            graphics.setClip(3, 3, i5 - 5, hMiniMap - 5);
            graphics.saveCanvas();
            graphics.translateAndroid(translateX, translateY);
            int i6 = wMiniMap;
            graphics.ClipRec2(3, 3, (i6 <= 60 ? i6 : 60) - 5, hMiniMap - 5);
            graphics.translate(-cmxMini, -cmyMini);
            if (Tilemap.minimap != null) {
                graphics.drawImage(Tilemap.minimap, 0, 0);
            }
            graphics.setColor(-1);
            graphics.fillRect(i, i2 - 2, 5, 5);
            graphics.setColor(-16776961);
            graphics.fillRect(i + 1, i2 - 1, 3, 3);
            paintPosNPCQuest(graphics);
            paintPoint(graphics, posNpcReceiveClan, 1);
            paintPoint(graphics, this.posBoss, 2);
            graphics.setColor(-261099);
            int size = MainChar.party.size();
            for (int i7 = 0; i7 < size; i7++) {
                Actor actorFindCharByID = findCharByID((short) ((PartyInfo) MainChar.party.elementAt(i7)).id);
                if (actorFindCharByID != null) {
                    graphics.fillRect(actorFindCharByID.x / 16, actorFindCharByID.y / 16, 2, 2);
                }
            }
            if (this.mainChar.posTransRoad != null) {
                if (this.posCam != null) {
                    graphics.setColor(-1578479);
                    graphics.fillRect(this.posCam.x, this.posCam.y, 3, 3);
                }
                for (int i8 = 0; i8 < this.mainChar.posTransRoad.length; i8++) {
                    graphics.setColor(-1578479);
                    byte b = (byte) (xStart + (this.mainChar.posTransRoad[i8] >> 8));
                    byte b2 = (byte) (yStart + (this.mainChar.posTransRoad[i8] & 255));
                    if (b != -1) {
                        graphics.fillRect(b + 1, b2 + 1, 1, 1);
                    }
                }
            }
            graphics.setColor(-460211);
            int size2 = Tilemap.pointPop.size();
            for (int i9 = 0; i9 < size2; i9++) {
                PopupName popupName = (PopupName) Tilemap.pointPop.elementAt(i9);
                int i10 = -3;
                int i11 = popupName.x / 16 >= wMiniMap - 3 ? -3 : 0;
                if (popupName.x / 16 <= 3) {
                    i11 = 3;
                }
                int i12 = popupName.y / 16 <= 3 ? 3 : 0;
                if (popupName.y < hMiniMap - 3) {
                    i10 = i12;
                }
                graphics.fillRect((popupName.x / 16) + i11, (popupName.y / 16) + i10, 3, 3);
            }
            Graphics.resetTransAndroid(graphics);
            graphics.restoreCanvas();
            GCanvas.resetTrans(graphics);
            Font.verySmallFont.drawString(graphics, (this.mainChar.x / 16) + "." + (this.mainChar.y / 16), GCanvas.w - 5, hMiniMap - 7, 1);
        }
    }

    public boolean canPaintNPC_SV(Actor actor) {
        return actor.x + (actor.width / 2) >= cmx && actor.x - (actor.width / 2) <= cmx + GCanvas.w && actor.y - actor.height <= cmy + GCanvas.h && actor.y >= cmy;
    }

    public boolean checkCanPaint() {
        if (GCanvas.currentScreen == this) {
            return true;
        }
        Screen screen = GCanvas.currentScreen;
        MenuSelectItem.gI();
        return screen == MenuSelectItem.me || GCanvas.currentScreen == DialLuckyScr.gI();
    }

    @Override
    public void paint(Graphics graphics) {
        int i;
        int i2;
        int length;
        try {
            if (this.readyGetGameLogic) {
                GCanvas.resetTrans(graphics);
                if (this.isFillScr) {
                    graphics.setColor(-1);
                    graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
                    graphics.setColor(0);
                    graphics.setAlpha(150);
                    graphics.fillRect(GCanvas.hw - 50, GCanvas.hh + 20, 100, 27);
                    Font.normalFont[0].drawString("Time: " + Skill_Kham.t, GCanvas.hw, GCanvas.hh + 25, 2, graphics);
                    return;
                }
                if (lockcmx || lockcmy) {
                    graphics.setColor(0);
                    graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
                }
                byte b = 2;
                int iNextInt = this.timerung > 0 ? r.nextInt(2) : 0;
                graphics.translate(((-cmx) - 10) + iNextInt, ((-cmy) - 10) - iNextInt);
                Tilemap.paintTile(graphics);
                try {
                    EffectManager.lowEffects.paintAll(graphics);
                    try {
                        HieuUng.veDuoi(graphics); // mod: hạt sáng khi di chuyển, nằm dưới nhân vật
                    } catch (Throwable th) {
                    }
                    int size = aeo.size();
                    for (int i3 = 0; i3 < size; i3++) {
                        ((Effect) aeo.elementAt(i3)).paint(graphics);
                    }
                } catch (Exception unused) {
                }
                int size2 = this.arrowsDown.size();
                for (int i4 = 0; i4 < size2; i4++) {
                    ((IArrow) this.arrowsDown.elementAt(i4)).paint(graphics);
                }
                if (Tilemap.trees != null) {
                    if (checkCanPaint()) {
                        int i5 = this.gssy;
                        int i6 = 0;
                        int i7 = 0;
                        int i8 = 0;
                        while (i5 < this.gssye + 5) {
                            if (paintCay == 1) {
                                while (i6 < Tilemap.trees.size() && ((Tree) Tilemap.trees.elementAt(i6)) != null) {
                                    Tree tree = (Tree) Tilemap.trees.elementAt(i6);
                                    if (tree.y == i5) {
                                        if (tree.x - (tree.getW() / 32) <= this.gssxe && tree.x + (tree.getW() / 32) >= this.gssx) {
                                            tree.paint(graphics);
                                        }
                                    } else if (tree.y > i5) {
                                        break;
                                    }
                                    i6++;
                                }
                            }
                            int size3 = this.npc_limit.size();
                            while (i7 < size3) {
                                Actor actor = (Actor) this.npc_limit.elementAt(i7);
                                if (actor != null && actor.typeLimit == 1 && canPaintNPC_SV(actor)) {
                                    actor.paint(graphics);
                                }
                                i7++;
                            }
                            while (i8 < this.actors.size()) {
                                Actor actor2 = (Actor) this.actors.elementAt(i8);
                                if (actor2.catagory != b) {
                                    if ((actor2.y >> 4) > i5) {
                                        break;
                                    }
                                    if (((((actor2.catagory == 0 && paintChar == 1) || actor2.ID == this.mainChar.ID || actor2.catagory != 0) && (actor2.y >> 4) == i5 && actor2.y <= this.gssye * 16 && actor2.y >= this.gssy * 16) || actor2.allwayPaint()) && !(actor2 instanceof CongThanh)) {
                                        actor2.paint(graphics);
                                    }
                                } else if (actor2.catagory == b && actor2.typeLimit != 1 && canPaintNPC_SV(actor2)) {
                                    actor2.paint(graphics);
                                }
                                i8++;
                                Actor actor3 = this.focusedActor;
                                if (actor2 == actor3 && actor3.catagory != 10 && this.focusedActor.catagory != 11 && this.focusedActor.catagory != 12 && ((this.focusedActor.catagory != b || this.posNpcRequest == null || actor2.x / 16 != this.posNpcRequest.x / 16 || actor2.y / 16 != this.posNpcRequest.y / 16) && !this.isAutoRangeWhenAuto)) {
                                    graphics.drawImage(Res.imgSelect, actor2.x, actor2.y - actor2.height, Graphics.HCENTER | Graphics.BOTTOM);
                                }
                                b = 2;
                            }
                            i5++;
                            b = 2;
                        }
                    }
                    Tilemap.paintTileTop(graphics);
                }
                Actor actor4 = congThanh;
                if (actor4 != null) {
                    actor4.paint(graphics);
                }
                int size4 = arrowsUp.size();
                for (int i9 = 0; i9 < size4; i9++) {
                    ((IArrow) arrowsUp.elementAt(i9)).paint(graphics);
                }
                if (checkCanPaint()) {
                    int size5 = this.actors.size();
                    for (int i10 = 0; i10 < size5; i10++) {
                        Actor actor5 = (Actor) this.actors.elementAt(i10);
                        if (actor5 != null) {
                            actor5.paintName(graphics);
                        }
                    }
                }
                if (checkCanPaint()) {
                    EffectManager.hiEffects.paintAll(graphics);
                    paintFlyText(graphics);
                }
                if (checkCanPaint()) {
                    int size6 = Tilemap.pointPop.size();
                    for (int i11 = 0; i11 < size6; i11++) {
                        PopupName popupName = (PopupName) Tilemap.pointPop.elementAt(i11);
                        if (Tilemap.isOfflineMap) {
                            popupName.name = "thoát";
                        } else {
                            popupName.name = "vào";
                        }
                        popupName.paint(graphics);
                    }
                }
                try {
                    int size7 = effAnimate.size();
                    if (size7 > 0 && !Tilemap.isOfflineMap && size7 > 0) {
                        for (int i12 = 0; i12 < size7; i12++) {
                            ((AnimateEffect) effAnimate.elementAt(i12)).paint(graphics);
                        }
                    }
                } catch (Exception unused2) {
                }
                CharCountDown charCountDown = charcountdonw;
                if (charCountDown != null) {
                    charCountDown.paint(graphics, objcountDown.x - 30, (objcountDown.y - 40) - objcountDown.getyHorse());
                }
                paintFocusActorInfo(graphics);
                paintMainCharInfo(graphics);
                if (this == GCanvas.currentScreen) {
                    if (this.isBatMiniMap) {
                        paintMiniMap(graphics);
                    }
                    if (this.hideGUI == 0) {
                        if (this.mainChar.nationID > -1) {
                            Font.normalFontColor1.drawString(graphics, nameCountry[this.mainChar.inCountry], 2, GCanvas.h - 30, 0);
                        }
                        Font.normalFontColor1.drawString(graphics, nameMap, 2, GCanvas.h - 15, 0);
                    }
                    if (!this.chatMode && this.hideGUI == 0 && GCanvas.currentDialog == null && !GCanvas.menu.showMenu) {
                        paintButton(graphics);
                        paintQuickSlot(graphics);
                        paintIndexBuff(graphics);
                    }
                    paintServerInfo(graphics);
                    paintSkillClan(graphics);
                }
                if (Tilemap.lv == 201 && timeChiemThanh != null) {
                    paintChiemThanh(graphics);
                }
                if (this == GCanvas.currentScreen) {
                    paintChat(graphics);
                }
                Vector vector = this.listMSGServer;
                if (vector != null && vector.size() > 0) {
                    paintMsgServer(graphics);
                }
                GCanvas.resetTrans(graphics);
                paintTime(graphics);
                if (msgWorld.size() > 0) {
                    MsgInfo msgInfo = (MsgInfo) msgWorld.elementAt(0);
                    graphics.setColor(0);
                    graphics.setAlpha(100);
                    Vector vector2 = this.listMSGServer;
                    if (vector2 != null && vector2.size() > 0 && GCanvas.gameScr.mainChar.receiveQuest && this.isPaint) {
                        graphics.fillRect(50, GCanvas.hh - 32, GCanvas.w - 100, msgInfo.arr.length * 18);
                        graphics.setClip(50, GCanvas.hh - 32, GCanvas.w - 100, msgInfo.arr.length * 18);
                        i = GCanvas.hh - 32;
                        i2 = GCanvas.w - 100;
                        length = msgInfo.arr.length;
                    } else {
                        graphics.fillRect(50, GCanvas.hh - 55, GCanvas.w - 100, msgInfo.arr.length * 18);
                        graphics.setClip(50, GCanvas.hh - 55, GCanvas.w - 100, msgInfo.arr.length * 18);
                        i = GCanvas.hh - 55;
                        i2 = GCanvas.w - 100;
                        length = msgInfo.arr.length;
                    }
                    graphics.ClipRec(50, i, i2, length * 18);
                    msgInfo.paint(graphics);
                    graphics.restoreCanvas();
                }
                if (MessageScr.nMSG > 0) {
                    graphics.drawImage(Res.imgMSG, 3, imgInfo.getHeight(), 0);
                    this.xChat = 24;
                } else {
                    this.xChat = 4;
                }
                super.paint(graphics);
            }
        } catch (Exception unused3) {
            Cout.println("Loi paint gamescr");
        }
    }

    public void paintTime(Graphics graphics) {
        for (int i = 0; i < this.VecTime.size(); i++) {
            TimecountDown timecountDown = (TimecountDown) this.VecTime.elementAt(i);
            if (timecountDown != null) {
                timecountDown.paint(graphics, GCanvas.w - 30, hMiniMap + (i * 18) + 16);
            }
        }
    }

    private void paintIndexBuff(Graphics graphics) {
        if (this.indexBuff == -1 || timeBuff - (System.currentTimeMillis() / 1000) <= 0) {
            return;
        }
        GameData.imgSkillIcon.drawFrame(this.indexBuff, GCanvas.w - 108, (GCanvas.h + 10) - deltaY, 0, Graphics.BOTTOM | Graphics.RIGHT, graphics);
        Font.smallFontWHITE.drawString(graphics, ((timeBuff - System.currentTimeMillis()) / 1000) + "", GCanvas.w - 118, (GCanvas.h - deltaY) - 18, 2);
    }

    private void paintSkillClan(Graphics graphics) {
        SkillClan[] skillClanArr = this.skillClan;
        if (skillClanArr != null) {
            int length = skillClanArr.length;
            int i = 0;
            for (int i2 = 0; i2 < length; i2++) {
                if (this.skillClan[i2].time > 0) {
                    int i3 = i * 16;
                    GameData.paintIcon(graphics, (short) (this.skillClan[i2].idIcon + 1600), (GCanvas.hw - 25) + i3, GCanvas.h - 10);
                    Font.smallFontWHITE.drawString(graphics, ((int) this.skillClan[i2].lv) + "", (GCanvas.hw - 25) + i3 + 4, GCanvas.h - 10, 2);
                    i++;
                }
            }
        }
        SkillClan[] skillClanArr2 = this.skillClanPrivate;
        if (skillClanArr2 != null) {
            int length2 = skillClanArr2.length;
            int i4 = 0;
            for (int i5 = 0; i5 < length2; i5++) {
                if (this.skillClanPrivate[i5].time > 0) {
                    int i6 = i4 * 16;
                    GameData.paintIcon(graphics, (short) (this.skillClanPrivate[i5].idIcon + 1600), (GCanvas.hw - 25) + i6, GCanvas.h - 30);
                    Font.smallFontWHITE.drawString(graphics, ((int) this.skillClanPrivate[i5].lv) + "", (GCanvas.hw - 25) + i6 + 4, GCanvas.h - 30, 2);
                    i4++;
                }
            }
        }
    }

    private void paintButton(Graphics graphics) {
        Bitmap bitmap = imgBtn2;
        graphics.drawRegion(bitmap, 0, 0, 100, 100, 0, (bitmap.getWidth() / 2) + 20, GCanvas.h - (GCanvas.hh / 2), Graphics.VCENTER | Graphics.HCENTER);
        paintFocus(graphics);
        graphics.drawRegion(imgBtn1, 0, 0, 81, 81, 0, GCanvas.w - 36, (GCanvas.h - (GCanvas.hh / 2)) + 19, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBtn5, 0, 0, 46, 46, 0, GCanvas.w - 90, (GCanvas.h - (GCanvas.hh / 2)) + 40, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBtn5, 0, 0, 46, 46, 0, GCanvas.w - 84, (GCanvas.h - (GCanvas.hh / 2)) + 3, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBtn5, 0, 0, 46, 46, 0, GCanvas.w - 62, (GCanvas.h - (GCanvas.hh / 2)) - 27, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawRegion(imgBtn5, 0, 0, 46, 46, 0, GCanvas.w - 25, (GCanvas.h - (GCanvas.hh / 2)) - 35, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawImage(imgSwapQuickSlot, (GCanvas.w - imgSwapQuickSlot.getWidth()) - 5, GCanvas.hh - 15, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawImage(imgFocusActor, (GCanvas.w - imgSwapQuickSlot.getWidth()) - 5, GCanvas.hh - 50, Graphics.VCENTER | Graphics.HCENTER);
        graphics.drawImage(imgMenu[this.isTouchMenu ? 1 : 0], GCanvas.w / 2, GCanvas.h, Graphics.BOTTOM | Graphics.HCENTER);
        graphics.drawImage(imgChatIcon[this.isTouchChat ? 1 : 0], this.xChat, this.yChat, Graphics.TOP | Graphics.LEFT);
    }

    private void paintFocus(Graphics graphics) {
        switch (this.btnFocus) {
            case 1:
                graphics.drawRegion(imgBtn5, 0, 46, 46, 46, 0, GCanvas.w - 90, (GCanvas.h - (GCanvas.hh / 2)) + 40, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 2:
                Bitmap bitmap = imgBtn2;
                graphics.drawRegion(bitmap, 0, 100, 100, 100, 0, (bitmap.getWidth() / 2) + 20, GCanvas.h - (GCanvas.hh / 2), Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 3:
                graphics.drawRegion(imgBtn5, 0, 46, 46, 46, 0, GCanvas.w - 84, (GCanvas.h - (GCanvas.hh / 2)) + 3, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 4:
                Bitmap bitmap2 = imgBtn2;
                graphics.drawRegion(bitmap2, 0, 100, 100, 100, 6, (bitmap2.getWidth() / 2) + 24, (GCanvas.h - (GCanvas.hh / 2)) - 5, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 5:
                graphics.drawRegion(imgBtn1, 0, 81, 81, 81, 0, GCanvas.w - 36, (GCanvas.h - (GCanvas.hh / 2)) + 25, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 6:
                Bitmap bitmap3 = imgBtn2;
                graphics.drawRegion(bitmap3, 0, 100, 100, 100, 5, (bitmap3.getWidth() / 2) + 16, (GCanvas.h - (GCanvas.hh / 2)) - 4, Graphics.VCENTER | Graphics.HCENTER);
                break;
            case 8:
                Bitmap bitmap4 = imgBtn2;
                graphics.drawRegion(bitmap4, 0, 100, 100, 100, 3, (bitmap4.getWidth() / 2) + 20, (GCanvas.h - (GCanvas.hh / 2)) - 9, Graphics.VCENTER | Graphics.HCENTER);
                break;
        }
        int i = this.btnFocusPotion;
        if (i == 0) {
            return;
        }
        if (i == 7) {
            graphics.drawRegion(imgBtn5, 0, 46, 46, 46, 0, GCanvas.w - 62, (GCanvas.h - (GCanvas.hh / 2)) - 27, Graphics.VCENTER | Graphics.HCENTER);
        } else if (i == 9) {
            graphics.drawRegion(imgBtn5, 0, 46, 46, 46, 0, GCanvas.w - 25, (GCanvas.h - (GCanvas.hh / 2)) - 35, Graphics.VCENTER | Graphics.HCENTER);
        }
    }

    private void pointer(int i, int i2) {
        if (GCanvas.isPointer(GCanvas.w - 82, (GCanvas.h - (GCanvas.hh / 2)) - 47, 40, 40)) {
            this.btnFocusPotion = 7;
            boolean[] zArr = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr[1] = false;
            boolean[] zArr2 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr2[9] = false;
            boolean[] zArr3 = GCanvas.keyHold;
            GCanvas.keyHold[8] = false;
            zArr3[2] = false;
            boolean[] zArr4 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr4[4] = false;
        }
        if (GCanvas.isPointer(GCanvas.w - 45, (GCanvas.h - (GCanvas.hh / 2)) - 55, 40, 40)) {
            this.btnFocusPotion = 9;
            boolean[] zArr5 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr5[2] = false;
            boolean[] zArr6 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr6[4] = false;
            boolean[] zArr7 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr7[1] = false;
            boolean[] zArr8 = GCanvas.keyHold;
            GCanvas.keyHold[8] = false;
            zArr8[7] = false;
        }
        if (xacDinhToaDo(i, i2, (imgBtn2.getWidth() / 2) + 20, ((GCanvas.h - (GCanvas.hh / 2)) - (imgBtn2.getHeight() / 4)) + 5, 60, 50)) {
            GCanvas.keyHold[2] = true;
            boolean[] zArr9 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr9[8] = false;
            boolean[] zArr10 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr10[4] = false;
            boolean[] zArr11 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr11[1] = false;
            GCanvas.keyHold[9] = false;
            this.btnFocus = 2;
            isNhanPhim = true;
            this.mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
        } else if (xacDinhToaDo(i, i2, (imgBtn2.getWidth() / 2) + 20, (GCanvas.h - (GCanvas.hh / 2)) + (imgBtn2.getHeight() / 4), 60, 50)) {
            GCanvas.keyHold[8] = true;
            boolean[] zArr12 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr12[2] = false;
            boolean[] zArr13 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr13[4] = false;
            boolean[] zArr14 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr14[1] = false;
            GCanvas.keyHold[9] = false;
            this.btnFocus = 8;
            isNhanPhim = true;
            this.mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
        } else if (xacDinhToaDo(i, i2, 20, GCanvas.h - (GCanvas.hh / 2), 60, 50)) {
            GCanvas.keyHold[4] = true;
            boolean[] zArr15 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr15[2] = false;
            boolean[] zArr16 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr16[8] = false;
            boolean[] zArr17 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr17[1] = false;
            GCanvas.keyHold[9] = false;
            this.btnFocus = 4;
            isNhanPhim = true;
            this.mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
        } else if (xacDinhToaDo(i, i2, imgBtn2.getWidth() + 10, GCanvas.h - (GCanvas.hh / 2), 60, 50)) {
            GCanvas.keyHold[6] = true;
            boolean[] zArr18 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr18[2] = false;
            boolean[] zArr19 = GCanvas.keyHold;
            GCanvas.keyHold[8] = false;
            zArr19[4] = false;
            boolean[] zArr20 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr20[1] = false;
            GCanvas.keyHold[9] = false;
            this.btnFocus = 6;
            isNhanPhim = true;
            this.mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
        } else if (GCanvas.isPointer(GCanvas.w - 55, (GCanvas.h - (GCanvas.hh / 2)) - 11, 50, 70)) {
            this.btnFocus = 5;
            boolean[] zArr21 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr21[2] = false;
            boolean[] zArr22 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr22[4] = false;
            boolean[] zArr23 = GCanvas.keyHold;
            GCanvas.keyHold[8] = false;
            zArr23[1] = false;
            boolean[] zArr24 = GCanvas.keyHold;
            GCanvas.keyHold[9] = false;
            zArr24[7] = false;
        }
        if (GCanvas.isPointer(GCanvas.w - 110, (GCanvas.h - (GCanvas.hh / 2)) + 20, 40, 40)) {
            this.btnFocus = 1;
            boolean[] zArr25 = GCanvas.keyHold;
            GCanvas.keyHold[3] = false;
            zArr25[2] = false;
            boolean[] zArr26 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr26[4] = false;
            boolean[] zArr27 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr27[8] = false;
            boolean[] zArr28 = GCanvas.keyHold;
            GCanvas.keyHold[9] = false;
            zArr28[7] = false;
            return;
        }
        if (GCanvas.isPointer(GCanvas.w - 104, (GCanvas.h - (GCanvas.hh / 2)) - 17, 40, 40)) {
            this.btnFocus = 3;
            boolean[] zArr29 = GCanvas.keyHold;
            GCanvas.keyHold[8] = false;
            zArr29[2] = false;
            boolean[] zArr30 = GCanvas.keyHold;
            GCanvas.keyHold[6] = false;
            zArr30[4] = false;
            boolean[] zArr31 = GCanvas.keyHold;
            GCanvas.keyHold[5] = false;
            zArr31[1] = false;
            boolean[] zArr32 = GCanvas.keyHold;
            GCanvas.keyHold[9] = false;
            zArr32[7] = false;
        }
    }

    private void paintMsgServer(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        graphics.setColor(0);
        graphics.setAlpha(100);
        graphics.fillRect(0, 0, GCanvas.w, 18);
        graphics.drawImage(Res.imgCmdBar, 0, GCanvas.h + 3, Graphics.LEFT | Graphics.BOTTOM);
        graphics.setClip(5, 0, GCanvas.w - 10, 18);
        graphics.ClipRec(5, 0, GCanvas.w - 10, 18);
        Font.arialFont11.drawString(graphics, (String) this.listMSGServer.elementAt(0), this.xMsgServer, (7 - (Font.arialFont11.getHeight() / 2)) + GCanvas.screenlevel == 1 ? 4 : 3, 0);
        graphics.restoreCanvas();
        graphics.setClip(0, 0, GCanvas.h, GCanvas.w);
    }

    private void paintChiemThanh(Graphics graphics) {
        int i = 38;
        int i2 = 0;
        while (true) {
            int[] iArr = timeChiemThanh;
            if (i2 >= iArr.length) {
                return;
            }
            if (((long) iArr[i2]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i2])) > 0) {
                int i3 = i2 + 1;
                Font.arialFont11.drawString(graphics, i3 + ": " + nameChiemThanh[i2] + " (" + (((long) timeChiemThanh[i2]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i2]))) + ")", GCanvas.hw + 2, i + 1, 3);
                Font.arialFontBlack.drawString(graphics, i3 + ": " + nameChiemThanh[i2] + " (" + (((long) timeChiemThanh[i2]) - ((System.currentTimeMillis() / 1000) - ((long) curTimeCT[i2]))) + ")", GCanvas.hw, i, 3);
            } else {
                if (!nameClan[i2].equals("")) {
                    Font.arialFontBlack.drawString(graphics, (i2 + 1) + ": " + nameClan[i2], GCanvas.hw, i, 3);
                }
                i2++;
            }
            i += 10;
            i2++;
        }
    }

    public void paintServerInfo(Graphics graphics) {
        if (serverInfo.size() > 0 && this.tk == null) {
            this.tk = new Ticker((String) serverInfo.elementAt(0), GCanvas.w + 10, (GCanvas.h / 2) - 53);
            serverInfo.removeElementAt(0);
        }
        Ticker ticker = this.tk;
        if (ticker != null) {
            ticker.paint(graphics);
        }
    }

    class Ticker {
        public boolean end = false;
        public String info;
        int x;
        int y;

        public Ticker(String str, int i, int i2) {
            this.info = str;
            this.x = i;
            this.y = i2;
        }

        public void paint(Graphics graphics) {
            graphics.setColor(0);
            graphics.setAlpha(100);
            graphics.fillRect(50, (GCanvas.h / 4) - 20, GCanvas.w - 100, Font.normalFont[0].getHeight() + 6);
            graphics.ClipRec(55, (GCanvas.h / 4) - 20, GCanvas.w - 110, Font.normalFont[0].getHeight() + 6);
            graphics.setClip(55, (GCanvas.h / 4) - 20, GCanvas.w - 110, Font.normalFont[0].getHeight() + 6);
            Font.normalFont[0].drawString(graphics, this.info, this.x, (GCanvas.h / 4) - 21, 0);
            graphics.restoreCanvas();
            graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        }

        public void update() {
            if (this.x + Font.normalFont[0].getWidth(this.info) + 10 < 0) {
                this.end = true;
            }
            this.x -= 2;
        }
    }

    @Override
    public boolean keyPress(int i) {
        if (this.chatMode) {
            this.tfChat.keyPressed(i);
            if (i == -7) {
                this.tfChat.clear();
            }
            if (i == -6) {
                GCanvas.clearKeyHold();
                GCanvas.clearKeyPressed();
                this.chatMode = false;
            }
            if (i == -5) {
                String text = this.tfChat.getText();
                this.tfChat.setText("");
                if (text.trim() != "") {
                    doChatFromMe(text);
                }
            }
            return true;
        }
        if (PageScr.me != GCanvas.currentScreen) {
            if (i == 48) {
                if (indexTabSlot == 0) {
                    indexTabSlot = 1;
                } else {
                    indexTabSlot = 0;
                }
                xSlot = Res.wKhung;
            } else if (i == 98) {
                GCanvas.keyPressed[8] = true;
            } else if (i == 106) {
                GCanvas.keyPressed[11] = true;
            } else if (i == 114) {
                GCanvas.keyPressed[1] = true;
            } else if (i == 121) {
                GCanvas.keyPressed[3] = true;
            } else if (i == 109) {
                GCanvas.keyPressed[0] = true;
            } else if (i != 110) {
                switch (i) {
                    case 102:
                        GCanvas.keyPressed[4] = true;
                        break;
                    case 103:
                        GCanvas.keyPressed[5] = true;
                        break;
                    case 104:
                        GCanvas.keyPressed[6] = true;
                        break;
                    default:
                        switch (i) {
                            case 116:
                                GCanvas.keyPressed[2] = true;
                                break;
                            case 117:
                                GCanvas.keyPressed[10] = true;
                                break;
                            case 118:
                                GCanvas.keyPressed[7] = true;
                                break;
                        }
                        break;
                }
            } else {
                GCanvas.keyPressed[9] = true;
            }
        }
        return false;
    }

    private void doChatFocus(String str) {
        Actor actor = this.focusedActor;
        if (actor == null || !actor.getCharName().equals(str)) {
            for (int i = 0; i < this.actors.size(); i++) {
                Actor actor2 = (Actor) this.actors.elementAt(i);
                if (actor2.catagory == 0 && actor2 != this.mainChar && actor2.getCharName().equals(str)) {
                    this.focusedActor = actor2;
                    return;
                }
            }
        }
    }

    private void doChatFromMe(String str) {
        this.mainChar.chat = null;
        addChat(this.mainChar, str, 200);
        MessageScr.gI().addTab(this.mainChar.name + ": " + str, null, 0);
        GameService.gI().chat(str);
    }

    private void paintChat(Graphics graphics) {
        if ((this.hideGUI != 2 || this.chatMode) && !GCanvas.menu.showMenu) {
            if (this.hideGUI == 0) {
                graphics.translate(-graphics.getTranslateX(), -graphics.getTranslateY());
                if (GCanvas.currentDialog != null || this != GCanvas.currentScreen) {
                    return;
                }
                Actor actor = this.focusedActor;
                if ((actor != null && actor.catagory != 2) || this.focusedActor == null) {
                    Font.normalFont[0].getWidth("Đóng");
                    int i = this.yMsgChat;
                    if (i < this.toYchat) {
                        this.yMsgChat = i + 1;
                    }
                    Vector vector = this.listMSGServer;
                    if (vector != null && vector.size() > 0 && GCanvas.gameScr.mainChar.receiveQuest && this.isPaint) {
                        graphics.setColor(0);
                        graphics.setAlpha(100);
                        graphics.fillRect(50, GCanvas.hh - 55, GCanvas.w - 100, 19);
                        graphics.setClip(55, GCanvas.hh - 55, GCanvas.w - 110, 19);
                        graphics.ClipRec(55, GCanvas.hh - 55, GCanvas.w - 110, 19);
                        String str = "";
                        for (int i2 = 0; i2 < this.chatHistory.size(); i2++) {
                            ChatInfo chatInfo = (ChatInfo) this.chatHistory.elementAt(i2);
                            if (!chatInfo.name.equals("")) {
                                str = str + chatInfo.name + ": ";
                            }
                            str = str + chatInfo.content;
                        }
                        Font.arialFont11.drawString(graphics, str, this.xMsgServer, GCanvas.hh - 52, 0);
                        graphics.restoreCanvas();
                        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
                    }
                }
            }
            if (this.chatMode) {
                graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
                Res.paintDlgFull(graphics, this.tfChat.x - 5, this.tfChat.y - 5, this.tfChat.width + 10, this.tfChat.height + 10);
                this.tfChat.paint(graphics);
            }
        }
    }

    private void paintMainCharInfo(Graphics graphics) {
        int i;
        if (this.hideGUI == 2) {
            return;
        }
        GCanvas.resetTrans(graphics);
        graphics.drawImage(imgInfo, -3, -2, Graphics.TOP | Graphics.LEFT);
        Res.getCharPartInfo(2, this.mainChar.currentHead).paintStatic(graphics, (short) 23, (short) 33, 0, this.mainChar.frame);
        Font.normalFontColor1.drawString(graphics, "Lv " + ((int) this.mainChar.level) + "+" + this.mainChar.getPercent() + "%", 48, 5, 0);
        if (this.mainChar.killer > 0) {
            Font.verySmallFont.drawString(graphics, ((int) this.mainChar.killer) + "", 48, imgInfo.getHeight() - 11, 0);
        }
        if (this.mainChar.delay >= 0) {
            int[] time = this.mainChar.getTime();
            Font.normalFont[0].drawString(graphics, time[0] + " : " + time[1], 5, imgInfo.getHeight() + 10, 0);
        }
        String goldTime = this.mainChar.getGoldTime();
        if (!goldTime.equals("")) {
            Font.normalFont[0].drawString(graphics, goldTime, 5, imgInfo.getHeight() + 20, 0);
            Font.smallFontWHITE.drawString(graphics, Char.goldTime, 5, imgInfo.getHeight() + 34, 0);
        }
        int i2 = (this.mainChar.hp * this.HPBARW) / this.mainChar.maxhp;
        int i3 = (this.mainChar.mp * this.HPBARW) / this.mainChar.maxmp;
        graphics.setColor(0);
        if (i2 != 45) {
            graphics.fillRect(i2 + 51, 23, (this.HPBARW - i2) - 2, 6);
            graphics.drawImage(imgRoundInfo, 93, 23);
            if (i2 == 0) {
                i = 45;
                graphics.drawRegion(imgRoundInfo, 0, 0, 3, 6, 3, 49, 23, 0);
            } else {
                i = 45;
                if (i2 != 0 && i2 < 45) {
                    graphics.drawImage(imgRoundInfo, i2 + 49, 23);
                }
            }
        } else {
            i = 45;
        }
        graphics.setColor(0);
        if (i3 != i) {
            graphics.fillRect(i3 + 48, 31, (this.HPBARW - i3) - 2, 6);
            graphics.drawImage(imgRoundInfo, 89, 31);
            if (i3 == 0) {
                graphics.drawRegion(imgRoundInfo, 0, 0, 3, 6, 3, 46, 31, 0);
            } else if (i3 != 0 && i3 < 44) {
                graphics.drawImage(imgRoundInfo, i3 + 46, 31);
            }
        }
        if (GCanvas.gameTick % 10 > 3) {
            if (this.mainChar.basePointLeft > 0) {
                graphics.drawImage(imgCong[0], GCanvas.w - 2, imgInfo.getHeight() + 15, Graphics.RIGHT | Graphics.TOP);
            }
            if (this.mainChar.skillPointLeft > 0) {
                graphics.drawImage(imgCong[1], GCanvas.w - 2, imgInfo.getHeight() + 26, Graphics.RIGHT | Graphics.TOP);
            }
        }
    }

    private void paintQuickSlot(Graphics graphics) {
        if (this.hideGUI == 2 || GCanvas.currentDialog != null || this.chatMode) {
            return;
        }
        Actor actor = this.focusedActor;
        if (actor != null) {
            byte b = actor.catagory;
        }
        GCanvas.resetTrans(graphics);
        for (int i = 0; i < 5; i++) {
            if (MainChar.quickSlot[indexTabSlot][i] != null) {
                if (i == 0) {
                    MainChar.quickSlot[indexTabSlot][i].paint(graphics, GCanvas.w - 91, (GCanvas.h - (GCanvas.hh / 2)) + 40);
                } else if (i == 1) {
                    MainChar.quickSlot[indexTabSlot][i].paint(graphics, GCanvas.w - 85, (GCanvas.h - (GCanvas.hh / 2)) + 3);
                } else if (i == 2) {
                    MainChar.quickSlot[indexTabSlot][i].paint(graphics, GCanvas.w - 35, (GCanvas.h - (GCanvas.hh / 2)) + 20);
                } else if (i == 3) {
                    MainChar.quickSlot[indexTabSlot][i].paint(graphics, GCanvas.w - 62, (GCanvas.h - (GCanvas.hh / 2)) - 27);
                } else if (i == 4) {
                    MainChar.quickSlot[indexTabSlot][i].paint(graphics, GCanvas.w - 25, (GCanvas.h - (GCanvas.hh / 2)) - 35);
                }
            }
        }
    }

    private void paintSoftIcon(Graphics graphics) {
        graphics.drawImage(Res.imgSoft[1], GCanvas.w - 15, GCanvas.h - 13, 0);
    }

    private void paintFocusActorInfo(Graphics graphics) {
        if (this.hideGUI == 2) {
            return;
        }
        GCanvas.resetTrans(graphics);
        if (this.isAutoRangeWhenAuto) {
            return;
        }
        Vector vector = this.listMSGServer;
        if (vector != null && vector.size() > 0) {
            graphics.translate(0, 18);
        }
        Actor actor = this.focusedActor;
        if (actor != null) {
            if (actor.catagory != 10 && this.focusedActor.catagory != 11 && this.focusedActor.catagory != 12) {
                Font.normalFontColor1.drawString(graphics, this.focusedActor.getDisplayName(), GCanvas.hw + 30, 0, 1);
                this.focusedActor.paintCorner(graphics, GCanvas.hw + 45, 20);
            }
            if (this.focusedActor.catagory == 0 || this.focusedActor.catagory == 1) {
                LiveActor liveActor = (LiveActor) this.focusedActor;
                if (liveActor.maxhp == 0 || liveActor.idBoss != -1) {
                    return;
                }
                GCanvas.resetTrans(graphics);
                Vector vector2 = this.listMSGServer;
                if (vector2 != null && vector2.size() > 0) {
                    graphics.translate(0, 18);
                }
                graphics.drawImage(imgHpMonster, GCanvas.hw - 20, 14, 20);
                int i = liveActor.hp > 0 ? (int) ((((long) liveActor.hp) * ((long) this.HPBARW_MONSTER)) / ((long) liveActor.maxhp)) : 0;
                graphics.setColor(-14408411);
                graphics.fillRect(GCanvas.hw - 19, 15, this.HPBARW_MONSTER - i, 3);
                Font.verySmallFont.drawString(graphics, (liveActor.hp > 0 ? liveActor.hp : 1) + "/" + liveActor.maxhp, (GCanvas.hw + 50) - (this.HPBARW_MONSTER >> 1), 22, 1);
                Font.smallFontWHITE.drawString(graphics, "lv" + ((int) liveActor.level), GCanvas.hw + 59, 23, 1);
            }
        }
    }

    public void startSkill_Noel(int i, int i2, int i3, Vector vector) {
        for (int i4 = 0; i4 < vector.size(); i4++) {
            LiveActor liveActor = (LiveActor) vector.elementAt(i4);
            EffectManager.addLowEffect(new Skill_Noel(i, i2, liveActor.x, liveActor.y, liveActor));
        }
    }

    public void startSkill_BossHalloween(int i, int i2, int i3, Vector vector) {
        for (int i4 = 0; i4 < vector.size(); i4++) {
            EffectManager.addLowEffect(new Skill_Boss_Halloween(i, i2, (LiveActor) vector.elementAt(i4)));
        }
    }

    public void startSkill_Cau_Lua(int i, int i2, int i3, Vector vector) {
        Skill_Da_Roi skill_Da_Roi = new Skill_Da_Roi(i, i2, i, i3, 0);
        skill_Da_Roi.type = (short) 1;
        skill_Da_Roi.list = vector;
        EffectManager.hiEffects.addElement(skill_Da_Roi);
    }

    public void startWeapsMango(LiveActor liveActor, LiveActor liveActor2, int i) {
        if (liveActor == null || liveActor2 == null) {
            return;
        }
        arrowsUp.addElement(new WeaponsMango(liveActor, liveActor2, i));
    }

    public void startWeapsAngleBoss(int i, int i2, LiveActor liveActor, LiveActor liveActor2, int i3, boolean z, int i4, int i5, int i6, int i7, boolean z2) {
        if (!z) {
            this.arrowsDown.addElement(new WeaponsAngleBoss(i, i2, liveActor, liveActor2, i3, i4, i5, i6, i7, z2));
        } else {
            if (liveActor != null && liveActor2 != null) {
                arrowsUp.addElement(new WeaponsAngleBoss(i, i2, liveActor, liveActor2, i3, i4, i5, i6, i7, z2));
            }
        }
    }

    public void SkillofBoss_Snake(LiveActor liveActor, LiveActor liveActor2, int i, int i2) {
        int i3 = 0;
        while (i3 < 12) {
            startWeapsAngleBoss(i3 * 30, 10, liveActor, liveActor2, 0, true, i, i2, 0, 0, i3 == 11);
            i3++;
        }
        this.timerung = 20;
    }

    public void startWeaponsLazer(LiveActor liveActor, LiveActor liveActor2, int i, int i2, int i3, int i4, int i5) {
        if (liveActor == null || liveActor2 == null) {
            return;
        }
        WeaponsLazer weaponsLazer = new WeaponsLazer(liveActor, liveActor2, i, i2, i4, i5);
        if (i3 == 1) {
            this.arrowsDown.addElement(weaponsLazer);
        } else {
            arrowsUp.addElement(weaponsLazer);
        }
    }

    public void startNewMagicBeam(int i, LiveActor liveActor, LiveActor liveActor2, int i2, int i3, int i4, byte b) {
        MagicBeam magicBeam = new MagicBeam();
        magicBeam.set(i, i2, i3, i4, b, liveActor, liveActor2);
        arrowsUp.addElement(magicBeam);
    }

    public void startNewMagicBeam(int i, LiveActor liveActor, LiveActor liveActor2, int i2, int i3, int i4, byte b, int i5) {
        MagicBeam magicBeam = new MagicBeam();
        magicBeam.set(i, i2, i3, i4, b, liveActor, liveActor2);
        magicBeam.setAngle(i5);
        arrowsUp.addElement(magicBeam);
    }

    public void startNewArrow(int i, LiveActor liveActor, LiveActor liveActor2, int i2, int i3, int i4, byte b, int i5) {
        Arrow arrow = new Arrow(i5);
        arrow.set(i, i2, i3, i4, b, liveActor, liveActor2);
        arrowsUp.addElement(arrow);
    }

    public void onActorMove(byte b, short s, short s2, short s3, short s4, byte b2, int i, byte b3, byte b4, boolean z) {
        // mod: lệnh vị trí cho chính mình = bước lướt bị từ chối -> auto đặt lại vị trí
        if (AutoGuard.viTriGuard(b, s, s2, s3, s4)) {
            return;
        }
        if (this.readyGetGameLogic) {
            isActorMove = true;
            for (int size = this.actors.size() - 1; size >= 0; size--) {
                Actor actor = (Actor) this.actors.elementAt(size);
                if (b == actor.catagory && actor.ID == s2) {
                    actor.setPosTo(s3, s4);
                    actor.canfocus = z;
                    if (actor.catagory == 0) {
                        actor.pk = b2;
                        if (actor.getHp() <= 0 || actor.getRealHp() <= 0) {
                            actor.setState(3);
                        }
                    }
                    isActorMove = false;
                    return;
                }
            }
            isActorMove = false;
            Actor actorCreateActor = CreateActor(b, s, s2, s3, s4, b3, b4);
            actorCreateActor.canfocus = z;
            if (Util.inDataRange(this.mainChar, actorCreateActor)) {
                this.actors.addElement(actorCreateActor);
                if (this.actors.size() < 200) {
                    if (b != 0) {
                        if (b != 1) {
                            return;
                        }
                        ((Monster) actorCreateActor).idChar = i;
                        this.gameService.requestMonsterInfo(s2);
                        return;
                    }
                    if (b4 == -1) {
                        Char r1 = (Char) actorCreateActor;
                        r1.stand = System.currentTimeMillis();
                        r1.pk = b2;
                    }
                    this.gameService.requestCharInfo(s2);
                }
            }
        }
    }

    public void onCharInfo(Char r10) {
        if (r10.idBoss != -1) {
            r10.dir = (short) 0;
        }
        short[] sArr = idChiemThanh;
        if (sArr != null) {
            int length = sArr.length;
            for (int i = 0; i < length; i++) {
                if (idChiemThanh[i] == r10.ID) {
                    if (r10 != null) {
                        r10.EffFace = null;
                    }
                    if (r10 != null && timeChiemThanh[i] > 0) {
                        r10.EffFace = new WayPoint(r10.x, r10.y - 5, (short) 1, (byte) 1, false);
                    }
                }
            }
        }
        if (r10.defend > 0 || r10.defend_magic > 0) {
            r10.effPhap = new Effect(r10.x, r10.y, r10.defend > r10.defend_magic ? 21 : 36);
        }
    }

    public void onPlayerAttackPlayer(short s, short s2, byte b, int i, int i2, byte b2, byte b3, byte b4, byte b5) {
        if (this.readyGetGameLogic) {
            if (s == this.mainChar.ID) {
                Char r1 = (Char) findCharByID(s2);
                if (r1 != null) {
                    if (this.mainChar.attkPower == 0) {
                        this.mainChar.attkPower = i;
                    }
                    if (r1 != null) {
                        r1.setRealHP(i2);
                        if (r1.hp <= 0 || r1.realHP <= 0) {
                            r1.state = (byte) 3;
                        }
                        AttackResult.startEff(b2, r1.x, r1.y - 25);
                        if (b4 == 0) {
                            AttackResult.startEff(b2, r1.x, r1.y + 35);
                        }
                    }
                    if (i > 0) {
                        for (int i3 = 0; i3 < b3; i3++) {
                            int i4 = i3 % 2;
                            GCanvas.gameScr.startFlyText("-" + (i / b3), 0, r1.x + (i4 == 0 ? (short) 0 : (short) -20), r1.y - (i4 == 0 ? (short) 20 : (short) 30), 1, -2);
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            if (s2 == this.mainChar.ID) {
                Char r2 = (Char) findCharByID(s);
                if (r2 != null) {
                    r2.dir = Util.findDirection(r2, this.mainChar);
                    r2.startAttack(this.mainChar, new AttackResult(i, b2), b, MainChar.skillLevelLearnt[b]);
                    if (SkillManager.isSkillAeo(this.mainChar.clazz, b / 10)) {
                        Vector vector = new Vector();
                        vector.addElement(this.mainChar);
                        r2.currentSkillAnimate.setActorter(vector);
                    }
                    if (b4 == 0) {
                        GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, r2.x, r2.y - 35, 1, -2);
                    }
                }
                this.mainChar.downDuarable();
                this.mainChar.setRealHP(i2);
                int size = this.actors.size();
                for (int i5 = 0; i5 < size && ((Actor) this.actors.elementAt(i5)).ID != s; i5++) {
                }
                return;
            }
            Char r3 = (Char) findCharByID(s2);
            if (r3 != null) {
                r3.setRealHP(i2);
                if (i2 == 0) {
                    r3.state = (byte) 3;
                }
                if (b4 == 0) {
                    GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, r3.x, r3.y - 35, 1, -2);
                }
            }
            Char r4 = (Char) findCharByID(s);
            if (r4 != null) {
                r4.dir = Util.findDirection(r4, r3);
                r4.startAttack(r3, new AttackResult(i, b2), b, b5);
            }
        }
    }

    public void onAttackMultiTarget(Message message) {
        int i;
        int i2;
        try {
            short s = message.reader().readShort();
            byte b = message.reader().readByte();
            int i3 = message.reader().readInt();
            byte b2 = message.reader().readByte();
            byte b3 = message.reader().readByte();
            byte b4 = message.reader().readByte();
            Char r8 = (Char) findCharByID(s);
            byte b5 = message.reader().readByte();
            Vector vector = new Vector();
            for (int i4 = 0; i4 < b5; i4++) {
                try {
                    Monster monsterFindMonsterByID = findMonsterByID(message.reader().readShort());
                    int i5 = message.reader().readInt();
                    if (monsterFindMonsterByID != null) {
                        vector.addElement(monsterFindMonsterByID);
                        int i6 = monsterFindMonsterByID.hp;
                        monsterFindMonsterByID.setRealHP(i5);
                        if (i5 <= 0) {
                            if (r8 != null) {
                                i = (monsterFindMonsterByID.x - r8.x) * 2;
                                i2 = (monsterFindMonsterByID.y - r8.y) * 2;
                                while (true) {
                                    if (i <= 10 && i2 <= 10 && i >= -10 && i2 >= -10) {
                                        break;
                                    }
                                    i >>= 1;
                                    i2 >>= 1;
                                }
                            } else {
                                i = 0;
                                i2 = 0;
                            }
                            monsterFindMonsterByID.startDeadFly(i, i2);
                        }
                        AttackResult.startEff(b2, monsterFindMonsterByID.x, monsterFindMonsterByID.y - 25);
                        if (i3 > 0) {
                            GCanvas.gameScr.startFlyText("-" + i3, 0, monsterFindMonsterByID.x, monsterFindMonsterByID.y - 15, 1, -2);
                        }
                        if (b4 == 0) {
                            GCanvas.gameScr.startFlyText(AttackResult.EFF_NAME[3], 0, monsterFindMonsterByID.x, monsterFindMonsterByID.y - 35, 1, -2);
                        }
                    }
                } catch (Exception e) {
                    Cout.println("LOI TRONG HAM onAttackMultiTarget()" + e.toString());
                }
            }
            if (this.mainChar.ID != s) {
                if (r8 != null) {
                    r8.startAttack((Monster) vector.elementAt(0), new AttackResult(i3, b2), b, b3);
                    r8.currentSkillAnimate.setMonster(vector);
                    return;
                }
                return;
            }
            if (this.mainChar.attkPower == 0) {
                this.mainChar.attkPower = i3;
            }
            if (i3 > 0) {
                GCanvas.gameScr.startFlyText("-" + i3, 0, ((Monster) vector.elementAt(0)).x, ((Monster) vector.elementAt(0)).y - 15, 1, -2);
            }
        } catch (Exception unused) {
            Cout.println("LOI TRONG HAM TAN CONG MULTI");
        }
    }

    public void onPlayerAttackMonster(short s, short s2, byte b, int i, int i2, byte b2, byte b3, byte b4, byte b5) {
        if (this.readyGetGameLogic) {
            Monster monsterFindMonsterByID = findMonsterByID(s2);
            if (monsterFindMonsterByID != null) {
                int i3 = monsterFindMonsterByID.hp;
                monsterFindMonsterByID.setRealHP(i2);
            }
            if (this.mainChar.ID == s) {
                if (this.mainChar.attkPower == 0) {
                    this.mainChar.attkPower = i;
                }
                AttackResult.startEff(b2, monsterFindMonsterByID.x, monsterFindMonsterByID.y - 25);
                if (b4 == 0) {
                    AttackResult.startEff((byte) 3, monsterFindMonsterByID.x, monsterFindMonsterByID.y + 35);
                }
                if (i > 0) {
                    for (int i4 = 0; i4 < b3; i4++) {
                        int i5 = i4 % 2;
                        GCanvas.gameScr.startFlyText("-" + i, 0, (i5 == 0 ? (short) 0 : (short) -20) + monsterFindMonsterByID.x, monsterFindMonsterByID.y - (i5 == 0 ? (short) 20 : (short) 30), 1, -2);
                    }
                    return;
                }
                return;
            }
            Char r4 = (Char) findCharByID(s);
            if (r4 != null) {
                r4.dir = Util.findDirection(r4, monsterFindMonsterByID);
                r4.startAttack(monsterFindMonsterByID, new AttackResult(i, b2), b, b5);
                if (b4 == 0) {
                    AttackResult.startEff((byte) 3, monsterFindMonsterByID.x, monsterFindMonsterByID.y + 35);
                }
            }
        }
    }

    public void onDropList(ItemDropInfo[] itemDropInfoArr) {
        for (int i = 0; i < itemDropInfoArr.length; i++) {
            try {
                Dropable dropable = (Dropable) CreateActor(itemDropInfoArr[i].itemCatagory, itemDropInfoArr[i].itemTemplateID, itemDropInfoArr[i].itemID, itemDropInfoArr[i].x, itemDropInfoArr[i].y, (byte) 0, (byte) -1);
                dropable.startDropFrom(itemDropInfoArr[i].x, itemDropInfoArr[i].y, dropable.x, dropable.y);
                this.actors.addElement(dropable);
            } catch (Exception unused) {
                return;
            }
        }
    }

    public void startDeadFly(Monster monster, int i) {
        int i2;
        int i3;
        Char r4 = (Char) findCharByID((short) i);
        if (monster != null) {
            if (r4 != null) {
                i2 = (monster.x - r4.x) * 2;
                i3 = (monster.y - r4.y) * 2;
                while (true) {
                    if (i2 <= 10 && i3 <= 10 && i2 >= -10 && i3 >= -10) {
                        break;
                    }
                    i2 >>= 1;
                    i3 >>= 1;
                }
            } else {
                i2 = 0;
                i3 = 0;
            }
            monster.startDeadFly(i2, i3);
        }
    }

    public void onMonsterDie(MonsterDieInfo monsterDieInfo) {
        Monster monsterFindMonsterByID;
        if (this.readyGetGameLogic && (monsterFindMonsterByID = findMonsterByID(monsterDieInfo.attacked)) != null) {
            startDeadFly(monsterFindMonsterByID, monsterDieInfo.attacker);
            if (this.focusedActor == monsterFindMonsterByID) {
                this.focusedActor = null;
            }
            if (monsterDieInfo.itemDrop != null) {
                for (int i = 0; i < monsterDieInfo.itemDrop.length; i++) {
                    Dropable dropable = (Dropable) CreateActor(monsterDieInfo.itemDrop[i].itemCatagory, monsterDieInfo.itemDrop[i].itemTemplateID, monsterDieInfo.itemDrop[i].itemID, monsterDieInfo.itemDrop[i].x, monsterDieInfo.itemDrop[i].y, (byte) 0, (byte) -1);
                    dropable.startDropFrom(monsterFindMonsterByID.x, monsterFindMonsterByID.y, dropable.x, dropable.y);
                    this.actors.addElement(dropable);
                }
            }
            if (this.mainChar.ID == monsterDieInfo.attacker) {
                AttackResult.startEff(monsterDieInfo.effect, monsterFindMonsterByID.x, monsterFindMonsterByID.y - 25);
                try {
                    if (monsterDieInfo.hpLost > 0) {
                        Quest quest = mainQuest;
                        if (quest != null && quest.isWorking()) {
                            if (mainQuest.isKillMons()) {
                                mainQuest.killMonster(monsterFindMonsterByID.monster_type, monsterFindMonsterByID.getDisplayName());
                            } else if (mainQuest.isKillMonsByLv()) {
                                mainQuest.killMonsterByLv(abs(this.mainChar.level - monsterFindMonsterByID.level));
                            }
                        }
                        Quest quest2 = subQuest;
                        if (quest2 != null && quest2.isWorking()) {
                            if (subQuest.isKillMons()) {
                                subQuest.killMonster(monsterFindMonsterByID.monster_type, monsterFindMonsterByID.getDisplayName());
                            } else if (subQuest.isKillMonsByLv()) {
                                subQuest.killMonsterByLv(abs(this.mainChar.level - monsterFindMonsterByID.level));
                            }
                        }
                        Quest quest3 = clanQuest;
                        if (quest3 != null && quest3.isWorking()) {
                            if (clanQuest.isKillMons()) {
                                clanQuest.killMonster(monsterFindMonsterByID.monster_type, monsterFindMonsterByID.getDisplayName());
                            } else if (clanQuest.isKillMonsByLv()) {
                                clanQuest.killMonsterByLv(abs(this.mainChar.level - monsterFindMonsterByID.level));
                            }
                        }
                        if (this.mainChar.attkPower == 0) {
                            this.mainChar.attkPower = monsterDieInfo.hpLost;
                        }
                        if (this.mainChar.kilAllMonsterQuest(monsterFindMonsterByID.monster_type)) {
                            addChat(new ChatInfo("", "Hạ " + this.mainChar.infoQuest + " " + monsterFindMonsterByID.getDisplayName(), 0));
                            if (WindowInfoScr.idMonsterClanQuest != -1) {
                                if (WindowInfoScr.numberQuestGet + 1 >= WindowInfoScr.numberQuestAll) {
                                    WindowInfoScr.idMonsterClanQuest = (byte) -1;
                                    addChat(new ChatInfo("", "Hoàn thành nhiệm vụ.", 0));
                                } else {
                                    StringBuilder sbAppend = new StringBuilder().append("Hạ được ");
                                    short s = (short) (WindowInfoScr.numberQuestGet + 1);
                                    WindowInfoScr.numberQuestGet = s;
                                    addChat(new ChatInfo("", sbAppend.append((int) s).append("/").append((int) WindowInfoScr.numberQuestAll).toString(), 0));
                                }
                            }
                        }
                        if (monsterDieInfo.hpLost > 0) {
                            for (int i2 = 0; i2 < monsterDieInfo.x2; i2++) {
                                GCanvas.gameScr.startFlyText("-" + monsterDieInfo.hpLost, 0, monsterFindMonsterByID.x + (i2 % 2 == 0 ? (short) 0 : (short) -20), monsterFindMonsterByID.y - (i2 % 2 == 0 ? (short) 20 : (short) 30), 1, -2);
                            }
                            return;
                        }
                        return;
                    }
                    return;
                } catch (Exception unused) {
                    return;
                }
            }
            monsterDieInfo.hpLost = (short) (monsterDieInfo.hpLost / monsterDieInfo.x2);
            Char r0 = (Char) findCharByID(monsterDieInfo.attacker);
            if (r0 == null || monsterFindMonsterByID == null) {
                return;
            }
            r0.dir = Util.findDirection(r0, monsterFindMonsterByID);
            if (SkillManager.isSkillAeo(r0.clazz, monsterDieInfo.skillID / 10)) {
                return;
            }
            r0.startAttack(monsterFindMonsterByID, new AttackResult(monsterDieInfo.hpLost, monsterDieInfo.effect), monsterDieInfo.skillID, monsterDieInfo.level);
        }
    }

    public void startExplosionAt(short s, short s2) {
        this.actors.addElement(new Explosion(s, s2));
    }

    public Actor findCharByID(short s) {
        isFindChar = true;
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor.catagory == 0 && actor.ID == s) {
                isFindChar = false;
                return actor;
            }
        }
        isFindChar = false;
        return null;
    }

    private Monster findMonsterByID(short s) {
        isFindMonster = true;
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor.catagory == 1 && actor.ID == s) {
                isFindMonster = false;
                return (Monster) actor;
            }
        }
        isFindMonster = false;
        return null;
    }

    public Actor findActorByID(short s) {
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor.ID == s) {
                return actor;
            }
        }
        return null;
    }

    private DropItem findItemByID(short s) {
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor.catagory == 3 && actor.ID == s) {
                return (DropItem) actor;
            }
        }
        return null;
    }

    private Dropable findPotionByID(short s, int i) {
        int size = this.actors.size();
        for (int i2 = 0; i2 < size; i2++) {
            Actor actor = (Actor) this.actors.elementAt(i2);
            if (actor.catagory == i && actor.ID == s) {
                return (Dropable) actor;
            }
        }
        return null;
    }

    public void onMainCharInfo() {
        GameData.gI().getImage();
        this.mainChar.timeDelayRequestCharInfo = System.currentTimeMillis();
        if (this.mainChar.gender <= 0) {
            this.mainChar.gender = MainChar.GENDER_OF_CLAZZ[this.mainChar.clazz];
        }
        if (this.mainChar.level == 1 && this.mainChar.lvpercent == 0 && !this.mainChar.first) {
            this.first = true;
            MainChar mainChar = this.mainChar;
            mainChar.isFirstTimeGetPotion = true;
            mainChar.isFirstTimeGetMoney = true;
            mainChar.isFirstTimeGetItem = true;
        }
        if (this.mainChar.defend > 0 || this.mainChar.defend_magic > 0) {
            this.mainChar.effPhap = new Effect(this.mainChar.x, this.mainChar.y, this.mainChar.defend > this.mainChar.defend_magic ? 21 : 36);
        }
        this.mainChar.loadQuickSlot();
        PauseMenu.gI().initName();
        MenuWindows.gI().initName();
    }

    public void onMonsterInfo(MonsterInfo monsterInfo) {
        Monster monsterFindMonsterByID;
        if (this.readyGetGameLogic && (monsterFindMonsterByID = findMonsterByID(monsterInfo.id)) != null) {
            monsterFindMonsterByID.setInfo(monsterInfo);
        }
    }

    public void charOutGame(short s) {
        if (this.readyGetGameLogic) {
            for (int i = 0; i < this.actors.size(); i++) {
                Actor actor = (Actor) this.actors.elementAt(i);
                if (actor.catagory == 0 && actor.ID == s) {
                    actor.wantDestroy = true;
                    return;
                }
            }
        }
    }

    public void onBossAttackPlayer(Message message) {
        try {
            short s = message.reader().readShort();
            byte b = message.reader().readByte();
            Monster monsterFindMonsterByID = findMonsterByID(s);
            if (monsterFindMonsterByID != null) {
                byte b2 = message.reader().readByte();
                Vector vector = new Vector();
                for (int i = 0; i < b2; i++) {
                    short s2 = message.reader().readShort();
                    LiveActor liveActor = (LiveActor) findActorByID(s2);
                    if (liveActor != null) {
                        liveActor.damAttack = message.reader().readInt();
                        int i2 = message.reader().readInt();
                        if (this.mainChar.ID == s2) {
                            if (this.mainChar.comeHome) {
                                this.mainChar.state = (byte) 0;
                                this.mainChar.comeHome = false;
                            }
                            MainChar mainChar = this.mainChar;
                            mainChar.hp = i2;
                            mainChar.realHP = i2;
                            monsterFindMonsterByID.dir = Util.findDirection(monsterFindMonsterByID, this.mainChar);
                            if (this.focusedActor == null) {
                                this.focusedActor = monsterFindMonsterByID;
                            }
                            vector.addElement(this.mainChar);
                        } else {
                            if (monsterFindMonsterByID.monster_type != 90) {
                                short s3 = monsterFindMonsterByID.monster_type;
                            }
                            if (liveActor != null) {
                                liveActor.hp = i2;
                                liveActor.realHP = i2;
                                if (liveActor.hp <= 0) {
                                    ((Char) liveActor).state = (byte) 3;
                                }
                                monsterFindMonsterByID.dir = Util.findDirection(monsterFindMonsterByID, liveActor);
                            }
                            vector.addElement(liveActor);
                        }
                    } else {
                        message.reader().readInt();
                        message.reader().readInt();
                    }
                }
                monsterFindMonsterByID.startAttack(vector, b);
                try {
                    monsterFindMonsterByID.setIdSkill(message.reader().readUnsignedByte());
                    monsterFindMonsterByID.setByMap(message.reader().readUnsignedByte());
                } catch (Exception unused) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMonsterAttackPlayer(short s, short s2, int i, int i2) {
        Monster monsterFindMonsterByID;
        byte b;
        if (this.readyGetGameLogic && (monsterFindMonsterByID = findMonsterByID(s)) != null) {
            if (s2 == 32001) {
                monsterFindMonsterByID.setRemoveTarget();
                return;
            }
            if (monsterFindMonsterByID.monster_type != 84) {
                monsterFindMonsterByID.startAttack();
            }
            if (this.mainChar.ID == s2) {
                monsterFindMonsterByID.setTarget(this.mainChar);
                if (this.mainChar.comeHome) {
                    this.mainChar.state = (byte) 0;
                    this.mainChar.comeHome = false;
                }
                MainChar mainChar = this.mainChar;
                mainChar.hp = i2;
                mainChar.realHP = i2;
                if (monsterFindMonsterByID.monster_type == 90) {
                    startFlyText(i != 0 ? "-" + i : "MISS", i == 0 ? 0 : 4, this.mainChar.x, this.mainChar.y - 40, 0, -1);
                    monsterFindMonsterByID.startAttack(this.mainChar, i, (byte) 8, (byte) 4);
                    if (this.mainChar.hp <= 0) {
                        this.mainChar.state = (byte) 3;
                        return;
                    }
                    return;
                }
                monsterFindMonsterByID.dir = Util.findDirection(monsterFindMonsterByID, this.mainChar);
                if (this.focusedActor == null) {
                    this.focusedActor = monsterFindMonsterByID;
                }
                if (Util.abs(this.mainChar.x - monsterFindMonsterByID.x) + Util.abs(this.mainChar.y - monsterFindMonsterByID.y) < 48) {
                    this.mainChar.smalljump();
                    b = 0;
                    startFlyText(i != 0 ? "-" + i : "MISS", i == 0 ? 0 : 4, this.mainChar.x, this.mainChar.y - 40, 0, -1);
                    if (monsterFindMonsterByID.monster_type != 90) {
                        EffectManager.addHiEffect(this.mainChar.x, this.mainChar.y - 10, 9);
                    }
                    if (this.mainChar.hp <= 0) {
                        this.mainChar.state = (byte) 3;
                    }
                } else {
                    b = 0;
                    if (monsterFindMonsterByID.monster_type != 90) {
                        startNewMagicBeam(20, monsterFindMonsterByID, this.mainChar, monsterFindMonsterByID.x, monsterFindMonsterByID.y, i, (byte) 0);
                    }
                }
                this.mainChar.downDuarable();
                if (monsterFindMonsterByID.monster_type == 84) {
                    Vector vector = new Vector();
                    vector.addElement(this.mainChar);
                    monsterFindMonsterByID.startAttack(vector, b);
                    return;
                }
                return;
            }
            Char r5 = (Char) findCharByID(s2);
            if (r5 != null) {
                monsterFindMonsterByID.setTarget(r5);
                r5.hp = i2;
                r5.realHP = i2;
                if (r5.hp <= 0) {
                    r5.state = (byte) 3;
                }
                monsterFindMonsterByID.dir = Util.findDirection(monsterFindMonsterByID, r5);
                r5.smalljump();
                startFlyText(i != 0 ? "-" + i : "MISS", i == 0 ? 0 : 4, r5.x, r5.y - 40, 0, -1);
                if (monsterFindMonsterByID.monster_type == 90) {
                    monsterFindMonsterByID.startAttack(r5, i, (byte) 8, (byte) 4);
                    return;
                }
                EffectManager.hiEffects.addElement(new Effect(r5.x, r5.y - 10, 9));
                if (monsterFindMonsterByID.monster_type == 84) {
                    Vector vector2 = new Vector();
                    vector2.addElement(r5);
                    monsterFindMonsterByID.startAttack(vector2, (byte) 0);
                }
            }
        }
    }

    public void onPingBack() {
        this.pingDelay = System.currentTimeMillis() - this.lastTimePing;
        this.pingNextIn = 30;
    }

    public void onConnectOK() {
        GCanvas.startWaitDlg("Xin chờ..", true);
    }

    public void onConnectFail() {
        GCanvas.startOKDlg("Không thể kết nối. Vui lòng chọn server khác", new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
                ServerListScr.gI().switchToMe();
                Session_ME.gI().close();
            }
        });
    }

    public void onDisconnect() {
        if (GCanvas.currentScreen != GCanvas.loginScr) {
            if (autoFight) {
                autoFight = false;
                GCanvas.cmdNgatKetNoi.action.perform();
            } else {
                autoFight = false;
                GCanvas.msgdlg.setInfo("Bị ngắt kết nối.", (Command) null, new Command("OK", new IAction() {
                    @Override
                    public void perform() {
                        MoQuaScr.gI().resetGame();
                        GCanvas.instance.init();
                        Session_ME.gI().close();
                        Tilemap.loadMap(0, null);
                        ServerListScr.gI().switchToMe();
                        GCanvas.endDlg();
                    }
                }), (Command) null);
                GCanvas.currentDialog = GCanvas.msgdlg;
            }
        }
    }

    public boolean isOffLineMap(int i) {
        int i2 = 0;
        while (true) {
            short[] sArr = offMapID;
            if (i2 >= sArr.length) {
                return false;
            }
            if (i == sArr[i2]) {
                return true;
            }
            i2++;
        }
    }

    public short getIndexArrMap() {
        for (int i = 0; i < mapID.length; i++) {
            int i2 = 0;
            while (true) {
                short[] sArr = mapID[i];
                if (i2 < sArr.length) {
                    if (this.map == sArr[i2]) {
                        return idArrMap[i];
                    }
                    i2++;
                }
            }
        }
        return (short) -1;
    }

    public short getIndexArrMap(int i) {
        for (int i2 = 0; i2 < mapID.length; i2++) {
            int i3 = 0;
            while (true) {
                short[] sArr = mapID[i2];
                if (i3 < sArr.length) {
                    if (i == sArr[i3]) {
                        return idArrMap[i2];
                    }
                    i3++;
                }
            }
        }
        return (short) -1;
    }

    public short getMapLoad(int i) {
        for (int i2 = 0; i2 < mapID.length; i2++) {
            int i3 = 0;
            while (true) {
                short[] sArr = mapID[i2];
                if (i3 < sArr.length) {
                    if (i == sArr[i3]) {
                        return sArr[0];
                    }
                    i3++;
                }
            }
        }
        return (short) -1;
    }

    public void onMap(short s, short s2, short s3, short s4, String str, byte[] bArr) {
        // mod: vào lại đúng map đang đứng -> ép nạp lại map để không mất NPC
        if (AutoGuard.mapGuard(s)) {
            return;
        }
        boolean z;
        Cout.println("onMap: " + ((int) s));
        GCanvas.endDlg();
        this.mainChar.posTransRoad = null;
        this.mainChar.EffFace = null;
        this.focusedActor = null;
        this.VecTime.removeAllElements();
        objcountDown = null;
        charcountdonw = null;
        this.nearActors.removeAllElements();
        Vector vector = effManager;
        if (vector != null) {
            vector.removeAllElements();
        }
        congThanh = null;
        if (s4 == -1) {
            s4 = s;
        }
        if (s2 != -1 && s3 != -1) {
            MainChar mainChar = this.mainChar;
            short s5 = (short) ((s2 * 16) + 8);
            mainChar.xTo = s5;
            mainChar.x = s5;
            MainChar mainChar2 = this.mainChar;
            short s6 = (short) ((s3 * 16) + 8);
            mainChar2.yTo = s6;
            mainChar2.y = s6;
            if (this.mainChar.x / 16 <= 1) {
                MainChar mainChar3 = this.mainChar;
                mainChar3.x = (short) (mainChar3.x + 32);
            }
            if (this.mainChar.y / 16 <= 1) {
                MainChar mainChar4 = this.mainChar;
                mainChar4.y = (short) (mainChar4.y + 32);
            }
            MainChar mainChar5 = this.mainChar;
            mainChar5.xTo = mainChar5.x;
            MainChar mainChar6 = this.mainChar;
            mainChar6.yTo = mainChar6.y;
            if (this.mainChar.myPet != null) {
                setInfoCharPet(this.mainChar.myPet, this.mainChar);
            }
        }
        this.actors.removeAllElements();
        MainChar mainChar7 = this.mainChar;
        if (mainChar7 != null) {
            this.actors.addElement(mainChar7);
        }
        if (this.map != s) {
            this.map = s;
            Res.load.loadMap(s4, new IAction() {
                @Override
                public void perform() {
                    GCanvas.gameScr.loadCamera();
                    GameScr.this.posNpcRequest = null;
                    if (GameScr.this.current_npc_talk != -1 || GameScr.this.idNpcReceive != -1) {
                        GameScr gameScr = GameScr.this;
                        gameScr.set_npc_request(gameScr.current_npc_talk);
                        Cout.println("KIEM TRA = " + ((int) GameScr.this.current_npc_talk));
                    }
                    GCanvas.gameScr.switchToMe();
                    GameScr.this.gameService.changeMapOK(-500, 0, 0);
                    try {
                        GameScr.this.setAutoTask();
                        GameScr.this.setObjDynamicType();
                    } catch (Exception unused) {
                        Cout.println("ERROR ONMAP");
                    }
                }
            }, bArr);
            z = true;
        } else {
            z = false;
        }
        if (s4 == 107) {
            this.mainChar.delay = 1;
            this.mainChar.realTime = 1;
            this.mainChar.timeWait2Board = System.currentTimeMillis();
        } else if (s4 == 10) {
            this.mainChar.delay = -1;
            this.mainChar.realTime = -1;
            if (this.mainChar.potions[20] > 0) {
                int[] iArr = this.mainChar.potions;
                iArr[20] = iArr[20] - 1;
            }
        }
        if (s4 != 201) {
            timeChiemThanh = null;
        }
        System.out.println("nhan cmd on map " + ((int) s4));
        this.mainChar.state = (byte) 0;
        this.mainChar.comeHome = false;
        try {
            if (GCanvas.loginScr != null) {
                GCanvas.loginScr = null;
                CharSelectScr.me = null;
                GCanvas.imgLogo = null;
                ServerListScr.close();
            }
            GCanvas.currentDialog = null;
            GCanvas.clearKeyHold();
            GCanvas.clearKeyPressed();
            Tilemap.setNameMap(s4);
            nameMap = str;
            if (!z) {
                GCanvas.gameScr.switchToMe();
                setAutoTask();
                setObjDynamicType();
            }
        } catch (Exception unused) {
            Cout.println("ERROR ONMAP");
        }
        bDragon[0] = new Boss_Dragon();
    }

    public void setObjDynamicType() {
        for (int i = 0; i < this.objDynamic.size(); i++) {
            DynamicObj dynamicObj = (DynamicObj) this.objDynamic.elementAt(i);
            if (!this.actors.contains(dynamicObj)) {
                this.actors.addElement(dynamicObj);
                for (int i2 = 0; i2 < dynamicObj.w / 2; i2++) {
                    Tilemap.type[((dynamicObj.y / 16) * Tilemap.w) + ((dynamicObj.x / 16) - i2)] = 2;
                    Tilemap.type[((dynamicObj.y / 16) * Tilemap.w) + (dynamicObj.x / 16) + i2] = 2;
                    for (int i3 = 0; i3 < dynamicObj.h / 2; i3++) {
                        Tilemap.type[(((dynamicObj.y / 16) - i3) * Tilemap.w) + ((dynamicObj.x / 16) - i2)] = 2;
                        Tilemap.type[(((dynamicObj.y / 16) - i3) * Tilemap.w) + (dynamicObj.x / 16) + i2] = 2;
                    }
                }
            }
        }
        this.objDynamic.removeAllElements();
    }

    public void setAutoTask() {
        if (Char.skillLevelLearnt[0] == 0) {
            if ((Tilemap.lv == 0 && Tilemap.lv == 101) || Tilemap.lv == 105 || iTaskAuto != 0) {
                return;
            }
            iTaskAuto = 1;
            GCanvas.startYesNoDlg("Bạn có muốn hệ thống tự động làm nhiệm vụ cho bạn hay không ?", new IAction() {
                @Override
                public void perform() {
                    GCanvas.startWaitDlg();
                    try {
                        GameScr.mapFind = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, Tilemap.w, Tilemap.h);
                        GameScr.iTaskAuto = 2;
                    } catch (Exception unused) {
                    }
                    GCanvas.endDlg();
                }
            }, new IAction() {
                @Override
                public void perform() {
                    GameScr.iTaskAuto = 1;
                    GCanvas.endDlg();
                    GameScr.this.addChat(new ChatInfo("", "Hãy đi gặp các bậc đàn anh trong làng để học chiêu thức.", 0));
                }
            });
        }
    }

    public int getPosNameMap(int i) {
        for (int i2 = 0; i2 < mapID.length; i2++) {
            int i3 = 0;
            while (true) {
                short[] sArr = mapID[i2];
                if (i3 < sArr.length) {
                    if (i == sArr[i3]) {
                        return i3;
                    }
                    i3++;
                }
            }
        }
        return -1;
    }

    public void onLoginFail(String str) {
        if (str.startsWith("1")) {
            GCanvas.startOKDlg(str.substring(1), new IAction() {
                @Override
                public void perform() {
                    GCanvas.currentDialog = null;
                    ServerListScr.gI().switchToMe();
                    Session_ME.gI().close();
                }
            });
        } else {
            GCanvas.startOKDlg(str);
        }
    }

    public void listServer(String str, final String[] strArr, final short[] sArr, final String[] strArr2) {
        GCanvas.endDlg();
        GCanvas.startOKDlg(str, new IAction() {
            @Override
            public void perform() {
                GCanvas.currentDialog = null;
                ServerListScr.gI().switchToMe();
                ServerListScr.address = strArr;
                ServerListScr.port = sArr;
                ServerListScr.nameServer = strArr2;
                RMS.saveRMSStringIp(strArr, sArr, strArr2);
                Session_ME.gI().close();
            }
        });
    }

    public void onCharWearingInfo(short s, Vector vector, byte b, byte b2, String str, AnimalMove animalMove, short[] sArr, byte b3) {
        if (s == this.mainChar.ID) {
            this.mainChar.idFashion = b;
            this.mainChar.idModel = sArr;
            this.mainChar.setWearingInfo(vector);
            this.mainChar.idAnimal = b2;
            this.mainChar.infoAnimal = str;
            this.mainChar.idPaintIconAnimal = b3;
            if (animalMove != null) {
                animalMove.idSameMyOwnerID = this.mainChar.ID;
                setCharPet(animalMove, this.mainChar);
                return;
            } else {
                if (this.mainChar.myPet != null) {
                    this.mainChar.myPet.status = (short) 3;
                    this.mainChar.myPet.timeMagic = 20;
                    return;
                }
                return;
            }
        }
        Char r4 = (Char) findCharByID(s);
        if (r4 != null) {
            r4.idFashion = b;
            r4.idModel = sArr;
            r4.setWearingInfo(vector);
            r4.idAnimal = b2;
            r4.infoAnimal = str;
            r4.idPaintIconAnimal = b3;
            if (animalMove != null) {
                animalMove.idSameMyOwnerID = r4.ID;
                setCharPet(animalMove, r4);
            } else if (r4.myPet != null) {
                r4.myPet.status = (short) 3;
                r4.myPet.timeMagic = 20;
            }
        }
    }

    public void setCharPet(AnimalMove animalMove, Char r7) {
        if (animalMove.type == 5) {
            clearCharPet(r7.ID);
            Pet pet = new Pet(r7, animalMove.imgID);
            setInfoCharPet(animalMove, r7);
            this.actors.addElement(pet);
            return;
        }
        clearCharPet(r7.ID);
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor != null && actor.catagory == animalMove.catagory && !actor.ispetTool()) {
                AnimalMove animalMove2 = (AnimalMove) actor;
                if (animalMove2.idSameMyOwnerID == animalMove.idSameMyOwnerID) {
                    animalMove2.type = animalMove.type;
                    animalMove2.imgID = animalMove.imgID;
                    animalMove2.sumFrame = animalMove.sumFrame;
                    animalMove2.infoPet = animalMove.infoPet;
                    animalMove2.totalTime = animalMove.totalTime;
                    animalMove2.timeStart = animalMove.timeStart;
                    setInfoCharPet(animalMove2, r7);
                    return;
                }
            }
        }
        animalMove.setFrameType4();
        setInfoCharPet(animalMove, r7);
        this.actors.addElement(animalMove);
    }

    public void clearCharPet(short s) {
        for (int i = 0; i < this.actors.size(); i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor != null && actor.catagory == 12 && actor.ID == s && actor.ispetTool()) {
                this.actors.removeElement(actor);
            }
        }
    }

    public void setInfoCharPet(AnimalMove animalMove, Char r5) {
        int i;
        animalMove.hImg = (short) 1;
        animalMove.wImg = (short) 1;
        animalMove.myOwner = r5;
        r5.myPet = animalMove;
        if (r5.dir == 2 || r5.dir == 1) {
            i = r5.x + 20;
        } else {
            i = r5.x - 20;
        }
        short s = (short) i;
        animalMove.xTo = s;
        animalMove.x = s;
        short s2 = r5.y;
        animalMove.yTo = s2;
        animalMove.y = s2;
        if (animalMove.type != 1) {
            animalMove.addY = (short) ((-r5.height) + 15);
        } else {
            animalMove.addY = (short) 0;
        }
        animalMove.v = (byte) r5.speed;
        animalMove.status = (short) 0;
        animalMove.idFrame = (short) 0;
        animalMove.count = (short) 0;
    }

    public void onViewAnimalInfo(Char r5) {
        WindowInfoScr.gI().setInfo(0, false, new byte[]{31});
        WindowInfoScr.charWearing = r5;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr.gI().name = "Trang bị thú";
        GCanvas.endDlg();
    }

    public void onAnimalWearingInfo(short s, Vector vector, Bitmap bitmap, byte b, int i, int i2, byte b2) {
        if (s == this.mainChar.ID) {
            this.mainChar.imgAnimal = bitmap;
            this.mainChar.numFrame = b;
            this.mainChar.wimg = i;
            this.mainChar.himg = i2;
            this.mainChar.timeChangeFrame = b2;
            this.mainChar.setAnimalWearingInfo(vector);
            return;
        }
        Char r2 = (Char) findCharByID(s);
        if (r2 != null) {
            r2.imgAnimal = bitmap;
            r2.numFrame = b;
            r2.wimg = i;
            r2.himg = i2;
            r2.timeChangeFrame = b2;
            r2.setAnimalWearingInfo(vector);
            onViewAnimalInfo(r2);
        }
    }

    public void onCharInventory(long j, int[] iArr, Vector vector, Vector vector2, int i) {
        this.mainChar.setInventory(j, iArr, vector, vector2, i);
        cancelTrade();
        if (GCanvas.currentScreen == WindowInfoScr.gI() && WindowInfoScr.index[WindowInfoScr.focusTab] == 0) {
            WindowInfoScr.gI().isLoad = true;
            MenuWindows.gI().isLoad = true;
            PauseMenu.gI().showMe(0);
            WindowInfoScr.gI().isLoad = false;
            MenuWindows.gI().isLoad = false;
        }
        if (GCanvas.currentScreen == MenuWindows.gI() && MenuWindows.focusTabChinh == 2) {
            MenuWindows.gI().isLoad = true;
            MenuWindows.gI().setCurTab();
            MenuWindows.gI().isLoad = false;
        }
    }

    public void onGetItemFromGround(short s, ItemInInventory itemInInventory) {
        DropItem dropItemFindItemByID = findItemByID(itemInInventory.itemOldID);
        if (dropItemFindItemByID != null) {
            Char r1 = (Char) findCharByID(s);
            if (r1 != null) {
                if (r1 == this.mainChar) {
                    addChat(new ChatInfo("", "Nhặt được " + dropItemFindItemByID.getDisplayName(), 1));
                }
                dropItemFindItemByID.startFlyTo(r1.x, r1.y);
            }
            dropItemFindItemByID.wantDestroy = true;
        }
        if (s == this.mainChar.ID) {
            Char.inventory.addElement(itemInInventory);
            if (this.mainChar.isFirstTimeGetItem) {
                GCanvas.startOKDlg("Bạn vừa nhặt được một món đồ.", false, 10);
                this.mainChar.isFirstTimeGetItem = false;
            }
        }
    }

    public void onGetPotionFromGround(short s, short s2, short s3, short s4) {
        DropPotion dropPotion = (DropPotion) findPotionByID(s2, 4);
        if (dropPotion != null) {
            Char r14 = (Char) findCharByID(s);
            if (r14 != null) {
                if (r14 == this.mainChar) {
                    startFlyText("+" + ((int) s4) + "" + dropPotion.getDisplayName2().toLowerCase(), 2, this.mainChar.x - 10, this.mainChar.y - 30, 1, -1);
                    if (this.mainChar.getItemQuest(dropPotion.item_template_id)) {
                        addChat(new ChatInfo("", "Nhặt " + this.mainChar.infoQuest + " " + dropPotion.getDisplayName2(), 0));
                    }
                }
                dropPotion.startFlyTo(r14.x, r14.y);
            } else {
                dropPotion.wantDestroy = true;
            }
        }
        if (s == this.mainChar.ID) {
            if (s3 == 0) {
                this.mainChar.charXu += (long) s4;
            } else {
                int[] iArr = this.mainChar.potions;
                iArr[s3] = iArr[s3] + s4;
            }
            if (s3 == 0) {
                if (this.mainChar.isFirstTimeGetMoney) {
                    GCanvas.startOKDlg("Bạn vừa nhặt được xu Bạn có thể dùng xu để mua HP, MP và đồ dùng khác.", false, 1);
                    this.mainChar.isFirstTimeGetMoney = false;
                    return;
                }
                return;
            }
            byte b = idItemClanquest;
            if (b != -1 && b == s3) {
                WindowInfoScr.numberQuestGet = (short) (WindowInfoScr.numberQuestGet + 1);
                if (WindowInfoScr.numberQuestGet >= WindowInfoScr.numberQuestAll) {
                    idItemClanquest = (byte) -1;
                    addChat(new ChatInfo("", "Hoàn thành nhiệm vụ.", 0));
                } else {
                    addChat(new ChatInfo("", "Nhặt được: " + ((int) WindowInfoScr.numberQuestGet) + "/" + ((int) WindowInfoScr.numberQuestAll), 0));
                }
            }
            if (this.mainChar.isFirstTimeGetPotion) {
                GCanvas.startOKDlg("Bạn vừa nhặt được một vật phẩm dùng được. Xin vào mục hành trang để xem.", false, 1);
                this.mainChar.isFirstTimeGetPotion = false;
            }
        }
    }

    public void onGetGemFromGround(short s, short s2, byte b) {
        DropGemItem dropGemItem = (DropGemItem) findPotionByID(s2, b);
        if (dropGemItem != null) {
            Char r1 = (Char) findCharByID(s);
            if (r1 != null) {
                dropGemItem.startFlyTo(r1.x, r1.y);
            } else {
                dropGemItem.wantDestroy = true;
            }
        }
    }

    public void onGetItemQuestFromGround(short s, short s2, byte b) {
        ItemQuest itemQuest = (ItemQuest) findPotionByID(s2, 14);
        if (itemQuest != null) {
            Char r0 = (Char) findCharByID(s);
            if (r0 != null) {
                itemQuest.startFlyTo(r0.x, r0.y);
            } else {
                itemQuest.wantDestroy = true;
            }
            if (this.mainChar.ID == s) {
                Quest quest = mainQuest;
                if (quest != null && quest.isWorking()) {
                    mainQuest.getItemQuest(b);
                }
                Quest quest2 = subQuest;
                if (quest2 != null && quest2.isWorking()) {
                    subQuest.getItemQuest(b);
                }
                Quest quest3 = clanQuest;
                if (quest3 == null || !quest3.isWorking()) {
                    return;
                }
                clanQuest.getItemQuest(b);
            }
        }
    }

    public void dellPotionQuest() {
        for (int i = 10; i < 14; i++) {
            this.mainChar.potions[i] = 0;
        }
    }

    private void setItemInfo(Vector vector, ItemInInventory itemInInventory) {
        ItemInInventory itemByID = getItemByID(vector, itemInInventory.itemID);
        if (itemByID != null) {
            itemByID.isEnoughData = true;
            itemByID.clazz = itemInInventory.clazz;
            itemByID.dayUse = itemInInventory.dayUse;
            itemByID.property = itemInInventory.property;
            itemByID.allAttribute = itemInInventory.allAttribute;
            itemByID.itemAttribute = itemInInventory.itemAttribute;
            itemByID.durable = itemInInventory.durable;
            itemByID.lastTime = System.currentTimeMillis();
            itemByID.lock = itemInInventory.lock;
            if (itemInInventory.plusTemplate > -1) {
                itemByID.plusTemplate = itemInInventory.plusTemplate;
            }
        }
    }

    public void onItemInfo(ItemInInventory itemInInventory) {
        setItemInfo(Char.inventory, itemInInventory);
        if (MenuWindows.charWearing != null) {
            setItemInfo(MenuWindows.charWearing.wearingItems, itemInInventory);
            if ((MenuWindows.charWearing.useHorse != -1 || MenuWindows.charWearing.idhorse > -1) && MenuWindows.charWearing.wearingAnimal != null && MenuWindows.charWearing.wearingAnimal.size() > 0) {
                setItemInfo(MenuWindows.charWearing.wearingAnimal, itemInInventory);
            }
        }
        if (WindowInfoScr.charWearing != null) {
            setItemInfo(WindowInfoScr.charWearing.wearingItems, itemInInventory);
            if ((WindowInfoScr.charWearing.useHorse != -1 || WindowInfoScr.charWearing.idhorse > -1) && WindowInfoScr.charWearing.wearingAnimal != null && WindowInfoScr.charWearing.wearingAnimal.size() > 0) {
                setItemInfo(WindowInfoScr.charWearing.wearingAnimal, itemInInventory);
            }
        }
        setItemInfo(this.mainChar.tItems, itemInInventory);
        setItemInfo(this.mainChar.rItems, itemInInventory);
        setItemInfo(Char.itembag, itemInInventory);
        setItemInfo(WindowInfoScr.gI().sellItem, itemInInventory);
        if (WindowInfoScr.gI().listItemAnimal != null && WindowInfoScr.gI().listItemAnimal.size() > 0) {
            setItemInfo(WindowInfoScr.gI().listItemAnimal, itemInInventory);
        }
        if (GCanvas.currentScreen == MenuWindows.gI()) {
            MenuWindows.gI().closeText();
            MenuWindows.gI().center.action.perform();
        }
        if (GCanvas.currentScreen == WindowInfoScr.gI()) {
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
        if (GCanvas.currentScreen == ScreenDapDo.gI()) {
            ScreenDapDo.gI().setInfoItem(itemInInventory.itemID);
        } else if (GCanvas.currentScreen == GCanvas.shop) {
            GCanvas.shop.onItemInfo();
        }
    }

    public void onUseItemPK(short s, int i, int i2) {
        Char r2 = (Char) findCharByID(s);
        if (r2 == null) {
            return;
        }
        if (i == 1) {
            r2.pk = (byte) i2;
        } else {
            r2.pk = (byte) 0;
        }
    }

    public void onUsePotion(short s, byte b, short s2, int i, int i2) {
        String str;
        String str2;
        String str3;
        String str4;
        Char r11 = (Char) findCharByID(s);
        if (r11 == null) {
            return;
        }
        if (i <= 0) {
            r11.hp = 0;
            r11.realHP = 0;
            r11.state = (byte) 3;
            if (s == this.mainChar.ID) {
                GCanvas.currentDialog = null;
                return;
            }
            return;
        }
        if (b != 34 && b != 82 && b != 85) {
            switch (b) {
                case 1:
                case 2:
                case 3:
                    if (r11.hp < i) {
                        r11.realHP = i;
                        r11.hp = i;
                    }
                    if (s2 > 0) {
                        str4 = "hp+";
                    } else {
                        str4 = "hp" + ((int) s2);
                    }
                    startFlyText(str4, 4, r11.x - 10, r11.y - 30, 0, -1);
                    if (s == this.mainChar.ID) {
                        GCanvas.currentDialog = null;
                    }
                    break;
                case 4:
                case 5:
                case 6:
                    if (r11.mp < i) {
                        r11.mp = i;
                    }
                    if (s2 > 0) {
                        str3 = "mp+";
                    } else {
                        str3 = "mp" + ((int) s2);
                    }
                    startFlyText(str3, 3, r11.x - 10, r11.y - 40, 0, -1);
                    str = "hp";
                    break;
                default:
                    switch (b) {
                        case 21:
                        case 22:
                            if (r11.hp < i) {
                                r11.realHP = i;
                                r11.hp = i;
                            }
                            if (s2 > 0) {
                                str4 = "hp+";
                            } else {
                                str4 = "hp" + ((int) s2);
                            }
                            startFlyText(str4, 4, r11.x - 10, r11.y - 30, 0, -1);
                            if (s == this.mainChar.ID) {
                                GCanvas.currentDialog = null;
                            }
                            break;
                        case 23:
                        case 24:
                            if (r11.mp < i) {
                                r11.mp = i;
                            }
                            if (s2 > 0) {
                                str3 = "mp+";
                            } else {
                                str3 = "mp" + ((int) s2);
                            }
                            startFlyText(str3, 3, r11.x - 10, r11.y - 40, 0, -1);
                            str = "hp";
                            break;
                    }
                    break;
            }
            MenuWindows.gI().setCurTab();
        }
        str = "hp";
        if (i2 == 1) {
            if (r11.hp < i) {
                r11.realHP = i;
                r11.hp = i;
            }
            startFlyText(s2 > 0 ? "hp+" : str + ((int) s2), 4, r11.x - 10, r11.y - 30, 0, -1);
        } else {
            if (r11.mp < i) {
                r11.mp = i;
            }
            if (s2 > 0) {
                str2 = "mp+";
            } else {
                str2 = "mp" + ((int) s2);
            }
            startFlyText(str2, 3, r11.x - 10, r11.y - 40, 0, -1);
        }
        MenuWindows.gI().setCurTab();
    }

    private ItemInInventory getItemByID(Vector vector, short s) {
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(i);
            if (itemInInventory.itemID == s) {
                return itemInInventory;
            }
        }
        return null;
    }

    public void onPutItem2Bag(short s) {
        ItemInInventory itemByID = getItemByID(Char.inventory, s);
        if (itemByID != null) {
            Char.inventory.removeElement(itemByID);
            if (this.mainChar.isExistInvector(Char.itembag, itemByID) == -1) {
                Char.itembag.addElement(itemByID);
                return;
            }
            return;
        }
        if (WindowInfoScr.gI() == GCanvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 22) {
            WindowInfoScr.gI().setSizeKeepItem(0);
        }
    }

    public void onGetItemFromBag(short s) {
        ItemInInventory itemByID = getItemByID(Char.itembag, s);
        if (itemByID != null) {
            Char.itembag.removeElement(itemByID);
            if (this.mainChar.isExistInvector(Char.inventory, itemByID) == -1) {
                Char.inventory.addElement(itemByID);
            }
        }
    }

    public void onNPCKeepItem(Vector vector) {
        Char.itembag = vector;
        showWindow(0, true, new byte[]{22});
    }

    public void onNPCSellPotion(byte[] bArr) {
        this.shop.removeAllElements();
        for (byte b : bArr) {
            ItemInInventory itemInInventory = new ItemInInventory();
            itemInInventory.catagory = (byte) 4;
            itemInInventory.potionType = b;
            itemInInventory.isEnoughData = true;
            itemInInventory.isShopItem = true;
            this.shop.addElement(itemInInventory);
        }
        showWindow(1, true, new byte[]{0, 19});
    }

    public void onShopSpecial(Vector vector) {
        Vector vector2 = Res.shopTemplate;
        this.shopSpecial = vector2;
        vector2.removeAllElements();
        int size = Res.shopTemplate.size();
        for (int i = 0; i < size; i++) {
            GemTemp gemTemp = (GemTemp) Res.shopTemplate.elementAt(i);
            if (gemTemp.isSell) {
                this.shopSpecial.addElement(gemTemp);
            }
        }
    }

    public void onNPCSellGemItem(Vector vector) {
        this.shop.removeAllElements();
        int size = Res.gemTemplate.size();
        for (int i = 0; i < size; i++) {
            GemTemp gemTemp = (GemTemp) Res.gemTemplate.elementAt(i);
            if (gemTemp.isSell) {
                this.shop.addElement(gemTemp);
            }
        }
        showWindow(1, true, new byte[]{0, 10});
    }

    public void onNPCSellItem(Vector vector) {
        this.shop = vector;
        ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(0);
        ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
        if ((item.type > 2 && item.type < 8) || (item.type >= 14 && item.type <= 18)) {
            WindowInfoScr.gI().setInfo(1, true, new byte[]{0, 9});
        } else {
            WindowInfoScr.gI().setInfo(0, true, new byte[]{11, 12, 13, 14, 15, 16, 17, 18, 0});
        }
        WindowInfoScr.gI().switchToMe();
    }

    public void onChat(short s, String str) {
        Actor actorFindActorByID = findActorByID(s);
        if (actorFindActorByID != null) {
            actorFindActorByID.chat = null;
            if (actorFindActorByID instanceof Char) {
                Char r1 = (Char) actorFindActorByID;
                if (r1.idBoss != -1) {
                    addChat(actorFindActorByID, str, 500);
                    return;
                } else {
                    addChat(actorFindActorByID, str, 50);
                    MessageScr.gI().addTab(r1.name + ": " + str, null, 0);
                    return;
                }
            }
            addChat(actorFindActorByID, str, 50);
        }
    }

    public void onSellItem(short s) {
        ItemInInventory itemByID = getItemByID(Char.inventory, s);
        if (itemByID != null) {
            Char.inventory.removeElement(itemByID);
            ItemTemplate item = Res.getItem(itemByID.charClazz, itemByID.idTemplate);
            this.mainChar.charXu += (long) (item.price / 5);
            GCanvas.startOKDlg("Đã bán thành công.", false);
        }
    }

    public void onSellGemItem(short s) {
        GemTemplate gemByRealID = GemTemplate.getGemByRealID(s);
        if (gemByRealID != null) {
            gemByRealID.number--;
            if (gemByRealID.number <= 0) {
                MainChar.gemItem.removeElement(gemByRealID);
                GemTemp gemByID = Res.getGemByID(gemByRealID.id);
                this.mainChar.charXu += (long) (gemByID.price / 5);
                GCanvas.startOKDlg("Đã bán thành công.");
            }
        }
    }

    public void onGiveItemToGround(short s, short s2, ItemDropInfo itemDropInfo) {
        ItemInInventory itemByID = getItemByID(Char.inventory, s2);
        if (itemByID != null) {
            Char.inventory.removeElement(itemByID);
            Dropable dropable = (Dropable) CreateActor(itemDropInfo.itemCatagory, itemDropInfo.itemTemplateID, itemDropInfo.itemID, itemDropInfo.x, itemDropInfo.y, (byte) 0, (byte) -1);
            dropable.startDropFrom(this.mainChar.x, this.mainChar.y, dropable.x, dropable.y);
            this.actors.addElement(dropable);
            if (this.mainChar.ID == s) {
                GCanvas.startOKDlg("Đã bỏ vật phẩm ra đất.");
                return;
            }
            return;
        }
        Dropable dropable2 = (Dropable) CreateActor(itemDropInfo.itemCatagory, itemDropInfo.itemTemplateID, itemDropInfo.itemID, itemDropInfo.x, itemDropInfo.y, (byte) 0, (byte) -1);
        dropable2.startDropFrom(this.mainChar.x, this.mainChar.y, dropable2.x, dropable2.y);
        this.actors.addElement(dropable2);
    }

    public void onSetXP(short s, short s2, int i) {
        Char r8 = (Char) findCharByID(s);
        if (r8 != null) {
            r8.lvpercent = s2;
            startFlyText("+" + i + "xp", 2, r8.x, r8.y - 30, -1, -1);
        }
    }

    public void onCharProperties(short s, Property[] propertyArr) {
        Char r3 = (Char) findCharByID(s);
        if (r3 != null) {
            for (Property property : propertyArr) {
                r3.setProperty(property);
            }
        }
    }

    public void onLevelUp(short s, byte b, int i, int i2) {
        Char r9 = (Char) findCharByID(s);
        if (r9 == null) {
            return;
        }
        r9.level = b;
        r9.lockUpdate = 30;
        r9.dir = (short) 0;
        r9.maxhp = i;
        r9.maxmp = i2;
        r9.hp = i;
        r9.mp = i2;
        r9.realHP = r9.hp;
        startFlyText("level-up", 3, r9.x, r9.y - 30, -1, 1);
        startFlyText("level-up", 3, r9.x, r9.y - 30, 1, -1);
        startFlyText("level-up", 3, r9.x, r9.y - 30, -1, -1);
        startFlyText("level-up", 3, r9.x, r9.y - 30, 1, 1);
        if (r9 != null) {
            EffectManager.addHiEffect(r9.x, r9.y - 30, 18);
        }
        if (r9 == this.mainChar) {
            addChat(new ChatInfo("", "Đạt được level " + ((int) b), 1));
            if (b == 2) {
                GCanvas.startOKDlg("Xin chúc mừng! Bạn vừa lên cấp. Chọn menu, Pokémon để tăng điểm tiềm năng và kỹ năng", false, 20);
            }
        }
    }

    public void onRemoveActor(short s, byte b) {
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor.catagory == b && actor.ID == s) {
                this.actors.removeElement(actor);
                return;
            }
        }
    }

    public void onCreadtePTOK(short s) {
        this.mainChar.IDParty = s;
        MainChar mainChar = this.mainChar;
        mainChar.IDMasterParty = mainChar.ID;
        int i = tempIDActorParty;
        if (i != -1) {
            this.gameService.invite2Party(i, 0);
            tempIDActorParty = -1;
        }
    }

    public void onAdd2Party(final Message message) {
        // mod: lời mời vào nhóm - auto up tự đồng ý / bỏ qua người lạ
        if (AutoGuard.nhomGuard(message)) {
            return;
        }
        try {
            byte b = message.reader().readByte();
            if (b != 0) {
                if (b == 1) {
                    short s = message.reader().readShort();
                    String utf = message.reader().readUTF();
                    byte b2 = message.reader().readByte();
                    if (Char.party.size() <= 0) {
                        MainChar mainChar = this.mainChar;
                        mainChar.IDMasterParty = mainChar.ID;
                    }
                    this.mainChar.add2Party(new PartyInfo(s, utf, b2, message.reader().readByte()));
                    addChat(new ChatInfo("", utf + " đã tham gia nhóm.", 0));
                    return;
                }
                if (b == 2) {
                    GCanvas.startOKDlg(message.reader().readUTF() + " từ chối vào nhóm.");
                    return;
                } else {
                    if (b == 3) {
                        while (message.reader().available() > 0) {
                            PartyInfo partyInfo = new PartyInfo(message.reader().readShort(), message.reader().readUTF(), message.reader().readByte(), message.reader().readByte());
                            addChat(new ChatInfo("", partyInfo.name + " đã vào nhóm.", 0));
                            this.mainChar.add2Party(partyInfo);
                        }
                        return;
                    }
                    return;
                }
            }
            final short s2 = message.reader().readShort();
            final short s3 = message.reader().readShort();
            final String utf2 = message.reader().readUTF();
            final byte b3 = message.reader().readByte();
            message.reader().readShort();
            final Message message2 = new Message((byte) 49);
            if (autoFight && GCanvas.autoTab.isAutoParty && !this.onoffInvite) {
                try {
                    message2.writer().writeByte(1);
                    message2.writer().writeShort(s3);
                    this.gameService.session.sendMessage(message2);
                    message2.cleanup();
                    this.mainChar.add2Party(new PartyInfo(s3, utf2, b3, message.reader().readByte()));
                    this.mainChar.IDMasterParty = s3;
                    this.mainChar.IDParty = s2;
                    while (message.reader().available() > 0) {
                        this.mainChar.add2Party(new PartyInfo(message.reader().readShort(), message.reader().readUTF(), message.reader().readByte(), message.reader().readByte()));
                    }
                    addChat(new ChatInfo("", "Đã tham gia nhóm", 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GCanvas.currentDialog = null;
                return;
            }
            GCanvas.startYesNoDlg(utf2 + " mời bạn tham gia nhóm.", new IAction() {
                @Override
                public void perform() {
                    try {
                        message2.writer().writeByte(1);
                        message2.writer().writeShort(s3);
                        GameScr.this.gameService.session.sendMessage(message2);
                        message2.cleanup();
                        GameScr.this.mainChar.add2Party(new PartyInfo(s3, utf2, b3, message.reader().readByte()));
                        GameScr.this.mainChar.IDMasterParty = s3;
                        GameScr.this.mainChar.IDParty = s2;
                        while (message.reader().available() > 0) {
                            GameScr.this.mainChar.add2Party(new PartyInfo(message.reader().readShort(), message.reader().readUTF(), message.reader().readByte(), message.reader().readByte()));
                        }
                        GameScr.this.addChat(new ChatInfo("", "Đã tham gia nhóm", 0));
                    } catch (Exception e2) {
                        System.out.println("LOI KHI GUI OK VAO PARTY LEN SERVER");
                        e2.printStackTrace();
                    }
                    GCanvas.currentDialog = null;
                }
            }, new IAction() {
                @Override
                public void perform() {
                    try {
                        message2.writer().writeByte(2);
                        message2.writer().writeShort(s3);
                        GameScr.this.gameService.session.sendMessage(message2);
                        message2.cleanup();
                    } catch (Exception unused) {
                    }
                    GCanvas.currentDialog = null;
                }
            });
        } catch (Exception unused) {
            Cout.println("LOI TRONG ONADD PARTY");
        }
    }

    public void onKickOutParty(Message message) {
        try {
            byte b = message.reader().readByte();
            if (b != 0 && b != 2) {
                if (b == 1) {
                    this.mainChar.IDParty = (short) -1;
                    this.mainChar.IDMasterParty = (short) -1;
                    Char.party.removeAllElements();
                    addChat(new ChatInfo("", "Nhóm đã bị giải tán", 0));
                    return;
                }
                return;
            }
            short s = message.reader().readShort();
            if (s == this.mainChar.ID) {
                this.mainChar.IDParty = (short) -1;
                this.mainChar.IDMasterParty = (short) -1;
                Char.party.removeAllElements();
                if (b == 0) {
                    addChat(new ChatInfo("", "Bị đuổi khỏi nhóm", 0));
                    return;
                } else {
                    addChat(new ChatInfo("", "Rời khỏi nhóm", 0));
                    return;
                }
            }
            String strRemoveUserParty = this.mainChar.removeUserParty(s);
            if (!strRemoveUserParty.equals("")) {
                if (b == 0) {
                    addChat(new ChatInfo("", strRemoveUserParty + " bị đuổi khỏi nhóm", 0));
                } else {
                    addChat(new ChatInfo("", strRemoveUserParty + " rời khỏi nhóm", 0));
                }
            }
            if (Char.party.size() <= 0) {
                Char.party.removeAllElements();
                this.mainChar.IDParty = (short) -1;
                this.mainChar.IDMasterParty = (short) -1;
                addChat(new ChatInfo("", "Nhóm đã bị giải tán", 0));
            }
        } catch (Exception unused) {
        }
    }

    public void onCharUseBuff(Message message) {
        try {
            byte b = message.reader().readByte();
            short s = message.reader().readShort();
            int i = 0;
            if (b == 2) {
                int size = this.actors.size();
                for (int i2 = 0; i2 < size; i2++) {
                    Actor actor = (Actor) this.actors.elementAt(i2);
                    if (actor.catagory == 0 && actor.ID == s) {
                        message.reader().read(((Char) actor).buffType, 0, ((Char) actor).buffType.length);
                        for (int i3 = 0; i3 < ((Char) actor).buffType.length; i3++) {
                            ((Char) actor).countDown[i3] = message.reader().readShort();
                        }
                        ((Char) actor).setBuff(((Char) actor).buffType, ((Char) actor).countDown);
                        message.reader().read(((Char) actor).lvBuff, 0, ((Char) actor).lvBuff.length);
                        return;
                    }
                }
                return;
            }
            byte b2 = message.reader().readByte();
            short s2 = message.reader().readShort();
            if (b != 1) {
                if (b == 0) {
                    int size2 = this.actors.size();
                    while (i < size2) {
                        Actor actor2 = (Actor) this.actors.elementAt(i);
                        if (actor2.catagory == 0 && actor2.ID == s) {
                            ((Char) actor2).removeBuff(b2);
                            return;
                        }
                        i++;
                    }
                    return;
                }
                return;
            }
            byte b3 = message.reader().readByte();
            int i4 = 0;
            while (i4 < Char.BUFFTYPE.length && Char.BUFFTYPE[i4] != b2) {
                i4++;
            }
            if (s == this.mainChar.ID) {
                if (this.mainChar.isExistBuff(b2)) {
                    return;
                }
                CharBuff charBuff = new CharBuff(this.mainChar.x, this.mainChar.y, b2);
                charBuff.setTimeLive(s2);
                timeBuff = charBuff.tick;
                this.isBuffAuto = true;
                this.mainChar.lvBuff[i4] = b3;
                this.mainChar.buffType[i4] = b2;
                this.mainChar.buff.addElement(charBuff);
                return;
            }
            int size3 = this.actors.size();
            while (i < size3) {
                Actor actor3 = (Actor) this.actors.elementAt(i);
                if (actor3.catagory == 0 && actor3.ID == s) {
                    if (((Char) actor3).isExistBuff(b2)) {
                        return;
                    }
                    CharBuff charBuff2 = new CharBuff(actor3.x, actor3.y, b2);
                    charBuff2.setTimeLive(s2);
                    ((Char) actor3).lvBuff[i4] = b3;
                    ((Char) actor3).buffType[i4] = b2;
                    ((Char) actor3).buff.addElement(charBuff2);
                    return;
                }
                i++;
            }
        } catch (Exception unused) {
            System.out.println("LOI KHI NHAN MSG USER BUFF TRONG ONCHARUSEBUFF ");
        }
    }

    public void onInfoGiftQuest(String str) {
        addChat(new ChatInfo("", str, 0));
    }

    public void receiveQuest(Message message) {
        GCanvas.endDlg();
    }

    public void onNewHPMP(Message message) {
        try {
            short s = message.reader().readShort();
            int i = message.reader().readInt();
            int i2 = message.reader().readInt();
            short s2 = message.reader().readShort();
            int i3 = message.reader().readInt();
            if (s == this.mainChar.ID) {
                this.mainChar.maxhp = i;
                this.mainChar.maxmp = i2;
                this.mainChar.defend = s2;
                this.mainChar.hp = i3;
                this.mainChar.realHP = i3;
                if (this.mainChar.state != 3 || this.mainChar.hp <= 0) {
                    return;
                }
                this.mainChar.state = (byte) 0;
                addChat(new ChatInfo("", "Đã được hồi sinh.", 0));
                return;
            }
            int size = this.actors.size();
            for (int i4 = 0; i4 < size; i4++) {
                Actor actor = (Actor) this.actors.elementAt(i4);
                if (actor.catagory == 0 && actor.ID == s) {
                    ((Char) actor).hp = i3;
                    ((Char) actor).realHP = i3;
                    ((Char) actor).maxhp = i;
                    ((Char) actor).maxmp = i2;
                    ((Char) actor).defend = s2;
                    if (((Char) actor).state != 3 || this.mainChar.hp <= 0) {
                        return;
                    }
                    ((Char) actor).state = (byte) 0;
                    addChat(new ChatInfo("", ((Char) actor).name + " đã được hồi sinh.", 0));
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }

    public void onKiller(int i, int i2, short s) {
        if (this.mainChar.ID == i) {
            this.mainChar.iskiller = i2 == 1;
            this.mainChar.killer = s;
            return;
        }
        int size = this.actors.size();
        for (int i3 = 0; i3 < size; i3++) {
            Actor actor = (Actor) this.actors.elementAt(i3);
            if (actor.catagory == 0 && actor.ID == i) {
                ((Char) actor).iskiller = i2 == 1;
                actor.killer = s;
                return;
            }
        }
    }

    public void onRequestTrade(final short s) {
        Char r0 = (Char) findCharByID(s);
        if (r0 != null) {
            GCanvas.startYesNoDlg(r0.name + " đề nghị giao dịch với bạn.", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendOk_notOkInviteTrafe(s, 1);
                    GCanvas.currentDialog = null;
                }
            }, new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendOk_notOkInviteTrafe(s, -1);
                    GCanvas.currentDialog = null;
                }
            });
        }
    }

    public void onOkTrade(short s) {
        String str;
        Char r0 = (Char) findCharByID(s);
        if (s != this.mainChar.ID && r0 != null) {
            str = r0.name + " đồng ý giao dịch";
        } else {
            str = "Bắt đầu giao dịch";
        }
        addChat(new ChatInfo("", str, 0));
        cancelTrade();
        WindowInfoScr.gI().setInfo(0, true, new byte[]{8});
        int i = 0;
        for (int i2 = 0; i2 < MainChar.listPotion.length; i2++) {
            if (MainChar.listPotion[i2].number - MainChar.listPotion[i2].numTrade > 0 && MainChar.listPotion[i2].isTrade) {
                i++;
            }
        }
        WindowInfoScr.gI().setSize(132, 110, Char.inventory.size() + i, 6, 22, 22);
        WindowInfoScr.gI().switchToMe();
    }

    public void onDeniedTrade(short s) {
        String str;
        Char r4 = (Char) findCharByID(s);
        if (r4 == null) {
            str = "";
        } else {
            str = r4.name;
        }
        addChat(new ChatInfo("", str + " không đồng ý giao dịch", 0));
        cancelTrade();
    }

    public void onItemTrade(short s, Message message, byte b) {
        try {
            if (b != 0) {
                if (b != 1) {
                    if (b == 2) {
                        int unsignedByte = message.reader().readUnsignedByte();
                        if (s == this.mainChar.ID) {
                            MainChar.listPotion[unsignedByte].numTrade = 0;
                            removePotionTrade(this.mainChar.tItems, unsignedByte);
                            return;
                        } else {
                            removePotionTrade(this.mainChar.rItems, unsignedByte);
                            return;
                        }
                    }
                    return;
                }
                Potion potion = new Potion();
                potion.id = (short) message.reader().readUnsignedByte();
                potion.index = MainChar.listPotion[potion.id].index;
                potion.number = message.reader().readShort();
                if (s == this.mainChar.ID) {
                    MainChar.listPotion[potion.id].numTrade += potion.number;
                    addPotionTrade(this.mainChar.tItems, potion);
                    return;
                }
                addPotionTrade(this.mainChar.rItems, potion);
                return;
            }
            ItemInInventory itemInInventory = new ItemInInventory();
            byte b2 = message.reader().readByte();
            itemInInventory.clazz = b2;
            itemInInventory.charClazz = b2;
            itemInInventory.itemID = message.reader().readShort();
            itemInInventory.itemOldID = itemInInventory.itemID;
            itemInInventory.idTemplate = message.reader().readShort();
            itemInInventory.plusTemplate = message.reader().readByte();
            itemInInventory.level = message.reader().readByte();
            itemInInventory.durable = message.reader().readShort();
            itemInInventory.mDurable = message.reader().readShort();
            byte b3 = message.reader().readByte();
            for (byte b4 = 0; b4 < b3; b4 = (byte) (b4 + 1)) {
                itemInInventory.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
            }
            itemInInventory.colorName = message.reader().readByte();
            itemInInventory.heItem = message.reader().readByte();
            itemInInventory.hangItem = message.reader().readByte();
            itemInInventory.magic_physic = message.reader().readByte();
            itemInInventory.isEnoughData = true;
            if (s == this.mainChar.ID) {
                MainChar mainChar = this.mainChar;
                int iIsExistInvector = mainChar.isExistInvector(mainChar.tItems, itemInInventory);
                if (iIsExistInvector == -1) {
                    this.mainChar.tItems.addElement(itemInInventory);
                    return;
                } else {
                    this.mainChar.tItems.removeElementAt(iIsExistInvector);
                    return;
                }
            }
            if (((Char) findCharByID(s)) != null) {
                MainChar mainChar2 = this.mainChar;
                int iIsExistInvector2 = mainChar2.isExistInvector(mainChar2.rItems, itemInInventory);
                if (iIsExistInvector2 == -1) {
                    this.mainChar.rItems.addElement(itemInInventory);
                } else {
                    this.mainChar.rItems.removeElementAt(iIsExistInvector2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPotionTrade(Vector vector, Potion potion) {
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            if (vector.elementAt(i) instanceof Potion) {
                Potion potion2 = (Potion) vector.elementAt(i);
                if (potion2.index == MainChar.listPotion[potion.id].index) {
                    potion2.number += potion.number;
                    return;
                }
            }
        }
        vector.addElement(potion);
    }

    private void removePotionTrade(Vector vector, int i) {
        int size = vector.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (vector.elementAt(i2) instanceof Potion) {
                Potion potion = (Potion) vector.elementAt(i2);
                if (potion.index == MainChar.listPotion[i].index) {
                    vector.removeElement(potion);
                    return;
                }
            }
        }
    }

    public void onConfirmTrade() {
        if (WindowInfoScr.index[WindowInfoScr.focusTab] == 8) {
            WindowInfoScr.gI().left = new Command("Chuyển", new IAction() {
                @Override
                public void perform() {
                    GCanvas.startYesNoDlg("Bạn thật sự muốn trao đổi", new IAction() {
                        @Override
                        public void perform() {
                            GameService.gI().sendConfirmTrade();
                            GCanvas.gameScr.mainChar.isTrade = false;
                            GCanvas.endDlg();
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
                            GCanvas.endDlg();
                        }
                    });
                }
            });
        }
    }

    public void onFinishTrade() {
        cancelTrade();
        GCanvas.startOKDlg("Giao dịch hoàn thành", false);
    }

    public void oncancelTrade() {
        cancelTrade();
        GCanvas.startOKDlg("Giao dịch bị hủy.");
    }

    public void creatWeaponFire(LiveActor liveActor, LiveActor liveActor2, int i) {
        WeaponsFire weaponsFire = new WeaponsFire(liveActor, liveActor2, i);
        if (liveActor.dir != 1) {
            arrowsUp.addElement(weaponsFire);
        } else {
            this.arrowsDown.addElement(weaponsFire);
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
        if (GCanvas.currentScreen == WindowInfoScr.gI() && WindowInfoScr.index[WindowInfoScr.focusTab] == 8) {
            switchToMe();
        }
    }

    public static Vector setListGemInbue(Vector vector) {
        Vector vector2 = new Vector();
        for (int i = 0; i < vector.size(); i++) {
            GemTemplate gemTemplate = (GemTemplate) vector.elementAt(i);
            if (Res.getGemByID(gemTemplate.id).type == 0) {
                GemTemplate gemTemplate2 = new GemTemplate(gemTemplate.id);
                GemTemplate.copyData(gemTemplate, gemTemplate2);
                vector2.addElement(gemTemplate2);
            }
        }
        return vector2;
    }

    public void onImbue(short s, byte b) {
        WindowInfoScr.gI().prizeImbue = s;
        Vector vector = new Vector();
        WindowInfoScr.khoaImbue = b;
        if (b == 0) {
            vector = setListGemInbue(MainChar.gemItem);
        } else if (b == 1) {
            vector = setListGemInbue(MainChar.gemItemKhoa);
        } else {
            int size = MainChar.gemItem.size();
            int size2 = MainChar.gemItemKhoa.size();
            for (int i = 0; i < size; i++) {
                GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
                if (Res.getGemByID(gemTemplate.id).type == 0) {
                    GemTemplate gemTemplate2 = new GemTemplate(gemTemplate.id);
                    GemTemplate.copyData(gemTemplate, gemTemplate2);
                    vector.addElement(gemTemplate2);
                }
            }
            for (int i2 = 0; i2 < size2; i2++) {
                GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean z = false;
                for (int i3 = 0; i3 < vector.size(); i3++) {
                    GemTemplate gemTemplate4 = (GemTemplate) vector.elementAt(i3);
                    if (gemTemplate4.id == gemTemplate3.id) {
                        gemTemplate4.number += gemTemplate3.number;
                        z = true;
                    }
                }
                if (!z && Res.getGemByID(gemTemplate3.id).type == 0) {
                    GemTemplate gemTemplate5 = new GemTemplate(gemTemplate3.id);
                    GemTemplate.copyData(gemTemplate3, gemTemplate5);
                    vector.addElement(gemTemplate5);
                }
            }
        }
        for (int i4 = 0; i4 < vector.size(); i4++) {
            WindowInfoScr.gI().listTemp.addElement((GemTemplate) vector.elementAt(i4));
        }
        showWindow(0, false, new byte[]{21});
        WindowInfoScr.gI().name = "Luyện đồ";
    }

    public void onKham(short s, byte b) {
        WindowInfoScr.gI().prizeImbue = s;
        Vector vector = new Vector();
        WindowInfoScr.khoaKham = b;
        if (b == 0) {
            vector = MainChar.gemItem;
        } else if (b == 1) {
            vector = MainChar.gemItemKhoa;
        } else {
            int size = MainChar.gemItem.size();
            int size2 = MainChar.gemItemKhoa.size();
            for (int i = 0; i < size; i++) {
                GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemplate gemTemplate2 = new GemTemplate(gemTemplate.id);
                GemTemplate.copyData(gemTemplate, gemTemplate2);
                vector.addElement(gemTemplate2);
            }
            for (int i2 = 0; i2 < size2; i2++) {
                GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean z = false;
                for (int i3 = 0; i3 < vector.size(); i3++) {
                    GemTemplate gemTemplate4 = (GemTemplate) vector.elementAt(i3);
                    if (gemTemplate4.id == gemTemplate3.id) {
                        gemTemplate4.number += gemTemplate3.number;
                        z = true;
                    }
                }
                if (!z) {
                    GemTemplate gemTemplate5 = new GemTemplate(gemTemplate3.id);
                    GemTemplate.copyData(gemTemplate3, gemTemplate5);
                    vector.addElement(gemTemplate5);
                }
            }
        }
        for (int i4 = 0; i4 < vector.size(); i4++) {
            WindowInfoScr.gI().listTemp.addElement((GemTemplate) vector.elementAt(i4));
        }
        showWindow(0, false, new byte[]{25});
    }

    public void onEpDothu(short s, byte b, byte b2, short s2, byte b3, byte b4, byte b5) {
        WindowInfoScr.gI().listTemp.removeAllElements();
        WindowInfoScr.gI().prizeImbue = s;
        WindowInfoScr.idColor = b;
        WindowInfoScr.lvThu = b2;
        WindowInfoScr.gI();
        WindowInfoScr.slNLThu = b3;
        WindowInfoScr.magicAnimal = b4;
        Vector vector = new Vector();
        WindowInfoScr.khoaDoThu = b5;
        if (b5 == 0) {
            vector = MainChar.gemItem;
        } else if (b5 == 1) {
            vector = MainChar.gemItemKhoa;
        } else {
            int size = MainChar.gemItem.size();
            int size2 = MainChar.gemItemKhoa.size();
            for (int i = 0; i < size; i++) {
                GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
                GemTemplate gemTemplate2 = new GemTemplate(gemTemplate.id);
                GemTemplate.copyData(gemTemplate, gemTemplate2);
                vector.addElement(gemTemplate2);
            }
            for (int i2 = 0; i2 < size2; i2++) {
                GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i2);
                boolean z = false;
                for (int i3 = 0; i3 < vector.size(); i3++) {
                    GemTemplate gemTemplate4 = (GemTemplate) vector.elementAt(i3);
                    if (gemTemplate4.id == gemTemplate3.id) {
                        gemTemplate4.number += gemTemplate3.number;
                        z = true;
                    }
                }
                if (!z) {
                    GemTemplate gemTemplate5 = new GemTemplate(gemTemplate3.id);
                    GemTemplate.copyData(gemTemplate3, gemTemplate5);
                    vector.addElement(gemTemplate5);
                }
            }
        }
        for (int i4 = 0; i4 < vector.size(); i4++) {
            GemTemplate gemTemplate6 = (GemTemplate) vector.elementAt(i4);
            if (gemTemplate6.id >= s2 && gemTemplate6.id <= s2 + 5) {
                WindowInfoScr.gI().listTemp.addElement(gemTemplate6);
            }
        }
        if (WindowInfoScr.gI().listTemp.size() == 0) {
            GCanvas.startOKDlg("Không có nguyên liệu.");
            return;
        }
        showWindow(0, false, new byte[]{32});
        WindowInfoScr.gI().name = "Đồ " + (b4 == 0 ? "ma" : "vật") + " " + ((int) b2);
        GCanvas.endDlg();
    }

    public void onEpNgoc(short s) {
        WindowInfoScr.gI().prizeImbue = s;
        showWindow(0, false, new byte[]{26});
        WindowInfoScr.gI().name = "Hợp thành";
        GCanvas.endDlg();
    }

    public void onSpecialItem(Message message) {
        MainChar.shopItem.removeAllElements();
        try {
            byte b = message.reader().readByte();
            for (int i = 0; i < b; i++) {
                GemTemplate gemTemplate = new GemTemplate();
                gemTemplate.rID = message.reader().readShort();
                gemTemplate.id = message.reader().readByte();
                gemTemplate.number = message.reader().readShort();
                MainChar.shopItem.addElement(gemTemplate);
            }
        } catch (Exception unused) {
            Cout.println("LOI NHAN SPECIAL_ITEM");
        }
        WindowInfoScr.gI().setCurTab();
        MenuWindows.gI().setCurTab();
    }

    public void onGemItem(Message message) throws IOException {
        byte b;
        Vector vector = new Vector();
        vector.removeAllElements();
        short s = message.reader().readShort();
        for (int i = 0; i < s; i++) {
            GemTemplate gemTemplate = new GemTemplate();
            gemTemplate.rID = message.reader().readShort();
            gemTemplate.id = message.reader().readShort();
            gemTemplate.number = message.reader().readShort();
            vector.addElement(gemTemplate);
        }
        try {
            b = message.reader().readByte();
        } catch (Exception unused) {
            b = 0;
        }
        if (b == 0) {
            MainChar.gemItem = vector;
        } else {
            MainChar.gemItemKhoa = vector;
        }
        if (GCanvas.currentScreen == WindowInfoScr.gI()) {
            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 26) {
                WindowInfoScr.gI().setListImbue(true, 1, b);
            }
            if (WindowInfoScr.index[WindowInfoScr.focusTab] == 0) {
                PauseMenu.gI().showMe(0);
            }
        }
        if (GCanvas.currentScreen == MenuWindows.gI() && MenuWindows.focusTabChinh == 2) {
            MenuWindows.gI().setCurTab();
        }
    }

    public void onAddItemImbue(short s) {
        this.mainChar.itemImbue = null;
        int size = Char.inventory.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) Char.inventory.elementAt(i);
            if (Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate).id == s) {
                this.mainChar.itemImbue = itemInInventory;
                return;
            }
        }
    }

    public void onImbueOK(String str) {
        GCanvas.startOKDlg(str, false);
        WindowInfoScr.gI().resetImbue();
    }

    public void onActorDie(Message message) {
        try {
            short s = message.reader().readShort();
            byte b = message.reader().readByte();
            int size = this.actors.size();
            for (int i = 0; i < size; i++) {
                Actor actor = (Actor) this.actors.elementAt(i);
                if (actor.ID == s) {
                    if (b != 1) {
                        if (b == 0) {
                            actor.actorDie();
                            if (actor.ID == this.mainChar.ID && BangAuto.isAutoComeHome && autoFight) {
                                GCanvas.currentDialog = null;
                                doFire(0, 0);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    int i2 = (actor.x - 5) * 2;
                    int i3 = (actor.y - 5) * 2;
                    while (true) {
                        if (i2 <= 10 && i3 <= 10 && i2 >= -10 && i3 >= -10) {
                            ((Monster) actor).startDeadFly(i2, i3);
                            actor.die = true;
                            return;
                        }
                        i2 >>= 1;
                        i3 >>= 1;
                    }
                }
            }
        } catch (Exception unused) {
            Cout.println("LOI TRONG HAM ONACTORDIE TRONG GAMESCR");
        }
    }

    public void onBuffAttack(Message message) {
        byte b;
        try {
            short s = message.reader().readShort();
            byte b2 = message.reader().readByte();
            byte b3 = message.reader().readByte();
            short s2 = message.reader().readShort();
            byte b4 = message.reader().readByte();
            byte b5 = message.reader().readByte();
            try {
                b = message.reader().readByte();
            } catch (Exception e) {
                System.out.println("loi  " + e.toString());
                b = -1;
            }
            int size = this.actors.size();
            for (int i = 0; i < size; i++) {
                Actor actor = (Actor) this.actors.elementAt(i);
                if (actor.ID == s) {
                    if (b5 == -1) {
                        startFlyText("-" + ((int) s2), 1, actor.x, actor.y - 40, 0, -1);
                        if (b2 == 1) {
                            ((Monster) actor).setHp(s2);
                            if (((Monster) actor).hp <= 0) {
                                ((Monster) actor).wantDestroy = true;
                                return;
                            }
                            return;
                        }
                        ((Char) actor).hp -= s2;
                        if (((Char) actor).hp <= 0) {
                            ((Char) actor).state = (byte) 3;
                            return;
                        }
                        return;
                    }
                    if (b5 == 2) {
                        if (b4 != 7 || s2 <= 0) {
                            return;
                        }
                        startFlyText("-" + ((int) s2), 3, actor.x, actor.y - 40, 0, -1);
                        ((Char) actor).mp -= s2;
                        if (((Char) actor).mp <= 0) {
                            ((Char) actor).mp = 0;
                            return;
                        }
                        return;
                    }
                    if (b5 == 3) {
                        if (b4 == 4) {
                            actor.beStune = true;
                            actor.timeBeStune = System.currentTimeMillis() + ((long) (b > 0 ? b * 1000 : 3000));
                            CharBuff charBuff = new CharBuff(actor.x, actor.y, 19);
                            if (b <= 0) {
                                b = 3;
                            }
                            charBuff.setTimeLive(b);
                            actor.addBuff(charBuff);
                            return;
                        }
                        return;
                    }
                    if (b5 == 4 && b4 == 4) {
                        CharBuff charBuff2 = new CharBuff(actor.x, actor.y, 22);
                        if (b <= 0) {
                            b = 36;
                        }
                        charBuff2.setTimeLive(b);
                        actor.addBuff(charBuff2);
                        actor.timeOutPoinson = System.currentTimeMillis();
                        actor.poinson = s2;
                        actor.tDelay = b3;
                        return;
                    }
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }

    public void onListShopOfNPC(final byte[] bArr) {
        GCanvas.currentDialog = null;
        Vector vector = new Vector();
        int i = 0;
        while (i < 3) {
            final int ii = i;
            vector.addElement(new Command("Gian hàng " + (i + 1), new IAction() {
                @Override
                public void perform() {
                    GameScr.this.doActionShop(ii, bArr[bArr.length - 1], bArr[bArr.length - 2]);
                }
            }));
            i++;
        }
        GCanvas.menu.startAt(vector, 3);
    }

    public void doActionShop(int i, int i2, int i3) {
        if (i2 == 0) {
            WindowInfoScr.gI().idSeller = i3;
            WindowInfoScr.gI().shopIdSell = i;
            GCanvas.startWaitDlg();
            this.gameService.requestGetListItem(i3, i, this.mainChar.ID);
            return;
        }
        if (i2 == 1) {
            this.gameService.requestGetListUser(i3, i);
            GCanvas.startWaitDlg();
        }
    }

    public void onFriendList(Message message, int i, String str) throws IOException {
        Vector vector = new Vector();
        short s = message.reader().readShort();
        for (int i2 = 0; i2 < s; i2++) {
            Char r9 = new Char();
            r9.name = message.reader().readUTF();
            r9.currentHead = message.reader().readByte();
            r9.dir = (short) 0;
            r9.level = message.reader().readByte();
            r9.weapon_frame = (short) 0;
            byte b = message.reader().readByte();
            Vector vector2 = new Vector();
            for (int i3 = 0; i3 < b; i3++) {
                ItemInInventory itemInInventory = new ItemInInventory();
                byte b2 = message.reader().readByte();
                itemInInventory.charClazz = b2;
                r9.clazz = b2;
                itemInInventory.idTemplate = message.reader().readShort();
                itemInInventory.level = message.reader().readByte();
                itemInInventory.plusTemplate = message.reader().readByte();
                vector2.addElement(itemInInventory);
            }
            r9.idClan = message.reader().readShort();
            r9.isMaster = message.reader().readByte();
            if (i == 3 || i == 4 || i == 7 || i == 8) {
                r9.charXu = message.reader().readLong();
                r9.luong = message.reader().readInt();
                r9.nationID = message.reader().readByte();
                if (i == 7) {
                    r9.congTrang = message.reader().readInt();
                    r9.lienTram = message.reader().readInt();
                }
            }
            r9.setWearingInfo(vector2);
            vector.addElement(r9);
        }
        if (vector.size() > 0) {
            if (i == 0) {
                ListScr.gI().freindList = vector;
                return;
            }
            if (i == 1 || i == 3 || i == 4 || i == 6 || i == 7 || i == 8) {
                ListScr.gI().setInfo(vector, i, str);
                ListScr.gI().switchToMe();
                GCanvas.endDlg();
            }
        }
    }

    public void onListInfoDepositeItem(Message message) {
        try {
            Vector vector = new Vector();
            byte b = message.reader().readByte();
            final byte b2 = message.reader().readByte();
            final byte b3 = message.reader().readByte();
            if (b == 0) {
                byte b4 = message.reader().readByte();
                for (int i = 0; i < b4; i++) {
                    String utf = message.reader().readUTF();
                    final short s = message.reader().readShort();
                    if (s != this.mainChar.ID) {
                        vector.addElement(new Command(utf, new IAction() {
                            @Override
                            public void perform() {
                                GameScr.this.gameService.requestGetListItem(b2, b3, s);
                                GCanvas.startWaitDlg();
                            }
                        }));
                    }
                }
                GCanvas.currentDialog = null;
                if (vector.size() > 0) {
                    GCanvas.menu.startAt(vector, 3);
                    return;
                }
                return;
            }
            if (b != 1) {
                return;
            }
            GCanvas.currentDialog = null;
            WindowInfoScr.gI().sellItem.removeAllElements();
            byte b5 = message.reader().readByte();
            short s2 = message.reader().readShort();
            for (int i2 = 0; i2 < b5; i2++) {
                ItemInInventory itemInInventory = new ItemInInventory();
                byte b6 = message.reader().readByte();
                itemInInventory.charClazz = b6;
                itemInInventory.clazz = b6;
                itemInInventory.itemID = message.reader().readShort();
                itemInInventory.idTemplate = message.reader().readShort();
                itemInInventory.plusTemplate = message.reader().readByte();
                itemInInventory.prize = message.reader().readInt();
                itemInInventory.level = message.reader().readByte();
                itemInInventory.durable = message.reader().readShort();
                itemInInventory.numKham = message.reader().readByte();
                itemInInventory.totalKham = message.reader().readByte();
                itemInInventory.allAttribute.removeAllElements();
                byte b7 = message.reader().readByte();
                for (byte b8 = 0; b8 < b7; b8 = (byte) (b8 + 1)) {
                    itemInInventory.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                }
                itemInInventory.colorName = message.reader().readByte();
                itemInInventory.heItem = message.reader().readByte();
                itemInInventory.beKicked = message.reader().readByte();
                itemInInventory.hangItem = message.reader().readByte();
                itemInInventory.magic_physic = message.reader().readByte();
                itemInInventory.lock = message.reader().readByte();
                itemInInventory.nameCharSeal = message.reader().readUTF();
                if (this.mainChar.ID == s2) {
                    int size = Char.inventory.size();
                    for (int i3 = 0; i3 < size; i3++) {
                        ItemInInventory itemInInventory2 = (ItemInInventory) Char.inventory.elementAt(i3);
                        if (itemInInventory2.itemID == itemInInventory.itemID) {
                            itemInInventory2.prize = itemInInventory.prize;
                            itemInInventory2.isSelling = true;
                            WindowInfoScr.gI().sellItem.addElement(itemInInventory2);
                        }
                    }
                } else {
                    WindowInfoScr.gI().sellItem.addElement(itemInInventory);
                }
            }
            byte b9 = message.reader().readByte();
            for (int i4 = 0; i4 < b9; i4++) {
                RealID realID = new RealID();
                realID.realID = message.reader().readShort();
                realID.ID = message.reader().readShort();
                realID.price = message.reader().readInt();
                WindowInfoScr.gI().sellItem.addElement(realID);
            }
            WindowInfoScr.gI().idSeller = b2;
            WindowInfoScr.gI().shopIdSell = b3;
            if (this.mainChar.ID == s2) {
                showWindow(0, true, new byte[]{23});
            } else if (WindowInfoScr.gI().sellItem.size() > 0) {
                WindowInfoScr.gI().idCharSell = s2;
                showWindow(0, true, new byte[]{24});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWindow(int i, boolean z, byte[] bArr) {
        // mod: cửa sổ chế đồ: auto chế thẳng, không mở cửa sổ
        if (AutoGuard.cuaSoGuard(i, z, bArr)) {
            return;
        }
        WindowInfoScr.gI().setInfo(i, z, bArr);
        WindowInfoScr.gI().switchToMe();
    }

    public void onUserSellItem(Message message) {
        try {
            boolean z = message.reader().readBoolean();
            short s = message.reader().readShort();
            int i = z ? message.reader().readInt() : 0;
            if (message.reader().readByte() == 0) {
                ItemInInventory itemByID = getItemByID(Char.inventory, s);
                if (itemByID != null) {
                    itemByID.isSelling = z;
                    if (z) {
                        itemByID.prize = i;
                        WindowInfoScr.gI().sellItem.addElement(itemByID);
                        GCanvas.startOKDlg("Đã bỏ vào gian hàng.");
                        return;
                    } else {
                        itemByID.prize = 0;
                        WindowInfoScr.gI().sellItem.removeElement(itemByID);
                        GCanvas.startOKDlg("Đã lấy vật phẩm khỏi gian hàng.");
                        return;
                    }
                }
                return;
            }
            short s2 = message.reader().readShort();
            if (z) {
                GemTemplate gemByRealID = GemTemplate.getGemByRealID(s);
                if (gemByRealID != null) {
                    gemByRealID.number--;
                    if (gemByRealID.number <= 0) {
                        MainChar.gemItem.removeElement(gemByRealID);
                    }
                    RealID realID = new RealID(gemByRealID.rID);
                    realID.ID = s2;
                    realID.price = i;
                    WindowInfoScr.gI().sellItem.addElement(realID);
                }
            } else {
                int size = WindowInfoScr.gI().sellItem.size();
                for (int i2 = 0; i2 < size; i2++) {
                    RealID realID2 = (RealID) WindowInfoScr.gI().sellItem.elementAt(i2);
                    if (realID2.realID == s) {
                        WindowInfoScr.gI().sellItem.removeElement(realID2);
                        GemTemplate gemByID = GemTemplate.getGemByID(realID2.ID);
                        if (gemByID != null) {
                            gemByID.number++;
                            break;
                        }
                        GemTemplate gemTemplate = new GemTemplate();
                        gemTemplate.rID = realID2.realID;
                        gemTemplate.id = realID2.ID;
                        gemTemplate.number = 1;
                        MainChar.gemItem.addElement(gemTemplate);
                        break;
                    }
                }
            }
            GCanvas.endDlg();
        } catch (Exception e) {
            e.printStackTrace();
            Cout.println("LOI HAM ON_USER_SELL_ITEM");
        }
    }

    public void onBuyItemOfUser(short s, short s2, byte b) {
        if (this.mainChar.ID == s2) {
            GCanvas.startOKDlg("Món đồ đã ở trong hành trang.");
        }
        int size = WindowInfoScr.gI().sellItem.size();
        for (int i = 0; i < size; i++) {
            if (b == 0 && (WindowInfoScr.gI().sellItem.elementAt(i) instanceof ItemInInventory)) {
                if (((ItemInInventory) WindowInfoScr.gI().sellItem.elementAt(i)).itemID == s) {
                    WindowInfoScr.gI().sellItem.removeElementAt(i);
                    if (WindowInfoScr.gI() == GCanvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 24) {
                        WindowInfoScr.gI().setInfo(WindowInfoScr.gI().getListBuyItem());
                        return;
                    }
                    return;
                }
            } else if ((WindowInfoScr.gI().sellItem.elementAt(i) instanceof RealID) && ((RealID) WindowInfoScr.gI().sellItem.elementAt(i)).realID == s) {
                WindowInfoScr.gI().sellItem.removeElementAt(i);
                if (WindowInfoScr.gI() == GCanvas.currentScreen && WindowInfoScr.index[WindowInfoScr.focusTab] == 24) {
                    WindowInfoScr.gI().setInfo(WindowInfoScr.gI().getListBuyItem());
                    return;
                }
                return;
            }
        }
    }

    public void onInfoFriend(Message message) {
        try {
            byte b = message.reader().readByte();
            String utf = message.reader().readUTF();
            final short s = message.reader().readShort();
            if (b != 0) {
                return;
            }
            GCanvas.startYesNoDlg(utf + " muốn kết bạn với bạn", new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendInfoAddFriend(s, 1);
                    GCanvas.currentDialog = null;
                }
            }, new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.sendInfoAddFriend(s, -1);
                    GCanvas.currentDialog = null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSMSStruct(Message message) throws IOException {
        byte b = message.reader().readByte();
        for (int i = 0; i < b; i++) {
            this.decriptNap.addElement(message.reader().readUTF());
            this.syntaxNap.addElement(message.reader().readUTF());
            this.centerNap.addElement(message.reader().readUTF());
        }
    }

    public static void loadConfig(int i) {
        int i2 = 0;
        if (i == 0) {
            paintCay = 0;
            paintChar = 1;
            if (Tilemap.trees != null) {
                int size = Tilemap.trees.size();
                while (i2 < size) {
                    ((Tree) Tilemap.trees.elementAt(i2)).removeImage();
                    i2++;
                }
                return;
            }
            return;
        }
        if (i == 1 || i == 2) {
            paintCay = 1;
            paintChar = 1;
        } else {
            if (i != 3) {
                return;
            }
            paintCay = 0;
            paintChar = 0;
            if (Tilemap.trees != null) {
                int size2 = Tilemap.trees.size();
                while (i2 < size2) {
                    ((Tree) Tilemap.trees.elementAt(i2)).removeImage();
                    i2++;
                }
            }
        }
    }

    public void showConfig() {
        Vector vector = new Vector();
        String[] strArr = {"Thấp", "Vừa", "Cao", "Rất thấp"};
        for (int i0 = 0; i0 < 4; i0++) {
            final int i = i0;
            vector.addElement(new Command(strArr[i], new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.setConfig(i);
                }
            }));
        }
        GCanvas.menu.startAt(vector, 3);
    }

    public void onRegClan(final short[] sArr) {
        Vector vector = new Vector();
        for (int i0 = 0; i0 < sArr.length; i0++) {
            final int i = i0;
            final int i2 = i;
            vector.addElement(new Command("", new IAction() {
                @Override
                public void perform() {
                    GCanvas.inputDlg.setInfo("Tên đội của bạn", new IAction() {
                        @Override
                        public void perform() {
                            if (GCanvas.inputDlg.tfInput.getText().equals("")) {
                                return;
                            }
                            GCanvas.startWaitDlg();
                            GameScr.this.gameService.chooseIconClan(sArr[i], GCanvas.inputDlg.tfInput.getText());
                            PaintPopup.gI().right.action.perform();
                        }
                    }, 0, 25, true);
                    GCanvas.inputDlg.show();
                }
            }) {
                @Override
                public void paint(Graphics graphics, int i3, int i4) {
                    GameData.paintIcon(graphics, (short) (sArr[i2] + 1000), i3, i4);
                }
            });
        }
        PaintPopup.gI().setPos(20, 20, GCanvas.w - 40, (GCanvas.h - 40) - Screen.deltaY, (GCanvas.w - 40) / 20);
        PaintPopup.gI().setInfo(vector, "ICON");
        PaintPopup.gI().switchToMe();
    }

    public void showClanInfo(ClanInfo clanInfo) {
        ViewInfoScr.gI().setInfo(clanInfo);
        ViewInfoScr.gI().switchToMe();
        GCanvas.endDlg();
    }

    public void onViewCharInfo(Char r5) {
        WindowInfoScr.gI().setInfo(0, false, new byte[]{1});
        WindowInfoScr.charWearing = r5;
        WindowInfoScr.gI().switchToMe();
        WindowInfoScr.gI().name = "Trang bị";
        GCanvas.endDlg();
    }

    public void setNPC_server_limit(String str, short s, short s2, short s3, short s4, short s5, short s6, byte b, byte b2) {
        if (b2 != 1) {
            return;
        }
        int size = this.npc_limit.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.npc_limit.elementAt(i);
            if (actor != null && actor.catagory == 2) {
                NPC npc = (NPC) actor;
                if (npc.npcType == 1) {
                    Npc_Server npc_Server = (Npc_Server) npc;
                    if (npc_Server.ID == s) {
                        npc_Server.name = str;
                        npc_Server.ID = s;
                        npc_Server.type = s;
                        npc_Server.idImg = s2;
                        npc_Server.x = s3;
                        npc_Server.y = s4;
                        npc_Server.width = s5;
                        npc_Server.height = s6;
                        npc_Server.nFrame = b;
                        npc.typeLimit = b2;
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
        Npc_Server npc_Server2 = new Npc_Server();
        npc_Server2.name = str;
        npc_Server2.ID = s;
        npc_Server2.type = s;
        npc_Server2.idImg = s2;
        npc_Server2.x = s3;
        npc_Server2.y = s4;
        npc_Server2.width = s5;
        npc_Server2.height = s6;
        npc_Server2.nFrame = b;
        npc_Server2.typeLimit = b2;
        this.npc_limit.addElement(npc_Server2);
    }

    public void setNPC_server(String str, short s, short s2, short s3, short s4, short s5, short s6, byte b, byte b2) {
        setNPC_server_limit(str, s, s2, s3, s4, s5, s6, b, b2);
        int size = this.actors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = (Actor) this.actors.elementAt(i);
            if (actor != null && actor.catagory == 2) {
                NPC npc = (NPC) actor;
                if (npc.npcType == 1) {
                    Npc_Server npc_Server = (Npc_Server) npc;
                    if (npc_Server.ID == s) {
                        npc_Server.name = str;
                        npc_Server.ID = s;
                        npc_Server.type = s;
                        npc_Server.idImg = s2;
                        npc_Server.x = s3;
                        npc_Server.y = s4;
                        npc_Server.width = s5;
                        npc_Server.height = s6;
                        npc_Server.nFrame = b;
                        npc.typeLimit = b2;
                        return;
                    }
                } else {
                    continue;
                }
            }
        }
        Npc_Server npc_Server2 = new Npc_Server();
        npc_Server2.name = str;
        npc_Server2.ID = s;
        npc_Server2.type = s;
        npc_Server2.idImg = s2;
        npc_Server2.x = s3;
        npc_Server2.y = s4;
        npc_Server2.width = s5;
        npc_Server2.height = s6;
        npc_Server2.nFrame = b;
        npc_Server2.typeLimit = b2;
        this.actors.addElement(npc_Server2);
    }

    public void setMyFarm(int i, int i2, int i3, int i4, String str, String str2, int i5, int i6, boolean z, byte b, byte b2) {
        int size = this.actors.size();
        for (int i7 = 0; i7 < size; i7++) {
            Actor actor = (Actor) this.actors.elementAt(i7);
            if (actor.catagory == 10) {
                Ground ground = (Ground) actor;
                if (ground.ID == i) {
                    ground.setInfo(i, i2, i3, i4, str, str2, i5, i6, z, b, b2);
                    return;
                }
            }
        }
        this.actors.addElement(new Ground(i, i2, i3, i4, str, str2, i5, i6, z, b, b2));
    }

    public static Actor CreateActor(byte catagory, short type, short id, short x, short y, byte isNewMonster, byte idThanThu) {
        Actor a;
        if (catagory == 0) {
            if (idThanThu > -1) {
                ThanThu thanThu = new ThanThu();
                thanThu.setIdThanThu(idThanThu);
                a = thanThu;
            } else {
                a = new Char();
            }
        } else if (catagory == 1) {
            if (type == 91 || type == 92) {
                a = new Boss_Snake();
            } else if (type == 93) {
                a = new Boss_Eyes();
            } else if (type == 94) {
                a = new Boss_Xuong();
            } else if (type == 46) {
                a = new Boss_TuongQuan();
            } else if (type == 43) {
                a = new TruTranThanh(0);
            } else if (type == 36) {
                a = new TruTranThanh(1);
            } else if (type == 37) {
                a = new TruTranThanh(2);
            } else if (type == 38) {
                a = new BossTL();
            } else if (type == 39) {
                a = new BossThanLan();
            } else if (type == 82) {
                a = new Boss_Eagle();
            } else if (type == 83) {
                a = new CongThanh();
                congThanh = a;
            } else if (type == 84) {
                a = new CuongThi();
            } else if (type == 113) {
                a = new MonsterNew(113);
            } else if (type == 115) {
                a = new MonsterNew(115);
            } else if (type == 116) {
                a = new MonsterNew(116);
            } else if (type == 117) {
                a = new MonsterNew(117);
            } else if ((type >= 95 && type <= 112) || isNewMonster == 1) {
                a = new MonsterNew(type);
            } else {
                a = new Monster();
            }
        } else if (catagory == 3) {
            a = new DropItem();
        } else if (catagory == 4) {
            a = new DropPotion();
        } else if (catagory == 6 || catagory == 7) {
            a = new DropGemItem();
        } else if (catagory == 14) {
            a = new ItemQuest();
        } else {
            a = null;
        }
        a.setNewMonster(isNewMonster);
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

    public void onCapCha(byte[] bArr) {
        final Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        int width = bitmapDecodeByteArray.getWidth();
        int height = bitmapDecodeByteArray.getHeight() + 60;
        final TField tField = new TField();
        int i = (width + 35) / 2;
        tField.x = (GCanvas.hw - i) + 5;
        int i2 = height / 2;
        tField.y = (((GCanvas.hh - i2) - deltaY) + height) - 26;
        tField.width = width + 25;
        tField.height = 20;
        tField.isFocus = true;
        PageScr.gI().setInfo((GCanvas.hw - i) - 20, (GCanvas.hh - i2) - deltaY, width + 75, height, "NHẬP CHỮ", null);
        PageScr.gI().left = new Command("Ok", new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.doCapCha(tField.getText());
                GCanvas.gameScr.switchToMe();
            }
        });
        PageScr.gI().right = tField.cmdClear;
        PageScr.gI().isBigMap = false;
        PageScr.gI().layer = new Layer() {
            @Override
            public void update() {
                tField.update();
            }

            @Override
            public void paint(Graphics graphics, int i3, int i4) {
                graphics.drawImage(bitmapDecodeByteArray, GCanvas.hw, GCanvas.hh - 30, Graphics.TOP | Graphics.HCENTER);
                tField.paint(graphics);
            }

            @Override
            public void keyPress(int i3) {
                tField.keyPressed(i3);
            }
        };
        PageScr.gI().switchToMe();
    }

    private void updateminiMap() {
        cmtoXMini = this.mainChar.x / 16;
        cmtoYmini = this.mainChar.y / 16;
        int i = wMiniMap;
        if (i >= 60) {
            i = 60;
        }
        if (cmtoXMini > Tilemap.w - (i / 2)) {
            cmtoXMini = Tilemap.w - i;
        } else {
            int i2 = cmtoXMini;
            int i3 = wMiniMap;
            if (i2 < i3 / 2) {
                cmtoXMini = 0;
            } else {
                cmtoXMini = i2 - (i3 / 2);
            }
        }
        int i4 = cmtoYmini;
        int i5 = hMiniMap;
        if (i4 < i5 / 2) {
            cmtoYmini = 0;
        } else {
            cmtoYmini = i4 - (i5 / 2);
        }
        if (cmtoYmini > Tilemap.h - hMiniMap) {
            cmtoYmini = Tilemap.h - hMiniMap;
        }
    }

    private void updatePoint() {
        if (GCanvas.menu.showMenu || GCanvas.currentDialog != null) {
            return;
        }
        boolean z = false;
        if (GCanvas.isPointer(0, 0, GCanvas.w, GCanvas.h)) {
            if (this.isBatMiniMap && GCanvas.isPointer(this.posMiniMap.x, this.posMiniMap.y, wMiniMap, hMiniMap) && !GCanvas.isPointer((GCanvas.w - (imgChat.getWidth() * 2)) - 40, 0, 30, (imgChat.getHeight() * 2) + 10) && GCanvas.isPointerClick) {
                int i = GCanvas.pxLast - GCanvas.px;
                if (Util.abs(GCanvas.pyLast - GCanvas.py) < 10 && Util.abs(i) < 10) {
                    if (this.posCam == null) {
                        this.posCam = new Position(0, 0);
                    }
                    this.posCam.x = (short) ((cmtoXMini + GCanvas.px) - (GCanvas.w - 60));
                    this.posCam.y = (short) ((cmtoYmini + GCanvas.py) - this.posMiniMap.y);
                    this.mainChar.posTransRoad = null;
                    if (!Tilemap.tileTypeAtPixel((this.posCam.x * 16) + 2, (this.posCam.y * 16) + 2, 2) && !checkMoveLimit((this.posCam.x * 16) + 2, (this.posCam.y * 16) + 2)) {
                        MainChar mainChar = this.mainChar;
                        mainChar.xTo = mainChar.x;
                        MainChar mainChar2 = this.mainChar;
                        mainChar2.yTo = mainChar2.y;
                        MainChar mainChar3 = this.mainChar;
                        mainChar3.xBegin = mainChar3.x;
                        MainChar mainChar4 = this.mainChar;
                        mainChar4.yBegin = mainChar4.y;
                        MainChar mainChar5 = this.mainChar;
                        mainChar5.posTransRoad = updateFindRoad(mainChar5.x / 16, this.mainChar.y / 16, this.posCam.x, this.posCam.y);
                        this.mainChar.countRoad = 0;
                        GCanvas.isPointerClick = false;
                        return;
                    }
                }
            }
            if (xacDinhToaDo(GCanvas.px, GCanvas.py, this.xChat + 15, this.yChat + 15, 30, 20)) {
                this.isTouchChat = true;
            } else {
                this.isTouchChat = false;
            }
            if (xacDinhToaDo(GCanvas.px, GCanvas.py, GCanvas.w / 2, GCanvas.h - (imgMenu[0].getHeight() / 2), imgMenu[0].getWidth(), imgMenu[0].getHeight())) {
                this.isTouchMenu = true;
            } else {
                this.isTouchMenu = false;
            }
            if (GCanvas.isPointerDown && this.hideGUI == 0) {
                pointer(GCanvas.px, GCanvas.py);
            } else if (GCanvas.isPointerClick) {
                isNhanPhim = false;
                if (this.hideGUI == 0) {
                    if (GCanvas.isPointer(GCanvas.w - 82, (GCanvas.h - (GCanvas.hh / 2)) - 47, 40, 40)) {
                        GCanvas.keyPressed[7] = true;
                        boolean[] zArr = GCanvas.keyHold;
                        GCanvas.keyHold[3] = false;
                        zArr[1] = false;
                        boolean[] zArr2 = GCanvas.keyHold;
                        GCanvas.keyHold[5] = false;
                        zArr2[9] = false;
                        boolean[] zArr3 = GCanvas.keyHold;
                        GCanvas.keyHold[8] = false;
                        zArr3[2] = false;
                        boolean[] zArr4 = GCanvas.keyHold;
                        GCanvas.keyHold[6] = false;
                        zArr4[4] = false;
                    }
                    if (GCanvas.isPointer(GCanvas.w - 45, (GCanvas.h - (GCanvas.hh / 2)) - 55, 40, 40)) {
                        GCanvas.keyPressed[9] = true;
                        boolean[] zArr5 = GCanvas.keyHold;
                        GCanvas.keyHold[3] = false;
                        zArr5[2] = false;
                        boolean[] zArr6 = GCanvas.keyHold;
                        GCanvas.keyHold[6] = false;
                        zArr6[4] = false;
                        boolean[] zArr7 = GCanvas.keyHold;
                        GCanvas.keyHold[5] = false;
                        zArr7[1] = false;
                        boolean[] zArr8 = GCanvas.keyHold;
                        GCanvas.keyHold[8] = false;
                        zArr8[7] = false;
                    }
                    if (GCanvas.isPointer(GCanvas.w - 55, (GCanvas.h - (GCanvas.hh / 2)) - 11, 50, 70)) {
                        this.btnFocus = 5;
                        GCanvas.keyPressed[5] = true;
                        boolean[] zArr9 = GCanvas.keyHold;
                        GCanvas.keyHold[3] = false;
                        zArr9[2] = false;
                        boolean[] zArr10 = GCanvas.keyHold;
                        GCanvas.keyHold[6] = false;
                        zArr10[4] = false;
                        boolean[] zArr11 = GCanvas.keyHold;
                        GCanvas.keyHold[8] = false;
                        zArr11[1] = false;
                        boolean[] zArr12 = GCanvas.keyHold;
                        GCanvas.keyHold[9] = false;
                        zArr12[7] = false;
                    } else if (GCanvas.isPointer(GCanvas.w - 110, (GCanvas.h - (GCanvas.hh / 2)) + 20, 40, 40)) {
                        this.btnFocus = 1;
                        GCanvas.keyPressed[1] = true;
                        boolean[] zArr13 = GCanvas.keyHold;
                        GCanvas.keyHold[3] = false;
                        zArr13[2] = false;
                        boolean[] zArr14 = GCanvas.keyHold;
                        GCanvas.keyHold[6] = false;
                        zArr14[4] = false;
                        boolean[] zArr15 = GCanvas.keyHold;
                        GCanvas.keyHold[5] = false;
                        zArr15[8] = false;
                        boolean[] zArr16 = GCanvas.keyHold;
                        GCanvas.keyHold[9] = false;
                        zArr16[7] = false;
                    } else if (GCanvas.isPointer(GCanvas.w - 104, (GCanvas.h - (GCanvas.hh / 2)) - 17, 40, 40)) {
                        this.btnFocus = 3;
                        GCanvas.keyPressed[3] = true;
                        boolean[] zArr17 = GCanvas.keyHold;
                        GCanvas.keyHold[8] = false;
                        zArr17[2] = false;
                        boolean[] zArr18 = GCanvas.keyHold;
                        GCanvas.keyHold[6] = false;
                        zArr18[4] = false;
                        boolean[] zArr19 = GCanvas.keyHold;
                        GCanvas.keyHold[5] = false;
                        zArr19[1] = false;
                        boolean[] zArr20 = GCanvas.keyHold;
                        GCanvas.keyHold[9] = false;
                        zArr20[7] = false;
                    }
                    if (xacDinhToaDo(GCanvas.px, GCanvas.py, (GCanvas.w - imgSwapQuickSlot.getWidth()) + 5, GCanvas.hh - 15, 40, 40)) {
                        if (indexTabSlot == 0) {
                            indexTabSlot = 1;
                        } else {
                            indexTabSlot = 0;
                        }
                        xSlot = Res.wKhung;
                    }
                    if (xacDinhToaDo(GCanvas.px, GCanvas.py, (GCanvas.w - imgSwapQuickSlot.getWidth()) + 5, GCanvas.hh - 50, 40, 50)) {
                        changeToNextFocusActor(false);
                    }
                }
                if (GCanvas.isPointer(0, 0, 50, 50) && this.mainChar.hp > 0) {
                    MenuWindows.gI().switchToMe();
                }
                if (MessageScr.nMSG > 0 && GCanvas.isPointer(0, imgInfo.getHeight() + 3, 30, 30)) {
                    GCanvas.gameScr.switchToMe();
                    MessageScr.gI().switchToMe();
                }
                GCanvas.clearKeyHold();
                this.btnFocus = 0;
            }
            if (GCanvas.isPointerClick && this.hideGUI == 0 && !GCanvas.isPointer(0, (GCanvas.h - (imgBtn2.getHeight() / 2)) - 20, imgBtn2.getWidth() + 30, imgBtn2.getHeight() + 30) && !GCanvas.isPointer((GCanvas.w - imgBtn5.getWidth()) - 70, (GCanvas.h - (GCanvas.hh / 2)) - 60, imgBtn5.getWidth() + 70, GCanvas.h - ((GCanvas.h - (GCanvas.hh / 2)) - 60)) && !GCanvas.isPointer(GCanvas.w - (imgSwapQuickSlot.getWidth() + 10), GCanvas.hh - 80, imgSwapQuickSlot.getWidth() * 2, 80) && !GCanvas.isPointer(GCanvas.w - (imgChat.getWidth() * 3), 0, imgChat.getWidth() * 3, imgChat.getHeight()) && !GCanvas.isPointer(0, 0, 60, 60) && !GCanvas.isPointer(0, 65, 15, 15) && !GCanvas.isPointer((GCanvas.w - (imgSwapQuickSlot.getWidth() * 2)) - 30, GCanvas.hh - 45, 40, 40) && !GCanvas.isPointer((GCanvas.w - (imgChat.getWidth() * 2)) - 40, 0, 30, (imgChat.getHeight() * 2) + 10)) {
                int i2 = cmx + GCanvas.px;
                int i3 = cmy + GCanvas.py;
                Actor actor = this.focusedActor;
                if (actor != null) {
                    if (actor.catagory != 10) {
                        int size = this.actors.size();
                        for (int i4 = 0; i4 < size; i4++) {
                            Actor actor2 = (Actor) this.actors.elementAt(i4);
                            if (Util.abs(actor2.x - i2) < 20 && Util.abs((actor2.y - 20) - i3) < 40) {
                                GCanvas.isPointerClick = false;
                                if (this.focusedActor.ID == actor2.ID) {
                                    if ((actor2 instanceof Char) && (this.mainChar.pk <= 0 || this.map / 100 == 3)) {
                                        if (((Char) this.focusedActor).idBoss != -1) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else {
                                            doParty();
                                            return;
                                        }
                                    }
                                    if (this.focusedActor.catagory != 2 || actor2.catagory != 2) {
                                        doFire(5, indexTabSlot);
                                        return;
                                    }
                                    NPC npc = (NPC) actor2;
                                    if (((NPC) this.focusedActor).type == npc.type) {
                                        if (npc.type != 4) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else if (((NPC) this.focusedActor).idLinhGac == npc.idLinhGac) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else if (actor2.ID != this.mainChar.ID) {
                                            this.focusedActor = actor2;
                                            doFire(5, indexTabSlot);
                                            return;
                                        }
                                    }
                                }
                                if (actor2.ID != this.mainChar.ID) {
                                    byte b = typeOptionFocus;
                                    if (b == 1) {
                                        if (actor2.catagory == 0 && (!actor2.iskiller || actor2.isNPC())) {
                                            z = true;
                                        }
                                        if (actor2.catagory != 1 && actor2.catagory == 0 && !actor2.iskiller) {
                                            z = true;
                                        }
                                    } else if (b == 2) {
                                        if (!actor2.isNPC()) {
                                            z = true;
                                        }
                                    } else if (b == 3) {
                                        if ((actor2.catagory == 0 && actor2.idClan == this.mainChar.idClan) || actor2.isNPC()) {
                                            z = true;
                                        }
                                    } else if (b == 4) {
                                        if ((actor2.catagory == 0 && actor2.nationID == this.mainChar.nationID) || actor2.isNPC()) {
                                            z = true;
                                        }
                                    }
                                    if (!z) {
                                        this.focusedActor = actor2;
                                    }
                                    if (this.map == 29) {
                                        findRoad(i2, i3);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                        this.focusedActor = null;
                    } else {
                        if (this.mainChar.x >= this.xBeginFrame && this.mainChar.x <= this.xTheendFrame + 32 && this.mainChar.y >= this.yBeginFrame && this.mainChar.y <= this.yTheendFrame + 32) {
                            int size2 = this.actors.size();
                            for (int i5 = 0; i5 < size2; i5++) {
                                Actor actor3 = (Actor) this.actors.elementAt(i5);
                                if (i2 >= actor3.x + 2 && i2 <= actor3.x + 30 && i3 >= actor3.y && i3 <= actor3.y + 30) {
                                    GCanvas.isPointerClick = false;
                                    if (this.focusedActor.ID != -1 && this.focusedActor.ID == actor3.ID) {
                                        if (this.mainChar.state == 0) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        }
                                        return;
                                    } else {
                                        if (actor3.ID != this.mainChar.ID) {
                                            findRoad(i2, i3);
                                            return;
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                        this.focusedActor = null;
                    }
                }
                if (Math.abs(GCanvas.pxLast - GCanvas.px) >= 10 || Math.abs(GCanvas.pyLast - GCanvas.py) >= 10) {
                    return;
                }
                findRoad(i2, i3);
                if (MenuSelectItem.isClose) {
                    MenuSelectItem.isClose = false;
                    this.mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
                    return;
                }
                return;
            }
            if (this.hideGUI == 2) {
                int i6 = cmx + GCanvas.px;
                int i7 = cmy + GCanvas.py;
                Actor actor4 = this.focusedActor;
                if (actor4 != null) {
                    if (actor4.catagory != 10) {
                        int size3 = this.actors.size();
                        for (int i8 = 0; i8 < size3; i8++) {
                            Actor actor5 = (Actor) this.actors.elementAt(i8);
                            if (Util.abs(actor5.x - i6) < 20 && Util.abs((actor5.y - 20) - i7) < 40) {
                                GCanvas.isPointerClick = false;
                                if (this.focusedActor.ID == actor5.ID) {
                                    if ((actor5 instanceof Char) && (this.mainChar.pk <= 0 || this.map / 100 == 3)) {
                                        if (((Char) this.focusedActor).idBoss != -1) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else {
                                            doParty();
                                            return;
                                        }
                                    }
                                    if (this.focusedActor.catagory != 2 || actor5.catagory != 2) {
                                        doFire(5, indexTabSlot);
                                        return;
                                    }
                                    NPC npc2 = (NPC) actor5;
                                    if (((NPC) this.focusedActor).type == npc2.type) {
                                        if (npc2.type != 4) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else if (((NPC) this.focusedActor).idLinhGac == npc2.idLinhGac) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        } else if (actor5.ID != this.mainChar.ID) {
                                            this.focusedActor = actor5;
                                            doFire(5, indexTabSlot);
                                            return;
                                        }
                                    }
                                }
                                if (actor5.ID != this.mainChar.ID) {
                                    this.focusedActor = actor5;
                                    if (this.map == 29) {
                                        findRoad(i6, i7);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                        this.focusedActor = null;
                    } else {
                        if (this.mainChar.x >= this.xBeginFrame && this.mainChar.x <= this.xTheendFrame + 32 && this.mainChar.y >= this.yBeginFrame && this.mainChar.y <= this.yTheendFrame + 32) {
                            int size4 = this.actors.size();
                            for (int i9 = 0; i9 < size4; i9++) {
                                Actor actor6 = (Actor) this.actors.elementAt(i9);
                                if (i6 >= actor6.x + 2 && i6 <= actor6.x + 30 && i7 >= actor6.y && i7 <= actor6.y + 30) {
                                    GCanvas.isPointerClick = false;
                                    if (this.focusedActor.ID != -1 && this.focusedActor.ID == actor6.ID) {
                                        if (this.mainChar.state == 0) {
                                            doFire(5, indexTabSlot);
                                            return;
                                        }
                                        return;
                                    } else {
                                        if (actor6.ID != this.mainChar.ID) {
                                            findRoad(i6, i7);
                                            return;
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                        this.focusedActor = null;
                    }
                }
                findRoad(i6, i7);
            }
        }
    }

    private void findRoad(int i, int i2) {
        if (GCanvas.isPointer(0, 0, GCanvas.w, GCanvas.h)) {
            try {
                int i3 = i / 16;
                int i4 = i2 / 16;
                if (Tilemap.tileTypeAtPixel(cmx + GCanvas.px, cmy + GCanvas.py, 2) || checkMoveLimit(cmx + GCanvas.px, cmy + GCanvas.py)) {
                    return;
                }
                if (this.posCam == null) {
                    this.posCam = new Position(0, 0);
                }
                this.posCam.x = (short) i3;
                this.posCam.y = (short) i4;
                GCanvas.isPointerClick = false;
                MainChar mainChar = this.mainChar;
                mainChar.xTo = mainChar.x;
                MainChar mainChar2 = this.mainChar;
                mainChar2.yTo = mainChar2.y;
                MainChar mainChar3 = this.mainChar;
                mainChar3.xBegin = mainChar3.x;
                MainChar mainChar4 = this.mainChar;
                mainChar4.yBegin = mainChar4.y;
                MainChar mainChar5 = this.mainChar;
                mainChar5.posTransRoad = updateFindRoad(mainChar5.x / 16, this.mainChar.y / 16, i3, i4);
                this.mainChar.countRoad = 0;
            } catch (Exception unused) {
            }
        }
    }

    public void findRoad2(int i, int i2) {
        int i3 = i / 16;
        int i4 = i2 / 16;
        if (this.posCam == null) {
            this.posCam = new Position(0, 0);
        }
        this.posCam.x = (short) i3;
        this.posCam.y = (short) i4;
        MainChar mainChar = this.mainChar;
        mainChar.xTo = mainChar.x;
        MainChar mainChar2 = this.mainChar;
        mainChar2.yTo = mainChar2.y;
        MainChar mainChar3 = this.mainChar;
        mainChar3.posTransRoad = updateFindRoad(mainChar3.x / 16, this.mainChar.y / 16, i3, i4);
        this.mainChar.countRoad = 0;
        timeRemovePos = 100;
    }

    private int setDis(int i, int i2, int i3, int i4) {
        return Util.abs(i - i3) + Util.abs(i2 - i4);
    }

    public short[] updateFindRoad(int xF, int yF, int xTo, int yTo) {
        isTarget = true;
        this.xT = xTo * 16;
        this.yT = yTo * 16;
        if (Util.distance(xF * 16, yF * 16, this.xT, this.yT) <= 16) {
            isTarget = false;
            return null;
        }
        if (checkMoveLimit(xTo, yTo)) {
            return null;
        }
        xStart = (byte) cmxMini;
        yStart = (byte) cmyMini;
        int xFrom = xF - xStart;
        int yFrom = yF - yStart;
        int xTo2 = xTo - xStart;
        int yTo2 = yTo - yStart;
        for (int i = 0; i < mapFind.length; i++) {
            for (int j = 0; j < mapFind[i].length; j++) {
                try {
                    int aa = ((yStart + j) * Tilemap.w) + xStart + i;
                    if (aa < Tilemap.type.length - 1) {
                        if (Tilemap.type[aa] == 2) {
                            mapFind[i][j] = -1;
                        } else {
                            mapFind[i][j] = 0;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        short xGoc = (short) xFrom;
        short yGoc = (short) yFrom;
        try {
            mapFind[xFrom][yFrom] = 1;
        } catch (Exception e) {
        }
        int iL = mapFind.length;
        int jL = mapFind[0].length;
        short index = 2;
        int count = 0;
        while (true) {
            count++;
            if (count > 500) {
                if (iTaskAuto == 2) {
                    this.iTaskAutoLast = iTaskAuto;
                    isFixBugAutoQuest = true;
                }
                iTaskAuto = 1;
                return null;
            }
            int xCur = -1;
            int yCur = -1;
            if (xFrom + 1 < iL && mapFind[xFrom + 1][yFrom] == 0) {
                mapFind[xFrom + 1][yFrom] = (byte) index;
                xCur = xFrom + 1;
                yCur = yFrom;
            }
            if (xFrom - 1 >= 0 && mapFind[xFrom - 1][yFrom] == 0) {
                mapFind[xFrom - 1][yFrom] = (byte) index;
                if (xCur == -1 || setDis(xCur, yCur, xTo2, yTo2) > setDis(xFrom - 1, yFrom, xTo2, yTo2)) {
                    xCur = xFrom - 1;
                    yCur = yFrom;
                }
            }
            if (yFrom + 1 < jL && mapFind[xFrom][yFrom + 1] == 0) {
                mapFind[xFrom][yFrom + 1] = (byte) index;
                if (xCur == -1 || setDis(xCur, yCur, xTo2, yTo2) > setDis(xFrom, yFrom + 1, xTo2, yTo2)) {
                    xCur = xFrom;
                    yCur = yFrom + 1;
                }
            }
            if (yFrom - 1 >= 0 && mapFind[xFrom][yFrom - 1] == 0) {
                mapFind[xFrom][yFrom - 1] = (byte) index;
                if (xCur == -1 || setDis(xCur, yCur, xTo2, yTo2) > setDis(xFrom, yFrom - 1, xTo2, yTo2)) {
                    xCur = xFrom;
                    yCur = yFrom - 1;
                }
            }
            int aa2 = -1;
            if (xCur != -1) {
                aa2 = 0;
                xFrom = xCur;
                yFrom = yCur;
            } else {
                xFrom = 1000;
                yFrom = 1000;
            }
            for (short i2 = 0; i2 < iL; i2 = (short) (i2 + 1)) {
                for (short j2 = 0; j2 < jL; j2 = (short) (j2 + 1)) {
                    if (mapFind[i2][j2] > 1 && setContinue(i2, j2, mapFind) && mapFind[i2][j2] + setDis(i2, j2, xTo2, yTo2) < index + setDis(xFrom, yFrom, xTo2, yTo2)) {
                        index = mapFind[i2][j2];
                        xFrom = i2;
                        yFrom = j2;
                        aa2 = 0;
                    }
                }
            }
            if (xFrom == xTo2 && yFrom == yTo2) {
                if (index >= 127) {
                    return null;
                }
                short[] success = new short[index];
                int ia = 0;
                while (true) {
                    success[ia] = (short) ((xFrom << 8) + yFrom);
                    if (xFrom + 1 < iL && mapFind[xFrom + 1][yFrom] == mapFind[xFrom][yFrom] - 1) {
                        xFrom = (short) (xFrom + 1);
                    } else if (xFrom - 1 >= 0 && mapFind[xFrom - 1][yFrom] == mapFind[xFrom][yFrom] - 1) {
                        xFrom = (short) (xFrom - 1);
                    } else if (yFrom + 1 < jL && mapFind[xFrom][yFrom + 1] == mapFind[xFrom][yFrom] - 1) {
                        yFrom = (short) (yFrom + 1);
                    } else if (yFrom - 1 >= 0 && mapFind[xFrom][yFrom - 1] == mapFind[xFrom][yFrom] - 1) {
                        yFrom = (short) (yFrom - 1);
                    }
                    if (xFrom == xGoc && yFrom == yGoc) {
                        return success;
                    }
                    ia++;
                }
            }
            if (aa2 != 0) {
                return null;
            }
            index = (short) (index + 1);
        }
    }

    private boolean setContinue(int i, int i2, byte[][] bArr) {
        int i3 = i + 1;
        if (i3 < bArr.length && bArr[i3][i2] == 0) {
            return true;
        }
        int i4 = i - 1;
        if (i4 >= 0 && bArr[i4][i2] == 0) {
            return true;
        }
        int i5 = i2 + 1;
        byte[] bArr2 = bArr[i];
        if (i5 < bArr2.length && bArr2[i5] == 0) {
            return true;
        }
        int i6 = i2 - 1;
        return i6 >= 0 && bArr2[i6] == 0;
    }

    public void onMenuOption(final int i, final byte b, String[] strArr) {
        // mod: menu NPC - auto nuôi pet / vận may đọc danh sách; việc chế đồ ẩn menu
        if (AutoGuard.menuGuard(i, b, strArr) || AutoGuard.autoHideUi()) {
            return;
        }
        if (GCanvas.menu.showMenu) {
            GCanvas.menu.showMenu = false;
        }
        Vector vector = new Vector();
        for (int i20 = 0; i20 < strArr.length; i20++) {
            final int i2 = i20;
            vector.addElement(new Command(strArr[i2], new IAction() {
                @Override
                public void perform() {
                    GameScr.this.gameService.doMenuOption(i, b, i2);
                }
            }));
        }
        GCanvas.menu.startAt(vector, 3);
        GCanvas.currentDialog = null;
    }

    public void onTextBox(final int i, final byte b, String str, int i2) {
        GCanvas.inputDlg.setInfo(str, new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.doTextBox(i, b, GCanvas.inputDlg.tfInput.getText());
                GCanvas.endDlg();
            }
        }, i2, 40, true);
        GCanvas.inputDlg.show();
    }

    public void onContentClanQuest(String str) {
        GCanvas.startYesNoDlg(str, new IAction() {
            @Override
            public void perform() {
                GameScr.this.gameService.questClan(1);
                GCanvas.endDlg();
            }
        });
    }

    public static void onWeather(byte b, int i, int i2) {
        if (b != -1) {
            effAnimate.addElement(new AnimateEffect(b, true, i, i2));
        } else {
            int size = effAnimate.size();
            for (int i3 = 0; i3 < size; i3++) {
                ((AnimateEffect) effAnimate.elementAt(i3)).isStop = true;
            }
        }
        weather = b;
    }

    public void onSetCharKham(short s, byte[] bArr, byte[] bArr2) {
        LiveActor liveActor = (LiveActor) findActorByID(s);
        if (liveActor != null) {
            int length = bArr.length;
            for (int i = 0; i < length; i++) {
                int size = liveActor.effSkill.size();
                int i2 = 0;
                while (true) {
                    if (i2 < size) {
                        if (((Skill_Kham) liveActor.effSkill.elementAt(i2)).type == bArr[i]) {
                            Skill_Kham.delay = bArr2[i];
                            break;
                        }
                        i2++;
                    } else {
                        liveActor.effSkill.addElement(new Skill_Kham(bArr[i], bArr2[i], liveActor));
                        break;
                    }
                }
            }
        }
    }

    public void onDynamicObj(DynamicObj dynamicObj) {
        this.objDynamic.addElement(dynamicObj);
        this.actors.addElement(dynamicObj);
    }

    public void onEffect(EffectServerManager effectServerManager) {
        if (effManager == null) {
            effManager = new Vector();
        }
        effManager.addElement(effectServerManager);
    }

    public void onCheDo(String str, short s, Vector vector, int i, int i2) {
        WindowInfoScr.gI().listTemp.removeAllElements();
        WindowInfoScr.gI().listCheDo = vector;
        WindowInfoScr.gI().listEp = new Vector();
        WindowInfoScr.idTemp = s;
        WindowInfoScr.optionCreateItem = (byte) i;
        int size = vector.size();
        if (size == 0) {
            GCanvas.startOKDlg("Vật phẩm không thể thăng cấp");
            return;
        }
        WindowInfoScr.khoaCheDo = (byte) i2;
        Vector vector2 = new Vector();
        if (i2 == 0) {
            vector2 = MainChar.gemItem;
        } else if (i2 == 1) {
            vector2 = MainChar.gemItemKhoa;
        } else {
            int size2 = MainChar.gemItem.size();
            int size3 = MainChar.gemItemKhoa.size();
            for (int i3 = 0; i3 < size2; i3++) {
                GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i3);
                GemTemplate gemTemplate2 = new GemTemplate(gemTemplate.id);
                GemTemplate.copyData(gemTemplate, gemTemplate2);
                vector2.addElement(gemTemplate2);
            }
            for (int i4 = 0; i4 < size3; i4++) {
                GemTemplate gemTemplate3 = (GemTemplate) MainChar.gemItemKhoa.elementAt(i4);
                boolean z = false;
                for (int i5 = 0; i5 < vector2.size(); i5++) {
                    GemTemplate gemTemplate4 = (GemTemplate) vector2.elementAt(i5);
                    if (gemTemplate4.id == gemTemplate3.id) {
                        gemTemplate4.number += gemTemplate3.number;
                        z = true;
                    }
                }
                if (!z) {
                    GemTemplate gemTemplate5 = new GemTemplate(gemTemplate3.id);
                    GemTemplate.copyData(gemTemplate3, gemTemplate5);
                    vector2.addElement(gemTemplate5);
                }
            }
        }
        for (int i6 = 0; i6 < size; i6++) {
            Mineral mineral = (Mineral) vector.elementAt(i6);
            int size4 = vector2.size();
            for (int i7 = 0; i7 < size4; i7++) {
                GemTemplate gemTemplate6 = (GemTemplate) vector2.elementAt(i7);
                if ((i <= 2 && (i != 2 || (i == 2 && i6 == 0))) || i > 2) {
                    if (gemTemplate6.id >= mineral.idTemplate + (i == 0 ? (short) 0 : (short) 3) && gemTemplate6.id <= mineral.idTemplate + 5) {
                        WindowInfoScr.gI().listTemp.addElement(gemTemplate6);
                    }
                } else if (gemTemplate6.id == mineral.idTemplate + 5) {
                    WindowInfoScr.gI().listTemp.addElement(gemTemplate6);
                }
            }
        }
        if (WindowInfoScr.gI().listTemp.size() == 0) {
            GCanvas.startOKDlg("Không có nguyên liệu.");
        } else {
            showWindow(0, false, new byte[]{28});
            WindowInfoScr.gI().name = str;
        }
    }

    public void onMSGServer(String str) {
        if (this.listMSGServer == null) {
            this.listMSGServer = new Vector();
        }
        this.listMSGServer.addElement(str);
        MessageScr.gI().addTab(str, null, 0);
    }

    public void onDropItem2Ground(Message message) {
        try {
            byte b = message.reader().readByte();
            for (int i = 0; i < b; i++) {
                Dropable dropable = (Dropable) CreateActor(message.reader().readByte(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), message.reader().readShort(), (byte) 0, (byte) -1);
                dropable.startDropFrom(dropable.x, dropable.y, dropable.x, dropable.y);
                this.actors.addElement(dropable);
            }
        } catch (Exception unused) {
        }
    }

    public void onSkillClan(SkillClan[] skillClanArr, SkillClan[] skillClanArr2) {
        this.skillClan = skillClanArr;
        this.skillClanPrivate = skillClanArr2;
    }

    public void onLuckyDraw(Message message) {
        try {
            byte b = message.reader().readByte();
            if (b != 0) {
                if (b == 1) {
                    DialLuckyScr.gI().lastScr = this;
                    DialLuckyScr.gI().switchToMe();
                    int i = message.reader().readByte();
                    DialLuckyScr.arrayIndex = new byte[i];
                    for (byte b2 = 0; b2 < i; b2 = (byte) (b2 + 1)) {
                        DialLuckyScr.arrayIndex[b2] = message.reader().readByte();
                    }
                    DialLuckyScr.gI().onStart();
                    return;
                }
                return;
            }
            ItemQsScreen.gI().resetData();
            ItemQsScreen.allItemLucky.removeAllElements();
            ItemQsScreen.totalItemSelect = message.reader().readByte();
            byte b3 = message.reader().readByte();
            for (byte b4 = 0; b4 < b3; b4 = (byte) (b4 + 1)) {
                ItemLuckyDraw itemLuckyDraw = new ItemLuckyDraw();
                itemLuckyDraw.cat = message.reader().readByte();
                itemLuckyDraw.id = message.reader().readShort();
                itemLuckyDraw.name = message.reader().readUTF();
                itemLuckyDraw.decript = new String[message.reader().readByte()];
                for (int i2 = 0; i2 < itemLuckyDraw.decript.length; i2++) {
                    itemLuckyDraw.decript[i2] = message.reader().readUTF();
                }
                itemLuckyDraw.isSelect = message.reader().readByte();
                ItemQsScreen.allItemLucky.addElement(itemLuckyDraw);
            }
            ItemQsScreen.total = (short) ItemQsScreen.allItemLucky.size();
            if (ItemQsScreen.total < 32) {
                ItemQsScreen.total = (short) 32;
            }
            ItemQsScreen.gI().last = this;
            ItemQsScreen.gI().switchToMe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reSetQuest() {
        mainQuest = null;
        subQuest = null;
        clanQuest = null;
    }

    public static int isNpcQuest(int i) {
        Quest quest = mainQuest;
        if (quest != null) {
            if ((quest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == i) {
                return 1;
            }
            if (mainQuest.stateQuest == 0) {
                return 0;
            }
        }
        Quest quest2 = subQuest;
        if (quest2 != null) {
            if ((quest2.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == i) {
                return 3;
            }
            if (subQuest.stateQuest == 0) {
                return 2;
            }
        }
        Quest quest3 = clanQuest;
        if (quest3 == null) {
            return -1;
        }
        if ((quest3.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == i) {
            return 3;
        }
        return clanQuest.stateQuest == 0 ? 2 : -1;
    }

    public void getAllPosNPCMInimap(Actor actor) {
        Quest quest = mainQuest;
        if (quest != null && ((quest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == actor.getTypeNpc())) {
            setPosNPC(actor.getTypeNpc(), mainQuest.stateQuest != 1 ? 0 : 3, actor.x, actor.y);
            return;
        }
        Quest quest2 = subQuest;
        if (quest2 != null && ((quest2.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == actor.getTypeNpc())) {
            setPosNPC(actor.getTypeNpc(), subQuest.stateQuest != 1 ? 0 : 3, actor.x, actor.y);
            return;
        }
        Quest quest3 = clanQuest;
        if (quest3 != null && ((quest3.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == actor.getTypeNpc())) {
            setPosNPC(actor.getTypeNpc(), clanQuest.stateQuest != 1 ? 0 : 3, actor.x, actor.y);
            return;
        }
        for (int i = 0; i < allQuestCanReceive.size(); i++) {
            Quest quest4 = (Quest) allQuestCanReceive.elementAt(i);
            if (quest4.stateQuest == 0 && quest4.idNpcReceive == actor.getTypeNpc()) {
                setPosNPC(actor.getTypeNpc(), 0, actor.x, actor.y);
            }
        }
    }

    public static Vector getAllQuestNpc(int i) {
        Vector vector = new Vector();
        Quest quest = mainQuest;
        if (quest != null && ((quest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == i)) {
            vector.addElement(new Command("Trả " + mainQuest.name, new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.mainQuest;
                    GameScr.currQuest.startTalk(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, GameScr.mainQuest.stateQuest);
                }
            }));
        }
        Quest quest2 = subQuest;
        if (quest2 != null && ((quest2.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == i)) {
            vector.addElement(new Command("Trả " + subQuest.name, new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.subQuest;
                    GameScr.currQuest.startTalk(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, GameScr.subQuest.stateQuest);
                }
            }));
        }
        Quest quest3 = clanQuest;
        if (quest3 != null && ((quest3.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == i)) {
            vector.addElement(new Command("Trả " + clanQuest.name, new IAction() {
                @Override
                public void perform() {
                    GameScr.currQuest = GameScr.clanQuest;
                    GameScr.currQuest.startTalk(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, GameScr.clanQuest.stateQuest);
                }
            }));
        }
        for (int i2 = 0; i2 < allQuestCanReceive.size(); i2++) {
            final Quest quest4 = (Quest) allQuestCanReceive.elementAt(i2);
            if (quest4.stateQuest == 0 && quest4.idNpcReceive == i) {
                vector.addElement(new Command(quest4.name, new IAction() {
                    @Override
                    public void perform() {
                        GameScr.currQuest = quest4;
                        GameScr.currQuest.startTalk(GCanvas.gameScr.mainChar, GCanvas.gameScr.focusedActor, quest4.stateQuest);
                    }
                }));
            }
        }
        return vector;
    }

    public static Bitmap getImgQuest(int i) {
        Quest quest = mainQuest;
        if (quest != null && ((quest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == i)) {
            return Res.imgSelect2;
        }
        Quest quest2 = subQuest;
        if (quest2 != null && ((quest2.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == i)) {
            return Res.imgSelect2;
        }
        Quest quest3 = clanQuest;
        if (quest3 != null && ((quest3.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == i)) {
            return Res.imgSelect2;
        }
        for (int i2 = 0; i2 < allQuestCanReceive.size(); i2++) {
            Quest quest4 = (Quest) allQuestCanReceive.elementAt(i2);
            if (quest4.stateQuest == 0 && quest4.idNpcReceive == i) {
                if (quest4.type == 3) {
                    return Res.imgSelect2;
                }
                return Res.imgSelect1;
            }
        }
        return Res.imgSelect;
    }

    public void setPosNPC(int i, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < PosNPCQuest.size(); i5++) {
            Position position = (Position) PosNPCQuest.elementAt(i5);
            if (position != null && position.id == i) {
                return;
            }
        }
        Position position2 = new Position();
        position2.id = i;
        position2.indexColor = (short) i2;
        position2.x = i3;
        position2.y = i4;
        PosNPCQuest.addElement(position2);
    }

    public void onQuest(Message message) {
        try {
            byte b = message.reader().readByte();
            byte b2 = message.reader().readByte();
            Cout.println(((int) b) + " NHAN NHIEM VU MOI " + ((int) b2));
            if (b == 0) {
                allQuestCanReceive.removeAllElements();
            }
            for (int i = 0; i < b2; i++) {
                Quest quest = new Quest(message.reader().readShort());
                quest.stateQuest = b;
                quest.main_sub = message.reader().readByte();
                quest.name = message.reader().readUTF();
                if (b == 0) {
                    quest.idNpcReceive = message.reader().readByte();
                    for (String str : Util.split(message.reader().readUTF(), ">")) {
                        quest.content.addElement(str);
                    }
                    quest.type = message.reader().readByte();
                    quest.deltaLv = message.reader().readByte();
                } else if (b == 1) {
                    quest.idNpcResponse = message.reader().readByte();
                    for (String str2 : Util.split(message.reader().readUTF(), ">")) {
                        quest.rescontent.addElement(str2);
                    }
                    quest.decript = message.reader().readUTF();
                    quest.infoGift = message.reader().readUTF();
                } else if (b == 2) {
                    quest.type = message.reader().readByte();
                    quest.decript = message.reader().readUTF();
                    quest.idNpcResponse = message.reader().readByte();
                    quest.deltaLv = message.reader().readByte();
                    quest.infoGift = message.reader().readUTF();
                    if (quest.type == 2) {
                        int i2 = message.reader().readByte();
                        quest.idItemGet = new short[i2];
                        quest.totalItemGet = new short[i2];
                        quest.allItemGet = new short[i2];
                        for (int i3 = 0; i3 < i2; i3++) {
                            short s = message.reader().readShort();
                            short s2 = message.reader().readShort();
                            short s3 = message.reader().readShort();
                            quest.idItemGet[i3] = s;
                            quest.allItemGet[i3] = s2;
                            quest.totalItemGet[i3] = s3;
                        }
                    } else if (quest.type == 0) {
                        int i4 = message.reader().readByte();
                        quest.idMonsKill = new short[i4];
                        quest.totalMonskill = new short[i4];
                        quest.allMonsKilled = new short[i4];
                        for (int i5 = 0; i5 < i4; i5++) {
                            short s4 = message.reader().readShort();
                            short s5 = message.reader().readShort();
                            short s6 = message.reader().readShort();
                            quest.idMonsKill[i5] = s4;
                            quest.allMonsKilled[i5] = s5;
                            quest.totalMonskill[i5] = s6;
                        }
                    } else if (quest.type == 4) {
                        quest.totalMonskill = new short[1];
                        quest.allMonsKilled = new short[1];
                        quest.allMonsKilled[0] = message.reader().readShort();
                        quest.totalMonskill[0] = message.reader().readShort();
                    }
                }
                if (b == 0) {
                    allQuestCanReceive.addElement(quest);
                    if (quest.main_sub == 0 && Char.skillLevelLearnt[0] == 0) {
                        short s7 = quest.idNpcReceive;
                        this.idNpcReceive = s7;
                        set_npc_request(s7);
                    }
                } else if (quest.main_sub == 0) {
                    mainQuest = quest;
                } else if (quest.main_sub == 1) {
                    subQuest = quest;
                } else if (quest.main_sub == 2) {
                    clanQuest = quest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFruit(Message message) {
        try {
            Tree2014.gI().setInfoTree(message);
        } catch (Exception unused) {
        }
    }

    public void onTuBinh(Message message) {
        TuBinhScr.gI().setData(message);
    }

    public static void createNewSkill(LiveActor liveActor, LiveActor liveActor2, int i, byte b, int i2) {
        Arrow arrow = new Arrow(i2);
        arrow.set(0, liveActor.x, liveActor.y, i, b, liveActor, liveActor2);
        if (i2 == 10) {
            arrow.typeEffEnd = (byte) 46;
        } else if (i2 == 11) {
            arrow.typeEffEnd = (byte) 45;
        } else if (i2 == 12) {
            arrow.typeEffEnd = (byte) 30;
        }
        arrowsUp.addElement(arrow);
    }

    public static Effect creatEff(LiveActor liveActor, int i, int i2, int i3) {
        Effect effect2 = new Effect(i, i2, i3);
        if (liveActor != null) {
            effect2.myChar = liveActor;
            if (liveActor.getHorse() != -1) {
                effect2.yadd = (byte) -5;
            }
        }
        if (i3 != 47 && i3 != 57) {
            switch (i3) {
                case 41:
                    effect2.loop = (byte) 2;
                    effect2.yadd = (byte) (effect2.yadd - 6);
                    break;
                case 42:
                    effect2.loop = (byte) 2;
                    effect2.yadd = (byte) (effect2.yadd - 6);
                    break;
                case 43:
                    effect2.loop = (byte) 3;
                    effect2.yadd = (byte) (effect2.yadd - 6);
                    break;
                case 44:
                    effect2.loop = (byte) 1;
                    effect2.y -= 6;
                    effect2.yadd = (byte) (effect2.yadd - 6);
                    break;
            }
        } else {
            effect2.loop = (byte) 1;
            effect2.y -= 6;
            effect2.yadd = (byte) (effect2.yadd - 6);
        }
        return effect2;
    }

    public void onNewEffect(Message message) {
        int[] iArr;
        int[] iArr2;
        byte b;
        Monster monsterFindMonsterByID;
        int[] iArr3 = {41, 42, 43, 56};
        int[] iArr4 = {12, 10, 11, 15};
        int[] iArr5 = {44, 44, 47, 57};
        try {
            LiveActor liveActor = (LiveActor) getActor(message.reader().readShort());
            if (liveActor != null) {
                byte b2 = message.reader().readByte();
                byte b3 = message.reader().readByte();
                byte b4 = message.reader().readByte();
                Vector vector = new Vector();
                Vector vector2 = new Vector();
                Vector vector3 = new Vector();
                int i = 0;
                while (i < b4) {
                    short s = message.reader().readShort();
                    int i2 = message.reader().readInt();
                    int i3 = message.reader().readInt();
                    if (b2 == 0) {
                        Char r14 = (Char) getActor(s);
                        if (r14 != null) {
                            b = b4;
                            vector2.addElement(i2 + "");
                            vector.addElement(r14);
                            vector3.addElement(i3 + "");
                            iArr = iArr4;
                            int i4 = 0;
                            for (int i5 = 3; i4 < i5; i5 = 3) {
                                Char r2 = new Char();
                                r2.x = (short) (r14.x + ((r.nextInt() % 10) * 16));
                                r2.y = (short) (r14.y + ((r.nextInt() % 10) * 16));
                                vector.addElement(r2);
                                vector2.addElement(i2 + "");
                                vector3.addElement(i3 + "");
                                i4++;
                                iArr5 = iArr5;
                            }
                            iArr2 = iArr5;
                        } else {
                            iArr = iArr4;
                            iArr2 = iArr5;
                            b = b4;
                        }
                    } else {
                        iArr = iArr4;
                        iArr2 = iArr5;
                        b = b4;
                        if (b2 == 1 && (monsterFindMonsterByID = findMonsterByID(s)) != null) {
                            vector2.addElement(i2 + "");
                            vector.addElement(monsterFindMonsterByID);
                            vector3.addElement(i3 + "");
                            for (int i6 = 0; i6 < 3; i6++) {
                                Monster monster = new Monster();
                                monster.x = (short) (monsterFindMonsterByID.x + ((r.nextInt() % 10) * 16));
                                monster.y = (short) (monsterFindMonsterByID.y + ((r.nextInt() % 10) * 16));
                                vector.addElement(monster);
                                vector2.addElement(i2 + "");
                                vector3.addElement(i3 + "");
                            }
                        }
                    }
                    i++;
                    b4 = b;
                    iArr4 = iArr;
                    iArr5 = iArr2;
                }
                Effect effectCreatEff = creatEff(liveActor, liveActor.x, liveActor.y, iArr3[b3]);
                effectCreatEff.subEff = new SubEff();
                effectCreatEff.subEff.ac = liveActor;
                effectCreatEff.subEff.target = vector;
                effectCreatEff.subEff.idEff = iArr5[b3];
                effectCreatEff.subEff.power = vector2;
                effectCreatEff.subEff.hp = vector3;
                effectCreatEff.subEff.eff = 0;
                effectCreatEff.subEff.indeximg = iArr4[b3];
                EffectManager.hiEffects.addElement(effectCreatEff);
            }
        } catch (Exception unused) {
        }
    }

    public void onAutoImbue(Message message) {
        try {
            if (message.reader().readByte() == 0) {
                ScreenDapDo.gI().switchToMe(this);
                ScreenDapDo.gI().setSizeCell((byte) 1, false);
                return;
            }
            String utf = message.reader().readUTF();
            byte b = message.reader().readByte();
            if (GCanvas.currentScreen == ScreenDapDo.gI()) {
                ScreenDapDo.gI().infoPoup = utf;
                ScreenDapDo.gI().doFinishInbue();
                GCanvas.endDlg();
            }
            if (b == 1) {
                ScreenDapDo.gI().resetSelect();
                ScreenDapDo.isFinishImbue = true;
            }
        } catch (Exception unused) {
        }
    }

    public TimecountDown findTime(int i) {
        for (int i2 = 0; i2 < this.VecTime.size(); i2++) {
            TimecountDown timecountDown = (TimecountDown) this.VecTime.elementAt(i2);
            if (timecountDown != null && timecountDown.id == i) {
                return timecountDown;
            }
        }
        return null;
    }

    public void onShopNew(Message message) {
        try {
            ShopNew.gI().typeshop = message.reader().readShort();
            short s = message.reader().readShort();
            Vector vector = new Vector();
            for (int i = 0; i < s; i++) {
                ItemInInventory itemInInventory = new ItemInInventory();
                itemInInventory.charClazz = message.reader().readByte();
                itemInInventory.itemID = message.reader().readShort();
                itemInInventory.idTemplate = message.reader().readShort();
                itemInInventory.plusTemplate = message.reader().readByte();
                itemInInventory.level = message.reader().readByte();
                itemInInventory.mDurable = message.reader().readShort();
                itemInInventory.durable = message.reader().readShort();
                itemInInventory.clazz = message.reader().readByte();
                itemInInventory.numKham = message.reader().readByte();
                itemInInventory.totalKham = message.reader().readByte();
                itemInInventory.colorName = message.reader().readByte();
                itemInInventory.heItem = message.reader().readByte();
                itemInInventory.beKicked = message.reader().readByte();
                itemInInventory.hangItem = message.reader().readByte();
                itemInInventory.magic_physic = message.reader().readByte();
                itemInInventory.lock = message.reader().readByte();
                itemInInventory.nameCharSeal = message.reader().readUTF();
                itemInInventory.allAttribute.removeAllElements();
                itemInInventory.lastTime = System.currentTimeMillis();
                itemInInventory.dayUse = message.reader().readUnsignedShort();
                byte b = message.reader().readByte();
                for (byte b2 = 0; b2 < b; b2 = (byte) (b2 + 1)) {
                    itemInInventory.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                }
                itemInInventory.isItemDrop = message.reader().readByte();
                itemInInventory.isEnoughData = true;
                vector.addElement(itemInInventory);
            }
            ShopNew.text = message.reader().readUTF();
            ShopNew.nameshop = message.reader().readUTF();
            ShopNew.gI().ItemShopNew = vector;
            PauseMenu.gI().showShopNew(0);
        } catch (Exception unused) {
        }
    }

    public void onCountDown(Message message) {
        byte b;
        short s;
        try {
            byte b2 = message.reader().readByte();
            short s2 = message.reader().readShort();
            String utf = message.reader().readUTF();
            int i = message.reader().readInt();
            try {
                short s3 = message.reader().readShort();
                b = message.reader().readByte();
                s = s3;
            } catch (Exception unused) {
                b = -1;
                s = -1;
            }
            long jCurrentTimeMillis = System.currentTimeMillis() + ((long) (i * 1000));
            if (b2 != -1) {
                if (i > 0) {
                    Actor actorFindActorByID_Cat = findActorByID_Cat(s2, b2);
                    if (actorFindActorByID_Cat != null) {
                        objcountDown = actorFindActorByID_Cat;
                    }
                    charcountdonw = new CharCountDown(jCurrentTimeMillis, utf);
                }
                if (i == 0) {
                    charcountdonw = null;
                    objcountDown = null;
                }
            }
            if (b2 != -1 || i < 0) {
                return;
            }
            TimecountDown timecountDownFindTime = findTime(s2);
            if (timecountDownFindTime == null) {
                timecountDownFindTime = new TimecountDown(s2, s, i, utf, b);
                this.VecTime.addElement(timecountDownFindTime);
            }
            if (timecountDownFindTime != null) {
                timecountDownFindTime.tile = utf;
                timecountDownFindTime.idIcon = s;
                timecountDownFindTime.setsecond(i);
                if (b == -2) {
                    timecountDownFindTime.wantdestroy = true;
                }
            }
        } catch (Exception unused2) {
        }
    }

    public void onTachNguyenLieu(Message message) {
        try {
            byte b = message.reader().readByte();
            if (b == 0) {
                ScreenDapDo.gI().switchToMe(this);
                ScreenDapDo.gI().setSizeCell((byte) 0, false);
            } else if (b == 4) {
                GCanvas.currentScreen.startXayBot(message.reader().readShort(), message.reader().readByte(), message.reader().readByte());
            }
        } catch (Exception unused) {
        }
    }

    public void onItemShopOnline(Message message) {
        try {
            byte b = message.reader().readByte();
            if (b == -1) {
                GCanvas.shop.switchToMe(GCanvas.currentScreen);
                return;
            }
            byte b2 = 4;
            if (b != 3) {
                b2 = b == 4 ? (byte) 5 : b;
            }
            ((Vector) ShopOnline.ALL_ITEM.elementAt(b2)).removeAllElements();
            byte b3 = message.reader().readByte();
            for (int i = 0; i < b3; i++) {
                ItemInInventory itemInInventory = new ItemInInventory();
                itemInInventory.isEnoughData = true;
                itemInInventory.clazz = message.reader().readByte();
                itemInInventory.idTemplate = message.reader().readShort();
                itemInInventory.plusTemplate = message.reader().readByte();
                itemInInventory.level = message.reader().readByte();
                itemInInventory.mDurable = message.reader().readShort();
                itemInInventory.durable = message.reader().readShort();
                itemInInventory.numKham = message.reader().readByte();
                itemInInventory.totalKham = message.reader().readByte();
                itemInInventory.colorName = message.reader().readByte();
                itemInInventory.heItem = message.reader().readByte();
                itemInInventory.beKicked = message.reader().readByte();
                itemInInventory.hangItem = message.reader().readByte();
                itemInInventory.magic_physic = message.reader().readByte();
                itemInInventory.lock = message.reader().readByte();
                itemInInventory.nameCharSeal = message.reader().readUTF();
                itemInInventory.nameCharSell = message.reader().readUTF();
                itemInInventory.nameCharBet = message.reader().readUTF();
                itemInInventory.prize = message.reader().readInt();
                itemInInventory.prizeBet = message.reader().readInt();
                itemInInventory.timeEnd = message.reader().readLong();
                itemInInventory.daySell = message.reader().readUTF();
                itemInInventory.allAttribute.removeAllElements();
                byte b4 = message.reader().readByte();
                for (byte b5 = 0; b5 < b4; b5 = (byte) (b5 + 1)) {
                    itemInInventory.allAttribute.addElement(new InfoAttributeItem((short) message.reader().readUnsignedByte(), message.reader().readShort()));
                }
                ((Vector) ShopOnline.ALL_ITEM.elementAt(b2)).addElement(itemInInventory);
            }
            if (b == 0) {
                GCanvas.shop.setItemInventory();
            }
            if (b == 3) {
                ShopOnline.money = message.reader().readLong();
            }
            ShopOnline.nPage[b2] = message.reader().readShort();
            if (ShopOnline.indexPage[b2] >= ShopOnline.nPage[b2]) {
                ShopOnline.indexPage[b2] = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTemplateInfo(Message message) {
        try {
            short unsignedByte = (short) message.reader().readUnsignedByte();
            short s = message.reader().readShort();
            if (unsignedByte == 0) {
                int i = message.reader().readByte();
                byte[] bArr = new byte[i];
                message.reader().read(bArr, 0, i);
                Res.loadMonster(s, bArr);
                Res.monsterTemplates[s].name = message.reader().readUTF();
                Res.monsterTemplates[s].he = message.reader().readByte();
                Res.monsterTemplates[s].maxhp = message.reader().readInt();
                Res.monsterTemplates[s].palate = message.reader().readByte();
                Res.monsterTemplates[s].spalate = message.reader().readByte();
                byte b = message.reader().readByte();
                Res.monsterTemplates[s].isNewMonster = b;
                if (b == 1) {
                    int i2 = message.reader().readShort();
                    byte[] bArr2 = new byte[i2];
                    message.reader().read(bArr2, 0, i2);
                    Res.monsterTemplates[s].doSetDataMonsterNew(bArr2);
                }
            }
        } catch (Exception unused) {
        }
    }

    public void onDynamicEffect(Message message) {
        try {
            byte b = message.reader().readByte();
            short s = message.reader().readShort();
            short s2 = message.reader().readShort();
            long j = message.reader().readLong();
            int i = message.reader().readByte() * 16;
            int i2 = message.reader().readByte() * 16;
            byte b2 = message.reader().readByte();
            byte b3 = message.reader().readByte();
            LiveActor liveActor = (LiveActor) (b2 == 0 ? findCharByID(s2) : findMonsterByID(s2));
            DynamicEffect dynamicEffect = new DynamicEffect(s);
            dynamicEffect.timeExist = j;
            dynamicEffect.x = (short) i;
            dynamicEffect.y = (short) i2;
            dynamicEffect.catagory = b2;
            dynamicEffect.stop = message.reader().readByte();
            byte b4 = message.reader().readByte();
            try {
                dynamicEffect.dam = message.reader().readInt();
            } catch (Exception unused) {
            }
            if (liveActor != null && b == 1) {
                if (b3 == 0) {
                    liveActor.addDynamicEffBuffBottom(dynamicEffect, b4);
                    return;
                } else {
                    liveActor.addDynamicEffBuffTop(dynamicEffect, b4);
                    return;
                }
            }
            if (s2 > 32000 || b == 0) {
                if (liveActor != null) {
                    dynamicEffect.x = liveActor.x;
                    dynamicEffect.y = liveActor.y;
                }
                this.actors.addElement(dynamicEffect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startSkillEffect(LiveActor liveActor, int i, long j, int i2, int i3, int i4) {
        System.out.println("TAN CONG: " + liveActor.getCharName());
        DynamicEffect dynamicEffect = new DynamicEffect(i);
        dynamicEffect.timeExist = j;
        dynamicEffect.x = liveActor.x;
        dynamicEffect.y = liveActor.y;
        dynamicEffect.catagory = liveActor.catagory;
        dynamicEffect.stop = (byte) 0;
        dynamicEffect.power = i3;
        if (i4 != 0) {
            GCanvas.gameScr.actors.addElement(dynamicEffect);
        } else if (i2 == 0) {
            liveActor.addDynamicEffBuffBottom(dynamicEffect, 0);
        } else {
            liveActor.addDynamicEffBuffTop(dynamicEffect, 0);
        }
    }

    public void onPetAttack(Message message) {
        try {
            short s = message.reader().readShort();
            byte b = message.reader().readByte();
            if (b == 0) {
                short s2 = message.reader().readShort();
                int i = message.reader().readByte();
                int[] iArr = new int[i];
                int[] iArr2 = new int[i];
                Vector vector = new Vector();
                Actor actorFindActorByID_Cat = GCanvas.gameScr.findActorByID_Cat(s, (byte) 12);
                if (actorFindActorByID_Cat != null) {
                    for (int i2 = 0; i2 < i; i2++) {
                        byte b2 = message.reader().readByte();
                        Actor actorFindActorByID_Cat2 = GCanvas.gameScr.findActorByID_Cat(message.reader().readShort(), b2);
                        if (actorFindActorByID_Cat2 != null) {
                            vector.addElement(actorFindActorByID_Cat2);
                        }
                        iArr[i2] = message.reader().readInt();
                        iArr2[i2] = message.reader().readInt();
                    }
                    actorFindActorByID_Cat.petAttack(vector, b, s2, iArr, iArr2);
                }
            }
            if (b == 1) {
                Actor actorFindActorByID = GCanvas.gameScr.findActorByID(s);
                short s3 = message.reader().readShort();
                long j = message.reader().readLong();
                if (actorFindActorByID != null) {
                    actorFindActorByID.addEffbuff(s3, actorFindActorByID.x, actorFindActorByID.y, j);
                }
            }
        } catch (Exception unused) {
        }
    }

    public Actor findActorByID_Cat(short s, byte b) {
        for (int size = this.actors.size() - 1; size >= 0; size--) {
            Actor actor = (Actor) this.actors.elementAt(size);
            if (actor.ID == s && actor.catagory == b) {
                return actor;
            }
        }
        return null;
    }

    public void onEffData(Message message) {
        byte b;
        try {
            if (message.reader().readByte() == 0) {
                byte b2 = message.reader().readByte();
                short s = message.reader().readShort();
                short s2 = message.reader().readShort();
                int i = message.reader().readInt();
                boolean z = message.reader().readBoolean();
                boolean z2 = message.reader().readBoolean();
                long jCurrentTimeMillis = i > 0 ? ((long) i) + System.currentTimeMillis() : 0L;
                try {
                    Actor actorFindActorByID_Cat = findActorByID_Cat(s, b2);
                    try {
                        b = message.reader().readByte();
                    } catch (Exception unused) {
                        b = 0;
                    }
                    byte b3 = b;
                    if (actorFindActorByID_Cat != null) {
                        actorFindActorByID_Cat.addEffectSkillTime(s2, actorFindActorByID_Cat.x, actorFindActorByID_Cat.y, jCurrentTimeMillis, z, z2, true, 0, (byte) Res.random(1, 8), b3);
                    }
                } catch (Exception unused2) {
                }
            }
        } catch (Exception unused3) {
        }
    }

    public static void paint12plus(Graphics graphics) {
        if (paint18plush == 0 || Font.verySmallFont == null) {
            return;
        }
        int width = Font.verySmallFont.getWidth("18+ Chơi quá 180 phút một ngày sẽ ảnh hưởng xấu đến sức khỏe");
        graphics.setColor(0);
        graphics.setAlpha(80);
        graphics.fillRect(4, 0, width, 13);
        Font.verySmallFont.drawString(graphics, "18+ Chơi quá 180 phút một ngày sẽ ảnh hưởng xấu đến sức khỏe", 4, 4, 0);
    }
}
