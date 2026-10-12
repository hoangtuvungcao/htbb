using System;
using System.Collections;
using System.IO;
using UnityEngine;

public class GameScr_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public class Ticker
	{
		private int x;

		private int y;

		private int y1;

		public bool end;

		public string info = string.Empty;

		public Ticker(string info, int x, int y)
		{
			this.info = info;
			this.x = x;
			this.y = y;
		}

		public void paint(MyGraphics_HAIRMOD_PROTECTED g)
		{
			g.setColor(0, 0.5f);
			g.fillRect(Canvas_HAIRMOD_PROTECTED.w15 - 10, Canvas_HAIRMOD_PROTECTED.h / 4 + 15 - MFont_HAIRMOD_PROTECTED.borderFont.getHeight() / 2, Canvas_HAIRMOD_PROTECTED.w - (2 * Canvas_HAIRMOD_PROTECTED.w15 - 20), MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() + 6);
			g.setClip(Canvas_HAIRMOD_PROTECTED.w15, 0, Canvas_HAIRMOD_PROTECTED.w - 2 * Canvas_HAIRMOD_PROTECTED.w15, Canvas_HAIRMOD_PROTECTED.h);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, info, x, y + 2, 0);
		}

		public void update()
		{
			if (x + MFont_HAIRMOD_PROTECTED.normalFont[0].getWidth(info) < 60)
			{
				end = true;
			}
			x -= 2;
		}
	}

	public const sbyte STATE_FINISH = 1;

	public const sbyte STATE_WORKING = 2;

	public const sbyte STATE_UN_RECEIVE = 0;

	public static string nameMap = string.Empty;

	public static readonly sbyte KILL_MOSTER = 0;

	public static readonly sbyte GET_ITEM = 2;

	public static readonly sbyte TRANSPORT = 1;

	public int gssx;

	public int gssy;

	public int gssw;

	public int gssh;

	public int gssxe;

	public int gssye;

	private int[] CMVIBRATEX = new int[4] { -1, 2, 1, -2 };

	private int[] CMVIBRATEY = new int[4] { -3, 2, -1, 1 };

	private int currentCmVibrate = -1;

	public static int cmtoX;

	public static int cmtoY;

	public static int cmvx;

	public static int cmvy;

	public static int cmdx;

	public static int cmdy;

	public static int cmx;

	public static int cmy;

	public static int cmxLim;

	public static int cmyLim;

	public static bool lockcmx;

	public static bool lockcmy;

	public mVector_HAIRMOD_PROTECTED actors = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED npc_limit = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED npc_Limit = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED arrowsUp = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED arrowsDown = new mVector_HAIRMOD_PROTECTED();

	public static Actor_HAIRMOD_PROTECTED objcountDown;

	public int hideGUI;

	public MainChar_HAIRMOD_PROTECTED mainChar = new MainChar_HAIRMOD_PROTECTED();

	public Actor_HAIRMOD_PROTECTED focusedActor;

	public bool readyGetGameLogic = true;

	public bool onoffInvite;

	public bool lockPosition = true;

	public bool isFillScr;

	public static bool isFight;

	public mVector_HAIRMOD_PROTECTED shop = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED shopSpecial = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED infoNewSkill = new mVector_HAIRMOD_PROTECTED();

	public Position_HAIRMOD_PROTECTED posBoss;

	public int timerung;

	private bool changeSinceLastUpdate;

	public static int gsw;

	public static int gsh;

	public static int gscmdBarY;

	public static int gshw;

	public static int gshh;

	public static int gswd3;

	public static int gshd3;

	public static int gsw2d3;

	public static int gsh2d3;

	public static int gsw3d4;

	public static int gsh3d4;

	public static int gswd6;

	public static int gshd6;

	public static mVector_HAIRMOD_PROTECTED serverInfo = new mVector_HAIRMOD_PROTECTED();

	public GameService_HAIRMOD_PROTECTED gameService = GameService_HAIRMOD_PROTECTED.gI();

	public string[] flyTextString;

	public int[] flyTextX;

	public int[] flyTextY;

	public int[] flyTextDx;

	public int[] flyTextDy;

	public int[] flyTextState;

	public int[] flyTextColor;

	public short saveIDViewInfoAnimal = -1;

	public bool chatMode;

	public bool isBuffAuto;

	public TField_HAIRMOD_PROTECTED tfChat = new TField_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED chatHistory = new mVector_HAIRMOD_PROTECTED();

	public static int tempIDActorParty = -1;

	public short questID = 1;

	public short current_npc_talk = -1;

	public int indexContent = -1;

	public short idNpcReceive = 5;

	public static sbyte idItemClanquest = -1;

	public static Position_HAIRMOD_PROTECTED posNpcReceiveClan;

	public bool receiveQuest;

	public string[] content_quest;

	public static string infoQuest = "Giết 10 con nhím, sau đó quay về nói chuyện với  NPC";

	public static string info_gif_quest = null;

	public static string titleQuest = string.Empty;

	public static Image_HAIRMOD_PROTECTED imgInfo;

	public static Image_HAIRMOD_PROTECTED imgHpMonster;

	public static Image_HAIRMOD_PROTECTED img12plus;

	public static Image_HAIRMOD_PROTECTED petShadow;

	public static Image_HAIRMOD_PROTECTED[] imgCong;

	public static Image_HAIRMOD_PROTECTED[] imghealth;

	public Position_HAIRMOD_PROTECTED posNpcRequest;

	private int yMsgChat;

	private int toYchat = -18;

	private int startMsg;

	public static mVector_HAIRMOD_PROTECTED aeo = new mVector_HAIRMOD_PROTECTED();

	public int indexBuff = -1;

	public static long timeBuff = -1L;

	public static int indexTabSlot = 0;

	public static int xSlot = 0;

	public static int idMapPK = -1;

	public static int iTaskAuto = 0;

	public static Actor_HAIRMOD_PROTECTED congThanh;

	public static sbyte xArena;

	public static sbyte yArena;

	public static sbyte wArena;

	public static sbyte hArena;

	public static long[] timeChiemThanh;

	public static long[] curTimeCT;

	public static short[] idChiemThanh;

	public static string[] nameChiemThanh;

	public static string[] nameClan;

	private static bool isFindChar = false;

	private static bool isActorMove = false;

	private static bool isFindMonster = false;

	public static mVector_HAIRMOD_PROTECTED effAnimate = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED effManager;

	public static mVector_HAIRMOD_PROTECTED savemsgWorld = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED msgWorld = new mVector_HAIRMOD_PROTECTED();

	public static string[] nameCountry = new string[2] { "Thanh Long", "Hắc Hổ" };

	public static sbyte typeOptionFocus = 0;

	public static bool[] isPaintFcBt = new bool[9];

	public static bool[] isPaintFcBtMHP = new bool[2];

	public static long timeMove = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();

	public static short dtmove = 250;

	public static MyRandom_HAIRMOD_PROTECTED r = new MyRandom_HAIRMOD_PROTECTED();

	private int HPBARW;

	private int HPBARW_MONSTER;

	private Position_HAIRMOD_PROTECTED posMiniMap;

	public static int wMiniMap;

	public static int hMiniMap;

	private Position_HAIRMOD_PROTECTED[] posQuickSlot;

	public long lastTimePing;

	public long pingDelay;

	private int pingNextIn = 10;

	public int[] DELTACAMERAX = new int[4] { 0, 0, -20, 20 };

	public int[] DELTACAMERAY = new int[4] { 20, -20, 0, 0 };

	private int ccc;

	public static long timeStandWhenAuto;

	private int[] xChange = new int[3] { 4, 0, -4 };

	private int countChang;

	private int xMsgServer;

	private static readonly int[][] C = new int[4][]
	{
		new int[4] { -90, 90, -90, 90 },
		new int[4] { -90, 90, -90, 90 },
		new int[4] { -90, 90, -90, 90 },
		new int[4] { -90, 90, -90, 90 }
	};

	public mVector_HAIRMOD_PROTECTED nearActors = new mVector_HAIRMOD_PROTECTED();

	private static readonly sbyte[][] DX = new sbyte[4][]
	{
		new sbyte[4] { 0, 0, -48, 48 },
		new sbyte[4] { 0, 0, -32, 32 },
		new sbyte[4] { 0, 0, -16, 16 },
		new sbyte[4]
	};

	private static readonly sbyte[][] DY = new sbyte[4][]
	{
		new sbyte[4] { 48, -48, 0, 0 },
		new sbyte[4] { 32, -32, 0, 0 },
		new sbyte[4] { 16, -16, 0, 0 },
		new sbyte[4]
	};

	private static readonly sbyte DELAY_BETWEEN_PING = 30;

	public static readonly sbyte MAX_INVENTORY = 42;

	private sbyte lastDir;

	private sbyte dir;

	private sbyte countStep;

	private short moveToX;

	private short moveToY;

	private int c;

	public int[] QUICKSLOT_KEY = new int[5] { 1, 3, 5, 7, 9 };

	private bool finishQuest;

	private int dem = 1;

	private int t = (int)(mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000);

	public static bool autoFight = false;

	public static bool saveAutoFight;

	public static bool isBeginAutoBuff;

	public static bool isAutoBomHp;

	public static bool isAutoBomMp;

	public static bool autoBomHMP;

	public bool isAutoRangeWhenAuto;

	public static long timeDelayHpMp;

	public static Quest_HAIRMOD_PROTECTED currQuest = null;

	private long timeTaskAuto;

	public static int timeDelayTask = 50;

	private sbyte[] X_FOWARD = new sbyte[4] { 0, 0, -17, 17 };

	private sbyte[] Y_FOWARD = new sbyte[4] { 17, -17, 0, 0 };

	private Position_HAIRMOD_PROTECTED posTo;

	public static bool cheat = false;

	private int[] SLOTINDEXOFKEY = new int[10] { 0, 0, 0, 1, 0, 2, 0, 3, 0, 4 };

	private bool first;

	public static int timeRemovePos;

	private Target_HAIRMOD_PROTECTED tg = new Target_HAIRMOD_PROTECTED();

	public int a;

	public int b;

	public int coutChangeFocusWhenDoing;

	private int[] colorPaint = new int[4] { 16520709, 16499718, 396543, 1101907 };

	private static int[] colorMini = new int[3] { 6898216, 11897430, 14469298 };

	public bool isBatMiniMap;

	private static int xF;

	private static int yF;

	private static int yTouchBar;

	private static int gH;

	private static int cmdBarH;

	private static int xU;

	private static int xD;

	private static int xL;

	private static int xR;

	private static int yU;

	private static int yD;

	private static int yR;

	private static int yL;

	private static int xMp;

	private static int yMp;

	private static int xHp;

	private static int yHp;

	private static int xOne;

	private static int yOne;

	private static int xThree;

	private static int yThree;

	private static int xFire;

	private static int yFire;

	private static int xCenTer;

	private static int yCenter;

	private static int xChatTouch;

	private static int yChatTouch;

	private static int xSelectFc;

	private static int ySelectFc;

	private static int xChangeTab;

	private static int yChangeTab;

	private static int xChangeFocus;

	private static int yChangeFocus;

	private static int xChangeClothes;

	private static int yChangeClothes;

	private sbyte idInfo;

	private sbyte xInfo;

	private sbyte yInfo;

	public Ticker tk;

	private bool isCloseChat;

	private bool isPaintQuest;

	private static int[] colorSlot = new int[5] { 3158321, 88080384, 2122, 2037000, 256 };

	public short map = -1;

	public bool isNewVersionAvailable;

	public static short[] idArrMap;

	public static short[][] mapID;

	private static short[] offMapID = new short[43]
	{
		90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
		100, 102, 103, 104, 108, 109, 260, 261, 262, 263,
		264, 265, 266, 267, 268, 269, 270, 271, 29, 30,
		31, 30, 31, 72, 73, 74, 78, 79, 82, 83,
		84, 88, 89
	};

	private static int oldMap = -1;

	public short xBeginFrame;

	public short yBeginFrame;

	public short xTheendFrame;

	public short yTheendFrame;

	public static int paintCay = 0;

	public static int paintChar = 1;

	public mVector_HAIRMOD_PROTECTED decriptNap = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED syntaxNap = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED centerNap = new mVector_HAIRMOD_PROTECTED();

	public static int pTicket = 1000;

	public static int cmtoYmini;

	public static int cmyMini;

	public static int cmdyMini;

	public static int cmvyMini;

	public static int cmtoXMini;

	public static int cmxMini;

	public static int cmdxMini;

	public static int cmvxMini;

	public static long timeTranMini;

	private Position_HAIRMOD_PROTECTED posCam;

	public static short timePutKeyMP;

	public static short timePutKeyHP;

	private bool isNhanPhim;

	public static sbyte[][] mapFind;

	public static sbyte xStart;

	public static sbyte yStart;

	public static bool isFixBugAutoQuest;

	public int iTaskAutoLast;

	public static sbyte weather = -1;

	public mVector_HAIRMOD_PROTECTED objDynamic = new mVector_HAIRMOD_PROTECTED();

	private mVector_HAIRMOD_PROTECTED listMSGServer = new mVector_HAIRMOD_PROTECTED();

	public SkillClan_HAIRMOD_PROTECTED[] skillClan;

	public SkillClan_HAIRMOD_PROTECTED[] skillClanPrivate;

	private static Scroll_HAIRMOD_PROTECTED scrInfo = new Scroll_HAIRMOD_PROTECTED();

	public static Scroll_HAIRMOD_PROTECTED scrMain = new Scroll_HAIRMOD_PROTECTED();

	public static sbyte STATE_DONE = 3;

	public static Quest_HAIRMOD_PROTECTED mainQuest;

	public static Quest_HAIRMOD_PROTECTED subQuest;

	public static Quest_HAIRMOD_PROTECTED clanQuest;

	public static mVector_HAIRMOD_PROTECTED allQuestCanReceive = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED PosNPCQuest = new mVector_HAIRMOD_PROTECTED();

	public static CharCountDown_HAIRMOD_PROTECTED charcountdonw;

	public mVector_HAIRMOD_PROTECTED VecTime = new mVector_HAIRMOD_PROTECTED();

	public static sbyte[][] TYPE_MP_HP = new sbyte[2][];

	public static int[][] VALUE_MP_HP = new int[2][];

	public GameScr_HAIRMOD_PROTECTED()
	{
		tfChat = new TField_HAIRMOD_PROTECTED();
		tfChat.height = MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2;
		tfChat.isFocus = true;
		tfChat.setMaxTextLenght(80);
		init();
		isBuffAuto = false;
		loadFlyText();
		setPosMiniMap();
		HPBARW = 45;
		HPBARW_MONSTER = 50;
		if (HPBARW_MONSTER < 30)
		{
			HPBARW_MONSTER = 30;
		}
		isPaintQuest = true;
		xMsgServer = Canvas_HAIRMOD_PROTECTED.w - 20;
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			return;
		}
		left = new Command_HAIRMOD_PROTECTED(string.Empty);
		left.action = delegate
		{
			if (mainChar.hp > 0 && !isCloseChat)
			{
				MenuWindows_HAIRMOD_PROTECTED.gI().switchToMe();
				mainChar.posTransRoad = null;
				Canvas_HAIRMOD_PROTECTED.isPointerClick = (Canvas_HAIRMOD_PROTECTED.isPointerDown = false);
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
			}
			isCloseChat = false;
		};
		right = new Command_HAIRMOD_PROTECTED(string.Empty);
		right.action = delegate
		{
			changeToNextFocusActor(false);
		};
	}

	public override void switchToMe()
	{
		Canvas_HAIRMOD_PROTECTED.keyHold[2] = false;
		base.switchToMe();
		init();
		if (mainChar != null)
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.posTransRoad = null;
		}
	}

	public void addChat(ChatInfo_HAIRMOD_PROTECTED ci)
	{
		if (chatHistory.size() > 50)
		{
			chatHistory.removeElementAt(0);
			yMsgChat -= 18;
			toYchat -= 18;
		}
		chatHistory.addElement(ci);
		toYchat += 18;
	}

	public bool canPaintIsShowMenu()
	{
		if (Main_HAIRMOD_PROTECTED.isPC)
		{
			return true;
		}
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap && Canvas_HAIRMOD_PROTECTED.menu.menuItems.size() >= 5)
		{
			return false;
		}
		return true;
	}

	public void loadFlyText()
	{
		flyTextX = new int[15];
		flyTextY = new int[15];
		flyTextDx = new int[15];
		flyTextDy = new int[15];
		flyTextState = new int[15];
		flyTextColor = new int[15];
		flyTextString = new string[15];
		for (int i = 0; i < 15; i++)
		{
			flyTextState[i] = -1;
		}
	}

	public bool checkParseString(string str)
	{
		char[] array = new char[10] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		char c = 'w';
		if (str.Length > 0)
		{
			c = str[0];
		}
		int num = array.Length;
		for (int i = 0; i < num; i++)
		{
			if (c == array[i])
			{
				return true;
			}
		}
		return false;
	}

	public string cutStringFromString(string data)
	{
		char[] array = new char[10] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		string text = string.Empty;
		sbyte b = (sbyte)data.Length;
		short num = (short)array.Length;
		for (int i = 0; i < b; i++)
		{
			for (int j = 0; j < num; j++)
			{
				char c = data[i];
				char c2 = array[j];
				if (c == c2)
				{
					text += c;
					break;
				}
			}
		}
		return text;
	}

	public void startFlyText(string flyString, int flyColor, int x, int y, int dx, int dy)
	{
		string text = cutStringFromString(flyString);
		if (checkParseString(text.Substring(0)))
		{
			int num = 0;
			num = int.Parse(text.Substring(0));
			if (num == 2000000)
			{
				return;
			}
		}
		int num2 = -1;
		for (int i = 0; i < 15; i++)
		{
			if (flyTextState[i] == -1)
			{
				num2 = i;
				break;
			}
		}
		if (num2 != -1)
		{
			flyTextString[num2] = flyString.ToLower();
			flyTextX[num2] = x;
			flyTextY[num2] = y;
			flyTextDx[num2] = dx * ((r.nextInt(2) != 1) ? 1 : (-1));
			flyTextDy[num2] = dy;
			flyTextState[num2] = 0;
			flyTextColor[num2] = flyColor;
		}
	}

	public void updateFlyText()
	{
		for (int i = 0; i < 15; i++)
		{
			if (flyTextState[i] != -1)
			{
				flyTextState[i] += Util_HAIRMOD_PROTECTED.abs(flyTextDy[i]);
				if (flyTextState[i] > 30)
				{
					flyTextState[i] = -1;
				}
				flyTextX[i] += flyTextDx[i];
				flyTextY[i] += flyTextDy[i];
			}
		}
	}

	public void paintFlyText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!canPaintIsShowMenu())
		{
			return;
		}
		for (int i = 0; i < 15; i++)
		{
			if (flyTextState[i] != -1)
			{
				if (flyTextColor[i] != 0)
				{
					MFont_HAIRMOD_PROTECTED.smallFontColor[flyTextColor[i]].drawString(g, flyTextString[i], flyTextX[i], flyTextY[i], 0);
				}
				else
				{
					MFont_HAIRMOD_PROTECTED.smallFontYellow.paintFontBitMap(g, flyTextString[i], flyTextX[i], flyTextY[i], 0);
				}
			}
		}
	}

	public override void init()
	{
		tfChat.x = 2;
		tfChat.y = Canvas_HAIRMOD_PROTECTED.h - 40;
		tfChat.width = Canvas_HAIRMOD_PROTECTED.w - 4;
		if (posQuickSlot == null)
		{
			posQuickSlot = new Position_HAIRMOD_PROTECTED[5];
			for (int i = 0; i < 5; i++)
			{
				posQuickSlot[i] = new Position_HAIRMOD_PROTECTED();
			}
		}
		posQuickSlot[0].x = Canvas_HAIRMOD_PROTECTED.hw - 47;
		posQuickSlot[0].y = Canvas_HAIRMOD_PROTECTED.h - 19;
		posQuickSlot[1].x = Canvas_HAIRMOD_PROTECTED.hw - 28;
		posQuickSlot[1].y = Canvas_HAIRMOD_PROTECTED.h - 19;
		posQuickSlot[2].x = Canvas_HAIRMOD_PROTECTED.hw - 8;
		posQuickSlot[2].y = Canvas_HAIRMOD_PROTECTED.h - 20;
		posQuickSlot[3].x = Canvas_HAIRMOD_PROTECTED.hw + 12;
		posQuickSlot[3].y = Canvas_HAIRMOD_PROTECTED.h - 19;
		posQuickSlot[4].x = Canvas_HAIRMOD_PROTECTED.hw + 31;
		posQuickSlot[4].y = Canvas_HAIRMOD_PROTECTED.h - 19;
		setPosMiniMap();
		loadCamera();
	}

	public void setPosMiniMap()
	{
		if (Tilemap_HAIRMOD_PROTECTED.imgMap == null)
		{
			return;
		}
		hMiniMap = Canvas_HAIRMOD_PROTECTED.h / 5;
		if (hMiniMap > 100)
		{
			hMiniMap = 100;
		}
		posMiniMap = new Position_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.hw + 50, 0);
		wMiniMap = Canvas_HAIRMOD_PROTECTED.w - posMiniMap.x - 1;
		if (wMiniMap > 100)
		{
			wMiniMap = 100;
		}
		if (Tilemap_HAIRMOD_PROTECTED.imgMap != null)
		{
			if (wMiniMap > Tilemap_HAIRMOD_PROTECTED.imgMap.getWidth())
			{
				wMiniMap = Tilemap_HAIRMOD_PROTECTED.imgMap.getWidth();
			}
			if (hMiniMap > Tilemap_HAIRMOD_PROTECTED.imgMap.getHeight())
			{
				hMiniMap = Tilemap_HAIRMOD_PROTECTED.imgMap.getHeight();
			}
		}
		posMiniMap.x = Canvas_HAIRMOD_PROTECTED.w - wMiniMap - 1;
		posMiniMap.y = 0;
	}

	public void loadCamera()
	{
		gsw = Canvas_HAIRMOD_PROTECTED.w;
		gsh = Canvas_HAIRMOD_PROTECTED.h;
		gshw = gsw >> 1;
		gshh = gsh >> 1;
		gswd3 = gsw / 3;
		gshd3 = gsh / 3;
		gsw2d3 = 2 * gsw / 3;
		gsh2d3 = 2 * gsh / 3;
		gsw3d4 = 3 * gsw / 4;
		gsh3d4 = 3 * gsh / 4;
		gswd6 = gsw / 6;
		gshd6 = gsh / 6;
		gssw = (gsw >> 4) + 2;
		gssh = (gsh >> 4) + 2;
		if (gsw % 24 != 0)
		{
			gssw++;
		}
		cmxLim = (Tilemap_HAIRMOD_PROTECTED.w << 4) - gsw - 10;
		cmyLim = (Tilemap_HAIRMOD_PROTECTED.h << 4) - gsh - 30;
		if (mainChar != null)
		{
			cmx = (cmtoX = mainChar.x - gshw);
			cmy = (cmtoY = mainChar.y - gshh);
		}
		if (cmx < 0)
		{
			cmx = 0;
		}
		if (cmx > cmxLim)
		{
			cmx = cmxLim;
		}
		if (cmy < 0)
		{
			cmy = 0;
		}
		if (cmy > cmyLim)
		{
			cmy = cmyLim;
		}
		gssx = (cmx >> 4) - 1;
		if (gssx < 0)
		{
			gssx = 0;
		}
		gssy = cmy >> 4;
		gssxe = gssx + gssw;
		gssye = gssy + gssh;
		if (gssy < 0)
		{
			gssy = 0;
		}
		if (gssye > Tilemap_HAIRMOD_PROTECTED.h - 1)
		{
			gssye = Tilemap_HAIRMOD_PROTECTED.h - 1;
		}
		lockcmx = (lockcmy = false);
		if (Tilemap_HAIRMOD_PROTECTED.pxw < gsw + 32)
		{
			lockcmx = true;
			cmx = -(gsw - Tilemap_HAIRMOD_PROTECTED.pxw) >> 1;
		}
		if (Tilemap_HAIRMOD_PROTECTED.pxh < gsh)
		{
			lockcmy = true;
			cmy = -(gsh - Tilemap_HAIRMOD_PROTECTED.pxh) >> 1;
		}
		setDataForButton();
	}

	public void updateCamera()
	{
		if (cmx != cmtoX && !lockcmx)
		{
			cmvx = cmtoX - cmx << 2;
			cmdx += cmvx;
			cmx += cmdx >> 4;
			cmdx &= 15;
			if (cmx < 0)
			{
				cmx = 0;
			}
			if (cmx > cmxLim)
			{
				cmx = cmxLim;
			}
		}
		if (cmy != cmtoY && !lockcmy)
		{
			cmvy = cmtoY - cmy << 2;
			cmdy += cmvy;
			cmy += cmdy >> 4;
			cmdy &= 15;
			if (cmy < 0)
			{
				cmy = 0;
			}
			if (cmy > cmyLim)
			{
				cmy = cmyLim;
			}
		}
		if (currentCmVibrate >= 0)
		{
			cmx += CMVIBRATEX[currentCmVibrate];
			cmy += CMVIBRATEY[currentCmVibrate];
			currentCmVibrate++;
			if (currentCmVibrate == 4)
			{
				currentCmVibrate = -1;
			}
			if (cmx < 0)
			{
				cmx = 0;
			}
			if (cmx > cmxLim)
			{
				cmx = cmxLim;
			}
			if (cmy < 0)
			{
				cmy = 0;
			}
			if (cmy > cmyLim)
			{
				cmy = cmyLim;
			}
		}
		gssx = (cmx >> 4) - 1;
		if (gssx < 0)
		{
			gssx = 0;
		}
		gssy = cmy >> 4;
		gssxe = gssx + gssw;
		if (gssxe > Tilemap_HAIRMOD_PROTECTED.w)
		{
			gssxe = Tilemap_HAIRMOD_PROTECTED.w;
		}
		if (gssye > Tilemap_HAIRMOD_PROTECTED.h)
		{
			gssye = Tilemap_HAIRMOD_PROTECTED.h;
		}
		gssye = gssy + gssh;
		if (gssy < 0)
		{
			gssy = 0;
		}
		if (gssye > Tilemap_HAIRMOD_PROTECTED.h - 1)
		{
			gssye = Tilemap_HAIRMOD_PROTECTED.h - 1;
		}
		scrMain.updatecm();
		scrInfo.updatecm();
	}

	public void updateMainCharWhenAuto()
	{
		if (!autoFight)
		{
			isAutoRangeWhenAuto = false;
		}
		else if (timeStandWhenAuto - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 <= 0)
		{
			gameService.moveChar(mainChar.x, mainChar.y);
			timeStandWhenAuto = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 + 10;
		}
	}

	public void updateTime()
	{
		for (int i = 0; i < VecTime.size(); i++)
		{
			TimecountDown_HAIRMOD_PROTECTED timecountDown_HAIRMOD_PROTECTED = (TimecountDown_HAIRMOD_PROTECTED)VecTime.elementAt(i);
			if (timecountDown_HAIRMOD_PROTECTED != null)
			{
				timecountDown_HAIRMOD_PROTECTED.update();
				if (timecountDown_HAIRMOD_PROTECTED.wantdestroy)
				{
					VecTime.removeElement(timecountDown_HAIRMOD_PROTECTED);
				}
			}
		}
	}

	public override void update()
	{
		timeRemovePos--;
		if (timeRemovePos < 0)
		{
			timeRemovePos = 0;
		}
		int num = aeo.size();
		for (int i = 0; i < num; i++)
		{
			((Effect_HAIRMOD_PROTECTED)aeo.elementAt(i))?.update();
		}
		updateTime();
		if (charcountdonw != null)
		{
			charcountdonw.update();
			if (charcountdonw.WantDestroy)
			{
				charcountdonw = null;
				objcountDown = null;
			}
		}
		if (tk != null)
		{
			tk.update();
			if (tk.end)
			{
				tk = null;
			}
		}
		if (tg != null && mainChar.posTransRoad != null)
		{
			if (posCam != null)
			{
				tg.update();
				tg.x = (short)posCam.x;
				tg.y = (short)posCam.y;
			}
			else
			{
				tg.index = 0;
			}
		}
		updateCamera();
		cmtoX = mainChar.x - gshw + DELTACAMERAX[mainChar.dir];
		cmtoY = mainChar.y - gshh + DELTACAMERAY[mainChar.dir] - 20;
		updateCmMini();
		for (int num2 = arrowsUp.size() - 1; num2 >= 0; num2--)
		{
			((IArrow_HAIRMOD_PROTECTED)arrowsUp.elementAt(num2))?.update();
		}
		for (int num3 = arrowsDown.size() - 1; num3 >= 0; num3--)
		{
			((IArrow_HAIRMOD_PROTECTED)arrowsDown.elementAt(num3))?.update();
		}
		int num4 = npc_Limit.size();
		for (int j = 0; j < num4; j++)
		{
			((Actor_HAIRMOD_PROTECTED)npc_Limit.elementAt(j))?.update();
		}
		PosNPCQuest.removeAllElements();
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int k = 0; k < actors.size(); k++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(k);
			if (actor_HAIRMOD_PROTECTED.isNPC())
			{
				getAllPosNPCMInimap(actor_HAIRMOD_PROTECTED);
			}
			if (actor_HAIRMOD_PROTECTED.ispetTool() && actor_HAIRMOD_PROTECTED.wantDestroy)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(actor_HAIRMOD_PROTECTED);
			}
			if (actor_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
			if (actor_HAIRMOD_PROTECTED.catagory == 2 || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_TREE || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_PET)
			{
				actor_HAIRMOD_PROTECTED.update();
				continue;
			}
			actor_HAIRMOD_PROTECTED.update();
			if (!Util_HAIRMOD_PROTECTED.inDataRange(actor_HAIRMOD_PROTECTED, mainChar) && actor_HAIRMOD_PROTECTED != congThanh && actor_HAIRMOD_PROTECTED.catagory != Actor_HAIRMOD_PROTECTED.CAT_CAN_NOT_FOCUS && actor_HAIRMOD_PROTECTED.catagory != Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.timeLive == -1)
			{
				actor_HAIRMOD_PROTECTED.wantDestroy = true;
			}
			if (!actor_HAIRMOD_PROTECTED.wantDestroy || actor_HAIRMOD_PROTECTED.timeLive != -1)
			{
				continue;
			}
			if (focusedActor == actor_HAIRMOD_PROTECTED)
			{
				focusedActor = null;
			}
			if (!isFindChar && !isActorMove && !isFindMonster && actor_HAIRMOD_PROTECTED.catagory != 100)
			{
				if (actor_HAIRMOD_PROTECTED.catagory == 0)
				{
					actor_HAIRMOD_PROTECTED.isDie = true;
				}
				mVector_HAIRMOD_PROTECTED2.addElement(actor_HAIRMOD_PROTECTED);
			}
		}
		int num5 = Tilemap_HAIRMOD_PROTECTED.pointPop.size();
		for (int l = 0; l < num5; l++)
		{
			((PopupName_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.pointPop.elementAt(l))?.update();
		}
		if (congThanh != null)
		{
			congThanh.update();
		}
		Util_HAIRMOD_PROTECTED.quickSort(actors);
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 10 == 0)
		{
			focusedActor = findFocusActor();
		}
		if (focusedActor != null && focusedActor.catagory == 100)
		{
			focusedActor = null;
		}
		EffectManager_HAIRMOD_PROTECTED.lowEffects.updateAll();
		EffectManager_HAIRMOD_PROTECTED.hiEffects.updateAll();
		updateFlyText();
		if (pingNextIn > 0)
		{
			pingNextIn--;
			if (pingNextIn == 0)
			{
				lastTimePing = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				gameService.ping();
			}
		}
		if (chatMode && tfChat != null)
		{
			tfChat.update();
		}
		if (indexBuff != -1 && timeBuff != -1 && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() > timeBuff)
		{
			indexBuff = -1;
			timeBuff = -1L;
		}
		if (xSlot > 0)
		{
			xSlot -= Res_HAIRMOD_PROTECTED.wKhung / 10;
			if (xSlot < 0)
			{
				xSlot = 0;
			}
		}
		if ((Tilemap_HAIRMOD_PROTECTED.w >= wMiniMap || Tilemap_HAIRMOD_PROTECTED.h >= hMiniMap) && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timeTranMini > 20)
		{
			updateminiMap();
		}
		try
		{
			if (posNpcRequest != null && iTaskAuto == 2)
			{
				updateTask();
				Canvas_HAIRMOD_PROTECTED.endDlg();
			}
		}
		catch (Exception)
		{
			mainChar.posTransRoad = null;
			mainChar.countRoad = 0;
		}
		if (effAnimate.size() > 0)
		{
			AnimateEffect_HAIRMOD_PROTECTED.updateWind();
			int num6 = effAnimate.size();
			for (int m = 0; m < num6; m++)
			{
				((AnimateEffect_HAIRMOD_PROTECTED)effAnimate.elementAt(m))?.update();
			}
		}
		if (effManager != null)
		{
			int num7 = effManager.size();
			for (int n = 0; n < num7; n++)
			{
				((EffectServerManager_HAIRMOD_PROTECTED)effManager.elementAt(n))?.update();
			}
		}
		for (int num8 = 0; num8 < mVector_HAIRMOD_PROTECTED2.size(); num8++)
		{
			actors.removeElement(mVector_HAIRMOD_PROTECTED2.elementAt(num8));
		}
		updateMsgServer();
		setRemoveIconImg();
		if (skillClan != null)
		{
			for (int num9 = 0; num9 < 1; num9++)
			{
				if (num9 < skillClan.Length && skillClan[num9] != null && skillClan[num9].time > 0)
				{
					skillClan[num9].updateTime();
				}
			}
		}
		if (timerung > 0)
		{
			timerung--;
		}
		else
		{
			timerung = 0;
		}
		if (Tilemap_HAIRMOD_PROTECTED.isOfflineMap && map == 29)
		{
			if (mainChar.x >= xBeginFrame - 16 && mainChar.x <= xTheendFrame + 40 && mainChar.y >= yBeginFrame - 16 && mainChar.y <= yTheendFrame + 40)
			{
				focusedActor = isFocusMyGround(mainChar);
			}
			else
			{
				int num10 = actors.size();
				for (int num11 = 0; num11 < num10; num11++)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(num11);
					if (actor_HAIRMOD_PROTECTED2 != null && actor_HAIRMOD_PROTECTED2.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
					{
						actor_HAIRMOD_PROTECTED2.isFocus = false;
					}
				}
			}
		}
		if (msgWorld.size() > 0)
		{
			((MsgInfo_HAIRMOD_PROTECTED)msgWorld.elementAt(0))?.update();
		}
		if (savemsgWorld.size() > 30)
		{
			savemsgWorld.removeElementAt(0);
		}
		for (int num12 = 0; num12 < num; num12++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)aeo.elementAt(num12);
			if (effect_HAIRMOD_PROTECTED != null && effect_HAIRMOD_PROTECTED.wantDetroy)
			{
				effect_HAIRMOD_PROTECTED = null;
				aeo.removeElementAt(num12);
			}
		}
		for (int num13 = arrowsUp.size() - 1; num13 >= 0; num13--)
		{
			IArrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = (IArrow_HAIRMOD_PROTECTED)arrowsUp.elementAt(num13);
			if (arrow_HAIRMOD_PROTECTED != null && arrow_HAIRMOD_PROTECTED.wantDestroy)
			{
				arrow_HAIRMOD_PROTECTED = null;
				arrowsUp.removeElementAt(num13);
			}
		}
		if (mainChar.state != 3 && autoFight && Canvas_HAIRMOD_PROTECTED.currentScreen is GameScr_HAIRMOD_PROTECTED && Canvas_HAIRMOD_PROTECTED.currentDialog != null && !mainChar.isDie && Tilemap_HAIRMOD_PROTECTED.lv != 0 && Tilemap_HAIRMOD_PROTECTED.lv != 201 && Tilemap_HAIRMOD_PROTECTED.lv != 70 && Tilemap_HAIRMOD_PROTECTED.lv != 80)
		{
			Canvas_HAIRMOD_PROTECTED.currentDialog = null;
		}
		updateMainCharWhenAuto();
		if (Tilemap_HAIRMOD_PROTECTED.lv == 0 || Tilemap_HAIRMOD_PROTECTED.lv == 70 || Tilemap_HAIRMOD_PROTECTED.lv == 80)
		{
			autoFight = false;
		}
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 200 == 0)
		{
			Char_HAIRMOD_PROTECTED.SetRemove();
		}
		if (isFixBugAutoQuest)
		{
			isFixBugAutoQuest = false;
			mainChar.xTo = mainChar.x;
			mainChar.yTo = mainChar.y;
			mainChar.xBegin = mainChar.x;
			mainChar.yBegin = mainChar.y;
			countChang++;
			if (countChang > xChange.Length - 1)
			{
				countChang = 0;
			}
			int xTo = Tilemap_HAIRMOD_PROTECTED.w / 2 + xChange[countChang];
			int yTo = Tilemap_HAIRMOD_PROTECTED.h / 2 + xChange[countChang];
			mainChar.posTransRoad = updateFindRoad(mainChar.x / 16, mainChar.y / 16, xTo, yTo);
			mainChar.countRoad = 0;
			iTaskAuto = iTaskAutoLast;
		}
	}

	public Actor_HAIRMOD_PROTECTED isFocusMyGround(LiveActor_HAIRMOD_PROTECTED ac)
	{
		Actor_HAIRMOD_PROTECTED result = null;
		if (Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			int num = actors.size();
			for (int i = 0; i < num; i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
				if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
				{
					if (ac.x >= actor_HAIRMOD_PROTECTED.x + 2 && ac.x <= actor_HAIRMOD_PROTECTED.x + 30 && ac.y >= actor_HAIRMOD_PROTECTED.y && ac.y + 2 <= actor_HAIRMOD_PROTECTED.y + 30)
					{
						actor_HAIRMOD_PROTECTED.isFocus = true;
						result = actor_HAIRMOD_PROTECTED;
					}
					else
					{
						actor_HAIRMOD_PROTECTED.isFocus = false;
					}
				}
			}
		}
		return result;
	}

	private void updateMsgServer()
	{
		if (listMSGServer != null && listMSGServer.size() > 0)
		{
			xMsgServer -= 5;
			string s = (string)listMSGServer.elementAt(0);
			if (xMsgServer + MFont_HAIRMOD_PROTECTED.arialFont.getWidth(s) < 20)
			{
				listMSGServer.removeElementAt(0);
				xMsgServer = Canvas_HAIRMOD_PROTECTED.w - 20;
				isPaintQuest = false;
			}
		}
	}

	private void setRemoveIconImg()
	{
		if (GameData_HAIRMOD_PROTECTED.listImgIcon.size() <= 100)
		{
			return;
		}
		IDictionaryEnumerator enumerator = GameData_HAIRMOD_PROTECTED.listImgIcon.GetEnumerator();
		while (enumerator.MoveNext())
		{
			string k = enumerator.Key.ToString();
			ImageIcon_HAIRMOD_PROTECTED imageIcon_HAIRMOD_PROTECTED = (ImageIcon_HAIRMOD_PROTECTED)GameData_HAIRMOD_PROTECTED.listImgIcon.get(k);
			if (!imageIcon_HAIRMOD_PROTECTED.isLoad && mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - imageIcon_HAIRMOD_PROTECTED.timeRemove > 120)
			{
				GameData_HAIRMOD_PROTECTED.listImgIcon.remove(k);
			}
		}
	}

	public void onLoadCloth(sbyte[] arr, sbyte type, sbyte id)
	{
		MyHashTable_HAIRMOD_PROTECTED myHashTable_HAIRMOD_PROTECTED = (MyHashTable_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.quanao.elementAt(type);
		((CharPartInfo_HAIRMOD_PROTECTED)myHashTable_HAIRMOD_PROTECTED.get(id + string.Empty))?.load(arr, type, id);
	}

	private void setRemoveClothes()
	{
		for (int i = 0; i < Res_HAIRMOD_PROTECTED.quanao.size(); i++)
		{
			MyHashTable_HAIRMOD_PROTECTED myHashTable_HAIRMOD_PROTECTED = (MyHashTable_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.quanao.elementAt(i);
			IDictionaryEnumerator enumerator = GameData_HAIRMOD_PROTECTED.listImgIcon.GetEnumerator();
			while (enumerator.MoveNext())
			{
				string k = (string)enumerator.Value;
				CharPartInfo_HAIRMOD_PROTECTED charPartInfo_HAIRMOD_PROTECTED = (CharPartInfo_HAIRMOD_PROTECTED)myHashTable_HAIRMOD_PROTECTED.get(k);
				if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - charPartInfo_HAIRMOD_PROTECTED.timeRemove > 60)
				{
					myHashTable_HAIRMOD_PROTECTED.remove(k);
				}
			}
		}
	}

	private void updateCmMini()
	{
		if (Tilemap_HAIRMOD_PROTECTED.w >= wMiniMap || Tilemap_HAIRMOD_PROTECTED.h >= hMiniMap)
		{
			if (cmyMini != cmtoYmini)
			{
				cmvyMini = cmtoYmini - cmyMini << 2;
				cmdyMini += cmvyMini;
				cmyMini += cmdyMini >> 4;
				cmdyMini &= 15;
			}
			if (cmxMini != cmtoXMini)
			{
				cmvxMini = cmtoXMini - cmxMini << 2;
				cmdxMini += cmvxMini;
				cmxMini += cmdxMini >> 4;
				cmdxMini &= 15;
			}
		}
	}

	public Actor_HAIRMOD_PROTECTED findFocusActor()
	{
		int num = mainChar.x + C[mainChar.dir][0];
		int num2 = mainChar.x + C[mainChar.dir][1];
		int num3 = mainChar.y + C[mainChar.dir][2];
		int num4 = mainChar.y + C[mainChar.dir][3];
		int num5 = 10000;
		int num6 = -1;
		nearActors.removeAllElements();
		int num7 = actors.size();
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = null;
		for (int i = 0; i < num7; i++)
		{
			actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED.x <= num || actor_HAIRMOD_PROTECTED.x >= num2 || actor_HAIRMOD_PROTECTED.y <= num3 || actor_HAIRMOD_PROTECTED.y >= num4)
			{
				continue;
			}
			if (actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_CAN_NOT_FOCUS || !actor_HAIRMOD_PROTECTED.canfocus || (actor_HAIRMOD_PROTECTED.catagory == 0 && actor_HAIRMOD_PROTECTED.ID == mainChar.ID) || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_TREE || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_PET || (autoFight && !Canvas_HAIRMOD_PROTECTED.autoTab.isAutoFire && Tilemap_HAIRMOD_PROTECTED.lv != 0 && Tilemap_HAIRMOD_PROTECTED.lv != 201 && Tilemap_HAIRMOD_PROTECTED.lv != 70 && Tilemap_HAIRMOD_PROTECTED.lv != 80 && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap && (actor_HAIRMOD_PROTECTED.isItemCanGet() || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_EXPLOTION || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_NPC || actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)))
			{
				if ((autoFight || autoBomHMP) && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_NPC)
				{
					addChat(mainChar, "Bạn hãy tắt auto trước khi focus", 100);
				}
			}
			else
			{
				if ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MONSTER && actor_HAIRMOD_PROTECTED.state == 8) || (autoFight && (actor_HAIRMOD_PROTECTED.state == 3 || actor_HAIRMOD_PROTECTED.isDropItem())))
				{
					continue;
				}
				if (typeOptionFocus == 1)
				{
					bool flag = false;
					if ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && (!actor_HAIRMOD_PROTECTED.iskiller || actor_HAIRMOD_PROTECTED.isNPC())) || (actor_HAIRMOD_PROTECTED.catagory != Actor_HAIRMOD_PROTECTED.CAT_MONSTER && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && !actor_HAIRMOD_PROTECTED.iskiller))
					{
						continue;
					}
				}
				else if (typeOptionFocus == 2)
				{
					if (!actor_HAIRMOD_PROTECTED.isNPC())
					{
						continue;
					}
				}
				else if (typeOptionFocus == 3)
				{
					if ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.idClan == mainChar.idClan) || actor_HAIRMOD_PROTECTED.isNPC())
					{
						continue;
					}
				}
				else if (typeOptionFocus == 4 && ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.nationID == mainChar.nationID) || actor_HAIRMOD_PROTECTED.isNPC()))
				{
					continue;
				}
				nearActors.addElement(actor_HAIRMOD_PROTECTED);
				int num8 = Util_HAIRMOD_PROTECTED.abs(mainChar.x - actor_HAIRMOD_PROTECTED.x) + Util_HAIRMOD_PROTECTED.abs(mainChar.y - actor_HAIRMOD_PROTECTED.y);
				if (actor_HAIRMOD_PROTECTED.catagory == 3 || actor_HAIRMOD_PROTECTED.catagory == 4)
				{
					num8 <<= 1;
				}
				if (num8 < num5)
				{
					num5 = num8;
					num6 = i;
				}
			}
		}
		if (num6 == -1)
		{
			return null;
		}
		if (autoFight && Canvas_HAIRMOD_PROTECTED.autoTab.isAutoFire)
		{
			for (int j = 0; j < nearActors.size(); j++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)nearActors.elementAt(j);
				if (actor_HAIRMOD_PROTECTED2 != null && actor_HAIRMOD_PROTECTED2.iskiller)
				{
					return actor_HAIRMOD_PROTECTED2;
				}
			}
		}
		if (focusedActor == null && num6 < actors.size())
		{
			return (Actor_HAIRMOD_PROTECTED)actors.elementAt(num6);
		}
		if (!Canvas_HAIRMOD_PROTECTED.instance.hasPointerEvents() && num6 < actors.size())
		{
			if (nearActors.conTains(focusedActor))
			{
				return focusedActor;
			}
			return (Actor_HAIRMOD_PROTECTED)actors.elementAt(num6);
		}
		if (Util_HAIRMOD_PROTECTED.distance(mainChar.x, mainChar.y, focusedActor.x, focusedActor.y) > Canvas_HAIRMOD_PROTECTED.w / 2)
		{
			focusedActor = null;
		}
		return focusedActor;
	}

	public void onStarDialLucky(int type)
	{
		DialLuckyScr_HAIRMOD_PROTECTED.gI().switchToMe();
		DialLuckyScr_HAIRMOD_PROTECTED.gI().lastScr = this;
	}

	public Actor_HAIRMOD_PROTECTED getActor(int id)
	{
		for (int i = 0; i < actors.size(); i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.ID == id)
			{
				return actor_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public void changeToNextFocusActor(bool isBack)
	{
		if (focusedActor == null)
		{
			focusedActor = findFocusActor();
			if (focusedActor != null && focusedActor.catagory == 100)
			{
				focusedActor = null;
			}
			return;
		}
		if (isBack && !focusedActor.iskiller && autoFight && typeOptionFocus == 1)
		{
			focusedActor = findFocusActor();
			if (focusedActor != null && focusedActor.iskiller)
			{
				return;
			}
		}
		int num = nearActors.indexOf(focusedActor);
		int num2 = num + 1;
		if (num2 >= nearActors.size() || num2 < 0)
		{
			num2 = 0;
		}
		if (nearActors.size() > 0)
		{
			focusedActor = (Actor_HAIRMOD_PROTECTED)nearActors.elementAt(num2);
		}
		if (focusedActor != null && focusedActor.catagory == 100)
		{
			focusedActor = null;
		}
	}

	public static void addChat(Actor_HAIRMOD_PROTECTED ac, string str, int time)
	{
		ac.chat = new ChatPopup_HAIRMOD_PROTECTED(time, str, 1);
		ac.chat.setPos(ac.x, ac.y - ac.height);
	}

	private bool doTalkWithNPCQuest()
	{
		if (mainChar.receiveQuest && mainChar.typeQuest == TRANSPORT && receiveQuest)
		{
			updateWhenTalkPhuOng();
			if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(5))
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
				indexContent++;
				if (indexContent <= mainChar.info_npc_quest.Length - 1)
				{
					mainChar.chat = null;
					focusedActor.chat = null;
					if (mainChar.info_npc_quest[indexContent].StartsWith("1"))
					{
						addChat(focusedActor, mainChar.info_npc_quest[indexContent].Substring(1), 500);
					}
					else
					{
						addChat(mainChar, mainChar.info_npc_quest[indexContent].Substring(1), 500);
					}
				}
				else
				{
					receiveQuest = false;
					mainChar.nextNpc++;
					if (mainChar.nextNpc >= mainChar.npc_quest.Length)
					{
						gameService.talk_npc(mainChar.npc_quest[mainChar.nextNpc - 1], 1, questID);
						dellPotionQuest();
					}
				}
			}
			return true;
		}
		return false;
	}

	private bool doTalkWithNPCResponse()
	{
		if (finishQuest && mainChar.finishQuest && mainChar.nextNpc < mainChar.npc_response.Length)
		{
			updateWhenTalkPhuOng();
			if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(5))
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
				indexContent++;
				if (indexContent <= mainChar.info_response[mainChar.nextNpc].Length - 1)
				{
					mainChar.chat = null;
					focusedActor.chat = null;
					if (mainChar.info_response[mainChar.nextNpc][indexContent].StartsWith("1"))
					{
						addChat(focusedActor, mainChar.info_response[mainChar.nextNpc][indexContent].Substring(1), 500);
					}
					else
					{
						addChat(mainChar, mainChar.info_response[mainChar.nextNpc][indexContent].Substring(1), 500);
					}
				}
				else
				{
					indexContent = 0;
					mainChar.nextNpc++;
					if (mainChar.nextNpc < mainChar.npc_response.Length)
					{
						mainChar.chat = null;
						focusedActor.chat = null;
						finishQuest = false;
						set_npc_request(mainChar.npc_response[mainChar.nextNpc]);
					}
					else
					{
						gameService.talk_npc_response_quest(mainChar.npc_response[mainChar.nextNpc - 1], 1, questID);
						finishQuest = false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public void set_npc_request(short type)
	{
		if (idNpcReceive != -1)
		{
			type = idNpcReceive;
		}
		else
		{
			current_npc_talk = type;
		}
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED is NPC_HAIRMOD_PROTECTED && ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type == type)
			{
				posNpcRequest = new Position_HAIRMOD_PROTECTED(actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y);
				break;
			}
		}
	}

	public void paintPosNPCQuest(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < PosNPCQuest.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)PosNPCQuest.elementAt(i);
			paintPoint(g, position_HAIRMOD_PROTECTED, position_HAIRMOD_PROTECTED.indexColor);
		}
	}

	private bool doReceiveQuest()
	{
		if (receiveQuest)
		{
			updateWhenTalkPhuOng();
			if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(5))
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
				indexContent++;
				if (indexContent <= content_quest.Length - 1)
				{
					mainChar.chat = null;
					focusedActor.chat = null;
					if (content_quest[indexContent].StartsWith("1"))
					{
						addChat(focusedActor, content_quest[indexContent].Substring(1), 500);
					}
					else
					{
						addChat(mainChar, content_quest[indexContent].Substring(1), 500);
					}
					if (indexContent == content_quest.Length - 1)
					{
						mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Đồng ý");
						ActionPerform_HAIRMOD_PROTECTED action = delegate
						{
							GameService_HAIRMOD_PROTECTED.gI().receiveQuest(idNpcReceive, questID, 1);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
							receiveQuest = false;
						};
						command_HAIRMOD_PROTECTED.action = action;
						mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
						if (mainChar.typeQuest != 3)
						{
							Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Không");
							ActionPerform_HAIRMOD_PROTECTED action2 = delegate
							{
								receiveQuest = false;
								GameService_HAIRMOD_PROTECTED.gI().receiveQuest(idNpcReceive, questID, 2);
							};
							command_HAIRMOD_PROTECTED2.action = action2;
							mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
						}
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
					}
				}
			}
			return true;
		}
		return false;
	}

	private bool doIntro()
	{
		if (Tilemap_HAIRMOD_PROTECTED.lv == 105)
		{
			string[] array = new string[15]
			{
				"1Cháu của ta", "1Nay con đã lớn", "1đã đến lúc con phải được học hỏi nhiều hơn", "1phải trải nghiệm nhiều hơn từ quê hương đất nước", "1và có nhiều điều để con phải học hỏi nơi biết bao đấng anh hào, ẩn sỹ.", "1Năm xưa ta có người anh em kết nghĩa là Hai Minh", "1ông ấy giờ đang là trưởng làng của làng này.", "1con hãy đi xuống phương Nam và tìm gặp người này", "1ông ấy sẽ giúp con gia nhập làng nghĩa sỹ Sơn Nam", "1Con hãy cố gắng để trưởng thành, để có thể giúp được quê hương",
				"1để có được sự nghiệp riêng và để tìm được nguồn gốc và cha mẹ của con…", "1Hãy lên đường đi", "1..nơi đây ta luôn dõi theo bước chân của con", "0Vâng! thưa nội con đi", "1Uhm, chúc con lên đường bình an."
			};
			if ((focusedActor == null || !(focusedActor is NPC_HAIRMOD_PROTECTED)) && Canvas_HAIRMOD_PROTECTED.currentDialog == null)
			{
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = true;
				indexContent = -1;
				doActorMove();
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = false;
				if (focusedActor == null)
				{
					return false;
				}
				if (Util_HAIRMOD_PROTECTED.abs(mainChar.y - focusedActor.y) <= 32)
				{
					if (indexContent == -1)
					{
						Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
					}
					if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
					{
						Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
						int num = cmx + Canvas_HAIRMOD_PROTECTED.px;
						int num2 = cmy + Canvas_HAIRMOD_PROTECTED.py;
						if (Util_HAIRMOD_PROTECTED.abs(focusedActor.x - num) < 20 && Util_HAIRMOD_PROTECTED.abs(focusedActor.y - 20 - num2) < 40)
						{
							Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
						}
					}
					if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(5) && indexContent < array.Length)
					{
						indexContent++;
						if (indexContent >= array.Length)
						{
							mainChar.state = 0;
							MyRandom_HAIRMOD_PROTECTED myRandom_HAIRMOD_PROTECTED = new MyRandom_HAIRMOD_PROTECTED();
							int toX = 13 + myRandom_HAIRMOD_PROTECTED.nextInt() % 5;
							int toY = 11 + myRandom_HAIRMOD_PROTECTED.nextInt() % 5;
							GameService_HAIRMOD_PROTECTED.gI().changeMap(0, toX, toY);
							Canvas_HAIRMOD_PROTECTED.keyHold[2] = false;
							Canvas_HAIRMOD_PROTECTED.startWaitDlg("Chuyển màn..", true);
						}
						else
						{
							mainChar.chat = null;
							focusedActor.chat = null;
							if (array[indexContent].StartsWith("1"))
							{
								addChat(focusedActor, array[indexContent].Substring(1), 500);
							}
							else
							{
								addChat(mainChar, array[indexContent].Substring(1), 700);
							}
						}
					}
					return true;
				}
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = true;
				indexContent = -1;
				doActorMove();
			}
		}
		return false;
	}

	public void setFinishQuest()
	{
		if (posNpcRequest != null && iTaskAuto == 2)
		{
			posNpcRequest = null;
			GameService_HAIRMOD_PROTECTED.gI().receiveQuest(21, 8, 1);
			Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			receiveQuest = false;
		}
	}

	private void doActorMove()
	{
		bool flag = false;
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(2) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.W))
		{
			flag = true;
			dir = 1;
		}
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(4) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.A))
		{
			flag = true;
			dir = 2;
		}
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(6) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.D))
		{
			flag = true;
			dir = 3;
		}
		if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(8) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.S))
		{
			flag = true;
			dir = 4;
		}
		bool flag2 = false;
		bool flag3 = false;
		int x = mainChar.x;
		int y = mainChar.y;
		int num = mainChar.dir;
		int num2 = 0;
		int num3 = 0;
		int speed = mainChar.speed;
		if (Canvas_HAIRMOD_PROTECTED.keyHold[2] || Canvas_HAIRMOD_PROTECTED.keyHold[KeyConst_HAIRMOD_PROTECTED.W])
		{
			mainChar.stop = false;
			autoFight = false;
			removeFocusWhenPutKey();
			autoFight = false;
			mainChar.posTransRoad = null;
			setFinishQuest();
			num3 = -16;
			if (congThanh == null || mainChar.y > congThanh.y + 40)
			{
				flag3 = (flag2 = true);
			}
			else
			{
				flag = false;
			}
			mainChar.dir = Char_HAIRMOD_PROTECTED.UP;
			moveToX = mainChar.x;
			moveToY = (short)(mainChar.y - speed);
			try
			{
				if (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(moveToX, moveToY, 2) || checkMoveLimit(moveToX, moveToY))
				{
					moveToY = mainChar.y;
					if (mainChar.setWay(0, -8))
					{
						changeSinceLastUpdate = true;
						mainChar.comeHome = false;
						return;
					}
					flag = false;
					flag2 = false;
				}
			}
			catch (Exception ex)
			{
				flag = false;
				moveToY = mainChar.y;
				Out_HAIRMOD_PROTECTED.println(ex.StackTrace + "move actor key");
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyHold[8] || Canvas_HAIRMOD_PROTECTED.keyHold[KeyConst_HAIRMOD_PROTECTED.S])
		{
			mainChar.stop = false;
			autoFight = false;
			removeFocusWhenPutKey();
			autoFight = false;
			mainChar.posTransRoad = null;
			setFinishQuest();
			num3 = 16;
			flag3 = (flag2 = true);
			mainChar.dir = Char_HAIRMOD_PROTECTED.DOWN;
			moveToX = mainChar.x;
			moveToY = (short)(mainChar.y + speed);
			try
			{
				if (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(moveToX, moveToY, 2) || checkMoveLimit(moveToX, moveToY))
				{
					moveToY = mainChar.y;
					if (mainChar.setWay(0, 8))
					{
						changeSinceLastUpdate = true;
						mainChar.comeHome = false;
						return;
					}
					flag = false;
					flag2 = false;
				}
			}
			catch (Exception ex2)
			{
				flag = false;
				moveToY = mainChar.y;
				Out_HAIRMOD_PROTECTED.println(ex2.StackTrace + "move actor key");
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyHold[4] || Canvas_HAIRMOD_PROTECTED.keyHold[KeyConst_HAIRMOD_PROTECTED.A])
		{
			mainChar.stop = false;
			autoFight = false;
			removeFocusWhenPutKey();
			autoFight = false;
			mainChar.posTransRoad = null;
			setFinishQuest();
			num2 = -16;
			flag3 = (flag2 = true);
			mainChar.dir = Char_HAIRMOD_PROTECTED.LEFT;
			moveToX = (short)(mainChar.x - speed);
			moveToY = mainChar.y;
			try
			{
				if (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(moveToX, moveToY, 2) || checkMoveLimit(moveToX, moveToY))
				{
					moveToX = mainChar.x;
					if (mainChar.setWay(-8, 0))
					{
						changeSinceLastUpdate = true;
						mainChar.comeHome = false;
						return;
					}
					flag = false;
					flag2 = false;
				}
			}
			catch (Exception ex3)
			{
				flag = false;
				moveToX = mainChar.x;
				Out_HAIRMOD_PROTECTED.println(ex3.StackTrace + "move actor key");
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.keyHold[6] || Canvas_HAIRMOD_PROTECTED.keyHold[KeyConst_HAIRMOD_PROTECTED.D])
		{
			mainChar.stop = false;
			autoFight = false;
			removeFocusWhenPutKey();
			autoFight = false;
			mainChar.posTransRoad = null;
			setFinishQuest();
			num2 = 16;
			flag3 = (flag2 = true);
			mainChar.dir = Char_HAIRMOD_PROTECTED.RIGHT;
			moveToY = mainChar.y;
			moveToX = (short)(mainChar.x + speed);
			try
			{
				if (Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(moveToX, moveToY, 2) || checkMoveLimit(moveToX, moveToY))
				{
					moveToX = mainChar.x;
					if (mainChar.setWay(8, 0))
					{
						changeSinceLastUpdate = true;
						mainChar.comeHome = false;
						return;
					}
					flag = false;
					flag2 = false;
				}
			}
			catch (Exception ex4)
			{
				flag = false;
				moveToX = mainChar.x;
				Out_HAIRMOD_PROTECTED.println(ex4.StackTrace + "move actor key");
			}
		}
		if (!mainChar.FuntioncanMove() || (flag3 && checkCanChangeMap(x + num2, y + num3, num)))
		{
			return;
		}
		if (flag2)
		{
			if (moveToX < 0)
			{
				moveToX = mainChar.x;
			}
			if (moveToY < 0)
			{
				moveToY = mainChar.y;
			}
			if (moveToX > Tilemap_HAIRMOD_PROTECTED.w * 16)
			{
				moveToX = mainChar.x;
			}
			if (moveToY > Tilemap_HAIRMOD_PROTECTED.h * 16)
			{
				moveToX = mainChar.y;
			}
			mainChar.moveTo(moveToX, moveToY);
			if (saveAutoFight && typeOptionFocus == 1)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.lastXAuto = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.xTo;
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.lastYAuto = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.yTo;
			}
			changeSinceLastUpdate = true;
			mainChar.comeHome = false;
			countStep++;
		}
		if (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeMove >= 0 && !mainChar.stop)
		{
			timeMove = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + dtmove;
			gameService.moveChar(moveToX, moveToY);
		}
	}

	public void updateWhenTalkPhuOng()
	{
		if (iTaskAuto == 2 || focusedActor == null || !Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			return;
		}
		int num = cmx + Canvas_HAIRMOD_PROTECTED.px;
		int num2 = cmy + Canvas_HAIRMOD_PROTECTED.py;
		if (focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
		{
			return;
		}
		for (int i = 0; i < actors.size(); i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (Util_HAIRMOD_PROTECTED.abs(actor_HAIRMOD_PROTECTED.x - num) >= 20 || Util_HAIRMOD_PROTECTED.abs(actor_HAIRMOD_PROTECTED.y - 20 - num2) >= 40)
			{
				continue;
			}
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			if (focusedActor.ID == actor_HAIRMOD_PROTECTED.ID)
			{
				if (actor_HAIRMOD_PROTECTED is Char_HAIRMOD_PROTECTED && (mainChar.pk <= 0 || map / 100 == 3))
				{
					focusedActor = null;
					return;
				}
				if (focusedActor.catagory != 2 || actor_HAIRMOD_PROTECTED.catagory != 2)
				{
					return;
				}
				if (((NPC_HAIRMOD_PROTECTED)focusedActor).type == ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type)
				{
					if (((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type != 4)
					{
						Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
						return;
					}
					if (((NPC_HAIRMOD_PROTECTED)focusedActor).idLinhGac == ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).idLinhGac)
					{
						focusedActor = null;
						return;
					}
					if (actor_HAIRMOD_PROTECTED.ID != mainChar.ID)
					{
						focusedActor = null;
						if (map == 29)
						{
							findRoad(num, num2);
						}
						return;
					}
				}
			}
			if (actor_HAIRMOD_PROTECTED.ID != mainChar.ID)
			{
				focusedActor = null;
				if (map == 29)
				{
					findRoad(num, num2);
				}
			}
			return;
		}
		focusedActor = null;
	}

	public void removeFocusWhenPutKey()
	{
		if (map == 17 && mainChar.isDoing)
		{
			focusedActor = null;
			coutChangeFocusWhenDoing = 0;
		}
	}

	public override void updateKey()
	{
		if (chatMode && Canvas_HAIRMOD_PROTECTED.currentDialog == null && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			switch (Canvas_HAIRMOD_PROTECTED.collisionCmdBar())
			{
			case 0:
				Canvas_HAIRMOD_PROTECTED.clearKeyHold();
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
				chatMode = false;
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				return;
			case 1:
			{
				string text = tfChat.getText();
				Out_HAIRMOD_PROTECTED.println("THONG TIN CHAT  " + text);
				tfChat.setText(string.Empty);
				doChatFromMe(text);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				return;
			}
			case 2:
				tfChat.clear();
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				return;
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.keyPressed[11])
		{
			cheat = !cheat;
		}
		if (doIntro() || chatMode)
		{
			return;
		}
		if (currQuest != null && currQuest.isShow)
		{
			currQuest.update(mainChar, focusedActor);
		}
		else
		{
			if (doTalkWithNPCQuest() || doTalkWithNPCResponse() || doReceiveQuest())
			{
				return;
			}
			int num = 0;
			if (idChiemThanh != null)
			{
				for (int i = 0; i < idChiemThanh.Length; i++)
				{
					if (idChiemThanh[i] == mainChar.ID && timeChiemThanh[i] - (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - curTimeCT[i]) >= 0)
					{
						num = 1;
					}
				}
			}
			if (num == 0)
			{
				updateTouchMHP();
				updatePoint();
			}
			base.updateKey();
			if (mainChar.justStopFromRun)
			{
				mainChar.justStopFromRun = false;
				changeSinceLastUpdate = true;
				changeSinceLastUpdate = false;
				mainChar.stop = false;
				sendActorMove(mainChar.x, mainChar.y);
			}
			if (Canvas_HAIRMOD_PROTECTED.isKeyPressed((!Main_HAIRMOD_PROTECTED.isPC) ? 10 : 21))
			{
				if (Main_HAIRMOD_PROTECTED.isPC)
				{
					if (Canvas_HAIRMOD_PROTECTED.currentDialog == null)
					{
						doParty();
						Canvas_HAIRMOD_PROTECTED.clearKeyHold();
					}
				}
				else
				{
					doParty();
					Canvas_HAIRMOD_PROTECTED.clearKeyHold();
				}
				return;
			}
			if (Main_HAIRMOD_PROTECTED.isPC && Canvas_HAIRMOD_PROTECTED.currentDialog == null)
			{
				if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.Y))
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.chatMode = true;
					Canvas_HAIRMOD_PROTECTED.endDlg();
					ActionPerform_HAIRMOD_PROTECTED ok = delegate
					{
						doChatFromMe(Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
						Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.setText(string.Empty);
					};
					Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nội dung Chat", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 100, false);
					Canvas_HAIRMOD_PROTECTED.inputDlg.show();
					return;
				}
				if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.U))
				{
					GameService_HAIRMOD_PROTECTED.gI().setConfig(6);
					Canvas_HAIRMOD_PROTECTED.endDlg();
					return;
				}
				if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(KeyConst_HAIRMOD_PROTECTED.I))
				{
					if (indexTabSlot == 0)
					{
						indexTabSlot = 1;
					}
					else
					{
						indexTabSlot = 0;
					}
					xSlot = Res_HAIRMOD_PROTECTED.wKhung;
					return;
				}
			}
			if (autoFight)
			{
				if (indexBuff != -1 && timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 > 0)
				{
					isBeginAutoBuff = true;
				}
				else
				{
					isBeginAutoBuff = false;
				}
			}
			if (mainChar.state != 3 && (autoFight || autoBomHMP) && !mainChar.isDie && Tilemap_HAIRMOD_PROTECTED.lv != 0 && Tilemap_HAIRMOD_PROTECTED.lv != 201 && Tilemap_HAIRMOD_PROTECTED.lv != 70 && Tilemap_HAIRMOD_PROTECTED.lv != 80 && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
			{
				if (autoBomHMP && (Canvas_HAIRMOD_PROTECTED.autoTab.isAutoMHP || Canvas_HAIRMOD_PROTECTED.autoTab.isAll) && timeDelayHpMp - mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000 <= 0)
				{
					int num2 = mainChar.hp * 100 / mainChar.maxhp;
					int num3 = mainChar.mp * 100 / mainChar.maxmp;
					if (num2 < BangAuto_HAIRMOD_PROTECTED.bomHp && mainChar.hp < mainChar.maxhp)
					{
						isAutoBomHp = true;
					}
					if (num3 < BangAuto_HAIRMOD_PROTECTED.bomMp && mainChar.mp < mainChar.maxmp)
					{
						isAutoBomMp = true;
					}
					if (num2 >= BangAuto_HAIRMOD_PROTECTED.bomHp)
					{
						isAutoBomHp = false;
					}
					if (num3 >= BangAuto_HAIRMOD_PROTECTED.bomMp)
					{
						isAutoBomMp = false;
					}
					if (isAutoBomHp && !mainChar.beStune)
					{
						doFire(7, indexTabSlot);
					}
					if (isAutoBomMp && !mainChar.beStune)
					{
						doFire(9, indexTabSlot);
					}
					timeDelayHpMp = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 1000 + 1;
				}
				if (autoFight && (Canvas_HAIRMOD_PROTECTED.autoTab.isAutoFire || Canvas_HAIRMOD_PROTECTED.autoTab.isAll) && BangAuto_HAIRMOD_PROTECTED.tgDelay - mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 100 <= 0)
				{
					if (mainChar.isDoing)
					{
						if (focusedActor != null && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_NPC && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_PLAYER && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_EXPLOTION)
						{
							Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
						}
						else
						{
							changeToNextFocusActor(false);
						}
					}
					else
					{
						if (focusedActor != null && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_NPC && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_EXPLOTION)
						{
							bool flag = false;
							if (focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && !focusedActor.iskiller)
							{
								changeToNextFocusActor(false);
								flag = true;
							}
							if (!flag)
							{
								Canvas_HAIRMOD_PROTECTED.keyPressed[dem] = true;
								dem += 2;
								if (dem > 5)
								{
									dem = 1;
								}
							}
						}
						else
						{
							changeToNextFocusActor(false);
						}
						if (!isBeginAutoBuff)
						{
							if (mainChar.clazz != 2)
							{
								doFire(1, 1);
								doFire(3, 1);
								doFire(5, 1);
							}
							else if (!isBuffHoisinh())
							{
								doFire(1, 1);
								doFire(3, 1);
								doFire(5, 1);
							}
						}
					}
					BangAuto_HAIRMOD_PROTECTED.tgDelay = (int)(mSystem_HAIRMOD_PROTECTED.currentTimeMillis() / 100 + 10);
				}
				if (Canvas_HAIRMOD_PROTECTED.keyPressed[7])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[7] = false;
					doFire(7, indexTabSlot);
				}
				if (Canvas_HAIRMOD_PROTECTED.keyPressed[9])
				{
					Canvas_HAIRMOD_PROTECTED.keyPressed[9] = false;
					doFire(9, indexTabSlot);
				}
			}
			for (int j = 0; j < QUICKSLOT_KEY.Length; j++)
			{
				if (Canvas_HAIRMOD_PROTECTED.isKeyPressed(QUICKSLOT_KEY[j]))
				{
					if (!mainChar.beStune)
					{
						doFire(QUICKSLOT_KEY[j], indexTabSlot);
					}
					break;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.menu.showMenu)
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
			}
			else if ((Canvas_HAIRMOD_PROTECTED.isKeyPressed(2) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(4) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(6) || Canvas_HAIRMOD_PROTECTED.isKeyPressed(8)) && mainChar.state == 3)
			{
				mainChar.stop = false;
			}
			else if ((mainChar.state == 1 || mainChar.state == 0 || mainChar.state == 4) && num == 0)
			{
				doActorMove();
			}
		}
	}

	public bool isBuffHoisinh()
	{
		sbyte[] array = new sbyte[3] { 1, 3, 5 };
		for (int i = 0; i < array.Length; i++)
		{
			QuickSlot_HAIRMOD_PROTECTED quickSlot_HAIRMOD_PROTECTED = MainChar_HAIRMOD_PROTECTED.quickSlot[1][SLOTINDEXOFKEY[array[i]]];
			if (mainChar.clazz == 2 && quickSlot_HAIRMOD_PROTECTED.getSkillType() == 6)
			{
				return true;
			}
		}
		return false;
	}

	private void updateTask()
	{
		if (mainChar.posTransRoad != null)
		{
			return;
		}
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[0] == 0)
		{
			if (posNpcRequest != null && posNpcRequest.x / 16 == 6 && posNpcRequest.y / 16 == 7)
			{
				posNpcRequest.x = 160;
			}
			if (Util_HAIRMOD_PROTECTED.distance(mainChar.x, mainChar.y, posNpcRequest.x, posNpcRequest.y) > 32)
			{
				mainChar.posTransRoad = updateFindRoad(mainChar.x / 16, mainChar.y / 16, posNpcRequest.x / 16, posNpcRequest.y / 16 + 2);
				mainChar.countRoad = 0;
				return;
			}
			mainChar.dir = 1;
			if (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100 - timeTaskAuto <= timeDelayTask)
			{
				return;
			}
			timeDelayTask = 3;
			int num = actors.size();
			for (int i = 0; i < num; i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
				if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED is NPC_HAIRMOD_PROTECTED && ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type == idNpcReceive)
				{
					focusedActor = actor_HAIRMOD_PROTECTED;
					break;
				}
			}
			timeTaskAuto = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 100;
			if (Canvas_HAIRMOD_PROTECTED.menu.showMenu)
			{
				Canvas_HAIRMOD_PROTECTED.menu.selected = 0;
				Canvas_HAIRMOD_PROTECTED.menu.center.perform();
				Canvas_HAIRMOD_PROTECTED.menu.showMenu = false;
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
			}
		}
		else
		{
			iTaskAuto = 0;
		}
	}

	public bool checkCanChangeMap(int x, int y, int dir)
	{
		CMLocation_HAIRMOD_PROTECTED cMLocation_HAIRMOD_PROTECTED = Tilemap_HAIRMOD_PROTECTED.toOtherMapAt(x + X_FOWARD[dir], y + Y_FOWARD[dir]);
		if (cMLocation_HAIRMOD_PROTECTED != null)
		{
			ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, cMLocation_HAIRMOD_PROTECTED.toX, cMLocation_HAIRMOD_PROTECTED.toY);
			mainChar.state = 0;
			if (!Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
			{
				if (!isOffLineMap(cMLocation_HAIRMOD_PROTECTED.toM))
				{
					int indexArrMap = getIndexArrMap();
					string[] name = Res_HAIRMOD_PROTECTED.getNameMap(indexArrMap, cMLocation_HAIRMOD_PROTECTED.toM);
					short[] local = Res_HAIRMOD_PROTECTED.getLocation(indexArrMap, cMLocation_HAIRMOD_PROTECTED.toM);
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
					int num = name.Length;
					for (int j = 0; j < num; j++)
					{
						int i = j;
						if (name[j].Equals(string.Empty))
						{
							continue;
						}
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(name[j]);
						ActionPerform_HAIRMOD_PROTECTED action = delegate
						{
							int toX = local[i * 3 + 1];
							int toY = local[i * 3 + 2];
							GameService_HAIRMOD_PROTECTED.gI().changeMap(local[i * 3], toX, toY);
							ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
							int num2 = objDynamic.size();
							for (int k = 0; k < num2; k++)
							{
								DynamicObj_HAIRMOD_PROTECTED dynamicObj_HAIRMOD_PROTECTED = (DynamicObj_HAIRMOD_PROTECTED)objDynamic.elementAt(k);
								if (dynamicObj_HAIRMOD_PROTECTED != null)
								{
									dynamicObj_HAIRMOD_PROTECTED = null;
									objDynamic.removeElementAt(k);
								}
							}
							Res_HAIRMOD_PROTECTED.removeMonsterTemplet();
							nameMap = name[i];
						};
						command_HAIRMOD_PROTECTED.action = action;
						mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
					}
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
					Canvas_HAIRMOD_PROTECTED.menu.setIndex();
				}
				else
				{
					oldMap = map;
					GameService_HAIRMOD_PROTECTED.gI().changeMap(cMLocation_HAIRMOD_PROTECTED.toM, cMLocation_HAIRMOD_PROTECTED.toX, cMLocation_HAIRMOD_PROTECTED.toY);
					ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
					Res_HAIRMOD_PROTECTED.removeMonsterTemplet();
				}
			}
			else
			{
				GameService_HAIRMOD_PROTECTED.gI().changeMap(oldMap, cMLocation_HAIRMOD_PROTECTED.toX, cMLocation_HAIRMOD_PROTECTED.toY);
				ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
				Res_HAIRMOD_PROTECTED.removeMonsterTemplet();
			}
			return true;
		}
		return false;
	}

	private void doFire(int key, int index)
	{
		if (mainChar.state == 3)
		{
			Canvas_HAIRMOD_PROTECTED.msgdlg.isIcon = false;
			indexBuff = -1;
			timeBuff = 0L;
			mainChar.resetBuff();
			if (saveAutoFight)
			{
				autoFight = saveAutoFight;
			}
			if (BangAuto_HAIRMOD_PROTECTED.isAutoComeHome && autoFight)
			{
				GameService_HAIRMOD_PROTECTED.gI().comeHomeWhenDie();
				ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
				ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, 0, 0);
				autoFight = (saveAutoFight = false);
				return;
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Về làng");
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Hồi sinh");
			command_HAIRMOD_PROTECTED.action = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().comeHomeWhenDie();
				ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
				ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, 0, 0);
				autoFight = (saveAutoFight = false);
			};
			command_HAIRMOD_PROTECTED2.action = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().doCustomPopup(mainChar.ID, -1, string.Empty, 1);
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Đóng");
			command_HAIRMOD_PROTECTED3.action = delegate
			{
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo("Quay về làng.", command_HAIRMOD_PROTECTED, command_HAIRMOD_PROTECTED2, command_HAIRMOD_PROTECTED3);
			Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
		}
		else
		{
			if (Main_HAIRMOD_PROTECTED.isPC && Canvas_HAIRMOD_PROTECTED.currentDialog != null)
			{
				return;
			}
			mainChar.stand = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			if (mainChar.state == 3)
			{
				Canvas_HAIRMOD_PROTECTED.msgdlg.isIcon = false;
				indexBuff = -1;
				timeBuff = 0L;
				mainChar.resetBuff();
				if (BangAuto_HAIRMOD_PROTECTED.isAutoComeHome && autoFight)
				{
					GameService_HAIRMOD_PROTECTED.gI().comeHomeWhenDie();
					ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
					ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, 0, 0);
					autoFight = (saveAutoFight = false);
					return;
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Về làng");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().comeHomeWhenDie();
					ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
					ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, 0, 0);
					autoFight = (saveAutoFight = false);
				};
				command_HAIRMOD_PROTECTED4.action = action;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Chờ");
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				command_HAIRMOD_PROTECTED5.action = action2;
				Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo("Quay về làng.", null, command_HAIRMOD_PROTECTED4, command_HAIRMOD_PROTECTED5);
				Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
				return;
			}
			if (focusedActor != null && focusedActor.isNpcChar())
			{
				mVector_HAIRMOD_PROTECTED allQuestNpc = getAllQuestNpc(focusedActor.getTypeNpc());
				if (allQuestNpc.size() > 0 && focusedActor.isNpcChar())
				{
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = allQuestNpc;
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Nói chuyện");
					command_HAIRMOD_PROTECTED6.action = delegate
					{
						GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo(((Char_HAIRMOD_PROTECTED)focusedActor).idBoss);
					};
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED6);
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
				}
				else
				{
					GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo(((Char_HAIRMOD_PROTECTED)focusedActor).idBoss);
				}
				return;
			}
			QuickSlot_HAIRMOD_PROTECTED quickSlot_HAIRMOD_PROTECTED = MainChar_HAIRMOD_PROTECTED.quickSlot[index][SLOTINDEXOFKEY[key]];
			if (quickSlot_HAIRMOD_PROTECTED.quickslotType != QuickSlot_HAIRMOD_PROTECTED.TYPE_POTION)
			{
				if (quickSlot_HAIRMOD_PROTECTED.isBuff && !(focusedActor is NPC_HAIRMOD_PROTECTED))
				{
					sbyte b = Char_HAIRMOD_PROTECTED.skillLevelLearnt[quickSlot_HAIRMOD_PROTECTED.getSkillType()];
					if (b <= 0)
					{
						return;
					}
					int num = SkillManager_HAIRMOD_PROTECTED.EFF_BUFF_SKILL[Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz][quickSlot_HAIRMOD_PROTECTED.getSkillType() - 4];
					int skillMP = SkillManager_HAIRMOD_PROTECTED.getSkillMP(quickSlot_HAIRMOD_PROTECTED.getSkillType(), Char_HAIRMOD_PROTECTED.skillLevelLearnt[quickSlot_HAIRMOD_PROTECTED.getSkillType() - 4], Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.clazz);
					if (skillMP > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.mp)
					{
						if (!first)
						{
							addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Không đủ MP", 0));
							first = true;
						}
						return;
					}
					first = false;
					if (focusedActor != null && focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
					{
						if (quickSlot_HAIRMOD_PROTECTED.getSkillType() == 6)
						{
							GameService_HAIRMOD_PROTECTED.gI().useBuff(focusedActor.ID, 0, (sbyte)num, 0);
							mainChar.mp -= skillMP;
						}
						else
						{
							mainChar.mp -= skillMP;
							indexBuff = quickSlot_HAIRMOD_PROTECTED.getSkillType();
							WindowInfoScr_HAIRMOD_PROTECTED.gI().doCastBuffToActor(mainChar, focusedActor, num, SkillManager_HAIRMOD_PROTECTED.SKILL_CAN_BUFF_TO_USER[mainChar.clazz][quickSlot_HAIRMOD_PROTECTED.getSkillType() - 4] == 1);
						}
					}
					else if (quickSlot_HAIRMOD_PROTECTED.getSkillType() != 6)
					{
						mainChar.mp -= skillMP;
						if (quickSlot_HAIRMOD_PROTECTED.isBuff && (Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == -1 || Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff == quickSlot_HAIRMOD_PROTECTED.getSkillType()))
						{
							Canvas_HAIRMOD_PROTECTED.gameScr.indexBuff = quickSlot_HAIRMOD_PROTECTED.getSkillType();
						}
						long num2 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - mainChar.timeLastUseSkills[quickSlot_HAIRMOD_PROTECTED.getSkillType()];
						if (autoFight && num2 <= Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[quickSlot_HAIRMOD_PROTECTED.getSkillType()] && timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 < 0 && isBuffAuto)
						{
							num2 = Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[quickSlot_HAIRMOD_PROTECTED.getSkillType()] + 1;
							isBuffAuto = false;
							Out_HAIRMOD_PROTECTED.println("Kiem tra auto buff ================================");
						}
						if (num2 > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[quickSlot_HAIRMOD_PROTECTED.getSkillType()])
						{
							GameService_HAIRMOD_PROTECTED.gI().useBuff(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID, 0, (sbyte)num, 0);
							mainChar.coolDownSkill[quickSlot_HAIRMOD_PROTECTED.getSkillType()] = SkillManager_HAIRMOD_PROTECTED.getSkillCooldown(quickSlot_HAIRMOD_PROTECTED.getSkillType(), mainChar.clazz, Char_HAIRMOD_PROTECTED.skillLevelLearnt[quickSlot_HAIRMOD_PROTECTED.getSkillType()]);
							mainChar.timeLastUseSkills[quickSlot_HAIRMOD_PROTECTED.getSkillType()] = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
						}
					}
					else
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Chỉ có thể hồi sinh cho người đã hết HP.");
					}
					return;
				}
				if (focusedActor != null && focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_NPC && Char_HAIRMOD_PROTECTED.skillLevelLearnt[quickSlot_HAIRMOD_PROTECTED.getSkillType()] >= 0)
				{
					doUseSkillToFocusActor(quickSlot_HAIRMOD_PROTECTED.getSkillType());
				}
				if (focusedActor != null)
				{
					long num3 = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - mainChar.timeLastUseSkills[quickSlot_HAIRMOD_PROTECTED.getSkillType()];
					if (focusedActor.catagory != 2 && num3 > Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.coolDownSkill[quickSlot_HAIRMOD_PROTECTED.getSkillType()] && (Char_HAIRMOD_PROTECTED.skillLevelLearnt[quickSlot_HAIRMOD_PROTECTED.getSkillType()] > 0 || focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND))
					{
						doUseSkillToFocusActor(quickSlot_HAIRMOD_PROTECTED.getSkillType());
					}
				}
			}
			else if (quickSlot_HAIRMOD_PROTECTED.quickslotType == QuickSlot_HAIRMOD_PROTECTED.TYPE_POTION)
			{
				doUsePotion(quickSlot_HAIRMOD_PROTECTED.getPotionType());
			}
		}
	}

	public void doUsePotion(int potionType)
	{
		if (potionType >= Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions.Length)
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[potionType] <= 0)
		{
			int num = MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot].Length;
			int[] array = new int[7] { 94, 93, 22, 21, 3, 2, 1 };
			int[] array2 = new int[7] { 96, 95, 24, 23, 6, 5, 4 };
			for (int i = 0; i < num; i++)
			{
				if (MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].quickslotType != QuickSlot_HAIRMOD_PROTECTED.TYPE_POTION || MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].getPotionType() != potionType)
				{
					continue;
				}
				MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].setIsNothing();
				if (potionType == 1 || potionType == 2 || potionType == 3 || potionType == 21 || potionType == 22 || potionType == 93 || potionType == 94)
				{
					bool flag = false;
					for (int j = 1; j <= 3; j++)
					{
						if (j != potionType && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[j] > 0)
						{
							MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].setIsPotion(j);
							flag = true;
							break;
						}
					}
					if (!flag)
					{
						for (int k = 0; k < array.Length; k++)
						{
							if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[array[k]] > 0)
							{
								MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].setIsPotion(array[k]);
								break;
							}
						}
					}
				}
				if (potionType != 4 && potionType != 5 && potionType != 6 && potionType != 23 && potionType != 24 && potionType != 95 && potionType != 96)
				{
					continue;
				}
				bool flag2 = false;
				for (int l = 4; l <= 6; l++)
				{
					if (l != potionType && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[l] > 0)
					{
						MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].setIsPotion(l);
						break;
					}
				}
				if (flag2)
				{
					continue;
				}
				for (int m = 0; m < array2.Length; m++)
				{
					if (Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.potions[array2[m]] > 0)
					{
						MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].setIsPotion(array2[m]);
						break;
					}
				}
			}
			RMS_HAIRMOD_PROTECTED.saveQuickSlot();
			return;
		}
		long currentTimeMillis = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		if (currentTimeMillis - mainChar.potionLastTimeUse[potionType] < MainChar_HAIRMOD_PROTECTED.listPotion[potionType].delay || (mainChar.hp >= mainChar.maxhp && (potionType == 1 || potionType == 2 || potionType == 3 || potionType == 21 || potionType == 22 || potionType == 93 || potionType == 94)) || (mainChar.mp >= mainChar.maxmp && (potionType == 4 || potionType == 5 || potionType == 6 || potionType == 23 || potionType == 24 || potionType == 95 || potionType == 96)))
		{
			return;
		}
		if (potionType == 19)
		{
			if (map == 0)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể sử dụng trong làng.");
				return;
			}
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.timeComeHome = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.comeHome = true;
			Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.state = 4;
		}
		if (potionType < 10 || potionType > 20)
		{
			mainChar.potions[potionType]--;
			mainChar.potionLastTimeUse[potionType] = currentTimeMillis;
		}
		if (mainChar.state != 3 && autoFight && !mainChar.isDie && Tilemap_HAIRMOD_PROTECTED.lv != 0 && Tilemap_HAIRMOD_PROTECTED.lv != 201 && Tilemap_HAIRMOD_PROTECTED.lv != 70 && Tilemap_HAIRMOD_PROTECTED.lv != 80 && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			mainChar.hp += getMpHpPutKey(0, potionType);
			mainChar.mp += getMpHpPutKey(1, potionType);
			if (mainChar.hp >= mainChar.maxhp)
			{
				mainChar.hp = mainChar.maxhp;
			}
			if (mainChar.mp >= mainChar.maxmp)
			{
				mainChar.mp = mainChar.maxmp;
			}
		}
		GameService_HAIRMOD_PROTECTED.gI().usePotion(potionType);
	}

	public int getMpHpPutKey(int type, int idPotion)
	{
		for (int i = 0; i < TYPE_MP_HP[type].Length; i++)
		{
			if (TYPE_MP_HP[type][i] == idPotion)
			{
				return VALUE_MP_HP[type][i];
			}
		}
		return 0;
	}

	public void doParty()
	{
		if (focusedActor != null)
		{
			if (focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Mời party");
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					if (((Char_HAIRMOD_PROTECTED)focusedActor).pk > 0 || ((Char_HAIRMOD_PROTECTED)focusedActor).iskiller)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể mời người trong trạng thái đánh nhau hoặc phạm tội");
					}
					else if (mainChar.IDParty == -1)
					{
						gameService.requestCreateParty(mainChar.ID);
						tempIDActorParty = focusedActor.ID;
					}
					else
					{
						gameService.invite2Party(focusedActor.ID);
					}
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				if (map != 0)
				{
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Trao đổi");
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
					if (!mainChar.isTrade)
					{
						gameService.trade(focusedActor.ID);
					}
				};
				command_HAIRMOD_PROTECTED2.action = action2;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Mời đám cưới");
				ActionPerform_HAIRMOD_PROTECTED action3 = delegate
				{
					gameService.invite2Party(focusedActor.ID, 3);
				};
				command_HAIRMOD_PROTECTED3.action = action3;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
				if (focusedActor is Char_HAIRMOD_PROTECTED)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Nhắn tin");
					ActionPerform_HAIRMOD_PROTECTED action4 = delegate
					{
						MessageScr_HAIRMOD_PROTECTED.gI().addTab(string.Empty, ((Char_HAIRMOD_PROTECTED)focusedActor).name, 0);
						MessageScr_HAIRMOD_PROTECTED.gI().setFocusTab(((Char_HAIRMOD_PROTECTED)focusedActor).name);
						MessageScr_HAIRMOD_PROTECTED.gI().switchToMe();
					};
					command_HAIRMOD_PROTECTED4.action = action4;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED4);
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Kết bạn");
				ActionPerform_HAIRMOD_PROTECTED action5 = delegate
				{
					gameService.sendInfoAddFriend(((Char_HAIRMOD_PROTECTED)focusedActor).ID, 0);
				};
				command_HAIRMOD_PROTECTED5.action = action5;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED5);
				if ((Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 0 || Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 1 || Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isMaster == 2) && ((Char_HAIRMOD_PROTECTED)focusedActor).idClan == -1)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Mời vào bang hội");
					ActionPerform_HAIRMOD_PROTECTED action6 = delegate
					{
						gameService.sendRequestAddClan(focusedActor.ID, 0, false);
					};
					command_HAIRMOD_PROTECTED6.action = action6;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED6);
				}
				if (((Char_HAIRMOD_PROTECTED)focusedActor).idClan != -1)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED7 = new Command_HAIRMOD_PROTECTED("Xem thông tin bang hội");
					ActionPerform_HAIRMOD_PROTECTED action7 = delegate
					{
						gameService.requestClanInfo(((Char_HAIRMOD_PROTECTED)focusedActor).idClan);
					};
					command_HAIRMOD_PROTECTED7.action = action7;
					mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED7);
				}
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED8 = new Command_HAIRMOD_PROTECTED("Xem thông tin");
				ActionPerform_HAIRMOD_PROTECTED action8 = delegate
				{
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
					if (focusedActor != null)
					{
						saveIDViewInfoAnimal = focusedActor.ID;
						gameService.requestSomeOneInfo(focusedActor.ID, 0);
					}
					else
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không tìm thấy thông tin");
					}
				};
				command_HAIRMOD_PROTECTED8.action = action8;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED8);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED9 = new Command_HAIRMOD_PROTECTED("Xuống ngựa");
				ActionPerform_HAIRMOD_PROTECTED action9 = delegate
				{
					unRideHorse();
				};
				command_HAIRMOD_PROTECTED9.action = action9;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED9);
				Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 2);
			}
			else
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = false;
			}
		}
		else
		{
			unRideHorse();
		}
	}

	public void unRideHorse()
	{
		if (mainChar.useHorse != -1 || mainChar.idhorse > -1)
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				gameService.unRideHorse(0);
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Ngựa sẽ bị mất nếu rương đồ không còn chỗ. Bạn có muốn xuống ngựa không ?", yesAction);
		}
	}

	protected mVector_HAIRMOD_PROTECTED getNearMonster(Actor_HAIRMOD_PROTECTED mt, int distance)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		mVector_HAIRMOD_PROTECTED2.addElement(mt);
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MONSTER && actor_HAIRMOD_PROTECTED.state != 8 && actor_HAIRMOD_PROTECTED.ID != mt.ID && abs(actor_HAIRMOD_PROTECTED.x - mt.x) <= distance && abs(actor_HAIRMOD_PROTECTED.y - mt.y) <= distance)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(actor_HAIRMOD_PROTECTED);
				if (mVector_HAIRMOD_PROTECTED2.size() > 10)
				{
					break;
				}
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	private bool isMapPK()
	{
		if (Tilemap_HAIRMOD_PROTECTED.lv == 112 || Tilemap_HAIRMOD_PROTECTED.lv == 2541 || Tilemap_HAIRMOD_PROTECTED.lv == 2542 || Tilemap_HAIRMOD_PROTECTED.lv == 2543 || Tilemap_HAIRMOD_PROTECTED.lv == 2544)
		{
			return true;
		}
		return false;
	}

	private bool isChiemThanh()
	{
		if (((mainChar.x > xArena * 16 && mainChar.y > yArena * 16 && mainChar.x < wArena * 16 && mainChar.y < hArena * 16) || isFight) && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_NPC && Tilemap_HAIRMOD_PROTECTED.lv == 201)
		{
			return true;
		}
		return false;
	}

	private bool isFocusKiller()
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)focusedActor;
		if (char_HAIRMOD_PROTECTED != null && char_HAIRMOD_PROTECTED.iskiller)
		{
			return true;
		}
		return false;
	}

	public static void addviewInfo(Actor_HAIRMOD_PROTECTED ac, string str, int time)
	{
		if (ac != null)
		{
			ac.chat = new ChatPopup_HAIRMOD_PROTECTED(time, str, 0);
			ac.chat.setPos(ac.x, ac.y);
		}
	}

	public void doViewInfoGround(string text, Actor_HAIRMOD_PROTECTED obj)
	{
		obj.chat = null;
		addviewInfo(obj, text, 50);
	}

	public void addMsgWorld(string info)
	{
		MsgInfo_HAIRMOD_PROTECTED o = new MsgInfo_HAIRMOD_PROTECTED(info);
		msgWorld.addElement(o);
		savemsgWorld.addElement(o);
	}

	public void sendActorMove(int x, int y)
	{
		mainChar.stop = false;
		if (mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - timeMove >= 0)
		{
			timeMove = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + dtmove;
			gameService.moveChar(x, y);
		}
	}

	public void doUseSkillToFocusActor(sbyte skillType)
	{
		sbyte skillIDFromType = mainChar.getSkillIDFromType(skillType);
		try
		{
			if (focusedActor == null || (autoFight && (focusedActor.isNPC() || focusedActor.isNpcChar())))
			{
				return;
			}
			if (iTaskAuto != 2 && timeRemovePos <= 0)
			{
				mainChar.posTransRoad = null;
				tg.x = (tg.y = -1);
			}
			if (focusedActor.isMonster() || (mainChar.nationID != focusedActor.nationID && focusedActor.isPlayer()) || (focusedActor.isPlayer() && ((idMapPK == Tilemap_HAIRMOD_PROTECTED.lv && mainChar.idClan != ((Char_HAIRMOD_PROTECTED)focusedActor).idClan) || (focusedActor.pk > 0 && mainChar.pk > 0 && focusedActor.pk != mainChar.pk))) || (mainChar.iskiller && focusedActor.isPlayer()) || (focusedActor.isPlayer() && (isChiemThanh() || isMapPK() || isFocusKiller())) || (focusedActor.isPlayer() && ((Char_HAIRMOD_PROTECTED)focusedActor).isAnimal) || (mainChar.isAnimal && (focusedActor.isMonster() || focusedActor.isPlayer())))
			{
				int num = 0;
				if (focusedActor.isMonster() && ((Monster_HAIRMOD_PROTECTED)focusedActor).isMineral())
				{
					num = 1;
					skillIDFromType = mainChar.getSkillIDFromType(0);
					skillType = 0;
				}
				if (!focusedActor.isPlayer() || !focusedActor.isNPC())
				{
					int skillMP = SkillManager_HAIRMOD_PROTECTED.getSkillMP(skillType, Char_HAIRMOD_PROTECTED.skillLevelLearnt[skillType], mainChar.clazz);
					if (skillMP > mainChar.mp)
					{
						if (!first)
						{
							addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Không đủ MP", 0));
							first = true;
						}
						return;
					}
					if (mainChar.getDurableWeapone() <= 0)
					{
						addChat(mainChar, "Không thể tấn công khi vũ khí bị hư hại. Hãy đến thợ rèn để sửa lại.", 70);
						return;
					}
					if (autoFight)
					{
						mainChar.idMonster = focusedActor.ID;
						mainChar.autoFight = true;
					}
					first = false;
					LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)focusedActor;
					if ((liveActor_HAIRMOD_PROTECTED.realHP <= 0 || liveActor_HAIRMOD_PROTECTED.hp <= 0) && liveActor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
					{
						return;
					}
					int num2 = SkillManager_HAIRMOD_PROTECTED.getSkillRange(skillType, mainChar.clazz);
					if (num == 1)
					{
						num2 = 30;
					}
					if (Util_HAIRMOD_PROTECTED.distance(mainChar.x, mainChar.y, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y) > num2)
					{
						coutChangeFocusWhenDoing++;
						moveToRange(liveActor_HAIRMOD_PROTECTED, num2);
					}
					else
					{
						isAutoRangeWhenAuto = false;
						coutChangeFocusWhenDoing = 0;
						if ((mainChar.iskiller && focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && ((Char_HAIRMOD_PROTECTED)focusedActor).level < 10 && mainChar.nationID == focusedActor.nationID) || (focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MONSTER && ((Monster_HAIRMOD_PROTECTED)focusedActor).state == 5))
						{
							return;
						}
						long currentTimeMillis = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
						if (currentTimeMillis - mainChar.timeLastUseSkills[skillType] > mainChar.coolDownSkill[skillType])
						{
							mainChar.dir = Util_HAIRMOD_PROTECTED.findDirection(mainChar, liveActor_HAIRMOD_PROTECTED);
							AttackResult_HAIRMOD_PROTECTED attackResult_HAIRMOD_PROTECTED = new AttackResult_HAIRMOD_PROTECTED();
							attackResult_HAIRMOD_PROTECTED.dam = 2000000;
							mainChar.mp -= skillMP;
							mainChar.startAttack(liveActor_HAIRMOD_PROTECTED, attackResult_HAIRMOD_PROTECTED, skillType, Char_HAIRMOD_PROTECTED.skillLevelLearnt[skillType]);
							bool flag = SkillManager_HAIRMOD_PROTECTED.isSkillAeo(mainChar.clazz, skillType);
							mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
							if (flag)
							{
								if (liveActor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MONSTER)
								{
									mainChar.currentSkillAnimate.setMonster(mVector_HAIRMOD_PROTECTED2 = getNearMonster(liveActor_HAIRMOD_PROTECTED, SkillManager_HAIRMOD_PROTECTED.getRangeSkillAeo(mainChar.clazz, skillType)));
								}
								else if (liveActor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
								{
									mVector_HAIRMOD_PROTECTED2.addElement(liveActor_HAIRMOD_PROTECTED);
									mainChar.currentSkillAnimate.setMonster(mVector_HAIRMOD_PROTECTED2);
								}
							}
							changeSinceLastUpdate = false;
							switch (liveActor_HAIRMOD_PROTECTED.catagory)
							{
							case 0:
								gameService.attackOtherPlayer(liveActor_HAIRMOD_PROTECTED.ID, skillType, attackResult_HAIRMOD_PROTECTED.effect, attackResult_HAIRMOD_PROTECTED.dam, 0, attackResult_HAIRMOD_PROTECTED.buffEffect);
								mainChar.downDurableWeapone();
								break;
							case 1:
								if (!flag)
								{
									gameService.attackMonster(liveActor_HAIRMOD_PROTECTED.ID, skillType, attackResult_HAIRMOD_PROTECTED.effect, attackResult_HAIRMOD_PROTECTED.dam, 0, attackResult_HAIRMOD_PROTECTED.buffEffect);
								}
								else
								{
									gameService.attackMultiMonster(liveActor_HAIRMOD_PROTECTED.ID, skillType, attackResult_HAIRMOD_PROTECTED.effect, attackResult_HAIRMOD_PROTECTED.dam, 0, attackResult_HAIRMOD_PROTECTED.buffEffect, mVector_HAIRMOD_PROTECTED2);
								}
								mainChar.downDurableWeapone();
								break;
							}
							return;
						}
					}
				}
			}
			if (focusedActor != null && focusedActor.isItemCanGet())
			{
				Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED = (Dropable_HAIRMOD_PROTECTED)focusedActor;
				if (dropable_HAIRMOD_PROTECTED.isSendGet)
				{
					return;
				}
				if (Util_HAIRMOD_PROTECTED.distance(mainChar.x, mainChar.y, dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y) <= 35)
				{
					bool flag2 = true;
					string content = string.Empty;
					if (dropable_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_POTION)
					{
						if (dropable_HAIRMOD_PROTECTED.item_template_id == 0 && mainChar.charXu > 100000000)
						{
							flag2 = false;
							content = "Quá nhiều tiền";
						}
					}
					else if (dropable_HAIRMOD_PROTECTED.catagory == 3 && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isFullInventory())
					{
						content = "Hành trang đã đầy";
						flag2 = false;
					}
					if (!flag2)
					{
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, content, 0));
						changeToNextFocusActor(false);
						return;
					}
					mainChar.dir = Util_HAIRMOD_PROTECTED.findDirection(mainChar, dropable_HAIRMOD_PROTECTED);
					sendActorMove(mainChar.x, mainChar.y);
					changeSinceLastUpdate = false;
					gameService.getDropableFromGround(dropable_HAIRMOD_PROTECTED.catagory, dropable_HAIRMOD_PROTECTED.ID);
					return;
				}
				moveToRange(dropable_HAIRMOD_PROTECTED, 35);
			}
			if (focusedActor.isNPC())
			{
				if (focusedActor.getNpcType() == 1)
				{
					GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo(focusedActor.getTypeNpc());
					return;
				}
				if (!Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
				{
					mVector_HAIRMOD_PROTECTED allQuestNpc = getAllQuestNpc(focusedActor.getTypeNpc());
					if (allQuestNpc.size() > 0 && !focusedActor.isNpcChar())
					{
						mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = allQuestNpc;
						if (posNpcReceiveClan != null && ((NPC_HAIRMOD_PROTECTED)focusedActor).type == posNpcReceiveClan.index)
						{
							Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Nhiệm vụ bang hội");
							command_HAIRMOD_PROTECTED.action = delegate
							{
								gameService.questClan(3);
								posNpcReceiveClan = null;
								Canvas_HAIRMOD_PROTECTED.startWaitDlg();
							};
							mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED);
						}
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Nói chuyện");
						command_HAIRMOD_PROTECTED2.action = delegate
						{
							mainChar.chat = null;
							focusedActor.chat = null;
							if (!focusedActor.isNpcChar() && focusedActor.getTypeNpc() != 7 && focusedActor.getTypeNpc() != 10 && focusedActor.getTypeNpc() != 22 && focusedActor.getTypeNpc() != 31 && focusedActor.getTypeNpc() != 21 && focusedActor.getTypeNpc() != 25)
							{
								addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(focusedActor.getTypeNpc()), 500);
							}
							else if (!focusedActor.isNpcChar())
							{
								showNpcMenu(focusedActor.getTypeNpc(), new mVector_HAIRMOD_PROTECTED());
							}
						};
						mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED2);
						Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 3);
						return;
					}
					focusedActor.chat = null;
					if (!focusedActor.isNpcChar() && focusedActor.getTypeNpc() != 7 && focusedActor.getTypeNpc() != 10 && focusedActor.getTypeNpc() != 22 && focusedActor.getTypeNpc() != 31 && focusedActor.getTypeNpc() != 21 && focusedActor.getTypeNpc() != 25)
					{
						addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(focusedActor.getTypeNpc()), 500);
					}
					else if (!focusedActor.isNpcChar())
					{
						showNpcMenu(focusedActor.getTypeNpc(), new mVector_HAIRMOD_PROTECTED());
					}
				}
				else if (focusedActor.getTypeNpc() == 2 || (focusedActor.getTypeNpc() > 10 && focusedActor.getTypeNpc() != 29 && focusedActor.getTypeNpc() != 24 && focusedActor.getTypeNpc() != 26 && focusedActor.getTypeNpc() != 30) || focusedActor.getTypeNpc() == 3 || focusedActor.getTypeNpc() == 27 || focusedActor.getTypeNpc() == 28)
				{
					showNpcMenu(focusedActor.getTypeNpc(), new mVector_HAIRMOD_PROTECTED());
				}
				else
				{
					GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo(focusedActor.getTypeNpc());
				}
			}
			if (focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
			{
				return;
			}
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
			if (focusedActor != null)
			{
				Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED = (Ground_HAIRMOD_PROTECTED)focusedActor;
				if (ground_HAIRMOD_PROTECTED.isBuyOk)
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Xem thông tin");
					ActionPerform_HAIRMOD_PROTECTED action = delegate
					{
						doViewInfoGround(focusedActor.infoGround, focusedActor);
					};
					command_HAIRMOD_PROTECTED3.action = action;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED3);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Nâng cấp đất");
					ActionPerform_HAIRMOD_PROTECTED action2 = delegate
					{
						if (focusedActor != null)
						{
							Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED5 = (Ground_HAIRMOD_PROTECTED)focusedActor;
							gameService.doPlant(4, (sbyte)ground_HAIRMOD_PROTECTED5.ID);
						}
					};
					command_HAIRMOD_PROTECTED4.action = action2;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED4);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Trồng cây");
					ActionPerform_HAIRMOD_PROTECTED action3 = delegate
					{
						if (focusedActor != null)
						{
							Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED4 = (Ground_HAIRMOD_PROTECTED)focusedActor;
							gameService.doPlant(0, (sbyte)ground_HAIRMOD_PROTECTED4.ID);
						}
					};
					command_HAIRMOD_PROTECTED5.action = action3;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED5);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Thu hoạch");
					ActionPerform_HAIRMOD_PROTECTED action4 = delegate
					{
						if (focusedActor != null)
						{
							Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED3 = (Ground_HAIRMOD_PROTECTED)focusedActor;
							gameService.doPlant(1, (sbyte)ground_HAIRMOD_PROTECTED3.ID);
						}
					};
					command_HAIRMOD_PROTECTED6.action = action4;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED6);
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED7 = new Command_HAIRMOD_PROTECTED("Nhổ bỏ");
					ActionPerform_HAIRMOD_PROTECTED action5 = delegate
					{
						if (focusedActor != null)
						{
							Ground_HAIRMOD_PROTECTED gr40 = (Ground_HAIRMOD_PROTECTED)focusedActor;
							if (gr40.myTree != null)
							{
								ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
								{
									gameService.doPlant(3, (sbyte)gr40.ID);
									Canvas_HAIRMOD_PROTECTED.endDlg();
								};
								Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn nhổ bỏ cây này không? ", yesAction);
							}
						}
					};
					command_HAIRMOD_PROTECTED7.action = action5;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED7);
				}
				else
				{
					Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED8 = new Command_HAIRMOD_PROTECTED("Mua đất");
					ActionPerform_HAIRMOD_PROTECTED action6 = delegate
					{
						if (focusedActor != null)
						{
							Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED2 = (Ground_HAIRMOD_PROTECTED)focusedActor;
							gameService.doPlant(2, (sbyte)ground_HAIRMOD_PROTECTED2.ID);
						}
					};
					command_HAIRMOD_PROTECTED8.action = action6;
					mVector_HAIRMOD_PROTECTED4.addElement(command_HAIRMOD_PROTECTED8);
				}
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED4, 3);
		}
		catch (Exception)
		{
		}
	}

	private void moveToRange(Actor_HAIRMOD_PROTECTED attackedLiveActor, int maxRange)
	{
		if ((autoFight && typeOptionFocus == 1 && mainChar.posTransRoad != null) || mainChar.posTransRoad != null || (isFight && map == 201))
		{
			return;
		}
		int num = Util_HAIRMOD_PROTECTED.angle(attackedLiveActor.x - mainChar.x, -(attackedLiveActor.y - mainChar.y));
		int num2 = Util_HAIRMOD_PROTECTED.distance(mainChar.x, mainChar.y, attackedLiveActor.x, attackedLiveActor.y);
		int num3 = (num2 - maxRange) * Util_HAIRMOD_PROTECTED.Cos(num) >> 10;
		int num4 = -((num2 - maxRange) * Util_HAIRMOD_PROTECTED.Sin(num)) >> 10;
		bool flag = false;
		if (num3 > 0)
		{
			if (num3 > 50)
			{
				num3 = 50;
			}
		}
		else if (num3 < 0 && num3 < -50)
		{
			num3 = -50;
		}
		if (num4 > 0)
		{
			if (num4 > 50)
			{
				num4 = 50;
			}
		}
		else if (num4 < 0 && num4 < -50)
		{
			num4 = -50;
		}
		if (autoFight && typeOptionFocus == 1 && focusedActor != null && focusedActor.iskiller)
		{
			flag = true;
		}
		if (map == 17)
		{
			if (autoFight && mainChar.isDoing && coutChangeFocusWhenDoing > 10)
			{
				changeToNextFocusActor(false);
				coutChangeFocusWhenDoing = 0;
			}
		}
		else if (autoFight && !flag)
		{
			num3 = (num4 = 0);
			changeToNextFocusActor(false);
		}
		if (flag && timeRemovePos <= 0 && (Math_HAIRMOD_PROTECTED.abs(mainChar.x - mainChar.lastXAuto) > 120 || Math_HAIRMOD_PROTECTED.abs(mainChar.y - mainChar.lastYAuto) > 120))
		{
			num3 = (num4 = 0);
			changeToNextFocusActor(true);
			mainChar.timeGoToLastXY = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() - 1;
			findRoad2(mainChar.lastXAuto, mainChar.lastYAuto);
		}
		mainChar.moveTo((short)(mainChar.x + num3), (short)(mainChar.y + num4));
		if (!autoFight || flag)
		{
			sendActorMove((short)(mainChar.x + num3), (short)(mainChar.y + num4));
		}
	}

	private void showNpcMenu(int type, mVector_HAIRMOD_PROTECTED menu)
	{
		if (type == 3 || type == 27)
		{
			int num = Res_HAIRMOD_PROTECTED.nameClazz.Length;
			for (int i = 0; i < num; i++)
			{
				int ii = i;
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(Res_HAIRMOD_PROTECTED.nameClazz[i]);
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo((short)type, ii);
					WindowInfoScr_HAIRMOD_PROTECTED.gI().idClass = ii;
				};
				command_HAIRMOD_PROTECTED.action = action;
				menu.addElement(command_HAIRMOD_PROTECTED);
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(menu, 3);
		}
		else if (type == 2 || type == 28)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Mua bán");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().requestNPCInfo((short)type);
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			if (iTaskAuto != 2)
			{
				menu.addElement(command_HAIRMOD_PROTECTED2);
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Nghiền bột");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				ScreenItemTim_HAIRMOD_PROTECTED screenItemTim_HAIRMOD_PROTECTED = new ScreenItemTim_HAIRMOD_PROTECTED();
				screenItemTim_HAIRMOD_PROTECTED.setSizeCell(5, false);
				screenItemTim_HAIRMOD_PROTECTED.switchToMe(this);
			};
			command_HAIRMOD_PROTECTED3.action = action3;
			menu.addElement(command_HAIRMOD_PROTECTED3);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED("Thêm dòng");
			ActionPerform_HAIRMOD_PROTECTED action4 = delegate
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(4, false);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().tile = "Thêm dòng";
			};
			command_HAIRMOD_PROTECTED4.action = action4;
			menu.addElement(command_HAIRMOD_PROTECTED4);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED5 = new Command_HAIRMOD_PROTECTED("Luyện đồ");
			ActionPerform_HAIRMOD_PROTECTED action5 = delegate
			{
				if (mainChar.haveItemImbue())
				{
					gameService.requestImbue(0);
				}
				else
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chưa có nguyên tố để luyện, hãy đến nhà lão Địa chủ mua rồi quay lại đây.");
				}
			};
			command_HAIRMOD_PROTECTED5.action = action5;
			menu.addElement(command_HAIRMOD_PROTECTED5);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED6 = new Command_HAIRMOD_PROTECTED("Luyện đồ tự động");
			ScreenDapDo_HAIRMOD_PROTECTED.gI().tile = "Luyện đồ tự động";
			command_HAIRMOD_PROTECTED6.action = delegate
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(1, false);
			};
			menu.addElement(command_HAIRMOD_PROTECTED6);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED7 = new Command_HAIRMOD_PROTECTED("Cộng thuộc tính");
			ScreenDapDo_HAIRMOD_PROTECTED.gI().tile = "Cộng thuộc tính";
			command_HAIRMOD_PROTECTED7.action = delegate
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(0, false);
			};
			menu.addElement(command_HAIRMOD_PROTECTED7);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED8 = new Command_HAIRMOD_PROTECTED("Khóa đồ thú");
			command_HAIRMOD_PROTECTED8.action = delegate
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(2, false);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().tile = "Khóa đồ thú";
			};
			menu.addElement(command_HAIRMOD_PROTECTED8);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED9 = new Command_HAIRMOD_PROTECTED("Khóa trang bị");
			command_HAIRMOD_PROTECTED9.action = delegate
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(3, false);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().tile = "Khóa trang bị";
			};
			menu.addElement(command_HAIRMOD_PROTECTED9);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED10 = new Command_HAIRMOD_PROTECTED("Sửa đồ");
			ActionPerform_HAIRMOD_PROTECTED action6 = delegate
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED28 = new Command_HAIRMOD_PROTECTED("Vũ khí");
				ActionPerform_HAIRMOD_PROTECTED action24 = delegate
				{
					if (mainChar.charXu < mainChar.getPriceRepair(1))
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Hết tiền");
					}
					else
					{
						ActionPerform_HAIRMOD_PROTECTED yesAction3 = delegate
						{
							mainChar.charXu -= mainChar.getPriceRepair(1);
							gameService.repairItem(0);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						};
						ActionPerform_HAIRMOD_PROTECTED noAction3 = delegate
						{
							Canvas_HAIRMOD_PROTECTED.currentDialog = null;
						};
						Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Phí sửa chữa là " + mainChar.getPriceRepair(1) + " " + Res_HAIRMOD_PROTECTED.stMoney, yesAction3, noAction3);
					}
				};
				command_HAIRMOD_PROTECTED28.action = action24;
				mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED28);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED29 = new Command_HAIRMOD_PROTECTED("Quần áo");
				ActionPerform_HAIRMOD_PROTECTED action25 = delegate
				{
					if (mainChar.charXu < mainChar.getPriceRepair(0))
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Hết tiền");
					}
					else
					{
						ActionPerform_HAIRMOD_PROTECTED yesAction2 = delegate
						{
							mainChar.charXu -= mainChar.getPriceRepair(0);
							gameService.repairItem(1);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						};
						ActionPerform_HAIRMOD_PROTECTED noAction2 = delegate
						{
							Canvas_HAIRMOD_PROTECTED.currentDialog = null;
						};
						Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Phí sửa chữa là " + mainChar.getPriceRepair(0) + " " + Res_HAIRMOD_PROTECTED.stMoney, yesAction2, noAction2);
					}
				};
				command_HAIRMOD_PROTECTED29.action = action25;
				mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED29);
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED30 = new Command_HAIRMOD_PROTECTED("Tất cả");
				ActionPerform_HAIRMOD_PROTECTED action26 = delegate
				{
					if (mainChar.charXu < mainChar.getPriceRepair(2))
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Hết tiền");
					}
					else
					{
						ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
						{
							mainChar.charXu -= mainChar.getPriceRepair(2);
							gameService.repairItem(2);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						};
						ActionPerform_HAIRMOD_PROTECTED noAction = delegate
						{
							Canvas_HAIRMOD_PROTECTED.currentDialog = null;
						};
						Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Phí sửa chữa là " + mainChar.getPriceRepair(2) + " " + Res_HAIRMOD_PROTECTED.stMoney, yesAction, noAction);
					}
				};
				command_HAIRMOD_PROTECTED30.action = action26;
				mVector_HAIRMOD_PROTECTED3.addElement(command_HAIRMOD_PROTECTED30);
				Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 3);
			};
			command_HAIRMOD_PROTECTED10.action = action6;
			menu.addElement(command_HAIRMOD_PROTECTED10);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED11 = new Command_HAIRMOD_PROTECTED("Đục lỗ");
			ActionPerform_HAIRMOD_PROTECTED action7 = delegate
			{
				gameService.requestKham(0);
				WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Đục lỗ";
				WindowInfoScr_HAIRMOD_PROTECTED.isKham = false;
			};
			command_HAIRMOD_PROTECTED11.action = action7;
			menu.addElement(command_HAIRMOD_PROTECTED11);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED12 = new Command_HAIRMOD_PROTECTED("Khảm");
			ActionPerform_HAIRMOD_PROTECTED action8 = delegate
			{
				gameService.requestKham(0);
				WindowInfoScr_HAIRMOD_PROTECTED.isKham = true;
				WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Khảm";
			};
			command_HAIRMOD_PROTECTED12.action = action8;
			menu.addElement(command_HAIRMOD_PROTECTED12);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED13 = new Command_HAIRMOD_PROTECTED("Hợp thành");
			ActionPerform_HAIRMOD_PROTECTED action9 = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().epNgoc(0, null);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			};
			command_HAIRMOD_PROTECTED13.action = action9;
			menu.addElement(command_HAIRMOD_PROTECTED13);
		}
		else if (type == 7)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED14 = new Command_HAIRMOD_PROTECTED("Đi đến");
			ActionPerform_HAIRMOD_PROTECTED action10 = delegate
			{
				XaphuTemplate_HAIRMOD_PROTECTED xp = Res_HAIRMOD_PROTECTED.getXaphu();
				if (xp != null)
				{
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
					int num2 = xp.mapID.Length;
					for (int j = 0; j < num2; j++)
					{
						int aa = j;
						int idm = xp.mapID[aa];
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED27 = new Command_HAIRMOD_PROTECTED(Res_HAIRMOD_PROTECTED.nameBigMap[xp.mapID[aa]]);
						ActionPerform_HAIRMOD_PROTECTED action23 = delegate
						{
							if (xp.price[aa] > mainChar.charXu)
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Không còn đủ tiền");
							}
							else
							{
								doUseXaPhu(idm, aa);
							}
						};
						command_HAIRMOD_PROTECTED27.action = action23;
						command_HAIRMOD_PROTECTED27.idMap = xp.mapID[aa];
						mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED27);
					}
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
				}
			};
			command_HAIRMOD_PROTECTED14.action = action10;
			menu.addElement(command_HAIRMOD_PROTECTED14);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED15 = new Command_HAIRMOD_PROTECTED("Giao tiếp");
			ActionPerform_HAIRMOD_PROTECTED action11 = delegate
			{
				addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(type), 500);
			};
			command_HAIRMOD_PROTECTED15.action = action11;
			menu.addElement(command_HAIRMOD_PROTECTED15);
		}
		else if (type == 10)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED16 = new Command_HAIRMOD_PROTECTED("Hoa tiêu");
			ActionPerform_HAIRMOD_PROTECTED action12 = delegate
			{
				if (mainChar.potions[20] <= 0)
				{
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa có vé");
				}
				else
				{
					gameService.moveUpBoard();
					Canvas_HAIRMOD_PROTECTED.startWaitDlg();
				}
			};
			command_HAIRMOD_PROTECTED16.action = action12;
			menu.addElement(command_HAIRMOD_PROTECTED16);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED17 = new Command_HAIRMOD_PROTECTED("Mua vé (" + pTicket + " xu)");
			ActionPerform_HAIRMOD_PROTECTED action13 = delegate
			{
				gameService.buyTicket();
				Canvas_HAIRMOD_PROTECTED.startOKDlg("xin chờ", true);
			};
			command_HAIRMOD_PROTECTED17.action = action13;
			menu.addElement(command_HAIRMOD_PROTECTED17);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED18 = new Command_HAIRMOD_PROTECTED("Nói chuyện");
			ActionPerform_HAIRMOD_PROTECTED action14 = delegate
			{
				addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(type), 500);
			};
			command_HAIRMOD_PROTECTED18.action = action14;
			menu.addElement(command_HAIRMOD_PROTECTED18);
		}
		else if (type == 22 || type == 31)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED19 = new Command_HAIRMOD_PROTECTED("Nạp xu");
			ActionPerform_HAIRMOD_PROTECTED action15 = delegate
			{
				doNapXu();
			};
			command_HAIRMOD_PROTECTED19.action = action15;
			menu.addElement(command_HAIRMOD_PROTECTED19);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED20 = new Command_HAIRMOD_PROTECTED("Nói chuyện");
			ActionPerform_HAIRMOD_PROTECTED action16 = delegate
			{
				addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(type), 500);
			};
			command_HAIRMOD_PROTECTED20.action = action16;
			menu.addElement(command_HAIRMOD_PROTECTED20);
		}
		else if (type == 25)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED21 = new Command_HAIRMOD_PROTECTED("Giao tiếp");
			ActionPerform_HAIRMOD_PROTECTED action17 = delegate
			{
				addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(type), 500);
			};
			command_HAIRMOD_PROTECTED21.action = action17;
			menu.addElement(command_HAIRMOD_PROTECTED21);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED22 = new Command_HAIRMOD_PROTECTED("Chuyển thẻ");
			ActionPerform_HAIRMOD_PROTECTED action18 = delegate
			{
				gameService.giveCard2NPC(focusedActor.x / 16, focusedActor.y / 16);
			};
			command_HAIRMOD_PROTECTED22.action = action18;
			menu.addElement(command_HAIRMOD_PROTECTED22);
		}
		else if (type == 21)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED23 = new Command_HAIRMOD_PROTECTED("Học kỹ năng");
			ActionPerform_HAIRMOD_PROTECTED action19 = delegate
			{
				showWindow(0, true, new sbyte[1] { 7 });
			};
			command_HAIRMOD_PROTECTED23.action = action19;
			menu.addElement(command_HAIRMOD_PROTECTED23);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED24 = new Command_HAIRMOD_PROTECTED("Nói chuyện");
			ActionPerform_HAIRMOD_PROTECTED action20 = delegate
			{
				addChat(focusedActor, Res_HAIRMOD_PROTECTED.getDefaultChat(type), 500);
			};
			command_HAIRMOD_PROTECTED24.action = action20;
			menu.addElement(command_HAIRMOD_PROTECTED24);
		}
		else if (type > 10 && type != 30)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED25 = new Command_HAIRMOD_PROTECTED("Bán");
			ActionPerform_HAIRMOD_PROTECTED action21 = delegate
			{
				gameService.requestNPCInfo(type, 0);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			};
			command_HAIRMOD_PROTECTED25.action = action21;
			menu.addElement(command_HAIRMOD_PROTECTED25);
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED26 = new Command_HAIRMOD_PROTECTED("Mua");
			ActionPerform_HAIRMOD_PROTECTED action22 = delegate
			{
				gameService.requestNPCInfo(type, 1);
				Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			};
			command_HAIRMOD_PROTECTED26.action = action22;
			menu.addElement(command_HAIRMOD_PROTECTED26);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(menu, 3);
	}

	public void doUseXaPhu(int mapID, int idXphu)
	{
		int indexArrMap = getIndexArrMap(mapID);
		string[] name = Res_HAIRMOD_PROTECTED.namMap[indexArrMap];
		short[] local = Res_HAIRMOD_PROTECTED.localXaphu[indexArrMap];
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int num = name.Length;
		for (int j = 0; j < num; j++)
		{
			int i = j;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(name[j]);
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				gameService.doMove2Map((sbyte)idXphu, local[i * 3]);
				ChangScr_HAIRMOD_PROTECTED.gI().switchToMe();
				ChangScr_HAIRMOD_PROTECTED.gI().setMap(map, 0, 0);
				Res_HAIRMOD_PROTECTED.removeMonsterTemplet();
				nameMap = name[i];
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
		Canvas_HAIRMOD_PROTECTED.menu.setIndex();
	}

	public void doNapXu()
	{
		if (decriptNap.size() == 0)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Chức năng này hiện đang tạm khóa");
			return;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int num = decriptNap.size();
		for (int i = 0; i < num; i++)
		{
			string syn = (string)syntaxNap.elementAt(i);
			string center = (string)centerNap.elementAt(i);
			string decription = (string)decriptNap.elementAt(i);
			if (center.Length >= 4)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED((string)decriptNap.elementAt(i));
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					ActionPerform_HAIRMOD_PROTECTED successAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi tin. Xin chờ trong chốc lát để nhận tin nhắn phản hồi.");
					};
					ActionPerform_HAIRMOD_PROTECTED failAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Có lỗi khi gửi tin nhắn nạp. Xin hãy thử lại");
					};
					GameMidlet_HAIRMOD_PROTECTED.sendSMS(syn, "sms://" + center, successAction, failAction);
				};
				command_HAIRMOD_PROTECTED.action = action;
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
				continue;
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED((string)decriptNap.elementAt(i));
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				if (!syn.Equals("-1"))
				{
					ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
					{
						GameMidlet_HAIRMOD_PROTECTED.requestLink(syn);
					};
					ActionPerform_HAIRMOD_PROTECTED noAction = delegate
					{
						Canvas_HAIRMOD_PROTECTED.currentDialog = null;
					};
					Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Game sẽ tự động thoát. Vui lòng đăng nhập lại sau ít phút sau khi nạp.", yesAction, noAction);
				}
				else
				{
					doNapMoney(decription);
				}
			};
			command_HAIRMOD_PROTECTED2.action = action2;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	public bool checkMoveLimit(int xTo, int yTo)
	{
		if (npc_Limit.size() == 0)
		{
			return false;
		}
		int num = npc_Limit.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)npc_Limit.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null)
			{
				Npc_Server_HAIRMOD_PROTECTED npc_Server_HAIRMOD_PROTECTED = (Npc_Server_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
				if (xTo >= npc_Server_HAIRMOD_PROTECTED.getLimxL() && xTo <= npc_Server_HAIRMOD_PROTECTED.getLimxR() && yTo >= npc_Server_HAIRMOD_PROTECTED.getLimyU() && yTo <= npc_Server_HAIRMOD_PROTECTED.getLimyD())
				{
					return true;
				}
			}
		}
		return false;
	}

	private void popupNapTien(int index, string decription, string title)
	{
		TField_HAIRMOD_PROTECTED tfSeri = new TField_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 120, 140, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2);
		tfSeri.isFocus = true;
		TField_HAIRMOD_PROTECTED tfMaso = new TField_HAIRMOD_PROTECTED(Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 60, 140, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 2);
		tfMaso.setIputType(TField_HAIRMOD_PROTECTED.INPUT_ALPHA_NUMBER_ONLY);
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Nạp");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			gameService.onSendNapSMS(index, decription, tfSeri.getText().Trim(), tfMaso.getText().Trim());
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã gửi thông tin nạp");
		};
		command_HAIRMOD_PROTECTED.action = action;
		PageScr_HAIRMOD_PROTECTED.gI().setInfo(Canvas_HAIRMOD_PROTECTED.hw - 90, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 170, 180, 140, title, command_HAIRMOD_PROTECTED);
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Đóng");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		command_HAIRMOD_PROTECTED2.action = action2;
		PageScr_HAIRMOD_PROTECTED.gI().left = command_HAIRMOD_PROTECTED2;
		Layer_HAIRMOD_PROTECTED layer_HAIRMOD_PROTECTED = new Layer_HAIRMOD_PROTECTED();
		ActionPerform_HAIRMOD_PROTECTED isUpdate = delegate
		{
			tfSeri.update();
			tfMaso.update();
		};
		ActionPaintCmd_HAIRMOD_PROTECTED isPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
		{
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Seri", Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 140, 0);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Mã thẻ", Canvas_HAIRMOD_PROTECTED.hw - 70, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 80, 0);
			tfSeri.paint(g);
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
			tfMaso.paint(g);
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		};
		ActionKey_HAIRMOD_PROTECTED isKeyCode = delegate(int keycode)
		{
			if (tfSeri.isFocus)
			{
				tfSeri.keyPressed(keycode);
			}
			if (tfMaso.isFocus)
			{
				tfMaso.keyPressed(keycode);
			}
		};
		layer_HAIRMOD_PROTECTED.isUpdate = isUpdate;
		layer_HAIRMOD_PROTECTED.isPaint = isPaint;
		layer_HAIRMOD_PROTECTED.isKeyCode = isKeyCode;
		PageScr_HAIRMOD_PROTECTED.gI().layer = layer_HAIRMOD_PROTECTED;
		PageScr_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	public void doNapMoney(string decription)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Nạp lượng");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			popupNapTien(0, decription, "NẠP LƯỢNG");
		};
		command_HAIRMOD_PROTECTED.action = action;
		mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Nạp xu");
		ActionPerform_HAIRMOD_PROTECTED action2 = delegate
		{
			popupNapTien(1, decription, "NẠP XU");
		};
		command_HAIRMOD_PROTECTED2.action = action2;
		mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	private void paintPoint(MyGraphics_HAIRMOD_PROTECTED g, Position_HAIRMOD_PROTECTED pos, int index)
	{
		if (pos != null)
		{
			int num = pos.x / 16;
			int num2 = pos.y / 16;
			if (num - 4 <= cmxMini)
			{
				num = cmxMini + 4;
			}
			else if (num + 6 > cmxMini + wMiniMap)
			{
				num = cmxMini + 60 - 6;
			}
			if (num2 - 4 <= cmyMini)
			{
				num2 = cmyMini + 4;
			}
			else if (num2 + 6 > cmyMini + hMiniMap)
			{
				num2 = cmyMini + 50 - 6;
			}
			g.setColor(colorPaint[index]);
			g.fillRect(num, num2, 3, 3);
			g.setColor(16777215);
			g.drawRect(num - 1, num2 - 1, 4, 4);
		}
	}

	public void paintMiniMap(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!canPaintIsShowMenu())
		{
			return;
		}
		int num = 0;
		if (Tilemap_HAIRMOD_PROTECTED.w < Canvas_HAIRMOD_PROTECTED.w / 16 && Tilemap_HAIRMOD_PROTECTED.h < Canvas_HAIRMOD_PROTECTED.h / 16)
		{
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if ((listMSGServer.size() > 0 && msgWorld.size() == 0) || (listMSGServer.size() == 0 && msgWorld.size() > 0))
		{
			num = 20;
		}
		else if (listMSGServer.size() > 0 && msgWorld.size() > 0)
		{
			num = 40;
		}
		if (posMiniMap.x < Canvas_HAIRMOD_PROTECTED.w - 62)
		{
			g.translate(Canvas_HAIRMOD_PROTECTED.w - 62, 0);
		}
		else
		{
			g.translate(posMiniMap.x, 0);
		}
		int num2 = mainChar.x / 16;
		int num3 = mainChar.y / 16;
		for (int i = 0; i < 3; i++)
		{
			g.setColor(colorMini[i]);
			g.fillRect(i, i, (posMiniMap.x >= Canvas_HAIRMOD_PROTECTED.w - 62) ? (wMiniMap - i * 2) : (60 - i * 2), hMiniMap - i * 2);
		}
		g.setClip(3, 3, ((wMiniMap <= 60) ? wMiniMap : 60) - 5, hMiniMap - 5);
		g.translate(-cmxMini, -cmyMini);
		if (Tilemap_HAIRMOD_PROTECTED.imgMap != null)
		{
			g.drawImage(Tilemap_HAIRMOD_PROTECTED.imgMap, 0, 0, 0);
		}
		g.setColor(16777215);
		g.fillRect(num2, num3 - 2, 5, 5);
		g.setColor(255);
		g.fillRect(num2 + 1, num3 - 1, 3, 3);
		paintPosNPCQuest(g);
		paintPoint(g, posNpcReceiveClan, 1);
		paintPoint(g, posBoss, 2);
		g.setColor(16516117);
		int num4 = Char_HAIRMOD_PROTECTED.party.size();
		for (int j = 0; j < num4; j++)
		{
			PartyInfo_HAIRMOD_PROTECTED partyInfo_HAIRMOD_PROTECTED = (PartyInfo_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.party.elementAt(j);
			if (partyInfo_HAIRMOD_PROTECTED != null)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = findCharByID((short)partyInfo_HAIRMOD_PROTECTED.id);
				if (actor_HAIRMOD_PROTECTED != null)
				{
					g.fillRect(actor_HAIRMOD_PROTECTED.x / 16, actor_HAIRMOD_PROTECTED.y / 16, 2, 2);
				}
			}
		}
		g.setColor(16317005);
		int num5 = Tilemap_HAIRMOD_PROTECTED.pointPop.size();
		for (int k = 0; k < num5; k++)
		{
			PopupName_HAIRMOD_PROTECTED popupName_HAIRMOD_PROTECTED = (PopupName_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.pointPop.elementAt(k);
			int num6 = 0;
			int num7 = 0;
			if (popupName_HAIRMOD_PROTECTED != null)
			{
				if (popupName_HAIRMOD_PROTECTED.x / 16 >= wMiniMap - 3)
				{
					num6 = -3;
				}
				if (popupName_HAIRMOD_PROTECTED.x / 16 <= 3)
				{
					num6 = 3;
				}
				if (popupName_HAIRMOD_PROTECTED.y / 16 <= 3)
				{
					num7 = 3;
				}
				if (popupName_HAIRMOD_PROTECTED.y >= hMiniMap - 3)
				{
					num7 = -3;
				}
				g.fillRect(popupName_HAIRMOD_PROTECTED.x / 16 + num6, popupName_HAIRMOD_PROTECTED.y / 16 + num7, 3, 3);
			}
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, mainChar.x / 16 + "." + mainChar.y / 16, Canvas_HAIRMOD_PROTECTED.w - 5, hMiniMap - 7 + num, 1);
	}

	public bool canPaintGameScr()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == this || Canvas_HAIRMOD_PROTECTED.currentScreen == MenuSelectItem_HAIRMOD_PROTECTED.gI() || Canvas_HAIRMOD_PROTECTED.currentScreen == PageScr_HAIRMOD_PROTECTED.gI() || Canvas_HAIRMOD_PROTECTED.currentScreen == DialLuckyScr_HAIRMOD_PROTECTED.gI())
		{
			return true;
		}
		return false;
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (canPaintGameScr() && isFillScr)
		{
			g.setColor(16777215);
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
			g.setColor(0, 0.5f);
			g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 50, Canvas_HAIRMOD_PROTECTED.hh + 20, 100, 27);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Time: " + Skill_Kham_HAIRMOD_PROTECTED.t, Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.hh + 25, 2);
			return;
		}
		if (lockcmx || lockcmy)
		{
			g.setColor(0);
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		}
		int num = 0;
		if (timerung > 0)
		{
			num = r.nextInt(2);
		}
		g.translate(-cmx + num, -cmy - num);
		g.setColor(0);
		g.fillRect(cmx - 5, cmy - 5, Canvas_HAIRMOD_PROTECTED.w + 10, Canvas_HAIRMOD_PROTECTED.h + 10);
		Tilemap_HAIRMOD_PROTECTED.paintTile(g);
		if (mainChar.posTransRoad != null && posCam != null)
		{
			tg.paint(g);
		}
		if (canPaintGameScr() || canPaintIsShowMenu())
		{
			EffectManager_HAIRMOD_PROTECTED.lowEffects.paintAll(g);
			int num2 = aeo.size();
			for (int i = 0; i < num2; i++)
			{
				((Effect_HAIRMOD_PROTECTED)aeo.elementAt(i))?.paint(g);
			}
		}
		if (canPaintGameScr() || canPaintIsShowMenu())
		{
			int num3 = arrowsDown.size();
			for (int j = 0; j < num3; j++)
			{
				((IArrow_HAIRMOD_PROTECTED)arrowsDown.elementAt(j))?.paint(g);
			}
		}
		int k = 0;
		int num4 = 0;
		int l = 0;
		try
		{
			if (Tilemap_HAIRMOD_PROTECTED.trees != null)
			{
				for (int m = gssy; m < gssye + 5; m++)
				{
					if (paintCay == 1)
					{
						for (int num5 = Tilemap_HAIRMOD_PROTECTED.trees.size(); k < num5 && (Tree_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.trees.elementAt(k) != null; k++)
						{
							Tree_HAIRMOD_PROTECTED tree_HAIRMOD_PROTECTED = (Tree_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.trees.elementAt(k);
							if (tree_HAIRMOD_PROTECTED.y == m)
							{
								if (tree_HAIRMOD_PROTECTED.x - tree_HAIRMOD_PROTECTED.getW() / 32 <= gssxe && tree_HAIRMOD_PROTECTED.x + tree_HAIRMOD_PROTECTED.getW() / 32 >= gssx)
								{
									tree_HAIRMOD_PROTECTED.paint(g);
								}
							}
							else if (tree_HAIRMOD_PROTECTED.y > m)
							{
								break;
							}
						}
					}
					for (; l < npc_limit.size(); l++)
					{
						Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)npc_limit.elementAt(l);
						if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.typeLimit == 1 && canPaintNPC_SV(actor_HAIRMOD_PROTECTED))
						{
							actor_HAIRMOD_PROTECTED.paint(g);
						}
					}
					while (num4 < actors.size())
					{
						Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(num4);
						if (actor_HAIRMOD_PROTECTED2.catagory != 2)
						{
							if (actor_HAIRMOD_PROTECTED2.y >> 4 > m)
							{
								break;
							}
							if (((((actor_HAIRMOD_PROTECTED2.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && paintChar == 1) || actor_HAIRMOD_PROTECTED2.ID == mainChar.ID || actor_HAIRMOD_PROTECTED2.catagory != Actor_HAIRMOD_PROTECTED.CAT_PLAYER) && actor_HAIRMOD_PROTECTED2.y >> 4 == m && actor_HAIRMOD_PROTECTED2.y <= gssye * 16 && actor_HAIRMOD_PROTECTED2.y >= gssy * 16) || actor_HAIRMOD_PROTECTED2.allwayPaint()) && !(actor_HAIRMOD_PROTECTED2 is CongThanh_HAIRMOD_PROTECTED))
							{
								actor_HAIRMOD_PROTECTED2.paint(g);
							}
						}
						else if (actor_HAIRMOD_PROTECTED2.catagory == 2 && actor_HAIRMOD_PROTECTED2.typeLimit != 1 && canPaintNPC_SV(actor_HAIRMOD_PROTECTED2))
						{
							actor_HAIRMOD_PROTECTED2.paint(g);
						}
						num4++;
						if (actor_HAIRMOD_PROTECTED2 != focusedActor || focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND || focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_TREE || focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_PET)
						{
							continue;
						}
						if (focusedActor.catagory == Actor_HAIRMOD_PROTECTED.CAT_NPC)
						{
							if (posNpcRequest != null && actor_HAIRMOD_PROTECTED2.x / 16 == posNpcRequest.x / 16 && actor_HAIRMOD_PROTECTED2.y / 16 == posNpcRequest.y / 16)
							{
								continue;
							}
						}
						else
						{
							bool flag = false;
						}
						if (!isAutoRangeWhenAuto)
						{
							g.drawImage(Res_HAIRMOD_PROTECTED.imgSelect, actor_HAIRMOD_PROTECTED2.x, actor_HAIRMOD_PROTECTED2.y - actor_HAIRMOD_PROTECTED2.height, MyGraphics_HAIRMOD_PROTECTED.HCENTER | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
						}
					}
				}
				Tilemap_HAIRMOD_PROTECTED.paintTileTop(g);
			}
		}
		catch (Exception)
		{
		}
		if (canPaintGameScr() && congThanh != null)
		{
			congThanh.paint(g);
		}
		if (canPaintGameScr())
		{
			int num6 = arrowsUp.size();
			for (int n = 0; n < num6; n++)
			{
				IArrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = (IArrow_HAIRMOD_PROTECTED)arrowsUp.elementAt(n);
				if (arrow_HAIRMOD_PROTECTED != null && canPaintIsShowMenu())
				{
					arrow_HAIRMOD_PROTECTED.paint(g);
				}
			}
		}
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED3 = null;
		if (canPaintGameScr())
		{
			int num7 = actors.size();
			for (int num8 = 0; num8 < num7; num8++)
			{
				actor_HAIRMOD_PROTECTED3 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(num8);
				if (actor_HAIRMOD_PROTECTED3 != null && canPaintIsShowMenu())
				{
					actor_HAIRMOD_PROTECTED3.paintName(g);
				}
			}
		}
		if (canPaintGameScr())
		{
			EffectManager_HAIRMOD_PROTECTED.hiEffects.paintAll(g);
			paintFlyText(g);
		}
		if (canPaintGameScr())
		{
			int num9 = Tilemap_HAIRMOD_PROTECTED.pointPop.size();
			for (int num10 = 0; num10 < num9; num10++)
			{
				PopupName_HAIRMOD_PROTECTED popupName_HAIRMOD_PROTECTED = (PopupName_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.pointPop.elementAt(num10);
				if (popupName_HAIRMOD_PROTECTED != null)
				{
					if (Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
					{
						popupName_HAIRMOD_PROTECTED.name = "thoát";
					}
					else
					{
						popupName_HAIRMOD_PROTECTED.name = "vào";
					}
					popupName_HAIRMOD_PROTECTED.paint(g);
				}
			}
		}
		if (canPaintGameScr() && effAnimate.size() > 0 && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap && effAnimate.size() > 0)
		{
			int num11 = effAnimate.size();
			for (int num12 = 0; num12 < num11; num12++)
			{
				((AnimateEffect_HAIRMOD_PROTECTED)effAnimate.elementAt(num12))?.paint(g);
			}
		}
		if (charcountdonw != null)
		{
			charcountdonw.paint(g, objcountDown.x - 30, objcountDown.y - 40 - objcountDown.getyHorse());
		}
		try
		{
			if (canPaintGameScr())
			{
				paintFocusActorInfo(g);
			}
			paintMainCharInfo(g);
		}
		catch (Exception)
		{
		}
		try
		{
			if (this == Canvas_HAIRMOD_PROTECTED.currentScreen)
			{
				if (isBatMiniMap)
				{
					paintMiniMap(g);
				}
				if (hideGUI == 0)
				{
					if (mainChar.nationID > -1)
					{
						MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, nameCountry[mainChar.inCountry], 4, Canvas_HAIRMOD_PROTECTED.h - 34 - (Main_HAIRMOD_PROTECTED.isPC ? 12 : 0), 0);
					}
					MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, nameMap, 4, Canvas_HAIRMOD_PROTECTED.h - 18 - (Main_HAIRMOD_PROTECTED.isPC ? 12 : 0), 0);
				}
				if (!chatMode && hideGUI == 0 && Canvas_HAIRMOD_PROTECTED.currentDialog == null && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
				{
					paintButton(g);
					if (!Main_HAIRMOD_PROTECTED.isPC)
					{
						paintQuickSlot(g);
					}
					else
					{
						paintQuickSlotPc(g);
					}
					paintIndexBuff(g);
				}
				paintServerInfo(g);
				if (Main_HAIRMOD_PROTECTED.isPC)
				{
					paintSkillClanPc(g);
				}
				else
				{
					paintSkillClan(g);
				}
			}
		}
		catch (Exception)
		{
		}
		if (!canPaintIsShowMenu())
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			g.setColor(3815994, 0.5f);
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		}
		if (Main_HAIRMOD_PROTECTED.isPC && (Canvas_HAIRMOD_PROTECTED.menu.showMenu || (Canvas_HAIRMOD_PROTECTED.currentScreen != this && Main_HAIRMOD_PROTECTED.isPC && Canvas_HAIRMOD_PROTECTED.currentScreen != DialLuckyScr_HAIRMOD_PROTECTED.gI() && Canvas_HAIRMOD_PROTECTED.currentScreen != ItemQsScreen_HAIRMOD_PROTECTED.gI())))
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			g.setColor(3815994, 0.5f);
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		}
		base.paint(g);
		if (canPaintGameScr() && Tilemap_HAIRMOD_PROTECTED.lv == 201 && timeChiemThanh != null)
		{
			paintChiemThanh(g);
		}
		if (this == Canvas_HAIRMOD_PROTECTED.currentScreen)
		{
			paintChat(g);
		}
		if (listMSGServer != null && listMSGServer.size() > 0)
		{
			paintMsgServer(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (MessageScr_HAIRMOD_PROTECTED.nMSG > 0 && imgInfo != null)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgMSG, 3, imgInfo.getHeight(), 0);
		}
		paintTime(g);
		if (msgWorld.size() > 0 && msgWorld.size() > 0)
		{
			MsgInfo_HAIRMOD_PROTECTED msgInfo_HAIRMOD_PROTECTED = (MsgInfo_HAIRMOD_PROTECTED)msgWorld.elementAt(0);
			g.setColor(0, 0.5f);
			if (listMSGServer.size() > 0)
			{
			}
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, msgInfo_HAIRMOD_PROTECTED.arr.Length * 21);
			msgInfo_HAIRMOD_PROTECTED.paint(g);
		}
		if (Main_HAIRMOD_PROTECTED.isDisConnect)
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			g.setColor(0);
			g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
		}
	}

	public void paintTime(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < VecTime.size(); i++)
		{
			((TimecountDown_HAIRMOD_PROTECTED)VecTime.elementAt(i))?.paint(g, Canvas_HAIRMOD_PROTECTED.w - 10, hMiniMap + i * 18 + 16);
		}
	}

	public void paintSkillClanPc(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = 0;
		if (skillClan != null)
		{
			for (int i = 0; i < skillClan.Length; i++)
			{
				if (skillClan[i].time > 0)
				{
					GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(skillClan[i].idIcon + 2600), Canvas_HAIRMOD_PROTECTED.w - 19, Canvas_HAIRMOD_PROTECTED.hh - 83 + 16 * num);
					MFont_HAIRMOD_PROTECTED.smallFontColor[3].drawString(g, skillClan[i].lv + string.Empty, Canvas_HAIRMOD_PROTECTED.w - 19 + 10, Canvas_HAIRMOD_PROTECTED.hh - 85 + 16 * num - 1, 0);
					num++;
				}
			}
		}
		num = 0;
		if (skillClanPrivate == null)
		{
			return;
		}
		for (int j = 0; j < skillClanPrivate.Length; j++)
		{
			if (skillClanPrivate[j].time > 0)
			{
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(skillClanPrivate[j].idIcon + 2600), Canvas_HAIRMOD_PROTECTED.w - 47, Canvas_HAIRMOD_PROTECTED.hh - 83 + 16 * num);
				MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, skillClanPrivate[j].lv + string.Empty, Canvas_HAIRMOD_PROTECTED.w - 37, Canvas_HAIRMOD_PROTECTED.hh - 85 + 16 * num - 1, 0);
				num++;
			}
		}
	}

	private void paintSkillClan(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 0;
		if (skillClan != null)
		{
			int num2 = skillClan.Length;
			for (int i = 0; i < num2; i++)
			{
				if (skillClan[i] != null && skillClan[i].time > 0)
				{
					GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(skillClan[i].idIcon + 2600), Canvas_HAIRMOD_PROTECTED.hw - 25 + 16 * num, Canvas_HAIRMOD_PROTECTED.h - 10);
					MFont_HAIRMOD_PROTECTED.normalFont[4].drawString(g, skillClan[i].lv + string.Empty, Canvas_HAIRMOD_PROTECTED.hw - 25 + 16 * num + 4, Canvas_HAIRMOD_PROTECTED.h - 10, 2);
					num++;
				}
			}
		}
		num = 0;
		if (skillClanPrivate == null)
		{
			return;
		}
		int num3 = skillClanPrivate.Length;
		for (int j = 0; j < num3; j++)
		{
			if (skillClanPrivate[j] != null && skillClanPrivate[j].time > 0)
			{
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(skillClanPrivate[j].idIcon + 2600), Canvas_HAIRMOD_PROTECTED.hw - 25 + 16 * num, Canvas_HAIRMOD_PROTECTED.h - 30);
				MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, skillClanPrivate[j].lv + string.Empty, Canvas_HAIRMOD_PROTECTED.hw - 25 + 16 * num + 4, Canvas_HAIRMOD_PROTECTED.h - 30, 2);
				num++;
			}
		}
	}

	public bool canPaintNPC_SV(Actor_HAIRMOD_PROTECTED npc)
	{
		if (npc.x + npc.width / 2 < cmx || npc.x - npc.width / 2 > cmx + Canvas_HAIRMOD_PROTECTED.w || npc.y - npc.height > cmy + Canvas_HAIRMOD_PROTECTED.h || npc.y < cmy)
		{
			return false;
		}
		return true;
	}

	private void paintIndexBuff(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!Main_HAIRMOD_PROTECTED.isPC && indexBuff != -1 && timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 > 0)
		{
			GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(indexBuff, Canvas_HAIRMOD_PROTECTED.w - 160, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY - 3, 0, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.RIGHT, g);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, (timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis()) / 1000 + string.Empty, Canvas_HAIRMOD_PROTECTED.w - 170, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY - 8, 2);
		}
	}

	public static void setDataForButton()
	{
        xCenTer = 40;
        yCenter = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - 50;
        xU = 40 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getWidth() / 2 - Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth() / 2;
        yU = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - 20 - 8 - 30;
        xD = 40 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getWidth() / 2 - Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth() / 2;
        yD = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - 20 - 8 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight() - 10;
        xL = 20;
        yL = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - 50 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() / 2 - Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight() / 2;
        xR = 40 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getWidth() - Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth() + 18;
        yR = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() - 50 + Res_HAIRMOD_PROTECTED.imgNewBt[0].getHeight() / 2 - Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight() / 2;
        xFire = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth();
        yFire = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2 - 50;
        xMp = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth();
        yMp = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2 - 40 - Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2 + 15;
        xHp = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth() - Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth() + 15;
        yHp = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2 - 40 - Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 4 + 5;
        xThree = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth() - Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth() + 5;
        yThree = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2 - 40 + Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 4;
        xOne = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth() - 20;
        yOne = Canvas_HAIRMOD_PROTECTED.h - Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2 - 40 + Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2 + 10; 
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			xChangeTab = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getWidth() - 5 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getWidth() / 2 + 3;
			yChangeTab = Canvas_HAIRMOD_PROTECTED.hh - 35 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight() / 2 - 5;
			xChangeFocus = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgFocusActor.getWidth() - 5 - Res_HAIRMOD_PROTECTED.imgFocusActor.getWidth() / 2 + 4;
			yChangeFocus = Canvas_HAIRMOD_PROTECTED.hh - 80 - Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight() / 2;
			xChangeClothes = Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgSwapClothes.getWidth() * 2 - 30 - Res_HAIRMOD_PROTECTED.imgSwapClothes.getWidth() / 2 + 3;
			yChangeClothes = Canvas_HAIRMOD_PROTECTED.hh - 50 - Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight() / 2 - 5;
		}
		else
		{
			xChangeTab = 10;
			yChangeTab = Canvas_HAIRMOD_PROTECTED.h - 60 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight();
			xChangeFocus = 10;
			yChangeFocus = Canvas_HAIRMOD_PROTECTED.h - 60 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight() - 5 - Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight() - 20;
			xChangeClothes = 10;
			yChangeClothes = Canvas_HAIRMOD_PROTECTED.h - 60 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight() - 5 - Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight() - Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight() - 40;
			xChatTouch = 10;
			yChatTouch = Canvas_HAIRMOD_PROTECTED.h - 60 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight() - 5 - Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight() - Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight() - Res_HAIRMOD_PROTECTED.imgChatTouch.getHeight() - 60;
			xSelectFc = 10;
			ySelectFc = Canvas_HAIRMOD_PROTECTED.h - 60 - Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight() - 5 - Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight() - Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight() - Res_HAIRMOD_PROTECTED.imgChatTouch.getHeight() - Res_HAIRMOD_PROTECTED.imgSelectFc.getHeight() - 80;
		}
	}

	private void paintButton(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgNewBt[0], xCenTer, yCenter, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			paintFocus(g);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[1], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2, 0, xFire, yFire, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xOne, yOne, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xThree, yThree, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xHp, yHp, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xMp, yMp, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		int num = 0;
		if ((listMSGServer.size() > 0 && msgWorld.size() == 0) || (listMSGServer.size() == 0 && msgWorld.size() > 0))
		{
			num = 20;
		}
		else if (listMSGServer.size() > 0 && msgWorld.size() > 0)
		{
			num = 40;
		}
		if (isBatMiniMap)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgChat, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + 2 + num, MyGraphics_HAIRMOD_PROTECTED.VCENTER | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgMapMini, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 + 2 + num, 3);
		}
		else
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgChat, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 20, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + 2, 3);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgMapMini, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() - 10, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + 2, 3);
		}
		if (!Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgSwapQuickSlot, xChangeTab, yChangeTab, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgFocusActor, xChangeFocus, yChangeFocus, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgSwapClothes, xChangeClothes, yChangeClothes, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		if (Main_HAIRMOD_PROTECTED.isPC && !Tilemap_HAIRMOD_PROTECTED.isOfflineMap)
		{
			g.drawImage(Res_HAIRMOD_PROTECTED.imgChatTouch, xChatTouch, yChatTouch, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			g.drawImage(Res_HAIRMOD_PROTECTED.imgSelectFc, xSelectFc, ySelectFc, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "I", xChangeTab + Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getWidth() + 2, yChangeTab + 1, 0);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "F2", xChangeFocus + Res_HAIRMOD_PROTECTED.imgFocusActor.getWidth() + 2, yChangeFocus + 1, 0);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "U", xChangeClothes + Res_HAIRMOD_PROTECTED.imgFocusActor.getWidth() + 2, yChangeClothes + 1, 0);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "Y", xChatTouch + Res_HAIRMOD_PROTECTED.imgChatTouch.getWidth() + 2, yChatTouch + 1, 0);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, "O", xSelectFc + Res_HAIRMOD_PROTECTED.imgSelectFc.getWidth() + 2, ySelectFc + 1, 0);
		}
	}

	public bool canTouchScreen(int px, int py)
	{
		if (Main_HAIRMOD_PROTECTED.isPC)
		{
			if ((px >= 0 && px <= 50 && py >= 0 && py <= 80) || (px >= Canvas_HAIRMOD_PROTECTED.w - 100 && px <= Canvas_HAIRMOD_PROTECTED.w && py >= 0 && py <= Canvas_HAIRMOD_PROTECTED.hh / 2) || (px < xChatTouch + Res_HAIRMOD_PROTECTED.imgChatTouch.getWidth() && py >= ySelectFc))
			{
				return false;
			}
		}
		else if ((px >= 0 && px <= xR + 30 && py >= yU && py <= Canvas_HAIRMOD_PROTECTED.h) || (px >= 0 && px <= 50 && py >= 0 && py <= 80) || (px >= xThree && px <= Canvas_HAIRMOD_PROTECTED.w && py >= yMp && py <= Canvas_HAIRMOD_PROTECTED.h) || (px >= xChangeClothes && px <= Canvas_HAIRMOD_PROTECTED.w && py >= yChangeFocus && py <= yChangeClothes + Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight()))
		{
			return false;
		}
		return true;
	}

	private void paintFocus(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (!Main_HAIRMOD_PROTECTED.isPC)
		{
			if (isPaintFcBt[0])
			{
				g.drawImage(Res_HAIRMOD_PROTECTED.imgNewBt[4], xU, yU, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
			if (isPaintFcBt[1])
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[4], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight(), 6, xL, yL, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
			if (isPaintFcBt[2])
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[4], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight(), 5, xR, yR, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
			if (isPaintFcBt[3])
			{
				g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[4], 0, 0, Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight(), 3, xD, yD, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
			}
		}
		if (isPaintFcBt[4])
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xOne, yOne, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		if (isPaintFcBt[5])
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xThree, yThree, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		if (isPaintFcBt[6])
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[1], 0, Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2, Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 2, 0, xFire, yFire + 10, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		if (isPaintFcBt[7] || isPaintFcBtMHP[0])
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xHp, yHp, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
		if (isPaintFcBt[8] || isPaintFcBtMHP[1])
		{
			g.drawRegion(Res_HAIRMOD_PROTECTED.imgNewBt[2], 0, Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth(), Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 2, 0, xMp, yMp, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		}
	}

	private void paintMsgServer(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.setColor(0, 0.5f);
		int w = Canvas_HAIRMOD_PROTECTED.w15;
		g.fillRect(0, 0, Canvas_HAIRMOD_PROTECTED.w, 21);
		g.drawImage(Res_HAIRMOD_PROTECTED.imgCmdBar, 0, Canvas_HAIRMOD_PROTECTED.h + 3, MyGraphics_HAIRMOD_PROTECTED.LEFT | MyGraphics_HAIRMOD_PROTECTED.BOTTOM);
		string st = (string)listMSGServer.elementAt(0);
		MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, st, xMsgServer, 1, 0);
		g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.h, Canvas_HAIRMOD_PROTECTED.w);
	}

	private void paintChiemThanh(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = 38;
		int num2 = timeChiemThanh.Length;
		g.setColor(0, 0.5f);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 80, num - 5, 160, 70);
		for (int i = 0; i < num2; i++)
		{
			if (timeChiemThanh[i] - (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - curTimeCT[i]) > 0)
			{
				MFont_HAIRMOD_PROTECTED.arialFontW.drawString(g, i + 1 + ": " + nameChiemThanh[i] + " (" + (timeChiemThanh[i] - (mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 - curTimeCT[i])) + ")", Canvas_HAIRMOD_PROTECTED.hw + 2, num + 1, 3);
				num += 20;
			}
			else if (!nameClan[i].Equals(string.Empty))
			{
				MFont_HAIRMOD_PROTECTED.arialFont.drawString(g, i + 1 + ": " + nameClan[i], Canvas_HAIRMOD_PROTECTED.hw, num, 3);
				num += 20;
			}
		}
	}

	public void paintServerInfo(MyGraphics_HAIRMOD_PROTECTED g)
	{
		int num = serverInfo.size();
		if (num > 0 && tk == null)
		{
			tk = new Ticker((string)serverInfo.elementAt(0), Canvas_HAIRMOD_PROTECTED.w - 50, Canvas_HAIRMOD_PROTECTED.h / 4 + 15 - MFont_HAIRMOD_PROTECTED.borderFont.getHeight() / 2);
			serverInfo.removeElementAt(0);
		}
		if (tk != null)
		{
			tk.paint(g);
		}
	}

	public override bool keyPress(int keyCode)
	{
		if (chatMode)
		{
			tfChat.keyPressed(keyCode);
			if (keyCode == KeyConst_HAIRMOD_PROTECTED.SOFT_RIGHT)
			{
				tfChat.clear();
			}
			if (keyCode == KeyConst_HAIRMOD_PROTECTED.SOFT_LEFT)
			{
				isCloseChat = true;
				Canvas_HAIRMOD_PROTECTED.clearKeyHold();
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
				chatMode = false;
			}
			if (keyCode == KeyConst_HAIRMOD_PROTECTED.FIRE)
			{
				string text = tfChat.getText();
				tfChat.setText(string.Empty);
				doChatFromMe(text);
			}
			return true;
		}
		return false;
	}

	public void doChatFromMe(string text)
	{
		mainChar.chat = null;
		addChat(mainChar, text, 200);
		MessageScr_HAIRMOD_PROTECTED.gI().addTab(mainChar.name + ": " + text, null, 0);
		GameService_HAIRMOD_PROTECTED.gI().chat(text);
	}

	private void paintChat(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if ((hideGUI == 2 && !chatMode) || Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			return;
		}
		if (hideGUI == 0)
		{
			g.translate(-g.getTranslateX(), -g.getTranslateY());
			if (Canvas_HAIRMOD_PROTECTED.currentDialog != null || this != Canvas_HAIRMOD_PROTECTED.currentScreen)
			{
				return;
			}
			if ((focusedActor != null && focusedActor.catagory != 2) || focusedActor == null)
			{
				int num = MFont_HAIRMOD_PROTECTED.borderFont.getWidth("Ðóng") + 10;
				g.setClip(num, Canvas_HAIRMOD_PROTECTED.h - 16, Canvas_HAIRMOD_PROTECTED.w - num - 13, 20);
				if (yMsgChat < toYchat)
				{
					yMsgChat++;
				}
				if (listMSGServer != null && listMSGServer.size() > 0 && Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.receiveQuest && isPaintQuest)
				{
					g.setColor(0, 0.5f);
					g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
					g.fillRect(Canvas_HAIRMOD_PROTECTED.w15 - 10, Canvas_HAIRMOD_PROTECTED.h / 4 + 40 - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() / 2 - 1, Canvas_HAIRMOD_PROTECTED.w - (Canvas_HAIRMOD_PROTECTED.w15 * 2 - 20), 21);
					g.setClip(Canvas_HAIRMOD_PROTECTED.w15, 0, Canvas_HAIRMOD_PROTECTED.w - Canvas_HAIRMOD_PROTECTED.w15 * 2, Canvas_HAIRMOD_PROTECTED.h);
					string text = string.Empty;
					int num2 = chatHistory.size();
					for (int i = 0; i < num2; i++)
					{
						ChatInfo_HAIRMOD_PROTECTED chatInfo_HAIRMOD_PROTECTED = (ChatInfo_HAIRMOD_PROTECTED)chatHistory.elementAt(i);
						if (!chatInfo_HAIRMOD_PROTECTED.name.Equals(string.Empty))
						{
							text = text + chatInfo_HAIRMOD_PROTECTED.name + ": ";
						}
						text += chatInfo_HAIRMOD_PROTECTED.content;
					}
					MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, text, xMsgServer, Canvas_HAIRMOD_PROTECTED.h / 4 + 41 - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() / 2, 0);
					g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
				}
			}
		}
		if (chatMode)
		{
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
			tfChat.paint(g);
			g.setClip(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Ðóng", 5, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY, 0);
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Xóa", Canvas_HAIRMOD_PROTECTED.w - 5, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY, 1);
		}
	}

	private void paintMainCharInfo(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (hideGUI == 2)
		{
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = 0;
		if ((listMSGServer.size() > 0 && msgWorld.size() == 0) || (listMSGServer.size() == 0 && msgWorld.size() > 0))
		{
			num = 20;
		}
		else if (listMSGServer.size() > 0 && msgWorld.size() > 0)
		{
			num = 40;
		}
		g.drawImage(imgInfo, 0, -1, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.LEFT);
		Res_HAIRMOD_PROTECTED.getCharPartInfo(2, mainChar.currentHead).paintStatic(g, 28, 33, 0, mainChar.frame);
		MFont_HAIRMOD_PROTECTED.normalFontColor.drawString(g, "Lv " + mainChar.level + "+" + mainChar.getPercent(), 55, 5, MFont_HAIRMOD_PROTECTED.LEFT);
		if (mainChar.killer > 0)
		{
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, mainChar.killer + string.Empty, 48, imgInfo.getHeight() - 11, MFont_HAIRMOD_PROTECTED.LEFT);
		}
		if (mainChar.delay >= 0)
		{
			long[] time = mainChar.getTime();
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, time[0] + " : " + time[1], 5, imgInfo.getHeight() + 10, MFont_HAIRMOD_PROTECTED.LEFT);
		}
		string goldTime = mainChar.getGoldTime();
		if (!goldTime.Equals(string.Empty))
		{
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, goldTime, 5, imgInfo.getHeight() + 20, MFont_HAIRMOD_PROTECTED.LEFT);
			MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, Char_HAIRMOD_PROTECTED.goldTime, 5, imgInfo.getHeight() + 36, MFont_HAIRMOD_PROTECTED.LEFT);
		}
		int num2 = mainChar.hp * HPBARW / mainChar.maxhp;
		int num3 = mainChar.mp * HPBARW / mainChar.maxmp;
		g.setColor(0);
		g.fillRect(58 + num2, 23, HPBARW - num2 - 1, 5);
		g.setColor(0);
		g.fillRect(53 + num3, 32, HPBARW - num3 - 1, 5);
		if (Canvas_HAIRMOD_PROTECTED.gameTick % 10 > 3)
		{
			if (mainChar.basePointLeft > 0)
			{
				g.drawImage(imgCong[0], Canvas_HAIRMOD_PROTECTED.w - 2, imgInfo.getHeight() + 15, MyGraphics_HAIRMOD_PROTECTED.RIGHT | MyGraphics_HAIRMOD_PROTECTED.TOP);
			}
			if (mainChar.skillPointLeft > 0)
			{
				g.drawImage(imgCong[1], Canvas_HAIRMOD_PROTECTED.w - 2, imgInfo.getHeight() + 26, MyGraphics_HAIRMOD_PROTECTED.RIGHT | MyGraphics_HAIRMOD_PROTECTED.TOP);
			}
		}
	}

	private void paintQuickSlotPc(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (hideGUI == 2 || Canvas_HAIRMOD_PROTECTED.currentDialog != null || chatMode)
		{
			return;
		}
		if (focusedActor != null && focusedActor.catagory == 2)
		{
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, "Giao tiếp", Canvas_HAIRMOD_PROTECTED.hw, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY, MFont_HAIRMOD_PROTECTED.CENTER);
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int num = 15;
		int width = Res_HAIRMOD_PROTECTED.imgPanel.getWidth();
		int height = Res_HAIRMOD_PROTECTED.imgPanel.getHeight();
		g.setColor(colorSlot[mainChar.clazz]);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - width / 2 + 3, Canvas_HAIRMOD_PROTECTED.h - 12 - height + xSlot + num, width - 3, height - 5);
		for (int i = 0; i < 5; i++)
		{
			if (MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i] != null)
			{
				MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, Canvas_HAIRMOD_PROTECTED.hw - width / 2 + i * Res_HAIRMOD_PROTECTED.wKhung + Res_HAIRMOD_PROTECTED.wKhung / 2 - 7, Canvas_HAIRMOD_PROTECTED.h - 16 - 6 + Res_HAIRMOD_PROTECTED.wKhung / 2 - height + xSlot - ((i == 2) ? 1 : 0) + num);
			}
			string[] array = new string[5] { "G", "H", "J", "K", "L" };
			MFont_HAIRMOD_PROTECTED.borderFont.drawString(g, array[i], Canvas_HAIRMOD_PROTECTED.hw - width / 2 + i * Res_HAIRMOD_PROTECTED.wKhung + Res_HAIRMOD_PROTECTED.wKhung / 2 + Res_HAIRMOD_PROTECTED.wKhung / 2 - 14, Canvas_HAIRMOD_PROTECTED.h - 27 - height + xSlot - ((i == 2) ? 2 : 0) + num - 3, 2);
		}
		if (Main_HAIRMOD_PROTECTED.isPC && indexBuff != -1 && timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() / 1000 > 0)
		{
			GameData_HAIRMOD_PROTECTED.imgSkillIcon.drawFrame(indexBuff, Canvas_HAIRMOD_PROTECTED.hw + width / 2 + 23, Canvas_HAIRMOD_PROTECTED.h - 2, 0, MyGraphics_HAIRMOD_PROTECTED.BOTTOM | MyGraphics_HAIRMOD_PROTECTED.LEFT, g);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, (timeBuff - mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis()) / 1000 + string.Empty, Canvas_HAIRMOD_PROTECTED.hw + width / 2 + 43, Canvas_HAIRMOD_PROTECTED.h - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() - 2, 0);
		}
		g.drawImage(Res_HAIRMOD_PROTECTED.imgPanel, Canvas_HAIRMOD_PROTECTED.hw - width / 2, Canvas_HAIRMOD_PROTECTED.h - 16 - height + xSlot + num, 0);
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == this && !chatMode)
		{
			paintSoftIcon(g);
		}
		g.setColor(9043835);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 50, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY + num, mainChar.lvpercent / 10, 1);
		g.setColor(1422592);
		g.fillRect(Canvas_HAIRMOD_PROTECTED.hw - 50, Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.deltaY + num, mainChar.lvpercent / 10, 1);
	}

	private void paintQuickSlot(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (hideGUI == 2 || Canvas_HAIRMOD_PROTECTED.currentDialog != null || chatMode)
		{
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		int width = Res_HAIRMOD_PROTECTED.imgPanel.getWidth();
		int height = Res_HAIRMOD_PROTECTED.imgPanel.getHeight();
		int num = Res_HAIRMOD_PROTECTED.imgNewBt[2].getWidth() / 2;
		int num2 = Res_HAIRMOD_PROTECTED.imgNewBt[2].getHeight() / 4;
		for (int i = 0; i < 5; i++)
		{
			if (MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i] != null)
			{
				switch (i)
				{
				case 0:
					MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, xOne + num, yOne + num2);
					break;
				case 1:
					MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, xThree + num, yThree + num2);
					break;
				case 2:
					MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, xFire + Res_HAIRMOD_PROTECTED.imgNewBt[1].getWidth() / 2 + 1, yFire + Res_HAIRMOD_PROTECTED.imgNewBt[1].getHeight() / 4 + 1);
					break;
				case 3:
					MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, xHp + num, yHp + num2);
					break;
				case 4:
					MainChar_HAIRMOD_PROTECTED.quickSlot[indexTabSlot][i].paint(g, xMp + num, yMp + num2);
					break;
				}
			}
		}
	}

	private void paintSoftIcon(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawImage(Res_HAIRMOD_PROTECTED.imgSoft[0], 2, Canvas_HAIRMOD_PROTECTED.h - 13, 0);
		g.drawImage(Res_HAIRMOD_PROTECTED.imgSoft[1], Canvas_HAIRMOD_PROTECTED.w - 15, Canvas_HAIRMOD_PROTECTED.h - 13, 0);
	}

	private void paintFocusActorInfo(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (hideGUI == 2)
		{
			return;
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (isAutoRangeWhenAuto)
		{
			return;
		}
		int num = 0;
		if ((listMSGServer.size() > 0 && msgWorld.size() == 0) || (listMSGServer.size() == 0 && msgWorld.size() > 0))
		{
			num = 20;
		}
		else if (listMSGServer.size() > 0 && msgWorld.size() > 0)
		{
			num = 40;
		}
		if (focusedActor == null)
		{
			return;
		}
		if (focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND && focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_MY_TREE)
		{
			MFont_HAIRMOD_PROTECTED.normalFontColor.drawString(g, focusedActor.getDisplayName(), Canvas_HAIRMOD_PROTECTED.hw + 60 - 30, num, MFont_HAIRMOD_PROTECTED.RIGHT);
			focusedActor.paintCorner(g, Canvas_HAIRMOD_PROTECTED.hw + 60 - 15, 20 + num);
		}
		if (focusedActor.catagory != 0 && focusedActor.catagory != 1)
		{
			return;
		}
		LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)focusedActor;
		if (liveActor_HAIRMOD_PROTECTED != null && liveActor_HAIRMOD_PROTECTED.maxhp != 0 && liveActor_HAIRMOD_PROTECTED.idBoss == -1)
		{
			Canvas_HAIRMOD_PROTECTED.resetTrans(g);
			g.drawImage(imgHpMonster, Canvas_HAIRMOD_PROTECTED.hw + 60 - 80, 14 + num, 20);
			long num2 = focusedActor.hp;
			int num3 = (int)(num2 * HPBARW_MONSTER / focusedActor.maxhp);
			if (liveActor_HAIRMOD_PROTECTED.hp <= 0)
			{
				num3 = 0;
			}
			g.setColor(2368805);
			g.fillRect(Canvas_HAIRMOD_PROTECTED.hw + 60 - 79, 15 + num, HPBARW_MONSTER - num3, 3);
			MFont_HAIRMOD_PROTECTED.smallFont.drawString(g, ((liveActor_HAIRMOD_PROTECTED.hp <= 0) ? 1 : liveActor_HAIRMOD_PROTECTED.hp) + "/" + liveActor_HAIRMOD_PROTECTED.maxhp, Canvas_HAIRMOD_PROTECTED.hw + 60 - 10 - (HPBARW_MONSTER >> 1), 22 + num, MFont_HAIRMOD_PROTECTED.RIGHT);
			MFont_HAIRMOD_PROTECTED.smallFontColor[0].drawString(g, "lv" + liveActor_HAIRMOD_PROTECTED.level, Canvas_HAIRMOD_PROTECTED.hw + 50, 22 + num, MFont_HAIRMOD_PROTECTED.LEFT);
		}
	}

	public void startSkill_Noel(int x, int y, int yBg, mVector_HAIRMOD_PROTECTED list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(i);
			Skill_Noel_HAIRMOD_PROTECTED ef = new Skill_Noel_HAIRMOD_PROTECTED(x, y, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y, liveActor_HAIRMOD_PROTECTED);
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(ef);
		}
	}

	public void startSkill_BossHalloween(int x, int y, int yBg, mVector_HAIRMOD_PROTECTED list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			LiveActor_HAIRMOD_PROTECTED target = (LiveActor_HAIRMOD_PROTECTED)list.elementAt(i);
			Skill_Boss_Halloween_HAIRMOD_PROTECTED ef = new Skill_Boss_Halloween_HAIRMOD_PROTECTED(x, y, target);
			EffectManager_HAIRMOD_PROTECTED.addLowEffect(ef);
		}
	}

	public void startSkill_Cau_Lua(int xTo, int yTo, int yBg, mVector_HAIRMOD_PROTECTED list)
	{
		Skill_Da_Roi_HAIRMOD_PROTECTED skill_Da_Roi_HAIRMOD_PROTECTED = new Skill_Da_Roi_HAIRMOD_PROTECTED(xTo, yTo, xTo, yBg, 0);
		skill_Da_Roi_HAIRMOD_PROTECTED.type = 1;
		skill_Da_Roi_HAIRMOD_PROTECTED.list = list;
		EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(skill_Da_Roi_HAIRMOD_PROTECTED);
	}

	public void startWeapsMango(LiveActor_HAIRMOD_PROTECTED acFr, LiveActor_HAIRMOD_PROTECTED acTo, int vangle)
	{
		if (acFr != null && acTo != null)
		{
			WeaponsMango_HAIRMOD_PROTECTED o = new WeaponsMango_HAIRMOD_PROTECTED(acFr, acTo, vangle);
			arrowsUp.addElement(o);
		}
	}

	public void startWeapsAngleBoss(int angle, int power, LiveActor_HAIRMOD_PROTECTED acFr, LiveActor_HAIRMOD_PROTECTED acTo, int type, bool isUpDown, int addw, int addh, int xbg, int ybg, bool lastActo)
	{
		if (isUpDown)
		{
			if (acFr != null && acTo != null)
			{
				WeaponsAngleBoss_HAIRMOD_PROTECTED o = new WeaponsAngleBoss_HAIRMOD_PROTECTED(angle, power, acFr, acTo, type, addw, addh, xbg, ybg, lastActo);
				arrowsUp.addElement(o);
			}
		}
		else
		{
			WeaponsAngleBoss_HAIRMOD_PROTECTED o2 = new WeaponsAngleBoss_HAIRMOD_PROTECTED(angle, power, acFr, acTo, type, addw, addh, xbg, ybg, lastActo);
			arrowsDown.addElement(o2);
		}
	}

	public void SkillofBoss_Snake(LiveActor_HAIRMOD_PROTECTED acFr, LiveActor_HAIRMOD_PROTECTED acTo, int w, int h)
	{
		for (int i = 0; i < 12; i++)
		{
			startWeapsAngleBoss(i * 30, 10, acFr, acTo, 0, true, w, h, 0, 0, i == 11);
		}
		timerung = 20;
	}

	public void startWeaponsLazer(LiveActor_HAIRMOD_PROTECTED acFrom, LiveActor_HAIRMOD_PROTECTED acTo, int xadd, int yadd, int dir, int power, int idColor)
	{
		if (acFrom != null && acTo != null)
		{
			WeaponsLazer_HAIRMOD_PROTECTED o = new WeaponsLazer_HAIRMOD_PROTECTED(acFrom, acTo, xadd, yadd, power, idColor);
			if (dir == 1)
			{
				arrowsDown.addElement(o);
			}
			else
			{
				arrowsUp.addElement(o);
			}
		}
	}

	public void startNewMagicBeam(int type, LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, int x, int y, int power, sbyte effect)
	{
		IArrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = new MagicBeam_HAIRMOD_PROTECTED();
		arrow_HAIRMOD_PROTECTED.set(type, x, y, power, effect, from, to);
		arrowsUp.addElement(arrow_HAIRMOD_PROTECTED);
	}

	public void startNewMagicBeam(int type, LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, int x, int y, int power, sbyte effect, int angle)
	{
		IArrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = new MagicBeam_HAIRMOD_PROTECTED();
		arrow_HAIRMOD_PROTECTED.set(type, x, y, power, effect, from, to);
		arrow_HAIRMOD_PROTECTED.setAngle(angle);
		arrowsUp.addElement(arrow_HAIRMOD_PROTECTED);
	}

	public void startNewArrow(int type, LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, int x, int y, int power, sbyte effect, int imgIndex)
	{
		Arrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = new Arrow_HAIRMOD_PROTECTED(imgIndex);
		arrow_HAIRMOD_PROTECTED.set(type, x, y, power, effect, from, to);
		arrowsUp.addElement(arrow_HAIRMOD_PROTECTED);
	}

	public void creatWeaponFire(LiveActor_HAIRMOD_PROTECTED live_From, LiveActor_HAIRMOD_PROTECTED live_To, int type)
	{
		WeaponsFire_HAIRMOD_PROTECTED o = new WeaponsFire_HAIRMOD_PROTECTED(live_From, live_To, type);
		if (live_From.dir != 1)
		{
			arrowsUp.addElement(o);
		}
		else
		{
			arrowsDown.addElement(o);
		}
	}

	public void onActorMove(sbyte actorCatagory, short type, short actorID, short x2, short y2, sbyte pk, int charid, sbyte isnewmonster, sbyte isThanthu, bool canfocus)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		isActorMove = true;
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED == null || actorCatagory != actor_HAIRMOD_PROTECTED.catagory || actor_HAIRMOD_PROTECTED.ID != actorID)
			{
				continue;
			}
			actor_HAIRMOD_PROTECTED.setPosTo(x2, y2);
			actor_HAIRMOD_PROTECTED.canfocus = canfocus;
			if (actor_HAIRMOD_PROTECTED.catagory == 0)
			{
				actor_HAIRMOD_PROTECTED.pk = pk;
				if (actor_HAIRMOD_PROTECTED.getHp() <= 0 || actor_HAIRMOD_PROTECTED.getRealHp() <= 0)
				{
					actor_HAIRMOD_PROTECTED.setState(3);
				}
			}
			isActorMove = false;
			return;
		}
		isActorMove = false;
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = CreateActor(actorCatagory, type, actorID, x2, y2, isnewmonster, isThanthu);
		actor_HAIRMOD_PROTECTED2.canfocus = canfocus;
		if (!Util_HAIRMOD_PROTECTED.inDataRange(mainChar, actor_HAIRMOD_PROTECTED2))
		{
			return;
		}
		actors.addElement(actor_HAIRMOD_PROTECTED2);
		if (actors.size() >= 200)
		{
			return;
		}
		switch (actorCatagory)
		{
		case 0:
			if (isThanthu == -1)
			{
				((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED2).stand = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
				((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED2).pk = pk;
			}
			gameService.requestCharInfo(actorID);
			break;
		case 1:
			((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED2).idChar = charid;
			gameService.requestMonsterInfo(actorID);
			break;
		}
	}

	public void onCharInfo(Char_HAIRMOD_PROTECTED cha)
	{
		if (cha == null)
		{
			return;
		}
		if (cha.idBoss != -1)
		{
			cha.dir = 0;
		}
		if (idChiemThanh != null)
		{
			int num = idChiemThanh.Length;
			for (int i = 0; i < num; i++)
			{
				if (idChiemThanh[i] == cha.ID)
				{
					if (cha != null)
					{
						cha.EffFace = null;
					}
					if (cha != null && timeChiemThanh[i] > 0)
					{
						cha.EffFace = new WayPoint_HAIRMOD_PROTECTED(cha.x, cha.y - 5, 1, 1, false);
					}
				}
			}
		}
		if (cha.defend > 0 || cha.defend_magic > 0)
		{
			int num2 = 0;
			cha.effPhap = new Effect_HAIRMOD_PROTECTED(type: (cha.defend <= cha.defend_magic) ? 36 : 21, x: cha.x, y: cha.y);
		}
	}

	public void onPlayerAttackPlayer(short attacker, short attacked, sbyte skillID, int hpLost, int hpLeft, sbyte effect, sbyte x2, sbyte buffAttack, sbyte level)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		if (attacker == mainChar.ID)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(attacked);
			if (char_HAIRMOD_PROTECTED == null)
			{
				return;
			}
			if (mainChar.attkPower == 0)
			{
				mainChar.attkPower = hpLost;
			}
			if (char_HAIRMOD_PROTECTED != null)
			{
				char_HAIRMOD_PROTECTED.setRealHP(hpLeft);
				if (char_HAIRMOD_PROTECTED.hp <= 0 || char_HAIRMOD_PROTECTED.realHP <= 0)
				{
					char_HAIRMOD_PROTECTED.state = 3;
				}
				AttackResult_HAIRMOD_PROTECTED.startEff(effect, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 25);
				if (buffAttack == 0)
				{
					AttackResult_HAIRMOD_PROTECTED.startEff(effect, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y + 35);
				}
			}
			if (hpLost > 0)
			{
				for (int i = 0; i < x2; i++)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + hpLost / x2, 0, char_HAIRMOD_PROTECTED.x + ((i % 2 != 0) ? (-20) : 0), char_HAIRMOD_PROTECTED.y - ((i % 2 != 0) ? 30 : 20), 1, -2);
				}
			}
			return;
		}
		if (attacked == mainChar.ID)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED2 = (Char_HAIRMOD_PROTECTED)findCharByID(attacker);
			if (char_HAIRMOD_PROTECTED2 != null)
			{
				char_HAIRMOD_PROTECTED2.dir = Util_HAIRMOD_PROTECTED.findDirection(char_HAIRMOD_PROTECTED2, mainChar);
				char_HAIRMOD_PROTECTED2.startAttack(mainChar, new AttackResult_HAIRMOD_PROTECTED(hpLost, effect), skillID, Char_HAIRMOD_PROTECTED.skillLevelLearnt[skillID]);
				if (SkillManager_HAIRMOD_PROTECTED.isSkillAeo(mainChar.clazz, skillID / 10))
				{
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
					mVector_HAIRMOD_PROTECTED2.addElement(mainChar);
					char_HAIRMOD_PROTECTED2.currentSkillAnimate.setActorter(mVector_HAIRMOD_PROTECTED2);
				}
				if (buffAttack == 0)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[3], 0, char_HAIRMOD_PROTECTED2.x, char_HAIRMOD_PROTECTED2.y - 35, 1, -2);
				}
			}
			mainChar.downDuarable();
			mainChar.setRealHP(hpLeft);
			int num = actors.size();
			for (int j = 0; j < num; j++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(j);
				if (actor_HAIRMOD_PROTECTED.ID == attacker)
				{
					break;
				}
			}
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED3 = (Char_HAIRMOD_PROTECTED)findCharByID(attacked);
		if (char_HAIRMOD_PROTECTED3 != null)
		{
			char_HAIRMOD_PROTECTED3.setRealHP(hpLeft);
			if (hpLeft == 0)
			{
				char_HAIRMOD_PROTECTED3.state = 3;
			}
			if (buffAttack == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[3], 0, char_HAIRMOD_PROTECTED3.x, char_HAIRMOD_PROTECTED3.y - 35, 1, -2);
			}
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED4 = (Char_HAIRMOD_PROTECTED)findCharByID(attacker);
		if (char_HAIRMOD_PROTECTED4 != null)
		{
			char_HAIRMOD_PROTECTED4.dir = Util_HAIRMOD_PROTECTED.findDirection(char_HAIRMOD_PROTECTED4, char_HAIRMOD_PROTECTED3);
			char_HAIRMOD_PROTECTED4.startAttack(char_HAIRMOD_PROTECTED3, new AttackResult_HAIRMOD_PROTECTED(hpLost, effect), skillID, level);
		}
	}

	public void onAttackMultiTarget(Message_HAIRMOD_PROTECTED msg)
	{
		short num = msg.reader().readShort();
		sbyte skillID = msg.reader().readByte();
		int num2 = msg.reader().readInt();
		sbyte b = msg.reader().readByte();
		sbyte level = msg.reader().readByte();
		sbyte b2 = msg.reader().readByte();
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(num);
		int num3 = msg.reader().readByte();
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < num3; i++)
		{
			Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED = findMonsterByID(msg.reader().readShort());
			int num4 = msg.reader().readInt();
			if (monster_HAIRMOD_PROTECTED == null)
			{
				continue;
			}
			mVector_HAIRMOD_PROTECTED2.addElement(monster_HAIRMOD_PROTECTED);
			int num5 = monster_HAIRMOD_PROTECTED.hp - num4;
			monster_HAIRMOD_PROTECTED.setRealHP(num4);
			if (num4 <= 0)
			{
				int num6 = 0;
				int num7 = 0;
				if (char_HAIRMOD_PROTECTED != null)
				{
					num6 = (monster_HAIRMOD_PROTECTED.x - char_HAIRMOD_PROTECTED.x) * 2;
					num7 = (monster_HAIRMOD_PROTECTED.y - char_HAIRMOD_PROTECTED.y) * 2;
					while (num6 > 10 || num7 > 10 || num6 < -10 || num7 < -10)
					{
						num6 >>= 1;
						num7 >>= 1;
					}
				}
				monster_HAIRMOD_PROTECTED.startDeadFly(num6, num7);
			}
			AttackResult_HAIRMOD_PROTECTED.startEff(b, monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y - 25);
			if (num2 > 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + num2, 0, monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y - 15, 1, -2);
			}
			if (b2 == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText(AttackResult_HAIRMOD_PROTECTED.EFF_NAME[3], 0, monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y - 35, 1, -2);
			}
		}
		if (mainChar.ID == num)
		{
			if (mainChar.attkPower == 0)
			{
				mainChar.attkPower = num2;
			}
			if (num2 > 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + num2, 0, ((Monster_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(0)).x, ((Monster_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(0)).y - 15, 1, -2);
			}
		}
		else if (char_HAIRMOD_PROTECTED != null && char_HAIRMOD_PROTECTED.currentSkillAnimate != null)
		{
			char_HAIRMOD_PROTECTED.startAttack((Monster_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(0), new AttackResult_HAIRMOD_PROTECTED(num2, b), skillID, level);
			char_HAIRMOD_PROTECTED.currentSkillAnimate.setMonster(mVector_HAIRMOD_PROTECTED2);
		}
	}

	public void onPlayerAttackMonster(short attacker, short attacked, sbyte skillID, int hpLost, int hpLeft, sbyte effect, sbyte x2, sbyte buffAttack, sbyte level)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = findMonsterByID(attacked);
		int num = 0;
		if (liveActor_HAIRMOD_PROTECTED != null)
		{
			num = liveActor_HAIRMOD_PROTECTED.hp - hpLeft;
			liveActor_HAIRMOD_PROTECTED.setRealHP(hpLeft);
		}
		if (mainChar.ID == attacker)
		{
			if (mainChar.attkPower == 0)
			{
				mainChar.attkPower = hpLost;
			}
			AttackResult_HAIRMOD_PROTECTED.startEff(effect, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y - 25);
			if (buffAttack == 0)
			{
				AttackResult_HAIRMOD_PROTECTED.startEff(3, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y + 35);
			}
			if (hpLost > 0)
			{
				for (int i = 0; i < x2; i++)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + hpLost, 0, liveActor_HAIRMOD_PROTECTED.x + ((i % 2 != 0) ? (-20) : 0), liveActor_HAIRMOD_PROTECTED.y - ((i % 2 != 0) ? 30 : 20), 1, -2);
				}
			}
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(attacker);
		if (char_HAIRMOD_PROTECTED != null && liveActor_HAIRMOD_PROTECTED != null)
		{
			char_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(char_HAIRMOD_PROTECTED, liveActor_HAIRMOD_PROTECTED);
			char_HAIRMOD_PROTECTED.startAttack(liveActor_HAIRMOD_PROTECTED, new AttackResult_HAIRMOD_PROTECTED(hpLost, effect), skillID, level);
			if (buffAttack == 0)
			{
				AttackResult_HAIRMOD_PROTECTED.startEff(3, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y + 35);
			}
		}
	}

	public void onDropList(ItemDropInfo_HAIRMOD_PROTECTED[] info)
	{
		try
		{
			if (info == null)
			{
				return;
			}
			int num = info.Length;
			for (int i = 0; i < num; i++)
			{
				Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED = (Dropable_HAIRMOD_PROTECTED)CreateActor(info[i].itemCatagory, info[i].itemTemplateID, info[i].itemID, info[i].x, info[i].y, 0, -1);
				if (dropable_HAIRMOD_PROTECTED != null)
				{
					dropable_HAIRMOD_PROTECTED.startDropFrom(info[i].x, info[i].y, dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y);
					actors.addElement(dropable_HAIRMOD_PROTECTED);
				}
			}
		}
		catch (Exception)
		{
			Debug.Log("LOI TRONG MONSTER DIE 111");
		}
	}

	public void startDeadFly(Monster_HAIRMOD_PROTECTED m, int attacker)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID((short)attacker);
		if (m == null)
		{
			return;
		}
		int num = 0;
		int num2 = 0;
		if (char_HAIRMOD_PROTECTED != null)
		{
			num = (m.x - char_HAIRMOD_PROTECTED.x) * 2;
			num2 = (m.y - char_HAIRMOD_PROTECTED.y) * 2;
			while (num > 10 || num2 > 10 || num < -10 || num2 < -10)
			{
				num >>= 1;
				num2 >>= 1;
			}
		}
		m.startDeadFly(num, num2);
	}

	public void onMonsterDie(MonsterDieInfo_HAIRMOD_PROTECTED mdi)
	{
		if (!readyGetGameLogic || mdi == null)
		{
			return;
		}
		Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED = findMonsterByID(mdi.attacked);
		if (monster_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		startDeadFly(monster_HAIRMOD_PROTECTED, mdi.attacker);
		if (focusedActor == monster_HAIRMOD_PROTECTED)
		{
			focusedActor = null;
		}
		if (mdi.itemDrop != null)
		{
			int num = mdi.itemDrop.Length;
			for (int i = 0; i < num; i++)
			{
				Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED = (Dropable_HAIRMOD_PROTECTED)CreateActor(mdi.itemDrop[i].itemCatagory, mdi.itemDrop[i].itemTemplateID, mdi.itemDrop[i].itemID, mdi.itemDrop[i].x, mdi.itemDrop[i].y, 0, -1);
				if (dropable_HAIRMOD_PROTECTED != null)
				{
					dropable_HAIRMOD_PROTECTED.startDropFrom(monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y, dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y);
					actors.addElement(dropable_HAIRMOD_PROTECTED);
				}
			}
		}
		if (mainChar.ID == mdi.attacker)
		{
			AttackResult_HAIRMOD_PROTECTED.startEff(mdi.effect, monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y - 25);
			if (mdi.hpLost <= 0)
			{
				return;
			}
			if (mainQuest != null && mainQuest.isWorking())
			{
				if (mainQuest.isKillMons())
				{
					mainQuest.killMonster(monster_HAIRMOD_PROTECTED.monster_type, monster_HAIRMOD_PROTECTED.getDisplayName());
				}
				else if (mainQuest.isKillMonsByLv())
				{
					mainQuest.killMonsterByLv(abs(mainChar.level - monster_HAIRMOD_PROTECTED.level));
				}
			}
			if (subQuest != null && subQuest.isWorking())
			{
				if (subQuest.isKillMons())
				{
					subQuest.killMonster(monster_HAIRMOD_PROTECTED.monster_type, monster_HAIRMOD_PROTECTED.getDisplayName());
				}
				else if (subQuest.isKillMonsByLv())
				{
					subQuest.killMonsterByLv(abs(mainChar.level - monster_HAIRMOD_PROTECTED.level));
				}
			}
			if (clanQuest != null && clanQuest.isWorking())
			{
				if (clanQuest.isKillMons())
				{
					clanQuest.killMonster(monster_HAIRMOD_PROTECTED.monster_type, monster_HAIRMOD_PROTECTED.getDisplayName());
				}
				else if (clanQuest.isKillMonsByLv())
				{
					clanQuest.killMonsterByLv(abs(mainChar.level - monster_HAIRMOD_PROTECTED.level));
				}
			}
			if (mainChar.attkPower == 0)
			{
				mainChar.attkPower = mdi.hpLost;
			}
			if (mainChar.kilAllMonsterQuest(monster_HAIRMOD_PROTECTED.monster_type))
			{
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Giết " + mainChar.infoQuest + " " + monster_HAIRMOD_PROTECTED.getDisplayName(), 0));
				if (WindowInfoScr_HAIRMOD_PROTECTED.idMonsterClanQuest != -1)
				{
					if (WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet + 1 >= WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll)
					{
						WindowInfoScr_HAIRMOD_PROTECTED.idMonsterClanQuest = -1;
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Hoàn thành nhiệm vụ", 0));
					}
					else
					{
						string empty = string.Empty;
						short num2 = ++WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet;
						addChat(new ChatInfo_HAIRMOD_PROTECTED(empty, "Giết được " + num2 + "/" + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll, 0));
					}
				}
			}
			if (mdi.hpLost > 0)
			{
				for (int j = 0; j < mdi.x2; j++)
				{
					Canvas_HAIRMOD_PROTECTED.gameScr.startFlyText("-" + mdi.hpLost, 0, monster_HAIRMOD_PROTECTED.x + ((j % 2 != 0) ? (-20) : 0), monster_HAIRMOD_PROTECTED.y - ((j % 2 != 0) ? 30 : 20), 1, -2);
				}
			}
			return;
		}
		mdi.hpLost /= mdi.x2;
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(mdi.attacker);
		if (char_HAIRMOD_PROTECTED != null && monster_HAIRMOD_PROTECTED != null)
		{
			char_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(char_HAIRMOD_PROTECTED, monster_HAIRMOD_PROTECTED);
			if (!SkillManager_HAIRMOD_PROTECTED.isSkillAeo(char_HAIRMOD_PROTECTED.clazz, mdi.skillID / 10))
			{
				char_HAIRMOD_PROTECTED.startAttack(monster_HAIRMOD_PROTECTED, new AttackResult_HAIRMOD_PROTECTED(mdi.hpLost, mdi.effect), mdi.skillID, mdi.level);
			}
		}
	}

	public void startExplosionAt(short x, short y)
	{
		actors.addElement(new Explosion_HAIRMOD_PROTECTED(x, y));
	}

	public Actor_HAIRMOD_PROTECTED findCharByID(short id)
	{
		isFindChar = true;
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == 0 && actor_HAIRMOD_PROTECTED.ID == id)
			{
				isFindChar = false;
				return actor_HAIRMOD_PROTECTED;
			}
		}
		isFindChar = false;
		return null;
	}

	private Monster_HAIRMOD_PROTECTED findMonsterByID(short id)
	{
		isFindMonster = true;
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == 1 && actor_HAIRMOD_PROTECTED.ID == id)
			{
				isFindMonster = false;
				return (Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			}
		}
		isFindMonster = false;
		return null;
	}

	public Actor_HAIRMOD_PROTECTED findActorByID(short id)
	{
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.ID == id)
			{
				return actor_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	private DropItem_HAIRMOD_PROTECTED findItemByID(short id)
	{
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == 3 && actor_HAIRMOD_PROTECTED.ID == id)
			{
				return (DropItem_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	private Dropable_HAIRMOD_PROTECTED findPotionByID(short id, int catagory)
	{
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == catagory && actor_HAIRMOD_PROTECTED.ID == id)
			{
				return (Dropable_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public void onMainCharInfo()
	{
		GameData_HAIRMOD_PROTECTED.gI().getImage();
		mainChar.timeDelayRequestCharInfo = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
		if (mainChar.gender <= 0)
		{
			mainChar.gender = Char_HAIRMOD_PROTECTED.GENDER_OF_CLAZZ[mainChar.clazz];
		}
		if (mainChar.level == 1 && mainChar.lvpercent == 0 && !mainChar.first)
		{
			first = true;
			mainChar.isFirstTimeGetItem = (mainChar.isFirstTimeGetMoney = (mainChar.isFirstTimeGetPotion = true));
		}
		if (mainChar.defend > 0 || mainChar.defend_magic > 0)
		{
			int num = 0;
			num = ((mainChar.defend <= mainChar.defend_magic) ? 36 : 21);
			mainChar.effPhap = new Effect_HAIRMOD_PROTECTED(mainChar.x, mainChar.y, num);
		}
		mainChar.loadQuickSlot();
		PauseMenu_HAIRMOD_PROTECTED.gI().initName();
		MenuWindows_HAIRMOD_PROTECTED.gI().initName();
	}

	public void onMonsterInfo(MonsterInfo_HAIRMOD_PROTECTED monsterInfo)
	{
		if (readyGetGameLogic && monsterInfo != null)
		{
			findMonsterByID(monsterInfo.id)?.setInfo(monsterInfo);
		}
	}

	public void charOutGame(short charID)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == 0 && actor_HAIRMOD_PROTECTED.ID == charID)
			{
				actor_HAIRMOD_PROTECTED.wantDestroy = true;
				break;
			}
		}
	}

	public void onBossAttackPlayer(Message_HAIRMOD_PROTECTED msg)
	{
		short id = msg.reader().readShort();
		sbyte skillID = msg.reader().readByte();
		Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED = findMonsterByID(id);
		if (monster_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		sbyte b = msg.reader().readByte();
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < b; i++)
		{
			short num = msg.reader().readShort();
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)findActorByID(num);
			if (liveActor_HAIRMOD_PROTECTED != null)
			{
				liveActor_HAIRMOD_PROTECTED.damAttack = msg.reader().readInt();
				int hp = msg.reader().readInt();
				if (mainChar.ID == num)
				{
					if (mainChar.comeHome)
					{
						mainChar.state = 0;
						mainChar.comeHome = false;
					}
					mainChar.realHP = (mainChar.hp = hp);
					monster_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(monster_HAIRMOD_PROTECTED, mainChar);
					if (focusedActor == null)
					{
						focusedActor = monster_HAIRMOD_PROTECTED;
					}
					mVector_HAIRMOD_PROTECTED2.addElement(mainChar);
					continue;
				}
				if (monster_HAIRMOD_PROTECTED.monster_type == 90 || monster_HAIRMOD_PROTECTED.monster_type == 82)
				{
				}
				if (liveActor_HAIRMOD_PROTECTED != null)
				{
					liveActor_HAIRMOD_PROTECTED.realHP = (liveActor_HAIRMOD_PROTECTED.hp = hp);
					if (liveActor_HAIRMOD_PROTECTED.hp <= 0)
					{
						((Char_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED).state = 3;
					}
					monster_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(monster_HAIRMOD_PROTECTED, liveActor_HAIRMOD_PROTECTED);
				}
				mVector_HAIRMOD_PROTECTED2.addElement(liveActor_HAIRMOD_PROTECTED);
			}
			else
			{
				int num2 = msg.reader().readInt();
				int num3 = msg.reader().readInt();
			}
		}
		monster_HAIRMOD_PROTECTED.startAttack(mVector_HAIRMOD_PROTECTED2, skillID);
		try
		{
			int idSkill = msg.reader().readUnsignedByte();
			monster_HAIRMOD_PROTECTED.setIdSkill(idSkill);
			int byMap = msg.reader().readUnsignedByte();
			monster_HAIRMOD_PROTECTED.setByMap(byMap);
		}
		catch (Exception)
		{
		}
	}

	public void onMonsterAttackPlayer(short attacker, short attacked, int hpLost, int hpLeft)
	{
		if (!readyGetGameLogic)
		{
			return;
		}
		Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED = findMonsterByID(attacker);
		if (monster_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		if (attacked == 32001)
		{
			monster_HAIRMOD_PROTECTED.setRemoveTarget();
			return;
		}
		if (monster_HAIRMOD_PROTECTED.monster_type != 84)
		{
			monster_HAIRMOD_PROTECTED.startAttack();
		}
		if (mainChar.ID == attacked)
		{
			monster_HAIRMOD_PROTECTED.setTarget(mainChar);
			if (mainChar.comeHome)
			{
				mainChar.state = 0;
				mainChar.comeHome = false;
			}
			mainChar.realHP = (mainChar.hp = hpLeft);
			if (monster_HAIRMOD_PROTECTED.monster_type == 90)
			{
				startFlyText((hpLost == 0) ? "MISS" : ("-" + hpLost), (hpLost != 0) ? 4 : 0, mainChar.x, mainChar.y - 40, 0, -1);
				monster_HAIRMOD_PROTECTED.startAttack(mainChar, hpLost, 8, 4);
				if (mainChar.hp <= 0)
				{
					mainChar.state = 3;
				}
				return;
			}
			monster_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(monster_HAIRMOD_PROTECTED, mainChar);
			if (focusedActor == null)
			{
				focusedActor = monster_HAIRMOD_PROTECTED;
			}
			int num = Util_HAIRMOD_PROTECTED.abs(mainChar.x - monster_HAIRMOD_PROTECTED.x) + Util_HAIRMOD_PROTECTED.abs(mainChar.y - monster_HAIRMOD_PROTECTED.y);
			if (num < 48)
			{
				mainChar.smalljump();
				startFlyText((hpLost == 0) ? "MISS" : ("-" + hpLost), (hpLost != 0) ? 4 : 0, mainChar.x, mainChar.y - 40, 0, -1);
				if (monster_HAIRMOD_PROTECTED.monster_type != 90)
				{
					EffectManager_HAIRMOD_PROTECTED.addHiEffect(mainChar.x, mainChar.y - 10, 9);
				}
				if (mainChar.hp <= 0)
				{
					mainChar.state = 3;
				}
			}
			else if (monster_HAIRMOD_PROTECTED.monster_type != 90)
			{
				startNewMagicBeam(20, monster_HAIRMOD_PROTECTED, mainChar, monster_HAIRMOD_PROTECTED.x, monster_HAIRMOD_PROTECTED.y, hpLost, 0);
			}
			mainChar.downDuarable();
			if (monster_HAIRMOD_PROTECTED.monster_type == 84)
			{
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				mVector_HAIRMOD_PROTECTED2.addElement(mainChar);
				monster_HAIRMOD_PROTECTED.startAttack(mVector_HAIRMOD_PROTECTED2, 0);
			}
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(attacked);
		if (char_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		monster_HAIRMOD_PROTECTED.setTarget(char_HAIRMOD_PROTECTED);
		char_HAIRMOD_PROTECTED.realHP = (char_HAIRMOD_PROTECTED.hp = hpLeft);
		if (char_HAIRMOD_PROTECTED.hp <= 0)
		{
			char_HAIRMOD_PROTECTED.state = 3;
		}
		monster_HAIRMOD_PROTECTED.dir = Util_HAIRMOD_PROTECTED.findDirection(monster_HAIRMOD_PROTECTED, char_HAIRMOD_PROTECTED);
		char_HAIRMOD_PROTECTED.smalljump();
		startFlyText((hpLost == 0) ? "MISS" : ("-" + hpLost), (hpLost != 0) ? 4 : 0, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 40, 0, -1);
		if (monster_HAIRMOD_PROTECTED.monster_type == 90)
		{
			monster_HAIRMOD_PROTECTED.startAttack(char_HAIRMOD_PROTECTED, hpLost, 8, 4);
			return;
		}
		EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(new Effect_HAIRMOD_PROTECTED(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 10, 9));
		if (monster_HAIRMOD_PROTECTED.monster_type == 84)
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED3.addElement(char_HAIRMOD_PROTECTED);
			monster_HAIRMOD_PROTECTED.startAttack(mVector_HAIRMOD_PROTECTED3, 0);
		}
	}

	public void onPingBack()
	{
		pingDelay = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis() - lastTimePing;
		pingNextIn = DELAY_BETWEEN_PING;
	}

	public void onLoginSuccess()
	{
	}

	public void onConnectOK()
	{
		Canvas_HAIRMOD_PROTECTED.isConnectOk = true;
	}

	public void onConnectFail()
	{
		Canvas_HAIRMOD_PROTECTED.isConnectFail = true;
	}

	public void onLoginFail(string reason)
	{
		if (reason.StartsWith("1"))
		{
			if (!Main_HAIRMOD_PROTECTED.isDisConnect)
			{
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
					ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
					Session_ME_HAIRMOD_PROTECTED.gI().close();
				};
				Canvas_HAIRMOD_PROTECTED.startOKDlg(reason.Substring(1), action);
				return;
			}
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("OK");
			ActionPerform_HAIRMOD_PROTECTED action2 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
				Canvas_HAIRMOD_PROTECTED.instance.init();
				Session_ME_HAIRMOD_PROTECTED.gI().close();
				Tilemap_HAIRMOD_PROTECTED.loadMap(0, null);
				ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
			};
			command_HAIRMOD_PROTECTED.action = action2;
			Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo(reason.Substring(1), null, command_HAIRMOD_PROTECTED, null);
			Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
		}
		else if (!Main_HAIRMOD_PROTECTED.isDisConnect)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg(reason);
		}
		else if (Main_HAIRMOD_PROTECTED.isDisConnect)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("OK");
			ActionPerform_HAIRMOD_PROTECTED action3 = delegate
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
				Canvas_HAIRMOD_PROTECTED.instance.init();
				Session_ME_HAIRMOD_PROTECTED.gI().close();
				Tilemap_HAIRMOD_PROTECTED.loadMap(0, null);
				ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
			};
			command_HAIRMOD_PROTECTED2.action = action3;
			Canvas_HAIRMOD_PROTECTED.msgdlg.setInfo(reason, null, command_HAIRMOD_PROTECTED2, null);
			Canvas_HAIRMOD_PROTECTED.currentDialog = Canvas_HAIRMOD_PROTECTED.msgdlg;
		}
	}

	public void onDisconnect()
	{
		autoFight = false;
		Canvas_HAIRMOD_PROTECTED.isBeginAutoLogin = true;
	}

	public bool isOffLineMap(int mapID)
	{
		int num = offMapID.Length;
		for (int i = 0; i < num; i++)
		{
			if (mapID == offMapID[i])
			{
				return true;
			}
		}
		return false;
	}

	public short getIndexArrMap()
	{
		int num = mapID.Length;
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < mapID[i].Length; j++)
			{
				if (map == mapID[i][j])
				{
					return idArrMap[i];
				}
			}
		}
		return -1;
	}

	public short getIndexArrMap(int idm)
	{
		int num = mapID.Length;
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < mapID[i].Length; j++)
			{
				if (idm == mapID[i][j])
				{
					return idArrMap[i];
				}
			}
		}
		return -1;
	}

	public short getMapLoad(int mapid)
	{
		int num = mapID.Length;
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < mapID[i].Length; j++)
			{
				if (mapid == mapID[i][j])
				{
					return mapID[i][0];
				}
			}
		}
		return -1;
	}

	public void onMap(short map, short toX, short toY, short mapLoad, string mapName, sbyte[] sbyteMap)
	{
		btnFocus = 0;
		Canvas_HAIRMOD_PROTECTED.endDlg();
		mainChar.posTransRoad = null;
		mainChar.EffFace = null;
		objcountDown = null;
		focusedActor = null;
		VecTime.removeAllElements();
		charcountdonw = null;
		int num = nearActors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)nearActors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null)
			{
				actor_HAIRMOD_PROTECTED = null;
			}
		}
		nearActors.removeAllElements();
		if (effManager != null)
		{
			int num2 = effManager.size();
			for (int j = 0; j < num2; j++)
			{
				EffectServerManager_HAIRMOD_PROTECTED effectServerManager_HAIRMOD_PROTECTED = (EffectServerManager_HAIRMOD_PROTECTED)effManager.elementAt(j);
				if (effectServerManager_HAIRMOD_PROTECTED != null)
				{
					effectServerManager_HAIRMOD_PROTECTED = null;
				}
			}
			effManager.removeAllElements();
		}
		congThanh = null;
		short num3 = mapLoad;
		if (num3 == -1)
		{
			num3 = map;
		}
		if (toX != -1 && toY != -1)
		{
			mainChar.x = (mainChar.xTo = (short)(toX * 16 + 8));
			mainChar.y = (mainChar.yTo = (short)(toY * 16 + 8));
			if (mainChar.x / 16 <= 1)
			{
				mainChar.x += 32;
			}
			if (mainChar.y / 16 <= 1)
			{
				mainChar.y += 32;
			}
			mainChar.xTo = mainChar.x;
			mainChar.yTo = mainChar.y;
			if (mainChar.myPet != null)
			{
				setInfoCharPet(mainChar.myPet, mainChar);
			}
		}
		int num4 = 0;
		int num5 = actors.size();
		for (int k = 0; k < num5; k++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(k);
			if (actor_HAIRMOD_PROTECTED2 != null && mainChar != null && actor_HAIRMOD_PROTECTED2.ID != mainChar.ID)
			{
				actor_HAIRMOD_PROTECTED2 = null;
			}
		}
		actors.removeAllElements();
		if (mainChar != null)
		{
			actors.addElement(mainChar);
		}
		if (this.map != map)
		{
			num4 = 1;
			this.map = map;
			ActionPerform_HAIRMOD_PROTECTED ia = delegate
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.loadCamera();
				posNpcRequest = null;
				if (current_npc_talk != -1 || idNpcReceive != -1)
				{
					set_npc_request(current_npc_talk);
				}
				Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
				gameService.changeMapOK(-500, 0, 0);
				try
				{
					setAutoTask();
					setObjDynamicType();
					Out_HAIRMOD_PROTECTED.println("auto 1 >> onmap ");
				}
				catch (Exception ex2)
				{
					Out_HAIRMOD_PROTECTED.Log("ERROR ONMAP" + ex2.StackTrace);
				}
			};
			Res_HAIRMOD_PROTECTED.load.loadMap(num3, ia, sbyteMap);
		}
		switch (num3)
		{
		case 107:
			mainChar.delay = 1L;
			mainChar.realTime = 1L;
			mainChar.timeWait2Board = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			break;
		case 10:
			mainChar.delay = -1L;
			mainChar.realTime = -1L;
			if (mainChar.potions[20] > 0)
			{
				mainChar.potions[20]--;
			}
			break;
		}
		if (num3 != 201)
		{
			timeChiemThanh = null;
		}
		readyGetGameLogic = true;
		mainChar.state = 0;
		mainChar.comeHome = false;
		try
		{
			if (Canvas_HAIRMOD_PROTECTED.loginScr != null)
			{
				Canvas_HAIRMOD_PROTECTED.loginScr = null;
				CharSelectScr_HAIRMOD_PROTECTED.me = null;
				Canvas_HAIRMOD_PROTECTED.imgLogo = null;
				ServerListScr_HAIRMOD_PROTECTED.close();
			}
			Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			Canvas_HAIRMOD_PROTECTED.clearKeyHold();
			Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
			Tilemap_HAIRMOD_PROTECTED.setNameMap(num3);
			nameMap = mapName;
			if (num4 == 0)
			{
				Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
				setAutoTask();
				setObjDynamicType();
				Out_HAIRMOD_PROTECTED.println("auto 2 >>> onmap ");
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println("ERROR ONMAP" + ex.StackTrace);
		}
		Resources.UnloadUnusedAssets();
		GC.Collect();
	}

	public void setObjDynamicType()
	{
		int num = objDynamic.size();
		for (int i = 0; i < num; i++)
		{
			DynamicObj_HAIRMOD_PROTECTED dynamicObj_HAIRMOD_PROTECTED = (DynamicObj_HAIRMOD_PROTECTED)objDynamic.elementAt(i);
			if (actors.conTains(dynamicObj_HAIRMOD_PROTECTED))
			{
				continue;
			}
			actors.addElement(dynamicObj_HAIRMOD_PROTECTED);
			for (int j = 0; j < dynamicObj_HAIRMOD_PROTECTED.w / 2; j++)
			{
				int num2 = dynamicObj_HAIRMOD_PROTECTED.y / 16 * Tilemap_HAIRMOD_PROTECTED.w + (dynamicObj_HAIRMOD_PROTECTED.x / 16 - j);
				if (num2 < Tilemap_HAIRMOD_PROTECTED.type.Length)
				{
					Tilemap_HAIRMOD_PROTECTED.type[num2] = 2;
				}
				int num3 = dynamicObj_HAIRMOD_PROTECTED.y / 16 * Tilemap_HAIRMOD_PROTECTED.w + (dynamicObj_HAIRMOD_PROTECTED.x / 16 + j);
				if (num3 < Tilemap_HAIRMOD_PROTECTED.type.Length)
				{
					Tilemap_HAIRMOD_PROTECTED.type[num3] = 2;
				}
				for (int k = 0; k < dynamicObj_HAIRMOD_PROTECTED.h / 2; k++)
				{
					int num4 = (dynamicObj_HAIRMOD_PROTECTED.y / 16 - k) * Tilemap_HAIRMOD_PROTECTED.w + (dynamicObj_HAIRMOD_PROTECTED.x / 16 - j);
					if (num4 < Tilemap_HAIRMOD_PROTECTED.type.Length)
					{
						Tilemap_HAIRMOD_PROTECTED.type[num4] = 2;
					}
					int num5 = (dynamicObj_HAIRMOD_PROTECTED.y / 16 - k) * Tilemap_HAIRMOD_PROTECTED.w + (dynamicObj_HAIRMOD_PROTECTED.x / 16 + j);
					if (num5 < Tilemap_HAIRMOD_PROTECTED.type.Length)
					{
						Tilemap_HAIRMOD_PROTECTED.type[num5] = 2;
					}
				}
			}
		}
		for (int l = 0; l < num; l++)
		{
			DynamicObj_HAIRMOD_PROTECTED dynamicObj_HAIRMOD_PROTECTED2 = (DynamicObj_HAIRMOD_PROTECTED)objDynamic.elementAt(l);
			if (dynamicObj_HAIRMOD_PROTECTED2 != null)
			{
				dynamicObj_HAIRMOD_PROTECTED2 = null;
				objDynamic.removeElementAt(l);
			}
		}
	}

	private void setAutoTask()
	{
		if (Char_HAIRMOD_PROTECTED.skillLevelLearnt[0] != 0 || (Tilemap_HAIRMOD_PROTECTED.lv == 0 && Tilemap_HAIRMOD_PROTECTED.lv == 101) || Tilemap_HAIRMOD_PROTECTED.lv == 105 || iTaskAuto != 0)
		{
			return;
		}
		iTaskAuto = 1;
		ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
		{
			Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			mapFind = new sbyte[Tilemap_HAIRMOD_PROTECTED.w][];
			int num = mapFind.Length;
			for (int i = 0; i < num; i++)
			{
				mapFind[i] = new sbyte[Tilemap_HAIRMOD_PROTECTED.h];
			}
			iTaskAuto = 2;
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		ActionPerform_HAIRMOD_PROTECTED noAction = delegate
		{
			iTaskAuto = 1;
			Canvas_HAIRMOD_PROTECTED.endDlg();
			addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Hãy đi gặp các tiền bối trong làng để học võ công.", 0));
		};
		Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn có muốn hệ thống tự động làm nhiệm vụ cho bạn hay không ?", yesAction, noAction);
	}

	public int getPosNameMap(int idm)
	{
		int num = mapID.Length;
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < mapID[i].Length; j++)
			{
				if (idm == mapID[i][j])
				{
					return j;
				}
			}
		}
		return -1;
	}

	public void listServer(string reason, string[] ip, short[] port, string[] name)
	{
		Canvas_HAIRMOD_PROTECTED.endDlg();
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			Debug.Log("CHUAN BI CAP NHAT ");
			Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			ServerListScr_HAIRMOD_PROTECTED.gI().switchToMe();
			ServerListScr_HAIRMOD_PROTECTED.address = ip;
			ServerListScr_HAIRMOD_PROTECTED.port = port;
			ServerListScr_HAIRMOD_PROTECTED.nameServer = name;
			RMS_HAIRMOD_PROTECTED.saveRMSStringIp(ip, port, name);
			Session_ME_HAIRMOD_PROTECTED.gI().close();
		};
		Canvas_HAIRMOD_PROTECTED.startOKDlg(reason, action);
	}

	public void onCharList(MainChar_HAIRMOD_PROTECTED[] charInfos)
	{
		if (Session_ME_HAIRMOD_PROTECTED.connected)
		{
			Canvas_HAIRMOD_PROTECTED.loginScr.savePass();
			if (!isNewVersionAvailable)
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
			}
			CharSelectScr_HAIRMOD_PROTECTED.gI().setCharList(charInfos);
			CharSelectScr_HAIRMOD_PROTECTED.gI().switchToMe();
		}
	}

	public void onCharWearingInfo(short charID, mVector_HAIRMOD_PROTECTED items, sbyte idFashion, sbyte typePet, string petInfo, AnimalMove_HAIRMOD_PROTECTED myPet, short[] idModel0, sbyte idIconAnimal)
	{
		if (charID == mainChar.ID)
		{
			mainChar.idFashion = idFashion;
			mainChar.setWearingInfo(items);
			mainChar.idAnimal = typePet;
			mainChar.infoAnimal = petInfo;
			mainChar.idModel = idModel0;
			mainChar.idPaintIconAnimal = idIconAnimal;
			if (myPet != null)
			{
				myPet.idSameMyOwnerID = mainChar.ID;
				setCharPet(myPet, mainChar);
			}
			else if (mainChar.myPet != null)
			{
				mainChar.myPet.status = 3;
				mainChar.myPet.timeMagic = 20;
			}
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(charID);
		if (char_HAIRMOD_PROTECTED != null)
		{
			char_HAIRMOD_PROTECTED.idFashion = idFashion;
			char_HAIRMOD_PROTECTED.setWearingInfo(items);
			char_HAIRMOD_PROTECTED.idAnimal = typePet;
			char_HAIRMOD_PROTECTED.infoAnimal = petInfo;
			char_HAIRMOD_PROTECTED.idModel = idModel0;
			char_HAIRMOD_PROTECTED.idPaintIconAnimal = idIconAnimal;
			if (myPet != null)
			{
				myPet.idSameMyOwnerID = char_HAIRMOD_PROTECTED.ID;
				setCharPet(myPet, char_HAIRMOD_PROTECTED);
			}
			else if (char_HAIRMOD_PROTECTED.myPet != null)
			{
				char_HAIRMOD_PROTECTED.myPet.status = 3;
				char_HAIRMOD_PROTECTED.myPet.timeMagic = 20;
			}
		}
	}

	public void setCharPet(AnimalMove_HAIRMOD_PROTECTED pet, Char_HAIRMOD_PROTECTED myOwner0)
	{
		if (pet.type == 5)
		{
			clearCharPet(myOwner0.ID);
			Pet_HAIRMOD_PROTECTED o = new Pet_HAIRMOD_PROTECTED(myOwner0, pet.imgID);
			setInfoCharPet(pet, myOwner0);
			actors.addElement(o);
			return;
		}
		clearCharPet(myOwner0.ID);
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == pet.catagory && !actor_HAIRMOD_PROTECTED.ispetTool())
			{
				AnimalMove_HAIRMOD_PROTECTED animalMove_HAIRMOD_PROTECTED = (AnimalMove_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
				if (animalMove_HAIRMOD_PROTECTED.idSameMyOwnerID == pet.idSameMyOwnerID)
				{
					animalMove_HAIRMOD_PROTECTED.type = pet.type;
					animalMove_HAIRMOD_PROTECTED.imgID = pet.imgID;
					animalMove_HAIRMOD_PROTECTED.sumFrame = pet.sumFrame;
					animalMove_HAIRMOD_PROTECTED.infoPet = pet.infoPet;
					animalMove_HAIRMOD_PROTECTED.totalTime = pet.totalTime;
					animalMove_HAIRMOD_PROTECTED.timeStart = pet.timeStart;
					setInfoCharPet(animalMove_HAIRMOD_PROTECTED, myOwner0);
					return;
				}
			}
		}
		pet.setFrameType4();
		setInfoCharPet(pet, myOwner0);
		actors.addElement(pet);
	}

	public void onPetAttack(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			short id = msg.reader().readShort();
			sbyte b = msg.reader().readByte();
			if (b == 0)
			{
				short idSkill = msg.reader().readShort();
				sbyte b2 = msg.reader().readByte();
				int[] array = new int[b2];
				int[] array2 = new int[b2];
				mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID_Cat(id, Actor_HAIRMOD_PROTECTED.CAT_MY_PET);
				if (actor_HAIRMOD_PROTECTED != null)
				{
					for (int i = 0; i < b2; i++)
					{
						sbyte cat = msg.reader().readByte();
						short id2 = msg.reader().readShort();
						Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID_Cat(id2, cat);
						if (actor_HAIRMOD_PROTECTED2 != null)
						{
							mVector_HAIRMOD_PROTECTED2.addElement(actor_HAIRMOD_PROTECTED2);
						}
						array[i] = msg.reader().readInt();
						array2[i] = msg.reader().readInt();
					}
					actor_HAIRMOD_PROTECTED.petAttack(mVector_HAIRMOD_PROTECTED2, b, idSkill, array, array2);
				}
			}
			if (b == 1)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED3 = Canvas_HAIRMOD_PROTECTED.gameScr.findActorByID(id);
				short id3 = msg.reader().readShort();
				long timelive = msg.reader().readLong();
				actor_HAIRMOD_PROTECTED3?.addEffbuff(id3, actor_HAIRMOD_PROTECTED3.x, actor_HAIRMOD_PROTECTED3.y, timelive);
			}
		}
		catch (Exception)
		{
		}
	}

	public Actor_HAIRMOD_PROTECTED findActorByID_Cat(short id, sbyte cat)
	{
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = null;
		for (int num = actors.size() - 1; num >= 0; num--)
		{
			actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(num);
			if (actor_HAIRMOD_PROTECTED.ID == id && actor_HAIRMOD_PROTECTED.catagory == cat)
			{
				return actor_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public void clearCharPet(short id)
	{
		for (int i = 0; i < actors.size(); i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_PET && actor_HAIRMOD_PROTECTED.ID == id && actor_HAIRMOD_PROTECTED.ispetTool())
			{
				actors.removeElement(actor_HAIRMOD_PROTECTED);
			}
		}
	}

	public void setInfoCharPet(AnimalMove_HAIRMOD_PROTECTED pp, Char_HAIRMOD_PROTECTED myOwner0)
	{
		pp.hImg = 1;
		pp.wImg = 1;
		pp.myOwner = myOwner0;
		myOwner0.myPet = pp;
		pp.x = (pp.xTo = ((myOwner0.dir != Char_HAIRMOD_PROTECTED.LEFT && myOwner0.dir != Char_HAIRMOD_PROTECTED.UP) ? ((short)(myOwner0.x - 20)) : ((short)(myOwner0.x + 20))));
		pp.y = (pp.yTo = myOwner0.y);
		if (pp.type != 1)
		{
			pp.addY = (short)(-myOwner0.height + 15);
		}
		else
		{
			pp.addY = 0;
		}
		pp.v = (sbyte)myOwner0.speed;
		pp.status = 0;
		pp.count = (pp.idFrame = 0);
	}

	public void onViewAnimalInfo(Char_HAIRMOD_PROTECTED ch)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(0, false, new sbyte[1] { 31 });
		WindowInfoScr_HAIRMOD_PROTECTED.charWearing = ch;
		WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Trang bị thú";
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void onAnimalWearingInfo(short charID, mVector_HAIRMOD_PROTECTED items, Image_HAIRMOD_PROTECTED imgAnimal0, sbyte numFrame0, int wimg0, int himg0, sbyte timeChangeFrame0)
	{
		if (charID == mainChar.ID)
		{
			mainChar.imgAnimal = imgAnimal0;
			mainChar.numFrame = numFrame0;
			mainChar.wimg = wimg0;
			mainChar.himg = himg0;
			mainChar.timeChangeFrame = timeChangeFrame0;
			mainChar.setAnimalWearingInfo(items);
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(charID);
		if (char_HAIRMOD_PROTECTED != null)
		{
			char_HAIRMOD_PROTECTED.imgAnimal = imgAnimal0;
			char_HAIRMOD_PROTECTED.numFrame = numFrame0;
			char_HAIRMOD_PROTECTED.wimg = wimg0;
			char_HAIRMOD_PROTECTED.himg = himg0;
			char_HAIRMOD_PROTECTED.timeChangeFrame = timeChangeFrame0;
			char_HAIRMOD_PROTECTED.setAnimalWearingInfo(items);
			onViewAnimalInfo(char_HAIRMOD_PROTECTED);
		}
	}

	public void onCharInventory(long charXu, int[] potions, mVector_HAIRMOD_PROTECTED items, mVector_HAIRMOD_PROTECTED animals, int type)
	{
		mainChar.setInventory(charXu, potions, items, animals, type);
		cancelTrade();
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == WindowInfoScr_HAIRMOD_PROTECTED.gI() && WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 0)
		{
			WindowInfoScr_HAIRMOD_PROTECTED.gI().isLoad = true;
			MenuWindows_HAIRMOD_PROTECTED.gI().isLoad = true;
			PauseMenu_HAIRMOD_PROTECTED.gI().showMe(0);
			WindowInfoScr_HAIRMOD_PROTECTED.gI().isLoad = false;
			MenuWindows_HAIRMOD_PROTECTED.gI().isLoad = false;
		}
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == MenuWindows_HAIRMOD_PROTECTED.gI() && MenuWindows_HAIRMOD_PROTECTED.gI().focusTabChinh == 2)
		{
			MenuWindows_HAIRMOD_PROTECTED.gI().isLoad = true;
			MenuWindows_HAIRMOD_PROTECTED.gI().setCurTab();
			MenuWindows_HAIRMOD_PROTECTED.gI().isLoad = false;
		}
	}

	public static int abs(int i)
	{
		return (i <= 0) ? (-i) : i;
	}

	public void onGetItemFromGround(short whoGet, ItemInInventory_HAIRMOD_PROTECTED cii)
	{
		DropItem_HAIRMOD_PROTECTED dropItem_HAIRMOD_PROTECTED = findItemByID(cii.itemOldID);
		if (dropItem_HAIRMOD_PROTECTED != null)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(whoGet);
			if (char_HAIRMOD_PROTECTED != null)
			{
				if (char_HAIRMOD_PROTECTED == mainChar)
				{
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Nhặt được " + dropItem_HAIRMOD_PROTECTED.getDisplayName(), 1));
				}
				dropItem_HAIRMOD_PROTECTED.startFlyTo(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y);
			}
			dropItem_HAIRMOD_PROTECTED.wantDestroy = true;
		}
		if (whoGet == mainChar.ID)
		{
			Char_HAIRMOD_PROTECTED.inventory.addElement(cii);
			if (mainChar.isFirstTimeGetItem)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn vừa nhặt được một món đồ.", false, 10);
				mainChar.isFirstTimeGetItem = false;
			}
		}
	}

	public void onGetPotionFromGround(short whoGet, short potionID, short potionType, short potionQuantity)
	{
		DropPotion_HAIRMOD_PROTECTED dropPotion_HAIRMOD_PROTECTED = (DropPotion_HAIRMOD_PROTECTED)findPotionByID(potionID, 4);
		if (dropPotion_HAIRMOD_PROTECTED != null)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(whoGet);
			if (char_HAIRMOD_PROTECTED != null)
			{
				if (char_HAIRMOD_PROTECTED == mainChar)
				{
					startFlyText("+" + potionQuantity + string.Empty + dropPotion_HAIRMOD_PROTECTED.getDisplayName2().ToLower(), 2, mainChar.x - 10, mainChar.y - 30, 1, -1);
					if (mainChar.getItemQuest(dropPotion_HAIRMOD_PROTECTED.item_template_id))
					{
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Nhặt " + mainChar.infoQuest + " " + dropPotion_HAIRMOD_PROTECTED.getDisplayName2(), 0));
					}
				}
				dropPotion_HAIRMOD_PROTECTED.startFlyTo(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y);
			}
			else
			{
				dropPotion_HAIRMOD_PROTECTED.wantDestroy = true;
			}
		}
		if (whoGet != mainChar.ID)
		{
			return;
		}
		if (potionType == 0)
		{
			mainChar.charXu += potionQuantity;
		}
		else
		{
			mainChar.potions[potionType] += potionQuantity;
		}
		if (potionType == 0)
		{
			if (mainChar.isFirstTimeGetMoney)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn vừa nhặt được xu Bạn có thể dùng xu để mua HP, MP và đồ dùng khác.", false, 1);
				mainChar.isFirstTimeGetMoney = false;
			}
			return;
		}
		if (idItemClanquest != -1 && idItemClanquest == potionType)
		{
			WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet++;
			if (WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet >= WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll)
			{
				idItemClanquest = -1;
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Hoàn thành nhiệm vụ.", 0));
			}
			else
			{
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Nhặt được: " + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestGet + "/" + WindowInfoScr_HAIRMOD_PROTECTED.numberQuestAll, 0));
			}
		}
		if (mainChar.isFirstTimeGetPotion)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn vừa nhặt được một vật phẩm dùng được. Xin vào mục hành trang để xem.", false, 1);
			mainChar.isFirstTimeGetPotion = false;
		}
	}

	public void onGetGemFromGround(short whoGet3, short gemID, sbyte catagory)
	{
		DropGemItem_HAIRMOD_PROTECTED dropGemItem_HAIRMOD_PROTECTED = (DropGemItem_HAIRMOD_PROTECTED)findPotionByID(gemID, catagory);
		if (dropGemItem_HAIRMOD_PROTECTED != null)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(whoGet3);
			if (char_HAIRMOD_PROTECTED != null)
			{
				dropGemItem_HAIRMOD_PROTECTED.startFlyTo(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y);
			}
			else
			{
				dropGemItem_HAIRMOD_PROTECTED.wantDestroy = true;
			}
		}
	}

	public void onGetItemQuestFromGround(short whoGet3, short gemID, sbyte itemQuestType)
	{
		ItemQuest_HAIRMOD_PROTECTED itemQuest_HAIRMOD_PROTECTED = (ItemQuest_HAIRMOD_PROTECTED)findPotionByID(gemID, 14);
		if (itemQuest_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(whoGet3);
		if (char_HAIRMOD_PROTECTED != null)
		{
			itemQuest_HAIRMOD_PROTECTED.startFlyTo(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y);
		}
		else
		{
			itemQuest_HAIRMOD_PROTECTED.wantDestroy = true;
		}
		if (mainChar.ID == whoGet3)
		{
			if (mainQuest != null && mainQuest.isWorking())
			{
				mainQuest.getItemQuest(itemQuestType);
			}
			if (subQuest != null && subQuest.isWorking())
			{
				subQuest.getItemQuest(itemQuestType);
			}
			if (clanQuest != null && clanQuest.isWorking())
			{
				clanQuest.getItemQuest(itemQuestType);
			}
		}
	}

	public void dellPotionQuest()
	{
		for (int i = 10; i < 14; i++)
		{
			mainChar.potions[i] = 0;
		}
	}

	private void setItemInfo(mVector_HAIRMOD_PROTECTED list, ItemInInventory_HAIRMOD_PROTECTED itemMoreInfo)
	{
		if (list == null || itemMoreInfo == null)
		{
			return;
		}
		ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(list, itemMoreInfo.itemID);
		if (itemByID != null)
		{
			itemByID.isEnoughData = true;
			itemByID.clazz = itemMoreInfo.clazz;
			itemByID.dayUse = itemMoreInfo.dayUse;
			itemByID.property = itemMoreInfo.property;
			itemByID.allAttribute = itemMoreInfo.allAttribute;
			itemByID.itemAttribute = itemMoreInfo.itemAttribute;
			itemByID.durable = itemMoreInfo.durable;
			itemByID.lastTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
			itemByID.islock = itemMoreInfo.islock;
			if (itemMoreInfo.plusTemplate > -1)
			{
				itemByID.plusTemplate = itemMoreInfo.plusTemplate;
			}
		}
	}

	public void onItemInfo(ItemInInventory_HAIRMOD_PROTECTED itemMoreInfo)
	{
		Out_HAIRMOD_PROTECTED.println("onItemInfo");
		if (itemMoreInfo == null)
		{
			return;
		}
		setItemInfo(Char_HAIRMOD_PROTECTED.inventory, itemMoreInfo);
		if (MenuWindows_HAIRMOD_PROTECTED.charWearing != null)
		{
			setItemInfo(MenuWindows_HAIRMOD_PROTECTED.charWearing.wearingItems, itemMoreInfo);
			if ((MenuWindows_HAIRMOD_PROTECTED.charWearing.useHorse != -1 || MenuWindows_HAIRMOD_PROTECTED.charWearing.idhorse > -1) && MenuWindows_HAIRMOD_PROTECTED.charWearing.wearingAnimal != null && MenuWindows_HAIRMOD_PROTECTED.charWearing.wearingAnimal.size() > 0)
			{
				setItemInfo(MenuWindows_HAIRMOD_PROTECTED.charWearing.wearingAnimal, itemMoreInfo);
			}
		}
		if (WindowInfoScr_HAIRMOD_PROTECTED.charWearing != null)
		{
			setItemInfo(WindowInfoScr_HAIRMOD_PROTECTED.charWearing.wearingItems, itemMoreInfo);
			if ((WindowInfoScr_HAIRMOD_PROTECTED.charWearing.useHorse != -1 || WindowInfoScr_HAIRMOD_PROTECTED.charWearing.idhorse > -1) && WindowInfoScr_HAIRMOD_PROTECTED.charWearing.wearingAnimal != null && WindowInfoScr_HAIRMOD_PROTECTED.charWearing.wearingAnimal.size() > 0)
			{
				setItemInfo(WindowInfoScr_HAIRMOD_PROTECTED.charWearing.wearingAnimal, itemMoreInfo);
			}
		}
		setItemInfo(mainChar.tItems, itemMoreInfo);
		setItemInfo(mainChar.rItems, itemMoreInfo);
		setItemInfo(Char_HAIRMOD_PROTECTED.itembag, itemMoreInfo);
		setItemInfo(WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem, itemMoreInfo);
		if (WindowInfoScr_HAIRMOD_PROTECTED.gI().listItemAnimal != null && WindowInfoScr_HAIRMOD_PROTECTED.gI().listItemAnimal.size() > 0)
		{
			setItemInfo(WindowInfoScr_HAIRMOD_PROTECTED.gI().listItemAnimal, itemMoreInfo);
		}
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == WindowInfoScr_HAIRMOD_PROTECTED.gI())
		{
			WindowInfoScr_HAIRMOD_PROTECTED.gI().closeText();
			if (WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 21 || WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 25)
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().doShowItemInfoImbue();
			}
			else if (WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 32)
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().doshowInfoEpdothu();
			}
			else
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().center.perform();
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == MenuWindows_HAIRMOD_PROTECTED.gI())
		{
			MenuWindows_HAIRMOD_PROTECTED.gI().closeText();
			if (MenuWindows_HAIRMOD_PROTECTED.index[MenuWindows_HAIRMOD_PROTECTED.focusTab] != 21 && MenuWindows_HAIRMOD_PROTECTED.index[MenuWindows_HAIRMOD_PROTECTED.focusTab] != 32)
			{
				MenuWindows_HAIRMOD_PROTECTED.gI().center.perform();
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.currentScreen == ScreenDapDo_HAIRMOD_PROTECTED.gI())
		{
			ScreenDapDo_HAIRMOD_PROTECTED.gI().setInfoItem(itemMoreInfo.itemID);
		}
		else if (Canvas_HAIRMOD_PROTECTED.currentScreen == Canvas_HAIRMOD_PROTECTED.shop)
		{
			Canvas_HAIRMOD_PROTECTED.shop.onItemInfo();
		}
	}

	public void onUseItemPK(short charID, int use, int potionType)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(charID);
		if (char_HAIRMOD_PROTECTED != null)
		{
			if (use == 1)
			{
				char_HAIRMOD_PROTECTED.pk = (sbyte)potionType;
			}
			else
			{
				char_HAIRMOD_PROTECTED.pk = 0;
			}
		}
	}

	public void onUsePotion(short userId, sbyte potionType, short valueAdd, int valueNew, int isHP)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(userId);
		if (char_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		if (valueNew <= 0)
		{
			char_HAIRMOD_PROTECTED.hp = 0;
			char_HAIRMOD_PROTECTED.realHP = 0;
			char_HAIRMOD_PROTECTED.state = 3;
			if (userId == mainChar.ID)
			{
			}
			return;
		}
		switch (potionType)
		{
		case 1:
		case 2:
		case 3:
		case 21:
		case 22:
			if (char_HAIRMOD_PROTECTED.hp < valueNew)
			{
				char_HAIRMOD_PROTECTED.hp = (char_HAIRMOD_PROTECTED.realHP = valueNew);
			}
			startFlyText((valueAdd <= 0) ? ("hp" + valueAdd) : "hp+", 4, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 30, 0, -1);
			if (userId == mainChar.ID)
			{
			}
			break;
		case 4:
		case 5:
		case 6:
		case 23:
		case 34:
		case 82:
		case 85:
			if (isHP == 1)
			{
				if (char_HAIRMOD_PROTECTED.hp < valueNew)
				{
					char_HAIRMOD_PROTECTED.hp = (char_HAIRMOD_PROTECTED.realHP = valueNew);
				}
				startFlyText((valueAdd <= 0) ? ("hp" + valueAdd) : "hp+", 4, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 30, 0, -1);
			}
			else
			{
				if (char_HAIRMOD_PROTECTED.mp < valueNew)
				{
					char_HAIRMOD_PROTECTED.mp = valueNew;
				}
				startFlyText((valueAdd <= 0) ? ("mp" + valueAdd) : "mp+", 3, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 40, 0, -1);
			}
			break;
		case 24:
			if (char_HAIRMOD_PROTECTED.mp < valueNew)
			{
				char_HAIRMOD_PROTECTED.mp = valueNew;
			}
			startFlyText((valueAdd <= 0) ? ("mp" + valueAdd) : "mp+", 3, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 40, 0, -1);
			if (isHP == 1)
			{
				char_HAIRMOD_PROTECTED.hp = (char_HAIRMOD_PROTECTED.realHP = valueNew);
				startFlyText((valueAdd <= 0) ? ("hp" + valueAdd) : "hp+", 4, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 30, 0, -1);
			}
			else
			{
				char_HAIRMOD_PROTECTED.mp = valueNew;
				startFlyText((valueAdd <= 0) ? ("mp" + valueAdd) : "mp+", 3, char_HAIRMOD_PROTECTED.x - 10, char_HAIRMOD_PROTECTED.y - 30, 0, -1);
			}
			break;
		}
	}

	private ItemInInventory_HAIRMOD_PROTECTED getItemByID(mVector_HAIRMOD_PROTECTED list, short id)
	{
		int num = list.size();
		for (int i = 0; i < num; i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)list.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED != null && itemInInventory_HAIRMOD_PROTECTED.itemID == id)
			{
				return itemInInventory_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public void onEffData(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			if (msg.reader().readByte() == 0)
			{
				sbyte cat = msg.reader().readByte();
				short id = msg.reader().readShort();
				short id2 = msg.reader().readShort();
				int num = msg.reader().readInt();
				bool canmove = msg.reader().readBoolean();
				bool canpaint = msg.reader().readBoolean();
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = findActorByID_Cat(id, cat);
				long timelive = 0L;
				if (num > 0)
				{
					timelive = num + mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
				}
				sbyte dyhorse = 0;
				try
				{
					dyhorse = msg.reader().readByte();
				}
				catch (Exception)
				{
				}
				actor_HAIRMOD_PROTECTED?.addEffectSkillTime(id2, actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y, timelive, canmove, canpaint, true, 0, (sbyte)Res_HAIRMOD_PROTECTED.random(1, 8), dyhorse);
			}
		}
		catch (Exception)
		{
		}
	}

	public void onPutItem2Bag(short itemID)
	{
		ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(Char_HAIRMOD_PROTECTED.inventory, itemID);
		if (itemByID != null)
		{
			Char_HAIRMOD_PROTECTED.inventory.removeElement(itemByID);
			if (mainChar.isExistInvector(Char_HAIRMOD_PROTECTED.itembag, itemByID) == -1)
			{
				Char_HAIRMOD_PROTECTED.itembag.addElement(itemByID);
			}
		}
		else if (WindowInfoScr_HAIRMOD_PROTECTED.gI() == Canvas_HAIRMOD_PROTECTED.currentScreen && WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 22)
		{
			WindowInfoScr_HAIRMOD_PROTECTED.gI().setSizeKeepItem(0);
		}
	}

	public void onGetItemFromBag(short itemID)
	{
		ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(Char_HAIRMOD_PROTECTED.itembag, itemID);
		if (itemByID != null)
		{
			Char_HAIRMOD_PROTECTED.itembag.removeElement(itemByID);
			if (mainChar.isExistInvector(Char_HAIRMOD_PROTECTED.inventory, itemByID) == -1)
			{
				Char_HAIRMOD_PROTECTED.inventory.addElement(itemByID);
			}
		}
	}

	public void onNPCKeepItem(mVector_HAIRMOD_PROTECTED itemBag)
	{
		Char_HAIRMOD_PROTECTED.itembag = itemBag;
		showWindow(0, true, new sbyte[1] { 22 });
	}

	public void onNPCSellPotion(sbyte[] potionsSell)
	{
		shop.removeAllElements();
		int num = potionsSell.Length;
		for (int i = 0; i < num; i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
			if (itemInInventory_HAIRMOD_PROTECTED != null)
			{
				itemInInventory_HAIRMOD_PROTECTED.catagory = 4;
				if (i < potionsSell.Length)
				{
					itemInInventory_HAIRMOD_PROTECTED.potionType = potionsSell[i];
					itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
					itemInInventory_HAIRMOD_PROTECTED.isShopItem = true;
					shop.addElement(itemInInventory_HAIRMOD_PROTECTED);
				}
			}
		}
		showWindow(1, true, new sbyte[2] { 0, 19 });
	}

	public void onShopSpecial(mVector_HAIRMOD_PROTECTED v)
	{
		shopSpecial = Res_HAIRMOD_PROTECTED.shopTemplate;
		shopSpecial.removeAllElements();
		if (Res_HAIRMOD_PROTECTED.shopTemplate == null)
		{
			return;
		}
		int num = Res_HAIRMOD_PROTECTED.shopTemplate.size();
		for (int i = 0; i < num; i++)
		{
			GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.shopTemplate.elementAt(i);
			if (gemTemp_HAIRMOD_PROTECTED != null && gemTemp_HAIRMOD_PROTECTED.isSell)
			{
				shopSpecial.addElement(gemTemp_HAIRMOD_PROTECTED);
			}
		}
	}

	public void onNPCSellGemItem(mVector_HAIRMOD_PROTECTED gem)
	{
		shop.removeAllElements();
		int num = Res_HAIRMOD_PROTECTED.gemTemplate.size();
		for (int i = 0; i < num; i++)
		{
			GemTemp_HAIRMOD_PROTECTED gemTemp_HAIRMOD_PROTECTED = (GemTemp_HAIRMOD_PROTECTED)Res_HAIRMOD_PROTECTED.gemTemplate.elementAt(i);
			if (gemTemp_HAIRMOD_PROTECTED != null && gemTemp_HAIRMOD_PROTECTED.isSell)
			{
				shop.addElement(gemTemp_HAIRMOD_PROTECTED);
			}
		}
		showWindow(1, true, new sbyte[2] { 0, 10 });
	}

	public void onNPCSellItem(mVector_HAIRMOD_PROTECTED itemsSell)
	{
		shop = itemsSell;
		ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)itemsSell.elementAt(0);
		if (itemInInventory_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
		if (item != null)
		{
			if ((item.type > 2 && item.type < 8) || (item.type >= 14 && item.type <= 18))
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(1, true, new sbyte[2] { 0, 9 });
			}
			else
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(0, true, new sbyte[9] { 11, 12, 13, 14, 15, 16, 17, 18, 0 });
			}
			WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
		}
	}

	public void onChat(short chatFromID, string text)
	{
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = findActorByID(chatFromID);
		if (actor_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		actor_HAIRMOD_PROTECTED.chat = null;
		if (actor_HAIRMOD_PROTECTED is Char_HAIRMOD_PROTECTED)
		{
			if (((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).idBoss != -1)
			{
				addChat(actor_HAIRMOD_PROTECTED, text, 500);
				return;
			}
			addChat(actor_HAIRMOD_PROTECTED, text, 50);
			MessageScr_HAIRMOD_PROTECTED.gI().addTab(((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).name + ": " + text, null, 0);
		}
		else
		{
			addChat(actor_HAIRMOD_PROTECTED, text, 50);
		}
	}

	public void onSellItem(short itemID)
	{
		ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(Char_HAIRMOD_PROTECTED.inventory, itemID);
		if (itemByID != null)
		{
			Char_HAIRMOD_PROTECTED.inventory.removeElement(itemByID);
			ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemByID.charClazz, itemByID.idTemplate);
			mainChar.charXu += item.price / 5;
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Ðã bán thành công.", false);
		}
	}

	public void onSellGemItem(short itemID)
	{
		GemTemplate_HAIRMOD_PROTECTED gemByRealID = GemTemplate_HAIRMOD_PROTECTED.getGemByRealID(itemID);
		if (gemByRealID != null)
		{
			gemByRealID.number--;
			if (gemByRealID.number <= 0)
			{
				MainChar_HAIRMOD_PROTECTED.gemItem.removeElement(gemByRealID);
				GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemByRealID.id);
				mainChar.charXu += gemByID.price / 5;
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Ðã bán thành công.");
			}
		}
	}

	public void onGiveItemToGround(short charID, short itemID, ItemDropInfo_HAIRMOD_PROTECTED mdi)
	{
		ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(Char_HAIRMOD_PROTECTED.inventory, itemID);
		if (itemByID != null)
		{
			Char_HAIRMOD_PROTECTED.inventory.removeElement(itemByID);
			Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED = (Dropable_HAIRMOD_PROTECTED)CreateActor(mdi.itemCatagory, mdi.itemTemplateID, mdi.itemID, mdi.x, mdi.y, 0, -1);
			dropable_HAIRMOD_PROTECTED.startDropFrom(mainChar.x, mainChar.y, dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y);
			actors.addElement(dropable_HAIRMOD_PROTECTED);
			if (mainChar.ID == charID)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã bỏ vật phẩm ra đất.");
			}
		}
		else
		{
			Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED2 = (Dropable_HAIRMOD_PROTECTED)CreateActor(mdi.itemCatagory, mdi.itemTemplateID, mdi.itemID, mdi.x, mdi.y, 0, -1);
			if (dropable_HAIRMOD_PROTECTED2 != null)
			{
				dropable_HAIRMOD_PROTECTED2.startDropFrom(mainChar.x, mainChar.y, dropable_HAIRMOD_PROTECTED2.x, dropable_HAIRMOD_PROTECTED2.y);
				actors.addElement(dropable_HAIRMOD_PROTECTED2);
			}
		}
	}

	public void onSetXP(short whoSetXP, short newpercent, int dxp)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(whoSetXP);
		if (char_HAIRMOD_PROTECTED != null)
		{
			char_HAIRMOD_PROTECTED.lvpercent = newpercent;
			startFlyText("+" + dxp + "xp", 2, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, -1, -1);
		}
	}

	public void onCharProperties(short id, Property_HAIRMOD_PROTECTED[] ps)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(id);
		if (char_HAIRMOD_PROTECTED != null)
		{
			int num = ps.Length;
			for (int i = 0; i < num; i++)
			{
				char_HAIRMOD_PROTECTED.setProperty(ps[i]);
			}
		}
	}

	public void onLevelUp(short idc, sbyte newlevel, int maxHP, int maxMP)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(idc);
		if (char_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		char_HAIRMOD_PROTECTED.level = newlevel;
		char_HAIRMOD_PROTECTED.lockUpdate = 30;
		char_HAIRMOD_PROTECTED.dir = 0;
		char_HAIRMOD_PROTECTED.maxhp = maxHP;
		char_HAIRMOD_PROTECTED.maxmp = maxMP;
		char_HAIRMOD_PROTECTED.hp = maxHP;
		char_HAIRMOD_PROTECTED.mp = maxMP;
		char_HAIRMOD_PROTECTED.realHP = char_HAIRMOD_PROTECTED.hp;
		startFlyText("level-up", 3, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, -1, 1);
		startFlyText("level-up", 3, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, 1, -1);
		startFlyText("level-up", 3, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, -1, -1);
		startFlyText("level-up", 3, char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, 1, 1);
		if (char_HAIRMOD_PROTECTED != null)
		{
			EffectManager_HAIRMOD_PROTECTED.addHiEffect(char_HAIRMOD_PROTECTED.x, char_HAIRMOD_PROTECTED.y - 30, 18);
		}
		if (char_HAIRMOD_PROTECTED == mainChar)
		{
			addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Đạt được level " + newlevel, 1));
			if (newlevel == 2)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chúc mừng! Bạn vừa lên cấp. Chọn menu, nhân vật để tăng điểm tiềm năng và kỹ năng", false, 20);
			}
		}
	}

	public void onRemoveActor(short actorID, sbyte cat)
	{
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == cat && actor_HAIRMOD_PROTECTED.ID == actorID)
			{
				actors.removeElement(actor_HAIRMOD_PROTECTED);
				break;
			}
		}
	}

	public void onCreadtePTOK(short idParty)
	{
		mainChar.IDParty = idParty;
		mainChar.IDMasterParty = mainChar.ID;
		if (tempIDActorParty != -1)
		{
			gameService.invite2Party(tempIDActorParty);
			tempIDActorParty = -1;
		}
	}

	public void onAdd2Party(Message_HAIRMOD_PROTECTED m)
	{
		try
		{
			short idInvite;
			string charName;
			int charLV;
			switch (m.reader().readByte())
			{
			case 0:
			{
				short partyID = m.reader().readShort();
				idInvite = m.reader().readShort();
				charName = m.reader().readUTF();
				charLV = m.reader().readByte();
				int num = m.reader().readShort();
				Message_HAIRMOD_PROTECTED msg = new Message_HAIRMOD_PROTECTED((sbyte)49);
				if (autoFight && Canvas_HAIRMOD_PROTECTED.autoTab.isAutoParty && !onoffInvite)
				{
					try
					{
						msg.writer().writeByte(1);
						msg.writer().writeShort(idInvite);
						gameService.session.sendMessage(msg);
						msg.cleanup();
						PartyInfo_HAIRMOD_PROTECTED info2 = new PartyInfo_HAIRMOD_PROTECTED(idInvite, charName, charLV, m.reader().readByte());
						mainChar.add2Party(info2);
						mainChar.IDMasterParty = idInvite;
						mainChar.IDParty = partyID;
						while (m.reader().available() > 0)
						{
							PartyInfo_HAIRMOD_PROTECTED info3 = new PartyInfo_HAIRMOD_PROTECTED(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
							mainChar.add2Party(info3);
						}
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Đã tham gia nhóm", 0));
					}
					catch (Exception)
					{
						Out_HAIRMOD_PROTECTED.println(" loi tai onaddpart2");
					}
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
					return;
				}
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					try
					{
						msg.writer().writeByte(1);
						msg.writer().writeShort(idInvite);
						gameService.session.sendMessage(msg);
						msg.cleanup();
						PartyInfo_HAIRMOD_PROTECTED info4 = new PartyInfo_HAIRMOD_PROTECTED(idInvite, charName, charLV, m.reader().readByte());
						mainChar.add2Party(info4);
						mainChar.IDMasterParty = idInvite;
						mainChar.IDParty = partyID;
						while (m.reader().available() > 0)
						{
							PartyInfo_HAIRMOD_PROTECTED info5 = new PartyInfo_HAIRMOD_PROTECTED(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
							mainChar.add2Party(info5);
						}
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Đã tham gia nhóm", 0));
					}
					catch (Exception)
					{
						Out_HAIRMOD_PROTECTED.println("LOI KHI GUI OK VAO PARTY LEN SERVER");
					}
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				ActionPerform_HAIRMOD_PROTECTED noAction = delegate
				{
					try
					{
						msg.writer().writeByte(2);
						msg.writer().writeShort(idInvite);
						gameService.session.sendMessage(msg);
						msg.cleanup();
					}
					catch (Exception)
					{
					}
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg(charName + " mời bạn tham gia nhóm.", yesAction, noAction);
				return;
			}
			case 1:
			{
				idInvite = m.reader().readShort();
				charName = m.reader().readUTF();
				charLV = m.reader().readByte();
				if (Char_HAIRMOD_PROTECTED.party.size() <= 0)
				{
					mainChar.IDMasterParty = mainChar.ID;
				}
				PartyInfo_HAIRMOD_PROTECTED info = new PartyInfo_HAIRMOD_PROTECTED(idInvite, charName, charLV, m.reader().readByte());
				mainChar.add2Party(info);
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, charName + " đã tham gia nhóm.", 0));
				return;
			}
			case 2:
				Canvas_HAIRMOD_PROTECTED.startOKDlg(m.reader().readUTF() + " từ chối vào nhóm.");
				return;
			case 3:
				break;
			default:
				return;
			}
			while (m.reader().available() > 0)
			{
				PartyInfo_HAIRMOD_PROTECTED partyInfo_HAIRMOD_PROTECTED = new PartyInfo_HAIRMOD_PROTECTED(m.reader().readShort(), m.reader().readUTF(), m.reader().readByte(), m.reader().readByte());
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, partyInfo_HAIRMOD_PROTECTED.name + " đã vào nhóm.", 0));
				mainChar.add2Party(partyInfo_HAIRMOD_PROTECTED);
			}
		}
		catch (Exception)
		{
		}
	}

	public void onKickOutParty(Message_HAIRMOD_PROTECTED m)
	{
		sbyte b = m.reader().readByte();
		string empty = string.Empty;
		if (b == 0 || b == 2)
		{
			short num = m.reader().readShort();
			if (num == mainChar.ID)
			{
				mainChar.IDParty = -1;
				mainChar.IDMasterParty = -1;
				Char_HAIRMOD_PROTECTED.party.removeAllElements();
				if (b == 0)
				{
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Bị đuổi khỏi nhóm", 0));
				}
				else
				{
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Rời khỏi nhóm", 0));
				}
				return;
			}
			empty = mainChar.removeUserParty(num);
			if (!empty.Equals(string.Empty))
			{
				if (b == 0)
				{
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, empty + " b? du?i kh?i nhóm", 0));
				}
				else
				{
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, empty + " rời khỏi nhóm", 0));
				}
			}
			if (Char_HAIRMOD_PROTECTED.party.size() <= 0)
			{
				Char_HAIRMOD_PROTECTED.party.removeAllElements();
				mainChar.IDParty = -1;
				mainChar.IDMasterParty = -1;
				addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Nhóm đã bị giải tán", 0));
			}
		}
		else if (b == 1)
		{
			mainChar.IDParty = -1;
			mainChar.IDMasterParty = -1;
			Char_HAIRMOD_PROTECTED.party.removeAllElements();
			addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Nhóm đã bị giải tán", 0));
		}
	}

	public void onCharUseBuff(Message_HAIRMOD_PROTECTED m)
	{
		int num = m.reader().readByte();
		int num2 = m.reader().readShort();
		if (num == 2)
		{
			int num3 = actors.size();
			for (int i = 0; i < num3; i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
				if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == 0 && actor_HAIRMOD_PROTECTED.ID == num2)
				{
					for (int j = 0; j < ((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).buffType.Length; j++)
					{
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).buffType[j] = m.reader().readByte();
					}
					for (int k = 0; k < ((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).buffType.Length; k++)
					{
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).countDown[k] = m.reader().readShort();
					}
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).setBuff(((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).buffType, ((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).countDown);
					for (int l = 0; l < ((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).lvBuff.Length; l++)
					{
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).lvBuff[l] = m.reader().readByte();
					}
					break;
				}
			}
			return;
		}
		int num4 = m.reader().readByte();
		int timeLive = m.reader().readShort();
		switch (num)
		{
		case 1:
		{
			int num6 = m.reader().readByte();
			int num7;
			for (num7 = 0; num7 < Char_HAIRMOD_PROTECTED.BUFFTYPE.Length && Char_HAIRMOD_PROTECTED.BUFFTYPE[num7] != num4; num7++)
			{
			}
			if (num2 == mainChar.ID)
			{
				if (!mainChar.isExistBuff(num4))
				{
					CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = new CharBuff_HAIRMOD_PROTECTED(mainChar.x, mainChar.y, num4);
					charBuff_HAIRMOD_PROTECTED.setTimeLive(timeLive);
					timeBuff = charBuff_HAIRMOD_PROTECTED.tick;
					isBuffAuto = true;
					mainChar.lvBuff[num7] = (sbyte)num6;
					mainChar.buffType[num7] = (sbyte)num4;
					mainChar.buff.addElement(charBuff_HAIRMOD_PROTECTED);
				}
				break;
			}
			int num8 = actors.size();
			for (int num9 = 0; num9 < num8; num9++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED3 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(num9);
				if (actor_HAIRMOD_PROTECTED3.catagory == 0 && actor_HAIRMOD_PROTECTED3.ID == num2)
				{
					if (!((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED3).isExistBuff(num4))
					{
						CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED2 = new CharBuff_HAIRMOD_PROTECTED(actor_HAIRMOD_PROTECTED3.x, actor_HAIRMOD_PROTECTED3.y, num4);
						charBuff_HAIRMOD_PROTECTED2.setTimeLive(timeLive);
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED3).lvBuff[num7] = (sbyte)num6;
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED3).buffType[num7] = (sbyte)num4;
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED3).buff.addElement(charBuff_HAIRMOD_PROTECTED2);
					}
					break;
				}
			}
			break;
		}
		case 0:
		{
			int num5 = actors.size();
			for (int n = 0; n < num5; n++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(n);
				if (actor_HAIRMOD_PROTECTED2 != null && actor_HAIRMOD_PROTECTED2.catagory == 0 && actor_HAIRMOD_PROTECTED2.ID == num2)
				{
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED2).removeBuff(num4);
					break;
				}
			}
			break;
		}
		}
	}

	public void onInfoQuestLogin(Message_HAIRMOD_PROTECTED m)
	{
	}

	public void onInfoGiftQuest(string str)
	{
		addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, str, 0));
	}

	public void isreceiveQuest(Message_HAIRMOD_PROTECTED m)
	{
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void onFinishQuest(Message_HAIRMOD_PROTECTED m)
	{
	}

	public void onNewHPMP(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			int num = msg.reader().readShort();
			int maxhp = msg.reader().readInt();
			int maxmp = msg.reader().readInt();
			short defend = msg.reader().readShort();
			int num2 = msg.reader().readInt();
			if (num == mainChar.ID)
			{
				mainChar.maxhp = maxhp;
				mainChar.maxmp = maxmp;
				mainChar.defend = defend;
				mainChar.hp = num2;
				mainChar.realHP = num2;
				if (mainChar.state == 3 && mainChar.hp > 0)
				{
					mainChar.state = 0;
					addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, "Đã được hồi sinh.", 0));
				}
				return;
			}
			int num3 = actors.size();
			for (int i = 0; i < num3; i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
				if (actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.ID == num)
				{
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).hp = num2;
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).realHP = num2;
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).maxhp = maxhp;
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).maxmp = maxmp;
					((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).defend = defend;
					if (((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).state == 3 && mainChar.hp > 0)
					{
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).state = 0;
						addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, ((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).name + " đã được hồi sinh.", 0));
					}
					break;
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void onKiller(int idChar, int check, short pkPoint)
	{
		if (mainChar.ID == idChar)
		{
			mainChar.iskiller = check == 1;
			mainChar.killer = pkPoint;
			return;
		}
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.ID == idChar)
			{
				((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).iskiller = check == 1;
				actor_HAIRMOD_PROTECTED.killer = pkPoint;
				break;
			}
		}
	}

	public void onRequestTrade(short userid)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(userid);
		if (char_HAIRMOD_PROTECTED != null)
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				gameService.sendOk_notOkInviteTrafe(userid, 1);
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			ActionPerform_HAIRMOD_PROTECTED noAction = delegate
			{
				gameService.sendOk_notOkInviteTrafe(userid, -1);
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg(char_HAIRMOD_PROTECTED.name + " đề nghị giao dịch với bạn.", yesAction, noAction);
		}
	}

	public void onOkTrade(short userID)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(userID);
		string content = "Bắt đầu giao dịch";
		if (userID != mainChar.ID && char_HAIRMOD_PROTECTED != null)
		{
			content = char_HAIRMOD_PROTECTED.name + " đồng ý giao dịch";
		}
		addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, content, 0));
		cancelTrade();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(0, true, new sbyte[1] { 8 });
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setSize(108, 90, 6, 6, 18, 18);
		WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	public void onDeniedTrade(short userid)
	{
		Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(userid);
		string text = string.Empty;
		if (char_HAIRMOD_PROTECTED != null)
		{
			text = char_HAIRMOD_PROTECTED.name;
		}
		addChat(new ChatInfo_HAIRMOD_PROTECTED(string.Empty, text + " không đồng ý giao dịch", 0));
		cancelTrade();
	}

	public void onItemTrade(short userIdTrade, Message_HAIRMOD_PROTECTED msg, sbyte type)
	{
		try
		{
			switch (type)
			{
			case 0:
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
				itemInInventory_HAIRMOD_PROTECTED.charClazz = (itemInInventory_HAIRMOD_PROTECTED.clazz = msg.reader().readByte());
				itemInInventory_HAIRMOD_PROTECTED.itemID = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.itemOldID = itemInInventory_HAIRMOD_PROTECTED.itemID;
				itemInInventory_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.plusTemplate = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.level = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.durable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.mDurable = msg.reader().readShort();
				sbyte b2 = msg.reader().readByte();
				for (sbyte b3 = 0; b3 < b2; b3++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED o = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
					itemInInventory_HAIRMOD_PROTECTED.allAttribute.addElement(o);
				}
				itemInInventory_HAIRMOD_PROTECTED.colorName = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.heItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.hangItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.magic_physic = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
				int num = -1;
				if (userIdTrade == mainChar.ID)
				{
					num = mainChar.isExistInvector(mainChar.tItems, itemInInventory_HAIRMOD_PROTECTED);
					if (num == -1)
					{
						mainChar.tItems.addElement(itemInInventory_HAIRMOD_PROTECTED);
					}
					else
					{
						mainChar.tItems.removeElementAt(num);
					}
					break;
				}
				Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)findCharByID(userIdTrade);
				if (char_HAIRMOD_PROTECTED != null)
				{
					num = mainChar.isExistInvector(mainChar.rItems, itemInInventory_HAIRMOD_PROTECTED);
					if (num == -1)
					{
						mainChar.rItems.addElement(itemInInventory_HAIRMOD_PROTECTED);
					}
					else
					{
						mainChar.rItems.removeElementAt(num);
					}
				}
				break;
			}
			case 1:
			{
				Potion_HAIRMOD_PROTECTED potion_HAIRMOD_PROTECTED = new Potion_HAIRMOD_PROTECTED();
				potion_HAIRMOD_PROTECTED.id = msg.reader().readByte();
				potion_HAIRMOD_PROTECTED.index = MainChar_HAIRMOD_PROTECTED.listPotion[potion_HAIRMOD_PROTECTED.id].index;
				potion_HAIRMOD_PROTECTED.number = msg.reader().readShort();
				if (userIdTrade == mainChar.ID)
				{
					MainChar_HAIRMOD_PROTECTED.listPotion[potion_HAIRMOD_PROTECTED.id].numTrade += potion_HAIRMOD_PROTECTED.number;
					addPotionTrade(mainChar.tItems, potion_HAIRMOD_PROTECTED);
				}
				else
				{
					addPotionTrade(mainChar.rItems, potion_HAIRMOD_PROTECTED);
				}
				break;
			}
			case 2:
			{
				sbyte b = msg.reader().readByte();
				if (userIdTrade == mainChar.ID)
				{
					MainChar_HAIRMOD_PROTECTED.listPotion[b].numTrade = 0;
					removePotionTrade(mainChar.tItems, b);
				}
				else
				{
					removePotionTrade(mainChar.rItems, b);
				}
				break;
			}
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.Log(ex.Message);
		}
	}

	private void addPotionTrade(mVector_HAIRMOD_PROTECTED list, Potion_HAIRMOD_PROTECTED pa)
	{
		int num = list.size();
		for (int i = 0; i < num; i++)
		{
			if (list.elementAt(i) != null && list.elementAt(i) is Potion_HAIRMOD_PROTECTED)
			{
				Potion_HAIRMOD_PROTECTED potion_HAIRMOD_PROTECTED = (Potion_HAIRMOD_PROTECTED)list.elementAt(i);
				if (potion_HAIRMOD_PROTECTED != null && potion_HAIRMOD_PROTECTED.index == MainChar_HAIRMOD_PROTECTED.listPotion[pa.id].index)
				{
					potion_HAIRMOD_PROTECTED.number += pa.number;
					return;
				}
			}
		}
		list.addElement(pa);
	}

	private void removePotionTrade(mVector_HAIRMOD_PROTECTED list, int id)
	{
		int num = list.size();
		for (int i = 0; i < num; i++)
		{
			if (list.elementAt(i) != null && list.elementAt(i) is Potion_HAIRMOD_PROTECTED)
			{
				Potion_HAIRMOD_PROTECTED potion_HAIRMOD_PROTECTED = (Potion_HAIRMOD_PROTECTED)list.elementAt(i);
				if (potion_HAIRMOD_PROTECTED != null && potion_HAIRMOD_PROTECTED.index == MainChar_HAIRMOD_PROTECTED.listPotion[id].index)
				{
					list.removeElement(potion_HAIRMOD_PROTECTED);
					break;
				}
			}
		}
	}

	public void onConfirmTrade()
	{
		if (WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] != 8)
		{
			return;
		}
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
			{
				GameService_HAIRMOD_PROTECTED.gI().sendConfirmTrade();
				Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.isTrade = false;
				Canvas_HAIRMOD_PROTECTED.endDlg();
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(string.Empty);
				ActionPerform_HAIRMOD_PROTECTED action2 = delegate
				{
				};
				command_HAIRMOD_PROTECTED.action = action2;
				WindowInfoScr_HAIRMOD_PROTECTED.gI().left = command_HAIRMOD_PROTECTED;
				WindowInfoScr_HAIRMOD_PROTECTED.gI().center = new Command_HAIRMOD_PROTECTED("Xin chờ");
				WindowInfoScr_HAIRMOD_PROTECTED.gI().center.action = delegate
				{
				};
			};
			ActionPerform_HAIRMOD_PROTECTED noAction = delegate
			{
				Canvas_HAIRMOD_PROTECTED.endDlg();
			};
			Canvas_HAIRMOD_PROTECTED.startYesNoDlg("Bạn thật sự muốn trao đổi", yesAction, noAction);
		};
		WindowInfoScr_HAIRMOD_PROTECTED.gI().left = new Command_HAIRMOD_PROTECTED("Chuyển");
		WindowInfoScr_HAIRMOD_PROTECTED.gI().left.action = action;
	}

	public void onFinishTrade()
	{
		cancelTrade();
		Canvas_HAIRMOD_PROTECTED.startOKDlg("Giao dịch hoàn thành", false);
	}

	public void oncancelTrade()
	{
		cancelTrade();
		Canvas_HAIRMOD_PROTECTED.startOKDlg("Giao dịch bị hủy.");
	}

	private void cancelTrade()
	{
		mainChar.isTrade = false;
		mainChar.tItems.removeAllElements();
		mainChar.rItems.removeAllElements();
		int num = 0;
		if (MainChar_HAIRMOD_PROTECTED.listPotion != null)
		{
			num = MainChar_HAIRMOD_PROTECTED.listPotion.Length;
		}
		for (int i = 0; i < num; i++)
		{
			MainChar_HAIRMOD_PROTECTED.listPotion[i].numTrade = 0;
		}
		if (Canvas_HAIRMOD_PROTECTED.currentScreen == WindowInfoScr_HAIRMOD_PROTECTED.gI() && WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 8)
		{
			switchToMe();
		}
	}

	public static mVector_HAIRMOD_PROTECTED setListGemInbue(mVector_HAIRMOD_PROTECTED listGem)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < listGem.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)listGem.elementAt(i);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			if (gemByID.type == 0)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.id);
				GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED, gemTemplate_HAIRMOD_PROTECTED2);
				mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED2);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public void onImbue(short priceImbue, sbyte khoa)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().prizeImbue = priceImbue;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		WindowInfoScr_HAIRMOD_PROTECTED.khoaImbue = khoa;
		switch (khoa)
		{
		case 0:
			mVector_HAIRMOD_PROTECTED2 = setListGemInbue(MainChar_HAIRMOD_PROTECTED.gemItem);
			break;
		case 1:
			mVector_HAIRMOD_PROTECTED2 = setListGemInbue(MainChar_HAIRMOD_PROTECTED.gemItemKhoa);
			break;
		default:
		{
			int num = MainChar_HAIRMOD_PROTECTED.gemItem.size();
			int num2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
				GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
				if (gemByID.type == 0)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.id);
					GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED, gemTemplate_HAIRMOD_PROTECTED2);
					mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED2);
				}
			}
			for (int j = 0; j < num2; j++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItemKhoa.elementAt(j);
				bool flag = false;
				for (int k = 0; k < mVector_HAIRMOD_PROTECTED2.size(); k++)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(k);
					if (gemTemplate_HAIRMOD_PROTECTED4.id == gemTemplate_HAIRMOD_PROTECTED3.id)
					{
						gemTemplate_HAIRMOD_PROTECTED4.number += gemTemplate_HAIRMOD_PROTECTED3.number;
						flag = true;
					}
				}
				if (!flag)
				{
					GemTemp_HAIRMOD_PROTECTED gemByID2 = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED3.id);
					if (gemByID2.type == 0)
					{
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED5 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED3.id);
						GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED3, gemTemplate_HAIRMOD_PROTECTED5);
						mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED5);
					}
				}
			}
			break;
		}
		}
		GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED6 = null;
		for (int l = 0; l < mVector_HAIRMOD_PROTECTED2.size(); l++)
		{
			gemTemplate_HAIRMOD_PROTECTED6 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(l);
			WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED6);
		}
		showWindow(0, false, new sbyte[1] { 21 });
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Luyện đồ";
	}

	public void onKham(short price1, sbyte khoa)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().prizeImbue = price1;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		WindowInfoScr_HAIRMOD_PROTECTED.khoaKham = khoa;
		switch (khoa)
		{
		case 0:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItem;
			break;
		case 1:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa;
			break;
		default:
		{
			int num = MainChar_HAIRMOD_PROTECTED.gemItem.size();
			int num2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.id);
				GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED, gemTemplate_HAIRMOD_PROTECTED2);
				mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED2);
			}
			for (int j = 0; j < num2; j++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItemKhoa.elementAt(j);
				bool flag = false;
				for (int k = 0; k < mVector_HAIRMOD_PROTECTED2.size(); k++)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(k);
					if (gemTemplate_HAIRMOD_PROTECTED4.id == gemTemplate_HAIRMOD_PROTECTED3.id)
					{
						gemTemplate_HAIRMOD_PROTECTED4.number += gemTemplate_HAIRMOD_PROTECTED3.number;
						flag = true;
					}
				}
				if (!flag)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED5 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED3.id);
					GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED3, gemTemplate_HAIRMOD_PROTECTED5);
					mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED5);
				}
			}
			break;
		}
		}
		int num3 = mVector_HAIRMOD_PROTECTED2.size();
		GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED6 = null;
		for (int l = 0; l < num3; l++)
		{
			gemTemplate_HAIRMOD_PROTECTED6 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(l);
			WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED6);
		}
		showWindow(0, false, new sbyte[1] { 25 });
	}

	public void onEpDothu(short price, sbyte idColor, sbyte lv, short startIDNL, sbyte slNguyenLieu, sbyte magic, sbyte khoa)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.removeAllElements();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().prizeImbue = price;
		WindowInfoScr_HAIRMOD_PROTECTED.idColor = idColor;
		WindowInfoScr_HAIRMOD_PROTECTED.lvThu = lv;
		WindowInfoScr_HAIRMOD_PROTECTED.slNLThu = slNguyenLieu;
		WindowInfoScr_HAIRMOD_PROTECTED.magicAnimal = magic;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		WindowInfoScr_HAIRMOD_PROTECTED.khoaDoThu = khoa;
		switch (khoa)
		{
		case 0:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItem;
			break;
		case 1:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa;
			break;
		default:
		{
			int num = MainChar_HAIRMOD_PROTECTED.gemItem.size();
			int num2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.id);
				GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED, gemTemplate_HAIRMOD_PROTECTED2);
				mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED2);
			}
			for (int j = 0; j < num2; j++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItemKhoa.elementAt(j);
				bool flag = false;
				for (int k = 0; k < mVector_HAIRMOD_PROTECTED2.size(); k++)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(k);
					if (gemTemplate_HAIRMOD_PROTECTED4.id == gemTemplate_HAIRMOD_PROTECTED3.id)
					{
						gemTemplate_HAIRMOD_PROTECTED4.number += gemTemplate_HAIRMOD_PROTECTED3.number;
						flag = true;
					}
				}
				if (!flag)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED5 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED3.id);
					GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED3, gemTemplate_HAIRMOD_PROTECTED5);
					mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED5);
				}
			}
			break;
		}
		}
		int num3 = mVector_HAIRMOD_PROTECTED2.size();
		GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED6 = null;
		for (int l = 0; l < num3; l++)
		{
			gemTemplate_HAIRMOD_PROTECTED6 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(l);
			if (gemTemplate_HAIRMOD_PROTECTED6.id >= startIDNL && gemTemplate_HAIRMOD_PROTECTED6.id <= startIDNL + 5)
			{
				WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED6);
			}
		}
		if (WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.size() == 0)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Không có nguyên liệu.");
			return;
		}
		showWindow(0, false, new sbyte[1] { 32 });
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Đồ " + ((magic != 0) ? "vật" : "ma") + " " + lv;
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void onEpNgoc(short price)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().prizeImbue = price;
		showWindow(0, false, new sbyte[1] { 26 });
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Hợp thành";
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void onSpecialItem(Message_HAIRMOD_PROTECTED msg)
	{
		MainChar_HAIRMOD_PROTECTED.shopItem.removeAllElements();
		try
		{
			int num = msg.reader().readByte();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = new GemTemplate_HAIRMOD_PROTECTED();
				if (gemTemplate_HAIRMOD_PROTECTED != null)
				{
					gemTemplate_HAIRMOD_PROTECTED.rID = msg.reader().readShort();
					gemTemplate_HAIRMOD_PROTECTED.id = msg.reader().readByte();
					gemTemplate_HAIRMOD_PROTECTED.number = msg.reader().readShort();
					MainChar_HAIRMOD_PROTECTED.shopItem.addElement(gemTemplate_HAIRMOD_PROTECTED);
				}
			}
		}
		catch (Exception)
		{
			Debug.Log("LOI NHAN SPECIAL_ITEM");
		}
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setCurTab();
		MenuWindows_HAIRMOD_PROTECTED.gI().setCurTab();
	}

	public void onGemItem(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED2.removeAllElements();
			int num = msg.reader().readShort();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = new GemTemplate_HAIRMOD_PROTECTED();
				gemTemplate_HAIRMOD_PROTECTED.rID = msg.reader().readShort();
				gemTemplate_HAIRMOD_PROTECTED.id = msg.reader().readShort();
				gemTemplate_HAIRMOD_PROTECTED.number = msg.reader().readShort();
				mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED);
			}
			sbyte b = 0;
			try
			{
				b = msg.reader().readByte();
			}
			catch (Exception)
			{
			}
			if (b == 0)
			{
				MainChar_HAIRMOD_PROTECTED.gemItem = mVector_HAIRMOD_PROTECTED2;
			}
			else
			{
				MainChar_HAIRMOD_PROTECTED.gemItemKhoa = mVector_HAIRMOD_PROTECTED2;
			}
			if (Canvas_HAIRMOD_PROTECTED.currentScreen == WindowInfoScr_HAIRMOD_PROTECTED.gI())
			{
				if (WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 26)
				{
					WindowInfoScr_HAIRMOD_PROTECTED.gI().setListImbue(true, 1, b);
				}
				if (WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 0)
				{
					PauseMenu_HAIRMOD_PROTECTED.gI().showMe(0);
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.currentScreen == MenuWindows_HAIRMOD_PROTECTED.gI() && MenuWindows_HAIRMOD_PROTECTED.gI().focusTabChinh == 2)
			{
				MenuWindows_HAIRMOD_PROTECTED.gI().setCurTab();
			}
		}
		catch (Exception)
		{
		}
	}

	public void onAddItemImbue(short id)
	{
		mainChar.itemImbue = null;
		int num = Char_HAIRMOD_PROTECTED.inventory.size();
		for (int i = 0; i < num; i++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(i);
			if (itemInInventory_HAIRMOD_PROTECTED != null)
			{
				ItemTemplate_HAIRMOD_PROTECTED item = Res_HAIRMOD_PROTECTED.getItem(itemInInventory_HAIRMOD_PROTECTED.charClazz, itemInInventory_HAIRMOD_PROTECTED.idTemplate);
				if (item != null && item.id == id)
				{
					mainChar.itemImbue = itemInInventory_HAIRMOD_PROTECTED;
					break;
				}
			}
		}
	}

	public void onImbueOK(string info)
	{
		Canvas_HAIRMOD_PROTECTED.startOKDlg(info, false);
		WindowInfoScr_HAIRMOD_PROTECTED.gI().resetImbue();
	}

	public void onActorDie(Message_HAIRMOD_PROTECTED msg)
	{
		int num = msg.reader().readShort();
		int num2 = msg.reader().readByte();
		int num3 = actors.size();
		for (int i = 0; i < num3; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED == null || actor_HAIRMOD_PROTECTED.ID != num)
			{
				continue;
			}
			if (num2 == Actor_HAIRMOD_PROTECTED.CAT_MONSTER)
			{
				int num4 = 0;
				int num5 = 0;
				num4 = (actor_HAIRMOD_PROTECTED.x - 5) * 2;
				num5 = (actor_HAIRMOD_PROTECTED.y - 5) * 2;
				while (num4 > 10 || num5 > 10 || num4 < -10 || num5 < -10)
				{
					num4 >>= 1;
					num5 >>= 1;
				}
				((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).startDeadFly(num4, num5);
				actor_HAIRMOD_PROTECTED.die = true;
			}
			else if (num2 == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
			{
				actor_HAIRMOD_PROTECTED.actorDie();
				if (actor_HAIRMOD_PROTECTED.ID == mainChar.ID && BangAuto_HAIRMOD_PROTECTED.isAutoComeHome && autoFight)
				{
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
					doFire(0, 0);
				}
			}
			break;
		}
	}

	public void onBuffAttack(Message_HAIRMOD_PROTECTED m)
	{
		try
		{
			int num = m.reader().readShort();
			int num2 = m.reader().readByte();
			int num3 = m.reader().readByte();
			int num4 = m.reader().readShort();
			int num5 = m.reader().readByte();
			int num6 = m.reader().readByte();
			int num7 = -1;
			try
			{
				num7 = m.reader().readByte();
			}
			catch (Exception)
			{
			}
			int num8 = actors.size();
			for (int i = 0; i < num8; i++)
			{
				Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
				if (actor_HAIRMOD_PROTECTED == null || actor_HAIRMOD_PROTECTED.ID != num)
				{
					continue;
				}
				switch (num6)
				{
				case -1:
					startFlyText("-" + num4, 1, actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y - 40, 0, -1);
					if (num2 == Actor_HAIRMOD_PROTECTED.CAT_MONSTER)
					{
						((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).setHp(num4);
						if (((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).hp <= 0)
						{
							((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).wantDestroy = true;
						}
					}
					else
					{
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).hp -= num4;
						if (((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).hp <= 0)
						{
							((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).state = 3;
						}
					}
					break;
				case 0:
					break;
				case 1:
					break;
				case 2:
					if (num5 == 7 && num4 > 0)
					{
						startFlyText("-" + num4, 3, actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y - 40, 0, -1);
						((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).mp -= num4;
						if (((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).mp <= 0)
						{
							((Char_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).mp = 0;
						}
					}
					break;
				case 3:
					if (num5 == 4)
					{
						actor_HAIRMOD_PROTECTED.beStune = true;
						actor_HAIRMOD_PROTECTED.timeBeStune = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + ((num7 <= 0) ? 3000 : (num7 * 1000));
						CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED2 = new CharBuff_HAIRMOD_PROTECTED(actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y, 19);
						charBuff_HAIRMOD_PROTECTED2.setTimeLive((num7 <= 0) ? 3 : num7);
						actor_HAIRMOD_PROTECTED.addBuff(charBuff_HAIRMOD_PROTECTED2);
					}
					break;
				case 4:
					if (num5 == 4)
					{
						CharBuff_HAIRMOD_PROTECTED charBuff_HAIRMOD_PROTECTED = new CharBuff_HAIRMOD_PROTECTED(actor_HAIRMOD_PROTECTED.x, actor_HAIRMOD_PROTECTED.y, 22);
						charBuff_HAIRMOD_PROTECTED.setTimeLive((num7 <= 0) ? 36 : num7);
						actor_HAIRMOD_PROTECTED.addBuff(charBuff_HAIRMOD_PROTECTED);
						actor_HAIRMOD_PROTECTED.timeOutPoinson = mSystem_HAIRMOD_PROTECTED.currentTimeMillis();
						actor_HAIRMOD_PROTECTED.poinson = (short)num4;
						actor_HAIRMOD_PROTECTED.tDelay = (short)num3;
					}
					break;
				}
				break;
			}
		}
		catch (Exception)
		{
		}
	}

	public void onListShopOfNPC(sbyte[] idShop)
	{
		Canvas_HAIRMOD_PROTECTED.currentDialog = null;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		for (int i = 0; i < 3; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Gian hàng " + (i + 1));
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				doActionShop(ii, idShop[idShop.Length - 1], idShop[idShop.Length - 2]);
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	public void doActionShop(int shopID, int type, int ncpType)
	{
		switch (type)
		{
		case 0:
			WindowInfoScr_HAIRMOD_PROTECTED.gI().idSeller = ncpType;
			WindowInfoScr_HAIRMOD_PROTECTED.gI().shopIdSell = shopID;
			Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			gameService.requestGetListItem(ncpType, shopID, mainChar.ID);
			break;
		case 1:
			gameService.requestGetListUser(ncpType, shopID);
			Canvas_HAIRMOD_PROTECTED.startWaitDlg();
			break;
		}
	}

	public void onFriendList(Message_HAIRMOD_PROTECTED msg, int type, string title)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		short num = msg.reader().readShort();
		for (int i = 0; i < num; i++)
		{
			Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = new Char_HAIRMOD_PROTECTED();
			char_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
			char_HAIRMOD_PROTECTED.currentHead = msg.reader().readByte();
			char_HAIRMOD_PROTECTED.dir = 0;
			char_HAIRMOD_PROTECTED.level = msg.reader().readByte();
			char_HAIRMOD_PROTECTED.weapon_frame = 0;
			sbyte b = msg.reader().readByte();
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			for (int j = 0; j < b; j++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
				char_HAIRMOD_PROTECTED.clazz = (itemInInventory_HAIRMOD_PROTECTED.charClazz = msg.reader().readByte());
				itemInInventory_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.level = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.plusTemplate = msg.reader().readByte();
				mVector_HAIRMOD_PROTECTED3.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
			char_HAIRMOD_PROTECTED.idClan = msg.reader().readShort();
			char_HAIRMOD_PROTECTED.isMaster = msg.reader().readByte();
			if (type == 3 || type == 4 || type == 7 || type == 8)
			{
				char_HAIRMOD_PROTECTED.charXu = msg.reader().readLong();
				char_HAIRMOD_PROTECTED.luong = msg.reader().readInt();
				char_HAIRMOD_PROTECTED.nationID = msg.reader().readByte();
				if (type == 7)
				{
					char_HAIRMOD_PROTECTED.congTrang = msg.reader().readInt();
					char_HAIRMOD_PROTECTED.lienTram = msg.reader().readInt();
				}
			}
			char_HAIRMOD_PROTECTED.setWearingInfo(mVector_HAIRMOD_PROTECTED3);
			mVector_HAIRMOD_PROTECTED2.addElement(char_HAIRMOD_PROTECTED);
		}
		if (mVector_HAIRMOD_PROTECTED2.size() > 0)
		{
			switch (type)
			{
			case 0:
				ListScr_HAIRMOD_PROTECTED.gI().freindList = mVector_HAIRMOD_PROTECTED2;
				break;
			case 1:
			case 3:
			case 4:
			case 6:
			case 7:
			case 8:
				ListScr_HAIRMOD_PROTECTED.gI().setInfo(mVector_HAIRMOD_PROTECTED2, type, title);
				ListScr_HAIRMOD_PROTECTED.gI().switchToMe();
				Canvas_HAIRMOD_PROTECTED.endDlg();
				break;
			case 2:
			case 5:
				break;
			}
		}
	}

	public void onListInfoDepositeItem(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			int num = msg.reader().readByte();
			int npc = msg.reader().readByte();
			int shopID = msg.reader().readByte();
			switch (num)
			{
			case 0:
			{
				int num4 = msg.reader().readByte();
				for (int l = 0; l < num4; l++)
				{
					string caption = msg.reader().readUTF();
					short charID = msg.reader().readShort();
					if (charID != mainChar.ID)
					{
						Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(caption);
						ActionPerform_HAIRMOD_PROTECTED action = delegate
						{
							gameService.requestGetListItem(npc, shopID, charID);
							Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						};
						command_HAIRMOD_PROTECTED.action = action;
						mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
					}
				}
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				if (mVector_HAIRMOD_PROTECTED2.size() > 0)
				{
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
				}
				break;
			}
			case 1:
			{
				Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.removeAllElements();
				int num2 = msg.reader().readByte();
				short num3 = msg.reader().readShort();
				for (int i = 0; i < num2; i++)
				{
					ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
					itemInInventory_HAIRMOD_PROTECTED.clazz = (itemInInventory_HAIRMOD_PROTECTED.charClazz = msg.reader().readByte());
					itemInInventory_HAIRMOD_PROTECTED.itemID = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED.plusTemplate = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.prize = msg.reader().readInt();
					itemInInventory_HAIRMOD_PROTECTED.level = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.durable = msg.reader().readShort();
					itemInInventory_HAIRMOD_PROTECTED.numKham = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.totalKham = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.allAttribute.removeAllElements();
					sbyte b = msg.reader().readByte();
					for (sbyte b2 = 0; b2 < b; b2++)
					{
						InfoAttributeItem_HAIRMOD_PROTECTED o = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
						itemInInventory_HAIRMOD_PROTECTED.allAttribute.addElement(o);
					}
					itemInInventory_HAIRMOD_PROTECTED.colorName = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.heItem = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.beKicked = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.hangItem = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.magic_physic = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.islock = msg.reader().readByte();
					itemInInventory_HAIRMOD_PROTECTED.nameCharSeal = msg.reader().readUTF();
					if (mainChar.ID == num3)
					{
						for (int j = 0; j < Char_HAIRMOD_PROTECTED.inventory.size(); j++)
						{
							ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)Char_HAIRMOD_PROTECTED.inventory.elementAt(j);
							if (itemInInventory_HAIRMOD_PROTECTED2.itemID == itemInInventory_HAIRMOD_PROTECTED.itemID)
							{
								itemInInventory_HAIRMOD_PROTECTED2.prize = itemInInventory_HAIRMOD_PROTECTED.prize;
								itemInInventory_HAIRMOD_PROTECTED2.isSelling = true;
								WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.addElement(itemInInventory_HAIRMOD_PROTECTED2);
							}
						}
					}
					else
					{
						WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.addElement(itemInInventory_HAIRMOD_PROTECTED);
					}
				}
				sbyte b3 = msg.reader().readByte();
				for (int k = 0; k < b3; k++)
				{
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = new RealID_HAIRMOD_PROTECTED();
					realID_HAIRMOD_PROTECTED.realID = msg.reader().readShort();
					realID_HAIRMOD_PROTECTED.ID = msg.reader().readShort();
					realID_HAIRMOD_PROTECTED.price = msg.reader().readInt();
					WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.addElement(realID_HAIRMOD_PROTECTED);
				}
				WindowInfoScr_HAIRMOD_PROTECTED.gI().idSeller = npc;
				WindowInfoScr_HAIRMOD_PROTECTED.gI().shopIdSell = shopID;
				if (mainChar.ID == num3)
				{
					showWindow(0, true, new sbyte[1] { 23 });
				}
				else if (WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.size() > 0)
				{
					WindowInfoScr_HAIRMOD_PROTECTED.gI().idCharSell = num3;
					showWindow(0, true, new sbyte[1] { 24 });
				}
				break;
			}
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.LogError(ex.StackTrace + " loi tai onListInfoDepositeItem");
		}
	}

	public void showWindow(int focus, bool isSell, sbyte[] index)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(focus, isSell, index);
		WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	public void onUserSellItem(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			bool flag = msg.reader().readBoolean();
			short num = msg.reader().readShort();
			int num2 = 0;
			if (flag)
			{
				num2 = msg.reader().readInt();
			}
			if (msg.reader().readByte() == 0)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemByID = getItemByID(Char_HAIRMOD_PROTECTED.inventory, num);
				if (itemByID != null)
				{
					itemByID.isSelling = flag;
					if (flag)
					{
						itemByID.prize = num2;
						WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.addElement(itemByID);
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã bỏ vào gian hàng.");
					}
					else
					{
						itemByID.prize = 0;
						WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.removeElement(itemByID);
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Đã lấy vật phẩm khỏi gian hàng.");
					}
				}
				return;
			}
			short iD = msg.reader().readShort();
			if (flag)
			{
				GemTemplate_HAIRMOD_PROTECTED gemByRealID = GemTemplate_HAIRMOD_PROTECTED.getGemByRealID(num);
				if (gemByRealID != null)
				{
					gemByRealID.number--;
					if (gemByRealID.number <= 0)
					{
						MainChar_HAIRMOD_PROTECTED.gemItem.removeElement(gemByRealID);
					}
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = new RealID_HAIRMOD_PROTECTED(gemByRealID.rID);
					realID_HAIRMOD_PROTECTED.ID = iD;
					realID_HAIRMOD_PROTECTED.price = num2;
					WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.addElement(realID_HAIRMOD_PROTECTED);
				}
			}
			else
			{
				int num3 = WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.size();
				for (int i = 0; i < num3; i++)
				{
					RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED2 = (RealID_HAIRMOD_PROTECTED)WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.elementAt(i);
					if (realID_HAIRMOD_PROTECTED2 != null && realID_HAIRMOD_PROTECTED2.realID == num)
					{
						WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.removeElement(realID_HAIRMOD_PROTECTED2);
						GemTemplate_HAIRMOD_PROTECTED gemByID = GemTemplate_HAIRMOD_PROTECTED.getGemByID(realID_HAIRMOD_PROTECTED2.ID);
						if (gemByID != null)
						{
							gemByID.number++;
							break;
						}
						GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = new GemTemplate_HAIRMOD_PROTECTED();
						gemTemplate_HAIRMOD_PROTECTED.rID = realID_HAIRMOD_PROTECTED2.realID;
						gemTemplate_HAIRMOD_PROTECTED.id = realID_HAIRMOD_PROTECTED2.ID;
						gemTemplate_HAIRMOD_PROTECTED.number = 1;
						MainChar_HAIRMOD_PROTECTED.gemItem.addElement(gemTemplate_HAIRMOD_PROTECTED);
						break;
					}
				}
			}
			Canvas_HAIRMOD_PROTECTED.endDlg();
		}
		catch (Exception)
		{
			Out_HAIRMOD_PROTECTED.LogError("LOI HAM ON_USER_SELL_ITEM");
		}
	}

	public void onBuyItemOfUser(short itemID, short charID, sbyte type)
	{
		if (mainChar.ID == charID)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Món đồ đã ở trong hành trang.");
		}
		int num = WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.size();
		for (int i = 0; i < num; i++)
		{
			if (type == 0 && WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.elementAt(i) is ItemInInventory_HAIRMOD_PROTECTED)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.elementAt(i);
				if (itemInInventory_HAIRMOD_PROTECTED != null && itemInInventory_HAIRMOD_PROTECTED.itemID == itemID)
				{
					WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.removeElementAt(i);
					if (WindowInfoScr_HAIRMOD_PROTECTED.gI() == Canvas_HAIRMOD_PROTECTED.currentScreen && WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 24)
					{
						WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(WindowInfoScr_HAIRMOD_PROTECTED.gI().getListBuyItem());
					}
					break;
				}
			}
			else
			{
				if (!(WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.elementAt(i) is RealID_HAIRMOD_PROTECTED))
				{
					continue;
				}
				RealID_HAIRMOD_PROTECTED realID_HAIRMOD_PROTECTED = (RealID_HAIRMOD_PROTECTED)WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.elementAt(i);
				if (realID_HAIRMOD_PROTECTED != null && realID_HAIRMOD_PROTECTED.realID == itemID)
				{
					WindowInfoScr_HAIRMOD_PROTECTED.gI().sellItem.removeElementAt(i);
					if (WindowInfoScr_HAIRMOD_PROTECTED.gI() == Canvas_HAIRMOD_PROTECTED.currentScreen && WindowInfoScr_HAIRMOD_PROTECTED.index[WindowInfoScr_HAIRMOD_PROTECTED.focusTab] == 24)
					{
						WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(WindowInfoScr_HAIRMOD_PROTECTED.gI().getListBuyItem());
					}
					break;
				}
			}
		}
	}

	public void onInfoFriend(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			int num = msg.reader().readByte();
			string text = msg.reader().readUTF();
			int charID = msg.reader().readShort();
			switch (num)
			{
			case 0:
			{
				ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
				{
					gameService.sendInfoAddFriend(charID, 1);
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				ActionPerform_HAIRMOD_PROTECTED noAction = delegate
				{
					gameService.sendInfoAddFriend(charID, -1);
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				};
				Canvas_HAIRMOD_PROTECTED.startYesNoDlg(text + " muốn kết bạn với bạn", yesAction, noAction);
				break;
			}
			case 1:
				break;
			case -1:
				break;
			}
		}
		catch (Exception ex)
		{
			Debug.Log(ex.Message);
		}
	}

	public void onSMSStruct(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			int num = msg.reader().readByte();
			for (int i = 0; i < num; i++)
			{
				string text = msg.reader().readUTF();
				string o = msg.reader().readUTF();
				string o2 = msg.reader().readUTF();
				if (Main_HAIRMOD_PROTECTED.isPC)
				{
					if (!text.Contains("sms"))
					{
						decriptNap.addElement(text);
					}
				}
				else
				{
					decriptNap.addElement(text);
				}
				syntaxNap.addElement(o);
				centerNap.addElement(o2);
			}
		}
		catch (IOException)
		{
		}
	}

	public static void loadConfig(int type)
	{
		switch (type)
		{
		case 0:
			paintCay = 0;
			paintChar = 1;
			if (Tilemap_HAIRMOD_PROTECTED.trees != null)
			{
				int num2 = Tilemap_HAIRMOD_PROTECTED.trees.size();
				for (int j = 0; j < num2; j++)
				{
					((Tree_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.trees.elementAt(j))?.removeImage();
				}
			}
			break;
		case 1:
		case 2:
			paintCay = 1;
			paintChar = 1;
			break;
		case 3:
			paintCay = 0;
			paintChar = 0;
			if (Tilemap_HAIRMOD_PROTECTED.trees != null)
			{
				int num = Tilemap_HAIRMOD_PROTECTED.trees.size();
				for (int i = 0; i < num; i++)
				{
					((Tree_HAIRMOD_PROTECTED)Tilemap_HAIRMOD_PROTECTED.trees.elementAt(i))?.removeImage();
				}
			}
			break;
		}
	}

	public void showConfig()
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		string[] array = new string[4] { "Thấp", "Vừa", "Cao", "Rất thấp" };
		for (int i = 0; i < 4; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(array[i]);
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				gameService.setConfig(ii);
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
	}

	public void onRegClan(short[] arrayIndex)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int num = arrayIndex.Length;
		for (int i = 0; i < num; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED();
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				ActionPerform_HAIRMOD_PROTECTED ok = delegate
				{
					if (!Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText().Equals(string.Empty))
					{
						Canvas_HAIRMOD_PROTECTED.startWaitDlg();
						gameService.chooseIconClan(arrayIndex[ii], Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
						PaintPopup_HAIRMOD_PROTECTED.gI().right.perform();
					}
				};
				Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Tên bang hội của bạn", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 25, true);
				Canvas_HAIRMOD_PROTECTED.inputDlg.show();
			};
			ActionPaintCmd_HAIRMOD_PROTECTED actionPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
			{
				GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(arrayIndex[ii] + 1000), x, y);
			};
			command_HAIRMOD_PROTECTED.action = action;
			command_HAIRMOD_PROTECTED.actionPaint = actionPaint;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		PaintPopup_HAIRMOD_PROTECTED.gI().setPos(20, 20, Canvas_HAIRMOD_PROTECTED.w - 40, Canvas_HAIRMOD_PROTECTED.h - 40 - MyScreen_HAIRMOD_PROTECTED.deltaY, (Canvas_HAIRMOD_PROTECTED.w - 40) / 20);
		PaintPopup_HAIRMOD_PROTECTED.gI().setInfo(mVector_HAIRMOD_PROTECTED2, "ICON");
		PaintPopup_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	public void showClanInfo(ClanInfo_HAIRMOD_PROTECTED clan)
	{
		ViewInfoScr_HAIRMOD_PROTECTED.gI().setInfo(clan);
		ViewInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void onViewCharInfo(Char_HAIRMOD_PROTECTED ch)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().setInfo(0, false, new sbyte[1] { 1 });
		WindowInfoScr_HAIRMOD_PROTECTED.charWearing = ch;
		WindowInfoScr_HAIRMOD_PROTECTED.gI().switchToMe();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = "Trang bị";
		Canvas_HAIRMOD_PROTECTED.endDlg();
	}

	public void setNPC_server_limit(string name0, short ID0, short idImg0, short x0, short y0, short wimg0, short himg0, sbyte nFrame0, sbyte typeLimit)
	{
		if (typeLimit != 1)
		{
			return;
		}
		int num = npc_Limit.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)npc_Limit.elementAt(i);
			if (actor_HAIRMOD_PROTECTED == null || actor_HAIRMOD_PROTECTED.catagory != 2)
			{
				continue;
			}
			NPC_HAIRMOD_PROTECTED nPC_HAIRMOD_PROTECTED = (NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			if (nPC_HAIRMOD_PROTECTED.npcType == 1)
			{
				Npc_Server_HAIRMOD_PROTECTED npc_Server_HAIRMOD_PROTECTED = (Npc_Server_HAIRMOD_PROTECTED)nPC_HAIRMOD_PROTECTED;
				if (npc_Server_HAIRMOD_PROTECTED.ID == ID0)
				{
					npc_Server_HAIRMOD_PROTECTED.name = name0;
					npc_Server_HAIRMOD_PROTECTED.type = (npc_Server_HAIRMOD_PROTECTED.ID = ID0);
					npc_Server_HAIRMOD_PROTECTED.idImg = idImg0;
					npc_Server_HAIRMOD_PROTECTED.x = x0;
					npc_Server_HAIRMOD_PROTECTED.y = y0;
					npc_Server_HAIRMOD_PROTECTED.width = wimg0;
					npc_Server_HAIRMOD_PROTECTED.height = himg0;
					npc_Server_HAIRMOD_PROTECTED.nFrame = nFrame0;
					nPC_HAIRMOD_PROTECTED.typeLimit = typeLimit;
					return;
				}
			}
		}
		Npc_Server_HAIRMOD_PROTECTED npc_Server_HAIRMOD_PROTECTED2 = new Npc_Server_HAIRMOD_PROTECTED();
		npc_Server_HAIRMOD_PROTECTED2.name = name0;
		npc_Server_HAIRMOD_PROTECTED2.type = (npc_Server_HAIRMOD_PROTECTED2.ID = ID0);
		npc_Server_HAIRMOD_PROTECTED2.idImg = idImg0;
		npc_Server_HAIRMOD_PROTECTED2.x = x0;
		npc_Server_HAIRMOD_PROTECTED2.y = y0;
		npc_Server_HAIRMOD_PROTECTED2.width = wimg0;
		npc_Server_HAIRMOD_PROTECTED2.height = himg0;
		npc_Server_HAIRMOD_PROTECTED2.nFrame = nFrame0;
		npc_Server_HAIRMOD_PROTECTED2.typeLimit = typeLimit;
		npc_Limit.addElement(npc_Server_HAIRMOD_PROTECTED2);
	}

	public void setNPC_server(string name0, short ID0, short idImg0, short x0, short y0, short wimg0, short himg0, sbyte nFrame0, sbyte typeLimit)
	{
		setNPC_server_limit(name0, ID0, idImg0, x0, y0, wimg0, himg0, nFrame0, typeLimit);
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED == null || actor_HAIRMOD_PROTECTED.catagory != 2)
			{
				continue;
			}
			NPC_HAIRMOD_PROTECTED nPC_HAIRMOD_PROTECTED = (NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
			if (nPC_HAIRMOD_PROTECTED.npcType == 1)
			{
				Npc_Server_HAIRMOD_PROTECTED npc_Server_HAIRMOD_PROTECTED = (Npc_Server_HAIRMOD_PROTECTED)nPC_HAIRMOD_PROTECTED;
				if (npc_Server_HAIRMOD_PROTECTED.ID == ID0)
				{
					npc_Server_HAIRMOD_PROTECTED.name = name0;
					npc_Server_HAIRMOD_PROTECTED.type = (npc_Server_HAIRMOD_PROTECTED.ID = ID0);
					npc_Server_HAIRMOD_PROTECTED.idImg = idImg0;
					npc_Server_HAIRMOD_PROTECTED.x = x0;
					npc_Server_HAIRMOD_PROTECTED.y = y0;
					npc_Server_HAIRMOD_PROTECTED.width = wimg0;
					npc_Server_HAIRMOD_PROTECTED.height = himg0;
					npc_Server_HAIRMOD_PROTECTED.nFrame = nFrame0;
					npc_Server_HAIRMOD_PROTECTED.typeLimit = typeLimit;
					return;
				}
			}
		}
		Npc_Server_HAIRMOD_PROTECTED npc_Server_HAIRMOD_PROTECTED2 = new Npc_Server_HAIRMOD_PROTECTED();
		npc_Server_HAIRMOD_PROTECTED2.name = name0;
		npc_Server_HAIRMOD_PROTECTED2.type = (npc_Server_HAIRMOD_PROTECTED2.ID = ID0);
		npc_Server_HAIRMOD_PROTECTED2.idImg = idImg0;
		npc_Server_HAIRMOD_PROTECTED2.x = x0;
		npc_Server_HAIRMOD_PROTECTED2.y = y0;
		npc_Server_HAIRMOD_PROTECTED2.width = wimg0;
		npc_Server_HAIRMOD_PROTECTED2.height = himg0;
		npc_Server_HAIRMOD_PROTECTED2.nFrame = nFrame0;
		npc_Server_HAIRMOD_PROTECTED2.typeLimit = typeLimit;
		actors.addElement(npc_Server_HAIRMOD_PROTECTED2);
	}

	public void setMyFarm(int idFarm, int id, int x, int y, string info, string name, int idImgTree, int timeLive, bool isBuyOk, sbyte addy, sbyte colorString)
	{
		int num = actors.size();
		for (int i = 0; i < num; i++)
		{
			Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
			if (actor_HAIRMOD_PROTECTED != null && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
			{
				Ground_HAIRMOD_PROTECTED ground_HAIRMOD_PROTECTED = (Ground_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED;
				if (ground_HAIRMOD_PROTECTED.ID == idFarm)
				{
					ground_HAIRMOD_PROTECTED.setInfo(idFarm, id, x, y, info, name, idImgTree, timeLive, isBuyOk, addy, colorString);
					return;
				}
			}
		}
		actors.addElement(new Ground_HAIRMOD_PROTECTED(idFarm, id, x, y, info, name, idImgTree, timeLive, isBuyOk, addy, colorString));
	}

	public static Actor_HAIRMOD_PROTECTED CreateActor(sbyte catagory, short type, short id, short x, short y, sbyte isnewmonster, sbyte isThanthu)
	{
		Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = null;
		switch (catagory)
		{
		case 0:
			if (isThanthu > -1)
			{
				actor_HAIRMOD_PROTECTED = new ThanThu_HAIRMOD_PROTECTED();
				actor_HAIRMOD_PROTECTED.setIdThanThu(isThanthu);
			}
			else
			{
				actor_HAIRMOD_PROTECTED = new Char_HAIRMOD_PROTECTED();
			}
			break;
		case 1:
			switch (type)
			{
			case 91:
			case 92:
				actor_HAIRMOD_PROTECTED = new Boss_Snake_HAIRMOD_PROTECTED();
				break;
			case 93:
				actor_HAIRMOD_PROTECTED = new Boss_Eyes_HAIRMOD_PROTECTED();
				break;
			case 94:
				actor_HAIRMOD_PROTECTED = new Boss_Xuong_HAIRMOD_PROTECTED();
				break;
			case 46:
				actor_HAIRMOD_PROTECTED = new Boss_TuongQuan_HAIRMOD_PROTECTED();
				break;
			case 43:
				actor_HAIRMOD_PROTECTED = new TruTranThanh_HAIRMOD_PROTECTED(0);
				break;
			case 36:
				actor_HAIRMOD_PROTECTED = new TruTranThanh_HAIRMOD_PROTECTED(1);
				break;
			case 37:
				actor_HAIRMOD_PROTECTED = new TruTranThanh_HAIRMOD_PROTECTED(2);
				break;
			case 38:
				actor_HAIRMOD_PROTECTED = new BossTL_HAIRMOD_PROTECTED();
				break;
			case 39:
				actor_HAIRMOD_PROTECTED = new BossThanLan_HAIRMOD_PROTECTED();
				break;
			case 82:
				actor_HAIRMOD_PROTECTED = new Boss_Eagle_HAIRMOD_PROTECTED();
				break;
			case 83:
				actor_HAIRMOD_PROTECTED = (congThanh = new CongThanh_HAIRMOD_PROTECTED());
				break;
			case 84:
				actor_HAIRMOD_PROTECTED = new CuongThi_HAIRMOD_PROTECTED();
				break;
			case 113:
				actor_HAIRMOD_PROTECTED = new MonsterNew_HAIRMOD_PROTECTED(113);
				break;
			case 115:
				actor_HAIRMOD_PROTECTED = new MonsterNew_HAIRMOD_PROTECTED(115);
				break;
			case 116:
				actor_HAIRMOD_PROTECTED = new MonsterNew_HAIRMOD_PROTECTED(116);
				break;
			case 117:
				actor_HAIRMOD_PROTECTED = new MonsterNew_HAIRMOD_PROTECTED(117);
				break;
			case 95:
			case 96:
			case 97:
			case 98:
			case 99:
			case 100:
			case 101:
			case 102:
			case 103:
			case 104:
			case 105:
			case 106:
			case 107:
			case 108:
			case 109:
			case 110:
			case 111:
			case 112:
				actor_HAIRMOD_PROTECTED = new MonsterNew_HAIRMOD_PROTECTED(type);
				break;
			default:
				actor_HAIRMOD_PROTECTED = ((isnewmonster != 1) ? new Monster_HAIRMOD_PROTECTED() : new MonsterNew_HAIRMOD_PROTECTED(type));
				break;
			}
			break;
		case 3:
			actor_HAIRMOD_PROTECTED = new DropItem_HAIRMOD_PROTECTED();
			break;
		case 4:
			actor_HAIRMOD_PROTECTED = new DropPotion_HAIRMOD_PROTECTED();
			break;
		case 6:
		case 7:
			actor_HAIRMOD_PROTECTED = new DropGemItem_HAIRMOD_PROTECTED();
			break;
		case 14:
			actor_HAIRMOD_PROTECTED = new ItemQuest_HAIRMOD_PROTECTED();
			break;
		}
		actor_HAIRMOD_PROTECTED.setNewMonster(isnewmonster);
		if (type >= 85 && type <= 89 && catagory == 1)
		{
			((Monster_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).isMineralC = true;
		}
		actor_HAIRMOD_PROTECTED.x = x;
		actor_HAIRMOD_PROTECTED.y = y;
		actor_HAIRMOD_PROTECTED.catagory = catagory;
		actor_HAIRMOD_PROTECTED.ID = id;
		actor_HAIRMOD_PROTECTED.xFirst = x;
		actor_HAIRMOD_PROTECTED.yFirst = y;
		actor_HAIRMOD_PROTECTED.setType(type);
		return actor_HAIRMOD_PROTECTED;
	}

	public void onCapCha(sbyte[] arrayImg)
	{
		Image_HAIRMOD_PROTECTED img = Image_HAIRMOD_PROTECTED.createImage(arrayImg, 0, arrayImg.Length);
		int w = img.getWidth() + 35;
		int num = img.getHeight() + 60;
		TField_HAIRMOD_PROTECTED tfText = new TField_HAIRMOD_PROTECTED(5, num - 26, w - 10, 20);
		tfText.isFocus = true;
		Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("OK");
		ActionPerform_HAIRMOD_PROTECTED action = delegate
		{
			gameService.doCapCha(tfText.getText());
			Canvas_HAIRMOD_PROTECTED.gameScr.switchToMe();
		};
		command_HAIRMOD_PROTECTED.action = action;
		PageScr_HAIRMOD_PROTECTED.gI().setInfo(Canvas_HAIRMOD_PROTECTED.hw - w / 2, Canvas_HAIRMOD_PROTECTED.hh - num / 2 - MyScreen_HAIRMOD_PROTECTED.deltaY, w, num, "NHẬP CHỮ", null);
		PageScr_HAIRMOD_PROTECTED.gI().left = command_HAIRMOD_PROTECTED;
		PageScr_HAIRMOD_PROTECTED.gI().right = tfText.cmdClear;
		Layer_HAIRMOD_PROTECTED layer_HAIRMOD_PROTECTED = new Layer_HAIRMOD_PROTECTED();
		ActionPerform_HAIRMOD_PROTECTED isUpdate = delegate
		{
			tfText.update();
		};
		ActionPaintCmd_HAIRMOD_PROTECTED isPaint = delegate(MyGraphics_HAIRMOD_PROTECTED g, int x, int y)
		{
			g.drawImage(img, w / 2, 30, MyGraphics_HAIRMOD_PROTECTED.TOP | MyGraphics_HAIRMOD_PROTECTED.HCENTER);
			tfText.paint(g);
		};
		ActionKey_HAIRMOD_PROTECTED isKeyCode = delegate(int keyCode)
		{
			tfText.keyPressed(keyCode);
		};
		layer_HAIRMOD_PROTECTED.isUpdate = isUpdate;
		layer_HAIRMOD_PROTECTED.isPaint = isPaint;
		layer_HAIRMOD_PROTECTED.isKeyCode = isKeyCode;
		PageScr_HAIRMOD_PROTECTED.gI().layer = layer_HAIRMOD_PROTECTED;
		PageScr_HAIRMOD_PROTECTED.gI().switchToMe();
	}

	private void updateminiMap()
	{
		cmtoXMini = mainChar.x / 16;
		cmtoYmini = mainChar.y / 16;
		int num = 60;
		if (wMiniMap < 60)
		{
			num = wMiniMap;
		}
		if (cmtoXMini > Tilemap_HAIRMOD_PROTECTED.w - num / 2)
		{
			cmtoXMini = Tilemap_HAIRMOD_PROTECTED.w - num;
		}
		else if (cmtoXMini < wMiniMap / 2)
		{
			cmtoXMini = 0;
		}
		else
		{
			cmtoXMini -= wMiniMap / 2;
		}
		if (cmtoYmini < hMiniMap / 2)
		{
			cmtoYmini = 0;
		}
		else
		{
			cmtoYmini -= hMiniMap / 2;
		}
		if (cmtoYmini > Tilemap_HAIRMOD_PROTECTED.h - hMiniMap)
		{
			cmtoYmini = Tilemap_HAIRMOD_PROTECTED.h - hMiniMap;
		}
	}

	public void isOff()
	{
		if (isBatMiniMap)
		{
			if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 + 2, 40, 30))
			{
				isBatMiniMap = !isBatMiniMap;
			}
			if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + 2, 40, 30))
			{
				MenuSelectItem_HAIRMOD_PROTECTED.gI().switchToMe();
			}
			return;
		}
		int num = 0;
		int num2 = 0;
		if (Tilemap_HAIRMOD_PROTECTED.imgMap != null)
		{
			num = Tilemap_HAIRMOD_PROTECTED.imgMap.getWidth();
			num2 = Tilemap_HAIRMOD_PROTECTED.imgMap.getHeight();
		}
		if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, isBatMiniMap ? (Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() - 20 - num) : (Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() - 10), isBatMiniMap ? (num2 / 2) : (Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2), 40, 30) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			isBatMiniMap = !isBatMiniMap;
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
		}
		if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 20, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2, 40, 30) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			MenuSelectItem_HAIRMOD_PROTECTED.gI().switchToMe();
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
		}
	}

	public void updateTouchMHP()
	{
		if (hideGUI != 0)
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerMHP(Canvas_HAIRMOD_PROTECTED.w - 141, Canvas_HAIRMOD_PROTECTED.h - Canvas_HAIRMOD_PROTECTED.hh / 2 - 82, 70, 70))
		{
			if (Canvas_HAIRMOD_PROTECTED.isPointerClick1)
			{
				timePutKeyMP = 3;
				Canvas_HAIRMOD_PROTECTED.keyPressed[7] = true;
				Canvas_HAIRMOD_PROTECTED.isPointerClick1 = false;
			}
		}
		else if (Canvas_HAIRMOD_PROTECTED.isPointerMHP(Canvas_HAIRMOD_PROTECTED.w - 90, Canvas_HAIRMOD_PROTECTED.h - Canvas_HAIRMOD_PROTECTED.hh / 2 - 100, 70, 70) && Canvas_HAIRMOD_PROTECTED.isPointerClick1)
		{
			timePutKeyHP = 3;
			Canvas_HAIRMOD_PROTECTED.keyPressed[9] = true;
			Canvas_HAIRMOD_PROTECTED.isPointerClick1 = false;
		}
	}

	private void updatePoint()
	{
		if (Canvas_HAIRMOD_PROTECTED.currentDialog != null || Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			return;
		}
		int num = 0;
		if ((listMSGServer.size() > 0 && msgWorld.size() == 0) || (listMSGServer.size() == 0 && msgWorld.size() > 0))
		{
			num = 20;
		}
		else if (listMSGServer.size() > 0 && msgWorld.size() > 0)
		{
			num = 40;
		}
		if (!Canvas_HAIRMOD_PROTECTED.isPointer(0, 0, Canvas_HAIRMOD_PROTECTED.w, Canvas_HAIRMOD_PROTECTED.h))
		{
			return;
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			if (isBatMiniMap)
			{
				if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 + 2 + num, 40, 30))
				{
					isBatMiniMap = !isBatMiniMap;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					return;
				}
				if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 30, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + 2 + num, 40, 30))
				{
					MenuSelectItem_HAIRMOD_PROTECTED.gI().switchToMe();
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					return;
				}
			}
			else
			{
				if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() - 10, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + num, 40, 30))
				{
					isBatMiniMap = !isBatMiniMap;
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					return;
				}
				if (xacDinhToaDo(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Canvas_HAIRMOD_PROTECTED.w - Res_HAIRMOD_PROTECTED.imgChat.getWidth() * 2 - 20, Res_HAIRMOD_PROTECTED.imgChat.getWidth() / 2 + num, 40, 30))
				{
					MenuSelectItem_HAIRMOD_PROTECTED.gI().switchToMe();
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					return;
				}
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDown && hideGUI == 0)
		{
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			pointer(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py);
		}
		else if (Canvas_HAIRMOD_PROTECTED.isPointerClick)
		{
			isNhanPhim = false;
			if (hideGUI == 0)
			{
				if (isTouchPre(xChangeTab, yChangeTab, Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getWidth(), Res_HAIRMOD_PROTECTED.imgSwapQuickSlot.getHeight()))
				{
					if (indexTabSlot == 0)
					{
						indexTabSlot = 1;
					}
					else
					{
						indexTabSlot = 0;
					}
					xSlot = Res_HAIRMOD_PROTECTED.wKhung;
				}
				if (isTouchPre(xChangeFocus, yChangeFocus, Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Res_HAIRMOD_PROTECTED.imgFocusActor.getWidth(), Res_HAIRMOD_PROTECTED.imgFocusActor.getHeight()) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					changeToNextFocusActor(false);
				}
				if (isTouchPre(xChangeClothes, yChangeClothes, Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Res_HAIRMOD_PROTECTED.imgSwapClothes.getWidth(), Res_HAIRMOD_PROTECTED.imgSwapClothes.getHeight()) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					GameService_HAIRMOD_PROTECTED.gI().setConfig(6);
					Canvas_HAIRMOD_PROTECTED.endDlg();
				}
				if (isTouchPre(xSelectFc, ySelectFc, Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Res_HAIRMOD_PROTECTED.imgSelectFc.getWidth(), Res_HAIRMOD_PROTECTED.imgSelectFc.getHeight()) && Canvas_HAIRMOD_PROTECTED.isPointerClick)
				{
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					doParty();
				}
				if (isTouchPre(xChatTouch, yChatTouch, Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py, Res_HAIRMOD_PROTECTED.imgChatTouch.getWidth(), Res_HAIRMOD_PROTECTED.imgChatTouch.getHeight()))
				{
					Out_HAIRMOD_PROTECTED.println("chat mode ");
					if (Canvas_HAIRMOD_PROTECTED.isPointerClick && Main_HAIRMOD_PROTECTED.isPC)
					{
						Canvas_HAIRMOD_PROTECTED.gameScr.chatMode = true;
						Canvas_HAIRMOD_PROTECTED.endDlg();
						ActionPerform_HAIRMOD_PROTECTED ok = delegate
						{
							doChatFromMe(Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
							Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.setText(string.Empty);
						};
						Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo("Nội dung Chat", ok, TField_HAIRMOD_PROTECTED.INPUT_TYPE_ANY, 100, false);
						Canvas_HAIRMOD_PROTECTED.inputDlg.show();
					}
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointer(xHp + 10, yHp + 10, 50, 50) && !Main_HAIRMOD_PROTECTED.isPC)
			{
				resetFcBt();
				isPaintFcBt[7] = true;
				Canvas_HAIRMOD_PROTECTED.keyPressed[7] = true;
				Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[9] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointer(xFire + 20, yFire + 20, 80, 80) && !Main_HAIRMOD_PROTECTED.isPC)
			{
				resetFcBt();
				isPaintFcBt[3] = true;
				Canvas_HAIRMOD_PROTECTED.keyPressed[5] = true;
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointer(xOne + 10, yOne + 10, 50, 50) && !Main_HAIRMOD_PROTECTED.isPC)
			{
				resetFcBt();
				isPaintFcBt[4] = true;
				Canvas_HAIRMOD_PROTECTED.keyPressed[1] = true;
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[8] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				Out_HAIRMOD_PROTECTED.println("11111111111111 fire");
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointer(xThree + 10, yThree + 10, 50, 50) && !Main_HAIRMOD_PROTECTED.isPC)
			{
				resetFcBt();
				isPaintFcBt[5] = true;
				Canvas_HAIRMOD_PROTECTED.keyPressed[3] = true;
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
				Out_HAIRMOD_PROTECTED.println("3333333333333333 fire");
			}
			else if (Canvas_HAIRMOD_PROTECTED.isPointer(xMp + 10, yMp + 10, 50, 50) && !Main_HAIRMOD_PROTECTED.isPC)
			{
				resetFcBt();
				isPaintFcBt[8] = true;
				Canvas_HAIRMOD_PROTECTED.keyPressed[9] = true;
				Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
				Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
				Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			}
			if (Canvas_HAIRMOD_PROTECTED.isPointer(0, 0, 60, 50) && mainChar.hp > 0 && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
			{
				MenuWindows_HAIRMOD_PROTECTED.gI().switchToMe();
				mainChar.posTransRoad = null;
				Canvas_HAIRMOD_PROTECTED.isPointerClick = (Canvas_HAIRMOD_PROTECTED.isPointerDown = false);
				Canvas_HAIRMOD_PROTECTED.clearKeyPressed();
			}
			if (MessageScr_HAIRMOD_PROTECTED.nMSG > 0 && Canvas_HAIRMOD_PROTECTED.isPointer(0, imgInfo.getHeight() + 3, 30, 30))
			{
				MessageScr_HAIRMOD_PROTECTED.gI().switchToMe();
			}
			Canvas_HAIRMOD_PROTECTED.clearKeyHold();
			btnFocus = 0;
		}
		if (!canTouchScreen(Canvas_HAIRMOD_PROTECTED.px, Canvas_HAIRMOD_PROTECTED.py) || !Canvas_HAIRMOD_PROTECTED.isPointerClick || !Main_HAIRMOD_PROTECTED.canvas.canPutKey())
		{
			return;
		}
		int num2 = cmx + Canvas_HAIRMOD_PROTECTED.px;
		int num3 = cmy + Canvas_HAIRMOD_PROTECTED.py;
		if (focusedActor != null)
		{
			if (focusedActor.catagory != Actor_HAIRMOD_PROTECTED.CAT_MY_GROUND)
			{
				int num4 = actors.size();
				for (int i = 0; i < num4; i++)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = (Actor_HAIRMOD_PROTECTED)actors.elementAt(i);
					if (Util_HAIRMOD_PROTECTED.abs(actor_HAIRMOD_PROTECTED.x - num2) >= 20 || Util_HAIRMOD_PROTECTED.abs(actor_HAIRMOD_PROTECTED.y - 20 - num3) >= 40)
					{
						continue;
					}
					Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
					if (focusedActor.ID == actor_HAIRMOD_PROTECTED.ID)
					{
						if (actor_HAIRMOD_PROTECTED is Char_HAIRMOD_PROTECTED && (mainChar.pk <= 0 || map / 100 == 3))
						{
							if (((Char_HAIRMOD_PROTECTED)focusedActor).idBoss == -1)
							{
								doParty();
							}
							else
							{
								doFire(5, indexTabSlot);
							}
							return;
						}
						if (focusedActor.catagory != 2 || actor_HAIRMOD_PROTECTED.catagory != 2)
						{
							doFire(5, indexTabSlot);
							return;
						}
						if (((NPC_HAIRMOD_PROTECTED)focusedActor).type == ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type)
						{
							if (((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).type != 4)
							{
								doFire(5, indexTabSlot);
								return;
							}
							if (((NPC_HAIRMOD_PROTECTED)focusedActor).idLinhGac == ((NPC_HAIRMOD_PROTECTED)actor_HAIRMOD_PROTECTED).idLinhGac)
							{
								doFire(5, indexTabSlot);
								return;
							}
							if (actor_HAIRMOD_PROTECTED.ID != mainChar.ID)
							{
								focusedActor = actor_HAIRMOD_PROTECTED;
								doFire(5, indexTabSlot);
								return;
							}
						}
					}
					if (actor_HAIRMOD_PROTECTED.ID == mainChar.ID)
					{
						return;
					}
					bool flag = false;
					if (typeOptionFocus == 1)
					{
						if (actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && (!actor_HAIRMOD_PROTECTED.iskiller || actor_HAIRMOD_PROTECTED.isNPC()))
						{
							flag = true;
						}
						if (actor_HAIRMOD_PROTECTED.catagory != Actor_HAIRMOD_PROTECTED.CAT_MONSTER && actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && !actor_HAIRMOD_PROTECTED.iskiller)
						{
							flag = true;
						}
					}
					else if (typeOptionFocus == 2)
					{
						if (!actor_HAIRMOD_PROTECTED.isNPC())
						{
							flag = true;
						}
					}
					else if (typeOptionFocus == 3)
					{
						if ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.idClan == mainChar.idClan) || actor_HAIRMOD_PROTECTED.isNPC())
						{
							flag = true;
						}
					}
					else if (typeOptionFocus == 4 && ((actor_HAIRMOD_PROTECTED.catagory == Actor_HAIRMOD_PROTECTED.CAT_PLAYER && actor_HAIRMOD_PROTECTED.nationID == mainChar.nationID) || actor_HAIRMOD_PROTECTED.isNPC()))
					{
						flag = true;
					}
					if (!flag)
					{
						focusedActor = actor_HAIRMOD_PROTECTED;
					}
					if (map == 29)
					{
						findRoad(num2, num3);
					}
					return;
				}
				focusedActor = null;
			}
			else
			{
				if (mainChar.x >= xBeginFrame && mainChar.x <= xTheendFrame + 32 && mainChar.y >= yBeginFrame && mainChar.y <= yTheendFrame + 32)
				{
					int num5 = actors.size();
					for (int j = 0; j < num5; j++)
					{
						Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED2 = (Actor_HAIRMOD_PROTECTED)actors.elementAt(j);
						if (num2 < actor_HAIRMOD_PROTECTED2.x + 2 || num2 > actor_HAIRMOD_PROTECTED2.x + 30 || num3 < actor_HAIRMOD_PROTECTED2.y || num3 > actor_HAIRMOD_PROTECTED2.y + 30)
						{
							continue;
						}
						Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
						if (focusedActor.ID != -1 && focusedActor.ID == actor_HAIRMOD_PROTECTED2.ID)
						{
							if (mainChar.state == 0)
							{
								doFire(5, indexTabSlot);
							}
						}
						else if (actor_HAIRMOD_PROTECTED2.ID != mainChar.ID)
						{
							findRoad(num2, num3);
						}
						return;
					}
				}
				focusedActor = null;
			}
		}
		findRoad(num2, num3);
		if (MenuSelectItem_HAIRMOD_PROTECTED.isClose)
		{
			MenuSelectItem_HAIRMOD_PROTECTED.isClose = false;
			mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
		}
	}

	public void resetFcBt()
	{
		for (int i = 0; i < isPaintFcBt.Length; i++)
		{
			isPaintFcBt[i] = false;
		}
	}

	private void pointer(int x11, int y11)
	{
		int width = Res_HAIRMOD_PROTECTED.imgNewBt[4].getWidth();
		int height = Res_HAIRMOD_PROTECTED.imgNewBt[4].getHeight();
		if (isTouchHold(xHp + 10, yHp + 10, x11, y11, 50, 50))
		{
			resetFcBt();
			isPaintFcBt[7] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[9] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
		}
		if (isTouchHold(xU, yU, x11, y11, width, height))
		{
			resetFcBt();
			isPaintFcBt[0] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[8] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[9] = false;
			isNhanPhim = true;
			mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
		}
		else if (isTouchHold(xD, yD, x11, y11, width, height))
		{
			resetFcBt();
			isPaintFcBt[3] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[8] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[9] = false;
			isNhanPhim = true;
			mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
		}
		else if (isTouchHold(xL, yL, x11, y11, width, height))
		{
			resetFcBt();
			isPaintFcBt[1] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[8] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[9] = false;
			isNhanPhim = true;
			mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
		}
		else if (isTouchHold(xR, yR, x11, y11, width, height))
		{
			resetFcBt();
			isPaintFcBt[2] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[6] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[9] = false;
			isNhanPhim = true;
			mainChar.posTransRoad = updateFindRoad(0, 0, 0, 0);
		}
		else if (Canvas_HAIRMOD_PROTECTED.isPointer(Canvas_HAIRMOD_PROTECTED.w - 108, Canvas_HAIRMOD_PROTECTED.h - Canvas_HAIRMOD_PROTECTED.hh / 2 - 35, 120, 120))
		{
			resetFcBt();
			isPaintFcBt[6] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
		}
		else if (isTouchHold(xMp + 10, yMp + 10, x11, y11, 50, 50))
		{
			resetFcBt();
			isPaintFcBt[8] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
		}
		if (isTouchHold(xOne + 10, yOne + 10, x11, y11, 50, 50))
		{
			resetFcBt();
			isPaintFcBt[4] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[3] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[8] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
		}
		else if (isTouchHold(xThree + 10, yThree + 10, x11, y11, 50, 50))
		{
			resetFcBt();
			isPaintFcBt[5] = true;
			Canvas_HAIRMOD_PROTECTED.keyHold[2] = (Canvas_HAIRMOD_PROTECTED.keyHold[8] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[4] = (Canvas_HAIRMOD_PROTECTED.keyHold[6] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[1] = (Canvas_HAIRMOD_PROTECTED.keyHold[5] = false);
			Canvas_HAIRMOD_PROTECTED.keyHold[7] = (Canvas_HAIRMOD_PROTECTED.keyHold[9] = false);
		}
	}

	public static bool xacDinhToaDo(int px, int py, int posx, int posy, int w, int h)
	{
		if (px > posx - w / 2 && px < posx + w / 2 && py > posy - h / 2 && py < posy + h / 2)
		{
			return true;
		}
		return false;
	}

	public static bool isTouchPre(int x, int y, int px, int py, int w, int h)
	{
		if (px >= x && px <= x + w && py >= y && py <= y + h)
		{
			return true;
		}
		return false;
	}

	public static bool isTouchHold(int x, int y, int px, int py, int w, int h)
	{
		if (Main_HAIRMOD_PROTECTED.isPC)
		{
			return false;
		}
		if (px >= x && px <= x + w && py >= y && py <= y + h)
		{
			return true;
		}
		return false;
	}

	public void findRoad(int aa, int bb)
	{
		aa /= 16;
		bb /= 16;
		if (!Tilemap_HAIRMOD_PROTECTED.tileTypeAtPixel(cmx + Canvas_HAIRMOD_PROTECTED.px, cmy + Canvas_HAIRMOD_PROTECTED.py, 2) && !checkMoveLimit(cmx + Canvas_HAIRMOD_PROTECTED.px, cmy + Canvas_HAIRMOD_PROTECTED.py))
		{
			if (posCam == null)
			{
				posCam = new Position_HAIRMOD_PROTECTED(0, 0);
			}
			posCam.x = (short)aa;
			posCam.y = (short)bb;
			Canvas_HAIRMOD_PROTECTED.isPointerClick = false;
			mainChar.xTo = mainChar.x;
			mainChar.yTo = mainChar.y;
			mainChar.xBegin = mainChar.x;
			mainChar.yBegin = mainChar.y;
			mainChar.posTransRoad = updateFindRoad(mainChar.x / 16, mainChar.y / 16, aa, bb);
			mainChar.countRoad = 0;
		}
	}

	public void findRoad2(int aa, int bb)
	{
		aa /= 16;
		bb /= 16;
		if (posCam == null)
		{
			posCam = new Position_HAIRMOD_PROTECTED(0, 0);
		}
		posCam.x = (short)aa;
		posCam.y = (short)bb;
		mainChar.xTo = mainChar.x;
		mainChar.yTo = mainChar.y;
		tg.index = 8;
		mainChar.posTransRoad = updateFindRoad(mainChar.x / 16, mainChar.y / 16, aa, bb);
		mainChar.countRoad = 0;
		timeRemovePos = 100;
	}

	private int setDis(int x1, int y1, int x2, int y2)
	{
		return Util_HAIRMOD_PROTECTED.abs(x1 - x2) + Util_HAIRMOD_PROTECTED.abs(y1 - y2);
	}

	private short[] updateFindRoad(int xF, int yF, int xTo, int yTo)
	{
		if (Util_HAIRMOD_PROTECTED.distance(xF * 16, yF * 16, xTo * 16, yTo * 16) <= 16)
		{
			return null;
		}
		if (checkMoveLimit(xTo, yTo))
		{
			return null;
		}
		xStart = (sbyte)cmxMini;
		yStart = (sbyte)cmyMini;
		xF -= xStart;
		yF -= yStart;
		xTo -= xStart;
		yTo -= yStart;
		int num = mapFind.Length;
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < mapFind[i].Length; j++)
			{
				int num2 = (yStart + j) * Tilemap_HAIRMOD_PROTECTED.w + (xStart + i);
				if (num2 < Tilemap_HAIRMOD_PROTECTED.type.Length - 1)
				{
					if (Tilemap_HAIRMOD_PROTECTED.type[num2] == 2)
					{
						mapFind[i][j] = -1;
					}
					else
					{
						mapFind[i][j] = 0;
					}
				}
			}
		}
		short num3 = 0;
		int num4 = xF;
		int num5 = yF;
		short num6 = (short)num4;
		short num7 = (short)num5;
		if (num4 < 0 || num4 >= mapFind.Length)
		{
			return null;
		}
		if (num4 < mapFind.Length && num5 < mapFind[num4].Length)
		{
			mapFind[num4][num5] = 1;
		}
		num3 = 2;
		int num8 = mapFind.Length;
		int num9 = mapFind[0].Length;
		int num10 = 0;
		while (true)
		{
			num10++;
			if (num10 > 500)
			{
				if (iTaskAuto == 2)
				{
					iTaskAutoLast = iTaskAuto;
					isFixBugAutoQuest = true;
				}
				iTaskAuto = 1;
				return null;
			}
			int num11 = -1;
			int num12 = -1;
			if (num4 + 1 < num8 && mapFind[num4 + 1][num5] == 0)
			{
				mapFind[num4 + 1][num5] = (sbyte)num3;
				num11 = num4 + 1;
				num12 = num5;
			}
			if (num4 - 1 >= 0 && mapFind[num4 - 1][num5] == 0)
			{
				mapFind[num4 - 1][num5] = (sbyte)num3;
				if (num11 != -1)
				{
					if (setDis(num11, num12, xTo, yTo) > setDis(num4 - 1, num5, xTo, yTo))
					{
						num11 = num4 - 1;
						num12 = num5;
					}
				}
				else
				{
					num11 = num4 - 1;
					num12 = num5;
				}
			}
			if (num5 + 1 < num9 && mapFind[num4][num5 + 1] == 0)
			{
				mapFind[num4][num5 + 1] = (sbyte)num3;
				if (num11 != -1)
				{
					if (setDis(num11, num12, xTo, yTo) > setDis(num4, num5 + 1, xTo, yTo))
					{
						num11 = num4;
						num12 = num5 + 1;
					}
				}
				else
				{
					num11 = num4;
					num12 = num5 + 1;
				}
			}
			if (num5 - 1 >= 0 && mapFind[num4][num5 - 1] == 0)
			{
				mapFind[num4][num5 - 1] = (sbyte)num3;
				if (num11 != -1)
				{
					if (setDis(num11, num12, xTo, yTo) > setDis(num4, num5 - 1, xTo, yTo))
					{
						num11 = num4;
						num12 = num5 - 1;
					}
				}
				else
				{
					num11 = num4;
					num12 = num5 - 1;
				}
			}
			int num13 = -1;
			if (num11 != -1)
			{
				num13 = 0;
				num4 = num11;
				num5 = num12;
			}
			else
			{
				num4 = (num5 = 1000);
			}
			for (short num14 = 0; num14 < num8; num14++)
			{
				for (short num15 = 0; num15 < num9; num15++)
				{
					if (mapFind[num14][num15] > 1 && setContinue(num14, num15, mapFind) && mapFind[num14][num15] + setDis(num14, num15, xTo, yTo) < num3 + setDis(num4, num5, xTo, yTo))
					{
						num4 = num14;
						num5 = num15;
						num3 = mapFind[num14][num15];
						num13 = 0;
					}
				}
			}
			if (num4 == xTo && num5 == yTo)
			{
				break;
			}
			if (num13 == 0)
			{
				num3++;
				continue;
			}
			return null;
		}
		if (num3 >= 127)
		{
			return null;
		}
		int num16 = 0;
		short[] array = new short[num3];
		while (true)
		{
			array[num16] = (short)((num4 << 8) + num5);
			if (num4 + 1 < num8 && mapFind[num4 + 1][num5] == mapFind[num4][num5] - 1)
			{
				num4++;
			}
			else if (num4 - 1 >= 0 && mapFind[num4 - 1][num5] == mapFind[num4][num5] - 1)
			{
				num4--;
			}
			else if (num5 + 1 < num9 && mapFind[num4][num5 + 1] == mapFind[num4][num5] - 1)
			{
				num5++;
			}
			else if (num5 - 1 >= 0 && mapFind[num4][num5 - 1] == mapFind[num4][num5] - 1)
			{
				num5--;
			}
			if (num4 == num6 && num5 == num7)
			{
				break;
			}
			num16++;
		}
		return array;
	}

	private bool setContinue(int i, int j, sbyte[][] mapFind)
	{
		if (i + 1 < mapFind.Length && mapFind[i + 1][j] == 0)
		{
			return true;
		}
		if (i - 1 >= 0 && mapFind[i - 1][j] == 0)
		{
			return true;
		}
		if (j + 1 < mapFind[i].Length && mapFind[i][j + 1] == 0)
		{
			return true;
		}
		if (j - 1 >= 0 && mapFind[i][j - 1] == 0)
		{
			return true;
		}
		return false;
	}

	public void onMenuOption(int userId1, sbyte idMenu, string[] listStr)
	{
		Out_HAIRMOD_PROTECTED.println("may lan nho");
		if (Canvas_HAIRMOD_PROTECTED.menu.showMenu)
		{
			Canvas_HAIRMOD_PROTECTED.menu.showMenu = false;
		}
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		int num = listStr.Length;
		for (int i = 0; i < num; i++)
		{
			int ii = i;
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED(listStr[i]);
			ActionPerform_HAIRMOD_PROTECTED action = delegate
			{
				gameService.doMenuOption(userId1, idMenu, ii);
			};
			command_HAIRMOD_PROTECTED.action = action;
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
		Canvas_HAIRMOD_PROTECTED.currentDialog = null;
	}

	public void onTextBox(int userID, sbyte idMenu, string nameText, int typeInput)
	{
		ActionPerform_HAIRMOD_PROTECTED ok = delegate
		{
			gameService.doTextBox(userID, idMenu, Canvas_HAIRMOD_PROTECTED.inputDlg.tfInput.getText());
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		Canvas_HAIRMOD_PROTECTED.inputDlg.setInfo(nameText, ok, typeInput, 40, true);
		Canvas_HAIRMOD_PROTECTED.inputDlg.show();
	}

	public void onContentClanQuest(string content)
	{
		ActionPerform_HAIRMOD_PROTECTED yesAction = delegate
		{
			gameService.questClan(1);
			Canvas_HAIRMOD_PROTECTED.endDlg();
		};
		Canvas_HAIRMOD_PROTECTED.startYesNoDlg(content, yesAction);
	}

	public static void onWeather(sbyte weather2, int number, int timeLimit)
	{
		if (weather2 != -1)
		{
			effAnimate.addElement(new AnimateEffect_HAIRMOD_PROTECTED(weather2, true, number, timeLimit));
		}
		else
		{
			int num = effAnimate.size();
			for (int i = 0; i < num; i++)
			{
				AnimateEffect_HAIRMOD_PROTECTED animateEffect_HAIRMOD_PROTECTED = (AnimateEffect_HAIRMOD_PROTECTED)effAnimate.elementAt(i);
				animateEffect_HAIRMOD_PROTECTED.isStop = true;
			}
		}
		weather = weather2;
	}

	public void onSetCharKham(short idChar, sbyte[] idSkill, sbyte[] time)
	{
		LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)findActorByID(idChar);
		if (liveActor_HAIRMOD_PROTECTED == null)
		{
			return;
		}
		int num = idSkill.Length;
		for (int i = 0; i < num; i++)
		{
			int num2 = 0;
			int num3 = liveActor_HAIRMOD_PROTECTED.effSkill.size();
			for (int j = 0; j < num3; j++)
			{
				Skill_Kham_HAIRMOD_PROTECTED skill_Kham_HAIRMOD_PROTECTED = (Skill_Kham_HAIRMOD_PROTECTED)liveActor_HAIRMOD_PROTECTED.effSkill.elementAt(j);
				if (skill_Kham_HAIRMOD_PROTECTED.type == idSkill[i])
				{
					skill_Kham_HAIRMOD_PROTECTED.delay = time[i];
					num2 = 1;
					break;
				}
			}
			if (num2 == 0)
			{
				liveActor_HAIRMOD_PROTECTED.effSkill.addElement(new Skill_Kham_HAIRMOD_PROTECTED(idSkill[i], time[i], liveActor_HAIRMOD_PROTECTED));
			}
		}
	}

	public void onDynamicObj(DynamicObj_HAIRMOD_PROTECTED obj)
	{
		objDynamic.addElement(obj);
		actors.addElement(obj);
	}

	public void onEffect(EffectServerManager_HAIRMOD_PROTECTED effObj)
	{
		if (effManager == null)
		{
			effManager = new mVector_HAIRMOD_PROTECTED();
		}
		effManager.addElement(effObj);
	}

	public void onCheDo(string nameItem, short idItem, mVector_HAIRMOD_PROTECTED listMineral, int type, int Khoa)
	{
		WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.removeAllElements();
		WindowInfoScr_HAIRMOD_PROTECTED.gI().listCheDo = listMineral;
		WindowInfoScr_HAIRMOD_PROTECTED.gI().listEp = new mVector_HAIRMOD_PROTECTED();
		WindowInfoScr_HAIRMOD_PROTECTED.idTemp = idItem;
		WindowInfoScr_HAIRMOD_PROTECTED.optionCreateItem = (sbyte)type;
		if (listMineral.size() == 0)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Vật phẩm không thể thăng cấp");
			return;
		}
		WindowInfoScr_HAIRMOD_PROTECTED.khoaCheDo = (sbyte)Khoa;
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		switch (Khoa)
		{
		case 0:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItem;
			break;
		case 1:
			mVector_HAIRMOD_PROTECTED2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa;
			break;
		default:
		{
			int num = MainChar_HAIRMOD_PROTECTED.gemItem.size();
			int num2 = MainChar_HAIRMOD_PROTECTED.gemItemKhoa.size();
			for (int i = 0; i < num; i++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItem.elementAt(i);
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED.id);
				GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED, gemTemplate_HAIRMOD_PROTECTED2);
				mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED2);
			}
			for (int j = 0; j < num2; j++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED3 = (GemTemplate_HAIRMOD_PROTECTED)MainChar_HAIRMOD_PROTECTED.gemItemKhoa.elementAt(j);
				bool flag = false;
				for (int k = 0; k < mVector_HAIRMOD_PROTECTED2.size(); k++)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED4 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(k);
					if (gemTemplate_HAIRMOD_PROTECTED4.id == gemTemplate_HAIRMOD_PROTECTED3.id)
					{
						gemTemplate_HAIRMOD_PROTECTED4.number += gemTemplate_HAIRMOD_PROTECTED3.number;
						flag = true;
					}
				}
				if (!flag)
				{
					GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED5 = new GemTemplate_HAIRMOD_PROTECTED(gemTemplate_HAIRMOD_PROTECTED3.id);
					GemTemplate_HAIRMOD_PROTECTED.copyData(gemTemplate_HAIRMOD_PROTECTED3, gemTemplate_HAIRMOD_PROTECTED5);
					mVector_HAIRMOD_PROTECTED2.addElement(gemTemplate_HAIRMOD_PROTECTED5);
				}
			}
			break;
		}
		}
		int num3 = listMineral.size();
		Mineral_HAIRMOD_PROTECTED mineral_HAIRMOD_PROTECTED = null;
		for (int l = 0; l < num3; l++)
		{
			mineral_HAIRMOD_PROTECTED = (Mineral_HAIRMOD_PROTECTED)listMineral.elementAt(l);
			for (int m = 0; m < mVector_HAIRMOD_PROTECTED2.size(); m++)
			{
				GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED6 = (GemTemplate_HAIRMOD_PROTECTED)mVector_HAIRMOD_PROTECTED2.elementAt(m);
				if ((type <= 2 && (type != 2 || (type == 2 && l == 0))) || type > 2)
				{
					if (gemTemplate_HAIRMOD_PROTECTED6.id >= mineral_HAIRMOD_PROTECTED.idTemplate + ((type != 0) ? 3 : 0) && gemTemplate_HAIRMOD_PROTECTED6.id <= mineral_HAIRMOD_PROTECTED.idTemplate + 5)
					{
						WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED6);
					}
				}
				else if (gemTemplate_HAIRMOD_PROTECTED6.id == mineral_HAIRMOD_PROTECTED.idTemplate + 5)
				{
					WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.addElement(gemTemplate_HAIRMOD_PROTECTED6);
				}
			}
		}
		if (WindowInfoScr_HAIRMOD_PROTECTED.gI().listTemp.size() == 0)
		{
			Canvas_HAIRMOD_PROTECTED.startOKDlg("Không có nguyên liệu.");
			return;
		}
		showWindow(0, false, new sbyte[1] { 28 });
		WindowInfoScr_HAIRMOD_PROTECTED.gI().name = nameItem;
	}

	private void ktGem(GemTemplate_HAIRMOD_PROTECTED g, mVector_HAIRMOD_PROTECTED vTam)
	{
		for (int i = 0; i < vTam.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)vTam.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED.id == g.id)
			{
				return;
			}
		}
		vTam.addElement(g);
	}

	public void onMSGServer(string readUTF)
	{
		if (listMSGServer == null)
		{
			listMSGServer = new mVector_HAIRMOD_PROTECTED();
		}
		listMSGServer.addElement(readUTF);
		MessageScr_HAIRMOD_PROTECTED.gI().addTab(readUTF, null, 0);
	}

	public void onDropItem2Ground(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			int num = msg.reader().readByte();
			for (int i = 0; i < num; i++)
			{
				Dropable_HAIRMOD_PROTECTED dropable_HAIRMOD_PROTECTED = (Dropable_HAIRMOD_PROTECTED)CreateActor(msg.reader().readByte(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), 0, -1);
				if (dropable_HAIRMOD_PROTECTED != null)
				{
					dropable_HAIRMOD_PROTECTED.startDropFrom(dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y, dropable_HAIRMOD_PROTECTED.x, dropable_HAIRMOD_PROTECTED.y);
					actors.addElement(dropable_HAIRMOD_PROTECTED);
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void onSkillClan(SkillClan_HAIRMOD_PROTECTED[] skillclan, SkillClan_HAIRMOD_PROTECTED[] skillClanPrivate)
	{
		skillClan = skillclan;
		this.skillClanPrivate = skillClanPrivate;
	}

	public void onLuckyDraw(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			switch (msg.reader().readByte())
			{
			case 0:
			{
				ItemQsScreen_HAIRMOD_PROTECTED.gI().resetData();
				ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.removeAllElements();
				ItemQsScreen_HAIRMOD_PROTECTED.totalItemSelect = msg.reader().readByte();
				int num = msg.reader().readByte();
				for (sbyte b3 = 0; b3 < num; b3++)
				{
					ItemLuckyDraw_HAIRMOD_PROTECTED itemLuckyDraw_HAIRMOD_PROTECTED = new ItemLuckyDraw_HAIRMOD_PROTECTED();
					itemLuckyDraw_HAIRMOD_PROTECTED.cat = msg.reader().readByte();
					itemLuckyDraw_HAIRMOD_PROTECTED.id = msg.reader().readShort();
					itemLuckyDraw_HAIRMOD_PROTECTED.name = msg.reader().readUTF();
					itemLuckyDraw_HAIRMOD_PROTECTED.decript = new string[msg.reader().readByte()];
					for (int i = 0; i < itemLuckyDraw_HAIRMOD_PROTECTED.decript.Length; i++)
					{
						itemLuckyDraw_HAIRMOD_PROTECTED.decript[i] = msg.reader().readUTF();
					}
					itemLuckyDraw_HAIRMOD_PROTECTED.isSelect = msg.reader().readByte();
					ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.addElement(itemLuckyDraw_HAIRMOD_PROTECTED);
				}
				ItemQsScreen_HAIRMOD_PROTECTED.total = (short)ItemQsScreen_HAIRMOD_PROTECTED.allItemLucky.size();
				if (ItemQsScreen_HAIRMOD_PROTECTED.total < 32)
				{
					ItemQsScreen_HAIRMOD_PROTECTED.total = 32;
				}
				ItemQsScreen_HAIRMOD_PROTECTED.gI().last = this;
				ItemQsScreen_HAIRMOD_PROTECTED.gI().switchToMe();
				break;
			}
			case 1:
			{
				DialLuckyScr_HAIRMOD_PROTECTED.gI().lastScr = this;
				DialLuckyScr_HAIRMOD_PROTECTED.gI().switchToMe();
				sbyte b = msg.reader().readByte();
				DialLuckyScr_HAIRMOD_PROTECTED.arrayIndex = new sbyte[b];
				for (sbyte b2 = 0; b2 < b; b2++)
				{
					DialLuckyScr_HAIRMOD_PROTECTED.arrayIndex[b2] = msg.reader().readByte();
				}
				DialLuckyScr_HAIRMOD_PROTECTED.gI().onStart();
				break;
			}
			}
		}
		catch (Exception)
		{
		}
	}

	public static void reSetQuest()
	{
		mainQuest = null;
		subQuest = null;
		clanQuest = null;
	}

	public static int isNpcQuest(int idNpc)
	{
		if (mainQuest != null)
		{
			if ((mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc)
			{
				return 1;
			}
			if (mainQuest.stateQuest == 0)
			{
				return 0;
			}
		}
		if (subQuest != null)
		{
			if ((subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc)
			{
				return 3;
			}
			if (subQuest.stateQuest == 0)
			{
				return 2;
			}
		}
		if (clanQuest != null)
		{
			if ((clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc)
			{
				return 3;
			}
			if (clanQuest.stateQuest == 0)
			{
				return 2;
			}
		}
		return -1;
	}

	public void getAllPosNPCMInimap(Actor_HAIRMOD_PROTECTED npc)
	{
		if (mainQuest != null && (mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == npc.getTypeNpc())
		{
			setPosNPC(npc.getTypeNpc(), (mainQuest.stateQuest == 1) ? 3 : 0, npc.x, npc.y);
			return;
		}
		if (subQuest != null && (subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == npc.getTypeNpc())
		{
			setPosNPC(npc.getTypeNpc(), (subQuest.stateQuest == 1) ? 3 : 0, npc.x, npc.y);
			return;
		}
		if (clanQuest != null && (clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == npc.getTypeNpc())
		{
			setPosNPC(npc.getTypeNpc(), (clanQuest.stateQuest == 1) ? 3 : 0, npc.x, npc.y);
			return;
		}
		for (int i = 0; i < allQuestCanReceive.size(); i++)
		{
			Quest_HAIRMOD_PROTECTED quest_HAIRMOD_PROTECTED = (Quest_HAIRMOD_PROTECTED)allQuestCanReceive.elementAt(i);
			if (quest_HAIRMOD_PROTECTED.stateQuest == 0 && quest_HAIRMOD_PROTECTED.idNpcReceive == npc.getTypeNpc())
			{
				setPosNPC(npc.getTypeNpc(), 0, npc.x, npc.y);
			}
		}
	}

	public static mVector_HAIRMOD_PROTECTED getAllQuestNpc(int idNpc)
	{
		mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
		if (mainQuest != null && (mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED = new Command_HAIRMOD_PROTECTED("Trả" + mainQuest.name);
			command_HAIRMOD_PROTECTED.action = delegate
			{
				currQuest = mainQuest;
				currQuest.startTalk(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar, Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor, mainQuest.stateQuest);
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED);
		}
		if (subQuest != null && (subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED2 = new Command_HAIRMOD_PROTECTED("Trả " + subQuest.name);
			command_HAIRMOD_PROTECTED2.action = delegate
			{
				currQuest = subQuest;
				currQuest.startTalk(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar, Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor, subQuest.stateQuest);
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED2);
		}
		if (clanQuest != null && (clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc)
		{
			Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED3 = new Command_HAIRMOD_PROTECTED("Trả " + clanQuest.name);
			command_HAIRMOD_PROTECTED3.action = delegate
			{
				currQuest = clanQuest;
				currQuest.startTalk(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar, Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor, clanQuest.stateQuest);
			};
			mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED3);
		}
		for (int i = 0; i < allQuestCanReceive.size(); i++)
		{
			Quest_HAIRMOD_PROTECTED q = (Quest_HAIRMOD_PROTECTED)allQuestCanReceive.elementAt(i);
			if (q.stateQuest == 0 && q.idNpcReceive == idNpc)
			{
				Command_HAIRMOD_PROTECTED command_HAIRMOD_PROTECTED4 = new Command_HAIRMOD_PROTECTED(q.name);
				command_HAIRMOD_PROTECTED4.action = delegate
				{
					currQuest = q;
					currQuest.startTalk(Canvas_HAIRMOD_PROTECTED.gameScr.mainChar, Canvas_HAIRMOD_PROTECTED.gameScr.focusedActor, q.stateQuest);
				};
				mVector_HAIRMOD_PROTECTED2.addElement(command_HAIRMOD_PROTECTED4);
			}
		}
		return mVector_HAIRMOD_PROTECTED2;
	}

	public static Image_HAIRMOD_PROTECTED getImgQuest(int idNpc)
	{
		if (mainQuest != null && (mainQuest.stateQuest == 1 || mainQuest.stateQuest == 2) && mainQuest.idNpcResponse == idNpc)
		{
			return Res_HAIRMOD_PROTECTED.imgSelect2;
		}
		if (subQuest != null && (subQuest.stateQuest == 1 || subQuest.stateQuest == 2) && subQuest.idNpcResponse == idNpc)
		{
			return Res_HAIRMOD_PROTECTED.imgSelect2;
		}
		if (clanQuest != null && (clanQuest.stateQuest == 1 || clanQuest.stateQuest == 2) && clanQuest.idNpcResponse == idNpc)
		{
			return Res_HAIRMOD_PROTECTED.imgSelect2;
		}
		for (int i = 0; i < allQuestCanReceive.size(); i++)
		{
			Quest_HAIRMOD_PROTECTED quest_HAIRMOD_PROTECTED = (Quest_HAIRMOD_PROTECTED)allQuestCanReceive.elementAt(i);
			if (quest_HAIRMOD_PROTECTED.stateQuest == 0 && quest_HAIRMOD_PROTECTED.idNpcReceive == idNpc)
			{
				if (quest_HAIRMOD_PROTECTED.type == 3)
				{
					return Res_HAIRMOD_PROTECTED.imgSelect2;
				}
				return Res_HAIRMOD_PROTECTED.imgSelect1;
			}
		}
		return Res_HAIRMOD_PROTECTED.imgSelect;
	}

	public void setPosNPC(int id, int indexColor, int x, int y)
	{
		for (int i = 0; i < PosNPCQuest.size(); i++)
		{
			Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED = (Position_HAIRMOD_PROTECTED)PosNPCQuest.elementAt(i);
			if (position_HAIRMOD_PROTECTED != null && position_HAIRMOD_PROTECTED.id == id)
			{
				return;
			}
		}
		Position_HAIRMOD_PROTECTED position_HAIRMOD_PROTECTED2 = new Position_HAIRMOD_PROTECTED();
		position_HAIRMOD_PROTECTED2.id = id;
		position_HAIRMOD_PROTECTED2.indexColor = (short)indexColor;
		position_HAIRMOD_PROTECTED2.x = x;
		position_HAIRMOD_PROTECTED2.y = y;
		PosNPCQuest.addElement(position_HAIRMOD_PROTECTED2);
	}

	public void onQuest(Message_HAIRMOD_PROTECTED m)
	{
		try
		{
			sbyte b = m.reader().readByte();
			sbyte b2 = m.reader().readByte();
			Out_HAIRMOD_PROTECTED.println(b + " NHAN NHIEM VU MOI " + b2);
			if (b == 0)
			{
				allQuestCanReceive.removeAllElements();
			}
			for (int i = 0; i < b2; i++)
			{
				int id = m.reader().readShort();
				Quest_HAIRMOD_PROTECTED quest_HAIRMOD_PROTECTED = new Quest_HAIRMOD_PROTECTED(id);
				quest_HAIRMOD_PROTECTED.stateQuest = b;
				quest_HAIRMOD_PROTECTED.main_sub = m.reader().readByte();
				quest_HAIRMOD_PROTECTED.name = m.reader().readUTF();
				switch (b)
				{
				case 0:
				{
					quest_HAIRMOD_PROTECTED.idNpcReceive = m.reader().readByte();
					string[] array = Util_HAIRMOD_PROTECTED.split(m.reader().readUTF(), ">");
					for (int l = 0; l < array.Length; l++)
					{
						quest_HAIRMOD_PROTECTED.content.addElement(array[l]);
					}
					quest_HAIRMOD_PROTECTED.type = m.reader().readByte();
					quest_HAIRMOD_PROTECTED.deltaLv = m.reader().readByte();
					break;
				}
				case 1:
				{
					quest_HAIRMOD_PROTECTED.idNpcResponse = m.reader().readByte();
					string[] array2 = Util_HAIRMOD_PROTECTED.split(m.reader().readUTF(), ">");
					for (int n = 0; n < array2.Length; n++)
					{
						quest_HAIRMOD_PROTECTED.rescontent.addElement(array2[n]);
					}
					quest_HAIRMOD_PROTECTED.decript = m.reader().readUTF();
					quest_HAIRMOD_PROTECTED.infoGift = m.reader().readUTF();
					break;
				}
				case 2:
					quest_HAIRMOD_PROTECTED.type = m.reader().readByte();
					quest_HAIRMOD_PROTECTED.decript = m.reader().readUTF();
					quest_HAIRMOD_PROTECTED.idNpcResponse = m.reader().readByte();
					quest_HAIRMOD_PROTECTED.deltaLv = m.reader().readByte();
					quest_HAIRMOD_PROTECTED.infoGift = m.reader().readUTF();
					if (quest_HAIRMOD_PROTECTED.type == 2)
					{
						int num = m.reader().readByte();
						quest_HAIRMOD_PROTECTED.idItemGet = new short[num];
						quest_HAIRMOD_PROTECTED.totalItemGet = new short[num];
						quest_HAIRMOD_PROTECTED.allItemGet = new short[num];
						for (int j = 0; j < num; j++)
						{
							short num2 = m.reader().readShort();
							short num3 = m.reader().readShort();
							short num4 = m.reader().readShort();
							quest_HAIRMOD_PROTECTED.idItemGet[j] = num2;
							quest_HAIRMOD_PROTECTED.allItemGet[j] = num3;
							quest_HAIRMOD_PROTECTED.totalItemGet[j] = num4;
						}
					}
					else if (quest_HAIRMOD_PROTECTED.type == 0)
					{
						int num5 = m.reader().readByte();
						quest_HAIRMOD_PROTECTED.idMonsKill = new short[num5];
						quest_HAIRMOD_PROTECTED.totalMonskill = new short[num5];
						quest_HAIRMOD_PROTECTED.allMonsKilled = new short[num5];
						for (int k = 0; k < num5; k++)
						{
							short num6 = m.reader().readShort();
							short num7 = m.reader().readShort();
							short num8 = m.reader().readShort();
							quest_HAIRMOD_PROTECTED.idMonsKill[k] = num6;
							quest_HAIRMOD_PROTECTED.allMonsKilled[k] = num7;
							quest_HAIRMOD_PROTECTED.totalMonskill[k] = num8;
						}
					}
					else if (quest_HAIRMOD_PROTECTED.type == 4)
					{
						quest_HAIRMOD_PROTECTED.totalMonskill = new short[1];
						quest_HAIRMOD_PROTECTED.allMonsKilled = new short[1];
						quest_HAIRMOD_PROTECTED.allMonsKilled[0] = m.reader().readShort();
						quest_HAIRMOD_PROTECTED.totalMonskill[0] = m.reader().readShort();
					}
					break;
				}
				if (b == 0)
				{
					allQuestCanReceive.addElement(quest_HAIRMOD_PROTECTED);
					if (quest_HAIRMOD_PROTECTED.main_sub == 0 && Char_HAIRMOD_PROTECTED.skillLevelLearnt[0] == 0)
					{
						idNpcReceive = quest_HAIRMOD_PROTECTED.idNpcReceive;
						set_npc_request(idNpcReceive);
					}
				}
				else if (quest_HAIRMOD_PROTECTED.main_sub == 0)
				{
					mainQuest = quest_HAIRMOD_PROTECTED;
				}
				else if (quest_HAIRMOD_PROTECTED.main_sub == 1)
				{
					subQuest = quest_HAIRMOD_PROTECTED;
				}
				else if (quest_HAIRMOD_PROTECTED.main_sub == 2)
				{
					clanQuest = quest_HAIRMOD_PROTECTED;
				}
			}
		}
		catch (Exception ex)
		{
			Out_HAIRMOD_PROTECTED.println("Loi tai onQuest" + ex.ToString());
		}
	}

	public void onFruit(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			Tree2014_HAIRMOD_PROTECTED.gI().setInfoTree(msg);
		}
		catch (Exception)
		{
		}
	}

	public TimecountDown_HAIRMOD_PROTECTED findTime(int id)
	{
		for (int i = 0; i < VecTime.size(); i++)
		{
			TimecountDown_HAIRMOD_PROTECTED timecountDown_HAIRMOD_PROTECTED = (TimecountDown_HAIRMOD_PROTECTED)VecTime.elementAt(i);
			if (timecountDown_HAIRMOD_PROTECTED != null && timecountDown_HAIRMOD_PROTECTED.id == id)
			{
				return timecountDown_HAIRMOD_PROTECTED;
			}
		}
		return null;
	}

	public void onShopNew(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			short typeshop = msg.reader().readShort();
			ShopNew_HAIRMOD_PROTECTED.gI().typeshop = typeshop;
			int num = msg.reader().readShort();
			for (int i = 0; i < num; i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
				itemInInventory_HAIRMOD_PROTECTED.charClazz = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.itemID = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.plusTemplate = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.level = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.mDurable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.durable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.clazz = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.numKham = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.totalKham = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.colorName = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.heItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.beKicked = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.hangItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.magic_physic = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.islock = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.nameCharSeal = msg.reader().readUTF();
				itemInInventory_HAIRMOD_PROTECTED.allAttribute.removeAllElements();
				itemInInventory_HAIRMOD_PROTECTED.lastTime = mSystem_HAIRMOD_PROTECTED.getCurrentTimeMillis();
				itemInInventory_HAIRMOD_PROTECTED.dayUse = msg.reader().readUnsignedShort();
				sbyte b = msg.reader().readByte();
				for (sbyte b2 = 0; b2 < b; b2++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED o = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
					itemInInventory_HAIRMOD_PROTECTED.allAttribute.addElement(o);
				}
				itemInInventory_HAIRMOD_PROTECTED.isItemDrop = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
				mVector_HAIRMOD_PROTECTED2.addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
			ShopNew_HAIRMOD_PROTECTED.text = msg.reader().readUTF();
			ShopNew_HAIRMOD_PROTECTED.nameshop = msg.reader().readUTF();
			ShopNew_HAIRMOD_PROTECTED.gI().ItemShopNew = mVector_HAIRMOD_PROTECTED2;
			PauseMenu_HAIRMOD_PROTECTED.gI().showShopNew(0);
		}
		catch (Exception)
		{
		}
	}

	public void onCountDown(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			sbyte b = msg.reader().readByte();
			short id = msg.reader().readShort();
			string tile = msg.reader().readUTF();
			int num = msg.reader().readInt();
			short num2 = -1;
			sbyte b2 = -1;
			try
			{
				num2 = msg.reader().readShort();
				b2 = msg.reader().readByte();
			}
			catch (Exception)
			{
				num2 = -1;
				b2 = -1;
			}
			long time = mSystem_HAIRMOD_PROTECTED.currentTimeMillis() + num * 1000;
			if (b != -1)
			{
				if (num > 0)
				{
					Actor_HAIRMOD_PROTECTED actor_HAIRMOD_PROTECTED = findActorByID_Cat(id, b);
					if (actor_HAIRMOD_PROTECTED != null)
					{
						objcountDown = actor_HAIRMOD_PROTECTED;
					}
					charcountdonw = new CharCountDown_HAIRMOD_PROTECTED(time, tile);
				}
				if (num == 0)
				{
					charcountdonw = null;
					objcountDown = null;
				}
			}
			if (b != -1 || num < 0)
			{
				return;
			}
			TimecountDown_HAIRMOD_PROTECTED timecountDown_HAIRMOD_PROTECTED = findTime(id);
			int num3 = num;
			if (timecountDown_HAIRMOD_PROTECTED == null)
			{
				timecountDown_HAIRMOD_PROTECTED = new TimecountDown_HAIRMOD_PROTECTED(id, num2, num3, tile, b2);
				VecTime.addElement(timecountDown_HAIRMOD_PROTECTED);
			}
			if (timecountDown_HAIRMOD_PROTECTED != null)
			{
				timecountDown_HAIRMOD_PROTECTED.tile = tile;
				timecountDown_HAIRMOD_PROTECTED.idIcon = num2;
				timecountDown_HAIRMOD_PROTECTED.setsecond(num3);
				if (b2 == -2)
				{
					timecountDown_HAIRMOD_PROTECTED.wantdestroy = true;
				}
			}
		}
		catch (Exception)
		{
		}
	}

	public void onTuBinh(Message_HAIRMOD_PROTECTED msg)
	{
		TuBinhScr_HAIRMOD_PROTECTED.gI().setData(msg);
	}

	public static void createNewSkill(LiveActor_HAIRMOD_PROTECTED from, LiveActor_HAIRMOD_PROTECTED to, int power, sbyte eff, int imgIndex)
	{
		Arrow_HAIRMOD_PROTECTED arrow_HAIRMOD_PROTECTED = new Arrow_HAIRMOD_PROTECTED(imgIndex);
		arrow_HAIRMOD_PROTECTED.set(0, from.x, from.y, power, eff, from, to);
		switch (imgIndex)
		{
		case 10:
			arrow_HAIRMOD_PROTECTED.typeEffEnd = 46;
			break;
		case 11:
			arrow_HAIRMOD_PROTECTED.typeEffEnd = 45;
			break;
		case 12:
			arrow_HAIRMOD_PROTECTED.typeEffEnd = 30;
			break;
		}
		arrowsUp.addElement(arrow_HAIRMOD_PROTECTED);
	}

	public static Effect_HAIRMOD_PROTECTED creatEff(LiveActor_HAIRMOD_PROTECTED ac, int x, int y, int type)
	{
		Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = new Effect_HAIRMOD_PROTECTED(x, y, type);
		if (ac != null)
		{
			effect_HAIRMOD_PROTECTED.myChar = ac;
			if (ac.getHorse() != -1)
			{
				effect_HAIRMOD_PROTECTED.yadd = -5;
			}
		}
		switch (type)
		{
		case 41:
			effect_HAIRMOD_PROTECTED.loop = 2;
			effect_HAIRMOD_PROTECTED.yadd -= 6;
			break;
		case 42:
			effect_HAIRMOD_PROTECTED.loop = 2;
			effect_HAIRMOD_PROTECTED.yadd -= 6;
			break;
		case 43:
			effect_HAIRMOD_PROTECTED.loop = 3;
			effect_HAIRMOD_PROTECTED.yadd -= 6;
			break;
		case 44:
		case 47:
		case 57:
			effect_HAIRMOD_PROTECTED.loop = 1;
			effect_HAIRMOD_PROTECTED.y -= 6;
			effect_HAIRMOD_PROTECTED.yadd -= 6;
			break;
		}
		return effect_HAIRMOD_PROTECTED;
	}

	public void onNewEffect(Message_HAIRMOD_PROTECTED msg)
	{
		int[] array = new int[4] { 41, 42, 43, 56 };
		int[] array2 = new int[4] { 12, 10, 11, 15 };
		int[] array3 = new int[4] { 44, 44, 47, 57 };
		try
		{
			short id = msg.reader().readShort();
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)getActor(id);
			if (liveActor_HAIRMOD_PROTECTED == null)
			{
				return;
			}
			sbyte b = msg.reader().readByte();
			sbyte b2 = msg.reader().readByte();
			sbyte b3 = msg.reader().readByte();
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED4 = new mVector_HAIRMOD_PROTECTED();
			for (int i = 0; i < b3; i++)
			{
				short id2 = msg.reader().readShort();
				int num = msg.reader().readInt();
				int num2 = msg.reader().readInt();
				if (b == Actor_HAIRMOD_PROTECTED.CAT_PLAYER)
				{
					Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED = (Char_HAIRMOD_PROTECTED)getActor(id2);
					if (char_HAIRMOD_PROTECTED != null)
					{
						mVector_HAIRMOD_PROTECTED3.addElement(num + string.Empty);
						mVector_HAIRMOD_PROTECTED2.addElement(char_HAIRMOD_PROTECTED);
						mVector_HAIRMOD_PROTECTED4.addElement(num2 + string.Empty);
						for (int j = 0; j < 3; j++)
						{
							Char_HAIRMOD_PROTECTED char_HAIRMOD_PROTECTED2 = new Char_HAIRMOD_PROTECTED();
							char_HAIRMOD_PROTECTED2.x = (short)(char_HAIRMOD_PROTECTED.x + r.nextInt() % 10 * 16);
							char_HAIRMOD_PROTECTED2.y = (short)(char_HAIRMOD_PROTECTED.y + r.nextInt() % 10 * 16);
							mVector_HAIRMOD_PROTECTED2.addElement(char_HAIRMOD_PROTECTED2);
							mVector_HAIRMOD_PROTECTED3.addElement(num + string.Empty);
							mVector_HAIRMOD_PROTECTED4.addElement(num2 + string.Empty);
						}
					}
				}
				else
				{
					if (b != Actor_HAIRMOD_PROTECTED.CAT_MONSTER)
					{
						continue;
					}
					Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED = findMonsterByID(id2);
					if (monster_HAIRMOD_PROTECTED != null)
					{
						mVector_HAIRMOD_PROTECTED3.addElement(num + string.Empty);
						mVector_HAIRMOD_PROTECTED2.addElement(monster_HAIRMOD_PROTECTED);
						mVector_HAIRMOD_PROTECTED4.addElement(num2 + string.Empty);
						for (int k = 0; k < 3; k++)
						{
							Monster_HAIRMOD_PROTECTED monster_HAIRMOD_PROTECTED2 = new Monster_HAIRMOD_PROTECTED();
							monster_HAIRMOD_PROTECTED2.x = (short)(monster_HAIRMOD_PROTECTED.x + r.nextInt() % 10 * 16);
							monster_HAIRMOD_PROTECTED2.y = (short)(monster_HAIRMOD_PROTECTED.y + r.nextInt() % 10 * 16);
							mVector_HAIRMOD_PROTECTED2.addElement(monster_HAIRMOD_PROTECTED2);
							mVector_HAIRMOD_PROTECTED3.addElement(num + string.Empty);
							mVector_HAIRMOD_PROTECTED4.addElement(num2 + string.Empty);
						}
					}
				}
			}
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = creatEff(liveActor_HAIRMOD_PROTECTED, liveActor_HAIRMOD_PROTECTED.x, liveActor_HAIRMOD_PROTECTED.y, array[b2]);
			effect_HAIRMOD_PROTECTED.subEff = new SubEff_HAIRMOD_PROTECTED();
			effect_HAIRMOD_PROTECTED.subEff.ac = liveActor_HAIRMOD_PROTECTED;
			effect_HAIRMOD_PROTECTED.subEff.target = mVector_HAIRMOD_PROTECTED2;
			effect_HAIRMOD_PROTECTED.subEff.idEff = array3[b2];
			effect_HAIRMOD_PROTECTED.subEff.power = mVector_HAIRMOD_PROTECTED3;
			effect_HAIRMOD_PROTECTED.subEff.hp = mVector_HAIRMOD_PROTECTED4;
			effect_HAIRMOD_PROTECTED.subEff.eff = 0;
			effect_HAIRMOD_PROTECTED.subEff.indeximg = array2[b2];
			EffectManager_HAIRMOD_PROTECTED.hiEffects.addElement(effect_HAIRMOD_PROTECTED);
		}
		catch (Exception)
		{
		}
	}

	public void onAutoImbue(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			if (msg.reader().readByte() == 0)
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(1, false);
				return;
			}
			string infoPoup = msg.reader().readUTF();
			sbyte b = msg.reader().readByte();
			if (Canvas_HAIRMOD_PROTECTED.currentScreen == ScreenDapDo_HAIRMOD_PROTECTED.gI())
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().infoPoup = infoPoup;
				ScreenDapDo_HAIRMOD_PROTECTED.gI().doFinishInbue();
				Canvas_HAIRMOD_PROTECTED.endDlg();
			}
			if (b == 1)
			{
				ScreenDapDo_HAIRMOD_PROTECTED.gI().resetSelect();
				ScreenDapDo_HAIRMOD_PROTECTED.isFinishImbue = true;
			}
		}
		catch (Exception)
		{
		}
	}

	public void onTachNguyenLieu(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			switch (msg.reader().readByte())
			{
			case 0:
				ScreenDapDo_HAIRMOD_PROTECTED.gI().switchToMe(this);
				ScreenDapDo_HAIRMOD_PROTECTED.gI().setSizeCell(0, false);
				break;
			case 4:
				Canvas_HAIRMOD_PROTECTED.currentScreen.startXayBot(msg.reader().readShort(), msg.reader().readByte(), msg.reader().readByte());
				break;
			}
		}
		catch (Exception)
		{
		}
	}

	public void onItemShopOnline(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			sbyte b = msg.reader().readByte();
			if (b == -1)
			{
				Canvas_HAIRMOD_PROTECTED.shop.switchToMe(Canvas_HAIRMOD_PROTECTED.currentScreen);
				return;
			}
			sbyte b2 = b;
			switch (b)
			{
			case 3:
				b2 = 4;
				break;
			case 4:
				b2 = 5;
				break;
			}
			((mVector_HAIRMOD_PROTECTED)ShopOnline_HAIRMOD_PROTECTED.ALL_ITEM.elementAt(b2)).removeAllElements();
			sbyte b3 = msg.reader().readByte();
			for (int i = 0; i < b3; i++)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = new ItemInInventory_HAIRMOD_PROTECTED();
				itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
				itemInInventory_HAIRMOD_PROTECTED.clazz = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.idTemplate = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.plusTemplate = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.level = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.mDurable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.durable = msg.reader().readShort();
				itemInInventory_HAIRMOD_PROTECTED.numKham = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.totalKham = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.colorName = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.heItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.beKicked = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.hangItem = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.magic_physic = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.islock = msg.reader().readByte();
				itemInInventory_HAIRMOD_PROTECTED.nameCharSeal = msg.reader().readUTF();
				itemInInventory_HAIRMOD_PROTECTED.nameCharSell = msg.reader().readUTF();
				itemInInventory_HAIRMOD_PROTECTED.nameCharBet = msg.reader().readUTF();
				itemInInventory_HAIRMOD_PROTECTED.prize = msg.reader().readInt();
				itemInInventory_HAIRMOD_PROTECTED.prizeBet = msg.reader().readInt();
				itemInInventory_HAIRMOD_PROTECTED.timeEnd = msg.reader().readLong();
				itemInInventory_HAIRMOD_PROTECTED.daySell = msg.reader().readUTF();
				itemInInventory_HAIRMOD_PROTECTED.allAttribute.removeAllElements();
				sbyte b4 = msg.reader().readByte();
				for (sbyte b5 = 0; b5 < b4; b5++)
				{
					InfoAttributeItem_HAIRMOD_PROTECTED o = new InfoAttributeItem_HAIRMOD_PROTECTED(msg.reader().readUnsignedByte(), msg.reader().readShort());
					itemInInventory_HAIRMOD_PROTECTED.allAttribute.addElement(o);
				}
				((mVector_HAIRMOD_PROTECTED)ShopOnline_HAIRMOD_PROTECTED.ALL_ITEM.elementAt(b2)).addElement(itemInInventory_HAIRMOD_PROTECTED);
			}
			if (b == 0)
			{
				Canvas_HAIRMOD_PROTECTED.shop.setItemInventory();
			}
			if (b == 3)
			{
				ShopOnline_HAIRMOD_PROTECTED.money = msg.reader().readLong();
			}
			ShopOnline_HAIRMOD_PROTECTED.nPage[b2] = msg.reader().readShort();
			if (ShopOnline_HAIRMOD_PROTECTED.indexPage[b2] >= ShopOnline_HAIRMOD_PROTECTED.nPage[b2])
			{
				ShopOnline_HAIRMOD_PROTECTED.indexPage[b2] = 0;
			}
		}
		catch (Exception)
		{
		}
	}

	public void onTemplateInfo(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			short num = msg.reader().readUnsignedByte();
			short num2 = msg.reader().readShort();
			if (num != 0)
			{
				return;
			}
			sbyte b = msg.reader().readByte();
			sbyte[] array = new sbyte[b];
			for (int i = 0; i < array.Length; i++)
			{
				array[i] = msg.reader().readByte();
			}
			Res_HAIRMOD_PROTECTED.loadMonster(num2, array);
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].name = msg.reader().readUTF();
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].he = msg.reader().readByte();
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].maxhp = msg.reader().readInt();
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].palate = msg.reader().readByte();
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].spalate = msg.reader().readByte();
			sbyte b2 = msg.reader().readByte();
			Res_HAIRMOD_PROTECTED.monsterTemplates[num2].isNewMonster = b2;
			if (b2 == 1)
			{
				sbyte[] array2 = new sbyte[msg.reader().readShort()];
				for (int j = 0; j < array2.Length; j++)
				{
					array2[j] = msg.reader().readByte();
				}
				Res_HAIRMOD_PROTECTED.monsterTemplates[num2].doSetDataMonsterNew(array2);
			}
		}
		catch (Exception)
		{
		}
	}

	public void onDynamicEffect(Message_HAIRMOD_PROTECTED msg)
	{
		try
		{
			sbyte b = msg.reader().readByte();
			short id = msg.reader().readShort();
			short num = msg.reader().readShort();
			long timeExist = msg.reader().readLong();
			int num2 = msg.reader().readByte() * 16;
			int num3 = msg.reader().readByte() * 16;
			sbyte b2 = msg.reader().readByte();
			sbyte b3 = msg.reader().readByte();
			LiveActor_HAIRMOD_PROTECTED liveActor_HAIRMOD_PROTECTED = (LiveActor_HAIRMOD_PROTECTED)((b2 != 0) ? findMonsterByID(num) : findCharByID(num));
			DynamicEffect_HAIRMOD_PROTECTED dynamicEffect_HAIRMOD_PROTECTED = new DynamicEffect_HAIRMOD_PROTECTED(id);
			dynamicEffect_HAIRMOD_PROTECTED.timeExist = timeExist;
			dynamicEffect_HAIRMOD_PROTECTED.x = (short)num2;
			dynamicEffect_HAIRMOD_PROTECTED.y = (short)num3;
			dynamicEffect_HAIRMOD_PROTECTED.catagory = b2;
			dynamicEffect_HAIRMOD_PROTECTED.stop = msg.reader().readByte();
			int add_remove = msg.reader().readByte();
			int num4 = 0;
			try
			{
				num4 = msg.reader().readInt();
				dynamicEffect_HAIRMOD_PROTECTED.dam = num4;
			}
			catch (Exception)
			{
			}
			if (liveActor_HAIRMOD_PROTECTED != null && b == 1)
			{
				if (b3 == 0)
				{
					liveActor_HAIRMOD_PROTECTED.addDynamicEffBuffBottom(dynamicEffect_HAIRMOD_PROTECTED, add_remove);
				}
				else
				{
					liveActor_HAIRMOD_PROTECTED.addDynamicEffBuffTop(dynamicEffect_HAIRMOD_PROTECTED, add_remove);
				}
			}
			else if (num > 32000 || b == 0)
			{
				if (liveActor_HAIRMOD_PROTECTED != null)
				{
					dynamicEffect_HAIRMOD_PROTECTED.x = liveActor_HAIRMOD_PROTECTED.x;
					dynamicEffect_HAIRMOD_PROTECTED.y = liveActor_HAIRMOD_PROTECTED.y;
				}
				actors.addElement(dynamicEffect_HAIRMOD_PROTECTED);
			}
		}
		catch (Exception)
		{
		}
	}

	public static void paint12plus(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.drawImage(img12plus, 0, (Canvas_HAIRMOD_PROTECTED.currentScreen != Canvas_HAIRMOD_PROTECTED.gameScr) ? 15 : 80);
	}

	public static void startSkillEffect(LiveActor_HAIRMOD_PROTECTED c, int idSkill, long timeExist, int top_bottom, int power, int bymap)
	{
		DynamicEffect_HAIRMOD_PROTECTED dynamicEffect_HAIRMOD_PROTECTED = new DynamicEffect_HAIRMOD_PROTECTED(idSkill);
		dynamicEffect_HAIRMOD_PROTECTED.timeExist = timeExist;
		dynamicEffect_HAIRMOD_PROTECTED.x = c.x;
		dynamicEffect_HAIRMOD_PROTECTED.y = c.y;
		dynamicEffect_HAIRMOD_PROTECTED.catagory = c.catagory;
		dynamicEffect_HAIRMOD_PROTECTED.stop = 0;
		dynamicEffect_HAIRMOD_PROTECTED.power = power;
		if (bymap == 0)
		{
			if (top_bottom == 0)
			{
				c.addDynamicEffBuffBottom(dynamicEffect_HAIRMOD_PROTECTED, 0);
			}
			else
			{
				c.addDynamicEffBuffTop(dynamicEffect_HAIRMOD_PROTECTED, 0);
			}
		}
		else
		{
			Canvas_HAIRMOD_PROTECTED.gameScr.actors.addElement(dynamicEffect_HAIRMOD_PROTECTED);
		}
	}
}
