using System;
using System.IO;
using UnityEngine;

public class Res_HAIRMOD_PROTECTED
{
	public const int MAX_DAM = 2000000;

	public const string xu = "xu";

	public const string luong = "lượng";

	public const int COLLECT_ITEM_RANGE = 35;

	public const string doYouWantSell = "Muốn nhờ ta bán gì nào";

	public const short MAX_MONSTER_TEMPLATE = 115;

	public const sbyte LEG = 0;

	public const sbyte BODY = 1;

	public const sbyte HEAD = 2;

	public const sbyte HAT = 3;

	public const sbyte COAT = 4;

	public const sbyte WP = 5;

	public const string doYouWantBuyIten = "Bạn có muốn mua vật phẩm này không ?";

	public const string talk = "Nói chuyện";

	public const string buy = "Bán";

	public const string shell = "Mua";

	public const string SELECT = "Chọn";

	public const string CLOSE = "Đóng";

	public static string PORTION_ITEM_NAME = string.Empty;

	public static string PORTION_ITEM_NAME_CLAN = string.Empty;

	public static string PORTION_ITEM_NAME2 = string.Empty;

	public static mVector_HAIRMOD_PROTECTED itemTemplates = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED gemTemplate = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED shopTemplate = new mVector_HAIRMOD_PROTECTED();

	public static XaphuTemplate_HAIRMOD_PROTECTED[] XAPHU;

	public static Res_HAIRMOD_PROTECTED me;

	public static int hString = 16;

	public static sbyte mauTile;

	public static LoadData_HAIRMOD_PROTECTED load;

	public static readonly string[][] NPC_DEFAULT_CHAT = new string[32][]
	{
		new string[1] { "Vào trong đi cậu. Đủ loại HP từ lớn đến bé!" },
		new string[3] { "Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe" },
		new string[3] { "Vũ khí ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Ngươi muốn sửa chữa đồ?", "Ngươi muốn luyện đồ?" },
		new string[3] { "Mua giáp hộ thân nào …", "Mua giáp của ta, ngươi sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho ngươi biết tại sao người ta gọi ta là Thiết Bì" },
		new string[1] { "Trách nhiệm của ta là bảo vệ ngôi làng này." },
		new string[3] { "Cố gắng lên con, hãy hoàn thành các nhiệm vụ đi nhé", "Anh hùng xuất thiếu niên..anh hùng xuất thiếu niên", "Con là thành viên của làng Nghĩa Sĩ này ! Hãy nhớ lấy điều đó" },
		new string[3] { "Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Ngươi muốn mua vật phẩm để luyện đồ?" },
		new string[3] { "Đường xa vạn lý, không làm nản lòng xa phu ta đây", "Đi lâu thành lối, lối thành đường đi.", "Nói ta nghe ngươi muốn đi đâu?" },
		new string[1] { string.Empty },
		new string[3] { "Gửi đồ chỗ ta là an toàn nhất đó", "Ngươi muốn nhờ ta giữ đồ?", "Ngươi có nhiều đồ muốn gửi àh?" },
		new string[1] { "Bạn phải có vé mới được lên tàu." },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[1] { "Muốn nhờ ta bán gì nào" },
		new string[3] { "Tập luyện nào, tập luyện nào ..", "Sức khỏe trước đã, tập luyện nào …", "Cho ta xem khả năng của các ngươi nào !!" },
		new string[3] { "Giao tiền ta giữ nào, yên tâm nhé", "Ngươi muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào" },
		new string[1] { "Chà, lâu rồi mới có người đi qua." },
		new string[1] { "Ngươi muốn thuê gì?" },
		new string[1] { "Hãy quản lý bang của ngươi thật tốt vào." },
		new string[3] { "Hò ơ… mua gì cũng có, hỏi gì cũng biết", "Gánh hàng nhỏ nhưng món gì cũng có đây", "Mua gì đây, nói ta nghe" },
		new string[3] { "Mua giáp hộ thân nào …", "Mua giáp của ta, ngươi sẽ luôn được bảo vệ an toàn", "Đến với ta, ta sẽ nói cho ngươi biết tại sao người ta gọi ta là Giáp Sư" },
		new string[3] { "Vũ khí ở đây rất lợi hại, vào trong tôi cho cậu xem!", "Ngươi muốn sửa chữa đồ?", "Ngươi muốn luyện đồ?" },
		new string[3] { "Chào chàng trai trẻ", "Muốn mua ngọc thì vào nhà", "Ngươi muốn mua vật phẩm để luyện đồ?" },
		new string[3] { "Gửi đồ chỗ ta là an toàn nhất đó", "Ngươi muốn nhờ ta giữ đồ?", "Ngươi có nhiều đồ muốn gửi àh?" },
		new string[3] { "Giao tiền ta giữ nào, yên tâm nhé", "Ngươi muốn đổi tiền à, nói ta nghe", "Ta lấy chữ Tín làm đầu, yên tâm nào" }
	};

	public static string[] newNameAtbItem = new string[9]
	{
		"Tăng Hp ",
		"Tăng Mp ",
		"Sức mạnh +",
		"Nhanh nhẹn +",
		"Tinh thần +",
		"Sức khoẻ +",
		string.Empty,
		"Chí mạng tăng ",
		"Tăng st chí mạng "
	};

	public static string[] nameAtbItemLock = new string[3] { "Tăng công ", "Tăng thủ ma ", "Tăng thủ vật " };

	private static MyRandom_HAIRMOD_PROTECTED rd = new MyRandom_HAIRMOD_PROTECTED();

	public static readonly string[] GENDERNAME = new string[3] { "bất kỳ", "nam", "nữ" };

	public static Image_HAIRMOD_PROTECTED[] imgSoft;

	public static Image_HAIRMOD_PROTECTED imgPlus;

	public static Image_HAIRMOD_PROTECTED imgShadow;

	public static Image_HAIRMOD_PROTECTED imgSelect;

	public static Image_HAIRMOD_PROTECTED imgSelect1;

	public static Image_HAIRMOD_PROTECTED imgSelect2;

	public static Image_HAIRMOD_PROTECTED imgPanel;

	public static Image_HAIRMOD_PROTECTED imgShadowPet;

	public static Image_HAIRMOD_PROTECTED imgSelectFc;

	public static Image_HAIRMOD_PROTECTED[] imgEffect = new Image_HAIRMOD_PROTECTED[63];

	public static Image_HAIRMOD_PROTECTED imgExplosion;

	public static Image_HAIRMOD_PROTECTED imgGrid;

	public static Image_HAIRMOD_PROTECTED imgDie;

	public static Image_HAIRMOD_PROTECTED imgEye;

	public static Image_HAIRMOD_PROTECTED imgEffNew;

	public static Image_HAIRMOD_PROTECTED[] imgGround = new Image_HAIRMOD_PROTECTED[3];

	public static Image_HAIRMOD_PROTECTED[] imgArrow = new Image_HAIRMOD_PROTECTED[20];

	public static Image_HAIRMOD_PROTECTED[] imgInfoWindow;

	public static Image_HAIRMOD_PROTECTED[] imgNewBt = new Image_HAIRMOD_PROTECTED[5];

	public static Image_HAIRMOD_PROTECTED imgWaitIcon;

