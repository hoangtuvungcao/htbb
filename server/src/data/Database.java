package data;

import io.Session;

import real.Map;
import real.*;
import server.TeamServer;
import util.Logger;
import io.Message;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.*;



public class Database {

    private static String LINK;
    private static String LINK1;
    public static Database instance;
    List<Connection> connections;
    public static ConnPool connPool;
    public static ConnPool connPool1;
    public static ConnPool connPoolNap;
    public static ConnPool connGiftCode;
    List<Boolean> busy;
    public static String dayCreateRace;
    public static String[] nameTinhThanh;
    public static byte[][] basicPoint;
    public static SMSProvider smsNapMe;
    public static SMSProvider smsNapVTC;
    public static Vector<String> agent;
    public static Hashtable<String, CCUProvider> allProvider;
    public static Vector<String> badWord;
    public static String tableRace;
    public static byte createRace;
    private static SimpleDateFormat dateFormat;
    public static Vector<NewClan> allClanDB;
    public static DateFormat timeFormat;
    public String curWeekMonthYear;
    public static String curMonthYear;
    static String nameTableTopLoiDai;
    static String nameTableThachDau;
    static String[] tableNameWeek_month_year;
    static String[] tableNamemonth_year;
    static String curWeekMonthYearDrop;
    static String curMonthYearDrop;
    Vector<String> sqlUpdate;
    boolean stop;
    private int timeXP;
    Vector<ItemTemplates> templateTest;
    public static Random r;
    static String[] name;
    int sid;
    int mid;
    boolean notFalse;
    public static String[] namechar;
    int update;
    public static CharTemplate[] charTemplatesOld;
    public static CharTemplate[][] charTemplates;
    public static Vector<CharInfo> all_char_top;
    Hashtable<String, infoHack> hack;
    public static String[] nameMons_indo;
    public static String[] nameItem_indo;
    public static String[] name_att_in;
    public static String[] nameServer;
    public static boolean ENABLE_ACTIVE_CHECK = true;  // bật tắt kích hoạt  //false đóng //true bật
    // So nhan vat toi da moi tai khoan (client chi ve duoc 3 slot o man chon nhan vat).
    // Doc tu server.ini: sv.maxCharPerAccount
    public static int MAX_CHAR_PER_ACCOUNT = 3;

