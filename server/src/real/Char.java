
package real;

import Constant.Logger2;
import ThanKhi.ThanKhiCraftState;
import com.kpah.event.TichLuyOnline;
import com.kpah.runtime.runtimeCharProperties;
import java.util.Set;
import data.Xoso;
import data.UserLogger;
import data.SeedTemplate;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import data.InfoQuestTemplate;
import util.Logger;
import data.Logdata;
import io.ThreadPool;
import java.util.Date;
import data.Text;
import server.TeamServer;
import java.util.Random;
import data.Database;
import real.cmd.LoginHandler;
import java.util.Iterator;
import io.Message;
import data.NewClan;
import java.io.IOException;

import data.InfoCharPotion;
import data.DanhHieu;
import data.Cooking;
import data.TuBinhTemplate;
import data.FruitTemplate;
import data.AdvInfo;
import data.CharInfo;
import data.TopLuong;
import data.ItemLuckyDraw;
import data.Goverment;
import data.ModelWearing;
import data.CharThanthu;
import java.util.Calendar;
import data.ItemChangeColor;
import java.util.Hashtable;
import data.InfoItemCreate;
import data.GemItem;
import data.CharInboxMessage;
import data.SeedItem;
import data.Pet;
import data.Animal;
import io.Session;
import data.SellerInfo;
import data.Veso;
import java.util.Vector;
import static real.Map.itemTemplates;

public class Char extends LiveActor {

    public runtimeCharProperties runtimeCharProperties = new runtimeCharProperties();

    // <editor-fold defaultstate="collapsed" desc="Instance method">
    public String systemGetInfo() {
        return String.format("Player: id: %s - name: %s\n", id, charname == null ? "NULL" : charname);
    }
    // </editor-fold>

    public Vector<String> nameLienTram;
    public static final int MAP_DUNGEON = 117;
    public static final int MAP_MATERIAL = 17;
    public static byte STRENGTH;
    public static byte AGITITY;
    public static byte SPIRIT;
    public static byte HEALTH;
    public static byte LUCK;
    public static final byte MAX_ITEM_QUEST = 2;
    public static final byte MAX_FARM = 50;
    public static final byte MAX_WEARING = 12;
    public short MAX_INVENTORY;
    public short idPotionUsed;
    public short idTongKim;
    public byte maxBag;
    public byte idTeamTongKim;
    public String dbSkill;
    public String dbPotion;
    public String dbBasic;
    public String dbCharQuest;
    public String dbInfo;
    public Vector<Integer> friendListID;
    public Vector<String> friendListName;
    public Vector<InfoChatRieng> allMsgChatRieng;
    public byte using;
    public byte plant;
    public byte lvHouse;
    public byte lvStore;
    int luong;
    public int lockLuong;
    public int vetangluong;
    long xu;
    public long xutim;
    public long xumat;
    public long xuxai;
    public long luongxai;
    public long luongxainhanRuong;
    // Luong da tieu, rieng cho su kien Vu Lan (khong dung chung luongxai/luongxainhanRuong
    // - hai field do da bi 5 su kien khac dung chung, them mot su kien nua vao se de dam
    // vao nhau neu bat cung luc). Cu 200 luong tieu (= 2 luong/diem x 100 diem) tu dong
    // tang 1 Hoa hong do - xem subLuong().
    public long luongXaiVuLan;
    public int charDBID, tichluy, tichluy_tuan, countChar, tichluy_bosung, qua_npc;
    public byte speed;
    public byte slotWearing;
    public short agenID;
    public short idClan;
    public short nMoonCake2Exp;
    public short ID_ITEM;
    public int userID;
    // Vi tri (idMenu) cua muc "Nhan moc nap" trong menu NPC Tong Quan (npcType -10), duoc
    // RequestNPCInfoHandler gan lai moi lan mo menu de Map.doMenu nhan dung click du menu
    // co bao nhieu muc tuy theo co/khong co cac dong "Nhan qua top"/"Nhan qua" phia truoc.
    public int idxMocNapMenu = -1;
    // Vi tri (idOptionMenu) cua muc "Su kien World Cup" trong menu NPC Tong Quan, cung
    // duoc RequestNPCInfoHandler gan lai moi lan mo menu. -1 = su kien dang tat nen
    // menu khong co dong nay. Xem real/SuKienWorldCup.
    public int idxSuKienWorldCup = -1;
    // Vi tri (idOptionMenu) cua muc "Su kien Vu Lan" trong menu NPC Dau Truong (npcType
    // -31), gan lai moi lan RequestNPCInfoHandler mo menu -31. -1 = su kien dang tat.
    // Doc TRUOC bang remap VI_TRI_MENU_DAU_TRUONG trong MapDoMenu de khong bi bang do
    // chan nham (bang chi chap nhan vi tri no biet). Xem real/MapSuKien.doMenuDauTruongVuLan.
    public int idxSuKienVuLanDauTruong = -1;
    // Vi tri (idOptionMenu) cua muc "Nhan thuong dua top" trong menu NPC Tong Quan, gan
    // lai moi lan RequestNPCInfoHandler mo menu -10. -1 = nhan vat nay khong co thuong
    // dua top nao dang cho nhan (menu khong co dong nay). Xem real/SuKienDuaTop.
    public int idxThuongDuaTop = -1;
    public short partyID;
    public int masterIDParty;
    public Vector<Short> idItem;
    public Vector<Short> idPotion;
    public Vector<Short> idGem;
    public Vector<Short> idSP;
    public Vector<Short> idItemQuest;
    public Vector<Veso> vesoCu;
    public Vector<Veso> vesoxu;
    public SellerInfo sellerInfo;
    public Party party;
    public String charname;
    public byte headStyle;
    public byte charClass;
    public byte[] skill;
    public short[] skillClan;
    public byte[] skillLvClan;
    public long[] timeUseSkillClan;
    public short strength;
    public short agitity;
    public short spirit;
    public short health;
    public short basic;
    public short luck;
    public short lastLV;
    public boolean exit;
    public boolean die_pk;
    public boolean gotWages;
    public boolean gifMoonFestival;
    public byte isBot;
    public byte rankClan;
    public LevelDetail lvDetail;
    public short basepoint;
    public short luongOpenInventory;
    public short skillpoint;
    Session session;
    public short[][] coolDown;
    String stCapcha;
    public long[] timeLastUseSkills;
    public long timeDropCake;
    public long timeDropCake1;
    public long timeInClan;
    public long timeUp5Attribute;
    public long timeNguoiTuyet;
    public long timeNguoiTuyet2;
    public long timeLastMove;
    public long timeNextRegion;
    public long lastTimeNghienBot;
    public Vector<Short> nearChars;
    public Vector<Short> nearMons;
    public byte[] arrayBuff;
    public short[] timeOut;
    public short[] countDown;
    byte[] lvBuff;
    public long[] timeUseBuff;
    public long lasTimeMove;
    public long timeInviteTrade;
    public long timeGiveCardTown;
    public long xpLost;
    public long timeUsePK;
    public long timeJoinClan;
    public long timeHoiSinh;

    // ==================== LOI MOI VAO BANG (MapBangHoi.doInviteCharJoinClan) ====================
    // Truoc day type==1 (chap nhan/tu choi) khong doi chieu voi bat ky trang thai nao tu type==0
    // (moi) - no chi tra idCharx (id nguoi goi tim thay o goi tin) ra mot Char roi lay LUON
    // idClan cua nguoi do lam bang de gia nhap, khong kiem nguoi do co that su vua moi minh
    // khong. Client tu gui idCharx = id BAT KY thanh vien nao cua bang muon vao la tu ghep duoc
    // vao bang do, bo qua het dieu kien cua type==0 (quyen si quan, bang dang cho giai tan...).
    /** Id bang vua moi nhan vat nay (type==0 gui toi), -1 = khong co loi moi nao dang cho. */
    public int idClanVuaDuocMoi = -1;
    /** Han hieu luc cua loi moi tren. */
    public long timeHetHanMoiClan;
    public int mp;
    public int maxmp;
    
    int lastX;
    int lastY;
    int pointCongHienClan;
    public byte typeConfig;
    public byte upSpeedNguaVanTieu;
    public int giveMoneyClan;
    public boolean isChangeMap;
    public boolean isMonster;
    public byte idNgtuyet;
    public String subCharname;
    public int[] potions;
    public short[] itemquest;
    public int[] tPotions;
    public int[] rPotions;
    public short[][] posNPCInVilage;
    /**
     * True cho mot bot duoc dat vao TAT CA cac khu (region) cua mot ban do nhieu khu (vi du NPC
     * "hoa tieu" tren cac ban do nhu Loc tri - 5 khu: chinh + Dong/Tay/Nam/Bac).
     *
     * VI SAO CAN. MapKhoiTao dat CHUNG mot doi tuong Char (tu Map.npcHoatieu) vao moi khu bang
     * Map.addPlayer(country, bot, i) voi i chay tu 0..nregion-1. Duong nay goi thang
     * Region.addPlayer(country, p) - KHONG giong Region.playerJoin(Char) (duong nguoi choi that
     * dung) - nen KHONG BAO GIO gan bot.region. Truong bot.region vi vay dung mai o gia tri mac
     * dinh 0 (Actor.region), trong khi CharCapNhat.updateNearChar loc nghiem ngat
     * "p1.region != self.region" khong co ngoai le cho bot -> bot chi hien voi nguoi choi dang
     * o DUNG khu 0 (khu chinh), MAT HAN voi nguoi choi o cac khu Dong/Tay/Nam/Bac (su co
     * 02/09/2026). Mot doi tuong Char DUY NHAT khong the mang nhieu gia tri region cung luc (no
     * duoc dat vao ca 5 khu), nen sua bang co nay: bot co co nay thi bo qua han phep so
     * region trong updateNearChar, thay vi co gang gan dung mot region roi that bai voi 4 khu
     * con lai.
     */
    public boolean hienThiMoiRegion;
    public int attackDamage;
    public int outdelay;
    /**
     * Khoa cho viec LUU rieng nhan vat nay. Database.saveChar va
     * Database.saveCharAuto deu giu khoa nay, nen hai lan luu cua CUNG mot nhan vat
     * khong bao gio chong nhau.
     *
     * VI SAO CAN. Ca hai ham luu chia lam hai pha tach roi: pha CHUP (ghep chuoi
     * equip/inven/bag/tuido tu doi tuong Char dang song) roi moi den pha GHI (xin ket
     * noi trong pool, prepareStatement, execute). Giua hai pha la mot khoang tre khong
     * xac dinh. Cau lenh la "UPDATE tob_char SET ... inven=? WHERE id=?" - ghi de toan
     * bo cot, khong co dieu kien phien ban. Nen neu hai lan luu chong nhau thi ban chup
     * SOM hon co the execute SAU, va nhung mon tao ra giua hai moc chup bien mat khoi
     * CSDL du bo nho van dung.
     *
     * Ma co RAT NHIEU luong cung luu mot nhan vat: luong doc socket goi saveCharAuto
     * sau moi lan che tao (Map.doCreateNewItem), luong ban do day saveCharAuto vao
     * ThreadPool moi 5-11 phut (CharVongLap), luong doc socket goi saveChar khi cat ket
     * noi (CharManager.kickPlayer), va RealController.saveAllChar luc tat server.
     */
    public final transient Object KHOA_LUU = new Object();
    public Vector<Item> iItems;
    public Vector<Item> tItems;
    public Vector<Item> rItems;
    public Vector<Item> bItem;
    public Vector<Item> awItems;
    public Vector<Item> sellShopItem;
    public Vector<Item> bagItems;
    public Item[][] wItems;
    public Vector<Animal> animal;
    public Vector<Pet> pet;
    public Vector<SeedItem> seedsItem;
    public Vector<CharInboxMessage> inbox;
    public Vector<GemItem> gemItem;
    public Vector<GemItem> gemItemImbue;
    public int[] listGemitem;
    public int[] listGemitemLock;
    public int[] listGemitemSell;
    public int[] allGemGet;
    public int[] allGemGetLock;
    public int[] allGemUse;
    public int[] allGemUseLock;
    public Vector<ShopTemplate> specialItem;
    public int[] listSpItem;
    public int itemImbue;
    public int mapID;
    public int mapID_the_mua_ban;
    public short region_the_mua_ban;
    public byte x_the_mua_ban;
    public byte y_the_mua_ban;
    public short killer;
    public boolean isKiller;
    public byte pk;
    public byte typeItemBuy;
    public byte classBuy;
    public byte pk_chienTruong;
    public boolean isThodia;
    public boolean finishTrade;
    public boolean lockTrade;
    public boolean isPutItem2Bag;
    public boolean isImbue;
    public Vector<Char> userTrade;

    // ==================== GIAO DICH THUOC (goi 66, MapMuaBan.doTrade) ====================
    // Khoa DUY NHAT cho moi thao tac doc/ghi trang thai giao dich cua BAT KY nhan vat nao.
    //
    // Vi sao phai co: goi 66 khong nam trong danh sach lenh rieng (RealController.privateCmd)
    // nen no chay tren LUONG CUA BAN DO ma nguoi gui dang dung. Hai nguoi giao dich nhung o hai
    // ban do (hoac hai khu) khac nhau la hai luong khac nhau cung sua mot bo trang thai chung
    // (tPotions/rPotions/finishTrade/lockTrade cua ca hai). Buoc hoan tat (case 5) tu hai phia
    // chay song song thi ca hai deu thay "doi phuong da khoa" va cung cong thuoc cho nhau -
    // mot lan giao dich, hai lan nhan thuoc. Luat kiem tra luc hoan tat: real/GiaoDichThuoc.
    public static final Object KHOA_GIAO_DICH = new Object();
    /** id (ngan, trong phien) cua nguoi vua gui loi moi giao dich cho minh; -1 = chua ai moi. */
    public short idNguoiMoiGiaoDich = -1;
    /** Thoi diem nhan loi moi tren, de loi moi cu khong dung lai duoc. */
    public long timeDuocMoiGiaoDich;

    /** Doi phuong dang giao dich voi minh, hay null. */
    public Char doiPhuongGiaoDich() {
        try {
            return this.userTrade.size() > 0 ? this.userTrade.get(0) : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Xoa SACH trang thai giao dich cua rieng nhan vat nay.
     *
     * Truoc day cac duong huy (Map.checkTrade, doUsePotion.checkTrade, actorDie) chi xoa
     * tItems/rItems/userTrade ma DE LAI tPotions/rPotions: mang "thuoc doi phuong da bo vao cho
     * minh" con nguyen, nen o lan giao dich SAU voi bat ky ai, luc hoan tat minh duoc cong dung
     * bay nhieu thuoc tu khong khi. Day la goc cua chuyen nhan doi nhan sam / the van tieu /
     * bo hoa... qua giao dich.
     */
    public void datLaiTrangThaiGiaoDich() {
        this.finishTrade = false;
        this.lockTrade = false;
        try {
            this.tItems.removeAllElements();
        } catch (final Exception e) {
        }
        try {
            this.rItems.removeAllElements();
        } catch (final Exception e) {
        }
        final int n = (this.potions != null) ? this.potions.length : SO_O_THUOC;
        this.tPotions = new int[n];
        this.rPotions = new int[n];
        try {
            this.userTrade.removeAllElements();
        } catch (final Exception e) {
        }
        this.idNguoiMoiGiaoDich = -1;
    }

    /**
     * Huy giao dich dang mo (neu co) cho CA HAI ben, duoi khoa KHOA_GIAO_DICH.
     *
     * @param baoMinh      gui goi 66/3 (dong cua so giao dich) cho chinh minh
     * @param baoDoiPhuong gui goi 66/3 cho doi phuong
     */
    public void huyGiaoDich(final boolean baoMinh, final boolean baoDoiPhuong) {
        synchronized (KHOA_GIAO_DICH) {
            final Char dp = this.doiPhuongGiaoDich();
            this.datLaiTrangThaiGiaoDich();
            if (dp != null && dp != this) {
                // Chi dong den doi phuong neu ho that su dang giao dich voi minh: khong pha giao
                // dich cua ho voi nguoi thu ba, cung khong xoa loi moi ho vua nhan tu ai khac.
                // (Mang cu con sot ben ho, neu co, se bi xoa o buoc bat dau giao dich ke tiep -
                // MapMuaBan.doTrade case 1 dat lai ca hai ben truoc khi mo.)
                if (dp.doiPhuongGiaoDich() == this) {
                    dp.datLaiTrangThaiGiaoDich();
                    if (baoDoiPhuong) {
                        guiDongGiaoDich(dp);
                    }
                }
            }
            if (baoMinh) {
                guiDongGiaoDich(this);
            }
        }
    }

    private static void guiDongGiaoDich(final Char p) {
        try {
            final Message m = new Message(66);
            m.dos.writeByte(3);
            p.sendMessage(m);
            m.cleanup();
        } catch (final Exception e) {
        }
    }

    protected static final byte[] GENDER_OF_CLAZZ;
    public byte gender;
    public byte rideHorse;
    public byte nMsgChat;
    public byte idImgHorse;
    public boolean isNewClient;
    public boolean gotoIsland;
    public short nPKill;
    public short timeXP;
    public long timeKiller;
    public long timeWait;
    public long timeChat;
    public long timeChatRieng;
    public long timeChatBang;
    /** Lan cuoi dung loa. Loa phat toi TOAN may chu nen phai gian cach rieng. */
    public long timeDungLoa;
    /** Lan cuoi go ma qua tang. Moi lan go la vai cau truy van database. */
    public long timeNhapGiftCode;
    public long fullGoldTime;
    public long halfGoldTime;
    public long timeOutGame;
    public int minuteExp;
    public byte x2;
    public byte[] upAbility;
    public byte[] abilityKham;
    public byte[] attNguHanh;
    public byte[] attNguHanhAnimal;
    public long timePlay;
    public long startPlay;
    public boolean isAdmin;
    public boolean beAttack;
    public short nNearchar;
    public short idRegionNuiChauBau;
    public short idRegionLoidai;
    public byte autoGetItem;
    public byte autoSkill;
    public byte canGiveCard;
    public byte autoComeHome;
    public byte luot_van_tieu;
    public byte buy_luot_van_tieu;
    // Id pet / thu cuoi dang cho xac nhan xoa o npc tong tieu dau (menu Ho Tro).
    // Truoc day id duoc nhoi vao idActor cua goi popup, ma doPopup doc idActor bang
    // readShort() -> id vuot 32767 la xac nhan sai doi tuong. Giu o day thi khong phu
    // thuoc do rong cua goi tin. Chi song trong phien, khong luu DB.
    public int idPetChoXoa = -1;
    public int idThuChoXoa = -1;
    public byte cuop_tieu;
    public byte vip;
    private long timeHit;
    private long timeHit4;
    long timeStartHack;
    public int oldLv;
    short nHit;
    short nhit4;
    public InfoItemCreate infoItemCreate;
    public Item itemAx;
    public Item itemLinhDanhThue;
    public static short maxHit;
    public static short maxHit4;
    public Hashtable<Short, Monster> monsters;
    public long timeWaitComeHome;
    public long timeUseThanHanhPhu;
    public byte rcvGifByHour;
    public byte rcvGifByMonth;
    public byte rcvGifByLv;
    public byte totalDayInMonth;
    public short hutmau;
    public short hoihp;
    public short hoimp;
    public short khangbanglan;
    public short khangsetlan;
    public short khangdoclan;
    public short lamthinh;
    public short tanphe;
    public short khanglualan;
    public boolean isOnlineToDay;
    long millisOnlineStart;
    long millisOnline;
    long timeSave;
    public Monster monsTerThuThap;
    public Vector<Item> itemChange5;
    public ItemChangeColor itChangeColor;
    public static byte[][] dx_horse_tool;
    public static byte[][] dy_horse_tool;
    public static byte[][] Dx_Dy_WP_tool;
    public static byte[][] Dx_Dy_PP_tool;
    public static byte[][] Dx_Dy_AVT_tool;
    public int indexDxDyHorseTool;
    public int idHorseTool;
    public Hashtable<Short, PotionUse> coolDownPotion;
    public Vector<EffectBuff> ALL_BUFF_INMAP;
    public static byte ID_CHUYEN_MAP_CHIEN_TRUONG_MOBA;
    public static byte ID_ALL_KHU_CHIEN_TRUONG_MOBA;
    public static byte ID_NOI_LUC;
    public static byte ID_HON_DON;
    public static byte ID_HOI_HON;
    public static byte ID_HON_NGUYEN;
    public static byte ID_BACH_TIEN;
    public static byte ID_DINH_CHU;
    public static byte ID_CAM_LANG;
    public static byte ID_PT_HOA_CONG;
    public static byte ID_PT_HON_NGUYEN;
    public static byte ID_PT_HON_DON;
    public static byte ID_HOI_SINH;
    public static byte ID_INFO_PLAYER;
    public static byte ID_TIME_NUI_KHO_BAU;
    public static byte ID_NOI_LUC_TANG;
    public static byte ID_DEM_NGUOC;
    public static byte ID_TY_SO;
    public static byte ID_TIME_LOI_DAI;
    public static Hashtable<Integer, IndexWearing> POS_WEARING_CHAR;
    int indexHelp;
    public Vector<String> stHelp;
    byte nConfirm;
    private static byte[][] groupKickAnimal;
    private static byte[][] groupKick;
    public static Hashtable<Integer, GroupKick> GROUP_KICH;
    byte fullSet15;
    byte fullSet10;
    byte fullSet13;
    byte isFullsetAnimal;
    public static int Lvx3Exp;
    public static Vector<String> timex2;
    public static Vector<String> timex3;
    public static Vector<String> timex50;
    public boolean rcvInvite;
    public boolean rcvInviteVip;
    public boolean canJoinTranYeuTran;
    short lvSearch;
    byte typeItemSearch;
    byte colortItemSearch;
    byte phamSearch;
    byte plusSearch;
    String searchChar;
    short[] buffPass;
    public int secondHealth;
    byte idRing;
    public static byte KHAM_MU;
    public static byte KHAM_DOC;
    public static byte KHAM_CHOANG;
    public static byte KHAM_BANG;
    public static byte KHAM_HOA_DA;
    public static byte KHAM_GIAM_TOC;
    int attackPlayer;
    int defendMaPlayer;
    int defendVatPlayer;
    public static final byte KIEM = 0;
    public static final byte DAO = 1;
    public static final byte PS = 2;
    public static final byte DS = 3;
    public static final byte CUNG = 4;
    short idBot;
    public byte posNPC;
    long timeSendHelp;
    long timedie;
    long timeCrazy;
    short isCrazy;
    public static byte SET_LAN;
    public static byte DOC_LAN;
    public static byte BANG_LAN;
    public static byte LUA_LAN;
    byte pcGiamSatThuongByCuMeo;
    static String[] namelan;
    long timeExistCharHire;
    byte classlinh;
    byte lvlinh;
    byte lvlinhthue;
    byte genderlinh;
    static int[] idmapthangbe;
    static int[][] xythangbe;
    static int[] idmapchucnu;
    static int[][] xychucnu;
    public byte typeTieu;
    public static int[] HP_MONSTER_VANTIEU;
    public static int[] DEF_MONSTER_VANTIEU;
    public static int[] UP_HP_MONSTER_VANTIEU;
    public static int[] UP_DEF_MONSTER_VANTIEU;
    static byte INDIVITUAL;
    static byte CLAN;
    static byte NATION;
    static long[][] EXP_NV_HANG_NGAY;
    public byte idNgtuyet2;
    long cooldownNguyetAnh;
    Calendar calCheck;
    String dateStartOnline;
    static byte indexInfoAdv;
    public static boolean isRemove;
    public static long timeSendRaoVat;
    public MonsterVantieu monster;
    public Char charCopy;
    public Char charHire;
    public CharThanthu charthanthu;
    public long timeRegentHpMp;
    public boolean isDoChangeMap;
    public byte idCuiDot;
    public byte idRuouUong;
    public boolean isLuLan;
    int timeHs;
    public static short timeAddMoreHs;
    static byte TOTAL_FANCUNG;
    // Khoa cho CharVongLap.update: nhieu nhan vat o nhieu map/khu khac nhau cung vuot moc
    // minuteOnline>120 gan nhu cung luc chay tren nhieu luong that su song song, kiem
    // "TOTAL_FANCUNG > 0" roi moi tru sau se cho nhieu hon 1 nguoi qua kiem tra khi chi con
    // dung 1 suat.
    static final Object KHOA_FAN_CUNG = new Object();
    short countSecond;
    public long timeSendAlertQuangCao;
    byte nNpc;
    short wScreen;
    short hScreen;
    static int ID_HEAD_TIEU_LONG_NU;
    static int ID_HEAD_DUONG_QUA;
    static int ID_HEAD_HANG_NGA;
    static int ID_BODY_HANG_NGA;
    static int ID_LEG_HANG_NGA;
    static int ID_MAT_NA_BI_NGO;
    static int ID_MAT_NA_QUY;
    static int ID_MAT_NA_SOI;
    static int ID_MAT_NA_ZOOMBI;
    static int ID_MAT_NA_PANDA;
    public short[] rangeAddMonster;
    public short[] rangeRemoveMonster;
    public byte typeFirmware;
    long timeForbitChat;
    long timeSendAllChar;
    long timeSendEffTool5l;
    long timeBuffGiamSatThuong;
    byte countSendchat;
    int kkk;
    static byte[][] buffSkill;
    static byte[][] hourShield;
    public long timeExit;
    public Animal animalRide;
    public Pet petUsing;
    public Item itemHoaKyLan;
    /**
     * Moc het han "Ao hoa Hoa Ky Lan" (0 = khong dang ao hoa). Xem
     * dangAoHoaHoaKyLan/doAoHoaHoaKyLan/checkExpireAoHoaHoaKyLan.
     */
    public long timeAoHoaHoaKyLan;
    public Item itemTam;
    public Item itemPet;
    public Item itemVukhiThoiTrang;
    public Item itemThanKhi;
    byte winPH;
    public short hlt_buy;
    public short timePlayThamdinh;
    byte countnvVulan;
    int countMonsterKill;
    public int diemdoivePH;
    public byte reclass;
    static boolean offAuto;
    public Vector<Item> itemCheckDuplicate;
    int countDup;
    public long timeUseTheMuaBan;
    public static String[] nameTop100DaiViet;
    public String lastLog;
    public byte giftTop;
    public byte selectPhiphong;
    public static long[] timeExpireAutoSkill;
    public static long[] timeExpireAutoGetItem;
    public byte idAutoSkill;
    public byte idAutoGetitem;
    public long timeOnlineSukien;
    byte countTime;
    public String infoWearing;
    public Vector<Item> itemResetSock;
    public Vector<Byte> treeID;
    public int questID;
    public int nextQuest;
    public short idNpc;
    public short currentNpc;
    public boolean receiveQuest;
    public boolean isFinish;
    public boolean rcvQuestClan;
    public boolean finishQC;
    public int[][] monsterKill;
    public int[][] monsterIem;
    public int[] npc_quest;
    public boolean talk_npc;
    public CharQuest char_quest;
    public short[][] killMSClan;
    public short[][] itemClan;
    public short flower;
    public ModelWearing wModel;
    public byte[] newAtb;
    public byte[] addMoreLevelSkill;
    public byte[] lockAtb;
    public byte idSkillLearnClan;
    public byte myCountry;
    public byte inCountry;
    public long cSalary;
    public byte typeBox;
    Vector<InfoGifLucky> gifLuckyBox;
    Vector<Byte> idOpen;
    byte nSpecialBox;
    byte[][] nNglieu;
    public static byte[] lixilucky;
    public static byte totalLixiOpened;
    static int[] XU_LI_XI;
    static String daysomaymm;
    public static int numberLucky;
    public static int numberRandom;
    public static int total_open;
    // Khoa cho CharVongLap.checkReceiveTrungLinhThuRuongBatbao: total_open/numberRandom la
    // static thuong, doc-tang-so sanh tu nhieu luong Map/Region khac nhau cung mot luc (moi
    // nguoi choi mo ruong o mot map rieng) se lam mat lan tang (lost update) hoac nhieu
    // nguoi cung thay dieu kien trung dung mot luc.
    public static final Object KHOA_MO_RUONG = new Object();
    public String dayOpenXoso;
    static short[] idGif35;
    public static int ID_BIG_AVT_AO_TIEN_NU;
    public static int ID_BIG_AVT_AO_TINH_QUAN;
    // Anh dau tien trong nam dai anh avatar cua canh (map/icon/855..859), theo
    // thu tu vat pham 898..902. Xem getIdBigPhiPhong().
    public static final int ID_BIG_AVT_CANH_DAU = 855;
    public static int ID_BIG_AVT_PP_KHONG_TUOC;
    public static int ID_BIG_AVT_PP_HUYEN_VU;
    public static int ID_BIG_AVT_PP_BONG_DA;
    public static int ID_BIG_AVT_AO_DAI_NAM;
    public static int ID_BIG_AVT_AO_DAI_NU;
    static byte[] idRankGov;
    public byte headModel;
    public static Goverment[] gov;
    static Vector<ItemLuckyDraw> allItemLuckyDraw;
    byte indexItemLuckyDraw;
    public static byte pcWinLuckyDraw;
    public static byte nItemLucky;
    public static byte addMore;
    static byte[][] groupLucky;
    public static String[] except_quay_so;
    public static long timeGetPhuongHoang;
    public static Hashtable<String, TopLuong> TOP_LUONG;
    public static byte have8k;
    long timeMove;
    int lastMovex;
    int lastMovey;
    static int[][] lcx_chan_nui_chau_bau;
    static int[][] lcy_chan_nui_chau_bau;
    boolean startAutoImbue;
    boolean lkdLock;
    boolean MMLock;
    boolean BHLock;
    byte plus;
    short idGemLkd;
    short idGemMM;
    short idGemBH;
    public byte receiveLG;
    public byte gif35;
    /** Da nhan thuong len cap 35 chua (0 = chua, 1 = da) - xem MapXuLyLenh.thuongLenCap35(). */
    public byte thuongLv35;
    public byte nHopMut;
    public int pArena;
    public byte buyAnimalArmor;
    public boolean isCorrectPass;
    public long luongNap;
    public long luonglost;
    public long salaryClan;
    public long expRace;
    public int x25Exp;
    public int x2Phaohoa;
    public int honor;
    public int idWedding;
    public int idArena;
    public byte idFlag;
    public short totalKill;
    public short maxKill;
    public short idGropRace;
    public short idBanhNuongDoi;
    public short idNglieuDoi;
    public short totalKillInArena;
    public short nBekill;
    public byte forbitChat;
    public byte married;
    public byte rcvGiftWedding;
    // Diem doi thuong o NPC "doi thuong": diemDoiThuong la so du vinh vien (tru khi
    // doi qua), diemQuaiHomNay/soQuaiHomNay reset moi ngay cung luc voi dayLogin.
    public int diemDoiThuong;
    public int diemQuaiHomNay;
    public int soQuaiHomNay;
    public String dayLogin;
    public String dayCamChat;
    public byte receiveGiftEvent;
    public byte subpk;
    public byte rankGov;
    public byte usingSummons;
    public byte posRing;
    public byte countQuestClan;
    public byte nBinhTra;
    public byte gif83;
    public byte ntangtoc;
    public byte receiveQuestVulan;
    public byte receiveGiftOldChar;
    public Vector<String> listKiller;
    public int typeGemImbue;
    public int diemNapVip;
    public int diemxaiVip;
    public int typeGemKham;
    public byte xichtho_thienlyma;
    public byte typeHopMaterial;
    public byte slNgLieuDoi;
    public byte type_luong_doi_he;
    public Vector<CharInfo> myFriend;
    public static Vector<AdvInfo> infoAdv;
    public static Vector<AdvInfo> infoAdv2;
    public int itemTemp;
    public int charCount;
    public int moneyThachdau;
    public int moneyTrade;
    public short idItemTach;
    public short setlan;
    public short banglan;
    public short lualan;
    public short doclan;
    public short tyleRotItem;
    public short tylerotXu;
    public String nameHusband_wife;
    public String pInviteWedding;
    public String passWord;
    public String name_char_loi_dai;
    public Vector<Byte> idGifWedding;
    public short idMainQuest;
    public short pointActiveQuest;
    public short minuteSocola;
    public short minuteOnline;
    public NewCharQuest mainQuest;
    public NewCharQuest subQuest;
    public NewCharQuest clanQuestNew;
    public NewCharQuest eventQuest;
    public String itemget;
    public String monskilled;
    public String finish;
    public String itquest;
    public String equip;
    public String invent;
    public String bag;
    public String tuido;
    public Vector<Item> allItemBuy;
    public Vector<String> listKillme;
    public FruitTemplate[] fruit;
    public Vector<TuBinhTemplate> tubinh;
    Hashtable<Short, Short> ALL_ID_USE;
    public String nameCompetitor;
    public Vector<ItemSell> allNewItem;
    public Vector<ItemSell> allSearchItem;
    public Vector<ItemSell> allItemBid;
    public ItemSell itemBuy;
    public ThanKhiCraftState thanKhiCraftState;
    public static Hashtable<String, Integer> TOP_CHAR_USE_LUONG;
    public long lastTimeOut;
    public long timeEatSocola;
    public long timecamchat;
    public byte firstNapMoney;
    public byte new_search;
    public int banphaohoa;
    /**
     * Trung Thu 2026: het han buff "can moc tha den 300" (x2 exp 24h) - moc thoi gian
     * System.currentTimeMillis(), giong cach hetHanLan/timeActive8sRongLon dang lam. KHONG
     * luu DB (giong da so buff tam thoi khac trong code nay) - dang xuat trong 24h se mat
     * buff som, chap nhan don gian hoa nay.
     */
    public long trungThuMoc300HetHan;
    /** Lan dot den (thap nen) gan nhat - spec "thap nen se mat 3 phut" (them 19/09/2026,
     * chi trong bo nho nhu trungThuMoc300HetHan ngay tren, khong luu DB). */
    public long lanDotDenGanNhat;
    /** Trung Thu 2026 (moi 19/09/2026): tong SO LUONG da tieu trong su kien, dung de tinh
     * "diem tieu xai" (cu 2 luong = 1 diem, xem subLuong()) doi lay long den o NPC Hang
     * Nga - KHAC voi luongxai/luongxainhanRuong (bi nhieu su kien khac DUNG CHUNG, doi diem
     * rieng cho Trung Thu se lam nham voi cac su kien khac neu tai su dung). Co luu DB
     * (pInfo, xem CharVongLap/CharNapDuLieu) vi qua trinh tieu luong thuong keo dai qua
     * nhieu lan dang nhap, khac trungThuMoc300HetHan/lanDotDenGanNhat (chi mang tinh tam
     * thoi trong 1 phien).
     */
    public long luongDaXaiTrungThu;
    public static Cooking nauBanhChung;
    public static Cooking nauBanhTet;
    public Vector<Item> itemPetEat;
    public static boolean isBaotriVxmm;
    public Vector<DanhHieu> allDanhHieu;
    public DanhHieu currentDanhHieu;
    public int idEffDanhHieu;
    static short[] ID_POINT_NOI_LUC_CHIEN_TRUONG;
    public static Hashtable<String, PlayerXoso> ALL_PLAYER_XOSO;
    public static long MONEY_XOSO;
    public static long timeXoso;
    public static String charNameXosoWin;
    public static long moneyCharXosowin;
    public static long moneyCharXosoBid;
    /** Chu ky tu dong luu nhan vat. Cu goc la 120-210 phut — qua thua, tat sv la mat het.
     *  Random nhieu moc de tranh ca server cung luu mot luc. */
    public static final short[] AUTOSAVE_MINUTES  = {5, 7, 9, 11};
    /** Lan luu dau tien sau khi vao game (cu goc 7200000 = 2 tieng). */
    public static final long    AUTOSAVE_FIRST_MS = 3L * 60L * 1000L;
    /** Khoang cach toi thieu giua 2 lan dung CUNG mot loai binh (ms). Chong spam. */
    public static final long POTION_COOLDOWN_MS = 250L;
    /**
     * Gian cach giua 2 lan dung loa (ms). Loa la thu duy nhat nguoi choi tu goi
     * duoc ma phat toi TOAN BO may chu: mot goi tin gui len thanh N goi tin gui
     * xuong, N la so nguoi dang online. Dat rong tay hon nhieu so voi binh.
     */
    public static final long LOA_COOLDOWN_MS = 3000L;
    /**
     * Gian cach giua 2 lan go ma qua tang (ms). Moi lan go la vai cau truy van
     * database; khong chan thi vua dung de do ma vua lam nghen ket noi DB.
     */
    public static final long GIFTCODE_COOLDOWN_MS = 2000L;
    /** Moc thoi gian dung lan cuoi cua tung loai binh. */
    public long[] timeUsePotion = new long[200];
    /** So tui hanh trang toi da mo rong duoc (moi tui = MAX_INVENTORY o). */
    // 19/09/2026: nang tran tang tui tu 5 len 6 (yeu cau nguoi dung) -> tong toi da
    // MAX_INVENTORY * MAX_BAG_LIMIT = 42 * 6 = 252 o (truoc la 210).
    public static final byte MAX_BAG_LIMIT = 5;
    /** Kho do NPC Anh bay (bItem) - truoc la 15 co dinh, +42 theo yeu cau nguoi dung 19/09/2026. */
    public static final int MAX_KHO_DO = 57;
    /** "Tui Do" (bagItems, menu NPC "Mo Tui Do"/"Bo do vao tui") - truoc la 42 co dinh, +42 theo yeu cau nguoi dung 19/09/2026. */
    public static final int MAX_TUI_DO = 84;
    /** Tong so nguyen lieu (gem) toi da giu duoc khi mua tu Phu ong o lang. */
    public static final int MAX_GEM_TOTAL = 1000000;
    /** Vong quay vip (thay ngu hanh) — sua 4 hang so nay de chinh gioi han. */
    public static final int  MAX_BID_XOSO    = 1000000000;   // dat toi da moi nguoi: 1 ty
    public static final int  MIN_BID_XOSO    = 1000000;      // dat toi thieu: 1 trieu
    public static final int  MAX_PLAYER_XOSO = 20;           // so nguoi toi da moi luot
    public static final long MAX_POT_XOSO    = 20000000000L; // tran hu: 20 nguoi x 1 ty
    public static Hashtable<String, PlayerXoso> ALL_PLAYER_XOSO_low;
    public static long MONEY_XOSO_low;
    public static long timeXoso_low;
    public static String charNameXosoWin_low;
    public static int moneyCharXosowin_low;
    public static int moneyCharXosoBid_low;
    public static byte onOffThamdinh;
    public static byte onOffTime180;
    public static String chartopchoitrungthu;
    public byte canReceiveGiftOffline;
    public byte buychoi;
    public byte buylongden;
    public short idPetTang;
    public byte typeJoinXoso;
    public String hoten;
    public String diachi;
    public String nametangkm;
    public String nameTangPet;
    ItemWaitSell itemwait;
    public int nhomThidau;
    public static int dayEndVip;
    public static int timeEndVip;
    public static int minuteEnd;
    public static byte[] soluong_loa_vip;
    public static byte[] up_px_xu_vip;
    public int countRongLon; // Thêm biến đếm số lần bị đánh
    public long timeRongLon;
    private long timeActive8sRongLon;
    private int damageAccumulated = 0; // Sát thương tích lũy
    // ===== CẤU HÌNH TÍCH/XẢ RỒNG LỚN =====
    private boolean rlAccumulateAlways = true;   // BẬT: bị hit lúc nào cũng tích (bỏ qua 8s)
    private boolean rlUseDeathReset = true;      // BẬT: dùng số lần chết để reset khi đạt 8 mà chưa xả   
    private int rlDeathCount = 0;                // số lần chết kể từ lần xả gần nhất
    private int rlLifeStartMaxHP = 0;            // MaxHP chụp tại đầu “đời”
    private static final int RL_RELEASE_MULT = 2; // hệ số nhân sát thương khi xả (x2)




    public long lastTimeUseHP = 0;
    public long lastTimeUseMP = 0;
    // bo suu tap
    public int isCaiTrang = 0;
    public boolean isNghienAuto = false;
    public boolean isAutoBan = false;


    static {
        Char.STRENGTH = 0;
        Char.AGITITY = 1;
        Char.SPIRIT = 2;
        Char.HEALTH = 3;
        Char.LUCK = 4;
        GENDER_OF_CLAZZ = new byte[]{1, 1, 2, 1, 2};
        Char.maxHit = 150;
        Char.maxHit4 = 200;
        // Bang bu toa do NGUOI NGOI tren thu cuoi tool, theo huong {xuong, len, trai, phai}.
        // Gui nguyen bang cho client trong goi 26 (MessageCreator, "dx_horse_tool"), client
        // doc SO DONG tu goi (Unity MessageHandler.cs:1795, JAR MessageHandler.java:1754) roi
        // tra dong theo indexdxy (Char.typehorse) trong getdxHorse()/getdyParHorse(). Vi vay
        // them dong o day la du, khong phai dong goi lai client nao.
        //   0 Poro (va 5 skin thu bon chan)   1 Lan   2 Heo/Heo Bang   3 Rong Lon/Ngua Xuong
        //   4 hoa hinh
        //   5 SKIN KIM CANG LONG (11-12/09/2026): rong dai, yen o dung tam than khi quay ngang
        //     (dx +1/-1 thay cho +7/-7 cua Poro). Huong xuong nguoi ngoi sau dau rong: -28
        //     (bat dau tu -23 khop Rong Lon -22, roi nang them 5 theo anh chup nguoi choi
        //     gui - "cho cao len chut"). Huong len: -15 -> -10 -> 0, hai lan theo anh chup -
        //     o -10 than nguoi van "dau tren yen", ca yen do lo nguyen ben duoi (huong len
        //     client ve thu TRUOC nguoi nen yen khong the che nguoi, chi co cach ha than
        //     xuong che phan tren yen); o 0 con lai mep sau yen, giong huong ngang.
        //     Xem SkinLinhThu.INDEX_DXY va docs/skin-linh-thu.md muc 9.
        //     paintName cua client dat ten theo DONG 0 (Char.cs: num = dy_horse[0][dir]):
        //     dong moi CAO hon dong 0 qua ~10px la ten de dau (anh chup o -23 con cach ~14px,
        //     -28 con ~9px); THAP hon dong 0 thi vo hai, chi lam ten cach dau xa them (huong
        //     len 0 thap hon dong 0 tro 15px la co y).
        //   6 SKIN TIEN LOC (12/09/2026): nai/hoai nhon, than thap va gon hon Poro han han -
        //     dung chung dong 0 (nguoi nhac -20/-15 nhu Poro) lam nguoi noi han len tren, cach
        //     xa hong con thu, "ngoi lo lung tren khong" (nguoi choi bao kem anh chup). Chi ha
        //     rieng hai huong xuong/len (-20->-9, -15->-7, do bang tools/ThuChoNgoiSkinThu roi
        //     chon muc nguoi lot vao giua cum sung/canh, khop bo cuc cua 4 skin thu bon chan
        //     con lai); ngang giu nguyen -15/dx 7 nhu dong 0 vi huong do von da on (khong noi).
        //     Xem SkinLinhThu.INDEX_DXY va docs/skin-linh-thu.md muc 9.
        Char.dx_horse_tool = new byte[][]{{0, 0, 7, -7}, {0, 0, 5, -5}, {0, 0, 8, -8}, new byte[4], new byte[4],
            {0, 0, 1, -1}, {0, 0, 7, -7}};
        Char.dy_horse_tool = new byte[][]{{-20, -15, -15, -15}, {-25, -15, -15, -15}, {-20, -15, -15, -15}, {-22, -15, -16, -16}, {-22, -15, -17, -16},
            {-28, 0, -15, -15}, {-9, -7, -15, -15}};

        Char.Dx_Dy_WP_tool = new byte[][]{
            {-35, -27, -30, -37, -26, -55, -55, -47, -35, -55,-46,-35},
            {-75, -71, -60, -40, -52, -100, -75, -90, -85, -70,-48,-60}
        };

        // Do lech ve anh phi phong / canh o o xem truoc cua so Trang bi. Client doc
        // thanh Char.Dx_Dy_PP roi ve o (x + hang0[i], y + hang1[i]) voi i la so
        // getIdBigPhiPhong() tra ve o a[2].
        //   0 = Khong Tuoc (anh 53 rong -> -26 la giua)
        //   1 = Huyen Vu / Bong da / phi phong tim (anh 65 rong -> -32)
        //   2 = CANH. Truoc day la (60,-58), mot cap so khong ai dung: khong nhanh
        //       nao trong getIdBigPhiPhong() tra a[2] = 2, va dx duong 60 thi ve
        //       han ra ngoai nguoi. Nay dung cho nam bo canh, anh 66x50 moi khung
        //       nen -32 la giua, -63 dat canh vao sau than.
        //       BE NGANG 66 KHONG PHAI CHON BUA. Khung cua so Trang bi ve DE LEN
        //       nhan vat (WindowInfoScr.paintCharWearing ve nguoi truoc roi moi
        //       drawImage(Res.imgInfoWindow[0])), va lo trong suot cua khung do -
        //       file inv_0.png 117x104 trong /main.sh, giai ma XOR khoa
        //       "NguyenVanMinh" - chi trong suot tu x 25 den x 91. Nguoi ve o
        //       x = 58 nen canh chi duoc rong 67 diem. Ban dau lam anh 96 rong
        //       thi khung an mat khoang 20 diem dau canh moi ben.
        Char.Dx_Dy_PP_tool = new byte[][]{{-26, -32, -32}, {-58, -58, -63}};

        Char.Dx_Dy_AVT_tool = new byte[][]{
            {-44, -18, -44, -18, -18, -44, -44, -25, -20, -21, -20, -26, -28, -28,-22,-32},
            {-63, -63, -67, -69, -70, -63, -63, -69, -64, -62, -69, -69, -69, -85,-75,-75}
        };


        Char.ID_CHUYEN_MAP_CHIEN_TRUONG_MOBA = 0;
        Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA = 1;
        Char.ID_NOI_LUC = 2;
        Char.ID_HON_DON = 3;
        Char.ID_HOI_HON = 4;
        Char.ID_HON_NGUYEN = 5;
        Char.ID_BACH_TIEN = 6;
        Char.ID_DINH_CHU = 7;
        Char.ID_CAM_LANG = 8;
        Char.ID_PT_HOA_CONG = 9;
        Char.ID_PT_HON_NGUYEN = 10;
        Char.ID_PT_HON_DON = 11;
        Char.ID_HOI_SINH = 12;
        Char.ID_INFO_PLAYER = 13;
        Char.ID_TIME_NUI_KHO_BAU = 14;
        Char.ID_NOI_LUC_TANG = 15;
        Char.ID_DEM_NGUOC = 16;
        Char.ID_TY_SO = 17;
        Char.ID_TIME_LOI_DAI = 18;
        (Char.POS_WEARING_CHAR = new Hashtable<Integer, IndexWearing>()).put(0, new IndexWearing(new byte[1]));
        Char.POS_WEARING_CHAR.put(1, new IndexWearing(new byte[]{1}));
        Char.POS_WEARING_CHAR.put(2, new IndexWearing(new byte[]{2}));
        Char.POS_WEARING_CHAR.put(11, new IndexWearing(new byte[]{3}));
        Char.POS_WEARING_CHAR.put(9, new IndexWearing(new byte[]{4}));
        Char.POS_WEARING_CHAR.put(8, new IndexWearing(new byte[]{6, 5}));
        Char.POS_WEARING_CHAR.put(10, new IndexWearing(new byte[]{7}));
        Char.POS_WEARING_CHAR.put(12, new IndexWearing(new byte[]{8}));
        Char.POS_WEARING_CHAR.put(13, new IndexWearing(new byte[]{9}));
        Char.POS_WEARING_CHAR.put(3, new IndexWearing(new byte[]{10}));
        Char.POS_WEARING_CHAR.put(5, new IndexWearing(new byte[]{10}));
        Char.POS_WEARING_CHAR.put(4, new IndexWearing(new byte[]{10}));
        Char.POS_WEARING_CHAR.put(6, new IndexWearing(new byte[]{10}));
        Char.POS_WEARING_CHAR.put(7, new IndexWearing(new byte[]{10}));
        Char.POS_WEARING_CHAR.put(19, new IndexWearing(new byte[]{11}));
        Char.groupKickAnimal = new byte[][]{{18}, {16}, {14}, {15}, {17}};
        Char.groupKick = new byte[][]{{2, 3, 6, 7, 4, 5}, {0, 9}, {10, 8}, {10, 8}, {10, 8}, {10, 8}, {10, 8}, {10, 8}, {12, 11, 0, 9}, {1, 3, 6, 7, 4, 5}, {12, 11}, {8, 1}, {8, 1}};
        (Char.GROUP_KICH = new Hashtable<Integer, GroupKick>()).put(0, new GroupKick(new byte[]{2, 10}));
        Char.GROUP_KICH.put(1, new GroupKick(new byte[]{0, 4}));
        Char.GROUP_KICH.put(2, new GroupKick(new byte[]{5, 7}));
        Char.GROUP_KICH.put(3, new GroupKick(new byte[]{6, 1}));
        Char.GROUP_KICH.put(4, new GroupKick(new byte[]{2, 10}));
        Char.GROUP_KICH.put(5, new GroupKick(new byte[]{8, 3}));
        Char.GROUP_KICH.put(6, new GroupKick(new byte[]{0, 4}));
        Char.GROUP_KICH.put(7, new GroupKick(new byte[]{8, 3}));
        Char.GROUP_KICH.put(8, new GroupKick(new byte[]{6, 1}));
        Char.GROUP_KICH.put(9, new GroupKick(new byte[]{-1}));
        Char.GROUP_KICH.put(10, new GroupKick(new byte[]{5, 7}));
        Char.GROUP_KICH.put(11, new GroupKick(new byte[]{-1}));
        Char.Lvx3Exp = 0;
        Char.timex2 = new Vector<String>();
        Char.timex3 = new Vector<String>();
        Char.timex50 = new Vector<String>();
        Char.KHAM_MU = 0;
        Char.KHAM_DOC = 1;
        Char.KHAM_CHOANG = 2;
        Char.KHAM_BANG = 3;
        Char.KHAM_HOA_DA = 4;
        Char.KHAM_GIAM_TOC = 5;
        Char.SET_LAN = 0;
        Char.DOC_LAN = 1;
        Char.BANG_LAN = 2;
        Char.LUA_LAN = 3;
        Char.namelan = new String[]{"Bang", "Set", "Doc", "Lua"};
        Char.idmapthangbe = new int[]{2, 6, 8, 9};
        Char.xythangbe = new int[][]{{20, 20}, {36, 33}, {40, 40}, {33, 44}, {98, 67}, {48, 24}, {17, 64}, {77, 20}, {35, 10}, {43, 12}, {20, 26}, {2, 20}};
        Char.idmapchucnu = new int[]{113, 10, 15, 18, 21};
        Char.xychucnu = new int[][]{{87, 91}, {38, 8}, {9, 68}, {36, 4}, {8, 12}, {6, 65}};
        Char.HP_MONSTER_VANTIEU = new int[]{5500000, 15000000, 19000000, 23000000, 27000000, 31000000};
        Char.DEF_MONSTER_VANTIEU = new int[]{9000, 16200, 22200, 45000, 65000, 85000};
        Char.UP_HP_MONSTER_VANTIEU = new int[]{0, 10, 20, 35, 50, 65, 65, 65};
        Char.UP_DEF_MONSTER_VANTIEU = new int[]{0, 4, 8, 12, 14, 20, 20};
        Char.INDIVITUAL = 0;
        Char.CLAN = 1;
        Char.NATION = 2;
        Char.EXP_NV_HANG_NGAY = new long[][]{{500000L, 1200000L, 2000000L}, {1200000L, 2500000L, 4000000L}, {3000000L, 6000000L, 10000000L}, {6000000L, 16000000L, 25000000L}, {24000000L, 50000000L, 80000000L}, {40000000L, 90000000L, 150000000L}, {80000000L, 180000000L, 300000000L}};
        Char.indexInfoAdv = 0;
        Char.isRemove = false;
        Char.timeSendRaoVat = System.currentTimeMillis() + 20000L;
        Char.timeAddMoreHs = 30000;
        Char.TOTAL_FANCUNG = 100;
        Char.ID_HEAD_TIEU_LONG_NU = 8784;
        Char.ID_HEAD_DUONG_QUA = 8783;
        Char.ID_HEAD_HANG_NGA = 8792;
        Char.ID_BODY_HANG_NGA = 8793;
        Char.ID_LEG_HANG_NGA = 8794;
        Char.ID_MAT_NA_BI_NGO = 8800;
        Char.ID_MAT_NA_QUY = 8801;
        Char.ID_MAT_NA_SOI = 8802;
        Char.ID_MAT_NA_ZOOMBI = 8803;
        Char.ID_MAT_NA_PANDA = 8804;
        Char.buffSkill = new byte[][]{{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};
        Char.hourShield = new byte[][]{{6, 10}, {11, 15}, {16, 20}, {20, 24}};
        Char.offAuto = true;
        Char.nameTop100DaiViet = new String[]{"chucteo", "bamtoc", "duoitoc", "cbrong", "14LaTao", "tokyo1", "zzshaclozz", "zkimdongz", "uylong", "baophuong", "aloha", "theanh", "xemtaone", "kiemhiep", "xedap96vn", "rainy", "9999p", "buadapcho", "nhubg83", "lamborgini", "ChienBjnh", "giangho02", "daophap", "chjenthan", "longvuong", "boday", "genha", "zhieudolaz", "betyxiu", "start", "chihuy", "zz321pro", "BaiTranh", "zzvip9zz", "taolaso12", "zzz123zzz", "vebumedj", "thanhomenh", "zmakiem", "daika", "saobang", "nhoyeu", "hjhehe", "99xu", "fogvan", "badaosv", "bua223", "xxbadaoxx", "zxpeyeuxz", "0owaterdam", "tokyo", "duongpro", "Honey", "kathy8x", "b0ydepzai", "boyydepzai", "khoc5", "shdida", "gakaka", "thandao", "b0ydepzaii", "lyhamchoi", "XauTrai", "khoc1", "Apply3", "xuanquynh", "khoc123", "Fighter", "huebg87", "vida5", "zoro", "thanbua", "dawat", "taxi2017", "ikuta", "tuananh", "Apply2", "sangbg82", "Apply", "trjutulong", "7DAPPRO", "satang1", "hoavjnh", "batgioi1", "24htansat", "tragkhuyet", "vipvipday1", "thienlong", "Trau", "nicolaizs3", "dosatthue", "bmw8", "boyvip", "heawen12", "ngoclinh", "tokyo3", "nhuom", "Nobita", "duckien", "tokyo2"};
        Char.timeExpireAutoSkill = new long[]{2592000000L, 3600000L};
        Char.timeExpireAutoGetItem = new long[]{2592000000L, 3600000L};
        Char.lixilucky = new byte[]{-1, -1, -1, -1, -1};
        Char.totalLixiOpened = 0;
        Char.XU_LI_XI = new int[]{1000000, 1000000, 1000000, 2000000, 5000000};
        Char.daysomaymm = "";
        Char.numberLucky = 5300;
        Char.numberRandom = 4067;
        Char.total_open = 0;
        Char.idGif35 = new short[]{80, 101, 73, 136, 115, 108, 94, 129, 87, 122};
        Char.ID_BIG_AVT_AO_TIEN_NU = 613;
        Char.ID_BIG_AVT_AO_TINH_QUAN = 615;
        Char.ID_BIG_AVT_PP_KHONG_TUOC = 614;
        Char.ID_BIG_AVT_PP_HUYEN_VU = 625;
        // 708 chu khong phai 711. Anh lon phi phong phai la dai 3 frame xep doc, be ngang
        // 65-86 (Khong Tuoc 614, Huyen Vu 625 co 65x186, tim 714 co 86x177). map/icon/708.png
        // dung dang do: 81x180 = 3 frame 81x60. Con 711 chi la ICON 18x54 (3 frame 18x18)
        // nen client cat ra mieng 18x18 -> hien khong ra hinh phi phong bong da.
        Char.ID_BIG_AVT_PP_BONG_DA = 708;
        Char.ID_BIG_AVT_AO_DAI_NAM = 628;
        Char.ID_BIG_AVT_AO_DAI_NU = 629;
        Char.idRankGov = new byte[]{56, 54, 55, 53};
        Char.gov = new Goverment[]{new Goverment(), new Goverment(), new Goverment()};
        Char.allItemLuckyDraw = new Vector<ItemLuckyDraw>();
        Char.pcWinLuckyDraw = 2;
        Char.nItemLucky = 11;
        // addMore = 7 (truoc la 2): CharSuKien.loadAllItemLucky() vua chen 5 phan
        // tu Canh (898-902) ngay sau id 619, day toan bo vi tri phia sau lui 5 o.
        // groupLucky[] o duoi la CHI SO TUONG DOI (chua cong addMore) tro vao vong
        // lap idgem/ps trong loadAllItemLucky() - tang addMore len 7 de bu dung 5
        // o vua chen, neu khong phan thuong phu se boc nham sang Canh/thu cung.
        Char.addMore = 7;
        Char.groupLucky = new byte[][]{{42, 43, 44, 45, 46, 47, 48, 49}, {51}, {15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41}, {50, 52}};
        Char.except_quay_so = null;
        Char.timeGetPhuongHoang = 0L;
        Char.TOP_LUONG = new Hashtable<String, TopLuong>();
        Char.have8k = 0;
        Char.lcx_chan_nui_chau_bau = new int[][]{{7, 4}, {19, 4}, {30, 4}, {42, 4}, {52, 4}};
        Char.lcy_chan_nui_chau_bau = new int[][]{{6, 3}, {6, 3}, {6, 3}, {6, 3}, {6, 3}};
        Char.infoAdv = new Vector<AdvInfo>();
        Char.infoAdv2 = new Vector<AdvInfo>();
        Char.TOP_CHAR_USE_LUONG = new Hashtable<String, Integer>();
        Char.nauBanhChung = new Cooking();
        Char.nauBanhTet = new Cooking();
        Char.isBaotriVxmm = false;
        Char.ID_POINT_NOI_LUC_CHIEN_TRUONG = new short[]{0, 20, 60, 140, 260, 410, 590, 800, 1050, 1350};
        Char.ALL_PLAYER_XOSO = new Hashtable<String, PlayerXoso>();
        Char.MONEY_XOSO = 0L;
        Char.timeXoso = System.currentTimeMillis();
        Char.charNameXosoWin = "";
        Char.moneyCharXosowin = 0;
        Char.moneyCharXosoBid = 0;
        Char.ALL_PLAYER_XOSO_low = new Hashtable<String, PlayerXoso>();
        Char.MONEY_XOSO_low = 0L;
        Char.timeXoso_low = System.currentTimeMillis();
        Char.charNameXosoWin_low = "";
        Char.moneyCharXosowin_low = 0;
        Char.moneyCharXosoBid_low = 0;
        Char.onOffThamdinh = 1;
        Char.onOffTime180 = 0;
        Char.chartopchoitrungthu = "";
        Char.dayEndVip = 1;
        Char.timeEndVip = 23;
        Char.minuteEnd = 59;
        Char.soluong_loa_vip = new byte[]{0, 1, 3, 5};
        Char.up_px_xu_vip = new byte[]{0, 0, 10, 30};
    }
    
    //thêm 1 rồng
    private int saturatingAdd(int a, int b) {
    long s = (long) a + (long) b;
    if (s > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (s < Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return (int) s;
}


    public static int getIndexItemWearing(final int type, int posNhan) {
        if (type != 8) {
            posNhan = 0;
        }
        return Char.POS_WEARING_CHAR.get(type).getIndex(posNhan);
    }

    public boolean countHit() {
        return false;
    }

    public int receiveXP() {
        if (!Map.checkRcvXP) {
            return 1;
        }
        if (this.timeXP < 180) {
            return 1;
        }
        if (this.timeXP < 300) {
            return 2;
        }
        return 3;
    }

    /**
     * So o thuoc cua nhan vat. Bang so dong cua bang data_potion (id 0..161) va bang
     * so truong CSV cua cot tob_char.potion. Doi so nay la phai doi ca hai cho.
     */
    public static final int SO_O_THUOC = 162;

    /** Da canh bao thieu dong data_potion chua (chi ghi log mot lan cho do ram log). */
    private static boolean daCanhBaoThieuPotion = false;

    /**
     * Danh sach thuoc dang co, gui trong goi hanh trang (MessageCreator, type 0).
     *
     * KHONG DUOC gui id thuoc >= so DONG cua bang data_potion. Client biet so o thuoc
     * qua goi dang nhap (LoginHandler.createMsgLogin gui Map.potionTemplates2.size()
     * -> Char.CHAR_POTION) roi cap phat MainChar.listPotion dung bang do, va khi doc
     * goi hanh trang no gan thang listPotion[id] KHONG kiem tra bien. Gui id vuot tran:
     *
     *   1. client nem IndexOutOfRange giua luc doc goi 16
     *   2. setInventory() khong chay -> mang potions cua client giu nguyen do dai
     *      khoi tao (25)
     *   3. vong dung danh sach tui do (WindowInfoScr.getInventori) cham potions[25]
     *      va nem tiep — ma ca vong nay nam trong MOT try duy nhat, dung truoc phan
     *      nguyen lieu / thu / vat pham
     *   -> NGUOI CHOI THAY CA HANH TRANG RONG: khong thuoc, khong nguyen lieu, khong
     *      vat pham. Trang bi cung trong theo vi cung tra cuu template item.
     *
     * Vi vay bo qua id vuot tran va ghi log de biet ma bo sung dong cho data_potion.
     */
    public Vector<InfoCharPotion> getAllPotion() {
        final Vector<InfoCharPotion> all = new Vector<InfoCharPotion>();
        final int tranClient = Map.potionTemplates2.size();
        for (int i = 1; i < this.potions.length; ++i) {
            if (this.potions[i] <= 0) {
                continue;
            }
            if (tranClient > 0 && i >= tranClient) {
                if (!daCanhBaoThieuPotion) {
                    daCanhBaoThieuPotion = true;
                    System.out.println("CANH BAO: bang data_potion chi co " + tranClient
                            + " dong nhung nhan vat dang giu thuoc id " + i
                            + ". Bo qua de client khong bi rong hanh trang."
                            + " Bo sung dong cho data_potion (can du id 0.."
                            + (this.potions.length - 1) + ") roi bat lai server.");
                }
                continue;
            }
            all.add(new InfoCharPotion(i, this.potions[i]));
        }
        return all;
    }

    public void doAddItemSellShop(final Item it) {
        if (it.timeLoan > 0L && it.minuteExist > 0) {
            return;
        }
        this.sellShopItem.add(it);
        if (this.sellShopItem.size() > 20) {
            this.sellShopItem.remove(0);
        }
    }

    public void doRegentHpByBattle() {
        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
        if (c != null) {
            final long pc = c.getNoiLuc();
            this.hp += (int) (this.maxhp * pc / 100L);
            this.mp += (int) (this.maxmp * pc / 100L);
            if (this.hp > this.maxhp) {
                this.hp = this.maxhp;
            }
            if (this.mp > this.maxmp) {
                this.mp = this.maxmp;
            }
            try {
                this.sendMessage(MessageCreator.createNew_HMP_Message(this, (int) (this.maxhp * pc / 100L)));
                this.sendToNearPlayer(MessageCreator.createNew_HMP_Message(this, (int) (this.maxhp * pc / 100L)));
                MessageCreator.createMsgUseHpMP(this, (int) (this.maxmp * pc / 100L), 4);
            } catch (final IOException ex) {
            }
        }
    }

    public void confirmCapcha(final String st) {
        ++this.nConfirm;
        if (this.nConfirm > 3) {
            CharManager.instance.kickPlayer(this, "char 2");
            return;
        }
        if (!st.equals(this.stCapcha)) {
            this.stCapcha = MessageCreator.createMsgCapcha(this);
        } else {
            this.timeStartHack = 0L;
            this.nHit = 0;
            this.nConfirm = 0;
            this.timeHit = 0L;
            this.nhit4 = 0;
            this.timeHit4 = 0L;
        }
    }

    /**
     * SKIN LINH THU (Tien Loc): phan cong them vao tung diem tiem nang, TINH TAI CHO.
     *
     * VI SAO KHONG NANG THANG this.strength/agitity/spirit/health: bon truong do duoc
     * ghi xuong CSDL nguyen si (DatabaseNhanVat gop chuoi "strength,agitity,..."). Nang
     * chung len roi tra lai o cuoi calculateAttrib() la dat cuoc vao viec khong luong nao
     * luu nhan vat trong dung khoang giua — thua mot lan la so da phong len nam VINH VIEN
     * trong CSDL, va con phong tiep moi lan luu sau do. Khong co gia tri nao dang de doi
     * lay rui ro do.
     *
     * Tinh tai cho thi khong co trang thai, khong can dat lai, khong cong don, va khong
     * bao gio cham vao gia tri duoc luu. Cai gia phai tra la moi cong thuc dung diem tiem
     * nang deu phai cong tay mot lan — 28 cho, va test/KiemSkinLinhThu chot lai tung cho.
     *
     * Tat cung luc voi hinh anh (di qua dangMang), giong moi noi tai skin khac.
     */
    public int tnSucManh() {
        return this.strength * SkinLinhThu.pcTiemNang(this) / 100;
    }

    public int tnKheoLeo() {
        return this.agitity * SkinLinhThu.pcTiemNang(this) / 100;
    }

    public int tnTinhThan() {
        return this.spirit * SkinLinhThu.pcTiemNang(this) / 100;
    }

    public int tnSucKhoe() {
        return this.health * SkinLinhThu.pcTiemNang(this) / 100;
    }

    /**
     * May man — DIEM TIEM NANG THU NAM, dung quen nhu lan dau.
     *
     * Nhan vat co NAM diem tiem nang chu khong phai bon: xem cho dat lai chi so goc,
     * Database.basicPoint[class][0..4] = suc manh, kheo leo, tinh than, suc khoe, MAY MAN.
     * Bo sot no thi "+20% diem tiem nang" cong cho bon cai dau ma CHI MANG dung im
     * (crit1 = (luck + ...) / 5), va bang chi so hien mot dong khong nhuc nhich giua bon
     * dong tang — nguoi choi nhin la biet co gi do sai.
     */
    public int tnMayMan() {
        return this.luck * SkinLinhThu.pcTiemNang(this) / 100;
    }

    public void resetNguHanh() {
        this.attNguHanh = new byte[5];
        this.attNguHanhAnimal = new byte[2];
        for (int i = 0; i < (this.wItems[this.slotWearing]).length; i++) {
            Item item = this.wItems[this.slotWearing][i];
            if (item != null) {
                item.beKick = 0;

                if (item.isPhiphonghehehe()) {
                    item.beKick = 1;

                }

            }
        }
        Vector<Item> wItems = getAnimalWearing();
        for (int j = 0; j < wItems.size(); j++) {
            ((Item) wItems.get(j)).beKick = 0;
        }
        this.newAtb = new byte[10];
        this.addMoreLevelSkill = new byte[15];
        this.lockAtb = new byte[3];
    }

    public void reCheckAtb5() {
        calculateAttrib();
        try {
            sendMessage(MessageCreator.createMainCharInfoMessage(this));
        } catch (Exception exception) {
        }
    }

    public String checkNhanLuongBang() {
        if (this.idClan == -1) {
            return null;
        }
        if (System.currentTimeMillis() - this.timeInClan < 0L) {
            return getDayTimeByTime(this.timeInClan);
        }
        return null;
    }

    public boolean canPhatLuongBang() {
        final NewClan cl = NewClan.getClan(this.idClan);
        return false;
    }

    public void setSkillClan(final int idSkill, final short time, final int devoteBuy, final byte lvSkill) {
        this.skillClan[idSkill] = time;
        this.pointCongHienClan -= devoteBuy;
        this.timeUseSkillClan[idSkill] = System.currentTimeMillis();
        this.skillLvClan[idSkill] = lvSkill;
        this.sendMessage(MessageCreator.createSkillClan(this));
    }

    public void checkKick5AttAnimal() {
        this.isFullsetAnimal = 0;
        Vector<Item> wItems = getAnimalWearing();
        byte type = 0;
        while (type < wItems.size()) {
            Item mItem = wItems.get(type);
            try {
                // RAO CHI SO: ben duoi tra groupKickAnimal[mItem.getType() - 14], ma mang
                // do chi dai 5 (type 14..18). Vat pham SKIN LINH THU mang type 30 -> chi
                // so 16 -> ArrayIndexOutOfBounds. Loi do roi vao catch rong ngay duoi nen
                // no IM LANG: khong log gi, chi lang le bo qua phan con lai cua mon dang
                // xet. Dung loai loi da tung lam hong createAnimalWearingMessage.
                //
                // Khong trong cay vao "skin luon co heItem = -1": heItem la truong cua
                // TUNG MON chu khong phai cua template, va co nhieu duong gan no
                // (ItemChangeColor, nang cap ngu hanh). Chan bang chinh do dai mang thi
                // dung du sau nay them type moi vao bang trang bi thu.
                //
                // KHONG dung "continue": vong lap nay tang bien dem o CUOI than vong,
                // continue se nhay qua dong do va treo may chu.
                final int loaiTrangBiThu = mItem.getType() - 14;
                if (mItem.heItem > -1
                        && loaiTrangBiThu >= 0 && loaiTrangBiThu < groupKickAnimal.length) {
                    if (mItem.colorName == 1 && mItem.hangItem == 1
                            && mItem.plus == 15) {
                        this.isFullsetAnimal = (byte) (this.isFullsetAnimal + 1);
                    }
                    for (int i = 0; i < wItems.size(); i++) {
                        Item it = wItems.get(i);
                        for (int j = 0; j < (groupKickAnimal[mItem.getType() - 14]).length; j++) {
                            if (it.magic_physic == mItem.magic_physic && it.getType() == groupKickAnimal[mItem.getType() - 14][j] && (mItem.heItem + 1) % 5 == it.heItem && it.heItem > -1) {
                                if (it.beKick != 1) {
                                    it.beKick = 1;
                                    for (int k = 0; k < 2; k++) {
                                        this.attNguHanhAnimal[k] = (byte) (this.attNguHanhAnimal[k] + it.otherAtt[k]);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (Exception exception) {
            }
            type = (byte) (type + 1);
        }
    }

    public boolean checkGroupKick() {
        return false;
    }

    public void checkKickAtt5() {
        int type = 0;
        this.fullSet15 = 0;
        this.fullSet10 = 0;
        this.fullSet13 = 0;
        Vector<Item> wItems = getCharWearing();
        wItems = changePosRing(wItems);
        while (type < wItems.size()) {
            Item mItem = null;
            mItem = wItems.get(type);
            int i;
            for (i = 0; i < mItem.newAtb.length; i++) {
                this.newAtb[i] = (byte) (this.newAtb[i] + mItem.newAtb[i]);
            }
            for (i = 0; i < mItem.addMoreLevelSkill.length; i++) {
                this.addMoreLevelSkill[i] = (byte) (this.addMoreLevelSkill[i] + mItem.addMoreLevelSkill[i]);
                if (this.addMoreLevelSkill[i] > 1) {
                    this.addMoreLevelSkill[i] = 1;
                }
            }
            for (i = 0; i < mItem.lockAtb.length; i++) {
                this.lockAtb[i] = (byte) (this.lockAtb[i] + mItem.lockAtb[i]);
            }
            if (mItem.heItem > -1) {
                if (mItem.plus == 15) {
                    this.fullSet15 = (byte) (this.fullSet15 + 1);
                }
                if (mItem.plus >= 13) {
                    this.fullSet13 = (byte) (this.fullSet13 + 1);
                }
                if (mItem.plus >= 10) {
                    this.fullSet10 = (byte) (this.fullSet10 + 1);
                }
                try {
                    int indexMain = getIndexItemWearing((mItem.getTemplate()).type, mItem.pos);
                    byte[] group = ((GroupKick) GROUP_KICH.get(Integer.valueOf(indexMain))).getGroup();
                    for (int j = 0; j < wItems.size(); j++) {
                        Item it = wItems.get(j);
                        int checkIndex = getIndexItemWearing((it.getTemplate()).type, it.pos);
                        if (it.magic_physic == mItem.magic_physic || Map.isWeapone(it.getType()) || Map.isWeapone(mItem.getType())) {
                            for (int k = 0; k < group.length; k++) {
                                if (checkIndex == group[k] && (mItem.heItem + 1) % 5 == it.heItem && it.heItem > -1) {
                                    if (it.beKick != 1) {
                                        it.beKick = 1;
                                        for (int m = 28; m < 33; m++) {
                                            this.attNguHanh[m - 28] = (byte) (this.attNguHanh[m - 28] + it.atb[m] + it.getValueUpByVuKhiThoiTrang(it.atb[m], this.itemVukhiThoiTrang));
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            type++;
        }
    }

    public void setItemChange5() {
        Vector<Item> wItems = getCharWearing();
        this.itemChange5.removeAll(this.itemChange5);
        for (int i = 0; i < wItems.size(); i++) {
            Item it = wItems.get(i);
            if (it.heItem != -1 && it.level >= 40 && it.lock == 1) {
                this.itemChange5.add(it);
            }
        }
    }

    public void desTroy() {
        for (int i = 0; i < 6; ++i) {
            this.countDown[i] = 0;
            this.timeUseBuff[i] = 0L;
            this.arrayBuff[i] = -1;
            this.lvBuff[i] = 0;
        }
        this.percentBuff[0] = 0;
        this.percentBuff[1] = 0;
        this.percentBuff[2] = 0;
        this.divSpeed = 1;
        try {
            this.timeCrazy = 0L;
            this.isCrazy(this.isCrazy);
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            this.sendToNearPlayer(MessageCreator.createCharInfo(this));
            MessageCreator.createMsgCharMonster(this, this);
        } catch (final Exception ex) {
        }
        this.resetTimeLastuseSkill();
    }

    public int getPK() {
        return this.killer / 100;
    }

    public void sendMsgDropList(final Vector<Actor> dropList) {
        try {
            final Message m = new Message(-55);
            m.dos.writeByte(dropList.size());
            for (int i = 0; i < dropList.size(); ++i) {
                final Actor a = dropList.get(i);
                m.dos.writeByte(a.cat);
                if (a.cat == 4) {
                    m.dos.writeShort(a.getType());
                } else if (a.cat == 3) {
                    m.dos.writeShort(((Item) a).getTemplate().id);
                } else if (a.cat == 6) {
                    m.dos.writeShort(((GemItem) a).idGemTemplate);
                } else if (a.cat == 7) {
                    m.dos.writeShort(((ShopTemplate) a).id);
                }
                if (a.cat == 7) {
                    m.dos.writeShort(((ShopTemplate) a).realId);
                } else {
                    m.dos.writeShort(a.id);
                }
                m.dos.writeShort(a.x);
                m.dos.writeShort(a.y);
            }
            this.sendMessage(m);
            this.sendToNearPlayer(m);
        } catch (final Exception ex) {
        }
    }

    // Than method (110 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public int expReceive(int xpReceive) {
        return CharCapNhat.expReceive(this, xpReceive);
    }

    public static int TangEXP = 1;

    public int getX2() {
        if (this.map.isDun()) {
            return 0;
        }
        int x2 = this.x2;
        if (Map.minuteX2Country[this.myCountry] > 0L) {
            x2 = Map.x2Country[this.myCountry];
        }
        if (x2 == 0 && this.trungThuMoc300HetHan > System.currentTimeMillis()) {
            // Trung Thu 2026 - moc 300 lan tha den (yeu cau nguoi dung "tang vang up khi
            // danh quai" trong 24h, xem MapSuKien.java doMenuTrungThu2016): tan dung lai
            // bac x2=1 co san (+50% vang roi khi giet quai, xem Monster.java ~dong 882-883)
            // thay vi lam rieng mot co che tang vang moi - khong ghi de neu nguoi choi dang
            // co ve x2 vang khac manh hon (x2Country hoac x2 tu mua rieng).
            x2 = 1;
        }
        return (this.x25Exp > 0) ? 3 : x2;
    }

    public void setX2(final int id) {
     
        this.x2 = (byte) id;
        if (id == 1) {
            this.halfGoldTime = System.currentTimeMillis();
            this.fullGoldTime = 0L;
            this.x25Exp = 0;
            this.minuteExp = 0;
        }
        if (id == 2 || id == 4 || id == 5) {
            this.halfGoldTime = 0L;
            this.fullGoldTime = System.currentTimeMillis();
            this.x25Exp = 0;
            this.minuteExp = 0;
            if (id == 4 || id == 5) {
                this.minuteExp = 60;
                if (id == 5) {
                    this.minuteExp = 180;
                }
                this.x2 = 2;
                try {
                    final Message m = new Message(86);
                    m.dos.writeByte(2);
                    m.dos.writeByte(this.x2);
                    m.dos.writeInt(this.getGoldTime());
                    m.dos.writeUTF(this.getInfoX2());
                    this.sendMessage(m);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (id == 3) {
            this.halfGoldTime = 0L;
            this.fullGoldTime = 0L;
            this.x2 = 0;
            this.x25Exp = 3600;
        }
    }

    public int getGoldTime() {
        if (Map.minuteX2Country[this.myCountry] > 0L) {
            final int minute = (int) ((Map.minuteX2Country[this.myCountry] - System.currentTimeMillis()) / 1000L / 60L);
            return (minute > 0) ? (minute + 1) : 0;
        }
        if (this.x2 == 2) {
            if (this.minuteExp > 0) {
                return this.minuteExp;
            }
            return 1440 - (int) (System.currentTimeMillis() - this.fullGoldTime) / 60000;
        } else {
            if (this.x2 == 1) {
                return 1440 - (int) (System.currentTimeMillis() - this.halfGoldTime) / 60000;
            }
            if (this.x2 == 3) {
                return 1440 - (int) (System.currentTimeMillis() - this.halfGoldTime) / 60000;
            }
            return 0;
        }
    }

    public String getInfoX2() {
        if (Map.minuteX2Country[this.myCountry] > 0L) {
            if (Map.x2Country[this.myCountry] == 1) {
                return "50%";
            }
            if (Map.x2Country[this.myCountry] == 2) {
                return "100%";
            }
            if (Map.x2Country[this.myCountry] == 3) {
                return "150%";
            }
        }
        if (this.x2 == 1) {
            return "50%";
        }
        if (this.x2 == 2) {
            return "100%";
        }
        if (this.x2 == 3) {
            return "150%";
        }
        return "";
    }

    private int countGem() {
        int k = 0;
        for (int i = 0; i < this.listGemitem.length; ++i) {
            if (this.listGemitem[i] > 0) {
                ++k;
            }
        }
        return k;
    }

    private int countGemLock() {
        int k = 0;
        for (int i = 0; i < this.listGemitemLock.length; ++i) {
            if (this.listGemitemLock[i] > 0) {
                ++k;
            }
        }
        return k;
    }

    private int countSpecial() {
        boolean isTicket1 = false;
        boolean isTicket2 = false;
        int n = 0;
        for (int i = 0; i < this.specialItem.size(); ++i) {
            if (!isTicket1 && this.specialItem.get(i).id == 0) {
                ++n;
                isTicket1 = true;
                if (isTicket2) {
                    return n;
                }
            }
            if (!isTicket2 && this.specialItem.get(i).id == 1) {
                ++n;
                isTicket2 = true;
                if (isTicket1) {
                    return n;
                }
            }
        }
        return n;
    }

    public boolean hadGemItem(final int id) {
        return this.listGemitem[id] > 0;
    }

    public boolean hadSpecialItem(final int id) {
        for (int j = 0; j < this.specialItem.size(); ++j) {
            if (this.specialItem.get(j).id == id) {
                return true;
            }
        }
        return false;
    }

    public int countSlotInventory() {
        final int n = this.iItems.size() + this.countPotion() + this.tItems.size() + this.rItems.size() + this.countGemLock() + this.countGem() + this.countSpecial();
        return this.MAX_INVENTORY * this.maxBag - n;
    }

    public boolean isFullInventory() {
        final int n = this.iItems.size() + this.countPotion() + this.tItems.size() + this.rItems.size() + this.countGemLock() + this.countGem() + this.countSpecial();
        return n >= this.MAX_INVENTORY * this.maxBag;
    }

    public short countPotion() {
        short a = 0;
        for (int i = 1; i < this.potions.length; ++i) {
            if (this.potions[i] > 0) {
                ++a;
            }
        }
        return a;
    }

    public void loadConfigMap() {
        if (Map.config == 0) {
            this.resetConfig();
        } else if (Map.config == 1) {
            this.nNearchar = 30000;
        }
    }

    public void resetConfig() {
        if (this.typeConfig == 0) {
            this.nNearchar = 8;
        } else if (this.typeConfig == 1) {
            this.nNearchar = 12;
        } else if (this.typeConfig == 2) {
            this.nNearchar = 20;
        }
    }

    public void config(final int type) {
        if (type == 0) {
            this.nNearchar = 20;
        } else if (type == 1) {
            this.nNearchar = 25;
        } else if (type == 2) {
            this.nNearchar = 30;
        } else if (type == 3) {
            this.nNearchar = 15;
        } else if (type == 4) {
            this.rcvInvite = true;
        } else if (type == 5) {
            this.rcvInvite = false;
        } else if (type == 6) {
            this.slotWearing = (byte) ((this.slotWearing + 1) % 2);
            this.calculateAttrib();
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                final Message m = MessageCreator.createCharWearingMessage(this, this);
                this.sendMessage(m);
                this.sendToNearPlayer(m);
                this.sendToNearPlayer(MessageCreator.createCharInfo(this));
                MessageCreator.createMsgCharMonster(this, this);
                final Vector<Item> wItems = this.getCharWearing();
                for (int i = 0; i < wItems.size(); ++i) {
                    final Item item = wItems.get(i);
                    if (item.getTemplate().type > 2 && item.getTemplate().type < 8) {
                        this.sendMessage(MessageCreator.createCharWeaponeData(1, item.getTemplate().type, item.getTemplate().style, item.getTemplate().color));
                        return;
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        if (type < 4) {
            this.typeConfig = (byte) type;
            try {
                final Message msgCfg = new Message(104);
                msgCfg.dos.writeByte(this.typeConfig);
                msgCfg.dos.writeByte(this.autoSkill);
                this.sendMessage(msgCfg);
            } catch (final Exception ex) {
            }
        }
    }

    public void putIDItem2IDGen(final Vector<Item> iItems) {
        try {
            for (final Item item : iItems) {
                RealController.intance.idGen.putID(item.id, 3, "return id item 2 IDGEN");
            }
        } catch (final Exception ex) {
        }
    }

    public void putIDSpecialItem2IDGen() {
        try {
            for (final ShopTemplate item : this.specialItem) {
                RealController.intance.idGen.putID(item.realId, 7, "return id item 2 IDGEN");
            }
        } catch (final Exception ex) {
        }
    }

    public void putIDChar2IDGen() {
        try {
            RealController.intance.idGen.putID(this.id, 0, "return id player 2 IDGEN");
        } catch (final Exception ex) {
        }
    }

    public void putIDPotion2IDGen() {
        try {
            for (final Item item : this.iItems) {
                RealController.intance.idGen.putID(item.id, 3, "return id item 2 IDGEN");
            }
        } catch (final Exception ex) {
        }
    }

    /**
     * Mo ta vi tri chi tiet cua mot Char de ghi kem vao cac dong log giao dich/ky gui
     * (14/09/2026): ten map + id map, khu/instance (region - 0 tru khi dang trong ban
     * do tach nhieu khu nhu boss), toa do x,y luc ghi log.
     */
    public static String moTaViTri(final Char p) {
        return "map:" + Map.getNameMap(p.mapID) + "(" + p.mapID + ") khu:" + p.region
                + " xy:" + p.x + "," + p.y;
    }

    /**
     * Chuyen thuoc hai ben sau khi giao dich hoan tat. Tra ve false va KHONG DOI GI neu mot
     * ben khong con du thuoc nhu luc bo vao (ban cu tru ra am roi chan ve 0 - ben nhan van
     * nhan du -> thuoc tu khong khi khi mot luong khac vua tieu thuoc trong khe giua kiem tra
     * o MapMuaBan va luc tru o day).
     */
    public boolean doAddPotionTrad2Inventory() {
        final Char dp = this.userTrade.get(0);
        for (int i = 0; i < this.tPotions.length; ++i) {
            if (this.tPotions[i] < 0 || this.tPotions[i] > this.potions[i]) {
                return false;
            }
        }
        for (int i = 0; i < dp.tPotions.length; ++i) {
            if (dp.tPotions[i] < 0 || dp.tPotions[i] > dp.potions[i]) {
                return false;
            }
        }
        String info = String.valueOf(this.charname) + " chuyển_";
        for (int i = 0; i < this.tPotions.length; ++i) {
            if (this.tPotions[i] > 0) {
                info = String.valueOf(info) + this.tPotions[i] + "_" + LoginHandler.PORTION_NAME[i] + " n ";
                final int[] potions = this.potions;
                final int n = i;
                potions[n] -= this.tPotions[i];
                if (this.potions[i] < 0) {
                    this.potions[i] = 0;
                }
            }
        }
        info = String.valueOf(info) + " >> " + this.userTrade.get(0).charname + " chuyển_";
        for (int i = 0; i < this.userTrade.get(0).tPotions.length; ++i) {
            if (this.userTrade.get(0).tPotions[i] > 0) {
                info = String.valueOf(info) + this.userTrade.get(0).tPotions[i] + "_" + LoginHandler.PORTION_NAME[i] + " n ";
                final int[] potions2 = this.userTrade.get(0).potions;
                final int n2 = i;
                potions2[n2] -= this.userTrade.get(0).tPotions[i];
                if (this.userTrade.get(0).potions[i] < 0) {
                    this.userTrade.get(0).potions[i] = 0;
                }
            }
        }
        this.tPotions = null;
        this.tPotions = new int[162];
        this.userTrade.get(0).tPotions = null;
        this.userTrade.get(0).tPotions = new int[162];
        info = String.valueOf(info) + " ||| " + this.charname + " nhận_";
        for (int i = 0; i < this.rPotions.length; ++i) {
            if (this.rPotions[i] > 0) {
                info = String.valueOf(info) + this.rPotions[i] + "_" + LoginHandler.PORTION_NAME[i] + " n ";
                final int[] potions3 = this.potions;
                final int n3 = i;
                potions3[n3] += this.rPotions[i];
            }
        }
        info = String.valueOf(info) + " >> " + this.userTrade.get(0).charname + " nhận_";
        for (int i = 0; i < this.userTrade.get(0).rPotions.length; ++i) {
            if (this.userTrade.get(0).rPotions[i] > 0) {
                info = String.valueOf(info) + this.userTrade.get(0).rPotions[i] + "_" + LoginHandler.PORTION_NAME[i] + " n ";
                final int[] potions4 = this.userTrade.get(0).potions;
                final int n4 = i;
                potions4[n4] += this.userTrade.get(0).rPotions[i];
            }
        }
        this.rPotions = null;
        this.rPotions = new int[162];
        this.userTrade.get(0).rPotions = null;
        this.userTrade.get(0).rPotions = new int[162];
        try {
            // 14/09/2026: ghi kem vi tri chi tiet (map/khu/toa do) de biet giao dich xay ra
            // o dau (vd "Thanh Tran Danh" idMapTown=201), doc duoc ngay tu tob_log_all_item.
            Database.instance.saveLogTradeItem(this.charname, this.userTrade.elementAt(0).charname,
                    info + " | " + Char.moTaViTri(this));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /** @return false neu buoc chuyen thuoc bi tu choi (xem doAddPotionTrad2Inventory) - nguoi goi phai huy giao dich. */
    public boolean doAddItemTrad2Inventory() {
        String info = String.valueOf(this.charname) + " chuyển_";
        for (int i = 0; i < this.tItems.size(); ++i) {
            final Item item = this.tItems.elementAt(i);
            this.iItems.remove(item);
            this.ALL_ID_USE.remove(item.id);
            // 16/09/2026: them "dbid=" (id dong DB rieng cua item, on dinh qua giao dich vi chi
            // doi owner/dbowner chu khong tao dong moi) theo yeu cau "log chi tiet hon" - truoc
            // day idTemplate nam chim khong nhan trong getDBInfo(), khong the tra dung MOT item
            // cu the neu 2 nguoi cung trade nhieu mon giong het nhau.
            info = String.valueOf(info) + "dbid" + item.dbid + "_" + item.getTemplate().name + "_" + item.plus + "_" + item.getAttribute() + "_" + item.getDBInfo() + " | ";
        }
        info = String.valueOf(info) + " >> " + this.userTrade.get(0).charname + " chuyển_";
        for (int i = 0; i < this.userTrade.get(0).tItems.size(); ++i) {
            final Item item = this.userTrade.get(0).tItems.elementAt(i);
            this.userTrade.get(0).iItems.remove(item);
            this.userTrade.get(0).ALL_ID_USE.remove(item.id);
            info = String.valueOf(info) + "dbid" + item.dbid + "_" + item.getTemplate().name + "_" + item.plus + item.getAttribute() + "_" + item.getDBInfo() + " | ";
        }
        this.tItems.removeAllElements();
        this.userTrade.get(0).tItems.removeAllElements();
        for (int i = 0; i < this.rItems.size(); ++i) {
            final Item item = this.rItems.elementAt(i);
            item.owner = this.charDBID;
            if (item.dbowner != 0) {
                item.dbowner = this.charDBID;
            }
            item.id = this.getIDItem();
            item.place = 0;
            this.iItems.add(item);
        }
        for (int i = 0; i < this.userTrade.get(0).rItems.size(); ++i) {
            final Item item = this.userTrade.get(0).rItems.elementAt(i);
            item.owner = this.userTrade.get(0).charDBID;
            if (item.dbowner != 0) {
                item.dbowner = this.userTrade.get(0).charDBID;
            }
            item.id = this.userTrade.get(0).getIDItem();
            item.place = 0;
            this.userTrade.get(0).iItems.add(item);
        }
        try {
            // 14/09/2026: ghi kem vi tri chi tiet (map/khu/toa do) de biet giao dich xay ra
            // o dau (vd "Thanh Tran Danh" idMapTown=201), doc duoc ngay tu tob_log_all_item.
            Database.instance.saveLogTradeItem(this.charname, this.userTrade.elementAt(0).charname,
                    info + " | " + Char.moTaViTri(this));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.rItems.removeAllElements();
        this.userTrade.get(0).rItems.removeAllElements();
        this.userTrade.get(0).finishTrade = false;
        this.userTrade.get(0).lockTrade = false;
        this.finishTrade = false;
        this.lockTrade = false;
        return this.doAddPotionTrad2Inventory();
    }

    public boolean checkExistItem(final int idTemplate) {
        try {
            for (int i = 0; i < this.iItems.size(); ++i) {
                final Item it = this.iItems.get(i);
                if (it.getTemplate().id == idTemplate) {
                    return true;
                }
            }
        } catch (final Exception ex) {
        }
        return false;
    }

    public Item getLenhBaiTranYeu() {
        if (this.lvDetail.lv < 50) {
            return null;
        }
        int idtemplate = 678;
        if (this.lvDetail.lv >= 60 && this.lvDetail.lv < 70) {
            idtemplate = 679;
        }
        // >= 70 cho khop bang chia map o MapDoMenu (lv >= 70 -> map 210); ban cu dung "> 70"
        // nen dung cap 70 dung lenh bai cap 50 (678) ma vao duoc tran cap cao.
        if (this.lvDetail.lv >= 70) {
            idtemplate = 680;
        }
        try {
            for (int i = 0; i < this.iItems.size(); ++i) {
                final Item it = this.iItems.get(i);
                if (it.getTemplate().id == idtemplate) {
                    return it;
                }
            }
        } catch (final Exception ex) {
        }
        return null;
    }

    public int isExistInvector(final Vector<Item> v, final short itemID) {
        for (int i = 0; i < v.size(); ++i) {
            final Item c = v.elementAt(i);
            if (c.id == itemID) {
                return i;
            }
        }
        return -1;
    }

    public int isExistInvector(final short itemID, final Vector<GemItem> v) {
        for (int i = 0; i < v.size(); ++i) {
            final GemItem c = v.elementAt(i);
            if (c.realId == itemID) {
                return i;
            }
        }
        return -1;
    }

    public void removeGemItem(final short id) {
        for (int i = 0; i < this.gemItem.size(); ++i) {
            if (this.gemItem.get(i).realId == id) {
                this.gemItem.remove(i);
                return;
            }
        }
    }

    public Vector<ItemSell> getAllItemSearch() {
        if (!this.searchChar.equals("")) {
            return this.getAllItemSearchBySell(this.searchChar);
        }
        return this.getListItemSearch(this.lvSearch, this.typeItemSearch, this.colortItemSearch, this.phamSearch, this.plusSearch);
    }

    public Vector<ItemSell> getListItemSearch(final short level, final short typeItem, final short colorItem, final short pham, final short cong) {
        this.allSearchItem.removeAllElements();
        try {
            this.lvSearch = level;
            this.typeItemSearch = (byte) typeItem;
            this.colortItemSearch = (byte) colorItem;
            this.phamSearch = (byte) pham;
            this.plusSearch = (byte) cong;
            final Vector<ItemSell> itemSell = Market.getListItemSearch(level, typeItem, colorItem, pham, cong, this.inCountry);
            if (itemSell.size() > 0) {
                final Vector<ItemSell> removeItem = new Vector<ItemSell>();
                for (int i = 0; i < itemSell.size(); ++i) {
                    final ItemSell item = itemSell.get(i);
                    if (item.isExpire()) {
                        removeItem.add(item);
                    }
                }
                while (removeItem.size() > 0) {
                    final ItemSell item2 = removeItem.remove(0);
                    if (ItemMarket.HASH_ITEM_SELL.get(item2.id) != null) {
                        Market.isItemExpired(item2);
                    } else {
                        itemSell.remove(item2);
                    }
                }
            }
            this.allSearchItem = itemSell;
        } catch (final Exception ex) {
        }
        return this.allSearchItem;
    }

    public Vector<ItemSell> getAllItemSearchBySell(final String charsell) {
        this.searchChar = charsell.trim().toLowerCase();
        System.out.println("getAllItemSearchBySell Char: " + this.searchChar);
        this.allSearchItem.removeAllElements();
        final Vector<ItemSell> itemSell = Market.getListItemSearchByName(charsell.toLowerCase().trim());
        if (itemSell.size() > 0 && !itemSell.get(0).canbuy(this.inCountry)) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể mua vật phẩm của " + charsell + " khi bạn ở trên lãnh thổ của " + charsell, ""));
            return new Vector<ItemSell>();
        }
        return this.allSearchItem = itemSell;
    }

    public Vector<ItemSell> getAllListItemBid() {
        this.allItemBid.removeAllElements();
        synchronized (ItemMarket.ALL_CHAR_SELL) {
            final CharSellItem c = Market.getCharSell(this.charname);
            this.allItemBid = c.getListItemBid();
            //monitorexit(ItemMarket.ALL_CHAR_SELL);
        }
        return this.allItemBid;
    }

    public Vector<ItemSell> getAllistNewItemSell() {
        this.allNewItem.removeAllElements();
        final Vector<ItemSell> it = new Vector<ItemSell>();
        int size = 50;
        Vector<ItemSell> itemsell = ItemMarket.ALL_ITEM_SELL_THANH_LONG;
        if (this.inCountry == 1) {
            itemsell = ItemMarket.ALL_ITEM_SELL_HAC_HO;
        }
        final Vector<ItemSell> vector;
        synchronized (vector = ((this.inCountry == 1) ? ItemMarket.ALL_ITEM_SELL_HAC_HO : ItemMarket.ALL_ITEM_SELL_THANH_LONG)) {
            if (size > itemsell.size()) {
                size = itemsell.size();
            }
            int i = 0;
            int j = 0;
            final Vector<ItemSell> itExpire = new Vector<ItemSell>();
            while (i < size) {
                final ItemSell item = itemsell.get(j);
                if (!item.isExpire()) {
                    it.add(itemsell.get(i));
                    ++i;
                } else {
                    itExpire.add(item);
                }
                if (++j >= itemsell.size()) {
                    break;
                }
            }
            for (i = 0, i = 0; i < itExpire.size(); ++i) {
                Market.isItemExpired(itExpire.get(i));
            }
            this.allNewItem = it;
            //monitorexit(vector);
        }
        return it;
    }

    public Vector<ItemSell> getAllistItemSell() {
        synchronized (ItemMarket.ALL_CHAR_SELL) {
            final CharSellItem c = ItemMarket.ALL_CHAR_SELL.get(this.charname);
            if (c != null) {
                final Vector<ItemSell> allItems = c.getAllItems();
                //monitorexit(ItemMarket.ALL_CHAR_SELL);
                return allItems;
            }
            final Vector<ItemSell> vector = new Vector<ItemSell>();
            //monitorexit(ItemMarket.ALL_CHAR_SELL);
            return vector;
        }
    }

    public Vector<ItemSell> getAllistItemExpire() {
        synchronized (ItemMarket.ALL_CHAR_SELL) {
            final CharSellItem c = ItemMarket.ALL_CHAR_SELL.get(this.charname);
            if (c != null) {
                final Vector<ItemSell> allItemExpire = c.getAllItemExpire();
                //monitorexit(ItemMarket.ALL_CHAR_SELL);
                return allItemExpire;
            }
            final Vector<ItemSell> vector = new Vector<ItemSell>();
            //monitorexit(ItemMarket.ALL_CHAR_SELL);
            return vector;
        }
    }

    public GemItem getItemFormVector(final short id, final Vector<GemItem> v) {
        for (int i = 0; i < v.size(); ++i) {
            final GemItem item = v.elementAt(i);
            if (item.realId == id) {
                return item;
            }
        }
        return null;
    }

    public int countLKD() {
        int n = 0;
        for (int i = 0; i < this.gemItemImbue.size(); ++i) {
            final GemItem item = this.gemItemImbue.elementAt(i);
            if ((item.idGemTemplate > 7 && item.idGemTemplate < 12) || item.idGemTemplate == 158 || item.idGemTemplate == 157 || item.idGemTemplate == 66 || item.idGemTemplate == 245) {
                ++n;
            }
        }
        return n;
    }

    public int countBaoHiem() {
        int n = 0;
        for (int i = 0; i < this.gemItemImbue.size(); ++i) {
            final GemItem item = this.gemItemImbue.elementAt(i);
            if (item.idGemTemplate < 5) {
                ++n;
            }
        }
        return n;
    }

    public int getTotalGem() {
        int total = 0;
        for (int i = 0; i < this.listGemitem.length; ++i) {
            total += this.listGemitem[i];
        }
        return total;
    }

    public String getInfoGemLock() {
        String info = "";
        for (int i = 0; i < this.listGemitemLock.length; ++i) {
            if (this.listGemitemLock[i] > 0) {
                info = String.valueOf(info) + this.listGemitemLock[i] + " " + Map.gemTemplate[i].name + " ";
            }
        }
        return info;
    }

    public int getAllGem() {
        int total = 0;
        for (int i = 0; i < this.listGemitem.length; ++i) {
            if (this.listGemitem[i] > 0 && this.listGemitem[i] - this.listGemitemSell[i] > 0) {
                ++total;
            }
        }
        return total;
    }

    public int getAllGemLock() {
        int total = 0;
        for (int i = 0; i < this.listGemitemLock.length; ++i) {
            if (this.listGemitemLock[i] > 0) {
                ++total;
            }
        }
        return total;
    }

    public int getAllSP() {
        int total = 0;
        for (int i = 0; i < this.listSpItem.length; ++i) {
            if (this.listSpItem[i] > 0) {
                total += this.listSpItem[i];
            }
        }
        return total;
    }

    public int getAllKindSP() {
        int total = 0;
        for (int i = 0; i < this.listSpItem.length; ++i) {
            if (this.listSpItem[i] > 0) {
                ++total;
            }
        }
        return total;
    }

    public void removeItemOutVector(final short id, final Vector<GemItem> v) {
        for (int i = 0; i < v.size(); ++i) {
            final GemItem item = v.elementAt(i);
            if (item.realId == id) {
                v.removeElementAt(i);
                this.ALL_ID_USE.remove(item.realId);
                return;
            }
        }
    }

    public Item getItemFormVector(final Vector<Item> v, final short id) {
        for (int i = 0; i < v.size(); ++i) {
            final Item item = v.elementAt(i);
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }

    public Item getItemInventotyByIDTemplate(final int idtemplate, final int lock) {
        for (int i = 0; i < this.iItems.size(); ++i) {
            final Item item = this.iItems.elementAt(i);
            if (item.tempateID == idtemplate && (lock == -1 || item.lock == lock)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public short getLevel() {
        return (short) this.lvDetail.lv;
    }

    public void doSendProperties() {
        try {
            final Message m = new Message(32);
            m.dos.writeShort(this.id);
            m.dos.writeByte(2);
            m.dos.writeUTF("hp");
            m.dos.writeInt(this.hp);
            m.dos.writeUTF("mp");
            m.dos.writeInt(this.mp);
            m.dos.writeByte(this.lvDetail.lv);
            m.dos.writeShort(this.lvDetail.percent);
            this.sendMessage(m);
            m.cleanup();
        } catch (final Exception ex) {
        }
    }

    private void getInfoDefendItem() {
    }

    public String getinfoSkillClan(final int id, final int type) {
        final String st = "";
        if (type == 0) {
            if (this.skillClan[id] > 0) {
                return String.valueOf(Skill_Clan.SKILL_DECRIPTION_CLAN[id]) + Skill_Clan.SKILL_CLAN[id][this.skillLvClan[id]];
            }
        } else if (type == 1 && this.skillClan[id] > 0) {
            return String.valueOf(Skill_Clan.SKILL_DECRIPTION_CLAN_MEMBER[id - 10]) + Skill_Clan.SKILL_CLAN_MEMBER[id - 10][this.skillLvClan[id]];
        }
        return st;
    }

    public int getEffSkillClan(final int idSkill) {
        if (this.skillClan[idSkill] <= 0) {
            return 0;
        }
        return Skill_Clan.getEffectSkill((byte) idSkill, this.skillLvClan[idSkill], (byte) 0);
    }

    public int getEffSkillClanMember(final int idSkill) {
        if (this.skillClan[idSkill + 10] <= 0) {
            return 0;
        }
        return Skill_Clan.getEffectSkill((byte) idSkill, this.skillLvClan[idSkill + 10], (byte) 1);
    }

    private void checkWearing() {
    }

    public void calculateAttrib() {
        this.checkWearing();
        this.resetNguHanh();
        this.checkKickAtt5();
        this.checkKick5AttAnimal();
        // SKIN LINH THU (Xich Duong): xuyen giap. PHAI dat sau resetNguHanh() —
        // ham do cap phat lai attNguHanh moi lan chay, con calculateAttrib() thi chay
        // lai moi lan mac/thao do. Cong truoc khi reset la mat, va neu ban than
        // attNguHanh khong duoc reset thi "+=" o day se cong don khong gioi han roi
        // tran kieu byte thanh so am (dung loi da xay ra voi this.speed, xem chu thich
        // ben duoi). Cong sau reset + co reset that = an toan.
        final int xuyenGiapSkin = SkinLinhThu.pcXuyenGiap(this);
        if (xuyenGiapSkin > 0) {
            this.attNguHanh[3] = (byte) Math.min(127, this.attNguHanh[3] + xuyenGiapSkin);
        }
        this.coolDown = new short[CharManager.SKILL_COOLDOWN[this.charClass].length][11];
        // Than khi: "Tang toc do danh" = giam thoi gian hoi chieu. Ap ngay khi
        // dung bang coolDown nen moi noi kiem tra hoi chieu deu huong theo.
        int tkGiamCd = getTkGiamHoiChieu();
        if (tkGiamCd > 50) {
            tkGiamCd = 50; // chan tran, khong cho spam ky nang khong nghi
        }
        for (int i = 0; i < this.coolDown.length; ++i) {
            for (int j = 0; j < this.coolDown[i].length; ++j) {
                int cd = CharManager.SKILL_COOLDOWN[this.charClass][i][j] * 100;
                if (tkGiamCd > 0) {
                    cd -= cd * tkGiamCd / 100;
                }
                this.coolDown[i][j] = (short) cd;
            }
        }
        if (this.xichtho_thienlyma == 2) {
            this.upAbility[0] = 3;
            this.upAbility[1] = 3;
            this.upAbility[2] = 3;
            this.upAbility[3] = 3;
            this.upAbility[4] = 3;
            this.speed = 6;
        } else if (this.xichtho_thienlyma == 1) {
            this.upAbility[0] = 0;
            this.upAbility[1] = 0;
            this.upAbility[2] = 0;
            this.upAbility[3] = 0;
            this.upAbility[4] = 0;
        }
        if (this.rideHorse > 0) {
            this.speed = 6;
        }
        final Vector<Item> wItems = this.getCharWearing();
        final byte[] att = new byte[18];
        for (int k = 0; k < wItems.size(); ++k) {
            final Item item = wItems.get(k);
            for (int l = 0; l < 18; ++l) {
                final byte[] array = att;
                final int n = l;
                array[n] += (byte) (item.atb[l + 10] + item.getValueUpByVuKhiThoiTrang(item.atb[l + 10], this.itemVukhiThoiTrang));
            }
        }
        this.abilityKham = att;
        // Toc do than khi KHONG cong vao this.speed: calculateAttrib() chay lai
        // nhieu lan moi phien va this.speed khong duoc dat lai o dau ham, nen
        // "+= 1" se cong don khong gioi han roi tran kieu byte thanh so am.
        // Cong bonus ngay luc ghi goi tin thay vi doi trang thai. Xem
        // getThanKhiSpeedBonus().
        this.updateSkillBuffPassive();
        this.calculateMaxHPMP();
        this.calculateAttack();
        this.calculateDefend();
        this.calculateAccurate();
        this.calculateDodge();
        this.calculateCritical();
    }

    public void updateSkillBuffPassive() {
        switch (this.charClass) {
            case 0: {
                this.buffPass[0] = CharManager.SKILL_DAM_PERCENT[this.charClass][4][this.skill[4] + this.addMoreLevelSkill[4]];
                break;
            }
            case 1: {
                this.buffPass[0] = CharManager.SKILL_DAM_PERCENT[this.charClass][5][this.skill[5] + this.addMoreLevelSkill[5]];
                break;
            }
            case 2: {
                this.buffPass[0] = CharManager.SKILL_DAM_PERCENT[this.charClass][5][this.skill[5] + this.addMoreLevelSkill[5]];
                break;
            }
            case 3: {
                this.buffPass[0] = CharManager.SKILL_DAM_PERCENT[this.charClass][5][this.skill[5] + this.addMoreLevelSkill[5]];
                break;
            }
            case 4: {
                this.buffPass[0] = CharManager.SKILL_DAM_PERCENT[this.charClass][5][this.skill[5] + this.addMoreLevelSkill[5]];
                break;
            }
        }
    }

    @Override
    public int getBuffDefCB(final int type, final boolean isMons) {
        if (this.percentBuff[0] <= 0) {
            return 0;
        }
        if (type == 0) {
            return this.percentBuff[0];
        }
        return CharManager.SKILL_DAM_PERCENT[1][4][this.skill[4] + this.addMoreLevelSkill[4]] * this.defend_magic / 100;
    }

    public void upPercentAtribute(Char c, int bidEff, int id, int lv, Char cBuff) {
        if (bidEff == 19) {
            return;
        }
        Message m = null;
        int percen = 0;
        try {
            switch (bidEff) {
                case 20:
                    if (cBuff.charClass == 1 && c.id == cBuff.id) {
                        c.percentBuff[0] = 0;
                        c.percentBuffPlayer[0] = 0;
                        percen = CharManager.SKILL_DAM_PERCENT[1][4][this.skill[4] + this.addMoreLevelSkill[4]];
                        c.percentBuff[0] = percen * this.defend_physic / 100;
                        c.percentBuffPlayer[0] = percen * this.defendVatPlayer / 100;
                        if (percen * 100 % 100 >= 5) {
                            c.percentBuff[0] = c.percentBuff[0] + 1;
                            c.percentBuffPlayer[0] = c.percentBuffPlayer[0] + 1;
                        }
                        c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                        c.sendToNearPlayer(MessageCreator.createNew_HMP_Message(c, 0));
                    }
                    break;
                case 25:
                    if (cBuff.charClass == 2 && c.id == cBuff.id) {
                        c.percentBuff[2] = 0;
                        c.percentBuff[1] = 0;
                        // id/lv la vi tri trong mang tam tempBuff cua CharCapNhat.setBuff2Char,
                        // KHONG PHAI id ky nang that (giong case 20/27 canh day, tra bang bang
                        // chi so ky nang that = buff+4 = 4 cho buffEff=25) va addMoreLevelSkill[]
                        // duoc lap chi so theo ID KY NANG, khong phai theo cap - dung dung index
                        // 4 cho ca hai, giong het cach case 20/27 tu tinh lai thay vi dung id/lv.
                        percen = CharManager.SKILL_DAM_PERCENT[2][4][this.skill[4] + this.addMoreLevelSkill[4]];
                        c.percentBuff[2] = percen * this.maxhp / 100;
                        c.percentBuff[1] = percen * this.maxmp / 100;
                        c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                        c.sendToNearPlayer(MessageCreator.createNew_HMP_Message(c, 0));
                    }
                    break;
                case 27:
                    if (System.currentTimeMillis() - cBuff.timeLastUseSkills[6] < cBuff.coolDown[6][cBuff.skill[6]]) {
                        return;
                    }
                    if (cBuff.charClass != 2) {
                        if (cBuff.xu < 1000000L) {
                            return;
                        }
                        Database.instance.saveOrtherLog("tob_other_log", cBuff.charname, "hack hs " + cBuff.xu + "_1000000", "hhs");
                        cBuff.xu -= 1000000L;
                        if (cBuff.xu < 0L) {
                            cBuff.xu = 0L;
                        }
                        cBuff.sendMessage(MessageCreator.createCharInventoryMessage(cBuff, 0));
                    }
                    cBuff.timeLastUseSkills[6] = System.currentTimeMillis();
                    c.percentBuff[2] = 0;
                    percen = CharManager.SKILL_DAM_PERCENT[2][6][cBuff.skill[6] + cBuff.addMoreLevelSkill[6]];
                    c.hp = (int) Math.min((long) c.maxhp, (long) c.maxhp * percen / 100L);
                    c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                    c.sendToNearPlayer(MessageCreator.createNew_HMP_Message(c, 0));
                    if (c.petUsing != null && !c.petUsing.isPetTool() && c.petUsing.getIdEffPet() > -1
                            && c.addEffBuff(c.petUsing.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                        c.sendEffToChar(c);
                    }
                    sendMessage(m);
                    break;
            }
        } catch (Exception exception) {
        }
    }

    private void calculateCritical() {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        Vector<Item> wItems = getCharWearing();
        int crit1 = 0;
        int crit2 = 0;
        if (this.charClass == 4) {
            crit1 = ((this.luck + this.tnMayMan()) + getEffSkillClan(3)) / 5;
        }
        for (Item item : wItems) {
            if (item.level > this.lvDetail.lv) {
                continue;
            }
            if ((item.getTemplate()).type == 9 || (item.getTemplate()).type == 11 || (item.getTemplate()).type == 12 || ((item.getTemplate()).type > 2 && (item.getTemplate()).type < 8)) {
                crit2 += item.atb[4] + item.getValueUpByVuKhiThoiTrang(item.atb[4], this.itemVukhiThoiTrang);
            }
        }
        this.critical = (short) (crit1 + this.newAtb[7] + crit2 + ((animalRide != null) ? (animalRide.att[11] + animalRide.attUp[11]) : 0));
        long pc = 0L;
        if (this.timeUp5Attribute > 0L) {
            pc += 5L;
            this.critical = (short) (int) (this.critical + this.critical * pc / 100L);
        }
        int atkTubinh = 0;
        for (int i = 0; i < this.tubinh.size(); i++) {
            atkTubinh += ((TuBinhTemplate) this.tubinh.get(i)).getCrit();
        }
        this.critical = (short) (this.critical + atkTubinh);
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null) {
                long noiluc = c.getNoiLuc();
                this.critical = (short) (int) (this.critical * noiluc / 10L);
            }
        }
        if(isSaTang()){
            int level = this.getLevelCaiTrang();

            if(level >= 3 && level < 8){
                this.critical = (short) (int) (this.critical * 1.01); // +1%
            } else if(level >= 8 && level < 14){
                this.critical = (short) (int) (this.critical * 1.02); // +2%
            } else if(level == 14){
                this.critical = (short) (int) (this.critical * 1.04); // +4%
            } else if(level == 15){
                this.critical = (short) (int) (this.critical * 1.05); // +5%
            }

        }
    }

    public int getXLuaLan(final int cat) {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int x = 1;
        if (petUsing != null) {
            x += petUsing.getXLuaLan();
        }
        if (this.wModel != null) {
            x += this.wModel.getLuaLan();
        }
        if (animalRide != null) {
            if (animalRide.isLan()) {
                if (cat == 1) {
                    x += animalRide.getLuaLan();
                }
            } else {
                x += animalRide.getLuaLan();
            }
        }
        if (this.itemVukhiThoiTrang != null) {
            x += this.itemVukhiThoiTrang.getLuaLan();
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                x = (int) (x * noiluc / 10L);
            }
        }
        return x;
    }

    public int getXBangLan(final int cat) {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int x = 1;
        if (petUsing != null) {
            x += petUsing.getXBangLan();
        }
        if (this.wModel != null) {
            x += (int) this.wModel.getBangLan();
        }
        if (animalRide != null) {
            if (animalRide.isLan()) {
                if (cat == 1) {
                    x += animalRide.getBangLan();
                }
            } else {
                x += animalRide.getBangLan();
            }
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                x = (int) (x * noiluc / 10L);
            }
        }
        return x;
    }

    public int getPCXbangSetDocLuaLan() {
        if (this.wModel != null) {
            return this.wModel.getPCXbangSetDocLuaLan();
        }
        return 0;
    }

    public int getXSetLan(final int cat) {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int x = 1;
        if (petUsing != null) {
            x += petUsing.getXSetLan();
        }
        if (this.wModel != null) {
            x += this.wModel.getSetLan();
        }
        if (animalRide != null) {
            if (animalRide.isLan()) {
                if (cat == 1) {
                    x += animalRide.getSetLan();
                }
            } else {
                x += animalRide.getSetLan();
            }
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                x = (int) (x * noiluc / 10L);
            }
        }
        return x;
    }

    public int getXDocLan(final int cat) {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int x = 1;
        if (petUsing != null) {
            x += petUsing.getXDocLan();
        }
        if (this.wModel != null) {
            x += this.wModel.getDocLan();
        }
        if (animalRide != null) {
            if (animalRide.isLan()) {
                if (cat == 1) {
                    x += animalRide.getDocLan();
                }
            } else {
                x += animalRide.getDocLan();
            }
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                x = (int) (x * noiluc / 10L);
            }
        }
        return x;
    }

    // Than method (134 dong) da chuyen sang CharChiSo.java. Chu ky GIU NGUYEN.
    private void calculateMaxHPMP() {
        CharChiSo.calculateMaxHPMP(this);
    }

    private void calculateDodge() {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int dogde1 = (this.agitity + this.tnKheoLeo()) + this.getEffSkillClan(3) + this.upAbility[3] + this.abilityKham[1] + ((animalRide != null) ? animalRide.att[7] : 0);
        dogde1 += ((petUsing != null) ? petUsing.getagitity() : 0);
        int dogde2 = 0;
        for (int i = 0; i < this.wItems[this.slotWearing].length; ++i) {
            final Item item = this.wItems[this.slotWearing][i];
            if (item != null) {
                if (item.level <= this.lvDetail.lv) {
                    dogde2 += item.atb[2];
                }
            }
        }
        this.dodge = (short) ((dogde1 + dogde2 + this.basic) / 7 * 2);
        int atkTubinh = 0;
        for (int j = 0; j < this.tubinh.size(); ++j) {
            atkTubinh += this.tubinh.get(j).getDog();
        }
        this.dodge += (short) atkTubinh;
        if (this.dodge >= 300) {
            this.dodge = 299;
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                this.dodge = (short) (this.dodge * noiluc / 10L);
            }
        }
    }

    /**
     * Tran cung cua diem chinh xac: ham tinh chia 7, chan o 99, roi nhan 700/100.
     * 99 * 700 / 100 = 693. Moi nguon chinh xac cua game deu nam duoi con so nay.
     */
    public static final int TRAN_CHINH_XAC = 99 * 700 / 100;

    private void calculateAccurate() {
        Pet petUsing = this.petUsing;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        int accurate1 = (this.agitity + this.tnKheoLeo()) + this.getEffSkillClan(3) + this.upAbility[3] + this.abilityKham[1] + ((animalRide != null) ? animalRide.att[7] : 0);
        accurate1 += ((petUsing != null) ? petUsing.getagitity() : 0);
        int accurate2 = 0;
        for (int i = 0; i < this.wItems[this.slotWearing].length; ++i) {
            final Item item = this.wItems[this.slotWearing][i];
            if (item != null) {
                if (item.level <= this.lvDetail.lv) {
                    accurate2 += item.atb[3] + item.getValueUpByVuKhiThoiTrang(item.atb[3], this.itemVukhiThoiTrang);
                }
            }
        }
        this.accurate = (short) ((accurate1 + accurate2 + this.basic) / 7);
        if (this.accurate >= 100) {
            this.accurate = 99;
        }
        this.accurate = (short) (this.accurate * 700 / 100);
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c2 = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c2 != null) {
                final long noiluc = c2.getNoiLuc();
                this.accurate = (short) (this.accurate * noiluc / 10L);
            }
        }
        // SKIN LINH THU (Gau Truc): cong THANG vao diem chinh xac cua nhan vat.
        //
        // Dat o CUOI HAM, sau chia 7 / chan tran 99 / nhan 700 — nen 200 la 200 that.
        // Cong vao accurate2 o tren thi no di qua phep chia 7 va cai tran 99, con khoang
        // 28 diem, va thanh 0 HAN voi nhan vat da cham tran.
        //
        // Vat pham skin nam trong bang TRANG BI THU, ma vong lap accurate2 o tren chi
        // duyet wItems (trang bi nhan vat) — no khong bao gio thay skin.
        //
        // NHUNG VAN PHAI TON TRONG TRAN CUA GAME. Chinh xac cua game co tran cung:
        // chia 7 roi chan o 99 roi nhan 700/100 => toi da TRAN_CHINH_XAC = 693. Moi
        // nguon chinh xac khac deu nam duoi tran do. De skin vuot tran thi no la chi so
        // duy nhat trong ca tinh nang di ra ngoai thang do cua game — do chinh la cho
        // "cong sai" nhin thay duoc.
        //
        // Chan bang max(tran, gia tri dang co) chu khong chan cung 693: ban do chien
        // truong nhan chinh xac voi noi luc (co the vuot 693 mot cach hop le), va dong
        // nay khong duoc phep CAT BOT cai nguoi choi da co.
        final int chinhXacSkin = SkinLinhThu.congChinhXac(this);
        if (chinhXacSkin > 0) {
            final int tran = Math.max(TRAN_CHINH_XAC, this.accurate);
            this.accurate = (short) Math.min(tran, this.accurate + chinhXacSkin);
        }
    }

    public byte getRing() {
        return this.idRing = (byte) ((this.idRing == 0) ? 1 : 0);
    }

    // Than method (377 dong) da chuyen sang CharChiSo.java. Chu ky GIU NGUYEN.
    private void calculateDefend() {
        CharChiSo.calculateDefend(this);
    }

    // Than method (29 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void buffAttackSkill(final Monster mt, final int d) {
        CharCapNhat.buffAttackSkill(this, mt,d);
    }

    public int khamMu() {
        return this.abilityKham[4];
    }

    public int khamBang() {
        return this.abilityKham[5];
    }

    public int khamDoc() {
        return this.abilityKham[6];
    }

    public int khamChoang() {
        return this.abilityKham[7];
    }

    public int khamHoathach() {
        return this.abilityKham[8];
    }

    public int khamGiamtoc() {
        return this.abilityKham[9];
    }

    @Override
    public int khamKhangGiamtoc() {
        return this.abilityKham[10];
    }

    @Override
    public int khamKhangDoc() {
        return this.abilityKham[11];
    }

    @Override
    public int khamKhangMu() {
        return this.abilityKham[12];
    }

    @Override
    public int khamKhangBang() {
        // Lay con so thuc cua thu (hien tai phuong hoang lua va rong bang deu la 95)
        // thay vi ap cung 95, de bang thong tin va combat luon khop nhau.
        if (this.animalRide != null && this.animalRide.KHANG_DONG_BANG > 0) {
            return this.animalRide.KHANG_DONG_BANG;
        }
        return this.abilityKham[13];
    }

    @Override
    public int khamKhangChoang() {
        return this.abilityKham[14];
    }

    @Override
    public int khamKhangHoathach() {
        return this.abilityKham[15];
    }

    @Override
    public int khamX2() {
        return this.abilityKham[16];
    }

    @Override
    public int khamDropItem() {
        return this.abilityKham[17];
    }

    public void buffSkillKham(final LiveActor actor) {
        try {
            // Hoa giai ky nang pet xuyen qua Tien khi (ky nang pet), nhung KHONG xuyen
            // qua Cam chiu 8s cua Rong Lon vi do la noi tai thu cuoi.
            if ((actor.isActiveTienKhi() && !this.dangHoaGiaiKyNangPet()) || actor.isActive8sRongLon()) {
                return;
            }
            int[] khang = new int[6];
            byte nEff = 0;
            khang = new int[]{actor.khamKhangMu(), actor.khamKhangBang(), actor.khamKhangDoc(), actor.khamKhangChoang(), actor.khamKhangHoathach(), actor.khamKhangGiamtoc()};
            final int pckhang = 1000;
            if (this.khamMu() > 0 && Map.r.nextInt(pckhang) <= this.khamMu() && Map.r.nextInt(pckhang) > khang[0] && actor.khamEff[0] == 0L) {
                actor.khamEff[0] = System.currentTimeMillis() / 1000L;
                ++nEff;
            }
            if (this.khamBang() > 0 && Map.r.nextInt(pckhang) <= this.khamBang() && Map.r.nextInt(pckhang) > khang[1] && actor.khamEff[3] == 0L) {
                actor.khamEff[3] = System.currentTimeMillis() / 1000L;
                ++nEff;
            }
            if (this.khamDoc() > 0 && Map.r.nextInt(pckhang) <= this.khamDoc() && Map.r.nextInt(pckhang) > khang[2] && actor.khamEff[1] == 0L) {
                actor.khamEff[1] = System.currentTimeMillis() / 1000L;
                actor.timeOutKhamDoc = System.currentTimeMillis();
                ++nEff;
            }
            if (this.khamChoang() > 0 && Map.r.nextInt(pckhang) <= this.khamChoang() && Map.r.nextInt(pckhang) > khang[3] && actor.khamEff[2] == 0L) {
                actor.khamEff[2] = System.currentTimeMillis() / 1000L;
                ++nEff;
            }
            if (this.khamHoathach() > 0 && Map.r.nextInt(pckhang) <= this.khamHoathach() && Map.r.nextInt(pckhang) > khang[4] && actor.khamEff[4] == 0L) {
                actor.khamEff[4] = System.currentTimeMillis() / 1000L;
                ++nEff;
            }
            if (this.khamGiamtoc() > 0 && Map.r.nextInt(pckhang) <= this.khamGiamtoc() && Map.r.nextInt(pckhang) > khang[5] && actor.khamEff[5] == 0L) {
                actor.khamEff[5] = System.currentTimeMillis() / 1000L;
                ++nEff;
            }
            final Message m = this.sendEffKham(actor);
            if (m != null) {
                this.sendMessage(m);
                this.sendToNearPlayer(m);
            }
        } catch (final Exception ex) {
        }
    }

    public void setBangLan(final Vector<LiveActor> actor) {
    }

    public void setDocLan(final Vector<LiveActor> actor) {
    }

    public Message sendEffKham(final LiveActor actor) {
        final Message msg = new Message(-42);
        boolean haveEff = false;
        try {
            msg.dos.writeShort(actor.id);
            for (int i = 0; i < actor.khamEff.length; ++i) {
                if (actor.khamEff[i] > 0L) {
                    msg.dos.writeByte(i);
                    msg.dos.writeByte((byte) (3L - (System.currentTimeMillis() / 1000L - actor.khamEff[i])));
                    if (!haveEff) {
                        haveEff = true;
                    }
                }
            }
        } catch (final Exception ex) {
        }
        if (haveEff) {
            return msg;
        }
        return null;
    }

    public void buffAttackSkill(final int d, final LiveActor actor) {
        try {
            if ((actor.isActiveTienKhi() && !this.dangHoaGiaiKyNangPet()) || actor.isActive8sRongLon()) {
                return;
            }
            if (this.charClass == 4) {
                try {
                    if (actor.cat == 0 && ((Char) actor).charClass == 4) {
                        return;
                    }
                } catch (final Exception ex) {
                }
                if (this.arrayBuff[2] != -1) {
                    int random = 0;
                    if ((random = Map.r.nextInt(100)) <= 100) {
                        int khangdoc = actor.khamKhangDoc();
                        if (actor.cat == 0) {
                            final int hour = getCurrentHour();
                            if (hour > 6 && khangdoc > 50) {
                                khangdoc = 50;
                            }
                            if (actor.getLevel() - this.getLevel() > 5 && this.getLevel() < 60) {
                                khangdoc = 0;
                            }
                        }
                        if (actor.tDelay != 0 || random < khangdoc) {
                            return;
                        }
                        final int[] timeCash = {5, 5, 5, 5, 5, 4, 4, 3, 2, 1, 1};
                        int damage = CharManager.SKILL_DAM_PERCENT[4][4][this.skill[4] + this.addMoreLevelSkill[4]];
                        final int deltaLV = this.lvDetail.lv - actor.getLevel();
                        if (actor.cat == 0 && deltaLV < 0) {
                            damage -= Map.abs(deltaLV) * 50;
                            if (damage < 50) {
                                damage = 50;
                            }
                        }
                        if (this.skill[5] + this.addMoreLevelSkill[5] > 0) {
                            final int pc = CharManager.SKILL_DAM_PERCENT[4][5][this.skill[5] + this.addMoreLevelSkill[5]];
                            damage += damage * pc / 100;
                        }
                        actor.timeOutPoinson = System.currentTimeMillis();
                        actor.poinson = (short) damage;
                        final int idxTimeCash = Math.min(this.skill[4] + this.addMoreLevelSkill[4], timeCash.length - 1);
                        actor.tDelay = (short) timeCash[idxTimeCash];
                        actor.totalTime = 36;
                        final Message m = new Message(89);
                        m.dos.writeShort(actor.id);
                        m.dos.writeByte(actor.cat);
                        m.dos.writeByte(timeCash[idxTimeCash]);
                        m.dos.writeShort(damage);
                        m.dos.writeByte(4);
                        m.dos.writeByte(this.charClass);
                        m.dos.writeByte(36);
                        this.sendMessage(m);
                        this.sendToNearPlayer(m);
                        m.cleanup();
                    }
                }
            } else if (this.charClass == 3 && this.arrayBuff[0] != -1) {
                try {
                    if (actor.cat == 0 && ((Char) actor).charClass == 3) {
                        return;
                    }
                } catch (final Exception ex2) {
                }
                if (actor.beStune) {
                    return;
                }
                if ((actor.cat == 0 || (actor.cat == 1 && actor.isMonsterVantieu())) && actor.getLevel() - this.getLevel() > 5 && this.getLevel() < 60) {
                    return;
                }
                final int percent = CharManager.SKILL_DAM_PERCENT[3][4][this.skill[4] + this.addMoreLevelSkill[4]];
                if (Map.r.nextInt(100) <= percent) {
                    this.timeBeStune = 3000L + System.currentTimeMillis();
                    actor.beStune = true;
                    final Message i = new Message(89);
                    i.dos.writeShort(actor.id);
                    i.dos.writeByte(actor.cat);
                    i.dos.writeByte(-1);
                    i.dos.writeShort(-1);
                    i.dos.writeByte(4);
                    i.dos.writeByte(this.charClass);
                    i.dos.writeByte(3);
                    this.sendMessage(i);
                    this.sendToNearPlayer(i);
                }
            }
            if (actor.cat == 0) {
                final Char temp = (Char) actor;
                // SKIN LINH THU (Gau Truc): noi tai cong them vao PHAN SAT THUONG.
                // attNguHanh[4] chinh la dong "Phan st" (thuoc tinh so 32) ma trang bi
                // cong cho — nen cong o day la CONG DON voi phan sat thuong san co cua
                // do, dung nhu nguoi choi da chot.
                //
                // Tran 80% CHI AP KHI noi tai dang gop vao. Ap tran vo dieu kien se am
                // tham cat bot phan sat thuong cua nhung nguoi da co san tren 80% tu
                // trang bi — ho khong mac skin nao ma van bi yeu di.
                final int pcPstSkin = SkinLinhThu.pcCongPhanSatThuong(temp);
                int pcPst = temp.attNguHanh[4];
                if (pcPstSkin > 0) {
                    pcPst += pcPstSkin;
                    if (pcPst > SkinLinhThu.PC_TRAN_PST) {
                        pcPst = SkinLinhThu.PC_TRAN_PST;
                    }
                }
                int phandam = d * pcPst / 100;
                if (temp.arrayBuff[4] != -1 && temp.charClass == 0 && Map.r.nextInt(100) < 70) {
                    // Cap ky nang phan sat thuong la cua NGUOI BI DANH (temp) - ban cu cong bonus
                    // cua nguoi danh (this) vao, vua sai chu vua co the vuot bang -> AIOOBE bi
                    // nuot va ke tan cong thoat han phan don. Chan chi so trong bien bang.
                    final short[] bangPhan = CharManager.SKILL_DAM_PERCENT[temp.charClass][5];
                    int capPhan = temp.skill[5] + temp.addMoreLevelSkill[5];
                    if (capPhan < 0) {
                        capPhan = 0;
                    }
                    if (capPhan >= bangPhan.length) {
                        capPhan = bangPhan.length - 1;
                    }
                    final int phandam2 = d * bangPhan[capPhan] / 100;
                    if (phandam2 > phandam) {
                        phandam = phandam2;
                    }
                    if (this.charClass == 2) {
                        phandam -= (int) ((Char) actor).checkMagicShield(phandam);
                    }
                }
                if (phandam > 0) {
                    final Message j = new Message(89);
                    j.dos.writeShort(this.id);
                    j.dos.writeByte(this.cat);
                    j.dos.writeByte(0);
                    j.dos.writeShort(phandam);
                    j.dos.writeByte(5);
                    j.dos.writeByte(-1);
                    this.hp -= phandam;
                    if (this.hp <= 0) {
                        this.actorDie();
                        this.hp = 0;
                        if (Map.giveCardFail(this)) {
                            this.map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " giao thẻ thất bại"), this.myCountry);
                            this.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
                        }
                    }
                    this.sendMessage(j);
                    this.sendToNearPlayer(j);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    public boolean checkMatNaVaLan() {
        return ((this.wModel.isMatNaTho() || this.wModel.isMatNaCao() )&& this.wModel.isNguyetLinhTruong()) ;
 
    }


    public boolean checkVuKhiThanBinh() {
        return this.itemVukhiThoiTrang != null && this.itemVukhiThoiTrang.isVuKhiThanBinhNewUpdate09092024();
    }

    /**
     * Dang mac vu khi than binh DA tinh luyen xong 100% hay khong.
     *
     * Luc cham moc 100, Map.doPopup doi tempateID thanh oldId + 15, tuc nhay tu
     * dai 758-772 (than binh chua nang cap) sang 773-787 (than binh da nang cap).
     * Ke tu do checkVuKhiThanBinh() tra ve FALSE.
     *
     * Hau qua neu khong phan biet: nguoi choi mang vu khi da hoan thanh toi tinh
     * luyen se nhan thong bao "Ban phai mac vu khi than binh moi co the tinh
     * luyen" - trong khi ho DANG mac than binh. Con cau "da dat cap toi da" nam
     * ngay duoi thi khong bao gio chay toi.
     */
    public boolean daTinhLuyenToiDa() {
        return this.itemVukhiThoiTrang != null && this.itemVukhiThoiTrang.isVuKhiThanBinhNangCap();
    }

    public boolean CheckNguyenLieuTL() {
        // Kiểm tra xem có đang mặc vũ khí thần binh không
        if (this.itemVukhiThoiTrang == null || !this.itemVukhiThoiTrang.isVuKhiThanTL()) {
            return false;
        }
    
        int[] validMaterialIDs = {421,408,447,434,460,422,409,448,435,461,423,410,449,436,462,424,411,450,437,463,425,412,451,438,464,893,894,895,896,897};
        
        // Lấy level và type của trang bị đang mặc
        int equipLevel = this.itemVukhiThoiTrang.level;
        int equipType = this.itemVukhiThoiTrang.getTemplate().type;
    
        // Kiểm tra trong túi đồ
        for (Item item : this.iItems) {
            // Kiểm tra ID có trong danh sách nguyên liệu hợp lệ
            boolean isValidMaterial = false;
            for (int materialID : validMaterialIDs) {
                if (item.tempateID == materialID) {
                    isValidMaterial = true;
                    break;
                }
            }
            if (isValidMaterial) {
                // Kiểm tra level (phải < 11) và cùng type
                if (Math.abs(item.level - equipLevel) <= 10 && 
                    item.getTemplate().type == equipType) {
                    
                    return true; // Tìm thấy nguyên liệu hợp lệ
                }
            }
        }
    
        return false; // Không tìm thấy nguyên liệu hợp lệ
    }

    public String getNguyenLieuTL() {
        if (this.itemVukhiThoiTrang == null || !this.itemVukhiThoiTrang.isVuKhiThanTL()) {
            return "vũ khí thần binh";
        }
        
        // Get equipment type and level
        int equipType = this.itemVukhiThoiTrang.getTemplate().type;
        int equipLevel = this.itemVukhiThoiTrang.level;
        
        // Get valid materials for this type
        StringBuilder materials = new StringBuilder("\n");
        boolean first = true;
        
        int[] validMaterialIDs = {421,408,447,434,460,422,409,448,435,461,423,410,449,436,462,424,411,450,437,463,425,412,451,438,464,893,894,895,896,897};
        
        for (int materialID : validMaterialIDs) {
            ItemTemplates template = (ItemTemplates)Map.itemTemplates.get(0).get(materialID);
            if (template != null && template.type == equipType) {
                if (Math.abs(template.level - equipLevel) <= 10) {  // Add level check
                    if (!first) {
                        materials.append(" hoặc ");
                    }
                    materials.append(template.name);
                    if (equipLevel > 0) {
                        materials.append("(lv ").append(template.level).append(")");
                    }
                    first = false;
                }
            }
        }
        
        return materials.toString();
    }

public int applyRongLonReleaseTo(int baseDamage) {
    if (this.damageAccumulated == 0) return baseDamage;

    // cộng thêm tích * hệ số
    long out = (long) baseDamage + ((long) this.damageAccumulated * RL_RELEASE_MULT);

    // chống tràn int
    if (out > Integer.MAX_VALUE) out = Integer.MAX_VALUE;
    if (out < 0) out = 0;

    // reset sau khi xả
    this.damageAccumulated = 0;
    this.rlDeathCount = 0;

    try {
        this.sendMessage(MessageCreator.createServerAlertAutoOffMessage(
            "Tải Game Ngay Tại Ngude2.vn"
        ));
    } catch (Exception ignored) {}

    return (int) out;
}


    public int checkHapthuSatThuong(final int dam, final LiveActor actor) {
        if (actor.isBossSonTinhThuyTinh()) {
            return dam;
        }
        int finalDam = dam;
        // RỒNG LỚN: tích mọi lúc
        if (finalDam > 0 && this.rlAccumulateAlways) {
            long s = (long) this.damageAccumulated + finalDam;
            if (s > Integer.MAX_VALUE) s = Integer.MAX_VALUE;
            this.damageAccumulated = (int) s;
        }

        if (this.wModel.isMatNaTho() || this.wModel.isMatNaCao()) {
            int hpRecover = (finalDam * 10) / 100;
            
          
            
            this.hp += hpRecover;
            if (this.hp > this.maxhp) {
                this.hp = this.maxhp;
            }
      
            
            
        }

        int a = 0;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }

        if (isLinhThueLan()) {
            // Hấp thụ 3% sát thương
            a += finalDam * 3 / 100;
        }
        // "a +=" chu khong phai "a =": hap thu phai CONG DON nhu phan sat thuong.
        //
        // Truoc day dong nay gan de, nen no XOA SACH moi thu cong vao a truoc no — cu
        // the la 3% noi tai cua Lan ngay ben tren: nguoi choi cuoi mot con linh thu co
        // dong att[9] thi mat han 3% do, im lang, khong cach nao biet. Doi sang cong don
        // thi ca ba nguon (noi tai Lan, chi so linh thu, skin) deu duoc tinh.
        if (animalRide != null && animalRide.att[9] + animalRide.attUp[9] > 0 && Map.r.nextInt(200) <= animalRide.att[9] + animalRide.attUp[9]) {
            a += finalDam * (animalRide.att[9] + animalRide.attUp[9]) / 100;
        }
        final int backdam = this.getBackDam(dam);
        if (backdam > 0 && ((!actor.isLienHoaTru() && actor.isMonsterMoba()) || !this.map.isMapChienTruongMoba())) {
            final Message mbd = MessageCreator.createMsgBuffEffect(5, actor.cat, actor, backdam, 0, -1);
            this.sendMessage(mbd);
            this.sendToNearPlayer(mbd);
            actor.hp -= backdam;
            if (actor.hp <= 0) {
                if (actor.cat == 1) {
                    Map.onMosterDie(this, actor, (byte) 0, (short) backdam, (byte) 0, (byte) 0);
                } else {
                    actor.actorDie();
                }
            }
        }
        // SKIN LINH THU: thu kich hoat noi tai co ti le + hoi chieu.
        //
        // Dat o day vi checkHapthuSatThuong la CHO DUY NHAT moi duong sat thuong danh vao
        // nhan vat deu di qua — da soat het: PvP (MapDanhNhau, MapLienDau, Region,
        // RegionVantienTran), quai thuong (Monster), va toan bo cac lop Boss. Moc o
        // subDam se sot duong boss; moc o tung noi goi se sot khi them duong moi.
        //
        // Truyen finalDam de dieu kien "HP tut duoi 30%" tinh theo mau CON LAI SAU don
        // nay, chu khong phai doi den don ke tiep moi kich hoat.
        if (SkinLinhThu.thuKichHoat(this, actor, finalDam)) {
            SkinLinhThu.baoKichHoat(this);
        }
        a += finalDam * SkinLinhThu.pcChanSatThuong(this) / 100;

        // SKIN LINH THU (Chien Lang): hap thu them mot phan sat thuong.
        // PHAI dat SAU khoi hap thu cua thu cuoi o tren — khoi do dung "a = ..." (gan de)
        // chu khong phai "a += ...", nen bat cu thu gi cong vao a truoc no deu bi xoa
        // sach. (Do cung la ly do noi tai 3% cua Lan hien khong an khi dang cuoi mot con
        // thu co dong att[9] — loi co san, khong phai do dong nay.)
        a += finalDam * SkinLinhThu.pcHapThu(this) / 100;
        a += finalDam * this.getPcGiamSatThuong() / 100;
        if (a < 0) {
            a = 1;
        }
        finalDam -= a;
        if (finalDam < 0) {
            finalDam = 1;
        }
        return finalDam;
    }
    
    



    @Override
    public boolean isMagic() {
        return this.charClass == 2 || this.charClass == 4;
    }

    public int checkPassAttack(final LiveActor attacker, final int dam) {
        this.checkActiveSkillPet();
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 7) {
                return dam;
            }
        }
        if (attacker.isMagic()) {
            if (Map.r.nextInt(100) < this.attNguHanhAnimal[0]) {
                return 0;
            }
        } else if (Map.r.nextInt(100) < this.attNguHanhAnimal[1]) {
            return 0;
        }
        return dam;
    }

    @Override
    public int checkGiamSatThuongTong(final int dam) {
        int finalDam = dam;
        long pcgiam = 0L;
        if (this.wModel != null && System.currentTimeMillis() - this.timeBuffGiamSatThuong < 0L) {
            pcgiam = this.wModel.getPcGiamSatThuong();
        }
        final EffectBuff ef = this.hashEffBuff.get(EffectBuff.EFF_PHA_TRIEN);
        if (ef != null) {
            pcgiam += ef.dam;
        }
        finalDam -= (int) (finalDam * pcgiam / 100L);
        return finalDam;
    }

    /**
     * todo check giảm sát thương khi bị ăn đòn
     *
     * @param dam
     * @return
     */
    /**
     * Ban co ke tan cong: neu don nay dang hoa giai ky nang pet thi Pha trien (ky nang
     * pet, giam 50% sat thuong) khong duoc tinh cho rieng don do.
     */
    public int checkGiamSatThuong(final int dam, final Char attacker) {
        return checkGiamSatThuong(dam, attacker != null && attacker.dangHoaGiaiKyNangPet());
    }

    public int checkGiamSatThuong(final int dam) {
        return checkGiamSatThuong(dam, false);
    }

    private int checkGiamSatThuong(final int dam, final boolean boQuaPhaTrien) {
        int finalDam = dam;
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        if (animalRide != null && animalRide.att[10] + animalRide.attUp[10] > 0 && Map.r.nextInt(2000) <= animalRide.att[10] + animalRide.attUp[10]) {
            finalDam = 1;
        }
        long pcgiam = 0L;
        this.ischeckActivebuffgiamsatthuong();
        if (this.wModel != null && System.currentTimeMillis() - this.timeBuffGiamSatThuong < 0L) {
            pcgiam = this.wModel.getPcGiamSatThuong();
        }
        final EffectBuff ef = this.hashEffBuff.get(EffectBuff.EFF_PHA_TRIEN);
        if (ef != null && !boQuaPhaTrien) {
            pcgiam += ef.dam;
        }
        finalDam -= (int) (finalDam * pcgiam / 100L);
        return finalDam;
    }

    public long checkMagicShield(final int d) {
        long mpLost = 0L;
        try {
            if (this.arrayBuff[3] != -1 && this.charClass == 2) {
                final long pc = CharManager.SKILL_DAM_PERCENT[2][7][this.skill[7] + this.addMoreLevelSkill[7]];
                mpLost = d * pc / 100L;
                if (this.mp < mpLost) {
                    mpLost = this.mp;
                }
                this.mp -= (int) mpLost;
                if (this.mp < 0) {
                    this.mp = 0;
                }
                final Message m = new Message(89);
                m.dos.writeShort(this.id);
                m.dos.writeByte(this.cat);
                m.dos.writeByte(0);
                m.dos.writeShort((int) mpLost);
                m.dos.writeByte(7);
                m.dos.writeByte(2);
                m.dos.writeByte(0);
                this.sendMessage(m);
                this.sendToNearPlayer(m);
            }
        } catch (final Exception ex) {
        }
        if (mpLost < 0L) {
            mpLost = 0L;
        }
        return mpLost;
    }

    public boolean havecrit() {
        return Map.r.nextInt(300) < this.critical + this.getEffSkillClan(1) * this.critical / 100;
    }

    @Override
    public boolean haveBaoKich() {
        return Map.r.nextInt(1000) < this.baokich;
    }

    public int getBuffEffAttack() {
        final Random r = new Random(System.currentTimeMillis());
        switch (this.charClass) {
            case 0: {
                if (this.attNguHanh[3] > 0 && r.nextInt(100) < this.attNguHanh[3]) {
                    return 1;
                }
                if (this.skill[4] + this.addMoreLevelSkill[4] > 0 && r.nextInt(100) < CharManager.SKILL_DAM_PERCENT[0][4][this.skill[4] + this.addMoreLevelSkill[4]]) {
                    return 0;
                }
                break;
            }
            case 1:
            case 2:
            case 3:
            case 4: {
                if (this.attNguHanh[3] > 0 && r.nextInt(100) < this.attNguHanh[3]) {
                    return 1;
                }
                break;
            }
        }
        return -1;
    }

    public int calDelTaLevel(final Char actor1, final Char actor2) {
        return Map.abs(actor1.lvDetail.lv - actor2.lvDetail.lv);
    }

    public int playerAttackPlayer(final Char p1, final Char p2, final int buffAttack) {
        final int finalDam = 0;
        if (p1.charClass != 0 && p1.charClass != 1 && p1.charClass != 2 && p1.charClass != 3) {
            final byte charClass = p1.charClass;
        }
        return finalDam;
    }

    public int[] calInfoAttackPlayer(final Char actor, final int defend) {
        final int[] result = {defend, this.attack};
        if (this.isMonster) {
            final int[] array = result;
            final int n = 1;
            array[n] *= 2;
        }
        return result;
    }

    @Override
    public boolean attackMiss(final LiveActor actor) {
        return super.attackMiss(actor);
    }

    public boolean isCharMagic() {
        return this.charClass == 2 || this.charClass == 4;
    }

    public int getDamtThanThu(final LiveActor ac) {
        if (this.charthanthu != null) {
            return this.charthanthu.attackDam(ac);
        }
        return 0;
    }

    public int getDamtThanThuAuto(final LiveActor ac) {
        if (this.charthanthu != null) {
            return this.charthanthu.attackDamAuto(ac);
        }
        return 0;
    }

    public void charHireAttackDam(final LiveActor actor, final int type, final int level, final int bubbAttack) {
        try {
            if (this.charHire != null && this.charHire.hp > 0) {
                if (actor.isRuongMaquai() || actor.isNgocRong() || actor.isBossSonTinh() || actor.isBossThuyTinh() || actor.isBossSonTinhThuyTinh()) {
                    return;
                }
                if (this.charHire.isCoolDown(type) && actor.cat == 1) {
                    return;
                }
                this.charHire.attackDam(actor, type, level, bubbAttack);
            }
        } catch (final Exception ex) {
        }
    }

    public void charHireAttackMultiMOnster(final Vector<Monster> ms, final int idskill) {
        try {
            if (this.charHire != null && this.charHire.hp > 0) {
                if (this.charHire.isCoolDown(idskill)) {
                    return;
                }
                this.charHire.charHireAttackMultiMOnster(ms, idskill);
            }
        } catch (final Exception ex) {
        }
    }

    public boolean isNotDownExp() {
        return this.vip == 3;
    }

    @Override
    public int getAttack() {
        int att = super.getAttack() + this.getValueLua(2) * this.attack / 100;
        // Mat na Ong Dia (id 872): +10% sat thuong khi dang mac, luon luon
        if (this.wModel != null && this.wModel.isMatNaOngDia()) {
            att += att * 10 / 100;
        }
        return att;
    }

    // Mat na Ong Dia (id 872): dieu kien buff +20% hieu qua hoi HP/MP - dang mac
    // mat na VA hp hien tai duoi 30% toi da. Dung chung cho moi diem hoi HP/MP.
    public boolean isBuffHoiMatNaOngDia() {
        return this.wModel != null && this.wModel.isMatNaOngDia() && this.maxhp > 0 && this.hp * 100 / this.maxhp < 30;
    }

    @Override
    public boolean isKhangDoSat() {
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        return petUsing != null && petUsing.isPetBachThu();
    }

    @Override
    public void doHoaNguoiTuyetKhiBiDanh(final LiveActor p) {
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        if (animalRide != null) {
            final int pc = animalRide.getPcHoaNguoiTuyetKhiBiDanh();
            if (Map.r.nextInt(100) < pc) {
                p.setNguoiTuyet(2);
                MessageCreator.createMsgCharMonster((Char) p, this);
            }
        }
    }

    @Override // check tích 8 lần
    public void do8sRongLon(final LiveActor p) {
        Animal animalRide = this.animalRide;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 7) {
                animalRide = null;
            }
        }
        
        if (animalRide != null) {
            final int pc = animalRide.getPc8sRongLon();
            // Kiểm tra thời gian cooldown
            if (System.currentTimeMillis() - this.timeRongLon < 8000) {
                return;
            }
            
            if (Map.r.nextInt(100) < pc) {
                this.countRongLon++;
//                System.out.println("[DEBUG] do8sRongLon: countRongLon = " + this.countRongLon);

                    try {
                        this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Sát thương cam chịu tích lũy lên " + this.countRongLon));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (this.countRongLon >= 8) {
                        this.countRongLon = 0;
                        this.timeRongLon = System.currentTimeMillis();
                        this.timeActive8sRongLon = System.currentTimeMillis() + 8000;

//                        System.out.println("[DEBUG] Kích hoạt trạng thái bất tử 8s, timeActive8sRongLon = " + this.timeActive8sRongLon);

                        try {
                            this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Kích hoạt trạng thái bất tử trong 8 giây!"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

        }
        }

        @Override
        public boolean isActive8sRongLon() {
            return System.currentTimeMillis() < this.timeActive8sRongLon;        
        }

    public boolean isPetCanAttack(final LiveActor actor) {
        return (!actor.isBossSonTinh() || this.wModel.isThuyTinh()) && (!actor.isBossThuyTinh() || this.wModel.isSonTinh());
    }

    public void doUseHoaDuoc() {
        try {
            final Message msg = MessageCreator.createMsgNewEffectSkill5Long(EffectBuff.EFF_BOM_DIA_HOA, 1000, this.id, this.cat, true, false);
            this.sendMessage(msg);
            this.sendToNearPlayer(msg);
            final CharChienTruong cct = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (cct == null) {
                return;
            }
            for (int i = 0; i < this.nearChars.size(); ++i) {
                final Char p = this.map.getPlayerByID(this.nearChars.get(i));
                // p.session doc TRUOC kiem null: mot id trong nearChars vua roi map la NPE, catch
                // cuoi ham nuot va bo luon nhung nguoi dung sau trong danh sach.
                if (p != null && p.hp > 0 && p.id != this.id && p.isBot == -1 && this.near(p, 90) && p.map == this.map && p.region == this.region) {
                    final int hpb = p.maxhp * (cct.getNoiLuc() * 10) / 100;
                    final Char char1 = p;
                    char1.hp -= hpb;
                    try {
                        this.sendMessage(MessageCreator.createNew_HMP_Message(p, hpb));
                        this.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, hpb));
                    } catch (final IOException ex) {
                    }
                    if (p.hp <= 0) {
                        p.hp = 0;
                        p.desTroy();
                        p.actorDie();
                        this.doAddPointChienTruong(p.getPointChienTruong());
                        if (cct != null && !p.isCharCopy()) {
                            cct.checkLienTram(p.charname);
                        }
                        p.doSetTimeAutoHoiSinhMapMoba();
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInfoNuiChauBau() {
        int t = (int) this.getTimeNuiChauBau();
        if (t > 0 && t < 60) {
            t = 1;
        }
        if (t > 0) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Tìm kho báu: ", t, -1, Char.ID_TIME_NUI_KHO_BAU, Map.COUNT_DOWN, 618));
        }
    }

    public void sendInfoLoidai() {
        this.sendMessage(MessageCreator.createMsgTimeCountdown("Time: ", 180, -1, Char.ID_TIME_LOI_DAI, Map.COUNT_DOWN, -1));
    }

    public void sendInfoChienTruong(final int id, final int time) {
        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
        if (id == Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Còn lại " + MapChienTruongMoba.getAllKhu() + " khu", 5, -1, Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA, Map.NOT_COUNT_DOWN, -1));
        }
        if (id == Char.ID_NOI_LUC) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Nội lực: " + c.getNoiLuc() + "/10", 5, -1, Char.ID_NOI_LUC, Map.NOT_COUNT_DOWN, -1));
        }
        if (id == Char.ID_HON_DON) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("+50% st: ", time, -1, Char.ID_HON_DON, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_DINH_CHU) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("20% gây câm lặng: ", time, -1, Char.ID_DINH_CHU, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_HOI_HON) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Hồi 5% hp: ", time, -1, Char.ID_HOI_HON, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_HON_NGUYEN) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Kháng st trụ: ", time, -1, Char.ID_HON_NGUYEN, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_BACH_TIEN) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("+20% giáp: ", time, -1, Char.ID_BACH_TIEN, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_CAM_LANG) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Câm lặng: ", time, -1, Char.ID_CAM_LANG, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_PT_HOA_CONG) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Hoá công: ", time, -1, Char.ID_PT_HOA_CONG, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_PT_HON_DON) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Hỗn độn: ", time, -1, Char.ID_PT_HON_DON, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_PT_HON_NGUYEN) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Hỗn nguyên: ", time, -1, Char.ID_PT_HON_NGUYEN, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_HOI_SINH) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Hồi sinh sau: ", time, -1, Char.ID_HOI_SINH, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_INFO_PLAYER) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown(": ", time, -1, Char.ID_INFO_PLAYER, Map.NOT_COUNT_DOWN, -1));
        }
        if (id == Char.ID_NOI_LUC_TANG) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Nội lực x", time, -1, Char.ID_NOI_LUC_TANG, Map.COUNT_DOWN, -1));
        }
        if (id == Char.ID_DEM_NGUOC) {
            this.sendMessage(MessageCreator.createMsgTimeCountdown("Bắt đầu sau: ", time, -1, Char.ID_DEM_NGUOC, Map.COUNT_DOWN, -1));
        }
    }

    public int attackDamMonsterMoba(final LiveActor actor, final int idSkill, final int level, final int bubbAttack) {
        final int def = actor.getDefendPhysic() + actor.percentBuff[0];
        final int att = this.getAttack();
        actor.isLienHoaTru();
        return 0;
    }

    // Than method (434 dong) da chuyen sang CharChiSo.java. Chu ky GIU NGUYEN.
    public int attackDam(final LiveActor actor, final int idSkill, final int level, final int bubbAttack) {
        return CharChiSo.attackDam(this, actor,idSkill,level,bubbAttack);
    }

    @Override
    public Pet getPetUsing() {
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        return petUsing;
    }

    

    @Override
    public boolean isBatTu() {
        final EffectBuff ef = this.hashEffBuff.get(EffectBuff.BAT_TU);
        return ef != null && !ef.timeOut();
    }

    public boolean isHoangLoan() {
        return this.hashEffBuff.get(EffectBuff.HOANG_LOAN) != null;
    }

    public boolean isYemBua() {
        return this.hashEffBuff.get(EffectBuff.YEM_BUA) != null;
    }

    public boolean isVetThuongSau() {
        return this.hashEffBuff.get(EffectBuff.VET_THUONG_SAU) != null;
    }

    @Override
    public boolean haveDamAnhsang() {
        if (this.wModel != null) {
            return this.wModel.haveDameAnhSang();
        }
        return super.haveDamAnhsang();
    }

    @Override
    public boolean haveDamBongtoi() {
        if (this.wModel != null) {
            return this.wModel.haveDameBongtoi();
        }
        return super.haveDamBongtoi();
    }

    public int subDam(final Char c, int finalDame) {
        if (this.isCharMagic()) {
            finalDame -= finalDame * c.attNguHanh[1] / 100;
        } else {
            finalDame -= finalDame * c.attNguHanh[0] / 100;
        }
        if (finalDame < 0) {
            finalDame = 1;
        }
        return finalDame;
    }

    public int subDam(final Monster c, int finalDame) {
        int a = 0;
        if (c.magic_physic == 0) {
            a = finalDame * this.attNguHanh[1] / 100;
        } else {
            a = finalDame * this.attNguHanh[0] / 100;
        }
        if (a < 0) {
            a = 1;
        }
        finalDame -= a;
        // Than khi cua nguoi BI danh: "Giam % ST nhan vao tu Boss" + khang he.
        // Nguon la quai nen truyen classNguon = -1.
        finalDame = this.subDamThanKhi(finalDame, c.isBoss(), -1);
        if (finalDame < 0) {
            finalDame = 1;
        }
        return finalDame;
    }

    public void checkExpirePet() {
        try {
            boolean expire = false;
            for (int i = 0; i < this.pet.size(); ++i) {
                if (this.pet.get(i).getTimeExpire() <= 0) {
                    expire = true;
                    this.pet.remove(i);
                    --i; // remove(i) don phan tu ke tiep vao vi tri i - khong lui thi bo qua no
                }
            }
            if (expire) {
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            }
            // Hoa hinh Lan het han (10 ngay) -> tra pet ve con cu NGAY khi dang choi, khong doi
            // dang nhap lai (luat 02/09/2026). Pet trong tui don im lang; pet dang dung thi gui
            // lai hinh cho minh va nguoi xung quanh.
            for (int i = 0; i < this.pet.size(); ++i) {
                final data.Pet petTui = this.pet.get(i);
                if (petTui != this.petUsing && petTui.donHoaHinhHetHan()) {
                    Database.instance.savePet(petTui);
                }
            }
            if (this.petUsing != null && this.petUsing.donHoaHinhHetHan()) {
                Database.instance.savePet(this.petUsing);
                this.sendPetEff();
                this.sendMessage(MessageCreator.createCharWearingMessage(this, this));
                this.sendToNearPlayer(MessageCreator.createCharWearingMessage(this, this));
                this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hoá hình Lân của pet đã hết hạn, pet trở về hình cũ"));
                Database.instance.saveOrtherLog("", this.charname, "hoa hinh pet Lan het han", "hoahinhpet");
            }
            if (this.petUsing != null && this.petUsing.getTimeExpire() <= 0) {
                this.calculateAttrib();
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                Database.instance.saveOrtherLog("tob_log_other_item", this.charname, this.petUsing.getDBInfo(), "pd");
                Database.instance.deletePet(this.petUsing.idDB);
                this.sendMessage(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.petUsing.getName()) + " đã hết thời gian sử dụng"));
                this.petUsing = null;
                this.sendMessage(MessageCreator.createCharWearingMessage(this, this));
                this.sendToNearPlayer(MessageCreator.createCharWearingMessage(this, this));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void checkExpireMDAmor() {
        for (int i = 0; i < this.wItems[this.slotWearing].length; ++i) {
            final Item item = this.wItems[this.slotWearing][i];
            if (item != null && item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                try {
                    this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                    this.wItems[this.slotWearing][i] = null;
                    Database.instance.deleteItem(item.dbid);
                    this.calculateAttrib();
                    this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                    this.sendMessage(MessageCreator.createCharWearingMessage(this, this));
                    // 16/09/2026: them ten + dbid vao log (truoc day chi co idTemplate chim
                    // trong getDBInfo(), phai tra data_item moi biet la mon gi).
                    Database.instance.saveOrtherLog("tob_log_other_item", this.charname,
                            "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                } catch (final Exception ex) {
                }
            }
        }
    }

    public void checkExpireItemInentry() {
        try {
            final Vector<Item> itemExpire = new Vector<Item>();
            for (int i = 0; i < this.iItems.size(); ++i) {
                final Item item = this.iItems.get(i);
                if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                    itemExpire.add(item);
                }
            }
            if (itemExpire.size() > 0) {
                for (int i = 0; i < itemExpire.size(); ++i) {
                    try {
                        final Item item = itemExpire.get(i);
                        this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                        this.iItems.remove(item);
                        Database.instance.deleteItem(item.dbid);
                        // 16/09/2026: them ten + dbid (theo yeu cau "log chi tiet hon").
                        Database.instance.saveOrtherLog("tob_log_other_item", this.charname,
                                "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                        this.removeIDItem(item.id);
                    } catch (final Exception ex) {
                    }
                }
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            }
        } catch (final Exception ex2) {
        }
    }

    public boolean checkDurableWeapone() {
        for (int i = 0; i < this.wItems[this.slotWearing].length; ++i) {
            final Item item = this.wItems[this.slotWearing][i];
            if (item != null) {
                final int t = item.getTemplate().type;
                final int id = item.getTemplate().id;
    
                // Bỏ qua kiểm tra nếu ID nằm trong khoảng 773-787
                if (id >= 773 && id <= 787) {
                    continue;
                }
             

    
                if (t == 6 || t == 7 || t == 4 || t == 5 || t == 3 || t == 13) {
                    if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                        try {
                            if (!TeamServer.isServerIndo()) {
                                this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian thuê vũ khí " + item.getTemplate().name + " đã hết"));
                            } else {
                                this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Masa sewa senjata  " + item.getTemplate().name + " sudah berakhir"));
                            }
                            this.wItems[this.slotWearing][i] = null;
                            Database.instance.deleteItem(item.dbid);
                            this.calculateAttrib();
                            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                            this.sendMessage(MessageCreator.createCharWearingMessage(this, this));
                            // 16/09/2026: them ten + dbid (theo yeu cau "log chi tiet hon").
                            Database.instance.saveOrtherLog("tob_log_other_item", this.charname,
                                    "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                            if (t == 13) {
                                this.itemAx = null;
                            }
                        } catch (final Exception ex) {
                        }
                        return false;
                    }
                    if (item.mDurable == 0 || item.durable == 0) {
                        if (this.luongNap >= 20000 || this.diemNapVip >= 20000) {
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
    

  

    public int getPriceRepair(final int type) {
        final Vector<Item> wItems = this.getCharWearing();
        int pArmo = 0;
        int pWeapone = 0;
        int total = 0;
        for (int i = 0; i < wItems.size(); ++i) {
            final Item cwi = wItems.elementAt(i);
            if (cwi.getTemplate().type == 3 || cwi.getTemplate().type == 4 || cwi.getTemplate().type == 5 || cwi.getTemplate().type == 6 || cwi.getTemplate().type == 7) {
                pWeapone += cwi.getTemplate().price / 10;
            } else if (cwi.getTemplate().type == 0 || cwi.getTemplate().type == 1 || cwi.getTemplate().type == 2 || cwi.getTemplate().type == 11 || cwi.getTemplate().type == 10) {
                pArmo += cwi.getTemplate().price / 10;
            }
        }
        if (type == 0) {
            total = pArmo;
        } else if (type == 1) {
            total = pWeapone;
        } else {
            total = pArmo + pWeapone;
        }
        return total;
    }

    /**
     * Sua do dang mac. id: 0 = vu khi, 1 = giap, 2 = tat ca.
     *
     * NGOI NO CUA CHUOI LOI TIEN AM — da vá:
     * `id` den THANG tu goi tin 72 (MapXuLyLenh:698 -> OfflineMap.doRepair -> day) va la
     * mot byte tho, mien -128..127, khong he duoc loc o bat ky chang nao. Truoc day switch
     * chi phu 0/1/2 nhung lenh `this.xu -= price` lai nam NGOAI switch, nen mot id la
     * (vi du 3):
     *   - khong case nao chay -> KHONG kiem tra so du, va cung khong sua duoc mon do nao
     *   - nhung van tru 5000 xu, ghi THANG vao truong nen bo qua ca chan duoi 0 lan dong
     *     log cua subXu()
     * Tuc la nguoi choi tu ep duoc xu ve AM, am bao nhieu tuy y bang cach gui lai goi tin.
     * Va xu am chinh la thu kich hoat chuoi ban lien tuc o getxu() — xem chu thich
     * "PHAT HIEN TIEN AM" ben duoi.
     *
     * Nay: id la thi tu choi va ghi hack.txt; tru tien qua subXu() de co chan duoi 0,
     * cong dung xumat va co dong log nhu moi cho tieu xu khac.
     */
    public void repairItem(final int id) {
        if (id != 0 && id != 1 && id != 2) {
            data.NghiVanGianLan.ghiVatPham(this, "hackR",
                    "goi lenh sua do voi id = " + id + " (client goc chi gui 0, 1 hoac 2)");
            return;
        }
        final Vector<Item> wItems = this.getCharWearing();
        try {
            // Gia theo loai: 0 = vu khi, 1 = giap, 2 = tat ca.
            final int[] loaiGia = {1, 0, 2};
            final int price = this.getPriceRepair(loaiGia[id]);
            // TRU TIEN TRUOC, kiem-va-tru trong mot buoc (truXuNeuDu). Ban cu kiem `xu < price`
            // roi phuc hoi do ben, mai cuoi ham moi subXu: goi 72 chay tren luong phien, mot
            // duong tieu xu khac chay tren luong ban do (mua o Phu ong...) xen vao giua thi ca
            // hai deu qua kiem, subXu chan ve 0 -> sua do gan nhu mien phi.
            if (!this.truXuNeuDu(price, true, "sua do " + id)) {
                this.sendMessage(MessageCreator.createServerAlertMessage(Text.HET_TIEN, ""));
                return;
            }
            for (final Item item : wItems) {
                final int t = item.getTemplate().type;
                final boolean laVuKhi = t == 6 || t == 7 || t == 4 || t == 5 || t == 3;
                final boolean laGiap = t == 0 || t == 11 || t == 2 || t == 10 || t == 1;
                if (id == 2 || (id == 0 && laVuKhi) || (id == 1 && laGiap)) {
                    item.durable = item.getTemplate().durable;
                    item.mDurable = (short) (item.durable * 10);
                }
            }
            final String[] nhan = {"VK", "GIAP", "ALL"};
            final String[] loai = {"RW", "RG", "RA"};
            Database.instance.saveOrtherLog("tob_log_other_item", this.charname, String.valueOf(this.xu) + " sửa " + nhan[id] + " với giá " + price + " xu", loai[id]);
            this.calculateAttrib();
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            this.sendMessage(MessageCreator.createCharWearingMessage(this, this));
        } catch (final Exception e) {
            System.out.println("LOI TRONG HAM REPAIR ITEM");
        }
    }

    public int getDurableWeapone() {
        final Vector<Item> wItems = this.getCharWearing();
        for (final Item item : wItems) {
            final int t = item.getTemplate().type;
            if (t == 6 || t == 7 || t == 4 || t == 5 || t == 3) {
                return item.durable;
            }
        }
        return 0;
    }

    public int getDurableArmo() {
        final Vector<Item> wItems = this.getCharWearing();
        for (final Item item : wItems) {
            final int t = item.getTemplate().type;
            if (t == 0 || t == 11 || t == 2 || t == 1) {
                return item.durable;
            }
        }
        return 0;
    }

    public void downDurableWeapone() {
        final Vector<Item> wItems = this.getCharWearing();
        for (final Item item : wItems) {
            final int t = item.getTemplate().type;
            if ((t == 6 || t == 7 || t == 4 || t == 5 || t == 3) && item.mDurable > 0) {
                final Item item2 = item;
                --item2.mDurable;
                if (item.mDurable % 10 != 0) {
                    continue;
                }
                final Item item3 = item;
                --item3.durable;
            }
        }
    }

    public void downDuarable() {
        final Vector<Item> wItems = this.getCharWearing();
        for (final Item item : wItems) {
            final int t = item.getTemplate().type;
            if ((t == 0 || t == 11 || t == 2 || t == 10 || t == 1) && item.mDurable > 0) {
                final Item item2 = item;
                --item2.mDurable;
                if (item.mDurable % 10 != 0) {
                    continue;
                }
                final Item item3 = item;
                --item3.durable;
            }
        }
    }

    // ==================== THAN KHI — doc chi so dong an ====================
    // itemThanKhi KHONG nam trong wItems nen getCharWearing() khong thay no,
    // vi vay calculateAttrib() truoc day bo qua hoan toan. Cac ham duoi day la
    // cau noi duy nhat giua thanh khi va he thong chi so.
    //
    // Gia tri luu trong otherAtt[attrId - 61]. Bang HIDDEN_STAT_TABLE luu
    // NHAN SAN 10 cho cac dong dang phan tram le, nen doc ra phai chia lai.

    // Giao thuc gui thu vat ly / thu ma phap bang writeShort (16 bit co dau),
    // nen moi gia tri > 32767 se bi client doc thanh so am. Chan bien truoc
    // khi ghi goi tin; gia tri that ben trong van la int va van dung de tinh
    // sat thuong.
    public static short clampShort(final int v) {
        if (v > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        if (v < 0) {
            return 0;
        }
        return (short) v;
    }

    /** Bonus toc do di chuyen tu than khi, cong luc gui goi tin (khong luu trang thai). */
    public int getThanKhiSpeedBonus() {
        return getThanKhiRaw(161) > 0 ? 1 : 0;
    }

    /** Gia tri tho cua mot dong an tren than khi dang mac. 0 neu khong co. */
    public int getThanKhiRaw(final int attrId) {
        if (this.itemThanKhi == null) {
            return 0;
        }
        final int idx = attrId - 61;
        if (idx < 0 || idx >= this.itemThanKhi.otherAtt.length) {
            return 0;
        }
        return this.itemThanKhi.otherAtt[idx];
    }

    /** Lop chien binh / kiem khach / dau si — an dong sat thuong VAT LY. */
    public boolean isClassVatLy() {
        return !this.isCharMagic();
    }

    /** Tong diem sat thuong cong them tu than khi, da loc theo class. */
    public int getThanKhiDiemSatThuong() {
        // 158/159 = o du phong danh rieng cho than khi. Truoc dung 140/139 la
        // trung voi "Tri Mang"/"HP" cua trang bi thuong.
        return this.isClassVatLy() ? getThanKhiRaw(158) : getThanKhiRaw(159);
    }

    /** Phan tram sat thuong cong them tu than khi, da loc theo class. */
    public int getThanKhiPhanTramSatThuong() {
        return this.isClassVatLy() ? getThanKhiRaw(144) : getThanKhiRaw(145);
    }

    // =====================================================================
    // THAN KHI — cac dong con lai.
    //
    // QUY UOC DON VI: cot ispercent trong data_attribute chi la goi y hien
    // thi cho client. Voi ispercent = 2 client in ra value/10 (vd luu 250 ->
    // "25.0%"), nghia la PHAN TRAM THAT = raw/10. De khong mat phan le khi
    // bang co gia tri le (15 -> 1,5%), moi ham duoi tra ve raw va noi ap dung
    // chia cho 1000. Con ispercent = 1 la phan tram thang, chia 100.
    // =====================================================================

    /** raw/1000. Xuyen thu: bo qua bot giap muc tieu. */
    public int getTkXuyenThu(final boolean targetMagicDef) {
        return targetMagicDef ? getThanKhiRaw(146) : getThanKhiRaw(147);
    }

    /** raw/1000. Cong them sat thuong len boss / tru. */
    public int getTkStBoss() {
        return getThanKhiRaw(148);
    }

    /** raw/1000. Cong them sat thuong len quai thuong. */
    public int getTkStQuai() {
        return getThanKhiRaw(149);
    }

    /** raw/1000. Cong them sat thuong theo % HP toi da cua muc tieu. */
    public int getTkStTheoHp() {
        return getThanKhiRaw(142);
    }

    /** raw/1000. Cong them sat thuong theo % MP toi da cua muc tieu. */
    public int getTkStTheoMp() {
        return getThanKhiRaw(143);
    }

    /** raw/1000. Cong them sat thuong khi ra don chi mang. */
    public int getTkStChiMang() {
        return getThanKhiRaw(141);
    }

    /**
     * Nhan he so chi mang (x2) hoac bao kich (x4) roi cong them phan tram
     * "Tang sat thuong chi mang" cua than khi. Thay cho "damage *= heSo" o
     * tat ca cac diem tinh chi mang, de mot cho sua la ap dung toan bo.
     */
    public int tkNhanChiMang(final int damage, final int heSo) {
        long d = (long) damage * heSo;
        final int tk = getTkStChiMang();
        if (tk > 0) {
            d += d * tk / 1000L;
        }
        if (d > Integer.MAX_VALUE) {
            d = Integer.MAX_VALUE;
        }
        return (int) d;
    }

    /** raw/1000. Giam sat thuong nhan tu boss. */
    public int getTkGiamStBoss() {
        return getThanKhiRaw(150);
    }

    /** raw/1000. Giam sat thuong nhan tu nguoi choi khac. */
    public int getTkGiamStPlayer() {
        return getThanKhiRaw(180);
    }

    /**
     * raw/1000. Khang theo he cua ke tan cong, cong voi "Khang tat ca".
     * Anh xa lop -> he theo dac ta:
     *   Kiem khach(0)=Kim 152, Chien binh(1)=Hoa 155, Phap su(2)=Thuy 154,
     *   Dau si(3)=Tho 156, Cung thu(4)=Moc 153.
     * NAME_CLASS = {Kiem khach, Chien binh, Phap su, Dau si, Cung thu}.
     */
    public int getTkKhangTheoClass(final int attackerClass) {
        final int[] idTheoClass = {152, 155, 154, 156, 153};
        int tong = getThanKhiRaw(186); // Khang tat ca — luon cong
        if (attackerClass >= 0 && attackerClass < idTheoClass.length) {
            // Khang he luu ispercent=1 (phan tram thang) nen phai x10 de dua
            // ve cung don vi phan nghin voi "Khang tat ca".
            tong += getThanKhiRaw(idTheoClass[attackerClass]) * 10;
        }
        return tong;
    }

    /** Phan tram thang (/100). Hut HP theo sat thuong gay ra. */
    public int getTkHutHp() {
        return getThanKhiRaw(182);
    }

    /** Phan tram thang (/100). Hut MP theo sat thuong gay ra. */
    public int getTkHutMp() {
        return getThanKhiRaw(181);
    }

    /** Phan tram thang. Ne tranh — noi dung doi sang phan nghin. */
    public int getTkNeTranh() {
        return getThanKhiRaw(183);
    }

    /** Phan tram thang (/100). Phan lai mot phan sat thuong nhan vao. */
    public int getTkPhanDon() {
        return getThanKhiRaw(185);
    }

    /** Phan tram thang (/100). Giam thoi gian hoi chieu. */
    public int getTkGiamHoiChieu() {
        return getThanKhiRaw(160);
    }

    /** Phan tram thang. Ti le gay cam lang len muc tieu. */
    public int getTkCamLang() {
        return getThanKhiRaw(162);
    }

    /** raw/1000. Giam suc manh + phong thu cua muc tieu. */
    public int getTkGiamSucManhDich() {
        return getThanKhiRaw(184);
    }

    /**
     * Dong "Giam suc manh + phong thu cua ke dich" la dong DUY NHAT co thoi
     * gian hieu luc doi theo cap trong bang thiet ke: cap 1..10 -> 3, 3, 4, 4,
     * 5, 5, 6, 6, 7, 8 giay. Truoc day cam cung 3000ms nen tu cap 3 tro len la
     * ngan hon mo ta.
     */
    private static final int[] TK_SUY_YEU_GIAY = { 3, 3, 4, 4, 5, 5, 6, 6, 7, 8 };

    /**
     * Thoi gian suy yeu (ms) suy nguoc tu gia tri tho. HIDDEN_STAT_TABLE[27] la
     * 20 * cap nen cap = raw / 20; chan lai 1..10 phong khi dong nay bi sua tay
     * trong DB thanh so ngoai bang.
     */
    public static long tkSuyYeuThoiGian(final int raw) {
        int cap = raw / 20;
        if (cap < 1) {
            cap = 1;
        }
        if (cap > 10) {
            cap = 10;
        }
        return TK_SUY_YEU_GIAY[cap - 1] * 1000L;
    }

    /**
     * Tran giam sat thuong. Bang so cua game cho toi 200 (tuc 20%) o moi dong
     * rieng le, nhung cong don khang he + khang tat ca + giam ST boss/nguoi
     * choi co the vuot 100% lam nhan vat bat tu. Chan lai o 80%.
     */
    public static final int TK_TRAN_GIAM_ST = 800; // phan nghin

    /** Ap dung mot muc giam sat thuong tinh theo phan nghin, da chan tran. */
    public static int tkGiamSatThuong(final int dam, int phanNghin) {
        if (phanNghin <= 0 || dam <= 0) {
            return dam;
        }
        if (phanNghin > TK_TRAN_GIAM_ST) {
            phanNghin = TK_TRAN_GIAM_ST;
        }
        return (int) ((long) dam - (long) dam * phanNghin / 1000L);
    }

    /**
     * "Bo qua tc ma" / "Bo qua tc vat" cua mat na, don vi phan tram giap muc tieu.
     * Mat na cao ghi otherAtt[0] (tc ma), mat na tho ghi otherAtt[1] (tc vat).
     */
    public int getPcBoQuaThuMatNa() {
        if (this.wModel == null || this.wModel.matNa == null) {
            return 0;
        }
        return this.isCharMagic() ? this.wModel.matNa.otherAtt[0] : this.wModel.matNa.otherAtt[1];
    }

    /** "Chuyen sat thuong thanh hp" cua mat na, phan tram sat thuong gay ra. */
    public int getPcChuyenStThanhHpMatNa() {
        if (this.wModel == null || this.wModel.matNa == null) {
            return 0;
        }
        return this.wModel.matNa.otherAtt[68];
    }

    /**
     * "Hoa giai ky nang pet" chi kich hoat khi deo mat na tho/cao VA cam Nguyet Linh
     * Truong — dung cap do checkMatNaVaLan() da viet san nhung chua duoc goi o dau.
     */
    public boolean coDuDoHoaGiaiKyNangPet() {
        return this.wModel != null
                && (this.wModel.isMatNaTho() || this.wModel.isMatNaCao())
                && this.wModel.isNguyetLinhTruong();
    }

    /** % hoa giai ghi tren mat na. Tra 0 neu chua du bo mat na + Nguyet Linh Truong. */
    public int getPcHoaGiaiKyNangPet() {
        if (this.wModel == null || this.wModel.matNa == null || !coDuDoHoaGiaiKyNangPet()) {
            return 0;
        }
        return this.wModel.matNa.otherAtt[71];
    }

    /** Ket qua quay hoa giai cua don danh dang xu ly. Dat lai o dau moi attackDam. */
    private boolean hoaGiaiPetHit = false;

    public void rollHoaGiaiKyNangPet() {
        final int pc = getPcHoaGiaiKyNangPet();
        this.hoaGiaiPetHit = pc > 0 && Map.r.nextInt(100) < pc;
    }

    /** Don danh dang xu ly co dang hoa giai ky nang pet cua muc tieu khong. */
    public boolean dangHoaGiaiKyNangPet() {
        return this.hoaGiaiPetHit;
    }

    /**
     * Ban tinh cho phia PHONG THU: mat na cua muc tieu hoa giai ky nang pet cua ke tan
     * cong (Lien kich). Quay rieng vi day khong phai don danh cua chinh minh.
     */
    public static boolean hoaGiaiKyNangPet(final LiveActor target) {
        if (!(target instanceof Char)) {
            return false;
        }
        final int pc = ((Char) target).getPcHoaGiaiKyNangPet();
        return pc > 0 && Map.r.nextInt(100) < pc;
    }

    /** "Khang bao kich" cua mat na muc tieu, cung don vi phan nghin voi baokich. */
    public int getKhangBaoKichMatNa() {
        if (this.wModel == null || this.wModel.matNa == null) {
            return 0;
        }
        return this.wModel.matNa.otherAtt[15];
    }

    /**
     * Bao kich co tinh den "Khang bao kich" tren mat na cua muc tieu. Dung ban nay o
     * cac duong danh nguoi; danh quai thi quai khong co mat na nen giu ban khong doi so.
     */
    public boolean haveBaoKich(final LiveActor target) {
        int bk = this.baokich;
        if (target instanceof Char) {
            bk -= ((Char) target).getKhangBaoKichMatNa();
        }
        return bk > 0 && Map.r.nextInt(1000) < bk;
    }

    /**
     * Khang ngu hanh cua thu cuoi dang cuoi, doi ra PHAN NGHIN de cong chung don vi
     * voi khang he cua than khi (xem getTkKhangTheoClass va tkGiamSatThuong).
     */
    public int getKhangNguHanhThuCuoi(final int attackerClass) {
        if (this.animalRide == null) {
            return 0;
        }
        return this.animalRide.getKhangNguHanh(attackerClass) * 10;
    }

    /** Muc giam sat thuong phan don cua noi tai "Tra dua" (phan tram). */
    public static final int PC_TRA_DUA_RONG_LON = 50;

    /**
     * Noi tai "Tra dua" ghi trong bang thong tin Rong Lon: dang cuoi Rong Lon VA
     * dang mac Hoa Ky Lan thi chi con nhan mot nua sat thuong phan don. Dung chu
     * "Ket hop hoa ki lan" nen bat buoc phai co ca hai, thieu mot cai la khong tinh.
     */
    public boolean isTraDuaRongLon() {
        return this.itemHoaKyLan != null && this.animalRide != null && this.animalRide.isRongLon();
    }

    /** Ap noi tai "Tra dua" len mot don phan sat thuong sap tru vao chinh nguoi nay. */
    public int giamSatThuongPhanDon(final int damPhan) {
        if (damPhan <= 0 || !isTraDuaRongLon()) {
            return damPhan;
        }
        return damPhan - damPhan * PC_TRA_DUA_RONG_LON / 100;
    }

    /**
     * Goi o cuoi attackDam(). Gom toan bo hieu ung than khi phu thuoc vao sat
     * thuong da tinh xong: cong them theo muc tieu, tru theo khang cua muc
     * tieu, roi cac hieu ung dinh kem (hut HP/MP, phan don, cam lang, suy yeu).
     *
     * this  = nguoi danh, actor = muc tieu.
     */
    // Than method (137 dong) da chuyen sang CharChiSo.java. Chu ky GIU NGUYEN.
    private int applyThanKhiOnHit(final LiveActor actor, int finalDame) {
        return CharChiSo.applyThanKhiOnHit(this, actor,finalDame);
    }

    /**
     * Giam sat thuong cua than khi khi NHAN don. Goi o duong quai/boss danh
     * nguoi choi (subDam) — cho ra "Giam % ST nhan vao tu Boss" va khang he.
     *
     * @param nguonLaBoss muc tieu dang bi danh boi boss hay quai thuong
     * @param classNguon  class cua Char gay sat thuong, -1 neu la quai
     */
    public int subDamThanKhi(final int finalDame, final boolean nguonLaBoss, final int classNguon) {
        if (this.itemThanKhi == null || finalDame <= 0) {
            return finalDame;
        }
        int giam = 0;
        if (nguonLaBoss) {
            giam += getTkGiamStBoss();
        }
        if (classNguon >= 0) {
            giam += getTkGiamStPlayer();
            giam += getTkKhangTheoClass(classNguon);
        } else {
            giam += getThanKhiRaw(186); // khang tat ca van tinh voi quai
        }
        return tkGiamSatThuong(finalDame, giam);
    }

    // Than method (210 dong) da chuyen sang CharChiSo.java. Chu ky GIU NGUYEN.
    private void calculateAttack() {
        CharChiSo.calculateAttack(this);
    }

    // Than constructor (413 dong) da chuyen sang real/CharKhoiTao.java.
    // super() BAT BUOC la lenh dau tien cua constructor nen phai o lai day.
    public Char(final Session conn) {
        super((byte) 0);
        CharKhoiTao.init(this, conn);
    }

    public void initWItem() {
    }

    public void setTimeCake() {
        int[] tick = {10000, 9000, 8000, 10000, 10000, 10000, 10000, 11000, 12000};
        this.timeDropCake = System.currentTimeMillis() + tick[Map.r.nextInt(tick.length)];
        if (isSuKienNoel()) {
            tick = new int[]{30000, 60000, 75000, 80000, 81000, 85000};
            this.timeDropCake = System.currentTimeMillis() + tick[Map.r.nextInt(tick.length)];
        }
    }

    public void setTimeCake1() {
        int[] tick = {60000, 120000, 150000, 160000, 162000, 170000};
        this.timeDropCake1 = System.currentTimeMillis() + tick[Map.r.nextInt(tick.length)];
        if (isSuKienNoel()) {
            tick = new int[]{7200000, 7320000, 7350000, 7360000, 7362000, 7370000};
            this.timeDropCake1 = System.currentTimeMillis() + tick[Map.r.nextInt(tick.length)];
        }
    }

    public void setTimeCake2() {
        final int[] tick = {29, 29, 30, 30, 31, 31};
    }

    public Message sendBuffToUser(final Char ch) {
        final Message m = new Message(51);
        try {
            m.dos.writeByte(2);
            m.dos.writeShort(ch.id);
            m.dos.write(ch.arrayBuff, 0, ch.arrayBuff.length);
            for (int i = 0; i < ch.arrayBuff.length; ++i) {
                m.dos.writeShort(ch.timeOut[i] - ch.countDown[i]);
            }
            m.dos.write(ch.lvBuff, 0, ch.lvBuff.length);
        } catch (final Exception ex) {
        }
        return m;
    }

    public Message sendRemoveBuffToUser(final Char ch) {
        final Message m = new Message(51);
        try {
            m.dos.writeByte(2);
            m.dos.writeShort(ch.id);
            m.dos.write(ch.arrayBuff, 0, ch.arrayBuff.length);
            for (int i = 0; i < ch.arrayBuff.length; ++i) {
                m.dos.writeShort(1);
            }
            m.dos.write(ch.lvBuff, 0, ch.lvBuff.length);
        } catch (final Exception ex) {
        }
        return m;
    }

    public int[] setTimeOutBuff() {
        try {
            final byte[][] temp = {{4, 5}, {1, 1, 6}, {5, 5, 6, 3}, {0, 1, 6}, {2, 3, 6}};
            for (int i = 0; i < temp[this.charClass].length; ++i) {
                if (this.skill[i + 4] + this.addMoreLevelSkill[i + 4] != -1) {
                    this.timeOut[temp[this.charClass][i]] = CharManager.TIME_LIFE_BUFF_SKILL[temp[this.charClass][i]][this.skill[i + 4] + this.addMoreLevelSkill[i + 4]];
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean calculatorHPMP() {
        boolean rs = false;
        if (this.hp > this.maxhp + this.percentBuff[2]) {
            this.hp = this.maxhp + this.percentBuff[2];
            rs = true;
        }
        if (this.mp > this.maxmp + this.percentBuff[1]) {
            this.mp = this.maxmp + this.percentBuff[1];
            rs = true;
        }
        return rs;
    }

    public void resetGoldTimeCountry() {
        try {
            if (this.x2 != 0) {
                final Message m = new Message(86);
                m.dos.writeByte(2);
                m.dos.writeByte(this.x2);
                m.dos.writeInt(this.getGoldTime());
                m.dos.writeUTF(this.getInfoX2());
                this.sendMessage(m);
            } else {
                final Message m = new Message(86);
                m.dos.writeByte(0);
                this.sendMessage(m);
            }
        } catch (final Exception ex) {
        }
    }

    void updateGoldTime() {
        final int time = 86400000;
        try {
            if (this.x2 == 1) {
                if (System.currentTimeMillis() - this.halfGoldTime > time) {
                    this.x2 = 0;
                    this.halfGoldTime = 0L;
                    this.fullGoldTime = 0L;
                    final Message m = new Message(86);
                    m.dos.writeByte(0);
                    this.sendMessage(m);
                    m.cleanup();
                }
            } else if (this.x2 == 2) {
                if (this.minuteExp > 0) {
                    if (System.currentTimeMillis() - this.fullGoldTime > 60000L) {
                        --this.minuteExp;
                        this.fullGoldTime = System.currentTimeMillis();
                        if (this.minuteExp <= 0) {
                            this.x2 = 0;
                            this.fullGoldTime = 0L;
                            this.minuteExp = 0;
                            final Message m = new Message(86);
                            m.dos.writeByte(0);
                            this.sendMessage(m);
                        }
                    }
                } else if (System.currentTimeMillis() - this.fullGoldTime > time) {
                    this.x2 = 0;
                    this.halfGoldTime = 0L;
                    this.fullGoldTime = 0L;
                    final Message m = new Message(86);
                    m.dos.writeByte(0);
                    this.sendMessage(m);
                    m.cleanup();
                }
            }
            if ((this.x25Exp > 0 || this.x2Phaohoa > 0) && System.currentTimeMillis() - this.startPlay > 1000L) {
                if (this.x25Exp > 0) {
                    --this.x25Exp;
                    this.startPlay = System.currentTimeMillis();
                    if (this.x25Exp % 900 == 0) {
                        this.sendMessage(MessageCreator.createMsgChat(this.id, "Thời gian nhận được 150% kinh nghiệm của bạn còn lại " + this.x25Exp / 60 + " phút"));
                    }
                    if (this.x25Exp == 0) {
                        try {
                            this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng vé giờ vàng tăng 150% kinh nghiệm đẵ hết"));
                        } catch (final Exception ex) {
                        }
                    }
                }
                if (this.x2Phaohoa > 0) {
                    --this.x2Phaohoa;
                    if (this.x2Phaohoa % 900 == 0) {
                        this.sendMessage(MessageCreator.createMsgChat(this.id, "Thời gian nhận được 50% kinh nghiệm của bạn còn lại " + this.x2Phaohoa / 60 + " phút"));
                    }
                }
            }
        } catch (final Exception ex2) {
        }
    }

    public void doSetTimeAutoHoiSinh() {
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            this.doSetTimeAutoHoiSinhMapMoba();
        } else if (this.timeHoiSinh == 0L && this.lvDetail.lv < 50 && (this.map.isMapTrain() || this.map.isMapBoss())) {
            this.timeHoiSinh = System.currentTimeMillis() + this.timeHs;
            if (this.isKiller) {
                this.timeHs += Char.timeAddMoreHs * 2;
            } else {
                this.timeHs += Char.timeAddMoreHs;
            }
        }
    }

    public void doSetTimeAutoHoiSinhMapMoba() {
        if (this.timeHoiSinh == 0L && this.map != null && (this.map.isMapChienTruongMoba() || this.map.isMapLoiDai())) {
            if (this.map.isMapLoiDai()) {
                final RegionLoiDai region = this.map.getRegionLoiDai(this.idRegionLoidai);
                if (!region.isFinish) {
                    region.isFinish(this, false);
                    this.timeHoiSinh = System.currentTimeMillis() + 5000L;
                }
            } else {
                final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
                if (c != null) {
                    this.timeHoiSinh = System.currentTimeMillis() + c.getNoiLuc() * 5 * 1000;
                    this.sendInfoChienTruong(Char.ID_HOI_SINH, c.getNoiLuc() * 5);
                    c.lientram = 0;
                    c.resetTimeBuff();
                }
            }
        }
    }

    @Override
    public int getPointChienTruong() {
        return 3;
    }

    @Override
    public void actorDie() 
    {
//        System.out.println("actor die: " + charname);
//        Thread.dumpStack();  // <-- In ra nơi gọi đến phương thức này        
        try 
        {
            // Chet giua giao dich: huy cho ca hai ben va xoa SACH trang thai (ke ca
            // tPotions/rPotions - ban cu de sot, xem datLaiTrangThaiGiaoDich).
            if (this.userTrade.size() > 0) {
                this.huyGiaoDich(false, true);
            }
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            this.hpDocLan = 0;
            this.isThodia = false;
            this.finishTrade = false;
            final Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            this.sendMessage(m);
            this.sendToNearPlayer(m);
            m.cleanup();
            if (this.map != null && (this.map.isMapChienTruongMoba() || this.map.isMapLoiDai())) 
            {
                this.doSetTimeAutoHoiSinhMapMoba();
            } else 
            {
                this.doSetTimeAutoHoiSinh();
            }
            this.timedie = System.currentTimeMillis();
            if (this.timeWaitComeHome == 0L) {
                this.timeWaitComeHome = this.timedie;
            }
            this.partyIsDie(this, this.mapID);
            if (this.isMonster || this.isNguoiTuyet()) 
            {
                this.map.charMonsterDissapear(this);
                this.map.removeCharOutEventMonster(this);
            }

            this.hashEffBuff.clear();
            this.huyAllEff(this);
            this.AllEffBuff.removeAllElements();
        } 
        catch (final Exception ex) 
        {
//            System.out.println("Loi khi nhan vat die: " + charname + ", eff còn lại: " + AllEffBuff.size());
            ex.printStackTrace();
        }
    }

    //doiten
    public boolean isNhom() {
    return this.partyID != -1 ;
}

    public void setInfoHelp(final short idBot) {
        if (this.indexHelp >= Text.help.length || this.indexHelp == 0) {
            this.indexHelp = 0;
            this.timeSendHelp = System.currentTimeMillis();
        }
        this.idBot = idBot;
    }

    public synchronized void isCrazyChangeMap(final short id) {
        if (this.timeCrazy > 0L) {
            if (this.isCrazy != -1) {
                this.calculateAttrib();
            }
            if (id == 0) {
                this.defend_magic *= 2;
                this.defend_physic *= 2;
                this.attack *= 2;
                this.accurate *= 2;
                this.critical *= 2;
                this.dodge *= 2;
                this.isCrazy = id;
                this.defendMaPlayer *= 2;
                this.defendVatPlayer *= 2;
                this.attackPlayer *= 2;
            } else if (id == 1) {
                this.defend_magic = this.defend_magic * 2 + this.defend_magic / 2;
                this.defend_physic = this.defend_physic * 2 + this.defend_physic / 2;
                this.attack = this.attack * 2 + this.attack / 2;
                this.accurate = (short) (this.accurate * 2 + this.accurate / 2);
                this.critical = (short) (this.critical * 2 + this.critical / 2);
                this.dodge = (short) (this.dodge * 2 + this.dodge / 2);
                this.isCrazy = id;
                this.defendMaPlayer = this.defendMaPlayer * 2 + this.defendMaPlayer / 2;
                this.defendVatPlayer = this.defendVatPlayer * 2 + this.defendVatPlayer / 2;
                this.attackPlayer = this.attackPlayer * 2 + this.attackPlayer / 2;
            }
        } else {
            this.calculateAttrib();
            this.isCrazy = -1;
        }
    }

    public synchronized void isCrazy(final short id) {
        if (this.timeCrazy > 0L) {
            if (this.isCrazy == -1 || this.isCrazy != id) {
                if (this.isCrazy != -1) {
                    this.calculateAttrib();
                }
                if (id == 0) {
                    this.defend_magic *= 2;
                    this.defend_physic *= 2;
                    this.attack *= 2;
                    this.accurate *= 2;
                    this.critical *= 2;
                    this.dodge *= 2;
                    this.isCrazy = id;
                    this.defendMaPlayer *= 2;
                    this.defendVatPlayer *= 2;
                    this.attackPlayer *= 2;
                } else if (id == 1) {
                    this.defend_magic = this.defend_magic * 2 + this.defend_magic / 2;
                    this.defend_physic = this.defend_physic * 2 + this.defend_physic / 2;
                    this.attack = this.attack * 2 + this.attack / 2;
                    this.accurate = (short) (this.accurate * 2 + this.accurate / 2);
                    this.critical = (short) (this.critical * 2 + this.critical / 2);
                    this.dodge = (short) (this.dodge * 2 + this.dodge / 2);
                    this.isCrazy = id;
                    this.defendMaPlayer = this.defendMaPlayer * 2 + this.defendMaPlayer / 2;
                    this.defendVatPlayer = this.defendVatPlayer * 2 + this.defendVatPlayer / 2;
                    this.attackPlayer = this.attackPlayer * 2 + this.attackPlayer / 2;
                }
            }
        } else {
            this.calculateAttrib();
            this.isCrazy = -1;
        }
    }

    public void selectionSort(final Vector<Integer> a, final Vector<Integer> d) {
        for (int j = 0; j < a.size() - 1; ++j) {
            int iMin = j;
            for (int i = j + 1; i < a.size(); ++i) {
                if (a.get(i) < a.get(iMin)) {
                    iMin = i;
                }
            }
            if (iMin != j) {
                final int b = a.get(iMin);
                final int c = d.get(iMin);
                a.setElementAt(a.elementAt(j), iMin);
                a.setElementAt(b, j);
                d.setElementAt(d.elementAt(j), iMin);
                d.setElementAt(c, j);
            }
        }
    }

    public int getUpPCBangSetDocLuaLan(final int type) {
        if (this.wModel != null) {
            if (type == 0) {
                return ((this.wModel.wpModel != null) ? this.wModel.wpModel.otherAtt[8] : 0) + ((this.wModel.matNa != null) ? this.wModel.matNa.otherAtt[8] : 0) + ((this.wModel.CanhModel != null) ? this.wModel.CanhModel.otherAtt[8] : 0);
            }
            if (type == 1) {
                return ((this.wModel.wpModel != null) ? this.wModel.wpModel.otherAtt[9] : 0) + ((this.wModel.matNa != null) ? this.wModel.matNa.otherAtt[9] : 0) + ((this.wModel.CanhModel != null) ? this.wModel.CanhModel.otherAtt[9] : 0);
            }
            if (type == 2) {
                return ((this.wModel.wpModel != null) ? this.wModel.wpModel.otherAtt[10] : 0) + ((this.wModel.matNa != null) ? this.wModel.matNa.otherAtt[10] : 0) + ((this.wModel.CanhModel != null) ? this.wModel.CanhModel.otherAtt[10] : 0);
            }
            if (type == 3) {
                return ((this.wModel.wpModel != null) ? this.wModel.wpModel.otherAtt[29] : 0) + ((this.wModel.matNa != null) ? this.wModel.matNa.otherAtt[29] : 0) + ((this.wModel.CanhModel != null) ? this.wModel.CanhModel.otherAtt[29] : 0);
            }
        }
        if (this.itemVukhiThoiTrang != null && this.itemVukhiThoiTrang.isChoiLuaLanBienHinh()) {
            return this.itemVukhiThoiTrang.otherAtt[29];
        }
        return 0;
    }

    public int getUpDamBangSetDocLuaLan(final int type) {
        if (this.wModel != null && this.wModel.wpModel != null) {
            if (type == 0) {
                return this.wModel.wpModel.otherAtt[12];
            }
            if (type == 1) {
                return this.wModel.wpModel.otherAtt[13];
            }
            if (type == 2) {
                return this.wModel.wpModel.otherAtt[11];
            }
            if (type == 3) {
                return this.wModel.wpModel.otherAtt[30];
            }
        }
        if (this.itemVukhiThoiTrang != null && this.itemVukhiThoiTrang.isChoiLuaLanBienHinh()) {
            return this.itemVukhiThoiTrang.otherAtt[30];
        }
        return 0;
    }

    /** Dang deo Choi Set (614/618) o model, dung cho hieu ung "Set danh" cua skin Kim Cang Long. */
    public boolean dangDeoChoiSet() {
        return this.wModel != null && this.wModel.wpModel != null && this.wModel.wpModel.isChoiSet();
    }

    public static int getIndexRandomXoso(final int... num) {
        try {
            final Random r = new Random(System.currentTimeMillis());
            // Cong don bang long: 20 nguoi x 1 ty da vuot tran int
            long max = 0L;
            for (int i = 0; i < num.length; ++i) {
                max += num[i];
            }
            if (max <= 0L) {
                return -1;
            }
            final long intRandom = (long) (r.nextDouble() * max) + 1L;
            long percent = 0L;
            for (int j = 0; j < num.length; ++j) {
                percent += num[j];
                if (percent >= intRandom) {
                    return j;
                }
            }
        } catch (final Exception ex) {
        }
        return -1;
    }

    public static int getIndexRandom(final Vector<Short> num) {
        try {
            int max = 0;
            for (int i = 0; i < num.size(); ++i) {
                max += num.get(i);
            }
            final int intRandom = Map.r.nextInt(max) + 1;
            int percent = 0;
            for (int j = 0; j < num.size(); ++j) {
                percent += num.get(j);
                if (percent >= intRandom) {
                    return j;
                }
            }
        } catch (final Exception ex) {
        }
        return -1;
    }

    public static int getIndexRandom(final int... num) {
        try {
            int max = 0;
            for (int i = 0; i < num.length; ++i) {
                max += num[i];
            }
            final int intRandom = Map.r.nextInt(max) + 1;
            int percent = 0;
            for (int j = 0; j < num.length; ++j) {
                percent += num[j];
                if (percent >= intRandom) {
                    return j;
                }
            }
        } catch (final Exception ex) {
        }
        return -1;
    }

    @Override
    public boolean isKhangLan(final int type, int giamKhang) {
        if (type == 1) {
            return this.khangSetLan(giamKhang);
        }
        if (type == 0) {
            return this.khangBangLan(giamKhang);
        }
        if (type == 2) {
            return this.khangDocLan(giamKhang);
        }
        return this.khangLuaLan(giamKhang);
    }

    @Override
    public boolean isActiveTienKhi() {
        return this.hashEffBuff.get(EffectBuff.EFF_TIEN_KHI) != null;
    }

  
    @Override
    public void checkActiveSkillPet() {
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        if (petUsing != null) {
            int[] a = petUsing.getTienKhi();
            if ((Map.r.nextInt(100) < a[0] || (TeamServer.isServerLocal() && a[0] > 0)) && this.addEffBuff(EffectBuff.EFF_TIEN_KHI, System.currentTimeMillis() + a[1], EffectBuff.BY_ACTOR, 0) != null) {
                final Message msg = MessageCreator.createMsgNewEffectSkill5Long(EffectBuff.EFF_TIEN_KHI, a[1], this.id, this.cat, false, false);
                this.sendMessage(msg);
                this.sendToNearPlayer(msg);
            }
            a = petUsing.getPhatTrien();
            if ((Map.r.nextInt(100) < a[1] || (TeamServer.isServerLocal() && a[1] > 0)) && this.addEffBuff(EffectBuff.EFF_PHA_TRIEN, System.currentTimeMillis() + a[2], EffectBuff.BY_ACTOR, a[0]) != null) {
                final Message msg = MessageCreator.createMsgNewEffectSkill5Long(EffectBuff.EFF_PHA_TRIEN, a[2], this.id, this.cat, false, false);
                this.sendMessage(msg);
                this.sendToNearPlayer(msg);
            }
        }
    }

    // Than method (127 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void checkNewEffectPet(final LiveActor beAttack) {
        CharCapNhat.checkNewEffectPet(this, beAttack);
    }

    // todo tính toán giảm kháng
    private int calGiamKhangLan(int type) {

        if (this.animalRide != null) {
            return this.animalRide.getGiamKhangLan(type);
        }

        return 0;
    }

    // Than method (330 dong) da chuyen sang CharTrangBi.java. Chu ky GIU NGUYEN.
    public void checkNewEffectItem(final int cat, long damage, final LiveActor attacker) {
        CharTrangBi.checkNewEffectItem(this, cat,damage,attacker);
    }

    public void doHutMau() {
    }

    public int getIdSkillAttack() {
        return 0;
    }

    public void doCharHireAttackPlayer(final Char be_attacked) {
    }

    public void doCharHireAttackMonster(final Monster be_attacked) {
    }

    public void doCharHireAttackMultiMonster(final Vector<Monster> allmonster) {
    }

    public void doCreateCharThanThu(final int idThanthu) {
        try {
            final byte[][] idquanao = {{2, 28, 54}, {1, 27, 53}};
            this.charthanthu = new CharThanthu((Session) null);
            this.charthanthu.owner = this;
            this.charthanthu.headStyle = 9;
            this.charthanthu.gender = this.gender;
            this.charthanthu.charClass = this.charClass;
            this.charthanthu.lvDetail.setExpNew(LevelDetail.getXpFromLevel(40));
            this.charthanthu.mapID = this.map.mapId;
            this.charthanthu.id = RealController.intance.idGen.getID(0, "Tao bot");
            this.charthanthu.x = this.x;
            this.charthanthu.y = this.y;
            this.charthanthu.map = this.map;
            this.charthanthu.lastLV = 40;
            this.charthanthu.charClass = this.charClass;
            this.charthanthu.charname = "@" + this.charname + "@";
            // "Suc khoe" cua Than Thu doc tu Hoa Ky Lan dang mac (otherAtt 39), cong duoc
            // "ao hoa" (+10%, xem Char.chiSoTuyetDoiHoaKyLan). CA BA cap hien deu ghi
            // otherAtt[39]=1000 nen gia tri KHONG doi so voi ban cu khi chua ao hoa - chi
            // khac tu luc co ao hoa, hoac neu sau nay them cap moi voi suc khoe rieng.
            // itemHoaKyLan BAT BUOC phai duoc gan TRUOC khi goi ham nay - xem
            // doTrangBiHoaKyLan (noi goi DUY NHAT cua doCreateCharThanThu).
            final int sucKhoe = this.chiSoTuyetDoiHoaKyLan(39);
            this.charthanthu.maxhp = (sucKhoe > 0) ? sucKhoe : 1000;
            this.charthanthu.maxmp = 1000;
            this.charthanthu.hp = this.charthanthu.maxhp;
            this.charthanthu.mp = 1000;
            this.charthanthu.setXtoYto(this.x, this.y);
            this.charthanthu.userID = this.userID + 1000000000;
            this.charthanthu.myCountry = this.myCountry;
            this.charthanthu.inCountry = this.inCountry;
            this.charthanthu.idThanThu = (byte) (idThanthu - 1);
            CharManager.instance.put(this.charthanthu);
            this.sendMessage(this.writeActorPos(new Message(4), this.charthanthu));
            if (!this.isAdmin) {
                this.sendToNearPlayer(this.writeActorPos(new Message(4), this.charthanthu));
            }
        } catch (final Exception ex) {
        }
    }

    public void doCreateCharHire(final int cclass, final int capdo, final int gender, final int lvLinhthue) {
        if (this.charHire != null) {
            this.charhireDissapear();
        }
        this.charHire = new CharCopy((Session) null);
        final byte[][] idquanao = {{2, 28, 54}, {1, 27, 53}};
        final int gd = gender;
        // Doc capdo chu KHONG doc this.lvlinh: this.lvlinh chi duoc gan o cuoi ham
        // nay, nen luc dat ten no van dang giu gia tri cua lan trieu truoc. Lan dau
        // trieu ho ve lan (capdo 64) thi ten ra "ho ve" thay vi "lan su vu"; phai
        // dang xuat vao lai moi dung, vi luc do checkCreateCharHire goi lai voi
        // lvlinh da luu.
        this.charHire.setInfoChar(String.valueOf(this.charname) + (capdo == 64 ? " lan su vu" : " ho ve"), -1, gd + 1, cclass, this.map, 1088, 1008, -100000 + -1 * this.userID, idquanao[gd][0], idquanao[gd][1], -1);

        final int[] idWeapone = {599, 601, 603, 605, 607};
        this.charHire.wItems[0][getIndexItemWearing(3, 0)] = this.genItem(idWeapone[cclass], 0);
        this.charHire.headStyle = (byte) ((gd == 0) ? 10 : 9);
        this.charHire.id = RealController.intance.idGen.getID(0, "Tao bot");
        this.charHire.lvDetail.setExpNew(LevelDetail.getXpFromLevel(capdo));
        ((CharCopy) this.charHire).lvLinhthue = (byte) lvLinhthue;
        this.charHire.lastLV = (byte) capdo;
        this.charHire.charClass = (byte) cclass;
        this.charHire.x = this.x;
        this.charHire.y = this.y;
        this.charHire.mapID = this.mapID;
        this.charHire.region = this.region;
        this.charHire.setXtoYto(this.x, this.y);
        this.charHire.myCountry = this.myCountry;
        this.charHire.inCountry = this.inCountry;
        ((CharCopy) this.charHire).owner = this;
        ((CharCopy) this.charHire).setMaxHp();
        CharManager.instance.put(this.charHire);
        this.charHire.setFollow();
        this.sendMessage(this.writeActorPos(new Message(4), this.charHire));
        this.sendToNearPlayer(this.writeActorPos(new Message(4), this.charHire));
        this.classlinh = (byte) cclass;
        this.genderlinh = (byte) gender;
        this.lvlinh = (byte) capdo;
        this.lvlinhthue = (byte) lvLinhthue;
       
        if (this.lvlinh == 64) {
            this.charHire.setCharHire(true);
            
        }
       
       
    }


 
    
    public void checkCreateThangBe(final int mapID) {
    }

    public boolean isLinhThueLan() {
        return this.lvlinh == 64 && this.timeExistCharHire > 0L &&  System.currentTimeMillis() < this.timeExistCharHire && this.charHire != null;
    }

    public void doInitCharCopy(final int mapid, final int x, final int y) {
        if (this.charCopy != null) {
            this.charCopyDissapear();
        }
        if (Map.r.nextInt(100) < 100) {
            this.charCopy = new CharCopy((Session) null);
            final byte[][] idquanao = {{2, 28, 54}, {1, 27, 53}};
            final int gd = Map.r.nextInt(2);
            this.charCopy.setInfoChar(String.valueOf(this.charname) + " copy", -64, gd + 1, 0, RealController.mapList.get(mapid), 1088, 1008, -10000 + -1 * this.userID, idquanao[gd][0], idquanao[gd][1], -1);
            this.charCopy.headStyle = (byte) ((gd == 0) ? 10 : 9);
            this.charCopy.id = RealController.intance.idGen.getID(0, "Tao bot");
            this.charCopy.charClass = this.charClass;
            this.charCopy.x = x * 16;
            this.charCopy.y = y * 16;
            this.charCopy.setXtoYto(x * 16, y * 16);
            this.charCopy.region = 0;
            this.charCopy.myCountry = this.myCountry;
            this.charCopy.inCountry = this.inCountry;
            ((CharCopy) this.charCopy).owner = this;
            CharManager.instance.put(this.charCopy);
        }
    }

    public void doInitCharCopy() {
        if (this.charCopy != null) {
            this.charCopyDissapear();
        }
        this.charCopy = new CharCopy((Session) null);
        final byte[][] idquanao = {{2, 28, 54}, {1, 27, 53}};
        final int gd = Map.r.nextInt(2);
        final int[] idMap = isSuKienMiniChucNu() ? Char.idmapchucnu : Char.idmapthangbe;
        int indexMap = Map.r.nextInt(idMap.length);
        if (TeamServer.isServerLocal()) {
            if (isSuKienMiniChucNu()) {
                indexMap = 1;
            } else {
                indexMap = 2;
            }
        }
        System.out.println("MAP THANG BE: " + idMap[indexMap]);
        this.charCopy.setInfoChar(String.valueOf(this.charname) + " copy", -64, gd + 1, 0, RealController.mapList.get(idMap[indexMap]), 1088, 1008, -10000 + -1 * this.userID, idquanao[gd][0], idquanao[gd][1], -1);
        this.charCopy.headStyle = (byte) ((gd == 0) ? 10 : 9);
        this.charCopy.id = RealController.intance.idGen.getID(0, "Tao bot");
        if (isSuKienMiniChucNu()) {
            this.charCopy.setInfoModelCharBot(0, 574);
        }
        this.charCopy.charClass = this.charClass;
        if (isSuKienMiniChucNu()) {
            this.charCopy.x = Char.xychucnu[indexMap][0] * 16;
            this.charCopy.y = Char.xychucnu[indexMap][1] * 16;
            this.charCopy.setXtoYto(Char.xychucnu[indexMap][0] * 16, Char.xychucnu[indexMap][1] * 16);
        } else {
            this.charCopy.x = Char.xythangbe[indexMap][0] * 16;
            this.charCopy.y = Char.xythangbe[indexMap][1] * 16;
            this.charCopy.setXtoYto(Char.xythangbe[indexMap][0] * 16, Char.xythangbe[indexMap][1] * 16);
        }
        this.charCopy.region = this.region;
        this.charCopy.myCountry = this.myCountry;
        this.charCopy.inCountry = this.inCountry;
        ((CharCopy) this.charCopy).owner = this;
        CharManager.instance.put(this.charCopy);
    }

    public static Char doCreateCharNhanBan(final Char p) {
        final Char charCopy = new CharCopy((Session) null);
        final byte[][] idquanao = {{2, 28, 54}, {1, 27, 53}};
        final int gd = Map.r.nextInt(2);
        final int[] idMap = Char.idmapthangbe;
        int indexMap = Map.r.nextInt(idMap.length);
        if (TeamServer.isServerLocal()) {
            indexMap = 2;
        }
        charCopy.charDBID = -MapChienTruongMoba.all_char_chien_truong.size();
        charCopy.setInfoChar(String.valueOf(p.charname) + " " + MapChienTruongMoba.all_char_chien_truong.size(), -1, gd + 1, 0, RealController.mapList.get(idMap[indexMap]), 1088, 1008, -10000 + -1 * MapChienTruongMoba.all_char_chien_truong.size(), idquanao[gd][0], idquanao[gd][1], -1);
        charCopy.headStyle = (byte) ((gd == 0) ? 10 : 9);
        charCopy.id = RealController.intance.idGen.getID(0, "Tao bot");
        charCopy.charClass = p.charClass;
        ((CharCopy) charCopy).isCharChienTruong = true;
        charCopy.lastLV = 60;
        charCopy.x = Char.xythangbe[indexMap][0] * 16;
        charCopy.y = Char.xythangbe[indexMap][1] * 16;
        charCopy.setXtoYto(Char.xythangbe[indexMap][0] * 16, Char.xythangbe[indexMap][1] * 16);
        charCopy.region = p.region;
        charCopy.myCountry = p.myCountry;
        charCopy.inCountry = p.inCountry;
        ((CharCopy) charCopy).isCharChienTruong = true;
        CharManager.instance.put(charCopy);
        return charCopy;
    }

    public void setFollow() {
    }

    public boolean isCharHire() {
        return false;
    }

    public void setCharHire(final boolean hire) {
    }

    // ================= TI LE MAU TIEU + HE SO EXP TRA TIEU =================
    // Quan ly qua SQL (bang cau_hinh, xem server.CauHinh) - sua qua
    // web/admin/cauhinh.php, server tu nap lai toi da 10 giay, khong can restart.
    /**
     * Nguong CONG DON xac suat mau tieu khi nhan tieu moi (doInitMonter), tren
     * thang Map.r.nextInt(100000). 5 gia tri cho Trang/Xanh/Do/Hoan My/Vang,
     * phan con lai (ran >= gia tri cuoi) la Tim. PHAI tang dan, moi gia tri
     * trong khoang 0..100000.
     */
    public static int[] TIEU_NGUONG_MAU = {60189, 89389, 99689, 99989, 99999};
    /**
     * He so % nhan truoc khi ap TIEU_PC_EXP_THEO_MAU (CharSuKien.doResponseTieu),
     * mac dinh 200 = giu nguyen "x2" (TangExpVanTieu) cu.
     */
    public static int TIEU_HE_SO_EXP = 200;
    /**
     * % EXP theo mau tieu (chi so 0 Trang..5 Tim, phan tu thu 7 khong dung toi)
     * khi lvOpen < 50 hoac mau Trang/Xanh/Do.
     */
    public static int[] TIEU_PC_EXP_THEO_MAU = {50, 100, 150, 250, 250, 500, 0};
    /**
     * Phan nghin EXP can de len cap cho rieng Hoan My/Vang/Tim tu lv50, gop
     * phang 3 khung cap (50-59 / 60-69 / 70+) x 3 mau thanh 9 phan tu -
     * chi so = khung*3 + (mau - 3).
     */
    public static int[] TIEU_PERMILLE_CAO_CAP = {50, 70, 90, 35, 55, 75, 5, 25, 45};

    /**
     * Nguong CONG DON xac suat mau khi DOI tieu (doChangeTieu, reroll tra phi) - bang
     * RIENG, doc lap voi TIEU_NGUONG_MAU (do la luc NHAN tieu moi). Cung dinh dang: 5
     * gia tri cho Trang/Xanh/Do/Hoan My/Vang tren thang 100000, PHAI tang dan, phan con
     * lai la Tim. Dung khi tra phi bang XU. Quan ly qua SQL (bang cau_hinh, xem
     * server.CauHinh) - sua qua web/admin/cauhinh.php, server tu nap lai toi da 10 giay.
     */
    public static int[] DOI_TIEU_NGUONG_MAU_XU = {50545, 85545, 98945, 99945, 99995};
    /** Nhu DOI_TIEU_NGUONG_MAU_XU, nhung dung khi tra phi bang LUONG (dat hon, ti le mau hiem cao hon). */
    public static int[] DOI_TIEU_NGUONG_MAU_LUONG = {22890, 52890, 96890, 99890, 99990};

    public static synchronized void doInitMonter(final Char p, final int typetieu) {
        try {
            if (p.monster != null) {
                if (!p.monster.isFinish()) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa hoàn thành chuyến tiêu đã nhận", ""));
                    return;
                }
                try {
                    final Message m = new Message(90);
                    m.dos.writeShort(p.monster.id);
                    m.dos.writeByte(p.monster.cat);
                    Map.sendAllCharServerInMap(new int[]{0, 70, 301, 1701, 1, 2, 3, 4, 5, 6, 7, 8, 9, 118}, m);
                    MonsterVantieu.removeMonster(p.monster);
                    RealController.intance.idGen.putID(p.monster.id, 1, "");
                    p.monster = null;
                    p.typeTieu = -1;
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            if (p.luot_van_tieu < 1) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã hết lượt nhận tiêu", ""));
                return;
            }
            p.upSpeedNguaVanTieu = 1;
            final String[] name = {"Trắng", "Xanh", "Đỏ", "Hoàn mỹ", "Vàng", "Tím"};
            final int[][] idMonster = {{95, 96, 97, 98, 99, 100}, {101, 102, 103, 104, 105, 106}, {107, 108, 109, 110, 111, 112}};
            // Ha ti le Tim xuong thap nhat co the (0.001%) va Vang xuong con rat it
            // (0.01%) - yeu cau nguoi dung 13/09/2026 "ae van di duoc vang tim nhung se
            // rat it". Doi thang do tu nextInt(1000) (buoc 0.1%) sang nextInt(100000)
            // (buoc 0.001%) de bieu dien duoc phan tram nho nhu vay. Do/Xanh/Trang giu
            // NGUYEN ti le tuong doi cu (x100); phan xac suat rut tu Vang/Tim don het
            // vao Trang (mau pho bien nhat, khong doi ty le giua Do/Xanh/HoanMy).
            final int ran = Map.r.nextInt(100000);
            final int[] nguongMau = TIEU_NGUONG_MAU;
            int indexMons = 5;
            if (ran < nguongMau[0]) {
                indexMons = 0; // Trang
            } else if (ran < nguongMau[1]) {
                indexMons = 1; // Xanh
            } else if (ran < nguongMau[2]) {
                indexMons = 2; // Do
            } else if (ran < nguongMau[3]) {
                indexMons = 3; // Hoan My
            } else if (ran < nguongMau[4]) {
                indexMons = 4; // Vang
            }
            // else: Tim (indexMons=5) - ran >= nguongMau[4]
            p.typeTieu = (byte) (typetieu * 6 + indexMons);
            p.monster = new MonsterVantieu(p.map, Map.monsterTemplates.get(idMonster[typetieu][indexMons]), p.x, p.y, p.myCountry);
            p.monster.id = RealController.intance.idGen.getID(1, "");
            p.monster.charDbid = p.charDBID;
            p.monster.level = p.lvDetail.lv;
            p.monster.typeTieu = p.typeTieu;
            p.monster.maxhp = Char.HP_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] + Char.HP_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] * Char.UP_HP_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] / 100;
            p.monster.hp = p.monster.maxhp;
            p.monster.defend_magic = Char.DEF_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] + Char.DEF_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] * Char.UP_DEF_MONSTER_VANTIEU[(p.lvDetail.lv - 40) / 10] / 100;
            p.monster.defend_physic = p.monster.defend_magic;
            p.monster.setTimeFinish(System.currentTimeMillis() + 1200000L);
            p.monster.namemaster = p.charname;
            MonsterVantieu.addMonster(p.monster);
            p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được tiêu " + name[indexMons], ""));
            Database.instance.saveOrtherLog("", p.charname, "Nhan tieu " + name[indexMons], "nhantieu");
            try {
                if (indexMons > 3) {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " lãnh thổ " + Map.nameCountry[p.myCountry] + " đã nhận được tiêu " + name[indexMons]));
                }
            } catch (final IOException ex) {
            }
            if (!p.isAdmin && TeamServer.server != 100) {
                --p.luot_van_tieu;
            }
            final long[] receive_TIEU = Map.RECEIVE_TIEU;
            final int n = indexMons;
            ++receive_TIEU[n];
            Database.instance.saveMonsterVantieu(p.monster);
        } catch (final Exception e) {
            p.typeTieu = -1;
            p.monster.actorDie();
            p.monster = null;
            e.printStackTrace();
        }
    }

    public boolean canChangeTieu() {
        return (this.map.mapIDLoadMap == 0 || this.map.mapIDLoadMap == 5 || this.myCountry != 1) && (this.map.mapIDLoadMap == 70 || this.map.mapIDLoadMap == 5 || this.myCountry != 0) && (this.map.mapIDLoadMap == 5 || this.map.mapIDLoadMap == 0 || this.map.mapIDLoadMap == 70) && (this.map.mapIDLoadMap != 5 || (this.x / 16 >= 37 && this.y / 16 >= 37 && this.x / 16 <= 51 && this.y / 16 <= 49)) && this.monster.map.mapId == this.map.mapId && this.region == this.monster.region;
    }

    public void doChangeTieu(final int xu_luong) {
        synchronized (Map.CHANGE_TIEU) {
            if (this.monster == null) {
                //monitorexit(Map.CHANGE_TIEU);
                return;
            }
            this.upSpeedNguaVanTieu = 1;
            final int oldColor = this.typeTieu % 6;
            final int[][] idMonster = {{95, 96, 97, 98, 99, 100}, {101, 102, 103, 104, 105, 106}, {107, 108, 109, 110, 111, 112}};
            // Doi tieu (reroll co tra phi) dung bang ngoung RIENG (DOI_TIEU_NGUONG_MAU_XU/
            // _LUONG), doc lap voi TIEU_NGUONG_MAU cua luc nhan tieu moi - sua qua
            // web/admin/cauhinh.php (muc "Doi tieu"), khong con hardcode nhu truoc
            // 15/09/2026. Y do goc: cho ti le Tim/Vang cao hon mot chut so voi ti le goc,
            // de dong tra phi con co gia tri.
            final int ran = Map.r.nextInt(100000);
            final int[] nguongDoiMau = (xu_luong == 0) ? DOI_TIEU_NGUONG_MAU_XU : DOI_TIEU_NGUONG_MAU_LUONG;
            int index = 5;
            if (ran < nguongDoiMau[0]) {
                index = 0; // Trang
            } else if (ran < nguongDoiMau[1]) {
                index = 1; // Xanh
            } else if (ran < nguongDoiMau[2]) {
                index = 2; // Do
            } else if (ran < nguongDoiMau[3]) {
                index = 3; // Hoan My
            } else if (ran < nguongDoiMau[4]) {
                index = 4; // Vang
            }
            // else: Tim (index=5) - ran >= nguongDoiMau[4]
            try {
                final Message m = new Message(90);
                m.dos.writeShort(this.monster.id);
                m.dos.writeByte(this.monster.cat);
                Map.sendAllCharServerInMap(new int[]{0, 70, 301, 1701, 1, 2, 3, 4, 5, 6, 7, 8, 9, 118}, m);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            this.monster.idTemplate = idMonster[this.typeTieu / 6][index];
            this.typeTieu = (byte) (this.typeTieu / 6 * 6 + index);
            this.monster.setType(Map.monsterTemplates.get(this.monster.idTemplate).type);
            this.monster.typeTieu = this.typeTieu;
            try {
                this.monster.map.sendAllCharMap(this.writeActorPos(new Message(4), this.monster), this.myCountry, 0);
            } catch (final Exception ex) {
            }
            try {
                final Message m = new Message(7);
                m.dos.writeShort(this.monster.id);
                m.dos.writeByte(this.monster.getType());
                m.dos.writeShort(this.monster.x);
                m.dos.writeShort(this.monster.y);
                m.dos.writeInt(this.monster.hp);
                m.dos.writeByte(this.monster.level);
                m.dos.writeByte(this.monster.he);
                m.dos.writeInt(this.monster.maxhp);
                m.dos.writeInt(this.monster.getTimeReborn());
                this.sendMessage(m);
                this.monster.map.sendAllPlayer(m, this.monster.inCountry, this.region);
            } catch (final Exception ex2) {
            }
            final String[] name = {"Trắng", "Xanh", "Đỏ", "Hoàn mỹ", "Vàng", "Tím"};
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được tiêu " + name[index], ""));
            try {
                if (index > 3) {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " lãnh thổ " + Map.nameCountry[this.myCountry] + " đã đổi được tiêu " + name[index]));
                }
            } catch (final IOException ex3) {
            }
            final long[] change_TIEU = Map.CHANGE_TIEU;
            final int n = index;
            ++change_TIEU[n];
            final long[] card_TIEU = Map.CARD_TIEU;
            ++card_TIEU[xu_luong];
            try {
                Database.instance.saveOrtherLog("", this.charname, "doi tu tieu " + name[oldColor] + " sang tieu" + name[index], "doitieu");
                Database.instance.saveMonsterVantieu(this.monster);
            } catch (final Exception ex4) {
            }
            //monitorexit(Map.CHANGE_TIEU);
        }
    }

    // Than method (120 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public void doResponseTieu(final Char pReceive, final boolean isThief, final int type, int lvOpen) {
        CharSuKien.doResponseTieu(this, pReceive,isThief,type,lvOpen);
    }

    public Item doAddItemVanTieu(final Char pRcv, final int colorTieu, final int lvOpen) {
        Item it = this.map.dropItemAnimalByLvChar(lvOpen, 4);
        if (it == null) {
            return null;
        }
        it.id = pRcv.getIDItem();
        // Tieu Trang (mau thap nhat): truoc day 50% khong nhan duoc gi ca (ca nhan van
        // vao "return null" o cuoi ham vi khong mau nao trong cac nhanh duoi khop 0).
        // Yeu cau nguoi dung 16/09/2026: dam bao luon co trang bi thu, giong moi mau
        // khac deu chac chan co do (chi khac nhau o mau/do hiem cua do).
        if (colorTieu == 0) {
            pRcv.iItems.add(it);
            return it;
        }
        if (colorTieu == 1) {
            if (Map.r.nextInt(100) < 50) {
                pRcv.iItems.add(it);
                return it;
            }
            it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_BLUE, Map.r.nextInt(3) + 1);
            pRcv.iItems.add(it);
            return it;
        } else if (colorTieu == 2) {
            if (Map.r.nextInt(100) < 50) {
                it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_BLUE, Map.r.nextInt(3) + 1);
                pRcv.iItems.add(it);
                return it;
            }
            it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_RED, Map.r.nextInt(3) + 1);
            pRcv.iItems.add(it);
            return it;
        } else if (colorTieu == 3 || colorTieu == 4) {
            if (Map.r.nextInt(100) < 50) {
                it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_RED, Map.r.nextInt(3) + 1);
                pRcv.iItems.add(it);
                return it;
            }
            it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_GREEN, (Map.r.nextInt(100) < 10) ? 1 : 2);
            pRcv.iItems.add(it);
            return it;
        } else {
            if (colorTieu == 5) {
                it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_GREEN, (Map.r.nextInt(100) < 10) ? 1 : 2);
                pRcv.iItems.add(it);
                return it;
            }
            return null;
        }
    }

    public boolean isBienHinh() {
        return this.getIDNguoiuyet() > -1 || this.isMonster;
    }

    @Override
    public void setNguoiTuyet(final int id) {
        if (this.idNgtuyet > -1) {
            return;
        }
        if ((id == 1 || id == 2) && (this.isActiveTienKhi() || this.isActive8sRongLon() || (this.animalRide != null && Map.r.nextInt(100) < this.animalRide.KHANG_HOA_TUYET))) {
            return;
        }
        if (id == 1) {
            this.timeNguoiTuyet = System.currentTimeMillis() + 60000L;
            this.idNgtuyet = (byte) id;
        } else if (id == 0 || id == 2) {
            this.timeNguoiTuyet = System.currentTimeMillis() + 8000L;
            this.idNgtuyet = (byte) id;
        } else if (id == 3) {
            this.timeNguoiTuyet = System.currentTimeMillis() + 900000L;
            this.idNgtuyet = (byte) id;
            this.calculateAttrib();
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            } catch (final IOException ex) {
            }
        } else if (id == 4) {
            this.timeNguoiTuyet2 = System.currentTimeMillis() + 30000L;
            this.idNgtuyet2 = (byte) id;
        } else if (id == 5) {
            this.timeNguoiTuyet = System.currentTimeMillis() + 120000L;
            this.idNgtuyet = (byte) id;
            this.calculateAttrib();
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            } catch (final IOException ex2) {
            }
        } else if (id == 6) {
            this.timeNguoiTuyet = System.currentTimeMillis() + 5000L;
            this.idNgtuyet = (byte) id;
            this.calculateAttrib();
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            } catch (final IOException ex3) {
            }
        }
        MessageCreator.createMsgCharMonster(this, this);
    }

    public boolean isHieuUngCoLongDuongQua() {

        if (this.isAdmin) {
            return true;
        } else {
            return false;
        }
        /*
        if (this.party == null) {
            return false;
        }
        for (int i = 0; i < this.party.userParty.size(); ++i) {
            final Char p = this.party.userParty.get(i);
            if (p.id != this.id && p.nameHusband_wife.equals(this.charname) && p.map.mapId == this.map.mapId && p.region == this.region) {
                if (this.isDuongQua() && p.isTieuLongNu()) {
                    return true;
                }
                if (this.isTieuLongNu() && p.isDuongQua()) {
                    return true;
                }
            }
        }
        return false;
         */
    }

    public void sendEffectBuff(final LiveActor a, final int ideff, final int milisecond) {
        this.sendMessage(MessageCreator.createMsgNewEffectSkill5Long(ideff, milisecond, a.id, a.cat, true, false));
        this.sendToNearPlayer(MessageCreator.createMsgNewEffectSkill5Long(ideff, milisecond, a.id, a.cat, false, false));
    }

    public int getPCDamNguyetAnh(final int idskill) {
        if (idskill > -1 && System.currentTimeMillis() - this.cooldownNguyetAnh >= 0L && (this.wModel.isTienNu() || this.wModel.isTinhQuan()) && Map.r.nextInt(100) < 10) {
            this.sendMessage(MessageCreator.createMsgNewEffectSkill5Long(EffectBuff.EFF_START_NGUYET_ANH, 1000, this.id, this.cat, false, false));
            this.sendToNearPlayer(MessageCreator.createMsgNewEffectSkill5Long(EffectBuff.EFF_START_NGUYET_ANH, 1000, this.id, this.cat, false, false));
            this.cooldownNguyetAnh = System.currentTimeMillis() + 1000L;
            return 10 + Map.r.nextInt(6);
        }
        return 0;
    }

    private int getPcKhangHoa() {
        final Item[] wItems = this.getArrayCharWearing();
        final int index = getIndexItemWearing(19, 0);
        if (wItems[index] != null && wItems[index].isPhiPhongHuyenVu()) {
            return 300;
        }
        return 0;
    }

    public boolean isDuongQua() {
        return this.wModel.isDuongQua();
    }

    public boolean isTieuLongNu() {
        return this.wModel.isTieuLongNu();
    }

    public boolean isNguoiTuyet() {
        real.plugins.addNPC.addNPC(this);
        return this.idNgtuyet > -1;
    }

    public boolean isCuMeo() {
        return this.idNgtuyet == 6;
    }

    public boolean cannotAttackWhenBienhinh() {
        return this.getIDNguoiuyet() == 2 || this.isCuMeo();
    }

    public int getIDNguoiuyet() {
        return (this.idNgtuyet2 > -1) ? this.idNgtuyet2 : this.idNgtuyet;
    }

    @Override
    public boolean checkTanPhe() {
        return this.hashEffBuff.get(EffectBuff.TAN_PHE) != null;
    }

    @Override
    public boolean checkLamthinh() {
        return this.hashEffBuff.get(EffectBuff.LAM_THINH) != null;
    }

    public boolean checkCamLang() {
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong cc = MapChienTruongMoba.getCharChienTruong(this.charname);
            return cc.isCamLang();
        }
        return false;
    }

    @Override
    public int haveRegentHP() {
        if (this.hp <= 0) {
            return 0;
        }
        int hoihp = this.hoihp;
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        if (petUsing != null) {
            hoihp += petUsing.getHpRegen();
            final int[] a = petUsing.getChuaTri();
            final long b = this.maxhp * a[0] / 100;
            hoihp += (int) (b + petUsing.getPhiHuyet());
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            final int pchoi = c.getBuffHeThuy();
            if (pchoi > 0) {
                hoihp += this.maxhp * 5 / 100;
            } else {
                hoihp += 500;
            }
        }
        // Mat na Ong Dia: +20% hieu qua hoi khi hp duoi 30% toi da
        if (this.isBuffHoiMatNaOngDia()) {
            hoihp += hoihp * 20 / 100;
        }
        return hoihp;
    }

    @Override
    public int haveRegentMP() {
        if (this.hp <= 0) {
            return 0;
        }
        int hoimp = this.hoimp;
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        if (petUsing != null) {
            hoimp += petUsing.getMpRegen();
            hoimp += petUsing.getThuanAnh();
        }
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null) {
                hoimp += 50;
            }
        }
        // Mat na Ong Dia: +20% hieu qua hoi khi hp duoi 30% toi da
        if (this.isBuffHoiMatNaOngDia()) {
            hoimp += hoimp * 20 / 100;
        }
        return hoimp;
    }

    @Override
    public int getPcGiamSatThuong() {
        if (this.hashEffBuff.get(EffectBuff.EFF_SHIELD_1) != null) {
            return 15;
        }
        if (this.hashEffBuff.get(EffectBuff.EFF_SHIELD_2) != null) {
            return 30;
        }
        return super.getPcGiamSatThuong();
    }

    @Override
    public int getBackDam(final int dam) {
        int backDam = dam;
        
        // Nếu đang cưỡi thú và là Rồng Lớn (dung chung dieu kien voi isTraDuaRongLon() - ban cu
        // chi kiem animalRide != null nen ap dung nham cho MOI thu cuoi, khong rieng Rong Lon)
        if (this.isTraDuaRongLon()) {
            backDam = backDam * (100 - PC_TRA_DUA_RONG_LON) / 100;
        }

        if (this.hashEffBuff.get(EffectBuff.EFF_SHIELD_1) != null) {
            return 20 * backDam / 100;
        }
        if (this.hashEffBuff.get(EffectBuff.EFF_SHIELD_2) != null) {
            return 30 * backDam / 100;
        }
        return super.getBackDam(backDam);
    }

    @Override
    public boolean haveBackDam() {
        return this.hashEffBuff.get(EffectBuff.EFF_SHIELD_1) != null || this.hashEffBuff.get(EffectBuff.EFF_SHIELD_2) != null;
    }

    @Override
    public int getPcSuyyeu() {
        final EffectBuff ef = this.hashEffBuff.get(EffectBuff.SUY_YEU);
        if (ef != null) {
            return ef.dam;
        }
        return super.getPcSuyyeu();
    }

    @Override
    public boolean haveSuyYeu() {
        return this.hashEffBuff.get(EffectBuff.SUY_YEU) != null;
    }

    @Override
    public int haveTanPhe() {
        return (Map.r.nextInt(1000) < this.tanphe) ? 1 : 0;
    }

    @Override
    public int haveLamThinh() {
        return (Map.r.nextInt(1000) < this.lamthinh) ? 1 : 0;
    }

    @Override
    public int haveKhangBangLan() {
        return (Map.r.nextInt(1000) < this.khangbanglan) ? 1 : 0;
    }

    @Override
    public int haveKhangSetLan() {
        return (Map.r.nextInt(1000) < this.khangsetlan) ? 1 : 0;
    }

    @Override
    public int haveKhangDocLan() {
        return (Map.r.nextInt(1000) < this.khangdoclan) ? 1 : 0;
    }

    @Override
    public int haveHutHp() {
        int hphut = this.hutmau;
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        if (petUsing != null && Map.r.nextInt(1000) < petUsing.getpcHutHpByHit()) {
            hphut += petUsing.getHpHutByHit();
        }
        return hphut;
    }

    /** Ne tranh sau khi da bo phan Thien nhan cua pet (bi hoa giai ky nang pet). */
    public int haveNeTranhBoQuaPet() {
        int ne = super.haveNeTranh();
        ne += getTkNeTranh() * 10;
        if (ne > 900) {
            ne = 900;
        }
        return ne;
    }

    @Override
    public int haveNeTranh() {
        Pet petUsing = this.petUsing;
        if (this.map != null && this.map.isMapChienTruongMoba()) {
            final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
            if (c != null && c.getNoiLuc() < 5) {
                petUsing = null;
            }
        }
        int ne = (petUsing != null) ? petUsing.getNeTranh() : super.haveNeTranh();
        // Than khi: dong ne tranh luu phan tram thang, con noi goi so sanh voi
        // Map.r.nextInt(1000) nen phai x10 de doi sang phan nghin.
        ne += getTkNeTranh() * 10;
        if (ne > 900) {
            ne = 900; // khong cho ne tuyet doi
        }
        return ne;
    }

    @Override
    public boolean khangBangLan(int giamKhang) 
    {
        return Map.r.nextInt(1000) < this.khangbanglan - giamKhang;
    }

    @Override
    public boolean khangSetLan(int giamKhang) {
        return Map.r.nextInt(1000) < this.khangsetlan - giamKhang;
    }

    @Override
    public boolean khangDocLan(int giamKhang) {
        return Map.r.nextInt(1000) < this.khangdoclan - giamKhang;
    }

    @Override
    public boolean khangLuaLan(int giamKhang) {
        return Map.r.nextInt(1000) < this.khanglualan - giamKhang;
    }

    @Override
    public void resetSpeed() {
        this.divSpeed = 1;
        try {
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
        } catch (final IOException ex) {
        }
    }

    @Override
    public boolean khangBiChay() {
        return this.animalRide != null && this.animalRide.isPhuongHoangBang() && this.animalRide.KHANG_BI_CHAY > 0 && Map.r.nextInt(100) < 95;
    }

    @Override
    public EffectBuff addEffBuff(final int id, final long time, final int type, final int dam) 
    {
        if (this.hashEffBuff.get((short) id) != null) 
        {
            if (!this.hashEffBuff.get((short) id).timeOut()) 
            {
                return null;
            }
            this.AllEffBuff.remove(this.hashEffBuff.get((short) id));
            this.hashEffBuff.remove((short) id);
        }
        final EffectBuff ef = new EffectBuff(id, time);
        this.AllEffBuff.add(ef);
        ef.type = (byte) type;
        ef.damFly = dam;
        this.hashEffBuff.put((short) id, ef);
//        System.out.println("(" + this.charname + ")Add Eff Buff: " +  id + ": " + time);
        return ef;
    }

    @Override
//    public void updateEffectBuff() 
//    {
//        
//        final Vector<EffectBuff> rm = new Vector<EffectBuff>();
//        try {
//            for (int i = 0; i < this.AllEffBuff.size(); ++i) {
//                final EffectBuff ef = this.AllEffBuff.get(i);
//                if (ef.idEff == EffectBuff.UONG_RUOU && System.currentTimeMillis() - this.timeRegentHpMp >= 5000L) {
//                    Map.addXpCharEvent(this, 12500L, false, "char updateEffectBuff1");
//                } else if (ef.idEff == EffectBuff.UONG_RUOU1 && System.currentTimeMillis() - this.timeRegentHpMp >= 5000L) {
//                    Map.addXpCharEvent(this, 83330L, false, "char updateEffectBuff2");
//                } else if (ef.idEff == EffectBuff.UONG_RUOU2 && System.currentTimeMillis() - this.timeRegentHpMp >= 5000L) {
//                    Map.addXpCharEvent(this, 583330L, false, "char updateEffectBuff3");
//                } else if (ef.idEff == EffectBuff.EFF_RI_MAU && System.currentTimeMillis() - ef.timeCount >= 1000L) {
//                    ef.timeCount = System.currentTimeMillis();
//                    this.hp -= 2000;
//                    if (this.hp <= 0) {
//                        this.hp = 0;
//                        this.actorDie();
//                        try {
//                            if (Map.giveCardFail(this)) {
//                                this.map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " giao thẻ thất bại"), this.myCountry);
//                                this.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
//                            }
//                        } catch (final IOException ex) {
//                        }
//                    } else {
//                        try {
//                            MessageCreator.createMsgUseHpMP(this, -2000, 1);
//                        } catch (final Exception ex2) {
//                        }
//                    }
//                } else if (ef.idEff == EffectBuff.TRUNG_DOC && System.currentTimeMillis() - ef.timeCount >= 1000L) {
//                    ef.timeCount = System.currentTimeMillis();
//                    this.hp -= ef.dam;
//                    if (this.hp <= 0) {
//                        this.hp = 0;
//                        this.actorDie();
//                        try {
//                            if (Map.giveCardFail(this)) {
//                                this.map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " giao thẻ thất bại"), this.myCountry);
//                                this.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
//                            }
//                        } catch (final IOException ex3) {
//                        }
//                    } else {
//                        try {
//                            MessageCreator.createMsgUseHpMP(this, -ef.dam, 1);
//                        } catch (final Exception ex4) {
//                        }
//                    }
//                }
//                if (ef.timeOut()) {
//                    if (this.cat == 0) {
//                        if (ef.isTanPhe()) {
//                            this.resetSpeed();
//                        }
//                        if (ef.isEffBuffNoiLuc()) {
//                            this.calculateAttrib();
//                            this.sendInfoChienTruong(Char.ID_NOI_LUC, 0);
//                            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
//                            this.sendToNearPlayer(MessageCreator.createCharInfo(this));
//                        }
//                    }
//                    rm.add(ef);
//                    final Message m = MessageCreator.createMsgRemoveDynamicNewEffect(ef.idEff, 1L, 1, this.id, this.x / 16, this.y / 16, this.cat, 1);
//                    this.sendMessage(m);
//                    this.sendToNearPlayer(m);
//                    if (ef.idEff == EffectBuff.UONG_RUOU || ef.idEff == EffectBuff.UONG_RUOU1 || ef.idEff == EffectBuff.UONG_RUOU2) {
//                        this.idRuouUong = -1;
//                    }
//                }
//            }
//            for (int i = 0; i < rm.size(); ++i) {
//                final EffectBuff ef = rm.get(i);
//                this.AllEffBuff.remove(ef);
//                this.hashEffBuff.remove(ef.idEff);
//            }
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    // thêm sửa lan test 
    public void updateEffectBuff() {
    try {
        final Iterator<EffectBuff> it = this.AllEffBuff.iterator();

        while (it.hasNext()) {
            final EffectBuff ef = it.next();

            // Xử lý hiệu ứng có ticking liên tục như máu/độc
            if (ef.idEff == EffectBuff.EFF_RI_MAU || ef.idEff == EffectBuff.TRUNG_DOC) {
            if (System.currentTimeMillis() - ef.timeCount >= 1000L) {
                ef.timeCount = System.currentTimeMillis();
                int damage = (ef.idEff == EffectBuff.EFF_RI_MAU) ? 2000 : ef.dam;
                this.hp -= damage;

                    if (this.hp <= 0) {
                        this.hp = 0;
                        this.actorDie();
                        try {
                            if (Map.giveCardFail(this)) {
                                this.map.doSend2AllChar(
                                    MessageCreator.createServerAlertAutoOffMessage(this.charname + " giao thẻ thất bại"),
                                    this.myCountry
                                );
                                this.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
                            }
                        } catch (IOException ignored) {}
                    } else {
                        try {
                            MessageCreator.createMsgUseHpMP(this, -damage, 1);
                        } catch (Exception ignored) {}
                    }
                }
            }

            // Xử lý EXP từ hiệu ứng rượu
            if ((ef.idEff == EffectBuff.UONG_RUOU || ef.idEff == EffectBuff.UONG_RUOU1 || ef.idEff == EffectBuff.UONG_RUOU2)
                    && System.currentTimeMillis() - this.timeRegentHpMp >= 5000L) {
                                long expGain = 0;
                if (ef.idEff == EffectBuff.UONG_RUOU) {
                    expGain = 12500L;
                } else if (ef.idEff == EffectBuff.UONG_RUOU1) {
                    expGain = 83330L;
                } else if (ef.idEff == EffectBuff.UONG_RUOU2) {
                    expGain = 583330L;
                }
                Map.addXpCharEvent(this, expGain, false, "char updateEffectBuff");

            }

            // Xử lý hết hiệu ứng
            if (ef.timeOut()) {
                // Gửi xoá hiệu ứng về client
                Message m = MessageCreator.createMsgRemoveDynamicNewEffect(
                    ef.idEff, 1L, 1, this.id, this.x / 16, this.y / 16, this.cat, 1);
                this.sendMessage(m);
                this.sendToNearPlayer(m);

                // Xử lý riêng 1 số loại hiệu ứng
                if (this.cat == 0) {
                    if (ef.isTanPhe()) this.resetSpeed();
                    if (ef.isEffBuffNoiLuc()) {
                        this.calculateAttrib();
                        this.sendInfoChienTruong(Char.ID_NOI_LUC, 0);
                        this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                        this.sendToNearPlayer(MessageCreator.createCharInfo(this));
                    }
                }

                // Nếu là rượu → reset idRuou
                if (ef.idEff == EffectBuff.UONG_RUOU || ef.idEff == EffectBuff.UONG_RUOU1 || ef.idEff == EffectBuff.UONG_RUOU2) {
                    this.idRuouUong = -1;
                }

                // Xoá hiệu ứng
                it.remove();
                this.hashEffBuff.remove(ef.idEff);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public EffectBuff getEffectUongRuou() {
        for (int i = 0; i < this.AllEffBuff.size(); ++i) {
            final EffectBuff ef = this.AllEffBuff.get(i);
            if (!ef.timeOut()) {
                return ef;
            }
        }
        return null;
    }

    public EffectBuff getEffectDongLua() {
        for (int i = 0; i < this.ALL_BUFF_INMAP.size(); ++i) {
            final EffectBuff ef = this.ALL_BUFF_INMAP.get(i);
            if (!ef.timeOut()) {
                return ef;
            }
        }
        return null;
    }

    public boolean canHSChiemThanh() {
        return System.currentTimeMillis() - this.timeWaitComeHome > 1500L;
    }

    @Override
    public int getValueLua(final int type) {
        int updef = 0;
        final Vector<EffectBuff> rm = new Vector<EffectBuff>();
        for (int i = 0; i < this.ALL_BUFF_INMAP.size(); ++i) {
            final EffectBuff ef = this.ALL_BUFF_INMAP.get(i);
            if (type == 1 && ef.idEff == EffectBuff.LUA_VUA && ef.mapID == this.mapID && ef.idRegion == this.region && ef.idCountry == this.myCountry && Map.inRangeActor(ef, this, 120)) {
                updef = 5;
            } else if (type == 0 && ef.idEff == EffectBuff.LUA_NHO && ef.mapID == this.mapID && ef.idRegion == this.region && ef.idCountry == this.myCountry && Map.inRangeActor(ef, this, 120)) {
                updef = 2;
            } else if (type == 2 && ef.idEff == EffectBuff.LUA_TO && ef.mapID == this.mapID && ef.idRegion == this.region && ef.idCountry == this.myCountry && Map.inRangeActor(ef, this, 120)) {
                updef = 10;
            }
            if (ef.timeOut()) {
                rm.add(ef);
                if (ef.idEff == EffectBuff.LUA_NHO || ef.idEff == EffectBuff.LUA_VUA || ef.idEff == EffectBuff.LUA_TO) {
                    this.idCuiDot = -1;
                }
            }
            if (updef > 0) {
                break;
            }
        }
        for (int i = 0; i < rm.size(); ++i) {
            this.ALL_BUFF_INMAP.remove(rm.get(i));
        }
        return updef;
    }

    public void updateLuLan() {
        if (!this.isHoangLoan()) {
            return;
        }
        final int xx0 = this.x;
        final int yy0 = this.y;
        int xL = xx0 - 72;
        final int xR = xx0 + 72;
        final int yU = yy0 + 72;
        int yD = yy0 - 72;
        final int xNew = Map.ranDom(xL, xR);
        final int yNew = Map.ranDom(yD, yU);
        if (xL < 16) {
            xL = 16;
        }
        if (yD < 16) {
            yD = 16;
        }
        if (!this.map.canMove(xNew, yNew)) {
            final Message m = new Message(4);
            this.writeActorPos(m, this);
            this.sendMessage(m);
            return;
        }
        final byte step = 2;
        final int totalPositionX = (xNew - this.x) / step;
        final int totalPositionY = (yNew - this.y) / step;
        boolean blockX = false;
        boolean blockY = false;
        int dau = (totalPositionX >= 0) ? 1 : -1;
        for (int i = 0; i < Map.abs(totalPositionX); ++i) {
            final int tem = this.x + dau * i * step;
            if (!this.map.canMove(tem, this.y)) {
                blockX = true;
                break;
            }
        }
        if (!blockX) {
            dau = ((totalPositionY >= 0) ? 1 : -1);
            for (int i = 0; i < Map.abs(totalPositionY); ++i) {
                final int tem = this.x + dau * i * step;
                if (!this.map.canMove(this.y, tem)) {
                    blockY = true;
                    break;
                }
            }
        }
        if (blockX || blockY) {
            final Message j = new Message(4);
            this.writeActorPos(j, this);
            this.sendMessage(j);
            return;
        }
        this.x = xNew;
        this.y = yNew;
        final Message j = new Message(4);
        this.writeActorPos(j, this);
        this.sendMessage(j);
        this.sendInfoMove2Near();
        try {
            if (Map.giveCardFail(this)) {
                this.map.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " giao thẻ thất bại"), this.myCountry);
                this.map.sendAllPlayer(MessageCreator.createMsgStartGetTown(this.inCountry), this.inCountry);
            }
        } catch (final Exception ex) {
        }
    }

    @Override
    // Than method (613 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public void update() {
        CharVongLap.update(this);
    }

    public void doAddGifThungGoNuiChauBau(final int lv) {
        final CharInfo cinfo = MapChauBau.all_char_nui_kho_bau.get(this.charname);
        String info = "Nhận được ";
        int sl = 1;
        int idGem = 0;
        final int idLkd = GemTemplate.LKD_30;
        final int[] in = {0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        int index = in[Map.r.nextInt(in.length)];
        final int pcBonus = Map.r.nextInt(100);
        if (lv < 60) {
            idGem = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2) + 1][Map.r.nextInt(5)];
            if (pcBonus < 10) {
                if (index == 0) {
                    sl = Map.r.nextInt(2) + 1;
                    info = String.valueOf(info) + sl + " lkd 30% " + (cinfo.isLockGift ? "khoá" : "không khoá.");
                    this.doAddGemItem(idLkd, sl, cinfo.isLockGift);
                } else if (index == 1) {
                    final int time = Map.r.nextInt(2) + 1;
                    final Item it = Map.doCreateVuKhiThoiTrang(this, time * 24 * 60, Map.ID_VU_KHI_THOI_TRANG[this.charClass]);
                    info = String.valueOf(info) + it.getName() + " thời hạn " + time + " ngày.";
                } else if (index == 2) {
                    final int[][] iditem = {{725, 723}, {726, 724}};
                    final Item it = Map.doBuyModelClothe(this, this.isCharNam() ? iditem[0][Map.r.nextInt(2)] : iditem[1][Map.r.nextInt(2)], 1, 1);
                    info = String.valueOf(info) + " " + it.getName() + " thời hạn " + 1 + " ngày.";
                }
            }
        } else if (lv < 80) {
            idGem = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2) + 2][Map.r.nextInt(5)];
            if (pcBonus < 10) {
                if (index == 0) {
                    sl = Map.r.nextInt(2) + 1;
                    info = String.valueOf(info) + sl + " lkd 35% " + (cinfo.isLockGift ? "khoá" : "không khoá.");
                    this.doAddGemItem(GemTemplate.LKD_35, sl, cinfo.isLockGift);
                } else if (index == 1) {
                    index = Map.r.nextInt(3);
                    sl = Map.r.nextInt(2) + 1;
                    if (index == 0) {
                        this.createNguaXuong(sl, false);
                        info = String.valueOf(info) + "Bạch cốt mã thời hạn " + sl + " ngày";
                    } else if (index == 1) {
                        this.createHeo(11L, false);
                        info = String.valueOf(info) + "Đương khang thời hạn " + sl + " ngày";
                    } else if (index == 2) {
                        this.createLan(1440L, false, 95);
                        info = String.valueOf(info) + "Lân sư tử thời hạn " + sl + " ngày";
                    }
                } else if (index == 2) {
                    final int[][] iditem = {{725, 723}, {726, 724}};
                    final Item it = Map.doBuyModelClothe(this, this.isCharNam() ? iditem[0][Map.r.nextInt(2)] : iditem[1][Map.r.nextInt(2)], 1, 1);
                    info = String.valueOf(info) + " " + it.getName() + " thời hạn " + 1 + " ngày.";
                }
            }
        } else {
            idGem = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2) + 3][Map.r.nextInt(5)];
            if (pcBonus < 10) {
                if (index == 0) {
                    sl = Map.r.nextInt(2) + 1;
                    info = String.valueOf(info) + sl + " lkd 40% " + (cinfo.isLockGift ? "khoá" : "không khoá.");
                    this.doAddGemItem(GemTemplate.LKD_40, sl, cinfo.isLockGift);
                } else if (index == 1) {
                    final int[] idchoi = {617, 619, 729, 618};
                    final int time2 = Map.r.nextInt(2) + 1;
                    final boolean isBienHinh = Map.r.nextInt(2) == 1;
                    final Item it2 = createChoi(idchoi[Map.r.nextInt(idchoi.length)], this, time2 * 24 * 60, 0, isBienHinh);
                    info = String.valueOf(info) + " " + it2.getName() + " thời hạn " + time2 + " ngày. " + (isBienHinh ? "có hoá tuyết" : "không có hoá tuyết");
                } else if (index == 2) {
                    final int[][] iditem = {{725, 723}, {726, 724}};
                    final Item it = Map.doBuyModelClothe(this, this.isCharNam() ? iditem[0][Map.r.nextInt(2)] : iditem[1][Map.r.nextInt(2)], 1, 1);
                    info = String.valueOf(info) + " " + it.getName() + " thời hạn " + 1 + " ngày.";
                }
            }
        }
        sl = Map.r.nextInt(3) + 1;
        this.doAddGemItem(idGem, sl, cinfo.isLockGift);
        info = String.valueOf(info) + " " + sl + " " + Map.gemTemplate[idGem].name;
        Database.instance.saveOrtherLog("", this.charname, info, "quakhobau");
        this.sendMessage(MessageCreator.createMsgChat(this.id, info));
        this.sendMessage(MessageCreator.createCharGemItem(this));
    }

    public boolean kickOutTown() {
        if (this.isAdmin) {
            return false;
        }
        if (this.map.mapId == Map.idMapTown) {
            if ((Map.getTown[this.inCountry] && this.myCountry != this.inCountry) || (Map.getTown[this.inCountry] && this.lvDetail.lv < 70)) {
                return true;
            }
            if (Map.getTown[this.inCountry] && !MapTown.congthanh.get(this.inCountry).isDead && this.y < 1200) {
                return true;
            }
        }
        return false;
    }

    public void receiveGiftDay() {
    }

    public static boolean canSendRaoVat() {
        if (!Map.openLog) {
            return false;
        }
        if (Char.infoAdv2.size() <= 0) {
            return false;
        }
        final long time = System.currentTimeMillis() - Char.timeSendRaoVat;
        if (time >= 0L) {
            if (time >= 20000L) {
                Char.timeSendRaoVat = System.currentTimeMillis() + 900000L;
            }
            return false;
        }
        return true;
    }

    public Vector<Char> getAllPlayerMap() {
        Vector<Char> p = new Vector<Char>();
        p = this.map.getAllPlayer((this.mapID == 225 || this.mapID == 226) ? this.idWedding : ((this.mapID == 204 || this.mapID == 205) ? this.idArena : this.inCountry), (this.mapID == 206) ? this.idClan : this.region);
        return p;
    }

    public void doAddGiftEventOutGame() {
    }

    public void setMocNap() {
        if (this.tichluy > 0 && this.luongNap == 0) {
            this.luongNap = this.tichluy / 2;
        }
    }

    public static Item createtemCoat_chonkhang(int idTemplate, Char p, int mavat, boolean ismax, int time, int khang) 
    {
        Item newItem = new Item();
        ItemTemplates item = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(Integer.valueOf(idTemplate));
        for (int i = 0; i < 10; i++) 
        {
            if (item.atb[i] > 0) {
                newItem.atb[i] = (short) (item.atb[i] + item.atb[i] / 4);
            }
        }
        newItem.setTemplate(idTemplate, p.charClass, p.charClass, idTemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = (newItem.getTemplate()).level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        if (time > 0) {
            newItem.minuteExist = time * 24 * 60;
            newItem.timeLoan = System.currentTimeMillis();
        }

        /*
        5 - Kháng hoả
        1 - Kháng thủy
        2 - Kháng mộc
        3 - Kháng kim
        4 - Kháng thổ
         */
        newItem.createAttributeItemCoat(ismax, (byte) mavat, khang);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        System.out.println(String.valueOf(idTemplate) + "_" + ((mavat == 0) ? "maphap" : "vat ly ") + newItem.getInfoSave());
        return newItem;
    }

    public static Item createtemCoat(int idTemplate, Char p, int mavat, boolean ismax, int time) {
        Item newItem = new Item();
        ItemTemplates item = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(Integer.valueOf(idTemplate));
        for (int i = 0; i < 10; i++) {
            if (item.atb[i] > 0) {
                newItem.atb[i] = (short) (item.atb[i] + item.atb[i] / 4);
            }
        }
        newItem.setTemplate(idTemplate, p.charClass, p.charClass, idTemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = (newItem.getTemplate()).level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.minuteExist = time;
        if (time > 0) {
            newItem.timeLoan = System.currentTimeMillis();
        }
        newItem.createAttributeItemCoat(ismax, (byte) mavat, -1);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        System.out.println(String.valueOf(idTemplate) + "_" + ((mavat == 0) ? "maphap" : "vat ly ") + newItem.getInfoSave());
        return newItem;
    }

    // TODO BẬT TẮT QUA TICH LUY
    // Da TAT moc nap: doAddQuaTichLuy() duoc goi tu SelectCharHandler moi lan
    // chon nhan vat, tu dong phat qua theo diemNapVip / luongNap. Doi lai true
    // la bat lai toan bo, khong can sua cho nao khac.
    public static boolean TYPE_QUA_TICH_LUY = false;

    // Than method (255 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public void doAddQuaTichLuy(int QuaTuan) {
        CharSuKien.doAddQuaTichLuy(this, QuaTuan);
    }

    /**
     * Tao Than Khi (890 Quat ba tieu / 891 Gay nhu y / 892 Luong nhan dao)
     * dung chuan nhu NPC Tho Ren Than Bi.
     * Prefix TK/TKMAX co san goi createThanKhi() — ham do KHONG set colorName,
     * so dong an, nguyen lieu nang cap hay dong thuoc tinh, nen ra mon trang tron
     * khong nang cap duoc. Ham nay lam day du.
     *
     * @param mauSac 0 trang, 3 xanh duong, 2 do, 1 xanh la, 4 tim (tim = nhieu dong an nhat)
     */
    public static Item createThanKhiFull(final Char p, final int idtemplate, final int mauSac, final int lock) {
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return null;
        }
        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(p.charClass).get(idtemplate);
        if (template == null) {
            return null;
        }
        final Item newItem = new Item(template, false, p.charClass, p.charClass, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = 50;
        newItem.identify = p.charDBID;
        newItem.clazz = (byte) -1;
        newItem.mDurable = 0;
        newItem.charSeal = p.charname;
        newItem.lock = (byte) lock;
        newItem.colorName = (byte) mauSac;

        int hiddenCount;
        switch (mauSac) {
            case 3:  hiddenCount = 2 + Map.random(3); break; // 2..4
            case 2:  hiddenCount = 3 + Map.random(4); break; // 3..6
            case 1:  hiddenCount = 4 + Map.random(5); break; // 4..8
            case 4:  hiddenCount = 5 + Map.random(5); break; // 5..9
            default: hiddenCount = 0;                 break;
        }
        if (hiddenCount > 9) {
            hiddenCount = 9;
        }
        newItem.otherAtt[168 - 61] = (short) hiddenCount;

        // Nguyen lieu nang cap theo quy tac 2-2-1 giong ThoRenThanBi.CreateThanKhi
        final java.util.List<Integer> pickedIdx = new java.util.ArrayList<Integer>(5);
        while (pickedIdx.size() < 2) {
            final int idx = Map.random(5);
            if (!pickedIdx.contains(idx)) {
                pickedIdx.add(idx);
            }
        }
        while (pickedIdx.size() < 4) {
            final int idx = 5 + Map.random(5);
            if (!pickedIdx.contains(idx)) {
                pickedIdx.add(idx);
            }
        }
        pickedIdx.add(10);
        for (final int idx : pickedIdx) {
            final short optId = ThanKhi.ThoRenThanBi.item_upgrade_material_ids[idx];
            newItem.otherAtt[optId - 61] = (short) ThanKhi.ThoRenThanBi.amountMaterial[idx];
        }

        final int attrIdx = Map.random(ThanKhi.ThoRenThanBi.item_attribute_ids.length);
        final short attrOptId = ThanKhi.ThoRenThanBi.item_attribute_ids[attrIdx];
        newItem.otherAtt[attrOptId - 61] = (short) (100 + Map.random(81)); // 100..180

        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        Database.instance.saveItem(newItem);
        return newItem;
    }

    /**
     * Tao than khi da nang toi da: +10, mo san cac dong an va dat tat ca ve
     * gia tri bac cao nhat (cot 9 cua HIDDEN_STAT_TABLE) — dung bang gia tri
     * ma UpgradeThanKhi tinh ra khi len +10, nen hien thi va tinh chien dau
     * deu khop voi mon nang tay.
     *
     * soDongAn : so dong an mo san, 1..29. Nang binh thuong toi da chi duoc 9
     *            dong (tran cua CreateThanKhi), dat lon hon la vuot muc choi.
     * he       : 0..4 = kim/moc/thuy/hoa/tho, -1 = ngau nhien.
     *
     * Khong gan nguyen lieu nang cap (option 169..179) vi mon +10 da bi
     * UpgradeThanKhi chan khong nang tiep duoc nua.
     */
    public static Item createThanKhiMax(final Char p, final int idtemplate, final int soDongAn, final int he, final int lock) {
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return null;
        }
        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(p.charClass).get(idtemplate);
        if (template == null) {
            return null;
        }
        final Item newItem = new Item(template, false, p.charClass, p.charClass, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = 50;
        newItem.identify = p.charDBID;
        newItem.clazz = (byte) -1;
        newItem.mDurable = 0;
        newItem.charSeal = p.charname;
        newItem.lock = (byte) lock;
        newItem.colorName = 4;    // tim - bac cao nhat
        newItem.plus = (byte) 10;

        final short[] statIds = ThanKhi.ThoRenThanBi.HIDDEN_STAT_IDS;
        int nLine = soDongAn;
        if (nLine < 1) {
            nLine = 1;
        }
        if (nLine > statIds.length) {
            nLine = statIds.length;
        }

        int opened = 0;
        for (int si = 0; si < statIds.length && opened < nLine; ++si) {
            final short statOptId = statIds[si];
            if (statOptId < 0) {
                continue;
            }
            final int store = statOptId - 61;
            if (store < 0 || store >= newItem.otherAtt.length) {
                continue;
            }
            newItem.otherAtt[store] = (short) Math.round(ThanKhi.ThoRenThanBi.HIDDEN_STAT_TABLE[si][9]);
            ++opened;
        }
        newItem.otherAtt[168 - 61] = (short) opened;

        // Dong sat thuong he: tao ra toi da 180, moi lan nang thanh cong +4,
        // 10 lan nang => tran that su la 220.
        final short[] attrIds = ThanKhi.ThoRenThanBi.item_attribute_ids;
        final int heIdx = (he >= 0 && he < attrIds.length) ? he : Map.r.nextInt(attrIds.length);
        newItem.otherAtt[attrIds[heIdx] - 61] = (short) 220;

        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        Database.instance.saveItem(newItem);
        return newItem;
    }

    // Thu tu "chuoi kich" ma checkKick5AttAnimal() doc qua groupKickAnimal:
    // 14(Giap) kich 18(Yen cuong) kich 17(Ban dap) kich 15(Ho uyen) kich 16(Non)
    // kich lai 14 — dung 5 mat xich, khep vong. Id template moi cap: Giap=521+n,
    // Yen cuong=533+n, Ban dap=545+n, Ho uyen=557+n, Non=509+n (n = (level-30)/5).
    private static final byte[] CHAIN_TYPE_TRANG_BI_THU = {14, 18, 17, 15, 16};
    private static final int[] CHAIN_BASE_ID_TRANG_BI_THU = {521, 533, 545, 557, 509};

    /**
     * Tao du 1 bo 5 mon trang bi thu (Giap/Ho uyen/Non/Ban dap/Yen cuong) cung
     * mot cap level, gan heItem theo dung thu tu "chuoi kich" cua
     * checkKick5AttAnimal() (Char.java, groupKickAnimal) — moi mon duoc gan
     * heItem = he mon truoc + 1, nen CA 5 MON deu kich duoc ngu hanh cua mon
     * truoc trong chuoi, khong mon nao bi bo sot ("full he" giftcode yeu cau).
     */
    public static void createTrangBiThuFullHe(final Char p, final int level, final int minute) {
        if (level < 30 || level > 85 || (level - 30) % 5 != 0) {
            return; // level khong hop le (phai 30,35,...,85) - im lang nhu VKTBLV
        }
        final int n = (level - 30) / 5;
        final int base = Map.r.nextInt(5);
        // Ca 5 mon PHAI CUNG mot magic_physic (0=Ma phap, 1=Vat ly - xem Item.java,
        // Map.java:8768). Hai ly do:
        // 1) createItemColorAnimal() doc magic_physic de chia atb[1]/atb[6] (Vat ly/Ma
        //    phap) cho type 14/16/18 - de mac dinh (=2, khong phai 0/1) thi nhanh do bi
        //    BO QUA HOAN TOAN, ca hai chi so cung giu nguyen ban goc chua ai chia 10 ->
        //    mon do hien "vua day Vat ly vua day Ma phap" (dung bug nguoi dung bao).
        // 2) checkKick5AttAnimal() chi kich duoc ngu hanh giua 2 mon CUNG magic_physic -
        //    khac nhau la chuoi kich khong bao gio chay.
        // Random MOT LAN cho ca bo, giong dung cach do tu nhien roi (Map.dropItemAnimalByLvChar
        // r.nextInt(2)) nhung ap dung CHUNG cho 5 mon thay vi tung mon rieng le.
        final byte magicPhysic = (byte) Map.r.nextInt(2);
        for (int i = 0; i < CHAIN_TYPE_TRANG_BI_THU.length; i++) {
            if (p.isFullInventory()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
                return;
            }
            final int idtemplate = CHAIN_BASE_ID_TRANG_BI_THU[i] + n;
            final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(idtemplate);
            if (template == null) {
                continue;
            }
            final Item newItem = new Item(template, false, 0, 0, idtemplate);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.identify = p.charDBID;
            newItem.level = template.level;
            newItem.clazz = 0;
            newItem.heItem = (byte) ((base + i) % 5);
            newItem.magic_physic = magicPhysic;
            // Trang bi thu tu giftcode phai la Hoan My (colorName=1) - Nhat pham
            // (hangItem=1), giong duong duy nhat khac dang tao ra hang nay
            // (ItemChangeColor.createItemColorAnimal, nang mau tu do roi) - khong
            // chi doi ten hien thi ma con tinh lai dung atb/otherAtt theo dung cong
            // thuc cua tier do. Goi SAU khi da gan heItem/magic_physic de giu dung
            // ca hai (ham nay chi tu random heItem khi dang -1, va can magic_physic
            // dung 0/1 tu truoc de chia dung atb[1]/atb[6]).
            data.ItemChangeColor.createItemColorAnimal(newItem, 1, 1);
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            newItem.dateCreate = getDayTime(0L);
            p.iItems.add(newItem);
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
    }

    /**
     * Tao do thoi trang cho BAT KY id nao.
     * Viet de thay doDropModelClothe() — ham do lay template tu itemTemplates.get(0),
     * ep clazz = 0 va goi createAttGiap() (chi so cua GIAP, khong phai thoi trang),
     * nen canh/choi ra khong co chi so va nhan vat khac class 0 mac khong len.
     */
    public static Item createThoiTrang(final Char p, final int idtemplate, final int nday, final int lock) {
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return null;
        }
        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idtemplate);
        if (template == null) {
            return null;
        }
        final Item newItem = new Item(template, false, -1, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.identify = p.charDBID;
        newItem.level = (newItem.getTemplate()).level;
        newItem.clazz = -1; // do thoi trang dung chung cho moi class
        newItem.lock = (byte) lock;
        if (newItem.isCanh()) {
            // canh 898-902 co atb rong trong data_item, chi so nam o createOptionCanh
            if (idtemplate == 898) {
                newItem.createOptionCanh(0, 5);
            } else if (idtemplate == 899) {
                newItem.createOptionCanh(1, 5);
            } else if (idtemplate == 900) {
                newItem.createOptionCanh(2, 10);
            } else if (idtemplate == 901) {
                newItem.createOptionCanh(3, 30);
            } else if (idtemplate == 902) {
                newItem.createOptionCanh(4, 50);
            }
        } else if (template.type == 13) {
            newItem.createAttChoiVinhVien(); // choi bay
        } else if (idtemplate >= 745 && idtemplate <= 749) {
            // Mat na 745-749: atb trong data_item deu bang 0, chi so nam o
            // createOptionMatNa. Chi so option/value lay dung theo cach prefix
            // MATNA goc lam: index = idtemplate - 745.
            final int[] valuemn = {5, 5, 10, 30, 50};
            final int idx = idtemplate - 745;
            newItem.createOptionMatNa(idx, valuemn[idx]);
        } else if (idtemplate == 837) {
            newItem.createOptionMatNa(10, 5); // mat na tho
        } else if (idtemplate == 838) {
            newItem.createOptionMatNa(11, 5); // mat na cao
        } else {
            newItem.createAttributeItemModel(false);
        }
        if (nday > 0) {
            newItem.minuteExist = nday * 24 * 60;
            newItem.timeLoan = System.currentTimeMillis();
        }
        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        Database.instance.saveItem(newItem);
        return newItem;
    }

    public static void doDropModelClothe(Char player, int IDItem, int nday) {
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return;
        }
        ItemTemplates template = null;
        Item newItem = null;
        template = Map.itemTemplates.get(0).get(IDItem);
        newItem = new Item(template, false, -1, 0, IDItem);
        if (newItem != null) {
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            player.iItems.add(newItem);
            newItem.clazz = 0;
            newItem.createAttGiap();
            newItem.identify = player.charDBID;
            if (nday > 0) {
                newItem.minuteExist = nday * 24 * 60;
                newItem.timeLoan = System.currentTimeMillis();
            }
                Logger2.DebugLogic("NAME ITEM        :::: " + newItem.getTemplate().name);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            // Database.instance.saveLogBuyItemShop(player.charname, (newItem.getTemplate()).name, 0, item.getPrice(isSale), String.valueOf(player.getxu()) + "_" + player.getLuong());
            Database.instance.saveItem(newItem);
        }
    }

    // TODO Nhận quà từ ngude_giftcode
    public static boolean NhanGiftCode = true;

    // Than method (187 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public void doAddGiftEventOffline() {
        CharSuKien.doAddGiftEventOffline(this);
    }

    // Than method (130 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    void updateNearChar() {
        CharCapNhat.updateNearChar(this);
    }

    public void updateFarm() {
        boolean growUp = false;
        for (int i = 0; i < this.seedsItem.size(); ++i) {
            final SeedItem s = this.seedsItem.get(i);
            if (s.idTemplate > -1) {
                final int level = s.lvTree;
                s.setLevelByTime();
                if (level < s.lvTree) {
                    growUp = true;
                }
                if (s.isDie()) {
                    s.idTemplate = -1;
                    s.lvTree = 0;
                    s.timeLive = 0L;
                    growUp = true;
                }
            }
        }
        if (growUp && this.mapID == Map.mapIDFarm) {
            this.map.doSendFarm(this);
        }
    }

    public void sendInfoMove2Player(final Char p) {
        if (!this.isAdmin) {
            final Message m = new Message(4);
            this.writeActorPos(m, this);
            p.sendMessage(m);
        }
    }

    public void sendInfoMove2Near() {
        if (!this.isAdmin) {
            final Message m = new Message(4);
            this.writeActorPos(m, this);
            for (int size = this.nearChars.size(), i = 0; i < size; ++i) {
                try {
                    final Char p = this.map.getPlayerByID(this.nearChars.elementAt(i));
                    if (p != null && p.id != this.id && p.isBot == -1 && this.near(p, this.rangeAddMonster[0]) && p.map == this.map && p.region == this.region) {
                        p.sendMessage(m);
                    }
                } catch (final Exception ex) {
                }
            }
        }
    }

    public void setSizeScreen(short w, final short h) {
        if (w < 0) {
            w = 320;
        }
        if (w < 320) {
            w = 220;
        }
        this.rangeAddMonster[0] = (short) (w / 2 + 50);
        this.rangeRemoveMonster[0] = (short) (w / 2 + 100);
    }

    public boolean inDataRange(final Actor p1, final Actor p2) {
        if (this.wScreen <= 240) {
            return Math.abs(p1.x - p2.x) < this.wScreen + (this.wScreen >> 1) && Math.abs(p1.y - p2.y) < this.hScreen + (this.hScreen >> 1);
        }
        return Math.abs(p1.x - p2.x) < this.wScreen && Math.abs(p1.y - p2.y) < this.hScreen;
    }

    public static boolean givingCard(final int country, final int id) {
        if (!Map.getTown[country]) {
            return false;
        }
        final Vector<NpcReceiveCard> npcReceiveCard = Map.npcReceiveCard.get(country);
        for (int pos = 0; pos < npcReceiveCard.size(); ++pos) {
            final NpcReceiveCard npc = npcReceiveCard.get(pos);
            if (npc.charGive != null && npc.charGive.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isBatGioi() {
        return this.isCaiTrang == 1 ;
    }

    public boolean isSaTang() {
        return this.isCaiTrang == 2 ;
    }

    public int getLevelCaiTrang() {
        int level = 0;
        int targetId = 0;
        if (this.isCaiTrang == 1) {
            targetId = 903;
        } else if (this.isCaiTrang == 2) {
            targetId = 904;
        }
        // Lọc items từ rương người chơi
        final int size = this.iItems.size();
        for (int i = 0; i < size; i++) {
            Item item = this.iItems.elementAt(i);
            if (item == null) continue;
            
            // Kiểm tra tempateID thay vì id
            if (item.tempateID == targetId) {
                level += item.plus;
            }
        }
        return level;
    }

    public boolean checkItemTheMuaBan() {
        // Lọc items từ rương người chơi
        final int size = this.iItems.size();
        for (int i = 0; i < size; i++) {
            Item item = this.iItems.elementAt(i);
            if (item == null) continue;
            
            // Kiểm tra id 675 (THE_MUA_BAN)
            if (item.tempateID == Item.THE_MUA_BAN) {
                // Kiểm tra thời gian hết hạn
                if (item.isHetHan()) {
                    return false; // Item đã hết hạn
                }
                return true; // Item còn hạn
            }
        }
        return false; // Không tìm thấy item
    }

    // Than method (127 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public int getIdHeadDynamic() {
        return CharVongLap.getIdHeadDynamic(this);
    }

    public int getIdBodyDynamic() {
        if(this.isBatGioi() && this.isBatCaiTrang()){
            if(this.getLevelCaiTrang() >= 13){
            return 8700 + 250;
           } else {
            return 8700 + 239;
           }
        }
        if(this.isSaTang() && this.isBatCaiTrang()){
            if(this.getLevelCaiTrang() >= 13){
            return 8700 + 253;
           } else {
            return 8700 + 242;
           }
        }
        if (this.wModel.isCaiTaoSonTinh()) {
            return 8700 + 282;
        }
        if (this.wModel.isCaiTaoMiNuong()) {
            return 8700 + 279;
        }
        if (this.wModel.isGiapRong()) {
            return 8700 + 230;
        }
        if (this.wModel.isMeoXanh()) {
            return 8700 + 217;
        }
        if (this.wModel.isMeoQuyToc()) {
            return 8700 + 214;
        }

        if (this.wModel.isTienNu()) {
            return EffectBuff.EFF_AO_TIEN_NU;
        }
        if (this.wModel.isTinhQuan()) {
            return EffectBuff.EFF_AO_TINH_QUAN;
        }
        if (this.wModel.isAoDaiNam()) {
            return EffectBuff.EFF_AO_DAI_NAM;
        }
        if (this.wModel.isAoDaiNu()) {
            return EffectBuff.EFF_AO_DAI_NU;
        }
        
         // TODO THÊM ÁO THỜI TRANG (4) MAIN
        if (this.wModel.isNgheThuong()) {
            return 8700 + 176;
        }
        if (this.wModel.isNgocNu()) {
            return 8700 + 179;
        }       
        if (this.wModel.isXichCuoc()) {
            return 8700 + 182;
        }       
        if (this.wModel.isKimDong()) {
            return 8700 + 184;
        }       
        if (this.wModel.isLoiCong()) {
            return 8700 + 173;
        }
        if (this.wModel.isLacLongQuan()) {
            return 8700 + 120;
        }
        if (this.wModel.isAuCo()) {
            return 8700 + 117;
        }
        // Ba Thien Ho: than ao. Nam xanh-vang (195), nu do-vang (192).
        if (this.wModel.isBaThienHoNam()) {
            return 8700 + 195;
        }
        if (this.wModel.isBaThienHoNu()) {
            return 8700 + 192;
        }
        // Bon bo bong da - than ao. Xem bang o getIdHeadDynamic().
        if (this.wModel.isBongDaNamAoDo()) {
            return 8700 + 154;
        }
        if (this.wModel.isBongDaNuAoTrang()) {
            return 8700 + 157;
        }
        if (this.wModel.isBongDaNamAoCam()) {
            return 8700 + 161;
        }
        if (this.wModel.isBongDaNuAoSoc()) {
            return 8700 + 164;
        }
         if (this.wModel.isAonoelNam2024()) {
            return 8700 + 263;
        }  
         if (this.wModel.isAonoelNu2024()) {
            return 8700 + 260;
        } 
        
        return -1;
    }

    public int getIdLegDynamic() {
        if(this.isBatGioi() && this.isBatCaiTrang()){
            if(this.getLevelCaiTrang() >= 13){
            return 8700 + 248;
           } else {
            return 8700 + 240;
           }
           }
        if(this.isSaTang() && this.isBatCaiTrang()){
            if(this.getLevelCaiTrang() >= 13){
            return 8700 + 251;
           } else {
            return 8700 + 243;
           }
           }
        if (this.wModel.isCaiTaoSonTinh()) {
            return 8700 + 280;
        }
        if (this.wModel.isCaiTaoMiNuong()) {
            return 8700 + 277;
        }
        if (this.wModel.isGiapRong()) {
            return 8700 + 231;
        }
        if (this.wModel.isMeoXanh()) {
            return 8700 + 218;
        }
        if (this.wModel.isMeoQuyToc()) {
            return 8700 + 215;
        }

        if (this.wModel.isTienNu()) {
            return EffectBuff.EFF_QUAN_TIEN_NU;
        }
        if (this.wModel.isTinhQuan()) {
            return EffectBuff.EFF_QUAN_TINH_QUAN;
        }
        if (this.wModel.isAoDaiNam()) {
            return EffectBuff.EFF_QUAN_AO_DAI_NAM;
        }
        if (this.wModel.isAoDaiNu()) {
            return EffectBuff.EFF_QUAN_AO_DAI_NU;
        }
        
         // TODO THÊM ÁO THỜI TRANG (5) LEG
        if (this.wModel.isNgheThuong()) {
            return 8700 + 174;
        }
        if (this.wModel.isNgocNu()) {
            return 8700 + 177;
        }       
        if (this.wModel.isXichCuoc()) {
            return 8700 + 180;
        }       
        if (this.wModel.isKimDong()) {
            return 8700 + 183;
        }       
        if (this.wModel.isLoiCong()) {
            return 8700 + 171;
        }   
        if (this.wModel.isLacLongQuan()) {
            return 8700 + 121;
        }
        if (this.wModel.isAuCo()) {
            return 8700 + 118;
        }
        // Ba Thien Ho: vay/chan. Nam xanh-vang (196), nu do-vang (193).
        if (this.wModel.isBaThienHoNam()) {
            return 8700 + 196;
        }
        if (this.wModel.isBaThienHoNu()) {
            return 8700 + 193;
        }
        // Bon bo bong da - quan. Xem bang o getIdHeadDynamic().
        if (this.wModel.isBongDaNamAoDo()) {
            return 8700 + 155;
        }
        if (this.wModel.isBongDaNuAoTrang()) {
            return 8700 + 158;
        }
        if (this.wModel.isBongDaNamAoCam()) {
            return 8700 + 162;
        }
        if (this.wModel.isBongDaNuAoSoc()) {
            return 8700 + 165;
        }
        if (this.wModel.isAonoelNam2024()) {
            return 8700 + 261;
        } 
        if (this.wModel.isAonoelNu2024()) {
            return 8700 + 258;
        } 
        return -1;
    }

    public int getIdEffTool5l() {
        if (this.wModel != null) {
            return this.wModel.getIdEff();
        }
        return -1;
    }

    public boolean ischeckActivebuffgiamsatthuong() {
        if (System.currentTimeMillis() - this.timeSendEffTool5l >= 0L) {
            this.timeBuffGiamSatThuong = System.currentTimeMillis() + 10000L;
            return true;
        }
        return false;
    }

    
    @Override
    // Than method (213 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void subUpdate() {
        CharCapNhat.subUpdate(this);
    }
    
    
   
    void handleModelEffects()
    {
        // KHONG duoc doi hoi getIdEffTool5l() > -1 o day. Ham nay tu chon id hieu ung theo
        // tung mau ao ben duoi, khong dung ideff cua item, nen bat buoc item phai co ideff
        // la vo ly — va da lam mat hieu ung that: Mieu quy toc (id 834) co nhanh
        // effectId = 233 nhung ideff trong data_item la -1 nen ham thoat ngay tu dong dau,
        // hao quang cua no chua bao gio chay. Dieu kien can va du chi la con song va da qua
        // moc gui lai; chinh khoi "if (effectId != -1)" ben duoi da loc nguoi khong mang mau
        // ao nao co hieu ung.
        if (this.hp <= 0 || System.currentTimeMillis() - this.timeSendEffTool5l < 0L)
        {
            return;
        }
        if (this.wModel == null)
        {
            return;
        }

        int effectId = -1;
    
        // Xác định effectId dựa trên model
        if (this.wModel.isGiapRong()) 
        {
            effectId = 232;
        } else if (this.wModel.isMeoQuyToc()) 
        {
            effectId = 233;
        } else if (this.wModel.isNgheThuong()) 
        {
            effectId = 189;
        } else if (this.wModel.isNgocNu()) 
        {
            effectId = 190;
        } else if (this.wModel.isXichCuoc()) 
        {
            
            effectId = 187;
        } else if (this.wModel.isKimDong()) 
        {
          
            effectId = 188;
        } else if (this.wModel.isLoiCong()) 
        {
            effectId = 186;
        }
        // Thêm các model khác ở đây...
    
        
        // Nếu có effect thì apply
        if (effectId != -1) {
        int idEff = effectId + 8700;
        long duration = 5000L; // 5 giây

        if (this.addEffBuff(idEff, duration, EffectBuff.BY_ACTOR, 0) != null) {
        try {
            Message mef = MessageCreator.createMsgNewEffectSkill5Long(idEff, (int) duration, this.id, this.cat, true, false);
            this.sendMessage(mef);
            this.sendToNearPlayer(mef);
            this.timeSendEffTool5l = System.currentTimeMillis() + 10000L;
        } catch (Exception e) {
            e.printStackTrace();
        }
      }
    }
  }      
    void charOutRange(final Char p) {
        final Message msg = new Message(8);
        try {
            msg.dos.writeShort(p.id);
            if (p.map == this.map) {
                this.sendMessage(msg);
            }
            if (p.isBot != -1) {
                --this.nNpc;
            }
        } catch (final IOException ex) {
        }
    }

    public boolean checkInParty(final Char c) {
        return this.party.userParty.contains(c) && c.region == this.region;
    }

    public void checkOutDelay() {
        if (this.outdelay > 0) {
            --this.outdelay;
            if (this.outdelay == 0) {
                CharManager.instance.kickPlayer(this, "char 1");
                this.exit = true;
            }
        }
    }

    public void doResetItemChienTruong() {
        this.potions[128] = 0;
        this.potions[131] = 0;
        this.potions[132] = 0;
        this.potions[129] = 0;
        this.potions[130] = 0;
        this.potions[126] = 0;
        this.potions[127] = 0;
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
    }

    @Override
    public int getInCountry() {
        if (this.map == null || !this.map.isMapChienTruongMoba()) {
            return this.inCountry;
        }
        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
        if (c != null) {
            return c.team;
        }
        return 0;
    }

    public int getMyCountry() {
        if (this.map == null || !this.map.isMapChienTruongMoba()) {
            return this.myCountry;
        }
        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
        if (c != null) {
            return c.team;
        }
        return 0;
    }

    @Override
    public int getTypePk() {
        if (this.map != null && (this.map.isMapChienTruongMoba() || this.map.isMapLoiDai())) {
            return this.pk_chienTruong;
        }
        return this.pk;
    }

    /** Loi ghi actor da bao roi - moi loai bao mot lan, khong de log ngap moi 300ms. */
    private static final java.util.Set<String> daBaoLoiGhiActor =
            java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

    /**
     * Ghi MOT ban ghi vi tri actor vao goi tin, KIEU "duoc an ca, nga ve khong".
     *
     * LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "hinh nhan vat cua toi va cua moi
     * nguoi khac luc an luc hien", 1 giay 1-2 lan, o MOI ban do): ban cu ghi thang vao
     * m.dos va chi bat IOException. Nhung nhieu dong trong than ham co the nem loi KHAC
     * ma khong ai bat:
     *   - posNPCInVilage[this.inCountry]   mang luon dai 3 (MapNapTaiNguyen), inCountry
     *                                      ra ngoai 0..2 -> ArrayIndexOutOfBounds
     *   - ((Monster) a).getMonsterTemplate().isNewMonster   template null -> NPE
     *   - a.getIdCharThanThu() / a.canFocus(this) / a.isMyHoVe(this)   NPE
     *
     * Khi do vai truong DAU da nam trong goi roi (cat, type, id, x, y...), con cac truong
     * SAU thi khong. Goi van duoc gui di (updateNearChar bat Exception roi "continue", sau
     * do van sendMessage). Client doc ban ghi do bi thieu byte nen LECH NHIP: no lay byte
     * dau cua actor ke tiep lam toa do cua actor nay, roi ca phan con lai cua goi thanh
     * rac. Ket qua khong phai "mat mot NPC" ma la MAT SACH moi thu trong goi - ke ca nhan
     * vat cua chinh minh. Tick sau (300ms) neu khong dinh loi thi hien lai -> nhap nhay.
     *
     * Nay ghi vao bo dem tam truoc; chi khi ban ghi HOAN CHINH moi noi vao goi that. Hong
     * thi mat dung mot actor do, goi van doc duoc binh thuong. Kem mot dong log (moi loai
     * mot lan) de con lan ra nguyen nhan goc thay vi im lang.
     */
    public Message writeActorPos(final Message m, final Actor a) {
        final java.io.ByteArrayOutputStream dem = new java.io.ByteArrayOutputStream(24);
        final java.io.DataOutputStream tam = new java.io.DataOutputStream(dem);
        try {
            tam.writeByte(a.cat);
            tam.writeByte(a.getType());
            tam.writeShort(a.id);
            short x = (short) a.x;
            short y = (short) a.y;
            if (a.cat == 0) {
                final Char aa = (Char) a;
                // Chan bien: mang nay luon dai 3 (mot o cho moi nuoc), con inCountry co
                // the mang gia tri khac o ban do dau truong/cuoi. Ngoai bien thi giu
                // nguyen toa do that cua actor thay vi nem loi.
                if (aa.posNPCInVilage != null
                        && this.inCountry >= 0
                        && this.inCountry < aa.posNPCInVilage.length
                        && aa.posNPCInVilage[this.inCountry] != null
                        && aa.posNPCInVilage[this.inCountry].length >= 2) {
                    x = aa.posNPCInVilage[this.inCountry][0];
                    y = aa.posNPCInVilage[this.inCountry][1];
                }
            }
            tam.writeShort(x);
            tam.writeShort(y);
            tam.writeByte((a.cat == 0 && (this.isKiller || this.killer <= 0)) ? a.getTypePk() : -1);
            tam.writeInt(a.getCharID());
            if (a.cat == 1) {
                tam.writeByte(((Monster) a).getMonsterTemplate().isNewMonster);
            }
            if (!TeamServer.isServerIndo()) {
                tam.writeByte(a.getIdCharThanThu());
                if (a.isMyHoVe(this)) {
                    tam.writeBoolean(false);
                } else {
                    tam.writeBoolean(a.canFocus(this));
                }
            }
        } catch (final Exception ex) {
            // Bo qua DUNG actor nay - khong ghi mot byte nao vao goi that.
            final String khoa = ex.getClass().getName() + "|" + a.cat + "|" + ex.getMessage();
            if (daBaoLoiGhiActor.add(khoa)) {
                System.out.println("[ACTOR] bo qua actor cat=" + a.cat + " id=" + a.id
                        + " vi loi ghi: " + ex + " (bao mot lan cho moi loai loi)");
            }
            return m;
        }
        try {
            m.dos.write(dem.toByteArray());
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        return m;
    }

    public void sendPetEff() {
        if (this.petUsing != null && !this.petUsing.isPetTool() && this.petUsing.getIdEffPet() > -1 && this.addEffBuff(this.petUsing.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
            this.sendEffToChar(this);
        }
    }

    public void sendMessage(final Message m) {
        if (this.isBot != -1) {
            return;
        }
        if (this.session != null) {
            this.session.sendMessage(m);
        }
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(final Session s) {
        this.session = s;
    }

    /*
     * KHONG THEM LAI PHEP LOC BAN DO / KHU VUC / TAM NHIN VAO HAI HAM DUOI DAY.
     *
     * Hai ham nay phat goi tin cho NGUOI DUNG GAN - trong do co
     * MessageCreator.createCharInfo(this), tuc goi mo ta NGOAI HINH nhan vat. Nguoi ben canh
     * khong nhan duoc goi nay thi khong co gi de ve.
     *
     * DANH SACH nearChars DA LA DANH SACH DA LOC. CharCapNhat.updateNearChar chi them id nao
     * qua duoc kiem tra ban do + quoc gia + khu vuc + tam nhin, va go id ra ngay khi ho roi
     * di. Loc them mot lan nua o day la THUA, va chi co the LAM MAT nguoi nhan khi mot truong
     * nao do lo cu: nguoi vua doi khu van con nam trong nearChars mot cach hop le cho toi luc
     * updateNearChar don, trong khoang do ho van phai duoc nhan goi.
     *
     * LICH SU - da hong HAI lan lien tiep vi co gang "sua" cho nay:
     *
     *   bb83665 (ban dich nguoc goc): than "if" RONG, khong bo qua ai ca.
     *       if (p == null || ... || p.region == this.region) {
     *       }
     *       s.sendMessage(m);      // -> gui cho TAT CA trong nearChars. CHAY DUNG.
     *
     *   2480c47 "fix isse" (05/09/2026 02:29): them "continue;" vao than rong do. Phep loc
     *       bat dau chay that va lay luon ve "p.region == this.region" - tuc bo qua dung
     *       nhung nguoi CUNG khu vuc. Gui cho gan nhu khong ai. Nguoi choi bao nhan vat
     *       "luc an luc hien" NGAY SAU khi ban nay len server.
     *
     *   Lan sua dau cua toi: doi thanh "p.region != this.region" cho khop huong voi ham song
     *       sinh sendInfoMove2Near(). Dung huong nhung VAN HEP HON ban goc - gio chi gui cho
     *       nguoi cung khu vuc. Nguoi choi bao VAN NHAY.
     *
     * Nay quay ve dung ngu nghia cua ban goc: gui cho moi id trong nearChars. Chi giu lai ba
     * phep bo qua that su can:
     *   p == null      id da roi map, khong con doi tuong
     *   p.id == this.id  chinh minh (call site nao can thi da tu goi sendMessage rieng)
     *   p.isBot != -1  bot khong co phien, Char.sendMessage cung tu thoat ngay
     *
     * Rieng sendInfoMove2Near() (o tren cung tep) VAN giu phep loc region cua no - ham do
     * chua bao gio bi sua va chua bao gio bi bao loi, dung lam chuan doi chieu cho hai ham
     * nay la SAI vi hai ham nay von khong loc gi ca.
     *
     * test/KiemGuiGoiNguoiGan.java chot lai dieu nay.
     */
    @Override
    public void sendEffToNearChar() {
        for (int j = this.nearChars.size() - 1; j >= 0; --j) {
            try {
                final Char p = this.map.getPlayerByID(this.nearChars.get(j));
                if (p == null || p.id == this.id || p.isBot != -1) {
                    continue;
                }
                this.sendEffToChar(p);
            } catch (final Exception ex) {
            }
        }
    }

    public void sendToNearPlayer(final Message m) {
        for (int j = this.nearChars.size() - 1; j >= 0; --j) {
            try {
                final Char p = this.map.getPlayerByID(this.nearChars.get(j));
                // Doc p.session PHAI nam sau phep kiem null: ban cu doc "p.session" ngay
                // truoc dong "if (p == null ...)" nen khi p null la nem NPE truoc khi kip
                // kiem - phep kiem null do thuc te la ma chet.
                if (p == null || p.id == this.id || p.isBot != -1) {
                    continue;
                }
                final Session s = p.session;
                if (s != null) {
                    s.sendMessage(m);
                }
            } catch (final Exception ex) {
            }
        }
    }

    public boolean partyIsDie(final Char p, final int mapID) {
        if (p.party.userParty.size() < 0) {
            return false;
        }
        for (int i = 0; i < p.party.userParty.size(); ++i) {
            final Char c = p.party.userParty.get(i);
            if (c.hp > 0 && c.mapID == mapID) {
                return false;
            }
        }
        return true;
    }

    public void createParty() {
        try {
            final int size = RealController.intance.idGen.getSize(5);
            if (this.partyID != -1 || size == 0) {
                final Message m = MessageCreator.createServerAlertMessage("Không thể tạo thêm party", "");
                this.sendMessage(m);
                m.cleanup();
                return;
            }
            short idP = RealController.intance.idGen.getID(5, "tao party");
            if (idP == -1) {
                idP = RealController.intance.idGen.getID(5, "tao party");
            }
            this.partyID = idP;
            this.masterIDParty = this.id;
            this.party.idMaster = this.id;
            final Message m = new Message(48);
            m.dos.writeShort(idP);
            this.sendMessage(m);
            m.cleanup();
        } catch (final Exception ex) {
        }
    }

    public void deleteParty() {
        final int size = this.party.userParty.size();
        try {
            if (size < 0 || this.partyID == -1 || (size > 0 && this.id != this.party.idMaster)) {
                final Message mm = MessageCreator.createServerAlertMessage(Text.TRUONG_NHOM_GIAI_TAN, "");
                this.sendMessage(mm);
                mm.cleanup();
                return;
            }
            if (size > 0) {
                final Message mm = new Message(50);
                mm.dos.writeByte(1);
                for (int i = 0; i < size; ++i) {
                    final Char ch = this.party.userParty.elementAt(i);
                    if (ch.id != this.id) {
                        ch.party = null;
                        ch.sendMessage(mm);
                        ch.party = new Party();
                        ch.partyID = -1;
                        ch.masterIDParty = -1;
                    }
                }
                RealController.intance.idGen.putID(this.partyID, 5, "xoa party");
                this.sendMessage(mm);
                this.party.userParty.removeAllElements();
                this.party = null;
                this.party = new Party();
                this.partyID = -1;
                this.masterIDParty = -1;
                mm.cleanup();
            }
        } catch (final Exception ex) {
        }
    }

    public void sendInviteParty2User(final Message m) {
        try {
            if (!this.party.userParty.isEmpty() && this.id != this.party.idMaster) {
                final Message mm = MessageCreator.createServerAlertMessage("Chỉ trưởng nhóm mới được mời.", "");
                this.sendMessage(mm);
                mm.cleanup();
                return;
            }
            final int nMember = this.party.userParty.size();
            if (nMember >= Party.MAX_MEMBER) {
                final Message mm = MessageCreator.createServerAlertMessage(Text.KO_THE_MOI_THEM_PARTY, "");
                this.sendMessage(mm);
                mm.cleanup();
                return;
            }
            final int idFriend = m.dis.readShort();
            final Char fr = this.map.getPlayerByID((short) idFriend);
            if (fr != null) {
                if (fr.map.isMapLoiDai()) {
                    return;
                }
                if (!fr.rcvInvite || !fr.rcvInviteVip) {
                    this.sendMessage(MessageCreator.createServerAlertMessage(Text.KO_NHAN_LOI_MOI, ""));
                    return;
                }
                if (fr.id == idFriend && fr.party.userParty.size() <= 0) {
                    final Message mm = new Message(49);
                    mm.dos.writeByte(0);
                    mm.dos.writeShort(this.partyID);
                    mm.dos.writeShort(this.id);
                    mm.dos.writeUTF(this.charname);
                    mm.dos.writeByte(this.lvDetail.lv);
                    mm.dos.writeShort(this.masterIDParty);
                    mm.dos.writeByte(this.charClass);
                    if (nMember > 0) {
                        for (int j = 0; j < nMember; ++j) {
                            final Char ch = this.party.userParty.elementAt(j);
                            if (ch.id != this.id) {
                                mm.dos.writeShort(ch.id);
                                mm.dos.writeUTF(ch.charname);
                                mm.dos.writeByte(ch.lvDetail.lv);
                                mm.dos.writeByte(ch.charClass);
                            }
                        }
                        fr.sendMessage(mm);
                    }
                    fr.sendMessage(mm);
                    mm.cleanup();
                    return;
                }
            }
            final Message mm = MessageCreator.createServerAlertMessage(Text.DUNG_QUA_XA, "");
            this.sendMessage(mm);
            mm.cleanup();
        } catch (final Exception ex) {
        }
    }

    public void okJoinParty(final Message m) {
        try {
            if (this.party.userParty.size() >= Party.MAX_MEMBER) {
                return;
            }
            final short idInvite = m.dis.readShort();
            final Char fr = CharManager.instance.getByCharID(idInvite);
            if (fr != null && this.partyID == -1) {
                final int size = fr.party.userParty.size();
                this.partyID = fr.partyID;
                Message mm = new Message(49);
                mm.dos.writeByte(1);
                mm.dos.writeShort(this.id);
                mm.dos.writeUTF(this.charname);
                mm.dos.writeByte(this.lvDetail.lv);
                mm.dos.writeByte(this.charClass);
                fr.sendMessage(mm);
                mm.cleanup();
                if (size < 1) {
                    fr.party.addUser(fr);
                    mm.cleanup();
                } else {
                    mm = new Message(49);
                    mm.dos.writeByte(3);
                    mm.dos.writeShort(this.id);
                    mm.dos.writeUTF(this.charname);
                    mm.dos.writeByte(this.lvDetail.lv);
                    mm.dos.writeByte(this.charClass);
                    for (int j = 0; j < size; ++j) {
                        final Char ch = fr.party.userParty.elementAt(j);
                        if (ch.id != fr.id) {
                            ch.sendMessage(mm);
                        }
                    }
                    mm.cleanup();
                }
                fr.party.addUser(this);
                this.party = fr.party;
                this.masterIDParty = fr.masterIDParty;
            }
        } catch (final Exception e) {
            System.out.println("LOI KHI NHAN LOI OK VAO NHOM CUA USER");
            e.printStackTrace();
        }
    }

    public void cancelJoinParty(final Message m) {
        try {
            final int idInvite = m.dis.readShort();
            final Char fr = this.map.getPlayerByID((short) idInvite);
            if (fr != null && fr.id == idInvite) {
                final Message mm = new Message(49);
                mm.dos.writeByte(2);
                mm.dos.writeUTF(this.charname);
                fr.sendMessage(mm);
                mm.cleanup();
            }
        } catch (final Exception ex) {
        }
    }

    public void outParty() {
        try {
            final Message msg = new Message(50);
            msg.dos.writeByte(2);
            msg.dos.writeShort(this.id);
            for (int i = 0; i < this.party.userParty.size(); ++i) {
                final Char ch = this.party.userParty.elementAt(i);
                if (ch.id != this.id) {
                    ch.party.remoUser(this.id);
                    if (ch.id == this.party.idMaster && ch.party.userParty.size() == 1) {
                        ch.removeParty();
                    }
                }
                ch.sendMessage(msg);
            }
            this.party = null;
            this.partyID = -1;
            this.masterIDParty = -1;
            this.party = new Party();
            this.sendMessage(msg);
            msg.cleanup();
        } catch (final Exception ex) {
        }
    }

    public void kickOutParty(final Message m) {
        try {
            if (this.id != this.party.idMaster) {
                final Message msg = MessageCreator.createServerAlertMessage(Text.DUOI_PARTY, "");
                this.sendMessage(msg);
                msg.cleanup();
                return;
            }
            final short idchar = m.dis.readShort();
            final Char delChar = this.party.remoUser(idchar);
            if (delChar != null) {
                final Message msg = new Message(50);
                msg.dos.writeByte(0);
                msg.dos.writeShort(idchar);
                for (int i = 0; i < this.party.userParty.size(); ++i) {
                    final Char ch = this.party.userParty.elementAt(i);
                    if (ch.id != this.id) {
                        ch.party.remoUser(idchar);
                    }
                    ch.sendMessage(msg);
                }
                delChar.party = null;
                delChar.partyID = -1;
                delChar.masterIDParty = -1;
                delChar.party = new Party();
                delChar.sendMessage(msg);
                this.sendMessage(msg);
                if (this.party.userParty.size() == 1) {
                    this.removeParty();
                }
                msg.cleanup();
            }
        } catch (final Exception ex) {
        }
    }

    private void removeParty() {
        RealController.intance.idGen.putID(this.partyID, 5, "kick va giai tan party");
        this.party.partyRemove();
        this.party = null;
        this.partyID = -1;
        this.masterIDParty = -1;
        this.party = new Party();
    }

    public void resetTimeLastuseSkill() {
        for (int i = 0; i < this.timeLastUseSkills.length; ++i) {
            this.timeLastUseSkills[i] = 0L;
        }
    }

    // Than method (114 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void setBuff2Char(final Message message) {
        CharCapNhat.setBuff2Char(this, message);
    }

    public Char getCharFromNearChar(final int charID) {
        for (int i = 0; i < this.nearChars.size(); ++i) {
            final Char c = this.map.getPlayerByID(this.nearChars.elementAt(i));
            if (c != null && c.id == charID) {
                return c;
            }
        }
        return null;
    }

    public void removeBuff(final Message message) {
        try {
            final int charID = message.dis.readShort();
            final int buffEff = message.dis.readByte();
            final Message m = new Message(51);
            m.dos.writeByte(0);
            m.dos.writeShort(charID);
            m.dos.writeByte(buffEff);
            for (int i = 0; i < this.nearChars.size(); ++i) {
                final Char ch = this.map.getPlayerByID(this.nearChars.elementAt(i));
                if (ch != null) {
                    ch.sendMessage(m);
                }
            }
            m.cleanup();
        } catch (final Exception ex) {
        }
    }

    private int getBuffType(final int buffID) {
        try {
            return CharManager.EFF_BUFF_SKILL[this.charClass][buffID];
        } catch (final Exception ex) {
            return -1;
        }
    }

    public void receiveClanQuest() {
        try {
            if (this.rcvQuestClan) {
                this.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhận thêm khi đang làm nhiệm vụ", ""));
                return;
            }
            final int index = Map.r.nextInt(10);
            final Message m = new Message(-70);
            m.dos.writeByte(0);
            m.dos.writeByte(QuestClan.info_receive[index].length);
            for (int i = 0; i < QuestClan.info_receive[index].length; ++i) {
                m.dos.writeUTF(QuestClan.info_receive[index][i]);
            }
            this.sendMessage(m);
        } catch (final Exception ex) {
        }
    }

    public void acceptClanQuest(final int type) {
        try {
            if (type != 1) {
            }
        } catch (final Exception ex) {
        }
    }

    public void responseClanQuest() {
    }

    public void finishClanQuest() {
    }

    public void receiveQuest(final Message m) {
    }

    public void sendPotionQuest(final Potion pt) {
        try {
            final Message m = new Message(19);
            RealController.intance.idGen.putID(pt.id, 4, "tra lai cho real trong char");
            final int[] potions = this.potions;
            final short type = pt.getType();
            potions[type] += pt.quantity;
            m.dos.writeShort(this.id);
            m.dos.writeShort(pt.id);
            m.dos.writeByte(pt.getType());
            m.dos.writeShort(pt.quantity);
            this.sendMessage(m);
            m.cleanup();
        } catch (final Exception ex) {
        }
    }

    public void createCharQuest() {
        if (this.char_quest == null) {
            this.char_quest = new CharQuest();
        }
        this.char_quest.id_char = this.charDBID;
        this.char_quest.id_quest = this.nextQuest;
        if (this.monsterKill != null) {
            for (int i = 0; i < this.monsterKill.length; ++i) {
                this.char_quest.nMonster[i] = (short) this.monsterKill[i][1];
            }
        }
        if (this.monsterIem != null) {
            for (int i = 0; i < this.monsterIem.length; ++i) {
                this.char_quest.nItem[i] = (short) this.monsterIem[i][1];
            }
        }
        this.char_quest.receive = (byte) (this.receiveQuest ? 1 : 0);
        this.char_quest.finish = (byte) (this.isFinish ? 1 : 0);
    }

    public void sendInfoQuest() {
        MessageCreator.createMsgCharQuest(this, 0);
        MessageCreator.createMsgCharQuest(this, 1);
        MessageCreator.createMsgCharQuest(this, 2);
    }

    private void sendInfoQuest(final int idQuest) {
        final Message msg = new Message(52);
        final int type = QuestTemplate.QUEST_TYPE[idQuest - 1];
        try {
            msg.dos.writeByte(0);
            msg.dos.writeByte(type);
            msg.dos.writeUTF(QuestTemplate.title_quest[idQuest - 1]);
            msg.dos.writeByte(QuestTemplate.content_quest[idQuest - 1].length);
            for (int i = 0; i < QuestTemplate.content_quest[idQuest - 1].length; ++i) {
                msg.dos.writeUTF(QuestTemplate.content_quest[idQuest - 1][i]);
            }
            msg.dos.writeUTF(QuestTemplate.decription_quest[idQuest - 1]);
            msg.dos.writeUTF(QuestTemplate.info_Gif[idQuest - 1]);
            switch (type) {
                case 0: {
                    final int[][] monster = QuestTemplate.getMonsterKill(idQuest - 1);
                    this.monsterKill = new int[monster.length][3];
                    msg.dos.writeByte(monster.length);
                    for (int j = 0; j < monster.length; ++j) {
                        for (int k = 0; k < monster[j].length; ++k) {
                            msg.dos.writeShort(monster[j][k]);
                            this.monsterKill[j][k] = monster[j][k];
                        }
                    }
                    break;
                }
                case 1: {
                    this.npc_quest = null;
                    this.npc_quest = new int[QuestTemplate.npc_quest[idQuest - 1].length];
                    msg.dos.writeByte(this.npc_quest.length);
                    for (int j = 0; j < this.npc_quest.length; ++j) {
                        msg.dos.writeByte(QuestTemplate.npc_quest[idQuest - 1][j]);
                        this.npc_quest[j] = QuestTemplate.npc_quest[idQuest - 1][j];
                    }
                    final String[] info = QuestTemplate.info_npc_quest[idQuest - 1];
                    msg.dos.writeByte(info.length);
                    for (int l = 0; l < info.length; ++l) {
                        msg.dos.writeUTF(info[l]);
                    }
                    msg.dos.writeUTF(QuestTemplate.nameItemQuest[this.nextQuest - 1]);
                    break;
                }
                case 2: {
                    final short[][] monster_item = QuestTemplate.monster_item[idQuest - 1];
                    this.monsterIem = new int[monster_item.length][3];
                    msg.dos.writeByte(this.monsterIem.length);
                    for (int m = 0; m < monster_item.length; ++m) {
                        for (int j2 = 0; j2 < monster_item[m].length; ++j2) {
                            msg.dos.writeShort(monster_item[m][j2]);
                            this.monsterIem[m][j2] = monster_item[m][j2];
                        }
                    }
                    msg.dos.writeUTF(QuestTemplate.nameItemQuest[this.nextQuest - 1]);
                    break;
                }
            }
            this.sendMessage(msg);
        } catch (final Exception e) {
            System.out.println("LOI KHI GUI THONG TIN QUEST CHO CLIENT " + e.toString());
        } finally {
            try {
                msg.cleanup();
            } catch (final Exception ex) {
            }
        }
        try {
            msg.cleanup();
        } catch (final Exception ex2) {
        }
    }

    public static byte getIdOpenShield(final int iHour) {
        if (iHour < 10) {
            return 0;
        }
        if (iHour >= 10 && iHour < 15) {
            return 1;
        }
        if (iHour >= 15 && iHour < 20) {
            return 2;
        }
        return 3;
    }

    public static boolean haveShieldNation(final int inCountry) {
        final Calendar cl = Calendar.getInstance();
        final int iHour = cl.get(11);
        if (iHour >= 0 && iHour < 6) {
            return true;
        }
        if (Map.idClanTown[inCountry] == -1) {
            return true;
        }
        for (byte i = 0; i < 4; ++i) {
            if (Map.SHIELD[inCountry][i] > 0 && iHour >= Char.hourShield[i][0] && iHour < Char.hourShield[i][1]) {
                return true;
            }
        }
        return false;
    }

    public boolean haveShield() {
        final Calendar cl = Calendar.getInstance();
        final int iHour = cl.get(11);
        if (this.map != null && this.map.mapIDLoadMap == 118) {
            return false;
        }
        if (iHour >= 0 && iHour < 6) {
            return true;
        }
        if (Map.pkAuto) {
            return false;
        }
        if (Map.idClanTown[this.inCountry] == -1) {
            return !this.isKiller;
        }
        for (byte i = 0; i < 4; ++i) {
            if (Map.SHIELD[this.inCountry][i] > 0 && iHour >= Char.hourShield[i][0] && iHour < Char.hourShield[i][1]) {
                return !this.isKiller;
            }
        }
        return false;
    }

    public void controlQuest() {
    }

    public void gotItemQuest() {
    }

    public void removeAllItem() {
        this.iItems.removeAll(this.iItems);
        this.tItems.removeAll(this.tItems);
        this.rItems.removeAll(this.rItems);
        this.bItem.removeAll(this.bItem);
        this.awItems.removeAll(this.awItems);
        this.friendListID.removeAll(this.friendListID);
        this.friendListName.removeAll(this.friendListName);
    }

    public void cleanup() {
        this.nearChars.removeAll(this.nearChars);
        this.iItems.removeAll(this.iItems);
        this.tItems.removeAll(this.tItems);
        this.rItems.removeAll(this.rItems);
        this.bItem.removeAll(this.bItem);
        this.awItems.removeAll(this.awItems);
        this.friendListID.removeAll(this.friendListID);
        this.friendListName.removeAll(this.friendListName);
        this.animal.removeAll(this.animal);
        this.pet.removeAll(this.pet);
    }

    public void resetClanQuest() {
    }

    public void sendInfoQuestClan() {
    }

    public void checkFinishQuestClan(final byte item) {
    }

    public boolean checkFinishQuestClanMons(final int idmonster) {
        return false;
    }

    public boolean checkFinsishQuest(final int idMonster, final int idNpc, final int idItem) {
        if (this.isFinish) {
            return false;
        }
        final int type = QuestTemplate.QUEST_TYPE[this.questID - 1];
        final Message m = new Message(52);
        final int nNPC = 0;
        int f = 0;
        try {
            switch (type) {
                case 0: {
                    f = 0;
                    if (this.monsterKill != null) {
                        for (int i = 0; i < this.monsterKill.length; ++i) {
                            if (this.monsterKill[i][1] == 0) {
                                ++f;
                            } else if (idMonster == this.monsterKill[i][0]) {
                                if (this.monsterKill[i][1] > 0) {
                                    final int[] array = this.monsterKill[i];
                                    final int n = 1;
                                    --array[n];
                                }
                                if (this.monsterKill[i][1] == 0) {
                                    ++f;
                                }
                            }
                        }
                    }
                    if (f == this.monsterKill.length) {
                        this.sendInfoFinishQuest();
                        break;
                    }
                    break;
                }
                case 1: {
                    this.sendInfoFinishQuest();
                    break;
                }
                case 2: {
                    f = 0;
                    if (this.monsterIem != null) {
                        for (int i = 0; i < this.monsterIem.length; ++i) {
                            if (this.monsterIem[i][1] == 0) {
                                ++f;
                            } else if (idItem == this.monsterIem[i][2]) {
                                if (this.monsterIem[i][1] > 0) {
                                    final int[] array2 = this.monsterIem[i];
                                    final int n2 = 1;
                                    --array2[n2];
                                }
                                if (this.monsterIem[i][1] == 0) {
                                    ++f;
                                }
                            }
                        }
                    }
                    if (f == this.monsterIem.length) {
                        this.sendInfoFinishQuest();
                        break;
                    }
                    break;
                }
                case 3: {
                    this.sendInfoFinishQuest();
                    break;
                }
            }
        } catch (final Exception ex) {
        } finally {
            try {
                m.cleanup();
            } catch (final Exception ex2) {
            }
        }
        try {
            m.cleanup();
        } catch (final Exception ex3) {
        }
        return false;
    }

    private void sendInfoFinishQuest() {
        MessageCreator.createMsgCharQuest(this, 0);
        MessageCreator.createMsgCharQuest(this, 1);
        MessageCreator.createMsgCharQuest(this, 2);
    }

    public void talkWithNpcResponse(final int idNpc, final int type, final int idQuest) {
        if (type == 0) {
            if (idQuest != this.questID || !this.isFinish || !this.receiveQuest) {
                return;
            }
            if (!this.talk_npc && idNpc == QuestTemplate.npc_quest_response[this.questID - 1][this.currentNpc]) {
                this.talk_npc = true;
            }
        } else {
            if ((idQuest != this.questID || !this.isFinish || !this.receiveQuest) && QuestTemplate.getTypeQuest(idQuest) != 3) {
                return;
            }
            if (this.talk_npc && idNpc == QuestTemplate.npc_quest_response[this.questID - 1][this.currentNpc]) {
                ++this.currentNpc;
                if (this.currentNpc >= QuestTemplate.npc_quest_response[this.questID - 1].length) {
                    final Message m = new Message(54);
                    try {
                        this.delItemQuestOutInventory();
                        this.onSendGiftQuest();
                        this.resetQuest();
                        if (this.nextQuest < QuestTemplate.npc_quest_receive.length) {
                            this.idNpc = QuestTemplate.npc_quest_receive[this.nextQuest];
                            m.dos.writeShort(++this.nextQuest);
                            m.dos.writeByte(QuestTemplate.npc_quest_receive[this.nextQuest - 1]);
                            this.createCharQuest();
                            Database.instance.saveCharQuest(this.charDBID, this.char_quest);
                        } else {
                            m.dos.writeShort(-1);
                            ++this.nextQuest;
                            this.createCharQuest();
                            Database.instance.saveCharQuest(this.charDBID, this.char_quest);
                        }
                        this.sendMessage(m);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            m.cleanup();
                        } catch (final Exception ex) {
                        }
                    }
                    try {
                        m.cleanup();
                    } catch (final Exception ex2) {
                    }
                }
                this.talk_npc = false;
            }
        }
    }

    public boolean canEatCandy(final int type) {
        for (int i = 0; i < this.AllEffBuff.size(); ++i) {
            if (this.AllEffBuff.get(i).idEff == type) {
                return false;
            }
        }
        return true;
    }

    private void delItemQuestOutInventory() {
        for (int i = 10; i < 14; ++i) {
            this.potions[i] = 0;
        }
    }

    private void onSendGiftQuest() {
        Message m = new Message(59);
        try {
            if (this.questID == 2) {
                Item item = null;
                ItemTemplates itemTemplate = null;
                m.dos.writeUTF(QuestTemplate.info_Gif[this.questID - 1]);
                this.sendMessage(m);
                m.cleanup();
                m = new Message(18);
                switch (this.charClass) {
                    case 0: {
                        itemTemplate = (ItemTemplates) Map.itemTemplates.get(0).get(80);
                        item = new Item(itemTemplate, false, 0, 0, 80);
                        break;
                    }
                    case 1: {
                        itemTemplate = (ItemTemplates) Map.itemTemplates.get(1).get(87);
                        item = new Item(itemTemplate, false, 1, 1, 87);
                        break;
                    }
                    case 2: {
                        itemTemplate = (ItemTemplates) Map.itemTemplates.get(2).get(94);
                        item = new Item(itemTemplate, false, 2, 2, 94);
                        break;
                    }
                    case 3: {
                        itemTemplate = (ItemTemplates) Map.itemTemplates.get(3).get(101);
                        item = new Item(itemTemplate, false, 3, 3, 101);
                        break;
                    }
                    case 4: {
                        itemTemplate = (ItemTemplates) Map.itemTemplates.get(4).get(108);
                        item = new Item(itemTemplate, false, 4, 4, 108);
                        break;
                    }
                }
                item.id = this.getIDItem();
                item.owner = this.charDBID;
                this.iItems.add(item);
                m.dos.writeShort(this.id);
                m.dos.writeByte(item.clazz);
                m.dos.writeShort(item.id);
                m.dos.writeShort(item.id);
                m.dos.writeByte(item.getTemplate().id);
                m.dos.writeByte(item.plus);
                m.dos.writeByte(item.level);
                m.dos.writeShort(item.durable);
                m.dos.writeShort(item.mDurable);
                this.sendMessage(m);
                m.cleanup();
            }
            if (this.questID == 8) {
                final String[] st = {"Chém", "Chém", "Đánh", "Đập", "Bắn"};
                m.dos.writeUTF(String.valueOf(QuestTemplate.info_Gif[this.questID - 1]) + st[this.charClass]);
                if (this.skill[0] == 0) {
                    this.skill[0] = 1;
                }
                this.sendMessage(m);
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            }
            Map.addXPForChar(this, QuestTemplate.gold_xp_gift[this.questID - 1][1], false, "char onSendGiftQuest");
            if (QuestTemplate.gold_xp_gift[this.questID - 1][0] > 0) {
                this.sendPotionQuest(this.map.createPotion((short) 0, QuestTemplate.gold_xp_gift[this.questID - 1][0], this.map));
            }
            final short[] giftPotion = QuestTemplate.potionGift[this.questID - 1];
            for (int i = 0; i < giftPotion.length; ++i) {
                final int[] potions = this.potions;
                final int n = i;
                potions[n] += giftPotion[i];
            }
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("LOI GUI THONG TIN QUA CHO CLIENT");
        }
    }

    public void talkWithNpc(final int idNpc, final int type, final int questID) {
        if (questID != this.questID || !this.receiveQuest || this.npc_quest == null) {
            return;
        }
        if (QuestTemplate.QUEST_TYPE[questID - 1] != 1) {
            return;
        }
        if (type == 0) {
            if (!this.talk_npc && idNpc == this.npc_quest[this.currentNpc]) {
                this.talk_npc = true;
            }
        } else if (this.npc_quest != null && this.talk_npc) {
            ++this.currentNpc;
            if (this.currentNpc >= this.npc_quest.length) {
                this.currentNpc = 0;
                this.checkFinsishQuest(-1, -1, -1);
                this.npc_quest = null;
                this.delItemQuestOutInventory();
            }
            this.talk_npc = false;
        }
    }

    public int getIdItemQuest(final int idmonster) {
        if (this.monsterIem != null) {
            for (int i = 0; i < this.monsterIem.length; ++i) {
                if (idmonster == this.monsterIem[i][0] && this.monsterIem[i][1] > 0) {
                    return this.monsterIem[i][2];
                }
            }
        }
        return -1;
    }

    public void resetQuest() {
        this.receiveQuest = false;
        this.isFinish = false;
        this.monsterIem = null;
        this.monsterKill = null;
        this.npc_quest = null;
        this.questID = -1;
        this.talk_npc = false;
        this.currentNpc = 0;
        if (this.char_quest != null) {
            for (int i = 0; i < 3; ++i) {
                this.char_quest.nMonster[i] = 0;
                this.char_quest.nItem[i] = 0;
            }
        }
    }

    public void userExitGame() {
        TichLuyOnline.Instance.exitGame(this.charDBID);
        if (!this.isAdmin) {
            Logdata.addChar(this, 0);
        }
        this.timeExit = System.currentTimeMillis();
        try {
            if (this.userTrade.size() > 0) {
                // Thoat game giua giao dich: huy cho ca hai (duoi Char.KHOA_GIAO_DICH), doi
                // phuong nhan 66/3 de dong cua so.
                this.huyGiaoDich(false, true);
            }
            this.gifLuckyBox.removeAllElements();
        } catch (final Exception e) {
            System.out.println("LOI KHI DANG TRONG QUA TRINH EXIT + CO TRAO DOI");
        }
        this.pk = 0;
        this.isThodia = false;
        this.finishTrade = false;
        final int size = this.party.userParty.size();
        if (size > 0) {
            if (this.id == this.party.idMaster) {
                this.deleteParty();
            } else {
                this.outParty();
            }
        }
        try {
            this.tItems.removeAllElements();
            this.rItems.removeAllElements();
        } catch (final Exception ex) {
        }
        this.removeUserSell();
        if (this.mapID == Map.idMapTown && this.timeGiveCardTown > 0L) {
            if (Map.giveCardFail(this)) {
                final Vector<Char> players = this.map.getAllPlayer((this.mapID == 225 || this.mapID == 226) ? this.idWedding : this.inCountry, this.region);
                for (int i = 0; i < players.size(); ++i) {
                    try {
                        players.get(i).sendMessage(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " giao thẻ thất bại"));
                        players.get(i).sendMessage(MessageCreator.createMsgStartGetTown(this.myCountry));
                    } catch (final Exception ex2) {
                    }
                }
            }
            if (this.map.isMapLienDau()) {
                final MapLienDau j = (MapLienDau) this.map;
                if (j.giveCardLienDauFail(this)) {
                    try {
                        j.sendAllPlayer(MessageCreator.createServerAlertAutoOffMessage("Lãnh thổ " + NpcReceiveCard.nameCountry[this.myCountry] + " giao thẻ thất bại"), this.myCountry);
                        j.sendAllPlayer(j.createMsgStartGetTown(this.myCountry), this.myCountry);
                    } catch (final IOException ex3) {
                    }
                }
            }
        }
        this.exit = true;
        this.charCopyDissapear();
        this.charhireDissapear();
        this.charThanThuDissapear();
    }

    public void charCopyDissapear() {
        try {
            if (this.charCopy != null) {
                this.receiveQuestVulan = 0;
                CharManager.instance.remove(this.charCopy);
                RealController.intance.idGen.putID(this.charCopy.id, 0, "return id player 2 IDGEN");
                final Message msg = new Message(8);
                msg.dos.writeShort(this.charCopy.id);
                final byte idcountry = this.charCopy.myCountry;
                ((CharCopy) this.charCopy).owner = null;
                this.charCopy = null;
                this.map.sendAllPlayer(msg, idcountry, this.region);
            }
        } catch (final Exception ex) {
        }
    }

    public void charhireDissapear() {
        try {
            if (this.charHire != null) {
                CharManager.instance.remove(this.charHire);
                RealController.intance.idGen.putID(this.charHire.id, 0, "return id player 2 IDGEN");
                final Message msg = new Message(8);
                msg.dos.writeShort(this.charHire.id);
                final byte idcountry = this.charHire.myCountry;
                ((CharCopy) this.charHire).owner = null;
                this.charHire = null;
                this.map.sendAllPlayer(msg, idcountry, this.region);
            }
        } catch (final Exception ex) {
        }
    }

    public void charThanThuDissapear() {
        try {
            if (this.charthanthu != null) {
                CharManager.instance.remove(this.charthanthu);
                RealController.intance.idGen.putID(this.charthanthu.id, 0, "return id player 2 IDGEN");
                final Message msg = new Message(8);
                msg.dos.writeShort(this.charthanthu.id);
                final byte idcountry = this.charthanthu.myCountry;
                this.charthanthu.owner = null;
                this.charthanthu = null;
                this.map.sendAllPlayer(msg, idcountry, this.region);
            }
        } catch (final Exception ex) {
        }
    }

    public boolean removeUserSell() {
        for (int i = 0; i < Map.npcSell[this.myCountry].length; ++i) {
            Map.npcSell[this.myCountry][i].removeAllUser(this.id);
        }
        return false;
    }

    public void setItem2Shop() {
        try {
            if (this.myCountry == -1) {
                return;
            }
            for (int i = 0; i < Map.npcSell[this.myCountry].length; ++i) {
                final Vector<Item> itnew = new Vector<Item>();
                for (int m = 0; m < 3; ++m) {
                    final Vector<Item> item = Map.npcSell[this.myCountry][i].getListItemUser(this.id, m);
                    if (item.size() > 0) {
                        for (int k = 0; k < item.size(); ++k) {
                            final Item its = item.get(k);
                            for (int j = 0; j < this.iItems.size(); ++j) {
                                final Item it = this.iItems.get(j);
                                if (it.dbid == its.dbid) {
                                    itnew.add(it);
                                    it.prizeSell = its.prizeSell;
                                }
                            }
                        }
                        if (itnew.size() > 0) {
                            Map.npcSell[this.myCountry][i].shop[m].setListItem(this, itnew);
                        }
                    }
                }
            }
        } catch (final Exception ex) {
        }
    }

    public void resetAllData() {
        this.resetQuest();
        this.monsterIem = null;
        this.monsterKill = null;
        this.receiveQuest = false;
        this.isFinish = false;
        this.char_quest = null;
    }

    public String getInfoAnimal() {
        if (this.animalRide != null) {
            return this.animalRide.getInfoSendUser();
        }
        return "";
    }

    public String[] getListGem() {
        String soluong = String.valueOf(this.listGemitem[0]) + "_" + this.allGemGet[0] + "_" + this.allGemUse[0];
        String soluongLock = String.valueOf(this.listGemitemLock[0]) + "_" + this.allGemGetLock[0] + "_" + this.allGemUseLock[0];
        for (int i = 1; i < this.listGemitem.length; ++i) {
            soluong = String.valueOf(soluong) + "," + this.listGemitem[i] + "_" + this.allGemGet[i] + "_" + this.allGemUse[i];
            soluongLock = String.valueOf(soluongLock) + "," + this.listGemitemLock[i] + "_" + this.allGemGetLock[i] + "_" + this.allGemUseLock[i];
        }
        return new String[]{soluong, soluongLock};
    }

    // Than method (119 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public String getDBInfo() {
        return CharVongLap.getDBInfo(this);
    }

    public static void setTimeAuto(final int day) {
    }

    public Item genItem(final int idTemplate, final int charClass) {
        final ItemTemplates itTemp = (ItemTemplates) Map.itemTemplates.get(charClass).get(new Integer(idTemplate));
        final Item item = new Item(itTemp, false, charClass, charClass, idTemplate);
        return item;
    }

    public void setInfoModelCharBot(final int type, final int idTemplate) {
        final Item it = new Item();
        it.setTemplate(idTemplate, 0, 0, idTemplate);
        this.wModel.setItemModel(this, it, it.atb[4]);
    }

    public void setInfoChar(final String charname, final int type, final int gender, final int charclass, final Map mapid, final int x, final int y, final int userid, final int ao, final int quan, final int non) {
        this.headStyle = 9;
        this.charname = charname;
        this.isBot = (byte) type;
        this.gender = (byte) gender;
        this.charClass = (byte) charclass;
        this.wItems[0][getIndexItemWearing(0, 0)] = this.genItem(ao, 0);
        this.wItems[0][getIndexItemWearing(1, 0)] = this.genItem(quan, 0);
        if (non > -1) {
            this.wItems[0][getIndexItemWearing(2, 0)] = this.genItem(non, 0);
        }
      
        this.wItems[0][getIndexItemWearing(0, 0)].id = 0;
        this.map = mapid;
        this.mapID = this.map.mapId;
        this.x = x;
        this.y = y;
        this.userID = userid;
        this.hp = 1;
        this.myCountry = 1;
        Char.timeSendRaoVat = System.currentTimeMillis() + 900000L;
    }

   


    void initOnlineReceiveGif(final Date lastlog) {
        final Calendar calNow = Calendar.getInstance();
        final Calendar calLog = Calendar.getInstance();
        calLog.setTime(lastlog);
        if (calLog.get(1) != calNow.get(1) || calLog.get(2) != calNow.get(2) || calLog.get(5) != calNow.get(5)) {
            this.rcvGifByHour = 1;
            ++this.totalDayInMonth;
            this.isOnlineToDay = false;
        }
        if (calLog.get(1) != calNow.get(1) || calLog.get(2) != calNow.get(2)) {
            this.rcvGifByMonth = 1;
            this.totalDayInMonth = 1;
            this.isOnlineToDay = false;
        }
    }

    public void setCountry() {
        try {
            final NewClan clan = NewClan.getClan(this.idClan);
            if (clan == null) {
                this.idClan = -1;
            } else if ((this.idClan >= 0 && this.myCountry == -1) || clan.country != this.myCountry) {
                this.myCountry = clan.country;
            }
        } catch (final Exception ex) {
        }
    }

    public void countItemBug() {
        for (int i = 0; i < this.itemCheckDuplicate.size(); ++i) {
        }
    }

    // Than method (105 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public void setItemTuiDo(String info) {
        CharVongLap.setItemTuiDo(this, info);
    }

    /**
     * Tran so entry doc lai duoc tu cot `inven` / `bag`.
     *
     * Cot `inven` KHONG chi chua hanh trang: no chua 3 o dac biet ghi truoc
     * (itemHoaKyLan, itemVukhiThoiTrang, itemThanKhi — xem
     * Database.saveCharAuto), roi toan bo iItems, roi toi da 6 mon model dinh
     * o cuoi (ModelWearing.getInfoModelWearing).
     *
     * Suc chua that cua hanh trang la MAX_INVENTORY * maxBag = 42 * 5 = 210 o
     * (tu ngay nang MAX_BAG_LIMIT tu 3 len 5). Tran cu cam cung 130 nen moi lan
     * login la XOA VINH VIEN cac entry tu vi tri 130 tro di — mon vua tao nam o
     * cuoi iItems nen chet dau tien. Day chinh la ly do "tao Than Khi xong,
     * logout vao lai la mat mon do", va cung lam mat luon phan model o cuoi
     * chuoi. +16 la bien do cho 3 o dac biet, 6 mon model va vai duong them
     * item khong kiem tra day tui (vd ThoRenThanBi.CreateThanKhi).
     */
    public static final int MAX_ENTRY_INVEN_LOAD = 42 * MAX_BAG_LIMIT + 16;

    // Than method (180 dong) da chuyen sang CharTrangBi.java. Chu ky GIU NGUYEN.
    public void setitemInvenBag(final int place) {
        CharTrangBi.setitemInvenBag(this, place);
    }

    // Than method (275 dong) da chuyen sang CharTrangBi.java. Chu ky GIU NGUYEN.
    public void setItemWearing() {
        CharTrangBi.setItemWearing(this);
    }

    // Than method (223 dong) da chuyen sang CharNapDuLieu.java. Chu ky GIU NGUYEN.
    public void initPinfo() {
        CharNapDuLieu.initPinfo(this);
    }

    public void checkCreateCharHire() {
        try {
            if (this.timeExistCharHire > 0L) {
                if (System.currentTimeMillis() - this.timeExistCharHire < 0L) {
                    this.doCreateCharHire(this.classlinh, this.lvlinh, this.genderlinh, this.lvlinhthue);
                } else {
                    this.timeExistCharHire = 0L;
                    this.lvlinh = 0;
                    this.lvlinhthue = 0;
                    this.classlinh = 0;
                    this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hộ vệ của bạn đã hết hạn"));
                    Database.instance.saveOrtherLog("", this.charname, "linh thue het han:c= " + this.classlinh + ",g=" + this.genderlinh + ",lvl=" + this.lvlinhthue + ",lvcl=" + this.lvlinh, "lhethan");
                }
            }
        } catch (final Exception ex) {
        }
    }

    public boolean canUseTheMuaBan() {
        return this.isAdmin || (this.mapID != 17 && this.mapID != 25 && this.mapID != 111 && this.mapID != 118 && this.mapID != 201 && this.mapID != 202 && this.mapID != 203 && this.mapID != 204 && this.mapID != 205 && this.mapID != 207 && this.mapID != 225 && this.mapID != 226 && this.mapID != 0 && this.mapID != 301 && this.mapID != 302 && this.mapID != 303 && this.mapID != 304 && this.mapID != 206 && this.mapID != 208 && this.mapID != 209 && this.mapID != 210 && this.mapID != 70 && this.mapID != 1701 && this.mapID != 1702 && this.mapID != 1703 && this.mapID != 1704 && System.currentTimeMillis() - this.timeUseTheMuaBan >= 0L);
    }

    public void checkItemDuplicate() {
    }

    // Than method (1379 dong) da chuyen sang CharNapDuLieu.java. Chu ky GIU NGUYEN.
    public void initInfoFromDB(final Vector<Item> charItem, final Vector<Date> lastlog) {
        CharNapDuLieu.initInfoFromDB(this, charItem,lastlog);
    }

    void doAddGiftSn() {
        String info = "";
        final int pc = Map.r.nextInt(100);
        if (pc < 20) {
            final int[] potions = this.potions;
            final int n = 69;
            ++potions[n];
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            info = "Nhận được vé quay số";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (pc < 40) {
            final int sl = 50000 + (Map.r.nextInt(5) + 1) * 10000;
            this.addXu(sl, "char 1");
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            info = "Nhận được " + sl + " xu";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (pc < 60) {
            final int idtangtoc = (Map.r.nextInt(100) < 30) ? 8 : 13;
            final int[] potions2 = this.potions;
            final int n2 = idtangtoc;
            ++potions2[n2];
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            info = "Nhận được " + LoginHandler.PORTION_NAME[idtangtoc];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (pc < 80) {
            final int[] potions3 = this.potions;
            final int n3 = 100;
            ++potions3[n3];
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            info = "Nhận được loa";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else {
            final int idgem = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(4)][Map.r.nextInt(5)];
            this.doAddGemItem(idgem, 2, false);
            this.sendMessage(MessageCreator.createCharGemItem(this));
            info = "Nhận được 2 " + Map.gemTemplate[idgem].name;
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "ogsn");
    }

    private void doSetGiftOffline() {
        for (int i = 0; i < GemTemplate.ID_MATERIAL_HIGHT[5].length; ++i) {
            this.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 3, true);
            this.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 3, true);
        }
        this.doAddGemItem(154, 3, true);
        Database.instance.updateRcvGiftTop(this.charname);
        Database.instance.saveOrtherLog("", this.charname, "Nhan qua offline hn", "offline");
        try {
            this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Bạn đã nhận được quà offline"));
        } catch (final IOException ex) {
        }
    }

    public void addPet(final int id) {
        final Pet pet = new Pet(Map.petTemplate[id].idTemplate, Map.petTemplate[id].idImg, this.getIDItem(), 0, this.charDBID, Map.petTemplate[id].name);
        pet.totalMinute = Map.petTemplate[id].timeExpire;
        pet.type = 3;
        this.pet.add(pet);
        pet.timeBuy = System.currentTimeMillis();
        Database.instance.savePet(pet);
    }

    public void addGif83() {
        final Calendar cl = Calendar.getInstance();
        final int day = cl.get(5);
    }

    public void resetTN() {
        final int allBasicPoint = this.lvDetail.getBasicPoint(this.lastLV) + (this.lastLV - 1) * 5;
        this.basepoint = (short) (allBasicPoint - (this.lastLV - 1) * 5);
        this.strength = (short) (Database.basicPoint[this.charClass][0] + (this.lastLV - 1));
        this.agitity = (short) (Database.basicPoint[this.charClass][1] + (this.lastLV - 1));
        this.spirit = (short) (Database.basicPoint[this.charClass][2] + (this.lastLV - 1));
        this.health = (short) (Database.basicPoint[this.charClass][3] + (this.lastLV - 1));
        this.luck = (short) (Database.basicPoint[this.charClass][4] + (this.lastLV - 1));
        if (this.basepoint < 0) {
            this.basepoint = 0;
        }
        this.calculateAttrib();
        try {
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
        } catch (final Exception ex) {
        }
    }

    public void setAutoSkill(final int type) {
    }

    public void setAutoSkillSmall() {
    }

    public void setAutoGetITem(final int type) {
    }

    public void resetKN() {
        for (int i = 0; i < this.skill.length; ++i) {
            if (this.skill[i] > 0) {
                this.skill[i] = 0;
            }
        }
        if (this.lastLV >= 3) {
            this.skill[0] = 1;
        }
        this.skillpoint = (short) (this.lastLV - 1);
        if (this.skillpoint < 0) {
            this.skillpoint = 0;
        }
        try {
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
        } catch (final Exception ex) {
        }
    }

    public void resetKNDownlv() {
        for (int i = 0; i < this.skill.length; ++i) {
            if (this.skill[i] > 0) {
                this.skill[i] = 0;
            }
        }
        if (this.lastLV > 3) {
            this.skill[0] = 1;
        }
        this.skillpoint = (short) (this.lvDetail.lv - 1);
        if (this.skillpoint < 0) {
            this.skillpoint = 0;
        }
    }

    public static String[] split(String original, final String separator) {
        Vector<String> nodes = new Vector();

        for (int index = original.indexOf(separator); index >= 0; index = original.indexOf(separator)) {
            nodes.addElement(original.substring(0, index));
            original = original.substring(index + separator.length());
        }

        nodes.addElement(original);
        String[] result = new String[nodes.size()];
        if (nodes.size() > 0) {
            for (int loop = 0; loop < nodes.size(); ++loop) {
                result[loop] = (String) nodes.elementAt(loop);
            }
        }

        return result;
    }

    public boolean isFullParty() {
        if (this.party.userParty.size() < 5) {
            return false;
        }
        final int[] clazz = new int[5];
        for (int i = 0; i < this.party.userParty.size(); ++i) {
            final Char p = this.party.userParty.get(i);
            final int[] array = clazz;
            final byte charClass = p.charClass;
            ++array[charClass];
            if (clazz[p.charClass] > 1) {
                return false;
            }
        }
        return true;
    }

    public int getMaxLvUserParty() {
        int max = 0;
        for (int i = 0; i < this.party.userParty.size(); ++i) {
            final Char p = this.party.userParty.get(i);
            if (p.lvDetail.lv > max) {
                max = p.lvDetail.lv;
            }
        }
        return max;
    }

    // Than method (122 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public void sendItem(final int type, final int idGif) {
        CharVongLap.sendItem(this, type,idGif);
    }

    void bonusTime() {
        isSuKienTet2017();
        try {
            switch (this.rcvGifByHour) {
                case 1: {
                    if (this.millisOnline >= 300000L) {
                        this.millisOnlineStart = System.currentTimeMillis();
                        ++this.rcvGifByHour;
                        this.sendItem(0, 0);
                        break;
                    }
                    break;
                }
                case 2: {
                    if (this.millisOnline >= 900000L) {
                        this.millisOnlineStart = System.currentTimeMillis();
                        ++this.rcvGifByHour;
                        this.sendItem(0, 1);
                        break;
                    }
                    break;
                }
                case 3: {
                    if (this.millisOnline >= 1800000L) {
                        this.millisOnlineStart = System.currentTimeMillis();
                        ++this.rcvGifByHour;
                        this.sendItem(0, 2);
                        break;
                    }
                    break;
                }
                case 4: {
                    if (this.millisOnline >= 2700000L) {
                        this.millisOnlineStart = System.currentTimeMillis();
                        ++this.rcvGifByHour;
                        this.sendItem(0, 3);
                        break;
                    }
                    break;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    void bonusDay() {
        try {
            if (!this.isOnlineToDay) {
                if (this.dateStartOnline.trim().equals("") || this.dateStartOnline.trim().equals("0")) {
                    final Calendar calNow = Calendar.getInstance();
                    final NumberFormat f = new DecimalFormat("00");
                    final int y = calNow.get(1) - 2000;
                    final int m = calNow.get(2);
                    final int d = calNow.get(5);
                    this.dateStartOnline = String.valueOf(f.format(y)) + f.format(m) + f.format(d);
                }
                this.isOnlineToDay = true;
                this.sendItem(2, 0);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    void bonusMonth() {
        try {
            switch (this.rcvGifByMonth) {
                case 1: {
                    if (this.totalDayInMonth >= 3) {
                        ++this.rcvGifByMonth;
                        this.sendItem(3, 0);
                        break;
                    }
                    break;
                }
                case 2: {
                    if (this.totalDayInMonth >= 7) {
                        ++this.rcvGifByMonth;
                        this.sendItem(3, 0);
                        break;
                    }
                    break;
                }
                case 3: {
                    if (this.totalDayInMonth >= 14) {
                        ++this.rcvGifByMonth;
                        this.sendItem(3, 0);
                        break;
                    }
                    break;
                }
                case 4: {
                    if (this.totalDayInMonth >= 26) {
                        ++this.rcvGifByMonth;
                        this.sendItem(3, 0);
                        break;
                    }
                    break;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    void bonusLevel() {
        try {
            switch (this.rcvGifByLv) {
                case 1: {
                    if (this.lvDetail.lv >= 5) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 0);
                        break;
                    }
                    break;
                }
                case 2: {
                    if (this.lvDetail.lv >= 10) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 1);
                        break;
                    }
                    break;
                }
                case 3: {
                    if (this.lvDetail.lv >= 15) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 2);
                        break;
                    }
                    break;
                }
                case 4: {
                    if (this.lvDetail.lv >= 20) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 3);
                        break;
                    }
                    break;
                }
                case 5: {
                    if (this.lvDetail.lv >= 25) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 4);
                        break;
                    }
                    break;
                }
                case 6: {
                    if (this.lvDetail.lv >= 30) {
                        ++this.rcvGifByLv;
                        this.sendItem(1, 5);
                        break;
                    }
                    break;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setPlant(final int idFarm, final int idTree) {
        if (idFarm < this.seedsItem.size()) {
            final SeedTemplate st = Map.seedsTemplate[idTree];
            for (int i = 0; i < this.seedsItem.size(); ++i) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idFarm) {
                    s.idTemplate = (byte) idTree;
                    s.timeLive = System.currentTimeMillis();
                    s.rankTree = (byte) Map.r.nextInt(4);
                    s.lvTree = 0;
                    return true;
                }
            }
        }
        return false;
    }

    public String buyFarm(final int idFarm) {
        if (idFarm < this.seedsItem.size()) {
            if (idFarm > 3 && this.seedsItem.get(idFarm - 1).buy == 0) {
                return "Bạn phải mua các ô đất theo thứ tự";
            }
            int i = 0;
            while (i < this.seedsItem.size()) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idFarm) {
                    if (s.buy == 0) {
                        s.buy = 1;
                        this.map.doSendFarm(this);
                        return "Chúc mừng. bạn đã mua được ô đất " + (idFarm + 1);
                    }
                    return "ô đất đã được mua";
                } else {
                    ++i;
                }
            }
        }
        return "Có lỗi khi mua ô đất";
    }

    public boolean checkAvaliableFarm(final int idFarm) {
        if (idFarm < this.seedsItem.size()) {
            for (int i = 0; i < this.seedsItem.size(); ++i) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idFarm) {
                    return s.checkAvaliable();
                }
            }
        }
        return false;
    }

    public String doHarvest(final int idFarm) {
        if (idFarm < this.seedsItem.size()) {
            for (int i = 0; i < this.seedsItem.size(); ++i) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idFarm) {
                    return s.doHavest(this);
                }
            }
        }
        return "Có lỗi khi thu hoạch";
    }

    public boolean doDelTreePlot(final int idPlot) {
        if (idPlot < this.seedsItem.size()) {
            for (int i = 0; i < this.seedsItem.size(); ++i) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idPlot) {
                    final int idtree = s.idTemplate;
                    final int lv = s.lvTree;
                    s.delTree();
                    Database.instance.saveOrtherLog("tob_log_farm", this.charname, String.valueOf(s.idFarm + 1) + " cay " + idtree + " lv = " + lv, "dt");
                    return true;
                }
            }
        }
        return false;
    }

    public String doUpLvPlot(final int idPlot) {
        if (idPlot < this.seedsItem.size()) {
            for (int i = 0; i < this.seedsItem.size(); ++i) {
                final SeedItem s = this.seedsItem.get(i);
                if (s.idFarm == idPlot) {
                    return s.upLevelPlot(this);
                }
            }
        }
        return "Có lỗi khi nâng cấp ô đất.";
    }

    public byte getLvFarm() {
        return 1;
    }

    public byte getLvStore() {
        return 1;
    }

    public byte getLvMainHouse() {
        return 1;
    }

    public SeedItem getFram(final int idFarm) {
        for (int i = 0; i < this.seedsItem.size(); ++i) {
            if (this.seedsItem.get(i).idFarm == idFarm) {
                return this.seedsItem.get(i);
            }
        }
        return null;
    }

    public void getTreeID(final int idFarm) {
        final SeedItem farm = this.getFram(idFarm);
        if (farm != null) {
            for (int i = 0; i < Map.seedsTemplate.length; ++i) {
                final SeedTemplate s = Map.seedsTemplate[i];
                if (s.sell == 1 && this.lvDetail.lv >= s.lvChar && farm.lvPlot >= s.lvFarm && farm.buy == 1 && !this.treeID.contains(s.id)) {
                    this.treeID.add(s.id);
                }
            }
        }
    }

    public String getInfoWebWearing() {
        this.infoWearing = new StringBuilder().append(this.myCountry).toString();
        String result = "";
        try {
            final String[] data = {"-1,-1", "-1,-1", "2," + this.headStyle, "-1,-1", "", ""};
            final byte[] typeClient = {1, 0, 3};
            int currentWP = -1;
            int currentSkillType = -1;
            int weaIndex = -1;
            int typeWeapone = -1;
            final int classChar = -1;
            Vector<Item> charwearing = new Vector<Item>();
            if (this.getCharWearing().size() == 0) {
                charwearing = MessageCreator.createItem(this.gender, this.charClass);
            } else {
                charwearing = this.getCharWearing();
            }
            for (int i = 0; i < charwearing.size(); ++i) {
                final Item item = charwearing.get(i);
                if (!Map.isModelClothes(item.atb[9]) && (item.getType() == 0 || item.getType() == 1 || item.getType() == 2)) {
                    data[typeClient[item.getType()]] = String.valueOf(typeClient[item.getType()]) + "," + item.getTemplate().style;
                }
                this.infoWearing = String.valueOf(this.infoWearing) + "," + item.getTemplate().id + "," + item.clazz + "," + item.level + "," + item.plus;
                if (Map.isWeapone(item.getType())) {
                    currentSkillType = 0;
                    currentWP = item.getType() - 3;
                    weaIndex = item.getTemplate().color;
                    typeWeapone = item.getTemplate().style;
                    data[4] = String.valueOf(currentWP) + "," + currentSkillType + "," + weaIndex + "," + typeWeapone + "," + this.charClass;
                }
            }
            int idImgPet = -1;
            int typePet = -1;
            int nFrame = 0;
            if (this.petUsing != null) {
                idImgPet = this.petUsing.idImg;
                typePet = this.petUsing.type;
                nFrame = this.petUsing.getPetTemplate().nFrame;
                data[5] = String.valueOf(idImgPet) + "," + typePet + "," + nFrame;
            }
            result = data[0];
            for (int j = 1; j < 6; ++j) {
                result = String.valueOf(result) + "," + data[j];
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAccountName() {
        String st = "";
        try {
            st = this.session.username;
        } catch (final Exception ex) {
        }
        return st;
    }

    public String[] getAllItemInfoSave() {
        final String[] st = {"", "", "", ""};
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < this.wItems[i].length; ++j) {
                if (this.wItems[i][j] != null) {
                    final String[] array = st;
                    final int n = 0;
                    array[n] = String.valueOf(array[n]) + this.wItems[i][j].getDBInfo() + "|" + this.wItems[i][j].getAttribute() + ">";
                }
            }
            final String[] array2 = st;
            final int n2 = 0;
            array2[n2] = String.valueOf(array2[n2]) + "@";
        }
        for (int k = 0; k < this.iItems.size(); ++k) {
            final String[] array3 = st;
            final int n3 = 1;
            array3[n3] = String.valueOf(array3[n3]) + this.iItems.get(k).getDBInfo() + "|" + this.iItems.get(k).getAttribute() + ">";
        }
        for (int k = 0; k < this.bItem.size(); ++k) {
            final String[] array4 = st;
            final int n4 = 2;
            array4[n4] = String.valueOf(array4[n4]) + this.bItem.get(k).getDBInfo() + "|" + this.bItem.get(k).getAttribute() + ">";
        }
        for (int k = 0; k < this.awItems.size(); ++k) {
            final String[] array5 = st;
            final int n5 = 3;
            array5[n5] = String.valueOf(array5[n5]) + this.awItems.get(k).getDBInfo() + "|" + this.awItems.get(k).getAttribute() + ">";
        }
        return st;
    }

    public void addItem2Wearing(final Item it) {
    }

    public Item[] getArrayCharWearing() {
        return this.wItems[this.slotWearing];
    }

    public void removeItemOutWearing(final int type, final int posnhan) {
        this.wItems[this.slotWearing][getIndexItemWearing(type, posnhan)] = null;
    }

    public Vector<Item> getCharWearing() {
        final Vector<Item> item = new Vector<Item>();
        for (int i = 0; i < this.wItems[this.slotWearing].length; ++i) {
            if (this.wItems[this.slotWearing][i] != null) {
                item.add(this.wItems[this.slotWearing][i]);
            }
        }
        return item;
    }

    public Vector<Item> changePosRing(final Vector<Item> witem) {
        final byte[] pos = new byte[2];
        byte index = 0;
        for (byte i = 0; i < witem.size(); ++i) {
            final Item it = witem.get(i);
            if (it.getType() == 8) {
                final byte[] array = pos;
                final byte b = index;
                index = (byte) (b + 1);
                array[b] = i;
                if (index >= 2) {
                    break;
                }
            }
        }
        if (index == 2) {
            final Item it2 = witem.get(pos[0]);
            final Item it3 = witem.get(pos[1]);
            if (it2.pos == 0 && it3.pos == 0) {
                it2.pos = 1;
            }
            if (it2.pos == 1 && it3.pos == 1) {
                it3.pos = 0;
            }
            if (it2.pos < it3.pos) {
                witem.setElementAt(it3, pos[0]);
                witem.setElementAt(it2, pos[1]);
            }
        } else if (index == 1) {
            witem.get(pos[0]).pos = 1;
        }
        return witem;
    }

    public Vector<Item> getItemResetSock() {
        this.itemResetSock.removeAllElements();
        final Vector<Item> witem = this.getCharWearing();
        this.itemResetSock = new Vector<Item>();
        for (byte i = 0; i < witem.size(); ++i) {
            final Item it = witem.get(i);
            if (it.nhadSock > 0) {
                this.itemResetSock.add(it);
            }
        }
        return this.itemResetSock;
    }

    public Vector<Item> getCharWearing(final int slotWearing) {
        final Vector<Item> item = new Vector<Item>();
        for (int i = 0; i < this.wItems[slotWearing].length; ++i) {
            if (this.wItems[slotWearing][i] != null) {
                item.add(this.wItems[slotWearing][i]);
            }
        }
        return item;
    }

    public boolean checkExistAnimal(final int dbid) {
        for (int i = 0; i < this.animal.size(); ++i) {
            if (this.animal.get(i).dbid == dbid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Animal getAnimalRide() {
        return this.animalRide;
    }

    // Than method (111 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public Vector<Item> getAnimalWearing() {
        return CharVongLap.getAnimalWearing(this);
    }

    public boolean isPublicMap() {
        return this.map.isPublicMap();
    }

    public void createGifLixi() {
        if (isFinishAllSuKienTet2017()) {
            return;
        }
        this.idOpen.removeAllElements();
        this.nSpecialBox = 2;
        final byte nBox = 9;
        byte id = 0;
        for (byte i = 0; i < nBox - 2; ++i) {
            id = (byte) Map.r.nextInt(3);
            final InfoGifLucky info = new InfoGifLucky(id, this.getType());
            info.createGifGold(false, (byte) ((nBox - 5) / 2));
            this.gifLuckyBox.add(info);
        }
        for (byte i = 0; i < 2; ++i) {
            final InfoGifLucky info = new InfoGifLucky((byte) 2, this.getType());
            info.createGifGold(true, (byte) ((nBox - 5) / 2));
            this.gifLuckyBox.add(info);
        }
    }

    public boolean createGifLuckyBox(final byte nBox, final byte type) {
        this.idOpen.removeAllElements();
        if (this.gifLuckyBox.size() > 2) {
            return false;
        }
        this.nSpecialBox = 2;
        byte id = 0;
        for (byte i = 0; i < nBox - 2; ++i) {
            if (type == 0) {
                id = (byte) Map.r.nextInt(3);
                final InfoGifLucky info = new InfoGifLucky(id, type);
                info.createGifSilver(false, (byte) ((nBox - 5) / 2));
                this.gifLuckyBox.add(info);
            } else if (type == 1) {
                id = (byte) Map.r.nextInt(3);
                final InfoGifLucky info = new InfoGifLucky(id, type);
                info.createGifGold(false, (byte) ((nBox - 5) / 2));
                this.gifLuckyBox.add(info);
            }
        }
        if (type == 0) {
            for (byte i = 0; i < 2; ++i) {
                if (Map.r.nextInt(100) < 90) {
                    final InfoGifLucky info = new InfoGifLucky((byte) 2, type);
                    info.createGifSilver(true, (byte) ((nBox - 5) / 2));
                    this.gifLuckyBox.add(info);
                } else {
                    final InfoGifLucky info = new InfoGifLucky((byte) 3, type);
                    info.createGifSilver(true, (byte) ((nBox - 5) / 2));
                    this.gifLuckyBox.add(info);
                }
            }
        } else if (type == 1) {
            for (byte i = 0; i < 2; ++i) {
                if (Map.r.nextInt(100) < 50) {
                    final InfoGifLucky info = new InfoGifLucky((byte) 3, type);
                    info.createGifGold(true, (byte) ((nBox - 5) / 2));
                    this.gifLuckyBox.add(info);
                } else {
                    final InfoGifLucky info = new InfoGifLucky((byte) 2, type);
                    info.createGifGold(true, (byte) ((nBox - 5) / 2));
                    this.gifLuckyBox.add(info);
                }
            }
        }
        return true;
    }

    public void doCreatePetMax(final int idTemplate, final int level, final int idtemplatetrung, final int time) {
        ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(new Integer(idTemplate));
        Item newItem = null;
        newItem = new Item(template, false, 0, 0, template.id);
        newItem.id = this.getIDItem();
        newItem.owner = this.charDBID;
        newItem.level = (short) level;
        newItem.identify = this.charDBID;
        newItem.clazz = 0;
        newItem.doAddOptionPet(template.atb[0]);
        newItem.dateCreate = getDayTime(0L);
        newItem.otherAtt[4] = 30;
        template = (ItemTemplates) Map.itemTemplates.get(0).get(new Integer(idtemplatetrung));
        this.createPet(template.atb[1], time, newItem);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        this.sendMessage(MessageCreator.createServerAlertMessage("Tạo pet thành công", ""));
    }

    public void doCreatePetBachThu(final int time, final boolean isShow) {
        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(727);
        Item newItem = null;
        newItem = new Item(template, false, 0, 0, template.id);
        newItem.id = this.getIDItem();
        newItem.owner = this.charDBID;
        newItem.level = newItem.getTemplate().level;
        if (time > 0) {
            newItem.minuteExist = time;
            newItem.timeLoan = System.currentTimeMillis();
        }
        newItem.identify = this.charDBID;
        newItem.clazz = 0;
        newItem.doAddOptionPet(template.atb[0]);
        newItem.dateCreate = getDayTime(0L);
        this.createPet(29, newItem.minuteExist, newItem);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        if (isShow) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Nhận được Bạch Thử", ""));
        }
    }

    /**
     * Trung Thu 2026 (moi 19/09/2026): pet Capybara - nhai y het doCreatePetBachThu() ben
     * tren nhung dung item 873 ("Capybara", data_item) + petTemplate idTemplate=30 (Map.java).
     */
    public void doCreatePetCapybara(final int time, final boolean isShow) {
        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(873);
        Item newItem = null;
        newItem = new Item(template, false, 0, 0, template.id);
        newItem.id = this.getIDItem();
        newItem.owner = this.charDBID;
        newItem.level = newItem.getTemplate().level;
        if (time > 0) {
            newItem.minuteExist = time;
            newItem.timeLoan = System.currentTimeMillis();
        }
        newItem.identify = this.charDBID;
        newItem.clazz = 0;
        newItem.doAddOptionPet(template.atb[0]);
        newItem.dateCreate = getDayTime(0L);
        this.createPet(30, newItem.minuteExist, newItem);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        if (isShow) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Nhận được Capybara", ""));
        }
    }

    public synchronized void doCreatePet(final Item itemtrung) {
        if (!this.iItems.contains(itemtrung)) {
            return;
        }

        final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(new Integer(itemtrung.getTemplate().idItemUpLevel));
        Item newItem = null;
        newItem = new Item(template, false, 0, 0, template.id);
        newItem.id = this.getIDItem();
        newItem.owner = this.charDBID;
        newItem.level = newItem.getTemplate().level;
        if (itemtrung.minuteExist > 0) {
            newItem.minuteExist = itemtrung.minuteExist;
            newItem.timeLoan = itemtrung.timeLoan;
        }
        newItem.identify = this.charDBID;
        newItem.clazz = 0;
        newItem.doAddOptionPet(template.atb[0]);
        newItem.dateCreate = getDayTime(0L);
        this.iItems.remove(itemtrung);
        final Pet p = this.createPet(itemtrung.getTemplate().atb[1], itemtrung.minuteExist, newItem);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        this.sendMessage(MessageCreator.createServerAlertMessage("Tạo pet thành công", ""));
        if (itemtrung.minuteExist > 0 && Map.r.nextInt(100) < 1) {
            try {
                p.doUpdrage2MaxByAdmin(this, 4);
                p.itemPet.level = 2;
                Database.instance.savePet(p);
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
                Database.instance.saveOrtherLog("", this.charname, "tao pet " + itemtrung.getName() + " co thoi han va dc nang cap len cap cuoi", "taopcc");
            } catch (final Exception ex) {
            }
        }
    }

    public void doNangCapHoaKyLan(final Item itemHoaKyLan, final int idHoaThach) {
        // player.itemTam (tham chieu duoc MapDoMenu case -513 giu qua nhieu goi tin) khong bao
        // gio duoc xac thuc lai la con trong tui. Neu goi lai ham nay voi CUNG doi tuong sau khi
        // mot lan da nang cap thanh cong (item goc da bi remove khoi iItems o duoi), template cua
        // no van con idItemUpLevel > -1 -> lai rung 10%/1% -> lai tao them mot mon cap tren MOI,
        // trong khi mon goc chi mat MOT lan. Kiem con trong tui truoc khi lam bat cu gi.
        if (!this.iItems.contains(itemHoaKyLan)) {
            return;
        }
        if (itemHoaKyLan.getTemplate().idItemUpLevel > -1) {
            int pcUp = 1;
            if (idHoaThach == GemTemplate.HOA_THACH_C2) {
                pcUp = 10;
            }
            String infoLog = String.valueOf(idHoaThach) + "|" + itemHoaKyLan.getName() + "_" + itemHoaKyLan.getInfoSave();
            if (Map.r.nextInt(100) < pcUp) {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(0).get(new Integer(itemHoaKyLan.getTemplate().idItemUpLevel));
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                newItem.clazz = 0;
                newItem.doAddOptionThanThu(template.atb[0]);
                newItem.dateCreate = getDayTime(0L);
                this.iItems.remove(itemHoaKyLan);
                infoLog = String.valueOf(infoLog) + " nang cap thanh cong";
                this.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thành công", ""));
            } else {
                this.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thất bại", ""));
                infoLog = String.valueOf(infoLog) + " nang cap that bai";
            }
            if (this.listGemitemLock[idHoaThach] > 0) {
                this.delGem(idHoaThach, 1, true);
            } else {
                this.delGem(idHoaThach, 1, false);
            }
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            this.sendMessage(MessageCreator.createCharGemItem(this));
            Database.instance.saveOrtherLog("", this.charname, infoLog, "nangcaphkl");
        }
    }

    /** Gia (luong) va han (ms) cua "Ao hoa Hoa Ky Lan". Xem doAoHoaHoaKyLan. */
    public static final int GIA_LUONG_AO_HOA_HOA_KY_LAN = 100;
    public static final long HAN_AO_HOA_HOA_KY_LAN_MS = 7L * 24 * 60 * 60 * 1000L;

    public boolean dangAoHoaHoaKyLan() {
        return this.timeAoHoaHoaKyLan - System.currentTimeMillis() > 0L;
    }

    /** So ngay con lai cua "Ao hoa Hoa Ky Lan", chi goi khi dangAoHoaHoaKyLan() = true. */
    public long conLaiNgayAoHoaHoaKyLan() {
        return (this.timeAoHoaHoaKyLan - System.currentTimeMillis()) / (24L * 60L * 60000L);
    }

    /**
     * Thoi gian con lai cua "Ao hoa Hoa Ky Lan" dang chu, ngay + gio (theo yeu cau 02/09/2026
     * "đếm ngày + thời gian"): "6 ngày 23 giờ"; duoi 1 ngay: "5 giờ 12 phút"; duoi 1 gio:
     * "12 phút"; duoi 1 phut: "dưới 1 phút". Dung chung cho dong trong tui do
     * (Item.getCharSeal(Char)) va nhan menu NPC tho ren than bi (RequestNPCInfoHandler) de hai
     * cho khong lech nhau. Chi goi khi dangAoHoaHoaKyLan() = true.
     */
    public String conLaiAoHoaHoaKyLanText() {
        long conMs = this.timeAoHoaHoaKyLan - System.currentTimeMillis();
        if (conMs < 0L) {
            conMs = 0L;
        }
        final long ngay = conMs / (24L * 60L * 60000L);
        final long gio = (conMs / (60L * 60000L)) % 24L;
        final long phut = (conMs / 60000L) % 60L;
        if (ngay > 0L) {
            return ngay + " ngày " + gio + " giờ";
        }
        if (gio > 0L) {
            return gio + " giờ " + phut + " phút";
        }
        return phut > 0L ? phut + " phút" : "dưới 1 phút";
    }

    /**
     * Chi so KIEU PHAN TRAM cua Hoa Ky Lan dang mac (otherAtt cua item), cong them ao
     * hoa neu dang bat. Dung cho khang bang/set/doc/lua lan (CharChiSo) va sat thuong
     * theo % tan cong chu / % maxhp doi thu (data.CharThanthu). Tra ve 0 neu khong mac
     * Hoa Ky Lan nao.
     *
     * ao hoa CONG THANG 10 (10 diem phan tram), khong nhan 1.1: dung y "kháng độc lan
     * 5% > 15%" trong yeu cau goc - nhan 1.1 se ra 5.5%, khong phai 15%.
     */
    public int chiSoPhanTramHoaKyLan(final int idxOtherAtt) {
        if (this.itemHoaKyLan == null) {
            return 0;
        }
        final int co = this.itemHoaKyLan.otherAtt[idxOtherAtt];
        return this.dangAoHoaHoaKyLan() ? congAoHoaPhanTram(idxOtherAtt, co) : co;
    }

    /**
     * Chi so KIEU TUYET DOI (khong phai %, vi du suc khoe) cua Hoa Ky Lan dang mac, ao
     * hoa thi +10%. Tra ve 0 neu khong mac Hoa Ky Lan nao.
     */
    public int chiSoTuyetDoiHoaKyLan(final int idxOtherAtt) {
        if (this.itemHoaKyLan == null) {
            return 0;
        }
        final int co = this.itemHoaKyLan.otherAtt[idxOtherAtt];
        return this.dangAoHoaHoaKyLan() ? congAoHoaTuyetDoi(co) : co;
    }

    /**
     * Cac o otherAtt cua Hoa Ky Lan (Item.doAddOptionThanThu) va cong thuc ao hoa - MOT NGUON
     * duy nhat, dung chung cho luc DANH (chiSo*HoaKyLan o tren, CharChiSo, data.CharThanthu)
     * va luc HIEN THI trong tui do (Item.getInfoAtbItem(Char)). Truoc 02/09/2026 tui do gui
     * so goc nen nguoi choi "khong thay cong" du ao hoa da chay trong tran.
     *
     * Yeu cau goc: "kháng độc lan 5% > 15%", "st theo chủ 30% > 40%", "st max hp 20% > 30%",
     * "sức khỏe 1000 > 1100" - tuc +10 DIEM PHAN TRAM cho chi so %, +10% cho suc khoe. Nhung
     * cac o KHONG cung thang do (xac nhan 02/09/2026 qua data_attribute.ispercent va cach dung
     * trong tran):
     *   - 24/25/26/31 bon khang lan: ispercent=2, tran dung Map.r.nextInt(1000) < khang
     *     (CharChiSo/Char) -> don vi PHAN NGHIN. Gia tri goc 50 = 5,0%. Muon +10 diem phan tram
     *     phai cong +100 (truoc do cong +10 = chi +1,0%, ca hien thi lan tran deu sai so voi
     *     yeu cau, nguoi choi nhin 5,0% -> 6,0% "khong thay cong").
     *   - 38 sat thuong % tan cong chu, 40 sat thuong % maxhp doi thu: ispercent=1, tran chia
     *     /100 -> don vi PHAN TRAM. Cong +10.
     *   - 39 suc khoe Than Thu: tuyet doi, +10%.
     * O nao dang bang 0 (Hoa Ky Lan cap 1/2 khong co o 40) thi KHONG cong: "+10% cac chi so cua
     * HKL" la chi so no CO. Truoc do cap 1/2 dang ao hoa bi cong nham 10% maxhp tu so 0 trong
     * tran ma tui do lai khong hien dong nao (getInfoAtbItem bo qua o = 0).
     */
    public static final int[] OTHER_ATT_HOA_KY_LAN_PHAN_TRAM = {24, 25, 26, 31, 38, 40};
    public static final int OTHER_ATT_HOA_KY_LAN_SUC_KHOE = 39;
    /** Bon o khang lan do bang phan nghin (1000 = 100%), nen +10 diem phan tram = +100. */
    private static final int[] OTHER_ATT_HOA_KY_LAN_PHAN_NGHIN = {24, 25, 26, 31};

    public static int congAoHoaPhanTram(final int idxOtherAtt, final int co) {
        if (co <= 0) {
            return co;
        }
        for (final int i : OTHER_ATT_HOA_KY_LAN_PHAN_NGHIN) {
            if (i == idxOtherAtt) {
                return co + 100;
            }
        }
        return co + 10;
    }

    public static int congAoHoaTuyetDoi(final int co) {
        return co <= 0 ? co : co + co / 10;
    }

    public static boolean laOtherAttHoaKyLanPhanTram(final int idx) {
        for (final int i : OTHER_ATT_HOA_KY_LAN_PHAN_TRAM) {
            if (i == idx) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gan MOT Hoa Ky Lan len nguoi: huy Than Thu cu (neu co), tra vat pham cu ve tui,
     * tao Than Thu moi theo cap cua item MOI. Dung chung cho ca nut "Sử dụng" (Map.java
     * doUseItem, MapDoMenu case -513) lan doAoHoaHoaKyLan ben duoi.
     *
     * PHAI gan itemHoaKyLan = item TRUOC khi goi doCreateCharThanThu: ham do doc
     * chiSoTuyetDoiHoaKyLan(39) qua chinh itemHoaKyLan de lay suc khoe Than Thu, nen
     * item moi phai san sang truoc.
     */
    public void doTrangBiHoaKyLan(final Item item) {
        // Cung mot dang tham chieu treo nhu doNangCapHoaKyLan: neu "item" (player.itemTam) da
        // roi khoi tui giua chung (vua nang cap thanh cong, gui vao kho, giao dich...) thi
        // remove tra false ma ban cu van tiep tuc gan itemHoaKyLan = item - mon KHONG con
        // trong iItems nhung van duoc luu rieng qua cot itemHoaKyLan (DatabaseNhanVat), sinh
        // ra hai ban ghi cho cung mot vat pham.
        if (!this.iItems.remove(item)) {
            return;
        }
        this.charThanThuDissapear();
        if (this.itemHoaKyLan != null) {
            this.iItems.add(this.itemHoaKyLan);
        }
        this.itemHoaKyLan = item;
        this.doCreateCharThanThu(item.getTemplate().atb[0]);
        this.calculateAttrib();
        try {
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        } catch (final Exception ex) {
        }
    }

    /**
     * "ẢO HÓA HỎA KỲ LÂN" - theo yeu cau nguoi dung ngay 02/09/2026.
     *
     * Cong thuc: Hoa Ky Lan (VINH VIEN, minuteExist <= 0) + 100 luong -> +10% (hoac +10
     * neu chi so do la %) MOI chi so cua Hoa Ky Lan (4 khang lan + sat thuong theo chu +
     * sat thuong max hp cua doi thu + suc khoe Than Thu) trong 7 ngay. Het han tu dong
     * tro ve chi so goc, khong can dang nhap lai (xem checkExpireAoHoaHoaKyLan, goi tu
     * CharVongLap moi 5 phut giong checkExpirePet).
     *
     * Neu item chua duoc mac thi mac luon (dung chung logic voi nut "Sử dụng") - "ảo
     * hóa" mot Hoa Ky Lan dang nam trong tui la vo nghia neu khong mang len nguoi.
     */
    public void doAoHoaHoaKyLan(final Item item) {
        // item (player.itemTam) co the da roi tui giua chung (xem doNangCapHoaKyLan). Kiem
        // truoc khi tru luong - khong thi nguoi choi mat 100 luong ma khong duoc ao hoa gi (vi
        // doTrangBiHoaKyLan o duoi se tu choi mon la vat, khong phai loi mat tien cho ai).
        if (item != this.itemHoaKyLan && !this.iItems.contains(item)) {
            return;
        }
        if (item.minuteExist > 0) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ ảo hóa được Hoả Kỳ Lân vĩnh viễn", ""));
            return;
        }
        if (this.getLuong() < GIA_LUONG_AO_HOA_HOA_KY_LAN) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
            return;
        }
        this.subLuong((long) GIA_LUONG_AO_HOA_HOA_KY_LAN);
        // Dat moc het han TRUOC khi mac (dong duoi): doTrangBiHoaKyLan -> doCreateCharThanThu
        // doc chiSoTuyetDoiHoaKyLan(39) NGAY luc tao Than Thu de lay suc khoe. Dat sau thi
        // Than Thu vua tao thieu +10% suc khoe cho toi lan mac lai ke tiep - da tung lam
        // sai o ban dau, sua lai o day.
        this.timeAoHoaHoaKyLan = System.currentTimeMillis() + HAN_AO_HOA_HOA_KY_LAN_MS;
        if (this.itemHoaKyLan != item) {
            this.doTrangBiHoaKyLan(item);
        } else {
            this.calculateAttrib();
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                // Gui lai tui do: so cua cac Hoa Ky Lan trong tui doi theo co ao hoa
                // (Item.getInfoAtbItem(Char)) - khong gui thi client giu so cu toi lan mo tui sau.
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            } catch (final Exception ex) {
            }
        }
        // Dat "ảo hóa" vao GIUA cau de no viet thuong: font ban client JAR khong ve duoc
        // chu HOA co dau khoi U+1EA0..U+1EF8 (chu "Ả" bi mat han) — xem Map.java, cho tao
        // menu Hoa Ky Lan.
        this.sendMessage(MessageCreator.createServerAlertMessage("Hoả Kỳ Lân đã được ảo hóa thành công, có tác dụng trong 7 ngày", ""));
        Database.instance.saveOrtherLog("", this.charname, item.getInfoSave(), "aohoahkl");
    }

    /** Ao hoa Hoa Ky Lan het han: tro ve chi so goc NGAY khi dang choi. Goi tu CharVongLap moi 5 phut. */
    public void checkExpireAoHoaHoaKyLan() {
        try {
            if (this.timeAoHoaHoaKyLan > 0L && this.timeAoHoaHoaKyLan - System.currentTimeMillis() <= 0L) {
                this.timeAoHoaHoaKyLan = 0L;
                this.calculateAttrib();
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                // Tui do tro ve so goc ngay (xem Item.getInfoAtbItem(Char)).
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                // "ảo hóa" viet thuong, cung ly do font o cho tren.
                this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Hiệu lực ảo hóa Hoả Kỳ Lân đã hết hạn, chỉ số trở về ban đầu"));
                Database.instance.saveOrtherLog("", this.charname, "ao hoa hoa ky lan het han", "aohoahkl");
            }
        } catch (final Exception ex) {
        }
    }

    public String doOpenLuckyBox() {
        if (this.gifLuckyBox.size() <= 2) {
            return "";
        }
        String result = "";
        if (Map.r.nextInt(100) < 2 && this.nSpecialBox > 0) {
            final byte index = (byte) (Map.r.nextInt(this.nSpecialBox) + (this.gifLuckyBox.size() - this.nSpecialBox));
            final InfoGifLucky info = this.gifLuckyBox.get(index);
            this.gifLuckyBox.remove(info);
            result = info.removeGif(this);
            --this.nSpecialBox;
        } else {
            final byte index = (byte) Map.r.nextInt(this.gifLuckyBox.size() - this.nSpecialBox);
            final InfoGifLucky info = this.gifLuckyBox.get(index);
            this.gifLuckyBox.remove(info);
            result = info.removeGif(this);
        }
        return result;
    }

    // Than method (136 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public void doChuc83(final String friendName) {
        CharSuKien.doChuc83(this, friendName);
    }

    public void doOpenLixiNgude() {
        final int[] per = {50, 50, 50, 50, 50, 50, 50, 50, 50};
        final int[] idgift = {108, 115, 122, 129, 136, 69, GemTemplate.DA_NGU_HOP_6, GemTemplate.DA_THUOC_TINH_05, -1};
        final int[] typegift = {6, 6, 6, 6, 6, 4, 6, 6, -1};
        final int[] soluongmax = {1, 1, 1, 1, 1, 5, 1, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 5, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        final int index = getIndexRandom(per);
        if (typegift[index] == -1) {
            final int[] minute = {4320};
            final Item newItem = ItemLuckyDraw.createItemCoat(this, Map.r.nextInt(2), false, minute[Map.r.nextInt(minute.length)]);
            this.iItems.add(newItem);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            info = String.valueOf(info) + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
            this.sendMessage(MessageCreator.createMsgChat(this.id, info));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            this.doAddGemItem(idgift[index], soluong, true);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = this.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            if (idgift[index] == 9) {
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được nhân sâm khi mở bao lì xì"));
                } catch (final IOException ex) {
                }
            }
        } else if (typegift[index] == 3) {
            if (this.isFullInventory()) {
                final int xu = 5000 + Map.r.nextInt(100000);
                this.addXu(xu, "char 4");
                info = String.valueOf(info) + xu + " xu";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                info = String.valueOf(info) + newItem.getName();
            }
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "olixi");
    }

    public void doEatBanhSn() {
        final int[] per = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 1};
        final int[] idgift = {73, 80, 87, 94, 101, 108, 115, 122, 129, 136, 35, 61, -1};
        final int[] typegift = {6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, -1};
        final int[] soluongmax = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 9, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 9, 1};
        String info = "Chúc mừng bạn nhận được ";
        final int index = getIndexRandom(per);
        if (typegift[index] == -1) {
            final int[] minute = {-1};
            final Item newItem = ItemLuckyDraw.createItemCoat(this, Map.r.nextInt(2), false, minute[Map.r.nextInt(minute.length)]);
            this.iItems.add(newItem);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            info = String.valueOf(info) + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
            this.sendMessage(MessageCreator.createMsgChat(this.id, info));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            this.doAddGemItem(idgift[index], soluong, true);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = this.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            if (idgift[index] == 9) {
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được nhân sâm khi mở bao lì xì"));
                } catch (final IOException ex) {
                }
            }
        } else if (typegift[index] == 3) {
            if (this.isFullInventory()) {
                final int xu = 5000 + Map.r.nextInt(100000);
                this.addXu(xu, "char 5");
                info = String.valueOf(info) + xu + " xu";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                info = String.valueOf(info) + newItem.getName();
            }
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "obsn");
    }

    public static void resetLixi() {
        Char.lixilucky[0] = -1;
        Char.daysomaymm = "";
        Char.totalLixiOpened = 0;
        Map.MAX_LI_XI = 100;
    }

    public static synchronized void doOpenLixi2021(final Char p) {
        if (Char.lixilucky[0] == -1) {
            final Vector<Byte> number = new Vector<Byte>();
            for (int i = 0; i < 100; ++i) {
                number.add((byte) (i + 1));
            }
            System.out.print("SO MAY MAN: ");
            for (int i = 0; i < Char.lixilucky.length; ++i) {
                Char.lixilucky[i] = number.remove(Map.r.nextInt(number.size()));
                System.out.print(String.valueOf(Char.lixilucky[i]) + ",");
                Char.daysomaymm = String.valueOf(Char.daysomaymm) + Char.lixilucky[i] + ",";
            }
            System.out.println("");
        }
        ++Char.totalLixiOpened;
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
        final int[] idgift = {119, 9, 93, 95, GemTemplate.NGOC_HUYEN_MINH_1, GemTemplate.NGOC_HUYEN_MINH_2, GemTemplate.NGOC_HUYEN_MINH_3, 676, 677, 10, 11, 12, GemTemplate.BAN_DO_KHO_BAU, GemTemplate.DA_NGU_HOP_4, GemTemplate.DA_NGU_HOP_5, GemTemplate.DA_NGU_HOP_6};
        final int[] typegift = {4, 4, 4, 4, 6, 6, 6, 3, 3, 4, 4, 4, 6, 6, 6, 6};
        final int[] soluongmax = {1, 1, 10, 10, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        if (TeamServer.isServerLocal()) {
            info = String.valueOf(Char.daysomaymm) + " số hộp dc mở: " + Char.totalLixiOpened + ". " + info;
        }
        boolean isXu = false;
        for (int j = 0; j < Char.lixilucky.length; ++j) {
            if (Char.lixilucky[j] == Char.totalLixiOpened) {
                p.addXu(Char.XU_LI_XI[j], "char 6");
                info = String.valueOf(info) + Char.XU_LI_XI[j] + " xu. ";
                isXu = true;
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                break;
            }
        }
        if (!isXu) {
            final int xu = UtilKPAH.getRandomMinMax(10000, 20000);
            p.addXu(xu, "char 6");
            info = String.valueOf(info) + xu + " xu. ";
            isXu = true;
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        }
        final int index = getIndexRandom(per);
        if (typegift[index] == -1) {
            p.addXu(soluongmax[index], "char 6");
            info = String.valueOf(info) + soluongmax[index] + " xu. ";
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            p.doAddGemItem(idgift[index], soluong, false);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            p.sendMessage(MessageCreator.createCharGemItem(p));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = p.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            if (idgift[index] == 9) {
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " đã nhận được nhân sâm khi mở bao lì xì"));
                } catch (final IOException ex) {
                }
            }
        } else if (typegift[index] == 3) {
            if (p.isFullInventory()) {
                final int xu2 = 5000 + Map.r.nextInt(100000);
                p.addXu(xu2, "char 7");
                info = String.valueOf(info) + xu2 + " xu";
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = p.getIDItem();
                newItem.owner = p.charDBID;
                newItem.level = newItem.getTemplate().level;
                p.iItems.add(newItem);
                newItem.identify = p.charDBID;
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                info = String.valueOf(info) + newItem.getName();
            }
        }
        p.sendMessage(MessageCreator.createMsgChat(p.id, info));
        Database.instance.saveOrtherLog("", p.charname, info, "olixi");
    }

    public void doOpenLixi() {
        Map.addXpCharEvent(this, 100000L, false, "char doOpenLixi");
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        final int[] idgift = {82, 9, 93, 95, GemTemplate.NGOC_HUYEN_MINH_1, GemTemplate.NGOC_HUYEN_MINH_2, GemTemplate.NGOC_HUYEN_MINH_3, 676, 677};
        final int[] typegift = {4, 4, 4, 4, 6, 6, 6, 3, 3};
        final int[] soluongmax = {1, 1, 10, 10, 3, 3, 3, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        final int index = getIndexRandom(per);
        if (typegift[index] == -1) {
            this.addXu(soluongmax[index], "char 6");
            info = String.valueOf(info) + soluongmax[index] + " xu";
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            this.doAddGemItem(idgift[index], soluong, false);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = this.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            if (idgift[index] == 9) {
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được nhân sâm khi mở bao lì xì"));
                } catch (final IOException ex) {
                }
            }
        } else if (typegift[index] == 3) {
            if (this.isFullInventory()) {
                final int xu = 5000 + Map.r.nextInt(100000);
                this.addXu(xu, "char 7");
                info = String.valueOf(info) + xu + " xu";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                info = String.valueOf(info) + newItem.getName();
            }
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "olixi");
    }

    public void doOpenHopQuaNoelHong() {
        if (isFinishAllSuKienNoel()) {
            return;
        }
        String info = "";
        if (Map.r.nextInt(100) < 10) {
            Map.addXpCharEvent(this, 300000L, false, "char doOpenHopQuaNoelHong");
            info = "Bạn nhận được 300.000 exp";
        } else {
            int[] idPo;
            int id;
            for (idPo = new int[]{73, 77}, id = idPo[Map.r.nextInt(idPo.length)]; id == this.nHopMut; id = idPo[Map.r.nextInt(idPo.length)]) {
            }
            if (System.currentTimeMillis() - this.timeDropCake1 >= 0L && this.nBinhTra > 0) {
                if (Map.r.nextInt(100) < 50) {
                    id = this.nHopMut;
                    --this.nBinhTra;
                }
                this.setTimeCake1();
            }
            final int[] potions = this.potions;
            final int n = id;
            ++potions[n];
            boolean nhansam = false;
            if (this.potions[74] > 0 && this.potions[77] > 0) {
                final int[] potions2 = this.potions;
                final int n2 = 9;
                ++potions2[n2];
                final int[] potions3 = this.potions;
                final int n3 = 73;
                --potions3[n3];
                final int[] potions4 = this.potions;
                final int n4 = 77;
                --potions4[n4];
                nhansam = true;
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được nhân sâm khi sưu tầm đủ chữ noel"));
                } catch (final IOException ex) {
                }
            }
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            info = "Bạn nhận được 1 " + LoginHandler.PORTION_NAME[id] + (nhansam ? " và 1 nhân sâm" : "");
        }
        if (Map.r.nextInt(100) < 30) {
            final short[] id2 = {82, 93, 95, 84, 100};
            final int[] sl = {1, 500, 500, 1, 1};
            final int index = Map.r.nextInt(id2.length);
            final int[] potions5 = this.potions;
            final short n5 = id2[index];
            potions5[n5] += sl[index];
            info = String.valueOf(info) + " và " + sl[index] + " " + LoginHandler.PORTION_NAME[id2[index]];
        }
        this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", this.charname, info, "hopquahong");
    }
    public static int[] ID_MAT_NA = new int[]{745, 746, 747, 748, 749};

    // Than method (97 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public void doOpenTuiQuaMayManNoel() {
        CharVongLap.doOpenTuiQuaMayManNoel(this);
    }

    public void doDoiQuaDacBietNoel() {
        if (this.isFullInventory()) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
            return;
        }
        for (int i = 0; i < GemTemplate.ID_NGOC_RONG.length; ++i) {
            this.delGem(GemTemplate.ID_NGOC_RONG[i], 1, true);
        }
        final int[] soluongmax = {3, 10, 100, 100, 5, 1, 1, 1, 1, 5, 20, 20, 20, 10, 100, 1, 10, 1, 1, 1};
        final int[] soluongmin = {1, 5, 50, 50, 3, 1, 1, 1, 1, 3, 10, 10, 10, 5, 50, 1, 5, 1, 1, 1};
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 10, 10, 500, 250, 250, 250, 150, 350, 50, 150, 150, 150, 150};
        final int[] idgift = {69, 100, 93, 95, 58, 35, GemTemplate.NGOC_HUYEN_MINH_4, GemTemplate.LKD_30, GemTemplate.LKD_35, GemTemplate.XUONG_C1, GemTemplate.BOT_TRANG, GemTemplate.BOT_XANH, GemTemplate.BOT_DO, GemTemplate.BOT_XANH_LA, GemTemplate.DA_MAY_MAN_C3, GemTemplate.DA_NGU_HOP_6 - 4, -5, -6, -7};
        final int[] typegift = {4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, -4, -5, -6, -7};
        final int sl = Map.r.nextInt(3) + 3;
        String info = "Chúc mừng bạn nhận được ";
        for (int j = 0; j < sl; ++j) {
            final int index = getIndexRandom(per);
            if (typegift[index] == -7) {
                this.doCreatePetBachThu(14400, false);
                info = String.valueOf(info) + " pet bạch thử, ";
            } else if (typegift[index] == -6) {
                final Item it = Map.doBuyModelClothe(this, this.isCharNam() ? 725 : 726, 10, 1);
                info = String.valueOf(info) + " " + it.getName();
            } else if (typegift[index] == -5) {
                final int time = Map.r.nextInt(2) + 1;
                this.createHeo(time, false);
                info = String.valueOf(info) + " đuong khang, ";
            } else if (typegift[index] == -4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int indexTB = Map.r.nextInt(GemTemplate.ID_TU_BINH.length);
                final int idtubinh = GemTemplate.ID_TU_BINH[indexTB][Map.r.nextInt(GemTemplate.ID_TU_BINH[indexTB].length)];
                this.doAddGemItem(idtubinh, soluong, false);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idtubinh].name + ", ";
                this.sendMessage(MessageCreator.createCharGemItem(this));
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                this.doAddGemItem(idgift[index], soluong, false);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idgift[index]].name + ", ";
                this.sendMessage(MessageCreator.createCharGemItem(this));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int[] potions = this.potions;
                final int n = idgift[index];
                potions[n] += soluong;
                info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]] + ", ";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else if (typegift[index] == 3) {
                if (this.isFullInventory()) {
                    final int xu = 5000 + Map.r.nextInt(100000);
                    this.addXu(xu, "char 9");
                    info = String.valueOf(info) + xu + " xu" + ", ";
                    this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                } else {
                    final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template, false, 0, 0, template.id);
                    newItem.id = this.getIDItem();
                    newItem.owner = this.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    this.iItems.add(newItem);
                    newItem.identify = this.charDBID;
                    this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                    info = String.valueOf(info) + newItem.getName();
                }
            }
        }
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
        this.sendMessage(MessageCreator.createCharGemItem(this));
        this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", this.charname, info, "quadacbiet");
    }

    // Than method (97 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public void doDoiQuaDacBietNoel2020() {
        CharVongLap.doDoiQuaDacBietNoel2020(this);
    }

    public static void initINfoCheckTrungItemVV() {
        if (TeamServer.isServerKinhMon()) {
            Char.numberRandom = Map.r.nextInt(62) + 10;
        } else if (TeamServer.isServerHoangMy()) {
            Char.numberRandom = Map.r.nextInt(11) + 10;
        } else if (TeamServer.isServerHoaLu()) {
            Char.numberRandom = Map.r.nextInt(51) + 100;
        } else if (TeamServer.isServerTrangTien()) {
            Char.numberRandom = Map.r.nextInt(23) + 10;
        } else if (TeamServer.isServerDaiLa()) {
            Char.numberRandom = Map.r.nextInt(43) + 10;
        } else if (TeamServer.isServerLocal()) {
            Char.numberRandom = Map.r.nextInt(5) + 10;
        }
    }

    // Than method (108 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public static boolean checkReceiveTrungLinhThuRuongBatbao(final Char p) {
        return CharVongLap.checkReceiveTrungLinhThuRuongBatbao(p);
    }

    public void doOpenRuongBatBao() {
        if (!TeamServer.isServerHoaLu() && !TeamServer.isServerLocal()) {
            return;
        }
        try {
            final int[] trungthu = {86, 70, 74, 72};
            final int[] idgift = {Animal.PHUONG_HOANG, Animal.PHUONG_HOANG_BANG, Animal.HEO, Animal.NGUA_XUONG, 119, -10001, -15001, -20001, -30001, -2};
            final int[] typegift = {-3, -3, -3, -3, 4, -10001, -15001, -20001, -30001, -2};
            final int[] pc = {500, 500, 500, 500, 500, 100, 100, 100, 100, 500};
            final int index = getIndexRandom(pc);
            final boolean istrungtrung = checkReceiveTrungLinhThuRuongBatbao(this);
            System.out.println("index trung " + index);
            if (istrungtrung) {
                final int idtrung = trungthu[Map.r.nextInt(trungthu.length)];
                final int[] potions = this.potions;
                final int n = idtrung;
                ++potions[n];
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + LoginHandler.PORTION_NAME[idtrung]));
                try {
                    Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + this.charname + " mở rương bát bảo trúng 1 " + LoginHandler.PORTION_NAME[idtrung]), -1);
                } catch (final IOException ex) {
                }
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                Database.instance.saveOrtherLog("", this.charname, "trung trung " + LoginHandler.PORTION_NAME[idtrung], "ruongbb");
            } else {
                String info = "nhan dc: ";
                if (typegift[index] == -10001 || typegift[index] == -15001 || typegift[index] == -20001 || typegift[index] == -30001) {
                    final int xu = Map.abs(idgift[index]) - 1;
                    this.addXu(xu, "ruong bb char");
                    info = String.valueOf(info) + xu + " xu";
                    this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + xu + " xu"));
                } else if (typegift[index] == -2) {
                    final Item it = Map.doCreateHoVeLenh(this, 4320);
                    if (it != null) {
                        info = String.valueOf(info) + it.getName();
                        this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + it.getName()));
                    }
                } else if (typegift[index] == -3) {
                    final int[] d = {3, 5, 7};
                    final int day = d[Map.r.nextInt(d.length)];
                    if (idgift[index] == Animal.NGUA_XUONG) {
                        this.createNguaXuong(day, true);
                        info = String.valueOf(info) + " ngua xuong " + day + " ngay";
                    } else if (idgift[index] == Animal.HEO) {
                        this.createHeo(day, true);
                        info = String.valueOf(info) + " duong khang " + day + " ngay";
                    } else {
                        this.createPhuongHoang(day, idgift[index], true);
                    }
                } else if (typegift[index] == 4) {
                    final int[] potions2 = this.potions;
                    final int n2 = idgift[index];
                    ++potions2[n2];
                    info = String.valueOf(info) + " tien dan";
                    this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được Tiên đan"));
                }
                Database.instance.saveOrtherLog("", this.charname, info, "ruongbb");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void doOpenQuaTet() {
        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; ++i) {
            this.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 1, true);
        }
        this.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được nguyên liệu sơ cấp cấp 6 khoá mỗi loại 1 viên", ""));
        this.sendMessage(MessageCreator.createCharGemItem(this));
        Database.instance.saveOrtherLog("", this.charname, "mo hop qua tet, nhan dc nglsc 6 moi loai 1 vien", "quatet");
    }

    /**
     * Trung Thu 2026: BO hoan toan phan cong cung 500.000 exp truoc day (nguoi dung xac nhan
     * hop dac biet CHI con ngau nhien 1 trong bang qua duoi, khong cong them co dinh nua -
     * rieng "EXP luon co" gio la MOT O ngau nhien trong bang, khong phai bonus ngoai bang).
     * Bang qua doi lai theo dung danh sach spec moi.
     *
     * Sua 19/09/2026 (yeu cau nguoi dung, 2 lan): (1) 4 o "manh" cuoi bang truoc day dung
     * SAI loai (kim cang thay vi gang) VA lan sang Sa Tang - nguoi dung xac nhan HIEN CHUA
     * co cac manh Sa Tang/Ngo Khong nen BO HET, chi giu dung 4 manh Bat Gioi da co san
     * (kim co 851, gang 853, giay 852, quan 849) dung nhu spec "Manh Kim Co/Gang Bat Gioi".
     */
    public void doOpenHopBanhTrungThuDacBiet() {
        // Trong so dieu chinh 19/09/2026 (yeu cau nguoi dung): 200.000 exp (idx 13) len
        // 10%, Manh ban do 3 (idx 14) len 8%, Sach ky nang pet (idx 9) va Tien dan (idx
        // 22) giam xuong con 1% moi loai. 24 muc con lai GIU NGUYEN trong so cu (khong
        // dong, chi bi anh huong nhe do tong bang thay doi 12500 -> 12876).
        // Them idx 28 (Long den vinh vien, typegift 13) va idx 29 (Thuoc hoa hinh, typegift
        // 14) 19/09/2026 - 2 muc spec doi hoi ma ban truoc dang thieu hoan toan.
        // Them idx 30 (pet Capybara, typegift 15) 19/09/2026.
        final int[] per      = {400, 400, 600, 600, 600, 600, 500, 500, 800, 129, 500, 500, 300, 1288, 1030, 700, 800, 800, 150, 150, 150, 150, 129, 500, 150, 150, 150, 150, 150, 150, 150};
        final int[] idgift   = {GemTemplate.XUONG_C5, GemTemplate.XUONG_C6, 123, 82, 7, 13, 676, 677, 136, 111, 156, 157, GemTemplate.LKD_40, 0, 117, 0, 93, 95, 12, 19, 20, 0, 119, 100, 851, 853, 852, 849, 0, 0, 0};
        final int[] typegift = {6, 6, 4, 4, 4, 4, 3, 3, 4, 4, 4, 4, 6, 7, 4, 8, 4, 4, 9, 9, 9, 10, 4, 4, 3, 3, 3, 3, 13, 14, 15};
        final int[] soluongmax = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        if (this.isFullInventory()) {
            return;
        }
        String info = "Chúc mừng bạn nhận được: ";
        final int index = getIndexRandom(per);
        if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            this.doAddGemItem(idgift[index], soluong, true);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = this.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 3) {
            if (this.isFullInventory()) {
                final int xu = 5000 + Map.r.nextInt(100000);
                this.addXu(xu, "char 10");
                info = String.valueOf(info) + xu + " xu";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                info = String.valueOf(info) + newItem.getName();
            }
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 7) {
            final long expGift = 200000L;
            Map.addXpCharEvent(this, expGift, false, "char doOpenHopBanhTrungThuDacBiet");
            info = String.valueOf(info) + expGift + " exp";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 8) {
            final int xu = 10000 + Map.r.nextInt(40000);
            this.addXu(xu, "hop banh trung thu dac biet");
            info = String.valueOf(info) + xu + " xu";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 9) {
            this.createPet(idgift[index], 10080);
            info = String.valueOf(info) + "lồng đèn (thú cưỡi thời hạn 7 ngày)";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 10) {
            final Item hvl = Map.doCreateHoVeLenhByLevel(this, 10080);
            info = String.valueOf(info) + (hvl != null ? hvl.getName() : "hộ vệ lệnh");
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 13) {
            // Long den VINH VIEN (moi 19/09/2026, spec doi hoi rieng biet voi 3 o thoi han
            // idx 18-20) - dung dung idTemplate=28 ("Long den hoa sen"), template DUY NHAT
            // co Pet.isPetLongDenvinhVien()==true trong petTemplate.
            this.createPetVinhVien(28);
            info = String.valueOf(info) + "Lồng đèn hoa sen (vĩnh viễn)";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 14) {
            // Thuoc hoa hinh (moi 19/09/2026, GemTemplate id 268).
            this.doAddGemItem(268, 1, true);
            info = String.valueOf(info) + "Thuốc hóa hình (khóa)";
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 15) {
            // Pet Capybara thoi han 7 ngay, co the giao dich (moi 19/09/2026).
            this.doCreatePetCapybara(10080, false);
            info = String.valueOf(info) + "Capybara (7 ngày, có thể giao dịch)";
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "ohqtt");
    }

    public void doOpenHopQuaTrungThu() {
        final int[] per = {500, 500, 500, 500, 500, 500, 500, 1, 500};
        final int[] idgift = {GemTemplate.DA_TIEN_GIAI, GemTemplate.XUONG_C6, GemTemplate.LKD_40, 81, 82, 123, 100, -1, -2};
        final int[] typegift = {6, 6, 6, 4, 4, 4, 4, -1, -2};
        final int[] soluongmax = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        final int[] soluongmin = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        if (this.isFullInventory()) {
            return;
        }
        String info = "Chúc mừng bạn nhận được: ";
        final int index = getIndexRandom(per);
        if (typegift[index] == -1) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int idgem = Map.r.nextInt(31) + 195;
            this.doAddGemItem(idgem, soluong, Map.r.nextInt(100) < 50);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgem].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == -2) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int idgem = Map.r.nextInt(31) + 195;
            this.doAddGemItem(idgem, soluong, Map.r.nextInt(100) < 50);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgem].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            this.doAddGemItem(idgift[index], soluong, true);
            info = String.valueOf(info) + soluongmax[index] + " " + Map.gemTemplate[idgift[index]].name;
            this.sendMessage(MessageCreator.createCharGemItem(this));
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }
            final int[] potions = this.potions;
            final int n = idgift[index];
            potions[n] += soluong;
            info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            if (idgift[index] == 9) {
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được nhân sâm khi mở bao lì xì"));
                } catch (final IOException ex) {
                }
            }
        } else if (typegift[index] == 3) {
            if (this.isFullInventory()) {
                final int xu = 5000 + Map.r.nextInt(100000);
                this.addXu(xu, "char 10");
                info = String.valueOf(info) + xu + " xu";
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            } else {
                final ItemTemplates template = (ItemTemplates) Map.itemTemplates.get(5).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = this.getIDItem();
                newItem.owner = this.charDBID;
                newItem.level = newItem.getTemplate().level;
                this.iItems.add(newItem);
                newItem.identify = this.charDBID;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
                info = String.valueOf(info) + newItem.getName();
            }
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "ohqtt");
    }

    public void doOpenQuaHe() {
        String info = "";
        final int pc = Map.r.nextInt(100);
        if (pc < 30) {
            final short[] arr = (Map.r.nextInt(100) < 50) ? GemTemplate.ID_MATERIAL_LOW[3] : GemTemplate.ID_MATERIAL_LOW[4];
            final int idgem = arr[Map.r.nextInt(arr.length)];
            this.doAddGemItem(idgem, 1, true);
            this.sendMessage(MessageCreator.createCharGemItem(this));
            info = "Nhận được " + Map.gemTemplate[idgem].name;
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (pc < 80 && !this.isFullInventory()) {
            final int[] id = {584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597};
            int lv = this.lvDetail.lv;
            if (lv < 35) {
                lv = 35;
            }
            final int idtemp = id[(lv - 35) / 5];
            final Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(idtemp, this, Map.r.nextInt(2), false);
            newItem.dateCreate = getDayOpen(0L);
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = 1440;
            this.iItems.add(newItem);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            this.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName(), ""));
            info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level + " thoi han " + 1440;
        } else {
            final short[] arr = (Map.r.nextInt(100) < 50) ? GemTemplate.ID_MATERIAL_HIGHT[3] : GemTemplate.ID_MATERIAL_HIGHT[4];
            final int idgem = arr[Map.r.nextInt(arr.length)];
            this.doAddGemItem(idgem, 1, true);
            this.sendMessage(MessageCreator.createCharGemItem(this));
            info = "Nhận được " + Map.gemTemplate[idgem].name;
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
        Database.instance.saveOrtherLog("", this.charname, info, "ogsn");
    }

    public void doOpenAllLuckyBox() {
        if (this.gifLuckyBox.size() <= 2) {
            this.gifLuckyBox.removeAllElements();
            this.idOpen.removeAllElements();
            this.nSpecialBox = 2;
            return;
        }
        String infoGif = "";
        while (this.gifLuckyBox.size() > 2) {
            if (Map.r.nextInt(100) < 2 && this.nSpecialBox > 0) {
                final byte index = (byte) (Map.r.nextInt(this.nSpecialBox) + (this.gifLuckyBox.size() - this.nSpecialBox));
                final InfoGifLucky info = this.gifLuckyBox.get(index);
                infoGif = String.valueOf(infoGif) + info.removeGif(this) + " ";
                this.gifLuckyBox.remove(info);
                --this.nSpecialBox;
            } else {
                final byte index = (byte) Map.r.nextInt(this.gifLuckyBox.size() - this.nSpecialBox);
                final InfoGifLucky info = this.gifLuckyBox.get(index);
                infoGif = String.valueOf(infoGif) + info.removeGif(this) + " ";
                this.gifLuckyBox.remove(info);
            }
        }
        this.sendMessage(MessageCreator.createLuckyBox((byte) 0, null, 2));
        if (!infoGif.trim().equals("")) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được " + infoGif, ""));
        } else {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc bạn may mắn lần sau", ""));
        }
        this.gifLuckyBox.removeAllElements();
        this.idOpen.removeAllElements();
        this.nSpecialBox = 2;
    }

    public Vector<String> getInfoGifLucky() {
        final Vector<String> info = new Vector<String>();
        for (int i = 0; i < this.gifLuckyBox.size(); ++i) {
            final String st = this.gifLuckyBox.get(i).getInfoGif();
            if (!st.equals("")) {
                if (info.size() == 0) {
                    info.add(st);
                } else {
                    info.insertElementAt(st, Map.r.nextInt(info.size()));
                }
            }
        }
        return info;
    }

    public void doBuyNumber(final byte type, final byte number) {
        if (this.xu < 1000000L) {
            return;
        }
        this.xu -= 1000000L;
        final Vector<Veso> temp = this.vesoxu;
        if (this.dayOpenXoso.equals("")) {
            final int time = Calendar.getInstance().get(11);
            if (time >= 17 && time <= 23) {
                this.dayOpenXoso = getDayOpen(32400000L);
            } else {
                this.dayOpenXoso = getDayOpen(0L);
            }
        }
        for (byte size = (byte) temp.size(), i = 0; i < size; ++i) {
            final Veso vs = temp.get(i);
            if (vs.number == number) {
                vs.addNumber();
                Database.instance.saveVeso(this);
                return;
            }
        }
        final Veso vs2 = new Veso();
        vs2.number = number;
        vs2.addNumber();
        temp.add(vs2);
        Database.instance.saveOrtherLog("", this.charname, "Mua vé số " + number, "bxoso");
        Database.instance.saveVeso(this);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
    }

    public void sendInfoVeso(final byte type) {
        String info = "";
        final Vector<Veso> temp = this.vesoxu;
        final byte size = (byte) temp.size();
        if (size == 0) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chưa có vé.", ""));
            return;
        }
        info = "Ngày xổ: " + this.dayOpenXoso + "  ";
        for (byte i = 0; i < size; ++i) {
            final Veso vs = temp.get(i);
            info = String.valueOf(info) + "vé " + vs.number + " sl " + vs.soluong + ", ";
        }
        this.sendMessage(MessageCreator.createServerAlertMessage(info.substring(0, info.length() - 2), ""));
    }

    public static int getCurrentHour() {
        final Calendar cl = Calendar.getInstance();
        final int iHour = cl.get(11);
        final int iMinute = cl.get(12);
        return iHour;
    }

    public static String getDayOpen(final long time) {
        return UserLogger.dateFormat.format(new Date(System.currentTimeMillis() + time));
    }

    public static String getYear(final long time) {
        return UserLogger.dateYearFormat.format(new Date(System.currentTimeMillis() + time));
    }

    public static String getMonth(final long time) {
        return UserLogger.dateMonthFormat.format(new Date(System.currentTimeMillis() + time));
    }

    public static String getDay(final long time) {
        return UserLogger.dateDayFormat.format(new Date(System.currentTimeMillis() + time));
    }

    public static String getDayTime(final long time) {
        final long t = System.currentTimeMillis() + time;
        return Database.timeFormat.format(new Date(t));
    }

    public static String getDayTimeByTime(final long time) {
        return Database.timeFormat.format(new Date(time));
    }

    public void doOpenNumber() {
        final int time = Calendar.getInstance().get(11);
        if (!this.dayOpenXoso.equals("")) {
            if (this.dayOpenXoso.equals(getDayOpen(0L))) {
                if (time >= 17 && time <= 23) {
                    if (Map.winNumber == -1) {
                        Xoso.selectWinNumber();
                    }
                    final String allNumber = this.allNumberBuy();
                    while (this.vesoxu.size() > 0) {
                        final Veso vs = this.vesoxu.remove(0);
                        if (vs.number == Map.winNumber) {
                            long win = 70000000L * vs.soluong;
                            win -= win * 10L / 100L;
                            this.xu += win;
                            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã mua " + vs.soluong + " số " + vs.number + " và trúng được " + win + " xu trong chương trình may mắn đầu năm. Thuế 10%", ""));
                            Database.instance.saveOrtherLog("", this.charname, String.valueOf(win) + "_" + this.xu + "_" + allNumber, "txs");
                            this.vesoxu.removeAllElements();
                            Database.instance.saveVeso(this);
                            this.dayOpenXoso = "";
                            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                            try {
                                Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + this.charname + " đã trúng được " + win + " xu trong chương trình may mắn đầu năm"));
                            } catch (final Exception ex) {
                            }
                            return;
                        }
                    }
                    try {
                        this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Vé không trúng thưởng. Số trúng là số " + Map.winNumber));
                    } catch (final IOException ex2) {
                    }
                    this.dayOpenXoso = "";
                    Database.instance.saveOrtherLog("", this.charname, String.valueOf(this.xu) + "_" + allNumber, "fxs");
                    Database.instance.saveVeso(this);
                }
            } else {
                Database.instance.saveOrtherLog("", this.charname, this.allNumberBuy(), "hethanXS");
                try {
                    this.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Vé bạn mua hôm qua đã hết hạn."));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                this.vesoxu.removeAllElements();
                this.dayOpenXoso = "";
                Database.instance.saveVeso(this);
            }
        }
    }

    public void doSwitch2Old() {
        if (this.vesoxu.size() > 0) {
            for (byte i = 0; i < this.vesoxu.size(); ++i) {
                this.vesoCu.add(this.vesoxu.get(i));
            }
        }
        this.vesoxu.removeAllElements();
        Database.instance.saveVeso(this);
    }

    public static boolean timeBuy() {
        final Calendar cl = Calendar.getInstance();
        final int iHour = cl.get(11);
        return iHour >= 18 || iHour < 16;
    }

    public String allNumberBuy() {
        String info = "";
        final Vector<Veso> temp = this.vesoxu;
        if (temp.size() == 0) {
            return "";
        }
        for (byte size = (byte) temp.size(), i = 0; i < size; ++i) {
            final Veso vs = temp.get(i);
            info = String.valueOf(info) + vs.number + "," + vs.soluong + ",";
        }
        return String.valueOf(info.substring(0, info.length() - 1)) + "|" + this.dayOpenXoso;
    }

    public String oldNumber() {
        String info = "";
        final Vector<Veso> temp = this.vesoCu;
        if (this.vesoCu.size() == 0) {
            return "";
        }
        for (byte size = (byte) temp.size(), i = 0; i < size; ++i) {
            final Veso vs = temp.get(i);
            info = String.valueOf(info) + vs.number + "," + vs.soluong + ",";
        }
        return info.equals("") ? info : info.substring(0, info.length() - 1);
    }

    public boolean canReceiveGif35() {
        return this.lvDetail.lv == 35 && this.thuongLv35 == 0 && this.gif35 == 0;
    }

    public synchronized void doReceiveGif35(final int magic) {
        if (!this.canReceiveGif35()) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Phải đúng cấp 35 và chưa nhận quà cấp 35 mới được nhận thưởng.", ""));
            return;
        }
        MapXuLyLenh.thuongLenCap35(this);
    }

    public void doReceiveGif35Offline(final int magic) {
        final byte[] soluong = {(byte) ((magic == 0) ? 0 : 4), (byte) (4 + ((magic == 0) ? 4 : 0)), 4, (byte) (2 + ((magic == 0) ? 2 : 0)), (byte) ((magic == 0) ? 0 : 2), 2, 0, 0, 2, 2};
        switch (this.charClass) {
            case 0:
            case 1:
            case 3: {
                final byte[] array = soluong;
                final int n = 6;
                array[n] += 4;
                final byte[] array2 = soluong;
                final int n2 = 4;
                array2[n2] += 2;
                break;
            }
            case 2:
            case 4: {
                final byte[] array3 = soluong;
                final int n3 = 0;
                array3[n3] += 4;
                final byte[] array4 = soluong;
                final int n4 = 7;
                array4[n4] += 2;
                break;
            }
        }
        String info = "";
        for (byte i = 0; i < Char.idGif35.length; ++i) {
            if (soluong[i] > 0) {
                info = String.valueOf(info) + soluong[i] + " " + Map.gemTemplate[Char.idGif35[i]].name + " ";
                this.doAddGemItem(Char.idGif35[i], soluong[i], true);
            }
        }
        this.doAddGemItem(240, 10, true);
        this.doAddGemItem(154, 5, true);
        info = String.valueOf(info) + " và 10 ngọc huyền minh cấp 6";
        info = String.valueOf(info) + " và 5 đá ngũ hợp tinh khiết cấp 6";
        this.gifMoonFestival = false;
        this.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + info.trim(), ""));
        Database.instance.saveOrtherLog("", this.charname, info, "gifoffline");
        this.sendMessage(MessageCreator.createCharGemItem(this));
    }

    public void addLockGem(final int id, final int soluong) {
        if (this.listGemitem[id] == 0 && this.listGemitemLock[id] == 0) {
            final GemItem g = new GemItem(id);
            g.ownerId = this.charDBID;
            g.realId = this.getIDItem();
            this.gemItem.add(g);
        }
        final int[] listGemitemLock = this.listGemitemLock;
        listGemitemLock[id] += soluong;
        final int[] allGemGetLock = this.allGemGetLock;
        allGemGetLock[id] += soluong;
    }

    public boolean checkEnoughtGem(final int idGem, final int sl, final int lock) {
        if (lock == 0) {
            return this.listGemitem[idGem] >= sl;
        }
        return this.listGemitemLock[idGem] >= sl;
    }

    // synchronized tren chinh Char nay (giong doReceiveGif35/doStartLuckDraw): cung dang
    // "kiem so luong roi tru" nhu KHOA_TIEN o tren nhung truoc day khong co khoa cho
    // ngoc - checkEnoughtGem() roi delGem() la hai buoc tach roi, hai goi tin gan nhu
    // dong thoi cho cung nhan vat co the cung doc thay du ngoc truoc khi ben nao kip tru
    // (delGem tu kep ve 0 nen KHONG am, nhung phan thuong/ket qua phia sau co the duoc
    // cap 2 lan cho 1 lan tra ngoc that). Noi nao can gop nhieu buoc kiem+tru+ap dung
    // ket qua thanh MOT khoi nguyen tu thi tu bao "synchronized (player) { ... }" quanh
    // ca khoi do - dung chung mot khoa voi ham nay.
    public synchronized boolean truGemNeuDu(final int idGem, final int sl, final int lock) {
        if (!checkEnoughtGem(idGem, sl, lock)) {
            return false;
        }
        delGem(idGem, sl, lock != 0);
        return true;
    }

    public void doAddGemItem(final int id, final int soluong, final boolean lock) {
        if (this.listGemitem[id] == 0 && this.listGemitemLock[id] == 0) {
            final GemItem g = new GemItem(id);
            g.ownerId = this.charDBID;
            g.realId = this.getIDItem();
            this.gemItem.add(g);
        }
        if (lock) {
            final int[] listGemitemLock = this.listGemitemLock;
            listGemitemLock[id] += soluong;
            final int[] allGemGetLock = this.allGemGetLock;
            allGemGetLock[id] += soluong;
        } else {
            final int[] allGemGet = this.allGemGet;
            allGemGet[id] += soluong;
            final int[] listGemitem = this.listGemitem;
            listGemitem[id] += soluong;
        }
    }

    public void delGem(final int id, final int sl, final boolean lock) {
        if (lock) {
            final int[] listGemitemLock = this.listGemitemLock;
            listGemitemLock[id] -= sl;
            final int[] allGemUseLock = this.allGemUseLock;
            allGemUseLock[id] += sl;
            if (this.listGemitemLock[id] < 0) {
                this.listGemitemLock[id] = 0;
            }
        } else {
            final int[] listGemitem = this.listGemitem;
            listGemitem[id] -= sl;
            final int[] allGemUse = this.allGemUse;
            allGemUse[id] += sl;
            if (this.listGemitem[id] < 0) {
                this.listGemitem[id] = 0;
            }
        }
        if (this.listGemitem[id] <= 0 && this.listGemitemLock[id] <= 0) {
            this.listGemitem[id] = 0;
            this.listGemitemLock[id] = 0;
            for (int i = 0; i < this.gemItem.size(); ++i) {
                final GemItem g = this.gemItem.get(i);
                if (g.idGemTemplate == id) {
                    this.gemItem.remove(i);
                    this.ALL_ID_USE.remove(g.realId);
                    break;
                }
            }
        }
    }

    public void doAddGemItem(final int id) {
        if (this.listGemitem[id] == 0 && this.listGemitemLock[id] == 0) {
            final GemItem g = new GemItem(id);
            g.ownerId = this.charDBID;
            g.realId = this.getIDItem();
            this.gemItem.add(g);
        }
        final int[] listGemitem = this.listGemitem;
        ++listGemitem[id];
        final int[] allGemGet = this.allGemGet;
        ++allGemGet[id];
    }

    public void doAddSpecialItem(final int id, final int soluong) {
        final ShopTemplate item = Map.getShopTemplate(id);
        if (this.listSpItem[item.id] == 0) {
            final ShopTemplate spItem = new ShopTemplate();
            spItem.coppy(spItem, item);
            spItem.ownerId = this.charDBID;
            spItem.realId = this.getIDItem();
            this.specialItem.add(spItem);
        }
        final int[] listSpItem = this.listSpItem;
        final short id2 = item.id;
        listSpItem[id2] += soluong;
        this.sendMessage(MessageCreator.createCharSpecialItem(this));
    }

    public void doAddSpecialItem(final int id) {
        final ShopTemplate item = Map.getShopTemplate(id);
        if (this.listSpItem[item.id] == 0) {
            final ShopTemplate spItem = new ShopTemplate();
            spItem.coppy(spItem, item);
            spItem.ownerId = this.charDBID;
            spItem.realId = this.getIDItem();
            this.specialItem.add(spItem);
        }
        final int[] listSpItem = this.listSpItem;
        final short id2 = item.id;
        ++listSpItem[id2];
        this.sendMessage(MessageCreator.createCharSpecialItem(this));
    }

    public void doReceiveGifNap() {
    }

    public byte getIdModel() {
        if (this.itemVukhiThoiTrang != null && this.itemVukhiThoiTrang.isChoiLuaLanBienHinh()) {
            return 0;
        }
        if (this.wModel.wpModel != null && this.wModel.wpModel.isChoiLuaLanBienHinh()) {
            return 0;
        }
        return this.wModel.idModel;
    }

    public boolean hasWearingThanBinhWeapon() {
        // Check all wearing items
        for (Item item : this.wItems[0]) {
            if (item != null) {
                // Check if item is equipped
                if (item.place == Item.PLACE_WEARING) {
                    // Check if item is a weapon (type 3-7)
                    int type = item.getTemplate().type;
                    if (type >= 3 && type <= 7) {
                        // Check if item ID is in range 773-787
                        int id = item.getTemplate().id;
                        if (id >= 773 && id <= 787) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int[] getInfoVukhiThoiTrang() 
    {
        final int[] a = {-1, -1, -1, 1};
        
        if (this.itemVukhiThoiTrang != null) 
        {
            a[0] = this.itemVukhiThoiTrang.getIdEff();
            a[1] = this.itemVukhiThoiTrang.getIndexDxy();
            a[2] = this.itemVukhiThoiTrang.getIdBigImgAvatar();

            if (a[2] == 823 || a[2] == 824 || a[2] == 825 || a[2] == 852 || a[2] == 853 ) 
            {
                a[3] = 5;
            } 
            else if (a[2] == 636 || a[2] == 684 || a[2] == 688) 
            {
                a[3] = 4; 
            } 
            else if (java.util.Arrays.binarySearch(new int[]{640, 641, 648, 652, 656, 660, 664, 668, 675, 679, 683, 692}, a[2]) >= 0) 
            {
                a[3] = 5;
            }
            else if (java.util.Arrays.binarySearch(new int[]{638, 642, 646, 650, 654, 658, 662, 666, 670, 674, 678, 682, 686, 690, 694}, a[2]) >= 0) 
            {
                a[3] = 1;
            }
            else 
            {
                a[3] = 3; 
            }
        }
        else if(this.wModel.wpModel != null && this.wModel.wpModel.isNguyetLinhTruong())
        {
            // Nhanh nay chi vao duoc khi itemVukhiThoiTrang == null (la else cua if tren),
            // nen doc itemVukhiThoiTrang o day la nem NullPointerException. Phai doc wpModel.
            a[0] = this.wModel.wpModel.getIdEff();
            a[1] = this.wModel.wpModel.getIndexDxy();
            a[2] = this.wModel.wpModel.getIdBigImgAvatar();
            a[3] = 3;
        }
        else if (this.wModel.wpModel != null && this.wModel.wpModel.isChoiLuaLanBienHinh()) 
        {
            a[0] = this.wModel.wpModel.getIdEff();
            a[1] = this.wModel.wpModel.getIndexDxy();
            a[2] = this.wModel.wpModel.getIdBigImgAvatar();
            a[3] = 3;
        } 
        else if (this.hasWearingThanBinhWeapon()) 
        {
            int v = a[2];
            int[] set5 = {640, 641, 648, 652, 656, 660, 664, 668, 675, 679, 683, 692};
            int[] set1 = {638, 642, 646, 650, 654, 658, 662, 666, 670, 674, 678, 682, 686, 690, 694};
            a[0] = this.getIdEff();
            a[1] = this.getIndexDxy();
            a[2] = this.getIdBigImgAvatar();
            if (a[2] == 823 || a[2] == 824 || a[2] == 825) 
            { 
                a[3] = 5;
            } 
            else if (a[2] == 636 || a[2] == 684 || a[2] == 688) 
            {
                a[3] = 4; 
            } 
            if (java.util.Arrays.binarySearch(set5, v) >= 0) 
            {
                a[3] = 5;
            } 
            else if (java.util.Arrays.binarySearch(set1, v) >= 0) 
            {
                a[3] = 1;
            }
            else 
            {
                a[3] = 3; 
            }
        }
        if (this.itemThanKhi != null)
        {
            a[0] = this.itemThanKhi.getIdEff();
            a[1] = this.itemThanKhi.getIndexDxy();
            a[2] = this.itemThanKhi.getIdBigImgAvatar();
            a[3] = 5; 
        } 
        
        return a;
    }
    
    public int getIdEff()
    {
        if (this.hasWearingThanBinhWeapon()) {
            // Get the currently wearing weapon item
            Vector<Item> wearingItems = this.getCharWearing();
            for (Item item : wearingItems) {
                if (item != null && item.place == Item.PLACE_WEARING) {
                    // Check if item is a weapon (type 3-7)
                    int type = item.getTemplate().type;
                    if (type >= 3 && type <= 7) {
                        // Check if item ID is in range 773-787 (Than Binh weapons)
                        int id = item.getTemplate().id;
                        if (id >= 773 && id <= 787) {
                            return item.getTemplate().idEff + 8700;
                        }
                    }
                }
            }
        }
        return -1; // Return -1 if no Than Binh weapon is found
    }

    public int getIndexDxy() {
        return 0;
    }
    
    public int getIdBigImgAvatar() {
        if (this.hasWearingThanBinhWeapon()) {
            // Get the currently wearing weapon item
            Vector<Item> wearingItems = this.getCharWearing();
            for (Item item : wearingItems) {
                if (item != null && item.place == Item.PLACE_WEARING) {
                    // Check if item is a weapon (type 3-7)
                    int type = item.getTemplate().type;
                    if (type >= 3 && type <= 7) {
                        // Check if item ID is in range 773-787 (Than Binh weapons)
                        int id = item.getTemplate().id;
                        if (id >= 773 && id <= 787) {
                            return item.getTemplate().idBigImgAvatar; // Avatar ID for Than Binh weapons
                        }
                    }
                }
            }
        }
        return -1;
    }

    public int[] getIdBigAoThoiTrang() {
        final int[] a = {-1, -1, -1};
        
        // Kiểm tra Bát Cái Trang
      
            if (this.isBatGioi() && this.isBatCaiTrang()) {
                if (this.getLevelCaiTrang() >= 13) {
                    a[0] = 781;
                    a[1] = 12;
                    a[2] = 2;
                } else {
                    a[0] = 780;
                    a[1] = 12;
                    a[2] = 2;
                }
            } else if (this.isSaTang() && this.isBatCaiTrang()) {
                if (this.getLevelCaiTrang() >= 13) {
                    a[0] = 783;
                    a[1] = 12;
                    a[2] = 3;
                } else {
                    a[0] = 782;
                    a[1] = 12;
                    a[2] = 3;
                }
            }
        
        // Kiểm tra các loại trang phục khác
        else if (this.wModel.isCaiTaoSonTinh()) {
            a[0] = 831;
            a[1] = 10;
            a[2] = 2;
        }
        else if (this.wModel.isCaiTaoMiNuong()) {
            a[0] = 833;
            a[1] = 10;
            a[2] = 2;
        }
        else if (this.wModel.isTienNu()) {
            a[0] = Char.ID_BIG_AVT_AO_TIEN_NU;
            a[1] = 0;
            a[2] = 3;
        }
        else if (this.wModel.isTinhQuan()) {
            a[0] = Char.ID_BIG_AVT_AO_TINH_QUAN;
            a[1] = 1;
            a[2] = 1;
        }
        else if (this.wModel.isAoDaiNam()) {
            a[0] = Char.ID_BIG_AVT_AO_DAI_NAM;
            a[1] = 2;
            a[2] = 3;
        }
        else if (this.wModel.isAoDaiNu()) {
            a[0] = Char.ID_BIG_AVT_AO_DAI_NU;
            a[1] = 2;
            a[2] = 3;
        }
        else if (this.wModel.isMeoQuyToc()) {
            a[0] = 761;
            a[1] = 11;
            a[2] = 2;
        }
        else if (this.wModel.isGiapRong()) {
          
            a[0] = 770;
            a[1] = 13;
            a[2] = 2;
        }

        else if (this.wModel.isLacLongQuan()) {
            a[0] = 634;
            a[1] = 1;
            a[2] = 3;
        }
        else if (this.wModel.isAuCo()) {
            a[0] = 632;
            a[1] = 1;
            a[2] = 3;
        }
        else if (this.wModel.isAonoelNam2024()) {
            a[0] = 845;
            a[1] = 3;
            a[2] = 2;
        }
        else if (this.wModel.isAonoelNu2024()) {
            a[0] = 847;
            a[1] = 3;
            a[2] = 2;
        }
        // Ba Thien Ho: anh avatar nam 727 (ao xanh) / nu 728 (vay vang-do), deu 48x148
        // = 2 khung 48x74. Chi so 14 trong Dx_Dy_AVT_tool cho dx=-22, dy=-75 - vua khung.
        // Thieu khoi nay thi o xem truoc trong cua so Trang bi ve nhan vat tran, vi
        // Char.paintAvatar ben client chi doc idModel (atb5..atb8, item nay = 0) hoac
        // idImgAVT, KHONG doc lop phu head/body/leg.
        else if (this.wModel.isBaThienHoNam()) {
            a[0] = 727;
            a[1] = 14;
            a[2] = 2;
        }
        else if (this.wModel.isBaThienHoNu()) {
            a[0] = 728;
            a[1] = 14;
            a[2] = 2;
        }
        // Bon bo Bong da, cung ly do. Anh avatar 31-35 x 138 = 2 khung cao 69,
        // chi so 4 cho dx=-18, dy=-70. Mau ao tren avatar khop mau icon cua tung item.
        else if (this.wModel.isBongDaNamAoDo()) {
            a[0] = 697;
            a[1] = 4;
            a[2] = 2;
        }
        else if (this.wModel.isBongDaNuAoTrang()) {
            a[0] = 696;
            a[1] = 4;
            a[2] = 2;
        }
        else if (this.wModel.isBongDaNamAoCam()) {
            a[0] = 701;
            a[1] = 4;
            a[2] = 2;
        }
        else if (this.wModel.isBongDaNuAoSoc()) {
            a[0] = 703;
            a[1] = 4;
            a[2] = 2;
        }
        // Nhom Nghe Thuong. Anh avatar 717-721 da co san trong map/icon, chua ai dung.
        // Ghep bang cach doi chieu voi sprite d_effect cua tung bo (KHONG dung icon
        // 18x18 - icon ve bang palette khac nen doi chieu ra sai):
        //   797 Nghe Thuong -> 718  vay hong canh sen, toc cam cai hoa hong
        //   798 Ngoc Nu     -> 719  ao xanh ngoc, toc vang kim
        //   799 Xich Cuoc   -> 720  giap vang kim vien navy, toc do dung nhu lua
        //   800 Kim Dong    -> 721  ao xanh duong, toc DEN + vuong mien bac
        //   801 Loi Cong    -> 717  CO TRAN (than mau da), quan trang-tim vien vang
        // Cac muc isCaiTaoSonTinh/isCaiTaoMiNuong o tren van thang khi CaiTao > 0,
        // nen ban cai tao giu avatar rieng 831/833 - dung nhu truoc.
        else if (this.wModel.isNgheThuong()) {
            a[0] = 718;
            a[1] = 14;   // dx=-22 dy=-75, khung 43x74
            a[2] = 3;
        }
        else if (this.wModel.isNgocNu()) {
            a[0] = 719;
            a[1] = 7;    // dx=-25 dy=-69, khung 45x68
            a[2] = 3;
        }
        else if (this.wModel.isXichCuoc()) {
            a[0] = 720;
            a[1] = 11;   // dx=-26 dy=-69, khung 49x72
            a[2] = 2;
        }
        else if (this.wModel.isKimDong()) {
            a[0] = 721;
            a[1] = 14;   // dx=-22 dy=-75, khung 52x76
            a[2] = 2;
        }
        else if (this.wModel.isLoiCong()) {
            a[0] = 717;
            a[1] = 7;    // dx=-25 dy=-69, khung 45x67
            a[2] = 3;
        }

        return a;
    }

    private boolean isTatPhiPhong = false;

    public boolean isTatPhiPhong() {
        return isTatPhiPhong;
    }

    public void setTatPhiPhong() {
        isTatPhiPhong = true;
    }

    public void setBatPhiPhong() {
        isTatPhiPhong = false;
    }

    /**
     * Bat/tat HIEN THI skin linh thu, chon o NPC Tho Ren Than Bi. CHI an hinh anh con thu
     * (xem SkinLinhThu.hienThi) — khong dong toi chi so/noi tai da mac.
     *
     * Luu xuong CSDL (cot tob_char.isSkinThu, cung khuon voi isCT/isCanh — xem
     * DatabaseNhanVat.saveChar va Database.getChar/themCotCongTacHienThi). Ban dau (them
     * 01/09/2026) cong tac nay CHU Y khong luu, coi nhu tam thoi giong hoi chieu noi tai co
     * ti le — nhung nguoi choi bao tat xong dang xuat vao lai bi tu bat lai, gay kho chiu
     * (khac voi hoi chieu 40s von ngan hon moi phien choi). Da doi sang luu ben 05/09/2026.
     */
    private boolean isTatSkinLinhThu = false;

    public boolean isTatSkinLinhThu() {
        return isTatSkinLinhThu;
    }

    public void setTatSkinLinhThu() {
        isTatSkinLinhThu = true;
    }

    public void setBatSkinLinhThu() {
        isTatSkinLinhThu = false;
    }

    private boolean isTatCanh = false;

    public boolean isTatCanh() {
        return isTatCanh;
    }

    public void setTatCanh() {
        isTatCanh = true;
    }

    public void setBatCanh() {
        isTatCanh = false;
    }


    private boolean isTatMatNa = false;

    public boolean isTatMatNa() {
        return isTatMatNa;
    }

    public void setTatMatNa() {
        isTatMatNa = true;
    }

    public void setBatMatNa() {
        isTatMatNa = false;
    }

    private boolean isBatCaiTrang = true;

    public boolean isBatCaiTrang() {
        return isBatCaiTrang;
    }

    public void setTatCaiTrang() {
        isBatCaiTrang = false;
    }

    public void setBatCaiTrang() {
        isBatCaiTrang = true;
    }

    public int[] getIdBigPhiPhong() {
        final int[] a = {-1, -1, -1, -1};
        final Item[] wItems = this.getArrayCharWearing();
        final int index = getIndexItemWearing(19, 0);
        
        // Kiểm tra cánh trước
        if (this.wModel.CanhModel != null && this.wModel.CanhModel.isCanh() && !this.isTatCanh()) {
            a[0] = this.wModel.CanhModel.getIdEff();
            // a[1] TRUOC DAY BO TRONG (-1) nen canh khong bao gio hien o o xem truoc
            // cua so Trang bi - moi phi phong dac biet ben duoi deu dat a[1]. Nay tro
            // vao dai anh rieng cua tung bo canh, map/icon/855..859, dung tu chinh
            // hieu ung tren ban do (d_effect 270..274) nen nhin giong het.
            // Vat pham 898..902 = Lam Bang, Bach Linh, Xich Vu, Hong Van, Tu Anh.
            final int idCanh = this.wModel.CanhModel.getTemplate().id;
            if (idCanh >= 898 && idCanh <= 902) {
                a[1] = Char.ID_BIG_AVT_CANH_DAU + (idCanh - 898);
            }
            // So 2 = o rieng cua canh trong Dx_Dy_PP_tool, khac 1 cua phi phong vi
            // anh canh rong 96 chu khong phai 65.
            a[2] = 2;
            a[3] = 3;
            return a;
        }
        
        // Xu ly phi phong.
        //
        // Dieu kien truoc day bi DAO: phi phong dac biet dang mac chi duoc ve trong nhanh
        // else cua "!isTatPhiPhong()", tuc CHI HIEN KHI NGUOI CHOI DA TAT phi phong. Mac
        // dinh isTatPhiPhong = false (dang bat) va cot isPP trong DB cung luu the, nen
        // nguoi thuong mac Khong Tuoc / Huyen Vu / Bong Da / Tim thi khong hien gi ca, con
        // nguoi VIP thi chi thay hao quang VIP. Nhan chu thich cu ("khi da tat phi phong
        // VIP") thi nguoi viet hieu cong tac nay la tat rieng hao quang VIP, nhung thuc te
        // no la cong tac tat/bat phi phong noi chung (menu bao "Da tat phi phong").
        //
        // Dung phai la: bat thi ve, tat thi khong ve gi. Phi phong dac biet dang mac uu
        // tien hon hao quang VIP — no la do nguoi choi so huu.
        if (!this.isTatPhiPhong()) {
            if (wItems[index] != null) {
                if (wItems[index].isPhiPhongKhongTuoc()) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_KHONG_TUOC;
                    a[1] = Char.ID_BIG_AVT_PP_KHONG_TUOC;
                    a[2] = 0;
                    a[3] = 3;
                } else if (wItems[index].isPhiPhongHuyenVu()) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_HUYEN_VU;
                    a[1] = Char.ID_BIG_AVT_PP_HUYEN_VU;
                    a[2] = 1;
                    a[3] = 3;
                } else if (wItems[index].isPhiPhongBongDa()) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_BONG_DA;
                    // Truoc day dat ID_BIG_AVT_PP_HUYEN_VU (625) — chep sot tu nhanh tren.
                    // Hang so ID_BIG_AVT_PP_BONG_DA (711) co san nhung khong cho nao doc,
                    // nen ao bong da mac vao ra hinh phi phong Huyen Vu.
                    a[1] = Char.ID_BIG_AVT_PP_BONG_DA;
                    a[2] = 1;
                    a[3] = 3;
                } else if (wItems[index].isPhiPhongTim()) {
                    a[0] = 8937; // EFFECT
                    a[1] = 714; // AVATAR
                    a[2] = 1;
                    a[3] = 3;
                }
            }
            // Khong mac phi phong dac biet nao thi moi den hao quang VIP.
            if (a[0] == -1) {
                if (this.vip == 1) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_VIP3;
                    a[2] = 1;
                    a[3] = 3;
                } else if (this.vip == 2) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_VIP2;
                    a[2] = 1;
                    a[3] = 3;
                } else if (this.vip == 3) {
                    a[0] = EffectBuff.EFF_PHI_PHONG_VIP1;
                    a[2] = 1;
                    a[3] = 3;
                }
            }
        }
        return a;
    }

    public short[] getIDModel() {
        final short[] id = {-1, -1, -1, -1, -1};
        if (this.wModel.itemModel != null) {
            for (int i = 0; i < 4; ++i) {
                if (this.wModel.itemModel.getTemplate().atb[i + 5] > 0) {
                    id[i] = this.wModel.itemModel.getTemplate().atb[i + 5];
                }
            }
        }
        // Do thoi trang DOI DAU va mat na deu ve len dau nen deo ca hai la chong
        // len nhau. Rieng "Dau bi ngo" (vat pham 577, bo phan dau 26) la mot cai
        // dau DAC chu khong phai toc, nen phan thoi ra ngoai mat na nhin rat xau.
        // Chon: van deo ca hai de an chi so, nhung chi HIEN mat na.
        // Day chi la bo phan HINH - chi so cua mon do lay tu thuoc tinh vat pham,
        // khong di qua ham nay, nen khong mat gi.
        // Chi bo do DOI DAU (headModel). Dau di kem bo do (itemModel.atb5) van giu,
        // vi do la toc/mu cua bo do, mat na chi che phan mat nen nhin van hop.
        final boolean matNaThayDoDoiDau = this.wModel.matNa != null
                && this.wModel.hatModel == null
                && this.getIdHatMatNa() != -2;
        if (this.wModel.headModel != null && !matNaThayDoDoiDau) {
            id[0] = this.wModel.headModel.getTemplate().atb[5];
        }
        if (this.wModel.hatModel != null) {
            id[1] = this.wModel.hatModel.getTemplate().atb[6];
        }
        if (this.rankGov > -1) {
            id[1] = Char.idRankGov[this.rankGov];
        }
        if (this.wModel.wpModel != null && !this.wModel.wpModel.isChoiLuaLanBienHinh()) {
            id[4] = this.wModel.wpModel.getTemplate().atb[8];
        }
        if (this.wModel.itemModel != null && this.wModel.doNotPaintHat()) {
            id[1] = -2;
        }
        if (this.headModel > -1) {
            id[0] = this.headModel;
        }
        if (this.wModel.matNa != null && this.wModel.hatModel == null) {
            // Truoc day cho -2 = "khong ve non". Nay tro vao mot bo phan non that
            // (map/c/hat/67..74): tren ban do no trong suot nen khong doi gi, con
            // o o xem truoc cua so Trang bi thi hien dung hinh mat na. Xem
            // getIdHatMatNa() de biet vi sao phai lam vong nay.
            id[1] = (short) this.getIdHatMatNa();
        }

//        Logger2.DebugLogic("id: " + Arrays.toString(id));
        return id;
    }

    /**
     * Tra ve so bo phan "non" dung cho mat na dang deo.
     *
     * VI SAO CAN: o xem truoc trong cua so Trang bi ve nguoi bang Char.paintAvatar
     * cua client, ma ham do CHI doc chan / than / dau / non (atb5..atb8) hoac anh
     * avatar phang. Mat na lai khong nam trong nhom do - no la lop phu dong
     * (getIdHeadDynamic -> map/d_effect/<n>) va lop phu chi duoc ve boi paintHair,
     * la ham dung toa do TREN BAN DO. Vi vay mat na chua bao gio hien trong cua so
     * Trang bi.
     *
     * CACH LAM: moi mat na duoc cap mot file bo phan non rieng trong map/c/hat.
     * File do co hai phan: khung avatar la hinh mat na 18x18 (lay tu chinh icon
     * cua vat pham), con bon khung theo huong deu tro vao mot o 1x1 TRONG SUOT.
     * Nho vay tren ban do khong ve them gi (lop phu d_effect van lo hinh mat na
     * nhu cu), ma trong cua so Trang bi thi paintAvatar ve duoc mat na len mat.
     *
     * Tra -2 (nghia cu: khong ve non) neu gap mat na chua co anh.
     */
    public int getIdHatMatNa() {
        if (this.wModel == null || this.wModel.matNa == null) {
            return -2;
        }
        if (this.isTatMatNa()) {
            return -2;
        }
        if (this.wModel.isMatNaBiNgo()) {
            return 67;
        }
        if (this.wModel.isMatNaQuy()) {
            return 68;
        }
        if (this.wModel.isMatNaSoi()) {
            return 69;
        }
        if (this.wModel.isMatNaZoombi()) {
            return 70;
        }
        if (this.wModel.isMatNaPanda()) {
            return 71;
        }
        if (this.wModel.isMatNaCao()) {
            return 72;
        }
        if (this.wModel.isMatNaTho()) {
            return 73;
        }
        if (this.wModel.isMatNaOngDia()) {
            return 74;
        }
        return -2;
    }

    public void addKing(final Char p, final int country) {
        Char.gov[country].addMember(0, p.charname);
    }

    public void addDaiThan(final Char p, final int country) {
        if (!Char.gov[country].addMember(1, p.charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không thể phong chức cho " + p.charname, ""));
        } else {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(p.charname) + " đã được phong làm đại thần của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " đã được phong làm đại thần của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            p.rankGov = 1;
            this.potions[28] = 1;
            p.potions[87] = 1;
            this.potions[78] = 1;
            if (this.potions[27] == 0) {
                this.potions[27] = 1;
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            Database.instance.saveEvent(Map.event.getInfo());
            MessageCreator.createMsgCharMonster(p, p);
        }
    }

    public void addTuongQuan(final Char p, final int country) {
        if (!Char.gov[country].addMember(2, p.charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không thể phong chức cho " + p.charname, ""));
        } else {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(p.charname) + " đã được phong làm tướng quân của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " đã được phong làm đại thần của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            p.rankGov = 2;
            this.potions[28] = 1;
            this.potions[78] = 1;
            if (this.potions[27] == 0) {
                this.potions[27] = 1;
            }
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            Database.instance.saveEvent(Map.event.getInfo());
            MessageCreator.createMsgCharMonster(p, p);
        }
    }

    public void addBoDau(final Char p, final int country) {
        if (!Char.gov[country].addMember(3, p.charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không thể phong chức cho " + p.charname, ""));
        } else {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(p.charname) + " đã được phong làm bộ đầu của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(p.charname) + " đã được phong làm bộ đầu của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            p.rankGov = 3;
            this.potions[28] = 1;
            this.potions[78] = 1;
            if (this.potions[27] == 0) {
                this.potions[27] = 1;
            }
            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            Database.instance.saveEvent(Map.event.getInfo());
            MessageCreator.createMsgCharMonster(p, p);
        }
    }

    public void removeDaiThan(final String charname, final int country) {
        final Char p = CharManager.instance.getCharByCharName(charname);
        if (Char.gov[country].removeMember(1, charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(charname) + " đã bị phế chức đại thần của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(charname) + " đã bị phế chức đại thần của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            if (p != null) {
                p.rankGov = -1;
                p.potions[87] = 0;
                this.potions[28] = 0;
                this.potions[78] = 0;
                if (this.potions[27] == 1) {
                    this.potions[27] = 0;
                }
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                MessageCreator.createMsgCharMonster(p, p);
            }
            Database.instance.saveEvent(Map.event.getInfo());
        }
    }

    public void removeTuongQuan(final String charname, final int country) {
        final Char p = CharManager.instance.getCharByCharName(charname);
        if (Char.gov[country].removeMember(2, p.charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(charname) + " đã bị phế chức tướng quân của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(charname) + " đã bị phế chức tướng quân của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            if (p != null) {
                p.rankGov = -1;
                p.potions[87] = 0;
                this.potions[28] = 0;
                this.potions[78] = 0;
                if (this.potions[27] == 1) {
                    this.potions[27] = 0;
                }
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                MessageCreator.createMsgCharMonster(p, p);
            }
            Database.instance.saveEvent(Map.event.getInfo());
        }
    }

    public void removeBoDau(final String charname, final int country) {
        final Char p = CharManager.instance.getCharByCharName(charname);
        if (Char.gov[country].removeMember(3, p.charname)) {
            this.sendMessage(MessageCreator.createServerAlertMessage(String.valueOf(charname) + " đã bị phế chức bộ đầu của lãnh thổ " + Map.nameCountry[this.myCountry], ""));
            try {
                Map.sendAllCharServer(MessageCreator.createServerAlertAutoOffMessage(String.valueOf(charname) + " đã bị phế chức bộ đầu của lãnh thổ " + Map.nameCountry[this.myCountry]), this.myCountry);
            } catch (final IOException ex) {
            }
            if (p != null) {
                p.rankGov = -1;
                p.potions[87] = 0;
                this.potions[28] = 0;
                this.potions[78] = 0;
                if (this.potions[27] == 1) {
                    this.potions[27] = 0;
                }
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                MessageCreator.createMsgCharMonster(p, p);
            }
            Database.instance.saveEvent(Map.event.getInfo());
        }
    }

    public void removeKing(final Char p, final int country) {
        Char.gov[country].addMember(0, p.charname);
    }

    public void startPrivateMap() {
    }

    public boolean fullRing() {
        final Vector<Item> wItems = this.getCharWearing();
        byte n = 0;
        for (int i = 0; i < wItems.size(); ++i) {
            if (wItems.get(i).getType() == 8) {
                ++n;
                if (n > 1) {
                    break;
                }
            }
        }
        return n > 1;
    }

    public void createIdGiftWedding(final int type) {
        this.idGifWedding.removeAll(this.idGifWedding);
        if (type > 0) {
            for (byte i = 0; i < 6; ++i) {
                this.idGifWedding.add(i);
            }
        }
        this.rcvGiftWedding = 2;
    }

    // Than method (116 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public static void loadAllItemLucky() {
        CharSuKien.loadAllItemLucky();
    }

    public void doSendListItemLuckyDraw() {
        final Message m = new Message(87);
        try {
            m.dos.writeByte(0);
            m.dos.writeByte(1);
            m.dos.writeByte(Char.allItemLuckyDraw.size());
            for (int i = 0; i < Char.allItemLuckyDraw.size(); ++i) {
                final ItemLuckyDraw item = Char.allItemLuckyDraw.get(i);
                m.dos.writeByte(item.cat);
                m.dos.writeShort((item.cat == 12) ? item.idIconPet : item.id);
                m.dos.writeUTF(item.name);
                final Vector<String> decript = item.getInfo(this);
                m.dos.writeByte(decript.size());
                for (int j = 0; j < decript.size(); ++j) {
                    m.dos.writeUTF(decript.get(j));
                }
                m.dos.writeByte(item.isSelect);
            }
            this.sendMessage(m);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    // synchronized: kiem ve (potions[69]/[76]/[118]) roi tru la kiem-roi-tru khong
    // nguyen tu. Neu khong khoa, 2 goi tin "quay so" gan nhu dong thoi cho cung nhan
    // vat co the cung doc thay con ve truoc khi ben nao kip tru -> mat 1 ve nhung
    // nhan thuong 2 lan.
    public synchronized void doStartLuckDraw(final byte index) {
        if (this.indexItemLuckyDraw > -1) {
            return;
        }


        final boolean isPPWorldCup = Char.allItemLuckyDraw.get(index).isPPWorldCup();
        if (isPPWorldCup) {
            if (this.potions[76] <= 0) {
                this.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng mua vé quay số đặc biệt để có thể tham gia quay số vật phẩm này", ""));
                return;
            }
        } else if (Char.allItemLuckyDraw.get(index).isPhuongHoang()) {
            if (this.potions[118] <= 0) {
                this.sendMessage(MessageCreator.createServerAlertMessage("bạn phải có vé phượng hoàng mới có thể quay phượng hoàng", ""));
                return;
            }
        } else if (this.potions[69] <= 0) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng mua vé để có thể tham gia quay số", ""));
            return;
        }
        this.indexItemLuckyDraw = index;
        if (Char.allItemLuckyDraw.get(index).isPhuongHoang()) {
            final int[] potions = this.potions;
            final int n = 118;
            --potions[n];
        } else if (isPPWorldCup) {
            final int[] potions2 = this.potions;
            final int n2 = 76;
            --potions2[n2];
        } else {
            final int[] potions3 = this.potions;
            final int n3 = 69;
            --potions3[n3];
        }
        this.doFinishLuckyDraw();
        Database.instance.saveOrtherLog("", this.charname, "quay so " + index, "luckydraw");
    }
    

    // ==================================================================================
    //  CONG BAT/TAT SU KIEN
    //
    //  MOI MOC NGAY GHI CUNG O DAY DA BI BO (30/08/2026). Truoc day tung ham duoi day so
    //  ngay he thong voi mot moc trong khoang 2015..2024:
    //
    //      return getDayTime(0L).compareTo("2020-11-01 00:00:00") < 0;
    //
    //  Moc nao cung da qua tu lau, nghia la khong su kien nao bat lai duoc neu khong sua
    //  ma nguon roi build lai jar. Gio tat ca hoi real/SuKien, lay trang thai tu bang
    //  tob_su_kien - bat luc nao cung duoc, dat o trang quan tri web/admin/sukien.php.
    //
    //  TEN VA CHU KY GIU NGUYEN het: hon 400 cho trong ma nguon, va ca bytecode trong
    //  lib/NQSH_5h.jar, dang goi cac ham nay.
    //
    //  CAP isSuKienX / isFinishAllSuKienX GIO LA PHU DINH CHINH XAC cua nhau. Truoc day
    //  vai cap lech nhau vai ngay - vi du Halloween 2015 "bat" den 20/11 nhung "ket thuc"
    //  tu 29/11, chin ngay o giua khong ben nao dung - khe ho do chi co nghia tren truc
    //  thoi gian cu, gio khong con.
    //
    //  BA DIEU KIEN KHONG PHAI NGAY THANG THI GIU NGUYEN, vi chung noi ve LOAI MAY CHU
    //  chu khong noi ve thoi diem:
    //      Noel      : luon tat tren server lien dau va server local
    //      Cong tong : server Indo luon coi la moi su kien da ket thuc
    //      SN Ngude  : tat khi sv.me=1 (Map.openLog)
    // ==================================================================================

    /** Cong tong: con FALSE nghia la cac su kien cu van duoc phep chay. */
    public static boolean isFinishAllSuKien() {
        if (TeamServer.isServerIndo()) {
            return true;
        }
        return !SuKien.bat(SuKien.TAT_CA);
    }

    // ---------------------------------- Noel ----------------------------------

    public static boolean isSuKienNoel() {
        if (TeamServer.isServerLocal() || TeamServer.isServerLienDau()) {
            return false;
        }
        return SuKien.bat(SuKien.NOEL);
    }

    public static boolean isFinishAllSuKienNoel() {
        return !isSuKienNoel();
    }

    public static boolean isFinishSuKien() {
        return !isSuKienNoel();
    }

    public static boolean isSuKienNoel2023() {
        return SuKien.bat(SuKien.NOEL_2023);
    }

    public static boolean isDemNoel() {
        return SuKien.bat(SuKien.DEM_NOEL);
    }

    // ---------------------------------- Tet ----------------------------------

    public static boolean isSuKienTet2017() {
        return SuKien.bat(SuKien.TET_2017);
    }

    public static boolean isFinishAllSuKienTet2017() {
        return !isSuKienTet2017();
    }

    public static boolean isSuKienTetduonglich2024() {
        return SuKien.bat(SuKien.TET_DUONG_LICH);
    }

    public static boolean isSuKienTet2016() {
        return SuKien.bat(SuKien.TET_2016);
    }

    public static boolean isFinishAllSuKienTet2016() {
        return !isSuKienTet2016();
    }

    public static boolean is123Tet() {
        return SuKien.bat(SuKien.TET_123);
    }

    // ------------------------------ Black Friday ------------------------------

    public static boolean isSuKienBlackFriday() {
        return SuKien.bat(SuKien.BLACK_FRIDAY);
    }

    public static boolean isOpenDauGiaChoiBlackFriday() {
        return SuKien.bat(SuKien.BLACK_FRIDAY_DAU_GIA);
    }

    public static boolean isOpenSellBlackFriday() {
        return SuKien.bat(SuKien.BLACK_FRIDAY_BAN);
    }

    // -------------------------------- Trung Thu --------------------------------

    public static boolean isSuKienTrungThul2016() {
        return SuKien.bat(SuKien.TRUNG_THU);
    }

    public static boolean isFinishAllSuKienTrungThul2016() {
        return !isSuKienTrungThul2016();
    }

    /**
     * Dem ram: tang 1 Banh nuong (potion 125) khi dang nhap va khi len level, moi nhan
     * vat chi mot lan (chan boi nBinhTra). Mac dinh di theo cong Trung Thu, nhung tach
     * ma rieng de bat duoc mot dem trong ca dot su kien.
     */
    public static boolean isDemTrungThu() {
        return SuKien.bat(SuKien.DEM_TRUNG_THU);
    }

    public static boolean isSuKienTrungThu2015() {
        return SuKien.bat(SuKien.TRUNG_THU_2015);
    }

    public static boolean isFinishAllSuKienTrungthu2015() {
        return !isSuKienTrungThu2015();
    }

    public static boolean isBanLongDenHoaSen() {
        return SuKien.bat(SuKien.LONG_DEN_HOA_SEN);
    }

    /**
     * Trung Thu 2026: true neu HOM NAY dung la ngay bo dem "can moc tha den" toan server
     * (data.DatabaseCanMocThaDen) vua cham moc 10.000 lan dau tien - chi dung DUY NHAT trong
     * ngay do, khong phai moi ngay sau khi da vuot moc.
     */
    public static boolean isTrungThuX2ExpHomNay() {
        return data.DatabaseCanMocThaDen.homNayLaNgayDat10000();
    }

    // -------------------------------- Halloween --------------------------------

    public static boolean isSuKienHaloween2016() {
        return SuKien.bat(SuKien.HALLOWEEN);
    }

    public static boolean isFinishAllSuKienHaloween2016() {
        return !isSuKienHaloween2016();
    }

    /** Gop ca isDemHaloween lan isDemHaloween2023 vao mot cong. */
    public static boolean isDemHaloween() {
        return SuKien.bat(SuKien.DEM_HALLOWEEN);
    }

    public static boolean isDemHaloween2023() {
        return SuKien.bat(SuKien.DEM_HALLOWEEN);
    }

    public static boolean isSuKienHalowwen2015() {
        return SuKien.bat(SuKien.HALLOWEEN_2015);
    }

    public static boolean isFinishAllSuKienHaloween2015() {
        return !isSuKienHalowwen2015();
    }

    // ----------------------------------- He -----------------------------------

    public static boolean isSuKienHe2017() {
        return SuKien.bat(SuKien.HE_2017);
    }

    public static boolean isFinishAllSuKienHe2017() {
        return !isSuKienHe2017();
    }

    public static boolean isFinishAllSuKienHe2015() {
        return !SuKien.bat(SuKien.HE_2015);
    }

    // ---------------------------------- Vu Lan ----------------------------------

    public static boolean isSuKienVuLan2015() {
        return SuKien.bat(SuKien.VU_LAN);
    }

    public static boolean isFinishAllSuKienVulan2015() {
        return !isSuKienVuLan2015();
    }

    // --------------------------------- Gio To ---------------------------------

    public static boolean isSuKienGioTo2016() {
        return SuKien.bat(SuKien.GIO_TO);
    }

    public static boolean isFinishAllSuKienGioTo2016() {
        return !isSuKienGioTo2016();
    }

    // ---------------------------------- Mini ----------------------------------

    public static boolean isSuKienMini() {
        return SuKien.bat(SuKien.MINI);
    }

    public static boolean isFinishAllSuKienMini() {
        return !isSuKienMini();
    }

    public static boolean isSuKienMiniChucNu() {
        return SuKien.bat(SuKien.MINI_CHUC_NU);
    }

    public static boolean isFinishSuKienMiniChucNu() {
        return !isSuKienMiniChucNu();
    }

    public static boolean isSuKienMiniNuiChauBau() {
        return SuKien.bat(SuKien.MINI_NUI_CHAU_BAU);
    }

    // --------------------------------- Ky niem ---------------------------------

    public static boolean isSuKien83() {
        return SuKien.bat(SuKien.QUOC_TE_8_3);
    }

    public static boolean isSuKien2011l2016() {
        return SuKien.bat(SuKien.SINH_NHAT_2011);
    }

    public static boolean isSuKienSnNgude() {
        if (Map.openLog) {
            return false;
        }
        return SuKien.bat(SuKien.SINH_NHAT_NGUDE);
    }

    public static boolean isFinishAllSuKienSnNgude() {
        return !isSuKienSnNgude();
    }

    public static boolean isSinhNhatNgude() {
        if (Map.openLog) {
            return false;
        }
        return SuKien.bat(SuKien.SINH_NHAT_NGUDE_QUA);
    }

    public static boolean canReceiveGiftSNNgude() {
        return isSinhNhatNgude();
    }

    public static boolean isSuKienDuaTopHoaLu() {
        return SuKien.bat(SuKien.DUA_TOP_HOA_LU);
    }

    // -------------------------------- World Cup --------------------------------

    public static boolean isSuKienWordcup2017() {
        return SuKien.bat(SuKien.WORLD_CUP_2017);
    }
    

    // Than method (213 dong) da chuyen sang CharSuKien.java. Chu ky GIU NGUYEN.
    public void doFinishLuckyDraw() {
        CharSuKien.doFinishLuckyDraw(this);
    }

    public void createMainQuest() {
        if (this.mainQuest.state_quest == NewCharQuest.DONE || this.idMainQuest > this.mainQuest.id_quest) {
            final InfoQuestTemplate info = Map.allMainQuest.get(this.idMainQuest);
            if (info != null) {
                this.mainQuest = new NewCharQuest(this.idMainQuest, (byte) 0);
                MessageCreator.createMsgCharQuest(this, 0);
            }
        }
    }

    public void initQuest(final String itemGet, final String monsKilled, final String finish) {
        if (itemGet.equals("") && this.idMainQuest == 0) {
            this.mainQuest = new NewCharQuest(this.idMainQuest, (byte) 0);
        } else {
            final String[] data2 = split(itemGet, "|");
            final String[] data3 = split(monsKilled, "|");
            final String[] data4 = split(finish, "|");
            for (short i = 0; i < data4.length; ++i) {
                final NewCharQuest q = new NewCharQuest(true);
                final String[] isfinish = split(data4[i], ",");
                q.main_sub = Byte.parseByte(isfinish[2]);
                q.initQuest(Short.parseShort(isfinish[0]), split(data2[i], ","), split(data3[i], ","), isfinish[1]);
                if (q.main_sub == 0) {
                    this.mainQuest = q;
                    if (this.mainQuest.id_quest > this.idMainQuest) {
                        this.idMainQuest = this.mainQuest.id_quest;
                    }
                    if (this.mainQuest.id_quest < this.idMainQuest) {
                        this.mainQuest.state_quest = NewCharQuest.DONE;
                    }
                    if (this.mainQuest.state_quest == NewCharQuest.DONE) {
                        this.createMainQuest();
                    }
                } else if (q.main_sub == 1) {
                    final InfoQuestTemplate info = q.getTemplate();
                    if (info != null) {
                        this.subQuest = q;
                    }
                } else if (q.main_sub == 2) {
                    this.clanQuestNew = q;
                }
            }
        }
    }

    public String[] getInfoQuestSave() {
        final String[] info = {"", "", "", ""};
        final NewCharQuest cq = this.mainQuest;
        info[0] = new StringBuilder(String.valueOf(cq.getInfoItemGet())).toString();
        info[1] = new StringBuilder(String.valueOf(cq.getInfoMonsKilled())).toString();
        info[2] = String.valueOf(cq.id_quest) + "," + cq.state_quest + "," + cq.main_sub;
        if (this.subQuest.id_quest > -1) {
            final String[] array = info;
            final int n = 0;
            array[n] = String.valueOf(array[n]) + "|" + this.subQuest.getInfoItemGet();
            final String[] array2 = info;
            final int n2 = 1;
            array2[n2] = String.valueOf(array2[n2]) + "|" + this.subQuest.getInfoMonsKilled();
            final String[] array3 = info;
            final int n3 = 2;
            array3[n3] = String.valueOf(array3[n3]) + "|" + this.subQuest.id_quest + "," + this.subQuest.state_quest + "," + this.subQuest.main_sub;
        }
        if (this.clanQuestNew.id_quest > -1) {
            final String[] array4 = info;
            final int n4 = 0;
            array4[n4] = String.valueOf(array4[n4]) + "|" + this.clanQuestNew.getInfoItemGet();
            final String[] array5 = info;
            final int n5 = 1;
            array5[n5] = String.valueOf(array5[n5]) + "|" + this.clanQuestNew.getInfoMonsKilled();
            final String[] array6 = info;
            final int n6 = 2;
            array6[n6] = String.valueOf(array6[n6]) + "|" + this.clanQuestNew.id_quest + "," + this.clanQuestNew.state_quest + "," + this.clanQuestNew.main_sub;
        }
        info[3] = new StringBuilder(String.valueOf(this.itemquest[0])).toString();
        for (int i = 1; i < this.itemquest.length; ++i) {
            final String[] array7 = info;
            final int n7 = 3;
            array7[n7] = String.valueOf(array7[n7]) + "," + this.itemquest[i];
        }
        return info;
    }

    public Vector<NewCharQuest> getAllQuestCanReceive() {
        final Vector<NewCharQuest> allquest = new Vector<NewCharQuest>();
        if (this.mainQuest.state_quest == NewCharQuest.UN_RECEIVE) {
            allquest.add(this.mainQuest);
        }
        if (this.subQuest.id_quest == -1) {
            for (final InfoQuestTemplate info : Map.allSubQuest.values()) {
                if (info.type == 4) {
                    final int lvget = this.lvDetail.lv - info.lv;
                    if (lvget < 0 || lvget >= 10) {
                        continue;
                    }
                    allquest.add(new NewCharQuest(info.id, (byte) 1));
                } else {
                    allquest.add(new NewCharQuest(info.id, (byte) 1));
                }
            }
        }
        if (this.clanQuestNew.id_quest == -1 && this.idClan > -1 && this.countQuestClan < 10) {
            for (final InfoQuestTemplate info : Map.allClanQuest.values()) {
                allquest.add(new NewCharQuest(info.id, (byte) 2));
            }
        }
        return allquest;
    }

    public Vector<NewCharQuest> getAllQuestFinish() {
        final Vector<NewCharQuest> allquest = new Vector<NewCharQuest>();
        if (this.mainQuest.state_quest == NewCharQuest.FINISH) {
            allquest.add(this.mainQuest);
        }
        if (this.subQuest.state_quest == NewCharQuest.FINISH && this.subQuest.id_quest != -1) {
            allquest.add(this.subQuest);
        }
        if (this.clanQuestNew.state_quest == NewCharQuest.FINISH && this.clanQuestNew.id_quest != -1) {
            allquest.add(this.clanQuestNew);
        }
        return allquest;
    }

    public Vector<NewCharQuest> getAllQuestWorking() {
        final Vector<NewCharQuest> allquest = new Vector<NewCharQuest>();
        if (this.mainQuest.state_quest == NewCharQuest.WORKING) {
            allquest.add(this.mainQuest);
        }
        if (this.subQuest.state_quest == NewCharQuest.WORKING && this.subQuest.id_quest != -1) {
            allquest.add(this.subQuest);
        }
        if (this.clanQuestNew.state_quest == NewCharQuest.WORKING && this.clanQuestNew.id_quest != -1) {
            allquest.add(this.clanQuestNew);
        }
        return allquest;
    }

    public Vector<Short> checkDropItemQuest(final int idMonster) {
        final Vector<Short> idItemquest = new Vector<Short>();
        final Vector<NewCharQuest> allQuestWorking = this.getAllQuestWorking();
        if (allQuestWorking.size() > 0) {
            for (int i = 0; i < allQuestWorking.size(); ++i) {
                final NewCharQuest q = allQuestWorking.get(i);
                final InfoQuestTemplate info = q.getTemplate();
                if (info.type == 2) {
                    final short[] monsKill = info.getMonsterKill();
                    final short[] totalItemGet = info.getTotalItemGet();
                    for (int j = 0; j < q.nItem.length; ++j) {
                        if (monsKill[j] == idMonster && q.nItem[j] < totalItemGet[j] && !idItemquest.contains(info.itemget.get(j))) {
                            idItemquest.add(info.itemget.get(j));
                        }
                    }
                }
            }
        }
        return idItemquest;
    }

    public void checkFinishQuest(final int idMonster_item, final boolean iskillMons, final int lv) {
        final Vector<NewCharQuest> allQuestWorking = this.getAllQuestWorking();
        if (allQuestWorking.size() > 0) {
            for (int i = 0; i < allQuestWorking.size(); ++i) {
                final NewCharQuest q = allQuestWorking.get(i);
                final InfoQuestTemplate info = q.getTemplate();
                if (q.state_quest == NewCharQuest.WORKING) {
                    boolean isFinish = true;
                    if (info.type == 0 && iskillMons) {
                        final short[] monsKill = info.getMonsterKill();
                        final short[] totalMonsKill = info.getTotalMonsterKill();
                        for (int j = 0; j < q.monsterKilled.length; ++j) {
                            if (monsKill[j] == idMonster_item && q.monsterKilled[j] < totalMonsKill[j]) {
                                final short[] monsterKilled = q.monsterKilled;
                                final int n = j;
                                ++monsterKilled[n];
                                break;
                            }
                        }
                        for (int j = 0; j < totalMonsKill.length; ++j) {
                            if (q.monsterKilled[j] < totalMonsKill[j]) {
                                isFinish = false;
                            }
                        }
                        if (isFinish) {
                            q.state_quest = NewCharQuest.FINISH;
                            MessageCreator.createMsgCharQuest(this, 1);
                            Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                            this.sendMessage(MessageCreator.createServerAlertMessage("Nhiệm vụ " + info.name + " đã hoàn thành. Hãy đi trả nhiệm vụ", ""));
                        }
                    } else if (info.type == 4 && iskillMons) {
                        if (Map.abs(this.lvDetail.lv - lv) <= info.deltaLv) {
                            final short[] totalMonsKill2 = info.getTotalMonsterKill();
                            for (int k = 0; k < q.monsterKilled.length; ++k) {
                                if (q.monsterKilled[k] < totalMonsKill2[k]) {
                                    final short[] monsterKilled2 = q.monsterKilled;
                                    final int n2 = k;
                                    ++monsterKilled2[n2];
                                    break;
                                }
                            }
                            for (int k = 0; k < totalMonsKill2.length; ++k) {
                                if (q.monsterKilled[k] < totalMonsKill2[k]) {
                                    isFinish = false;
                                }
                            }
                            if (isFinish) {
                                q.state_quest = NewCharQuest.FINISH;
                                MessageCreator.createMsgCharQuest(this, 1);
                                Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                                this.sendMessage(MessageCreator.createServerAlertMessage("Nhiệm vụ " + info.name + " đã hoàn thành. Hãy đi trả nhiệm vụ", ""));
                            }
                        }
                    } else if (info.type == 2 && !iskillMons) {
                        final short[] itemQuest = info.getItemGet();
                        final short[] totalItemGet = info.getTotalItemGet();
                        for (int j = 0; j < q.nItem.length; ++j) {
                            if (itemQuest[j] == idMonster_item && q.nItem[j] < totalItemGet[j]) {
                                final short[] nItem = q.nItem;
                                final int n3 = j;
                                ++nItem[n3];
                                break;
                            }
                        }
                        for (int j = 0; j < totalItemGet.length; ++j) {
                            if (q.nItem[j] < totalItemGet[j]) {
                                isFinish = false;
                            }
                        }
                        if (isFinish) {
                            q.state_quest = NewCharQuest.FINISH;
                            MessageCreator.createMsgCharQuest(this, 1);
                            MessageCreator.createMsgCharQuest(this, 2);
                            Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                            this.sendMessage(MessageCreator.createServerAlertMessage("Nhiệm vụ " + info.name + " đã hoàn thành. Hãy đi trả nhiệm vụ", ""));
                        }
                    }
                }
            }
        }
    }

    public void OnQuest(final byte type, final short questID, final byte main_sub) 
    {              
        if (main_sub == 0) 
        {
            final InfoQuestTemplate info = this.mainQuest.getTemplate();
            if (type == 0) 
            {
                try {
                    if (info.pAvtive > this.pointActiveQuest) 
                    {
                        this.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần " + info.pAvtive + " điểm hoạt động mới có thể nhận nhiệm vụ này. Bạn còn " + this.pointActiveQuest + " điểm.", ""));
                        return;
                    }
                    this.pointActiveQuest -= info.pAvtive; // trừ điểm hoạt động
                    this.mainQuest.state_quest = NewCharQuest.WORKING;
                    if (info.type == 1) 
                    {
                        this.mainQuest.state_quest = NewCharQuest.FINISH;
                        this.itemquest[info.itemget.get(0)] = 1;
                        MessageCreator.createMsgCharQuest(this, 1);
                        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 3));
                    } 
                    else 
                    {
                        MessageCreator.createMsgCharQuest(this, 2);
                    }
                    MessageCreator.createMsgCharQuest(this, 0);
                    this.sendMessage(MessageCreator.createServerAlertMessage("Đã nhận nhiệm vụ " + info.name, ""));
                    Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                } catch (final Exception e) 
                {
                    e.printStackTrace();
                }
            } else if (type == 1) {
                try {
                    if (this.mainQuest.state_quest != NewCharQuest.DONE && (this.mainQuest.state_quest == NewCharQuest.FINISH || info.type == 1 || info.type == 3)) {
                        this.mainQuest.state_quest = NewCharQuest.DONE;
                        this.mainQuest.addGift(this);
                        boolean sendInfo = true;
                        if (info.type == 3) {
                            sendInfo = false;
                        }
                        if (info.type == 2 || info.type == 1) {
                            final short[] itemQuest = info.getItemGet();
                            final short[] totalItemGet = info.getTotalItemGet();
                            for (int i = 0; i < itemQuest.length; ++i) {
                                this.itemquest[itemQuest[i]] = 0;
                                this.mainQuest.nItem[i] = 0;
                                if (this.mainQuest.nItem[i] < 0) {
                                    this.mainQuest.nItem[i] = 0;
                                }
                                if (this.itemquest[itemQuest[i]] < 0) {
                                    this.itemquest[itemQuest[i]] = 0;
                                }
                            }
                            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 3));
                        }
                        ++this.idMainQuest;
                        this.createMainQuest();
                        MessageCreator.createMsgCharQuest(this, 1);
                        MessageCreator.createMsgCharQuest(this, 0);
                        if (sendInfo) {
                            this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhiệm vụ hoàn thành"));
                        }
                        Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } else if (type == 2) {
                if (this.mainQuest.state_quest == NewCharQuest.DONE) {
                    return;
                }
                this.mainQuest.reset();
                MessageCreator.createMsgCharQuest(this, 0);
                MessageCreator.createMsgCharQuest(this, 2);
                this.sendMessage(MessageCreator.createServerAlertMessage("Đã hủy nhiệm vụ " + info.name, ""));
                Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
            }
        } else if (main_sub == 1) {
            InfoQuestTemplate info = null;
            if (type == 0) {
                if (this.subQuest.id_quest > -1 && this.subQuest.id_quest != questID) {
                    return;
                }
                this.subQuest = new NewCharQuest(questID, (byte) 1);
                info = this.subQuest.getTemplate();
                if (info.pAvtive > this.pointActiveQuest) {
                    this.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần " + info.pAvtive + " điểm hoạt động mới có thể nhận nhiệm vụ này. Bạn còn " + this.pointActiveQuest + " điểm.", ""));
                    this.subQuest.reset();
                    this.subQuest.id_quest = -1;
                    return;
                }
                this.pointActiveQuest -= info.pAvtive;
                try {
                    this.subQuest.state_quest = NewCharQuest.WORKING;
                    if (info.type == 1) {
                        this.subQuest.state_quest = NewCharQuest.FINISH;
                        MessageCreator.createMsgCharQuest(this, 1);
                    } else {
                        MessageCreator.createMsgCharQuest(this, 2);
                    }
                    MessageCreator.createMsgCharQuest(this, 0);
                    this.sendMessage(MessageCreator.createServerAlertMessage("Đã nhận nhiệm vụ " + info.name, ""));
                    Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                    if (info.pAvtive > 0) {
                        this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } else if (type == 1) {
                if (this.subQuest.id_quest > -1 && this.subQuest.id_quest != questID) {
                    return;
                }
                info = this.subQuest.getTemplate();
                if (this.subQuest.state_quest != NewCharQuest.DONE && (info.type == 3 || info.type == 1 || this.subQuest.state_quest == NewCharQuest.FINISH)) {
                    this.subQuest.state_quest = NewCharQuest.DONE;
                    this.subQuest.addGift(this);
                    boolean sendInfo = true;
                    if (info.type == 3) {
                        sendInfo = false;
                    }
                    if (info.type == 2 || info.type == 1) {
                        final short[] itemQuest = info.getItemGet();
                        final short[] totalItemGet = info.getTotalItemGet();
                        for (int i = 0; i < itemQuest.length; ++i) {
                            this.itemquest[itemQuest[i]] = 0;
                            this.subQuest.nItem[i] = 0;
                            if (this.subQuest.nItem[i] < 0) {
                                this.subQuest.nItem[i] = 0;
                            }
                            if (this.itemquest[itemQuest[i]] < 0) {
                                this.itemquest[itemQuest[i]] = 0;
                            }
                        }
                        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 3));
                    }
                    this.subQuest.reset();
                    this.subQuest.id_quest = -1;
                    if (info.type != 3) {
                        MessageCreator.createMsgCharQuest(this, 1);
                    }
                    MessageCreator.createMsgCharQuest(this, 0);
                    if (sendInfo) {
                        this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhiệm vụ hoàn thành"));
                    }
                    Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                }
            } else if (type == 2) {
                if (this.subQuest.id_quest > -1 && this.subQuest.id_quest != questID) {
                    return;
                }
                info = this.subQuest.getTemplate();
                if (this.subQuest.state_quest == NewCharQuest.DONE) {
                    return;
                }
                this.subQuest.reset();
                this.subQuest.id_quest = -1;
                MessageCreator.createMsgCharQuest(this, 0);
                MessageCreator.createMsgCharQuest(this, 2);
                this.sendMessage(MessageCreator.createServerAlertMessage("Đã hủy nhiệm vụ " + info.name, ""));
                Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
            }
        } else if (main_sub == 2) {
            InfoQuestTemplate info = null;
            if (type == 0) 
            {
                if (this.clanQuestNew.id_quest > -1 && this.clanQuestNew.id_quest != questID) 
                {
                    return;
                }
                this.clanQuestNew = new NewCharQuest(questID, (byte) 2);
                info = this.clanQuestNew.getTemplate();
                if (info.pAvtive > this.pointActiveQuest) 
                {
                    this.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần " + info.pAvtive + " điểm hoạt động mới có thể nhận nhiệm vụ này. Bạn còn " + this.pointActiveQuest + " điểm.", ""));
                    this.clanQuestNew.reset();
                    this.clanQuestNew.id_quest = -1;
                    return;
                }
                
                System.out.print(this.charname + " nhận nv bang:  pAvtive: " + info.pAvtive + " điểm hoạt động mới có thể nhận nhiệm vụ này. Bạn còn " + this.pointActiveQuest + " điểm.");
                this.pointActiveQuest -= info.pAvtive;
                try {
                    this.clanQuestNew.state_quest = NewCharQuest.WORKING;
                    if (info.type == 1) {
                        this.clanQuestNew.state_quest = NewCharQuest.FINISH;
                        MessageCreator.createMsgCharQuest(this, 1);
                    } else {
                        MessageCreator.createMsgCharQuest(this, 2);
                    }
                    MessageCreator.createMsgCharQuest(this, 0);
                    this.sendMessage(MessageCreator.createServerAlertMessage("Đã nhận nhiệm vụ " + info.name, ""));
                    Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } else if (type == 1) {
                if (this.clanQuestNew.id_quest > -1 && this.clanQuestNew.id_quest != questID) {
                    return;
                }
                info = this.clanQuestNew.getTemplate();
                if (this.clanQuestNew.state_quest != NewCharQuest.DONE && (info.type == 3 || info.type == 1 || this.clanQuestNew.state_quest == NewCharQuest.FINISH)) {
                    this.clanQuestNew.state_quest = NewCharQuest.DONE;
                    this.clanQuestNew.addGiftClan(this);
                    boolean sendInfo = true;
                    if (info.type == 3) {
                        sendInfo = false;
                    }
                    if (info.type == 2 || info.type == 1) {
                        final short[] itemQuest = info.getItemGet();
                        final short[] totalItemGet = info.getTotalItemGet();
                        for (int i = 0; i < itemQuest.length; ++i) {
                            this.itemquest[itemQuest[i]] = 0;
                            this.clanQuestNew.nItem[i] = 0;
                            if (this.clanQuestNew.nItem[i] < 0) {
                                this.clanQuestNew.nItem[i] = 0;
                            }
                            if (this.itemquest[itemQuest[i]] < 0) {
                                this.itemquest[itemQuest[i]] = 0;
                            }
                        }
                        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 3));
                    }
                    this.clanQuestNew.reset();
                    this.clanQuestNew.id_quest = -1;
                    if (info.type != 3) {
                        MessageCreator.createMsgCharQuest(this, 1);
                    }
                    MessageCreator.createMsgCharQuest(this, 0);
                    ++this.countQuestClan;
                    if (sendInfo) {
                        this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhiệm vụ hoàn thành"));
                    }
                    Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
                }
            } else if (type == 2) {
                if (this.clanQuestNew.id_quest > -1 && this.clanQuestNew.id_quest != questID) {
                    return;
                }
                info = this.clanQuestNew.getTemplate();
                if (this.clanQuestNew.state_quest == NewCharQuest.DONE) {
                    return;
                }
                this.clanQuestNew.reset();
                this.clanQuestNew.id_quest = -1;
                MessageCreator.createMsgCharQuest(this, 0);
                MessageCreator.createMsgCharQuest(this, 2);
                this.sendMessage(MessageCreator.createServerAlertMessage("Đã hủy nhiệm vụ " + info.name, ""));
                Database.instance.saveCharQuest(this.charDBID, this.getInfoQuestSave());
            }
        }
    }

    public static Item createPhiPhongLongThuMax(final Char p, final int ma_vat, final int idTemplate, final boolean is8x, final boolean lock, final int time) {
        final int[] id = {584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597};
        int lv = p.lvDetail.lv;
        if (lv < 35) {
            lv = 35;
        }
        if (lv > 80) {
            lv = 80;
        }
        int idtemp = id[(lv - 35) / 5];
        if (is8x) {
            idtemp = 593;
        }
        final Item newItem = new Item();
        final ItemTemplates item = (ItemTemplates) Map.itemTemplates.get(5).get(idtemp);
        for (int i = 0; i < 10; ++i) {
            if (item.atb[i] > 0) {
                newItem.atb[i] = (short) (item.atb[i] + item.atb[i] / 4);
            }
        }
        newItem.setTemplate(idTemplate, p.charClass, p.charClass, idTemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = item.level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.dateCreate = getDayTime(0L);
        if (lock) {
            newItem.lock = 1;
        }
        if (time > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = time;
        }
        newItem.createAttributeItemCoat(true, (byte) ma_vat, -1);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        return newItem;
    }

    public static Item createPhiPhongMaxBaoKich(final Char player, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        final int[] id = {584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597, 840};
        int lv = player.lvDetail.lv;
        if (lv < 35) {
            lv = 35;
        }
        if (lv > 80) {
            lv = 80;
        }
        if (is8x) {
            lv = 80;
        }
        int idtemp = id[(lv - 35) / 5];
        if (is8x) {
            idtemp = 593;
        }
        final Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(idtemp, player, ma_vat, isMax);
        newItem.dateCreate = getDayTime(0L);
        newItem.timeLoan = System.currentTimeMillis();
        if (isMax) {
            newItem.otherAtt[4] = 30;
        }
        if (time > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = time;
        }
        if (plus > 0) {
            player.doImbueItem(newItem, plus);
        }
        if (isLock) {
            newItem.lock = 1;
        }
        player.iItems.add(newItem);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        return newItem;
    }

    
    public static void createNguyetLinhTruong(final Char p, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        if (p != null) {
            int iditem = 796;
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = (byte) (isLock ? 1 : 0);
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang()) {
                newItem.createAttributeNguyetLinhTruong();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeNguyetLinhTruongMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            if (plus > 0) {
                p.doImbueItem(newItem, plus);
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }

    public static void createThanKhi(final Char p, final int idThanKhi, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        if (p != null) {
            int iditem = idThanKhi;
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = 0;
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang()) {
                newItem.createAttributeVuKhiThoiTrang();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeVuKhiThoiTrangMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            if (plus > 0) {
                p.doImbueItem(newItem, plus);
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }


    public static void createVuKhiThoiTrangID(final Char p, final int iditem, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        if (p != null) {
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = 1;
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang() && !isMax) {
                newItem.createAttributeVuKhiThoiTrang();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeVuKhiThoiTrangMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            if (plus > 0) {
                p.doImbueItem(newItem, plus);
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }



    public static void createVuKhiThoiTrang(final Char p, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        if (p != null) {
            int iditem = (715 + p.charClass);
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = 1;
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang() && !isMax) {
                newItem.createAttributeVuKhiThoiTrang();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeVuKhiThoiTrangMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }
    
    public static void createVuKhiThoiTrangTHANBINH_LV(final Char p, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final int level, final int tinhLuyen, final boolean isLock) {
        if (p != null) {
            int iditem;
            // Xác định ID item dựa vào level
            if (level == 60) {
                iditem = 758 + p.charClass;
            } else if (level == 70) {
                iditem = 763 + p.charClass;
            } else if (level == 80) {
                iditem = 768 + p.charClass;
            } else {
                return; // Return nếu level không hợp lệ
            }
            
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = 0;
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
    
            // Set tinh luyện nếu có
            if (tinhLuyen > 0 && tinhLuyen <= 99) {
                newItem.TinhLuyen = tinhLuyen;
            }
    
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang() && !isMax) {
                newItem.createAttributeVuKhiThoiTrang();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeVuKhiThoiTrangMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }

    public static void createVuKhiThoiTrangTHANBINH_NEW(final Char p, final int ma_vat, final int time, final Item itemReceive, final boolean isMax, final boolean is8x, final int plus, final boolean isLock) {
        if (p != null) {
            int iditem = 758 + p.charClass;
            int clazz = p.charClass;
            int magic_vl = ma_vat;
            int minute = time;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = (newItem.getTemplate()).level;
            newItem.lock = 0;
            p.iItems.add(newItem);
            newItem.identify = p.charDBID;
            newItem.clazz = (byte) clazz;
            if (template.type < 3 || template.type == 10 || template.type == 11) {
                newItem.magic_physic = p.typeItemBuy;
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (OfflineMap.isAnimalItem(template.type)) {
                newItem.magic_physic = (byte) magic_vl;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }
                if (magic_vl == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_vl == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }
            }
            if (minute > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = minute;
            }
            if (isItemThanThu(template.type)) {
                newItem.doAddOptionThanThu(template.atb[0]);
            }
            if (newItem.isVukhiThoiTrang() && !isMax) {
                newItem.createAttributeVuKhiThoiTrang();
            }
            if (newItem.isVukhiThoiTrang() && isMax) {
                newItem.createAttributeVuKhiThoiTrangMax();
            }
            newItem.createAttChoi();
            newItem.dateCreate = Char.getDayTime(0L);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        }
    }

    public static boolean isItemThanThu(int type) {
        return (type == 25);
    }

    public static void genItemPhiPhong() {
        final int[] id = {584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597};
        final int time = 0;
        for (int i = 0; i < id.length; ++i) {
            final int idTemplate = id[i];
            final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idTemplate), false, 0, 0, idTemplate);
            newItem.level = newItem.getTemplate().level;
            newItem.createAttributeItemCoat(false, (byte) (-1), -1);
            newItem.dateCreate = getDayOpen(0L);
            System.out.println(String.valueOf(newItem.getTemplate().name) + " LV_" + newItem.level + "_" + newItem.getDBInfo() + "|" + newItem.getAttribute());
        }
    }

    public void genItemPhiPhong(final int idTemplate, final boolean isMax, final int mv, final int time) {
        final int[] id = {584, 585, 586, 587, 588, 589, 590, 591, 592, 593, 594, 595, 596, 597};
        int lv = this.lvDetail.lv;
        if (lv < 35) {
            lv = 35;
        }
        final int idtemp = id[(lv - 35) / 5];
        final Item newItem = new Item();
        final ItemTemplates item = (ItemTemplates) Map.itemTemplates.get(5).get(idtemp);
        for (int i = 0; i < 10; ++i) {
            if (item.atb[i] > 0) {
                newItem.atb[i] = (short) (item.atb[i] + item.atb[i] / 4);
            }
        }
        newItem.setTemplate(idTemplate, this.charClass, this.charClass, idTemplate);
        newItem.id = this.getIDItem();
        newItem.owner = this.charDBID;
        newItem.level = (byte) ((lv - 35) / 5 * 5 + 35);
        newItem.identify = this.charDBID;
        newItem.clazz = this.charClass;
        newItem.timeLoan = System.currentTimeMillis();
        newItem.minuteExist = time;
        newItem.lock = 1;
        newItem.createAttributeItemCoat(true, (byte) mv, -1);
        this.iItems.add(newItem);
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
    }

    public void initListKillMe(final String list) {
        try {
            final String[] data = split(list, ",");
            for (int i = 0; i < data.length; ++i) {
                if (!data[i].trim().equals("")) {
                    this.listKillme.add(data[i].trim());
                }
            }
        } catch (final Exception ex) {
        }
    }

    public void addListKillMe(final String charname2) {
        for (int i = 0; i < this.listKillme.size(); ++i) {
            if (this.listKillme.get(i).equals(charname2)) {
                return;
            }
        }
        this.listKillme.insertElementAt(charname2, 0);
        if (this.listKillme.size() > 20) {
            this.listKillme.remove(this.listKillme.size() - 1);
        }
    }

    public Vector<String> getTopKillMe() {
        Vector<String> result = new Vector<String>();
        final Vector<String> online = new Vector<String>();
        final Vector<String> off = new Vector<String>();
        for (int i = 0; i < this.listKillme.size(); ++i) {
            final String name = this.listKillme.get(i);
            if (CharManager.instance.getCharByCharName(name) != null) {
                online.add(String.valueOf(name) + " - on");
            } else {
                off.add(String.valueOf(name) + " - off");
            }
        }
        result = online;
        for (int i = 0; i < off.size(); ++i) {
            result.add(off.get(i));
        }
        return result;
    }

    public String getSaveListKillMe() {
        String info = "";
        int size = this.listKillme.size();
        if (size > 20) {
            size = 20;
        }
        for (int i = 0; i < size; ++i) {
            info = String.valueOf(info) + this.listKillme.get(i) + ",";
        }
        if (!info.equals("")) {
            info = info.substring(0, info.length() - 1);
        }
        return info;
    }

    public boolean createPoro(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Đại Bạch Thử";
            anima.idImg = Animal.PORO;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua poro", "poro");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được linh thú Đại bạch thử", ""));
        }
        return true;
    }

    public boolean createNguaXuong(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Bạch cốt mã";
            anima.idImg = Animal.NGUA_XUONG;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua ngua", "ngua");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Bạch cốt mã"));
        }
        return true;
    }

    public boolean createHeo(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Đương khang";
            anima.idImg = Animal.HEO;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua heo", "heo");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Đương khang"));
        }
        return true;
    }

    public boolean createHeoBang(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Đương khang Băng Giá";
            anima.idImg = Animal.HEO_BANG;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua heo bang", "heo bang");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Đương khang Băng Giá"));
        }
        return true;
    }

     public boolean createSuTu(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Sư tử";
            anima.idImg = Animal.SU_TU;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua su tu", "su tu");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Sư tử"));
        }
        return true;
    }
    
    public boolean createSuTuBang(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Sư tử Băng";
            anima.idImg = Animal.SU_TU_BANG;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua su tu bang 1", "su tu bang");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Sư tử Băng"));
        }
        return true;
    }

    public boolean createRongLon(final long day, final boolean isAlert) {
        try {
            final Animal anima = new Animal();
            anima.name = "Rồng Băng";
            anima.idImg = Animal.RONG_LON;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua heo", "heo");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú Rồng Băng"));
        }
        return true;
    }

    public boolean createLan(final long minute, final boolean isAlert, final int khanghoatuyet) {
        try {
            final Animal anima = new Animal();
            anima.name = "Lân Sư Tử";
            anima.idImg = Animal.LAN;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (minute > 0L) {
                anima.texpire = System.currentTimeMillis() + minute * 60000L;
            }
            anima.KHANG_HOA_TUYET = (byte) khanghoatuyet;
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua lan", "lan");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (isAlert) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được linh thú Lân sư tử", ""));
        }
        return true;
    }

    public void createTuanLoc(final long day) {
        try {
            final Animal anima = new Animal();
            anima.name = "Tuần lộc";
            anima.idImg = Animal.TUAN_LOC;
            anima.createAttMax();
            anima.level = 4;
            anima.place = 0;
            anima.ownerId = this.charDBID;
            if (day > 0L) {
                anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
            }
            anima.id = this.getIDItem();
            this.animal.add(anima);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
            Database.instance.saveAnimal(anima);
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "mua tuan loc", "tuan loc");
        } catch (final Exception ex) {
        }
        this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được linh thú tuần lộc", ""));
    }

    public void createPhuongHoang(final long day, final int idPH, final boolean isAlert) {
        try {
            try {
                final Animal anima = new Animal();
                anima.name = "Phượng hoàng";
                anima.idImg = Animal.PHUONG_HOANG;

                if (idPH == Animal.PHUONG_HOANG_BANG) {
                    anima.name = "Phượng hoàng băng";
                    anima.idImg = Animal.PHUONG_HOANG_BANG;
                }
                if (idPH == Animal.PHUONG_HOANG_7MAU) {
                    anima.name = "Phượng hoàng đa sắc";
                    anima.idImg = Animal.PHUONG_HOANG_7MAU;
                }
                if (idPH == Animal.PHUONG_HOANG_MOC) {
                    anima.name = "Phượng hoàng Mộc";
                    anima.idImg = Animal.PHUONG_HOANG_MOC;
                }
                if (idPH == Animal.PHUONG_HOANG_KIM) {
                    anima.name = "Phượng hoàng Kim";
                    anima.idImg = Animal.PHUONG_HOANG_KIM;
                }
                if (idPH == Animal.PHUONG_HOANG_THO) {
                    anima.name = "Phượng hoàng Thổ";
                    anima.idImg = Animal.PHUONG_HOANG_THO;
                }

                anima.createAttMax();
                anima.level = 4;
                anima.place = 0;
                anima.ownerId = this.charDBID;
                if (day > 0L) {
                    anima.texpire = System.currentTimeMillis() + day * 24L * 60L * 60000L;
                }
                anima.id = this.getIDItem();
                this.animal.add(anima);
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
                Database.instance.saveAnimal(anima);
                Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "trung " + anima.name, "phuonghoang");
                if (isAlert) {
                    this.sendMessage(MessageCreator.createMsgChat(this.id, "Chúc mừng. Bạn đã nhận được linh thú " + anima.name));
                }

            } catch (final Exception e) {
                e.printStackTrace();
            }
        } catch (final Exception ex) {
        }
    }

    public void createTrungPhuongHoang(final long day, final int idPH) {
        try {
            if (idPH == 16) {
                final int[] potions = this.potions;
                final int n = 86;
                ++potions[n];
            } else if (idPH == 17) {
                final int[] potions2 = this.potions;
                final int n2 = 70;
                ++potions2[n2];
            } else if (idPH == 18) {
                final int[] potions2 = this.potions;
                final int n2 = 71;
                ++potions2[n2];
            } else if (idPH == 19) {
                final int[] potions2 = this.potions;
                final int n2 = 111;
                ++potions2[n2];
            }
            Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, "Trung trung phuong hoang " + idPH, "phuonghoang");
        } catch (final Exception ex) {
        }
        this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được trứng phượng hoàng", ""));
    }

    public static void createChoi(final int idtemplate, final Char p) {
        p.buychoi = 1;
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.createAttChoiVinhVien();
        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "nhanchoi");
    }


    public static Item createChoi(final int idtemplate, final Char p, final int minute, final int lock, final boolean isHaveBienhinh) {
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.createAttChoiVinhVien();
        newItem.lock = (byte) lock;
        newItem.dateCreate = getDayTime(0L);
        if (minute > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
        }
        if (isHaveBienhinh) {
            newItem.setOptionHoaNguoiTuyetChoi();
        }
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "nhanchoi");
        if (isDemNoel()) {
            p.receiveGiftEvent = 1;
        }
        return newItem;
    }

    

    public static Item createItemSoLuong(final int idtemplate, int soLuong, final Char p) {
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        newItem.clazz = -1;
        newItem.lock = 1;
        // Nhanh hoa dao giftcode: 24.1% ST chuan, 50.1% vu khi, 29.0% ty le.
        if (idtemplate == 835) {
            newItem.createAttributeVuKhiThoiTrangMax();
        }
        // Skin Kim Cang Long (911): roll 2-3 dong chi so nhu duong GiftCode.AddItem (DB:).
        // Truoc 12/09/2026 duong ITEM: (bo chon thuong tren web sinh ra cho nhom skin) tao
        // mon voi 0 chi so. Ham tu return neu khong phai 911 nen goi vo dieu kien.
        newItem.createAttributeSkinKimCangLong();
        newItem.dateCreate = getDayTime(0L);
        if (newItem.isManhSuuTap()) {
            boolean found = false;
            for (Item item : p.iItems) {
                if (item.isManhSuuTap() && item.getTemplate().id == newItem.getTemplate().id) {
                    // Cộng dồn otherAtt[65]
                    item.otherAtt[65] += soLuong;
                    if (item.otherAtt[65] > 30000) {
                        item.otherAtt[65] = 30000;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                newItem.otherAtt[65] = (short) soLuong;
                p.iItems.add(newItem);
            }
        } else {
            // Manh suu tap cong don qua 1 item duy nhat (otherAtt[65] o tren) -
            // cac loai vat pham khac (vd trung thu type 26 trong giftcode) KHONG
            // co co che cong don, nen truoc day chi tao dung 1 item roi VAN bao
            // "nhan duoc soLuong X" - sai. Phai tao THAT su tung item rieng le.
            p.iItems.add(newItem);
            int daTao = 1;
            for (int i = 1; i < soLuong; i++) {
                if (p.isFullInventory()) {
                    break;
                }
                final Item themItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
                themItem.id = p.getIDItem();
                themItem.owner = p.charDBID;
                themItem.level = themItem.getTemplate().level;
                themItem.identify = p.charDBID;
                themItem.clazz = -1;
                themItem.lock = 1;
                if (idtemplate == 835) {
                    themItem.createAttributeVuKhiThoiTrangMax();
                }
                // Moi ban sao roll rieng - khong chep chi so cua newItem.
                themItem.createAttributeSkinKimCangLong();
                themItem.dateCreate = getDayTime(0L);
                p.iItems.add(themItem);
                daTao++;
            }
            soLuong = daTao;
        }
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được " + soLuong + " " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "item_soluong");
      
        return newItem;
    }

    public static Item createItemCaiTrang(final int idtemplate, final Char p, int lv) {
        if (p.isFullInventory()) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không nhận thêm được.", ""));
            return null;
        }
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.lock = 1;
        newItem.plus = (byte) lv;
   
        if (idtemplate == 903) {
            newItem.setAttributeByBatGioi();
           } else if (idtemplate == 904) {
            newItem.setAttributeBySaTang();
        }
      
        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "item_caitrang");
      
        return newItem;
    }

    public static Item createItemSuuTap(final int idtemplate, final Char p) {
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = 0;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.lock = 1;
        newItem.dateCreate = getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được   " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "suutap");
      
        return newItem;
    }

    public static Item createHoVeLenhLan(final int idtemplate, final Char p, final int minute, final int lock) {
        final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idtemplate), false, 0, 0, idtemplate);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        newItem.clazz = p.charClass;
        newItem.createAttChoiVinhVien();
        newItem.lock = (byte) lock;
        newItem.dateCreate = getDayTime(0L);
        if (minute > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
        }
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        final String info = String.valueOf(newItem.getDBInfo()) + "|" + newItem.getAttribute() + "_" + newItem.level;
        p.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nhận được " + newItem.getName(), ""));
        Database.instance.saveOrtherLog("", p.getName(), info, "nhanchoi");
        if (isDemNoel()) {
            p.receiveGiftEvent = 1;
        }
        return newItem;
    }

    public void createPet(final int idpet, final int time) {
        final Pet pet = new Pet(Map.petTemplate[idpet].idTemplate, Map.petTemplate[idpet].idImg, this.getIDItem(), 0, this.charDBID, Map.petTemplate[idpet].name);
        pet.totalMinute = time;
        pet.type = Map.petTemplate[idpet].type_move;
        pet.timeBuy = System.currentTimeMillis();
        this.pet.add(pet);
        Database.instance.savePet(pet);
        Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, String.valueOf(pet.getName()) + "_" + this.luong, "pet");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
        this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã mua được " + pet.getName(), ""));
    }

    public void createPetVinhVien(final int idpet) {
        final Pet pet = new Pet(Map.petTemplate[idpet].idTemplate, Map.petTemplate[idpet].idImg, this.getIDItem(), 0, this.charDBID, Map.petTemplate[idpet].name);
        pet.type = Map.petTemplate[idpet].type_move;
        this.pet.add(pet);
        Database.instance.savePet(pet);
        Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, String.valueOf(pet.getName()) + "_" + this.luong, "pet");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
        this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã mua được " + pet.getName(), ""));
    }

    public Pet createPet(final int idpet, final int time, final Item itemPet) {
        final Pet pet = new Pet(Map.petTemplate[idpet].idTemplate, Map.petTemplate[idpet].idImg, this.getIDItem(), 0, this.charDBID, Map.petTemplate[idpet].name, itemPet);
        pet.totalMinute = time;
        pet.type = Map.petTemplate[idpet].type_move;
        pet.timeBuy = System.currentTimeMillis();
        if (time > 0) {
            pet.totalMinute = time;
            pet.timeBuy = itemPet.timeLoan;
            pet.ispetthoihan = true;
        }
        this.pet.add(pet);
        pet.itemPet = itemPet;
        Database.instance.savePet(pet);
        Database.instance.saveOrtherLog("tob_log_other_animal", this.charname, String.valueOf(pet.getName()) + "_" + this.luong, "pet");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 2));
        this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã mua được " + pet.getName(), ""));
        return pet;
    }

    public String[] getInfoSaveTubinh() {
        final String[] info = {"", ""};
        for (int i = 0; i < this.tubinh.size(); ++i) {
            final String[] array = info;
            final int n = 0;
            array[n] = String.valueOf(array[n]) + this.tubinh.get(i).id + ",";
            final String[] array2 = info;
            final int n2 = 1;
            array2[n2] = String.valueOf(array2[n2]) + this.tubinh.get(i).getInfoSave() + "|";
        }
        if (!info[0].equals("")) {
            info[0] = info[0].substring(0, info[0].length() - 1);
        }
        if (!info[1].equals("")) {
            info[1] = info[1].substring(0, info[1].length() - 1);
        }
        return info;
    }

    public String[] getInfoSaveFruit() {
        final String[] info = {"", "", "", "", "", ""};
        if (this.fruit != null) {
            for (int i = 0; i < FruitTemplate.MAX_FRUIT; ++i) {
                if (this.fruit[i] != null) {
                    final String[] array = info;
                    final int n = 0;
                    array[n] = String.valueOf(array[n]) + this.fruit[i].id + ",";
                    final String[] array2 = info;
                    final int n2 = 1;
                    array2[n2] = String.valueOf(array2[n2]) + this.fruit[i].status + ",";
                    final String[] array3 = info;
                    final int n3 = 2;
                    array3[n3] = String.valueOf(array3[n3]) + this.fruit[i].tcount + ",";
                    final String[] array4 = info;
                    final int n4 = 3;
                    array4[n4] = String.valueOf(array4[n4]) + this.fruit[i].gift + ",";
                    final String[] array5 = info;
                    final int n5 = 4;
                    array5[n5] = String.valueOf(array5[n5]) + this.fruit[i].tcountTangtoc + ",";
                    final String[] array6 = info;
                    final int n6 = 5;
                    array6[n6] = String.valueOf(array6[n6]) + this.fruit[i].tcountKho + ",";
                }
            }
        }
        return info;
    }

    public void createTubinh() {
        if (this.tubinh.size() == 0) {
            for (int i = 0; i < TuBinhTemplate.ALL_TU_BINH.size(); ++i) {
                final TuBinhTemplate t = new TuBinhTemplate();
                t.id = (byte) i;
                this.tubinh.add(t);
            }
        } else if (this.tubinh.size() == 4) {
            for (int i = 4; i < TuBinhTemplate.ALL_TU_BINH.size(); ++i) {
                final TuBinhTemplate t = new TuBinhTemplate();
                t.id = (byte) i;
                this.tubinh.add(t);
            }
        } else if (this.tubinh.size() == 8) {
            for (int i = 8; i < TuBinhTemplate.ALL_TU_BINH.size(); ++i) {
                final TuBinhTemplate t = new TuBinhTemplate();
                t.id = (byte) i;
                this.tubinh.add(t);
            }
        } else if (this.tubinh.size() == 12) {
            for (int i = 12; i < TuBinhTemplate.ALL_TU_BINH.size(); ++i) {
                final TuBinhTemplate t = new TuBinhTemplate();
                t.id = (byte) i;
                this.tubinh.add(t);
            }
        }
    }

    public void doAddManhTubinh(final int idManhTemplate, final boolean lock) {
        if (lock && this.listGemitemLock[idManhTemplate] <= 0) {
            return;
        }
        if (!lock && this.listGemitem[idManhTemplate] - this.listGemitemSell[idManhTemplate] <= 0) {
            return;
        }
        int idTubinh = 0;
        if (idManhTemplate >= 159 && idManhTemplate <= 174) {
            idTubinh = (idManhTemplate - 159) / 4;
        } else if (idManhTemplate >= 179 && idManhTemplate <= 226) {
            idTubinh = (idManhTemplate - 179) / 4 + 4;
        }
        // initInfoFromDB chi dung this.tubinh khi nhan vat tu lv40 tro len, nen
        // nhan vat duoi lv40 co this.tubinh RONG. Truoc day get(idTubinh) nem
        // ArrayIndexOutOfBounds len tan Session.run -> nguoi choi bi vang ra.
        if (this.lvDetail.lv < 40) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Phải đạt cấp 40 mới ghép được tứ bình.", ""));
            return;
        }
        if (this.tubinh.isEmpty()) {
            this.createTubinh();
        }
        if (idTubinh < 0 || idTubinh >= this.tubinh.size()) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng mảnh này.", ""));
            return;
        }
        final TuBinhTemplate temp = this.tubinh.get(idTubinh);
        int indexManh = 0;
        if (idManhTemplate >= 159 && idManhTemplate <= 174) {
            indexManh = (idManhTemplate - 159) % 4;
        } else if (idManhTemplate >= 179 && idManhTemplate <= 226) {
            indexManh = (idManhTemplate - 179) % 4;
        }
        if (temp.addManh(indexManh, this)) {
            if (lock) {
                final int[] listGemitemLock = this.listGemitemLock;
                --listGemitemLock[idManhTemplate];
                final int[] allGemUseLock = this.allGemUseLock;
                ++allGemUseLock[idManhTemplate];
            } else {
                final int[] listGemitem = this.listGemitem;
                --listGemitem[idManhTemplate];
                final int[] allGemUse = this.allGemUse;
                ++allGemUse[idManhTemplate];
            }
            if (this.listGemitem[idManhTemplate] <= 0 && this.listGemitemLock[idManhTemplate] <= 0) {
                for (int i = 0; i < this.gemItem.size(); ++i) {
                    final GemItem g = this.gemItem.get(i);
                    if (g.idGemTemplate == idManhTemplate) {
                        this.gemItem.remove(g);
                        this.ALL_ID_USE.remove(g.realId);
                        break;
                    }
                }
            }
            MessageCreator.createMsgCharTuBinh(this);
            this.sendMessage(MessageCreator.createCharGemItem(this));
        } else {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không thể cộng thêm", ""));
        }
    }

    public void createFruit() {
        if (this.fruit[4] == null) {
            this.fruit[4] = new FruitTemplate();
            this.fruit[4].id = 4;
            this.fruit[4].tcount = System.currentTimeMillis();
            this.fruit[4].type = this.fruit[4].getTemplate().type;
        }
        if (this.fruit[1] == null) {
            this.fruit[1] = new FruitTemplate();
            this.fruit[1].id = 1;
            this.fruit[1].tcount = System.currentTimeMillis();
            this.fruit[1].type = this.fruit[1].getTemplate().type;
        }
        final byte[] id = {0, 2, 3, 5, 6};
        for (int i = 0; i < this.fruit.length; ++i) {
            if (this.fruit[i] == null) {
                this.fruit[i] = new FruitTemplate();
                this.fruit[i].id = id[Map.r.nextInt(id.length)];
                this.fruit[i].type = this.fruit[i].getTemplate().type;
                this.fruit[i].tcount = System.currentTimeMillis();
                this.fruit[i].tcountTangtoc = System.currentTimeMillis() + 600000L;
                this.fruit[i].initGift(this.lvDetail.lv);
            }
        }
    }

    public synchronized short getIDItem() {
        while (this.ALL_ID_USE.get(this.ID_ITEM) != null) {
            ++this.ID_ITEM;
        }
        this.ALL_ID_USE.put(this.ID_ITEM, this.ID_ITEM);
        return this.ID_ITEM;
    }

    public void removeIDItem(final short id) {
        this.ALL_ID_USE.remove(id);
    }

    public boolean isMasterClan() {
        return this.rankClan == 0;
    }

    public boolean isCharNam() {
        return this.gender == 1;
    }

    public boolean isCharNu() {
        return this.gender == 2;
    }

    // Bao cho client BIET la phien luyen tu dong da dung han. Bat buoc phai goi o MOI
    // duong that bai, vi client (ScreenDapDo) khoa man hinh bang co TINH static
    // isFinishImbue = false ngay khi bam "Cong N" va chi mo khoa khi nhan goi -69 co
    // byte cuoi = 1. Neu chi gui createServerAlertMessage thi hop thoai "Xin cho" dong
    // nhung co van ket false -> nhung lan bam sau bao "Khong the chon khi dang trong
    // qua trinh luyen tu dong" cho den khi thoat game.
    public void sendStopAutoImbue(final String info) {
        this.sendStopAutoImbue(info, true);
    }

    public void sendStopAutoImbue(final String info, final boolean showAlert) {
        this.startAutoImbue = false;
        final Message m = new Message(-69);
        try {
            m.dos.writeByte(1);
            m.dos.writeUTF(info);
            m.dos.writeByte(1);
            this.sendMessage(m);
            m.cleanup();
        } catch (final Exception ex) {
        }
        if (showAlert) {
            // Gui SAU goi -69: onAutoImbue goi Canvas.endDlg(), neu gui alert truoc thi
            // chinh endDlg() do se dong luon hop thoai bao loi vua hien.
            this.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }
    }

    public void doSetAutoImbue(final short idItem, final short[] idGem, final int plus, final byte[] lock) {
        this.startAutoImbue = false;
        // Xoa lua chon cua phien TRUOC. Truoc day hai truong nay khong duoc dat lai:
        // neu phien truoc co dung da bao hiem / da may man ma phien nay khong chon,
        // server van doc idGemBH/idGemMM cu va an luon da cua nguoi choi.
        this.idGemBH = -1;
        this.idGemMM = -1;
        this.BHLock = false;
        this.MMLock = false;
        if (idGem == null || lock == null || idGem.length < 3 || lock.length < 3) {
            // doan duoi doc idGem[0..2] khong dieu kien; goi tin thieu phan tu se nem
            // ArrayIndexOutOfBounds va nguoi choi chi thay treo o "Xin cho".
            this.sendStopAutoImbue("Dữ liệu luyện tự động không hợp lệ");
            return;
        }
        final Item item = this.getItemFormVector(this.iItems, idItem);
        if (item == null) {
            this.sendStopAutoImbue("Không tìm thấy vật phẩm");
            return;
        }
        try {
            if (Map.isItemSelled(this, item.id, item)) {
                // isItemSelled co nhanh tra ve true KHONG bao gi (item.isSelling ||
                // item.prizeSell > 0) - truoc day cho nay return im lang, client treo.
                this.sendStopAutoImbue("Không thể luyện khi vật phẩm đang được bán");
                return;
            }
            if (!Map.isItemCanImbue(item.getTemplate().type)) {
                this.sendStopAutoImbue("Không thể cường hoá trang bị này");
                return;
            }
            if (item.isManhSuuTap() || item.isTBSuuTap() || item.isCanh() || item.isCaiTrangSuuTap()) {
                this.sendStopAutoImbue("Không thể cường hoá ");
                return;
            }
        } catch (final Exception ex) {
            // lo #7 loi.xlsx: truoc day catch rong o day nuot loi va CHAY TIEP xuong
            // duoi nhu the moi kiem tra da qua het, co the khoi dong auto-imbue tren
            // mot item dang loi/hong template - dung han lai, khong chay tiep.
            this.sendStopAutoImbue("Không thể cường hoá vật phẩm này");
            return;
        }
        if (!GemTemplate.isLKD(idGem[0])) {
            this.sendStopAutoImbue("Chưa chọn luyện kim dược");
            return;
        }
        if (plus < 0 || plus > 15) {
            // lo #7 loi.xlsx: khong ep gioi han truoc day co the lam CharCapNhat.doAutoImbue
            // truy cap percent[item.plus] ngoai mang (mang chi co 15 phan tu, index 0-14)
            // neu self.plus > 15, nem ArrayIndexOutOfBoundsException bi nuot am tham o noi khac.
            this.sendStopAutoImbue("Mức cường hoá không hợp lệ");
            return;
        }
        this.plus = (byte) plus;
        this.idGemLkd = idGem[0];
        this.lkdLock = (lock[0] == 1);
        if (GemTemplate.isBH(idGem[1])) {
            this.idGemBH = idGem[1];
            this.BHLock = (lock[1] == 1);
        }
        if (GemTemplate.isDaMayMan(idGem[2])) {
            this.idGemMM = idGem[2];
            this.MMLock = (lock[2] == 1);
        }
        this.itemImbue = idItem;
        this.startAutoImbue = true;
    }

    boolean enoughtLkd() {
        if (this.idGemLkd <= 0) {
            return false;
        }
        if (this.lkdLock) {
            return this.listGemitemLock[this.idGemLkd] > 0;
        }
        return this.listGemitem[this.idGemLkd] > 0;
    }

    int getPcBHAuto() {
        if (this.idGemBH <= -1) {
            return 0;
        }
        if (this.BHLock) {
            if (this.listGemitemLock[this.idGemBH] <= 0) {
                this.idGemBH = -1;
                return 0;
            }
            return Map.gemTemplate[this.idGemBH].value;
        } else {
            if (this.listGemitem[this.idGemBH] <= 0) {
                this.idGemBH = -1;
                return 0;
            }
            return Map.gemTemplate[this.idGemBH].value;
        }
    }

    int getSLMM() {
        int maxMM = 0;
        if (this.idGemMM > -1) {
            maxMM = 7 - ((this.idGemBH > -1) ? 1 : 0);
            if (this.MMLock) {
                if (this.listGemitemLock[this.idGemMM] < maxMM) {
                    maxMM = this.listGemitemLock[this.idGemMM];
                }
            } else if (this.listGemitem[this.idGemMM] < maxMM) {
                maxMM = this.listGemitem[this.idGemMM];
            }
        }
        return maxMM;
    }

    int calculatePercenUp(final Char player, final int percent) {
        int pc = 0;
        if (Map.isSpesialLkd(this.idGemLkd)) {
            pc += Map.gemTemplate[this.idGemLkd].value;
        } else {
            pc += percent * Map.gemTemplate[this.idGemLkd].value / 100;
        }
        if (this.idGemMM > -1) {
            for (int slMM = this.getSLMM(), i = 0; i < slMM; ++i) {
                if (this.MMLock) {
                    if (this.listGemitemLock[this.idGemMM] > 0) {
                        pc += percent * Map.gemTemplate[this.idGemMM].value / 100;
                    }
                } else if (this.listGemitem[this.idGemMM] > 0) {
                    pc += percent * Map.gemTemplate[this.idGemMM].value / 100;
                }
            }
        }
        return pc;
    }

    public void doImbueItem(final Item item, final int endplus) {
        if (item != null && item.plus < 15) {
            for (int j = item.plus; j < endplus; ++j) {
                ++item.plus;
                final int[] plus = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};
                for (int i = 0; i < 10; ++i) {
                    final short[] atb = item.atb;
                    final int n = i;
                    atb[n] += (short) ((i > 0) ? 1 : plus[item.plus]);
                }
                if (item.plus == endplus) {
                    break;
                }
            }
        }
    }

    // Than method (130 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void doAutoImbue() {
        CharCapNhat.doAutoImbue(this);
    }

    public void setLuong(final int l) {
        this.luong = l;
        if (this.luong < 0) {
            this.luong = 0;
        }
    }

    // ==================== PHAT HIEN TIEN AM ====================
    // Truoc day getLuong()/getLuongLock()/getxu() goi thang Database.banChar() MOI LAN
    // duoc goi trong khi gia tri con am — nhung khong he sua gia tri am do, nen lan goi
    // sau van am, van ban tiep. Ba ham nay co hon 400 diem goi trong src/, rieng
    // MessageCreator.createCharInventoryMessage (gui moi lan tui do thay doi, moi lan
    // dang nhap, nhieu lan trong mot thao tac) da cham ca getLuong lan getxu.
    //
    // Ket qua la mot nhan vat am tien sinh ra banChar() lien tuc. Moi lan la:
    //   - mot cau UPDATE xep vao Logdata.query (Vector khong gioi han -> phinh bo nho)
    //   - va truoc ban va nay, mot ket noi MySQL ro ri VINH VIEN (Database.banChar
    //     lay ket noi ma khong tra lai pool).
    // Vai phut la het max_connections, va luc do CA SERVER mat DB chu khong rieng
    // nguoi choi do. Xem them ban va o Database.banChar.
    //
    // Nay: bao dung MOT LAN moi phien, va CHAN GIA TRI VE 0 ngay tai cho de tinh trang
    // tu lanh — co goi lai bao nhieu lan nua cung khong con so am de bao.
    private boolean daBaoAmLuong = false;
    private boolean daBaoAmLuongKhoa = false;
    private boolean daBaoAmXu = false;

    /** Da phat hien tien am trong phien nay chua? Xem getLuong()/getLuongLock()/getxu(). */
    public boolean daPhatHienAmTien() {
        return this.daBaoAmLuong || this.daBaoAmLuongKhoa || this.daBaoAmXu;
    }

    /** Ghi hack.txt + ban, boc kin de loi ghi log khong bao gio lam hong duong goi tien. */
    private void baoAmTien(final String loai, final String chiTiet) {
        try {
            data.NghiVanGianLan.ghiChung(this, loai, chiTiet);
        } catch (final Exception ignored) {
        }
        try {
            Database.instance.banChar(this, loai);
        } catch (final Exception ignored) {
        }
    }

    /**
     * Cong hai so int co chan tran: khong bao gio cuon vong sang so am.
     *
     * addLuong() truoc day cong thang vao truong int. Tran int la duong duy nhat lam
     * `luong` am ma khong qua ham sub nao — tuc la kip no cua ca chuoi loi tren.
     */
    public static int congChan(final int a, final int b) {
        final long tong = (long) a + (long) b;
        if (tong > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (tong < 0L) {
            return 0;
        }
        return (int) tong;
    }

    /**
     * Nhan hai so int co chan tran: khong bao gio cuon vong sang so am.
     *
     * Dung cho cac cho nhan he so TRUOC khi dua vao addLuong — vi du
     * DatabaseXepHang.getBoardNapTien lam `rs.getInt("luong") * Database.TileNap`, ca hai
     * ve deu la int nen phep nhan tinh o 32-bit va tran NGAY TAI CHO, truoc khi gia tri
     * kip di qua congChan cua addLuong.
     */
    public static int nhanChan(final int a, final int b) {
        final long tich = (long) a * (long) b;
        if (tich > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (tich < 0L) {
            return 0;
        }
        return (int) tich;
    }

    /**
     * Tru mot so long ra khoi mot so int, chan duoi tai 0.
     *
     * Cac ham subLuong* truoc day lam `this.luong -= (int) l`. Ep long ve int truoc khi
     * tru: l vuot 2^31 thi (int) l cuon vong va co the thanh so AM — tuc la TRU tien lai
     * thanh CONG tien. Tinh bang long roi moi chan nen khong con cua so do.
     */
    public static int truChan(final int hienCo, final long tru) {
        if (tru <= 0L) {
            return (hienCo > 0) ? hienCo : 0;
        }
        final long conLai = (long) hienCo - tru;
        return (conLai > 0L) ? (int) conLai : 0;
    }

    public int getLuong() {
        if (this.luong < 0) {
            final int cu = this.luong;
            this.luong = 0;
            if (!this.daBaoAmLuong) {
                this.daBaoAmLuong = true;
                this.baoAmTien("amLuong", "luong = " + cu);
            }
        }
        return this.luong;
    }

    public int getLuongLock() {
        if (this.lockLuong < 0) {
            final int cu = this.lockLuong;
            this.lockLuong = 0;
            if (!this.daBaoAmLuongKhoa) {
                this.daBaoAmLuongKhoa = true;
                this.baoAmTien("amluongLock", "luongKhoa = " + cu);
            }
        }
        return this.lockLuong;
    }

    // ==================== KHOA TIEN ====================
    // Moi phep doc-sua-ghi tren xu / luong / luongKhoa di qua khoa nay. Cac handler cua cung
    // mot nhan vat chay tren HAI luong (luong phien cho lenh rieng, luong ban do cho lenh
    // con lai - xem RealController.privateCmd), nen "kiem so du roi tru" o hai noi co the
    // xen nhau: ca hai deu thay du tien, ca hai deu tru, subXu chan ve 0 -> mot mon duoc
    // mua thieu tien. Hai ham truXuNeuDu / truLuongNeuDu lam kiem-va-tru trong MOT buoc
    // duoi khoa; cac duong tieu tien quan trong nen dung chung thay cho cap "getxu() <
    // gia" + "subXu(gia)".
    private final Object KHOA_TIEN = new Object();

    /** Tru xu neu con du. @return false (khong doi gi) khi thieu hoac so am. */
    public boolean truXuNeuDu(final long xu, final boolean isCountTop, final String vitri) {
        synchronized (KHOA_TIEN) {
            if (xu < 0L || this.xu < xu) {
                return false;
            }
            this.subXu(xu, isCountTop, vitri);
            return true;
        }
    }

    /** Tru luong (mo) neu con du. @return false (khong doi gi) khi thieu hoac so am. */
    public boolean truLuongNeuDu(final long l) {
        synchronized (KHOA_TIEN) {
            if (l < 0L || this.luong < l) {
                return false;
            }
            this.subLuong(l);
            return true;
        }
    }

    /** Tru luong khoa neu con du. @return false (khong doi gi) khi thieu hoac so am. */
    public boolean truLuongKhoaNeuDu(final long l) {
        synchronized (KHOA_TIEN) {
            if (l < 0L || this.lockLuong < l) {
                return false;
            }
            this.subLuongLock(l);
            return true;
        }
    }

    /**
     * Ban tong quat cua truXuNeuDu/truLuongNeuDu cho cac giao dich doi qua tuy y (vi du
     * doi lượng+potions[] lay qua/nguyen lieu o cac NPC su kien). Goi ham kiem tra dieu
     * kien va ham ap dung thay doi trong CUNG mot khoa KHOA_TIEN: neu dieu kien sai thi
     * apDung khong bao gio chay va khong co gi bi doi. Dung khi mot handler kiem "con du
     * X/Y khong" roi moi tru X, tru Y rieng le (check-then-act) - cung loi voi getxu()<gia
     * roi subXu(gia) nhung tren nhieu truong/nhieu mang potions[] cung luc nen khong the
     * dung thang truXuNeuDu/truLuongNeuDu.
     */
    public boolean truNeuDu(final java.util.function.BooleanSupplier duDieuKien, final Runnable apDung) {
        synchronized (KHOA_TIEN) {
            if (!duDieuKien.getAsBoolean()) {
                return false;
            }
            apDung.run();
            return true;
        }
    }

    public void addLuong(final int luong) {
        synchronized (KHOA_TIEN) {
            this.luong = congChan(this.luong, luong);
        }
    }

    public void addLuongLock(final int luong) {
        synchronized (KHOA_TIEN) {
            this.lockLuong = congChan(this.lockLuong, luong);
        }
    }

    // ==================== SKIN LINH THU (xem real.SkinLinhThu) ====================
    /**
     * -1 = khong mang skin; 0..4 = Phuc Thu / Gau Truc / Tien Loc / Xich Duong / Chien Lang.
     *
     * KHONG luu rieng xuong DB: skin do vat pham dang mac o bang Trang bi thu quyet dinh,
     * ma vat pham thi da nam san trong awItems (duoc luu qua getAllItemInfoSave). Moi lan
     * dang nhap / mac do / cuoi thu deu goi SkinLinhThu.dongBoTheoTrangBi de tinh lai, nen
     * truong nay chi la ban dem tam, khong bao gio lech voi vat pham.
     */
    public int idSkinLinhThu = SkinLinhThu.KHONG_CO;

    /**
     * Trang thai noi tai cua skin: moc het hieu luc va moc het hoi chieu, tra theo so
     * hieu skin (moi skin dung mot noi tai). Xem real.SkinLinhThu.
     *
     * CHI NAM TRONG BO NHO, khong co cot CSDL: hoi chieu 40 giay ngan hon moi phien
     * choi, nen mat khi dang xuat la dung — dang nhap lai thi noi tai san sang, khong ai
     * mat gi. Them cot chi de luu 40 giay la them mot cho co the lech.
     *
     * Tra theo so hieu skin chu khong phai mot cap bien: doi skin khi noi tai dang chay
     * thi buff cua skin cu khong theo sang skin moi, va hoi chieu cua tung skin dem
     * rieng — dung y, khong phai tinh co.
     */
    public final long[] skinHetHieuLuc = new long[SkinLinhThu.SO_SKIN];
    public final long[] skinHetHoiChieu = new long[SkinLinhThu.SO_SKIN];

    // ==================== HAN MUC LUONG NHAT DUOC KHI TRAIN QUAI ====================
    // Moi nhan vat, moi ngay: toi da MAX_LUONG_TRAIN_NGAY luong mo va MAX_LUONG_TRAIN_NGAY
    // luong khoa. Han muc luu trong bang tob_train_luong kem ngay, nen bao tri / restart
    // server khong lam reset. Ngoai han muc ngay, con them nguong dang giu trong nguoi:
    // qua MAX_LUONG_KHOA_DANG_GIU / MAX_LUONG_MO_DANG_GIU thi khong nhan them tu train quai
    // nua (xem addLuongTrainQuai), du han muc ngay chua het.
    public static final int MAX_LUONG_TRAIN_NGAY = 50000;
    public static final int MAX_LUONG_KHOA_DANG_GIU = 50000;
    public static final int MAX_LUONG_MO_DANG_GIU = 50000;

    public String ngayTrainLuong = "";
    public int luongMoTrainNgay = 0;
    public int luongKhoaTrainNgay = 0;
    public boolean daLoadTrainLuong = false;
    public boolean trainLuongDirty = false;

    /** Nap han muc tu DB (1 lan moi phien) va reset khi sang ngay moi. */
    private void checkNgayTrainLuong() {
        if (!this.daLoadTrainLuong) {
            this.daLoadTrainLuong = true;
            try {
                Database.instance.loadTrainLuong(this);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final String homNay = getDayOpen(0L);
        if (!homNay.equals(this.ngayTrainLuong)) {
            this.ngayTrainLuong = homNay;
            this.luongMoTrainNgay = 0;
            this.luongKhoaTrainNgay = 0;
            this.trainLuongDirty = true;
        }
    }

    /** Con duoc nhan bao nhieu luong tu train quai trong hom nay. */
    public int conLaiLuongTrainNgay(final boolean khoa) {
        checkNgayTrainLuong();
        final int daNhan = khoa ? this.luongKhoaTrainNgay : this.luongMoTrainNgay;
        final int conLai = MAX_LUONG_TRAIN_NGAY - daNhan;
        return (conLai > 0) ? conLai : 0;
    }

    /**
     * Cong luong nhat duoc tu train quai, tu dong cat theo han muc con lai cua ngay.
     *
     * @param khoa true = luong khoa, false = luong mo
     * @return so luong thuc su duoc cong (0 neu da het han muc)
     */
    public int addLuongTrainQuai(int soluong, final boolean khoa) {
        if (soluong <= 0) {
            return 0;
        }
        final int dangGiu = khoa ? this.getLuongLock() : this.getLuong();
        final int nguongDangGiu = khoa ? MAX_LUONG_KHOA_DANG_GIU : MAX_LUONG_MO_DANG_GIU;
        final int conLaiDangGiu = nguongDangGiu - dangGiu;
        if (conLaiDangGiu <= 0) {
            return 0;
        }
        if (soluong > conLaiDangGiu) {
            soluong = conLaiDangGiu;
        }
        final int conLai = conLaiLuongTrainNgay(khoa);
        if (conLai <= 0) {
            return 0;
        }
        if (soluong > conLai) {
            soluong = conLai;
        }
        if (khoa) {
            this.addLuongLock(soluong);
            this.luongKhoaTrainNgay += soluong;
        } else {
            this.addLuong(soluong);
            this.luongMoTrainNgay += soluong;
        }
        this.trainLuongDirty = true;
        try {
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        } catch (final Exception e) {
        }
        return soluong;
    }

    public void addXu(final long xu, final String vitri) {
        // 16/09/2026: them so du TRUOC giao dich (truoc) theo yeu cau nguoi dung "log chi tiet
        // hon" - truoc day chi co so du SAU, muon biet truoc phai tu tinh nguoc (this.xu - xu),
        // khong an toan neu co nhieu luong cung cong/tru gan nhau (rang buoc chi trong KHOA_TIEN).
        final long truoc;
        synchronized (KHOA_TIEN) {
            truoc = this.xu;
            this.xu = congChanDai(this.xu, xu);
            this.xutim += xu;
        }
        if (xu != 0L) {
            Database.instance.saveOrtherLog("", this.charname,
                    truoc + " + " + xu + " = " + this.xu + " tu vi tri " + vitri, "nhanxu");
        }
    }

    public void subXu(final long xu, final boolean isCountTop, final String vitri) {
        if (xu < 0L) {
            return;
        }
        boolean thieu = false;
        final long truoc;
        synchronized (KHOA_TIEN) {
            truoc = this.xu;
            thieu = this.xu < xu;
            this.xu -= xu;
            this.xumat += xu;
            if (this.xu < 0L) {
                this.xu = 0L;
            }
        }
        if (xu != 0L) {
            // 16/09/2026: them so du TRUOC (truoc), doi xung voi addXu - theo yeu cau nguoi
            // dung "log chi tiet hon" cho xu (nhan xu VA tru xu).
            Database.instance.saveOrtherLog("", this.charname,
                    truoc + " - " + xu + " = " + this.xu + " tu vi tri " + vitri, "truxu");
        }
        if (thieu) {
            // Bi tru nhieu hon so dang co: noi goi khong kiem so du, hoac hai luong cung tieu.
            // Ghi DB de doi chieu (khong ghi hackdo.txt: nhieu duong hop le van goi subXu khi
            // nguoi choi da het xu).
            Database.instance.saveOrtherLog("", this.charname, "tru " + xu + " xu khi khong du, vi tri " + vitri, "truthieu");
        }
        if (isSuKienNoel()) {
        }
    }

    /** Cong hai so long co chan tran. Xem chu thich o congChan(int,int). */
    public static long congChanDai(final long a, final long b) {
        final long tong = a + b;
        // Tran long: hai so cung dau ma ket qua doi dau.
        if (((a ^ tong) & (b ^ tong)) < 0L) {
            return (a > 0L) ? Long.MAX_VALUE : 0L;
        }
        return (tong < 0L) ? 0L : tong;
    }

    public long getxu() {
        if (this.xu < 0L) {
            final long cu = this.xu;
            this.xu = 0L;
            if (!this.daBaoAmXu) {
                this.daBaoAmXu = true;
                this.baoAmTien("amXu", "xu = " + cu);
            }
        }
        return this.xu;
    }

    public void setxu(final long money) {
        this.xu = money;
        if (this.xu < 0L) {
            this.xu = 0L;
        }
    }

    public void subLuong(final long l) {
        if (l < 0L) {
            return;
        }
        boolean thieu = false;
        synchronized (KHOA_TIEN) {
            thieu = (long) this.luong < l;
            this.luong = truChan(this.luong, l);
            this.luonglost += l;
        }
        if (thieu) {
            Database.instance.saveOrtherLog("", this.charname, "tru " + l + " luong khi khong du", "truthieu");
        }
        this.diemxaiVip = congChan(this.diemxaiVip, (int) Math.min(l, (long) Integer.MAX_VALUE));
        // Database.instance.addCharXaiNapLuong(this, 1);
        isSuKienNoel();
        if (getDayTime(0L).compareTo("2023-04-16 00:00:00") >= 0 && getDayTime(0L).compareTo("2023-06-01 00:00:00") <= 0) {
            Database.instance.updateLuongNap((int) l, this);
        }
        if (isSuKienNoel2023()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent_ND(this, 2, 0, (int) l);
        }
        if (isSuKienTrungThul2016()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent_ND(this, 2, 0, (int) l);
            // "Diem tieu xai" rieng cho Trung Thu (moi 19/09/2026, spec "cu xai 2 luong thi
            // duoc 1 diem") - KHONG dung chung luongxai/luongxainhanRuong (bi cac su kien
            // khac dung chung, xem chu thich khai bao luongDaXaiTrungThu).
            this.luongDaXaiTrungThu += l;
        }
        if (((TeamServer.isServerHoaLu() || TeamServer.isServerLocal()) && isSuKienGioTo2016()) || isSuKienMini()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent(this, 2, 0, (int) l);

            final long a = this.luongxai / 100L;
            if (a > 0L) {
                final int x = 300000;
                this.addXu(a * x, "char 11");
                Database.instance.saveOrtherLog("", this.charname, "nhan " + a * x + " xu khi xai luong : lx= " + this.luongxai + " con: " + (this.luongxai - a * 100L), "xl");
                this.luongxai -= a * 100L;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + a * x + " xu khi xài lượng"));
            }
        }
        if (isSuKienMiniChucNu()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent_MiniCN(this, 2, 0, (int) l);
//            System.out.println("Đã + 1 lượng vào điểm top event" + this.luongxai);
            final long a = this.luongxai / 5000L;
            if (a > 0L) {
                final int[] potions = this.potions;
                final int n = 118;
                potions[n] += (int) a;
                Database.instance.saveOrtherLog("", this.charname, "nhan " + a + " ve qua so ph khi xai luong : lx= " + this.luongxai + " con: " + (this.luongxai - a * 5000L), "xl");
                this.luongxai -= a * 5000L;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + a + " vé quay Phượng Hoàng khi xài lượng"));
            }
        }
        if (isSuKienHaloween2016()) {
            long totalLuongDaTieu = 0; // Biến mới để theo dõi tổng lượng đã tiêu
            this.luongxai += l;
            // Tính điểm từ việc tiêu 100 lượng
            totalLuongDaTieu += l; // Cộng thêm lượng tiêu dùng vào tổng

            // Tính điểm từ việc tiêu 100 lượng
            if (totalLuongDaTieu >= 100) {
                long points = totalLuongDaTieu / 100; // Tính số điểm
                this.luongxainhanRuong += points; // Cộng điểm vào số điểm hiện có
                totalLuongDaTieu -= points * 100; // Giữ lại lượng tiêu dùng dư cho lần sau
            }
            
            Database.instance.addCharEventHLW(this, 2, 0, (int) l, (int)this.luongxainhanRuong);
        }
        if (SuKien.bat(SuKien.VU_LAN)) {
            // "Tieu 2 luong = 1 diem tieu xai, du 100 diem doi duoc 1 nhanh hoa hong" ->
            // gop lam 1 nguong duy nhat: cu 200 luong tieu thi tu dong tang 1 Hoa hong do
            // (khong can ghe NPC), dung field rieng luongXaiVuLan (xem khai bao field).
            this.luongXaiVuLan += l;
            if (this.luongXaiVuLan >= 200L) {
                long soHoaHong = this.luongXaiVuLan / 200L;
                this.luongXaiVuLan -= soHoaHong * 200L;
                this.potions[Potion.TYPE_HOA_HONG_DO] += (int) soHoaHong;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + soHoaHong + " hoa hồng đỏ (điểm tiêu xài)"));
                Database.instance.saveOrtherLog("", this.charname, "nhan " + soHoaHong + " hoa hong do tu diem tieu xai", "vulan");
            }
        }
    }

    public void subLuongTang(final long l) {
        if (l < 0L) {
            return;
        }
        synchronized (KHOA_TIEN) {
            this.luong = truChan(this.luong, l);
            this.luonglost += l;
        }
        isSuKienNoel();
        if (((TeamServer.isServerHoaLu() || TeamServer.isServerLocal()) && isSuKienGioTo2016()) || isSuKienMini()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent(this, 2, 0, (int) l);
            final long a = this.luongxai / 100L;
            if (a > 0L) {
                final int x = 300000;
                this.addXu(a * x, "char 11");
                Database.instance.saveOrtherLog("", this.charname, "nhan " + a * x + " xu khi xai luong : lx= " + this.luongxai + " con: " + (this.luongxai - a * 100L), "xl");
                this.luongxai -= a * 100L;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + a * x + " xu khi xài lượng"));
            }
        }
        if (isSuKienMiniChucNu()) {
            this.luongxai += l;
            this.luongxainhanRuong += l;
            Database.instance.addCharEvent_MiniCN(this, 2, 0, (int) l);
            final long a = this.luongxai / 5000L;
            if (a > 0L) {
                final int[] potions = this.potions;
                final int n = 118;
                potions[n] += (int) a;
                Database.instance.saveOrtherLog("", this.charname, "nhan " + a + " ve qua so ph khi xai luong : lx= " + this.luongxai + " con: " + (this.luongxai - a * 5000L), "xl");
                this.luongxai -= a * 5000L;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + a + " vé quay Phượng Hoàng khi xài lượng"));
            }
        }
        if (SuKien.bat(SuKien.VU_LAN)) {
            this.luongXaiVuLan += l;
            if (this.luongXaiVuLan >= 200L) {
                long soHoaHong = this.luongXaiVuLan / 200L;
                this.luongXaiVuLan -= soHoaHong * 200L;
                this.potions[Potion.TYPE_HOA_HONG_DO] += (int) soHoaHong;
                this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                this.sendMessage(MessageCreator.createMsgChat(this.id, "Nhận được " + soHoaHong + " hoa hồng đỏ (điểm tiêu xài)"));
                Database.instance.saveOrtherLog("", this.charname, "nhan " + soHoaHong + " hoa hong do tu diem tieu xai", "vulan");
            }
        }
    }

    public void subLuongLock(final long l) {
        if (l < 0L) {
            return;
        }
        synchronized (KHOA_TIEN) {
            this.lockLuong = truChan(this.lockLuong, l);
        }
    }

    public void doFinishAutoInbue() {
        if (this.startAutoImbue) {
            this.startAutoImbue = false;
            final Message m = new Message(-69);
            try {
                m.dos.writeByte(1);
                m.dos.writeUTF("Hoàn thành");
                m.dos.writeByte(1);
                this.sendMessage(m);
            } catch (final Exception ex) {
            }
        }
    }

    public static synchronized void addLuongNapToTop(final Char p) {
        try {
            if (p.luongNap >= 10000L) {
                if (Map.nameTopNapLuong == null || Map.nameTopNapLuong.equals("")) {
                    Map.nameTopNapLuong = p.charname;
                    Map.maxLuongNap = p.luongNap;
                    Database.instance.saveOrtherLog("", p.charname, "nhan dc top 10k " + p.luongNap, "topluong");
                }
                Logger.logStringWithDate(String.valueOf(p.charname) + "_" + p.luongNap + "_" + p.luong, "logtopluong.txt");
            }
        } catch (final Exception ex) {
        }
    }

    public static void initTopNapLuong(final String info) {
        try {
            if (info == null || info.equals("")) {
                return;
            }
            final String[] data = split(info, ",");
            Map.nameTopNapLuong = data[0];
            Map.maxLuongNap = Long.parseLong(data[1]);
        } catch (final Exception ex) {
        }
    }

    public static String getInfoTopLuongSave() {
        return String.valueOf(Map.nameTopNapLuong) + "," + Map.maxLuongNap;
    }

    public static synchronized String[] getTopListCharNapLuong() {
        final String[] list = new String[Char.TOP_LUONG.size()];
        final String[] name = new String[Char.TOP_LUONG.size()];
        final long[] ll = new long[Char.TOP_LUONG.size()];
        synchronized (Char.TOP_LUONG) {
            int i = 0;
            for (final TopLuong top : Char.TOP_LUONG.values()) {
                name[i] = top.name;
                ll[i] = top.luongNap;
                ++i;
            }
            i = 0;
            int j;
            int n;
            int iMin;
            long a;
            String b;
            for (j = 0, n = name.length, j = 0; j < n - 1; ++j) {
                iMin = j;
                for (i = j + 1; i < n; ++i) {
                    if (ll[i] > ll[j]) {
                        iMin = i;
                    }
                }
                if (iMin != j) {
                    a = ll[j];
                    b = name[j];
                    ll[j] = ll[iMin];
                    ll[iMin] = a;
                    name[j] = name[iMin];
                    name[iMin] = b;
                }
            }
            for (j = 0; j < name.length; ++j) {
                list[j] = String.valueOf(name[j]) + " " + ll[j] + " lượng";
            }
            //monitorexit(Char.TOP_LUONG);
        }
        return list;
    }

    public static void initTongKim(final String info) {
        try {
            final String[] data = split(info, ",");
            MapTongKim.ID_TONG_KIM = (short) (data.length / 2);
        } catch (final Exception ex) {
        }
    }

    public static synchronized String[] getTopListCharUseLuong() {
        final String[] list = new String[Char.TOP_CHAR_USE_LUONG.size()];
        final String[] name = new String[Char.TOP_CHAR_USE_LUONG.size()];
        final int[] ll = new int[Char.TOP_CHAR_USE_LUONG.size()];
        final Set<String> set = Char.TOP_CHAR_USE_LUONG.keySet();
        final Iterator<String> itr = set.iterator();
        int i = 0;
        while (itr.hasNext()) {
            final String str = itr.next();
            final int value = Char.TOP_CHAR_USE_LUONG.get(str);
            name[i] = str;
            ll[i] = value;
            ++i;
        }
        i = 0;
        int j;
        int n;
        int iMin;
        int a;
        String b;
        for (j = 0, n = name.length, j = 0; j < n - 1; ++j) {
            iMin = j;
            for (i = j + 1; i < n; ++i) {
                if (ll[i] > ll[j]) {
                    iMin = i;
                }
            }
            if (iMin != j) {
                a = ll[j];
                b = name[j];
                ll[j] = ll[iMin];
                ll[iMin] = a;
                name[j] = name[iMin];
                name[iMin] = b;
            }
        }
        for (j = 0; j < name.length; ++j) {
            list[j] = String.valueOf(name[j]) + " " + ll[j] + " lượng";
        }
        return list;
    }

    public static String getTopCharUseLuong() {
        return new StringBuilder(String.valueOf(MapTongKim.ID_TONG_KIM)).toString();
    }

    public static synchronized void addCharUsingLuong(final Char p, final int luongLost) {
    }

    // Than method (151 dong) da chuyen sang CharCapNhat.java. Chu ky GIU NGUYEN.
    public void doPlayerMove(final Message message) {
        CharCapNhat.doPlayerMove(this, message);
    }

    public static boolean isOnLocaltion(final int x, final int y) {
        for (int i = 0; i < Char.lcx_chan_nui_chau_bau.length; ++i) {
            final int xs = Char.lcx_chan_nui_chau_bau[i][0] * 16;
            final int xe = xs + Char.lcx_chan_nui_chau_bau[i][1] * 16;
            final int ys = Char.lcy_chan_nui_chau_bau[i][0] * 16;
            final int ye = ys + Char.lcy_chan_nui_chau_bau[i][1] * 16;
            if (x >= xs && x <= xe && y >= ys && y <= ye) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnLocaltionSanhChoLoiDai(final int x, final int y) {
        final int xs = 256;
        final int xe = xs + 128;
        final int ys = 592;
        final int ye = ys + 64;
        return x >= xs && x <= xe && y >= ys && y <= ye;
    }

    public boolean checkToPoint(final Positions p1, final Positions p2) {
        if (p1.x1 == p2.x1 && this.checkLineX(p1.y1, p2.y1, p1.x1)) {
            return true;
        }
        if (p1.y1 == p2.y1 && this.checkLineY(p1.x1, p2.x1, p1.y1)) {
            return true;
        }
        int t = -1;
        return (t = this.checkRectX(p1, p2)) != -1 || (t = this.checkRectY(p1, p2)) != -1 || (t = this.checkMoreLineX(p1, p2, 1)) != -1 || (t = this.checkMoreLineX(p1, p2, -1)) != -1 || (t = this.checkMoreLineY(p1, p2, 1)) != -1 || (t = this.checkMoreLineY(p1, p2, -1)) != -1;
    }

    public boolean checkLineX(final int y1, final int y2, final int x) {
        final int min = Math.min(y1, y2);
        for (int max = Math.max(y1, y2), y3 = min; y3 <= max; ++y3) {
            if ((this.map.type[y3 * this.map.w + x] & 0x2) == 0x2) {
                return false;
            }
        }
        return true;
    }

    public boolean checkLineY(final int x1, final int x2, final int y) {
        final int min = Math.min(x1, x2);
        for (int max = Math.max(x1, x2), x3 = min; x3 <= max; ++x3) {
            if ((this.map.type[y * this.map.w + x3] & 0x2) == 0x2) {
                return false;
            }
        }
        return true;
    }

    public int checkRectX(final Positions p1, final Positions p2) {
        Positions pMinY = p1;
        Positions pMaxY = p2;
        if (p1.y1 > p2.y1) {
            pMinY = p2;
            pMaxY = p1;
        }
        for (int y = pMinY.y1 + 1; y < pMaxY.y1; ++y) {
            if (this.checkLineX(pMinY.y1, y, pMinY.x1) && this.checkLineY(pMinY.x1, pMaxY.x1, y) && this.checkLineX(y, pMaxY.y1, pMaxY.x1)) {
                return y;
            }
        }
        return -1;
    }

    public int checkRectY(final Positions p1, final Positions p2) {
        Positions pMinX = p1;
        Positions pMaxX = p2;
        if (p1.x1 > p2.x1) {
            pMinX = p2;
            pMaxX = p1;
        }
        for (int x = pMinX.x1 + 1; x < pMaxX.x1; ++x) {
            if (this.checkLineY(pMinX.x1, x, pMinX.y1) && this.checkLineX(pMinX.y1, pMaxX.y1, x) && this.checkLineY(x, pMaxX.x1, pMaxX.y1)) {
                return x;
            }
        }
        return -1;
    }

    public int checkMoreLineX(final Positions p1, final Positions p2, final int type1) {
        Positions pMinY = p1;
        Positions pMaxY = p2;
        if (p1.y1 > p2.y1) {
            pMinY = p2;
            pMaxY = p1;
        }
        int y = pMaxY.y1;
        int row = pMinY.x1;
        if (type1 == -1) {
            y = pMinY.y1;
            row = pMaxY.x1;
        }
        if (this.checkLineX(pMinY.y1, pMaxY.y1, row)) {
            while ((this.map.type[y * this.map.w + pMinY.x1] & 0x2) != 0x2 && (this.map.type[y * this.map.w + pMaxY.x1] & 0x2) != 0x2) {
                if (this.checkLineY(pMinY.x1, pMaxY.x1, y)) {
                    return y;
                }
                y += type1;
            }
        }
        return -1;
    }

    private int checkMoreLineY(final Positions p1, final Positions p2, final int type1) {
        Positions pMinX = p1;
        Positions pMaxX = p2;
        if (p1.x1 > p2.x1) {
            pMinX = p2;
            pMaxX = p1;
        }
        int x = pMaxX.x1;
        int col = pMinX.y1;
        if (type1 == -1) {
            x = pMinX.x1;
            col = pMaxX.y1;
        }
        if (this.checkLineY(pMinX.x1, pMaxX.x1, col)) {
            while ((this.map.type[pMinX.y1 * this.map.w + x] & 0x2) != 0x2 && (this.map.type[pMaxX.y1 * this.map.w + x] & 0x2) != 0x2) {
                if (this.checkLineX(pMinX.y1, pMaxX.y1, x)) {
                    return x;
                }
                x += type1;
            }
        }
        return -1;
    }

    public void doMove(final int x, final int y) {
        try {
            final long now = System.currentTimeMillis();
            if (this.beStune || this.freeze() || this.isStune()) {
                final Message m = new Message(4);
                this.writeActorPos(m, this);
                this.sendMessage(m);
                return;
            }
            if (this.map.givingCard(this)) {
                final Message m = new Message(4);
                this.writeActorPos(m, this);
                this.sendMessage(m);
                return;
            }
            if ((this.x != x || this.y != y) && this.hp <= 0) {
                this.map.doUseHP(this, (short) 4, -1000000);
                this.actorDie();
                return;
            }
            if (this.map.mapId == Map.idMapTown && Map.getTown[this.inCountry] && !MapTown.congthanh.get(this.inCountry).isDead && !this.isAdmin && y < 1206) {
                this.y = 1280;
                final Message m = new Message(4);
                this.writeActorPos(m, this);
                this.sendMessage(m);
                return;
            }
            if (this.x == x && this.y == y) {
                return;
            }
            this.isThodia = false;
            if (this.hp > 0) {
                int distance = (this.mapID == 17) ? 96 : 50;
                if (this.checkTanPhe()) {
                    distance = ((this.mapID == 17) ? 48 : 25);
                }
                this.timeMove = System.currentTimeMillis();
                this.lastMovex = 0;
                this.lastMovey = 0;
                if (Map.abs(this.x - x) <= distance && Map.abs(this.y - y) <= distance) {
                    this.x = x;
                    this.y = y;
                    this.sendInfoMove2Near();
                    if (this.charCopy != null) {
                        this.charCopy.setXtoYto(x, y);
                    }
                    if (this.charHire != null && !Map.inRangeActor(this, this.charHire, 60)) {
                        this.charHire.setXtoYto(x, y);
                    }
                } else {
                    final Message i = new Message(4);
                    this.writeActorPos(i, this);
                    this.sendMessage(i);
                }
            }
            this.moved = true;
        } catch (final Exception ex) {
        }
    }

    public void setXtoYto(final int x, final int y) {
    }

    @Override
    public boolean getPaintHat() {
        final short[] idModel = this.getIDModel();
        return (idModel[0] == 26 && idModel[1] == 50) || idModel[0] != 26;
    }

    public void doChangeMapCharHire() {
        try {
            if (this.charHire != null) {
                try {
                    final Message msg = new Message(8);
                    msg.dos.writeShort(this.charHire.id);
                    this.charHire.map.sendAllPlayer(msg, this.charHire.inCountry, this.region);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                this.charHire.mapID = this.mapID;
                this.charHire.map = this.map;
                this.charHire.setXtoYto(this.x + 32, this.y + 32);
                this.charHire.region = this.region;
                this.charHire.inCountry = this.inCountry;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void doChangeMapMonster(final boolean onMapMonster, final Map oldMap) {
        if (this.monster != null && onMapMonster && this.map.mapId == this.map.mapIDLoadMap && this.map.isMapTrain()) {
            this.monster.x = this.x;
            this.monster.y = this.y;
            this.monster.removeOutMap();
            try {
                final Message m = new Message(90);
                m.dos.writeShort(this.monster.id);
                m.dos.writeByte(this.monster.cat);
                Map.sendAllCharServerInMap(new int[]{0, 70, 301, 1701, 1, 2, 3, 4, 5, 6, 7, 8, 9, 118}, m);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            this.monster.map = this.map;
            this.monster.region = this.region;
            this.monster.inCountry = this.inCountry;
        }
        try {
            if (this.charthanthu != null) {
                try {
                    final Message msg = new Message(8);
                    msg.dos.writeShort(this.charthanthu.id);
                    this.charthanthu.map.sendAllPlayer(msg, this.charthanthu.inCountry, this.region);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                this.charthanthu.map = this.map;
                this.charthanthu.mapID = this.mapID;
                this.charthanthu.setXtoYto(this.x + 48, this.y + 48);
                this.charthanthu.region = this.region;
                this.charthanthu.inCountry = this.inCountry;
            }
        } catch (final Exception ex) {
        }
        try {
            if (this.charHire != null) {
                try {
                    final Message msg = new Message(8);
                    msg.dos.writeShort(this.charHire.id);
                    this.charHire.map.sendAllPlayer(msg, this.charHire.inCountry, this.region);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                this.charHire.map = this.map;
                this.charHire.mapID = this.mapID;
                this.charHire.setXtoYto(this.x + 32, this.y + 32);
                this.charHire.region = this.region;
                this.charHire.inCountry = this.inCountry;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (this.charCopy != null && this.charCopy.map.mapId == oldMap.mapId && this.charCopy.region == this.region) {
            if (this.charCopy.isFollow()) {
                try {
                    final Message msg = new Message(8);
                    msg.dos.writeShort(this.charCopy.id);
                    this.charCopy.map.sendAllPlayer(msg, this.charCopy.myCountry);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                this.charCopy.map = this.map;
                this.charCopy.setXtoYto(this.x + 17, this.y + 16);
                this.charCopy.region = this.region;
                this.charCopy.inCountry = this.inCountry;
            } else {
                this.sendMessage(this.writeActorPos(new Message(4), this.charCopy));
            }
        }
    }

    public boolean isFollow() {
        return false;
    }

    /** So phut toi da co the dong (Hat mam, Trung Thu 2026 - yeu cau nguoi dung). */
    public static final int MAX_PHUT_HAT_MAM = 1080;

    public void setTimeEatSocola() {
        // Trung Thu 2026 (sua 19/09/2026): CONG DON voi thoi gian con lai thay vi luon
        // dat cung = 180, nhung khong vuot tran MAX_PHUT_HAT_MAM (1080 phut) - truoc day
        // dung hat mam moi luon ghi de = 180 du dang con du thoi gian tu truoc.
        this.minuteSocola = (short) Math.min(MAX_PHUT_HAT_MAM, this.minuteSocola + 180);
        this.timeEatSocola = System.currentTimeMillis();
        this.sendMessage(MessageCreator.createServerAlertMessage("Thời gian sử dụng hạt mầm của bạn còn " + this.minuteSocola + " phút", ""));
    }

    public boolean canReceiveGiftEvent() {
        return this.receiveGiftEvent == 0 && (getDayOpen(0L).compareTo("2020-08-11") <= 0 || getDayOpen(0L).equals("2020-08-12") || getDayOpen(0L).equals("2020-08-13") || getDayOpen(0L).equals("2020-08-14") || getDayOpen(0L).equals("2020-08-15"));
    }

    public void receiveGiftEvent(final int ma_vat) {
        try {
            if (this.isFullInventory()) {
                this.sendMessage(MessageCreator.createServerAlertMessage("Rương đầy", ""));
                return;
            }
            final int time = (Map.r.nextInt(5) + 1) * 60;
            final Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(616, this, ma_vat);
            newItem.dateCreate = getDayTime(0L);
            if (time > 0) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = time;
            }
            this.iItems.add(newItem);
            this.sendMessage(MessageCreator.createCharInventoryMessage(this, 1));
            this.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phi phong lông thú thời hạn " + time + " phút", ""));
            Database.instance.saveOrtherLog("", this.charname, String.valueOf(newItem.getTemplate().name) + "_" + newItem.getDBInfo() + "|" + newItem.getAttribute() + " " + newItem.level, "quanoel");
            this.receiveGiftEvent = 1;
        } catch (final Exception ex) {
        }
    }

    @Override
    public boolean isCharMonster() {
        return this.isMonster;
    }

    public String getNameChiemThanh() {
        return "daihiep";
    }

    @Override
    public String getName() {
        return this.charname;
    }

    public boolean isCharCopy() {
        return false;
    }

    public boolean isCharChienTruong() {
        return false;
    }

    @Override
    public boolean isMyHoVe(final Char p) {
        return false;
    }

    public boolean isThangBe() {
        return false;
    }

    public String getinfoCharCopy() {
        return "";
    }

    public boolean isCoolDown(final int idSkill) {
        return false;
    }

    public void doChoKeo() {
        if (this.potions[83] <= 0) {
            int indexmap;
            int idmap;
            for (indexmap = Map.r.nextInt(Char.idmapthangbe.length), idmap = Char.idmapthangbe[indexmap]; idmap == this.mapID; idmap = Char.idmapthangbe[indexmap]) {
                indexmap = Map.r.nextInt(Char.idmapthangbe.length);
            }
            this.map.move2Map(this, Char.xythangbe[indexmap][0], Char.xythangbe[indexmap][1], idmap, this.inCountry);
            this.charCopyDissapear();
            return;
        }
        final int[] potions = this.potions;
        final int n = 83;
        --potions[n];
        final int[] per = {3000, 3000, 3000, 3000, 3000, 500, 3000, 3000, 3000, 3000, 3000, 500, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
        final int[] idgift = {-2, -2, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, 114, 113, 112, 93, 95, 123, 100, GemTemplate.BOT_XANH, GemTemplate.BOT_DO, GemTemplate.BOT_XANH_LA};
        final int[] typegift = {-1, -1, -1, -1, -1, -1, -2, -2, -2, -2, -2, -2, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6};
        final int[] soluongmax = {10000, 20000, 30000, 40000, 50000, 5000000, 500000, 70000, 80000, 90000, 100000, 10000000, 3, 3, 3, 200, 200, 1, 1, 10, 10, 10};
        final int[] soluongmin = {10000, 20000, 30000, 40000, 50000, 5000000, 500000, 70000, 80000, 90000, 100000, 10000000, 1, 1, 1, 100, 100, 1, 1, 10, 10, 10};
        String info = "Chúc mừng bạn nhận được ";
        final int index = getIndexRandom(per);
        if (typegift[index] == -2) {
            info = String.valueOf(info) + soluongmax[index] + " exp";
            Map.addXpCharEvent(this, soluongmax[index], false, "char doChoKeo");
            this.sendMessage(MessageCreator.createMsgChat(this.id, info));
        }
        Label_1295:
        {
            if (typegift[index] == -1) {
                this.addXu(soluongmax[index], "char 12");
                info = String.valueOf(info) + soluongmax[index] + " xu";
                this.sendMessage(MessageCreator.createMsgChat(this.id, info));
                this.sendMessage(MessageCreator.createMsgChat(this.id, info));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final int[] potions2 = this.potions;
                final int n2 = idgift[index];
                potions2[n2] += soluong;
                info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                this.sendMessage(MessageCreator.createMsgChat(this.id, info));
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }
                final boolean lock = Map.r.nextInt(2) == 1;
                this.doAddGemItem(idgift[index], soluong, lock);
                info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                this.sendMessage(MessageCreator.createCharGemItem(this));
                this.sendMessage(MessageCreator.createMsgChat(this.id, info));
                if (idgift[index] != GemTemplate.LKD_35) {
                    if (idgift[index] != GemTemplate.LKD_40) {
                        break Label_1295;
                    }
                }
                try {
                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(this.charname) + " đã nhận được " + Map.gemTemplate[idgift[index]].name + " đốt đèn"));
                } catch (final IOException ex) {
                }
            }
        }
        Database.instance.saveOrtherLog("", this.charname, info, "chokeo");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        this.charCopyDissapear();
    }

    public void addLienTram(final String name) {
        if (this.nameLienTram.size() >= 15) {
            this.nameLienTram.remove(0);
        }
        this.nameLienTram.add(name);
    }

    public boolean checkLienTram(final String info) {
        for (int i = 0; i < this.nameLienTram.size(); ++i) {
            if (this.nameLienTram.get(i).equals(info)) {
                return true;
            }
        }
        this.addLienTram(info);
        return false;
    }

    public static boolean isAcountTest(final Char p) {
        try {
            return p.getSession().username.toLowerCase().equals("huog25") || p.getSession().username.equals("tmbtest123") || p.getSession().username.equals("tmbtest123@gmail.com") || !p.getSession().is18Tuoi() || ((!p.getSession().is18Tuoi() || p.getSession().userID >= 127597851) && Char.onOffThamdinh == 1);
        } catch (final Exception ex) {
            return false;
        }
    }

    public static void startNauBanh() {
        Char.nauBanhChung = Database.instance.loadAllCharCooking(1);
        Char.nauBanhTet = Database.instance.loadAllCharCooking(2);
        if (Char.nauBanhChung == null) {
            Char.nauBanhChung = new Cooking();
            Char.nauBanhChung.type = 1;
        }
        if (Char.nauBanhTet == null) {
            Char.nauBanhTet = new Cooking();
            Char.nauBanhTet.type = 2;
        }
        if (isSuKienTet2017()) {
            new Thread() {
                @Override
                public void run() {
                    while (!AdminHandler.isStopServer) {
                        try {
                            Char.nauBanhChung.update();
                        } catch (final Exception ex) {
                        }
                        try {
                            Char.nauBanhTet.update();
                        } catch (final Exception ex2) {
                        }
                        try {
                            Thread.sleep(500L);
                        } catch (final Exception ex3) {
                        }
                    }
                }
            }.start();
        }
    }

    // Than method (99 dong) da chuyen sang CharVongLap.java. Chu ky GIU NGUYEN.
    public static void startThreadXoso() {
        CharVongLap.startThreadXoso();
    }

    public static void startThreadXosoLow() {
        new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        if ((System.currentTimeMillis() - Char.timeXoso_low) / 1000L >= 120L) {
                            if (Char.ALL_PLAYER_XOSO_low.size() > 1) {
                                final int[] value = new int[Char.ALL_PLAYER_XOSO_low.size()];
                                final String[] namexoso = new String[Char.ALL_PLAYER_XOSO_low.size()];
                                final Vector<PlayerXoso> allplayer = new Vector<PlayerXoso>();
                                int i = 0;
                                for (final PlayerXoso pxs : Char.ALL_PLAYER_XOSO_low.values()) {
                                    value[i] = pxs.money;
                                    namexoso[i] = pxs.name;
                                    pxs.index = i;
                                    allplayer.add(pxs);
                                    ++i;
                                }
                                int tempindex;
                                int index;
                                Char pwin;
                                int[] value2;
                                PlayerXoso pxs2;
                                for (index = (tempindex = Char.getIndexRandomXoso(value)), pwin = CharManager.instance.getCharByCharName(namexoso[index]); pwin == null && allplayer.size() > 0; pwin = CharManager.instance.getCharByCharName(namexoso[index])) {
                                    allplayer.remove(tempindex);
                                    if (allplayer.size() == 0) {
                                        break;
                                    }
                                    value2 = new int[allplayer.size()];
                                    for (i = 0; i < allplayer.size(); ++i) {
                                        pxs2 = allplayer.get(i);
                                        value2[i] = pxs2.money;
                                    }
                                    tempindex = Char.getIndexRandomXoso(value2);
                                    if (tempindex == -1) {
                                        break;
                                    }
                                    index = allplayer.get(tempindex).index;
                                }
                                int tax = Char.ALL_PLAYER_XOSO_low.size() - 1;
                                if (tax > 10) {
                                    tax = 10;
                                }
                                // Chup lai hu tien va DONG VONG NGAY - truoc ca khi phat thuong.
                                // Truoc day dong vong (ba dong MONEY_XOSO_low=0/timeXoso_low=now/
                                // ALL_PLAYER_XOSO_low.clear()) nam SAU addXu+saveCharAuto: neu
                                // saveCharAuto nem loi (Database.saveCharAuto co doan dau ham
                                // KHONG boc try/catch), catch cua vong lap nuot loi ma khong dong
                                // vong -> 100ms sau dieu kien mo dau lai dung, chon LAI dung
                                // nguoi thang va addXu THEM MOT LAN NUA cho toi khi saveCharAuto
                                // may man thanh cong - xu sinh tu khong khi moi lan nem loi.
                                final long huTien = Char.MONEY_XOSO_low;
                                final int moneyWin = (int) (huTien - huTien * tax / 100L);
                                final long moneytest = value[index];
                                final long tl = moneytest * 100L / huTien;
                                Char.MONEY_XOSO_low = 0L;
                                Char.timeXoso_low = System.currentTimeMillis();
                                Char.ALL_PLAYER_XOSO_low.clear();
                                String info = "Không tìm thấy người chiến thắng";
                                if (pwin != null) {
                                    info = "Chúc mừng " + namexoso[index].toUpperCase() + " đã chiến thắng " + Map.getDotNumber(moneyWin) + " vàng trong trò chơi Vòng xoay may mắn";
                                    if (tl <= 2L) {
                                        info = "Chúc mừng " + namexoso[index].toUpperCase() + " đặt " + Map.getDotNumber(moneytest) + " vàng và chiến thắng " + Map.getDotNumber(moneyWin) + " vàng trong trò chơi Vòng xoay may mắn";
                                    }
                                    Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(info));
                                    Database.instance.resetXosoLow(namexoso[index], value[index], moneyWin, (int) (huTien * tax / 100L));
                                    Char.charNameXosoWin_low = namexoso[index];
                                    Char.moneyCharXosowin_low = moneyWin;
                                    Char.moneyCharXosoBid_low = value[index];
                                    pwin.addXu(moneyWin, "vxmm thuong");
                                    pwin.sendMessage(MessageCreator.createCharInventoryMessage(pwin, 0));
                                    Database.instance.saveCharAuto(pwin);
                                } else {
                                    Database.instance.resetXosoLow("ko char win", value[index], moneyWin, (int) (huTien * tax / 100L));
                                    Char.charNameXosoWin_low = "";
                                    Char.moneyCharXosowin_low = 0;
                                    Char.moneyCharXosoBid_low = 0;
                                }
                            } else {
                                Char.timeXoso_low = System.currentTimeMillis();
                            }
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(100L);
                    } catch (final Exception ex) {
                    }
                }
            }
        }.start();
    }

    public void infoXosoLow() {
        try {
            if (Char.ALL_PLAYER_XOSO_low.size() < 2) {
                Char.timeXoso_low = System.currentTimeMillis();
            }
            final PlayerXoso pxs = Char.ALL_PLAYER_XOSO_low.get(this.charname.toLowerCase());
            long moneytest = 0L;
            if (pxs != null) {
                moneytest = pxs.money;
            }
            final long moneyMax = Char.MONEY_XOSO_low;
            String tl = "";
            String tl2 = "";
            long pc = 0L;
            if (moneyMax > 0L) {
                tl = String.valueOf(tl) + moneytest * 100L / moneyMax;
                pc = moneytest * 100L / moneyMax;
                long mod = moneytest * 100L % moneyMax;
                if (mod != 0L) {
                    tl = String.valueOf(tl) + ".";
                    int i = 0;
                    while (i < 4) {
                        ++i;
                        tl = String.valueOf(tl) + mod * 10L / moneyMax;
                        tl2 = String.valueOf(tl2) + mod * 10L / moneyMax;
                        if (mod * 10L % moneyMax == 0L) {
                            break;
                        }
                        mod = mod * 10L % moneyMax;
                    }
                } else {
                    tl = String.valueOf(tl) + ".0";
                    tl2 = "0";
                }
            } else {
                tl = "0";
                tl2 = "0";
            }
            final long timexs = Char.timeXoso_low;
            long time = 120L - (System.currentTimeMillis() - timexs) / 1000L;
            if (time < 0L) {
                time = 0L;
            }
            String hh = "0" + time / 60L + ":";
            String sc = "";
            if (time / 60L == 0L) {
                sc = new StringBuilder(String.valueOf(time)).toString();
            } else {
                sc = new StringBuilder(String.valueOf(time % 60L)).toString();
            }
            if (sc.length() == 1) {
                sc = "0" + sc;
            }
            hh = String.valueOf(hh) + sc;
            final String str = "Vòng quay thường\nThời gian\n" + hh + "\n" + Map.getDotNumber(Char.MONEY_XOSO_low) + " xu\n" + "Tỷ lệ thắng: " + tl + "%\n" + "Số người tham gia: " + Char.ALL_PLAYER_XOSO_low.size() + "\n" + "Người vừa chiến thắng: " + Char.charNameXosoWin_low + "\n" + "Số xu ăn được: " + Map.getDotNumber(Char.moneyCharXosowin_low) + " xu\n" + "Số xu tham gia: " + Map.getDotNumber(Char.moneyCharXosoBid_low) + " xu";
            this.sendMessage(MessageCreator.createMsgPopUp(this.id, 100, str));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void infoXoso(final int type) {
        if (type == 1) {
            this.infoXosoLow();
            return;
        }
        try {
            if (Char.ALL_PLAYER_XOSO.size() < 2) {
                Char.timeXoso = System.currentTimeMillis();
            }
            final PlayerXoso pxs = Char.ALL_PLAYER_XOSO.get(this.charname.toLowerCase());
            long moneytest = 0L;
            if (pxs != null) {
                moneytest = pxs.money;
            }
            final long moneyMax = Char.MONEY_XOSO;
            String tl = "";
            String tl2 = "";
            long pc = 0L;
            if (moneyMax > 0L) {
                tl = String.valueOf(tl) + moneytest * 100L / moneyMax;
                pc = moneytest * 100L / moneyMax;
                long mod = moneytest * 100L % moneyMax;
                if (mod != 0L) {
                    tl = String.valueOf(tl) + ".";
                    int i = 0;
                    while (i < 4) {
                        ++i;
                        tl = String.valueOf(tl) + mod * 10L / moneyMax;
                        tl2 = String.valueOf(tl2) + mod * 10L / moneyMax;
                        if (mod * 10L % moneyMax == 0L) {
                            break;
                        }
                        mod = mod * 10L % moneyMax;
                    }
                } else {
                    tl = String.valueOf(tl) + ".0";
                    tl2 = "0";
                }
            } else {
                tl = "0";
                tl2 = "0";
            }
            final long timexs = Char.timeXoso;
            long time = 120L - (System.currentTimeMillis() - timexs) / 1000L;
            if (time < 0L) {
                time = 0L;
            }
            String hh = "0" + time / 60L + ":";
            String sc = "";
            if (time / 60L == 0L) {
                sc = new StringBuilder(String.valueOf(time)).toString();
            } else {
                sc = new StringBuilder(String.valueOf(time % 60L)).toString();
            }
            if (sc.length() == 1) {
                sc = "0" + sc;
            }
            hh = String.valueOf(hh) + sc;
            final String str = "Vòng quay vip\nThời gian\n" + hh + "\n" + Map.getDotNumber(Char.MONEY_XOSO) + " xu\n" + "Tỷ lệ thắng: " + tl + "%\n" + "Số người tham gia: " + Char.ALL_PLAYER_XOSO.size() + "\n" + "Người vừa chiến thắng: " + Char.charNameXosoWin + "\n" + "Số xu ăn được: " + Map.getDotNumber(Char.moneyCharXosowin) + " xu\n" + "Số xu tham gia: " + Map.getDotNumber(Char.moneyCharXosoBid) + " xu";
            this.sendMessage(MessageCreator.createMsgPopUp(this.id, 100, str));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<Item> getAllItemPetEat() {
        this.itemPetEat = new Vector<Item>();
        for (int i = 0; i < this.iItems.size(); ++i) {
            final Item item = this.iItems.get(i);
            if (!item.isItemThoiTrang() && !item.isVukhiThoiTrang() && !item.isSelling) {
                if (item.isHuyetLinhThao() && this.petUsing.itemPet != null && this.petUsing.itemPet != null && this.petUsing.itemPet.level > 0) {
                    this.itemPetEat.add(item);
                } else if (item.getTemplate().type < 13 && !item.isItemShop) {
                    this.itemPetEat.add(item);
                }
            }
        }
        if (this.potions[83] > 0) {
            final Item it = new Item((ItemTemplates) Map.itemTemplates.get(5).get(714), false, 0, 0, 714);
            this.itemPetEat.add(it);
        }
        return this.itemPetEat;
    }

    public void doXoso(final int money, final int type, final int hight_low) {
        if (Char.isBaotriVxmm) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
            return;
        }
        if (money < 0) {
            Database.instance.saveOrtherLog("", this.charname, "hack vxmm " + money, "hvxmm");
            return;
        }
        if (hight_low == 1) {
            this.doXosoLow(money, type, hight_low);
            return;
        }
        if (this.xu < money) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
            return;
        }
        if (Char.ALL_PLAYER_XOSO_low.get(this.charname.toLowerCase()) != null) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tham gia 1 vòng trong 1 lượt quay", ""));
            return;
        }
        if (this.xu < 5000000L) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có từ 5.000.000 xu trở lên mới có thể tham gia vòng xuay vip", ""));
            return;
        }
        PlayerXoso pxs = Char.ALL_PLAYER_XOSO.get(this.charname.toLowerCase());
        if (pxs != null) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tham gia 1 lần trong 1 lượt quay", ""));
            return;
        }
        if ((System.currentTimeMillis() - Char.timeXoso) / 1000L >= 110L || Char.MONEY_XOSO >= MAX_POT_XOSO) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Đã khoá đặt cược", ""));
            return;
        }
        if (Char.ALL_PLAYER_XOSO.size() >= MAX_PLAYER_XOSO) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Vòng quay đã đủ " + MAX_PLAYER_XOSO + " người, vui lòng chờ lượt sau", ""));
            return;
        }
        if (money > MAX_BID_XOSO) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Số xu tối đa có thể đặt là " + Map.getDotNumber(MAX_BID_XOSO) + " xu", ""));
            return;
        }
        if (money < MIN_BID_XOSO) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Số xu tối thiểu phải là " + Map.getDotNumber(MIN_BID_XOSO) + " xu", ""));
            return;
        }
        if (this.xu - money < 2500000L) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Số xu tối đa có thể đặt là " + (this.xu - 2500000L) + " xu", ""));
            return;
        }
        Char.MONEY_XOSO += money;
        pxs = new PlayerXoso();
        pxs.name = this.charname.toLowerCase();
        final PlayerXoso playerXoso = pxs;
        playerXoso.money += money;
        Char.ALL_PLAYER_XOSO.put(this.charname.toLowerCase(), pxs);
        this.subXu(money, false, "dat tien vxmm vip doxoso");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        final Char p = this;
        ThreadPool.instant.workList.add(new Runnable() {
            @Override
            public void run() {
                Database.instance.doAddUpdatePlayerXoso(Char.this.charname, money);
                Database.instance.saveCharAuto(p);
            }
        });
        this.infoXoso(hight_low);
        Database.instance.saveOrtherLog("", this.charname, "dat cuoc vong xoay may man vip " + money, "bidxoso");
    }

    public void doXosoLow(final int money, final int type, final int hight_low) {
        if (money < 0) {
            return;
        }
        if (Char.ALL_PLAYER_XOSO.get(this.charname.toLowerCase()) != null) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tham gia 1 vòng trong 1 lượt quay", ""));
            return;
        }
        if (this.xu < money) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
            return;
        }
        PlayerXoso pxs = Char.ALL_PLAYER_XOSO_low.get(this.charname.toLowerCase());
        if (pxs != null) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể tham gia 1 lần trong 1 lượt quay", ""));
            return;
        }
        if ((System.currentTimeMillis() - Char.timeXoso_low) / 1000L >= 110L || Char.MONEY_XOSO_low >= 500000000L) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Đã khoá đặt cược", ""));
            return;
        }
        if (money < 10000) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Số xu tổi thiểu phải là 10.000 xu", ""));
            return;
        }
        if (money > 100000) {
            this.sendMessage(MessageCreator.createServerAlertMessage("Số xu tổi đa có thể đặt là 100.000 xu", ""));
            return;
        }
        Char.MONEY_XOSO_low += money;
        pxs = new PlayerXoso();
        pxs.name = this.charname.toLowerCase();
        final PlayerXoso playerXoso = pxs;
        playerXoso.money += money;
        Char.ALL_PLAYER_XOSO_low.put(this.charname.toLowerCase(), pxs);
        this.subXu(money, false, "dat tien vxmm thg dosoxolow");
        this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
        final Char p = this;
        ThreadPool.instant.workList.add(new Runnable() {
            @Override
            public void run() {
                Database.instance.doAddUpdatePlayerXosoLow(Char.this.charname, money);
                Database.instance.saveCharAuto(p);
            }
        });
        this.infoXoso(hight_low);
        Database.instance.saveOrtherLog("", this.charname, "dat cuoc vong xoay may man thuong " + money, "bidxoso");
    }

    public void countKillMonster(final boolean count) {
        try {
            if (count) {
                ++this.countMonsterKill;
                if (this.countMonsterKill == 10 || this.countMonsterKill == 100 || this.countMonsterKill == 1000 || this.countMonsterKill == 10000 || this.countMonsterKill == 100000 || this.countMonsterKill == 1000000 || this.countMonsterKill == 10000000) {
                    final int[] potions = this.potions;
                    final int n = 77;
                    ++potions[n];
                    this.sendMessage(MessageCreator.createCharInventoryMessage(this, 0));
                    Database.instance.saveOrtherLog("", this.getName(), "nhan dc trung vang khi train quai", "trungvang");
                }
            }
        } catch (final Exception ex) {
        }
    }

    public void doOffDanhHieu() {
        if (this.idEffDanhHieu > -1) {
            this.idEffDanhHieu = -1;
            this.currentDanhHieu = null;
            try {
                this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
            } catch (final IOException ex) {
            }
            this.sendToNearPlayer(MessageCreator.createCharInfo(this));
        }
    }

    public void doSelectDanhHieu(final int index) {
        this.idEffDanhHieu = this.allDanhHieu.get(index).ideff;
        this.currentDanhHieu = this.allDanhHieu.get(index);

//        System.out.println("idEffDanhHieu: " + idEffDanhHieu +", currentDanhHieu: " + currentDanhHieu.getName());
        try {
            this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        this.sendToNearPlayer(MessageCreator.createCharInfo(this));
    }

    public String[] getAllNamDanhHieu() {
        String[] a = null;
        final Vector<DanhHieu> dh = new Vector<DanhHieu>();
        for (int i = 0; i < this.allDanhHieu.size(); ++i) {
            if (!this.allDanhHieu.get(i).isExpire()) {
                dh.add(this.allDanhHieu.get(i));
            }
        }
        this.allDanhHieu = dh;
        a = new String[dh.size() + 1];
        if (dh.size() > 0) {
            for (int i = 0; i < this.allDanhHieu.size(); ++i) {
                a[i] = dh.get(i).getName();
            }
        }
        a[a.length - 1] = "Tắt danh hiệu";
        return a;
    }

    public String getAllDanhHieuSave() {
        String info = "";
        for (int i = 0; i < this.allDanhHieu.size(); ++i) {
            if (!this.allDanhHieu.get(i).isExpire()) {
                info = String.valueOf(info) + this.allDanhHieu.get(i).ideff + "," + this.allDanhHieu.get(i).timeExpire + ",";
            }
        }
        return (info.length() > 0) ? info.substring(0, info.length() - 1) : info;
    }

    public boolean addDanhHieu(final int id, final long minute) {
        for (int i = 0; i < this.allDanhHieu.size(); ++i) {
            if (this.allDanhHieu.get(i).ideff == id) {
                if (minute > 0L) {
                    this.allDanhHieu.get(i).timeExpire = System.currentTimeMillis() + minute * 60000L;
                } else {
                    this.allDanhHieu.get(i).timeExpire = 0L;
                }
                Database.instance.addCharDanhHieu(this);
                return false;
            }
        }
        final DanhHieu dh = new DanhHieu();
        dh.ideff = id;
        if (minute > 0L) {
            dh.timeExpire = System.currentTimeMillis() + minute * 60000L;
        } else {
            dh.timeExpire = 0L;
        }
        this.allDanhHieu.add(dh);
        if (this.idEffDanhHieu == -1) {
            this.idEffDanhHieu = id;
            this.currentDanhHieu = dh;
        }
        Database.instance.addCharDanhHieu(this);
        return true;
    }

    public boolean addDanhHieu_VIP(final int id, final long minute) {
        for (int i = 0; i < this.allDanhHieu.size(); ++i) {
            if (this.allDanhHieu.get(i).ideff == id) {
                return false;
            }
        }
        final DanhHieu dh = new DanhHieu();
        dh.ideff = id;
        if (minute > 0L) {
            dh.timeExpire = System.currentTimeMillis() + minute * 60000L;
        } else {
            dh.timeExpire = 0L;
        }
        this.allDanhHieu.add(dh);
        if (this.idEffDanhHieu == -1) {
            this.idEffDanhHieu = id;
            this.currentDanhHieu = dh;
        }
        Database.instance.addCharDanhHieu(this);
        return true;
    }

    public void setDanhHieu(final String info) {
        try {
            if (info == null || info.equals("")) {
                return;
            }
            final String[] data = split(info, ",");
            for (int i = 0; i < data.length; i += 2) {
                final int ideff = Integer.parseInt(data[i]);
                final long time = Long.parseLong(data[i + 1]);
                if (time == 0L || (time > 0L && time - System.currentTimeMillis() > 10000L)) {
                    final DanhHieu dh = new DanhHieu();
                    dh.ideff = ideff;
                    dh.timeExpire = time;
                    this.allDanhHieu.add(dh);
                    if (this.idEffDanhHieu == ideff) {
                        this.currentDanhHieu = dh;
                    }

                }
            }
        } catch (final Exception ex) {
        }
    }

    public int[][] getInfoDanhHieu() {
        final int[][] a = {{this.idEffDanhHieu}, {2}};
        return a;
    }

    public void doAddGiftThungGo() {
        try {
            if (this.isFullInventory()) {
                return;
            }
            final int[] per = {5000, 5000, 5000, 200, 200, 200, 500, 5000, 5000, 2000, 300, 10, 4000, 1000};
            final int[] idgift = {94, 96, 69, GemTemplate.NGOC_HUYEN_MINH_3, GemTemplate.NGOC_HUYEN_MINH_4, GemTemplate.NGOC_HUYEN_MINH_5, GemTemplate.DA_TACH_NL_DAC_BIET, -1, -1, -1, -1, -1, -2, -3};
            final int[] typegift = {4, 4, 4, 6, 6, 6, 6, -1, -1, -1, -1, -1, -2, -3};
            final int[] soluongmax = {100, 100, 1, 5, 5, 5, 1, 10000, 15000, 20000, 50000, 200000, 1, 1};
            final int[] soluongmin = {50, 50, 1, 3, 3, 3, 1, 10000, 15000, 20000, 50000, 200000, 1, 1};
            final Char player = this;
            String info = "Chúc mừng bạn nhận được ";
            final int index = getIndexRandom(per);
            Label_1161:
            {
                if (typegift[index] == -1) {
                    player.addXu(soluongmax[index], "map 57");
                    info = String.valueOf(info) + soluongmax[index] + " xu";
                    player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (typegift[index] == -2) {
                    final int[] minute = {180, 300, 420, 1440};
                    final Item newItem = ItemLuckyDraw.createItemCoat(player, Map.r.nextInt(2), Map.r.nextInt(100) < 50, minute[Map.r.nextInt(minute.length)]);
                    player.iItems.add(newItem);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    info = String.valueOf(info) + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                    player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (typegift[index] == -3) {
                    final int[] idchoi = {617, 619, 618};
                    final Item it = createChoi(idchoi[Map.r.nextInt(idchoi.length)], this, 1440, 0, true);
                    info = String.valueOf(info) + " " + it.getName();
                } else if (typegift[index] == 4) {
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }
                    final int[] potions = player.potions;
                    final int n = idgift[index];
                    potions[n] += soluong;
                    info = String.valueOf(info) + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                    player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (typegift[index] == 6) {
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += Map.r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }
                    final boolean lock = Map.r.nextInt(2) == 1;
                    player.doAddGemItem(idgift[index], soluong, lock);
                    info = String.valueOf(info) + soluong + " " + Map.gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    if (idgift[index] != GemTemplate.LKD_35) {
                        if (idgift[index] != GemTemplate.LKD_40) {
                            break Label_1161;
                        }
                    }
                    try {
                        Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(String.valueOf(player.charname) + " đã nhận được " + Map.gemTemplate[idgift[index]].name + " khi làm nhiệm vụ tìm trẻ lạc"));
                    } catch (final IOException ex) {
                    }
                }
            }
            Database.instance.saveOrtherLog("", player.charname, info, "thunggo");
        } catch (final Exception ex2) {
        }
    }

    public String getInfoServer() {
        String a = "";
        a = "@";
        a = "Thông tin chiến trường:@";
        a = String.valueOf(a) + "Sát thương trụ: ";
        a = String.valueOf(a) + "Giáp trụ: 20@";
        a = String.valueOf(a) + "Giáp quái: 10@";
        return a;
    }

    public void doAddPointChienTruong(final int point) {
        final CharChienTruong c = MapChienTruongMoba.getCharChienTruong(this.charname);
        if (c != null) {
            if (c.noi_luc >= 10) {
                return;
            }
            final int oldNoiLuc = c.noi_luc;
            final CharChienTruong charChienTruong = c;
            charChienTruong.pointKillMons += point;
            if (c.pointKillMons >= Char.ID_POINT_NOI_LUC_CHIEN_TRUONG[c.noi_luc]) {
                final CharChienTruong charChienTruong2 = c;
                ++charChienTruong2.noi_luc;
                if (c.noi_luc == 5 || c.noi_luc == 7 || c.noi_luc == 10) {
                    this.calculateAttrib();
                }
            }
            if (oldNoiLuc < c.noi_luc) {
                try {
                    final Message m = new Message(33);
                    m.dos.writeShort(this.id);
                    m.dos.writeByte(c.noi_luc);
                    m.dos.writeInt(this.maxhp);
                    m.dos.writeInt(this.maxmp);
                    this.hp = this.maxhp;
                    this.mp = this.maxmp;
                    this.calculateAttrib();
                    this.sendMessage(m);
                    this.sendToNearPlayer(m);
                    this.sendMessage(MessageCreator.createMainCharInfoMessage(this));
                    this.sendInfoChienTruong(Char.ID_NOI_LUC, 0);
                } catch (final Exception ex) {
                }
            }
        }
    }

    public boolean isMasterClanAndHaveTown() {
        if (this.idClan == -1) {
            return false;
        }
        
        NewClan clanInfo = Map.getClaninfoByID(this.idClan);
        if (clanInfo == null) {
            return false;
        }
    
        // Check if character is clan master and town > 0
        return clanInfo.master.toLowerCase().equals(this.charname.toLowerCase()) && clanInfo.town > 0;
    }

    public long getTimeNuiChauBau() {
        final CharInfo c = MapChauBau.all_char_nui_kho_bau.get(this.charname);
        if (c != null) {
            return c.getTimeNuiChauBau();
        }
        return 0L;
    }

    public boolean isVip() {
        return this.vip > 0;
    }

    @Override
    public boolean canFocus(final Char me) {
        return this.idBot != -70 && (!this.map.isMapLoiDai() || (me.name_char_loi_dai != null && this.name_char_loi_dai != null)) && super.canFocus(me);
    }

    public void doAttackSkillNew(final Vector<Monster> ms) {
      
        try {
                for(int i = 0; i < ms.size(); ++i) {
                    Monster mt = (Monster)ms.get(i);
                    final Message msg = MessageCreator.createMsgNewEffectSkill5Long(8999, 450, mt.id, mt.cat, true, false);
                    this.sendMessage(msg);
                    this.sendToNearPlayer(msg);
                }
          
        } catch (Exception var10) {
        }

    }

    private boolean isRongLon() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    
    
}
