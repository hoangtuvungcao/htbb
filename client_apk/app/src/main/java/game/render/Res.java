package game.render;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.htbb.client.GameMidlet;
import game.core.j2me.FontSys;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.Char;
import game.model.CharPartInfo;
import game.model.ChatPopup;
import game.model.Cout;
import game.model.FilePack;
import game.model.FrameImage;
import game.model.GemTemp;
import game.model.IAction;
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
import game.render.screen.Font;
import game.render.screen.FontBitMap;
import game.render.screen.GameData;
import game.render.screen.GameScr;
import game.render.screen.MenuSelectItem;
import game.render.screen.MoQuaScr;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public class Res {
    public static final byte BODY = 1;
    public static final byte COAT = 4;
    public static final int COLLECT_ITEM_RANGE = 35;
    public static final byte HAT = 3;
    public static final byte HEAD = 2;
    public static final byte LEG = 0;
    public static final int MAX_DAM = 2000000;
    public static final short MAX_MONSTER_TEMPLATE = 115;
    public static final byte MOUNT = 6;
    public static String PORTION_ITEM_NAME = "";
    public static String PORTION_ITEM_NAME2 = "";
    public static String PORTION_ITEM_NAME_CLAN = "";
    public static final byte WP = 5;
    public static XaphuTemplate[] XAPHU = null;
    public static FrameImage backSmoke = null;
    public static final String buy = "Bán";
    public static int[] color = null;
    public static CharPartInfo defaultBody = null;
    public static CharPartInfo defaultHead = null;
    public static CharPartInfo defaultLeg = null;
    public static final String doYouWantBuyIten = "Bạn có muốn mua vật phẩm này không ?";
    public static final String doYouWantSell = "Muốn nhờ ta bán gì nào";
    public static byte[] effSh = null;
    public static byte[] idNgocKham = null;
    public static Bitmap imgBGTiemNang = null;
    public static Bitmap imgBar = null;
    public static Bitmap imgBgPopup = null;
    public static Bitmap imgBgTrangBi = null;
    public static Bitmap imgBtnBarFocus = null;
    public static Bitmap imgButtonBar = null;
    public static FrameImage imgCheck = null;
    public static Bitmap imgCmdBar = null;
    public static Bitmap imgDie = null;
    public static Bitmap imgEffNew = null;
    public static Bitmap imgExplosion = null;
    public static Bitmap imgEye = null;
    public static Bitmap imgGrid = null;
    public static Bitmap imgIconSkillBua = null;
    public static Bitmap imgIconSkillCung = null;
    public static Bitmap imgIconSkillDao = null;
    public static Bitmap imgIconSkillKiem = null;
    public static Bitmap imgIconSkillPs = null;
    public static Bitmap[] imgInfoWindow = null;
    public static Bitmap[] imgInv = null;
    public static Bitmap imgKc = null;
    public static Bitmap imgKham = null;
    public static Bitmap imgKhoa = null;
    public static Bitmap imgKhung = null;
    public static Bitmap[] imgLeg = null;
    public static Bitmap imgMSG = null;
    public static Bitmap imgNation = null;
    public static Bitmap imgNguHanh = null;
    public static FrameImage imgNo = null;
    public static Bitmap imgPanel = null;
    public static Bitmap imgPlus = null;
    public static Bitmap imgPopupGoc = null;
    public static Bitmap imgPopupNgoc = null;
    public static Bitmap imgSD = null;
    public static Bitmap imgSanta = null;
    public static Bitmap imgSelect = null;
    public static Bitmap imgSelect1 = null;
    public static Bitmap imgSelect2 = null;
    public static Bitmap imgShadow = null;
    public static FrameImage imgSmoke = null;
    public static Bitmap[] imgSoft = null;
    public static Bitmap imgTile = null;
    public static Bitmap imgWaitIcon = null;
    public static Bitmap imgWayPoint = null;
    public static Bitmap imgtileSmall = null;
    public static String[] khamProperty = null;
    public static short[][] localXaphu = null;
    public static short[][][] locationMap = null;
    public static final String luong = "lượng";
    public static int mauTile = 0;
    public static String[][] namMap = null;
    public static final String[] nameBigMap;
    public static final String[] nameClazz;
    public static String[][][] nameMap = null;
    static int num = 0;
    public static PotionTemplate[] potionTemplates = null;
    public static Vector quanao = null;
    static Random r = null;
    public static final String shell = "Mua";
    public static String stMoney = null;
    public static String stVND = null;
    public static final String talk = "Nói chuyện";
    public static Hashtable treeInfos = null;
    public static int wKhung = 0;
    static int wh = 0;
    public static final String xu = "xu";
    public static Vector itemTemplates = new Vector();
    public static Vector gemTemplate = new Vector();
    public static Vector shopTemplate = new Vector();
    public static final String[][] NPC_DEFAULT_CHAT = {new String[]{"Vào trong đi cậu. Đủ loại HP từ lớn đến bé!"}, new String[]{"Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe"}, new String[]{"Đồ cầm tay ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Cậu muốn sửa chữa đồ?", "Cậu muốn luyện đồ?"}, new String[]{"Mua giáp hộ thân nào …", "Mua giáp của ta, cậu sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho cậu biết tại sao người ta gọi ta là Bronzong"}, new String[]{"Trách nhiệm của ta là bảo vệ ngôi làng này."}, new String[]{"Cố gắng lên con, hãy hoàn thành các nhiệm vụ đi nhé", "Tuổi trẻ tài cao..tuổi trẻ tài cao", "Con là thành viên của làng huấn luyện này! Hãy nhớ lấy điều đó"}, new String[]{"Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Cậu muốn mua vật phẩm để luyện đồ?"}, new String[]{"Đường xa vạn lý, không làm nản lòng Pidgeot (Bay) ta đây", "Đi lâu thành lối, lối thành đường đi.", "Nói ta nghe cậu muốn đi đâu?"}, new String[]{""}, new String[]{"Gửi đồ chỗ ta là an toàn nhất đó", "Cậu muốn nhờ ta giữ đồ?", "Cậu có nhiều đồ muốn gửi àh?"}, new String[]{"Bạn phải có vé mới được lên tàu."}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Muốn nhờ ta bán gì nào"}, new String[]{"Tập luyện nào, tập luyện nào ..", "Sức khỏe trước đã, tập luyện nào …", "Cho ta xem khả năng của các cậu nào !!"}, new String[]{"Giao tiền ta giữ nào, yên tâm nhé", "Cậu muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào"}, new String[]{"Chà, lâu rồi mới có người đi qua."}, new String[]{"Cậu muốn thuê gì?"}, new String[]{"Hãy quản lý đội của cậu thật tốt vào."}, new String[]{"Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe"}, new String[]{"Mua giáp hộ thân nào …", "Mua giáp của ta, cậu sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho cậu biết tại sao người ta gọi ta là Bronzong"}, new String[]{"Đồ cầm tay ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Cậu muốn sửa chữa đồ?", "Cậu muốn luyện đồ?"}, new String[]{"Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Cậu muốn mua vật phẩm để luyện đồ?"}, new String[]{"Gửi đồ chỗ ta là an toàn nhất đó", "Cậu muốn nhờ ta giữ đồ?", "Cậu có nhiều đồ muốn gửi àh?"}, new String[]{"Giao tiền ta giữ nào, yên tâm nhé", "Cậu muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào"}};
    public static String[] newNameAtbItem = {"HP +", "PP +", "Sức mạnh +", "Nhanh nhẹn +", "Tinh thần +", "Sức khoẻ +", "", "Chí mạng +", "ST chí mạng % +"};
    public static String[] nameAtbItemLock = {"Tăng công ", "Tăng phòng thủ đặc biệt ", "Tăng phòng thủ "};
    static Random rd = new Random(System.currentTimeMillis());
    public static final String[] GENDERNAME = {"bất kỳ", "nam", "nữ"};
    public static Bitmap[] imgEffect = new Bitmap[63];
    public static Bitmap[] imgGround = new Bitmap[3];
    public static Bitmap[] imgArrow = new Bitmap[20];
    public static WPSplashInfo[][] wpSplashInfos = (WPSplashInfo[][]) Array.newInstance((Class<?>) WPSplashInfo.class, 5, 3);
    public static byte[] percent = {20, 50, 10, 70, 20};
    public static LoadData load = new LoadData();
    public static Hashtable allMonterData = new Hashtable();
    public static MonsterTemplate[] monsterTemplates = new MonsterTemplate[115];

    public static boolean isDestroy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return i <= i4 && i2 >= i3 && i5 <= i8 && i6 >= i7;
    }

    public static void loadMonster() {
    }

    public static final void loadTreeImage() {
    }

    public static void removePartInfo() {
    }

    public static final void removeTreeImage(int i) {
    }

    static {
        Vector vector = new Vector();
        quanao = vector;
        vector.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        quanao.addElement(new Hashtable());
        treeInfos = new Hashtable();
        potionTemplates = new PotionTemplate[Char.CHAR_POTION];
        color = new int[]{-8431096, -924406, -2249714, -2181525, -5535422, -11918336, -14595766};
        wh = 6;
        stMoney = "xu";
        stVND = "lượng";
        r = new Random();
        nameBigMap = new String[]{"Làng Sơn Nam", "Dao Châu", "Tiên Du", "Phù Liệt", "Kỳ Bố", "Hàm Tử", "Thạch Giang", "Đông Sơn", "Tử Quan", "Trường Giang", "Lộc Trĩ", "Sơn Lâm", "Hang động", "Hang mãng xà", "Hang thằn lằn", "Đấu trường", "Khu vực 1"};
        nameClazz = new String[]{"Dòng Charmander", "Dòng Bulbasaur", "Dòng Eevee", "Dòng Squirtle", "Dòng Pichu", ""};
    }

    public static String getDefaultChat(int i) {
        try {
            String[] strArr = NPC_DEFAULT_CHAT[i];
            return strArr[GameScr.abs(rd.nextInt(strArr.length))];
        } catch (Exception unused) {
            return "";
        }
    }

    public static void createTab(Graphics graphics) {
        int i = (GCanvas.w / 2) + 1;
        for (int i2 = 0; i2 < i; i2++) {
            graphics.drawImage(imgKc, i2 * 2, GCanvas.h + 2, Graphics.LEFT | Graphics.BOTTOM);
        }
    }

    public static void setEffSh(byte[] bArr) {
        effSh = bArr;
        FilePack.initByArray(bArr);
        imgExplosion = FilePack.getImg("explosion");
        FilePack.reset();
    }

    public static void init() {
        try {
            FilePack.init("/main.sh");
            imgNo = new FrameImage(FilePack.getImg("no"), 10, 10);
            imgKham = FilePack.getImg("kham");
            imgWaitIcon = FilePack.getImg("wicon");
            imgBar = FilePack.getImg("bar");
            imgKc = FilePack.getImg("kc");
            PopupName.imgArrow = FrameImage.init("arF", 11, 9);
            imgInv = new Bitmap[3];
            for (int i = 0; i < 3; i++) {
                imgInv[i] = FilePack.getImg("inv" + i);
                Bitmap[] bitmapArr = imgInv;
                if (bitmapArr[i] == null) {
                    bitmapArr[i] = FilePack.getImg("inv" + i);
                }
            }
            imgPanel = FilePack.getImg("panel");
            wKhung = 20;
            imgKhung = FilePack.getImg("sk31");
            imgDie = FilePack.getImg("die");
            imgEye = FilePack.getImg("eye");
            imgPlus = FilePack.getImg("plus");
            imgShadow = FilePack.getImg("shadow");
            imgGrid = FilePack.getImg("grid");
            imgInfoWindow = new Bitmap[2];
            int i2 = 0;
            while (true) {
                Bitmap[] bitmapArr2 = imgInfoWindow;
                if (i2 < bitmapArr2.length) {
                    bitmapArr2[i2] = FilePack.getImg("inv_" + i2);
                    i2++;
                } else {
                    ChatPopup.getImg();
                    imgSmoke = new FrameImage(FilePack.getImg("smoke"), 14, 15);
                    imgCheck = new FrameImage(FilePack.getImg("check"), 10, 10);
                    FilePack.reset();
                    FontSys.iniFontsyn();
                    Font font = new Font(0, null);
                    FontBitMap.init();
                    font.input();
                    FontSys.init();
                    FilePack.init("/eff.sh");
                    imgExplosion = FilePack.getImg("explosion");
                    FilePack.reset();
                    FilePack.init("/g.sh");
                    imgGround[0] = FilePack.getImg("g0");
                    imgGround[1] = FilePack.getImg("g1");
                    imgGround[2] = FilePack.getImg("g2");
                    FilePack.reset();
                    FilePack.init("/other");
                    imgBgPopup = FilePack.getImg("bg_popup");
                    imgBGTiemNang = FilePack.getImg("inv_1");
                    imgBgTrangBi = FilePack.getImg("inv_0");
                    imgPopupNgoc = FilePack.getImg("inv1");
                    imgPopupGoc = FilePack.getImg("inv0");
                    imgIconSkillKiem = FilePack.getImg("icon_sk_kiem");
                    imgIconSkillDao = FilePack.getImg("icon_sk_dao");
                    imgIconSkillPs = FilePack.getImg("icon_sk_ps");
                    imgIconSkillBua = FilePack.getImg("icon_sk_bua");
                    imgIconSkillCung = FilePack.getImg("icon_sk_cung");
                    imgButtonBar = FilePack.getImg("button_bar");
                    GameScr.imgBtn1 = FilePack.getImg("button1");
                    GameScr.imgBtn2 = FilePack.getImg("button2");
                    GameScr.imgBtn5 = FilePack.getImg("button5");
                    GameScr.imgChat = FilePack.getImg("chat");
                    GameScr.imgMapMini = FilePack.getImg("map_mini");
                    GameScr.imgSwapQuickSlot = FilePack.getImg("swap");
                    GameScr.imgFocusActor = FilePack.getImg("focus_actor");
                    imgMSG = FilePack.getImg("msg0");
                    QuickSlot.getImg();
                    FilePack.reset();
                    FilePack.init("/g.sh");
                    imgGround[0] = FilePack.getImg("g0");
                    imgGround[1] = FilePack.getImg("g1");
                    imgGround[2] = FilePack.getImg("g2");
                    FilePack.reset();
                    FilePack.init("/nation");
                    imgNation = FilePack.getImg("icon");
                    FilePack.reset();
                    imgSD = Image.loadImageFromAsset("sd.png");
                    FilePack.init("/items");
                    MenuSelectItem.imgIChat = FilePack.getImg("chat");
                    MenuSelectItem.imgIChange = FilePack.getImg("change");
                    MenuSelectItem.imgINapxu = FilePack.getImg("napxu");
                    MenuSelectItem.imgIDosat = FilePack.getImg("dosat");
                    MenuSelectItem.imgIXuongngua = FilePack.getImg("xuongngua");
                    MenuSelectItem.imgIDangxuat = FilePack.getImg("dangxuat");
                    MenuSelectItem.imgIThoat = FilePack.getImg("exit");
                    FilePack.reset();
                    MoQuaScr.imgBox = Image.loadImageFromAsset("box.png");
                    imgBtnBarFocus = Image.loadImageFromAsset("button_bar_focus.png");
                    MenuSelectItem.imgAuto = Image.loadImageFromAsset("icon_automode.png");
                    MenuSelectItem.imgDoParty = Image.loadImageFromAsset("fastmenu.png");
                    GameScr.imgMenu = new Bitmap[2];
                    GameScr.imgMenu[0] = Image.createImageAsset("/bt_menu0.png");
                    GameScr.imgMenu[1] = Image.createImageAsset("/bt_menu1.png");
                    GameScr.imgChatIcon = new Bitmap[2];
                    GameScr.imgChatIcon[0] = Image.createImageAsset("icon_Chat0.png");
                    GameScr.imgChatIcon[1] = Image.createImageAsset("icon_Chat1.png");
                    GameScr.img12plus = Image.createImageAsset("plus12.png");
                    GameScr.petShadow = Image.createImageAsset("shadow.png");
                    GameScr.imghealth = new Bitmap[2];
                    GameScr.imghealth[0] = Image.createImageAsset("/m1.png");
                    GameScr.imghealth[1] = Image.createImageAsset("/m2.png");
                    imgEffNew = Image.createImageAsset("/sword skill/h0.png");
                    return;
                }
            }
        } catch (Exception unused) {
        }
    }

    public static void loadPart2() {
        if (imgSelect == null) {
            GameScr.imgInfo = Image.loadImageFromAsset("info2.png");
            GameScr.imgRoundInfo = Image.loadImageFromAsset("round_info.png");
            FilePack.init("/main.sh");
            imgSelect = FilePack.getImg("select");
            imgSelect1 = FilePack.getImg("select1");
            imgSelect2 = FilePack.getImg("select3");
            GameScr.imgCong = new Bitmap[2];
            int i = 0;
            while (i < 2) {
                int i2 = i + 1;
                GameScr.imgCong[i] = FilePack.getImg("cong" + i2);
                i = i2;
            }
            if (GameScr.imgHpMonster == null) {
                GameScr.imgHpMonster = FilePack.getImg("mauquai");
            }
            MagicBeam.imgFire = FilePack.getImg("fire");
            Char.imgFlgPk = FilePack.getImg("flg");
            Char.imgLight = FilePack.getImg("light");
            imgSoft = new Bitmap[2];
            for (int i3 = 0; i3 < 2; i3++) {
                imgSoft[i3] = FilePack.getImg("soft" + i3);
            }
            imgNguHanh = FilePack.getImg("nguhanh");
            if (imgLeg == null) {
                imgLeg = new Bitmap[2];
            }
            for (int i4 = 0; i4 < 2; i4++) {
                imgLeg[i4] = FilePack.getImg("ch" + i4);
            }
            FilePack.reset();
        }
    }

    public static int random(int i, int i2) {
        return i + r.nextInt(i2 - i);
    }

    public static int random(int i) {
        return r.nextInt(i);
    }

    public static GemTemp getGemByID(short s) {
        int size = gemTemplate.size();
        for (int i = 0; i < size; i++) {
            GemTemp gemTemp = (GemTemp) gemTemplate.elementAt(i);
            if (gemTemp.id == s) {
                return gemTemp;
            }
        }
        return null;
    }

    public static int[] getAttributeItem(int i, int i2, int i3) {
        int i4;
        int[] iArr = new int[10];
        ItemTemplate itemTemplate = ((ItemTemplate[]) itemTemplates.elementAt(0))[i2];
        iArr[0] = itemTemplate.attb[0];
        int i5 = 1;
        while (true) {
            if (i5 >= 4) {
                break;
            }
            iArr[i5] = (short) (itemTemplate.attb[i5] + (ItemInInventory.isAnimalArmor(i3) ? 0 : (itemTemplate.attb[i5] * percent[i]) / 100));
            i5++;
        }
        for (i4 = 4; i4 < 10; i4++) {
            iArr[i4] = itemTemplate.attb[i4];
        }
        return iArr;
    }

    public static ItemTemplate getItem(int i, int i2) {
        ItemTemplate[] itemTemplateArr = (ItemTemplate[]) itemTemplates.elementAt(0);
        for (int i3 = 0; i3 < itemTemplateArr.length; i3++) {
            ItemTemplate itemTemplate = itemTemplateArr[i3];
            if (itemTemplate != null && itemTemplate.id == i2) {
                return itemTemplateArr[i3];
            }
        }
        return null;
    }

    public static GemTemp getShopByID(short s) {
        int size = shopTemplate.size();
        for (int i = 0; i < size; i++) {
            GemTemp gemTemp = (GemTemp) shopTemplate.elementAt(i);
            if (gemTemp.id == s) {
                return gemTemp;
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

    public static Bitmap createImgByHeader(byte[] bArr, byte[] bArr2) {
        int length = bArr.length + bArr2.length;
        byte[] bArr3 = new byte[length];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return BitmapFactory.decodeByteArray(bArr3, 0, length);
    }

    public static void removeWeaPone() {
        for (int i = 0; i < Char.imgWeapone.length; i++) {
            for (int i2 = 0; i2 < Char.imgWeapone[i].length; i2++) {
                for (int i3 = 0; i3 < Char.imgWeapone[i][i2].length; i3++) {
                    Char.imgWeapone[i][i2][i3] = null;
                }
            }
        }
        for (int i4 = 0; i4 < 5; i4++) {
            for (int i5 = 0; i5 < 3; i5++) {
                wpSplashInfos[i4][i5] = null;
            }
        }
    }

    public static WeaponInfo loadImgWeaPone(int i, int i2, int i3) {
        if (Char.imgWeapone[i][i2][i3] == null) {
            load.loadImgWeaPone(i, i2, i3);
        }
        return Char.imgWeapone[i][i2][i3];
    }

    public static WPSplashInfo loadWpsPlash(int i, int i2) {
        if (wpSplashInfos[i][i2] == null) {
            load.loadWpsPlash(i, i2);
        }
        return wpSplashInfos[i][i2];
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

        public void loadMap(int i, IAction iAction, byte[] bArr) {
            this.indexMap = i;
            this.iaLoadMap = iAction;
            this.byteMap = bArr;
            Tilemap.reset();
            System.out.println("trung oi-----------------------------");
        }

        public void loadWpsPlash(int i, int i2) {
            if (this.iWpsPlash == -1 && this.jWpsPlash == -1) {
                this.iWpsPlash = i;
                this.jWpsPlash = i2;
            }
        }

        public void loadImgWeaPone(int i, int i2, int i3) {
            if (this.iImgWeaPone == -1 && this.jImgWeaPone == -1) {
                this.iImgWeaPone = i;
                this.jImgWeaPone = i2;
                this.indexWeapone = i3;
            }
        }

        public void loadImgEffect(int i) {
            if (this.indexImgEffect == -1) {
                this.indexImgEffect = i;
            }
        }

        @Override
        public void run() {
            int i;
            int i2;
            while (true) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int i3 = this.iWpsPlash;
                if (i3 != -1 && (i2 = this.jWpsPlash) != -1) {
                    Res.GetWPSplashInfo(i3, i2);
                    this.iWpsPlash = -1;
                    this.jWpsPlash = -1;
                }
                int i4 = this.iImgWeaPone;
                if (i4 != -1 && (i = this.jImgWeaPone) != -1) {
                    Res.getImgWeaPone(i4, i, this.indexWeapone);
                    this.iImgWeaPone = -1;
                    this.jImgWeaPone = -1;
                }
                int i5 = this.indexImgEffect;
                if (i5 != -1) {
                    try {
                        Res.getImgEffect(i5);
                    } catch (Exception unused) {
                    }
                    this.indexImgEffect = -1;
                }
                int i6 = this.indexMap;
                if (i6 != -1) {
                    this.indexMap = -1;
                    Tilemap.loadMap(i6, this.byteMap);
                    this.iaLoadMap.perform();
                    this.byteMap = null;
                }
                int i7 = this.indexArrow;
                if (i7 != -1) {
                    Res.loadImgArrow(i7);
                    this.indexArrow = -1;
                }
                if (this.isInitGame) {
                    GameMidlet.instance.load();
                    this.isInitGame = false;
                }
            }
        }
    }

    public static Bitmap getImgeffect(int i) {
        return imgEffect[i];
    }

    public static Bitmap getImgArrow(int i) {
        if (imgArrow[i] == null && load.indexArrow == -1) {
            load.indexArrow = i;
        }
        return imgArrow[i];
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
                imgArrow[10] = Image.createImageAsset("/newEf/46.png");
            } else if (i == 11) {
                imgArrow[11] = Image.createImageAsset("/newEf/47.png");
            } else if (i == 12) {
                imgArrow[12] = Image.createImageAsset("/newEf/48.png");
            } else if (i == 13) {
                imgArrow[i] = Image.createImageAsset("/newEf/54.png");
            } else if (i == 14) {
                imgArrow[i] = Image.createImageAsset("/newEf/56.png");
            } else if (i == 15) {
                imgArrow[i] = Image.createImageAsset("/newEf/62.png");
            } else if (i == 16) {
                imgArrow[i] = Image.createImageAsset("/newEf/65.png");
            } else if (i == 17) {
                imgArrow[i] = Image.createImageAsset("/newEf/68.png");
            } else if (i == 18) {
                imgArrow[i] = Image.createImageAsset("/newEf/69.png");
            } else if (i == 19) {
                imgArrow[i] = Image.createImageAsset("/newEf/70.png");
            } else {
                imgArrow[i] = FilePack.getImg("arrow" + i);
            }
        } catch (Exception unused) {
        }
        FilePack.reset();
    }

    public static void paintImgEff(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        Bitmap bitmap = imgEffect[i];
        if (bitmap == null) {
            load.loadImgEffect(i);
        } else {
            graphics.drawRegion(bitmap, i2, i3, i4, i5, 0, i6, i7, i8);
        }
    }

    public static Bitmap loadImgEff(int i) {
        if (imgEffect[i] == null) {
            load.loadImgEffect(i);
        }
        return imgEffect[i];
    }

    public static void getImgEffect(int i) {
        FilePack.init("/eff.sh");
        if (i < 25 || i > 33) {
            try {
                imgEffect[i] = FilePack.getImg("g" + i);
            } catch (Exception unused) {
                Cout.println("Loi effect");
            }
        }
        Bitmap[] bitmapArr = imgEffect;
        if (bitmapArr[26] == null) {
            bitmapArr[25] = FilePack.getImg("g25");
            imgEffect[26] = FilePack.getImg("g26");
            Bitmap[] bitmapArr2 = imgEffect;
            bitmapArr2[27] = Bitmap.createBitmap(bitmapArr2[25]);
            imgEffect[28] = FilePack.getImg("g27");
            imgEffect[29] = FilePack.getImg("g28");
            imgEffect[30] = FilePack.getImg("g29");
            imgEffect[31] = FilePack.getImg("g30");
            imgEffect[32] = FilePack.getImg("g31");
            imgEffect[33] = FilePack.getImg("g32");
            try {
                imgEffect[41] = Image.createImageAsset("/newEf/42.png");
                imgEffect[42] = Image.createImageAsset("/newEf/43.png");
                imgEffect[43] = Image.createImageAsset("/newEf/44.png");
                imgEffect[44] = Image.createImageAsset("/newEf/45.png");
                imgEffect[45] = Image.createImageAsset("/newEf/49.png");
                imgEffect[46] = Image.createImageAsset("/newEf/50.png");
                imgEffect[47] = Image.createImageAsset("/newEf/51.png");
                imgEffect[48] = Image.createImageAsset("/newEf/52.png");
                imgEffect[49] = Image.createImageAsset("/newEf/53.png");
                imgEffect[50] = Image.createImageAsset("/newEf/55.png");
                imgEffect[51] = Image.createImageAsset("/newEf/57.png");
                imgEffect[52] = Image.createImageAsset("/newEf/58.png");
                imgEffect[53] = Image.createImageAsset("/newEf/59.png");
                imgEffect[54] = Image.createImageAsset("/newEf/60.png");
                imgEffect[55] = Image.createImageAsset("/newEf/61.png");
                imgEffect[56] = Image.createImageAsset("/newEf/63.png");
                Bitmap bitmap = imgArrow[15];
                if (bitmap != null) {
                    imgEffect[57] = bitmap;
                } else {
                    imgEffect[57] = Image.createImageAsset("/newEf/62.png");
                }
                imgEffect[58] = Image.createImageAsset("/newEf/64.png");
                imgEffect[59] = Image.createImageAsset("/newEf/66.png");
                imgEffect[60] = Image.createImageAsset("/newEf/67.png");
                imgEffect[61] = Image.createImageAsset("/newEf/71.png");
                imgEffect[62] = Image.createImageAsset("/newEf/72.png");
            } catch (Exception unused2) {
            }
            backSmoke = new FrameImage(imgEffect[32], 12, 13);
        }
        FilePack.reset();
    }

    public static void getImgWeaPone(int i, int i2, int i3) {
        try {
            FilePack.init("/wpsplash_" + new String[]{"kiem", "daidao", "phapsu", "bua", "cung"}[i] + "_" + i2);
            Char.imgWeapone[i][i2][i3] = new WeaponInfo();
            try {
                Char.imgWeapone[i][i2][i3].img = createImgByHeader(FilePack.instance.loadFile(i3 + "_h"), FilePack.instance.loadFile("data"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            FilePack.reset();
        } catch (Exception unused) {
        }
    }

    public static WPSplashInfo GetWPSplashInfo(int i, int i2) {
        int i3;
        InputStream inputStreamLoadData;
        try {
            wpSplashInfos[i][i2] = new WPSplashInfo();
            if (i == 3 || i == 4) {
                i3 = 0;
            } else {
                i3 = 1;
                if (i == 2 && i2 == 1) {
                    i3 = 0;
                } else if ((i != 0 && i != 1) || i2 != 2) {
                    i3 = i2;
                }
            }
            try {
                FilePack.init("/wpsplash_" + i + ".wp");
                wpSplashInfos[i][i2].image = FilePack.getImg("" + i3);
                inputStreamLoadData = FilePack.instance.loadData(i3 + ".d");
            } catch (Exception unused) {
                FilePack.init("/wpsplash_" + i + ".wp");
                wpSplashInfos[i][i2].image = FilePack.getImg("" + i3);
                inputStreamLoadData = FilePack.instance.loadData(i3 + ".d");
            }
            if (inputStreamLoadData != null) {
                for (int i4 = 0; i4 < 4; i4++) {
                    for (int i5 = 0; i5 < 8; i5++) {
                        wpSplashInfos[i][i2].P0_X[i4][i5] = inputStreamLoadData.read();
                        wpSplashInfos[i][i2].P0_Y[i4][i5] = inputStreamLoadData.read();
                        wpSplashInfos[i][i2].PF_X[i4][i5] = readSignByte(inputStreamLoadData);
                        wpSplashInfos[i][i2].PF_Y[i4][i5] = readSignByte(inputStreamLoadData);
                        wpSplashInfos[i][i2].PF_W[i4][i5] = inputStreamLoadData.read();
                        wpSplashInfos[i][i2].PF_H[i4][i5] = inputStreamLoadData.read();
                    }
                }
            }
            FilePack.reset();
        } catch (IOException unused2) {
            wpSplashInfos[i][i2] = null;
        }
        return wpSplashInfos[i][i2];
    }

    public static void removeImgEffect() {
        for (int i = 0; i < 25; i++) {
            if (i != 8) {
                imgEffect[i] = null;
            }
        }
        imgWayPoint = null;
    }

    public static void loadMonster(int i, byte[] bArr) {
        monsterTemplates[i].setInfo(i, "", "m" + i, bArr[0], bArr[1], bArr[2], bArr[3], bArr[4], bArr[5], bArr[6], bArr[7], bArr[8]);
    }

    public static void resetImgMonsTemp() {
        if (monsterTemplates == null) {
            return;
        }
        int i = 0;
        while (true) {
            MonsterTemplate[] monsterTemplateArr = monsterTemplates;
            if (i >= monsterTemplateArr.length) {
                return;
            }
            MonsterTemplate monsterTemplate = monsterTemplateArr[i];
            if (monsterTemplate != null) {
                monsterTemplate.image = null;
                monsterTemplates[i].imageObj = null;
            }
            i++;
        }
    }

    public static void removeMonsterTemplet() {
        resetImgMonsTemp();
        treeInfos.clear();
    }

    public static CharPartInfo getCharPartInfo(int i, int i2) {
        Hashtable hashtable = (Hashtable) quanao.elementAt(i);
        CharPartInfo charPartInfo = (CharPartInfo) hashtable.get(i2 + "");
        if (charPartInfo == null) {
            charPartInfo = new CharPartInfo(i, i2);
            charPartInfo.time = (int) (System.currentTimeMillis() / 1000);
            if (charPartInfo.image == null) {
                GameService.gI().getTreeImage(-1, i, i2);
            }
            hashtable.put(i2 + "", charPartInfo);
        }
        if (charPartInfo.image == null && (System.currentTimeMillis() / 1000) - ((long) charPartInfo.time) == 15) {
            GameService.gI().getTreeImage(-1, i, i2);
            charPartInfo.time = (int) (System.currentTimeMillis() / 1000);
        }
        charPartInfo.timeRemove = (int) (System.currentTimeMillis() / 1000);
        return charPartInfo;
    }

    public static void paintDefault(Graphics graphics, int i, int i2, int i3, int i4, int i5) {
        CharPartInfo charPartInfo;
        CharPartInfo charPartInfo2;
        CharPartInfo charPartInfo3;
        if (i5 == 1 && (charPartInfo3 = defaultBody) != null) {
            charPartInfo3.paint(graphics, i, i5, i3, i4);
        }
        if (i5 == 0 && (charPartInfo2 = defaultLeg) != null) {
            charPartInfo2.paint(graphics, i, i5, i3, i4);
        }
        if (i5 != 2 || (charPartInfo = defaultHead) == null) {
            return;
        }
        charPartInfo.paint(graphics, i, i5, i3, i4);
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

    public static TreeInfo getTree(int i) {
        TreeInfo treeInfo = (TreeInfo) treeInfos.get("" + i);
        if (treeInfo != null) {
            return treeInfo;
        }
        TreeInfo treeInfo2 = new TreeInfo();
        treeInfo2.id = (short) i;
        treeInfos.put("" + i, treeInfo2);
        return treeInfo2;
    }

    public static int getHeightTree(int i) {
        TreeInfo tree = getTree(i);
        if (tree != null) {
            return tree.getHeight();
        }
        return 0;
    }

    public static int getwidthTree(int i) {
        TreeInfo tree = getTree(i);
        if (tree != null) {
            return tree.getWidth();
        }
        return 0;
    }

    public static int getDxTree(int i) {
        TreeInfo tree = getTree(i);
        if (tree != null) {
            return tree.dx;
        }
        return 0;
    }

    public static int getDyTree(int i) {
        TreeInfo tree = getTree(i);
        if (tree != null) {
            return tree.dy;
        }
        return 0;
    }

    public static int readSignByte(InputStream inputStream) {
        byte[] bArr = new byte[1];
        try {
            inputStream.read(bArr, 0, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bArr[0];
    }

    public static final void loadTileImage(int i) {
        InputStream inputStreamOpen;
        if (imgTile == null) {
            try {
                if (i > 200) {
                    imgTile = Image.loadImageFromAsset("tile_t_thanh.png");
                    inputStreamOpen = GameMidlet.asset.open("t_thanh.type");
                    mauTile = 0;
                } else if (i < 110) {
                    imgTile = Image.loadImageFromAsset("tile_t.png");
                    inputStreamOpen = GameMidlet.asset.open("t.type");
                    mauTile = 1;
                } else {
                    imgTile = Image.loadImageFromAsset("tile_t_hang.png");
                    inputStreamOpen = GameMidlet.asset.open("t_hang.type");
                    mauTile = 2;
                }
                try {
                    Tilemap.typeOfTile = null;
                    Tilemap.typeOfTile = new int[inputStreamOpen.available()];
                    for (int i2 = 0; i2 < Tilemap.typeOfTile.length; i2++) {
                        Tilemap.typeOfTile[i2] = inputStreamOpen.read();
                    }
                } catch (Exception unused) {
                    Cout.println("Error Load Back Tile");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void paintDlgFull(Graphics graphics, int i, int i2) {
        int i3 = i - 8;
        int i4 = i2 - 9;
        graphics.setClip(i3, i4, 164, 184);
        graphics.ClipRec(i3, i4, 164, 184);
        for (int i5 = 0; i5 < 3; i5++) {
            for (int i6 = 0; i6 < 4; i6++) {
                graphics.drawImage(imgBgPopup, (i5 * 70) + i3, (i6 * 47) + i4, 0);
            }
        }
        graphics.restoreCanvas();
        int i7 = i - 10;
        int i8 = i2 - 11;
        graphics.setClip(i7, i8, GCanvas.w, GCanvas.h);
        graphics.ClipRec(i7, i8, GCanvas.w, GCanvas.h);
        for (int i9 = 0; i9 < 3; i9++) {
            graphics.setColor(color[i9]);
            int i10 = i3 + i9;
            int i11 = i4 + i9;
            int i12 = i9 * 2;
            int i13 = 184 - i12;
            graphics.fillRect(i10, i11, 1, i13);
            graphics.fillRect((164 - i9) + i3, i11, 1, i13);
            int i14 = 164 - i12;
            graphics.fillRect(i10, i11, i14, 1);
            graphics.fillRect(i10, (184 - i9) + i4, i14, 1);
        }
        int i15 = i - 10;
        int i16 = i2 - 11;
        graphics.drawImage(imgPopupGoc, i15, i16, 20);
        int i17 = i + 159;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 2, i17, i16, 24);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 6, i15, i2 + 179, 36);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 3, i17, i2 + 178, 40);
        graphics.restoreCanvas();
    }

    public static void paintDlgFullCreateChar(Graphics graphics, int i, int i2) {
        int i3 = i - 10;
        int i4 = i + 2;
        int i5 = i2 + 11;
        graphics.setClip(i4, i5, 274, 144);
        graphics.ClipRec(i4, i5, 274, 144);
        for (int i6 = 0; i6 < 274; i6++) {
            for (int i7 = 0; i7 < 144; i7++) {
                Bitmap bitmap = imgBgPopup;
                graphics.drawImage(bitmap, (bitmap.getWidth() * i6) + i4, (imgBgPopup.getHeight() * i7) + i5, 0);
            }
        }
        graphics.restoreCanvas();
        int i8 = i2 + 9;
        graphics.setClip(i, i8, GCanvas.w, GCanvas.h);
        graphics.ClipRec(i, i8, GCanvas.w, GCanvas.h);
        for (int i9 = 0; i9 < 3; i9++) {
            graphics.setColor(color[i9]);
            int i10 = i4 + i9;
            int i11 = i5 + i9;
            int i12 = i9 * 2;
            int i13 = 144 - i12;
            graphics.fillRect(i10, i11, 1, i13);
            graphics.fillRect(i10 + 137, i11, 1, i13);
            graphics.fillRect((274 - i9) + i4, i11, 1, i13);
            int i14 = 274 - i12;
            graphics.fillRect(i10, i11, i14, 1);
            graphics.fillRect(i10, (144 - i9) + i5, i14, 1);
        }
        int i15 = i2 + 9;
        graphics.drawImage(imgPopupGoc, i, i15, 20);
        int i16 = i + 279;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 2, i16, i15, 24);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 6, i, i2 + 159, 36);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 3, i16, i2 + 158, 40);
        graphics.restoreCanvas();
        graphics.setClip(i3, i2, GCanvas.w, GCanvas.h);
        graphics.drawImage(imgPopupNgoc, GCanvas.hw, i2 + 7, 3);
    }

    public static void paintSelected(Graphics graphics, int i, int i2, int i3, int i4) {
        graphics.setColor(-16742267);
        graphics.fillRect(i, i2, i3, i4);
        graphics.setColor(-4868426);
        graphics.drawRect(i, i2, i3, i4);
    }

    public static void paintDlgDragonFull(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5 = (i3 / 70) + 1;
        int i6 = (i4 / 47) + 1;
        graphics.setClip(i, i2, i3, i4);
        graphics.ClipRec(i, i2, i3, i4);
        for (int i7 = 0; i7 < i5; i7++) {
            for (int i8 = 0; i8 < i6; i8++) {
                graphics.drawImage(imgBgPopup, i + (i7 * 70), i2 + (i8 * 47), 0);
            }
        }
        graphics.restoreCanvas();
        graphics.setClip(0, -5, GCanvas.w, GCanvas.h);
        graphics.ClipRec(0, -5, GCanvas.w, GCanvas.h);
        for (int i9 = 0; i9 < 3; i9++) {
            graphics.setColor(color[i9]);
            int i10 = i + i9;
            int i11 = i2 + i9;
            int i12 = i9 * 2;
            int i13 = i4 - i12;
            graphics.fillRect(i10, i11, 1, i13);
            graphics.fillRect(i + (i3 - i9), i11, 1, i13);
            int i14 = i3 - i12;
            graphics.fillRect(i10, i11, i14, 1);
            graphics.fillRect(i10, i2 + (i4 - i9), i14, 1);
        }
        int i15 = i - 2;
        int i16 = i2 - 2;
        graphics.drawImage(imgPopupGoc, i15, i16, 0);
        int i17 = (i + i3) - 15;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 2, i17, i16, 0);
        int i18 = i2 + i4;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 6, i15, i18 - 15, 0);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 3, i17, i18 - 16, 0);
        graphics.drawImage(imgPopupNgoc, i + (i3 / 2), i2 - 4, 3);
        graphics.restoreCanvas();
    }

    public static void paintRectColor(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        graphics.setColor(i6);
        graphics.drawRect(i, i2, i3, i4);
        graphics.setColor(i7);
        if (i5 < i3) {
            int i8 = wh;
            if (i8 + i5 >= i3) {
                i8 = i3 - i5;
            }
            graphics.fillRect(i + i5, i2, i8, 1);
        } else if (i5 < i3 + i4) {
            int i9 = wh;
            int i10 = i5 - i3;
            if (i9 + i10 >= i4) {
                i9 = i4 - i10;
            }
            graphics.fillRect(i + i3, i2 + i10, 1, i9);
        } else {
            int i11 = i3 * 2;
            if (i5 < i11 + i4) {
                int i12 = wh;
                int i13 = (i5 - i3) - i4;
                if (i12 + i13 >= i3) {
                    i12 = i3 - i13;
                }
                graphics.fillRect((i + (i3 - i13)) - i12, i2 + i4, i12, 1);
            } else if (i5 < i3 * i4 * 2) {
                int i14 = wh;
                int i15 = (i5 - i11) - i4;
                if (i14 + i15 >= i4) {
                    i14 = i4 - i15;
                }
                graphics.fillRect(i, (i2 + (i4 - i15)) - i14, 1, i14);
            }
        }
        int i16 = num + 1;
        num = i16;
        if (i16 >= 4) {
            num = 0;
        }
    }

    public static void paintDlgFull(Graphics graphics, int i, int i2, int i3, int i4) {
        int i5 = (i3 / 70) + 1;
        graphics.setClip(i, i2, i3, i4);
        graphics.ClipRec(i, i2, i3, i4);
        for (int i6 = 0; i6 < i5; i6++) {
            graphics.drawImage(imgBgPopup, (i6 * 70) + i, i2, 0);
        }
        graphics.setColor(-16500172);
        graphics.fillRect(i, i2 + 25, i3, i4);
        graphics.restoreCanvas();
        int i7 = i - 5;
        int i8 = i2 - 10;
        graphics.setClip(i7, i8, GCanvas.w, GCanvas.h);
        graphics.ClipRec(i7, i8, GCanvas.w, GCanvas.h);
        for (int i9 = 0; i9 < 3; i9++) {
            graphics.setColor(color[i9]);
            int i10 = i + i9;
            int i11 = i2 + i9;
            int i12 = i9 * 2;
            int i13 = i4 - i12;
            graphics.fillRect(i10, i11, 1, i13);
            graphics.fillRect((i3 - i9) + i, i11, 1, i13);
            int i14 = i3 - i12;
            graphics.fillRect(i10, i11, i14, 1);
            graphics.fillRect(i10, (i4 - i9) + i2, i14, 1);
        }
        int i15 = i - 2;
        int i16 = i2 - 2;
        graphics.drawImage(imgPopupGoc, i15, i16, 0);
        int i17 = i + i3 + 3;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 2, i17, i16, Graphics.TOP | Graphics.RIGHT);
        int i18 = i2 + i4;
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 6, i15, i18 + 4, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(imgPopupGoc, 0, 0, 18, 19, 3, i17, i18 + 3, Graphics.BOTTOM | Graphics.RIGHT);
        graphics.restoreCanvas();
    }

    public static void paintDlgFull(Graphics graphics, int i, int i2, int i3, int i4, int i5, String str, boolean z, int i6) {
        int i7 = (i3 / 70) + 1;
        graphics.setClip(i, i2, i3, i4);
        graphics.ClipRec(i, i2, i3, i4);
        for (int i8 = 0; i8 < i7; i8++) {
            graphics.drawImage(imgInv[2], (i8 * 70) + i, i2, 0);
        }
        graphics.restoreCanvas();
        graphics.setColor(-16500172);
        int i9 = i2 + 25;
        graphics.fillRect(i, i9, i3, i4);
        graphics.setClip(0, 0, GCanvas.w, GCanvas.h);
        graphics.ClipRec(0, 0, GCanvas.w, GCanvas.h);
        for (int i10 = 0; i10 < 3; i10++) {
            graphics.setColor(color[i10]);
            int i11 = i10 * 2;
            graphics.drawRect(i + i10, i2 + i10, (i3 - i11) - 1, (i4 - i11) - 1);
        }
        int i12 = i + 3;
        int i13 = i3 - 6;
        graphics.fillRect(i12, i9, i13, 1);
        if (!z) {
            graphics.fillRect(i12, i9 + i5 + i6, i13, 1);
        } else {
            graphics.fillRect(i12, i9 + i5 + i6, i13, 1);
            graphics.fillRect(i12, ((i2 + i5) + i6) - 2, i13, 1);
        }
        Font.normalFont[0].drawString(graphics, str, i + (i3 / 2), i2 + 8, 2);
        int i14 = i - 2;
        int i15 = i2 - 2;
        graphics.drawImage(imgInv[0], i14, i15, 0);
        int i16 = i + i3 + 2;
        graphics.drawRegion(imgInv[0], 0, 0, 18, 19, 2, i16, i15, Graphics.TOP | Graphics.RIGHT);
        int i17 = i2 + i4 + 2;
        graphics.drawRegion(imgInv[0], 0, 0, 18, 19, 6, i14, i17, Graphics.BOTTOM | Graphics.LEFT);
        graphics.drawRegion(imgInv[0], 0, 0, 18, 19, 3, i16, i17, Graphics.BOTTOM | Graphics.RIGHT);
        graphics.restoreCanvas();
    }

    public static void drawRect(Graphics graphics, int i, int i2, int i3, int i4) {
        for (int i5 = 0; i5 < 3; i5++) {
            graphics.setColor(color[i5]);
            int i6 = i5 * 2;
            graphics.drawRect(i + i5, i2 + i5, i3 - i6, i4 - i6);
        }
    }

    public static int[] trimArray(int[] iArr) {
        Vector vector = new Vector();
        for (int i = 0; i < iArr.length; i++) {
            if (vector.size() == 0) {
                vector.addElement(iArr[i] + "");
            } else {
                int size = vector.size();
                int i2 = 0;
                while (true) {
                    if (i2 < size) {
                        if (Integer.parseInt((String) vector.elementAt(i2)) == iArr[i]) {
                            break;
                        }
                        i2++;
                    } else {
                        vector.addElement(iArr[i] + "");
                        break;
                    }
                }
            }
        }
        int[] iArr2 = new int[vector.size()];
        for (int i3 = 0; i3 < vector.size(); i3++) {
            iArr2[i3] = Integer.parseInt((String) vector.elementAt(i3));
        }
        return iArr2;
    }

    public static void paintPotion(Graphics graphics, int i, int i2, int i3, int i4) {
        GameData.paintIcon(graphics, (short) (i + 5500), i2, i3, i4);
    }

    public static void paintGem(Graphics graphics, int i, int i2, int i3) {
        GameData.paintIcon(graphics, (short) (i + 6500), i2, i3, 3);
    }

    public static void paintGemKhoa(Graphics graphics, int i, int i2, int i3) {
        GameData.paintIcon(graphics, (short) (i + 6500), i2, i3, 3);
    }

    public static Bitmap createImgByByteArray(byte[] bArr) {
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public static int rnd(int i) {
        return r.nextInt(i);
    }

    public static int rnd(int i, int i2) {
        return r.nextInt(2) == 0 ? i : i2;
    }

    public static String[] getNameMap(int i, int i2) {
        try {
            return nameMap[i][i2];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static short[] getLocation(int i, int i2) {
        try {
            return locationMap[i][i2];
        } catch (Exception unused) {
            return null;
        }
    }

    public static Bitmap getWayPoint() {
        if (imgWayPoint == null) {
            FilePack.init("/main.sh");
            imgWayPoint = FilePack.getImg("waypoint");
            FilePack.reset();
        }
        return imgWayPoint;
    }

    public static byte[] intToByteArray(int i) {
        byte[] bArr = new byte[4];
        for (int i2 = 0; i2 < 4; i2++) {
            bArr[i2] = (byte) ((i >>> ((3 - i2) * 8)) & 255);
        }
        return bArr;
    }

    public static int getDistance(int i, int i2, int i3, int i4) {
        return getDistance(i - i3, i2 - i4);
    }

    public static int sqrt(int i) {
        if (i <= 0) {
            return 0;
        }
        int i2 = (i + 1) / 2;
        while (true) {
            int i3 = (i2 / 2) + (i / (i2 * 2));
            if (Math.abs(i2 - i3) <= 1) {
                return i3;
            }
            i2 = i3;
        }
    }

    public static int getDistance(int i, int i2) {
        return sqrt((i * i) + (i2 * i2));
    }

    public static int byteToInit(byte[] bArr) {
        return (bArr[3] & 255) | ((bArr[0] & 255) << 24) | ((bArr[1] & 255) << 16) | ((bArr[2] & 255) << 8);
    }

    public static int getRange(int i, int i2, int i3, int i4) {
        return Math.abs(i - i2) + Math.abs(i3 - i4);
    }

    public static int random_Am_0(int i) {
        int iNextInt = 0;
        while (iNextInt == 0) {
            iNextInt = r.nextInt() % i;
        }
        return iNextInt;
    }
}
