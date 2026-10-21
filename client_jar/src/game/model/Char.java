package game.model;

import effect.DataSkillEff;
import effect.DynamicEffect;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Char extends LiveActor {
    public static Image imgLight;
    public int IDDB;
    public SkillAnimate currentSkillAnimate;
    public byte currentSkillType;
    public int lastXAuto;
    public int lastYAuto;
    public String name;
    public LiveActor attackTarget;
    public byte upSpeed;
    public byte gender;
    public long timeComeHome;
    public long timeWearScarves;
    public short p1;
    public short p2;
    public short p3;
    public short p4;
    public short xTo;
    public short yTo;
    public short wName;
    public static final byte A_STAND = 0;
    public static final byte A_RUN = 1;
    public static final byte A_ATTACK = 2;
    public static final byte A_DEAD = 3;
    public static final byte A_COME_HOME = 4;
    public static final byte MOVETOFIRST = 5;
    public short basePointLeft;
    public short skillPointLeft;
    public short strength;
    public short spirit;
    public short agility;
    public short health;
    public short luck;
    public static final byte DOWN = 0;
    public static final byte UP = 1;
    public static final byte LEFT = 2;
    public static final byte RIGHT = 3;
    public static final byte REAL_FRAME_EACH_SIZE = 5;
    // pokemon: than Pokemon (lenh -90 POKE_THAN), 0 = ve paper doll cu (Unity Char.cs)
    public short pokeDex;
    public boolean pokeShiny;
    /** Tick con lai cua hieu ung tien hoa (lenh -92). */
    public int pokeTienHoaCon;
    public short pokeDexTruoc;
    /** Pokemon thu cuoi (lenh -93 POKE_CUOI), 0 = khong cuoi. */
    public short pokeThuDex;
    public boolean pokeThuShiny;
    /** Pet di theo dang Pokemon (lenh -94 POKE_PET), 0 = pet kieu cu. */
    public short pokePetDex;
    public boolean pokePetShiny;
    /** 0.9.8: bạn đồng hành từ Hộp PC (lệnh -121 kiểu 3, chỉ để nhìn), 0 = không; vị trí đuổi theo mượt. */
    public short pokeBanDex;
    public boolean pokeBanShiny;
    public short banX = Short.MIN_VALUE, banY;
    /** Ngoai hinh trang bi (lenh -102 POKE_NGOAI_HINH): phu kien, vang sang, trang phuc. null = chua co. */
    public game.pokemon.NgoaiHinhCua ngoaiHinh;
    /** 1,5 giay o 25 tick/giay (Unity: 75 tick o 50/giay). */
    public static final int POKE_TICK_TIEN_HOA = 38;
    public int attkPower;
    public int wimg;
    public int himg;
    public byte clazz;
    public short lvpercent;
    public static byte[] skillLevelLearnt;
    public Vector wearingItems;
    public Vector wearingAnimal;
    public Image imgWpa;
    public Image imgAnimal;
    public short dxWear;
    public short dyWear;
    public short[] listEffect;
    /** 0.9.7 danh hiệu đang đeo (gói -110 nhãn, DanhHieuKho): id, tên (null = không), độ hiếm 0..3. */
    public int danhHieuId = -1;
    public String danhHieuTen;
    public int danhHieuHiem;
    public byte[] Tick;
    public byte numFrame;
    public long charXu;
    public int maxmp;
    public int mp;
    public byte attkEffect;
    public int lockUpdate;
    public int[][] monster_kill;
    public byte[] npc_response;
    public byte[] npc_quest;
    public boolean[] talked;
    public String[][] info_response;
    public String[] info_npc_quest;
    public boolean isTrade;
    public boolean gotoBoard;
    public boolean isNo;
    public boolean isDoing;
    public ItemInInventory itemImbue;
    public long timeWait2Board;
    public long timeUseGolgTicket;
    public static final byte KIEM = 0;
    public static final byte DAO = 1;
    public static final byte PS = 2;
    public static final byte DS = 3;
    public static final byte CUNG = 4;
    public Effect effPhap;
    public Effect effAnimal;
    public boolean isAnimal;
    public AnimalMove myPet;
    /** pokemon: actor pet ĐANG ở trong cảnh (Pet hoặc AnimalMove) - một pet / người chơi (game.pokemon.PetDuyNhat) */
    public AnimalMove petCanh;
    public short baokich;
    public int congTrang;
    public int lienTram;
    public int hoatdong;
    public long stand;
    public static final byte[] STANDFRAME;
    public static final byte[] RUNFRAME;
    public static final byte[] RUNFRAMEWAL;
    public static final byte[] GENDER_OF_CLAZZ;
    public long timeDelayRequestCharInfo;
    public long timeGoToLastXY;
    public static Hashtable ALL_PART_EFF;
    public static short[][][] fHorse;
    public static final byte[][] dxh;
    public static final byte[][] dyh;
    public static final byte[][] dyb;
    public static final byte[][][] dyr;
    public static final byte[][][] dxr;
    public static final byte[][] dxs;
    static final byte[][][] sizeHorse;
    static final byte[] hHorse;
    static byte[][][] ORDER_PAINT;
    public int frameThuCuoi;
    public int fh;
    public static byte[] horseMove;
    public Effect EffFace;
    static int count;
    int idLight;
    static final byte[] lightFrame;
    static final byte[][] skill_type;
    public static WeaponInfo[][][] imgWeapone;
    static final byte[][] tWeapone;
    static final byte[][] pWeapone;
    static final byte[][] sw;
    static final byte[][] dwx;
    static final byte[][] dwy;
    static final byte[] hwp;
    static final byte[] fixY;
    public static byte[][] dx_horse;
    public static byte[][] dy_horse;
    public static byte[][] Dx_Dy_WP;
    public static byte[][] Dx_Dy_PP;
    public static byte[][] Dx_Dy_AVT;
    private static byte[][] countX;
    private static byte[][] countY;
    byte ppAvartar;
    byte wpAvartar;
    byte AVT;
    public static int[][] mainDef;
    public static int[][] mainAt;
    public static int[][] pcAttack;
    public static int[][] pcDef;
    byte Frameeff;
    public static final byte[] BUFFTYPE;
    public static short CHAR_POTION = 25;
    public static Image imgFlgPk = null;
    public static boolean dissolved = false;
    public static byte[] nSkill = new byte[5];
    public static Vector inventory = new Vector();
    public static Vector animal = new Vector();
    public static Vector party = new Vector();
    public static Vector itembag = new Vector();
    public static int goldTimeMinute = 0;
    public static int idGoldTicket = 0;
    public static String goldTime = "";
    public static byte char_speed = 3;
    public static final byte[] NEXTX = {0, 0, -1, 1};
    public static final byte[] NEXTY = {1, -1, 0, 0};
    public int using = 1;
    public byte isMaster = 3;
    public boolean justStopFromRun = false;
    public boolean paintHat = true;
    public byte idAnimal = -1;
    public byte idPaintIconAnimal = 0;
    public String infoAnimal = "";
    public long[] timeLastUseSkills = new long[11];
    public long[] coolDownSkill = new long[11];
    public short weapon_frame = -1;
    public short bodyStyle = -1;
    public short legStyle = -1;
    public short hatStyle = -1;
    public short currentHead = -1;
    public short coatStyle = -1;
    public byte idFashion = -1;
    public byte timeChangeFrame = 1;
    public int luong = 0;
    public int luongKhoa = -1;
    public short idhorse = -1;
    public short typehorse = -1;
    public short idHair = -1;
    public short idBody = -1;
    public short idLeg = -1;
    public short idPhiPhong = -1;
    public short idWeapon = -1;
    public short typeWeapon = -1;
    public short idImageWeapon = -1;
    public byte indexPP = -1;
    public byte nFrameWP = 0;
    public byte nFramePP = 0;
    public byte nFrameAVT = 0;
    public byte indexAVT = -1;
    public short idImgPP = -1;
    public short idImgAVT = -1;
    public int weaponStyle = -1;
    public int weaponType = -1;
    public byte weaponIndex = 0;
    public int[] potions = new int[CHAR_POTION];
    public long[] potionLastTimeUse = new long[CHAR_POTION];
    public byte[] buffType = {-1, -1, -1, -1, -1, -1, -1};
    public byte[] lvBuff = new byte[7];
    public short[] countDown = new short[7];
    public boolean useBuff = false;
    public short questID = -1;
    public short idQuestNPC = -1;
    public boolean receiveQuest = false;
    public boolean finishQuest = false;
    public byte nextNpc = 0;
    public byte typeQuest = 3;
    public int delay = -1;
    public IsAnimal myAnimal = null;
    public short[] idModel = {-1, -1, -1, -1, -1};
    public boolean comeHome = false;
    public int realTime = 0;
    public long timeRemoEffanimal = 0;
    int idColor = 0;
    public int ifHorse = 0;
    public byte idImgHorse = 0;
    public byte useHorse = -1;
    public byte anmFly = 0;
    public short[] infoPaint = {-1, -1, -1, -1, -1, -1};
    public int Fhorse = 0;
    byte countNo = 0;
    public byte dyThanthu = 0;
    int time = 0;
    public byte FrameHair = 0;
    public byte Framebody = 0;
    public byte frameLeg = 0;
    public byte framePP = 0;
    public byte FrameWP = 0;
    boolean xuyengiap = false;
    public String infoQuest = "";

    static {
        byte[] bArr = new byte[12];
        bArr[8] = 1;
        bArr[9] = 1;
        bArr[10] = 1;
        bArr[11] = 1;
        STANDFRAME = bArr;
        RUNFRAME = new byte[]{2, 2, 2, 0, 0, 0, 3, 3, 3, 0, 0, 0};
        RUNFRAMEWAL = new byte[]{2, 2, 2, 0, 0, 3, 3, 3, 0, 0};
        GENDER_OF_CLAZZ = new byte[]{1, 1, 2, 1, 2};
        ALL_PART_EFF = new Hashtable();
        fHorse = new short[][][]{new short[][]{new short[]{95, 112, 130}, new short[]{246, 263, 280}, new short[]{216, 182, 149}, new short[]{298, 329, 363}}, new short[][]{new short[]{119, 138, 158}, new short[]{308, 325, 341}, new short[]{267, 223, 178}, new short[]{359, 400, 444}}, new short[][]{new short[]{107, 124, 140}, new short[]{276, 292, 309}, new short[]{239, 197, 158}, new short[]{324, 362, 404}}, new short[][]{new short[]{115, 133, 150}, new short[]{296, 313, 330}, new short[]{254, 210, 167}, new short[]{347, 387, 433}}, new short[][]{new short[]{129, 152, 179, 152}, new short[]{210, 233, 260, 233}, new short[]{0, 43, 86, 43}, new short[]{291, 334, 377, 334}}, new short[][]{new short[]{180, 210, 255, 210}, new short[]{303, 333, 379, 333}, new short[]{0, 60, 120, 60}, new short[]{427, 487, 547, 487}}, new short[][]{new short[]{0, 24, 48}, new short[]{171, 192, 213}, new short[]{72, 106, 140}, new short[]{234, 265, 299}}, new short[][]{new short[]{180, 210, 255, 210}, new short[]{303, 333, 379, 333}, new short[]{0, 60, 120, 60}, new short[]{427, 487, 547, 487}}};
        dxh = new byte[][]{new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, 3, -4}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, 3, -4}};
        dyh = new byte[][]{new byte[4], new byte[4], new byte[4], new byte[4], new byte[4], new byte[]{-2, 0, 7, 7}, new byte[4], new byte[]{-2, 0, 7, 7}};
        dyb = new byte[][]{new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}};
        dyr = new byte[][][]{new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}}, new byte[][]{new byte[]{-3, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{-3, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}}};
        dxr = new byte[][][]{new byte[][]{new byte[3], new byte[3], new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[3], new byte[3], new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[3], new byte[3], new byte[]{-2, -2, -2}, new byte[]{2, 2, 2}}, new byte[][]{new byte[3], new byte[3], new byte[]{-2, -2, -2}, new byte[]{2, 2, 2}}, new byte[][]{new byte[4], new byte[4], new byte[]{-4, -4, -4, -4}, new byte[]{2, 2, 2, 2}}, new byte[][]{new byte[4], new byte[4], new byte[]{-4, -4, -2, -4}, new byte[]{2, 2, 1, 2}}, new byte[][]{new byte[3], new byte[3], new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[4], new byte[4], new byte[]{-4, -4, -2, -4}, new byte[]{2, 2, 1, 2}}};
        dxs = new byte[][]{new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}};
        sizeHorse = new byte[][][]{new byte[][]{new byte[]{18, 18, 18}, new byte[]{17, 17, 17}, new byte[]{31, 34, 33}, new byte[]{31, 34, 33}}, new byte[][]{new byte[]{20, 20, 20}, new byte[]{17, 17, 17}, new byte[]{41, 44, 44}, new byte[]{41, 44, 44}}, new byte[][]{new byte[]{17, 17, 17}, new byte[]{16, 16, 16}, new byte[]{37, 42, 40}, new byte[]{37, 42, 40}}, new byte[][]{new byte[]{17, 17, 17}, new byte[]{17, 17, 17}, new byte[]{41, 45, 43}, new byte[]{41, 45, 43}}, new byte[][]{new byte[]{23, 27, 31, 27}, new byte[]{23, 27, 31, 27}, new byte[]{43, 43, 43, 43}, new byte[]{43, 43, 43, 43}}, new byte[][]{new byte[]{30, 45, 48, 45}, new byte[]{30, 45, 48, 45}, new byte[]{60, 60, 60, 60}, new byte[]{60, 60, 60, 60}}, new byte[][]{new byte[]{24, 24, 24}, new byte[]{21, 21, 21}, new byte[]{34, 34, 31}, new byte[]{31, 34, 34}}, new byte[][]{new byte[]{30, 45, 48, 45}, new byte[]{30, 45, 48, 45}, new byte[]{60, 60, 60, 60}, new byte[]{60, 60, 60, 60}}};
        hHorse = new byte[]{28, 26, 23, 27, 35, 45, 37, 45};
        ORDER_PAINT = new byte[][][]{new byte[][]{new byte[]{5, 4, 0, 1, 6, 2, 3}, new byte[]{6, 1, 4, 2, 3, 5}, new byte[]{4, 5, 0, 1, 2, 3, 6}, new byte[]{4, 5, 0, 1, 2, 3, 6}}, new byte[][]{new byte[]{4, 0, 1, 6, 2, 3, 5}, new byte[]{6, 0, 1, 4, 2, 3, 5}, new byte[]{4, 0, 1, 2, 3, 5, 6}, new byte[]{4, 0, 1, 2, 3, 5, 6}}};
        horseMove = new byte[]{2, 2, 2, 3, 3, 4, 4, 4};
        count = 0;
        lightFrame = new byte[]{0, 1, 2, 3, 2, 1};
        skill_type = new byte[][]{new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 0, 2, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}};
        imgWeapone = new WeaponInfo[5][15][7];
        byte[] bArr2 = new byte[4];
        bArr2[0] = 2;
        byte[] bArr3 = new byte[4];
        bArr3[1] = 2;
        tWeapone = new byte[][]{bArr2, new byte[]{0, 2, 2, 0}, new byte[]{2, 0, 0, 2}, bArr3, new byte[]{0, 2, 2, 0}};
        pWeapone = new byte[][]{new byte[]{0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 29, 14}, new byte[]{12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 11, 11, 0, 0}, new byte[28], new byte[]{15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 16, 16, 43, 0}, new byte[]{14, 14, 0, 0, 15, 15, 0, 0, 15, 15, 0, 0, 15, 15, 0, 0, 16, 16, 0, 0, 19, 19, 0, 0, 21, 21, 0, 0}};
        sw = new byte[][]{new byte[]{21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 14, 14, 5, 15}, new byte[]{19, 19, 9, 11, 19, 19, 9, 11, 19, 19, 9, 11, 19, 19, 9, 11, 20, 20, 12, 12, 19, 19, 9, 11, 20, 20, 12, 12}, new byte[]{14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 14, 14, 14, 14, 15, 15, 15, 15}, new byte[]{24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15}, new byte[]{21, 21, 14, 14, 21, 21, 15, 15, 21, 21, 15, 15, 21, 21, 15, 15, 22, 22, 16, 16, 21, 21, 15, 15, 26, 26, 20, 20}};
        byte[] bArr4 = new byte[28];
        bArr4[2] = -6;
        bArr4[6] = -6;
        bArr4[10] = -6;
        bArr4[14] = -6;
        bArr4[18] = -6;
        bArr4[22] = -6;
        bArr4[24] = 6;
        bArr4[25] = 2;
        bArr4[27] = 1;
        byte[] bArr5 = new byte[28];
        bArr5[2] = -5;
        bArr5[6] = -5;
        bArr5[10] = -5;
        bArr5[14] = -5;
        bArr5[18] = -7;
        bArr5[22] = -5;
        bArr5[26] = -7;
        byte[] bArr6 = new byte[28];
        bArr6[6] = -1;
        bArr6[10] = -1;
        bArr6[14] = -1;
        bArr6[18] = -1;
        bArr6[20] = 3;
        bArr6[21] = -2;
        bArr6[22] = 1;
        bArr6[23] = 1;
        bArr6[25] = -2;
        bArr6[26] = -2;
        bArr6[27] = -2;
        dwx = new byte[][]{bArr4, bArr5, new byte[28], new byte[28], bArr6};
        byte[] bArr7 = new byte[28];
        bArr7[2] = 1;
        bArr7[6] = 1;
        bArr7[10] = 1;
        bArr7[14] = 1;
        bArr7[18] = 1;
        bArr7[22] = 1;
        bArr7[26] = 1;
        byte[] bArr8 = new byte[28];
        bArr8[2] = -3;
        bArr8[6] = -3;
        bArr8[10] = -3;
        bArr8[14] = -3;
        bArr8[18] = -3;
        bArr8[22] = -3;
        bArr8[26] = -3;
        dwy = new byte[][]{bArr7, bArr8, new byte[28], new byte[28], new byte[28]};
        hwp = new byte[]{21, 40, 14, 28, 20};
        byte[] bArr9 = new byte[5];
        bArr9[1] = -1;
        fixY = bArr9;
        dx_horse = new byte[][]{new byte[]{0, 0, 7, -7}};
        dy_horse = new byte[][]{new byte[]{-20, -15, -15, -15}};
        Dx_Dy_WP = new byte[][]{new byte[1], new byte[1]};
        Dx_Dy_PP = new byte[][]{new byte[1], new byte[1]};
        Dx_Dy_AVT = new byte[][]{new byte[1], new byte[1]};
        countX = new byte[][]{new byte[]{7, -15, 7, -12}, new byte[]{6, -14, -4, -1}, new byte[]{-12, 3, 8, -12}, new byte[]{9, -15, 7, -10}, new byte[]{-14, -13, 7, -11}};
        countY = new byte[][]{new byte[]{-12, -8, -10, -1}, new byte[]{-12, -8, -15, -11}, new byte[]{-6, -6, -6, -6}, new byte[]{-12, -5, -19, 0}, new byte[]{-18, 3, 0, -4}};
        int[] iArr = new int[5];
        iArr[1] = 4;
        mainDef = new int[][]{new int[5], new int[]{2, 0, 0, 3, 0}, new int[5], new int[5], iArr};
        int[] iArr2 = new int[5];
        iArr2[0] = 1;
        mainAt = new int[][]{new int[5], new int[5], new int[5], new int[5], iArr2};
        pcAttack = new int[][]{new int[]{0, -25, 0, 25, 15}, new int[]{10, 0, -15, 10, -10}, new int[]{5, 0, 0, -35, -5}, new int[]{-20, 5, 35, 0, -10}, new int[]{80, -5, 5, 30, 0}};
        pcDef = new int[][]{new int[]{0, -5, -15, 20, 0}, new int[]{-90, 0, -15, 40, -35}, new int[]{0, 0, 0, -5, -15}, new int[]{-20, 10, 5, 0, -10}, new int[]{-25, 0, 0, 20, 0}};
        BUFFTYPE = new byte[]{19, 20, 22, 23, 24, 25, 27};
    }

    public void resetQuest() {
        this.nextNpc = (byte) 0;
        this.questID = (short) -1;
        this.idQuestNPC = (short) -1;
        this.receiveQuest = false;
        this.finishQuest = false;
        this.info_npc_quest = null;
        this.info_response = null;
        this.typeQuest = (byte) -1;
        this.monster_kill = null;
        this.npc_response = null;
        this.npc_quest = null;
    }

    public void cancelQuest() {
        this.nextNpc = (byte) 0;
        this.idQuestNPC = (short) -1;
        this.receiveQuest = false;
        this.finishQuest = false;
        this.info_npc_quest = null;
        this.info_response = null;
        this.typeQuest = (byte) -1;
        this.monster_kill = null;
        this.npc_response = null;
        this.npc_quest = null;
    }

    public boolean isFullInventory() {
        return FullInventory() > 42 * Canvas.gameScr.mainChar.limItemBag;
    }

    public int FullInventory() {
        return inventory.size() + MainChar.gemItem.size() + getCountPotion() + MainChar.shopItem.size();
    }

    public byte getnSkill() {
        return nSkill[this.clazz];
    }

    public String getGoldTime() {
        String st = "";
        int t = goldTimeMinute * 60000;
        long time = System.currentTimeMillis() - this.timeUseGolgTicket;
        long aaa = ((long) t) - time;
        if (aaa > 0) {
            long h = aaa / 3600000;
            long m = ((aaa % 3600000) / 1000) / 60;
            st = new StringBuffer(String.valueOf(h)).append(" : ").append(m).toString();
            if (h == 0 && m == 0) {
                st = "";
            }
        }
        return st;
    }

    public boolean isLKD(int idTemplate) {
        return idTemplate == 8 || idTemplate == 9 || idTemplate == 10 || idTemplate == 11 || idTemplate == 66 || idTemplate == 245 || idTemplate == 157 || idTemplate == 158;
    }

    public int countLKD() {
        int n = 0;
        for (int i = 0; i < MainChar.gemitemImbue.size(); i++) {
            RealID gem = (RealID) MainChar.gemitemImbue.elementAt(i);
            if ((gem.ID > 7 && gem.ID < 12) || gem.ID == 158 || gem.ID == 157) {
                n++;
            }
        }
        return n;
    }

    public int countBaoHiem() {
        int n = 0;
        for (int i = 0; i < MainChar.gemitemImbue.size(); i++) {
            RealID gem = (RealID) MainChar.gemitemImbue.elementAt(i);
            if (gem.ID < 5) {
                n++;
            }
        }
        return n;
    }

    public boolean haveItemImbue() {
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            GemTemplate gem = (GemTemplate) MainChar.gemItem.elementAt(i);
            if ((gem.id > 7 && gem.id < 12) || gem.id == 66 || gem.id == 245 || gem.id == 158 || gem.id == 157) {
                return true;
            }
        }
        return false;
    }

    public boolean hadPutItemImbue() {
        for (int i = 0; i < MainChar.gemitemImbue.size(); i++) {
            RealID gem = (RealID) MainChar.gemitemImbue.elementAt(i);
            if ((gem.ID > 7 && gem.ID < 12) || gem.ID == 158 || gem.ID == 66 || gem.ID == 245 || gem.ID == 157) {
                return true;
            }
            if (gem.ID >= 12 && gem.ID <= 66) {
                return true;
            }
        }
        return false;
    }

    public int isExistInvector(Vector v, ItemInInventory cii) {
        for (int i = 0; i < v.size(); i++) {
            if (v.elementAt(i) instanceof ItemInInventory) {
                ItemInInventory c = (ItemInInventory) v.elementAt(i);
                if (c.itemID == cii.itemID) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void dead() {
        this.state = (byte) 3;
    }

    public Char() {
        this.timeDelayRequestCharInfo = 0L;
        this.catagory = (byte) 0;
        this.speed = char_speed;
        this.upSpeed = (byte) 0;
        this.centerX = (short) 12;
        this.centerY = (short) 12;
        this.height = (short) 32;
        this.width = (short) 40;
        this.hp = 1;
        this.realHP = 1;
        this.dir = (short) LiveActor.r.nextInt(4);
        this.timeDelayRequestCharInfo = System.currentTimeMillis();
        this.isDie = false;
    }

    @Override
    public String getDisplayName() {
        return this.name == null ? "" : this.name;
    }

    @Override
    public void paintCorner(Graphics g, int xCorner, int yCorner) {
        if (this.pokeDex > 0 && PokeBody.paintChanDung(g, this.pokeDex, this.pokeShiny, xCorner + 2, yCorner - 10, 24)) {
            return;
        }
        g.translate((-this.x) + xCorner + 2, ((-this.y) + yCorner) - 10);
        if (this.currentHead != -1) {
            Res.getCharPartInfo(2, this.currentHead).paintStatic(g, this.x, (short) (this.y + (this.hatStyle != -1 ? (short) 5 : (short) 0)), this.dir, this.frame);
        }
        if (this.hatStyle != -1) {
            Res.getCharPartInfo(3, this.hatStyle).paintStatic(g, this.x, this.y, this.dir, this.frame);
        }
    }

    public void moveTo(short xTo, short yTo) {
        if (!canMove()) {
            return;
        }
        this.xTo = xTo;
        this.yTo = yTo;
        if (this.x == xTo && this.y == yTo) {
            this.stand = System.currentTimeMillis();
            if (this.state != 2 && this.state != 3) {
                this.state = (byte) 0;
                return;
            }
            return;
        }
        this.state = (byte) 1;
    }

    @Override
    public void updateBuff() {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff cbuf = (CharBuff) this.buff.elementAt(i);
            cbuf.update();
            if (cbuf.wantDetroy) {
                this.buffType[i] = -1;
                this.lvBuff[i] = -1;
                this.buff.removeElementAt(i);
            } else {
                cbuf.setXY(this.x, cbuf.isCenter() ? this.y - 12 : this.y);
            }
        }
    }

    public void createBuff(int id) {
        if (!isExistBuff(id)) {
            CharBuff cb = new CharBuff(this.x, this.y, this.clazz + 19);
            if (id == this.ID) {
                cb.setTimeLive(20);
            }
            this.buff.addElement(cb);
        }
    }

    public void removeBuff(int id) {
        for (int i = 0; i < this.buff.size(); i++) {
            if (((CharBuff) this.buff.elementAt(i)).type == id) {
                this.buff.removeElementAt(i);
                return;
            }
        }
    }

    /** Dat lai thoi gian con lai cua bieu tuong hieu ung dang co theo server (giay). */
    public void lamMoiBuff(int id, int timeOut) {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff cb = (CharBuff) this.buff.elementAt(i);
            if (cb.type == id) {
                cb.setTimeLive(timeOut);
                return;
            }
        }
    }

    public boolean isExistBuff(int id) {
        for (int i = 0; i < this.buff.size(); i++) {
            if (((CharBuff) this.buff.elementAt(i)).type == id) {
                return true;
            }
        }
        return false;
    }

    public void setPercentSpeed(int percent) {
        this.upSpeed = (byte) ((percent * this.speed) / 100);
        if ((percent * this.speed) % 100 >= 5) {
            this.upSpeed = (byte) (this.upSpeed + 1);
        }
    }

    public int[] getTime() {
        long aa = System.currentTimeMillis() - this.timeWait2Board;
        return new int[]{this.delay, 59 - ((((int) aa) / 1000) % 60)};
    }

    @Override
    public void actorDie() {
        this.hp = 0;
        this.timeOutPoinson = 0L;
        this.tDelay = (short) 0;
        this.totalTime = (short) 36;
        this.state = (byte) 3;
        this.poinson = (short) 0;
        this.Vec_Effect_Skill.removeAllElements();
        this.effSkill.removeAllElements();
    }

    @Override
    public void isPoinson() {
        if (System.currentTimeMillis() - this.timeOutPoinson >= this.tDelay * 1000 && this.tDelay > 0) {
            this.hp -= this.poinson;
            this.totalTime = (short) (this.totalTime - this.tDelay);
            this.timeOutPoinson = System.currentTimeMillis();
            if (this.totalTime == 0) {
                this.tDelay = (short) 0;
                this.totalTime = (short) 36;
            }
            if (this.hp <= 0 && this.state != 3) {
                this.hp = 1;
                this.poinson = (short) 0;
                this.tDelay = (short) 0;
                this.totalTime = (short) 36;
            }
        }
    }

    @Override
    public byte getHorse() {
        if (Tilemap.isOfflineMap) {
            return (byte) -1;
        }
        return this.useHorse;
    }

    public static DataSkillEff getPartEff(short id) {
        if (id == -1 || game.pokemon.SinhThai.laDanhHieu(id) || game.pokemon.DanhHieuKho.laDanhHieu(id)) {
            return null;   // danh hiệu sinh thái: huy hiệu vẽ bằng code, không xin dữ liệu hiệu ứng
        }
        DataSkillEff ef = (DataSkillEff) ALL_PART_EFF.get(new StringBuffer(String.valueOf((int) id)).toString());
        if (ef == null) {
            ef = new DataSkillEff(id);
            ALL_PART_EFF.put(new StringBuffer(String.valueOf((int) id)).toString(), ef);
            ef.timeRemove = (int) (System.currentTimeMillis() / 1000);
        } else {
            ef.timeRemove = System.currentTimeMillis();
        }
        return ef;
    }

    public static void SetRemove() {
        Enumeration k = ALL_PART_EFF.keys();
        while (k.hasMoreElements()) {
            String keyset = (String) k.nextElement();
            DataSkillEff img = (DataSkillEff) ALL_PART_EFF.get(keyset);
            if ((System.currentTimeMillis() - img.timeRemove) / 1000 > 200) {
                ALL_PART_EFF.remove(keyset);
            }
        }
    }

    public void updateHorse() {
        DataSkillEff partHorse;
        if (Canvas.gameTick % 5 == 0 && this.idhorse != -1 && (partHorse = getPartEff(this.idhorse)) != null) {
            int ms = partHorse.listFrame.size() / 21;
            if (ms == 0) {
                ms = 1;
            }
            this.frameThuCuoi = (byte) ((this.frameThuCuoi + 1) % ms);
        }
    }

    @Override
    public void update() {
        int set;
        if (this.pokeTienHoaCon > 0) {
            this.pokeTienHoaCon--;
        }
        if (this.ID == 32001) {
            return;
        }
        if (this.hp > 0) {
        }
        updateframeeff();
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                DynamicEffect dym = (DynamicEffect) this.dynamicEffTop.elementAt(i);
                dym.update();
                if (dym.canDestroy()) {
                    this.dynamicEffTop.removeElementAt(i);
                    if (dym.power < 2000000000) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(dym.power).toString(), 0, this.x + 0, (this.y + this.dy) - 15, 1, -2);
                    }
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i2 = 0; i2 < this.dynamicEffBottom.size(); i2++) {
                DynamicEffect dym2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i2);
                dym2.update();
                if (dym2.canDestroy()) {
                    this.dynamicEffBottom.removeElementAt(i2);
                    if (dym2.power < 2000000000) {
                        Canvas.gameScr.startFlyText(new StringBuffer("-").append(dym2.power).toString(), 0, this.x + 0, (this.y + this.dy) - 15, 1, -2);
                    }
                }
            }
        }
        if (!this.isFreeze) {
            if (this.EffFace != null) {
                this.EffFace.update();
            }
            if (this.effPhap != null) {
                this.effPhap.update();
            }
            if (this.beStune && System.currentTimeMillis() > this.timeBeStune) {
                this.beStune = false;
            }
            if (System.currentTimeMillis() - this.timeDelayRequestCharInfo > 10000 && (set = fullSet()) != 1) {
                if (this.ID != Canvas.gameScr.mainChar.ID) {
                    if (GameScr.paintChar == 1) {
                        GameService.gI().requestCharInfo(this.ID);
                        Cout.println(new StringBuffer(String.valueOf(set)).append(" GUI THONG TIN REQUEST CHAR ").append(getDisplayName()).append(" >> ").append((int) this.ID).toString());
                    }
                } else {
                    GameService.gI().requestMainCharInfo();
                }
                this.timeDelayRequestCharInfo = System.currentTimeMillis();
            }
            isPoinson();
            if (this.delay >= 0) {
                int kk = ((int) (System.currentTimeMillis() - this.timeWait2Board)) / 60000;
                this.delay = this.realTime - kk;
            }
            if (this.lockUpdate > 0) {
                this.lockUpdate--;
                return;
            }
            boolean isCanGoto = false;
            if (this.ID == Canvas.gameScr.mainChar.ID) {
                if (GameScr.autoFight && GameScr.typeOptionFocus == 1 && System.currentTimeMillis() - this.timeGoToLastXY < 0) {
                    this.timeGoToLastXY = System.currentTimeMillis() + 30000;
                    isCanGoto = true;
                }
                if (isCanGoto && Canvas.gameScr.mainChar.posTransRoad == null && GameScr.timeRemovePos <= 0 && (Math.abs(this.x - this.lastXAuto) > 120 || Math.abs(this.y - this.lastYAuto) > 120)) {
                    Canvas.gameScr.findRoad2(this.lastXAuto, this.lastYAuto);
                }
            }
        }
        super.update();
        if (this.realHPSyncTime > 0) {
            this.realHPSyncTime--;
            if (this.realHPSyncTime == 0) {
                if (this.realHP < 0) {
                    this.realHP = 0;
                }
                this.hp = this.realHP;
            }
        }
        if (this.state != 3 && this.realHP <= 0 && this.attack != 0) {
            this.state = (byte) 3;
            this.p3 = (short) 0;
            this.p2 = (short) 0;
            this.p1 = (short) 0;
            Canvas.gameScr.startExplosionAt(this.x, this.y);
        }
        if (this.isAnimal) {
            if (this.effAnimal != null) {
                this.effAnimal.update();
            }
            if ((System.currentTimeMillis() / 1000) - this.timeRemoEffanimal > 0) {
                this.effAnimal = null;
            }
            if (this.myAnimal != null) {
                this.myAnimal.update();
                this.myAnimal.dir = this.dir;
                this.myAnimal.status = this.state;
            }
        }
        switch (this.state) {
            case 0:
                if (this.catagory == 0) {
                    long now = System.currentTimeMillis();
                    if (now - this.stand > 300000 && Canvas.gameScr.mainChar != null && this.ID != Canvas.gameScr.mainChar.ID) {
                        this.wantDestroy = true;
                    }
                }
                if (this.ID == Canvas.gameScr.mainChar.ID && GameScr.saveAutoFight) {
                    GameScr.autoFight = GameScr.saveAutoFight;
                }
                this.p1 = (short) (this.p1 + 1);
                if (getHorse() == -1 || this.anmFly == 0) {
                    if (this.p1 >= STANDFRAME.length) {
                        this.p1 = (short) 0;
                    }
                    this.frame = STANDFRAME[this.p1];
                    this.weapon_frame = this.frame;
                    this.ifHorse = 0;
                } else {
                    if (this.p1 >= (getAttack() > -1 ? RUNFRAME.length : RUNFRAMEWAL.length)) {
                        this.p1 = (short) 0;
                    }
                    if (getHorse() == -1 || GameData.imgHorse == null) {
                        this.frame = RUNFRAMEWAL[this.p1];
                    } else if (this.p1 % 3 == 0) {
                        this.ifHorse = (this.ifHorse + 1) % (this.anmFly == 1 ? 4 : 3);
                    }
                }
                if (this.frame > 3) {
                    this.frame = (byte) 0;
                }
                this.Fhorse = this.frame;
                break;
            case 1:
                int sp = 0;
                if (GameScr.cheat) {
                    sp = 0;
                }
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 >= (getHorse() > -1 ? RUNFRAME.length : RUNFRAMEWAL.length)) {
                    this.p1 = (short) 0;
                }
                if (getHorse() == -1 || GameData.imgHorse == null) {
                    this.frame = RUNFRAMEWAL[this.p1];
                } else if (this.p1 % 3 == 0) {
                    this.ifHorse = (this.ifHorse + 1) % (this.anmFly == 1 ? 4 : 3);
                }
                boolean isMatchX = false;
                boolean isMatchY = false;
                int dx1 = Canvas.abs(this.x - this.xTo);
                int dy1 = Canvas.abs(this.y - this.yTo);
                int spd = this.speed + this.upSpeed + sp;
                // Tăng tốc đuổi kịp CHỈ cho nhân vật khác (nội suy theo gói vị trí máy chủ). Nhân vật chính đi đúng
                // tốc độ máy chủ: tăng tốc ở đây làm bước giữa hai gói vượt ngưỡng máy chủ -> bị trả vị trí cũ.
                if (!isMainchar()) {
                    if (dx1 > 100 || dy1 > 100) {
                        spd = this.speed + this.upSpeed + 4 + sp;
                    } else if (dx1 > 70 || dy1 > 70) {
                        spd = this.speed + this.upSpeed + 3 + sp;
                    } else if (dx1 > 50 || dy1 > 50) {
                        spd = this.speed + this.upSpeed + 2 + sp;
                    } else if (dx1 > 30 || dy1 > 30) {
                        spd = this.speed + this.upSpeed + 1 + sp;
                    }
                }
                if (spd < 1) {
                    spd = 1;
                }
                if (dx1 < spd && dy1 < spd) {
                    this.x = this.xTo;
                    isMatchX = true;
                    this.y = this.yTo;
                    isMatchY = true;
                }
                if (isMatchX && isMatchY) {
                    this.p3 = (short) 0;
                    this.p2 = (short) 0;
                    this.p1 = (short) 0;
                    this.state = (byte) 0;
                    this.justStopFromRun = true;
                    this.stand = System.currentTimeMillis();
                } else {
                    // Đi THẲNG tới điểm đích (hai trục cùng lúc theo tỉ lệ), không đi hình chữ L (trục X hết rồi mới
                    // tới Y) như cũ: chữ L lệch khỏi đường máy chủ thấy và nhìn như giật góc. Hướng mặt theo trục lớn,
                    // có trễ (lệch < 25%) để không lật mặt liên tục khi đi chéo.
                    int sx;
                    int sy;
                    if (dx1 >= dy1) {
                        sx = spd < dx1 ? spd : dx1;
                        sy = (dy1 * sx) / dx1;
                    } else {
                        sy = spd < dy1 ? spd : dy1;
                        sx = (dx1 * sy) / dy1;
                    }
                    boolean ngang = dx1 * 4 >= dy1 * 5;
                    boolean doc = dy1 * 4 >= dx1 * 5;
                    if (!ngang && !doc) {
                        ngang = this.dir == 2 || this.dir == 3;
                    }
                    if (this.x < this.xTo) {
                        this.x = (short) (this.x + sx);
                        if (ngang) {
                            this.dir = (short) 3;
                        }
                    } else if (this.x > this.xTo) {
                        this.x = (short) (this.x - sx);
                        if (ngang) {
                            this.dir = (short) 2;
                        }
                    }
                    if (this.y > this.yTo) {
                        this.y = (short) (this.y - sy);
                        if (!ngang) {
                            this.dir = (short) 1;
                        }
                    } else if (this.y < this.yTo) {
                        this.y = (short) (this.y + sy);
                        if (!ngang) {
                            this.dir = (short) 0;
                        }
                    }
                    if (this.frame > 3) {
                        this.frame = (byte) 0;
                    }
                    this.weapon_frame = this.frame;
                    if (this.ID == Canvas.gameScr.mainChar.ID) {
                        GameScr.autoFight = false;
                    }
                    if (this.idhorse != -1) {
                        this.fh++;
                        if (this.fh > horseMove.length - 1) {
                            this.fh = 0;
                        }
                        this.Fhorse = horseMove[this.fh];
                    }
                }
                break;
            case 2:
                if (this.currentSkillAnimate != null) {
                    this.currentSkillAnimate.updateSkill(this);
                }
                break;
            case 3:
                if (this.hp > 0 && this.realHP > 0) {
                    this.state = (byte) 0;
                }
                this.p1 = (short) (this.p1 + 1);
                if (this.p1 == 40 && this.ID == Canvas.gameScr.mainChar.ID) {
                    Canvas.msgdlg.isIcon = false;
                    this.delay = -1;
                    this.EffFace = null;
                }
                this.isAnimal = false;
                this.myAnimal = null;
                break;
            case 4:
                if (this.comeHome) {
                    long n = System.currentTimeMillis() - this.timeComeHome;
                    if (n > 10000) {
                        resetBuff();
                        GameService.gI().comeHomeWhenDie();
                        this.comeHome = false;
                    }
                    this.time = (int) (n / 1000);
                    this.time = (this.time * 32) / 10;
                    if (this.time > 32) {
                        this.time = 32;
                    }
                }
                break;
        }
        updateHorse();
        updateBuff();
        super.update();
    }

    public boolean isLoadAnimalOk() {
        if (this.myAnimal != null && IsAnimal.img[this.myAnimal.idMonster] != null) {
            return true;
        }
        return false;
    }

    public void resetBuff() {
        this.buff.removeAllElements();
        for (int i = 0; i < this.buffType.length; i++) {
            this.buffType[i] = -1;
            this.lvBuff[i] = 0;
            this.countDown[i] = 0;
        }
    }

    public void paintbuff(Graphics g) {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff cbuf = (CharBuff) this.buff.elementAt(i);
            cbuf.paint(g);
        }
    }

    public int fullSet() {
        if (this.legStyle == -1 || this.bodyStyle == -1) {
            return 0;
        }
        if (this.currentHead == -1) {
            return 2;
        }
        return 1;
    }

    public void paintEff(Graphics g, int x, int y) {
        for (int i = 0; i < 8; i++) {
            Util.paintColumColor(g, x, y, 9, 193, 10, 100, 25, 1, 1, 1, 2, i * 45, 30);
            if (this.idColor == i) {
                Util.paintColumColor(g, x, y, 9, 228, 198, 34, 25, 1, 1, 1, 2, i * 45, 30);
            }
        }
        this.idColor = (this.idColor + 1) % 8;
    }

    @Override
    public boolean isPaint() {
        if (this.x < GameScr.cmx || this.x > GameScr.cmx + Canvas.w || this.y < GameScr.cmy || this.y > GameScr.cmy + Canvas.h + 30) {
            return false;
        }
        return true;
    }

    public byte[] getOrderPaint(int dir, int typeAnimal) {
        boolean paintBehind = dir != 1 && this.frame < 4;
        return ORDER_PAINT[paintBehind ? (char) 0 : (char) 1][dir];
    }

    public boolean canPaint() {
        if (this.bodyStyle == -1 || this.legStyle == -1 || this.currentHead == -1) {
            return false;
        }
        return true;
    }

    public int getIdPartPaint(int type_part) {
        int part = -1;
        if (!isLoadAnimalOk()) {
            if (type_part == 1) {
                if (this.bodyStyle != -1) {
                    part = this.bodyStyle;
                    if (this.idModel[2] != -1) {
                        part = this.idModel[2];
                    }
                }
            } else if (type_part == 0) {
                byte isHorse = getHorse();
                if (this.legStyle != -1 && isHorse == -1) {
                    part = this.legStyle;
                    if (this.idModel[3] != -1) {
                        part = this.idModel[3];
                    }
                }
            } else if (type_part == 2) {
                if (this.currentHead != -1) {
                    part = this.currentHead;
                    if (this.idModel[0] != -1) {
                        part = this.idModel[0];
                    }
                }
            } else if (type_part == 3) {
                if (this.idModel[1] != -1) {
                    this.hatStyle = this.idModel[1];
                }
                if (this.hatStyle != -1) {
                    part = this.hatStyle;
                    if (this.idModel[1] != -1) {
                        part = this.idModel[1];
                    }
                }
            } else if (type_part == 4 && this.coatStyle > -1) {
                part = this.coatStyle;
            }
        }
        return part;
    }

    public int getDyAnimalEff(int type, int dir, int typeAnimal) {
        int dy = 0;
        if (this.idhorse != -1) {
            return getdyParHorse();
        }
        byte isHorse = getHorse();
        if (isHorse != -1) {
            dy = this.z + dyb[isHorse][0];
        }
        return dy;
    }

    public int getDyAnimal(int type, int dir, int typeAnimal) {
        int dy = 0;
        if (this.idhorse != -1) {
            return getdyParHorse();
        }
        byte isHorse = getHorse();
        switch (type) {
            case 0:
                dy = this.z + this.dy;
                break;
            case 1:
                if (isHorse > -1) {
                    if (typeAnimal == 0 || dir >= 2) {
                        dy = this.z + dyb[isHorse][dir] + dyr[isHorse][dir][this.ifHorse] + this.dy;
                    } else {
                        dy = this.z + (dyb[isHorse][dir] - (dir == 1 ? (byte) 5 : (byte) 0)) + dyr[isHorse][dir][this.ifHorse] + this.dy;
                    }
                } else {
                    dy = this.z + this.dy;
                }
                break;
            case 2:
                dy = this.z + (isHorse != -1 ? (dyb[isHorse][dir] - ((dir == 1 && this.anmFly == 1) ? (byte) 5 : (byte) 0)) + dyr[isHorse][dir][this.ifHorse] : 0) + this.dy;
                break;
            case 3:
                dy = ((this.z + (isHorse != -1 ? dyb[isHorse][dir] + dyr[isHorse][dir][this.ifHorse] : 0)) - ((dir == 1 && this.anmFly == 1) ? 5 : 0)) + this.dy;
                break;
            case 4:
                dy = this.z + (isHorse != -1 ? dyb[isHorse][dir] + dyr[isHorse][dir][this.ifHorse] : 0) + this.dy;
                break;
        }
        return dy;
    }

    public int getdxHorse() {
        if (this.typehorse != -1) {
            return dx_horse[this.typehorse][this.dir];
        }
        return 0;
    }

    public int getDxAnimal(int type, int dir, int typeAnimal) {
        int dx = 0;
        if (this.idhorse != -1) {
            return getdxHorse();
        }
        byte isHorse = getHorse();
        switch (type) {
            case 1:
                if (isHorse > -1 && dir > 1) {
                    dx = dxr[isHorse][dir][this.ifHorse];
                }
                break;
            case 2:
                dx = isHorse != -1 ? dxr[isHorse][dir][this.ifHorse] : (byte) 0;
                break;
            case 3:
                dx = isHorse != -1 ? dxr[isHorse][dir][this.ifHorse] : (byte) 0;
                break;
            case 4:
                dx = isHorse != -1 ? dxr[isHorse][dir][this.ifHorse] : (byte) 0;
                break;
        }
        return dx;
    }

    public int getFrameHorse() {
        if (this.dir == 0) {
            return this.Fhorse + (this.frameThuCuoi * 21);
        }
        if (this.dir == 1) {
            return this.Fhorse + 7 + (this.frameThuCuoi * 21);
        }
        return this.Fhorse + 14 + (this.frameThuCuoi * 21);
    }

    @Override
    public void paint(Graphics g) {
        this.infoPaint[2] = this.currentHead;
        if (!isPaint()) {
            return;
        }
        boolean ishorseEff = false;
        if (this.idhorse > -1 || this.useHorse != -1) {
            ishorseEff = true;
        }
        try {
            // pokemon: than Pokemon ve nguyen con, bo qua paper doll, vu khi, ngua, phi phong (Q5)
            if (!this.isFreeze && !paintPokemon(g)) {
                byte isHorse = getHorse();
                if (GameData.imgHorse == null) {
                    isHorse = -1;
                }
                if (this.effPhap != null) {
                    this.effPhap.x = this.x;
                    this.effPhap.y = this.y;
                    if (!isLoadAnimalOk() && isHorse != -1) {
                        this.effPhap.x += dxs[isHorse][this.dir];
                        this.effPhap.y -= this.anmFly == 0 ? 3 : -6;
                    }
                    this.effPhap.paint(g);
                }
                if (this.state == 3) {
                    g.drawImage(Res.imgDie, this.x - 10, (this.y - 27) + this.dy, 0);
                    if (Canvas.gameTick % 20 != 17 && Canvas.gameTick % 20 != 13) {
                        g.drawImage(Res.imgEye, this.x - 4, (this.y - 19) + this.dy, 0);
                        return;
                    }
                    return;
                }
                paintDataEff_Top(g, this.x, (this.y - (this.anmFly == 0 ? (short) 0 : (short) 6)) + getdyParHorse(), ishorseEff);
                paintListEff(g, 0);
                boolean z = (this.dir == 0 || this.dir == 2 || this.dir == 3) && this.frame < 4;
                int f = 0;
                int fw = 0;
                if (!isLoadAnimalOk() && isHorse != -1) {
                    this.z = 0;
                    if (this.anmFly == 1) {
                        byte[] temp = {-1, -1, -5, -1};
                        f = temp[this.ifHorse];
                        byte[] temp2 = {-1, -2, -3, -2};
                        fw = temp2[this.ifHorse];
                    }
                }
                byte[] orderpaint = getOrderPaint(this.dir, this.anmFly);
                if (!isLoadAnimalOk() && can_Paint()) {
                    for (int i = 0; i < orderpaint.length; i++) {
                        if (getIdPartPaint(orderpaint[i]) > -1 || orderpaint[i] == 6 || ((orderpaint[i] == 4 && this.idPhiPhong != -1) || ((orderpaint[i] == 5 && this.infoPaint[orderpaint[i]] > -1) || this.idWeapon != -1))) {
                            if (orderpaint[i] == 5) {
                                paintWeapon(g, fw);
                            } else {
                                if (orderpaint[i] == 0 && this.dynamicEffBottom != null) {
                                    for (int ii = 0; ii < this.dynamicEffBottom.size(); ii++) {
                                        ((DynamicEffect) this.dynamicEffBottom.elementAt(ii)).paint(g, this.x, this.y);
                                    }
                                }
                                if (orderpaint[i] != 0 || this.idhorse == -1) {
                                    if (orderpaint[i] == 6 && this.idhorse != -1) {
                                        paintMount(g, f);
                                        if (isHorse > -1) {
                                            g.drawImage(Res.imgShadow, this.x + dxs[isHorse][this.dir], this.y - (this.anmFly == 0 ? (short) 3 : (short) -6), 3);
                                        }
                                    } else if (orderpaint[i] == 2 && this.idHair != -1) {
                                        paintHair(g, f);
                                    } else if (orderpaint[i] == 0 && this.idLeg != -1 && isHorse < 0) {
                                        paintLeg(g, fw);
                                    } else if (orderpaint[i] == 4 && this.idPhiPhong != -1) {
                                        paintPP(g, fw);
                                    } else if (orderpaint[i] == 1) {
                                        DataSkillEff partHorse = getPartEff(this.idhorse);
                                        if (partHorse != null) {
                                            if (this.idBody == -1) {
                                                Res.getCharPartInfo(orderpaint[i], getIdPartPaint(orderpaint[i])).paint(g, this.x + getDxAnimal(orderpaint[i], this.dir, this.anmFly), f + this.y + getDyAnimal(orderpaint[i], this.dir, this.anmFly), this.dir, this.frame);
                                            } else {
                                                paintBody(g, fw);
                                            }
                                        }
                                        if (isHorse > -1) {
                                            if (this.dir < 2 && this.anmFly == 0) {
                                                if (this.idBody == -1) {
                                                    Res.getCharPartInfo(orderpaint[i], getIdPartPaint(orderpaint[i])).paint(g, this.x + getDxAnimal(orderpaint[i], this.dir, this.anmFly), f + this.y + getDyAnimal(orderpaint[i], this.dir, this.anmFly), this.dir, this.frame);
                                                } else {
                                                    paintBody(g, fw);
                                                }
                                            }
                                            if (this.dynamicEffBottom != null) {
                                                for (int ii2 = 0; ii2 < this.dynamicEffBottom.size(); ii2++) {
                                                    ((DynamicEffect) this.dynamicEffBottom.elementAt(ii2)).paint(g, this.x, this.y);
                                                }
                                            }
                                            if (GameData.imgHorse.size() > 0) {
                                                g.drawRegion((Image) GameData.imgHorse.elementAt(this.idImgHorse), fHorse[isHorse][this.dir][this.ifHorse], 0, sizeHorse[isHorse][this.dir][this.ifHorse], hHorse[isHorse], 0, this.x + dxh[isHorse][this.dir], this.y + dyh[isHorse][this.dir] + this.dy, 33);
                                            }
                                            if (this.dir > 1 && this.anmFly == 0) {
                                                g.drawImage(Res.imgLeg[this.dir - 2], (this.x - (this.dir == 2 ? (short) -2 : (short) 1)) + dxr[isHorse][this.dir][this.ifHorse], (this.y - 10) + dyr[isHorse][this.dir][this.ifHorse] + this.dy, 3);
                                            }
                                            if ((this.dir > 1 && this.anmFly == 0) || this.anmFly == 1) {
                                                if (this.idBody == -1) {
                                                    Res.getCharPartInfo(orderpaint[i], getIdPartPaint(orderpaint[i])).paint(g, this.x + getDxAnimal(orderpaint[i], this.dir, this.anmFly), f + this.y + getDyAnimal(orderpaint[i], this.dir, this.anmFly), this.dir, this.frame);
                                                } else {
                                                    paintBody(g, fw);
                                                }
                                            }
                                        } else if (this.idhorse == -1) {
                                            if (this.idBody == -1) {
                                                Res.getCharPartInfo(orderpaint[i], getIdPartPaint(orderpaint[i])).paint(g, this.x + getDxAnimal(orderpaint[i], this.dir, this.anmFly), f + this.y + getDyAnimal(orderpaint[i], this.dir, this.anmFly), this.dir, this.frame);
                                            } else {
                                                paintBody(g, fw);
                                            }
                                        }
                                    } else {
                                        boolean ph = true;
                                        if (orderpaint[i] == 3) {
                                            ph = this.paintHat;
                                        }
                                        if (ph) {
                                            Res.getCharPartInfo(orderpaint[i], getIdPartPaint(orderpaint[i])).paint(g, this.x + getDxAnimal(orderpaint[i], this.dir, this.anmFly), f + this.y + getDyAnimal(orderpaint[i], this.dir, this.anmFly), this.dir, this.frame);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoadAnimalOk() && this.myAnimal != null) {
                    this.myAnimal.paint(g, this.x, this.y + this.z + this.dy);
                    g.drawImage(Res.imgShadow, this.x, this.y - 2, 3);
                }
                if (this.isAnimal && this.effAnimal != null) {
                    this.effAnimal.x = this.x;
                    this.effAnimal.y = this.y;
                    this.effAnimal.paint(g);
                }
                if (this.comeHome) {
                    g.setColor(0);
                    g.fillRect(this.x - 16, this.y - 46, 32, 4);
                    g.setColor(1165236);
                    g.fillRect(this.x - 16, this.y - 46, this.time, 4);
                    g.setColor(3363085);
                    g.drawRect(this.x - 16, this.y - 46, 32, 4);
                }
                paintbuff(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(new StringBuffer("paint err ").append(e).toString());
        }
        super.paint(g);
        if (this.EffFace != null && !this.isFreeze) {
            this.EffFace.paint(g);
        }
        if (isNPC()) {
            Image tempImg = GameScr.getImgQuest(this.idBoss);
            if (!tempImg.equals(Res.imgSelect)) {
                g.drawImage(tempImg, this.x, (this.y - this.height) - 5, 33);
            }
        }
        if (this.dynamicEffTop != null) {
            for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                ((DynamicEffect) this.dynamicEffTop.elementAt(i2)).paint(g, this.x, this.y);
            }
        }
        paintDataEff_Bot(g, this.x, (this.y - (this.anmFly == 0 ? (short) 0 : (short) 6)) + getdyParHorse(), ishorseEff);
        paintListEff(g, 1);
    }

    /** Trang thai client -> dong tac PokeBody (bang 4.3). Class 2 (Eevee) va 4 (Pichu) danh xa. */
    public int pokeDongTac() {
        if (this.state == 3) {
            return PokeDuLieu.GUC_NGA;
        }
        if (this.state == 2) {
            return (this.clazz == 2 || this.clazz == 4) ? PokeDuLieu.DANH_XA : PokeDuLieu.DANH_GAN;
        }
        if (pokeDangTrungDon()) {
            return PokeDuLieu.TRUNG_DON;
        }
        if (this.state == 1) {
            return PokeDuLieu.CHAY;
        }
        return PokeDuLieu.DUNG;
    }

    /** Bat dau hieu ung tien hoa (lenh -92). */
    public void batDauTienHoa(short dexCu, short dexMoi) {
        this.pokeDexTruoc = dexCu;
        this.pokeTienHoaCon = POKE_TICK_TIEN_HOA;
        if (dexMoi > 0) {
            this.pokeDex = dexMoi;
        }
    }

    /** Ve than Pokemon thay paper doll; false (ve kieu cu) khi chua co dex hoac thieu sprite. */
    public boolean paintPokemon(Graphics g) {
        if (this.pokeDex <= 0 || !PokeBody.coSprite(this.pokeDex, this.pokeShiny)) {
            return false;
        }
        int dexVe = this.pokeDex;
        int doSang = 0;
        if (this.pokeTienHoaCon > 0) {
            if (this.pokeTienHoaCon > 5) {
                // nhap nhay giua dang cu va dang moi, cang ve cuoi cang nhanh, than sang trang
                int daQua = POKE_TICK_TIEN_HOA - this.pokeTienHoaCon;
                int chuKy = 1 + this.pokeTienHoaCon / 10;
                if (this.pokeDexTruoc > 0 && (daQua / chuKy) % 2 == 0) {
                    dexVe = this.pokeDexTruoc;
                }
                doSang = 100;
            } else {
                doSang = this.pokeTienHoaCon * 20;
            }
            if (!PokeBody.coSprite(dexVe, this.pokeShiny)) {
                dexVe = this.pokeDex;
            }
        }
        int dongTac = pokeDongTac();
        int tick = pokeTickTrongDongTac(dongTac);
        // ten, icon nhiem vu, bong chat dat tai y - height: nang len theo chieu cao sprite
        int cao = PokeBody.caoTrenChan(this.pokeDex, this.pokeShiny, 100) + 6;
        this.height = (short) (cao > 32 ? cao : 32);
        if (this.effPhap != null) {
            this.effPhap.x = this.x;
            this.effPhap.y = this.y;
            this.effPhap.paint(g);
        }
        if (this.state != 3) {
            paintDataEff_Top(g, this.x, this.y, false);
            paintListEff(g, 0);
        }
        game.pokemon.BatPokemon.veBanDongHanh(g, this, tick); // 0.9.8: Pokémon Hộp PC đi theo (sau lưng, chỉ hình)
        g.drawImage(Res.imgShadow, this.x, this.y - 2, 3);
        int yNguoi = this.y + this.z + this.dy;
        if (this.pokeThuDex > 0 && this.state != 3 && PokeBody.coSprite(this.pokeThuDex, this.pokeThuShiny)) {
            // thu cuoi: ve thu (dung/chay theo nguoi choi) roi nang nguoi choi len ~55% chieu cao thu
            int dongTacThu = this.state == 1 ? PokeDuLieu.CHAY : PokeDuLieu.DUNG;
            PokeBody.paint(g, this.pokeThuDex, this.pokeThuShiny, this.x, yNguoi, this.dir, dongTacThu, tick, 100);
            int caoThu = PokeBody.caoTrenChan(this.pokeThuDex, this.pokeThuShiny, 100);
            yNguoi -= caoThu * 55 / 100;
            this.height = (short) (this.height + caoThu * 55 / 100);
        }
        // pokemon: chop trang + lac nhe khi trung don (HieuUngDanh)
        // ngoai hinh trang bi: lop sau (vong sang, vien, ao choang) -> than (trang phuc) -> lop truoc (mu, khan...)
        int xThan = this.x + pokeLac();
        boolean coNgoaiHinh = this.ngoaiHinh != null && this.state != 3 && dexVe == this.pokeDex
                && (game.pokemon.NgoaiHinhVe.hien || this == Canvas.gameScr.mainChar);
        if (coNgoaiHinh) {
            game.pokemon.NgoaiHinhVe.veSau(g, this.ngoaiHinh, dexVe, this.pokeShiny, xThan, yNguoi, this.y, this.dir, dongTac, tick);
        }
        PokeBody.paint(g, dexVe, this.pokeShiny, xThan, yNguoi, this.dir, dongTac, tick, 100, doSang == 0 && pokeChop() ? 230 : doSang,
                coNgoaiHinh ? this.ngoaiHinh.trangPhuc : 0);
        if (coNgoaiHinh) {
            game.pokemon.NgoaiHinhVe.veTruoc(g, this.ngoaiHinh, dexVe, this.pokeShiny, xThan, yNguoi, this.dir, dongTac, tick);
        }
        if (this.comeHome) {
            g.setColor(0);
            g.fillRect(this.x - 16, this.y - 46, 32, 4);
            g.setColor(1165236);
            g.fillRect(this.x - 16, this.y - 46, this.time, 4);
            g.setColor(3363085);
            g.drawRect(this.x - 16, this.y - 46, 32, 4);
        }
        paintbuff(g);
        return true;
    }

    /** Ve Pokemon dang dung yen tai (x, y) = diem chan (man tao/chon nhan vat, bang trang bi). */
    public boolean paintPokemonTinh(Graphics g, int xp, int yp) {
        if (this.pokeDex <= 0) {
            return false;
        }
        return paintPokemonTinh(g, xp, yp, DOWN);
    }

    /** Nhu tren, them huong (the trang bi xoay Pokemon) + ngoai hinh trang bi. */
    public boolean paintPokemonTinh(Graphics g, int xp, int yp, int huong) {
        if (this.pokeDex <= 0) {
            return false;
        }
        int t = Canvas.gameTick;
        game.pokemon.NgoaiHinhCua n = this.ngoaiHinh;
        if (n != null) {
            game.pokemon.NgoaiHinhVe.veSau(g, n, this.pokeDex, this.pokeShiny, xp, yp, yp, huong, PokeDuLieu.DUNG, t);
        }
        boolean ok = PokeBody.paint(g, this.pokeDex, this.pokeShiny, xp, yp, huong, PokeDuLieu.DUNG, t, 100, 0,
                n != null ? n.trangPhuc : 0);
        if (ok && n != null) {
            game.pokemon.NgoaiHinhVe.veTruoc(g, n, this.pokeDex, this.pokeShiny, xp, yp, huong, PokeDuLieu.DUNG, t);
        }
        return ok;
    }

    public void paintCharList(Graphics g) {
        int i;
        if (!this.isFreeze) {
            byte isHorse = getHorse();
            if (GameData.imgHorse == null) {
                isHorse = -1;
            }
            if (this.effPhap != null) {
                this.effPhap.x = this.x;
                this.effPhap.y = this.y;
                if (!isLoadAnimalOk() && isHorse != -1) {
                    this.effPhap.x += dxs[isHorse][this.dir];
                    this.effPhap.y -= this.anmFly == 0 ? 3 : -6;
                }
                this.effPhap.paint(g);
            }
            if (this.state == 3) {
                g.drawImage(Res.imgDie, this.x - 10, (this.y - 27) + this.dy, 0);
                if (Canvas.gameTick % 20 == 17 || Canvas.gameTick % 20 == 13) {
                    return;
                }
                g.drawImage(Res.imgEye, this.x - 4, (this.y - 19) + this.dy, 0);
                return;
            }
            boolean paintWPBack = (this.dir == 0 || this.dir == 2 || this.dir == 3) && this.frame < 4;
            int f = 0;
            int fw = 0;
            if (!isLoadAnimalOk() && isHorse != -1) {
                this.z = 0;
                if (this.anmFly == 1) {
                    byte[] temp = {-1, -1, -5, -1};
                    f = temp[this.ifHorse];
                    byte[] temp2 = {-1, -2, -3, -2};
                    fw = temp2[this.ifHorse];
                }
            }
            if (!isLoadAnimalOk() && paintWPBack) {
                paintWeapon(g, fw);
            }
            if (!isLoadAnimalOk() && this.legStyle != -1 && isHorse == -1) {
                int idLeg = this.legStyle;
                if (this.idModel[3] != -1) {
                    idLeg = this.idModel[3];
                }
                Res.getCharPartInfo(0, idLeg).paint(g, this.x, this.y + this.z + this.dy, this.dir, this.frame);
            }
            if (!isLoadAnimalOk() && this.bodyStyle != -1) {
                int body = this.bodyStyle;
                if (this.idModel[2] != -1) {
                    body = this.idModel[2];
                }
                if (isHorse != -1) {
                    if (this.dir < 2 && this.anmFly == 0) {
                        Res.getCharPartInfo(1, body).paint(g, this.x, f + this.y + this.z + dyb[isHorse][this.dir] + dyr[isHorse][this.dir][this.ifHorse] + this.dy, this.dir, this.frame);
                    }
                    g.drawImage(Res.imgShadow, this.x + dxs[isHorse][this.dir], this.y - (this.anmFly == 0 ? (short) 3 : (short) -6), 3);
                    if (GameData.imgHorse.size() > 0) {
                        g.drawRegion((Image) GameData.imgHorse.elementAt(this.idImgHorse), fHorse[isHorse][this.dir][this.ifHorse], 0, sizeHorse[isHorse][this.dir][this.ifHorse], hHorse[isHorse], 0, this.x + dxh[isHorse][this.dir], this.y + dyh[isHorse][this.dir] + this.dy, 33);
                    }
                    if (this.dir > 1 && this.anmFly == 0) {
                        g.drawImage(Res.imgLeg[this.dir - 2], (this.x - (this.dir == 2 ? (short) -2 : (short) 1)) + dxr[isHorse][this.dir][this.ifHorse], (this.y - 10) + dyr[isHorse][this.dir][this.ifHorse] + this.dy, 3);
                    }
                    if (this.dir > 1 && this.anmFly == 0) {
                        Res.getCharPartInfo(1, body).paint(g, this.x + dxr[isHorse][this.dir][this.ifHorse], f + this.y + this.z + dyb[isHorse][this.dir] + dyr[isHorse][this.dir][this.ifHorse] + this.dy, this.dir, this.frame);
                    } else if (this.anmFly == 1) {
                        if (this.dir < 2) {
                            Res.getCharPartInfo(1, body).paint(g, this.x, f + this.y + this.z + (dyb[isHorse][this.dir] - ((this.dir == 1 && this.anmFly == 1) ? (byte) 5 : (byte) 0)) + dyr[isHorse][this.dir][this.ifHorse] + this.dy, this.dir, this.frame);
                        } else {
                            Res.getCharPartInfo(1, body).paint(g, this.x + dxr[isHorse][this.dir][this.ifHorse], f + this.y + this.z + dyb[isHorse][this.dir] + dyr[isHorse][this.dir][this.ifHorse] + this.dy, this.dir, this.frame);
                        }
                    }
                } else if (!isLoadAnimalOk()) {
                    Res.getCharPartInfo(1, body).paint(g, this.x, f + this.y + this.z + this.dy, this.dir, this.frame);
                }
            }
            if (!isLoadAnimalOk() && this.currentHead != -1) {
                int head = this.currentHead;
                if (this.idModel[0] != -1) {
                    head = this.idModel[0];
                }
                CharPartInfo charPartInfo = Res.getCharPartInfo(2, head);
                int i2 = this.x + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0);
                int i3 = f + this.y + this.z;
                if (isHorse != -1) {
                    i = (dyb[isHorse][this.dir] - ((this.dir == 1 && this.anmFly == 1) ? (byte) 5 : (byte) 0)) + dyr[isHorse][this.dir][this.ifHorse];
                } else {
                    i = 0;
                }
                charPartInfo.paint(g, i2, i3 + i + this.dy, this.dir, this.frame);
            }
            if (!isLoadAnimalOk()) {
                if (this.idModel[1] != -1) {
                    this.hatStyle = this.idModel[1];
                }
                if (this.hatStyle != -1) {
                    int hat = this.hatStyle;
                    if (this.idModel[1] != -1) {
                        hat = this.idModel[1];
                    }
                    if ((this.idModel[0] == 26 && this.idModel[1] == 50) || this.idModel[0] != 26) {
                        Res.getCharPartInfo(3, hat).paint(g, this.x + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0), f + this.y + this.z + (isHorse != -1 ? dyb[isHorse][this.dir] + dyr[isHorse][this.dir][this.ifHorse] : 0) + this.dy, this.dir, this.frame);
                    }
                }
            }
            if (!isLoadAnimalOk() && !paintWPBack) {
                paintWeapon(g, fw);
            }
            if (isLoadAnimalOk() && this.myAnimal != null) {
                this.myAnimal.paint(g, this.x, this.y + this.z + this.dy);
                g.drawImage(Res.imgShadow, this.x, this.y - 2, 3);
            }
            if (this.isAnimal && this.effAnimal != null) {
                this.effAnimal.x = this.x;
                this.effAnimal.y = this.y;
                this.effAnimal.paint(g);
            }
            if (this.comeHome) {
                g.setColor(0);
                g.fillRect(this.x - 16, this.y - 46, 32, 4);
                g.setColor(1165236);
                g.fillRect(this.x - 16, this.y - 46, this.time, 4);
                g.setColor(3363085);
                g.drawRect(this.x - 16, this.y - 46, 32, 4);
            }
            paintbuff(g);
        }
        super.paint(g);
        if (this.EffFace == null || this.isFreeze) {
            return;
        }
        this.EffFace.paint(g);
    }

    @Override
    public void paintName(Graphics g) {
        if (Tilemap.isOfflineMap) {
            return;
        }
        if (this.name != null && Canvas.gameScr.hideGUI != 2) {
            // pokemon: hang ten dat sat tren DINH HINH THAT (ban goc co dinh y - 40 cho nguoi ~32 px)
            int dyTen = dyHangTen();
            int bb = -((getHorse() == -1 || GameData.imgHorse == null) ? 2 : 10);
            int aa = 0;
            if (this.idhorse != -1) {
                bb = dy_horse[0][this.dir];
            }
            if (this.idClan != -1) {
                aa = 8;
                ImageIcon img = GameData.getImgIcon((short) (this.idClan + 1000));
                int wImg = 0;
                if (img != null) {
                    if (!img.isLoad) {
                        g.drawImage(img.img, (this.x - (this.wName / 2)) - 6, ((this.y - 32 + dyTen) - 6) + bb, 3);
                        wImg = img.img.getWidth();
                    }
                    if (this.isMaster >= 0 && this.isMaster < 3) {
                        int[][] color = {new int[]{16450576, 14548228}, new int[]{14548228, 16450576}, new int[]{1049853, 16450576}};
                        Res.paintRectColor(g, ((this.x - (this.wName / 2)) - (wImg / 2)) - 8, ((((this.y - 32 + dyTen) - 6) + bb) - (wImg / 2)) - 1, wImg + 2, wImg + 2, count, color[this.isMaster][0], color[this.isMaster][1]);
                        count += 3;
                        if (count > 46) {
                            count = 0;
                        }
                    }
                } else if (this.isMaster >= 0 && this.isMaster < 3) {
                    int[][] color2 = {new int[]{16450576, 14548228}, new int[]{14548228, 16450576}, new int[]{1049853, 16450576}};
                    Res.paintRectColor(g, (this.x - (this.wName / 2)) - 8, (((this.y - 32 + dyTen) - 6) + bb) - 1, 10, 10, count, color2[this.isMaster][0], color2[this.isMaster][1]);
                    count += 3;
                    if (count > 46) {
                        count = 0;
                    }
                }
            }
            if (this.nationID > -1 && this.idBoss == -1 && ((this.nationID != this.inCountry || this.ID == Canvas.gameScr.mainChar.ID) && (this.nationID != this.inCountry || this.ID == Canvas.gameScr.mainChar.ID))) {
                g.drawRegion(Res.imgNation, 0, this.nationID * 11, 11, 11, 0, this.x + (this.wName / 2) + 9, ((this.y - 32 + dyTen) - 12) + bb, 20);
            }
            int dhSinhThai = this.danhHieuTen != null ? -1 : game.pokemon.SinhThai.danhHieuCua(this.listEffect);
            if (this.danhHieuTen != null && !game.pokemon.NhanTheGioi.bat()) {
                // 0.9.7 danh hiệu (nhãn chữ màu độ hiếm) - máy J2ME vẽ ngay trên tên trong lớp thế giới
                game.pokemon.NhanTheGioi.danhHieuJ2me(g, this.danhHieuTen, this.danhHieuHiem, this.x,
                        ((this.y - 42 + dyTen) + bb) - this.dyThanthu);
            }
            if (dhSinhThai >= 0) {
                game.pokemon.SinhThai.veDanhHieu(g, this.x, ((this.y - 42 + dyTen) + bb) - this.dyThanthu, dhSinhThai);
            }
            // 0.9.5: tên ở lớp giao diện (game.pokemon.NhanTheGioi: chữ nhỏ viền tối, chống chồng); J2ME vẽ kiểu cũ
            MainChar mcTen = Canvas.gameScr.mainChar;
            int loaiTen = this.ID == mcTen.ID ? game.pokemon.NhanTheGioi.BAN_THAN : this.idBoss != -1 ? game.pokemon.NhanTheGioi.TRUM
                    : this.nationID == mcTen.nationID ? game.pokemon.NhanTheGioi.NGUOI : game.pokemon.NhanTheGioi.KHAC_PHE;
            if (game.pokemon.NhanTheGioi.nguoi(this, this.name, this.x + aa, ((this.y - 31 + dyTen) + bb) - this.dyThanthu, this.y,
                    loaiTen, Canvas.gameScr.focusedActor == this)) {
                // đã ghi
            } else if (this.ID == Canvas.gameScr.mainChar.ID || this.nationID == Canvas.gameScr.mainChar.nationID || this.idBoss != -1) {
                Font.smallFont.drawString(g, this.name, this.x + aa, ((this.y - 40 + dyTen) + bb) - this.dyThanthu, 2);
            } else {
                Font.smallFontColor[4].drawString(g, this.name, this.x + aa, ((this.y - 43 + dyTen) + bb) - this.dyThanthu, 2);
            }
            int cc = 0;
            int dd = 0;
            if (this.isNo) {
                dd = 5;
            }
            if (this.pk > 0) {
                cc = -5;
                g.drawRegion(imgFlgPk, 0, (this.pk - 14) * 10, 11, 10, 0, this.x + dd, (this.y - 41 + dyTen) + bb, 33);
            } else if (this.killer > 0 || this.iskiller) {
                cc = -5;
                g.drawRegion(imgFlgPk, 0, 50, 11, 10, 0, this.x + dd, (this.y - 41 + dyTen) + bb, 33);
            }
            if (this.isNo) {
                Res.imgNo.drawFrame(this.countNo / 5, this.x + cc, ((this.y - 41 + dyTen) + bb) - 2, 0, 33, g);
            }
            this.countNo = (byte) (this.countNo + 1);
            if (this.countNo >= 10) {
                this.countNo = (byte) 0;
            }
        }
        super.paintName(g);
    }

    /**
     * pokemon: do lech (px the gioi, duong = xuong) cua hang ten / huy hieu so voi vi tri goc y - 40 (nguoi
     * ~32 px). Than Pokemon: ten cach dinh hinh that 9 px (cong phan nang len khi cuoi thu).
     */
    public int dyHangTen() {
        if (this.pokeDex <= 0 || !PokeBody.coSprite(this.pokeDex, this.pokeShiny)) {
            return 0;
        }
        int cao = PokeBody.caoHinh(this.pokeDex, this.pokeShiny);
        if (cao <= 0) {
            return 0;
        }
        if (this.pokeThuDex > 0 && this.state != 3 && PokeBody.coSprite(this.pokeThuDex, this.pokeThuShiny)) {
            cao += PokeBody.caoTrenChan(this.pokeThuDex, this.pokeThuShiny, 100) * 55 / 100;
        }
        return tinhDyTen(cao);
    }

    /** Lech hang ten cho hinh cao caoHinh px tren chan (ten cach dinh 9 px, khong thap hon dinh 14 px). */
    public static int tinhDyTen(int caoHinh) {
        int dau = caoHinh + 9;
        if (dau < 14) {
            dau = 14;
        }
        return 40 - dau;
    }

    public int getdyParHorse() {
        if (this.typehorse != -1) {
            return dy_horse[this.typehorse][this.dir];
        }
        return 0;
    }

    public int getFrameBody() {
        if (this.dir == 0) {
            return this.frame + (this.Framebody * 18);
        }
        if (this.dir == 1) {
            return (this.dir * 6) + this.frame + (this.Framebody * 18);
        }
        return this.frame + 12 + (this.Framebody * 18);
    }

    public int getFrameLeg() {
        if (this.dir == 0) {
            return this.frame + (this.frameLeg * 18);
        }
        if (this.dir == 1) {
            return (this.dir * 6) + this.frame + (this.frameLeg * 18);
        }
        return this.frame + 12 + (this.frameLeg * 18);
    }

    public int getFramePP() {
        if (this.dir == 0) {
            return this.frame + (this.framePP * 18);
        }
        if (this.dir == 1) {
            return (this.dir * 6) + this.frame + (this.framePP * 18);
        }
        return this.frame + 12 + (this.framePP * 18);
    }

    public int getFrameHair() {
        if (this.dir == 0) {
            return this.frame + (this.FrameHair * 18);
        }
        if (this.dir == 1) {
            return (this.dir * 6) + this.frame + (this.FrameHair * 18);
        }
        return this.frame + 12 + (this.FrameHair * 18);
    }

    public int getFrameWP() {
        if (this.dir == 0) {
            return this.frame + (this.FrameWP * 18);
        }
        if (this.dir == 1) {
            return (this.dir * 6) + this.frame + (this.FrameWP * 18);
        }
        return this.frame + 12 + (this.FrameWP * 18);
    }

    public void paintVK(Graphics g, int f) {
        byte isHorse = getHorse();
        if (GameData.imgHorse == null) {
            isHorse = -1;
        }
        DataSkillEff part_Vk = getPartEff(this.idWeapon);
        if (part_Vk != null && Canvas.gameTick % 5 == 0) {
            int ms = part_Vk.listFrame.size() / 18;
            if (ms == 0) {
                ms = 1;
            }
            this.FrameWP = (byte) ((this.FrameWP + 1) % ms);
        }
        if (part_Vk != null) {
            part_Vk.paintTopHorse(g, this.x + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), this.y + (isHorse != -1 ? dyb[isHorse][this.dir] : (byte) 0) + getdyParHorse() + f, getFrameWP(), this.dir == 3 ? 2 : 0);
            part_Vk.paintBottomHorse(g, this.x + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), this.y + (isHorse != -1 ? dyb[isHorse][this.dir] : (byte) 0) + getdyParHorse() + f, getFrameWP(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintHair(Graphics g, int f) {
        getHorse();
        if (GameData.imgHorse == null) {
        }
        DataSkillEff parhair = getPartEff(this.idHair);
        if (parhair != null && Canvas.gameTick % 6 == 0) {
            int ms = parhair.listFrame.size() / 18;
            if (ms == 0) {
                ms = 1;
            }
            this.FrameHair = (byte) ((this.FrameHair + 1) % ms);
        }
        if (parhair != null) {
            parhair.paintTopHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(2, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(2, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameHair(), this.dir == 3 ? 2 : 0);
            parhair.paintBottomHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(2, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(2, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameHair(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintMount(Graphics g, int f) {
        DataSkillEff partHorse = getPartEff(this.idhorse);
        if (partHorse != null) {
            partHorse.paintTopHorse(g, this.x, this.y, getFrameHorse(), this.dir == 3 ? 2 : 0);
            partHorse.paintBottomHorse(g, this.x, this.y, getFrameHorse(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintBody(Graphics g, int f) {
        getHorse();
        if (GameData.imgHorse == null) {
        }
        DataSkillEff parbody = getPartEff(this.idBody);
        if (parbody != null && Canvas.gameTick % 6 == 0) {
            int ms = parbody.listFrame.size() / 18;
            if (ms == 0) {
                ms = 1;
            }
            this.Framebody = (byte) ((this.Framebody + 1) % ms);
        }
        if (parbody != null) {
            parbody.paintTopHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(1, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(1, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameBody(), this.dir == 3 ? 2 : 0);
            parbody.paintBottomHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(1, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(1, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameBody(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintLeg(Graphics g, int f) {
        getHorse();
        if (GameData.imgHorse == null) {
        }
        DataSkillEff parleg = getPartEff(this.idLeg);
        if (parleg != null && Canvas.gameTick % 6 == 0) {
            int ms = parleg.listFrame.size() / 18;
            if (ms == 0) {
                ms = 1;
            }
            this.frameLeg = (byte) ((this.frameLeg + 1) % ms);
        }
        if (parleg != null) {
            parleg.paintTopHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(0, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(0, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameLeg(), this.dir == 3 ? 2 : 0);
            parleg.paintBottomHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(0, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(0, this.dir, this.anmFly)) + getdyParHorse() + f, getFrameLeg(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintPP(Graphics g, int f) {
        getHorse();
        if (GameData.imgHorse == null) {
        }
        DataSkillEff parleg = getPartEff(this.idPhiPhong);
        if (parleg != null && Canvas.gameTick % 10 == 0) {
            int ms = parleg.listFrame.size() / 18;
            if (ms == 0) {
                ms = 1;
            }
            this.framePP = (byte) ((this.framePP + 1) % ms);
        }
        if (parleg != null) {
            parleg.paintTopHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(4, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(4, this.dir, this.anmFly)) + getdyParHorse() + f, getFramePP(), this.dir == 3 ? 2 : 0);
            parleg.paintBottomHorse(g, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(4, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(4, this.dir, this.anmFly)) + getdyParHorse() + f, getFramePP(), this.dir == 3 ? 2 : 0);
        }
    }

    private void paintWeapon(Graphics g, int f) {
        if (this.idWeapon != -1) {
            paintVK(g, f);
            return;
        }
        int currentWP = this.weaponType - 3;
        if (currentWP < 0 || currentWP >= 5) {
            return;
        }
        byte isHorse = getHorse();
        if (GameData.imgHorse == null) {
            isHorse = -1;
        }
        int claz = this.clazz;
        int weaIndex = this.weaponIndex;
        if (this.idModel[4] > -1) {
            currentWP = 1;
            weaIndex = 0;
            claz = 1;
        }
        if (this.isDoing) {
            claz = 3;
            weaIndex = 0;
            currentWP = 3;
        }
        int currentSkillType = skill_type[claz][this.currentSkillType];
        if (this.state != 2) {
            currentSkillType = 0;
        }
        if (this.weapon_frame >= 0 && currentWP >= 0) {
            WPSplashInfo wp = Res.wpSplashInfos[currentWP][currentSkillType];
            if (wp == null) {
                Res.GetWPSplashInfo(currentWP, currentSkillType);
                return;
            }
            int typeWeapone = this.weaponStyle;
            if (this.idModel[4] > -1 && !this.isDoing) {
                typeWeapone = this.idModel[4];
            }
            if (this.isDoing) {
                typeWeapone = 0;
            }
            if (this.state != 2) {
                if (this.weapon_frame > 1) {
                    this.weapon_frame = (short) 0;
                }
                if (this.weapon_frame < 0) {
                    this.weapon_frame = (short) 0;
                }
                if (typeWeapone != -1 && Res.loadImgWeaPone(claz, typeWeapone, weaIndex) != null) {
                    WeaponInfo wi = Res.loadImgWeaPone(claz, typeWeapone, weaIndex);
                    if (this.idModel[4] > -1 && !this.isDoing) {
                        typeWeapone = 4;
                    }
                    g.drawRegion(wi.img, pWeapone[claz][this.dir + (typeWeapone * 4)], 0, sw[claz][this.dir + (typeWeapone * 4)], hwp[claz], tWeapone[claz][this.dir], this.x + dwx[claz][this.dir + (typeWeapone * 4)] + wp.PF_X[this.dir][this.weapon_frame] + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), (((((((f + this.y) + dwy[claz][this.dir + (typeWeapone * 4)]) + wp.PF_Y[this.dir][this.weapon_frame]) + ((this.dir == 2 && this.weapon_frame == 1) ? fixY[claz] : (byte) 0)) + this.z) + (isHorse != -1 ? dyb[isHorse][this.dir] : (byte) 0)) - ((this.dir == 1 && this.anmFly == 1) ? 5 : 0)) + this.dy + getdyParHorse(), 0);
                }
            } else {
                if (!this.isDoing) {
                    int currentWP2 = this.weaponType - 3;
                    byte b = skill_type[claz][this.currentSkillType];
                    claz = this.clazz;
                    byte b2 = this.weaponIndex;
                    wp = Res.wpSplashInfos[currentWP2][b];
                    if (wp == null) {
                        Res.GetWPSplashInfo(currentWP2, b);
                        return;
                    }
                }
                if (this.weapon_frame < wp.P0_X[this.dir].length && wp.image != null) {
                    g.drawRegion(wp.image, wp.P0_X[this.dir][this.weapon_frame], wp.P0_Y[this.dir][this.weapon_frame], wp.PF_W[this.dir][this.weapon_frame], wp.PF_H[this.dir][this.weapon_frame], 0, this.x + wp.PF_X[this.dir][this.weapon_frame] + (isHorse != -1 ? dxr[isHorse][this.dir][this.ifHorse] : (byte) 0), f + this.y + wp.PF_Y[this.dir][this.weapon_frame] + this.z + (isHorse != -1 ? dyb[isHorse][this.dir] : (byte) 0) + this.dy, 0);
                }
            }
            if (this.state == 0 || this.state == 1 || this.state == 4) {
                int lv = getStyleWeapon();
                if (Canvas.gameTick % 2 == 0) {
                    this.idLight = (this.idLight + 1) % lightFrame.length;
                }
                if (lv > 1) {
                    int id = lv - 1;
                    if (id > 3) {
                        id = 3;
                    }
                    paintEffectVukhi(g, id, wp, claz);
                }
            }
        }
    }

    private void paintEffectVukhi(Graphics g, int id, WPSplashInfo wp, int clazz) {
        int xx = 0;
        if (getHorse() == -1 || GameData.imgHorse == null) {
            xx = 9;
        }
        g.drawRegion(imgLight, 0, (id * 28) + (lightFrame[this.idLight] * 7), 7, 7, 0, this.x + countX[clazz][this.dir], 5 + this.y + xx + wp.PF_Y[this.dir][this.weapon_frame] + countY[clazz][this.dir] + this.dy, 0);
    }

    public void paintAvatar(Graphics g, short x, short y) {
        if (paintPokemonTinh(g, x, y)) {
            return;
        }
        ImageIcon img;
        ImageIcon img2;
        int leg = this.legStyle;
        int body = this.bodyStyle;
        int head = this.currentHead;
        int hat = this.hatStyle;
        int coat = this.coatStyle;
        if (this.idModel[0] != -1) {
            head = this.idModel[0];
        }
        if (this.idModel[1] != -1) {
            hat = this.idModel[1];
        }
        if (this.idModel[2] != -1) {
            body = this.idModel[2];
        }
        if (this.idModel[3] != -1) {
            leg = this.idModel[3];
        }
        if (coat != -1) {
            if (this.idImgPP == -1) {
                Res.getCharPartInfo(4, coat).paintAvatar(g, x, y, this.frame);
            } else {
                ImageIcon img3 = GameData.getImgIcon(this.idImgPP);
                if (img3 != null && img3.img != null) {
                    if (Canvas.gameTick % 5 == 0) {
                        this.ppAvartar = (byte) ((this.ppAvartar + 1) % this.nFramePP);
                    }
                    g.drawRegion(img3.img, 0, (img3.img.getHeight() / this.nFramePP) * this.ppAvartar, img3.img.getWidth(), img3.img.getHeight() / this.nFramePP, 0, x + Dx_Dy_PP[0][this.indexPP], y + Dx_Dy_PP[1][this.indexPP], 0);
                }
            }
        }
        if (leg != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(0, leg).paintAvatar(g, x, y, this.frame);
        }
        if (body != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(1, body).paintAvatar(g, x, y, this.frame);
        }
        if (head != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(2, head).paintAvatar(g, x, y, this.frame);
        }
        if (hat != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(3, hat).paintAvatar(g, x, y, this.frame);
        }
        if (this.idImgAVT != -1 && (img2 = GameData.getImgIcon(this.idImgAVT)) != null && img2.img != null) {
            if (Canvas.gameTick % 5 == 0) {
                this.AVT = (byte) ((this.AVT + 1) % this.nFrameAVT);
            }
            g.drawRegion(img2.img, 0, (img2.img.getHeight() / this.nFrameAVT) * this.AVT, img2.img.getWidth(), img2.img.getHeight() / this.nFrameAVT, 0, x + Dx_Dy_AVT[0][this.indexAVT], y + Dx_Dy_AVT[1][this.indexAVT], 0);
        }
        if (this.weaponType != -1 && this.weaponStyle != -1 && this.idImageWeapon == -1 && this.imgWpa != null) {
            g.drawImage(this.imgWpa, x + this.dxWear, y + this.dyWear, 0);
        }
        if (this.idImageWeapon != -1 && (img = GameData.getImgIcon(this.idImageWeapon)) != null && img.img != null) {
            if (Canvas.gameTick % 5 == 0) {
                this.wpAvartar = (byte) ((this.wpAvartar + 1) % this.nFrameWP);
            }
            g.drawRegion(img.img, 0, (img.img.getHeight() / this.nFrameWP) * this.wpAvartar, img.img.getWidth(), img.img.getHeight() / this.nFrameWP, 0, x + Dx_Dy_WP[0][this.typeWeapon], y + Dx_Dy_WP[1][this.typeWeapon], 0);
        }
    }

    @Override
    public void setPosTo(short x, short y) {
        moveTo(x, y);
    }

    public void setCurrentSkillByID(byte skillID) {
        this.currentSkillType = skillID;
        if (this.isDoing) {
            this.currentSkillAnimate = SkillManager.getSkillAnimate(0, this.clazz);
        } else {
            this.currentSkillAnimate = SkillManager.getSkillAnimate(this.currentSkillType, this.clazz);
        }
        // pokemon (K3): Pokemon dung particle PMD cua chieu thay hieu ung cu (thieu thi giu cu)
        SkillAnimate pokeSkill = game.pokemon.PokeHieuUng.layChieu(this, this.isDoing ? 0 : this.currentSkillType);
        if (pokeSkill != null) {
            this.currentSkillAnimate = pokeSkill;
        }
        this.coolDownSkill[this.currentSkillType] = SkillManager.getSkillCooldown(this.currentSkillType, this.clazz, skillLevelLearnt[skillID]);
    }

    public byte getSkillIDFromType(byte skillType) {
        return (byte) ((skillType * 10) + skillLevelLearnt[skillType]);
    }

    @Override
    public int checkMagicShield(int dam) {
        int mpLost = 0;
        if (this.clazz == 2 && this.buffType[3] != -1) {
            mpLost = (dam * SkillManager.SKILL_DAM_PERCENT[this.clazz][7][this.lvBuff[3]]) / 100;
            if (mpLost > this.mp) {
                mpLost = this.mp;
            }
        }
        return mpLost;
    }

    public void startAttack(LiveActor target, AttackResult atr, byte skillID, byte level) {
        setCurrentSkillByID(skillID);
        this.currentSkillAnimate.setLvSkill(level);
        this.attackTarget = target;
        this.timeLastUseSkills[this.currentSkillType] = System.currentTimeMillis();
        this.state = (byte) 2;
        this.attkPower = 0;
        this.currentSkillAnimate.getX2();
        this.attkPower = atr.dam;
        this.attkEffect = atr.effect;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    public int getBuffEffAttack() {
        Random r = new Random(System.currentTimeMillis());
        switch (this.clazz) {
            case 0:
                if (skillLevelLearnt[4] > 0 && r.nextInt(100) < SkillManager.SKILL_DAM_PERCENT[0][4][skillLevelLearnt[4]]) {
                    return 0;
                }
                return -1;
            case 1:
            case 2:
            case 3:
            case 4:
            default:
                return -1;
        }
    }

    public void setWearingInfo(Vector items) {
        this.wearingItems = items;
        this.isDoing = false;
        this.bodyStyle = (short) -1;
        this.legStyle = (short) -1;
        this.hatStyle = (short) -1;
        this.weaponStyle = -1;
        this.weaponType = -1;
        this.coatStyle = (short) -1;
        for (int i = 0; i < items.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) items.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if (item.type == 0) {
                this.bodyStyle = item.style;
                this.infoPaint[1] = item.style;
            }
            if (item.type == 1) {
                this.legStyle = item.style;
                this.infoPaint[0] = item.style;
            }
            if (item.type == 2) {
                this.hatStyle = item.style;
                this.infoPaint[3] = item.style;
            }
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                this.weaponType = item.type;
                this.weaponStyle = item.style;
                this.weaponIndex = item.colorItem;
                this.infoPaint[5] = item.style;
            }
            if (item.type == 19) {
                this.coatStyle = item.style;
                this.infoPaint[4] = item.style;
            }
            if (item.type == 13) {
                this.isDoing = true;
            }
        }
        if (this.hp <= 0) {
            this.state = (byte) 3;
        }
        this.isWearing = true;
    }

    public void setAnimalWearingInfo(Vector items) {
        this.wearingAnimal = items;
    }

    private int getStyleWeapon() {
        for (int i = 0; i < this.wearingItems.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                return item.style;
            }
        }
        return -1;
    }

    public void setInventory(long charXu0, int[] potions, Vector items, Vector anm, int type) {
        if (type == 0) {
            this.potions = potions;
            this.charXu = charXu0;
        }
        if (type == 1) {
            inventory.removeAllElements();
            inventory = items;
        }
        if (type == 2) {
            animal.removeAllElements();
            animal = anm;
        }
    }

    public int getCountPotion() {
        int c = 0;
        for (int i = 1; i < this.potions.length; i++) {
            if (this.potions[i] > 0) {
                c++;
            }
        }
        return c;
    }

    public int getAttack() {
        return this.attack + ((this.attack * 0) / 100);
    }

    public int[] calInfoAttackPlayer(Char actor, int def) {
        int[] result = {def, 0};
        result[0] = result[0] - (result[0] / 3);
        result[1] = this.attack;
        if (actor.clazz == this.clazz) {
            result[1] = result[1] / 2;
        } else {
            int mAt = mainAt[this.clazz][actor.clazz];
            int mDf = mainDef[this.clazz][actor.clazz];
            int pcAt = pcAttack[this.clazz][actor.clazz];
            int pcDf = pcDef[this.clazz][actor.clazz];
            if (mAt == 0) {
                result[1] = result[1] + ((result[1] * pcAt) / 100);
            } else if (mAt == 1) {
                result[1] = (this.attack * pcAt) / 100;
            }
            if (mDf == 0) {
                result[0] = result[0] - ((result[0] * pcDf) / 100);
            } else if (mDf == 2) {
                result[0] = result[0] - ((def * pcDf) / 100);
            } else if (mDf == 3) {
                result[0] = def;
                result[0] = result[0] - ((def * pcDf) / 100);
            }
        }
        return result;
    }

    @Override
    public int attackDam(LiveActor actor) {
        int def = actor.defend;
        int att = getAttack();
        if (actor.catagory == 0) {
            if (this.clazz == 2 || this.clazz == 4) {
                def = actor.defend_magic;
            }
            int[] result = calInfoAttackPlayer((Char) actor, def);
            def = result[0];
            att = result[1];
        } else {
            if ((this.he + 2) % 5 == actor.he) {
                def -= (def * 5) / 100;
            } else if ((this.he + 3) % 5 == actor.he && actor.catagory == 0) {
                def += (def * 5) / 100;
            }
            if ((this.he + 2) % 5 == actor.he) {
                att += (att * 25) / 100;
            } else if ((this.he + 3) % 5 == actor.he) {
                att -= (att * (actor.catagory == 1 ? 5 : 25)) / 100;
            }
        }
        int dam = att - def;
        if (dam < 0) {
            dam = att / 10;
        }
        int dam2 = ((dam * 110) / 100) + nextInt(5);
        this.xuyengiap = false;
        if (getBuffEffAttack() > -1 && this.clazz == 0) {
            dam2 = att;
            this.xuyengiap = true;
        }
        if (actor.catagory == 1 && isFullParty()) {
            dam2 += (dam2 * 20) / 100;
        }
        actor.getClass();
        if (dam2 < 5) {
            dam2 = 5;
        }
        return dam2;
    }

    public boolean isFullParty() {
        if (party.size() < 4) {
            return false;
        }
        int[] clazz = new int[5];
        byte b = Canvas.gameScr.mainChar.clazz;
        clazz[b] = clazz[b] + 1;
        for (int i = 0; i < party.size(); i++) {
            PartyInfo p = (PartyInfo) party.elementAt(i);
            if (p != null) {
                int i2 = p.clazz;
                clazz[i2] = clazz[i2] + 1;
                if (clazz[p.clazz] > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public AttackResult getFinalDamTo(LiveActor attackedLiveActor, byte skillID, byte level) {
        int skillDamPercent = SkillManager.getSkillDamPercent(skillID, level, this.clazz);
        AttackResult atr = new AttackResult();
        int dam = (attackDam(attackedLiveActor) * skillDamPercent) / 100;
        if (this.xuyengiap && attackedLiveActor.catagory == 0 && ((Char) attackedLiveActor).clazz != 2) {
            dam /= 2;
        }
        if (dam <= 0) {
            dam = 1;
        }
        isCritical();
        boolean isMiss = attackMiss(attackedLiveActor);
        boolean isCrit = false;
        if (isMiss) {
            atr.effect = (byte) 1;
            atr.dam = 0;
        } else if (isCrit) {
            atr.effect = (byte) 2;
            atr.dam = (short) (dam * 2);
        } else {
            atr.effect = (byte) 0;
            atr.dam = (short) dam;
        }
        atr.mpLost = SkillManager.getSkillMP(skillID, level, this.clazz);
        atr.buffEffect = getBuffEffAttack();
        return atr;
    }

    public void setProperty(Property p) {
        if (p.key.equals("hp")) {
            int i = p.value;
            this.hp = i;
            this.realHP = i;
        }
        if (p.key.equals("mp")) {
            this.mp = p.value;
        }
        if (p.key.equals("bp")) {
            this.basePointLeft = (short) p.value;
        }
        if (p.key.equals("sp")) {
            this.skillPointLeft = (short) p.value;
        }
    }

    public void setBuff(byte[] bufftype, short[] countdown) {
        for (int i = 0; i < bufftype.length; i++) {
            if (bufftype[i] <= 0) {
                // server bao o nay het hieu ung: xoa bieu tuong con dang ve (ban cu dao dieu kien -> khong bao gio xoa)
                if (isExistBuff(BUFFTYPE[i])) {
                    removeBuff(BUFFTYPE[i]);
                }
            } else if (!isExistBuff(bufftype[i])) {
                CharBuff cb = new CharBuff(this.x, this.y, bufftype[i]);
                cb.setTimeLive(countdown[i]);
                this.buff.addElement(cb);
            }
        }
    }

    public void setCharInfo() {
        this.xTo = this.x;
        this.yTo = this.x;
        this.wName = (short) Font.smallFont.getWidth(this.name);
        this.realHP = this.hp;
        this.gender = GENDER_OF_CLAZZ[this.clazz];
        setBuff(this.buffType, this.countDown);
        if (this.killer > 0) {
            this.iskiller = true;
        }
        if (this.hp <= 0) {
            this.state = (byte) 3;
        }
        this.isMaininfo = true;
    }

    public boolean kilAllMonsterQuest(int id) {
        if (this.monster_kill != null && this.receiveQuest && this.typeQuest == 0) {
            for (int i = 0; i < this.monster_kill.length; i++) {
                if (id == this.monster_kill[i][0]) {
                    if (this.monster_kill[i][2] < this.monster_kill[i][1]) {
                        int[] iArr = this.monster_kill[i];
                        iArr[2] = iArr[2] + 1;
                        this.infoQuest = new StringBuffer(String.valueOf(this.monster_kill[i][2])).append("/").append(this.monster_kill[i][1]).toString();
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public boolean getItemQuest(int idItem) {
        if (this.monster_kill != null && this.receiveQuest && this.typeQuest == 2) {
            for (int i = 0; i < this.monster_kill.length; i++) {
                if (idItem == this.monster_kill[i][2]) {
                    if (this.monster_kill[i][3] < this.monster_kill[i][1]) {
                        int[] iArr = this.monster_kill[i];
                        iArr[3] = iArr[3] + 1;
                        this.infoQuest = new StringBuffer(String.valueOf(this.monster_kill[i][3])).append("/").append(this.monster_kill[i][1]).toString();
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public int getDurableWeapone() {
        for (int i = 0; i < this.wearingItems.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                return cwi.durable;
            }
        }
        return 0;
    }

    public int getPriceRepair(int type) {
        int total;
        int pArmo = 0;
        int pWeapone2 = 0;
        for (int i = 0; i < this.wearingItems.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                pWeapone2 += item.price / 10;
            } else {
                pArmo += item.price / 10;
            }
        }
        if (type == 0) {
            total = pArmo;
        } else if (type == 1) {
            total = pWeapone2;
        } else {
            total = pArmo + pWeapone2;
        }
        return total;
    }

    public void downDurableWeapone() {
        for (int i = 0; i < this.wearingItems.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if ((item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) && cwi.mDurable > 0) {
                cwi.mDurable = (short) (cwi.mDurable - 1);
                if (cwi.mDurable % 10 == 0) {
                    cwi.durable = (short) (cwi.durable - 1);
                }
            }
        }
    }

    public void downDuarable() {
        for (int i = 0; i < this.wearingItems.size(); i++) {
            ItemInInventory cwi = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(cwi.charClazz, cwi.idTemplate);
            if ((item.type == 0 || item.type == 1 || item.type == 2) && cwi.mDurable > 0) {
                cwi.mDurable = (short) (cwi.mDurable - 1);
                if (cwi.mDurable % 10 == 0) {
                    cwi.durable = (short) (cwi.durable - 1);
                }
            }
        }
    }

    public void updateframeeff() {
        if (this.listEffect != null && this.listEffect.length > 0) {
            for (int i = 0; i < this.listEffect.length; i++) {
                DataSkillEff parEff = getPartEff(this.listEffect[i]);
                if (parEff != null && Canvas.gameTick % this.Tick[i] == 0 && parEff != null) {
                    int ms = parEff.listFrame.size();
                    if (ms == 0) {
                        ms = 1;
                    }
                    this.Frameeff = (byte) ((this.Frameeff + 1) % ms);
                }
            }
        }
    }

    public void paintListEff(Graphics g, int lv) {
        if (this.listEffect != null && this.listEffect.length > 0) {
            for (int i = 0; i < this.listEffect.length; i++) {
                DataSkillEff parEff = getPartEff(this.listEffect[i]);
                if (parEff != null) {
                    if (lv == 0) {
                        parEff.paintTopHorse(g, this.x, this.y + (this.idhorse != -1 ? 0 : getDyAnimalEff(2, this.dir, this.anmFly)) + getdyParHorse(), this.Frameeff, 0);
                    } else {
                        parEff.paintBottomHorse(g, this.x, this.y + (this.idhorse != -1 ? 0 : getDyAnimalEff(2, this.dir, this.anmFly)) + getdyParHorse(), this.Frameeff, 0);
                    }
                }
            }
        }
    }

    @Override
    public int getHp() {
        return this.hp;
    }

    @Override
    public void setState(int state) {
        this.state = (byte) state;
    }

    @Override
    public int getRealHp() {
        return this.realHP;
    }

    public boolean getAllItemQuest(int id) {
        return false;
    }

    @Override
    public boolean isNPC() {
        return this.idBoss != -1;
    }

    @Override
    public boolean isNpcChar() {
        return isNPC();
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public int getTypeNpc() {
        if (isNPC()) {
            return this.idBoss;
        }
        return super.getTypeNpc();
    }

    @Override
    public String getCharName() {
        return this.name;
    }

    @Override
    public void setHorse(int idhorse, int typehorse) {
        this.idhorse = (short) idhorse;
        this.typehorse = (short) typehorse;
    }

    @Override
    public void setWeapon(int id, int type, int idImg) {
        this.idWeapon = (short) id;
        this.typeWeapon = (short) type;
        this.idImageWeapon = (short) idImg;
    }

    @Override
    public void setPartWearing(short idHair, short idBody, short idLeg) {
        this.idHair = idHair;
        this.idBody = idBody;
        this.idLeg = idLeg;
    }

    @Override
    public void setEff(short[] arrID, byte[] arrTick) {
        this.listEffect = arrID;
        this.Tick = arrTick;
    }

    @Override
    public short getyHorse() {
        if (this.useHorse != -1 || this.idhorse > -1) {
            return (short) 35;
        }
        return (short) 0;
    }

    @Override
    public short getx() {
        return this.x;
    }

    @Override
    public short gety() {
        return this.y;
    }
}