	public static Image_HAIRMOD_PROTECTED imgNguHanh;

	public static Image_HAIRMOD_PROTECTED[] imgLeg;

	public static Image_HAIRMOD_PROTECTED[] imgInv;

	public static Image_HAIRMOD_PROTECTED[] imgBox = new Image_HAIRMOD_PROTECTED[2];

	public static Image_HAIRMOD_PROTECTED[] imgCell = new Image_HAIRMOD_PROTECTED[3];

	public static Image_HAIRMOD_PROTECTED imgBar;

	public static Image_HAIRMOD_PROTECTED imgCmdBar;

	public static Image_HAIRMOD_PROTECTED imgMSG;

	public static Image_HAIRMOD_PROTECTED imgInfoWindowt;

	public static FrameImage_HAIRMOD_PROTECTED imgSmoke;

	public static FrameImage_HAIRMOD_PROTECTED backSmoke;

	public static FrameImage_HAIRMOD_PROTECTED imgNo;

	public static Image_HAIRMOD_PROTECTED imgtileSmall;

	public static Image_HAIRMOD_PROTECTED imgWayPoint;

	public static Image_HAIRMOD_PROTECTED imgKham;

	public static Image_HAIRMOD_PROTECTED imgKc;

	public static Image_HAIRMOD_PROTECTED imgKhung;

	public static Image_HAIRMOD_PROTECTED icon_automode;

	public static Image_HAIRMOD_PROTECTED imgNation;

	public static Image_HAIRMOD_PROTECTED imgBgPopup;

	public static Image_HAIRMOD_PROTECTED imgBGTiemNang;

	public static Image_HAIRMOD_PROTECTED imgBgTrangBi;

	public static Image_HAIRMOD_PROTECTED imgPopupNgoc;

	public static Image_HAIRMOD_PROTECTED imgPopupGoc;

	public static Image_HAIRMOD_PROTECTED imgIconSkillKiem;

	public static Image_HAIRMOD_PROTECTED imgIconSkillDao;

	public static Image_HAIRMOD_PROTECTED imgIconSkillPs;

	public static Image_HAIRMOD_PROTECTED imgIconSkillBua;

	public static Image_HAIRMOD_PROTECTED imgIconSkillCung;

	public static Image_HAIRMOD_PROTECTED imgButtonBar;

	public static Image_HAIRMOD_PROTECTED imgChat;

	public static Image_HAIRMOD_PROTECTED imgMapMini;

	public static Image_HAIRMOD_PROTECTED imgSwapQuickSlot;

	public static Image_HAIRMOD_PROTECTED imgFocusActor;

	public static Image_HAIRMOD_PROTECTED imgChatTouch;

	public static Image_HAIRMOD_PROTECTED imgSwapClothes;

	public static Image_HAIRMOD_PROTECTED imgTG;

	public static Image_HAIRMOD_PROTECTED imgIce;

	public static Image_HAIRMOD_PROTECTED imgStone;

	public static Image_HAIRMOD_PROTECTED imgInv3;

	public static Image_HAIRMOD_PROTECTED imgButtonBar1;

	public static Image_HAIRMOD_PROTECTED imgCheck;

	public static Image_HAIRMOD_PROTECTED imgK0;

	public static int wKhung;

	public static int wTrangbi;

	public static int wimgBgPopup;

	public static mVector_HAIRMOD_PROTECTED allImageMain = new mVector_HAIRMOD_PROTECTED();

	public static sbyte[] effSh;

	public static mVector_HAIRMOD_PROTECTED quanao = new mVector_HAIRMOD_PROTECTED();

	public static WPSplashInfo_HAIRMOD_PROTECTED[][] wpSplashInfos = new WPSplashInfo_HAIRMOD_PROTECTED[5][];

	public static sbyte[] percent = new sbyte[5] { 20, 50, 10, 70, 20 };

	public static MyHashTable_HAIRMOD_PROTECTED allMonterData = new MyHashTable_HAIRMOD_PROTECTED();

	public static MonsterTemplate_HAIRMOD_PROTECTED[] monsterTemplates = new MonsterTemplate_HAIRMOD_PROTECTED[115];

	private bool isLocal;

	public static CharPartInfo_HAIRMOD_PROTECTED defaultBody;

	public static CharPartInfo_HAIRMOD_PROTECTED defaultLeg;

	public static CharPartInfo_HAIRMOD_PROTECTED defaultHead;

	public static MyHashTable_HAIRMOD_PROTECTED treeInfos = new MyHashTable_HAIRMOD_PROTECTED();

	public static PotionTemplate_HAIRMOD_PROTECTED[] potionTemplates = new PotionTemplate_HAIRMOD_PROTECTED[Char_HAIRMOD_PROTECTED.CHAR_POTION];

	public static int[] color = new int[7] { 8346120, 15852810, 14527502, 14595691, 11241794, 4858880, 2181450 };

	private static int wh = 6;

	private static int num;

	public static string stMoney = "xu";

	public static string stVND = "lượng";

	private static MyRandom_HAIRMOD_PROTECTED r = new MyRandom_HAIRMOD_PROTECTED();

	public static short[][] localXaphu;

	public static string[][] namMap;

	public static string[][][] nameMap;

	public static short[][][] locationMap;

	public static string[] khamProperty;

	public static sbyte[] idNgocKham;

	public static readonly string[] nameBigMap = new string[17]
	{
		"Làng Sơn Nam", "Giao Châu", "Tiên Du", "Phù Liệt", "Kỳ Bố", "Hàm Tử", "Thạch Giang", "Đông Sơn", "Tử Quan", "Trường Giang",
		"Lộc Trĩ", "Sơn Lâm", "Hang động", "Hang mãng xà", "Hang thằn lằn", "Đấu trường", "Khu vực 1"
	};

	public static readonly string[] nameClazz = new string[6]
	{
		"Kiếm khách",
		"Chiến binh",
		"Pháp sư",
		"Đấu sĩ",
		"Cung thủ",
		string.Empty
	};

