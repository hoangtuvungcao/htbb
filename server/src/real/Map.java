package real;


import data.Animal;
import data.BossMatQuy;
import data.CharClan;
import data.CharInboxMessage;
import data.CharInfo;
import data.CharWeaponData;
import data.Cooking;
import data.CreateItemTemplate;
import data.Database;
import data.EventGame;
import data.GemItem;
import data.InfoItemCreate;
import data.InfoQuestTemplate;
import data.ItemChangeColor;
import data.ItemInfo;
import data.ItemLuckyDraw;
import data.ItemQuest;
import data.MapLocation;
import data.Net;
import data.NewClan;
import data.OtherHandle;
import data.Pet;
import data.PetTemplate;
import data.SeedItem;
import data.SeedTemplate;
import data.Text;
import data.ThungGo;
import data.Weather;
import data.Xoso;
import io.Message;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import real.cmd.LoginHandler;
import real.cmd.RequestNPCInfoHandler;
import server.TeamServer;
import ThanKhi.ThoRenThanBi;


public class Map implements Runnable {

    public static final byte SAND = 0;
    public static final byte LEAF = 1;
    public static final byte WIND = 2;
    public static byte MAX_MEMBER_ARENA = 10;
    public boolean isMapOffline = false;
    public int mapId;
    public int mapIDLoadMap;
    public static int mapIDFarm = 29;
    public static int idMapMateria = 17;
    public static boolean[] getTown = new boolean[3];
    public static boolean[] nwar = new boolean[3];
    public static byte[] x2Country = new byte[]{3, 3, 3};
    public static long[] minuteX2Country = new long[3];
    public static int idMapDautruong = 202;
    public static int idMapChanNui = 41;
    public static int idMapChoLoiDai = 228;
    public static int idMapLoiDai = 227;
    public static EventGame event = new EventGame();
    public static Vector<Vector<Integer>> idMapMONSTER = new Vector();
    public static int idMapTown = 201;
    public static short[] idClanTown = new short[]{-1, -1, -1};
    public static byte[] nationBeAttack = new byte[]{-1, -1, -1};
    public static byte[] taxOfClan = new byte[3];
    public static byte MAX_FARM = 24;
    public Vector<Vector<Char>> allPlayers = new Vector();
    protected Vector<Hashtable<Short, Monster>> monsters = new Vector();
    protected Vector<Hashtable<Short, Monster>> temp_monsters = new Vector();
    public Vector<Vector<Potion>> potions = new Vector();
    public Vector<Vector<ItemQuest>> itemquests = new Vector();
    public Vector<Vector<Item>> items = new Vector();
    public static Vector<Hashtable<Integer, CharThiDau>> ALL_CHAR_LOI_DAI = new Vector();
    public static Hashtable<Integer, CharThiDau> H_ALL_CHAR_LOI_DAI = new Hashtable();
    public static Hashtable<Short, InfoQuestTemplate> allMainQuest = new Hashtable();
    public static Hashtable<Short, InfoQuestTemplate> allSubQuest = new Hashtable();
    public static Hashtable<Short, InfoQuestTemplate> allClanQuest = new Hashtable();
    public static Hashtable<Short, InfoQuestTemplate> allEventQuest = new Hashtable();
    public static Vector<Monster> all_ngoc_rong_thanh_long = new Vector();
    public static Vector<Monster> all_ngoc_rong_hac_ho = new Vector();
    public Vector<Region> ALL_REGION = new Vector();
    public Vector<Vector<GemItem>> gemItem = new Vector();
    public Vector<Vector<EffectBuff>> EFFECT_AUTO = new Vector();
    public static Hashtable<Integer, Vector<LocationMap>> ALL_LOCALTION_MAP = new Hashtable();
    public short ID_ITEM_MAP = -32000;
    public static long[] RECEIVE_TIEU = new long[6];
    public static long[] CHANGE_TIEU = new long[6];
    public static long[] CARD_TIEU = new long[2];
    public static long[] FINISH_TIEU = new long[6];
    public static long[] THIEFT_TIEU = new long[6];
    public static String[] NAME_TIEU = new String[]{"Trắng", "Xanh", "Đỏ", "Hoàn mỹ", "Vàng", "Tím"};
    public static byte x3ExpDotPhao = 0;
    public static int totalDotPhao = 0;
    public static byte[][] SHIELD = new byte[][]{{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
    public static Vector<Vector<String>> listKillPerson = new Vector();
    public Vector<ShopTemplate> spItem = new Vector();
    public static Vector<Vector<NpcReceiveCard>> npcReceiveCard = new Vector();
    public static boolean onSMS = true;
    public static boolean openLog = true;
    public static boolean checkRcvXP = false;
    public static boolean isNewVersion = true;
    public static boolean isSale = false;
    public static byte[][] idSkill = new byte[][]{{6, 7, 8}, {6, 7, 8}, {8, 9, 10}, {6, 7, 8}, {6, 7, 8}};
    public static Vector<BossLocaltion> bossLocation = new Vector();
    public static int[][] priceLearnSkill = new int[][]{
        {60000, 120000, 300000}, {60000, 120000, 300000}, {60000, 120000, 300000}, {60000, 120000, 300000}, {60000, 120000, 300000}
    };
    private static final boolean ENABLE_WATERING = false;
    public static int MAX_DAM = 0;
    public static int MAX_DEF_V = 0;
    public static int MAX_DEF_M = 0;
    public static int MAX_CRIT = 0;
    public static Vector<Integer> idIconClan = new Vector();
    public static Hashtable<Integer, MonsterTemplate> monsterTemplates = new Hashtable();
    public static Vector<PotionTemplate2> potionTemplates2 = new Vector<>();
    public static Hashtable<Integer, Wedding> allWedding = new Hashtable();
    int mapWidth;
    int mapHeight;
    public static byte doubleALL = 1;
    public int[] typeOfTile;
    public int[] type;
    public int w;
    public int h;
    public short[] map;
    Vector<MapLocation> mapLocation = new Vector();
   
    public static byte SELL = 1;
    public static byte NOT_SELL = 0;
    public static byte SELL_XU = 1;
    public static byte SELL_LUONG = 0;
    static int gg1 = 1;
    static int gg2 = 1;
    public static ShopTemplate[] shop;
    public static SeedTemplate[] seedsTemplate = new SeedTemplate[]{
        new SeedTemplate(0, new byte[]{2, 2, 2, 2, 2}, "Cà chua", new short[]{1, 2, 3, 4, 5}, 4, 10, 10, new byte[]{5, 6, 7, 8, 9}, 29, 14, 1, 0, 0, 1),
        new SeedTemplate(1, new byte[]{2, 2, 2, 2, 2}, "Cà rốt", new short[]{1, 2, 3, 4, 5}, 4, 10, 10, new byte[]{20, 21, 22, 23, 24}, 31, 14, 1, 0, 0, 1),
        new SeedTemplate(2, new byte[]{2, 2, 2, 2, 2}, "Hoa hồng", new short[]{1, 2, 3, 4, 5}, 4, 10, 10, new byte[]{10, 11, 12, 13, 14}, 33, 14, 1, 0, 0, 1),
        new SeedTemplate(3, new byte[]{8, 8, 8, 8, 8}, "Thơm", new short[]{1, 2, 3, 4, 5}, 4, 10, 50, new byte[]{25, 26, 27, 28, 29}, 29, 16, 3, 0, 0, 1),
        new SeedTemplate(4, new byte[]{2, 2, 2, 2, 2}, "Nho", new short[]{1, 2, 3, 4, 5}, 4, 10, 50, new byte[]{0, 1, 2, 3, 4}, 31, 16, 3, 0, 0, 1),
        new SeedTemplate(
        5, new byte[]{0, 1, 1, 1, 1}, "Hoa hướng dương", new short[]{1, 2, 3, 4, 5}, 4, 10, 50, new byte[]{45, 46, 47, 48, 49}, 33, 16, 3, 0, 0, 1
        ),
        new SeedTemplate(6, new byte[]{8, 8, 8, 8, 8}, "Dưa hấu", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{30, 31, 32, 33, 34}, 36, 14, 5, 0, 0, 1),
        new SeedTemplate(7, new byte[]{2, 2, 2, 2, 2}, "Thanh long", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{40, 41, 42, 43, 44}, 38, 14, 5, 0, 0, 1),
        new SeedTemplate(8, new byte[]{12, 12, 12, 12, 12}, "Lúa", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{35, 36, 37, 38, 39}, 40, 14, 5, 0, 0, 1),
        new SeedTemplate(
        9, new byte[]{2, 2, 2, 2, 2}, "Hoa tulip", new short[]{1, 2, 3, 4, 5}, 4, 10, 500000, new byte[]{50, 51, 52, 53, 54}, 36, 16, 1, 20, 0, 1
        ),
        new SeedTemplate(10, new byte[]{2, 2, 2, 2, 2}, "Cây ớt", new short[]{1, 2, 3, 4, 5}, 4, 10, 1000000, new byte[]{55, 56, 57, 58, 59}, 38, 16, 2, 30, 0, 1),
        new SeedTemplate(11, new byte[]{2, 2, 2, 2, 2}, "Dâu", new short[]{1, 2, 3, 4, 5}, 4, 10, 5000000, new byte[]{60, 61, 62, 63, 64}, 40, 16, 3, 40, 0, 1),
        new SeedTemplate(12, new byte[]{2, 2, 2, 2, 2}, "Xoài", new short[]{1, 2, 3, 4, 5}, 4, 10, 10000000, new byte[]{15, 16, 17, 18, 19}, 29, 19, 4, 50, 0, 1),
        new SeedTemplate(13, new byte[]{2, 2, 2, 2, 2}, "Chuối", new short[]{1, 2, 3, 4, 5}, 4, 10, 20000000, new byte[]{65, 66, 67, 68, 69}, 31, 19, 5, 60, 0, 1),
        new SeedTemplate(14, new byte[]{2, 2, 2, 2, 2}, "Cam", new short[]{1, 2, 3, 4, 5}, 4, 10, 30000000, new byte[]{70, 71, 72, 73, 74}, 33, 19, 6, 70, 0, 1),
        new SeedTemplate(15, new byte[]{2, 2, 2, 2, 2}, "Bắp", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{75, 76, 77, 78, 79}, 29, 21, 3, 0, 1, 1),
        new SeedTemplate(16, new byte[]{8, 8, 8, 8, 8}, "Bí ngô", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{80, 81, 82, 83, 84}, 31, 21, 4, 0, 1, 1),
        new SeedTemplate(17, new byte[]{2, 2, 2, 2, 2}, "Tulip vàng", new short[]{1, 2, 3, 4, 5}, 4, 10, 100, new byte[]{85, 86, 87, 88, 89}, 33, 21, 5, 0, 1, 1),
        new SeedTemplate(18, new byte[]{4, 4, 4, 4, 4}, "Táo", new short[]{1, 2, 3, 4, 5}, 4, 10, 2, new byte[]{90, 91, 92, 93, 94}, 36, 19, 6, 0, 1, 1),
        new SeedTemplate(19, new byte[5], "Hạt giống 20", new short[]{1, 2, 3, 4}, 4, 10, 4, new byte[]{8, 9, 10, 11, 11}, 38, 19, 6, 0, 1, 0),
        new SeedTemplate(20, new byte[5], "Hạt giống 21", new short[]{1, 2, 3, 4}, 4, 10, 7, new byte[]{8, 9, 10, 11, 11}, 40, 19, 6, 0, 1, 0),
        new SeedTemplate(21, new byte[5], "Hạt giống 22", new short[]{1, 2, 3, 4}, 4, 10, 10, new byte[]{8, 9, 10, 11, 11}, 36, 21, 6, 0, 1, 0),
        new SeedTemplate(22, new byte[5], "Hạt giống 23", new short[]{1, 2, 3, 4}, 4, 10, 100, new byte[]{8, 9, 10, 11, 11}, 38, 21, 6, 0, 1, 0),
        new SeedTemplate(23, new byte[5], "Hạt giống 24", new short[]{1, 2, 3, 4}, 4, 10, 100, new byte[]{8, 9, 10, 11, 11}, 40, 21, 6, 0, 1, 0)
    };
    public static GemTemplate[] gemTemplate;
    public static PetTemplate[] petTemplate = null;
    public static int[][] ID_GEM_TU_BINH = new int[][]{
        {159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174},
        {179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194},
        {195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210},
        {211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226}
    };
    public static SellerTemplate[][] npcSell = new SellerTemplate[][]{
        {
            new SellerTemplate(0, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(1, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(2, new byte[]{9, 8, 12, 26, 28, -1}, 2),
            new SellerTemplate(3, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(4, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(5, new byte[]{9, 8, 12, 23, 24, -1}, 2)
        },
        {
            new SellerTemplate(0, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(1, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(2, new byte[]{9, 8, 12, 26, 28, -1}, 2),
            new SellerTemplate(3, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(4, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(5, new byte[]{9, 8, 12, 23, 24, -1}, 2)
        },
        {
            new SellerTemplate(0, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(1, new byte[]{0, 11, 1, 2, 10, 14, 16, 15, 17, 18, 19}, 0),
            new SellerTemplate(2, new byte[]{9, 8, 12, 26, 28, -1}, 2),
            new SellerTemplate(3, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(4, new byte[]{3, 4, 5, 6, 7, 13, 21}, 1),
            new SellerTemplate(5, new byte[]{9, 8, 12, 23, 24, -1}, 2)
        }
    };
    public static Weather weather = null;
    public static Vector<Hashtable<Integer, ItemTemplates>> itemTemplates = new Vector();
    public static Vector<Collection<ItemTemplates>> itemTemplateCollection = new Vector();
    public static String[] nameCountry = new String[]{"Thanh long", "Hắc hổ", "Huyền vũ"};
    public static Vector<ClanInfo> allClan = new Vector();
    public static Hashtable<Short, Short> locationId = new Hashtable();
    public static String[][][] nameMap = new String[][][]{
        {
            {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}
        },
        {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}, {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"}},
        {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}, {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"}},
        {
            {"Tiên du", "Đông Tiên du", "Tây Tiên du", "Nam Tiên du", "Bắc Tiên du"},
            {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"},
            {"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"}
        },
        {
            {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"},
            {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"}
        },
        {
            {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"},
            {"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"}
        },
        {{"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"}, {"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"}},
        {
            {"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"},
            {"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"}
        },
        {
            {"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"}
        },
        {{"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"}, {"Thành Trấn danh", "Biên giới", "", "", ""}},
        {
            {"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
            {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"}
        },
        {
            {"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
            {"Hầm tối", "Đông Hầm tối", "Tây Hầm tối", "Nam Hầm tối", "Bắc Hầm tối"},
            {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"}
        },
        {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},
        {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},
        {{"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"}, {"Hang thằn lằn", "Hang Đại Bàng", "", "", ""}, {"Hang Dracula", "", "", "", ""}},
        new String[0][],
        {{"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"}},
        {{"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"}},
        {{"Sơn Nam", "Đông Sơn Nam", "", "", ""}},
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}
        },
        {
            {"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
            {"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"},
            {"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"}
        },
        {
            {"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"},
            {"Rừng già", "", "", "", ""}
        },
        {{"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"}},
        {
            {"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}
        },
        {
            {"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"},
            {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
            {"Siêu loại", "Đông Siêu loại", "Tây Siêu loại", "Nam Siêu loại", "Bắc Siêu loại"}
        },
        {
            {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"},
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"}
        },
        {
            {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
            {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
            {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"}
        },
        new String[0][],
        new String[0][],
        new String[0][],
        new String[0][],
        {{"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"}, {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"}},
        new String[0][],
        {
            {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}
        },
        {
            {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
            {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
            {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
            {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"}
        },
        {{"Biên giới", "", "", "", ""}, {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"}},
        {{"Biên giới", "", "", "", ""}, {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"}},
        {{"Thành Trấn Danh", "", "", "", ""}},
        {{"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"}},
        new String[0][],
        new String[0][],
        new String[0][],
        new String[0][],
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
            {"", "", "", "", ""}
        },
        {{"", "", "", "", ""}, {"", "", "", "", ""}, {"", "", "", "", ""}},
        {{"Về làng", "", "", "", ""}, {"Về làng", "", "", "", ""}, {"Về làng", "", "", "", ""}},
        {{"", "", "", "", ""}, {"", "", "", "", ""}, {"", "", "", "", ""}},
        {{"", "", "", "", ""}, {"", "", "", "", ""}, {"", "", "", "", ""}},
        {{"", "", "", "", ""}, {"", "", "", "", ""}, {"", "", "", "", ""}}
    };
    public static short[][][] locationMap = new short[][][]{
        {
            {1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}
        },
        {{0, 17, 2, 301, 17, 2, 302, 17, 2, 303, 17, 2, 304, 17, 2}, {4, 1, 12, 381, 1, 12, 382, 1, 12, 383, 1, 12, 384, 1, 12}},
        {{0, 23, 58, 301, 23, 58, 302, 23, 58, 303, 23, 58, 304, 23, 58}, {3, 1, 22, 361, 1, 22, 362, 1, 22, 363, 1, 22, 364, 1, 22}},
        {
            {2, 126, 21, 341, 126, 21, 342, 126, 21, 343, 126, 21, 344, 126, 21},
            {4, 93, 98, 381, 93, 98, 382, 93, 98, 383, 93, 98, 384, 93, 98},
            {5, 71, 1, 401, 71, 1, 402, 71, 1, 403, 71, 1, 404, 71, 1}
        },
        {
            {1, 78, 11, 321, 78, 11, 322, 78, 11, 323, 78, 11, 324, 78, 11},
            {110, 18, 79, 2501, 18, 79, 2502, 18, 79, 2503, 18, 79, 2504, 18, 79},
            {3, 91, 3, 361, 91, 3, 362, 91, 3, 363, 91, 3, 364, 91, 3}
        },
        {{3, 91, 97, 361, 91, 97, 362, 91, 97, 363, 91, 97, 364, 91, 97}, {6, 40, 2, 421, 40, 2, 422, 40, 2, 423, 40, 2, 424, 40, 2}},
        {{5, 65, 77, 401, 65, 77, 402, 65, 77, 403, 65, 77, 404, 65, 77}, {7, 2, 91, 441, 2, 91, 442, 2, 91, 443, 2, 91, 444, 2, 91}},
        {{6, 45, 44, 421, 45, 44, 422, 45, 44, 423, 45, 44, 424, 45, 44}, {8, 27, 77, 461, 27, 77, 462, 27, 77, 463, 27, 77, 464, 27, 77}},
        {{7, 32, 2, 441, 32, 2, 442, 32, 2, 443, 32, 2, 444, 32, 2}, {9, 28, 98, 481, 28, 98, 482, 28, 98, 483, 28, 98, 484, 28, 98}},
        {{8, 48, 2, 461, 48, 2, 462, 48, 2, 463, 48, 2, 464, 48, 2}, {201, 39, 90, 118, 10, 20, 30, 39, 90, 463, 39, 77, 464, 39, 77}},
        {
            {11, 77, 67, 521, 77, 67, 522, 77, 67, 523, 77, 67, 524, 77, 67},
            {13, 5, 51, 561, 5, 51, 562, 5, 51, 563, 5, 51, 564, 5, 51},
            {14, 77, 29, 581, 77, 29, 582, 77, 29, 583, 77, 29, 584, 77, 29}
        },
        {
            {10, 97, 2, 501, 97, 2, 502, 97, 2, 503, 97, 2, 504, 97, 2},
            {15, 4, 22, 601, 4, 22, 602, 4, 22, 603, 4, 22, 604, 4, 22},
            {113, 97, 4, 2561, 97, 4, 2562, 97, 4, 2563, 97, 4, 2564, 97, 4},
            {114, 74, 1, 2581, 74, 1, 2582, 74, 1, 2583, 74, 1, 2584, 74, 1}
        },
        {{0, 2, 32, 301, 2, 32, 302, 2, 32, 303, 2, 32, 304, 2, 32}},
        {{0, 58, 30, 301, 58, 30, 302, 58, 30, 303, 58, 30, 304, 58, 30}},
        {
            {4, 49, 5, 381, 49, 5, 382, 49, 5, 383, 49, 5, 384, 49, 5},
            {112, 52, 32, 2541, 52, 32, 2542, 52, 32, 2543, 52, 32, 2544, 52, 32},
            {111, 24, 28, 2521, 24, 28, 2522, 24, 28, 2523, 24, 28, 2524, 24, 28}
        },
        new short[0][],
        {{110, 1, 9, 2501, 1, 9, 2502, 1, 9, 22303, 1, 9, 2504, 1, 9}},
        {{110, 77, 1, 2501, 77, 1, 2502, 77, 1, 2503, 77, 1, 2504, 77, 1}},
        {{0, 24, 39, 301, 24, 39, 302, 24, 39, 303, 24, 39, 304, 24, 39}},
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {{10, 127, 43, 501, 127, 43, 502, 127, 43, 503, 127, 43, 504, 127, 43}, {20, 25, 1, 701, 25, 1, 702, 25, 1, 703, 25, 1, 704, 25, 1}},
        {
            {10, 1, 52, 501, 1, 52, 502, 1, 52, 503, 1, 52, 504, 1, 52},
            {23, 54, 2, 761, 54, 2, 762, 54, 2, 763, 54, 2, 764, 54, 2},
            {18, 34, 58, 661, 34, 58, 662, 34, 58, 663, 34, 58, 664, 34, 58}
        },
        {
            {11, 31, 4, 521, 31, 4, 522, 31, 4, 523, 31, 4, 524, 31, 4},
            {19, 1, 22, 681, 1, 22, 682, 1, 22, 683, 1, 22, 684, 1, 22},
            {24, 28, 3, 781, 28, 3, 782, 28, 3, 783, 28, 3, 784, 28, 3},
            {26, 22, 55, 821, 22, 55, 822, 22, 55, 823, 22, 55, 824, 22, 55}
        },
        {{11, 80, 2, 521, 80, 2, 522, 80, 2, 523, 80, 2, 524, 80, 2}},
        {{11, 148, 9, 521, 148, 9, 522, 148, 9, 523, 148, 9, 524, 148, 9}, {20, 69, 43, 701, 69, 43, 702, 69, 43, 703, 69, 43, 704, 69, 43}},
        {
            {18, 1, 33, 661, 1, 33, 662, 1, 33, 663, 1, 33, 664, 1, 33},
            {15, 98, 78, 601, 98, 78, 602, 98, 78, 603, 98, 78, 604, 98, 78},
            {20, 1, 47, 701, 1, 47, 702, 1, 47, 703, 1, 47, 704, 1, 47}
        },
        {{14, 36, 1, 581, 36, 1, 582, 36, 1, 583, 36, 1, 584, 36, 1}, {19, 43, 58, 681, 43, 58, 682, 43, 58, 683, 43, 58, 684, 43, 58}},
        {
            {19, 75, 27, 681, 75, 27, 682, 75, 27, 683, 75, 27, 684, 75, 27},
            {13, 37, 98, 561, 37, 98, 562, 37, 98, 563, 37, 98, 564, 37, 98},
            {114, 1, 76, 2581, 1, 76, 2582, 1, 76, 2583, 1, 76, 2584, 1, 76}
        },
        new short[0][],
        new short[0][],
        new short[0][],
        new short[0][],
        {{23, 57, 66, 761, 57, 66, 762, 57, 66, 763, 57, 66, 764, 57, 66}, {24, 9, 67, 781, 9, 67, 782, 9, 67, 783, 9, 67, 784, 9, 67}},
        new short[0][],
        {
            {1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}
        },
        {
            {1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
            {106, 1, 9, 2421, 1, 9, 2422, 1, 9, 2423, 1, 9, 2424, 1, 9},
            {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
            {12, 46, 13, 541, 46, 13, 542, 46, 13, 543, 46, 13, 544, 46, 13}
        },
        {{118, 2, 20, 2661, 2, 20, 2662, 2, 20, 2663, 2, 20, 2664, 2, 20}, {14, 71, 77, 581, 71, 77, 582, 71, 77, 583, 71, 77, 584, 71, 77}},
        {{118, 68, 2, 2661, 68, 2, 2662, 68, 2, 2663, 68, 2, 2664, 68, 2}, {15, 48, 97, 601, 48, 97, 602, 48, 97, 603, 48, 97, 604, 48, 97}},
        {{201, 54, 10, 201, 54, 10, 201, 54, 10, 201, 54, 10, 201, 54, 10}},
        {{15, 65, 4, 601, 65, 4, 602, 65, 4, 603, 65, 4, 604, 65, 4}},
        new short[0][],
        new short[0][],
        new short[0][],
        new short[0][],
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        {
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {9, 33, 1, 481, 33, 1, 482, 33, 1, 483, 33, 1, 484, 33, 1},
            {206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47, 206, 5, 47}
        },
        new short[0][],
        {{0, 33, 1, 0, 33, 1, 0, 33, 1, 0, 33, 1, 0, 33, 1}},
        new short[0][],
        new short[0][],
        new short[0][]
    };
    public static short[][] localXaphu = new short[][]{
        {0, 24, 39, 301, 24, 39, 302, 24, 39, 303, 24, 39, 304, 24, 39},
        {1, 1, 61, 321, 1, 61, 322, 1, 61, 323, 1, 61, 324, 1, 61},
        {2, 15, 1, 341, 15, 1, 342, 15, 1, 343, 15, 1, 344, 15, 1},
        {3, 5, 20, 361, 5, 20, 362, 5, 20, 363, 5, 20, 364, 5, 20},
        {4, 27, 15, 381, 27, 15, 382, 27, 15, 383, 27, 15, 384, 27, 15},
        {5, 58, 12, 401, 58, 12, 402, 58, 12, 403, 58, 12, 404, 58, 12},
        {6, 37, 9, 421, 37, 9, 422, 37, 9, 423, 37, 9, 424, 37, 9},
        {7, 9, 86, 441, 9, 86, 442, 9, 86, 443, 9, 86, 444, 9, 86},
        {8, 25, 73, 461, 25, 73, 462, 25, 73, 463, 25, 73, 464, 25, 73},
        {9, 17, 91, 481, 17, 91, 482, 17, 91, 483, 17, 91, 484, 17, 91},
        {10, 0, 0, 501, 0, 0, 502, 0, 0, 503, 0, 0, 504, 0, 0},
        {11, 0, 0, 521, 0, 0, 522, 0, 0, 523, 0, 0, 524, 0, 0},
        {12, 0, 0, 541, 0, 0, 542, 0, 0, 543, 0, 0, 544, 0, 0},
        {106, 0, 0, 2421, 0, 0, 2422, 0, 0, 2423, 0, 0, 2424, 0, 0},
        {110, 0, 0, 2501, 0, 0, 2502, 0, 0, 2503, 0, 0, 2504, 0, 0},
        {107, 0, 0, 2441, 0, 0, 2442, 0, 0, 2443, 0, 0, 2444, 0, 0},
        {111, 0, 0, 2521, 0, 0, 2522, 0, 0, 2523, 0, 0, 2524, 0, 0},
        {112, 0, 0, 2541, 0, 0, 2542, 0, 0, 2543, 0, 0, 2544, 0, 0},
        {202, 0, 0, 4341, 0, 0, 4342, 0, 0, 4343, 0, 0, 4344, 0, 0},
        {201, 0, 0, 4321, 0, 0, 4322, 0, 0, 4323, 0, 0, 4324, 0, 0}
    };
    public static String[][] namMap = new String[][]{
        {"Sơn Nam", "Đông Sơn Nam", "Tây Sơn Nam", "Nam Sơn Nam", "Bắc Sơn Nam"},
        {"Dao Châu", "Đông Dao Châu", "Tây Dao Châu", "Nam Dao Châu", "Bắc Dao Châu"},
        {"Tiên du", "Đông Tiên Du", "Tây Tiên Du", "Nam Tiên Du", "Bắc Tiên Du"},
        {"Phù liệt", "Đông Phù liệt", "Tây Phù liệt", "Nam Phù liệt", "Bắc Phù liệt"},
        {"Kỳ bố", "Đông Kỳ bố", "Tây Kỳ bố", "Nam Kỳ bố", "Bắc Kỳ bố"},
        {"Hàm tử", "Đông Hàm tử", "Tây Hàm tử", "Nam Hàm tử", "Bắc Hàm tử"},
        {"Thạch giang", "Đông Thạch giang", "Tây Thạch giang", "Nam Thạch giang", "Bắc Thạch giang"},
        {"Đông sơn", "Đông Đông sơn", "Tây Đông sơn", "Nam Đông sơn", "Bắc Đông sơn"},
        {"Tử quan", "Đông Tử quan", "Tây Tử quan", "Nam Tử quan", "Bắc Tử quan"},
        {"Trường giang", "Đông Trường giang", "Tây Trường giang", "Nam Trường giang", "Bắc Trường giang"},
        {"Lộc trĩ", "Đông Lộc trĩ", "Tây Lộc trĩ", "Nam Lộc trĩ", "Bắc Lộc trĩ"},
        {"Sơn lâm", "Đông Sơn lâm", "Tây Sơn lâm", "Nam Sơn lâm", "Bắc Sơn lâm"},
        {"Khu mua bán", "Khu đông", "Khu tây", "Khu nam", "Khu bắc"},
        {"Bến tàu", "Bến đông", "Bến tây", "Bến nam", "Bến bắc"},
        {"Hang động", "Hang phía đông", "Hang phía tây", "Hang phía nam", "Hang phía bắc"},
        {"Thuyền", "Thuyền bến đông", "Thuyền bến tây", "Thuyền bến nam", "Thuyền bến bắc"},
        {"Hang Thằn Lằn", "Hang Đại Bàng", "Hang thằn lằn tây", "Hang thằn lằn nam", "Hang thằn lằn bắc"},
        {"Hang Dracula", "Hang Dracula phía đông", "Hang Dracula phía tây", "Hang Dracula phía nam", "Hang Dracula phía bắc"},
        {"Đấu trường", "", "", "", ""},
        {"Thành Trấn Danh", "", "", "", ""},
        {"Phong châu", "Đông Phong châu", "Tây Phong châu", "Nam Phong châu", "Bắc Phong châu"},
        {"Bình lục", "Đông Bình lục", "Tây Bình lục", "Nam Bình lục", "Bắc Bình lục"},
        {"Bảo thái", "Đông Bảo thái", "Tây Bảo thái", "Nam Bảo thái", "Bắc Bảo thái"},
        {"Hầm tối", "Đông Hầm tối", "Tây Hầm tối", "Nam Hầm tối", "Bắc Hầm tối"},
        {"Hồi hồ", "Đông Hồi hồ", "Tây Hồi hồ", "Nam Hồi hồ", "Bắc Hồi hồ"},
        {"Tế giang", "Đông Tế giang", "Tây Tế giang", "Nam Tế giang", "Bắc Tế giang"},
        {"Bạch hạc", "Đông Bạch hạc", "Tây Bạch hạc", "Nam Bạch hạc", "Bắc Bạch hạc"},
        {"Siêu Loại", "Đông Siêu Loại", "Tây Siêu Loại", "Nam Siêu Loại", "Bắc Siêu Loại"},
        {"Bình Kiều", "Đông Bình Kiều", "Tây Bình Kiều", "Nam Bình Kiều", "Bắc Bình Kiều"},
        {"Chiến hạm", "Đông Chiến hạm", "Tây Chiến hạm", "Nam Chiến hạm", "Bắc Chiến hạm"},
        {"Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã", "Đấu trường la mã"},
        {"Ngục tối", "", "", "", ""},
        {"Biên giới", "", "", "", ""},
        {"Khu mỏ", "", "", "", ""},
        {"Dương đông", "Đông Dương đông", "Tây Dương đông", "Nam Dương đông", "Bắc Dương đông"},
        {"Lâm tây", "Đông Lâm tây", "Tây Lâm tây", "Nam Lâm tây", "Bắc Lâm tây"},
        {"Cẩm khê", "Đông Cẩm khê", "Tây Cẩm khê", "Nam Cẩm khê", "Bắc Cẩm khê"},
        {"Đỗ đông", "Đông Đỗ đông", "Tây Đỗ đông", "Nam Đỗ đông", "Bắc Đỗ đông"},
        {"Lãnh địa bang", "", "", "", ""},
        {"Rừng già", "", "", "", ""},
        {"TYT 5x", "", "", "", ""},
        {"TYT 6x", "", "", "", ""},
        {"TYT 7x", "", "", "", ""},
        {"Chien truong", "", "", "", ""},
        {"Thành liên đấu 1", "", "", "", ""},
        {"Thành liên đấu 2", "", "", "", ""},
        {"Thành liên đấu 3", "", "", "", ""},
        {"Thành liên đấu 4", "", "", "", ""},
        {"Thành liên đấu 5", "", "", "", ""},
        {"Thành liên đấu 6", "", "", "", ""},
        {"Thành liên đấu 7", "", "", "", ""},
        {"Thành liên đấu 8", "", "", "", ""},
        {"Thành liên đấu 9", "", "", "", ""},
        {"Thành liên đấu 10", "", "", "", ""},
        {"Chiến trường", "", "", "", ""},
        {"Cân núi", "", "", "", ""},
        {"Núi kho báu", "", "", "", ""},
        {"Lôi đài", "", "", "", ""},
        {"Sảnh chờ", "", "", "", ""}
    };
    byte idXaphu = -1;
    static short[] idArrMap = new short[]{
        0,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18,
        19,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        32,
        33,
        34,
        35,
        36,
        37,
        38,
        39,
        40,
        41,
        42,
        43,
        44,
        45,
        46,
        47,
        48,
        49,
        50,
        51,
        52,
        53,
        54,
        55,
        56,
        57,
        58
    };
    static short[][] mapID = new short[][]{
        {0, 301, 302, 303, 304},
        {1, 321, 322, 323, 324},
        {2, 341, 342, 343, 344},
        {3, 361, 362, 363, 364},
        {4, 381, 382, 383, 384},
        {5, 401, 402, 403, 404},
        {6, 421, 422, 423, 424},
        {7, 441, 442, 443, 444},
        {8, 461, 462, 463, 464},
        {9, 481, 482, 483, 484},
        {10, 501, 502, 503, 504},
        {11, 521, 522, 523, 524},
        {12, 541, 542, 543, 544},
        {106, 2421, 2422, 2423, 2424},
        {110, 2501, 2502, 2503, 2504},
        {107, 2441, 2442, 2443, 2444},
        {112, 2541, 2542, 2543, 2544},
        {111, 2521, 2522, 2523, 2524},
        {202, 202, 202, 202, 202},
        {201, 201, 201, 201, 201},
        {13, 561, 562, 563, 564},
        {14, 581, 582, 583, 584},
        {15, 601, 602, 603, 604},
        {113, 2561, 2562, 2563, 2564},
        {114, 2581, 2582, 2583, 2584},
        {19, 681, 682, 683, 684},
        {18, 661, 662, 663, 664},
        {20, 701, 702, 703, 704},
        {21, 721, 722, 723, 724},
        {22, 741, 742, 743, 744},
        {203, 4361, 4362, 4363, 4364, 4365, 4366},
        {117, 117, 117, 117, 117},
        {118, 118, 118, 118, 118},
        {17, 17, 17, 17, 17},
        {70, 1701, 1702, 1703, 1704},
        {80, 1901, 1902, 1903, 1904},
        {23, 761, 762, 763, 764},
        {24, 781, 782, 783, 784},
        {206, 206, 206, 206, 206},
        {26, 821, 822, 823, 824},
        {208, 208, 208, 208, 208},
        {209, 209, 209, 209, 209},
        {210, 210, 210, 210, 210},
        {27, 27, 27, 27, 27},
        {30, 30, 30, 30, 30},
        {31, 31, 31, 31, 31},
        {32, 32, 32, 32, 32},
        {33, 33, 33, 33, 33},
        {34, 34, 34, 34, 34},
        {35, 35, 35, 35, 35},
        {36, 36, 36, 36, 36},
        {37, 37, 37, 37, 37},
        {38, 38, 38, 38, 38},
        {39, 39, 39, 39, 39},
        {40, 40, 40, 40, 40},
        {41, 41, 41, 41, 41},
        {42, 42, 42, 42, 42},
        {227, 227, 227, 227, 227},
        {228, 228, 228, 228, 228}
    };
    public static Vector<Vector<Vector<CharWeaponData>>> weapone_data = new Vector();
    public static Hashtable<Short, byte[]> idMapSend2Client = new Hashtable();
    static short timeDelay;
    static int indexInfoAdv;
    static int indexInfoAdv2;
    static Char botRaoVat;
    static Vector<Char> npcVilage;
    static Vector<Char> npcHoatieu;
    static Vector<Char> npcGate;
    public Vector<Char> npcPrivateMap = new Vector();
    public static Vector<Item> all_item_shop_special;
    /** Moi 19/09/2026: danh muc rieng cho "Shop Trung Thu" (menu Khac, idOptionMenu 7) -
     * TACH voi all_item_shop_special vi shop kia bat buoc Vip, con shop nay thi khong. */
    public static Vector<Item> all_item_shop_trungthu;
    public static short idNPC49;
    static Char botChuyenlanhtho;
    public byte nRegion = 0;
    public short[] pRegion = null;
    public static Hashtable<String, CharInfo> ALL_CHAR_BUY_RUONG;
    public static Hashtable<String, Integer> ALL_CHAR_VIP;
    public static Hashtable<String, CharChuyenPhieuQua> ALL_CHAR_RECEIVE_KIM_CUONG;
    public static Hashtable<String, CharChuyenPhieuQua> ALL_CHAR_CHUYEN_KIM_CUONG;
    public static Vector<Char> ALL_CHAR_HS;
    public static Hashtable<Integer, Char> HASH_ALL_CHAR_HS;
    public Vector<CMLocation> mapchangeLocations = new Vector();
    public static byte[][] imgDynamic;
    byte[] idImgDynamic;
    byte[] idImg;
    byte[] limX;
    byte[] limY;
    byte[] nFrame;
    byte[] xDynamic;
    byte[] yDynamic;
    static boolean sendDynamicObj;
    static byte[] imgIconBang;
    public static int hashCodeValue;
    public static int versionPotion;
    public static int versionGem;
    public static byte[] imgPotion;
    public static byte[] imgItemIcon;
    public static byte[] imgGemItem;
    public static byte[] imgLinhthu;
    public static byte[] sound;
    public static byte[] imgCountry;
    public static short MAX_CLAN;
    public static Vector<Boss> bossCopy;
    public Vector<Vector<Monster>> tempMonster = new Vector();
    public Vector<Vector<Monster>> tempRemoveMonster = new Vector();
    public static Vector<Boss> boss;
    public static Vector<Boss> bossTime;
    public static byte MAX_COUNTRY;
    public static boolean loadGhost;
    public static int idGhost;
    static int[][] pos_fire_work;
    boolean[] haveCharMonter = new boolean[3];
    long[] timeMonster = new long[]{System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis()};
    long[] timeLiveMonster = new long[3];
    long[] timeStartGame = new long[3];
    public static String newDay;
    static long timeCheckCallBoss;
    boolean isSkelonton = false;
    public static Random r;
    byte[] nCharBeKilled = new byte[3];
    static long timeDropCake;
    static short[] MAX_RANGE_CHAR;
    public static final int timeBossApear = 24;
    int vx = 0;
    int nmonster = 0;
    public static boolean isTrungHoaKyLan;
    public static String[] timeOpenHoaKyLan;
    public static int ID_RUONG_MAY_MAN_TRUNG;
    public static short ALL_RUONG_MAY_MAN;
    public static boolean isStopSellRuong;
    public static long timeWaitSellRuongMM;
    public static byte[] TOTAL_ITEM_RUONG_MAY_MAN;
    public static int[] TOTAL_DONG_XU_MAY_MAN;
    static int[] ID_DONG_XU;
    static short MAX_RUONG_MM_SELL;
    static long timeSellRuongMayMan;
    public byte total_cuongthi = 0;
    static int[][] ID_MAP_THUNG_GO;
    public static RuongMaQuai ruong_ma_quai_hac_ho;
    public static RuongMaQuai ruong_ma_quai_thanh_long;
    static short IDBATTLE;
    static String[] gifcodengudetanthu;
    static String[] gifcodengudeTrian;
    static String[] gifcodengudeTopVanlang;
    public static String[] code100;
    static short[][] idMaterial13;
    static long timeReceiveGoldHeart;
    static long timeReceiveAnimal;
    static byte[] lvEventMonster;
    static int IDARENA;
    static int[] ID_NPC_THANH;
    public static int MAX_LI_XI;
    public static int hourGiveLixi;
    public static int minuteGiveLixi;
    public static short TYPE_SHOP_NEW_TUI_DO;
    public static short TYPE_SHOP_NEW_CUA_HANG_VIP;
    public static short TYPE_SHOP_NEW_BO_ITEM_VAO_TUI;
    /** Moi 19/09/2026: "Shop Trung Thu" - dung chung UI createMessageShopNew nhung KHONG
     * bat buoc Vip nhu TYPE_SHOP_NEW_CUA_HANG_VIP (xem nhanh rieng trong doBuyItemShopNew).
     * Sua 19/09/2026: gia tri 3 BI TRUNG voi mot nhanh "type==3" co san (nang cap trang bi
     * 860-871) NGAY TRONG CUNG ham doBuyItemShopNew - client gui type=3 bi nhanh do chan
     * nham, bao "Vat pham khong hop le". Doi sang 8 (kiem tra het moi "type ==" trong ham
     * do: 0,1,2,3,4,5,6,7,14,15,99 deu da dung, 8 con trong). */
    public static short TYPE_SHOP_NEW_TRUNG_THU;
    public static short TYPE_NANG_CAP_THAN_KHI;
    public static short TYPE_DOI_DONG_THAN_KHI;
    public static short MAX_VIP1;
    public static short MAX_VIP2;
    public static short MAX_VIP3;
    static byte allItemBlackfriday;
    public static int[] ID_MAT_NA;
    public static int totaltrungget;
    public static int totalxuathienvv;
    public static int totaltrungvv;
    public static int minOpenHopqua;
    public static int countOpenHopqua;
    public static byte totalPhuongHoang;
    public static byte winPhuongHoang;
    public static int[] ID_VU_KHI_THOI_TRANG;
    static long timeHoaTuyet;
    static long timeTangHoa;
    static int idClassTest;
    static int idColorTest;
    static int[][] ID_ITEMTEPLATE_CREATE;
    public static byte teamtongkim;
    public static boolean openvantieu;
    // Ten cua NPC bi an (khong gui cho client). NPC "admin" cu dung ten nay.
    /**
     * Ten cua NPC an chi admin thay.
     *
     * PHAI KHAC (phan biet hoa thuong) ten cua moi nhan vat that. Ly do:
     * NPC nay duoc dang ky vao CharManager (CharManager.instance.put(bot)), ma
     * CharManager.put() TU CHOI khi ten da ton tai trong Hashtable charName ->
     * SelectCharHandler:246 goi session.disconnect(14). Ket qua: nhan vat trung
     * ten voi NPC nay dang nhap duoc nhung bi ngat ngay luc chon nhan vat.
     *
     * Truoc day ten NPC la "admin" (chu thuong) nen KHONG AI dat duoc nhan vat
     * ten "admin" - do la ly do ban goc dat ten GM khac han ten NPC nay.
     *
     * Nay dat "ADMIN" chu HOA: Hashtable phan biet hoa thuong nen khong dung voi
     * nhan vat that ten "admin" chu thuong. Nhung phai luu y:
     *   - CharCapNhat.updateNearChar loc bang Map.laNpcAnCuaAdmin (so sanh equals) nen an dung NPC nay.
     *   - CharManager.getCharByCharName() luon .toLowerCase() truoc khi tra cuu,
     *     nen NPC nay khong bao gio tim duoc theo ten (hien khong cho nao can).
     *   - CharManager.remove() so sanh khong phan biet hoa thuong khi don vChars,
     *     nen lan dau nhan vat "admin" thoat game se lam bot bi go khoi vChars
     *     (bot van con trong npcVilage nen NPC hien thi va quang cao khong anh
     *     huong).
     *   - Neu co nhan vat ten dung "ADMIN" chu hoa thi loi disconnect(14) tro lai.
     *     Hien khong the xay ra: cot charname co UNIQUE index kieu utf8mb4_general_ci
     *     (khong phan biet hoa thuong) nen da co "admin" thi khong tao duoc "ADMIN",
     *     va Database.laTenDanhRieng chan moi ten chua chu "admin".
     */
    public static final String NAME_NPC_AN = "ADMIN";

    /**
     * NPC "ADMIN" co duoc giau khoi nguoi choi nay khong?
     *
     * VI SAO PHAI CO HAM RIENG: NPC nay nam trong Map.npcVilage, va MapKhoiTao con
     * addPlayer() no vao map.allPlayers[0] va [1]. Nghia la no den client qua HAI duong
     * khac nhau trong CharCapNhat.updateNearChar:
     *   1) vong rieng duyet npcVilage
     *   2) vong quet nguoi choi quanh day (getAllPlayerMap) — vong nay chi loc bang
     *      `!p1.isAdmin`, ma isAdmin cua bot nay KHONG BAO GIO duoc dat (mac dinh false
     *      tu CharKhoiTao), nen no lot qua het.
     * Truoc day chi vong (1) duoc chan, nen nguoi choi thuong VAN THAY NPC nay.
     * De quy tac o mot cho de ca hai duong dung chung va duong thu ba sau nay khoi quen.
     */
    public static boolean laNpcAnCuaAdmin(final Char c) {
        return c != null && NAME_NPC_AN.equals(c.charname);
    }
    static Vector<String> topHoa;
    static long timeGettopHoa;
    static String[] VIP;
    public static boolean okChedo;
    int idQuestSuthi = 0;
    int idQuestClan = 0;
    public static boolean pause;
    public static boolean pauseSellTown;
    static short[] offMapID;
    boolean board = true;
    public static boolean openMarket;
    public static int AO_DAI;
    public static int LAC_LONG_QUAN;
    public static int NOEL_NU;
    public static long timeGettop;
    public static Vector<CharInfo> topRich;
    public static Vector<CharInfo> topLv;
    public static Vector<NewClan> topClan;
    public static Vector<Vector<CharInfo>> topLvNation;
    public static Vector<Vector<CharInfo>> topHonor;
    public static Vector<Vector<CharInfo>> topPK;
    public static boolean getTop;
    public static String currentDay;
    static String[] topnation;
    public static boolean stopGiveMoney;
    public static boolean isUseNewClan;
    public static int ID_THE_MUA_BAN;
    public static int config;
    public static boolean onOffSound;
    public static boolean adminTest;
    public static Vector<CharInfo> chartrungquayso;
    public static byte counttop;
    public static boolean isThamDinh;
    public static boolean testMove;
    public static boolean istestliendau;
    public static int indexPet;
    public static byte COUNT_DOWN;
    public static byte NOT_COUNT_DOWN;
    public static String[] extend;
    public static String[] stRao;
    static short[][] posNpcThanTai;
    static short[][] posNpcSANTA;
    static short[][] posNpcVuLan;
    static short[][] posNpcCayLixi;
    static short[] wHouse;
    static short[] hHouse;
    static Hashtable<Short, short[]> mapChange;
    static byte[] X_FOWARD;
    static byte[] Y_FOWARD;
    public static long DELAY_UPDATE_MAP;
    // Hai vong lap "RUN MAP" (updateWind/update) chay MAI cho MOI map. Neu co
    // loi, no lap lai moi DELAY_UPDATE_MAP (300ms) cho toi khi server tat -
    // in day du stack trace moi lan se ngap log ngay lap tuc. Chi bao 1 lan
    // cho ca server (dung chung giua cac map), giong GhiSoOnline.baoLoiMotLan.
    private static boolean daBaoLoiRunMapWind = false;
    private static boolean daBaoLoiRunMapUpdate = false;
    Object LOCK = new Object();
    Object LOCK2 = new Object();
    Object LOCK1 = new Object();
    long lastTimeUpdateMap;
    long lastTimeUpdateMap1;
    long lastTimeUpdateMap2;
    public int map_run_state;
    public int map_run_state1;
    public int map_run_state2;
    public static final int STATE_PROCESS = 0;
    public static final int STATE_SLEEP = 1;
    public static long timeWait;
    public static boolean goldTime;
    public static boolean npcAppear;
    public static boolean pkAuto;
    public String[] curday = new String[]{"", "", ""};
    public boolean[] killMonster = new boolean[3];
    public static boolean[] eventKillMonster;
    public static byte timeEventMonster;
    public static byte minuteEventMons;
    Vector<PlayerMessage> playerMessages = new Vector();
    Vector<Vector<PlayerMessage>> allPlayerMessages = new Vector();
    public static Vector<RegisterAttack> nationWar;
    public static boolean giveSpecialItem;
    public static Vector<Vector<Vector<Short>>> groupMonster;
    public static Vector<Vector<String>> listRegArena;
    public static int[] allUser;
    public static int[] allLV;
    public static String dayQuaySo;
    public static byte nPhuongHoang;
    public static byte max_phuong_hoang;
    public static int countQuaySo;
    public static int MAX_QUAY;
    public static long allXuLucky;
    public static long allLuongLucky;
    public static byte winNumber;
    public static byte deltaLVAttackMons;
    public static Vector<TeamArena> teamArena;
    public static boolean dropHoaHong;
    public Vector<Vector<EffectBuff>> ALL_EFFECT_INMAP = new Vector();
    public static byte totalChoiBan;
    public static String nameTopNapLuong;
    public static long maxLuongNap;
    public static long timeSellChoi;
    public static byte tradePet;

    static {
        // Luong tinh cua VAN TIEU: cu 300 ms cap nhat moi xe tieu va ket thuc xe da het gio.
        //
        // Truoc day than vong lap KHONG co try/catch va duyet ALL_MONSTER_VANTIEU theo chi so
        // trong khi cac luong ban do them/bot xe (Char.doInitMonter, huy tieu). Vector rut mot
        // phan tu giua size() va get(i) la ArrayIndexOutOfBoundsException -> luong nay CHET
        // VINH VIEN cho toi khi restart: xe tieu khong update, khong bao gio isFinish, nguoi
        // choi treo "chua hoan thanh chuyen tieu da nhan" mai. Nay: duyet tren ban sao, moi
        // xe/moi vong co try/catch rieng, va chi xu ly xe con nam trong danh sach luc go
        // (tranh tra id monster ve kho hai lan khi nguoi choi huy tieu cung luc).
        new Thread(() -> {
            while (!AdminHandler.isStopServer) {
                try {
                    Vector<MonsterVantieu> ms = new Vector();
                    Object[] anh = MonsterVantieu.ALL_MONSTER_VANTIEU.toArray();

                    for (int i = 0; i < anh.length; i++) {
                        MonsterVantieu mons = (MonsterVantieu) anh[i];
                        if (mons == null) {
                            continue;
                        }
                        try {
                            if (mons.isFinish()) {
                                ms.add(mons);
                            } else {
                                mons.update();
                            }
                        } catch (Throwable loiXe) {
                            loiXe.printStackTrace();
                        }
                    }

                    for (int ix = 0; ix < ms.size(); ix++) {
                        MonsterVantieu mon = (MonsterVantieu) ms.get(ix);
                        try {
                            boolean conTrongDanhSach = MonsterVantieu.ALL_MONSTER_VANTIEU.contains(mon);
                            MonsterVantieu.removeMonster(mon);
                            if (!conTrongDanhSach) {
                                // Mot luong khac (huy tieu / nhan tieu moi) da go xe nay roi va da
                                // tra id ve kho - khong lam lai lan hai.
                                continue;
                            }
                            Char p = CharManager.instance.getCharByCharDbID(mon.charDbid);
                            if (p != null) {
                                if (p.monster != null && p.monster.isFinish) {
                                    try {
                                        p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Chúc mừng bạn đã có 1 chuyến tiêu thành công."));
                                    } catch (IOException var7) {
                                    }
                                } else {
                                    p.sendMessage(MessageCreator.createServerAlertMessage("Chuyến tiêu của bạn đã kết thúc.", ""));
                                }

                                if (p.monster == mon) {
                                    p.monster = null;
                                    p.typeTieu = -1;
                                }
                            }

                            if (mon.map != null) {
                                mon.map.removeMonster(mon.id, mon.inCountry, mon.region);
                            }

                            try {
                                Message m = new Message(90);
                                m.dos.writeShort(mon.id);
                                m.dos.writeByte(mon.cat);
                                Map.sendAllCharServerInMap(new int[]{0, 70, 301, 1701, 1, 2, 3, 4, 5, 6, 7, 8, 9, 118}, m);
                            } catch (Exception var8) {
                                var8.printStackTrace();
                            }

                            RealController.intance.idGen.putID(mon.id, 1, "xoa monster van tieu");
                            Database.instance.saveOrtherLog("", mon.namemaster, mon.getName(), "hethan");
                        } catch (Throwable loiGo) {
                            loiGo.printStackTrace();
                        }
                    }
                } catch (Throwable loiVong) {
                    // Khong de bat ky loi nao giet luong nay.
                    loiVong.printStackTrace();
                }

                try {
                    Thread.sleep(300L);
                } catch (Exception var6) {
                }
            }
        }).start();

        try {
            short[] id = new short[]{
                6,
                8,
                12,
                17,
                18,
                19,
                20,
                21,
                22,
                23,
                29,
                90,
                91,
                92,
                93,
                94,
                95,
                96,
                97,
                98,
                99,
                100,
                101,
                102,
                103,
                104,
                105,
                107,
                108,
                109,
                110,
                111,
                112,
                115,
                116,
                117,
                118,
                202,
                201,
                203,
                260,
                261,
                262,
                263,
                264,
                265,
                266,
                267,
                268,
                269,
                270,
                271,
                70,
                80,
                30,
                31,
                72,
                73,
                74,
                78,
                79,
                82,
                83,
                84,
                88,
                89,
                10,
                11,
                15,
                113,
                114,
                3,
                4,
                5,
                7,
                8,
                9,
                14,
                24,
                225,
                226,
                204,
                205,
                2,
                1,
                106,
                13,
                206,
                207,
                25,
                26,
                208,
                27,
                40,
                41,
                42,
                227,
                228
            };

            for (short i = 0; i < id.length; i++) {
                try {
                    FileInputStream fis = null;
                    DataInputStream dis = null;
                    fis = new FileInputStream("cMap/" + id[i]);
                    dis = new DataInputStream(fis);
                    byte[] byteMap = new byte[dis.available()];
                    dis.read(byteMap, 0, byteMap.length);

                    try {
                        dis.close();
                    } catch (Exception var22) {
                    }

                    try {
                        fis.close();
                    } catch (Exception var21) {
                    }

                    idMapSend2Client.put(id[i], byteMap);
                } catch (Exception var23) {
                }
            }
        } catch (Exception var25) {
            var25.printStackTrace();
        }

        timeDelay = 200;
        indexInfoAdv = 0;
        indexInfoAdv2 = 0;
        botRaoVat = null;
        npcVilage = new Vector();
        npcHoatieu = new Vector();
        npcGate = new Vector();
        all_item_shop_special = new Vector();
        all_item_shop_trungthu = new Vector();
        idNPC49 = -1;
        botChuyenlanhtho = null;
        ALL_CHAR_BUY_RUONG = new Hashtable();
        ALL_CHAR_VIP = new Hashtable();
        ALL_CHAR_RECEIVE_KIM_CUONG = new Hashtable();
        ALL_CHAR_CHUYEN_KIM_CUONG = new Hashtable();
        ALL_CHAR_HS = new Vector();
        HASH_ALL_CHAR_HS = new Hashtable();
        sendDynamicObj = true;
        hashCodeValue = 24;
        versionPotion = 69;
        versionGem = 15;
        MAX_CLAN = 1344; // them icon 1342 (logo Manchester United) + 1343 (co sao vang), 17/09/2026

        try {
            listKillPerson.add(new Vector());
            listKillPerson.add(new Vector());
            listKillPerson.add(new Vector());
            if (imgDynamic == null) {
                imgDynamic = new byte[7][];
                String[] url = new String[]{"map/dao.png", "map/mai.png", "map/rong.png", "map/rong1.png", "map/banhsn.png", "map/caythong.png", "map/longden.png"};
                FileInputStream fis = null;
                DataInputStream dis = null;

                for (byte i = 0; i < url.length; i++) {
                    fis = new FileInputStream(url[i]);
                    dis = new DataInputStream(fis);
                    imgDynamic[i] = new byte[dis.available()];
                    int byteRead = 0;

                    while (byteRead != -1 && byteRead < imgDynamic[i].length) {
                        int len = dis.read(imgDynamic[i], byteRead, imgDynamic[i].length - byteRead);
                        if (len > 0) {
                            byteRead += len;
                        }

                        if (len == -1) {
                            break;
                        }
                    }

                    try {
                        fis.close();
                    } catch (Exception var20) {
                    }

                    try {
                        dis.close();
                    } catch (Exception var19) {
                    }
                }
            }

            if (imgIconBang == null) {
                FileInputStream fis = new FileInputStream("map/iconclan.png");
                DataInputStream dis = new DataInputStream(fis);
                imgIconBang = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < imgIconBang.length) {
                    int lenx = dis.read(imgIconBang, byteRead, imgIconBang.length - byteRead);
                    if (lenx > 0) {
                        byteRead += lenx;
                    }

                    if (lenx == -1) {
                        break;
                    }
                }

                for (int i = 0; i < MAX_CLAN; i++) {
                    idIconClan.add(new Integer(i));
                }

                Vector<Integer> idCLan = Database.instance.getIDClanUsed();

                for (int i = 0; i < idCLan.size(); i++) {
                    idIconClan.remove(new Integer((Integer) idCLan.get(i)));
                }

              

                try {
                    fis.close();
                } catch (Exception var18) {
                }

                try {
                    dis.close();
                } catch (Exception var17) {
                }
            }

            if (imgPotion == null) {
                FileInputStream fis = new FileInputStream("map/potionIcon.png");
                DataInputStream dis = new DataInputStream(fis);
                imgPotion = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < imgPotion.length) {
                    int lenxx = dis.read(imgPotion, byteRead, imgPotion.length - byteRead);
                    if (lenxx > 0) {
                        byteRead += lenxx;
                    }

                    if (lenxx == -1) {
                        break;
                    }
                }

                try {
                    fis.close();
                } catch (Exception var16) {
                }

                try {
                    dis.close();
                } catch (Exception var15) {
                }
            }

            if (imgLinhthu == null) {
                FileInputStream fis = new FileInputStream("map/linhthu.png");
                DataInputStream dis = new DataInputStream(fis);
                imgLinhthu = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < imgLinhthu.length) {
                    int lenxxx = dis.read(imgLinhthu, byteRead, imgLinhthu.length - byteRead);
                    if (lenxxx > 0) {
                        byteRead += lenxxx;
                    }

                    if (lenxxx == -1) {
                        break;
                    }
                }

                try {
                    fis.close();
                } catch (Exception var14) {
                }

                try {
                    dis.close();
                } catch (Exception var13) {
                }
            }

            if (imgItemIcon == null) {
                FileInputStream fis = new FileInputStream("map/itemIconGiap.png");
                DataInputStream dis = new DataInputStream(fis);
                imgItemIcon = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < imgItemIcon.length) {
                    int lenxxxx = dis.read(imgItemIcon, byteRead, imgItemIcon.length - byteRead);
                    if (lenxxxx > 0) {
                        byteRead += lenxxxx;
                    }

                    if (lenxxxx == -1) {
                        break;
                    }
                }

                try {
                    fis.close();
                } catch (Exception var12) {
                }

                try {
                    dis.close();
                } catch (Exception var11) {
                }
            }

            if (imgGemItem == null) {
                FileInputStream fis = new FileInputStream("map/gemIcon.png");
                DataInputStream dis = new DataInputStream(fis);
                imgGemItem = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < imgGemItem.length) {
                    int lenxxxxx = dis.read(imgGemItem, byteRead, imgGemItem.length - byteRead);
                    if (lenxxxxx > 0) {
                        byteRead += lenxxxxx;
                    }

                    if (lenxxxxx == -1) {
                        break;
                    }
                }

                try {
                    fis.close();
                } catch (Exception var10) {
                }

                try {
                    dis.close();
                } catch (Exception var9) {
                }
            }

            if (sound == null) {
                FileInputStream fis = new FileInputStream("map/nhac_noel.mid");
                DataInputStream dis = new DataInputStream(fis);
                sound = new byte[dis.available()];
                int byteRead = 0;

                while (byteRead != -1 && byteRead < sound.length) {
                    int lenxxxxxx = dis.read(sound, byteRead, sound.length - byteRead);
                    if (lenxxxxxx > 0) {
                        byteRead += lenxxxxxx;
                    }

                    if (lenxxxxxx == -1) {
                        break;
                    }
                }

                try {
                    fis.close();
                } catch (Exception var8) {
                }

                try {
                    dis.close();
                } catch (Exception var7) {
                }
            }
        } catch (Exception var24) {
            System.out.println("LOI LOAD ICON CLAN");
        }

        bossCopy = new Vector();
        boss = new Vector();
        bossTime = new Vector();
        MAX_COUNTRY = 2;
        loadGhost = false;
        idGhost = 84;
        pos_fire_work = new int[][]{{8, 29}, {16, 35}, {16, 35}};
        newDay = "";
        timeCheckCallBoss = System.currentTimeMillis() + 20000L;
        r = new Random(System.currentTimeMillis());
        timeDropCake = System.currentTimeMillis() + 1200000L;
        MAX_RANGE_CHAR = new short[]{130, 130, 130, 130, 130};
        isTrungHoaKyLan = true;
        timeOpenHoaKyLan = new String[]{
            "", "", "2018-02-16 16:30:00", "2018-02-17 10:30:00", "2018-02-18 22:30:00", "2018-02-18 22:30:00", "2018-02-18 22:30:00"
        };
        ID_RUONG_MAY_MAN_TRUNG = 0;
        ALL_RUONG_MAY_MAN = 300;
        isStopSellRuong = false;
        timeWaitSellRuongMM = System.currentTimeMillis() + 1800000L;
        TOTAL_ITEM_RUONG_MAY_MAN = new byte[]{2, 20, 20, 20, 20, 3, 3, 5};
        TOTAL_DONG_XU_MAY_MAN = new int[]{2, 5, 5, 5, 5, 3, 3, 5};
        ID_DONG_XU = new int[]{730, 731, 731, 731, 731, 734, 732, 733};
        MAX_RUONG_MM_SELL = 300;
        timeSellRuongMayMan = 0L;
        ID_MAP_THUNG_GO = new int[][]{
            {3, 23, 6},
            {7, 6, 24},
            {7, 62, 9},
            {10, 11, 7},
            {11, 84, 4},
            {11, 7, 7},
            {13, 63, 35},
            {13, 64, 62},
            {14, 6, 42},
            {14, 71, 6},
            {15, 8, 57},
            {18, 54, 31},
            {19, 6, 10},
            {20, 61, 10},
            {21, 7, 13},
            {22, 56, 29},
            {113, 44, 3},
            {114, 4, 9}
        };
        IDBATTLE = 0;
        gifcodengudetanthu = new String[0];
        gifcodengudeTrian = new String[0];
        gifcodengudeTopVanlang = new String[0];
        code100 = new String[0];
        idMaterial13 = new short[][]{
            {68, 75, 82, 89, 96}, {69, 76, 83, 90, 97}, {70, 77, 84, 91, 98}, {103, 110, 117, 124, 131}, {104, 111, 118, 125, 132}, {105, 112, 119, 126, 133}
        };
        timeReceiveGoldHeart = System.currentTimeMillis() + 1800000L;
        timeReceiveAnimal = System.currentTimeMillis() + 43200000L;
        lvEventMonster = new byte[]{29, 35, 40, 45, 50, 55, 60};
        IDARENA = 0;
        ID_NPC_THANH = new int[]{-3, -4, -5, -6, -16, -13, -25, -27, -29};
        MAX_LI_XI = 100;
        hourGiveLixi = -1;
        minuteGiveLixi = 0;
        TYPE_SHOP_NEW_TUI_DO = 0;
        TYPE_SHOP_NEW_CUA_HANG_VIP = 1;
        TYPE_SHOP_NEW_BO_ITEM_VAO_TUI = 2;
        TYPE_SHOP_NEW_TRUNG_THU = 8;
        TYPE_DOI_DONG_THAN_KHI = 14;
        TYPE_NANG_CAP_THAN_KHI = 15;
        MAX_VIP1 = 500;
        MAX_VIP2 = 100;
        MAX_VIP3 = 5;
        allItemBlackfriday = 50;
        ID_MAT_NA = new int[]{745, 746, 747, 748, 749};
        totaltrungget = 0;
        totalxuathienvv = 1000000;
        totaltrungvv = 0;
        minOpenHopqua = 10000;
        countOpenHopqua = 0;
        totalPhuongHoang = 5;
        winPhuongHoang = 0;
        ID_VU_KHI_THOI_TRANG = new int[]{715, 716, 717, 718, 719};
        timeHoaTuyet = System.currentTimeMillis();
        timeTangHoa = 0L;
        idClassTest = 0;
        idColorTest = 0;
        ID_ITEMTEPLATE_CREATE = new int[0][];
        teamtongkim = 0;
        openvantieu = true;
        topHoa = new Vector();
        timeGettopHoa = 0L;
        VIP = new String[]{"", "Cạ cứng", "Đại gia", "Vip"};
        okChedo = true;
        pause = false;
        pauseSellTown = false;
        offMapID = new short[]{
            90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 29, 102, 103, 104, 108, 109, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271
        };
        openMarket = true;
        AO_DAI = 267;
        LAC_LONG_QUAN = 265;
        NOEL_NU = 579;
        timeGettop = System.currentTimeMillis();
        topRich = new Vector();
        topLv = new Vector();
        topClan = new Vector();
        topLvNation = new Vector();
        topHonor = new Vector();
        topPK = new Vector();
        getTop = false;
        currentDay = Char.getDayOpen(0L);
        topnation = new String[]{
            "Top cao thủ Thanh long",
            "Top cao thủ Hắc hổ",
            "Top cao thủ Huyền vũ",
            "Top công trạng Thanh long",
            "Top công trạng Hắc hổ",
            "Top công trạng Huyền vũ",
            "Top liên trảm Thanh long",
            "Top liên trảm Hắc hổ",
            "Top liên trảm Huyền vũ",
            "Danh sách cừu nhân"
        };
        stopGiveMoney = false;
        isUseNewClan = true;
        ID_THE_MUA_BAN = 675;
        config = 0;
        onOffSound = false;
        adminTest = false;
        chartrungquayso = new Vector();
        counttop = 0;
        isThamDinh = false;
        testMove = false;
        istestliendau = false;
        indexPet = 0;
        COUNT_DOWN = 1;
        NOT_COUNT_DOWN = 0;
        extend = new String[]{
            ".com",
            ".info",
            ".name",
            ".net",
            ".org",
            ".pro",
            ".biz",
            ".asia",
            ".cat",
            ".coop",
            ".edu",
            ".gov",
            ".int",
            ".jobs",
            ".mil ",
            ".mobi",
            ".museum",
            ".tel",
            ".travel",
            ".ac",
            ".ad",
            ".ae",
            ".af",
            ".ag",
            ".ai",
            ".al",
            ".am",
            ".an",
            ".ao",
            ".aq",
            ".ar",
            ".as",
            ".at",
            ".au",
            ".aw",
            ".ax",
            ".az",
            ".ba",
            ".bb",
            ".bd",
            ".be",
            ".bf",
            ".bg",
            ".bh",
            ".bi",
            ".bj",
            ".bm",
            ".bn",
            ".bo",
            ".br",
            ".bs",
            ".bt",
            ".bw",
            ".by",
            ".bz",
            ".ca",
            ".cc",
            ".cd",
            ".cf",
            ".cg",
            ".ch",
            ".ci",
            ".ck",
            ".cl",
            ".cm",
            ".cn",
            ".co",
            ".cr",
            ".cu",
            ".cv",
            ".cx",
            ".cy",
            ".cz",
            ".de",
            ".dj",
            ".dk",
            ".dm",
            ".do",
            ".dz",
            ".ec",
            ".ee",
            ".eg",
            ".er",
            ".es",
            ".et",
            ".eu",
            ".fi",
            ".fj",
            ".fk",
            ".fm",
            ".fo",
            ".fr",
            ".ga",
            ".gd",
            ".ge",
            ".gf",
            ".gg",
            ".gh",
            ".gi",
            ".gl",
            ".gm",
            ".gn",
            ".gp",
            ".gq",
            ".gr",
            ".gs",
            ".gt",
            ".gu",
            ".gw",
            ".gy",
            ".hk",
            ".hm",
            ".hn",
            ".hr",
            ".ht",
            ".hu",
            ".id",
            ".ie",
            ".il",
            ".im",
            ".in",
            ".io",
            ".iq",
            ".ir",
            ".is",
            ".it",
            ".je",
            ".jm",
            ".jo",
            ".jp",
            ".ke",
            ".kg",
            ".kh",
            ".ki",
            ".km",
            ".kn",
            ".kp",
            ".kr",
            ".kw",
            ".ky",
            ".kz",
            ".la",
            ".lb",
            ".lc",
            ".li",
            ".lk",
            ".lr",
            ".ls",
            ".lt",
            ".lu",
            ".lv",
            ".ly",
            ".ma",
            ".mc",
            ".md",
            ".me",
            ".mg",
            ".mh",
            ".mk",
            ".ml",
            ".mm",
            ".mn",
            ".mo",
            ".mp",
            ".mq",
            ".mr",
            ".ms",
            ".mt",
            ".mu",
            ".mv",
            ".mw",
            ".mx",
            ".my",
            ".mz",
            ".na",
            ".nc",
            ".ne",
            ".nf",
            ".ng",
            ".ni",
            ".nl",
            ".no",
            ".np",
            ".nr",
            ".nu",
            ".nz",
            ".om",
            ".pa",
            ".pe",
            ".pf",
            ".pg",
            ".ph",
            ".pk",
            ".pl",
            ".pn",
            ".pr",
            ".ps",
            ".pt",
            ".pw",
            ".py",
            ".qa",
            ".re",
            ".ro",
            ".rs",
            ".ru",
            ".rw",
            ".sa",
            ".sb",
            ".sc",
            ".sd",
            ".se",
            ".sg",
            ".sh",
            ".si",
            ".sk",
            ".sl",
            ".sm",
            ".sn",
            ".sr",
            ".st",
            ".su",
            ".sv",
            ".sy",
            ".sz",
            ".tc",
            ".td",
            ".tf",
            ".tg",
            ".th",
            ".tj",
            ".tk",
            ".tl",
            ".tm",
            ".tn",
            ".to",
            ".tr",
            ".tt",
            ".tv",
            ".tw",
            ".tz",
            ".ua",
            ".ug",
            ".uk",
            ".us",
            ".uy",
            ".uz",
            ".va",
            ".vc",
            ".ve",
            ".vg",
            ".vi",
            ".vn",
            ".vu",
            ".wf",
            ".ws",
            ".ye",
            ".za",
            ".zm",
            ".zw",
            ".vn",
            ".com.vn",
            ".net.vn",
            ".biz.vn",
            ".info.vn",
            ".org.vn",
            ".gov.vn",
            ".name.vn",
            ".edu.vn",
            ".ac.vn",
            ".pro.vn",
            ".health.vn",
            ".angiang.vn",
            ".danang.vn",
            ".kontum.vn",
            ".quangtri.vn",
            ".bacgian.vn",
            ".dienbien.vn",
            ".laichau.vn",
            ".soctrang.vn",
            ".backan.vn",
            ".dongnai.vn",
            ".lamdong.vn",
            ".sonla.vn",
            ".baclieu.vn",
            ".dongthap.vn",
            ".langson.vn",
            ".tayninh.vn",
            ".bacninh.vn",
            ".gialai.vn",
            ".laocai.vn",
            ".thaibinh.vn.baria-vungtau.vn",
            ".hagiang.vn",
            ".longan.vn",
            ".thainguyen.vn",
            ".bentre.vn",
            ".haiduong.vn",
            ".namdinh.vn",
            ".thanhhoa.vn",
            ".binhdinh.vn",
            ".haiphong.vn",
            ".nghean.vn",
            ".thanhphohochiminh.vn",
            ".binhduong.vn",
            ".hanam.vn",
            ".ninhbinh.vn",
            ".thuathienhue.vn",
            ".binhphuoc.vn",
            ".hanoi.vn",
            ".ninhthuan.vn",
            ".tiengiang.vn",
            ".binhthuan.vn",
            ".hatinh.vn",
            ".phutho.vn",
            ".travinh.vn",
            ".camau.vn",
            ".haugiang.vn",
            ".phuyen.vn",
            ".tuyenquang.vn",
            ".cantho.vn",
            ".hoabinh.vn",
            ".quangbinh.vn",
            ".vinhlong.vn",
            ".caobang.vn",
            ".hungyen.vn",
            ".quangnam.vn",
            ".vinhphuc.vn",
            ".daklac.vn",
            ".khanhhoa.vn",
            ".quangngai.vn",
            ".yenbai.vn",
            ".daknong.vn",
            ".kiengiang.vn",
            ".quangninh.vn",
            ".Esy.es",
            ".16mb.com",
            ".96.Lt",
            ".hol.es",
            ".pe.hu",
            ".890m.com",
            ".wap.sh",
            "wap.",
            ".wap",
            ". com",
            ". info",
            ". name",
            ". net",
            ". org",
            ". pro",
            ". biz",
            ". asia",
            ". cat",
            ". coop",
            ". edu",
            ". gov",
            ". int",
            ". jobs",
            ". mil ",
            ". mobi",
            ". museum",
            ". tel",
            ". travel",
            ". ac",
            ". ad",
            ". ae",
            ". af",
            ". ag",
            ". ai",
            ". al",
            ". am",
            ". an",
            ". ao",
            ". aq",
            ". ar",
            ". as",
            ". at",
            ". au",
            ". aw",
            ". ax",
            ". az",
            ". ba",
            ". bb",
            ". bd",
            ". be",
            ". bf",
            ". bg",
            ". bh",
            ". bi",
            ". bj",
            ". bm",
            ". bn",
            ". bo",
            ". br",
            ". bs",
            ". bt",
            ". bw",
            ". by",
            ". bz",
            ". ca",
            ". cc",
            ". cd",
            ". cf",
            ". cg",
            ". ch",
            ". ci",
            ". ck",
            ". cl",
            ". cm",
            ". cn",
            ". co",
            ". cr",
            ". cu",
            ". cv",
            ". cx",
            ". cy",
            ". cz",
            ". de",
            ". dj",
            ". dk",
            ". dm",
            ". do",
            ". dz",
            ". ec",
            ". ee",
            ". eg",
            ". er",
            ". es",
            ". et",
            ". eu",
            ". fi",
            ". fj",
            ". fk",
            ". fm",
            ". fo",
            ". fr",
            ". ga",
            ". gd",
            ". ge",
            ". gf",
            ". gg",
            ". gh",
            ". gi",
            ". gl",
            ". gm",
            ". gn",
            ". gp",
            ". gq",
            ". gr",
            ". gs",
            ". gt",
            ". gu",
            ". gw",
            ". gy",
            ". hk",
            ". hm",
            ". hn",
            ". hr",
            ". ht",
            ". hu",
            ". id",
            ". ie",
            ". il",
            ". im",
            ". in",
            ". io",
            ". iq",
            ". ir",
            ". is",
            ". it",
            ". je",
            ". jm",
            ". jo",
            ". jp",
            ". ke",
            ". kg",
            ". kh",
            ". ki",
            ". km",
            ". kn",
            ". kp",
            ". kr",
            ". kw",
            ". ky",
            ". kz",
            ". la",
            ". lb",
            ". lc",
            ". li",
            ". lk",
            ". lr",
            ". ls",
            ". lt",
            ". lu",
            ". lv",
            ". ly",
            ". ma",
            ". mc",
            ". md",
            ". me",
            ". mg",
            ". mh",
            ". mk",
            ". ml",
            ". mm",
            ". mn",
            ". mo",
            ". mp",
            ". mq",
            ". mr",
            ". ms",
            ". mt",
            ". mu",
            ". mv",
            ". mw",
            ". mx",
            ". my",
            ". mz",
            ". na",
            ". nc",
            ". ne",
            ". nf",
            ". ng",
            ". ni",
            ". nl",
            ". no",
            ". np",
            ". nr",
            ". nu",
            ". nz",
            ". om",
            ". pa",
            ". pe",
            ". pf",
            ". pg",
            ". ph",
            ". pk",
            ". pl",
            ". pn",
            ". pr",
            ". ps",
            ". pt",
            ". pw",
            ". py",
            ". qa",
            ". re",
            ". ro",
            ". rs",
            ". ru",
            ". rw",
            ". sa",
            ". sb",
            ". sc",
            ". sd",
            ". se",
            ". sg",
            ". sh",
            ". si",
            ". sk",
            ". sl",
            ". sm",
            ". sn",
            ". sr",
            ". st",
            ". su",
            ". sv",
            ". sy",
            ". sz",
            ". tc",
            ". td",
            ". tf",
            ". tg",
            ". th",
            ". tj",
            ". tk",
            ". tl",
            ". tm",
            ". tn",
            ". to",
            ". tr",
            ". tt",
            ". tv",
            ". tw",
            ". tz",
            ". ua",
            ". ug",
            ". uk",
            ". us",
            ". uy",
            ". uz",
            ". va",
            ". vc",
            ". ve",
            ". vg",
            ". vi",
            ". vn",
            ". vu",
            ". wf",
            ". ws",
            ". ye",
            ". za",
            ". zm",
            ". zw",
            ". vn",
            ". com.vn",
            ". net.vn",
            ". biz.vn",
            ". info.vn",
            ". org.vn",
            ". gov.vn",
            ". name.vn",
            ". edu.vn",
            ". ac.vn",
            ". pro.vn",
            ". health.vn",
            ". angiang.vn",
            ". danang.vn",
            ". kontum.vn",
            ". quangtri.vn",
            ". bacgian.vn",
            ". dienbien.vn",
            ". laichau.vn",
            ". soctrang.vn",
            ". backan.vn",
            ". dongnai.vn",
            ". lamdong.vn",
            ". sonla.vn",
            ". baclieu.vn",
            ". dongthap.vn",
            ". langson.vn",
            ". tayninh.vn",
            ". bacninh.vn",
            ". gialai.vn",
            ". laocai.vn",
            ". thaibinh.vn.baria-vungtau.vn",
            ". hagiang.vn",
            ". longan.vn",
            ". thainguyen.vn",
            ". bentre.vn",
            ". haiduong.vn",
            ". namdinh.vn",
            ". thanhhoa.vn",
            ". binhdinh.vn",
            ". haiphong.vn",
            ". nghean.vn",
            ". thanhphohochiminh.vn",
            ". binhduong.vn",
            ". hanam.vn",
            ". ninhbinh.vn",
            ". thuathienhue.vn",
            ". binhphuoc.vn",
            ". hanoi.vn",
            ". ninhthuan.vn",
            ". tiengiang.vn",
            ". binhthuan.vn",
            ". hatinh.vn",
            ". phutho.vn",
            ". travinh.vn",
            ". camau.vn",
            ". haugiang.vn",
            ". phuyen.vn",
            ". tuyenquang.vn",
            ". cantho.vn",
            ". hoabinh.vn",
            ". quangbinh.vn",
            ". vinhlong.vn",
            ". caobang.vn",
            ". hungyen.vn",
            ". quangnam.vn",
            ". vinhphuc.vn",
            ". daklac.vn",
            ". khanhhoa.vn",
            ". quangngai.vn",
            ". yenbai.vn",
            ". daknong.vn",
            ". kiengiang.vn",
            ". quangninh.vn",
            ". Esy.es",
            ". 16mb.com",
            ". 96.Lt",
            ". hol.es",
            ". pe.hu",
            ". 890m.com",
            ". wap.sh",
            "wap.",
            ". wap"
        };
        stRao = new String[]{"wap.sh", "wap.", ".wap"};
        posNpcThanTai = new short[][]{{272, 352}, {224, 432}, {288, 480}};
        posNpcSANTA = new short[][]{{880, 672}, {560, 816}, {560, 704}};
        posNpcVuLan = new short[][]{{368, 608}, {288, 512}, {560, 704}};
        posNpcCayLixi = new short[][]{{368, 512}, {288, 416}, {368, 512}};
        wHouse = new short[]{143, 160, 144, 208, 288, 288, 96, 96, 113, 176, 144, 160};
        hHouse = new short[]{120, 120, 112, 128, 141, 205, 104, 104, 97, 112, 109, 125};
        mapChange = new Hashtable();

        try {
            mapChange.put((short) 105, new short[]{0, 70, 80});
            mapChange.put((short) 0, new short[]{0, 1, 2, 12, 106});
            mapChange.put((short) 1, new short[]{0, 1, 4, 70, 80});
            mapChange.put((short) 2, new short[]{0, 2, 3, 70, 80});
            mapChange.put((short) 3, new short[]{2, 3, 4, 5});
            mapChange.put((short) 4, new short[]{1, 3, 4, 110});
            mapChange.put((short) 5, new short[]{3, 5, 6});
            mapChange.put((short) 6, new short[]{5, 6, 7});
            mapChange.put((short) 7, new short[]{6, 7, 8});
            mapChange.put((short) 8, new short[]{7, 8, 9});
            mapChange.put((short) 9, new short[]{8, 9, 201, 118});
            mapChange.put((short) 10, new short[]{10, 11, 13, 14});
            mapChange.put((short) 11, new short[]{11, 10, 113, 114, 15});
            mapChange.put((short) 12, new short[]{12, 0, 70, 80});
            mapChange.put((short) 13, new short[]{13, 10, 20});
            mapChange.put((short) 14, new short[]{14, 10, 18, 23});
            mapChange.put((short) 15, new short[]{15, 11, 19, 24, 26});
            mapChange.put((short) 18, new short[]{18, 14, 19});
            mapChange.put((short) 19, new short[]{19, 15, 18, 20});
            mapChange.put((short) 20, new short[]{20, 13, 114, 19});
            mapChange.put((short) 106, new short[]{106, 0, 70, 80});
            mapChange.put((short) 110, new short[]{4, 110, 111, 112});
            mapChange.put((short) 111, new short[]{110});
            mapChange.put((short) 112, new short[]{110});
            mapChange.put((short) 113, new short[]{11, 113});
            mapChange.put((short) 114, new short[]{11, 20, 114});
            mapChange.put((short) 201, new short[]{9, 201, 206});
            mapChange.put((short) 70, new short[]{70, 1, 2, 12, 106});
            mapChange.put((short) 80, new short[]{80, 1, 2, 12, 106});
            mapChange.put((short) 23, new short[]{14, 118});
            mapChange.put((short) 24, new short[]{15, 118});
            mapChange.put((short) 118, new short[]{23, 24});
            mapChange.put((short) 206, new short[]{206, 201});
            mapChange.put((short) 26, new short[]{26, 15});
            mapChange.put((short) 208, new short[]{208});
            mapChange.put((short) 30, new short[]{9});
            mapChange.put((short) 31, new short[]{9});
            mapChange.put((short) 32, new short[]{9});
            mapChange.put((short) 33, new short[]{9});
            mapChange.put((short) 34, new short[]{9});
            mapChange.put((short) 35, new short[]{9});
            mapChange.put((short) 36, new short[]{9});
            mapChange.put((short) 37, new short[]{9});
            mapChange.put((short) 38, new short[]{9});
        } catch (Exception var6) {
        }

        X_FOWARD = new byte[]{0, 0, -17, 17};
        Y_FOWARD = new byte[]{17, -17, 0, 0};
        DELAY_UPDATE_MAP = 300L;
        goldTime = false;
        npcAppear = false;
        pkAuto = false;
        eventKillMonster = new boolean[3];
        timeEventMonster = 20;
        minuteEventMons = 5;
        nationWar = new Vector();
        giveSpecialItem = false;
        groupMonster = new Vector();
        listRegArena = new Vector();
        groupMonster.add(new Vector());
        groupMonster.add(new Vector());
        groupMonster.add(new Vector());

        for (int i = 0; i < 7; i++) {
            ((Vector) groupMonster.get(0)).add(new Vector());
            ((Vector) groupMonster.get(1)).add(new Vector());
            ((Vector) groupMonster.get(2)).add(new Vector());
        }

        int[] idm = new int[]{203, 4362, 4363, 4364, 4365, 4366, 4367};
        idMapMONSTER.add(new Vector());
        idMapMONSTER.add(new Vector());
        idMapMONSTER.add(new Vector());

        for (int i = 0; i < idm.length; i++) {
            ((Vector) idMapMONSTER.get(0)).add(idm[i]);
            ((Vector) idMapMONSTER.get(1)).add(idm[i]);
            ((Vector) idMapMONSTER.get(2)).add(idm[i]);
        }

        nationWar.add(new RegisterAttack());
        nationWar.add(new RegisterAttack());
        nationWar.add(new RegisterAttack());

        for (int i = 0; i < 4; i++) {
            listRegArena.add(new Vector());
        }

        allUser = new int[3];
        allLV = new int[3];
        dayQuaySo = "";
        nPhuongHoang = 0;
        max_phuong_hoang = 0;
        countQuaySo = 0;
        MAX_QUAY = 5000;
        allXuLucky = 0L;
        allLuongLucky = 0L;
        winNumber = -1;
        deltaLVAttackMons = 10;
        teamArena = new Vector();
        dropHoaHong = true;
        totalChoiBan = 0;
        nameTopNapLuong = "";
        maxLuongNap = 0L;
        timeSellChoi = 0L;
        tradePet = 0;
    }

    public static ShopTemplate getShopTemplate(int id) {
        for (int i = 0; i < shop.length; i++) {
            if (shop[i].id == id) {
                return shop[i];
            }
        }

        return null;
    }

    public static int getIndexShopTemplate(int id) {
        for (int i = 0; i < shop.length; i++) {
            if (shop[i].id == id) {
                return i;
            }
        }

        return -1;
    }

    public static void doChangeInfoShopEvent() {
        

        if (Char.isSuKienWordcup2017()) 
        {
            int index = getIndexShopTemplate(148);
            if (index != -1) 
            {
                Map.shop[index] = new ShopTemplate(148, 248, "Quả bóng vàng\n- Vật phẩm sự kiện", "", 1, 50, Map.SELL_LUONG, 0, Char.isSuKienWordcup2017() ? Map.SELL : Map.NOT_SELL, 50);
            }

            index = getIndexShopTemplate(149);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(149, 248, "Quả bóng vàng\n- Vật phẩm sự kiện", "", 1, 5000000, Map.SELL_XU, 0, Char.isSuKienWordcup2017() ? Map.SELL : Map.NOT_SELL, 5000000);
            }
        }

        if (Char.isSuKienHe2017()) 
        {
            int index = getIndexShopTemplate(148);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(148, 64, "Pháo hoa tết\n- Vật phẩm sự kiện", "", 1, 100, Map.SELL_LUONG, 0, Char.isSuKienHe2017() ? Map.SELL : Map.NOT_SELL, 100);
            }
            index = getIndexShopTemplate(149);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(149, 64, "Pháo hoa tết\n- Vật phẩm sự kiện", "", 1, 10000000, Map.SELL_XU, 0, Char.isSuKienHe2017() ? Map.SELL : Map.NOT_SELL, 10000000);
            }
             index = getIndexShopTemplate(135);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(135, 112, "Socola \n - Dùng tặng cho con nít.", "", 10, 25, Map.SELL_XU, 0, Char.isSuKienHe2017() ? Map.SELL : Map.NOT_SELL, 25);
            }
        }

        if (Char.isSuKienHaloween2016()) 
        {
            int index = getIndexShopTemplate(148);
            if (index != -1) 
            {
                Map.shop[index] = new ShopTemplate(148, 35, "Hộp quà\n- Vật phẩm sự kiện", "", 1, 40, Map.SELL_LUONG, 0, Char.isSuKienHaloween2016() ? Map.SELL : Map.NOT_SELL, 40);
            }
            index = getIndexShopTemplate(147);
            if (index != -1) 
            {
                Map.shop[index] = new ShopTemplate(147, 35, "Hộp quà\n- Vật phẩm sự kiện", "", 1, 6000000, Map.SELL_XU, 0, Char.isSuKienHaloween2016() ? Map.SELL : Map.NOT_SELL, 6000000);
            }
        }
        if (Char.isSuKienTet2017())
        {
            int index = getIndexShopTemplate(148);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(148, 64, "Pháo hoa tết\n- Vật phẩm sự kiện", "", 1, 100, Map.SELL_LUONG, 0, Char.isSuKienTet2017() ? Map.SELL : Map.NOT_SELL, 100);
            }
            index = getIndexShopTemplate(149);
            if (index != -1) {
                Map.shop[index] = new ShopTemplate(149, 64, "Pháo hoa tết\n- Vật phẩm sự kiện", "", 1, 10000000, Map.SELL_XU, 0, Char.isSuKienTet2017() ? Map.SELL : Map.NOT_SELL, 10000000);
            }
        }

        // Trung Thu 2026 (sua 19/09/2026): o 148 dung chung nay THIEU nhanh rieng cho Trung
        // Thu du doBuySpecialItem.java (item.id==148) da co san logic ban Hat mam
        // (potions[135]) khi Char.isSuKienTrungThul2016() - vi thieu nhanh nay nen o 148
        // luon bi cac khoi tren/duoi tat mat, khong mua duoc trong thuc te.
        if (Char.isSuKienTrungThul2016()) {
            int index = getIndexShopTemplate(148);
            if (index != -1) {
                // idImg=108 = map/images_potion/108.png, dung icon that cua Hat mam theo
                // nguoi dung xac nhan 19/09/2026 (truoc do tam muon icon 35 cua Halloween).
                Map.shop[index] = new ShopTemplate(148, 108, "Hạt mầm\n- Vật phẩm sự kiện Trung Thu", "", 1, 50, Map.SELL_LUONG, 0, Map.SELL, 50);
            }
        }

        // ---- Khong su kien nao dang bat thi phai TAT hai o dung chung 148/149 ----
        //
        // Bon khoi if o tren chi GHI DE hai o nay khi su kien tuong ung dang bat. Khi tat
        // ca deu tat thi khong khoi nao chay, va hai o giu nguyen dong trong bang
        // data_shop - ma hai dong do de sell=1. Ket qua: "Pháo hoa tết" nam trong gian
        // hang quanh nam du khong co su kien nao lien quan. Client van ve chung ra vi no
        // loc theo co isSell (GameScr.onNPCSellSpecialItem), con doBuySpecialItem thi tu
        // choi ban - nguoi choi thay mot mon "co ma khong mua duoc".
        //
        // Chi dong hai o NAY: chung la vat pham su kien thuan tuy trong data_shop
        // ("Pháo hoa tết - Vật phẩm sự kiện"). Hai o 147 va 135 cung duoc su kien muon
        // nhung dong goc cua chung la "Dây thừng" va "Socola" - do that, khong duoc tu
        // dong tat.
        // 148 con duoc Trung Thu dung rieng (Hat mam) nen tach dieu kien tat rieng voi 149
        // (149 KHONG lien quan Trung Thu, giu nguyen dieu kien cu).
        if (!Char.isSuKienWordcup2017() && !Char.isSuKienHe2017()
                && !Char.isSuKienHaloween2016() && !Char.isSuKienTet2017()
                && !Char.isSuKienTrungThul2016()) {
            ngungBanOSuKien(148);
        }
        if (!Char.isSuKienWordcup2017() && !Char.isSuKienHe2017()
                && !Char.isSuKienHaloween2016() && !Char.isSuKienTet2017()) {
            ngungBanOSuKien(149);
        }

        // ---- Thoi trang bong da: hien trong cua hang khi Vu Lan dang bat ----
        //
        // Hai bo nay la thu DUY NHAT nhan doi ti le roi nhanh hoa hong vang khi danh quai
        // trong ky Vu Lan (Monster.java, nhanh isSuKienVuLan2015 dung
        // wModel.isBongDaNamAoCam/isBongDaNuAoSoc). O gian hang (id 180/181, ten, gia
        // 1000 luong, anh 162/163) DA CO SAN trong data_shop tren VPS TU TRUOC, chi
        // thieu duong noi voi co su kien - khac voi 147/148/149/135 o tren, cho nay
        // KHONG ghi de ten/gia: chi bat/tat co sell, giu nguyen moi thu khac trong DB.
        datOanhToggleSuKien(SHOP_BONG_DA_NAM, Char.isSuKienVuLan2015());
        datOanhToggleSuKien(SHOP_BONG_DA_NU, Char.isSuKienVuLan2015());

        // Trung Thu 2026 (yeu cau nguoi dung 19/09/2026): 4 trang phuc chu de Trung Thu
        // (Hang Nga/Chu Cuoi/Tinh Quan/Tien Nu) va gian mat na Ong Dia moi them - truoc
        // day KHONG co doan nao tu bat/tat sell theo su kien, phai vao DB chinh tay. Ten
        // nhan vat trong ten cac item nay (Hang Nga, Chu Cuoi...) la nhan vat than thoai
        // RIENG cua Trung Thu, an toan de gate theo Char.isSuKienTrungThul2016().
        datOanhToggleSuKien(87, Char.isSuKienTrungThul2016());
        datOanhToggleSuKien(89, Char.isSuKienTrungThul2016());
        datOanhToggleSuKien(174, Char.isSuKienTrungThul2016());
        datOanhToggleSuKien(175, Char.isSuKienTrungThul2016());
    }

    /** O gian hang cua thoi trang Bóng đá nam (item 791), da co san trong data_shop. */
    public static final int SHOP_BONG_DA_NAM = 180;
    /** O gian hang cua thoi trang Bóng đá nữ (item 792), da co san trong data_shop. */
    public static final int SHOP_BONG_DA_NU = 181;
    /** So ngay su dung khi mua thoi trang bong da. */
    public static final int SO_NGAY_BONG_DA = 7;

    /** Dua mot o gian hang ve trang thai KHONG BAN, giu nguyen moi truong khac. */
    private static void ngungBanOSuKien(final int idShop) {
        final int index = getIndexShopTemplate(idShop);
        if (index != -1 && Map.shop[index] != null) {
            Map.shop[index].sell = (byte) Map.NOT_SELL;
        }
    }

    /** Bat/tat co sell cua mot o gian hang co san, khong dung toi ten/gia/anh. */
    private static void datOanhToggleSuKien(final int idShop, final boolean dangBat) {
        final int index = getIndexShopTemplate(idShop);
        if (index != -1 && Map.shop[index] != null) {
            Map.shop[index].sell = (byte) (dangBat ? Map.SELL : Map.NOT_SELL);
        }
    }

    public static void reloadShop() {
        Vector<ShopTemplate> shopList =  Database.instance.loadShopTemplate();
        Map.shop = shopList.toArray(new ShopTemplate[0]);
        doChangeInfoShopEvent(); // Nếu cần cập nhật thông tin shop cho client
        return;
    }

    public static void loadShop() {

        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
        Map.ALL_CHAR_LOI_DAI.add(new Hashtable<>());
    
        // Lấy dữ liệu shop từ database
        Vector<ShopTemplate> shopList = Database.instance.loadShopTemplate();
        Map.shop = shopList.toArray(new ShopTemplate[0]);
    
        doChangeInfoShopEvent();
    }

    public static boolean isSellHuyetLinhThao() {
        String daycheck = Char.getDayTime(0L);
        return daycheck.compareTo("2020-02-25 23:23:59") < 0;
    }

    public boolean isItemLock(int id) {
        return id >= 116 && id <= 128 || id == 150 || id == 176;
    }

    // Than method (71 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadGemTemplateIndo() {
        MapNapTaiNguyen.loadGemTemplateIndo();
    }

    // Than method (400 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadGemTemplate() {
        MapNapTaiNguyen.loadGemTemplate();
    }

    /**
     * Doi chieu so dong bang data_potion voi so o thuoc cua nhan vat, in canh bao ngay
     * luc khoi dong neu lech.
     *
     * Client cap phat o thuoc theo so dong bang nay (goi dang nhap gui
     * potionTemplates2.size() -> Char.CHAR_POTION) roi tra cuu bang chi so ma khong
     * kiem tra bien. Thieu dong la nguoi choi giu thuoc id cao se lam client rong
     * TOAN BO hanh trang — xem Char.getAllPotion(). Bang phai co du id 0..161.
     */
    public static void kiemTraSoDongPotion() {
        final int co = (potionTemplates2 == null) ? 0 : potionTemplates2.size();
        if (co == Char.SO_O_THUOC) {
            return;
        }
        System.out.println("=================================================================");
        System.out.println(" CANH BAO data_potion: dang co " + co + " dong, can "
                + Char.SO_O_THUOC + " dong (id 0.." + (Char.SO_O_THUOC - 1) + ").");
        if (co < Char.SO_O_THUOC) {
            System.out.println(" Nguoi choi giu thuoc id >= " + co + " se thay HANH TRANG RONG"
                    + " (mat ca thuoc, nguyen lieu va vat pham) vi client tra cuu vuot mang.");
            System.out.println(" Vi du: sach ky nang pet la thuoc id "
                    + Potion.TYPE_SACH_SKILL_PET + ".");
        }
        System.out.println(" Sua: chay vps/db_new_server.sql PHAN 2H roi bat lai server.");
        System.out.println("=================================================================");
    }

    /** Gem sach ky nang pet ban cu, da nghi huu - xem Potion.TYPE_SACH_SKILL_PET. */
    public static final short SACH_SKILL_PET_GEM = 269;

    public static void loadPetTemplate() {
        petTemplate = new PetTemplate[]{
            new PetTemplate((byte) 0, "Lồng đèn xếp", 20160, (byte) 0, (byte) 2, (byte) 16, (byte) 16, 1000, (byte) 0, (byte) 5, Pet.TYPE_FLY_1_FRAME),
            new PetTemplate((byte) 1, "Thanh tước", 43200, (byte) 1, (byte) 6, (byte) 10, (byte) 10, 1000, (byte) 0, (byte) 6, Pet.TYPE_FLY_2_FRAME),
            new PetTemplate((byte) 2, "Cánh cam", 43200, (byte) 2, (byte) 6, (byte) 10, (byte) 10, 1000, (byte) 0, (byte) 7, Pet.TYPE_FLY_2_FRAME),
            new PetTemplate((byte) 3, "Dơi quỷ", 10080, (byte) 3, (byte) 6, (byte) 16, (byte) 16, 1000, (byte) 0, (byte) 8, Pet.TYPE_FLY_2_FRAME),
            new PetTemplate((byte) 4, "Tim bay", 10080, (byte) 4, (byte) 2, (byte) 16, (byte) 16, 1000, (byte) 0, (byte) 9, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 5, "Lồng đèn hoa sen", 20160, (byte) 5, (byte) 2, (byte) 14, (byte) 14, 1000, (byte) 0, (byte) 11, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 6, "Lồng đèn ngôi sao", 20160, (byte) 6, (byte) 2, (byte) 14, (byte) 14, 1000, (byte) 0, (byte) 12, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 7, "Pet Hoa tuyết", 20160, (byte) 7, (byte) 4, (byte) 22, (byte) 21, 1000, (byte) 0, (byte) 15, Pet.TYPE_FLY_IMAGE),
            new PetTemplate((byte) 8, "Pet Giáp Ngọ", 20160, (byte) 8, (byte) 9, (byte) 26, (byte) 19, 1000, (byte) 0, (byte) 16, Pet.TYPE_FLY_1_FRAME),
            new PetTemplate((byte) 9, "Pet Armadillo băng", 20160, (byte) 9, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 17, Pet.TYPE_WALK),
            new PetTemplate((byte) 10, "Pet Armadillo sét", 20160, (byte) 10, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 18, Pet.TYPE_WALK),
            new PetTemplate((byte) 11, "Pet Armadillo độc", 20160, (byte) 11, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 19, Pet.TYPE_WALK),
            new PetTemplate((byte) 12, "Lồng đèn cá chép", 20160, (byte) 12, (byte) 2, (byte) 21, (byte) 16, 1000, (byte) 0, (byte) 21, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 13, "Chim cánh cụt", 20160, (byte) 13, (byte) 12, (byte) 17, (byte) 29, 1000, (byte) 0, (byte) 22, Pet.TYPE_WALK),
            new PetTemplate((byte) 14, "Pet ất mùi", 20160, (byte) 14, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 23, Pet.TYPE_WALK),
            new PetTemplate((byte) 15, "Ngôi sao băng", 20160, (byte) 15, (byte) 2, (byte) 14, (byte) 14, 1000, (byte) 0, (byte) 24, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 16, "Phượng hoàng lửa", 20160, (byte) 16, (byte) 4, (byte) 22, (byte) 21, 1000, (byte) 0, (byte) 10, Pet.TYPE_FLY_2_FRAME),
            new PetTemplate((byte) 17, "Phượng hoàng băng", 20160, (byte) 17, (byte) 4, (byte) 22, (byte) 21, 1000, (byte) 0, (byte) 20, Pet.TYPE_FLY_2_FRAME),
            new PetTemplate((byte) 18, "Lồng đèn", 20160, (byte) 16, (byte) 2, (byte) 9, (byte) 19, 1000, (byte) 0, (byte) 25, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 19, "Lồng đèn Ngôi sao", 20160, (byte) 18, (byte) 2, (byte) 15, (byte) 14, 1000, (byte) 0, (byte) 26, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 20, "Lồng đèn Trăng khuyết", 20160, (byte) 19, (byte) 2, (byte) 16, (byte) 16, 1000, (byte) 0, (byte) 27, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 21, "Lồng đèn Rồng xanh", 20160, (byte) 17, (byte) 2, (byte) 40, (byte) 40, 1000, (byte) 0, (byte) 28, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 22, "Khỉ vàng", 20160, (byte) 20, (byte) 12, (byte) 36, (byte) 32, 1000, (byte) 0, (byte) 29, Pet.TYPE_WALK),
            // SAU MAU 23-27 VA 29 MUON ANH CUA MAU CO SAN - xem chu thich dai o Pet.apDungAnhTheoMau().
            //
            // Ban cu ghi idImg=21, nFrame=1, w=1, h=1, TYPE_WALK cho ca sau: toan gia tri GIU CHO,
            // map/pet/21.png chua bao gio ton tai (ke ca fork KPAH-VER3 trong Downloads va lich su
            // git). TYPE_WALK lai dung bang khung cua pet 12 khung (chi so khung toi 9), nen client
            // ve khung 1..9 tren mot anh khai bao 1 khung -> vung ve NGOAI BIEN anh. Client J2ME
            // (MIDP drawRegion) nem IllegalArgumentException -> gay ca vong ve actor cua khung hinh
            // do -> moi actor ve sau pet bien mat, khung sau (idFrame ve 0) hien lai: nhan vat
            // "luc an luc hien" ~1 lan/giay (05/09/2026, 338 pet cua 33 nguoi choi). Client Unity
            // khong nem loi nhung pet vo hinh.
            //
            // Cot 4-7 va cot cuoi (idImg, nFrame, wFrame, hFrame, type_move) CHEP Y NGUYEN tu mau
            // nguon ghi ben phai - do la bo tham so da chay dung tren san pham voi dung tam anh do.
            // Ten, thoi han, gia, idImgPaint (icon) giu nguyen. Khi co anh rieng thi thay o day va
            // xoa cap tuong ung trong KiemMauPetCoAnh.
            //
            // CHON MAU NGUON THE NAO: bang khung cua client (AnimalMove.cs arrFrameStand/arrFrameMove)
            // theo type_move, tinh CA BON HUONG, yeu cau chi so khung toi: type 0 FLY_1_FRAME -> 8,
            // type 1 WALK -> 11, type 3 FLY_2_FRAME -> 5. Nen chi muon sheet co nFrame >= 9 / 12 / 6
            // tuong ung. Phuong hoang 16/17 (FLY_2_FRAME nhung chi 4 khung) KHONG dung: huong 2/3
            // yeu cau khung 4-5 -> lai ngoai bien y nhu loi dang sua.
            new PetTemplate((byte) 23, "Cú", 20160, (byte) 13, (byte) 12, (byte) 17, (byte) 29, 1000, (byte) 0, (byte) 30, Pet.TYPE_WALK),        // <- mau 13 Chim cánh cụt
            new PetTemplate((byte) 24, "Rồng", 20160, (byte) 8, (byte) 9, (byte) 26, (byte) 19, 1000, (byte) 0, (byte) 31, Pet.TYPE_FLY_1_FRAME), // <- mau 8  Pet Giáp Ngọ
            new PetTemplate((byte) 25, "Thỏ bay", 20160, (byte) 14, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 32, Pet.TYPE_WALK),    // <- mau 14 Pet ất mùi
            new PetTemplate((byte) 26, "Dơi", 20160, (byte) 3, (byte) 6, (byte) 16, (byte) 16, 1000, (byte) 0, (byte) 33, Pet.TYPE_FLY_2_FRAME),   // <- mau 3  Dơi quỷ
            new PetTemplate((byte) 27, "Đại bàng", 20160, (byte) 1, (byte) 6, (byte) 10, (byte) 10, 1000, (byte) 0, (byte) 34, Pet.TYPE_FLY_2_FRAME), // <- mau 1  Thanh tước
            new PetTemplate((byte) 28, "Lồng đèn hoa sen", 20160, (byte) 5, (byte) 2, (byte) 14, (byte) 14, 1000, (byte) 0, (byte) 11, Pet.TYPE_LANTERNS),
            new PetTemplate((byte) 29, "Bạch Thử", 20160, (byte) 9, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 46, Pet.TYPE_WALK),    // <- mau 9  Pet Armadillo băng
            // Trung Thu 2026 (moi 19/09/2026, yeu cau nguoi dung): pet Capybara - MUON tam
            // anh/kich thuoc cua mau 9 "Pet Armadillo bang" (giong cach Bach Thu o tren da
            // lam) vi CHUA co anh Capybara that. Se hien HINH GIONG Bach Thu cho toi khi co
            // anh rieng - can sua idImg/nFrame/wFrame/hFrame o day khi co anh that.
            new PetTemplate((byte) 30, "Capybara", 20160, (byte) 9, (byte) 12, (byte) 29, (byte) 30, 1000, (byte) 0, (byte) 47, Pet.TYPE_WALK)
        };
    }

    public static void loadAllTemplate() {
        Map.itemTemplates = Database.instance.loadItemTemplate();
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(0).values());
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(1).values());
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(2).values());
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(3).values());
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(4).values());
        Map.itemTemplateCollection.add(Map.itemTemplates.elementAt(5).values());
        Database.instance.loadMonsterTemplate(Map.monsterTemplates);
        Map.npcReceiveCard.add(new Vector<>());
        Map.npcReceiveCard.add(new Vector<>());
        Map.npcReceiveCard.add(new Vector<>());
        Map.allWedding = Database.instance.loadWedding();
        Map.potionTemplates2 = Database.instance.loadPotionTemplate();
        kiemTraSoDongPotion();
       loadShop();
        Map.loadGemTemplate();
        Map.loadPetTemplate();
        Map.genTrungRuongMayMan();

    }

    public static GemItem dropGemItemBoss(int idGem) {
        GemItem gem = new GemItem(idGem);
        gem.isBoss = true;
        gem.cat = 6;
        return gem;
    }

    public GemItem doCreateGemItemMaterial(int idTemplateMonster) {
        return null;
    }


    public static GemItem dropGemTuBinh(int level) {
        if (level < 40) {
            return null;
        } else {
            int index = 0;
            if (level >= 40 && level < 50) {
                index = 0;
            } else if (level >= 50 && level < 60) {
                index = 1;
            } else if (level >= 60 && level < 70) {
                index = 2;
            } else if (level >= 70 && level < 80) {
                index = 3;
            } else if (level >= 80) {
                index = 3;
            }

            int[][] id = new int[][]{
                {159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174},
                {179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194},
                {195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210},
                {211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226}
            };
            return new GemItem(id[index][r.nextInt(id[index].length)]);
        }
    }

    public static GemItem dropGemItem(int start, int idTpMonster, boolean isboss) {
        int var7 = 12;
        GemItem gem = null;
        if (idTpMonster != 84 && idTpMonster != 90 && !isboss) {
            int type = r.nextInt(5);
            int percent = 0;
            int startIndex = 0;
            switch (type) {
                case 0:
                    int var12 = 12;
                    percent = r.nextInt(1000);
                    if (percent < 5) {
                        var12 = 24;
                    } else if (percent < 20) {
                        var12 = 20;
                    } else if (percent < 50) {
                        var12 = 16;
                    }

                    gem = new GemItem(r.nextInt(4) + var12);
                    break;
                case 1:
                    int var11 = 28;
                    gem = new GemItem(r.nextInt(6) + var11);
                    break;
                case 2:
                    int var10 = 34;
                    percent = r.nextInt(1000);
                    if (percent < 5) {
                        var10 = 52;
                    } else if (percent < 30) {
                        var10 = 46;
                    } else if (percent < 50) {
                        var10 = 40;
                    }

                    gem = new GemItem(r.nextInt(6) + var10);
                    break;
                case 3:
                    gem = new GemItem(r.nextInt(2) == 0 ? 58 : 59);
                    break;
                case 4:
                    gem = new GemItem(229);
            }
        }

        if (gem != null) {
            gem.cat = 6;
        }

        return gem;
    }

    public static String[][][] getNameMapLocation(int idCountry) {
        String[][][] name = new String[nameMap.length][][];

        for (short i = 0; i < nameMap.length; i++) {
            name[i] = nameMap[i];
        }

        if (idCountry == 0) {
            name[1][0] = new String[]{"Dương đông", "Đông Dương đông", "", "", ""};
            name[2][0] = new String[]{"Dương đông", "Đông Dương đông", "", "", ""};
            name[12][0] = new String[]{"Dương đông", "Đông Dương đông", "", "", ""};
            name[13][0] = new String[]{"Dương đông", "Đông Dương đông", "", "", ""};
            if (TeamServer.isServerIndo()) {
                name[1][0] = new String[]{"Duong dong", "Duong dong Timur", "", "", ""};
                name[2][0] = new String[]{"Duong dong", "Duong dong Timur", "", "", ""};
                name[12][0] = new String[]{"Duong dong", "Duong dong Timur", "", "", ""};
                name[13][0] = new String[]{"Duong dong", "Duong dong Timur", "", "", ""};
            }
        } else if (idCountry == 2) {
            name[1][0] = new String[]{"Lâm tây", "Đông Lâm tây", "", "", ""};
            name[2][0] = new String[]{"Lâm tây", "Đông Lâm tây", "", "", ""};
            name[12][0] = new String[]{"Lâm tây", "Đông Lâm tây", "", "", ""};
            name[13][0] = new String[]{"Lâm tây", "Đông Lâm tây", "", "", ""};
            if (TeamServer.isServerIndo()) {
                name[1][0] = new String[]{"Lam Tay", "Lam Tay Timur", "", "", ""};
                name[2][0] = new String[]{"Lam Tay", "Lam Tay Timur", "", "", ""};
                name[12][0] = new String[]{"Lam Tay", "Lam Tay Timur", "", "", ""};
                name[13][0] = new String[]{"Lam Tay", "Lam Tay Timur", "", "", ""};
            }
        } else if (idCountry == 1) {
            name[1][0] = new String[]{"Sơn nam", "Đông Sơn nam", "", "", ""};
            name[2][0] = new String[]{"Sơn nam", "Đông Sơn nam", "", "", ""};
            name[12][0] = new String[]{"Sơn nam", "Đông Sơn nam", "", "", ""};
            name[13][0] = new String[]{"Sơn nam", "Đông Sơn nam", "", "", ""};
            if (TeamServer.isServerIndo()) {
                name[1][0] = new String[]{"Son Nam", "Ðông Son Nam", "", "", ""};
                name[2][0] = new String[]{"Son Nam", "Ðông Son Nam", "", "", ""};
                name[12][0] = new String[]{"Son Nam", "Ðông Son Nam", "", "", ""};
                name[13][0] = new String[]{"Son Nam", "Ðông Son Nam", "", "", ""};
            }
        }

        return name;
    }

    public static short[][][] getLocaltion(int idCountry) {
        short[][][] location = new short[locationMap.length][][];

        for (short i = 0; i < locationMap.length; i++) {
            location[i] = locationMap[i];
        }

        if (idCountry == 0) {
            location[1][0] = new short[]{70, 22, 2, 1701, 22, 2, 1702, 22, 2, 1703, 22, 2, 1704, 22, 2};
            location[2][0] = new short[]{70, 22, 57, 1701, 22, 57, 1702, 22, 57, 1703, 22, 57, 1704, 22, 57};
            location[12][0] = new short[]{70, 3, 34, 1701, 3, 34, 1702, 3, 34, 1703, 3, 34, 1704, 3, 34};
            location[13][0] = new short[]{70, 61, 45, 1701, 61, 45, 1702, 61, 45, 1703, 61, 45, 1704, 61, 45};
        } else if (idCountry == 2) {
            location[1][0] = new short[]{80, 47, 2, 1901, 47, 2, 1902, 47, 2, 1903, 47, 2, 1904, 47, 2};
            location[2][0] = new short[]{80, 34, 57, 1901, 34, 57, 1902, 34, 57, 1903, 34, 57, 1904, 34, 57};
            location[12][0] = new short[]{80, 4, 33, 1901, 4, 33, 1902, 4, 33, 1903, 4, 33, 1904, 4, 33};
            location[13][0] = new short[]{80, 59, 28, 1901, 59, 28, 1902, 59, 28, 1903, 59, 28, 1904, 59, 28};
        } else if (idCountry == 1) {
            location[1][0] = new short[]{0, 17, 2, 301, 17, 2, 302, 17, 2, 303, 17, 2, 304, 17, 2};
            location[2][0] = new short[]{0, 23, 50, 301, 23, 50, 302, 23, 50, 303, 23, 50, 304, 23, 50};
            location[12][0] = new short[]{0, 2, 32, 301, 2, 32, 302, 2, 32, 303, 2, 32, 304, 2, 32};
            location[13][0] = new short[]{0, 58, 30, 301, 58, 30, 302, 58, 30, 303, 58, 30, 304, 58, 30};
        }

        return location;
    }

    // Than method (111 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadAllLocaltionMap() {
        MapNapTaiNguyen.loadAllLocaltionMap();
    }

    // Than method (78 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadImageCharWeaponeData() {
        MapNapTaiNguyen.loadImageCharWeaponeData();
    }

    public static int getPosNameMap(int idm) {
        for (short i = 0; i < mapID.length; i++) {
            for (short j = 0; j < mapID[i].length; j++) {
                if (idm == mapID[i][j]) {
                    return j;
                }
            }
        }

        return -1;
    }

    public static String getNameMap(int idm) {
        for (short i = 0; i < mapID.length; i++) {
            for (short j = 0; j < mapID[i].length; j++) {
                if (idm == mapID[i][j]) {
                    int pos = getPosNameMap(idm);
                    if (pos != -1) {
                        return namMap[i][pos];
                    }
                }
            }
        }

        return "";
    }

    public static short getIndexArrMap(int idm) {
        for (short i = 0; i < mapID.length; i++) {
            for (short j = 0; j < mapID[i].length; j++) {
                if (idm == mapID[i][j]) {
                    return idArrMap[i];
                }
            }
        }

        return -1;
    }

    public short getMapLoad(int mapid) {
        return (short) this.mapIDLoadMap;
    }

    private void loadGate() {
        try {
            if (this.mapId == 201) {
                ((Hashtable) this.monsters.get(0)).remove((short) 1);
                ((Hashtable) this.monsters.get(1)).remove((short) 1);
                ((Hashtable) this.monsters.get(2)).remove((short) 1);

                for (int i = 0; i < 3; i++) {
                    Monster m = new Monster(this, (MonsterTemplate) monsterTemplates.get(83), 1008, 1769, (byte) i);
                    m.id = 0;
                    ((Hashtable) this.monsters.get(i)).put((short) 0, m);
                    m.isDead = true;
                }
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    // Than method (209 dong) da chuyen sang MapVongLap.java. Chu ky GIU NGUYEN.
    public static void startSendInfoAdv() {
        MapVongLap.startSendInfoAdv();
    }

    // Than method (114 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadNpcServerIndo() {
        MapNapTaiNguyen.loadNpcServerIndo();
    }

    // Than method (273 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadNpcServer() {
        MapNapTaiNguyen.loadNpcServer();
    }


    public RegionMapMoba getRegionMoba(int id) {
        return null;
    }

    public RegionLoiDai getRegionLoiDai(int id) {
        return null;
    }

    public RegionNuiChauBau getRegionNuiChauBau(int id) {
        return null;
    }

    public RegionVantienTran getRegionJoinMapVanTienTran(Char p) {
        return null;
    }

    public void removeDynamicMonster(Monster m, int country, int region) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(region)).removeDynamicMonster(m, country);
            } else {
                ((Vector) this.tempRemoveMonster.get(country)).add(m);
            }
        } catch (Exception var5) {
        }
    }

    public void removeMonster(int idMons, int country, int region) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(region)).removeMonster(country, idMons);
            } else {
                ((Hashtable) this.monsters.get(country)).remove((short) idMons);
            }
        } catch (Exception var5) {
        }
    }

    public void addMonsterDynamic(Monster m, int country, int region) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(region)).addMonsterDynamic(m, country);
            } else {
                ((Vector) this.tempMonster.get(country)).add(m);
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public void addMonster(Monster m, int country, int region) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(region)).addMonster(m.id, m, country);
            } else {
                ((Hashtable) this.monsters.get(country)).put(m.id, m);
            }
        } catch (Exception var5) {
        }
    }

    public void addEffectAutoInMap2Char(int country, Char p, int region, int x, int y, int idEff, long time) {
        try {
            Vector<EffectBuff> rv = new Vector();

            for (int i = 0; i < p.ALL_BUFF_INMAP.size(); i++) {
                EffectBuff ef = (EffectBuff) p.ALL_BUFF_INMAP.get(i);
                if (ef.idEff == idEff && !ef.timeOut()) {
                    return;
                }

                if (ef.timeOut()) {
                    rv.add(ef);
                }
            }

            for (int i = 0; i < rv.size(); i++) {
                p.ALL_BUFF_INMAP.remove(rv.get(i));
            }

            EffectBuff eff = new EffectBuff(idEff, time);
            eff.x = (short) (p.x / 16);
            eff.y = (short) (p.y / 16);
            eff.idCountry = p.myCountry;
            eff.mapID = p.mapID;
            eff.idRegion = (byte) p.region;
            eff.type = EffectBuff.BY_MAP;
            p.ALL_BUFF_INMAP.add(eff);
            p.sendEffInMap(eff, p);
        } catch (Exception var12) {
        }
    }

    public void addPlayer(int country, Char p, int region) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(region)).addPlayer(country, p);
            } else {
                ((Vector) this.allPlayers.get(country)).add(p);
            }
        } catch (Exception var5) {
        }
    }

    public void removePlayer(int country, Char p) {
        try {
            if (this.nRegion > 0) {
                ((Region) this.ALL_REGION.get(p.region)).removePlayer(country, p);
            } else {
                ((Vector) this.allPlayers.get(country)).remove(p);
            }
        } catch (Exception var4) {
        }
    }

    public void setPostRegion(short[] p) {
        this.pRegion = p;
    }

    public static void ThreadProcessHoiSinh() {
        new Thread(() -> {
            while (TeamServer.running && !AdminHandler.isStopServer) {
                Vector<Char> cremove = new Vector();

                for (int i = 0; i < Map.ALL_CHAR_HS.size(); i++) {
                    try {
                        Char player = (Char) Map.ALL_CHAR_HS.get(i);
                        if (!player.exit && player.hp <= 0 && player.getSession() != null) {
                            if (player.canHSChiemThanh()) {
                                int[] cost = new int[]{
                                    1000,
                                    2000,
                                    3000,
                                    4000,
                                    5000,
                                    10000,
                                    15000,
                                    20000,
                                    30000,
                                    60000,
                                    70000,
                                    500000,
                                    1000000,
                                    1300000,
                                    1500000,
                                    2000000,
                                    2500000,
                                    3000000,
                                    4500000
                                };
                                int cp = cost[player.lvDetail.lv / 5];
                                if (player.mapID == 112 || player.mapID == 2541 || player.mapID == 2542 || player.mapID == 2543 || player.mapID == 2544) {
                                    cp = 500000;
                                }

                                if (player.mapID == 204 || player.mapID == 205) {
                                    cp = 0;
                                }

                                cremove.add(player);
                                player.subXu((long) cp, true, "map 1");
                                player.hp = player.maxhp;
                                player.mp = player.maxmp;
                                player.desTroy();
                                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                                player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                Database.instance.saveOrtherLog("tob_other_log", player.charname, String.valueOf(cp), "hs");
                            }
                        } else {
                            cremove.add(player);
                        }
                    } catch (Exception var8) {
                    }
                }

                for (int i = 0; i < cremove.size(); i++) {
                    try {
                        Char p = (Char) cremove.get(i);
                        Map.ALL_CHAR_HS.remove(p);
                        Map.HASH_ALL_CHAR_HS.remove(p.charDBID);
                    } catch (Exception var7) {
                    }
                }

                try {
                    Thread.sleep(200L);
                } catch (Exception var6) {
                }
            }
        }).start();
    }

    // Than constructor (476 dong) da chuyen sang real/MapKhoiTao.java.
    public Map(int id, int idXaphu, int magic_physic, int mapload, int nregion) {
        MapKhoiTao.init(this, id,idXaphu,magic_physic,mapload,nregion);
    }

    public void doSendDynamicObj(Char player) {
        try {
            boolean laMapLang = player.mapID == 0 || player.mapID == 301 || player.mapID == 302 || player.mapID == 303 || player.mapID == 304
                    || player.mapID == 70 || player.mapID == 1701 || player.mapID == 1702 || player.mapID == 1703 || player.mapID == 1704
                    || player.mapID == 80 || player.mapID == 1901 || player.mapID == 1902 || player.mapID == 1903 || player.mapID == 1904;
            boolean suKienTrungThu = Char.isSuKienTrungThul2016();

            // Trung Thu: dau lan mua (longden.png, idImg=6 trong imgDynamic) dat o cac map lang,
            // canh vi tri NPC "Long den keo quan" (xem Map.sendInfoNpc / addNPC.tien.nu), lech
            // theo phe nhu cac NPC do. Cac field nay (idImgDynamic/idImg/xDynamic/...) da co san
            // tu truoc nhung chua tung duoc gan o dau ca nen doSendDynamicObj luon la no-op.
            if (laMapLang && suKienTrungThu) {
                byte[] xTheoPhe = {23, 18, 35};
                byte[] yTheoPhe = {38, 32, 44};
                player.map.idImgDynamic = new byte[]{6};
                player.map.idImg = new byte[]{6};
                player.map.xDynamic = new byte[]{xTheoPhe[player.inCountry]};
                player.map.yDynamic = new byte[]{yTheoPhe[player.inCountry]};
                player.map.limX = new byte[]{0};
                player.map.limY = new byte[]{0};
                player.map.nFrame = new byte[]{6};
            } else {
                player.map.idImgDynamic = null;
            }

            if (player.map.idImgDynamic != null) {
                Message m = new Message(-43);
                m.dos.writeByte(player.map.idImgDynamic.length);

                for (int i = 0; i < player.map.idImgDynamic.length; i++) {
                    m.dos.writeInt(imgDynamic[player.map.idImgDynamic[i]].length);
                    m.dos.write(imgDynamic[player.map.idImgDynamic[i]]);
                }

                m.dos.writeByte(player.map.xDynamic.length);

                for (int i = 0; i < player.map.xDynamic.length; i++) {
                    m.dos.writeByte(player.map.idImg[i]);
                    m.dos.writeByte(player.map.xDynamic[i]);
                    m.dos.writeByte(player.map.yDynamic[i]);
                    m.dos.writeByte(player.map.limX[i]);
                    m.dos.writeByte(player.map.limY[i]);
                    m.dos.writeByte(player.map.nFrame[i]);
                }

                player.sendMessage(m);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            System.out.println("LOI GUI THONG TIN DYNAMIC OBJ");
        }
    }

    public static void addCharKiller(Char p, int incountry) {
        if (!((Vector) listKillPerson.get(incountry)).contains(p.charname)) {
            ((Vector) listKillPerson.get(incountry)).add(p.charname);
        }
    }

    public static Vector<String> getCharKiller(int incountry) {
        return (Vector<String>) listKillPerson.get(incountry);
    }

    public static void loadClanPropertyTown() {
        try {
            idClanTown = Database.instance.getIDClanProTown();
        } catch (Exception var1) {
        }
    }

    public Map(int id) {
        // PHAI khoi tao TRUOC new Thread(this).start(). Thread do chay run() -> update(),
        // va no bat dau chay NGAY khi ham dung nay ket thuc — tuc la truoc khi than ham
        // dung cua lop con (OfflineMap) chay xong. Khoi tao o lop con thi van con mot khe
        // hep ma thread da doc trong khi cac kho con rong.
        //
        // Ham dung MOT THAM SO nay chi co DUY NHAT OfflineMap dung (qua super(id)), nen
        // do day ca hai ho kho o day la khep kin, khong dong toi cac loai ban do khac.
        this.initVatTheDuoiDat();
        this.initKhoQuaiVaNguoi();
        this.mapId = id;
        if (this.mapId != -1) {
            this.loadMap("map/map" + id, 0);
        }

        new Thread(this).start();
    }

    public Map() {
    }

    public static int random(int n) {
        Random rnd = new Random(System.currentTimeMillis());
        return Math.abs(rnd.nextInt(n));
    }

    public static int ranDom(int min, int max) {
        return min + r.nextInt(max - min);
    }

    public static boolean canCreateBossCopy(int idTemplate, int incountry) {
        // Phai loc theo CA HAI idTemplate va incountry - ban cu chi so idTemplate tren danh
        // sach bossCopy DUNG CHUNG toan server (khong theo tung nuoc), nen ngay khi MOT nuoc
        // tao ban sao thanh cong, canCreateBossCopy tra ve false cho CA 2 nuoc con lai dang
        // cung danh boss cung idTemplate do, tuoc mat co hoi tao ban sao rieng cua ho.
        for (int i = 0; i < bossCopy.size(); i++) {
            Boss b = (Boss) bossCopy.get(i);
            if (b.idTemplate == idTemplate && b.getInCountry() == incountry) {
                return false;
            }
        }

        return true;
    }

    public void createBossCopy(int idTemplate, int incountry, int x, int y) {
        Boss bo = null;
        switch (idTemplate) {
            case 91:
                bo = new BossLinhXa(this, (MonsterTemplate) monsterTemplates.get(91), x, y, incountry);
                break;
            case 92:
                bo = new BossBachXa(this, (MonsterTemplate) monsterTemplates.get(92), x, y, incountry);
                break;
            case 93:
                bo = new BossMatQuy(this, (MonsterTemplate) monsterTemplates.get(93), x, y, incountry);
                break;
            case 94:
                bo = new BossSkelonton(this, (MonsterTemplate) monsterTemplates.get(94), x, y, incountry);
        }

        if (bo != null) {
            bo.id = RealController.intance.idGen.getID(1, "new monster");
            byte[] he = new byte[]{0, 1, 2, 3, 4};
            bo.isCopy = true;
            bo.isDead = false;
            bo.timeLive = System.currentTimeMillis() + 10000L;
            bo.he = he[r.nextInt(5)];
            bo.level = bo.getMonsterTemplate().level;
            bo.attack /= 2;
            bo.defend_physic /= 2;
            bo.hp /= 2;
            bo.maxhp /= 2;
            bo.maxxp /= 2;
            ((Vector) this.tempMonster.get(incountry)).add(bo);
            bossCopy.add(bo);
        }
    }

    // Than method (95 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadBoss(int type) {
        MapNapTaiNguyen.loadBoss(type);
    }

    public static String getNameBossApear(int country) {
        if (country < 0) {
            return "";
        } else {
            String info = "";

            for (int i = 0; i < boss.size(); i++) {
                Boss b = (Boss) boss.get(i);
                if (b.isOpen && !b.isDead) {
                    info = info + b.getName() + " , ";
                }
            }

            for (int ix = 0; ix < bossTime.size(); ix++) {
                Boss b = (Boss) bossTime.get(ix);
                if (b.isOpen && !b.isDead) {
                    info = info + b.getName() + " , ";
                }
            }

            if (!info.equals("")) {
                info = info.substring(0, info.length() - 1);
            }

            return info;
        }
    }

    public static void createLocationGate(int type) {
        int pos = 0;
        int idMap = 0;
        int rd = 0;
        if (type == 0) {
            int[] mapID = new int[]{2, 3, 6, 5};
            int[][] var10000 = new int[][]{{106, 7}, {91, 54}, {5, 6}, {10, 53}};
            pos = r.nextInt(mapID.length);
            idMap = mapID[pos];
            rd = r.nextInt(5);
            if (rd > 0) {
                idMap = idMap * 20 + 300 + rd;
            }

            removeBossLocation(0);
            System.out.println(mapID[pos] + " MAP BOSS THUONGLUONG " + idMap + " >> " + rd);
        } else if (type == 1) {
            int[] mapID = new int[]{1, 1, 1, 1};
            int[][] var13 = new int[][]{{72, 72}, {72, 72}, {72, 72}, {72, 72}};
            pos = r.nextInt(mapID.length);
            idMap = mapID[pos];
            rd = r.nextInt(5);
            if (rd > 0) {
                idMap = idMap * 20 + 300 + rd;
            }

            removeBossLocation(1);
            System.out.println(mapID[pos] + " MAP BOSS THANLAN " + idMap + " >> " + rd);
        }
    }

    public static int getMax(Vector<String> cord) {
        int n = -10;

        for (int i = 0; i < cord.size(); i++) {
            int vl = Integer.parseInt((String) cord.elementAt(i));
            if (n < vl) {
                n = vl;
            }
        }

        return n;
    }

    public static int getMin(Vector<String> cord) {
        int n = 10000;

        for (int i = 0; i < cord.size(); i++) {
            int vl = Integer.parseInt((String) cord.elementAt(i));
            if (n > vl) {
                n = vl;
            }
        }

        return n;
    }

    // Than method (75 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public void loadTileMap(InputStream iss, DataInputStream is, int loadmap) {
        MapNapTaiNguyen.loadTileMap(this, iss,is,loadmap);
    }

    // Than method (163 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public void loadMap(String filename, int magic_physic) {
        MapNapTaiNguyen.loadMap(this, filename,magic_physic);
    }

    // Than method (50 dong) da chuyen sang MapNapTaiNguyen.java. Chu ky GIU NGUYEN.
    public static void loadGhost() {
        MapNapTaiNguyen.loadGhost();
    }

    public static void loadBossSonTinhThuyTinh() {
        if (Char.isSuKienGioTo2016()) {
            try {
                int[] mapct = new int[]{2, 1};
                int[][] xxx = new int[][]{{45}, {53}};
                int[][] yyy = new int[][]{{11}, {19}};
                int[] var10000 = new int[]{10, 10};
                int[] isboss = new int[]{130, 129};

                for (int i = 0; i < mapct.length; i++) {
                    int rd = 0;
                    int c = 0;
                    int mapid = rd == 0 ? mapct[i] : mapct[i] * 20 + 300 + rd;
                    System.out.println("MAP GHOST " + mapid + " >> " + mapct[i]);
                    Map m = (Map) RealController.mapList.get(mapid);

                    for (int j = 0; j < xxx[i].length; j++) {
                        for (int k = 0; k < MAX_COUNTRY; k++) {
                            BossSonTinhThuyTinh ct = new BossSonTinhThuyTinh(
                                    m, (MonsterTemplate) monsterTemplates.get(isboss[i]), xxx[i][j] * 16, yyy[i][j] * 16, (byte) k
                            );
                            if (TeamServer.isServerHoaLu()) {
                                ct.setInfo(10, 1000000, 1000000);
                                ct.maxHit = 200;
                            } else if (TeamServer.isServerLocal()) {
                                ct.setInfo(10, 10000, 1000000);
                                ct.maxHit = 10;
                            } else {
                                ct.setInfo(10, 1000000, 1000000);
                                ct.maxHit = 500;
                            }

                            ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                            ct.attack = 800;
                            ct.defend_physic = 3000;
                            ct.defend_magic = 3000;
                            ct.isDead = false;
                            ct.bornTime = System.currentTimeMillis() + 10800000L;
                            byte[] he = new byte[]{0, 1, 2, 3, 4};
                            ct.he = he[r.nextInt(5)];
                            m.addMonster(ct, k, 0);
                        }
                    }
                }
            } catch (Exception var14) {
                var14.printStackTrace();
            }
        }
    }

    public void sendDynamicEff(Char p) {
        Vector<EffectBuff> remove = new Vector();
        Vector<EffectBuff> remove1 = new Vector();

        for (int i = 0; i < p.ALL_BUFF_INMAP.size(); i++) {
            EffectBuff ef = (EffectBuff) p.ALL_BUFF_INMAP.get(i);
            if (!ef.timeOut() && ef.idCountry == p.myCountry && ef.idRegion == p.region && ef.mapID == p.mapID) {
                p.sendEffInMap(ef, p);
            } else if (ef.timeOut()) {
                remove.add(ef);
            }
        }

        if (this.ALL_EFFECT_INMAP.size() > 0) {
            for (int ix = 0; ix < ((Vector) this.ALL_EFFECT_INMAP.get(p.inCountry)).size(); ix++) {
                EffectBuff ef = (EffectBuff) ((Vector) this.ALL_EFFECT_INMAP.get(p.inCountry)).get(ix);
                if (!ef.timeOut() && ef.idCountry == p.inCountry && ef.mapID == p.mapID) {
                    try {
                        p.sendMessage(
                                MessageCreator.createMsgAddDynamicNewEffect(ef.idEff, ef.timeExist, ef.type, 32002, ef.x, ef.y, 127, ef.getODER_PAINT(), ef.damFly)
                        );
                    } catch (Exception var7) {
                    }
                } else if (ef.timeOut()) {
                    remove1.add(ef);
                }
            }

            for (int ixx = 0; ixx < remove1.size(); ixx++) {
                ((Vector) this.ALL_EFFECT_INMAP.get(p.inCountry)).remove(remove1.get(ixx));
            }
        }

        for (int ixx = 0; ixx < remove.size(); ixx++) {
            p.ALL_BUFF_INMAP.remove(remove.get(ixx));
        }
    }

    public void playerJoin(Char player) {
        if (this.nRegion > 0) {
            if (player.region >= this.ALL_REGION.size()) {
                player.region = 0;
            }

            ((Region) this.ALL_REGION.get(player.region)).playerJoin(player);
        } else {
            ((Vector) this.allPlayers.get(player.inCountry)).add(player);
            player.region = 0;
        }

        player.nNpc = 0;
        player.map = this;
        player.mapID = this.mapId;
        if (getTown[player.inCountry] && this.mapId == idMapTown) {
            try {
                String charname = "";
                String nameNPC = "";

                for (int i = 0; i < 3; i++) {
                    NpcReceiveCard npc = (NpcReceiveCard) ((Vector) npcReceiveCard.get(player.inCountry)).get(i);
                    if (npc.charGive != null) {
                        if (charname.equals("")) {
                            charname = "Bang " + getClaninfoByID(npc.charGive.idClan).name;
                            nameNPC = NpcReceiveCard.npc[npc.posNpc];
                        } else {
                            charname = charname + " và Bang " + getClaninfoByID(npc.charGive.idClan).name;
                            nameNPC = nameNPC + " và " + NpcReceiveCard.npc[npc.posNpc];
                        }
                    }
                }

                if (!charname.equals("")) {
                    player.sendMessage(MessageCreator.createServerAlertAutoOffMessage(charname.toUpperCase() + " đang giao thẻ tại " + nameNPC));
                    player.sendMessage(MessageCreator.createMsgStartGetTown(player.inCountry));
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        if (this.mapId == idMapTown && idClanTown[player.inCountry] > -1) {
            try {
                NewClan cl = NewClan.getClan(idClanTown[player.inCountry]);
                if (cl != null) {
                    player.sendMessage(
                            MessageCreator.createServerAlertAutoOffMessage(
                                    cl.name + " đang sở hữu thành Trấn Danh. Mức thuế hiện tại là " + taxOfClan[player.inCountry] + "%"
                            )
                    );
                }
            } catch (Exception var6) {
            }
        }
    }

    public void doStartArena() {
    }

    public void doStart(Char p, int type) {
    }

    public void doAddMonster(Char p) {
    }

    public Char getPlayerByID(short id) {
        return CharManager.instance.getByCharID(id);
    }

    public static void writePlayerOut(Message m, int id) {
        try {
            m.dos.writeShort(id);
        } catch (Exception var3) {
        }
    }

    public void playerExit(Char player) {
        // Roi ban do (cong dich chuyen, xa phu, than hanh phu, bi kick...) thi giao dich dang mo
        // phai huy. Truoc day chi move2Map goi checkTrade, con doChangeMap (di qua cong) thi
        // KHONG: hai ben giao dich tiep tren hai ban do khac nhau = hai luong xu ly goi 66 khac
        // nhau, buoc hoan tat chay song song va thuoc duoc cong hai lan. Xem Char.KHOA_GIAO_DICH.
        try {
            if (player.userTrade.size() > 0) {
                player.huyGiaoDich(true, true);
            }
        } catch (Exception e) {
        }
        Message m = new Message(8);

        try {
            m.dos.writeShort(player.id);
        } catch (IOException var8) {
        }

        boolean givecardFail = giveCardFail(player);
        if (getTown[player.inCountry] && this.mapId == idMapTown && givecardFail) {
            try {
                this.sendAllPlayer(MessageCreator.createMsgStartGetTown(player.inCountry), player.inCountry);
            } catch (Exception var7) {
            }
        }

        int kk = 1;
        if (kk != -1) {
            try {
                this.removePLayer(player);
                this.removePlayer(player.inCountry, player);
                player.sendToNearPlayer(m);
            } catch (Exception var6) {
                var6.printStackTrace();
                System.out.println("LOI REMOVE PLAYER KHOI MAP");
            }
        }

        m.cleanup();
    }

    public static void writeActorPos(Message m, Actor a, byte oldVersion) {
        try {
            m.dos.writeByte(a.cat);
            if (a.cat == 4 || a.cat == 14) {
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
        } catch (IOException var4) {
            var4.printStackTrace();
            System.err.println("LOI GUI ACTOR POS");
        }
    }

    public void charMonsterDissapear(Char p) {
        int country = p.myCountry;
        if (!openLog) {
            country = 0;
        }

        this.nCharBeKilled[country] = 0;
        p.isMonster = false;
        this.haveCharMonter[country] = false;
        this.timeMonster[country] = System.currentTimeMillis();
        this.timeLiveMonster[country] = 0L;

        try {
            p.sendMessage(MessageCreator.createMsgInfoChar2Monster(p, false, 0, "charMonsterDissapear map"));
            p.sendToNearPlayer(MessageCreator.createMsgInfoChar2Monster(p, false, 0, "charMonsterDissapear map2"));
        } catch (Exception var4) {
        }
    }

    public void charNguoiTuyetDissapear(Char p) {
        int country = p.myCountry;
        p.idNgtuyet = -1;

        try {
            p.sendMessage(MessageCreator.createMsgInfoChar2Monster(p, false, 0, "charMonsterDissapear map3"));
            p.sendToNearPlayer(MessageCreator.createMsgInfoChar2Monster(p, false, 0, "charMonsterDissapear map4"));
        } catch (Exception var4) {
        }
    }

    public boolean timeOutNationWar(int country) {
        return false;
    }

    public void removeCharOutEventMonster(Char p) {
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < ((Vector) groupMonster.get(j)).size(); i++) {
                Vector<Short> v = (Vector<Short>) ((Vector) groupMonster.get(j)).get(i);
                v.removeElement(p.id);
            }
        }
    }

    public void openX2Country() {
        Calendar cl = Calendar.getInstance();
        int ihour = cl.get(11);
        int minute = cl.get(12);
        if ((ihour == 21 || ihour == 9) && minute < 5) {
            for (int i = 0; i < 3; i++) {
                RegisterAttack reg = (RegisterAttack) nationWar.get(i);
                if (ihour == 21) {
                    reg.addGif(i);
                }

                if (reg.timeX2 > 0) {
                    reg.openX2(i, ihour);
                }
            }
        }
    }

    static String getLogVantieu(int type) {
        String result = "";
        if (type == 0) {
            result = RECEIVE_TIEU[0] + "," + RECEIVE_TIEU[1] + "," + RECEIVE_TIEU[2] + "," + RECEIVE_TIEU[3] + "," + RECEIVE_TIEU[4] + "," + RECEIVE_TIEU[5];
            RECEIVE_TIEU = new long[6];
        } else if (type == 1) {
            result = CHANGE_TIEU[0] + "," + CHANGE_TIEU[1] + "," + CHANGE_TIEU[2] + "," + CHANGE_TIEU[3] + "," + CHANGE_TIEU[4] + "," + CHANGE_TIEU[5];
            CHANGE_TIEU = new long[6];
        } else if (type == 2) {
            result = CARD_TIEU[0] + "," + CARD_TIEU[1];
            CARD_TIEU = new long[2];
        } else if (type == 3) {
            result = FINISH_TIEU[0] + "," + FINISH_TIEU[1] + "," + FINISH_TIEU[2] + "," + FINISH_TIEU[3] + "," + FINISH_TIEU[4] + "," + FINISH_TIEU[5];
            FINISH_TIEU = new long[6];
        } else if (type == 4) {
            result = THIEFT_TIEU[0] + "," + THIEFT_TIEU[1] + "," + THIEFT_TIEU[2] + "," + THIEFT_TIEU[3] + "," + THIEFT_TIEU[4] + "," + THIEFT_TIEU[5];
            THIEFT_TIEU = new long[6];
        }

        return result;
    }

    // Than method (306 dong) da chuyen sang MapVongLap.java. Chu ky GIU NGUYEN.
    public void update() {
        MapVongLap.update(this);
    }

    public static void removeBossTime(Boss b) {
        try {
            bossTime.remove(b);
        } catch (Exception var2) {
        }
    }

    // Than method (240 dong) da chuyen sang MapVongLap.java. Chu ky GIU NGUYEN.
    public void updateWind() {
        MapVongLap.updateWind(this);
    }

    // Than method (229 dong) da chuyen sang MapVongLap.java. Chu ky GIU NGUYEN.
    public void updateSand() {
        MapVongLap.updateSand(this);
    }

    public void deletePotionAndItemOnGround(int country) {
        long now = System.currentTimeMillis();
        Vector<Potion> potions = (Vector<Potion>) this.potions.get(country);

        try {
            for (int i = 0; i < potions.size(); i++) {
                if (i < potions.size()) {
                    Potion p = (Potion) potions.elementAt(i);
                    if (p.belongUser != 0 && now - p.time_drop > 10000L) {
                        p.belongUser = 0;
                    }

                    if (now - p.time_drop > (long) (p.getType() < 10 ? 20000 : '\uea60')) {
                        potions.remove(p);
                    }
                }
            }
        } catch (Exception var17) {
        }

        try {
            Vector<ItemQuest> itemquest = (Vector<ItemQuest>) this.itemquests.get(country);

            for (int ix = 0; ix < itemquest.size(); ix++) {
                if (ix < itemquest.size()) {
                    ItemQuest px = (ItemQuest) itemquest.elementAt(ix);
                    if (px.belongUser != 0 && now - px.time_drop > 10000L) {
                        px.belongUser = 0;
                    }

                    if (now - px.time_drop > 60000L) {
                        itemquest.remove(px);
                    }
                }
            }
        } catch (Exception var16) {
        }

        Vector<GemItem> gemItem = (Vector<GemItem>) this.gemItem.get(country);
        int size = gemItem.size() - 1;

        try {
            for (int ixx = size; ixx >= 0; ixx--) {
                try {
                    GemItem item = (GemItem) gemItem.elementAt(ixx);
                    if (now - item.time_drop > 5000L && item.belongUser != 0 && this.mapId != idMapMateria) {
                        item.belongUser = 0;
                    }

                    if (now - item.time_drop > 30000L) {
                        gemItem.remove(item);
                    }
                } catch (Exception var12) {
                }
            }
        } catch (Exception var15) {
        }

        try {
            size = this.spItem.size() - 1;

            for (int ixx = size; ixx >= 0; ixx--) {
                try {
                    ShopTemplate itemx = (ShopTemplate) this.spItem.elementAt(ixx);
                    if (now - itemx.time_drop > 5000L && itemx.belongUser != 0) {
                        itemx.belongUser = 0;
                    }

                    if (now - itemx.time_drop > 20000L) {
                        this.spItem.remove(itemx);
                        RealController.intance.idGen.putID(itemx.realId, 7, "delete item timeout in map");
                    }
                } catch (Exception var11) {
                }
            }
        } catch (Exception var14) {
        }

        try {
            Vector<Item> items = (Vector<Item>) this.items.get(country);
            size = items.size() - 1;

            for (int ixx = size; ixx >= 0; ixx--) {
                try {
                    Item itemxx = (Item) items.elementAt(ixx);
                    if (now - itemxx.time_drop > 5000L && itemxx.belongUser != 0 && !itemxx.isRuongVanTieu()) {
                        itemxx.belongUser = 0;
                    }

                    if (now - itemxx.time_drop > 20000L) {
                        items.remove(itemxx);
                        RealController.intance.idGen.putID(itemxx.id, 3, "delete item timeout in map");
                    }
                } catch (Exception var10) {
                }
            }
        } catch (Exception var13) {
        }
    }

    public static boolean near(Actor p1, Actor p2) {
        return p1.map == p2.map && Math.abs(p1.x - p2.x) < 200 && Math.abs(p1.y - p2.y) < 200;
    }

    public void killAllMonster(Vector<Hashtable<Short, Monster>> monsters, int country, int region) {
        if (this.nRegion > 0) {
            ((Region) this.ALL_REGION.get(region)).killAllMonster(monsters, country);
        } else {
            short i = (short) country;
            Vector<Char> players = (Vector<Char>) this.allPlayers.get(i);
            if (players.size() > 0) {
                for (int j = 0; j < players.size(); j++) {
                    try {
                        if (((Char) players.get(j)).isBot == -1) {
                            this.move2Map(
                                    (Char) players.get(j),
                                    ((Char) players.get(j)).x / 16,
                                    ((Char) players.get(j)).y / 16,
                                    ((Char) players.get(j)).mapID,
                                    ((Char) players.get(j)).inCountry
                            );
                        }
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
            }
        }
    }

    public void onOffSkelonton(int region, boolean onoff) {
        if (this.nRegion > 0) {
            ((Region) this.ALL_REGION.get(region)).onOffSkelonton(onoff);
        } else {
            this.isSkelonton = onoff;
        }
    }

    public static Monster_Clone doCloneMonster(Monster mt) {
        Monster_Clone mons = new Monster_Clone((byte) 1);
        mons.map = mt.map;
        mons.hp = mt.hp;
        mons.maxhp = mt.maxhp;
        mons.xp = mt.xp;
        mons.xpClone = mt.getMonsterTemplate().rcvXp * 2;
        mons.maxxp = mt.getMonsterTemplate().rcvXp;
        mons.default_x = mt.default_x;
        mons.default_y = mt.default_y;
        mons.defend_magic = mt.defend_magic;
        mons.defend_physic = mt.defend_physic;
        mons.attack = mt.attack;
        mons.attackDelay = mt.attackDelay;
        mons.idTemplate = 114;
        mons.he = mt.he;
        mons.inCountry = mt.inCountry;
        mons.x = mt.x;
        mons.y = mt.y;
        mons.toX = mt.toX;
        mons.toY = mt.toY;
        mons.accurate = mt.accurate;
        mons.dodge = mt.dodge;
        mons.bornTime = mt.bornTime;
        mons.dmove = mt.dmove;
        mons.moveDelay = mt.moveDelay;
        mons.id = mt.id;
        mons.isActive = mt.isActive();
        mons.region = mt.region;
        mons.typeAttack = mt.typeAttack;
        mons.goldRcv = mt.getGoldRcv();
        mons.setType(mons.getMonsterTemplate().type);
        mons.level = mt.level;
        if (mons.level >= 70) {
            mons.defend_magic = mt.defend_magic - mt.defend_magic / 3;
            mons.defend_physic = mt.defend_physic - mt.defend_physic / 3;
        }

        return mons;
    }

    public void reSetAllMonster(int country, int region) {
        if (this.nRegion > 0) {
            ((Region) this.ALL_REGION.get(region)).reSetAllMonster(country);
        } else {
            synchronized (this.monsters) {
                Vector<Hashtable<Short, Monster>> monsters = this.monsters;
                short i = (short) country;
                Collection<Monster> listmonster = ((Hashtable) monsters.get(i)).values();
                ((Hashtable) this.temp_monsters.get(i)).clear();

                for (Monster mt : listmonster) {
                    if (!mt.isBoss) {
                        if (random(100) < 50) {
                            Monster_Clone mons = new Monster_Clone((byte) 1);
                            mons.map = mt.map;
                            mons.hp = mt.hp;
                            mons.maxhp = mt.maxhp;
                            mons.xp = mt.xp;
                            mons.xpClone = mt.getMonsterTemplate().rcvXp;
                            mons.maxxp = mt.getMonsterTemplate().rcvXp;
                            mons.default_x = mt.default_x;
                            mons.default_y = mt.default_y;
                            mons.defend_magic = mt.defend_magic;
                            mons.defend_physic = mt.defend_physic;
                            mons.attack = mt.attack;
                            mons.attackDelay = mt.attackDelay;
                            mons.idTemplate = 114;
                            mons.he = mt.he;
                            mons.inCountry = mt.inCountry;
                            mons.x = mt.x;
                            mons.y = mt.y;
                            mons.toX = mt.toX;
                            mons.toY = mt.toY;
                            mons.accurate = mt.accurate;
                            mons.dodge = mt.dodge;
                            mons.bornTime = mt.bornTime;
                            mons.dmove = mt.dmove;
                            mons.moveDelay = mt.moveDelay;
                            mons.id = mt.id;
                            mons.setType(mons.getMonsterTemplate().type);
                            mons.level = mt.level;
                            ((Hashtable) this.temp_monsters.get(i)).put(mons.id, mons);
                        } else {
                            ((Hashtable) this.temp_monsters.get(i)).put(mt.id, mt);
                        }
                    } else {
                        ((Hashtable) this.temp_monsters.get(i)).put(mt.id, mt);
                    }
                }
            }
        }
    }

    public Monster getMonster(short id, int country, int region) {
        if (this.nRegion > 0) {
            return ((Region) this.ALL_REGION.get(region)).getMonster(id, country);
        } else {
            try {
                Monster m = (Monster) ((Hashtable) this.monsters.get(country)).get(id);
                if (this.isSkelonton) {
                    m = (Monster) ((Hashtable) this.temp_monsters.get(country)).get(id);
                }

                if (m == null) {
                    Monster mons = (Monster) MonsterVantieu.HAS_ALL_MONSTER_VANTIEU.get(id);
                    if (mons != null) {
                        return mons;
                    }
                }

                return m;
            } catch (Exception var6) {
                System.out.print(" " + this.mapIDLoadMap + " ");
                return null;
            }
        }
    }

    protected void addPotion(Potion pt, int country) {
        ((Vector) this.potions.get(country)).add(pt);
    }

    protected void addItemQuest(ItemQuest pt, int country) {
        ((Vector) this.itemquests.get(country)).add(pt);
    }

    protected void addItem(Item pt, int country) {
        ((Vector) this.items.get(country)).add(pt);
    }

    protected void addGemItem(GemItem pt, int country) {
        ((Vector) this.gemItem.get(country)).add(pt);
    }

    // ==================== CHIEM VAT ROI (nguyen tu) ====================
    //
    // Vector.remove(Object) la NGUYEN TU va tra ve true CHI cho luong that su rut duoc phan
    // tu ra. Cac duong nhat cu deu theo mau "tim thay tren dat -> remove -> cap thuong", ma
    // hai buoc dau khong nguyen tu voi nhau: goi 18/19/-41/-65 nam trong privateCmd nen chay
    // tren LUONG PHIEN, con vong tu dong nhat (CharVongLap.update, idItem/idPotion/idGem/
    // idItemQuest do Monster day vao khi quai chet) chay tren LUONG BAN DO. Hai luong cung
    // thay mot vat -> ca hai remove (lan hai la no-op, ket qua bi bo qua) -> CA HAI cap
    // thuong: mot vat roi thanh hai. Sau 5-10 giay belongUser ve 0 (deletePotionAndItemOnGround)
    // nen hai NGUOI CHOI khac nhau cung dua duoc.
    //
    // Bon ham duoi day tra ve true = "toi da chiem duoc, duoc phep cap thuong". Moi duong nhat
    // phai dung chung mau: if (!chiemX(...)) return;  TRUOC khi cong vao tui.
    // Giu nguyen bon ham remove* (void) ben duoi: chung con duoc goi tu cac lop con nam trong
    // lib/NQSH_5h.jar - doi chu ky la doi descriptor, lop trong jar se khong con ghi de dung.
    protected boolean chiemGem(GemItem pt, int country) {
        if (pt == null || country < 0 || country >= this.gemItem.size()) {
            return false;
        }
        return ((Vector) this.gemItem.get(country)).remove(pt);
    }

    protected boolean chiemPotion(Potion pt, int country) {
        if (pt == null || country < 0 || country >= this.potions.size()) {
            return false;
        }
        return ((Vector) this.potions.get(country)).remove(pt);
    }

    protected boolean chiemItem(Item pt, int country) {
        if (pt == null || country < 0 || country >= this.items.size()) {
            return false;
        }
        return ((Vector) this.items.get(country)).remove(pt);
    }

    protected boolean chiemItemQuest(ItemQuest pt, int country) {
        if (pt == null || country < 0 || country >= this.itemquests.size()) {
            return false;
        }
        return ((Vector) this.itemquests.get(country)).remove(pt);
    }

    protected void removeGem(GemItem pt, int country) {
        ((Vector) this.gemItem.get(country)).remove(pt);
    }

    protected void removePotion(Potion pt, int country) {
        ((Vector) this.potions.get(country)).remove(pt);
    }

    protected void removeItemQuest(ItemQuest pt, int country) {
        ((Vector) this.itemquests.get(country)).remove(pt);
    }

    protected void removeItem(Item pt, int country) {
        ((Vector) this.items.get(country)).remove(pt);
    }

    protected ItemQuest getItemQuest(short id, int country) {
        // Hai loi trong dieu kien cu `country > this.potions.size()`:
        //   1. do nham kho khac (potions) trong khi lay tren itemquests
        //   2. lech mot don vi: country == size() van lot qua roi nem ArrayIndexOutOfBounds
        if (country < 0 || country >= this.itemquests.size()) {
            return null;
        } else {
            Vector<ItemQuest> potions = (Vector<ItemQuest>) this.itemquests.get(country);

            for (int i = 0; i < potions.size(); i++) {
                try {
                    if (((ItemQuest) potions.get(i)).id == id) {
                        return (ItemQuest) potions.get(i);
                    }
                } catch (Exception var6) {
                    break;
                }
            }

            return null;
        }
    }

    protected Potion getPotion(short id, int country) {
        // `>` la lech mot don vi: country == size() van lot qua roi nem ArrayIndexOutOfBounds.
        if (country < 0 || country >= this.potions.size()) {
            return null;
        } else {
            Vector<Potion> potions = (Vector<Potion>) this.potions.get(country);

            for (int i = 0; i < potions.size(); i++) {
                try {
                    if (((Potion) potions.get(i)).id == id) {
                        return (Potion) potions.get(i);
                    }
                } catch (Exception var6) {
                    break;
                }
            }

            return null;
        }
    }

    protected GemItem getGem(short id, int country) {
        Vector<GemItem> gemItem = (Vector<GemItem>) this.gemItem.get(country);

        for (int i = 0; i < gemItem.size(); i++) {
            try {
                if (((GemItem) gemItem.get(i)).id == id) {
                    return (GemItem) gemItem.get(i);
                }
            } catch (Exception var6) {
                break;
            }
        }

        return null;
    }

    private ShopTemplate getSpItem(short id) {
        for (int i = 0; i < this.spItem.size(); i++) {
            try {
                if (((ShopTemplate) this.spItem.get(i)).realId == id) {
                    return (ShopTemplate) this.spItem.get(i);
                }
            } catch (Exception var4) {
                break;
            }
        }

        return null;
    }

    protected Item getItem(short id, int country) {
        if (country > this.items.size()) {
            return null;
        } else {
            Vector<Item> items = (Vector<Item>) this.items.get(country);

            for (int i = 0; i < items.size(); i++) {
                try {
                    if (((Item) items.get(i)).id == id) {
                        return (Item) items.get(i);
                    }
                } catch (Exception var6) {
                    break;
                }
            }

            return null;
        }
    }

    private ShopTemplate dropSpItem(int start) {
        ShopTemplate sp = shop[r.nextInt(2)];
        sp.cat = 7;
        return sp;
    }

    public Item dropItemAnimalByLvChar(int lvChar, int deltalv) {
        if (lvChar > 78) {
            lvChar = 78;
        }

        Vector<ItemTemplates> a = new Vector();
        // Du phong khi khong co mau nao roi vao khoang [lvChar-deltalv, lvChar+1] - vi
        // du trang bi thu cap thap nhat trong game la lv30 (Giap/Non/Yen cuong/Ban
        // dap/Ho uyen Vu Dung), nhan vat duoi ~lv29 luon roi vao khoang rong va TRUOC
        // DAY tra ve null (van tieu mat trang tay du la mau Tim hiem nhat - yeu cau
        // nguoi dung 16/09/2026: trang bi thu phai chac chan 100%, khong duoc trang
        // tay). Lay mau GAN cap nhat lam du phong thay vi bo cuoc hoan toan.
        ItemTemplates ganNhat = null;
        int khoangCachGanNhat = Integer.MAX_VALUE;

        for (ItemTemplates e : itemTemplateCollection.get(5)) {
            if (!isWearingAnimal(e.type)) {
                continue;
            }
            int lvfrom = lvChar - deltalv;
            int lvTo = lvChar + 1;
            if (e.level <= lvTo && e.level >= lvfrom) {
                a.add(e);
            }
            final int khoangCach = Math.abs(e.level - lvChar);
            if (khoangCach < khoangCachGanNhat) {
                khoangCachGanNhat = khoangCach;
                ganNhat = e;
            }
        }

        if (a.size() == 0 && ganNhat == null) {
            return null;
        } else {
            ItemTemplates itemtemplate = (a.size() == 0) ? ganNhat : (ItemTemplates) a.get(random(a.size()));
            Item it = null;
            it = new Item(itemtemplate, true, 0, 0, itemtemplate.id);
            if (itemtemplate.type != 15 && itemtemplate.type != 17) {
                it.magic_physic = (byte) r.nextInt(2);
                if (it.magic_physic == 0) {
                    it.atb[6] = it.atb[1];
                    it.atb[1] = (short) (it.atb[1] / 10);
                } else if (it.magic_physic == 1) {
                    it.atb[6] = (short) (it.atb[1] / 10);
                }
            }

            it.level = itemtemplate.level;
            it.durable = itemtemplate.durable;
            it.mDurable = (short) (it.durable * 10);
            it.dateCreate = Char.getDayTime(0L);
            it.lockItem(null);
            return it;
        }
    }

    public synchronized short getIDITEM() {
        return this.ID_ITEM_MAP++;
    }

    public static boolean isIdTemplateItemDrop(int id) {
        return id < 214 || id >= 598 && id <= 607;
    }

    public static Item createtrungPet(int idTrung) {
        try {
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idTrung);
            Item newItem = null;
            newItem = new Item(template, false, 0, 0, template.id);
            newItem.level = newItem.getTemplate().level;
            newItem.idCountry = 0;
            newItem.clazz = 0;
            newItem.dateCreate = Char.getDayTime(0L);
            return newItem;
        } catch (Exception var3) {
            return null;
        }
    }

    public static Item dropItemByIdTemplate(byte idCountry, int idtemplate) {
        ItemTemplates itemTemplate = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idtemplate);
        if (itemTemplate.id == 2) {
            return null;
        } else {
            Item it = null;
            int type_clazz = r.nextInt(5);
            if (itemTemplate.type >= 3 && itemTemplate.type <= 7) {
                it = new Item(itemTemplate, true, itemTemplate.clazz, itemTemplate.clazz, itemTemplate.id);
            } else {
                it = new Item(itemTemplate, true, type_clazz, type_clazz, itemTemplate.id);
                it.magic_physic = (byte) r.nextInt(2);
                if (itemTemplate.type < 3 || itemTemplate.type == 10 || itemTemplate.type == 11) {
                    if (it.magic_physic == 0) {
                        it.atb[6] = it.atb[1];
                        it.atb[1] = (short) (it.atb[1] / 10);
                    } else if (it.magic_physic == 1) {
                        it.atb[6] = (short) (it.atb[1] / 10);
                    }
                }
            }

            it.idCountry = idCountry;
            it.level = itemTemplate.level;
            it.durable = itemTemplate.durable;
            it.mDurable = (short) (it.durable * 10);
            it.dateCreate = Char.getDayTime(0L);
            it.lockItem(null);
            return it;
        }
    }

    public static Item dropItem(int levelMonster, byte idCountry) {
        try {
            Random rnd = new Random(System.currentTimeMillis());
            int type_clazz = rnd.nextInt(5);
            Vector<ItemTemplates> a = new Vector();

            for (ItemTemplates e : itemTemplateCollection.get(type_clazz)) {
                int lvFrom = levelMonster - 5;
                int lvTo = levelMonster + 1;
                if (e.level <= lvTo && e.level >= lvFrom && isIdTemplateItemDrop(e.id) && e.ndayLoan == 0) {
                    a.add(e);
                }
            }

            if (a.size() == 0) {
                return null;
            } else {
                ItemTemplates itemTemplate = (ItemTemplates) a.get(random(a.size()));
                if (itemTemplate.id == 2) {
                    return null;
                } else {
                    Item it = null;
                    if (itemTemplate.type >= 3 && itemTemplate.type <= 7) {
                        it = new Item(itemTemplate, true, itemTemplate.clazz, itemTemplate.clazz, itemTemplate.id);
                    } else {
                        it = new Item(itemTemplate, true, type_clazz, type_clazz, itemTemplate.id);
                        it.magic_physic = (byte) r.nextInt(2);
                        if (itemTemplate.type < 3 || itemTemplate.type == 10 || itemTemplate.type == 11) {
                            if (it.magic_physic == 0) {
                                it.atb[6] = it.atb[1];
                                it.atb[1] = (short) (it.atb[1] / 10);
                            } else if (it.magic_physic == 1) {
                                it.atb[6] = (short) (it.atb[1] / 10);
                            }
                        }
                    }

                    it.idCountry = idCountry;
                    it.level = itemTemplate.level;
                    it.durable = itemTemplate.durable;
                    it.mDurable = (short) (it.durable * 10);
                    it.dateCreate = Char.getDayTime(0L);
                    it.lockItem(null);
                    return it;
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
            return null;
        }
    }

    public Potion createPotion(short type, int quality, Map m) {
        Potion pt = new Potion(type, quality, m);
        pt.id = this.getIDITEM();
        return pt;
    }

    public static void onMosterDie(Char p, LiveActor mt, byte skill, int ahp, byte effect, byte level) {
        try {
            capNhatDiemGietQuai(p, mt);
        } catch (Exception ignored) {
        }
        try {
            Message m = new Message(17);
            m.dos.writeShort(p.id);
            m.dos.writeShort(mt.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(0);
            m.dos.writeByte(effect);
            m.dos.writeByte(0);
            m.dos.writeByte(1);
            m.dos.writeByte(0);
            m.dos.writeByte(level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var7) {
        }
    }

    // Diem doi thuong tu giet quai: moi 10 con (quai trong khoang +-10 level nguoi choi)
    // duoc 1 diem, tran 1000 diem/ngay tu nguon nay (khong tinh cham vao diemDoiThuong
    // vinh vien da tich tu viec khac - chi chan phan tich luy THEM tu quai trong ngay).
    // Luu y: onMosterDie khong duoc goi tu MOI nhanh giet quai trong engine (mot so nhanh
    // tan cong dien hang loat tu ghi msg 17 rieng ma khong qua ham nay), nen day la
    // nguon diem "hau het" chu khong tuyet doi day du - chap nhan duoc de tranh dung vao
    // hang chuc diem code trung lap khac trong Map.java/MapTown.java/MapLienDau.java...
    private static void capNhatDiemGietQuai(Char p, LiveActor mt) {
        if (p == null || mt == null || mt.cat != 1) {
            return; // chi tinh quai (cat==1), khong tinh PK giet nguoi
        }
        if (Math.abs(p.lvDetail.lv - mt.getLevel()) > 10) {
            return;
        }
        if (p.diemQuaiHomNay >= 1000) {
            return;
        }
        p.soQuaiHomNay++;
        if (p.soQuaiHomNay >= 10) {
            p.soQuaiHomNay -= 10;
            p.diemQuaiHomNay++;
            p.diemDoiThuong++;
        }
    }

    public void onMosterDie(Char p, short idMt, byte skill, int ahp, byte effect, byte level) {
        try {
            Message m = new Message(17);
            m.dos.writeShort(p.id);
            m.dos.writeShort(idMt);
            m.dos.writeByte(skill);
            m.dos.writeInt(0);
            m.dos.writeByte(effect);
            m.dos.writeByte(0);
            m.dos.writeByte(1);
            m.dos.writeByte(0);
            m.dos.writeByte(level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var8) {
        }
    }

    public Char getChar(short id) {
        return CharManager.instance.getByCharID(id);
    }

    public static boolean inRangeSkill(LiveActor p1, LiveActor p2, int rangeSkill) {
        return Math.abs(p1.y - p2.y) > rangeSkill ? false : Math.abs(p1.x - p2.x) <= rangeSkill;
    }

    public static boolean inRangeActor(LiveActor p1, LiveActor p2) {
        return Math.abs(p1.x - p2.x) < 240 && Math.abs(p1.y - p2.y) < 240;
    }

    public static boolean inRangeActor(LiveActor p1, LiveActor p2, int distance) {
        return Math.abs(p1.x - p2.x) < distance && Math.abs(p1.y - p2.y) < distance;
    }

    public static boolean inRangeActor(EffectBuff p1, LiveActor p2, int distance) {
        return Math.abs(p1.x * 16 - p2.x) < distance && Math.abs(p1.y * 16 - p2.y) < distance;
    }

    public static boolean inRangeActor(Actor p1, Actor p2, int distance) {
        return Math.abs(p1.x - p2.x) < distance && Math.abs(p1.y - p2.y) < distance;
    }

    public boolean inArena(Char p) {
        return false;
    }

    public void doSend2AllChar(Message msg, int country) {
        Vector<Char> players = (Vector<Char>) this.allPlayers.get(country);

        for (int i = 0; i < players.size(); i++) {
            try {
                ((Char) players.get(i)).sendMessage(msg);
            } catch (Exception var6) {
            }
        }
    }

    public boolean inMapBoss(Char p) {
        return p.mapID == 112 || p.mapID == 2541 || p.mapID == 2542 || p.mapID == 2543 || p.mapID == 2544;
    }

    private byte countPlayerDie(int country) {
        byte n = 0;
        Vector<Char> players = (Vector<Char>) this.allPlayers.get(country);

        for (int i = 0; i < players.size(); i++) {
            if (((Char) players.get(i)).hp <= 0) {
                n++;
            }
        }

        return n;
    }

    public static int getPcDropItem(int deltaLv, int luck) {
    // Giảm 1% mỗi 5 cấp lệch (tuỳ chỉnh)
    int penalty = Math.min(deltaLv * 20, 500); // max trừ 5%
    return 1500 + luck * 5 - penalty;
    }



    // So chia so xu moi lan quai rot ra. Cang lon thi xu cang it.
    // Doc tu server.ini: sv.chiaXuRoi (mac dinh 10 = nhu cu).
    public static int DIV_DROP_XU = 10;

    public static int getDivDropXu() {
        return (DIV_DROP_XU > 0) ? DIV_DROP_XU : 1;
    }


    // Than method (593 dong) da chuyen sang real/MapDanhNhau.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    protected void doAttackPlayer(Char p, Message message) {
        MapDanhNhau.doAttackPlayer(this, p,message);
    }

    public static void doAddTopLienTram(int country, Char p) {
        Vector<CharInfo> top = (Vector<CharInfo>) topPK.get(country);

        for (int i = 0; i < top.size(); i++) {
            CharInfo c = (CharInfo) top.get(i);
            if (c.name.equals(p.charname)) {
                c.pk = p.maxKill;
                c.honor = p.honor;
                selectionSort(top);
                return;
            }
        }

        CharInfo charinfo = new CharInfo();
        charinfo.name = p.charname;
        charinfo.level = (byte) p.lvDetail.lv;
        charinfo.headStyle = p.headStyle;
        charinfo.money = p.getxu();
        charinfo.idClan = p.idClan;
        charinfo.luong = p.getLuong();
        charinfo.honor = p.honor;
        charinfo.pk = p.maxKill;
        if (top.size() < 10) {
            try {
                String[] data = Char.split(p.infoWearing, ",");
                charinfo.country = Byte.parseByte(data[0]);

                for (short k = 1; k < data.length; k = (short) (k + 4)) {
                    ItemInfo item = new ItemInfo();
                    item.idTemplate = Short.parseShort(data[k]);
                    item.charClass = Byte.parseByte(data[k + 1]);
                    item.level = Byte.parseByte(data[k + 2]);
                    item.plus = Byte.parseByte(data[k + 3]);
                    charinfo.wearingItem.add(item);
                }
            } catch (Exception var9) {
            }

            top.add(charinfo);
        } else {
            int pos = -1;

            for (int ix = 0; ix < top.size(); ix++) {
                if (((CharInfo) top.get(ix)).pk < p.maxKill) {
                    pos = ix;
                    break;
                }
            }

            if (pos > -1) {
                try {
                    String[] data = Char.split(p.infoWearing, ",");
                    charinfo.country = Byte.parseByte(data[0]);

                    for (short k = 1; k < data.length; k = (short) (k + 4)) {
                        ItemInfo item = new ItemInfo();
                        item.idTemplate = Short.parseShort(data[k]);
                        item.charClass = Byte.parseByte(data[k + 1]);
                        item.level = Byte.parseByte(data[k + 2]);
                        item.plus = Byte.parseByte(data[k + 3]);
                        charinfo.wearingItem.add(item);
                    }
                } catch (Exception var8) {
                }

                top.set(top.size() - 1, charinfo);
            }
        }

        selectionSort(top);
    }

    public boolean monsterKillChar(Char p) {
        int allDie = this.countPlayerDie(p.myCountry);
        this.nCharBeKilled[p.myCountry]++;
        if (this.nCharBeKilled[p.myCountry] >= ((Vector) this.allPlayers.get(p.myCountry)).size() - 1) {
            p.potions[101]++;
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được rương vàng"));
            this.nCharBeKilled[p.myCountry] = 0;
        } else if (this.nCharBeKilled[p.myCountry] == 5) {
            p.potions[101]++;
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được rương vàng"));
        } else if (this.nCharBeKilled[p.myCountry] == 15) {
            p.potions[101]++;
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Nhận được rương vàng"));
        }

        Vector<Char> players = (Vector<Char>) this.allPlayers.get(p.myCountry);
        return allDie == players.size() - 1;
    }

    public static boolean canAttackMonsterVantieu(Monster mt, Char p) 
    {
        if (p != null && mt != null) 
        {
            if (mt.isMonsterVantieu()) 
            {
                if (p.charDBID == mt.getCharID()) 
                {
                    return false;
                }

                 if (p.cuop_tieu <= 0 && TeamServer.server != 100) 
                 {
                     return false;
                 }

                if (!p.map.equals(mt.map)) 
                {
                    return false;
                }

                if (p.region != mt.region) 
                {
                    return false;
                }

                if (p.monster != null) 
                {
                    return false;
                }

                if (p.lvDetail.lv < 40) 
                {
                    return false;
                }

                if (p.isKiller || p.myCountry != mt.inCountry)
                {
                    // Gioi han cuop tieu +-5 lv (yeu cau nguoi dung 19/09/2026), thay cho
                    // 10/15 truoc day (15 danh rieng cho tieu loai >=4).
                    if (abs(mt.level - p.lvDetail.lv) > 5)
                    {
                        return false;
                    }
                }

                if (!p.isKiller && p.myCountry == mt.inCountry) 
                {
                    return false;
                }
            }

            return true;
        } else 
        {
            return false;
        }
    }

    // Than method (509 dong) da chuyen sang real/MapDanhNhau.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    public void doAttackMultiTarget(Char p, Message message) {
        MapDanhNhau.doAttackMultiTarget(this, p,message);
    }

    public static synchronized void setTimeCake() {
        int[] var10000 = new int[]{2};
        int time = r.nextInt(10) + 5;
        timeDropCake = System.currentTimeMillis() + (long) (time * 60000);
    }

    public static synchronized Potion dropItemSuKienChungSV(Char p, Monster mt) {
        Potion pt = null;

        try {
            if (System.currentTimeMillis() - timeDropCake > 0L) {
                int deltalv = abs(p.lvDetail.lv - mt.level);
                if (deltalv <= deltaLVAttackMons) {
                    setTimeCake();
                    byte ptType = 104;
                    pt = new Potion(ptType, 1, mt.map);
                    pt.id = mt.map.getIDITEM();
                    pt.x = mt.x - 4;
                    pt.y = mt.y - 4;
                    pt.belongUser = p.charDBID;
                }
            }
        } catch (Exception var5) {
        }

        return pt;
    }

    public void doSetBangLan(Char p) {
        Vector<LiveActor> ac = new Vector();

        for (Monster mt : (this.monsters.get(p.inCountry)).values()) {
            if (!mt.isDead && inRangeActor(p, mt, 120)) {
                ac.add(mt);
                if (ac.size() == 5) {
                    p.setBangLan(ac);
                    break;
                }
            }
        }

        this.doSetDocLan(p);
    }

    public void doSetDocLan(Char p) {
        Vector<LiveActor> ac = new Vector();

        for (Monster mt : (this.monsters.get(p.inCountry)).values()) {
            if (!mt.isDead && inRangeActor(p, mt, 120)) {
                ac.add(mt);
                if (ac.size() == 5) {
                    p.setDocLan(ac);
                    break;
                }
            }
        }
    }

    /// TODO Đánh Quái
    // Than method (470 dong) da chuyen sang real/MapDanhNhau.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    protected void doAttackMonster(Char p, Message message) throws IOException {
        MapDanhNhau.doAttackMonster(this, p,message);
    }

    public void sendAllCharMap(Message m, int country, int provider) {
        Vector<Char> allcharmap = (Vector<Char>) this.allPlayers.get(country);

        for (int i = 0; i < allcharmap.size(); i++) {
            try {
                Char p = (Char) allcharmap.get(i);
                if (p.isBot == -1 && p.getSession().clientProvider.equals("" + provider)) {
                    p.sendMessage(m);
                }
            } catch (Exception var7) {
            }
        }
    }

    public void sendAllCharMap(Message m, int country) {
        Vector<Char> allcharmap = (Vector<Char>) this.allPlayers.get(country);

        for (int i = 0; i < allcharmap.size(); i++) {
            try {
                Char p = (Char) allcharmap.get(i);
                if (p.isBot == -1) {
                    p.sendMessage(m);
                }
            } catch (Exception var6) {
            }
        }
    }

    public void sendAllPlayer(Message m, int country) {
        try {
            Vector<Char> players = (Vector<Char>) this.allPlayers.get(country);

            for (int i = 0; i < players.size(); i++) {
                ((Char) players.get(i)).sendMessage(m);
            }

            m.cleanup();
        } catch (Exception var5) {
        }
    }

    public void sendAllPlayer(Message m, int country, int region) {
        try {
            Vector<Char> players = this.getAllPlayer(country, region);

            for (int i = 0; i < players.size(); i++) {
                ((Char) players.get(i)).sendMessage(m);
            }

            m.cleanup();
        } catch (Exception var6) {
        }
    }

    public void kickAllCharDifCountry(int country) {
        try {
            Vector<Char> players = (Vector<Char>) this.allPlayers.get(country);
            Vector<Char> kick = new Vector();

            for (int i = 0; i < players.size(); i++) {
                Char c = (Char) players.get(i);
                if (c.myCountry != country && c.isBot == -1) {
                    kick.add(c);
                }
            }

            Map map = (Map) RealController.mapList.get(-1);
            players = (Vector<Char>) map.allPlayers.get(country);

            for (int ix = 0; ix < players.size(); ix++) {
                Char c = (Char) players.get(ix);
                if (c.myCountry != country && c.isBot == -1 && c.mapID >= 201) {
                    kick.add(c);
                }
            }

            for (int ixx = 0; ixx < kick.size(); ixx++) {
                Char p = (Char) kick.get(ixx);
                int[] mapstart = new int[]{9, 481, 482, 483, 484};
                int toTX = 16 + Database.r.nextInt() % 5;
                int toTY = 84;
                int toMapID = mapstart[r.nextInt(MessageCreator.nclone)];
                this.move2Map(p, toTX, toTY, toMapID, p.inCountry);
            }
        } catch (Exception var11) {
        }
    }

    public static int calculatorXpParty(Char p, int xp) {
        int nuser = p.party.userParty.size();
        if (nuser > 1 && nuser <= 5) {
            if (nuser > 5) {
                nuser = 5;
            }

            int percent = p.isFullParty() ? 400 : 300 + (nuser - 2) * 25;
            // Tinh bang long: quai co rcvxp toi 20.000.000 (Dracula, Bi ngo), xp * 400 tran int
            // ngay tu ~5,4 trieu -> increaXp AM -> addXPForChar nhan dxp am (chi chan dxp == 0)
            // -> ca to doi TUT CAP, exp co the am roi LevelDetail nem loi giua synchronized.
            long tong = (long) xp + (long) xp * percent / 100L;
            xp = (int) Math.min((long) Integer.MAX_VALUE, tong);
        }

        return xp;
    }

    // Than method (118 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public static void addXpCharEvent(Char p, long dxp, boolean isNhansam, String pos) {
        MapXuLyLenh.addXpCharEvent(p,dxp,isNhansam,pos);
    }

    // Than method (109 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public static void addXPForChar(Char p, long dxp, boolean isNhansam, String pos) throws IOException {
        MapXuLyLenh.addXPForChar(p,dxp,isNhansam,pos);
    }

    public static int randomMillion() {
        return r.nextInt(1000000);
    }

    public static int rand10(int x) {
        return x >> 2 == 0 ? x : x + abs(r.nextInt()) % (x >> 2);
    }

    public static int abs(int x) {
        return x > 0 ? x : -x;
    }

    protected void doGetGemItem(Char player, Message message) {
        try {
            int cat = message.dis.readByte();
            short id = message.dis.readShort();
            if (player.hp <= 0) {
                CharManager.instance.kickPlayer(player, "map 8");
                return;
            }

            if (cat == 6) {
                GemItem gem = getGem(id, player.inCountry);
                if (gem != null) {
                    if (player.isFullInventory() && !player.hadGemItem(gem.idGemTemplate)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy", ""));
                        return;
                    }

                    if (player.listGemitem[gem.idGemTemplate] > 32000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã có quá nhiều", ""));
                        return;
                    }

                    if (gem.isBoss) {
                        if (gem.idPartyBoss != 0 && (gem.belongParty != 0 && gem.belongParty == player.partyID || gem.belongParty == player.charDBID)) {
                            if (!this.chiemGem(gem, player.inCountry)) {
                                return; // luong khac vua nhat vien nay
                            }
                            player.doAddGemItem(gem.idGemTemplate, 1, gem.islock);
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.allGemGet[gem.idGemTemplate]++;
                        }
                    } else {
                        if (gem.belongUser != player.charDBID && gem.belongUser != 0) {
                            return;
                        }

                        if (inRangeActor(player, gem, 48)) {
                            if (!this.chiemGem(gem, player.inCountry)) {
                                return;
                            }
                            player.doAddGemItem(gem.idGemTemplate, 1, gem.islock);
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.allGemGet[gem.idGemTemplate]++;
                        }
                    }
                }
            } else if (cat == 7) {
                ShopTemplate sp = this.getSpItem(id);
                if (sp != null) {
                    this.spItem.remove(sp);
                    ShopTemplate spItem = new ShopTemplate();
                    spItem.coppy(spItem, sp);
                    spItem.ownerId = player.charDBID;
                    spItem.realId = player.getIDItem();
                    player.specialItem.add(spItem);
                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                    Database.instance.saveOrtherLog("tob_log_other_gem", player.charname, spItem.name, "gsp");
                }
            }

            Message m = new Message(-41);
            m.dos.writeShort(player.id);
            m.dos.writeByte(cat);
            m.dos.writeShort(id);
            player.sendMessage(m);
            player.sendToNearPlayer(m);
        } catch (Exception var7) {
            var7.printStackTrace();
            System.out.println("LOI LUOM  GEM");
        }
    }

    public void doGetGemItem(Char player, int cat, short id) {
        try {
            if (cat == 6) {
                GemItem gem = getGem(id, player.inCountry);
                if (gem != null) {
                    if (player.isFullInventory() && !player.hadGemItem(gem.idGemTemplate)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy", ""));
                        return;
                    }

                    if (player.listGemitem[gem.idGemTemplate] > 32000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã có quá nhiều", ""));
                        return;
                    }

                    if (!this.chiemGem(gem, player.inCountry)) {
                        return;
                    }
                    player.doAddGemItem(gem.idGemTemplate, 1, gem.islock);
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.allGemGet[gem.idGemTemplate]++;
                }
            } else if (cat == 7) {
                ShopTemplate sp = this.getSpItem(id);
                if (sp != null) {
                    this.spItem.remove(sp);
                    ShopTemplate spItem = new ShopTemplate();
                    spItem.coppy(spItem, sp);
                    spItem.ownerId = player.charDBID;
                    spItem.realId = player.getIDItem();
                    player.specialItem.add(spItem);
                    player.sendMessage(MessageCreator.createCharSpecialItem(player));
                }
            }

            Message m = new Message(-41);
            m.dos.writeShort(player.id);
            m.dos.writeByte(cat);
            m.dos.writeShort(id);
            player.sendMessage(m);
            player.sendToNearPlayer(m);
        } catch (Exception var6) {
        }
    }

    // Than method (87 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public void doGetItem(Char player, short id) {
        MapXuLyLenh.doGetItem(this, player,id);
    }

    public static synchronized void addGetTrungPet() {
        totaltrungget++;
        Database.instance.saveEvent(event.getInfo());
    }

    public static synchronized boolean checkXuatHienTrungVV() {
        int total = totalxuathienvv;
        if (totaltrungget >= total) {
            totaltrungget = 0;
            totaltrungvv++;
            if (totalxuathienvv <= 1000) {
                totalxuathienvv = 5000;
            } else if (totalxuathienvv <= 5000) {
                totalxuathienvv = 10000;
            }

            return true;
        } else {
            return false;
        }
    }

    public static Item droptrungPet(int idcountry) {
        try {
            int[] idTrung = new int[]{684, 685, 686, 687, 688};
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idTrung[r.nextInt(idTrung.length)]);
            Item newItem = null;
            newItem = new Item(template, false, 0, 0, template.id);
            newItem.level = newItem.getTemplate().level;
            newItem.idCountry = (byte) idcountry;
            newItem.clazz = 0;
            newItem.dateCreate = Char.getDayTime(0L);
            int[] nday = new int[]{3, 5, 7, 15};
            int day = nday[r.nextInt(nday.length)];
            if (day > -1) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = day * 24 * 60;
            }

            return newItem;
        } catch (Exception var6) {
            return null;
        }
    }

    protected void doGetItem(Char player, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        if (player.isFullInventory()) {
            Message m = new Message(27);
            m.dos.writeShort(player.id);
            m.dos.writeUTF("Hành trang đã đầy");
            player.sendMessage(m);
            m.cleanup();
        } else if (player.hp > 0) {
            Item item = this.getItem(id, player.inCountry);
            if (item != null) {
                if (item.isBoss) {
                    if (item.idPartyBoss != 0 && (item.belongParty != 0 && item.belongParty == player.partyID || item.belongParty == player.charDBID)) {
                        if (!this.chiemItem(item, player.inCountry)) {
                            return; // luong khac vua nhat mon nay
                        }
                        item.isBoss = false;
                        item.owner = player.charDBID;
                        short oldid = item.id;
                        item.id = player.getIDItem();
                        item.identify = player.charDBID;
                        player.iItems.add(item);
                        item.isItemDrop = 1;
                        Message m = new Message(18);
                        m.dos.writeShort(player.id);
                        m.dos.writeByte(item.clazz);
                        m.dos.writeShort(oldid);
                        m.dos.writeShort(item.id);
                        m.dos.writeShort(item.getTemplate().id);
                        m.dos.writeByte(item.plus);
                        m.dos.writeByte(item.level);
                        m.dos.writeShort(item.durable);
                        m.dos.writeShort(item.mDurable);
                        player.sendMessage(m);
                        player.sendToNearPlayer(m);
                        m.cleanup();
                        String info = item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute();
              
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    }
                } else if ((item.belongUser == player.charDBID || item.belongUser == 0) && inRangeActor(player, item, 64)) {
                    if (!this.chiemItem(item, player.inCountry)) {
                        return;
                    }
                    item.owner = player.charDBID;
                    short oldid = item.id;
                    boolean isOk = true;
                    if (item.istrungPet()) {
                        if (player.potions[104] <= 0 || player.isFullInventory() || player.potions[104] > 0 && r.nextInt(100) < 0) {
                            isOk = false;
                            if (player.potions[104] > 0) {
                                player.potions[104]--;
                                addGetTrungPet();
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            }

                            sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã hụt " + item.getName()));
                        } else {
                            if (player.potions[104] <= 0) {
                                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã hụt " + item.getName()));
                            } else {
                                int[] nday = new int[]{3, 5, 7, 15};
                                int day = nday[r.nextInt(nday.length)];
                                if (day > -1) {
                                    item.timeLoan = System.currentTimeMillis();
                                    item.minuteExist = day * 24 * 60;
                                }

                                if (checkXuatHienTrungVV()) {
                                    day = -1;
                                    item.timeLoan = 0L;
                                    item.minuteExist = 0;
                                }

                                if (day == -1) {
                                    sendAllCharServer(
                                            -1,
                                            MessageCreator.createServerAlertAutoOffMessage(
                                                    player.charname + " đã nhận được " + item.getName() + " " + (day == -1 ? "vĩnh viễn" : day + " ngày")
                                            )
                                    );
                                }

                                player.potions[104]--;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                if (day == -1) {
                                    Database.instance
                                            .saveOrtherLog(
                                                    "",
                                                    player.charname,
                                                    player.charname + " đã nhận được " + item.getName() + " " + (day == -1 ? "vĩnh viễn" : day + " ngày"),
                                                    "batpetvv"
                                            );
                                } else {
                                    Database.instance
                                            .saveOrtherLog(
                                                    "",
                                                    player.charname,
                                                    player.charname + " đã nhận được " + item.getName() + " " + (day == -1 ? "vĩnh viễn" : day + " ngày"),
                                                    "batpet"
                                            );
                                }
                            }

                            addGetTrungPet();
                        }
                    }

                    if (isOk) {
                        item.id = player.getIDItem();
                        player.iItems.add(item);
                    }

                    Message m = new Message(18);
                    m.dos.writeShort(player.id);
                    m.dos.writeByte(item.clazz);
                    m.dos.writeShort(oldid);
                    m.dos.writeShort(item.id);
                    m.dos.writeShort(item.getTemplate().id);
                    m.dos.writeByte(item.plus);
                    m.dos.writeByte(item.level);
                    m.dos.writeShort(item.durable);
                    m.dos.writeShort(item.mDurable);
                    player.sendMessage(m);
                    player.sendToNearPlayer(m);
                    m.cleanup();
                    String info = isOk + "_" + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute();

                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                }
            } else {
                Message m = new Message(20);
                m.dos.writeByte(3);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
            }
        }
    }

    public static boolean isPotionUnlimit(short id) {
        return id == 104
                || id == 74
                || id == 118
                || id == 117
                || id == 80
                || id == 82
                || id == 121
                || id == 125
                || id == 88
                || id == 142
                || id == 119
                || id == 35
                || id == 74
                || id == 73
                || id == 77
                || id == 122
                || id == 71
                || id == 72
                || id == 120
                || id == 124
                || id == 105
                || id == 137
                || id == 138
                || id == 139
                || id == 140
                || id == 143
                || id == 145
                || id == 146
                || id == 147
                || id == 148
                || id == 149
                || id == 150
                || id == 151
                || id == 152
                || id == 156
                || id == 158
                || id == 157
                || id == 159
                || id == 122
                || id == 154
                || id == 153
                // Nhanh hoa hong vang (Vu Lan, xem Potion.TYPE_NHANH_HOA_HONG_VANG): thieu
                // trong danh sach nay lam doGetPotion chan nhat (ca tu dong lan bam tay) im
                // lang khi da vuot 999 - trong khi boss/che do da cong thang qua duong khong
                // kiem tran nen nhan vat vuot moc rat nhanh.
                || id == 106
                // Bong bac / bong vang (World Cup, SuKienWorldCup.ITEM_BONG_VANG = 161):
                // cung roi qua duong nay (Monster.java, SuKienWorldCup.quaiRoiBongVang) va
                // cung khong kiem tran o cho roi - boss "luon roi 20-50 qua" nen vuot 999
                // trong vai chuc lan giet, dung loi voi Vu Lan.
                || id == 160
                || id == 161;
    }

    public void doGetPotion(Char player, short id) throws IOException {
        Potion pt = this.getPotion(id, player.inCountry);
        // SECURITY: guard against corrupted/out-of-range potion type used as array index.
        if (pt != null && (pt.getType() < 0 || pt.getType() >= player.potions.length)) {
            return;
        }
        if (player.isFullInventory() && pt != null && player.potions[pt.getType()] == 0 && pt.getType() > 0) {
            Message m = new Message(27);
            m.dos.writeShort(player.id);
            m.dos.writeUTF("Hành trang đã đầy");
            player.sendMessage(m);
            m.cleanup();
        } else {
            if (pt != null) {
                if ((pt.getType() == 0
                        || isPotionUnlimit(pt.getType())
                        || player.potions[pt.getType()] < 999 && pt.getType() != 85 && pt.getType() != 80 && pt.getType() != 88
                        || pt.getType() == 78
                        || pt.getType() == 80
                        || pt.getType() == 35)
                        && (pt.belongUser == player.charDBID || pt.belongUser == 0)) {
                    if (!this.chiemPotion(pt, player.inCountry)) {
                        return; // luong khac vua nhat binh nay
                    }
                    player.potions[pt.getType()] += pt.quantity;
                    if (pt.getType() == 0) {
                        player.addXu((long) pt.quantity, "map 2");
                        player.addXu((long) (pt.quantity * player.getEffSkillClanMember(0) / 100), "map 3");
                    }

                    if (pt.getType() != 0
                            && pt.getType() != 85
                            && !isPotionUnlimit(pt.getType())
                            && pt.getType() != 80
                            && pt.getType() != 88
                            && player.potions[pt.getType()] > 999) {
                        player.potions[pt.getType()] = 999;
                    }

                    Message m = new Message(19);
                    m.dos.writeShort(player.id);
                    m.dos.writeShort(pt.id);
                    m.dos.writeByte(pt.getType());
                    m.dos.writeShort(pt.quantity);
                    player.sendMessage(m);
                    player.sendToNearPlayer(m);
                    m.cleanup();
                    if ((pt.getType() == 85 || pt.getType() == 80 || pt.getType() == 88 || isPotionUnlimit(pt.getType())) && pt.getType() != 0) {
                        Database.instance
                                .saveOrtherLog(
                                        "tob_log_other_potion",
                                        player.charname,
                                        pt.getType() + " Nhat dc " + LoginHandler.PORTION_NAME[pt.getType()] + " " + pt.quantity,
                                        LoginHandler.PORTION_NAME[pt.getType()]
                                );
                    }
                }
            } else {
                Message m = new Message(20);
                m.dos.writeByte(4);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
            }
        }
    }

    void doGetItemQuest(Char player, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        ItemQuest pt = this.getItemQuest(id, player.inCountry);
        if (pt != null) {
            if (pt.id_char == player.charDBID) {
                if (!this.chiemItemQuest(pt, player.inCountry)) {
                    return; // luong khac (vong tu dong nhat) vua lay mon nay
                }
                player.itemquest[pt.getType()] += pt.quantity;
                Message m = new Message(-65);
                m.dos.writeShort(player.id);
                m.dos.writeShort(pt.id);
                m.dos.writeByte(pt.getType());
                player.sendMessage(m);
                player.sendToNearPlayer(m);
                m.cleanup();
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 3));
                player.checkFinishQuest(pt.getType(), false, 0);
            }
        } else {
            Message m = new Message(20);
            m.dos.writeByte(14);
            m.dos.writeShort(id);
            player.sendMessage(m);
            m.cleanup();
        }
    }

    public void doGetItemQuest(Char player, short id) throws IOException {
        ItemQuest pt = this.getItemQuest(id, player.inCountry);
        if (pt != null) {
            if (pt.id_char == player.charDBID) {
                if (!this.chiemItemQuest(pt, player.inCountry)) {
                    return;
                }
                player.itemquest[pt.getType()] += pt.quantity;
                Message m = new Message(-65);
                m.dos.writeShort(player.id);
                m.dos.writeShort(pt.id);
                m.dos.writeByte(pt.getType());
                player.sendMessage(m);
                player.sendToNearPlayer(m);
                m.cleanup();
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 3));
                player.checkFinishQuest(pt.getType(), false, 0);
            }
        } else {
            Message m = new Message(20);
            m.dos.writeByte(14);
            m.dos.writeShort(id);
            player.sendMessage(m);
            m.cleanup();
        }
    }

    protected void doGetPotion(Char player, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        Potion pt = this.getPotion(id, player.inCountry);
        // SECURITY: guard against corrupted/out-of-range potion type used as array index (was crashing
        // with ArrayIndexOutOfBoundsException, e.g. index -110 for length 162).
        if (pt != null && (pt.getType() < 0 || pt.getType() >= player.potions.length)) {
            return;
        }
        if (player.isFullInventory() && pt != null && player.potions[pt.getType()] == 0 && pt.getType() > 0) {
            Message m = new Message(27);
            m.dos.writeShort(player.id);
            m.dos.writeUTF("Hành trang đã đầy");
            player.sendMessage(m);
            m.cleanup();
        } else {
            if (pt != null) {
                if ((pt.getType() == 0
                        || isPotionUnlimit(pt.getType())
                        || player.potions[pt.getType()] < 999 && pt.getType() != 78 && pt.getType() != 85 && pt.getType() != 80 && pt.getType() != 35
                        || pt.getType() == 78
                        || pt.getType() == 80
                        || pt.getType() == 35)
                        && (pt.belongUser == player.charDBID || pt.belongUser == 0)) {
                    if (!this.chiemPotion(pt, player.inCountry)) {
                        return;
                    }
                    player.potions[pt.getType()] += pt.quantity;
                    if (pt.getType() == 0) {
                        player.addXu((long) (pt.quantity + pt.quantity * player.getEffSkillClanMember(0) / 100), "map 4");
                    }

                    if (pt.getType() != 0
                            && !isPotionUnlimit(pt.getType())
                            && pt.getType() != 85
                            && pt.getType() != 78
                            && pt.getType() != 80
                            && pt.getType() != 35
                            && player.potions[pt.getType()] > 999) {
                        player.potions[pt.getType()] = 999;
                    }

                    Message m = new Message(19);
                    m.dos.writeShort(player.id);
                    m.dos.writeShort(pt.id);
                    m.dos.writeByte(pt.getType());
                    m.dos.writeShort(pt.quantity);
                    player.sendMessage(m);
                    player.sendToNearPlayer(m);
                    m.cleanup();
                    if ((pt.getType() == 80 || pt.getType() == 88 || isPotionUnlimit(pt.getType())) && pt.getType() != 0) {
                        Database.instance
                                .saveOrtherLog(
                                        "tob_log_other_potion",
                                        player.charname,
                                        pt.getType() + " Nhat dc " + LoginHandler.PORTION_NAME[pt.getType()] + " " + pt.quantity,
                                        LoginHandler.PORTION_NAME[pt.getType()]
                                );
                    }
                }
            } else {
                Message m = new Message(20);
                m.dos.writeByte(4);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
            }
        }
    }

    protected void doPlayerMove(Char p, Message message) throws IOException {
        p.doPlayerMove(message);
    }

    public boolean canMove(int x, int y) {
        try {
            return (this.type[(y >> 4) * this.w + (x >> 4)] & 2) != 2;
        } catch (Exception var4) {
            return false;
        }
    }

    public void doRequestMonterInfo(Char p, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        Monster monster = this.getMonster(id, p.mapID != 225 && p.mapID != 226 ? p.inCountry : p.idWedding, p.region);
        if (monster == null) {
            boolean die = true;

            try {
                Monster mons = (Monster) MonsterVantieu.HAS_ALL_MONSTER_VANTIEU.get(id);
                if (mons != null && mons.id == id && !mons.isFinish() && p.map.equals(mons.map)) {
                    monster = mons;
                    die = false;
                }

                if (die) {
                    Message m = new Message(90);
                    m.dos.writeShort(id);
                    m.dos.writeByte(1);
                    p.sendMessage(m);
                }
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            if (die) {
                return;
            }
        }

        if (p.map.equals(monster.map) && (p.near(monster, p.rangeAddMonster[0]) || monster.isBoss() || monster.isCongThanh())) {
            Message m = new Message(7);
            m.dos.writeShort(monster.id);
            m.dos.writeByte(monster.getType());
            m.dos.writeShort(monster.x);
            m.dos.writeShort(monster.y);
            m.dos.writeInt(monster.hp);
            m.dos.writeByte(monster.level);
            m.dos.writeByte(monster.he);
            m.dos.writeInt(monster.maxhp);
            m.dos.writeInt(monster.getTimeReborn());
            p.sendMessage(m);
        }
    }


    void doUnRideHorse(Char player, int type) {
        try {
            String sub = " full";
            if (type == 0 && player.rideHorse > 0) {
                int horse = 0;
                if (player.animalRide != null) {
                    switch (player.rideHorse) {
                        case 1:
                            horse = 68;
                            break;
                        case 2:
                            horse = 64;
                            break;
                        case 3:
                            horse = 65;
                            break;
                        case 4:
                            horse = 66;
                            break;
                        case 5:
                            horse = 67;
                            break;
                        case 6:
                            horse = 86;
                            break;
                        case 7:
                            horse = 36;
                    }
                } else if (player.idImgHorse == 0) {
                    player.maxhp -= 700;
                    player.maxmp -= 700;
                    horse = 30;
                } else {
                    horse = 34;
                }

                boolean isfullInventory = player.isFullInventory();
                if (horse <= 34 && horse > 0) {
                    if (!isfullInventory || isfullInventory && player.potions[horse] > 0) {
                        player.potions[horse]++;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        sub = " chưa full";
                    }
                } else {
                    if (player.animalRide != null) {
                        player.animalRide.place = 0;
                    }

                    player.animalRide = null;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                }

                player.rideHorse = 0;
                player.speed = 5;
                player.calculateAttrib();

                try {
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                    player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                    MessageCreator.createMsgCharMonster(player, player);
                } catch (Exception var7) {
                }

                Database.instance.saveOrtherLog("tob_log_other_animal", player.charname, "Xuống ngựa " + horse + " và hành trang " + sub, "uride");
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    void doUnRideHorse(Char player, Message msg) {
        try {
            byte type = 0;

            try {
                type = msg.dis.readByte();
            } catch (Exception var9) {
            }

            String sub = " full";
            if (type == 0) {
                if (player.rideHorse > 0) {
                    int horse = 0;
                    if (player.animalRide != null) {
                        switch (player.rideHorse) {
                            case 1:
                                horse = 68;
                                break;
                            case 2:
                                horse = 64;
                                break;
                            case 3:
                                horse = 65;
                                break;
                            case 4:
                                horse = 66;
                                break;
                            case 5:
                                horse = 67;
                                break;
                            case 6:
                                horse = 86;
                                break;
                            case 7:
                                horse = 36;
                        }
                    } else if (player.idImgHorse == 0) {
                        player.maxhp -= 700;
                        player.maxmp -= 700;
                        horse = 30;
                    } else {
                        horse = 34;
                    }

                    boolean isfullInventory = player.isFullInventory();
                    if (horse <= 34 && horse > 0) {
                        if (!isfullInventory || isfullInventory && player.potions[horse] > 0) {
                            player.potions[horse]++;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            sub = " chưa full";
                        }
                    } else {
                        if (player.animalRide != null) {
                            player.animalRide.place = 0;
                        }

                        player.animalRide = null;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    }

                    player.rideHorse = 0;
                    player.speed = 5;
                    player.calculateAttrib();

                    try {
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        MessageCreator.createMsgCharMonster(player, player);
                    } catch (Exception var8) {
                    }

                    Database.instance.saveOrtherLog("tob_log_other_animal", player.charname, "Xuống ngựa " + horse + " và hành trang " + sub, "uride");
                }
            } else if (type == 1) {
                boolean old = false;
                if (player.itemVukhiThoiTrang != null && player.itemVukhiThoiTrang.isChoiLuaLanBienHinh()) {
                    player.iItems.add(player.itemVukhiThoiTrang);
                    player.itemVukhiThoiTrang = null;
                    old = true;
                }

                if (player.wModel.idModel > 0) {
                    old = true;
                    player.wModel.unWearing(player, -1);
                }

                if (old) {
                    Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "tháo đồ " + player.wModel + " và hành trang" + sub, "uwea");
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                }
            } else if (type == 2 && player.itemAx != null) {
                player.itemAx.place = 0;
                player.removeItemOutWearing(13, 0);
                player.iItems.add(player.itemAx);
                player.itemAx = null;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    public static boolean isPotionChienTruong(int id) {
        return id >= 126 && id <= 132;
    }

    // Than method (760 dong) da chuyen sang real/MapDungThuoc.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    protected void doUsePotion(Char player, Message message) throws IOException {
        MapDungThuoc.doUsePotion(this, player,message);
    }

    public Item doCreateHoaKyLan(Char player, int idTemplate, long timeExpire) {
        try {
            int clazz = 0;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(idTemplate));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            newItem.identify = player.charDBID;
            newItem.clazz = (byte) clazz;
            newItem.doAddOptionThanThu(template.atb[0]);
            if (timeExpire > 0L) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = (int) timeExpire;
                newItem.timeExpireHkl = System.currentTimeMillis() + timeExpire * 60000L;
            }

            newItem.dateCreate = Char.getDayTime(0L);
            player.iItems.add(newItem);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            Database.instance.saveOrtherLog("", player.charname, "tao hoa ky lan " + idTemplate + " thoi han " + timeExpire, "hoakylan");
            return newItem;
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }

    public Item doCreateLanSuVu(Char player, int idTemplate, long timeExpire) {
        try {
            int clazz = 0;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(idTemplate));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            newItem.identify = player.charDBID;
            newItem.clazz = (byte) clazz;
            newItem.doAddOptionThanThu(template.atb[0]);
            if (timeExpire > 0L) {
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = (int) timeExpire;
                newItem.timeExpireHkl = System.currentTimeMillis() + timeExpire * 60000L;
            }

            newItem.dateCreate = Char.getDayTime(0L);
            player.iItems.add(newItem);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            Database.instance.saveOrtherLog("", player.charname, "tao hoa ky lan " + idTemplate + " thoi han " + timeExpire, "hoakylan");
            return newItem;
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }

    void doCreateHoaKyLan(Char player) {
        try {
            int clazz = 0;
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(681));
            Item newItem = null;
            newItem = new Item(template, false, clazz, clazz, template.id);
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            player.iItems.add(newItem);
            newItem.identify = player.charDBID;
            newItem.clazz = (byte) clazz;
            newItem.doAddOptionThanThu(template.atb[0]);
            newItem.dateCreate = Char.getDayTime(0L);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            Database.instance.saveOrtherLog("", player.charname, "tao hoa ky lan", "hoakylan");
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }


    /**
     * Trung Thu 2026 (sua 19/09/2026, yeu cau nguoi dung): ham nay von co san nhung
     * KHONG CON duoc goi o dau ca, va con dung SAI potionType cua nam truoc (124/122 la
     * banh deo/dau xanh trong bang potions[] hien tai, khong phai Lua vua/Lua to nua) -
     * nen dung potion Lua thuong/vua/to hien tai (156/157/158) khong co hieu ung gi. Sua
     * lai dung 3 id hien hanh, doi thanh package-private de doUsePotion.java/
     * MapDungThuoc.java goi duoc.
     */
    void doDotLua(Char p, short potionType) {
        int type = EffectBuff.LUA_NHO;
        p.idCuiDot = 0;
        if (potionType == 157) {
            p.idCuiDot = 1;
            type = EffectBuff.LUA_VUA;
        } else if (potionType == 158) {
            type = EffectBuff.LUA_TO;
            p.idCuiDot = 2;
        }

        this.addEffectAutoInMap2Char(p.myCountry, p, p.region, p.x / 16, p.y / 16, type, System.currentTimeMillis() + 3600000L);
        Database.instance.saveOrtherLog("", p.charname, "dot lua " + LoginHandler.PORTION_NAME[potionType], "dotlua");
    }

    private void doUongRuou(Char p, short potionType) {
        if (potionType == 142) {
            p.idRuouUong = 0;
            p.addEffBuff(EffectBuff.UONG_RUOU, System.currentTimeMillis() + 60000L, 1, 0);
        } else if (potionType == 104) {
            p.addEffBuff(EffectBuff.UONG_RUOU1, System.currentTimeMillis() + 60000L, 1, 0);
            p.idRuouUong = 1;
        } else if (potionType == 124) {
            p.addEffBuff(EffectBuff.UONG_RUOU2, System.currentTimeMillis() + 60000L, 1, 0);
            p.idRuouUong = 2;
        }

        p.sendEffToChar(p);
        p.sendEffToNearChar();
    }


    public void doBongVang(Char p) {
        try {
            p.banphaohoa++;
            totalDotPhao++;
            if (totalDotPhao >= 15000) {
                x3ExpDotPhao = 1;
                totalDotPhao = 0;
            }

            addXpCharEvent(p, 300000L, false, "doBanPhaoHoa");
            int typeParty = 1;
            Vector<Char> players = this.getAllPlayer(p.inCountry, p.region);

            for (int i = 0; i < players.size(); i++) {
                Char player = (Char) players.get(i);

                try {
                    if (player.id == p.id || player.getSession().firmWare != 0) {
                        for (int j = 0; j < pos_fire_work[typeParty].length; j += 2) {
                            Message m = MessageCreator.createMsgDynamicEff(
                                    p, pos_fire_work[typeParty][j] * 16, pos_fire_work[typeParty][j + 1] * 16, 0, 0, 0, 0, 45, 5
                            );
                            player.sendMessage(m);
                        }
                    }
                } catch (Exception var15) {
                }
            }

            int[] per = new int[]{500, 500, 500, 500, 500, 10, 100, 10, 500, 500, 500, 5, 10, 300, 300, 300, 50};
            int[] idgift = new int[]{
                13,
                8,
                93,
                95,
                35,
                84,
                82,
                100,
                112,
                113,
                114,
                GemTemplate.LKD_40,
                GemTemplate.DA_TIEN_GIAI,
                GemTemplate.BOT_TRANG,
                GemTemplate.BOT_XANH,
                GemTemplate.BOT_DO,
                GemTemplate.BOT_XANH_LA
            };
            int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6};
            int[] soluongmax = new int[]{1, 1, 100, 100, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            int[] soluongmin = new int[]{1, 1, 50, 50, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            String info = "Chúc mừng bạn nhận được ";
            int index = Char.getIndexRandom(per);
            if (typegift[index] == 6) {
                boolean lock = r.nextInt(100) < 50;
                p.doAddGemItem(idgift[index], soluongmax[index], lock);
                info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name;
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                if (idgift[index] == GemTemplate.LKD_40 || idgift[index] == GemTemplate.LKD_30) {
                    try {
                        sendAllCharServer(
                                -1,
                                MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi dùng bóng vàng")
                        );
                    } catch (IOException var14) {
                    }
                }
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.potions[idgift[index]] = p.potions[idgift[index]] + soluong;
                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                if (idgift[index] == 9) {
                    try {
                        sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được nhân sâm khi bắn pháo hoa"));
                    } catch (IOException var13) {
                    }
                }
            }

            Database.instance.saveOrtherLog("", p.charname, info, "dotphao");
        } catch (Exception var16) {
        }
    }




    public static void setInfoShopRuongMayMan(String info) {
        String[] data1 = Char.split(info, "|");
        String[] data = Char.split(data1[0], ",");

        for (int i = 0; i < data.length; i++) {
            TOTAL_ITEM_RUONG_MAY_MAN[i] = Byte.parseByte(data[i]);
        }

        data = Char.split(data1[1], ",");

        for (int i = 0; i < data.length; i++) {
            TOTAL_DONG_XU_MAY_MAN[i] = Byte.parseByte(data[i]);
        }

        timeWaitSellRuongMM = Long.parseLong(data1[2]);
        if (timeWaitSellRuongMM == 0L) {
            timeWaitSellRuongMM = System.currentTimeMillis() + 1800000L;
        }

        ALL_RUONG_MAY_MAN = Short.parseShort(data1[3]);
        int idtrung = Integer.parseInt(data1[4]);
        if (idtrung > -1) {
            ID_RUONG_MAY_MAN_TRUNG = idtrung;
        }
    }

    public static long getTimeOpenSellruongMM() {
        if (timeSellRuongMayMan > 0L && timeSellRuongMayMan - System.currentTimeMillis() <= 0L) {
            genTrungRuongMayMan();
            Database.instance.updateShopRuongMM();
            Database.instance.saveOrtherLog("", "admi", "het gio ban ruong, reset luot moi", "hetgio");
        }

        long time = (timeWaitSellRuongMM - System.currentTimeMillis()) / 1000L;
        if (time <= 0L) {
            if (timeSellRuongMayMan == 0L) {
                timeSellRuongMayMan = System.currentTimeMillis() + 3600000L;
                if (TeamServer.isServerLocal()) {
                    timeSellRuongMayMan = System.currentTimeMillis() + 120000L;
                }
            }

            return 0L;
        } else {
            return time;
        }
    }

    public static String getInfoSaveShopRuongMM() {
        String s = String.valueOf(TOTAL_ITEM_RUONG_MAY_MAN[0]);

        for (int i = 1; i < TOTAL_ITEM_RUONG_MAY_MAN.length; i++) {
            s = s + "," + TOTAL_ITEM_RUONG_MAY_MAN[i];
        }

        String s1 = String.valueOf(TOTAL_DONG_XU_MAY_MAN[0]);

        for (int i = 1; i < TOTAL_DONG_XU_MAY_MAN.length; i++) {
            s1 = s1 + "," + TOTAL_DONG_XU_MAY_MAN[i];
        }

        return s + "|" + s1 + "|" + timeWaitSellRuongMM + "|" + ALL_RUONG_MAY_MAN + "|" + ID_RUONG_MAY_MAN_TRUNG;
    }

    public static int getIDDongXu() {
        int index = Char.getIndexRandom(TOTAL_DONG_XU_MAY_MAN);
        if (index > -1) {
            TOTAL_DONG_XU_MAY_MAN[index]--;
            Database.instance.updateShopRuongMM();
            return ID_DONG_XU[index];
        } else {
            return -1;
        }
    }

    public static void genTrungRuongMayMan() {
        ID_RUONG_MAY_MAN_TRUNG = UtilKPAH.getRandomMinMax(r.nextInt(MAX_RUONG_MM_SELL), MAX_RUONG_MM_SELL) + 1;
        ALL_RUONG_MAY_MAN = MAX_RUONG_MM_SELL;
        timeWaitSellRuongMM = System.currentTimeMillis() + 7200000L;
        if (TeamServer.isServerLocal()) {
            timeWaitSellRuongMM = System.currentTimeMillis() + 120000L;
        }

       
        timeSellRuongMayMan = 0L;
        Database.instance.saveOrtherLog("", "admin", "id ruong trung " + ID_RUONG_MAY_MAN_TRUNG, "idruongmm");
    }

    public static void doOpenRuongMayMan(Char p, Item it) {
        try {
            if (it.id_dongxu_trung > -1) {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(it.id_dongxu_trung);
                Item newItem = null;
                newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                newItem.id = p.getIDItem();
                newItem.owner = p.charDBID;
                newItem.level = newItem.getTemplate().level;
                newItem.identify = p.charDBID;
                newItem.dateCreate = Char.getDayTime(0L);
                newItem.lock = 1;
                p.iItems.add(newItem);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                Database.instance.saveOrtherLog("", p.charname, "mo ruong may man trung " + newItem.getName(), "trungdx");
                genTrungRuongMayMan();
                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Chúc mừng " + p.charname + " mở rương may mắn trúng " + newItem.getName()));
                Database.instance.updateShopRuongMM();
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName(), ""));
            } else {
                int idtem = GemTemplate.ID_MATERIAL_LOW[4][r.nextInt(GemTemplate.ID_MATERIAL_LOW[4].length)];
                p.doAddGemItem(idtem, 5, false);
                doCreateHuyetLinhThao(p, 0);
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được Huyết linh thảo và 5 " + gemTemplate[idtem].name, ""));
                Database.instance.saveOrtherLog("", p.charname, "mo ruong may man nhan dc Huyet linh thao + 5 " + gemTemplate[idtem].name, "trungdx");
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public static boolean checkTimeBuyRuongMM() {
        return false;
    }

    public static synchronized void doBuyRuongMayMan(Char p, int minute) {
    }

    public void doBuyGayGiangSinh(Char p, int minute) {
        try {
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(728);
            Item newItem = null;
            newItem = new Item(template, false, template.clazz, template.clazz, template.id);
            newItem.id = p.getIDItem();
            newItem.owner = p.charDBID;
            newItem.level = newItem.getTemplate().level;
            newItem.identify = p.charDBID;
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
            newItem.createAttributeVuKhiThoiTrang();
            newItem.dateCreate = Char.getDayTime(0L);
            p.iItems.add(newItem);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }


    private void doOpenHotMutTo(Char player) {
        try {
            String info = "";
            if (r.nextInt(100) < 70) {
                int[] id = new int[]{75, 8, 116, 94, 96};
                int[] sl = new int[]{1, 1, 1, 50, 50};
                int idpt = r.nextInt(id.length);
                int soluong = sl[idpt];
                if (idpt > 2) {
                    soluong += r.nextInt(50);
                }

                player.potions[id[idpt]] = player.potions[id[idpt]] + soluong;
                info = "Bạn nhận được " + soluong + " " + LoginHandler.PORTION_NAME[id[idpt]];
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else {
                int pc = r.nextInt(100);
                int soluong = 1;
                if (pc < 50) {
                    int[] id = new int[]{238, 239, 240};
                    int idgem = id[r.nextInt(id.length)];
                    player.doAddGemItem(idgem, soluong, true);
                    info = "Bạn nhận được " + soluong + " " + gemTemplate[idgem].name;
                } else {
                    if (pc < 95) {
                        String[] menu = new String[]{"", "", "", "", ""};

                        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                            menu[i] = gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                        }

                        player.potions[119]++;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.idNglieuDoi = 1;
                        player.sendMessage(MessageCreator.createMsgMenu(menu, -511, 0));
                        return;
                    }

                    player.doAddGemItem(66, soluong, true);
                    info = "Bạn nhận được " + soluong + " " + gemTemplate[11].name;
                }

                player.sendMessage(MessageCreator.createCharGemItem(player));
            }

            Database.instance.saveOrtherLog("", player.charname, info, "hopmutnho");
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }





    private void dousebanhchung(Char player) {
        try {
            addXpCharEvent(player, 200000L, false, "dousebanhchung");
            int[] idbot = new int[]{247, 248, 249};
            int[] slmin = new int[]{36, 8, 2};
            int rd = r.nextInt(idbot.length);
            int sl = slmin[rd];
            boolean lock = true;
            String info = "Chúc mừng bạn nhận được " + sl + " " + gemTemplate[idbot[rd]].name + " " + (lock ? "khoá" : "");
            player.doAddGemItem(idbot[rd], sl, lock);
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharGemItem(player));
            Database.instance.saveOrtherLog("", player.charname, info, "anbc");
        } catch (Exception var8) {
        }
    }

    public void doUseGoldKey(Char player) {
        if (Char.isSuKienHaloween2016()) {
            Region rg = null;
            if (this.nRegion > 0) {
                rg = (Region) this.ALL_REGION.get(player.region);
                if (rg.total_cuongthi >= 5) {
                    player.potions[144]++;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Khu vực này đã có quá nhiều cương thi.", ""));
                    return;
                }
            } else if (this.total_cuongthi >= 5) {
                player.potions[144]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Khu vực này đã có quá nhiều cương thi.", ""));
                return;
            }

            if (player.region == 0
                    && !this.isMapOffline
                    && this.mapIDLoadMap != 201
                    && this.mapIDLoadMap != 202
                    && this.mapIDLoadMap != 203
                    && this.mapIDLoadMap != 204
                    && this.mapIDLoadMap != 0
                    && this.mapIDLoadMap != 70
                    && this.mapIDLoadMap != 80) {
                int[] idTemplate = new int[]{57, 76, 81, 56, 118};
                int level = (player.lvDetail.lv - 40) / 10;
                int index = level < 0 ? 0 : level;
                MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
                CuongThi ct = new CuongThi(this, (MonsterTemplate) monsterTemplates.get(idGhost), player.x, player.y, player.inCountry);
                ct.setInfo(player.lvDetail.lv, mt.maxhp * 12, mt.rcvXp * 2);
                ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                ct.attack = mt.attack + 1000;
                ct.defend_physic = mt.defend + 1500;
                ct.defend_magic = mt.defend + 1500;
                ct.isDead = true;
                if (TeamServer.isServerLocal()) {
                    ct.maxhp = ct.hp = 1;
                }

                ct.bornTime = System.currentTimeMillis() + 1000L;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                ct.he = he[r.nextInt(5)];
                if (this.nRegion > 0) {
                    ct.idregion = (byte) player.region;
                    rg.total_cuongthi++;
                } else {
                    this.total_cuongthi++;
                }

                this.addMonsterDynamic(ct, player.inCountry, player.region);
                Database.instance.saveOrtherLog("", player.charname, "Goi boss cuong thi tai " + this.mapIDLoadMap + " " + this.mapId, "goiboss");
            } else {
                player.potions[144]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng sao vàng tại vị trí này", ""));
            }
        }
    }

    public static void loadThungGo() {
        if (Char.isSuKienHe2017() || Char.isSuKienMiniChucNu()) {
            for (int j = 0; j < ID_MAP_THUNG_GO.length; j++) {
                Map m = (Map) RealController.mapList.get(ID_MAP_THUNG_GO[j][0]);

                try {
                    for (int i = 0; i < 2; i++) {
                        ThungGo ct = new ThungGo(m, (MonsterTemplate) monsterTemplates.get(131), ID_MAP_THUNG_GO[j][1] * 16, ID_MAP_THUNG_GO[j][2] * 16, i);
                        ct.setInfo(1, 1, 1);
                        ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                        ct.attack = 0;
                        ct.idregion = 0;
                        ct.inCountry = (byte) i;
                        ct.region = 0;
                        ct.defend_physic = 0;
                        ct.defend_magic = 0;
                        ct.isDead = true;
                        ct.bornTime = System.currentTimeMillis() + 5000L;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        ct.he = he[r.nextInt(5)];
                        m.addMonsterDynamic(ct, i, 0);
                    }
                } catch (Exception var5) {
                    var5.printStackTrace();
                }
            }
        }
    }

    public void doAddThungGo(Char player) {
        Region rg = null;
        if (player.region == 0
                && !this.isMapOffline
                && this.mapIDLoadMap != 201
                && this.mapIDLoadMap != 202
                && this.mapIDLoadMap != 203
                && this.mapIDLoadMap != 204
                && this.mapIDLoadMap != 0
                && this.mapIDLoadMap != 70
                && this.mapIDLoadMap != 80) {
            try {
                int[] idTemplate = new int[]{57, 76, 81, 56, 118};
                int level = (player.lvDetail.lv - 40) / 10;
                int index = level < 0 ? 0 : level;
                MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
                ThungGo ct = new ThungGo(this, (MonsterTemplate) monsterTemplates.get(131), player.x, player.y, player.inCountry);
                ct.setInfo(player.lvDetail.lv, 1, mt.rcvXp * 2);
                ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                ct.attack = 0;
                ct.idregion = (byte) player.region;
                ct.inCountry = player.inCountry;
                ct.region = player.region;
                ct.defend_physic = 0;
                ct.defend_magic = 0;
                ct.isDead = false;
                ct.bornTime = System.currentTimeMillis() + 1000L;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                ct.he = he[r.nextInt(5)];
                ct.idregion = (byte) player.region;
                ct.region = player.region;
                this.addMonsterDynamic(ct, player.inCountry, player.region);
                Database.instance.saveOrtherLog("", player.charname, "Goi boss cuong thi tai " + this.mapIDLoadMap + " " + this.mapId, "goiboss");
            } catch (Exception var9) {
                var9.printStackTrace();
            }
        } else {
            player.potions[105]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng sao vàng tại vị trí này", ""));
        }
    }

    public static void loadRuongMaQuai() {
        Map m = (Map) RealController.mapList.get(2);

        try {
            for (int i = 0; i < 2; i++) {
                int[] idTemplate = new int[]{57, 76, 81, 56, 118};
                int level = 1;
                int index = level < 0 ? 0 : level;
                MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
                RuongMaQuai ct = new RuongMaQuai(m, (MonsterTemplate) monsterTemplates.get(121), 720, 160, i);
                ct.setInfo(level, TeamServer.isServerLocal() ? 10 : 3000, 1);
                ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                ct.attack = 0;
                ct.idregion = 0;
                ct.inCountry = (byte) i;
                ct.region = 0;
                ct.defend_physic = 0;
                ct.defend_magic = 0;
                ct.isDead = true;
                ct.bornTime = System.currentTimeMillis() + 1000L;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                ct.he = he[r.nextInt(5)];
                m.addMonsterDynamic(ct, i, 0);
                if (i == 0) {
                    ruong_ma_quai_thanh_long = ct;
                } else if (i == 1) {
                    ruong_ma_quai_hac_ho = ct;
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    public void doAddRuongMaQuai(Char player) {
        Region rg = null;
        if (player.region == 0
                && !this.isMapOffline
                && this.mapIDLoadMap != 201
                && this.mapIDLoadMap != 202
                && this.mapIDLoadMap != 203
                && this.mapIDLoadMap != 204
                && this.mapIDLoadMap != 0
                && this.mapIDLoadMap != 70
                && this.mapIDLoadMap != 80) {
            try {
                int[] idTemplate = new int[]{57, 76, 81, 56, 118};
                int level = (player.lvDetail.lv - 40) / 10;
                int index = level < 0 ? 0 : level;
                MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
                RuongMaQuai ct = new RuongMaQuai(this, (MonsterTemplate) monsterTemplates.get(121), player.x, player.y, player.inCountry);
                ct.setInfo(player.lvDetail.lv, 1, mt.rcvXp * 2);
                ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
                ct.attack = 0;
                ct.idregion = (byte) player.region;
                ct.inCountry = player.inCountry;
                ct.region = player.region;
                ct.defend_physic = 0;
                ct.defend_magic = 0;
                ct.isDead = true;
                ct.bornTime = System.currentTimeMillis() + 1000L;
                byte[] he = new byte[]{0, 1, 2, 3, 4};
                ct.he = he[r.nextInt(5)];
                ct.idregion = (byte) player.region;
                ct.region = player.region;
                this.addMonsterDynamic(ct, player.inCountry, player.region);
                Database.instance.saveOrtherLog("", player.charname, "Goi boss cuong thi tai " + this.mapIDLoadMap + " " + this.mapId, "goiboss");
            } catch (Exception var9) {
                var9.printStackTrace();
            }
        } else {
            player.potions[105]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng sao vàng tại vị trí này", ""));
        }
    }

    public static void removeMonsterNgocRong(LiveActor ms, int country) {
        try {
            Vector<Monster> allMonsterNgoc = all_ngoc_rong_hac_ho;
            if (country == 0) {
                allMonsterNgoc = all_ngoc_rong_thanh_long;
            }

            for (int i = 0; i < allMonsterNgoc.size(); i++) {
                Monster m = (Monster) allMonsterNgoc.get(i);
                if (m != null && m.id == ms.id && m.map.equals(ms.map)) {
                    allMonsterNgoc.remove(i);
                    return;
                }
            }
        } catch (Exception var5) {
        }
    }

    public void doAddNgocRong(int x, int y, int country, int region, int idtemplate) {
        try {
            NgocRong ct = new NgocRong(this, (MonsterTemplate) monsterTemplates.get(idtemplate), x, y, country);
            ct.setInfo(1, 1, 1);
            ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
            byte[] he = new byte[]{0, 1, 2, 3, 4};
            ct.he = he[r.nextInt(5)];
            this.addMonsterDynamic(ct, country, region);
            if (country == 0) {
                all_ngoc_rong_thanh_long.add(ct);
            } else {
                all_ngoc_rong_hac_ho.add(ct);
            }

            System.out
                    .println(
                            "ADD "
                            + ct.getName()
                            + " TAI VI TRI map: "
                            + this.mapId
                            + " "
                            + this.mapIDLoadMap
                            + " , country: "
                            + country
                            + ", REGION: "
                            + region
                            + " , VI TRI: "
                            + x / 16
                            + " , "
                            + y / 16
                    );
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    public void doAddBossTest(Char player, int idtemplate) {
        Region rg = null;
        int[] idTemplate = new int[]{57, 76, 81, 56, 118};
        int level = (player.lvDetail.lv - 40) / 10;
        int index = level < 0 ? 0 : level;
        MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
        BossSkelonton ct = new BossSkelonton(this, (MonsterTemplate) monsterTemplates.get(idtemplate), player.x, player.y, player.inCountry);
        ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
        ct.attack = mt.attack + 1000;
        ct.defend_physic = mt.defend + 1500;
        ct.defend_magic = mt.defend + 1500;
        byte[] he = new byte[]{0, 1, 2, 3, 4};
        ct.he = he[r.nextInt(5)];
        this.addMonsterDynamic(ct, player.inCountry, player.region);
    }

    public void doAddNgocRongTest(Char player, int idtemplate) {
        Region rg = null;
        int[] idTemplate = new int[]{57, 76, 81, 56, 118};
        int level = (player.lvDetail.lv - 40) / 10;
        int index = level < 0 ? 0 : level;
        MonsterTemplate mt = (MonsterTemplate) monsterTemplates.get(idTemplate[index]);
        NgocRong ct = new NgocRong(this, (MonsterTemplate) monsterTemplates.get(idtemplate), player.x, player.y, player.inCountry);
        ct.setInfo(player.lvDetail.lv, mt.maxhp * 12, mt.rcvXp * 2);
        ct.id = RealController.intance.idGen.getID(1, "TAO GHOST");
        ct.attack = mt.attack + 1000;
        ct.defend_physic = mt.defend + 1500;
        ct.defend_magic = mt.defend + 1500;
        byte[] he = new byte[]{0, 1, 2, 3, 4};
        ct.he = he[r.nextInt(5)];
        this.addMonsterDynamic(ct, player.inCountry, player.region);
        Database.instance.saveOrtherLog("", player.charname, "Goi boss cuong thi tai " + this.mapIDLoadMap + " " + this.mapId, "goiboss");
    }

    private void doUseNen(Char player) {
        Item it = this.dropItemAnimalByLvChar(player.lvDetail.lv, 4);
        it = ItemChangeColor.createItemColorAnimal(it, Item.COLOR_GREEN, 1);
        it.owner = player.charDBID;
        it.id = player.getIDItem();
        it.identify = player.charDBID;
        it.dateCreate = Char.getDayTime(0L);
        player.iItems.add(it);
        int idgem = GemTemplate.ID_MATERIAL_HIGHT[5][r.nextInt(GemTemplate.ID_MATERIAL_HIGHT[5].length)];
        player.doAddGemItem(idgem, 1, true);
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + it.getName() + " và " + gemTemplate[idgem].name, ""));
        Database.instance
                .saveOrtherLog("", player.charname, it.getName() + "_" + it.getAttribute() + "_" + it.getDBInfo() + " ng: " + gemTemplate[idgem].name, "unen");
    }

    void doSummons(Char player) {
        try {
            if (!player.dayLogin.equals(Char.getDayOpen(0L))) {
                player.usingSummons = 0;
                player.dayLogin = Char.getDayOpen(0L);
            }

            if (player.usingSummons == 2) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Một ngày bạn chỉ có thể triệu hồi được 2 lần.", ""));
                return;
            }

            if (player.inCountry != player.myCountry && nationBeAttack[player.inCountry] != player.myCountry) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể triệu hồi tại đây.", ""));
                return;
            }

            for (int i = 0; i < CharManager.instance.charName.size(); i++) {
                try {
                    Char p = (Char) CharManager.instance.vChars.elementAt(i);
                    if (p != null && p.myCountry == player.myCountry && p.id != player.id && p.myCountry == player.myCountry && p.lvDetail.lv >= 40) {
                        p.sendMessage(MessageCreator.createMsgPopUp(p.id, 6, "Đức vua triệu hồi bảo vệ lãnh thổ. Bạn có muốn tham gia không ?"));
                    }
                } catch (Exception var4) {
                    var4.printStackTrace();
                }
            }

            player.usingSummons++;
        } catch (Exception var5) {
        }
    }

    void doSelectPosBuildLienHoa(Char player) {
        player.sendMessage(
                MessageCreator.createMsgMenu(
                        new String[]{"Xây tại long trụ phụ trên", "Xây tại long trụ phụ dưới", "Xây tại long trụ phụ trái", "Xây tại long trụ phụ phải"}, -503, 0
                )
        );
    }

    void doPhongChuc(Char player) {
        if (player.idClan != -1) {
            if (player.rankGov > 1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có vua hoặc đại thần mới có thể phong chức.", ""));
            } else {
                String[][] menu = new String[][]{
                    {"Phong chức Đại thần", "Phong chức Tướng quân", "Phế Đại thần", "Phế Tướng quân"}, {"Phong chức Bộ đầu", "Phế Bộ đầu"}
                };
                player.sendMessage(MessageCreator.createMsgMenu(menu[player.rankGov], -501, 0));
            }
        }
    }



    private void doGiveGif83(Char player) {
        try {
            if (player.listSpItem[0] == 0) {
                ShopTemplate item = getShopTemplate(0);
                ShopTemplate spItem = new ShopTemplate();
                spItem.coppy(spItem, item);
                spItem.ownerId = player.charDBID;
                spItem.realId = player.getIDItem();
                player.specialItem.add(spItem);
            }

            player.listSpItem[0]++;
            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng, bạn đã nhận được 1 vé giờ vàng 100%", ""));
        } catch (Exception var4) {
        }
    }

    public static boolean isWeapone(int type) {
        return type >= 3 && type <= 7;
    }

    boolean doUserUpLevelItem(Char player, Message message) {
        try {
            if (player.infoItemCreate == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
                return false;
            }
            Item item = player.infoItemCreate.item;
            if (item != null) {
                // player.infoItemCreate.item la tham chieu duoc giu tu luc "chon vat pham"
                // (option 15, mo menu -502) qua nhieu goi tin, khong bao gio duoc xac thuc
                // lai. Neu giua chung mon do bi MAC LEN NGUOI (MapMuaBan.exchangeItem rut no
                // khoi iItems dua vao o trang bi) thi cac kiem duoi day (level/isSelling/lock)
                // van qua vi day la thuoc tinh cua doi tuong Item, khong doi khi doi cho -
                // doCreateNewItem van tao mon cap tren MOI, roi "player.iItems.remove(item)"
                // o cuoi ham la no-op (mon dang o trang bi, khong o iItems) nen mon GOC van
                // con nguyen tren nguoi: nhan doi trang bi. Bat buoc con nam trong iItems.
                if (!player.iItems.contains(item)) {
                    player.infoItemCreate = null;
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
                    return false;
                }
                // Vu khi (kiem/dao/but/bua/cung) la LOAI DUY NHAT co level = 81 (level
                // trang bi/trang suc khac deu la boi so cua 5: 20,25,...,80,85 nen khong
                // bao gio dung 81, cho nay khong doi hanh vi cua chung).
                if (item.level >= 86) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể nâng cấp thêm", ""));
                    return false;
                }

                if (item.isSelling) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                    return false;
                }

                if (item.lock == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể thăng cấp trang bị đã khoá", ""));
                    return false;
                }

                if (bacTrenKhongHopLe(player, item)) {
                    return false;
                }

                int[][] var10000 = new int[][]{
                    {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 467, 468, 469, 470, 471, 472, 473, 474},
                    {35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 475, 476, 477, 478, 479, 480, 481, 482},
                    {61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 483, 484, 485, 486, 487, 488, 489, 490},
                    {83, 84, 85, 174, 175, 176, 177, 178, 179, 180, 181},
                    {90, 91, 92, 182, 183, 184, 185, 186, 187, 188, 189},
                    {97, 98, 99, 190, 191, 192, 193, 194, 195, 196, 197},
                    {104, 105, 106, 198, 199, 200, 201, 202, 203, 204, 205},
                    {111, 112, 113, 206, 207, 208, 209, 210, 211, 212, 213},
                    {117, 118, 119, 120, 121, 122, 123, 124, 125, 499, 500, 501, 502},
                    {129, 130, 131, 132, 133, 134, 135, 136, 137, 503, 504, 505, 506},
                    {141, 142, 143, 144, 145, 146, 147, 148, 149, 491, 492, 493, 494},
                    {153, 154, 155, 156, 157, 158, 159, 160, 161, 495, 496, 497, 498},
                    {165, 166, 167, 168, 169, 170, 171, 172, 173, 569, 570, 571, 572}
                };
                int var10007 = item.level + 5;
                Item newItem = this.doCreateNewItem(
                        player,
                        message,
                        item.getIdTemplateUpLevel(),
                        item.getTemplate().gender,
                        item.clazz,
                        item.getTemplate().type,
                        var10007,
                        item.magic_physic,
                        item.plus,
                        1,
                        item,
                        true
                );
                if (newItem == null) {
                    return false;
                }

                newItem.heItem = (byte) r.nextInt(5);
                item.initAtt(null);
                Database.instance.deleteItem(item.dbid);
                player.iItems.remove(item);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                Database.instance
                        .saveOrtherLog("tob_log_other_item", player.charname, item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "uplvNM");
                return true;
            }
        } catch (Exception var6) {
        }
        return false;
    }

    /**
     * "Bac tren" cua mon do co THAT SU la mot bac tren hop le khong.
     *
     * PHAI GOI TRUOC MOI LAN THANG CAP. Tra ve true (va da bao loi cho nguoi choi) khi
     * KHONG hop le - nguoi goi phai dung lai ngay, khong duoc dung toi doCreateNewItem.
     *
     * VI SAO CAN (loi da xay ra that, 05/09/2026): Item.getIdTemplateUpLevel() khi cot
     * data_item.idUpLevel chua dat (= -1) thi DOAN bang "id + 1". Cong thuc do dung cho
     * cac bac thap vi chung xep lien nhau (kiem 400..412), NHUNG dut han o bac 81 - moi
     * dong vu khi la mot khoi 13 id lien tiep roi sang khoi cua mon phai KHAC:
     *
     *     412 Pha giap kiem (kiem, lv81) + 1 = 413 Tram ma dao   (DAO,  lv21)
     *     425 Gia cat dao   (dao,  lv81) + 1 = 426 Can quan but  (BUT,  lv21)
     *     438 Y to but      (but,  lv81) + 1 = 439 Thon nhat bua (BUA,  lv21)
     *     451 Vien dinh bua (bua,  lv81) + 1 = 452 Ke vi cung    (CUNG, lv21)
     *     464 Anh vu cung   (cung, lv81) + 1 = 465 Cuoc thuong   (CUOC, lv1!)
     *
     * Khong co chot nay thi bon dong dau AM THAM AN MAT vu khi lv81 cua nguoi choi va tra
     * lai mot mon lv21 SAI CA MON PHAI, kem thong bao "Chuc mung" - mat do that su, khong
     * hoan tac duoc. Rieng dong cuoi (Cuoc thuong lv1) con lam getPointAddForRank tinh
     * pcADD2RankItem[(1-20)/5] = chi so -3 -> ArrayIndexOutOfBounds, bi catch rong nuot,
     * doCreateNewItem tra null va KHONG gui goi tin nao -> client treo o hop thoai
     * "Xin chờ..." (dung trieu chung nguoi choi bao).
     *
     * Dat idUpLevel dung trong data_item la sua duoc goc, nhung KHONG DUOC trong cay vao
     * do: co so du lieu that co the chua chay lenh cap nhat, hoac chay roi ma chua khoi
     * dong lai may chu (Map.loadAllTemplate chi doc data_item MOT LAN luc khoi dong, nen
     * ban trong RAM van la -1). Chot o day dung trong moi truong hop do.
     */
    private boolean bacTrenKhongHopLe(final Char player, final Item item) {
        ItemTemplates bacTren = null;
        try {
            bacTren = (ItemTemplates) ((Hashtable) itemTemplates.get(5))
                    .get(Integer.valueOf(item.getIdTemplateUpLevel()));
        } catch (final Exception ex) {
        }
        final int typeCu = item.getTemplate().type;
        // Bac tren phai: co that, CUNG loai do (khong the kiem hoa dao), va DUNG hon mot
        // bac (5 cap). Sai bat ky dieu nao deu la "chua cau hinh", khong phai bac tren.
        if (bacTren != null && bacTren.type == typeCu && bacTren.level == item.level + 5) {
            return false;
        }
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Vật phẩm này chưa được cấu hình bậc kế tiếp nên chưa nâng cấp được. "
                + "Báo quản trị viên đặt cột idUpLevel cho vật phẩm #"
                + item.getTemplate().id + " (" + item.getTemplate().name + ").", ""));
        return true;
    }

    boolean doUserUpLevelItemSpecial(Char player, Message message) {
        try {
            if (player.infoItemCreate == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
                return false;
            }
            Item item = player.infoItemCreate.item;
            if (item != null) {
                // Xem chu thich day du o doUserUpLevelItem (cung mot dang tham chieu treo,
                // va cung ly do tran 86 chu khong phai 81 nua).
                if (!player.iItems.contains(item)) {
                    player.infoItemCreate = null;
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
                    return false;
                }
                if (item.level >= 86) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể nâng cấp thêm", ""));
                    return false;
                }

                if (item.isSelling) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                    return false;
                }

                if (item.lock == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể thăng cấp trang bị đã khoá", ""));
                    return false;
                }

                if (bacTrenKhongHopLe(player, item)) {
                    return false;
                }

                int var10007 = item.level + 5;
                Item newItem = this.doCreateNewItem(
                        player,
                        message,
                        item.getIdTemplateUpLevel(),
                        item.getTemplate().gender,
                        item.clazz,
                        item.getTemplate().type,
                        var10007,
                        item.magic_physic,
                        item.plus,
                        2,
                        item,
                        true
                );
                if (newItem == null) {
                    System.out.println("THANG CAP THAT BAI");
                    return false;
                }

                for (int i = 28; i < Item.MAX_ATT; i++) {
                    newItem.atb[i] = item.atb[i];
                }

                newItem.idNguHanh2 = item.idNguHanh2;

                for (int i = 0; i < Item.MAX_ATT_KHAM; i++) {
                    newItem.atb[i + 10] = item.atb[i + 10];
                }

                newItem.nSocAdd = item.nSocAdd;
                newItem.nhadSock = item.nhadSock;
                Database.instance.deleteItem(item.dbid);
                player.iItems.remove(item);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                Database.instance
                        .saveOrtherLog("tob_log_other_item", player.charname, item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "uplvSP");
                return true;
            }
        } catch (Exception var6) {
        }
        return false;
    }




    void doSendIfoMaterial2Client(Char player, int type) {
        // Kiem tra truoc khi truy cap: ban cu doc .item.id o dong dau roi moi
        // kiem tra item == null ben duoi, nen da NPE tu truoc khi kip bao loi.
        if (player.infoItemCreate == null || player.infoItemCreate.item == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hãy chọn lại vật phẩm cần nâng cấp.", ""));
            return;
        }
        short itemID = player.infoItemCreate.item.id;
        Item item = player.infoItemCreate.item;
        if (item == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại.", ""));
        } else if (!isWearingAnimal(item.getTemplate().type)) {
            if (item.isSelling) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm đang được bán.", ""));
            } else if (item.colorName != 0 && item.colorName != 4) {
                if (type == 5 && !isWeapone(item.getType()) && item.getType() != 8 && item.getType() != 9) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cho vật phẩm này.", ""));
                } else {
                    int typeItem = item.getTemplate().type;
                    player.sendMessage(
                            MessageCreator.createMsgInfoMaterialClothing(
                                    item,
                                    typeItem,
                                    isWeapone(typeItem) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                                    item.magic_physic,
                                    type + 1,
                                    player.infoItemCreate.materialUse
                            )
                    );
                }
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cho vật phẩm này.", ""));
            }
        }
    }

    void doSendInfoMaterial2Client(Char player, Message message, int type) {
        try {
            short itemID = message.dis.readShort();
            Item item = player.getItemFormVector(player.iItems, itemID);
            if (item == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại.", ""));
                return;
            }

            if (isWearingAnimal(item.getTemplate().type)) {
                return;
            }

            if (item.isSelling) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm đang được bán.", ""));
                return;
            }

            if (item.colorName == 0 || item.colorName == 4) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cho vật phẩm này.", ""));
                return;
            }

            if (type == 5 && !isWeapone(item.getType()) && item.getType() != 8 && item.getType() != 9) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cho vật phẩm này.", ""));
                return;
            }

            int typeItem = item.getTemplate().type;
            player.infoItemCreate = new InfoItemCreate();
            player.infoItemCreate.item = item;
            player.infoItemCreate.typeOption = (byte) type;
            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}, -502, 0));
        } catch (Exception var7) {
        }
    }

    Item doCreateNewItem(
            Char player,
            Message message,
            int idTemplate,
            int gender,
            int charClass,
            int typeItem,
            int levelItem,
            int magic_physic,
            int plusItem,
            int option,
            Item oldItem,
            boolean uplv
    ) {
        Item item = null;

        try {
            if (player.isFullInventory()) {
                // lo #5 loi.xlsx: truoc day khong kiem tra day hanh trang o day (chi co
                // o doCreateItem - ham MO MENU, khong phai ham XAC NHAN tao do nay) -
                // van tru xu/nguyen lieu va cong duoc do vao iItems du da day, nhung do
                // do chi ton tai trong RAM (xem sua o duoi, saveItem cu la no-op) nen
                // mat khi ngat ket noi giua chung.
                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy, không thể tạo thêm đồ", ""));
                return null;
            }

            if (!this.enoughMaterial(
                    player, typeItem, isWeapone(typeItem) ? (levelItem - 21) / 5 : (levelItem - 20) / 5, magic_physic, option, player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return null;
            }

            int templateItem = message.dis.readShort();
            if (templateItem != idTemplate) {
                return null;
            }

            int n = 2;
            int[][] material = new int[n][];
            material[0] = new int[6];
            material[1] = new int[6];

            for (int i = 0; i < 6; i++) {
                material[0][i] = message.dis.readByte();
            }

            for (int i = 0; i < 6; i++) {
                material[1][i] = message.dis.readByte();
            }

            if (MapEpNgoc.chanSoLuongAm(player, material, "doCreateNewItem")) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng nguyên liệu không hợp lệ", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return null;
            }

            if (!this.enoughMaterial(
                    player,
                    typeItem,
                    isWeapone(typeItem) ? (levelItem - 21) / 5 : (levelItem - 20) / 5,
                    magic_physic,
                    material[0],
                    material[1],
                    option,
                    player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu2", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return null;
            }

            byte[] id = CreateItemTemplate.getIDMAterial(typeItem, magic_physic, 0);
            Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(typeItem >= 3 && typeItem <= 7 ? (levelItem - 21) / 5 : (levelItem - 20) / 5, option);
            if (soluong.size() == 0) {
                return null;
            }

            int[][] perc = getPercentCreateItem(0, (Byte) soluong.get(0), material[0], (Byte) soluong.get(1), material[1]);

            for (int i = 0; i < perc.length; i++) {
                int j = 0;

                while (j < perc[i].length) {
                    j++;
                }
            }

            if (perc == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Nguyên liệu sử dụng không hợp lệ", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return null;
            }

            String[] nameItem = new String[]{"Trắng", "Xanh", "Đỏ", "Hoàn mỹ"};
            int[] colorName = new int[]{0, 3, 2, 1};
            int pc = r.nextInt(100);
            int tempPC = 110;
            int posPC = 0;
            int i = 0;

            for (String nItem = ""; i < 4; i++) {
                int max = 0;
                int posMax = 0;

                for (int j = 0; j < 4; j++) {
                    if (perc[1][j] > 0 && perc[1][j] < tempPC && perc[1][j] > max) {
                        max = perc[1][j];
                        posMax = j;
                    }
                }

                posPC += max;
                tempPC = max;
                if (pc <= posPC) {
                    nItem = nameItem[posMax];
                    if (typeItem >= 3 && typeItem <= 7) {
                        item = MapCheTao.createItemWeapone(this, player, posMax, idTemplate, charClass, levelItem, plusItem);
                    } else {
                        item = MapCheTao.createItemArmor(this, player, posMax, idTemplate, charClass, levelItem, plusItem, typeItem, gender);
                    }

                    item.level = item.getTemplate().level;
                    item.magic_physic = (byte) magic_physic;
                    if (item.getTemplate().type < 3 || item.getTemplate().type == 10 || item.getTemplate().type == 11) {
                        if (magic_physic == 0) {
                            item.atb[6] = item.atb[1];
                            item.atb[1] = (short) (item.atb[1] / 10);
                        } else if (magic_physic == 1) {
                            item.atb[6] = (short) (item.atb[1] / 10);
                        }
                    }

                    item.colorName = (byte) colorName[posMax];
                    item.heItem = (byte) r.nextInt(5);
                    if (oldItem != null) {
                        item.heItem = oldItem.heItem;
                    }

                    String hangitem = "";
                    if (posMax > 0) {
                        int idAtt = r.nextInt(5);
                        int[] pcAtt = getPercentAppearAtt((Byte) soluong.get(0), material[0], posMax, material[1][5] > 0 ? material[1][5] : 0);
                        System.out.print(pcAtt[0] + "," + pcAtt[1] + "," + pcAtt[2] + "," + pcAtt[3] + "," + pcAtt[4]);
                        String[] nameAtt = new String[]{"", "Nhất phẩm", "Nhị phẩm", "Tam phẩm", "Tứ phẩm", "Ngũ phẩm"};
                        int[] idOfAtt = new int[]{5, 4, 3, 2, 1};
                        int pcatt = r.nextInt(100);
                        int tempPCAtt = -100;
                        int posPCAtt = 0;

                        for (int l = 0; l < 5; l++) {
                            int maxAtt = 200;
                            int posMaxAtt = 0;

                            for (int jx = 0; jx < 5; jx++) {
                                if (pcAtt[jx] > 0 && pcAtt[jx] > tempPCAtt && pcAtt[jx] < maxAtt) {
                                    maxAtt = pcAtt[jx];
                                    posMaxAtt = jx;
                                }
                            }

                            posPCAtt += maxAtt;
                            tempPCAtt = maxAtt;
                            if (pcatt <= posPCAtt) {
                                item.hangItem = (byte) idOfAtt[posMaxAtt];
                                int[][][] pcAttSp = new int[][][]{
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {{5}, {4}, {3}, {2}, {1}},
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    }
                                };
                                if (idOfAtt[posMaxAtt] == 1) {
                                    pcAttSp = new int[][][]{
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {{5}, {4}, {3}, {1, 2}, new int[0]},
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        }
                                    };
                                }

                                int[] tempArr = pcAttSp[idAtt][idOfAtt[posMaxAtt] - 1];
                                item.atb[idAtt + 28] = (short) tempArr[r.nextInt(tempArr.length)];
                                hangitem = nameAtt[item.hangItem];
                                byte[][] var10000 = new byte[][]{
                                    new byte[1],
                                    {10, 11, 10, 11, 10, 10, 10, 10, 11, 10, 12, 10, 12, 10, 10, 10, 10, 10},
                                    {7, 8, 7, 8, 7, 7, 7, 7, 8, 7, 9, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7},
                                    {4, 5, 4, 5, 4, 4, 4, 4, 5, 4, 6, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4},
                                    {1, 2, 1, 2, 1, 1, 1, 2, 2, 1, 3, 1, 3},
                                    new byte[1]
                                };
                                if (uplv) {
                                    var10000 = new byte[][]{
                                        new byte[1],
                                        {10, 11, 10, 11, 10, 10, 10, 10, 11, 10, 10, 10, 12, 10, 10, 10, 10, 10, 10},
                                        {7, 8, 7, 8, 7, 7, 7, 7, 8, 7, 7, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
                                        {4, 5, 4, 5, 4, 4, 4, 4, 5, 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                                        {1, 2, 1, 2, 1, 1, 1, 2, 2, 1, 1, 1, 3},
                                        new byte[1]
                                    };
                                }

                                byte addMore = CreateItemTemplate.getAddMorGreen(item.hangItem);
                                byte pointAdd = CreateItemTemplate.getPointAddForRank(item.level, item.hangItem);
                                byte bu = CreateItemTemplate.getBuCong(item.level, item.colorName, item.hangItem, item.getTemplate().type);

                                for (int k = 0; k < 10; k++) {
                                    item.atb[k] = (short) (item.atb[k] + addMore + pointAdd + bu);
                                }
                                break;
                            }
                        }
                    }

                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Chúc mừng bạn đã tạo được " + item.getTemplate().name + " " + hangitem + " " + nameItem[posMax], "")
                    );
                    if (oldItem != null) {
                        item.lock = 1;
                    }

                    item.owner = player.charDBID;
                    item.id = player.getIDItem();
                    player.iItems.add(item);
                    item.charSeal = player.charname;
                    item.dateCreate = Char.getDayTime(0L);
                    if (item.colorName != 0 && item.colorName != 4) {
                        item.initAtt(material[1]);
                        item.changLevelSkill();
                    }

                    item.identify = player.charDBID;
                    item.lockItem(material[1]);
                    if (item.lock == 0) {
                        if (player.infoItemCreate.materialUse == 1) {
                            item.lock = 1;
                        } else if (player.infoItemCreate.materialUse != 0) { // != 0: deleteGemItem coi moi gia tri khac 0/1 la "tong hop" (uu tien tru kho khoa), phai khoa do trong CUNG dieu kien
                            try {
                                if (material[0] != null) {
                                    for (int m = 0; m < material[0].length; m++) {
                                        if (material[0][m] > 0 && player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[0]] + m] > 0) {
                                            item.lock = 1;
                                            break;
                                        }
                                    }
                                }

                                if (material[1] != null) {
                                    for (int mx = 0; mx < material[1].length; mx++) {
                                        int aa = CreateItemTemplate.startIDMaterial[id[id.length < 2 ? 0 : 1]] + mx;
                                        if (material[1][mx] > 0 && player.listGemitemLock[aa] > 0) {
                                            item.lock = 1;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception var48) {
                                var48.printStackTrace();
                            }
                        }
                    }

                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    String gemUse = MapEpNgoc.deleteGemItem(this, player, id, material, player.infoItemCreate.materialUse);
                    // lo #5 loi.xlsx: Database.saveItem(Item) la ham RONG (no-op) - do
                    // vua tao chi ton tai trong RAM cho toi lan saveCharAuto() ke tiep
                    // (kich hoat boi hanh dong KHAC nhu ban do/dung thuoc, khong phai boi
                    // chinh viec che tao), nen mat het neu ngat ket noi truoc do. Luu ngay.
                    Database.instance.saveCharAuto(player);
                    // aclog rieng "chetaotrangbi" (KHONG dung "tao" chung) - "tao" nam trong
                    // danh sach loai bo tu 02/09/2026 (xem data.LocLogVatPham) vi la nguon rac
                    // lon nhat luc do; doi ten o day de dong ghi CHI cho che tao trang bi nay
                    // duoc giu lai ma khong keo theo cac cho "tao" khac (neu co sau nay).
                    Database.instance
                            .saveOrtherLog(
                                    "tob_log_other_item",
                                    player.charname,
                                    // 16/09/2026: them "id<item.id>" de tra dung mon vua che tao
                                    // trong tui do (theo yeu cau "log chi tiet hon").
                                    "id" + item.id
                                    + "_"
                                    + gemUse
                                    + "_"
                                    + (oldItem == null ? "false" : "true")
                                    + "_"
                                    + item.getTemplate().name
                                    + " "
                                    + nItem
                                    + " "
                                    + hangitem
                                    + "_"
                                    + item.getAttribute()
                                    + "|"
                                    + item.getDBInfo(),
                                    "chetaotrangbi"
                            );
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.infoItemCreate.item = null;
                    player.infoItemCreate = null;
                    break;
                }
            }
        } catch (Exception var49) {
            var49.printStackTrace();
            return null;
        }

        try {
            if (r.nextInt(1000) < 5) {
                doCreateBookSkillPet(player, 0);
            }
        } catch (Exception var47) {
        }

        if (Char.isSuKienTrungThul2016() && r.nextInt(100) < 70) {
            player.potions[136]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            Database.instance.saveOrtherLog("", player.getName(), "nhan dc nen khi che do", "nen");
        }

        if (Char.isSuKienHaloween2016() && r.nextInt(100) < 50) {
            player.potions[144]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            Database.instance.saveOrtherLog("", player.getName(), "nhan dc sao vang khi che do", "saovang");
        }

        Char.isSuKienHe2017();
        if (Char.isSuKienGioTo2016() && r.nextInt(100) < 50) {
            player.potions[117]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            Database.instance.saveOrtherLog("", player.getName(), "nhan dc dua hau khi che do", "trungdua");
        }

        if (Char.isSuKienVuLan2015()) {
            int tlVuLan = 50;
            if (player.wModel != null && (player.wModel.isBongDaNamAoCam() || player.wModel.isBongDaNuAoSoc())) {
                tlVuLan *= 2;
            }
            if (r.nextInt(100) < tlVuLan) {
                player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.getName(), "nhan dc nhanh hoa hong vang khi che do", "vulan");
            }
        }

        return item;
    }


    Item getItem(Char player, Message message) {
        try {
            short itemID = message.dis.readShort();
            Item item = player.getItemFormVector(player.iItems, itemID);
            if (item == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại.", ""));
                return null;
            } else if (item.isSelling) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm đang được bán.", ""));
                return null;
            } else if (item.colorName != 0 && item.colorName != 4) {
                return item;
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cho vật phẩm này.", ""));
                return null;
            }
        } catch (Exception var5) {
            return null;
        }
    }

    boolean doChangeLevelSkill(Char player, Message message) {
        try {
            Item item = this.getItem(player, message);
            if (item == null) {
                return false;
            }

            if (item.isSelling) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return false;
            }

            if (!item.charSeal.toLowerCase().equals(player.charname.toLowerCase()) || item.lock == 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thay đổi", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return false;
            }

            if (!this.enoughMaterial(
                    player,
                    item.getType(),
                    isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                    item.magic_physic,
                    6,
                    player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return false;
            }

            int n = 2;
            int[][] material = new int[n][];
            material[1] = new int[6];
            boolean fail = false;

            for (int i = 0; i < 6; i++) {
                material[1][i] = message.dis.readByte();
                if (i < 3 && material[1][i] != 0 && !fail) {
                    fail = true;
                }
            }

            if (fail) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể dùng nguyên liệu cấp 4 trở lên.", ""));
                return false;
            }

            if (MapEpNgoc.chanSoLuongAm(player, material, "doChangeLevelSkill")) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng nguyên liệu không hợp lệ", ""));
                return false;
            }

            if (!this.enoughMaterial(
                    player,
                    item.getType(),
                    isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                    item.magic_physic,
                    material[0],
                    material[1],
                    6,
                    player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                return false;
            }

            item.changLevelSkill();
            player.sendMessage(MessageCreator.createServerAlertMessage("Thay đổi thành công", ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            String gemUse = MapEpNgoc.deleteGemItem(this,
                    player, CreateItemTemplate.getIDMAterial(item.getType(), item.magic_physic, 6), material, player.infoItemCreate.materialUse
            );
            Database.instance
                    .saveOrtherLog(
                            "tob_log_other_item", player.charname, gemUse + "_" + item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "skill"
                    );
            player.infoItemCreate = null;
            return true;
        } catch (Exception var8) {
        }
        return false;
    }


    private static int[] getPercentAppearAtt(int n1, int[] material, int kindItem, int material6) {
        int[] perc = new int[5];
        int total = 0;

        for (int i = 0; i < material.length; i++) {
            if (material[i] > 0) {
                total += material[i];
            }
        }

        if (total > n1) {
            return null;
        } else {
            int p1 = (100 + (material6 > 0 ? material6 * 2 : 0)) / n1;
            int p2 = 100 - p1 * n1;
            if (p2 < 0) {
                p2 = 0;
            }

            byte[][] pcTemp = new byte[][]{{100, 0, 0, 0, 0}, {80, 20, 0, 0, 0}, {60, 20, 20, 0, 0}, {10, 40, 40, 20, 0}, {0, 0, 50, 45, 5}, {0, 0, 0, 0, 100}};
            int startAtt = 0;
            if (kindItem == 3) {
                pcTemp = new byte[][]{{0, 100, 0, 0, 0}, {0, 80, 20, 0, 0}, {0, 60, 30, 10, 0}, {0, 10, 60, 30, 0}, {0, 0, 0, 90, 10}, {0, 0, 0, 0, 100}};
                startAtt = 1;
            }

            perc[startAtt] = material[0] * p1;
            perc[4] = material[5] * p1;
            int temp = 0;

            for (int ix = 1; ix < 5; ix++) {
                if (material[ix] > 0) {
                    temp = material[ix] * p1;
                    int temp2 = 0;
                    int k = 0;
                    int maxPC = 0;

                    for (int j = 0; j < 5; j++) {
                        int temp3 = 0;
                        if (pcTemp[ix][j] > 0) {
                            if (maxPC < pcTemp[ix][j]) {
                                k = j;
                                maxPC = pcTemp[ix][j];
                            }

                            temp3 = p1 * pcTemp[ix][j] / 100;
                            perc[j] += temp3;
                            temp2 += temp3;
                        }
                    }

                    perc[k] += temp - temp2;
                }
            }

            if (perc[4] > 0) {
                perc[4] += p2;
            } else if (perc[3] > 0) {
                perc[3] += p2;
            } else if (perc[2] > 0) {
                perc[2] += p2;
            } else if (perc[1] > 0) {
                perc[1] += p2;
            } else if (perc[0] > 0) {
                perc[0] += p2;
            }

            return perc;
        }
    }

    private static int[][] getPercentCreateItem(int idAtt, int n1, int[] material1, int n2, int[] material2) {
        int[][] perc = new int[][]{new int[5], new int[4]};
        int total = 0;

        for (int i = 0; i < total; i++) {
            if (material1[i] > 0) {
                total += material1[i];
            }
        }

        if (total > n1) {
            return null;
        } else {
            int p1 = (100 + (material2[5] > 0 ? material2[5] * 2 : 0)) / n1;
            int p2 = 100 - p1 * n1;
            byte[][] percentForEachLv = new byte[][]{{100, 0, 0, 0}, {80, 20, 0, 0}, {60, 30, 10, 0}, {10, 60, 30, 0}, {0, 0, 90, 10}, {0, 0, 0, 100}};
            total = 0;

            for (int ix = 0; ix < total; ix++) {
                if (material2[ix] > 0) {
                    total += material2[ix];
                }
            }

            if (total > n2) {
                return null;
            } else {
                p1 = 100 / n2;
                perc[1][0] = material2[0] * p1;
                perc[1][3] = material2[5] * p1;
                p2 = 100 - p1 * n2;
                int temp = 0;

                for (int ixx = 1; ixx < 5; ixx++) {
                    if (material2[ixx] > 0) {
                        temp = material2[ixx] * p1;
                        int temp2 = 0;
                        int k = 0;

                        for (int j = 0; j < 4; j++) {
                            int temp3 = 0;
                            if (percentForEachLv[ixx][j] > 0) {
                                k = j;
                                temp3 = temp * percentForEachLv[ixx][j] / 100;
                                perc[1][j] = perc[1][j] + temp3;
                                temp2 += temp3;
                            }
                        }

                        perc[1][k] = perc[1][k] + (temp - temp2);
                    }
                }

                if (perc[1][3] > 0) {
                    perc[1][3] = perc[1][3] + p2;
                } else if (perc[1][2] > 0) {
                    perc[1][2] = perc[1][2] + p2;
                } else if (perc[1][1] > 0) {
                    perc[1][1] = perc[1][1] + p2;
                } else if (perc[1][0] > 0) {
                    perc[1][0] = perc[1][0] + p2;
                }

                return perc;
            }
        }
    }





    public static Item createItemBoss(int lvBoss, Monster mt) {
        Item item = null;

        try {
            item = mt.dropItemAnimal(lvBoss, mt.idTemplate == idGhost ? Item.COLOR_BLUE : 0);
            if (item != null) {
                item.isBoss = true;
                item.dateCreate = Char.getDayTime(0L);
            } else {
                System.out.println("KHONG TAO DC ITEM BOSS");
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return item;
    }


    void doRideHorse(Char player, int type) {
        if (player.rideHorse <= 0) {
            if (type == 30) {
                player.rideHorse = 1;
                player.idImgHorse = 0;
                player.maxhp += 700;
                player.maxmp += 700;
                player.speed = 6;
                player.xichtho_thienlyma = 1;
            } else if (type == 34) {
                player.rideHorse = 1;
                player.xichtho_thienlyma = 2;
                player.idImgHorse = 1;
                player.maxhp += 1000;
                player.maxmp += 1000;
                player.speed = 6;
            } else if (type == 64) {
                player.rideHorse = 3;
            } else if (type == 65) {
                player.rideHorse = 4;
            } else if (type == 66) {
                player.rideHorse = 5;
            } else if (type == 67) {
                player.rideHorse = 6;
            } else if (type == 68) {
                player.rideHorse = 7;
            } else if (type == 86) {
                player.rideHorse = 8;
            } else if (type == 36) {
                player.rideHorse = 8;
            }

            if (type != 30) {
                player.calculateAttrib();
                player.speed = 6;
            }

            try {
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                MessageCreator.createMsgCharMonster(player, player);
            } catch (Exception var5) {
            }
        } else {
            try {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dùng khi đang cưỡi ngựa", ""));
                player.potions[type]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } catch (Exception var4) {
            }
        }
    }

    protected void setCoolDownHp(Char player) {
        try {
            short[] id = new short[]{1, 2, 3, 21, 22, 93, 94};

            for (int i = 0; i < id.length; i++) {
                ((PotionUse) player.coolDownPotion.get(id[i])).setCoolDown();
            }
        } catch (Exception var4) {
        }
    }

    protected void setCoolDownMp(Char player) {
        try {
            short[] id = new short[]{4, 5, 6, 23, 24, 95, 96, 126, 127};

            for (int i = 0; i < id.length; i++) {
                ((PotionUse) player.coolDownPotion.get(id[i])).setCoolDown();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    protected boolean doUseHP(Char player, short potionType, int hpAdd) throws IOException {
        if (player.checkChoang() && hpAdd > 0) {
            return false;
        } else if (player.hp <= 0) {
            return false;
        } else if (((PotionUse) player.coolDownPotion.get(potionType)).doUsePotion() && !player.isVetThuongSau()) {
            // Mat na Ong Dia: +20% hieu qua hoi khi hp duoi 30% toi da
            if (player.isBuffHoiMatNaOngDia()) {
                hpAdd += hpAdd * 20 / 100;
            }
            player.hp = player.hp + hpAdd + hpAdd * player.getEffSkillClanMember(3) / 100;
            if (player.hp <= 0) {
                player.hp = 0;
            }

            player.calculatorHPMP();
            if (potionType > 125) {
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
            } else {
                Message m = new Message(22);
                m.dos.writeShort(player.id);
                m.dos.writeByte(potionType);
                m.dos.writeShort(hpAdd);
                m.dos.writeInt(player.hp);
                m.dos.writeByte(1);
                player.sendMessage(m);
                this.setCoolDownHp(player);
                if (player.map.mapId != idMapTown || !getTown[player.inCountry]) {
                    player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, hpAdd));
                }

                m.cleanup();
            }

            return true;
        } else {
            player.potions[potionType]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            return false;
        }
    }

    protected boolean doUseMP(Char player, short potionType, int mpAdd) throws IOException {
        boolean isUse = ((PotionUse) player.coolDownPotion.get(potionType)).doUsePotion();
        if (isUse && !player.isYemBua()) {
            // Mat na Ong Dia: +20% hieu qua hoi khi hp duoi 30% toi da
            if (player.isBuffHoiMatNaOngDia()) {
                mpAdd += mpAdd * 20 / 100;
            }
            player.mp += mpAdd;
            player.calculatorHPMP();
            if (potionType > 125) {
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
            } else {
                Message m = new Message(22);
                m.dos.writeShort(player.id);
                m.dos.writeByte(potionType);
                m.dos.writeShort(mpAdd);
                m.dos.writeInt(player.mp);
                player.sendMessage(m);
                if (player.map.mapId != idMapTown || !getTown[player.inCountry]) {
                    player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, mpAdd));
                }

                m.cleanup();
            }

            this.setCoolDownMp(player);
            return true;
        } else {
            player.potions[potionType]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            return false;
        }
    }

    // Than method (430 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public void processMessage(Char player, Message message) throws IOException {
        MapXuLyLenh.processMessage(this, player,message);
    }
    // Cai Tao Cai Trang: danh sach bo thoi trang vinh vien cai tao duoc (bo dang mac dung
    // dau, roi hanh trang). Luat, nhom, gia: xem real/CaiTaoCaiTrang.
    public void showCaiTaoMaterialList(Char player) {
        player.sendMessage(MessageCreator.createMessageShopNew(
                player, CaiTaoCaiTrang.locTrangPhuc(player), "Chọn", "Cải Tạo", 5));
    }


    public void showKhoManh (Char player) {
        // Danh sách ID cải trang cần lọc
        int[] validItemIDs = {848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, 859};
        Vector<Item> manhSuuTap = new Vector<>();
   
        // Lọc items từ rương người chơi
        for (int i = 0; i < player.iItems.size(); i++) {
            Item item = (Item) player.iItems.elementAt(i);
            for (int validID : validItemIDs) {
                if (item.tempateID == validID && item.minuteExist <= 0) { // -1 là vĩnh viễn
                    manhSuuTap.addElement(item);
                    break;
                }
            }
        }
        
        // Hiển thị shop với danh sách đã lọc
        player.sendMessage(MessageCreator.createMessageShopNew(player, manhSuuTap, "Chọn", "Kho Mảnh", 4));
    }

  

    public void ShowBoSuuTap(Char player) {
        // Danh sách ID cải trang cần lọc - đặt static final
        final int[] validItemIDs = {860, 861, 862, 863, 864, 865, 866, 867, 868, 869, 870, 871};
        final Vector<Item> manhSuuTap = new Vector<>(12); // Khởi tạo với capacity = 12
    
        // Lọc items từ rương người chơi - tối ưu vòng lặp
        final int size = player.iItems.size();
        for (int i = 0; i < size; i++) {
            Item item = player.iItems.elementAt(i);
            if (item == null || item.minuteExist > 0) continue;
            
            // Kiểm tra ID trực tiếp
            int id = item.tempateID;
            if (id >= 860 && id <= 871) {
                manhSuuTap.addElement(item);
            }
        }
       
        // Hiển thị shop với danh sách đã lọc
        player.sendMessage(MessageCreator.createMessageShopNew(player, manhSuuTap, "Chọn", "Bộ Sưu Tập", 3));
    }

    public void ShowCaiTrang(Char player) {
        // Danh sách ID cải trang cần lọc
        final int[] validItemIDs = {903, 904};
        final Vector<Item>  CaiTrang = new Vector<>(validItemIDs.length);
        
        // Lọc items từ rương người chơi
        final int size = player.iItems.size();
        for (int i = 0; i < size; i++) {
            Item item = player.iItems.elementAt(i);
            if (item == null || item.minuteExist > 0) continue;
            
            // Kiểm tra ID sử dụng mảng validItemIDs
            int id = item.tempateID;
            for (int validId : validItemIDs) {
                if (id == validId) {
                    CaiTrang.addElement(item);
                    break;
                }
            }
        }
        
        // Hiển thị shop với danh sách đã lọc
        player.sendMessage(MessageCreator.createMessageShopNew(player, CaiTrang, "Chọn", "Cải Trang", 99));
    }
        
     
    /** ID các nguyên liệu tinh luyện thần binh hợp lệ. */
    private static final int[] ID_NGUYEN_LIEU_TINH_LUYEN = {
        421, 408, 447, 434, 460,
        422, 409, 448, 435, 461,
        423, 410, 449, 436, 462,
        424, 411, 450, 437, 463,
        425, 412, 451, 438, 464,
        893, 894, 895, 896, 897
    };

    /**
     * Nhân một chỉ số lên theo "Chỉ số vũ khí tăng" của thần binh, có CHẶN TRẦN.
     *
     * att55 là otherAtt[55] của cây thần binh, đơn vị PHẦN NGHÌN: 501 nghĩa là +50,1%,
     * tức nhân 1,501. Chia số nguyên nên phần lẻ bị cắt cụt (giữ nguyên cách tính cũ
     * của mảng atb[], để số liệu người chơi đang có không đổi).
     *
     * VÌ SAO PHẢI CHẶN TRẦN: newAtb[], lockAtb[], addMoreLevelSkill[] khai báo là
     * byte[] (Item.java) nên chỉ chứa được -128..127. Một món "Tăng công 90%" nhân
     * 1,501 ra 135, ép về byte thì QUAY VÒNG thành -121 - chỉ số dương biến thành âm,
     * và không có lỗi nào được ném ra. atb[] là short[] (trần 32767) nên xa ngưỡng
     * hơn nhiều, nhưng dùng chung một hàm cho khỏi phải nhớ mảng nào kiểu gì.
     *
     * Giá trị <= 0 trả về nguyên vẹn: 0 nhân lên vẫn là 0, còn số âm (nếu dữ liệu cũ
     * lỡ có) thì nhân lên sẽ càng âm thêm.
     */
    private static int nhanTheoChiSoVuKhiTang(final int giaTri, final int att55, final int tran) {
        if (giaTri <= 0) {
            return giaTri;
        }
        final long ketQua = giaTri + (long) giaTri * att55 / 1000L;
        return (ketQua > tran) ? tran : (int) ketQua;
    }

    /**
     * Lọc nguyên liệu tinh luyện hợp lệ trong hành trang.
     *
     * PHẢI dùng chung giữa lúc HIỆN danh sách cho người chơi chọn
     * (showTinhLuyenMaterialList) và lúc XỬ LÝ lựa chọn đó (popup 124), vì người
     * chơi gửi về một CHỈ SỐ trong danh sách chứ không phải id vật phẩm.
     *
     * Trước đây hai chỗ tự lọc riêng và lệch nhau ở hai điểm:
     *   - danh sách hiện cho người chơi thiếu 5 id 893-897
     *   - ở mốc 99, chỗ xử lý đòi nguyên liệu phải CAO HƠN trang bị, còn chỗ hiển
     *     thị vẫn cho cả hai chiều ±10 cấp
     * Chỉ cần người chơi có một món 893-897 hợp lệ, hoặc đang ở mốc 99, là hai
     * danh sách lệch nhau và chỉ số gửi về trỏ sang MÓN KHÁC - người chơi mất
     * đúng món mình không chọn.
     */
    private Vector<Item> locNguyenLieuTinhLuyen(Char player) {
        Vector<Item> ra = new Vector<>();
        if (player.itemVukhiThoiTrang == null) {
            return ra;
        }
        int equipLevel = player.itemVukhiThoiTrang.level;
        int equipType = player.itemVukhiThoiTrang.getTemplate().type;
        boolean mocCuoi = player.itemVukhiThoiTrang.TinhLuyen == 99;

        for (int i = 0; i < player.iItems.size(); i++) {
            Item item = (Item) player.iItems.elementAt(i);
            boolean hopLe = false;
            for (int materialID : ID_NGUYEN_LIEU_TINH_LUYEN) {
                if (item.tempateID == materialID) {
                    hopLe = true;
                    break;
                }
            }
            if (!hopLe || item.getTemplate().type != equipType) {
                continue;
            }
            if (mocCuoi) {
                // Ở mốc 99 nguyên liệu phải cao hơn trang bị, tối đa 10 cấp
                if (item.level > equipLevel && item.level - equipLevel <= 10) {
                    ra.addElement(item);
                }
            } else if (Math.abs(item.level - equipLevel) <= 10) {
                ra.addElement(item);
            }
        }
        return ra;
    }

    public void showTinhLuyenMaterialList(Char player) {
        player.sendMessage(MessageCreator.createMessageShopNew(
                player, locNguyenLieuTinhLuyen(player), "Chọn", "Nguyên liệu", 6));
    }

  

    void doBuyItemShopNew(Char player, Message message) 
    {
        try 
        {
            if (!player.isCorrectPass && !player.passWord.equals("")) 
            {
                player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                return;
            }

            

            int type = message.dis.readShort();
           
            if (type == 99) 
            {
                int shopIndex = message.dis.readUnsignedByte();
                
                // Tạo danh sách chứa các item cải trang
                Vector<Item> filteredItems = new Vector<>();
                
                // Lọc items từ rương người chơi
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item item = player.iItems.elementAt(i);
                    // Kiểm tra item cải trang
                    if ((item.tempateID == 903 || item.tempateID == 904) && item.minuteExist <= 0) {
                        filteredItems.addElement(item);
                    }
                }
            
                // Kiểm tra index hợp lệ
                if (shopIndex < 0 || shopIndex >= filteredItems.size()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không hợp lệ", ""));
                    return;
                }
            
                // Lấy item được chọn từ danh sách đã lọc
                Item selectedItem = filteredItems.elementAt(shopIndex);
                if (selectedItem == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại", ""));
                    return;
                }
            
               
                if (selectedItem.tempateID == 903) {
                    if (player.isCaiTrang == 1) {
                        // Nếu đang sử dụng cải trang 1 thì tắt
                        player.isCaiTrang = 0;
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã tắt cải trang Bát Giới", ""));
                    } else {
                        // Nếu chưa sử dụng thì bật
                        player.isCaiTrang = 1;
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã bật cải trang Bát Giới", ""));
                    }
                } else if (selectedItem.tempateID == 904) {
                    if (player.isCaiTrang == 2) {
                        // Nếu đang sử dụng cải trang 2 thì tắt
                        player.isCaiTrang = 0;
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã tắt cải trang Sa Tăng", ""));
                    } else {
                        // Nếu chưa sử dụng thì bật
                        player.isCaiTrang = 2;
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã bật cải trang Sa Tăng", ""));
                    }
                }
                return;
            } 
            else if (type == 3) 
            {
                int shopIndex = message.dis.readUnsignedByte();
                
                // Tạo danh sách chứa các item phù hợp để nâng cấp
                Vector<Item> filteredItems = new Vector<>();
                
                // Lọc items từ rương người chơi
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item item = (Item) player.iItems.elementAt(i);
                    // Kiểm tra item nâng cấp
                    if (item.tempateID >= 860 && item.tempateID <= 871 && item.minuteExist <= 0) {
                        filteredItems.addElement(item);
                    }
                }
            
                // Kiểm tra index hợp lệ
                if (shopIndex < 0 || shopIndex >= filteredItems.size()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không hợp lệ", ""));
                    return;
                }
            
                // Lấy item được chọn từ danh sách đã lọc
                Item selectedItem = filteredItems.elementAt(shopIndex);
                if (selectedItem == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại", ""));
                    return;
                }
                if(selectedItem.plus >= 15) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể nâng cấp đến cấp 15", ""));
                    return;
                }
            
                // Tìm nguyên liệu tương ứng
                int materialId = selectedItem.tempateID - 12;
                Item materialItem = null;
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item item = (Item) player.iItems.elementAt(i);
                    if (item.tempateID == materialId && item.minuteExist <= 0) {
                        materialItem = item;
                        break;
                    }
                }
            
                if (materialItem == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không có nguyên liệu để nâng cấp", ""));
                    return;
                }
            
                // Tính toán số lượng mảnh và lượng cần thiết
                int requiredFragments = 100 + (selectedItem.plus * 100);
                int requiredGold = requiredFragments;
            
                if (materialItem.otherAtt[65] < requiredFragments) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Cần " + requiredFragments + " nguyên liệu để nâng cấp", ""));
                    return;
                }
                
                String popupMsg = String.format(
                    "Nâng cấp %s từ cấp %s lên cấp %s cần %d %s và phí nâng cấp là %d lượng tỉ lệ 50%% ?",
                    selectedItem.getTemplate().name,
                    selectedItem.plus,
                    selectedItem.plus + 1,
                    requiredFragments,
                    materialItem.getTemplate().name,
                    requiredGold
                );
            
                player.sendMessage(MessageCreator.createMsgPopUp(shopIndex, 122, popupMsg));
                return;
            }
            else if (type == 4) 
            {
                int shopIndex = message.dis.readUnsignedByte();
                
                // Lọc items từ rương người chơi
                Item selectedMaterial = null;
                int resultItemId = -1;
            
                // Tìm nguyên liệu
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item item = (Item) player.iItems.elementAt(i);
                    if (item.tempateID >= 848 && item.tempateID <= 859 && item.minuteExist <= 0) {
                        if (shopIndex == 0) {
                            selectedMaterial = item;
                            resultItemId = item.tempateID + 12;
                            break;
                        }
                        shopIndex--;
                    }
                }
            
                // Kiểm tra điều kiện
                if (selectedMaterial == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không hợp lệ", ""));
                    return;
                }
                
                // Kiểm tra item đã tồn tại
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item item = (Item) player.iItems.elementAt(i);
                    if (item.tempateID == resultItemId) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã có vật phẩm này, vui lòng chế tạo vật phẩm khác", ""));
                        return;
                    }
                }
            
                // Kiểm tra số lượng nguyên liệu
                if (selectedMaterial.otherAtt[65] < 200) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Cần 200 nguyên liệu để chế tạo", ""));
                    return;
                }
            
                // Thực hiện chế tạo
                selectedMaterial.otherAtt[65] -= 200;
                if (selectedMaterial.otherAtt[65] <= 0) {
                    player.iItems.remove(selectedMaterial);
                }
            
                // Tạo item sưu tập
                player.createItemSuuTap(resultItemId, player);
                
              
                
                player.sendMessage(MessageCreator.createServerAlertMessage("Chế tạo thành công", ""));
                showKhoManh(player);
                return;
            }
            else if (type == 5)
            {
                // Cai Tao Cai Trang - buoc 1: nguoi choi chon mot bo trong danh sach
                // (showCaiTaoMaterialList). O day chi kiem dieu kien roi mo hop thoai xac
                // nhan (idpopup 125); tru diem va quay ket qua nam o idpopup 125, KHONG o day.
                // Luat, nhom, gia: xem real/CaiTaoCaiTrang.
                try {
                    int shopIndex = message.dis.readUnsignedByte();
                    Vector<Item> dsCaiTao = CaiTaoCaiTrang.locTrangPhuc(player);
                    if (shopIndex < 0 || shopIndex >= dsCaiTao.size()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không hợp lệ", ""));
                        return;
                    }
                    Item selectedItem = dsCaiTao.elementAt(shopIndex);
                    int diemCan = CaiTaoCaiTrang.diemCan(selectedItem.tempateID);
                    String tenDiem = CaiTaoCaiTrang.tenDiem(selectedItem.tempateID);
                    if (CaiTaoCaiTrang.diemHienCo(player, selectedItem.tempateID) < diemCan) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Bạn không đủ " + diemCan + " " + tenDiem + " để cải tạo", ""));
                        return;
                    }
                    String popupMsg = "Bạn có muốn cải tạo " + CaiTaoCaiTrang.tenNgan(selectedItem.getTemplate().name)
                            + " với " + diemCan + " " + tenDiem + " không? Thành công nhận "
                            + Item.tenTrangPhucSauTinhHoa(selectedItem.tempateID)
                            + " kèm dòng Tinh Hoa tăng % HP, thất bại mất điểm.";
                    player.sendMessage(MessageCreator.createMsgPopUp(shopIndex, 125, popupMsg));
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }               
            else if (type == 6) 
            {
                try {
                    int shopIndex = message.dis.readUnsignedByte();
                    // shopIndex là vị trí của item trong shop (filteredItems)
                    
                    // Kiểm tra điều kiện cơ bản
                    if (!player.checkVuKhiThanBinh()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                player.daTinhLuyenToiDa()
                                        ? "Vũ khí thần binh này đã tinh luyện tối đa 100%, không thể tinh luyện thêm."
                                        : "Bạn phải mặc vũ khí thần binh mới có thể tinh luyện", ""));
                        return;
                    }

                    if (player.itemVukhiThoiTrang.TinhLuyen >= 100) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Cấp độ tinh luyện đã đạt cấp tối đa", ""));
                        return;
                    }
            
                    if (player.getLuong() < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 1 lượng để tinh luyện", ""));
                        return;
                    }

                    if (player.getxu() < 3000000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần có 3.000.000 Xu để tinh luyện", ""));
                        return;
                    }

                    // Dung CHUNG locNguyenLieuTinhLuyen() voi luc HIEN danh sach va luc
                    // THUC THI (idpopup 124) - xem chu thich o dinh nghia ham do. Truoc
                    // day cho nay tu loc rieng bang validMaterialIDs, thieu truong hop
                    // moc 99% (locNguyenLieuTinhLuyen doi nguyen lieu phai CAO HON trang
                    // bi) va hien gia sai (500 luong, khong tru xu) so voi luc thuc thi
                    // that (1 luong + 3 trieu xu) - khien shopIndex nguoi choi gui ve co
                    // the tro sang MON KHAC trong danh sach that su duoc dung de thuc thi.
                    Vector<Item> filteredItems = locNguyenLieuTinhLuyen(player);

                    // Kiểm tra index hợp lệ
                    if (shopIndex < 0 || shopIndex >= filteredItems.size()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Nguyên liệu không hợp lệ", ""));
                        return;
                    }

                    // Lấy item được chọn từ danh sách đã lọc
                    Item selectedItem = (Item)filteredItems.elementAt(shopIndex);
                  
                   // Get the selected item name
                    String nguyenLieuName = selectedItem.getTemplate().name;
                    String itemThoiTrangName = player.itemVukhiThoiTrang.getTemplate().name;

                    // Format the message with proper spacing and item names
                    String popupMsg = String.format(
                        "Bạn có muốn dùng %s để tinh luyện %s với giá 1 lượng và 3.000.000 xu không?",
                        nguyenLieuName,
                        itemThoiTrangName, 
                        nguyenLieuName
                    );

                    player.sendMessage(MessageCreator.createMsgPopUp(shopIndex, 124, popupMsg));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (type == 7)
            {
                // Buoc "xem vu khi than binh dang mac" truoc khi mo danh sach nguyen
                // lieu (RealController.doCreateItem, muc menu "Tinh Luyện Thần Binh",
                // gui shop nay voi type 7). Shop chi co dung 1 item nen khong can doc
                // shopIndex nguoi choi chon - chi validate lai dieu kien (phong khi
                // trang bi/luong/xu da doi giua luc mo shop nay va luc bam Tiep tuc)
                // roi moi mo tiep danh sach nguyen lieu that.
                try {
                    if (!player.checkVuKhiThanBinh()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                player.daTinhLuyenToiDa()
                                        ? "Vũ khí thần binh này đã tinh luyện tối đa 100%, không thể tinh luyện thêm."
                                        : "Bạn phải mặc vũ khí thần binh mới có thể tinh luyện", ""));
                        return;
                    }

                    if (player.itemVukhiThoiTrang.TinhLuyen >= 100) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Cấp độ tinh luyện đã đạt cấp tối đa", ""));
                        return;
                    }

                    if (player.getLuong() < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 1 lượng để tinh luyện", ""));
                        return;
                    }

                    if (player.getxu() < 3000000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần có 3.000.000 Xu để tinh luyện", ""));
                        return;
                    }

                    showTinhLuyenMaterialList(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (type == -10000)
            {
                // if (player.vip <= 0) {
                //     player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ điều kiện mua rương nguyên liệu.", ""));
                //     return;
                // }
    
                String[] menu = !Char.isFinishAllSuKienTrungThul2016()
                        // Moi 19/09/2026 (yeu cau nguoi dung): them "Shop Trung Thu" (rieng,
                        // KHONG yeu cau Vip nhu "Shop Dac Biet") vao dung menu "Khac" nay -
                        // xem case moi trong MapDoMenu.java (idNpc==-516, idOptionMenu==7).
                        ? new String[]{"Mở Túi Đồ", "Bỏ đồ vào túi", "Mở Kho", "Shop Đặc Biệt", player.rcvInviteVip ? "Tắt lời mời" : "Bật lời mời",player.isAutoBan ? "Bật tự động bán đồ thường":"Tắt tự động bán đồ thường",player.isNghienAuto ? "Bật tự động nghiền bột thường" : "Tắt tự động nghiền bột thường", "Shop Trung Thu"}
                        : new String[]{"Mở Túi Đồ", "Bỏ đồ vào túi", "Mở Kho", "Shop Đặc Biệt", player.rcvInviteVip ? "Tắt lời mời" : "Bật lời mời",player.isAutoBan ? "Bật tự động bán đồ thường":"Tắt tự động bán đồ thường",player.isNghienAuto ? "Bật tự động nghiền bột thường" : "Tắt tự động nghiền bột thường"};
                // if (player.vip > 0) {
                //     if (player.rcvInviteVip) {
                //         menu[4] = "Tắt lời mời";
                //     }
                // } else {
                //     menu = new String[]{"Mở Túi Đồ", "Bỏ Đồ Vào Túi", "Mở Kho", "Shop Đặc Biệt"};
                // }

                player.sendMessage(MessageCreator.createMsgMenu(menu, -516, 0));
            } 
            else if (type == 15)
            {
                short shopIndex = (short)message.dis.readUnsignedByte();
                ThoRenThanBi.UpgradeThanKhi(player, shopIndex); 
            }
            else if (type == 14)
            {
                short shopIndex = (short)message.dis.readUnsignedByte();
                ThoRenThanBi.RerollThanKhiAttribute(player, shopIndex);
            }
            else if (type == TYPE_SHOP_NEW_TRUNG_THU)
            {
                // "Shop Trung Thu" (moi 19/09/2026) - KHONG doi hoi Vip (khac han nhanh
                // TYPE_SHOP_NEW_CUA_HANG_VIP ben duoi), danh sach rieng all_item_shop_trungthu,
                // logic mua don gian (khong can cac truong hop dac biet isPhuPhuThe/
                // isRuongNguyenLieu... chi danh cho shop VIP).
                int iditem = message.dis.readUnsignedByte();
                if (iditem < 0 || iditem >= all_item_shop_trungthu.size()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không còn bán. Hãy mở lại shop.", ""));
                    return;
                }
                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy.", ""));
                    return;
                }
                Item mauShop = (Item) all_item_shop_trungthu.get(iditem);
                int xu = mauShop.getXuSell();
                int luong = mauShop.getLuongSell();
                if (player.getxu() < (long) xu) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }
                if (player.getLuong() < luong) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }
                if (luong > 0) {
                    player.subLuong((long) luong);
                }
                if (xu > 0) {
                    player.subXu((long) xu, false, "doBuyItemShopTrungThu");
                }
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(mauShop.tempateID);
                Item newItem = new Item(template, false, 0, 0, mauShop.tempateID);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                newItem.identify = player.charDBID;
                // Sua 19/09/2026: clazz=-1 KHOP DUNG voi item xem truoc trong
                // all_item_shop_trungthu (ItemShop tao voi clazz=-1, xem MapNapTaiNguyen.java)
                // - createAttributeItemModel() truoc do KHONG lam gi cho mat na (chi xu ly
                // rieng Tien Nu/Tinh Quan), gan clazz sai (0) moi la thu THAT SU lam getType()/
                // attribShowedForType tra ve khac di so voi ban xem truoc, khien tooltip
                // (getInfoAtbItem) thieu dong so voi khi con trong shop.
                newItem.clazz = -1;
                // "Sua thang mau goc" (yeu cau nguoi dung): thoi han 7 ngay co dinh cho MOI
                // vat pham mua o Shop Trung Thu - mauShop.minuteExist LUON = 0 (ItemShop chi
                // dung rieng field timeExpire qua setTimeExpire(), khong phai minuteExist,
                // nen dieu kien cu ben duoi khong bao gio dung -> mua xong item KHONG BAO GIO
                // het han). Dung thang mauShop.getTimeExpire() cho dung.
                if (mauShop.getTimeExpire() > 0) {
                    newItem.minuteExist = mauShop.getTimeExpire();
                    newItem.timeLoan = System.currentTimeMillis();
                }
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua " + newItem.getName(), ""));
                Database.instance.saveOrtherLog("", player.charname, "mua " + newItem.getName() + " gia " + xu + " xu " + luong + " luong", "shoptrungthu");
                Database.instance.saveLogThongKeChiTieu(newItem.getName(), 0, 1, (long) (luong > 0 ? luong : xu), luong > 0 ? "luong" : "xu");
            }
            else
            {
                if (player.vip <= 0)
                {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn Cần Vip 1 Trở lên để thực hiện tính năng này", ""));
                    return;
                }
    
                int iditem = message.dis.readUnsignedByte();
                if (type == TYPE_SHOP_NEW_CUA_HANG_VIP) {
                    if (iditem < 0 || iditem >= all_item_shop_special.size()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không còn bán. Hãy mở lại shop.", ""));
                        return;
                    }
                    Item it = (Item) all_item_shop_special.get(iditem);
                    int xu = it.getXuSell();
                    int luong = it.getLuongSell();
                    if (player.getxu() < (long) xu) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    if (player.getLuong() < luong) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    String money = "xu";
                    if (luong > 0) {
                        money = "luong";
                        player.subLuong((long) luong);
                    }

                    if (xu > 0) {
                        money = "xu";
                        player.subXu((long) xu, false, "doBuyItemShopNew");
                    }

                    if (it.isPhuPhuThe()) {
                        player.potions[98] = player.potions[98] + it.getSoluong();
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua " + it.getSoluong() + " " + it.getName(), ""));
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        " mua: "
                                        + it.getName()
                                        + " _ voi gia "
                                        + xu
                                        + " xu, "
                                        + luong
                                        + " luong. tien sau mua "
                                        + player.getxu()
                                        + " xu, "
                                        + player.getLuong()
                                        + " luong",
                                        "shopvip"
                                );
                        Database.instance.saveLogThongKeChiTieu(it.getName(), 0, it.getSoluong(), (long) (xu > 0 ? xu : (luong > 0 ? luong : 0)), money);
                    } else if (it.isRuongNguyenLieu()) {
                        if (player.vip <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn Cần Vip 1 Trở lên để thực hiện tính năng này.", ""));
                            return;
                        }

                        CharInfo cinfo = (CharInfo) ALL_CHAR_BUY_RUONG.get(player.charname);
                        if (cinfo == null) {
                            cinfo = new CharInfo();
                            cinfo.name = player.charname;
                            ALL_CHAR_BUY_RUONG.put(player.charname, cinfo);
                            Database.instance.doAddCharBuyRuong(cinfo);
                        } else if (cinfo.luong >= player.vip) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể mua tối đa " + player.vip + " rương trong 1 ngày.", ""));
                            return;
                        }

                        cinfo.luong++;
                        Database.instance.doUpdateCharBuyRuong(cinfo);
                        int hour = 24 - UtilKPAH.getHour();
                        int minute = UtilKPAH.getMinute();
                        int value = hour * 60 - minute;
                        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(it.tempateID);
                        Item newItem = null;
                        newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = 1;
                        newItem.identify = player.charDBID;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = value;
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.iItems.add(newItem);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        Database.instance
                                .saveOrtherLog(
                                        "", player.charname, ID_RUONG_MAY_MAN_TRUNG + " mua ruong may man " + newItem.id_dongxu_trung + " " + ALL_RUONG_MAY_MAN, "buyruong"
                                );
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua 1 rương nguyên liệu", ""));
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        " mua: "
                                        + it.getName()
                                        + " _ voi gia "
                                        + xu
                                        + " xu, "
                                        + luong
                                        + " luong. tien sau mua "
                                        + player.getxu()
                                        + " xu, "
                                        + player.getLuong()
                                        + " luong",
                                        "shopvip"
                                );
                        Database.instance.saveLogThongKeChiTieu(it.getName(), 0, it.getSoluong(), (long) (xu > 0 ? xu : (luong > 0 ? luong : 0)), money);
                    } else if (it.isAoDai2021() || it.isTieuLongNu() || it.isDuongQua()) {
                        if (player.vip <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn Cần Vip 1 Trở lên để thực hiện tính năng này.", ""));
                            return;
                        }

                        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(it.tempateID);
                        Item newItem = null;
                        newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = 1;
                        newItem.identify = player.charDBID;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = it.getTimeExpire();
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.iItems.add(newItem);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        Database.instance.saveOrtherLog("", player.charname, "mua " + newItem.getName() + "_" + newItem.getInfoSave(), "buyshopdb");
                        player.sendMessage(MessageCreator.createServerAlertMessage("Đã mua 1 " + newItem.getName(), ""));
                        Database.instance.saveLogThongKeChiTieu(it.getName(), 0, it.getSoluong(), (long) (xu > 0 ? xu : (luong > 0 ? luong : 0)), money);
                    }
                    

                    Database.instance.saveChar(player, false);
                } else if (type == TYPE_SHOP_NEW_BO_ITEM_VAO_TUI) {
                    if (player.vip <= 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn Cần Vip 1 Trở lên để thực hiện tính năng này.", ""));
                        return;
                    }
        
                    if (player.bagItems.size() >= Char.MAX_TUI_DO) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Túi đồ đã đầy", ""));
                        return;
                    }

                    Item itx = (Item) player.iItems.remove(iditem);
                    player.bagItems.add(itx);
                    player.sendMessage(MessageCreator.createMessageShopNew(player, player.iItems, "Bỏ vào", "Hành trang", TYPE_SHOP_NEW_BO_ITEM_VAO_TUI));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    Database.instance.saveOrtherLog("", player.charname, "bo " + itx.getName() + "_" + itx.getInfoSave() + " vao tui do", "inven2tui");
                    Database.instance.saveChar(player, false);
                } else if (type == TYPE_SHOP_NEW_TUI_DO) {
                    Item itx = (Item) player.bagItems.remove(iditem);
                    player.iItems.add(itx);
                    player.sendMessage(MessageCreator.createMessageShopNew(player, player.bagItems, "Lấy ra", "Túi Đồ", TYPE_SHOP_NEW_TUI_DO));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    Database.instance.saveOrtherLog("", player.charname, "lay " + itx.getName() + "_" + itx.getInfoSave() + " vao hanh trang", "tui2inven");
                    Database.instance.saveChar(player, false);
                }
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        }
    }

    public void doNghienBot(Char player, Message message) {
    }

    public void doLockItemChar(Char player, short readShort, short readShort2, byte readByte) {
    }

    public void doLockItemAnimal(Char player, short idItem, short idMaterial, byte lock) {
    }

    public void doRequestSellItemMarket(Char player, Message message) {
    }

    public static void createListItemMarket(Char player, Vector<ItemSell> itemSell, int type) {
        int npage = 0;
        if (type == 1 || type == 4) {
            npage = itemSell.size() / 50;
            if (itemSell.size() % 50 != 0) {
                npage++;
            }
        }

        Message m = new Message(62);

        try {
            m.dos.writeByte(type);
            if (type > -1) {
                m.dos.writeByte(itemSell.size());

                for (int i = 0; i < itemSell.size(); i++) {
                    ItemSell item = (ItemSell) itemSell.get(i);
                    Item it = item.item;
                    m.dos.writeByte(it.clazz);
                    m.dos.writeShort(it.getTemplate().id);
                    m.dos.writeByte(it.plus);
                    m.dos.writeByte(it.level);
                    m.dos.writeShort(it.mDurable);
                    m.dos.writeShort(it.durable);
                    m.dos.writeByte(it.nhadSock);
                    m.dos.writeByte(it.nSocAdd);
                    m.dos.writeByte(it.colorName);
                    m.dos.writeByte(it.heItem);
                    m.dos.writeByte(it.beKick);
                    m.dos.writeByte(it.hangItem);
                    m.dos.writeByte(it.magic_physic);
                    m.dos.writeByte(it.lock);
                    m.dos.writeUTF(it.charSeal);
                    m.dos.writeUTF(item.charSell);
                    m.dos.writeUTF(item.charBid);
                    m.dos.writeInt(item.priceSell);
                    m.dos.writeInt(item.priceBid);
                    m.dos.writeLong(item.timeEnd);
                    m.dos.writeUTF(item.timeSell);
                    Vector<InfoItemAttribute> allAtb = it.getInfoAtbItem();
                    m.dos.writeByte(allAtb.size());

                    for (byte j = 0; j < allAtb.size(); j++) {
                        InfoItemAttribute info = (InfoItemAttribute) allAtb.get(j);
                        m.dos.writeByte(info.id);
                        m.dos.writeShort(info.value);
                    }
                }
            }

            if (type == 3) {
                m.dos.writeLong(ItemMarket.getMoneyCharSell(player.charname));
            }

            m.dos.writeShort(npage);
            player.sendMessage(m);
        } catch (Exception var11) {
        }
    }

    public void doRegisterBattle(Char player) {
        player.idTongKim = IDBATTLE;
        TeamArena team1 = new TeamArena(TeamArena.genIDTeam());
        TeamArena team2 = new TeamArena(TeamArena.genIDTeam());
        MapTongKim map = (MapTongKim) RealController.mapList.get(207);
        TongKimData data = new TongKimData();
        data.addTeam(team1);
        data.addTeam(team2);
        data.idArena = IDBATTLE;
        data.typeArena = 5;
        data.start();
        map.doAddArena(data);
        team1.addMember(player);
        data.sendInfoMatch = true;
        data.timeWaitFight = System.currentTimeMillis() + 30000L;
        IDBATTLE = (short) ((IDBATTLE + 1) % 32000);
    }

    private void doChallenge(Char player, Message message) {
        try {
            byte type = message.dis.readByte();
            short id = message.dis.readShort();
            byte ok = message.dis.readByte();
            if (type == 0) {
                Char p = CharManager.instance.getByCharID(id);
                if (p != null) {
                    if (ok == 0) {
                        p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " không chấp nhận lời thách đấu của bạn.", ""));
                    } else {
                        p.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        player.charname + " đã chấp nhận lời thách đấu của bạn. Trận đấu của bạn và " + p.charname + " sẽ diễn ra sau 30s nữa.", ""
                                )
                        );
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn đã chấp nhận lời thách đấu của " + p.charname + ". Trận đấu của bạn và " + p.charname + " sẽ diễn ra sau 30s nữa.", ""
                                )
                        );
                        TeamArena team1 = new TeamArena(TeamArena.genIDTeam());
                        TeamArena team2 = new TeamArena(TeamArena.genIDTeam());
                        ArenaMap mm = (ArenaMap) RealController.mapList.get(205);
                        ArenaData arena = new ArenaData();
                        arena.addTeam(team1);
                        arena.addTeam(team2);
                        arena.idArena = IDARENA;
                        arena.typeArena = 3;
                        arena.start();
                        mm.doAddArena(arena);
                        p.idArena = IDARENA;
                        player.idArena = IDARENA;
                        arena.moneyThachdau = p.moneyThachdau * 2;
                        p.subXu((long) p.moneyThachdau, false, "map 24");
                        player.subXu((long) p.moneyThachdau, false, "map 25");
                        team1.addMember(p);
                        team2.addMember(player);
                        Message m = new Message(65);
                        m.dos.writeShort(p.id);
                        if (p.pk == 0) {
                            p.pk = 14;
                            m.dos.writeByte(1);
                            m.dos.writeByte(14);
                            p.timeUsePK = System.currentTimeMillis();
                            p.sendMessage(m);
                        }

                        m = new Message(65);
                        m.dos.writeShort(player.id);
                        if (player.pk == 0) {
                            player.pk = 15;
                            m.dos.writeByte(1);
                            m.dos.writeByte(15);
                            player.timeUsePK = System.currentTimeMillis();
                            player.sendMessage(m);
                        }

                        arena.sendInfoMatch = true;
                        arena.timeWaitFight = System.currentTimeMillis() + 30000L;
                        IDARENA = (IDARENA + 1) % 2000000000;
                        p.moneyThachdau = 0;
                        player.moneyThachdau = 0;
                    }
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Đối thủ của bạn đã thoát khỏi game", ""));
                }
            }
        } catch (Exception var12) {
        }
    }

    public static boolean isBotThuocTinh(int id) {
        return id >= 246 && id <= 249;
    }

    void doAddNewAttribute(Char player, short idItem, short idMaterial, byte lock) {
        if (player.map.isMapOffline) {
            if (!player.isCorrectPass && !player.passWord.equals("")) {
                player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
            } else {
                Item item = player.getItemFormVector(player.iItems, idItem);
                if (item != null) {
                    // idMaterial la short doc THANG tu goi tin nen client gui gi cung duoc.
                    // checkEnoughtGem tra cuu listGemitem[idGem] KHONG kiem bien (Char.java:9849)
                    // va gemTemplate[idMaterial] cung vay - ra ngoai bien la nem ngoai le.
                    if (idMaterial < 0 || idMaterial >= gemTemplate.length
                            || idMaterial >= player.listGemitem.length) {
                        data.NghiVanGianLan.ghiVatPham(player, "hackthemdong",
                                "gui id da thuoc tinh ngoai bien: " + idMaterial);
                        return;
                    }
                    if (!player.checkEnoughtGem(idMaterial, 1, lock) || gemTemplate[idMaterial].type != GemTemplate.TYPE_ADD_NEW_ATTRIBUTE) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không có đủ đá thuộc tính", ""));
                        return;
                    }

                    if (isModelClothes(item.getTemplate().atb[9]) || isWearingAnimal(item.getTemplate().type)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cộng thuộc tính cho vật phẩm này.", ""));
                        return;
                    }

                    if (item.colorName != Item.COLOR_GREEN) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cộng thuộc tính cho vật phẩm này.", ""));
                        return;
                    }

                    // TRU DA TRUOC khi cong thuoc tinh.
                    //
                    // Truoc day tru o cuoi, sau ca doan dung chuoi thong bao co
                    // ALL_NAME_ATTRIBUTE_ITEM.get(result[0] + 61) - Vector nap tu bang
                    // data_attribute. Bang do hien co 198 dong nen chi so toi da 89 van
                    // hop le va KHONG nem loi, nhung neu bang bi cat bot thi ngoai le se
                    // roi vao giua "da cong thuoc tinh" va "da tru da": thuoc tinh duoc
                    // cong ma vien da khong mat. Dung kieu loi da gap that o Huyet Bo De
                    // (Map.doUseItem + Pet.doChangeThongThao).
                    player.delGem(idMaterial, 1, lock == 1);
                    int[] result = item.doAddNewAttribute(idMaterial - 227);
                    player.sendMessage(MessageCreator.createMsgItemInfo(item));
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                    "Chúc mừng bạn đã tạo được thuộc tính "
                                    + ((NameAttributeItem) ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(result[0] + 61)).name
                                    + " "
                                    + result[1] / 10
                                    + "."
                                    + result[1] % 10
                                    + "% cho vật phẩm "
                                    + item.getName(),
                                    ""
                            )
                    );
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    Database.instance
                            .saveOrtherLog(
                                    "",
                                    player.charname,
                                    ((NameAttributeItem) ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(result[0] + 61)).name
                                    + " "
                                    + result[1] / 10
                                    + "."
                                    + result[1] % 10
                                    + " cho vật phẩm "
                                    + item.getName(),
                                    "newatt"
                            );
                    Char.isSuKienHe2017();
                    if (Char.isSuKienGioTo2016() && r.nextInt(100) < 50) {
                        player.potions[117]++;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Database.instance.saveOrtherLog("", player.getName(), "nhan dc dua hau khi tien giai", "trungdua");
                    }
                }
            }
        }
    }



    public boolean isGemTuBinh(short idGem) {
        return idGem >= 159 && idGem <= 174 || idGem >= 179 && idGem <= 226;
    }

    void doTuBinh(Char player, Message message) {
        try {
            short idGem = message.dis.readShort();
            boolean lock = message.dis.readByte() == 1;
            if (this.isGemTuBinh(idGem)) {
                player.doAddManhTubinh(idGem, lock);
            } else {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    /**
     * Dung 1 quyen sach ky nang pet (potion 111).
     * doUsePotion da tru quyen sach TRUOC khi goi vao day, nen moi nhanh khong
     * hoc duoc deu phai hoan lai, khong thi nguoi choi mat sach oan.
     */
    public static void doUseSachSkillPetPotion(Char player) {
        try {
            if (player.petUsing == null) {
                hoanLaiSachSkillPet(player, "Bạn phải mang theo pet mới dùng được sách.");
                return;
            }
            if (!player.petUsing.canLearnSkill()) {
                hoanLaiSachSkillPet(player, player.petUsing.getName() + " không thể học thêm kỹ năng.");
                return;
            }
            int idskill = player.petUsing.doLearSkill();
            String info;
            if (idskill == -1) {
                info = player.petUsing.getName() + " học kỹ năng thất bại";
                Database.instance.saveOrtherLog("", player.charname, info, "hocskill");
            } else {
                info = "Chúc mừng " + player.petUsing.getName() + " học được kỹ năng " + Pet.ALL_SKILL[idskill].name;
                Database.instance.saveOrtherLog("", player.charname, info, "hocskill");
                try {
                    Database.instance.savePet(player.petUsing);
                } catch (Exception ignored) {
                }
            }
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hoanLaiSachSkillPet(Char player, String alert) {
        player.potions[Potion.TYPE_SACH_SKILL_PET]++;
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(alert, ""));
    }

    void doFruit(Char player, Message m) {
        try {
            byte type = m.dis.readByte();
            int index = m.dis.readByte();
            // index doc THANG tu goi tin nen client gui gi cung duoc (-128..127),
            // trong khi player.fruit chi co FruitTemplate.MAX_FRUIT o. Truoc day
            // vuot mang thi ArrayIndexOutOfBoundsException bi khoi catch cuoi ham
            // nuot im lang: khong hong gi nhung cung khong ai biet co ke dang do.
            if (player.fruit == null || index < 0 || index >= player.fruit.length) {
                data.NghiVanGianLan.ghiVatPham(player, "hackcaythan",
                        "gui chi so o cay than = " + index + " (chi co "
                      + (player.fruit == null ? 0 : player.fruit.length) + " o), lenh " + type);
                return;
            }
            if (player.fruit[index] == null) {
                return;
            }

            if (type == 1) {
                MessageCreator.createMsgCharFruit(player, 1, index);
            } else if (type == 2) {
                if (player.fruit[index].isTraiVang()) {
                    if (player.potions[8] <= 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có bình tăng tốc vàng mới có thể sử dụng chức năng này", ""));
                        return;
                    }
                } else if (player.fruit[index].isTraiBac()) {
                    if (player.potions[13] <= 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có bình tăng tốc bạc mới có thể sử dụng chức năng này", ""));
                        return;
                    }
                } else if (player.potions[7] <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có bình tăng tốc thường mới có thể sử dụng chức năng này", ""));
                    return;
                }

                if (player.fruit[index].canTangToc(player, index)) {
                    if (player.fruit[index].isTraiVang()) {
                        player.potions[8]--;
                    } else if (player.fruit[index].isTraiBac()) {
                        player.potions[13]--;
                    } else {
                        player.potions[7]--;
                    }

                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                }
            } else if (type == 3 && player.fruit[index].doHavest(player)) {
                player.fruit[index] = null;
                player.createFruit();
                MessageCreator.createMsgCharFruit(player, 0, index);
            }
        } catch (Exception var5) {
        }
    }

    void doLuckyDraw(Char player, Message message) {
        try {
            byte type = message.dis.readByte();
            player.doStartLuckDraw(type);
        } catch (Exception var4) {
        }
    }

    void doBuyXoso(Char player, Message message) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            try {
                if (player.getxu() < 1000000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                    return;
                }

                byte number = message.dis.readByte();
                player.sendMessage(MessageCreator.createServerAlertMessage(Xoso.buyNumber(player, number, (byte) 0), ""));
            } catch (Exception var4) {
            }
        }
    }


    public void doProcessItemAnimal(Char player, Message message) {
        try {
            byte len = message.dis.readByte();
            if (len < 5) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                return;
            }
    
            Vector<Short> id = new Vector();
            Vector<Item> realItems = new Vector();  // Lưu các item thực sự
                
            for (byte i = 0; i < len; i++) {
                short itemId = message.dis.readShort();
                Item item = player.getItemFormVector(player.iItems, itemId);
                
                // Kiểm tra item có tồn tại không
                if (item == null) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại", ""));
                    return;
                }
                
                // Kiểm tra item đã được sử dụng chưa
                if (realItems.contains(item)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cùng một vật phẩm", ""));
                    return;
                }
                
                id.add(itemId);
                realItems.add(item);
            }
            
            // Kiểm tra lại số lượng thực tế
            if (realItems.size() < 5) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                return;
            }
    
            byte[] material = new byte[6];
            message.dis.read(material, 0, 6);
            player.sendMessage(MessageCreator.createServerAlertMessage(ItemChangeColor.doUpColorItem(player, id, material, player.itChangeColor.materialUse), ""));
            player.itChangeColor = null;
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }
   // con lon di theo
    void doMsgCharMonster(Char player, Message message) {
        try {
            if (player.myCountry == -1) {
                return;
            }

            int type = message.dis.readByte();
            Char p = this.getChar(message.dis.readShort());
            if (!eventKillMonster[player.myCountry] && type == 0 && !p.isCharCopy()) {
                return;
            }

            if (type == -1) {
                int idmons = 0 ;
           
                if ( p.isCharHire()) {
               
                    idmons = 9;
                }
              
             

                player.sendMessage(MessageCreator.createMsgInfoChar2Monster(p, true, idmons, "doMsgCharMonster Map"));
            } else {
                player.sendMessage(MessageCreator.createImgChar2Monster(type));
            }
        } catch (Exception var6) {
        }
    }

    void doChangTypeItem(Char player, Message message) {
        try {
            byte magic_physic = message.dis.readByte();
            short itemid = message.dis.readShort();
            Item item = player.getItemFormVector(player.iItems, itemid);
            if (item != null) {
                item.magic_physic = magic_physic;
                if (item.getTemplate().type < 3 || item.getTemplate().type == 10 || item.getTemplate().type == 11) {
                    if (magic_physic == 0) {
                        item.atb[6] = item.atb[1];
                        item.atb[1] = (short) (item.atb[1] / 10);
                    } else if (magic_physic == 1) {
                        item.atb[6] = (short) (item.atb[1] / 10);
                    }
                }

                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Vật phẩm " + item.getTemplate().name + " đã được chuyển đổi thành trang bị " + (magic_physic == 0 ? "Ma pháp" : "Vật lý"), ""
                        )
                );
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            }
        } catch (Exception var6) {
        }
    }

    public void doRideAnimal(Char player, int type, int id) {
        try {
            if (type == 0) {
                if (player.rideHorse > 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dùng khi đang cưỡi linh thú khác.", ""));
                    return;
                }

                short idHorse = (short) id;
                player.rideHorse = 0;
                player.animalRide = null;

                for (int i = 0; i < player.animal.size(); i++) {
                    Animal animal = (Animal) player.animal.get(i);
                    if (animal.id == idHorse) {
                        animal.place = 1;
                        player.animalRide = animal;
                        player.rideHorse = Animal.rideHorse[animal.idImg];
                        player.idImgHorse = Animal.rideHorse[animal.idImg];
                    } else {
                        animal.place = 0;
                    }
                }

                player.calculateAttrib();
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                MessageCreator.createMsgCharMonster(player, player);
            } else if (type == 1) {
                short idPet = (short) id;
                Pet pet = null;

                for (int ix = 0; ix < player.pet.size(); ix++) {
                    pet = (Pet) player.pet.get(ix);
                    if (pet.id == idPet) {
                        pet.place = 1;
                        break;
                    }
                }

                if (pet != null && pet.canTrade()) {
                    player.idPetTang = idPet;
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Giao dịch"}, -514, 0));
                    pet.place = 0;
                    return;
                }

                if (pet != null) {
                    if (player.petUsing != null) {
                        player.petUsing.place = 0;
                        if (!player.petUsing.isPetTool() && player.petUsing.getIdEffPet() > -1 && player.petUsing.getIdEffPet() != pet.getIdEffPet()) {
                            player.huyEff(player, player.petUsing.getIdEffPet());
                        }
                    }

                    player.petUsing = pet;
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                    player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                    if (!player.petUsing.isPetTool()
                            && pet.getIdEffPet() > -1
                            && player.addEffBuff(pet.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                        player.sendEffToChar(player);
                    }

                    Database.instance.addInfoWebChar(player);
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    public void doRideAnimal(Char player, Message message) {
        try {
            byte type = 0;

            try {
                type = message.dis.readByte();
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            short id = message.dis.readShort();
            if (type == 0) {
                short idHorse = id;

                for (int i = 0; i < player.animal.size(); i++) {
                    Animal animal = (Animal) player.animal.get(i);
                    if (animal.id == idHorse) {
                        if (this.isMapOffline) {
                            if ((animal.isPoro() || animal.isLan()) && animal.place == 0) {
                                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Bán"}, -800 - type, i));
                                return;
                            }
                        } else if (animal.isAnimalCanTang() && animal.place == 0) {
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Tặng"}, -800 - type, i));
                            return;
                        }

                        if (player.rideHorse > 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dùng khi đang cưỡi linh thú khác.", ""));
                            return;
                        }

                        player.rideHorse = 0;
                        player.animalRide = null;
                        animal.place = 1;
                        player.animalRide = animal;
                        player.rideHorse = Animal.rideHorse[animal.idImg];
                        player.idImgHorse = Animal.rideHorse[animal.idImg];
                    } else {
                        animal.place = 0;
                    }
                }

                player.calculateAttrib();
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                MessageCreator.createMsgCharMonster(player, player);
            } else if (type == 1) {
                short idPet = id;
                Pet pet = null;

                for (int ix = 0; ix < player.pet.size(); ix++) {
                    pet = (Pet) player.pet.get(ix);
                    if (pet.id == idPet) {
                        if (this.isMapOffline && pet.isPetBachThu() && pet.place == 0) {
                            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Bán"}, -800 - type, ix));
                            return;
                        }

                        pet.place = 1;
                        break;
                    }
                }

                if (pet != null && pet.canTrade()) {
                    player.idPetTang = idPet;
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Sử dụng", "Giao dịch"}, -514, 0));
                    pet.place = 0;
                    return;
                }

                if (pet != null) {
                    if (player.petUsing != null) {
                        player.petUsing.place = 0;
                        if (!player.petUsing.isPetTool() && player.petUsing.getIdEffPet() > -1 && player.petUsing.getIdEffPet() != pet.getIdEffPet()) {
                            player.huyEff(player, player.petUsing.getIdEffPet());
                        }
                    }

                    player.petUsing = pet;
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                    player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                    if (!player.petUsing.isPetTool()
                            && pet.getIdEffPet() > -1
                            && player.addEffBuff(pet.getIdEffPet(), System.currentTimeMillis() + 320000000L, EffectBuff.BY_ACTOR, 0) != null) {
                        player.sendEffToChar(player);
                    }

                    Database.instance.addInfoWebChar(player);
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    void doConfirmCapcha(Char player, Message message) {
        String st = "";

        try {
            st = message.dis.readUTF();
        } catch (Exception var5) {
        }

        player.confirmCapcha(st);
    }

    void doMove2Warp(Char player, Message message) {
        try {
            if (player.monster != null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang vận tiêu", ""));
                return;
            }

            int x = message.dis.readByte();
            int y = message.dis.readByte();
            int mapid = message.dis.readShort();

            int rangeNpc = 5;
            for (int i = 0; i < bossLocation.size(); i++) {
                BossLocaltion b = (BossLocaltion) bossLocation.get(i);
                if (b.mapID == mapid && b.x == x && b.y == y
                        && player.mapID == b.mapID
                        && Math.abs(player.x / 16 - b.x) <= rangeNpc
                        && Math.abs(player.y / 16 - b.y) <= rangeNpc) {
                    this.move2Map(player, b.tox, b.toy, b.type == 0 ? ((Boss) boss.get(0)).map.mapId : ((Boss) boss.get(player.inCountry)).map.mapId, player.inCountry);
                    break;
                }
            }
        } catch (Exception var8) {
            System.out.println("LOI CHUYEN VAO MAP BOSS");
        }
    }

    void doPopup(Char player, Message message) 
    {
        try {
           
            int idActor = message.dis.readShort();
            int idpopup = message.dis.readByte();
        
            String text = "";
            byte ok = 1;

            try 
            {
                text = message.dis.readUTF();
            } 
            catch (Exception var18) 
            {
            }

            try 
            {
                ok = message.dis.readByte();
            } catch (Exception var17) {
            }

            if (idpopup != 10 && idpopup != 11 && idpopup != 12 && idActor == player.id && ok == 0) {
                return;
            }
            if (idpopup == 119)
            {
                try
                {
                    // Bam "Khong" thi PHAI dung lai. Chot chung o tren chi return khi
                    // idActor == player.id, ma popup nay gui idActor = id thu -> khong bao gio
                    // khop, nen truoc day huy van xoa thu va van tru 500 luong.
                    if (ok == 0) {
                        player.idThuChoXoa = -1;
                        return;
                    }

                    if (!player.isCorrectPass && !player.passWord.equals("")) {
                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                        return;
                    }

                    if(player.getLuong() < 500L) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                        return;
                    }
                    int animalIdDB = player.idThuChoXoa;
                    if (animalIdDB <= 0) {
                        return;
                    }
                    player.idThuChoXoa = -1;
                    data.Animal targetAnimal = null;
                    if (player.animal != null && !player.animal.isEmpty()) {
                        for (data.Animal animal : player.animal) {
                            if (animal.dbid == animalIdDB) {
                                targetAnimal = animal;
                                break;
                            }
                        }
                    }
                
                    if (targetAnimal == null) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Vui lòng kiểm tra lại và nhập đúng ID"));
                        return;
                    }
                
                    if (targetAnimal.ownerId != player.charDBID) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Vui lòng kiểm tra lại và nhập đúng ID"));
                        return;
                    }
                
                    if (targetAnimal.place == 1) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể xóa thú cưỡi đang sử dụng"));
                        return;
                    }
                
                    player.subLuong(500L);
                    player.animal.remove(targetAnimal);
                    Database.instance.delAnimal(targetAnimal.dbid);
                
                    // Thêm log xóa thú cưỡi - 16/09/2026: them so luong da tru + so du con lai
                    // (theo yeu cau "log chi tiet hon").
                    String logInfo = String.format("Xóa thú cưỡi: %s (ID: %d) - tru 500 luong, con lai %d",
                            targetAnimal.name, targetAnimal.dbid, player.getLuong());
                    Database.instance.saveOrtherLog("", player.charname, logInfo, "XOATC");
                
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Xóa thú cưỡi thành công", ""));
                } catch (Exception e) {
                    System.out.println("Lỗi xóa thú cưỡi: " + e.getMessage());
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Có lỗi xảy ra khi xóa thú cưỡi"));
                }
            }
            
            if (idpopup == 120)
            {
                try {
                    // Bam "Khong" thi PHAI dung lai - xem giai thich o popup 119.
                    if (ok == 0) {
                        player.idPetChoXoa = -1;
                        return;
                    }

                    int petIdDB = player.idPetChoXoa;
                    if (petIdDB <= 0) {
                        return;
                    }
                    player.idPetChoXoa = -1;
                    data.Pet targetPet = null;
                    
                    if (player.pet != null && !player.pet.isEmpty()) {
                        for (data.Pet pet : player.pet) {
                            if (pet.idDB == petIdDB) {
                                targetPet = pet;
                                break;
                            }
                        }
                    }
                   
                    if (targetPet == null) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không tìm thấy Pet!"));
                        return;
                    }
            
                    if (!player.isCorrectPass && !player.passWord.equals("")) {
                        player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                        return;
                    }
            
                    if (targetPet.ownerid != player.charDBID) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Vui lòng nhập đúng id!"));
                        return;
                    }
                    if (targetPet.place == 1) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể xóa Pet đang sử dụng!"));
                        return;
                    }
            
                    if (player.getLuong() < 500L) {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng!"));
                        return;
                    }
                    player.subLuong(500L);
            
                    player.pet.remove(targetPet);
                    Database.instance.deletePet(targetPet.idDB);
            
                    // Thêm log xóa pet - 16/09/2026: them so luong da tru + so du con lai
                    // (theo yeu cau "log chi tiet hon").
                    String logInfo = String.format("Xóa pet: %s (ID: %d) - tru 500 luong, con lai %d",
                            targetPet.getName(), targetPet.idDB, player.getLuong());
                    Database.instance.saveOrtherLog("", player.charname, logInfo, "XOAPET");
            
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã xóa Pet: " + targetPet.getName(), ""));
                } catch (Exception e) {
                    System.out.println("Lỗi xóa pet: " + e.getMessage());
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Có lỗi xảy ra khi xóa pet"));
                }
            }

            if (idpopup == 122) 
            { // Nâng cấp item cải trang
                try {
                    // Tạo danh sách ID cải trang cần lọc
                    final int[] validItemIDs = {860, 861, 862, 863, 864, 865, 866, 867, 868, 869, 870, 871};
                    final Vector<Item> upgradableItems = new Vector<>(12);
                    
                    // Lọc items từ rương người chơi
                    for (int i = 0; i < player.iItems.size(); i++) {
                        Item item = (Item) player.iItems.elementAt(i);
                        if (item.tempateID >= 860 && item.tempateID <= 871 && item.minuteExist <= 0) {
                            upgradableItems.addElement(item);
                        }
                    }

                    // Kiểm tra index hợp lệ
                    if (idActor < 0 || idActor >= upgradableItems.size()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không hợp lệ", ""));
                        return;
                    }

                    // Lấy item được chọn để nâng cấp
                    Item selectedItem = upgradableItems.get(idActor);
                    if (selectedItem == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không tồn tại", ""));
                        return;
                    }

                    // Tìm nguyên liệu tương ứng
                    int materialId = selectedItem.tempateID - 12;
                    Item materialItem = null;
                    for (int i = 0; i < player.iItems.size(); i++) {
                        Item item = (Item) player.iItems.elementAt(i);
                        if (item.tempateID == materialId && item.minuteExist <= 0) {
                            materialItem = item;
                            break;
                        }
                    }

                    if (materialItem == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không có nguyên liệu để nâng cấp", ""));
                        return;
                    }

                    // Tính toán số lượng mảnh và lượng cần thiết
                    int requiredFragments = 100 + (selectedItem.plus * 100);
                    int requiredGold = requiredFragments;

                    if (materialItem.otherAtt[65] < requiredFragments) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Cần " + requiredFragments + " nguyên liệu để nâng cấp", ""));
                        return;
                    }

                    if(selectedItem.plus >= 15) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể nâng cấp đến cấp 15", ""));
                        return;
                    }

                    if (player.getLuong() < requiredGold) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ lượng để nâng cấp", ""));
                        return;
                    }

                    // Trừ nguyên liệu và lượng
                    materialItem.otherAtt[65] -= requiredFragments;
                    player.subLuong(requiredGold);

                    // Logic nâng cấp với tỉ lệ 50%
                    if (new Random().nextInt(100) < 50) {
                        selectedItem.plus += 1;
                        
                        // Kiểm tra và xử lý item 903 (từ item 860-865)
                        boolean hasAllItems860to865 = true;
                        int minPlus860to865 = Integer.MAX_VALUE;
                        for (int id = 860; id <= 865; id++) {
                            boolean found = false;
                            for (Item item : upgradableItems) {
                                if (item.tempateID == id && item.plus >= 1) {
                                    found = true;
                                    minPlus860to865 = Math.min(minPlus860to865, item.plus);
                                    break;
                                }
                            }
                            if (!found) {
                                hasAllItems860to865 = false;
                                break;
                            }
                        }

                        // Kiểm tra và xử lý item 904 (từ item 866-871)
                        boolean hasAllItems866to871 = true;
                        int minPlus866to871 = Integer.MAX_VALUE;
                        for (int id = 866; id <= 871; id++) {
                            boolean found = false;
                            for (Item item : upgradableItems) {
                                if (item.tempateID == id && item.plus >= 1) {
                                    found = true;
                                    minPlus866to871 = Math.min(minPlus866to871, item.plus);
                                    break;
                                }
                            }
                            if (!found) {
                                hasAllItems866to871 = false;
                                break;
                            }
                        }

                        // Xử lý item 903
                        if (hasAllItems860to865) {
                            Item item903 = null;
                            // Tìm item 903 nếu đã tồn tại
                            for (Item item : player.iItems) {
                                if (item.tempateID == 903) {
                                    item903 = item;
                                    break;
                                }
                            }

                            if (item903 == null) {
                                // Tạo mới item 903
                                item903 = player.createItemCaiTrang(903, player,minPlus860to865);     
                                } else {
                                    // Kiểm tra nếu plus của item hiện tại >= minPlus860to865 thì không update
                                    if (item903.plus >= minPlus860to865) {    
                                    } else {
                                        player.iItems.remove(item903);
                                        item903 = player.createItemCaiTrang(903, player, minPlus860to865);                 
                                    }
                                }
                        }
                        // Xử lý item 904
                        if (hasAllItems866to871) {
                            Item item904 = null;
                            // Tìm item 904 nếu đã tồn tại
                            for (Item item : player.iItems) {
                                if (item.tempateID == 904) {
                                    item904 = item;
                                    break;
                                }
                            }
                            if (item904 == null) {
                                // Tạo mới item 904
                                item904 = player.createItemCaiTrang(904, player,minPlus866to871);             
                                } else {
                                    // Kiểm tra nếu plus của item hiện tại >= minPlus866to871 thì không update
                                    if (item904.plus >= minPlus866to871) {
                                    } else {
                                        player.iItems.remove(item904);
                                        item904 = player.createItemCaiTrang(904, player, minPlus866to871);                  
                                    }
                                }
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage(
                            "Nâng cấp thành công! " + selectedItem.getTemplate().name + " lên cấp " + selectedItem.plus, 
                            ""
                        ));
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Nâng cấp thất bại!", ""));
                    }

                    // Cập nhật giao diện
                    ShowBoSuuTap(player);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra", ""));
                }
            }
            if (idpopup == 125)
            {
                // Cai Tao Cai Trang - buoc 2: nguoi choi da bam xac nhan. idActor la chi so
                // trong danh sach loc, doc thang tu goi tin nen phai chan bien. Loc lai bang
                // DUNG ham da dung o type == 5 de hai buoc khong lech nhau. Luat, nhom, gia:
                // xem real/CaiTaoCaiTrang.
                try {
                    Vector<Item> dsCaiTao = CaiTaoCaiTrang.locTrangPhuc(player);
                    if (idActor < 0 || idActor >= dsCaiTao.size()) {
                        data.NghiVanGianLan.ghiVatPham(player, "haccaitaocaitrang",
                                "gui chi so cai tao ngoai bien: " + idActor + " (danh sach co " + dsCaiTao.size() + " mon)");
                        return;
                    }
                    Item selectedItem = dsCaiTao.elementAt(idActor);
                    int diemCan = CaiTaoCaiTrang.diemCan(selectedItem.tempateID);
                    String tenDiem = CaiTaoCaiTrang.tenDiem(selectedItem.tempateID);
                    if (CaiTaoCaiTrang.diemHienCo(player, selectedItem.tempateID) < diemCan) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Bạn không đủ " + diemCan + " " + tenDiem + " để cải tạo", ""));
                        return;
                    }

                    // Tru diem TRUOC khi quay: thanh cong hay that bai deu mat diem.
                    CaiTaoCaiTrang.truDiem(player, selectedItem.tempateID, diemCan);

                    int caiTaoCu = selectedItem.CaiTao;
                    Random r = new Random();
                    boolean thanhCong = r.nextInt(100) < CaiTaoCaiTrang.TI_LE_THANH_CONG;
                    String ketQua;
                    if (thanhCong) {
                        selectedItem.CaiTao = CaiTaoCaiTrang.quayPhanTramTinhHoa(r);
                        ketQua = "thanh cong " + selectedItem.CaiTao + "%";
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Cải tạo thành công! Nhận " + Item.tenTrangPhucSauTinhHoa(selectedItem.tempateID)
                                + ", Tinh Hoa +" + selectedItem.CaiTao + "% HP", ""));
                    } else {
                        ketQua = "that bai";
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                "Cải tạo thất bại! Mất " + diemCan + " " + tenDiem, ""));
                    }

                    // Bo DANG MAC doi CaiTao thi HP va hinh anh doi theo -> tinh lai chi so va
                    // gui lai cho chinh minh + nguoi xung quanh, y het luc mac/thao do.
                    if (CaiTaoCaiTrang.laBoDangMac(player, selectedItem)) {
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                        player.sendToNearPlayer(MessageCreator.createCharWearingMessage(player, player));
                    }
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    showCaiTaoMaterialList(player);

                    Database.instance.saveOrtherLog("", player.charname,
                            CaiTaoCaiTrang.tenNgan(selectedItem.getTemplate().name) + " (id " + selectedItem.tempateID
                            + ") | tru " + diemCan + " " + tenDiem + " | tinh hoa " + caiTaoCu + "% -> " + ketQua,
                            "caitaocaitrang");
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra", ""));
                }
            }
             else if (idpopup == 124) {
                try {
                    // Kiểm tra điều kiện cơ bản
                    if (!player.checkVuKhiThanBinh()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                                player.daTinhLuyenToiDa()
                                        ? "Vũ khí thần binh này đã tinh luyện tối đa 100%, không thể tinh luyện thêm."
                                        : "Bạn phải mặc vũ khí thần binh mới có thể tinh luyện", ""));
                        return;
                    }

                    if (player.itemVukhiThoiTrang.TinhLuyen >= 100) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vũ khí đã đạt cấp tinh luyện tối đa", ""));
                        return;
                    }
        
                    if (player.getLuong() < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 1 lượng để tinh luyện", ""));
                        return;
                    }
        
                    if (player.getxu() < 3000000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần có 3.000.000 Xu để tinh luyện", ""));
                        return;
                    }
        
                    // Dùng CHUNG một hàm với lúc hiển thị danh sách cho người chơi.
                    // Xem chú thích ở locNguyenLieuTinhLuyen().
                    Vector<Item> filteredItems = locNguyenLieuTinhLuyen(player);

                    if (filteredItems.isEmpty()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không tìm thấy nguyên liệu phù hợp", ""));
                        return;
                    }

                    // idActor là chỉ số người chơi chọn, đọc thẳng từ gói tin nên phải chặn biên.
                    if (idActor < 0 || idActor >= filteredItems.size()) {
                        data.NghiVanGianLan.ghiVatPham(player, "hacktinhluyen",
                                "gui chi so nguyen lieu tinh luyen ngoai bien: " + idActor
                              + " (danh sach co " + filteredItems.size() + " mon)");
                        return;
                    }

                    // Lấy item đã chọn
                    Item selectedItem = (Item)filteredItems.elementAt(idActor);
                    if (selectedItem == null) {
                        return;
                    }
        
                    // Trừ chi phí
                    player.subLuong(1);
                    player.subXu(3000000, false, "tinh luyen");
        
                    // Tỷ lệ thành công TĂNG dần theo mốc: càng gần 0% càng khó, càng gần
                    // 100% càng dễ. Đường cong BẬC HAI nên đoạn đầu rất chậm rồi mới bứt
                    // lên ở đoạn cuối:
                    //
                    //    mốc   0 :  5%      mốc  30 : 10%      mốc  80 : 42%
                    //    mốc  10 :  6%      mốc  50 : 19%      mốc  98 : 60%
                    //    mốc  20 :  7%                         mốc  99 : 100%
                    //
                    // Kỳ vọng cả chặng 0 -> 100 là ~771 lần đánh (~2,31 tỉ xu, 771 lượng,
                    // 771 nguyên liệu), trong đó riêng đoạn 0 -> 30 đã chiếm ~478 lần.
                    //
                    // Tính bằng SỐ NGUYÊN, không dùng số thực:
                    //     tỉ lệ = 5 + làm_tròn( 55 * mốc^2 / 98^2 )
                    // 9604 = 98^2, cộng 4802 (một nửa) trước khi chia là để làm tròn thay
                    // vì cắt cụt. Mốc 98 cho đúng 5 + 55 = 60.
                    final int TI_LE_GOC = 5;    // ở mốc 0
                    final int TI_LE_THEM = 55;  // cộng dồn tới mốc 98 -> 60%
                    final int MOC_TRAN = 98;
                    int moc = player.itemVukhiThoiTrang.TinhLuyen;
                    if (moc < 0) {
                        moc = 0;
                    } else if (moc > MOC_TRAN) {
                        moc = MOC_TRAN;
                    }
                    int successRate = TI_LE_GOC
                            + (TI_LE_THEM * moc * moc + (MOC_TRAN * MOC_TRAN) / 2)
                              / (MOC_TRAN * MOC_TRAN);

                    // ✅ Nếu là Thần Binh và đang ở % tinh luyện là 99%
                    if (player.checkVuKhiThanBinh() && player.itemVukhiThoiTrang.TinhLuyen == 99) {
                        successRate = 100;
                    }
        
                    // Xác định thành công/thất bại
                    boolean success = r.nextInt(100) < successRate;
                  
        
                    // Xóa nguyên liệu đã dùng
                    player.iItems.remove(selectedItem);
                    Database.instance.deleteItem(selectedItem.dbid);
        
                    if (success) {
                        // Tăng cấp tinh luyện
                        player.itemVukhiThoiTrang.TinhLuyen++;
        
                        // Thêm thời hạn nếu item có hạn
                        if (player.itemVukhiThoiTrang.minuteExist > 0 && 
                            player.itemVukhiThoiTrang.TinhLuyen <= 99) {
                            player.itemVukhiThoiTrang.minuteExist += 24 * 60; // Thêm 1 ngày
                        }
        
                        // Xử lý đặc biệt khi đạt cấp 100
                        if (player.itemVukhiThoiTrang.TinhLuyen == 100) {
                            int oldId = player.itemVukhiThoiTrang.tempateID;
                            
                            if (oldId >= 758 && oldId <= 772) {
                                // Lưu các giá trị otherAtt cần giữ lại
                                short att54 = player.itemVukhiThoiTrang.otherAtt[54];
                                short att55 = player.itemVukhiThoiTrang.otherAtt[55];
                                short att56 = player.itemVukhiThoiTrang.otherAtt[56];
        
                                // Copy toàn bộ thuộc tính từ nguyên liệu
                                player.itemVukhiThoiTrang.atb = selectedItem.atb.clone();
                                player.itemVukhiThoiTrang.newAtb = selectedItem.newAtb.clone();
                                player.itemVukhiThoiTrang.addMoreLevelSkill = selectedItem.addMoreLevelSkill.clone();
                                player.itemVukhiThoiTrang.lockAtb = selectedItem.lockAtb.clone();
                                player.itemVukhiThoiTrang.otherAtt = selectedItem.otherAtt.clone();
                                
                                // Cong don VKTB: chi so vu khi vua hut tu nguyen lieu duoc nhan len theo
                                // "Chi so vu khi tang" (att55, don vi phan nghin) CUA CHINH cay than binh.
                                // Vi du att55 = 501 -> x1,501.
                                //
                                // PHAM VI NHAN (chu server chot 05/09/2026) - dung 3 mang chi so:
                                //   atb[]     Tan cong, Chinh xac, Giam st ma, Tang tan cong...  (short)
                                //   newAtb[]  Tang HP, Tang MP, Cong suc manh/kheo leo/...       (byte)
                                //   lockAtb[] Tang cong, Tang thu ma, Tang thu vat               (byte)
                                // KHONG nhan:
                                //   addMoreLevelSkill[]  la CAP KY NANG (vd "Ho doc tien: 1"), khong
                                //                        phai chi so
                                //   otherAtt[54/55/56]   xem chu thich ngay duoi khoi nay
                                //   durable / mDurable   do ben, khong phai chi so
                                for (int k = 0; k < 33; k++)
                                {
                                    player.itemVukhiThoiTrang.atb[k] = (short) nhanTheoChiSoVuKhiTang(
                                            player.itemVukhiThoiTrang.atb[k], att55, Short.MAX_VALUE);
                                }
                                for (int k = 0; k < player.itemVukhiThoiTrang.newAtb.length; k++)
                                {
                                    player.itemVukhiThoiTrang.newAtb[k] = (byte) nhanTheoChiSoVuKhiTang(
                                            player.itemVukhiThoiTrang.newAtb[k], att55, Byte.MAX_VALUE);
                                }
                                for (int k = 0; k < player.itemVukhiThoiTrang.lockAtb.length; k++)
                                {
                                    player.itemVukhiThoiTrang.lockAtb[k] = (byte) nhanTheoChiSoVuKhiTang(
                                            player.itemVukhiThoiTrang.lockAtb[k], att55, Byte.MAX_VALUE);
                                }
                                // Khoi phuc 3 chi so rieng cua than binh (otherAtt vua bi clone de len
                                // bang cua nguyen lieu o tren). Ba chi so nay GIU NGUYEN, KHONG nhan
                                // theo att55.
                                //
                                // Vi sao khong nhan (quyet dinh cua chu server, 05/09/2026):
                                // otherAtt[55] khong phai chi so thuong ma la CHINH HE SO NHAN - no duoc
                                // ap len vu khi that dang mac (Item.getValueUpByVuKhiThoiTrang va
                                // getInfoAtbItemWearing). Cho no tu nhan chinh no la an hai lan: 50,1%
                                // -> 75,2%, tuc cay vu khi that bong duoc cong them 25% nua ngoai du kien.
                                //
                                // LICH SU: ban goc (bb83665) dat khoi khoi phuc nay o SAU khoi cong don
                                // nen ket qua cong don bi ghi de mat -> ba chi so giu nguyen. Commit
                                // 2480c47 doi len TRUOC, vo tinh bat growth len that. Nay quay lai hanh
                                // vi goc, nhung viet ro rang thay vi dua vao thu tu ghi de.
                                player.itemVukhiThoiTrang.otherAtt[54] = att54; // Sát thương chuẩn
                                player.itemVukhiThoiTrang.otherAtt[55] = att55; // Chỉ số vũ khí tăng
                                player.itemVukhiThoiTrang.otherAtt[56] = att56; // Tỷ lệ gây sát thương chuẩn


                                // Cập nhật allAtb
                                Vector<InfoItemAttribute> attributes = selectedItem.getInfoAtbItem();
                                player.itemVukhiThoiTrang.allAtb.clear();
                                for (InfoItemAttribute attr : attributes) {
                                    player.itemVukhiThoiTrang.allAtb.put(attr.id, attr);
                                }
        
                                // Copy các thuộc tính khác
                                player.itemVukhiThoiTrang.durable = selectedItem.durable;
                                player.itemVukhiThoiTrang.mDurable = selectedItem.mDurable;
                                player.itemVukhiThoiTrang.plus = selectedItem.plus;
                                player.itemVukhiThoiTrang.nhadSock = selectedItem.nhadSock;
                                player.itemVukhiThoiTrang.nSocAdd = selectedItem.nSocAdd;
                                player.itemVukhiThoiTrang.magic_physic = selectedItem.magic_physic;
                                player.itemVukhiThoiTrang.hangItem = selectedItem.hangItem;
                                player.itemVukhiThoiTrang.heItem = selectedItem.heItem;
                                player.itemVukhiThoiTrang.lock = selectedItem.lock;
                                player.itemVukhiThoiTrang.charSeal = selectedItem.charSeal;
                                player.itemVukhiThoiTrang.idAnimal = selectedItem.idAnimal;
                                player.itemVukhiThoiTrang.oldKham = selectedItem.oldKham;
                                player.itemVukhiThoiTrang.dateCreate = selectedItem.dateCreate;
                                player.itemVukhiThoiTrang.pos = selectedItem.pos;
                                player.itemVukhiThoiTrang.timeLoan = selectedItem.timeLoan;
                                player.itemVukhiThoiTrang.typetieu = selectedItem.typetieu;
                                player.itemVukhiThoiTrang.idNguHanh2 = selectedItem.idNguHanh2;
                                player.itemVukhiThoiTrang.isItemDrop = selectedItem.isItemDrop;
                                player.itemVukhiThoiTrang.idSellMarket = selectedItem.idSellMarket;
                                player.itemVukhiThoiTrang.isItemShop = selectedItem.isItemShop;
                                player.itemVukhiThoiTrang.timeExpireHkl = selectedItem.timeExpireHkl;
                                player.itemVukhiThoiTrang.id_dongxu_trung = selectedItem.id_dongxu_trung;
                                player.itemVukhiThoiTrang.quanlity = selectedItem.quanlity;
        
                                // Cập nhật ID template và màu sắc
                                player.itemVukhiThoiTrang.tempateID = (short)(oldId + 15);
                                player.itemVukhiThoiTrang.colorName = 4;
                                player.itemVukhiThoiTrang.minuteExist = 0;
                            }
                        }
        
                        // Thông báo thành công
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                            "Tinh luyện thành công! Cấp tinh luyện hiện tại: " +
                            player.itemVukhiThoiTrang.TinhLuyen, ""));

                        // Bao toan server khi tinh luyen thanh cong, kem % tinh luyen dat duoc
                        // va so lan that bai lien tiep TRUOC do (yeu cau nguoi dung 18/09/2026).
                        // Lay so lan that bai TRUOC khi reset ve 0.
                        final int soLanThatBaiTruocDo = player.itemVukhiThoiTrang.soLanTinhLuyenThatBai;
                        player.itemVukhiThoiTrang.soLanTinhLuyenThatBai = 0;
                        try {
                            Map.sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(
                                    "Chúc mừng " + player.charname + " đã tinh luyện thành công "
                                            + player.itemVukhiThoiTrang.getName() + " lên "
                                            + player.itemVukhiThoiTrang.TinhLuyen + "%"
                                            + (soLanThatBaiTruocDo > 0
                                                    ? " sau " + soLanThatBaiTruocDo + " lần thất bại liên tiếp"
                                                    : "")
                                            + "!"));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }

                        // An mung tinh luyen thanh cong: mua hoa cho TOAN SERVER (yeu cau nguoi
                        // dung 18/09/2026). Dung chung ham voi thuong len cap 60+ - xem
                        // muaHoaAnMungToanServer().
                        Map.muaHoaAnMungToanServer();
                    } else {
                        // That bai: cong don so lan that bai LIEN TIEP (chi de bao cao, KHONG
                        // doi ti le thanh cong) - reset ve 0 khi tinh luyen thanh cong o tren.
                        player.itemVukhiThoiTrang.soLanTinhLuyenThatBai++;
                        // Thông báo thất bại
                        player.sendMessage(MessageCreator.createServerAlertMessage(
                            "Tinh luyện thất bại! Đã thất bại " +
                            player.itemVukhiThoiTrang.soLanTinhLuyenThatBai + " lần liên tiếp.", ""));
                    }
        
                    // Cập nhật giao diện
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    showTinhLuyenMaterialList(player);
        
                    // Lưu log
                    String logInfo = String.format("tinh_luyen_%s_cap_%d_%s",
                        player.itemVukhiThoiTrang.getName(),
                        player.itemVukhiThoiTrang.TinhLuyen,
                        success ? "thanh_cong" : "that_bai");
                    Database.instance.saveOrtherLog("", player.charname, logInfo, "tinhluyentb");
        
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(idpopup == 123){
                if (!player.checkVuKhiThanBinh()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không có vật phẩm để gia hạn", ""));
                    return;
                }

                if (player.itemVukhiThoiTrang.GiaHan == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Trang bị đã hết số lần gia hạn", ""));
                    return;
                }

                if(player.itemVukhiThoiTrang.minuteExist <= 0){
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm vĩnh viễn không cần gia hạn", ""));
                    return;
                }

                // Tính số lần gia hạn đã dùng
                int lanGiaHan = 6 - player.itemVukhiThoiTrang.GiaHan;
                int giaLuong = 0;
                switch (lanGiaHan) {
                    case 1: giaLuong = 5000; break;
                    case 2: giaLuong = 10000; break;
                    case 3: giaLuong = 15000; break;
                    case 4: giaLuong = 20000; break;
                    case 5: giaLuong = 25000; break;
                    default: giaLuong = 5000; break;
                }

                if (player.getLuong() < giaLuong) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + giaLuong + " lượng để gia hạn", ""));
                    return;
                }

                player.itemVukhiThoiTrang.minuteExist += 7 * 24 * 60;
                // Decrease number of extensions remaining
                player.itemVukhiThoiTrang.GiaHan = Math.max(0, player.itemVukhiThoiTrang.GiaHan - 1);
                // Deduct currency cost
                player.subLuong((long)giaLuong);

                // Gửi lại thông tin hành trang để client cập nhật số lượng lượng mới
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Gia hạn thành công! Đã trừ " + giaLuong + " lượng.", ""));
                return;
            }

            if (idpopup == 114) {
                if (stopGiveMoney) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại chưa thể quyên góp cho bang.", ""));
                    return;
                }

                if (player.idClan == -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa gia nhập bang.", ""));
                    return;
                }

                if (ok == 0) {
                    String timeReceiveLuong = player.checkNhanLuongBang();
                    if (timeReceiveLuong != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể góp xu sau " + timeReceiveLuong, ""));
                        if (player.giveMoneyClan > 1) {
                            player.giveMoneyClan = 1;
                        }

                        return;
                    }
                } else if (player.pointCongHienClan < 200) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 200 điểm cống hiến", ""));
                    if (player.giveMoneyClan > 1) {
                        player.giveMoneyClan = 1;
                    }

                    return;
                }

                NewClan clandata = NewClan.getClan(player.idClan);
                if (clandata == null) {
                    return;
                }

                int money = player.giveMoneyClan;
                if (money <= 1) {
                    return;
                }

                if (player.giveMoneyClan <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể quyên góp xu vào bang 1 lần trong 1 ngày.", ""));
                    return;
                }

                if (player.lvDetail.lv < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt cấp độ 50 trở lên mới có thể quyên góp vào bang.", ""));
                    return;
                }

                if (player.getxu() < (long) money) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                    return;
                }

                player.giveMoneyClan = 0;
                if (ok == 1) {
                    player.pointCongHienClan -= 200;
                }

                Database.instance
                        .saveLogClan(
                                player.charname,
                                "give",
                                player.charname
                                + " "
                                + player.getxu()
                                + " >> góp "
                                + money
                                + " xu cho bang "
                                + clandata.name
                                + " | "
                                + clandata.id
                                + " > dung dch "
                                + (ok == 1 ? " yes" : "no")
                        );
                player.subXu((long) money, false, "map 777");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                clandata.addMoney2Clan((long) money);
                clandata.updateNewClandata2DB();
                Vector<CharClan> allmem = clandata.getAllMember();

                for (int i = 0; i < allmem.size(); i++) {
                    Char p = CharManager.instance.getCharByCharName(((CharClan) allmem.get(i)).charname);
                    if (p != null) {
                        p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " đã quyên góp " + money + " xu cho bang.", ""));
                    }
                }

                return;
            }

            if (idpopup == 113) {
                if (ok == 1) {
                    if (!Char.isOpenDauGiaChoiBlackFriday()) {
                        return;
                    }

                    if (player.moneyTrade < 500) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Giá đấu phải bắt đầu từ 500 lượng.", ""));
                        return;
                    }

                    if (player.getLuong() < player.moneyTrade) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    boolean isOk = Database.instance.doAddCharDauGia(player, player.moneyTrade);
                    if (isOk) {
                        int luong = player.getLuong();
                        player.subLuong((long) (player.moneyTrade / 2));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn đã đấu giá mua đồ thời trang với giá " + player.moneyTrade + " lượng. Bạn đã đặt cọc trước " + player.moneyTrade / 2 + " lượng.",
                                        ""
                                )
                        );
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        "dau gia mua do thoi trang voi gia "
                                        + player.moneyTrade
                                        + " luong. luong trc dau gia: "
                                        + luong
                                        + ", luong sau dau gia: "
                                        + player.getLuong(),
                                        "daugia"
                                );
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đấu giá rồi nên không thể tiếp tục tham gia đấu giá.", ""));
                        Database.instance.saveOrtherLog("", player.charname, "khong the tiep tuc dau gia voi gia " + player.moneyTrade + " luong", "daugiafail");
                    }

                    player.moneyTrade = 0;
                }

                return;
            }

            if (idpopup == 115) {
                if (ok == 1) {
                    if (!Char.isOpenDauGiaChoiBlackFriday()) {
                        return;
                    }

                    if (player.moneyTrade < 1000) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Giá đấu phải bắt đầu từ 1.000 lượng.", ""));
                        return;
                    }

                    if (player.getLuong() < player.moneyTrade) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    boolean isOk = Database.instance.doAddCharDauGia2(player, player.moneyTrade);
                    if (isOk) {
                        int luong = player.getLuong();
                        player.subLuong((long) (player.moneyTrade / 2));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn đã đấu giá mua lồng đèn với giá " + player.moneyTrade + " lượng. Bạn đã đặt cọc trước " + player.moneyTrade / 2 + " lượng.", ""
                                )
                        );
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        "dau gia mua long den voi gia "
                                        + player.moneyTrade
                                        + " luong. luong trc dau gia: "
                                        + luong
                                        + ", luong sau dau gia: "
                                        + player.getLuong(),
                                        "daugia"
                                );
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đấu giá rồi nên không thể tiếp tục tham gia đấu giá.", ""));
                        Database.instance.saveOrtherLog("", player.charname, "khong the tiep tuc dau gia voi gia " + player.moneyTrade + " luong", "daugiafail");
                    }

                    player.moneyTrade = 0;
                }

                return;
            }

            if (idpopup == 112) {
                if (ok == 1) {
                    Char p = CharManager.instance.getCharByCharName(player.nameTangPet);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trong game. Không thể giao dịch.", ""));
                        return;
                    }

                    Pet pet = null;
                    int ix = 0;

                    while (true) {
                        if (ix < p.pet.size()) {
                            pet = (Pet) p.pet.get(ix);
                            if (pet.id != p.idPetTang) {
                                ix++;
                                continue;
                            }
                        }

                        if (pet == null || !pet.canTrade()) {
                            return;
                        }

                        if (pet.isPetTradeThoiHan()) {
                            p.moneyTrade = 0;
                        }

                        if (player.getxu() < (long) p.moneyTrade) {
                            player.nameTangPet = "";
                            p.nameTangPet = "";
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                            p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " không đủ xu", ""));
                            return;
                        }

                        p.pet.remove(pet);
                        pet.id = player.getIDItem();
                        pet.ownerid = player.charDBID;
                        player.pet.add(pet);
                        long xuP = p.getxu();
                        long xuPL = player.getxu();
                        long moneytrade = (long) p.moneyTrade;
                        p.addXu(moneytrade * 90L / 100L, "map 26");
                        player.subXu(moneytrade, false, "map 27");
                        Database.instance.savePet(pet);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được " + pet.getName() + " từ " + p.charname, ""));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 2));
                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn giao dịch " + pet.getName() + " cho " + player.charname, ""));
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        p.charname,
                                        "giao dich " + pet.getName() + " cho " + player.charname + " voi gia " + p.moneyTrade + ", xt: " + xuP + ", xs: " + p.getxu(),
                                        "gdpet"
                                );
                        Database.instance
                                .saveOrtherLog(
                                        "",
                                        player.charname,
                                        "nhan " + pet.getName() + " tu " + p.charname + " voi gia " + p.moneyTrade + ", xt: " + xuPL + ", xs: " + player.getxu(),
                                        "gdpet"
                                );
                        p.moneyTrade = 0;
                        player.moneyTrade = 0;
                        p.nameTangPet = "";
                        player.nameTangPet = "";
                        break;
                    }
                }

                return;
            }

            if (idpopup == 111) {
                if (ok == 1) {
                    Char p = CharManager.instance.getCharByCharName(player.nameTangPet);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Người chơi không có trong game. Không thể giao dịch.", ""));
                        return;
                    }

                    Pet pet = null;
                    int ix = 0;

                    while (true) {
                        if (ix < player.pet.size()) {
                            pet = (Pet) player.pet.get(ix);
                            if (pet.id != player.idPetTang) {
                                ix++;
                                continue;
                            }
                        }

                        if (pet == null || !pet.canTrade()) {
                            return;
                        }

                        p.nameTangPet = player.charname;
                        p.sendMessage(
                                MessageCreator.createMsgPopUp(
                                        112, 112, player.charname + " muốn giao dịch " + pet.getName() + " với giá " + player.moneyTrade + ". Bạn có đồng ý không?"
                                )
                        );
                        break;
                    }
                }

                return;
            }

            if (idpopup == 110) {
                if (!openLog) {
                    return;
                }

                CharChuyenPhieuQua cReceive = (CharChuyenPhieuQua) ALL_CHAR_RECEIVE_KIM_CUONG.remove(player.charname);
                if (cReceive != null) {
                    ALL_CHAR_CHUYEN_KIM_CUONG.remove(cReceive.nameChuyen);
                }

                if ((cReceive == null || !cReceive.isTimeOver()) && cReceive != null) {
                    Char ch = CharManager.instance.getCharByCharName(cReceive.nameChuyen);
                    if (ch == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage(cReceive.nameChuyen + " chưa đăng nhập vào trò chơi.", ""));
                        return;
                    }

                    if (ok == 1) {
                        // SECURITY: da bo han chuc nang chuyen kim cuong qua server ben thu ba
                        // (truoc day gui userID + ten tai khoan 2 ben sang 27.0.14.41 bang HTTP thuong).
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng chuyển kim cương hiện không khả dụng.", ""));
                    } else {
                        ch.sendMessage(
                                MessageCreator.createServerAlertMessage(player.charname + " không đồng ý nhận " + cReceive.soluong + " kim cương của bạn.", "")
                        );
                    }

                    return;
                }

                player.sendMessage(MessageCreator.createServerAlertMessage("Hết thời gian giao dịch.", ""));
                return;
            }

            if ((idpopup == 100 || idpopup == 100) && idActor == player.id) {
                if (ok == 0) {
                    return;
                }

                player.infoXoso(idpopup - 100);
            } else if (idpopup == 0 && idActor == player.id) {
                if (player.idClan != -1) {
                    long moneyClan = NewClan.getMoneyClan(player.idClan);
                    long moneyUp = (long) NewClan.getMoneyUplevelClan(player.idClan);
                    if (!NewClan.enoughtXP(player.idClan)) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bang hội phải cần có " + NewClan.getXPUplevelClan(player.idClan) + " xp", ""));
                        return;
                    }

                    if (moneyClan < moneyUp) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bang không đủ tiền", ""));
                    } else {
                        NewClan c = NewClan.getClan(player.idClan);
                        c.addMoney2Clan(-moneyUp);
                        c.level++;
                        c.updateNewClandata2DB();
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng, cấp độ hiện tại của quý bang là " + c.level, ""));
                        NewClan clanInfo = getClaninfoByID(player.idClan);
                        if (clanInfo != null && clanInfo.master.toLowerCase().equals(c.master.toLowerCase())) {
                            clanInfo.money = c.money;
                            clanInfo.xp = c.xp;
                            clanInfo.level = c.level;
                        }

                        Database.instance.saveOrtherLog("tob_other_log", player.charname, String.valueOf(moneyUp), "uclan");
                    }
                }
            } else if (idpopup == 1 && idActor == player.id) {
                if (!this.isvanTienTran()) {
                    int[] cost = new int[]{
                        1000, 2000, 3000, 4000, 5000, 10000, 15000, 20000, 30000, 60000, 70000, 500000, 1000000, 1300000, 1500000, 2000000, 2500000, 3000000, 4500000
                    };
                    int cp = cost[player.lvDetail.lv / 5];
                    if (player.myCountry != player.inCountry && player.charthanthu != null) {
                        cp = cost[player.lastLV / 5];
                    }

                    if (player.charthanthu != null && player.isKiller) {
                        cp *= 2;
                    }

                    if (player.mapID == 112 || player.mapID == 2541 || player.mapID == 2542 || player.mapID == 2543 || player.mapID == 2544) {
                        cp = 500000;
                    }

                    if (player.mapID == 204 || player.mapID == 205) {
                        cp = 0;
                    }

                    if (player.getxu() < (long) cp) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                        return;
                    }

                    if (isMapThanh(this) && !player.canHSChiemThanh() && getTown[player.inCountry]) {
                        long time = 1L - (System.currentTimeMillis() - player.timeWaitComeHome) / 1000L;
                        if (time < 1L) {
                            time = 1L;
                        }

                        if (HASH_ALL_CHAR_HS.get(player.charDBID) == null) {
                            ALL_CHAR_HS.add(player);
                            HASH_ALL_CHAR_HS.put(player.charDBID, player);
                        }

                        return;
                    }

                    player.subXu((long) cp, true, "map 31");
                    player.hp = player.maxhp;
                    player.mp = player.maxmp;
                    player.timeHs = Char.timeAddMoreHs;
                    player.desTroy();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendPetEff();
                    Database.instance.saveOrtherLog("tob_other_log", player.charname, String.valueOf(cp), "hs");
                } else {
                    int cpx = 100000;
                    if (player.getxu() < (long) cpx) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hết tiền", ""));
                        return;
                    }

                    player.subXu((long) cpx, true, "map 28");
                    player.hp = player.maxhp;
                    player.mp = player.maxmp;
                    player.timeHs = Char.timeAddMoreHs;
                    player.desTroy();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createNew_HMP_Message(player, 0));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendPetEff();
                    Database.instance.saveOrtherLog("tob_other_log", player.charname, "1 luong", "hsl");
                }
            } else if (idpopup == 2 && idActor == player.id) {
                this.buyItemOfUser(player);
            } else if (idpopup != 3 || idActor != player.id) {
                if (idpopup == 4 && idActor == player.id) {
                    if (!isMapLang(player.map)) {
                        return;
                    }

                    if (player.maxBag < Char.MAX_BAG_LIMIT && player.getLuong() >= player.luongOpenInventory) {
                        player.subLuong((long) player.luongOpenInventory);
                        player.maxBag++;
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng, hành trang của bạn đã thêm được 42 ô.", ""));
                        Database.instance.saveOrtherLog("tob_other_log", player.charname, "mo rong hanh trang " + player.luongOpenInventory, "mi");
                        Database.instance.saveLogThongKeChiTieu("mo rong hanh trang", 0, 1, (long) player.luongOpenInventory, "luong");
                    }
                } else if (idpopup == 5 && idActor == player.id) {
                    player.doOpenAllLuckyBox();
                } else if (idpopup == 6 && idActor == player.id) {
                    if (player.monster != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể dịch chuyển khi đang vận tiêu", ""));
                        return;
                    }

                    Char p = CharManager.instance.getCharByCharName(Char.gov[player.myCountry].king);
                    if (p != null) {
                        this.move2Map(player, p.x / 16, p.y / 16, p.mapID, p.inCountry);
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    }
                } else if (idpopup == 7 && idActor == player.id) {
                    if (player.idWedding > -1) {
                        return;
                    }

                    Char p = CharManager.instance.getCharByCharName(player.pInviteWedding);
                    if (p != null) {
                        if (p.potions[92] <= 0 || p.idWedding == -1) {
                            return;
                        }

                        Wedding wd = (Wedding) allWedding.get(p.idWedding);
                        if (wd == null) {
                            return;
                        }

                        player.idWedding = p.idWedding;
                        player.potions[92] = 1;
                        p.potions[92]--;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        p.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " đã nhận lời tham gia tiệc cưới của bạn", ""));
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn đã nhận lời tham gia tiệc cưới của " + p.charname + ". Tiệc cưới sẽ diễn ra ngày " + wd.dayParty, ""
                                )
                        );
                        Database.instance.saveOrtherLog("", player.charname, "ok wedding " + p.charname, "okIVT");
                        wd.listMember.put(player.charDBID, player.userID);
                        player.pInviteWedding = "";
                    }
                } else if (idpopup == 8 && idActor == player.id) {
                    if (player.gender == 2) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có phu quân của bạn mới có thể chọn chức năng này.", ""));
                        return;
                    }

                    if (player.party == null || player.party != null && !player.party.checkConditionLydi()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ điều kiện để ly dị.", ""));
                        return;
                    }

                    if (player.nameHusband_wife.equals("")) {
                        return;
                    }

                    Wedding wd = (Wedding) allWedding.get(player.idWedding);
                    if (wd != null && wd.dayParty.equals(Char.getDayOpen(0L))) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể chọn chức năng này vào ngày mai.", ""));
                        return;
                    }

                    Char p = CharManager.instance.getCharByCharName(player.nameHusband_wife);
                    if (p == null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Phu nhân của bạn hiện tại chưa online", ""));
                        return;
                    }

                    if (player.getLuong() < 150) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có 150 lượng mới có thể ly dị được.", ""));
                        return;
                    }

                    if (p.getLuong() < 150) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Phu nhân của bạn phải có 150 lượng mới có thể ly dị được.", ""));
                        return;
                    }

                    Database.instance.updateStatusWedding(player.charDBID, 2);
                    player.nameHusband_wife = "";
                    player.married = 0;
                    player.potions[92] = 0;
                    player.potions[88] = 0;
                    player.potions[89] = 0;
                    player.potions[90] = 0;
                    player.potions[98] = 0;
                    player.idWedding = -1;
                    player.subLuong(150L);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn và " + p.charname + " đã ly dị.", ""));
                    p.nameHusband_wife = "";
                    p.married = 0;
                    p.potions[92] = 0;
                    p.potions[88] = 0;
                    p.potions[89] = 0;
                    p.potions[90] = 0;
                    p.potions[98] = 0;
                    p.idWedding = -1;
                    p.subLuong(150L);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn và " + player.charname + " đã ly dị.", ""));
                    Database.instance.saveOrtherLog("", player.charname, p.charname, "lydi");
                    p.calculateAttrib();
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } else if (idpopup == 9 && idActor == player.id) {
                    if (player.nameHusband_wife.equals("")) {
                        return;
                    }

                    Wedding wdx = (Wedding) allWedding.get(player.idWedding);
                    if (wdx != null && wdx.dayParty.equals(Char.getDayOpen(0L))) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ có thể chọn chức năng này vào ngày mai.", ""));
                        return;
                    }

                    if (player.getLuong() < 300) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có 300 lượng mới có thể đơn phương ly dị được.", ""));
                        return;
                    }

                    Database.instance.updateStatusWedding(player.charDBID, 2);
                    player.married = 0;
                    player.potions[92] = 0;
                    player.potions[88] = 0;
                    player.potions[89] = 0;
                    player.potions[90] = 0;
                    player.potions[98] = 0;
                    player.idWedding = -1;
                    player.subLuong(300L);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn và " + player.nameHusband_wife + " đã ly dị.", ""));
                    Char px = CharManager.instance.getCharByCharName(player.nameHusband_wife);
                    if (px != null) {
                        px.nameHusband_wife = "";
                        px.married = 0;
                        px.potions[92] = 0;
                        px.potions[88] = 0;
                        px.potions[89] = 0;
                        px.potions[90] = 0;
                        px.potions[98] = 0;
                        px.idWedding = -1;
                        px.sendMessage(MessageCreator.createServerAlertMessage("Bạn và " + player.charname + " đã ly dị.", ""));
                        px.sendMessage(MessageCreator.createCharInventoryMessage(px, 0));
                        px.calculateAttrib();
                        px.sendMessage(MessageCreator.createMainCharInfoMessage(px));
                    } else {
                        Database.instance.resetStateMarried(player.nameHusband_wife, player.charname);
                    }

                    Database.instance.saveOrtherLog("", player.charname, player.nameHusband_wife, "lydi1");
                    player.nameHusband_wife = "";
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                } else if (idpopup == 10 && idActor == player.id) {
                    Char px = CharManager.instance.getCharByCharName(player.nameCompetitor);
                    if (px != null) {
                        if (ok == 0) {
                            px.nameCompetitor = "";
                            px.moneyThachdau = 0;
                            px.sendMessage(MessageCreator.createServerAlertMessage(player.charname + " không chấp nhận lời thách đấu của bạn", ""));
                            return;
                        }

                        if (px.moneyThachdau <= 0) {
                            px.moneyThachdau = 0;
                            player.sendMessage(MessageCreator.createServerAlertMessage("Số tiền cược không hợp lệ", ""));
                            return;
                        }

                        if (player.getxu() < (long) px.moneyThachdau) {
                            px.nameCompetitor = "";
                            px.moneyThachdau = 0;
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                            return;
                        }

                        if (px.getxu() < (long) px.moneyThachdau) {
                            px.nameCompetitor = "";
                            px.moneyThachdau = 0;
                            player.sendMessage(MessageCreator.createServerAlertMessage(px.charname + " Không đủ xu", ""));
                            return;
                        }

                        px.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        player.charname + " đã chấp nhận lời thách đấu của bạn. Trận đấu của bạn và " + player.nameCompetitor + " sẽ diễn ra sau 30s nữa.", ""
                                )
                        );
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn đã chấp nhận lời thách đấu của " + px.charname + ". Trận đấu của bạn và " + px.nameCompetitor + " sẽ diễn ra sau 30s nữa.", ""
                                )
                        );
                        TeamArena team1 = new TeamArena(TeamArena.genIDTeam());
                        TeamArena team2 = new TeamArena(TeamArena.genIDTeam());
                        ArenaMap mm = (ArenaMap) RealController.mapList.get(205);
                        ArenaData arena = new ArenaData();
                        arena.addTeam(team1);
                        arena.addTeam(team2);
                        arena.idArena = IDARENA;
                        arena.typeArena = 3;
                        arena.start();
                        mm.doAddArena(arena);
                        px.idArena = IDARENA;
                        player.idArena = IDARENA;
                        arena.moneyThachdau = px.moneyThachdau * 2;
                        arena.moneyThachdau = arena.moneyThachdau - arena.moneyThachdau / 10;
                        long pxu = px.getxu();
                        long playerxu = player.getxu();
                        px.subXu((long) px.moneyThachdau, false, "map 30");
                        player.subXu((long) px.moneyThachdau, false, "map 29");
                        Database.instance
                                .saveOrtherLog(
                                        "", px.charname, player.charname + " chap nhan loi thach dau " + px.moneyThachdau + " xu " + pxu + " > " + playerxu, "challen"
                                );
                        Database.instance
                                .saveOrtherLog(
                                        "", player.charname, px.charname + " chap nhan loi thach dau " + px.moneyThachdau + " xu " + playerxu + " > " + pxu, "challen"
                                );
                        team1.addMember(px);
                        team2.addMember(player);
                        px.sendMessage(MessageCreator.createCharInventoryMessage(px, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Message m = new Message(65);
                        m.dos.writeShort(px.id);
                        if (px.pk == 0) {
                            px.pk = 14;
                            m.dos.writeByte(1);
                            m.dos.writeByte(14);
                            px.timeUsePK = System.currentTimeMillis();
                            px.sendMessage(m);
                        }

                        m = new Message(65);
                        m.dos.writeShort(player.id);
                        if (player.pk == 0) {
                            player.pk = 15;
                            m.dos.writeByte(1);
                            m.dos.writeByte(15);
                            player.timeUsePK = System.currentTimeMillis();
                            player.sendMessage(m);
                        }

                        arena.sendInfoMatch = true;
                        arena.timeWaitFight = System.currentTimeMillis() + 1000L;
                        IDARENA = (IDARENA + 1) % 2000000000;
                        player.nameCompetitor = "";
                        px.nameCompetitor = "";
                        px.moneyThachdau = 0;
                        player.moneyThachdau = 0;
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage(player.nameCompetitor + " đã thoát khỏi game", ""));
                    }
                } else if (idpopup == 11 && idActor == player.id) {
                    this.doAcceptChallengeParty(player, ok);
                } else if (idpopup == 12 && idActor == player.id) {
                    this.doPopUpMapTown(player, idpopup, idActor, ok == 1);
                } else if (idpopup == 13 && idActor == player.id) {
                    if (!Char.isSuKienHe2017()) {
                        return;
                    }

                    if (player.countnvVulan >= 100) {
                        player.sendMessage(MessageCreator.createMsgChat(-49, "Con ta đã về nhà rồi. Chúc ngươi may mắn"));
                        return;
                    }

                    if (player.potions[142] <= 0) {
                        return;
                    }

                    if (player.receiveQuestVulan == 0 && ok == 1) {
                        player.countnvVulan++;
                        player.doInitCharCopy();
                        player.receiveQuestVulan = 1;
                        player.potions[142]--;
                        player.sendMessage(MessageCreator.createMsgChat(-49, "Cảm ơn ngươi đã nhận lời giúp đỡ của ta. Chúc ngươi may mắn"));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else {
                        player.sendMessage(
                                MessageCreator.createMsgChat(-49, "Cảm ơn ngươi. Trên đường hành tẩu hãy làm nhiều việc nghĩa hiệp nhé. Chúc ngươi may mắn")
                        );
                    }
                } else if (idpopup == 14 && idActor == player.id) {
                    if (!Char.isSuKienMiniChucNu()) {
                        return;
                    }

                    if (player.countnvVulan >= 100) {
                        player.sendMessage(MessageCreator.createMsgChat(idNPC49, "Chức Nữ đã về rồi. Chúc ngươi may mắn"));
                        return;
                    }

                    if (player.potions[77] <= 0) {
                        return;
                    }

                    if (player.receiveQuestVulan == 0 && ok == 1) {
                        player.countnvVulan++;
                        player.doInitCharCopy();
                        player.receiveQuestVulan = 1;
                        player.potions[77]--;
                        player.sendMessage(MessageCreator.createMsgChat(idNPC49, "Cảm ơn ngươi đã nhận lời giúp đỡ của ta. Chúc ngươi may mắn"));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    } else {
                        player.sendMessage(
                                MessageCreator.createMsgChat(idNPC49, "Cảm ơn ngươi. Trên đường hành tẩu hãy làm nhiều việc nghĩa hiệp nhé. Chúc ngươi may mắn")
                        );
                    }
                } 
                else if (idpopup == -1 && idActor == player.id) 
                {
                    if (!this.isMapChienTruongMoba()) 
                    {
                        if (!this.isvanTienTran()) 
                        {
                            if (player.myCountry > -1 && !((Vector) idMapMONSTER.get(player.myCountry)).contains(player.map.mapId) && player.hp <= 0) 
                            {
                                int[] costx = new int[]{
                                    1000,
                                    2000,
                                    3000,
                                    4000,
                                    5000,
                                    10000,
                                    15000,
                                    20000,
                                    30000,
                                    60000,
                                    70000,
                                    500000,
                                    1000000,
                                    1300000,
                                    1500000,
                                    2000000,
                                    2500000,
                                    3000000,
                                    4500000
                                };
                                int cpx = costx[player.lvDetail.lv / 5];
                                if (player.mapID != 112 && player.mapID != 2541 && player.mapID != 2542 && player.mapID != 2543 && player.mapID != 2544) 
                                {
                                    if (player.myCountry != player.inCountry && player.charthanthu != null)
                                    {
                                        cpx = costx[player.lastLV / 5];
                                    }

                                    if (player.charthanthu != null && player.isKiller) 
                                    {
                                        cpx *= 2;
                                    }
                                } else {
                                    cpx = 500000;
                                }

                                if (player.mapID == 204 || player.mapID == 205) 
                                {
                                    cpx = 0;
                                }
                                if (Map.getTown[0] || Map.getTown[1] || Map.getTown[2]) 
                                {
//                                     cpx = 500000; // xu hồi sinh chiếm thành
                                }
                                
                                if (cpx > 0) 
                                {
                                    if (player.potions[119] > 0) 
                                    {
                                        String[] menu = new String[]{"Dùng tiên đan", "Hs bằng xu (" + cpx + " xu)"};
                                        player.sendMessage(MessageCreator.createMsgMenu(menu, -802, 0));
                                    } 
                                    else 
                                    {
                                        player.sendMessage(MessageCreator.createMsgPopUp(player.id, 1, "Bạn muốn hồi sinh tại chỗ không. Chi phí " + cpx + " xu"));
                                    }
                                } else {
                                    player.sendMessage(MessageCreator.createMsgPopUp(player.id, 1, "Bạn muốn hồi sinh tại chỗ không?"));
                                }
                            }
                        } 
                        else if (player.potions[119] > 0) 
                        {
                            String[] menu = new String[]{"Dùng tiên đan", "Hs bằng xu (100000 xu)"};
                            player.sendMessage(MessageCreator.createMsgMenu(menu, -802, 1));
                        } 
                        else 
                        {
                            player.sendMessage(MessageCreator.createMsgPopUp(player.id, 1, "Bạn muốn hồi sinh tại chỗ không. Chi phí 100000 xu"));
                        }
                    }
                } 
                else if (idpopup >= 14 && idpopup < 24 && idActor == player.id) 
                {
                    if (player.itemLinhDanhThue == null)
                    {
                        return;
                    }

                    // Giua luc dat itemLinhDanhThue (doUseItem, khi bam "Dung the bai") va luc
                    // popup nay duoc xac nhan, the co the da roi tui (gui vao kho, ban, giao
                    // dich). Truoc day RUT (tieu the) chay SAU khi CAP (trieu hoi ho ve) - phai
                    // dao lai: tru truoc, chi cap khi tru thanh cong, khong thi mot the trieu
                    // hoi duoc ho ve vo han lan.
                    if (!player.iItems.remove(player.itemLinhDanhThue)) {
                        player.itemLinhDanhThue = null;
                        return;
                    }
                    player.removeIDItem(player.itemLinhDanhThue.id);

                    int clazz = (idpopup - 14) / 2;
                    int gender = (idpopup - 14) % 2;
                    player.doCreateCharHire(clazz, player.itemLinhDanhThue.level, gender, player.itemLinhDanhThue.getTemplate().atb[0]);
                    if (player.itemLinhDanhThue.minuteExist > 0) {
                        if (player.itemLinhDanhThue.timeLoan > 0L) 
                        {
                            player.timeExistCharHire = player.itemLinhDanhThue.timeLoan + (long) player.itemLinhDanhThue.minuteExist * 60000L;
                        } else {
                            player.timeExistCharHire = System.currentTimeMillis() + (long) player.itemLinhDanhThue.minuteExist * 60000L;
                        }
                    } else {
                        player.timeExistCharHire = System.currentTimeMillis() + 604800000L;
                    }

                    Database.instance.saveOrtherLog("", player.charname, player.itemLinhDanhThue.getInfoSave(), "hove");
                    player.itemLinhDanhThue = null;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                    // Ho ve lan (the bai co level 64, tuc "Ho ve lan su tu 6x lv 3")
                    // cong chi so cho CHINH NGUOI CHOI qua Char.isLinhThueLan():
                    //   +5% tan cong   LiveActor.getAttack()
                    //   +5 lua lan     CharChiSo.calculateDefend()
                    //   +2% sat thuong gay ra / hap thu 3% sat thuong nhan
                    // Hai muc sau tinh ngay luc danh nen van an. Nhung +5 lua lan
                    // chi duoc cong khi chay lai calculateDefend, con so tan cong
                    // hien cho nguoi choi thi di trong goi 3 (MessageCreator ghi
                    // p.getAttack()). Truoc day cho nay chi gui lai tui do, nen
                    // trieu ho ve xong nguoi choi khong thay tan cong nhuc nhich
                    // va cung chua thuc su duoc +5 lua lan.
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                }
                else if (idpopup == 121)
                {
                    ThoRenThanBi.CreateThanKhi(player);
                }
            }
        } catch (Exception var19) {
            var19.printStackTrace();
        }
    }

    protected void doPopUpMapTown(Char p, int idpopup, int idActor, boolean ok) {
    }

    // Than method (120 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public void doAcceptChallengeParty(Char player, int ok) {
        MapXuLyLenh.doAcceptChallengeParty(this, player,ok);
    }


    public void doProcessInputTextDauGia(Char player, int idActor, int idMenu, String text) {
        try {
            if (!Char.isOpenDauGiaChoiBlackFriday()) {
                return;
            }

            long sl = (long) Integer.parseInt(text.trim());
            if (sl <= 0L || sl > 2000000000L) {
                return;
            }

            if (sl < 500L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Giá đấu phải bắt đầu từ 500 lượng.", ""));
                return;
            }

            if ((long) player.getLuong() < sl) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                return;
            }

            System.out.println("DAU GIA NE " + idActor);
            player.moneyTrade = (int) sl;
            if (idActor == -32018) {
                player.sendMessage(
                        MessageCreator.createMsgPopUp(
                                113, 113, "Bạn có muốn đấu giá mua đồ thời trang với giá " + sl + " lượng không?. Bạn sẽ phải đặt cọc với số lượng là " + sl / 2L + " lượng."
                        )
                );
            } else {
                player.sendMessage(
                        MessageCreator.createMsgPopUp(
                                115, 115, "Bạn có muốn đấu giá mua lồng đèn với giá " + sl + " lượng không?. Bạn sẽ phải đặt cọc với số lượng là " + sl / 2L + " lượng."
                        )
                );
            }
        } catch (Exception var7) {
        }
    }

    public void doProcessInputTextTet(Char player, int idActor, int idMenu, String text) {
        try {
            if (idActor == -32016) {
                if (Char.isFinishAllSuKienTet2017()) {
                    return;
                }

                long sl = (long) Integer.parseInt(text.trim());
                if (sl <= 0L || sl > 500000L || sl * 10000000L <= 0L) {
                    return;
                }

                if (player.getxu() < sl * 10000000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }

                if ((long) player.potions[154] < sl * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[154], ""));
                    return;
                }

                if ((long) player.potions[153] < sl * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[153], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getLuong());
                player.subXu(sl * 10000000L, false, "doprocessinputtet");
                player.potions[154] = (int) ((long) player.potions[154] - sl * 10L);
                player.potions[153] = (int) ((long) player.potions[153] - sl * 10L);
                player.potions[155] = (int) ((long) player.potions[155] + sl);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + sl + " pháo hoa", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh nuong " + sl, "phaohoaxu");
                Database.instance.saveLogThongKeChiTieu("Doi phao hoa xu", 0, (int) sl, sl * 10000000L, "xu");
            } else if (idActor == -32017) {
                if (Char.isFinishAllSuKienTet2017()) {
                    return;
                }

                long slx = (long) Integer.parseInt(text.trim());
                if (slx <= 0L || slx > 500000L || slx * 10000000L <= 0L) {
                    return;
                }

                if ((long) player.getLuong() < slx * 6L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                if ((long) player.potions[154] < slx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[154], ""));
                    return;
                }

                if ((long) player.potions[153] < slx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[153], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getLuong());
                player.subLuong(slx * 6L);
                player.potions[154] = (int) ((long) player.potions[154] - slx * 10L);
                player.potions[153] = (int) ((long) player.potions[153] - slx * 10L);
                player.potions[155] = (int) ((long) player.potions[155] + slx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slx + " pháo hoa", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh nuong " + slx, "phaohoaluong");
                Database.instance.saveLogThongKeChiTieu("Doi phao hoa luong", 0, (int) slx, slx * 6L, "luong");
            }
        } catch (Exception var8) {
        }
    }

    public void doProcessInputTextTrungThu(Char player, int idActor, int idMenu, String text) {
        try {
            if (idActor == -32018) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long sl = (long) Integer.parseInt(text.trim());
                if (sl <= 0L || sl > 2000000000L) {
                    return;
                }

                if ((long) player.getLuong() < sl * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                // Trung Thu 2026: hop dac biet chi con can 5 banh nuong + 5 banh deo (truoc
                // la 10+10) cho moi hop, theo dung cong thuc moi.
                if ((long) player.potions[125] < sl * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh nướng", ""));
                    return;
                }

                if ((long) player.potions[124] < sl * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    return;
                }

                player.subLuong(sl * 5L);
                player.potions[125] = (int) ((long) player.potions[125] - sl * 5L);
                player.potions[124] = (int) ((long) player.potions[124] - sl * 5L);
                player.potions[134] = (int) ((long) player.potions[134] + sl);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + sl + " hop banh db", "banhdb");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + sl + " hộp bánh đặc biệt", ""));
                Database.instance.saveLogThongKeChiTieu("hop dac biet", 0, (int) sl, sl * 5L, "luong");
            } else if (idActor == -32017) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slx = (long) Integer.parseInt(text.trim());
                if (slx <= 0L || slx > 2000000000L) {
                    return;
                }

                if (player.getxu() < slx * 5000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }

                // Trung Thu 2026: hop thap cam chi con can 5 banh nuong + 5 banh deo (truoc
                // la 10+10) cho moi hop, theo dung cong thuc moi.
                if ((long) player.potions[125] < slx * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh nướng", ""));
                    return;
                }

                if ((long) player.potions[124] < slx * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    return;
                }

                player.subXu(slx * 5000L, false, "32017");
                player.potions[125] = (int) ((long) player.potions[125] - slx * 5L);
                player.potions[124] = (int) ((long) player.potions[124] - slx * 5L);
                player.potions[133] = (int) ((long) player.potions[133] + slx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + slx + " hop banh thap cam", "banhtc");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + slx + " hộp bánh thập cẩm", ""));
                Database.instance.saveLogThongKeChiTieu("hop thap cam", 0, (int) slx, slx * 5000L, "xu");
            } else if (idActor == -32016) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slxx = (long) Integer.parseInt(text.trim());
                if (slxx <= 0L || slxx > 2000000000L || slxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[142] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if ((long) player.potions[122] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[122], ""));
                    return;
                }

                if ((long) player.potions[121] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[121], ""));
                    return;
                }

                if ((long) player.potions[120] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[120], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getLuong());
                player.potions[142] = (int) ((long) player.potions[142] - slxx * 10L);
                player.potions[122] = (int) ((long) player.potions[122] - slxx * 10L);
                player.potions[121] = (int) ((long) player.potions[121] - slxx * 10L);
                player.potions[120] = (int) ((long) player.potions[120] - slxx * 10L);
                player.potions[125] = (int) ((long) player.potions[125] + slxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxx + " bánh nướng", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh nuong " + slxx, "banhnuong");
                Database.instance.saveLogThongKeChiTieu("Doi banh nuong", 0, (int) slxx, 0L, "banhnuong");
            } else if (idActor == -32015) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slxxx = (long) Integer.parseInt(text.trim());
                if (slxxx <= 0L || slxxx > 2000000000L || slxxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[142] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if ((long) player.potions[122] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[122], ""));
                    return;
                }

                if ((long) player.potions[121] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[121], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getxu());
                player.potions[142] = (int) ((long) player.potions[142] - slxxx * 10L);
                player.potions[122] = (int) ((long) player.potions[122] - slxxx * 10L);
                player.potions[121] = (int) ((long) player.potions[121] - slxxx * 10L);
                player.potions[124] = (int) ((long) player.potions[124] + slxxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxxx + " bánh dẻo", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh deo " + slxxx + "xu trc" + infoxu + ", xu còn " + player.getxu(), "banhdeo");
                Database.instance.saveLogThongKeChiTieu("Doi banh deo", 0, (int) slxxx, 0L, "banhdeo");
            }
        } catch (Exception var8) {
        }
    }

    public void doProcessInputTextHalloWeen2016(Char player, int idActor, int idMenu, String text) {
        try {
            if (idActor == -32018) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long sl = (long) Integer.parseInt(text.trim());
                if (sl <= 0L || sl > 2000000000L) {
                    return;
                }

                if ((long) player.getLuong() < sl * 5L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                if ((long) player.potions[125] < sl * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh nướng", ""));
                    return;
                }

                if ((long) player.potions[124] < sl * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    return;
                }

                player.subLuong(sl * 5L);
                player.potions[125] = (int) ((long) player.potions[125] - sl * 10L);
                player.potions[124] = (int) ((long) player.potions[124] - sl * 10L);
                player.potions[134] = (int) ((long) player.potions[134] + sl);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + sl + " hop banh db", "banhdb");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + sl + " hộp bánh đặc biệt", ""));
                Database.instance.saveLogThongKeChiTieu("hop dac biet", 0, (int) sl, sl * 5L, "luong");
            } else if (idActor == -32017) {
                if (Char.isFinishAllSuKienHaloween2016()) {
                    return;
                }

                long slx = (long) Integer.parseInt(text.trim());
                if (slx <= 0L || slx > 2000000000L) {
                    return;
                }

                if (player.luongxainhanRuong < slx * 1000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                    return;
                }

                player.luongxainhanRuong -= slx * 1000L;
                player.potions[143] = (int) ((long) player.potions[143] + slx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "doi  " + slx + " hop qua bi mat. còn lại " + player.luongxainhanRuong + " diem", "hopbimat");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được " + slx + " hộp quà bí mật", ""));
                Database.instance.saveLogThongKeChiTieu("hop qua bi mat", 0, (int) slx, slx * 1000L, "diem");
            } else if (idActor == -32016) {
                if (Char.isFinishAllSuKienHaloween2016()) {
                    return;
                }

                long slxx = (long) Integer.parseInt(text.trim());
                if (slxx <= 0L || slxx > 2000000000L || slxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[137] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[137], ""));
                    return;
                }

                if ((long) player.potions[138] < slxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[138], ""));
                    return;
                }

                if ((long) player.potions[139] < slxx) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[139], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getLuong());
                player.potions[137] = (int) ((long) player.potions[137] - slxx * 10L);
                player.potions[138] = (int) ((long) player.potions[138] - slxx * 10L);
                player.potions[139] = (int) ((long) player.potions[139] - slxx);
                player.potions[140] = (int) ((long) player.potions[140] + slxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxx + " " + LoginHandler.PORTION_NAME[140], ""));
                Database.instance.saveOrtherLog("", player.charname, LoginHandler.PORTION_NAME[140] + " " + slxx, "hopqua");
                Database.instance.saveLogThongKeChiTieu(LoginHandler.PORTION_NAME[140], 0, (int) slxx, 0L, "doi hop qua");
            } else if (idActor == -32015) {
                if (Char.isFinishAllSuKienTrungThul2016()) {
                    return;
                }

                long slxxx = (long) Integer.parseInt(text.trim());
                if (slxxx <= 0L || slxxx > 2000000000L || slxxx * 10L <= 0L) {
                    return;
                }

                if ((long) player.potions[142] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if ((long) player.potions[122] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[122], ""));
                    return;
                }

                if ((long) player.potions[121] < slxxx * 10L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[121], ""));
                    return;
                }

                String infoxu = String.valueOf(player.getxu());
                player.potions[142] = (int) ((long) player.potions[142] - slxxx * 10L);
                player.potions[122] = (int) ((long) player.potions[122] - slxxx * 10L);
                player.potions[121] = (int) ((long) player.potions[121] - slxxx * 10L);
                player.potions[124] = (int) ((long) player.potions[124] + slxxx);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slxxx + " bánh dẻo", ""));
                Database.instance.saveOrtherLog("", player.charname, "banh deo " + slxxx + "xu trc" + infoxu + ", xu còn " + player.getxu(), "banhdeo");
                Database.instance.saveLogThongKeChiTieu("Doi banh deo", 0, (int) slxxx, 0L, "banhdeo");
            }
        } catch (Exception var8) {
        }
    }

    public void doProcessInputTextNoel(Char player, int idActor, int idMenu, String text) {
        try {
            if (idActor != -32018) {
                if (idActor == -32017) {
                    if (Char.isFinishAllSuKienNoel()) {
                        return;
                    }

                    long sl = (long) Integer.parseInt(text.trim());
                    if (sl <= 0L || sl > 2000000000L) {
                        return;
                    }

                    if ((long) player.getLuong() < sl * 10L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    if ((long) player.potions[146] < sl * 10L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[146], ""));
                        return;
                    }

                    player.subLuong(sl * 10L);
                    player.potions[146] = (int) ((long) player.potions[146] - sl * 10L);
                    player.potions[147] = (int) ((long) player.potions[147] + sl);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + sl + " túi quà may mắn", ""));
                    Database.instance.saveOrtherLog("", player.charname, "tui qua may man " + sl, "tuiqua");
                    Database.instance.saveLogThongKeChiTieu("tui qua may man", 0, (int) sl, sl * 10L, "luong");
                } else if (idActor == -32016) {
                    if (Char.isFinishAllSuKienNoel()) {
                        return;
                    }

                    long slx = (long) Integer.parseInt(text.trim());
                    if (slx <= 0L || slx > 2000000000L) {
                        return;
                    }

                    if (player.getxu() < slx * 20000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    if ((long) player.potions[145] < slx * 100L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + LoginHandler.PORTION_NAME[145], ""));
                        return;
                    }

                    player.subXu(slx * 20000L, false, "doprocessinputnoel");
                    player.potions[145] = (int) ((long) player.potions[145] - slx * 100L);
                    player.potions[146] = (int) ((long) player.potions[146] + slx);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + slx + " kem", ""));
                    Database.instance.saveOrtherLog("", player.charname, "kem " + slx, "kem");
                    Database.instance.saveLogThongKeChiTieu("Kem", 0, (int) slx, slx * 20000L, "xu");
                }
            }
        } catch (Exception var7) {
        }
    }

    // Than method (1432 dong) da chuyen sang real/MapNhapText.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    protected void doProcessInputText(Char player, Message message) {
        MapNhapText.doProcessInputText(this, player,message);
    }



    public static void initGiftCode() {
        gifcodengudetanthu = new String[0];
        gifcodengudeTrian = new String[0];
        gifcodengudeTopVanlang = new String[0];
    }

    void dosetGiftCodeTanThuNgude(Char player, String code) {
        player.doAddSpecialItem(0);
        player.potions[69] = player.potions[69] + 5;
        doBuyModelClothe(player, getShopTemplate(player.isCharNu() ? 50 : 54), 3);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(
                MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được 5 vé quay số, 1 vé giờ vàng, 1 áo thời trang có thời hạn 3 ngày", "")
        );
        Database.instance.saveOrtherLog("", player.charname, code + " nhận được 5 vé quay số, 1 vé giờ vàng, 1 áo thời trang có thời hạn 3 ngày", "gcode");
    }

    void dosetGiftCodeTrianNgude(Char player, String code) {
        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 2, true);
            player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 2, true);
        }

        doBuyModelClothe(player, getShopTemplate(player.isCharNu() ? 50 : 54), 7);
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(
                MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được nlsc, nlcc cấp 6 mỗi loại 2 viên, 1 áo thời trang có thời hạn 7 ngày", "")
        );
        Database.instance
                .saveOrtherLog("", player.charname, code + " đã nhận được nlsc, nlcc cấp 6 mỗi loại 2 viên, 1 áo thời trang có thời hạn 7 ngày", "gcode");
    }

    void dosetGiftCodeTopVanlangNgude(Char player, String code) {
        player.doAddSpecialItem(0, 3);

        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 2, true);
            player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 2, true);
        }

        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được nhận được 3 vé giờ vàng, nlsc, nlcc cấp 6 mỗi loại 2 viên", ""));
        Database.instance.saveOrtherLog("", player.charname, code + " nhận được 3 vé giờ vàng, nlsc, nlcc cấp 6 mỗi loại 2 viên", "gcode");
    }

    public static boolean isServerVangLang() {
        return TeamServer.server == 5 && !openLog;
    }



    public static String getNumberFromString(String info) {
        String result = "";

        for (int i = 0; i < info.length(); i++) {
            if (info.charAt(i) >= '0' && info.charAt(i) <= '9') {
                result = result + info.charAt(i);
            }
        }

        return result;
    }




    private void doOpenSnake(Char p) {
        for (byte i = 0; i < 5; i++) {
            p.doAddGemItem(InfoGifLucky.idMaterial[2][i]);
            p.doAddGemItem(InfoGifLucky.idMaterial[5][i]);
        }

        Database.instance.saveOrtherLog("", p.charname, "5 ng lieu 6 moi loai", "snake");
        p.sendMessage(MessageCreator.createCharGemItem(p));
        p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được 5 nguyên liệu sơ cấp cấp 6 và 5 nguyên liệu cao cấp cấp 6.", ""));
    }

    public boolean isNpcThanh(int id) {
        for (int i = 0; i < ID_NPC_THANH.length; i++) {
            if (ID_NPC_THANH[i] == id) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean checkFullMember(int country) {
        return false;
    }

    public boolean isStartLienDau() {
        return false;
    }

    public static synchronized void doReceiveLixi(Char player) {
        try {
            if (!checkCanReceiveLixi()) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể nhận lì xì."));
                return;
            }

            player.potions[159]++;
            MAX_LI_XI--;
            player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 1 bao lì xì."));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            Database.instance.updateInfoLixiEvent();
            Database.instance.saveOrtherLog("", player.charname, "nhan lixi, con lai " + MAX_LI_XI, "lixi");
        } catch (Exception var2) {
        }
    }

    public static synchronized boolean doTuoiNuocCayLixi() {
        if (!Char.is123Tet()) {
            return false;
        } else if (UtilKPAH.getHour() >= hourGiveLixi) {
            return false;
        } else {
            minuteGiveLixi -= 10;
            if (minuteGiveLixi < 0) {
                hourGiveLixi--;
                minuteGiveLixi = 50;
            }

            Database.instance.updateInfoLixiEvent();
            return true;
        }
    }

    public static synchronized boolean checkCanReceiveLixi() {
        return MAX_LI_XI > 0 && (UtilKPAH.getHour() == hourGiveLixi && UtilKPAH.getMinute() >= minuteGiveLixi || UtilKPAH.getHour() > hourGiveLixi);
    }

    /**
     * Mo menu con "Ho Tro" (npc -519). Truoc day nam o npc admin, nay chuyen
     * sang npc tong tieu dau. Npc -519 khong co case rieng trong RealController
     * nen roi vao default -> defHandle = true -> quay ve doMenu o duoi, chay tot.
     *
     * public vi RealController goi truc tiep: lenh -30 cua npc -50 duoc
     * RealController xu ly het, khong bao gio xuong doMenu.
     */
    public void showMenuHoTro(Char player) {
        String[] menuHoTro = new String[]{"Xóa Pet (500L)", "Xóa Thú Cưỡi (500L)"};
        player.sendMessage(MessageCreator.createMsgMenu(menuHoTro, -519, 0));
    }

    // ===== Moc nap tan thu (49k/99k/299k) - nhan qua o NPC Tong Quan (-10) =====
    static final int[] MOC_NAP_GIA = {49000, 99000, 299000};

    public void showMenuMocNap(Char player) {
        int[] status = Database.instance.getMocNapStatus(player.userID);
        String[] menuMoc = new String[3];
        for (int i = 1; i <= 3; i++) {
            String tinhTrang;
            if (status[i] == 2) {
                tinhTrang = "Đã nhận";
            } else if (status[i] == 1) {
                tinhTrang = "Nhận ngay";
            } else {
                tinhTrang = "Chưa đủ điều kiện";
            }
            menuMoc[i - 1] = "Mốc " + i + " (" + (MOC_NAP_GIA[i - 1] / 1000) + "k) - " + tinhTrang;
        }
        player.sendMessage(MessageCreator.createMsgMenu(menuMoc, -520, 0));
    }

    public void doChonMocNap(Char player, int idOptionMenu) {
        int moc = idOptionMenu + 1;
        if (moc < 1 || moc > 3) {
            return;
        }
        int[] status = Database.instance.getMocNapStatus(player.userID);
        if (status[moc] == 2) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận mốc nạp này rồi.", ""));
            return;
        }
        if (status[moc] != 1) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa nạp đủ mốc này hoặc giao dịch chưa được xác nhận.", ""));
            return;
        }
        if (moc == 3) {
            // Moc 3 duoc chon Phuong Hoang hoac Phuong Hoang Bang truoc khi trao qua.
            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Phượng Hoàng", "Phượng Hoàng Băng"}, -521, 0));
            return;
        }
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy, không nhận được quà.", ""));
            return;
        }
        if (!Database.instance.claimMocNap(player.userID, moc)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mốc nạp đã được nhận hoặc không hợp lệ.", ""));
            return;
        }
        MapQuaTang.grantMocNapReward(this, player, moc, -1);
    }

    public void doChonPhuongHoangMocNap(Char player, int idOptionMenu) {
        int[] status = Database.instance.getMocNapStatus(player.userID);
        if (status[3] != 1) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mốc nạp không hợp lệ hoặc đã được nhận.", ""));
            return;
        }
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy, không nhận được quà.", ""));
            return;
        }
        if (!Database.instance.claimMocNap(player.userID, 3)) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mốc nạp đã được nhận hoặc không hợp lệ.", ""));
            return;
        }
        MapQuaTang.grantMocNapReward(this, player, 3, idOptionMenu == 1 ? 1 : 0);
    }

    /**
     * Trao qua cho 1 moc nap. phoenixChoice chi dung khi moc==3: 0 = Phuong Hoang,
     * 1 = Phuong Hoang Bang, -1 = khong ap dung (moc 1/2).
     */

    // ===== NPC Doi Thuong (diem tich tu van tieu/cuop tieu/danh quai) =====
    // 31 vat pham co dinh, chia doi so trang tu dong (xem soMucMoiTrangDoiThuong).
    // Ten hien thi dung "diem" (khong con "sao vang").
    static final String[] TEN_DOI_THUONG = {
            "Phi phong Huyền Vũ ma pháp random kháng 7 ngày",
            "Phi phong Huyền Vũ vật lý random kháng 7 ngày",
            "Vũ khí thời trang random chỉ số 7 ngày",
            "Hoả thạch cấp 2 x20",
            "Nghê Thường 7 ngày",
            "Xích Cước 7 ngày",
            "Nguyệt Linh Trượng random chỉ số 7 ngày",
            // Canh: random CA he (Bang/Set/Doc/Lua/EXP/bao kich...) LAN chi so, tru
            // tier cao nhat (tier cao nhat chi danh cho quay so may man). Xem
            // Item.createOptionCanhNgauNhien.
            "Cánh ngẫu nhiên (hệ + chỉ số random) 7 ngày",
            "Mặt nạ Bí Ngô 7 ngày",
            "Mặt nạ Dạ Xoa 7 ngày",
            "Mặt nạ Thi Khiển 7 ngày",
            "Mặt nạ Ngạ Khuyển 7 ngày",
            // Choi: random CA he (Bang/Set/Doc/Lua) LAN chi so, tru tier cao nhat.
            // Xem Item.createAttChoiNgauNhien.
            "Chổi ngẫu nhiên (hệ + chỉ số random) 7 ngày",
            "Đầu Bí Ngô 7 ngày",
            "Nón Phù Thủy 7 ngày",
            // 5 skin linh thu, 7 ngay. Xem real.SkinLinhThu — skin chi doi hinh,
            // hoat anh va cach di chuyen cua con linh thu, khong cong them chi so nao,
            // nen dat gia theo do hiem chu khong theo suc manh.
            "Skin linh thú Phúc Thú 7 ngày",
            "Skin linh thú Gấu Trúc 7 ngày",
            "Skin linh thú Tiên Lộc 7 ngày",
            "Skin linh thú Xích Dương 7 ngày",
            "Skin linh thú Chiến Lang 7 ngày",
            // Ho ve lenh: random lv1-lv3 CUNG mot cap (5x/6x), 7 ngay. Da bo han muc 4x.
            "Hộ vệ lệnh 5x random lv1-3, 7 ngày",
            "Hộ vệ lệnh 6x random lv1-3, 7 ngày",
            // Tu hon dan la Potion (TYPE_TU_HON_DAN_C1=83, data_potion isTrade=0 - da luon
            // KHONG giao dich duoc, khoa la ban chat san co chu khong phai lua chon rieng),
            // dem qua player.potions[83] nen xep chong duoc (khac Item id 714 dung khi cho
            // thu an - tempateID rieng, KHONG stack, moi cai chiem 1 o hanh trang). Yeu cau
            // nguoi dung 17/09/2026: doi 1000 diem lay 100 tu hon dan khoa.
            "Tụ hồn đan khoá x100",
    };
    static final int[] GIA_DOI_THUONG = {
            500, 500, 1500, 800, 1200, 1500, 1500,
            1000,
            800, 800, 800, 800,
            500,
            2000, 2000,
            2000, 2000, 2000, 2000, 2000,
            800, 800,
            1000,
    };

    public void showMenuDiemDoiThuong(Char player) {
        if (player.diemDoiThuong <= 0 && !player.isAdmin) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Bạn không đủ điểm để đổi thưởng. Hãy vận tiêu, cướp tiêu hoặc đánh quái để tích điểm.", ""));
            return;
        }
        MapQuaTang.showTrangDoiThuong(this, player, 0);
    }

    /**
     * So muc moi trang. Menu doi thuong chi co DUNG HAI trang: dong dieu huong cuoi menu
     * lat qua lat lai giua trang 0 va trang 1 (doChonDoiThuong), va hai trang dung hai id
     * npc ao khac nhau (-91 / -92) nen khong them trang thu ba duoc neu khong them id.
     *
     * Vi vay chia doi danh sach thay vi co dinh 14: co dinh 14 thi danh sach dai hon 28
     * muc se co nhung muc KHONG BAO GIO hien ra, va hong am tham — nguoi them phan thuong
     * moi khong he biet. Chia doi thi bao nhieu muc cung hien du.
     */


    public void doChonDoiThuong(Char player, int trang, int idOptionMenu) {
        final int moiTrang = MapQuaTang.soMucMoiTrangDoiThuong(this);
        int tuVitri = trang * moiTrang;
        int viTriCuoiTrang = Math.min(tuVitri + moiTrang, TEN_DOI_THUONG.length) - tuVitri;
        if (idOptionMenu == viTriCuoiTrang) {
            // dong dieu huong trang: trang 0 -> "Trang sau" mo trang 1, trang 1 -> "Trang truoc" mo trang 0
            MapQuaTang.showTrangDoiThuong(this, player, trang == 0 ? 1 : 0);
            return;
        }
        int idx = tuVitri + idOptionMenu;
        if (idx < 0 || idx >= TEN_DOI_THUONG.length) {
            return;
        }
        if (!player.isAdmin && player.diemDoiThuong < GIA_DOI_THUONG[idx]) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ điểm để đổi vật phẩm này.", ""));
            return;
        }
        if (!MapQuaTang.traoVatPhamDoiThuong(this, player, idx)) {
            return; // ham tra ve false da tu gui thong bao ly do (het cho hanh trang...)
        }
        if (!player.isAdmin) {
            player.diemDoiThuong -= GIA_DOI_THUONG[idx];
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đổi thưởng thành công: " + TEN_DOI_THUONG[idx] + ". Còn lại " + player.diemDoiThuong + " điểm.", ""));
        Database.instance.saveOrtherLog("", player.charname, "Doi thuong: " + TEN_DOI_THUONG[idx], "doithuong");
    }


    // Than method (4.601 dong) da chuyen sang real/MapDoMenu.java.
    // Chu ky va tinh protected GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi
    // Map.doMenu, va lop con van override duoc nhu cu.
    protected void doMenu(Char player, Message message) {
        MapDoMenu.doMenu(this, player, message);
    }

    public static synchronized void doBuyItemBlackFriday(Char player) {
    }

    void doChamNuoc(Char player, int idNpc, int idMenu, int idOptionMenu) {
        Calendar cl = Calendar.getInstance();
        int ihour = cl.get(11);
        int iMinute = cl.get(12);
        if (Char.getDayOpen(0L).equals("2020-01-17")) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chương trình nấu bánh sẽ bắt đầu từ ngày 18/1", ""));
        } else {
            if (idNpc == -65) {
                if (idOptionMenu == 0) {
                    if (ihour == Cooking.timeNauBanh) {
                        if (player.getxu() < 500000L) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                            return;
                        }

                        if (Char.nauBanhChung.ChamNuoc()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Châm nước xong", ""));
                            player.subXu(1000000L, true, "map 45");
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Database.instance.saveOrtherLog("", player.charname, "cham nc banh chung", "chamnc");
                        } else {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể châm nước vào thời gian này", ""));
                        }
                    } else {
                        if (ihour == 17 && iMinute >= 30) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể góp bánh trong thời gian này", ""));
                            return;
                        }

                        if (player.potions[148] < 10 || player.potions[122] < 10 || player.potions[149] < 10 || player.potions[150] < 10) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngyên liệu", ""));
                            return;
                        }

                        if (Char.nauBanhChung.doGopNguyenLieu(player, 1)) {
                            player.potions[148] = player.potions[148] - 10;
                            player.potions[122] = player.potions[122] - 10;
                            player.potions[149] = player.potions[149] - 10;
                            player.potions[150] = player.potions[150] - 10;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            Database.instance.saveOrtherLog("", player.charname, "gop ngl banh chung", "banhchung");
                            Database.instance.saveCooking(Char.nauBanhChung, 1);
                        } else {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa đăng ký nấu bánh", ""));
                        }
                    }
                }
            } else if (idNpc == -66 && idOptionMenu == 0) {
                if (ihour == Cooking.timeNauBanh) {
                    if (player.getxu() < 500000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    if (Char.nauBanhTet.ChamNuoc()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Châm nước xong", ""));
                        player.subXu(1000000L, true, "map 47");
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Database.instance.saveOrtherLog("", player.charname, "cham nc banh tet", "chamnc");
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể châm nước vào thời gian này", ""));
                    }
                } else {
                    if (ihour == 17 && iMinute >= 30) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể góp bánh trong thời gian này", ""));
                        return;
                    }

                    if (player.potions[150] < 10 || player.potions[148] < 10 || player.potions[122] < 10 || player.potions[149] < 10) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngyên liệu", ""));
                        return;
                    }

                    if (Char.nauBanhTet.doGopNguyenLieu(player, 1)) {
                        player.potions[150] = player.potions[150] - 10;
                        player.potions[148] = player.potions[148] - 10;
                        player.potions[149] = player.potions[149] - 10;
                        player.potions[122] = player.potions[122] - 10;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        Database.instance.saveOrtherLog("", player.charname, "gop ngl banh tet", "banhtet");
                        Database.instance.saveCooking(Char.nauBanhTet, 2);
                    } else {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa đăng ký nấu bánh", ""));
                    }
                }
            }
        }
    }






    public void doDoi7VienNgocRong(Char p) {
    }

    public void doReceiveGiftTopTet2021(Char p, int idNpc, int idMenu, int idOptionMenu) {
        if (p.canReceiveGiftOffline >= 0) {
            if (p.isFullInventory()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
            } else if (Char.getDayTime(0L).compareTo("2020-10-22 10:00:00") < 0) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể nhận quà top sau 10h ngày 22-10-2020", ""));
            } else {
                int[] khang = new int[]{62, 61, 60, 59, 63, 62, 61, 60, 59, 63};
                int indexTop = p.canReceiveGiftOffline;
                String info = "";
                if (idMenu == 0) {
                    String[] menu = new String[]{
                        "Phi phong Huyền vũ ma(kim)",
                        "Phi phong Huyền vũ ma(mộc)",
                        "Phi phong Huyền vũ ma(thuỷ)",
                        "Phi phong Huyền vũ ma(hoả)",
                        "Phi phong Huyền vũ ma(thổ)",
                        "Phi phong Huyền vũ vật(kim)",
                        "Phi phong Huyền vũ vật(mộc)",
                        "Phi phong Huyền vũ vật(thuỷ)",
                        "Phi phong Huyền vũ vật(hoả)",
                        "Phi phong Huyền vũ vật(thổ)"
                    };
                    p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
                } else if (idMenu == 20) {
                    if (indexTop < 3) {
                        String[] menu = new String[]{"Trứng Cú", "Trứng Rồng", "Trứng Yêu Tinh", "Trứng Dơi", "Trứng Đại bàng"};
                        p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 50 + idOptionMenu));
                    } else {
                        p.canReceiveGiftOffline = -1;
                        Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                        Item newItem = ItemLuckyDraw.createtemCoatHaveKhang(750, p, idOptionMenu / 5, false, indexTop == 3 ? 0 : 4320, khang[idOptionMenu]);
                        newItem.lock = 1;
                        info = "nhan qua top : " + newItem.getName() + " " + newItem.getInfoSave();
                        Item it = doCreateVuKhiThoiTrang(p, 43200, ID_VU_KHI_THOI_TRANG[p.charClass], 1);
                        info = info + " | " + it.getName() + " " + it.getInfoSave();
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        Database.instance.saveOrtherLog("", p.charname, info, "quatop");
                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà đua top sự kiện tết nguyên đán 2021", ""));
                    }
                } else if (idMenu >= 50 && idMenu < 60) {
                    p.idPetTang = (short) idMenu;
                    if (indexTop < 2) {
                        String[] menu = new String[]{"Chổi băng", "Chổi độc", "Chổi điện", "Chổi lửa"};
                        p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 60 + idOptionMenu));
                    } else if (indexTop == 2) {
                        p.canReceiveGiftOffline = -1;
                        Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                        int[] idTrung = new int[]{684, 685, 686, 687, 688};
                        System.out.println("NHAN TRUNG " + idMenu + " >> " + idTrung[idOptionMenu]);
                        Item ittrung = createtrungPet(idTrung[idOptionMenu]);
                        info = "nhan qua top: " + ittrung.getName();
                        p.iItems.add(ittrung);
                        Item newItem = ItemLuckyDraw.createtemCoatHaveKhang(750, p, (idMenu - 50) / 5, true, 0, khang[idMenu - 50]);
                        newItem.lock = 1;
                        info = info + ", " + newItem.getName() + " " + newItem.getInfoSave();
                        Item it = doCreateVuKhiThoiTrang(p, 43200, ID_VU_KHI_THOI_TRANG[p.charClass], 1);
                        info = info + " | " + it.getName() + " " + it.getInfoSave();
                        Database.instance.saveOrtherLog("", p.charname, info, "quatop");
                        p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà đua top sự kiện tết nguyên đán 2021", ""));
                    }
                } else if (idMenu >= 60 && idMenu < 65) {
                    if (indexTop > 1) {
                        return;
                    }

                    p.canReceiveGiftOffline = -1;
                    Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                    int[] idTrung = new int[]{684, 685, 686, 687, 688};
                    Item ittrung = createtrungPet(idTrung[idMenu - 60]);
                    info = "nhan qua top: " + ittrung.getName();
                    p.iItems.add(ittrung);
                    int idpp = p.idPetTang - 50;
                    Item newItem = ItemLuckyDraw.createtemCoatHaveKhang(750, p, idpp / 5, true, 0, khang[idpp]);
                    if (indexTop != 0) {
                        newItem.lock = 1;
                    }

                    info = info + ", " + newItem.getName() + " " + newItem.getInfoSave();
                    if (indexTop < 2) {
                        if (idOptionMenu == 0) {
                            Char.createChoi(617, p, 43200, 1, true);
                            info = info + " nhan choi bang";
                        } else if (idOptionMenu == 1) {
                            Char.createChoi(619, p, 43200, 1, true);
                            info = info + " nhan choi doc";
                        } else if (idOptionMenu == 2) {
                            Char.createChoi(618, p, 43200, 1, true);
                            info = info + " nhan choi bang";
                        } else if (idOptionMenu == 3) {
                            Char.createChoi(729, p, 43200, 1, true);
                            info = info + " nhan choi lua";
                        }
                    }

                    if (indexTop == 0) {
                        Item it = doCreateVuKhiThoiTrang(p, 43200, ID_VU_KHI_THOI_TRANG[p.charClass], 1);
                        info = info + " | " + it.getName() + " " + it.getInfoSave();
                    }

                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    Database.instance.saveOrtherLog("", p.charname, info, "quatop");
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận quà đua top sự kiện tết nguyên đán 2021", ""));
                }
            }
        }
    }

    public void doReceiveGiftTopTrungThu2020(Char p, int idNpc, int idMenu, int idOptionMenu) {
        if (p.canReceiveGiftOffline >= 0) {
            if (Char.getDayTime(0L).compareTo("2020-10-22 10:00:00") < 0) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể nhận quà top sau 10h ngày 22-10-2020", ""));
            } else {
                int indexTop = p.canReceiveGiftOffline;
                String info = "";
                if (idMenu == 0) {
                    String[] menu = new String[]{"Phi phong Khổng tước ma", "Phi phong Khổng tước vật"};
                    p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
                } else if (idMenu == 20) {
                    if (indexTop < 3) {
                        String[] menu = new String[]{"Lân sư tử", "Đương khang", "Bạch cốt mã"};
                        p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 30 + idOptionMenu));
                    } else if (indexTop == 3) {
                        String[] menu = new String[]{"Lồng đèn Cá chép", "Lồng đèn Ngôi sao", "Lồng đèn Trăng khuyết"};
                        p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 40 + idOptionMenu));
                    }
                } else if (idMenu >= 30 && idMenu < 33) {
                    p.canReceiveGiftOffline = -1;
                    Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                    int[][] iditem = new int[][]{{744}, {743}};
                    int[] idpt = new int[]{115, 72, 74};
                    p.potions[idpt[idOptionMenu]]++;
                    p.doCreatePetBachThu((indexTop < 2 ? 60 : 30) * 24 * 60, false);
                    Item it = doBuyModelClothe(p, p.isCharNam() ? iditem[0][0] : iditem[1][0], 30, 1);
                    int[] minute = new int[]{0, 86400, 43200};
                    Item newItem = ItemLuckyDraw.createtemCoat(742, p, idMenu - 30, true, minute[indexTop]);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    info = info + "Nhan qua top " + indexTop + ": pp " + (idMenu - 30) + " thu cuoi " + idpt[idOptionMenu];
                    p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà top " + indexTop + "_" + newItem.getName() + " " + it.getName(), ""));
                    Database.instance.saveOrtherLog("", p.charname, info, "quatop");
                } else if (idMenu >= 40 && idMenu < 43) {
                    p.canReceiveGiftOffline = -1;
                    Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                    int[][] iditem = new int[][]{{744}, {743}};
                    int[] minute = new int[]{0, 86400, 43200, 43200};
                    Item newItem = ItemLuckyDraw.createtemCoat(742, p, idMenu - 40, true, minute[indexTop]);
                    Item it = doBuyModelClothe(p, p.isCharNam() ? iditem[0][0] : iditem[1][0], 30, 1);
                    int[] id = new int[]{12, 19, 20};
                    info = info + "Nhan qua top " + indexTop + ": pp " + (idMenu - 40) + " pet " + id[idOptionMenu] + " " + it.getName() + "_" + it.getInfoSave();
                    p.createPet(id[idOptionMenu], 0);
                    p.receiveGiftEvent = 1;
                    Database.instance.saveOrtherLog("", p.charname, info, "quatop");
                }
            }
        }
    }

    public void doReceiveGiftTopTrungThu(Char p, int idNpc, int idMenu, int idOptionMenu) {
        if (p.canReceiveGiftOffline >= 0) {
            int indexTop = p.canReceiveGiftOffline;
            if (idMenu == 0) {
                String[] menu = new String[]{"Phi phong ma theo lv", "Phi phong vật theo lv", "Phi phong ma lv 80", "Phi phong vật lv 80"};
                p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
            } else if (idMenu == 20) {
                if (indexTop == 0) {
                    String[] menu = new String[]{"Chổi băng", "Chổi độc", "Chổi điện"};
                    p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 30 + idOptionMenu));
                } else if (indexTop == 1) {
                    p.canReceiveGiftOffline = -1;
                    Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                    p.potions[71]++;

                    for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                        p.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 10, true);
                        p.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 10, true);
                    }

                    Item it = Char.createPhiPhongMaxBaoKich(p, idOptionMenu % 2, 0, null, true, idOptionMenu > 1, 0, false);
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(
                            MessageCreator.createServerAlertMessage("Nhận được " + it.getName() + " và 1 trứng Hoả Kỳ Lân và nguyên liệu khoá mỗi loại 10 viên", "")
                    );
                    Database.instance
                            .saveOrtherLog(
                                    "", p.charname, "nhan qua top 2: " + it.getName() + "_" + it.getInfoSave() + " và 1 trung hkl và ngl c6 khoa moi loai 10 vien", "quatop"
                            );
                } else if (indexTop == 2) {
                    p.canReceiveGiftOffline = -1;
                    Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                    p.potions[71]++;
                    Item it = Char.createPhiPhongMaxBaoKich(p, idOptionMenu % 2, 0, null, true, idOptionMenu > 1, 0, false);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + it.getName() + " và 1 trứng Hoả Kỳ Lân", ""));
                    Database.instance.saveOrtherLog("", p.charname, "nhan qua top 3: " + it.getName() + "_" + it.getInfoSave() + " và 1 trung hkl", "quatop");
                } else if (indexTop > 2) {
                    String[] menu = new String[]{"Trứng Cú", "Trứng Rồng", "Trứng Yêu Tinh", "Trứng Dơi", "Trứng Đại bàng"};
                    p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 40 + idOptionMenu));
                }
            } else if (idMenu >= 30 && idMenu < 34) {
                p.canReceiveGiftOffline = -1;
                Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                int ma_vat = idMenu - 30;
                p.potions[71]++;
                Item newItem = Char.createPhiPhongLongThuMax(p, ma_vat % 2, 616, ma_vat > 1, false, 0);
                int[] idchoi = new int[]{617, 619, 618};
                Item choi = Char.createChoi(idchoi[idOptionMenu], p, 129600, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newItem.getName() + ", " + choi.getName() + " và 1 trứng Hoả Kỳ Lân", ""));
                Database.instance
                        .saveOrtherLog(
                                "",
                                p.charname,
                                "nhan qua top 1: " + newItem.getName() + "_" + newItem.getInfoSave() + " va " + choi.getName() + "_" + choi.getInfoSave() + " và 1 trung hkl",
                                "quatop"
                        );
            } else if (idMenu >= 40 && idMenu < 44) {
                p.canReceiveGiftOffline = -1;
                Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                int ma_vat = idMenu - 40;
                int[] idTrung = new int[]{684, 685, 686, 687, 688};
                Item ittrung = createtrungPet(idTrung[idOptionMenu]);
                p.iItems.add(ittrung);
                Item it = Char.createPhiPhongMaxBaoKich(p, ma_vat % 2, 0, null, true, ma_vat > 1, 0, true);
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + it.getName() + ", " + ittrung.getName(), ""));
                Database.instance
                        .saveOrtherLog(
                                "",
                                p.charname,
                                "nhan qua top 4-10: " + it.getName() + "_" + it.getInfoSave() + " va " + ittrung.getName() + "_" + ittrung.getInfoSave(),
                                "quatop"
                        );
            }
        }
    }



    private void doReceiveTopHoaLu2020(Char player, int idNpc, int idMenu, int idOptionMenu) {
        try {
            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            String[] menu = new String[]{"Bạch mi", "Thuý vũ hồng sam", "Hắc lang", "Thần nữ"};
            String[] menu2 = new String[]{"Phi phong ma", "Phi phong vật"};
            String info = "Nhận quà top " + player.canReceiveGiftOffline;
            if (idMenu == 0) {
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
            } else if (idMenu == 20) {
                player.sendMessage(MessageCreator.createMsgMenu(menu2, idNpc, 22 + idOptionMenu));
            } else {
                int day = 43200;
                int daytt = 30;
                int[] luong = new int[]{5000, 2000, 1000, 0};
                int[] luongk = new int[]{0, 1000, 1000, 0};
                if (player.canReceiveGiftOffline == 0) {
                    day = 0;
                    daytt = 0;
                }

                if (player.canReceiveGiftOffline == 1 || player.canReceiveGiftOffline == 2) {
                    daytt = 0;
                }

                Item pp = Char.createPhiPhongLongThuMax(player, idOptionMenu, 616, false, false, day);
                if (idMenu == 22) {
                    doBuyModelClothe(player, 725, daytt, 1);
                    info = info
                            + ". Nhận thời trang bạch mi, phi phong "
                            + idOptionMenu
                            + " luong: "
                            + luong[player.canReceiveGiftOffline]
                            + " ,lk: "
                            + luongk[player.canReceiveGiftOffline];
                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được thời trang Bạch Mi", ""));
                } else if (idMenu == 23) {
                    doBuyModelClothe(player, 726, daytt, 1);
                    info = info
                            + ". Nhận thời trang thuý vũ hồng sam, phi phong "
                            + idOptionMenu
                            + " luong: "
                            + luong[player.canReceiveGiftOffline]
                            + " ,lk: "
                            + luongk[player.canReceiveGiftOffline];
                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được thời trang Thuý Vũ Hồng Sam", ""));
                } else if (idMenu == 24) {
                    doBuyModelClothe(player, 723, daytt, 1);
                    info = info
                            + ". Nhận thời trang hắc lang, phi phong "
                            + idOptionMenu
                            + " luong: "
                            + luong[player.canReceiveGiftOffline]
                            + " ,lk: "
                            + luongk[player.canReceiveGiftOffline];
                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được thời trang Hắc Lang", ""));
                } else if (idMenu == 25) {
                    doBuyModelClothe(player, 724, daytt, 1);
                    info = info
                            + ". Nhận thời trang thần nữ, phi phong "
                            + idOptionMenu
                            + " luong: "
                            + luong[player.canReceiveGiftOffline]
                            + " ,lk: "
                            + luongk[player.canReceiveGiftOffline];
                    player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được thời trang Thần Nữ", ""));
                }

                player.addLuong(luong[player.canReceiveGiftOffline]);
                player.addLuongLock(luongk[player.canReceiveGiftOffline]);
                player.canReceiveGiftOffline = -1;
                Database.instance.updateStateReceiveGiftEventOutGame(player.charname);
                Database.instance.saveOrtherLog("", player.charname, info, "tophl");
            }
        } catch (Exception var13) {
        }
    }











    public void doUseItemLinhDanhThue(Char player) {
        try {
            if (player.itemLinhDanhThue == null) {
                return;
            }

            String[] menu = new String[]{
                "Hộ vệ nam hệ kim",
                "Hộ vệ nữ hệ kim",
                "Hộ vệ nam hệ hoả",
                "Hộ vệ nữ hệ hoả",
                "Hộ vệ nam hệ thuỷ",
                "Hộ vệ nữ hệ thuỷ",
                "Hộ vệ nam hệ thổ",
                "Hộ vệ nữ hệ thổ",
                "Hộ vệ nam hệ mộc",
                "Hộ vệ nữ hệ mộc"
            };
            player.sendMessage(MessageCreator.createMsgMenu(menu, -512, 0));
        } catch (Exception var3) {
        }
    }

    // ==================== AP TRUNG RONG BANG ====================
    // Trung rong bang (item 887) no ra linh thu Rong Bang. CHI mot duong vao: NPC tao thu
    // (menu -6, option "Rong bang") - giong moi loai trung linh thu khac. Bam Dung trong
    // hanh trang chi nhac di gap NPC, xem doUseItem.
    //
    // No ra thu LEVEL 1 voi thuoc tinh random - giong het 10 trung con lai o NPC tao thu.
    // Co y khong dung createRongLon (level 4 + createAttMax) nhu giftcode RONGBANG:<ngay>:
    // trung la de nuoi. Luyen o NPC len level 4 thi tran con CAO HON ban phat san -
    // pcBasic level 4 + addMoreSP[RONG_LON]=3 cho att[0..2] toi 13 (ban phat: 10) va
    // hp/mp toi 8000 (ban phat: 5000). Xem Animal.upAttribute + pcBasic/pcTN/pcSP.
    public boolean doApTrungRongBang(Char player, Item trung) {
        try {
            if (player == null || trung == null) {
                return false;
            }

            Animal anima = new Animal();
            anima.name = "Rồng Băng";
            anima.idImg = Animal.RONG_LON;
            anima.createAtt();
            // Mo o dong dac biet thu hai cho bang ban phat san bang giftcode RONGBANG:<ngay>
            // (createBasePointMax bat 2 o trong att[9..13], createBasePoint chi bat 1).
            anima.createOneMoreSpecialPoint();
            anima.level = 1;
            anima.place = 0;
            anima.ownerId = player.charDBID;
            anima.id = player.getIDItem();
            player.animal.add(anima);
            player.iItems.remove(trung);
            player.removeIDItem(trung.id);
            Database.instance.saveAnimal(anima);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
            Database.instance
                    .saveOrtherLog(
                            "tob_log_other_animal", player.charname, "ap trung rong bang | " + anima.getAttribute(), "crt"
                    );
            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng. Bạn đã nở được linh thú " + anima.name, ""));
            return true;
        } catch (Exception var4) {
            var4.printStackTrace();
            return false;
        }
    }

    // Tim trung rong bang dau tien trong hanh trang, null neu khong co.
    public static Item getTrungRongBang(Char player) {
        for (int i = 0; i < player.iItems.size(); i++) {
            Item it = (Item) player.iItems.elementAt(i);
            if (it.isTrungRongBang()) {
                return it;
            }
        }

        return null;
    }



    // Sach ky nang pet la POTION 111, xep chong trong tui binh nen khong bao gio
    // day tui va khong con phu thuoc so o hanh trang.
    // Tham so "lock" giu lai cho 10 cho goi san; potion khong co khai niem khoa.
    public static boolean doCreateBookSkillPet(Char player, int lock) {
        player.potions[Potion.TYPE_SACH_SKILL_PET]++;
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        return true;
    }

    public static boolean doCreateHuyetBoDe(Char player, int lock) {
        if (!player.isFullInventory()) {
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(721);
            Item newItem = null;
            newItem = new Item(template, false, 0, 0, template.id);
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            player.iItems.add(newItem);
            newItem.identify = player.charDBID;
            newItem.lock = (byte) lock;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            return true;
        } else {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
            return false;
        }
    }

    public static Item doCreateHuyetLinhThao(Char player, int lock) {
        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(720);
        Item newItem = null;
        newItem = new Item(template, false, 0, 0, template.id);
        newItem.id = player.getIDItem();
        newItem.owner = player.charDBID;
        newItem.level = newItem.getTemplate().level;
        player.iItems.add(newItem);
        newItem.identify = player.charDBID;
        newItem.lock = (byte) lock;
        newItem.dateCreate = Char.getDayTime(0L);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        return newItem;
    }



    public static String getInfoSavetrungpet() {
        return Char.total_open + "|" + Char.numberLucky + "|" + Char.numberRandom;
    }

    public static String getInfoSavePhuongHoang() {
        return minOpenHopqua + "," + totalPhuongHoang + "," + winPhuongHoang + "," + countOpenHopqua;
    }

    public static synchronized void doHopQuaHaloween2016(Char player) {
        countOpenHopqua++;
        long exp = 300000L;
        addXpCharEvent(player, exp, false, "doHopQuaHaloween2016");
        player.idNglieuDoi = -1;
        int[] per = new int[]{250, 250, 70, 500, 500, 100, 100, 100, 400, 70, 70, 70, 70, 70, 70, 70, 70, 70, 100, 100, 50, 50, 250, 250, 250};
        int[] idgift = new int[]{118, 118, 35, 93, 95, 59, 51, 55, 144, 66, 676, 677, -5, -5, -5, -5, -5, -5, -1, -2, -3, -4, 114, 112, 113};
        int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, -5, -5, -5, -5, -5, -5, -1, -2, -3, -4, 4, 4, 4};
        int[] soluongmax = new int[]{1, 1, 1, 100, 100, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5};
        int[] soluongmin = new int[]{1, 1, 1, 100, 100, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3};
        String info = "Chúc mừng bạn nhận được ";
        boolean lock = r.nextInt(2) == 1;
        int index = Char.getIndexRandom(per);
        if (winPhuongHoang < totalPhuongHoang && countOpenHopqua >= (winPhuongHoang + 1) * minOpenHopqua) {
            winPhuongHoang++;
            countOpenHopqua = 0;
            index = r.nextInt(2);
        }

        if (typegift[index] == -1) {
            short[] idGem = GemTemplate.ID_MATERIAL_LOW[r.nextInt(3) + 1];
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            int idmt = idGem[r.nextInt(idGem.length)];
            player.doAddGemItem(idmt, soluong, lock);
            info = info + soluong + " " + gemTemplate[idmt].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == -2) {
            short[] idGem = GemTemplate.ID_DA_NGU_HOP[2];
            lock = false;
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            int idmt = idGem[r.nextInt(2) + 3];
            player.doAddGemItem(idmt, soluong, lock);
            info = info + soluong + " " + gemTemplate[idmt].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == -4) {
            if (!player.isFullInventory()) {
                int[] minute = new int[]{1440, 4320, 7200, 10080};
                Item newItem = ItemLuckyDraw.createItemCoat(player, r.nextInt(2), false, minute[r.nextInt(minute.length)]);
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            } else {
                player.addXu((long) (100 + r.nextInt(100)), "map 55");
                info = info + soluongmax[index] + " xu";
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            }
        } else if (typegift[index] == -3) {
            if (!player.isFullInventory()) {
                int[] minute = new int[]{1440, 4320, 7200, 10080};
                Item newItem = ItemLuckyDraw.createtemCoat(742, player, r.nextInt(2), true, minute[r.nextInt(minute.length)]);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            } else {
                player.addXu((long) (100 + r.nextInt(100)), "map 55");
                info = info + soluongmax[index] + " xu";
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            }
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));

            try {
                if (idgift[index] == 70) {
                    sendAllCharServer(
                            -1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được Phượng hoàng băng khi mở hộp quà Halloween")
                    );
                } else if (idgift[index] == 86) {
                    sendAllCharServer(
                            -1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được Phượng hoàng lửa khi mở hộp quà Halloween")
                    );
                }
            } catch (IOException var14) {
            }
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == -5) {
            if (player.isFullInventory()) {
                int xu = 5000 + r.nextInt(100000);
                player.addXu((long) xu, "map 51");
                info = info + xu + " xu";
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else {
                Item newItem = doCreateHoVeLenhByLevel(player, 10080);
                info = info + newItem.getName();
            }

            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == 3) {
            if (player.isFullInventory()) {
                int xu = 5000 + r.nextInt(100000);
                player.addXu((long) xu, "map 51");
                info = info + xu + " xu";
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                newItem.lock = (byte) (lock ? 1 : 0);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                info = info + newItem.getName();
            }

            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        }

        Database.instance.saveOrtherLog("", player.charname, info, "ohopqua");
        Database.instance.saveEvent(event.getInfo());
    }

    public static void doHopQuaBiMatHaloween2016(Char player) {
        player.sendMessage(
                MessageCreator.createMsgMenu(new String[]{"Mặt nạ bí ngô", "Mặt nạ ngạ quỷ", "Mặt nạ ngạ khuyển", "Mặt nạ thi khiển", "Mặt nạ dạ xoa"}, -515, 0)
        );
    }

    public static Item doCreateVuKhiThoiTrang(Char p, int minute, int idtemplate) {
        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idtemplate);
        Item newItem = null;
        newItem = new Item(template, false, template.clazz, template.clazz, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        if (minute > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
        }

        newItem.createAttributeVuKhiThoiTrang();
        newItem.dateCreate = Char.getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        return newItem;
    }

    public static Item doCreateVuKhiThoiTrang(Char p, int minute, int idtemplate, int isLock) {
        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idtemplate);
        Item newItem = null;
        newItem = new Item(template, false, template.clazz, template.clazz, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        newItem.identify = p.charDBID;
        if (minute > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
        }

        newItem.lock = (byte) isLock;
        newItem.createAttributeVuKhiThoiTrang();
        newItem.dateCreate = Char.getDayTime(0L);
        p.iItems.add(newItem);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        return newItem;
    }

    public Item doCreateHoVeLenh(Char player, int minute, int idtemplate) {
        try {
            if (!player.isFullInventory()) {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idtemplate);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                }

                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception var6) {
        }

        return null;
    }

    public static Item doCreateMatNa(Char player, int minute, int idtemplate, int option, int valueOption) {
        try {
            if (!player.isFullInventory()) {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idtemplate);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                } else {
                    newItem.lock = 1;
                }

                newItem.createOptionMatNa(option, valueOption);
                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception var7) {
        }

        return null;
    }

    public static Item doCreateMatNa3(Char player, int minute, int idtemplate, int option, int valueOption) {
        try {
            if (!player.isFullInventory()) {
                ItemTemplates template = Map.itemTemplates.get(5).get(idtemplate);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                }
                newItem.lock = 1;
                newItem.createOptionMatNa(option, valueOption);
                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception ex) {
        }
        return null;
    }

     public static Item doCreateMatNa2(Char player, int minute, int idtemplate, int option, int valueOption) {
        return doCreateMatNa2(player, minute, idtemplate, option, valueOption, true);
    }

     /** Nhu doCreateMatNa2() nhung cho chon KHOA hay khong (them 19/09/2026 cho MATNAID -
      * ban cu (MATNA/MATNA2) luon khoa cung, giu nguyen qua overload phia tren). */
     public static Item doCreateMatNa2(Char player, int minute, int idtemplate, int option, int valueOption, boolean khoa) {
        try {
            if (!player.isFullInventory()) {
                // Lấy template trực tiếp từ ID mong muốn
                ItemTemplates template = (ItemTemplates) ((Hashtable) Map.itemTemplates.get(5)).get(idtemplate);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                }
                newItem.lock = (byte) (khoa ? 1 : 0);
                newItem.createOptionMatNa(option, valueOption);
                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public static Item doCreateHoVeLenhByLevel(Char player, int minute) {
        int[][] idgift = new int[][]{{631, 632}, {634, 635}, {637, 638}};

        try {
            if (!player.isFullInventory()) {
                int index = 0;
                if (player.lvDetail.lv >= 50 && player.lvDetail.lv < 60) {
                    index = 1;
                } else if (player.lvDetail.lv >= 60) {
                    index = 2;
                }

                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index][r.nextInt(idgift[index].length)]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                }

                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public static Item doCreateHoVeLenh(Char player, int minute) {
        int[] idgift = new int[]{631, 632, 634, 635, 637, 638};

        try {
            if (!player.isFullInventory()) {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[r.nextInt(idgift.length)]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                if (minute > 0) {
                    newItem.minuteExist = minute;
                    newItem.timeLoan = System.currentTimeMillis();
                }

                newItem.dateCreate = Char.getDayTime(0L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                return newItem;
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return null;
    }

    public void doHopQuaNoelCayThong(Char player) {
        addXpCharEvent(player, 500000L, false, "doTuiQuaNoel");
        player.idNglieuDoi = -1;
        int[] per = new int[]{200, 200, 200, 100, 100, 100, 30, 30, 200, 100, 50, 20};
        int[] idgift = new int[]{114, 113, 112, 82, 93, 95, 35, 100, GemTemplate.BOT_TRANG, GemTemplate.BOT_XANH, GemTemplate.BOT_DO, GemTemplate.BOT_XANH_LA};
        int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6};
        int[] soluongmax = new int[]{1, 1, 1, 1, 500, 500, 1, 3, 50, 25, 15, 10};
        int[] soluongmin = new int[]{1, 1, 1, 1, 50, 50, 1, 1, 50, 25, 15, 10};
        String info = "Chúc mừng bạn nhận được ";
        int index = Char.getIndexRandom(per);
        if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        }

        Database.instance.saveOrtherLog("", player.charname, info, "otuiqua");
    }

    public void doOpenRuongDracula(Char player) {
        player.idNglieuDoi = -1;
        int[] per = new int[]{10, 10, 1000, 200, 200, 200, 100, 100, 100};
        int[] idgift = new int[]{
            9,
            GemTemplate.LKD_40,
            GemTemplate.DA_TACH_NL_DAC_BIET,
            GemTemplate.NGOC_HUYEN_MINH_4,
            GemTemplate.NGOC_HUYEN_MINH_5,
            GemTemplate.NGOC_HUYEN_MINH_6,
            -1,
            -1,
            -1
        };
        int[] typegift = new int[]{4, 6, 6, 6, 6, 6, -1, -1, -1};
        int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 5, 5, 5};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 3, 3, 3};
        String info = "Chúc mừng bạn nhận được ";
        int index = Char.getIndexRandom(per);
        if (typegift[index] == -1) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.idNglieuDoi = (short) soluong;
            String[] menu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < menu.length; i++) {
                menu[i] = soluong + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
            }

            player.sendMessage(MessageCreator.createMsgMenu(menu, -511, 0));
        } else {
            if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));

                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được nhân sâm khi mở rương dracula"));
                } catch (IOException var13) {
                }
            } else if (typegift[index] == 6) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                boolean lock = r.nextInt(2) == 1;
                player.doAddGemItem(idgift[index], soluong, lock);
                info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createMsgChat(player.id, info));
                if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                    try {
                        sendAllCharServer(
                                -1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " đốt đèn")
                        );
                    } catch (IOException var12) {
                    }
                }
            }

            Database.instance.saveOrtherLog("", player.charname, info, "ruongdracula");
        }
    }

    public void doOpenRuongMa(Char player) {
        int[] per = new int[]{200, 50, 100, 100, 50, 100, 150, 100};
        int[] idgift = new int[]{
            35,
            GemTemplate.DA_TIEN_GIAI,
            GemTemplate.DUC_TUONG_KHAM_5,
            GemTemplate.DUC_TUONG_KHAM_6,
            GemTemplate.XUONG_C6,
            GemTemplate.XUONG_C5,
            GemTemplate.XUONG_C4,
            -1
        };
        int[] typegift = new int[]{4, 6, 6, 6, 6, 6, 6, -1};
        int[] soluongmax = new int[]{1, 1, 3, 3, 1, 1, 1, 1000000, 1};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1000000, 1};
        String info = "Chúc mừng bạn nhận được ";
        int index = Char.getIndexRandom(per);
        int var10000 = typegift[index];
        if (typegift[index] == -1) {
            player.addXu((long) soluongmax[index], "map 53");
            info = info + soluongmax[index] + " xu";
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                try {
                    sendAllCharServer(
                            -1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " đốt đèn")
                    );
                } catch (IOException var12) {
                }
            }
        }

        Database.instance.saveOrtherLog("", player.charname, info, "ruongma");
    }

    public void doTrangTriCayThong(Char player) {
        if (!Char.isFinishAllSuKienNoel()) {
            if (player.potions[120] < 1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ vật liệu trang trí", ""));
            } else {
                player.potions[120]--;
                player.banphaohoa++;
                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                } else {
                    int[] per = new int[]{500, 500, 300, 200, 500, 500, 500, 300, 200, 500, 500, 100, 150, 150, 20};
                    int[] idgift = new int[]{
                        GemTemplate.BOT_TRANG, GemTemplate.BOT_XANH, GemTemplate.BOT_DO, GemTemplate.BOT_XANH_LA, 7, 82, 114, 113, 112, 93, 95, 100, 676, 677, -1
                    };
                    int[] typegift = new int[]{6, 6, 6, 6, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, -1};
                    int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 50, 50, 1, 1, 1, 1};
                    int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 50, 50, 1, 1, 1, 1};
                    String info = "Chúc mừng bạn nhận được ";
                    int index = Char.getIndexRandom(per);
                    if (typegift[index] == -1) {
                        if (player.isFullInventory()) {
                            int xu = 5000 + r.nextInt(100000);
                            player.addXu((long) xu, "map 54");
                            info = info + xu + " xu" + ", ";
                            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else {
                            int[] idtem = new int[]{681, 682, 683};
                            int[] time = new int[]{1, 3, 5, 7};
                            Item it = this.doCreateHoaKyLan(player, idtem[r.nextInt(idtem.length)], (long) (time[r.nextInt(time.length)] * 24 * 60));
                            if (it != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage(info + " " + it.getName(), ""));

                                try {
                                    sendAllCharServer(
                                            -1,
                                            MessageCreator.createServerAlertAutoOffMessage(
                                                    "Chúc mừng " + player.charname + " nhận được " + it.getTemplate().name + " khi trang trí cây thông."
                                            )
                                    );
                                } catch (IOException var14) {
                                }
                            }
                        }
                    } else if (typegift[index] == 6) {
                        int soluong = soluongmin[index];
                        if (soluongmin[index] < soluongmax[index]) {
                            soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                        }

                        player.doAddGemItem(idgift[index], soluong, true);
                        info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name + ", ";
                        player.sendMessage(MessageCreator.createCharGemItem(player));
                        player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    } else if (typegift[index] == 4) {
                        int soluong = soluongmin[index];
                        if (soluongmin[index] < soluongmax[index]) {
                            soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                        }

                        player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
                        info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]] + ", ";
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        if (idgift[index] == 9) {
                            try {
                                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được nhân sâm khi mở bao lì xì"));
                            } catch (IOException var13) {
                            }
                        }
                    } else if (typegift[index] == 3) {
                        if (player.isFullInventory()) {
                            int xu = 5000 + r.nextInt(100000);
                            player.addXu((long) xu, "map 52");
                            info = info + xu + " xu" + ", ";
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        } else {
                            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                            Item newItem = null;
                            newItem = new Item(template, false, 0, 0, template.id);
                            newItem.id = player.getIDItem();
                            newItem.owner = player.charDBID;
                            newItem.level = newItem.getTemplate().level;
                            player.iItems.add(newItem);
                            newItem.identify = player.charDBID;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            info = info + newItem.getName();
                        }
                    }

                    player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    Database.instance.saveOrtherLog("", player.charname, info, "hopquamayman");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Database.instance.saveOrtherLog("", player.charname, "trang tri cay thong " + player.banphaohoa, "caythong");
                    if (System.currentTimeMillis() - timeHoaTuyet > 0L) {
                        Message msg = MessageCreator.createMsgWeather(2, 50, 60000);

                        for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                            Char c = (Char) CharManager.instance.vChars.get(i);
                            c.sendMessage(msg);
                        }

                        timeHoaTuyet = System.currentTimeMillis() + 65000L;
                    }
                }
            }
        }
    }

    // Than method (303 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public void doDotDen(Char player, int type_dotden) {
        MapXuLyLenh.doDotDen(this, player,type_dotden);
    }

    public void doGiftQuestChucNu(Char player) {
        int[] per = new int[]{
            4000, 4000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 7000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 500, 100
        };
        int[] idgift = new int[]{
            94,
            96,
            13,
            7,
            112,
            113,
            114,
            35,
            29,
            105,
            GemTemplate.ID_MATERIAL_LOW[2][0],
            GemTemplate.ID_MATERIAL_LOW[2][1],
            GemTemplate.ID_MATERIAL_LOW[2][2],
            GemTemplate.ID_MATERIAL_LOW[2][3],
            GemTemplate.ID_MATERIAL_LOW[2][4],
            GemTemplate.ID_MATERIAL_LOW[3][0],
            GemTemplate.ID_MATERIAL_LOW[3][1],
            GemTemplate.ID_MATERIAL_LOW[3][2],
            GemTemplate.ID_MATERIAL_LOW[3][3],
            GemTemplate.ID_MATERIAL_LOW[3][4],
            GemTemplate.DA_TIEN_GIAI,
            -2,
            -3
        };
        int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, -2, -3};
        int[] soluongmax = new int[]{100, 100, 1, 3, 3, 3, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] soluongmin = new int[]{100, 100, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] xu = new int[]{10000, 12000, 15000, 18000, 20000, 22000, 25000, 30000};
        int index = Char.getIndexRandom(per);
        int x = xu[r.nextInt(xu.length)];
        String info = "Chúc mừng bạn nhận được " + x + " xu, ";
        player.addXu((long) x, "map 577");
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        if (typegift[index] == -3) {
            int[] time = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7};
            int idt = r.nextInt(time.length);
            player.createPoro((long) time[idt], false);
            info = info + " đại bạch thử " + time[idt] + " ngày, ";
        } else if (typegift[index] == -1) {
            int soluong = r.nextInt(11) * 1000 + 10000;
            player.addXu((long) soluong, "map 57");
            info = info + soluongmax[index] + " xu";
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == -2) {
            int[] minute = new int[]{180, 300, 420};
            Item newItem = ItemLuckyDraw.createItemCoat(player, r.nextInt(2), r.nextInt(100) < 50, minute[r.nextInt(minute.length)]);
            player.iItems.add(newItem);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                try {
                    sendAllCharServer(
                            -1,
                            MessageCreator.createServerAlertAutoOffMessage(
                                    player.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi làm nhiệm vụ tìm trẻ lạc"
                            )
                    );
                } catch (IOException var14) {
                }
            }
        }

        Database.instance.saveOrtherLog("", player.charname, info, "chucnu");
    }

    public void doGiftQuestDuaTre2020(Char player) {
        int[] per = new int[]{5000, 5000, 5000, 5000, 5000, 200, 500, 5000, 300, 10, 10, 10, 10, 10, 10, 5000, 5000, 5000, 5000, 200, 10, 200, 0};
        int[] idgift = new int[]{
            93,
            95,
            8,
            13,
            7,
            GemTemplate.BOT_XANH_LA,
            GemTemplate.BOT_XANH,
            GemTemplate.BOT_TRANG,
            GemTemplate.BOT_DO,
            GemTemplate.ID_MATERIAL_LOW[5][0],
            GemTemplate.ID_MATERIAL_LOW[5][1],
            GemTemplate.ID_MATERIAL_LOW[5][2],
            GemTemplate.ID_MATERIAL_LOW[5][3],
            GemTemplate.ID_MATERIAL_LOW[5][4],
            GemTemplate.LKD_35,
            -1,
            -1,
            -1,
            -1,
            -1,
            GemTemplate.HOA_THACH_C1,
            -2,
            -3,
            GemTemplate.DA_THUOC_TINH_05
        };
        int[] typegift = new int[]{4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, -1, -1, -1, -1, -1, 6, -2, -3};
        int[] soluongmax = new int[]{100, 100, 5, 5, 10, 50, 100, 100, 50, 3, 3, 3, 3, 1, 1, 5000, 10000, 15000, 20000, 500000, 1};
        int[] soluongmin = new int[]{50, 50, 1, 1, 1, 10, 50, 50, 10, 1, 1, 1, 1, 1, 1, 5000, 10000, 50000, 200000, 500000, 1};
        String info = "Chúc mừng bạn nhận được ";
        int index = Char.getIndexRandom(per);
        if (typegift[index] == -1) {
            int soluong = r.nextInt(11) * 1000 + 10000;
            player.addXu((long) soluong, "map 57");
            info = info + soluongmax[index] + " xu";
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == -2) {
            int[] minute = new int[]{180, 300, 420};
            Item newItem = ItemLuckyDraw.createItemCoat(player, r.nextInt(2), r.nextInt(100) < 50, minute[r.nextInt(minute.length)]);
            player.iItems.add(newItem);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_40) {
                try {
                    sendAllCharServer(
                            -1,
                            MessageCreator.createServerAlertAutoOffMessage(
                                    player.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi làm nhiệm vụ tìm trẻ lạc"
                            )
                    );
                } catch (IOException var12) {
                }
            }
        }

        Database.instance.saveOrtherLog("", player.charname, info, "duabe");
    }

    /**
     * Mo 1 "Bo hoa vang" (su kien Vu Lan viet lai, xem real/Potion.TYPE_BO_HOA_VANG).
     *
     * TAI SU DUNG ten ham va vi tri cu (truoc day la thuong het nhiem vu Vu Lan 2015,
     * chi 500000 exp + 1 bang thuong ngau nhien) - noi dung ben trong da thay het theo
     * danh sach qua that su cua ban Vu Lan moi. Goi tu doUsePotion.java khi nguoi choi
     * bam dung Bo hoa vang.
     *
     * Khuon mau bang thuong: giong het doGiftQuestChucNu/doBanPhaoHoaGioto - mang song
     * song per (trong so) / idgift / typegift / soluongmin / soluongmax, chon bang
     * Char.getIndexRandom(per) roi re nhanh theo typegift. Type dung lai: 4=potions[],
     * 6=gemTemplate qua doAddGemItem. Type moi rieng cho Vu Lan (xem doTraThuongVuLan):
     * -10=vu khi thoi trang (mua ban duoc), -11=ho ve lenh 7/3 ngay theo isBoVang (mua
     * ban duoc), -12=NLSC tu chon bac (khoa/khong khoa ngau nhien, bac theo isBoVang),
     * -13=ban do nui chau bau (GemTemplate.BAN_DO_KHO_BAU), -14=huyet linh thao
     * (Map.doCreateHuyetLinhThao), -15=manh Bat Gioi/Sa Tang (thay Ngo Khong/Duong Tang -
     * chua co trong DB), -16=mat na ong dia (thoi han 3-5 ngay, ti le nho ra ban vinh
     * vien), -17=ngoc huyen minh tu chon cap theo isBoVang, -18=exp ngau nhien
     * (soluongmin/max la khoang exp, khac voi cac dong khac luon co soluong=1).
     */
    public void doGiftQuestVulan(Char player) {
        int[] per = new int[]{
            1200, 1200, 1200, 1000, 1000, 1000, 1200,
            600, 500,
            700,
            400,
            600,
            300,
            500,
            300,
            200,
            300,
            200,
            80
        };
        int[] idgift = new int[]{
            Potion.TYPE_LUA_TO, Potion.TYPE_LUA_VUA, Potion.TYPE_LUA_THUONG,
            Potion.TYPE_ATTACK_FOOD, Potion.TYPE_DEF_FOOD, Potion.TYPE_ALL_FOOD,
            Potion.TYPE_BINH_TANG_TOC,
            GemTemplate.LKD_30, -10,
            -11,
            -12,
            Potion.TYPE_SACH_SKILL_PET,
            -14,
            -17,
            Potion.TYPE_HP7000,
            Potion.TYPE_TIEN_DAN,
            -13,
            -15,
            -16
        };
        int[] typegift = new int[]{
            4, 4, 4,
            4, 4, 4,
            4,
            6, -10,
            -11,
            -12,
            4,
            -14,
            -17,
            4,
            4,
            -13,
            -15,
            -16
        };
        int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        // "Ngau nhien 300-500k exp" chen rieng ngoai bang trong so per (khong dung chung
        // co che typegift 1-don-vi voi cac dong tren, can range rieng cho soluong).
        per = appendInt(per, 500);
        idgift = appendInt(idgift, -18);
        typegift = appendInt(typegift, -18);
        soluongmin = appendInt(soluongmin, 300000);
        soluongmax = appendInt(soluongmax, 500000);
        int index = Char.getIndexRandom(per);
        String info = doTraThuongVuLan(player, idgift[index], typegift[index], soluongmin[index], soluongmax[index], true);
        Database.instance.saveOrtherLog("", player.charname, info, "bohoavangvulan");
    }

    /**
     * Mo 1 "Bo hoa do" (su kien Vu Lan viet lai) - phan thuong nhe hon bo hoa vang,
     * dung chung ham tra thuong doTraThuongVuLan. Goi tu doUsePotion.java.
     */
    public void doGiftBoHoaDoVuLan(Char player) {
        int[] per = new int[]{
            1300, 1300, 1300,
            1000, 1000, 1000,
            1200,
            700,
            500,
            600,
            500,
            300,
            200,
            400,
            300,
            300
        };
        int[] idgift = new int[]{
            Potion.TYPE_LUA_VUA, Potion.TYPE_LUA_THUONG, Potion.TYPE_LUA_THUONG,
            Potion.TYPE_ATTACK_FOOD, Potion.TYPE_DEF_FOOD, Potion.TYPE_ALL_FOOD,
            Potion.TYPE_BINH_TANG_TOC,
            GemTemplate.LKD_30,
            -11,
            -12,
            -14,
            Potion.TYPE_HP7000,
            Potion.TYPE_TIEN_DAN,
            -13,
            -15,
            -15
        };
        int[] typegift = new int[]{
            4, 4, 4,
            4, 4, 4,
            4,
            6,
            -11,
            -12,
            -14,
            4,
            4,
            -13,
            -15,
            -15
        };
        int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        per = appendInt(per, 500);
        idgift = appendInt(idgift, -18);
        typegift = appendInt(typegift, -18);
        soluongmin = appendInt(soluongmin, 100000);
        soluongmax = appendInt(soluongmax, 300000);
        int index = Char.getIndexRandom(per);
        String info = doTraThuongVuLan(player, idgift[index], typegift[index], soluongmin[index], soluongmax[index], false);
        Database.instance.saveOrtherLog("", player.charname, info, "bohoadovulan");
    }

    /** Noi them 1 phan tu vao cuoi mang int - dung de ghep dong "exp ngau nhien" vao bang thuong Vu Lan. */
    private static int[] appendInt(int[] a, int v) {
        int[] r2 = java.util.Arrays.copyOf(a, a.length + 1);
        r2[a.length] = v;
        return r2;
    }

    /**
     * Tra 1 phan thuong Vu Lan cho player theo (idgift, typegift), dung chung cho ca bo
     * hoa vang va bo hoa do (isBoVang phan biet thoi han Ho ve lenh 7 vs 3 ngay va mo ta
     * chi tiet mang Bat Gioi/Sa Tang bac tren/duoi). Tra ve chuoi mo ta de ghi log.
     */
    private String doTraThuongVuLan(Char player, int idgift, int typegift, int soluongmin, int soluongmax, boolean isBoVang) {
        String info = "Chúc mừng bạn nhận được ";
        if (player.isFullInventory() && typegift != 4 && typegift != -18) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy, không nhận thêm được.", ""));
            return info + "(hanh trang day)";
        }
        if (typegift == 4) {
            int soluong = soluongmin;
            if (soluongmin < soluongmax) {
                soluong += r.nextInt(soluongmax - soluongmin + 1);
            }
            player.potions[idgift] = player.potions[idgift] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift];
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift == 6) {
            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgift, 1, lock);
            info = info + gemTemplate[idgift].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -10) {
            // Vu khi thoi trang co the tinh luyen, MUA BAN DUOC. createVuKhiThoiTrang
            // luon ep newItem.lock = 1 (bo qua tham so isLock - loi co san trong ham
            // do), nen phai tu mo khoa lai item vua tao (la phan tu cuoi cua iItems).
            int soLuongTruoc = player.iItems.size();
            Char.createVuKhiThoiTrang(player, r.nextInt(2), 0, null, false, true, 1, false);
            if (player.iItems.size() > soLuongTruoc) {
                Item vuKhi = player.iItems.lastElement();
                vuKhi.lock = 0;
                info = info + vuKhi.getName() + " (có thể giao dịch)";
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            }
        } else if (typegift == -11) {
            int minute = isBoVang ? 10080 : 4320;
            Item hoVeLenh = doCreateHoVeLenh(player, minute);
            if (hoVeLenh != null) {
                info = info + hoVeLenh.getName() + " thời hạn " + (isBoVang ? "7" : "3") + " ngày (có thể giao dịch)";
            }
        } else if (typegift == -12) {
            // NLSC tu chon bac: bo vang cap 4-5, bo do cap 3-4 - dai bac lay tu isBoVang.
            int bacMin = isBoVang ? 3 : 2;
            int bac = bacMin + r.nextInt(2);
            int idgem = GemTemplate.ID_MATERIAL_LOW[bac][r.nextInt(GemTemplate.ID_MATERIAL_LOW[bac].length)];
            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idgem, 1, lock);
            info = info + gemTemplate[idgem].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -13) {
            boolean lock = true;
            player.doAddGemItem(GemTemplate.BAN_DO_KHO_BAU, 1, lock);
            info = info + gemTemplate[GemTemplate.BAN_DO_KHO_BAU].name + " khoá";
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -14) {
            Item hlt = doCreateHuyetLinhThao(player, r.nextInt(2));
            if (hlt != null) {
                info = info + hlt.getName();
            }
        } else if (typegift == -15) {
            // Manh Bat Gioi/Sa Tang thay cho Ngo Khong/Duong Tang (chua co trong DB) -
            // random 1 trong cac manh da co san (id 848-871).
            int idManh = 848 + r.nextInt(871 - 848 + 1);
            boolean lock = true;
            player.doAddGemItem(idManh, 1, lock);
            info = info + gemTemplate[idManh].name + " khoá";
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -17) {
            // Ngoc huyen minh tu chon cap: bo vang cap 5-6, bo do cap 4-5. ID_NGOC_HUYEN_MINH
            // = {235..240} ung voi cap 1..6, nen chi so cap N la N-1.
            int capMin = isBoVang ? 5 : 4;
            int cap = capMin + r.nextInt(2);
            int idngoc = GemTemplate.ID_NGOC_HUYEN_MINH[cap - 1];
            boolean lock = r.nextInt(2) == 1;
            player.doAddGemItem(idngoc, 1, lock);
            info = info + gemTemplate[idngoc].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -18) {
            int soluong = soluongmin;
            if (soluongmin < soluongmax) {
                soluong += r.nextInt(soluongmax - soluongmin + 1);
            }
            addXpCharEvent(player, (long) soluong, false, isBoVang ? "bohoavangvulan" : "bohoadovulan");
            info = info + soluong + " exp";
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift == -16) {
            // Mat na Ong Dia (item 872) thoi han 3-5 ngay, MUA BAN DUOC, ti le nho ra
            // ban VINH VIEN. doCreateMatNa(player, minute, ...): minute>0 tu do KHONG
            // khoa item (dung y cho ban co thoi han - mua ban duoc), minute<=0 thi TU
            // KHOA (phai mo lai vi ban vinh vien cung phai mua ban duoc theo yeu cau).
            int[] soNgay = new int[]{3, 4, 5};
            int minute = soNgay[r.nextInt(soNgay.length)] * 1440;
            boolean vinhVien = r.nextInt(100) < 2;
            Item mask = doCreateMatNa(player, vinhVien ? 0 : minute, 872, 0, 0);
            if (mask != null) {
                mask.lock = 0;
                info = info + mask.getName() + (vinhVien ? " (vĩnh viễn, có thể giao dịch)" : " thời hạn " + (minute / 1440) + " ngày (có thể giao dịch)");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            }
        }
        return info;
    }

    public Item doGenNewItem(
            Char player,
            int idTemplate,
            int gender,
            int charClass,
            int typeItem,
            int levelItem,
            int magic_physic,
            int plusItem,
            int option,
            Item oldItem,
            boolean uplv
    ) {
        Item item = null;

        try {
            int n = 2;
            int[][] material = new int[n][];
            material[0] = new int[6];
            material[1] = new int[6];

            for (int i = 0; i < 6; i++) {
                material[0][i] = 5;
            }

            for (int i = 0; i < 6; i++) {
                material[1][i] = 5;
            }

            byte[] id = CreateItemTemplate.getIDMAterial(typeItem, magic_physic, 0);
            Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(typeItem >= 3 && typeItem <= 7 ? (levelItem - 21) / 5 : (levelItem - 20) / 5, option);
            if (soluong.size() == 0) {
                return null;
            }

            int[][] perc = getPercentCreateItem(0, (Byte) soluong.get(0), material[0], (Byte) soluong.get(1), material[1]);

            for (int i = 0; i < perc.length; i++) {
                int j = 0;

                while (j < perc[i].length) {
                    j++;
                }
            }

            if (perc == null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Nguyên liệu sử dụng không hợp lệ", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return null;
            }

            String[] nameItem = new String[]{"Trắng", "Xanh", "Đỏ", "Hoàn mỹ"};
            int[] colorName = new int[]{0, 3, 2, 1};
            int pc = r.nextInt(100);
            int tempPC = 110;
            int posPC = 0;
            int i = 0;

            for (String nItem = ""; i < 4; i++) {
                int max = 0;
                int posMax = 0;

                for (int j = 0; j < 4; j++) {
                    if (perc[1][j] > 0 && perc[1][j] < tempPC && perc[1][j] > max) {
                        max = perc[1][j];
                        posMax = j;
                    }
                }

                posPC += max;
                tempPC = max;
                if (pc <= posPC) {
                    nItem = nameItem[posMax];
                    if (typeItem >= 3 && typeItem <= 7) {
                        item = MapCheTao.createItemWeapone(this, player, posMax, idTemplate, charClass, levelItem, plusItem);
                    } else {
                        item = MapCheTao.createItemArmor(this, player, posMax, idTemplate, charClass, levelItem, plusItem, typeItem, gender);
                    }

                    item.level = item.getTemplate().level;
                    item.magic_physic = (byte) magic_physic;
                    if (item.getTemplate().type < 3 || item.getTemplate().type == 10 || item.getTemplate().type == 11) {
                        if (magic_physic == 0) {
                            item.atb[6] = item.atb[1];
                            item.atb[1] = (short) (item.atb[1] / 10);
                        } else if (magic_physic == 1) {
                            item.atb[6] = (short) (item.atb[1] / 10);
                        }
                    }

                    item.colorName = (byte) colorName[posMax];
                    item.heItem = (byte) r.nextInt(5);
                    if (oldItem != null) {
                        item.heItem = oldItem.heItem;
                    }

                    String hangitem = "";
                    if (posMax > 0) {
                        int idAtt = r.nextInt(5);
                        int[] pcAtt = getPercentAppearAtt((Byte) soluong.get(0), material[0], posMax, material[1][5] > 0 ? material[1][5] : 0);
                        System.out.print(pcAtt[0] + "," + pcAtt[1] + "," + pcAtt[2] + "," + pcAtt[3] + "," + pcAtt[4]);
                        String[] nameAtt = new String[]{"", "Nhất phẩm", "Nhị phẩm", "Tam phẩm", "Tứ phẩm", "Ngũ phẩm"};
                        int[] idOfAtt = new int[]{5, 4, 3, 2, 1};
                        int pcatt = r.nextInt(100);
                        int tempPCAtt = -100;
                        int posPCAtt = 0;

                        for (int l = 0; l < 5; l++) {
                            int maxAtt = 200;
                            int posMaxAtt = 0;

                            for (int jx = 0; jx < 5; jx++) {
                                if (pcAtt[jx] > 0 && pcAtt[jx] > tempPCAtt && pcAtt[jx] < maxAtt) {
                                    maxAtt = pcAtt[jx];
                                    posMaxAtt = jx;
                                }
                            }

                            posPCAtt += maxAtt;
                            tempPCAtt = maxAtt;
                            if (pcatt <= posPCAtt) {
                                item.hangItem = (byte) idOfAtt[posMaxAtt];
                                int[][][] pcAttSp = new int[][][]{
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    },
                                    {{5}, {4}, {3}, {2}, {1}},
                                    {
                                        {8, 9, 8, 8, 8, 9, 8, 8, 8, 8, 8, 8},
                                        {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6},
                                        {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4},
                                        {2, 3, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2},
                                        {1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1}
                                    }
                                };
                                if (idOfAtt[posMaxAtt] == 1) {
                                    pcAttSp = new int[][][]{
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        },
                                        {{5}, {4}, {3}, {1, 2}, new int[0]},
                                        {
                                            {8, 9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9},
                                            {6, 7, 6, 6, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6},
                                            {4, 5, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4},
                                            {1, 2, 3},
                                            new int[0]
                                        }
                                    };
                                }

                                int[] tempArr = pcAttSp[idAtt][idOfAtt[posMaxAtt] - 1];
                                item.atb[idAtt + 28] = (short) tempArr[r.nextInt(tempArr.length)];
                                hangitem = nameAtt[item.hangItem];
                                byte[][] var10000 = new byte[][]{
                                    new byte[1],
                                    {10, 11, 10, 11, 10, 10, 10, 10, 11, 10, 12, 10, 12, 10, 10, 10, 10, 10},
                                    {7, 8, 7, 8, 7, 7, 7, 7, 8, 7, 9, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7},
                                    {4, 5, 4, 5, 4, 4, 4, 4, 5, 4, 6, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4},
                                    {1, 2, 1, 2, 1, 1, 1, 2, 2, 1, 3, 1, 3},
                                    new byte[1]
                                };
                                if (uplv) {
                                    var10000 = new byte[][]{
                                        new byte[1],
                                        {10, 11, 10, 11, 10, 10, 10, 10, 11, 10, 10, 10, 12, 10, 10, 10, 10, 10, 10},
                                        {7, 8, 7, 8, 7, 7, 7, 7, 8, 7, 7, 7, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
                                        {4, 5, 4, 5, 4, 4, 4, 4, 5, 4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
                                        {1, 2, 1, 2, 1, 1, 1, 2, 2, 1, 1, 1, 3},
                                        new byte[1]
                                    };
                                }

                                byte addMore = CreateItemTemplate.getAddMorGreen(item.hangItem);
                                byte pointAdd = CreateItemTemplate.getPointAddForRank(item.level, item.hangItem);
                                byte bu = CreateItemTemplate.getBuCong(item.level, item.colorName, item.hangItem, item.getTemplate().type);

                                for (int k = 0; k < 10; k++) {
                                    item.atb[k] = (short) (item.atb[k] + addMore + pointAdd + bu);
                                }
                                break;
                            }
                        }
                    }

                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Chúc mừng bạn đã tạo được " + item.getTemplate().name + " " + hangitem + " " + nameItem[posMax], "")
                    );
                    if (oldItem != null) {
                        item.lock = 1;
                    }

                    item.owner = player.charDBID;
                    item.id = player.getIDItem();
                    player.iItems.add(item);
                    item.charSeal = player.charname;
                    item.dateCreate = Char.getDayTime(0L);
                    if (item.colorName != 0 && item.colorName != 4) {
                        item.initAtt(material[1]);
                        item.changLevelSkill();
                    }

                    item.identify = player.charDBID;
                    item.lockItem(material[1]);
                    if (item.lock == 0) {
                        if (player.infoItemCreate.materialUse == 1) {
                            item.lock = 1;
                        } else if (player.infoItemCreate.materialUse == 2) {
                            try {
                                if (material[0] != null) {
                                    for (int m = 0; m < material[0].length; m++) {
                                        if (material[0][m] > 0 && player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[0]] + m] > 0) {
                                            item.lock = 1;
                                            break;
                                        }
                                    }
                                }

                                if (material[1] != null) {
                                    for (int mx = 0; mx < material[1].length; mx++) {
                                        int aa = CreateItemTemplate.startIDMaterial[id[id.length < 2 ? 0 : 1]] + mx;
                                        if (material[1][mx] > 0 && player.listGemitemLock[aa] > 0) {
                                            item.lock = 1;
                                            return item;
                                        }
                                    }
                                }
                            } catch (Exception var46) {
                                var46.printStackTrace();
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception var47) {
            var47.printStackTrace();
        }

        return item;
    }




    public void doMenuTongKim(Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0 && idOptionMenu == 0) {
            if (player.idTongKim > -1) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn đã đăng ký tham gia chiến trường tống kim, nhóm của bạn là nhóm " + (player.idTeamTongKim + 1), "")
                );
                return;
            }

            GroupTongKim gr = MapTongKim.registerToGroup();
            int result = gr.registerToGroup(player.charname, teamtongkim);
            if (result == -1) {
                player.idTongKim = MapTongKim.ID_TONG_KIM;
                player.idTeamTongKim = (byte) (abs(result) - 1);
                teamtongkim = (byte) (abs(result) % 2);
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn đã đăng ký tham gia chiến trường tống kim, nhóm của bạn là nhóm " + abs(result), "")
                );
            } else if (result == -2) {
                player.idTongKim = MapTongKim.ID_TONG_KIM;
                player.idTeamTongKim = (byte) (abs(result) - 1);
                teamtongkim = (byte) (abs(result) % 2);
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn đã đăng ký tham gia chiến trường tống kim, nhóm của bạn là nhóm " + abs(result), "")
                );
            }
        }
    }







    public void doMenuTown(Char player, int idNpc, int idMenu, int idOptionMenu) {
    }

    void doHoaTieu(Char player, int idNpc, int idMenu, int idOptionMenu) {
        int idMap;
        int[] xy;
        if (idNpc == -100 || idNpc == -101) {
            // Hoa Tieu moi cho map 26 "Rung gia" (-100) va map 113 "Ham toi" (-101)
            // (lo #4 loi.xlsx) - id qua nho/lon de dung chung cong thuc idNpc+48 voi
            // 11 Hoa Tieu cu (-38..-48) nen tach rieng, khong dung mang posHoaTieu/
            // IDMAP_HOA_TIEU dung chung. Chi co 2 lua chon dich moi noi, toa do dich
            // lay lai tu xyNPC cua Hoa Tieu cu (da biet la vi tri dung duoc).
            int[] destMapId = (idNpc == -100) ? new int[]{114, 10} : new int[]{114, 15};
            int[][] destXY = (idNpc == -100) ? new int[][]{{64, 8}, {71, 123}} : new int[][]{{64, 8}, {9, 19}};
            idMap = destMapId[idOptionMenu];
            xy = destXY[idOptionMenu];
        } else {
            idMap = RequestNPCInfoHandler.getIDMap(idNpc + 48, idOptionMenu);
            xy = RequestNPCInfoHandler.getXYMap(idNpc + 48, idOptionMenu);
        }
        this.playerExit(player);
        player.region = 0;
        Map oldMap = player.map;
        if (Char.isSuKienMiniChucNu()) {
            player.isDoChangeMap = true;
        }

        this.move2Map(player, xy[0], xy[1], idMap, player.inCountry);
        if (Char.isSuKienMiniChucNu()) {
            player.doChangeMapMonster(false, oldMap);
            player.isDoChangeMap = false;
        }
    }

    private void doSetTeam(Vector<String> list1, Vector<Char> list2) {
        try {
            TeamArena team1 = new TeamArena(TeamArena.genIDTeam());
            TeamArena team2 = new TeamArena(TeamArena.genIDTeam());
            ArenaMap mm = (ArenaMap) RealController.mapList.get(204);
            ArenaData arena = new ArenaData();
            arena.addTeam(team1);
            arena.addTeam(team2);
            arena.idArena = IDARENA;
            arena.start();
            mm.doAddArena(arena);

            while (list2.size() > 0) {
                Char c1 = (Char) list2.remove(0);
                Char c2 = (Char) list2.remove(0);
                c1.idArena = IDARENA;
                c2.idArena = IDARENA;
                team1.addMember(c1);
                team2.addMember(c2);
                Message m = new Message(65);
                m.dos.writeShort(c1.id);
                if (c1.pk == 0) {
                    c1.pk = 14;
                    m.dos.writeByte(1);
                    m.dos.writeByte(14);
                    c1.timeUsePK = System.currentTimeMillis();
                    c1.sendMessage(m);
                }

                m = new Message(65);
                m.dos.writeShort(c2.id);
                if (c2.pk == 0) {
                    c2.pk = 15;
                    m.dos.writeByte(1);
                    m.dos.writeByte(15);
                    c2.timeUsePK = System.currentTimeMillis();
                    c2.sendMessage(m);
                }

                list1.remove(c1.charname);
                list1.remove(c2.charname);
            }

            arena.sendInfoMatch = true;
            arena.timeWaitFight = System.currentTimeMillis() + 30000L;
            IDARENA = (IDARENA + 1) % 2000000000;
        } catch (Exception var10) {
        }
    }

    public void doCheckCreateMatch(Char player) {
    }

    public void doCheckCreateMatch() {
        for (int i = 0; i < listRegArena.size(); i++) {
            Vector<String> list = (Vector<String>) listRegArena.get(i);
            this.doStartMatch(list);
        }
    }

    private void doStartMatch(Vector<String> list) {
        try {
            if (list.size() < MAX_MEMBER_ARENA) {
                return;
            }

            Vector<Char> selectChar = new Vector();

            for (int i = 0; i < list.size(); i++) {
                Char p = CharManager.instance.getCharByCharName((String) list.get(i));
                if (p == null) {
                    list.remove(p);
                } else {
                    selectChar.add(p);
                    if (selectChar.size() == MAX_MEMBER_ARENA) {
                        this.doSetTeam(list, selectChar);
                        if (list.size() < MAX_MEMBER_ARENA) {
                            break;
                        }

                        i = 0;
                    }
                }
            }
        } catch (Exception var5) {
        }
    }


    private void doRedeemMaterial(Char player, int idOptionMenu, int type) {
    }

    private void doSendTopOfNPC() {
        if (System.currentTimeMillis() - timeGettopHoa > 900000L) {
            timeGettopHoa = System.currentTimeMillis();
            topHoa = Database.instance.getTopHoa();
        }
    }


    boolean enoughMaterial(Char player, int type, int level, int magic_physic, int option, int materialUse) {
        byte[] id = CreateItemTemplate.getIDMAterial(type, magic_physic, option);
        Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(level, option);

        for (int i = 0; i < soluong.size(); i++) {
            int total = 0;

            for (int j = 0; j < 6; j++) {
                if (materialUse == 0) {
                    total += player.listGemitem[CreateItemTemplate.startIDMaterial[id[i]] + j]
                            - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[i]] + j];
                } else if (materialUse == 1) {
                    total += player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[i]] + j];
                } else {
                    total += player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[i]] + j]
                            + (player.listGemitem[CreateItemTemplate.startIDMaterial[id[i]] + j] - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[i]] + j]);
                }
            }

            if (total < (Byte) soluong.get(i)) {
                return false;
            }
        }

        return true;
    }

    boolean enoughMaterial(Char player, int type, int level, int magic_physic, int[] material1, int[] material2, int option, byte materialUse) {
        byte[] id = CreateItemTemplate.getIDMAterial(type, magic_physic, option);
        Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(level, option);
        int total = 0;
        if (material1 != null) {
            for (int i = 0; i < material1.length; i++) {
                total += material1[i];
                if (materialUse == 0) {
                    if (player.listGemitem[CreateItemTemplate.startIDMaterial[id[0]] + i] - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[0]] + i]
                            < material1[i]) {
                        return false;
                    }
                } else if (materialUse == 1) {
                    if (player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[0]] + i] < material1[i]) {
                        return false;
                    }
                } else if (player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[0]] + i]
                        + player.listGemitem[CreateItemTemplate.startIDMaterial[id[0]] + i]
                        - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[0]] + i]
                        < material1[i]) {
                    System.out.println("KHONG DU 1");
                    return false;
                }
            }

            if (total != (Byte) soluong.get(0)) {
                return false;
            }
        }

        total = 0;
        if (material2 != null) {
            for (int ix = 0; ix < material2.length; ix++) {
                total += material2[ix];

                try {
                    if (materialUse == 0) {
                        if (player.listGemitem[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix]
                                - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix]
                                < material2[ix]) {
                            return false;
                        }
                    } else if (materialUse == 1) {
                        if (player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix] < material2[ix]) {
                            return false;
                        }
                    } else if (player.listGemitemLock[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix]
                            + player.listGemitem[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix]
                            - player.listGemitemSell[CreateItemTemplate.startIDMaterial[id[id.length > 1 ? 1 : 0]] + ix]
                            < material2[ix]) {
                        return false;
                    }
                } catch (Exception var14) {
                }
            }

            if (total != (soluong.size() > 1 ? (Byte) soluong.get(1) : (Byte) soluong.get(0))) {
                return false;
            }
        }

        return true;
    }

    short getTemplateItemCreate(int level, int type, int charClass, int gender) {
        short[] var10000 = new short[]{400, 413, 426, 439, 452};
        var10000 = new short[]{268, 292, 316, 340, 352, 364, 376, 388};
        // Hang 3-7 (kiem/dao/but/bua/cung) thieu diem level 86 (893/894/895/896/897) so
        // voi ban RealController.getTemplateItemCreate() da co san - hai noi ban dau la
        // MOT ham duoc COPY ra hai lop (RealController.doCreateItem va MapCheTao.doCreateItem,
        // deu xu ly cung npcType -8), ban RealController da duoc cap nhat rieng them cap 86
        // nhung ban nay (goi tu MapCheTao qua MapDoMenu) thi khong. Thieu dong nay lam
        // MapCheTao.doCreateItem (idMenu==11) nem ArrayIndexOutOfBoundsException ngay khi
        // idTempateItemCreate[type][13] duoc doc - loi bi nuot boi catch(Exception) bao quanh
        // nen nguoi choi khong thay gi ca, chi thay "nang cap len lv86" khong co phan ung
        // (phat hien 05/09/2026, xem test/KiemNangCapVuKhiLv86.java).
        short[][] idTempateItemCreate = new short[][]{
            {268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 640, 639},
            {292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 644, 643},
            {316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 648, 647},
            {400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 893},
            {413, 414, 415, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 894},
            {426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 895},
            {439, 440, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451, 896},
            {452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 463, 464, 897},
            {364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 651},
            {376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 655},
            {340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 659},
            {352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 663},
            {388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 667}
        };
        int index = (level - 21) / 5;
        if (type < 3) {
            index = (level - 20) / 5 * 2 + (gender == 1 ? 1 : 0);
        } else if (type > 7) {
            index = (level - 20) / 5;
        }

        return index > -1 ? idTempateItemCreate[type][index] : -1;
    }


    private Char getBot(int type) {
        for (int j = 0; j < 3; j++) {
            Vector<Char> players = (Vector<Char>) this.allPlayers.get(j);

            for (int i = 0; i < players.size(); i++) {
                if (((Char) players.get(i)).getType() != type) {
                    return (Char) players.get(i);
                }
            }
        }

        return null;
    }

    void doSendWeaponeData(Char player, Message message) {
        try {
            int type = message.dis.readByte();
            int typeItem = message.dis.readByte();
            int style = message.dis.readByte();
            int color = message.dis.readByte();
            player.sendMessage(MessageCreator.createCharWeaponeData(type, typeItem, style, color));
        } catch (Exception var7) {
        }
    }

    private void createMsgStringInfo(Char player, String st, int type) {
        try {
            Message m = new Message(-24);
            m.dos.writeByte(type);
            m.dos.writeUTF(st);
            player.sendMessage(m);
        } catch (Exception var5) {
        }
    }

    void doSendStringInfo(Char player, Message msg) {
        try {
            int type = msg.dis.readByte();
            if (type == 0) {
                this.createMsgStringInfo(player, TeamServer.stHelp, 0);
            }
        } catch (Exception var4) {
        }
    }

    // Than method (46 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public void doNapSMS(Char player, int type, String cardID, String cardCode) {
        MapXuLyLenh.doNapSMS(this, player,type,cardID,cardCode);
    }

    public void doNapSMS(Char player, Message msg) {
        try {
            // SECURITY: disabled external card top-up that sent card ID/code + account info to third-party (teamobi).
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng nạp thẻ hiện không khả dụng.", ""));
            if (true) {
                return;
            }
            if (!onSMS) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng hiện đang tạm khoá.", ""));
                return;
            }

            if (openLog && System.currentTimeMillis() - player.getSession().timeNapMoney < 30000L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Xin thử lại sau", ""));
                return;
            }

            player.getSession().timeNapMoney = System.currentTimeMillis();
            int type = msg.dis.readByte();
            String cardType = msg.dis.readUTF();
            String cardID = msg.dis.readUTF();
            String cardCode = msg.dis.readUTF();
            String url = "";
            String info = "Nạp ";
            if (!openLog && cardType.toLowerCase().indexOf("viettel") != -1) {
                System.out.print(" vt " + player.getSession().clientProvider.equals("1") + " ");
            }

            if (cardType.toLowerCase().indexOf("carot") != -1 && openLog) {
                if (type == 0) {
                    url = Net.url[0][0];
                    info = info + "Luong Teakey ";
                } else if (type == 1) {
                    url = Net.url[0][1];
                    info = info + "Xu Teakey ";
                }

                url = url
                        + player.charname
                        + "&cardid="
                        + cardID
                        + "&cardcode="
                        + cardCode
                        + "&game=9&provider="
                        + player.getSession().agentProvider
                        + "&s="
                        + TeamServer.server;
            } else if (!openLog && cardType.toLowerCase().indexOf("vina") != -1 && player.getSession().clientProvider.equals("1")) {
                if (player.getSession().clientProvider.equals("1")) {
                    if (type == 0) {
                        url = Net.url[1][3];
                        info = info + "Luong vina VTC ";
                    } else if (type == 1) {
                        url = Net.url[1][3];
                        info = info + "XU vina VTC ";
                    }

                    url = url
                            + player.charname
                            + "&cardid="
                            + cardID
                            + "&cardcode="
                            + cardCode
                            + "&provider="
                            + player.getSession().agentProvider
                            + "&moneytype="
                            + (type == 0 ? "luong" : "xu")
                            + "&server="
                            + TeamServer.server
                            + "&agent="
                            + player.agenID;
                }
            } else if (!openLog && cardType.toLowerCase().indexOf("mobi") != -1 && player.getSession().clientProvider.equals("1")) {
                if (player.getSession().clientProvider.equals("1")) {
                    if (type == 0) {
                        url = Net.url[1][2];
                        info = info + "Luong mobi VTC ";
                    } else if (type == 1) {
                        url = Net.url[1][2];
                        info = info + "XU mobi VTC ";
                    }

                    url = url
                            + player.charname
                            + "&cardid="
                            + cardID
                            + "&cardcode="
                            + cardCode
                            + "&provider="
                            + player.getSession().agentProvider
                            + "&moneytype="
                            + (type == 0 ? "luong" : "xu")
                            + "&server="
                            + TeamServer.server
                            + "&agent="
                            + player.agenID;
                } else {
                    System.out.println("NAP MOBILE ME");
                }
            } else if (!openLog && cardType.toLowerCase().indexOf("viettel") != -1 && player.getSession().clientProvider.equals("1")) {
                System.out.println("NAP THE VIETTEL VTC");
                if (player.getSession().clientProvider.equals("1")) {
                    if (type == 0) {
                        url = Net.url[1][4];
                        info = info + "Luong viettel VTC ";
                    } else if (type == 1) {
                        url = Net.url[1][4];
                        info = info + "XU viettel VTC ";
                    }

                    url = url
                            + player.charname
                            + "&cardid="
                            + cardID
                            + "&cardcode="
                            + cardCode
                            + "&provider="
                            + player.getSession().agentProvider
                            + "&moneytype="
                            + (type == 0 ? "luong" : "xu")
                            + "&server="
                            + TeamServer.server
                            + "&agent="
                            + player.agenID;
                } else {
                    System.out.println("NAP viettel ME");
                }
            } else if (!openLog && cardType.toLowerCase().indexOf("vtc") != -1) {
                if (player.getSession().clientProvider.equals("1")) {
                    if (type == 0) {
                        url = Net.url[1][0];
                        info = info + "Luong VTC ";
                    } else if (type == 1) {
                        url = Net.url[1][1];
                        info = info + "XU VTC ";
                    }

                    url = url
                            + player.charname
                            + "&cardid="
                            + cardID
                            + "&cardcode="
                            + cardCode
                            + "&provider="
                            + player.getSession().agentProvider
                            + "&moneytype="
                            + (type == 0 ? "luong" : "xu")
                            + "&server="
                            + TeamServer.server
                            + "&agent="
                            + player.agenID;
                }
            } else {
                if (!openLog) {
                    return;
                }

                if (type == 0) {
                    url = Net.url[4][0];
                    info = info + "Luong ALL ";
                } else if (type == 1) {
                    url = Net.url[4][1];
                    info = info + "Xu ALL ";
                }

                url = url
                        + player.charname
                        + "&cardid="
                        + cardID
                        + "&cardcode="
                        + cardCode
                        + "&provider="
                        + player.getSession().clientProvider
                        + "&game=9&moneytype="
                        + (type == 0 ? "luong" : "xu")
                        + "&server="
                        + TeamServer.server
                        + "&agent="
                        + player.getSession().agentProvider
                        + "&account="
                        + player.getAccountName();
            }

            String noidug = Net.getHttp(url);
            if (noidug.indexOf("http") > -1 || noidug.indexOf("www") > -1) {
                noidug = "Có lỗi xảy ra. Lỗi 41";
            }

            Database.instance.saveLogNapMoney(player.charname, cardID + "|" + cardCode + "_" + noidug);
            player.sendMessage(MessageCreator.createServerAlertMessage(noidug, ""));
        } catch (Exception var10) {
            System.out.println("LOI NAP TIEN " + var10.toString());
        }
    }

    public void doNapSMSGiftCode(Char player, String cardCode, int type) {
        try {
            if (!onSMS) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng hiện đang tạm khoá.", ""));
                return;
            }

            if (!openLog) {
                return;
            }

            if (openLog && System.currentTimeMillis() - player.getSession().timeNapMoney < 30000L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra. Xin thử lại sau", ""));
                return;
            }

            player.getSession().timeNapMoney = System.currentTimeMillis();
            String cardType = "";
            String cardID = "GIFTCODE";
            String url = "";
            String info = "Nạp ";
            if (!openLog) {
                return;
            }

            if (type == 0) {
                url = Net.url[4][0];
                info = info + "Luong ALL ";
            } else if (type == 1) {
                url = Net.url[4][1];
                info = info + "Xu ALL ";
            }

            url = url
                    + player.charname
                    + "&cardid="
                    + cardID
                    + "&cardcode="
                    + cardCode
                    + "&provider="
                    + player.getSession().clientProvider
                    + "&game=9&moneytype="
                    + (type == 0 ? "luong" : "xu")
                    + "&server="
                    + TeamServer.server
                    + "&agent="
                    + player.getSession().agentProvider
                    + "&account="
                    + player.getAccountName();
            String noidug = Net.getHttp(url);
            if (noidug.indexOf("http") > -1 || noidug.indexOf("www") > -1) {
                noidug = "Có lỗi xảy ra. Lỗi 41";
            }

            Database.instance.saveLogNapMoney(player.charname, cardID + "|" + cardCode + "_" + noidug);
            player.sendMessage(MessageCreator.createServerAlertMessage(noidug, ""));
        } catch (Exception var9) {
            System.out.println("LOI NAP TIEN " + var9.toString());
        }
    }

    // Than method (130 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    private void doSendDetailChar(Char player, Message msg) {
        MapXuLyLenh.doSendDetailChar(this, player,msg);
    }

    void doGetDetailChar(Char player, Message msg) {
        try {
            byte get = msg.dis.readByte();
            if (get == 1) {
                player.sendMessage(MessageCreator.createMsgDetailChar(msg.dis.readInt()));
            }
        } catch (Exception var4) {
            System.out.println("LOI LAY THONGs TIN CHI TIET CHAR");
        }
    }

    public void doAddLienHoaTru(Char player, int pos) {
    }


    public static CharInfo createCharInfo(Char p) {
        CharInfo charinfo = new CharInfo();
        charinfo.name = p.charname;
        charinfo.level = (byte) p.lvDetail.lv;
        charinfo.headStyle = p.headStyle;
        charinfo.idClan = p.idClan;

        try {
            String[] data = Char.split(p.infoWearing, ",");
            charinfo.country = Byte.parseByte(data[0]);

            for (short k = 1; k < data.length; k = (short) (k + 4)) {
                ItemInfo item = new ItemInfo();
                item.idTemplate = Short.parseShort(data[k]);
                item.charClass = Byte.parseByte(data[k + 1]);
                item.level = Byte.parseByte(data[k + 2]);
                item.plus = Byte.parseByte(data[k + 3]);
                charinfo.wearingItem.add(item);
            }
        } catch (Exception var5) {
        }

        return charinfo;
    }

    public synchronized void buyItemOfUser(Char player) {
    }

    public synchronized void doBuyItemOfUser(Char player, Message msg) {
        if (!this.isMapOffline) {
            if (isMapLang(player.map)) {
                try {
                    byte idNpc = msg.dis.readByte();
                    byte shopId = msg.dis.readByte();
                    short charID = msg.dis.readShort();
                    short itemID = msg.dis.readShort();

                    try {
                        int type = msg.dis.readByte();
                    } catch (Exception var11) {
                    }

                    if (idNpc != -67) {
                        return;
                    }

                    for (int i = 0; i < player.sellShopItem.size(); i++) {
                        Item it = (Item) player.sellShopItem.get(i);
                        if (it.id == itemID) {
                            if (player.getxu() < (long) it.prizeSell) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                                return;
                            }

                            long xuold = player.getxu();
                            player.subXu((long) it.prizeSell, false, "doBuyItemOfUser map");
                            player.sellShopItem.remove(i);
                            it.id = player.getIDItem();
                            player.iItems.add(it);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.saveOrtherLog("", player.charname, it.getName() + "_" + it.getInfoSave(), "mualai");
                            this.sendListItemSell2Shop(player);
                            Database.instance.saveCharAuto(player);
                            player.sendMessage(
                                    MessageCreator.createServerAlertMessage(
                                            "Đã mua " + it.getName() + " với giá " + it.prizeSell + " xu. xu trc mua " + xuold + ", xu sau mua: " + player.getxu(), ""
                                    )
                            );
                            it.prizeSell = 0;
                            return;
                        }
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                }
            }
        }
    }

    public void doUserSellItemAfterInputText(Char player, String privatename) {
    }

    public void doUserSellItem(Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        }
    }

    public void proccessDepositeItem(Char player, Message m) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        }
    }

    public void sendListItemSell2Shop(Char player) {
        Message msg = null;

        try {
            Vector<Item> item = player.sellShopItem;
            Vector<GemSellEntry> gitem = new Vector();
            msg = new Message(94);
            msg.dos.writeByte(1);
            msg.dos.writeByte(-67);
            msg.dos.writeByte(0);
            msg.dos.writeByte(item.size());
            msg.dos.writeShort(-32001);
            int size = item.size();
            if (size > 20) {
                int var13 = 20;
            }

            for (int i = 0; i < item.size(); i++) {
                Item it = (Item) item.elementAt(i);
                msg.dos.writeByte(it.clazz);
                msg.dos.writeShort(it.id);
                msg.dos.writeShort(it.getTemplate().id);
                msg.dos.writeByte(it.plus);
                msg.dos.writeInt(it.prizeSell);
                msg.dos.writeByte(it.level);
                msg.dos.writeShort(it.durable);
                msg.dos.writeByte(it.nhadSock);
                msg.dos.writeByte(it.nSocAdd);
                Vector<InfoItemAttribute> allAtb = it.getInfoAtbItem();
                msg.dos.writeByte(allAtb.size());

                for (byte j = 0; j < allAtb.size(); j++) {
                    InfoItemAttribute info = (InfoItemAttribute) allAtb.get(j);
                    msg.dos.writeByte(info.id);
                    msg.dos.writeShort(info.value);
                }

                msg.dos.writeByte(it.colorName);
                msg.dos.writeByte(it.heItem);
                msg.dos.writeByte(it.beKick);
                msg.dos.writeByte(it.hangItem);
                msg.dos.writeByte(it.magic_physic);
                msg.dos.writeByte(it.lock);
                msg.dos.writeUTF(it.charSeal);
            }

            msg.dos.writeByte(gitem.size());

            for (int i = 0; i < gitem.size(); i++) {
                GemSellEntry gem = gitem.get(i);
                msg.dos.writeShort(gem.realId);
                msg.dos.writeShort(gem.idGemTemplate);
                msg.dos.writeInt(gem.prizeSell);
            }

            player.sendMessage(msg);
        } catch (Exception var11) {
        }
    }

    public void sendListItemToUser(Char player, int charID, int npcType, int shopID, int type, int country) {
        Message msg = null;

        try {
            Vector<Item> item = npcSell[country][npcType - 11].shop[shopID].getListItemSell(charID);
            Vector<GemSellEntry> gitem = npcSell[country][npcType - 11].shop[shopID].getListGemItemSell(charID);
            msg = new Message(94);
            msg.dos.writeByte(type);
            msg.dos.writeByte(npcType);
            msg.dos.writeByte(shopID);
            msg.dos.writeByte(item.size());
            msg.dos.writeShort(charID);

            for (int i = 0; i < item.size(); i++) {
                Item it = (Item) item.elementAt(i);
                msg.dos.writeByte(it.clazz);
                msg.dos.writeShort(it.id);
                msg.dos.writeShort(it.getTemplate().id);
                msg.dos.writeByte(it.plus);
                msg.dos.writeInt(it.prizeSell);
                msg.dos.writeByte(it.level);
                msg.dos.writeShort(it.durable);
                msg.dos.writeByte(it.nhadSock);
                msg.dos.writeByte(it.nSocAdd);
                Vector<InfoItemAttribute> allAtb = it.getInfoAtbItem();
                msg.dos.writeByte(allAtb.size());

                for (byte j = 0; j < allAtb.size(); j++) {
                    InfoItemAttribute info = (InfoItemAttribute) allAtb.get(j);
                    msg.dos.writeByte(info.id);
                    msg.dos.writeShort(info.value);
                }

                msg.dos.writeByte(it.colorName);
                msg.dos.writeByte(it.heItem);
                msg.dos.writeByte(it.beKick);
                msg.dos.writeByte(it.hangItem);
                msg.dos.writeByte(it.magic_physic);
                msg.dos.writeByte(it.lock);
                msg.dos.writeUTF(it.charSeal);
            }

            msg.dos.writeByte(gitem.size());

            for (int i = 0; i < gitem.size(); i++) {
                // 19/09/2026: ban theo so luong. Gui gia TONG (prizeSell * soLuong) o dung field
                // "prizeSell" cu de KHONG doi dinh dang goi tin - client van doc dung 3 field cu,
                // chi thay o dong danh sach hien "gia" la tong tien ca lo (khong phai gia 1 vien).
                // So luong that duoc noi ro trong popup xac nhan khi bam mua (doBuyItemOfUser).
                GemSellEntry gem = gitem.get(i);
                msg.dos.writeShort(gem.realId);
                msg.dos.writeShort(gem.idGemTemplate);
                msg.dos.writeInt(gem.prizeSell * gem.soLuong);
            }

            player.sendMessage(msg);
        } catch (Exception var15) {
            var15.printStackTrace();
        }
    }

    private boolean isOffLineMap(int mapID) {
        for (int i = 0; i < offMapID.length; i++) {
            if (mapID == offMapID[i]) {
                return true;
            }
        }

        return false;
    }

    public void doRepair(Char player, int id) {
        try {
            if (!this.isOffLineMap(player.mapID)) {
                System.out.println("NGAT KET NOI REPAIR ONLINE");
                CharManager.instance.kickPlayer(player, "map 9");
                Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "hack sua do trong map online >> " + player.charname, "hackR");
                data.NghiVanGianLan.ghiVatPham(player, "hackR",
                        "goi lenh sua do (id " + id + ") khi dang o map online mapID=" + player.mapID
                      + " - client goc chi cho sua trong map offline");
                return;
            }
        } catch (Exception var4) {
            System.out.println("LOI TRONG HAM DOREPAIR CLASS MAP");
        }
    }

    void doMove2Map(Char player, int index, int mapID) {
        try {
            if (Char.isSuKienMiniChucNu() && player.charCopy != null && player.map.mapId == player.charCopy.map.mapId) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang làm nhiệm vụ tìm Chức Nữ", ""));
                return;
            }

            if (player.charname.equals("kwnjpt")) {
                System.out.println(player.charname + " >> " + mapID);
            }

            if (player.char_quest != null && player.char_quest.receive == 1 && QuestTemplate.getTypeQuest(player.char_quest.id_quest) == 1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể di chuyển bằng xa phu khi đang làm nhiệm vụ vận chuyển", ""));
                return;
            }

            player.idItem.removeAllElements();
            player.idItemQuest.removeAllElements();
            player.idPotion.removeAllElements();
            this.sendInfoMap(player, index, mapID);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    void doUp2Board(Char player) {
        try {
            if (player.lvDetail.lv < 15) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải đạt được cấp độ 15 mới được ra đảo", ""));
                return;
            }

            if (player.potions[20] <= 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa mua vé", ""));
                return;
            }

            long now = System.currentTimeMillis();
            if (this.mapId == 106 || this.mapId == 2421 || this.mapId == 2422 || this.mapId == 2423 || this.mapId == 2424) {
                player.gotoIsland = true;
                player.timeWait = TeamServer.timUpBoard;
                Message m = new Message(82);
                m.dos.writeByte(1 - (int) (now - TeamServer.timUpBoard) / 60000);
                player.sendMessage(m);
                m.cleanup();
            }
        } catch (Exception var5) {
            System.out.println("LOI KHI LEN TAU");
        }
    }

    void doBuyTicket(Char player) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else if (!this.checkTrade(player)) {
            try {
                if (!this.board) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bến tàu hiện tại không hoạt động.", ""));
                    return;
                }

                if (player.getxu() < (long) potionTemplates2.get(20).getPrice()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền", ""));
                    return;
                }

                player.subXu((long) potionTemplates2.get(20).getPrice(), true, "map 70");
                player.potions[20]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveLogThongKeChiTieu("Ve di tau", 0, 1, (long)  potionTemplates2.get(20).getPrice(), "xu");
                Message m = new Message(81);
                player.sendMessage(m);
                m.cleanup();
            } catch (Exception var3) {
            }
        }
    }


    public int getToTalGem(int idGemtemplate, Char p) {
        int count = 0;

        for (int i = 0; i < p.gemItemImbue.size(); i++) {
            GemItem g = (GemItem) p.gemItemImbue.get(i);
            if (g.idGemTemplate == idGemtemplate) {
                count++;
            }
        }

        return count;
    }


    public static boolean isSpesialLkd(int id) {
        return id == GemTemplate.LKD_30 || id == GemTemplate.LKD_35 || id == GemTemplate.LKD_40;
    }

    int calculatePercenUp(Char player, int percent) {
        int pc = 0;

        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (gem.idGemTemplate > 4 && gem.idGemTemplate < 8 || gem.idGemTemplate >= 8) {
                if (isSpesialLkd(gem.idGemTemplate)) {
                    pc += gem.getTemplate().value;
                } else {
                    pc += percent * gem.getTemplate().value / 100;
                }
            }
        }

        return pc;
    }

    public static int getPercentInSu(Char player) {
        int pc = 0;

        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (gem.idGemTemplate <= 4 || gem.idGemTemplate == 155 || gem.idGemTemplate == 156) {
                return gem.getTemplate().value;
            }
        }

        return pc;
    }


    private String getLKD(Char player) {
        for (int i = 0; i < player.gemItemImbue.size(); i++) {
            GemItem gem = (GemItem) player.gemItemImbue.elementAt(i);
            if (gem.idGemTemplate > 7 && gem.idGemTemplate < 12 || gem.idGemTemplate == GemTemplate.LKD_35 || gem.idGemTemplate == GemTemplate.LKD_40) {
                return gem.getTemplate().name;
            }
        }

        return "";
    }





    public static void addGoldTimeTicket(Char player, int n) {
        ShopTemplate item = getShopTemplate(0);
        if (player.listSpItem[item.id] == 0) {
            ShopTemplate spItem = new ShopTemplate();
            spItem.coppy(spItem, item);
            spItem.ownerId = player.charDBID;
            spItem.realId = player.getIDItem();
            player.specialItem.add(spItem);
        }

        player.listSpItem[item.id] = player.listSpItem[item.id] + n;
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
    }


    // Than method (133 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    public static void doBuyModelClothe(Char player, ShopTemplate item, int nday) {
        MapXuLyLenh.doBuyModelClothe(player,item,nday);
    }

    public static Item doBuyModelClothe(Char player, int idItemtemplate, int nday, int lock) {
        ItemTemplates template = null;
        Item newItem = null;
        switch (idItemtemplate) {
            case 723:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(723));
                newItem = new Item(template, false, -1, 0, 723);
                break;
            case 724:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(724));
                newItem = new Item(template, false, -1, 0, 724);
                break;
            case 725:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(725));
                newItem = new Item(template, false, -1, 0, 725);
                break;
            case 726:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(726));
                newItem = new Item(template, false, -1, 0, 726);
                break;
            case 736:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(736));
                newItem = new Item(template, false, -1, 0, 736);
                break;
            case 737:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(737));
                newItem = new Item(template, false, -1, 0, 737);
                break;
            case 740:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(740));
                newItem = new Item(template, false, -1, 0, 740);
                break;
            case 741:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(741));
                newItem = new Item(template, false, -1, 0, 741);
                break;
            case 743:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(743));
                newItem = new Item(template, false, -1, 0, 743);
                break;
            case 744:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(744));
                newItem = new Item(template, false, -1, 0, 744);
                break;
            case 754:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(754));
                newItem = new Item(template, false, -1, 0, 754);
                break;
            case 755:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(755));
                newItem = new Item(template, false, -1, 0, 755);
                break;
            // Hai bo thoi trang bong da. PHAI co case rieng: switch nay khong co nhanh
            // default, id nao khong liet ke o day thi newItem giu null va ham tra ve null
            // trong im lang - nguoi choi tru tien xong khong nhan duoc gi.
            case 791:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(791));
                newItem = new Item(template, false, -1, 0, 791);
                break;
            case 792:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(792));
                newItem = new Item(template, false, -1, 0, 792);
        }

        if (newItem != null) {
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            player.iItems.add(newItem);
            newItem.clazz = 0;
            newItem.createAttributeItemModel(false);
            newItem.identify = player.charDBID;
            if (nday > 0) {
                newItem.minuteExist = nday * 24 * 60;
                newItem.timeLoan = System.currentTimeMillis();
            }

            newItem.lock = (byte) lock;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            Database.instance.saveOrtherLog("", player.charname, "mua  " + newItem.getName() + "|" + newItem.getInfoSave(), "dothoitrang");
            // Database.saveItem(Item) la ham RONG (no-op) - mon vua mua chi ton tai trong
            // BO NHO cho toi lan luu tu dong ke tiep, ma chu ky do la 5/7/9/11 phut ngau
            // nhien. Mat ket noi trong khoang do la nguoi choi mat tien ma khong co do.
            // Luu ngay, giong cach duong che tao da phai sua.
            Database.instance.saveCharAuto(player);
        }

        return newItem;
    }

    public static Item doDropModelClothe(Char player, ShopTemplate item, int nday) {
        ItemTemplates template = null;
        Item newItem = null;
        switch (item.id) {
            case 50:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(267));
                newItem = new Item(template, false, 0, 0, 267);
                break;
            case 51:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(265));
                newItem = new Item(template, false, 0, 0, 265);
                break;
            case 52:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(579));
                newItem = new Item(template, false, -1, 0, 579);
                break;
            case 53:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(508));
                newItem = new Item(template, false, -1, 0, 508);
                break;
            case 54:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(266));
                newItem = new Item(template, false, 0, 0, 266);
                break;
            case 55:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(264));
                newItem = new Item(template, false, -1, 0, 264);
                break;
            case 56:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(507));
                newItem = new Item(template, false, -1, 0, 507);
                break;
            case 57:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(580));
                newItem = new Item(template, false, -1, 0, 580);
                break;
            case 58:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(new Integer(581));
                newItem = new Item(template, false, -1, 0, 581);
                break;
            case 86:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(573));
                newItem = new Item(template, false, -1, 0, 573);
                break;
            case 87:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(574));
                newItem = new Item(template, false, -1, 0, 574);
                break;
            case 88:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(575));
                newItem = new Item(template, false, -1, 0, 575);
                break;
            case 89:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(576));
                newItem = new Item(template, false, -1, 0, 576);
                break;
            case 90:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(577));
                newItem = new Item(template, false, -1, 0, 577);
                break;
            case 91:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(578));
                newItem = new Item(template, false, -1, 0, 578);
                break;
            case 105:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(582));
                newItem = new Item(template, false, -1, 0, 582);
                break;
            case 106:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(583));
                newItem = new Item(template, false, -1, 0, 583);
                break;
            case 113:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(609));
                newItem = new Item(template, false, -1, 0, 609);
                break;
            case 114:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(610));
                newItem = new Item(template, false, -1, 0, 610);
                break;
            case 137:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(620));
                newItem = new Item(template, false, -1, 0, 620);
                break;
            case 138:
                template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(621));
                newItem = new Item(template, false, -1, 0, 621);
        }

        if (newItem != null) {
            item.belongUser = player.charDBID;
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            newItem.clazz = 0;
            newItem.identify = player.charDBID;
            newItem.minuteExist = nday * 24 * 60;
            newItem.timeLoan = System.currentTimeMillis();
        }

        return newItem;
    }

    public void doBuyGemItem(Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        }
    }





    protected void doPutItem2Bag(Char player, short itemID) {
        if (player.isVip()) {
            if (!this.checkTrade(player)) {
                if (player.isPutItem2Bag) {
                    if (player.bItem.size() >= Char.MAX_KHO_DO) {
                        try {
                            Message m = MessageCreator.createServerAlertMessage("Kho đồ đã đầy", "");
                            player.sendMessage(m);
                            m.cleanup();
                        } catch (Exception var6) {
                        }
                    } else {
                        for (int i = 0; i < player.iItems.size(); i++) {
                            Item item = (Item) player.iItems.get(i);
                            if (item.id == itemID) {
                                if (isItemSelled(player, itemID, item)) {
                                    return;
                                }

                                try {
                                    if (player.isExistInvector(player.bItem, itemID) == -1) {
                                        item.place = 3;
                                        player.bItem.add(item);
                                    }

                                    try {
                                        player.iItems.remove(item);
                                        Message m = new Message(68);
                                        m.dos.writeShort(item.id);
                                        player.sendMessage(m);
                                        Database.instance.saveOrtherLog("", player.charname, item.getAttribute() + "|" + item.getDBInfo(), "pitb");
                                        m.cleanup();
                                    } catch (Exception var7) {
                                    }
                                } catch (Exception var8) {
                                }

                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void doGetItemFromBag(Char player, short itemID) {
        if (player.isVip()) {
            if (player.isPutItem2Bag) {
                for (int i = 0; i < player.bItem.size(); i++) {
                    Item item = (Item) player.bItem.get(i);
                    if (item.id == itemID) {
                        if (player.isExistInvector(player.iItems, itemID) == -1) {
                            player.iItems.add(item);
                            item.place = 0;
                        }

                        try {
                            player.bItem.remove(item);
                            Message m = new Message(69);
                            m.dos.writeShort(item.id);
                            player.sendMessage(m);
                            Database.instance
                                    .saveOrtherLog(
                                            "tob_log_other_item",
                                            player.charname,
                                            item.getTemplate().name + "_" + item.plus + "_" + item.getAttribute() + "|" + item.getDBInfo(),
                                            "gfb"
                                    );
                        } catch (Exception var6) {
                            var6.printStackTrace();
                        }

                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(MessageCreator.createRequestNpcInfo(player, 9));
                        return;
                    }
                }
            }
        }
    }

    public static boolean isItemThanThu(int type) {
        return type == 25;
    }

    public static boolean isPhiPhong(int type) {
        return type == 19;
    }


    public void doRequestMainCharInfo(Char player) {
        try {
            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        } catch (Exception var3) {
        }
    }

    void doSendMainInfo(Char player, int me, int id) {
        if (me == 1) {
            try {
                Message m = MessageCreator.createMainCharInfoMessage(player);
                player.sendMessage(m);
                m.cleanup();
                m = MessageCreator.createCharWearingMessage(player, player);
                player.sendMessage(m);
                m.cleanup();
            } catch (Exception var7) {
            }
        } else {
            try {
                Char ch = CharManager.instance.getByCharID((short) id);
                if (ch == null) {
                    return;
                }

                Message m = new Message(5);
                m.dos.writeShort(ch.id);
                m.dos.writeUTF(ch.charname);
                m.dos.writeShort(ch.x);
                m.dos.writeShort(ch.y);
                m.dos.writeInt(ch.hp + ch.percentBuff[2]);
                m.dos.writeInt(ch.maxhp + ch.percentBuff[1]);
                m.dos.writeInt(ch.mp);
                m.dos.writeInt(ch.maxmp);
                m.dos.writeByte(ch.headStyle);
                m.dos.writeByte(ch.charClass);
                m.dos.write(ch.arrayBuff, 0, ch.arrayBuff.length);

                for (int i = 0; i < ch.arrayBuff.length; i++) {
                    m.dos.writeShort(ch.timeOut[i] - ch.countDown[i]);
                }

                m.dos.writeShort(ch.killer);
                m.dos.writeByte(ch.pk);
                m.dos.writeShort(Char.clampShort(ch.defend_physic + ch.percentBuff[0]));
                m.dos.writeByte(ch.lvDetail.lv);
                m.dos.writeByte(ch.he);
                m.dos.writeByte(ch.rideHorse);
                byte fly = 0;
                if (ch.animalRide != null) {
                    fly = Animal.typeAnimal[ch.animalRide.idImg];
                }

                m.dos.writeByte(fly);
                m.dos.writeByte(ch.speed + ch.getThanKhiSpeedBonus());
                player.sendMessage(m);
                m.cleanup();
            } catch (Exception var8) {
                SystemOut("LOI KHI SEND CHAR INFO TRONG DOSENDMAININFO");
            }
        }
    }

    public void doSendWearingInfo(Char player, int me, int id) {
        if (me == 1) {
            Message m = MessageCreator.createCharWearingMessage(player, player);
            player.sendMessage(m);
            m.cleanup();
        } else {
            try {
                Char ch = CharManager.instance.getByCharID((short) id);
                if (ch == null) {
                    return;
                }

                if (!ch.isCharCopy()) {
                    player.sendMessage(MessageCreator.createCharWearingMessage(ch, player));
                    if (ch.animalRide != null) {
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(ch, player));
                    }
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage(ch.getinfoCharCopy(), ""));
                }
            } catch (Exception var5) {
                var5.printStackTrace();
                SystemOut("LOI KHI SEND CHAR WEARING TRONG DOSENDMAININFO");
            }
        }
    }

    public void doSendKiller(Char player) {
        try {
            if (this.mapId == 204 || this.mapId == 205) {
                return;
            }

            if (player.lvDetail.lv < 10) {
                return;
            }

            if (player.pk != 0) {
                return;
            }

            if (player.killer == 0) {
                player.isKiller = !player.isKiller;
                Message m = new Message(67);
                m.dos.writeShort(player.id);
                m.dos.writeByte(player.isKiller ? 1 : 0);
                m.dos.writeShort(player.killer);
                player.sendMessage(m);
                player.sendToNearPlayer(m);
                m.cleanup();
            }
        } catch (Exception var3) {
        }
    }

    void doUserBuff(Char player, Message m) {
        if (!this.checkTrade(player)) {
            try {
                byte use = m.dis.readByte();
                if (use == 1) {
                    player.setBuff2Char(m);
                } else if (use == 0) {
                    player.removeBuff(m);
                }
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }
    }

    void doKickParty(Char player, Message m) {
        try {
            byte value = m.dis.readByte();
            if (value == 0) {
                player.kickOutParty(m);
                return;
            }

            if (value == 1) {
                player.deleteParty();
                return;
            }

            if (value == 2) {
                player.outParty();
            }
        } catch (Exception var4) {
        }
    }


    void doSetTaxTown(Char player, Message message) {
        try {
            if (player.myCountry == -1) {
                return;
            }

            if (idClanTown[player.myCountry] == -1) {
                return;
            }

            if (idClanTown[player.myCountry] == player.idClan && player.inCountry != player.myCountry) {
                return;
            }

            if (player.myCountry != player.inCountry) {
                return;
            }

            byte tax = message.dis.readByte();
            if (tax < 0 || tax > 30) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể đặt thuế tối đa là 30%", ""));
                return;
            }

            NewClan clanInfo = getClaninfoByID(player.idClan);
            if (player.idClan == -1 || clanInfo == null || clanInfo != null && !clanInfo.master.toLowerCase().equals(player.charname.toLowerCase())) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ mới có thể đặt thuế.", ""));
                return;
            }

            if (player.idClan != -1 && player.idClan != idClanTown[player.myCountry]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang đang sở hữu thành mới được đặt thuế.", ""));
                return;
            }

            taxOfClan[player.myCountry] = tax;
            Database.instance.saveOrtherLog("", player.charname, clanInfo.name + "_" + clanInfo.master + "_" + tax + "_" + player.myCountry, "settax");
        } catch (Exception var5) {
        }
    }

    public synchronized void playerGiveCard(Char player, int posNpc) {
    }

    public static boolean giveCardFail(Char p) {
        if ((p.myCountry <= -1 || getTown[p.myCountry]) && p.myCountry != -1) {
            Vector<NpcReceiveCard> npcReceiveCard = (Vector<NpcReceiveCard>) Map.npcReceiveCard.get(p.myCountry);

            for (int pos = 0; pos < npcReceiveCard.size(); pos++) {
                NpcReceiveCard npc = (NpcReceiveCard) npcReceiveCard.get(pos);
                if (npc.charGive != null && npc.charGive.id == p.id) {
                    npc.charGive.timeGiveCardTown = 0L;
                    npc.charGive.canGiveCard = -1;
                    npc.charGive = null;
                    npc.time = 0;
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public synchronized boolean givingCard(Char p) {
        if (!getTown[p.inCountry]) {
            return false;
        } else if (p.timeGiveCardTown > 0L) {
            return true;
        } else {
            Vector<NpcReceiveCard> npcReceiveCard = (Vector<NpcReceiveCard>) Map.npcReceiveCard.get(p.inCountry);

            for (int pos = 0; pos < npcReceiveCard.size(); pos++) {
                NpcReceiveCard npc = (NpcReceiveCard) npcReceiveCard.get(pos);
                if (npc.charGive != null && npc.charGive.id == p.id) {
                    return true;
                }
            }

            return false;
        }
    }

    protected void doGiveCardTown(Char player, Message message) {
    }


    /**
     * Nap lai toan bo top (cao thu, dai gia, bang hoi, theo phe). AN TOAN de goi
     * NHIEU LAN (khong chi luc server khoi dong) - moi lan tao Vector MOI roi gan
     * de thay hang loat, giong topRich/topLv/topClan ngay tren, KHONG dung
     * topLvNation.add()/set() nhu ban cu: goi lai add() nhieu lan se cong don thanh
     * danh sach phinh to mai (add 3 phan tu moi lan, chi 3 phan tu dau con dung),
     * ro ra khi them vong lap lam moi top dinh ky (xem TeamServer.batDau, moi 60s).
     */
    public static void getAllTopBegin() {
        topRich = Database.instance.getTopRich();
        topLv = Database.instance.getTopLV();
        topClan = Database.instance.getTopClan();

        final Vector<Vector<CharInfo>> moiTopLvNation = new Vector<Vector<CharInfo>>();
        final Vector<Vector<CharInfo>> moiTopHonor = new Vector<Vector<CharInfo>>();
        final Vector<Vector<CharInfo>> moiTopPK = new Vector<Vector<CharInfo>>();
        for (int i = 0; i < 3; i++) {
            moiTopLvNation.add(Database.instance.getTopCountry(i, 0));
            moiTopHonor.add(Database.instance.getTopCountry(i, 1));
            moiTopPK.add(Database.instance.getTopCountry(i, 2));
        }
        topLvNation = moiTopLvNation;
        topHonor = moiTopHonor;
        topPK = moiTopPK;
    }

    public void doSendTopGroup(Char p) {
        try {
            Message m = new Message(-19);
            m.dos.writeByte(8);
            m.dos.writeUTF("TOP NHÓM " + p.idGropRace);
            Vector<CharInfo> top = Database.instance.getTopGroup(p.idGropRace, true);
            m.dos.writeShort(top.size());

            for (int i = 0; i < top.size(); i++) {
                CharInfo charinfo = (CharInfo) top.get(i);
                m.dos.writeUTF(charinfo.name);
                m.dos.writeByte(charinfo.headStyle);
                m.dos.writeByte(charinfo.level);
                m.dos.writeByte(charinfo.wearingItem.size());

                for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                    ItemInfo item = (ItemInfo) charinfo.wearingItem.get(j);
                    m.dos.writeByte(item.charClass);
                    m.dos.writeShort(item.idTemplate);
                    m.dos.writeByte(item.level);
                    m.dos.writeByte(item.plus);
                }

                m.dos.writeShort(-1);
                m.dos.writeByte(0);
                m.dos.writeLong(0L);
                m.dos.writeInt(0);
                m.dos.writeByte(charinfo.country);
            }

            p.sendMessage(m);
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    // Than method (173 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    private void doSendTop(Char player, Message msg) {
        MapXuLyLenh.doSendTop(this, player,msg);
    }

    public void doSendTopNation(Char player, int idoption) {
        try {
            if (idoption < 9) {
                int nation = idoption % 3;
                Message m = new Message(-19);
                m.dos.writeByte(7);
                m.dos.writeUTF(topnation[idoption].toUpperCase());
                Vector<CharInfo> info = null;
                if (idoption < 3) {
                    info = (Vector<CharInfo>) topLvNation.get(nation);
                } else if (idoption < 6) {
                    info = (Vector<CharInfo>) topHonor.get(nation);
                } else if (idoption < 9) {
                    info = (Vector<CharInfo>) topPK.get(nation);
                }

                m.dos.writeShort(info.size());

                for (int i = 0; i < info.size(); i++) {
                    CharInfo charinfo = (CharInfo) info.get(i);
                    String online = " - off";
                    if (CharManager.instance.getCharByCharName(charinfo.name) != null) {
                        online = " - on";
                    }

                    m.dos.writeUTF(charinfo.name + online);
                    m.dos.writeByte(charinfo.headStyle);
                    m.dos.writeByte(charinfo.level);
                    m.dos.writeByte(charinfo.wearingItem.size());

                    for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                        ItemInfo item = (ItemInfo) charinfo.wearingItem.get(j);
                        m.dos.writeByte(item.charClass);
                        m.dos.writeShort(item.idTemplate);
                        m.dos.writeByte(item.level);
                        m.dos.writeByte(item.plus);
                    }

                    m.dos.writeShort(charinfo.idClan);
                    m.dos.writeByte(charinfo.titlesClan);
                    m.dos.writeLong(charinfo.money);
                    m.dos.writeInt(charinfo.luong);
                    m.dos.writeByte(charinfo.country);
                    m.dos.writeInt(charinfo.honor);
                    m.dos.writeInt(charinfo.pk);
                }

                player.sendMessage(m);
            } else {
                player.sendMessage(MessageCreator.createMsgMenu(player.getTopKillMe(), -509, 0));
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    void doSendIconPotion(Char s, int hascode, int type) {
        try {
            if (type == 2) {
                if (versionPotion != hascode) {
                    Message msg = new Message(-8);
                    msg.dos.writeByte(2);
                    msg.dos.writeByte(versionPotion);
                    msg.dos.writeShort(imgPotion.length);
                    msg.dos.write(imgPotion);
                    msg.dos.writeShort(imgItemIcon.length);
                    msg.dos.write(imgItemIcon);
                    msg.dos.writeByte(OtherHandle.dataHorse.length);

                    for (int i = 0; i < OtherHandle.dataHorse.length; i++) {
                        msg.dos.writeShort(OtherHandle.dataHorse[i].length);
                        msg.dos.write(OtherHandle.dataHorse[i]);
                        msg.dos.writeByte(OtherHandle.headerHorse[i].length);

                        for (int j = 0; j < OtherHandle.headerHorse[i].length; j++) {
                            msg.dos.writeShort(OtherHandle.headerHorse[i][j].length);
                            msg.dos.write(OtherHandle.headerHorse[i][j]);
                        }
                    }

                    s.sendMessage(msg);
                }
            } else if (type == 3) {
                if (versionGem != hascode) {
                    Message msg = new Message(-8);
                    msg.dos.writeByte(3);
                    msg.dos.writeByte(versionGem);
                    msg.dos.writeShort(imgGemItem.length);
                    msg.dos.write(imgGemItem);
                    s.sendMessage(msg);
                }
            } else if (type == 4) {
                Message msg = new Message(-8);
                msg.dos.writeByte(4);
                msg.dos.writeByte(versionPotion);
                msg.dos.writeShort(imgLinhthu.length);
                msg.dos.write(imgLinhthu);
                s.sendMessage(msg);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }




    public static void putId2IdClan(int id) {
        if (!idIconClan.contains(new Integer(id))) {
            idIconClan.add(new Integer(id));
        }
    }






















    void delFriend(Char player, String fName) {
        try {
            if (player.friendListID.size() == 0) {
                Database.instance.getFriendlist(player);
            }

            for (int i = 0; i < player.friendListName.size(); i++) {
                if (fName.toLowerCase().equals(((String) player.friendListName.get(i)).toLowerCase())) {
                    player.friendListID.remove(i);
                    player.friendListName.remove(i);
                    player.myFriend.remove(i);
                    Database.instance.saveFriendList(player);
                    return;
                }
            }

            player.friendListID.removeAllElements();
            player.friendListName.removeAllElements();
            player.myFriend.removeAllElements();
        } catch (Exception var4) {
        }
    }

    void doReadMessage(Char player, Message msg) {
        try {
            int index = msg.dis.readByte();
            CharInboxMessage ib = (CharInboxMessage) player.inbox.get(index);
            if (ib != null) {
                Database.instance.delInboxMsg(index);
            }
        } catch (Exception var5) {
        }
    }

    void doLearnSkil(Char player, Message msg) {
        try {
            int charclass = msg.dis.readByte();
            int skillSelectIndex = msg.dis.readByte();
            if (player.charClass != charclass) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể học kỹ năng của nhân vật khác", ""));
                return;
            }

            if (skillSelectIndex >= idSkill[charclass].length) {
                return;
            }

            if (player.skill[idSkill[charclass][skillSelectIndex]] > -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể học kỹ năng đã học.", ""));
                return;
            }

            if (player.getxu() < (long) priceLearnSkill[charclass][skillSelectIndex]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ tiền.", ""));
                return;
            }

            player.subXu((long) priceLearnSkill[charclass][skillSelectIndex], true, "map 80");
            player.skill[idSkill[charclass][skillSelectIndex]] = 0;
            Database.instance
                    .saveLogLearnSkill(
                            player.charname,
                            "Học skill: " + Text.nameSkillLearn[charclass][skillSelectIndex] + " với giá: " + priceLearnSkill[charclass][skillSelectIndex] + " xu"
                    );
            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
            player.sendMessage(MessageCreator.createServerAlertMessage("Kỹ năng đã được học.", ""));
        } catch (Exception var5) {
        }
    }

    void doAddSkillPoint(Char player, Message message) throws IOException {
        if (player.skill[0] == 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chưa học kỹ năng chiến đấu từ Lâm tướng quân", ""));
        } else {
            byte skillType = message.dis.readByte();
            if (skillType >= 0 && skillType < player.skill.length) {
                if (player.skill[skillType] == -1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Xin gặp Lâm tướng quân để học kỹ năng này", ""));
                } else {
                    if (player.skillpoint > 0 && player.skill[skillType] < 9) {
                        int lv = CharManager.getLvAddSkill(player.charClass, skillType, player.skill[skillType]);
                        if (player.getLevel() < lv) {
                            Message m = MessageCreator.createServerAlertMessage("Bạn chưa đủ cấp độ để cộng cho kỹ năng này.", "");
                            player.sendMessage(m);
                            m.cleanup();
                            return;
                        }

                        player.skillpoint--;
                        player.skill[skillType]++;
                        player.setTimeOutBuff();
                    }

                    player.setTimeOutBuff();
                    Message m = new Message(36);
                    player.sendMessage(m);
                    m = MessageCreator.createMainCharInfoMessage(player);
                    player.sendMessage(m);
                    m.cleanup();
                }
            }
        }
    }

    void doAddBasePoint(Char player, Message message) throws IOException {
        byte toPoint = message.dis.readByte();
        short value = message.dis.readShort();
        if (value < 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhập số âm", ""));
        } else if (value > player.basepoint) {
            try {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chỉ còn " + player.basepoint + " điểm tiềm năng", ""));
            } catch (Exception var6) {
            }
        } else {
            if (player.basepoint > 0) {
                player.basepoint -= value;
                switch (toPoint) {
                    case 0:
                        player.strength += value;
                        break;
                    case 1:
                        player.agitity += value;
                        break;
                    case 2:
                        player.spirit += value;
                        break;
                    case 3:
                        player.health += value;
                        break;
                    case 4:
                        player.luck += value;
                }
            }

            Message m = new Message(34);
            player.sendMessage(m);
            player.calculateAttrib();
            m = MessageCreator.createMainCharInfoMessage(player);
            player.sendMessage(m);
            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
            MessageCreator.createMsgCharMonster(player, player);
        }
    }

    void doGivePotion2Ground(Char player, Message msg) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (!this.checkTrade(player)) {
            try {
                short potionID = msg.dis.readShort();
                if (potionID == 88 || potionID == 89 || potionID == 90) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể bỏ", ""));
                    return;
                }

                if (potionID == 92) {
                    Wedding wd = (Wedding) allWedding.get(player.idWedding);
                    if (wd != null) {
                        if (wd.idP1 == player.charDBID || wd.idP2 == player.charDBID) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Vật phẩm không thể bỏ", ""));
                            return;
                        }

                        player.sendMessage(MessageCreator.createServerAlertMessage("Thiệp cưới đã bị hủy. Bạn có thể nhận lời mời tham gia tiệc cưới khác.", ""));
                    } else {
                        player.idWedding = -1;
                    }
                }

                String sub = player.potions[potionID] + " " + LoginHandler.PORTION_NAME[potionID] + " potion " + potionID;
                player.potions[potionID] = 0;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "Bỏ " + sub + " ra đất", "delP");
            } catch (Exception var5) {
            }
        }
    }

    void doGiveItemToGround(Char player, Message msg) throws IOException {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else if (!this.checkTrade(player)) {
            short itemID = msg.dis.readShort();
            if (!isItemSelled(player, itemID, null)) {
                int size = player.iItems.size() - 1;

                for (int i = size; i >= 0; i--) {
                    Item item = (Item) player.iItems.get(i);
                    if (item.id == itemID) {
                        if (item.isSelling) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang được bán trong gian hàng", ""));
                            return;
                        }

                        if (item.lock != 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể bỏ vật phẩm đã khoá ra đất", ""));
                            return;
                        }

                        if (item.dbid != 0 || item.dbowner != 0) {
                            try {
                                Database.instance.deleteItem(item.dbid);
                            } catch (Exception var8) {
                            }
                        }

                        item.owner = 0;
                        item.dbowner = 0;
                        if (player.iItems.remove(item)) {
                            // 16/09/2026: them dbid (theo yeu cau "log chi tiet hon").
                            String sub = "dbid" + item.dbid + " " + item.getTemplate().name + "_" + item.plus + "|" + item.getAttribute() + "|" + item.getDBInfo();
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            player.sendMessage(MessageCreator.createServerAlertMessage("Đã huỷ vật phẩm khi bỏ ra đất", ""));
                            Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "Bỏ " + sub + " ra đất", "delI");
                            return;
                        }

                        return;
                    }
                }
            }
        }
    }

    public void doSellGemItem(Char player, Message messge) {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else if (player.checkExistItem(ID_THE_MUA_BAN)) {
            if (!this.checkTrade(player)) {
                try {
                    short id = messge.dis.readShort();
                    byte typeMaterial = messge.dis.readByte();

                    for (int i = 0; i < player.gemItem.size(); i++) {
                        GemItem gem = (GemItem) player.gemItem.elementAt(i);
                        if (gem.realId == id
                                && (typeMaterial == 0
                                        ? player.listGemitem[gem.idGemTemplate] - player.listGemitemSell[gem.idGemTemplate] > 0
                                        : player.listGemitemLock[gem.idGemTemplate] > 0)) {
                            int n = player.listGemitem[gem.idGemTemplate] - player.listGemitemSell[gem.idGemTemplate];
                            if (typeMaterial == 1) {
                                n = player.listGemitemLock[gem.idGemTemplate];
                            }

                            long price = (long) (gem.getTemplate().getPrice(isSale) / 5 * n);
                            if (n <= 0) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể bán", ""));
                                return;
                            }

                            if (typeMaterial == 0) {
                                player.listGemitem[gem.idGemTemplate] = player.listGemitem[gem.idGemTemplate] - n;
                                player.allGemUse[gem.idGemTemplate] = player.allGemUse[gem.idGemTemplate] + n;
                            } else {
                                player.listGemitemLock[gem.idGemTemplate] = player.listGemitemLock[gem.idGemTemplate] - n;
                                player.allGemUseLock[gem.idGemTemplate] = player.allGemUseLock[gem.idGemTemplate] + n;
                            }

                            if (player.listGemitem[gem.idGemTemplate] <= 0 && player.listGemitemLock[gem.idGemTemplate] <= 0) {
                                player.gemItem.remove(gem);
                                player.removeIDItem(gem.realId);
                            }

                            if (n > 0) {
                                player.addXu(price, "map 82");
                                player.sendMessage(MessageCreator.createServerAlertMessage("Đã bán thành công", ""));
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                player.sendMessage(MessageCreator.createCharGemItem(player));
                                Database.instance
                                        .saveLogItemSellShop(
                                                player.charname,
                                                player.charDBID,
                                                player.lvDetail.lv
                                                + " GEM | "
                                                + gem.getTemplate().name
                                                + " | "
                                                + gem.getTemplate().getPrice(isSale) / 5
                                                + "|"
                                                + n
                                                + "_"
                                                + typeMaterial
                                        );
                            }
                        }
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                }
            }
        }
    }

    public void doSellItem(Char player, Message message) throws IOException {
    }



    public static boolean isItemSelled(Char player, int itemID, Item item) {
        try {
            if (item.isSelling || item.prizeSell > 0) {
                return true;
            }

            for (int j = 0; j < npcSell[player.myCountry].length; j++) {
                for (int k = 0; k < npcSell[player.myCountry][j].shop.length; k++) {
                    if (npcSell[player.myCountry][j].shop[k].get1ItemOfCharSell(player.id, (short) itemID) != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang được bán trong gian hàng", ""));
                        System.out.print("DANG BAN TRONG SHOP");
                        return true;
                    }
                }
            }

            if (item != null) {
                if (item.isSelling) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang được bán trong gian hàng", ""));
                    return true;
                }
            } else {
                for (int i = 0; i < player.iItems.size(); i++) {
                    Item it = (Item) player.iItems.get(i);
                    if (it.id == itemID && it.isSelling) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang được bán trong gian hàng", ""));
                        return true;
                    }
                }
            }
        } catch (Exception var5) {
        }

        return false;
    }

    public boolean checkTrade(Char player) {
        try {
            if (player.hp <= 0) {
                return true;
            }

            if (player.userTrade.size() > 0) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi đang trao đổi", ""));
                // Huy cho CA HAI ben va xoa sach trang thai duoi Char.KHOA_GIAO_DICH. Ban cu chi
                // xoa tItems/rItems/userTrade ma de lai tPotions/rPotions -> lan giao dich sau
                // nhan them thuoc tu khong khi. Xem Char.datLaiTrangThaiGiaoDich, real/GiaoDichThuoc.
                player.huyGiaoDich(false, true);
                return true;
            }
        } catch (Exception var3) {
        }

        return false;
    }

    public static boolean isItemCanImbue(int type) {
        return isArmor(type) || isWearingAnimal(type) || isWeapone(type) ;
    }

    private static boolean isArmor(int type) {
        return (type < 3 || type > 7) && type != 13;
    }

    public static boolean isWearingAnimal(int type) {
        return type >= 14 && type <= 18;
    }

    public static boolean isItemChar(int type) {
        return type < 13;
    }

    public static boolean isItemVantieu(int type) {
        return type == 20;
    }

    public static boolean isItemTangTocVanTieu(int type) {
        return type == 23;
    }

    public static boolean isTheBaiLinhDanhThue(int type) {
        return type == 21;
    }

    public static boolean isTheBaiLinhLanSu(int type, int id) {
        return type == 21 && id == 883;
    }



    public static boolean isTheBaiMuaban(int type) {
        return type == 22;
    }

    void doUseItem(Char player, Message message) throws IOException 
    {
        if (!this.checkTrade(player)) 
        {
            short itemID = message.dis.readShort();
            boolean isItemAnimal = false;

            // Bam vao mon DANG MAC trong bang "Trang bi thu" = thao no ra.
            //
            // Game goc khong co duong nao thao trang bi thu ca: client khong gui gi khi
            // bam vao mon dang mac, va vong lap duoi day chi tim trong hanh trang
            // (player.iItems) nen co gui cung khong thay. Cach duy nhat la mac mon khac
            // de len. Voi skin linh thu thi dieu do nghia la mac vao roi khong bao gio
            // tro lai hinh thu that duoc, nen truoc day phai lam mot lenh chat rieng.
            //
            // Nay client co gui goi 29 voi id cua mon dang mac (xem paintAnimalWearing
            // ben ClientUnity), va cho nay xu ly. Dung cho MOI trang bi thu chu khong
            // rieng skin - lo hong nay von co san cho ca nam loai kia.
            if (thaoTrangBiThu(player, itemID)) {
                return;
            }

            for (int i = 0; i < player.iItems.size(); i++)
            {
                Item item = (Item) player.iItems.elementAt(i);
                if (item.id == itemID) {
                    if (isItemSelled(player, item.id, item)) 
                    {
                        return;
                    }

                    int t = item.getTemplate().type;
                    // Vat pham skin linh thu mang type 30 nen isWearingAnimal (14..18)
                    // khong nhan ra. Coi no la trang bi thu ngay tu day de bo qua cac
                    // kiem tra danh cho do nhan vat (dung mon phai, dung gioi tinh,
                    // isArmor...) va de gui lai goi thong tin thu cuoi o duoi.
                    isItemAnimal = isWearingAnimal(item.getTemplate().type)
                            || SkinLinhThu.laItemSkin(item.getTemplate().id);
                    if (t == 24) {
                        return;
                    }

                    if (item.isManhSuuTap()) 
                    {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng: " + item.otherAtt[65], ""));
                        return;
                    }
                    if(item.isTBSuuTap()){
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng tìm thợ rèn bí ẩn để xem bộ sưu tập", ""));
                        return;
                    }

                    if(item.isCaiTrangSuuTap()){
                        player.sendMessage(MessageCreator.createServerAlertMessage("Vui lòng tìm thợ rèn bí ẩn để sử dụng cải trang", ""));
                        return;
                    }
                    if (item.isThanKhi())
                    {
                        player.iItems.remove(item);
                        
                        if (player.itemThanKhi != null) 
                        {
                            player.iItems.add(player.itemThanKhi);
                        }
                        player.itemThanKhi = item;
                        
                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        player.sendMessage(MessageCreator.createCharWeaponeData(1, item.getTemplate().type, item.getTemplate().style, item.getTemplate().color));
                        Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "item than khi");
                        return;
                    }
                 

                    if (item.isRuongNguyenLieu()) 
                    {
                        if (item.isHetHan()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Rương đã hết hạn", ""));
                            player.iItems.remove(item);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.deleteItem(item.dbid);
                            // 16/09/2026: them ten + dbid (theo yeu cau "log chi tiet hon").
                            Database.instance.saveOrtherLog("tob_log_other_item", player.charname,
                                    "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                        } else if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                        } else {
                            int index = r.nextInt(5);
                            int[] sl4 = new int[]{1, 2, 5, 15, 40, 100};
                            int sl = Char.getIndexRandom(sl4);
                            int sl5 = 5 - sl;
                            String info = "Nhận được ";
                            if (item.tempateID == 752) {
                                if (sl > 0) {
                                    player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[3][index], sl, true);
                                    info = info + sl + " " + gemTemplate[GemTemplate.ID_MATERIAL_HIGHT[3][index]].name + " khoá. ";
                                }

                                if (sl5 > 0) {
                                    player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[4][index], sl5, true);
                                    info = info + sl5 + " " + gemTemplate[GemTemplate.ID_MATERIAL_HIGHT[4][index]].name + " khoá";
                                }
                            } else if (item.tempateID == 753) {
                                if (sl > 0) {
                                    player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[3][index], sl, true);
                                    info = info + sl + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[3][index]].name + " khoá. ";
                                }

                                if (sl5 > 0) {
                                    player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[4][index], sl5, true);
                                    info = info + sl5 + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[4][index]].name + " khoá. ";
                                }
                            }

                            player.iItems.remove(item);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                            Database.instance.saveOrtherLog("", player.charname, "mo " + item.getName() + " " + info, "ruongnl");
                        }

                        return;
                    }

                    if (item.isRuongMayMan()) 
                    {
                        if (item.isHetHan()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Rương đã hết hạn", ""));
                            player.iItems.remove(item);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.deleteItem(item.dbid);
                            // 16/09/2026: them ten + dbid (theo yeu cau "log chi tiet hon").
                            Database.instance.saveOrtherLog("tob_log_other_item", player.charname,
                                    "dbid" + item.dbid + " " + item.getTemplate().name + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                        } else if (player.isFullInventory()) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                        } else {
                            doOpenRuongMayMan(player, item);
                            player.iItems.remove(item);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            player.sendMessage(MessageCreator.createCharGemItem(player));
                        }

                        return;
                    }

                    if (item.isHuyetLinhThao()) 
                    {
                        if (!player.petUsing.checkCanTienHoa()) {
                            if (player.petUsing.itemPet != null && player.petUsing.itemPet.level < 2) {
                                player.sendMessage(
                                        MessageCreator.createServerAlertMessage(
                                                "Thú nuôi của bạn chưa đủ điều kiện tiến hoá. Hãy luyện thú đến cấp độ "
                                                + (player.petUsing.itemPet.level == 0 ? "40" : "49,99%")
                                                + " để có thể tiến hoá",
                                                ""
                                        )
                                );
                            } else {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thú nuôi của bạn không thể tiến hoá", ""));
                            }
                        } else if (player.petUsing.doTienHoa(player)) {
                            player.iItems.remove(item);
                            player.removeIDItem(item.id);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "huyetlinhthao");
                        }

                        return;
                    }

                    if (item.isHuyetBoDe()) 
                    {
                        if (!player.petUsing.checkCanChangeThongThao()) {
                            if (player.petUsing.itemPet != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thú nuôi của bạn chưa đủ điều kiện để sử dụng.", ""));
                            } else {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Thú nuôi của bạn không thể sử dụng vật phẩm này", ""));
                            }
                        } else {
                            // TRU VAT PHAM TRUOC. Truoc day tru sau, va do la mot
                            // lo hong that: doChangeThongThao DA doi pham chat cua
                            // thu nuoi o nhung dong dau, roi moi nem ngoai le o dong
                            // in thong bao (NAME_PHAM_CHAT[-1], vi phamchat = -1 la
                            // gia tri MAC DINH cua thu nuoi chua tung doi pham chat).
                            // Ngoai le lam ba dong duoi day khong chay -> Huyet Bo De
                            // KHONG bi tru, nhung pham chat thi da doi roi.
                            // Ngoai le do co that trong errorlog.txt cua VPS.
                            player.iItems.remove(item);
                            player.removeIDItem(item.id);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            player.petUsing.doChangeThongThao(player);
                        }

                        return;
                    }

                    if (isItemVantieu(t)) 
                    {
                        try {
                            player.iItems.remove(item);
                            player.removeIDItem(item.id);
                            player.doResponseTieu(player, true, item.typetieu, item.level);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "itemvantieu");
                        } catch (Exception var14) {
                            var14.printStackTrace();
                        }

                        return;
                    }

                    if (item.istrungPet()) 
                    {
                        try {
                            player.doCreatePet(item);
                            Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "itemtrungpet");
                        } catch (Exception var15) {
                            var15.printStackTrace();
                        }

                        return;
                    }

                    if (isItemThanThu(t))
                    {
                        // Da toi cap toi da (idItemUpLevel == -1) thi an "Nâng cấp" di (bam vao
                        // tu no khong lam gi - Char.doNangCapHoaKyLan tu kiem dieu kien nay ben
                        // trong - de menu con 2 muc cho ro rang thay vi mot nut vo tac dung).
                        // MapDoMenu.java (case -513) doc lai dung idItemUpLevel nay de biet
                        // "Ảo hóa" nam o idOptionMenu 1 hay 2 tuong ung.
                        player.itemTam = item;
                        // VIET THUONG "ảo hóa", KHONG viet hoa chu "Ả" — font cua ban client
                        // JAR KHONG CO chu HOA co dau thuoc khoi Unicode U+1EA0..U+1EF8, ve ra
                        // la MAT HAN chu do: nguoi choi thay muc menu la "o hóa" (bao 05/09/2026).
                        // Chu thuong cung khoi thi ve binh thuong — chinh muc "Sử dụng" ngay ben
                        // canh co "ử" (U+1EED) va "ụ" (U+1EE5) van hien du.
                        // Cac chuoi khac con dinh loi nay: xem MapChat "BẬT"/"TẮT",
                        // MapXuLyLenh "TOP ĐẠI GIA"/"TOP CAO THỦ"/"TOP BANG HỘI", Pet "ĐÃ HẾT HẠN".
                        final String[] menuHoaKyLan = item.getTemplate().idItemUpLevel > -1
                                ? new String[]{"Sử dụng", "Nâng cấp", "ảo hóa"}
                                : new String[]{"Sử dụng", "ảo hóa"};
                        player.sendMessage(MessageCreator.createMsgMenu(menuHoaKyLan, -513, 0));
                        return;
                    }

                    if (item.isVukhiThoiTrang()) 
                    {
                        player.iItems.remove(item);
                        if (player.itemVukhiThoiTrang != null) {
                            player.iItems.add(player.itemVukhiThoiTrang);
                        }

                        player.itemVukhiThoiTrang = item;
                        if (player.itemVukhiThoiTrang.isChoiLuaLanBienHinh() && player.wModel.isChoi()) {
                            player.wModel.unWearChoi(player);
                        }

                        player.calculateAttrib();
                        player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
                        if (item.getTemplate().type > 2 && item.getTemplate().type < 8) {
                            player.sendMessage(MessageCreator.createCharWeaponeData(1, item.getTemplate().type, item.getTemplate().style, item.getTemplate().color));
                        }

                        player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                        Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "vukhithoitrang");
                        return;
                    }

                    if (isItemTangTocVanTieu(t)) 
                    {
                        if (player.monster != null) {
                            player.iItems.remove(item);
                            player.removeIDItem(item.id);
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                            Database.instance.saveOrtherLog("", player.charname, item.getInfoSave(), "itemvantieu");
                            if (item.getTemplate().id == 676) {
                                player.upSpeedNguaVanTieu = 2;
                            } else if (item.getTemplate().id == 677) {
                                player.upSpeedNguaVanTieu = 3;
                            }
                        }

                        return;
                    }

                    // Trung rong bang: bam Dung KHONG no ra thu - phai mang toi NPC tao thu,
                    // giong moi loai trung linh thu khac. Bao dung cau "Khong the su dung" nhu
                    // cac trung dang potion (doUsePotion case 70/72/74/115) cho nhat quan.
                    // Van phai chan o day, TRUOC nhanh ho ve lenh: template 887 la type 21 nen
                    // neu de roi xuong duoi thi no mo menu trieu hoi ho ve danh thue.
                    if (item.isTrungRongBang()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng", ""));
                        return;
                    }

                    if (isTheBaiLinhDanhThue(t)) {
                        try {
                            if (player.lvDetail.lv < item.level) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không đủ cấp độ để sử dụng vật phẩm này", ""));
                                return;
                            }

                            player.itemLinhDanhThue = item;
                            this.doUseItemLinhDanhThue(player);
                        } catch (Exception var16) {
                        }

                        return;
                    }

                   
                    if (isTheBaiMuaban(t)) {
                        if (this.isvanTienTran()) {
                            return;
                        }

                        if (!player.canUseTheMuaBan()) {
                            long time = (System.currentTimeMillis() - player.timeUseTheMuaBan) / 1000L;
                            if (time < 0L) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Còn " + abs((int) time) + "s nữa bạn mới có thể sử dụng vật phẩm này", ""));
                            }

                            return;
                        }

                        if (player.monster != null) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng khi đang vận tiêu", ""));
                            return;
                        }

                        if (this.isMapOffline) {
                            return;
                        }

                        if (getTown[player.inCountry]) {
                            return;
                        }

                        player.timeUseTheMuaBan = System.currentTimeMillis() + 300000L;
                        player.mapID_the_mua_ban = player.mapID;
                        player.region_the_mua_ban = (short) player.region;
                        player.x_the_mua_ban = (byte) (player.x / 16);
                        player.y_the_mua_ban = (byte) (player.y / 16);
                        this.move2Map(player, 5, 5, 103, player.inCountry);
                        return;
                    }

                    if (item.isChoiLuaLanBienHinh()) {
                        MapMuaBan.exchangeItem(this, player, item, i);
                    } else {
                        boolean vk = false;
                        if (t == 6 || t == 7 || t == 4 || t == 5 || t == 3) {
                            vk = true;
                        }

                        if (item.getTemplate().clazz != player.charClass && vk && !isItemAnimal && !player.isAdmin) {
                            return;
                        }

                        if (item.clazz != player.charClass
                                && !vk
                                && isArmor(t)
                                && !isModelClothes(item.getTemplate().atb[9])
                                && !isItemAnimal
                                && item.getTemplate().type != 19
                                && !player.isAdmin) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mặc đồ của " + CharManager.NAME_CLASS[item.clazz], ""));
                            return;
                        }

                        if (item.level > player.lvDetail.lv) {
                            System.out.println("khong du cap do su dung");
                            return;
                        }

                        if (item.getTemplate().type == 13 && !isModelClothes(item.getTemplate().atb[9]) && !player.isAdmin) {
                            if (player.itemAx != null) {
                                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng cùng lúc 2 loại cuốc", ""));
                                return;
                            }

                            player.itemAx = item;
                        }

                        if (item.getTemplate().gender != 0 && player.gender != 0 && !player.isAdmin && item.getTemplate().gender != player.gender) {
                            String[] gender = new String[]{"", "nam", "nữ"};
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không thể mặc đồ của " + gender[item.getTemplate().gender], ""));
                            return;
                        }

                        try {
                            boolean fullRing = player.fullRing();
                            if (item.getTemplate().type == 8 && fullRing) {
                                player.itemTemp = itemID;
                                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nhẫn trên", "Nhẫn dưới"}, -507, 0));
                                return;
                            }

                            MapMuaBan.exchangeItem(this, player, item, i);
                        } catch (Exception var17) {
                            var17.printStackTrace();
                        }
                    }

                    // Vua doi trang bi thu: cap nhat skin dang mang TRUOC khi dung goi
                    // tin, vi ca ba goi ben duoi deu mang hinh con thu.
                    final boolean doiSkin = isItemAnimal && SkinLinhThu.dongBoTheoTrangBi(player);
                    // TINH LAI CHI SO PHAI NAM SAU dongBoTheoTrangBi, KHONG duoc truoc.
                    //
                    // LOI DA XAY RA THAT (05/09/2026, nguoi choi bao "mac skin thi bang Tiem
                    // nang tang ma Tan cong dung im"): day la duong MAC/THAO skin o bang
                    // "Trang bi thu". Truoc day calculateAttrib() chay o TREN dong dong bo,
                    // tuc no tinh bang idSkinLinhThu CU (chua co skin khi vua mac vao, hoac
                    // van con skin khi vua thao ra).
                    //
                    // Hau qua chi lo ra mot nua: attack/maxhp/defend/accurate la TRUONG DUOC
                    // LUU SAN, chi doi khi calculateAttrib() chay - nen chung giu so cu. Con
                    // bang diem tiem nang thi MessageCreator cong p.tnSucManh()/tnKheoLeo()
                    // TAI CHO luc dung goi tin, doc idSkinLinhThu vua moi cap nhat - nen no
                    // tang ngay. Nguoi choi thay tiem nang nhay len ma tan cong dung im.
                    //
                    // Ba duong kia (dongBoVaGui, doRideAnimal, SelectCharHandler) da dat dung
                    // thu tu tu truoc; rieng duong nay bi sot. Xem kiemTinhLaiChiSo() trong
                    // test/KiemSkinLinhThu.java - da them phep kiem cho ca bon duong.
                    player.calculateAttrib();
                    Message m = MessageCreator.createMainCharInfoMessage(player);
                    player.sendMessage(m);
                    m = MessageCreator.createCharWearingMessage(player, player);
                    player.sendMessage(m);
                    if (isItemAnimal) {
                        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
                        if (doiSkin) {
                            // KHONG gui createAnimalWearingMessage cho nguoi xung quanh.
                            // Goi do ben client chay vao GameScr.onAnimalWearingInfo, va
                            // khi charID khac nhan vat cua ho thi no goi onViewAnimalInfo
                            // -> WindowInfoScr.switchToMe() + Canvas.endDlg(): DOI HAN
                            // man hinh cua ho sang bang "Trang bi thu" cua MINH, cat
                            // ngang viec ho dang lam. Doi skin qua lai se thanh cong cu
                            // pha nguoi khac.
                            // Ho cung khong can goi do: du lieu duy nhat no mang la
                            // imgAnimal, ma imgAnimal chi duoc ve trong cua so thong tin
                            // chu khong dung de ve con thu tren ban do. Hinh tren ban do
                            // di bang createCharInfo, da gui vo dieu kien o cuoi ham.
                            player.sendMessage(MessageCreator.createServerAlertMessage(
                                    SkinLinhThu.thongBaoDoiSkin(player), ""));
                        }
                    }

                    m = MessageCreator.createCharInventoryMessage(player, 1);
                    player.sendMessage(m);
                    m.cleanup();
                    if (item.getTemplate().type > 2 && item.getTemplate().type < 8) {
                        player.sendMessage(MessageCreator.createCharWeaponeData(1, item.getTemplate().type, item.getTemplate().style, item.getTemplate().color));
                    }

                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                    MessageCreator.createMsgCharMonster(player, player);
                    return;
                }
            }
        }
    }

    /**
     * Thao mot mon trang bi thu dang mac, tra ve hanh trang.
     *
     * Tra ve true neu itemID dung la mot mon dang mac (da xu ly xong, nguoi goi thoat),
     * false neu khong phai - luc do doUseItem chay tiep nhu cu de tim trong hanh trang.
     *
     * Cach go giong het doan go mon cu trong exchangeItemAnimal: place ve 0, idAnimal ve
     * -1. Giu giong nhau la co y - hai duong cung dan den mot trang thai, lech nhau la
     * sinh ra mon "nua mac nua khong".
     */
    boolean thaoTrangBiThu(Char player, short itemID) {
        if (player.awItems == null) {
            return false;
        }
        Item mon = null;
        for (int i = 0; i < player.awItems.size(); i++) {
            final Item it = (Item) player.awItems.elementAt(i);
            if (it != null && it.id == itemID) {
                mon = it;
                break;
            }
        }
        if (mon == null) {
            return false;
        }
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Hành trang đầy, không tháo ra được.", ""));
            return true;
        }
        final String sub = mon.getTemplate().name + "_" + mon.plus + "|" + mon.getAttribute()
                + "|" + mon.getDBInfo();
        player.awItems.remove(mon);
        player.iItems.add(mon);
        mon.place = 0;
        mon.idAnimal = -1;
        Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "Thao " + sub, "change");

        // Gui lai dung nhung goi ma duong MAC gui, khong thi bang van hien mon vua thao.
        player.sendMessage(MessageCreator.createAnimalWearingMessage(player, player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        SkinLinhThu.dongBoVaGui(player);
        return true;
    }

    void exchangeItemAnimal(Char player, Item iItem, int slot) {
        String sub = iItem.getTemplate().name + "_" + iItem.plus + "|" + iItem.getAttribute() + "|" + iItem.getDBInfo();
        int i = 0;

        while (i < player.awItems.size()) {
            Item wItem = (Item) player.awItems.elementAt(i);
            i++;
            // Vat pham skin gan theo NHAN VAT nen khong so idAnimal khi go mon cu ra.
            // Trang bi thu that thi van so nhu cu (moi con thu mot bo rieng).
            //
            // Neu so ca idAnimal cho skin thi khi mon skin cu con dinh vao con thu truoc
            // do, no khong bi go ra: nguoi choi mac skin moi ma khong co gi xay ra, cung
            // khong co thong bao nao. Hien tai chua xay ra duoc vi moi luong doi thu deu
            // goi createAnimalWearingMessage ngay sau do, ma ham do goi getAnimalWearing
            // — cho buoc lai idAnimal. Nhung do la mot rang buoc rat mong, chi can them
            // mot luong doi thu quen gui goi do la loi bat ra.
            final boolean cungLoai = wItem.getTemplate().type == iItem.getTemplate().type;
            final boolean laSkin = SkinLinhThu.laItemSkin(iItem.getTemplate().id);
            if (cungLoai && (laSkin ? SkinLinhThu.laItemSkin(wItem.getTemplate().id)
                                    : wItem.idAnimal == player.animalRide.dbid)) {
                player.awItems.remove(wItem);
                player.iItems.add(wItem);
                wItem.place = 0;
                i = 0;
                sub = sub + " >>> " + wItem.getTemplate().name + "_" + wItem.plus + "|" + wItem.getAttribute() + "|" + wItem.getDBInfo();
                wItem.idAnimal = -1;
            }
        }

        iItem.place = 5;
        iItem.idAnimal = player.animalRide.dbid;
        player.awItems.add(iItem);
        player.iItems.remove(iItem);
        Database.instance.saveOrtherLog("tob_log_other_item", player.charname, "Sử dụng " + sub, "change");
    }



    public void doAddBossTruRong(int wave, int country, int pos) {
    }

    public static String getDotNumber(long m) {
        String str = "" + m;
        int len = str.length() / 3;
        if (str.length() % 3 == 0) {
            len--;
        }

        for (int i = 0; i < len; i++) {
            int index = str.length() - (i + 1) * 3 - i;
            str = str.substring(0, index) + "." + str.substring(index, str.length());
        }

        return str;
    }

    void startFight() {
        String day = "abc";

        for (int i = 0; i < 3; i++) {
            if (!this.curday[i].equals("abc")) {
                String nt = new Date(System.currentTimeMillis()).toString();
                boolean isDay = true;
                if (isDay) {
                    Calendar cl = Calendar.getInstance();
                    int iHour = cl.get(11);
                    int iMinute = cl.get(12);
                    System.out.println("BAT DAU CHIEM THANH");

                    this.curday[i] = day;
                    getTown[i] = true;
                    if (idClanTown[i] > -1) {
                        try {
                            NewClan.updateTown(idClanTown[i], 0, i);
                        } catch (Exception var13) {
                        }
                    }

                    ((Vector) npcReceiveCard.get(i)).removeAllElements();
                    ((Vector) npcReceiveCard.get(i)).add(new NpcReceiveCard(0, i));
                    ((Vector) npcReceiveCard.get(i)).add(new NpcReceiveCard(1, i));
                    ((Vector) npcReceiveCard.get(i)).add(new NpcReceiveCard(2, i));
                    idClanTown[i] = -1;
                    taxOfClan[i] = 0;
                    ((Monster) MapTown.congthanh.get(i)).initInfo();
                    ((Hashtable) ((Map) RealController.mapList.get(201)).monsters.get(i))
                            .put(((Monster) MapTown.congthanh.get(i)).id, (Monster) MapTown.congthanh.get(i));
                    int[] mapstart = new int[]{9, 481, 482, 483, 484};
                    int homeX = 31 + Database.r.nextInt() % 5;
                    int homeY = 79 + Database.r.nextInt(20);

                    for (int j = 0; j < CharManager.instance.vChars.size(); j++) {
                        try {
                            Char p = (Char) CharManager.instance.vChars.elementAt(j);
                            p.sendMessage(MessageCreator.createMsgStartGetTown(p.inCountry));
                            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian chiếm thành bắt đầu"));
                            if (p.isBot == -1 && p.mapID >= 201 && p.mapID <= 271 && p.mapID != 202) {
                                p.map.move2Map(p, homeX, homeY, mapstart[r.nextInt(MessageCreator.nclone)], p.inCountry);
                            }
                        } catch (Exception var14) {
                        }
                    }

                    ((Map) RealController.mapList.get(201)).doAddBossTruRong(1, i, 0);
                    ((Map) RealController.mapList.get(201)).doAddBossTruRong(1, i, 1);
                    ((Map) RealController.mapList.get(201)).doAddBossTruRong(1, i, 2);
                }
            }
        }
    }

    void startFight(int country) {
        String day = "abc";
        int i = country;
        if (!this.curday[country].equals("abc")) {
            String nt = new Date(System.currentTimeMillis()).toString();
            boolean isDay = true;
            if (isDay) {
                Calendar cl = Calendar.getInstance();
                int iHour = cl.get(11);
                int iMinute = cl.get(12);
                System.out.println("BAT DAU CHIEM THANH");
             
                this.curday[country] = day;
                getTown[country] = true;
                if (idClanTown[country] > -1) {
                    try {
                        NewClan.updateTown(idClanTown[i], 0, i);
                    } catch (Exception var15) {
                    }
                }

                ((Vector) npcReceiveCard.get(country)).removeAllElements();
                ((Vector) npcReceiveCard.get(country)).add(new NpcReceiveCard(0, country));
                ((Vector) npcReceiveCard.get(country)).add(new NpcReceiveCard(1, country));
                ((Vector) npcReceiveCard.get(country)).add(new NpcReceiveCard(2, country));
                idClanTown[country] = -1;
                taxOfClan[country] = 0;
                ((Monster) MapTown.congthanh.get(country)).initInfo();
                ((Hashtable) ((Map) RealController.mapList.get(201)).monsters.get(country))
                        .put(((Monster) MapTown.congthanh.get(country)).id, (Monster) MapTown.congthanh.get(country));
                int[] mapstart = new int[]{9, 481, 482, 483, 484};
                int homeX = 31 + Database.r.nextInt() % 5;
                int homeY = 79 + Database.r.nextInt(20);

                for (int j = 0; j < CharManager.instance.vChars.size(); j++) {
                    try {
                        Char p = (Char) CharManager.instance.vChars.elementAt(j);
                        p.sendMessage(MessageCreator.createMsgStartGetTown(p.inCountry));
                        p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian chiếm thành bắt đầu"));
                        if (p.isBot == -1 && p.mapID >= 201 && p.mapID <= 271 && p.mapID != 202) {
                            this.move2Map(p, homeX, homeY, mapstart[r.nextInt(MessageCreator.nclone)], p.inCountry);
                        }
                    } catch (Exception var14) {
                    }
                }
            }
        }
    }

    public static String checkBadWord(String info) {
        for (int i = 0; i < Database.badWord.size(); i++) {
            String st = ((String) Database.badWord.get(i)).toLowerCase();
            if (info.toLowerCase().indexOf((String) Database.badWord.get(i)) != -1) {
                info = info.replaceAll(st, "*");
            }
        }

        return info;
    }

    public void doAddDragonTower(int country) {
    }

    public void doAddLienHoaTru(int pos, int idClan, int myCountry) {
    }

    void startWar() {
        Calendar cl = Calendar.getInstance();
        System.out.println("CHUAN BI QUOC CHIEN");
        String info = "";

        try {
            Map m = (Map) RealController.mapList.get(201);

            for (int i = 0; i < 3; i++) {
                if (!nwar[i] && nationBeAttack[i] != -1) {
                    nwar[i] = true;
                    info = info + nameCountry[i] + ",";
                    m.doAddDragonTower(i);

                    for (int k = 0; k < 4; k++) {
                        for (int j = 0; j < 4; j++) {
                            m.doAddLienHoaTru(k, idClanTown[i], i);
                        }
                    }
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        if (!info.equals("")) {
            info = "Bắt đầu diễn ra sự kiện chiếm lãnh thổ tại " + info.substring(0, info.length() - 1);

            try {
                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(info));
            } catch (IOException var7) {
            }
        }
    }

    public static void addWeddingToPrivateMap() {
        for (Wedding wd : allWedding.values()) {
            PrivateMap m1 = (PrivateMap) RealController.mapList.get(225);
            PrivateMap m2 = (PrivateMap) RealController.mapList.get(226);
            m1.doAddWedding(wd);
            m2.doAddWedding(wd);
        }
    }


    public void startWedding(int idWedding) {
        try {
            Char player = null;
            Wedding wd = (Wedding) allWedding.get(idWedding);
            if (wd != null && wd.action == 0) {
                if (wd.listMember.get(wd.idP1) != null && wd.listMember.get(wd.idP2) != null) {
                    Char p1 = CharManager.instance.getByUserID((Integer) wd.listMember.get(wd.idP1));
                    Char p2 = CharManager.instance.getByUserID((Integer) wd.listMember.get(wd.idP2));
                    if (p1 != null && p2 != null) {
                        Map map = (Map) RealController.mapList.get(wd.typeParty > 0 ? 226 : 225);
                        if (p1.nameHusband_wife.equals("")) {
                            p1.nameHusband_wife = p2.charname;
                        }

                        if (p2.nameHusband_wife.equals("")) {
                            p2.nameHusband_wife = p1.charname;
                        }

                        map.doStart(p1, wd.typeParty);
                        wd.action = 1;

                        for (int userid : wd.listMember.values()) {
                            player = CharManager.instance.getByUserID(userid);
                            if (player != null) {
                                player.createIdGiftWedding(wd.typeParty);
                                this.move2Map(player, 16, 24, wd.typeParty > 0 ? 226 : 225, player.myCountry);
                                player.potions[92] = 0;
                                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            }
                        }

                        Database.instance.saveOrtherLog("", p1.charname, "bat dau tiec cuoi " + p1.charname + "_" + p2.charname, "startWD");
                    } else {
                        System.out.println("KHOGN DUA VAO DC");
                    }
                } else {
                    System.err.println("KHOGN TIM TIM CO DAU CHU RE");
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    void startWedding() {
        String day = Char.getDayOpen(0L);
        int hour = Calendar.getInstance().get(11);

        try {
            System.out.println("BAT DAU TIEC CUOI");

            for (Wedding wd : allWedding.values()) {
                Char player = null;
                if (wd.action == 0) {
                    if (wd.listMember.get(wd.idP1) != null && wd.listMember.get(wd.idP2) != null) {
                        Char p1 = CharManager.instance.getByUserID((Integer) wd.listMember.get(wd.idP1));
                        Char p2 = CharManager.instance.getByUserID((Integer) wd.listMember.get(wd.idP2));
                        if (p1 != null && p2 != null) {
                            Map map = (Map) RealController.mapList.get(wd.typeParty > 0 ? 226 : 225);
                            if (p1.nameHusband_wife.equals("")) {
                                p1.nameHusband_wife = p2.charname;
                            }

                            if (p2.nameHusband_wife.equals("")) {
                                p2.nameHusband_wife = p1.charname;
                            }

                            map.doStart(p1, wd.typeParty);
                            wd.action = 1;

                            for (int userid : wd.listMember.values()) {
                                player = CharManager.instance.getByUserID(userid);
                                if (player != null) {
                                    player.createIdGiftWedding(wd.typeParty);
                                    this.move2Map(player, 16, 24, wd.typeParty > 0 ? 226 : 225, player.myCountry);
                                    player.potions[92] = 0;
                                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                                }
                            }
                        } else {
                            System.out.println("KHOGN DUA VAO DC");
                        }
                    } else {
                        System.err.println("KHOGN TIM TIM CO DAU CHU RE");
                    }
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    public void setDieAll(Char player) {
    }

    public static void doAddItemIsNotWearing(Char p, int iditem, int minute) {
        int clazz = 0;
        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(clazz)).get(new Integer(iditem));
        Item newItem = null;
        newItem = new Item(template, false, clazz, clazz, template.id);
        newItem.id = p.getIDItem();
        newItem.owner = p.charDBID;
        newItem.level = newItem.getTemplate().level;
        p.iItems.add(newItem);
        newItem.identify = p.charDBID;
        newItem.clazz = (byte) clazz;
        if (minute > 0) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = minute;
        }

        newItem.dateCreate = Char.getDayTime(0L);
        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
        Database.instance.saveOrtherLog("", p.charname, newItem.getName(), "spitem");
    }

    public static boolean isAcountAdmin(String name) {
        if (name == null) {
            return false;
        }
        String n = name.toLowerCase();
        // TEST: cho phép cấu hình tài khoản admin qua sv.adminAccounts trong server.ini
        if (TeamServer.adminAccounts.contains(n)) {
            return true;
        }
        return n.equals("staynight") || n.equals("lamkckc");
    }

    public void doCreateBossCharCopy(int region, int team) {
    }

    // Than method (1497 dong) da chuyen sang real/MapChat.java.
    // Chu ky GIU NGUYEN: bytecode trong lib/NQSH_5h.jar goi method nay,
    // va lop con van override duoc nhu cu.
    public void doChat(Char player, Message message) throws IOException {
        MapChat.doChat(this, player,message);
    }

    /**
     * Chat binh thuong cua nguoi choi (khong xu ly lenh admin).
     * Giu nguyen logic o cuoi doChat: loc tu ngu xau, chong spam, gioi han do dai,
     * chi lv >= 20 moi chat duoc, phat cho nguoi choi o gan.
     */

    public static String replaceExtendDomain(String info) {
        info = info.toLowerCase().replaceAll("\\s+", " ").trim();

        for (int i = 0; i < extend.length; i++) {
            if (info.indexOf(extend[i].toLowerCase()) > -1) {
                info = info.replaceAll(extend[i].toLowerCase(), "");
            }
        }

        return info;
    }

    public static String replaceStringWithSpace(String info) {
        return info.replaceAll(
                "[^a-zA-Z0-9áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴ@._+ -]",
                ""
        );
    }

    public static boolean isModelClothes(int id) {
        return id == 1 || id == 2;
    }

    public void doBuyItem(Char player, Message message) throws IOException {
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        }
    }

    void doSendMonsterTemplate(Char player, int template) {
    }

    public void move2Map(Char player, int x, int y, int mapID, int country) {
        this.checkTrade(player);

        try {
            Map toMap = (Map) RealController.mapList.get(mapID);
            if (getTown[player.inCountry] && giveCardFail(player)) {
                this.doSend2AllChar(MessageCreator.createServerAlertAutoOffMessage(player.charname + " giao thẻ thất bại"), player.inCountry);
                this.sendAllPlayer(MessageCreator.createMsgStartGetTown(player.inCountry), player.inCountry);
            }

            if (player.map != null) {
                this.playerExit(player);
            }

            player.inCountry = (byte) country;
            if (toMap != null) {
                toMap.playerJoin(player);
            } else {
                Map offlineMap = (Map) RealController.mapList.get(-1);
                offlineMap.playerJoin(player);
            }

            if (mapID == 0
                    || mapID == 301
                    || mapID == 302
                    || mapID == 303
                    || mapID == 304
                    || mapID == 70
                    || mapID == 1701
                    || mapID == 1702
                    || mapID == 1703
                    || mapID == 1704) {
                int[][] pos = new int[][]{{10, 23, 14, 38, 30, 35, 21, 49}, {22, 41, 27, 32, 8, 30, 18, 11}, {10, 23, 14, 38, 30, 35, 21, 49}};
                int index = r.nextInt(4);
                x = pos[player.myCountry][index * 2] + r.nextInt() % 3;
                y = pos[player.myCountry][index * 2 + 1] + r.nextInt() % 3;
            }

            player.nearChars.removeAllElements();
            player.nearMons.removeAllElements();
            player.mapID = mapID;
            player.x = x * 16 + 8;
            player.y = y * 16 + 8;
            player.moved = false;
            Message m = MessageCreator.createMapMessage(player);
            player.sendMessage(m);
            player.map.sendInfoNpc(player);
            player.map.sendDynamicEff(player);
            if (player.isNguoiTuyet()) {
                MessageCreator.createMsgCharMonster(player, player);
            }

            m.cleanup();

            try {
                for (int i = 0; i < bossLocation.size(); i++) {
                    if (((BossLocaltion) bossLocation.get(i)).mapID == mapID) {
                        BossLocaltion b = (BossLocaltion) bossLocation.get(i);
                        player.sendMessage(MessageCreator.createGate(b.type, b.x, b.y, b.mapID));
                        break;
                    }
                }

                this.doSendDynamicObj(player);
            } catch (Exception var10) {
                var10.printStackTrace();
                data.Logger.logError(var10);
            }

            if (toMap != null
                    && (toMap.mapIDLoadMap == 80
                    || toMap.mapIDLoadMap == 70
                    || toMap.mapIDLoadMap == 0
                    || toMap.mapIDLoadMap == 1
                    || toMap.mapIDLoadMap == 2
                    || toMap.mapIDLoadMap == 12
                    || toMap.mapIDLoadMap == 106)) {
                player.sendMessage(MessageCreator.createMessageLocation(player.inCountry));
            }

            player.doFinishAutoInbue();
            player.doChangeMapCharHire();
            player.sendMessage(MessageCreator.createCharWearingMessage(player, player));
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    private void sendInfoMap(Char player, int indexXaphu, int mapID) {
        if (!this.checkTrade(player)) {
            try {
                if (this.idXaphu != -1 && player.mapID == this.mapId) {
                    if (player.getxu() < (long) RealController.xaphu[this.idXaphu].price[indexXaphu]) {
                        return;
                    }

                    int toTX = RealController.xaphu[this.idXaphu].x[indexXaphu];
                    int toTY = RealController.xaphu[this.idXaphu].y[indexXaphu];
                    boolean isok = false;
                    int firstID = mapID;

                    for (short i = 0; i < Map.mapID.length; i++) {
                        for (short j = 0; j < Map.mapID[i].length; j++) {
                            if (mapID == Map.mapID[i][j]) {
                                firstID = Map.mapID[i][0];
                                break;
                            }
                        }
                    }

                    for (int i = 0; i < RealController.xaphu[this.idXaphu].mapID.length; i++) {
                        if (firstID == RealController.xaphu[this.idXaphu].mapID[i]) {
                            isok = true;
                            break;
                        }
                    }

                    if (!isok) {
                        return;
                    }

                    Map toMap = (Map) RealController.mapList.get(mapID);
                    if (toMap != null) {
                        if (player.map != null) {
                            this.playerExit(player);
                        }

                        player.subXu((long) RealController.xaphu[this.idXaphu].price[indexXaphu], true, "map 84");
                        toMap.playerJoin(player);
                        player.nearChars.removeAllElements();
                        player.nearMons.removeAllElements();
                        player.mapID = mapID;
                        player.x = toTX * 16 + 8;
                        player.y = toTY * 16 + 8;
                        player.moved = false;
                        Message m = MessageCreator.createMapMessage(player);
                        player.sendMessage(m);
                        m.cleanup();
                        this.sendInfoNpc(player);
                        player.map.sendDynamicEff(player);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        if (player.isNguoiTuyet()) {
                            MessageCreator.createMsgCharMonster(player, player);
                        }

                        player.sendMessage(MessageCreator.createCharWearingMessage(player, player));

                        try {
                            for (int ix = 0; ix < bossLocation.size(); ix++) {
                                if (((BossLocaltion) bossLocation.get(ix)).mapID == mapID) {
                                    BossLocaltion b = (BossLocaltion) bossLocation.get(ix);
                                    player.sendMessage(MessageCreator.createGate(b.type, b.x, b.y, b.mapID));
                                    break;
                                }
                            }

                            this.doSendDynamicObj(player);
                        } catch (Exception var12) {
                            var12.printStackTrace();
                            data.Logger.logError(var12);
                        }
                    }
                }
            } catch (Exception var13) {
                var13.printStackTrace();
                System.out.println("LOI DI XA PHU");
            }
        }
    }

    public void sendInfoNpc(Char player) {
        try {
            if ((player.mapID == 0
                    || player.mapID == 301
                    || player.mapID == 302
                    || player.mapID == 303
                    || player.mapID == 304
                    || player.mapID == 70
                    || player.mapID == 1701
                    || player.mapID == 1702
                    || player.mapID == 1703
                    || player.mapID == 1704
                    || player.mapID == 80
                    || player.mapID == 1901
                    || player.mapID == 1902
                    || player.mapID == 1903
                    || player.mapID == 1904)
                    && !TeamServer.isServerIndo()) {
                player.sendMessage(
                        MessageCreator.createMsgNpc("Thần tài", posNpcThanTai[player.inCountry][0], posNpcThanTai[player.inCountry][1], 28, 31, 2, 14, -24, (byte) 1)
                );
                if (!Char.isFinishAllSuKienHe2017()) {
                    player.sendMessage(
                            MessageCreator.createMsgNpc("Mẹ hiền", posNpcVuLan[player.inCountry][0], posNpcVuLan[player.inCountry][1], 15, 31, 2, 20, -49, (byte) 1)
                    );
                }

                if (!Char.isFinishAllSuKienTrungThul2016()) {
                    // Vi tri rieng theo phe (nameCountry = Thanh long/Hac ho/Huyen vu): Thanh
                    // long doi sang o (22,36), Hac ho + Huyen vu giu nguyen toa do goc
                    // posNpcVuLan (368,608)/(560,704) - xem addNPC.java.
                    int[][] posLongDenKeoQuan = new int[][]{{22 * 16, 36 * 16}, {288, 512}, {560, 704}};
                    player.sendMessage(
                            MessageCreator.createMsgNpc(
                                    "Lồng đèn Lân Sư Tử", posLongDenKeoQuan[player.inCountry][0], posLongDenKeoQuan[player.inCountry][1], 100, 150, 3, 21, -65, (byte) 1
                            )
                    );
                }

                if (!Char.isFinishAllSuKienNoel()) {
                    player.sendMessage(
                            MessageCreator.createMsgNpc(
                                    "Cây thông Noel", posNpcVuLan[player.inCountry][0], posNpcVuLan[player.inCountry][1], 86, 127, 2, 23, -65, (byte) 1
                            )
                    );
                    player.sendMessage(
                            MessageCreator.createMsgNpc("ông Già Noel", posNpcSANTA[player.inCountry][0], posNpcSANTA[player.inCountry][1], 20, 31, 2, 24, -49, (byte) 1)
                    );
                }

                if (!Char.isFinishAllSuKienTet2017()) {
                    player.sendMessage(
                            MessageCreator.createMsgNpc(
                                    "Nồi bánh chưng", posNpcVuLan[player.inCountry][0] - 30, posNpcVuLan[player.inCountry][1], 50, 60, 3, 25, -65, (byte) 1
                            )
                    );
                    player.sendMessage(
                            MessageCreator.createMsgNpc(
                                    "Nồi bánh tét", posNpcVuLan[player.inCountry][0] + 30, posNpcVuLan[player.inCountry][1], 50, 60, 3, 26, -66, (byte) 1
                            )
                    );
                }

                if (Char.isSuKienTet2017()) {
                    if (player.inCountry == 0) {
                        player.sendMessage(
                                MessageCreator.createMsgNpc(
                                        "Cây lì xì", posNpcCayLixi[player.inCountry][0], posNpcCayLixi[player.inCountry][1], 82, 102, 3, 29, -67, (byte) 1
                                )
                        );
                    } else {
                        player.sendMessage(
                                MessageCreator.createMsgNpc(
                                        "Cây lì xì", posNpcCayLixi[player.inCountry][0], posNpcCayLixi[player.inCountry][1], 77, 96, 1, 30, -67, (byte) 1
                                )
                        );
                    }
                }
            }

            if (player.mapID == 118) {
                player.sendMessage(MessageCreator.createMsgNpc("Cổng dịch chuyển", 736, 640, 180, 180, 3, 13, -22, (byte) 1));
            }

            if (player.map.nRegion > 0 && player.map.pRegion != null) {
                player.sendMessage(
                        MessageCreator.createMsgNpc("Khu " + (player.region + 1), player.map.pRegion[0] * 16, player.map.pRegion[1] * 16, 27, 34, 1, 15, -34, (byte) 1)
                );
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public void sendInfoNpcFarm(Char player, int idImg, int house_store) {
    }

    public boolean checkChangeMap(Char p, int x, int y, int mapidChange, int xout, int yout) {
        try {
            CMLocation toMap = null;
            int dx = p.x / 16;
            int dy = p.y / 16;

            for (int i = 0; i < this.mapchangeLocations.size(); i++) {
                CMLocation cm = (CMLocation) this.mapchangeLocations.get(i);
                if (dy >= cm.toY - 8 && dy <= cm.toY + 8 && dx >= cm.toX - 8 && dx <= cm.toX + 8) {
                    toMap = cm;
                    break;
                }
            }

            int index = -1;

            for (int ix = 0; ix < mapID.length; ix++) {
                for (int j = 0; j < mapID[ix].length; j++) {
                    if (mapID[ix][j] == this.mapId) {
                        index = idArrMap[ix];
                        break;
                    }
                }

                if (index > -1) {
                    break;
                }
            }

            // lo #3-b loi.xlsx (tam): log chan doan "Canh rung dong -> nham Binh Kieu".
            // Chi log khi dang o nhom Bach Hac (18, 661-664) de khong ngap log server.
            // XOA sau khi biet duoc dung index/toM/gia tri that su can sua.
            if (this.mapIDLoadMap == 18) {
                try {
                    System.out.println("[CHK-MAP] player=" + p.charname + " mapId=" + this.mapId
                            + " mapIDLoadMap=" + this.mapIDLoadMap + " index=" + index
                            + " mapidChange(yeu cau)=" + mapidChange
                            + " toMap(CMLocation tim thay)=" + (toMap == null ? "null" : ("toM=" + toMap.toM + " toX=" + toMap.toX + " toY=" + toMap.toY))
                            + " player.x/16=" + (p.x / 16) + " player.y/16=" + (p.y / 16));
                    if (toMap != null && index >= 0 && index < locationMap.length && toMap.toM < locationMap[index].length) {
                        short[] dbgLoc = locationMap[index][toMap.toM];
                        String[] dbgName = (index < nameMap.length && toMap.toM < nameMap[index].length) ? nameMap[index][toMap.toM] : null;
                        StringBuilder sb = new StringBuilder("[CHK-MAP] locationMap[" + index + "][" + toMap.toM + "]=");
                        for (int z = 0; z + 2 < dbgLoc.length; z += 3) {
                            sb.append("{mapId=").append(dbgLoc[z]).append(",x=").append(dbgLoc[z + 1]).append(",y=").append(dbgLoc[z + 2]).append("}");
                            if (dbgName != null && z / 3 < dbgName.length) {
                                sb.append("(ten=").append(dbgName[z / 3]).append(") ");
                            }
                        }
                        System.out.println(sb.toString());
                    } else {
                        System.out.println("[CHK-MAP] KHONG doc duoc locationMap[" + index + "][" + (toMap == null ? -1 : toMap.toM) + "] - index/toM ngoai pham vi.");
                    }
                } catch (Exception dbgEx) {
                    System.out.println("[CHK-MAP] loi khi log: " + dbgEx);
                }
            }

            if (toMap != null) {
                short[] location = locationMap[index][toMap.toM];

                for (int ix = 0; ix < nameMap[index][toMap.toM].length; ix++) {
                    if (location[ix * 3] == mapidChange && !nameMap[index][toMap.toM][ix].equals("")) {
                        return true;
                    }
                }
            }
        } catch (Exception var13) {
        }

        return false;
    }

    public void doJoinMapOk(Char p) {
    }

    // Than method (388 dong) da chuyen sang MapXuLyLenh.java. Chu ky GIU NGUYEN.
    protected void doChangeMap(Char player, Message message) {
        MapXuLyLenh.doChangeMap(this, player,message);
    }

    public void kickCharToVilage(Char player) {
        try {
            int[] mapstart = new int[]{9, 481, 482, 483, 484};
            int homeX = 31 + Database.r.nextInt() % 5;
            int homeY = 79 + Database.r.nextInt(20);
            this.move2Map(player, homeX, homeY, mapstart[r.nextInt(MessageCreator.nclone)], player.inCountry);
        } catch (Exception var5) {
        }
    }

    public void doReturnVillage(Char player) {
        try {
            player.timedie = 0L;
            player.timeWaitComeHome = 0L;
            int[][] mapstart1 = new int[][]{{70, 1701}, {0, 301}, {80, 1901}};
            int homeX = 18 + Database.r.nextInt() % 5;
            int homeY = 24 + Database.r.nextInt(20);
            int homeMap = mapstart1[player.myCountry][r.nextInt(mapstart1[player.myCountry].length)];
            player.inCountry = player.myCountry;
            this.move2Map(player, homeX, homeY, homeMap, player.myCountry);
            player.timeWaitComeHome = 0L;
            player.calculateAttrib();
            player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
            player.sendToNearPlayer(MessageCreator.createCharInfo(player));
            player.doResetItemChienTruong();
        } catch (Exception var6) {
        }
    }

    protected void doComeHomeWhenDie(Char player) throws IOException {
        this.checkTrade(player);
        if (player.hp <= 0 || player.isThodia) {
            if (player.map.isMapChienTruongMoba()) {
                player.map.move2Map(player, player.x / 16, player.y / 16, 40, player.inCountry);
                player.doSendProperties();
            } else if (player.map.isMapNuiChauBau()) {
                try {
                    CharInfo cinfo = (CharInfo) MapChauBau.all_char_nui_kho_bau.get(player.charname);
                    if (cinfo.fail >= 4) {
                        cinfo.timeNuiChaubau = 0L;
                        cinfo.fail = 0;
                        this.doReturnVillage(player);
                        Database.instance.saveOrtherLog("", player.charname, "ko vao duoc nui chau bau do bi quai giet or mkn 4 lan", "chaubaufail");
                        Database.instance.saveLogThongKeChiTieu("KO VAO DUOC NUI CHAU BAU", 0, 1, 0L, "nui chau bau fail mskill");
                        return;
                    }

                    player.map.move2Map(player, 25, 11, idMapChanNui, player.inCountry);
                    player.hp = player.maxhp;
                    player.mp = player.maxmp;
                    player.doSendProperties();
                } catch (Exception var10) {
                    var10.printStackTrace();
                }
            } else {
                if (player.map != null) {
                    this.playerExit(player);

                    try {
                        ((Vector) listKillPerson.get(player.inCountry)).remove(player.charname);
                    } catch (Exception var11) {
                    }
                }

                player.timeHs = Char.timeAddMoreHs;
                player.timedie = 0L;
                player.timeWaitComeHome = 0L;
                int[] var10000 = new int[]{0, 301};
                int[][] mapstart1 = new int[][]{{70, 1701}, {0, 301}, {80, 1901}};
                int homeX = 18 + Database.r.nextInt() % 5;
                int homeY = 24 + Database.r.nextInt(20);
                int[] mapstart;
                if (player.myCountry == -1) {
                    mapstart = new int[]{117, 117, 117, 117, 117};
                    homeX = 39 + Database.r.nextInt() % 3;
                    homeY = 32 + Database.r.nextInt() % 5;
                } else if ((getTown[player.myCountry] || nwar[player.inCountry]) && this.mapId == idMapTown) {
                    mapstart = new int[]{9, 481, 482, 483, 484};
                    homeX = 31 + Database.r.nextInt() % 5;
                    homeY = 79 + Database.r.nextInt(20);
                } else if (this.mapId != 225 && this.mapId != 226) {
                    mapstart = mapstart1[player.myCountry];
                    int[][] pos = new int[][]{{10, 23, 14, 38, 30, 35, 21, 49}, {22, 41, 27, 32, 8, 30, 18, 11}, {10, 23, 14, 38, 30, 35, 21, 49}};
                    int index = r.nextInt(4);
                    homeX = pos[player.myCountry][index * 2] + r.nextInt() % 3;
                    homeY = pos[player.myCountry][index * 2 + 1] + r.nextInt() % 3;
                } else {
                    mapstart = new int[]{201};
                    homeX = 32;
                    homeY = 12;
                }

                int homeMap = player.hp > 0 && !player.isThodia ? player.mapID : mapstart[r.nextInt(mapstart.length)];
                if (!nwar[player.inCountry] && (player.hp <= 0 || player.isThodia)) {
                    player.inCountry = player.myCountry;
                }

                player.desTroy();
                Map toMap = (Map) RealController.mapList.get(homeMap);
                byte idxp = -1;
                if (toMap != null) {
                    toMap.playerJoin(player);
                    idxp = toMap.idXaphu;
                } else {
                    Map offlineMap = (Map) RealController.mapList.get(-1);
                    offlineMap.playerJoin(player);
                }

                player.x = homeX * 16 + Database.r.nextInt() % 16;
                player.y = homeY * 16 + Database.r.nextInt() % 16;
                player.mapID = homeMap;
                player.gotoIsland = false;
                player.calculateAttrib();
                player.die_pk = false;
                player.divSpeed = 1;
                player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                player.sendMessage(MessageCreator.createMapMessage(player));
                if (!player.isThodia) {
                    player.hp = player.maxhp;
                    player.mp = player.maxmp;
                }

                player.isThodia = false;
                player.idItem.removeAllElements();
                player.idItemQuest.removeAllElements();
                player.idPotion.removeAllElements();
                player.timeWaitComeHome = 0L;
                player.resetTimeLastuseSkill();
                this.sendInfoNpc(player);
                player.doSendProperties();
                player.sendPetEff();
            }
        }
    }

    public void doStartThreadUpdatePlayer() {
    }

    public void startLeafVilage() {
        if (this.nRegion <= 0) {
            new Thread(() -> {
                Thread.currentThread().setName("MAP SEND MONSTER 2 player" + Map.this.mapId);

                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = (Vector) Map.this.allPlayerMessages.get(1);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                            if (!pm.player.exit) {
                                Map.this.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }

                        Vector<NpcReceiveCard> npcReceiveCard = (Vector<NpcReceiveCard>) Map.npcReceiveCard.get(1);
                        if (Map.getTown[1] && Map.this.mapId == 201) {
                            for (int i = 0; i < npcReceiveCard.size(); i++) {
                                ((NpcReceiveCard) npcReceiveCard.get(i)).update();
                                if (NpcReceiveCard.isFinish[1]) {
                                    break;
                                }
                            }

                            if (NpcReceiveCard.isFinish[1]) {
                                Map.getTown[1] = false;
                                npcReceiveCard.removeAll(npcReceiveCard);
                            }
                        }

                        synchronized (Map.this.LOCK1) {
                            Map.this.LOCK1.wait((long) Map.timeDelay);
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }
                }

            }).start();
        }
    }

    public void startWindVilage() {
        if (this.mapIDLoadMap == 80) {
            if (this.nRegion <= 0) {
                new Thread(() -> {

                    Thread.currentThread().setName("THREAD LEAF " + Map.this.mapId);

                    while (true) {
                        try {
                            if (AdminHandler.isStopServer) {
                                Map.this.map_run_state = 0;
                                return;
                            }

                            while (RealController.savingChar) {
                                Thread.sleep(100L);
                            }

                            Map.this.map_run_state2 = 0;
                            long l1 = System.currentTimeMillis();
                            if (l1 - Map.this.lastTimeUpdateMap2 >= Map.DELAY_UPDATE_MAP) {
                                Map.this.updateWind();
                                Map.this.lastTimeUpdateMap2 = System.currentTimeMillis();
                            }

                            Map.this.map_run_state2 = 1;
                        } catch (Exception var5) {
                            if (!daBaoLoiRunMapWind) {
                                daBaoLoiRunMapWind = true;
                                System.out.println("LOI TRONG HAM RUN MAP (updateWind, chi bao 1 lan - lap lai se ngap log):");
                                var5.printStackTrace();
                            }
                        }

                        try {
                            synchronized (Map.this.LOCK2) {
                                Map.this.LOCK2.wait(Map.DELAY_UPDATE_MAP);
                            }
                        } catch (Exception var4) {
                        }
                    }

                }).start();
                new Thread(() -> {

                    Thread.currentThread().setName("MAP SEND MONSTER 2 player" + Map.this.mapId);

                    while (!AdminHandler.isStopServer) {
                        try {
                            int count = 0;
                            new Vector();
                            Vector<PlayerMessage> playerMessages = (Vector) Map.this.allPlayerMessages.get(2);

                            while (playerMessages.size() > 0) {
                                PlayerMessage pm = (PlayerMessage) playerMessages.remove(0);
                                if (!pm.player.exit) {
                                    Map.this.processMessage(pm.player, pm.message);
                                }

                                if (++count == 500) {
                                    count = 0;
                                    Thread.sleep(5L);
                                }
                            }

                            if (Map.getTown[2] && Map.this.mapId == 201) {
                                Vector<NpcReceiveCard> npcReceiveCard = (Vector<NpcReceiveCard>) Map.npcReceiveCard.get(2);

                                for (int i = 0; i < npcReceiveCard.size(); i++) {
                                    ((NpcReceiveCard) npcReceiveCard.get(i)).update();
                                    if (NpcReceiveCard.isFinish[2]) {
                                        break;
                                    }
                                }

                                if (NpcReceiveCard.isFinish[2]) {
                                    Map.getTown[2] = false;
                                    npcReceiveCard.removeAll(npcReceiveCard);
                                }
                            }

                            synchronized (Map.this.LOCK2) {
                                Map.this.LOCK2.wait((long) Map.timeDelay);
                            }
                        } catch (Exception var6) {
                            var6.printStackTrace();
                        }
                    }

                }).start();
            }
        }
    }

    // Than method (90 dong) da chuyen sang MapVongLap.java. Chu ky GIU NGUYEN.
    public void startSandVilage() {
        MapVongLap.startSandVilage(this);
    }

    public void run() {
        if (this.nRegion <= 0) {
            Thread.currentThread().setName("MAP " + this.mapId);

            while (true) {
                try {
                    if (AdminHandler.isStopServer) {
                        this.map_run_state = 0;
                        return;
                    }

                    while (RealController.savingChar) {
                        Thread.sleep(100L);
                    }

                    this.map_run_state1 = 0;
                    long l1 = System.currentTimeMillis();
                    if (l1 - this.lastTimeUpdateMap1 >= DELAY_UPDATE_MAP) {
                        this.update();
                        this.lastTimeUpdateMap1 = System.currentTimeMillis();
                    }

                    this.map_run_state1 = 1;
                } catch (Exception var5) {
                    if (!daBaoLoiRunMapUpdate) {
                        daBaoLoiRunMapUpdate = true;
                        System.out.println("LOI TRONG HAM RUN MAP (update, chi bao 1 lan - lap lai se ngap log):");
                        var5.printStackTrace();
                    }
                }

                try {
                    synchronized (this.LOCK1) {
                        this.LOCK1.wait(DELAY_UPDATE_MAP);
                    }
                } catch (Exception var4) {
                }
            }
        }
    }

    private void checkTimeCreateWeather() {
        if (weather == null && System.currentTimeMillis() > timeWait) {
            Calendar cl = Calendar.getInstance();
            int iHour = cl.get(11);
            int time = r.nextInt(24);
            if (iHour <= iHour + 1 && iHour >= abs(iHour - 1)) {
                short[] temp = new short[]{10, 15, 20, 25};
                short tExist = temp[r.nextInt(4)];
                weather = new Weather();
                byte[] eff = new byte[]{2, 2, 2, 2, 2, 2, 2, 2, 2};
                weather.id = eff[r.nextInt(9)];
                weather.time = tExist;
                if (weather.id != 0 && weather.id != 2) {
                    weather.n = (short) (10 + r.nextInt(10));
                } else {
                    weather.n = (short) (50 + r.nextInt(150));
                }

                timeWait = System.currentTimeMillis() + (long) (weather.time * '\uea60');
                sendAllCharServer(-1, MessageCreator.createMsgWeather(weather.id, weather.n, weather.time * '\uea60'));
            } else {
                timeWait = System.currentTimeMillis() + 900000L;
            }
        }
    }

    public static void sendAllCharServerChat(int country, String info) {
        try {
            try {
                for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                    Char p = (Char) CharManager.instance.vChars.get(i);
                    if (p != null && p.myCountry == country) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                    } else if (p == null) {
                        CharManager.instance.vChars.remove(i);
                    }
                }
            } catch (Exception var4) {
            }
        } catch (Exception var5) {
        }
    }

    /**
     * Mua 3 loai hoa (hong/mai-vang/dao) cho TOAN SERVER - hieu ung thoi tiet THUAN HIEN THI,
     * KHONG cong potions[] cho ai. Dung chung cho tinh luyen than binh thanh cong VA len cap tu
     * 60 tro len (yeu cau nguoi dung 18/09/2026). Moi loai hoa goi 3 "goi" x 99 hoa, giong het
     * cach vong lap thoi tiet dinh ky cua server tu goi sendAllCharServer.
     *
     * ID hieu ung trung voi co che tang hoa thu cong o MapNhapText.java (idHoa={4,3,5} ung voi
     * mai/dao/hong) - xem AnimateEffect.cs ben client, chi co dung 3 loai hoa roi nay.
     */
    public static void muaHoaAnMungToanServer() {
        final int[] idHoaAnMung = {5, 4, 3}; // hoa hong, hoa mai (vang), hoa dao
        for (int idHoa : idHoaAnMung) {
            for (int lanMuaHoa = 0; lanMuaHoa < 3; lanMuaHoa++) {
                Map.sendAllCharServer(-1, MessageCreator.createMsgWeather(idHoa, 99, 60000));
            }
        }
    }

    public static void sendAllCharServer(Message m, int country) {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                Char p = (Char) CharManager.instance.vChars.get(i);
                if (p != null && p.myCountry == country) {
                    p.sendMessage(m);
                    m.cleanALlData();
                } else if (p == null) {
                    CharManager.instance.vChars.remove(i);
                }
            }
        } catch (Exception var4) {
        }
    }

    public static void sendAllCharServer(int exceptMapid, Message m) {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                Char p = (Char) CharManager.instance.vChars.get(i);
                if (p != null) {
                    if (exceptMapid == -1 || p.mapID != exceptMapid) {
                        p.sendMessage(m);
                    }
                } else {
                    CharManager.instance.vChars.remove(i);
                }
            }
        } catch (Exception var4) {
        }
    }

    public static void sendAllCharServerExceptWarTown(int exceptMapid, Message m) {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                Char p = (Char) CharManager.instance.vChars.get(i);
                if (p != null) {
                    if ((p.map.mapId != idMapTown || getTown[p.inCountry]) && (exceptMapid == -1 || p.mapID != exceptMapid)) {
                        p.sendMessage(m);
                    }
                } else {
                    CharManager.instance.vChars.remove(i);
                }
            }
        } catch (Exception var4) {
        }
    }

    public static void sendAllCharServerInMap(int[] mapsend, Message m) {
        try {
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                Char p = (Char) CharManager.instance.vChars.get(i);
                if (p != null) {
                    for (int j = 0; j < mapsend.length; j++) {
                        if (p.mapID == mapsend[j]) {
                            p.sendMessage(m);
                        }
                    }
                } else {
                    CharManager.instance.vChars.remove(i);
                }
            }
        } catch (Exception var5) {
        }
    }

    public static boolean is83() {
        Calendar cl = Calendar.getInstance();
        int day = cl.get(5);
        int month = cl.get(2) + 1;
        return day == 8 && month == 3;
    }

    public static boolean checkGoldTime() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        int iMinute = cl.get(12);
        if ((iHour >= 8 && iHour < 11 || iHour >= 15 && iHour < 17) && iMinute >= 0 && iMinute <= 59) {
            if (!goldTime) {
                goldTime = true;

                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Giờ vàng bắt đầu."));
                } catch (Exception var4) {
                }
            }

            return true;
        } else {
            goldTime = false;
            return false;
        }
    }

    public static boolean checkTimeThanTaiApprear() {
        Calendar cl = Calendar.getInstance();
        int day = cl.get(5);
        int iHour = cl.get(11);
        int iMinute = cl.get(12);
        if ((day == 23 || day == 24 || day == 25)
                && (iHour >= 8 && iHour < 11 || iHour >= 15 && iHour < 17 || iHour >= 21 && iHour < 24)
                && iMinute >= 0
                && iMinute <= 59) {
            if (!npcAppear) {
                npcAppear = true;

                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Thần Tài chúc các game thủ một năm mới An Khang Thịnh Vượng."));
                } catch (Exception var5) {
                }
            }

            return true;
        } else {
            npcAppear = false;
            return false;
        }
    }

    static boolean timeCanPK() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        int iMinute = cl.get(12);
        int time1 = 17;
        int time2 = 12;
        if (!openLog) {
            int var8 = 16;
            int var9 = 11;
        }

        if (pkAuto) {
            if ((iHour < 17 || iHour >= 18) && (iHour <= 10 && (iHour != 10 || iMinute <= 30) || iHour >= 12 && (iHour != 12 || iMinute >= 30))) {
                pkAuto = false;

                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Thời gian pk tự do kết thúc"));
                } catch (IOException var7) {
                }
            }
        } else if (iHour >= 17 && iHour < 18 || (iHour > 10 || iHour == 10 && iMinute > 30) && (iHour < 12 || iHour == 12 && iMinute < 30)) {
            pkAuto = true;

            try {
                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Thời gian pk tự do bắt đầu."));
            } catch (IOException var6) {
            }
        }

        return pkAuto;
    }

    static boolean checkTimeRegEventMonster() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        return iHour >= 19 && iHour <= 21;
    }

    public static boolean openSkelonton() {
        return false;
    }

    public boolean checkTimeStartWar(String day) {
        return false;
    }

    public boolean checkTimeGetTown(int id) {
        return false;
    }

    public void addChar2MapMonster(int from, int country) {
        int[] idmapmons = new int[]{203, 4361, 4362, 4363, 4364, 4365, 4366};
        int i = 0;

        for (int j = 0; j < idmapmons.length; j++) {
            if (idmapmons[j] == this.mapId) {
                i = j;
                break;
            }
        }

        Vector<Short> v = (Vector<Short>) ((Vector) groupMonster.get(country)).get(i);
        if (v.size() < 20 && v.size() > 0) {
            if (v.size() >= 10) {
                int k = 0;

                for (int jx = 0; jx < v.size(); jx++) {
                    k++;
                    Short chardbid = (Short) v.get(jx);
                    Char player = CharManager.instance.getByCharID(chardbid);
                    if (player != null) {
                        player.x = (21 + Database.r.nextInt() % 5) * 16;
                        player.y = (32 + Database.r.nextInt() % 5) * 16;
                        byte[] xxx = new byte[]{21, 21, 21, 21};
                        byte[] yxx = new byte[]{32, 32, 32, 32};
                        int rddd = r.nextInt(4);
                        if (openLog) {
                            this.move2Map(player, xxx[rddd], yxx[rddd], idmapmons[i], player.inCountry);
                        } else {
                            this.move2Map(player, xxx[rddd], yxx[rddd], idmapmons[i], 0);
                            System.out.println(" ADD " + player.charname + " VÀO MAP " + idmapmons[i]);
                        }

                        k++;
                    }

                    if (k >= 20) {
                        break;
                    }
                }

                v.removeAllElements();
                this.timeStartGame[country] = System.currentTimeMillis();
            }
        } else {
            for (int k = 0; k <= 20 && v.size() > 0; k++) {
                Char playerx = CharManager.instance.getByCharID((Short) v.remove(0));
                if (playerx != null) {
                    playerx.x = (21 + Database.r.nextInt() % 5) * 16;
                    playerx.y = (32 + Database.r.nextInt() % 5) * 16;
                    byte[] xxx = new byte[]{21, 21, 21, 21};
                    byte[] yxx = new byte[]{32, 32, 32, 32};
                    int rddd = r.nextInt(4);
                    this.move2Map(playerx, xxx[rddd], yxx[rddd], idmapmons[i], playerx.inCountry);
                }
            }

            this.timeStartGame[country] = System.currentTimeMillis();
        }
    }

    boolean checkTimeEventMonster(int country) {
        if (!this.killMonster[country]) {
            String nt = new Date(System.currentTimeMillis()).toString();
            boolean isDay = nt.startsWith("Wed");
            if (isDay) {
                Calendar cl = Calendar.getInstance();
                int iHour = cl.get(11);
                int iMinute = cl.get(12);
                if (iHour == timeEventMonster && iMinute < minuteEventMons) {
                    System.out.println("BAT DAU su kien san manh thu " + country);
                    this.killMonster[country] = true;
                    eventKillMonster[country] = true;
                    this.addChar2MapMonster(0, country);
                    return true;
                }
            }
        }

        return false;
    }

    void kickPlayerOutEventMonster(int country) {
        this.nCharBeKilled[country] = 0;
        int[][] mapstart1 = new int[][]{{70, 1701}, {0, 301}, {80, 1901}};
        int[] mapstart = mapstart1[country];
        Vector<Char> players = (Vector<Char>) this.allPlayers.get(country);

        for (int i = 0; i < players.size(); i++) {
            Char player = (Char) players.get(i);
            player.isMonster = false;
            player.x = 16 + Database.r.nextInt() % 10;
            player.y = 24 + Database.r.nextInt() % 10;
            if (openLog) {
                this.move2Map(player, player.x, player.y, mapstart[r.nextInt(mapstart.length)], player.inCountry);
            } else {
                this.move2Map(player, player.x, player.y, mapstart[r.nextInt(mapstart.length)], player.myCountry);
            }
        }

        players.removeAllElements();
    }

    boolean endGameMonster(int country) {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        if (iHour <= 20) {
            return false;
        } else {
            this.killMonster[country] = false;
            eventKillMonster[country] = false;
            this.kickPlayerOutEventMonster(country);

            for (int i = 0; i < ((Vector) groupMonster.get(country)).size(); i++) {
                ((Vector) ((Vector) groupMonster.get(country)).get(i)).removeAllElements();
            }

            return true;
        }
    }

    public boolean gameOver(int country) {
        return false;
    }

    public static void removeBossLocation(int type) {
        for (int i = 0; i < bossLocation.size(); i++) {
            if (((BossLocaltion) bossLocation.get(i)).type == type) {
                bossLocation.remove(i);
                break;
            }
        }
    }

    public void removePLayer(Char player) {
        Vector<Char> players = null;
        if (this.nRegion > 0) {
            players = ((Region) this.ALL_REGION.get(player.region)).getAllPlayer(player.inCountry);
        } else {
            players = (Vector<Char>) this.allPlayers.get(player.inCountry);
        }

        // Go theo THAM CHIEU doi tuong, khong theo ten. Khi nguoi choi bi cat roi vao
        // lai ngay, hai doi tuong Char cua cung mot nhan vat co the cung ton tai: doi
        // chieu theo ten thi playerExit(ban_cu) go dung ban MOI dang choi ra khoi ban
        // do - nguoi choi bien mat voi nguoi khac, khong con duoc p.update() nua, roi
        // bi chinh vong lap ban do don di.
        try {
            players.remove(player);
        } catch (Exception ignored) {
        }
    }

    public void addPlayerMessage(Char p, Message message) {
        if (this.nRegion > 0) {
            ((Region) this.ALL_REGION.get(p.region)).addPlayerMessage(p, message);
        } else {
            ((Vector) this.allPlayerMessages.get(p.inCountry)).add(new PlayerMessage(p, message));
        }
    }

    public void notifyMap() {
        try {
            synchronized (this.LOCK) {
                this.LOCK.notify();
            }
        } catch (Exception var7) {
        }

        try {
            synchronized (this.LOCK1) {
                this.LOCK1.notify();
            }
        } catch (Exception var5) {
        }

        try {
            synchronized (this.LOCK2) {
                this.LOCK2.notify();
            }
        } catch (Exception var3) {
        }
    }

    private void doOpenDun(Char player, int typeDun) {
        try {
            int delta = 0;
            switch (typeDun) {
                case 1:
                    delta = -10;
                    break;
                case 2:
                    delta = 0;
                    break;
                case 3:
                    delta = 10;
            }

            if (player.party.userParty.size() > 0 && player.party.idMaster != player.id) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chủ pt mới được phép mở cửa", ""));
                return;
            }

            int maxLevel = 75;
            int timePlay = 7200000;
            Hashtable<Short, Monster> monsters = new Hashtable();
            int lvMonster = player.getLevel();
            Map mapDUN;
            if (player.party.userParty.size() <= 0) {
                mapDUN = (Map) RealController.mapList.get(114);
                if (((Vector) mapDUN.allPlayers.get(player.myCountry)).size() >= 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Dun đã đầy, vui lòng trở lại sau", ""));
                    return;
                }
            } else {
                mapDUN = (Map) RealController.mapList.get(114);
                if (((Vector) mapDUN.allPlayers.get(player.myCountry)).size() >= 500) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Dun đã đầy, vui lòng trở lại sau", ""));
                    return;
                }

                int totalLv = 0;

                for (int i = 0; i < player.party.userParty.size(); i++) {
                    totalLv += ((Char) player.party.userParty.get(i)).getLevel();
                }

                lvMonster = totalLv / player.party.userParty.size() + 3;
            }

            if (lvMonster > maxLevel) {
                lvMonster = maxLevel;
            }

            Collection<MonsterTemplate> temps = monsterTemplates.values();
            MonsterTemplate template = null;
            MonsterTemplate template1 = null;
            MonsterTemplate template2 = null;

            for (MonsterTemplate temp : temps) {
                if (temp.level == lvMonster - 1) {
                    template1 = temp;
                }

                if (temp.level == lvMonster) {
                    template2 = temp;
                }

                if (template1 != null && template2 != null) {
                    break;
                }
            }

            int[][] tempCord = new int[][]{
                {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                {41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60},
                {41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60}
            };

            for (int i = 0; i < tempCord.length; i += 2) {
                for (int j = 0; j < tempCord[i].length; j++) {
                    template = r.nextBoolean() ? template1 : template2;
                    Monster m0 = new Monster(mapDUN, template, tempCord[i][j], tempCord[i + 1][j], player.inCountry);
                    m0.id = player.getIDItem();
                    m0.level = m0.getMonsterTemplate().level;
                    byte[] he = new byte[]{0, 1, 2, 3, 4};
                    m0.he = he[r.nextInt(5)];
                    byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0};
                    m0.typeAttack = t[r.nextInt(10)];
                    m0.magic_physic = player.magic_physic;
                    m0.attack = m0.attack + m0.attack / 100 * delta;
                    m0.defend_magic = m0.defend_magic + m0.defend_magic / 100 * delta;
                    m0.defend_physic = m0.defend_physic + m0.defend_physic / 100 * delta;
                    m0.xp = m0.xp + m0.xp / 100 * delta;
                    m0.maxhp = m0.maxhp + m0.maxhp / 100 * delta;
                    m0.hp = m0.hp + m0.hp / 100 * delta;
                    m0.maxxp = m0.maxxp + m0.maxxp / 100 * delta;
                    monsters.put(m0.id, m0);
                }
            }

            player.monsters = monsters;
            player.timeWaitComeHome = System.currentTimeMillis() + (long) timePlay;
        } catch (Exception var19) {
            var19.printStackTrace();
        }
    }

    private void doGoInDun(Char player) {
        try {
            if (player.monster != null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi đang vận tiêu", ""));
                return;
            }

            if (player.party.userParty.size() > 0) {
                Char pLeader = CharManager.instance.getByCharID(player.party.idMaster);
                if (pLeader.monsters == null || pLeader.monsters.size() == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điều kiện để vào cửa", ""));
                    return;
                }

                this.move2Map(player, 16, 8, 114, player.inCountry);
            } else {
                if (player.monsters == null || player.monsters.size() == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chưa mở cửa không thể vào", ""));
                    return;
                }

                this.move2Map(player, 16, 8, 114, player.inCountry);
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public void doGoOutDun(Char player) {
        try {
            if (player.monster != null) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể tham gia khi đang vận tiêu", ""));
                return;
            }

            if (player.party.userParty.size() > 0) {
                if (player.party.idMaster == player.id) {
                    player.monsters = null;
                }
            } else {
                player.monsters = null;
            }

            int[] mapstart = new int[]{0, 301, 302, 303, 304};
            int homeX = 18 + Database.r.nextInt() % 5;
            int homeY = 24 + Database.r.nextInt(20);
            this.move2Map(player, homeX, homeY, mapstart[r.nextInt(MessageCreator.nclone)], player.inCountry);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public static byte compareDate(String date1, String date2) {
        String[] a1 = Char.split(date1, "-");
        String[] a2 = Char.split(date2, "-");
        int y1 = Integer.parseInt(a1[0]);
        int m1 = Integer.parseInt(a1[1]);
        int d1 = Integer.parseInt(a1[2]);
        int y2 = Integer.parseInt(a2[0]);
        int m2 = Integer.parseInt(a2[1]);
        int d2 = Integer.parseInt(a2[2]);
        if (y1 <= y2 && (y1 != y2 || m1 <= m2) && (y1 != y2 || m1 != m2 || d1 <= d2)) {
            return (byte) (y1 >= y2 && (y1 != y2 || m1 >= m2) && (y1 != y2 || m1 != m2 || d1 >= d2) ? 0 : -1);
        } else {
            return 1;
        }
    }

    public boolean isDun() {
        return this instanceof Dun;
    }



    public static NewClan getClaninfoByID(int idClan) {
        return NewClan.getClan((short) idClan);
    }

    public static void removeClaninfoOutHash(int idCLan) {
        NewClan.removeClan((short) idCLan);
    }

    public static void putClaninfoOutHash(int idCLan, ClanInfo clan) {
    }

    public static void resetClanReg() {
    }


    private void addGifEventDaiNam(Char p, byte typeGif) {
        if (!openLog) {
            String info = "";
            int pc = r.nextInt(100);
            if (pc <= 70) {
                addXpCharEvent(p, 50000L, false, "addGifEventDaiNam");
                info = "Nhận được 50k exp";
            } else if (pc <= 80) {
                p.addXu(1000L, "map 86");
                info = "Nhận được 1000 xu";
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else if (pc <= 85) {
                int[] idmodel = new int[]{50, 51};
                int id = idmodel[r.nextInt(2)];
                ShopTemplate s = getShopTemplate(id);
                doBuyModelClothe(p, s, 7);
                info = "Nhận được " + s.name + " thời hạn 7 ngày";
            } else {
                int idgem = -1;
                short var9;
                if (pc <= 90) {
                    var9 = GemTemplate.ID_MATERIAL_LOW[0][r.nextInt(5)];
                } else if (pc <= 93) {
                    var9 = GemTemplate.ID_MATERIAL_LOW[1][r.nextInt(5)];
                } else if (pc <= 96) {
                    var9 = GemTemplate.ID_MATERIAL_LOW[2][r.nextInt(5)];
                } else {
                    var9 = GemTemplate.ID_DA_NGU_HOP[r.nextInt(3)][r.nextInt(6)];
                }

                if (var9 > -1) {
                    p.addLockGem(var9, 1);
                    info = "Nhận được " + gemTemplate[var9].name;
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                }
            }

            Database.instance.saveOrtherLog("", p.charname, info, "opengift1");
        }
    }


    private void giveItemAnimal(Char player, byte magic_physic) {
        short[] idTemplate = new short[]{509, 521, 533, 545, 557};
        int lv = player.lvDetail.lv / 5 * 5;
        int start = (lv - 30) / 5;
        byte kindItem = 0;

        for (int i = 0; i < 5; i++) {
            for (byte j = 0; j < 2; j++) {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(idTemplate[i] + start));
                Item newItem = new Item(
                        template, false, kindItem == -1 ? template.clazz : kindItem, kindItem == -1 ? template.clazz : kindItem, idTemplate[i] + start
                );
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                newItem.clazz = kindItem;
                newItem.magic_physic = magic_physic;
                if (newItem.magic_physic < 0) {
                    newItem.magic_physic = 2;
                }

                if (magic_physic == 0) {
                    newItem.atb[6] = newItem.atb[1];
                    newItem.atb[1] = (short) (newItem.atb[1] / 10);
                } else if (magic_physic == 1) {
                    newItem.atb[6] = (short) (newItem.atb[1] / 10);
                }

                newItem.dateCreate = Char.getDayTime(0L);
            }
        }

        Database.instance.saveOrtherLog("", player.charname, "lv : " + lv + "_" + start, "gifevent2");
    }

    private void doSellItemVip(Char player, Message msg) {
        ShopVip.doSellItemVip(player, msg);
    }

    public static void doSendListItem(Char player, Message msg) {
        ShopVip.doSearchItem(player, msg);
    }

    public static void SystemOut(String info) {
        System.out.println(info);
    }

    public void removeRegion(int id) {
    }

    public static boolean isMapLang(Map m) {
        return m.mapIDLoadMap == 0 || m.mapIDLoadMap == 70 || m.mapIDLoadMap == 80;
    }

    public static boolean isMapThanh(Map m) {
        return m.mapIDLoadMap == 201;
    }

    public boolean isMapMonster() {
        int[] idm = new int[]{203, 4362, 4363, 4364, 4365, 4366, 4367};

        for (byte i = 0; i < idm.length; i++) {
            if (idm[i] == this.mapId) {
                return true;
            }
        }

        return false;
    }

    public void doSendFarm(Char p) {
        if (p.seedsItem.size() > 0) {
            Message m = new Message(-59);

            try {
                m.dos.writeByte(p.seedsItem.size());

                for (int i = 0; i < p.seedsItem.size(); i++) {
                    SeedItem s = (SeedItem) p.seedsItem.get(i);
                    m.dos.writeByte(s.idFarm);
                    m.dos.writeByte(s.idTemplate);
                    m.dos.writeUTF(s.getNameTree());
                    m.dos.writeByte(s.getIdImgLevel());
                    int totalTime = s.getTotalTimeLive() - 60;
                    if (totalTime < 0) {
                        totalTime = 0;
                    }

                    m.dos.writeInt(totalTime);
                    m.dos.writeByte(s.getTemplate().x);
                    m.dos.writeByte(s.getTemplate().y);
                    m.dos.writeBoolean(s.buy == 1);
                    m.dos.writeByte(s.getDelYImgLv());
                    m.dos.writeByte(s.rankTree);
                }

                p.sendMessage(m);
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        this.sendInfoNpcFarm(p, p.lvHouse - 1, 0);
        this.sendInfoNpcFarm(p, p.lvStore - 1, 1);
    }

    public void doSendFarm(Char p, SeedItem s) {
        if (p.seedsItem.size() > 0) {
            Message m = new Message(-59);

            try {
                m.dos.writeByte(1);
                m.dos.writeByte(s.idFarm);
                m.dos.writeByte(s.idTemplate);
                m.dos.writeUTF(s.getNameTree());
                m.dos.writeByte(s.getIdImgLevel());
                int totalTime = s.getTotalTimeLive() - 60;
                if (totalTime < 0) {
                    totalTime = 0;
                }

                m.dos.writeInt(totalTime);
                m.dos.writeByte(s.getTemplate().x);
                m.dos.writeByte(s.getTemplate().y);
                p.sendMessage(m);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }
    }

    public void createMonsterBoss(int idTemplate, int hp, int attack, int defend, Vector<Short> x, Vector<Short> y, byte lv) {
        for (int i = 0; i < x.size(); i++) {
            for (int k = 0; k < 3; k++) {
                Monster m = new Monster(this, (MonsterTemplate) monsterTemplates.get(idTemplate), (Short) x.get(i) * 16, (Short) y.get(i) * 16, (byte) k);
                m.level = lv;
                m.attack = (short) attack;
                m.hp = hp;
                m.defend_magic = (short) defend;
                m.defend_physic = (short) defend;
            }
        }
    }

    /** Bu cho du SO_NUOC ngan. Goi lai nhieu lan khong cong don. */
    public void initAllPlayer() {
        while (this.allPlayers.size() < SO_NUOC) {
            this.allPlayers.add(new Vector());
        }
    }

    /** Ba nuoc, chi so 0..2. MAX_COUNTRY la CHI SO lon nhat (=2), khong phai so luong. */
    public static final int SO_NUOC = 3;

    /**
     * Khoi tao cac kho chua VAT THE NAM DUOI DAT: moi loai mot ngan rieng cho tung nuoc.
     *
     * VI SAO PHAI GOM THANH HAM RIENG
     * --------------------------------
     * Truoc day chi MapKhoiTao.init() lam viec nay. Nhung OfflineMap goi super(id) ->
     * Map(int id), va ham dung ay KHONG di qua MapKhoiTao. Nghia la moi ban do offline
     * chay voi nam Vector RONG, trong khi co 22 cho trong Map.java lay .get(country) tren
     * chung. Tat ca deu nem ArrayIndexOutOfBoundsException:
     *
     *   - addPotion / addItemMap / addGemItem / addItemQuest  -> do roi xuong dat MAT HAN
     *   - removePotion / removeItem / ...                      -> nem loi
     *   - getPotion / getItemQuest / getGemItem                -> nem loi
     *   - deletePotionAndItemOnGround                          -> nem MOI NHIP vong lap
     *
     * Cai cuoi la thu duy nhat lo ra: vong lap sand goi no lien tuc nen server sinh
     * 124 MB log moi ngay va an 22% CPU du chi co mot nguoi choi. Ba cai tren im lang.
     *
     * Da co nguoi vap phai dung ho loi nay roi va vao tay tung cho trong constructor cua
     * OfflineMap (allPlayerMessages, ALL_EFFECT_INMAP) — nhung bo sot bon kho con lai.
     * Do la ly do gom vao mot cho: de khong con HAI noi phai nho giong nhau.
     *
     * Goi lai nhieu lan khong sao: chi bu cho du, khong bao gio them chong len.
     */
    public void initVatTheDuoiDat() {
        while (this.potions.size() < SO_NUOC) {
            this.potions.add(new Vector());
        }
        while (this.items.size() < SO_NUOC) {
            this.items.add(new Vector());
        }
        while (this.itemquests.size() < SO_NUOC) {
            this.itemquests.add(new Vector());
        }
        while (this.gemItem.size() < SO_NUOC) {
            this.gemItem.add(new Vector());
        }
        while (this.ALL_EFFECT_INMAP.size() < SO_NUOC) {
            this.ALL_EFFECT_INMAP.add(new Vector());
        }
    }

    /** Bu cho du SO_NUOC ngan. Goi lai nhieu lan khong cong don. */
    public void initAllMonsTer() {
        while (this.monsters.size() < SO_NUOC) {
            this.monsters.add(new Hashtable());
        }
    }

    /**
     * Khoi tao cac kho QUAI VA NGUOI CHOI theo nuoc.
     *
     * Cung ho voi initVatTheDuoiDat(). MapKhoiTao chi do day chung khi nregion == 0
     * (ban do co Region thi Region tu giu quai cua no), con OfflineMap thi khong di qua
     * MapKhoiTao nen bo trong het.
     *
     * Hau qua da thay that: MapVongLap.updateSand:857 doc map.tempMonster.get(0) va nem
     * ArrayIndexOutOfBounds. No chi nem khi CO nguoi dung tren ban do do — dong 856 la
     * `if (playersx.size() > 0 ...)` — nen truoc day no bi che khuat sau mot loi khac
     * xay ra thuong xuyen hon o cuoi cung ham. Log cu co 1.012 lan cua no.
     *
     * Bu cho du, khong cong don, nen goi chung voi initAllPlayer()/initAllMonsTer() deu duoc.
     */
    public void initKhoQuaiVaNguoi() {
        this.initAllPlayer();
        this.initAllMonsTer();
        while (this.allPlayerMessages.size() < SO_NUOC) {
            this.allPlayerMessages.add(new Vector());
        }
        while (this.temp_monsters.size() < SO_NUOC) {
            this.temp_monsters.add(new Hashtable());
        }
        while (this.tempMonster.size() < SO_NUOC) {
            this.tempMonster.add(new Vector());
        }
        while (this.tempRemoveMonster.size() < SO_NUOC) {
            this.tempRemoveMonster.add(new Vector());
        }
    }

    public Vector<LiveActor> getAllNearActor(Char from, int cat, int sl, LiveActor attacker) {
        if (this.nRegion > 0) {
            return ((Region) this.ALL_REGION.get(from.region)).getAllNearActor(from, cat, sl, attacker);
        } else {
            Vector<LiveActor> ac = new Vector();
            Vector<LiveActor> c = new Vector();
            Char actorGiveCard = null;
            if (cat == 0) {
                for (int i = 0; i < from.nearChars.size(); i++) {
                    Char pp = this.getPlayerByID((Short) from.nearChars.get(i));
                    if ((!pp.isKhangDoSat() || pp.isKiller || !from.map.isMapTrain())
                            && pp != null
                            && pp.isBot == -1
                            && pp.id != from.id
                            && pp.hp > 0
                            && !pp.isAdmin
                            && pp.getIdCharThanThu() == -1) {
                        if ((from.mapID != 201 || pp.idClan == from.idClan) && pp.killer <= 0 && pp.timeGiveCardTown <= 0L) {
                            if (pp.myCountry != from.myCountry) {
                                c.add(pp);
                            }
                        } else if (this.givingCard(pp)) {
                            actorGiveCard = pp;
                        } else {
                            c.add(pp);
                        }
                    }
                }
            } else if (cat == 1) {
                for (Monster mons : this.monsters.get(from.inCountry).values()) {
                    if (!mons.isDead && !mons.isCongThanh() && !mons.isRuongMaquai() && !mons.isNgocRong() && near(from, mons) && !mons.isMonsterVantieu()) {
                        c.add(mons);
                    }
                }
            }

            if (c.size() < sl) {
                if (attacker.hp > 0) {
                    c.add(attacker);
                }

                if (attacker.cat == 0 && actorGiveCard != null && actorGiveCard.id != attacker.id && r.nextInt(100) < 50) {
                    c.add(actorGiveCard);
                }

                return c;
            } else {
                while (ac.size() < sl) {
                    ac.add((LiveActor) c.remove(r.nextInt(c.size())));
                }

                if (attacker.hp > 0) {
                    ac.add(attacker);
                }

                if (attacker.cat == 0 && actorGiveCard != null && actorGiveCard.id != attacker.id && actorGiveCard.hp > 0 && r.nextInt(100) < 50) {
                    // Nhanh nay tra ve 'ac' (khong phai 'c') - phai them vao dung Vector duoc
                    // tra ve, neu khong actorGiveCard khong bao gio xuat hien trong ket qua du
                    // roll trung 50%.
                    ac.add(actorGiveCard);
                }

                return ac;
            }
        }
    }

    public Hashtable<Short, Monster> getAllMons(int country, int region) {
        return this.nRegion > 0 ? ((Region) this.ALL_REGION.get(region)).getAllMons(country) : (Hashtable) this.monsters.get(country);
    }

    public Vector<Char> getAllPlayer(int inCountry, int region) {
        return this.nRegion > 0 ? ((Region) this.ALL_REGION.get(region)).getAllPlayer(inCountry) : (Vector) this.allPlayers.get(inCountry);
    }

    public Vector<EffectBuff> getAllEffectAuto(int inCountry, int region) {
        return this.nRegion > 0 ? ((Region) this.ALL_REGION.get(region)).getAllEffectAuto(inCountry) : (Vector) this.EFFECT_AUTO.get(inCountry);
    }

    public void doPlant(Char p, Message msg) {
    }

    public void processPlant(Char p, Message msg, int idNpc, int idMenu, int idOptionMenu) {
    }

    public boolean isMapLienDau() {
        return false;
    }

    public static boolean isDayOpenLiendau() {
        String nt = new Date(System.currentTimeMillis()).toString();
        return nt.startsWith("Sat");
    }

    public boolean isPublicMap() {
        return false;
    }

    static int[] getTotalMinMaxLV_User() {
        int[] totla_min_max = new int[2];
        int min = allUser[0];
        int max = allUser[0];

        for (byte i = 1; i < 3; i++) {
            if (min > allUser[i]) {
                min = allUser[i];
            }

            if (max < allUser[i]) {
                max = allUser[i];
            }
        }

        totla_min_max[0] = min + max;
        min = allLV[0];
        max = allLV[0];

        for (byte i = 1; i < 3; i++) {
            if (min > allLV[i]) {
                min = allLV[i];
            }

            if (max < allLV[i]) {
                max = allLV[i];
            }
        }

        totla_min_max[1] = min + max;
        return totla_min_max;
    }

    public static boolean checkFullNation(int idNation, int n, int totalLV) {
        return false;
    }

    public static String getAllUserCountry() {
        return allUser[0] + "," + allUser[1] + "," + allUser[2] + "," + allLV[0] + "," + allLV[1] + "," + allLV[2] + "," + allXuLucky + "," + allLuongLucky;
    }

    public static String getInfoWar() {
        String info = "";
        String infowinlose = "";

        for (int i = 0; i < nationWar.size(); i++) {
            RegisterAttack reg = (RegisterAttack) nationWar.get(i);
            info = info + reg.dayAttack + "," + reg.idMyAttack + "," + reg.idNationAttackMe + ",";
            infowinlose = infowinlose + reg.win + "," + reg.lose + "," + reg.timeX2 + ",";
        }

        info = info + nationBeAttack[0] + "," + nationBeAttack[1] + "," + nationBeAttack[2] + ",";
        info = info + minuteX2Country[0] + "," + minuteX2Country[1] + "," + minuteX2Country[2] + ",";
        return info + x2Country[0] + "," + x2Country[1] + "," + x2Country[2] + "," + infowinlose.substring(0, infowinlose.length() - 1);
    }

    public static String getInfoT() {
        return event.getInfoT() + "," + winNumber;
    }

    public static String getQuaySoPhuongHoang() {
        String day = Char.getDayOpen(0L);
        day.equals(dayQuaySo);
        dayQuaySo = day;
        return dayQuaySo + "," + nPhuongHoang + "," + max_phuong_hoang + "," + countQuaySo + "," + MAX_QUAY;
    }

    public static void resetShield(int id) {
        SHIELD[id] = new byte[4];
    }

    public static void resetAllGov() {
        Char.gov[0].reset();
        Char.gov[1].reset();
        Char.gov[2].reset();
    }

    public static void resetAllShield() {
        SHIELD[0] = new byte[4];
        SHIELD[1] = new byte[4];
        SHIELD[2] = new byte[4];
    }

    public static String getShield() {
        return SHIELD[0][0]
                + ","
                + SHIELD[0][1]
                + ","
                + SHIELD[0][2]
                + ","
                + SHIELD[0][3]
                + ","
                + SHIELD[1][0]
                + ","
                + SHIELD[1][1]
                + ","
                + SHIELD[1][2]
                + ","
                + SHIELD[1][3]
                + ","
                + SHIELD[2][0]
                + ","
                + SHIELD[2][1]
                + ","
                + SHIELD[2][2]
                + ","
                + SHIELD[2][3]
                + ","
                + NewClan.timeDelayOpenShield[0]
                + ","
                + NewClan.timeDelayOpenShield[1]
                + ","
                + NewClan.timeDelayOpenShield[2]
                + ","
                + newDay;
    }

    public static String getGov(int country) {
        return Char.gov[country].getInfo();
    }

    public void setX2Country(int totalTime) {
    }

    public static void selectionSort(Vector<CharInfo> top) {
        int n = top.size();

        for (int j = 0; j < n - 1; j++) {
            int iMin = j;
            CharInfo c1 = (CharInfo) top.get(j);

            for (int i = j + 1; i < n; i++) {
                CharInfo c2 = (CharInfo) top.get(i);
                if (c2.pk > c1.pk) {
                    iMin = i;
                }
            }

            if (iMin != j) {
                CharInfo c2 = (CharInfo) top.get(j);
                CharInfo c3 = (CharInfo) top.get(iMin);
                top.set(iMin, c2);
                top.set(j, c3);
            }
        }
    }

    public void removeArena(int idParty) {
        Wedding wd = (Wedding) allWedding.get(idParty);
        if (wd != null) {
            Char player = null;

            for (int userid : wd.listMember.values()) {
                try {
                    player = CharManager.instance.getByUserID(userid);
                    if (player != null) {
                        player.idWedding = -1;
                        player.rcvGiftWedding = 0;
                    }
                } catch (Exception var7) {
                }
            }
        }

        allWedding.remove(idParty);
    }

    public ArenaData getArena(int id) {
        return null;
    }

    public TongKimData getArenaTongKim(int id) {
        return null;
    }

    public void addEffBuffToMap(int id, long time, int x, int y, int country) {
        try {
            EffectBuff ef = new EffectBuff(id, time);
            ef.x = (short) x;
            ef.y = (short) y;
            ef.idCountry = (byte) country;
            ef.mapID = this.mapId;
            ((Vector) this.ALL_EFFECT_INMAP.get(country)).add(ef);
            Message msg = MessageCreator.createMsgAddDynamicNewEffect(ef.idEff, ef.timeExist, 0, 32002, x, y, 127, ef.getODER_PAINT(), ef.damFly);
            this.sendAllPlayer(msg, country);
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    public void removeClanMap(int clanId) {
    }

    public void addClanToMap(int clanid) {
    }

    public Item dropItemColor(int levelMonster, int idCountry) {
        return null;
    }

    protected void doProcessNpcClan(Char player, int idNpc, int idMenu, int idOptionMenu) {
    }

    public void onActorDie(LiveActor ac) {
    }

    public boolean isvanTienTran() {
        return false;
    }

    public static boolean isKMLuongKhoa() {
        String day = Char.getDayOpen(0L);
        return day.equals("2015-01-29") || day.equals("2015-01-30") || day.equals("2015-01-31");
    }

    public boolean checkBossLive(int country) {
        return false;
    }

    public void addMonsterDun() {
    }

    public void sendInfoChiemThanh() {
    }

    public void checkStartVanTienTran() {
    }

    public static synchronized boolean isTimeBuyChoi() {
        if (totalChoiBan <= 0) {
            return false;
        } else if (timeSellChoi > 0L && System.currentTimeMillis() - timeSellChoi >= 0L) {
            timeSellChoi = System.currentTimeMillis() + (long) ((r.nextInt(3) + 3) * 60000);
            return true;
        } else {
            return false;
        }
    }

    public static void initTimeSellChoi() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        if (iHour == 21 && timeSellChoi == 0L) {
            timeSellChoi = System.currentTimeMillis() + (long) ((r.nextInt(25) + 15) * 60000);
        }
    }

    public boolean isMapTrain() {
        return this.mapIDLoadMap != 203
                && this.mapIDLoadMap != idMapChanNui
                && this.mapId != idMapChanNui
                && this.mapId != idMapChoLoiDai
                && this.mapId != idMapLoiDai;
    }

    public boolean isMapBoss() {
        return false;
    }

    public boolean isMapChienTruongMoba() {
        return false;
    }

    public boolean isMapLoiDai() {
        return false;
    }

    public boolean isMapNuiChauBau() {
        return false;
    }

    public boolean isMapChanNui() {
        return this.mapId == idMapChanNui || this.mapIDLoadMap == idMapChanNui;
    }

    public boolean isMapChoLoiDai() {
        return this.mapId == idMapChoLoiDai || this.mapIDLoadMap == idMapChoLoiDai;
    }

    public void doResetChienTruong() {
    }

    public void doGenCapChaInput(Char player) {
        String tem = "abcdefghijklmnopqrstwxyz";
        String num = "0123456789";
        player.diachi = "";

        for (int i = 0; i < 5; i++) {
            if (i > 0 && i % 2 == 0) {
                player.diachi = player.diachi + num.charAt(r.nextInt(num.length()));
            } else {
                player.diachi = player.diachi + tem.charAt(r.nextInt(tem.length()));
            }
        }

        player.sendMessage(MessageCreator.createMsgInputText(-32019, 0, "Nhập " + player.diachi + " để xác nhận", 0));
    }
}
