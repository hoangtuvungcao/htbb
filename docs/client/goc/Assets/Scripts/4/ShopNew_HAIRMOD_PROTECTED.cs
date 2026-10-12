using System;

public class ShopNew_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public static ShopNew_HAIRMOD_PROTECTED me;

	public static string text = string.Empty;

	public short typeshop;

	public int x;

	public int y;

	public int w;

	public int h;

	public int selected;

	public int numW = 7;

	public int numH;

	public int xShow;

	public int yShow;

	public int wShow;

	public int hShow;

	public int wSmall;

	public int hSmall;

	public int x0;

	public int y0;

	public int idFrame;

	public int selectedGD;

	public int countFrame;

	public int[][] arrFrame = new int[2][]
	{
		new int[3] { 0, 1, 2 },
		new int[2] { 0, 1 }
	};

	public static string[] infoMainCharServer;

	private string[] title = new string[33]
	{
		"Hành trang", "Trang bị", "Tiềm năng", "Kỹ năng", "Thông tin", "Nhiệm vụ", "Học kỹ năng", "Trao đổi", "Nhóm", "Gian hàng",
		"Gian hàng", "Áo", "Quần", "Nón", "Nhẫn", "Dây chuyền", "Giày", "Găng tay", "Ngọc bội", "Gian hàng",
		"Gian hàng", "Luyện đồ", "Kho đồ", "Gian hàng", "Gian hàng", "Khảm", "Hợp thành", "Nhiệm vụ", "Chế đồ", "Kỹ năng bang hội",
		"Kỹ năng cá nhân", "Trang bị thú", "Hợp đồ thú"
	};

	private string[] tabName = new string[8] { "Áo", "Quần", "Nón", "Nhẫn", "D.chuyền", "Giày", "Găng tay", "Ngọc bội" };

	private string[] showText;

	public string name = string.Empty;

	public string firName = string.Empty;

	public mVector_HAIRMOD_PROTECTED list = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED newSkill;

	public mVector_HAIRMOD_PROTECTED readyBuy = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED ItemShopNew = new mVector_HAIRMOD_PROTECTED();

	public bool isShowText;

	public bool isSell;

	public bool isChangeTab;

	public bool isPaintMoney;

	public bool isTextArr;

	private bool trans;

	private int pb;

	private int pa;

	public static int cmtoY;

	public static int cmy;

	public static int cmdy;

	public static int cmvy;

	public static int cmyLim;

	public static int cmtoYText;

	public static int cmyText;

	public static int cmdyText;

	public static int cmvyText;

	public static int cmyLimText;

	public static int cmtoX;

	public static int cmx;

	public static int cmdx;

	public static int cmvx;

	public static int cmxLim;

	public static int focusTab;

	private int focusTabChinh;

	private int cmtoXTab;

	private int cmxTab;

	private int cmdxTab;

	private int cmvxTab;

	private int cmxLimTab;

	public static int cmtoXName;

	public static int cmxname;

	public static int dir = 1;

	public static sbyte[] index;

	private mVector_HAIRMOD_PROTECTED questInfo;

	public int idClass;

	public int countPop;

	public short idCharSell;

	public mVector_HAIRMOD_PROTECTED sellItem = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listTemp = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED listEp;

	public mVector_HAIRMOD_PROTECTED listCheDo;

	public mVector_HAIRMOD_PROTECTED listItemAnimal = new mVector_HAIRMOD_PROTECTED();

	private ItemInInventory_HAIRMOD_PROTECTED[] itemEpdothu = new ItemInInventory_HAIRMOD_PROTECTED[5];

	private short[][] posEpNgoc;

	public static sbyte idMonsterClanQuest = -1;

	public static sbyte countR;

	public static sbyte countL;

	public static sbyte countBL;

	public static sbyte countBR;

	public static sbyte idColor;

	public static sbyte lvThu;

	public static sbyte slNLThu;

	public static sbyte magicAnimal;

	public static short numberQuestGet = 0;

	public static short numberQuestAll;

	public static short idTemp;

	public static Char_HAIRMOD_PROTECTED charWearing;

	public sbyte[] arrayKhamNgoc;

	public sbyte[] arrSell;

	public static bool isKham = false;

	public bool isLoad;

	public bool isSelectCheDo;

	public bool isPaintfc;

	public static bool isWindowInfo;

	private int limMyBag;

	private int excess;

	private int maxSize;

	private int countTabMyitem;

	public static sbyte khoaCheDo;

	public static sbyte khoaDoThu;

	public static sbyte khoaImbue;

	public static sbyte khoaKham;

	private int wCell = 32;

	private int hCell = 32;

	private sbyte idTrade;

	public static int saveFocusTab;

	public static int addY;

	private bool isLastScreenMenuWindow;

	public static int yTr;

	public static int xTr;

	private int wKhung;

	private int hKhung;

	public Command_HAIRMOD_PROTECTED cmdShop;

	private bool is29;

	private int hView;

	private int xKhung;

	private int yKhung;

	private int addHclip;

	private int xCus = -20;

	public static sbyte optionCreateItem = 0;

	private Image_HAIRMOD_PROTECTED imgCharWear;

	public int idSeller;

	public int shopIdSell;

	private bool isFcStore;

	public int prizeImbue;

	private int idImbue = -1;

	private int idfcsetSelectedImbue;

	private bool first;

	private string[] charInfo;

	private bool isCmyTab3;

	private bool isshowInfo;

	private bool isTab3;

	private int saveSelect = -1;

	private int countClick;

	private int cmyKeep;

	private int newZise = 42;

	private int smallS = 34;

	private bool checkPaint;

	private int tdx;

	private int transX;

	private int dirX = 1;

	public static string nameshop = string.Empty;

	private bool isDefault;

	private string[] mau = new string[4] { "Trắng", "Hoàn mỹ", "Đỏ", "Xanh" };

	public bool isNguyenLieu;

	private int wSkill = 30;

	public static int[] posPaintWearingX = new int[20]
	{
		15 + xTr,
		15 + xTr,
		15 + xTr,
		165 + xTr,
		165 + xTr,
		165 + xTr,
		165 + xTr,
		165 + xTr,
		165 + xTr,
		165 + xTr,
		15 + xTr,
		165 + xTr,
		15 + xTr,
		115 + xTr,
		15 + xTr,
		15 + xTr,
		15 + xTr,
		15 + xTr,
		15 + xTr,
		34 + xTr
	};

	public static int[] posPaintWearingY = new int[20]
	{
		51 + yTr,
		135 + yTr,
		91 + yTr,
		11 + yTr,
		11 + yTr,
		11 + yTr,
		11 + yTr,
		11 + yTr,
		96 + yTr,
		51 + yTr,
		175 + yTr,
		175 + yTr,
		11 + yTr,
		175 + yTr,
		51 + yTr,
		91 + yTr,
		135 + yTr,
		175 + yTr,
		11 + yTr,
		92 + yTr
	};

	private int www = 180;

	public mVector_HAIRMOD_PROTECTED potionShop = new mVector_HAIRMOD_PROTECTED();

	public Image_HAIRMOD_PROTECTED imgWeaponAvatar;

	private static sbyte[] idWearChar = new sbyte[10] { 12, -1, 0, 9, 2, 8, 1, 8, 10, 11 };

	private static sbyte[] idWearAnimal = new sbyte[10] { 18, 19, 14, 20, 15, 21, 16, 22, 17, 23 };

	private static sbyte[][] posWearChar = new sbyte[10][]
	{
		new sbyte[2] { 10, 11 },
		new sbyte[2] { 56, 11 },
		new sbyte[2] { 10, 32 },
		new sbyte[2] { 56, 32 },
		new sbyte[2] { 10, 52 },
		new sbyte[2] { 56, 52 },
		new sbyte[2] { 10, 73 },
		new sbyte[2] { 56, 94 },
		new sbyte[2] { 10, 94 },
		new sbyte[2] { 56, 94 }
	};

	public static short MAX_POTION = 999;

	public bool transY;

	public bool transXB;

	public bool canSelecet;

	public bool isTabIp;

	public bool isPaintFc;

	private long timePointY;

	private long timePointX;

	private long count;

	private long timeOpen;

	private long timeDelay;

	private int pxLast;

	private int pyLast;

	private int xCamLast;

	private int dyTran;

	private int dxTran;

	private int hOne = 44;

	private int wOne = 44;

	private int sizeW;

	private int sizeH;

	private int disX;

	private int xBegin;

	private int yBegin;

	private int wTouch;

	private int hTouch;

	private int curentTabUpdate;

	private int timeHold;

	private int countPutKey;

	public float vY;

	public float vX;

	public bool transYShowText;

	private long timePointYt;

	private long timeOpent;

	private long timeDelayt;

	private int pyLastt;

	private int dyTrant;

	private int dxTrant;

	private int xBegint;

	private int yBegint;

	private int wToucht;

	private int hToucht;

	private int countShowText;

	public float vYT;

	public ShopNew_HAIRMOD_PROTECTED()
	{
		init();
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (isShowText)
			{
				closeText();
			}
			else
			{
				listTemp.removeAllElements();
				listEp = null;
				focusTab = saveFocusTab;
				doBuyItem();
				Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
				list.removeAllElements();
				charInfo = null;
				questInfo = null;
				imgCharWear = null;
				charWearing = null;
				transX = 0;
				dirX = 1;
				focusTabChinh = 0;
				focusTab = 0;
				isSell = false;
			}
		};
		command_HAIRMOD_PROTECTED.action = action;
		right = command_HAIRMOD_PROTECTED;
		focusTabChinh = 0;
		selected = 0;
		focusTab = 0;
		transX = 0;
		dirX = 1;
	}

	public static ShopNew_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ShopNew_HAIRMOD_PROTECTED());
	}

	public override void switchToMe()
	{
		isLastScreenMenuWindow = false;
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == MenuWindows_HAIRMOD_PROTECTED.gI())
		{
			isLastScreenMenuWindow = true;
		}
		else
		{
			isLastScreenMenuWindow = false;
		}
		base.switchToMe();
		init();
		isChangeTab = true;
		isSelectCheDo = false;
		isPaintfc = true;
	}

	public override void init()
	{
		isPaintFc = false;
		x = Canvas_HAIRMOD_PROTECTED.hw - 64;
		y = Canvas_HAIRMOD_PROTECTED.hh - 77;
		wKhung = Canvas_HAIRMOD_PROTECTED.w - 20;
		hKhung = Canvas_HAIRMOD_PROTECTED.h - 60;
		if (hKhung > GameMidlet_HAIRMOD_PROTECTED.MAX_H - 60)
		{
			hKhung = GameMidlet_HAIRMOD_PROTECTED.MAX_H - 60;
		}
		if (wKhung > GameMidlet_HAIRMOD_PROTECTED.MAX_W - 20)
		{
			wKhung = GameMidlet_HAIRMOD_PROTECTED.MAX_W - 20;
		}
		if (Canvas_HAIRMOD_PROTECTED.w * Canvas_HAIRMOD_PROTECTED.h > 153600)
		{
			xTr = (Canvas_HAIRMOD_PROTECTED.w - wKhung) / 2;
			yTr = (Canvas_HAIRMOD_PROTECTED.h - hKhung) / 2;
		}
		setLim();
		isWindowInfo = true;
		selected = 0;
		selectedGD = -1;
		xKhung = 10;
		yKhung = 20;
		cmtoX = (cmx = (cmxLimTab = (cmtoXTab = (cmxTab = 0))));
		cmxLimTab = tabName.Length * 70 - wKhung + 10;
		if (cmxLimTab < 0)
		{
			cmxLimTab = 0;
		}
		idTrade = 0;
		posPaintWearingX = new int[20]
		{
			15 + xTr,
			15 + xTr,
			15 + xTr,
			165 + xTr,
			165 + xTr,
			165 + xTr,
			165 + xTr,
			165 + xTr,
			165 + xTr,
			165 + xTr,
			15 + xTr,
			165 + xTr,
			15 + xTr,
			115 + xTr,
			15 + xTr,
			15 + xTr,
			15 + xTr,
			15 + xTr,
			15 + xTr,
			58 + xTr
		};
		posPaintWearingY = new int[20]
		{
			51 + yTr,
			135 + yTr,
			91 + yTr,
			11 + yTr,
			11 + yTr,
			11 + yTr,
			11 + yTr,
			11 + yTr,
			96 + yTr,
			51 + yTr,
			175 + yTr,
			175 + yTr,
			11 + yTr,
			175 + yTr,
			51 + yTr,
			91 + yTr,
			135 + yTr,
			175 + yTr,
			11 + yTr,
			180 + yTr
		};
	}

	public void setInfo(mVector_HAIRMOD_PROTECTED list)
	{
		this.list = list;
		numW = 7;
		numH = list.size() / 5;
		if (list.size() % numW != 0)
		{
			numH++;
		}
		if (numH < 5)
		{
			numH = 5;
		}
		if (index[focusTab] == 0 && numH > 6)
		{
			numH = 6;
		}
		setLim();
		isChangeTab = true;
		setCmdCenter();
	}

	public static int getIndex()
	{
		return index[focusTab];
	}

	private void setLim()
	{
		if (!is29)
		{
			int num = newZise;
			cmxLim = numW * wSmall - w;
			if (index != null)
			{
				if (index[focusTab] == 21 || index[focusTab] == 25)
				{
					if (ItemShopNew != null && listTemp != null)
					{
						int num2 = ItemShopNew.size() + listTemp.size();
						cmxLim = num2 * newZise - w + 50;
						Out_HAIRMOD_PROTECTED.println(num2 + " cmxlim  " + cmxLim);
					}
				}
				else if (index[focusTab] == 28)
				{
					cmx = (cmtoX = 0);
					int num3 = listTemp.size();
					cmxLim = num3 * newZise - 5 * newZise;
				}
				else if (index[focusTab] == 8)
				{
					int num4 = ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.listPotion.Length - 2;
					cmxLim = num4 * smallS - (wKhung - 7 * newZise);
				}
				if (index[focusTab] == 22 || index[focusTab] == 23)
				{
					num = 34;
					h = hView - 50;
				}
			}
			cmyLim = numH * num - h;
			if (cmxLim < 0)
			{
				cmxLim = 0;
			}
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
		}
		else
		{
			cmxLim = numW * wSmall - w;
			cmyLim = numH * hSmall - h;
			if (cmxLim < 0)
			{
				cmxLim = 0;
			}
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
		}
	}

	public void setCurTab()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == gI() && focusTab == 0)
		{
			setSelectTab();
			isChangeTab = false;
		}
	}

	public void setInfo(int focus, bool isSell, sbyte[] inn)
	{
		this.isSell = isSell;
		if (isSell)
		{
			arrSell = inn;
		}
		if (isSell)
		{
			arrSell = inn;
		}
		index = inn;
		focusTab = 0;
		if (index != null)
		{
			if (index[focusTab] == 29)
			{
				is29 = true;
			}
			else
			{
				is29 = false;
			}
		}
		selected = 0;
		setCmdCenter();
		setSelectTab();
	}

	public void setSize(int wBig, int hBig, int numW, int numH, int wSmall, int hSmall)
	{
		w = wBig;
		h = hBig;
		this.numW = numW;
		this.numH = numH;
		this.wSmall = wSmall;
		this.hSmall = hSmall;
		setLim();
		if (index[focusTab] == 3)
		{
			cmx = (cmtoX = (cmy = (cmtoY = 10)));
		}
		else if (index[focusTab] != 29)
		{
			cmx = (cmy = (cmtoX = (cmtoY = -10)));
		}
		else
		{
			cmx = (cmy = (cmtoX = (cmtoY = 0)));
		}
		if (isDefault)
		{
			cmx = (cmtoX = -10);
			cmy = (cmtoY = 0);
		}
	}

	public override void updateKey()
	{
		int num = 34;
		if (isShowText)
		{
			if (Canvas_HAIRMOD_PROTECTED.keyHold[2])
			{
				if (hShow >= 100)
				{
					cmtoYText -= 10;
					if (cmtoYText < 0)
					{
						cmtoYText = 0;
					}
				}
				else
				{
					closeText();
				}
				Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyHold[8])
			{
				if (hShow >= 100)
				{
					cmtoYText += 10;
					if (cmtoYText > cmyLimText)
					{
						cmtoYText = cmyLimText;
					}
				}
				else
				{
					closeText();
				}
				Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
			}
			Canvas_HAIRMOD_PROTECTED.isKeyPressed(2);
			Canvas_HAIRMOD_PROTECTED.isKeyPressed(8);
			if (Canvas_HAIRMOD_PROTECTED.currentDialog == null && isShowText)
			{
				xBegint = xShow;
				yBegint = y0;
				wToucht = wKhung - (x0 + 5 * (wCell + 12)) - 1;
				int num2 = Canvas_HAIRMOD_PROTECTED.h;
				if (num2 > 320)
				{
					num2 = 320;
				}
				hToucht = num2 - 60;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerClick && index[focusTab] == 0)
		{
			int num3 = Canvas_HAIRMOD_PROTECTED.h;
			if (num3 > 320)
			{
				num3 = 320;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointer(x0 - 5 + xTr, num3 - 70 + yTr, 50, 50))
			{
				if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					setSize(5 * wCell, 112, 7, 9, num, num);
					selected = 0;
					closeText();
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointer(x0 + 6 * wCell + xTr, num3 - 70 + yTr, 50, 50) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				setSize(5 * wCell, 112, 7, 9, num, num);
				selected = 0;
				closeText();
			}
		}
		if (isChangeTab && !Main_HAIRMOD_PROTECTED.isPC)
		{
			if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(8))
			{
				isChangeTab = false;
				setCmdCenter();
				Canvas_HAIRMOD_PROTECTED.keyHold[8] = false;
			}
			else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(4))
			{
				if (index.Length > 1)
				{
					countL = 5;
					cmxname = 100;
					dir = 1;
				}
				setSelectTab();
				setCmdCenter();
			}
			else if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(6))
			{
				if (index.Length > 1)
				{
					countR = 5;
					dir = -1;
					cmxname = -100;
				}
				setSelectTab();
				setCmdCenter();
			}
		}
		else if (index[focusTab] != 26 && cmy == 0 && numW != 0 && selected / numW == 0 && Canvas_HAIRMOD_PROTECTED.isKeyPressed(2) && index[focusTab] != 32)
		{
			isChangeTab = true;
			center = null;
		}
		updateKeyMain();
		if (left == null)
		{
			Out_HAIRMOD_PROTECTED.println("null me no roi ");
		}
		base.updateKey();
	}

	public void getItemAnimalFromList()
	{
		listItemAnimal = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < ItemShopNew.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if ((item.type == 14 || item.type == 15 || item.type == 16 || item.type == 17 || item.type == 18) && itemInInventory_HAIRMOD_PROTECTED.colorName == idColor)
			{
				listItemAnimal.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
		}
	}

	public void setSelectTab()
	{
		Out_HAIRMOD_PROTECTED.LogWarning(" WINDOWINFO TAI INDEX THU :  " + index[focusTab]);
		if (index[focusTab] != 0)
		{
			saveFocusTab = focusTab;
		}
		cmxname = 0;
		left = null;
		isPaintMoney = false;
		newSkill = null;
		posEpNgoc = null;
		list.removeAllElements();
		x0 = 1;
		y0 = 35;
		addHclip = -2;
		if (index.Length > 1)
		{
			firName = name;
			name = title[index[focusTab]];
		}
		int num = 34;
		countTabMyitem = 0;
		hView = hKhung - y0 + 20;
		switch (index[focusTab])
		{
		case 0:
		{
			isDefault = true;
			isPaintMoney = true;
			setInfo(getInventori());
			int hBig = 5 * wCell;
			if (isDefault)
			{
				hBig = 4 * wCell;
			}
			setSize(5 * wCell, hBig, 7, 7, num, num);
			hView = hKhung - y0 + 20;
			addHclip = -33;
			break;
		}
		case 1:
			xKhung = 10;
			yKhung = 20;
			x0 = xKhung + 15;
			y0 = yKhung + MainJ2ME_HAIRMOD_PROTECTED.hTab;
			setSize(117, 104, 3, 5, 97, 21);
			break;
		case 4:
			center = null;
			isPaintMoney = true;
			x0 += -4;
			y0 += -2;
			setSize(134, 120, 1, 10, 50, 12);
			break;
		case 6:
			questInfo.removeAllElements();
			break;
		case 7:
			x0 += 5;
			y0 += 7;
			left = null;
			setCmdCenter();
			break;
		case 8:
		{
			ActionPerform_HAIRMOD_PROTECTED action4 = delegate
			{
				doSelectedTrade(idTrade);
			};
			center = new Command_HAIRMOD_PROTECTED();
			center.action = action4;
			setCmdLeftTrade();
			break;
		}
		case sbyte.MaxValue:
			isPaintMoney = true;
			setSize(126, 90, 1, 1, 18, 18);
			setListSellItem(Canvas_HAIRMOD_PROTECTED.gameScr.shop, -1);
			setInfo(list);
			doSetLeftPetEat(-1);
			break;
		case 9:
		{
			isPaintMoney = true;
			int num3 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 7, num3, num3);
			setListSellItem(Canvas_HAIRMOD_PROTECTED.gameScr.shop, -1);
			setInfo(list);
			doSetLeftSellWeapon(-1);
			break;
		}
		case 10:
		{
			isPaintMoney = true;
			int num2 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 1, num2, num2);
			setListSellGemItem(Canvas_HAIRMOD_PROTECTED.gameScr.shop);
			setInfo(list);
			dosetLeftSellGemItem();
			break;
		}
		case 11:
		case 12:
		case 13:
		{
			isPaintMoney = true;
			int num12 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 1, num12, num12);
			setListSellItem(Canvas_HAIRMOD_PROTECTED.gameScr.shop, index[focusTab] - 11);
			setInfo(list);
			doSetLeftSellWeapon(index[focusTab] - 11);
			break;
		}
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
		{
			isPaintMoney = true;
			int num11 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 1, num11, num11);
			setListSellItem(Canvas_HAIRMOD_PROTECTED.gameScr.shop, index[focusTab] - 6);
			setInfo(list);
			doSetLeftSellWeapon(index[focusTab] - 6);
			break;
		}
		case 19:
		{
			isPaintMoney = true;
			int num10 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 1, num10, num10);
			setListPotion();
			setInfo(list);
			doSetLeftPotion();
			break;
		}
		case 20:
			isPaintMoney = true;
			setSize(126, 112, 1, 1, 18, 18);
			getShopTemplate();
			setInfo(list);
			doBuyShopSpecial();
			name = name + " " + (focusTab + 1);
			break;
		case 21:
			x0++;
			y0 += -6;
			setInfo(list);
			setSize(144, 110, 3, 5, 24, 24);
			setCmdCenter();
			doLeftImbue();
			break;
		case 22:
		{
			isPaintMoney = true;
			selected = 0;
			setSizeKeepItem(0);
			int num9 = 34;
			setSize(7 * wCell, 4 * wCell, 6, numH, num9, num9);
			setCmdCenter();
			setLeftKeepAndSellItem();
			break;
		}
		case 23:
		{
			isPaintMoney = true;
			selected = 0;
			setSizeKeepItem(0);
			int num8 = 34;
			setSize(7 * wCell, 4 * wCell, 6, numH, num8, num8);
			setCmdCenter();
			setLeftKeepAndSellItem();
			break;
		}
		case 24:
		{
			isPaintMoney = true;
			int num7 = 34;
			setSize(7 * wCell, 4 * wCell, 1, 1, num7, num7);
			setInfo(getListBuyItem());
			setLeftBuyItem();
			break;
		}
		case 25:
		{
			isChangeTab = false;
			x0++;
			y0 += -4;
			setInfo(list);
			setSize(144, 110, 3, 5, 24, 24);
			if (isKham)
			{
				setListImbue(false, 1, khoaKham);
			}
			else
			{
				name = "Đục lỗ";
				setListImbue(false, 2, khoaKham);
			}
			ActionPerform_HAIRMOD_PROTECTED action7 = delegate
			{
				setSelectedImbue();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action7;
			doLeftImbue();
			break;
		}
		case 26:
		{
			x0++;
			y0 += -4;
			setSize(126, 108, 5, 1, 20, 18);
			posEpNgoc = new short[5][]
			{
				new short[2] { 0, -10 },
				new short[2]
				{
					(short)(5 * wCell),
					-10
				},
				new short[2]
				{
					(short)(2 * wCell + wCell / 2),
					(short)(2 * wCell - wCell / 2)
				},
				new short[2]
				{
					0,
					(short)(4 * wCell - 10)
				},
				new short[2]
				{
					(short)(5 * wCell),
					(short)(4 * wCell - 10)
				}
			};
			setListImbue(true, 1, 0);
			listEp = new mVector_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action6 = delegate
			{
				doSelectedEpNgoc();
			};
			center = new Command_HAIRMOD_PROTECTED();
			center.action = action6;
			doLeftEpNgoc();
			break;
		}
		case 27:
		{
			x0 += -4;
			y0 += -2;
			if (questInfo == null)
			{
				questInfo = new mVector_HAIRMOD_PROTECTED();
			}
			setSize(134, 95, 1, questInfo.size(), 130, 15);
			ActionPerform_HAIRMOD_PROTECTED action5 = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().questClan(2);
				right.perform();
			};
			left = new Command_HAIRMOD_PROTECTED("Hủy");
			left.action = action5;
			break;
		}
		case 28:
		{
			int num5 = listEp.size() / 2;
			if (num5 < 6)
			{
				num5 = 6;
			}
			int num6 = 34;
			setSize(7 * wCell, 4 * wCell, num5, 3, num6, num6);
			setLimitCheDo();
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				doSelectedCheDo();
			};
			center = new Command_HAIRMOD_PROTECTED("Chọn");
			center.action = action2;
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				doLeftCheDo();
			};
			left = new Command_HAIRMOD_PROTECTED("Xong");
			left.action = action3;
			break;
		}
		case 29:
			try
			{
				setInfo(setListGuildSkill());
				setSize(126, 100, 1, 6, 126, 20);
				int num4 = 0;
				for (int j = 0; j < Canvas_HAIRMOD_PROTECTED.gameScr.skillClan.Length; j++)
				{
					if (Canvas_HAIRMOD_PROTECTED.gameScr.skillClan[j].time > 0)
					{
						num4++;
					}
				}
				for (int k = 0; k < Canvas_HAIRMOD_PROTECTED.gameScr.skillClanPrivate.Length; k++)
				{
					if (Canvas_HAIRMOD_PROTECTED.gameScr.skillClanPrivate[k].time > 0)
					{
						num4++;
					}
				}
				if (num4 < 5)
				{
					num4 = 5;
				}
				isChangeTab = true;
			}
			catch (Exception)
			{
				Out_HAIRMOD_PROTECTED.println("Loi goi skill ban hoi");
			}
			break;
		case 30:
			setInfo(setListGuildSkill());
			setSize(126, 100, 1, 6, 126, 20);
			isChangeTab = true;
			break;
		case 31:
			xKhung = 10;
			yKhung = 20;
			x0 = xKhung + 15;
			y0 = yKhung + MainJ2ME_HAIRMOD_PROTECTED.hTab;
			setSize(117, 104, 3, 5, 97, 21);
			break;
		case 32:
		{
			x0++;
			y0 += -4;
			setSize(126, 108, 5, 1, 20, 18);
			posEpNgoc = new short[5][]
			{
				new short[2] { 0, -10 },
				new short[2]
				{
					(short)(5 * wCell),
					-10
				},
				new short[2]
				{
					(short)(2 * wCell + wCell / 2),
					(short)(2 * wCell - wCell / 2 - 10)
				},
				new short[2]
				{
					0,
					(short)(4 * wCell - 30)
				},
				new short[2]
				{
					(short)(5 * wCell),
					(short)(4 * wCell - 30)
				}
			};
			for (int i = 0; i < itemEpdothu.Length; i++)
			{
				itemEpdothu[i] = null;
			}
			getItemAnimalFromList();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (numW != 0)
				{
					int num13 = listItemAnimal.size() + listTemp.size() - 1;
					if (selected % numW >= 0 && selected % numW < num13)
					{
						doSelectedEpDothu();
					}
				}
			};
			listEp = new mVector_HAIRMOD_PROTECTED();
			center = new Command_HAIRMOD_PROTECTED();
			center.action = action;
			doLeftEpDothu();
			break;
		}
		}
		if (selected >= numH * numW - 1)
		{
			selected = 0;
		}
	}

	private mVector_HAIRMOD_PROTECTED setListGuildSkill()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int aa = 0;
		try
		{
			for (int i = 0; i < Canvas_HAIRMOD_PROTECTED.gameScr.skillClan.Length; i++)
			{
				int ii2 = i;
				if (Canvas_HAIRMOD_PROTECTED.gameScr.skillClan[i].time <= 0)
				{
					continue;
				}
				int ind2 = aa;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
				};
				ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
				{
					GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(ii2 + 1600), x - wSmall / 2 + 9, y);
					int num3 = 0;
					if (ind2 == selected && !isChangeTab)
					{
						int width2 = MFont_HAIRMOD_PROTECTED.arialFont.getWidth(Canvas_HAIRMOD_PROTECTED.gameScr.skillClan[ii2].info);
						if (width2 > w - (wSmall / 2 + 16))
						{
							xCus += 2;
							if (xCus > width2 - (wSmall / 2 + 30))
							{
								xCus = -20;
							}
						}
						num3 = xCus;
						if (xCus < 0)
						{
							num3 = 0;
						}
					}
					MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.skillClan[ii2].info, x - wSmall / 2 + 18 - aa, y - MFont_HAIRMOD_PROTECTED.arialFont.getHeight() / 2, 0);
				};
				command_HAIRMOD_PROTECTED.action = action;
				command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				int num = aa;
				aa = num + 1;
			}
			for (int j = 0; j < Canvas_HAIRMOD_PROTECTED.gameScr.skillClanPrivate.Length; j++)
			{
				int ii = j;
				if (Canvas_HAIRMOD_PROTECTED.gameScr.skillClanPrivate[j].time <= 0)
				{
					continue;
				}
				int ind = aa;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED();
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
				};
				ActionPaintCmd_HAIRMOD_PROTECTED actionPaint2 = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
				{
					GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(ii + 1600), x - wSmall / 2 + 9, y);
					int num2 = 0;
					if (ind == selected && !isChangeTab)
					{
						int width = MFont_HAIRMOD_PROTECTED.arialFont.getWidth(Canvas_HAIRMOD_PROTECTED.gameScr.skillClan[ii].info);
						if (width > w - (wSmall / 2 + 16))
						{
							xCus += 2;
							if (xCus > width - (wSmall / 2 + 30))
							{
								xCus = -20;
							}
						}
						num2 = xCus;
						if (xCus < 0)
						{
							num2 = 0;
						}
					}
					MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.skillClanPrivate[ii].info, x - wSmall / 2 + 18 + aa, y - MFont_HAIRMOD_PROTECTED.arialFont.getHeight() / 2, 0);
				};
				command_HAIRMOD_PROTECTED2.action = action2;
				command_HAIRMOD_PROTECTED2.actionPaint = actionPaint2;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
				int num = aa;
				aa = num + 1;
			}
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println("null skill bang hoi setListGuildSkill");
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	protected void doLeftCheDo()
	{
		if (listEp.size() == 0)
		{
			return;
		}
		sbyte[][] array = new sbyte[listCheDo.size()][];
		for (int i = 0; i < array.Length; i++)
		{
			array[i] = new sbyte[6];
		}
		for (int j = 0; j < listEp.size(); j++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
			for (int k = 0; k < listCheDo.size(); k++)
			{
				Mineral_HAIRMOD_PROTECTED mineral_HAIRMOD_PROTECTED = (Mineral_HAIRMOD_PROTECTED)listCheDo.elementAt(k);
				int num = gemTemplate_HAIRMOD_PROTECTED.id - mineral_HAIRMOD_PROTECTED.idTemplate;
				if (num <= 5)
				{
					array[k][num] = (sbyte)gemTemplate_HAIRMOD_PROTECTED.number;
					break;
				}
			}
		}
		sbyte[][] ar = array;
		ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
		{
			for (int num2 = listEp.size() - 1; num2 >= 0; num2--)
			{
				GemTemplate_HAIRMOD_PROTECTED g = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(num2);
				removeItemCheDo(g);
			}
			GameService_HAIRMOD_PROTECTED.gI().doCheDo((short)listCheDo.size(), idTemp, ar);
			right.perform();
			Canvas_HAIRMOD_PROTECTED.startWaitDlg();
		};
		Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn hoàn thành không ?", yesAction);
	}

	private void removeItemCheDo(GemTemplate_HAIRMOD_PROTECTED g)
	{
		g.number--;
		if (g.number <= 0)
		{
			listEp.removeElement(g);
		}
		bool flag = false;
		for (int i = 0; i < listTemp.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED.id == g.id)
			{
				gemTemplate_HAIRMOD_PROTECTED.number++;
				flag = true;
				break;
			}
		}
		if (!flag)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(g.id);
			GemTemplate_HAIRMOD_PROTECTED.copyData(g, gemTemplate_HAIRMOD_PROTECTED2);
			gemTemplate_HAIRMOD_PROTECTED2.number = 1;
			listTemp.addElement(g);
		}
	}

	protected void doSelectedCheDo()
	{
		try
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = null;
			if (isSelectCheDo)
			{
				if (selected < listEp.size())
				{
					gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(selected);
				}
			}
			else if (selected < listTemp.size())
			{
				gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(selected);
			}
			if (gemTemplate_HAIRMOD_PROTECTED == null)
			{
				return;
			}
			GemTemplate_HAIRMOD_PROTECTED g = gemTemplate_HAIRMOD_PROTECTED;
			int xx = selected * newZise + cmx;
			int yy = selected * newZise;
			short idTem = gemTemplate_HAIRMOD_PROTECTED.id;
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(isSelectCheDo ? "Lấy ra" : "Bỏ vào");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (isSelectCheDo)
				{
					removeItemCheDo(g);
				}
				else
				{
					int num = 0;
					for (int i = 0; i < listCheDo.size(); i++)
					{
						Mineral_HAIRMOD_PROTECTED mineral_HAIRMOD_PROTECTED = (Mineral_HAIRMOD_PROTECTED)listCheDo.elementAt(i);
						if (g.id - mineral_HAIRMOD_PROTECTED.idTemplate >= 0 && g.id - mineral_HAIRMOD_PROTECTED.idTemplate <= 5)
						{
							for (int j = 0; j < listEp.size(); j++)
							{
								GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
								if (gemTemplate_HAIRMOD_PROTECTED2.id - mineral_HAIRMOD_PROTECTED.idTemplate >= 0 && gemTemplate_HAIRMOD_PROTECTED2.id - mineral_HAIRMOD_PROTECTED.idTemplate <= 5)
								{
									num += gemTemplate_HAIRMOD_PROTECTED2.number;
								}
							}
							if (num >= mineral_HAIRMOD_PROTECTED.number)
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã đủ số lượng.");
								return;
							}
						}
					}
					bool flag = false;
					for (int k = 0; k < listEp.size(); k++)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(k);
						if (gemTemplate_HAIRMOD_PROTECTED3.id == g.id)
						{
							gemTemplate_HAIRMOD_PROTECTED3.number++;
							flag = true;
							break;
						}
					}
					if (!flag)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = new GemTemplate_HAIRMOD_PROTECTED(g.id);
						GemTemplate_HAIRMOD_PROTECTED.copyData(g, gemTemplate_HAIRMOD_PROTECTED4);
						gemTemplate_HAIRMOD_PROTECTED4.number = 1;
						listEp.addElement(gemTemplate_HAIRMOD_PROTECTED4);
					}
					g.number--;
					if (g.number <= 0)
					{
						listTemp.removeElement(g);
					}
				}
				setLimitCheDo();
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Thông tin");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if (khoaCheDo == 0 || khoaCheDo == 2)
				{
					showGemItemInfo(idTem, xx, yy);
				}
				else
				{
					showGemItemKhoaInfo(idTem, xx, yy);
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			command_HAIRMOD_PROTECTED2.perform();
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
		}
		catch (Exception)
		{
		}
	}

	public void setImageCharWear()
	{
		int num = 0;
		for (int i = 0; i < charWearing.wearingItems.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item.plus > 13)
			{
				num++;
			}
		}
		if (num < 3 || charWearing.idFashion != -1)
		{
			return;
		}
		int[] array = new int[6000];
		for (int j = 0; j < array.Length; j++)
		{
			if (array[j] != 16777215)
			{
				if (j - 1 > 0 && array[j - 1] == 16777215)
				{
					array[j] = -1;
				}
				else if (j + 1 < array.Length && array[j + 1] == 16777215)
				{
					array[j] = -1;
				}
			}
		}
	}

	private void doSelectedEpNgoc()
	{
		try
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			if (numH != 1)
			{
				if (selected % numW < listTemp.size())
				{
					GemTemplate_HAIRMOD_PROTECTED item = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(selected % numW);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Bỏ vào");
					ActionPerform_HAIRMOD_PROTECTED action = delegate
					{
						if (listEp.size() < 5)
						{
							item.number--;
							if (item.number <= 0)
							{
								listTemp.removeElement(item);
							}
							RealID_HAIRMOD_PROTECTED o = new RealID_HAIRMOD_PROTECTED(item.rID)
							{
								ID = item.id
							};
							listEp.addElement(o);
						}
					};
					command_HAIRMOD_PROTECTED.action = action;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
					mVector_HAIRMOD_PROTECTED2.addElement(addGemInfo(item, selected % numW * 20 + 4, 86));
				}
			}
			else if (selected < listEp.size())
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Lấy ra");
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = null;
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = null;
					for (int i = 0; i < listEp.size(); i++)
					{
						realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)listEp.elementAt(i);
						gemTemplate_HAIRMOD_PROTECTED = GemTemplate_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID);
						gemTemplate_HAIRMOD_PROTECTED.number++;
						realID_HAIRMOD_PROTECTED = null;
					}
					listEp.removeAllElements();
				};
				command_HAIRMOD_PROTECTED2.action = action2;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
		}
		catch (Exception)
		{
		}
	}

	private Command_HAIRMOD_PROTECTED addGemInfo(GemTemplate_HAIRMOD_PROTECTED item, int xx, int yy)
	{
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Thông tin");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (khoaDoThu == 0 || khoaDoThu == 2)
			{
				showGemItemInfo(item.id, 145, -10);
			}
			else
			{
				showGemItemKhoaInfo(item.id, 145, -10);
			}
		};
		command_HAIRMOD_PROTECTED.action = action;
		return command_HAIRMOD_PROTECTED;
	}

	private void doLeftEpNgoc()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (listEp.size() < 3)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn phải ép ít nhất 3 vật phẩm.");
			}
			else
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().epNgoc(1, listEp);
					Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang hợp..", false);
					listEp.removeAllElements();
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn phải mất " + Canvas_HAIRMOD_PROTECTED.getMoneys(prizeImbue) + "Xu để hợp.", yesAction);
			}
		};
		left = new Command_HAIRMOD_PROTECTED("Hợp");
		left.action = action;
	}

	public void setLeftBuyItem()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (selected < sellItem.size())
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					if (sellItem.elementAt(selected) is ItemInInventory_HAIRMOD_PROTECTED)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(selected);
						GameService_HAIRMOD_PROTECTED.gI().requestBuyItemUser(idCharSell, idSeller, shopIdSell, itemInInventory_HAIRMOD_PROTECTED.itemID, 0);
					}
					else
					{
						RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)sellItem.elementAt(selected);
						GameService_HAIRMOD_PROTECTED.gI().requestBuyItemUser(idCharSell, idSeller, shopIdSell, realID_HAIRMOD_PROTECTED.realID, 1);
					}
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn mua không ?", yesAction);
			}
		};
		left = new Command_HAIRMOD_PROTECTED("Mua");
		left.action = action;
	}

	public mVector_HAIRMOD_PROTECTED getListBuyItem()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < sellItem.size(); i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (sellItem.elementAt(ii) is ItemInInventory_HAIRMOD_PROTECTED)
				{
					if (isDefault)
					{
						setShowTextNew(((ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(ii)).getInfoItemUserSell(), ii % numW * wSmall, ii / numW * wSmall);
					}
					else if ((ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(ii) != null)
					{
						setShowText(((ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(ii)).getInfoItemUserSell(), ii % numW * wSmall, ii / numW * wSmall);
					}
				}
				else
				{
					showRealIDInfo((RealID_HAIRMOD_PROTECTED)sellItem.elementAt(ii), ii % numW * wSmall, ii / numW * wSmall);
				}
			};
			ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
			{
				if (sellItem.elementAt(ii) is ItemInInventory_HAIRMOD_PROTECTED)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(ii);
					if (itemInInventory_HAIRMOD_PROTECTED.isSelling)
					{
						g.setColor(7706352);
						g.fillRect(x - 7, y - 7, 14, 14);
					}
					itemInInventory_HAIRMOD_PROTECTED.paint(g, x, y);
				}
				else
				{
					Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(((RealID_HAIRMOD_PROTECTED)sellItem.elementAt(ii)).ID).idImage, x, y);
				}
			};
			command_HAIRMOD_PROTECTED.action = action;
			command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	private void setLeftKeepAndSellItem()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			int num = selected / numW * 3 + selected % numW;
			ItemInInventory_HAIRMOD_PROTECTED item = null;
			if (selected % numW < 3)
			{
				if (num < ItemShopNew.size())
				{
					item = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(num);
					if (index[focusTab] == 22)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.gameService.putItem2Bag(item.itemID);
					}
					else if (index[focusTab] == 23)
					{
						if (item.isSelling)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Vật phẩm đang được bán.");
						}
						else
						{
							setLeftSellItem(item, -1);
						}
					}
				}
				else if (num < ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size() && index[focusTab] == 23)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(num - ItemShopNew.size());
					setLeftSellItem(item, gemTemplate_HAIRMOD_PROTECTED.rID);
				}
			}
			else
			{
				closeText();
				if (index[focusTab] == 22 && num - 3 < Char_HAIRMOD_PROTECTED.itembag.size())
				{
					item = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.itembag.elementAt(num - 3);
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.getItemFromBag(item.itemID);
				}
				else if (index[focusTab] == 23 && num - 3 < sellItem.size())
				{
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = null;
					if (sellItem.elementAt(num - 3) is ItemInInventory_HAIRMOD_PROTECTED)
					{
						item = (ItemInInventory_HAIRMOD_PROTECTED)sellItem.elementAt(num - 3);
					}
					else
					{
						realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)sellItem.elementAt(num - 3);
					}
					doRequestSellItem(item, realID_HAIRMOD_PROTECTED?.realID ?? (-1), 0, false);
				}
			}
		};
		left = new Command_HAIRMOD_PROTECTED((index[focusTab] != 22) ? "Chọn" : "Chuyển");
		left.action = action;
	}

	protected void setLeftSellItem(ItemInInventory_HAIRMOD_PROTECTED item, short gem)
	{
		closeText();
		ActionPerform_HAIRMOD_PROTECTED ok = delegate
		{
			try
			{
				string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
				if (!text.Equals(string.Empty))
				{
					int num = int.Parse(text);
					if (num >= 0)
					{
						doRequestSellItem(item, gem, num, true);
					}
				}
			}
			catch (Exception)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Vui lòng chỉ nhập số.");
			}
		};
		Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập giá muốn bán: ", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC, 10, true);
		Canvas_HAIRMOD_PROTECTED.inputDlg.show();
	}

	public void doRequestSellItem(ItemInInventory_HAIRMOD_PROTECTED item, short gem, int prize, bool sell)
	{
		GameService_HAIRMOD_PROTECTED.gI().requestSellItem(idSeller, shopIdSell, item, gem, prize, sell);
		Canvas_HAIRMOD_PROTECTED.startWaitDlg();
	}

	private void setSelectedKeepAndSellItem()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			isFcStore = false;
			int num = selected / numW * 3 + selected % numW;
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
			if (selected % numW < 3)
			{
				if (num < ItemShopNew.size())
				{
					itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(num);
					if (itemInInventory_HAIRMOD_PROTECTED != null)
					{
						showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED, isSell, selected % numW * wSmall, selected / numW * hSmall - cmy);
						isFcStore = true;
					}
				}
				else if (num < ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size())
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(num - ItemShopNew.size());
					if (gemTemplate_HAIRMOD_PROTECTED != null)
					{
						showGemItemInfo(gemTemplate_HAIRMOD_PROTECTED.id, selected % numW * wSmall, selected / numW * hSmall - cmy);
						isFcStore = true;
					}
				}
			}
			else
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = null;
				mVector_HAIRMOD_PROTECTED2 = ((index[focusTab] != 22) ? sellItem : Char_HAIRMOD_PROTECTED.itembag);
				if (num - 3 < mVector_HAIRMOD_PROTECTED2.size())
				{
					if (mVector_HAIRMOD_PROTECTED2.elementAt(num - 3) is ItemInInventory_HAIRMOD_PROTECTED)
					{
						itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(num - 3);
						showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED, isSell, selected % numW * wSmall, selected / numW * hSmall - cmy);
					}
					else
					{
						showRealIDInfo((RealID_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(num - 3), selected % numW * wSmall, selected / numW * hSmall - cmy);
					}
				}
			}
		};
		center = new Command_HAIRMOD_PROTECTED();
		center.action = action;
	}

	public void setListImbue(bool isEp, int type, int khoa)
	{
		listTemp.removeAllElements();
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = ((khoa != 0) ? MainChar_HAIRMOD_PROTECTED.gemItemKhoa : MainChar_HAIRMOD_PROTECTED.gemItem);
		for (int i = 0; i < mVector_HAIRMOD_PROTECTED2.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(i);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			if (!isEp)
			{
				if (gemByID.type == type)
				{
					listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED);
				}
			}
			else if (gemByID.typeEp == type)
			{
				listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED);
			}
		}
		if (listTemp.size() == 0)
		{
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				right.perform();
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Không có nguyên liệu.", action);
		}
	}

	private void doLeftImbue()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.itemImbue != null)
			{
				if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.hadPutItemImbue())
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
					{
						if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu < prizeImbue)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Không đủ tiền");
						}
						else
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu -= prizeImbue;
							if (index[focusTab] == 21)
							{
								GameService_HAIRMOD_PROTECTED.gI().doImbueItem(1);
							}
							else
							{
								GameService_HAIRMOD_PROTECTED.gI().doKhamItem(1);
							}
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						}
					};
					ActionPerform_HAIRMOD_PROTECTED noAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.currentDialog = null;
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn phải mất " + prizeImbue + " xu để luyện.", yesAction, noAction);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg((index[focusTab] == 21) ? "Chưa đặt luyện kim dược." : "Chưa đặt ngọc");
				}
			}
		};
		left = new Command_HAIRMOD_PROTECTED((index[focusTab] != 21) ? "Xong" : "Đập");
		left.action = action;
	}

	public void setSelectedImbue()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int index = selected / numW;
		int xx = selected % numW;
		int num = index * 2 + xx / 2;
		if ((index == 4 && (idImbue == -1 || xx >= ItemShopNew.size())) || (index != 4 && ((xx == 1 && idImbue != -1) || (xx != 1 && num < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size()))))
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED((index == 4) ? "Bỏ vào" : "Lấy ra");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (index == 4)
				{
					if (xx < ItemShopNew.size())
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(xx);
						if (itemInInventory_HAIRMOD_PROTECTED.dayUse > 0)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Không được đập đồ thuê.");
							return;
						}
						if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.itemImbue == null && !itemInInventory_HAIRMOD_PROTECTED.isSelling)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.itemImbue = itemInInventory_HAIRMOD_PROTECTED;
							ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
							if (item.type < 3 || item.type > 7)
							{
								imgWeaponAvatar = GameData_HAIRMOD_PROTECTED.getImgIcon(item.idIcon).img;
							}
							else if (item.type >= 3 && item.type < 8)
							{
								GameService_HAIRMOD_PROTECTED.gI().doRequestWeapone(2, item.type, item.style, (sbyte)item.index);
								Canvas_HAIRMOD_PROTECTED.startWaitDlg();
							}
						}
						idImbue = xx;
						GameService_HAIRMOD_PROTECTED.gI().addItemImbue(itemInInventory_HAIRMOD_PROTECTED.itemID);
					}
					else
					{
						try
						{
							if (MainChar_HAIRMOD_PROTECTED.gemitemImbue.size() >= 8)
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể thêm");
								return;
							}
							int num2 = xx - ItemShopNew.size();
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(num2);
							RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = new RealID_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.rID)
							{
								ID = gemTemplate_HAIRMOD_PROTECTED.id
							};
							bool flag = false;
							if (WindowInfoScr_HAIRMOD_PROTECTED.index[focusTab] == 21 && MainChar_HAIRMOD_PROTECTED.gemitemImbue.size() == 0 && (realID_HAIRMOD_PROTECTED.ID <= 7 || realID_HAIRMOD_PROTECTED.ID == 155 || realID_HAIRMOD_PROTECTED.ID == 156))
							{
								flag = true;
							}
							if (!flag)
							{
								if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.countBaoHiem() >= 1 && gemTemplate_HAIRMOD_PROTECTED.id < 5)
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Chỉ có thể sử dụng 1 loại bảo hiểm cho 1 lần luyện.");
									return;
								}
								if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.countLKD() >= 1 && gemTemplate_HAIRMOD_PROTECTED.id >= 8 && gemTemplate_HAIRMOD_PROTECTED.id != 155 && gemTemplate_HAIRMOD_PROTECTED.id != 156)
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể sử dụng 2 loại ngọc cho 1 lần luyện.");
									return;
								}
								MainChar_HAIRMOD_PROTECTED.gemitemImbue.addElement(realID_HAIRMOD_PROTECTED);
								gemTemplate_HAIRMOD_PROTECTED.number--;
								if (gemTemplate_HAIRMOD_PROTECTED.number == 0)
								{
									listTemp.removeElement(gemTemplate_HAIRMOD_PROTECTED);
								}
								GameService_HAIRMOD_PROTECTED.gI().addGemItemImbue(realID_HAIRMOD_PROTECTED.realID, 0);
							}
							else
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Phải đặt luyện kim dược trước.");
							}
						}
						catch (Exception)
						{
						}
					}
				}
				else if (xx == 1)
				{
					if (idImbue != -1)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(idImbue);
						GameService_HAIRMOD_PROTECTED.gI().addItemImbue(itemInInventory_HAIRMOD_PROTECTED2.itemID);
						idImbue = -1;
						Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.itemImbue = null;
						imgWeaponAvatar = null;
					}
				}
				else
				{
					int num3 = index * 2 + xx / 2;
					if (num3 < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size())
					{
						RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED2 = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(num3);
						MainChar_HAIRMOD_PROTECTED.gemitemImbue.removeElement(realID_HAIRMOD_PROTECTED2);
						int num4 = 0;
						for (int i = 0; i < listTemp.size(); i++)
						{
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
							if (gemTemplate_HAIRMOD_PROTECTED2.id == realID_HAIRMOD_PROTECTED2.ID)
							{
								gemTemplate_HAIRMOD_PROTECTED2.number++;
								num4 = 1;
								break;
							}
						}
						if (num4 == 0)
						{
							GemTemplate_HAIRMOD_PROTECTED o = new GemTemplate_HAIRMOD_PROTECTED(realID_HAIRMOD_PROTECTED2.ID)
							{
								rID = realID_HAIRMOD_PROTECTED2.realID
							};
							listTemp.addElement(o);
						}
						GameService_HAIRMOD_PROTECTED.gI().addGemItemImbue(realID_HAIRMOD_PROTECTED2.realID, 1);
					}
				}
				int num5 = selected % numW;
				int num6 = selected / numW;
				numW = ItemShopNew.size() + listTemp.size();
				if (numW < 6)
				{
					numW = 6;
				}
				if (num5 >= numW)
				{
					num5 = numW - 1;
				}
				selected = num6 * numW + num5;
				cmxLim = numW * wSmall - w + 6;
				if (cmxLim < 0)
				{
					cmxLim = 0;
				}
				if (cmx > cmxLim)
				{
					cmx = (cmtoX = cmxLim);
				}
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		if (index == 4 || (xx != 1 && num < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size()) || (idImbue != -1 && xx == 1))
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Thông tin");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				doShowItemInfoImbue();
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			command_HAIRMOD_PROTECTED2.perform();
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
		idfcsetSelectedImbue = xx;
	}

	public void doShowItemInfoImbue()
	{
		int num = selected / numW;
		int num2 = selected % numW;
		string empty = string.Empty;
		if (num == 4)
		{
			if (num2 < ItemShopNew.size())
			{
				ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(num2);
				showItemInventoryInfo(item, isSell, num2 * wSmall, 4 * wSmall);
				return;
			}
			for (int i = 0; i < listTemp.size(); i++)
			{
				if (i == num2 - ItemShopNew.size())
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
					GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
					empty = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
					if ((khoaImbue == 1 && WindowInfoScr_HAIRMOD_PROTECTED.index[focusTab] == 21) || (khoaKham == 1 && WindowInfoScr_HAIRMOD_PROTECTED.index[focusTab] == 25))
					{
						empty += " - Đã khóa";
					}
					empty += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
					setShowTextNew(empty, 145, -10);
				}
			}
		}
		else if (num2 == 1)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(idImbue);
			setShowTextNew(itemInInventory_HAIRMOD_PROTECTED.getDescription(isSell), 145, -10);
		}
		else
		{
			int num3 = num * 2 + num2 / 2;
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(num3);
			GemTemp_HAIRMOD_PROTECTED gemByID2 = Res_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID);
			string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID2.name, "0");
			if ((khoaImbue == 1 && WindowInfoScr_HAIRMOD_PROTECTED.index[focusTab] == 21) || (khoaKham == 1 && WindowInfoScr_HAIRMOD_PROTECTED.index[focusTab] == 25))
			{
				text += " - Đã khóa";
			}
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID2.decript, "0");
			setShowTextNew(text, 145, -10);
		}
	}

	public bool isBuffSkill()
	{
		sbyte[][] array = new sbyte[5][]
		{
			new sbyte[2] { 4, 5 },
			new sbyte[2] { 4, 5 },
			new sbyte[4] { 4, 5, 6, 7 },
			new sbyte[2] { 4, 5 },
			new sbyte[2] { 4, 5 }
		};
		for (int i = 0; i < array[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz].Length; i++)
		{
			if (selected == array[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][i])
			{
				return true;
			}
		}
		return false;
	}

	private void doSelectedSkill()
	{
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected] == -1)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin gặp Lâm tướng quân để học kỹ năng này");
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		bool flag = false;
		bool isBuff = isBuffSkill();
		if (isBuff)
		{
			flag = SkillManager_HAIRMOD_PROTECTED.SKILL_CAN_BUFF_TO_USER[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][selected - 4] == -1;
		}
		if (isBuff && !flag)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Sử dụng");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected] > 0)
				{
					Canvas_HAIRMOD_PROTECTED.menu.showMenu = false;
					int num = SkillManager_HAIRMOD_PROTECTED.EFF_BUFF_SKILL[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][selected - 4];
					int skillMP = SkillManager_HAIRMOD_PROTECTED.getSkillMP(selected, Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected - 4], Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz);
					if (skillMP > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.mp)
					{
						if (!first)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Không đủ MP", 0));
							first = true;
						}
					}
					else
					{
						first = false;
						if (Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor != null && Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
						{
							if (selected == 6)
							{
								GameService_HAIRMOD_PROTECTED.gI().useBuff(Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor.ID, 0, (sbyte)num, 0);
								Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.mp -= skillMP;
							}
							else
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.mp -= skillMP;
								if (isBuff && (Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == -1 || Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == selected))
								{
									Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff = selected;
								}
								doCastBuffToActor(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar, Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor, num, SkillManager_HAIRMOD_PROTECTED.SKILL_CAN_BUFF_TO_USER[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][selected - 4] == 1);
							}
						}
						else if (selected != 6)
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.mp -= skillMP;
							if (isBuff && (Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == -1 || Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == selected))
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff = selected;
							}
							GameService_HAIRMOD_PROTECTED.gI().useBuff(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID, 0, (sbyte)num, 0);
						}
						else
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
						}
					}
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa học kĩ năng này");
				}
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		if (!flag)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Cho vào phím tắt");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected] > 0)
				{
					doGiveSkillToQuickSlot(selected, isBuff);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa học kỹ năng này");
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		}
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Cộng");
		ActionPerform_HAIRMOD_PROTECTED action3 = delegate
		{
			if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.skillPointLeft == 0)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã hết điểm kỹ năng để cộng. Xin đánh lên level để có điểm kỹ năng.");
			}
			else if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.level < SkillManager_HAIRMOD_PROTECTED.getLvAddSkill(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz, selected, Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected]))
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa đủ cấp độ để cộng. Xin đánh lên level yêu cầu để có thể cộng.");
			}
			else
			{
				GameService_HAIRMOD_PROTECTED.gI().addSkillPoint(selected);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg("Xin chờ..", true);
			}
		};
		command_HAIRMOD_PROTECTED3.action = action3;
		mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
	}

	protected void doGiveSkillToQuickSlot(int skillType, bool isBuff)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < 3; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Phím số " + (1 + i * 2));
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				MainChar_HAIRMOD_PROTECTED.quickSlot[GameScr_HAIRMOD_PROTECTED.indexTabSlot][ii].setIsSkill(skillType, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz, isBuff);
				RMS_HAIRMOD_PROTECTED.saveQuickSlot();
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
	}

	public void doCastBuffToActor(MainChar_HAIRMOD_PROTECTED actor1, Actor_HAIRMOD_PROTECTED actor2, int skillIdEff, bool buff2Friend)
	{
		if (actor1 == null || actor2 == null)
		{
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if (buff2Friend)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Cho mình");
			command_HAIRMOD_PROTECTED.action = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().useBuff(actor1.ID, 0, (sbyte)skillIdEff, 0);
			};
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Cho bạn");
			command_HAIRMOD_PROTECTED2.action = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().useBuff(actor2.ID, 0, (sbyte)skillIdEff, 0);
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		}
		else
		{
			GameService_HAIRMOD_PROTECTED.gI().useBuff(actor1.ID, 0, (sbyte)skillIdEff, 0);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
	}

	public void cmdCentertab2125()
	{
		Out_HAIRMOD_PROTECTED.println("set cmd center   ..");
		switch (index[focusTab])
		{
		case 21:
		{
			center = null;
			center = new Command_HAIRMOD_PROTECTED("Chọn");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				setSelectedImbue();
			};
			center.action = action3;
			break;
		}
		case 25:
		{
			center = new Command_HAIRMOD_PROTECTED("Chọn");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				setSelectedImbue();
			};
			center.action = action2;
			break;
		}
		case 31:
		{
			center = new Command_HAIRMOD_PROTECTED("Chọn");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (charWearing != null && (charWearing.useHorse != -1 || charWearing.idhorse > -1))
				{
					showInfoWearingAnimal();
				}
			};
			center.action = action;
			break;
		}
		}
	}

	public void setCmdCenter()
	{
		charInfo = null;
		if (index[focusTab] == 21 || index[focusTab] == 25 || index[focusTab] == 31)
		{
			cmdCentertab2125();
			return;
		}
		switch (index[focusTab])
		{
		case 1:
		{
			ActionPerform_HAIRMOD_PROTECTED action10 = delegate
			{
				showCharWearingInfo();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action10;
			break;
		}
		case 2:
		{
			ActionPerform_HAIRMOD_PROTECTED action8 = delegate
			{
				dofireBasePoint();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action8;
			ActionPerform_HAIRMOD_PROTECTED action9 = delegate
			{
				dofireBasePoint();
			};
			left = new Command_HAIRMOD_PROTECTED("Chọn");
			left.action = action9;
			break;
		}
		case 3:
		{
			cmyLim = (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.getInfoSkill(selected).Length + 1) * 12 - 72;
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
			ActionPerform_HAIRMOD_PROTECTED action5 = delegate
			{
				doSelectedSkill();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action5;
			ActionPerform_HAIRMOD_PROTECTED action6 = delegate
			{
				doSelectedSkill();
			};
			left = new Command_HAIRMOD_PROTECTED("Chọn");
			left.action = action6;
			break;
		}
		case 4:
			setTextCharInfo();
			break;
		case 5:
		{
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				doSelectedParty();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action3;
			ActionPerform_HAIRMOD_PROTECTED action4 = delegate
			{
				doSelectedParty();
			};
			left = new Command_HAIRMOD_PROTECTED("Chọn");
			left.action = action4;
			break;
		}
		case 6:
		{
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				doSelectedQuest();
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action;
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				doSelectedQuest();
			};
			left = new Command_HAIRMOD_PROTECTED("Chọn");
			left.action = action2;
			break;
		}
		case 7:
		{
			newSkill = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED news = (mVector_HAIRMOD_PROTECTED)GameScr_HAIRMOD_PROTECTED.infoNewSkill.elementAt(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz);
			if (news == null)
			{
				break;
			}
			InfoSkillLearn_HAIRMOD_PROTECTED infoSkillLearn_HAIRMOD_PROTECTED = (InfoSkillLearn_HAIRMOD_PROTECTED)news.elementAt(selected);
			if (infoSkillLearn_HAIRMOD_PROTECTED == null)
			{
				break;
			}
			sbyte b = Char_HAIRMOD_PROTECTED.skillLevelLearnt[infoSkillLearn_HAIRMOD_PROTECTED.idSkill];
			newSkill.addElement("`" + ((InfoSkillLearn_HAIRMOD_PROTECTED)news.elementAt(selected)).name);
			if (b == -1)
			{
				newSkill.addElement("`Phí: " + ((InfoSkillLearn_HAIRMOD_PROTECTED)news.elementAt(selected)).price + " xu");
			}
			string[] array = MFont_HAIRMOD_PROTECTED.arialFont.splitFontBStrInLine(((InfoSkillLearn_HAIRMOD_PROTECTED)news.elementAt(selected)).decript, 110);
			for (int i = 0; i < array.Length; i++)
			{
				newSkill.addElement(array[i]);
			}
			setSize(114, 30, news.size(), 2, 30, 20);
			cmyLim = newSkill.size() * 12 - 72;
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
			ActionPerform_HAIRMOD_PROTECTED action7 = delegate
			{
				InfoSkillLearn_HAIRMOD_PROTECTED infoSkillLearn_HAIRMOD_PROTECTED2 = (InfoSkillLearn_HAIRMOD_PROTECTED)news.elementAt(selected);
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().learnSkill(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz, selected);
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn muốn học kỹ năng " + infoSkillLearn_HAIRMOD_PROTECTED2.name + " với giá " + infoSkillLearn_HAIRMOD_PROTECTED2.price + "xu không? ", yesAction);
			};
			center = new Command_HAIRMOD_PROTECTED(string.Empty);
			center.action = action7;
			break;
		}
		case 8:
		case 26:
		case 27:
		case 29:
		case 30:
		case 32:
			break;
		case 22:
		case 23:
			setSelectedKeepAndSellItem();
			break;
		case 28:
			setLimitCheDo();
			break;
		default:
		{
			int num = selected + ((index[focusTab] == 0) ? (countTabMyitem * 42) : 0);
			center = null;
			if (num < list.size())
			{
				center = (Command_HAIRMOD_PROTECTED)list.elementAt(num);
			}
			break;
		}
		}
	}

	private void setLimitCheDo()
	{
		int num = selected % numW;
		if (selected / numW == 1)
		{
			int num2 = listEp.size() / 2;
			if (num2 < 6)
			{
				num2 = 6;
			}
			numW = num2;
			if (numW < 6)
			{
				numW = 6;
			}
			if (num >= numW)
			{
				num = numW - 1;
			}
			selected = numW + num;
		}
		else if (selected / numW == 2)
		{
			numW = listTemp.size();
			if (numW < 6)
			{
				numW = 6;
			}
			if (num >= numW)
			{
				num = numW - 1;
			}
			selected = 2 * numW + num;
		}
		int num3 = listTemp.size();
		cmxLim = num3 * newZise - w;
		if (cmxLim <= 0)
		{
			cmxLim = 0;
		}
	}

	public void doViewInfoItemTradeup()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		object obj = null;
		int num = selectedGD % 6;
		if (num <= 2)
		{
			int num2 = selectedGD / 6;
			int cc = num2 * 3 + num;
			mVector_HAIRMOD_PROTECTED tItem = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.tItems;
			if (cc < tItem.size())
			{
				object it2;
				obj = (it2 = tItem.elementAt(cc));
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Hủy");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					if (tItem.elementAt(cc) is ItemInInventory_HAIRMOD_PROTECTED)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendAddItemTrade(((ItemInInventory_HAIRMOD_PROTECTED)it2).itemID, 0, 0);
					}
					else
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendAddItemTrade(((Potion_HAIRMOD_PROTECTED)it2).id, 2, 0);
					}
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			}
		}
		else
		{
			int num3 = selectedGD / 6;
			int num4 = num3 * 3 + (num - 3);
			if (num4 < Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.rItems.size())
			{
				obj = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.rItems.elementAt(num4);
			}
		}
		if (obj != null)
		{
			int x1 = num * 22;
			int y1 = selectedGD / 6 * 22;
			object it = obj;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Thông tin");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if (it is ItemInInventory_HAIRMOD_PROTECTED)
				{
					showItemInventoryInfo((ItemInInventory_HAIRMOD_PROTECTED)it, isSell, x1, y1);
				}
				else
				{
					int num5 = 0;
					for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.listPotion.Length; i++)
					{
						if (MainChar_HAIRMOD_PROTECTED.listPotion[i].index == ((Potion_HAIRMOD_PROTECTED)it).index)
						{
							setShowTextNew(getPotionInfo(num5), x1, y1);
							num5++;
							break;
						}
					}
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			command_HAIRMOD_PROTECTED2.perform();
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	public void doAdditemTrade()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if (selected < ItemShopNew.size())
		{
			bool flag = false;
			ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(selected);
			mVector_HAIRMOD_PROTECTED tItems = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.tItems;
			for (int i = 0; i < tItems.size(); i++)
			{
				if (tItems.elementAt(i) is ItemInInventory_HAIRMOD_PROTECTED)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)tItems.elementAt(i);
					if (itemInInventory_HAIRMOD_PROTECTED.itemID == item.itemID)
					{
						flag = true;
						break;
					}
				}
			}
			if (!flag)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Giao dịch");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.tItems.size() >= 12)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể thêm.");
					}
					else if (!item.isSelling)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendAddItemTrade(item.itemID, 0, 0);
					}
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Thông tin");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if (!item.isEnoughData)
				{
					item.isEnoughData = true;
					GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(item.itemID, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
				}
				else
				{
					setShowTextNew(item.getDescription(false), selected * 18, h);
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			command_HAIRMOD_PROTECTED2.perform();
		}
		else
		{
			int num = selected - ItemShopNew.size();
			int num2 = 0;
			for (int j = 0; j < MainChar_HAIRMOD_PROTECTED.listPotion.Length; j++)
			{
				int ii = j;
				if (MainChar_HAIRMOD_PROTECTED.listPotion[j].number - MainChar_HAIRMOD_PROTECTED.listPotion[j].numTrade <= 0 || !MainChar_HAIRMOD_PROTECTED.listPotion[j].isTrade)
				{
					continue;
				}
				int bb = num2;
				if (num2 == num)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Giao dịch");
					ActionPerform_HAIRMOD_PROTECTED action3 = delegate
					{
						ActionPerform_HAIRMOD_PROTECTED ok = delegate
						{
							try
							{
								int num3 = int.Parse(Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
								if (num3 > 0 && num3 <= MainChar_HAIRMOD_PROTECTED.listPotion[ii].number - MainChar_HAIRMOD_PROTECTED.listPotion[ii].numTrade)
								{
									Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendAddItemTrade((short)ii, 1, num3);
									Canvas_HAIRMOD_PROTECTED.endDlg();
								}
							}
							catch (Exception)
							{
							}
						};
						Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Số lượng", ok, TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY, 10, true);
						Canvas_HAIRMOD_PROTECTED.inputDlg.show();
					};
					command_HAIRMOD_PROTECTED3.action = action3;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Thông tin");
					ActionPerform_HAIRMOD_PROTECTED action4 = delegate
					{
						setShowTextNew(getPotionInfo(ii), bb % numW * 18, bb / numW * 18);
					};
					command_HAIRMOD_PROTECTED4.action = action4;
					command_HAIRMOD_PROTECTED4.perform();
					break;
				}
				num2++;
			}
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	protected void doSelectedTrade(sbyte id)
	{
		switch (id)
		{
		case 0:
			doViewInfoItemTradeup();
			break;
		case 1:
			doAdditemTrade();
			break;
		}
	}

	protected void doSelectedQuest()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.receiveQuest)
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.resetQuest();
				Canvas_HAIRMOD_PROTECTED.gameScr.dellPotionQuest();
				GameService_HAIRMOD_PROTECTED.gI().receiveQuest(0, 0, 3);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn thật sự muốn từ bỏ nhiệm vụ này?", yesAction);
		}
	}

	protected void doBuyitemshopnew()
	{
		ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
		{
			GameService_HAIRMOD_PROTECTED.gI().dobuyitemShopnew((sbyte)selected, typeshop);
		};
		Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn " + text + " vật phẩm này không ?", yesAction);
	}

	protected void doSelectedParty()
	{
		try
		{
			if (Char_HAIRMOD_PROTECTED.party.size() <= 0)
			{
				return;
			}
			MainChar_HAIRMOD_PROTECTED mainChar = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar;
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			if (mainChar.ID == mainChar.IDMasterParty)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Đuổi");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.kickOutParty(((PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(selected)).id, 0);
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Giải tán");
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.kickOutParty(0, 1);
				};
				command_HAIRMOD_PROTECTED2.action = action2;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
			}
			else
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Rời nhóm");
				ActionPerform_HAIRMOD_PROTECTED action3 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.gameService.kickOutParty(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID, 2);
				};
				command_HAIRMOD_PROTECTED3.action = action3;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
		}
		catch (Exception)
		{
		}
	}

	private void setTextCharInfo()
	{
		charInfo = new string[15];
		MainChar_HAIRMOD_PROTECTED mainChar = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar;
		charInfo[0] = "Nhân vật: " + mainChar.name;
		charInfo[1] = "Level: " + mainChar.level + "+" + mainChar.getPercent();
		charInfo[2] = "HP: " + mainChar.hp + "/" + mainChar.maxhp;
		charInfo[3] = "MP: " + mainChar.mp + "/" + mainChar.maxmp;
		charInfo[4] = "Tấn công: " + mainChar.getAttack();
		charInfo[5] = "Thủ vật lý: " + mainChar.defend;
		charInfo[6] = "Thủ ma pháp: " + mainChar.defend_magic;
		charInfo[7] = "Chính xác: " + mainChar.accurate;
		charInfo[8] = "Né tránh: " + mainChar.dodge;
		charInfo[9] = "Bạo kích: " + ((mainChar.baokich <= 0) ? "0" : (mainChar.baokich / 10 + "." + mainChar.baokich % 10)) + "%";
		charInfo[10] = "Chí mạng: " + mainChar.critical;
		charInfo[11] = "Cống hiến: " + mainChar.dedicationPoint + " điểm.";
		charInfo[12] = "Liên trảm: " + mainChar.lienTram + " điểm.";
		charInfo[13] = "Công trạng: " + mainChar.congTrang + " điểm.";
		numH = charInfo.Length + 1;
		setLim();
	}

	private void setCamEpDoThu()
	{
		if (!Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			return;
		}
		isPaintFc = false;
		for (int i = 0; i < posEpNgoc.Length; i++)
		{
			if (Canvas_HAIRMOD_PROTECTED.isPointer(posEpNgoc[i][0] + 25 + xTr, posEpNgoc[i][1] + 65 + yTr, 34, 34))
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				selected = i;
				isSelectCheDo = true;
				if (numH != 1)
				{
					numH = 1;
					numW = 5;
					cmx = (cmy = (cmtoX = (cmtoY = 0)));
				}
				doSelectedEpDothu();
			}
		}
	}

	private void setCamEpNgoc()
	{
		if (!Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			return;
		}
		isPaintFc = false;
		for (int i = 0; i < posEpNgoc.Length; i++)
		{
			if (Canvas_HAIRMOD_PROTECTED.isPointer(posEpNgoc[i][0] + 25 + xTr, posEpNgoc[i][1] + 65 + yTr, newZise, newZise))
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				selected = i;
				isSelectCheDo = true;
				if (numH != 1)
				{
					numH = 1;
					numW = 5;
					cmx = (cmy = (cmtoX = (cmtoY = 0)));
				}
				setCmdCenter();
				if (center != null)
				{
					center.perform();
				}
			}
		}
	}

	private void updateKeyMain()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		bool flag = false;
		if (index[focusTab] != 28 && index[focusTab] != 21 && index[focusTab] != 25 && index[focusTab] != 26 && index[focusTab] != 32 && index[focusTab] != 7 && index[focusTab] != 8 && index[focusTab] != 22 && index[focusTab] != 23 && index[focusTab] != 1 && index[focusTab] != 31 && isDefault)
		{
			int num = ((index[focusTab] == 0) ? 73 : 53);
			curentTabUpdate = index[focusTab];
			xBegin = 15 + xTr;
			yBegin = 55 + yTr;
			wTouch = 5 * (wCell + 12) + 10;
			hTouch = hView - num;
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointerDown)
		{
			isshowInfo = false;
		}
		if (flag && !isDefault && index[focusTab] != 3)
		{
			cmtoY = selected / numW * hSmall - h / 2;
			if (cmtoY < 0)
			{
				cmtoY = 0;
			}
			if (cmtoY > cmyLim)
			{
				cmtoY = cmyLim;
			}
			if (index[focusTab] != 32)
			{
				cmtoX = selected % numW * wSmall - w / 2;
			}
			else if (selected > 4)
			{
				cmtoX = selected % numW * wSmall - w / 2;
			}
			if (cmtoX < 0)
			{
				cmtoX = 0;
			}
			if (cmtoX > cmxLim)
			{
				cmtoX = cmxLim;
			}
		}
	}

	private void setShowTextArr(string[] text, int x1, int y1)
	{
		isTextArr = true;
		isShowText = true;
		wShow = wKhung - (x0 - 2 + 7 * wCell);
		xShow = 7 * wCell + xTr;
		yShow = -10 + yTr;
		showText = text;
		hShow = hView;
		if (countPop == 0)
		{
			countPop = -(showText.Length - 1);
		}
		cmyText = (cmtoYText = (cmyLimText = 0));
		cmyLimText = showText.Length * 18 - hShow;
		if (cmyLimText < 0)
		{
			cmyLimText = 0;
		}
	}

	private void setUpdatePointerImbue(int bb, int aa)
	{
		if (bb > 0 && bb < 5)
		{
			selected = 1;
			return;
		}
		if (bb == 5)
		{
			bb = 2;
		}
		selected = aa * numW + bb;
	}

	private void setSizeEpNgoc(int i, bool isAn)
	{
		if (i == -1)
		{
			if (numH != 1)
			{
				numH = 1;
				selected = 4;
				numW = 5;
				cmx = (cmy = (cmtoX = (cmtoY = 0)));
			}
			else if (selected == 2)
			{
				selected = 0;
			}
			else if (selected == 3)
			{
				selected = 2;
			}
			else if (selected == 4)
			{
				selected = 1;
			}
		}
		else if (selected == 3 || selected == 4)
		{
			if (!isAn)
			{
				numW = listTemp.size();
				if (numW < 6)
				{
					numW = 6;
				}
				numH = 5;
			}
			else
			{
				resetEpdothu(-1);
			}
		}
		else if (selected == 2)
		{
			selected = 3;
		}
		else if (selected == 0)
		{
			selected = 2;
		}
		else if (selected == 1)
		{
			selected = 4;
		}
	}

	public bool setSizeKeepItem(int i)
	{
		int num = 34;
		int num2 = cmy;
		int num3 = 0;
		num3 = ((index[focusTab] != 22) ? sellItem.size() : Char_HAIRMOD_PROTECTED.itembag.size());
		if (ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size() < num3)
		{
			numH = num3 / 3 + 1;
		}
		else
		{
			numH = (ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size()) / 3 + 1;
		}
		if (numH < 5)
		{
			numH = 5;
		}
		if ((selected % numW == 2 && i == 1) || (selected % numW == 3 && i == -1))
		{
			if (i == -1)
			{
				cmyLim = ((((ItemShopNew.size() + MainChar_HAIRMOD_PROTECTED.gemItem.size()) / 3 + 1) * wSmall - index[focusTab] != 22) ? (h + num) : h);
			}
			else
			{
				cmyLim = (((num3 / 3 + 1) * -index[focusTab] != 22) ? (h + num) : h);
			}
			if (cmyLim < 0)
			{
				cmyLim = 0;
			}
			int num4 = selected / numW - cmy / num;
			cmy = (cmtoY = cmyKeep);
			selected = (cmy / num + num4) * numW + selected % numW;
			cmyKeep = num2;
			return true;
		}
		int num5 = ((index[focusTab] != 22) ? (h + num) : h);
		cmyLim = numH * num - num5;
		if (cmyLim < 0)
		{
			cmyLim = 0;
		}
		return false;
	}

	private void setSizeTrade(int dir)
	{
		int num = selected % numW - cmx / 22;
		if (num < 0)
		{
			num = 0;
		}
		int num2 = selected / numW;
		if (selected / numW + 2 == numH && dir == 1)
		{
			int num3 = 0;
			for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.listPotion.Length; i++)
			{
				if (MainChar_HAIRMOD_PROTECTED.listPotion[i].number - MainChar_HAIRMOD_PROTECTED.listPotion[i].numTrade > 0 && MainChar_HAIRMOD_PROTECTED.listPotion[i].isTrade)
				{
					num3++;
				}
			}
			numW = ItemShopNew.size() + num3;
			if (numW < 6)
			{
				numW = 6;
			}
			cmxLim = numW * 22 - w;
		}
		else
		{
			numW = 6;
			cmxLim = 0;
		}
		if (num >= numW)
		{
			num = numW - 1;
		}
		selected = num2 * numW + num;
	}

	private void setSizeImbue(int dir)
	{
		int num = selected % numW - cmx / newZise;
		if (num < 0)
		{
			num = 0;
		}
		int num2 = selected / numW;
		if (num2 + 2 == 5 && dir == 1)
		{
			numW = ItemShopNew.size() + listTemp.size();
			if (numW < 6)
			{
				numW = 6;
			}
			cmxLim = numW * newZise - w + 6 - 90;
			if (cmxLim < 0)
			{
				cmxLim = 0;
			}
		}
		else
		{
			numW = 3;
			cmxLim = 0;
		}
		if (num >= numW)
		{
			num = numW - 1;
		}
		selected = num2 * numW + num;
		cmx = (cmy = (cmtoX = (cmtoY = 0)));
	}

	public void updatePoint()
	{
		if (cmx != cmtoX)
		{
			cmvx = cmtoX - cmx << 2;
			cmdx += cmvx;
			cmx += cmdx >> 4;
			cmdx &= 15;
		}
		if (Math_HAIRMOD_PROTECTED.abs(cmtoY - cmy) < 15 && cmy < 0)
		{
			cmtoY = 0;
		}
		if (Math_HAIRMOD_PROTECTED.abs(cmtoY - cmy) < 10 && cmy > cmyLim)
		{
			cmtoY = cmyLim;
		}
	}

	public override void update()
	{
		Canvas_HAIRMOD_PROTECTED.gameScr.update();
		updateKeyShowText();
		if (index != null)
		{
			if (index[focusTab] != 22 && index[focusTab] != 23 && index[focusTab] != 8)
			{
				updateKeyDong();
				moveCamera();
			}
			else
			{
				if (cmy != cmtoY)
				{
					cmvy = cmtoY - cmy << 2;
					cmdy += cmvy;
					cmy += cmdy >> 4;
					cmdy &= 15;
				}
				if (cmx != cmtoX)
				{
					cmvx = cmtoX - cmx << 2;
					cmdx += cmvx;
					cmx += cmdx >> 4;
					cmdx &= 15;
				}
				if (Math_HAIRMOD_PROTECTED.abs(cmtoY - cmy) < 15 && cmy < 0)
				{
					cmtoY = 0;
				}
				if (Math_HAIRMOD_PROTECTED.abs(cmtoY - cmy) < 10 && cmy > cmyLim)
				{
					cmtoY = cmyLim;
				}
				if (Math_HAIRMOD_PROTECTED.abs(cmtoX - cmx) < 15 && cmx < 0)
				{
					cmtoX = 0;
				}
				else if (Math_HAIRMOD_PROTECTED.abs(cmtoX - cmx) < 15 && cmx > cmxLim)
				{
					cmtoX = cmxLim;
				}
			}
		}
		if (cmxTab != cmtoXTab)
		{
			cmvxTab = cmtoXTab - cmxTab << 2;
			cmdxTab += cmvxTab;
			cmxTab += cmdxTab >> 4;
			cmdxTab &= 15;
		}
		if (Math_HAIRMOD_PROTECTED.abs(cmtoXTab - cmxTab) < 15 && cmxTab < 0)
		{
			cmtoXTab = 0;
		}
		else if (Math_HAIRMOD_PROTECTED.abs(cmtoXTab - cmxTab) < 15 && cmxTab > cmxLimTab)
		{
			cmtoXTab = cmxLimTab;
		}
		if (cmtoXName != cmxname)
		{
			cmxname += (cmtoXName - cmxname) / 2;
			if (Util_HAIRMOD_PROTECTED.abs(cmxname) <= 1)
			{
				cmxname = 0;
			}
		}
		if (index[focusTab] == 31 && charWearing != null && charWearing.imgAnimal != null)
		{
			if (Canvas_HAIRMOD_PROTECTED.gameTick % charWearing.timeChangeFrame == 0)
			{
				countFrame++;
				if (countFrame > arrFrame[(charWearing.numFrame != 3) ? 1 : 0].Length - 1)
				{
					countFrame = 0;
				}
			}
			try
			{
				idFrame = arrFrame[(charWearing.numFrame != 3) ? 1 : 0][countFrame];
			}
			catch (Exception)
			{
			}
		}
		if (index[focusTab] == 0 && list != null && list.size() > 0)
		{
			limMyBag = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.limItemBag - 1;
			if (limMyBag <= 0)
			{
				limMyBag = 0;
			}
			excess = list.size() % 42;
			maxSize = list.size();
		}
		updateTab();
		if (index[focusTab] != 28 && index[focusTab] != 25 && index[focusTab] != 21 && index[focusTab] != 26 && index[focusTab] != 32 && !isDefault && index[focusTab] != 7 && index[focusTab] != 22 && index[focusTab] != 23 && index[focusTab] != 1 && index[focusTab] != 31 && index[focusTab] != 8 && index[focusTab] != 27 && index[focusTab] != 0)
		{
			checkPaint = true;
		}
		else
		{
			checkPaint = false;
		}
	}

	private void updateTab()
	{
		int num = Canvas_HAIRMOD_PROTECTED.w;
		if (num > 480)
		{
			num = 480;
		}
		int num2 = Canvas_HAIRMOD_PROTECTED.h;
		if (num2 > 320)
		{
			num2 = 320;
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointer(xKhung + xTr, yTr, wKhung, MainJ2ME_HAIRMOD_PROTECTED.hTab + 20) || Canvas_HAIRMOD_PROTECTED.menu.showMenu || !isDefault)
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
		{
			if (!Canvas_HAIRMOD_PROTECTED.isMove && !Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				trans = false;
			}
			if (!trans)
			{
				pa = cmxTab;
				trans = true;
				closeText();
			}
			cmtoXTab = pa + (Canvas_HAIRMOD_PROTECTED.pxLast - Canvas_HAIRMOD_PROTECTED.px);
			if (cmtoXTab < -Canvas_HAIRMOD_PROTECTED.pxLast)
			{
				cmtoXTab = -Canvas_HAIRMOD_PROTECTED.pxLast;
			}
			if (cmtoXTab > cmxLimTab + Canvas_HAIRMOD_PROTECTED.pxLast)
			{
				cmtoXTab = cmxLimTab + Canvas_HAIRMOD_PROTECTED.pxLast;
			}
		}
		sbyte b = index[focusTab];
		sbyte b2 = b;
		if ((uint)(b2 - 11) <= 7u)
		{
			if (!Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				return;
			}
			saveSelect = -1;
			isPaintFc = false;
			closeText();
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			if (Math_HAIRMOD_PROTECTED.abs(Canvas_HAIRMOD_PROTECTED.pxLast - Canvas_HAIRMOD_PROTECTED.px) < 10)
			{
				tdx = (cmtoXTab + Canvas_HAIRMOD_PROTECTED.px - 24 - xTr) / 70;
				cmtoXTab = tdx * 70 - num / 2;
				if (cmtoXTab < 0)
				{
					cmtoXTab = 0;
				}
				if (cmtoXTab > cmxLimTab)
				{
					cmtoXTab = cmxLimTab;
				}
				selected = 0;
				focusTabChinh = 0;
				focusTab = 0;
				saveSelect = -1;
				setSelectTab();
				setCmdCenter();
			}
		}
		else if (index[focusTab] != 24 && Canvas_HAIRMOD_PROTECTED.isPointer(15 + xTr, yTr, num / 2 - 15, 50))
		{
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
			{
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				closeText();
				focusTabChinh = 0;
				focusTab = 0;
				saveSelect = -1;
				setSelectTab();
				setCmdCenter();
				if (index != null)
				{
					curentTabUpdate = index[focusTab];
				}
				isPaintFc = false;
			}
		}
		else if (index[focusTab] != 24 && Canvas_HAIRMOD_PROTECTED.isPointer(15 + num / 2 + xTr, yTr, num / 2 - 15, 50) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			closeText();
			focusTabChinh = 0;
			focusTab = 0;
			saveSelect = -1;
			setSelectTab();
			setCmdCenter();
			if (index != null)
			{
				curentTabUpdate = index[focusTab];
			}
			isPaintFc = false;
		}
	}

	public void paintKhungClan(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = Canvas_HAIRMOD_PROTECTED.w;
		if (num > 480)
		{
			num = 480;
		}
		int num2 = Canvas_HAIRMOD_PROTECTED.h;
		if (num2 > 320)
		{
			num2 = 320;
		}
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 10 + xTr, 10 + yTr, num - 20, num2 - 50);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, name, Canvas_HAIRMOD_PROTECTED.hw, 15 + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
		int num3 = 55;
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = null;
		for (int i = 0; i < list.size(); i++)
		{
			if (isLoad)
			{
				break;
			}
			command_HAIRMOD_PROTECTED = (Command_HAIRMOD_PROTECTED)list.elementAt(i);
			command_HAIRMOD_PROTECTED.paint(g, 80 + xTr, num3 + yTr);
			num3 += 25;
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		try
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.paint(g);
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			g.translate(x, y);
			if (checkPaint)
			{
				if (index[focusTab] != 29)
				{
					Res_HAIRMOD_PROTECTED.paintDlgFull(g, -10, -10);
				}
				else
				{
					paintKhungClan(g, -10, -10);
				}
			}
			if (index[focusTab] != 27 && index[focusTab] != 29 && index[focusTab] != 0)
			{
				if (countL > 0)
				{
					countL--;
				}
				if (countR > 0)
				{
					countR--;
				}
				g.setColor(7961465);
				g.fillRect(21, 11, 88, 16);
				g.fillRect(20, 12, 90, 14);
				g.setColor((!isChangeTab) ? 2368548 : 30611);
				g.fillRect(21, 12, 88, 14);
				g.setClip(21, 11, 88, 16);
				g.translate(-cmxname, 0);
				int width = MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(nameshop);
				if (width > 88)
				{
					transX -= dirX;
					if (Util_HAIRMOD_PROTECTED.abs(transX) > (width - 88) / 2 + 5)
					{
						dirX *= -1;
					}
				}
			}
			g.translate(cmxname, 0);
			g.translate(x0, y0);
			g.setClip(-100, -100, 300, 300);
			int num = Canvas_HAIRMOD_PROTECTED.w;
			if (num > 480)
			{
				num = 480;
			}
			int num2 = Canvas_HAIRMOD_PROTECTED.h;
			if (num2 > 320)
			{
				num2 = 320;
			}
			try
			{
				isDefault = true;
				Canvas_HAIRMOD_PROTECTED.resetTrans(g);
				paintBG(g, 10 + xTr, 10 + yTr, num - 20, num2 - 60);
				g.translate(25, 65);
				g.setColor(25695);
				int num3 = hView - 34;
				int num4 = 7 * wCell - 2;
				sbyte b = 24;
				g.fillRect(num4 + xTr, -11 + yTr, wKhung - (x0 - 2 + 7 * wCell + b), num3);
				num3--;
				g.setColor(16774720);
				g.drawRect(num4 + xTr, -11 + yTr, wKhung - (x0 - 2 + 7 * wCell + b) - 1, num3);
				int num5 = -24 + num3;
				if (isPaintMoney)
				{
					MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu + "$", num4 + (wKhung - (x0 - 2 + 7 * wCell + b)) - 4 + xTr, num5 + yTr, MFont_HAIRMOD_PROTECTED.RIGHT);
					MFont_HAIRMOD_PROTECTED.smallFontColor[1].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luong + "L", num4 + 2 + xTr, num5 + yTr, MFont_HAIRMOD_PROTECTED.LEFT);
					if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luongKhoa > -1)
					{
						MFont_HAIRMOD_PROTECTED.smallFontColor[1].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luongKhoa + "LK", num4 + (wKhung - (x0 - 2 + 7 * wCell + b)) / 2 + xTr, num5 + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
					}
				}
				if (index[focusTab] == 0)
				{
					int num6 = num2 - 130;
				}
				g.setClip(-10 + xTr, -10 + yTr, numW * wSmall + 20, num3 + addHclip + 10);
				g.translate(xTr, -cmy + yTr);
				int num7 = wKhung;
				int num8 = cmy / (hSmall + 10);
				if (num8 < 0)
				{
					num8 = 0;
				}
				int num9 = num8 + (num7 + 20) / (hSmall + 10);
				if (index[focusTab] == 0 && num9 > 42)
				{
					num9 = 42;
				}
				for (int i = num8; i < num9; i++)
				{
					for (int j = 0; j < 5; j++)
					{
						paintGrid(g, j * (wSmall + 10) - 7, i * (hSmall + 10) - 10);
					}
				}
				if (selected != -1 && isPaintFc)
				{
					g.setColor(10595790, 0.5f);
					g.fillRect(selected % 5 * (wSmall + 10) - 5, selected / 5 * (hSmall + 10) - 8, wSmall + 10 - 3, hSmall + 10 - 3);
				}
				if (index[focusTab] != 0)
				{
					paintProduct(g);
				}
				else
				{
					paintBag(g);
				}
				Canvas_HAIRMOD_PROTECTED.resetTrans(g);
				sbyte b2 = index[focusTab];
				sbyte b3 = b2;
				if ((uint)(b3 - 11) <= 7u)
				{
					g.setClip(15 + xTr, 9 + yTr, num - 30, 33);
					g.translate(-cmxTab, 0);
					g.setColor(0, 0.5f);
					g.fillRect(17 + focusTabChinh * 69 + xTr, 12 + yTr, 69, 40);
					for (int k = 0; k < tabName.Length; k++)
					{
						MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, string.Empty + tabName[k], 48 + 70 * k + xTr, 20 + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
					}
					for (int l = 0; l < tabName.Length; l++)
					{
						g.drawImage(Res_HAIRMOD_PROTECTED.imgK0, 17 + 69 * l + xTr, 12 + yTr, 0);
					}
					g.translate(cmxTab, 0);
					g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
				}
				else if (index[focusTab] != 24)
				{
					g.setColor(0, 0.5f);
					g.fillRect(13 + focusTabChinh * (num / 2 - 13) + xTr, 13 + yTr, num / 2 - 12, 30);
					MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, nameshop, num / 4 + 10 + xTr, 18 + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
					g.setColor(10591392);
					g.fillRect(num / 2 + xTr, 13 + yTr, 2, 29);
				}
				else
				{
					MFont_HAIRMOD_PROTECTED.bigFont.drawString(g, "Gian hàng", Canvas_HAIRMOD_PROTECTED.hw, 69 + num5, MFont_HAIRMOD_PROTECTED.CENTER);
				}
			}
			catch (Exception)
			{
			}
			if (isShowText)
			{
				Canvas_HAIRMOD_PROTECTED.resetTrans(g);
				if (checkPaintText())
				{
					g.translate(25, 65);
					paintShowTextNew(g);
				}
				else
				{
					paintShowText(g);
				}
			}
			base.paint(g);
		}
		catch (Exception)
		{
		}
	}

	public bool checkPaintText()
	{
		if (index == null)
		{
			return false;
		}
		if (index[focusTab] == 28 || index[focusTab] == 21 || index[focusTab] == 25 || index[focusTab] == 26 || index[focusTab] == 32 || isDefault || index[focusTab] == 22 || index[focusTab] == 23 || index[focusTab] == 1 || index[focusTab] == 31 || index[focusTab] == 8 || index[focusTab] == 7)
		{
			return true;
		}
		return false;
	}

	private int canPutItem()
	{
		int num = -1;
		for (int i = 0; i < itemEpdothu.Length; i++)
		{
			if (itemEpdothu[i] == null)
			{
				num++;
			}
		}
		return num;
	}

	public void resetEpdothu(int saveIndex)
	{
		numW = listItemAnimal.size() + listTemp.size();
		if (numW < 6)
		{
			numW = 6;
		}
		numH = 5;
		if (saveIndex == -1)
		{
			selected = (numH - 1) * numW;
		}
		else
		{
			selected -= numH;
			if (selected > numH * numW - 1)
			{
				selected = numH * numW - 1;
			}
			if (selected < (numH - 1) * numW)
			{
				selected = (numH - 1) * numW;
			}
			cmtoX = selected % numW * wSmall - w / 2;
			if (cmtoX < 0)
			{
				cmtoX = 0;
			}
			if (cmtoX > cmxLim)
			{
				cmtoX = cmxLim;
			}
			cmx = cmtoX;
		}
		cmxLim = numW * 20 - (w - 8);
		if (cmxLim < 0)
		{
			cmxLim = 0;
		}
		if (saveIndex == -1)
		{
			cmx = (cmy = (cmtoX = (cmtoY = 0)));
		}
	}

	public void doshowInfoEpdothu()
	{
		ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)listItemAnimal.elementAt(selected);
		showItemInventoryInfo(item, false, selected * 20 + 4, 86);
	}

	private void doSelectedEpDothu()
	{
		if (isShowText)
		{
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if (numH != 1 && !isNguyenLieu)
		{
			if (selected >= 0 && selected < listItemAnimal.size())
			{
				ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)listItemAnimal.elementAt(selected);
				Out_HAIRMOD_PROTECTED.println(selected + "vi tri chon item ep   " + selected);
				if (canPutItem() > -1)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Bỏ vào");
					command_HAIRMOD_PROTECTED.action = delegate
					{
						if (item.level >= 30 && item.level <= lvThu + 5 && item.level >= lvThu - 5 && item.colorName == idColor)
						{
							if (itemEpdothu[2] == null)
							{
								if (magicAnimal == item.magic_physic)
								{
									itemEpdothu[2] = item;
									listItemAnimal.removeElement(item);
								}
							}
							else
							{
								for (int l = 0; l < itemEpdothu.Length; l++)
								{
									if (itemEpdothu[l] == null)
									{
										itemEpdothu[l] = item;
										listItemAnimal.removeElement(item);
										break;
									}
								}
							}
						}
						else
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Đồ ép phải cùng màu và chênh lệch +-5 level.");
						}
						resetEpdothu(1);
					};
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Thông tin");
				command_HAIRMOD_PROTECTED2.action = delegate
				{
					doshowInfoEpdothu();
					Canvas_HAIRMOD_PROTECTED.endDlg();
				};
				command_HAIRMOD_PROTECTED2.perform();
			}
			else if (selected - listItemAnimal.size() >= 0 && selected - listItemAnimal.size() < listTemp.size())
			{
				GemTemplate_HAIRMOD_PROTECTED item2 = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(selected - listItemAnimal.size());
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Bỏ vào");
				command_HAIRMOD_PROTECTED3.action = delegate
				{
					if (listEp.size() < 6)
					{
						if (checkItem(false) >= slNLThu)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Nguyên liệu đã đủ số lượng.");
							return;
						}
						item2.number--;
						if (item2.number <= 0)
						{
							listTemp.removeElement(item2);
						}
						GemTemplate_HAIRMOD_PROTECTED o = new GemTemplate_HAIRMOD_PROTECTED(item2.rID)
						{
							id = item2.id
						};
						listEp.addElement(o);
					}
					bool flag2 = false;
					int num2 = listEp.size();
					for (int j = 0; j < num2; j++)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
						if (gemTemplate_HAIRMOD_PROTECTED3.id == item2.id)
						{
							gemTemplate_HAIRMOD_PROTECTED3.number++;
							flag2 = true;
							break;
						}
					}
					for (int k = 0; k < num2; k++)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(k);
						if (gemTemplate_HAIRMOD_PROTECTED4.number == 0)
						{
							listEp.removeElement(gemTemplate_HAIRMOD_PROTECTED4);
						}
					}
					if (!flag2)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED5 = new GemTemplate_HAIRMOD_PROTECTED(item2.id);
						GemTemplate_HAIRMOD_PROTECTED.copyData(item2, gemTemplate_HAIRMOD_PROTECTED5);
						gemTemplate_HAIRMOD_PROTECTED5.number = 1;
						listEp.addElement(gemTemplate_HAIRMOD_PROTECTED5);
					}
				};
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
				mVector_HAIRMOD_PROTECTED2.addElement(addGemInfo(item2, selected * 20 + 4, 86));
			}
		}
		else if (isNguyenLieu)
		{
			if (selected >= 0 && selected < listEp.size())
			{
				GemTemplate_HAIRMOD_PROTECTED g = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(selected);
				if (g != null)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Lâ\u0301y ra");
					command_HAIRMOD_PROTECTED4.action = delegate
					{
						g.number--;
						if (g.number <= 0)
						{
							listEp.removeElement(g);
						}
						bool flag = false;
						int num = listTemp.size();
						for (int i = 0; i < num; i++)
						{
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
							if (gemTemplate_HAIRMOD_PROTECTED.id == g.id)
							{
								gemTemplate_HAIRMOD_PROTECTED.number++;
								flag = true;
								break;
							}
						}
						if (!flag)
						{
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(g.id)
							{
								number = 1
							};
							listTemp.addElement(g);
						}
						resetEpdothu(-1);
					};
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED4);
				}
			}
		}
		else if (selected >= 0 && selected < itemEpdothu.Length)
		{
			ItemInInventory_HAIRMOD_PROTECTED real = itemEpdothu[selected];
			if (real != null)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Lâ\u0301y ra");
				command_HAIRMOD_PROTECTED5.action = delegate
				{
					listItemAnimal.addElement(real);
					itemEpdothu[selected] = null;
					resetEpdothu(-1);
				};
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED5);
			}
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
	}

	private int checkItem(bool isItem)
	{
		int num = 0;
		if (isItem)
		{
			for (int i = 0; i < itemEpdothu.Length; i++)
			{
				if (itemEpdothu[i] != null)
				{
					num++;
				}
			}
		}
		else
		{
			for (int j = 0; j < listEp.size(); j++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
				num += gemTemplate_HAIRMOD_PROTECTED.number;
			}
		}
		return num;
	}

	private void doLeftEpDothu()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (checkItem(true) < 5)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn phải sử dụng đủ 5 vật phẩm.");
			}
			else if (checkItem(false) < slNLThu)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Nguyên liệu không đủ. Cần " + slNLThu + " nguyên liệu.");
			}
			else
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				if (itemEpdothu[2] != null)
				{
					mVector_HAIRMOD_PROTECTED2.addElement(itemEpdothu[2]);
				}
				for (int i = 0; i < itemEpdothu.Length; i++)
				{
					if (i != 2 && itemEpdothu[i] != null)
					{
						mVector_HAIRMOD_PROTECTED2.addElement(itemEpdothu[i]);
					}
				}
				sbyte[] array = new sbyte[6];
				for (int j = 0; j < listEp.size(); j++)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
					GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
					string s = gemByID.name.Substring(gemByID.name.Length - 1);
					int num = int.Parse(s);
					array[num - 1] = (sbyte)gemTemplate_HAIRMOD_PROTECTED.number;
				}
				GameService_HAIRMOD_PROTECTED.gI().epDothu(mVector_HAIRMOD_PROTECTED2, array);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang hợp..", false);
				for (int k = 0; k < itemEpdothu.Length; k++)
				{
					itemEpdothu[k] = null;
				}
				if (listEp != null)
				{
					listEp.removeAllElements();
				}
				right.perform();
			}
		};
		left = new Command_HAIRMOD_PROTECTED("Hợp");
		left.action = action;
	}

	private void paintBG(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int wd, int hd)
	{
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, x, y, wd, hd + 5);
		g.setColor(Res_HAIRMOD_PROTECTED.color[1]);
		g.fillRect(x + 1, y + MainJ2ME_HAIRMOD_PROTECTED.hTab - 13, wd - 2, 2);
	}

	private void paintGrid(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		int num = ((!isDefault) ? 34 : 44);
		g.setColor(6513507);
		g.drawRect(x, y, num, num);
		g.setColor(8684162);
		g.drawRect(x + 1, y + 1, num - 2, num - 2);
	}

	private void paintGrid29(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], x, y, 0);
	}

	private void paintCheDo(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 0;
		int num2 = 34;
		for (int i = 0; i < listCheDo.size(); i++)
		{
			Mineral_HAIRMOD_PROTECTED mineral_HAIRMOD_PROTECTED = (Mineral_HAIRMOD_PROTECTED)listCheDo.elementAt(i);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, mineral_HAIRMOD_PROTECTED.name + ": " + mineral_HAIRMOD_PROTECTED.number + " Cái.", xTr, i * 15 + 2 - 10 + yTr, 0);
		}
		int num3 = 0;
		int num4 = listEp.size() / 2;
		if (num4 < 6)
		{
			num4 = 6;
		}
		g.translate(0, 0);
		g.setColor(8092027, 0.5f);
		if (selected / numW < 2 && isSelectCheDo)
		{
			g.fillRect(selected % numW * num2 + xTr, listCheDo.size() * 13 + selected / numW * num2 + 13 - 10 + yTr, num2, num2);
		}
		int num5 = listEp.size();
		int num6 = listCheDo.size();
		for (int j = 0; j < num5; j++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(j);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, xTr + j % num4 * num2 + wSmall / 2 + 4, num6 * 13 + 13 + j / num4 * num2 + wSmall / 2 - ((!isLastScreenMenuWindow) ? 13 : 0) + yTr);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, j % num4 * num2 + wSmall / 2 + 12 + xTr, num6 * 13 + 17 + j / num4 * num2 + wSmall / 2 - ((!isLastScreenMenuWindow) ? 10 : 0) + yTr, MFont_HAIRMOD_PROTECTED.RIGHT);
			num3++;
		}
		for (int k = 0; k < 3; k++)
		{
			g.fillRect(xTr, k * num2 + 27 + yTr, 6 * num2 + 2, 2);
		}
		g.setColor(8092027);
		for (int l = 0; l < 7; l++)
		{
			g.fillRect(l * num2 + xTr, 27 + yTr, 2, 2 * num2);
		}
		g.setColor(8092027, 0.5f);
		g.setClip(-10 + xTr, 3 * newZise + yTr, 5 * newZise + 20, 3 * newZise);
		g.translate(-cmx + 10 + xTr, yTr);
		paintGemItemCheDo(g, num, 158);
		num += listTemp.size();
	}

	private void paintSelected(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		g.setColor(10595790);
		g.fillRect(x, y, 32, 32);
	}

	private void paintKeepItem(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = hView - 34;
		g.setClip(-10 + xTr, -10 + yTr, 7 * wCell, num + addHclip);
		int num2 = 0;
		int num3 = 0;
		int num4 = 34;
		if (selected % numW < 3)
		{
			g.translate(0, -cmy);
			if (!isChangeTab && isFcStore)
			{
				paintSelected(g, xTr + selected % numW * (num4 - 1) + 1, yTr + selected / numW * (num4 - 1) + 1);
			}
		}
		else
		{
			g.translate(0, -cmyKeep);
		}
		int num5 = ItemShopNew.size();
		g.setColor(7706352);
		for (int i = 0; i < num5; i++)
		{
			num2 = i % 3 * (num4 - 1);
			num3 = i / 3 * (num4 - 1);
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED.isSelling)
			{
				g.fillRect(num2 + xTr, num3 + yTr, num4, num4);
			}
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], num2 + xTr, num3 + yTr, 0);
			itemInInventory_HAIRMOD_PROTECTED.paint(g, num2 + 17 + xTr, num3 + 17 + yTr);
		}
		int num6 = ItemShopNew.size();
		int num7 = MainChar_HAIRMOD_PROTECTED.gemItem.size();
		for (int j = 0; j < num7; j++)
		{
			num2 = num6 % 3 * (num4 - 1);
			num3 = num6 / 3 * (num4 - 1);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], num2 + xTr, num3 + yTr, 0);
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(j);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, num2 + 17 + xTr, num3 + 10 + yTr);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, num2 + num4 + xTr, yTr + num3 + num4 - MFont_HAIRMOD_PROTECTED.smallFont.getHeight() - 7, MFont_HAIRMOD_PROTECTED.RIGHT);
			num6++;
		}
		if (selected % numW > 2)
		{
			g.translate(0, cmyKeep);
			g.translate(0, -cmy);
		}
		else
		{
			g.translate(0, cmy);
			g.translate(0, -cmyKeep);
		}
		for (int k = 0; k < numH; k++)
		{
			for (int l = 0; l < 3; l++)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[2], l * (num4 - 1) + num4 * 3 - 3 + xTr, k * (num4 - 1) + yTr, 0);
				g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], l * (num4 - 1) + num4 * 3 - 3 + xTr, k * (num4 - 1) + yTr, 0);
			}
		}
		if (selected % numW > 2 && !isChangeTab)
		{
			paintSelected(g, selected % numW * (num4 - 1) + 1 + xTr, selected / numW * (num4 - 1) + 1 + yTr);
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = null;
		mVector_HAIRMOD_PROTECTED2 = ((index[focusTab] != 22) ? sellItem : Char_HAIRMOD_PROTECTED.itembag);
		for (int m = 0; m < mVector_HAIRMOD_PROTECTED2.size(); m++)
		{
			if (mVector_HAIRMOD_PROTECTED2.elementAt(m) is ItemInInventory_HAIRMOD_PROTECTED)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(m);
				itemInInventory_HAIRMOD_PROTECTED2.paint(g, m % 3 * num4 + (num4 - 1) * 3 + 14 + xTr, m / 3 * (num4 - 1) + 17 + yTr);
			}
			else
			{
				RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(m);
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID).idImage, m % 3 * (num4 - 1) + num4 * 3 + 14 + xTr, m / 3 * (num4 - 1) + 17 + yTr);
			}
		}
	}

	private void paintKeepAndSellItem(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setClip(0, 0, w, h);
		int num = 0;
		int num2 = 0;
		if (selected % numW < 3)
		{
			g.translate(0, -cmy);
			if (!isChangeTab)
			{
				paintSelected(g, selected % numW * 18 + 1, selected / numW * 18 + 1);
			}
		}
		else
		{
			g.translate(0, -cmyKeep);
		}
		for (int i = 0; i < numH; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgGrid, j * 18, i * 18, 0);
			}
		}
		for (int k = 0; k < ItemShopNew.size(); k++)
		{
			num = k % 3 * 18;
			num2 = k / 3 * 18;
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(k);
			if (itemInInventory_HAIRMOD_PROTECTED.isSelling)
			{
				g.setColor(7706352);
				g.fillRect(num + 2, num2 + 2, 14, 14);
			}
			itemInInventory_HAIRMOD_PROTECTED.paint(g, num + 9, num2 + 9);
		}
		int num3 = ItemShopNew.size();
		for (int l = 0; l < MainChar_HAIRMOD_PROTECTED.gemItem.size(); l++)
		{
			num = num3 % 3 * 18;
			num2 = num3 / 3 * 18;
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(l);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, num + 9, num2 + 9);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, num + 18, num2 + 18 - MFont_HAIRMOD_PROTECTED.smallFont.getHeight(), MFont_HAIRMOD_PROTECTED.RIGHT);
			num3++;
		}
		if (selected % numW > 2)
		{
			g.translate(0, cmyKeep);
			g.translate(0, -cmy);
		}
		else
		{
			g.translate(0, cmy);
			g.translate(0, -cmyKeep);
		}
		for (int m = 0; m < numH; m++)
		{
			for (int n = 0; n < 3; n++)
			{
				g.setColor(6568449);
				g.fillRect(n * 18 + 54, m * 18, 18, 18);
				g.drawImage(Res_HAIRMOD_PROTECTED.imgGrid, n * 18 + 54, m * 18, 0);
			}
		}
		if (selected % numW > 2 && !isChangeTab)
		{
			paintSelected(g, selected % numW * 18 + 1, selected / numW * 18 + 1);
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = null;
		mVector_HAIRMOD_PROTECTED2 = ((index[focusTab] != 22) ? sellItem : Char_HAIRMOD_PROTECTED.itembag);
		for (int num4 = 0; num4 < mVector_HAIRMOD_PROTECTED2.size(); num4++)
		{
			if (mVector_HAIRMOD_PROTECTED2.elementAt(num4) is ItemInInventory_HAIRMOD_PROTECTED)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(num4);
				itemInInventory_HAIRMOD_PROTECTED2.paint(g, num4 % 3 * 18 + 54 + 9, num4 / 3 * 18 + 9);
			}
			else
			{
				RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(num4);
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID).idImage, num4 % 3 * 18 + 54 + 9, num4 / 3 * 18 + 9);
			}
		}
	}

	private void paintEpDothu(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < 5; i++)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], posEpNgoc[i][0] + xTr, posEpNgoc[i][1] + yTr, 0);
		}
		g.setColor(10595790, 0.5f);
		if (numH == 1 && selected < 5)
		{
			g.fillRect(posEpNgoc[selected][0] + 2 + xTr, posEpNgoc[selected][1] + 2 + yTr, 30, 30);
		}
		for (int j = 0; j < itemEpdothu.Length; j++)
		{
			if (itemEpdothu[j] != null)
			{
				paintItemIcon(g, itemEpdothu[j], posEpNgoc[j][0] + 17 + xTr, posEpNgoc[j][1] + 21 + yTr);
			}
		}
		int num = 3;
		if (idColor == 3)
		{
			num = 2;
		}
		else if (idColor == 2)
		{
			num = 1;
		}
		MFont_HAIRMOD_PROTECTED.normalFont[num].drawString(g, string.Empty + mau[num], posEpNgoc[2][0] + 16 + xTr, posEpNgoc[1][1] + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Số Đá: " + slNLThu, posEpNgoc[2][0] + 16 + xTr, posEpNgoc[3][1] + yTr + 16, MFont_HAIRMOD_PROTECTED.CENTER);
		for (int k = 0; k < 6; k++)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], posEpNgoc[0][0] + xTr + k * 32, posEpNgoc[3][1] + yTr + 38, 0);
		}
		int num2 = listEp.size();
		for (int l = 0; l < num2; l++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listEp.elementAt(l);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			string s = gemByID.name.Substring(gemByID.name.Length - 1);
			int num3 = int.Parse(s);
			if (selected == l && isNguyenLieu)
			{
				g.fillRect(posEpNgoc[0][0] + xTr + l * 32 + 1, posEpNgoc[3][1] + yTr + 38 + 1, 31, 31);
			}
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, posEpNgoc[0][0] + xTr + l * 32 + 16, posEpNgoc[3][1] + yTr + 54);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, num3 + string.Empty, posEpNgoc[0][0] + xTr + l * 32 + 32, posEpNgoc[3][1] + yTr + 38, MFont_HAIRMOD_PROTECTED.RIGHT);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, posEpNgoc[0][0] + xTr + l * 32 + 32, posEpNgoc[3][1] + yTr + 54, MFont_HAIRMOD_PROTECTED.RIGHT);
		}
		g.setClip(-10 + xTr, 4 * newZise - 4 + yTr, 5 * newZise + 10, 52);
		g.translate(-cmx + xTr, yTr);
		int num4 = 0;
		paintListItemAnimal(g, num4);
		num4 += listItemAnimal.size();
	}

	public void paintListItemAnimal(MyGraphics_HAIRMOD_PROTECTED g, int count)
	{
		int num = 176;
		for (int i = 0; i < listItemAnimal.size(); i++)
		{
			if (selected > -1 && selected == i && isPaintFc && !isNguyenLieu)
			{
				g.fillRect(i * 32 + 1, num - 2, 32, 32);
			}
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItemAnimal.elementAt(i);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], count * 32, num - 3, 0);
			itemInInventory_HAIRMOD_PROTECTED.paint(g, count * 32 + 16, num + 17);
			count++;
		}
		paintGemItemThu(g, count, num);
	}

	private void paintGemItemThu(MyGraphics_HAIRMOD_PROTECTED g, int count, int yy)
	{
		int num = listTemp.size();
		for (int i = 0; i < num; i++)
		{
			if (khoaDoThu == 1)
			{
				g.setColor(3276851, 0.5f);
				g.fillRect(count * 32, yy - 3, 34, 34);
			}
			g.setColor(10595790, 0.5f);
			if (isPaintFc && selected > -1 && selected == count && !isNguyenLieu)
			{
				g.fillRect(count * 32 + 1, yy - 2, 32, 32);
			}
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], count * 32, yy - 3, 0);
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, count * 32 + 16, yy + 17);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			string s = gemByID.name.Substring(gemByID.name.Length - 1);
			try
			{
				int num2 = int.Parse(s);
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, num2 + string.Empty, count * 32 + 32, yy - 3, MFont_HAIRMOD_PROTECTED.RIGHT);
			}
			catch (Exception)
			{
			}
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, count * 32 + 32, yy + 15, MFont_HAIRMOD_PROTECTED.RIGHT);
			count++;
		}
	}

	private void paintEpNgoc(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < 5; i++)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], posEpNgoc[i][0] + xTr, posEpNgoc[i][1] + yTr, 0);
		}
		g.setColor(10595790, 0.5f);
		if (numH == 1 && selected < 5 && isSelectCheDo)
		{
			g.fillRect(posEpNgoc[selected][0] + 1 + xTr, posEpNgoc[selected][1] + 1 + yTr, newZise, newZise);
		}
		for (int j = 0; j < listEp.size(); j++)
		{
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)listEp.elementAt(j);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID).idImage, posEpNgoc[j][0] + 21 + xTr, posEpNgoc[j][1] + 21 + yTr);
		}
		g.setClip(-10 + xTr, 4 * newZise - 4 + yTr, 5 * newZise + 20, 52);
		g.translate(-cmx + xTr, yTr);
		if (numH != 1)
		{
		}
		int num = 0;
		paintGemItemHopThanh(g, num, 164);
		num += listTemp.size();
	}

	private void paintGemItemCheDo(MyGraphics_HAIRMOD_PROTECTED g, int count, int y)
	{
		int num = listTemp.size();
		for (int i = 0; i < num; i++)
		{
			if (khoaCheDo == 1)
			{
				g.setColor(3276851, 0.5f);
				g.fillRect(count * 42 - 21, y, 42, 42);
			}
			g.setColor(10595790, 0.5f);
			if (selected == i && isPaintFc)
			{
				g.fillRect(count * 42 - 21, y, 42, 42);
			}
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], count * 42 - 21, y, 0);
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, count * newZise, y + 17);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			string s = gemByID.name.Substring(gemByID.name.Length - 1);
			try
			{
				int num2 = int.Parse(s);
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, num2 + string.Empty, count * newZise + 5, y + 2, MFont_HAIRMOD_PROTECTED.LEFT);
			}
			catch (Exception)
			{
			}
			MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, count * newZise + 21, y + 28, MFont_HAIRMOD_PROTECTED.RIGHT);
			count++;
		}
	}

	private void paintGemItemHopThanh(MyGraphics_HAIRMOD_PROTECTED g, int count, int y)
	{
		for (int i = 0; i < listTemp.size(); i++)
		{
			if (selected == i && isPaintFc)
			{
				g.fillRect(count * 42, y, 41, 42);
			}
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], count * 42, y, 0);
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, count * 42 + 21, y + 17);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			string s = gemByID.name.Substring(gemByID.name.Length - 1);
			try
			{
				int num = int.Parse(s);
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, num + string.Empty, count * 42 + 42, y + 2, MFont_HAIRMOD_PROTECTED.RIGHT);
			}
			catch (Exception)
			{
			}
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, count * 42 + 42, y + 28, MFont_HAIRMOD_PROTECTED.RIGHT);
			count++;
		}
	}

	private void paintGemItem(MyGraphics_HAIRMOD_PROTECTED g, int count, int y)
	{
		for (int i = 0; i < listTemp.size(); i++)
		{
			if ((khoaImbue == 1 && index[focusTab] == 21) || (khoaKham == 1 && index[focusTab] == 25))
			{
				g.setColor(3276851, 0.5f);
				g.fillRect(count * 42 - 21 + xTr, y + yTr, 42, 42);
			}
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], count * 42 - 21 + xTr, y + yTr, 0);
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listTemp.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, count * 42 + xTr, y + 17 + yTr);
				if (index[focusTab] != 21)
				{
					GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
					string s = gemByID.name.Substring(gemByID.name.Length - 1);
					try
					{
						int num = int.Parse(s);
						MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, num + string.Empty, count * 42 + 5 + xTr, y + 2 + yTr, MFont_HAIRMOD_PROTECTED.LEFT);
					}
					catch (Exception)
					{
					}
				}
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, count * 42 + 21 + xTr, y + 22 + yTr, MFont_HAIRMOD_PROTECTED.RIGHT);
			}
			count++;
		}
	}

	private void paintImbue(MyGraphics_HAIRMOD_PROTECTED g)
	{
		paintDapBG(g, 0, 0);
		if (imgWeaponAvatar != null)
		{
			g.drawImage(imgWeaponAvatar, 106 + xTr, 4 * newZise / 2 - 5 + yTr, 3);
		}
		for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size(); i++)
		{
			RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(i);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID).idImage, xTr + i % 2 * (5 * wCell) + wCell / 2 + 6, yTr + i / 2 * 42 + hSmall / 2 - 4);
		}
		g.setClip(-10 + xTr, 4 * newZise - 4 + yTr, 5 * newZise + 20, 52);
		g.translate(-cmx + 10, 0);
		int num = 0;
		g.setColor(10595790, 0.5f);
		if (isPaintFc)
		{
			g.fillRect(idfcsetSelectedImbue * 42 - 21 + xTr, 164 + yTr, 42, 42);
		}
		for (int j = 0; j < ItemShopNew.size(); j++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(j);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], num * 42 - 21 + xTr, 164 + yTr, 0);
			itemInInventory_HAIRMOD_PROTECTED.paint(g, num * 42 + xTr, 185 + yTr);
			num++;
		}
		paintGemItem(g, num, 164);
		num += listTemp.size();
	}

	private void paintDapBG(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
	{
		if (!isChangeTab && Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3 && selected / numW < 4 && isSelectCheDo)
		{
			g.setColor(10595790);
			if (selected % numW == 1 && selected / numW != 4)
			{
				g.fillRect(63 + xTr, -10 + yTr, 84, 168);
			}
			else
			{
				int num = selected % numW;
				if (num > 1)
				{
					num = 1;
				}
				g.fillRect(num * (newZise * 4) - num * 6 + xTr, yTr + selected / numW * newZise - 9, newZise, newZise);
			}
		}
		for (int i = 0; i < 8; i++)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], i / 4 * (5 * wCell) + xTr, i % 4 * 42 - 10 + yTr, 0);
		}
		g.setColor(10595790);
		g.drawRect(63 + xTr, -10 + yTr, 84, 168);
	}

	private void paintNewCell(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int wSmall)
	{
		g.setColor(6513507);
		g.drawRect(x, y, wSmall, wSmall);
		g.setColor(8684162);
		g.drawRect(x + 1, y + 1, wSmall - 2, wSmall - 2);
	}

	private void paintTrade(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				if (j > 2)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[2], j * 33 - 17 + xTr, i * 32 - 8 + yTr, 0);
				}
				if (selectedGD == i * 6 + j && selectedGD != -1 && Canvas_HAIRMOD_PROTECTED.gameTick % 10 == 0)
				{
					g.fillRect(j * 33 - 17 + xTr, i * 32 - 8 + yTr, 34, 34);
				}
				g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[0], j * 33 - 17 + xTr, i * 32 - 8 + yTr, 0);
			}
		}
		mVector_HAIRMOD_PROTECTED tItems = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.tItems;
		mVector_HAIRMOD_PROTECTED rItems = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.rItems;
		for (int k = 0; k < tItems.size(); k++)
		{
			paintItemTrade(g, tItems.elementAt(k), k % 3 * 34 - 12 + xTr, k / 3 * 34 + yTr);
		}
		for (int l = 0; l < rItems.size(); l++)
		{
			paintItemTrade(g, rItems.elementAt(l), 102 + l % 3 * 34 - 12 + xTr, l / 3 * 34 + yTr);
		}
		g.setClip(-15 + xTr, 3 * newZise + yTr, 5 * newZise + 10, 3 * newZise);
		g.translate(-cmx, 0);
		int num = -12;
		h = 4 * newZise;
		g.setColor(10595790);
		g.setColor(10595790, 0.5f);
		if (!isPaintfc)
		{
			g.fillRect(selected * smallS + num + xTr, h + yTr + 1, 31, 31);
		}
		g.setColor(10595790);
		for (int m = 0; m < ItemShopNew.size(); m++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(m);
			if (itemInInventory_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
			if (item != null)
			{
				g.drawRect(num + xTr - 1, h + yTr, 32, 32);
				if (itemInInventory_HAIRMOD_PROTECTED.isSelling)
				{
					g.setColor(7706352);
					g.fillRect(num + xTr - 1, h + yTr, 32, 32);
				}
				GameData_HAIRMOD_PROTECTED.paintIcon(g, item.idIcon, 20 + (num - 4) + xTr, h + smallS / 2 - 3 + yTr);
				num += smallS;
			}
		}
		for (int n = 0; n < MainChar_HAIRMOD_PROTECTED.listPotion.Length; n++)
		{
			if (MainChar_HAIRMOD_PROTECTED.listPotion[n].number - MainChar_HAIRMOD_PROTECTED.listPotion[n].numTrade > 0 && MainChar_HAIRMOD_PROTECTED.listPotion[n].isTrade)
			{
				g.drawRect(num + xTr - 1, h + yTr, 32, 32);
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(MainChar_HAIRMOD_PROTECTED.listPotion[n].index + 5500), num - 4 + 20 + xTr, h + smallS / 2 - 3 + yTr);
				MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, MainChar_HAIRMOD_PROTECTED.listPotion[n].number - MainChar_HAIRMOD_PROTECTED.listPotion[n].numTrade + string.Empty, num + 22 + xTr, h + smallS / 2 - 2 + yTr, MFont_HAIRMOD_PROTECTED.RIGHT);
				num += smallS;
			}
		}
	}

	public int checktotalItemTrade()
	{
		int num = 0;
		for (int i = 0; i < ItemShopNew.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED != null)
			{
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				if (item != null)
				{
					num++;
				}
			}
		}
		for (int j = 0; j < MainChar_HAIRMOD_PROTECTED.listPotion.Length; j++)
		{
			if (MainChar_HAIRMOD_PROTECTED.listPotion[j].number - MainChar_HAIRMOD_PROTECTED.listPotion[j].numTrade > 0 && MainChar_HAIRMOD_PROTECTED.listPotion[j].isTrade)
			{
				num++;
			}
		}
		return num;
	}

	private void paintItemTrade(MyGraphics_HAIRMOD_PROTECTED g, object item, int x, int y)
	{
		if (item is ItemInInventory_HAIRMOD_PROTECTED)
		{
			((ItemInInventory_HAIRMOD_PROTECTED)item).paint(g, x + 9, y + 9);
			return;
		}
		Potion_HAIRMOD_PROTECTED potion_HAIRMOD_PROTECTED = (Potion_HAIRMOD_PROTECTED)item;
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(potion_HAIRMOD_PROTECTED.index + 5500), x + 9, y + 9);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, potion_HAIRMOD_PROTECTED.number + string.Empty, x + 17, y + 9, MFont_HAIRMOD_PROTECTED.RIGHT);
	}

	private void paintNewSkillLearn(MyGraphics_HAIRMOD_PROTECTED g)
	{
		string[] array = new string[5] { "Kiếm sỹ", "Chiến binh", "Pháp sư", "Đấu sỹ", "Cung thủ" };
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz], 7 * wCell / 2 + xTr, -16 + yTr, MFont_HAIRMOD_PROTECTED.CENTER);
		g.setColor(11908533);
		g.fillRect(-4 + xTr, yTr, 7 * wCell, 1);
		int num = 20;
		for (int i = 0; i < numW; i++)
		{
			InfoSkillLearn_HAIRMOD_PROTECTED infoSkillLearn_HAIRMOD_PROTECTED = (InfoSkillLearn_HAIRMOD_PROTECTED)((mVector_HAIRMOD_PROTECTED)GameScr_HAIRMOD_PROTECTED.infoNewSkill.elementAt(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz)).elementAt(i);
			if (infoSkillLearn_HAIRMOD_PROTECTED != null)
			{
				if (i < 5)
				{
					paintFc(g, i * newZise + 3 + num + xTr, h / 2 + num + yTr, newZise, newZise, i, selected);
					GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(infoSkillLearn_HAIRMOD_PROTECTED.idSkill, i * newZise + 3 + num + xTr, h / 2 + num + yTr, 0, 3, g);
					g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], i * newZise + 3 + num + xTr, h / 2 + num + yTr, 3);
				}
				else if (i >= 5 && i < 10)
				{
					paintFc(g, (i - 5) * newZise + 3 + num + xTr, h / 2 + newZise + num + yTr, newZise, newZise, i, selected);
					GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(i, (i - 5) * newZise + 3 + num + xTr, h / 2 + newZise + num + yTr, 0, 3, g);
					g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], (i - 5) * newZise + 3 + num + xTr, h / 2 + newZise + num + yTr, 3);
				}
				else if (i >= 10)
				{
					paintFc(g, (i - 10) * newZise + 3 + num + xTr, h / 2 + newZise * 2 + num + yTr, newZise, newZise, i, selected);
					GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(i, (i - 10) * newZise + 3 + num + xTr, h / 2 + newZise * 2 + num + yTr, 0, 3, g);
					g.drawImage(Res_HAIRMOD_PROTECTED.imgCell[1], (i - 10) * newZise + 3 + num + xTr, h / 2 + newZise * 2 + num + yTr, 3);
				}
			}
		}
	}

	public void paintFc(MyGraphics_HAIRMOD_PROTECTED g, int x, int y, int w, int h, int i, int selected)
	{
		if (selected == i && Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3 && !isPaintfc)
		{
			g.setColor(6513507, 0.5f);
			g.fillRect(x - w / 2, y - h / 2, w, h);
		}
	}

	private void paintKhung(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = h + 26;
		if (index[focusTab] == 4)
		{
			num = h;
		}
		g.setColor(11908533);
		g.drawRect(0, -10, w, num);
		g.setClip(0, -10, w, num);
		g.translate(0, 1 - cmy);
	}

	private void paintQuestClan(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, 10, 10, Canvas_HAIRMOD_PROTECTED.w - 20, Canvas_HAIRMOD_PROTECTED.h - 50);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Nhiệm vụ bang hội", Canvas_HAIRMOD_PROTECTED.hw, 15, MFont_HAIRMOD_PROTECTED.CENTER);
		int num = 45;
		g.setClip(0, num - 1, Canvas_HAIRMOD_PROTECTED.w, h - 32);
		g.translate(0, num);
		for (int i = 0; i < questInfo.size(); i++)
		{
			string st = (string)questInfo.elementAt(i);
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, st, 15, i * 15, 0);
		}
	}

	private void paintSkillClan(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = 170;
		int num2 = 130;
		int num3 = (Canvas_HAIRMOD_PROTECTED.w - num) / 2;
		int num4 = (Canvas_HAIRMOD_PROTECTED.h - num2) / 2;
		Res_HAIRMOD_PROTECTED.paintDlgDragonFull(g, num3 - 5, num4 - 10, num + 10, num2 + 20);
		g.setColor(11908533);
		g.drawRect(num3 + 2, num4 + 15, num - 4, num2 - 30);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Kỹ năng bang hội", num3 + (num + 10) / 2, num4 - 5, MFont_HAIRMOD_PROTECTED.CENTER);
		g.setClip(num3 + 2, num4 + 15, num - 4, num2 - 30);
		g.translate(num3 + 5, num4 + 20);
		g.translate(0, -cmy);
		int num5 = 2;
		for (int i = 0; i < questInfo.size(); i++)
		{
			string text = (string)questInfo.elementAt(i);
			if (text.Length >= 2 && text.Substring(0, 1).Equals("`"))
			{
				string text2 = text.Substring(1, text.Length - 1);
				text = text2;
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, text, 5, i * 15 + num5, 0);
			}
			else
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, text, 5, i * 15 + num5, 0);
			}
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu + "$", num3 + num - 10, num4 + num2 - 10, 1);
		MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luong + "L", num3 + 10, num4 + num2 - 10, 0);
	}

	public static string getInfoQuestPaint(int type)
	{
		string result = string.Empty;
		if (type == 0 && GameScr_HAIRMOD_PROTECTED.mainQuest != null)
		{
			result = GameScr_HAIRMOD_PROTECTED.mainQuest.getInfoPaintScr();
		}
		if (GameScr_HAIRMOD_PROTECTED.subQuest != null && type == 1)
		{
			try
			{
				result = GameScr_HAIRMOD_PROTECTED.subQuest.getInfoPaintScr();
			}
			catch (Exception)
			{
			}
		}
		if (GameScr_HAIRMOD_PROTECTED.clanQuest != null && type == 2)
		{
			try
			{
				result = GameScr_HAIRMOD_PROTECTED.clanQuest.getInfoPaintScr();
			}
			catch (Exception)
			{
			}
		}
		return result;
	}

	private void paintQuest(MyGraphics_HAIRMOD_PROTECTED g)
	{
		string infoQuestPaint = getInfoQuestPaint(0);
		string infoQuestPaint2 = getInfoQuestPaint(1);
		string infoQuestPaint3 = getInfoQuestPaint(2);
		if (infoQuestPaint.Equals(string.Empty) && infoQuestPaint2.Equals(string.Empty))
		{
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, "Chưa nhận nhiệm vụ", 8 + xTr, -7 + yTr, 0);
			return;
		}
		string[] array = null;
		if (!infoQuestPaint.Equals(string.Empty))
		{
			string[] array2 = Util_HAIRMOD_PROTECTED.split(infoQuestPaint, "|");
			array = new string[array2.Length + 1];
			array[0] = GameScr_HAIRMOD_PROTECTED.mainQuest.name;
			for (int i = 0; i < array2.Length; i++)
			{
				array[i + 1] = array2[i];
			}
		}
		int num = 0;
		if (array != null)
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[0], 4, num * 15 - 8 + yTr, 0);
			for (int j = 1; j < array.Length; j++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, array[j], 4, j * 15 - 8 + yTr, 0);
			}
			num = array.Length;
		}
		array = null;
		if (!infoQuestPaint2.Equals(string.Empty))
		{
			string[] array3 = Util_HAIRMOD_PROTECTED.split(infoQuestPaint2, "|");
			array = new string[array3.Length + 1];
			array[0] = GameScr_HAIRMOD_PROTECTED.subQuest.name;
			for (int k = 0; k < array3.Length; k++)
			{
				array[k + 1] = array3[k];
			}
		}
		if (array != null)
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[0], 4, num * 15 - 8 + yTr, 0);
			for (int l = 1; l < array.Length; l++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, array[l], 4, (l + num) * 15 - 8 + yTr, 0);
			}
			num += array.Length;
		}
		array = null;
		if (!infoQuestPaint3.Equals(string.Empty))
		{
			string[] array4 = Util_HAIRMOD_PROTECTED.split(infoQuestPaint3, "|");
			array = new string[array4.Length + 1];
			array[0] = GameScr_HAIRMOD_PROTECTED.clanQuest.name;
			for (int m = 0; m < array4.Length; m++)
			{
				array[m + 1] = array4[m];
			}
		}
		if (array != null)
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, array[0], 4, num * 15 - 8 + yTr, 0);
			for (int n = 1; n < array.Length; n++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, array[n], 4, (n + num) * 15 - 8 + yTr, 0);
			}
			num = array.Length;
		}
	}

	private void paintParty(MyGraphics_HAIRMOD_PROTECTED g)
	{
		paintKhung(g);
		int num = Char_HAIRMOD_PROTECTED.party.size();
		if (num > 0)
		{
			g.setColor(6448384);
			g.fillRect(2, selected * 32, 131, 32);
			for (int i = 0; i < num; i++)
			{
				PartyInfo_HAIRMOD_PROTECTED partyInfo_HAIRMOD_PROTECTED = (PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(i);
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, "NV:" + partyInfo_HAIRMOD_PROTECTED.name, 8, i * 33, 0);
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, "LV: " + partyInfo_HAIRMOD_PROTECTED.lv, 8, (i * 2 + 1) * 16, 0);
				g.setColor(65535);
				g.fillRect(2, (i * 2 + 1) * 16 + 15, 131, 1);
			}
		}
		else
		{
			MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, "Chưa có nhóm", 8, -3, 0);
		}
	}

	private void paintCharInfo(MyGraphics_HAIRMOD_PROTECTED g)
	{
		paintKhung(g);
		if (charInfo != null)
		{
			for (int i = 0; i < charInfo.Length; i++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, charInfo[i], 4, 5 + i * 12 - 8, 0);
			}
		}
	}

	private void paintSkill(MyGraphics_HAIRMOD_PROTECTED g)
	{
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.skillPointLeft + string.Empty, 105, -16, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Điểm kỹ năng: ", 60, -22, MFont_HAIRMOD_PROTECTED.CENTER);
		g.setColor(11908533);
		g.fillRect(-8, -8, 133, 1);
		g.fillRect(-8, 26, 133, 1);
		g.setClip(-8, -8, w + 20, h + 8);
		g.translate(-cmx, 0);
		if (!isChangeTab && Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3)
		{
			g.setColor(589295);
			g.fillRect(selected * wSmall + 3, 5 - h / 4, wSmall - 6, hSmall + h / 4 - 3);
		}
		for (int i = 0; i < numW; i++)
		{
			GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(i, i * wSkill + wSmall / 2, h / 2 - h / 4 + 2, 0, 3, g);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgKhung, i * wSkill + wSmall / 2, h / 2 - h / 4 + 2, 3);
			MFont_HAIRMOD_PROTECTED.smallFontColor[1].drawString(g, Char_HAIRMOD_PROTECTED.skillLevelLearnt[i] + string.Empty, i * wSkill + wSmall - 4, h / 2 - 1, MFont_HAIRMOD_PROTECTED.RIGHT);
		}
		if (!isChangeTab)
		{
			if (selected < 0)
			{
				selected = 0;
			}
			else if (selected > SkillManager_HAIRMOD_PROTECTED.SKILL_NAME[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz].Length - 1)
			{
				selected = SkillManager_HAIRMOD_PROTECTED.SKILL_NAME[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz].Length - 1;
			}
			g.translate(cmx, 0);
			g.setClip(-11, 28, 138, 72);
			string text = SkillManager_HAIRMOD_PROTECTED.SKILL_NAME[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][selected];
			sbyte b = Char_HAIRMOD_PROTECTED.skillLevelLearnt[selected];
			if (b == 9)
			{
				text = text + " MAX" + b;
			}
			else if (b >= 1)
			{
				text = text + " lv " + b;
			}
			string[] infoSkill = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.getInfoSkill(selected);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, text, -5, 30, 0);
			int num = 40;
			for (int j = 0; j < infoSkill.Length; j++)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, infoSkill[j], -5, num, 0);
				num += 12;
			}
		}
	}

	private void paintFeatures(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!isChangeTab)
		{
			if (selected > 4)
			{
				selected = 4;
			}
			g.setColor(7278866);
			g.fillRect(3, selected * 19, 121, 18);
		}
		g.drawImage(Res_HAIRMOD_PROTECTED.imgBGTiemNang, 0, -20, 0);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.basePointLeft + string.Empty, 107, -11, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.strength + string.Empty, 107, 8, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.agility + string.Empty, 107, 27, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.spirit + string.Empty, 107, 46, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.health + string.Empty, 107, 65, MFont_HAIRMOD_PROTECTED.CENTER);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luck + string.Empty, 107, 84, MFont_HAIRMOD_PROTECTED.CENTER);
	}

	private void paintCharWearing(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (selected > 14)
		{
			selected = 14;
		}
		if (selected < 0)
		{
			selected = 0;
		}
		charWearing.paintAvatar(g, (short)(90 + xTr), (short)(110 + yTr));
		if (imgCharWear != null && Canvas_HAIRMOD_PROTECTED.gameTick % 16 >= 8)
		{
			g.drawImage(imgCharWear, w / 2 + 5 + xTr, -5 + yTr, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
		}
		g.setColor(14605278);
		g.drawRect(39 + xTr, -10 + yTr, 104, 208);
		g.fillRect(43 + xTr, -10 + yTr + 175, 30, 30);
		g.fillRect(110 + xTr, -10 + yTr + 175, 30, 30);
		for (int i = 0; i < 5; i++)
		{
			g.fillRect(-3 + xTr, -10 + i * 42 + yTr, 40, 40);
			g.fillRect(147 + xTr, -10 + i * 42 + yTr, 40, 40);
		}
		g.setColor(4802889);
		g.fillRect(44 + xTr, -10 + yTr + 176, 28, 28);
		g.fillRect(111 + xTr, -10 + yTr + 176, 28, 28);
		for (int j = 0; j < 5; j++)
		{
			g.fillRect(-2 + xTr, -9 + j * 42 + yTr, 38, 38);
			g.fillRect(148 + xTr, -9 + j * 42 + yTr, 38, 38);
		}
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3 && isPaintFc)
		{
			g.setColor(10595790, 0.5f);
			if (selected % 3 != 1)
			{
				int num = 0;
				if (selected == 2 || selected == 5 || selected == 8 || selected == 11 || selected == 14)
				{
					num = 1;
				}
				g.fillRect(-2 + num * 150 + xTr, selected / numW * 42 - 9 + yTr, 38, 38);
			}
			else if (selected != 4)
			{
				g.fillRect(111 + xTr, -10 + yTr + 176, 28, 28);
			}
			else
			{
				g.fillRect(44 + xTr, -10 + yTr + 176, 28, 28);
			}
		}
		int num2 = 0;
		if (charWearing.wearingItems != null)
		{
			sbyte b = (sbyte)charWearing.wearingItems.size();
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
			ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = null;
			for (sbyte b2 = 0; b2 < b; b2++)
			{
				itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(b2);
				itemTemplate_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				if (itemTemplate_HAIRMOD_PROTECTED.type == 8)
				{
					if (itemInInventory_HAIRMOD_PROTECTED.viTriVe == 1)
					{
						paintItemIcon(g, itemInInventory_HAIRMOD_PROTECTED, 165 + xTr, 96 + yTr);
					}
					else
					{
						paintItemIcon(g, itemInInventory_HAIRMOD_PROTECTED, 165 + xTr, 140 + yTr);
					}
				}
				else
				{
					paintItemIcon(g, itemInInventory_HAIRMOD_PROTECTED, posPaintWearingX[itemTemplate_HAIRMOD_PROTECTED.type], posPaintWearingY[itemTemplate_HAIRMOD_PROTECTED.type]);
				}
			}
		}
		if (!Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isDoing && charWearing.idAnimal > -1)
		{
			GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(charWearing.idPaintIconAnimal + 7500), 125 + xTr, 180 + yTr);
		}
	}

	private void paintAnimalWearing(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (charWearing != null && (charWearing.imgAnimal != null || charWearing.idhorse > -1) && (charWearing.useHorse != -1 || charWearing.idhorse > -1))
		{
			g.drawRegion(charWearing.imgAnimal, 0, charWearing.himg * idFrame, charWearing.wimg, charWearing.himg, 0, Res_HAIRMOD_PROTECTED.imgInfoWindow[0].getWidth() / 2 + xTr, Res_HAIRMOD_PROTECTED.imgInfoWindow[0].getHeight() / 2 - 5 + yTr, 3);
		}
		g.drawImage(Res_HAIRMOD_PROTECTED.imgInfoWindowt, -3 + xTr, -10 + yTr, 0);
		g.setColor(14605278);
		for (int i = 0; i < 5; i++)
		{
			g.fillRect(-3 + xTr, -10 + i * 42 + yTr, 40, 40);
			g.fillRect(147 + xTr, -10 + i * 42 + yTr, 40, 40);
		}
		g.setColor(4802889);
		for (int j = 0; j < 5; j++)
		{
			g.fillRect(-2 + xTr, -9 + j * 42 + yTr, 38, 38);
			g.fillRect(148 + xTr, -9 + j * 42 + yTr, 38, 38);
		}
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3 && isPaintFc)
		{
			g.setColor(10595790, 0.5f);
			if (selected % 2 == 0)
			{
				g.fillRect(-2 + xTr, selected / 2 * 42 - 9 + yTr, 38, 38);
			}
			else
			{
				g.fillRect(148 + xTr, selected / 2 * 42 - 9 + yTr, 38, 38);
			}
		}
		int num = 0;
		if (charWearing == null || charWearing.wearingAnimal == null || (charWearing.useHorse == -1 && charWearing.idhorse <= -1))
		{
			return;
		}
		sbyte b = (sbyte)charWearing.wearingAnimal.size();
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
		ItemTemplate_HAIRMOD_PROTECTED itemTemplate_HAIRMOD_PROTECTED = null;
		for (sbyte b2 = 0; b2 < b; b2++)
		{
			itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingAnimal.elementAt(b2);
			if (itemInInventory_HAIRMOD_PROTECTED != null)
			{
				itemTemplate_HAIRMOD_PROTECTED = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				if (itemTemplate_HAIRMOD_PROTECTED != null)
				{
					paintItemIcon(g, itemInInventory_HAIRMOD_PROTECTED, posPaintWearingX[itemTemplate_HAIRMOD_PROTECTED.type], posPaintWearingY[itemTemplate_HAIRMOD_PROTECTED.type]);
				}
			}
		}
	}

	private void paintItemIcon(MyGraphics_HAIRMOD_PROTECTED g, ItemInInventory_HAIRMOD_PROTECTED item, int x, int y)
	{
		ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(item.charClazz, item.idTemplate);
		GameData_HAIRMOD_PROTECTED.paintIcon(g, item2.idIcon, x, y);
	}

	private void paintShowText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(25695);
		g.fillRect(xShow, yShow, wShow, hShow + countPop * 15);
		g.setColor(16774720);
		g.drawRect(xShow, yShow, wShow - 1, hShow - 1 + countPop * 15);
		int num = 0;
		g.setClip(xShow, yShow, wShow, hShow + countPop * 15 - 2);
		g.translate(0, -cmyText);
		for (int i = 0; i < showText.Length + countPop; i++)
		{
			if (i == 1 && arrayKhamNgoc != null && arrayKhamNgoc.Length != 0)
			{
				for (int j = 0; j < arrayKhamNgoc.Length; j++)
				{
					g.drawImage(Res_HAIRMOD_PROTECTED.imgKham, xShow + 12 + j * 20, yShow + 12 + num, 3);
					if (arrayKhamNgoc[j] != -1)
					{
						Res_HAIRMOD_PROTECTED.paintGem(g, arrayKhamNgoc[j], xShow + 12 + j * 20, yShow + 12 + num);
					}
				}
				num += 18;
			}
			if (!showText[i].Equals(string.Empty))
			{
				sbyte b = (sbyte)(showText[i][0] - 48);
				sbyte startIndex = 1;
				if (!isDegit(showText[i][0]))
				{
					b = 0;
					startIndex = 0;
				}
				MFont_HAIRMOD_PROTECTED.normalFont[(b < 6) ? b : 0].drawString(g, showText[i].Substring(startIndex), xShow + 4, yShow + 4 + num, 0);
				num += Res_HAIRMOD_PROTECTED.hString;
			}
		}
		if (countPop < 0)
		{
			countPop++;
		}
	}

	public static bool isDegit(char c)
	{
		return c >= '0' && c <= '9';
	}

	private void paintBag(MyGraphics_HAIRMOD_PROTECTED g)
	{
		try
		{
			int num = cmy / (hSmall + 10) * 5;
			if (num < 0)
			{
				num = 0;
			}
			int num2 = num + 45;
			if (num2 > 42)
			{
				num2 = 42;
			}
			if (countTabMyitem * 42 + 42 > maxSize)
			{
				num2 = excess;
			}
			for (int i = num; i < num2; i++)
			{
				if (isLoad)
				{
					break;
				}
				int num3 = i + countTabMyitem * 42;
				if (num3 < list.size())
				{
					((Command_HAIRMOD_PROTECTED)list.elementAt(num3))?.paint(g, i % 5 * (wSmall + 10) + (wSmall + 10) / 2 - 7, i / 5 * (hSmall + 10) + (hSmall + 10) / 2 - 10);
				}
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println("ERROR hanh trang " + ex.ToString());
		}
	}

	private void paintProduct(MyGraphics_HAIRMOD_PROTECTED g)
	{
	}

	private void getShopTemplate()
	{
	}

	public void closeText()
	{
		isShowText = false;
		arrayKhamNgoc = null;
	}

	private void setShowText(string text, int x1, int y1)
	{
		setShowTextNew(text, x1, y1);
	}

	public void doBuyShopSpecial()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (selected < Res_HAIRMOD_PROTECTED.shopTemplate.size())
			{
				if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isFullInventory())
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Hành trang đã đầy.");
				}
				else
				{
					int num = 0;
					for (int i = 0; i < Res_HAIRMOD_PROTECTED.shopTemplate.size(); i++)
					{
						GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.shopTemplate.elementAt(i);
						if (gemTemp_HAIRMOD_PROTECTED.shopType == focusTab && gemTemp_HAIRMOD_PROTECTED.isSell)
						{
							if (num == selected)
							{
								GemTemp_HAIRMOD_PROTECTED item = (GemTemp_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.shopTemplate.elementAt(i);
								ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
								{
									GameService_HAIRMOD_PROTECTED.gI().buyItemShopSpecial(item.id);
									Canvas_HAIRMOD_PROTECTED.startWaitDlg();
								};
								Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn mua vật phẩm này không ?", yesAction);
								break;
							}
							num++;
						}
					}
				}
			}
		};
		cmdShop = new Command_HAIRMOD_PROTECTED();
		cmdShop.action = action;
	}

	public int getCountPotion(int i)
	{
		return 0;
	}

	public mVector_HAIRMOD_PROTECTED getInventori()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int num = 0;
		try
		{
			for (int i = 0; i < ItemShopNew.size(); i++)
			{
				int ii = num;
				ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(i);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					showItemInventoryInfo(item, isSell, ii % numW * 18, ii / numW * 18);
				};
				ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
				{
					if (item.isSelling)
					{
						g.setColor(7706352);
						g.fillRect(x - 8, y - 8, 16, 16);
					}
					item.paint(g, x, y);
				};
				command_HAIRMOD_PROTECTED.action = action;
				command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				num++;
			}
		}
		catch (Exception)
		{
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	protected void showGemItemInfo(short id, int xx, int yy)
	{
		GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(id);
		string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
		text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
		if (isSell)
		{
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp("Giá bán lại: " + gemByID.price / 5, "0");
		}
		if (index[focusTab] == 28 || index[focusTab] == 26 || isDefault || index[focusTab] == 22 || index[focusTab] == 23)
		{
			setShowTextNew(text, xx, yy);
		}
		else
		{
			setShowText(text, xx, yy);
		}
	}

	protected void showGemItemKhoaInfo(short id, int xx, int yy)
	{
		GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(id);
		string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
		text += "- Đã khóa";
		text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
		if (isSell)
		{
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp("Giá bán lại: " + gemByID.price / 5, "0");
		}
		if (index[focusTab] == 28 || index[focusTab] == 26 || isDefault || index[focusTab] == 22 || index[focusTab] == 23)
		{
			setShowTextNew(text, xx, yy);
		}
		else
		{
			setShowText(text, xx, yy);
		}
	}

	protected void showRealIDInfo(RealID_HAIRMOD_PROTECTED real, int xx, int yy)
	{
		GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(real.ID);
		string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
		text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
		if (isSell)
		{
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp("Giá bán : " + real.price, "0");
		}
		if (isDefault)
		{
			setShowTextNew(text, xx, yy);
		}
		else
		{
			setShowText(text, xx, yy);
		}
	}

	protected void showShopItemInfo(short id, int count)
	{
		GemTemp_HAIRMOD_PROTECTED shopByID = Res_HAIRMOD_PROTECTED.getShopByID(id);
		string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(shopByID.name, "0");
		text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(shopByID.decript, "0");
		if (isSell)
		{
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp("Giá bán lại: " + Canvas_HAIRMOD_PROTECTED.getMoneys(shopByID.price / 5), "0");
		}
		if (isDefault)
		{
			setShowTextNew(text, count % numW * 18, count / numW * 18);
		}
		else
		{
			setShowText(text, count % numW * 18, count / numW * 18);
		}
	}

	public string getInfoItemQuest(int id)
	{
		string text = "0" + ItemQuest_HAIRMOD_PROTECTED.NAME_ITEM[id];
		return text + "\nSố lượng: " + MainChar_HAIRMOD_PROTECTED.itemQuest[id];
	}

	private string getPotionInfo(int aa)
	{
		return string.Empty;
	}

	public void doSelectedInventori()
	{
		int element = selected + ((index[focusTab] == 0) ? (countTabMyitem * 42) : 0);
		int num = element;
		int num2 = 0;
		for (int i = 1; i < Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions.Length; i++)
		{
			int num3 = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[i];
			if (num3 > 0)
			{
				if (element == num2)
				{
					showMenuForPotion(i);
					return;
				}
				num2++;
			}
		}
		num -= num2;
		if (num < MainChar_HAIRMOD_PROTECTED.shopItem.size())
		{
			GemTemplate_HAIRMOD_PROTECTED shop2 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.shopItem.elementAt(num);
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Sử dụng");
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().doUseSpecialItem(shop2.rID);
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
			mVector_HAIRMOD_PROTECTED2.addElement(cmdDelItem(shop2, 1, MainChar_HAIRMOD_PROTECTED.shopItem, 0));
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
			return;
		}
		num -= MainChar_HAIRMOD_PROTECTED.shopItem.size();
		if (num < MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size())
		{
			GemTemplate_HAIRMOD_PROTECTED item3 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItemKhoa.elementAt(num);
			if (isSell)
			{
				GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(item3.id);
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().sellGemItem(item3.rID, 1);
					Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang bán..", true);
					list.removeElementAt(element);
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + gemByID.price / 5 + "$ không?", yesAction);
				return;
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			GemTemp_HAIRMOD_PROTECTED gemByID2 = Res_HAIRMOD_PROTECTED.getGemByID(item3.id);
			if (gemByID2 != null && gemByID2.type == 4)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Sử dụng");
				command_HAIRMOD_PROTECTED2.action = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().douseGemItem(item3.id, 1);
				};
				mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED2);
			}
			mVector_HAIRMOD_PROTECTED3.addElement(cmdDelItem(item3, 0, MainChar_HAIRMOD_PROTECTED.gemItemKhoa, 1));
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 2);
			return;
		}
		num -= MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size();
		if (num < MainChar_HAIRMOD_PROTECTED.gemItem.size())
		{
			GemTemplate_HAIRMOD_PROTECTED item2 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(num);
			if (isSell)
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().sellGemItem(item2.rID, 0);
					Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang bán..", true);
					list.removeElementAt(element);
				};
				GemTemp_HAIRMOD_PROTECTED gemByID3 = Res_HAIRMOD_PROTECTED.getGemByID(item2.id);
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + gemByID3.price / 5 + "$ không?", yesAction2);
				return;
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
			GemTemp_HAIRMOD_PROTECTED gemByID4 = Res_HAIRMOD_PROTECTED.getGemByID(item2.id);
			if (gemByID4 != null && gemByID4.type == 4)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Sử dụng");
				command_HAIRMOD_PROTECTED3.action = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().douseGemItem(item2.id, 0);
				};
				mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED3);
			}
			mVector_HAIRMOD_PROTECTED4.addElement(cmdDelItem(item2, 0, MainChar_HAIRMOD_PROTECTED.gemItem, 0));
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED4, 2);
			return;
		}
		num -= MainChar_HAIRMOD_PROTECTED.gemItem.size();
		if (num < Char_HAIRMOD_PROTECTED.animal.size())
		{
			AnimalInfo_HAIRMOD_PROTECTED shop = (AnimalInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.animal.elementAt(num);
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED5 = new mVector_HAIRMOD_PROTECTED();
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Sử dụng");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if ((Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.useHorse > -1 || Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.idhorse > -1) && shop.typeAnimal == 0)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể cưỡi.");
				}
				else
				{
					GameService_HAIRMOD_PROTECTED.gI().doRideAnimal(shop.id, shop.typeAnimal);
				}
			};
			command_HAIRMOD_PROTECTED4.action = action2;
			mVector_HAIRMOD_PROTECTED5.addElement(command_HAIRMOD_PROTECTED4);
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED5, 2);
			return;
		}
		num -= Char_HAIRMOD_PROTECTED.animal.size();
		if (num >= ItemShopNew.size())
		{
			return;
		}
		ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)ItemShopNew.elementAt(num);
		if (!isSell)
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED6 = new mVector_HAIRMOD_PROTECTED();
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Sử dụng");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				doUseItem(item);
			};
			command_HAIRMOD_PROTECTED5.action = action3;
			mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED5);
			if (item.colorName == 1 && item.level >= 50 && !ItemInInventory_HAIRMOD_PROTECTED.isAnimalArmor(item.getTemplate().type))
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Tách trang bị");
				command_HAIRMOD_PROTECTED6.action = delegate
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction5 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestTachNguyenLIeu(item.itemID, 0, -1, 0);
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn tách nguyên liệu từ vật phẩm này không?", yesAction5);
				};
				mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED6);
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED7 = new Command_HAIRMOD_PROTECTED("Bỏ ra đất");
			ActionPerform_HAIRMOD_PROTECTED action4 = delegate
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction4 = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().giveItemToGround(item.itemID);
					Canvas_HAIRMOD_PROTECTED.endDlg();
					list.removeElementAt(element);
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Vật phẩm sẽ mất khi bạn bỏ ra đất. Bạn có muốn tiếp tục không?", yesAction4);
			};
			command_HAIRMOD_PROTECTED7.action = action4;
			mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED7);
			if (item.colorName != 0 && item.colorName != 4)
			{
				if (item.colorName == 1)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED8 = new Command_HAIRMOD_PROTECTED("Sử dụng");
					ActionPerform_HAIRMOD_PROTECTED action5 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(10, item.itemID);
					};
					command_HAIRMOD_PROTECTED8.action = action5;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED8);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED9 = new Command_HAIRMOD_PROTECTED("Thăng cấp thuộc tính ");
					ActionPerform_HAIRMOD_PROTECTED action6 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(11, item.itemID);
					};
					command_HAIRMOD_PROTECTED9.action = action6;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED9);
				}
				if (item.nameCharSeal.Equals(string.Empty))
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED10 = new Command_HAIRMOD_PROTECTED("Đóng dấu");
					ActionPerform_HAIRMOD_PROTECTED action7 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(14, item.itemID);
					};
					command_HAIRMOD_PROTECTED10.action = action7;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED10);
				}
				if (!item.nameCharSeal.ToLower().Equals(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.name.ToLower()))
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED11 = new Command_HAIRMOD_PROTECTED(" ");
					ActionPerform_HAIRMOD_PROTECTED action8 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(14, item.itemID);
					};
					command_HAIRMOD_PROTECTED11.action = action8;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED11);
				}
				else
				{
					ItemTemplate_HAIRMOD_PROTECTED item4 = Res_HAIRMOD_PROTECTED.getItem(item.charClazz, item.idTemplate);
					if ((item4.type >= 3 && item4.type <= 7) || item4.type == 8 || item4.type == 9)
					{
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED12 = new Command_HAIRMOD_PROTECTED("Sửa thuộc tính kỹ năng");
						ActionPerform_HAIRMOD_PROTECTED action9 = delegate
						{
							GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(15, item.itemID);
						};
						command_HAIRMOD_PROTECTED12.action = action9;
						mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED12);
					}
				}
				if (item.islock == 0)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED13 = new Command_HAIRMOD_PROTECTED("Khoá");
					ActionPerform_HAIRMOD_PROTECTED action10 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(12, item.itemID);
					};
					command_HAIRMOD_PROTECTED13.action = action10;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED13);
				}
				if (item.islock == 1)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED14 = new Command_HAIRMOD_PROTECTED("Khoá lại");
					ActionPerform_HAIRMOD_PROTECTED action11 = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(12, item.itemID);
					};
					command_HAIRMOD_PROTECTED14.action = action11;
					mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED14);
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED15 = new Command_HAIRMOD_PROTECTED(" ");
				ActionPerform_HAIRMOD_PROTECTED action12 = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().doRequestActionCheDo(13, item.itemID);
				};
				command_HAIRMOD_PROTECTED15.action = action12;
				mVector_HAIRMOD_PROTECTED6.addElement(command_HAIRMOD_PROTECTED15);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED6, 2);
		}
		else
		{
			ItemTemplate_HAIRMOD_PROTECTED item5 = Res_HAIRMOD_PROTECTED.getItem(item.charClazz, item.idTemplate);
			ActionPerform_HAIRMOD_PROTECTED yesAction3 = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().sellItem(item.itemID);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg("Đang bán..", true);
				list.removeElementAt(element);
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn bán vật phẩm này với giá " + (item5.price / 5 + item5.plus * 1000) + "$ không?", yesAction3);
		}
	}

	protected void doUseItem(ItemInInventory_HAIRMOD_PROTECTED item)
	{
		ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(item.charClazz, item.idTemplate);
		if (item2.gender != 0 && item2.gender != Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.gender)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Vật phẩm này chỉ dành cho " + Res_HAIRMOD_PROTECTED.GENDERNAME[item2.gender] + ".");
		}
		else if (item2.clazz != -1 && item2.clazz != Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz && isWeapone(item2.type))
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Vật phẩm này chỉ dành cho " + CreateCharScr_HAIRMOD_PROTECTED.clazz[item2.clazz][CreateCharScr_HAIRMOD_PROTECTED.selectGender].name + ".");
		}
		else if (item2.level > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.level)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn phải đạt cấp " + item2.level + " để có thể dùng.");
		}
		else
		{
			GameService_HAIRMOD_PROTECTED.gI().useItem(item.itemID);
		}
	}

	protected void doSellItem()
	{
	}

	private Command_HAIRMOD_PROTECTED cmdDelItem(GemTemplate_HAIRMOD_PROTECTED item, int index, mVector_HAIRMOD_PROTECTED lit, sbyte khoa)
	{
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Bỏ ra đất");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().dellGemItem(item.id, index, khoa);
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn bỏ hết vật phẩm này không ?", yesAction);
		};
		command_HAIRMOD_PROTECTED.action = action;
		return command_HAIRMOD_PROTECTED;
	}

	private void showMenuForPotion(int potionType)
	{
	}

	private bool isWeapone(int type)
	{
		return type >= 3 && type <= 7;
	}

	private void showCharWearingInfo()
	{
		try
		{
			int num = 0;
			int num2 = 0;
			if (selected % 3 == 1 || charWearing.isDoing)
			{
				if (charWearing.isDoing)
				{
					if (selected == 4)
					{
						return;
					}
					num2 = selected / 3 * 2 + (selected % 3 - ((selected % 3 > 0) ? 1 : 0));
					for (int i = 0; i < charWearing.wearingItems.size(); i++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(i);
						ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
						if (item.type == 13)
						{
							showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED, false, posWearChar[num2][0] + 5, posWearChar[num2][1] - 2);
						}
					}
					return;
				}
				if (index.Length == 1)
				{
					if (selected != 4)
					{
						mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Thông tin");
						ActionPerform_HAIRMOD_PROTECTED action = delegate
						{
							setShowTextNew(charWearing.infoAnimal, 100, 120);
							Canvas_HAIRMOD_PROTECTED.endDlg();
						};
						command_HAIRMOD_PROTECTED.action = action;
						command_HAIRMOD_PROTECTED.perform();
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Trang bị linh thú");
						ActionPerform_HAIRMOD_PROTECTED action2 = delegate
						{
							Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.findCharByID(Canvas_HAIRMOD_PROTECTED.gameScr.saveIDViewInfoAnimal);
							if (char_HAIRMOD_PROTECTED != null)
							{
								Canvas_HAIRMOD_PROTECTED.gameScr.gameService.requestSomeOneInfo(char_HAIRMOD_PROTECTED.ID, 1);
							}
							else
							{
								right.perform();
							}
							Canvas_HAIRMOD_PROTECTED.endDlg();
						};
						command_HAIRMOD_PROTECTED2.action = action2;
						mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
						return;
					}
					int num3 = charWearing.wearingItems.size();
					for (int j = 0; j < num3; j++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(j);
						ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED2.charClazz, itemInInventory_HAIRMOD_PROTECTED2.idTemplate);
						if (item2.type == 19)
						{
							showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED2, false, posWearChar[9][0] + 5, posWearChar[9][1] - 2);
							break;
						}
					}
					return;
				}
				if (selected != 4)
				{
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Linh thú");
					ActionPerform_HAIRMOD_PROTECTED action3 = delegate
					{
						if (!charWearing.infoAnimal.Equals(string.Empty))
						{
							setShowText(charWearing.infoAnimal, 80, 120);
						}
					};
					command_HAIRMOD_PROTECTED3.action = action3;
					mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED3);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Thú cưng");
					ActionPerform_HAIRMOD_PROTECTED action4 = delegate
					{
						if (charWearing.myPet != null)
						{
							setShowText(ItemInInventory_HAIRMOD_PROTECTED.setTemp(charWearing.myPet.infoPet, "0") + ItemInInventory_HAIRMOD_PROTECTED.setTemp("Còn lại: " + (charWearing.myPet.totalTime - (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - charWearing.myPet.timeStart) / 60000) + " phút", "0"), 80, 120);
						}
					};
					command_HAIRMOD_PROTECTED4.action = action4;
					mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED4);
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 3);
					return;
				}
				int num4 = charWearing.wearingItems.size();
				for (int k = 0; k < num4; k++)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED3 = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(k);
					ItemTemplate_HAIRMOD_PROTECTED item3 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED3.charClazz, itemInInventory_HAIRMOD_PROTECTED3.idTemplate);
					if (item3.type == 19)
					{
						showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED3, false, posWearChar[9][0] + 5, posWearChar[9][1] - 2);
						break;
					}
				}
				return;
			}
			num2 = selected / 3 * 2 + (selected % 3 - ((selected % 3 > 0) ? 1 : 0));
			for (int l = 0; l < charWearing.wearingItems.size(); l++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED4 = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingItems.elementAt(l);
				ItemTemplate_HAIRMOD_PROTECTED item4 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED4.charClazz, itemInInventory_HAIRMOD_PROTECTED4.idTemplate);
				if (item4.type == idWearChar[num2] || (idWearChar[num2] == -1 && item4.type > 2 && item4.type < 8))
				{
					num++;
					if (num2 != 7 || num != 1)
					{
						showItemInventoryInfo(itemInInventory_HAIRMOD_PROTECTED4, false, posWearChar[num2][0] + 5, posWearChar[num2][1] - 2);
						break;
					}
				}
			}
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.println("Ngoai mang");
		}
	}

	private void showItemInventoryAnimalInfo(ItemInInventory_HAIRMOD_PROTECTED item, bool isSell, int x, int y)
	{
		arrayKhamNgoc = null;
		if (item.totalKham > 0)
		{
			arrayKhamNgoc = new sbyte[item.totalKham];
			for (int i = 0; i < item.totalKham; i++)
			{
				arrayKhamNgoc[i] = -1;
			}
			for (int j = 0; j < item.property.Length; j++)
			{
				if (item.property[j] > 0)
				{
					for (int k = 0; k < item.numKham; k++)
					{
						arrayKhamNgoc[k] = Res_HAIRMOD_PROTECTED.idNgocKham[j];
					}
					break;
				}
			}
		}
		setShowText(item.getDescription(isSell), x, y);
		if (!item.isEnoughData)
		{
			item.isEnoughData = true;
			GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(item.itemID, (charWearing != null) ? charWearing.ID : Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
		}
	}

	private void showInfoWearingAnimal()
	{
		try
		{
			int num = 0;
			int num2 = 0;
			if (charWearing.wearingAnimal == null || charWearing.wearingAnimal.size() <= 0)
			{
				return;
			}
			for (int i = 0; i < charWearing.wearingAnimal.size(); i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)charWearing.wearingAnimal.elementAt(i);
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				if (item.type == idWearAnimal[selected])
				{
					num++;
					showItemInventoryAnimalInfo(itemInInventory_HAIRMOD_PROTECTED, false, posWearChar[num2][0] + 5, posWearChar[num2][1] - 2);
					break;
				}
			}
		}
		catch (Exception)
		{
		}
	}

	private void showItemInventoryInfo(ItemInInventory_HAIRMOD_PROTECTED item, bool isSell, int x, int y)
	{
		arrayKhamNgoc = null;
		if (item.totalKham > 0)
		{
			arrayKhamNgoc = new sbyte[item.totalKham];
			for (int i = 0; i < item.totalKham; i++)
			{
				arrayKhamNgoc[i] = -1;
			}
			sbyte b = 0;
			for (int j = 0; j < item.allAttribute.size(); j++)
			{
				InfoAttributeItem_HAIRMOD_PROTECTED infoAttributeItem_HAIRMOD_PROTECTED = (InfoAttributeItem_HAIRMOD_PROTECTED)item.allAttribute.elementAt(j);
				if (infoAttributeItem_HAIRMOD_PROTECTED.getColorPaint(false) == 3)
				{
					for (int k = 0; k < item.numKham; k++)
					{
						arrayKhamNgoc[b++] = Res_HAIRMOD_PROTECTED.idNgocKham[infoAttributeItem_HAIRMOD_PROTECTED.getID() - 10];
					}
					break;
				}
			}
		}
		if (index[focusTab] == 21 || index[focusTab] == 25 || index[focusTab] == 32 || isDefault || index[focusTab] == 22 || index[focusTab] == 23 || index[focusTab] == 1 || index[focusTab] == 31 || index[focusTab] == 8)
		{
			setShowTextNew(item.getDescription(isSell), 145, -10);
		}
		else
		{
			setShowText(item.getDescription(isSell), x, y);
		}
		if (!item.isEnoughData)
		{
			item.isEnoughData = true;
			GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(item.itemID, (charWearing != null) ? charWearing.ID : Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
		}
	}

	private void dofireBasePoint()
	{
		if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.basePointLeft == 0)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã hết điểm tiềm năng để cộng. Xin đánh lên level để có điểm tiềm năng.");
			return;
		}
		ActionPerform_HAIRMOD_PROTECTED ok = delegate
		{
			string text = Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText();
			if (text.Equals(string.Empty))
			{
				return;
			}
			try
			{
				int num = int.Parse(text);
				if (num > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.basePointLeft)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chỉ còn " + Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.basePointLeft + " điểm tiềm năng");
				}
				else
				{
					GameService_HAIRMOD_PROTECTED.gI().addBasePoint(selected, num);
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
				}
			}
			catch (Exception)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Có lỗi xảy ra. Vui lòng chỉ nhập số.");
			}
		};
		Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nhập số", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC, 10, true);
		Canvas_HAIRMOD_PROTECTED.inputDlg.show();
	}

	public void setCmdLeftTrade()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			closeText();
			Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendOkTrade(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
			left = new Command_HAIRMOD_PROTECTED("Xin chờ");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendCancelTrade(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isTrade = false;
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			left.action = action2;
		};
		left = new Command_HAIRMOD_PROTECTED("Xong");
		left.action = action;
	}

	public void setListSellItem(mVector_HAIRMOD_PROTECTED itemsSell, int type)
	{
		list.removeAllElements();
		int num = 0;
		for (int i = 0; i < itemsSell.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(i);
			ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(item.charClazz, item.idTemplate);
			if (item2.type != type && type != -1)
			{
				continue;
			}
			int co = num;
			num++;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				if (isDefault || index[focusTab] == 22 || index[focusTab] == 23)
				{
					setShowTextNew(item.getTemplateDescription(), co % numW * 18, co / numW * 18);
				}
				else
				{
					setShowText(item.getTemplateDescription(), co % numW * 18, co / numW * 18);
				}
			};
			ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
			{
				item.paint(g, x, y);
			};
			command_HAIRMOD_PROTECTED.action = action;
			command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
			list.addElement(command_HAIRMOD_PROTECTED);
		}
	}

	private bool setMaxxreadyBuy()
	{
		if (readyBuy.size() >= 100)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chỉ được mua 100 vật phẩm một lần.");
			return true;
		}
		return false;
	}

	private void doSetLeftSellWeapon(int type)
	{
		cmdShop = new Command_HAIRMOD_PROTECTED();
		cmdShop.action = delegate
		{
			if (!setMaxxreadyBuy())
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
				int num = 0;
				if (type != -1)
				{
					for (int i = 0; i < Canvas_HAIRMOD_PROTECTED.gameScr.shop.size(); i++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(i);
						ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED2.charClazz, itemInInventory_HAIRMOD_PROTECTED2.idTemplate);
						if (item2.type == type)
						{
							if (num == selected)
							{
								itemInInventory_HAIRMOD_PROTECTED = itemInInventory_HAIRMOD_PROTECTED2;
								break;
							}
							num++;
						}
					}
				}
				else
				{
					itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(selected);
				}
				ItemInInventory_HAIRMOD_PROTECTED item = itemInInventory_HAIRMOD_PROTECTED;
				if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.FullInventory() >= GameScr_HAIRMOD_PROTECTED.MAX_INVENTORY * Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.limItemBag)
				{
					setShowText("Hành trang đã đầy", selected % numW * 18, selected / numW * 18);
				}
				else
				{
					ItemTemplate_HAIRMOD_PROTECTED itt = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
					if (itt.price > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Hết tiền");
					}
					else
					{
						string empty = string.Empty;
						empty = ((itt.ndayLoan != 0) ? ("Bạn có muốn thuê " + itt.name + ", Giá: " + itt.price + "Lượng không?") : ("Bạn có muốn mua " + itt.name + ", Giá: " + itt.price + "$ không?"));
						ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu -= itt.price;
							readyBuy.addElement(item);
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã mua. Món đồ đang ở trong hành trang.", false);
						};
						Canvas_HAIRMOD_PROTECTED.startYesNoDlg(empty, yesAction);
					}
				}
			}
		};
	}

	private void dosetLeftSellGemItem()
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			if (!setMaxxreadyBuy())
			{
				GemTemp_HAIRMOD_PROTECTED selectedGemItem = (GemTemp_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(selected);
				bool flag = false;
				int num = 0;
				GemTemplate_HAIRMOD_PROTECTED gemByID = GemTemplate_HAIRMOD_PROTECTED.getGemByID(selectedGemItem.id);
				if (gemByID != null)
				{
					num = gemByID.number;
					flag = true;
				}
				if (!flag && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isFullInventory())
				{
					setShowText("Hành trang đã đầy", selected % numW * 18, selected / numW * 18);
				}
				else
				{
					GemTemp_HAIRMOD_PROTECTED g = Res_HAIRMOD_PROTECTED.getGemByID(selectedGemItem.id);
					int numm = num;
					ActionPerform_HAIRMOD_PROTECTED ok = delegate
					{
						try
						{
							int number = int.Parse(Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
							if (number >= 0)
							{
								if ((g.typeMoney == 0 && g.price * number > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu) || (g.typeMoney == 1 && g.price * number > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luong))
								{
									setShowText("Hết tiền", selected % numW * 18, selected / numW * 18);
								}
								else
								{
									ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
									{
										int num2 = 0;
										for (int i = 0; i < readyBuy.size(); i++)
										{
											GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)readyBuy.elementAt(i);
											if (gemTemp_HAIRMOD_PROTECTED.id == selectedGemItem.id)
											{
												if (numm + gemTemp_HAIRMOD_PROTECTED.number + number <= 300)
												{
													gemTemp_HAIRMOD_PROTECTED.number += (short)number;
													num2 = 1;
													break;
												}
												Canvas_HAIRMOD_PROTECTED.startOKDlg("Không được mua nhiều hơn 300 cái.");
												return;
											}
										}
										if (num2 == 0)
										{
											GemTemp_HAIRMOD_PROTECTED o = new GemTemp_HAIRMOD_PROTECTED
											{
												id = selectedGemItem.id,
												number = (short)number
											};
											readyBuy.addElement(o);
										}
										if (g.typeMoney == 0)
										{
											Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu -= g.price * number;
										}
										else
										{
											Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.luong -= g.price * number;
										}
										Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã mua. Món đồ đang ở trong hành trang.");
									};
									Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn mua " + number + " " + g.name + ", Giá: " + g.price * number + ((g.typeMoney != 0) ? "lượng" : "$") + " không?", yesAction);
								}
							}
						}
						catch (Exception)
						{
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Chỉ được nhập số.");
						}
					};
					Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Số lượng: ", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_NUMERIC, 4, true);
					Canvas_HAIRMOD_PROTECTED.inputDlg.show();
				}
			}
		};
		cmdShop = new Command_HAIRMOD_PROTECTED();
		cmdShop.action = action;
	}

	private void setListSellGemItem(mVector_HAIRMOD_PROTECTED shop)
	{
		list.removeAllElements();
		for (int i = 0; i < shop.size(); i++)
		{
			GemTemp_HAIRMOD_PROTECTED gem = (GemTemp_HAIRMOD_PROTECTED)shop.elementAt(i);
			int ii = i;
			if (!gem.isSell)
			{
				continue;
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gem.name, "0") + ItemInInventory_HAIRMOD_PROTECTED.setTemp(gem.decript, "0") + ItemInInventory_HAIRMOD_PROTECTED.setTemp("Giá: " + gem.price + ((gem.typeMoney != 0) ? " lượng" : " xu"), "0");
				if (isDefault || index[focusTab] == 22 || index[focusTab] == 23)
				{
					setShowTextNew(text, ii % numW * 18, ii / numW * 18);
				}
				else
				{
					setShowText(text, ii % numW * 18, ii / numW * 18);
				}
			};
			ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, gem.idImage, x, y);
			};
			command_HAIRMOD_PROTECTED.action = action;
			command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
			list.addElement(command_HAIRMOD_PROTECTED);
		}
	}

	private void setListPotion()
	{
		list.removeAllElements();
		for (int i = 0; i < Canvas_HAIRMOD_PROTECTED.gameScr.shop.size(); i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED item = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(i);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				setShowTextNew(item.getTemplateDescription(), selected % numW * 18, selected / numW * 18);
			};
			ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
			{
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(MainChar_HAIRMOD_PROTECTED.listPotion[item.potionType].index + 5500), x, y);
			};
			command_HAIRMOD_PROTECTED.action = action;
			command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
			list.addElement(command_HAIRMOD_PROTECTED);
		}
	}

	private void doSetLeftPotion()
	{
	}

	public void doBuyItem()
	{
		if (readyBuy.size() > 0)
		{
			Canvas_HAIRMOD_PROTECTED.startWaitDlg("Xin chờ", true);
			if (index[focusTab] == 10)
			{
				GameService_HAIRMOD_PROTECTED.gI().buyGemItem(readyBuy);
			}
			else if (index[focusTab] == 19)
			{
				GameService_HAIRMOD_PROTECTED.gI().buyPotion(readyBuy);
			}
			else
			{
				GameService_HAIRMOD_PROTECTED.gI().buyItem(readyBuy);
			}
			readyBuy.removeAllElements();
		}
		if (index[focusTab] == 21 || index[focusTab] == 25)
		{
			if (index[focusTab] == 21)
			{
				GameService_HAIRMOD_PROTECTED.gI().doImbueItem(-1);
			}
			else
			{
				GameService_HAIRMOD_PROTECTED.gI().doKhamItem(-1);
			}
			for (int i = 0; i < MainChar_HAIRMOD_PROTECTED.gemitemImbue.size(); i++)
			{
				RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemitemImbue.elementAt(i);
				GemTemplate_HAIRMOD_PROTECTED gemByID = GemTemplate_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED.ID);
				if (gemByID != null)
				{
					gemByID.number++;
					break;
				}
			}
			MainChar_HAIRMOD_PROTECTED.gemitemImbue.removeAllElements();
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.itemImbue = null;
			imgWeaponAvatar = null;
			idImbue = -1;
		}
		else if (index[focusTab] == 22)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.gameService.finishPutItem2Bag();
		}
		else if (index[focusTab] == 8)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.gameService.sendCancelTrade(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isTrade = false;
		}
	}

	public void resetImbue()
	{
		MainChar_HAIRMOD_PROTECTED.gemitemImbue.removeAllElements();
	}

	private void setQuestInfo()
	{
		try
		{
			questInfo = new mVector_HAIRMOD_PROTECTED();
			string infoQuestPaint = getInfoQuestPaint(0);
			string infoQuestPaint2 = getInfoQuestPaint(1);
			string infoQuestPaint3 = getInfoQuestPaint(2);
			if (!infoQuestPaint.Equals(string.Empty))
			{
				string[] array = Util_HAIRMOD_PROTECTED.split(infoQuestPaint, "|");
				questInfo.addElement(GameScr_HAIRMOD_PROTECTED.mainQuest.name);
				for (int i = 0; i < array.Length; i++)
				{
					questInfo.addElement(array[i]);
				}
			}
			if (!infoQuestPaint2.Equals(string.Empty))
			{
				string[] array2 = Util_HAIRMOD_PROTECTED.split(infoQuestPaint2, "|");
				questInfo.addElement(GameScr_HAIRMOD_PROTECTED.subQuest.name);
				for (int j = 0; j < array2.Length; j++)
				{
					questInfo.addElement(array2[j]);
				}
			}
			if (!infoQuestPaint3.Equals(string.Empty))
			{
				string[] array3 = Util_HAIRMOD_PROTECTED.split(infoQuestPaint3, "|");
				questInfo.addElement(GameScr_HAIRMOD_PROTECTED.clanQuest.name);
				for (int k = 0; k < array3.Length; k++)
				{
					questInfo.addElement(array3[k]);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void questClan(string text1, sbyte finishQ)
	{
		questInfo = new mVector_HAIRMOD_PROTECTED();
		string[] array = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(text1, Canvas_HAIRMOD_PROTECTED.w - 100);
		for (int i = 0; i < array.Length; i++)
		{
			questInfo.addElement(array[i]);
		}
		short num = (short)(Canvas_HAIRMOD_PROTECTED.h - 90 - MyScreen_HAIRMOD_PROTECTED.deltaY);
		cmy = (cmtoY = (cmyLim = 0));
		cmyLim = questInfo.size() * 13 - (num - 35);
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	private void paintShowTextNew(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 0;
		g.setClip(xShow, yShow + 1, wShow, hView - 50);
		g.translate(0, -cmyText);
		if (!isTextArr)
		{
			for (int i = 0; i < showText.Length + countPop; i++)
			{
				if (i == 1 && arrayKhamNgoc != null && arrayKhamNgoc.Length != 0)
				{
					for (int j = 0; j < arrayKhamNgoc.Length; j++)
					{
						g.drawImage(Res_HAIRMOD_PROTECTED.imgKham, xShow + 12 + j * 20, yShow + 12 + num, 3);
						if (arrayKhamNgoc[j] != -1)
						{
							Res_HAIRMOD_PROTECTED.paintGem(g, arrayKhamNgoc[j], xShow + 12 + j * 20, yShow + 12 + num);
						}
					}
					num += 18;
				}
				if (!showText[i].Equals(string.Empty))
				{
					sbyte b = (sbyte)(showText[i][0] - 48);
					sbyte startIndex = 1;
					if (!isDegit(showText[i][0]))
					{
						b = 0;
						startIndex = 0;
					}
					MFont_HAIRMOD_PROTECTED.normalFont[(b < 6) ? b : 0].drawString(g, showText[i].Substring(startIndex), xShow + 4, yShow + 4 + num, 0);
					num += Res_HAIRMOD_PROTECTED.hString;
				}
			}
			if (countPop < 0)
			{
				countPop++;
			}
			return;
		}
		for (int k = 0; k < showText.Length + countPop; k++)
		{
			if (k == 0)
			{
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, showText[k], xShow + 4, yShow + 4 + num, 0);
			}
			else
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, showText[k], xShow + 4, yShow + 4 + num, 0);
			}
			num += MainJ2ME_HAIRMOD_PROTECTED.CHAR_HEIGHT;
		}
		if (countPop < 0)
		{
			countPop++;
		}
	}

	private void setShowTextNew(string text, int x1, int y1)
	{
		isTextArr = false;
		isShowText = true;
		wShow = wKhung - (x0 - 2 + 7 * wCell);
		xShow = 7 * wCell + xTr;
		yShow = -10 + yTr;
		showText = MFont_HAIRMOD_PROTECTED.arialFontW.splitFontBStrInLine(text, www);
		hShow = hView;
		if (countPop == 0)
		{
			countPop = -(showText.Length - 1);
		}
		cmyText = (cmtoYText = (cmyLimText = 0));
		cmyLimText = showText.Length * 20 - hShow;
		if (cmyLimText < 0)
		{
			cmyLimText = 0;
		}
	}

	public void updateKeyDong()
	{
		count++;
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (timeOpen > 0)
		{
			timeOpen--;
			isPaintFc = true;
			if (timeOpen != 0L || !canSelecet)
			{
				return;
			}
			switch (curentTabUpdate)
			{
			default:
				if (selected <= -1)
				{
					break;
				}
				if (saveSelect != selected)
				{
					countClick = 0;
				}
				setCmdCenter();
				if (center == null)
				{
					break;
				}
				countClick++;
				if (saveSelect != selected)
				{
					saveSelect = selected;
				}
				else
				{
					doBuyitemshopnew();
				}
				center.perform();
				if (countClick >= 2)
				{
					if (curentTabUpdate != 0 && cmdShop != null && index[focusTab] != 24)
					{
						cmdShop.perform();
					}
					countClick = 0;
				}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 21:
			case 22:
			case 23:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
			case 32:
				break;
			}
			canSelecet = false;
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownItem && Canvas_HAIRMOD_PROTECTED.isPointer(xBegin, yBegin, wTouch, hTouch))
		{
			pyLast = Canvas_HAIRMOD_PROTECTED.pyLast;
			pxLast = Canvas_HAIRMOD_PROTECTED.pxLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownItem = false;
			timeDelay = count;
			pa = cmy;
			pb = cmx;
			xCamLast = cmx;
			transY = true;
			vY = 0f;
			vX = 0f;
		}
		if (!transY)
		{
			return;
		}
		long num = count - timeDelay;
		int num2 = pyLast - Canvas_HAIRMOD_PROTECTED.py;
		pyLast = Canvas_HAIRMOD_PROTECTED.py;
		int num3 = pxLast - Canvas_HAIRMOD_PROTECTED.px;
		pxLast = Canvas_HAIRMOD_PROTECTED.px;
		if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
		{
			if (count % 2 == 0)
			{
				dyTran = Canvas_HAIRMOD_PROTECTED.py;
				dxTran = Canvas_HAIRMOD_PROTECTED.px;
				timePointY = count;
				timePointX = count;
			}
			vY = 0f;
			vX = 0f;
			if (cmtoY <= 0 || cmtoY >= cmyLim)
			{
				if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
				{
					cmtoY = pa + Canvas_HAIRMOD_PROTECTED.dy() / 2;
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
			{
				cmtoY = pa + num2;
				pa = cmtoY;
			}
			if (cmtoX <= 0 || cmtoX >= cmxLim)
			{
				if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrX())
				{
					cmtoX = pb + Canvas_HAIRMOD_PROTECTED.dx() / 2;
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrX())
			{
				cmtoX = pb + num3;
				pb = cmtoX;
			}
			cmy = cmtoY;
			cmx = cmtoX;
			if (Main_HAIRMOD_PROTECTED.canvas.canPutKey())
			{
				timeHold++;
				if (timeHold >= 5)
				{
					timeHold = 5;
					isPaintFc = true;
					checkSelect();
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.canNotPutKey())
			{
				timeHold = 0;
				countPutKey = 0;
				closeText();
				isPaintFc = false;
				isSelectCheDo = false;
			}
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointerRelease)
		{
			return;
		}
		transY = false;
		int num4 = (int)(count - timePointY);
		float num5 = dyTran - Canvas_HAIRMOD_PROTECTED.py;
		float num6 = dxTran - Canvas_HAIRMOD_PROTECTED.px;
		if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
		{
			vY = num5 / (float)num4 * 10f;
		}
		int num7 = (int)(count - timePointX);
		if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrX())
		{
			vX = num6 / (float)num7 * 10f;
		}
		timePointY = -1L;
		timePointX = -1L;
		if (Main_HAIRMOD_PROTECTED.canvas.canPutKey() && timeHold != 5)
		{
			if (num <= 4)
			{
				timeHold = 0;
				timeOpen = 5L;
				canSelecet = true;
				checkSelect();
				countPutKey++;
			}
		}
		else if (timeHold >= 5)
		{
			timeHold = 0;
			timeOpen = 5L;
			canSelecet = true;
			checkSelect();
			countPutKey++;
		}
		transXB = false;
	}

	public void checkSelect()
	{
		switch (curentTabUpdate)
		{
		case 1:
			return;
		case 2:
			return;
		case 3:
			return;
		case 4:
			return;
		case 5:
			return;
		case 6:
			return;
		case 7:
			return;
		case 8:
			return;
		case 21:
		case 25:
			return;
		case 22:
			return;
		case 23:
			return;
		case 26:
		case 32:
			return;
		case 27:
			return;
		case 28:
			isSelectCheDo = false;
			xCus = -20;
			isChangeTab = false;
			selected = (cmtoX + Canvas_HAIRMOD_PROTECTED.px - 15 - xTr) / newZise;
			if (selected < 0)
			{
				selected = 0;
			}
			return;
		case 29:
			return;
		case 30:
			return;
		case 31:
			return;
		}
		cmtoX = 0;
		int num = Canvas_HAIRMOD_PROTECTED.pyLast - Canvas_HAIRMOD_PROTECTED.py;
		int num2 = Canvas_HAIRMOD_PROTECTED.pxLast - Canvas_HAIRMOD_PROTECTED.px;
		int num3 = 35;
		int num4 = 65;
		int num5 = (cmtoY + Canvas_HAIRMOD_PROTECTED.py - (num4 - 10) - yTr) / (hSmall + 10);
		int num6 = (cmtoX + Canvas_HAIRMOD_PROTECTED.px - (num3 - 10) - xTr) / 42;
		selected = num5 * 5 + num6;
		if (selected < 0)
		{
			selected = 0;
		}
	}

	public void moveCamera()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (vY != 0f)
		{
			if (cmy < 0 || cmy > cmyLim)
			{
				if (vY > 500f)
				{
					vY = 500f;
				}
				else if (vY < -500f)
				{
					vY = -500f;
				}
				vY -= vY / 5f;
				if (Math_HAIRMOD_PROTECTED.abs((int)(vY / 10f)) <= 10)
				{
					vY = 0f;
				}
			}
			cmy += (int)(vY / 15f);
			cmtoY = cmy;
			vY -= vY / 20f;
		}
		else if (cmy < 0)
		{
			cmtoY = 0;
		}
		else if (cmy > cmyLim)
		{
			cmtoY = cmyLim;
		}
		if (vX != 0f)
		{
			if (cmx < 0 || cmx > cmxLim)
			{
				if (vX > 500f)
				{
					vX = 500f;
				}
				else if (vX < -500f)
				{
					vX = -500f;
				}
				vX -= vX / 5f;
				if (Math_HAIRMOD_PROTECTED.abs((int)(vX / 10f)) <= 10)
				{
					vX = 0f;
				}
			}
			cmx += (int)(vX / 15f);
			cmtoX = cmx;
			vX -= vX / 20f;
		}
		else if (curentTabUpdate != 32 && curentTabUpdate != 26)
		{
			if (cmx < 0)
			{
				cmtoX = 0;
			}
			else if (cmx > cmxLim)
			{
				cmtoX = cmxLim;
			}
		}
		else if (cmx <= 0)
		{
			cmtoX = 0;
		}
		else if (cmx > cmxLim)
		{
			cmtoX = cmxLim;
		}
		if (cmy != cmtoY)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
		}
		if (cmx != cmtoX)
		{
			cmvx = cmtoX - cmx << 2;
			cmdx += cmvx;
			cmx += cmdx >> 4;
			cmdx &= 15;
		}
	}

	public void updateKeyShowText()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		countShowText++;
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownText && Canvas_HAIRMOD_PROTECTED.isPointer(xBegint, yBegint + yTr, wToucht, hToucht))
		{
			pyLastt = Canvas_HAIRMOD_PROTECTED.pyLast;
			Canvas_HAIRMOD_PROTECTED.isPointerDownText = false;
			timeDelayt = countShowText;
			pa = cmyText;
			transYShowText = true;
			vYT = 0f;
		}
		if (transYShowText)
		{
			long num = countShowText - timeDelayt;
			int num2 = pyLastt - Canvas_HAIRMOD_PROTECTED.py;
			pyLastt = Canvas_HAIRMOD_PROTECTED.py;
			if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
			{
				if (countShowText % 2 == 0)
				{
					dyTrant = Canvas_HAIRMOD_PROTECTED.py;
					timePointYt = countShowText;
				}
				vYT = 0f;
				if (cmtoYText <= 0 || cmtoYText >= cmyLimText)
				{
					if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
					{
						cmtoYText = pa + Canvas_HAIRMOD_PROTECTED.dy() / 2;
					}
				}
				else if (Canvas_HAIRMOD_PROTECTED.beginMoveCmrY())
				{
					cmtoYText = pa + num2;
					pa = cmtoYText;
				}
				cmyText = cmtoYText;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointerRelease)
			{
				transYShowText = false;
				int num3 = (int)(countShowText - timePointYt);
				float num4 = dyTrant - Canvas_HAIRMOD_PROTECTED.py;
				float num5 = dxTrant - Canvas_HAIRMOD_PROTECTED.px;
				vYT = num4 / (float)num3 * 10f;
				timePointYt = -1L;
			}
		}
		moveCameraShowText();
	}

	public void moveCameraShowText()
	{
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu || Canvas_HAIRMOD_PROTECTED.currentDialog != null)
		{
			return;
		}
		if (vYT != 0f)
		{
			if (cmyText < 0 || cmyText > cmyLimText)
			{
				if (vYT > 500f)
				{
					vYT = 500f;
				}
				else if (vYT < -500f)
				{
					vYT = -500f;
				}
				vYT -= vYT / 5f;
				if (Math_HAIRMOD_PROTECTED.abs((int)(vYT / 10f)) <= 10)
				{
					vYT = 0f;
				}
			}
			cmyText += (int)(vYT / 15f);
			cmtoYText = cmyText;
			vYT -= vYT / 20f;
		}
		else if (cmyText < 0)
		{
			cmtoYText = 0;
		}
		else if (cmyText > cmyLimText)
		{
			cmtoYText = cmyLimText;
		}
		if (cmyText != cmtoYText)
		{
			cmvyText = cmtoYText - cmyText << 2;
			cmdyText += cmvyText;
			cmyText += cmdyText >> 4;
			cmdyText &= 15;
		}
	}

	private void doSetLeftPetEat(int type)
	{
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			closeText();
			if (!setMaxxreadyBuy())
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = null;
				int num = 0;
				if (type != -1)
				{
					int num2 = Canvas_HAIRMOD_PROTECTED.gameScr.shop.size();
					for (int i = 0; i < num2; i++)
					{
						ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(i);
						ItemTemplate_HAIRMOD_PROTECTED item2 = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED2.charClazz, itemInInventory_HAIRMOD_PROTECTED2.idTemplate);
						if (item2.type == type)
						{
							if (num == selected)
							{
								itemInInventory_HAIRMOD_PROTECTED = itemInInventory_HAIRMOD_PROTECTED2;
								break;
							}
							num++;
						}
					}
				}
				else
				{
					itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Canvas_HAIRMOD_PROTECTED.gameScr.shop.elementAt(selected);
				}
				ItemInInventory_HAIRMOD_PROTECTED item = itemInInventory_HAIRMOD_PROTECTED;
				ItemTemplate_HAIRMOD_PROTECTED itt = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				string empty = string.Empty;
				empty = "Bạn có muốn cho thú cưng ăn vật phẩm này không?";
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.charXu -= itt.price;
					readyBuy.addElement(item);
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã chọn vật phẩm cho thú cưng ăn.", false);
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg(empty, yesAction);
			}
		};
		left = new Command_HAIRMOD_PROTECTED("Mua");
		left.action = action;
	}
}
