public class ScreenDapDo_HAIRMOD_PROTECTED : MyScreen_HAIRMOD_PROTECTED
{
	public const sbyte TYPE2 = 0;

	public const sbyte TYPE4 = 1;

	public const sbyte TYPE_ANIMAL = 2;

	public const sbyte TYPE_LOCK_ITEM = 3;

	public const sbyte TYPE_THEM_DONG = 4;

	public static ScreenDapDo_HAIRMOD_PROTECTED me;

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

	public string tile = "Đập đồ";

	public bool isShowText;

	public sbyte type;

	private int[] xItem;

	private int[] yItem;

	private int[] xTo;

	private int[] yTo;

	private bool isEp;

	private bool isShowPopup;

	private int xPopup;

	private int yPopup;

	public string infoPoup = "Thong bao";

	private int cout;

	public static mVector_HAIRMOD_PROTECTED listItem = new mVector_HAIRMOD_PROTECTED();

	public sbyte currentRow;

	private int selectItem;

	private Command_HAIRMOD_PROTECTED cmdClose;

	private Command_HAIRMOD_PROTECTED cmdErr;

	private Command_HAIRMOD_PROTECTED cmdView;

	private Command_HAIRMOD_PROTECTED cmdEp;

	private Command_HAIRMOD_PROTECTED cmdContinue;

	public static ItemInInventory_HAIRMOD_PROTECTED itemSelect;

	public static GemTemplate_HAIRMOD_PROTECTED selectGem = null;

	public static GemTemplate_HAIRMOD_PROTECTED selectGemBH = null;

	public static GemTemplate_HAIRMOD_PROTECTED selectGemMM = null;

	public static sbyte COLOR_WHITE = 0;

	public static sbyte COLOR_GREEN = 1;

	public static sbyte COLOR_RED = 2;

	public static sbyte COLOR_BLUE = 3;

	public static sbyte COLOR_MAGENTA = 4;

	public static sbyte COLOR_YELLOW = 5;

	public static bool LKDLock;

	public static bool MMLock;

	public static bool BHLock;

	public static bool isFinishImbue = true;

	public static short idLkd = -1;

	public static short idMM = -1;

	public static short idBH = -1;

	public static short itemID;

	public int timeDelay;

