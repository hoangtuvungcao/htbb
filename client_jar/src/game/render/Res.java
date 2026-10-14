package game.render;

import game.GameMidlet;
import game.model.Char;
import game.model.CharPartInfo;
import game.model.ChatPopup;
import game.model.Cout;
import game.model.FilePack;
import game.model.FrameImage;
import game.model.GemTemp;
import game.model.IAction;
import game.model.ImageIcon;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MonsterTemplate;
import game.model.PopupName;
import game.model.PotionTemplate;
import game.model.QuickSlot;
import game.model.Tilemap;
import game.model.TreeInfo;
import game.model.WPSplashInfo;
import game.model.WeaponInfo;
import game.model.XaphuTemplate;
import game.model.arrow.MagicBeam;
import game.networklogic.GameService;
import game.render.screen.CreateCharScr;
import game.render.screen.Font;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.RMS;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Res {
    public static final int MAX_DAM = 2000000;
    public static final String xu = "xu";
    public static final String luong = "lượng";
    public static XaphuTemplate[] XAPHU;
    public static final int COLLECT_ITEM_RANGE = 35;
    public static final String doYouWantSell = "Muốn nhờ ta bán gì nào";
    public static Image[] imgSoft;
    public static Image imgPlus;
    public static Image imgShadow;
    public static Image imgTile;
    public static Image imgSelect;
    public static Image imgSelect1;
    public static Image imgSelect2;
    public static Image imgPanel;
    public static Image imgShadowPet;
    public static Image imgNotice;
    public static Image imgCoatCell;
    public static Image imgExplosion;
    public static Image imgGrid;
    public static Image imgDie;
    public static Image imgEye;
    public static Image imgEffNew;
    public static Image[] imgInfoWindow;
    public static Image imgWaitIcon;
    public static Image imgNguHanh;
    public static Image[] imgLeg;
    public static Image[] imgInv;
    public static Image imgBar;
    public static Image imgCmdBar;
    public static Image imgMSG;
    public static FrameImage imgSmoke;
    public static FrameImage backSmoke;
    public static FrameImage imgCheck;
    public static FrameImage imgNo;
    public static Image imgtileSmall;
    public static Image imgWayPoint;
    public static Image imgKham;
    public static Image imgKc;
    public static Image imgKhung;
    public static Image imgNation;
    public static int wKhung;
    public static WPSplashInfo[][] wpSplashInfos;
    public static byte[] percent;
    public static LoadData load;
    public static byte TYPE_ARROW;
    public static byte TYPE_EFFECT;
    public static byte[] effSh;
    public static Hashtable allMonterData;
    public static final short MAX_MONSTER_TEMPLATE = 115;
    public static MonsterTemplate[] monsterTemplates;
    public static final byte LEG = 0;
    public static final byte BODY = 1;
    public static final byte HEAD = 2;
    public static final byte HAT = 3;
    public static final byte COAT = 4;
    public static final byte WP = 5;
    public static final byte MOUNT = 6;
    public static Vector quanao;
    public static CharPartInfo defaultBody;
    public static CharPartInfo defaultLeg;
    public static CharPartInfo defaultHead;
    public static Hashtable treeInfos;
    public static PotionTemplate[] potionTemplates;
    public static int[] color;
    static int wh;
    static int num;
    public static String stMoney;
    public static String stVND;
    static Random r;
    public static short[][] localXaphu;
    public static String[][] namMap;
    public static String[][][] nameMap;
    public static short[][][] locationMap;
    public static String[] khamProperty;
    public static byte[] idNgocKham;
    public static final String doYouWantBuyIten = "Bạn có muốn mua vật phẩm này không ?";
    public static final String talk = "Nói chuyện";
    public static final String buy = "Bán";
    public static final String shell = "Mua";
    public static final String[] nameBigMap;
    public static final String[] nameClazz;
    public static String PORTION_ITEM_NAME = "";
    public static String PORTION_ITEM_NAME_CLAN = "";
    public static String PORTION_ITEM_NAME2 = "";
    public static Vector itemTemplates = new Vector();
    public static Vector gemTemplate = new Vector();
    public static Vector shopTemplate = new Vector();
    public static final String[][] NPC_DEFAULT_CHAT = {new String[]{"Vào trong đi cậu. Đủ loại HP từ lớn đến bé!"}, new String[]{"Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe"}, new String[]{"Vũ khí ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Ngươi muốn sửa chữa đồ?", "Ngươi muốn luyện đồ?"}, new String[]{"Mua giáp hộ thân nào …", "Mua giáp của ta, ngươi sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho ngươi biết tại sao người ta gọi ta là Thiết Bì"}, new String[]{"Trách nhiệm của ta là bảo vệ ngôi làng này."}, new String[]{"Cố gắng lên con, hãy hoàn thành các nhiệm vụ đi nhé", "Anh hùng xuất thiếu niên..anh hùng xuất thiếu niên", "Con là thành viên của làng Nghĩa Sĩ này ! Hãy nhớ lấy điều đó"}, new String[]{"Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Ngươi muốn mua vật phẩm để luyện đồ?"}, new String[]{"Đường xa vạn lý, không làm nản lòng xa phu ta đây", "Đi lâu thành lối, lối thành đường đi.", "Nói ta nghe ngươi muốn đi đâu?"}, new String[]{""}, new String[]{"Gửi đồ chỗ ta là an toàn nhất đó", "Ngươi muốn nhờ ta giữ đồ?", "Ngươi có nhiều đồ muốn gửi àh?"}, new String[]{"Bạn phải có vé mới được lên tàu."}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Tập luyện nào, tập luyện nào ..", "Sức khỏe trước đã, tập luyện nào …", "Cho ta xem khả năng của các ngươi nào !!"}, new String[]{"Giao tiền ta giữ nào, yên tâm nhé", "Ngươi muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào"}, new String[]{"Chà, lâu rồi mới có người đi qua."}, new String[]{"Ngươi muốn thuê gì?"}, new String[]{"Hãy quản lý bang của ngươi thật tốt vào."}, new String[]{"Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe"}, new String[]{"Mua giáp hộ thân nào …", "Mua giáp của ta, ngươi sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho ngươi biết tại sao người ta gọi ta là Giáp Sư"}, new String[]{"Vũ khí ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Ngươi muốn sửa chữa đồ?", "Ngươi muốn luyện đồ?"}, new String[]{"Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Ngươi muốn mua vật phẩm để luyện đồ?"}, new String[]{"Gửi đồ chỗ ta là an toàn nhất đó", "Ngươi muốn nhờ ta giữ đồ?", "Ngươi có nhiều đồ muốn gửi àh?"}, new String[]{"Giao tiền ta giữ nào, yên tâm nhé", "Ngươi muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào"}};
    public static String[] newNameAtbItem = {"Tăng Hp ", "Tăng Mp ", "Sức mạnh +", "Nhanh nhẹn +", "Tinh thần +", "Sức khoẻ +", "", "Chí mạng tăng ", "Tăng st chí mạng "};
    public static String[] nameAtbItemLock = {"Tăng công ", "Tăng thủ ma ", "Tăng thủ vật "};
    static Random rd = new Random(System.currentTimeMillis());
    public static final String[] GENDERNAME = {"bất kỳ", "nam", "nữ"};
    public static Image[] imgEffect = new Image[66];
    public static Image[] imgGround = new Image[3];
    public static Image[] imgArrow = new Image[18];
    public static Image[] imgBox = new Image[2];

    static {
        FilePack.init("/main.sh");
        imgNo = new FrameImage(FilePack.getImg("no"), 10, 10);
        imgKham = FilePack.getImg("kham");
        imgMSG = FilePack.getImg("msg0");
        imgWaitIcon = FilePack.getImg("wicon");
        imgBar = FilePack.getImg("bar");
        imgKc = FilePack.getImg("kc");
        PopupName.imgArrow = FrameImage.init("arF", 11, 9);
        createTab();
        imgInv = new Image[3];
        for (int i = 0; i < 3; i++) {
            imgInv[i] = FilePack.getImg(new StringBuffer("inv").append(i).toString());
        }
        if (Canvas.instance.hasPointerEvents()) {
            imgPanel = FilePack.getImg("panel150x36");
            wKhung = 30;
        } else {
            imgPanel = FilePack.getImg("panel");
            wKhung = 20;
        }
        imgKhung = FilePack.getImg("sk31");
        imgDie = FilePack.getImg("die");
        imgEye = FilePack.getImg("eye");
        imgPlus = FilePack.getImg("plus");
        imgShadow = FilePack.getImg("shadow");
        imgGrid = FilePack.getImg("grid");
        imgInfoWindow = new Image[2];
        for (int i2 = 0; i2 < imgInfoWindow.length; i2++) {
            imgInfoWindow[i2] = FilePack.getImg(new StringBuffer("inv_").append(i2).toString());
        }
        ChatPopup.getImg();
        QuickSlot.getImg();
        imgSmoke = new FrameImage(FilePack.getImg("smoke"), 14, 15);
        imgCheck = new FrameImage(FilePack.getImg("check"), 10, 10);
        Font.init();
        FilePack.reset();
        FilePack.init("/eff.sh");
        imgExplosion = FilePack.getImg("explosion");
        FilePack.reset();
        FilePack.init("/g.sh");
        imgGround[0] = FilePack.getImg("g0");
        imgGround[1] = FilePack.getImg("g1");
        imgGround[2] = FilePack.getImg("g2");
        FilePack.reset();
        FilePack.init("/nation");
        imgNation = FilePack.getImg("icon");
        FilePack.reset();
        FilePack.init("/box.sh");
        imgBox[0] = FilePack.getImg("b0");
        imgBox[1] = FilePack.getImg("b1");
        FilePack.reset();
        try {
            if (imgCoatCell == null) {
                imgCoatCell = Image.createImage("/m/coat.png");
            }
            if (imgShadowPet == null) {
                imgShadowPet = Image.createImage("/m/imgshadow.png");
            }
            imgNotice = Image.createImage("/m/notice.png");
        } catch (Exception e) {
            Cout.println(new StringBuffer("loi load hinh res  ++  ").append(e.toString()).toString());
        }
        try {
            imgEffNew = Image.createImage("/sword skill/h0.png");
            GameScr.img12plus = Image.createImage("/plus12.png");
            GameScr.petShadow = Image.createImage("/shadow.png");
            GameScr.imghealth = new Image[2];
            GameScr.imghealth[0] = Image.createImage("/m1.png");
            GameScr.imghealth[1] = Image.createImage("/m2.png");
        } catch (Exception e2) {
        }
        wpSplashInfos = new WPSplashInfo[5][3];
        percent = new byte[]{20, 50, 10, 70, 20};
        load = new LoadData();
        TYPE_ARROW = (byte) 0;
        TYPE_EFFECT = (byte) 1;
        allMonterData = new Hashtable();
        monsterTemplates = new MonsterTemplate[115];
        quanao = new Vector();
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        treeInfos = new Hashtable();
        potionTemplates = new PotionTemplate[Char.CHAR_POTION];
        color = new int[]{8346120, 15852810, 14527502, 14595691, 11241794, 4858880, 2181450};
        wh = 6;
        stMoney = "xu";
        stVND = "lượng";
        r = new Random();
        nameBigMap = new String[]{"Làng Sơn Nam", "Dao Châu", "Tiên Du", "Phù Liệt", "Kỳ Bố", "Hàm Tử", "Thạch Giang", "Đông Sơn", "Tử Quan", "Trường Giang", "Lộc Trĩ", "Sơn Lâm", "Hang động", "Hang mãng xà", "Hang thằn lằn", "Đấu trường", "Khu vực 1"};
        nameClazz = new String[]{"Kiếm khách", "Chiến binh", "Pháp sư", "Đấu sĩ", "Cung thủ", ""};
    }

    public static String getDefaultChat(int type) {
        String st = "";
        try {
            st = NPC_DEFAULT_CHAT[type][GameScr.abs(rd.nextInt(NPC_DEFAULT_CHAT[type].length))];
        } catch (Exception e) {
        }
        return st;
    }

    public static void createTab() {
        int n = (Canvas.w / 2) + 1;
        imgCmdBar = Image.createImage(Canvas.w, imgKc.getHeight());
        Graphics g = imgCmdBar.getGraphics();
        for (int i = 0; i < n; i++) {
            g.drawImage(imgKc, i * 2, 0, 0);
        }
    }

    public static void setEffSh(byte[] arr) {
        effSh = arr;
        FilePack.initByArray(arr);
        imgExplosion = FilePack.getImg("explosion");
        FilePack.reset();
        effSh = null;
    }

    public static void loadPart2() {
        if (imgSelect == null) {
            FilePack.init("/main.sh");
            imgSelect = FilePack.getImg("select");
            imgSelect1 = FilePack.getImg("select1");
            imgSelect2 = FilePack.getImg("select3");
            GameScr.imgCong = new Image[2];
            for (int i = 0; i < 2; i++) {
                GameScr.imgCong[i] = FilePack.getImg(new StringBuffer("cong").append(i + 1).toString());
            }
            if (GameScr.imgInfo == null) {
                GameScr.imgInfo = FilePack.getImg("info");
            }
            if (GameScr.imgHpMonster == null) {
                GameScr.imgHpMonster = FilePack.getImg("mauquai");
            }
            MagicBeam.imgFire = FilePack.getImg("fire");
            Char.imgFlgPk = FilePack.getImg("flg");
            Char.imgLight = FilePack.getImg("light");
            imgSoft = new Image[2];
            for (int i2 = 0; i2 < 2; i2++) {
                imgSoft[i2] = FilePack.getImg(new StringBuffer("soft").append(i2).toString());
            }
            imgNguHanh = FilePack.getImg("nguhanh");
            if (imgLeg == null) {
                imgLeg = new Image[2];
            }
            for (int i3 = 0; i3 < 2; i3++) {
                imgLeg[i3] = FilePack.getImg(new StringBuffer("ch").append(i3).toString());
            }
            FilePack.reset();
        }
    }

    public static GemTemp getGemByID(short id) {
        for (int i = 0; i < gemTemplate.size(); i++) {
            GemTemp g = (GemTemp) gemTemplate.elementAt(i);
            if (g.id == id) {
                return g;
            }
        }
        return null;
    }

    public static int random(int a, int b) {
        return a + r.nextInt(b - a);
    }

    public static boolean isDestroy(int x1, int xw1, int x2, int xw2, int y1, int yh1, int y2, int yh2) {
        if (x1 > xw2 || xw1 < x2 || y1 > yh2 || yh1 < y2) {
            return false;
        }
        return true;
    }

    public static int[] getAttributeItem(int charClass, int idTemplate, int type) {
        int[] attb = new int[10];
        ItemTemplate it = ((ItemTemplate[]) itemTemplates.elementAt(0))[idTemplate];
        attb[0] = it.attb[0];
        for (int i = 1; i < 4; i++) {
            attb[i] = (short) (it.attb[i] + (ItemInInventory.isAnimalArmor(type) ? 0 : (it.attb[i] * percent[charClass]) / 100));
        }
        for (int i2 = 4; i2 < 10; i2++) {
            attb[i2] = it.attb[i2];
        }
        return attb;
    }

    public static ItemTemplate getItem(int clazz, int id) {
        ItemTemplate[] item = (ItemTemplate[]) itemTemplates.elementAt(0);
        for (int i = 0; i < item.length; i++) {
            if (item[i] != null && item[i].id == id) {
                return item[i];
            }
        }
        return null;
    }

    public static GemTemp getShopByID(short id) {
        for (int i = 0; i < shopTemplate.size(); i++) {
            GemTemp g = (GemTemp) shopTemplate.elementAt(i);
            if (g.id == id) {
                return g;
            }
        }
        return null;
    }

    public static XaphuTemplate getXaphu() {
        if (Tilemap.idXaphu >= 0) {
            return XAPHU[Tilemap.idXaphu];
        }
        return null;
    }

    public static Image createImgByHeader(byte[] header, byte[] data) {
        byte[] total = new byte[header.length + data.length];
        System.arraycopy(header, 0, total, 0, header.length);
        System.arraycopy(data, 0, total, header.length, data.length);
        return Image.createImage(total, 0, total.length);
    }

    public static void removeWeaPone() {
        for (int i = 0; i < Char.imgWeapone.length; i++) {
            for (int j = 0; j < Char.imgWeapone[i].length; j++) {
                for (int a = 0; a < Char.imgWeapone[i][j].length; a++) {
                    Char.imgWeapone[i][j][a] = null;
                }
            }
        }
        for (int i2 = 0; i2 < 5; i2++) {
            for (int j2 = 0; j2 < 3; j2++) {
                wpSplashInfos[i2][j2] = null;
            }
        }
    }

    public static WeaponInfo loadImgWeaPone(int i, int j, int index) {
        if (Char.imgWeapone[i][j][index] == null) {
            load.loadImgWeaPone(i, j, index);
        }
        return Char.imgWeapone[i][j][index];
    }

    public static WPSplashInfo loadWpsPlash(int i, int j) {
        if (wpSplashInfos[i][j] == null) {
            load.loadWpsPlash(i, j);
        }
        return wpSplashInfos[i][j];
    }

    public static class LoadData implements Runnable {
        IAction iaLoadMap;
        int iWpsPlash = -1;
        int jWpsPlash = -1;
        int iImgWeaPone = -1;
        int jImgWeaPone = -1;
        int indexWeapone = -1;
        int indexImgEffect = -1;
        int indexTreeImg = -1;
        int indexMap = -1;
        int indexArrow = -1;
        public boolean isInitGame = false;
        public byte[] byteMap = null;

        public void loadMap(int index, IAction ia, byte[] byteMap) {
            this.indexMap = index;
            this.iaLoadMap = ia;
            this.byteMap = byteMap;
            Tilemap.reset();
        }

        public void loadWpsPlash(int i, int j) {
            if (this.iWpsPlash == -1 && this.jWpsPlash == -1) {
                this.iWpsPlash = i;
                this.jWpsPlash = j;
            }
        }

        public void loadImgWeaPone(int i, int j, int index) {
            if (this.iImgWeaPone == -1 && this.jImgWeaPone == -1) {
                this.iImgWeaPone = i;
                this.jImgWeaPone = j;
                this.indexWeapone = index;
            }
        }

        public void loadImgEffect(int index) {
            if (this.indexImgEffect == -1) {
                this.indexImgEffect = index;
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (this.iWpsPlash != -1 && this.jWpsPlash != -1) {
                    Res.GetWPSplashInfo(this.iWpsPlash, this.jWpsPlash);
                    this.iWpsPlash = -1;
                    this.jWpsPlash = -1;
                }
                if (this.iImgWeaPone != -1 && this.jImgWeaPone != -1) {
                    Res.getImgWeaPone(this.iImgWeaPone, this.jImgWeaPone, this.indexWeapone);
                    this.iImgWeaPone = -1;
                    this.jImgWeaPone = -1;
                }
                if (this.indexImgEffect != -1) {
                    try {
                        Res.getImgEffect(this.indexImgEffect);
                    } catch (IOException e2) {
                    }
                    this.indexImgEffect = -1;
                }
                if (this.indexMap != -1) {
                    int aa = this.indexMap;
                    this.indexMap = -1;
                    Tilemap.loadMap(aa, this.byteMap);
                    this.iaLoadMap.perform();
                    this.byteMap = null;
                }
                if (this.indexArrow != -1) {
                    Res.loadImgArrow(this.indexArrow);
                    this.indexArrow = -1;
                }
                if (this.isInitGame) {
                    GameMidlet.instance.load();
                    this.isInitGame = false;
                }
            }
        }
    }

    public static Image getImgArrow(int i) {
        if (i < imgArrow.length - 1) {
            if (imgArrow[i] == null && imgArrow[i] == null && load.indexArrow == -1) {
                load.indexArrow = i;
            }
            Image imgFromclient = imgArrow[i];
            if (imgFromclient != null) {
                return imgFromclient;
            }
        }
        ImageIcon img = GameData.getImgIcon((short) (i + 10000));
        if (img != null && img.img != null) {
            return img.img;
        }
        return null;
    }

    public static Image getImageEffect(int id) {
        if (id <= imgEffect.length - 1) {
            if (imgEffect[id] == null) {
                load.loadImgEffect(id);
            }
            Image imgFromclient = imgEffect[id];
            if (imgFromclient != null) {
                return imgFromclient;
            }
        }
        ImageIcon img = GameData.getImgIcon((short) (id + 9000));
        if (img != null && img.img != null) {
            return img.img;
        }
        return null;
    }

    public static void loadImgArrow(int i) {
        FilePack.init("/arrow.sh");
        try {
            if (i == 7) {
                imgArrow[i] = Image.createImage("/sword skill/kiem.png");
            } else if (i == 8) {
                imgArrow[i] = Image.createImage("/sword skill/skillboss.png");
            } else if (i == 9) {
                imgArrow[i] = Image.createImage("/sword skill/nut.png");
            } else if (i == 10) {
                imgArrow[10] = Image.createImage("/newEf/46.png");
            } else if (i == 11) {
                imgArrow[11] = Image.createImage("/newEf/47.png");
            } else if (i == 12) {
                imgArrow[12] = Image.createImage("/newEf/48.png");
            } else if (i == 13) {
                imgArrow[i] = Image.createImage("/newEf/54.png");
            } else {
                if (i == 14) {
                    imgArrow[i] = Image.createImage("/newEf/56.png");
                }
                FilePack.reset();
            }
        } catch (Exception e) {
        }
        FilePack.reset();
    }

    public static void paintImgEff(Graphics g, int type, int x0, int y0, int w, int h, int x, int y, int anthor) {
        try {
            Image img = getImageEffect(type);
            if (img != null) {
                g.drawRegion(img, x0, y0, w, h, 0, x, y, anthor);
            }
        } catch (Exception e) {
        }
    }

    public static Image loadImgEff(int index) {
        ImageIcon img = GameData.getImgIcon((short) (index + 9000));
        if (img != null && img.img != null) {
            if (index == 32) {
                backSmoke = new FrameImage(img.img, 12, 13);
            }
            return img.img;
        }
        return null;
    }

    public static void getImgEffect(int index) throws IOException {
        FilePack.init("/eff.sh");
        if (index < 25 || index > 33) {
            try {
                imgEffect[index] = FilePack.getImg(new StringBuffer("g").append(index).toString());
            } catch (Exception e) {
                Cout.println("Loi effect");
            }
        }
        if (imgEffect[26] == null) {
            imgEffect[25] = FilePack.getImg("g25");
            imgEffect[26] = FilePack.getImg("g26");
            imgEffect[27] = Image.createImage(imgEffect[25]);
            imgEffect[28] = FilePack.getImg("g27");
            imgEffect[29] = FilePack.getImg("g28");
            imgEffect[30] = FilePack.getImg("g29");
            imgEffect[31] = FilePack.getImg("g30");
            imgEffect[32] = FilePack.getImg("g31");
            imgEffect[33] = FilePack.getImg("g32");
            imgEffect[41] = Image.createImage("/newEf/42.png");
            imgEffect[42] = Image.createImage("/newEf/43.png");
            imgEffect[43] = Image.createImage("/newEf/44.png");
            imgEffect[44] = Image.createImage("/newEf/45.png");
            imgEffect[45] = Image.createImage("/newEf/49.png");
            imgEffect[46] = Image.createImage("/newEf/50.png");
            imgEffect[47] = Image.createImage("/newEf/51.png");
            imgEffect[48] = Image.createImage("/newEf/52.png");
            imgEffect[49] = Image.createImage("/newEf/53.png");
            imgEffect[50] = Image.createImage("/newEf/55.png");
            imgEffect[51] = Image.createImage("/newEf/57.png");
            imgEffect[52] = Image.createImage("/newEf/58.png");
            imgEffect[53] = Image.createImage("/newEf/59.png");
            imgEffect[54] = Image.createImage("/newEf/60.png");
            imgEffect[55] = Image.createImage("/newEf/61.png");
            imgEffect[63] = Image.createImage("/newEf/63.png");
            imgEffect[64] = Image.createImage("/newEf/64.png");
            imgEffect[65] = Image.createImage("/newEf/65.png");
            backSmoke = new FrameImage(imgEffect[32], 12, 13);
        }
        FilePack.reset();
    }

    public static void getImgWeaPone(int i, int j, int index) {
        String[] st = {"kiem", "daidao", "phapsu", "bua", "cung"};
        FilePack.init(new StringBuffer("/wpsplash/").append(st[i]).append("/").append(j).toString());
        Char.imgWeapone[i][j][index] = new WeaponInfo();
        try {
            byte[] header = FilePack.instance.loadFile(new StringBuffer(String.valueOf(index)).append("_h").toString());
            byte[] data = FilePack.instance.loadFile("data");
            Char.imgWeapone[i][j][index].img = createImgByHeader(header, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FilePack.reset();
    }

    public static WPSplashInfo GetWPSplashInfo(int i, int j) {
        try {
            wpSplashInfos[i][j] = new WPSplashInfo();
            int b = j;
            if (i == 3 || i == 4 || (i == 2 && b == 1)) {
                b = 0;
            } else if ((i == 0 || i == 1) && b == 2) {
                b = 1;
            }
            FilePack instance = new FilePack(new StringBuffer("/wpsplash/").append(i).append(".wp").toString(), null);
            wpSplashInfos[i][j].image = instance.loadImage(new StringBuffer().append(b).append(".png").toString());
            InputStream is = instance.loadData(new StringBuffer(String.valueOf(b)).append(".d").toString());
            if (is != null) {
                for (int d = 0; d < 4; d++) {
                    for (int f = 0; f < 8; f++) {
                        wpSplashInfos[i][j].P0_X[d][f] = is.read();
                        wpSplashInfos[i][j].P0_Y[d][f] = is.read();
                        wpSplashInfos[i][j].PF_X[d][f] = readSignByte(is);
                        wpSplashInfos[i][j].PF_Y[d][f] = readSignByte(is);
                        wpSplashInfos[i][j].PF_W[d][f] = is.read();
                        wpSplashInfos[i][j].PF_H[d][f] = is.read();
                    }
                }
            }
            FilePack.reset();
        } catch (IOException e) {
            wpSplashInfos[i][j] = null;
        }
        return wpSplashInfos[i][j];
    }

    public static void removeImgEffect() {
        imgWayPoint = null;
    }

    public static void loadMonster(int type, byte[] info) {
        monsterTemplates[type].setInfo(type, "", new StringBuffer("m").append(type).toString(), info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7], info[8]);
    }

    public static void resetImgMonsTemp() {
        if (monsterTemplates != null) {
            for (int i = 0; i < monsterTemplates.length; i++) {
                if (monsterTemplates[i] != null) {
                    monsterTemplates[i].image = null;
                    monsterTemplates[i].imageObj = null;
                }
            }
        }
    }

    public static void removeMonsterTemplet() {
        resetImgMonsTemp();
        treeInfos.clear();
    }

    public static void loadMonster() {
    }

    public static void removePartInfo() {
    }

    public static CharPartInfo getCharPartInfo(int type, int id) {
        Hashtable has = (Hashtable) quanao.elementAt(type);
        CharPartInfo cp = (CharPartInfo) has.get(new StringBuffer(String.valueOf(id)).toString());
        if (cp == null) {
            cp = new CharPartInfo(type, id);
            cp.time = (int) (System.currentTimeMillis() / 1000);
            if (cp.image == null) {
                GameService.gI().getTreeImage(-1, type, id);
            }
            has.put(new StringBuffer(String.valueOf(id)).toString(), cp);
        }
        if (cp.image == null && (System.currentTimeMillis() / 1000) - ((long) cp.time) == 15) {
            GameService.gI().getTreeImage(-1, type, id);
            cp.time = (int) (System.currentTimeMillis() / 1000);
        }
        cp.timeRemove = (int) (System.currentTimeMillis() / 1000);
        return cp;
    }

    public static void paintDefault(Graphics g, int xp, int yp, int dir, int frame, int type) {
        if (type == 1 && defaultBody != null) {
            defaultBody.paint(g, xp, type, dir, frame);
        }
        if (type == 0 && defaultLeg != null) {
            defaultLeg.paint(g, xp, type, dir, frame);
        }
        if (type == 2 && defaultHead != null) {
            defaultHead.paint(g, xp, type, dir, frame);
        }
    }

    public static void resetTrangBi() {
        if (defaultBody == null) {
            defaultBody = getCharPartInfo(1, 54);
        }
        if (defaultLeg == null) {
            defaultLeg = getCharPartInfo(0, 54);
        }
        if (defaultHead == null) {
            defaultHead = getCharPartInfo(2, 27);
        }
    }

    public static TreeInfo getTree(int index) {
        TreeInfo tree = (TreeInfo) treeInfos.get(new StringBuffer().append(index).toString());
        if (tree == null) {
            tree = new TreeInfo();
            tree.id = (short) index;
            treeInfos.put(new StringBuffer().append(index).toString(), tree);
        }
        return tree;
    }

    public static int getHeightTree(int index) {
        TreeInfo tree = getTree(index);
        if (tree != null) {
            return tree.getHeight();
        }
        return 0;
    }

    public static int getwidthTree(int index) {
        TreeInfo tree = getTree(index);
        if (tree != null) {
            return tree.getWidth();
        }
        return 0;
    }

    public static int getDxTree(int index) {
        TreeInfo tree = getTree(index);
        if (tree != null) {
            return tree.dx;
        }
        return 0;
    }

    public static int getDyTree(int index) {
        TreeInfo tree = getTree(index);
        if (tree != null) {
            return tree.dy;
        }
        return 0;
    }

    public static final void loadTreeImage() {
    }

    public static final void removeTreeImage(int i) {
    }

    public static int readSignByte(InputStream is) {
        byte[] x = new byte[1];
        try {
            is.read(x, 0, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return x[0];
    }

    public static final void loadTileImage(int lv) {
        InputStream is;
        if (imgTile == null) {
            try {
                if (lv > 200) {
                    try {
                        imgTile = Image.createImage("/t_thanh.png");
                    } catch (Exception e) {
                        byte[] data = RMS.loadRMS(Canvas.nameTile[1]);
                        imgTile = Image.createImage(data, 0, data.length);
                    }
                    is = "".getClass().getResourceAsStream("/t_thanh.type");
                    imgtileSmall = Image.createImage("/t_thanh_s.png");
                } else if (lv < 110) {
                    imgTile = Image.createImage("/t.png");
                    is = "".getClass().getResourceAsStream("/t.type");
                    imgtileSmall = Image.createImage("/t_small.png");
                } else {
                    try {
                        imgTile = Image.createImage("/t_hang.png");
                    } catch (Exception e2) {
                        byte[] data2 = RMS.loadRMS(Canvas.nameTile[0]);
                        imgTile = Image.createImage(data2, 0, data2.length);
                    }
                    is = "".getClass().getResourceAsStream("/t_hang.type");
                    imgtileSmall = Image.createImage("/t_hang_s.png");
                }
                try {
                    Tilemap.typeOfTile = null;
                    Tilemap.typeOfTile = new int[is.available()];
                    for (int i = 0; i < Tilemap.typeOfTile.length; i++) {
                        Tilemap.typeOfTile[i] = is.read();
                    }
                } catch (Exception e3) {
                    Cout.println("Error Load Back Tile");
                }
            } catch (Exception e4) {
            }
        }
    }

    public static void paintDlgFull(Graphics g, int x, int y) {
        int x2 = x - 10;
        int yy = y + 14;
        g.drawImage(imgInv[2], x2 + 15, yy, 20);
        g.drawImage(imgInv[2], x2 + 85, yy, 20);
        g.drawImage(imgInv[2], x2 + 15, yy + 46, 20);
        g.drawImage(imgInv[2], x2 + 85, yy + 46, 20);
        if (Canvas.currentScreen == CreateCharScr.gI()) {
            g.drawImage(imgInv[2], x2 + 15, (yy + 92) - 10, 20);
            g.drawImage(imgInv[2], x2 + 85, (yy + 92) - 10, 20);
        } else {
            g.drawImage(imgInv[2], x2 + 15, yy + 92, 20);
            g.drawImage(imgInv[2], x2 + 85, yy + 92, 20);
            g.drawImage(imgInv[2], x2 + 15, yy + 92 + 5, 20);
            g.drawImage(imgInv[2], x2 + 85, yy + 92 + 5, 20);
        }
        int xc = x2 + 12;
        int yc = y + 11;
        int h = Canvas.currentScreen == CreateCharScr.gI() ? 129 : 144;
        for (int i = 0; i < 3; i++) {
            g.setColor(color[i]);
            g.drawRect(xc + i, yc + i, 144 - (i * 2), (h - (i * 2)) + 5);
        }
        g.drawImage(imgInv[0], x2 + 10, y + 9, 20);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x2 + 159, y + 9, 24);
        if (Canvas.currentScreen == CreateCharScr.gI()) {
            g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x2 + 10, y + 148, 36);
            g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, x2 + 159, y + 148, 40);
        } else {
            g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x2 + 10, y + 163, 36);
            g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, x2 + 159, y + 163, 40);
        }
    }

    public static void paintSelected(Graphics g, int x, int y, int w, int h) {
        g.setColor(34949);
        g.fillRect(x, y, w, h);
        g.setColor(11908790);
        g.drawRect(x, y, w, h);
    }

    public static void paintDlgDragonFull(Graphics g, int x, int y, int w, int h) {
        int nW = (w / 70) + 1;
        int nH = (h / 47) + 1;
        g.setClip(x, y, w, h);
        for (int i = 0; i < nW; i++) {
            for (int j = 0; j < nH; j++) {
                g.drawImage(imgInv[2], x + (i * 70), y + (j * 47), 0);
            }
        }
        g.setClip(0, 0, Canvas.w, Canvas.h);
        for (int i2 = 0; i2 < 3; i2++) {
            g.setColor(color[i2]);
            g.drawRect(x + i2, y + i2, w - (i2 * 2), h - (i2 * 2));
        }
        g.drawImage(imgInv[0], x - 2, y - 2, 0);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, (x + w) - 15, y - 2, 0);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x - 2, (y + h) - 15, 0);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, (x + w) - 15, (y + h) - 16, 0);
        g.drawImage(imgInv[1], x + (w / 2), y - 4, 3);
    }

    public static void paintRectColor(Graphics g, int x, int y, int w, int h, int count, int color1, int color2) {
        g.setColor(color1);
        g.drawRect(x, y, w, h);
        g.setColor(color2);
        if (count < w) {
            int aa = wh;
            if (aa + count >= w) {
                aa = w - count;
            }
            g.fillRect(x + count, y, aa, 1);
        } else if (count < w + h) {
            int aa2 = wh;
            if (aa2 + (count - w) >= h) {
                aa2 = h - (count - w);
            }
            g.fillRect(x + w, y + (count - w), 1, aa2);
        } else if (count < (w * 2) + h) {
            int aa3 = wh;
            if (aa3 + ((count - w) - h) >= w) {
                aa3 = w - ((count - w) - h);
            }
            g.fillRect((x + (w - ((count - w) - h))) - aa3, y + h, aa3, 1);
        } else if (count < w * h * 2) {
            int aa4 = wh;
            if (aa4 + ((count - (w * 2)) - h) >= h) {
                aa4 = h - ((count - (w * 2)) - h);
            }
            g.fillRect(x, (y + (h - ((count - (w * 2)) - h))) - aa4, 1, aa4);
        }
        num++;
        if (num >= 4) {
            num = 0;
        }
    }

    public static void paintDlgFull(Graphics g, int x, int y, int w, int h) {
        int aa = (w / 70) + 1;
        g.setClip(x, y, w, h);
        for (int i = 0; i < aa; i++) {
            g.drawImage(imgInv[2], x + (70 * i), y, 0);
        }
        g.setColor(277044);
        g.fillRect(x, y + 25, w, h);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        for (int i2 = 0; i2 < 3; i2++) {
            g.setColor(color[i2]);
            g.drawRect(x + i2, y + i2, (w - (i2 * 2)) - 1, (h - (i2 * 2)) - 1);
            g.fillRect(x + 3, y + 25, w - 6, 1);
        }
        g.drawImage(imgInv[0], x - 2, y - 2, 0);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + w + 2, y - 2, 24);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x - 2, y + h + 2, 36);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, x + w + 2, y + h + 2, 40);
    }

    public static void paintDlgFull(Graphics g, int x, int y, int w, int h, int y1, String tile, boolean isDD, int h0) {
        int aa = (w / 70) + 1;
        g.setClip(x, y, w, h);
        for (int i = 0; i < aa; i++) {
            g.drawImage(imgInv[2], x + (70 * i), y, 0);
        }
        g.setColor(277044);
        g.fillRect(x, y + 25, w, h);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        for (int i2 = 0; i2 < 3; i2++) {
            g.setColor(color[i2]);
            g.drawRect(x + i2, y + i2, (w - (i2 * 2)) - 1, (h - (i2 * 2)) - 1);
        }
        g.fillRect(x + 3, y + 25, w - 6, 1);
        if (!isDD) {
            g.fillRect(x + 3, y + 25 + y1 + h0, w - 6, 1);
        } else {
            g.fillRect(x + 3, y + 25 + y1 + h0, w - 6, 1);
            g.fillRect(x + 3, ((y + y1) + h0) - 2, w - 6, 1);
        }
        Font.normalFont[0].drawString(g, tile, x + (w / 2), y + 8, 2);
        g.drawImage(imgInv[0], x - 2, y - 2, 0);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + w + 2, y - 2, 24);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x - 2, y + h + 2, 36);
        g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, x + w + 2, y + h + 2, 40);
    }

    public static void drawRect(Graphics g, int x, int y, int w, int h) {
        for (int i = 0; i < 3; i++) {
            g.setColor(color[i]);
            g.drawRect(x + i, y + i, w - (i * 2), h - (i * 2));
        }
    }

    public static int[] trimArray(int[] array) {
        Vector temp = new Vector();
        for (int i = 0; i < array.length; i++) {
            if (temp.size() == 0) {
                temp.addElement(new StringBuffer(String.valueOf(array[i])).toString());
            } else {
                boolean dup = false;
                for (int j = 0; j < temp.size(); j++) {
                    int value = Integer.parseInt((String) temp.elementAt(j));
                    if (value == array[i]) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    temp.addElement(new StringBuffer(String.valueOf(array[i])).toString());
                }
            }
        }
        int[] result = new int[temp.size()];
        for (int i2 = 0; i2 < temp.size(); i2++) {
            result[i2] = Integer.parseInt((String) temp.elementAt(i2));
        }
        return result;
    }

    public static void paintPotion(Graphics g, int index, int x, int y, int anthor) {
        GameData.paintIcon(g, (short) (index + 5500), x, y, anthor);
    }

    public static void paintGem(Graphics g, int index, int x, int y) {
        GameData.paintIcon(g, (short) (index + 6500), x, y, 3);
    }

    public static void paintGemKhoa(Graphics g, int index, int x, int y) {
        GameData.paintIcon(g, (short) (index + 6500), x, y, 3);
    }

    public static Image createImgByByteArray(byte[] array) {
        return Image.createImage(array, 0, array.length);
    }

    public static int rnd(int a) {
        return r.nextInt(a);
    }

    public static int rnd(int a, int b) {
        int aa = r.nextInt(2);
        if (aa == 0) {
            return a;
        }
        return b;
    }

    public static String[] getNameMap(int index, int id) {
        try {
            return nameMap[index][id];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static short[] getLocation(int index, int id) {
        try {
            return locationMap[index][id];
        } catch (Exception e) {
            return null;
        }
    }

    public static Image getWayPoint() {
        if (imgWayPoint == null) {
            FilePack.init("/main.sh");
            imgWayPoint = FilePack.getImg("waypoint");
            FilePack.reset();
        }
        return imgWayPoint;
    }

    public static Image transparentBG(Image img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] rgb = new int[w * h];
        img.getRGB(rgb, 0, w, 0, 0, w, h);
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] == -65315) {
                rgb[i] = 16777215;
            }
        }
        return Image.createRGBImage(rgb, w, h, true);
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = ((b.length - 1) - i) * 8;
            b[i] = (byte) ((value >>> offset) & 255);
        }
        return b;
    }

    public static int random(int a) {
        return r.nextInt(a);
    }

    public static int sqrt(int a) {
        int x1;
        if (a <= 0) {
            return 0;
        }
        int x = (a + 1) / 2;
        do {
            x1 = x;
            x = (x / 2) + (a / (2 * x));
        } while (Math.abs(x1 - x) > 1);
        return x;
    }

    public static int getDistance(int x, int y, int x2, int y2) {
        return getDistance(x - x2, y - y2);
    }

    public static int getDistance(int x, int y) {
        return sqrt((x * x) + (y * y));
    }

    public static int random_Am_0(int a) {
        int iNextInt = 0;
        while (true) {
            int t = iNextInt;
            if (t == 0) {
                iNextInt = r.nextInt() % a;
            } else {
                return t;
            }
        }
    }

    public static DataInputStream openFile(String path) {
        DataInputStream file = new DataInputStream("".getClass().getResourceAsStream(path));
        return file;
    }

    public static int getRange(int x1, int x2, int y1, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static int byteToInit(byte[] bArr) {
        return ((255 & bArr[0]) << 24) | ((255 & bArr[1]) << 16) | ((255 & bArr[2]) << 8) | (255 & bArr[3]);
    }
}