    static {
        Database.instance = new Database();
        Database.dayCreateRace = "2013-07-04";
        Database.nameTinhThanh = new String[]{"Cần Thơ", "Đà Nẵng", "Hải Phòng", "Hà Nội", "TP HCM", "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái", "Phú Yên"};
        Database.basicPoint = new byte[][]{{25, 20, 10, 25, 10}, {30, 20, 10, 20, 10}, {10, 25, 35, 10, 10}, {20, 30, 10, 20, 10}, {20, 30, 15, 15, 10}};
        Database.smsNapMe = new SMSProvider(0);
        Database.smsNapVTC = new SMSProvider(1);
        Database.agent = null;
        Database.allProvider = new Hashtable<>();
        Database.badWord = new Vector<>();
        Database.createRace = 0;
        Database.dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Database.allClanDB = new Vector<>();
        Database.timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Database.nameTableTopLoiDai = "tob_qua_loi_dai";
        Database.nameTableThachDau = "tob_char_thach_dau";
        Database.tableNameWeek_month_year = new String[]{    "tob_log_use_luong"};
        Database.tableNamemonth_year = new String[]{"tob_nap_xai_luong"};
        Database.curWeekMonthYearDrop = "";
        Database.curMonthYearDrop = "";
        Database.r = new Random(System.currentTimeMillis());
        Database.name = new String[]{"vloz123", "kiepxike", "hylap", "ophongthan", "ocma", "okgaden", "hoangty72", "thienlong", "phuyeumai", "thuylong39", "bjnhnho113", "longhommm", "hotgirl75", "kissss", "hodaica", "huong6123", "tieuyen", "ccuibap", "ochuongheh", "nhoxat", "yeutaokg", "longo", "khungnha", "moonlove", "kimnguu", "iloveyou", "ranhoma", "rongchua", "kopato", "saubo", "thjetthu00", "ngoc84", "dajkaquy", "nututien", "pronqsh", "quan1986", "voodka", "kjmhoang", "buataa", "girlngheo", "athjenloj", "colong007", "tienhoangx", "songvedem", "icarus"};
        Database.namechar = new String[]{"luckystars@@", "colongzz", "xzhellozx", "fulever", "shdida ", "zzongnoizz", "acetatel", "Mexahoi", "anhkhoa15", "1thekybuon", "thuypro", "zzbaodaozz", "Zhieu7t7", "Zingpro ", "Dausi190 ", "tecong02", "lonthjenha ", "thienson@@", "37rong ", "emcodon01 ", "kekhacmau ", "vinhhuong", "dausi1966", "Dapdaups", "daothan35", "0ocbo0 ", "zznokiazz@@", "maydetaoxu ", "daovodanh ", "cafeden ", "donkihote@@", "rongconno1@@", "thjetthu@@", "hang9doi", "muavaban", "dytnhau24h", "pequynh00", "daophu6789", "hacbua6789 ", "alacuopday", "minhxin", "gogogo6789", "mekiepvtc", "mecua5de", "police6789", "34queanh", "remix6789", "34hero6789 ", "34hero", "x0son0x ", "mexahoi ", "ztanlongz ", "remix6789", "laoliethoa", "zzongnoizz@@", "thuypro@@", "meocon12@@", "tutinh ", "lag9", "daothan35 ", "dapdaups", " zrose@@", "sockill ", "girlvipvn@@", "longthjenha", "Zkieunuz@@", "girlbuidn", "boxiii@@", "nhiepphong@@", "yeuvotao", "boyanchoi1", "nlccvtc", "kingboss@@", "hotboyso5", "zpkgapkz", "phatieu1"};
        Database.charTemplatesOld = new CharTemplate[]{new CharTemplate(0, -1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(0, -1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(0, -1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, 1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(0, 1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, 1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(0, 1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, 1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)};
        Database.charTemplates = new CharTemplate[][]{{new CharTemplate(0, -1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 3, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 4, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 5, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 6, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}, {new CharTemplate(0, -1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10), new CharTemplate(1, -1, 7, 200, 200, 10, 10, 100, 100, 1, 10, 10, 10, 10, 10)}};
        Database.all_char_top = new Vector<>();
        Database.nameMons_indo = new String[]{"Landak", "Dalam", "Tetesan Air", "Ayam Gila", "Ular berbisa", "Bara Api", "Teko", "Setan kutu daun", "Tikus", "Bunga Iblis", "Ulat", "Hantu Api", "Mega Manikam", "Ayam", "Babi", "Kalajengking", "Penyu Merah", "Laba-laba Ungu", "Jamur Payung", "Beruang Iblis", "Lipan", "Ayam raksasa", "Ular raksasa", "Kupu-kupu raksasa", "Hantu", "Label kartu", "Tengkorak", "Harimau Raksasa", "Buaya", "Katak Iblis", "Jangkrik Raksasa", "Jantan", "Kerbau Gunung", "Bandit", "Bajak Laut", "Penyihir", "Monyet Kristal", "Berunding", "Kadal", "Kelabang Merah", "Kelabang Ungu", "Kelabang Hijau", "Ayam kelabu", "Statistik kematian", "Statistik gagal", "Api padat", "Kobra", "Singa", "Bekicot", "Kupu-kupu Coklat", "Mayat hidup", "Penghisap darah", "Gabus Lumpur", "Kepiting Besar", "Mata Satu", "Ikan Hantu", "Mata Merah", "Iblis bunga", "Tengkorak", "Badak", "Macan ungu", "Harimau hijau", "Macan merah", "Buaya merah", "Kepiting kuku", "Buaya hijau", "Kodok Hijau", "Katak Api", "Katak Hijau", "Jangkrik muda", "Jangkrik merah", "Jangkrik hitam", "Kerbau merah", "Manusia Batu", "Kerbau Hijau", "Siput", "Kepik", "Bandit 3", "Bajak Laut 1", "Jamur beracun", "Sayap oranye", "Burung elang", "Pilar Lengkung", "Zombie", "Batu", "Kapas", "Kayu", "Kulit", "Besi", "Manusia Salju", "Gadis Perkasa", "Anak Perkasa", "Mata Iblis", "Janggut Putih Sakti", "Kupu-kupu ungu", "Kupu-kupu biru"};
        Database.nameItem_indo = new String[]{"", "Baju Petani", "(Tanpa baju)", "Baju Kasar (Wanita)", "Baju Kasar (Pria)", "Baju Brokat (Wanita)", "Baju Brokat (Pria)", "Baju Katun (Wanita)", "Baju Katun (Pria)", "Baju Biasa sutra (Wanita)", "Baju Biasa sutra (Pria)", "Baju Sutra mewah (Wanita)", "Baju Sutra mewah (Pria)", "Baju Beludru (Wanita)", "Baju Beludru (Pria)", "Baju Bordir sutra (Wanita)", "Baju Bordir sutra (Pria)", "Baju Berkilau (Wanita)", "Baju Berkilau (Pria)", "Baju Mempesona (Wanita)", "Baju Mempesona (Pria)", "Baju Harimau (Wanita)", "Baju Harimau (Pria)", "Baju Singa (Wanita)", "Baju Singa (Pria)", "Baju Naga Emas (Wanita)", "Baju Naga Emas (Pria)", "Celana petani", "Celana pendek", "Celana kasar (Wanita)", "Celana kasar (Pria)", "Celana Brokat (Wanita)", "Celana Brokat (Pria)", "Celana Katun (Wanita)", "Celana Katun (Pria)", "Celana sutra biasal (Wanita)", "Celana sutra biasal (Pria)", "Celana Sutra mewah (Wanita)", "Celana Sutra mewah (Pria)", "Celana Beludru (Wanita)", "Celana Beludru (Pria)", "Celana Bordir sutra (Wanita)", "Celana Bordir sutra (Pria)", "Celana Berkilau (Wanita)", "Celana Berkilau (Pria)", "Celana Mempesona (Wanita)", "Baju Mempesona (Pria)", "Celana Harimau (Wanita)", "Celana Harimau (Pria)", "Celana Singa (Wanita)", "Celana Singa (Pria)", "Celana Naga Emas (Wanita)", "Celana Naga Emas (Pria)", "Ikat kepala", "Syal", "Topi Jubah berduri (Wanita)", "Topi Jubah berduri (Pria)", "Topi Bambu biru (Wanita)", "Topi Bambu biru (Pria)", "Topi Bambu Hitam (Wanita)", "Topi Bambu Hitam (Wanita)", "Topi Bambu hijau (Wanita)", "Topi Bambu hijau (Pria)", "Topi Biru kain (Wanita)", "Topi Biru kain (Pria)", "Topi Hitam tembaga (Wanita)", "Topi Hitam tembaga (Pria)", "Topi Tembaga merah (Wanita)", "Topi Tembaga merah (Pria)", "Topi besi hitam (Wanita)", "Topi besi hitam (Pria)", "Topi besi kelabu (Wanita)", "Topi besi kelabu (Pria)", "Topi Hati Sempurna (Wanita)", "Topi Hati Sempurna (Pria)", "Topi Pohon Pinus (Wanita)", "Topi Pohon Pinus (Pria)", "Topi Naga Emas (Wanita)", "Topi Naga Emas (Pria)", "Pedang Bambu", "Pedang kayu", "Pedang Tembaga", "Pedang Besi", "Pedang Tembang Lama", "Pedang Kesatuan", "Pedang Bintang", "Tombak Bambu", "Tombak kayu", "Tombak Tembaga", "Tombak Besi", "Tombak Buaya", "Tombak Kebijaksanaan", "Tombak Fenix", "Tongkat Bambu", "Tongkat kayu", "Tongkat Tembaga", "Tongkat besi", "Tongkat Nasib", "Tongkat Gravity", "Tongkat Mata Dewa", "Palu Bumi", "Palu kayu", "Palu Tembaga", "Palu Besi", "Palu Kepala Kerbau", "Palu Wajah Kuda", "Palu Arwah Bumi", "Busur Bambu", "Busur kayu", "Busur Tembaga", "Busur Besi", "Busur Bulan Sabit", "Busur Fenix Emas", "Busur Selatan", "Cincin Citrine", "Cincin Saphia", "Cincin Amber", "Cincin Turquoise", "Cincin Peridot", "Cincin Opal", "Cincin Giok", "Cincin Ruby", "Cincin Acient", "Cincin Aquamarine", "Cincin Ruby", "Cincin Moonstone", "Kalung Tembaga", "Kalung Koral", "Kalung kayu hijau", "Kalung Mata Kucing", "Kalung Platinum", "Kalung Kristal", "Kalung mutiara", "Kalung Amber", "Kalung perak", "Kalung Giok", "Batu Permata", "Kalung Fenix", "Sepatu kasar (Wanita)", "Sepatu Brokat (Wanita)", "Sepatu Katun (Wanita)", "Sepatu sutra biasa (Wanita)", "Sepatu mewah sutra (Wanita)", "Sepatu Beludru (Wanita)", "Sepatu Bordir sutra (Wanita)", "Sepatu Berkilau (Pria)", "Sepatu Mempesona (Wanita)", "Sepatu Harimau (Wanita)", "Sepatu Singa (Wanita)", "Sepatu Naga Emas (Wanita)", "Sarung tangan kasar (Wanita)", "Sarung tangan Brokat (Wanita)", "Sarung tangan Katun (Wanita)", "Sarung tangan sutra biasa (Wanita)", "Sarung tangan sutra mewah (Wanita)", "Sarung tangan Beludru (Wanita)", "Sarung tangan sutra bordir (Wanita)", "Sarung tangan Berkilau (Pria)", "Sarung tangan menyilaukan (Wanita)", "Sarung tangan Harimau (Wanita)", "Sarung tangan Singa (Wanita)", "Sarung tangan Naga Emas (Wanita)", "Permata Giok", "Permata Perak", "Permata Sempurna", "Permata Menara Emas", "Permata Kupu-kupu", "Permata Bunga biru", "Permata Peri", "Permata merah bundar", "Permata Purnama", "Permata Sakura", "Permata Aprikot", "Permata Peri Naga", "Pedang Bayu Biru", "Pedang Itik Lengkap", "Pedang Merah Darah", "Pedang Bunga", "Pedang Besi Hitam", "Pedang Titisan", "Pedang Macan", "Pedang Walet suci", "Pisau Iblis", "Pisau Pemotongan Bulan", "Pisau Belah Topan", "Pisau Mukjizat", "Pisau Air Mata Mutiara", "Pisau Tornado", "Pisau Baja Bersama", "Pisau Neraka", "Tongkat Junjung Metalik", "Tongkat Sakura", "Tongkat Telepati", "Tongkat Racun", "Tongkat Angry Angin", "Tongkat Bintang Qurban", "Tongkat Licin berkarat", "Tongkat Penangkap Angin", "Palu Tulang", "Palu Top Clumber", "Palu Delapan Ujung", "Palu Lima Bintang", "Palu Guntur Api", "Palu Raksa Keajaiban", "Palu Pisah Inti", "Palu Api Naga", "Busur Titik depan", "Busur Walet", "Busur Terbang Fenix", "Busur Hijau Limo", "Busur Raksa Keajaiban", "Busur Kupu-kupu", "Busur Bersayap Terbang", "Busur Surga", "Pedang Tembang Lama (Sewa)", "Pedang Bersatu (Sewa)", "Pedang Bintang (Sewa)", "Pedang Angin Biru (Sewa)", "Pedang Lengkap Drake (Sewa)", "Pedang Tujuh Rantai (Sewa)", "Pedang Bunga (Sewa)", "Pedang besi hitam (Sewa)", "Pedang titisan (Sewa)", "Pedang macan tutul (Sewa)", "Pisau Gerak (Sewa)", "Pisau Intel (Sewa)", "Pisau Fenix (Sewa)", "Pisau Setan Kepala (Sewa)", "Pisau Pemotongan Bulan (Sewa)", "Pisau Topan Breaker (Sewa)", "Pisau Mujizat (Sewa)", "Pisau Pearl Air Mata (Sewa)", "Pisau Tornado (Sewa)", "Pisau Baja Bersama (Sewa)", "Pena Takdir", "Tongkat Serenade Call (Sewa)", "Tongkat Allah Eye (Sewa)", "Tongkat Metalic Tribute (Sewa)", "Tongkat Sakura (Sewa)", "Tongkat telepati (Sewa)", "Tongkat Racun (Sewa)", "Tongkat Angry Angin (Sewa)", "Tongkat Bintang Qurban (Sewa)", "Tongkat Slick terkorupsi (Sewa)", "Palu Buffalo Kepala (Sewa)", "Palu Kuda Wajah (Sewa)", "Palu Bumi Roh (Sewa)", "Palu Tulang (Sewa)", "Palu Top Clumber (Sewa)", "Palu Delapan Ujung (Sewa)", "Palu Five Star (Sewa)", "Palu Guntur Api (Sewa)", "Palu Raksa Keajaiban (Sewa)", "Palu Inti Seperater (Sewa)", "Busur Bulan Sabit (Sewa)", "Busur Fenix Emas (Sewa)", "Busur Selatan (Sewa)", "Busur Titik depan (Sewa)", "Busur Walet (Sewa)", "Busur Terbang Fenix (Sewa)", "Busur Hijau Li (Sewa)", "Busur Raksa Keajaiban (Sewa)", "Busur Kupu-kupu (Sewa)", "Busur Bersayap Terbang (Sewa)", "Baju Peri", "Baju Naga King", "Baju Old guru", "Gaun panjang", "Baju Biru Sempurna (Wanita)", "Baju Biru Sempurna (Pria)", "Baju Marun (Wanita)", "Baju Marun (Pria)", "Baju Banyak (Wanita)", "Baju Banyak (Pria)", "Baju Ular Suci (Wanita)", "Baju Ular Suci (Pria)", "Baju Berwarna (Wanita)", "Baju Berwarna (Pria)", "Baju Setan Jiwa (Wanita)", "Baju Setan Jiwa (Pria)", "Baju Gelap macan tutul (Wanita)", "Baju Gelap macan tutul (Pria)", "Baju Es Roh (Wanita)", "Baju Es Roh (Pria)", "Baju Tujuh Harta (Wanita)", "Baju Tujuh Harta (Pria)", "Baju Lotus (Wanita)", "Baju Lotus (Pria)", "Baju Perak Sutra (Wanita)", "Baju Perak Sutra (Pria)", "Baju Abadi (Wanita)", "Baju Abadi (Pria)", "Celana Biru Sempurna (Wanita)", "Celana Biru Sempurna (Pria)", "Celana Marun (Wanita)", "Celana Marun (Pria)", "Celana cukup (Wanita)", "Celana cukup (Pria)", "Celana Ular Suci (Wanita)", "Celana Ular Suci (Pria)", "Celana berwarna (Wanita)", "Celana berwarna (Pria)", "Celana Setan Jiwa (Wanita)", "Celana Setan Jiwa (Pria)", "Celana Gelap macan tutul (Wanita)", "Celana Gelap macan tutul (Pria)", "Celana Roh es (Wanita)", "Celana Roh es (Pria)", "Celana Tujuh Harta (Wanita)", "Celana Tujuh Harta (Pria)", "Celana Lotus (Wanita)", "Celana Lotus (Pria)", "Celana Perak Sutra (Wanita)", "Celana Perak Sutra (Pria)", "Celana Abadi (Wanita)", "Celana Abadi (Pria)", "Topi Biru Sempurna (Wanita)", "Topi Biru Sempurna (Pria)", "Topi Marun (Wanita)", "Topi Marun (Pria)", "Topi Cukup (Wanita)", "Topi Cukup (Pria)", "Topi Ular Suci (Wanita)", "Topi Ular Suci (Pria)", "Topi Berwarna (Wanita)", "Topi Berwarna (Pria)", "Topi Setan Jiwa (Wanita)", "Topi Setan Jiwa (Pria)", "Topi Gelap macan tutul (Wanita)", "Topi Gelap macan tutul (Pria)", "Topi Semangat Beku (Wanita)", "Topi Semangat Beku (Pria)", "Topi Tujuh Harta (Wanita)", "Topi Tujuh Harta (Pria)", "Topi Lotus (Wanita)", "Topi Lotus (Pria)", "Topi Perak Sutra (Wanita)", "Topi Perak Sutra (Pria)", "Topi Abadi (Wanita)", "Topi Abadi (Pria)", "Sepatu Biru Sempurna", "Sepatu Marun", "Sepatu yang cukup", "Sepatu Ular Suci", "Sepatu berwarna", "Sepatu Iblis Jiwa", "Sepatu Gelap macan tutul", "Sepatu Semangat Beku", "Sepatu Tujuh Harta", "Sepatu Lotus", "Sepatu Perak Sutra", "Sepatu Abadi", "Sarung tangan Biru Sempurna", "Sarung tangan Marun", "Sarung tangan yang cukup", "Sarung tangan Ular Suci", "Sarung tangan berwarna", "Sarung tangan Iblis Jiwa", "Sarung tangan Gelap macan tutul", "Sarung tangan Roh Es", "Sarung tangan Tujuh Harta", "Sarung tangan Lotus", "Sarung tangan Perak Sutra", "Sarung tangan Abadi", "Cincin Batu Akik", "Cincin Ular Suci", "Cincin Pearl", "Cincin Batu Perak", "Cincin Koral", "Cincin Lima Warna", "Cincin Badak Hitam", "Cincin Gajah", "Cincin Yin Yang", "Cincin Mentari Bulan", "Cincin Ganda Fenix", "Cincin Ganda Naga", "Kalung batu", "Kalung Ular Suci", "Kalung mutiara", "Kalung Perak Gem", "Kalung Koral", "Kalung Lima Warna", "Kalung Badak Hitam", "Kalung gajah", "Kalung Yin Yang", "Kalung Mentari Bulan", "Kalung Ganda Fenix", "Kalung Ganda Naga", "Batu berlian", "Permata Ular Suci", "Permata Pearl", "Permata Perak Gem", "Permata Koral", "Permata Lima Warna", "Permata Badak Hitam", "Permata Gajah", "Permata Yin Yang", "Permata Mentari Bulan", "Permata Ganda Fenix", "Permata Ganda Naga", "Pedang Merak", "Pedang Sekilas", "Pedang Api Nirwana", "Pedang Bunga Hutan", "Pedang Ekor Rubah", "Pedang Laut", "Pedang emas", "Pedang Timah", "Pedang Hitam Putih", "Pedang Bunga Salju", "Pedang Kekasih", "Pedang Opal", "Pedang Tempur", "Tombak kuda", "Tombak Bara", "Tombak Gigi Serigala", "Pisau Sepuluh Palang", "Pisau Termahsyur", "Pisau Standard", "Pisau Petugas", "Pisau Bulan Suram", "Pisau Angin Biru", "Pisau Kerajaan", "Pisau Penghargaan", "Pisau Kaisar", "Pisau Pasir", "Tongkat berat", "Tongkat Walet Terbang", "Tongkat Langit", "Tongkat Timah", "Tongkat Angsa Terbang", "Tongkat Bola Perak", "Tongkat Rantai", "Tongkat Segi empat", "Tongkat Biru Firman", "Tongkat Cerdas", "Tongkat Merah Mempesona", "Tongkat Keadilan", "Tongkat Penggetar", "Palu Cerah", "Palu Setengah Wajah", "Palu Beras Perak", "Palu Tembaga", "Palu Emas Kain", "Palu Emas Tembaga", "Palu Rusuh", "Palu Lengan Panjang", "Palu Wajah Ganda", "Palu Deary", "Palu Syal Beludru", "Palu Buddha", "Palu Lingkaran", "Palu Surai Ayam", "Busur Fenix biru", "Busur Rose", "Busur Hutan", "Busur Univer", "Busur iblis biru", "Busur Iblis Serigala", "Busur Lapis", "Busur spektakuler", "Busur Benang Emas", "Busur Biru Bunga", "Busur Tulang", "Busur Nuri", "Cangkul biasa", "Cangkul Baik", "Baju Menari Wing (Wanita)", "Baju Menari Wing (Pria)", "Baju Cahaya Ungu (Wanita)", "Baju Cahaya Ungu (Pria)", "Baju Beruang besar (Wanita)", "Baju Beruang besar (Pria)", "Baju Walet Terbang (Wanita)", "Baju Walet Terbang (Pria)", "Celana Naga terbang (Wanita)", "Celana Naga terbang (Pria)", "Celana Cahaya ungu (Wanita)", "Celana Cahaya ungu (Pria)", "Celana Beruang besar (Wanita)", "Celana Beruang besar (Pria)", "Celana Terbang layang (Wanita)", "Celana Terbang layang (Pria)", "Topi Naga terbang (Wanita)", "Topi Naga terbang (Pria)", "Topi Ungu terang (Wanita)", "Topi Ungu terang (Pria)", "Topi Beruang besar (Wanita)", "Topi Beruang besar (Pria)", "Topi Terbang layang (Wanita)", "Topi Terbang layang (Pria)", "Sepatu Naga terbang", "Sepatu Ungu terang", "Sepatu Beruang besar", "Sepatu Walet Terbang", "Sarung tangan Naga terbang", "Sarung tangan Ungu terang", "Sarung tangan Beruang besar", "Sarung tangan Walet Terbang", "Cincin Naga terbang", "Cincin Ungu terang", "Cincin Beruang besar", "Cincin Walet Terbang", "Kalung Naga Terbang", "Kalung Cahaya ungu", "Kalung Beruang besar", "Kalung Walet Terbang", "Syal Merah", "Baju Ksatria hutan", "Topi Keberanian", "Topi Jantung Es", "Topi Dinamis", "Topi Dewata", "Topi Ruang", "Topi Ritual", "Topi Bulu", "Topi Raksasa", "Topi Kuat", "Topi Berawan", "Topi Kuat", "Topi Baik", "Pelindung Keberanian", "Pelindung Jantung Es", "Pelindung Dinamis", "Pelindung Dewata", "Pelindung Ruang", "Pelindung Ritual", "Pelindung Bulu", "Pelindung Raksasa", "Pelindung kokoh", "Pelindung Berawan", "Pelindung kekuatan", "Pelindung sempurna", "Pelana Keberanian", "Pelana Jantung Es", "Pelana Dinamis", "Pelana Dewata", "Pelana Ruang", "Pelana Ritual", "Pelana Bulu", "Pelana Raksasa", "Pelana kuat", "Pelana Berawan", "Pelana kuat", "Pelana Baik", "Pelana Keberanian", "Pelana Jantung Es", "Pelana Dinamis", "Pelana Dewata", "Pelana Ruang", "Pelana Ritual", "Pelana Bulu", "Pelana Raksasa", "Pelana kuat", "Pelana Berawan", "Pelana kuat", "Pelana Baik", "Pedal Keberanian", "Pedal Jantung es", "Pedal dinamis", "Pedal Dewata", "Pedal Ruang", "Pedal Ritual", "Pedal Bulu", "Pedal Raksasa", "Pedal kuat", "Pedal Berawan", "Pedal kuat", "Pedal Baik", "Permata Naga Terbang", "Permata Ungu terang", "Permata Beruang besar", "Permata Walet Terbang", "Kalung Naga Terbang", "Baju Peri Bulan (Wanita)", "Baju Dracula (Pria)", "Kemeja Lelaki Bulan", "Orang bodoh", "Topi Penyihir", "Baju Noel (Wanita)", "Baju Noel (Pria)", "Topi Noel", "Gaun panjang (Wanita)", "Gaun panjang (Pria)", "Mahkota", "Topi Magneta", "Topi General", "Topi Kapten", "Sepatu kasar (Pria)", "Sepatu Brokat (Pria)", "Sepatu Kapas (Pria)", "Sepatu sutra biasal (Pria)", "Sarung tangan kasar (Pria)", "Sarung tangan Brokat (Pria)", "Sarung tangan Katun (Pria)", "Sarung tangan sutra biasa (Pria)", "Sepatu mewah sutra (Pria)", "Sepatu Beludru (Pria)", "Sepatu Bordir sutra (Pria)", "Sepatu Berkilau (Pria)", "Sarung tangan mewah sutra (Pria)", "Sarung tangan Beludru (Pria)", "Sarung tangan Bordir sutra (Pria)", "Sarung tangan Berkilau (Pria)"};
        Database.name_att_in = new String[]{"", "Serangan", "Hewan", "Menghindar", "Benar sekali", "Kritis", "Kesehatan", "Hantu Utama", "Indikator 7", "Indikator 8", "Indeks 9", "Tambah kekuatan", "Tambah kelincahan", "Tambah semangat", "Tambah kesehatan", "Kebutaan", "Lapisan Es", "Meracun", "Membius", "Fosil", "Melambat", "Anti pelambat", "Anti racun", "Anti kebutaan", "Anti beku", "Anti setrum", "Anti fosil", "Meningkatkan hit 2x lipat", "Tambah penurunan kadar harta", "Bahan kurangi kerusakan", "Kurangi kerusakan hantu", "Meningkatkan serangan", "Baja", "Respon Kerusakan", "Tingkatkan HP", "Tingkatkan MP", "Tingkatkan kekuatan", "Tingkatkan kelincahan", "Tingkatkan moral", "Tingkatkan kesehatan", "Tingkatkan skil", "Tingkatkan fatal", "Tingkatkan kritis", "berisi", "1 Jurus tambahan", "2 Jurus tambahan", "3 Jurus tambahan", "4 Jurus tambahan", "5 Jurus tambahan", "Jurus 6 ditambah", "Jurus 7 ditambah", "Jurus 8 ditambah", "9 Jurus tambahan", "Jurus 10 ditambah", "Jurus 11 ditambah", "Jurus 12 ditambah", "Jurus 13 ditambah", "Jurus 14 ditambah", "Jurus 15 ditambah", "Tambah", "Tambah pelindung ajaib", "Tambah pelindung", "Abaikan Serangan Fantom", "Lewati Serangan Hewan", "Tambah alat perlindungan", "Tambah alat perlindungan", "Klik pertama"};
        Database.nameServer = new String[]{"lc", "tl", "tt", "hm", "ms", "km", "cb", "hl", "cb", "cb"};
    }
    

   
    
    public void updateCountBoss(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO top_boss (charname, count) VALUES (?, 1) " +
                        "ON DUPLICATE KEY UPDATE count = count + 1";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, charname);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) Database.connPool.free(conn);
            } catch (Exception ignored) {}
        }
    }

    public void saveCooking(Cooking cook, int id) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            String sql = "update tob_nau_banh set listchar=?,timecook=?,water=?,chamnc=?,listCharGift=? where id=" + id;
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, cook.getListChar());
            pre.setInt(2, cook.timeCook);
            pre.setInt(3, cook.water);
            pre.setInt(4, cook.timeChamnc);
            pre.setString(5, cook.getListCharGift());
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetCooking() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "update tob_nau_banh set listchar='',listCharGift='',timecook=60,water=0,chamnc=0";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }




    public Cooking loadAllCharCooking(int id) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_nau_banh where id=" + id;
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            if (rs.next()) {
                Cooking cook = new Cooking();
                cook.timeChamnc = rs.getByte("chamnc");
                cook.timeCook = rs.getByte("timecook");
                cook.water = rs.getByte("water");
                cook.setListCharCooking(rs.getString("listchar"));
                cook.setListCharGift(rs.getString("listCharGift"));
                cook.type = (byte) id;
                return cook;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void updateSoLuongChoiBan() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update tob_x2exp set banchoi=" + Map.totalChoiBan;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateLuongNap(int luong, Char player) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update `tob_char` set `topNap` = `topNap` + '" + luong + "' where charname = '" + player.charname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    // Than method (118 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void loadTimeX2Server() {
        DatabaseXepHang.loadTimeX2Server(this);
    }

    
    

    public static boolean haveBadWord(String infoCheck) {
        infoCheck = infoCheck.toLowerCase();
        for (int i = 0; i < Database.badWord.size(); ++i) {
            String st = Database.badWord.get(i);
            if (infoCheck.indexOf(st) != -1) {
                return true;
            }
        }
        return false;
    }

    public boolean isFullConnection() {
        return Database.connPool.countConnection >= Database.connPool.maxConn;
    }

    public void updateStateReceiveGiftEventOutGame(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "update tob_qua set qua=1 WHERE charname = ?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public byte checkAddGiftEventGame(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_qua WHERE charname = ? and qua=0";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getByte("typeQua");
            }
        } catch (Exception e) {
            Logger.logError(e, "errorlog.txt");
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public boolean baotriCMD() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM 5h_systems WHERE command = ? and status = 0";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, "restart");
            rs = pre.executeQuery();
            if (rs.next()) {
                updateBaoTriCMD();
                return true;
            }
        } catch (Exception e) {
            Logger.logError(e, "errorlog.txt");
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return false;
    }

    public void updateBaoTriCMD() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "update 5h_systems set status=1 WHERE command = ?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, "restart");
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    // ==================== CO BAO TRUOC BAO TRI ====================
    // Deploy tu dong (vps/deploy-auto.sh, goi tu GitHub Actions) dat
    // 5h_systems.status = 0 o dong command='baotri_thongbao' de bao truoc cho nguoi dang
    // online. Khac 'restart': co nay CHI bao, khong tu tat server - script se goi
    // systemctl stop sau do de shutdown hook luu du lieu dang hoang.
    // Doc xong tra co ve 1 ngay, de khong bao lap lai moi 5 giay.
    public boolean baoTriThongBaoCMD() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        boolean co = false;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement("SELECT * FROM 5h_systems WHERE command = ? and status = 0");
            pre.setString(1, "baotri_thongbao");
            rs = pre.executeQuery();
            if (rs.next()) {
                co = true;
            }
        } catch (Exception e) {
            Logger.logError(e, "errorlog.txt");
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        if (co) {
            conn = null;
            pre = null;
            try {
                conn = this.getConnection();
                pre = conn.prepareStatement("update 5h_systems set status=1 WHERE command = ?");
                pre.setString(1, "baotri_thongbao");
                pre.execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
        }
        return co;
    }

    public boolean checkAddGiftEventOutGame(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_qua WHERE charname = ? and qua=0";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            Logger.logError(e, "errorlog.txt");
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean checkReceiveGiftEventOutGame(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_qua WHERE charname = ?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return false;
    }

    public void sendNotiLogin(Char p, Message m) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from 5h_notify";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("owner");
                String content = rs.getString("content");
//                m = new Message(27);
//                m.dos.writeShort(p.id);
//                m.dos.writeUTF(content);
//                p.sendMessage(m);
                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public byte checkAddGiftQuaLoiDai(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_qua_loi_dai WHERE charname = ? and qua=0";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getByte("typeQua");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void backupTobThachDau() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "insert into " + Database.nameTableThachDau + " SELECT * FROM tob_char_thach_dau";
            System.err.println(sql);
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void getTopQuaLoiDai(int idNhom) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM tob_char_thach_dau where nhom=" + idNhom + " and pointrank>=100 order by pointrank desc";
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            int count = 0;
            while (rs.next()) {
                this.addTopQuaLoiDai(rs.getString("charname"), idNhom, (count == 0) ? 1 : 2, rs.getInt("pointrank"));
                ++count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addTopQuaLoiDai(String name, int nhom, int typeQua, long diem) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "insert into " + Database.nameTableTopLoiDai + " (charname,nhom,typeQua,diem) values('" + name + "'," + nhom + "," + typeQua + "," + diem + ")";
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateStateReceiveGiftQuaLoiDai(String charname) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "update tob_qua_loi_dai set qua=1 WHERE charname = ?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadBadWords() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Database.badWord.removeAllElements();
        try {
            final String sql = "select * from bad_word";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                Database.badWord.add(rs.getString("word").toLowerCase());
            }
        } catch (Exception e) {
            System.out.println("KHONG TIM THAY DATABASE");
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
      
    }

    public void loadPercentDrop() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from 5h_systems where command = 'percentDrop'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Monster.percentDrop = rs.getInt("status");
                System.out.println("percentDrop TOAN SERVER = " + Monster.percentDrop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadTileDrop() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from 5h_systems where command = 'tileDrop'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Monster.tileDrop = rs.getInt("status");
                System.out.println("tileDrop TOAN SERVER = " + Monster.tileDrop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadTangNap() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from 5h_systems where command = 'tangnap'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                TileNap = rs.getInt("status");
                System.out.println("TANG NAP TOAN SERVER = " + TileNap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadTangExp() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from 5h_systems where command = 'tangexp'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Char.TangEXP = rs.getInt("status");
                System.out.println("TANG EXP TOAN SERVER = " + Char.TangEXP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public String loadSystemDB(String params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String db = "";
        try {
            final String sql = "select * from 5h_systems where command = '" + params + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                db = rs.getString("status");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return db;
    }

    public void loadAllXosoLow() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from nin_xoso_low";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("charname");
                int money = rs.getInt("money");
                int win = rs.getInt("win");
                int moneyWin = rs.getInt("moneywin");
                int moneyBid = rs.getInt("moneyBid");
                if (money > 0) {
                    PlayerXoso pxs = new PlayerXoso();
                    pxs.name = name;
                    pxs.money = money;
                    Char.ALL_PLAYER_XOSO_low.put(name, pxs);
                    Char.MONEY_XOSO_low += money;
                }
                if (win == 1) {
                    Char.moneyCharXosoBid_low = moneyBid;
                    Char.moneyCharXosowin_low = moneyWin;
                    Char.charNameXosoWin_low = name;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetXosoLow(String charnameWin, int moneyBid, int moneyWin, int moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "update nin_xoso_low set money=0,win=0";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        this.updateWinXosoLow(charnameWin, moneyBid, moneyWin, moneyTaxWin);
    }

    public void updateWinXosoLow(String charnameWin, int moneyBid, int moneyWin, int moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update nin_xoso_low set win=1,moneyBid=" + moneyBid + ",moneywin=" + moneyWin + " where charname='" + charnameWin + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        this.saveOrtherLog("", charnameWin, "chien thang vong quay thuong " + moneyWin + ", tax: " + moneyTaxWin, "xoso");
        this.updateTaxWinXosoLow(moneyTaxWin);
    }

    public void updateTaxWinXosoLow(int moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update nin_xoso_low set moneyTax=moneyTax+" + moneyTaxWin;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void doAddUpdatePlayerXosoLow(String charname, int money) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into nin_xoso_low(charname,money) values ('" + charname + "'," + money + ") ON DUPLICATE KEY UPDATE money=" + money;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }


  
    public void loadAllXoso() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from nin_xoso";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("charname");
                int money = rs.getInt("money");
                int win = rs.getInt("win");
                long moneyWin = rs.getLong("moneywin");
                long moneyBid = rs.getLong("moneyBid");
                if (money > 0) {
                    PlayerXoso pxs = new PlayerXoso();
                    pxs.name = name;
                    pxs.money = money;
                    Char.ALL_PLAYER_XOSO.put(name, pxs);
                    Char.MONEY_XOSO += money;
                }
                if (win == 1) {
                    Char.moneyCharXosoBid = moneyBid;
                    Char.moneyCharXosowin = moneyWin;
                    Char.charNameXosoWin = name;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetXoso(String charnameWin, long moneyBid, long moneyWin, long moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "update nin_xoso set money=0,win=0";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        this.updateWinXoso(charnameWin, moneyBid, moneyWin, moneyTaxWin);
    }

    public void updateWinXoso(String charnameWin, long moneyBid, long moneyWin, long moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update nin_xoso set win=1,moneyBid=" + moneyBid + ",moneywin=" + moneyWin + " where charname='" + charnameWin + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        this.saveOrtherLog("", charnameWin, "chien thang vong quay " + moneyWin + ", tax: " + moneyTaxWin, "xoso");
        this.updateTaxWinXoso(moneyTaxWin);
    }
    public void addCharWinXoSo(Char charWin, long moneyWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into `history_win_xoso` (`id_user`, `money_win`) values (?, ?)";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,charWin.charDBID);
            pstmt.setLong(2,moneyWin);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateTaxWinXoso(long moneyTaxWin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update nin_xoso set moneyTax=moneyTax+" + moneyTaxWin;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void doAddUpdatePlayerXoso(String charname, int money) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into nin_xoso(charname,money) values ('" + charname + "'," + money + ") ON DUPLICATE KEY UPDATE money=" + money;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }


    public void loadNameAttributeItem() {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from data_attribute order by id";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            byte i = 0;
            while (rs.next()) {
                String name = rs.getString("name");
                if (TeamServer.isServerIndo()) {
                    name = rs.getString("namein");
                }
                ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.add(new NameAttributeItem(i, name, rs.getByte("ispercent"), rs.getByte("idcolor")));
                ++i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

   

    public static void checkFirstNap(Char p, Connection conn, boolean online) {
        ResultSet rs = null;
        Statement stm = null;
        try {
            String sql = "select * from tob_get_board_nap where charname='" + p.charname + "'";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            if (!rs.next() && online) {
                p.doReceiveGifNap();
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
    }

    // Than method (91 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void getTopNapLuong() {
        DatabaseXepHang.getTopNapLuong(this);
    }

    public String getFirstNap(Char player) {
        String kq = "";
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stm = conn.createStatement();
            String sql = "select * from tob_first_nap where charname='" + player.charname + "'";
            rs = stm.executeQuery(sql);
            long oldPotion = 0L;
            int oldluong = 0;
            try {
                oldPotion = player.getxu();
                oldluong = player.getLuong();
            } catch (Exception ignored) {
            }
            if (rs.next()) {
                int xu = rs.getInt("xu");
                int luong = rs.getInt("luong");
                player.addXu(xu, "getFirstNap db");
                player.addLuong(luong);
                if (player.firstNapMoney == 0) {
                    player.firstNapMoney = 1;
                }
                String[] info = {"Chúc mừng bạn nhận được ", "You receive more ", "Kamu Akan Mendapatkan 2X Top Up "};
                String[] info2 = {" cho lần nạp đầu tiên", " for the first insert coins", " Di Saat Top Up Pertama Kali"};
                if (xu > 0 && luong > 0) {
                    kq = info[0] + xu + " xu và " + luong + " lượng" + info2[0];
                } else if (xu > 0) {
                    kq = info[0] + xu + " xu " + info2[0];
                } else if (luong > 0) {
                    kq = info[0] + luong + " lượng " + info2[0];
                }
                String sqlUpdate = "update tob_first_nap set xu=0,luong=0 where charname='" + player.charname + "'";
                stm.executeUpdate(sqlUpdate);
                return kq;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return kq;
    }

    public boolean updateMoneyTopThidau(String name, int luong, int xu) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.connPoolNap.getConnection();
            final String sql = "update board_naptien set luong=luong+?,xu=xu+? where username=?";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, luong);
            pre.setInt(2, xu);
            pre.setString(3, name);
            return pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPoolNap.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void insertMoneyTopThiDau(String name, int luong, int xu) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.connPoolNap.getConnection();
            if (!this.updateMoneyTopThidau(name, luong, xu)) {
                final String sql = "insert into board_naptien(username,luong,xu) values(?,?,?) ON DUPLICATE KEY UPDATE luong=luong+?,xu=xu+?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, name);
                pre.setInt(2, luong);
                pre.setInt(3, xu);
                pre.setInt(4, luong);
                pre.setInt(5, xu);
                pre.execute();
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPoolNap.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPoolNap.free(conn);
        } catch (Exception ignored) {
        }
    }

   

    // Than method (590 dong) da chuyen sang DatabaseGiftCode.java. Chu ky GIU NGUYEN.
    public void newDoGetGiftCode(String text, Char player) {
        DatabaseGiftCode.newDoGetGiftCode(this, text,player);
    }


    // ==================== KIEM TRA KICH HOAT TAI KHOAN ====================
    // KHONG DUOC dat Connection vao try-with-resources o ba ham duoi day.
    //
    // ConnPool (trong lib/NQSH_5h.jar) phat ra Connection THO tu DriverManager, khong he
    // boc proxy. Nen conn.close() dong THAT cai socket chu khong tra ket noi ve pool:
    // ket noi da bi removeFirst() ra khoi pool luc lay, va khong bao gio duoc
    // addConToPool() tra lai -> POOL TEO VINH VIEN moi lan goi. Them nua, ConnPool chi
    // giam countConnection trong free(), nen bo dem cung bi keo len mai mai.
    //
    // Ba ham nay nam ngay tren duong DANG NHAP (LoginHandler 293/379/436 va
    // SelectCharHandler 166), tuc khoang 4 lan moi luot vao game — ro nhanh hon han
    // ban Database.banChar cu. Pool chi duoc nap lai khi da can sach
    // (ConnPool.getConFromPool goi initPool khi size()==0), gay burst tao lai maxco
    // ket noi MySQL cung mot luc.
    //
    // Cach dung dung: PreparedStatement/ResultSet van de trong try-with-resources
    // (dong chung la dung), rieng Connection thi tra ve pool bang connPool.free() o
    // finally. Bat Exception chu khong chi SQLException vi getConFromPool() co the tra
    // null (-> NPE) hoac nem NoSuchElementException khi pool rong.

    /** Tra ket noi ve pool. free(null) van giam countConnection nen phai chan null. */
    private static void traKetNoi(Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean check_active(String username) {
        if (!ENABLE_ACTIVE_CHECK) {
            return true; // Nếu tắt kiểm tra active thì luôn trả về true
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM 5h_active WHERE username = ? LIMIT 1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi kiểm tra trạng thái kích hoạt cho username: " + username);
            e.printStackTrace();
            return true;
        } finally {
            traKetNoi(conn);
        }
    }

    public static boolean check_active(int userID) {
        if (!ENABLE_ACTIVE_CHECK) {
            return true; // Nếu tắt kiểm tra active thì luôn trả về true
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM 5h_active WHERE userID = ? LIMIT 1")) {
                ps.setInt(1, userID);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi kiểm tra trạng thái kích hoạt cho userID: " + userID);
            e.printStackTrace();
            return true;
        } finally {
            traKetNoi(conn);
        }
    }

    public static boolean insert_active(int userID, String username) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO 5h_active (userID, username) VALUES (?, ?)")) {
                ps.setInt(1, userID);
                ps.setString(2, username);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm bản ghi kích hoạt cho userID: " + userID);
            e.printStackTrace();
            return false;
        } finally {
            traKetNoi(conn);
        }
    }

    public static boolean check_active_putcode(int userID) {
        return check_active(userID);
    }






    public String getGiftCode_DB(Char player) {
        String respond = "0|KHONG CO DATA";
        try {
            Connection conn = Database.connGiftCode.getConnection();
            try (Statement stm = conn.createStatement()) {
                String sql = "select * from ngude_giftcode where charname='" + player.charname + "' and status = 0 limit 1";
                ResultSet rs = stm.executeQuery(sql);
                if (rs.next()) {
                    respond = "1|" + rs.getInt("xu") + "|" + rs.getInt("luong") + "|" + rs.getInt("luongLock") + "|" + rs.getString("item") + "|" + rs.getInt("level") + "|" + rs.getString("code");
                    if (player.getLevel() >= rs.getInt("level")) {
                        sql = "update ngude_giftcode set status = 1 where id = '" + rs.getInt("id") + "'";
                        stm.execute(sql);
                    }
                    rs.close();
                }
            }
            Database.connGiftCode.free(conn);
            return respond;
        } catch (SQLException e) {
            return "0|LOI MYSQL: " + e.getMessage();
        }
    }

    public void countChar(Char p, int UserID) {
//        int userId = getUserIDbyCharname(p);
//        if (userId == -1) {
//            p.countChar = 0;
//            return;
//        }
        try {
            Connection conn = Database.connGiftCode.getConnection();
            String sql = "SELECT `charname` FROM `tob_char` WHERE `userId` = '" + p.userID + "';";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
            }
            rs.close();
            stm.close();
            Database.connGiftCode.free(conn);
            p.countChar = count;
            return;
        } catch (Exception ignored) {
            p.countChar = 0;
            return;
        }
    }

    public int getUserIDbyCharname(Char p) {
        try {
            Connection conn = Database.connGiftCode.getConnection();
            ResultSet rs = null;
            Statement stm = null;
            String sql = "SELECT userId FROM `tob_char` WHERE `charname` = '" + p.charname + "';";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            if (rs != null && rs.next()) {
                int count = rs.getInt("userId");
                rs.close();
                stm.close();
                Database.connGiftCode.free(conn);
                return count;
            }
            rs.close();
            stm.close();
            Database.connGiftCode.free(conn);
            return -1;
        } catch (Exception ignored) {
            return -1;
        }
    }

    public String getCheckQua_Tuan(Char player) {
        String respond = "0| KHONG CO QUA";
        if (player.tichluy_tuan >= player.diemNapVip || player.diemNapVip < 8000) {
            return respond;
        }
        try {
            int a = player.diemNapVip;
            int b = player.tichluy_tuan;
            if (a - b > 8000) {
                a = b + 8000;
            }
            if ((a - b) / 8000 > 0) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|0|GEM:249:50:-1|1|mốc nạp tích luỹ tuần.";
                player.tichluy += 8000;
                stm.execute("update `tob_char` set `tichluy_tuan` = `tichluy_tuan` + 8000 where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            return respond;
        } catch (SQLException e) {
            return "0|LOI MYSQL: " + e.getMessage();
        }
//    
    }

    //Nạp 5000k " đc trứng rồng hoặc đại bàng ngẫu nhiên vv Pp vv theo lv , pét Bạch Thử 30 ngày
    //Nạp 2000k đc trúng cú hoặc dơi vv , Pp vv theo lv 
    //Nạp 1000k đc trứng yêu tinh vv + pp theo lv 
    //Nạp 500k đc Pp vv Theo lv 
    //Nạp 200k đc trứng yêu Tinh 30   ngày + 10 Vé quay số Ph
    // Than method (210 dong) da chuyen sang DatabaseGiftCode.java. Chu ky GIU NGUYEN.
    public String getCheckQua(Char player) {
        return DatabaseGiftCode.getCheckQua(this, player);
    }

    public String getCheckQua_Bonus(Char player) {
        String respond = "0| KHONG CO QUA";
        long daNap = (player.luongNap * 2);
        if (player.tichluy_bosung >= daNap) {
            return respond;
        }
        try {
//            if (daNap >= 20000 && player.tichluy_bosung < 20000) {
//                Connection conn = Database.connGiftCode.getConnection();
//                Statement stm = conn.createStatement();
//                respond = "1|0|0|0|DB:720:1:-1|1|mốc nạp bổ sung 20000 luong.";
//                player.tichluy_bosung = 20000;
//                stm.execute("update `tob_char` set tichluy_bosung = '20000' where charname = '" + player.charname + "'");
//                stm.close();
//                Database.connGiftCode.free(conn);
//                return respond;
//            }
            if (daNap >= 800000 && player.tichluy_bosung < 800000) {
                Connection conn = Database.connGiftCode.getConnection();
                Statement stm = conn.createStatement();
                respond = "1|0|0|0|DH:8773:1:129600|1|mốc nạp bổ sung 800K luong.";
                player.tichluy_bosung = 800000;
                stm.execute("update `tob_char` set tichluy_bosung = '800000' where charname = '" + player.charname + "'");
                stm.close();
                Database.connGiftCode.free(conn);
                return respond;
            }
            return respond;
        } catch (SQLException e) {
            return "0|LOI MYSQL: " + e.getMessage();
        }
    }

    public void UpdateQua_NPC(Char p) {
        Connection conn = null;
        Statement stm = null;
        try {
            conn = Database.connGiftCode.getConnection();
            stm = conn.createStatement();
            stm.execute("update `tob_char` set qua_npc = '" + p.qua_npc + "' where charname = '" + p.charname + "'");
            stm.close();
            Database.connGiftCode.free(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int TileNap = 1;

    // Than method (84 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void getBoardNapTien(Char player) {
        DatabaseXepHang.getBoardNapTien(this, player);
    }

    // Than method (86 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void getBoardCreated(Char player) {
        DatabaseXepHang.getBoardCreated(this, player);
    }

    /**
     * Trang thai 3 moc nap (chi so 1..3, chi so 0 khong dung) cho 1 tai khoan (userID):
     *   0 = chua nap / chua thanh toan
     *   1 = da thanh toan, cho nhan qua o NPC Tong Quan
     *   2 = da nhan qua roi
     * Neu 1 moc co nhieu don (vi du mua lai), uu tien trang thai cao nhat (da_nhan > da_thanh_toan).
     */
    public int[] getMocNapStatus(int userID) {
        int[] status = new int[4];
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.connPoolNap.getConnection();
            pre = conn.prepareStatement("SELECT moc, trang_thai FROM tob_nap_moc WHERE userID=? AND moc BETWEEN 1 AND 3");
            pre.setInt(1, userID);
            rs = pre.executeQuery();
            while (rs.next()) {
                int moc = rs.getInt("moc");
                String tt = rs.getString("trang_thai");
                int val = "da_nhan".equals(tt) ? 2 : ("da_thanh_toan".equals(tt) ? 1 : 0);
                if (val > status[moc]) {
                    status[moc] = val;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { rs.close(); } catch (Exception ignored) { }
            try { pre.close(); } catch (Exception ignored) { }
            try { Database.connPoolNap.free(conn); } catch (Exception ignored) { }
        }
        return status;
    }

    /**
     * Danh dau 1 moc nap la da nhan qua (khong the nhan lai). Tra ve true neu that su
     * co 1 dong 'da_thanh_toan' duoc chuyen thanh 'da_nhan' (chong bam nhan 2 lan/duplicate).
     */
    public boolean claimMocNap(int userID, int moc) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        PreparedStatement update = null;
        try {
            conn = Database.connPoolNap.getConnection();
            pre = conn.prepareStatement("SELECT id FROM tob_nap_moc WHERE userID=? AND moc=? AND trang_thai='da_thanh_toan' ORDER BY id ASC LIMIT 1");
            pre.setInt(1, userID);
            pre.setInt(2, moc);
            rs = pre.executeQuery();
            if (!rs.next()) {
                return false;
            }
            int id = rs.getInt("id");
            update = conn.prepareStatement("UPDATE tob_nap_moc SET trang_thai='da_nhan', claimed_at=NOW() WHERE id=? AND trang_thai='da_thanh_toan'");
            update.setInt(1, id);
            return update.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { rs.close(); } catch (Exception ignored) { }
            try { pre.close(); } catch (Exception ignored) { }
            try { update.close(); } catch (Exception ignored) { }
            try { Database.connPoolNap.free(conn); } catch (Exception ignored) { }
        }
    }

    public void saveAllChar(Char p) {
    }

    public Char clone(Char copythis) {
        return new Char(null);
    }

    // Than method (550 dong) da chuyen sang DatabaseGiftCode.java. Chu ky GIU NGUYEN.
    public void doAddNewColumInfoPart() {
        DatabaseGiftCode.doAddNewColumInfoPart(this);
    }

    public boolean updateValidCodeLixi(String code, String charname) {
        // SECURITY: codes are alphanumeric only; strip the rest to prevent SQL injection.
        if (code == null) {
            return false;
        }
        code = code.replaceAll("[^a-zA-Z0-9_-]", "");
        charname = charname + "_" + Database.nameServer[TeamServer.server];
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "update tob_code_lixi set nhan=1, charnhan='" + charname + "', timeNhan=now() WHERE code='" + code + "'";
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean checkValidCodeLixi(String code) {
        // SECURITY: codes are alphanumeric only; strip the rest to prevent SQL injection.
        if (code == null) {
            return false;
        }
        code = code.replaceAll("[^a-zA-Z0-9_-]", "");
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "SELECT * FROM tob_code_lixi WHERE code='" + code + "' and nhan=0";
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean checkCanInputCodeLixi(String charname) {
        charname = charname + "_" + Database.nameServer[TeamServer.server];
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "SELECT * FROM tob_code_lixi WHERE charnhan='" + charname + "'";
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return false;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return true;
    }

    public static String genCodeLixi() {
        String info;
        String code;
        for (info = "0123456789", code = ""; code.length() < 12; code = code + info.charAt(Map.r.nextInt(info.length()))) {
        }
        code = code + "kpah";
        return code;
    }

    public static void startSaveChar() {
        MessageCreator.doInitItemNpcSell();
        Database.instance.doAddNewColumInfoPart();
        Map.loadAllTemplate();
   
        if (Char.getDayTime(0L).compareTo("2020-10-28 10:30:00") <= 0) {
            Database.instance.resetTopEvent();
        }
        if (Char.getDayTime(0L).compareTo("2021-02-02 15:45:00") <= 0) {
            Database.instance.resetCooking();
            Database.instance.resetTopEvent();
            Database.instance.doResetCharBuyRuong();
        }
        if (Database.instance.checkTableTobQuaLoiDai()) {
            Database.instance.backupTobThachDau();
            for (int i = 0; i < 5; ++i) {
                Database.instance.getTopQuaLoiDai(i);
            }
        }
        Database.instance.loadAllCharBuyRuong();
        Database.instance.doGetAllCharVip();
        Database.instance.loadAllQuestTemplate(0);
        Database.instance.loadAllQuestTemplate(1);
        Database.instance.loadAllQuestTemplate(2);
        
        Database.instance.loadSMS(0);
        Database.instance.loadSMS(1);
        Database.instance.check();
        Database.instance.loadTimeX2Server();
        Database.instance.loadAllClaninfo();
        Database.instance.loadAllFruitTemplate();
        Database.instance.loadAllTubinhTemplate();
        NewClan.checkDelClanAuto();
        NewClan.startThreadSaveClan();
        Map.loadClanPropertyTown();
        Database.instance.loadEvent();

        Database.instance.loadBadWords();
      

        Char.loadAllItemLucky();
      
        Database.instance.loadNameAttributeItem();
   

        if (Database.createRace == 1) {
            Database.instance.createTableRace(Database.tableRace);
            Database.instance.initDataRace(Database.tableRace, Database.dayCreateRace);
        }
       

        System.gc();
        // TODO bật lại
        if (!Constant.Checker.LOCAL_SERVER) {
            Database.instance.checkDropTable();
        }

        Database.instance.loadAllXoso();
      
        Database.instance.loadAllXosoLow();
        

        Database.instance.loadCharChienTruong();
        for (int i = 0; i < 6; ++i) {
            Database.instance.loadAllCharThiDau(i);
        }
        if (Char.getDayTime(0L).compareTo("2025-05-05 10:00:00") <= 0) {
            Map.MAX_LI_XI = 100;
            Map.hourGiveLixi = 15;
            Map.minuteGiveLixi = 30;
            Database.instance.updateInfoLixiEvent();
        }
    }

    private Database() {
        this.connections = new Vector<>();
        this.busy = new Vector<>();
        this.curWeekMonthYear = "";
        this.curMonthYear = "";
        this.sqlUpdate = new Vector<>();
        this.stop = false;
        this.templateTest = new Vector<>();
        this.sid = 0;
        this.mid = 0;
        this.notFalse = false;
        this.update = 0;
        this.hack = new Hashtable<>();
    }

   
   

    

    public void updateDelChar(int charDBID, int del) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            final String sql = "update tob_char set del=?,tDelChar=? where id=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, del);
            pre.setLong(2, System.currentTimeMillis());
            pre.setInt(3, charDBID);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public static void setLink(String host, String dbname, String user, String pwd, String maxCo) {
        Database.LINK = "jdbc:mysql://" + host + "/" + dbname + "?user=" + user + "&password=" + pwd;
        Database.connPool = new ConnPool("jdbc:mysql://" + host + "/" + dbname, user, pwd, Integer.parseInt(maxCo));
    }

    public static void setLink1(String host, String dbname, String user, String pwd, String maxCo) {
        Database.LINK1 = "jdbc:mysql://" + host + "/" + dbname + "?user=" + user + "&password=" + pwd;
        Database.connPool1 = new ConnPool("jdbc:mysql://" + host + "/" + dbname, user, pwd, Integer.parseInt(maxCo));
    }

    public static void setLinkBoardNap(String host, String dbname, String user, String pwd, String maxCo) {
        Database.connPoolNap = new ConnPool("jdbc:mysql://" + host + "/" + dbname, user, pwd, Integer.parseInt(maxCo));
    }

    public static void setLinkGiftCode(String host, String dbname, String user, String pwd, String maxCo) {
        Database.connGiftCode = new ConnPool("jdbc:mysql://" + host + "/" + dbname, user, pwd, Integer.parseInt(maxCo));
    }

    public Connection getConnection() throws SQLException {
        return Database.connPool.getConFromPool();
    }

    public Connection getConnection1() throws SQLException {
        return Database.connPool1.getConFromPool();
    }

   

    public String addSlashes(String st) {
        StringBuffer sbuff = new StringBuffer();
        for (int i = 0; i < st.length(); ++i) {
            if (st.charAt(i) == '\'') {
                sbuff.append("'");
            } else if (st.charAt(i) == '\\') {
                sbuff.append("\\\\");
            } else {
                sbuff.append(st.charAt(i));
            }
        }
        return sbuff.toString();
    }

    public synchronized boolean checkUsername(String uname) {
        // SECURITY: disabled external socket to third-party auth server (27.0.14.75).
        if (true) {
            return true;
        }
        String text = "-1";
        Socket sc = null;
        InputStream in = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream ba = null;
        try {
            sc = new Socket("27.0.14.75", 8008);
            dos = new DataOutputStream(sc.getOutputStream());
            dos.writeBytes("?u=" + uname + "&p=0" + "&t=1" + "&g=21\r\n");
            dos.flush();
            in = sc.getInputStream();
            ba = new ByteArrayOutputStream();
            while (true) {
                byte[] b = {0};
                int x = in.read(b);
                if (x == -1) {
                    return true;
                }
                if (b[0] == 13) {
                    text = ba.toString("UTF-8");
                    try {
                        text = text.substring(0, text.indexOf("HTTP/1.1"));
                    } catch (Exception ignored) {
                    }
                    break;
                }
                ba.write(b);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                sc.close();
            } catch (Exception ignored) {
            }
            try {
                ba.close();
            } catch (Exception ignored) {
            }
            try {
                in.close();
            } catch (Exception ignored) {
            }
            try {
                dos.close();
            } catch (Exception ignored) {
            }
        }
        try {
            sc.close();
        } catch (Exception ignored) {
        }
        try {
            ba.close();
        } catch (Exception ignored) {
        }
        try {
            in.close();
        } catch (Exception ignored) {
        }
        try {
            dos.close();
        } catch (Exception ignored) {
        }
        sc = null;
        ba = null;
        in = null;
        dos = null;
        return Integer.parseInt(text) == -1;
    }

    public String getDateReg(int userid) {
        // SECURITY: disabled external call to third-party (teamobi) server.
        return "";
    }

    public synchronized boolean checkUsernameVTC(String uname) {
        // SECURITY: disabled external call to third-party (VTC) server.
        return false;
    }

    public int[] getUserID(String provider, String uname, String pwd) {
        // SECURITY: disabled external VTC login that sent username+password in plaintext over HTTP
        // to a third-party server. Use the local login server / local account DB instead.
        return new int[]{-3, 0};
    }

    public int getUserIDTest(String uname, String pass) {
        // SECURITY: disabled external call that sent username+password to teamobi over HTTP.
        return -3;
    }

    public int getUserID(String uname, String pwd, int t) {
        // SECURITY: disabled external socket to third-party auth server (27.0.14.75) that sent credentials.
        if (true) {
            return -3;
        }
        String text = "-3";
        Socket sc = null;
        InputStream in = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream ba = null;
        try {
            sc = new Socket("27.0.14.75", 8008);
            dos = new DataOutputStream(sc.getOutputStream());
            in = sc.getInputStream();
            dos.writeBytes("?u=" + uname + "&p=" + pwd + "&t=" + t + "&g=21\r\n");
            dos.flush();
            ba = new ByteArrayOutputStream();
            byte[] b;
            do {
                b = new byte[]{0};
                int x = in.read(b);
                if (x == -1) {
                    return -3;
                }
                if (b[0] == 13) {
                    break;
                }
                ba.write(b);
            } while (b.length <= 1024);
            text = ba.toString("UTF-8");
        } catch (Exception e) {
            System.out.print(" f ");
        } finally {
            try {
                ba.close();
            } catch (Exception ignored) {
            }
            try {
                in.close();
            } catch (Exception ignored) {
            }
            try {
                dos.close();
            } catch (Exception ignored) {
            }
            try {
                sc.close();
            } catch (Exception ignored) {
            }
        }
        try {
            ba.close();
        } catch (Exception ignored) {
        }
        try {
            in.close();
        } catch (Exception ignored) {
        }
        try {
            dos.close();
        } catch (Exception ignored) {
        }
        try {
            sc.close();
        } catch (Exception ignored) {
        }
        return Integer.parseInt(text);
    }

    public void updateAgentTeamUser(int userid, String agent) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String sql = "update team_user set newagent=" + agent + " where userid=" + userid;
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateAgentTeamUser1(int userid, String agent) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String sql = "update team_user_1 set newagent=" + agent + " where userid=" + userid;
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateTeamUserID(int userid, String provider, String agent) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String sql = "update team_user set newprovider='" + provider + "',newagent='" + agent + "' where userid=" + userid;
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateTeamUserID1(int userid, String provider, String agent) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String sql = "update team_user_1 set newprovider='" + provider + "',newagent='" + agent + "' where userid=" + userid;
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addTeamUserID(int userid, String provider, String agent) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String sql = "insert team_user set userid=" + userid + ",newprovider='" + provider + "',newagent='" + agent + "'" + ",provider=0,agent=0";
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public String[] getProviderFromTeamUser1(int userid, String provider, String agent) {
        String[] result = {provider, agent};
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select * from team_user_1 where userid=" + userid;
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                int result2 = rs.getInt("provider");
                int result3 = rs.getInt("agent");
                result[0] = rs.getString("newprovider");
                result[1] = rs.getString("newagent");
                if (result[0] == null || result[1] == null || result[0].equals("-1") || result[1].equals("-1") || result[0].equals("") || result[1].equals("")) {
                    result[0] = String.valueOf(result2);
                    result[1] = String.valueOf(result3);
                    this.updateTeamUserID1(userid, result[0], result[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return result;
    }

    public String[] getProviderFromTeamUser(int userid, String provider, String agent) {
        String[] result = {provider, agent};
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select * from team_user where userid=" + userid;
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                int result2 = rs.getInt("provider");
                int result3 = rs.getInt("agent");
                result[0] = rs.getString("newprovider");
                result[1] = rs.getString("newagent");
                if (result[0] == null || result[1] == null || result[0].equals("-1") || result[1].equals("-1") || result[0].equals("") || result[1].equals("")) {
                    result[0] = String.valueOf(result2);
                    result[1] = String.valueOf(result3);
                    this.updateTeamUserID(userid, result[0], result[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return result;
    }

    public void updateNewClanData(Vector<NewClan> clan) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            for (NewClan c : clan) {
                if (c.money < 0L) {
                    c.money = 0L;
                }
                String sql = "update tob_clan set xp=" + c.xp + ",lvClan=" + c.level + ",money=" + c.money + ",town=" + c.town + ",tax=" + c.tax + ",skillclan='" + c.getSkillClan() + "'" + ",devote=" + c.pDevote + ",country=" + c.country + " where id_clan=" + c.id;
                pre.executeUpdate(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void getMemberNewClan(NewClan clan) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select charname,pInfo,xp,basepoint from tob_char where idclan=" + clan.id + " and ban=0";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("charname").toLowerCase();
                Char c = new Char(null);
                String dbinfo = rs.getString("pInfo");
                String[] data = Char.split(dbinfo, ",");
                int oldLv = 0;
                try {
                    oldLv = Integer.parseInt(data[20]);
                } catch (Exception ignored) {
                }
                c.charname = name;
                c.lvDetail.setExp(rs.getLong("xp"), oldLv, c.getName(), "database");
                c.rankClan = rs.getByte("basepoint");
                clan.addMember(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public byte getCountryClan(String master) {
        byte idcountry = -1;
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select pInfo from tob_char where charname='" + master + "'";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                String info = rs.getString("pInfo");
                String[] data = Char.split(info, ",");
                try {
                    idcountry = Byte.parseByte(data[39]);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return idcountry;
    }

    public Vector<ClanData> loadClanData() {
        if (Map.isUseNewClan) {
            return new Vector<>();
        }
        return new Vector<>();
    }

    public void loadSellerVip(Hashtable<Byte, CharSellVip> charSeller) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_clan";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    // Than method (108 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void loadAllQuestTemplate(int type) {
        DatabaseXepHang.loadAllQuestTemplate(this, type);
    }

    public void loadAllTubinhTemplate() {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_tubinh";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                TuBinhTemplate tb = new TuBinhTemplate();
                if (!TeamServer.isServerIndo()) {
                    tb.name = rs.getString("name");
                } else {
                    tb.name = rs.getString("namein");
                }
                tb.attack = rs.getShort("attack");
                tb.def = rs.getShort("def");
                tb.crit = rs.getShort("crit");
                tb.dog = rs.getShort("dog");
                tb.hp = rs.getShort("hp");
                tb.mp = rs.getShort("mp");
                tb.nmanh = rs.getByte("nmanh");
                tb.level = rs.getByte("level");
                TuBinhTemplate.ALL_TU_BINH.add(tb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadAllFruitTemplate() {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_fruit";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                FruitTemplate fruit = new FruitTemplate();
                fruit.id = rs.getShort("id");
                if (!TeamServer.isServerIndo()) {
                    fruit.name = rs.getString("name");
                } else {
                    fruit.name = rs.getString("namein");
                }
                fruit.type = rs.getByte("tfruit");
                fruit.decript = rs.getString("decript");
                fruit.idImage[0] = rs.getShort("id1");
                fruit.idImage[1] = rs.getShort("id2");
                fruit.time[0] = rs.getShort("time1");
                fruit.time[1] = rs.getShort("time2");
                fruit.time[2] = rs.getShort("time3");
                FruitTemplate.ALL_FRUIT_TEMPLATE.add(fruit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadAllNewClaninfo() {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_clan";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                NewClan clanInfo = new NewClan();
                clanInfo.id = rs.getShort("id_clan");
                clanInfo.name = rs.getString("clanName");
                clanInfo.master = rs.getString("charMaster");
                clanInfo.level = rs.getShort("lvClan");
                this.getMemberNewClan(clanInfo);
                clanInfo.money = rs.getLong("money");
                clanInfo.xp = rs.getLong("xp");
                clanInfo.date = rs.getDate("datecreate");
                clanInfo.slogan = rs.getString("slogan");
                clanInfo.isDel = (rs.getInt("isdel") == 1);
                clanInfo.timeStartDel = rs.getLong("tcheckdel");
                clanInfo.country = rs.getByte("country");
                clanInfo.initOtherInfo(rs.getString("otherinfo"));
                clanInfo.level = rs.getShort("lvClan");
                clanInfo.town = rs.getByte("town");
                clanInfo.tax = rs.getByte("tax");
                clanInfo.pDevote = rs.getInt("devote");
                clanInfo.initSkillClan(rs.getString("skillclan"));
                clanInfo.country = rs.getByte("country");
                NewClan.calLevelClan(clanInfo);
                NewClan.addClan(clanInfo);
                NewClan.getSubLeader(clanInfo.id, 1);
                NewClan.getSubLeader(clanInfo.id, 2);
                Database.allClanDB.add(clanInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public static void addClanToMap() {
        Database.allClanDB.removeAllElements();
    }

    public void loadAllClaninfo() {
        if (Map.isUseNewClan) {
            this.loadAllNewClaninfo();
        }
    }

    public short[] getIDClanProTown() {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        short[] result = {-1, -1, -1};
        try {
            final String sql = "select * from tob_clan where town>0";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            final int i = 0;
            while (rs.next()) {
                byte country = rs.getByte("country");
                if (country > -1) {
                    Map.taxOfClan[country] = rs.getByte("tax");
                    result[country] = rs.getShort("id_clan");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return result;
    }

    public Vector<Integer> getIDClanUsed() {
        Vector<Integer> idClan = new Vector<>();
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select id_clan from tob_clan ";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                idClan.add(rs.getInt("id_clan"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return idClan;
    }

    public void resetMemberClan() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            final String sql = "select * from tob_clan";
            rs = stmt.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count = 0;
                int id_clan = rs.getInt("id_clan");
                String[] mem = Char.split(rs.getString("member"), ",");
                System.out.println("SO LUONG THANH VIEN " + mem.length);
                StringBuilder newmember = new StringBuilder(mem[0]);
                for (int i = 1; i < mem.length; ++i) {
                    String sql2 = "select * from tob_char where charname='" + mem[i] + "' and idClan=" + id_clan;
                    ResultSet rs2 = conn.createStatement().executeQuery(sql2);
                    if (rs2.next()) {
                        System.out.print(mem[i] + " >> ");
                        ++count;
                        newmember.append(",").append(mem[i]);
                    } else {
                        System.err.println(mem[i]);
                    }
                    try {
                        rs2.close();
                    } catch (Exception ignored) {
                    }
                }
                System.out.println(newmember + " " + id_clan + " SO LUONG SAU KHI UPDATE " + count);
                String sqlUpdate = "update tob_clan set member='" + newmember + "' where id_clan=" + id_clan;
                conn.createStatement().execute(sqlUpdate);
            }
            System.out.println("SO LUONG CHO " + count);
            try {
                Thread.sleep(10000L);
            } catch (Exception ignored) {
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reUpdateClan() {
    }

    public void reupdateIdCLanChar(String charname, int idClan) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_char where charname='" + charname + "'";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                int charDBID = rs.getInt("id");
                Statement stmt2 = null;
                try {
                    stmt2 = conn.createStatement();
                    sql = "update tob_char set idClan=" + idClan + " where id=" + charDBID;
                    stmt2.executeUpdate(sql);
                } catch (Exception e) {
                    try {
                        stmt2.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public NewClan getInfoClan(int idClan) {
        if (idClan == -1) {
            return null;
        }
        return Map.getClaninfoByID(idClan);
    }

    public void getCharPet(Char p, Connection conn) {
        String sql = "";
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            sql = "select * from tob_pet WHERE owner=" + p.charDBID;
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            Vector<Pet> pdelete = new Vector<>();
            while (rs.next()) {
                Pet pet = new Pet(rs.getString("name"), rs.getByte("idImg"), rs.getInt("owner"), rs.getString("info"), rs.getString("itPet"), p.lastLog);
                pet.idDB = rs.getInt("id");
                pet.place = rs.getByte("place");
                pet.id = p.getIDItem();
                String lastLog = Char.split(p.lastLog.trim(), ".")[0].trim();
                if (pet.getTimeExpire() <= 0 || (lastLog.compareTo("2018-11-20 10:46:00") < 0 && pet.isPetLongDenvinhVien())) {
                    if (lastLog.compareTo("2018-11-20 10:46:00") < 0 && pet.isPetLongDenvinhVien()) {
                        p.addLuong(1000);
                    }
                    pdelete.add(pet);
                    this.saveOrtherLog("tob_log_other_item", p.charname, pet.getDBInfo(), "pd");
                    p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(pet.getName() + " đã hết thời gian sử dụng"));
                } else {
                    // Hoa hinh Pet het han: don ve mac dinh luc nap nhan vat (dang
                    // nhap), giong cach ban goc dang lam - khong dat het han thuc
                    // thoi trong luc dang choi, vi cong that cua het han
                    // (endHoaHinhPet()) da duoc applyKhangXuyenGiap()/getIdEffPet()
                    // tu kiem tra rieng, chi CSDL la can don lai cho lan luu sau.
                    pet.donHoaHinhHetHan();
                    if (pet.place == 1) {
                        p.petUsing = pet;
                    }
                    p.pet.add(pet);
                }
            }
            for (Pet pet : pdelete) {
                this.deletePet(pet.idDB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
    }

    public void getInfoCharClan(Char p) {
        if (p.idClan == -1) {
            return;
        }
    }

    public void delClan(int idClan) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            String sql = "delete from tob_clan where id_clan=" + idClan;
            conn = this.getConnection();
            pre1 = conn.prepareStatement(sql);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public boolean checkClanName(String clanName) {
        Connection conn = null;
        Statement pre1 = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_clan where clanName='" + clanName + "'";
            conn = this.getConnection();
            pre1 = conn.createStatement();
            rs = pre1.executeQuery(sql);
            return !rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean addNewClan(NewClan clan) {
        if (clan.id == -1) {
            return false;
        }
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            final String sql = "insert into tob_clan (id_clan,clanName,charMaster,lvClan,member,money,xp,datecreate,slogan,country,tcheckdel) values (?,?,?,1,?,0,0,now(),?,?,?)";
            conn = this.getConnection();
            pre1 = conn.prepareStatement(sql);
            pre1.setInt(1, clan.id);
            pre1.setString(2, clan.name);
            pre1.setString(3, clan.master);
            pre1.setString(4, clan.master);
            pre1.setString(5, "");
            pre1.setInt(6, clan.country);
            pre1.setLong(7, clan.timeStartDel);
            pre1.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void updateTimeCheckDelClan(int clanID, long time) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            String sql = "UPDATE tob_clan SET tcheckdel=" + time + " where id_clan=" + clanID;
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void banchar() {
        Connection conn = null;
        Statement pre = null;
        Vector<String> charname = readText("hackdo.txt");
        Vector<String> b = new Vector<>();
        try {
            conn = this.getConnection();
            for (String s : charname) {
                String sql = "UPDATE tob_char SET ban=0 where charname='" + s + "'";
                pre = conn.createStatement();
                pre.execute(sql);
                Char pp = CharManager.instance.getCharByCharName(s);
                if (pp != null) {
                    CharManager.instance.kickPlayer(pp, "banchar database");
                }
                System.out.println(s);
                if (!b.contains(s)) {
                    b.add(s);
                    Logger.logString(s, "hack.txt");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            Thread.sleep(30000L);
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

    public void banChar(String charName, int ban) {
        return;
        /*
        Connection conn = null;
        Statement pre = null;
        try {
            String sql = "UPDATE tob_char SET ban=" + ban + " where charname='" + charName + "'";
            conn = this.getConnection();
            pre = conn.createStatement();
            pre.execute(sql);
            if (ban == 1) {
                Char pp = CharManager.instance.getCharByCharName(charName);
                if (pp != null) {
                    CharManager.instance.kickPlayer(pp, "banchar database 2");
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
         */
    }

    public void updateCharNewClan(NewClan clan) {
        if (clan == null || clan.id == -1) {
            return;
        }
        String mem = clan.getMem();
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            final String sql = "UPDATE tob_clan SET charMaster=?,clanName=?,member=?,slogan=?,isdel=?,timedel=?,otherinfo=? where id_clan=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, clan.master);
            pre.setString(2, clan.name);
            pre.setString(3, mem);
            pre.setString(4, clan.slogan);
            pre.setInt(5, clan.isDel ? 1 : 0);
            pre.setLong(6, clan.timeStartDel);
            pre.setString(7, clan.getOtherInfo());
            pre.setInt(8, clan.id);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }

    }

    public void saveVeso(Char player) {
        try {
            String info = player.allNumberBuy();
            String sql = "insert into tob_xoso(owner,vsxu,vsl) values (" + player.charDBID + ",'" + info + "','" + info + "') ON DUPLICATE KEY UPDATE vsxu='" + info + "',vsl='" + player.oldNumber() + "'";
            Vector<String> data = new Vector<>();
            data.add(sql);
            this.saveAllLog(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 

    public void saveLogXoso() {
        String sql = "insert into tob_log_xoso (win,timelog) values (" + Map.winNumber + ",now())";
        Vector<String> data = new Vector<>();
        data.add(sql);
        this.saveAllLog(data);
    }

    public void saveLogThongKeChiTieu(String nameItem, int idtemplate, int soluong, long money, String moneytype) {
        nameItem = BadWord.removeAccent(nameItem);
        String sql = "INSERT INTO tob_log_use_luong" + this.curWeekMonthYear + " set name ='" + nameItem + "', idtemplate=" + idtemplate + ",soluong=" + soluong + ",totalmoney=" + money + ",moneytype='" + moneytype + "' ON DUPLICATE KEY UPDATE soluong=soluong+" + soluong + ",totalmoney=totalmoney+" + money + ",solanmua=solanmua+1";
        Logdata.query.add(sql);
    }

    public void saveOrtherLog(String table, String charname, String info, String action) {
        // CHI ghi loai nam trong LocLogVatPham.GIU (mat do, gian lan, tai san tang bat thuong);
        // loai khac bo ca bang lan UserLogger. Ca bay ham ghi vao tob_log_all_item deu gac nhu vay.
        if (LocLogVatPham.boQua(action)) {
            return;
        }
        try {
            info = BadWord.removeAccent(info.toLowerCase());
            String sql = "insert into tob_log_all_item (charname,info,timelog,aclog) values ('" + charname + "','" + info + "',now(),'" + action + "')";
            Logdata.query.add(sql);
            UserLogger.addLog(charname, info, action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTableRace(String tableName) {
        String sql = "CREATE TABLE  " + tableName + "(charid int(10) unsigned NOT NULL," + "charname varchar(45) NOT NULL," + "exp bigint(20) unsigned NOT NULL DEFAULT 0," + "lastlv int(10) unsigned NOT NULL DEFAULT 1," + "idgroup int(10) NOT NULL DEFAULT -1," + "lvchar int(10) unsigned NOT NULL," + "infotop varchar(500) NOT NULL DEFAULT ''," + "headStyle tinyint(3) unsigned NOT NULL DEFAULT 0," + "rcvGift tinyint(3) unsigned NOT NULL DEFAULT 0," + "PRIMARY KEY (charid)," + "KEY Index_2 (idgroup)," + "KEY Index_3 (exp)) ENGINE=MyISAM DEFAULT CHARSET=utf8";
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception ex) {
            System.out.println("SQL ERROR=" + sql);
            return;
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            Database.connPool.free(conn);
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        Database.connPool.free(conn);
    }

    public String checkDangKy(String uname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from reg_acount where username='" + uname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int userid = rs.getInt("userid");
                return ((userid == 0) ? "90" : "") + rs.getString("ngaysinh");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return "18 11 1984";
    }

    public void addInfoAcount(String uname, String info, String born, int userid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "INSERT INTO reg_acount(username,info,ngaysinh,dateReg,userid) values(?,?,?,now(),?) ON DUPLICATE KEY UPDATE info=?,ngaysinh=?";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uname);
            pstmt.setString(2, info);
            pstmt.setString(3, born);
            pstmt.setInt(4, userid);
            pstmt.setString(5, info);
            pstmt.setString(6, born);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateInfoAcount(String uname, int userid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update reg_acount set userid=" + userid + " where username='" + uname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addData2Race(Connection conn, String tableName, String charname, int charid, int groupID, int lvchar, String infotop, byte headStyle) {
        Statement stmt = null;
        try {
            String sql = "insert into " + tableName + "(charid,charname,idgroup,lvchar,infotop,headStyle) values(" + charid + ",'" + charname + "'," + groupID + "," + lvchar + ",'" + infotop + "'," + headStyle + ")";
            stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
    }

    public void initDataRace(String tableName, String dayBegin) {
        Statement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select * from tob_char where lastLog>='" + dayBegin + "' and lastLv>=40 order by lastLv desc";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int count = 0;
            int id = 0;
            while (rs.next()) {
                this.addData2Race(conn, tableName, rs.getString("charname"), rs.getInt("id"), id, rs.getInt("lastlv"), rs.getString("infotop"), rs.getByte("headStyle"));
                System.out.println(rs.getString("charname") + "_" + rs.getInt("lastlv"));
                if (++count % 50 == 0) {
                    ++id;
                }
            }
            System.out.println("TONG CHAR THAM GIA " + count);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            Database.connPool.free(conn);
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        Database.connPool.free(conn);
    }

    public boolean checkTableTobQuaLoiDai() {
        Calendar cl = Calendar.getInstance();
        int w = cl.get(Calendar.DATE);
        int m = cl.get(Calendar.MONTH) + 1;
        int year = cl.get(Calendar.YEAR);
        String rs = "_" + m + year;
        if (w == 16) {
            Database.nameTableTopLoiDai = Database.nameTableTopLoiDai + rs;
            Database.nameTableThachDau = Database.nameTableThachDau + rs;
            System.out.println(Database.nameTableThachDau + " >> " + Database.nameTableTopLoiDai);
            this.createTable("tob_char_thach_dau" + rs, "tob_char_thach_dau");
            return this.createTable("tob_qua_loi_dai" + rs, "tob_qua_loi_dai");
        }
        return false;
    }

    public boolean createTable(String newTable, String oldTable) {
        String sql = "CREATE TABLE " + newTable + " LIKE " + oldTable;
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception ex) {
            System.err.println("SQL ERROR=" + sql);
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            Database.connPool.free(conn);
        }
        return false;
    }

    public void check() {
        String now = getWeekMonthYear();
        if (!now.equals(this.curWeekMonthYear)) {
            this.curWeekMonthYear = now;
            for (byte i = 0; i < Database.tableNameWeek_month_year.length; ++i) {
                this.createTable(Database.tableNameWeek_month_year[i] + this.curWeekMonthYear, Database.tableNameWeek_month_year[i]);
            }
        }
        now = getMonthYear();
        if (!now.equals(this.curMonthYear)) {
            this.curMonthYear = now;
            for (byte i = 0; i < Database.tableNamemonth_year.length; ++i) {
                this.createTable(Database.tableNamemonth_year[i] + this.curMonthYear, Database.tableNamemonth_year[i]);
            }
        }
    }

    public void checkDropTable() {
        int ndrop = 1000;
        int nday = 90;
        while (ndrop > 0) {
            String now = Database.curWeekMonthYearDrop = getWeekMonthYear1(nday);
            int count = 0;
            for (byte i = 0; i < Database.tableNameWeek_month_year.length; ++i) {
                count += this.DropTable(Database.tableNameWeek_month_year[i] + Database.curWeekMonthYearDrop, Database.tableNameWeek_month_year[i]);
            }
            for (byte i = 0; i < Database.tableNamemonth_year.length; ++i) {
                count += this.DropTable(Database.tableNamemonth_year[i] + Database.curMonthYearDrop, Database.tableNamemonth_year[i]);
            }
            ++nday;
            --ndrop;

        }
    }

    public int DropTable(String newTable, String oldTable) {
        String sql = "drop table " + newTable;
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            if (stmt.execute(sql)) {
                System.out.println(sql);
            }
            return 1;
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            Database.connPool.free(conn);
        }
        return 0;
    }

    public static String getWeekMonthYear1(int d) {
        String y = Char.getYear(-(long) d * 24L * 60L * 60000L);
        String month = Char.getMonth(-(long) d * 24L * 60L * 60000L);
        String day = Char.getDay(-(long) d * 24L * 60L * 60000L);
        Calendar cl = Calendar.getInstance();
        cl.set(Integer.parseInt(y), Integer.parseInt(month) - 1, Integer.parseInt(day));
        int w = cl.get(Calendar.WEEK_OF_MONTH);
        int m = cl.get(Calendar.MONTH) + 1;
        int year = cl.get(Calendar.YEAR);
        String rs = "_" + w + "_" + m + "_" + year;
        Database.curMonthYearDrop = "_" + m + "_" + year;
        return rs;
    }

    public static String getWeekMonthYear() {
        Calendar cl = Calendar.getInstance();
        int w = cl.get(Calendar.WEEK_OF_MONTH);
        int m = cl.get(Calendar.MONTH) + 1;
        int year = cl.get(Calendar.YEAR);
        String rs = "_" + w + "_" + m + "_" + year;
        Database.curMonthYearDrop = "_" + m + "_" + year;
        return rs;
    }

    public static String getMonthYear() {
        Calendar cl = Calendar.getInstance();
        int m = cl.get(Calendar.MONTH) + 1;
        int year = cl.get(Calendar.YEAR);
        return "_" + m + year;
    }

    public void saveAllLog(Vector<String> data) {
        this.check();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            while (data.size() > 0) {
                String st = data.remove(0);
                try {
                    stmt.execute(st);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("LOI SAVE LOG " + st);
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveLogClan(String charname, String actioninfo, String info) {
        try {
            String sql = "insert into tob_log_clan (charname,actioninfo,info,timeLog) values ('" + charname + "','" + actioninfo + "','" + info + "',Now())";
            Logdata.query.add(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addInfoWebChar(Char player) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String sql = "INSERT INTO tob_web_info_char set charname=?,infoWearing=?,otherInfo=?,userid=?,lv=?,acountname=? ON DUPLICATE KEY UPDATE infoWearing=?,otherInfo=?,lv=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, player.charname);
            pre.setString(2, player.getInfoWebWearing());
            pre.setString(3, "");
            pre.setInt(4, player.userID);
            pre.setInt(5, player.lvDetail.lv);
            pre.setString(6, player.getAccountName());
            pre.setString(7, player.getInfoWebWearing());
            pre.setString(8, "");
            pre.setInt(9, player.lvDetail.lv);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

   
  

    public void saveLogActiveUser(String charname, String info) {
    }

 

    public void saveLogLearnSkill(String charname, String info) {
        if (LocLogVatPham.boQua("hocskill")) {
            return;
        }
        try {
            String sql = "insert into tob_log_all_item (charname,charname2,info,timelog,aclog) values ('" + charname + "','" + charname + "','" + info + "',Now(),'hocskill')";
            Logdata.query.add(sql);
            UserLogger.addLog(charname, info, "hocskill");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLogNapMoney(String charname, String info) {
        if (LocLogVatPham.boQua("naptien")) {
            return;
        }
        try {
            String sql = "insert into tob_log_all_item (charname,charname2,info,timelog,aclog) values ('" + charname + "','" + charname + "','" + info + "',Now(),'naptien')";
            Logdata.query.add(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLogTradeItem(String from, String to, String info) {
        if (LocLogVatPham.boQua("trade")) {
            return;
        }
        try {
            String sql = "insert into tob_log_all_item(charname,charname2,info,timelog,aclog) values ('" + from + "','" + to + "','" + info + "',Now(),'trade')";
            Logdata.query.add(sql);
            UserLogger.addLog(from, to + "_" + info, "ftrade");
            UserLogger.addLog(to, from + "_" + info, "ttrade");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GHI LAI MOT VAT PHAM ROI KHOI TAY NGUOI CHOI — de sau con truy nguyen nhan mat do.
     *
     * Cac duong CHUYEN NHUONG deu da co log rieng tu truoc:
     *   ban trong gian hang  -> saveLogSellItem
     *   ban cho NPC          -> saveLogItemSellShop  (aclog 'sitems' - DA TAT tu 02/09/2026: khong nam trong
     *                           LocLogVatPham.GIU vi qua on; muon ghi lai thi them no vao danh sach do)
     *   trao doi voi nguoi   -> saveLogTradeItem
     * Ham nay danh cho cac duong lam vat pham BIEN MAT ma truoc day khong ghi gi:
     * nghien do, ban tu dong luc nhat, admin xoa tay, va server tu xoa luc nap nhan vat.
     *
     * Tat ca dung chung aclog 'matdo' de tim mot lan ra het:
     *   SELECT * FROM tob_log_all_item WHERE aclog='matdo' AND charname='...' ORDER BY id DESC;
     *
     * Ghi ca getInfoSave() (dbInfo + thuoc tinh) chu khong chi ten: hai mon cung ten
     * co the khac han nhau ve cong dam, ngu hanh, tinh luyen — muon den bu dung mon da
     * mat thi phai co du chi so, khong the dung lai o "mot cai kiem".
     *
     * KHONG dung deleteItem() lam cho ghi log duoc: than ham do RONG (khong co bang
     * tob_item tren may chu nay, vat pham nam trong 3 cot cua tob_char), nen 20 loi goi
     * toi no deu khong lam gi ca.
     */
    /**
     * Lam sach mot manh chuoi truoc khi ghep vao cau INSERT ghi log.
     *
     * VI SAO CAN: cac cau log duoc GHEP CHUOI roi day vao Logdata.query, va Logdata
     * chay ca lo mot luot. Mot dau nhay don lot vao la hong CA LO chu khong rieng dong
     * do — mat luon nhung dong log khac cua cung nhip.
     *
     * Bo ca dau "\": trong MySQL no la ky tu thoat, nen chuoi ket thuc bang "\" se nuot
     * dau nhay dong chuoi va lam lech toan bo phan con lai cau lenh. Ten vat pham lay tu
     * bang data_item do admin nhap tay nen khong the tin la sach.
     *
     * Bo xuong dong de moi su kien nam gon mot dong khi doc log.
     */
    public static String lamSachChoLog(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", " ").replace("'", " ").replace("\r", " ").replace("\n", " ");
    }

    public void saveLogMatVatPham(Char p, Item item, String lyDo) {
        if (LocLogVatPham.boQua("matdo")) {
            return;
        }
        try {
            if (p == null || item == null) {
                return;
            }
            String ten = "";
            try {
                ten = item.getTemplate().name;
            } catch (Exception ignored) {
            }
            String info = lyDo + " | " + ten + " +" + item.plus + " | " + item.getInfoSave();
            info = BadWord.removeAccent(info);
            String sql = "insert into tob_log_all_item (idchar,charname,info,timelog,aclog) values ("
                    + p.charDBID + ",'" + lamSachChoLog(p.charname) + "','" + lamSachChoLog(info)
                    + "',Now(),'matdo')";
            Logdata.query.add(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLogItemSellShop(String seller, int charID, String info) {
        if (LocLogVatPham.boQua("sitems")) {
            return;
        }
        try {
            info = BadWord.removeAccent(info);
            String sql = "insert into tob_log_all_item (idchar,charname,info,timelog,aclog) values (" + charID + ",'" + seller + "','" + info + "',Now(),'sitems')";
            Logdata.query.add(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLogSellItem(String seller, String customer, String info) {
        if (LocLogVatPham.boQua("sitemu")) {
            return;
        }
        try {
            info = BadWord.removeAccent(info);
            String sql = "insert into tob_log_all_item (charname,charname2,info,timelog,aclog) values ('" + seller + "','" + customer + "','" + info + "',Now(),'sitemu')";
            Logdata.query.add(sql);
            UserLogger.addLog(seller, customer + "_" + info, "sitemu");
            UserLogger.addLog(customer, seller + "_" + info, "bitemu");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI LOG MUA ITEM USER");
        }
    }


    public void saveLogBuyItemShop(String seller, String info, int type, int price, String money) {
        try {
            String sql = "insert into tob_log_buy_shop (charname,info,timeBuy,typeitem,price,money) values ('" + seller + "','" + info + "',Now()," + type + "," + price + ",'" + money + "')";
            Logdata.query.add(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateClanQuest(Char p) {
    }

    public ClanQuest loadCCharQuest(Char p) {
        Connection conn = null;
        Statement stmt = null;
        ClanQuest cq = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM tob_quest_clan WHERE id_char=" + p.charDBID;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                cq = new ClanQuest();
                cq.idQuest = rs.getByte("idquest");
                cq.rcv = rs.getByte("rcv");
                cq.finish = rs.getByte("finish");
                cq.item_mons[3] = (short) rs.getInt("nmons1");
                cq.item_mons[4] = (short) rs.getInt("nmons2");
                cq.item_mons[5] = (short) rs.getInt("nmons3");
                cq.item_mons[0] = (short) rs.getInt("nitem1");
                cq.item_mons[1] = (short) rs.getInt("nitem2");
                cq.item_mons[2] = (short) rs.getInt("nitem3");
                cq.count = rs.getByte("n");
                try {
                    cq.timeEndQClan = rs.getLong("timeEnd");
                } catch (Exception ignored) {
                }
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("KHONG TON TAI CHAR QUEST");
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return cq;
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return cq;
    }

    public void isCheckCharQuest(int idChar) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "update top_char_quest set ischeck=1 WHERE id_char=" + idChar;
            stmt.execute(sql);
        } catch (Exception e) {
            System.out.println("KHONG TON TAI CHAR QUEST");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public CharQuest loadCharQuest(int idChar) {
        Connection conn = null;
        Statement stmt = null;
        CharQuest cq = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM top_char_quest WHERE id_char=" + idChar + " and ischeck=0";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                cq = new CharQuest();
                cq.id_quest = rs.getInt("id_quest");
                cq.receive = (byte) rs.getInt("receive");
                cq.finish = (byte) rs.getInt("finish");
                cq.nMonster[0] = (short) rs.getInt("nmonster1");
                cq.nMonster[1] = (short) rs.getInt("nmonster2");
                cq.nMonster[2] = (short) rs.getInt("nmonster3");
                cq.nItem[0] = (short) rs.getInt("nitem1");
                cq.nItem[1] = (short) rs.getInt("nitem2");
                cq.nItem[2] = (short) rs.getInt("nitem3");
                cq.currentNpc = (short) rs.getInt("currentnpc");
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("KHONG TON TAI CHAR QUEST");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return cq;
    }

    public void getCharQuest(Char p) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM tob_char_quest WHERE id_char=" + p.charDBID;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String itemget = rs.getString("itemget");
                String monskilled = rs.getString("monskilled");
                String finish = rs.getString("isfinish");
                String itquest = rs.getString("itemquest");
                p.itemget = itemget;
                p.monskilled = monskilled;
                p.finish = finish;
                p.itquest = itquest;
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("KHONG TON TAI CHAR QUEST");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<TuBinhTemplate> loadCharTubinh0610(Char p) {
        Vector<TuBinhTemplate> all = new Vector<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_char_tubinh_0610 where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, p.charDBID);
            rs = stmt.executeQuery();
            if (rs.next() && !rs.getString("listid").equals("")) {
                String[] id = Char.split(rs.getString("listid"), ",");
                String[] manh = Char.split(rs.getString("listmanh"), "|");
                for (int i = 0; i < id.length; ++i) {
                    TuBinhTemplate tb = new TuBinhTemplate();
                    tb.id = Byte.parseByte(id[i]);
                    String[] manh2 = Char.split(manh[i], ",");
                    for (int j = 0; j < manh2.length; ++j) {
                        tb.manh[j] = Byte.parseByte(manh2[j]);
                    }
                    all.add(tb);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return all;
    }

    public Vector<TuBinhTemplate> loadCharTubinh(Char p) {
        Vector<TuBinhTemplate> all = new Vector<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_char_tubinh where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, p.charDBID);
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("listid").equals("")) {
                    p.createTubinh();
                    this.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());
                    return p.tubinh;
                }
                String[] id = Char.split(rs.getString("listid"), ",");
                String[] manh = Char.split(rs.getString("listmanh"), "|");
                for (int i = 0; i < id.length; ++i) {
                    TuBinhTemplate tb = new TuBinhTemplate();
                    tb.id = Byte.parseByte(id[i]);
                    String[] manh2 = Char.split(manh[i], ",");
                    for (int j = 0; j < manh2.length; ++j) {
                        tb.manh[j] = Byte.parseByte(manh2[j]);
                    }
                    all.add(tb);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return all;
    }

    public void saveCharTubinh(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char_tubinh set listid=?,listmanh=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setInt(3, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE FRUIT ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharTubinh(int idDBChar) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "insert into tob_char_tubinh(id_char,listid,listmanh) values(?,?,?) ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idDBChar);
            stmt.setString(2, "");
            stmt.setString(3, "");
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public FruitTemplate[] loadCharFruit(Char p) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_char_fruit where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, p.charDBID);
            rs = stmt.executeQuery();
            if (rs.next()) {
                FruitTemplate[] fr = new FruitTemplate[FruitTemplate.MAX_FRUIT];
                if (!rs.getString("listidfruit").equals("")) {
                    {
                        try {
                            String[] id = Char.split(rs.getString("listidfruit"), ",");
                            String[] status = Char.split(rs.getString("stFruit"), ",");
                            String[] time = Char.split(rs.getString("tFruit"), ",");
                            String[] gift = Char.split(rs.getString("gift"), ",");
                            String[] theo = Char.split(rs.getString("theo"), ",");
                            String[] tkho = Char.split(rs.getString("tkho"), ",");
                            for (int i = 0; i < FruitTemplate.MAX_FRUIT; ++i) {
                                FruitTemplate f = new FruitTemplate();
                                f.id = Short.parseShort(id[i]);
                                f.status = Byte.parseByte(status[i]);
                                f.tcount = Long.parseLong(time[i]);
                                f.gift = Integer.parseInt(gift[i]);
                                f.tcountTangtoc = Long.parseLong(theo[i]);
                                f.tcountKho = Long.parseLong(tkho[i]);
                                f.type = FruitTemplate.ALL_FRUIT_TEMPLATE.get(f.id).type;
                                fr[i] = f;
                            }
                        } catch (Exception e) {
                            p.fruit = new FruitTemplate[FruitTemplate.MAX_FRUIT];
                            p.createFruit();
                            this.saveCharFruit(p.charDBID, p.getInfoSaveFruit());
                            return p.fruit;
                        }
                    }
                    return fr;
                }
                p.createFruit();
                this.saveCharFruit(p.charDBID, p.getInfoSaveFruit());
                return p.fruit;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void saveCharFruit(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char_fruit set listidfruit=?,stFruit=?,tFruit=?,gift=?,theo=?,tkho=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setString(3, info[2]);
            stmt.setString(4, info[3]);
            stmt.setString(5, info[4]);
            stmt.setString(6, info[5]);
            stmt.setInt(7, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE FRUIT ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharFruit(int idDBChar) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "insert into tob_char_fruit(id_char,listidfruit,stFruit,tFruit,gift,theo,tkho) values(?,?,?,?,?,?,?) ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idDBChar);
            stmt.setString(2, "");
            stmt.setString(3, "");
            stmt.setString(4, "");
            stmt.setString(5, "");
            stmt.setString(6, "");
            stmt.setString(7, "");
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharQuest(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char_quest set itemget=?,monskilled=?,isfinish=?, itemquest=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setString(3, info[2]);
            stmt.setString(4, info[3]);
            stmt.setInt(5, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE QUEST ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    

    public void addCharQuest(int idDBChar, String[] info) {
        if (info == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "insert into tob_char_quest(id_char,itemget,monskilled,isfinish) values(?,?,?,?) ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idDBChar);
            stmt.setString(2, info[0]);
            stmt.setString(3, info[1]);
            stmt.setString(4, info[2]);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE QUEST ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharQuest(int idDBChar, CharQuest c) {
        if (c == null) {
            return;
        }
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "update top_char_quest set id_quest=" + c.id_quest + ", receive=" + c.receive + ",finish=" + c.finish + ", nmonster1=" + c.nMonster[0] + ",nmonster2=" + c.nMonster[1] + ",nmonster3=" + c.nMonster[2] + ",nitem1=" + c.nItem[0] + ",nitem2=" + c.nItem[1] + ",nitem3=" + c.nItem[2] + ",currentnpc=" + c.currentNpc + " where id_char=" + idDBChar;
            if (stmt.executeUpdate(sql) == 0) {
                sql = "insert top_char_quest (id_char,id_quest,receive,finish,nmonster1,nmonster2,nmonster3,nitem1,nitem2,nitem3,currentnpc) values (" + idDBChar + "," + c.id_quest + "," + c.receive + "," + c.finish + "," + c.nMonster[0] + "," + c.nMonster[1] + "," + c.nMonster[2] + "," + c.nItem[0] + "," + c.nItem[1] + "," + c.nItem[2] + "," + c.currentNpc + ")";
                stmt.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE QUEST ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    

    public int getMoneyFromNameGem(String name) {
        for (int i = 0; i < Map.gemTemplate.length; ++i) {
            if (name.equals(Map.gemTemplate[i].name) && i != 13) {
                return Map.gemTemplate[i].getPrice(Map.isSale);
            }
        }
        for (ItemTemplates item : this.templateTest) {
            if (item.name.equals(name)) {
                System.out.println(name + " >> " + item.price);
                return item.price;
            }
        }
        return 0;
    }

    public void updateMoney() {
        new Thread(() -> {
            Connection conn = null;
            Statement pre = null;
            ResultSet rs = null;
            try {
                conn = Database.this.getConnection();
                pre = conn.createStatement();
                while (!Database.this.stop) {
                    while (Database.this.sqlUpdate.size() > 0) {
                        pre.execute(Database.this.sqlUpdate.remove(0));
                    }
                    Thread.sleep(1L);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("LOI UPDATE MONEY");
            } finally {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    if (rs != null) { rs.close(); }
                } catch (Exception ignored) {
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }).start();
    }


  

    public int checkLoginIndo(String name, String pass) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            pre = conn.createStatement();
            String query = "select * from tob_acount where username='" + name + "' and pass=password('" + pass + "')";
            rs = pre.executeQuery(query);
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public int addNewAcountAuto(String uname, String pass) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            String sql = "insert into tob_acount (username,pass,timereg) values('" + uname.toLowerCase() + "',password('" + pass + "'),now())";
            pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pre.execute();
            ResultSet rss = pre.getGeneratedKeys();
            rss.next();
            int charId = rss.getInt(1);
            try {
                rss.close();
            } catch (Exception ignored) {
            }
            return charId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

  
    // Than method (129 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public List<CharData> getCharList(int userID, Session s) {
        return DatabaseNhanVat.getCharList(this, userID,s);
    }

    

    public ItemTemplates copyItem(ItemTemplates it, int type) {
        int[] percent = {20, 50, 10, 70, 20};
        ItemTemplates item = new ItemTemplates();
        item.kind_class = type;
        item.id = it.id;
        item.name = it.name;
        item.type = it.type;
        item.style = it.style;
        item.he = it.he;
        item.gender = it.gender;
        item.level = it.level;
        item.durable = it.durable;
        item.clazz = it.clazz;
        item.idBigImgAvatar = it.idBigImgAvatar;
        boolean isModelClothes = Map.isModelClothes(it.atb[9]);
        if (it.type != 26 && it.type != 27) {
            item.atb[0] = it.atb[0];
            item.atb[1] = (short) (it.atb[1] + (isModelClothes ? 0 : (it.atb[1] * percent[type] / 100)));
            item.atb[2] = (short) (it.atb[2] + (isModelClothes ? 0 : (it.atb[2] * percent[type] / 100)));
            item.atb[3] = (short) (it.atb[3] + (isModelClothes ? 0 : (it.atb[3] * percent[type] / 100)));
        } else {
            item.atb[0] = it.atb[0];
            item.atb[1] = it.atb[1];
            item.atb[2] = it.atb[2];
            item.atb[3] = it.atb[3];
        }
        item.atb[4] = it.atb[4];
        item.atb[5] = it.atb[5];
        item.atb[6] = it.atb[6];
        if (!isModelClothes) {
            item.atb[6] = item.atb[1];
        }
        item.atb[7] = it.atb[7];
        item.atb[8] = it.atb[8];
        item.atb[9] = it.atb[9];
        item.price = it.price;
        item.idIcon = it.idIcon;
        item.ystart = it.ystart;
        item.color = it.color;
        item.ndayLoan = it.ndayLoan;
        item.idItemUpLevel = it.idItemUpLevel;
        item.idEff = it.idEff;
        return item;
    }

    // Than method (94 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public Vector<Hashtable<Integer, ItemTemplates>> loadItemTemplate() {
        return DatabaseXepHang.loadItemTemplate(this);
    }

    

    public void loadMonsterTemplate(Hashtable<Integer, MonsterTemplate> list) {
      
        Connection conn = null;
        Statement stmt = null;
        try {
            list.put(0, new MonsterTemplate());
            conn = this.getConnection();
            stmt = conn.createStatement();
            final String sql = "SELECT * FROM data_monster";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MonsterTemplate item = new MonsterTemplate();
                item.id = rs.getInt("id");
                if (!TeamServer.isServerIndo()) {
                    item.name = rs.getString("name");
                } else {
                    item.name = rs.getString("namein");
                }
                item.type = rs.getShort("style");
                item.he = rs.getByte("he");
                item.level = rs.getByte("level");
                item.maxhp = rs.getInt("maxhp");
                item.attack = rs.getInt("attack");
                item.defend = rs.getInt("defent");
                item.accurate = rs.getShort("accurate");
                item.dodge = rs.getShort("dodge");
                item.rcvGold = rs.getInt("rcvgold");
                item.rcvXp = rs.getInt("rcvxp");
                item.active = (rs.getInt("active") == 1);
                item.palate = rs.getShort("palate");
                item.spalate = rs.getByte("spalate");
                item.typeMons = rs.getByte("isuse");
                item.move = rs.getByte("move");
                item.isNewMonster = rs.getByte("isnew");
                list.put(item.id, item);
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
       
    }

    public Vector<ShopTemplate> loadShopTemplate() {
        Vector<ShopTemplate> shopTemplates = new Vector<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            final String sql = "SELECT * FROM data_shop";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ShopTemplate item = new ShopTemplate(
                    rs.getInt("id"),
                    rs.getInt("idImg"),
                    rs.getString("name"),
                    rs.getString("decript"),
                    rs.getInt("value"),
                    rs.getInt("price"),
                    rs.getInt("type"),
                    rs.getInt("tab"),
                    rs.getInt("sell"),
                    rs.getInt("priceSale")
                );
                shopTemplates.add(item);
            }
            try { rs.close(); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { stmt.close(); } catch (Exception ignored) {}
            try { Database.connPool.free(conn); } catch (Exception ignored) {}
        }
        return shopTemplates;
    }
    

    public Vector<PotionTemplate2> loadPotionTemplate() {
        Vector<PotionTemplate2> potionTemplates2 = new Vector<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            final String sql = "SELECT * FROM data_potion";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                PotionTemplate2 potion = new PotionTemplate2(
                    rs.getShort("id"),
                    rs.getShort("idImage"),
                    rs.getShort("delay"),
                    rs.getBoolean("isTrade"),
                    rs.getString("name"),
                    rs.getString("name2"),
                    rs.getInt("price"),
                    rs.getInt("recovered")
                );
                potionTemplates2.add(potion);
            }
            try { rs.close(); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { stmt.close(); } catch (Exception ignored) {}
            try { Database.connPool.free(conn); } catch (Exception ignored) {}
        }
        return potionTemplates2;
    }

    
   

    public static boolean isPhiPhong(int id) {
        return (id >= 584 && id <= 597) || id == 608;
    }

   

   




    public void getTopPhaoHoa() {
        if (Char.getDayTime(0L).compareTo("2017-10-20 10:50:00") >= 0) {
            return;
        }
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            int next = 100000;
            int start = 0;
            int count = -1;
            int stop = 0;
            while (true) {
                if (count == 0) {
                    if (stop == 20) {
                        break;
                    }
                    ++stop;
                }
                if (count > 0) {
                    count = 0;
                }
                String sql = "select lastlog, pInfo,id,charname,lastlv,ban,potion from tob_char where id>=" + start + " and id<" + next + " and ban=0";
                System.out.println(start + " >> " + next);
                start = next;
                next += 100000;
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    ++count;
                    String lastlog = rs.getString("lastlog");
                    try {
                        String[] data = Char.split(rs.getString("pInfo"), ",");
                        String[] potion = Char.split(rs.getString("potion"), ",");
                        int bi = 0;
                        try {
                            bi = Integer.parseInt(data[90]);
                        } catch (Exception ignored) {
                        }
                        if (bi <= 0) {
                            continue;
                        }
                        System.out.println(rs.getString("charname") + " " + bi);
                        Logger.logString(rs.getString("charname"), "longdenchar.txt");
                        Logger.logString(String.valueOf(bi), "longden.txt");
                        Logger.logString(lastlog, "longdendaylog.txt");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }


    public void updateGemItem2Char(int chardbid, int[] idGem, int[] nItem) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "select * from tob_gem_new where owner='" + chardbid + "'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String[] data = Char.split(rs.getString("soluong"), ",");
                String[] data2 = Char.split(rs.getString("slock"), ",");
                int[] totalGem = new int[Map.gemTemplate.length];
                int[] totalGemLock = new int[Map.gemTemplate.length];
                for (int i = 0; i < data.length; ++i) {
                    try {
                        totalGem[i] = Integer.parseInt(data[i]);
                    } catch (Exception ignored) {
                    }
                    try {
                        totalGemLock[i] = Integer.parseInt(data2[i]);
                    } catch (Exception ignored) {
                    }
                }
                for (int i = 0; i < idGem.length; ++i) {
                    int n = idGem[i];
                    totalGem[n] += nItem[i];
                }
                StringBuilder soluong = new StringBuilder(String.valueOf(totalGem[0]));
                StringBuilder soluonglock = new StringBuilder(String.valueOf(totalGemLock[0]));
                for (int j = 1; j < totalGem.length; ++j) {
                    soluong.append(",").append(totalGem[j]);
                    soluonglock.append(",").append(totalGemLock[j]);
                }
                this.saveCharGem(soluong.toString(), rs.getInt("owner"), soluonglock.toString());
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void giveGemItem() {
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            int count = 0;
            int i = 0;
            Vector<String> name = readText("trangan.txt");
            int[] idGem = {73, 80, 87, 94, 101, 108, 115, 122, 129, 136};
            int[] soluong = {5, 5, 5, 5, 5, 1, 1, 1, 1, 1};
            while (i < name.size()) {
                String charname = name.get(i);
                String sql1 = "select * from tob_char where charname='" + charname + "'";
                ResultSet rs2 = stmt2.executeQuery(sql1);
                if (rs2.next()) {
                    System.out.println(name.get(i) + " >> " + sql1);
                    ++count;
                    this.updateGemItem2Char(rs2.getInt("id"), idGem, soluong);
                    Logger.logString(name.get(i), "datra4.txt");
                } else {
                    Logger.logString(name.get(i), "saitenchar4.txt");
                }
                try {
                    rs2.close();
                } catch (Exception ignored) {
                }
                ++i;
            }
            System.out.println("SO LUONG CHO " + count);
            try {
                Thread.sleep(10000L);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        } finally {
            try {
                Database.connPool.free(conn);
                Database.connPool.closeIdleConnection();
            } catch (Exception ignored) {
            }
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                stmt2.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
            Database.connPool.closeIdleConnection();
        } catch (Exception ignored) {
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            stmt2.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

    
    public void giveItem(String charname) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "select * from tob_char where charname='" + charname + "'";
            rs = stmt.executeQuery(sql);
            Char p = null;
            if (rs.next()) {
                p = new Char(null);
                p.charDBID = rs.getInt("id");
                for (int k = 0; k < 9; ++k) {
                    int[] templateThue = {19, 45, 71, 21, 47, 73, 23, 49, 75};
                    int[] template = {19, 45, 71, 21, 47, 73, 23, 49, 75};
                    int[] maintm = {3, 3, 3, 3, 3, 3, 3, 3, 3};
                    ItemTemplates itTemp = Map.itemTemplates.get(maintm[k]).get(templateThue[k]);
                    Item item = new Item(itTemp, false, 2, maintm[k], template[k]);
                    for (int i = 0; i < 10; ++i) {
                        short[] atb = item.atb;
                        atb[i] += (short) (item.atb[i] * 20 / 100);
                    }
                    int[] plus = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10, 10};
                    for (int j = 0; j < 7; ++j) {
                        ++item.plus;
                        for (int l = 0; l < 10; ++l) {
                            short[] atb2 = item.atb;
                            atb2[l] += (short) ((l > 0) ? 2 : plus[item.plus]);
                        }
                    }
                    item.place = 0;
                    item.owner = p.charDBID;
                    item.colorName = 4;
                    item.plus = 15;
                    this.saveItem(conn, item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void giveItem() {
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            int next = 100000;
            int start = 0;
            while (true) {
                String sql = "select * from tob_char where id>=" + start + " and id<" + next;
                start = next;
                next += 100000;
                rs = stmt.executeQuery(sql);
                Char p = null;
                while (rs.next()) {
                    p = new Char(null);
                    p.charDBID = rs.getInt("id");
                    System.out.println(p.charDBID + " >>>");
                    p.dbPotion = rs.getString("potion");
                    if (p.dbPotion != null) {
                        String[] data = Char.split(p.dbPotion, ",");
                        for (int i = 0; i < data.length; ++i) {
                            try {
                                p.potions[i] = Integer.parseInt(data[i]);
                            } catch (Exception ignored) {
                            }
                        }
                        p.potions[25] = 1;
                        p.potions[26] = 1;
                        StringBuilder pinfo = new StringBuilder(String.valueOf(p.getxu()));
                        for (int j = 1; j < p.potions.length; ++j) {
                            pinfo.append(",").append(p.potions[j]);
                        }
                        String sqla = "update tob_char set potion='" + pinfo + "' where id=" + p.charDBID;
                        stmt2.executeUpdate(sqla);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void giveSpecialItem2Char(String charname, int idItem, int nItem) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "select * from tob_char where charname='" + charname + "'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                for (int i = 0; i < nItem; ++i) {
                    ShopTemplate item = Map.getShopTemplate(idItem);
                    ShopTemplate spItem = new ShopTemplate();
                    spItem.coppy(spItem, item);
                    spItem.ownerId = rs.getInt("id");
                    this.saveSpecialItem(conn, spItem);
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void giveSpecialItem() {
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            int next = 200000;
            int start = 100000;
            int count = -1;
            while (count != 0) {
                if (count > 0) {
                    count = 0;
                }
                String sql = "select * from tob_char where id>=" + start + " and id<" + next;
                System.out.println(start + " >> " + next);
                start = next;
                next += 100000;
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    ++count;
                    int charId = rs.getInt("id");
                    System.out.println(charId);
                    for (int i = 0; i < 1; ++i) {
                        ShopTemplate item = Map.getShopTemplate(0);
                        ShopTemplate spItem = new ShopTemplate();
                        spItem.coppy(spItem, item);
                        spItem.ownerId = charId;
                        this.saveSpecialItem(conn, spItem);
                    }
                }
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * CODE CHET - khong con duong nao goi toi, va tep no doc KHONG TON TAI.
     *
     * Da kiem ngay 02/09/2026: khong mot cho nao trong src/, test/, tools/, web/ goi
     * setPotionFromFile, va grep toan bo 878 class trong lib/NQSH_5h.jar cung khong co
     * tham chieu nao (nen khong phai bi goi qua reflection tu ma nguon cu).
     *
     * `map/data.txt` khong ton tai trong repo. TeamServer.readData nuot moi loi
     * (catch rong) roi tra ve Vector RONG, nen neu co ai goi thi vong lap ben duoi chay 0
     * lan - im lang khong lam gi, khong bao loi. Tuc trang thai "hong" va "binh thuong"
     * cua ham nay nhin nhu nhau, do la ly do no nam day lau ma khong ai biet.
     *
     * De lai vi day tung la tien ich chay tay (cong 1 binh o vi tri 27 cho danh sach nhan
     * vat ghi trong tep). Muon dung lai thi phai tu tao map/data.txt - moi dong mot ten
     * nhan vat. DUNG xoa tep nao trong map/ vi tuong ham nay can.
     */
    public void setPotionFromFile() {
        Vector<String> data = TeamServer.readData("map/data.txt", "data.txt");
        Connection conn = null;
        Statement stmt = null;
        Char p = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            for (String datum : data) {
                String sql = "SELECT * FROM tob_char where charname='" + datum + "'";
                System.out.println(datum + ">>");
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    p = new Char(null);
                    p.charDBID = rs.getInt("id");
                    String[] ps = Char.split(rs.getString("potion"), ",");
                    for (int j = 0; j < ps.length; ++j) {
                        try {
                            p.potions[j] = Integer.parseInt(ps[j]);
                        } catch (Exception ignored) {
                        }
                    }
                    int[] potions = p.potions;
                    final int n = 27;
                    ++potions[n];
                    StringBuilder cpos = new StringBuilder(String.valueOf(p.getxu()));
                    for (int k = 1; k < p.potions.length; ++k) {
                        cpos.append(",").append(p.potions[k]);
                    }
                    Statement stmt2 = null;
                    stmt2 = conn.createStatement();
                    sql = "update tob_char set potion='" + cpos + "' where id=" + p.charDBID;
                    stmt2.executeUpdate(sql);
                    try {
                        stmt2.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rs.close();
                    } catch (Exception ignored) {
                    }
                }
            }
            try {
                Thread.sleep(3000L);
            } catch (Exception ignored) {
            }
            System.exit(0);
        } catch (Exception ignored) {
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }
    
    public void setPotion() {
        Char p = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            final int start = 0;
            final int next = 100000;
            int count = -1;
            while (true) {
                String sql = "SELECT * FROM tob_char where lastlv>30";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    ++count;
                    p = new Char(null);
                    p.charDBID = rs.getInt("id");
                    String[] ps = Char.split(rs.getString("potion"), ",");
                    for (int i = 0; i < ps.length; ++i) {
                        try {
                            p.potions[i] = Integer.parseInt(ps[i]);
                        } catch (Exception ignored) {
                        }
                    }
                    int[] potions = p.potions;
                    final int n = 27;
                    ++potions[n];
                    StringBuilder cpos = new StringBuilder(String.valueOf(p.getxu()));
                    for (int j = 1; j < p.potions.length; ++j) {
                        cpos.append(",").append(p.potions[j]);
                    }
                    Statement stmt2 = null;
                    stmt2 = conn.createStatement();
                    sql = "update tob_char set potion='" + cpos + "' where id=" + p.charDBID;
                    stmt2.executeUpdate(sql);
                    System.out.println(count + " " + start);
                    try {
                        stmt2.close();
                    } catch (Exception ignored) {
                    }
                }
                System.out.println();
                System.err.println(count + " ><><> " + start);
                System.exit(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("LOI GET CHAR");
            this.notFalse = true;
        } finally {
            System.out.println("DONG");
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    public boolean bakupChar() {
        Char p = null;
        byte[] he = {4, 2, 0, 3, 1};
        short[] basic = {400, 400, 500, 350, 350};
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            Vector<String> nameHack = new Vector<>();
            nameHack = readText("hack.txt");
            while (nameHack.size() > 0) {
                String name = nameHack.remove(0);
                System.out.println(name);
                String sql = "SELECT * FROM tob_char where charname='" + name + "'";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    p = new Char(null);
                    p.charDBID = rs.getInt("id");
                    p.headStyle = rs.getByte("headStyle");
                    byte gender = rs.getByte("gender");
                    if (gender > 0) {
                        p.gender = gender;
                        p.isNewClient = true;
                    } else if (p.headStyle % 2 == 0) {
                        p.gender = 1;
                    } else {
                        p.gender = 2;
                    }
                    p.charname = rs.getString("charname");
                    p.charClass = rs.getByte("class");
                    p.lastLV = rs.getShort("lastLv");
                    Vector<Date> nt = new Vector<>();
                    Vector<Item> item = this.getChar(p, p.charDBID, nt);
                    while (item.size() > 0) {
                        Item it = item.remove(0);
                        this.deleteItem(it.dbid);
                    }
                    while (p.gemItem.size() > 0) {
                        this.deleteGemItem(p.gemItem.remove(0).dbid);
                    }
                    while (p.specialItem.size() > 0) {
                        this.deleteGemItem(p.specialItem.remove(0).dbid);
                    }
                    this.delQuest(p.charDBID);
                    this.resetChar(p.headStyle, p.charDBID, p.charClass, p.gender, p.charname, p.lastLV, conn);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("LOI GET CHAR");
            this.notFalse = true;
            return false;
        } finally {
            System.out.println("DONG");
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        System.out.println("DONG");
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        Logger.logString(String.valueOf(this.sid), "update.txt");
        System.exit(0);
        return true;
    }

    public void delInboxMsg(int id) {
        Connection conn = null;
        Statement pre = null;
        try {
            String sql = "delete from tob_inbox where id=" + id;
            conn = this.getConnection();
            pre = conn.createStatement();
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void delInboxMsg(String charname) {
        Connection conn = null;
        Statement pre = null;
        try {
            String sql = "delete from tob_inbox where charname='" + charname + "'";
            conn = this.getConnection();
            pre = conn.createStatement();
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void delMsgClan(int id) {
        Connection conn = null;
        Statement pre = null;
        try {
            String sql = "delete from tob_clan_msg where id=" + id;
            conn = this.getConnection();
            pre = conn.createStatement();
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<MsgClanInfo> getMsgClan(int id) {
        Vector<MsgClanInfo> msg = null;
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_clan_msg where id_clan=" + id;
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                if (msg == null) {
                    msg = new Vector<>();
                }
                MsgClanInfo m = new MsgClanInfo();
                m.id = rs.getInt("id");
                m.sender = rs.getString("charname");
                m.info = rs.getString("info");
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
                m.date = dayFormat.format(rs.getDate("datesend").getTime());
                msg.add(m);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return msg;
    }

    public void addNewMsgClan(Char player, String msg) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            String sql = "insert into tob_clan_msg (charname,info,id_clan,dateSend) values ('" + player.charname + "','" + msg + "','" + player.idClan + "',Now())";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public int addNewMessage(String charname, CharInboxMessage newMessage) {
        return 0;
    }

    public Vector<CharInboxMessage> getInbox(String charname) {
        Vector<CharInboxMessage> inbox = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_inbox WHERE charname=? order by dateSend desc";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            while (rs.next()) {
                CharInboxMessage ib = new CharInboxMessage();
                ib.sender = rs.getString("sender");
                ib.info = rs.getString("info");
                ib.idMsg = rs.getInt("id");
                inbox.add(ib);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return inbox;
    }

    public void getFriendlist(Char p) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT * FROM tob_friendlist WHERE charId=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, p.charDBID);
            rs = pre.executeQuery();
            if (rs.next()) {
                String[] fid = Char.split(rs.getString("friendId"), ",");
                p.friendListID.removeAllElements();
                p.friendListName.removeAllElements();
                int ischange = rs.getByte("ischange");
                for (String s : fid) {
                    if (!s.trim().equals("")) {
                        int id = Integer.parseInt(s.trim());
                        if (ischange == 0) {
                            if (Map.openLog) {
                                if (TeamServer.server == 3) {
                                    id += 2407770;
                                } else if (TeamServer.server == 2) {
                                    id += 2886804;
                                }
                            } else if (TeamServer.server == 1) {
                                id += 1497885;
                            }
                        }
                        CharInfo info = this.getInfoChar(id);
                        if (info != null) {
                            info.idDB = id;
                            p.myFriend.add(info);
                            p.friendListID.add(id);
                            p.friendListName.add(info.name);
                        }
                    }
                }
                if (ischange == 0) {
                    this.saveFriendList(p);
                }
            }
            if (p.charname.trim().equals("cungday")) {
                System.out.println("TONG SO BAN " + p.friendListID.size() + " >> " + p.friendListName.size());
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateLienTram() {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char_luong set nPk=0";
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            conn.close();
        } catch (Exception ignored) {
        }
    }

    public void saveTopienTram(Vector<CharInfo> top) {
        Connection conn = null;
        try {
            conn = this.getConnection();
            for (CharInfo c : top) {
                String sql = "update tob_char_luong set nPK=" + c.pk + " where charname='" + c.name + "'";
                PreparedStatement pre = conn.prepareStatement(sql);
                System.out.println(sql);
                pre.execute();
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<CharInfo> getTopCountry(int country, int typeTop) {
        Vector<CharInfo> top = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_char_luong where country=" + country + " order by exp desc limit 50";
            if (typeTop == 1) {
                sql = "select * from tob_char_luong where country=" + country + " order by honor desc limit 50";
            } else if (typeTop == 2) {
                sql = "select * from tob_char_luong where country=" + country + " order by nPK desc limit 10";
            }
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            int count = 0;
            while (rs.next()) {
                ++count;
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.money = rs.getLong("gold");
                charinfo.idClan = rs.getShort("idClan");
                charinfo.luong = rs.getInt("luong");
                charinfo.honor = rs.getInt("honor");
                charinfo.pk = rs.getInt("nPK");
                String info = rs.getString("wearing");
                if (!info.equals("-1")) {
                    try {
                        if (info == null || info.trim().equals("")) {
                            String[] data = Char.split(info.trim(), ",");
                            if (data.length > 0) {
                                charinfo.country = Byte.parseByte(data[0]);
                                for (short i = 1; i < data.length; i += 4) {
                                    ItemInfo item = new ItemInfo();
                                    item.idTemplate = Short.parseShort(data[i]);
                                    item.charClass = Byte.parseByte(data[i + 1]);
                                    item.level = Byte.parseByte(data[i + 2]);
                                    item.plus = Byte.parseByte(data[i + 3]);
                                    charinfo.wearingItem.add(item);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                top.add(charinfo);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        return top;
    }

    public void updateCharRaceRvcGift(Char p) {
        Connection conn = null;
        ResultSet rs = null;
        Statement stm = null;
        try {
            String sql = "update " + Database.tableRace + " set rcvGift=1 where charid=" + p.charDBID;
            conn = this.getConnection();
            stm = conn.createStatement();
            stm.executeUpdate(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public byte checkReceiveGiftGroup(Char p) {
        if (p.idGropRace == -1) {
            return -1;
        }
        Connection conn = null;
        ResultSet rs = null;
        Statement stm = null;
        Label_0308:
        {
            try {
                String sql = "select * from " + Database.tableRace + " where idgroup=" + p.idGropRace + " order by exp desc";
                conn = this.getConnection();
                stm = conn.createStatement();
                rs = stm.executeQuery(sql);
                byte i = 0;
                Block_7:
                {
                    while (rs.next()) {
                        if (i >= 3) {
                            break;
                        }
                        if (p.charname.equalsIgnoreCase(rs.getString("charname"))) {
                            break Block_7;
                        }
                        ++i;
                    }
                    break Label_0308;
                }
                if (rs.getByte("rcvGift") == 1) {
                    return -1;
                }
                return i;
            } catch (Exception ignored) {
            } finally {
                try {
                    stm.close();
                } catch (Exception ignored) {
                }
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.free(conn);
                } catch (Exception ignored) {
                }
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public Vector<CharInfo> getTopGroup(int idGroup, boolean sort) {
        Vector<CharInfo> top = new Vector<>();
        Connection conn = null;
        ResultSet rs = null;
        Statement stm = null;
        try {
            conn = this.getConnection();
            String sql = "select * from " + Database.tableRace + " where idgroup=" + idGroup + (sort ? " order by exp desc" : "");
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.level = rs.getByte("lastlv");
                String info = rs.getString("infotop");
                if (!info.equals("-1")) {
                    try {
                        String[] data = Char.split(info, ",");
                        charinfo.country = Byte.parseByte(data[0]);
                        for (short i = 1; i < data.length; i += 4) {
                            ItemInfo item = new ItemInfo();
                            item.idTemplate = Short.parseShort(data[i]);
                            item.charClass = Byte.parseByte(data[i + 1]);
                            item.level = Byte.parseByte(data[i + 2]);
                            item.plus = Byte.parseByte(data[i + 3]);
                            charinfo.wearingItem.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                top.add(charinfo);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return top;
        } finally {
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            Database.connPool.free(conn);
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        Database.connPool.free(conn);
        return top;
    }

    // Than method (126 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public Vector<CharInfo> getTopLV() {
        return DatabaseXepHang.getTopLV(this);
    }

    // Than method (122 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public Vector<CharInfo> getTopRich() {
        return DatabaseXepHang.getTopRich(this);
    }

    public int getIDClanChar(Connection conn, String nameChar) {
        Char p = CharManager.instance.getCharByCharName(nameChar.toLowerCase());
        if (p != null) {
            return p.idClan;
        }
        final int idClan = -1;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select idClan from tob_char where charname='" + nameChar + "'";
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("idClan");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        return idClan;
    }

    public Vector<NewClan> getTopClan() {
        Vector<NewClan> top = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_clan order by xp desc limit 100";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                NewClan clanInfo = new NewClan();
                clanInfo.id = rs.getShort("id_clan");
                clanInfo.name = rs.getString("clanName");
                clanInfo.master = rs.getString("charMaster");
                clanInfo.level = rs.getByte("lvClan");
                clanInfo.money = rs.getLong("money");
                String[] mb = Char.split(rs.getString("member"), ",");
                for (String s : mb) {
                    int idCharClan = this.getIDClanChar(conn, s);
                    if (idCharClan == clanInfo.id) {
                        clanInfo.addMember(s, 0);
                    }
                }
                clanInfo.slogan = rs.getString("slogan");
                clanInfo.country = rs.getByte("country");
                top.add(clanInfo);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return top;
    }

    public Vector<String> getTopHoa() {
        Vector<String> top = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_char order by ticket desc limit 5";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                String st = rs.getString("charname") + "_" + rs.getInt("ticket");
                top.add(st);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return top;
    }

    public CharInfo getInfoChar(int charDBID, String charnameGet) {
        final String st = "";
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Statement stm = null;
        ResultSet rss = null;
        try {
            final String sql = "select * from tob_char where id=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, charDBID);
            rs = pre.executeQuery();
            if (rs.next()) {
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lastLv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.idClan = rs.getByte("idClan");
                try {
                    String info = rs.getString("infotop");
                    if (!info.equals("-1")) {
                        try {
                            String[] data = Char.split(info, ",");
                            charinfo.country = Byte.parseByte(data[0]);
                            for (short i = 1; i < data.length; i += 4) {
                                ItemInfo item = new ItemInfo();
                                item.idTemplate = Short.parseShort(data[i]);
                                item.charClass = Byte.parseByte(data[i + 1]);
                                item.level = Byte.parseByte(data[i + 2]);
                                item.plus = Byte.parseByte(data[i + 3]);
                                charinfo.wearingItem.add(item);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        stm.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rss.close();
                    } catch (Exception ignored) {
                    }
                }
                try {
                    stm.close();
                } catch (Exception ignored) {
                }
                try {
                    rss.close();
                } catch (Exception ignored) {
                }
                return charinfo;
            }
            System.out.println("KHONG TIM THAY " + charDBID);
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return null;
    }

    public CharInfo getInfoChar(String charname) {
        final String st = "";
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Statement stm = null;
        ResultSet rss = null;
        try {
            String sql = "select * from tob_char where charname=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            rs = pre.executeQuery();
            if (rs.next()) {
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lastLv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.titlesClan = rs.getByte("basepoint");
                charinfo.idClan = rs.getShort("idClan");
                String info = rs.getString("infotop");
                if (!info.equals("-1")) {
                    try {
                        String[] data = Char.split(info, ",");
                        charinfo.country = Byte.parseByte(data[0]);
                        for (short i = 1; i < data.length; i += 4) {
                            ItemInfo item = new ItemInfo();
                            item.idTemplate = Short.parseShort(data[i]);
                            item.charClass = Byte.parseByte(data[i + 1]);
                            item.level = Byte.parseByte(data[i + 2]);
                            item.plus = Byte.parseByte(data[i + 3]);
                            charinfo.wearingItem.add(item);
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    String[] data = Char.split(rs.getString("pInfo"), ",");
                    try {
                        charinfo.country = Byte.parseByte(data[39]);
                    } catch (Exception ignored) {
                    }
                    try {
                        sql = "select * from tob_item where owner=" + rs.getInt("id") + " and place=1";
                        stm = conn.createStatement();
                        rss = stm.executeQuery(sql);
                        StringBuilder infoWearing = new StringBuilder(String.valueOf(charinfo.country));
                        byte[] iditem = new byte[16];
                        while (rss.next()) {
                            ItemInfo item2 = new ItemInfo();
                            Item it = new Item();
                            it.dbInfo = rss.getString("dbbasic");
                            it.initInfoFromDB();
                            if (it.getType() != 13) {
                                infoWearing.append(",").append(it.getTemplate().id).append(",").append(it.clazz).append(",").append(it.level).append(",").append(it.plus);
                                if (iditem[it.getType()] != 0) {
                                    continue;
                                }
                                item2.charClass = it.clazz;
                                item2.level = (byte) it.level;
                                item2.plus = it.plus;
                                item2.idTemplate = (short) it.getTemplate().id;
                                charinfo.wearingItem.add(item2);
                                short type = it.getType();
                                ++iditem[type];
                            }
                        }
                        this.updateInfoChar(charname, infoWearing.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            stm.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            rss.close();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        stm.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rss.close();
                    } catch (Exception ignored) {
                    }
                }
                return charinfo;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return null;
    }

    public CharInfo getInfoChar(int chardbid) {
        final String st = "";
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Statement stm = null;
        ResultSet rss = null;
        try {
            String sql = "select id,charname,lastLv,headStyle,basepoint,idClan,infotop,pInfo from tob_char where id=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, chardbid);
            rs = pre.executeQuery();
            if (rs.next()) {
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lastLv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.titlesClan = rs.getByte("basepoint");
                charinfo.idClan = rs.getShort("idClan");
                String info = rs.getString("infotop");
                if (!info.equals("-1")) {
                    try {
                        String[] data = Char.split(info, ",");
                        charinfo.country = Byte.parseByte(data[0]);
                        for (short i = 1; i < data.length; i += 4) {
                            ItemInfo item = new ItemInfo();
                            item.idTemplate = Short.parseShort(data[i]);
                            item.charClass = Byte.parseByte(data[i + 1]);
                            item.level = Byte.parseByte(data[i + 2]);
                            item.plus = Byte.parseByte(data[i + 3]);
                            charinfo.wearingItem.add(item);
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    String[] data = Char.split(rs.getString("pInfo"), ",");
                    try {
                        charinfo.country = Byte.parseByte(data[39]);
                    } catch (Exception ignored) {
                    }
                    try {
                        sql = "select * from tob_item where owner=" + rs.getInt("id") + " and place=1";
                        stm = conn.createStatement();
                        rss = stm.executeQuery(sql);
                        StringBuilder infoWearing = new StringBuilder(String.valueOf(charinfo.country));
                        byte[] iditem = new byte[16];
                        while (rss.next()) {
                            ItemInfo item2 = new ItemInfo();
                            Item it = new Item();
                            it.dbInfo = rss.getString("dbbasic");
                            it.initInfoFromDB();
                            if (it.getType() != 13) {
                                infoWearing.append(",").append(it.getTemplate().id).append(",").append(it.clazz).append(",").append(it.level).append(",").append(it.plus);
                                if (iditem[it.getType()] != 0) {
                                    continue;
                                }
                                item2.charClass = it.clazz;
                                item2.level = (byte) it.level;
                                item2.plus = it.plus;
                                item2.idTemplate = (short) it.getTemplate().id;
                                charinfo.wearingItem.add(item2);
                                short type = it.getType();
                                ++iditem[type];
                            }
                        }
                        this.updateInfoChar(charinfo.name, infoWearing.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            stm.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            rss.close();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        stm.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rss.close();
                    } catch (Exception ignored) {
                    }
                }
                return charinfo;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return null;
    }

    // Than method (114 dong) da chuyen sang DatabaseXepHang.java. Chu ky GIU NGUYEN.
    public void updateCharclass() {
        DatabaseXepHang.updateCharclass(this);
    }

    public void getVeso(Char p, Connection conn) {
        ResultSet rs = null;
        Statement st = null;
        try {
            String sql = "select * from tob_xoso where owner=" + p.charDBID;
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            if (rs.next()) {
                String[] da = Char.split(rs.getString("vsxu"), "|");
                if (da.length > 0) {
                    p.dayOpenXoso = da[1];
                    String[] data = Char.split(da[0], ",");
                    if (data.length > 1) {
                        for (byte i = 0; i < data.length; i += 2) {
                            Veso vs = new Veso();
                            vs.number = Byte.parseByte(data[i]);
                            vs.soluong = Byte.parseByte(data[i + 1]);
                            p.vesoxu.add(vs);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                st.close();
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            st.close();
        } catch (Exception ignored) {
        }
    }

    public void getIdGroupRaceChar(Connection conn, Char p) {
        ResultSet rs = null;
        Statement stm = null;
        try {
            String sql = "select * from " + Database.tableRace + " where charid=" + p.charDBID;
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            if (rs.next()) {
                p.idGropRace = rs.getShort("idgroup");
                p.expRace = rs.getLong("exp");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
    }

    public int getIDClanChar(String name) {
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select * from tob_char where charname='" + name + "'";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("idClan");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void resetStateMarried(String charname, String charname1) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            if (rs.next()) {
                String[] data = Char.split(rs.getString("pInfo"), ",");
                if (data[51].equals(charname1)) {
                    data[49] = "0";
                    data[50] = "-1";
                    data[51] = "";
                    StringBuilder info = new StringBuilder(data[0]);
                    for (int i = 1; i < data.length; ++i) {
                        info.append(",").append(data[i]);
                    }
                    sql = "update tob_char set pInfo='" + info + "' WHERE charname='" + charname + "'";
                    pre = conn.prepareStatement(sql);
                    pre.execute();
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
    }

    // Than method (247 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public Vector<Item> getChar(Char p, String charname, Vector<Date> lastlog) {
        return DatabaseNhanVat.getChar(this, p,charname,lastlog);
    }

    public void addCharDanhHieu(Char p) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_danh_hieu(charname,iddh) values ('" + p.charname + "','" + p.getAllDanhHieuSave() + "' )ON DUPLICATE KEY UPDATE iddh='" + p.getAllDanhHieuSave() + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void getCharDanhHieu(Char p) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String sql = "SELECT * FROM tob_danh_hieu WHERE charname=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, p.charname);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                p.setDanhHieu(rs.getString("iddh"));
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<Item> getChar(Char p, int charId, Vector<Date> lastlog) {
        Vector<Item> pItem = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();//cặc
            String sql = "SELECT * FROM tob_char WHERE id=? and ban=0";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, charId);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                p.charDBID = charId;
                p.userID = rs.getInt("userid");
                p.tichluy = rs.getInt("tichluy");
                p.qua_npc = rs.getInt("qua_npc");
                p.tichluy_tuan = rs.getInt("tichluy_tuan");
                p.tichluy_bosung = rs.getInt("tichluy_bosung");
                
                
                if (rs.getByte("isPP") == 0) {
                    p.setBatPhiPhong();
                } else {
                    p.setTatPhiPhong();
                }
                
                if (rs.getByte("isMN") == 0) {
                    p.setBatMatNa();
                } else {
                    p.setTatMatNa();
                }

                // Cong tac "Tat cai trang" (NPC Tho ren than bi -> Bo suu tap).
                //
                // Truoc day Char.isBatCaiTrang chi la mot bien trong bo nho, mac dinh
                // true va khong duoc luu o dau — nen tat xong, dang xuat vao lai la no
                // tu bat lai. Da bao.
                //
                // DOC RIENG TRONG TRY: cot isCT la cot MOI. May chu nao chua chay
                // themCotCongTacHienThi() thi lenh doc cot nay nem SQLException,
                // ma doan nay nam giua ham nap nhan vat — de loi lan ra la HONG DANG NHAP
                // cua toan bo nguoi choi chi vi quen mot cau ALTER TABLE. Thieu cot thi
                // lui ve dung hanh vi cu (luon bat), khong ai mat gi.
                try {
                    if (rs.getByte("isCT") == 0) {
                        p.setBatCaiTrang();
                    } else {
                        p.setTatCaiTrang();
                    }
                } catch (final Exception thieuCot) {
                    p.setBatCaiTrang();
                }

                // Cong tac "Tat canh" — cung mot loi, cung mot menu NPC, cung mot cach vá.
                // Doc trong try RIENG chu khong gop chung voi isCT: gop lai thi thieu mot
                // cot lam mat luon cai kia.
                try {
                    if (rs.getByte("isCanh") == 0) {
                        p.setBatCanh();
                    } else {
                        p.setTatCanh();
                    }
                } catch (final Exception thieuCot) {
                    p.setBatCanh();
                }
                
                 
                
                p.setLuong(rs.getInt("luong"));
                p.dbInfo = rs.getString("pInfo");
                p.using = rs.getByte("del");
                p.charname = rs.getString("charname").toLowerCase();
                p.dbPotion = rs.getString("potion");
                p.dbSkill = rs.getString("skill");
                p.dbBasic = rs.getString("basic");
                p.idClan = rs.getShort("idClan");
                p.reclass = rs.getByte("reupclass");
                p.flower = rs.getShort("ticket");
                p.gotWages = (rs.getShort("x") == 1);
                p.timeXP = rs.getShort("y");
                p.gifMoonFestival = (rs.getShort("book") == 20);
                lastlog.add(rs.getDate("lastlog"));
                p.lastLog = rs.getString("lastlog");
                p.giftTop = rs.getByte("nAr1");
                p.setxu(rs.getLong("gold"));
                if (!NewClan.isTimeOfferWages()) {
                    p.gotWages = false;
                }
                try {
                    p.rankClan = rs.getByte("basepoint");
                    if (p.rankClan < 0) {
                        p.rankClan = 3;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p.initListKillMe(rs.getString("killme"));
                this.getFriendlist(p);
                p.equip = rs.getString("equip");
                p.invent = rs.getString("inven");
                p.bag = rs.getString("bag");
                p.tuido = rs.getString("tuido");
                if (p.equip == null) {
                    sql = "SELECT * FROM tob_item WHERE owner=?";
                    try {
                        pre.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rs.close();
                    } catch (Exception ignored) {
                    }
                    pre = conn.prepareStatement(sql);
                    pre.setInt(1, charId);
                    rs = pre.executeQuery();
                    int nItem = 0;
                    while (rs.next()) {
                        ++nItem;
                        Item item = new Item();
                        item.dbid = rs.getInt("id");
                        int int1 = rs.getInt("owner");
                        item.dbowner = int1;
                        item.owner = int1;
                        item.dbInfo = rs.getString("dbbasic");
                        item.dbAttribute = rs.getString("allAtt");
                        item.timeLoan = rs.getLong("timeLoan");
                        try {
                            item.identify = rs.getInt("atb0");
                            item.switchAttNguhanh = rs.getShort("atb1");
                            item.resetItem = rs.getShort("atb2");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        pItem.add(item);
                    }
                }
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
                boolean haveNew = false;
                sql = "SELECT * FROM tob_gem_new WHERE owner=?";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                if (rs.next()) {
                    haveNew = true;
                    String[] soluong = Char.split(rs.getString("soluong"), ",");
                    int chardbid = rs.getInt("owner");
                    for (int i = 0; i < p.listGemitem.length; ++i) {
                        try {
                            int temsl = Integer.parseInt(soluong[i]);
                            if (temsl > 0) {
                                GemItem gem = new GemItem(i);
                                gem.ownerId = chardbid;
                                gem.realId = p.getIDItem();
                                p.gemItem.add(gem);
                                p.listGemitem[i] = temsl;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    String[] slock = Char.split(rs.getString("slock"), ",");
                    for (int j = 0; j < p.listGemitemLock.length; ++j) {
                        try {
                            int temsl2 = Integer.parseInt(slock[j]);
                            if (temsl2 > 0) {
                                p.addLockGem(j, temsl2);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
                boolean haveSpNew = false;
                sql = "SELECT * FROM tob_sp_new WHERE owner=?";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                if (rs.next()) {
                    haveSpNew = true;
                    String[] soluong2 = Char.split(rs.getString("soluong"), ",");
                    int chardbid2 = rs.getInt("owner");
                    for (int j = 0; j < soluong2.length; ++j) {
                        try {
                            int temsl2 = Integer.parseInt(soluong2[j]);
                            if (temsl2 > 0) {
                                ShopTemplate gem2 = new ShopTemplate();
                                ShopTemplate g = Map.getShopTemplate(j);
                                gem2.coppy(gem2, g);
                                gem2.ownerId = chardbid2;
                                gem2.realId = p.getIDItem();
                                p.specialItem.add(gem2);
                                p.listSpItem[j] = temsl2;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
                String lastlogout = null;
                try {
                    lastlogout = Char.split(p.lastLog.trim(), ".")[0].trim();
                } catch (Exception ignored) {
                }
                sql = "SELECT * FROM tob_animal WHERE owner=? and del=0";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                while (rs.next()) {
                    Animal animal = new Animal();
                    animal.place = rs.getByte("place");
                    animal.ownerId = rs.getInt("owner");
                    animal.dbid = rs.getInt("id");
                    animal.idImg = rs.getByte("id_img");
                    animal.dbownerId = animal.ownerId;
                    animal.initAtt(rs.getString("att"));
                    animal.id = p.getIDItem();
                    animal.level = rs.getByte("lv");
                    animal.name = rs.getString("name");
                    animal.texpire = rs.getLong("texpire");
                    animal.isHoaHinh = rs.getInt("isHoaHinh");
                    animal.timeHoaHinh =rs.getLong("timeHoaHinh");
                    if (lastlogout != null && lastlogout.compareTo("2017-12-28 11:40:00") < 0 && animal.idImg == Animal.TUAN_LOC) {
                        continue;
                    }
                    if (TeamServer.isServerHoaLu() && lastlogout != null && lastlogout.compareTo("2020-04-13 10:26:00") < 0 && (animal.idImg == Animal.PHUONG_HOANG || animal.idImg == Animal.PHUONG_HOANG_7MAU || animal.idImg == Animal.PHUONG_HOANG_BANG || animal.idImg == Animal.HEO || animal.idImg == Animal.NGUA_XUONG) && animal.texpire > 0L) {
                        long[] day = {3L, 5L, 7L};
                        animal.texpire = System.currentTimeMillis() + day[Map.r.nextInt(day.length)] * 24L * 60L * 60000L;
                        this.saveAnimal(animal);
                    }
                    if (animal.timeExpire()) {
                        this.delAnimal(animal.dbid);
                    } else {
                        p.animal.add(animal);
                        if (animal.place != 1) {
                            continue;
                        }
                        p.animalRide = animal;
                    }
                }
                this.getInfoCharClan(p);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            }
            this.getCharPet(p, conn);
            this.getVeso(p, conn);
            this.getIdGroupRaceChar(conn, p);
            this.getCharQuest(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return pItem;
    }

    public void traGemNgude(Char p) {
        boolean isExist = false;
        for (int i = 0; i < Database.namechar.length; ++i) {
            if (p.charname.equals(Database.namechar[i].trim().toLowerCase())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        StringBuilder infoTra = new StringBuilder("tra gem= ");
        try {
            conn = this.getConnection();
            final String sql = "SELECT * FROM tob_gem_new_2511 WHERE owner=?";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, p.charDBID);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                String[] soluong = Char.split(rs.getString("soluong"), ",");
                for (int j = 0; j < p.listGemitem.length; ++j) {
                    try {
                        int temsl = Integer.parseInt(soluong[j]);
                        if (temsl > 0) {
                            p.doAddGemItem(j, temsl, false);
                            infoTra.append(Map.gemTemplate[j].name).append(":").append(temsl).append(",");
                        }
                    } catch (Exception ignored) {
                    }
                }
                infoTra.append("tra gem lock= ");
                String[] slock = Char.split(rs.getString("slock"), ",");
                for (int k = 0; k < p.listGemitemLock.length; ++k) {
                    try {
                        int temsl2 = Integer.parseInt(slock[k]);
                        if (temsl2 > 0) {
                            p.addLockGem(k, temsl2);
                            infoTra.append(Map.gemTemplate[k].name).append(":").append(temsl2).append(",");
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            this.saveOrtherLog("", p.charname, infoTra.toString(), "tragem");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void delAnimal(int idAnimal) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            String sql = "UPDATE tob_animal SET del=1 where id=" + idAnimal;
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateTimeJoinClan(String charname) {
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select pInfo from tob_char where charname='" + charname + "'";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            if (rs.next()) {
                Char p = new Char(null);
                p.dbInfo = rs.getString("pInfo");
                p.initPinfo();
                this.updatePInfoChar(p);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updatePInfoChar(Char p) {
        final String sql = "UPDATE tob_char SET pInfo=? where charname=?";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, p.getDBInfo());
            pre.setString(2, p.charname);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateCharIDClan(String charname, int idClan) {
        final String sql = "UPDATE tob_char SET idClan=? where charname=?";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, idClan);
            pre.setString(2, charname);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<CharClan> getTopMemberClan(int idClan) {
        Vector<CharClan> topMem = new Vector<>();
        final String sql = "select charname,basepoint from tob_char where idClan=? and basepoint<3 order by basepoint";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, idClan);
            ResultSet rs = pre.executeQuery();
            int phobang = 0;
            int truonglao = 0;
            while (rs.next()) {
                CharClan cl = new CharClan();
                cl.charname = rs.getString("charname").toLowerCase();
                cl.rankClan = rs.getByte("basepoint");
                if (phobang < 2 && cl.rankClan == 1) {
                    ++phobang;
                    topMem.add(cl);
                } else if (truonglao < 7 && cl.rankClan == 2) {
                    ++truonglao;
                    topMem.add(cl);
                } else {
                    Char p = CharManager.instance.getCharByCharName(cl.charname);
                    if (p != null) {
                        p.rankClan = 3;
                    } else {
                        this.updateRankClan(cl.charname, 3);
                    }
                }
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return topMem;
    }

    public void updateAllTitleClanMaster() {
        final String sql = "select * from tob_clan";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            ResultSet rs = pre.executeQuery();
            while (rs.next()) {
                String charMaster = rs.getString("charmaster");
                this.updateRankClan(charMaster, 0);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateRankClan(String charname, int titles) {
        final String sql = "UPDATE tob_char SET basepoint=? where charname=?";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setInt(1, titles);
            pre.setString(2, charname);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateInfoAdmin(String info) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "update admin_msg set msg='" + info + "'";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            TeamServer.haveMsgAdmin = true;
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void getInfoAdmin() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from admin_msg";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                ((AdminHandler) RealController.getHandler()).adminRequest(rs.getString("msg").toLowerCase(), false);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateAllTimeXP() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char set y=0";
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateInfoChar(String charname, String info) {
        final String sql = "UPDATE tob_char SET infotop=? where charname=?";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, info);
            pre.setString(2, charname);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void savecharLuong(Connection conn, Char p) {
        PreparedStatement pre = null;
        PreparedStatement pre2 = null;
        try {
            String sql = "insert into tob_char_luong(charname,country,exp,idclan,honor,lv,wearing,luong,gold,headStyle) values(?,?,?,?,?,?,?,?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setString(1, p.charname);
            pre.setByte(2, p.myCountry);
            pre.setLong(3, p.lvDetail.getExp());
            pre.setShort(4, p.idClan);
            pre.setInt(5, p.honor);
            pre.setInt(6, p.lastLV);
            pre.setString(7, p.infoWearing);
            pre.setInt(8, (int) p.luonglost);
            pre.setByte(9, p.headStyle);
            pre.setLong(10, p.getxu());
            try {
                pre.execute();
            } catch (Exception e) {
                sql = "update tob_char_luong set country=?,exp=?,idclan=?,honor=?,lv=?,wearing=?,luong=?,gold=?,headStyle=? where charname=?";
                pre2 = conn.prepareStatement(sql);
                pre2.setByte(1, p.myCountry);
                pre2.setLong(2, p.lvDetail.getExp());
                pre2.setShort(3, p.idClan);
                pre2.setInt(4, p.honor);
                pre2.setInt(5, p.lastLV);
                pre2.setString(6, p.infoWearing);
                pre2.setInt(7, (int) p.luonglost);
                pre2.setLong(8, p.getxu());
                pre2.setByte(9, p.headStyle);
                pre2.setString(10, p.charname);
                pre2.execute();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre2.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
    }

    // Than method (153 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public void saveCharBackup(Connection conn, Char p) {
        DatabaseNhanVat.saveCharBackup(this, conn,p);
    }

    public void updateCharRace(Connection conn, Char p) {
        PreparedStatement pre = null;
        try {
            String sql = "update " + Database.tableRace + " set exp=?,lastlv=?,infotop=?,lvchar=? where charid=?";
            pre = conn.prepareStatement(sql);
            pre.setLong(1, p.expRace);
            pre.setInt(2, LevelDetail.getLevelFromExp(p.expRace));
            pre.setString(3, p.infoWearing);
            pre.setInt(4, p.lvDetail.lv);
            pre.setInt(5, p.charDBID);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
    }

    public void updateCharPotion(Char p) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            final String sql = "UPDATE tob_char SET potion=? where id=?";
            StringBuilder potion = new StringBuilder(String.valueOf(p.getxu()));
            for (int i = 1; i < p.potions.length; ++i) {
                potion.append(",").append(p.potions[i]);
            }
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, potion.toString());
            pre.setInt(2, p.charDBID);
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateRcvGiftTop(String charName) {
        Connection conn = null;
        Statement pre = null;
        try {
            String sql = "UPDATE tob_char SET nAr1=0 where charname='" + charName + "'";
            conn = this.getConnection();
            pre = conn.createStatement();
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public static void loadAllCharSell() {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            final String sql = "select * from tob_market ";
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                String charname = rs.getString("charname").toLowerCase();
                long money = rs.getLong("money");
                String info = rs.getString("itemsell");
                String items = rs.getString("items");
                String itembid = rs.getString("listBid");
                ItemMarket.setItemSell(info, money, charname.toLowerCase().trim(), items, itembid);
            }
            ItemMarket.sortAllItemSell();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateMoneyCharSell(CharSellItem p) {
        Connection conn = null;
        Statement pre = null;
        try {
            conn = this.getConnection();
            String sql = "update tob_market set money=" + p.money + " where charname='" + p.charname + "'";
            pre = conn.createStatement();
            pre.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveAllCharSell(CharSellItem p) {
        String allItem = p.getAllItemSell();
        String allItemBack = p.getAllItemExpireSave();
        String allItemBid = p.getListItemBidSave();
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String sql = "INSERT INTO tob_market set charname=?,itemsell=?,money=?,items=?,listBid=? ON DUPLICATE KEY UPDATE itemsell=?,money=?,items=?,listBid=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, p.charname);
            pre.setString(2, allItem);
            pre.setLong(3, p.money);
            pre.setString(4, allItemBack);
            pre.setString(5, allItemBid);
            pre.setString(6, allItem);
            pre.setLong(7, p.money);
            pre.setString(8, allItemBack);
            pre.setString(9, allItemBid);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadAllMonsterVantieu() {
        int[][] idMonster = {{95, 96, 97, 98, 99, 100}, {101, 102, 103, 104, 105, 106}, {107, 108, 109, 110, 111, 112}};
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_vantieu";
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                int idchar = rs.getInt("idchar");
                String namemaster = rs.getString("namechar");
                int typetieu = rs.getInt("typetieu");
                long timefinish = rs.getLong("tReceive");
                int lv = rs.getInt("lv");
                int mapid = rs.getInt("mapid");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int hp = rs.getInt("hp");
                int maxhp = rs.getInt("maxhp");
                int country = rs.getInt("idcountry");
                Map map = RealController.mapList.get(mapid);
                MonsterVantieu monster = new MonsterVantieu(map, Map.monsterTemplates.get(idMonster[typetieu / 6][typetieu % 6]), x, y, country);
                monster.charDbid = idchar;
                monster.level = lv;
                monster.maxhp = maxhp;
                monster.hp = hp;
                if (maxhp <= 0) {
                    monster.maxhp = Char.HP_MONSTER_VANTIEU[(lv - 40) / 10] + Char.HP_MONSTER_VANTIEU[(lv - 40) / 10] * Char.UP_HP_MONSTER_VANTIEU[(lv - 40) / 10] / 100;
                    monster.hp = monster.maxhp;
                }
                monster.defend_magic = Char.DEF_MONSTER_VANTIEU[(lv - 40) / 10] + Char.DEF_MONSTER_VANTIEU[(lv - 40) / 10] * Char.UP_DEF_MONSTER_VANTIEU[(lv - 40) / 10] / 100;
                monster.defend_physic = monster.defend_magic;
                monster.typeTieu = (byte) typetieu;
                monster.setTimeFinish(timefinish);
                monster.namemaster = namemaster;
                if (!monster.isFinish()) {
                    monster.id = RealController.intance.idGen.getID(1, "");
                    MonsterVantieu.addMonster(monster);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                st.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            st.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveMonsterVantieu(MonsterVantieu mons) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String sql = "INSERT INTO tob_vantieu set idchar=?,namechar=?,typetieu=?,tReceive=?,lv=?,mapid=?,x=?,y=?,idcountry=?,hp=?,maxhp=? ON DUPLICATE KEY UPDATE typetieu=?,lv=?,mapid=?,x=?,y=?,hp=?,maxhp=?,tReceive=?";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, mons.charDbid);
            pre.setString(2, mons.namemaster);
            pre.setByte(3, mons.typeTieu);
            pre.setLong(4, mons.timeFinish);
            pre.setShort(5, (short) mons.level);
            pre.setShort(6, (short) mons.map.mapId);
            pre.setShort(7, (short) mons.x);
            pre.setShort(8, (short) mons.y);
            pre.setByte(9, mons.inCountry);
            pre.setLong(10, mons.hp);
            pre.setLong(11, mons.maxhp);
            pre.setByte(12, mons.typeTieu);
            pre.setShort(13, (short) mons.level);
            pre.setShort(14, (short) mons.map.mapId);
            pre.setShort(15, (short) mons.x);
            pre.setShort(16, (short) mons.y);
            pre.setLong(17, mons.hp);
            pre.setLong(18, mons.maxhp);
            pre.setLong(19, mons.timeFinish);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public int getDiemLuongXaiNhanDiem(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_top_event where charname='" + charname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("luong");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return 0;
    }

    public int getRankLoiDai(int point, String timeUpdate, int idNhom) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select count(id) as a from tob_char_thach_dau where nhom=" + idNhom + " and point>" + point;
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getInt("a") + 1;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public Vector<CharInfo> getTopLoiDai(int idNhom) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Vector<CharInfo> top = new Vector<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM tob_char_thach_dau where nhom=" + idNhom + " order by point desc limit 10";
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return top;
    }

    public void loadAllCharThiDau(int index) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_char_thach_dau where nhom=" + index + " order by point desc";
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            while (rs.next()) {
                CharThiDau c = new CharThiDau(rs.getString("charname"), rs.getInt("lv"), rs.getInt("id"));
                c.nhom = index;
                c.point = rs.getInt("point");
                Map.ALL_CHAR_LOI_DAI.get(index).put(c.charDbId, c);
                Map.H_ALL_CHAR_LOI_DAI.put(c.charDbId, c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public int doGetNhomLoiDai(int charDBID) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_char_thach_dau where id=" + charDBID;
            conn = this.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return rs.getByte("nhom");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void doAddCharThachDau(CharThiDau player, int nhom, boolean updatePointRank) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO tob_char_thach_dau set id=?,charname=?,point=?,lv=?,timeupdate=now(),timeReg=now(),nhom=?,pointrank=? ON DUPLICATE KEY UPDATE point=?,timeupdate=now()";
            if (updatePointRank) {
                sql = "INSERT INTO tob_char_thach_dau set id=?,charname=?,point=?,lv=?,timeupdate=now(),timeReg=now(),nhom=?,pointrank=? ON DUPLICATE KEY UPDATE point=?,timeupdate=now(),pointrank=?";
            }
            pre = conn.prepareStatement(sql);
            pre.setInt(1, player.charDbId);
            pre.setString(2, player.name);
            pre.setInt(3, player.point);
            pre.setInt(4, player.level);
            pre.setInt(5, player.nhom);
            pre.setInt(6, player.point);
            pre.setInt(7, player.point);
            if (updatePointRank) {
                pre.setInt(8, player.point);
            }
            pre.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

public void getDiemNapXaiLuong(Char p) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_nap_xai_luong where charname=?";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, p.charname);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                p.diemNapVip = rs.getInt("luongnap");
                p.diemxaiVip = rs.getInt("luongxai");

                if (UtilKPAH.getDayOfMonth() == 3) {
                    p.diemxaiVip = 0;
                    Database.instance.addCharXaiNapLuong(p, 1); // lưu reset
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

public void addCharXaiNapLuong(Char p, int type) {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
        conn = this.getConnection();

        if (type == 0) { // LƯU NẠP
            String sql =
                "INSERT INTO tob_nap_xai_luong (charname, luongnap, tnap) " +
                "VALUES (?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE luongnap=?, tnap=NOW()";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.charname);
            ps.setInt(2, p.diemNapVip);
            ps.setInt(3, p.diemNapVip);
        } else { // LƯU TIÊU
            String sql =
                "INSERT INTO tob_nap_xai_luong (charname, luongxai, txai) " +
                "VALUES (?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE luongxai=?, txai=NOW()";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.charname);
            ps.setInt(2, p.diemxaiVip);
            ps.setInt(3, p.diemxaiVip);
        }

        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        try { Database.connPool.free(conn); } catch (Exception ignored) {}
    }
}

    public void addCharWinThanh(String tenBang, Char bangchu, int type, long xu) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_chiemthanh(clan,bangchu,win,xu) values ('" + tenBang + "'," + bangchu.charname + ",1," + xu + ") ON DUPLICATE KEY UPDATE win = win + '1',xu = xu + " + xu + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharVT(Char p, int type, int xu, int luong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "UPDATE tob_vantieu set count = count + 1 WHERE namechar = '" + p.charname + "'";

            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharEvent_ND(Char p, int type, int xu, int luong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + p.charname + "'," + p.banphaohoa + ",now()) ON DUPLICATE KEY UPDATE point1=" + p.banphaohoa + ",timeupdate=now()";
            if (type == 1) {
                sql = "insert into tob_top_event(charname,xu,datexu) values ('" + p.charname + "'," + xu + ",now()) ON DUPLICATE KEY UPDATE xu=xu+" + xu + ",datexu=now()";
            } else if (type == 2) {
                sql = "insert into tob_top_event(charname,luong,dateluong) values ('" + p.charname + "'," + luong + ",now()) ON DUPLICATE KEY UPDATE luong=luong+" + luong + ",dateluong=now()";
            } else if (type == 3) {
                sql = "insert into tob_top_event(charname,xu,datexu,point1,timeupdate,luong,dateluong) values ('" + p.charname + "'," + xu + ",now()," + p.banphaohoa + "',now()," + luong + ",now()) ON DUPLICATE KEY UPDATE xu=xu+" + xu + ",datexu=now(),point1=" + p.banphaohoa + ",timeupdate=now(),luong=luong+" + luong + ",dateluong=now()";
            }
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharEvent_MiniCN(Char p, int type, int xu, int luong) {
        if (!Char.isSuKienMiniChucNu()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,luong,dateluong) values ('" + p.charname + "'," + luong + ",now()) ON DUPLICATE KEY UPDATE luong=luong+" + luong + ",dateluong=now()";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }
    public void addCharEventHLW(Char p, int type, int xu, int luong, int point) {
        if (!Char.isSuKienHaloween2016()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + p.charname + "'," + p.banphaohoa + ",now()) ON DUPLICATE KEY UPDATE point1=" + p.banphaohoa + ",timeupdate=now()";
            if (type == 2) {
                sql = "insert into tob_top_event(charname,luong,dateluong,point1) values ('" + p.charname + "'," + luong + ",now()) ON DUPLICATE KEY UPDATE luong=luong+" + luong + ",point1="+point+"dateluong=now()";
            }
            
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }
    
    public void addCharEvent(Char p, int type, int xu, int luong) {
        if (!Char.isSuKienHaloween2016()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + p.charname + "'," + p.banphaohoa + ",now()) ON DUPLICATE KEY UPDATE point1=" + p.banphaohoa + ",timeupdate=now()";
            if (type == 1) {
                sql = "insert into tob_top_event(charname,xu,datexu) values ('" + p.charname + "'," + xu + ",now()) ON DUPLICATE KEY UPDATE xu=xu+" + xu + ",datexu=now()";
            } else if (type == 2) {
                sql = "insert into tob_top_event(charname,luong,dateluong,point1) values ('" + p.charname + "'," + luong + ",now()) ON DUPLICATE KEY UPDATE luong=luong+" + luong + ",point1="+(luong/100)+"dateluong=now()";
            } else if (type == 3) {
                sql = "insert into tob_top_event(charname,xu,datexu,point1,timeupdate,luong,dateluong) values ('" + p.charname + "'," + xu + ",now()," + p.banphaohoa + "',now()," + luong + ",now()) ON DUPLICATE KEY UPDATE xu=xu+" + xu + ",datexu=now(),point1=" + p.banphaohoa + ",timeupdate=now(),luong=luong+" + luong + ",dateluong=now()";
            }
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<CharInfo> getTopEvent() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<CharInfo> result = new Vector<>();
        try {
            final String sql = "select * from tob_top_event order by point1 desc limit 10";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CharInfo c = new CharInfo();
                c.name = rs.getString("charname");
                c.honor = rs.getInt("point1");
                result.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return result;
    }

    public void updateHangDauGia(String charname, int hang) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update tob_dau_gia set hang=" + hang + " where charname='" + charname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void loadSMS(int provider) {
        Vector<String> decript = new Vector<>();
        Vector<String> syntax = new Vector<>();
        Vector<String> center = new Vector<>();
        Vector<String> agent = new Vector<>();
        Vector<String> pro = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_smsnap where provider=" + provider;
            if (provider != 1) {
                sql = "select * from tob_smsnap where provider<>1";
            }
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            while (rs.next()) {
                decript.add(rs.getString("text"));
                syntax.add(rs.getString("syntax"));
                center.add(rs.getString("port"));
                agent.add(rs.getString("agent"));
                pro.add(rs.getString("provider"));
            }
            if (decript.size() > 0) {
                if (provider == 1) {
                    Database.smsNapVTC.reset();
                    Database.smsNapVTC.setData(decript, syntax, center, agent, pro);
                } else {
                    Database.smsNapMe.reset();
                    Database.smsNapMe.setData(decript, syntax, center, agent, pro);
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        Database.allProvider.put("0", new CCUProvider("0"));
    }

    public void updateHangDauGia2(String charname, int hang) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "update tob_dau_gia2 set hang=" + hang + " where charname='" + charname + "'";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public int getMoneyDauGia(String charname, int hang) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_dau_gia where charname='" + charname + "' and hang=" + hang;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (hang == 1) {
                    return rs.getInt("luongbid");
                }
                if (hang == 2) {
                    return rs.getInt("luongcoc");
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return 0;
    }

    public int getMoneyDauGia2(String charname, int hang) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_dau_gia2 where charname='" + charname + "' and hang=" + hang;
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (hang == 1) {
                    return rs.getInt("luongbid");
                }
                if (hang == 2) {
                    return rs.getInt("luongcoc");
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return 0;
    }

    public boolean doAddCharDauGia(Char p, int luong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_dau_gia(charname,luongbid,luongcoc,timebid) values ('" + p.charname + "'," + luong + "," + luong / 2 + ",now())";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            return true;
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean doAddCharDauGia2(Char p, int luong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_dau_gia2(charname,luongbid,luongcoc,timebid) values ('" + p.charname + "'," + luong + "," + luong / 2 + ",now())";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            return true;
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean doAddCharMuaDoThoiTrang(Char p, int luong) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_mua_thoi_trang(charname,timebid) values ('" + p.charname + "',now())";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            return true;
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void addCharEventTrungthu(Char p) {
        if (!Char.isSuKienTrungThul2016()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + p.charname + "'," + p.banphaohoa + ",now()) ON DUPLICATE KEY UPDATE point1=" + p.banphaohoa + ",timeupdate=now()";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetTopEvent() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "delete from tob_top_event";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetChienTruongMoba() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "delete from tob_chien_truong";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Vector<String> getTopChienTruongMoba(String colum) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "select * from tob_chien_truong where " + colum + ">0 order by " + colum + " desc limit 3";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Vector<String> result = new Vector<>();
            while (rs.next()) {
                result.add(rs.getString("charname"));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        return new Vector<>();
    }

    public void loadCharChienTruong() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "select * from tob_reg_chientruong";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String charname = rs.getString("charname");
                CharChienTruong c = new CharChienTruong();
                c.name = charname;
                MapChienTruongMoba.all_char_chien_truong.put(c.name, c);
                MapChienTruongMoba.v_all_char_chien_truong.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetCharRegChienTruongMoba(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_reg_chientruong_bk SELECT * FROM tob_reg_chientruong";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            try {
                pstmt.close();
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            sql = "delete FROM tob_reg_chientruong";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetCharQuaKhuCT(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_qua_ct_bk SELECT * FROM tob_qua_ct";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            try {
                pstmt.close();
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            conn = this.getConnection();
            sql = "delete FROM tob_qua_ct";
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void resetCharQuaTopCT(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_qua_ct_top_bk SELECT * FROM tob_qua_ct_top";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            try {
                pstmt.close();
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            conn = this.getConnection();
            sql = "delete FROM tob_qua_ct_top";
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception ignored) {
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharRegChienTruongMoba(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_reg_chientruong(charname,timeReg) values ('" + charname + "',now() ) ON DUPLICATE KEY UPDATE timeReg=now()";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void doAddCharWinCT(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_qua_ct(charname,tset) values ('" + charname + "',now()) ON DUPLICATE KEY UPDATE soluong=soluong+1";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void doAddCharTopCT(String charname, int top) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_qua_ct_top(charname,soluong,tset) values ('" + charname + "'," + top + ",now())";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharChienTruongMoba(String charname, String colum, int point) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_chien_truong(charname," + colum + ") values ('" + charname + "'," + point + " ) ON DUPLICATE KEY UPDATE " + colum + "=" + colum + "+" + point;
            if (colum.equals("lientram")) {
                sql = "insert into tob_chien_truong(charname," + colum + ") values ('" + charname + "'," + point + " ) ON DUPLICATE KEY UPDATE " + colum + "=" + point;
            }
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharEventWC(Char p) {
//        if (!Char.isSuKienTet2017()) {
//            return;
//        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event_wc (charname,point1,timeupdate,crtime) values ('" + p.charname + "'," + 1 + ",now()," + System.currentTimeMillis() + " ) ON DUPLICATE KEY UPDATE point1=point1+" + 1 + ",timeupdate=now(),crtime=" + System.currentTimeMillis();
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharEventTet(Char p) {
//        if (!Char.isSuKienTet2017()) {
//            return;
//        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate,crtime) values ('" + p.charname + "'," + 1 + ",now()," + System.currentTimeMillis() + " ) ON DUPLICATE KEY UPDATE point1=point1+" + 1 + ",timeupdate=now(),crtime=" + System.currentTimeMillis();
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    /**
     * Danh dau nhan vat bi khoa.
     *
     * TRUOC DAY HAM NAY RO RI KET NOI: no goi getConnection() — tuc lay mot Connection RA
     * KHOI pool — roi khong bao gio goi free(conn) hay conn.close() tren bat ky duong thoat
     * nao. Moi lan ban la mot ket noi mat vinh vien, va ket noi do cung khong duoc dong nen
     * MySQL van giu no den tan wait_timeout.
     *
     * Dieu do bien mot loi nho thanh loi chet server: Char.getLuong()/getxu() goi vao day
     * moi lan chung thay tien am, va chung co hon 400 diem goi. Mot nhan vat am tien lam
     * can pool trong vai phut roi ha ca server. Phia Char.java nay da chan lai (bao mot lan
     * roi chan gia tri ve 0), nhung ban than ham nay van phai tra ket noi ve — con nhieu
     * duong khac goi banChar.
     *
     * Statement cung duoc hoi len ngoai try de dong duoc ca khi execute() nem loi.
     */
    public void banChar(Char p, String lydo) {
        Connection conn = null;
        Statement pree = null;
        try {
            this.saveOrtherLog("", p.charname, "BANCHAR", lydo);
            String sql = "UPDATE tob_char SET ban=1 where charname='" + p.charname + "'";
            conn = this.getConnection();
            pree = conn.createStatement();
            pree.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                if (pree != null) { pree.close(); }
            } catch (Exception ignored) {
            }
            try {
                // Chi tra khi that su lay duoc: ConnPool.free(null) van giam countConnection.
                if (conn != null) { Database.connPool.free(conn); }
            } catch (Exception ignored) {
            }
        }
    }

    public void addCharEvent83(Char p) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + p.charname + "',1,now()) ON DUPLICATE KEY UPDATE point1=point1+1,timeupdate=now()";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    /**
     * todo Lưu dữ liệu player vào database
     *
     * @param p
     * @param admindExit
     * @return
     */
    // Than method (242 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    // Khoa Char.KHOA_LUU: hai lan luu cua CUNG mot nhan vat khong duoc chong nhau,
    // neu khong ban chup cu co the execute sau ban chup moi va an mat do vua lam.
    // Xem chu thich day du o Char.KHOA_LUU.
    public boolean saveChar(Char p, boolean admindExit) {
        if (p == null) {
            return false;
        }
        synchronized (p.KHOA_LUU) {
            return DatabaseNhanVat.saveChar(this, p, admindExit);
        }
    }

    // Than method (206 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public boolean saveCharAuto(Char p) {
        if (p == null) {
            return false;
        }
        synchronized (p.KHOA_LUU) {
            return DatabaseNhanVat.saveCharAuto(this, p);
        }
    }

    public void saveCharSP(String allGem, int chardbid) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            final String listtemplate = "0,1";
            final String sql = "insert tob_sp_new(owner,listtemplate,soluong) values (?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, chardbid);
            pre.setString(2, listtemplate);
            pre.setString(3, allGem);
            pre.execute();
        } catch (Exception e) {
            try {
                final String sql = "update tob_sp_new set soluong=? where owner=?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, allGem);
                pre.setInt(2, chardbid);
                pre.execute();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharGemBackup(Connection conn, String allGem, int chardbid, String allGemLock) {
    }

    public void saveCharGem(String allGem, int chardbid, String allGemLock) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection();
            StringBuilder listtemplate = new StringBuilder("0");
            this.saveCharGemBackup(conn, allGem, chardbid, allGemLock);
            for (int i = 1; i < Map.gemTemplate.length; ++i) {
                listtemplate.append(",").append(i);
            }
            final String sql = "insert tob_gem_new(owner,listtemplate,soluong,slock) values (?,?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, chardbid);
            pre.setString(2, listtemplate.toString());
            pre.setString(3, allGem);
            pre.setString(4, allGemLock);
            pre.execute();
        } catch (Exception e) {
            try {
                final String sql = "update tob_gem_new set soluong=?,slock=? where owner=?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, allGem);
                pre.setString(2, allGemLock);
                pre.setInt(3, chardbid);
                pre.execute();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveGemItem(GemItem gem) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            if (gem.dbownerId == 0) {
                conn = this.getConnection();
                final String sql = "INSERT tob_gemitem(owner,place,gemtemplate) VALUES (?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerId);
                pre.setInt(2, gem.place);
                pre.setInt(3, gem.idGemTemplate);
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.dbid = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        gem.dbownerId = gem.ownerId;
    }

    private void saveGemItem(Connection conn, GemItem gem) throws SQLException {
        PreparedStatement pre = null;
        if (gem.dbownerId == 0) {
            final String sql = "INSERT tob_gemitem(owner,place,gemtemplate) VALUES (?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, gem.ownerId);
            pre.setInt(2, gem.place);
            pre.setInt(3, gem.idGemTemplate);
            pre.execute();
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        gem.dbownerId = gem.ownerId;
    }

    public void saveAnimal(Animal gem) {
        if (gem == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        Statement pre2 = null;
        try {
            conn = this.getConnection();
            if (gem.isHoaHinh == 1 && gem.timeHoaHinh >= 0L && System.currentTimeMillis() - gem.timeHoaHinh >= 0L) {
                gem.isHoaHinh = 0;
                gem.timeHoaHinh = 0L;
                // Het han thi don luon cap hoa hinh (them 31/08/2026) - khong de
                // capHoaHinh > 0 ma isHoaHinh da ve 0, tranh lech du lieu.
                gem.capHoaHinh = 0;
                gem.khangXuyenGiapCap = 0;
            }

            if (gem.dbownerId == 0) {
                final String sql = "INSERT tob_animal(owner,place,att,id_img,lv,name,texpire,isHoaHinh,timeHoaHinh) VALUES (?,?,?,?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerId);
                pre.setInt(2, gem.place);
                pre.setString(3, gem.getAttribute());
                pre.setInt(4, gem.idImg);
                pre.setInt(5, gem.level);
                pre.setString(6, gem.name);
                pre.setLong(7, gem.texpire);
                pre.setInt(8, gem.isHoaHinh);
                pre.setLong(9, gem.timeHoaHinh);
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.dbid = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } else {
                String sql = "UPDATE tob_animal SET owner=" + gem.ownerId + ",place=" + gem.place + ",att='" + gem.getAttribute() + "', id_img=" + gem.idImg + ",lv=" + gem.level + ",name='" + gem.name + "',texpire=" + gem.texpire + ",isHoaHinh=" + gem.isHoaHinh + ",timeHoaHinh=" + gem.timeHoaHinh + " WHERE id=" + gem.dbid + ";";
                pre2 = conn.createStatement();
                pre2.execute(sql);
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            gem.dbownerId = gem.ownerId;
        } catch (Exception e) {
            Logger.logError(e, "errorAnima.txt");
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            pre2.close();
        } catch (Exception ignored) {
        }
    }

    public void updateShopRuongMM() {
        Connection conn = null;
        Statement pre1 = null;
        try {
            String allUser = Map.getAllUserCountry();
            conn = this.getConnection();
            String sql = "UPDATE tob_event SET ruongmm='" + Map.getInfoSaveShopRuongMM() + "' where id=1";
            pre1 = conn.createStatement();
            pre1.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void updateInfoLixiEvent() {
        Connection conn = null;
        Statement pre1 = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE tob_event SET lixi='" + Map.MAX_LI_XI + "," + Map.hourGiveLixi + "," + Map.minuteGiveLixi + "' where id=1";
            pre1 = conn.createStatement();
            pre1.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void saveEvent(String info) {
        Connection conn = null;
        Statement pre1 = null;
        try {
            String allUser = Map.getAllUserCountry();
            conn = this.getConnection();
            String sql = "UPDATE tob_event SET part1='" + info + "',part2='" + allUser + "',part3='" + Map.getInfoWar() + "',part4='" + Map.getInfoT() + "'," + "shield='" + Map.getShield() + "'" + ",gov1='" + Map.getGov(0) + "',gov2='" + Map.getGov(1) + "',gov3='" + Map.getGov(2) + "',quayso='" + Map.getQuaySoPhuongHoang() + "',topluong='" + Char.getTopCharUseLuong() + "',topNapLuong='" + Char.getInfoTopLuongSave() + "',phaohoa='" + Map.totalDotPhao + "," + Map.x3ExpDotPhao + "',iditemsell=" + ItemSell.ID_ITEM_SELL_AUTO_INCREMENT + ",openphuonghoang='" + Map.getInfoSavePhuongHoang() + "',trung='" + Map.getInfoSavetrungpet() + "' where id=1";
            pre1 = conn.createStatement();
            pre1.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    // Than method (253 dong) da chuyen sang DatabaseGiftCode.java. Chu ky GIU NGUYEN.
    public void loadEvent() {
        DatabaseGiftCode.loadEvent(this);
    }

    public int countCharVip(int vip) {
        return 1;

//        Connection conn = null;
//        PreparedStatement pre1 = null;
//        ResultSet rs = null;
//        try {
//            conn = this.getConnection();
//            String sql = "select count(id) as a from tob_char_vip where vip=" + vip + " and reset=0";
//            pre1 = conn.prepareStatement(sql);
//            rs = pre1.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("a");
//            }
//        } catch (Exception ignored) {
//        } finally {
//            try {
//                Database.connPool.free(conn);
//            } catch (Exception ignored) {
//            }
//            try {
//                pre1.close();
//            } catch (Exception ignored) {
//            }
//        }
//        try {
//            Database.connPool.free(conn);
//        } catch (Exception ignored) {
//        }
//        try {
//            pre1.close();
//        } catch (Exception ignored) {
//        }
//        return 1000;
    }

    public void doAddCharVip(String name, int vip) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            final String sql = "insert into tob_char_vip(charname,timeRcv,vip) values(?,now(),?)";
            pre1 = conn.prepareStatement(sql);
            pre1.setString(1, name);
            pre1.setInt(2, vip);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doresetCharVip() {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            final String sql = "update tob_char_vip set reset=1";
            pre1 = conn.prepareStatement(sql);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doUpdateCharVip(String name, int vip) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            String sql = "update tob_char_vip set vip=" + vip + ", timeRcv=now() where charname='" + name + "'";
            pre1 = conn.prepareStatement(sql);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doresetCharVip(String name) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            String sql = "update tob_char_vip set reset=1 where charname='" + name + "'";
            pre1 = conn.prepareStatement(sql);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doGetAllCharVip() {
        Connection conn = null;
        PreparedStatement pre1 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_char_vip where reset=0";
            pre1 = conn.prepareStatement(sql);
            rs = pre1.executeQuery();
            while (rs.next()) {
                Map.ALL_CHAR_VIP.put(rs.getString("charname"), rs.getInt("vip"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doGetAllCharVip(int type) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "select * from tob_char_vip where reset=0 and vip=" + type;
            pre1 = conn.prepareStatement(sql);
            rs = pre1.executeQuery();
            while (rs.next()) {
                Map.ALL_CHAR_VIP.put(rs.getString("charname"), rs.getInt("vip"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doUpdateCharBuyRuong(CharInfo cinfo) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            String sql = "update tob_buy_ruong set timeBuy=now(), soluong=" + cinfo.luong + " where charname='" + cinfo.name + "' and dateLog='" + Char.getDayOpen(0L) + "'";
            pre1 = conn.prepareStatement(sql);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void doResetCharBuyRuong() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String sql = "delete from tob_buy_ruong";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void doAddCharBuyRuong(CharInfo cinfo) {
        Connection conn = null;
        PreparedStatement pre1 = null;
        try {
            conn = this.getConnection();
            final String sql = "insert into tob_buy_ruong(charname,datelog,timeBuy,soluong) values(?,?,now(),?)";
            pre1 = conn.prepareStatement(sql);
            pre1.setString(1, cinfo.name);
            pre1.setString(2, Char.getDayOpen(0L));
            pre1.setInt(3, 1);
            pre1.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void loadAllCharBuyRuong() {
        Connection conn = null;
        Statement pre1 = null;
        try {
            conn = this.getConnection();
            String sql = "select * from tob_buy_ruong where dateLog='" + Char.getDayOpen(0L) + "'";
            pre1 = conn.createStatement();
            ResultSet rs = pre1.executeQuery(sql);
            while (rs.next()) {
                CharInfo cinfo = new CharInfo();
                cinfo.name = rs.getString("charname");
                cinfo.luong = rs.getInt("soluong");
                Map.ALL_CHAR_BUY_RUONG.put(cinfo.name, cinfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre1.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre1.close();
        } catch (Exception ignored) {
        }
    }

    public void savePet(Pet gem) {
        if (gem == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        Statement pre2 = null;
        try {
            conn = this.getConnection();
            if (gem.idDB == 0) {
                final String sql = "INSERT tob_pet(owner,place,info,idImg,name,itPet) VALUES (?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerid);
                pre.setInt(2, gem.place);
                pre.setString(3, gem.getDBInfo());
                pre.setInt(4, gem.idImg);
                pre.setString(5, gem.getNameSave());
                pre.setString(6, gem.getInfoItem());
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.idDB = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } else {
                String sql = "UPDATE tob_pet SET owner=" + gem.ownerid + ",place=" + gem.place + ",info='" + gem.getDBInfo() + "', idImg=" + gem.idImg + ",name='" + gem.getNameSave() + "',itPet='" + gem.getInfoItem() + "' WHERE id=" + gem.idDB + ";";
                pre2 = conn.createStatement();
                pre2.execute(sql);
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError(e, "errorAnima.txt");
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            pre2.close();
        } catch (Exception ignored) {
        }
    }

    void saveAnimal(Connection conn, Animal gem) {
        PreparedStatement pre = null;
        Statement pre2 = null;
        try {
            if (gem.isHoaHinh == 1 && gem.timeHoaHinh >= 0L && System.currentTimeMillis() - gem.timeHoaHinh >= 0L) {
                gem.isHoaHinh = 0;
                gem.timeHoaHinh = 0L;
                // Het han thi don luon cap hoa hinh (them 31/08/2026) - khong de
                // capHoaHinh > 0 ma isHoaHinh da ve 0, tranh lech du lieu.
                gem.capHoaHinh = 0;
                gem.khangXuyenGiapCap = 0;
            }

            if (gem.dbownerId == 0) {
                final String sql = "INSERT tob_animal(owner,place,att,id_img,lv,name,isHoaHinh,timeHoaHinh) VALUES (?,?,?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerId);
                pre.setInt(2, gem.place);
                pre.setString(3, gem.getAttribute());
                pre.setInt(4, gem.idImg);
                pre.setInt(5, gem.level);
                pre.setString(6, gem.name);
                pre.setInt(7,gem.isHoaHinh);
                pre.setLong(8,gem.timeHoaHinh);
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.dbid = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } else {

                String sql = "UPDATE tob_animal SET owner=" + gem.ownerId + 
                ",place=" + gem.place + 
                ",att='" + gem.getAttribute() + "'" +
                ",id_img=" + gem.idImg + 
                ",lv=" + gem.level + 
                ",name='" + gem.name + "'" +
                ",isHoaHinh=" + gem.isHoaHinh +
                ",timeHoaHinh=" + gem.timeHoaHinh + 
                " WHERE id=" + gem.dbid + ";";

                pre2 = conn.createStatement();
                pre2.execute(sql);
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
            gem.dbownerId = gem.ownerId;
        } catch (Exception e) {
            Logger.logError(e, "errorAnima.txt");
        }
    }

    void saveAnimal1(Connection conn, Animal gem, String idPrivateSv) {
        PreparedStatement pre = null;
        Statement pre2 = null;
        try {
            try {
                final String sql = "INSERT tob_animal(owner,place,att,id_img,lv,name,idPrivateSv) VALUES (?,?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerId);
                pre.setInt(2, gem.place);
                pre.setString(3, gem.getAttribute());
                pre.setInt(4, gem.idImg);
                pre.setInt(5, gem.level);
                pre.setString(6, gem.name);
                pre.setString(7, idPrivateSv);
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.dbid = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                String sql = "UPDATE tob_animal SET owner=" + gem.ownerId + ",place=" + gem.place + ",att='" + gem.getAttribute() + "', id_img=" + gem.idImg + ",lv=" + gem.level + ",name='" + gem.name + "' WHERE idPrivateSv='" + idPrivateSv + "';";
                pre2 = conn.createStatement();
                pre2.execute(sql);
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
            gem.dbownerId = gem.ownerId;
        } catch (Exception e) {
            Logger.logError(e, "errorAnima.txt");
        }
    }

    public void saveFarm(SeedItem s, Connection conn) {
        String sql = "";
        PreparedStatement pre = null;
        try {
            if (s.idDB != 0) {
                sql = "UPDATE tob_farm SET farmId=?,treeId=?,lv=?,timeBegin=?,buy=?,lvPlot=?,rank=?  WHERE id=?";
                pre = conn.prepareStatement(sql);
                pre.setByte(1, s.idFarm);
                pre.setByte(2, s.idTemplate);
                pre.setByte(3, s.lvTree);
                pre.setLong(4, s.timeLive);
                pre.setInt(5, s.buy);
                pre.setByte(6, s.lvPlot);
                pre.setByte(7, s.rankTree);
                pre.setInt(8, s.idDB);
                pre.executeUpdate();
            } else {
                sql = "insert tob_farm(owner,farmId,treeId,timeBegin,lv,buy) values(" + s.ownerID + "," + s.idFarm + "," + s.idTemplate + "," + s.timeLive + "," + s.lvTree + "," + s.buy + ")";
                pre = conn.prepareStatement(sql);
                pre.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
    }

    public void getFarm(Char p, Connection conn) {
        String sql = "";
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            sql = "select * from tob_farm WHERE owner=" + p.charDBID;
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            boolean have = false;
            while (rs.next()) {
                have = true;
                SeedItem s = new SeedItem(rs.getInt("farmID"), rs.getInt("treeId"), rs.getByte("buy"));
                s.ownerID = rs.getInt("owner");
                s.idDB = rs.getInt("id");
                s.timeLive = rs.getLong("timeBegin");
                s.lvPlot = rs.getByte("lvPlot");
                s.rankTree = rs.getByte("rank");
                p.seedsItem.add(s);
            }
            if (!have) {
                for (int i = 0; i < Map.MAX_FARM; ++i) {
                    SeedItem s2 = new SeedItem(i, -1, (byte) 0);
                    s2.ownerID = p.charDBID;
                    p.seedsItem.add(s2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
    }

    private void saveSpecialItem(Connection conn, ShopTemplate item) throws SQLException {
        PreparedStatement pre = null;
        if (item.dbownerId == 0) {
            final String sql = "INSERT tob_gemitem(owner,place,gemtemplate,typeItem) VALUES (?,?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, item.ownerId);
            pre.setInt(2, item.place);
            pre.setInt(3, item.id);
            pre.setInt(4, 1);
            pre.execute();
        } else {
            final String sql = "UPDATE tob_gemitem SET owner=?,place=?,gemtemplate=? WHERE id=?";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, item.ownerId);
            pre.setInt(2, item.place);
            pre.setInt(3, item.id);
            pre.setInt(4, item.dbid);
            pre.execute();
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        item.dbownerId = item.ownerId;
    }

    public void saveFriendList(Char p) {
        PreparedStatement pre = null;
        Connection conn = null;
        try {
            StringBuilder frInfo = new StringBuilder();
            StringBuilder frInfoName = new StringBuilder();
            if (p.friendListID.size() > 0) {
                frInfo.append(p.friendListID.elementAt(0));
                frInfoName.append(p.friendListName.elementAt(0));
                for (int i = 1; i < p.friendListID.size(); ++i) {
                    frInfo.append(",").append(p.friendListID.get(i));
                    frInfoName.append(",").append(p.friendListName.get(i));
                }
            }
            String sql = "update tob_friendlist set friendId=?,name=?,ischange=1 where charId=?";
            conn = this.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, frInfo.toString());
            pre.setString(2, frInfoName.toString());
            pre.setInt(3, p.charDBID);
            if (!pre.execute()) {
                sql = "insert tob_friendlist(charId,friendId,name) values(?,?,?)";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, p.charDBID);
                pre.setString(2, frInfo.toString());
                pre.setString(3, frInfoName.toString());
                pre.execute();
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveItemBackup(Connection conn, Item item, boolean insert) {
    }

    public void getListMemJoinWedding() {
        String[] typeWedding = {"don gian", "Nao nhiet", "hoanh trang"};
        Connection conn = null;
        ResultSet rs = null;
        Statement stm = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_wedding";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                Wedding wd = new Wedding();
                wd.idP1 = rs.getInt("idhusband");
                wd.idP2 = rs.getInt("idwife");
                String sql2 = "select * from tob_char where id=" + wd.idP1 + " or id=" + wd.idP2;
                ResultSet rs2 = null;
                Statement stm2 = null;
                stm2 = conn.createStatement();
                rs2 = stm2.executeQuery(sql2);
                StringBuilder info = new StringBuilder();
                while (rs2.next()) {
                    info.append(rs2.getString("charname")).append("_");
                }
                if (!info.toString().equals("")) {
                    info.append(typeWedding[rs.getByte("typeparty")]);
                    Logger.logString(info.toString(), "charWedding.txt");
                }
                rs2.close();
                stm2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public Hashtable<Integer, Wedding> loadWedding() {
        Hashtable<Integer, Wedding> wedding = new Hashtable<>();
        Connection conn = null;
        ResultSet rs = null;
        Statement stm = null;
        try {
            conn = this.getConnection();
            final String sql = "select * from tob_wedding where married=0";
            stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                Wedding wd = new Wedding();
                wd.idParty = rs.getInt("idwedding");
                wd.idP1 = rs.getInt("idhusband");
                wd.idP2 = rs.getInt("idwife");
                wd.action = rs.getByte("married");
                wd.time = rs.getByte("timeParty");
                wd.dayParty = rs.getString("dayparty");
                wd.typeParty = rs.getByte("typeparty");
                String day = Char.getDayOpen(0L);
                if (!day.equals(wd.dayParty)) {
                    this.updateStatusWedding(wd.idParty, 1);
                } else {
                    wedding.put(wd.idParty, wd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
       
        return wedding;
    }

    public void updateStatusWedding(int idWedding, int status) {
        PreparedStatement pre = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            String sql = "update tob_wedding set married=" + status + " where idwedding=" + idWedding;
            pre = conn.prepareStatement(sql);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

   

    public void saveWedding(Char p1, Char p2, byte time, String day, byte type) {
        PreparedStatement pre = null;
        Connection conn = null;
        try {
            conn = this.getConnection();
            final String sql = "INSERT tob_wedding(idhusband,idwife,timeParty,dayparty,typeparty,idwedding) VALUES(?,?,?,?,?,?)";
            pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pre.setInt(1, (p1.gender == 1) ? p1.charDBID : p2.charDBID);
            pre.setInt(2, (p2.gender == 2) ? p2.charDBID : p1.charDBID);
            pre.setByte(3, time);
            pre.setString(4, day);
            pre.setByte(5, type);
            pre.setInt(6, p1.idWedding);
            pre.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveItem(Item item) {
    }

    private void saveItem(Connection conn, Item item) {
        String att = item.getAttribute();
        String basic = item.getDBInfo();
        PreparedStatement pre = null;
        try {
            if (item.dbowner == 0) {
                final String sql = "INSERT tob_item(owner,allAtt,dbbasic,place,item_template,class,plus,timeLoan,atb0,atb1,atb2,atb3) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, item.owner);
                pre.setString(2, att);
                pre.setString(3, basic);
                pre.setInt(4, item.place);
                pre.setInt(5, item.getTemplate().id);
                pre.setInt(6, item.clazz);
                pre.setInt(7, item.plus);
                pre.setLong(8, item.timeLoan);
                pre.setInt(9, item.identify);
                pre.setInt(10, item.switchAttNguhanh);
                pre.setInt(11, item.resetItem);
                pre.setInt(12, item.slotWearing);
                pre.execute();
                item.dbowner = item.owner;
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                item.dbid = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
                this.saveItemBackup(conn, item, true);
            } else {
                final String sql = "UPDATE tob_item SET owner=?,allAtt=?,dbbasic=?,place=?,item_template=?,class=?,plus=?,timeLoan=?,atb0=?,atb1=?,atb2=?,atb3=? WHERE id=?";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, item.owner);
                pre.setString(2, att);
                pre.setString(3, basic);
                pre.setInt(4, item.place);
                pre.setInt(5, item.getTemplate().id);
                pre.setInt(6, item.clazz);
                pre.setInt(7, item.plus);
                pre.setLong(8, item.timeLoan);
                pre.setInt(9, item.identify);
                pre.setInt(10, item.switchAttNguhanh);
                pre.setInt(11, item.resetItem);
                pre.setInt(12, item.slotWearing);
                pre.setInt(13, item.dbid);
                pre.execute();
                this.saveItemBackup(conn, item, false);
            }
            try {
                pre.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            item.dbowner = item.owner;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetChar(int headStyle, int charDBID, int classID, int gender, String charName, int lastLv, Connection conn) {
        PreparedStatement pre = null;
        try {
            CharTemplate ct;
            if (gender != -1) {
                ct = Database.charTemplates[classID][gender - 1];
            } else {
                ct = Database.charTemplatesOld[classID];
            }
            StringBuilder potion = new StringBuilder("0," + ct.nHP1 + ",0,0," + ct.nMP1 + ",0,0");
            StringBuilder skill = new StringBuilder("0,0,0,0,0,0,0");
            ct.strength = Database.basicPoint[classID][0];
            ct.agitity = Database.basicPoint[classID][1];
            ct.spirit = Database.basicPoint[classID][2];
            ct.dextrity = Database.basicPoint[classID][3];
            ct.luck = Database.basicPoint[classID][4];
            String basic = ct.strength + "," + ct.agitity + "," + ct.spirit + "," + ct.dextrity + "," + ct.luck + ",0,0";
            for (int i = 7; i < 25; ++i) {
                potion.append(",0");
                if (i < 15) {
                    skill.append(",0");
                }
            }
            int maxhp = 0;
            int maxmp = 0;
            switch (classID) {
                case 0: {
                    maxhp = ct.dextrity * 80;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 1:
                case 4:
                case 3: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 2: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 50;
                    break;
                }
            }
            String pInfo = headStyle + "," + gender + "," + charName + "," + classID + ",0,1," + maxhp + "," + maxmp + ",0,0,0,0,0,0,0,0,0";
            final String sql = "UPDATE tob_char SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=0,luong=0,lastLv=1,del=1,tDelChar=0, gold=0,xp=0 where id=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, pInfo);
            pre.setString(2, basic);
            pre.setString(3, skill.toString());
            pre.setString(4, potion.toString());
            pre.setInt(5, charDBID);
            pre.execute();
            int aoTemplate = (ct.gender == 1) ? 1 : 2;
            int quanTemplate = (ct.gender == 1) ? 27 : 28;
            int[] defaultVK = {79, 86, 93, 100, 107, 79, 86, 93, 100, 107};
            int vkTemplate = defaultVK[classID];
            Item ao = new Item(Map.itemTemplates.get(classID).get(aoTemplate), false, classID, classID, aoTemplate);
            Item quan = new Item(Map.itemTemplates.get(classID).get(quanTemplate), false, classID, classID, quanTemplate);
            Item vk = new Item(Map.itemTemplates.get(classID).get(vkTemplate), false, classID, classID, vkTemplate);
            ao.place = 1;
            quan.place = 1;
            vk.place = 1;
            this.saveItem(conn, ao);
            this.saveItem(conn, quan);
            this.saveItem(conn, vk);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
    }

    public void delChar(int charid) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "update tob_char set ban=1 where id=" + charid;
            stmt.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addBackupChar(Connection conn, String sql) {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                st.close();
            } catch (Exception ignored) {
            }
        }
        try {
            st.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Cho phep tao nhan vat mang dung ten GM trong sv.adminAccounts.
     *
     * Doc tu server.ini: sv.choPhepTaoTenGM=1. Mac dinh TAT.
     * Dung khi chinh chu can tao lai nhan vat GM (vi du sau khi lam moi database):
     * bat len -> tao nhan vat ten "admin" -> tat lai. Doi bang bang setting trong
     * game (go "setting" roi lenh 8 de nap lai) nen khong can restart server.
     *
     * Chi mo dung cac ten co trong sv.adminAccounts, khong mo cho moi ten chua
     * chu "admin" — de nguoi choi khong the dat ten kieu "adminvip" luc dang bat.
     */
    public static boolean CHO_PHEP_TAO_TEN_GM = false;

    /**
     * Ten khong duoc phep dat khi tao nhan vat:
     *   - moi ten CO CHUA chu "admin" (admin, admin2, xxadminxx, ADMIN...): tranh
     *     nguoi choi mao danh admin va tranh trung ten trong sv.adminAccounts.
     *   - moi ten liet ke trong sv.adminAccounts (ke ca ten khong chua "admin").
     * Cac nhan vat DA ton tai khong bi anh huong, day chi chan luc tao moi.
     */
    public static boolean laTenDanhRieng(final String charname) {
        if (charname == null) {
            return true;
        }
        final String ten = charname.trim().toLowerCase();
        if (ten.isEmpty()) {
            return true;
        }
        // Cua ngo cho chinh chu tao lai nhan vat GM (xem CHO_PHEP_TAO_TEN_GM)
        try {
            if (CHO_PHEP_TAO_TEN_GM && server.TeamServer.adminAccounts.contains(ten)) {
                System.out.println("[TAO NHAN VAT] Cho phep ten GM '" + ten
                        + "' vi sv.choPhepTaoTenGM=1. NHO TAT LAI sau khi tao xong.");
                return false;
            }
        } catch (final Exception ignored) {
        }
        if (ten.contains("admin")) {
            return true;
        }
        try {
            return server.TeamServer.adminAccounts.contains(ten);
        } catch (final Exception e) {
            return false;
        }
    }

    // Than method (201 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public int addChar(Session s, String charname, int headStyle, int classID, int gender, int nation) {
        return DatabaseNhanVat.addChar(this, s,charname,headStyle,classID,gender,nation);
    }

    // ============ HAN MUC LUONG NHAT DUOC KHI TRAIN QUAI (theo ngay, moi nhan vat) ============

    /**
     * Them cot tob_char.isCT (cong tac "Tat cai trang") neu chua co. Goi 1 lan luc khoi
     * dong server, TRUOC khi nhan nguoi choi vao.
     *
     * VI SAO PHAI TU THEM CHU KHONG BAT NGUOI QUAN TRI CHAY SQL: cau UPDATE trong
     * DatabaseNhanVat.saveChar co ghi cot nay. Cot khong ton tai thi cau do nem loi, va
     * nhanh bat loi ngay duoi no KHOA NHAN VAT (ban=1) vi no coi moi that bai luu la loi
     * hanh trang. Tuc quen mot cau ALTER TABLE se KHOA SACH nguoi choi ngay lan dang xuat
     * dau tien. Khong duoc de mot buoc thu cong dung giua duong do.
     *
     * MySQL 5.x khong co "ADD COLUMN IF NOT EXISTS" nen phai hoi information_schema truoc.
     *
     * Mac dinh 0 = DANG BAT, dung y voi hanh vi tu truoc den gio, nen chay migration
     * khong lam ai bi tat cai trang.
     */
    public void themCotCongTacHienThi() {
        themCotNeuThieu("isCT", "cong tac Tat cai trang");
        themCotNeuThieu("isCanh", "cong tac Tat canh");
    }

    /** Them mot cot cong tac int NOT NULL DEFAULT 0 vao tob_char neu chua co. */
    private void themCotNeuThieu(final String tenCot, final String moTa) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS"
                    + " WHERE TABLE_SCHEMA = DATABASE()"
                    + " AND TABLE_NAME = 'tob_char' AND COLUMN_NAME = '" + tenCot + "'");
            final boolean daCo = rs.next() && rs.getInt(1) > 0;
            rs.close();
            rs = null;
            if (!daCo) {
                stmt.execute("ALTER TABLE tob_char ADD COLUMN " + tenCot
                        + " int(11) NOT NULL DEFAULT 0");
                System.out.println("[DB] Da them cot tob_char." + tenCot + " (" + moTa + ").");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    /** Tao bang luu han muc neu chua co. Goi 1 lan luc khoi dong server. */
    public void createTableTrainLuong() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tob_train_luong ("
                    + "charDBID int(10) unsigned NOT NULL,"
                    + "ngay varchar(10) NOT NULL DEFAULT '',"
                    + "luongmo int(10) unsigned NOT NULL DEFAULT 0,"
                    + "luongkhoa int(10) unsigned NOT NULL DEFAULT 0,"
                    + "PRIMARY KEY (charDBID)) ENGINE=MyISAM DEFAULT CHARSET=utf8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    /** Nap han muc da nhan cua nhan vat. Neu ngay trong DB khac hom nay thi Char tu reset ve 0. */
    public void loadTrainLuong(Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            pstmt = conn.prepareStatement("select ngay,luongmo,luongkhoa from tob_train_luong where charDBID=?");
            pstmt.setInt(1, p.charDBID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                p.ngayTrainLuong = rs.getString("ngay");
                p.luongMoTrainNgay = rs.getInt("luongmo");
                p.luongKhoaTrainNgay = rs.getInt("luongkhoa");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    /** Luu han muc da nhan. Duoc goi kem moi lan luu nhan vat (autosave / thoat game). */
    public void saveTrainLuong(Char p) {
        if (p == null || p.charDBID <= 0 || p.ngayTrainLuong == null || p.ngayTrainLuong.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = this.getConnection();
            pstmt = conn.prepareStatement("INSERT INTO tob_train_luong(charDBID,ngay,luongmo,luongkhoa) VALUES(?,?,?,?)"
                    + " ON DUPLICATE KEY UPDATE ngay=?,luongmo=?,luongkhoa=?");
            pstmt.setInt(1, p.charDBID);
            pstmt.setString(2, p.ngayTrainLuong);
            pstmt.setInt(3, p.luongMoTrainNgay);
            pstmt.setInt(4, p.luongKhoaTrainNgay);
            pstmt.setString(5, p.ngayTrainLuong);
            pstmt.setInt(6, p.luongMoTrainNgay);
            pstmt.setInt(7, p.luongKhoaTrainNgay);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
    }

    public void delQuest(int charDBID) {
        Connection conn = null;
        Statement stmt = null;
        CharQuest cq = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "delete FROM top_char_quest WHERE id_char=" + charDBID;
            stmt.execute(sql);
        } catch (Exception ignored) {
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void deleteItem2(Connection conn, int dbid) {
    }

    public void deleteItem(int dbid) {
    }

    public void deletePet(int dbid) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            String sql = "DELETE FROM tob_pet where id=" + dbid;
            stmt.execute(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void deleteGemItem(int dbid) {
    }

  

    public static Vector<String> readText(String st) {
        String test = null;
        InputStream is = null;
        try {
            is = new FileInputStream(st);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        Vector<String> listWord = new Vector<>();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        test = "";
        try {
            int ch;
            while ((ch = isr.read()) > -1) {
                if (ch == 13) {
                    listWord.addElement(test);
                    System.out.println(test);
                    test = "";
                } else {
                    if (ch == 10) {
                        continue;
                    }
                    test = test + (char) ch;
                }
            }
            if (!listWord.contains(test)) {
                listWord.add(test);
            }
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        System.err.println(test);
        if (!test.equals("") && !test.equals(" ") && !listWord.contains(test)) {
            listWord.add(test);
            System.out.println(test);
        }
        for (int i = 0; i < listWord.size(); ++i) {
            CharInfo c = new CharInfo();
            try {
                c.name = listWord.get(i);
                c.money = i;
                Database.all_char_top.add(c);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        System.out.println("TONG SO CHAR TOP " + listWord.size() + " > " + Database.all_char_top.size());
        return listWord;
    }

    public static Vector<String> cloneVectorString(Vector<String> v) {
        int size = v.size();
        int i = 0;
        Vector<String> data = new Vector<>();
        while (i < size) {
            try {
                data.add(v.remove(0));
            } catch (Exception ignored) {
            }
            ++i;
        }
        return data;
    }

 

   

        public static void checkDetailGems(String gemIds, String quantity, String compareType, 
                                    String charNameSearch, String fileName, String gemType) {
        StringBuilder result = new StringBuilder();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = new Database().getConnection();
            String sql = "SELECT t1.owner, t1.listtemplate, t1.soluong, t1.slock, t2.charname " +
                        "FROM tob_gem_new t1 " +
                        "LEFT JOIN tob_char t2 ON t1.owner = t2.id";
            
            if (charNameSearch != null && !charNameSearch.isEmpty()) {
                sql += " WHERE t2.charname LIKE ?";
            }
            
            stmt = conn.prepareStatement(sql);
            
            if (charNameSearch != null && !charNameSearch.isEmpty()) {
                stmt.setString(1, "%" + charNameSearch + "%");
            }
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                int owner = rs.getInt("owner");
                String charName = rs.getString("charname");
                String[] templates = rs.getString("listtemplate").split(",");
                String[] amounts = rs.getString("soluong").split(",");
                String[] slocks = rs.getString("slock").split(",");

                boolean foundMatch = false;
                for (int i = 0; i < templates.length; i++) {
                    int gemId = Integer.parseInt(templates[i]);
                    int amount;
                    
                    // Xác định số lượng dựa vào loại gem cần kiểm tra
                    if (gemType.equals("soluong")) {
                        amount = Integer.parseInt(amounts[i]);
                    } else if (gemType.equals("slock")) {
                        amount = Integer.parseInt(slocks[i]);
                    } else { // both
                        amount = Integer.parseInt(amounts[i]) + Integer.parseInt(slocks[i]);
                    }

                    if (gemIds.isEmpty() || gemIds.contains(String.valueOf(gemId))) {
                        boolean matchCondition = false;
                        int targetQuantity = Integer.parseInt(quantity);
                        
                        switch (compareType) {
                            case "greater":
                                matchCondition = amount >= targetQuantity;
                                break;
                            case "less":
                                matchCondition = amount <= targetQuantity;
                                break;
                            case "equal":
                                matchCondition = amount == targetQuantity;
                                break;
                        }

                        if (matchCondition) {
                            if (!foundMatch) {
                                result.append("\n=== Character: ").append(charName)
                                    .append(" (ID: ").append(owner).append(") ===\n");
                                foundMatch = true;
                            }
                            result.append(Map.gemTemplate[gemId].name)
                                .append(" (ID: ").append(gemId).append("): ")
                                .append(amount);
                            
                            // Thêm chi tiết về gem khóa/mở
                            if (gemType.equals("both")) {
                                result.append(" (Mở: ").append(amounts[i])
                                    .append(", Khóa: ").append(slocks[i]).append(")");
                            }
                            result.append("\n");
                        }
                    }
                }
            }

            
            // Lưu kết quả ra file
            if (fileName != null && !fileName.isEmpty()) {
                Logger.logString(result.toString(), fileName + ".txt");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

 

   
    public void setCharReceiveOffline() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Vector<String> allchar = readText("offline.txt");
            conn = this.getConnection();
            for (String s : allchar) {
                try {
                    String sql = "insert into tob_qua(charname,qua) values('" + s + "',0)";
                    stmt = conn.createStatement();
                    stmt.execute(sql);
                    try {
                        stmt.close();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.close(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.close(conn);
        } catch (Exception ignored) {
        }
    }

    public void updateNameMonsterIndo() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            for (int i = 0; i < Database.nameItem_indo.length; ++i) {
                conn = this.getConnection();
                String sql = "update data_attribute set namein='" + Database.name_att_in[i] + "' where id=" + i;
                stmt = conn.createStatement();
                stmt.execute(sql);
                try {
                    stmt.close();
                } catch (Exception ignored) {
                }
                try {
                    Database.connPool.close(conn);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    // Than method (190 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public void saveCharLienDau(Char p, int[] infoCharLiendau) {
        DatabaseNhanVat.saveCharLienDau(this, p,infoCharLiendau);
    }

    public void doUpdateReceiveGiftCT(String charname) {
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            String name = charname + "_" + Database.nameServer[TeamServer.server];
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "update tob_qua_ct set nhan=1, tlog=now() WHERE charname='" + name + "'";
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
    }

    public void doUpdateReceiveGiftTopCT(String charname, int type) {
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            String name = charname + "_" + Database.nameServer[TeamServer.server];
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "update tob_qua_ct_top set nhan=1, tlog=now() WHERE charname='" + name + "' and soluong=" + type;
            pre.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
    }

    public int getCharWinLiendau(String name) {
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            String charname = name + "_" + Database.nameServer[TeamServer.server];
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "SELECT * FROM tob_qua_ct WHERE charname='" + charname + "' and nhan=0";
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("soluong");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return 0;
    }

    public int getCharTopLiendau(String name, int type) {
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            String charname = name + "_" + Database.nameServer[TeamServer.server];
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "SELECT * FROM tob_qua_ct_top WHERE charname='" + charname + "' and nhan=0 and soluong=" + type;
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("soluong");
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return -1;
    }

    public boolean isCharRegisterLiendau(String name) {
        Statement pre = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            String charname = name + "_" + Database.nameServer[TeamServer.server];
            conn = this.getConnection1();
            pre = conn.createStatement();
            String sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            rs = pre.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        return false;
    }

    // Than method (195 dong) da chuyen sang DatabaseNhanVat.java. Chu ky GIU NGUYEN.
    public InfoCharLienDau addChar1(Session s, String charname, int headStyle, int classID, int gender, int nation) {
        return DatabaseNhanVat.addChar1(this, s,charname,headStyle,classID,gender,nation);
    }

    public void saveCharQuest1(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection1();
            final String sql = "update tob_char_quest set itemget=?,monskilled=?,isfinish=?, itemquest=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setString(3, info[2]);
            stmt.setString(4, info[3]);
            stmt.setInt(5, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE QUEST ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharQuest1(int idDBChar, String[] info) {
        if (info == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection1();
            final String sql = "insert into tob_char_quest(id_char,itemget,monskilled,isfinish) values(?,?,?,?) ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idDBChar);
            stmt.setString(2, info[0]);
            stmt.setString(3, info[1]);
            stmt.setString(4, info[2]);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE QUEST ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void addCharTubinh1(int idDBChar) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection1();
            final String sql = "insert into tob_char_tubinh(id_char,listid,listmanh) values(?,?,?) ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idDBChar);
            stmt.setString(2, "");
            stmt.setString(3, "");
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharFruit1(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection1();
            final String sql = "update tob_char_fruit set listidfruit=?,stFruit=?,tFruit=?,gift=?,theo=?,tkho=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setString(3, info[2]);
            stmt.setString(4, info[3]);
            stmt.setString(5, info[4]);
            stmt.setString(6, info[5]);
            stmt.setInt(7, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE FRUIT ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharGem1(String allGem, int chardbid, String allGemLock) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection1();
            StringBuilder listtemplate = new StringBuilder("0");
            this.saveCharGemBackup(conn, allGem, chardbid, allGemLock);
            for (int i = 1; i < Map.gemTemplate.length; ++i) {
                listtemplate.append(",").append(i);
            }
            final String sql = "insert tob_gem_new(owner,listtemplate,soluong,slock) values (?,?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, chardbid);
            pre.setString(2, listtemplate.toString());
            pre.setString(3, allGem);
            pre.setString(4, allGemLock);
            pre.execute();
        } catch (Exception e) {
            try {
                final String sql = "update tob_gem_new set soluong=?,slock=? where owner=?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, allGem);
                pre.setString(2, allGemLock);
                pre.setInt(3, chardbid);
                pre.execute();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharTubinh1(int idDBChar, String[] info) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection1();
            final String sql = "update tob_char_tubinh set listid=?,listmanh=? where id_char=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, info[0]);
            stmt.setString(2, info[1]);
            stmt.setInt(3, idDBChar);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("LOI SAVE FRUIT ROI");
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void saveCharSP1(String allGem, int chardbid) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = this.getConnection1();
            final String listtemplate = "0,1";
            final String sql = "insert tob_sp_new(owner,listtemplate,soluong) values (?,?,?)";
            pre = conn.prepareStatement(sql);
            pre.setInt(1, chardbid);
            pre.setString(2, listtemplate);
            pre.setString(3, allGem);
            pre.execute();
        } catch (Exception e) {
            try {
                final String sql = "update tob_sp_new set soluong=? where owner=?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, allGem);
                pre.setInt(2, chardbid);
                pre.execute();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
    }

    public void savePet1(Pet gem, String iddbPrivate) {
        if (gem == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        Statement pre2 = null;
        try {
            conn = this.getConnection1();
            try {
                final String sql = "INSERT tob_pet(owner,place,info,idImg,name,itPet,idPrivateSv) VALUES (?,?,?,?,?,?,?)";
                pre = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pre.setInt(1, gem.ownerid);
                pre.setInt(2, gem.place);
                pre.setString(3, gem.getDBInfo());
                pre.setInt(4, gem.idImg);
                pre.setString(5, gem.getNameSave());
                pre.setString(6, gem.getInfoItem());
                pre.setString(7, iddbPrivate);
                pre.execute();
                ResultSet rs = pre.getGeneratedKeys();
                rs.next();
                gem.idDB = rs.getInt(1);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } catch (Exception e) {
                String sql = "UPDATE tob_pet SET owner=" + gem.ownerid + ",place=" + gem.place + ",info='" + gem.getDBInfo() + "', idImg=" + gem.idImg + ",name='" + gem.getNameSave() + "',itPet='" + gem.getInfoItem() + "' WHERE idPrivateSv='" + iddbPrivate + "';";
                pre2 = conn.createStatement();
                pre2.execute(sql);
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logError(e, "errorAnima.txt");
        } finally {
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            pre2.close();
        } catch (Exception ignored) {
        }
    }

    

    public void addCharEvent83(String charname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "insert into tob_top_event(charname,point1,timeupdate) values ('" + charname + "',1,now()) ON DUPLICATE KEY UPDATE point1=point1+1,timeupdate=now()";
            conn = this.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (Exception ignored) {
            }
            try {
                if (rs != null) { rs.close(); }
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pstmt.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);

        } catch (Exception ignored) {
        }
    }

    public class infoHack {

        public String charname;
        public int totalSell;
        public Vector<String> buy;

        public infoHack() {
            this.totalSell = 0;
            this.buy = new Vector<>();
        }
    }
}