	public ScreenDapDo_HAIRMOD_PROTECTED()
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
				else
				{
					left = cmdEp;
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
			mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED3 = new mVector_HAIRMOD_PROTECTED();
			mVector_HAIRMOD_PROTECTED3.addElement(new Command_HAIRMOD_PROTECTED("Bỏ vào")
			{
				action = delegate
				{
					if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
					{
						timeDelay = 5;
					}
					if (currentRow == 1)
					{
						if (selectItemInventory != -1 && selectItemInventory <= listItem.size() - 1)
						{
							if (type == 1 && !isFinishImbue)
							{
								ActionPerform_HAIRMOD_PROTECTED action6 = delegate
								{
									right.perform();
									Canvas_HAIRMOD_PROTECTED.endDlg();
								};
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", action6);
							}
							else
							{
								itemSelect = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
							}
						}
					}
					else if (currentRow == 0)
					{
						if (type == 0 || type == 2 || type == 3)
						{
							selectGem = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(indexTabNguyenLieu);
						}
						else if (type == 4)
						{
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(indexTabNguyenLieu);
							GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
							if (isBot(gemByID.type))
							{
								selectGem = gemTemplate_HAIRMOD_PROTECTED;
							}
							else if (isDaTienCap(gemByID.type))
							{
								selectGemBH = gemTemplate_HAIRMOD_PROTECTED;
							}
						}
						else if (!isFinishImbue)
						{
							ActionPerform_HAIRMOD_PROTECTED action7 = delegate
							{
								right.perform();
								Canvas_HAIRMOD_PROTECTED.endDlg();
							};
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Không thể chọn khi đang trong quá trình luyện tự động.", action7);
						}
						else
						{
							GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED2 = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(indexTabNguyenLieu);
							if (isDaMayMan(gemTemplate_HAIRMOD_PROTECTED2.id))
							{
								selectGemMM = gemTemplate_HAIRMOD_PROTECTED2;
							}
							else if (isBH(gemTemplate_HAIRMOD_PROTECTED2.id))
							{
								selectGemBH = gemTemplate_HAIRMOD_PROTECTED2;
							}
							else if (isLKD(gemTemplate_HAIRMOD_PROTECTED2.id))
							{
								selectGem = gemTemplate_HAIRMOD_PROTECTED2;
							}
						}
					}
				}
			});
			mVector_HAIRMOD_PROTECTED3.addElement(new Command_HAIRMOD_PROTECTED("Thông tin")
			{
				action = delegate
				{
					if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
					{
						timeDelay = 5;
					}
					doView();
				}
			});
			Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED3, 3);
			Canvas_HAIRMOD_PROTECTED.currentDialog = null;
		};
		cmdEp = new Command_HAIRMOD_PROTECTED("Đập");
		cmdEp.action = delegate
		{
			currentRow = -1;
			if (vnguyenLieu.size() <= 0)
			{
				ActionPerform_HAIRMOD_PROTECTED action = delegate
				{
					right.perform();
					Canvas_HAIRMOD_PROTECTED.endDlg();
				};
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Không có nguyên liệu.", action);
			}
			else if (itemSelect != null)
			{
				if (type == 0 || type == 2 || type == 3 || type == 4)
				{
					if (selectGem != null)
					{
						if (type == 0)
						{
							GameService_HAIRMOD_PROTECTED.gI().doRequestTachNguyenLIeu(itemSelect.itemID, 1, selectGem.id, selectGem.ilock ? 1 : 0);
							left = cmdContinue;
							doEp();
						}
						else if (type == 2)
						{
							GameService_HAIRMOD_PROTECTED.gI().doRequestTachNguyenLIeu(itemSelect.itemID, 2, selectGem.id, selectGem.ilock ? 1 : 0);
							left = cmdContinue;
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
						}
						else if (type == 4)
						{
							if (selectGemBH != null)
							{
								GameService_HAIRMOD_PROTECTED.gI().doRequestThemThuocTinh(itemSelect.itemID, 5, selectGem.id, selectGem.ilock ? 1 : 0, selectGemBH.id, selectGemBH.ilock ? 1 : 0);
								left = cmdContinue;
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
							}
							else
							{
								ActionPerform_HAIRMOD_PROTECTED action2 = delegate
								{
									right.perform();
									Canvas_HAIRMOD_PROTECTED.endDlg();
								};
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa đặt đá", action2);
							}
						}
						else
						{
							GameService_HAIRMOD_PROTECTED.gI().doRequestTachNguyenLIeu(itemSelect.itemID, 3, selectGem.id, selectGem.ilock ? 1 : 0);
							left = cmdContinue;
							Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
						}
					}
					else
					{
						ActionPerform_HAIRMOD_PROTECTED action3 = delegate
						{
							right.perform();
							Canvas_HAIRMOD_PROTECTED.endDlg();
						};
						Canvas_HAIRMOD_PROTECTED.startOKDlg((type != 4) ? "Bạn chưa chọn đá thuộc tính." : "Bạn chưa chọn bột", action3);
					}
				}
				else if (selectGem == null)
				{
					ActionPerform_HAIRMOD_PROTECTED action4 = delegate
					{
						right.perform();
						Canvas_HAIRMOD_PROTECTED.endDlg();
					};
					Canvas_HAIRMOD_PROTECTED.startOKDlg("Bạn chưa chọn luyện kim dược", action4);
				}
				else
				{
					mVector_HAIRMOD_PROTECTED idgem = new mVector_HAIRMOD_PROTECTED();
					mVector_HAIRMOD_PROTECTED ilock = new mVector_HAIRMOD_PROTECTED();
					idgem.addElement(selectGem.id + string.Empty);
					ilock.addElement((selectGem.ilock ? 1 : 0) + string.Empty);
					LKDLock = selectGem.ilock;
					idLkd = selectGem.id;
					if (selectGemBH != null)
					{
						idgem.addElement(selectGemBH.id + string.Empty);
						ilock.addElement((selectGemBH.ilock ? 1 : 0) + string.Empty);
						BHLock = selectGemBH.ilock;
						idBH = selectGemBH.id;
					}
					else
					{
						idgem.addElement("-1");
						ilock.addElement("0");
						BHLock = false;
						idBH = -1;
					}
					if (selectGemMM != null)
					{
						idgem.addElement(selectGemMM.id + string.Empty);
						ilock.addElement((selectGemMM.ilock ? 1 : 0) + string.Empty);
						MMLock = selectGemMM.ilock;
						idMM = selectGemMM.id;
					}
					else
					{
						idgem.addElement("-1");
						ilock.addElement("0");
						MMLock = false;
						idMM = -1;
					}
					itemID = itemSelect.itemID;
					mVector_HAIRMOD_PROTECTED mVector_HAIRMOD_PROTECTED2 = new mVector_HAIRMOD_PROTECTED();
					for (int i = 1; i <= 15; i++)
					{
						int plus = i;
						mVector_HAIRMOD_PROTECTED2.addElement(new Command_HAIRMOD_PROTECTED("Cộng " + i)
						{
							action = delegate
							{
								GameService_HAIRMOD_PROTECTED.gI().doAutoImbue(itemSelect.itemID, idgem, plus, ilock, -1);
								isShowPopup = true;
								infoPoup = "Xin chờ";
								isFinishImbue = false;
								Canvas_HAIRMOD_PROTECTED.startOKDlg("Xin chờ");
							}
						});
					}
					Canvas_HAIRMOD_PROTECTED.menu.startAt(mVector_HAIRMOD_PROTECTED2, 3);
					Canvas_HAIRMOD_PROTECTED.currentDialog = null;
				}
			}
			else
			{
				ActionPerform_HAIRMOD_PROTECTED action5 = delegate
				{
					right.perform();
					Canvas_HAIRMOD_PROTECTED.endDlg();
				};
				Canvas_HAIRMOD_PROTECTED.startOKDlg("Chưa đặt vật phẩm.", action5);
			}
		};
		cmdContinue = new Command_HAIRMOD_PROTECTED("Tiếp tục");
		cmdContinue.action = delegate
		{
			setSizeCell(type, true);
		};
		cmdErr = new Command_HAIRMOD_PROTECTED(string.Empty);
		cmdErr.action = delegate
		{
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

	public static bool isBot(int type)
	{
		return type == GemTemp_HAIRMOD_PROTECTED.TYPE_BOT;
	}

	public bool isDaTienCap(int type)
	{
		return type == GemTemp_HAIRMOD_PROTECTED.TYPE_DA_TIEN_GIAI;
	}

	public static ScreenDapDo_HAIRMOD_PROTECTED gI()
	{
		return (me != null) ? me : (me = new ScreenDapDo_HAIRMOD_PROTECTED());
	}

	public void setData(Message_HAIRMOD_PROTECTED msg)
	{
	}

	protected void doEp()
	{
		int num = 44;
		int num2 = xDr + wCell / 2 - num;
		int num3 = yDr + hCell / 2 - 16;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			num2 = xDr + wCell / 4 - num;
		}
		xTo[0] = (xTo[1] = num2 + num - 11);
		cout = 10;
		isEp = true;
	}

	protected void doView()
	{
		if (currentRow == 1)
		{
			if (Char_HAIRMOD_PROTECTED.inventory.size() == 0 || selectItemInventory < 0 || selectItemInventory > listItem.size() - 1)
			{
				return;
			}
			ItemInInventory_HAIRMOD_PROTECTED itemInInventory_HAIRMOD_PROTECTED = (ItemInInventory_HAIRMOD_PROTECTED)listItem.elementAt(selectItemInventory);
			if (!itemInInventory_HAIRMOD_PROTECTED.isEnoughData)
			{
				itemInInventory_HAIRMOD_PROTECTED.isEnoughData = true;
				GameService_HAIRMOD_PROTECTED.gI().requestItemInfo(itemInInventory_HAIRMOD_PROTECTED.itemID, Canvas_HAIRMOD_PROTECTED.gameScr.mainChar.ID);
			}
			arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(itemInInventory_HAIRMOD_PROTECTED.getDescription(false), wSh - 10);
		}
		else if (currentRow == 0)
		{
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(indexTabNguyenLieu);
			GemTemp_HAIRMOD_PROTECTED gemByID = Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id);
			string text = ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.name, "0");
			text += ItemInInventory_HAIRMOD_PROTECTED.setTemp(gemByID.decript, "0");
			arrShowText = MFont_HAIRMOD_PROTECTED.normalFont[0].splitFontBStrInLine(text, wSh - 10);
		}
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
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
		isShowText = true;
		if (!Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			center = cmdErr;
		}
		left = cmdErr;
	}

	public void resetData()
	{
		mScrollItemInventory.clear();
		mScrollNguyenLieu.clear();
		mScrollShowText.clear();
		vnguyenLieu = new mVector_HAIRMOD_PROTECTED();
		index = (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? (-1) : 0);
		selectItemInventory = 0;
		indexTabNguyenLieu = 0;
		isShowPopup = false;
		setSizeCell(type, false);
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

	public void setSizeCell(sbyte type, bool isContinue)
	{
		int num = 44;
		int num2 = xDr + wCell / 2 - num;
		int num3 = yDr + hCell / 2 - 35;
		if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
		{
			num2 = xDr + wCell / 4 - num;
		}
		switch (type)
		{
		case 0:
			xItem = new int[2];
			xTo = new int[2];
			yItem = new int[2];
			xItem[0] = num2;
			xItem[1] = num2 + num + 22;
			yItem[0] = num3;
			yItem[1] = num3;
			xTo[0] = xItem[0];
			xTo[1] = xItem[1];
			break;
		case 4:
			cmdEp.caption = "Đập";
			xItem = new int[3];
			xTo = new int[3];
			yItem = new int[3];
			xItem[1] = num2 + num + 20;
			xItem[0] = num2 + num - 11;
			xItem[2] = num2 + num - 42;
			yItem[1] = num3 + 22;
			yItem[0] = num3 - 11;
			yItem[2] = num3 + 22;
			xTo[0] = xItem[0];
			xTo[1] = xItem[1];
			xTo[2] = xItem[1];
			xPopup = num2 + num - 1;
			yPopup = num3 + 40;
			break;
		default:
			xItem = new int[4];
			xTo = new int[4];
			yItem = new int[4];
			xItem[1] = num2 - 22;
			xItem[2] = num2 + num - 11;
			xItem[3] = num2 + num + 22 + 22;
			xItem[0] = num2 + num - 11;
			yItem[1] = num3 + 11;
			yItem[2] = num3 + 11;
			yItem[3] = num3 + 11;
			yItem[0] = num3 - 18;
			xTo[0] = xItem[0];
			xTo[1] = xItem[1];
			xTo[2] = xItem[2];
			xTo[3] = xItem[3];
			xPopup = num2 + num - 1;
			yPopup = num3 + 40;
			break;
		}
		cout = 0;
		if (isContinue)
		{
			currentRow = 0;
			if (Canvas_HAIRMOD_PROTECTED.isSmartPhone)
			{
				currentRow = -1;
			}
			left = cmdEp;
			isShowText = false;
			mScrollShowText.clear();
			isEp = false;
		}
		else
		{
			isEp = false;
			left = cmdEp;
		}
		this.type = type;
		resetSelect();
		getListGem(true, false);
	}

	public void getListGem(bool getItem, bool recheck)
	{
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
			else if (type == 4)
			{
				if (gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_BOT || gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_DA_TIEN_GIAI)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGem.ilock && gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_BOT)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED;
						num2 = 1;
					}
					if (selectGemBH != null && selectGemBH.id == gemTemplate_HAIRMOD_PROTECTED.id && !selectGemBH.ilock && gemByID.type == GemTemp_HAIRMOD_PROTECTED.TYPE_DA_TIEN_GIAI)
					{
						selectGemBH = gemTemplate_HAIRMOD_PROTECTED;
						num3 = 1;
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
			else if (type == 4)
			{
				if (gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_BOT || gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_DA_TIEN_GIAI)
				{
					if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGem.ilock && gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_BOT)
					{
						selectGem = gemTemplate_HAIRMOD_PROTECTED2;
						num6 = 1;
					}
					if (selectGemBH != null && selectGemBH.id == gemTemplate_HAIRMOD_PROTECTED2.id && selectGemBH.ilock && gemByID2.type == GemTemp_HAIRMOD_PROTECTED.TYPE_DA_TIEN_GIAI)
					{
						selectGemBH = gemTemplate_HAIRMOD_PROTECTED2;
						num7 = 1;
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
			if (type == 0 || type == 4)
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
				listItem = ItemInInventory_HAIRMOD_PROTECTED.getAllItem();
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

	public void doFinishInbue()
	{
		getListGem(false, true);
		isShowPopup = true;
	}

	public void resetSelect()
	{
		selectGem = null;
		selectGemBH = null;
		selectGemMM = null;
		itemSelect = null;
		getListGem(true, false);
	}

	public void updatEp()
	{
		cout--;
		if (cout < 0)
		{
			cout = 0;
		}
		if (cout > 0)
		{
			return;
		}
		if (xItem[0] != xTo[0])
		{
			if (xTo[0] - xItem[0] >> 1 == 0)
			{
				xItem[0] = xTo[0];
			}
			else
			{
				xItem[0] += xTo[0] - xItem[0] >> 1;
			}
		}
		if (xItem[1] != xTo[1])
		{
			if (xTo[1] - xItem[1] >> 1 == 0)
			{
				xItem[1] = xTo[1];
			}
			else
			{
				xItem[1] += xTo[1] - xItem[1] >> 1;
			}
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
			if (!isShowText)
			{
				return;
			}
		}
		else
		{
			if (!isShowText)
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

	public override void paint(MyGraphics_HAIRMOD_PROTECTED g)
	{
		if (last != null)
		{
			last.paint(g);
		}
		int num = ((!Canvas_HAIRMOD_PROTECTED.isSmartPhone || wCell != 300) ? wCell : (wCell / 2));
		Res_HAIRMOD_PROTECTED.paintDlgFull(g, xDr, yDr, wCell, hCell, 25, tile, true, h0 - 28);
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
		int num2 = (Canvas_HAIRMOD_PROTECTED.isSmartPhone ? (wCell - 2 - wSh) : (wCell - 2));
		mScrollNguyenLieu.setStyle(vnguyenLieu.size(), wTab + 2, xDr, yDr + h0, num2, 27, false, 0);
		mScrollNguyenLieu.setClip(g, xDr + 2, yDr + 23 + h0 - 28, num2 - 2, 31);
		for (int j = 0; j < vnguyenLieu.size(); j++)
		{
			if (indexTabNguyenLieu == j && currentRow == 0)
			{
				g.setColor(6513507);
				g.fillRect(xDr + wTab * j, yDr + 24 + h0 - 28, wTab, 26);
			}
			GemTemplate_HAIRMOD_PROTECTED gemTemplate_HAIRMOD_PROTECTED = (GemTemplate_HAIRMOD_PROTECTED)vnguyenLieu.elementAt(j);
			if (gemTemplate_HAIRMOD_PROTECTED.ilock)
			{
				g.setColor(3276851);
				g.fillRect(xDr + wTab * j, yDr + 24 + h0 - 28, wTab, 26);
				if (indexTabNguyenLieu == j && currentRow == 0)
				{
					g.setColor(6513507);
					g.fillRect(xDr + wTab * j, yDr + 24 + h0 - 28, wTab, 26);
				}
			}
			if (selectGem != null && selectGem.id == gemTemplate_HAIRMOD_PROTECTED.id && selectGem.ilock == gemTemplate_HAIRMOD_PROTECTED.ilock)
			{
				g.setColor(3268607);
				g.drawRect(xDr + wTab * j + 1, yDr + 24 + h0 - 28, wTab - 2, 25);
				g.drawRect(xDr + wTab * j + 2, yDr + 24 + h0 - 27, wTab - 4, 23);
			}
			if (selectGemBH != null && selectGemBH.id == gemTemplate_HAIRMOD_PROTECTED.id && selectGemBH.ilock == gemTemplate_HAIRMOD_PROTECTED.ilock)
			{
				g.setColor(238149631);
				g.drawRect(xDr + wTab * j + 1, yDr + 24 + h0 - 28, wTab - 2, 25);
				g.drawRect(xDr + wTab * j + 2, yDr + 24 + h0 - 27, wTab - 4, 23);
			}
			if (selectGemMM != null && selectGemMM.id == gemTemplate_HAIRMOD_PROTECTED.id && selectGemMM.ilock == gemTemplate_HAIRMOD_PROTECTED.ilock)
			{
				g.setColor(238149631);
				g.drawRect(xDr + wTab * j + 1, yDr + 24 + h0 - 28, wTab - 2, 25);
				g.drawRect(xDr + wTab * j + 2, yDr + 24 + h0 - 27, wTab - 4, 23);
			}
			Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(gemTemplate_HAIRMOD_PROTECTED.id).idImage, xDr + wTab * j + wTab / 2, yDr + 24 + h0 - 28 + 13);
			MFont_HAIRMOD_PROTECTED.smallFontColor[3].drawString(g, gemTemplate_HAIRMOD_PROTECTED.number + string.Empty, xDr + wTab * j + wTab - 1, yDr + 22 + h0 - MFont_HAIRMOD_PROTECTED.smallFontColor[3].getHeight(), MFont_HAIRMOD_PROTECTED.RIGHT);
			g.setColor(14527502);
			g.fillRect(xDr + wTab * j, yDr + 22 + h0 - 28, 1, 29);
		}
		g.fillRect(xDr + wTab * vnguyenLieu.size(), yDr + 22 + h0 - 28, 1, 28);
		Canvas_HAIRMOD_PROTECTED.resetTrans(g);
		paintItemEp(g);
		paintShowText(g);
		base.paint(g);
	}

	public void paintItemEp(MyGraphics_HAIRMOD_PROTECTED g)
	{
		for (int i = 0; i < xItem.Length; i++)
		{
			g.setColor(16777215);
			g.fillRect(xItem[i], yItem[i], 22, 22);
			if (cout % 2 == 0)
			{
				g.setColor(6513507);
				g.fillRect(xItem[i] + 1, yItem[i] + 1, 20, 20);
			}
		}
		if (type != 1)
		{
			if (selectGem != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selectGem.id).idImage, xItem[1] + 10, yItem[1] + 10);
			}
			if (itemSelect != null)
			{
				itemSelect.paint(g, xItem[0] + 10, yItem[0] + 10);
			}
			if (type == 4 && selectGemBH != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selectGemBH.id).idImage, xItem[2] + 10, yItem[2] + 10);
			}
		}
		else if (type == 1)
		{
			if (selectGem != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selectGem.id).idImage, xItem[2] + 10, yItem[2] + 10);
			}
			if (selectGemBH != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selectGemBH.id).idImage, xItem[1] + 10, yItem[1] + 10);
			}
			if (selectGemMM != null)
			{
				Res_HAIRMOD_PROTECTED.paintGem(g, Res_HAIRMOD_PROTECTED.getGemByID(selectGemMM.id).idImage, xItem[3] + 10, yItem[3] + 10);
			}
			if (itemSelect != null)
			{
				itemSelect.paint(g, xItem[0] + 10, yItem[0] + 10);
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
		}
	}

	public override void update()
	{
		if (last != null)
		{
			last.update();
		}
		updatEp();
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
		base.update();
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
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
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
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[2])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[2] = false;
				currentRow--;
				if (currentRow < 0)
				{
					currentRow = 0;
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[8])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[8] = false;
				currentRow++;
				if (currentRow > 1)
				{
					currentRow = 1;
				}
			}
			if (Canvas_HAIRMOD_PROTECTED.keyPressed[4])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[4] = false;
				if (currentRow == 1)
				{
					selectItemInventory--;
					if (selectItemInventory < 0)
					{
						selectItemInventory = listItem.size() - 1;
					}
					mScrollItemInventory.moveTo(selectItemInventory * wTab);
				}
				else if (currentRow == 0)
				{
					indexTabNguyenLieu--;
					if (indexTabNguyenLieu < 0)
					{
						indexTabNguyenLieu = vnguyenLieu.size() - 1;
					}
					mScrollNguyenLieu.moveTo(indexTabNguyenLieu * wTab);
				}
				else
				{
					selectItem--;
					if (selectItem < 0)
					{
						selectItem = xItem.Length - 1;
					}
				}
			}
			else if (Canvas_HAIRMOD_PROTECTED.keyPressed[6])
			{
				Canvas_HAIRMOD_PROTECTED.keyPressed[6] = false;
				if (currentRow == 1)
				{
					selectItemInventory++;
					if (selectItemInventory > listItem.size() - 1)
					{
						selectItemInventory = 0;
					}
					mScrollItemInventory.moveTo(selectItemInventory * wTab);
				}
				else if (currentRow == 0)
				{
					indexTabNguyenLieu++;
					if (indexTabNguyenLieu > vnguyenLieu.size() - 1)
					{
						indexTabNguyenLieu = 0;
					}
					mScrollNguyenLieu.moveTo(indexTabNguyenLieu * wTab);
				}
				else
				{
					selectItem--;
					if (selectItem < 0)
					{
						selectItem = xItem.Length - 1;
					}
				}
			}
		}
		base.updateKey();
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

	public override void switchToMe()
	{
		base.switchToMe();
	}

	public override void switchToMe(MyScreen_HAIRMOD_PROTECTED scr)
	{
		last = scr;
		base.switchToMe(scr);
	}
}
