public class ScreenItemTim_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public const sbyte TYPE2 = 0;

	public const sbyte TYPE4 = 1;

	public const sbyte TypeXayBot = 5;

	public const sbyte TYPE_ANIMAL = 2;

	public const sbyte TYPE_LOCK_ITEM = 3;

	public const sbyte TYPE_EP_ITEM_TIM = 6;

	public static ScreenItemTim_HAIRMOD_PROTECTED me;

	public static int hImg = 80;

	public static int wImg = 40;

	public static int wCell;

	public static int hCell;

	public static int xDr;

	public static int yDr;

	public static mVector_HAIRMOD_PROTECTED vnguyenLieu = new mVector_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScrollItemInventory = new Scroll_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScrollShowText = new Scroll_HAIRMOD_PROTECTED();

	public Scroll_HAIRMOD_PROTECTED mScrollNguyenLieu = new Scroll_HAIRMOD_PROTECTED();

	public int index = -1;

	public int xSh;

	public int ySh;

	public int wSh;

	public int hSh;

	public int wTab = 28;

	public int selectItemInventory;

	public int lastIndex1;

	public int h0;

	public int indexTabNguyenLieu;

	public int lastIndex2;

	public MyScreen_HAIRMOD_PROTECTED last;

	public string[] arrShowText = new string[1] { string.Empty };

	public string tile = "Cộng thuộc tính";

	public mVector_HAIRMOD_PROTECTED mShot = new mVector_HAIRMOD_PROTECTED();

	public bool isShowText;

	public sbyte type;

	public sbyte frame;

	public sbyte selectMenuItem;

	public bool createBot;

	public bool isdestroyitem;

	public bool isXayBot;

	public bool isShot;

	public bool isDap;

	public int xto;

	public int yto;

	public int hclip;

	public int hclip1;

	public int size = 22;

	private Command_HAIRMOD_PROTECTED cmdClose;

	private Command_HAIRMOD_PROTECTED cmdErr;

	private Command_HAIRMOD_PROTECTED cmdView;

	private Command_HAIRMOD_PROTECTED cmdContinue;

	private Command_HAIRMOD_PROTECTED cmdXay;

	private Command_HAIRMOD_PROTECTED cmdWaiting;

	private Command_HAIRMOD_PROTECTED cmdDapItemTim;

	public mVector_HAIRMOD_PROTECTED ListTtem = new mVector_HAIRMOD_PROTECTED();

	public bool[] CheckHaveItemCell = new bool[6];

	public bool[] CheckPaintItem = new bool[6];

	public static sbyte COLOR_WHITE = 0;

	public static sbyte COLOR_GREEN = 1;

	public static sbyte COLOR_RED = 2;

	public static sbyte COLOR_BLUE = 3;

	public static sbyte COLOR_MAGENTA = 4;

	public static sbyte COLOR_YELLOW = 5;

	public mVector_HAIRMOD_PROTECTED LisItemSelect = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED mEffect = new mVector_HAIRMOD_PROTECTED();

	public mVector_HAIRMOD_PROTECTED lazer = new mVector_HAIRMOD_PROTECTED();

	public static mVector_HAIRMOD_PROTECTED listItem = new mVector_HAIRMOD_PROTECTED();

	private int[] xItem;

	private int[] yItem;

	private int[] xTo;

	private int[] yTo;

	private bool isEp;

	private bool isShowPopup;

	private int xPopup;

	private int yPopup;

	private string infoPoup = "Thong bao";

	private bool DropItem;

	public sbyte currentRow;

	public sbyte timeDelay;

	public sbyte currentCell = 5;

	private int cout;

	private int selectItem;

	private static ItemInInventory_HAIRMOD_PROTECTED itemSelect;

	public ItemInInventory_HAIRMOD_PROTECTED selecItemNL1;

	public ItemInInventory_HAIRMOD_PROTECTED selecItemNL3;

	public ItemInInventory_HAIRMOD_PROTECTED selecItemNL4;

	public ItemInInventory_HAIRMOD_PROTECTED selecItemNL6;

	private static GemTemplate_HAIRMOD_PROTECTED selectGem = null;

	private static GemTemplate_HAIRMOD_PROTECTED selectGemBH = null;

	private static GemTemplate_HAIRMOD_PROTECTED selectGemMM = null;

	private static GemTemplate_HAIRMOD_PROTECTED selecItemNL2 = null;

	public static bool LKDLock;

	public static bool MMLock;

	public static bool BHLock;

	public static bool isFinishImbue = true;

	public static short idLkd = -1;

	public static short idMM = -1;

	public static short idBH = -1;

	public static short itemID;

	private mVector_HAIRMOD_PROTECTED mDestroy = new mVector_HAIRMOD_PROTECTED();

	public int timeDlay = 15;

	public int dem = -1;

	public int tick;

	public short id_bot;

	public sbyte typeResult;

	public sbyte soluong = 1;

	public ScreenItemTim_HAIRMOD_PROTECTED()
	{
		wCell = Canvas_HAIRMOD_PROTECTED.w - 10;
		hCell = Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 10;
		if (wCell > 176)
		{
			wCell = 176;
		}
		if (hCell > 220)
		{
			hCell = 220;
		}
		if (Canvas_HAIRMOD_PROTECTED.w >= 320 && Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			wCell = 300;
		}
		xDr = Canvas_HAIRMOD_PROTECTED.w / 2 - wCell / 2;
		yDr = (Canvas_HAIRMOD_PROTECTED.h - MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT) / 2 - hCell / 2;
		if (yDr < 5)
		{
			yDr = 5;
		}
		if (xDr < 5)
		{
			xDr = 5;
		}
		wSh = wCell;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone && wCell == 300)
		{
			xSh = xDr + wCell / 2;
			ySh = yDr + 55;
			wSh = wCell / 2 - 8;
			hSh = hCell - 54;
		}
		h0 = hCell - 54;
		cmdDapItemTim = new Command_HAIRMOD_PROTECTED("Đập");
		cmdDapItemTim.action = delegate
		{
			if (!CheckHaveItemcell())
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chưa bỏ nguyên liệu vào ô!");
			}
			else if (CheckHaveItemcell() && !isDap)
			{
				addlazeEpItemTim();
				isShot = true;
				isDap = true;
			}
		};
		cmdWaiting = new Command_HAIRMOD_PROTECTED("Xin chờ");
		cmdWaiting.action = delegate
		{
		};
		cmdXay = new Command_HAIRMOD_PROTECTED("Nghiền");
		cmdXay.action = delegate
		{
			if (itemSelect == null)
			{
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chưa bỏ trang bị vào");
			}
			else if (!isXayBot)
			{
				Out_HAIRMOD_PROTECTED.LogWarning("vao day ne cha");
				GameService_HAIRMOD_PROTECTED.gI().doRequestTachNguyenLIeu(itemSelect.itemID, 4, 0, 0);
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
			}
		};
		cmdClose = new Command_HAIRMOD_PROTECTED("Đóng");
		cmdClose.action = delegate
		{
			if (isShowText)
			{
				isShowText = false;
				center = cmdView;
				mScrollShowText.clear();
				arrShowText = new string[1] { string.Empty };
				if (isEp)
				{
					left = cmdContinue;
				}
				else if (type == 5)
				{
					left = cmdXay;
				}
				else if (type == 6)
				{
					left = cmdDapItemTim;
				}
			}
			else
			{
				resetData();
				last.switchToMe();
			}
		};
		cmdView = new Command_HAIRMOD_PROTECTED((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? string.Empty : string.Empty);
		cmdView.action = delegate
		{
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED2.addElement(new Command_HAIRMOD_PROTECTED("Bỏ vào")
			{
				action = delegate
				{
					if (type == 5 && isXayBot)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể chọn trong quá trình luyện");
					}
					else if (type == 6 && isDap)
					{
						Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể chọn trong quá trình luyện");
					}
					else
					{
						if (type == 6 && listItem.size() > 0)
						{
							if (currentCell == 1)
							{
								selecItemNL1 = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
								if (checKselecitem(selecItemNL1))
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Item Da duoc chon");
									selecItemNL1 = null;
								}
							}
							if (currentCell == 2 && selecItemNL2 != null)
							{
								CheckPaintItem[1] = true;
							}
							if (currentCell == 3)
							{
								selecItemNL3 = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
								if (checKselecitem(selecItemNL3))
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Item Da duoc chon");
									selecItemNL3 = null;
								}
							}
							if (currentCell == 4)
							{
								selecItemNL4 = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
								if (checKselecitem(selecItemNL4))
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Item Da duoc chon");
									selecItemNL4 = null;
								}
							}
							if (currentCell == 5)
							{
								itemSelect = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
								if (checKselecitem(itemSelect))
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Item Da duoc chon");
									itemSelect = null;
								}
							}
							if (currentCell == 6)
							{
								selecItemNL6 = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
								if (checKselecitem(selecItemNL6))
								{
									Canvas_HAIRMOD_PROTECTED.startOKDlg("Item Da duoc chon", false);
									selecItemNL6 = null;
								}
							}
						}
						if (type == 5)
						{
							if (createBot)
							{
								createBot = false;
							}
							if (hclip <= 0)
							{
								hclip = size;
							}
							if (hclip1 > 0)
							{
								hclip1 = 0;
							}
						}
						if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
						{
							timeDelay = 5;
						}
						if (currentRow == 1 && type != 6 && listItem.size() > 0 && selectItemInventory != -1 && selectItemInventory <= listItem.size() - 1 && !isDap)
						{
							if (type == 1 && !isFinishImbue)
							{
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.");
							}
							else
							{
								itemSelect = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
							}
						}
					}
				}
			});
			if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				mVector_HAIRMOD_PROTECTED2.addElement(new Command_HAIRMOD_PROTECTED("Thông tin")
				{
					action = delegate
					{
						if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
						{
							timeDelay = 5;
						}
						if (listItem.size() > 0 && currentRow == 1 && type != 6)
						{
							doView();
						}
						else if (listItem.size() > 0 && type == 6)
						{
							doView();
						}
					}
				});
			}
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
			Canvas_HAIRMOD_PROTECTED.currentDialog = null;
		};
		right = cmdClose;
		center = cmdView;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			index = 0;
		}
		setData(null);
		lastIndex1 = 0;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			lastIndex1 = -1;
			currentRow = -1;
			lastIndex2 = -1;
		}
		lastIndex2 = 0;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			lastIndex2 = -1;
		}
	}

	public static ScreenItemTim_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ScreenItemTim_HAIRMOD_PROTECTED());
	}

	public void setData(Message_HAIRMOD_PROTECTED msg)
	{
	}

	public override void startXayBot(int idbot, int type, int sl)
	{
		Canvas_HAIRMOD_PROTECTED.endDlg();
		id_bot = (short)idbot;
		typeResult = (sbyte)type;
		soluong = (sbyte)sl;
		addlazer();
		isXayBot = true;
	}

	public void resetData()
	{
		mScrollItemInventory.clear();
		LisItemSelect.removeAllElements();
		mScrollNguyenLieu.clear();
		mScrollShowText.clear();
		mEffect.removeAllElements();
		lazer.removeAllElements();
		mShot.removeAllElements();
		vnguyenLieu.removeAllElements();
		index = (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? (-1) : 0);
		selectItemInventory = 0;
		indexTabNguyenLieu = 0;
		isShowPopup = false;
		createBot = false;
		currentRow = 0;
		lastIndex1 = 0;
		lastIndex2 = 0;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			currentRow = -1;
			lastIndex1 = -1;
			lastIndex2 = -1;
		}
		lastIndex2 = 0;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			lastIndex2 = -1;
		}
	}

	private void addlazeEpItemTim()
	{
		WeaponsLazer_HAIRMOD_PROTECTED weaponsLazer_HAIRMOD_PROTECTED = null;
		weaponsLazer_HAIRMOD_PROTECTED = new WeaponsLazer_HAIRMOD_PROTECTED(xItem[6] + size / 2, yItem[6], xItem[1] + size / 2, yItem[1], 100, 3, 3);
		lazer.addElement(weaponsLazer_HAIRMOD_PROTECTED);
		weaponsLazer_HAIRMOD_PROTECTED = null;
		for (int i = 1; i < xItem.Length - 1; i++)
		{
			weaponsLazer_HAIRMOD_PROTECTED = new WeaponsLazer_HAIRMOD_PROTECTED(xItem[i] + size / 2, yItem[i], xItem[i + 1] + size / 2, yItem[i + 1], 100, 3, 6 + i * 2);
			lazer.addElement(weaponsLazer_HAIRMOD_PROTECTED);
			weaponsLazer_HAIRMOD_PROTECTED = null;
			dem = 6 + i * 2;
		}
	}

	private void addNguyenlieu()
	{
		for (int i = 0; i < vnguyenLieu.size(); i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(i);
			if (gemTemplate_HAIRMOD_PROTECTED != null)
			{
			}
		}
	}

	private bool CheckHaveItemcell()
	{
		for (int i = 0; i < CheckHaveItemCell.Length; i++)
		{
			if (!CheckHaveItemCell[i])
			{
				return false;
			}
		}
		return true;
	}

	private void resetCheck()
	{
		selectGem = null;
		cmdDapItemTim.caption = "Đập";
		center = cmdView;
		isShot = false;
		tick = 0;
		selecItemNL1 = null;
		selecItemNL2 = null;
		selecItemNL3 = null;
		selecItemNL4 = null;
		selecItemNL6 = null;
		LisItemSelect.removeAllElements();
		for (int i = 0; i < CheckHaveItemCell.Length; i++)
		{
			CheckHaveItemCell[i] = false;
			CheckPaintItem[i] = false;
		}
		itemSelect = null;
		getListGem(true, false);
	}

	public void resetSelect()
	{
		if (type == 5)
		{
			center = cmdView;
		}
		isShot = false;
		tick = 0;
		isDap = false;
		resetCheck();
		currentCell = 5;
		isXayBot = false;
		selectGemBH = null;
		selectGemMM = null;
		itemSelect = null;
		getListGem(true, false);
	}

	public void getListGem(bool getItem, bool recheck)
	{
		vnguyenLieu.removeAllElements();
		vnguyenLieu.removeAllElements();
		if (getItem)
		{
			listItem.removeAllElements();
		}
		mVector_HAIRMOD_PROTECTED gemItem = MainChar_HAIRMOD_PROTECTED.gemItem;
		int num = gemItem.size();
		int num2 = 0;
		int num3 = 0;
		int num4 = 0;
		int num5 = 0;
		for (int i = 0; i < num; i++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)gemItem.elementAt(i);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			if (type == 0)
			{
				if (gemByID.type == 6)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED;
						num2 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED);
				}
			}
			else if (type == 2)
			{
				if (gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_LOCK_ANIMAL)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED;
						num5 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED);
				}
			}
			else if (type == 3)
			{
				if (gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_LOCK_ITEM)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED;
						num5 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED);
				}
			}
			else if (gemByID.type == 0)
			{
				if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGem.ilock)
				{
					selectGem = gemTemplate_HAIRMOD_PROTECTED;
					num2 = 1;
				}
				else if (selectGem == null && recheck && idLkd == gemTemplate_HAIRMOD_PROTECTED.id && !LKDLock)
				{
					selectGem = gemTemplate_HAIRMOD_PROTECTED;
					num2 = 1;
				}
				if (selectGemBH != null && selectGemBH.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGemBH.ilock)
				{
					selectGemBH = gemTemplate_HAIRMOD_PROTECTED;
					num3 = 1;
				}
				else if (selectGemBH == null && recheck && idBH == gemTemplate_HAIRMOD_PROTECTED.id && !BHLock)
				{
					selectGemBH = gemTemplate_HAIRMOD_PROTECTED;
					num3 = 1;
				}
				if (selectGemMM != null && selectGemMM.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGemMM.ilock)
				{
					selectGemMM = gemTemplate_HAIRMOD_PROTECTED;
					num4 = 1;
				}
				else if (selectGemMM == null && recheck && idMM == gemTemplate_HAIRMOD_PROTECTED.id && !MMLock)
				{
					selectGemMM = gemTemplate_HAIRMOD_PROTECTED;
					num4 = 1;
				}
				vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED);
			}
		}
		gemItem = MainChar_HAIRMOD_PROTECTED.gemItemKhoa;
		num = gemItem.size();
		int num6 = 0;
		int num7 = 0;
		int num8 = 0;
		int num9 = 0;
		for (int j = 0; j < num; j++)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = (GemTemplate_HAIRMOD_PROTECTED)gemItem.elementAt(j);
			GemTemp_HAIRMOD_PROTECTED gemByID2 = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED2.id);
			if (type == 0)
			{
				if (gemByID2.type == 6)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED2;
						num6 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED2);
					gemTemplate_HAIRMOD_PROTECTED2.ilock = true;
				}
			}
			else if (type == 2)
			{
				if (gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_LOCK_ANIMAL)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED2;
						num9 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED2);
					gemTemplate_HAIRMOD_PROTECTED2.ilock = true;
				}
			}
			else if (type == 3)
			{
				if (gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_LOCK_ITEM)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGem.ilock)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED2;
						num9 = 1;
					}
					vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED2);
					gemTemplate_HAIRMOD_PROTECTED2.ilock = true;
				}
			}
			else if (gemByID2.type == 0)
			{
				if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGem.ilock)
				{
					selectGem = gemTemplate_HAIRMOD_PROTECTED2;
					num6 = 1;
				}
				else if (selectGem == null && recheck && idLkd == gemTemplate_HAIRMOD_PROTECTED2.id && LKDLock)
				{
					selectGem = gemTemplate_HAIRMOD_PROTECTED2;
					num6 = 1;
				}
				if (selectGemBH != null && selectGemBH.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGemBH.ilock)
				{
					selectGemBH = gemTemplate_HAIRMOD_PROTECTED2;
					num7 = 1;
				}
				else if (selectGemBH == null && recheck && idBH == gemTemplate_HAIRMOD_PROTECTED2.id && BHLock)
				{
					selectGemBH = gemTemplate_HAIRMOD_PROTECTED2;
					num7 = 1;
				}
				if (selectGemMM != null && selectGemMM.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGemMM.ilock)
				{
					selectGemMM = gemTemplate_HAIRMOD_PROTECTED2;
					num8 = 1;
				}
				else if (selectGemMM == null && recheck && idMM == gemTemplate_HAIRMOD_PROTECTED2.id && MMLock)
				{
					selectGemMM = gemTemplate_HAIRMOD_PROTECTED2;
					num8 = 1;
				}
				if (type == 6)
				{
					selecItemNL2 = gemTemplate_HAIRMOD_PROTECTED2;
				}
				vnguyenLieu.addElement(gemTemplate_HAIRMOD_PROTECTED2);
				gemTemplate_HAIRMOD_PROTECTED2.ilock = true;
			}
		}
		getItem = true;
		if (type == 0)
		{
			if (num2 == 0 && num6 == 0)
			{
				selectGem = null;
			}
			if (num3 == 0 && num7 == 0)
			{
				selectGemBH = null;
			}
			if (num4 == 0 && num8 == 0)
			{
				selectGemMM = null;
			}
		}
		if (getItem)
		{
			if (type == 0)
			{
				listItem = ItemInInventory_HAIRMOD_PROTECTED.getAllItem(COLOR_GREEN);
			}
			else if (type == 2)
			{
				listItem = ItemInInventory_HAIRMOD_PROTECTED.getAllItemAnimal(COLOR_GREEN, 1);
			}
			else if (type == 3)
			{
				listItem = ItemInInventory_HAIRMOD_PROTECTED.getAllItemCharExcepColor(COLOR_WHITE);
			}
			else
			{
				listItem = ItemInInventory_HAIRMOD_PROTECTED.getAllItemCharDrop();
			}
		}
		for (int k = 0; k < listItem.size(); k++)
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(k);
			if (itemID == itemInInventory_HAIRMOD_PROTECTED.itemID)
			{
				itemSelect = itemInInventory_HAIRMOD_PROTECTED;
				break;
			}
		}
	}

	public void setSizeCell(sbyte type, bool isContinue)
	{
		int num = 44;
		this.type = type;
		int num2 = xDr + wCell / 2 - num;
		int num3 = yDr + hCell / 2 - 35;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			num2 = xDr + wCell / 4 - num;
		}
		switch (type)
		{
		case 6:
		{
			currentRow = 1;
			cmdDapItemTim.caption = "Đập";
			tile = "Đập đồ tím";
			int num5 = yDr + 27 + (h0 - 28) / 2;
			int num6 = hCell / 4;
			int num7 = 30;
			xItem = new int[7];
			yItem = new int[7];
			xItem[0] = num2 + num - 10;
			yItem[0] = num5 - size / 2 + 20;
			currentRow = -1;
			for (int i = 1; i < 7; i++)
			{
				xItem[i] = Util_HAIRMOD_PROTECTED.Cos(num7) * num6 / 1024 + xItem[0];
				yItem[i] = Util_HAIRMOD_PROTECTED.Sin(num7) * num6 / 1024 + yItem[0];
				num7 += 60;
			}
			break;
		}
		case 5:
		{
			int num4 = yDr + 27 + (h0 - 28) / 2;
			cmdXay.caption = "Nghiền";
			tile = "Nghiền Bột";
			currentRow = 1;
			xItem = new int[5];
			xTo = new int[4];
			yTo = new int[4];
			yItem = new int[5];
			xItem[0] = num2 + num - 11;
			xItem[1] = xItem[0] - 48;
			xItem[3] = xItem[0] + 48;
			xItem[2] = xItem[0];
			yItem[0] = num4 - size / 2 + 3;
			if (yItem[0] - 2 * size < yDr + 27)
			{
				yItem[0] = yDr + 27 + 2 * size;
			}
			yItem[2] = yItem[0] - 2 * size;
			yItem[1] = yItem[0] + 28;
			yItem[3] = yItem[0] + 28;
			xTo[0] = xItem[0];
			xTo[1] = xItem[1];
			xTo[2] = xItem[2];
			xTo[3] = xItem[3];
			yTo[0] = yItem[0];
			yTo[1] = yItem[1];
			yTo[2] = yItem[2];
			yTo[3] = yItem[3];
			xPopup = num2 + num - 1;
			yPopup = num3 + 40;
			if (Canvas_HAIRMOD_PROTECTED.isSmallScreen)
			{
				yItem[2] = yItem[0] - 2 * size + 4;
				xItem[4] = xItem[0];
				yItem[4] = yItem[0] + 40;
			}
			else
			{
				xItem[4] = xItem[0];
				yItem[4] = yItem[0] + 50;
			}
			xto = xTo[0];
			yto = yTo[0];
			hclip = size;
			hclip1 = 0;
			break;
		}
		}
		Out_HAIRMOD_PROTECTED.println("TYPE SCREENDAPDO " + type);
		cout = 0;
		switch (type)
		{
		case 5:
			if (isContinue)
			{
				currentRow = 0;
				if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
				{
					currentRow = -1;
				}
				left = cmdXay;
				isShowText = false;
				mScrollShowText.clear();
				isEp = false;
			}
			else
			{
				isEp = false;
				left = cmdXay;
			}
			break;
		case 6:
			if (isContinue)
			{
				currentRow = 0;
				if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
				{
					currentRow = -1;
				}
				left = cmdDapItemTim;
				isShowText = false;
				mScrollShowText.clear();
				isEp = false;
			}
			else
			{
				isEp = false;
				left = cmdDapItemTim;
			}
			break;
		}
		resetSelect();
		getListGem(true, false);
		if (type == 6)
		{
			addNguyenlieu();
		}
	}

	private void updatelazer()
	{
		int num = Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][10] / 2;
		for (int i = 0; i < lazer.size(); i++)
		{
			WeaponsLazer_HAIRMOD_PROTECTED weaponsLazer_HAIRMOD_PROTECTED = (WeaponsLazer_HAIRMOD_PROTECTED)lazer.elementAt(i);
			if (weaponsLazer_HAIRMOD_PROTECTED != null && type == 5)
			{
				if (hclip > 4)
				{
					addEffectBot();
				}
				if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
				{
					Effect_HAIRMOD_PROTECTED o = new Effect_HAIRMOD_PROTECTED(xItem[2] + num - 4, yto + size / 2 - 10, 54);
					mEffect.addElement(o);
				}
			}
			weaponsLazer_HAIRMOD_PROTECTED.update2();
			if (Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0)
			{
				Effect_HAIRMOD_PROTECTED o2 = new Effect_HAIRMOD_PROTECTED(weaponsLazer_HAIRMOD_PROTECTED.xlim, weaponsLazer_HAIRMOD_PROTECTED.ylim, 54);
				mEffect.addElement(o2);
			}
			if (weaponsLazer_HAIRMOD_PROTECTED.wantDestroy)
			{
				lazer.removeElement(weaponsLazer_HAIRMOD_PROTECTED);
				isdestroyitem = true;
				isShot = true;
			}
		}
	}

	private void paintListItem(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, tile, false, h0 - 28);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (currentCell != 2)
		{
			mScrollItemInventory.setStyle(listItem.size(), wTab, xDr, yDr + 28 + h0, wCell - 2, 27, false, 0);
			mScrollItemInventory.setClip(g, xDr + 2, yDr + 23 + h0, wCell - 4, 31);
			for (int i = 0; i < listItem.size(); i++)
			{
				if (selectItemInventory == i && currentRow == 1)
				{
					g.setColor(6513507);
					g.fillRect(xDr + wTab * i, yDr + 23 + h0, wTab, 28);
				}
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(i);
				itemInInventory_HAIRMOD_PROTECTED.paint(g, xDr + wTab * i + wTab / 2, yDr + 36 + h0);
				g.setColor(14527502);
				g.fillRect(xDr + wTab * i, yDr + 22 + h0, 1, 29);
			}
			g.setColor(14527502);
			g.fillRect(xDr + wTab * listItem.size(), yDr + 22 + h0, 1, 29);
		}
		if (selecItemNL2 != null && currentCell == 2)
		{
			g.setColor(6513507);
			g.fillRect(xDr + 3, yDr + 23 + h0, wTab - 3, 28);
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selecItemNL2.id).idImage, xDr + wTab / 2, yDr + 36 + h0);
			MFont_HAIRMOD_PROTECTED.smallFontColor[3].drawString(g, selecItemNL2.number + string.Empty, xDr + wTab / 2 + size / 2, yDr + 50 + h0 - MFont_HAIRMOD_PROTECTED.smallFontColor[3].getHeight(), MFont_HAIRMOD_PROTECTED.RIGHT);
			g.setColor(14527502);
			g.fillRect(xDr + wTab, yDr + 22 + h0, 1, 29);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		g.setColor(15338765);
		g.fillRect(xItem[currentCell] - 1, yItem[currentCell] - 11, size + 3, size + 2);
	}

	private void paintResult(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setClip(xItem[4] + 4, yItem[4] - 8 + hclip, size, size);
		if (typeResult == 0)
		{
			Res_HAIRMOD_PROTECTED.paintPotion(g, MainChar_HAIRMOD_PROTECTED.listPotion[id_bot].index, xItem[4] + 4, yItem[4] - 6, 0);
			return;
		}
		GameData_HAIRMOD_PROTECTED.paintIcon(g, (short)(id_bot + 6500), xItem[4] + 12, yItem[4] + 2, 3);
		MFont_HAIRMOD_PROTECTED.smallFontColor[3].drawString(g, soluong + string.Empty, xItem[4] + 18, yItem[4] + 2, MFont_HAIRMOD_PROTECTED.RIGHT);
	}

	private void addEffectBot()
	{
		int num = Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][10] / 2;
		for (int i = 0; i < 2; i++)
		{
			Effect_XayBot_HAIRMOD_PROTECTED o = new Effect_XayBot_HAIRMOD_PROTECTED(xItem[2] + num - 4, yto + size / 2 - 10, 2 * i, yItem[4] + size / 2 - 10, 0);
			mEffect.addElement(o);
			o = null;
		}
	}

	private void paintlazer(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < lazer.size(); i++)
		{
			WeaponsLazer_HAIRMOD_PROTECTED weaponsLazer_HAIRMOD_PROTECTED = (WeaponsLazer_HAIRMOD_PROTECTED)lazer.elementAt(i);
			weaponsLazer_HAIRMOD_PROTECTED.paint(g);
		}
	}

	public void addlazer()
	{
		int num = Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][10] / 2;
		center = cmdWaiting;
		cmdXay.caption = string.Empty;
		Effect_HAIRMOD_PROTECTED o = new Effect_HAIRMOD_PROTECTED(xItem[2] + num - 4, yto + size / 2 - 10, 54);
		mEffect.addElement(o);
		WeaponsLazer_HAIRMOD_PROTECTED weaponsLazer_HAIRMOD_PROTECTED = null;
		for (int i = 0; i < xItem.Length; i++)
		{
			if (i % 4 != 0)
			{
				weaponsLazer_HAIRMOD_PROTECTED = new WeaponsLazer_HAIRMOD_PROTECTED(xItem[i] + num - 2, yItem[i] + num - 10, xItem[2] + num - 2, yto + size / 2 - 10, 40, 2);
				lazer.addElement(weaponsLazer_HAIRMOD_PROTECTED);
				weaponsLazer_HAIRMOD_PROTECTED = null;
			}
		}
	}

	private void updateEffect()
	{
		for (int i = 0; i < mEffect.size(); i++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)mEffect.elementAt(i);
			effect_HAIRMOD_PROTECTED?.update();
			if (effect_HAIRMOD_PROTECTED.wantDetroy && effect_HAIRMOD_PROTECTED.type == -1)
			{
				createBot = true;
				mEffect.removeElement(effect_HAIRMOD_PROTECTED);
			}
			if (effect_HAIRMOD_PROTECTED.wantDetroy)
			{
				mEffect.removeElement(effect_HAIRMOD_PROTECTED);
			}
		}
	}

	protected void doView()
	{
		if (type == 6)
		{
			if (Char_HAIRMOD_PROTECTED.inventory.size() == 0 || selectItemInventory < 0 || selectItemInventory > listItem.size() - 1)
			{
				return;
			}
			if (currentCell == 2)
			{
				if (selecItemNL2 == null)
				{
					return;
				}
				GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(selecItemNL2.id);
				string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
				text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
				arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(text, wSh - 10);
			}
			if (currentCell != 2)
			{
				ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
				if (!itemInInventory_HAIRMOD_PROTECTED.isEnoughData)
				{
					itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
					GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(itemInInventory_HAIRMOD_PROTECTED.itemID, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
				}
				arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(itemInInventory_HAIRMOD_PROTECTED.getDescription(false), wSh - 10);
			}
		}
		if (type == 5 && listItem.size() > 0)
		{
			if (Char_HAIRMOD_PROTECTED.inventory.size() == 0 || selectItemInventory < 0 || selectItemInventory > listItem.size() - 1)
			{
				return;
			}
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED2 = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
			if (!itemInInventory_HAIRMOD_PROTECTED2.isEnoughData)
			{
				itemInInventory_HAIRMOD_PROTECTED2.isEnoughData = true;
				GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(itemInInventory_HAIRMOD_PROTECTED2.itemID, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
			}
			arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(itemInInventory_HAIRMOD_PROTECTED2.getDescription(false), wSh - 10);
		}
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone && listItem.size() > 0)
		{
			if (arrShowText.Length == 1)
			{
				hSh = 35;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - MFont_HAIRMOD_PROTECTED.normalFont[0].getHeight() / 2;
			}
			else
			{
				hSh = arrShowText.Length * MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT + 8;
				ySh = Canvas_HAIRMOD_PROTECTED.h / 2 - arrShowText.Length * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) / 2;
			}
			if (ySh < yDr)
			{
				ySh = yDr;
			}
			if (hSh > 150)
			{
				hSh = 150;
			}
			xSh = xDr;
		}
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			isShowText = true;
			center = cmdErr;
			left = cmdErr;
		}
	}

	public void paintCell(MyGraphics_HAIRMOD_PROTECTED g)
	{
		g.setColor(25695);
		int num = ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 10 : (-27));
		g.fillRect(xSh + 2, ySh + num, wSh, hSh - 8);
		g.setColor(16774720);
		g.drawRect(xSh + 2, ySh + num, wSh, hSh - 8);
	}

	public void paintShowText(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		if (wCell >= 300)
		{
			paintCell(g);
			if (!isShowText && !Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				return;
			}
		}
		else
		{
			if (!isShowText && !Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				return;
			}
			paintCell(g);
		}
		int num = (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? 24 : 0);
		mScrollShowText.setStyle(arrShowText.Length, MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 2, xSh, ySh - num, wSh - 2, hSh - 10, true, 0);
		mScrollShowText.setClip(g, xSh, ySh + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 10 : 2) - num, wSh - 2, hSh - 15);
		for (int i = 0; i < arrShowText.Length; i++)
		{
			if (!arrShowText[i].Equals(string.Empty))
			{
				int num2 = (sbyte)(arrShowText[i][0] - 48);
				int startIndex = 1;
				if (!WindowInfoScr_HAIRMOD_PROTECTED.isDegit(arrShowText[i][0]))
				{
					num2 = 0;
					startIndex = 0;
				}
				int num3 = ((num2 < 5) ? num2 : 0);
				MFont_HAIRMOD_PROTECTED.normalFont[num3].drawString(g, arrShowText[i].Substring(startIndex), xSh + 8, ySh + i * (MyScreen_HAIRMOD_PROTECTED.ITEM_HEIGHT - 3) + ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone) ? 14 : 6) - (num + (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? 2 : 0)), 0);
			}
		}
	}

	private void paintScreenXayBot(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, tile, false, h0 - 28);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		mScrollItemInventory.setStyle(listItem.size(), wTab, xDr, yDr + 28 + h0, wCell - 2, 27, false, 0);
		mScrollItemInventory.setClip(g, xDr + 2, yDr + 23 + h0, wCell - 4, 31);
		for (int i = 0; i < listItem.size(); i++)
		{
			if (selectItemInventory == i && currentRow == 1)
			{
				g.setColor(6513507);
				g.fillRect(xDr + wTab * i, yDr + 23 + h0, wTab, 28);
			}
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(i);
			itemInInventory_HAIRMOD_PROTECTED.paint(g, xDr + wTab * i + wTab / 2, yDr + 36 + h0);
			g.setColor(14527502);
			g.fillRect(xDr + wTab * i, yDr + 22 + h0, 1, 29);
			if (itemSelect != null && itemSelect.itemID == itemInInventory_HAIRMOD_PROTECTED.itemID)
			{
				g.setColor(3268607);
				g.drawRect(xDr + wTab * i + 1, yDr + 23 + h0, wTab - 2, 28);
				g.drawRect(xDr + wTab * i + 2, yDr + 24 + h0, wTab - 4, 26);
			}
		}
		g.setColor(14527502);
		g.fillRect(xDr + wTab * listItem.size(), yDr + 22 + h0, 1, 29);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
	}

	private void paintEffect(MyGraphics_HAIRMOD_PROTECTED g)
	{
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		for (int i = 0; i < mEffect.size(); i++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)mEffect.elementAt(i);
			effect_HAIRMOD_PROTECTED.paint(g);
		}
	}

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (last != null)
		{
			last.paint(g);
		}
		if (type == 5)
		{
			paintScreenXayBot(g);
		}
		else if (type == 6)
		{
			paintListItem(g);
		}
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		paintItemEp(g);
		paintlazer(g);
		paintEffect(g);
		if (type == 5)
		{
			paintlazer(g);
			paintResult(g);
		}
		for (int i = 0; i < mShot.size(); i++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)mShot.elementAt(i);
			effect_HAIRMOD_PROTECTED.paint(g);
		}
		paintShowText(g);
		base.paint(g);
	}

	public void paintItemEp(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (type == 5)
		{
			for (int i = 0; i < xItem.Length; i++)
			{
				if (i % 4 == 0)
				{
					g.setColor(16777215);
					g.fillRect(xItem[i], yItem[i] - 10, size, size);
					g.setColor(6513507);
					g.fillRect(xItem[i] + 1, yItem[i] - 9, size - 2, size - 2);
				}
				else
				{
					g.drawRegion(Res_HAIRMOD_PROTECTED.getImgArrow(10), 0, frame * Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][10], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[0][10], Arrow_HAIRMOD_PROTECTED.ARROWSIZE[1][10], 0, xItem[i] - 3, yItem[i] - 10, 0);
				}
			}
		}
		else if (type == 6)
		{
			for (int j = 0; j < xItem.Length; j++)
			{
				g.setColor(16777215);
				g.fillRect(xItem[j], yItem[j] - 10, size, size);
				g.setColor(6513507);
				g.fillRect(xItem[j] + 1, yItem[j] - 9, size - 2, size - 2);
			}
		}
		if (type != 1)
		{
			if (itemSelect != null && type != 5 && type != 6)
			{
				itemSelect.paint(g, xItem[0] + 10, yItem[0]);
			}
			if (type == 6)
			{
				if (itemSelect != null)
				{
					itemSelect.paint(g, xItem[5] + size / 2, yItem[5] + 1);
					CheckHaveItemCell[5] = true;
				}
				if (selecItemNL1 != null)
				{
					selecItemNL1.paint(g, xItem[1] + size / 2, yItem[1] + 1);
					CheckHaveItemCell[0] = true;
				}
				if (CheckPaintItem[1])
				{
					Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selecItemNL2.id).idImage, xItem[2] + size / 2, yItem[2]);
					CheckHaveItemCell[1] = true;
				}
				if (selecItemNL3 != null)
				{
					selecItemNL3.paint(g, xItem[3] + size / 2, yItem[3] + 1);
					CheckHaveItemCell[2] = true;
				}
				if (selecItemNL4 != null)
				{
					selecItemNL4.paint(g, xItem[4] + size / 2, yItem[4] + 1);
					CheckHaveItemCell[3] = true;
				}
				if (selecItemNL6 != null)
				{
					selecItemNL6.paint(g, xItem[6] + size / 2, yItem[6] + 1);
					CheckHaveItemCell[4] = true;
				}
			}
			if (itemSelect != null && type == 5)
			{
				Canvas_HAIRMOD_PROTECTED.resetTrans(g);
				g.setClip(xItem[0], yItem[0] - size / 4 + hclip1 - 5, size, size);
				itemSelect.paint(g, xItem[0] + 11, yItem[0] + 1);
			}
		}
		if (type != 0 && isShowPopup && !infoPoup.Equals(string.Empty))
		{
			int num = yDr + h0 - 24;
			g.setColor(16777215);
			g.fillRect(xPopup - 60, num - 3, 120, 20);
			g.setColor(3222312);
			g.fillRect(xPopup - 59, num - 2, 118, 18);
			MFont_HAIRMOD_PROTECTED.normalFont[0].drawString(g, infoPoup, xPopup, num, 2);
			paintlazer(g);
		}
	}

	private void updateResult()
	{
		if (createBot && Canvas_HAIRMOD_PROTECTED.gameTick % 2 == 0 && hclip > 0)
		{
			hclip--;
			hclip1++;
		}
	}

	public override void update()
	{
		updateResult();
		updatelazer();
		for (int i = 0; i < mEffect.size(); i++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED = (Effect_HAIRMOD_PROTECTED)mEffect.elementAt(i);
			effect_HAIRMOD_PROTECTED?.update();
			if (effect_HAIRMOD_PROTECTED.wantDetroy && effect_HAIRMOD_PROTECTED.type == -1)
			{
				createBot = true;
				mEffect.removeElement(effect_HAIRMOD_PROTECTED);
			}
			if (effect_HAIRMOD_PROTECTED.wantDetroy)
			{
				mEffect.removeElement(effect_HAIRMOD_PROTECTED);
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone && listItem.size() > 0)
		{
			doView();
		}
		if (type == 6 && isDap)
		{
			center = cmdWaiting;
			cmdDapItemTim.caption = string.Empty;
			if (timeDlay >= 0)
			{
				timeDlay--;
			}
			if (timeDlay < 0 && lazer.size() <= 0)
			{
				timeDlay = 15;
				resetCheck();
				isDap = false;
			}
		}
		if (dem >= 0)
		{
			dem--;
		}
		if (dem < 0 && isShot && Canvas_HAIRMOD_PROTECTED.gameTick % 15 == 0 && tick < 3 && type == 6)
		{
			EffectDapDoTim_HAIRMOD_PROTECTED effectDapDoTim_HAIRMOD_PROTECTED = null;
			for (int j = 1; j < xItem.Length; j++)
			{
				effectDapDoTim_HAIRMOD_PROTECTED = new EffectDapDoTim_HAIRMOD_PROTECTED(xItem[j] + size / 2, yItem[j], xItem[0] + size / 2, yItem[0]);
				mShot.addElement(effectDapDoTim_HAIRMOD_PROTECTED);
				effectDapDoTim_HAIRMOD_PROTECTED = null;
			}
			tick++;
		}
		for (int k = 0; k < mShot.size(); k++)
		{
			Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED2 = (Effect_HAIRMOD_PROTECTED)mShot.elementAt(k);
			effect_HAIRMOD_PROTECTED2.update();
			if (effect_HAIRMOD_PROTECTED2.wantDetroy)
			{
				Effect_XayBot_HAIRMOD_PROTECTED effect_XayBot_HAIRMOD_PROTECTED = null;
				Effect_HAIRMOD_PROTECTED effect_HAIRMOD_PROTECTED3 = null;
				for (int l = 0; l < 10; l++)
				{
					effect_XayBot_HAIRMOD_PROTECTED = new Effect_XayBot_HAIRMOD_PROTECTED(xItem[0] + size / 2, yItem[0], 0, yItem[0] + 80, -1);
					mEffect.addElement(effect_XayBot_HAIRMOD_PROTECTED);
					effect_XayBot_HAIRMOD_PROTECTED = null;
				}
				effect_HAIRMOD_PROTECTED3 = new Effect_HAIRMOD_PROTECTED(xItem[0] + size / 2, yItem[0], 50);
				mEffect.addElement(effect_HAIRMOD_PROTECTED3);
				effect_HAIRMOD_PROTECTED3 = null;
				mDestroy.addElement(effect_HAIRMOD_PROTECTED2);
			}
		}
		for (int m = 0; m < mDestroy.size(); m++)
		{
			mShot.removeElement(mDestroy.elementAt(m));
		}
		mDestroy.removeAllElements();
		if (type == 5 || type == 6)
		{
			currentRow = 1;
		}
		frame++;
		if (frame >= 3)
		{
			frame = 0;
		}
		if (isdestroyitem && mEffect.size() <= 0 && type == 5)
		{
			if (timeDlay >= 0)
			{
				timeDlay--;
			}
			if (timeDlay < 0)
			{
				itemSelect = null;
				isdestroyitem = false;
				isXayBot = false;
				timeDlay = 15;
				cmdXay.caption = "Nghiền";
				center = cmdView;
				resetSelect();
			}
		}
		if (last != null)
		{
			last.update();
		}
		if (timeDelay > 0)
		{
			timeDelay--;
		}
		if (timeDelay <= 0)
		{
			timeDelay = 0;
		}
		bool flag = false;
		bool flag2 = false;
		if (Canvas_HAIRMOD_PROTECTED.currentDialog == null)
		{
			if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				if (mScrollShowText.FocusNew)
				{
					mScrollItemInventory.FocusNew = false;
					mScrollNguyenLieu.FocusNew = false;
				}
				else if (mScrollItemInventory.FocusNew)
				{
					mScrollShowText.FocusNew = false;
					mScrollNguyenLieu.FocusNew = false;
				}
				else if (mScrollNguyenLieu.FocusNew)
				{
					mScrollItemInventory.FocusNew = false;
					mScrollShowText.FocusNew = false;
				}
				if (mScrollItemInventory.FocusNew && !Canvas_HAIRMOD_PROTECTED.menu.showMenu && timeDelay == 0)
				{
					currentRow = 1;
					ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED = mScrollItemInventory.updateKey();
					if (scrollResult_HAIRMOD_PROTECTED.isDowning || scrollResult_HAIRMOD_PROTECTED.isFinish)
					{
						selectItemInventory = scrollResult_HAIRMOD_PROTECTED.selected;
						lastIndex1 = scrollResult_HAIRMOD_PROTECTED.selected;
						flag = true;
						if (right != null && isShowText && Canvas_HAIRMOD_PROTECTED.canTouch())
						{
							right.perform();
						}
					}
				}
				mScrollItemInventory.updatecm();
				if (mScrollNguyenLieu.FocusNew && !Canvas_HAIRMOD_PROTECTED.menu.showMenu && timeDelay == 0)
				{
					currentRow = 0;
					ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED2 = mScrollNguyenLieu.updateKey();
					if (scrollResult_HAIRMOD_PROTECTED2.isDowning || scrollResult_HAIRMOD_PROTECTED2.isFinish)
					{
						indexTabNguyenLieu = scrollResult_HAIRMOD_PROTECTED2.selected;
						lastIndex2 = scrollResult_HAIRMOD_PROTECTED2.selected;
						flag2 = true;
						if (right != null && isShowText && Canvas_HAIRMOD_PROTECTED.canTouch())
						{
							right.perform();
						}
					}
				}
				mScrollNguyenLieu.updatecm();
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED3 = null;
				if (mScrollShowText.FocusNew && !Canvas_HAIRMOD_PROTECTED.menu.showMenu)
				{
					scrollResult_HAIRMOD_PROTECTED3 = mScrollShowText.updateKey();
				}
				mScrollShowText.updatecm();
				if (!Canvas_HAIRMOD_PROTECTED.isPointerDown)
				{
					if (mScrollItemInventory.FocusNew && flag)
					{
						if (selectItemInventory > -1 && selectItemInventory <= listItem.size() - 1)
						{
							if (lastIndex1 != -1)
							{
								selectItemInventory = lastIndex1;
							}
							cmdView.perform();
						}
					}
					else if (mScrollNguyenLieu.FocusNew && flag2 && indexTabNguyenLieu > -1 && indexTabNguyenLieu <= vnguyenLieu.size() - 1)
					{
						if (lastIndex2 != -1)
						{
							indexTabNguyenLieu = lastIndex2;
						}
						cmdView.perform();
					}
					mScrollShowText.FocusNew = false;
					mScrollItemInventory.FocusNew = false;
					mScrollNguyenLieu.FocusNew = false;
				}
			}
			else if (!isShowText)
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED4 = mScrollItemInventory.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED4.isDowning || scrollResult_HAIRMOD_PROTECTED4.isFinish)
				{
					selectItemInventory = scrollResult_HAIRMOD_PROTECTED4.selected;
					mScrollItemInventory.moveTo(selectItemInventory * (wTab + 2));
				}
				mScrollItemInventory.updatecm();
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED5 = mScrollNguyenLieu.updateKey();
				if (scrollResult_HAIRMOD_PROTECTED5.isDowning || scrollResult_HAIRMOD_PROTECTED5.isFinish)
				{
					indexTabNguyenLieu = scrollResult_HAIRMOD_PROTECTED5.selected;
					mScrollNguyenLieu.moveTo(indexTabNguyenLieu * (wTab + 2));
				}
				mScrollNguyenLieu.updatecm();
			}
			else
			{
				ScrollResult_HAIRMOD_PROTECTED scrollResult_HAIRMOD_PROTECTED6 = mScrollShowText.updateKey();
				mScrollShowText.updatecm();
			}
		}
		if (Canvas_HAIRMOD_PROTECTED.isPointerDownItem && type == 6)
		{
			for (int n = 1; n < xItem.Length; n++)
			{
				if (Canvas_HAIRMOD_PROTECTED.px >= xItem[n] && Canvas_HAIRMOD_PROTECTED.px <= xItem[n] + size && Canvas_HAIRMOD_PROTECTED.py >= yItem[n] - 10 && Canvas_HAIRMOD_PROTECTED.py <= yItem[n] - 10 + size)
				{
					currentCell = (sbyte)n;
					selectItemInventory = 0;
					mScrollItemInventory.moveTo(wTab);
				}
			}
			Canvas_HAIRMOD_PROTECTED.isPointerDownItem = false;
		}
		base.update();
	}

	private bool checKselecitem(ItemInInventory_HAIRMOD_PROTECTED item)
	{
		switch (currentCell)
		{
		case 1:
			if ((itemSelect != null && itemSelect.itemID == item.itemID) || (selecItemNL3 != null && selecItemNL3.itemID == item.itemID) || (selecItemNL4 != null && selecItemNL4.itemID == item.itemID) || (selecItemNL6 != null && selecItemNL6.itemID == item.itemID))
			{
				return true;
			}
			break;
		case 3:
			if ((itemSelect != null && itemSelect.itemID == item.itemID) || (selecItemNL1 != null && selecItemNL1.itemID == item.itemID) || (selecItemNL4 != null && selecItemNL4.itemID == item.itemID) || (selecItemNL6 != null && selecItemNL6.itemID == item.itemID))
			{
				return true;
			}
			break;
		case 4:
			if ((itemSelect != null && itemSelect.itemID == item.itemID) || (selecItemNL3 != null && selecItemNL3.itemID == item.itemID) || (selecItemNL1 != null && selecItemNL1.itemID == item.itemID) || (selecItemNL6 != null && selecItemNL6.itemID == item.itemID))
			{
				return true;
			}
			break;
		case 5:
			if ((selecItemNL1 != null && selecItemNL1.itemID == item.itemID) || (selecItemNL3 != null && selecItemNL3.itemID == item.itemID) || (selecItemNL4 != null && selecItemNL4.itemID == item.itemID) || (selecItemNL6 != null && selecItemNL6.itemID == item.itemID))
			{
				return true;
			}
			break;
		case 6:
			if ((itemSelect != null && itemSelect.itemID == item.itemID) || (selecItemNL3 != null && selecItemNL3.itemID == item.itemID) || (selecItemNL4 != null && selecItemNL4.itemID == item.itemID) || (selecItemNL1 != null && selecItemNL1.itemID == item.itemID))
			{
				return true;
			}
			break;
		}
		return false;
	}

	public static bool isPointer(int x, int y, int w, int h)
	{
		if (Canvas_HAIRMOD_PROTECTED.isPointerDown)
		{
			if (Canvas_HAIRMOD_PROTECTED.px >= x && Canvas_HAIRMOD_PROTECTED.px <= x + w && Canvas_HAIRMOD_PROTECTED.py >= y && Canvas_HAIRMOD_PROTECTED.py <= y + h)
			{
				return false;
			}
			return true;
		}
		return false;
	}

	public override void updateKey()
	{
		if (isShowText)
		{
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[2] && type != 5)
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
				mScrollShowText.cmtoY -= 50;
				if (mScrollShowText.cmtoY < 0)
				{
					mScrollShowText.cmtoY = 0;
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
				mScrollShowText.cmtoY += 50;
				if (mScrollShowText.cmtoY > mScrollShowText.cmyLim)
				{
					mScrollShowText.cmtoY = mScrollShowText.cmyLim;
				}
			}
		}
		else
		{
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[2] && type != 5)
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
				currentCell--;
				if (selecItemNL1 != null)
				{
					LisItemSelect.addElement(selecItemNL1);
				}
				if (selecItemNL3 != null)
				{
					LisItemSelect.addElement(selecItemNL3);
				}
				if (selecItemNL4 != null)
				{
					LisItemSelect.addElement(selecItemNL4);
				}
				if (itemSelect != null)
				{
					LisItemSelect.addElement(itemSelect);
				}
				if (selecItemNL6 != null)
				{
					LisItemSelect.addElement(selecItemNL6);
				}
				DropItem = false;
				if (currentCell < 1)
				{
					currentCell = 6;
				}
				if (type == 6)
				{
					selectItemInventory = 0;
					mScrollItemInventory.moveTo(wTab);
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
				DropItem = false;
				if (selecItemNL1 != null)
				{
					LisItemSelect.addElement(selecItemNL1);
				}
				if (selecItemNL3 != null)
				{
					LisItemSelect.addElement(selecItemNL3);
				}
				if (selecItemNL4 != null)
				{
					LisItemSelect.addElement(selecItemNL4);
				}
				if (itemSelect != null)
				{
					LisItemSelect.addElement(itemSelect);
				}
				if (selecItemNL6 != null)
				{
					LisItemSelect.addElement(selecItemNL6);
				}
				currentCell++;
				if (currentCell > 6)
				{
					currentCell = 1;
				}
				if (type == 6)
				{
					selectItemInventory = 0;
					mScrollItemInventory.moveTo(wTab);
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				selectItemInventory--;
				if (selectItemInventory < 0)
				{
					selectItemInventory = listItem.size() - 1;
				}
				mScrollItemInventory.moveTo(selectItemInventory * wTab);
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				selectItemInventory++;
				if (selectItemInventory > listItem.size() - 1)
				{
					selectItemInventory = 0;
				}
				mScrollItemInventory.moveTo(selectItemInventory * wTab);
			}
		}
		base.updateKey();
	}

	public static bool isBH(int id)
	{
		return id >= 0 && id <= 4;
	}

	public static bool isDaMayMan(int id)
	{
		return (id >= 5 && id <= 7) || id == 155 || id == 156;
	}

	public static bool isLKD(int id)
	{
		return (id >= 8 && id <= 11) || id == 66 || id == 245 || id == 157 || id == 158;
	}

	public void setInfoItem(int itemid)
	{
		if (selectItemInventory < listItem.size())
		{
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
			if (itemInInventory_HAIRMOD_PROTECTED != null && itemInInventory_HAIRMOD_PROTECTED.itemID == itemid)
			{
				arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(itemInInventory_HAIRMOD_PROTECTED.getDescription(false), wSh - 10);
			}
		}
	}

	public void doFinishInbue()
	{
		getListGem(false, true);
		isShowPopup = true;
	}

	public override void switchToMe()
	{
		base.switchToMe();
	}

	public override void switchToMe(MyScreen_HAIRMOD_PROTECTED lastSCR)
	{
		last = lastSCR;
		base.switchToMe(lastSCR);
	}
}
