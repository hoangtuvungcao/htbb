package game.model;

import android.graphics.Bitmap;
import effect.DataSkillEff;
import effect.DynamicEffect;
import game.core.j2me.Graphics;
import game.model.skill.SkillAnimate;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import game.render.Util;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public class Char extends LiveActor {
    public static final byte A_ATTACK = 2;
    public static final byte A_COME_HOME = 4;
    public static final byte A_DEAD = 3;
    public static final byte A_RUN = 1;
    public static final byte A_STAND = 0;
    public static short CHAR_POTION = 25;
    public static final byte CUNG = 4;
    public static final byte DAO = 1;
    public static final byte DOWN = 0;
    public static final byte DS = 3;
    public static final byte KIEM = 0;
    public static final byte LEFT = 2;
    public static final byte MOVETOFIRST = 5;
    public static final byte PS = 2;
    public static final byte REAL_FRAME_EACH_SIZE = 5;
    public static final byte RIGHT = 3;
    public static final byte UP = 1;
    public static boolean dissolved = false;
    public static Bitmap imgFlgPk;
    public static Bitmap imgLight;
    public static byte[] skillLevelLearnt;
    byte AVT;
    public Effect EffFace;
    public int Fhorse;
    public byte FrameHair;
    public byte FrameWP;
    public byte Framebody;
    byte Frameeff;
    public int IDDB;
    public byte[] Tick;
    public short agility;
    public byte anmFly;
    public LiveActor attackTarget;
    public byte attkEffect;
    public int attkPower;
    public short baokich;
    public short basePointLeft;
    public byte[] buffType;
    public long charXu;
    public byte clazz;
    public boolean comeHome;
    public short[] countDown;
    byte countNo;
    public SkillAnimate currentSkillAnimate;
    public byte currentSkillType;
    public int delay;
    public short dxWear;
    public byte dyThanthu;
    public short dyWear;
    public Effect effAnimal;
    public Effect effPhap;
    public int fh;
    public boolean finishQuest;
    public byte frameLeg;
    public byte framePP;
    public int frameThuCuoi;
    public byte gender;
    public boolean gotoBoard;
    public short health;
    public int himg;
    int idColor;
    public byte idImgHorse;
    int idLight;
    public short[] idModel;
    public short idQuestNPC;
    public int ifHorse;
    public Bitmap imgAnimal;
    public Bitmap imgWpa;
    public short[] infoPaint;
    public String infoQuest;
    public String[] info_npc_quest;
    public String[][] info_response;
    public boolean isAnimal;
    public boolean isDoing;
    public boolean isNo;
    public boolean isTrade;
    public ItemInInventory itemImbue;
    public int lastXAuto;
    public int lastYAuto;
    public short[] listEffect;
    public int lockUpdate;
    public short luck;
    public byte[] lvBuff;
    public short lvpercent;
    public int maxmp;
    public int[][] monster_kill;
    public int mp;
    public IsAnimal myAnimal;
    public AnimalMove myPet;
    public String name;
    public byte nextNpc;
    public byte[] npc_quest;
    public byte[] npc_response;
    public byte numFrame;
    public short p1;
    public short p2;
    public short p3;
    public short p4;
    public long[] potionLastTimeUse;
    public int[] potions;
    byte ppAvartar;
    public short questID;
    public int realTime;
    public boolean receiveQuest;
    public short skillPointLeft;
    public short spirit;
    public long stand;
    public short strength;
    public boolean[] talked;
    int time;
    public long timeComeHome;
    public long timeDelayRequestCharInfo;
    public long timeGoToLastXY;
    public long timeRemoEffanimal;
    public long timeUseGolgTicket;
    public long timeWait2Board;
    public long timeWearScarves;
    public byte typeQuest;
    public byte upSpeed;
    public boolean useBuff;
    public byte useHorse;
    public short wName;
    public Vector wearingAnimal;
    public Vector wearingItems;
    public int wimg;
    byte wpAvartar;
    public short xTo;
    boolean xuyengiap;
    public short yTo;
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
    public static final byte[] STANDFRAME = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1};
    public static final byte[] RUNFRAME = {2, 2, 2, 0, 0, 0, 3, 3, 3, 0, 0, 0};
    public static final byte[] RUNFRAMEWAL = {2, 2, 2, 0, 0, 3, 3, 3, 0, 0};
    public static final byte[] GENDER_OF_CLAZZ = {1, 1, 2, 1, 2};
    public static Hashtable ALL_PART_EFF = new Hashtable();
    public static short[][][] fHorse = {new short[][]{new short[]{95, 112, 130}, new short[]{246, 263, 280}, new short[]{216, 182, 149}, new short[]{298, 329, 363}}, new short[][]{new short[]{119, 138, 158}, new short[]{308, 325, 341}, new short[]{267, 223, 178}, new short[]{359, 400, 444}}, new short[][]{new short[]{107, 124, 140}, new short[]{276, 292, 309}, new short[]{239, 197, 158}, new short[]{324, 362, 404}}, new short[][]{new short[]{115, 133, 150}, new short[]{296, 313, 330}, new short[]{254, 210, 167}, new short[]{347, 387, 433}}, new short[][]{new short[]{129, 152, 179, 152}, new short[]{210, 233, 260, 233}, new short[]{0, 43, 86, 43}, new short[]{291, 334, 377, 334}}, new short[][]{new short[]{180, 210, 255, 210}, new short[]{303, 333, 379, 333}, new short[]{0, 60, 120, 60}, new short[]{427, 487, 547, 487}}, new short[][]{new short[]{0, 24, 48}, new short[]{171, 192, 213}, new short[]{72, 106, 140}, new short[]{234, 265, 299}}, new short[][]{new short[]{180, 210, 255, 210}, new short[]{303, 333, 379, 333}, new short[]{0, 60, 120, 60}, new short[]{427, 487, 547, 487}}};
    public static final byte[][] dxh = {new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, 3, -4}, new byte[]{0, 0, -3, 3}, new byte[]{0, 0, 3, -4}};
    public static final byte[][] dyh = {new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{-2, 0, 7, 7}, new byte[]{0, 0, 0, 0}, new byte[]{-2, 0, 7, 7}};
    public static final byte[][] dyb = {new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}, new byte[]{-12, -7, -10, -10}};
    public static final byte[][][] dyr = {new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}, new byte[]{0, -2, 0, -2}}, new byte[][]{new byte[]{-3, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}}, new byte[][]{new byte[]{0, -2, 0}, new byte[]{0, -2, 0}, new byte[]{0, -2, 2}, new byte[]{0, -2, 2}}, new byte[][]{new byte[]{-3, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}, new byte[]{0, -2, 1, -2}}};
    public static final byte[][][] dxr = {new byte[][]{new byte[]{0, 0, 0}, new byte[]{0, 0, 0}, new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[]{0, 0, 0}, new byte[]{0, 0, 0}, new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[]{0, 0, 0}, new byte[]{0, 0, 0}, new byte[]{-2, -2, -2}, new byte[]{2, 2, 2}}, new byte[][]{new byte[]{0, 0, 0}, new byte[]{0, 0, 0}, new byte[]{-2, -2, -2}, new byte[]{2, 2, 2}}, new byte[][]{new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{-4, -4, -4, -4}, new byte[]{2, 2, 2, 2}}, new byte[][]{new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{-4, -4, -2, -4}, new byte[]{2, 2, 1, 2}}, new byte[][]{new byte[]{0, 0, 0}, new byte[]{0, 0, 0}, new byte[]{0, -2, 0}, new byte[]{0, 2, 0}}, new byte[][]{new byte[]{0, 0, 0, 0}, new byte[]{0, 0, 0, 0}, new byte[]{-4, -4, -2, -4}, new byte[]{2, 2, 1, 2}}};
    public static final byte[][] dxs = {new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}, new byte[]{0, 0, -2, 2}};
    static final byte[][][] sizeHorse = {new byte[][]{new byte[]{18, 18, 18}, new byte[]{17, 17, 17}, new byte[]{31, 34, 33}, new byte[]{31, 34, 33}}, new byte[][]{new byte[]{20, 20, 20}, new byte[]{17, 17, 17}, new byte[]{41, 44, 44}, new byte[]{41, 44, 44}}, new byte[][]{new byte[]{17, 17, 17}, new byte[]{16, 16, 16}, new byte[]{37, 42, 40}, new byte[]{37, 42, 40}}, new byte[][]{new byte[]{17, 17, 17}, new byte[]{17, 17, 17}, new byte[]{41, 45, 43}, new byte[]{41, 45, 43}}, new byte[][]{new byte[]{23, 27, 31, 27}, new byte[]{23, 27, 31, 27}, new byte[]{43, 43, 43, 43}, new byte[]{43, 43, 43, 43}}, new byte[][]{new byte[]{30, 45, 48, 45}, new byte[]{30, 45, 48, 45}, new byte[]{60, 60, 60, 60}, new byte[]{60, 60, 60, 60}}, new byte[][]{new byte[]{24, 24, 24}, new byte[]{21, 21, 21}, new byte[]{34, 34, 31}, new byte[]{31, 34, 34}}, new byte[][]{new byte[]{30, 45, 48, 45}, new byte[]{30, 45, 48, 45}, new byte[]{60, 60, 60, 60}, new byte[]{60, 60, 60, 60}}};
    static final byte[] hHorse = {28, 26, 23, 27, 35, 45, 37, 45};
    static byte[][][] ORDER_PAINT = {new byte[][]{new byte[]{5, 4, 0, 1, 6, 2, 3}, new byte[]{6, 1, 4, 2, 3, 5}, new byte[]{4, 5, 0, 1, 2, 3, 6}, new byte[]{4, 5, 0, 1, 2, 3, 6}}, new byte[][]{new byte[]{4, 0, 1, 6, 2, 3, 5}, new byte[]{6, 0, 1, 4, 2, 3, 5}, new byte[]{4, 0, 1, 2, 3, 5, 6}, new byte[]{4, 0, 1, 2, 3, 5, 6}}};
    public static byte[] horseMove = {2, 2, 2, 3, 3, 4, 4, 4};
    static int count = 0;
    static final byte[] lightFrame = {0, 1, 2, 3, 2, 1};
    static final byte[][] skill_type = {new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 0, 2, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}, new byte[]{0, 1, 2, 0, 0, 2, 2, 2, 2}};
    public static WeaponInfo[][][] imgWeapone = (WeaponInfo[][][]) Array.newInstance((Class<?>) WeaponInfo.class, 5, 15, 7);
    static final byte[][] tWeapone = {new byte[]{2, 0, 0, 0}, new byte[]{0, 2, 2, 0}, new byte[]{2, 0, 0, 2}, new byte[]{0, 2, 0, 0}, new byte[]{0, 2, 2, 0}};
    static final byte[][] pWeapone = {new byte[]{0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 21, 21, 0, 0, 29, 14}, new byte[]{12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 12, 12, 0, 0, 11, 11, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 15, 15, 38, 0, 16, 16, 43, 0}, new byte[]{14, 14, 0, 0, 15, 15, 0, 0, 15, 15, 0, 0, 15, 15, 0, 0, 16, 16, 0, 0, 19, 19, 0, 0, 21, 21, 0, 0}};
    static final byte[][] sw = {new byte[]{21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 21, 21, 11, 11, 14, 14, 5, 15}, new byte[]{19, 19, 9, 11, 19, 19, 9, 11, 19, 19, 9, 11, 19, 19, 9, 11, 20, 20, 12, 12, 19, 19, 9, 11, 20, 20, 12, 12}, new byte[]{14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 14, 14, 14, 14, 15, 15, 15, 15}, new byte[]{24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15, 24, 24, 8, 15}, new byte[]{21, 21, 14, 14, 21, 21, 15, 15, 21, 21, 15, 15, 21, 21, 15, 15, 22, 22, 16, 16, 21, 21, 15, 15, 26, 26, 20, 20}};
    static final byte[][] dwx = {new byte[]{0, 0, -6, 0, 0, 0, -6, 0, 0, 0, -6, 0, 0, 0, -6, 0, 0, 0, -6, 0, 0, 0, -6, 0, 6, 2, 0, 1}, new byte[]{0, 0, -5, 0, 0, 0, -5, 0, 0, 0, -5, 0, 0, 0, -5, 0, 0, 0, -7, 0, 0, 0, -5, 0, 0, 0, -7, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, -1, 0, 3, -2, 1, 1, 0, -2, -2, -2}};
    static final byte[][] dwy = {new byte[]{0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0}, new byte[]{0, 0, -3, 0, 0, 0, -3, 0, 0, 0, -3, 0, 0, 0, -3, 0, 0, 0, -3, 0, 0, 0, -3, 0, 0, 0, -3, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static final byte[] hwp = {21, 40, 14, 28, 20};
    static final byte[] fixY = {0, -1, 0, 0, 0};
    public static byte[][] dx_horse = {new byte[]{0, 0, 7, -7}};
    public static byte[][] dy_horse = {new byte[]{-20, -15, -15, -15}};
    public static byte[][] Dx_Dy_WP = {new byte[]{0}, new byte[]{0}};
    public static byte[][] Dx_Dy_PP = {new byte[]{0}, new byte[]{0}};
    public static byte[][] Dx_Dy_AVT = {new byte[]{0}, new byte[]{0}};
    private static byte[][] countX = {new byte[]{7, -15, 7, -12}, new byte[]{6, -14, -4, -1}, new byte[]{-12, 3, 8, -12}, new byte[]{9, -15, 7, -10}, new byte[]{-14, -13, 7, -11}};
    private static byte[][] countY = {new byte[]{-12, -8, -10, -1}, new byte[]{-12, -8, -15, -11}, new byte[]{-6, -6, -6, -6}, new byte[]{-12, -5, -19, 0}, new byte[]{-18, 3, 0, -4}};
    public static int[][] mainDef = {new int[]{0, 0, 0, 0, 0}, new int[]{2, 0, 0, 3, 0}, new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, new int[]{0, 4, 0, 0, 0}};
    public static int[][] mainAt = {new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, new int[]{1, 0, 0, 0, 0}};
    public static int[][] pcAttack = {new int[]{0, -25, 0, 25, 15}, new int[]{10, 0, -15, 10, -10}, new int[]{5, 0, 0, -35, -5}, new int[]{-20, 5, 35, 0, -10}, new int[]{80, -5, 5, 30, 0}};
    public static int[][] pcDef = {new int[]{0, -5, -15, 20, 0}, new int[]{-90, 0, -15, 40, -35}, new int[]{0, 0, 0, -5, -15}, new int[]{-20, 10, 5, 0, -10}, new int[]{-25, 0, 0, 20, 0}};
    public static final byte[] BUFFTYPE = {19, 20, 22, 23, 24, 25, 27};
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
    public short idWeapon = -1;
    public short typeWeapon = -1;
    public short idImageWeapon = -1;
    public short idHair = -1;
    public short idBody = -1;
    public short idLeg = -1;
    public short idPhiPhong = -1;
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

    public boolean getAllItemQuest(int i) {
        return false;
    }

    public boolean isLKD(int i) {
        return i == 8 || i == 9 || i == 10 || i == 11 || i == 66 || i == 245 || i == 157 || i == 158;
    }

    @Override
    public boolean isPlayer() {
        return true;
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
        return FullInventory() > GCanvas.gameScr.mainChar.limItemBag * 42;
    }

    public int FullInventory() {
        return inventory.size() + MainChar.gemItem.size() + getCountPotion() + MainChar.shopItem.size();
    }

    public byte getnSkill() {
        return nSkill[this.clazz];
    }

    public String getGoldTime() {
        long jCurrentTimeMillis = ((long) (goldTimeMinute * 60000)) - (System.currentTimeMillis() - this.timeUseGolgTicket);
        if (jCurrentTimeMillis <= 0) {
            return "";
        }
        long j = jCurrentTimeMillis / 3600000;
        long j2 = ((jCurrentTimeMillis % 3600000) / 1000) / 60;
        return (j == 0 && j2 == 0) ? "" : j + " : " + j2;
    }

    public int countLKD() {
        int i = 0;
        for (int i2 = 0; i2 < MainChar.gemitemImbue.size(); i2++) {
            RealID realID = (RealID) MainChar.gemitemImbue.elementAt(i2);
            if ((realID.ID > 7 && realID.ID < 12) || realID.ID == 158 || realID.ID == 157) {
                i++;
            }
        }
        return i;
    }

    public int countBaoHiem() {
        int size = MainChar.gemitemImbue.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            if (((RealID) MainChar.gemitemImbue.elementAt(i2)).ID < 5) {
                i++;
            }
        }
        return i;
    }

    public boolean haveItemImbue() {
        for (int i = 0; i < MainChar.gemItem.size(); i++) {
            GemTemplate gemTemplate = (GemTemplate) MainChar.gemItem.elementAt(i);
            if ((gemTemplate.id > 7 && gemTemplate.id < 12) || gemTemplate.id == 66 || gemTemplate.id == 158 || gemTemplate.id == 157) {
                return true;
            }
        }
        return false;
    }

    public boolean hadPutItemImbue() {
        for (int i = 0; i < MainChar.gemitemImbue.size(); i++) {
            RealID realID = (RealID) MainChar.gemitemImbue.elementAt(i);
            if ((realID.ID > 7 && realID.ID < 12) || realID.ID == 158 || realID.ID == 66 || realID.ID == 245 || realID.ID == 157 || (realID.ID >= 12 && realID.ID <= 66)) {
                return true;
            }
        }
        return false;
    }

    public int isExistInvector(Vector vector, ItemInInventory itemInInventory) {
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            if ((vector.elementAt(i) instanceof ItemInInventory) && ((ItemInInventory) vector.elementAt(i)).itemID == itemInInventory.itemID) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void dead() {
        this.state = (byte) 3;
    }

    public Char() {
        int i = CHAR_POTION;
        this.potions = new int[i];
        this.potionLastTimeUse = new long[i];
        this.buffType = new byte[]{-1, -1, -1, -1, -1, -1, -1};
        this.lvBuff = new byte[]{0, 0, 0, 0, 0, 0, 0};
        this.countDown = new short[]{0, 0, 0, 0, 0, 0, 0};
        this.useBuff = false;
        this.questID = (short) -1;
        this.idQuestNPC = (short) -1;
        this.receiveQuest = false;
        this.finishQuest = false;
        this.nextNpc = (byte) 0;
        this.typeQuest = (byte) 3;
        this.delay = -1;
        this.myAnimal = null;
        this.idModel = new short[]{-1, -1, -1, -1, -1};
        this.comeHome = false;
        this.realTime = 0;
        this.timeDelayRequestCharInfo = 0L;
        this.timeRemoEffanimal = 0L;
        this.idColor = 0;
        this.ifHorse = 0;
        this.idImgHorse = (byte) 0;
        this.useHorse = (byte) -1;
        this.anmFly = (byte) 0;
        this.infoPaint = new short[]{-1, -1, -1, -1, -1, -1};
        this.Fhorse = 0;
        this.countNo = (byte) 0;
        this.dyThanthu = (byte) 0;
        this.time = 0;
        this.FrameHair = (byte) 0;
        this.Framebody = (byte) 0;
        this.frameLeg = (byte) 0;
        this.framePP = (byte) 0;
        this.FrameWP = (byte) 0;
        this.xuyengiap = false;
        this.infoQuest = "";
        this.catagory = (byte) 0;
        this.speed = char_speed;
        this.upSpeed = (byte) 0;
        this.centerX = (short) 12;
        this.centerY = (short) 12;
        this.height = (short) 32;
        this.width = (short) 40;
        this.hp = 1;
        this.realHP = 1;
        this.dir = (short) r.nextInt(4);
        this.timeDelayRequestCharInfo = System.currentTimeMillis();
        this.isDie = false;
    }

    @Override
    public String getDisplayName() {
        String str = this.name;
        return str == null ? "" : str;
    }

    @Override
    public void paintCorner(Graphics graphics, int i, int i2) {
        graphics.translate((-this.x) + i + 2, ((-this.y) + i2) - 10);
        short s = this.currentHead;
        if (s != -1) {
            Res.getCharPartInfo(2, s).paintStatic(graphics, this.x, (short) (this.y + (this.hatStyle != -1 ? (short) 5 : (short) 0)), this.dir, this.frame);
        }
        short s2 = this.hatStyle;
        if (s2 != -1) {
            Res.getCharPartInfo(3, s2).paintStatic(graphics, this.x, this.y, this.dir, this.frame);
        }
    }

    public void moveTo(short s, short s2) {
        if (canMove()) {
            this.xTo = s;
            this.yTo = s2;
            if (this.x == s && this.y == s2) {
                this.stand = System.currentTimeMillis();
                if (this.state == 2 || this.state == 3) {
                    return;
                }
                this.state = (byte) 0;
                return;
            }
            this.state = (byte) 1;
        }
    }

    @Override
    public void updateBuff() {
        for (int i = 0; i < this.buff.size(); i++) {
            CharBuff charBuff = (CharBuff) this.buff.elementAt(i);
            charBuff.update();
            if (charBuff.wantDetroy) {
                this.buffType[i] = -1;
                this.lvBuff[i] = -1;
                this.buff.removeElementAt(i);
            } else {
                charBuff.setXY(this.x, charBuff.isCenter() ? this.y - 12 : this.y);
            }
        }
    }

    public void createBuff(int i) {
        if (isExistBuff(i)) {
            return;
        }
        CharBuff charBuff = new CharBuff(this.x, this.y, this.clazz + 19);
        if (i == this.ID) {
            charBuff.setTimeLive(20);
        }
        this.buff.addElement(charBuff);
    }

    public void removeBuff(int i) {
        int size = this.buff.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (((CharBuff) this.buff.elementAt(i2)).type == i) {
                this.buff.removeElementAt(i2);
                return;
            }
        }
    }

    public boolean isExistBuff(int i) {
        int size = this.buff.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (((CharBuff) this.buff.elementAt(i2)).type == i) {
                return true;
            }
        }
        return false;
    }

    public void setPercentSpeed(int i) {
        this.upSpeed = (byte) ((this.speed * i) / 100);
        if ((i * this.speed) % 100 >= 5) {
            this.upSpeed = (byte) (this.upSpeed + 1);
        }
    }

    public int[] getTime() {
        return new int[]{this.delay, 59 - ((((int) (System.currentTimeMillis() - this.timeWait2Board)) / 1000) % 60)};
    }

    @Override
    public void actorDie() {
        this.hp = 0;
        this.timeOutPoinson = 0L;
        this.tDelay = (short) 0;
        this.totalTime = (short) 36;
        this.state = (byte) 3;
        this.poinson = (short) 0;
    }

    @Override
    public void isPoinson() {
        if (System.currentTimeMillis() - this.timeOutPoinson < this.tDelay * 1000 || this.tDelay <= 0) {
            return;
        }
        this.hp -= this.poinson;
        this.totalTime = (short) (this.totalTime - this.tDelay);
        this.timeOutPoinson = System.currentTimeMillis();
        if (this.totalTime == 0) {
            this.tDelay = (short) 0;
            this.totalTime = (short) 36;
        }
        if (this.hp > 0 || this.state == 3) {
            return;
        }
        this.hp = 1;
        this.poinson = (short) 0;
        this.tDelay = (short) 0;
        this.totalTime = (short) 36;
    }

    @Override
    public byte getHorse() {
        if (Tilemap.isOfflineMap) {
            return (byte) -1;
        }
        return this.useHorse;
    }

    public static DataSkillEff getPartEff(short s) {
        if (s == -1) {
            return null;
        }
        DataSkillEff dataSkillEff = (DataSkillEff) ALL_PART_EFF.get(((int) s) + "");
        if (dataSkillEff == null) {
            DataSkillEff dataSkillEff2 = new DataSkillEff(s);
            ALL_PART_EFF.put(((int) s) + "", dataSkillEff2);
            dataSkillEff2.timeRemove = (int) (System.currentTimeMillis() / 1000);
            return dataSkillEff2;
        }
        dataSkillEff.timeRemove = System.currentTimeMillis();
        return dataSkillEff;
    }

    public static void SetRemove() {
        Enumeration enumerationKeys = ALL_PART_EFF.keys();
        while (enumerationKeys.hasMoreElements()) {
            String str = (String) enumerationKeys.nextElement();
            if ((System.currentTimeMillis() - ((DataSkillEff) ALL_PART_EFF.get(str)).timeRemove) / 1000 > 200) {
                ALL_PART_EFF.remove(str);
            }
        }
    }

    public void updateHorse() {
        short s;
        DataSkillEff partEff;
        if (GCanvas.gameTick % 5 != 0 || (s = this.idhorse) == -1 || (partEff = getPartEff(s)) == null) {
            return;
        }
        int size = partEff.listFrame.size() / 21;
        if (size == 0) {
            size = 1;
        }
        this.frameThuCuoi = (byte) ((this.frameThuCuoi + 1) % size);
    }

    @Override
    public void update() {
        boolean z;
        boolean z2;
        if (this.ID == 32001) {
            return;
        }
        if (this.hp > 0) {
            int i = this.realHP;
        }
        updateframeeff();
        if (this.dynamicEffTop != null) {
            for (int i2 = 0; i2 < this.dynamicEffTop.size(); i2++) {
                DynamicEffect dynamicEffect = (DynamicEffect) this.dynamicEffTop.elementAt(i2);
                dynamicEffect.update();
                if (dynamicEffect.canDestroy()) {
                    this.dynamicEffTop.removeElementAt(i2);
                    if (dynamicEffect.power < 2000000000) {
                        GCanvas.gameScr.startFlyText("-" + dynamicEffect.power, 0, this.x, (this.y + this.dy) - 15, 1, -2);
                    }
                }
            }
        }
        if (this.dynamicEffBottom != null) {
            for (int i3 = 0; i3 < this.dynamicEffBottom.size(); i3++) {
                DynamicEffect dynamicEffect2 = (DynamicEffect) this.dynamicEffBottom.elementAt(i3);
                dynamicEffect2.update();
                if (dynamicEffect2.canDestroy()) {
                    this.dynamicEffBottom.removeElementAt(i3);
                    if (dynamicEffect2.power < 2000000000) {
                        GCanvas.gameScr.startFlyText("-" + dynamicEffect2.power, 0, this.x, (this.y + this.dy) - 15, 1, -2);
                    }
                }
            }
        }
        if (!this.isFreeze) {
            Effect effect2 = this.EffFace;
            if (effect2 != null) {
                effect2.update();
            }
            Effect effect3 = this.effPhap;
            if (effect3 != null) {
                effect3.update();
            }
            if (this.beStune && System.currentTimeMillis() > this.timeBeStune) {
                this.beStune = false;
            }
            if (System.currentTimeMillis() - this.timeDelayRequestCharInfo > 10000 && fullSet() != 1) {
                if (this.ID != GCanvas.gameScr.mainChar.ID) {
                    if (GameScr.paintChar == 1) {
                        GameService.gI().requestCharInfo(this.ID);
                    }
                } else {
                    GameService.gI().requestMainCharInfo();
                }
                this.timeDelayRequestCharInfo = System.currentTimeMillis();
            }
            isPoinson();
            if (this.delay >= 0) {
                this.delay = this.realTime - (((int) (System.currentTimeMillis() - this.timeWait2Board)) / 60000);
            }
            int i4 = this.lockUpdate;
            if (i4 > 0) {
                this.lockUpdate = i4 - 1;
                return;
            }
            if (this.ID == GCanvas.gameScr.mainChar.ID && GameScr.autoFight && GameScr.typeOptionFocus == 1 && System.currentTimeMillis() - this.timeGoToLastXY < 0) {
                this.timeGoToLastXY = System.currentTimeMillis() + 30000;
                if (GCanvas.gameScr.mainChar.posTransRoad == null && GameScr.timeRemovePos <= 0 && (Math.abs(this.x - this.lastXAuto) > 120 || Math.abs(this.y - this.lastYAuto) > 120)) {
                    GCanvas.gameScr.findRoad2(this.lastXAuto, this.lastYAuto);
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
            GCanvas.gameScr.startExplosionAt(this.x, this.y);
        }
        if (this.isAnimal) {
            Effect effect4 = this.effAnimal;
            if (effect4 != null) {
                effect4.update();
            }
            if ((System.currentTimeMillis() / 1000) - this.timeRemoEffanimal > 0) {
                this.effAnimal = null;
            }
            IsAnimal isAnimal = this.myAnimal;
            if (isAnimal != null) {
                isAnimal.update();
                this.myAnimal.dir = this.dir;
                this.myAnimal.status = this.state;
            }
        }
        byte b = this.state;
        if (b == 0) {
            if (this.catagory == 0 && System.currentTimeMillis() - this.stand > 300000 && GCanvas.gameScr.mainChar != null && this.ID != GCanvas.gameScr.mainChar.ID) {
                this.wantDestroy = true;
            }
            if (this.ID == GCanvas.gameScr.mainChar.ID && GameScr.saveAutoFight) {
                GameScr.autoFight = GameScr.saveAutoFight;
            }
            this.p1 = (short) (this.p1 + 1);
            if (getHorse() == -1 || this.anmFly == 0) {
                short s = this.p1;
                byte[] bArr = STANDFRAME;
                if (s >= bArr.length) {
                    this.p1 = (short) 0;
                }
                this.frame = bArr[this.p1];
                this.weapon_frame = this.frame;
                this.ifHorse = 0;
            } else {
                if (this.p1 >= (getAttack() > -1 ? RUNFRAME.length : RUNFRAMEWAL.length)) {
                    this.p1 = (short) 0;
                }
                if (getHorse() == -1 || GameData.imgHorse == null) {
                    this.frame = RUNFRAMEWAL[this.p1];
                } else if (this.p1 % 3 == 0) {
                    this.ifHorse = (this.ifHorse + 1) % (this.anmFly != 1 ? 3 : 4);
                }
            }
            if (this.frame > 3) {
                this.frame = (byte) 0;
            }
            this.Fhorse = this.frame;
        } else if (b == 1) {
            boolean z3 = GameScr.cheat;
            short s2 = (short) (this.p1 + 1);
            this.p1 = s2;
            if (s2 >= (getHorse() > -1 ? RUNFRAME.length : RUNFRAMEWAL.length)) {
                this.p1 = (short) 0;
            }
            if (getHorse() == -1 || GameData.imgHorse == null) {
                this.frame = RUNFRAMEWAL[this.p1];
            } else if (this.p1 % 3 == 0) {
                this.ifHorse = (this.ifHorse + 1) % (this.anmFly == 1 ? 4 : 3);
            }
            int iAbs = GCanvas.abs(this.x - this.xTo);
            int iAbs2 = GCanvas.abs(this.y - this.yTo);
            int i5 = this.speed + this.upSpeed;
            if (iAbs > 100 || iAbs2 > 100) {
                i5 = this.speed + this.upSpeed + 4;
            } else if (iAbs > 70 || iAbs2 > 70) {
                i5 = this.speed + this.upSpeed + 3;
            } else if (iAbs > 50 || iAbs2 > 50) {
                i5 = this.speed + this.upSpeed + 2;
            } else if (iAbs > 30 || iAbs2 > 30) {
                i5 = this.speed + this.upSpeed + 1;
            }
            if (iAbs < i5) {
                this.x = this.xTo;
                z = true;
            } else {
                z = false;
            }
            if (iAbs2 < i5) {
                this.y = this.yTo;
                z2 = true;
            } else {
                z2 = false;
            }
            if (z && z2) {
                this.p3 = (short) 0;
                this.p2 = (short) 0;
                this.p1 = (short) 0;
                this.state = (byte) 0;
                this.justStopFromRun = true;
                this.stand = System.currentTimeMillis();
            } else {
                if (this.x < this.xTo) {
                    this.x = (short) (this.x + i5);
                    this.dir = (short) 3;
                } else if (this.x > this.xTo) {
                    this.x = (short) (this.x - i5);
                    this.dir = (short) 2;
                } else if (this.y > this.yTo) {
                    this.y = (short) (this.y - i5);
                    this.dir = (short) 1;
                } else if (this.y < this.yTo) {
                    this.y = (short) (this.y + i5);
                    this.dir = (short) 0;
                }
                if (this.frame > 3) {
                    this.frame = (byte) 0;
                }
                this.weapon_frame = this.frame;
                if (this.idhorse != -1) {
                    int i6 = this.fh + 1;
                    this.fh = i6;
                    byte[] bArr2 = horseMove;
                    if (i6 > bArr2.length - 1) {
                        this.fh = 0;
                    }
                    this.Fhorse = bArr2[this.fh];
                }
            }
        } else if (b == 2) {
            SkillAnimate skillAnimate = this.currentSkillAnimate;
            if (skillAnimate != null) {
                skillAnimate.updateSkill(this);
            }
            if (this.ID == GCanvas.gameScr.mainChar.ID) {
                GameScr.isTarget = false;
            }
        } else if (b != 3) {
            if (b == 4 && this.comeHome) {
                long jCurrentTimeMillis = System.currentTimeMillis() - this.timeComeHome;
                if (jCurrentTimeMillis > 10000) {
                    resetBuff();
                    GameService.gI().comeHomeWhenDie();
                    this.comeHome = false;
                }
                int i7 = (int) (jCurrentTimeMillis / 1000);
                this.time = i7;
                int i8 = (i7 * 32) / 10;
                this.time = i8;
                if (i8 > 32) {
                    this.time = 32;
                }
            }
        } else {
            if (this.hp > 0 && this.realHP > 0) {
                this.state = (byte) 0;
            }
            short s3 = (short) (this.p1 + 1);
            this.p1 = s3;
            if (s3 == 40 && this.ID == GCanvas.gameScr.mainChar.ID) {
                GCanvas.msgdlg.isIcon = false;
                this.delay = -1;
                this.EffFace = null;
            }
            this.isAnimal = false;
            this.myAnimal = null;
        }
        updateHorse();
        updateBuff();
        super.update();
    }

    public boolean isLoadAnimalOk() {
        return (this.myAnimal == null || IsAnimal.img[this.myAnimal.idMonster] == null) ? false : true;
    }

    public void resetBuff() {
        this.buff.removeAllElements();
        int i = 0;
        while (true) {
            byte[] bArr = this.buffType;
            if (i >= bArr.length) {
                return;
            }
            bArr[i] = -1;
            this.lvBuff[i] = 0;
            this.countDown[i] = 0;
            i++;
        }
    }

    public void paintbuff(Graphics graphics) {
        int size = this.buff.size();
        for (int i = 0; i < size; i++) {
            ((CharBuff) this.buff.elementAt(i)).paint(graphics);
        }
    }

    public int fullSet() {
        if (this.legStyle == -1 || this.bodyStyle == -1) {
            return 0;
        }
        return this.currentHead == -1 ? 2 : 1;
    }

    public void paintEff(Graphics graphics, int i, int i2) {
        for (int i3 = 0; i3 < 8; i3++) {
            int i4 = i3 * 45;
            Util.paintColumColor(graphics, i, i2, 9, 193, 10, 100, 25, 1, 1, 1, 2, i4, 30);
            if (this.idColor == i3) {
                Util.paintColumColor(graphics, i, i2, 9, 228, 198, 34, 25, 1, 1, 1, 2, i4, 30);
            }
        }
        this.idColor = (this.idColor + 1) % 8;
    }

    @Override
    public boolean isPaint() {
        return this.x >= GameScr.cmx && this.x <= GameScr.cmx + GCanvas.w && this.y >= GameScr.cmy && this.y <= (GameScr.cmy + GCanvas.h) + 30;
    }

    public byte[] getOrderPaint(int i, int i2) {
        return ORDER_PAINT[1 ^ ((i == 1 || this.frame >= 4) ? (char) 0 : (char) 1)][i];
    }

    public boolean canPaint() {
        return (this.bodyStyle == -1 || this.legStyle == -1 || this.currentHead == -1) ? false : true;
    }

    public int getIdPartPaint(int part) {
        if (isLoadAnimalOk()) {
            return -1;
        }
        if (part == 1) {
            short body = this.bodyStyle;
            if (body == -1) {
                return -1;
            }
            short model = this.idModel[2];
            return model != -1 ? model : body;
        }
        if (part == 0) {
            byte horse = getHorse();
            short leg = this.legStyle;
            if (leg == -1 || horse != -1) {
                return -1;
            }
            short model = this.idModel[3];
            return model != -1 ? model : leg;
        }
        if (part == 2) {
            short head = this.currentHead;
            if (head == -1) {
                return -1;
            }
            short model = this.idModel[0];
            return model != -1 ? model : head;
        }
        if (part == 3) {
            short model = this.idModel[1];
            if (model != -1) {
                this.hatStyle = model;
            }
            short hat = this.hatStyle;
            if (hat == -1) {
                return -1;
            }
            return model != -1 ? model : hat;
        }
        if (part == 4 && this.coatStyle > -1) {
            return this.coatStyle;
        }
        return -1;
    }

    public int getDyAnimal(int type, int dir, int typeAnimal) {
        if (this.idhorse != -1) {
            return getdyParHorse();
        }
        byte horse = getHorse();
        switch (type) {
            case 0:
                return this.z + this.dy;
            case 1:
                if (horse <= -1) {
                    return this.z + this.dy;
                }
                if (typeAnimal != 0 && dir < 2) {
                    return this.z + ((dyb[horse][dir] - (dir == 1 ? 5 : 0)) + dyr[horse][dir][this.ifHorse]) + this.dy;
                }
                return this.z + (dyb[horse][dir] + dyr[horse][dir][this.ifHorse]) + this.dy;
            case 2:
                return this.z + (horse != -1 ? (dyb[horse][dir] - ((dir == 1 && this.anmFly == 1) ? 5 : 0)) + dyr[horse][dir][this.ifHorse] : 0) + this.dy;
            case 3:
                return ((this.z + (horse != -1 ? dyb[horse][dir] + dyr[horse][dir][this.ifHorse] : 0)) - ((dir == 1 && this.anmFly == 1) ? 5 : 0)) + this.dy;
            case 4:
                return this.z + (horse != -1 ? dyb[horse][dir] + dyr[horse][dir][this.ifHorse] : 0) + this.dy;
            default:
                return 0;
        }
    }

    public int getdxHorse() {
        short s = this.typehorse;
        if (s != -1) {
            return dx_horse[s][this.dir];
        }
        return 0;
    }

    public int getDxAnimal(int i, int i2, int i3) {
        byte b;
        if (this.idhorse != -1) {
            return getdxHorse();
        }
        byte horse = getHorse();
        if (i == 1) {
            if (horse <= -1 || i2 <= 1) {
                return 0;
            }
            return dxr[horse][i2][this.ifHorse];
        }
        if (i != 2) {
            if (i != 3) {
                if (i != 4 || horse == -1) {
                    return 0;
                }
                b = dxr[horse][i2][this.ifHorse];
            } else {
                if (horse == -1) {
                    return 0;
                }
                b = dxr[horse][i2][this.ifHorse];
            }
        } else {
            if (horse == -1) {
                return 0;
            }
            b = dxr[horse][i2][this.ifHorse];
        }
        return b;
    }

    public int getFrameHorse() {
        int i;
        int i2;
        if (this.dir == 0) {
            i = this.Fhorse;
            i2 = this.frameThuCuoi;
        } else if (this.dir == 1) {
            i = this.Fhorse + 7;
            i2 = this.frameThuCuoi;
        } else {
            i = this.Fhorse + 14;
            i2 = this.frameThuCuoi;
        }
        return i + (i2 * 21);
    }

    @Override
    public void paint(Graphics graphics) {
        this.infoPaint[2] = this.currentHead;
        if (!isPaint()) {
            return;
        }
        boolean hasHorse = this.idhorse > -1 || this.useHorse != -1;
        try {
            if (!this.isFreeze) {
                int horse = getHorse();
                if (GameData.imgHorse == null) {
                    horse = -1;
                }
                if (this.effPhap != null) {
                    this.effPhap.x = this.x;
                    this.effPhap.y = this.y;
                    if (!isLoadAnimalOk() && horse != -1) {
                        this.effPhap.x += dxs[horse][this.dir];
                        this.effPhap.y -= this.anmFly == 0 ? 3 : -6;
                    }
                    this.effPhap.paint(graphics);
                }
                if (this.state == 3) {
                    graphics.drawImage(Res.imgDie, this.x - 10, (this.y - 27) + this.dy, 0);
                    if (GCanvas.gameTick % 20 != 17 && GCanvas.gameTick % 20 != 13) {
                        graphics.drawImage(Res.imgEye, this.x - 4, (this.y - 19) + this.dy, 0);
                    }
                    return;
                }
                paintDataEff_Top(graphics, this.x, (this.y - (this.anmFly == 0 ? 0 : 6)) + getdyParHorse(), hasHorse);
                paintListEff(graphics, 0);
                if (this.dir == 0 || this.dir == 2 || this.dir == 3) {
                    int unusedFrame = this.frame;
                }
                int dyMount = 0;
                int dyBody = 0;
                if (!isLoadAnimalOk() && horse != -1) {
                    this.z = 0;
                    if (this.anmFly == 1) {
                        byte[] dyMountFly = {-1, -1, -5, -1};
                        int ifh = this.ifHorse;
                        dyMount = dyMountFly[ifh];
                        byte[] dyBodyFly = {-1, -2, -3, -2};
                        dyBody = dyBodyFly[ifh];
                    }
                }
                byte[] order = getOrderPaint(this.dir, this.anmFly);
                if (!isLoadAnimalOk() && can_Paint()) {
                    for (int i = 0; i < order.length; i++) {
                        if (getIdPartPaint(order[i]) > -1 || order[i] == 6 || ((order[i] == 4 && this.idPhiPhong != -1) || ((order[i] == 5 && this.infoPaint[order[i]] > -1) || this.idWeapon != -1))) {
                            if (order[i] == 5) {
                                paintWeapon(graphics, dyBody);
                            } else {
                                if (order[i] == 0 && this.dynamicEffBottom != null) {
                                    for (int j = 0; j < this.dynamicEffBottom.size(); j++) {
                                        ((DynamicEffect) this.dynamicEffBottom.elementAt(j)).paint(graphics, this.x, this.y);
                                    }
                                }
                                byte part = order[i];
                                if (part == 0 && this.idhorse != -1) {
                                    // không vẽ
                                } else if (part == 6 && this.idhorse != -1) {
                                    paintMount(graphics, dyMount);
                                    if (horse > -1) {
                                        graphics.drawImage(Res.imgShadow, this.x + dxs[horse][this.dir], this.y - (this.anmFly == 0 ? 3 : -6), Graphics.HCENTER | Graphics.VCENTER);
                                    }
                                } else if (part == 2 && this.idHair != -1) {
                                    paintHair(graphics, dyMount);
                                } else if (part == 0 && this.idLeg != -1) {
                                    paintLeg(graphics, dyBody);
                                } else if (part == 4 && this.idPhiPhong != -1) {
                                    paintPP(graphics, dyBody);
                                } else if (part == 1) {
                                    if (getPartEff(this.idhorse) != null) {
                                        if (this.idBody == -1) {
                                            Res.getCharPartInfo(order[i], getIdPartPaint(order[i])).paint(graphics, this.x + getDxAnimal(order[i], this.dir, this.anmFly), this.y + dyMount + getDyAnimal(order[i], this.dir, this.anmFly), this.dir, this.frame);
                                        } else {
                                            paintBody(graphics, dyBody);
                                        }
                                    }
                                    if (horse > -1) {
                                        if (this.dir < 2 && this.anmFly == 0) {
                                            if (this.idBody == -1) {
                                                Res.getCharPartInfo(order[i], getIdPartPaint(order[i])).paint(graphics, this.x + getDxAnimal(order[i], this.dir, this.anmFly), this.y + dyMount + getDyAnimal(order[i], this.dir, this.anmFly), this.dir, this.frame);
                                            } else {
                                                paintBody(graphics, dyBody);
                                            }
                                        }
                                        if (this.dynamicEffBottom != null) {
                                            for (int j = 0; j < this.dynamicEffBottom.size(); j++) {
                                                ((DynamicEffect) this.dynamicEffBottom.elementAt(j)).paint(graphics, this.x, this.y);
                                            }
                                        }
                                        if (GameData.imgHorse.size() > 0) {
                                            graphics.drawRegion((Bitmap) GameData.imgHorse.elementAt(this.idImgHorse), fHorse[horse][this.dir][this.ifHorse], 0, sizeHorse[horse][this.dir][this.ifHorse], hHorse[horse], 0, this.x + dxh[horse][this.dir], this.y + dyh[horse][this.dir] + this.dy, Graphics.HCENTER | Graphics.BOTTOM);
                                        }
                                        if (this.dir > 1 && this.anmFly == 0) {
                                            graphics.drawImage(Res.imgLeg[this.dir - 2], (this.x - (this.dir == 2 ? -2 : 1)) + dxr[horse][this.dir][this.ifHorse], (this.y - 10) + dyr[horse][this.dir][this.ifHorse] + this.dy, 3);
                                        }
                                        if ((this.dir > 1 && this.anmFly == 0) || this.anmFly == 1) {
                                            if (this.idBody == -1) {
                                                Res.getCharPartInfo(order[i], getIdPartPaint(order[i])).paint(graphics, this.x + getDxAnimal(order[i], this.dir, this.anmFly), this.y + dyMount + getDyAnimal(order[i], this.dir, this.anmFly), this.dir, this.frame);
                                            } else {
                                                paintBody(graphics, dyBody);
                                            }
                                        }
                                    } else if (this.idhorse == -1) {
                                        if (this.idBody == -1) {
                                            Res.getCharPartInfo(order[i], getIdPartPaint(order[i])).paint(graphics, this.x + getDxAnimal(order[i], this.dir, this.anmFly), this.y + dyMount + getDyAnimal(order[i], this.dir, this.anmFly), this.dir, this.frame);
                                        } else {
                                            paintBody(graphics, dyBody);
                                        }
                                    }
                                } else {
                                    boolean canDraw = part == 3 ? this.paintHat : true;
                                    if (canDraw) {
                                        Res.getCharPartInfo(part, getIdPartPaint(part)).paint(graphics, this.x + getDxAnimal(order[i], this.dir, this.anmFly), this.y + dyMount + getDyAnimal(order[i], this.dir, this.anmFly), this.dir, this.frame);
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoadAnimalOk() && this.myAnimal != null) {
                    this.myAnimal.paint(graphics, this.x, this.y + this.z + this.dy);
                    graphics.drawImage(Res.imgShadow, this.x, this.y - 2, Graphics.HCENTER | Graphics.VCENTER);
                }
                if (this.isAnimal && this.effAnimal != null) {
                    this.effAnimal.x = this.x;
                    this.effAnimal.y = this.y;
                    this.effAnimal.paint(graphics);
                }
                if (this.comeHome) {
                    graphics.setColor(0);
                    graphics.fillRect(this.x - 16, this.y - 46, 32, 4);
                    graphics.setColor(0x11C7B4);
                    graphics.fillRect(this.x - 16, this.y - 46, this.time, 4);
                    graphics.setColor(0x33510D);
                    graphics.drawRect(this.x - 16, this.y - 46, 32, 4);
                }
                paintbuff(graphics);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.paint(graphics);
        if (this.EffFace != null && !this.isFreeze) {
            this.EffFace.paint(graphics);
        }
        if (isNPC()) {
            Bitmap imgQuest = GameScr.getImgQuest(this.idBoss);
            if (!imgQuest.equals(Res.imgSelect)) {
                graphics.drawImage(imgQuest, this.x, (this.y - this.height) - 5, Graphics.HCENTER | Graphics.BOTTOM);
            }
        }
        if (this.dynamicEffTop != null) {
            for (int i = 0; i < this.dynamicEffTop.size(); i++) {
                ((DynamicEffect) this.dynamicEffTop.elementAt(i)).paint(graphics, this.x, this.y);
            }
        }
        paintDataEff_Bot(graphics, this.x, (this.y - (this.anmFly == 0 ? 0 : 6)) + getdyParHorse(), hasHorse);
        paintListEff(graphics, 1);
    }

    public void paintCharList(Graphics g) {
        int horse = getHorse();
        if (GameData.imgHorse == null) {
            horse = -1;
        }
        if (this.effPhap != null) {
            this.effPhap.x = this.x;
            this.effPhap.y = this.y;
            if (!isLoadAnimalOk() && horse != -1) {
                this.effPhap.x += dxs[horse][this.dir];
                this.effPhap.y -= this.anmFly == 0 ? 3 : -6;
            }
            this.effPhap.paint(g);
        }
        if (this.state == 3) {
            g.drawImage(Res.imgDie, this.x - 10, this.y - 27 + this.dy, 0);
            if (GCanvas.gameTick % 20 != 17 && GCanvas.gameTick % 20 != 13) {
                g.drawImage(Res.imgEye, this.x - 4, this.y - 19 + this.dy, 0);
            }
            return;
        }
        boolean weaponFirst = (this.dir == 0 || this.dir == 2 || this.dir == 3) && this.frame < 4;
        int dyHorse = 0;
        int dyWeapon = 0;
        if (!isLoadAnimalOk() && horse != -1) {
            this.z = 0;
            if (this.anmFly == 1) {
                int f = this.ifHorse;
                dyHorse = new byte[]{-1, -1, -5, -1}[f];
                dyWeapon = new byte[]{-1, -2, -3, -2}[f];
            }
        }
        if (!isLoadAnimalOk() && weaponFirst) {
            paintWeapon(g, dyWeapon);
        }
        if (!isLoadAnimalOk()) {
            short leg = this.legStyle;
            if (leg != -1 && horse == -1) {
                short legModel = this.idModel[3];
                if (legModel != -1) {
                    leg = legModel;
                }
                Res.getCharPartInfo(0, leg).paint(g, this.x, this.y + this.z + this.dy, this.dir, this.frame);
            }
        }
        if (!isLoadAnimalOk() && this.bodyStyle != -1) {
            short body = this.bodyStyle;
            short bodyModel = this.idModel[2];
            if (bodyModel != -1) {
                body = bodyModel;
            }
            if (horse != -1) {
                if (this.dir < 2 && this.anmFly == 0) {
                    Res.getCharPartInfo(1, body).paint(g, this.x, dyHorse + this.y + this.z + (dyb[horse][this.dir] + dyr[horse][this.dir][this.ifHorse]) + this.dy, this.dir, this.frame);
                }
                g.drawImage(Res.imgShadow, this.x + dxs[horse][this.dir], this.y - (this.anmFly == 0 ? 3 : -6), Graphics.HCENTER | Graphics.VCENTER);
                if (GameData.imgHorse.size() > 0) {
                    g.drawRegion((Bitmap) GameData.imgHorse.elementAt(this.idImgHorse), fHorse[horse][this.dir][this.ifHorse], 0, sizeHorse[horse][this.dir][this.ifHorse], hHorse[horse], 0, this.x + dxh[horse][this.dir], this.y + dyh[horse][this.dir] + this.dy, Graphics.HCENTER | Graphics.BOTTOM);
                }
                if (this.dir > 1 && this.anmFly == 0) {
                    g.drawImage(Res.imgLeg[this.dir - 2], this.x - (this.dir == 2 ? -2 : 1) + dxr[horse][this.dir][this.ifHorse], this.y - 10 + dyr[horse][this.dir][this.ifHorse] + this.dy, 3);
                }
                if (this.dir > 1 && this.anmFly == 0) {
                    Res.getCharPartInfo(1, body).paint(g, this.x + dxr[horse][this.dir][this.ifHorse], dyHorse + this.y + this.z + (dyb[horse][this.dir] + dyr[horse][this.dir][this.ifHorse]) + this.dy, this.dir, this.frame);
                } else if (this.anmFly == 1) {
                    if (this.dir < 2) {
                        Res.getCharPartInfo(1, body).paint(g, this.x, dyHorse + this.y + this.z + ((dyb[horse][this.dir] - ((this.dir == 1 && this.anmFly == 1) ? 5 : 0)) + dyr[horse][this.dir][this.ifHorse]) + this.dy, this.dir, this.frame);
                    } else {
                        Res.getCharPartInfo(1, body).paint(g, this.x + dxr[horse][this.dir][this.ifHorse], dyHorse + this.y + this.z + (dyb[horse][this.dir] + dyr[horse][this.dir][this.ifHorse]) + this.dy, this.dir, this.frame);
                    }
                }
            } else if (!isLoadAnimalOk()) {
                Res.getCharPartInfo(1, body).paint(g, this.x, dyHorse + this.y + this.z + this.dy, this.dir, this.frame);
            }
        }
        if (!isLoadAnimalOk() && this.currentHead != -1) {
            short head = this.currentHead;
            short headModel = this.idModel[0];
            if (headModel != -1) {
                head = headModel;
            }
            Res.getCharPartInfo(2, head).paint(g,
                    this.x + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : 0),
                    dyHorse + this.y + this.z + (horse != -1 ? (dyb[horse][this.dir] - ((this.dir == 1 && this.anmFly == 1) ? 5 : 0)) + dyr[horse][this.dir][this.ifHorse] : 0) + this.dy,
                    this.dir, this.frame);
        }
        if (!isLoadAnimalOk()) {
            short hatModel = this.idModel[1];
            if (hatModel != -1) {
                this.hatStyle = hatModel;
            }
        }
        if (this.hatStyle != -1) {
            short hat = this.hatStyle;
            short[] model = this.idModel;
            short hatModel = model[1];
            if (hatModel != -1) {
                hat = hatModel;
            }
            short headModel = model[0];
            if (headModel != 26 || hatModel == 50) {
                Res.getCharPartInfo(3, hat).paint(g,
                        this.x + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : 0),
                        dyHorse + this.y + this.z + (horse != -1 ? dyb[horse][this.dir] + dyr[horse][this.dir][this.ifHorse] : 0) + this.dy,
                        this.dir, this.frame);
            }
        }
        if (!isLoadAnimalOk() && !weaponFirst) {
            paintWeapon(g, dyWeapon);
        }
        if (isLoadAnimalOk() && this.myAnimal != null) {
            this.myAnimal.paint(g, this.x, this.y + this.z + this.dy);
            g.drawImage(Res.imgShadow, this.x, this.y - 2, Graphics.HCENTER | Graphics.VCENTER);
        }
        if (this.isAnimal && this.effAnimal != null) {
            this.effAnimal.x = this.x;
            this.effAnimal.y = this.y;
            this.effAnimal.paint(g);
        }
        if (this.comeHome) {
            g.setColor(0);
            g.fillRect(this.x - 16, this.y - 46, 32, 4);
            g.setColor(0xFF11C7B4);
            g.fillRect(this.x - 16, this.y - 46, this.time, 4);
            g.setColor(0xFF33510D);
            g.drawRect(this.x - 16, this.y - 46, 32, 4);
        }
        paintbuff(g);
        try {
            super.paint(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.EffFace != null && !this.isFreeze) {
            this.EffFace.paint(g);
        }
    }

    @Override
    public void paintName(Graphics graphics) {
        short s;
        if (Tilemap.isOfflineMap) {
            return;
        }
        if (this.name != null && GCanvas.gameScr.hideGUI != 2) {
            int i = -((getHorse() == -1 || GameData.imgHorse == null) ? 2 : 10);
            if (this.idhorse != -1) {
                i = dy_horse[0][this.dir];
            }
            if (this.idClan != -1) {
                byte b = this.isMaster;
                if (b >= 0 && b < 3) {
                    int[][] iArr = {new int[]{-326640, -2228988}, new int[]{-2228988, -326640}, new int[]{-15727363, -326640}};
                    ImageIcon imgIcon = GameData.getImgIcon((short) (this.idClan + 1000));
                    if (imgIcon != null) {
                        if (!imgIcon.isLoad) {
                            int width = ((this.x - (this.wName / 2)) - (imgIcon.img.getWidth() / 2)) - 6;
                            int height = (((this.y - this.height) - (imgIcon.img.getHeight() / 2)) + i) - 7;
                            int width2 = imgIcon.img.getWidth() + 1;
                            int height2 = imgIcon.img.getHeight() + 1;
                            int i2 = count;
                            int[] iArr2 = iArr[this.isMaster];
                            Res.paintRectColor(graphics, width, height, width2, height2, i2, iArr2[0], iArr2[1]);
                        }
                    } else if (!imgIcon.isLoad) {
                        int i3 = (this.x - (this.wName / 2)) - 6;
                        int i4 = ((this.y - this.height) + i) - 7;
                        int i5 = count;
                        int[] iArr3 = iArr[this.isMaster];
                        Res.paintRectColor(graphics, i3, i4, 10, 10, i5, iArr3[0], iArr3[1]);
                    }
                    int i6 = count + 3;
                    count = i6;
                    if (i6 > 46) {
                        count = 0;
                    }
                }
                GameData.paintIcon(graphics, (short) (this.idClan + 1000), (this.x - (this.wName / 2)) - 5, ((this.y - this.height) - 6) + i);
                s = 8;
            } else {
                s = 0;
            }
            if (this.nationID > -1 && this.idBoss == -1 && ((this.nationID != this.inCountry || this.ID == GCanvas.gameScr.mainChar.ID) && (this.nationID != this.inCountry || this.ID == GCanvas.gameScr.mainChar.ID))) {
                graphics.drawRegion(Res.imgNation, 0, this.nationID * 11, 11, 11, 0, this.x + (this.wName / 2) + 9, ((this.y - this.height) - 12) + i, 20);
            }
            if (this.ID == GCanvas.gameScr.mainChar.ID || this.nationID == GCanvas.gameScr.mainChar.nationID || this.idBoss != -1) {
                Font.verySmallFont.drawString(graphics, this.name, this.x + s, ((this.y - 40) + i) - this.dyThanthu, 2);
            } else {
                Font.verySmallFont_Red.drawString(graphics, this.name, this.x + s, ((this.y - 43) + i) - this.dyThanthu, 2);
            }
            short s2 = this.isNo ? (short) 5 : (short) 0;
            short s3 = -5;
            if (this.pk > 0) {
                graphics.drawRegion(imgFlgPk, 0, (this.pk - 14) * 10, 11, 10, 0, this.x + s2, (this.y - 41) + i, Graphics.BOTTOM | Graphics.HCENTER);
            } else if (this.killer > 0 || this.iskiller) {
                graphics.drawRegion(imgFlgPk, 0, 50, 11, 10, 0, this.x + s2, (this.y - 41) + i, Graphics.BOTTOM | Graphics.HCENTER);
            } else {
                s3 = 0;
            }
            if (this.isNo) {
                Res.imgNo.drawFrame(this.countNo / 5, this.x + s3, ((this.y - 41) + i) - 2, 0, Graphics.BOTTOM | Graphics.HCENTER, graphics);
            }
            byte b2 = (byte) (this.countNo + 1);
            this.countNo = b2;
            if (b2 >= 10) {
                this.countNo = (byte) 0;
            }
        }
        super.paintName(graphics);
    }

    public int getdyParHorse() {
        short s = this.typehorse;
        if (s != -1) {
            return dy_horse[s][this.dir];
        }
        return 0;
    }

    public int getFramePP() {
        int i;
        byte b;
        if (this.dir == 0) {
            i = this.frame;
            b = this.framePP;
        } else if (this.dir == 1) {
            i = (this.dir * 6) + this.frame;
            b = this.framePP;
        } else {
            i = this.frame + 12;
            b = this.framePP;
        }
        return i + (b * 18);
    }

    public int getFrameBody() {
        int i;
        byte b;
        if (this.dir == 0) {
            i = this.frame;
            b = this.Framebody;
        } else if (this.dir == 1) {
            i = (this.dir * 6) + this.frame;
            b = this.Framebody;
        } else {
            i = this.frame + 12;
            b = this.Framebody;
        }
        return i + (b * 18);
    }

    public int getFrameLeg() {
        int i;
        byte b;
        if (this.dir == 0) {
            i = this.frame;
            b = this.frameLeg;
        } else if (this.dir == 1) {
            i = (this.dir * 6) + this.frame;
            b = this.frameLeg;
        } else {
            i = this.frame + 12;
            b = this.frameLeg;
        }
        return i + (b * 18);
    }

    public int getFrameHair() {
        int i;
        byte b;
        if (this.dir == 0) {
            i = this.frame;
            b = this.FrameHair;
        } else if (this.dir == 1) {
            i = (this.dir * 6) + this.frame;
            b = this.FrameHair;
        } else {
            i = this.frame + 12;
            b = this.FrameHair;
        }
        return i + (b * 18);
    }

    public int getFrameWP() {
        int i;
        byte b;
        if (this.dir == 0) {
            i = this.frame;
            b = this.FrameWP;
        } else if (this.dir == 1) {
            i = (this.dir * 6) + this.frame;
            b = this.FrameWP;
        } else {
            i = this.frame + 12;
            b = this.FrameWP;
        }
        return i + (b * 18);
    }

    public void paintVK(Graphics graphics, int i) {
        byte horse = getHorse();
        if (GameData.imgHorse == null) {
            horse = -1;
        }
        DataSkillEff partEff = getPartEff(this.idWeapon);
        if (partEff != null && GCanvas.gameTick % 5 == 0) {
            int size = partEff.listFrame.size() / 18;
            if (size == 0) {
                size = 1;
            }
            this.FrameWP = (byte) ((this.FrameWP + 1) % size);
        }
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), this.y + (horse != -1 ? dyb[horse][this.dir] : (byte) 0) + getdyParHorse() + i, getFrameWP(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), this.y + (horse != -1 ? dyb[horse][this.dir] : (byte) 0) + getdyParHorse() + i, getFrameWP(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintHair(Graphics graphics, int i) {
        getHorse();
        Vector vector = GameData.imgHorse;
        DataSkillEff partEff = getPartEff(this.idHair);
        if (partEff != null && GCanvas.gameTick % 6 == 0) {
            int size = partEff.listFrame.size() / 18;
            if (size == 0) {
                size = 1;
            }
            this.FrameHair = (byte) ((this.FrameHair + 1) % size);
        }
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(2, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(2, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameHair(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(2, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(2, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameHair(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintMount(Graphics graphics, int i) {
        DataSkillEff partEff = getPartEff(this.idhorse);
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x, this.y, getFrameHorse(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x, this.y, getFrameHorse(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintBody(Graphics graphics, int i) {
        getHorse();
        Vector vector = GameData.imgHorse;
        DataSkillEff partEff = getPartEff(this.idBody);
        if (partEff != null && GCanvas.gameTick % 6 == 0) {
            int size = partEff.listFrame.size() / 18;
            if (size == 0) {
                size = 1;
            }
            this.Framebody = (byte) ((this.Framebody + 1) % size);
        }
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(1, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(1, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameBody(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(1, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(1, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameBody(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintLeg(Graphics graphics, int i) {
        getHorse();
        Vector vector = GameData.imgHorse;
        DataSkillEff partEff = getPartEff(this.idLeg);
        if (partEff != null && GCanvas.gameTick % 6 == 0) {
            int size = partEff.listFrame.size() / 18;
            if (size == 0) {
                size = 1;
            }
            this.frameLeg = (byte) ((this.frameLeg + 1) % size);
        }
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(0, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(0, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameLeg(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(0, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(0, this.dir, this.anmFly)) + getdyParHorse() + i, getFrameLeg(), this.dir == 3 ? 2 : 0);
        }
    }

    public void paintPP(Graphics graphics, int i) {
        getHorse();
        Vector vector = GameData.imgHorse;
        DataSkillEff partEff = getPartEff(this.idPhiPhong);
        if (partEff != null && GCanvas.gameTick % 10 == 0) {
            int size = partEff.listFrame.size() / 18;
            if (size == 0) {
                size = 1;
            }
            this.framePP = (byte) ((this.framePP + 1) % size);
        }
        if (partEff != null) {
            partEff.paintTopHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(4, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(4, this.dir, this.anmFly)) + getdyParHorse() + i, getFramePP(), this.dir == 3 ? 2 : 0);
            partEff.paintBottomHorse(graphics, this.x + (this.idhorse != -1 ? 0 : getDxAnimal(4, this.dir, this.anmFly)) + getdxHorse(), this.y + (this.idhorse != -1 ? 0 : getDyAnimal(4, this.dir, this.anmFly)) + getdyParHorse() + i, getFramePP(), this.dir == 3 ? 2 : 0);
        }
    }

    private void paintWeapon(Graphics graphics, int i) {
        if (this.idWeapon != -1) {
            paintVK(graphics, i);
            return;
        }
        int i2 = this.weaponType - 3;
        if (i2 >= 0) {
            if (i2 >= 5) {
                return;
            }
            byte horse = getHorse();
            if (GameData.imgHorse == null) {
                horse = -1;
            }
            byte b = this.clazz;
            byte b2 = this.weaponIndex;
            if (this.idModel[4] > -1) {
                i2 = 1;
                b = 1;
                b2 = 0;
            }
            if (this.isDoing) {
                i2 = 3;
                b = 3;
                b2 = 0;
            }
            byte[][] bArr = skill_type;
            byte b3 = bArr[b][this.currentSkillType];
            if (this.state != 2) {
                b3 = 0;
            }
            if (this.weapon_frame < 0 || i2 < 0) {
                return;
            }
            WPSplashInfo wPSplashInfo = Res.wpSplashInfos[i2][b3];
            if (wPSplashInfo == null) {
                Res.GetWPSplashInfo(i2, b3);
                return;
            }
            int i3 = this.weaponStyle;
            short s = this.idModel[4];
            if (s > -1 && !this.isDoing) {
                i3 = s;
            }
            if (this.isDoing) {
                i3 = 0;
            }
            if (this.state != 2) {
                if (this.weapon_frame > 1) {
                    this.weapon_frame = (short) 0;
                }
                if (this.weapon_frame < 0) {
                    this.weapon_frame = (short) 0;
                }
                if (i3 != -1 && Res.loadImgWeaPone(b, i3, b2) != null) {
                    WeaponInfo weaponInfoLoadImgWeaPone = Res.loadImgWeaPone(b, i3, b2);
                    if (this.idModel[4] > -1 && !this.isDoing) {
                        i3 = 4;
                    }
                    int i4 = i3 * 4;
                    graphics.drawRegion(weaponInfoLoadImgWeaPone.img, pWeapone[b][this.dir + i4], 0, sw[b][this.dir + i4], hwp[b], tWeapone[b][this.dir], this.x + dwx[b][this.dir + i4] + wPSplashInfo.PF_X[this.dir][this.weapon_frame] + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : (byte) 0) + getdxHorse(), (((((((i + this.y) + dwy[b][this.dir + i4]) + wPSplashInfo.PF_Y[this.dir][this.weapon_frame]) + ((this.dir == 2 && this.weapon_frame == 1) ? fixY[b] : (byte) 0)) + this.z) + (horse != -1 ? dyb[horse][this.dir] : (byte) 0)) - ((this.dir == 1 && this.anmFly == 1) ? 5 : 0)) + this.dy + getdyParHorse(), 0);
                }
            } else {
                if (!this.isDoing) {
                    int i5 = this.weaponType - 3;
                    byte b4 = bArr[b][this.currentSkillType];
                    b = this.clazz;
                    wPSplashInfo = Res.wpSplashInfos[i5][b4];
                    if (wPSplashInfo == null) {
                        Res.GetWPSplashInfo(i5, b4);
                        return;
                    }
                }
                if (this.weapon_frame < wPSplashInfo.P0_X[this.dir].length && wPSplashInfo.image != null) {
                    graphics.drawRegion(wPSplashInfo.image, wPSplashInfo.P0_X[this.dir][this.weapon_frame], wPSplashInfo.P0_Y[this.dir][this.weapon_frame], wPSplashInfo.PF_W[this.dir][this.weapon_frame], wPSplashInfo.PF_H[this.dir][this.weapon_frame], 0, this.x + wPSplashInfo.PF_X[this.dir][this.weapon_frame] + (horse != -1 ? dxr[horse][this.dir][this.ifHorse] : (byte) 0), i + this.y + wPSplashInfo.PF_Y[this.dir][this.weapon_frame] + this.z + (horse != -1 ? dyb[horse][this.dir] : (byte) 0) + this.dy, 0);
                }
            }
            if (this.state == 0 || this.state == 1 || this.state == 4) {
                int styleWeapon = getStyleWeapon();
                if (GCanvas.gameTick % 2 == 0) {
                    this.idLight = (this.idLight + 1) % lightFrame.length;
                }
                if (styleWeapon > 1) {
                    int i6 = styleWeapon - 1;
                    paintEffectVukhi(graphics, i6 <= 3 ? i6 : 3, wPSplashInfo, b);
                }
            }
        }
    }

    private void paintEffectVukhi(Graphics graphics, int i, WPSplashInfo wPSplashInfo, int i2) {
        try {
            graphics.drawRegion(imgLight, 0, (i * 28) + (lightFrame[this.idLight] * 7), 7, 7, 0, this.x + countX[i2][this.dir], this.y + 5 + ((getHorse() == -1 || GameData.imgHorse == null) ? 9 : 0) + wPSplashInfo.PF_Y[this.dir][this.weapon_frame] + countY[i2][this.dir] + this.dy, 0);
        } catch (Exception unused) {
            Cout.println("hieu ung vukhi char");
        }
    }

    public void paintAvatar(Graphics graphics, short s, short s2) {
        byte b;
        ImageIcon imgIcon;
        Bitmap bitmap;
        ImageIcon imgIcon2;
        short s3 = this.legStyle;
        short s4 = this.bodyStyle;
        short s5 = this.currentHead;
        short s6 = this.hatStyle;
        short s7 = this.coatStyle;
        short[] sArr = this.idModel;
        short s8 = sArr[0];
        short s9 = s8 != -1 ? s8 : s5;
        short s10 = sArr[1];
        short s11 = s10 != -1 ? s10 : s6;
        short s12 = sArr[2];
        short s13 = s12 != -1 ? s12 : s4;
        short s14 = sArr[3];
        short s15 = s14 != -1 ? s14 : s3;
        if (s7 != -1) {
            short s16 = this.idImgPP;
            if (s16 == -1) {
                Res.getCharPartInfo(4, s7).paintAvatar(graphics, s, s2, this.frame);
            } else {
                ImageIcon imgIcon3 = GameData.getImgIcon(s16);
                if (imgIcon3 != null && imgIcon3.img != null) {
                    if (GCanvas.gameTick % 5 == 0) {
                        this.ppAvartar = (byte) ((this.ppAvartar + 1) % this.nFramePP);
                    }
                    Bitmap bitmap2 = imgIcon3.img;
                    int height = (imgIcon3.img.getHeight() / this.nFramePP) * this.ppAvartar;
                    int width = imgIcon3.img.getWidth();
                    int height2 = imgIcon3.img.getHeight() / this.nFramePP;
                    byte[][] bArr = Dx_Dy_PP;
                    byte[] bArr2 = bArr[0];
                    byte b2 = this.indexPP;
                    graphics.drawRegion(bitmap2, 0, height, width, height2, 0, s + bArr2[b2], s2 + bArr[1][b2], 0);
                }
            }
        }
        if (s15 != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(0, s15).paintAvatar(graphics, s, s2, this.frame);
        }
        short s17 = s13;
        if (s17 == -1 || this.idImgAVT != -1) {
            b = 1;
        } else {
            b = 1;
            Res.getCharPartInfo(1, s17).paintAvatar(graphics, s, s2, this.frame);
        }
        short s18 = s9;
        if (s18 != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(2, s18).paintAvatar(graphics, s, s2, this.frame);
        }
        short s19 = s11;
        if (s19 != -1 && this.idImgAVT == -1) {
            Res.getCharPartInfo(3, s19).paintAvatar(graphics, s, s2, this.frame);
        }
        short s20 = this.idImgAVT;
        if (s20 != -1 && (imgIcon2 = GameData.getImgIcon(s20)) != null && imgIcon2.img != null) {
            if (GCanvas.gameTick % 5 == 0) {
                this.AVT = (byte) ((this.AVT + b) % this.nFrameAVT);
            }
            Bitmap bitmap3 = imgIcon2.img;
            int height3 = this.AVT * (imgIcon2.img.getHeight() / this.nFrameAVT);
            int width2 = imgIcon2.img.getWidth();
            int height4 = imgIcon2.img.getHeight() / this.nFrameAVT;
            byte[][] bArr3 = Dx_Dy_AVT;
            byte[] bArr4 = bArr3[0];
            byte b3 = this.indexAVT;
            graphics.drawRegion(bitmap3, 0, height3, width2, height4, 0, s + bArr4[b3], s2 + bArr3[b][b3], 0);
        }
        if (this.weaponType != -1 && this.weaponStyle != -1 && this.idImageWeapon == -1 && (bitmap = this.imgWpa) != null) {
            graphics.drawImage(bitmap, this.dxWear + s, this.dyWear + s2, 0);
        }
        short s21 = this.idImageWeapon;
        if (s21 == -1 || (imgIcon = GameData.getImgIcon(s21)) == null || imgIcon.img == null) {
            return;
        }
        if (GCanvas.gameTick % 5 == 0) {
            this.wpAvartar = (byte) ((this.wpAvartar + b) % this.nFrameWP);
        }
        Bitmap bitmap4 = imgIcon.img;
        int height5 = this.wpAvartar * (imgIcon.img.getHeight() / this.nFrameWP);
        int width3 = imgIcon.img.getWidth();
        int height6 = imgIcon.img.getHeight() / this.nFrameWP;
        byte[][] bArr5 = Dx_Dy_WP;
        byte[] bArr6 = bArr5[0];
        short s22 = this.typeWeapon;
        graphics.drawRegion(bitmap4, 0, height5, width3, height6, 0, s + bArr6[s22], s2 + bArr5[b][s22], 0);
    }

    @Override
    public void setPosTo(short s, short s2) {
        moveTo(s, s2);
    }

    public void setCurrentSkillByID(byte b) {
        this.currentSkillType = b;
        if (this.isDoing) {
            this.currentSkillAnimate = SkillManager.getSkillAnimate(0, this.clazz);
        } else {
            this.currentSkillAnimate = SkillManager.getSkillAnimate(b, this.clazz);
        }
        long[] jArr = this.coolDownSkill;
        byte b2 = this.currentSkillType;
        jArr[b2] = SkillManager.getSkillCooldown(b2, this.clazz, skillLevelLearnt[b]);
    }

    public byte getSkillIDFromType(byte b) {
        return (byte) ((b * 10) + skillLevelLearnt[b]);
    }

    @Override
    public int checkMagicShield(int i) {
        if (this.clazz != 2 || this.buffType[3] == -1) {
            return 0;
        }
        int i2 = (i * SkillManager.SKILL_DAM_PERCENT[this.clazz][7][this.lvBuff[3]]) / 100;
        int i3 = this.mp;
        return i2 > i3 ? i3 : i2;
    }

    public void startAttack(LiveActor liveActor, AttackResult attackResult, byte b, byte b2) {
        setCurrentSkillByID(b);
        this.currentSkillAnimate.setLvSkill(b2);
        this.attackTarget = liveActor;
        this.timeLastUseSkills[this.currentSkillType] = System.currentTimeMillis();
        this.state = (byte) 2;
        this.attkPower = 0;
        this.currentSkillAnimate.getX2();
        this.attkPower = attackResult.dam;
        this.attkEffect = attackResult.effect;
        this.p3 = (short) 0;
        this.p2 = (short) 0;
        this.p1 = (short) 0;
    }

    public int getBuffEffAttack() {
        return (this.clazz == 0 && skillLevelLearnt[4] > 0 && new Random(System.currentTimeMillis()).nextInt(100) < SkillManager.SKILL_DAM_PERCENT[0][4][skillLevelLearnt[4]]) ? 0 : -1;
    }

    public void setWearingInfo(Vector vector) {
        System.out.println("cho nay ne --------" + this.infoPaint.length);
        this.wearingItems = vector;
        this.isDoing = false;
        this.bodyStyle = (short) -1;
        this.legStyle = (short) -1;
        this.hatStyle = (short) -1;
        this.weaponStyle = -1;
        this.weaponType = -1;
        this.coatStyle = (short) -1;
        for (int i = 0; i < vector.size(); i++) {
            ItemInInventory itemInInventory = (ItemInInventory) vector.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
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

    public void setAnimalWearingInfo(Vector vector) {
        this.wearingAnimal = vector;
    }

    private int getStyleWeapon() {
        int size = this.wearingItems.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                return item.style;
            }
        }
        return -1;
    }

    public void setInventory(long j, int[] iArr, Vector vector, Vector vector2, int i) {
        if (i == 0) {
            this.potions = iArr;
            this.charXu = j;
        }
        if (i == 1) {
            inventory.removeAllElements();
            inventory = vector;
        }
        if (i == 2) {
            animal.removeAllElements();
            animal = vector2;
        }
    }

    public int getCountPotion() {
        int i = 0;
        int i2 = 1;
        while (true) {
            int[] iArr = this.potions;
            if (i2 >= iArr.length) {
                return i;
            }
            if (iArr[i2] > 0) {
                i++;
            }
            i2++;
        }
    }

    public int getAttack() {
        int i = this.attack;
        int i2 = this.attack;
        return i + (0 / 100);
    }

    public int[] calInfoAttackPlayer(Char r11, int i) {
        int[] iArr = {0, 0};
        iArr[0] = i;
        iArr[0] = i - (i / 3);
        int i2 = this.attack;
        iArr[1] = i2;
        byte b = r11.clazz;
        byte b2 = this.clazz;
        if (b == b2) {
            iArr[1] = i2 / 2;
        } else {
            int i3 = mainAt[b2][b];
            int i4 = mainDef[b2][b];
            int i5 = pcAttack[b2][b];
            int i6 = pcDef[b2][b];
            if (i3 == 0) {
                iArr[1] = i2 + ((i5 * i2) / 100);
            } else if (i3 == 1) {
                iArr[1] = (this.attack * i5) / 100;
            }
            if (i4 == 0) {
                int i7 = iArr[0];
                iArr[0] = i7 - ((i6 * i7) / 100);
            } else if (i4 == 2) {
                iArr[0] = iArr[0] - ((i * i6) / 100);
            } else if (i4 == 3) {
                iArr[0] = i;
                iArr[0] = i - ((i6 * i) / 100);
            }
        }
        return iArr;
    }

    @Override
    public int attackDam(LiveActor liveActor) {
        int i = liveActor.defend;
        int attack = getAttack();
        if (liveActor.catagory == 0) {
            byte b = this.clazz;
            if (b == 2 || b == 4) {
                i = liveActor.defend_magic;
            }
            int[] iArrCalInfoAttackPlayer = calInfoAttackPlayer((Char) liveActor, i);
            int i2 = iArrCalInfoAttackPlayer[0];
            attack = iArrCalInfoAttackPlayer[1];
            i = i2;
        } else {
            if ((this.he + 2) % 5 == liveActor.he) {
                i -= (i * 5) / 100;
            } else if ((this.he + 3) % 5 == liveActor.he && liveActor.catagory == 0) {
                i += (i * 5) / 100;
            }
            if ((this.he + 2) % 5 == liveActor.he) {
                attack += (attack * 25) / 100;
            } else if ((this.he + 3) % 5 == liveActor.he) {
                attack -= ((liveActor.catagory == 1 ? 5 : 25) * attack) / 100;
            }
        }
        int i3 = attack - i;
        if (i3 < 0) {
            i3 = attack / 10;
        }
        int iNextInt = ((i3 * 110) / 100) + nextInt(5);
        this.xuyengiap = false;
        if (getBuffEffAttack() <= -1 || this.clazz != 0) {
            attack = iNextInt;
        } else {
            this.xuyengiap = true;
        }
        if (liveActor.catagory == 1 && isFullParty()) {
            attack += (attack * 20) / 100;
        }
        byte b2 = liveActor.catagory;
        if (attack < 5) {
            return 5;
        }
        return attack;
    }

    public boolean isFullParty() {
        int size = party.size();
        if (size < 4) {
            return false;
        }
        int[] iArr = new int[5];
        iArr[0] = 0;
        iArr[1] = 0;
        iArr[2] = 0;
        iArr[3] = 0;
        iArr[4] = 0;
        byte b = GCanvas.gameScr.mainChar.clazz;
        iArr[b] = iArr[b] + 1;
        for (int i = 0; i < size; i++) {
            PartyInfo partyInfo = (PartyInfo) party.elementAt(i);
            if (partyInfo != null) {
                int i2 = partyInfo.clazz;
                iArr[i2] = iArr[i2] + 1;
                if (iArr[partyInfo.clazz] > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public AttackResult getFinalDamTo(LiveActor liveActor, byte b, byte b2) {
        int skillDamPercent = SkillManager.getSkillDamPercent(b, b2, this.clazz);
        AttackResult attackResult = new AttackResult();
        int iAttackDam = (attackDam(liveActor) * skillDamPercent) / 100;
        if (this.xuyengiap && liveActor.catagory == 0 && ((Char) liveActor).clazz != 2) {
            iAttackDam /= 2;
        }
        if (iAttackDam <= 0) {
            iAttackDam = 1;
        }
        isCritical();
        if (attackMiss(liveActor)) {
            attackResult.effect = (byte) 1;
            attackResult.dam = 0;
        } else {
            attackResult.effect = (byte) 0;
            attackResult.dam = (short) iAttackDam;
        }
        attackResult.mpLost = SkillManager.getSkillMP(b, b2, this.clazz);
        attackResult.buffEffect = getBuffEffAttack();
        return attackResult;
    }

    public void setProperty(Property property) {
        if (property.key.equals("hp")) {
            int i = property.value;
            this.hp = i;
            this.realHP = i;
        }
        if (property.key.equals("mp")) {
            this.mp = property.value;
        }
        if (property.key.equals("bp")) {
            this.basePointLeft = (short) property.value;
        }
        if (property.key.equals("sp")) {
            this.skillPointLeft = (short) property.value;
        }
    }

    public void setBuff(byte[] bArr, short[] sArr) {
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            if (b <= 0) {
                byte[] bArr2 = BUFFTYPE;
                if (!isExistBuff(bArr2[i])) {
                    removeBuff(bArr2[i]);
                }
            } else if (!isExistBuff(b)) {
                CharBuff charBuff = new CharBuff(this.x, this.y, bArr[i]);
                charBuff.setTimeLive(sArr[i]);
                this.buff.addElement(charBuff);
            }
        }
    }

    public void setCharInfo() {
        this.xTo = this.x;
        this.yTo = this.x;
        this.wName = (short) Font.smallFont1.getWidth(this.name);
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

    public boolean kilAllMonsterQuest(int i) {
        if (this.monster_kill != null && this.receiveQuest && this.typeQuest == 0) {
            int i2 = 0;
            while (true) {
                int[][] iArr = this.monster_kill;
                if (i2 >= iArr.length) {
                    break;
                }
                int[] iArr2 = iArr[i2];
                if (i == iArr2[0]) {
                    int i3 = iArr2[2];
                    if (i3 >= iArr2[1]) {
                        break;
                    }
                    iArr2[2] = i3 + 1;
                    this.infoQuest = this.monster_kill[i2][2] + "/" + this.monster_kill[i2][1];
                    return true;
                }
                i2++;
            }
        }
        return false;
    }

    public boolean getItemQuest(int i) {
        if (this.monster_kill != null && this.receiveQuest && this.typeQuest == 2) {
            int i2 = 0;
            while (true) {
                int[][] iArr = this.monster_kill;
                if (i2 >= iArr.length) {
                    break;
                }
                int[] iArr2 = iArr[i2];
                if (i == iArr2[2]) {
                    int i3 = iArr2[3];
                    if (i3 >= iArr2[1]) {
                        break;
                    }
                    iArr2[3] = i3 + 1;
                    this.infoQuest = this.monster_kill[i2][3] + "/" + this.monster_kill[i2][1];
                    return true;
                }
                i2++;
            }
        }
        return false;
    }

    public int getDurableWeapone() {
        int size = this.wearingItems.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                return itemInInventory.durable;
            }
        }
        return 0;
    }

    public int getPriceRepair(int i) {
        int size = this.wearingItems.size();
        int i2 = 0;
        int i3 = 0;
        for (int i4 = 0; i4 < size; i4++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.wearingItems.elementAt(i4);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if (item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) {
                i3 += item.price / 10;
            } else {
                i2 += item.price / 10;
            }
        }
        if (i == 0) {
            return i2;
        }
        return i == 1 ? i3 : i2 + i3;
    }

    public void downDurableWeapone() {
        int size = this.wearingItems.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if ((item.type == 3 || item.type == 4 || item.type == 5 || item.type == 6 || item.type == 7) && itemInInventory.mDurable > 0) {
                itemInInventory.mDurable = (short) (itemInInventory.mDurable - 1);
                if (itemInInventory.mDurable % 10 == 0) {
                    itemInInventory.durable = (short) (itemInInventory.durable - 1);
                }
            }
        }
    }

    public void downDuarable() {
        int size = this.wearingItems.size();
        for (int i = 0; i < size; i++) {
            ItemInInventory itemInInventory = (ItemInInventory) this.wearingItems.elementAt(i);
            ItemTemplate item = Res.getItem(itemInInventory.charClazz, itemInInventory.idTemplate);
            if ((item.type == 0 || item.type == 1 || item.type == 2) && itemInInventory.mDurable > 0) {
                itemInInventory.mDurable = (short) (itemInInventory.mDurable - 1);
                if (itemInInventory.mDurable % 10 == 0) {
                    itemInInventory.durable = (short) (itemInInventory.durable - 1);
                }
            }
        }
    }

    public void paintListEff(Graphics graphics, int i) {
        short[] sArr = this.listEffect;
        if (sArr == null || sArr.length <= 0) {
            return;
        }
        int i2 = 0;
        while (true) {
            short[] sArr2 = this.listEffect;
            if (i2 >= sArr2.length) {
                return;
            }
            DataSkillEff partEff = getPartEff(sArr2[i2]);
            if (partEff != null) {
                if (i == 0) {
                    partEff.paintTopHorse(graphics, this.x, this.y + (this.idhorse != -1 ? 0 : getDyAnimalEff(2, this.dir, this.anmFly)) + getdyParHorse(), this.Frameeff, 0);
                } else {
                    partEff.paintBottomHorse(graphics, this.x, this.y + (this.idhorse != -1 ? 0 : getDyAnimalEff(2, this.dir, this.anmFly)) + getdyParHorse(), this.Frameeff, 0);
                }
            }
            i2++;
        }
    }

    public void updateframeeff() {
        short[] sArr = this.listEffect;
        if (sArr == null || sArr.length <= 0) {
            return;
        }
        int i = 0;
        while (true) {
            short[] sArr2 = this.listEffect;
            if (i >= sArr2.length) {
                return;
            }
            DataSkillEff partEff = getPartEff(sArr2[i]);
            if (partEff != null && GCanvas.gameTick % this.Tick[i] == 0 && partEff != null) {
                int size = partEff.listFrame.size();
                if (size == 0) {
                    size = 1;
                }
                this.Frameeff = (byte) ((this.Frameeff + 1) % size);
            }
            i++;
        }
    }

    public int getDyAnimalEff(int i, int i2, int i3) {
        if (this.idhorse != -1) {
            return getdyParHorse();
        }
        byte horse = getHorse();
        if (horse != -1) {
            return this.z + dyb[horse][0];
        }
        return 0;
    }

    @Override
    public int getHp() {
        return this.hp;
    }

    @Override
    public void setState(int i) {
        this.state = (byte) i;
    }

    @Override
    public int getRealHp() {
        return this.realHP;
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
    public int getTypeNpc() {
        if (isNPC()) {
            return this.idBoss;
        }
        return super.getTypeNpc();
    }

    @Override
    public void setHorse(int i, int i2) {
        this.idhorse = (short) i;
        this.typehorse = (short) i2;
    }

    @Override
    public void setWeapon(int i, int i2, int i3) {
        this.idWeapon = (short) i;
        this.typeWeapon = (short) i2;
        this.idImageWeapon = (short) i3;
    }

    @Override
    public String getCharName() {
        return this.name;
    }

    @Override
    public void setPartWearing(short s, short s2, short s3) {
        this.idHair = s;
        this.idBody = s2;
        this.idLeg = s3;
    }

    @Override
    public void setEff(short[] sArr, byte[] bArr) {
        this.listEffect = sArr;
        this.Tick = bArr;
    }

    @Override
    public short getyHorse() {
        return (this.useHorse != -1 || this.idhorse > -1) ? (short) 35 : (short) 0;
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