	public static Res_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new Res_HAIRMOD_PROTECTED());
	}

	public static bool isDestroy(int x1, int xw1, int x2, int xw2, int y1, int yh1, int y2, int yh2)
	{
		if (x1 > xw2 || xw1 < x2 || y1 > yh2 || yh1 < y2)
		{
			return false;
		}
		return true;
	}

	public static int abs(int i)
	{
		return (i <= 0) ? (-i) : i;
	}

	public static int random(int a)
	{
		return r.nextInt(a);
	}

	public static int random_Am_0(int a)
	{
		int num;
		for (num = 0; num == 0; num = r.nextInt() % a)
		{
		}
		return num;
	}

	public static string getDefaultChat(int type)
	{
		string result = string.Empty;
		try
		{
			result = NPC_DEFAULT_CHAT[type][GameScr_HAIRMOD_PROTECTED.abs(rd.nextInt(NPC_DEFAULT_CHAT[type].Length))];
		}
		catch (Exception)
		{
		}
		return result;
	}

	public static void loadRes()
	{
		for (int i = 0; i < 5; i++)
		{
			Char_HAIRMOD_PROTECTED.imgWeapone[i] = new WeaponInfo_HAIRMOD_PROTECTED[15][];
			for (int j = 0; j < Char_HAIRMOD_PROTECTED.imgWeapone[i].Length; j++)
			{
				Char_HAIRMOD_PROTECTED.imgWeapone[i][j] = new WeaponInfo_HAIRMOD_PROTECTED[7];
			}
		}
		for (int k = 0; k < 5; k++)
		{
			wpSplashInfos[k] = new WPSplashInfo_HAIRMOD_PROTECTED[3];
		}
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.main);
		imgNo = new FrameImage_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.getImg("no"), 10, 10);
		imgKham = FilePack_HAIRMOD_PROTECTED.getImg("kham");
		imgMSG = FilePack_HAIRMOD_PROTECTED.getImg("msg0");
		imgWaitIcon = FilePack_HAIRMOD_PROTECTED.getImg("wicon");
		imgBar = FilePack_HAIRMOD_PROTECTED.getImg("bar");
		imgKc = FilePack_HAIRMOD_PROTECTED.getImg("kc");
		PopupName_HAIRMOD_PROTECTED.imgArrow = FrameImage_HAIRMOD_PROTECTED.init("arF", 11, 9);
		imgFocusActor = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/fc");
		imgChatTouch = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/imgChat");
		imgSelectFc = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/selectfc");
		imgInv = new Image_HAIRMOD_PROTECTED[3];
		for (int l = 0; l < 3; l++)
		{
			imgInv[l] = FilePack_HAIRMOD_PROTECTED.getImg("inv" + l);
		}
		imgPanel = FilePack_HAIRMOD_PROTECTED.getImg("panel150x36");
		wKhung = 30;
		imgKhung = FilePack_HAIRMOD_PROTECTED.getImg("sk31");
		imgDie = FilePack_HAIRMOD_PROTECTED.getImg("die");
		imgEye = FilePack_HAIRMOD_PROTECTED.getImg("eye");
		imgPlus = FilePack_HAIRMOD_PROTECTED.getImg("plus");
		imgShadow = FilePack_HAIRMOD_PROTECTED.getImg("shadow");
		imgGrid = FilePack_HAIRMOD_PROTECTED.getImg("grid");
		ChatPopup_HAIRMOD_PROTECTED.getImg();
		imgSmoke = new FrameImage_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.getImg("smoke"), 14, 15);
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.eff);
		imgExplosion = FilePack_HAIRMOD_PROTECTED.getImg("explosion");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.ground);
		imgGround[0] = FilePack_HAIRMOD_PROTECTED.getImg("g0");
		imgGround[1] = FilePack_HAIRMOD_PROTECTED.getImg("g1");
		imgGround[2] = FilePack_HAIRMOD_PROTECTED.getImg("g2");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.ground);
		imgGround[0] = FilePack_HAIRMOD_PROTECTED.getImg("g0");
		imgGround[1] = FilePack_HAIRMOD_PROTECTED.getImg("g1");
		imgGround[2] = FilePack_HAIRMOD_PROTECTED.getImg("g2");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.nation);
		imgNation = FilePack_HAIRMOD_PROTECTED.getImg("icon");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(Main_HAIRMOD_PROTECTED.res + "/other");
		imgBgPopup = FilePack_HAIRMOD_PROTECTED.getImg("bg_popup");
		wimgBgPopup = imgBgPopup.getWidth();
		imgBGTiemNang = FilePack_HAIRMOD_PROTECTED.getImg("inv_1");
		imgBgTrangBi = FilePack_HAIRMOD_PROTECTED.getImg("inv_0");
		imgPopupNgoc = FilePack_HAIRMOD_PROTECTED.getImg("inv1");
		imgPopupGoc = FilePack_HAIRMOD_PROTECTED.getImg("inv0");
		imgIconSkillKiem = FilePack_HAIRMOD_PROTECTED.getImg("icon_sk_kiem");
		imgIconSkillDao = FilePack_HAIRMOD_PROTECTED.getImg("icon_sk_dao");
		imgIconSkillPs = FilePack_HAIRMOD_PROTECTED.getImg("icon_sk_ps");
		imgIconSkillBua = FilePack_HAIRMOD_PROTECTED.getImg("icon_sk_bua");
		imgIconSkillCung = FilePack_HAIRMOD_PROTECTED.getImg("icon_sk_cung");
		imgChat = FilePack_HAIRMOD_PROTECTED.getImg("chat");
		imgMapMini = FilePack_HAIRMOD_PROTECTED.getImg("map_mini");
		imgSwapQuickSlot = FilePack_HAIRMOD_PROTECTED.getImg("swap");
		imgMSG = FilePack_HAIRMOD_PROTECTED.getImg("msg0");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(Main_HAIRMOD_PROTECTED.res + "/items");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIChat = FilePack_HAIRMOD_PROTECTED.getImg("chat");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIChange = FilePack_HAIRMOD_PROTECTED.getImg("change");
		MenuSelectItem_HAIRMOD_PROTECTED.imgINapxu = FilePack_HAIRMOD_PROTECTED.getImg("napxu");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIDosat = FilePack_HAIRMOD_PROTECTED.getImg("dosat");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIXuongngua = FilePack_HAIRMOD_PROTECTED.getImg("xuongngua");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIDangxuat = FilePack_HAIRMOD_PROTECTED.getImg("dangxuat");
		MenuSelectItem_HAIRMOD_PROTECTED.imgIThoat = FilePack_HAIRMOD_PROTECTED.getImg("exit");
		FilePack_HAIRMOD_PROTECTED.reset();
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.box);
		imgBox[0] = FilePack_HAIRMOD_PROTECTED.getImg("b0");
		imgBox[1] = FilePack_HAIRMOD_PROTECTED.getImg("b1");
		FilePack_HAIRMOD_PROTECTED.reset();
		try
		{
			Boss_Dragon_HAIRMOD_PROTECTED.loadDragon();
			Skill_Kiem_Down_HAIRMOD_PROTECTED.load();
			if (imgShadowPet == null)
			{
				imgShadowPet = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/m/imgshadow");
			}
			imgSwapClothes = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/swapclothes");
			imgTG = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/tg");
			if (imgIce == null)
			{
				imgIce = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/skill_ice");
			}
			if (imgStone == null)
			{
				imgStone = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/skill_da");
			}
			if (imgButtonBar == null)
			{
				imgButtonBar = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/bar");
			}
			if (imgButtonBar1 == null)
			{
				imgButtonBar1 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/bar1");
			}
			if (imgInv3 == null)
			{
				imgInv3 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/inv3");
			}
			imgInfoWindow = new Image_HAIRMOD_PROTECTED[2];
			imgInfoWindow[0] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/inv_0");
			imgInfoWindow[1] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/inv_1");
			imgInfoWindowt = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/inv_5");
			wTrangbi = imgInfoWindow[0].getWidth();
			imgCheck = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/check");
			imgCell[0] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/c0");
			imgCell[1] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/c1");
			imgCell[2] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/c2");
			imgK0 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/k0");
			imgNewBt[0] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/b0");
			imgNewBt[1] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/b1");
			imgNewBt[2] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/b2");
			imgNewBt[4] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/b4");
			GameScr_HAIRMOD_PROTECTED.img12plus = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/plus12");
			GameScr_HAIRMOD_PROTECTED.petShadow = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/shadow");
			QuickSlot_HAIRMOD_PROTECTED.img = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/b3");
			icon_automode = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/icon_automode");
			GameScr_HAIRMOD_PROTECTED.imghealth = new Image_HAIRMOD_PROTECTED[2];
			GameScr_HAIRMOD_PROTECTED.imghealth[0] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/m1");
			GameScr_HAIRMOD_PROTECTED.imghealth[1] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/m2");
			try
			{
				imgEffNew = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/h0");
			}
			catch (Exception)
			{
			}
			initQuanAoHt();
		}
		catch (Exception ex2)
		{
			Out_HAIRMOD_PROTECTED.println(ex2.StackTrace);
		}
	}

	public static void setEffSh(sbyte[] arr)
	{
		effSh = arr;
		FilePack_HAIRMOD_PROTECTED.initByArray(arr);
		imgExplosion = FilePack_HAIRMOD_PROTECTED.getImg("explosion");
		FilePack_HAIRMOD_PROTECTED.reset();
	}

	public static void loadPart2()
	{
		if (imgSelect == null)
		{
			FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.main);
			imgSelect = FilePack_HAIRMOD_PROTECTED.getImg("select");
			imgSelect1 = FilePack_HAIRMOD_PROTECTED.getImg("select1");
			imgSelect2 = FilePack_HAIRMOD_PROTECTED.getImg("select3");
			GameScr_HAIRMOD_PROTECTED.imgCong = new Image_HAIRMOD_PROTECTED[2];
			for (int i = 0; i < 2; i++)
			{
				GameScr_HAIRMOD_PROTECTED.imgCong[i] = FilePack_HAIRMOD_PROTECTED.getImg("cong" + (i + 1));
			}
			if (GameScr_HAIRMOD_PROTECTED.imgHpMonster == null)
			{
				GameScr_HAIRMOD_PROTECTED.imgHpMonster = FilePack_HAIRMOD_PROTECTED.getImg("mauquai");
			}
			MagicBeam_HAIRMOD_PROTECTED.imgFire = FilePack_HAIRMOD_PROTECTED.getImg("fire");
			Char_HAIRMOD_PROTECTED.imgFlgPk = FilePack_HAIRMOD_PROTECTED.getImg("flg");
			Char_HAIRMOD_PROTECTED.imgLight = FilePack_HAIRMOD_PROTECTED.getImg("light");
			imgSoft = new Image_HAIRMOD_PROTECTED[2];
			for (int j = 0; j < 2; j++)
			{
				imgSoft[j] = FilePack_HAIRMOD_PROTECTED.getImg("soft" + j);
			}
			imgNguHanh = FilePack_HAIRMOD_PROTECTED.getImg("nguhanh");
			if (imgLeg == null)
			{
				imgLeg = new Image_HAIRMOD_PROTECTED[2];
			}
			for (int k = 0; k < 2; k++)
			{
				imgLeg[k] = FilePack_HAIRMOD_PROTECTED.getImg("ch" + k);
			}
			FilePack_HAIRMOD_PROTECTED.reset();
			FilePack_HAIRMOD_PROTECTED.init(Main_HAIRMOD_PROTECTED.res + "/other");
			if (GameScr_HAIRMOD_PROTECTED.imgInfo == null)
			{
				GameScr_HAIRMOD_PROTECTED.imgInfo = FilePack_HAIRMOD_PROTECTED.getImg("info");
			}
			FilePack_HAIRMOD_PROTECTED.reset();
		}
	}

	public static int random(int a, int b)
	{
		return a + r.nextInt(b - a);
	}

	public static GemTemp_HAIRMOD_PROTECTED getGemByID(short id)
	{
		for (int i = 0; i < gemTemplate.size(); i++)
		{
			GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)gemTemplate.elementAt(i);
			if (gemTemp_HAIRMOD_PROTECTED != null && gemTemp_HAIRMOD_PROTECTED.id == id)
			{
				return gemTemp_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public static void initQuanAoHt()
	{
		quanao.addElement(new MyHashTable_HAIRMOD_PROTECTED());
		quanao.addElement(new MyHashTable_HAIRMOD_PROTECTED());
		quanao.addElement(new MyHashTable_HAIRMOD_PROTECTED());
		quanao.addElement(new MyHashTable_HAIRMOD_PROTECTED());
		quanao.addElement(new MyHashTable_HAIRMOD_PROTECTED());
	}

	public static int[] getAttributeItem(int charClass, int idTemplate, int type)
	{
		int[] array = new int[10];
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = ((ItemTemplate_HAIRMOD_PROTECTED[])itemTemplates.elementAt(0))[idTemplate];
		array[0] = itemTemplate_HAIRMOD_PROTECTED.attb[0];
		for (int i = 1; i < 4; i++)
		{
			array[i] = (short)(itemTemplate_HAIRMOD_PROTECTED.attb[i] + ((!ItemInInventory_HAIRMOD_PROTECTED.isAnimalArmor(type)) ? (itemTemplate_HAIRMOD_PROTECTED.attb[i] * percent[charClass] / 100) : 0));
		}
		for (int j = 4; j < 10; j++)
		{
			array[j] = itemTemplate_HAIRMOD_PROTECTED.attb[j];
		}
		return array;
	}

	public static ItemTemplate_HAIRMOD_PROTECTED getItem(int clazz, int id)
	{
		ItemTemplate_HAIRMOD_PROTECTED[] array = (ItemTemplate_HAIRMOD_PROTECTED[])itemTemplates.elementAt(0);
		for (int i = 0; i < array.Length; i++)
		{
			if (array[i] != null && array[i].id == id)
			{
				return array[i];
			}
		}
		return null;
	}

	public static GemTemp_HAIRMOD_PROTECTED getShopByID(short id)
	{
		for (int i = 0; i < shopTemplate.size(); i++)
		{
			GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)shopTemplate.elementAt(i);
			if (gemTemp_HAIRMOD_PROTECTED != null && gemTemp_HAIRMOD_PROTECTED.id == id)
			{
				return gemTemp_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public static XaphuTemplate_HAIRMOD_PROTECTED getXaphu()
	{
		return (Tilemap_HAIRMOD_PROTECTED.idXaphu < 0) ? null : XAPHU[Tilemap_HAIRMOD_PROTECTED.idXaphu];
	}

	public static Image_HAIRMOD_PROTECTED getImgeffect(int id)
	{
		return imgEffect[id];
	}

	public static void arraycopy(sbyte[] scr, int scrPos, ref sbyte[] dest, int destPos, int lenght)
	{
		if (scr != null && dest != null && destPos + lenght <= dest.Length)
		{
			for (int i = scrPos; i < lenght; i++)
			{
				dest[destPos + i - scrPos] = scr[i];
			}
		}
	}

	public static Image_HAIRMOD_PROTECTED createImgByHeader(sbyte[] header, sbyte[] data)
	{
		sbyte[] dest = new sbyte[header.Length + data.Length];
		arraycopy(header, 0, ref dest, 0, header.Length);
		arraycopy(data, 0, ref dest, header.Length, data.Length);
		return Image_HAIRMOD_PROTECTED.createImage(dest, 0, dest.Length);
	}

	public static void removeWeaPone()
	{
		for (int i = 0; i < Char_HAIRMOD_PROTECTED.imgWeapone.Length; i++)
		{
			for (int j = 0; j < Char_HAIRMOD_PROTECTED.imgWeapone[i].Length; j++)
			{
				for (int k = 0; k < Char_HAIRMOD_PROTECTED.imgWeapone[i][j].Length; k++)
				{
					Char_HAIRMOD_PROTECTED.imgWeapone[i][j][k] = null;
				}
			}
		}
		for (int l = 0; l < 5; l++)
		{
			for (int m = 0; m < 3; m++)
			{
				wpSplashInfos[l][m] = null;
			}
		}
	}

	public static WeaponInfo_HAIRMOD_PROTECTED loadImgWeaPone(int i, int j, int index)
	{
		if (Char_HAIRMOD_PROTECTED.imgWeapone[i][j][index] == null)
		{
			load.loadImgWeaPone(i, j, index);
		}
		return Char_HAIRMOD_PROTECTED.imgWeapone[i][j][index];
	}

	public static WPSplashInfo_HAIRMOD_PROTECTED loadWpsPlash(int i, int j)
	{
		if (wpSplashInfos[i][j] == null)
		{
			load.loadWpsPlash(i, j);
		}
		return wpSplashInfos[i][j];
	}

	public static Image_HAIRMOD_PROTECTED getImgArrow(int i)
	{
		if (imgArrow[i] == null && load.indexArrow == -1)
		{
			load.indexArrow = i;
		}
		return imgArrow[i];
	}

	public static void loadImgArrow(int i)
	{
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.arrow);
		try
		{
			switch (i)
			{
			case 7:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/kiem");
				break;
			case 8:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/skillboss");
				break;
			case 9:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/sword skill/nut");
				break;
			case 10:
				imgArrow[10] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/46");
				break;
			case 11:
				imgArrow[11] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/47");
				break;
			case 12:
				imgArrow[12] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/48");
				break;
			case 13:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/54");
				break;
			case 14:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/56");
				break;
			case 15:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/62");
				break;
			case 16:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/65");
				break;
			case 17:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/68");
				break;
			case 18:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/69");
				break;
			case 19:
				imgArrow[i] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/70");
				break;
			default:
				imgArrow[i] = FilePack_HAIRMOD_PROTECTED.getImg("arrow" + i);
				break;
			}
		}
		catch (Exception)
		{
		}
		FilePack_HAIRMOD_PROTECTED.reset();
	}

	public static void paintImgEff(MyGraphics_HAIRMOD_PROTECTED g, int type, int x0, int y0, int w, int h, int x, int y, int anthor)
	{
		if (type < imgEffect.Length && imgEffect[type] == null)
		{
			load.loadImgEffect(type);
		}
		else if (type < imgEffect.Length)
		{
			g.drawRegion(imgEffect[type], x0, y0, w, h, 0, x, y, anthor);
		}
	}

	public static Image_HAIRMOD_PROTECTED loadImgEff(int index)
	{
		if (imgEffect[index] == null)
		{
			load.loadImgEffect(index);
		}
		return imgEffect[index];
	}

	public static void getImgEffect(int index)
	{
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.eff);
		if (index < 25 || index > 33)
		{
			imgEffect[index] = FilePack_HAIRMOD_PROTECTED.getImg("g" + index);
		}
		if (imgEffect[26] == null)
		{
			imgEffect[25] = FilePack_HAIRMOD_PROTECTED.getImg("g25");
			imgEffect[26] = FilePack_HAIRMOD_PROTECTED.getImg("g26");
			imgEffect[27] = Image_HAIRMOD_PROTECTED.creatImageByImage(imgEffect[25]);
			imgEffect[28] = FilePack_HAIRMOD_PROTECTED.getImg("g27");
			imgEffect[29] = FilePack_HAIRMOD_PROTECTED.getImg("g28");
			imgEffect[30] = FilePack_HAIRMOD_PROTECTED.getImg("g29");
			imgEffect[31] = FilePack_HAIRMOD_PROTECTED.getImg("g30");
			imgEffect[32] = FilePack_HAIRMOD_PROTECTED.getImg("g31");
			imgEffect[33] = FilePack_HAIRMOD_PROTECTED.getImg("g32");
			imgEffect[41] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/42");
			imgEffect[42] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/43");
			imgEffect[43] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/44");
			imgEffect[44] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/45");
			imgEffect[45] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/49");
			imgEffect[46] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/50");
			imgEffect[47] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/51");
			imgEffect[48] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/52");
			imgEffect[49] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/53");
			imgEffect[50] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/55");
			imgEffect[51] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/57");
			imgEffect[52] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/58");
			imgEffect[53] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/59");
			imgEffect[54] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/60");
			imgEffect[55] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/61");
			backSmoke = new FrameImage_HAIRMOD_PROTECTED(imgEffect[32], 12, 13);
		}
		if (imgEffect[41] == null)
		{
			imgEffect[41] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/42");
			imgEffect[42] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/43");
			imgEffect[43] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/44");
			imgEffect[44] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/45");
			imgEffect[45] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/49");
			imgEffect[46] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/50");
			imgEffect[47] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/51");
			imgEffect[48] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/52");
			imgEffect[49] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/53");
			imgEffect[50] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/55");
			imgEffect[51] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/57");
			imgEffect[52] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/58");
			imgEffect[53] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/59");
			imgEffect[54] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/60");
			imgEffect[55] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/61");
			imgEffect[56] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/63");
			if (imgArrow[15] != null)
			{
				imgEffect[57] = imgArrow[15];
			}
			else
			{
				imgEffect[57] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/62");
			}
			imgEffect[58] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/64");
			imgEffect[59] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/66");
			imgEffect[60] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/67");
			imgEffect[61] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/71");
			imgEffect[62] = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/newEf/72");
			backSmoke = new FrameImage_HAIRMOD_PROTECTED(imgEffect[32], 12, 13);
		}
		FilePack_HAIRMOD_PROTECTED.reset();
	}

	public static void getImgWeaPone(int i, int j, int index)
	{
		string[] array = new string[5] { "kiem", "daidao", "phapsu", "bua", "cung" };
		FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.wps + array[i] + "/" + j);
		Char_HAIRMOD_PROTECTED.imgWeapone[i][j][index] = new WeaponInfo_HAIRMOD_PROTECTED();
		try
		{
			if (j >= 5)
			{
				if (index < 5)
				{
					sbyte[] header = FilePack_HAIRMOD_PROTECTED.instance.loadFile(index + "_h");
					sbyte[] data = FilePack_HAIRMOD_PROTECTED.instance.loadFile("data");
					Char_HAIRMOD_PROTECTED.imgWeapone[i][j][index].img = createImgByHeader(header, data);
				}
			}
			else
			{
				sbyte[] header2 = FilePack_HAIRMOD_PROTECTED.instance.loadFile(index + "_h");
				sbyte[] data2 = FilePack_HAIRMOD_PROTECTED.instance.loadFile("data");
				Char_HAIRMOD_PROTECTED.imgWeapone[i][j][index].img = createImgByHeader(header2, data2);
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(" loi tai getimgweapone " + ex.StackTrace);
		}
		FilePack_HAIRMOD_PROTECTED.reset();
	}

	public static WPSplashInfo_HAIRMOD_PROTECTED GetWPSplashInfo(int i, int j)
	{
		try
		{
			wpSplashInfos[i][j] = new WPSplashInfo_HAIRMOD_PROTECTED();
			int num = j;
			if (i == 3 || i == 4 || (i == 2 && num == 1))
			{
				num = 0;
			}
			else if ((i == 0 || i == 1) && num == 2)
			{
				num = 1;
			}
			FilePack_HAIRMOD_PROTECTED filePack_HAIRMOD_PROTECTED = new FilePack_HAIRMOD_PROTECTED(FilePack_HAIRMOD_PROTECTED.wps + i, null);
			wpSplashInfos[i][j].image = filePack_HAIRMOD_PROTECTED.loadImage(string.Empty + num + ".png");
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = new DataInputStream_HAIRMOD_PROTECTED(filePack_HAIRMOD_PROTECTED.loadData(num + ".d"));
			if (dataInputStream_HAIRMOD_PROTECTED != null)
			{
				for (int k = 0; k < 4; k++)
				{
					for (int l = 0; l < 8; l++)
					{
						wpSplashInfos[i][j].P0_X[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
						wpSplashInfos[i][j].P0_Y[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
						wpSplashInfos[i][j].PF_X[k][l] = readSignByte(dataInputStream_HAIRMOD_PROTECTED);
						wpSplashInfos[i][j].PF_Y[k][l] = readSignByte(dataInputStream_HAIRMOD_PROTECTED);
						wpSplashInfos[i][j].PF_W[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
						wpSplashInfos[i][j].PF_H[k][l] = dataInputStream_HAIRMOD_PROTECTED.read();
					}
				}
			}
			FilePack_HAIRMOD_PROTECTED.reset();
		}
		catch (IOException)
		{
			wpSplashInfos[i][j] = null;
		}
		return wpSplashInfos[i][j];
	}

	public static void removeImgEffect()
	{
		for (int i = 0; i < 25; i++)
		{
			if (i != 8)
			{
				imgEffect[i] = null;
			}
		}
		imgWayPoint = null;
		GC.Collect();
	}

	public static void loadMonster(int type, sbyte[] info)
	{
		monsterTemplates[type].setInfo(type, string.Empty, "m" + type, info[0], info[1], info[2], info[3], info[4], info[5], info[6], info[7], info[8]);
	}

	public static void resetImgMonsTemp()
	{
		if (monsterTemplates == null)
		{
			return;
		}
		for (int i = 0; i < monsterTemplates.Length; i++)
		{
			if (monsterTemplates[i] != null)
			{
				monsterTemplates[i].image = null;
				monsterTemplates[i].imageObj = null;
			}
		}
	}

	public static void removeMonsterTemplet()
	{
		resetImgMonsTemp();
		treeInfos.clear();
	}

	public static void loadMonster()
	{
	}

	public static CharPartInfo_HAIRMOD_PROTECTED getCharPartInfo(int type, int id)
	{
		CharPartInfo_HAIRMOD_PROTECTED charPartInfo_HAIRMOD_PROTECTED = null;
		MyHashTable_HAIRMOD_PROTECTED myHashTable_HAIRMOD_PROTECTED = (MyHashTable_HAIRMOD_PROTECTED)quanao.elementAt(type);
		charPartInfo_HAIRMOD_PROTECTED = (CharPartInfo_HAIRMOD_PROTECTED)myHashTable_HAIRMOD_PROTECTED.get(id + string.Empty);
		if (charPartInfo_HAIRMOD_PROTECTED == null)
		{
			charPartInfo_HAIRMOD_PROTECTED = new CharPartInfo_HAIRMOD_PROTECTED(type, id);
			charPartInfo_HAIRMOD_PROTECTED.time = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000);
			if (charPartInfo_HAIRMOD_PROTECTED.image == null)
			{
				GameService_HAIRMOD_PROTECTED.gI().getTreeImage(-1, type, id);
			}
			myHashTable_HAIRMOD_PROTECTED.put(id + string.Empty, charPartInfo_HAIRMOD_PROTECTED);
		}
		if (charPartInfo_HAIRMOD_PROTECTED.image == null && mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000 - charPartInfo_HAIRMOD_PROTECTED.time == 15)
		{
			GameService_HAIRMOD_PROTECTED.gI().getTreeImage(-1, type, id);
			charPartInfo_HAIRMOD_PROTECTED.time = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000);
		}
		charPartInfo_HAIRMOD_PROTECTED.timeRemove = (int)(mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000);
		return charPartInfo_HAIRMOD_PROTECTED;
	}

	public static void paintDefault(MyGraphics_HAIRMOD_PROTECTED g, int xp, int yp, int dir, int frame, int type)
	{
		if (type == 1 && defaultBody != null)
		{
			defaultBody.paint(g, xp, type, dir, frame);
		}
		if (type == 0 && defaultLeg != null)
		{
			defaultLeg.paint(g, xp, type, dir, frame);
		}
		if (type == 2 && defaultHead != null)
		{
			defaultHead.paint(g, xp, type, dir, frame);
		}
	}

	public static void resetTrangBi()
	{
		if (defaultBody == null)
		{
			defaultBody = getCharPartInfo(1, 54);
		}
		if (defaultLeg == null)
		{
			defaultLeg = getCharPartInfo(0, 54);
		}
		if (defaultHead == null)
		{
			defaultHead = getCharPartInfo(2, 27);
		}
	}

	public static TreeInfo_HAIRMOD_PROTECTED getTree(int index)
	{
		TreeInfo_HAIRMOD_PROTECTED treeInfo_HAIRMOD_PROTECTED = (TreeInfo_HAIRMOD_PROTECTED)treeInfos.get(string.Empty + index);
		if (treeInfo_HAIRMOD_PROTECTED == null)
		{
			treeInfo_HAIRMOD_PROTECTED = new TreeInfo_HAIRMOD_PROTECTED();
			treeInfo_HAIRMOD_PROTECTED.id = (short)index;
			treeInfos.put(string.Empty + index, treeInfo_HAIRMOD_PROTECTED);
		}
		return treeInfo_HAIRMOD_PROTECTED;
	}

	public static int getHeightTree(int index)
	{
		return getTree(index)?.getHeight() ?? 0;
	}

	public static int getwidthTree(int index)
	{
		return getTree(index)?.getWidth() ?? 0;
	}

	public static int getDxTree(int index)
	{
		return getTree(index)?.dx ?? 0;
	}

	public static int getDyTree(int index)
	{
		return getTree(index)?.dy ?? 0;
	}

	public static void loadTreeImage()
	{
	}

	public static void removeTreeImage(int i)
	{
	}

	public static int readSignByte(DataInputStream_HAIRMOD_PROTECTED iss)
	{
		sbyte[] array = new sbyte[1];
		try
		{
			int num = array.Length;
			if (num > 1)
			{
				num = 1;
			}
			for (int i = 0; i < 1; i++)
			{
				array[i] = iss.readByte();
			}
		}
		catch (IOException ex)
		{
			Out_HAIRMOD_PROTECTED.println(ex.StackTrace + " loi readSingsbytes");
		}
		return array[0];
	}

	public static void loadImgMap()
	{
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED = null;
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED2 = null;
		Image_HAIRMOD_PROTECTED image_HAIRMOD_PROTECTED3 = null;
		int num = 0;
		int num2 = 0;
		int num3 = 0;
		if (image_HAIRMOD_PROTECTED == null)
		{
			image_HAIRMOD_PROTECTED = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t_thanh");
		}
		if (image_HAIRMOD_PROTECTED2 == null)
		{
			image_HAIRMOD_PROTECTED2 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t");
		}
		if (image_HAIRMOD_PROTECTED3 == null)
		{
			image_HAIRMOD_PROTECTED3 = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t_hang");
		}
		num = image_HAIRMOD_PROTECTED.getHeight() / 16;
		num2 = image_HAIRMOD_PROTECTED2.getHeight() / 16;
		num3 = image_HAIRMOD_PROTECTED3.getHeight() / 16;
		Tilemap_HAIRMOD_PROTECTED.img[0] = new Image_HAIRMOD_PROTECTED[num];
		Tilemap_HAIRMOD_PROTECTED.img[1] = new Image_HAIRMOD_PROTECTED[num2];
		Tilemap_HAIRMOD_PROTECTED.img[2] = new Image_HAIRMOD_PROTECTED[num3];
		for (int i = 0; i < num; i++)
		{
			Tilemap_HAIRMOD_PROTECTED.img[0][i] = Image_HAIRMOD_PROTECTED.createImage(image_HAIRMOD_PROTECTED, 0, i * 16, 16, 16, 0);
		}
		for (int j = 0; j < num2; j++)
		{
			Tilemap_HAIRMOD_PROTECTED.img[1][j] = Image_HAIRMOD_PROTECTED.createImage(image_HAIRMOD_PROTECTED2, 0, j * 16, 16, 16, 0);
		}
		for (int k = 0; k < num3; k++)
		{
			Tilemap_HAIRMOD_PROTECTED.img[2][k] = Image_HAIRMOD_PROTECTED.createImage(image_HAIRMOD_PROTECTED3, 0, k * 16, 16, 16, 0);
		}
		image_HAIRMOD_PROTECTED.texture = null;
		image_HAIRMOD_PROTECTED = null;
		image_HAIRMOD_PROTECTED2.texture = null;
		image_HAIRMOD_PROTECTED2 = null;
		image_HAIRMOD_PROTECTED3.texture = null;
		image_HAIRMOD_PROTECTED3 = null;
		Resources.UnloadUnusedAssets();
		GC.Collect();
	}

	public static void loadTileImage(int lv)
	{
		Tilemap_HAIRMOD_PROTECTED.typeTile = 0;
		try
		{
			DataInputStream_HAIRMOD_PROTECTED dataInputStream_HAIRMOD_PROTECTED = null;
			if (lv > 200)
			{
				Tilemap_HAIRMOD_PROTECTED.typeTile = 0;
				dataInputStream_HAIRMOD_PROTECTED = DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(Main_HAIRMOD_PROTECTED.res + "/t_thanh");
				mauTile = 0;
				imgtileSmall = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t_thanh_s");
			}
			else if (lv < 110)
			{
				Tilemap_HAIRMOD_PROTECTED.typeTile = 1;
				dataInputStream_HAIRMOD_PROTECTED = DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(Main_HAIRMOD_PROTECTED.res + "/t");
				imgtileSmall = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t_small");
				mauTile = 1;
			}
			else
			{
				Tilemap_HAIRMOD_PROTECTED.typeTile = 2;
				dataInputStream_HAIRMOD_PROTECTED = DataInputStream_HAIRMOD_PROTECTED.getResourceAsStream(Main_HAIRMOD_PROTECTED.res + "/t_hang");
				imgtileSmall = Image_HAIRMOD_PROTECTED.createImage(Main_HAIRMOD_PROTECTED.res + "/pngtile/t_hang_s");
				mauTile = 2;
			}
			try
			{
				Tilemap_HAIRMOD_PROTECTED.typeOfTile = null;
				Tilemap_HAIRMOD_PROTECTED.typeOfTile = new int[dataInputStream_HAIRMOD_PROTECTED.available()];
				for (int i = 0; i < Tilemap_HAIRMOD_PROTECTED.typeOfTile.Length; i++)
				{
					Tilemap_HAIRMOD_PROTECTED.typeOfTile[i] = dataInputStream_HAIRMOD_PROTECTED.readUnsignedByte();
				}
			}
			catch (Exception)
			{
				Out_HAIRMOD_PROTECTED.LogWarning("Error Load Back Tile");
			}
		}
		catch (Exception ex2)
		{
			Out_HAIRMOD_PROTECTED.LogError(ex2.StackTrace + "  load tile loi");
		}
	}

	public static void paintDlgFull(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		x -= 10;
		int num = y + 14;
		g.drawImage(imgInv[2], x + 15, num, 20);
		g.drawImage(imgInv[2], x + 85, num, 20);
		g.drawImage(imgInv[2], x + 15, num + 46, 20);
		g.drawImage(imgInv[2], x + 85, num + 46, 20);
		g.drawImage(imgInv[2], x + 15, num + 92, 20);
		g.drawImage(imgInv[2], x + 85, num + 92, 20);
		int num2 = x + 12;
		int num3 = y + 11;
		int num4 = 144;
		int num5 = 144;
		for (int i = 0; i < 3; i++)
		{
			g.setColor(color[i]);
			g.drawRect(num2 + i, num3 + i, num4 - i * 2, num5 - i * 2);
		}
		g.drawImage(imgInv[0], x + 10, y + 9, 20);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + 159, y + 9, 24);
		g.drawRegion(imgInv3, 0, 0, 18, 19, 2, x + 10, y + 158, 36);
		g.drawImage(imgInv3, x + 159, y + 158, 40);
	}

	public static void paintSelected(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h)
	{
		g.setColor(34949);
		g.fillRect(x, y, w, h);
		g.setColor(11908790);
		g.drawRect(x, y, w, h);
	}

	public static void paintDlgDragonFull(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h)
	{
		int num = w / 70 + 1;
		int num2 = h / 47 + 1;
		g.setClip(x, y, w, h);
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < num2; j++)
			{
				g.drawImage(imgInv[2], x + i * 70, y + j * 47, 0);
			}
		}
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		for (int k = 0; k < 3; k++)
		{
			g.setColor(color[k]);
			g.drawRect(x + k, y + k, w - k * 2, h - k * 2);
		}
		g.drawImage(imgInv[0], x - 2, y - 2, 0);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + w - 15, y - 2, 0);
		g.drawRegion(imgInv3, 0, 0, 18, 19, 2, x - 2, y + h - 15, 0);
		g.drawImage(imgInv3, x + w - 15, y + h - 16, 0);
		g.drawImage(imgInv[1], x + w / 2, y - 4, 3);
	}

	public static void paintRectColor(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h, int count, int color1, int color2)
	{
		g.setColor(color1);
		g.drawRect(x, y, w, h);
		g.setColor(color2);
		if (count < w)
		{
			int num = wh;
			if (num + count >= w)
			{
				num = w - count;
			}
			g.fillRect(x + count, y, num, 1);
		}
		else if (count < w + h)
		{
			int num2 = wh;
			if (num2 + (count - w) >= h)
			{
				num2 = h - (count - w);
			}
			g.fillRect(x + w, y + (count - w), 1, num2);
		}
		else if (count < w * 2 + h)
		{
			int num3 = wh;
			if (num3 + (count - w - h) >= w)
			{
				num3 = w - (count - w - h);
			}
			g.fillRect(x + (w - (count - w - h)) - num3, y + h, num3, 1);
		}
		else if (count < w * h * 2)
		{
			int num4 = wh;
			if (num4 + (count - w * 2 - h) >= h)
			{
				num4 = h - (count - w * 2 - h);
			}
			g.fillRect(x, y + (h - (count - w * 2 - h)) - num4, 1, num4);
		}
		Res_HAIRMOD_PROTECTED.num++;
		if (Res_HAIRMOD_PROTECTED.num >= 4)
		{
			Res_HAIRMOD_PROTECTED.num = 0;
		}
	}

	public static void paintDlgFull(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h)
	{
		int num = w / 70 + 1;
		g.setClip(x, y, w, h);
		for (int i = 0; i < num; i++)
		{
			g.drawImage(imgInv[2], x + 70 * i, y, 0);
		}
		g.setColor(277044);
		g.fillRect(x, y + 25, w, h);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		for (int j = 0; j < 3; j++)
		{
			g.setColor(color[j]);
			g.drawRect(x + j, y + j, w - j * 2 - 1, h - j * 2 - 1);
			g.fillRect(x + 3, y + 25, w - 6, 1);
		}
		g.drawImage(imgInv[0], x - 2, y - 2, 0);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + w + 2, y - 2, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
		g.drawRegion(imgInv3, 0, 0, 18, 19, 2, x - 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		g.drawImage(imgInv3, x + w + 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
	}

	public static void paintDlgFull(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h, int y1, string tile, bool isDD, int h0)
	{
		int num = w / 70 + 1;
		g.setClip(x, y, w, h);
		for (int i = 0; i < num; i++)
		{
			g.drawImage(imgInv[2], x + 70 * i, y, 0);
		}
		g.setColor(277044);
		g.fillRect(x, y + 25, w, h);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		for (int j = 0; j < 3; j++)
		{
			g.setColor(color[j]);
			g.drawRect(x + j, y + j, w - j * 2 - 1, h - j * 2 - 1);
		}
		g.fillRect(x + 3, y + 25, w - 6, 1);
		if (!isDD)
		{
			g.fillRect(x + 3, y + 25 + y1 + h0, w - 6, 1);
		}
		else
		{
			g.fillRect(x + 3, y + 25 + y1 + h0, w - 6, 1);
			g.fillRect(x + 3, y + y1 + h0 - 2, w - 6, 1);
		}
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, tile, x + w / 2, y + 8, 2);
		g.drawImage(imgInv[0], x - 2, y - 2, 0);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 2, x + w + 2, y - 2, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 6, x - 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		g.drawRegion(imgInv[0], 0, 0, 18, 19, 3, x + w + 2, y + h + 2, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.RIGHT);
	}

	public static void drawRect(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h)
	{
		for (int i = 0; i < 3; i++)
		{
			g.setColor(color[i]);
			g.drawRect(x + i, y + i, w - i * 2, h - i * 2);
		}
	}

	public static int[] trimArray(int[] array)
	{
		int[] array2 = null;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < array.Length; i++)
		{
			if (mVector_HAIRMOD_PROTECTED2.size() == 0)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(array[i] + string.Empty);
				continue;
			}
			bool flag = false;
			for (int j = 0; j < mVector_HAIRMOD_PROTECTED2.size(); j++)
			{
				int num = int.Parse((string)mVector_HAIRMOD_PROTECTED2.elementAt(j));
				if (num == array[i])
				{
					flag = true;
					break;
				}
			}
			if (!flag)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(array[i] + string.Empty);
			}
		}
		array2 = new int[mVector_HAIRMOD_PROTECTED2.size()];
		for (int k = 0; k < mVector_HAIRMOD_PROTECTED2.size(); k++)
		{
			array2[k] = int.Parse((string)mVector_HAIRMOD_PROTECTED2.elementAt(k));
		}
		return array2;
	}

	public static void paintPotion(MyGraphics_HAIRMOD_PROTECTED g, int index, int x, int y, int anthor)
	{
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(index + 5500), x, y, anthor);
	}

	public static void paintGem(MyGraphics_HAIRMOD_PROTECTED g, int index, int x, int y)
	{
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(index + 6500), x, y, 3);
	}

	public static void paintGemKhoa(MyGraphics_HAIRMOD_PROTECTED g, int index, int x, int y)
	{
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(index + 6500), x, y, 3);
	}

	public static Image_HAIRMOD_PROTECTED createImgByByteArray(sbyte[] array)
	{
		return Image_HAIRMOD_PROTECTED.createImage(array, 0, array.Length);
	}

	public static int rnd(int a)
	{
		return r.nextInt(a);
	}

	public static int rnd(int a, int b)
	{
		if (r.nextInt(2) == 0)
		{
			return a;
		}
		return b;
	}

	public static string[] getNameMap(int index, int id)
	{
		try
		{
			return nameMap[index][id];
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(" loi getNameMap nay Res" + ex.StackTrace);
		}
		return null;
	}

	public static short[] getLocation(int index, int id)
	{
		try
		{
			return locationMap[index][id];
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(" loi getLocation nay Res" + ex.StackTrace);
		}
		return null;
	}

	public static Image_HAIRMOD_PROTECTED getWayPoint()
	{
		if (imgWayPoint == null)
		{
			FilePack_HAIRMOD_PROTECTED.init(FilePack_HAIRMOD_PROTECTED.main);
			imgWayPoint = FilePack_HAIRMOD_PROTECTED.getImg("waypoint");
			FilePack_HAIRMOD_PROTECTED.reset();
		}
		return imgWayPoint;
	}

	public static Image_HAIRMOD_PROTECTED transparentBG(Image_HAIRMOD_PROTECTED img)
	{
		return null;
	}

	public static sbyte[] intTosbyteArray(int value)
	{
		sbyte[] array = new sbyte[4];
		for (int i = 0; i < 4; i++)
		{
			int num = (array.Length - 1 - i) * 8;
			array[i] = (sbyte)((value >> num) & 0xFF);
		}
		return array;
	}

	public static int getDistance(int x, int y, int x2, int y2)
	{
		return getDistance(x - x2, y - y2);
	}

	public static int sqrt(int a)
	{
		if (a <= 0)
		{
			return 0;
		}
		int num = (a + 1) / 2;
		int num2;
		do
		{
			num2 = num;
			num = num / 2 + a / (2 * num);
		}
		while (Math_HAIRMOD_PROTECTED.abs(num2 - num) > 1);
		return num;
	}

	public static int getDistance(int x, int y)
	{
		return sqrt(x * x + y * y);
	}

	public static int sbyteToInit(sbyte[] bArr)
	{
		return ((0xFF & bArr[0]) << 24) | ((0xFF & bArr[1]) << 16) | ((0xFF & bArr[2]) << 8) | (0xFF & bArr[3]);
	}

	public static void removePartInfo()
	{
	}

	public static int getRange(int x1, int x2, int y1, int y2)
	{
		return Math_HAIRMOD_PROTECTED.abs(x1 - x2) + Math_HAIRMOD_PROTECTED.abs(y1 - y2);
	}
}
